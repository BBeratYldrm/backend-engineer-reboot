# Day 34 — May 19, 2026

## What I learned

### Algorithm — Two Pointer
- RemoveDuplicates — the mechanics finally clicked
    - left = position of last unique element, right = scanner
    - when right finds a new unique, left moves forward and writes it there
    - remaining right side is garbage, nobody looks at it
    - return left + 1 because index starts at 0

- MergeSortedArray — three pointer approach, start from the end
    - pointer1 = last valid element in nums1
    - pointer2 = end of nums2
    - writePosition = last index of nums1
    - reason for starting from the end: starting from the front would overwrite real elements
    - when nums2 is exhausted, remaining nums1 elements are already in place

## How I feel
Low energy evening but kept the streak alive.
RemoveDuplicates finally made sense — "right carries unique elements to the left,
left marks the position" was the sentence that clicked everything into place.
Not every session needs to be intense. Showing up matters.

## Next
- Two Pointer — Remove Element, Majority Element
- System Design — SD-06 Search Autocomplete
- Java Interview Crash Course — Module 1
- AWS basics