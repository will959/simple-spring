package com.aling.util;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class UtilCollection {
    public static boolean isEmpty(Object[] obj) {
        return obj == null || obj.length == 0;
    }

    public static boolean isEmpty(Collection<?> obj) {
        return obj == null || obj.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> obj) {
        return obj == null || obj.isEmpty();
    }

    public static boolean notEmpty(Object[] obj) {
        return !isEmpty(obj);
    }

    public static boolean notEmpty(Collection<?> obj) {
        return !isEmpty(obj);
    }

    public static boolean notEmpty(Map<?, ?> obj) {
        return !isEmpty(obj);
    }
}
