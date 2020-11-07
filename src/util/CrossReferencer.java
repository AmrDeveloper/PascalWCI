package util;

import intermediate.SymbolTable;
import intermediate.SymbolTableEntry;
import intermediate.SymbolTableStack;

import java.util.List;

public class CrossReferencer {

    private static final int NAME_WIDTH = 16;
    private static final String NAME_FORMAT = "%-" + NAME_WIDTH + "s";
    private static final String NUMBERS_LABEL = " Line numbers    ";
    private static final String NUMBERS_UNDERLINE = " ------------    ";
    private static final String NUMBER_FORMAT = "%03d";

    public static final int LABEL_WIDTH = NUMBERS_LABEL.length();
    public static final int INDENT_WIDTH = NAME_WIDTH + LABEL_WIDTH;

    public static final StringBuilder INDENT = new StringBuilder(INDENT_WIDTH);
    static {
        for(int i = 0 ; i < INDENT_WIDTH ; i++) INDENT.append(" ");
    }

    //Print the cross-reference table
    public void print(SymbolTableStack symbolTableStack) {
        System.out.println("\n===== CROSS-REFERENCE TABLE =====");
        printColumnReadings();
        
        printSymbolTable(symbolTableStack.getLocalSymbolTable());
    }
    
    private void printColumnReadings() {
        System.out.println();
        System.out.println(String.format(NAME_FORMAT, "Identifier") + NUMBERS_LABEL);
        System.out.println(String.format(NAME_FORMAT, "----------") + NUMBERS_UNDERLINE);
    }

    private void printSymbolTable(SymbolTable table) {
        List<SymbolTableEntry> sorted = table.sortedEntries();
        for(SymbolTableEntry entry : sorted) {
            List<Integer> lineNumbers = entry.getLineNumbers();
            
            System.out.printf(NAME_FORMAT, entry.getName());
            if(lineNumbers != null) {
                 for(Integer lineNumber : lineNumbers) {
                     System.out.printf(NUMBER_FORMAT, lineNumber);
                 }
            }
            System.out.println();
        }
    }
}
