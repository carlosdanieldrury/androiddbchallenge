package db.drury.com.dbchallenge;

import android.util.Base64;

import java.util.Arrays;

/**
 * Created by carlos.drury on 23/08/2017.
 */

public class OTPHandler {

    // Link to native Lib wrote in C
    static {
        System.loadLibrary("otpjni");
    }

    // a interface to the native code
    public static native byte[] generateOtp(String key);

    // get the byte[] from generateOtp method and returns a String with the result of 6 digits
    public static String getTokenOTP6Digits(byte[] dbc) {
        String res = "";
        if (dbc != null) {
            byte[] dbc1 = Arrays.copyOfRange(dbc, 10, 14);
            String aux = toHexadecimal(dbc1);
            long decimalValue = Long.parseLong(aux, 16);
            String decimalValueString = Long.toString(decimalValue);
            int decimalValueStringSize = decimalValueString.length();
            res = decimalValueString.substring(decimalValueStringSize-6, decimalValueStringSize);
        }

        return res;
    }


    // converts the byte[] to hex string
    private static String toHexadecimal(byte[] digest){
        String hash = "";
        for(byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }

    public static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String decrypt(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }

}
