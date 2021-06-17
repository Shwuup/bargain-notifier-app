package com.github.shwuup.app;

import android.app.Application;

import androidx.work.Configuration;
import androidx.work.DelegatingWorkerFactory;

import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;

public class MainApplication extends Application implements Configuration.Provider {

    public static DelegatingWorkerFactory createApiSyncWorkerFactory() {
        TokenApiService service = ServiceGenerator.createService(TokenApiService.class);
        TokenApiManager tokenManager = new TokenApiManager(service);
        DelegatingWorkerFactory myWorkerFactory = new DelegatingWorkerFactory();
        myWorkerFactory.addFactory(new MyWorkerFactory(tokenManager));
        return myWorkerFactory;
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        DelegatingWorkerFactory myWorkerFactory = createApiSyncWorkerFactory();
        return new Configuration.Builder()
                .setWorkerFactory(myWorkerFactory)
                .build();
    }


}
