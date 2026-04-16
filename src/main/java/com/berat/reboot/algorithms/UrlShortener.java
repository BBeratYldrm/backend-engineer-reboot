package com.berat.reboot.algorithms;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UrlShortener {

    Map<String, String> urlShortener = new ConcurrentHashMap<>(); // I will create storage like

    public String shorten(String url) { //First of all need to create random short string
        String shortenedUrl = UUID.randomUUID().toString().substring(0, 6);
        urlShortener.put(shortenedUrl, url); // Store the url as a key, UUID for a value
        return shortenedUrl;
    }

    public String getCode(String urlCode) {
        return urlShortener.get(urlCode);
    }
}
