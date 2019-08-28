/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx;

import com.objectdb.Utilities;
import java.net.URL;
import java.util.Collection;
import java.util.function.Supplier;
import javax.jdo.PersistenceManager;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.containers.LazyValue;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.MultiStageManager;
import lt.lb.objectdbjavafx.model.EAValue;
import lt.lb.objectdbjavafx.model.FileEntity;
import lt.lb.objectdbjavafx.model.FileEntityFolder;

/**
 *
 * @author laim0nas100
 */
public class Main {

    public static final String databaseUri = System.getProperty("user.dir") + "/db/database.odb";
    public static final LazyValue<PersistenceManager> pm = new LazyValue<>(() -> {
        return Utilities.getPersistenceManager(databaseUri);
    });

    public static MultiStageManager sceneManager = new MultiStageManager();

    public static void makeMetaEditWindow(EAValue val) {
        URL resource = sceneManager.getResource("/fxml/MetaEdit.fxml");
        F.unsafeRun(() -> {
            Frame newFrame = sceneManager.newFrame(resource, UUIDgenerator.nextUUID("MetaEdit"), "MetaEdit", false);
            EAVEditController controller = newFrame.getController();
            controller.value = val;
            FX.submit(() -> {
                newFrame.getStage().show();
                controller.update();
            });

        });
    }

    public static void makeNewWindow(FileEntityFolder folder) {
        // create main window
        URL resource = sceneManager.getResource("/fxml/MainWindow.fxml");
        F.unsafeRun(() -> {
            Frame newFrame = sceneManager.newFrame(resource, UUIDgenerator.nextUUID("MainWindow"), "Main window", false);
            FX.submit(() -> {
                MainWindowController controller = newFrame.getController();
                controller.folder = folder;
                newFrame.getStage().show();
                controller.update();
            });
        });
    }

    public static void makeNewWindow(Supplier<Collection<FileEntity>> update, String title) {
        // create main window
        URL resource = sceneManager.getResource("/fxml/MainWindow.fxml");
        F.unsafeRun(() -> {
            Frame newFrame = sceneManager.newFrame(resource, UUIDgenerator.nextUUID("MainWindow"), title, false);
            FX.submit(() -> {
                MainWindowController controller = newFrame.getController();
                controller.filePopulatingFunction = update;
                newFrame.getStage().show();
                controller.update();

            });
        });
    }

    public static void makeTextEditWindow(FileEntity entity) {
        URL resource = sceneManager.getResource("/fxml/TextEdit.fxml");
        F.unsafeRun(() -> {
            Frame newFrame = sceneManager.newFrame(resource, UUIDgenerator.nextUUID("TextEdit"), "TextEdit", false);
            TextEditController controller = newFrame.getController();
            controller.file = entity;
            FX.submit(() -> {
                newFrame.getStage().show();
                controller.update();
            });

        });
    }

    public static void updateAllWindows() {
        F.iterate(sceneManager.frames, (k, fr) -> {
            fr.getController().update();
        });
    }

    public static void main(String... args) {

        Log main = Log.main();
        main.surroundString = false;
        main.keepBufferForFile = false;
        main.stackTrace = true;
        main.timeStamp = false;
        main.threadName = false;
        main.display = true;
        
        URL resource = sceneManager.getResource("/fxml/Launcher.fxml");
        F.unsafeRun(() -> {
            Frame newFrame = sceneManager.newFrame(resource, "Launcher");
            LauncherController controller = newFrame.getController();
            FX.submit(() -> {
                newFrame.getStage().show();
            });
        });

    }
}
