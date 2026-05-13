package crawler;

import invertedIndex.Index5;
import invertedIndex.SourceRecord;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


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

    public static void main(String[] args) throws Exception {
        WebCrawlerWithDepth app = new WebCrawlerWithDepth();
        Index5 index = new Index5();

        
        List<SourceRecord> pages = app.crawl(SEED_URL, MAX_PAGES);
        System.out.println("Crawled " + pages.size() + " pages.");

      
        index.buildIndexFromWeb(pages);
        index.printDictionary();

        index.computeIDF(pages.size());
        index.computeDocNorms();

        index.computeDocVectors();

        java.io.BufferedReader in = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
        String query;
        do {
            System.out.print("Enter query (or type 'exit' / press Enter to quit): ");
            query = in.readLine();
            if (query == null || query.isEmpty() || query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) break;

            HashMap<String, Double> qVec = index.queryToVector(query);
            HashMap<Integer, Double> scores = index.computeCosineSimilarity(qVec);
            List<Map.Entry<Integer, Double>> topK = index.rankTopK(scores, TOP_K);

            System.out.println("Top " + TOP_K + " results:");
            int rank = 1;
            for (Map.Entry<Integer, Double> e : topK) {
                if (e.getValue() <= 0) continue;
                SourceRecord sr = index.sources.get(e.getKey());
                System.out.printf("  %2d. [doc %d] score=%.4f  %s%n",
                        rank++, e.getKey(), e.getValue(), sr.title);
            }
        } while (true);

        System.out.println("Goodbye!");
        in.close();
    }

}
