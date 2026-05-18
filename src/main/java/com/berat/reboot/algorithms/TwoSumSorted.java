package com.berat.reboot.algorithms;

public class TwoSumSorted {

    /**
     * Time Complexity:  O(n) — single pass with two pointers
     * Space Complexity: O(1) — no extra space, this is why we prefer Two Pointer over HashMap for sorted arrays
     *
     * Left starts at beginning, right starts at end.
     * Sum too big → move right down. Sum too small → move left up.
     * Only works because array is sorted — unsorted would require HashMap.
     */
    public int[] twoSum(int[] nums, int target) {

        int left = 0;
        int right = nums.length - 1;

        while (left < right) {
            int sum = nums[left] + nums[right];
            if (sum == target) {
                return new int[]{left, right};
            } else if (sum > target) {
                right--;
            } else {
                left++;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        TwoSumSorted solution = new TwoSumSorted();

        System.out.println(java.util.Arrays.toString(solution.twoSum(new int[]{2, 7, 11, 15}, 9)));  // [0, 1]
        System.out.println(java.util.Arrays.toString(solution.twoSum(new int[]{2, 3, 4}, 6)));       // [0, 2]
        System.out.println(java.util.Arrays.toString(solution.twoSum(new int[]{3, 3}, 6)));          // [0, 1]
    }
}