package com.berat.reboot.algorithms;

import java.util.HashSet;
import java.util.Set;

public class ContainsDuplicate {

    public static boolean containsDuplicate(int[] nums) { //  I will take an array

        Set<Integer> set = new HashSet<>(); // I will keep in here what i see

        for  (int num : nums) { // I will start to see
            if (set.contains(num)) { // When i'm looking, i will check did i see before ?
                return true; // If i saw before, it will be on the set, so there is duplicate
            }
            set.add(num); // When i'm looking, if i didn't see before i will add that to list
        }

        return false; // I will return false if there is no duplicate
    }


    public static void main(String[] args) {
        ContainsDuplicate solution = new ContainsDuplicate();

        System.out.println(solution.containsDuplicate(new int[]{1,2,3,1})); // true
        System.out.println(solution.containsDuplicate(new int[]{1,2,3,4})); // false
    }
}