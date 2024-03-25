package xyz.kumaraswamy.itoox.emulation.interceptor;

import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import gnu.expr.Language;
import gnu.kawa.functions.ApplyToArgs;
import gnu.kawa.reflect.Invoke;
import gnu.mapping.SimpleSymbol;
import xyz.kumaraswamy.itoox.emulation.Emulator;

import java.util.Arrays;

import static xyz.kumaraswamy.itoox.emulation.Emulator.TAG;

public class ApplyArgsInterceptor extends ApplyToArgs {

  public static boolean skipTrace = false;

  private final Emulator emulator;
  private final ApplyToArgs callback;

  public ApplyArgsInterceptor(Emulator emulator, ApplyToArgs callback, String name, Language language) {
    super(name, language);
    this.emulator = emulator;
    this.callback = callback;
  }

  @Override
  public Object applyN(Object[] args) throws Throwable {
    if (args.length > 2 &&
        args[0] instanceof Invoke
        && args[1] instanceof Component
        && args[2] instanceof SimpleSymbol
    ) {
      String componentName = emulator.getComponentName((Component) args[1]);
      String blockName = args[2].toString();

      int len = args.length - 3;
      Object[] invokeArgs = new Object[len];
      System.arraycopy(args, 3, invokeArgs, 0, len);

      String packageName = args[1].getClass().getName();
      // virtualize it, in virtual mode
      args[1] = emulator.virtualize(packageName, componentName);

      skipTrace = true;
      Log.d(TAG, "Block " + componentName + "." + blockName + " " + Arrays.toString(invokeArgs));
    }
    // error tracing will be handled by @BlockInterceptorContext
    return callback.applyN(args);
  }

}
