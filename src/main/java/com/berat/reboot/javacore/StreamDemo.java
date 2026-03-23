package com.berat.reboot.javacore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamDemo {

    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);

        List<Integer> result = list.stream()
                .filter(x -> x > 3)
                .map(x -> x * 2)
                .collect(Collectors.toList());

        System.out.println(result);
    }
}
