package com.zahran.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "family")
public class PersonEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int gen;
    @NonNull private String father;
    @NonNull private String name;
    @ColumnInfo(name = "nick_name")
    private String nickName;
    private int gender;
    @ColumnInfo(name = "is_family")
    private int isFamily;

    public PersonEntity(int id, int gen, @NonNull String father, @NonNull String name, String nickName, int gender, int isFamily) {
        this.id = id;
        this.gen = gen;
        this.father = father;
        this.name = name;
        this.nickName = nickName;
        this.gender = gender;
        this.isFamily = isFamily;
    }

    @Ignore
    public PersonEntity(int gen, @NonNull String father, @NonNull String name, String nickName, int gender, int isFamily) {
        this.gen = gen;
        this.father = father;
        this.name = name;
        this.nickName = nickName;
        this.gender = gender;
        this.isFamily = isFamily;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGen() {
        return gen;
    }

    public void setGen(int gen) {
        this.gen = gen;
    }

    @NonNull
    public String getFather() {
        return father;
    }

    public void setFather(@NonNull String father) {
        this.father = father;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getIsFamily() {
        return isFamily;
    }

    public void setIsFamily(int isFamily) {
        this.isFamily = isFamily;
    }
}
