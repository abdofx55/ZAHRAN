package com.zahran.Utilities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.zahran.R;

public class Tasks {
    public static String ACTION_SHARE = "action_share";
    public static String ACTION_MORE_APPS = "action_more_apps";
    public static String ACTION_RATE = "action_rate";
    public static String ACTION_DEVELOPER = "action_developer";
    public static String ACTION_SEND_MAIL = "action_send_mail";
    public static String ACTION_SEND_WHATSAPP = "action_send_whatsapp";


    public static void executeTask(Context context, String action) {
        if (action.equals(ACTION_SHARE)) {
            shareApp(context);
        } else if (action.equals(ACTION_MORE_APPS)) {
            moreApps(context);
        } else if (action.equals(ACTION_RATE)) {
            rateApp(context);
        } else if (action.equals(ACTION_DEVELOPER)) {
            developer(context);
        } else if (action.equals(ACTION_SEND_MAIL)) {
            mail(context);
        } else if (action.equals(ACTION_SEND_WHATSAPP)) {
            whatsApp(context);
        }
    }


    private static void shareApp(Context context) {
        Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName());
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.عنوان_المشاركة));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.عنوان_المشاركة) + "\n" +
                context.getString(R.string.نص_المشاركة) + "\n" + uri.toString());
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sharingIntent);
    }

    private static void moreApps(Context context) {
        Intent playIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=%40AbdelRhman_Hamdy"));
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            playIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            if (playIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(playIntent);
            } else {
                Toast.makeText(context, context.getString(R.string.لا_يوجد_جوجول), Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void rateApp(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));

        }
    }

    private static void developer(Context context) {
        String url = "https://www.linkedin.com/in/abdelrhman-hamdy-55a6b618b/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    private static void mail(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", context.getString(R.string.الميل), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.عنوان_الميل));
        emailIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.نص_الميل));
        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } else {
            Toast.makeText(context, context.getString(R.string.لا_يوجد_ميل), Toast.LENGTH_LONG).show();
        }
    }

    private static void whatsApp(Context context) {
        //Check Whatsapp is installed or not ?
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            Intent whatsIntent = new Intent(Intent.ACTION_VIEW);
            whatsIntent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" +
                    context.getString(R.string.الرقم) + "&text=" + context.getString(R.string.نص_الميل)));
            context.startActivity(whatsIntent);

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.لا_يوجد_واتس), Toast.LENGTH_LONG).show();
        }
    }
}
