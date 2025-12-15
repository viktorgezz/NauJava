package ru.viktorgezz.NauJava.domain.result;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Итоговые оценки за прохождение теста.
 */
public enum Grade {

    A, B, C, F;

    public static Grade calculateGrade(BigDecimal pointCurr, BigDecimal pointMax) {
        BigDecimal percentage = pointCurr
                .divide(pointMax, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        BigDecimal valueGradeA = new BigDecimal("90");
        BigDecimal valueGradeB = new BigDecimal("80");
        BigDecimal valueGradeC = new BigDecimal("60");

        if (percentage.compareTo(valueGradeA) >= 0) {
            return A;
        }

        if (percentage.compareTo(valueGradeB) >= 0) {
            return B;
        }

        if (percentage.compareTo(valueGradeC) >= 0) {
            return C;
        }

        return F;
    }
}
