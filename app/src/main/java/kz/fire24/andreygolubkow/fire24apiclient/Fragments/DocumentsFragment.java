package kz.fire24.andreygolubkow.fire24apiclient.Fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kz.fire24.andreygolubkow.fire24apiclient.Activities.MainActivity;
import kz.fire24.andreygolubkow.fire24apiclient.Adapters.ImageAdapter;
import kz.fire24.andreygolubkow.fire24apiclient.Models.FireImage;
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
public class DocumentsFragment extends Fragment {

    private MainActivity _mainActivity;
    private View _view;
    private String _idCar;
    private String _serverAddress;
    private FiremanService _api;

    public DocumentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_documents, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        _mainActivity = ((MainActivity) getActivity());
        _view = getView();
        SharedPreferences settings = _mainActivity.getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE);
        _idCar = settings.getString(CAR_ID, "");
        _serverAddress = settings.getString(SERVER_ADDRESS, "localhost");

        _api = getDocsApi();

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
        }, 0, 5L * 1000); // интервал - 60000 миллисекунд, 0 миллисекунд до первого запуска.



    }

    private void UpdateData()
    {
        _api.GetImages(_idCar).enqueue(new Callback<List<FireImage>>() {
            @Override
            public void onResponse(Call<List<FireImage>> call, Response<List<FireImage>> response) {

                if (response.body() == null)
                {
                    return;
                }

                ListView imagesList = (ListView) _view.findViewById(R.id.imagesList);
                // создаем адаптер
                ImageAdapter imagesAdapter = new ImageAdapter(_view.getContext(), R.layout.list_item, response.body());

                imagesList.setAdapter(imagesAdapter);

                AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                        // получаем выбранный пункт
                        FireImage selectedState = (FireImage) parent.getItemAtPosition(position);
                        //-1 т. к. последним симповолом у сервера стоит слеш
                        String sAddr = _serverAddress.substring(0, _serverAddress.length() - 1);
                        Uri address = Uri.parse(sAddr + selectedState.Url + "/get");
                        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlinkIntent);

                    }
                };
                imagesList.setOnItemClickListener(itemListener);
            }

            @Override
            public void onFailure(Call<List<FireImage>> call, Throwable t) {

            }
        });
    }

    public FiremanService getDocsApi() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(_serverAddress)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        FiremanService api = retrofit.create(FiremanService.class);
        return api;
    }


}
