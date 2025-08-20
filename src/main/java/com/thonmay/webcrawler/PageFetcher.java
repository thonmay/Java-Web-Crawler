package com.thonmay.webcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageFetcher {

    private static final Pattern LINK_PATTERN =
            Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

    public static Optional<FetchResult> fetch(URI uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "SimpleJavaCrawler/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Failed to download " + uri + ". Status: " + responseCode);
                return Optional.empty();
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            }

            String html = content.toString();
            Set<URI> links = extractLinks(html, uri);
            return Optional.of(new FetchResult(uri, html, links));

        } catch (IOException e) {
            System.err.println("I/O Error for " + uri + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private static Set<URI> extractLinks(String html, URI baseUri) {
        Set<URI> extractedUris = new HashSet<>();
        Matcher matcher = LINK_PATTERN.matcher(html);

        while (matcher.find()) {
            String href = matcher.group(1).trim();
            try {
                URI resolvedUri = baseUri.resolve(href);
                if (isHttp(resolvedUri)) {
                    extractedUris.add(normalize(resolvedUri));
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping malformed link '" + href + "'");
            }
        }
        return extractedUris;
    }

    private static boolean isHttp(URI uri) {
        if (uri.getScheme() == null) return false;
        String scheme = uri.getScheme().toLowerCase();
        return "http".equals(scheme) || "https".equals(scheme);
    }

    private static URI normalize(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
        } catch (URISyntaxException e) {
            return uri;
        }
    }
}