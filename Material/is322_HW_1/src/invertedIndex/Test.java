/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author ehab
 */
public class Test {

    public static void main(String args[]) throws IOException {
    Index5 index = new Index5();
        
        // Use ../../ to go up from 'src' -> 'is322_HW_1' -> 'Material'
        String files = "../../Material/tmp11/rl/collection/";

        File file = new File(files);
        String[] fileList = file.list();

        // SAFETY CHECK: Prevent the NullPointerException if the path is wrong
        if (fileList == null) {
            System.err.println("ERROR: Could not find the collection directory at:");
            System.err.println(file.getAbsolutePath());
            System.err.println("Please check your file paths!");
            return; // Stop the program safely
        }

        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }
        
        index.buildIndex(fileList);
        index.store("index");
        index.printDictionary();

        String test3 = "data  should plain greatest comif"; // data  should plain greatest comif
        System.out.println("Boo0lean Model result = \n" + index.find_24_01(test3));

        String phrase = "";

        do {
            System.out.println("Print search phrase: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            phrase = in.readLine();
/// -3- **** complete here ****
            if (!phrase.isEmpty()) {
            System.out.println("Boolean Model result = \n" + index.find_24_01(phrase));
        }
        } 
        while (!phrase.isEmpty());

    }
}
