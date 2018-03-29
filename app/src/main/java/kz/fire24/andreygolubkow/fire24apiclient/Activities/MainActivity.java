package kz.fire24.andreygolubkow.fire24apiclient.Activities;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import kz.fire24.andreygolubkow.fire24apiclient.Adapters.ViewPagerAdapter;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.DepartureFragment;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.FireFragment;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.MapFragment;
import kz.fire24.andreygolubkow.fire24apiclient.Fragments.OptionsFragment;
import kz.fire24.andreygolubkow.fire24apiclient.R;

import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.CAR_ID;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.SETTINGS_FILE;

public class MainActivity extends AppCompatActivity  {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MapFragment _mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoadSettings();

        setContentView(R.layout.activity_main);
        SetupTabs();

    }

    private void LoadSettings()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Грузим настройки
        SharedPreferences settings = getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE);

        if (!settings.contains(CAR_ID) ) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return;
        }
    }

    private void SetupTabs()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        _mapFragment = new MapFragment();

        adapter.addFragment(_mapFragment, "Карта");
        adapter.addFragment(new FireFragment(), "Пожар");
        adapter.addFragment(new DepartureFragment(), "Выезд");
        //adapter.addFragment(new OptionsFragment(), "Действия");
        viewPager.setAdapter(adapter);

    }
}
