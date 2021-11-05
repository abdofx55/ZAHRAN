package com.zahran.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import com.zahran.Activities.MainActivity.MainActivity;
import com.zahran.Activities.MainActivity.MainViewModel;
import com.zahran.R;

public class SplashScreen extends AppCompatActivity {
    ImageView forwardImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        final Intent intent = new Intent(SplashScreen.this , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        forwardImageView = findViewById(R.id.forward_splash);
        if (MainViewModel.isRunning) {
            forwardImageView.setVisibility(View.VISIBLE);
        }

        forwardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });




        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!MainViewModel.isRunning) {
                    startActivity(intent);
                }
            }
        }, 2000);

    }



    @Override
    protected void onRestart() {
        super.onRestart();
        forwardImageView.setVisibility(View.VISIBLE);
    }
}
