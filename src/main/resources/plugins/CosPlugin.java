package com.example.demo.plugin;

public class CosPlugin implements Plugin {
    @Override
    public String getName() {
        return "cos";
    }

    @Override
    public double calculate(double value) {
        return Math.cos(value);
    }
}