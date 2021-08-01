package com.github.shwuup.app.models;

public class Token {
  private final String token;
  private String oldToken;

  public Token(String token) {
    this.token = token;
  }

  public Token(String token, String oldToken) {
    this.token = token;
    this.oldToken = oldToken;
  }
}
