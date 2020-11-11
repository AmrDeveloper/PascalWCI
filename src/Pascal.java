import backend.Backend;
import backend.BackendFactory;
import backend.interpreter.RuntimeErrorCode;
import frontend.FrontendFactory;
import frontend.Parser;
import frontend.Source;
import frontend.TokenType;
import intermediate.ICode;
import intermediate.SymbolTableStack;
import message.Message;
import message.MessageListener;
import message.MessageType;
import util.CrossReferencer;
import util.ParseTreePrinter;

import java.io.BufferedReader;
import java.io.FileReader;

import static frontend.pascal.PascalTokenType.STRING;

public class Pascal {

    private Parser parser;
    private Source source;
    private ICode iCode;
    private SymbolTableStack symbolTableStack;
    private Backend backend;

    public Pascal(String operation, String filePath, String flags) {
        try {
            boolean intermediate = flags.contains("i");
            boolean xref = flags.contains("x");

            source = new Source(new BufferedReader(new FileReader(filePath)));
            source.addMessageListener(new SourceMessageListener());

            parser = FrontendFactory.createParser("Pascal", "top-down", source);
            parser.addMessageListener(new ParserMessageListener());

            backend = BackendFactory.createBackend(operation);
            backend.addMessageListener(new BackendMessageListener());

            parser.parse();
            source.close();

            iCode = parser.getICode();
            symbolTableStack = parser.getSymbolTableStack();

            if (xref) {
                CrossReferencer crossReferencer = new CrossReferencer();
                crossReferencer.print(symbolTableStack);
            }

            if (intermediate) {
                ParseTreePrinter treePrinter =
                        new ParseTreePrinter(System.out);
                treePrinter.print(iCode);
            }

            backend.process(iCode, symbolTableStack);
        } catch (Exception e) {
            System.out.println("Internal Translator Error");
            e.printStackTrace();
        }
    }

    private static final String FLAGS = "[-ix]";
    private static final String USAGE = "Usage: Pascal execute|compile " + FLAGS + " <source file path>";

    public static void main(String[] args) {
        try {
            String operation = args[0];

            if (!(operation.equalsIgnoreCase("compile") || operation.equalsIgnoreCase("execute"))) {
                throw new Exception();
            }

            int i = 0;
            StringBuilder flags = new StringBuilder();

            while ((++i < args.length) && (args[i].charAt(0) == '-')) {
                flags.append(args[i].substring(1));
            }

            if (i < args.length) {
                String path = args[i];
                new Pascal(operation, path, flags.toString());
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(USAGE);
        }
    }

    private static final String SOURCE_LINE_FORMAT = "%03d %s";

    private static class SourceMessageListener implements MessageListener {

        @Override
        public void messageReceived(Message message) {

            MessageType type = message.getType();
            Object body[] = (Object[]) message.getBody();

            switch (type) {
                case SOURCE_LINE: {
                    int lineNumber = (Integer) body[0];
                    Integer lineText = (Integer) body[1];

                    System.out.println(String.format(SOURCE_LINE_FORMAT, lineNumber, lineText));

                    break;
                }
            }
        }
    }

    private static final String TOKEN_FORMAT = ">>> %-15s line=%03d, pos=%2d, text=\"%s\"";
    private static final String VALUE_FORMAT = ">>>                 value=%s";
    private static final int PREFIX_WIDTH = 5;

    private static class ParserMessageListener implements MessageListener {

        private static final String PARSER_SUMMARY_FORMAT =
                "\n%,20d source lines." +
                        "\n%,20d syntax errors." +
                        "\n%,20.2f seconds total parsing time.\n";

        @Override
        public void messageReceived(Message message) {
            MessageType type = message.getType();

            switch (type) {
                case TOKEN: {
                    Object[] body = (Object[]) message.getBody();
                    int line = (int) body[0];
                    int position = (int) body[1];
                    TokenType tokenType = (TokenType) body[2];
                    String tokenText = (String) body[3];
                    Object tokenValue = body[4];

                    System.out.printf((TOKEN_FORMAT) + "%n",
                            tokenType,
                            line,
                            position,
                            tokenText);

                    if (tokenValue != null) {
                        if (tokenType == STRING) {
                            tokenValue = "\"" + tokenValue + "\"";
                        }

                        System.out.printf((VALUE_FORMAT) + "%n", tokenValue);
                    }
                    break;
                }

                case SYNTAX_ERROR: {
                    Object[] body = (Object[]) message.getBody();
                    int lineNumber = (int) body[0];
                    int position = (int) body[1];
                    String tokenText = (String) body[2];
                    String errorMessage = (String) body[3];

                    int spaceCount = PREFIX_WIDTH + position;
                    StringBuffer flagBuilder = new StringBuffer();

                    for (int i = 0; i < spaceCount; i++) {
                        flagBuilder.append(" ");
                    }

                    flagBuilder.append("^\n*** ").append(errorMessage);

                    if (tokenText != null) {
                        flagBuilder.append(" [at \"").append(tokenText).append("\"]");
                    }

                    System.out.println(flagBuilder.toString());
                    break;
                }

                case PARSER_SUMMARY: {
                    Number body[] = (Number[]) message.getBody();
                    int statementCount = (int) body[0];
                    int syntaxErrors = (int) body[1];
                    float elapsedTime = (float) body[2];
                    System.out.printf(PARSER_SUMMARY_FORMAT, statementCount, syntaxErrors, elapsedTime);
                    break;
                }
            }
        }
    }

    private static class BackendMessageListener implements MessageListener {

        private static final String INTERPRETER_SUMMARY_FORMAT =
                "\n%,20d statements executed." +
                        "\n%,20d runtime errors." +
                        "\n%,20.2f seconds total code generation time.\n";

        private static final String COMPILER_SUMMARY_FORMAT =
                "\n%,20d statements executed." +
                        "\n%,20.2f seconds total code generation time.\n";

        private static final String ASSIGN_FORMAT = " >>> LINE %03d: %s = %s\n";

        @Override
        public void messageReceived(Message message) {
            MessageType type = message.getType();

            switch (type) {
                case INTERPRETER_SUMMARY: {
                    Number body[] = (Number[]) message.getBody();

                    int executionCount = (int) body[0];
                    int runtimeErrors = (int) body[1];
                    float elapsedTime = (float) body[2];
                    System.out.printf(INTERPRETER_SUMMARY_FORMAT, executionCount, runtimeErrors, elapsedTime);

                    break;
                }

                case COMPILER_SUMMARY: {
                    Number body[] = (Number[]) message.getBody();

                    int instructionCount = (int) body[0];
                    float elapsedTime = (float) body[1];
                    System.out.printf(COMPILER_SUMMARY_FORMAT, instructionCount, elapsedTime);

                    break;
                }
                case ASSIGN: {
                    Object body[] = (Object[]) message.getBody();
                    int lineNumber = (Integer) body[0];
                    String variableName = (String) body[1];
                    Object value = body[2];
                    System.out.printf(ASSIGN_FORMAT, lineNumber, variableName, value);
                    break;
                }
                case RUNTIME_ERROR: {
                    Object body[] = (Object[]) message.getBody();
                    RuntimeErrorCode errorMessage = (RuntimeErrorCode) body[0];
                    Integer lineNumber = (Integer) body[1];
                    System.out.print("*** RUNTIME ERROR");
                    if (lineNumber != null) {
                        System.out.print(" AT LINE " + String.format("%03d", lineNumber));
                    }
                    System.out.println(": " + errorMessage);
                    break;
                }

            }

        }
    }

}
