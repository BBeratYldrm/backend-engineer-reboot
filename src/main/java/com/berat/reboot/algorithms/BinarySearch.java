package com.berat.reboot.algorithms;

public class BinarySearch {

    /**
     * Time Complexity:  O(log n) — search space halves on every step
     * Space Complexity: O(1) — no extra space used
     *
     * Start with left and right pointers at both ends.
     * Calculate mid, compare with target.
     * If too small, move left up. If too big, move right down.
     * I use left + (right - left) / 2 instead of (left + right) / 2
     * to avoid integer overflow on large arrays.
     * Loop ends when left > right — target not found, return -1.
     */
    public int search(int[] nums, int target) {

        int left = 0;
        int right = nums.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        BinarySearch solution = new BinarySearch();

        System.out.println(solution.search(new int[]{1, 3, 5, 7, 9}, 7)); // 3
        System.out.println(solution.search(new int[]{1, 3, 5, 7, 9}, 6)); // -1
        System.out.println(solution.search(new int[]{2, 4, 6, 8}, 2));    // 0
    }
}