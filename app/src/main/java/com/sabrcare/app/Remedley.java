package com.sabrcare.app;

import android.app.Application;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import io.realm.Realm;

public class Remedley extends Application {
    public static AWSCredentials credentials =  new AnonymousAWSCredentials();;
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

    }
}
