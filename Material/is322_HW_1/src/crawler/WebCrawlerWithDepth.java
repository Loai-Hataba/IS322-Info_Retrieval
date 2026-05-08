package crawler;

import invertedIndex.Index5;
import invertedIndex.SourceRecord;
import java.util.*;

/**
 * HW2 – Main entry point.
 * Orchestrates: crawl → index → IDF/TF-IDF → cosine similarity → top-K ranking.
 */
public class WebCrawlerWithDepth {

    static final String SEED_URL = "https://en.wikipedia.org/wiki/List_of_pharaohs";
    static final int MAX_PAGES = 10;
    static final int TOP_K = 10;

    // ---------------------------------------------------------------
    // PERSON 1 – Web Crawler
    // ---------------------------------------------------------------
    /**
     * BFS-crawl Wikipedia starting from seedUrl, visiting at most maxPages pages.
     * Use jsoup to fetch each page: extract title, plain body text, and outgoing links.
     * Maintain a visited HashSet and a BFS queue to avoid re-visiting URLs.
     *
     * @param seedUrl  Starting Wikipedia URL.
     * @param maxPages Maximum number of pages to visit.
     * @return List of SourceRecord, one per visited page (fid, URL, title, text).
     */
    public List<SourceRecord> crawl(String seedUrl, int maxPages) {
        // TODO: Person 1 – implement BFS crawl using jsoup
        return new ArrayList<>();
    }

    // ---------------------------------------------------------------
    // PERSON 6 – Integration & Ranking
    // ---------------------------------------------------------------
    /**
     * Ranks scores map descending and returns the top-k entries.
     *
     * @param scores Map of docId → cosine similarity score.
     * @param k      Number of top results to return.
     * @return Sorted list of (docId, score) entries, highest first.
     */
    public List<Map.Entry<Integer, Double>> rankTopK(
            Map<Integer, Double> scores, int k) {
        // TODO: Person 6 – sort scores map descending, return top-k entries
        return new ArrayList<>();
    }

    // ---------------------------------------------------------------
    // MAIN – Wire the full HW2 pipeline (Person 6 fills this in)
    // ---------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        WebCrawlerWithDepth app = new WebCrawlerWithDepth();
        Index5 index = new Index5();

        // Step 1 – Crawl
        List<SourceRecord> pages = app.crawl(SEED_URL, MAX_PAGES);
        System.out.println("Crawled " + pages.size() + " pages.");

        // Step 2 – Build index
        index.buildIndexFromWeb(pages);
        index.printDictionary();

        // Step 3 – Compute IDF and document norms
        index.computeIDF(pages.size());
        index.computeDocNorms();

        // Step 4 – Build TF-IDF document vectors
        index.computeDocVectors();

        // Step 5-8 – Query loop
        java.io.BufferedReader in = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
        String query;
        do {
            System.out.print("Enter query (empty to quit): ");
            query = in.readLine();
            if (query == null || query.isEmpty()) break;

            HashMap<String, Double> qVec = index.queryToVector(query);
            HashMap<Integer, Double> scores = index.computeCosineSimilarity(qVec);
            List<Map.Entry<Integer, Double>> topK = app.rankTopK(scores, TOP_K);

            System.out.println("Top " + TOP_K + " results:");
            int rank = 1;
            for (Map.Entry<Integer, Double> e : topK) {
                SourceRecord sr = index.sources.get(e.getKey());
                System.out.printf("  %2d. [doc %d] score=%.4f  %s%n",
                        rank++, e.getKey(), e.getValue(), sr.title);
            }
        } while (true);

        System.out.println("Goodbye!");
        in.close();
    }
}
