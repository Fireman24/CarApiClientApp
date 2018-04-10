package kz.fire24.andreygolubkow.fire24apiclient.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import kz.fire24.andreygolubkow.fire24apiclient.R;

import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.CAR_ID;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.FILESERVER_ADDRESS;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.RTMP_ADDRESS;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.SERVER_ADDRESS;
import static kz.fire24.andreygolubkow.fire24apiclient.AppConstants.SETTINGS_FILE;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WAKE_LOCK
                },
                1);

        Button saveButton = (Button) findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                SharedPreferences settings = getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE);
                EditText carId = (EditText) findViewById(R.id.idCarText);
                EditText server = (EditText) findViewById(R.id.serverAdressText);
                EditText fileserver = (EditText) findViewById(R.id.fileServerAddressText);
                EditText rtmp = (EditText) findViewById(R.id.rtmpAdressText);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(CAR_ID, carId.getText().toString());
                editor.putString(SERVER_ADDRESS, server.getText().toString());
                editor.putString(RTMP_ADDRESS, rtmp.getText().toString());
                editor.putString(FILESERVER_ADDRESS, fileserver.getText().toString());

                editor.apply();
            }
        });
        SharedPreferences settings = getSharedPreferences(SETTINGS_FILE, MODE_PRIVATE);
        if (settings.contains(CAR_ID))
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


}
