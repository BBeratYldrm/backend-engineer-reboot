package com.berat.reboot.javacore;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionalInterfacesDemo {

    public static void main(String[] args) {
        // Predicate condition test
        Predicate<String> isLong = s -> s.length() > 5;
        System.out.println(isLong.test("Hi"));
        System.out.println(isLong.test("Hello World"));

        // Function — transformation
        Function<String, Integer> strToLength = s -> s.length();
        System.out.println(strToLength.apply("hello")); // 5

        // Consumer — consumes, returns nothing
        Consumer<String> printer = s -> System.out.println(">> " + s);
        printer.accept("Berat"); // >> Berat

        // Supplier — produces, takes nothing
        Supplier<String> greeting = () -> "Hello World";
        System.out.println(greeting.get()); // Hello World
    }
}
