import java.util.ArrayList;
import java.util.List;

public class QTest {
    public static void main(String[] args) {

        PQ<QItem<Integer>> pq = new PQ<>();

        //This generates values to be added to the queue
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 5; k++) {
                list.add(i + k);
            }
        }

        //This creates a list of QItems, with each with the proper rank.
        //This results in a list 'sorted' by rank (ascending).
        //Because it is sorted by rank, when you add, all the ranks should be ascending
        //But not all the values are in prioritized order, so it also tests correct prioritization
        List<QItem> rankedList = QItem.ranker(list);

        for (QItem item : rankedList) {
            System.out.println(item);
            pq.enqueue(item);
        }


        System.out.println("\nOrder Of Insertion: " + rankedList);
        System.out.println("\nOrder Of Your Queue: ");
        System.out.println(pq);
        System.out.println("\nAll values should be together, and all sub_ numbers should be in " +
                "order.");

    }



}
