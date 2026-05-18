package com.berat.reboot.algorithms;

public class RemoveDuplicates {

    /**
     * Time Complexity:  O(n) — single pass through the array
     * Space Complexity: O(1) — in-place, no extra space
     *
     * Slow pointer (left) tracks the last unique element position.
     * Fast pointer (right) scans the array.
     * When a new unique element is found, move left forward and overwrite with the new value.
     * Array is sorted, so duplicates are always adjacent.
     */
    public int removeDuplicates(int[] nums) {

        int left = 0;

        for (int right = 1; right < nums.length; right++) {
            if (nums[left] != nums[right]) {
                left++;
                nums[left] = nums[right];
            }
        }

        return left + 1;
    }

    public static void main(String[] args) {
        RemoveDuplicates solution = new RemoveDuplicates();

        System.out.println(solution.removeDuplicates(new int[]{1, 1, 2}));                       // 2
        System.out.println(solution.removeDuplicates(new int[]{0, 0, 1, 1, 1, 2, 2, 3, 3, 4})); // 5
    }
}