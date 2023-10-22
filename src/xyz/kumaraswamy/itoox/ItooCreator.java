package xyz.kumaraswamy.itoox;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.Texting;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.youngandroid.runtime;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.expr.ModuleMethod;
import gnu.mapping.CallContext;
import gnu.mapping.SimpleEnvironment;
import gnu.mapping.Symbol;
import gnu.math.IntNum;
import kawa.standard.Scheme;
import xyz.kumaraswamy.itoox.InstanceForm.FormX;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ItooCreator {

  private static final String TAG = "ItooCreator";

  public final Context context;
  public final String refScreen;
  public final boolean appOpen;
  private Timer timer;

  public EnvironmentX envX;
  private InstanceForm formInst = null;

  private final HashMap<String, Component> components = new HashMap<>();

  private final Form activeForm;
  private IntInvoke intIvk;
  private ItooInt ints;

  private boolean isStopped = false;

  private static final AtomicReference<ItooCreator> activeCreator = new AtomicReference<>();

  synchronized public static void lifecycleOnStop() {
    if (activeCreator.get() == null) {
      Log.d(TAG, "lifecycleOnStop Found Null Returning");
      return;
    }
    activeCreator.get().onAppStopped();
  }

  public InstanceForm.Listener listener = new InstanceForm.Listener() {
    @Override
    public void event(Component component, String componentName, String eventName, Object... args) {
      Log.d(TAG, "Event Default Triggered");
    }
  };

  public interface EndListener {
    void onEnd();
  }

  private final List<EndListener> endListeners = new ArrayList<>();

  public ItooCreator(Context context, String procedure, String refScreen, boolean runIfActive)
      throws Throwable {
    this(-1, context, procedure, refScreen, runIfActive);
  }

  public ItooCreator(final int jobId, Context context, final String procedure, String refScreen, boolean runIfActive)
      throws Throwable {
    this.context = context;
    this.refScreen = refScreen;

    Log.d(TAG, "Itoo Creator, name = " + procedure + ", ref screen = " + refScreen + " runIfActive = " + runIfActive);

    activeForm = Form.getActiveForm();
    Log.d(TAG, "ItooCreator: active form = " + activeForm);
    if (activeForm instanceof FormX) {
      appOpen = false;
    } else {
      appOpen = activeForm != null;
    }
    Log.d(TAG, "ItooCreator: is the app active " + appOpen);

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
      if (ints == null) {
        Log.d(TAG, "Initializing Ints");
        initializeIntVars();
      }
      Log.d(TAG, "ItooCreator: app ref instance " + Class.forName(
              ints.getScreenPkgName(refScreen)).getConstructor().newInstance());
      boolean typeNormal = true;
      YailDictionary config = (YailDictionary) startProcedureInvoke("itoo_config");
      if (config != null) {
        Log.d(TAG, "ItooCreator: Config = " + config);
        typeNormal = (boolean) config.get("type");
      }
      if (!typeNormal) {
        Log.d(TAG, "ItooCreator: multiple invocations");
        timer = new Timer();
        timer.schedule(new TimerTask() {
          @Override
          public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                try {
                  startProcedureInvoke(procedure, jobId);
                } catch (Throwable e) {
                  e.printStackTrace();
                }
              }
            });
          }
        }, 0, ((IntNum) config.get("ftimer")).longValue());
      } else {
        Log.d(TAG, "ItooCreator: normal invocations");
        startProcedureInvoke(procedure, jobId);
      }
    } else {
      Log.i(TAG, "Reject Initialization");
    }
  }

  public void addEndListener(EndListener listener) {
    endListeners.add(listener);
  }

  public void removeEndListener(EndListener listener) {
    endListeners.remove(listener);
  }

  private void addIntsToEnvironment() throws Exception {
    initializeIntVars();

    Map<String, ?> integers = ints.getAll();
    for (Map.Entry<String, ?> key : integers.entrySet()) {
      Integer value = (Integer) key.getValue();
      Log.d(TAG, "addIntsToEnvironment: add int (" + key.getKey() + ", " + value + ")");
      // todo test thus on 10-6-22
      formInst.formX.symbols.put(ItooInt.PROCEDURE_PREFIX + key.getKey()
              , new IntBody(value, 0));
    }
  }

  private void initializeIntVars() throws Exception {
    Form form = appOpen ? activeForm : formInst.formX;
    ints = new ItooInt(form, refScreen);
    intIvk = new IntInvoke();
  }

  public Component getInstance(String pkgName) throws Exception {
    return envX.getComponent(pkgName, ints.getPackageNameOf(pkgName));
  }

  public Object startProcedureInvoke(String procName, Object... args) throws Throwable{
    int _int = ints.getInt(procName);
    Log.d(TAG, "startProcedureInvoke: " + procName + " & " + _int);
    if (_int == -1) {
      Log.d(TAG, "startProcedureInvoke: failed to find name(" + procName + ")");
      return null;
    }
    return intIvk.intInvoke(_int, args);
  }

  @SuppressWarnings("unused")
  public void invokeInt(int _int, Object... args) throws Throwable {
    intIvk.intInvoke(_int, args);
  }

  @SuppressWarnings("unused")
  public void flagEnd() throws Exception {
    if (isStopped) {
      // don't do it again if we are already stopped
      return;
    }
    // when the service is being stopped by the Android system
    // we need to be quicker
    isStopped = true;
    Log.d(TAG, "flagEnd() called");
    if (timer != null) {
      timer.cancel();
    }
    for (EndListener endListener : endListeners) {
      endListener.onEnd();
    }
    for (Component component : components.values()) {
      callSilently(component, "signalEnd");
      callSilently(component, "onPause");
      callSilently(component, "onDestroy");
    }
    Language.setCurrentLanguage(null);
    activeFieldModification(false);
  }

  // called the extension implementing the
  // itoo framework

  public void onAppStopped() {
    Log.d(TAG, "Creator received onPause()");
    Log.d(TAG, "Texting Running = " + Texting.isRunning());
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
      String className = ints.getScreenPkgName(refScreen) + "$frame";
      Log.d(TAG, "IntInvoke: the attempt class name: " + className);
      Class<?> clazz = Class.forName(className);
      frameX = (ModuleBody) clazz.getConstructor().newInstance();
    }


    public Object intInvoke(int _int) throws Throwable {
      return intInvoke(_int, new Object[0]);
    }

    private Object intInvoke(int _int, Object... args) throws Throwable {
      IntBody slex = new IntBody(_int, args.length);
      return applySlex(slex, args);
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

    float deviceDensity = context.getResources().getDisplayMetrics().density;
    set("deviceDensity", deviceDensity);
    set("formWidth", (int)((float) context.getResources().getDisplayMetrics().widthPixels / deviceDensity));
    set("formHeight", (int)((float) context.getResources().getDisplayMetrics().heightPixels / deviceDensity));
    return formInst.formX;
  }

  private void set(String name, Object value) throws Exception {
    Field field = Form.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(formInst.formX, value);
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
      Component component = name.equals(refScreen) ? formInst.formX : getComponent(name, ints.getPackageNameOf(name));

      put(symbol, component);
      names.put(component, name);
      components.put(name, component);
    }

    private Component getComponent(String name, String packageNameOf) throws Exception {
      Log.d(TAG, "Create component = " + name + " = " + packageNameOf);
      Class<?> clazz;
      try {
        clazz = Class.forName(packageNameOf);
      } catch (ClassNotFoundException e) {
        Log.d(TAG, "Component Not found Name = " + packageNameOf + " realName = " + name);
        throw e;
      }
      Constructor<?> constructor;
      try {
        constructor = clazz.getConstructor(ComponentContainer.class);
      } catch (NoSuchMethodException e) {
        constructor = clazz.getConstructor(Form.class);
      }
      return (Component) constructor.newInstance(formInstance());
    }
  }
}
