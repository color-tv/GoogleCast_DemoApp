package com.colortv.googlecast.demo.castV2.connection;

import com.google.android.gms.cast.Cast;

public interface CastChannel extends Cast.MessageReceivedCallback {
    String getNamespace();
}