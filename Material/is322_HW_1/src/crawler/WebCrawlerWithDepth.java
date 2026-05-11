package crawler;

import invertedIndex.Index5;
import invertedIndex.SourceRecord;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * HW2 – Main entry point.
 * Orchestrates: crawl → index → IDF/TF-IDF → cosine similarity → top-K ranking.
 */
public class WebCrawlerWithDepth {

    static final String SEED_URL = "https://en.wikipedia.org/wiki/List_of_pharaohs";
    static final int MAX_PAGES = 10;
    static final int TOP_K = 10;


    public List<SourceRecord> crawl(String seedUrl, int maxPages) {
        if (seedUrl == null || seedUrl.isEmpty() || maxPages <= 0) {
            throw new IllegalArgumentException("Invalid seed URL or maxPages");
        }
        // check if seedUrl is a valid Wikipedia URL
        if (!seedUrl.startsWith("https://en.wikipedia.org/wiki/")) {
            throw new IllegalArgumentException("Seed URL must be a Wikipedia page");
        }

        HashSet<String> visitedUrls = new HashSet<>();
        Queue<String> urlQueue = new LinkedList<>();
        List<SourceRecord> records = new ArrayList<>();

        urlQueue.add(seedUrl);

        while (!urlQueue.isEmpty() && visitedUrls.size() < maxPages) {
            String url = urlQueue.poll();
            if (visitedUrls.contains(url)) continue;
            
            try {
                // Fetch the page using jsoup
                Document doc = Jsoup.connect(url).get();
                int fid = records.size(); // assign fid based on current size of records
                String title = doc.title();
                Elements paragraphs = doc.select("#mw-content-text p");
                StringBuilder sb = new StringBuilder();
                for (Element p : paragraphs) {
                    sb.append(p.text()).append(" ");
                }
                String text = sb.toString();
                text = text.replaceAll("\\[\\d+\\]", "");        // citation numbers [1], [23]
                text = text.replaceAll("\\[edit\\]", "");         // section edit links
                text = text.toLowerCase();
                text = text.replaceAll("[^a-z0-9\\s]", ""); // remove punctuation, keep letters, numbers, spaces
                text = text.replaceAll("\\s{2,}", " ");           // multiple spaces → single space
                text = text.trim();                               // leading/trailing whitespace

                records.add(new SourceRecord(fid, url, title, text));
                visitedUrls.add(url);

                // Extract outgoing links
                List<Element> links = doc.select("a[href]");

                for (Element link : links) {
                    String absUrl = link.attr("abs:href");

                    // skip empty links and non-Wikipedia URLs
                    if (absUrl == null || absUrl.isEmpty()) continue;
                    if (!absUrl.startsWith("https://en.wikipedia.org/wiki/")) continue;

                    // skip main page
                    if (absUrl.equals("https://en.wikipedia.org/wiki/Main_Page")) continue;

                    // strip fragment (#Section)
                    if (absUrl.contains("#")) {
                        absUrl = absUrl.substring(0, absUrl.indexOf("#"));
                    }

                    // skip non-article namespaces (File:, Category:, Help:, etc.)
                    String afterWiki = absUrl.substring("https://en.wikipedia.org/wiki/".length());
                    if (afterWiki.contains(":")) continue;
                    
                    if (!visitedUrls.contains(absUrl)) {
                        urlQueue.add(absUrl);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error fetching " + url + ": " + e.getMessage());
            }
    
        }

        return records;
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
