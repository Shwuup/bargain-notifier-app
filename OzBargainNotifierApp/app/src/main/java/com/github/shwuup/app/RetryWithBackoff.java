package com.github.shwuup.app;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;


public class RetryWithBackoff {
    private final int START_RETRY_TIME = 1;
    private final int STOP_RETRY_TIME;
    private Function<Integer, Integer> timeFunction;

    public RetryWithBackoff(int timesToRetry, Function<Integer, Integer> func) {
        STOP_RETRY_TIME = timesToRetry;
        timeFunction = func;
    }

    public <T> Single<T> retry(Single<T> response) {
        AtomicInteger counter = new AtomicInteger();
        return response.retryWhen(e -> {
            return e.flatMap(throwable -> {
                if (counter.getAndAdd(1) < STOP_RETRY_TIME) {
                    Timber.d("Retrying");
                    return Flowable.timer(timeFunction.apply(counter.get()), TimeUnit.SECONDS);
                } else {
                    Timber.d("Exiting");
                    return Flowable.error(throwable);
                }
            });
        });
    }

}
