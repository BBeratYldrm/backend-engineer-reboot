package com.berat.reboot.algorithms;

public class MoveZeroes {

    /**
     * Time Complexity:  O(n) — two passes through the array
     * Space Complexity: O(1) — in-place, no extra space
     *
     * Slow/fast two pointer — same skeleton as RemoveDuplicates.
     * left tracks the position for the next non-zero element.
     * right scans the array — when non-zero found, write it to left, advance left.
     * After the loop, everything from left onward is filled with zeros.
     */
    public void moveZeroes(int[] nums) {

        int left = 0;

        for (int right = 1; right < nums.length; right++) {
            if (nums[right] != 0) {
                nums[left] = nums[right];
                left++;
            }
        }

        for (int i = left; i < nums.length; i++) {
            nums[i] = 0;
        }
    }

    public static void main(String[] args) {
        MoveZeroes solution = new MoveZeroes();

        int[] arr1 = {0, 1, 0, 3, 12};
        solution.moveZeroes(arr1);
        System.out.println(java.util.Arrays.toString(arr1)); // [1, 3, 12, 0, 0]

        int[] arr2 = {0, 0, 1};
        solution.moveZeroes(arr2);
        System.out.println(java.util.Arrays.toString(arr2)); // [1, 0, 0]
    }
}