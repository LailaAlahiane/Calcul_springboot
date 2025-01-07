package com.example.demo.plugin;


public class SinPlugin implements Plugin {
    @Override
    public String getName() {
        return "sin";
    }

    @Override
    public double calculate(double value) {
        return Math.sin(value);
    }
}