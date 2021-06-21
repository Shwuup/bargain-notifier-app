package com.github.shwuup.app.token;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

public class TokenWorkerFactory extends WorkerFactory {
    TokenApiManager tokenApiManager;

    public TokenWorkerFactory(TokenApiManager tokenApiManager) {
        this.tokenApiManager = tokenApiManager;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public ListenableWorker createWorker(@NonNull @NotNull Context appContext, @NonNull @NotNull String workerClassName, @NonNull @NotNull WorkerParameters workerParameters) {
        if (workerClassName.equals(CreateTokenApiWorker.class.getName())) {
            return new CreateTokenApiWorker(appContext, workerParameters, tokenApiManager);
        } else if (workerClassName.equals(UpdateTokenApiWorker.class.getName())) {
            return new UpdateTokenApiWorker(appContext, workerParameters, tokenApiManager);
        }
        else {
            return null;
        }
    }
}
