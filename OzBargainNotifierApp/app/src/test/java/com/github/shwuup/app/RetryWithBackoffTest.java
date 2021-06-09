package com.github.shwuup.app;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;

import static org.junit.Assert.assertEquals;

public class RetryWithBackoffTest {
    @Test
    public void testRetry_onSuccessAfter3Tries() throws InterruptedException {
        TestObserver<Object> ts = new TestObserver<>();
        RetryWithBackoff retryManager = new RetryWithBackoff(4, x -> Math.toIntExact((long) Math.pow(x, 2)));
        AtomicInteger subscribeCounter = new AtomicInteger();
        final int EXPECTED_TRIES = 3;

        Single<Object> test = Single.create(subscriber -> {
            if (subscribeCounter.get() == EXPECTED_TRIES) {
                subscriber.onSuccess("Success");
            } else {
                subscribeCounter.addAndGet(1);
                subscriber.onError(new RuntimeException("some failure"));
            }
        });
        retryManager.retry(test).subscribe(ts);
        ts.await();
        assertEquals(EXPECTED_TRIES, subscribeCounter.get());
    }
    @Test
    public void testRetry_onErrorWhenReachMaxTries() throws InterruptedException {
        RetryWithBackoff retryManager = new RetryWithBackoff(3, x -> Math.toIntExact((long) Math.pow(x, 2)));
        TestObserver<Object> ts = new TestObserver<>();
        Single<Throwable> again = Single.create((s) -> {
            s.onError(new RuntimeException("always fails"));
        });
        retryManager.retry(again).subscribe(ts);
        ts.await();
        ts.assertError(RuntimeException.class);
    }
}
