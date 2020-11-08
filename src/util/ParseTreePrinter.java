package util;

import intermediate.ICode;
import intermediate.ICodeKey;
import intermediate.ICodeNode;
import intermediate.SymbolTableEntry;
import intermediate.icodeimpl.ICodeNodeImpl;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParseTreePrinter {

    private static final int INDENT_WIDTH = 4;
    private static final int LINE_WIDTH = 80;

    private PrintStream printStream;        // output print stream
    private int length;                     // output line length
    private String indent;                  // indent spaces
    private String indentation;             // indentation of a line
    private StringBuilder line;             // output line

    public ParseTreePrinter(PrintStream printStream) {
        this.printStream = printStream;
        this.length = 0;
        this.indentation = "";
        this.line = new StringBuilder();

        this.indent = "";
        for (int i = 0; i < INDENT_WIDTH; i++) {
            this.indent += "";
        }
    }

    public void print(ICode iCode) {
        printStream.println("\n===== INTERMEDIATE CODE =====\n");

        printNode((ICodeNodeImpl) iCode.getRoot());
        printLine();
    }

    private void printNode(ICodeNodeImpl node) {
        append(indentation);
        append("<" + node.toString());

        printAttributes(node);
        printTypeSpec(node);

        List<ICodeNode> childNodes = node.getChildren();
        if((childNodes != null) && (childNodes.size() > 0)) {
            append(">");
            printLine();

            printChildNodes(childNodes);
            append(indentation);
            append("</" + node.toString() + ">");
        }
        else {
            append(" ");
            append("/>");
        }

        printLine();
    }

    private void printAttributes(ICodeNodeImpl node) {
        String saveIndentation = indentation;
        indentation += indent;

        Set<Map.Entry<ICodeKey, Object>> attributes = node.entrySet();
        for (Map.Entry<ICodeKey, Object> attribute : attributes) {
            printAttribute(attribute.getKey().toString(), attribute.getValue());
        }

        indentation = saveIndentation;
    }

    private void printAttribute(String key, Object value) {
        boolean isSymbolTableEntry = value instanceof SymbolTableEntry;
        String valueString = (isSymbolTableEntry) ?
                ((SymbolTableEntry) value).getName() : value.toString();

        String text = key.toLowerCase() + "=\"" + valueString + "\"";
        append(" ");
        append(text);

        if(isSymbolTableEntry) {
            int level = ((SymbolTableEntry) value).getSymbolTable().getNestingLevel();
            printAttribute("LEVEL", level);
        }
    }

    private void printChildNodes(List<ICodeNode> childNodes) {
        String saveIndentation = indentation;
        indentation += indent;

        for(ICodeNode child : childNodes) {
            printNode((ICodeNodeImpl) child);
        }

        indentation = saveIndentation;
    }

    private void printTypeSpec(ICodeNodeImpl node) {}

    private void append(String text) {
        int textLength = text.length();
        boolean lineBreak = false;

        if((length + textLength) > LINE_WIDTH) {
            printLine();
            line.append(indentation);
            length = indentation.length();
            lineBreak = true;
        }

        if(!(lineBreak && " ".equals(text))) {
            line.append(text);
            length += textLength;
        }
    }

    private void printLine() {
        if (length > 0) {
            printStream.println(line);
            line.setLength(0);
            length = 0;
        }
    }
}
