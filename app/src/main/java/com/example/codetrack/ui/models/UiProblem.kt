package com.example.codetrack.ui.models

data class UiProblem(
    val id: String,
    val title: String,
    val difficulty: String,
    val topic: String,
    val platform: String,
    val status: String,
    val description: String,
    val url: String
)

val sampleProblems = listOf(
    UiProblem(
        id = "1",
        title = "Two Sum",
        difficulty = "Easy",
        topic = "Arrays",
        platform = "LeetCode",
        status = "Solved",
        description = "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
        url = "https://leetcode.com/problems/two-sum/"
    ),
    UiProblem(
        id = "2",
        title = "Binary Search",
        difficulty = "Easy",
        topic = "Arrays",
        platform = "LeetCode",
        status = "Solved",
        description = "Given an array of integers nums which is sorted in ascending order, and an integer target, write a function to search target in nums.",
        url = "https://leetcode.com/problems/binary-search/"
    ),
    UiProblem(
        id = "3",
        title = "Valid Parentheses",
        difficulty = "Easy",
        topic = "Strings",
        platform = "LeetCode",
        status = "In Progress",
        description = "Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.",
        url = "https://leetcode.com/problems/valid-parentheses/"
    ),
    UiProblem(
        id = "4",
        title = "Merge Intervals",
        difficulty = "Medium",
        topic = "Arrays",
        platform = "LeetCode",
        status = "In Progress",
        description = "Given an array of intervals where intervals[i] = [starti, endi], merge all overlapping intervals.",
        url = "https://leetcode.com/problems/merge-intervals/"
    ),
    UiProblem(
        id = "5",
        title = "Best Time to Buy and Sell Stock",
        difficulty = "Easy",
        topic = "Arrays",
        platform = "LeetCode",
        status = "Solved",
        description = "You are given an array prices where prices[i] is the price of a given stock on the ith day. Return the maximum profit you can achieve.",
        url = "https://leetcode.com/problems/best-time-to-buy-and-sell-stock/"
    ),
    UiProblem(
        id = "6",
        title = "Reverse Linked List",
        difficulty = "Easy",
        topic = "Linked Lists",
        platform = "LeetCode",
        status = "Not Started",
        description = "Given the head of a singly linked list, reverse the list, and return the reversed list.",
        url = "https://leetcode.com/problems/reverse-linked-list/"
    ),
    UiProblem(
        id = "7",
        title = "Maximum Subarray",
        difficulty = "Medium",
        topic = "Dynamic Programming",
        platform = "GeeksforGeeks",
        status = "In Progress",
        description = "Given an integer array nums, find the subarray with the largest sum, and return its sum.",
        url = "https://practice.geeksforgeeks.org/problems/kadanes-algorithm/"
    ),
    UiProblem(
        id = "8",
        title = "Climbing Stairs",
        difficulty = "Easy",
        topic = "Dynamic Programming",
        platform = "LeetCode",
        status = "Solved",
        description = "You are climbing a staircase. It takes n steps to reach the top. Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?",
        url = "https://leetcode.com/problems/climbing-stairs/"
    ),
    UiProblem(
        id = "9",
        title = "Number of Islands",
        difficulty = "Medium",
        topic = "Graphs",
        platform = "LeetCode",
        status = "Not Started",
        description = "Given an m x n 2D binary grid grid which represents a map of '1's (land) and '0's (water), return the number of islands.",
        url = "https://leetcode.com/problems/number-of-islands/"
    ),
    UiProblem(
        id = "10",
        title = "Longest Substring Without Repeating Characters",
        difficulty = "Medium",
        topic = "Strings",
        platform = "LeetCode",
        status = "In Progress",
        description = "Given a string s, find the length of the longest substring without repeating characters.",
        url = "https://leetcode.com/problems/longest-substring-without-repeating-characters/"
    )
)
