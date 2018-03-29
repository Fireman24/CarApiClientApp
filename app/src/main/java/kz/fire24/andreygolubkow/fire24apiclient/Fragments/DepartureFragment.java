package kz.fire24.andreygolubkow.fire24apiclient.Fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.util.Timer;
import java.util.TimerTask;

import kz.fire24.andreygolubkow.fire24apiclient.Activities.MainActivity;
import kz.fire24.andreygolubkow.fire24apiclient.Models.Departure;
import kz.fire24.andreygolubkow.fire24apiclient.Models.Fire;
import kz.fire24.andreygolubkow.fire24apiclient.R;
import kz.fire24.andreygolubkow.fire24apiclient.Services.FiremanService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.CAR_ID;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.SERVER_ADDRESS;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.SETTINGS_FILE;


/**
 * A simple {@link Fragment} subclass.
 */
public class DepartureFragment extends Fragment {
    private MainActivity _mainActivity;
    private View _view;
    private String _idCar;
    private String _serverAddress;
    private FiremanService _api;

    public DepartureFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_departure, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        _mainActivity = ((MainActivity) getActivity());
        _view = getView();
        SharedPreferences settings = _mainActivity.getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE);
        _idCar = settings.getString(CAR_ID, "");
        _serverAddress = settings.getString(SERVER_ADDRESS, "localhost");

        _api = getApi();

        Timer myTimer = new Timer(); // Создаем таймер
        final Handler uiHandler = new Handler();

        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UpdateData();
                    }
                });
            }
        }, 1000L, 5L * 1000); // интервал - 60000 миллисекунд, 0 миллисекунд до первого запуска.
    }

    public FiremanService getApi() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(_serverAddress)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        FiremanService firemanService = retrofit.create(FiremanService.class);
        return firemanService;
    }

    public void UpdateData()
    {
        final TextView address = (TextView) _view.findViewById(R.id.departureAddress);
        final TextView comments = (TextView) _view.findViewById(R.id.departureComments);
        final TextView intent = (TextView) _view.findViewById(R.id.departureIntent);
        final TextView receiver = (TextView) _view.findViewById(R.id.departureReceiver);
        final TextView startDate = (TextView) _view.findViewById(R.id.departureStartDate);

        _api.GetDeparture(_idCar).enqueue(new Callback<Departure>() {
            @Override
            public void onResponse(Call<Departure> call, Response<Departure> response) {
                if (response.code() == 404)
                {
                    address.setText("-");
                    comments.setText("-");
                    intent.setText("-");
                    receiver.setText("-");
                    startDate.setText("-");
                    return;
                }
                Departure dep = response.body();
                address.setText(dep.Address);
                comments.setText(dep.Comments);
                intent.setText(dep.Intent);
                receiver.setText(dep.Receiver);
                startDate.setText(DateTime.parse(dep.DateTime).toString("HH:mm"));
            }

            @Override
            public void onFailure(Call<Departure> call, Throwable t) {

            }
        });
    }

}
