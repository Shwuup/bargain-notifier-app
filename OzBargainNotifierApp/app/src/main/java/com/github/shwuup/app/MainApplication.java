package com.github.shwuup.app;

import android.app.Application;
import android.content.Context;

import androidx.work.Configuration;
import androidx.work.DelegatingWorkerFactory;

import com.github.shwuup.R;
import com.github.shwuup.app.keyword.KeywordApiManager;
import com.github.shwuup.app.keyword.KeywordFileManager;
import com.github.shwuup.app.keyword.KeywordService;
import com.github.shwuup.app.keyword.KeywordWorkerFactory;
import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;
import com.github.shwuup.app.token.TokenWorkerFactory;
import com.github.shwuup.app.util.ServiceGenerator;
import com.github.shwuup.app.util.SharedPref;

public class MainApplication extends Application implements Configuration.Provider {

  protected static DelegatingWorkerFactory addApiSyncFactory(
      DelegatingWorkerFactory workerFactory, TokenApiService service) {
    TokenApiManager tokenManager = new TokenApiManager(service);
    workerFactory.addFactory(new TokenWorkerFactory(tokenManager));
    return workerFactory;
  }

  protected static DelegatingWorkerFactory addKeywordFactory(
      DelegatingWorkerFactory workerFactory, Context context, String token) {
    KeywordFileManager keywordFileManager = new KeywordFileManager(context);
    KeywordApiManager keywordApiManager =
        new KeywordApiManager(
            ServiceGenerator.createService(KeywordService.class), keywordFileManager, token);
    workerFactory.addFactory(new KeywordWorkerFactory(keywordApiManager));
    return workerFactory;
  }

  @Override
  public Configuration getWorkManagerConfiguration() {
    TokenApiService service = ServiceGenerator.createService(TokenApiService.class);
    DelegatingWorkerFactory myWorkerFactory =
        addKeywordFactory(
            addApiSyncFactory(new DelegatingWorkerFactory(), service),
            getApplicationContext(),
            SharedPref.readString(this, this.getString(R.string.preference_token_key)));
    return new Configuration.Builder().setWorkerFactory(myWorkerFactory).build();
  }
}
