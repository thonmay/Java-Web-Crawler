package com.thonmay.webcrawler;

import java.net.URI;
import java.util.Set;

/**
 * Defines the contract for a component that extracts links from HTML content.
 */
public interface LinkExtractor {
    /**
     * Parses the given HTML content to find all hyperlinks.
     *
     * @param htmlContent The HTML content of the page.
     * @param baseUri The URI of the page from which the content was downloaded.
     * @return A Set of absolute URIs found on the page.
     */
    Set<URI> extractLinks(String htmlContent, URI baseUri);
}
