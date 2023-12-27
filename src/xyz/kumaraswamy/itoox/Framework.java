// Copyright (C) 2023 Kumaraswamy B G
// GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
// See LICENSE for full details

package xyz.kumaraswamy.itoox;

import android.content.Context;
import android.util.Log;

public class Framework {

  private static final String TAG = "FrameworkX";

  public static FrameworkResult get(Context context, String screen) {
    try {
      return new FrameworkResult(
          true,
          new Framework(context, screen)
      );
    } catch (Throwable th) {
      Log.d(TAG, "Unable to init ItooCreator, message: " + th.getMessage());
      th.printStackTrace();
      return new FrameworkResult(false, th);
    }
  }

  public static class FrameworkResult {

    private final boolean success;
    private final Object result;

    public FrameworkResult(boolean success, Object result) {
      this.success = success;
      this.result = result;
    }

    public boolean success() {
      return success;
    }
    public Framework getFramework() {
      return (Framework) result;
    }
    public Throwable getThrowable() {
      return (Throwable) result;
    }
  }

  private final ItooCreator creator;
  private boolean isClosed = false;

  private Framework(Context context, String screen) throws Throwable {
    creator = new ItooCreator(context, screen, true);
  }

  public CallResult call(String procedure, Object... args)  {
    try {
      if (isClosed) {
        throw new Exception("Framework is already closed");
      }
      return new CallResult(true, creator.startProcedureInvoke(procedure, args));
    } catch (Throwable t) {
      t.printStackTrace();
      return new CallResult(false, t);
    }
  }

  public boolean close() {
    try {
      creator.flagEnd();
      isClosed = true;
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean isClosed() {
    return isClosed;
  }

  public static class CallResult {

    private final boolean success;
    private final Object result;

    public CallResult(boolean success, Object result) {
      this.success = success;
      this.result = result;
    }

    public boolean success() {
      return success;
    }

    public Object get() {
      return result;
    }

    public Throwable getThrowable() {
      return (Throwable) result;
    }
  }
}
