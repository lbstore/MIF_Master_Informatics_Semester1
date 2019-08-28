/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lt.lb.commons.Log;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.InjectableController;
import lt.lb.objectdbjavafx.model.FileEntity;

/**
 * FXML Controller class
 *
 * @author laim0nas100
 */
public class TextEditController implements InjectableController {

    @FXML
    public TextArea area;

    public FileEntity file;

    @Override
    public void inject(Frame frame, URL url, ResourceBundle rb) {
    }

    @Override
    public void initialize() {
    }

    @Override
    public void update() {
        FX.submit(() -> {
            String string = file.getMeta().getString(MetaEnums.textContent);
            Log.print("Text:", string);
            area.setText(string);
        });
    }

    public void save() {
        Q.submit((pm) -> {
            
            file.getMeta().get(MetaEnums.textContent).setValue(area.getText());
            file.getMeta().get(MetaEnums.lastModifiedDate).setValue(new Date());
            pm.refresh(file);
        });
    }

    @Override
    public void exit() {
        save();
        Main.updateAllWindows();
    }

}
