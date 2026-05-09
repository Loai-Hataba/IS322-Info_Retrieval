/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ehab
 */
public class Test {

    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();

        // The collection path differs depending on which IDE runs the program:
        // NetBeans runs from src/ → needs ../../Material/tmp11/rl/collection/
        // VS Code runs from project root → needs Material/tmp11/rl/collection/
        // We try both and use whichever actually exists on disk.
        String[] candidatePaths = {
                "../../Material/tmp11/rl/collection/", // NetBeans working directory (src/)
                "Material/tmp11/rl/collection/" // VS Code working directory (project root)
        };

        String files = null;
        File file = null;
        for (String path : candidatePaths) {
            file = new File(path);
            if (file.exists() && file.isDirectory()) {
                files = path; // found a valid path — use it
                break;
            }
        }

        // SAFETY CHECK: neither path worked — tell the user what was tried
        if (files == null) {
            System.err.println("ERROR: Could not find the collection directory.");
            System.err.println("Tried:");
            for (String path : candidatePaths) {
                System.err.println("  " + new File(path).getAbsolutePath());
            }
            return; // Stop the program
        }

        String[] fileList = file.list();

        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }

        index.buildIndex(fileList);
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
            if (!phrase.isEmpty()) {
                System.out.println("Boolean Model result = \n" + index.find_24_01(phrase));

                HashMap<String, Double> qVec = index.queryToVector(phrase);

                HashMap<Integer, Double> scores = index.computeCosineSimilarity(qVec);

                System.out.println("Cosine Similarity Results:");

                for (Map.Entry<Integer, Double> entry : scores.entrySet()) {

                    int docId = entry.getKey();
                    double score = entry.getValue();

                    System.out.println(
                        "DocID: " + docId +
                        " | Score: " + score +
                        " | File: " + index.sources.get(docId).title
                    );
                }
            }
        } while (!phrase.isEmpty());

        System.out.println("Goodbye!");
        in.close();

    }
}
