package com.creazione.space_learning.utils;

public class BitUtils {

    public static String toBinaryString(long number, int bits) {
        return String.format("%" + bits + "s", Long.toBinaryString(number))
                .replace(' ', '0');
    }

    public static void printBits(long number, int bits, String label) {
        System.out.println(label + ": " + toBinaryString(number, bits));
    }

    public static void debugOperation(long before, long after, int bits, String operation) {
        System.out.println("=== " + operation + " ===");
        printBits(before, bits, "До");
        printBits(after, bits, "После");
        System.out.println();
    }
}
