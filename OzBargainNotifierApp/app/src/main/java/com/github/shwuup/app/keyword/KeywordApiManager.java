package com.github.shwuup.app.keyword;

import com.github.shwuup.app.models.KeywordData;
import com.github.shwuup.app.util.ApiManager;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class KeywordApiManager extends ApiManager {
  private final KeywordService keywordService;
  private final KeywordFileManager keywordFileManager;
  private final String token;

  public KeywordApiManager(KeywordService service, KeywordFileManager newKeywordFileManager, String token) {
    super();
    keywordService = service;
    keywordFileManager = newKeywordFileManager;
    this.token = token;
  }

  public Single<Response<ResponseBody>> updateKeywords() {
    return retryRequest(
        handleResponseCodes(
            keywordService.updateKeywords(
                new KeywordData(token, keywordFileManager.readKeywords()))));
  }
}
