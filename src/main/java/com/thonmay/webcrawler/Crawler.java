package com.thonmay.webcrawler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Crawler {

    private final int maxPagesToCrawl;
    private final Set<URI> visitedUris;
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final WordIndexer indexer;

    public Crawler(int maxPagesToCrawl) {
        this.maxPagesToCrawl = maxPagesToCrawl;
        this.visitedUris = ConcurrentHashMap.newKeySet();
        this.indexer = new WordIndexer();
    }

    public void crawl(String seedUrl) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            scheduleFetch(new URI(seedUrl), executor);

            while (activeTaskCount.get() > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Main thread interrupted.");
                    break;
                }
            }

        } catch (URISyntaxException e) {
            System.err.println("Invalid seed URL: " + seedUrl);
        } finally {
            shutdownAndAwaitTermination(executor); 
            System.out.println("\nCrawl finished. Visited " + visitedUris.size() + " unique pages.");
            indexer.printIndexResults();
        }
    }

    private void scheduleFetch(URI uri, ExecutorService executor) {
        if (visitedUris.size() >= maxPagesToCrawl || !visitedUris.add(uri)) {
            return;
        }

        activeTaskCount.incrementAndGet();
        System.out.println("Scheduling (" + activeTaskCount.get() + " active): " + uri);

        CompletableFuture.supplyAsync(() -> PageFetcher.fetch(uri), executor)
                .thenAcceptAsync(fetchResultOpt -> {
                    fetchResultOpt.ifPresent(result -> {
                        indexer.indexPage(result.fetchedUri(), result.htmlContent());
                        result.containedLinks().forEach(link -> scheduleFetch(link, executor));
                    });
                }, executor)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        System.err.println("Error processing " + uri + ": " + ex.getMessage());
                    }
                    activeTaskCount.decrementAndGet();
                });
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted. Waits for existing tasks to
     * terminate and then returns. If the specified waiting time elapses before
     * termination, the pool is forcibly terminated.
     *
     * @param pool The executor service to be shut down.
     */
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); 
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler(100);
        crawler.crawl("https://thonmay.netlify.app/");
    }
}