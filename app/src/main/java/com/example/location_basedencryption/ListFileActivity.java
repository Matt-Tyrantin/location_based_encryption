package com.example.location_basedencryption;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;

public class ListFileActivity extends ListActivity {
    private static LocationManager mLocationManager;
    private static CipherLocationListener mLocationListener;

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        initializeLocationServices();
        initializeDirectoryServices();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);

        if (path.endsWith(File.separator)) {
            filename = path + filename;

        } else {
            filename = path + File.separator + filename;
        }

        if (new File(filename).isDirectory()) {
            Intent intent = new Intent(this, ListFileActivity.class);
            intent.putExtra("path", filename);
            startActivity(intent);

        } else {
            if (filename.endsWith(".enc")) {
                try {
                    LocationCipher locationCipher = new LocationCipher(mLocationListener);
                    locationCipher.decryptFile(new File(filename));

                    Toast.makeText(this, "File has been decrypted.", Toast.LENGTH_LONG).show();

                } catch (NullLocationException e) {
                    Toast.makeText(this, "Cannot fetch location.", Toast.LENGTH_LONG).show();

                } catch (BadPaddingException e) {
                    Toast.makeText(this, "Invalid location.", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(this, "Cannot decrypt due to unknown error.", Toast.LENGTH_LONG).show();
                }


            } else {
                try {
                    LocationCipher locationCipher = new LocationCipher(mLocationListener);
                    locationCipher.encryptFile(new File(filename));

                    Toast.makeText(this, "File has been encrypted.", Toast.LENGTH_LONG).show();

                } catch (NullLocationException e) {
                    Toast.makeText(this, "Cannot fetch location.", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(this, "Cannot encrypt due to unknown error.", Toast.LENGTH_LONG).show();
                }
            }

            setupAdapter();
        }
    }

    protected void setupAdapter()
    {
        File dir = new File(path);
        List<String> values = Arrays.asList(dir.list());

        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                values
        );

        setListAdapter(adapter);

        Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
    }

    protected void initializeDirectoryServices()
    {
        final int MY_PERMISSION_ACCESS_STORAGE = 2;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    MY_PERMISSION_ACCESS_STORAGE
            );

        } else {
            path = Environment.getExternalStorageDirectory().getPath();

            if (getIntent().hasExtra("path")) {
                path = getIntent().getStringExtra("path");
            }

            setTitle(path);
            setupAdapter();
        }
    }

    protected void initializeLocationServices()
    {
        final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_PERMISSION_ACCESS_FINE_LOCATION
            );

        } else {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            }

            if (mLocationListener == null) {
                mLocationListener = new CipherLocationListener();
            }

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }
    }
}