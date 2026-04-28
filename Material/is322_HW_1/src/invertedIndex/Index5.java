/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintWriter;
import java.util.ArrayList;
/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    int N = 0;
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // THe inverted index
    //--------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }


    //---------------------------------------------
    public void printPostingList(Posting p) {
        System.out.print("[");
        while (p != null) {
            System.out.print(p.docId);
            if (p.next != null) {
                System.out.print(",");  // comma only between elements
            }
            p = p.next;
        }
        System.out.println("]");
    }
    //---------------------------------------------
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }
 
    //-----------------------------------------------
    public void buildIndex(String[] files) {  
        int fid = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) {
                    // Pass the current flen as the startPosition for the line
                    flen += indexOneLine(ln, fid, flen);
                }
                sources.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
    }
    //----------------------------------------------------------------------------  

    public int indexOneLine(String ln, int fid, int startPosition) {
        int flen = 0;
        String[] words = ln.split("\\W+");
        flen += words.length;
        
        int currentPos = startPosition;

        for (String word : words) {
            word = word.toLowerCase();
            
            if (stopWord(word)) {
                currentPos++; // Stop words take up a positional slot!
                continue;
            }
            
            word = stemWord(word);
            
            // If the word is not in the dictionary, add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            
            // If this is the first time the word appears in this document
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; 
                
                Posting newPosting = new Posting(fid);
                newPosting.addPosition(currentPos); // Track position
                
                if (index.get(word).pList == null) {
                    index.get(word).pList = newPosting;
                    index.get(word).last = newPosting;
                } else {
                    index.get(word).last.next = newPosting;
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                // Word already exists in this doc, just append the new position
                index.get(word).last.dtf += 1;
                index.get(word).last.addPosition(currentPos); 
            }
            
            index.get(word).term_freq += 1;
            currentPos++; // Increment position for the next word
        }
        return flen;
    }
//----------------------------------------------------------------------------  
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
//----------------------------------------------------------------------------  

    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------  
 /**
     * Intersects two positional posting lists to evaluate a phrase query.
     * Uses two-pointer logic at the document level, and nested two-pointer 
     * logic at the position level to check for exact word adjacency.
     * @param pL1 The first posting list.
     * @param pL2 The second posting list.
     * @return A new Posting list containing documents where the terms appear adjacently.
     */
    Posting intersect(Posting pL1, Posting pL2) {
        Posting answer = null;
        Posting last = null;
        
        while (pL1 != null && pL2 != null) {
            
            // Document Match found
            if (pL1.docId == pL2.docId) {
                
                ArrayList<Integer> pos1 = pL1.getPositions();
                ArrayList<Integer> pos2 = pL2.getPositions();
                ArrayList<Integer> matchedPositions = new ArrayList<>();
                
                // Nested two-pointers to find adjacent positions
                int i = 0, j = 0;
                while (i < pos1.size() && j < pos2.size()) {
                    int p1 = pos1.get(i);
                    int p2 = pos2.get(j);
                    
                    if (p2 == p1 + 1) { 
                        matchedPositions.add(p2); // Store p2 to chain longer phrases
                        i++;
                        j++;
                    } else if (p2 < p1 + 1) {
                        j++;
                    } else {
                        i++; 
                    }
                }
                
                // If adjacent positions were found, add to the result list
                if (!matchedPositions.isEmpty()) {
                    Posting newNode = new Posting(pL1.docId);
                    for (int pos : matchedPositions) {
                        newNode.addPosition(pos);
                    }
                    
                    if (answer == null) {
                        answer = newNode;
                        last = newNode;
                    } else {
                        last.next = newNode;
                        last = newNode;
                    }
                }
                
                pL1 = pL1.next;
                pL2 = pL2.next;
            } 
            else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            } else {
                pL2 = pL2.next;
            }
        }
        return answer;
    }



    public String find_24_01(String phrase) { 
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;
        if (len == 0) return result;
        
        String firstWord = words[0].toLowerCase();
        
        if (!index.containsKey(firstWord)) {
            return "Phrase not found in collection.\n";
        }
        
        Posting posting = index.get(firstWord).pList;
        int i = 1;
        while (i < len) {
            String nextWord = words[i].toLowerCase();
            
            // EDGE CASE FIX: Check if subsequent words exist
            if (!index.containsKey(nextWord)) {
                return "Phrase not found in collection.\n"; 
            }
            
            posting = intersect(posting, index.get(nextWord).pList);
            i++;
        }
        
        if (posting == null) {
            return "Phrase not found in collection.\n";
        }
        
        while (posting != null) {
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
    }
    
    
    //---------------------------------
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

     //---------------------------------

    public void store(String storageName) {
        try {
            String pathToStorage = "../../Material/tmp11/rl/" + storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//=========================================    
    public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("../../Material/tmp11/rl/" + storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
            
    }
//----------------------------------------------------    
    public void createStore(String storageName) {
        try {
            String pathToStorage = "../../Material/tmp11/rl/" + storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//----------------------------------------------------      
     //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = "../../Material/tmp11/rl/" + storageName;         
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
}

//=====================================================================
