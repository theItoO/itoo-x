package xyz.kumaraswamy.itoox.emulation.interceptor;

import android.util.Log;
import gnu.expr.ModuleMethod;
import gnu.mapping.MethodProc;
import xyz.kumaraswamy.itoox.emulation.Emulator;

import java.util.Arrays;

import static xyz.kumaraswamy.itoox.emulation.Emulator.TAG;

public class EventInterceptorModule extends ModuleMethod {

  private final Emulator emulator;

  private final MethodProc callback;
  private final String componentName;
  private final String eventName;

  public EventInterceptorModule(Emulator emulator, MethodProc callback, String componentName, String eventName) {
    super(null, -1, "", 0);
    this.emulator = emulator;
    this.callback = callback;
    this.componentName = componentName;
    this.eventName = eventName;
  }

  @Override
  public Object applyN(Object[] args) throws Throwable {
    Object result = callback.applyN(args);
    Log.d(TAG, "Event " + componentName + "." + eventName + " " + Arrays.toString(args) + " result " + result);
    return result;
  }
}
