package ru.geekbrains.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.geekbrains.myapplication.interfaces.OpenWeather;
import ru.geekbrains.myapplication.model.WeatherRequest;

import static android.content.Context.MODE_PRIVATE;

public class FragmentMain extends Fragment {
    private View mainView;
    private EditText editTextCity;
    private TextView textTemp;
    private Button btnEnterCity;
    private OpenWeather openWeather;
    private ImageView iconWeather;
    private SharedPreferences sharedPref;
    private String prefCity;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        savePreferences();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.content_main, null);
        initRetorfit();
        initGui();
        initOnClickEvents();
        initPreferences();
        return mainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestWeather(null);
    }

    private void initPreferences(){
        sharedPref =  getActivity().getPreferences(MODE_PRIVATE);
        loadPreferences();    // загрузить настройки
    }

    private void loadPreferences() {
        prefCity = sharedPref.getString("city", "Moscow");
        editTextCity.setText(prefCity);
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("city", editTextCity.getText().toString());
        editor.apply();
    }

    private void initOnClickEvents() {
        // реакция на нажатие кнопки
        btnEnterCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWeather(v);
            }
        });
    }

    private void requestWeather(@Nullable View view) {
        if (prefCity != null && prefCity.length() > 1 && view == null) {
            requestRetrofit(prefCity, "3d1ebc018f306cf73036dd285969216c");
        }
        if (editTextCity.getText().toString().equals("")){
            requestRetrofit("Moscow", "3d1ebc018f306cf73036dd285969216c");
        } else {
            requestRetrofit(editTextCity.getText().toString(), "3d1ebc018f306cf73036dd285969216c");
        }
    }

    private void initRetorfit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
// Базовая часть адреса
                .baseUrl("http://api.openweathermap.org/")
// Конвертер, необходимый для преобразования JSON в объекты
                .addConverterFactory(GsonConverterFactory.create())
                .build();
// Создаем объект, при помощи которого будем выполнять запросы
        openWeather = retrofit.create(OpenWeather.class);
    }

    private void requestRetrofit(String city, String keyApi){
        openWeather.loadWeather(city, keyApi)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            float faring = response.body().getMain().getTemp();
                            int cels = (int) faring - 272;
                            String result = String.valueOf(cels) + " °С";
                            textTemp.setText(result);
                        }else {
                            textTemp.setText("Error body");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherRequest> call, Throwable t) {
                        textTemp.setText("Error");
                    }
                });

    }

    private void initGui() {
        editTextCity = mainView.findViewById(R.id.edit_text_city);
        textTemp = mainView.findViewById(R.id.text_temp_in_city);
        btnEnterCity = mainView.findViewById(R.id.btn_enter_city);
        iconWeather = mainView.findViewById(R.id.icon_weather);
        iconWeather.setBackgroundColor(Color.BLACK);
        Picasso
                .with(mainView.getContext())
                .load("https://openweathermap.org/themes/openweathermap/assets/img/openweather-negative-logo-RGB.png")
                .into(iconWeather);

    }

}
