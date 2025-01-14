package org.example;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        FunctionAnalyzer analyzer = new FunctionAnalyzer();
        analyzer.analyze("ObjectModule.bsl", "output.txt");
    }
}