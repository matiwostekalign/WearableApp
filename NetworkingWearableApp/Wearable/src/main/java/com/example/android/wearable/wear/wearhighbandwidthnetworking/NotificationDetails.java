package com.example.android.wearable.wear.wearhighbandwidthnetworking;

import android.app.RemoteInput;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

// class for voice replay
public class NotificationDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);
        CharSequence charSequence = getMessageText(getIntent());
        String reply_was = getResources().getString(R.string.reply_was);
        Toast.makeText(this,reply_was+charSequence,Toast.LENGTH_LONG).show();

    }

    private CharSequence getMessageText(Intent intent)
    {
        Bundle bundle = RemoteInput.getResultsFromIntent(intent);
        if (bundle!=null){
            return bundle.getCharSequence(NotificationUtils.EXTRA_VOICE_REPLY);
        }

        return null;
    }
}
