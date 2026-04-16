package com.berat.reboot.algorithms;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UrlShortener {

    Map<String, String> urlToCode = new ConcurrentHashMap<>(); // url → code: to check if url already shortened
    Map<String, String> codeToUrl = new ConcurrentHashMap<>(); // code → url: to retrieve original url + collision check

    public String shorten(String url) {

        // max 100 urls — in-memory storage has a limit
        if (urlToCode.size() >= 100) {
            throw new IllegalStateException("Storage is full — max 100 URLs");
        }

        // same url already shortened — return existing code
        if (urlToCode.containsKey(url)) {
            return urlToCode.get(url);
        }

        // generate unique 6-char code
        // loop handles the rare case of UUID collision
        String code = UUID.randomUUID().toString().substring(0, 6);
        while (codeToUrl.containsKey(code)) {
            code = UUID.randomUUID().toString().substring(0, 6);
        }

        urlToCode.put(url, code);
        codeToUrl.put(code, url);

        return code;
    }

    public String getCode(String code) {
        return codeToUrl.get(code);
    }
}
