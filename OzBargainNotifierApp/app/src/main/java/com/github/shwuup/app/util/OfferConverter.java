package com.github.shwuup.app.util;

import com.github.shwuup.app.Offer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public final class OfferConverter {
    public static List<Offer> deserialize(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Offer>>(){}.getType();
        List<Offer> offers = gson.fromJson(json, listType);
        return offers;
    }
}
