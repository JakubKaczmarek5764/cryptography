import javax.swing.*;
import javax.swing.tree.ExpandVetoException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;

public class DES {

    byte[] byteKey;
    // Tabela dla początkowej permutacji
    private final byte[] IP =
            {
                    58, 50, 42, 34, 26, 18, 10, 2,
                    60, 52, 44, 36, 28, 20, 12, 4,
                    62, 54, 46, 38, 30, 22, 14, 6,
                    64, 56, 48, 40, 32, 24, 16, 8,
                    57, 49, 41, 33, 25, 17, 9, 1,
                    59, 51, 43, 35, 27, 19, 11, 3,
                    61, 53, 45, 37, 29, 21, 13, 5,
                    63, 55, 47, 39, 31, 23, 15, 7
            };

    // Tabela dla końcowej permutacji
    private final byte[] IPi =
            {
                    40, 8, 48, 16, 56, 24, 64, 32,
                    39, 7, 47, 15, 55, 23, 63, 31,
                    38, 6, 46, 14, 54, 22, 62, 30,
                    37, 5, 45, 13, 53, 21, 61, 29,
                    36, 4, 44, 12, 52, 20, 60, 28,
                    35, 3, 43, 11, 51, 19, 59, 27,
                    34, 2, 42, 10, 50, 18, 58, 26,
                    33, 1, 41, 9, 49, 17, 57, 25
            };

    // Tabela permutacji klucza
    private final byte[] PC1 =
            {
                    57, 49, 41, 33, 25, 17, 9,
                    1, 58, 50, 42, 34, 26, 18,
                    10, 2, 59, 51, 43, 35, 27,
                    19, 11, 3, 60, 52, 44, 36,
                    63, 55, 47, 39, 31, 23, 15,
                    7, 62, 54, 46, 38, 30, 22,
                    14, 6, 61, 53, 45, 37, 29,
                    21, 13, 5, 28, 20, 12, 4
            };

    // Tabela do permutacji złączonych podkluczy
    private final byte[] PC2 =
            {
                    14, 17, 11, 24, 1, 5,
                    3, 28, 15, 6, 21, 10,
                    23, 19, 12, 4, 26, 8,
                    16, 7, 27, 20, 13, 2,
                    41, 52, 31, 37, 47, 55,
                    30, 40, 51, 45, 33, 48,
                    44, 49, 39, 56, 34, 53,
                    46, 42, 50, 36, 29, 32
            };

    // Tabela do rozszerzenia 32bit bloku Rn-1 do 48bit
    private final byte[] E_BIT = {
            32, 1, 2, 3, 4, 5,
            4, 5, 6, 7, 8, 9,
            8, 9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32, 1
    };
    private final byte[][] S_BOXES = {
            {
                    14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7
                    , 0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8
                    , 4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0
                    , 15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13
            },
            {
                    15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10
                    , 3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5
                    , 0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15
                    , 13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9
            },
            {
                    10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8
                    , 13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1
                    , 13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7
                    , 1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12
            },
            {
                    7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15
                    , 13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9
                    , 10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4
                    , 3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14
            },
            {
                    2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9
                    , 14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6
                    , 4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14
                    , 11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3
            },
            {
                    12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11
                    , 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8
                    , 9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6
                    , 4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13
            },
            {
                    4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1
                    , 13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6
                    , 1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2
                    , 6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12
            },
            {
                    13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7
                    , 1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2
                    , 7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8
                    , 2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11
            }
    };

    // Tabela do permutacji wyniku Sboxow
    private final byte[] P = {
            16, 7, 20, 21
            , 29, 12, 28, 17
            , 1, 15, 23, 26
            , 5, 18, 31, 10
            , 2, 8, 24, 14
            , 32, 27, 3, 9
            , 19, 13, 30, 6
            , 22, 11, 4, 25
    };

