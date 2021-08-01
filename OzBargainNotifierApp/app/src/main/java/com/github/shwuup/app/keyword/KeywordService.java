package com.github.shwuup.app.keyword;

import com.github.shwuup.app.models.KeywordData;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface KeywordService {
  @Headers("Content-Type: application/json; charset=utf8")
  @POST("keywords")
  Single<Response<ResponseBody>> updateKeywords(@Body KeywordData keywordData);
}
