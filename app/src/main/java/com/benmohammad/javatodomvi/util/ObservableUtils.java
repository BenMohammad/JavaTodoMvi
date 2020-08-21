package com.benmohammad.javatodomvi.util;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class ObservableUtils {

    private ObservableUtils(){}

    public static <T> Observable<T> pairWithDelay(T immediate, T delayed) {
        return Observable.timer(2, TimeUnit.SECONDS)
                .take(1)
                .map(ignored -> delayed)
                .startWith(immediate);
    }
}
