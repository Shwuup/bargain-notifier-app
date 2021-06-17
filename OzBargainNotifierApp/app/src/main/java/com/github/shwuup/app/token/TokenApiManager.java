package com.github.shwuup.app.token;

import com.github.shwuup.app.RetryWithBackoff;
import com.github.shwuup.app.models.Token;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public class TokenApiManager {

    private final TokenApiService tokenApiService;
    private final RetryWithBackoff retryManager;

    public TokenApiManager(TokenApiService service) {
        retryManager = new RetryWithBackoff(3, x -> Math.toIntExact((long) Math.pow(x, 2)));
        tokenApiService = service;
    }

    public Single<Response<ResponseBody>> addToken(String token) {
        Single<Response<ResponseBody>> response = tokenApiService.addToken(new Token(token));
        Single<Response<ResponseBody>> t = handleResponseCodes(response);
        return retryManager.retry(t);
    }

    public Single<Response<ResponseBody>> handleResponseCodes(Single<Response<ResponseBody>> res) {
        return res.flatMap(response -> {
            if (response.code() == 500) {
                return Single.error(new HttpException(response));
            } else {
                return Single.just(response);
            }
        });
    }
}
