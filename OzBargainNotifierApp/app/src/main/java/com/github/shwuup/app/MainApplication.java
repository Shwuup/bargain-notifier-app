package com.github.shwuup.app;

import android.app.Application;

import androidx.work.Configuration;
import androidx.work.DelegatingWorkerFactory;

import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;
import com.github.shwuup.app.token.TokenWorkerFactory;
import com.github.shwuup.app.util.ServiceGenerator;

public class MainApplication extends Application implements Configuration.Provider {

    public static DelegatingWorkerFactory createApiSyncWorkerFactory(TokenApiService service) {
        TokenApiManager tokenManager = new TokenApiManager(service);
        DelegatingWorkerFactory myWorkerFactory = new DelegatingWorkerFactory();
        myWorkerFactory.addFactory(new TokenWorkerFactory(tokenManager));
        return myWorkerFactory;
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        TokenApiService service = ServiceGenerator.createService(TokenApiService.class);
        DelegatingWorkerFactory myWorkerFactory = createApiSyncWorkerFactory(service);
        return new Configuration.Builder()
                .setWorkerFactory(myWorkerFactory)
                .build();
    }


}
