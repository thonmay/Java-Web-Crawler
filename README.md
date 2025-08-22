# Java Concurrent Web Crawler

A multithreaded web crawler in Java that concurrently fetches pages and builds an in-memory word index from their content.

This project demonstrates a practical application of modern Java's concurrency and data processing APIs.

## Key Concepts Demonstrated

*   **Concurrency:** `java.util.concurrent` (`ExecutorService`, `CompletableFuture`)
*   **Asynchronous Programming:** Non-blocking, I/O-bound task pipelines.
*   **Thread-Safe Collections:** `ConcurrentHashMap` for managing shared state.
*   **Data Processing:** Java Streams API and Regular Expressions for text processing.
*   **Networking & I/O:** `HttpURLConnection` with modern resource management.
*   **Testing:** Unit testing core logic with JUnit 5.
*   **Build Automation:** Dependency management and packaging with Apache Maven.

## Getting Started

### Prerequisites

*   JDK 17 or newer
*   Apache Maven

### Build the Project

This compiles the code, runs all unit tests, and packages the application into a single executable JAR.

```