    public byte[][] subKeys() {
        byte[] pKey = new byte[7];
        byte[] leftKey = new byte[4];
        byte[] rightKey = new byte[4]; // z 28 bitow, z przodu zera


        for (int i = 0; i < 28; i++) {
            setBit(pKey, i, getBit(byteKey, PC1[i] - 1));


            setBit(leftKey, i + 4, getBit(pKey, i));


        }
        for (int i = 28; i < 56; i++) {
            setBit(pKey, i, getBit(byteKey, PC1[i] - 1));
            setBit(rightKey, i - 24, getBit(pKey, i));
        }

        final byte[] shift = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
        byte[][] subKeys = new byte[16][];
        int currentShift = 0;
        for (int i = 0; i < 16; i++) {
            currentShift += shift[i];
            subKeys[i] = rearrange(joinPair(leftShift(leftKey, currentShift, 4), leftShift(rightKey, currentShift, 4), 4), 48, PC2);

        }


        return subKeys;
    }

    public byte[] encodeBlock(byte[] M) {
        byte[][] subKeys = subKeys();
        byte[] encodedM;
        byte[] ip = rearrange(M, 64, IP);

        byte[] left = slice(ip, 0, 32);
        byte[] prevLeft = slice(ip, 0, 32);
        byte[] right = slice(ip, 32, 64);
        for (int i = 0; i < 16; i++) {
            bitArrayCopy(right, 0, left, 0, 32);
            right = xor(prevLeft, func(right, subKeys[i]));

            bitArrayCopy(left, 0, prevLeft, 0, 32);
        }
        byte[] RL = joinPair(right, left, 0);

        encodedM = rearrange(RL, 64, IPi);

        return encodedM;


    }

    public byte[] decodeBlock(byte[] encodedM) {
        byte[] M;
        byte[][] subKeys = subKeys();

        byte[] ip = rearrange(encodedM, 64, IP);
        byte[] left = slice(ip, 0, 32);
        byte[] prevLeft = slice(ip, 0, 32);
        byte[] right = slice(ip, 32, 64);
        for (int i = 15; i >= 0; i--) {
            bitArrayCopy(right, 0, left, 0, 32);
            right = xor(prevLeft, func(right, subKeys[i]));
            bitArrayCopy(left, 0, prevLeft, 0, 32);
        }
        byte[] RL = joinPair(right, left, 0);
        M = rearrange(RL, 64, IPi);
        return M;
    }

    public void bitArrayCopy(byte[] src, int srcPos, byte[] dest, int destPos, int len) {
        for (int i = 0; i < len; i++) {
            setBit(dest, destPos + i, getBit(src, srcPos + i));
        }
    }

    private int roundTo(int val, int to) {
        return (int) Math.ceil(val / (double) to) * to;
    }

