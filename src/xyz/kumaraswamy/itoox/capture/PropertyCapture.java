// Copyright (C) 2023 Kumaraswamy B G
// GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
// See LICENSE for full details

package xyz.kumaraswamy.itoox.capture;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;
import org.json.JSONException;
import xyz.kumaraswamy.itoox.InstanceForm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyCapture {

  private static final String TAG = "PropertyCapture";

  private final SharedPreferences preferences;

  public PropertyCapture(Context context) {
    preferences = context.getSharedPreferences("PropertyCaptureItoo", Context.MODE_PRIVATE);
  }

  public List<String> capture(Form form, String componentName,
                              Component component, String[] properties) throws JSONException {
    if (form instanceof InstanceForm.FormX) {
      throw new IllegalStateException("Can't capture properties in Background");
    }
    String screenName = form.getClass().getSimpleName();
    Class<?> clazz = component.getClass();

    Method[] methods = clazz.getMethods();
    Map<Object, Object> captured = new HashMap<>();

    List<String> failedCapture = new ArrayList<>();

    propLoop:
    for (String property : properties) {
      for (Method method : methods) {
        if (method.getName().equals(property)
            && method.getReturnType() != Void.class) {
          try {
            captured.put(property, method.invoke(component));
            continue propLoop;
          } catch (ReflectiveOperationException e) {
            // eehh it failed, log and send back a report
            Log.e(TAG, "Property capture for '" + property + "' for class " + clazz.getSimpleName() + " failed");
            failedCapture.add(property);
          }
        }
      }
    }
    YailDictionary dictionary = new YailDictionary(captured);
    String serialized = JsonUtil.getJsonRepresentation(dictionary);
    preferences
        .edit()
        .putString(getName(screenName, componentName, clazz), serialized)
        .apply();
    // send them back the property list that were failed to capture
    return failedCapture;
  }

  public Map<String, Object> retrieveProperties(String referenceScreen,
                                                String componentName,
                                                Class<?> clazz) throws IllegalAccessException, JSONException {
    String key = getName(referenceScreen, componentName, clazz);
    String serialized = preferences.getString(key, "");
    if (serialized.isEmpty()) {
      throw new IllegalAccessException("Serialization not found");
    }
    Log.d(TAG, "Serialized " + serialized);
    Object objectFromJson = JsonUtil.getObjectFromJson(serialized, true);
    Log.d(TAG, objectFromJson + " | " + objectFromJson.getClass());
    // this could be YAIL dictionary instead
    //noinspection unchecked
    return (Map<String, Object>) JsonUtil.getObjectFromJson(serialized, true);
  }

  public void release(String referenceScreen, String componentName, Component component)
      throws IllegalAccessException, JSONException, InvocationTargetException {
    Class<?> clazz = component.getClass();
    Map<String, Object> properties = retrieveProperties(referenceScreen, componentName, clazz);

    Method[] methods = clazz.getMethods();
    propLoop:
    for (Map.Entry<String, Object> property : properties.entrySet()) {
      String propName = property.getKey();
      Object propValue = property.getValue();
      for (Method method : methods) {
        if (method.getName().equals(propName) && method.getParameterCount() == 1) {
          method.invoke(component, propValue);
          continue propLoop;
        }
      }
    }
  }

  private String getName(String screenName, String componentName, Class<?> clazz) {
    return screenName + "$" + componentName + "$" + clazz.getName();
  }
}
