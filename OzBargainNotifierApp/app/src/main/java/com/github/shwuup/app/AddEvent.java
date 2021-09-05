package com.github.shwuup.app;

import com.github.shwuup.app.models.Event;

import io.reactivex.rxjava3.core.Observable;

public class AddEvent {
  private Runnable doOnAdd;
  private Observable<Event> addStream;

  public AddEvent(Observable<Event> addStream, Runnable doOnAdd) {
    this.doOnAdd = doOnAdd;
    this.addStream = addStream;
  }

  public Observable<Event> getAddStream() {
    return addStream.flatMap(
        event -> {
          doOnAdd.run();
          return Observable.just(event);
        });
  }
}
