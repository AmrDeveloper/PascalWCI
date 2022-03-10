package ide;

import ide.util.Info.WorkSpace;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;

public class SpaceFrame extends JFrame {
    private JPanel mainPanel;
    private JLabel infoLabel;
    private JPanel choosePanel;
    private JLabel workTextLabel;
    private JTextField pathText;
    private JButton chooseButton;
    private JPanel buttonPanel;
    private JButton confirmButton;
    private JButton cancleButton;
    private SpaceChooser chooser;
    private File folder;

    public SpaceFrame(IDEFrame ideFrame) {
        InitGlobalFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        mainPanel = new JPanel();
        infoLabel = new JLabel("Please select a workspace");
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        choosePanel = new JPanel();
        workTextLabel = new JLabel("Workspace");
        pathText = new JTextField("", 40);
        chooseButton = new JButton("Choose");
        buttonPanel = new JPanel();
        confirmButton = new JButton("Ok");
        cancleButton = new JButton("Cancel");
        chooser = new SpaceChooser(this);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(infoLabel);
        choosePanel.setLayout(new BoxLayout(choosePanel, BoxLayout.X_AXIS));
        chooseButton.addActionListener(new ChooseButtonListener(chooser));
        pathText.setEnabled(false);
        choosePanel.add(workTextLabel);
        choosePanel.add(pathText);
        choosePanel.add(chooseButton);
        mainPanel.add(choosePanel);

        confirmButton.setEnabled(false);
        confirmButton.addActionListener(new ConfirmButtonListener(this, ideFrame));

        buttonPanel.add(confirmButton);
        buttonPanel.add(new Label("            "));
        buttonPanel.add(cancleButton);

        cancleButton.addActionListener(e -> System.exit(0));// Amazing lambda!!

        mainPanel.add(buttonPanel);
        add(mainPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = mainPanel.getHeight();
        int width = mainPanel.getWidth();
        setLocation((screenSize.width - width) / 2, screenSize.height / 2 - height);
        setResizable(false);
    }

    private void InitGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }


    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public JTextField getPathText() {
        return pathText;
    }

    public JButton getConfirmButton() {
        return confirmButton;
    }
}

class ConfirmButtonListener implements ActionListener {
    private SpaceFrame spaceFrame;
    private IDEFrame ideFrame;

    public ConfirmButtonListener(SpaceFrame spaceFrame, IDEFrame ideFrame) {
        this.spaceFrame = spaceFrame;
        this.ideFrame = ideFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // !init here
        ideFrame.initFrame(new WorkSpace(spaceFrame.getFolder(), ideFrame));
        ideFrame.setVisible(true);
        spaceFrame.setVisible(false);
    }
}

class ChooseButtonListener implements ActionListener {
    private JFileChooser chooser;

    public ChooseButtonListener(JFileChooser chooser) {
        this.chooser = chooser;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.showOpenDialog(null);//show file chooser
    }
}

class SpaceChooser extends JFileChooser {
    private SpaceFrame spaceFrame;

    public SpaceChooser(SpaceFrame spaceFrame) {
        super("./");
        this.spaceFrame = spaceFrame;
    }

    @Override
    public void approveSelection() {
        super.approveSelection();
        File folder = getSelectedFile();
        spaceFrame.setFolder(folder);
        if (folder.getAbsolutePath().contains(" ")) {
            JOptionPane.showMessageDialog(null, "Path cannot contain spaces!");
            return;
        }
        spaceFrame.getPathText().setText(folder.getAbsolutePath());
        spaceFrame.getConfirmButton().setEnabled(true);
    }
}
