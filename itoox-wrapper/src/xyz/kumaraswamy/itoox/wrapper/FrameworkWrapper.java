package xyz.kumaraswamy.itoox.wrapper;

import android.content.Context;
import com.google.appinventor.components.runtime.Form;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FrameworkWrapper {

  private static final String FRAMEWORK_CLASS = "xyz.kumaraswamy.itoox.Framework";

  public static boolean isItooXPresent() {
    try {
      Class.forName(FRAMEWORK_CLASS);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static void activateItooX() throws ReflectiveOperationException {
    if (!isItooXPresent()) {
      return;
    }
    Form form = Form.getActiveForm();
    if (form == null) {
      return;
    }
    String refScreen = form.getClass().getSimpleName();

    Class<?> itooInt = Class.forName("xyz.kumaraswamy.itoox.ItooInt");
    Method method = itooInt.getMethod("saveIntStuff", Form.class, String.class);

    // static invoke saveIntStuff(Form, String)
    method.invoke(null, form, refScreen);
  }

  private boolean success;

  private Object framework;
  private Method callProcedureMethod;

  public FrameworkWrapper(Context context, String screen) {
    if (!isItooXPresent()) {
      success = false;
      return;
    }
    try {
      success = safeInit(context, screen);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
      success = false;
    }
  }

  private boolean safeInit(Context context, String screen) throws ReflectiveOperationException {
    final boolean success;
    Class<?> clazz = Class.forName(FRAMEWORK_CLASS);
    Object result = clazz.getMethod("get", Context.class, String.class)
        // static method invocation
        // get(Context, String)
        .invoke(null, context, screen);
    Class<?> resultClazz = result.getClass();
    // success() method
    success = (boolean) resultClazz.getMethod("success").invoke(result);
    if (success) {
      framework = resultClazz.getMethod("getFramework").invoke(result);
      callProcedureMethod = clazz.getMethod("call", String.class, Object[].class);
    }
    return success;
  }

  public boolean success() {
    return success;
  }

  public Object call(String procedure, Object... args) {
    try {
      return safeCall(procedure, args);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
      return Result.BAD;
    }
  }

  private Object safeCall(String procedure, Object[] args) throws ReflectiveOperationException {
    Object callResult = callProcedureMethod.invoke(framework, procedure, args);
    Class<?> callResultClazz = callResult.getClass();
    // success()
    boolean success = (boolean) callResultClazz.getMethod("success").invoke(callResult);
    if (!success) {
      return Result.BAD;
    }
    return callResultClazz.getMethod("get").invoke(callResult);
  }

  public boolean close() {
    try {
      return (boolean) framework.getClass().getMethod("close").invoke(framework);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
      return false;
    }
  }

  public enum Result {
    // well, we need something unique to return when
    // procedure invocation fails...
    BAD,
    SUCCESS
  }
}
