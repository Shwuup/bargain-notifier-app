package com.github.shwuup.app.util;

import com.github.shwuup.app.models.OfferResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public final class OffersConverter {
  public static OfferResponse deserialize(String json) {
    Gson gson = new Gson();
    Type listType = new TypeToken<OfferResponse>() {}.getType();
    return gson.fromJson(json, listType);
  }
}
