package com.example.chatv20;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.ws.client.PayloadGenerator;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.util.Log.e;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ScrollView ll;
    FloatingActionButton fab;
    EditText et;
    Handler h;
    LinearLayout layout;
    WebSocket socket_read, socket_write;
    BroadcastReceiver mDLCompleteReceiver;
    String name;
    ProgressBar pb;
    int icon;
    static String id;
    int update_id;
    String url;
    boolean alive, layout_notif, sending, uploading;
    boolean reconnecting = false;
    Helper helper;
    SQLiteDatabase database;
    int notif_id = 1;
    int upload_id = 0;
    int upload_table_count = 0;
    int scroll_from_end = 0;
    int last_message_height = 0;
    final int MSG = 0;
    final int TEXT_MSG = 1;
    final int RECONNECT = 2;
    final int SCROLL_TO_LAST = 3;
    final int SET_IMAGE = 4;
    final int START_DOWNLOADING = 5;
    final int END_DOWNLOADING = 6;
    final int CHANGE_BOO = 7;
    boolean downloaded = true;
    String dmm_id;
    DownloadManager dm;
    String original_activity;
    Snackbar snackbar;
    Context context = this;
    View view_on_top;
    LocalDate last_view_date = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate last_upload_date = last_view_date;
    final LocalDate current_date = last_view_date;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getIntent().getStringExtra("type").equals("notif")) {
//            finish();
//        }




        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log(2,"NEW ACTIVITY: " + getApplicationInfo());


/*
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

        for (ResolveInfo temp : appList) {
            if(temp.activityInfo.packageName.equals("com.example.chatv20")) {

                Log.v("mylog", "package and activity name = "
                        + temp.activityInfo.packageName + "    "
                        + temp.activityInfo.name);
                //temp.activityInfo.applicationInfo.
            }
        }*/
        log(1, this.toString());

        helper = new Helper(this);
        database = helper.getWritableDatabase();
        dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        //File file = new File(getCacheDir().getPath()  +
        //        File.separator + "image_test");
        //Log.e("mylog", "delete: " + file.delete());
        //file = new File(getFilesDir() + File.separator + "image_test");
        //Log.e("mylog", "delete2: " + file.delete());
/*
        File main_dir = Environment.getDataDirectory();
        e("mylog", main_dir.getAbsolutePath());
        try {
            writer = new BufferedWriter(new OutputStreamWriter(openFileOutput("log.txt", MODE_PRIVATE)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //showData(main_dir);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        e("mylog", "");

        File dir = getFilesDir();
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            e("mylog", files[i].getAbsolutePath());
            if(files[i].isDirectory()) {
                for (File f:files) {
                    e("mylog", f.getAbsolutePath());
                }
            }
        }
        e("mylog", "files done");*/


        mDLCompleteReceiver =
                new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                downloaded = true;
                /* our download */
                //if (DL_ID == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)) {
                //e("mylog", "nfljnlvnlkn");
                h.sendEmptyMessage(CHANGE_BOO);
                /* get the path of the downloaded file */
                DownloadManager.Query query = new DownloadManager.Query();
                //query.setFilterById(DL_ID);
                Cursor cursor = null;
                cursor = dm.query(query);
                String path = null;
                if (!cursor.moveToFirst()) {
                    log(2, "Download error: cursor is empty");
                    path = "error";
                    //return;
                }

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        != DownloadManager.STATUS_SUCCESSFUL) {
                    log(2,"Download failed: no success status");
                    path = "error";
                    //return;
                }
                if(path==null)
                    path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                log(1, "File download complete. Location: \n" + path);
                //h.sendMessage(h.obtainMessage(SET_IMAGE, path + "�" + name));

                    /*Cursor c = database.query("messages", null, "id = ?", new String[] {"" + id},
                            null, null, null);
                    ContentValues cv = new ContentValues();
                    if(c.moveToFirst()) {
                        cv.put("id");
                    }*/
                ContentValues cv = new ContentValues();
                cv.put("path", path);
                log(1, "insert count: " + database.update("messages", cv, "id = ?", new String[]{update_id + ""}));

                Cursor c = database.query("messages", null, "path = ?", new String[]{path},
                        null, null, null);
                if (c.moveToFirst()) {
                    log(1, "text:: " + c.getString(c.getColumnIndex("text")));
                    log(1, "path:: " + c.getString(c.getColumnIndex("path")));
                } else {
                    log(2, "wroong");
                }
                c.close();

                cv = new ContentValues();
                cv.put("url", url);
                cv.put("path", path);
                database.insert("images", null, cv);
            }
        };
        registerReceiver(mDLCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        /* register receiver to listen for ACTION_DOWNLOAD_COMPLETE action */



        //database.delete("messages", null, null);



//
//        Cursor c = database.query("messages", null, null, null, null,
//                null, null);
//        if(c.moveToFirst()) {
//            int i_id = c.getColumnIndex("id");
//            int i_uuid = c.getColumnIndex("uuid");
//            int i_name = c.getColumnIndex("name");
//            int i_text = c.getColumnIndex("text");
//            int i_icon = c.getColumnIndex("icon");
//            int i_type = c.getColumnIndex("type");
//            int i_time = c.getColumnIndex("time");
//            int i_path = c.getColumnIndex("path");
//            do {
//                Log.e("mylog", c.getString(i_id) + " " + c.getString(i_name) + " " + c.getString(i_text) +
//                        " " + c.getString(i_icon) + " " + c.getString(i_uuid) + " " + c.getString(i_time) +
//                        " " + c.getString(i_type));
//                Log.e("mylog", "path: " + c.getString(i_path));
//            } while (c.moveToNext());
//        } else {
//            Log.e("mylog", "NO MESSAGES");
//        }
//        c.close();
        BroadcastReceiver internet_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(hasConnection(context)) {
                    snackbar.dismiss();
                    start();
                } else {
                    snackbar.show();
                }
            }
        };
        registerReceiver(internet_receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        dmm_id = getResources().getString(R.string.dmm_id);

        alive = true;
        layout_notif = false;
        sending = false;
        reconnecting = false;
        uploading = false;
        if (getIntent().getStringExtra("type").equals("notif")) {
            layout_notif = true;
        } else {
            CharSequence ch_name = "New messages";
            String description = "CHANNEL_DESCRIPTION";

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("1", ch_name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        name = getIntent().getStringExtra("name");
        icon = getIntent().getIntExtra("icon", 1);
        id = getIntent().getStringExtra("uuid");
        layout = findViewById(R.id.layout);
        ll = findViewById(R.id.ll);
        ll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //Log.e("mylog", "" + hasConnection(context));
                if(scrollY<=5&&hasConnection(context)&&!uploading  //&&upload_table_count<1
                        ) {
                    log(1, "scrollY: " + scrollY + ", oldSY: " + oldScrollY);
                    upload_id = 0;
                    uploading = true;
                    scroll_from_end = ll.getHeight()-scrollY;
                    view_on_top = ll.getChildAt(0);
                    log(1,"max scroll amount: " + ll.getMaxScrollAmount());
                    log(1,"bottom: " + ll.getBottom());
                    log(1,"height: " + ll.getHeight());
                    log(1,"measured height: " + ll.getMeasuredHeight());
                    database.execSQL("create table if not exists upload_msg" + ++upload_table_count + " (" +
                            "id integer primary key autoincrement," +
                            "uuid text," +
                            "text text," +
                            "name text," +
                            "icon text," +
                            "type text," +
                            "time text," +
                            "path text" +
                            ");");
                    Cursor c = database.query("messages", null, null, null, null,
                            null, null);
                    c.moveToFirst();
                    int msg_count = 1;
                    while(c.moveToNext()) msg_count++;
                    c.close();
                    socket_read.sendText("{\"lim\":30,\"msg\":\"false\",\"key\":\"" + id + "\",\"start\":" + (msg_count + upload_table_count*30-30) + "}");
                    log(1, "uploading " + (msg_count + upload_table_count*30-30) + " - " + (msg_count + upload_table_count*30));
                    log(1, "upload sended");
                    ContentValues cv = new ContentValues();
                    cv.put("value", "" + upload_table_count);
                    database.update("stuff", cv, "title = ?", new String[] {"upload_img_count"});
                }
            }
        });
        et = findViewById(R.id.et);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        pb = findViewById(R.id.pb);

        snackbar = Snackbar.make(layout, "No internet connection", Snackbar.LENGTH_INDEFINITE);

        //  main method
        start();

    }
    /*
    scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
            }
        });
     */

    @Override
    public void onClick(View v) {
        //check();
        if(et.getText().toString().replaceAll(" ", "").equals(""))
            return;
        final String text = et.getText().toString();
        et.setText("");
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    socket_write = socket_write.recreate();
                    socket_write.connect();
                    //String message = "{\"system\":\"false\",\"uuid\":\"170.51287793191963\",\"text\":\"" + text + "\"," +
                    //        "\"name\":\"спамер\",\"icon\":\"3\",\n" +
                    //        "\t\"time\":\"October 26th 2018, 3:58:49 pm\",\"type\":\"text\"}\n";

                    SimpleDateFormat format = new SimpleDateFormat("MMMM d", Locale.UK);
                    SimpleDateFormat format1 = new SimpleDateFormat(" YYYY, h:mm:ss a", Locale.UK);

                    String date = format.format(new Date()) + "th" + format1.format(new Date());
                    JSONObject object = new JSONObject();
                    object.put("system", "false")
                            .put("uuid", id)
                            .put("text", text)
                            .put("name", name)
                            .put("icon", "" + icon)
                            .put("time", date)
                            .put("type", "text");
                    socket_write.sendText(object.toString());
                    socket_write.disconnect();
//                    socket.sendText("{\"system\":false,\"uuid\":\"170.51287793191963\",\"text\":\"test message 1\",\"name\":\"спамер\",\"icon\":\"3\",\n" +
//                            "\t\"time\":\"October 26th 2018, 3:58:49 pm\",\"type\":\"text\"}\n");

                } catch (Exception e) {
                    log(2, e.toString());
                    if(e.getMessage().contains("503 Service Unavailable")) {
                        makeToast("Сервер недоступен (ошибка 503)");
                    }
                }
            }
        };
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Quit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SQLiteDatabase db = new DBHelper(this).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", "");
        cv.put("uuid", "");
        cv.put("icon", 1);
        log(1,"data");
        int integer = db.update("users", cv, "id = 1", null);
        log(1, integer + "");
        if(integer==-1)
            Toast.makeText(this, "Data error", Toast.LENGTH_SHORT).show();
        else {
            startActivity(new Intent(this, Login.class));
        }
        return super.onOptionsItemSelected(item);
    }

    void createView(String[] text, int icon, boolean uploading) {
        try {

            log(1, "createView, text: " + Arrays.toString(text) + ", upload_id: " + upload_id);
            SimpleDateFormat format = new SimpleDateFormat("MMMM d", Locale.UK);
            SimpleDateFormat format1 = new SimpleDateFormat(" YYYY, h:mm:ss a", Locale.UK);

            String[] spl = text[2].split("th");
            if(spl.length<2) {
                spl = text[2].split("nd");
            }
            if(spl.length<2) {
                spl = text[2].split("st");
            }
            if(spl.length<2) {
                spl = text[2].split("rd");
            }
            String time_text;
            if(spl.length<2) {
                time_text="??:??";
            } else {
                Date d_time = format1.parse(spl[1]);
                Date year = format.parse(spl[0]);
                year.setYear(d_time.getYear() + 1);
                time_text = d_time.getHours() + ":" + String.format("%02d", d_time.getMinutes());
                LocalDate day = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

//            e("mylog", "last_date: " + last_view_date);
//            e("mylog", "date: " + day);
                if (layout.getChildCount() == 1) last_upload_date = day;

                if (uploading) {
                    if (last_upload_date.getDayOfYear() > day.getDayOfYear() || last_upload_date.getYear() > day.getYear()) {
                        log(1, "creating divider");
                        View divider = getLayoutInflater().inflate(R.layout.time_divider, layout, false);
                        ((TextView) divider.findViewById(R.id.tv_divider))
                                .setText(last_upload_date.getDayOfMonth() + "." + String.format("%02d",
                                        last_upload_date.getMonthValue()));
                        layout.addView(divider, 0);
                    }
                    last_upload_date = day;
                } else {
                    if (last_view_date.getDayOfYear() < day.getDayOfYear() || last_view_date.getYear() < day.getYear()) {
                        log(1, "creating divider");
                        View divider = getLayoutInflater().inflate(R.layout.time_divider, layout, false);
                        String div_text;
                        if (day.equals(current_date)) {
                            div_text = "Сегодня";
                        } else if (day.getDayOfYear() == (current_date.getDayOfYear() == 1 ? 365 : day.getDayOfYear() - 1) ||
                                (day.getDayOfYear() == 366 && current_date.getDayOfYear() == 1)) {
                            div_text = "Вчера";
                        } else {
                            div_text = day.getDayOfMonth() + "." + String.format("%02d", day.getMonthValue());
                        }
                        ((TextView) divider.findViewById(R.id.tv_divider)).setText(div_text);
                        layout.addView(divider);
                    }
                    last_view_date = day;
                }
            }

            Text f = new Text(text[1], 20);
            String t = f.Text();

            View view;
            TextView tv, tv_time;
            ImageView img;
            if (icon == 4) {
                view = getLayoutInflater().inflate(R.layout.text_right_layout, layout, false);
                tv = view.findViewById(R.id.tv_right_text);
                tv.setText(t);
                img = view.findViewById(R.id.img_right_icon);
                Drawable drawable;
                switch (this.icon) {
                    case 1:
                        drawable = getDrawable(R.drawable.icon_1);
                        img.setImageDrawable(drawable);
                        break;
                    case 2:
                        drawable = getDrawable(R.drawable.icon_2);
                        img.setImageDrawable(drawable);
                        break;
                    case 3:
                        drawable = getDrawable(R.drawable.icon_3);
                        img.setImageDrawable(drawable);
                        break;
                }
                tv_time = view.findViewById(R.id.tv_right_time);
            } else {
                view = getLayoutInflater().inflate(R.layout.text_layout, layout, false);
                //layout.addView(view);
                tv = view.findViewById(R.id.tv_text);
                tv.setText(t);
                img = view.findViewById(R.id.img_icon);
                Drawable drawable;
                if (text[0].contains("���")) {
                    text[0] = text[0].replaceAll("���", "");
                    //tv_name.setTextColor(Color.rgb(255, 0, 144));
                    drawable = getDrawable(R.drawable.my_icon);
                    img.setImageDrawable(drawable);
                } else {
                    switch (icon) {
                        case 1:
                            drawable = getDrawable(R.drawable.icon_1);
                            img.setImageDrawable(drawable);
                            break;
                        case 2:
                            drawable = getDrawable(R.drawable.icon_2);
                            img.setImageDrawable(drawable);
                            break;
                        case 3:
                            drawable = getDrawable(R.drawable.icon_3);
                            img.setImageDrawable(drawable);
                            break;
                    }
                }
                TextView tv_name = view.findViewById(R.id.tv_name);
                tv_name.setText(text[0]);
                tv_time = view.findViewById(R.id.tv_time);
            }



            tv_time.setText(time_text);

            if (uploading) {
                log(1,"child count: " + layout.getChildCount());
//
                layout.addView(view, upload_id++);
            } else {
                layout.addView(view);
                log(1, "h: " + view.getHeight());

                last_message_height = view.getHeight();

                ll.post(new Runnable() {
                    @Override
                    public void run() {
                        ll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        } catch (Exception e) {
            log(2, e.toString());
        }

    }

    void createImage(String path, String name, int icon, String time, boolean uploading) {
        try {
            log(1,"createImage, path: " + path + ", name: " + name + ", icon: " + icon + ", upload_id: " + upload_id);

            SimpleDateFormat format = new SimpleDateFormat("MMMM d", Locale.UK);
            SimpleDateFormat format1 = new SimpleDateFormat(" YYYY, h:mm:ss a", Locale.UK);

            String[] spl = time.split("th");
            if(spl.length<2) {
                spl = time.split("nd");
            }
            if(spl.length<2) {
                spl = time.split("st");
            }
            if(spl.length<2) {
                spl = time.split("rd");
            }

            Date d_time = format1.parse(spl[1]);
            Date year = format.parse(spl[0]);
            year.setYear(d_time.getYear()+1);

            LocalDate day = year.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

//            e("mylog", "last_date: " + last_view_date);
//            e("mylog", "date: " + day);
            if(layout.getChildCount() == 1) last_upload_date = day;

            if(uploading) {
                if (last_upload_date.getDayOfYear() > day.getDayOfYear() || last_upload_date.getYear() > day.getYear()) {
                    log(1, "creating divider");
                    View divider = getLayoutInflater().inflate(R.layout.time_divider, layout, false);
                    ((TextView) divider.findViewById(R.id.tv_divider))
                            .setText(last_upload_date.getDayOfMonth() + "." + String.format("%02d",
                                    last_upload_date.getMonthValue()));
                    layout.addView(divider, 0);
                }
                last_upload_date = day;
            } else {
                if (last_view_date.getDayOfYear() < day.getDayOfYear() || last_view_date.getYear() < day.getYear()) {
                    log(1,"creating divider");
                    View divider = getLayoutInflater().inflate(R.layout.time_divider, layout, false);
                    String div_text;
                    if (day.equals(current_date)) {
                        div_text = "Сегодня";
                    } else if (day.getDayOfYear() == (current_date.getDayOfYear() == 1 ? 365 : day.getDayOfYear() - 1) ||
                            (day.getDayOfYear() == 366 && current_date.getDayOfYear() == 1)) {
                        div_text = "Вчера";
                    } else {
                        div_text = day.getDayOfMonth() + "." + String.format("%02d", day.getMonthValue());
                    }
                    ((TextView) divider.findViewById(R.id.tv_divider)).setText(div_text);
                    layout.addView(divider);
                }
                last_view_date = day;
            }


            View view = getLayoutInflater().inflate(R.layout.img_layout, layout, false);
            ImageView icon_img = view.findViewById(R.id.icon_msg);
            TextView tv, tv_time;

            if (icon == 4) {
                view = getLayoutInflater().inflate(R.layout.img_right_layout, layout, false);
                ImageView img = view.findViewById(R.id.icon_msg_right);
                Drawable drawable;
                switch (this.icon) {
                    case 1:
                        drawable = getDrawable(R.drawable.icon_1);
                        img.setImageDrawable(drawable);
                        break;
                    case 2:
                        drawable = getDrawable(R.drawable.icon_2);
                        img.setImageDrawable(drawable);
                        break;
                    case 3:
                        drawable = getDrawable(R.drawable.icon_3);
                        img.setImageDrawable(drawable);
                        break;
                }
                if(path.equals("error")) {
                    drawable = getDrawable(R.drawable.error_image);
                } else {
                    drawable = Drawable.createFromPath(path);
                }
                ImageView img_view = view.findViewById(R.id.img_msg_right);
                img_view.setImageDrawable(drawable);
                tv = view.findViewById(R.id.text_msg_right);
                tv_time = view.findViewById(R.id.time_msg_right);
            } else if (name.contains("���")) {
                name = name.replaceAll("���", "");
                icon_img.setImageDrawable(getDrawable(R.drawable.my_icon));
                ImageView image = view.findViewById(R.id.img_msg);
                if(path.equals("error")) {
                    image.setImageDrawable(getDrawable(R.drawable.error_image));
                } else {
                    image.setImageDrawable(Drawable.createFromPath(path));
                }
                //tv.setTextColor(Color.rgb(255, 0, 144));
                tv = view.findViewById(R.id.text_msg);
                tv_time = view.findViewById(R.id.time_msg);
            } else {
                ImageView img_view = view.findViewById(R.id.img_msg);
                Drawable drawable;
                if(path.equals("error")) {
                    drawable = getDrawable(R.drawable.error_image);
                } else {
                    drawable = Drawable.createFromPath(path);
                }
                img_view.setImageDrawable(drawable);
                switch (icon) {
                    case 1:
                        drawable = getDrawable(R.drawable.icon_1);
                        icon_img.setImageDrawable(drawable);
                        break;
                    case 2:
                        drawable = getDrawable(R.drawable.icon_2);
                        icon_img.setImageDrawable(drawable);
                        break;
                    case 3:
                        drawable = getDrawable(R.drawable.icon_3);
                        icon_img.setImageDrawable(drawable);
                        break;
                }
                tv = view.findViewById(R.id.text_msg);
                tv_time = view.findViewById(R.id.time_msg);
            }
            tv_time.setText(d_time.getHours() + ":" + String.format("%02d", d_time.getMinutes()));

            tv.setText(name);
            if (uploading) {
//            if(layout.getChildCount() < upload_id+1) {
////                upload_id = 0;
////            }
                //upload_id = layout.getChildCount();
                // TODO
                layout.addView(view, upload_id++);//layout.getChildCount());
            } else {
                layout.addView(view);
            ll.post(new Runnable() {
                @Override
                public void run() {
                    ll.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
        } catch (Exception e) {
            log(2, e.toString());
        }
    }

    void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    void showNotification(String nam, String message, int icon) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        switch (icon) {
            case 1:
                builder.setSmallIcon(R.drawable.icon_1);
                break;
            case 2:
                builder.setSmallIcon(R.drawable.icon_2);
                break;
            case 3:
                builder.setSmallIcon(R.drawable.icon_3);
                break;
                default:
                    builder.setSmallIcon(R.drawable.icon_2);
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class)
                .putExtra("name", name)
                .putExtra("uuid", id)
                .putExtra("icon", icon)
                .putExtra("type", "notif"));
        builder.setContentTitle(nam)
                .setContentText(message)
                .setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_NO_CREATE));
        Notification notification = builder.build();


        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        log(1, "creating notif(icon: " + icon + ", message: " + message + ")");
        manager.notify(notif_id++, notification);
    }

    private void getImageFromURL(final String url, final int id) {//, final String name) {
        log(1, "creating image, url:" + url + ", id: " + id);
        this.url = url;
        update_id = id;
        DownloadManager.Request request;
        downloaded = false;

        try {
            request = new DownloadManager.Request(Uri.parse(url));
        } catch (IllegalArgumentException e) {
            log(2, "Error: " + e.getMessage());
            return;
        }
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle("DM Example");
        request.setDescription("Downloading file");

        /* we let the user see the download in a notification */
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        /* Try to determine the file extension from the url. Only allow image types. You
         * can skip this check if you only plan to handle the downloaded file manually and
         * don't care about file managers not recognizing the file as a known type */
        String[] allowedTypes = {"png", "jpg", "jpeg", "gif", "webp"};
        String suffix = url.substring(url.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(allowedTypes).contains(suffix)) {
            log(2, "Invalid file extension.");
            //return;
        }

        /* set the destination path for this download */
        String dest = getExternalFilesDir("/") +
                File.separator + "image_test";
        request.setDestinationInExternalFilesDir(this, dest, "name_of_the_file" + "." + suffix);
        log(1, "destination in external public dir: " + dest);
        /* allow the MediaScanner to scan the downloaded file */
        //request.allowScanningByMediaScanner();
       // request.setVisibleInDownloadsUi(false);
        /* this is our unique download id */
        dm.enqueue(request);
        //final long DL_ID  = dm.enqueue(request);
//        if (dm != null) {
//            DL_ID = dm.enqueue(request);
//        } else {DL_ID =-1; Log.e("mylog", "REQUEST WRONG");}


        waitForDownload();
        try {
            Thread.sleep(1000);
            log(1, "sleeeeping");
        } catch (Exception e) {}
        //unregisterReceiver(mDLCompleteReceiver);
    }

    void createAll(final boolean uploading, final boolean sending) {
        Thread t = new Thread() {
            @Override
            public void run() {
                int arg1;
                if(uploading) {
                    arg1 = 1;
                } else if(sending) {
                    arg1 = -1;
                } else {
                    arg1 = 0;
                }
                h.sendMessage(h.obtainMessage(START_DOWNLOADING, arg1));
                String table_name = (uploading?"upload_msg" + upload_table_count:"messages");
                log(1, "table " + table_name);
                Cursor c = database.query(table_name, null, "type = ?", new String[] {"img"},
                        null, null, null);
                Cursor cursor_img = database.query("images", null, null, null,
                        null, null, null);

                if (c.moveToFirst()) {
                    int i_text = c.getColumnIndex("text");
                    int i_id = c.getColumnIndex("id");
                    ContentValues cv;
                    m: do {
                            if (cursor_img.moveToFirst()) {
                                int i_url = cursor_img.getColumnIndex("url");
                                int i_path = cursor_img.getColumnIndex("path");
                                do {
                                    if (c.getString(i_text).equals(cursor_img.getString(i_url))) {
                                        cv = new ContentValues();
                                        cv.put("path", cursor_img.getString(i_path));
                                        long count = database.update((uploading?"upload_msg" + upload_table_count:"messages"),
                                                cv, "text = ?", new String[] {cursor_img.getString(i_url)});
                                        log(1, "upload = " + uploading  + ", update count = " + count);
                                        continue m;
                                    }
                                } while (cursor_img.moveToNext());
                            }
                            getImageFromURL(c.getString(i_text), c.getInt(i_id));
                        log(1,"getting img 1");
                    } while (c.moveToNext());
                }
                c.close();
                cursor_img.close();

                //SQLiteDatabase database = helper.getWritableDatabase();
                Cursor cursor = database.query(table_name,
                        null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int text_index = cursor.getColumnIndex("text");
                    int name_index = cursor.getColumnIndex("name");
                    int icon_index = cursor.getColumnIndex("icon");
                    int type_index = cursor.getColumnIndex("type");
                    int path_index = cursor.getColumnIndex("path");
                    int id_index = cursor.getColumnIndex("id");
                    int uuid_index = cursor.getColumnIndex("uuid");
                    int time_index = cursor.getColumnIndex("time");
                    do {
                        if (cursor.getString(type_index).equals("text")) {
                            //Log.e("mylog", "uuid: " + cursor.getString(uuid_index));
                            if(cursor.getString(uuid_index).replaceAll(" ", "")
                                    .equalsIgnoreCase(dmm_id)) {
                                h.sendMessage(h.obtainMessage(TEXT_MSG, arg1, Integer.parseInt(cursor.getString(icon_index)),
                                        new String[] {
                                                cursor.getString(name_index) + "���", cursor.getString(text_index),
                                                cursor.getString(time_index)
                                        }));
                            } else {
                                h.sendMessage(h.obtainMessage(TEXT_MSG, arg1, Integer.parseInt(cursor.getString(icon_index)),
                                        new String[]{
                                                cursor.getString(name_index), cursor.getString(text_index),
                                                cursor.getString(time_index)
                                        }));
                            }
                        } else {
                            log(1,"set image, id: " + cursor.getString(id_index) + ", text: " +
                                    cursor.getString(text_index));
                            log(1, "path: " + cursor.getString(path_index));
                            if(cursor.getString(uuid_index).replaceAll(" ", "")
                                    .equalsIgnoreCase(dmm_id)) {
                                h.sendMessage(h.obtainMessage(SET_IMAGE, arg1, Integer.parseInt(cursor.getString(icon_index)),
                                        new String[] {
                                                cursor.getString(path_index), cursor.getString(name_index) + "���",
                                                cursor.getString(time_index)
                                        }));
                            } else {
                                h.sendMessage(h.obtainMessage(SET_IMAGE, arg1, Integer.parseInt(cursor.getString(icon_index)),
                                        new String[]{
                                                cursor.getString(path_index), cursor.getString(name_index),
                                                cursor.getString(time_index)
                                        }));
                            }
                        }
                    } while (cursor.moveToNext());
                } else {
                    log(2, "no messages ???");
                    //makeToast("No messages (WTF?)");
                }
                cursor.close();
                log(1, "end downloading");
                h.sendEmptyMessage(END_DOWNLOADING);
                if(uploading) {
                    h.sendMessage(h.obtainMessage(SCROLL_TO_LAST));
                }
            }
        };
        t.start();
    }

    @SuppressLint("HandlerLeak")
    void start() {
        database.execSQL("drop table if exists messages;");
        //
        int t_count = 0;
        if(tableExists(database, "stuff")) {
            Cursor c = database.query("stuff", null, "title = ?", new String[] {"upload_img_count"},
                    null, null, null);
            c.moveToFirst();
            if(c.getCount() >= 1)
                t_count = Integer.parseInt(c.getString(c.getColumnIndex("value")));
            c.close();
            log(3, "count = " + t_count);
        }
        for (int i = 1; i <= t_count; i++) {
            database.execSQL("drop table if exists upload_msg" + i + ";");
        }
        database.execSQL("drop table if exists stuff;");
        //this.deleteDatabase("messages_db");

        log(1, "caсhe deleted");
        helper.onCreate(database);
        database.execSQL("create table if not exists images (" +
                "id integer primary key autoincrement," +
                "url text," +
                "path text" +
                ");");
        database.execSQL("create table if not exists stuff (" +
                "id integer primary key autoincrement," +
                "title text," +
                "value text);");
        ContentValues cv = new ContentValues();
        cv.put("title", "upload_img_count");
        cv.put("value", "0");
        database.insert("stuff", null, cv);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    //database = helper.getWritableDatabase();
                    socket_read = new WebSocketFactory().createSocket("wss://warm-bayou-37022.herokuapp.com/receive");
                    socket_write = new WebSocketFactory().createSocket("wss://warm-bayou-37022.herokuapp.com/submit");

                    socket_read.addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket websocket, String text) throws Exception {
                            log(0,"RECEIVED MESSAGE /receive : " + text);
                            h.sendMessage(h.obtainMessage(MSG, 0, 0, text));
                        }

                        @Override
                        public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            if (frame.getPayload() == null) {
                                log(2,"null frame SENT");
                                return;
                            }
                            log(1, "SENT MESSAGE /receive : " + frame.getPayloadText());
                            if (frame.getPayloadText().equals("\u0003�No more WebSocket frame from the server.") || frame.getPayloadText().contains("No more WebSocket frame from the server")) {
                                log(2,"TRYING TO RECONNECT...");
                                h.sendEmptyMessage(RECONNECT);
                            }
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                            log(2, "error: " + cause.toString());
                            if (cause.toString().contains("Failed to connect")) {
                                socket_read.connect();
                            }
                        }
                    });

                    socket_write.addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket websocket, String text) throws Exception {
                            log(0,"RECEIVED MESSAGE /submit : " + text);
                        }

                        @Override
                        public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            if (frame.hasPayload())
                                log(0, "SENDING MESSAGE /submit : " + frame.getPayloadText());
                        }

                        @Override
                        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                            log(2,"error: " + cause.toString());
                            Thread.sleep(10);
                            socket_write.connect();
                            snackbar.show();
                        }
                    });

                    socket_read.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
                    socket_write.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);

                    socket_read.setMissingCloseFrameAllowed(false);
                    socket_write.setMissingCloseFrameAllowed(false);

                    socket_read.setPingPayloadGenerator(new PayloadGenerator() {
                        @Override
                        public byte[] generate() {
                            try {
                                JSONObject object = new JSONObject();
                                object.put("system", "true")
                                        .put("name", "pong")
                                        .put("key", "key")
                                        .put("event", "pong");
                                return object.toString().getBytes();
                            } catch (Exception e) {
                                return new byte[0];
                            }
                        }
                    });
                    socket_write.setPingPayloadGenerator(new PayloadGenerator() {
                        @Override
                        public byte[] generate() {
                            try {
                                JSONObject object = new JSONObject();
                                object.put("system", "true")
                                        .put("name", "pong")
                                        .put("key", "key")
                                        .put("event", "pong");
                                return object.toString().getBytes();
                            } catch (Exception e) {
                                return new byte[0];
                            }
                        }
                    });

                    socket_read.setPingInterval(3999).connect();
                    socket_write.setPingInterval(3999).connect();


                    JSONObject object = new JSONObject();
                    object.put("key", id)
                            .put("msg", "true")
                            .put("name", name);
                    sending = true;
                    socket_read.sendText(object.toString());
                    //createAll();
                } catch (Exception e) {
                    log(2,"exception: " + e.toString() + ";; " + e.getMessage());
                }
            }
        };
        t.start();

       /* Thread pong = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3999);
                        log(0,"PONG");
                        //socket_write.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE).connect();

                        JSONObject object = new JSONObject();
                        object.put("system", "true")
                                .put("name", "pong")
                                .put("key", "key")
                                .put("event", "pong");
                        //socket_write.sendText(object.toString(), false);
                        //socket_read.sendText(object.toString(), false);

                    } catch (Exception e) {
                    }
                }
            }
        };*/
        //pong.start();


        h = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                    case RECONNECT:
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    reconnecting = true;
                                    Thread.sleep(20);
                                    log(2, "RECONNECTING");
                                    socket_read = socket_read.recreate();
                                    socket_read.connect();
                                    JSONObject object = new JSONObject();
                                    //object.put("key", 170.51287793191963);
                                    object.put("key", id)
                                            .put("msg", "true")
                                            .put("name", name);
                                    sending = true;
                                    socket_read.sendText(object.toString());
                                } catch (Exception e) {
                                    log(2, "handler exception: " + e.toString());
                                }
                            }
                        };
                        t.start();
                        break;

                    case MSG:

                        try {
                            final JSONObject object = new JSONObject((String) msg.obj);
                            ContentValues cv = new ContentValues();
                            if (object.has("system") && object.has("uuid")
                                    && object.has("type")) {
                                if (!reconnecting||uploading) {
                                    final int arg1 = (uploading?1:0);
                                    int msg_icon = Integer.parseInt(object.getString("icon"));
                                    if (object.getString("uuid").equals(MainActivity.id)) {
                                        msg_icon = 4;
                                    }
                                    log(1, "new message");
                                    cv.put("uuid", (String) object.get("uuid"));
                                    cv.put("name", (String) object.get("name"));
                                    cv.put("text", (String) object.get("text"));
                                    cv.put("type", (String) object.get("type"));
                                    cv.put("icon", msg_icon + "");
                                    cv.put("time", (String) object.get("time"));


                                    final long id = database.insert((uploading?"upload_msg" + upload_table_count:"messages"), null, cv);
                                    log(1, "message inserted, id: " + id);

                                    final int final_msg_icon = msg_icon;

                                    if (!sending&&!uploading) {
                                        if (object.get("type").equals("text")) {
                                            if (object.getString("uuid").replaceAll(" ", "")
                                                    .equalsIgnoreCase(dmm_id)) {
                                                h.sendMessage(h.obtainMessage(TEXT_MSG, arg1,
                                                        msg_icon,
                                                        new String[]{object.getString("name") + "���",
                                                                object.getString("text"),
                                                                object.getString("time")}));
                                            } else {
                                                h.sendMessage(h.obtainMessage(TEXT_MSG, arg1,
                                                        msg_icon,
                                                        new String[]{object.getString("name"),
                                                                object.getString("text"),
                                                                object.getString("time")}));
                                            }
                                        } else {
                                            Cursor cursor = database.query("images", null, "url = ?",
                                                    new String[]{object.getString("text")}, null,
                                                    null, null);
                                            if (cursor.moveToFirst()) {
                                                if (object.getString("uuid").replaceAll(" ", "")
                                                        .equalsIgnoreCase(dmm_id)) {
                                                    createImage(cursor.getString(cursor.getColumnIndex("path")).split("://")[1],
                                                            object.getString("name") + "���", msg_icon,
                                                            object.getString("time"), uploading);
                                                } else {
                                                    createImage(cursor.getString(cursor.getColumnIndex("path")).split("://")[1],
                                                            object.getString("name"), msg_icon, object.getString("time"), uploading);
                                                }
                                            } else {
                                                log(1,"getting img 2");
                                                getImageFromURL(object.getString("text"), (int) id);
                                                new Thread() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            Thread.sleep(100);
                                                        } catch (Exception e) {
                                                        }

                                                        final Cursor c = database.query("messages", null, "id = ?",
                                                                new String[]{id + ""}, null, null, null);
                                                        if (c.moveToFirst()) {
                                                            if (c.getString(c.getColumnIndex("path")) == null)
                                                                log(2, "DOWNLOADING IMAGE ERROR: null path");
                                                            else {
                                                                new Thread() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            Thread.sleep(100);
                                                                            Cursor cur_sor = database.query("messages", null, "id = ?",
                                                                                    new String[]{id + ""}, null, null, null);
                                                                            cur_sor.moveToFirst();
                                                                            if (object.getString("uuid").replaceAll(" ", "")
                                                                                    .equalsIgnoreCase(dmm_id)) {
                                                                                h.sendMessage(h.obtainMessage(SET_IMAGE, arg1, final_msg_icon, new String[]{
                                                                                        cur_sor.getString(cur_sor.getColumnIndex("path")),
                                                                                        cur_sor.getString(cur_sor.getColumnIndex("name")) + "���",
                                                                                        cur_sor.getString(cur_sor.getColumnIndex("time"))
                                                                                }));
                                                                            } else {
                                                                                h.sendMessage(h.obtainMessage(SET_IMAGE, arg1, final_msg_icon, new String[]{
                                                                                        cur_sor.getString(cur_sor.getColumnIndex("path")),
                                                                                        cur_sor.getString(cur_sor.getColumnIndex("name")),
                                                                                        cur_sor.getString(cur_sor.getColumnIndex("time"))
                                                                                }));
                                                                            }
                                                                            cur_sor.close();
                                                                        } catch (Exception e) {
                                                                            log(2, "er: " + e.toString());
                                                                        }

                                                                    }
                                                                }.start();


                                                            }
                                                        } else
                                                            log(2,"DOWNLOADING IMAGE ERROR");
                                                        c.close();
                                                    }
                                                }.start();
                                            }
                                            cursor.close();
                                        }
                                    }
                                }
                            }
                            if (object.has("end")) {
                                if (object.get("end").equals("true")) {
                                    if(uploading) {
                                        createAll(true, false);
                                        uploading = false;
                                    } else {
                                        createAll(false, sending);
                                        sending = false;
                                    }
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(50);
                                                reconnecting = false;
                                            } catch (Exception e) {
                                            }
                                        }
                                    };
                                }
                            }
                        } catch (Exception e) {
                            log(2, e.toString());
                            makeToast("Некорректный json");
                        }
                        break;
                    case TEXT_MSG:
                        createView((String[]) msg.obj, msg.arg2,msg.arg1==1);

                        if (!(alive || reconnecting ||
                                //layout_notif||
                                sending || msg.arg2 == 4 || msg.arg1 < 0)//&&MainActivity.this.toString().equals(original_activity)
                                ) {
                            // TODO
                            showNotification(((String[]) msg.obj)[0], ((String[]) msg.obj)[1], msg.arg2);
                        } else {
                            log(1, "alive: " + alive +
                                    ", layout_notif: " + layout_notif +
                                    ", arg1: " + msg.arg1 + ", icon: " + msg.arg2 +
                                    ", original activity: " + original_activity +
                                    ", this activity: " + MainActivity.this.toString()
                            );
                            //   teeest
                        }
                        break;
                    case SET_IMAGE:
                        String path = ((String[]) msg.obj)[0];
                        String name = ((String[]) msg.obj)[1];
                        String time = ((String[]) msg.obj)[2];
                        //createView(name + "�");
                        log(1, "path: " + path + ", name : " + name);
                        if (path == null) {
                            log(2, "AAAAAAAAAAAA");
                            createView(new String[]{":/ :", "//// загрузка провалилась", "время ошибок"}, 1, false);
                        } else
                            createImage(path.split("://")[1], name, msg.arg2, time, msg.arg1==1);
                        break;
                    case START_DOWNLOADING:
                        if(!uploading && (int)msg.obj !=1) {
                            log(1, "removing all views");
                            layout.removeAllViews();
                        }
                        pb.setVisibility(View.VISIBLE);
                        ll.setVisibility(View.INVISIBLE);
                        break;
                    case END_DOWNLOADING:
                        pb.setVisibility(View.INVISIBLE);
                        ll.setVisibility(View.VISIBLE);
                        break;
                    case CHANGE_BOO:
                        downloaded = true;
                        break;
                    case SCROLL_TO_LAST:
                        ll.post(new Runnable() {
                            @Override
                            public void run() {
                                //int scroll_layout_h = ll.getHeight();
                                //int scroll_h = Math.abs(last_message_height);
                                //e("mylog", "ll height: " + scroll_layout_h + ", last msg height: " + last_message_height);
                                DisplayMetrics displaymetrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                                int displayHeight = displaymetrics.heightPixels / 4;

                                log(3,"top v: " + view_on_top.getTop());

                                //     скроллим до последнего просмотренного сообщения + четверть экрана (чтобы удобно было)
                                log(3, "top: " + layout.getChildAt(30).getTop());
                                log(3, "h: " + layout.getHeight());
                                ll.scrollTo(0, layout.getChildAt(30).getTop()-displayHeight);//scroll_h - displayHeight);
                            }
                        });
                }
            }
        };
    }


    void waitForDownload() {
        int i = 1;
        do {
            i++;
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                log(2, e.toString());
            }
            if(downloaded) {
                log(1, "waiting " + i*5 + " millis");
                try {
                    Thread.sleep(200);
                } catch (Exception e) {}
            }
            if(i==500) {
                log(1, "too long to wait");
                break;
            }
        } while (!downloaded);
    }

    //BufferedWriter writer;
    //        функция, показывающая содержимое папки (на всякий)
    /*void showData(File file) {
        try {

            writer.append(file.getPath()).append("\n");
            //writer.close();
            //Log.e("mylog", file.getPath());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null)
                    for (File f : files) {
                        showData(f);
                    }
            }
        } catch (Exception e) {
            e("mylog", e.getMessage());
        }
    }*/


    @Override
    protected void onResume() {
        log(2, "resume, deleting notifications");
        alive = true;
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.cancelAll();
//        do {
//            if (hasConnection(this)) {
//                break;
//            }
//        } while (true);
        log(1,this.toString());
        super.onResume();
    }

    @Override
    protected void onRestart() {
        log(2, "restart");
        // TODO restart socket connection

        alive = true;
        super.onRestart();
    }

    @Override
    protected void onStart() {
        log(2, "start");
        alive = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        log(2,"stop");

        alive = false;
        super.onStop();
    }

    @Override
    protected void onPause() {
        log(2,"pause");
        alive = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        log(2, "destroy");

        super.onDestroy();
    }

    boolean tableExists(SQLiteDatabase db, String tableName) {
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    static void log(int type, String text) {
        switch (type) {
            case 0:
                Log.i("mylog", text);
                break;
            case 1:
                Log.v("mylog", text);
                break;
            case 2:
                Log.e("mylog", text);
                break;
                default:
                    Log.e("mylog", text + " (important)");
        }
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }
}
class Helper extends SQLiteOpenHelper {

    Helper(Context context) {super(context, "messages_db", null, 1);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table messages (" +
                "id integer primary key autoincrement," +
                "uuid text," +
                "text text," +
                "name text," +
                "icon text," +
                "type text," +
                "time text," +
                "path text" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
