package kz.fire24.andreygolubkow.fire24apiclient.Fragments;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import kz.fire24.andreygolubkow.fire24apiclient.Activities.MainActivity;
import kz.fire24.andreygolubkow.fire24apiclient.Models.GeoobjectModel;
import kz.fire24.andreygolubkow.fire24apiclient.Models.GpsPoint;
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

public class MapFragment extends Fragment {

    private MainActivity _mainActivity;
    private View _view;

    MapView _map;
    private String _idCar;
    private String _serverAddress;
    private LocationListener listener;
    private LocationManager _manager;
    private FiremanService _api;
    private double _lat = 0;
    private double _lon = 0;
    private double _navFireLat = 0;
    private double _navFireLon = 0;
    private double _navToLat = 0;
    private double _navToLon = 0;

    public MapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_map, container, false);

        _map = (MapView) _view.findViewById(R.id.mapView);
        return _view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        _mainActivity = ((MainActivity) getActivity());
        _view = getView();
        SharedPreferences settings = _mainActivity.getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE);
        _idCar = settings.getString(CAR_ID, "");
        _serverAddress = settings.getString(SERVER_ADDRESS, "localhost");
        _map = (MapView) _view.findViewById(R.id.mapView);
        _api = getApi();
        StartMap();
    }

    public void StartMap() {
        _manager = (LocationManager) _mainActivity.getSystemService(Context.LOCATION_SERVICE);
        Timer myTimer = new Timer(); // Создаем таймер
        final Handler uiHandler = new Handler();

        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getContext() == null )
                        {
                            return;
                        }
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        _manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);

                       UpdateGeoObjects();
                    }
                });
            }
        }, 1000L, 5L * 1000); // интервал - 60000 миллисекунд, 0 миллисекунд до первого запуска.

        Configuration.getInstance().setTileFileSystemCacheMaxBytes(1073741824);
        _map.setTileSource(TileSourceFactory.MAPNIK);

        _map.setBuiltInZoomControls(true);

        _map.setMultiTouchControls(true);

        Context ctx = _mainActivity.getApplicationContext();
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), _map);
        Bitmap car = BitmapFactory.decodeResource(getResources(), R.drawable.firecar);
        mLocationOverlay.setPersonIcon(car);
        mLocationOverlay.enableMyLocation();
        _map.getOverlays().add(mLocationOverlay);

        listener = CreateLocationListener();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Проверка наличия разрешений
            // Если нет разрешения на использование соответсвующих разркешений выполняем какие-то действия
            return;
        }
        _manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }

    private LocationListener CreateLocationListener()
    {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    _lat = location.getLatitude();
                    _lon = location.getLongitude();
                    GpsPoint gps = new GpsPoint();
                    gps.Lat = _lat;
                    gps.Lon = _lon;
                    _api.PostLocation(_idCar,gps).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                        }
                    });
                } else {
                    //Toast.makeText(MainActivity.this, "Ошибка при получении gps координат.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
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

    private void UpdateGeoObjects()
    {
        //Получение гео объектов
        _api.GetGeoObjects(_idCar).enqueue(new Callback<List<GeoobjectModel>>() {
            @Override
            public void onResponse(Call<List<GeoobjectModel>> call, Response<List<GeoobjectModel>> response) {
                MapView map = (MapView) _view.findViewById(R.id.mapView);

                List<GeoobjectModel> list = response.body();

                ArrayList<Overlay> markers = new ArrayList<Overlay>();

                for (GeoobjectModel geo : list) {

                    Marker marker = new Marker(map);
                    marker.setPosition(new GeoPoint(geo.gpsPoint.Lat, geo.gpsPoint.Lon));
                    //marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    //TODO:Показать гидранты
                    //Switch showHydrants = (Switch) findViewById(R.id.showHydarants);
                    boolean showHydrants = true;
                    if (Objects.equals(geo.marker, "hydrant") && showHydrants) {
                        marker.setIcon(getResources().getDrawable(R.drawable.hydrant));
                    } else if (Objects.equals(geo.marker, "fire")) {
                        marker.setIcon(getResources().getDrawable(R.drawable.fire));
                        _navFireLat = geo.gpsPoint.Lat;
                        _navFireLon = geo.gpsPoint.Lon;

                    } else {
                        continue;
                    }
                    marker.setTitle(geo.popupText);

                    marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            _navToLat = marker.getPosition().getLatitude();
                            _navToLon = marker.getPosition().getLongitude();

                            marker.showInfoWindow();
                            return true;
                        }
                    });

                    markers.add(marker);
                }

                List<Overlay> overlays = map.getOverlays();

                for (Overlay o : overlays) {
                    if (o.getClass() == Marker.class) {
                        overlays.remove(o);
                    }
                }

                map.getOverlays().addAll(markers);

            }

            @Override
            public void onFailure(Call<List<GeoobjectModel>> call, Throwable t) {
                //Toast.makeText(MainActivity.this, "Ошибка сети.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
