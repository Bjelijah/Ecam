package com.howell.pushlibrary;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Android 5.0+ 使用的 JobScheduler.
 * 运行在 :watch 子进程中.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!DaemonEnv.sInitialized) return false;
        Log.i("547","job scheduler service on start job");
        try {startService(new Intent(DaemonEnv.sApp, DaemonEnv.sServiceClass));} catch (Exception ignored) {}
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
