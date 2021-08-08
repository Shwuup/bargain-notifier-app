package com.github.shwuup.app.models;

import java.util.List;

public class KeywordData {
  private final String token;
  private final List<String> keywords;

  public KeywordData(String newToken, List<String> newKeywords) {
    token = newToken;
    keywords = newKeywords;
  }
}
