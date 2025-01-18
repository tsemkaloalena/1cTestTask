package org.example;

public class Main {
    public static void main(String[] args) {
        FunctionAnalyzer analyzer = new FunctionAnalyzer();
        analyzer.analyze("ObjectModule.bsl", "output.txt");
    }
}