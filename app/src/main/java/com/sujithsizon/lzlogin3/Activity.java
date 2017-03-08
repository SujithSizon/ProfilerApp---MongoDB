package com.sujithsizon.lzlogin3;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.Callable;

/**
 * Created by sujith on 16/2/17.
 */

public class Activity extends AppCompatActivity{

    private static Activity ourInstance = new Activity();

    public static Activity getInstance() {
        return ourInstance;
    }

    private Callable<Void> mLogoutCallable;

    private Activity() {
    }

    public void setLogoutCallable(Callable<Void> callable) {
        mLogoutCallable = callable;
    }
}
