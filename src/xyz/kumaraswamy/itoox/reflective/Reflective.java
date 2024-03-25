package xyz.kumaraswamy.itoox.reflective;

import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;

import java.lang.reflect.Constructor;

public class Reflective {

  private static final String TAG = "Reflective";

  public static Component componentInstance(Form form, String pkgName) throws ReflectiveOperationException {
    Class<?> clazz;
    try {
      clazz = Class.forName(pkgName);
    } catch (ClassNotFoundException e) {
      Log.d(TAG, "Component Instance Creation failure (not found) " + e.getMessage());
      return null;
    }
    Constructor<?> constructor;
    try {
      constructor = clazz.getConstructor(ComponentContainer.class);
    } catch (NoSuchMethodException e) {
      constructor = clazz.getConstructor(Form.class);
    }
    return (Component) constructor.newInstance(form);
  }
}
