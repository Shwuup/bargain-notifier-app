package com.github.shwuup.app.token;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import androidx.work.rxjava3.RxWorker;

import com.github.shwuup.R;
import com.github.shwuup.app.util.SharedPref;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class CreateTokenApiWorker extends RxWorker {
  private static final String TOKEN = "Token";
  private final TokenApiManager tokenManager;
  private Context context;

  public CreateTokenApiWorker(
      @NonNull Context context, @NonNull WorkerParameters params, TokenApiManager tokenManager) {
    super(context, params);
    this.context = context;
    this.tokenManager = tokenManager;
  }

  @NonNull
  @NotNull
  @Override
  public Single<Result> createWork() {
    String token = getInputData().getString(TOKEN);
    Single<Response<ResponseBody>> response = tokenManager.addToken(token);
    return response
        .flatMap(result -> Single.just(Result.success()))
        .doOnSuccess(
            __ ->
                SharedPref.writeString(
                    context, context.getString(R.string.preference_token_key), token))
        .doOnError(Timber::e)
        .onErrorReturnItem(Result.retry());
  }
}
