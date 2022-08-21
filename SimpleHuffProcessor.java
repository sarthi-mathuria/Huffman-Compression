

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleHuffProcessor implements IHuffProcessor {
    private static final boolean DISPLAY_UPDATES_TO_VIEWER = true;

    private IHuffViewer myViewer;

    // instance variables for precompress/compress
    private int[] freqs;
    private HuffTree tree;
    private HuffCode[] codes;
    private int headerFormat;
    private int bitsSaved;

    /**
     * Preprocess data so that compression is possible --- count characters/create tree/store state
     * so that a subsequent call to compress will work. The InputStream is <em>not</em> a
     * BitInputStream, so wrap it int one as needed.
     * 
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of header to
     *        use, standard count format, standard tree format, or possibly some format added in the
     *        future.
     * @return number of bits saved by compression or some other measure Note, to determine the
     *         number of bits saved, the number of bits written includes ALL bits that will be
     *         written including the magic number, the header format number, the header to reproduce
     *         the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        // check preconditions
        if (in == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Running preprocessCompress");
        }

        // find frequencies of each 8 bit chunk
        BitInputStream bitsIn = new BitInputStream(in);
        freqs = new int[ALPH_SIZE];
        int readBits = bitsIn.readBits(BITS_PER_WORD);

        while (readBits != -1) {
            freqs[readBits]++;
            readBits = bitsIn.readBits(BITS_PER_WORD);
        }

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("found freqs");
        }

        // create HuffTree from the freqs
        tree = new HuffTree(freqs);

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("created tree");
        }

        // store the codes of each character
        codes = tree.createCodes();

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("created codes");
        }

        // save headerFormat for later use ;P
        this.headerFormat = headerFormat;

        bitsIn.close();

        // find out how many bits will be saved by compression
        bitsSaved = calculateSavedBits(headerFormat);
        return bitsSaved;
    }

    /**
     * Find out how many bits will be saved by compression
     * 
     * @param headerFormat constant representing SCF or STF
     * @return the number of bits saved
     */
    private int calculateSavedBits(int headerFormat) {
        int compressedBits = BITS_PER_INT * 2; // store magic num and header format

        if (headerFormat == STORE_TREE) {
            // store size of tree
            compressedBits += BITS_PER_INT;
            // store size of tree representation
            compressedBits += tree.bitsOfTreeRepresentation(freqs);
        } else if (headerFormat == STORE_COUNTS) {
            // each number 0 to ALPHSIZE is stored as a 32 bit int
            compressedBits += BITS_PER_INT * ALPH_SIZE;
        }
        int uncompressedBits = 0;
        // count bits used in compressed version
        for (int i = 0; i < freqs.length; i++) {
            // we have a code iff the freq > 0
            if (freqs[i] > 0) {
                compressedBits += freqs[i] * codes[i].getNumBits();
                uncompressedBits += freqs[i] * BITS_PER_WORD;
            }
        }

        // add in the bits used to store the PEOF
        compressedBits += codes[PSEUDO_EOF].getNumBits();

        return uncompressedBits - compressedBits;
    }

    /**
     * Compresses input to output, where the same InputStream has previously been pre-processed via
     * <code>preprocessCompress</code> storing state used by this call. <br>
     * pre: <code>preprocessCompress</code> must be called before this method
     * 
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written for the compressed file (not a
     *        BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     *        If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or writing to the
     *         output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        // check preconditions
        if (in == null || out == null) {
            throw new IllegalArgumentException("Input and output streams cannot be null");
        }

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Running compress");
        }

        // if not forcing --> ensure we will save bits before compressing
        if (!force && bitsSaved <= 0) {
            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.update("Not compressing since no bits will be saved.");
            }

            return 0;
        }

        BitInputStream bitsIn = new BitInputStream(in);
        BitOutputStream bitsOut = new BitOutputStream(out);

        // write the magic number
        bitsOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Wrote magic number");
        }

        // write format (SCF vs STF)
        bitsOut.writeBits(BITS_PER_INT, headerFormat);

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Wrote const for the format of tree data");
        }

        // keep track of bits written --> BITS_PER_INT * 2 for magic number and header format
        int totalBitsWritten = BITS_PER_INT * 2;

        // write counts/tree according to format
        totalBitsWritten += writeHeaderData(bitsOut);

        // write data
        totalBitsWritten += writeCompressedData(bitsIn, bitsOut);

        bitsIn.close();
        bitsOut.close();

        return totalBitsWritten;
    }

    /**
     * Write the header data to the output stream.
     * 
     * @param bitsOut the BitOutputStream to write to
     * @param totalBitsWritten the number of bits written so far
     * @return
     */
    private int writeHeaderData(BitOutputStream bitsOut) {
        int totalBitsWritten = 0;

        // write tree data
        if (headerFormat == STORE_TREE) {
            bitsOut.writeBits(BITS_PER_INT, tree.bitsOfTreeRepresentation(freqs));

            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.update("Wrote bits of tree representation: "
                        + tree.bitsOfTreeRepresentation(freqs));
            }

            // the size is stored in a 32 bit int
            totalBitsWritten += BITS_PER_INT + tree.writeTree(bitsOut);

            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.update("Wrote tree");
            }

        }
        // write counts data
        else if (headerFormat == STORE_COUNTS) {
            // write the counts for each value 0 to ALPH_SIZE
            for (int i = 0; i < ALPH_SIZE; i++) {
                bitsOut.writeBits(BITS_PER_INT, freqs[i]);
            }

            // each number 0 to ALPHSIZE is stored as a 32 bit int
            totalBitsWritten += BITS_PER_INT * ALPH_SIZE;

            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.update("Wrote all freqs");
            }
        }

        return totalBitsWritten;
    }

    /**
     * Convert data from the inputstream to compressed data in the output stream.
     * 
     * @param bitsIn the BitInputStream to read from
     * @param bitsOut the BitOutputStream to write to
     * @return the number of bits written
     * @throws IOException
     */
    private int writeCompressedData(BitInputStream bitsIn, BitOutputStream bitsOut)
            throws IOException {
        int totalBitsWritten = 0;
        int bitsRead = bitsIn.readBits(BITS_PER_WORD);

        while (bitsRead != -1) {
            HuffCode code = codes[bitsRead];
            bitsOut.writeBits(code.getNumBits(), code.getValue());
            totalBitsWritten += code.getNumBits();

            bitsRead = bitsIn.readBits(BITS_PER_WORD);
        }

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Wrote all data using the codes");
        }

        // write PEOF
        HuffCode PEOFCode = codes[PSEUDO_EOF];
        bitsOut.writeBits(PEOFCode.getNumBits(), PEOFCode.getValue());
        totalBitsWritten += PEOFCode.getNumBits();

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Wrote PEOF - compressing complete :)");
        }

        return totalBitsWritten;
    }

    /**
     * Uncompress a previously compressed stream in, writing the uncompressed bits/data to out.
     * 
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or writing to the
     *         output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        // check preconditions
        if (in == null || out == null) {
            throw new IllegalArgumentException("Input and output streams cannot be null");
        }

        BitInputStream bitsIn = new BitInputStream(in);
        BitOutputStream bitsOut = new BitOutputStream(out);

        // check if the file is 'valid' by confirming the magic number
        if (!(bitsIn.readBits(BITS_PER_INT) == MAGIC_NUMBER)) {
            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.showError("Error reading compressed file. \n"
                        + "File did not start with the huff magic number.");
            }

            bitsIn.close();
            bitsOut.close();
            return -1;
        }

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Read and verified magic number");
        }

        // check if we are using SCF or STF and create the appropriate tree
        HuffTree newTree = createTreeFromData(bitsIn);

        // read bits and use the tree to convert to original data

        int bitsWritten = newTree.decode(bitsIn, bitsOut);

        if (DISPLAY_UPDATES_TO_VIEWER) {
            myViewer.update("Read codes and regenerated the original - uncompressing complete :)");
        }

        bitsIn.close();
        bitsOut.close();
        return bitsWritten;
    }

    /**
     * Create a HuffTree from the data in the input stream.
     * 
     * @param bitsIn the BitInputStream to read from
     * @return the HuffTree created from the data in the input stream
     * @throws IOException
     */
    private HuffTree createTreeFromData(BitInputStream bitsIn) throws IOException {
        HuffTree newTree = null;

        // read the header format and use STF or SCF to create the tree accordingly
        int format = bitsIn.readBits(BITS_PER_INT);
        if (format == STORE_TREE) {
            // read # of bits val from data
            int numOfBitsForTreeRepresentation = bitsIn.readBits(BITS_PER_INT);

            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.update("Read num bits of uncompressing tree representation "
                        + numOfBitsForTreeRepresentation);
            }

            // create HuffTree from the data
            newTree = new HuffTree(bitsIn);

            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.update("Read and created tree from STF data");
            }

        } else if (format == STORE_COUNTS) {
            // create a new tree based on the frequencies of the values
            int[] tempFreqs = new int[ALPH_SIZE];

            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                tempFreqs[k] = bitsIn.readBits(BITS_PER_INT);
            }

            // create HuffTree from freqs
            newTree = new HuffTree(tempFreqs);

            if (DISPLAY_UPDATES_TO_VIEWER) {
                myViewer.update("Read freqs and created tree from SCF data");
            }
        }

        return newTree;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }
}
