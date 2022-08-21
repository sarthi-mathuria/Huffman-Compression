import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class QItem<E extends Comparable<? super E>> implements Comparable<QItem<E>> {
    //The data being stored
    private E data;

    //The "rank" of a variable, used to check if the queue is fair
    final private int rank;
    //vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv//
    // IMPORTANT: Rank isn't dynamic! if you insert a QItem with rank 2, and then another        //
    // with the same data and rank 1, they should show up in a queue in order 2, 1 to stay fair. //
    // In these tests, items are always added in rank-order, but keep this in mind if you choose //
    // to use this QItem for other tests that you write.                                         //
    //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^//

    /* Construct a QItem with a specific rank
     * pre: newData != null
     * post: a new QItem
     */
    public QItem(E newData, int newRank) {
        if(newData == null) {
            throw new IllegalArgumentException("QItems cannot store null values.");
        }
        data = newData;
        rank = newRank;
    }

    /* Construct a QItem with rank 0
     * pre: newData != null
     * post: a new QItem
     */
    public QItem(E newData) {
        //Precon ensured by other constructor
        this(newData, 0);
    }

    /* Returns a string with the data and the rank
     * pre: none
     * post: [data sub_rank]
     */
    public String toString() {
        return "[" + data + " sub_" + rank + "]";
    }

    /* Compare to QItems for insertion in a priority queue
     * pre: o != null
     * post: negative if this data is less than the other's data, positive if it is greater, and
     *       zero if it is equal
     *
     *    ----> IMPORTANT: DOES NOT COMPARE BASED ON RANK <----
     */
    public int compareTo(QItem<E> o) {
        return data.compareTo(o.data);
    }


    /* Assigns ranks to all the values in a list, with the first instance of a value being rank 1,
     * and the second instance being rank 2, third being 3, etc. etc.
     * pre: list != null
     * post: A list with QItems with proper ranks, for insertion into Priority Queue
     */
    static public List<QItem> ranker(List<?> list) {
        if(list == null) {
            throw new IllegalArgumentException("Ranker cannot rank a list that doesn't exist, " +
                    "input list must not be null.");
        }
        //The output
        List<QItem> result = new ArrayList();

        //A map to track how the current rank for each object in the list
        HashMap<Object, Integer> seen = new HashMap();


        for (Object o : list) {
            int rank = 0;
            //If we've seen this object before, get it's rank
            if (seen.containsKey(o)) {
                rank = seen.get(o);
            }

            //We've now seen it one more time, so increment rank
            rank++;

            //Update the map with the new rank
            seen.put(o, rank);
            //Add a QItem to the output with the correct value and rank
            result.add(new QItem((Comparable) o, rank));
        }

        return result;
    }


}

