package com.zahran

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.zahran.ui.about.AboutScreen
import com.zahran.ui.addperson.AddPersonScreen
import com.zahran.ui.info.InfoScreen
import com.zahran.ui.info.InfoViewModel
import com.zahran.ui.navigation.AboutScreen as AboutDest
import com.zahran.ui.navigation.AddPersonScreen as AddPersonDest
import com.zahran.ui.navigation.InfoScreen as InfoDest
import com.zahran.ui.navigation.TreeScreen as TreeDest
import com.zahran.ui.tree.TreeScreen
import com.zahran.ui.tree.TreeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install AndroidX Splash Screen API
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Auto-subscribe to the notifications topic
        FirebaseMessaging.getInstance().subscribeToTopic("zahran_family")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("MainActivity", "Successfully subscribed to zahran_family topic")
                } else {
                    android.util.Log.e("MainActivity", "Failed to subscribe to topic", task.exception)
                }
            }
        
        setContent {
            MaterialTheme {
                // Force RTL Layout Direction (Arabic first)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        
                        NavHost(
                            navController = navController,
                            startDestination = TreeDest
                        ) {
                            composable<TreeDest> {
                                val viewModel: TreeViewModel = hiltViewModel()
                                TreeScreen(
                                    viewModel = viewModel,
                                    onNavigateToInfo = { navController.navigate(InfoDest) },
                                    onNavigateToAbout = { navController.navigate(AboutDest) },
                                    onNavigateToAddPerson = { navController.navigate(AddPersonDest) }
                                )
                            }
                            
                            composable<InfoDest> {
                                val viewModel: InfoViewModel = hiltViewModel()
                                InfoScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable<AboutDest> {
                                AboutScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable<AddPersonDest> {
                                AddPersonScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
