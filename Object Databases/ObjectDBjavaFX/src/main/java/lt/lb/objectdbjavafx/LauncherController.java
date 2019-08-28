/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import lt.lb.commons.Log;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.InjectableController;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 */
public class LauncherController implements InjectableController {

    Frame frame;

    @Override
    public void inject(Frame frame, URL url, ResourceBundle rb) {
        this.frame = frame;
    }

    @Override
    public void initialize() {
    }

    public void begin() {
        Main.makeNewWindow(FS.getMainFolder());
    }

    public void bootstrap() {
        Bootstrap.full();
    }

    public void tests() {
        Log.println("", "#############", "   Tests!!!","#############");
        JDOTests.selectAllFolders();
        JDOTests.selectJustFiles();
        JDOTests.selectEAVbyMetaRoot();
        JDOTests.selectEAVbyMetaFileName();
        JDOTests.fullTextSearch1();
        JDOTests.fullTextSearch2();

        JDOTests.selectByTypeTests();
    }

    public void exit() {
        ArrayList<Runnable> actions = new ArrayList<>();
        Main.sceneManager.frames.values().stream().filter(f -> !StringOp.equals(frame.getID(), f.getID())).forEach(f -> {
            actions.add(() -> {
                Main.sceneManager.closeFrame(f.getID());
            });
        });
        actions.add(Main.pm.get()::close);
        actions.forEach(Runnable::run);
    }

}
