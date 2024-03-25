package xyz.kumaraswamy.itoox.emulation.interceptor;

import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import gnu.expr.PrimProcedure;
import gnu.mapping.CallContext;
import xyz.kumaraswamy.itoox.emulation.Emulator;

import static xyz.kumaraswamy.itoox.emulation.Emulator.TAG;

public class BlockInterceptorContext extends CallContext {

  private final Emulator emulator;

  public BlockInterceptorContext(Emulator emulator, CallContext parent) {
    super();

    this.emulator = emulator;
    copyParentFields(parent);
  }

  private void copyParentFields(CallContext parent) {
    proc = parent.proc;
    values = parent.values;

    value1 = parent.value1;
    value2 = parent.value2;
    value3 = parent.value3;
    value4 = parent.value4;

    consumer = parent.consumer;

    pc = parent.pc;
    vstack = parent.vstack;
    ivalue1 = parent.ivalue1;
    ivalue2 = parent.ivalue2;
    count = parent.count;
    next = parent.next;
    where = parent.where;
    evalFrames = parent.evalFrames;
  }

  @Override
  public int getArgCount() {
    return super.getArgCount();
  }

  @Override
  public Object getNextArg() {
    return super.getNextArg();
  }

  @Override
  public int getNextIntArg() {
    return super.getNextIntArg();
  }

  @Override
  public Object getNextArg(Object defaultValue) {
    return super.getNextArg(defaultValue);
  }

  @Override
  public int getNextIntArg(int defaultValue) {
    return super.getNextIntArg(defaultValue);
  }

  @Override
  public void lastArg() {
    super.lastArg();
  }

  @Override
  public Object[] getArgs() {
    return super.getArgs();
  }

  @Override
  public void runUntilDone() throws Throwable {
    if (proc instanceof PrimProcedure) {
      if (value1 instanceof Component) {
        PrimProcedure procedure = (PrimProcedure) proc;
        Component component = (Component) value1;

        String componentName = emulator.getComponentName(component);
        if (componentName != null) {
          String blockName = procedure.getMethod().getName();
          Object[] args = values;

          if (!ApplyArgsInterceptor.skipTrace) {
            String packageName = component.getClass().getName();
            value1 = emulator.virtualize(packageName, componentName);

            Log.d(TAG, "Property " + componentName + "." + blockName + " " + (args.length == 0 ? "" : args[0]));
          } else {
            ApplyArgsInterceptor.skipTrace = false;
          }
        }
      }
    }
    super.runUntilDone();
  }

  @Override
  public void writeValue(Object value) {
    super.writeValue(value);
  }
}
