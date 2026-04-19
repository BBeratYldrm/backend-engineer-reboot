package com.berat.reboot.algorithms;

public class FizzBuzz {

    public String returnFizzBuzz() {

        StringBuilder result = new StringBuilder();

        for (int i = 1; i <= 100; i++) {

            if (i % 3 == 0 && i % 5 == 0) {
                result.append("FizzBuzz");
            } else if (i % 3 == 0) {
                result.append("Fizz");
            } else if (i % 5 == 0) {
                result.append("Buzz");
            } else {
                result.append(i);
            }
        }
        return result.toString();
    }
}
