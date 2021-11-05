package com.zahran.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;
@Dao
public interface PersonDao {

    @Query("SELECT * FROM family")
    LiveData<List<PersonEntity>> loadAllPersons();

    @Query("SELECT * FROM family WHERE father = :father")
    LiveData<List<PersonEntity>> loadPersonsByFather(String father);

    @Query("SELECT * FROM family WHERE gen = :gen AND father = :father")
    LiveData<List<PersonEntity>> loadPersonsByGenAndFather(int gen , String father);
}
