package com.github.shwuup.app;

import com.github.shwuup.app.keyword.KeywordApiManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class MergedObservable {
  public static Observable<Response<ResponseBody>> updateKeywordsApiRequest(
      AddEvent addEvent, DeleteEvent deleteEvent, KeywordApiManager keywordApiManager) {
    return Observable.merge(addEvent.getAddStream(), deleteEvent.getDeleteStream())
        .debounce(15, TimeUnit.SECONDS)
        .switchMap(x -> keywordApiManager.updateKeywords().toObservable());
  }
}
