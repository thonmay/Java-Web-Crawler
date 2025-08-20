package com.thonmay.webcrawler;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator; 
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordIndexer {

    private static final Pattern WORD_SPLITTER = Pattern.compile("[^a-zA-Z0-9]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "the", "in", "is", "it", "of", "for", "on", "with", "as", "by", "to"
    );
    private final Map<String, Set<URI>> index = new ConcurrentHashMap<>();

    public void indexPage(URI uri, String htmlContent) {
        getWords(htmlContent).forEach(word -> {
            Set<URI> uris = index.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet());
            uris.add(uri);
        });
    }

    private Set<String> getWords(String htmlContent) {
        String textOnly = htmlContent.replaceAll("<[^>]*>", " ");
        return WORD_SPLITTER.splitAsStream(textOnly)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }

    public void printIndexResults() {
        System.out.println("\n--- Indexing Results ---");
        System.out.println("Total unique words indexed: " + index.size());

        if (index.isEmpty()) return;

        index.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(Comparator.comparingInt(Set::size))))
                .limit(10)
                .forEach(entry -> {
                    System.out.printf("Word: '%s' appeared on %d pages.%n", entry.getKey(), entry.getValue().size());
                });
    }
}