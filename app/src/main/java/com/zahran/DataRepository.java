package com.zahran;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.zahran.data.Database;
import com.zahran.data.PersonDao;
import com.zahran.data.PersonEntity;

import java.util.List;

public class DataRepository {
    private static DataRepository sInstance;
    private final PersonDao personDao;

    private DataRepository(Application application) {
        Database db = Database.getInstance(application);
        personDao = db.personDao();
    }

    public static DataRepository getInstance(Application application) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(application);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<PersonEntity>> loadAllPersons() {
        return personDao.loadAllPersons();
    }

    public LiveData<List<PersonEntity>> loadPersonsByFather(String father) {
        return personDao.loadPersonsByFather(father);
    }

    public LiveData<List<PersonEntity>> loadPersonsByGenAndFather(int gen, String father) {
        return personDao.loadPersonsByGenAndFather(gen , father);
    }
}
