package xyz.kumaraswamy.itoox;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.appinventor.components.runtime.Form;

import java.lang.reflect.Field;

/**
 * Itoo Activity fixer
 * sometimes when we use foreground service, it makes
 * the Itoo think that the app is still active, to fix this issue
 * we need to figure out when the application gets stopped, and act
 * accordingly to prevent any error
 */
public class ItooActivityFixer implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "ItooActivityFixer";

    public interface ItooDestroyListener {
        void onDestroy();
    }

    private static ItooActivityFixer fixer;

    public static void registerItooDestroyListener(ItooDestroyListener listener, Form form) {
        Application application = form.getApplication();
        application.registerActivityLifecycleCallbacks(fixer = new ItooActivityFixer(application, listener, false));
    }

    public static void fixItooListener(Form form) {
        Application application = form.getApplication();
        application.registerActivityLifecycleCallbacks(fixer = new ItooActivityFixer(application, null, true));
    }

    private final Application application;
    private final ItooDestroyListener listener;
    private final boolean resetForm;

    public ItooActivityFixer(Application application, ItooDestroyListener listener, boolean resetForm) {
        this.application = application;
        // keep that private so other classes
        // cant access that directly
        this.listener = listener;
        this.resetForm = resetForm;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.d(TAG, "onActivityDestroyed()");
        if (resetForm) {
            try {
                activeFieldModification();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Unable to modify form");
            }
        }
        if (listener != null) listener.onDestroy();
        // we dont need the listener anymore
        // since all the activities (app) is destroyed
        application.unregisterActivityLifecycleCallbacks(this);
    }

    private void activeFieldModification() throws Exception {
        Field field = Form.class.getDeclaredField("activeForm");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        // we don't need to handle that either
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // we don't need to handle that
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // we don't need to handle that either
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // we don't need to handle that either
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // we don't need to handle that either
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        // we don't need to handle that either
    }
}
