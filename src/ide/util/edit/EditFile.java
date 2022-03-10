package ide.util.edit;

import javax.swing.*;
import java.io.File;

public class EditFile {
    private File file;
    private boolean saved;
    private JInternalFrame iframe;
    private Editor editor;

    public EditFile(File file, boolean saved, JInternalFrame iframe, Editor editor) {
        this.file = file;
        this.saved = saved;
        this.iframe = iframe;
        this.editor = editor;
    }

    public Editor getEditor() {
        return editor;
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    public JInternalFrame getIframe() {
        return iframe;
    }

    public void setIframe(JInternalFrame iframe) {
        this.iframe = iframe;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
