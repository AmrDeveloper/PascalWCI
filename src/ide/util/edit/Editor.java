package ide.util.edit;


import frontend.EofToken;
import frontend.FrontendFactory;
import frontend.Parser;
import frontend.Source;
import frontend.pascal.PascalTokenType;
import ide.IDEFrame;
import ide.util.FileUtil;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static frontend.pascal.PascalTokenType.COMMENT;

public class Editor extends JTextPane {
    private static final Set<String> PREDEFINED = new HashSet<>();

    static {
        PREDEFINED.add("integer");
        PREDEFINED.add("real");
        PREDEFINED.add("boolean");
        PREDEFINED.add("false");
        PREDEFINED.add("true");
        PREDEFINED.add("char");
        PREDEFINED.add("read");
        PREDEFINED.add("readln");
        PREDEFINED.add("write");
        PREDEFINED.add("writeln");
        PREDEFINED.add("abs");
        PREDEFINED.add("arctan");
        PREDEFINED.add("chr");
        PREDEFINED.add("cos");
        PREDEFINED.add("eof");
        PREDEFINED.add("eoln");
        PREDEFINED.add("exp");
        PREDEFINED.add("ln");
        PREDEFINED.add("odd");
        PREDEFINED.add("ord");
        PREDEFINED.add("pred");
        PREDEFINED.add("round");
        PREDEFINED.add("sin");
        PREDEFINED.add("sqr");
        PREDEFINED.add("sqrt");
        PREDEFINED.add("succ");
        PREDEFINED.add("trunc");
    }

    protected StyledDocument doc;
    protected SyntaxFormatter formatter = new SyntaxFormatter("colorscheme/pascal.stx");
    private SimpleAttributeSet lineAttr = new SimpleAttributeSet();
    private int curStart = 0;
    private IDEFrame ideFrame;
    private boolean isContainRead;

    public Editor(File file, IDEFrame ideFrame) {
        this.ideFrame = ideFrame;
        this.setText(FileUtil.readFile(file));
        this.setBackground(new Color(0xDB, 0xDB, 0xDB));
        this.setForeground(new Color(0xFF, 0x00, 0x00));
        this.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        this.doc = getStyledDocument();

        this.setMargin(new Insets(3, 50, 0, 0));
        syntaxParse();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                syntaxParse();
            }
        });
    }

    public void syntaxParse() {
        try {
            Element root = doc.getDefaultRootElement();// content of doc

            // note : this is a low effective solution
            // render the modified part if you want to render faster.
            String s = doc.getText(root.getStartOffset(), root.getEndOffset() - 1);
            curStart = 0;

            // reuse the code of frontend package
            Source source = new Source(
                    new BufferedReader(new InputStreamReader(
                            new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)))));
            Parser parser = FrontendFactory.createParser("Pascal", "top-down", source);

            /*
             * my note for future development!
             * render the code is harder than I thought.So, this is a simple solution!
             * I do not test the code completely! So find the bug by using the program!
             */
            isContainRead = false;
            while (!(parser.nextToken() instanceof EofToken)) {
                renderComment(s);
                String token = parser.currentToken().getText();
                PascalTokenType tokenType = (PascalTokenType) parser.currentToken().getType();
                int tokenPos = s.indexOf(token, curStart);
                if (PREDEFINED.contains(token.toLowerCase())) {
                    tokenType = PascalTokenType.PREDEFINED;
                }
                // check read or readln
                if (token.equals("read") || token.equals("readln")) {
                    isContainRead = true;
                }

                formatter.setHighLight(doc, tokenType, tokenPos, token.length());
                curStart = tokenPos + token.length();

            }
            renderComment(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (isContainRead) {
            this.ideFrame.getRunButton().setEnabled(false);
        } else {
            this.ideFrame.getRunButton().setEnabled(true);
        }
    }

    private void renderComment(String s) {
        // attention: parser cannot find comment.
        // so diy your own comment render !
        while ((curStart < s.length()) &&
                (s.charAt(curStart) == ' ' || s.charAt(curStart) == '\t'
                        || s.charAt(curStart) == '\n' || s.charAt(curStart) == '{')) {
            if (curStart < s.length() && s.charAt(curStart) == '{') {
                int commentStart = curStart;
                int len = 1;
                while (s.charAt(curStart) != '}') {
                    ++curStart;
                    ++len;
                }
                formatter.setHighLight(doc, COMMENT, commentStart, len);
            }
            curStart++;
        }

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Element root = doc.getDefaultRootElement();
        int line = root.getElementIndex(doc.getLength());
        g.setColor(new Color(0xB0, 0xB0, 0xB0));
        g.fillRect(0, 0, this.getMargin().left, getSize().height);
        g.setColor(new Color(40, 40, 40));
        for (int count = 0, j = 1; count <= line; ++count, ++j) {
            g.drawString(String.format("%4d", j), 3, (int) ((count + 1) * 1.4999 * StyleConstants.getFontSize(lineAttr)));
        }
    }
}

class SyntaxFormatter {
    SimpleAttributeSet normalAttr = new SimpleAttributeSet();
    private Map<SimpleAttributeSet, ArrayList> attrMap = new HashMap<>();

    public SyntaxFormatter(String syntaxFile) {
        StyleConstants.setForeground(normalAttr, Color.black);
        try {
            Scanner scanner = new Scanner(new File(syntaxFile));
            int color = -1;
            ArrayList<String> patterns = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#")) {
                    if (patterns.size() > 0 && color > -1) {
                        SimpleAttributeSet attr = new SimpleAttributeSet();
                        StyleConstants.setForeground(attr, new Color(color));
                        attrMap.put(attr, patterns);
                    }
                    patterns = new ArrayList<>();
                    color = Integer.parseInt(line.substring(1), 16);
                } else {
                    if (line.trim().length() > 0) {
                        patterns.add(line.trim());
                    }
                }
            }
            if (patterns.size() > 0 && color > -1) {
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, new Color(color));
                attrMap.put(attr, patterns);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setHighLight(StyledDocument doc, PascalTokenType tokenType, int start, int length) {
        SimpleAttributeSet currentAttrSet = null;
        outer:
        for (SimpleAttributeSet attr : attrMap.keySet()) {
            ArrayList patterns = attrMap.get(attr);
            for (Object pattern : patterns) {
                if (pattern.equals(tokenType.toString())) {
                    currentAttrSet = attr;
                    break outer;
                }
            }
        }
        if (currentAttrSet != null) {
            doc.setCharacterAttributes(start, length, currentAttrSet, false);
        } else {

            doc.setCharacterAttributes(start, length, normalAttr, false);
        }
    }
}

