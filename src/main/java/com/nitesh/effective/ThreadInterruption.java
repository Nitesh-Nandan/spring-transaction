package com.nitesh.effective;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class MyThread extends Thread {
    public void run () {
        String keyPrefix = String.format("{%s::%s::%s}::",
                "PROJECT_NAME_SHORTHAND", "getServiceName", 11);

        System.out.println(keyPrefix);
        try {
            for (int i = 0; i < 5; i++) {
                System.out.println("Child Thread executing");

                // Here current threads goes to sleeping state
                // Another thread gets the chance to execute
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
            System.out.println("InterruptedException occur");
        }
    }
}

class New {
    public static void print() {
        System.out.println("Hello");
    }
}

public class ThreadInterruption {
    public static void main(String[] args) {
        String startDate = "2022-11-09";
        List<Integer> endDateArr = Arrays.stream(startDate.split("-")).sequential()
                .map(Integer::parseInt).collect(Collectors.toList());

        Integer[] endDateArr1 = Arrays.stream(startDate.split("-")).sequential()
                .map(Integer::parseInt)
                .toArray(Integer[]::new);


       New nw = new New();
       nw.print();


    }
}

