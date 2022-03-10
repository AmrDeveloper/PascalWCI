package ide.util.Info;

import ide.IDEFrame;

import java.io.File;


public class WorkSpace {

    private File folder;

    private IDEFrame ideFrame;

    public WorkSpace(File folder, IDEFrame ideFrame) {
        this.folder = folder;
        this.ideFrame = ideFrame;
    }

    public IDEFrame getIDEFrame() {
        return ideFrame;
    }

    public void setIDEFrame(IDEFrame ideFrame) {
        this.ideFrame = ideFrame;
    }


    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

}
