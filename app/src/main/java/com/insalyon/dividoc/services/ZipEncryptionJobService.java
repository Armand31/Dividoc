package com.insalyon.dividoc.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.insalyon.dividoc.util.AppContext;
import com.insalyon.dividoc.util.FilesPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ZipEncryptionJobService extends JobService {

    /**
     * Function executed after the job is started. Here, we want to encrypt the zip file using AES
     * The AES key used to encrypt the zip file will be encrypted using RSA
     * @param jobParameters the job parameters
     * @return a boolean that is true if we want the job to continue running after function execution, which is not our case of use
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        // Creating a folder to put the encrypted zip, the AES key and initialization vector into it
        String oldZipPathWithoutExtension = jobParameters.getExtras().getString("zipPathWithoutExtension");
        String basename = oldZipPathWithoutExtension.substring(oldZipPathWithoutExtension.lastIndexOf(File.separator) + 1);
        String encryptionFolder = FilesPath.getExportDirectory() + File.separator + basename;
        FilesPath.createDirectory(encryptionFolder,"Folder for encryption could not be created");
        String zipPathWithoutExtension = encryptionFolder + File.separator + basename;

        try {
            SecretKey AESKey = generateAESKey();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, AESKey);

            encryptZip(oldZipPathWithoutExtension, zipPathWithoutExtension, cipher, AESKey);
            assert AESKey != null;
            encryptKeyAndIV(zipPathWithoutExtension, AESKey, cipher.getIV());

            // Delete the zip file
            FilesPath.deleteDirectory(jobParameters.getExtras().getString("zipPathWithoutExtension") + ".zip");

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    /**
     * Encrypt the zip file using AES algorithm
     */
    private static void encryptZip(String oldZipPathWithoutExtension, String zipPathWithoutExtension, Cipher cipher, SecretKey secretKey) throws GeneralSecurityException, IOException {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert secretKey != null;
            Log.d("Encryption", "AES key : " + bytesToHex(secretKey.getEncoded()));//Integer.toHexString(Integer.parseInt(Base64.getEncoder().encodeToString(secretKey.getEncoded()))));
            Log.d("Encryption", "AES IV : " + bytesToHex(cipher.getIV()));//Integer.toHexString(Integer.parseInt(Base64.getEncoder().encodeToString(cipher.getIV()))));
        }

        // Input file
        File input = new File(oldZipPathWithoutExtension + ".zip");
        FileInputStream fileInputStream = new FileInputStream(input);
        //BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        // Output file
        File output = new File(zipPathWithoutExtension + ".enc");
        FileOutputStream fileOutputStream = new FileOutputStream(output);
        //BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);

        // Writes the ciphertext in the output
        int b;
        byte[] d = new byte[8];
        while ((b = fileInputStream.read(d)) != -1) {
            cipherOutputStream.write(d, 0, b);
        }

        // Flushes and closes streams
        cipherOutputStream.flush();
        cipherOutputStream.close();
        fileInputStream.close();
    }

    /**
     * Encrypt the AES key and initialization vector using RSA public key
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void encryptKeyAndIV(String zipPathWithoutExtension, SecretKey AESKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());

        FileOutputStream keyOutputStream = new FileOutputStream(zipPathWithoutExtension + ".key");
        FileOutputStream IVOutputStream = new FileOutputStream(zipPathWithoutExtension + "_IV.txt");

        // Writing the key
        byte[] b1 = cipher.doFinal(bytesToHex(AESKey.getEncoded()).getBytes());
        keyOutputStream.write(b1);

        // Writing the initialization vector
        byte[] b2 = cipher.doFinal(bytesToHex(iv).getBytes());
        IVOutputStream.write(b2);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static PublicKey getPublicKey() {

        PublicKey publicKey = null;

        try {

            // Decodes the string and creates the PublicKey object
            String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr6/javy4M90GHxTzUEUeYqGRZx8+4GTaHIgVtEd6vp3I9dtuSy4ylMiyZch22ygs0/UErZJyXCj7DMH89TemUUAtmEcvZzJj3YJThEanlipwVMXAFnI14EignFxqUeRIBm+/RmAwpOkTsFGi3qCtBSN0mXz0cVYrD+/RbKBD+TVKHzVRmZPksWqK1wzZ9DMArccC9967lf8PG0v06Et65Mzdo0BA89XgUVw9DlyeSBUyc/F3Uy+XEZdnfM3Ws3DrPUMMHsSHDnUYZKIpCQjvrG3SvKWS4HoxRJ4FTBzOrOEs92b/PUius4NIDCTPc0FG4ka7Pq5EH5GMEtmZMHWoFwIDAQAB";

            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(PUBLIC_KEY.getBytes()));
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // Generates the RSA publicKey
            publicKey = keyFactory.generatePublic(keySpec);

        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // Returns a null publicKey if it can't create the key
        return publicKey;
    }

    private static SecretKey generateAESKey() {

        // Initialisation of the keyGenerator to generate a 128bit key
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return (keyGenerator.generateKey());
        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(AppContext.getAppContext(), "AES key could not be generated", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
