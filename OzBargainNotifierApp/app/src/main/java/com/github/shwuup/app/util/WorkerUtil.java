package com.github.shwuup.app.util;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class WorkerUtil {
    public static void createRecurringWorkRequest(Data data,
                                                  Class<? extends ListenableWorker> worker,
                                                  ExistingWorkPolicy policy, Context context,
                                                  String uniqueWorkName) {
        OneTimeWorkRequest myWorkRequest =
                new OneTimeWorkRequest.Builder(worker)
                        .setInputData(data)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                3,
                                TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(context).enqueueUniqueWork(uniqueWorkName, policy, myWorkRequest);
    }
}
