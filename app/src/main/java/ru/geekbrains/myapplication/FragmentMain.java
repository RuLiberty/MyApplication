package ru.geekbrains.myapplication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.geekbrains.myapplication.adapters.HistoryWeatherAdapter;
import ru.geekbrains.myapplication.interfaces.OpenWeather;
import ru.geekbrains.myapplication.model.WeatherRequest;
import ru.geekbrains.myapplication.realm.WeatherTable;

import static android.content.Context.MODE_PRIVATE;

public class FragmentMain extends Fragment {
    private View mainView;
    private AutoCompleteTextView editTextCity;
    private TextView textTemp;
    private Button btnEnterCity;
    private OpenWeather openWeather;
    private SharedPreferences sharedPref;
    private String prefCity;
    private HistoryWeatherAdapter historyWeatherAdapter;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        savePreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.content_main, null);
        initRetorfit();
        initGui();
        initOnClickEvents();
        Realm.init(Objects.requireNonNull(getContext()).getApplicationContext());
        return mainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPreferences();
        requestWeather(null);
    }

    private String firstUpperCase(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    // Realm function
    private void addElement(String city, int temp){
        if (isExist(city)) {
           editElement(city, temp);
       } else {
           Realm realm = Realm.getDefaultInstance();
           realm.beginTransaction();
           realm.copyToRealm(new WeatherTable(realm.where(WeatherTable.class).findAll().size(), city, temp));
           realm.commitTransaction();
           realm.close();
       }
        historyWeatherAdapter.notifyDataSetChanged();
    }

    public void deleteAll(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(WeatherTable.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
        historyWeatherAdapter.notifyDataSetChanged();
    }

    private void editElement(String city, int temp){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Objects.requireNonNull(realm.where(WeatherTable.class).equalTo("city", city).findFirst()).setTemp(temp);
        realm.commitTransaction();
        realm.close();
        historyWeatherAdapter.notifyDataSetChanged();
        }


    private void deleteElement(String city){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Objects.requireNonNull(realm.where(WeatherTable.class).equalTo("city", city).findFirst()).deleteFromRealm();
        realm.commitTransaction();
        realm.close();
        historyWeatherAdapter.notifyDataSetChanged();
    }

    private boolean isExist(String city) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        WeatherTable wtIsExist = realm.where(WeatherTable.class).
                equalTo("city", city).findFirst();
        realm.commitTransaction();
        realm.close();
        return wtIsExist != null;
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
            requestRetrofit(firstUpperCase(prefCity), "3d1ebc018f306cf73036dd285969216c");
        }
        if (editTextCity.getText().toString().equals("")){
            requestRetrofit("Moscow", "3d1ebc018f306cf73036dd285969216c");
        } else {
            requestRetrofit(firstUpperCase(editTextCity.getText().toString()), "3d1ebc018f306cf73036dd285969216c");
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

    private void requestRetrofit(final String city, String keyApi){
        openWeather.loadWeather(city, keyApi)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            float faring = response.body().getMain().getTemp();
                            int cels = (int) faring - 272;
                            String result = String.valueOf(cels) + " °С";
                            textTemp.setText(result);
                            addElement(city,cels);
                        }else {
                            textTemp.setText("Error body");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherRequest> call, Throwable t) {
                        textTemp.setText("Wrong city!");
                    }
                });

    }

    private void initGui() {
        editTextCity = mainView.findViewById(R.id.edit_text_city);
        textTemp = mainView.findViewById(R.id.text_temp_in_city);
        btnEnterCity = mainView.findViewById(R.id.btn_enter_city);
        RecyclerView recyclerView = mainView.findViewById(R.id.event_list);
        ImageView iconWeather = mainView.findViewById(R.id.icon_weather);
        iconWeather.setBackgroundColor(Color.BLACK);
        Picasso
                .with(mainView.getContext())
                .load("https://openweathermap.org/themes/openweathermap/assets/img/openweather-negative-logo-RGB.png")
                .into(iconWeather);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyWeatherAdapter = new HistoryWeatherAdapter();
        recyclerView.setAdapter(historyWeatherAdapter);

        // TODO костыль! Необходимо организовать выборку из RecycleView
        final String[] autoCity = {"Moscow","London"};
        editTextCity.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.item, autoCity));
    }

}
