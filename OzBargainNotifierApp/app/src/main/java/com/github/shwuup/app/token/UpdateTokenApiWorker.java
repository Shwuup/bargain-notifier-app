package com.github.shwuup.app.token;

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

public class UpdateTokenApiWorker extends RxWorker {
    private static final String TOKEN = "Token";
    private static final String OLD_TOKEN = "Old token";
    private final TokenApiManager tokenManager;


    public UpdateTokenApiWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params,
            TokenApiManager tokenManager) {
        super(context, params);
        this.tokenManager = tokenManager;
    }

    @NonNull
    @NotNull
    @Override
    public Single<Result> createWork() {
        String token = getInputData().getString(TOKEN);
        String oldToken = getInputData().getString(OLD_TOKEN);
        Single<Response<ResponseBody>> response = tokenManager.updateToken(token, oldToken);
        return response
                .flatMap(result -> Single.just(ListenableWorker.Result.success()))
                .doOnError(Timber::e)
                .onErrorResumeWith(error -> Single.just(ListenableWorker.Result.retry()));
    }
}
