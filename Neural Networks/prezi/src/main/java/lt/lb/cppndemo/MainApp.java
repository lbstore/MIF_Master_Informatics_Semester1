package lt.lb.cppndemo;

import java.net.URL;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.MultiStageManager;


public class MainApp{


    public static MultiStageManager manager;
    public static void main(String[] args) throws Exception{
        manager = new MultiStageManager();
        URL resource = manager.getResource("/fxml/Scene.fxml");
        Frame newFrame = manager.newFrame(resource, "CPPN demo");
        FX.submit(()->{
           newFrame.getStage().show();
        });
    }

}
