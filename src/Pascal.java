import backend.Backend;
import backend.BackendFactory;
import backend.interpreter.RuntimeErrorCode;
import frontend.FrontendFactory;
import frontend.Parser;
import frontend.Source;
import frontend.TokenType;
import intermediate.ICode;
import intermediate.SymbolTableEntry;
import intermediate.SymbolTableStack;
import message.Message;
import message.MessageListener;
import message.MessageType;
import util.CrossReferencer;
import util.ParseTreePrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static frontend.pascal.PascalTokenType.STRING;
import static intermediate.symtabimpl.SymbolTableKeyImp.ROUTINE_ICODE;

public class Pascal {

    private Parser parser;
    private Source source;
    private ICode iCode;
    private SymbolTableStack symbolTableStack;
    private Backend backend;

    private boolean intermediate;     // true to print intermediate code
    private boolean xref;             // true to print cross-reference listing
    private boolean lines;            // true to print source line tracing
    private boolean assign;           // true to print value assignment tracing
    private boolean fetch;            // true to print value fetch tracing
    private boolean call;             // true to print routine call tracing
    private boolean returnn;          // true to print routine return tracing

    public Pascal(String operation, String sourcePath, String inputPath,  String flags) {
        try {
            intermediate = flags.indexOf('i') > -1;
            xref = flags.indexOf('x') > -1;
            lines = flags.indexOf('l') > -1;
            assign = flags.indexOf('a') > -1;
            fetch = flags.indexOf('f') > -1;
            call = flags.indexOf('c') > -1;
            returnn = flags.indexOf('r') > -1;

            source = new Source(new BufferedReader(new FileReader(sourcePath)));
            source.addMessageListener(new SourceMessageListener());

            parser = FrontendFactory.createParser("Pascal", "top-down", source);
            parser.addMessageListener(new ParserMessageListener());

            backend = BackendFactory.createBackend(operation, inputPath);
            backend.addMessageListener(new BackendMessageListener());

            parser.parse();
            source.close();

            if (parser.getErrorCount() == 0) {
                symbolTableStack = parser.getSymbolTableStack();

                SymbolTableEntry programId = symbolTableStack.getProgramId();
                iCode = (ICode) programId.getAttribute(ROUTINE_ICODE);

                if (xref) {
                    CrossReferencer crossReferencer = new CrossReferencer();
                    crossReferencer.print(symbolTableStack);
                }

                if (intermediate) {
                    ParseTreePrinter treePrinter =
                            new ParseTreePrinter(System.out);
                    treePrinter.print(symbolTableStack);
                }

                backend.process(iCode, symbolTableStack);
            }
        } catch (Exception e) {
            System.out.println("Internal Translator Error");
            e.printStackTrace();
        }
    }

    private static final String FLAGS = "[-ixlafcr]";
    private static final String USAGE = "Usage: Pascal execute|compile " + FLAGS + " <source file path>";

    public static void main(String[] args) {
        try {
            String operation = args[0];
            
            if (!("compile".equalsIgnoreCase(operation)) 
                    || !("execute".equalsIgnoreCase(operation))) {
                throw new Exception();
            }

            int i = 0;
            StringBuilder flags = new StringBuilder();
            
            // Flags
            while ((++i < args.length) && (args[i].charAt(0) == '-')) {
                flags.append(args[i].substring(1));
            }
            
            String sourcePath = null;
            String inputPath = null;
            
            // Source path
            if (i < args.length) {
                sourcePath = args[i];
            } else {
                throw new Exception();
            }
            
            // Runtime input data file
            if (++i < args.length) {
                inputPath = args[i];
                
                File inputFile = new File(inputPath);
                if(!inputFile.exists()) {
                    System.out.println("Input file '" + inputPath + "' does not exist.");
                    throw new Exception();
                }
            }

            new Pascal(operation, sourcePath, inputPath, flags.toString());
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
                    String lineText = (String) body[1];

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

    private static final String INTERPRETER_SUMMARY_FORMAT =
            "\n%,20d statements executed." +
                    "\n%,20d runtime errors." +
                    "\n%,20.2f seconds total execution time.\n";

    private static final String COMPILER_SUMMARY_FORMAT =
            "\n%,20d instructions generated." +
                    "\n%,20.2f seconds total code generation time.\n";

    private static final String LINE_FORMAT =
            ">>> AT LINE %03d\n";

    private static final String ASSIGN_FORMAT =
            ">>> AT LINE %03d: %s = %s\n";

    private static final String FETCH_FORMAT =
            ">>> AT LINE %03d: %s : %s\n";

    private static final String CALL_FORMAT =
            ">>> AT LINE %03d: CALL %s\n";

    private static final String RETURN_FORMAT =
            ">>> AT LINE %03d: RETURN FROM %s\n";

    private class BackendMessageListener implements MessageListener {

        @Override
        public void messageReceived(Message message) {
            MessageType type = message.getType();

            switch (type) {
                case SOURCE_LINE: {
                    if (lines) {
                        int lineNumber = (Integer) message.getBody();
                        System.out.printf(LINE_FORMAT, lineNumber);
                    }
                    break;
                }
                case ASSIGN: {
                    if(assign) {
                        Object[] body = (Object[]) message.getBody();
                        int lineNumber = (Integer) body[0];
                        String variableName = (String) body[1];
                        Object value = body[2];
                        System.out.printf(ASSIGN_FORMAT, lineNumber, variableName, value);
                    }
                    break;
                }
                case FETCH: {
                    if (fetch) {
                        Object[] body = (Object[]) message.getBody();
                        int lineNumber = (Integer) body[0];
                        String variableName = (String) body[1];
                        Object value = body[2];

                        System.out.printf(FETCH_FORMAT, lineNumber, variableName, value);
                    }
                    break;
                }
                case CALL: {
                    if (call) {
                        Object[] body = (Object[]) message.getBody();
                        int lineNumber = (Integer) body[0];
                        String routineName = (String) body[1];

                        System.out.printf(CALL_FORMAT, lineNumber, routineName);
                    }
                    break;
                }
                case RETURN: {
                    if (returnn) {
                        Object[] body = (Object[]) message.getBody();
                        int lineNumber = (Integer) body[0];
                        String routineName = (String) body[1];

                        System.out.printf(RETURN_FORMAT, lineNumber, routineName);
                    }
                    break;
                }
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
                case RUNTIME_ERROR: {
                    /*
                    Object[] body = (Object[]) message.getBody();
                    String errorMessage = (String) body[0];
                    Integer lineNumber = (Integer) body[1];
                    System.out.print("*** RUNTIME ERROR");
                    if (lineNumber != null) {
                        System.out.print(" AT LINE " + String.format("%03d", lineNumber));
                    }
                    System.out.println(": " + errorMessage);

                     */
                    break;
                }
            }
        }
    }

}
