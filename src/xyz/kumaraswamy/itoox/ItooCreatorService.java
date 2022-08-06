package xyz.kumaraswamy.itoox;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.PersistableBundle;
import android.util.Log;

public class ItooCreatorService extends JobService {

    private static final String TAG = "ItooCreator";
    private ItooCreator creator;

    @Override
    public boolean onStartJob(JobParameters parms) {
        Log.d(TAG, "onStartJob()");

        PersistableBundle extras = parms.getExtras();

        String name = extras.getString("name");
        String refScreen = extras.getString("refScreen");
        boolean runIfActive = extras.getBoolean("runIfActive");
        try {
            creator = new ItooCreator(this, name, refScreen, runIfActive);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters parms) {
        try {
            creator.flagEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
