package com.github.shwuup.app.util;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public abstract class ApiManager {

    protected RetryWithBackoff retryManager;

    protected ApiManager() {
        retryManager = new RetryWithBackoff(3, x -> Math.toIntExact((long) Math.pow(x, 2)));
    }

    void setRetryManager(RetryWithBackoff newRetryManager) {
        retryManager = newRetryManager;
    }

    public Single<Response<ResponseBody>> handleResponseCodes(Single<Response<ResponseBody>> res) {
        return res.flatMap(response -> {
            if (response.code() == 500 || response.code() == 403) {
                return Single.error(new HttpException(response));
            } else {
                return Single.just(response);
            }
        });
    }

    public Single<Response<ResponseBody>> retryRequest(Single<Response<ResponseBody>> response) {
        return retryManager.retry(handleResponseCodes(response));
    }
}
