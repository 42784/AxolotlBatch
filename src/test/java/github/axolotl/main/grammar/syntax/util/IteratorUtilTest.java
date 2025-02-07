package github.axolotl.main.grammar.syntax.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class IteratorUtilTest {

    @Test
    void intRange() {
        System.out.println(IteratorUtil.intRange(1, 2));
        System.out.println(IteratorUtil.intRange(1, 10,3));
        System.out.println(IteratorUtil.intRange(22, 10,-3));
    }
}