package com.colortv.googlecast.demo.castV2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.colortv.android.googlecast.ColorTvCastSDK;
import com.colortv.googlecast.demo.castV2.connection.CastApiManager;
import com.colortv.googlecast.demo.castV2.connection.CastChannel;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static int GOOGLE_PLAY_SERVICES_ERROR_DIALOG_RESULT = 1;

    private static final String MESSAGE_CENTER = "MOVE_CENTER";
    private static final String MESSAGE_UP = "MOVE_UP";
    private static final String MESSAGE_DOWN = "MOVE_DOWN";
    private static final String MESSAGE_RIGHT = "MOVE_RIGHT";
    private static final String MESSAGE_LEFT = "MOVE_LEFT";
    private static final long VIBRATION_LENGTH = 100;

    private static final String APP_ID = "CAEE9D41";

    private MediaRouteSelector mediaRouteSelector;
    private CastDevice selectedDevice;
    private MediaRouter mediaRouter;
    private MediaRouter.Callback mediaRouterCallback;
    private TestappChannel testappChannel;
    private final CastApiManager castApiManager;
    private boolean mediaRouteItemInitialized;
    private Menu menu;

    private class TestappChannel implements CastChannel {
        public String getNamespace() {
            return "urn:x-cast:com.colortv.testapp";
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
        }
    }

    public MainActivity() {
        testappChannel = new TestappChannel();
        CastApiManager.init(APP_ID, testappChannel);
        castApiManager = CastApiManager.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView btnCenter = (ImageView) findViewById(R.id.btn_center);
        ImageView btnUp = (ImageView) findViewById(R.id.btn_up);
        ImageView btnDown = (ImageView) findViewById(R.id.btn_down);
        ImageView btnLeft = (ImageView) findViewById(R.id.btn_left);
        ImageView btnRight = (ImageView) findViewById(R.id.btn_right);

        btnCenter.setOnClickListener(prepareControllerOnClickListener(MESSAGE_CENTER));
        btnUp.setOnClickListener(prepareControllerOnClickListener(MESSAGE_UP));
        btnDown.setOnClickListener(prepareControllerOnClickListener(MESSAGE_DOWN));
        btnRight.setOnClickListener(prepareControllerOnClickListener(MESSAGE_RIGHT));
        btnLeft.setOnClickListener(prepareControllerOnClickListener(MESSAGE_LEFT));

        int connectionResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (connectionResult == ConnectionResult.SUCCESS) {
            init();
        } else {
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult, GOOGLE_PLAY_SERVICES_ERROR_DIALOG_RESULT);
            errorDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_PLAY_SERVICES_ERROR_DIALOG_RESULT && resultCode == RESULT_OK) {
            init();
        }
    }

    protected void init() {
        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();

        mediaRouterCallback = prepareMediaRouterCallback();
        mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        mediaRouter.updateSelectedRoute(mediaRouteSelector);
        if (!mediaRouteItemInitialized && menu != null) {
            initGoogleCastButton();
        }
        initColorTvCastSDK();
    }

    private void initColorTvCastSDK() {
        ColorTvCastSDK.setDebugMode(true);
        ColorTvCastSDK.init(getApplicationContext(), mediaRouteSelector);
    }

    protected void sendMessage(String message) {
        castApiManager.sendMessage(message);
    }

    protected boolean isConnected() {
        return castApiManager.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        if (mediaRouteSelector != null) {
            initGoogleCastButton();
        } else {
            mediaRouteItemInitialized = false;
        }
        return true;
    }

    public void initGoogleCastButton() {
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.item_google_cast);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);
    }

    private MediaRouter.Callback prepareMediaRouterCallback() {
        return new MediaRouter.Callback() {

            @Override
            public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
                selectedDevice = CastDevice.getFromBundle(info.getExtras());
                castApiManager.prepareConnection(getApplicationContext(), selectedDevice);
            }

            @Override
            public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
                selectedDevice = null;
                castApiManager.getApiClient().disconnect();
            }
        };
    }

    @Override
    protected void onDestroy() {
        if (mediaRouter != null) {
            mediaRouter.removeCallback(mediaRouterCallback);
        }
        super.onDestroy();
    }

    private View.OnClickListener prepareControllerOnClickListener(final String message) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VIBRATION_LENGTH);
                    sendMessage(message);
                }
            }
        };
    }
}
