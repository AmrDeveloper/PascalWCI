package frontend;

public abstract class Parser {

    protected static SymTab symTab;

    protected Scanner scanner;
    protected ICode iCode;

    protected Parser(Scanner scanner)    {
        this.scanner = scanner;
    }

    public abstract void parse()  throws Exception;

    public abstract int getErrorCount();

    public Token currentToken() {
        return scanner.currentToken();
    }

    public Token nextToken() throws Exception {
        return scanner.nextToken();
    }
}
