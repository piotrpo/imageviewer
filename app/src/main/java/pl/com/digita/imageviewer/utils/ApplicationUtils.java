package pl.com.digita.imageviewer.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Piotr on 08.02.14.
 * <p/>
 * Application utils and constants
 */
public class ApplicationUtils {
    public static final String ACTION_DOWNLOAD_HEARTBEAT = "pl.com.digita.imageviewer.ACTION_DOWNLOAD_HEARTBEAT";
    public static final String EXTRA_HAS_ERROR = "EXTRA_HAS_ERROR";
    public static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";
    public static final String EXTRA_DOWNLOADED = "EXTRA_DOWNLOADED";
    public static final String EXTRA_URL = "EXTRA_URL";
    public static final String ACTION_BITMAP_READY = "pl.com.digita.imageviewer.ACTION_BITMAP_READY";
    public static final String EXTRA_BITMAP = "EXTRA_BITMAP";
    public static final String TAG = "Image viewer";
    public static final String ACTION_DOWNLOAD_BITMAP = "ACTION_DOWNLOAD_BITMAP";
    public static final String WEB_SERVICE_URL = "http://mobileapitest.apiary.io/";

    public static String md5(final String pStringToHash) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(pStringToHash.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String hexDigit = Integer.toHexString(0xFF & aMessageDigest);
                while (hexDigit.length() < 2)
                    hexDigit = "0" + hexDigit;
                hexString.append(hexDigit);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
