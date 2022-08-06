package xyz.kumaraswamy.itoox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;

@SuppressWarnings("unused")
public class ItooA extends AndroidNonvisibleComponent {

  private static final String TAG = "ItooCreator";

  public ItooA(ComponentContainer container) throws Throwable {
    super(container.$form());
    /*
        {
          Initializer
        }
     */
    ItooInt.saveIntStuff(form,
        form.getClass().getSimpleName());
  }

  public static class ItooAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "onReceive()");
      String name = intent.getStringExtra("name");
      String screenName = intent.getStringExtra("screen_name");
      boolean runIfActive = intent.getBooleanExtra("runIfActive", true);

      try {
        new ItooCreator(context, name, screenName, runIfActive);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  public static void alarmEveryDay(Form form, int alarmId, long triggerAtMillis, int intervalMillis, String procedure, boolean runIfActive) {
    AlarmManager manager = (AlarmManager) form.getSystemService(Context.ALARM_SERVICE);

    Intent intent = new Intent(form, ItooAlarm.class);
    intent.putExtra("name", procedure);
    intent.putExtra("screen_name", screenNameOf(form));
    intent.putExtra("runIfActive", runIfActive);

    PendingIntent pd = PendingIntent.getBroadcast(form, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pd);
    Log.d(TAG, "alarmEveryDay: alarm set " + triggerAtMillis);
  }

  public static void startItoOJob(Form form, long latency, int jobId, String procedure, boolean runIfActive) {
    ComponentName name = new ComponentName(form, ItooCreatorService.class);

    PersistableBundle bundle = new PersistableBundle();
    bundle.putString("name", procedure);
    bundle.putString("refScreen", screenNameOf(form));
    bundle.putBoolean("runIfActive", runIfActive);

    JobInfo.Builder builder = new JobInfo.Builder(jobId, name)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .setMinimumLatency(latency)
            .setExtras(bundle);
    JobScheduler scheduler = (JobScheduler) form.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    scheduler.schedule(builder.build());
  }

  public static void cancelJob(Form form, int jobId) {
    JobScheduler scheduler = (JobScheduler) form.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    scheduler.cancel(jobId);
  }

  public static void cancelAlarm(Form form, int alarmId) {
    AlarmManager alarmManager = (AlarmManager) form.getSystemService(Context.ALARM_SERVICE);
    Intent myIntent = new Intent(form, ItooAlarm.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            form, alarmId, myIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);

    alarmManager.cancel(pendingIntent);
  }

  public static String screenNameOf(Form form) {
    if (form instanceof InstanceForm.FormX) {
      InstanceForm.FormX formX = (InstanceForm.FormX) form;
      return formX.creator.refScreen;
    }
    return form.getClass().getSimpleName();
  }
}
