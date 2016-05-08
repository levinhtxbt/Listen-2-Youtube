package com.kapp.youtube;

import java.util.Date;

/**
 * Created by khang on 24/03/2016.
 * Email: khang.neon.1997@gmail.com
 */
public class Constants {
    public static final String API_KEY = "AIzaSyDOFNS2rPLr5IGdJQFYs-L1eLhAelU63Yc";
    //public static final String HOST_SERVER = "https://murmuring-brushlands-18762.herokuapp.com/";
    //public static final File FOLDER_MUSICS = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

    public static final String FIREBASE_SERVER = "https://analyze-usage.firebaseio.com/";


    public static final String SERVER1 = "http://murmuring-brushlands-18762.herokuapp.com",
            SERVER2 = "https://young-taiga-59434.herokuapp.com",
            SERVER3 = "https://polar-ridge-85715.herokuapp.com";

    private static final long DAY = 24 * 60 * 60 * 1000L,
            DAY_DIV_3 = DAY / 3;

    public static String getServer() {
        long time = new Date().getTime() % DAY;
        if (time < DAY_DIV_3) {
            Utils.increaseValue("useServer1Times");
            return SERVER1;
        } else if (time < DAY_DIV_3 * 2) {
            Utils.increaseValue("useServer2Times");
            return SERVER2;
        } else {
            Utils.increaseValue("useServer3Times");
            return SERVER3;
        }
    }
}
