package org.example;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
public class FunctionAnalyzer {
    private final static String LANGUAGE_DELIMITER = "=";
    private final static String OUTPUT_FORMAT = "%s: %s : %s";

    public void analyze(String inputFilename, String outputFilename) {
        String startSearchString = "NStr(";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilename));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename))) {
            String line;
            int lineNumber = 0;
            while (nonNull(line = reader.readLine())) {
                lineNumber++;
                int currentLineNumber = lineNumber;

                List<String> functionsParams = new ArrayList<>();
                findFunctionParams(functionsParams, line, startSearchString);
                functionsParams.forEach(initialString -> {
                    formAndPrintLanguagesInfo(initialString, currentLineNumber, writer);
                });
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void printInfo(String info, BufferedWriter writer) {
        try {
            writer.write(info);
            writer.newLine();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Сформировать пары <язык - текст> из строки
     *
     * @param initialString строка (первый аргумент функции)
     * @param lineNumber номер строки
     * @param writer BufferedWriter для вывода текста в файл
     */
    private void formAndPrintLanguagesInfo(String initialString, int lineNumber, BufferedWriter writer) {
        try {
            initialString = initialString.replace("\"\"", "\"");
            while (!initialString.isBlank()) {
                int langDelimiterIndex = initialString.indexOf(LANGUAGE_DELIMITER);
                String langCode = initialString.substring(0, langDelimiterIndex).trim();
                initialString = initialString.substring(langDelimiterIndex + LANGUAGE_DELIMITER.length()).trim();
                String firstTextSymbol = initialString.substring(0, 1);
                String text;
                if ("'".equals(firstTextSymbol) || "\"".equals(firstTextSymbol)) {
                    int nextQuoteIndex = 0;

                    while (nextQuoteIndex != initialString.length() - 1
                            && nextQuoteIndex != -1
                            && initialString.charAt(nextQuoteIndex + 1) != ';'
                    ) {
                        nextQuoteIndex = initialString.indexOf(firstTextSymbol, firstTextSymbol.length());
                    }

                    text = initialString.substring(firstTextSymbol.length(), nextQuoteIndex);
                    initialString = initialString.substring(nextQuoteIndex + 1);
                } else {
                    text = initialString;
                    initialString = "";
                }
                printInfo(String.format(OUTPUT_FORMAT, lineNumber, langCode, text), writer);

                if (!initialString.isBlank() && initialString.charAt(0) == ';') {
                    initialString = initialString.substring(1).trim();
                }
            }

        } catch (Exception e) {
            log.error("Не удалось распарсить строку {}", initialString, e);
        }
    }

    /**
     * Поиск первых параметров функций
     *
     * @param functionParams    список для записи результатов поиска
     * @param line              строка, содержащая вызовы функций
     * @param startSearchString начало функции (название + открывающая скобка)
     */
    private void findFunctionParams(List<String> functionParams,
                                    String line,
                                    String startSearchString) {
        int startSearchIndex = line.indexOf(startSearchString);
        if (startSearchIndex == -1) {
            return;
        }

        line = line.substring(startSearchIndex + startSearchString.length()).trim();
        // На этом этапе у нас есть строка, которая начинается с первого аргумента функции NStr
        char openingQuote = line.charAt(0);
        boolean closingQuoteFound = false;
        startSearchIndex = 1;
        int nextQuoteIndex = 0;
        while (!closingQuoteFound && nextQuoteIndex != -1) {
            nextQuoteIndex = line.indexOf(openingQuote, startSearchIndex);
            if (line.charAt(nextQuoteIndex + 1) == openingQuote) {
                // Экранированная кавычка внутри первого аргумента, то есть две кавычки подряд
                startSearchIndex = nextQuoteIndex + 2;
            } else {
                closingQuoteFound = true;
                functionParams.add(line.substring(1, nextQuoteIndex).trim());
                line = line.substring(nextQuoteIndex + 1);
            }
        }

        findFunctionParams(functionParams, line, startSearchString);
    }
}
