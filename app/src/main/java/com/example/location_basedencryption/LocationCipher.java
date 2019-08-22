package com.example.location_basedencryption;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.style.TabStopSpan;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class LocationCipher {
    private final String ENCRYPTION = "AES/ECB/PKCS5Padding";
    private final String ENCRYIPTION_DIR = Environment.getExternalStorageDirectory() + File.separator + "encryption";
    private final String EXTENSION = ".enc";

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

        File outputFile = new File(ENCRYIPTION_DIR + File.separator + file.getName() + EXTENSION);
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

        File outputFile = new File( ENCRYIPTION_DIR + File.separator + file.getName().replace(".enc", ""));
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        fileOutputStream.write(outputBytes);
        fileOutputStream.close();
        fileInputStream.close();
        file.delete();
    }
}
