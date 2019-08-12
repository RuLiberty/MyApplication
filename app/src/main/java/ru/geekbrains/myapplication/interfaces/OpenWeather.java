package ru.geekbrains.myapplication.interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.geekbrains.myapplication.model.WeatherRequest;


public interface OpenWeather {
    @GET("data/2.5/weather")
    Call<WeatherRequest> loadWeather(@Query("q") String cityCountry, @Query("appid") String keyApi);
}
