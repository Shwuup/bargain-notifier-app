package com.github.shwuup.app.models;

public class Event {
  public String name;
  public String metadata;

  public Event(String newName, String newMetadata) {
    name = newName;
    metadata = newMetadata;
  }

  public Event(String newName) {
    name = newName;
  }
}
