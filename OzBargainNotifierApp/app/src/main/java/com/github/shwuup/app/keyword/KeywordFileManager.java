package com.github.shwuup.app.keyword;

import android.content.Context;

import com.github.shwuup.app.util.FileManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class KeywordFileManager extends FileManager {

  public KeywordFileManager(Context newContext) {
    super(newContext);
  }

  private File getKeywordFile() {
    File keywordFile = getFile("keywords");
    return keywordFile;
  }

  public boolean deleteKeywordFile() {
    File file = getKeywordFile();
    boolean deleted = file.delete();
    return deleted;
  }

  public List<String> readKeywords() {
    File file = getKeywordFile();
    String json = readFile(file);
    GsonBuilder gsonB = new GsonBuilder();
    Gson gson = gsonB.create();
    List<String> keywords = gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
    if (keywords == null) {
      return new ArrayList<>();
    } else {
      return keywords;
    }
  }

  public void deleteKeyword(String keywordToDelete) {
    List<String> keywords = readKeywords();
    keywords.removeIf(k -> k.equals(keywordToDelete));
    writeKeywords(keywords);
    Timber.d(this.readKeywords().toString());
  }

  public void writeKeywords(List<String> keywords) {
    GsonBuilder gsonB = new GsonBuilder();
    Gson gson = gsonB.create();
    File file = getKeywordFile();
    writeFile(file, gson.toJson(keywords));
  }

  private boolean keywordInList(String newKeyword, List<String> keywords) {
    for (String keyword : keywords) {
      if (newKeyword.equals(keyword)) {
        return true;
      }
    }
    return false;
  }

  public void addKeyword(String newKeyword) {
    List<String> keywords = readKeywords();
    if (!keywordInList(newKeyword, keywords)) {
      keywords.add(newKeyword);
      writeKeywords(keywords);
    }
    Timber.d(this.readKeywords().toString());
  }

  public void deleteAll() {
    writeKeywords(new ArrayList<String>());
  }
}
