package com.github.shwuup.app.token;

import com.github.shwuup.app.models.Token;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface TokenApiService {
  @Headers("Content-Type: application/json; charset=utf8")
  @POST("token")
  Single<Response<ResponseBody>> addToken(@Body Token token);

  @Headers("Content-Type: application/json; charset=utf8")
  @PUT("token")
  Single<Response<ResponseBody>> updateToken(@Body Token token);
}
