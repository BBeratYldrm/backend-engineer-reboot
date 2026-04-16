package com.berat.reboot.algorithms;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UrlShortenerTest {

    UrlShortener urlShortener = new UrlShortener();

    @Test
    void shorten_shouldReturn6CharCode() {
        String code = urlShortener.shorten("https://rakuten.co.jp");

        // code should exist
        assertNotNull(code);
        // should be exactly 6 chars — UUID substring(0,6)
        assertEquals(6, code.length());
    }

    @Test
    void getCode_shouldReturnCorrectValue() {
        String givenUrl = "https://rakuten.co.jp";

        // shorten first to get the key
        String code = urlShortener.shorten(givenUrl);

        // should get back the original url using that key
        String result = urlShortener.getCode(code);
        assertEquals(givenUrl, result);
    }

    @Test
    void shorten_shouldBeThreadSafe() throws InterruptedException {
        int threadCount = 100;

        // 100 threads all calling shorten() at the same time
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                urlShortener.shorten("https://example.com/" + index);
            });
        }

        // wait for all threads to finish
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // no entries should be lost — map must have exactly 100
        assertEquals(100, urlShortener.urlShortener.size());
    }
}