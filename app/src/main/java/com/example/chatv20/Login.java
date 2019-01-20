package com.example.chatv20;

import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static com.example.chatv20.MainActivity.log;
import static com.example.chatv20.MainActivity.tableExists;

public class Login extends AppCompatActivity implements View.OnClickListener {

    Button btnsignup;
    FloatingActionButton fab, fab_fingerprint;
    EditText etlogin, etpword;
    DBHelper dbh;
    SQLiteDatabase database;
    Context context;
    private KeyguardManager keyguardManager;
    String KEY_NAME = "my fucking key";

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


        //showAuthenticationScreen();
        context = this;
        dbh = new DBHelper(this);
        database = dbh.getWritableDatabase();
        int i = 0;
        //database.execSQL("drop table token;");
        if(tableExists(database, "token"))
            i = 1;
        database.execSQL("create table if not exists token ("
                + "id integer primary key autoincrement,"
                + "token text,"
                + "uuid text" + ");");
        if(i==0) {
            ContentValues cv = new ContentValues();
            cv.put("token", "z");
            cv.put("uuid", "z");
            database.insert("token", null, cv);
        }
        /*database.execSQL("drop table if exists users;");
        database.execSQL("create table if not exists users ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "login text,"
                + "password text,"
                + "uuid text,"
                + "icon integer,"
                + "token text" + ");");*/

        /*Cursor cursor = database.query("users", null, null, null, null, null, null);

        if(cursor.moveToFirst()) {
            int namei = cursor.getColumnIndex("name");
            int uuidi = cursor.getColumnIndex("uuid");
            int iconi = cursor.getColumnIndex("icon");
            if(!cursor.getString(namei).equals("")) {
                startActivity(new Intent(this, ChatsActivity.class)
                        .putExtra("name", cursor.getString(namei))
                        .putExtra("icon", cursor.getInt(iconi))
                        .putExtra("uuid", cursor.getString(uuidi))
                        .putExtra("type", "normal"));
            }
        }
        cursor.close();*/

        btnsignup = findViewById(R.id.btnsignup);
        fab = findViewById(R.id.fabnext);
        fab_fingerprint = findViewById(R.id.login_fab_finger);
        etlogin = findViewById(R.id.login_login);
        etpword = findViewById(R.id.login_password);
        etlogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!etlogin.getText().toString().equals("")&&etlogin.getText().toString().length()<20) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etpword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!etpword.getText().toString().equals("")&&etpword.getText().toString().length()<20) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fab.setOnClickListener(this);
        btnsignup.setOnClickListener(this);
        fab_fingerprint.setOnClickListener(this);
        fab.setEnabled(false);

        // TODO vk auth
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabnext:
                final String login = etlogin.getText().toString(),
                        pword = etpword.getText().toString();
                if(login.replaceAll(" ", "").equals(""))
                    return;

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            log(3, "auth");
                            URL url = new URL("https://warm-bayou-37022.herokuapp.com/login?type=lp&login=" + login
                                    + "&password=" + pword + "&save=true");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");

                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder builder = new StringBuilder();
                            for (int c; (c = reader.read()) >= 0; )
                                builder.append((char) c);

                            log(3, "builder: " + builder.toString());

                            String[] spl = builder.toString().split("\"");
                            String auth_token, token, id, name, icon;
                            if(spl.length > 7) {
                                id = spl[1];
                                auth_token = spl[3];
                                name = spl[5];
                                icon = spl[7];
                                token = spl[9];
                            } else
                                return;

                            ContentValues cv = new ContentValues();
                            cv.put("token", auth_token);
                            cv.put("uuid", id);
                            database.update("token", cv, "id = 1", null);

                            startActivity(new Intent(context, ChatsActivity.class)
                                    .putExtra("name", name)
                                    .putExtra("icon", (icon.equals("None")?1:Integer.parseInt(icon)))
                                    .putExtra("uuid", id)
                                    .putExtra("token", token)
                                    .putExtra("type", "normal"));
                        } catch (Exception e) {
                            log(2, e.toString());
                        }
                    }
                }.start();
                break;
            case R.id.btnsignup:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
            case R.id.login_fab_finger:
                keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                if(!keyguardManager.isKeyguardSecure())
                    return;

                log(1, "create key");
                createKey();
                log(1, "try encrypt");
                tryEncrypt();
        }
    }

    void authByCode() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String auth_token, id;
                    Cursor cursor = database.query("token", null, null, null, null,
                            null, null);
                    if(cursor.moveToFirst()) {
                        auth_token = cursor.getString(cursor.getColumnIndex("token"));
                        id = cursor.getString(cursor.getColumnIndex("uuid"));
                    } else {
                        return;
                    }
                    cursor.close();

                    URL url = new URL("https://warm-bayou-37022.herokuapp.com/check?cookie=" + auth_token + "&id=" + id);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                    connection.setRequestProperty("Accept","*/*");
                    connection.connect();

                    log(1, "response code " + connection.getResponseCode());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    for (int c; (c = reader.read()) >= 0; )
                        builder.append((char) c);
                    log(1, builder.toString());

                    String[] spl = builder.toString().split("\"");
                    String name, icon, token;
                    if(spl.length > 7) {
                        name = spl[3];
                        icon = spl[5];
                        token = spl[7];
                    } else
                        return;
                    log(2, "auth success");

                    startActivity(new Intent(context, ChatsActivity.class)
                            .putExtra("name", name)
                            .putExtra("icon", (icon.equals("None") ? 1 : Integer.parseInt(icon)))
                            .putExtra("uuid", id)
                            .putExtra("token", token)
                            .putExtra("type", "normal"));
                } catch (Exception e) {
                    log(2, "auth: " + e.toString());
                }
            }
        }.start();
    }

    private void showAuthenticationScreen() {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if(resultCode == RESULT_OK) {
            if(tryEncrypt()) {
                authByCode();
            }
        } else {
            fab_fingerprint.setImageDrawable(getDrawable(R.drawable.fingerprint_red));
        }
    }

    boolean tryEncrypt() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // Try encrypting something, it will only work if the user authenticated within
            // the last AUTHENTICATION_DURATION_SECONDS seconds.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            cipher.doFinal("something".getBytes());

            // reached only if user authenticated within 30 s
            authByCode();
            return true;
        } catch (UserNotAuthenticatedException e) {
            showAuthenticationScreen();
            return false;
        } catch (Exception e) {
            log(3, e.toString());
            return false;
        }
    }

    private void createKey() {
        // Generate a key to decrypt payment credentials, tokens, etc.
        // This will most likely be a registration step for the user when they are setting up your app.
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    // Require that the user has unlocked in the last 30 seconds
                    .setUserAuthenticationValidityDurationSeconds(30)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            log(3, "key: " + e.toString());
        }
    }
}
class DBHelper extends SQLiteOpenHelper {
    public DBHelper (Context context) {
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создаем таблицу с полями
        db.execSQL("create table token ("
                + "id integer primary key autoincrement,"
                + "token text,"
                + "uuid text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
