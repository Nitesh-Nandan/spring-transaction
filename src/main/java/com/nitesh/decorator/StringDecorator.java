package com.nitesh.decorator;

public class StringDecorator {
    public static void main(String[] args) {
        String str = "Nandan";
        Decorator<String> decorator = new HelpDecorator();
        System.out.println(decorator.decorate("Nandan"));
    }
}

interface Decorator<T> {
    T decorate(T v);
}

class HelpDecorator implements Decorator<String> {
    @Override
    public String decorate(String v) {
        return "Nitesh: " + v;
    }
}

