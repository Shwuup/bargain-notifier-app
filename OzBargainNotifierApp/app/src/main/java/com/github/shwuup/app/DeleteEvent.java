package com.github.shwuup.app;

import com.github.shwuup.app.models.Event;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;

public class DeleteEvent {
  private Consumer<String> doOnDelete;
  private Observable<Event> deleteStream;

  public DeleteEvent(Observable<Event> deleteStream, Consumer<String> doOnDelete) {
    this.doOnDelete = doOnDelete;
    this.deleteStream = deleteStream;
  }

  public Observable<Object> getDeleteStream() {
    return deleteStream.flatMap(
        event -> {
          doOnDelete.accept(event.metadata);
          return Observable.just(event);
        });
  }
}
