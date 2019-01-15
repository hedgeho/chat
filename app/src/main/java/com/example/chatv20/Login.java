package com.example.chatv20;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class Login extends AppCompatActivity implements View.OnClickListener {

    Button btnsignup;
    EditText etname;
    RadioGroup rgroup;
    RadioButton icon_1, icon_2, icon_3;
    ImageView img1, img2, img3;
    DBHelper dbh;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int permissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        Log.e("mylog", "permission check: " + permissionCheck);
        if (permissionCheck < 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 12345);
        }

        dbh = new DBHelper(this);
        database = dbh.getWritableDatabase();
        Cursor cursor = database.query("users", null, null, null, null, null, null);

        if(cursor.moveToFirst()) {
            int namei = cursor.getColumnIndex("name");
            int uuidi = cursor.getColumnIndex("uuid");
            int iconi = cursor.getColumnIndex("icon");
            if(!cursor.getString(namei).equals("")) {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("name", cursor.getString(namei))
                        .putExtra("icon", cursor.getInt(iconi))
                        .putExtra("uuid", cursor.getString(uuidi))
                        .putExtra("type", "normal"));
            }
        }
        cursor.close();

        btnsignup = findViewById(R.id.btnsignup);
        etname = findViewById(R.id.etname);
        etname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!etname.getText().toString().equals("")&&etname.getText().toString().length()<20) {
                    btnsignup.setEnabled(true);
                } else {
                    btnsignup.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        rgroup = findViewById(R.id.rgroup);
        icon_1 = findViewById(R.id.icon_1);
        icon_2 = findViewById(R.id.icon_2);
        icon_3 = findViewById(R.id.icon_3);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icon_1.setChecked(true);
                icon_2.setChecked(false);
                icon_3.setChecked(false);
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icon_1.setChecked(false);
                icon_2.setChecked(true);
                icon_3.setChecked(false);
            }
        });
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icon_1.setChecked(false);
                icon_2.setChecked(false);
                icon_3.setChecked(true);
            }
        });

        btnsignup.setOnClickListener(this);
        btnsignup.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        String name = etname.getText().toString();
        if(name.replaceAll(" ", "").equals(""))
            return;
        int icon = 0;
        switch (rgroup.getCheckedRadioButtonId()) {
            case R.id.icon_1:
                icon = 1;
                break;
            case R.id.icon_2:
                icon = 2;
                break;
            case R.id.icon_3:
                icon = 3;
                break;
        }
        String id = generateId();

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("icon", icon);
        cv.put("uuid", id);
        database.update("users", cv, "id = 1", null);

        Cursor c = database.query("users", null, null, null, null, null, null);
        if(c.moveToFirst()) {
            int namei = c.getColumnIndex("name");
            int uuidi = c.getColumnIndex("uuid");
            int iconi = c.getColumnIndex("icon");
            do {
                Log.e("mylog", "NAME: " + c.getString(namei));
                Log.e("mylog", "UUID: " + c.getString(uuidi));
                Log.e("mylog", "ICON: " + c.getInt(iconi));
            } while (c.moveToNext());
        } else {
            Log.e("mylog", "NO USERS");
            database.insert("users", null, cv);
        }
        c.close();

        startActivity(new Intent(this, MainActivity.class)
                .putExtra("name", name)
                .putExtra("icon", icon)
                .putExtra("uuid", id)
                .putExtra("type", "normal"));
    }
    String generateId() {
        return func() +  func() + '-' + func() + '-' + func() + '-' + func() + '-' + func() + func() + func();
    }

    String func() {
        char[] alphabet = new char[26];
        for (int i = 0; i < 26; i++) {
            alphabet[i] = (char) ('a' + i);
        }
        return ("" + (int)Math.floor((1 + Math.random()) * 0x10000)).substring(4)
                + alphabet[(int)Math.floor((1+Math.random()) * 0x10000)%26];
    }


}
class DBHelper extends SQLiteOpenHelper {
    public DBHelper (Context context) {
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создаем таблицу с полями
        db.execSQL("create table users ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "uuid text,"
                + "icon integer" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
