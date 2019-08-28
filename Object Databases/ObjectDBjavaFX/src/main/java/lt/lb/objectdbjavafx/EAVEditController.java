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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.InjectableController;
import lt.lb.objectdbjavafx.model.EAValue;
import lt.lb.objectdbjavafx.model.Meta;

/**
 *
 * @author laim0nas100
 */
public class EAVEditController implements InjectableController {

    public EAValue value;

    @FXML
    public Label label;
    @FXML
    public TextField field;

    @Override
    public void inject(Frame frame, URL url, ResourceBundle rb) {
    }

    @Override
    public void exit() {

        Main.updateAllWindows();
    }

    @Override
    public void update() {
        field.setText(value.get() + "");
        label.setText("Edit EAVValue with meta "+value.getMeta().getName()+" of type "+value.getMeta().getValType());
    }

    public void save() {

        try {
            Meta meta = value.getMeta();
            if (null != meta.getValType()) {
                switch (meta.getValType()) {
                    case BOOLEAN:
                        value.set(Boolean.parseBoolean(field.getText()));
                        break;
                    case DATE:
                        value.set(Date.parse(field.getText()));
                        return;
                    case DECIMAL:
                        value.set(Double.parseDouble(field.getText()));
                        return;
                    case INTEGER:
                        value.set(Integer.parseInt(field.getText()));
                        return;
                    case STRING:
                        value.set(field.getText());
                    default:
                        break;
                }
            }
            Main.updateAllWindows();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getLocalizedMessage(), ButtonType.OK);
            ex.printStackTrace();
            alert.showAndWait();
        }

    }

    @Override
    public void initialize() {
    }

}
