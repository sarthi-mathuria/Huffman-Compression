

import java.io.IOException;

public class HuffTree {
    private final TreeNode ROOT; // the tree is immutable, so we can use final

    /**
     * Create a tree from the array of frequencies
     * 
     * @param freqs array of frequencies
     */
    public HuffTree(int[] freqs) {
        ROOT = createTreeFromFreqs(freqs);
    }

    /**
     * Create a tree using a priority queue with values and frequencies.
     * 
     * @param freqs array of frequencies
     * @return the root node of the tree
     */
    private TreeNode createTreeFromFreqs(int[] freqs) {
        // add the frequencies to the queue
        PQ<TreeNode> queue = new PQ<>();

        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] > 0) {
                queue.enqueue(new TreeNode(i, freqs[i]));
            }
        }

        // PEOF added
        queue.enqueue(new TreeNode(IHuffConstants.PSEUDO_EOF, 1));

        // create a tree from the queue
        while (!queue.isSizeOne()) {
            TreeNode left = queue.dequeue();
            TreeNode right = queue.dequeue();
            // the value of a non-leaf node is the size of the subtrees at the node
            TreeNode parent = new TreeNode(left, (left.isLeaf() ? 1 : left.getValue())
                    + (right.isLeaf() ? 1 : right.getValue()) + 1, right);
            queue.enqueue(parent);
        }

        return queue.dequeue(); // only one node left, and it is the root
    }

    /**
     * Create a tree from an STF representation of a tree in the form of a BitInputStream
     * 
     * @param bitsIn the BitInputStream to read from
     */
    public HuffTree(BitInputStream bitsIn) throws IOException {
        ROOT = readSTF(bitsIn);
    }

    /**
     * Create a tree using STF.
     * 
     * @param bitsIn input stream to read data from
     * @return the root of the tree represented by the data
     * @throws IOException
     */
    private TreeNode readSTF(BitInputStream bitsIn) throws IOException {
        // if the next bit represents a parent
        if (bitsIn.readBits(1) == 0) {
            TreeNode node = new TreeNode(-1, -1); // all freq = -1 because they no longer matter
            node.setLeft(readSTF(bitsIn));
            node.setRight(readSTF(bitsIn));
            return node;
        }
        // otherwise the bit we read represents a leaf
        else {
            int val = bitsIn.readBits(IHuffConstants.BITS_PER_WORD + 1);
            return new TreeNode(val, -1); // all freq = -1 because they no longer matter
        }
    }

    /**
     * Create a HuffCode[] of codes representing each leaf in the tree.
     * 
     * @return the array of HuffCode filled with the codes
     */
    public HuffCode[] createCodes() {
        return createCodes(new HuffCode[IHuffConstants.ALPH_SIZE + 1], ROOT, ""); // plus 1 for EOF
    }

    /**
     * Create a HuffCode[] of codes representing each leaf in the tree.
     * 
     * @param codes the array of codes to be filled in
     * @param node the node we are at
     * @param code the current code we are building
     * @return the filled in codes array
     */
    private HuffCode[] createCodes(HuffCode[] codes, TreeNode node, String code) {
        // if the node is a leaf --> the code is complete and we can store it
        if (node.isLeaf()) {
            codes[node.getValue()] = new HuffCode(code);
        }
        // otherwise recurse down child trees checking for leaves
        else {
            createCodes(codes, node.getLeft(), code + "0");
            createCodes(codes, node.getRight(), code + "1");
        }

        return codes;
    }

    /**
     * Write the new tree to the output stream.
     * 
     * @param bitsOut the BitOutputStream to write to
     * @return the total # of bits writen
     */
    public int writeTree(BitOutputStream bitsOut) {
        return writeTree(bitsOut, ROOT);
    }

    /**
     * Writes the subtree at node to bitsOut and return the total # of bits written
     * 
     * @param bitsOut the BitOutputStream to write to
     * @param node the subtree we are printing
     * @return the total # of bits writen
     */
    private int writeTree(BitOutputStream bitsOut, TreeNode node) {
        if (node != null) {
            int bitsWritten = 1;
            // if we find a leaf, write it in a BITS_PER_WORD + 1 integer
            if (node.isLeaf()) {
                bitsOut.writeBits(1, 1);
                bitsOut.writeBits(IHuffConstants.BITS_PER_WORD + 1, node.getValue());
                bitsWritten += IHuffConstants.BITS_PER_WORD + 1; // add the written bit
            } else {
                // preorder: this --> left --> right
                bitsOut.writeBits(1, 0);
                bitsWritten += writeTree(bitsOut, node.getLeft());
                bitsWritten += writeTree(bitsOut, node.getRight());
            }
            // the total number of written bits in the recursion
            return bitsWritten;
        } else {
            return 0;
        }
    }

    /**
     * Get the number of bits needed to represent a tree
     * 
     * @return the number of bits that would be used to write the tree in STF
     */
    public int bitsOfTreeRepresentation(int[] freqs) {
        int bits = 0; // 1 per node, + 9 per leaf

        // # of nodes is size of the tree which is the value of the root
        bits += ROOT.getValue();

        // add 9 for every leaf, if freq > 0 --> is a leaf
        for (int freq : freqs) {
            if (freq > 0) {
                bits += 9;
            }
        }

        // (and one more for PEOF)
        bits += 9;

        return bits;
    }

    /**
     * Read a compressed file and use this tree's data to decode it and write the decoded data.
     * 
     * @param bitsIn the BitInputStream to read from
     * @param bitsOut the BitOutputStream to write to
     * @return the total # of bits written
     * @throws IOException
     */
    public int decode(BitInputStream bitsIn, BitOutputStream bitsOut) throws IOException {
        // get ready to walk tree, start at root
        boolean done = false;
        TreeNode curr = ROOT;
        int bitsWritten = 0;
        while (!done) {
            int bit = bitsIn.readBits(1);
            if (bit == -1) {
                throw new IOException("Error reading compressed file. "
                        + "\n unexpected end of input. No PSEUDO_EOF value.");
            } else {
                // move left or right in tree based on value of bit
                if (bit == 0) {
                    curr = curr.getLeft();
                } else {
                    curr = curr.getRight();
                }

                if (curr.isLeaf()) {
                    if (curr.getValue() == IHuffConstants.PSEUDO_EOF) {
                        done = true;
                    } else {
                        // write out value in leaf to output
                        bitsOut.writeBits(IHuffConstants.BITS_PER_WORD, curr.getValue());
                        bitsWritten += IHuffConstants.BITS_PER_WORD;
                        // go back to root of tree
                        curr = ROOT;
                    }
                }
            }
        }

        return bitsWritten;
    }

}
