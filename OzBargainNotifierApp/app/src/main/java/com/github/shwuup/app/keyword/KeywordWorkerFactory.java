package com.github.shwuup.app.keyword;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

public class KeywordWorkerFactory extends WorkerFactory {
  KeywordApiManager keywordApiManager;

  public KeywordWorkerFactory(KeywordApiManager keywordApiManager) {
    this.keywordApiManager = keywordApiManager;
  }

  @Nullable
  @org.jetbrains.annotations.Nullable
  @Override
  public ListenableWorker createWorker(
      @NonNull @NotNull Context appContext,
      @NonNull @NotNull String workerClassName,
      @NonNull @NotNull WorkerParameters workerParameters) {
    if (workerClassName.equals(KeywordWorker.class.getName())) {
      return new KeywordWorker(appContext, workerParameters, keywordApiManager);
    } else {
      return null;
    }
  }
}
