package frontend.pascal;

import frontend.TokenType;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public enum  PascalTokenType implements TokenType {
    // Reserved words.
    AND, ARRAY, BEGIN, CASE, CONST, DIV, DO, DOWNTO, ELSE, END,
    FILE, FOR, FUNCTION, GOTO, IF, IN, LABEL, MOD, NIL, NOT,
    OF, OR, PACKED, PROCEDURE, PROGRAM, RECORD, REPEAT, SET,
    THEN, TO, TYPE, UNTIL, VAR, WHILE, WITH,

    // Special symbols.
    PLUS("+"), MINUS("-"), STAR("*"), SLASH("/"), COLON_EQUALS(":="),
    DOT("."), COMMA(","), SEMICOLON(";"), COLON(":"), QUOTE("&apos;"),
    EQUALS("="), NOT_EQUALS("<>"), LESS_THAN("<"), LESS_EQUALS("<="),
    GREATER_EQUALS(">="), GREATER_THAN(">"), LEFT_PAREN("("),
    RIGHT_PAREN(")"), LEFT_BRACKET("["), RIGHT_BRACKET("]"), LEFT_BRACE("{"), RIGHT_BRACE("}"),
    UP_ARROW("^"), DOT_DOT(".."),

    IDENTIFIER, INTEGER, REAL, STRING,
    ERROR, END_OF_FILE;

    private static final int FIRST_RESERVED_INDEX = AND.ordinal();
    private static final int LAST_RESERVED_INDEX  = WITH.ordinal();

    private static final int FIRST_SPECIAL_INDEX = PLUS.ordinal();
    private static final int LAST_SPECIAL_INDEX  = DOT_DOT.ordinal();

    // Token text
    private final String text;

    PascalTokenType() {
        this.text = this.toString().toLowerCase();
    }

    PascalTokenType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    // Set of lower-cased Pascal reserved word text strings.
    public static Set<String> RESERVED_WORDS = new HashSet<String>();
    static {
        PascalTokenType[] values = PascalTokenType.values();
        for (int i = FIRST_RESERVED_INDEX; i <= LAST_RESERVED_INDEX; ++i) {
            RESERVED_WORDS.add(values[i].getText().toLowerCase());
        }
    }

    // Hash table of Pascal special symbols.  Each special symbol&apos;s text
    // is the key to its Pascal token type.
    public static Hashtable<String, PascalTokenType> SPECIAL_SYMBOLS = new Hashtable<>();
    static {
        PascalTokenType[] values = PascalTokenType.values();
        for (int i = FIRST_SPECIAL_INDEX; i <= LAST_SPECIAL_INDEX; ++i) {
            SPECIAL_SYMBOLS.put(values[i].getText(), values[i]);
        }
    }
}
