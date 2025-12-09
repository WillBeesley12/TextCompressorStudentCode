/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, William Beesley
 */
public class TextCompressor {
    public static final int EOF = 128;

    private static void compress(int BITS) {
        String input = BinaryStdIn.readString();
        TST keys = new TST();
        int iterator = 0;
        int length = input.length();
        String prefix;
        int code;
        int key_number = EOF + 1;
        int max = (int) Math.pow(2, BITS);
        while (iterator < length) {
            // Start by finding the longest prefix to save bits and the corresponding code
            prefix = keys.getLongestPrefix(input, iterator);
            code = keys.lookup(prefix);
            // Output the code
            BinaryStdOut.write(code, BITS);
            // Look ahead to create the new codes
            iterator += prefix.length();
            // Stay in bounds since we just changed iterator
            if (iterator >= length) {
                break;
            }
            if (key_number < max) {
                keys.insert(prefix + input.charAt(iterator), key_number);
            }
            key_number++;
        }
        BinaryStdOut.write(EOF, BITS);
        BinaryStdOut.close();
    }

    private static void expand(int BITS) {
        // Max number of keys is 2 ^ BITS - 128 so just use 2 ^ BITS to be safe
        String[] keys = new String[(int) Math.pow(2, BITS)];
        int key_number = EOF + 1;
        int max = (int) Math.pow(2, BITS);
        // Add ASCII to the map
        for (int i = 0; i < 128; i++) {
            keys[i] = (char) (i) + "";
        }
        int input = BinaryStdIn.readInt(BITS);
        while (input != EOF) {
            // Write out the string
            String str1 = keys[input];
            BinaryStdOut.write(str1);
            // Look ahead to the next string
            int next_input = BinaryStdIn.readInt(BITS);
            String str2 = keys[next_input];
            // Edge case! Next string isn't yet in our map
            if (str2 == null) {
                str2 = str1 + str1.charAt(0);
            }
            // Add new code to the map, only include first character of the next string
            if (key_number < max) {
                if (next_input != EOF) {
                    keys[key_number] = keys[input] + str2.charAt(0);
                }
                key_number++;
            }
            input = next_input;
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        int BITS = Integer.parseInt(args[1]);
        if      (args[0].equals("-")) compress(BITS);
        else if (args[0].equals("+")) expand(BITS);
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
