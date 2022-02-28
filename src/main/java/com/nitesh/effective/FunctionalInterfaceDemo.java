package com.nitesh.effective;

import java.util.function.Predicate;

public class FunctionalInterfaceDemo {
}

class FunctionalTest {

    Predicate<Integer> isEven = (even) -> even%2 == 0;

    public void test() {
        System.out.println(isEven.test(59));
    }

    public static void main(String[] args) {
       new FunctionalTest().test();
    }
}
