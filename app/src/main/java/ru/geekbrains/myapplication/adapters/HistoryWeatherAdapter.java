package ru.geekbrains.myapplication.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.Realm;
import ru.geekbrains.myapplication.R;
import ru.geekbrains.myapplication.realm.WeatherTable;

public class HistoryWeatherAdapter extends RecyclerView.Adapter<HistoryWeatherAdapter.ViewHolder> {

    @NonNull
    @Override
    public HistoryWeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryWeatherAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bind(position);
    }

    @Override
    public int getItemCount() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        int size = realm.where(WeatherTable.class).findAll().size();
        realm.commitTransaction();
        realm.close();
        return size;
    }

    public void adapterNotifyDataSetChange(){
        notifyDataSetChanged();
    }

public class ViewHolder extends RecyclerView.ViewHolder {

    private TextView textNote;
    private WeatherTable note;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        textNote = itemView.findViewById(R.id.text_line);
    }

    public void bind(int idx){
        Realm realm = Realm.getDefaultInstance();
        WeatherTable wt = realm.where(WeatherTable.class).findAll().get(idx);
        this.note = new WeatherTable(wt.getId(), wt.getCity(), wt.getTemp());
        textNote.setText(this.note.getCity() + "           Температура: " + this.note.getTemp() + " °С");
    }

}

}
