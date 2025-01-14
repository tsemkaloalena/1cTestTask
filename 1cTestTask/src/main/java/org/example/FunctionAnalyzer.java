package org.example;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Slf4j
public class FunctionAnalyzer {
    private final static String INITIAL_STRING_DELIMITER = ";";
    private final static String LANGUAGE_DELIMITER = "=";
    private final static String OUTPUT_FORMAT = "%s: %s : %s";

    public void analyze(String inputFilename, String outputFilename) {
        String startSearchString = "NStr(";
        String finishSearchString = ")";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilename));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename))) {
            String line;
            int lineNumber = 0;
            while (nonNull(line = reader.readLine())) {
                lineNumber++;
                int currentLineNumber = lineNumber;

                List<String> functionsParams = new ArrayList<>();
                findFunctionParams(functionsParams, line, startSearchString, finishSearchString, 0);
                functionsParams.forEach(functionParams -> {
                    getFirstInputParam(functionParams).ifPresent(initialString -> {
                        Stream<String> formedLanguagesInfo = formLanguagesInfo(initialString, currentLineNumber);
                        printInfo(formedLanguagesInfo, writer);
                    });
                });
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void printInfo(Stream<String> formedLanguagesInfo, BufferedWriter writer) {
        formedLanguagesInfo
                .filter(Objects::nonNull)
                .forEach(info -> {
                    try {
                        writer.write(info);
                        writer.newLine();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                });
    }

    private Stream<String> formLanguagesInfo(String initialString, int lineNumber) {
        boolean singleLanguage = !initialString.contains(INITIAL_STRING_DELIMITER);
        List<String> languagesInfo = List.of(initialString.split(INITIAL_STRING_DELIMITER));
        return languagesInfo.stream().map(languageInfo -> {
            List<String> infoParts = List.of(languageInfo.trim().split(LANGUAGE_DELIMITER));
            if (infoParts.size() != 2) {
                return null;
            }
            String langCode = infoParts.get(0).trim();
            String text = infoParts.get(1).trim();
            String firstTextSymbol = text.substring(0, 1);
            if (!singleLanguage && ("\"".equals(firstTextSymbol) || "'".equals(firstTextSymbol))) {
                text = text.substring(1, text.length() - 1);
            }
            return String.format(OUTPUT_FORMAT, lineNumber, langCode, text);
        });
    }

    /**
     * Поиск первого параметра функции
     *
     * @param functionParams параметры функции
     * @return первый параметр функции
     */
    private Optional<String> getFirstInputParam(String functionParams) {
        String paramBounds = functionParams.substring(0, 1);
        int finishingBoundIndex = functionParams.indexOf(paramBounds, 1);

        while (finishingBoundIndex != -1
                && "\\".equals(functionParams.substring(finishingBoundIndex - 1, finishingBoundIndex))) {
            // Если найденная кавычка является экранированной внутри строки с текстом первого параметра,
            // то продолжаем поиск
            finishingBoundIndex = functionParams.indexOf(paramBounds, finishingBoundIndex + 1);
        }
        if (finishingBoundIndex == -1) {
            return Optional.empty();
        }
        return Optional.of(functionParams.substring(1, finishingBoundIndex));
    }

    /**
     * Поиск параметров функций
     *
     * @param functionParams     список для записи результатов поиска
     * @param line               строка, содержащая вызовы функций
     * @param startSearchString  начало функции (название + открывающая скобка)
     * @param finishSearchString конец функции (закрывающая скобка)
     * @param startSearchIndex   индекс, начиная с которого начинается поиск
     */
    private void findFunctionParams(List<String> functionParams,
                                    String line,
                                    String startSearchString,
                                    String finishSearchString,
                                    int startSearchIndex) {
        startSearchIndex = line.indexOf(startSearchString, startSearchIndex);
        if (startSearchIndex == -1) {
            return;
        }

        int finishSearchIndex = line.indexOf(finishSearchString, startSearchIndex);
        if (finishSearchIndex == -1) {
            return;
        }

        functionParams.add(line.substring(startSearchIndex + startSearchString.length(), finishSearchIndex));
        findFunctionParams(functionParams, line, startSearchString, finishSearchString, finishSearchIndex);
    }
}
