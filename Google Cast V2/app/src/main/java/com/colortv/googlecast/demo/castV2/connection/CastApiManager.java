package com.colortv.googlecast.demo.castV2.connection;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.colortv.android.googlecast.ColorTvCastSDK;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

public class CastApiManager {

    private static CastApiManager instance;
    private String appId;
    private GoogleApiClient apiClient;
    private CastChannel castChannel;

    private CastApiManager() {
    }

    private CastApiManager(String appId, CastChannel castChannel) {
        this.appId = appId;
        this.castChannel = castChannel;
    }

    public static void init(String appId, CastChannel castChannel) {
        if (instance == null) {
            instance = new CastApiManager(appId, castChannel);
        }
    }

    public static CastApiManager getInstance() {
        return instance;
    }

    public void prepareConnection(Context context, CastDevice selectedDevice) {
        if ((apiClient == null || !apiClient.isConnected()) && context != null && selectedDevice != null) {
            Cast.CastOptions.Builder apiOptionsBuilder = new Cast.CastOptions.Builder(selectedDevice, prepareCastClientListener());

            apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(prepareConnectionCallbacks())
                    .addOnConnectionFailedListener(prepareConnectionFailedListener())
                    .build();

            apiClient.connect();
            ColorTvCastSDK.setApiClient(apiClient);
        }
    }

    private Cast.Listener prepareCastClientListener() {
        return new Cast.Listener() {
            @Override
            public void onApplicationDisconnected(int i) {
                super.onApplicationDisconnected(i);
                Log.w(CastApiManager.class.getName(), "Disconnected");
            }
        };
    }

    private GoogleApiClient.ConnectionCallbacks prepareConnectionCallbacks() {
        return new GoogleApiClient.ConnectionCallbacks() {

            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (apiClient == null) {
                    return;
                }
                boolean isReceiverAppRunning = bundle != null && !bundle.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING);
                if (!isReceiverAppRunning) {
                    Cast.CastApi.launchApplication(apiClient, appId)
                            .setResultCallback(prepareLaunchApplicationCallback());

                } else {
                    Cast.CastApi.joinApplication(apiClient);
                }
            }

            private ResultCallback<? super Cast.ApplicationConnectionResult> prepareLaunchApplicationCallback() {
                return new ResultCallback<Cast.ApplicationConnectionResult>() {
                    @Override
                    public void onResult(Cast.ApplicationConnectionResult result) {
                        Status status = result.getStatus();
                        if (status.isSuccess()) {
                            setUpChannel();
                        }
                    }
                };
            }

            @Override
            public void onConnectionSuspended(int i) {
                apiClient.reconnect();
            }
        };
    }

    private GoogleApiClient.OnConnectionFailedListener prepareConnectionFailedListener() {
        return new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.w("ColorTvTestapp", "connection failed");
            }
        };
    }

    public void setUpChannel() {
        try {
            Cast.CastApi.setMessageReceivedCallbacks(apiClient, castChannel.getNamespace(), castChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeChannel() {
        try {
            Cast.CastApi.removeMessageReceivedCallbacks(apiClient, castChannel.getNamespace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GoogleApiClient getApiClient() {
        return apiClient;
    }

    public void sendMessage(String message) {
        if (isConnected()) {
            Cast.CastApi.sendMessage(apiClient, castChannel.getNamespace(), message);
        }
    }


    public boolean isConnected() {
        return apiClient != null && apiClient.isConnected();
    }
}
