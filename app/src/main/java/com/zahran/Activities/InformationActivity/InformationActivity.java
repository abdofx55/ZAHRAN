package com.zahran.Activities.InformationActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.zahran.Activities.MainActivity.MainViewModel;
import com.zahran.R;
import com.zahran.data.PersonEntity;

import java.util.List;

public class InformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        final TextView familyNumberTextView = findViewById(R.id.family_number);
        ImageView backgroundImageView = findViewById(R.id.information_background);
        TextView bodyTextView = findViewById(R.id.family_information_body);

        backgroundImageView.setAlpha(50);
        bodyTextView.setMovementMethod(new ScrollingMovementMethod());

        InformationViewModel viewModel = new ViewModelProvider(this).get(InformationViewModel.class);
        viewModel.loadAllPersons().observe(this, new Observer<List<PersonEntity>>() {
            @Override
            public void onChanged(List<PersonEntity> personEntities) {
                    ValueAnimator animator = ValueAnimator.ofInt(0, personEntities.size());
                    animator.setDuration(3000);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            familyNumberTextView.setText(animation.getAnimatedValue().toString());
                        }
                    });
                    animator.start();

            }
        });


    }
}
