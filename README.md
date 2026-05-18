# IS322 – Information Retrieval

Course assignments implementing an **Inverted Index**, **Web Crawler**, and **TF-IDF Cosine Similarity** search engine in Java.

---

## Project Structure

```
Material/
└── is322_HW_1/
    ├── src/
    │   ├── invertedIndex/
    │   │   ├── Index5.java          # Core index: build, search, IDF, TF-IDF, cosine similarity
    │   │   ├── Test.java            # HW2 main entry point (interactive search)
    │   │   ├── DictEntry.java       # Dictionary entry (doc_freq, term_freq, posting list)
    │   │   ├── Posting.java         # Posting node (docId, dtf, positions, next)
    │   │   ├── SourceRecord.java    # Crawled document record (url, title, text, norm)
    │   │   └── Stemmer.java         # Porter Stemmer (available, not currently applied)
    │   └── crawler/
    │       └── WebCrawlerWithDepth.java  # BFS web crawler + alternative main
    └── lib/
        └── jsoup-1.17.2.jar         # HTML parsing dependency
```

---

## Assignment 1 (HW1)

Builds an **inverted index** over a local directory of text files and supports keyword search.

**Tasks completed:**

- `intersect(Posting, Posting)` — positional posting list intersection for phrase queries
- `buildIndex(String[] files)` — indexes all `.txt` files in a directory
- `printPostingList(Posting)` — prints posting list without trailing comma
- Converted to a **positional index** (tracks word positions per document)
- All methods in `Index5.java` are documented with Javadoc

---

## Assignment 2 (HW2)

Extends HW1 with a **web crawler** and **cosine similarity ranking**.

**Pipeline:**

1. **Crawl** — BFS crawl of Wikipedia starting from `List_of_pharaohs`, up to 10 pages (jsoup)
2. **Index** — Build inverted index from crawled pages (`buildIndexFromWeb`)
3. **IDF** — Compute `idf = log10(N / doc_freq)` for each term
4. **TF-IDF vectors** — Build per-document weight vectors (`weight = dtf × idf`)
5. **Document norms** — Compute Euclidean norm for each document vector
6. **Query vectorization** — Convert query to TF-IDF vector (`queryToVector`)
7. **Cosine similarity** — Score each document against the query vector
8. **Rank top-K** — Return top 10 documents sorted by score (zero-score docs filtered out)

---

## How to Run

### Prerequisites

- Java 17+ with `javac` on PATH (e.g. `/usr/lib/jvm/java-17-temurin-jdk/bin/javac`)
- `lib/jsoup-1.17.2.jar` present in the project

### Compile

```bash
cd Material/is322_HW_1
mkdir -p out
/usr/lib/jvm/java-17-temurin-jdk/bin/javac \
  -cp lib/jsoup-1.17.2.jar \
  -d out \
  $(find src -name "*.java")
```

### Run

```bash
/usr/lib/jvm/java-17-temurin-jdk/bin/java \
  -cp out:lib/jsoup-1.17.2.jar \
  invertedIndex.Test
```

### Usage

After the index is built and the dictionary is printed, an interactive menu appears:

```
1. Search
2. Exit
Choose:
```

- Enter `1` to search — you will be prompted for a query phrase
- Enter `2` to exit cleanly

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| [jsoup](https://jsoup.org/) | 1.17.2 | HTML parsing for web crawler |

---

## Team

Group project — IS322 Information Retrieval, 3rd Year 2nd Term.
