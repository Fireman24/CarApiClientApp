package kz.fire24.andreygolubkow.fire24apiclient.Activities;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import kz.fire24.andreygolubkow.fire24apiclient.Adapters.ViewPagerAdapter;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.DepartureFragment;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.DocumentsFragment;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.FireFragment;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.NoSwipeViewPager;
import kz.fire24.andreygolubkow.fire24apiclient.Models.GeoobjectModel;
import kz.fire24.andreygolubkow.fire24apiclient.Models.GpsPoint;
import kz.fire24.andreygolubkow.fire24apiclient.R;
import kz.fire24.andreygolubkow.fire24apiclient.Services.FiremanService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.CAR_ID;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.RTMP_ADDRESS;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.SERVER_ADDRESS;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.SETTINGS_FILE;

public class MainActivity extends AppCompatActivity implements RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FireFragment _fireFragment;
    private DepartureFragment _departureFragment;
    private DocumentsFragment _docsFragment;

    private FiremanService _api;
    private String _idCar;
    private String _serverAddress;
    private String _rtmpUrl;
    private MainActivity _view = this;
    private SrsPublisher _mPublisher;

    private GpsPoint _navFire;
    private GeoPoint _navTo;
    private Marker _marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        LoadSettings();

        _fireFragment = new FireFragment();
        _departureFragment = new DepartureFragment();
        _docsFragment = new DocumentsFragment();

        NoSwipeViewPager mViewPager;
        mViewPager = (NoSwipeViewPager) findViewById(R.id.viewpager);
        mViewPager.setSwipeable(false);
        SetupTabs();
        SetupMap();
        StartUpdateGeoObjects();
        SetupBroadcast();
        StartBroadcast();
    }

    private void LoadSettings()
    {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Грузим настройки
        SharedPreferences settings = getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE);

        _idCar = settings.getString(CAR_ID, "");
        _serverAddress = settings.getString(SERVER_ADDRESS, "localhost");
        _rtmpUrl = settings.getString(RTMP_ADDRESS, "localhost");
        _api = getApi();
    }

    private void SetupTabs()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(_fireFragment, "Пожар");
        adapter.addFragment(_departureFragment, "Выезд");
        adapter.addFragment(_docsFragment, "Вложения");
        //adapter.addFragment(new OptionsFragment(), "Действия");
        viewPager.setAdapter(adapter);

    }

    private void SetupMap()
    {
        MapView map = (MapView) this.findViewById(R.id.mapView);
        map.setUseDataConnection(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        Context ctx = this.getApplicationContext();
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), map);
        Bitmap car = BitmapFactory.decodeResource(getResources(), R.drawable.firecar);
        mLocationOverlay.setPersonIcon(car);
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);
        map.getController().animateTo(mLocationOverlay.getMyLocation());
    }

    private void StartUpdateGeoObjects()
    {
        Timer myTimer = new Timer(); // Создаем таймер
        final Handler uiHandler = new Handler();

        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UpdateGeoObjects();
                    }
                });
            }
        }, 1000L, 5L * 1000); // интервал - 60000 миллисекунд, 0 миллисекунд до первого запуска.
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
                        _navFire = geo.gpsPoint;

                    } else if (Objects.equals(geo.marker, "departure")) {
                        marker.setIcon(getResources().getDrawable(R.drawable.departure));
                    } else {
                        continue;
                    }
                    marker.setTitle(geo.popupText);

                    marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            _navTo = marker.getPosition();

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

    public void SetupBroadcast()
    {
        _mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.glsurfaceview_camera));
        _mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        _mPublisher.setRtmpHandler(new RtmpHandler(this));
        _mPublisher.setRecordHandler(new SrsRecordHandler(this));
        _mPublisher.setPreviewResolution(640, 360);
        _mPublisher.setOutputResolution(360, 640);
        _mPublisher.setSendVideoOnly(true);
        _mPublisher.setVideoSmoothMode();
        _mPublisher.startCamera();
    }

    public void StartBroadcast()
    {
        _mPublisher.startPublish(_rtmpUrl);
        _mPublisher.startCamera();
        _mPublisher.switchCameraFace((_mPublisher.getCamraId()+1)% Camera.getNumberOfCameras());

    }

    @Override
    public void onRtmpConnecting(String msg) {

    }

    @Override
    public void onRtmpConnected(String msg) {

    }

    @Override
    public void onRtmpVideoStreaming() {

    }

    @Override
    public void onRtmpAudioStreaming() {

    }

    @Override
    public void onRtmpStopped() {

    }

    @Override
    public void onRtmpDisconnected() {

    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {

    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {

    }

    @Override
    public void onRtmpSocketException(SocketException e) {

    }

    @Override
    public void onRtmpIOException(IOException e) {

    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {

    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {

    }

    @Override
    public void onNetworkWeak() {

    }

    @Override
    public void onNetworkResume() {

    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {

    }

    @Override
    public void onRecordPause() {

    }

    @Override
    public void onRecordResume() {

    }

    @Override
    public void onRecordStarted(String msg) {

    }

    @Override
    public void onRecordFinished(String msg) {

    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {

    }

    @Override
    public void onRecordIOException(IOException e) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _mPublisher.stopPublish();
    }

    public void goToNavigatorClick(View view) {
        if (_navTo == null) {
            Toast.makeText(MainActivity.this, "Нет координат для навигации.", Toast.LENGTH_SHORT).show();
        } else {
            _marker.closeInfoWindow();
            Uri uri = Uri.parse("yandexnavi://show_point_on_map?lat=" + String.valueOf(_navTo.getLatitude()) + "&lon=" + String.valueOf(_navTo.getLongitude()) + "&zoom=12&no-balloon=0");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("ru.yandex.yandexnavi");

// Проверяет, установлено ли приложение.
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            boolean isIntentSafe = activities.size() > 0;
            if (isIntentSafe) {

//Запускает Яндекс.Навигатор.
                startActivity(intent);
            } else {

// Открывает страницу Яндекс.Навигатора в Google Play.
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=ru.yandex.yandexnavi"));
                startActivity(intent);
            }
        }
    }

    public void SwitchCamera(View view) {
        _mPublisher.switchCameraFace((_mPublisher.getCamraId()+1)% Camera.getNumberOfCameras());
    }
}
