/*
 * Copyright (C) 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.wearhighbandwidthnetworking;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity  {
    private static ImageView button_next;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Intent action for sending the user directly to the add Wi-Fi network activity.
    private static final String ACTION_ADD_NETWORK_SETTINGS =
            "com.google.android.clockwork.settings.connectivity.wifi.ADD_NETWORK_SETTINGS";

    // Message to notify the network request timout handler that too much time has passed.
    private static final int MESSAGE_CONNECTIVITY_TIMEOUT = 1;

    private static final long NETWORK_CONNECTIVITY_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);

    // The minimum network bandwidth required by the app for high-bandwidth operations.
    private static final int MIN_NETWORK_BANDWIDTH_KBPS = 500;

    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    // Handler for dealing with network connection timeouts.
    private final TimeOutHandler mTimeOutHandler = new TimeOutHandler(this);

    private ImageView mConnectivityIcon;
    private TextView mConnectivityText;

    private View mButton;
    private ImageView mButtonIcon;
    private TextView mButtonText;
    private TextView mInfoText;
    private View mProgressBar;
    private ImageView recylcer;

    // Tags added to the button in the UI to detect what operation the user has requested.
    static final String TAG_REQUEST_NETWORK = "REQUEST_NETWORK";
    static final String TAG_RELEASE_NETWORK = "RELEASE_NETWORK";
    static final String TAG_ADD_WIFI = "ADD_WIFI";

    // These constants are used by setUiState() to determine what information to display in the UI
    static final int UI_STATE_REQUEST_NETWORK = 1;
    static final int UI_STATE_REQUESTING_NETWORK = 2;
    static final int UI_STATE_NETWORK_CONNECTED = 3;
    static final int UI_STATE_CONNECTION_TIMEOUT = 4;
    private static String CHANNEL_ID = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mConnectivityIcon = findViewById(R.id.connectivity_icon);
        mConnectivityText = findViewById(R.id.connectivity_text);

        mProgressBar = findViewById(R.id.progress_bar);

        mButton = findViewById(R.id.button);
        mButton.setTag(TAG_REQUEST_NETWORK);
        mButtonIcon = findViewById(R.id.button_icon);
        mButtonText = findViewById(R.id.button_label);

        mInfoText = findViewById(R.id.info_text);
        recylcer = findViewById(R.id.recylcer);
        recylcer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MyRecyclerActivity.class);
                startActivity(intent);
            }
        });


        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        onClickButtonListner();
    }

    @Override
    public void onStop() {
        releaseHighBandwidthNetwork();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isNetworkHighBandwidth()) {
            setUiState(UI_STATE_NETWORK_CONNECTED);
        } else {
            setUiState(UI_STATE_REQUEST_NETWORK);
        }
    }

    private void unregisterNetworkCallback() {
        if (mNetworkCallback != null) {
            Log.d(LOG_TAG, "Unregistering network callback");
            mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mNetworkCallback = null;
        }
    }

    // Determine if there is a high-bandwidth network exists. Checks both the active
    // and bound networks. Returns false if no network is available (low or high-bandwidth).
    private boolean isNetworkHighBandwidth() {
        Network network = mConnectivityManager.getBoundNetworkForProcess();
        network = network == null ? mConnectivityManager.getActiveNetwork() : network;
        if (network == null) {
            return false;
        }

        // requires android.permission.ACCESS_NETWORK_STATE
        int bandwidth = mConnectivityManager
                .getNetworkCapabilities(network).getLinkDownstreamBandwidthKbps();

        return bandwidth >= MIN_NETWORK_BANDWIDTH_KBPS;

    }

    private void requestHighBandwidthNetwork() {
        unregisterNetworkCallback();

        Log.d(LOG_TAG, "Requesting high-bandwidth network");

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(final Network network) {
                mTimeOutHandler.removeMessages(MESSAGE_CONNECTIVITY_TIMEOUT);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // requires android.permission.INTERNET
                        if (!mConnectivityManager.bindProcessToNetwork(network)) {
                            Log.e(LOG_TAG, "ConnectivityManager.bindProcessToNetwork()"
                                    + " requires android.permission.INTERNET");
                            setUiState(UI_STATE_REQUEST_NETWORK);
                        } else {
                            Log.d(LOG_TAG, "Network available");
                            setUiState(UI_STATE_NETWORK_CONNECTED);
                        }
                    }
                });
            }

            @Override
            public void onCapabilitiesChanged(Network network,
                                              NetworkCapabilities networkCapabilities) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOG_TAG, "Network capabilities changed");
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                Log.d(LOG_TAG, "Network lost");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setUiState(UI_STATE_REQUEST_NETWORK);
                    }
                });
            }
        };

        // requires android.permission.CHANGE_NETWORK_STATE
        mConnectivityManager.requestNetwork(request, mNetworkCallback);

        mTimeOutHandler.sendMessageDelayed(
                mTimeOutHandler.obtainMessage(MESSAGE_CONNECTIVITY_TIMEOUT),
                NETWORK_CONNECTIVITY_TIMEOUT_MS);
    }

    private void releaseHighBandwidthNetwork() {
        mConnectivityManager.bindProcessToNetwork(null);
        unregisterNetworkCallback();
    }

    private void addWifiNetwork() {
        // requires android.permission.CHANGE_WIFI_STATE
        startActivity(new Intent(ACTION_ADD_NETWORK_SETTINGS));
    }

    public void onButtonClick(View view) {
        switch (view.getTag().toString()) {
            case TAG_REQUEST_NETWORK:
                requestHighBandwidthNetwork();
                setUiState(UI_STATE_REQUESTING_NETWORK);
                break;

            case TAG_RELEASE_NETWORK:
                releaseHighBandwidthNetwork();
                setUiState(UI_STATE_REQUEST_NETWORK);
                break;

            case TAG_ADD_WIFI:
                addWifiNetwork();
                break;
        }
    }

    // Sets the text and icons the connectivity indicator, button, and info text in the app UI

    private void setUiState(int uiState) {
        switch (uiState) {
            case UI_STATE_REQUEST_NETWORK:
                if (isNetworkHighBandwidth()) {
                    mConnectivityIcon.setImageResource(R.drawable.ic_cloud_happy);
                    mConnectivityText.setText(R.string.network_fast);
                } else {
                    mConnectivityIcon.setImageResource(R.drawable.ic_cloud_sad);
                    mConnectivityText.setText(R.string.network_slow);
                }

                mButton.setTag(TAG_REQUEST_NETWORK);
                mButtonIcon.setImageResource(R.drawable.ic_fast_network);
                mButtonText.setText(R.string.button_request_network);
                mInfoText.setText(R.string.info_request_network);

                break;

            case UI_STATE_REQUESTING_NETWORK:
                mConnectivityIcon.setImageResource(R.drawable.ic_cloud_disconnected);
                mConnectivityText.setText(R.string.network_connecting);

                mProgressBar.setVisibility(View.VISIBLE);
                mInfoText.setVisibility(View.GONE);
                mButton.setVisibility(View.GONE);

                break;

            case UI_STATE_NETWORK_CONNECTED:
                if (isNetworkHighBandwidth()) {
                    mConnectivityIcon.setImageResource(R.drawable.ic_cloud_happy);
                    mConnectivityText.setText(R.string.network_fast);
                } else {
                    mConnectivityIcon.setImageResource(R.drawable.ic_cloud_sad);
                    mConnectivityText.setText(R.string.network_slow);
                }

                mProgressBar.setVisibility(View.GONE);
                mInfoText.setVisibility(View.VISIBLE);
                mButton.setVisibility(View.VISIBLE);

                mButton.setTag(TAG_RELEASE_NETWORK);
                mButtonIcon.setImageResource(R.drawable.ic_no_network);
                mButtonText.setText(R.string.button_release_network);
                mInfoText.setText(R.string.info_release_network);

                break;

            case UI_STATE_CONNECTION_TIMEOUT:
                mConnectivityIcon.setImageResource(R.drawable.ic_cloud_disconnected);
                mConnectivityText.setText(R.string.network_disconnected);

                mProgressBar.setVisibility(View.GONE);
                mInfoText.setVisibility(View.VISIBLE);
                mButton.setVisibility(View.VISIBLE);

                mButton.setTag(TAG_ADD_WIFI);
                mButtonIcon.setImageResource(R.drawable.ic_wifi_network);
                mButtonText.setText(R.string.button_add_wifi);
                mInfoText.setText(R.string.info_add_wifi);

                break;
        }
    }

    private static class TimeOutHandler extends Handler {
        private final WeakReference<MainActivity> mMainActivityWeakReference;

        TimeOutHandler(MainActivity mainActivity) {
            mMainActivityWeakReference = new WeakReference<>(mainActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mMainActivityWeakReference.get();

            if (mainActivity != null) {
                switch (msg.what) {
                    case MESSAGE_CONNECTIVITY_TIMEOUT:
                        Log.d(LOG_TAG, "Network connection timeout");
                        mainActivity.setUiState(UI_STATE_CONNECTION_TIMEOUT);
                        mainActivity.unregisterNetworkCallback();
                        break;
                }
            }
        }
    }

    // Allows the user to send voice input if the network is strong enough to send the reply
    public void voiceInput (View view)
    {

        createNotificationChannel();
        notifyThis("My notification", "Hello Doctor!");
    }
    // Create the NotificationChannel
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void notifyThis(String title,String message){
        Intent intent = new Intent(this,NotificationDetails.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        String reply_label = getResources().getString(R.string.reply_label);
        String[] reply_choices = getResources().getStringArray(R.array.reply_choices);

        RemoteInput.Builder remoteInputBuilder = new RemoteInput.Builder(NotificationUtils.EXTRA_VOICE_REPLY);
        remoteInputBuilder.setLabel(reply_label);
        remoteInputBuilder.setChoices(reply_choices);
        RemoteInput remoteInput = remoteInputBuilder.build();
        NotificationCompat.Action.Builder notificationActionBuilder = new NotificationCompat.Action.Builder(R.drawable.ic_stat_name,"My Choice",pendingIntent);
        notificationActionBuilder.addRemoteInput(remoteInput);
        NotificationCompat.Action action = notificationActionBuilder.build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setContentTitle("Drag for reply option");
        builder.setContentText("This only works if you have built in wearable support speaker ");
        builder.setSmallIcon(R.drawable.ic_stat_name);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.extend(new NotificationCompat.WearableExtender().addAction(action));

        Notification notification= builder.build();


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1,notification);

    }

    public void onClickButtonListner(){
        button_next = findViewById(R.id.Animation_button);
        button_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this,SecondActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }

}
