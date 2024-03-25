// Copyright (C) 2023 Kumaraswamy B G
// GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
// See LICENSE for full details

package xyz.kumaraswamy.itoox;

import android.content.Context;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.Texting;
import com.google.youngandroid.runtime;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.expr.ModuleMethod;
import gnu.mapping.CallContext;
import gnu.mapping.SimpleEnvironment;
import gnu.mapping.Symbol;
import kawa.standard.Scheme;
import xyz.kumaraswamy.itoox.InstanceForm.FormX;
import xyz.kumaraswamy.itoox.reflective.Reflective;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItooCreator {

  public final Context context;
  public final String refScreen;
  public final boolean appOpen;

  public EnvironmentX envX;
  private InstanceForm formInst = null;

  private final Map<String, Component> components = new HashMap<String, Component>();

  private final Form activeForm;
  private IntInvoke intIvk;
  private ItooInt ints;

  private final Log log;

  private boolean isStopped = false;

  public InstanceForm.Listener listener = new InstanceForm.Listener() {
    @Override
    public void event(Component component, String componentName, String eventName, Object... args) {

    }
  };

  public interface EndListener {
    void onEnd();
  }

  private final List<EndListener> endListeners = new ArrayList<EndListener>();

  @Deprecated
  public ItooCreator(Context context,
                     String procedure,
                     String refScreen,
                     boolean runIfActive)
      throws Throwable {
    this(context, refScreen, runIfActive);
    startProcedureInvoke(procedure, -1);
  }

  public ItooCreator(final int jobId,
                     Context context,
                     final String procedure,
                     String refScreen,
                     boolean runIfActive) throws Throwable {
    this(context, refScreen, runIfActive);
    startProcedureInvoke(procedure, jobId);
  }

  public ItooCreator(
      Context context,
      String refScreen,
      boolean runIfActive
  ) throws Throwable {
    this.context = context;
    this.refScreen = refScreen;
    log = new Log(context);

    log.debug("Itoo Creator, ref screen: " + refScreen + ", run if active = " + runIfActive);

    activeForm = Form.getActiveForm();
    log.debug("ItooCreator: active form = " + activeForm);
    if (activeForm instanceof FormX) {
      appOpen = false;
    } else {
      appOpen = activeForm != null;
    }
    log.debug("ItooCreator: is the app active " + appOpen);

    if (!appOpen) {
      envX = new EnvironmentX();
      log.debug("ItooCreator: Pass 1");
      languageInitialization();
      log.debug("ItooCreator: Pass 2");
      activeFieldModification(true);
      log.debug("ItooCreator: Pass 3");
      runtimeInitialization();
      log.debug("ItooCreator: Pass 4");
      addIntsToEnvironment();
      log.debug("ItooCreator: Pass 5");
      // warning: may break the project
      log.debug("ItooCreator: theme set");
      context.setTheme(2131427488);
    }
    if (!appOpen || runIfActive) {
      if (ints == null) {
        log.debug("Initializing Ints");
        initializeIntVars();
      }
      log.debug("ItooCreator: app ref instance " + Class.forName(
          ints.getScreenPkgName(refScreen)).getConstructor().newInstance());
    } else {
      log.debug("Reject Initialization");
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
      log.info("addIntsToEnvironment: add int (" + key.getKey() + ", " + value + ")");
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

  public String getComponentName(Component component) {
    return envX.toSimpleName(component);
  }

  public Object startProcedureInvoke(String procName, Object... args) throws Throwable {
    int _int = ints.getInt(procName);
    log.info("startProcedureInvoke: " + procName + " & " + _int);
    if (_int == -1) {
      log.info("startProcedureInvoke: failed to find name(" + procName + ")");
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
    log.debug("flagEnd() called");
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
    log.debug("Creator received onPause()");
    log.debug("Texting Running = " + Texting.isRunning());
  }

  private void callSilently(Component component, String name) {
    try {
      Method method = component.getClass().getMethod(name);
      method.invoke(component);
    } catch (Exception e) {
      // simply ignore the exception
    }
  }

  class IntInvoke {

    private final ModuleBody frameX;

    public IntInvoke() throws Exception {
      String className = ints.getScreenPkgName(refScreen) + "$frame";
      log.debug("IntInvoke: the attempt class name: " + className);
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
      log.info("applySlex: " + method + " " + Arrays.toString(args));
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
      log.info("applyN: with args(" + Arrays.toString(args) + ")");
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
    formInst = new InstanceForm(this, log);
    formInst.formX.creator = this;

    float deviceDensity = context.getResources().getDisplayMetrics().density;
    set("deviceDensity", deviceDensity);
    set("formWidth", (int) ((float) context.getResources().getDisplayMetrics().widthPixels / deviceDensity));
    set("formHeight", (int) ((float) context.getResources().getDisplayMetrics().heightPixels / deviceDensity));
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
      log.debug("Language == null");
    }
    Language.setCurrentLanguage(language);
    activeFieldModification(false);
  }

  public class EnvironmentX extends SimpleEnvironment {

    public final Map<Component, String> names = new HashMap<Component, String>();

    public String toSimpleName(Component component) {
      return names.get(component);
    }

    @Override
    public boolean isBound(Symbol key, Object property) {
      log.info("isBound: " + key.getName());
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
      Component component = name.equals(refScreen)
              ? formInst.formX
              : Reflective.componentInstance(formInstance(), ints.getPackageNameOf(name));

      put(symbol, component);
      names.put(component, name);
      components.put(name, component);
    }
  }
}
