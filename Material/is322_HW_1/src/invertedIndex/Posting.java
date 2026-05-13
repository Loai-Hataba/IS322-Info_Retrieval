
package invertedIndex;

import java.util.ArrayList;

/**
 *
 * @author ehab
 */
 
public class Posting {

    public Posting next = null;
    int docId;
    int dtf = 1;
    ArrayList<Integer> positions = new ArrayList<Integer>();

    Posting(int id, int t) {
        docId = id;
        dtf=t;
    }
    
    Posting(int id) {
        docId = id;
    }
    
    void addPosition(int position) {
        positions.add(position);
    }
    
    ArrayList<Integer> getPositions() {
        return positions;
    }
}