package com.thonmay.webcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple, single-threaded web crawler.
 * This class will crawl the web starting from a "seed" URL,
 * keeping track of visited URLs and discovering new ones.
 */
public class Crawler {
    private final Queue<String> frontier;

    private final Set<String> visited;
    
    private static final String URL_REGEX = "href=\"(.*?)\""; // Looks for href attributes on <a> tags.
    private final Pattern urlPattern;

    public Crawler() {
        this.frontier = new LinkedList<>();
        this.visited = new HashSet<>();
        this.urlPattern = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);
    }

    /**
     * The main crawling logic.
     * @param seedUrl The starting URL for the crawl.
     * @param maxPages The maximum number of pages to crawl.
     */
    public void startCrawling(String seedUrl, int maxPages) {
        frontier.add(seedUrl);

        // Loop until the lrontier is empty or visited the max number of pages.
        while (!frontier.isEmpty() && visited.size() < maxPages) {
            String currentUrl = frontier.poll(); 

            if (currentUrl == null || visited.contains(currentUrl)) {
                continue; 
            }

            visited.add(currentUrl);
            System.out.println("Crawling: " + currentUrl);

            try {
                URL url = new URL(currentUrl);
                String htmlContent = downloadPage(url);
                if (htmlContent != null) {
                    processPage(url, htmlContent);
                }
            } catch (MalformedURLException e) {
                System.err.println("Invalid URL format: " + currentUrl);
            } catch (IOException e) {
                System.err.println("Error downloading page: " + currentUrl + " - " + e.getMessage());
            }
        }
        System.out.println("\nCrawling finished. Visited " + visited.size() + " pages.");
    }

    /**
     * Downloads the HTML content of a given URL.
     * @param url The URL of the page to download.
     * @return The HTML content as a String, or null if an error occurs.
     * @throws IOException if a network error occurs.
     */
    private String downloadPage(URL url) throws IOException {
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        }
    }

    /**
     * Parses the HTML content to find new links and adds them to the Frontier.
     * @param baseUrl The URL of the page being processed, used to resolve relative links.
     * @param htmlContent The HTML content of the page.
     */
    private void processPage(URL baseUrl, String htmlContent) {
        Matcher matcher = urlPattern.matcher(htmlContent);
        
        while (matcher.find()) {
            String link = matcher.group(1).trim(); 

            try {
                URL absoluteUrl = new URL(baseUrl, link);
                String formattedUrl = absoluteUrl.toExternalForm();

                if (!visited.contains(formattedUrl)) {
                    frontier.add(formattedUrl);
                }
            } catch (MalformedURLException e) {
                
            }
        }
    }

    public static void main(String[] args) {
        
        Crawler crawler = new Crawler();
        crawler.startCrawling("https://thonmay.netlify.app/", 50);
    }
}