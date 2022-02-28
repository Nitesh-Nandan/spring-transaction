package com.nitesh.decorator;

public class DecoratorDemo {
    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.test();
    }
}

class Greet {
    public void greet() {
        System.out.println("Hello world");
    }
}

interface TaskDecorator {
    Runnable decorate(Runnable runnable);
}

class MdcDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        System.out.println("Decorated");
        return () -> {
            try {
                Greet greet = new Greet();
                greet.greet();
              runnable.run();
            } finally {
                System.out.println("Invoked finally");
            }
        };
    }
}


class MyThread implements Runnable {
    private String name;
    public MyThread(String name) {
        this.name = name;
    }
    @Override
    public void run() {
        System.out.println("Run ho gaya: " + name);
    }
}

class Demo {
    public void test() {
        Runnable th1 = new MyThread("Nitesh Nandan");
        Runnable r1 = new MdcDecorator().decorate(th1);
        r1.run();
    }

}