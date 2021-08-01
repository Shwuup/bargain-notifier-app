package com.github.shwuup.app.util;

import com.github.shwuup.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
  private static final String BASE_URL = BuildConfig.BASE_URL;

  private static final Retrofit.Builder builder =
      new Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(GsonConverterFactory.create())
          .addCallAdapterFactory(RxJava3CallAdapterFactory.create());

  private static Retrofit retrofit = builder.build();

  private static final HttpLoggingInterceptor logging =
      new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

  private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

  public static <S> S createService(Class<S> serviceClass) {
    if (!httpClient.interceptors().contains(logging)) {
      httpClient.addInterceptor(logging);
      builder.client(httpClient.build());
      retrofit = builder.build();
    }
    return retrofit.create(serviceClass);
  }
}
