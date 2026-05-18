package com.berat.reboot.algorithms;

public class MaxSumSubarray {

    /**
     * Time Complexity:  O(n) — single pass through the array
     * Space Complexity: O(1) — no extra space used
     *
     * Fixed size sliding window — window is always exactly k elements.
     * Add new element from right, remove oldest from left when window exceeds k.
     * Track the maximum sum seen at any window position.
     * This is the core fixed window skeleton — same structure applies to any fixed-size window problem.
     */
    public int maxSum(int[] nums, int k) {

        int left = 0;
        int currentSum = 0;
        int maxSum = 0;

        for (int right = 0; right < nums.length; right++) {
            currentSum += nums[right];

            if (right - left + 1 > k) {
                currentSum -= nums[left];
                left++;
            }

            maxSum = Math.max(maxSum, currentSum);
        }
        return maxSum;
    }

    public static void main(String[] args) {
        MaxSumSubarray solution = new MaxSumSubarray();

        System.out.println(solution.maxSum(new int[]{2, 1, 5, 1, 3, 2}, 3)); // 9
        System.out.println(solution.maxSum(new int[]{1, 2, 3}, 2));           // 5
    }
}