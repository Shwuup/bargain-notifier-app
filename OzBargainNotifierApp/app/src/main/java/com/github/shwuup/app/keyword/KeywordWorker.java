package com.github.shwuup.app.keyword;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.rxjava3.RxWorker;

import com.github.shwuup.app.util.WorkerUtil;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class KeywordWorker extends RxWorker {
  private static final String TOKEN = "Token";

  private final KeywordApiManager keywordApiManager;

  public KeywordWorker(
      @NonNull Context context,
      @NonNull WorkerParameters workerParams,
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
    return WorkerUtil.retryWork(response);
  }
}
