package kz.fire24.andreygolubkow.fire24apiclient.Fragments;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.osmdroid.views.MapView;

import java.util.Timer;
import java.util.TimerTask;

import kz.fire24.andreygolubkow.fire24apiclient.Activities.MainActivity;
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

public class FireFragment extends Fragment {


    private MainActivity _mainActivity;
    private View _view;
    private String _idCar;
    private String _serverAddress;
    private FiremanService _api;

    public FireFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fire, container, false);
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
        final TextView address = (TextView) _view.findViewById(R.id.fireAddress);
        final TextView comments = (TextView) _view.findViewById(R.id.fireComments);
        final TextView rank = (TextView) _view.findViewById(R.id.fireRank);
        final TextView receiver = (TextView) _view.findViewById(R.id.fireReceiver);
        final TextView startDate = (TextView) _view.findViewById(R.id.fireStartDate);

        _api.GetFire(_idCar).enqueue(new Callback<Fire>() {
            @Override
            public void onResponse(Call<Fire> call, Response<Fire> response) {
                if (response.code() == 404)
                {
                    address.setText("-");
                    comments.setText("-");
                    rank.setText("-");
                    receiver.setText("-");
                    startDate.setText("-");
                    return;
                }
                Fire fire = response.body();
                address.setText(fire.Address);
                comments.setText(fire.Comments);
                rank.setText(String.valueOf(fire.Rank));
                receiver.setText(fire.Receiver);
                startDate.setText(DateTime.parse(fire.StartDateTime).toString("HH:mm"));
            }

            @Override
            public void onFailure(Call<Fire> call, Throwable t) {

            }
        });
    }
}
