# Day 33 — May 18, 2026

## What I learned

### Algorithm — HashMap / HashSet
- IntersectionOfTwoArrays — HashSet, two sets for deduplication
- MostFrequentElement — HashMap + entrySet(), track max and result together
- CharacterFrequency — HashMap, single pass, map is the answer
- PairWithTargetDifference — HashMap, complement = target + current
- PalindromPermutation — HashSet, add if absent remove if present, size <= 1

### Algorithm — Stack
- ReverseDigits — Character.isDigit(), two passes, LIFO does the reversal

### Algorithm — Binary Search
- BinarySearch — left + (right-left)/2 to avoid overflow, three-way comparison

### Algorithm — Sliding Window
- MaxSumSubarray — fixed size window skeleton, sağdan ekle soldan çıkar
- PermutationInString — fixed window + frequency map comparison

### Algorithm — Two Pointer
- TwoSumSorted — sorted array, two ends toward middle, O(1) space
- ValidPalindrome — skip non-alphanumeric with inner while, toLowerCase comparison
- RemoveDuplicates — slow/fast pointer, in-place, sorted array

### New concepts learned today
- entrySet() — iterate over both key and value in a map
- for-each on arrays — cleaner than index-based loop
- Character.isDigit(), Character.isLetterOrDigit(), Character.toLowerCase()
- Fixed window vs variable window — fixed: if condition, variable: while condition
- Two Pointer variants — left/right from ends vs slow/fast same direction

## How I feel
Long and productive day. Started at Starbucks, continued at home.
Pattern recognition is getting faster — started identifying HashMap, Two Pointer,
Sliding Window without being told. Still need more reps on Sliding Window.
Some problems felt hard in the moment but made sense after seeing the visualization.
Consistency is building confidence slowly.

## Next
- System Design — SD-06 Search Autocomplete
- Algorithm practice — more Two Pointer and Sliding Window reps
- Java Interview Crash Course — Modül 1 başlangıç
- AWS basics