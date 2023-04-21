package xyz.kumaraswamy.itoox;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ItooLifecycle implements Application.ActivityLifecycleCallbacks {

  public interface ItooAppDestroyed {
    void destroyed();
  }

  private final ItooAppDestroyed destroyed;

  public ItooLifecycle(ItooAppDestroyed destroyed) {
    this.destroyed = destroyed;
  }

  @Override
  public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

  }

  @Override
  public void onActivityStarted(@NonNull Activity activity) {

  }

  @Override
  public void onActivityResumed(@NonNull Activity activity) {

  }

  @Override
  public void onActivityPaused(@NonNull Activity activity) {

  }

  @Override
  public void onActivityStopped(@NonNull Activity activity) {

  }

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

  }

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {
    destroyed.destroyed();
  }
}
