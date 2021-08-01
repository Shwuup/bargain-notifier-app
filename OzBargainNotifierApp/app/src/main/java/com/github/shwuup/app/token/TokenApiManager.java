package com.github.shwuup.app.token;

import com.github.shwuup.app.models.Token;
import com.github.shwuup.app.util.ApiManager;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class TokenApiManager extends ApiManager {

    private final TokenApiService tokenApiService;

    public TokenApiManager(TokenApiService service) {
        super();
        tokenApiService = service;
    }

    public Single<Response<ResponseBody>> addToken(String token) {
        return retryRequest(tokenApiService.addToken(new Token(token)));
    }

    public Single<Response<ResponseBody>> updateToken(String oldToken, String newToken) {
        return retryRequest(tokenApiService.updateToken(new Token(newToken, oldToken)));
    }
}
