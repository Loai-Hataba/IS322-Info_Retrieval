/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import crawler.WebCrawlerWithDepth;

/**
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

        String phrase = "";

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        do {
            System.out.println("Print search phrase: ");
            phrase = in.readLine();

            // NULL GUARD: readLine() returns null when the user presses Ctrl+Z
            if (phrase == null)
                break; // treat EOF as "quit"

            // FIX: normalize input to avoid "not found" due to casing/spaces
            phrase = phrase.trim().toLowerCase();

            if (!phrase.isEmpty()) {

                // Boolean Model (optional if still supported)
                System.out.println("Boolean Model result = \n" + index.find_24_01(phrase));

                // =========================
                // Query Vector
                // =========================
                HashMap<String, Double> qVec =
                        index.queryToVector(phrase);

                // =========================
                // Cosine Similarity
                // =========================
                HashMap<Integer, Double> scores =
                        index.computeCosineSimilarity(qVec);

                // =========================
                // Rank Top 10
                // =========================
                List<Map.Entry<Integer, Double>> ranked =
                        index.rankTopK(scores, 10);

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

        } while (phrase != null && !phrase.isEmpty());

        System.out.println("Goodbye!");
        in.close();

    }
}