package com.zahran.Activities.MainActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Util;
import com.zahran.Activities.AboutActivity;
import com.zahran.Activities.AddPersonActivity;
import com.zahran.Activities.InformationActivity.InformationActivity;
import com.zahran.Adapter;
import com.zahran.JobManager;
import com.zahran.R;
import com.zahran.Utilities.Tasks;
import com.zahran.data.PersonEntity;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private TextView fathersTextView;
    private ImageView treeImageView;
    BoomMenuButton bmb;
    private ListView listView;
    private Adapter adapter;
    private MainViewModel viewModel;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initValues();
        treeImageView.setAlpha(100);
        listView.setOnItemClickListener(this);

        installBMB();
//        isFather();

        // Firebase Job Dispatcher for Periodic Notification
        JobManager.scheduleNotification(this);


    }

    @Override
    public void onStart() {
        super.onStart();
        List<PersonEntity> currentList = MainViewModel.lists.get(MainViewModel.gen);
        if (!(currentList == null || currentList.isEmpty())){
            // if exist , Read from it
            adapter = new Adapter(context , currentList);
            listView.setAdapter(adapter);
        } else {
            // Read from Room
            viewModel.loadPersonsByGenAndFather(MainViewModel.gen, MainViewModel.father).observe(this, new Observer<List<PersonEntity>>() {
                @Override
                public void onChanged(List<PersonEntity> personEntities) {
                    MainViewModel.lists.add(MainViewModel.gen, personEntities);
                    adapter = new Adapter(context, personEntities);
                    listView.setAdapter(adapter);
                }
            });
        }
    }

    private void initValues() {
        context = MainActivity.this;
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        fathersTextView = findViewById(R.id.father_name);
        treeImageView = findViewById(R.id.background_main_activity);
        listView = findViewById(R.id.list_main_activity);
        bmb = findViewById(R.id.bmb);

        adapter = new Adapter(context, MainViewModel.lists.get(0));
        listView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<PersonEntity> currentList = MainViewModel.lists.get(MainViewModel.gen);
        // check list if null or empty --> return
        if (currentList == null || currentList.isEmpty()){
            return;
        }

        //get Father Name in English
        String currentPerson = currentList.get(position).getName();
        String currentFather = currentList.get(position).getFather();
        MainViewModel.father = currentPerson + " " + currentFather;
//        Log.v("************", futureFather + "    " + gen);


        viewModel.loadPersonsByGenAndFather((MainViewModel.gen+1) , MainViewModel.father).observe(this, new Observer<List<PersonEntity>>() {
            @Override
            public void onChanged(List<PersonEntity> personEntities) {
//                for (int i = 0; i < personEntities.size(); i++) {
//                    Log.v("************", personEntities.get(i).getPerson());
//                }
                if (personEntities.isEmpty()) {
                    return;
                }

                MainViewModel.gen++;
                MainViewModel.lists.add(MainViewModel.gen, personEntities);
                fathersTextView.setText(MainViewModel.father);
                adapter = new Adapter(context, personEntities);
                listView.setAdapter(adapter);

            }
        });
    }

    @Override
    public void onBackPressed() {
        MainViewModel.gen--;
        if (MainViewModel.gen == 1) {
            super.onBackPressed();
            return;
        }

        List<PersonEntity> lastList = MainViewModel.lists.get(MainViewModel.gen);
        //father name
        final String lastFather = lastList.get(0).getFather();

        if (!lastList.isEmpty()) {
            fathersTextView.setText(lastFather);
            adapter = new Adapter(context, MainViewModel.lists.get(MainViewModel.gen));
            listView.setAdapter(adapter);
        } else
            super.onBackPressed();
    }

    private void installBMB() {
        TextInsideCircleButton.Builder builder0 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_person_add)
                .imageRect(new Rect(Util.dp2px(10), Util.dp2px(10), Util.dp2px(65), Util.dp2px(58)))
                .normalText(getString(R.string.اضافة))
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        startActivity(new Intent(context , AddPersonActivity.class));
                    }
                });

        TextInsideCircleButton.Builder builder1 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_info_outline)
                .imageRect(new Rect(Util.dp2px(15), Util.dp2px(10), Util.dp2px(65), Util.dp2px(58)))
                .normalText(getString(R.string.معلومات))
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intent = new Intent(MainActivity.this , InformationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });


        TextInsideCircleButton.Builder builder2 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_person)
                .imageRect(new Rect(Util.dp2px(15), Util.dp2px(10), Util.dp2px(65), Util.dp2px(58)))
                .normalText(getString(R.string.المطور))
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Tasks.executeTask(context , Tasks.ACTION_DEVELOPER);
                    }
                });

        TextInsideCircleButton.Builder builder3 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_share4dp)
                .imageRect(new Rect(Util.dp2px(12), Util.dp2px(10), Util.dp2px(58), Util.dp2px(58)))
                .normalText(getString(R.string.مشاركة))
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Tasks.executeTask(context , Tasks.ACTION_SHARE);
                    }
                });


        TextInsideCircleButton.Builder builder4 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_stars)
                .imageRect(new Rect(Util.dp2px(15), Util.dp2px(10), Util.dp2px(65), Util.dp2px(58)))
                .normalText(getString(R.string.تقييم))
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Tasks.executeTask(context , Tasks.ACTION_RATE);
                    }
                });

        TextInsideCircleButton.Builder builder5 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_apps)
                .imageRect(new Rect(Util.dp2px(15), Util.dp2px(10), Util.dp2px(65), Util.dp2px(58)))
                .normalText(getString(R.string.المزيد))
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Tasks.executeTask(context , Tasks.ACTION_MORE_APPS);
                    }
                });

        TextInsideCircleButton.Builder builder6 = new TextInsideCircleButton.Builder()
                .normalImageRes(R.drawable.ic_phone_android)
                .imageRect(new Rect(Util.dp2px(15), Util.dp2px(10), Util.dp2px(65), Util.dp2px(54)))
                .normalText(getString(R.string.حول_التطبيق))
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        startActivity(new Intent(context , AboutActivity.class));
                    }
                });



        bmb.addBuilder(builder0);
        bmb.addBuilder(builder6);
        bmb.addBuilder(builder1);
        bmb.addBuilder(builder3);
        bmb.addBuilder(builder4);
        bmb.addBuilder(builder2);
        bmb.addBuilder(builder5);



    }



//    private void isFather(){
//        viewModel.loadAllPersons().observe(this, new Observer<List<PersonEntity>>() {
//            @Override
//            public void onChanged(List<PersonEntity> personEntities) {
//                for (int i = 0 ; i < personEntities.size() ; i++) {
//                    String fullName = personEntities.get(i).getName() + " " + personEntities.get(i).getFather();
////                    Log.v("***************", fullName);
//                    viewModel.loadPersonsByFather(fullName).observe(MainActivity.this, new Observer<List<PersonEntity>>() {
//                        @Override
//                        public void onChanged(List<PersonEntity> personEntities) {
//                            if (personEntities != null)
//                                Log.v("***************" , 1 + "");
//                            else
//                                Log.v("***************" , 0 + "");
//                        }
//                    });
//                }
//            }
//        });
//    }
}
