package com.example.chatv20;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.chatv20.MainActivity.log;

public class SignUpActivity extends AppCompatActivity {

    EditText et_login, et_password, et_info, et_name;
    FloatingActionButton fab;
    RadioGroup rg;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        et_login = findViewById(R.id.signup_login);
        et_password = findViewById(R.id.signup_password);
        et_info = findViewById(R.id.signup_email);
        et_name = findViewById(R.id.signup_name);
        fab = findViewById(R.id.signup_fab);
        rg = findViewById(R.id.signup_rgroup);

        context = this;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String login = et_login.getText().toString(),
                        pword = et_password.getText().toString(),
                        info = et_info.getText().toString(),
                        name = et_name.getText().toString();
                int x = 0;
                switch(rg.getCheckedRadioButtonId()) {
                    case R.id.signup_1:
                        x = 1;
                        break;
                    case R.id.signup_2:
                        x = 2;
                        break;
                    case R.id.signup_3:
                        x=3;
                }
                final int icon = x;
                try {
                    final Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                log(3, "registration");
                                URL url = new URL("https://warm-bayou-37022.herokuapp.com/reg");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                // TODO icon when sign up
                                connection.getOutputStream().write(("type=lp&login=" + login +
                                        "&password=" + pword + "&info=" + info + "&name=" + name).getBytes());
                                log(3, "written");
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                log(3, "got input stream");
                                StringBuilder builder = new StringBuilder();
                                for (int c; (c = reader.read()) >= 0; )
                                    builder.append((char) c);
                                //System.out.println(builder.toString());
                                if(!builder.toString().equals("success")) {
                                    Log.e("mylog", "no success: " + builder.toString());
                                    return;
                                }

                                log(3, "registrated");

                                url = new URL("https://warm-bayou-37022.herokuapp.com/login?type=lp&login=" + login
                                        + "&password=" + pword + "&save=false");
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");

                                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                builder = new StringBuilder();
                                for (int c; (c = reader.read()) >= 0; )
                                    builder.append((char) c);
                                Log.e("mylog", "builder:" + builder.toString());

                                String[] spl = builder.toString().split("\"");
                                String token, id;
                                if(spl.length > 7) {
                                    token = spl[7];
                                    id = spl[1];
                                } else
                                    return;

                                startActivity(new Intent(context, ChatsActivity.class)
                                        .putExtra("name", name)
                                        .putExtra("icon", 1)
                                        .putExtra("uuid", id)
                                        .putExtra("token", token)
                                        .putExtra("type", "normal")
                                );

                            } catch (Exception e) {
                                Log.e("mylog", e.getMessage());
                            }
                        }
                    };
                    t.start();
                } catch (Exception e) {
                    Log.e("mylog", e.getMessage());
                }
            }
        });
    }
}
