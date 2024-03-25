package xyz.kumaraswamy.itoox.emulation.environment;

import com.google.appinventor.components.runtime.Component;

import gnu.mapping.Environment;
import gnu.mapping.Location;
import gnu.mapping.MethodProc;
import gnu.mapping.Symbol;
import xyz.kumaraswamy.itoox.emulation.Emulator;
import xyz.kumaraswamy.itoox.emulation.interceptor.EventInterceptorModule;

public class EventComponentInterceptionEnvironment extends InterceptionEnvironment {

  private final Emulator emulator;

  public EventComponentInterceptionEnvironment(Environment callback, Emulator emulator) {
    super(callback);
    this.emulator = emulator;
  }

  @Override
  public void put(Symbol key, Object property, Object newValue) {
    if (newValue instanceof Component && property == null) {
      // components added later of our extension needs to be added
      // as well
      emulator.addComponent(key.getName(), (Component) newValue);
    }
    super.put(key, property, newValue);
  }

  @Override
  public Object get(Symbol key, Object property, Object defaultValue) {
    Location lookup = lookup(key, property);
    Object module = lookup == null ? property : lookup.get(property);


    String name = key.getName();
    if (name.contains("$")) {
      String[] split = name.split("\\$");

      if (split.length == 2) {
        String componentName = split[0];
        String eventName = split[1];

        Component component = emulator.getComponent(componentName);
        String packageName = component.getClass().getName();
        try {
          component = emulator.virtualize(packageName, componentName);
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException(e);
        }
        if (component != null) {
          return new EventInterceptorModule(emulator, (MethodProc) module, componentName, eventName);
        }
      }
    }

    return super.get(key, property, defaultValue);
  }
}