    public void printBits(byte[] arr, int split) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < arr.length * 8; i++) {
            if (i % split == 0) str.append(" ");
            str.append(getBit(arr, i));

        }
        System.out.println(str);
    }

    public void setBit(byte[] arr, int pos, int val) {
        if (val == 1) {
            arr[pos / 8] |= (byte) (1 << (7 - pos % 8));
        } else {
            arr[pos / 8] &= (byte) ~(1 << (7 - pos % 8));
        }
    }

    public int getBit(byte[] arr, int pos) {
        byte tmpByte = arr[pos / 8];
        tmpByte &= (byte) (1 << (7 - pos % 8));
        return ((tmpByte & 0xff) >> (7 - pos % 8)); // tak sie robi zeby uzyskac unsigned inta src: https://mkyong.com/java/java-convert-bytes-to-unsigned-bytes/
    }
    private byte[] leftShift(byte[] arr, int places, int offset) {

        byte[] shiftedArr = new byte[arr.length];

        int real_len = arr.length * 8 - offset;
        for (int i = 0; i < real_len; i++) {

            setBit(shiftedArr, i + offset, getBit(arr, offset + ((i + places) % (real_len))));
        }

        return shiftedArr;
    }

    private byte[] joinPair(byte[] left, byte[] right, int offset) {
        int fullKeyLen = ((left.length * 8 + right.length * 8) - offset * 2) / 8;
        byte[] fullKey = new byte[fullKeyLen];
        for (int i = 0; i < left.length * 8 - offset; i++) {
            setBit(fullKey, i, getBit(left, i + offset));
        }
        for (int i = 0; i < right.length * 8 - offset; i++) {
            setBit(fullKey, i + left.length * 8 - offset, getBit(right, i + offset));
        }
        return fullKey;
    }

    private byte[] rearrange(byte[] arr, int goalLength, byte[] table) { //goal length w bitach
        byte[] out = new byte[goalLength / 8];
        for (int i = 0; i < goalLength; i++) {
            setBit(out, i, getBit(arr, table[i] - 1));
        }
        return out;
    }

    private byte[] slice(byte[] arr, int start, int end) { // start inclusive end exclusive, start i end w bitach
        int offset = 0;
        if ((end - start) % 8 != 0) {
            offset = 8 - ((end - start) % 8);
        }
        byte[] out = new byte[roundTo(end - start, 8) / 8];
        for (int i = 0; i < end - start; i++) {
            setBit(out, i + offset, getBit(arr, i + start));
        }
        return out;
    }

    private byte[] xor(byte[] arr1, byte[] arr2) {


        byte[] out = new byte[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            out[i] = (byte) (arr1[i] ^ arr2[i]);
        }
        return out;
    }

    private byte[] dec2bin(int dec, int len) { // len w bajtach
        if (len == 0) {
            int numOfBits = 1;
            int tmpVal = 1;
            while (tmpVal < dec) {
                tmpVal *= 2;
                numOfBits += 1;
            }
            len = (numOfBits + (8 - numOfBits % 8)) / 8; // zaokraglone do bajtow
        }
        byte[] bin = new byte[len];
        int val = power(2, len * 8 - 1);
        for (int i = 0; i < len * 8; i++) {
            if (dec - val >= 0) {
                setBit(bin, i, 1);
                dec -= val;
            }
            val /= 2;
        }
        return bin;

    }

    private int bin2dec(byte[] bin) {
        int dec = 0;
        for (int i = 0; i < bin.length * 8; i++) {
            dec += getBit(bin, i) * power(2, bin.length * 8 - i - 1);
        }
        return dec;
    }

    private int power(int base, int x) {
        int out = 1;
        for (int i = 0; i < x; i++) {
            out *= base;
        }
        return out;
    }

    private byte[] func(byte[] right, byte[] k) {
        byte[] out;
        byte[] expandedRight = rearrange(right, 48, E_BIT);

        byte[] K_ER = xor(k, expandedRight);
        out = s_boxes_function(K_ER);

        return out;

    }

    private byte[] s_boxes_function(byte[] K_ER) {

        byte[] out = new byte[4];
        for (int i = 0; i < 8; i++) {
            int B_start_index = i * 6;
            int B_end_index = i * 6 + 5;
            int row_index = getBit(K_ER, B_start_index) * 2 + getBit(K_ER, B_end_index);

            int col_index = bin2dec(slice(K_ER, B_start_index + 1, B_end_index));


            byte[] s_box_output = dec2bin(S_BOXES[i][row_index * 16 + col_index], 1);

            // przepisywanie do outa

            bitArrayCopy(s_box_output, 4, out, i * 4, 4);
        }

        return rearrange(out, 32, P);
    }

    public byte[] encrypt(byte[] message) {
        int len = roundTo(message.length, 8);
        byte[] out = new byte[len];
        for (int i = 0; i < len / 8; i++) {
            byte[] block = new byte[8];
            bitArrayCopy(message, i * 64, block, 0, Math.min(message.length * 8 - i * 64, 64));
            bitArrayCopy(encodeBlock(block), 0, out, i * 64, 64);
        }
        return out;
    }

    public byte[] decrypt(byte[] encryptedMessage) {
        byte[] out = new byte[encryptedMessage.length];
        for (int i = 0; i < encryptedMessage.length / 8; i++) {
            byte[] block = new byte[8];
            bitArrayCopy(encryptedMessage, i * 64, block, 0, 64);
            bitArrayCopy(decodeBlock(block), 0, out, i * 64, 64);
        }
        int num_of_zeroes = 0;
        while (out[encryptedMessage.length - num_of_zeroes - 1] == 0) num_of_zeroes++;
        byte[] out1 = new byte[encryptedMessage.length - num_of_zeroes];
        System.arraycopy(out, 0, out1, 0, encryptedMessage.length - num_of_zeroes);
        return out1;
    }
    public String binHex(byte[] arr) {
        HexFormat hex = HexFormat.of();
        return hex.formatHex(arr);
    }
    public byte[] hexBin(String str) {
        HexFormat hex = HexFormat.of();
        return hex.parseHex(str);
    }
}

