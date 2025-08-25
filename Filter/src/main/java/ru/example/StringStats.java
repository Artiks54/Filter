package ru.example;

public class StringStats {
    long count = 0;
    long minLen = Long.MAX_VALUE;
    long maxLen = 0;

    public void add(String s) {
        count++;
        int len = s.length();
        if (len < minLen) {
            minLen = len;
        }
        if (len > maxLen) {
            maxLen = len;
        }
    }
}