/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx;

import com.objectdb.Enhancer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.objectdbjavafx.model.EAVType;
import lt.lb.objectdbjavafx.model.FileEntity;
import lt.lb.objectdbjavafx.model.FileEntityFolder;

/**
 *
 * @author laim0nas100
 */
public class Bootstrap {

    public static void bootstrapMeta() {
        Optional<Throwable> submit = Q.submit(pm -> {
            Log.print("Bootstrap Meta");
            FS.createMeta(MetaEnums.fileName, EAVType.STRING);
            FS.createMeta(MetaEnums.root, EAVType.BOOLEAN);
            FS.createMeta(MetaEnums.createdDate, EAVType.DATE);
            FS.createMeta(MetaEnums.lastModifiedDate, EAVType.DATE);
            FS.createMeta(MetaEnums.textContent, EAVType.STRING);
            FS.createMeta(MetaEnums.parentID, EAVType.STRING);
            Log.print("Bootstrap meta ok");
        });
        if (submit.isPresent()) {
            throw new RuntimeException(submit.get());
        }
    }

    public static void bootstrapFileSystem() {
        //create few files
        Optional<Throwable> submit = Q.submit((em) -> {

            Log.print("Bootsrap FS");
            FileEntityFolder folder = FS.createFolder("MainFolder");

            folder.getMeta().addAttribute(FS.valueOf("root", true));

            F.unsafeRun(() -> {
                FileEntity f1 = FS.createTextFile("File1.txt", "Some text f1");

                FileEntity f2 = FS.createTextFile("File2.txt", "Some other text f2");

                FS.addAssignFolder(folder, f1);
                FS.addAssignFolder(folder, f2);
            });

            F.unsafeRun(() -> {

                FileEntity f1 = FS.createTextFile("File11.txt", "Some text f11");

                FileEntity f2 = FS.createTextFile("File22.txt", "Some other text f22");
                FileEntityFolder deeper = FS.createFolder("DeeperFolder");
                FS.addAssignFolder(deeper, f1);
                FS.addAssignFolder(deeper, f2);
                FS.addAssignFolder(folder,deeper);
            });

            Log.print("Total file count:" + folder.count());

            Log.print("Boosrap FS ok");
        });
        if (submit.isPresent()) {
            throw new RuntimeException(submit.get());
        }
    }
    
    public static void full(){
        // delete prev database, ignore if not found 
        F.checkedRun(() -> {
            Log.print("Database uri:"+Main.databaseUri);
            File file = new File(Main.databaseUri);
            Path parent = Paths.get(file.getAbsolutePath()).getParent();
            Log.print("Parent", parent.toAbsolutePath(), Files.isDirectory(parent));
            Files.walk(parent).forEach(path -> {
                if (!Files.isDirectory(path)) {
                    F.unsafeRun(() -> {
                        boolean deleted = Files.deleteIfExists(path);
                        Log.print("Deleted:", path, deleted);
                    });

                }
            });
        });

        // set up db
        Bootstrap.bootstrapMeta();
        Bootstrap.bootstrapFileSystem();

        Log.print("DB setup ok");
    }
}
