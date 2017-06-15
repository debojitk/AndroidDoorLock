package com.test.arduinosocket.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.test.arduinosocket.R;
import com.test.arduinosocket.activity.adapters.RecordedMessageListAdapter;
import com.test.arduinosocket.common.Constants;

public class MessageViewActivity extends AppCompatActivity {
    ListView list;
    String[] web = {
            "Google Plus",
            "Twitter",
            "Windows",
            "Bing",
            "Itunes",
            "Wordpress",
            "Google Plus",
            "Twitter",
            "Windows",
            "Bing",
            "Itunes",
            "Wordpress",
            "Drupal"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String intentExtra = getIntent().getStringExtra(Constants.MESSAGES_STRING);
        String []messages=intentExtra.split(",");
        setContentView(R.layout.activity_message_view);
        RecordedMessageListAdapter adapter = new
                RecordedMessageListAdapter(MessageViewActivity.this, messages);
        list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
    }
    @Override
    protected void onStart(){
        super.onStart();
        String intentExtra = getIntent().getStringExtra(Constants.MESSAGES_STRING);
        String []messages=intentExtra.split(",");

    }
}
