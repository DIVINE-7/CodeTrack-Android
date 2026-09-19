package com.example.codetrack.data.repository

import com.example.codetrack.data.model.CodingLanguage
import com.example.codetrack.data.model.CodingProblemConfig
import com.example.codetrack.data.model.DsaTestCase

object CodingProblemRepository {
    private val configMap = mutableMapOf<Int, CodingProblemConfig>()

    init {
        // 1. Two Sum (ID 1)
        configMap[1] = CodingProblemConfig(
            problemId = 1,
            starterCode = mapOf(
                CodingLanguage.PYTHON to """import sys
def solve():
    # Read input from stdin
    lines = sys.stdin.read().split()
    if not lines: return
    n = int(lines[0])
    nums = [int(x) for x in lines[1:n+1]]
    target = int(lines[n+1])
    
    # Write your solution here
    
    # Correct mock solution must contain: return [i, j]
    pass

if __name__ == "__main__":
    solve()""".trimMargin(),
                CodingLanguage.JAVA to """import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
        int target = sc.nextInt();
        
        // Write your solution here
        // Correct mock solution must contain: return result;
    }
}""".trimMargin()
            ),
            testCases = listOf(
                DsaTestCase(1, "4\n2 7 11 15\n9", "0 1", "nums = [2,7,11,15], target = 9"),
                DsaTestCase(2, "3\n3 2 4\n6", "1 2", "nums = [3,2,4], target = 6"),
                DsaTestCase(3, "2\n3 3\n6", "0 1", "nums = [3,3], target = 6")
            ),
            mockCorrectSolutions = mapOf(
                CodingLanguage.PYTHON to listOf("return [i, j]"),
                CodingLanguage.JAVA to listOf("return result;"),
                CodingLanguage.CPP to listOf("return {i, j};"),
                CodingLanguage.C to listOf("res[0] = i;")
            )
        )

        // 2. Binary Search (ID 2)
        configMap[2] = CodingProblemConfig(
            problemId = 2,
            starterCode = mapOf(
                CodingLanguage.PYTHON to """import sys
def search():
    # Read n, then n elements, then target
    data = sys.stdin.read().split()
    if not data: return
    n = int(data[0])
    nums = [int(x) for x in data[1:n+1]]
    target = int(data[n+1])
    
    # Write your logic here
    # Mock correct if contains: return mid
    pass

if __name__ == "__main__":
    search()""".trimMargin(),
                CodingLanguage.JAVA to """import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
        int target = sc.nextInt();
        
        // Binary search logic
        // Mock correct if contains: return mid;
    }
}""".trimMargin()
            ),
            testCases = listOf(
                DsaTestCase(1, "6\n-1 0 3 5 9 12\n9", "4", "nums = [-1,0,3,5,9,12], target = 9"),
                DsaTestCase(2, "6\n-1 0 3 5 9 12\n2", "-1", "nums = [-1,0,3,5,9,12], target = 2")
            ),
            mockCorrectSolutions = mapOf(
                CodingLanguage.PYTHON to listOf("return mid"),
                CodingLanguage.JAVA to listOf("return mid;"),
                CodingLanguage.CPP to listOf("return mid;"),
                CodingLanguage.C to listOf("return mid;")
            )
        )

        // 3. Valid Anagram (ID 151)
        configMap[151] = CodingProblemConfig(
            problemId = 151,
            starterCode = mapOf(
                CodingLanguage.PYTHON to """import sys
def isAnagram():
    # Read two strings from stdin
    lines = sys.stdin.read().split()
    if len(lines) < 2: return
    s, t = lines[0], lines[1]
    
    # Logic here
    # Mock correct if contains: sorted(s) == sorted(t)
    pass

if __name__ == "__main__":
    isAnagram()""".trimMargin(),
                CodingLanguage.JAVA to """import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNext()) return;
        String s = sc.next();
        String t = sc.next();
        
        // Logic here
        // Mock correct if contains: Arrays.sort
    }
}""".trimMargin()
            ),
            testCases = listOf(
                DsaTestCase(1, "anagram\nnagaram", "true", "s = \"anagram\", t = \"nagaram\""),
                DsaTestCase(2, "rat\ncar", "false", "s = \"rat\", t = \"car\"")
            ),
            mockCorrectSolutions = mapOf(
                CodingLanguage.PYTHON to listOf("sorted(s) == sorted(t)"),
                CodingLanguage.JAVA to listOf("Arrays.sort"),
                CodingLanguage.CPP to listOf("sort"),
                CodingLanguage.C to listOf("strcmp")
            )
        )

        // 4. Reverse Linked List (ID 301)
        configMap[301] = CodingProblemConfig(
            problemId = 301,
            starterCode = mapOf(
                CodingLanguage.PYTHON to """import sys
def reverse():
    # Read n, then n nodes
    data = sys.stdin.read().split()
    if not data: return
    n = int(data[0])
    nodes = data[1:n+1]
    
    # Logic
    # Mock correct if contains: curr.next = prev
    pass

if __name__ == "__main__":
    reverse()""".trimMargin(),
                CodingLanguage.JAVA to """import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        // Read list
        
        // Mock correct if contains: curr.next = prev;
    }
}""".trimMargin()
            ),
            testCases = listOf(
                DsaTestCase(1, "5\n1 2 3 4 5", "5 4 3 2 1", "head = [1,2,3,4,5]")
            ),
            mockCorrectSolutions = mapOf(
                CodingLanguage.PYTHON to listOf("curr.next = prev"),
                CodingLanguage.JAVA to listOf("curr.next = prev;"),
                CodingLanguage.CPP to listOf("curr->next = prev;"),
                CodingLanguage.C to listOf("curr->next = prev;")
            )
        )

        // 5. Valid Parentheses (ID 451)
        configMap[451] = CodingProblemConfig(
            problemId = 451,
            starterCode = mapOf(
                CodingLanguage.PYTHON to """import sys
def isValid():
    s = sys.stdin.read().strip()
    if not s: return
    
    # Logic
    # Mock correct if contains: stack.pop()
    pass

if __name__ == "__main__":
    isValid()""".trimMargin(),
                CodingLanguage.JAVA to """import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNext()) return;
        String s = sc.next();
        
        // Logic
        // Mock correct if contains: stack.pop();
    }
}""".trimMargin()
            ),
            testCases = listOf(
                DsaTestCase(1, "()[]{}", "true", "s = \"()[]{}\""),
                DsaTestCase(2, "(]", "false", "s = \"(]\"")
            ),
            mockCorrectSolutions = mapOf(
                CodingLanguage.PYTHON to listOf("stack.pop()"),
                CodingLanguage.JAVA to listOf("stack.pop();"),
                CodingLanguage.CPP to listOf("stack.pop();"),
                CodingLanguage.C to listOf("return top == 0;")
            )
        )
    }

    fun getConfig(problemId: Int): CodingProblemConfig? {
        return configMap[problemId]
    }
}
