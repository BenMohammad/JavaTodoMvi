package com.benmohammad.javatodomvi.mvibase;

import io.reactivex.Observable;

public interface MviView<I extends MviIntent, S extends MviViewState> {

    Observable<I> intents();

    void render(S state);
}
