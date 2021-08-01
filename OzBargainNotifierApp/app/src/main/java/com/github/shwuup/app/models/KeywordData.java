package com.github.shwuup.app.models;

import java.util.List;

public class KeywordData {
  private String token;
  private List<String> keywords;

  public KeywordData(String newToken, List<String> newKeywords) {
    token = newToken;
    keywords = newKeywords;
  }
}
