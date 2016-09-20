package com.colortv.googlecast.demo.castV3.connection;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.ArrayList;
import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    private static final String APP_ID = "CAEE9D41";

    private static final String TESTAPP_NAMESPACE = "urn:x-cast:com.colortv.testapp";

    @Override
    public CastOptions getCastOptions(Context context) {
        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId(APP_ID)
                .build();
        return castOptions;
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

    public static String getNamespace() {
        return TESTAPP_NAMESPACE;
    }
}
