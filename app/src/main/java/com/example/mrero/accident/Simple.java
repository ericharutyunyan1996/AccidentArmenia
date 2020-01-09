package com.example.mrero.accident;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;



public class Simple extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
     if(!FirebaseApp.getApps(this).isEmpty()) {
         FirebaseDatabase.getInstance().setPersistenceEnabled(true);
     }
        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}
