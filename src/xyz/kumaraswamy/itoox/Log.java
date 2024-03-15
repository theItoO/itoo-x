// Copyright (C) 2023 Kumaraswamy B G
// GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
// See LICENSE for full details

package xyz.kumaraswamy.itoox;

import android.content.Context;
import android.content.SharedPreferences;

public class Log {

  private static final String TAG = ItooCreator.class.getSimpleName();

  private final boolean logEnabled;

  public Log(Context context) {
    ItooPreferences preferences = new ItooPreferences(context, "AdditionalItooConfig");
    logEnabled = (boolean) preferences.read("debug_mode", true);
  }

  public void warn(String message) {
    if (!logEnabled) {
      return;
    }
    android.util.Log.w(TAG, message);
  }

  public void debug(String message) {
    // critical debug information is essential
    // things such as initialization status
    android.util.Log.d(TAG, message);
  }

  public void info(String message) {
    if (!logEnabled) {
      return;
    }
    android.util.Log.i(TAG, message);
  }

  public void error(String message) {
    if (!logEnabled) {
      return;
    }
    android.util.Log.e(TAG, message);
  }
}
