package com.thonmay.webcrawler;

import java.net.URI;
import java.util.Set;

/**
 * A modern, immutable data carrier to hold the results of fetching a single page.
 *
 * @param fetchedUri The URI that was successfully fetched.
 * @param htmlContent The raw HTML content of the page.
 * @param containedLinks A set of absolute, normalized URIs found on the page.
 */
public record FetchResult(URI fetchedUri, String htmlContent, Set<URI> containedLinks) {
}
