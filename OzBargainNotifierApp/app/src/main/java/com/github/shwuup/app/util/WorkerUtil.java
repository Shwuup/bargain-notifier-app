package com.github.shwuup.app.util;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public final class WorkerUtil {
  public static void createRecurringWorkRequest(
      Data data,
      Class<? extends ListenableWorker> worker,
      ExistingWorkPolicy policy,
      Context context,
      String uniqueWorkName) {
    OneTimeWorkRequest myWorkRequest =
        new OneTimeWorkRequest.Builder(worker)
            .setInputData(data)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 3, TimeUnit.MINUTES)
            .build();
    WorkManager.getInstance(context).enqueueUniqueWork(uniqueWorkName, policy, myWorkRequest);
  }

  public static Single<ListenableWorker.Result> retryWork(Single<Response<ResponseBody>> response) {
    return response
        .flatMap(result -> Single.just(ListenableWorker.Result.success()))
        .doOnError(Timber::e)
        .onErrorReturnItem(ListenableWorker.Result.retry());
  }
}
