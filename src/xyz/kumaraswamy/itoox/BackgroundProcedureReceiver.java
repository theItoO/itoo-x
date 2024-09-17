package xyz.kumaraswamy.itoox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import com.google.appinventor.components.runtime.util.JsonUtil;
import org.json.JSONException;

import java.util.Arrays;

public class BackgroundProcedureReceiver {

  public static final String BACKGROUND_PROCEDURE_RECEIVER = "ITOO_BG_BG_PROCEDURE_RECEIVER";

  private final ItooCreator creator;

  // When U.I request the background to call a procedure
  private final BroadcastReceiver backgroundProcedureReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      // UI wants to call a procedure that's in background
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
      creator.log.info("Received Background Procedure Request: " + procedure + ", args: " + Arrays.toString(deserialized));
      try {
        creator.startProcedureInvoke(procedure, deserialized);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
  };

  public BackgroundProcedureReceiver(ItooCreator creator) {
    this.creator = creator;
    IntentFilter filter = new IntentFilter(BACKGROUND_PROCEDURE_RECEIVER);
    filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
    Context context = creator.context;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // with flag "receiver isn't exported" for API levels 26 onwards
      context.registerReceiver(backgroundProcedureReceiver, filter, 4);
    } else {
      context.registerReceiver(backgroundProcedureReceiver, filter);
    }
  }

  public void unregister() {
    creator.context.unregisterReceiver(backgroundProcedureReceiver);
  }
}
