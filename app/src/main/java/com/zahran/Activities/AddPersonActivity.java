package com.zahran.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Util;
import com.zahran.R;
import com.zahran.Utilities.Tasks;

public class AddPersonActivity extends AppCompatActivity {
    BoomMenuButton bmb;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);
        ImageView backgroundImageView = findViewById(R.id.background_add_person);
        bmb = findViewById(R.id.bmb);
        context = AddPersonActivity.this;
        backgroundImageView.setAlpha(25);

        installBMB();
    }

    private void installBMB() {
        TextInsideCircleButton.Builder builder1 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_emai)
                .imageRect(new Rect(Util.dp2px(15), Util.dp2px(10), Util.dp2px(65), Util.dp2px(58)))
                .normalText(getString(R.string.mail))
                .pieceColorRes(R.color.background_color)
                .normalColor(R.color.background_color).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Tasks.executeTask(context , Tasks.ACTION_SEND_MAIL);
                    }
                });

        TextInsideCircleButton.Builder builder2 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_whats)
                .imageRect(new Rect(Util.dp2px(15), Util.dp2px(10), Util.dp2px(65), Util.dp2px(58)))
                .normalText(getString(R.string.whats))
                .pieceColorRes(R.color.background_color)
                .normalColor(R.color.background_color).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Tasks.executeTask(context , Tasks.ACTION_SEND_WHATSAPP);
                    }
                });


        bmb.addBuilder(builder1);
        bmb.addBuilder(builder2);
    }
}
