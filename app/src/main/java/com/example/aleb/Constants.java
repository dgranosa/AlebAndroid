package com.example.aleb;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static String SERVER_IP = "aleb.mat1jaczyyy.com";
    public static int SERVER_PORT = 11252;
    public static int VERSION = 7;
    public static String USERNAME;

    public static String stringJoin(String delimiter, List<String> l) {
        String s = "";
        if (l.size() == 0)
            return s;

        s += l.get(0);
        for (int i = 1; i < l.size(); i++) {
            s += delimiter;
            s += l.get(i);
        }

        return s;
    }

    public static String stringJoin(String delimiter, String[] l) {
        return stringJoin(delimiter, Arrays.asList(l));
    }
}
