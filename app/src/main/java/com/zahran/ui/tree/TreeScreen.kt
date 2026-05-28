package com.zahran.ui.tree

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahran.R
import com.zahran.data.model.Person
import com.zahran.ui.UiState
import com.zahran.ui.components.EmptyView
import com.zahran.ui.components.ErrorView
import com.zahran.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeScreen(
    viewModel: TreeViewModel,
    onNavigateToInfo: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Handle system back press to traverse up the family tree
    BackHandler(enabled = state.navigationHistory.isNotEmpty()) {
        viewModel.handleIntent(TreeIntent.NavigateBack)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.currentParent?.fullName ?: stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (state.navigationHistory.isNotEmpty()) {
                        IconButton(onClick = { viewModel.handleIntent(TreeIntent.NavigateBack) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back),
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.معلومات)) },
                            onClick = {
                                showMenu = false
                                onNavigateToInfo()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.حول_التطبيق)) },
                            onClick = {
                                showMenu = false
                                onNavigateToAbout()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.تقييم)) },
                            onClick = {
                                showMenu = false
                                rateApp(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.مشاركة)) },
                            onClick = {
                                showMenu = false
                                shareApp(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.المزيد)) },
                            onClick = {
                                showMenu = false
                                openMoreApps(context)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddPerson,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(text = stringResource(id = R.string.اضافة), fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val uiState = state.uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Empty -> EmptyView()
                is UiState.Error -> ErrorView(
                    message = uiState.message,
                    onTryAgain = {
                        state.currentParent?.id?.let {
                            viewModel.handleIntent(TreeIntent.LoadMembers(it))
                        }
                    }
                )
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.data, key = { it.id }) { person ->
                            PersonRow(
                                person = person,
                                onClick = {
                                    viewModel.handleIntent(TreeIntent.NavigateToChild(person))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonRow(
    person: Person,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Premium theme colors based on gender and documentation state
    val cardColor = if (!person.isDocumented) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (!person.isDocumented) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val accentColor = if (person.gender == 2) {
        Color(0xFFE91E63) // Pink accent for females
    } else {
        MaterialTheme.colorScheme.primary // Green accent for males
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual gender/status avatar
            Surface(
                modifier = Modifier.size(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = accentColor
            ) {}

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    if (person.nickName.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${person.nickName})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (person.isDocumented) MaterialTheme.colorScheme.secondary else textColor
                        )
                    }
                }
                
                Text(
                    text = "${stringResource(id = R.string.شجرة_عائلة)} ${person.fullName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }

            // Arrow indicator
            Text(
                text = "←",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor.copy(alpha = if (person.isDocumented) 1f else 0.4f)
            )
        }
    }
}

// Action helpers
private fun shareApp(context: Context) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, context.getString(R.string.عنوان_المشاركة))
        putExtra(Intent.EXTRA_TEXT, "${context.getString(R.string.نص_المشاركة)}\nhttps://play.google.com/store/apps/details?id=com.zahran")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.مشاركة_بواسطة))
    context.startActivity(shareIntent)
}

private fun rateApp(context: Context) {
    val uri = Uri.parse("market://details?id=com.zahran")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        context.startActivity(goToMarket)
    } catch (e: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.zahran")))
    }
}

private fun openMoreApps(context: Context) {
    // Open publisher store search url
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=AHamdy"))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.لا_يوجد_جوجول), Toast.LENGTH_SHORT).show()
    }
}
