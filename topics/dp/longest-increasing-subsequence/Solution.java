// Problem:    Longest Increasing Subsequence
// Link:       https://leetcode.com/problems/longest-increasing-subsequence/
// Difficulty: Medium
// Tags:       dp, binary-search, patience-sorting, tree-map
// Time:       O(n log n)
// Space:      O(n)

import java.util.*;

class Solution {
    public int lengthOfLIS(int[] nums) {
        return longestIncreasingSubsequence(nums).size();
    }

    // Patience sorting: keys are the smallest tail of an increasing subsequence of each
    // length, values are the index of that tail. prev[i] links i to the tail one pile
    // shorter at the time i was placed, so walking prev from the last tail rebuilds an LIS.
    List<Integer> longestIncreasingSubsequence(int[] nums) {
        int n = nums.length;
        TreeMap<Integer, Integer> mpp = new TreeMap<>();
        int[] prev = new int[n];
        Arrays.fill(prev,-1);
        for (int i=0;i<n;i++) {
            Integer key = mpp.lowerKey(nums[i]);
            if (key!=null) prev[i] = mpp.get(key);
            if (mpp.containsKey(nums[i])) continue;
            Integer ceil = mpp.higherKey(nums[i]);
            if (ceil!=null) mpp.remove(ceil);
            mpp.put(nums[i], i);
        }

        List<Integer> seq = new ArrayList<>();
        if (mpp.isEmpty()) return seq;
        for (int idx = mpp.lastEntry().getValue();idx!=-1;idx=prev[idx]) {
            seq.add(nums[idx]);
        }
        Collections.reverse(seq);
        return seq;
    }

    // Local driver: prints the length and one LIS for each input.
    // Expected:
    //   4 [2, 3, 7, 18]
    //   4 [0, 1, 2, 3]
    //   1 [7]
    //   0 []
    public static void main(String[] args) {
        Solution sol = new Solution();
        int[][] tests = {
            {10, 9, 2, 5, 3, 7, 101, 18},
            {0, 1, 0, 3, 2, 3},
            {7, 7, 7, 7},
            {},
        };
        for (int[] nums : tests) {
            System.out.println(sol.lengthOfLIS(nums) + " " + sol.longestIncreasingSubsequence(nums));
        }
    }
}
