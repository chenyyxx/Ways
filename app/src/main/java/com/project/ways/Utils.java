
package com.project.ways;

import android.graphics.Bitmap;
import android.location.Location;
import android.graphics.Matrix;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Util class stores util static method
 */
public class Utils {
    /**
     * Md5 encryption, encode string
     *
     * @param input the string to be encoded
     * @return encoded string
     */
    public static String md5Encryption(final String input) {
        String result = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(input.getBytes(Charset.forName("UTF8")));
            byte[] resultByte = messageDigest.digest();
            result = new String(Hex.encodeHex(resultByte));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Get distance between two locations
     *
     * @param currentLatitude  current latitude
     * @param currentLongitude current longitude
     * @param destLatitude     destination latitude
     * @param destLongitude    destination longitude
     * @return the distance between two locations by miles
     */
    public static int distanceBetweenTwoLocations(double currentLatitude,
                                                  double currentLongitude,
                                                  double destLatitude,
                                                  double destLongitude) {
        Location currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);
        Location destLocation = new Location("DestLocation");
        destLocation.setLatitude(destLatitude);
        destLocation.setLongitude(destLongitude);
        double distance = currentLocation.distanceTo(destLocation);

        double inches = (39.370078 * distance);
        int miles = (int) (inches / 63360);
        return miles;
    }

    /**
     * Resize bitmap to corresponding height and width
     *
     * @param bm        input bitmap
     * @param newWidth  new width
     * @param newHeight new height
     * @return refactored image
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth / width);
        float scaleHeight = ((float) newHeight / height);

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // Resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;

    }
}
