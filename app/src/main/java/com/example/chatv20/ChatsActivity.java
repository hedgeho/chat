package com.example.chatv20;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ChatsActivity extends AppCompatActivity {

    ConstraintLayout main, main_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        main = findViewById(R.id.chats_main_l);
        main_chat = findViewById(R.id.chats_main_chat);

        final String name = getIntent().getStringExtra("name"),
                uuid = getIntent().getStringExtra("uuid"),
                type = getIntent().getStringExtra("type"),
                token = getIntent().getStringExtra("token");
        final int icon = getIntent().getIntExtra("icon", 0);

        final Context context = this;

        main_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, MainActivity.class)
                        .putExtra("name", name)
                        .putExtra("icon", icon)
                        .putExtra("uuid", uuid)
                        .putExtra("type", type)
                        .putExtra("token", token));
            }
        });
    }
}
