package com.berat.reboot.algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TwoSumTest {

    TwoSum twoSum = new TwoSum();

    @Test
    void twoSum_shouldReturnCorrectIndices() {
        // 2 + 7 = 9, indices 0 and 1
        assertArrayEquals(new int[]{0, 1}, twoSum.twoSum(new int[]{2, 7, 11, 15}, 9));
    }

    @Test
    void twoSum_shouldReturnCorrectIndices_whenAnswerIsNotFirst() {
        // 3 + 4 = 6, indices 1 and 2 (not first elements)
        assertArrayEquals(new int[]{1, 2}, twoSum.twoSum(new int[]{3, 2, 4}, 6));
    }

    @Test
    void twoSum_shouldThrowException_whenNoSolution() {
        // no two numbers sum to 5
        assertThrows(
                IllegalArgumentException.class,
                () -> twoSum.twoSum(new int[]{1, 2, 3}, 100)
        );
    }
}