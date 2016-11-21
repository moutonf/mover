package com.mover.vehicle;

import android.app.Application;
import android.content.Context;

import org.acra.*;
import org.acra.annotation.*;

/**
 * Created by LUKE on 2016/08/01.
 */

@ReportsCrashes(mailTo = "lukekingsleybell@gmail.com",
                mode = ReportingInteractionMode.TOAST,
                resToastText = com.mover.vehicle.R.string.crash_toast_text)
public class Mover extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

    private static int user;
    private static boolean accident;

    public static int getUser() {
        return user;
    }

    public static void setUser(int u) {
        user = u;
    }

    public static boolean getAccident() {
        return accident;
    }

    public static void setAccident(boolean a) {
        accident = a;
    }

}
