package invertedIndex;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crawler.WebCrawlerWithDepth;

/**s
 *
 * @author ehab
 */
public class Test {

    public static void main(String args[]) throws IOException {

        // Changed from Index5 
        Index5 index = new Index5();

        // =========================
        // Web Crawler
        // =========================
        WebCrawlerWithDepth crawler = new WebCrawlerWithDepth();

        String seed =
                "https://en.wikipedia.org/wiki/List_of_pharaohs";

        int maxPages = 10;

        // Crawl pages
        List<SourceRecord> docs =
                crawler.crawl(seed, maxPages);

        System.out.println("Crawled Pages: " + docs.size());

        // =========================
        // Build Index from Web Docs
        // =========================
        index.buildIndexFromWeb(docs);

        index.N = docs.size();

        index.computeIDF(index.N);
        index.computeDocVectors();
        index.computeDocNorms();

        index.store("index");
        index.printDictionary();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("\n1. Search");
            System.out.println("2. Exit");
            System.out.print("Choose: ");
            String choice = in.readLine();

            if (choice == null || choice.trim().equals("2")) break;

            if (!choice.trim().equals("1")) {
                System.out.println("Invalid choice. Please enter 1 or 2.");
                continue;
            }

            System.out.print("Print search phrase: ");
            String phrase = in.readLine();
            if (phrase == null) break;
            phrase = phrase.trim().toLowerCase();

            if (phrase.isEmpty()) continue;

            // Boolean Model
            System.out.println("Boolean Model result = \n" + index.find_24_01(phrase));

            // Query Vector + Cosine Similarity
            HashMap<String, Double> qVec = index.queryToVector(phrase);
            HashMap<Integer, Double> scores = index.computeCosineSimilarity(qVec);
            List<Map.Entry<Integer, Double>> ranked = index.rankTopK(scores, 10);

            System.out.println("Cosine Similarity Results:");
            int rank = 1;
            for (Map.Entry<Integer, Double> entry : ranked) {
                int docId = entry.getKey();
                double score = entry.getValue();
                if (score <= 0) continue;
                System.out.println(
                    rank + ". " +
                    "DocID: " + docId +
                    " | Score: " + score +
                    " | Title: " + index.sources.get(docId).title +
                    " | URL: " + index.sources.get(docId).URL
                );
                rank++;
            }
        }

        System.out.println("Goodbye!");
        in.close();

    }
}