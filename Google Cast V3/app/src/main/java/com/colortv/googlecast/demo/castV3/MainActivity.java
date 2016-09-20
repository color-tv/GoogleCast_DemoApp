package com.colortv.googlecast.demo.castV3;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.colortv.android.googlecast.ColorTvCastError;
import com.colortv.android.googlecast.ColorTvCastSDK;
import com.colortv.android.googlecast.listeners.ColorTvCastAdListener;
import com.colortv.android.googlecast.listeners.OnCurrencyEarnedListener;
import com.colortv.googlecast.demo.castV3.connection.CastOptionsProvider;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static int GOOGLE_PLAY_SERVICES_ERROR_DIALOG_RESULT = 1;

    private static final String MESSAGE_CENTER = "MOVE_CENTER";
    private static final String MESSAGE_UP = "MOVE_UP";
    private static final String MESSAGE_DOWN = "MOVE_DOWN";
    private static final String MESSAGE_RIGHT = "MOVE_RIGHT";
    private static final String MESSAGE_LEFT = "MOVE_LEFT";
    private static final long VIBRATION_LENGTH = 100;

    private CastContext castContext;
    private SessionManager sessionManager;
    private CastSession castSession;
    private final SessionManagerListener sessionManagerListener = new SessionManagerListenerImpl();
    private Menu menu;
    private boolean isMediaRouteButtonSetUp;

    public class SessionManagerListenerImpl implements SessionManagerListener<CastSession> {
        @Override
        public void onSessionStarting(CastSession session) {

        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            castSession = session;
        }

        @Override
        public void onSessionStartFailed(CastSession session, int i) {
        }

        @Override
        public void onSessionEnding(CastSession session) {

        }

        @Override
        public void onSessionEnded(CastSession session, int error) {
            castSession = session;
        }

        @Override
        public void onSessionResuming(CastSession session, String s) {
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            castSession = session;
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int i) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int i) {
        }
    }

    public MainActivity() {

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

    protected boolean isConnected() {
        return castSession != null && castSession.isConnected();
    }

    protected void init() {
        castContext = CastContext.getSharedInstance(getApplicationContext());
        sessionManager = castContext.getSessionManager();
        if (!isMediaRouteButtonSetUp && menu != null) {
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.item_google_cast);
        }
        initColorTvCastSDK();
    }

    private void initColorTvCastSDK() {
        ColorTvCastSDK.setDebugMode(true);
        ColorTvCastSDK.init(getApplicationContext());
        registerListeners();
    }

    private void registerListeners() {
        ColorTvCastSDK.registerAdListener(new ColorTvCastAdListener() {
            @Override
            public void onAdOpened() {
                Log.d(TAG, "Ad Opened");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Ad Closed");
            }

            @Override
            public void onAdError(ColorTvCastError colorTvCastError) {
                Log.e("TAG", colorTvCastError.getErrorMessage());
            }
        });
        ColorTvCastSDK.addOnCurrencyEarnedListener(new OnCurrencyEarnedListener() {
            @Override
            public void onCurrencyEarned(String placement, int currencyAmount, String currencyType) {
                Toast.makeText(MainActivity.this, String.format("Granted %d %s for %s", currencyAmount, currencyType, placement), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void sendMessage(String message) {
        castSession.sendMessage(CastOptionsProvider.getNamespace(), message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        if (castContext != null) {
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.item_google_cast);
        } else {
            isMediaRouteButtonSetUp = false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        if (sessionManager != null) {
            castSession = sessionManager.getCurrentCastSession();
            sessionManager.addSessionManagerListener(sessionManagerListener);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sessionManager != null) {
            sessionManager.removeSessionManagerListener(sessionManagerListener);
            castSession = null;
        }
    }
}
