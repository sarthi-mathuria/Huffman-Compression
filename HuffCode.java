public class HuffCode {
    private final int NUM_BITS;
    private final int VALUE;

    /**
     * Constructor for HuffCode.
     * 
     * @param stringCode the string representation of the HuffCode
     */
    public HuffCode(String stringCode) {
        this.NUM_BITS = stringCode.length();
        this.VALUE = Integer.parseInt(stringCode, 2);
    }

    /**
     * Returns the number of bits in the code.
     * 
     * @return the numBits
     */
    public int getNumBits() {
        return NUM_BITS;
    }

    /**
     * Returns the value of the code.
     * 
     * @return the value
     */
    public int getValue() {
        return VALUE;
    }
}
