

import java.util.NoSuchElementException;

public class PQ<E extends Comparable<E>> {
    private final Node HEADER;

    /**
     * Constructor for PQ
     */
    public PQ() {
        HEADER = new Node(null, null);
    }

    /**
     * Adds a given element to the priority queue.
     * 
     * @param element E to be added, element != null
     */
    public void enqueue(E element) {
        if (element == null) {
            throw new IllegalArgumentException("element cannot be null");
        }

        // search for the target solution, not updating if tied
        Node curr = HEADER;
        boolean inserted = false;

        while (!inserted && curr.next != null) {
            // look for the closest element greater than the given
            if (curr.next.VALUE.compareTo(element) > 0) {
                curr.next = new Node(element, curr.next);
                inserted = true;
            }

            curr = curr.next;
        }

        // !inserted --> insert at end
        if (!inserted) {
            curr.next = new Node(element);
        }
    }

    /**
     * Removes and returns the element with the lowest priority. Cannot be used on empty queue.
     * 
     * @return The element with the lowest priority.
     */
    public E dequeue() {
        if (HEADER.next.equals(null)) {
            throw new NoSuchElementException("Cannot call dequeue on an empty PQ");
        }

        // remove the first element and return it
        E result = HEADER.next.VALUE;
        HEADER.next = HEADER.next.next; // fix HEADER reference

        return result;
    }

    /**
     * Tells us if the queue has a size of one.
     */
    public boolean isSizeOne() {
        // size is one iff HEADER.next.next is null, check if HEADER.next is !null to avoid errors
        return HEADER.next == null ? false : HEADER.next.next == null;
    }

    /**
     * Returns a String representation of the Priority Queue in the form [item1, item2, ... itemN]
     */
    @Override
    public String toString() {
        // handle case of empty queue
        if (HEADER.next == null) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(HEADER.next.VALUE); // fix fencepost problem

        Node curr = HEADER.next.next;

        // iterate through the rest of the queue adding values
        while (curr != null) {
            builder.append(", ");
            builder.append(curr.VALUE);
            curr = curr.next;
        }

        builder.append("]");

        return builder.toString();
    }

    /**
     * The internal storage container for our linked list for our priority queue.
     */
    private class Node {
        final E VALUE; // the value never changes after initialization
        Node next; // however, the next reference does

        Node(E value) {
            this(value, null);
        }

        Node(E value, Node next) {
            this.VALUE = value;
            this.next = next;
        }
    }
}
