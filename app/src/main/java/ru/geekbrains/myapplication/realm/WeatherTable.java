package ru.geekbrains.myapplication.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class WeatherTable extends RealmObject {

    public WeatherTable(){}

    public WeatherTable(int id, String city, int temp){
    this.id = id;
    this.city = city;
    this.temp = temp;
    }

    @PrimaryKey
    private int id;
    private String city;
    private int temp;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
