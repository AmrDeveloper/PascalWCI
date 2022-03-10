package ide;

import ide.pane.*;
import util.ParseTreePrinter;
import ide.tree.ProjectTreeNode;
import ide.tree.TreeCreatorImpl;
import ide.util.Info.WorkSpace;
import ide.util.add.AddFileHandler;
import ide.util.add.AddFolderHandler;
import ide.util.add.AddFrame;
import ide.util.add.AddInfo;
import ide.util.run.RunProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.File;

import static ide.IDEControl.*;

public class IDEFrame extends JFrame {
    private JSplitPane editSplitPane;
    private EditPane editPane;
    private JTabbedPane debugPane;
    private CallStackPane callStackPane;
    private ConsolePane consolePane;
    private FileBrowserPane fileBrowserPane;
    private ICodePane iCodePane;
    private OutputPane outputPane;
    private JSplitPane fileSplitPane;

    // menu bar
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu helpMenu;

    // tool bar
    private JToolBar toolBar;
    private Component runButton;

    private AddFrame addFrame;
    private WorkSpace workSpace;

    private Action newFile = new AbstractAction("New File", new ImageIcon("images/fileNew.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            createNewFile();
        }
    };
    private Action newFolder = new AbstractAction("New Folder", new ImageIcon("images/folderNew.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            createNewFolder();
        }
    };
    private Action openFile = new AbstractAction("Open File", new ImageIcon("images/open.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectFile(false);
        }
    };
    private Action openDir = new AbstractAction("Open Directory", new ImageIcon("images/open.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectFile(true);
        }
    };
    private Action exit = new AbstractAction("Exit") {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };
    private Action run = new AbstractAction("run", new ImageIcon("images/run.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            runProgram();
        }
    };
    private Action step = new AbstractAction("debug", new ImageIcon("images/step.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    };
    private Action go = new AbstractAction("go", new ImageIcon("images/resume.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    };
    private Action debug = new AbstractAction("debug", new ImageIcon("images/debug.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    };
    private Action terminate = new AbstractAction("quit", new ImageIcon("images/terminate.gif")) {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    };

    public IDEFrame(String title) {
        super(title);
    }

    private void createNewFile() {
        AddInfo info = new AddInfo("Create a new file", this, new AddFileHandler());
        showAddFrame(info);
    }

    private void showAddFrame(AddInfo info) {
        setEnabled(false);
        addFrame = new AddFrame(info);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    public void createNewFolder() {
        AddInfo info = new AddInfo("Create a new Folder", this, new AddFolderHandler());
        showAddFrame(info);
    }

    public void selectFile(boolean onlyDir) {
        new FileChooser(this, onlyDir);
    }

    private void runProgram() {

        if (editPane.getCurrentFile() == null) {
            JOptionPane.showMessageDialog(null, "No file selected");
            return;
        }
        // save file first!
        editPane.saveFile(editPane.getCurrentFile());
        String path = editPane.getCurrentFile().getFile().getAbsolutePath();
        String result = RunProcess.run(path);
        String[] strings = result.split("\n");
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder consoleBuilder = new StringBuilder();
        StringBuilder iCodeBuilder = new StringBuilder();
        for (int i = 0; i < strings.length; ++i) {
            if (strings[i].startsWith(SYNTAX_TAG)) {
                consoleBuilder.append(strings[i].substring(SYNTAX_TAG.length())).append('\n');
            } else if (strings[i].startsWith(PARSER_TAG)) {
                consoleBuilder.append(strings[i].substring(PARSER_TAG.length())).append('\n');
            } else if (strings[i].startsWith(INTERPRETER_TAG)) {
                consoleBuilder.append(strings[i].substring(INTERPRETER_TAG.length())).append('\n');
            } else if (strings[i].startsWith(RUNTIME_ERROR_TAG)) {
                consoleBuilder.append(strings[i].substring(RUNTIME_ERROR_TAG.length())).append("\n");
            } else if (strings[i].startsWith(ParseTreePrinter.BEGIN_ICODE)) {
                while (!strings[++i].startsWith(ParseTreePrinter.END_ICODE)) {
                    iCodeBuilder.append(strings[i]).append('\n');
                }
            } else if (strings[i].startsWith(LISTING_TAG)) {

            } else {
                outputBuilder.append(strings[i]).append('\n');
            }
        }
        consolePane.setInfo(consoleBuilder.toString());
        outputPane.setOutput(outputBuilder.toString());
        iCodePane.setICode(iCodeBuilder.toString());

    }

    public void initFrame(WorkSpace workSpace) {
        this.workSpace = workSpace;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        UIManager.put("TextArea.font", font);

        // menu
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
        // toolbar
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setMargin(new Insets(5, 5, 5, 5));
        add(toolBar, BorderLayout.NORTH);

        addListeners();
        toolBar.getComponent(1).setEnabled(false);
        toolBar.getComponent(2).setEnabled(false);
        toolBar.getComponent(3).setEnabled(false);
        toolBar.getComponent(4).setEnabled(false);

        // edit pane
        editPane = new EditPane(BoxLayout.Y_AXIS, this);
        // debug pane
        debugPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        debugPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        consolePane = new ConsolePane(new JTextArea());
        callStackPane = new CallStackPane();
        iCodePane = new ICodePane(new JTextArea());
        outputPane = new OutputPane(new JTextArea());
        // Pane list
        debugPane.add(consolePane, "Running information");
        debugPane.add(outputPane, "Output");
        debugPane.add(iCodePane, "Intermediate code");
        editSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editPane, debugPane);
        editSplitPane.setDividerSize(4);
        editSplitPane.setDividerLocation(500);

        add(editSplitPane);

        // file browser
        fileBrowserPane = new TreeCreatorImpl().createTree(this);
        fileSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(fileBrowserPane), editSplitPane);
        fileSplitPane.setDividerSize(4);
        fileSplitPane.setDividerLocation(300);

        add(fileSplitPane);

    }

    public WorkSpace getWorkSpace() {
        return workSpace;
    }

    private void addListeners() {
        fileMenu.add(newFile).setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_MASK));
        fileMenu.add(newFolder);
        fileMenu.add(openFile).setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_MASK));
        fileMenu.add(openDir).setEnabled(false);
        fileMenu.add(exit);

        toolBar.add(run).setToolTipText("Run");
        runButton = toolBar.getComponent(0);
        toolBar.add(debug).setToolTipText("debugging");
        toolBar.add(step).setToolTipText("Single step debugging");
        toolBar.add(go).setToolTipText("Resume operation");
        toolBar.add(terminate).setToolTipText("Quit debugging");
    }

    public ProjectTreeNode getSelectNode() {
        return fileBrowserPane.getSelectNode();
    }

    public void reloadNode(ProjectTreeNode selectNode) {
        fileBrowserPane.reloadNode(selectNode);
    }

    public void openFile(File file) {
        setTitle("PascalIDE(" + file.getAbsolutePath() + ")");
        editPane.openFile(file);
    }

    public Component getRunButton() {
        return runButton;
    }

    public FileBrowserPane getFileBrowserPane() {
        return fileBrowserPane;
    }


    public void clearDebugPane() {
        consolePane.setInfo("");
        outputPane.setOutput("");
        iCodePane.setICode("");
    }

}

class FileChooser extends JFileChooser {
    private IDEFrame ideFrame;

    public FileChooser(IDEFrame ideFrame, boolean onlyDir) {
        super("./");
        this.ideFrame = ideFrame;
        if (onlyDir) {
            this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        showOpenDialog(ideFrame);
    }

    @Override
    public void approveSelection() {
        File file = getSelectedFile();
        this.ideFrame.getFileBrowserPane().setSelectionPath(null);
        if (file.isDirectory()) {
        } else {
            this.ideFrame.openFile(file);
        }
        super.approveSelection();
    }

}
