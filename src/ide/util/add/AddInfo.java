package ide.util.add;

import ide.IDEFrame;

public class AddInfo {
    private String info;// file name or folder name
    private IDEFrame ideFrame; // frame
    private AddHandler handler;// handler class after click add button

    public AddInfo(String info, IDEFrame ideFrame, AddHandler handler) {
        this.info = info;
        this.ideFrame = ideFrame;
        this.handler = handler;
    }

    public AddHandler getHandler() {
        return handler;
    }

    public void setHandler(AddHandler handler) {
        this.handler = handler;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public IDEFrame getIDEFrame() {
        return ideFrame;
    }

    public void setIDEFrame(IDEFrame ideFrame) {
        this.ideFrame = ideFrame;
    }
}
