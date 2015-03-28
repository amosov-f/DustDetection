package ru.spbu.astro.util;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 19:42
 */
public class ArrayTools {
    public static void sort(@NotNull final int[] array, @NotNull final Comparator<Integer> comparator) {
        final Integer[] sortedArray = ArrayUtils.toObject(array);
        Arrays.sort(sortedArray, comparator);
        System.arraycopy(ArrayUtils.toPrimitive(sortedArray), 0, array, 0, array.length);
    }
}
