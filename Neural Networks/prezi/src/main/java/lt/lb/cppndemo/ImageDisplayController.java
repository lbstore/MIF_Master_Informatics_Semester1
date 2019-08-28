/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.cppndemo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.Log;
import lt.lb.commons.containers.BooleanValue;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuple3;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.InjectableController;
import lt.lb.commons.threads.FastWaitingExecutor;
import lt.lb.commons.threads.TaskBatcher;
import lt.lb.cppndemo.FXMLController.LPixel;
import lt.lb.neurevol.neural.NNInfo;
import lt.lb.neurevol.neural.NeuralNetwork;

/**
 * FXML Controller class
 *
 * @author Lemmin
 */
public class ImageDisplayController implements InjectableController {

    public static final String resource = "/fxml/ImageDisplay.fxml";
    public static AtomicLong idGen = new AtomicLong(1);

    @FXML
    public ImageView view;

    public BooleanValue isCanceled = BooleanValue.FALSE();

    public Frame frame;
    public Lambda.L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel> neuralNetWriterL;
    public Lambda.L2<PixelWriter, LPixel> pixelWriterL;
    public NNInfo info;

    @Override
    public void initialize() {
        frame.getScene().setOnKeyPressed(kh -> {
            if (kh.isShiftDown() && "e".equalsIgnoreCase(kh.getText())) {
                Log.print("Exporting");
                F.unsafeRunWithHandler(FXMLController::alert, () -> {
                    BufferedImage fromFXImage = SwingFXUtils.fromFXImage(view.getImage(), null);
                    ImageIO.write(fromFXImage, "png", new File(new SimpleDateFormat("YYYY-MM-DD HH-mm-ss").format(new Date()) + " " + frame.getTitle() + ".png"));
                });
            }
        });

    }

    Executor exe = new FastWaitingExecutor(4);

    public FutureTask updateImage(int w, int h, PixelWriter pixelWriter) {
        ThreadLocal<NeuralNetwork> tl = ThreadLocal.withInitial(() -> {
            NeuralNetwork net = new NeuralNetwork(info);
            return net;
        });
        Callable r = () -> {
            Log.print("In callable");
            TaskBatcher batcher = new TaskBatcher(exe);
            for (int i = 0; i < h; i++) {
                final int ii = i;
                Callable call = () -> {

                    LPixel[] line = new LPixel[w];
                    Optional<Throwable> checkedRun = F.checkedRun(() -> {
                        for (int j = 0; j < w; j++) {
                            LPixel pixel = neuralNetWriterL.apply(tl.get(), j, ii, w, h);
                            line[j] = pixel;
                        }
                    });

                    if (checkedRun.isPresent()) {
                        checkedRun.get().printStackTrace();
                    }

                    FX.submit(() -> {
                        for (LPixel pixel : line) {
//                            Log.print(pixel);
                            pixelWriterL.apply(pixelWriter, pixel);
                        }
                    }).get();
                    return null;
                };
                batcher.execute(call);

            }
            TaskBatcher.BatchRunSummary sum = batcher.awaitFailOnFirst();
            Log.print("After batcher");
            Log.print(sum.total, sum.successful, sum.failures);

            return null;
        };
        return new FutureTask(r);
    }

    public FutureTask setImage(int w, int h, Tuple3<NNInfo, Lambda.L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel>, Lambda.L2<PixelWriter, LPixel>> tuple) {
        this.info = tuple.getG1();
        PixelWriter pixelWriter = this.prepareImage(w, h, new Tuple<>(tuple.g2, tuple.g3));
        return this.updateImage(w, h, pixelWriter);

    }

    public PixelWriter prepareImage(int w, int h, Tuple<Lambda.L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel>, Lambda.L2<PixelWriter, LPixel>> tuple) {
        WritableImage fxImage = new WritableImage(w, h);
        view.setImage(fxImage);
        frame.getStage().setHeight(Math.max(h, 200)+10);
        frame.getStage().setWidth(Math.max(w, 200)+10);
        this.pixelWriterL = tuple.getG2();
        this.neuralNetWriterL = tuple.getG1();
        return fxImage.getPixelWriter();
    }

    @Override
    public void inject(Frame frame, URL url, ResourceBundle rb) {
        this.frame = frame;
    }

}
