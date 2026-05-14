package com.berat.reboot.algorithms;

import java.util.HashSet;
import java.util.Set;

public class PairWithDifference {

    public boolean hasPairWithDifference(int[] nums, int k) {

        Set<Integer> set = new HashSet<>();
        for (int num : nums) {
            set.add(num);
            int needed = k + num;
            if (set.contains(needed) || set.contains(num - k)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        PairWithDifference solution = new PairWithDifference();

        System.out.println(solution.hasPairWithDifference(new int[]{1, 5, 3, 4, 2}, 2)); // true
        System.out.println(solution.hasPairWithDifference(new int[]{1, 5, 3}, 7));        // false
    }
}