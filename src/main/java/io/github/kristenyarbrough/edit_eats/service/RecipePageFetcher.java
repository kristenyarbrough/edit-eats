package io.github.kristenyarbrough.edit_eats.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class RecipePageFetcher {

    public Document fetch(String url) {

        try {

            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Unable to fetch recipe URL: " + url,
                    e
            );

        }

    }

}
