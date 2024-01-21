// Copyright (C) 2023 Kumaraswamy B G
// GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
// See LICENSE for full details

package xyz.kumaraswamy.itoox;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.collect.Sets;
import com.google.appinventor.components.runtime.util.BulkPermissionRequest;
import gnu.mapping.SimpleEnvironment;
import gnu.mapping.Symbol;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.kumaraswamy.itoox.ItooCreator.EnvironmentX;

public class InstanceForm {

  private static final String TAG = "ItooCreator";

  public FormX formX;
  private final ActivityX activityX;

  public InstanceForm(ItooCreator creator) throws Exception {
    String screenName = creator.refScreen;
    Context baseContext = creator.context;
    EnvironmentX envX = creator.envX;

    formX = new FormX();
    formX.attach(baseContext);

    activityX = new ActivityX();
    activityX.attach(baseContext, screenName);
    fieldModification(baseContext);
    formX.form$Mnenvironment = envX;
    formX.baseLinearLayout = new LinearLayout(formX, 0);
  }

  private void fieldModification(Context context) throws Exception {
    final String packageName = context.getPackageName();

    // the field names that should be changed
    final String[] fieldNames = {
        "mWindow", "mComponent", "mWindowManager"
    };
    // the values for them
    final Object[] fieldNewValues = {
        new Dialog(context).getWindow(),
        new ComponentName(packageName, packageName + ".Screen1"),
        context.getSystemService(Context.WINDOW_SERVICE)
    };
    for (int i = 0; i < fieldNames.length; i++) {
      modify(fieldNames[i], fieldNewValues[i]);
    }
  }

  private void modify(String name, Object value) throws Exception {
    Field field = activityX.getClass()
        .getSuperclass().getDeclaredField(name);
    field.setAccessible(true);

    // set them to the activity
    // and the form
    field.set(activityX, value);
    field.set(formX, value);
  }

  public interface Listener {
    void event(Component component, String componentName, String eventName, Object... args) throws Throwable;
  }

  @SuppressWarnings("unused")
  public static class FormX extends Form {

    public ItooCreator.EnvironmentX form$Mnenvironment;
    public ItooCreator creator;

    public LinearLayout baseLinearLayout;

    private int mRequestCode = 0;

    private final HashMap<Integer, ActivityResultListener> activityResultMap = Maps.newHashMap();
    private final HashMap<Integer, Set<ActivityResultListener>> activityResultMultiMap = Maps.newHashMap();

    public final Map<String, Object> symbols = new HashMap<String, Object>();

    public SimpleEnvironment global$Mnvar$Mnenvironment = new SimpleEnvironment() {

      @Override
      public boolean isBound(Symbol key, Object property) {
        String name = key.getName();
        boolean contains = symbols.containsKey(key.getName());
        Log.d(TAG, "get: attempt " + name + " = " + contains);
        return contains;
      }

      @Override
      public Object get(Symbol sym) {
        Log.d(TAG, "get: returning " + sym);
        return symbols.get(sym.getName());
      }
    };


    public void attach(Context baseContext) {
      attachBaseContext(baseContext);
    }

    @Override
    public void $add(AndroidViewComponent component) {
      baseLinearLayout.add(component);
    }

    @Override
    public boolean canDispatchEvent(Component component, String eventName) {
      return true;
    }

    @Override
    public boolean dispatchEvent(Component component, String componentName, String eventName, Object[] args) {
      dispatchGenericEvent(component, eventName, false, args);
      return true;
    }

    @Override
    public void dispatchGenericEvent(Component component, String eventName, boolean notAlreadyHandled, Object[] args) {
      System.out.println(creator.listener);
      String componentName = form$Mnenvironment.toSimpleName(component);
      Log.d(TAG, "AEvent(" + eventName + "=" +
          componentName
          + ") args " + Arrays.toString(args) + " listener = " + creator.listener);
      try {
        creator.listener.event(component, componentName, eventName, args);
      } catch (Throwable e) {
        e.printStackTrace();
        Log.e(TAG, "Unable To Invoke Event '" + eventName + "'");
      }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
      Log.d(TAG, "Form ignoring startActivityForResult(" + intent + ", " + requestCode + ")");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      creator.context.startActivity(intent);

      ActivityResultListener component = activityResultMap.get(requestCode);
      if (component != null) {
        component.resultReturned(requestCode, Activity.RESULT_OK, null);
      }
      // Many components are interested in this request (e.g., Texting, PhoneCall)
      Set<ActivityResultListener> listeners = activityResultMultiMap.get(requestCode);
      if (listeners != null) {
        for (ActivityResultListener listener : listeners.toArray(new ActivityResultListener[0])) {
          listener.resultReturned(requestCode, Activity.RESULT_OK, null);
        }
      }
    }

    @Override
    public int registerForActivityResult(ActivityResultListener listener) {
      int requestCode = mRequestCode++;
      activityResultMap.put(requestCode, listener);
      return requestCode;
    }

    @Override
    public void registerForActivityResult(ActivityResultListener listener, int requestCode) {
      Set<ActivityResultListener> listeners = activityResultMultiMap.get(requestCode);
      if (listeners == null) {
        listeners = Sets.newHashSet();
        activityResultMultiMap.put(requestCode, listeners);
      }
      listeners.add(listener);
    }

    @Override
    public void unregisterForActivityResult(ActivityResultListener listener) {
      List<Integer> keysToDelete = Lists.newArrayList();
      for (java.util.Map.Entry<Integer, ActivityResultListener> mapEntry : activityResultMap.entrySet()) {
        if (listener.equals(mapEntry.getValue())) {
          keysToDelete.add(mapEntry.getKey());
        }
      }
      for (Integer key : keysToDelete) {
        activityResultMap.remove(key);
      }

      // Remove any simulated broadcast receivers
      Iterator<Map.Entry<Integer, Set<ActivityResultListener>>> it =
          activityResultMultiMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Integer, Set<ActivityResultListener>> entry = it.next();
        entry.getValue().remove(listener);
        //noinspection SizeReplaceableByIsEmpty
        if (entry.getValue().size() == 0) {
          it.remove();
        }
      }
    }

    @Override
    public void askPermission(String permission, PermissionResultHandler responseRequestor) {
      // we are not allowed to ask for permissions in the background
      responseRequestor.HandlePermissionResponse(permission, true);
    }

    @Override
    public void askPermission(BulkPermissionRequest request) {
      // we are not allowed to ask for permissions in the background
      request.onGranted();
    }

    @Override
    public void onDestroy() {
      // prevent default behaviour
    }

    @Override
    public void onPause() {
      // prevent default behaviour
      Log.d(TAG, "onPause() ignore default behaviour.");
    }

    @Override
    public void onStop() {
      // prevent default behaviour
    }

    @Override
    public void onResume() {
      // prevent default behaviour
    }
  }

  static class ActivityX extends Activity {

    private String refScreen;

    public void attach(Context baseContext, String refScreen) {
      this.refScreen = refScreen;
      attachBaseContext(baseContext);
    }

    @Override
    public String getLocalClassName() {
      return refScreen;
    }

    @Override
    public void onCreate(Bundle bundle) {
      super.onCreate(bundle);
    }
  }
}
