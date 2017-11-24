package com.marcosevaristo.tcc001;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.marcosevaristo.tcc001.activities.Mapa;
import com.marcosevaristo.tcc001.database.SQLiteHelper;
import com.marcosevaristo.tcc001.model.Municipio;

public class App extends Application {
    private static Context context;
    private static SQLiteHelper sqLiteHelper;
    private static Municipio municipio;
    private static boolean possuiPermissoes;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sqLiteHelper = SQLiteHelper.getInstance(context);
    }

    public static Context getAppContext() {
        return App.context;
    }

    public static SQLiteHelper getSqLiteHelper() {
        return sqLiteHelper;
    }

    public static Municipio getMunicipio() {
        return municipio;
    }
    public static void setMunicipio(Municipio municipio) {
        App.municipio = municipio;
    }

    public static void toast(int stringID, String... params) {
        Toast.makeText(context, context.getString(stringID, params), Toast.LENGTH_SHORT).show();
    }

    public static boolean isPossuiPermissoes() {
        return possuiPermissoes;
    }
}
