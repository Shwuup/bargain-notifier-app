package com.github.shwuup.app.keyword;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.rxjava3.RxWorker;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class KeywordWorker extends RxWorker {
  private static final String TOKEN = "Token";

  private KeywordApiManager keywordApiManager;

  public KeywordWorker(
      @NonNull Context context,
      WorkerParameters workerParams,
      KeywordApiManager newKeywordApiManager) {
    super(context, workerParams);
    keywordApiManager = newKeywordApiManager;
  }

  @NonNull
  @NotNull
  @Override
  public Single<ListenableWorker.Result> createWork() {
    String token = getInputData().getString(TOKEN);
    Single<Response<ResponseBody>> response = keywordApiManager.updateKeywords(token);
    return response
        .flatMap(result -> Single.just(ListenableWorker.Result.success()))
        .doOnError(Timber::e)
        .onErrorReturnItem(ListenableWorker.Result.retry());
  }
}
