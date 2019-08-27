package com.example.location_basedencryption;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class LocationCipher {
    private final String ENCRYPTION = "AES/ECB/PKCS5Padding";
    private final String ENCRYPTION_DIR = Environment.getExternalStorageDirectory() + File.separator + "encryption";

    public static final String EXTENSION = ".enc";

    private CipherLocationListener mLocationListener;

    public LocationCipher(CipherLocationListener locationListener)
    {
        this.mLocationListener = locationListener;
    }

    private SecretKey generateKey() throws Exception
    {
        String coordinates = mLocationListener.getLocationString(
                mLocationListener.getCurrentLocation()
        );
        Log.v("Coordinates", coordinates);

        byte[] key = coordinates.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);

        return new SecretKeySpec(key, "AES");
    }

    public void encryptFile(File file) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey());

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] inputBytes = new byte[(int) file.length()];
        fileInputStream.read(inputBytes);

        byte[] outputBytes = cipher.doFinal(inputBytes);

        File dir = new File(ENCRYPTION_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //File outputFile = new File(ENCRYPTION_DIR + File.separator + file.getName() + EXTENSION);
        File outputFile = new File(dir, file.getName() + LocationCipher.EXTENSION);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        fileOutputStream.write(outputBytes);
        fileInputStream.close();
        fileOutputStream.close();
    }

    public void decryptFile(File file) throws Exception
    {
        Cipher cipher = Cipher.getInstance(ENCRYPTION);

        try {
            cipher.init(Cipher.DECRYPT_MODE, generateKey());

        } catch(Exception e) {
            throw e;
        }


        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] inputBytes = new byte[(int) file.length()];

        fileInputStream.read(inputBytes);

        byte[] outputBytes = cipher.doFinal(inputBytes);

        File outputFile = new File( ENCRYPTION_DIR + File.separator + file
                .getName()
                .replace(LocationCipher.EXTENSION, ""));

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        fileOutputStream.write(outputBytes);
        fileOutputStream.close();
        fileInputStream.close();
        file.delete();
    }
}
