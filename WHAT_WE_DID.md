# What We Did – IS322 Information Retrieval

> **Purpose:** A plain-language record of everything the team built across both assignments.
> Each section maps directly to a required task so you can trace *what was asked → what we coded → where to find it*.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Assignment 1 – Inverted Index](#assignment-1--inverted-index)
   - [Data Structures](#data-structures)
   - [Task 1 – Print Posting List](#task-1--print-posting-list)
   - [Task 2 – Build the Index from Files](#task-2--build-the-index-from-files)
   - [Task 3 – Index a Single Line (Positional)](#task-3--index-a-single-line-positional)
   - [Task 4 – Positional Intersection for Phrase Queries](#task-4--positional-intersection-for-phrase-queries)
   - [Task 5 – Phrase Search](#task-5--phrase-search)
   - [Supporting Work](#supporting-work-hw1)
3. [Assignment 2 – Web Crawler + Ranked Retrieval](#assignment-2--web-crawler--ranked-retrieval)
   - [Task 1 – Web Crawler](#task-1--web-crawler)
   - [Task 2 – Build Index from Crawled Pages](#task-2--build-index-from-crawled-pages)
   - [Task 3 – Compute IDF](#task-3--compute-idf)
   - [Task 4 – Build Document TF-IDF Vectors](#task-4--build-document-tf-idf-vectors)
   - [Task 5 – Document Norms](#task-5--document-norms)
   - [Task 6 – Query Vector](#task-6--query-vector)
   - [Task 7 – Cosine Similarity](#task-7--cosine-similarity)
   - [Task 8 – Rank Top-K Results](#task-8--rank-top-k-results)
   - [Supporting Work](#supporting-work-hw2)
4. [How It All Connects](#how-it-all-connects)

---

## Project Overview

This project implements a **search engine from scratch** in Java, broken across two assignments:

- **Assignment 1** builds the core engine: a *positional inverted index* over a local set of text files, with phrase search support.
- **Assignment 2** adds a *web crawler*, *TF-IDF ranking*, and *cosine similarity* so the engine can fetch real Wikipedia pages and return results sorted by relevance.

The entry point for the full system is `Test.java`. All core logic lives in `Index5.java`.

---

## Assignment 1 – Inverted Index

### Data Structures

Before the tasks, it helps to understand the three core building blocks:

| Class | What it represents |
|---|---|
| `DictEntry` | One row in the dictionary: how many documents contain the term (`doc_freq`), how often it appears in total (`term_freq`), and the head of its posting list. |
| `Posting` | One node in a posting list: which document (`docId`), how many times the term appears in it (`dtf`), a list of exact word positions, and a pointer to the next node. |
| `SourceRecord` | Metadata for one document: its ID (`fid`), URL, title, raw text, length, and later its TF-IDF norm. |

The main index itself is a `HashMap<String, DictEntry>` — a term maps to its dictionary entry, which chains to all postings.

---

### Task 1 – Print Posting List

**What was required:** Print the posting list for a term in a clean format — no trailing comma after the last entry.

**What we did:** Implemented `printPostingList(Posting p)` in `Index5.java`. It walks the linked list and places a comma *between* nodes only, not after the last one. This method is also used by `printDictionary()` which prints every term in the index together with its document frequency.

**File:** `src/invertedIndex/Index5.java` → `printPostingList`, `printDictionary`

---

### Task 2 – Build the Index from Files

**What was required:** Read a collection of text files, assign each a unique integer document ID, and populate the inverted index.

**What we did:** Implemented `buildIndex(String[] files)`. It:
1. Sorts the file list alphabetically (using a bubble-sort helper `sort()`) so document IDs are assigned consistently — e.g., `p1.txt` always gets ID 0, `p2.txt` gets ID 1, and so on.
2. Opens each file, reads it line by line, and calls `indexOneLine` for each line.
3. Records the total word count (document length) in the `sources` map.

Files that cannot be opened are skipped with a warning, not a crash.

**File:** `src/invertedIndex/Index5.java` → `buildIndex`, `sort`

---

### Task 3 – Index a Single Line (Positional)

**What was required:** Tokenise text, handle stop words, and track the position of every word so phrase queries can work correctly.

**What we did:** Implemented `indexOneLine(String ln, int fid, int startPosition)`. For each token in a line it:
1. Lowercases the token.
2. Skips it if it is a **stop word** (the, to, be, for, from, in, a, into, by, or, and, that) or shorter than 2 characters. Importantly, even skipped stop words still *consume a position number* — otherwise the distance between real words would be wrong.
3. Applies `stemWord()` (currently a pass-through; a full Porter Stemmer is available in `Stemmer.java` and can be enabled).
4. If the word is **new to this document**, creates a fresh `Posting` and appends it to the term's posting list, incrementing `doc_freq`.
5. If the word **already appeared** in this document, updates the existing posting's `dtf` counter and appends the new position.
6. Always increments `term_freq` (collection-wide count).

**File:** `src/invertedIndex/Index5.java` → `indexOneLine`, `stopWord`, `stemWord`

---

### Task 4 – Positional Intersection for Phrase Queries

**What was required:** Given two posting lists, find documents where the two terms appear *adjacent* to each other (one right after the other).

**What we did:** Implemented `intersect(Posting pL1, Posting pL2)` using a **two-level two-pointer algorithm**:

- **Outer level:** advance through documents in both lists simultaneously (standard merge). Skip non-matching doc IDs.
- **Inner level:** when both pointers are on the same document, scan their position lists. A match exists when `pos2 == pos1 + 1` (word 2 immediately follows word 1). All matched positions are recorded so the result can be chained further for 3+ word phrases.

The result is a new posting list containing only documents (and positions) where the phrase holds.

**File:** `src/invertedIndex/Index5.java` → `intersect`

---

### Task 5 – Phrase Search

**What was required:** Accept a multi-word phrase and return all documents containing it as a consecutive sequence.

**What we did:** Implemented `find_24_01(String phrase)`. It:
1. Splits the phrase into words.
2. Looks up the first word's posting list.
3. Iteratively calls `intersect` with each subsequent word's posting list. Each intersection reduces the result to only those documents where the growing phrase appears consecutively.
4. Returns a formatted string of matching documents (docId, title, length) or a "not found" message.

This works for phrases of any length — a single word, two words, or more.

**File:** `src/invertedIndex/Index5.java` → `find_24_01`

---

### Supporting Work (HW1)

| What | Where | Why |
|---|---|---|
| `store(name)` | `Index5.java` | Saves the index to a flat file on disk (two-section CSV format) so it doesn't need to be rebuilt every run. |
| `load(name)` | `Index5.java` | Reads the saved file back into memory, reconstructing `sources` and `index`. |
| `storageFileExists` / `createStore` | `Index5.java` | Utility guards around the storage file. |
| `Stemmer.java` | Available but inactive | Contains a complete Porter Stemmer. Plugging it into `stemWord()` would reduce inflected forms (e.g., "running" → "run"). Not required by HW1 so left as a no-op. |

---

## Assignment 2 – Web Crawler + Ranked Retrieval

Assignment 2 extends the engine so it crawls the web and ranks results by relevance, not just presence.

---

### Task 1 – Web Crawler

**What was required:** Fetch a seed Wikipedia page, follow its links, and collect up to a fixed number of pages.

**What we did:** Implemented `WebCrawlerWithDepth.crawl(String seedUrl, int maxPages)`. It uses **BFS (Breadth-First Search)**:

1. Starts from the seed URL (`https://en.wikipedia.org/wiki/List_of_pharaohs`).
2. Maintains a `Queue` of URLs to visit and a `HashSet` of already-visited URLs to avoid duplicates.
3. For each URL, it uses **jsoup** to fetch and parse the HTML. It extracts text from `#mw-content-text p` (Wikipedia's main article paragraphs).
4. Cleans the text: removes citation markers like `[1]`, strips punctuation, lowercases everything.
5. Filters outgoing links: only follows `https://en.wikipedia.org/wiki/` URLs, skips the Main Page, and ignores namespace URLs (those containing `:` like `File:`, `Category:`, `Help:`).
6. Each visited page becomes a `SourceRecord` (with its URL, title, cleaned text, and a generated doc ID).
7. Stops when `maxPages` (10) documents have been collected.

**File:** `src/crawler/WebCrawlerWithDepth.java` → `crawl`

---

### Task 2 – Build Index from Crawled Pages

**What was required:** Feed the crawled pages into the inverted index (which was designed for local files in HW1).

**What we did:** Implemented `buildIndexFromWeb(List<SourceRecord> docs)` in `Index5.java`. It reuses `indexOneLine` directly — the same tokenisation, stop word removal, and positional tracking from HW1. The only difference is that instead of reading from disk, it reads from the in-memory `SourceRecord.text` field that the crawler already populated. No duplication of logic.

**File:** `src/invertedIndex/Index5.java` → `buildIndexFromWeb`

---

### Task 3 – Compute IDF

**What was required:** Compute the Inverse Document Frequency (IDF) for every term in the index.

**What we did:** Implemented `computeIDF(int n)`. For each term in the dictionary:

```
idf(term) = log10( N / doc_freq )
```

Where `N` is the total number of documents and `doc_freq` is how many documents contain the term. A term that appears in every document gets an IDF close to 0 (not useful for distinguishing documents). A term that appears in only one document gets a high IDF (very distinctive). Results are stored in `idfMap`.

**File:** `src/invertedIndex/Index5.java` → `computeIDF`

---

### Task 4 – Build Document TF-IDF Vectors

**What was required:** Represent each document as a vector of term weights.

**What we did:** Implemented `computeDocVectors()`. For every term and every document that contains it:

```
weight(term, doc) = dtf(term, doc) × idf(term)
```

Where `dtf` is how many times the term appears in that specific document (Term Frequency in the document). This is stored as `docVectors[docId][term] = weight` — a nested `HashMap`. A document with many occurrences of a rare term gets a high weight for that term.

**File:** `src/invertedIndex/Index5.java` → `computeDocVectors`

---

### Task 5 – Document Norms

**What was required:** Compute the length (norm) of each document's TF-IDF vector for use in cosine similarity.

**What we did:** Implemented `computeDocNorms()`. For each document it computes:

```
norm(doc) = sqrt( sum over all terms t: weight(t, doc)² )
```

This is the Euclidean length of the document vector. Dividing by it during cosine similarity cancels out the effect of document length — a long Wikipedia article with many word occurrences should not automatically outscore a short but highly relevant one. The norm is stored in `SourceRecord.norm`.

**File:** `src/invertedIndex/Index5.java` → `computeDocNorms`

---

### Task 6 – Query Vector

**What was required:** Convert a user's search query into the same TF-IDF vector format used by documents.

**What we did:** Implemented `queryToVector(String query)`. It applies identical preprocessing to the query as was applied during indexing: tokenise, lowercase, skip stop words, apply stemming. Then for each remaining term:

```
query_weight(term) = tf_in_query × idf(term)
```

If a query term does not appear in the index at all, its IDF is 0 and it is excluded (it cannot match anything). The result is a `HashMap<String, Double>` — the query's TF-IDF vector.

**File:** `src/invertedIndex/Index5.java` → `queryToVector`

---

### Task 7 – Cosine Similarity

**What was required:** Score every document against the query by measuring how similar their vectors are.

**What we did:** Implemented `computeCosineSimilarity(HashMap<String, Double> qVec)`. For each document:

1. Compute the **dot product** — for every term that appears in *both* the query vector and the document vector, multiply their weights and sum the results. Terms in only one of the two contribute nothing.
2. Divide by the document's **norm** (computed in Task 5). This normalises for document length.

```
similarity(query, doc) = dot(qVec, docVec) / norm(doc)
```

A score of 1.0 would mean perfect alignment; 0 means no shared meaningful terms. The result is a `HashMap<Integer, Double>` mapping each docId to its score.

**File:** `src/invertedIndex/Index5.java` → `computeCosineSimilarity`

---

### Task 8 – Rank Top-K Results

**What was required:** Return the most relevant documents in order.

**What we did:** Implemented `rankTopK(HashMap<Integer, Double> scores, int k)`. It sorts all document scores in descending order using a stream sort and returns the top `k` entries. In the display loop, documents with a score of 0 or below are filtered out so the user never sees irrelevant results. Results show: rank, document ID, score, title, and URL.

**File:** `src/invertedIndex/Index5.java` → `rankTopK`

---

### Supporting Work (HW2)

| What | Where | Why |
|---|---|---|
| `Test.java` | `src/invertedIndex/Test.java` | Main entry point. Orchestrates the full pipeline: crawl → index → IDF → vectors → norms → interactive query loop. |
| Interactive menu | `Test.java` | After the index is built, the user sees a `1. Search / 2. Exit` menu. Choosing 1 prompts for a phrase and shows both Boolean (phrase match) and ranked cosine results. Choosing 2 prints "Goodbye!" and exits cleanly. |
| `WebCrawlerWithDepth.main` | `src/crawler/WebCrawlerWithDepth.java` | An alternative entry point that runs the same pipeline without the Boolean search step, useful for isolated testing of the crawler + ranking. |

---

## How It All Connects

```
HW1 pipeline (local files):
  Files on disk ──► buildIndex ──► indexOneLine ──► Inverted Index
                                                         │
                                              find_24_01 (phrase search)

HW2 pipeline (web):
  Wikipedia seed URL
       │
       ▼
  WebCrawlerWithDepth.crawl          (fetches 10 pages via jsoup)
       │
       ▼
  buildIndexFromWeb                  (reuses indexOneLine from HW1)
       │
       ▼
  computeIDF ──► computeDocVectors ──► computeDocNorms
                                              │
                              User enters query
                                              │
                                       queryToVector
                                              │
                                  computeCosineSimilarity
                                              │
                                         rankTopK
                                              │
                                    Display top results
```

The key insight is that **Assignment 2 does not replace Assignment 1** — it builds on top of it. The same inverted index structure, the same `indexOneLine` tokeniser, and the same phrase search all carry forward. Assignment 2 adds the source (a live web crawler), the ranking model (TF-IDF + cosine similarity), and the interactive interface.
