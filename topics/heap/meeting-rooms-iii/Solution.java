// Problem:    Meeting Rooms III
// Link:       https://leetcode.com/problems/meeting-rooms-iii/
// Difficulty: Hard
// Tags:       heap, sorting, simulation
// Time:       O(m log m + n log n + m log n)
// Space:      O(n)

import java.util.*;

class Solution {
    public int mostBooked(int n, int[][] meetings) {
        Arrays.sort(meetings, (a,b)->Integer.compare(a[0], b[0]));
        PriorityQueue<long[]> busyRooms = new PriorityQueue<long[]>((a,b)->Arrays.compare(a,b));
        PriorityQueue<Integer> availableRooms = new PriorityQueue<>();
        int[] roomScore = new int[n];
        for (int i=0;i<n;i++) {
            availableRooms.offer(i);
        }
        for (var meet : meetings) {
            long start = meet[0], duration = meet[1]-meet[0];
            while(!busyRooms.isEmpty() && busyRooms.peek()[0]<=start) {
                long[] roomEntry = busyRooms.poll();
                availableRooms.offer((int)roomEntry[1]);
            }

            if (availableRooms.isEmpty()) {
                // wait
                // take the room that becomes available first
                // change the start time also
                start = busyRooms.peek()[0];
                long[] roomEntry = busyRooms.poll();
                availableRooms.offer((int)roomEntry[1]);
            }
            int room = availableRooms.poll();
            busyRooms.offer(new long[]{start+duration, room});
            roomScore[room]++;
        }

        int winner=-1, score=0;
        for(int i=0;i<n;i++) {
            if (roomScore[i]>score) {
                score=roomScore[i];
                winner=i;
            }
        }
        return winner;
    }

    // Local driver: online judges ignore main, so the file can be pasted as-is.
    public static void main(String[] args) {
        Solution sol = new Solution();
        System.out.println(sol.mostBooked(2, new int[][]{{0,10},{1,5},{2,7},{3,4}}));         // 0
        System.out.println(sol.mostBooked(3, new int[][]{{1,20},{2,10},{3,5},{4,9},{6,8}}));  // 1
    }
}
