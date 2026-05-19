package com.berat.reboot.algorithms;

public class MergeSortedArray {

    /**
     * Time Complexity:  O(m + n) — single pass through both arrays
     * Space Complexity: O(1) — in-place, no extra space
     *
     * Start from the end — nums1 has empty slots at the back, safe to write there.
     * Three pointers: end of valid nums1, end of nums2, write position.
     * Compare from the back, write the larger one at writePosition.
     * When nums2 is exhausted, remaining nums1 elements are already in place.
     */
    public void merge(int[] nums1, int m, int[] nums2, int n) {

        int pointer1 = m - 1;
        int pointer2 = n - 1;
        int writePosition = m + n - 1;

        while (pointer2 >= 0) {
            if (pointer1 >= 0 && nums1[pointer1] > nums2[pointer2]) {
                nums1[writePosition] = nums1[pointer1];
                pointer1--;
            } else {
                nums1[writePosition] = nums2[pointer2];
                pointer2--;
            }
            writePosition--;
        }
    }

    public static void main(String[] args) {
        MergeSortedArray solution = new MergeSortedArray();

        int[] nums1 = {1, 2, 3, 0, 0, 0};
        solution.merge(nums1, 3, new int[]{2, 5, 6}, 3);
        System.out.println(java.util.Arrays.toString(nums1)); // [1, 2, 2, 3, 5, 6]

        int[] nums2 = {1};
        solution.merge(nums2, 1, new int[]{}, 0);
        System.out.println(java.util.Arrays.toString(nums2)); // [1]
    }
}