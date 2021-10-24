# Simple Kotlin crawler

This project is a simple Kotlin crawler.

High level flow:

- Parse base URI (found in Main.kt)
- Extract page information such as links, imports and title
- Conditionally iterate over links (internal only) and repeat above

Coroutines are used to speed up the process of crawling by adding concurrency.

Result is converted to JSON before writing to file.
