// Copyright (C) 2023 Kumaraswamy B G
// GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
// See LICENSE for full details

package xyz.kumaraswamy.itoox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.JsonUtil;
import gnu.expr.ModuleMethod;
import gnu.lists.LList;
import gnu.mapping.Symbol;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.Arrays;

public class UIProcedureInvocation {
  // This class will be started from the application thread.
  // It will listen to requests from the Itoo-X (that'll be running in Background)
  // to call UI App procedures.
  // Basically, when we need the flow to move from Background -> Application

  private static final String TAG = "ItooXUI";
  private static final String VARS_FIELD_NAME = "global$Mnvars$Mnto$Mncreate";

  public static final String ACTION = "UI_ITOO_X_RECEIVER";

  private static final BroadcastReceiver RECEIVER = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Bundle extras = intent.getExtras();
      String procedure = extras.getString("procedure");
      String[] jsonArgs = extras.getStringArray("args");
      int argsLen = jsonArgs.length;

      Object[] deserialized = new Object[argsLen];
      for (int i = 0; i < argsLen; i++) {
        try {
          deserialized[i] = JsonUtil.getObjectFromJson(jsonArgs[i], true);
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
      }
      Log.d(TAG, "UI Procedure Req: " + procedure + ", args: " + Arrays.toString(deserialized));
      try {
        startProcedureInvoke("p$" + procedure, deserialized);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
  };

  private static void startProcedureInvoke(String procedureName, Object[] args) throws Throwable {
    Form form = Form.getActiveForm();
    Field field = form.getClass().getField(VARS_FIELD_NAME);
    LList variables = (LList) field.get(form);

    ModuleMethod method = null;
    for (Object variable : variables) {
      if (LList.Empty.equals(variable)) {
        continue;
      }
      LList asPair = (LList) variable;
      String name = ((Symbol) asPair.get(0)).getName();
      if (name.equals(procedureName)) {
        method = (ModuleMethod) ((ModuleMethod) asPair.get(1)).apply0();
        break;
      }
    }
    if (method == null) {
      throw new YailRuntimeError("Could not find procedure '" + procedureName + "'", TAG);
    }
    method.applyN(args);
  }

  public static void register() {
    Form form = Form.getActiveForm();
    IntentFilter filter = new IntentFilter(ACTION);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // with flag "receiver isn't exported" for API levels 26 onwards
      form.registerReceiver(RECEIVER, filter, 4);
    } else {
      form.registerReceiver(RECEIVER, filter);
    }
  }

  public static void unregister() {
    Form.getActiveForm().unregisterReceiver(RECEIVER);
  }
}
