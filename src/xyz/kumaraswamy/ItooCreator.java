package xyz.kumaraswamy;

import android.content.Context;
import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.youngandroid.runtime;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.expr.ModuleMethod;
import gnu.mapping.CallContext;
import gnu.mapping.SimpleEnvironment;
import gnu.mapping.Symbol;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kawa.standard.Scheme;
import xyz.kumaraswamy.InstanceForm.FormX;

public class ItooCreator {

  private static final String TAG = "ItooCreator";

  public final Context context;
  public final String refScreen;
  private final boolean appOpen;

  public EnvironmentX envX;
  private InstanceForm formInst = null;

  private final HashMap<String, Component> components = new HashMap<>();

  private final Form activeForm;
  private IntInvoke intIvk;
  private ItooInt ints;

  public InstanceForm.Listener listener = new InstanceForm.Listener() {
    @Override
    public void event(Component component, String componentName, String eventName, Object... args) {
      Log.d(TAG, "Event Default Triggered");
    }
  };

  public ItooCreator(Context context, String procedure, String refScreen, boolean runIfActive)
      throws Throwable {
    this.context = context;
    this.refScreen = refScreen;

    Log.d(TAG, "Itoo Creator, name = " + procedure);

    activeForm = Form.getActiveForm();
    if (activeForm instanceof FormX) {
      appOpen = false;
    } else {
      appOpen = activeForm != null;
    }

    if (!appOpen) {
      envX = new EnvironmentX();
      Log.d(TAG, "ItooCreator: Pass 1");
      languageInitialization();
      Log.d(TAG, "ItooCreator: Pass 2");
      activeFieldModification(true);
      Log.d(TAG, "ItooCreator: Pass 3");
      runtimeInitialization();
      Log.d(TAG, "ItooCreator: Pass 4");
      addIntsToEnvironment();
      Log.d(TAG, "ItooCreator: Pass 5");
      // warning: may break the project
      Log.d(TAG, "ItooCreator: theme set");
      context.setTheme(2131427488);
    }
    if (!appOpen || runIfActive) {
      startProcedureInvoke(procedure);
    } else {
      Log.i(TAG, "Reject Initialization");
    }
  }

  private void addIntsToEnvironment() throws Exception {
    Form form = appOpen ? activeForm : formInst.formX;
    ints = new ItooInt(form, refScreen);
    intIvk = new IntInvoke();

    Map<String, ?> integers = ints.getAll();
    for (String key : integers.keySet()) {
      Integer value = (Integer) integers.get(key);
      Log.d(TAG, "addIntsToEnvironment: add int (" + key + ", " + value + ")");
      // todo test thus on 10-6-22
      formInst.formX.symbols.put(ItooInt.PROCEDURE_PREFIX + key
              , new IntBody(value, 0));
    }
  }

  private void startProcedureInvoke(String procName) throws Throwable{
    int _int = ints.getInt(procName);
    Log.d(TAG, "startProcedureInvoke: " + _int);
    intIvk.intInvoke(_int);
  }

  @SuppressWarnings("unused")
  public void invokeInt(int _int, Object... args) throws Throwable {
    intIvk.intInvoke(_int, args);
  }

  @SuppressWarnings("unused")
  public void flagEnd() throws Exception {
    for (Component component : components.values()) {
      callSilently(component, "onPause");
      callSilently(component, "onDestroy");
    }
    Language.setCurrentLanguage(null);
    activeFieldModification(false);
  }

  private void callSilently(Component component, String name) {
    try {
      Method method = component.getClass().getMethod(name);
      method.invoke(component);
    } catch (InvocationTargetException
        | IllegalAccessException | NoSuchMethodException e) {
      // simply ignore the exception
    }
  }

  class IntInvoke {

    private final ModuleBody frameX;

    public IntInvoke() throws Exception {
      Class<?> clazz = Class.forName(context.getPackageName() +
          "." + refScreen + "$frame");
      frameX = (ModuleBody) clazz.getConstructor().newInstance();
    }


    public void intInvoke(int _int) throws Throwable {
      intInvoke(_int, new Object[0]);
    }

    private void intInvoke(int _int, Object... args) throws Throwable {
      IntBody slex = new IntBody(_int, args.length);
      applySlex(slex, args);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Object applySlex(ModuleMethod method, Object... args) throws Throwable {
      switch (args.length) {
        case 0:
          return frameX.apply0(method);
        case 1:
          return frameX.apply1(method, args[0]);
        case 2:
          return frameX.apply2(method, args[0], args[1]);
        case 3:
          return frameX.apply3(method, args[0], args[1], args[2]);
        case 4:
          return frameX.apply4(method, args[0], args[1], args[2], args[3]);
        default:
          return frameX.applyN(method, args);
      }
    }
  }

  class IntBody extends ModuleMethod {

    public IntBody(int selector, int args) {
      super(null, selector, null, args);
    }

    @Override
    public Object applyN(Object[] args) throws Throwable {
      Log.d(TAG, "applyN: with args("+ Arrays.toString(args) + ")");
      return intIvk.applySlex(this, args);
    }
  }

  private void runtimeInitialization() {
    runtime runtime_ = new runtime();
    runtime_.run(new CallContext());
    runtime.setThisForm();
  }

  private void activeFieldModification(boolean init) throws Exception {
    Field field = Form.class.getDeclaredField("activeForm");
    field.setAccessible(true);

    if (init) {
      Form form = formInstance();
      field.set(null, form);
    } else {
      field.set(null, null);
    }
  }

  private Form formInstance() throws Exception {
    formInst = new InstanceForm(this);
    formInst.formX.creator = this;
    return formInst.formX;
  }

  private void languageInitialization() throws Exception {
    Language language = Scheme.getInstance();
    if (language == null) {
      Log.i(TAG, "Language = null");
    }
    Language.setCurrentLanguage(language);
    activeFieldModification(false);
  }

  public class EnvironmentX extends SimpleEnvironment {

    public final HashMap<Component, String> names = new HashMap<>();

    public String toSimpleName(Component component) {
      return names.get(component);
    }

    @Override
    public boolean isBound(Symbol key, Object property) {
      String name = key.getName();
      if (!components.containsKey(name)) {
        try {
          componentInstance(key);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return true;
      }
      return super.isBound(key, property);
    }

    private void componentInstance(Symbol symbol)
        throws Exception {
      String name = symbol.getName();

      Class<?> clazz = Class.forName(ints.getPackageNameOf(name));
      Constructor<?> constructor = clazz.getConstructor(ComponentContainer.class);
      Component component = (Component) constructor.newInstance(formInstance());

      put(symbol, component);
      names.put(component, name);
      components.put(name, component);
    }
  }
}
