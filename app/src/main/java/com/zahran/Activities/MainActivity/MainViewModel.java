package com.zahran.Activities.MainActivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.zahran.DataRepository;
import com.zahran.R;
import com.zahran.data.PersonEntity;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    static int gen;
    static String father;
    static List<List<PersonEntity>> lists;
    public static boolean isRunning;


    private DataRepository repository;
//    private static LiveData<List<PersonEntity>> allPersons;



    public MainViewModel(@NonNull Application application) {
        super(application);
        initialize();
        repository = DataRepository.getInstance(this.getApplication());
//        allPersons = repository.loadAllPersons();
    }

    private void initialize() {
        gen = 2;
        father = getApplication().getString(R.string.زهران);
        lists = new ArrayList<>();
        for (int i = 0 ; i < 20 ; i++) {
            lists.add(new ArrayList<PersonEntity>());
        }
        isRunning = true;
    }

//    public static LiveData<List<PersonEntity>> loadAllPersons() {
//        return allPersons;
//    }
//
//    public LiveData<List<PersonEntity>> loadPersonsByFather(String father) {
//        return repository.loadPersonsByFather(father);
//    }

    LiveData<List<PersonEntity>> loadPersonsByGenAndFather(int gen, String father) {
//        Log.v("************", father + "    " + gen);
        return repository.loadPersonsByGenAndFather(gen, father);
    }
}
