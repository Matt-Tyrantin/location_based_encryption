package com.example.location_basedencryption;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;

public class ListFileActivity extends ListActivity implements TextWatcher {
    private static LocationManager mLocationManager;
    private static CipherLocationListener mLocationListener;

    private boolean searching = false;

    private String path;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        this.etSearch = findViewById(R.id.etSearch);
        this.etSearch.addTextChangedListener(this);

        initializeLocationServices();
        initializeDirectoryServices();
    }

    protected void showEncryptDialog(final File file)
    {
        String coordinates = "Cannot fetch";

        try {
            coordinates = mLocationListener.getLocationString(
                    mLocationListener.getCurrentLocation()
            );

        } catch (NullLocationException e) {
            e.printStackTrace();
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("File name: ").append(file.getName()).append('\n')
                .append("Current coordinates: ").append(coordinates).append('\n')
                .append('\n').append("Encrypt file?");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringBuilder.toString())
                .setTitle(R.string.encrypt_title);

        builder.setPositiveButton(R.string.encrypt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    LocationCipher locationCipher = new LocationCipher(mLocationListener);
                    locationCipher.encryptFile(file);

                    updateAdapter();
                    showToast("File has been encrypted");

                } catch (NullLocationException e) {
                    showToast("Cannot fetch location.");

                } catch (BadPaddingException e) {
                    showToast("Invalid location.");

                } catch (Exception e) {
                    showToast("Cannot decrypt due to unknown error.");

                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Just exit the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void showDecryptDialog(final File file)
    {
        String coordinates = "Cannot fetch";

        try {
            coordinates = mLocationListener.getLocationString(
                    mLocationListener.getCurrentLocation()
            );

        } catch (NullLocationException e) {
            e.printStackTrace();
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("File name: ").append(file.getName()).append('\n')
                .append("Current coordinates: ").append(coordinates).append('\n')
                .append('\n').append("Decrypt file?");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringBuilder.toString())
                .setTitle(R.string.decrypt_title);

        builder.setPositiveButton(R.string.decrypt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    LocationCipher locationCipher = new LocationCipher(mLocationListener);
                    locationCipher.decryptFile(file);

                    updateAdapter();
                    showToast("File has been decrypted");

                } catch (NullLocationException e) {
                    showToast("Cannot fetch location.");

                } catch (BadPaddingException e) {
                    showToast("Invalid location.");

                } catch (Exception e) {
                    showToast("Cannot decrypt due to unknown error.");

                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Just exit the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);

        if (path.endsWith(File.separator)) {
            filename = path + filename;

        } else {
            filename = path + File.separator + filename;
        }

        final File file = new File(filename);
        if (file.isDirectory()) {
            Intent intent = new Intent(this, ListFileActivity.class);
            intent.putExtra("path", filename);
            startActivity(intent);

        } else {
            if (file.getName().endsWith(LocationCipher.EXTENSION)) {
                showDecryptDialog(file);

            } else {
                showEncryptDialog(file);
            }
        }
    }

    public void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void updateAdapter()
    {
        if (!searching) {
            setupAdapter(getDirectoryFiles(path));

        } else {
            setupAdapter(searchDirectoryFiles(path, etSearch.getText().toString()));
        }
    }

    protected void setupAdapter(List<String> values)
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                values
        );
        /*FileListAdapter adapter = new FileListAdapter(
                this,
                android.R.layout.simple_list_item_2,
                values
        );*/

        setListAdapter(adapter);

        Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
    }

    protected List<String> getDirectoryFiles(String dirPath)
    {
        File dir = new File(dirPath);
        List<String> values = Arrays.asList(dir.list());

        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }

        return values;
    }

    protected List<String> searchDirectoryFiles(String dirPath, String search)
    {
        if (!dirPath.endsWith(File.separator)) {
            dirPath += File.separator;
        }

        List<String> values = new ArrayList<>();
        List<String> files = getDirectoryFiles(dirPath);

        for (String file : files) {
            if (file.toLowerCase().contains(search.toLowerCase())) {
                values.add(file);
            }

            File dir = new File(dirPath + File.separator + file);

            if (dir.isDirectory() && !dir.getName().startsWith(".")) {
                List<String> addValues = searchDirectoryFiles(dir.getPath(), search);
                for (int i = 0; i < addValues.size(); i++) {
                    addValues.set(i, dir.getName() + File.separator + addValues.get(i));
                }
                values.addAll(addValues);
            }
        }

        return values;
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
            setupAdapter(getDirectoryFiles(path));
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if (s.toString().equals("")) {
            setupAdapter(getDirectoryFiles(path));
            searching = false;

        } else {
            setupAdapter(searchDirectoryFiles(path, s.toString()));
            searching = true;
        }
    }
}