package xyz.kumaraswamy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.YailDictionary;
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
import java.util.Timer;
import java.util.TimerTask;

import gnu.math.IntNum;
import kawa.standard.Scheme;
import org.jetbrains.annotations.NotNull;
import xyz.kumaraswamy.InstanceForm.FormX;

public class ItooCreator {

  private static final String TAG = "ItooCreator";

  private final int jobId;
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

  private String notification_title = "Itoo";
  private String notification_subtitle = "Itoo Creator";

  private static final String CHANNEL_ID = "Battery Service";

  public InstanceForm.Listener listener = new InstanceForm.Listener() {
    @Override
    public void event(Component component, String componentName, String eventName, Object... args) {
      Log.d(TAG, "Event Default Triggered");
    }
  };

  public ItooCreator(Context context, String procedure, String refScreen, boolean runIfActive)
      throws Throwable {
    this(-1, context, procedure, refScreen, runIfActive);
  }

  public ItooCreator(int jobId, Context context, String procedure, String refScreen, boolean runIfActive)
      throws Throwable {
    this.jobId = jobId;
    this.context = context;
    this.refScreen = refScreen;

    Log.d(TAG, "Itoo Creator, name = " + procedure + ", ref screen = " + refScreen + " runIfActive = " + runIfActive);

    activeForm = Form.getActiveForm();
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
        initializeIntVars();
      }
      Log.d(TAG, "ItooCreator: app ref instance " + Class.forName(
              context.getPackageName() + "." +
                      refScreen).getConstructor().newInstance());
      boolean typeNormal = true;
      YailDictionary config = (YailDictionary) startProcedureInvoke("itoo_config");
      if (config != null) {
        Log.d(TAG, "ItooCreator: Config = " + config);
        typeNormal = (boolean) config.get("type");
        if (config.containsKey("notification")) {
          YailDictionary notif_config = (YailDictionary) config.get("notification");
          notification_title = String.valueOf(notif_config.get("title"));
          notification_subtitle = String.valueOf(notif_config.get("subtitle"));
        }
      }
      if (!typeNormal) {
        Log.d(TAG, "ItooCreator: multiple invocations");
        foregroundInitialization();
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

  private void foregroundInitialization() {
    notificationChannel();
    JobService service = (JobService) context;
    service.startForeground(177723, new NotificationCompat.Builder(context,
            CHANNEL_ID) // don't forget create a notification channel first
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(notification_title)
            .setContentText(notification_subtitle)
            .build());
  }

  private void notificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel serviceChannel = new NotificationChannel(
              CHANNEL_ID,
              "Lite Service",
              NotificationManager.IMPORTANCE_HIGH
      );
      NotificationManager manager = context.getSystemService(NotificationManager.class);
      manager.createNotificationChannel(serviceChannel);
    }
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
    return envX.getComponent(pkgName);
  }

  public Object startProcedureInvoke(String procName, Object... args) throws Throwable{
    int _int = ints.getInt(procName);
    Log.d(TAG, "startProcedureInvoke: " + _int);
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
    if (timer != null) {
      timer.cancel();
    }
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
      String className = context.getPackageName() +
              "." + refScreen + "$frame";
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

      Component component = getComponent(ints.getPackageNameOf(name));

      put(symbol, component);
      names.put(component, name);
      components.put(name, component);
    }

    @NotNull
    private Component getComponent(String name) throws Exception {
      Class<?> clazz = Class.forName(name);
      Constructor<?> constructor = clazz.getConstructor(ComponentContainer.class);
      return (Component) constructor.newInstance(formInstance());
    }
  }
}
