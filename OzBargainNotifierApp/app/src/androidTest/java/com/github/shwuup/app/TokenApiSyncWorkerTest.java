package com.github.shwuup.app;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.Data;
import androidx.work.DelegatingWorkerFactory;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestListenableWorkerBuilder;


import com.github.shwuup.app.token.TokenApiSyncWorker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(JUnit4.class)
public class TokenApiSyncWorkerTest {
    private Context context;


    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testSleepWorker_retry() {
        DelegatingWorkerFactory myWorkerFactory = MainApplication.createApiSyncWorkerFactory();
        Data inputData = new Data.Builder()
                .putString("Token", "test_token")
                .build();

        TokenApiSyncWorker worker = TestListenableWorkerBuilder.from(context, TokenApiSyncWorker.class)
                .setWorkerFactory(myWorkerFactory)
                .setInputData(inputData)
                .build();
        worker.createWork().subscribe(result -> assertThat(result, is(ListenableWorker.Result.retry())));
    }
}
