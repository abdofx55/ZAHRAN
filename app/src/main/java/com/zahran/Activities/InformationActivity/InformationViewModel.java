package com.zahran.Activities.InformationActivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.zahran.DataRepository;
import com.zahran.data.PersonEntity;

import java.util.List;

public class InformationViewModel extends AndroidViewModel {
    private LiveData<List<PersonEntity>> allPersons;

    public InformationViewModel(@NonNull Application application) {
        super(application);
        DataRepository repository = DataRepository.getInstance(this.getApplication());
        allPersons = repository.loadAllPersons();
    }

    public LiveData<List<PersonEntity>> loadAllPersons() {
        return allPersons;
    }
}
