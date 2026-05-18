package com.berat.reboot.algorithms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IntersectionOfTwoArrays {

    /**
     * Time Complexity:  O(n + m) — one pass through each array
     * Space Complexity: O(n + m) — two sets, one per array
     *
     * I put all elements of nums1 into a HashSet.
     * Then I iterate over nums2 — if an element exists in the first set, it's a common element.
     * I add it to a second HashSet to automatically handle duplicates in the output.
     * Finally I convert the result set to int[] using streams.
     */
    public int[] intersection(int[] nums1, int[] nums2) {

        // load nums1 into a set for O(1) lookup
        Set<Integer> set = new HashSet<>();
        for (int num : nums1) set.add(num);

        // collect common elements — HashSet handles deduplication automatically
        Set<Integer> set2 = new HashSet<>();
        for (int num : nums2) {
            if (set.contains(num)) set2.add(num);
        }

        return set2.stream().mapToInt(i -> i).toArray();
    }

    public static void main(String[] args) {
        IntersectionOfTwoArrays solution = new IntersectionOfTwoArrays();

        System.out.println(Arrays.toString(solution.intersection(new int[]{1, 2, 2, 1}, new int[]{2, 2})));       // [2]
        System.out.println(Arrays.toString(solution.intersection(new int[]{4, 9, 5}, new int[]{9, 4, 9, 8, 4}))); // [4, 9]
        System.out.println(Arrays.toString(solution.intersection(new int[]{1, 2, 3}, new int[]{4, 5, 6})));       // []
    }
}