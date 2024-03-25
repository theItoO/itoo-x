package xyz.kumaraswamy.itoox.emulation;

import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import gnu.kawa.functions.Apply;
import gnu.kawa.functions.ApplyToArgs;
import gnu.mapping.*;
import kawa.standard.Scheme;
import xyz.kumaraswamy.itoox.emulation.environment.EventComponentInterceptionEnvironment;
import xyz.kumaraswamy.itoox.emulation.interceptor.ApplyArgsInterceptor;
import xyz.kumaraswamy.itoox.emulation.interceptor.BlockInterceptorContext;
import xyz.kumaraswamy.itoox.reflective.Reflective;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Emulator {

  public static final String TAG = "ItooEmulation";

  public static List<String> IGNORE_VIRTUALIZATION = new ArrayList<>();

  private static Emulator EMULATOR_INSTANCE = null;

  public static Emulator getInstance() throws NoSuchFieldException, IllegalAccessException {
    if (EMULATOR_INSTANCE == null) {
      EMULATOR_INSTANCE = new Emulator();
    }
    return EMULATOR_INSTANCE;
  }

  private boolean virtualize = false;

  private EventComponentInterceptionEnvironment modifiedFormEnvironment;

  private final Map<String, Component> components = new HashMap<>();
  private final Map<Component, String> componentNames = new HashMap<>();

  private final Map<String, Component> virtualComponents = new HashMap<>();
  private final Map<String, String> registeredEvents = new HashMap<>();

  private final Form form;

  private Emulator() throws NoSuchFieldException, IllegalAccessException {
    form = Form.getActiveForm();

    CallContext.setInstance(new BlockInterceptorContext(this, CallContext.getInstance()));

    applySpecial();
    modifyFormEnvironment();

    loadComponents();
  }

  public void virtualize(String procedure) {
    virtualize = true;
    execute(procedure);
  }

  public void disableVirtualization() {
    virtualize = false;
  }

  private void execute(String procedureName) {
    Scheme lang = Scheme.getInstance();
    try {
      // Since we're in the REPL, we can cheat and invoke the Scheme interpreter to get the method.
      Object result = lang.eval("(begin (require <com.google.youngandroid.runtime>)(get-var p$" +
          procedureName + "))");
      if (result instanceof ProcedureN) {
        ProcedureN procedureN =  (ProcedureN) result;
        // all of Itoo's procedure accepts one input argument
        procedureN.apply1("Itoo Emulation");
        return;
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    throw new RuntimeException("Unable to find procedure " + procedureName);
  }

  private void loadComponents() {
    LocationEnumeration enumeration = modifiedFormEnvironment.enumerateAllLocations();
    while (enumeration.hasNext()) {
      Location next = enumeration.next();
      if (next instanceof PlainLocation) {
        PlainLocation location = (PlainLocation) next;

        String componentName = location.getKey().toString();
        Object value = location.get(null);

        if (value instanceof Component) {
          addComponent(componentName, (Component) value);
        }
      }
    }
  }

  public Component getComponent(String componentName) {
    return components.get(componentName);
  }

  public String getComponentName(Component component) {
    return componentNames.get(component);
  }

  public void addComponent(String componentName, Component value) {
    components.put(componentName, value);
    componentNames.put(value, componentName);
  }

  public Component virtualize(String packageName, String componentName) throws ReflectiveOperationException {
    if (!virtualize || IGNORE_VIRTUALIZATION.contains(packageName)) {
      // we don't need to act, simply return the real components
      return getComponent(componentName);
    }
    Component component = virtualComponents.get(componentName);
    if (component == null) {
      component = Reflective.componentInstance(form, packageName);
      if (component == null) {
        throw new RuntimeException("Unable to virtualize " + componentName);
      }
      virtualComponents.put(componentName, component);
      Log.d(TAG, "Virtualized " + componentName + " " + component);
    }
    return component;
  }

  public void registerEvent(String event, String procedure) {
    registeredEvents.put(event, procedure);
  }

  private void applySpecial() throws NoSuchFieldException, IllegalAccessException {
    Apply apply = Scheme.apply;

    Field applyToArgsField = apply.getClass().getDeclaredField("applyToArgs");
    applyToArgsField.setAccessible(true);

    ApplyToArgs applyToArgs = (ApplyToArgs) applyToArgsField.get(apply);
    ApplyArgsInterceptor interceptor = new ApplyArgsInterceptor(this,
        applyToArgs,
        "apply-to-args",
        Scheme.instance);

    applyToArgsField.set(apply, interceptor);
  }

  private void modifyFormEnvironment() throws NoSuchFieldException, IllegalAccessException {
    Field formField = form.getClass().getField("form$Mnenvironment");
    Environment environment = (Environment) formField.get(form);

    modifiedFormEnvironment = new EventComponentInterceptionEnvironment(environment, this);
    formField.set(form, modifiedFormEnvironment);
  }
}
