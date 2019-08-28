/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.cppndemo;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.Log;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuple3;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.neurevol.evolution.NEAT.Genome;
import lt.lb.neurevol.evolution.NEAT.imp.DefaultHyperNEATMutator;
import lt.lb.neurevol.evolution.NEAT.interfaces.AgentMutator;
import lt.lb.neurevol.neural.NNInfo;
import lt.lb.neurevol.neural.NeuralNetwork;

/**
 *
 * @author Lemmin
 */
public class EvolvingImageDisplayController extends ImageDisplayController {

    public static final String resource = "/fxml/EvolvingImageDisplay.fxml";
    public static AtomicLong idGen = new AtomicLong(1);

    public CheckBox paused;

    @FXML
    public TextField tHeight;

    @FXML
    public TextField tWidth;
    private Tuple<Integer, Integer> dim;

    private Genome genome;
    private AgentMutator<Genome> mutator;

    @Override
    public void initialize() {
    }

    public void exit() {
        serv.shutdown();
    }

    ScheduledExecutorService serv = Executors.newSingleThreadScheduledExecutor();

    public void setMutating(Genome genome, AgentMutator<Genome> mutator) {

        this.genome = genome;
        this.mutator = mutator;
        Runnable run = () -> {
            if (!this.paused.isSelected()) {
                this.mutateAndDisplay();
            }
        };
        serv.scheduleWithFixedDelay(run, 1, 1, TimeUnit.SECONDS);

    }

    public void rerender() {
        F.unsafeRunWithHandler(FXMLController::alert, () -> {
            int w = Integer.parseInt(tWidth.getText());
            int h = Integer.parseInt(tHeight.getText());
            Log.print("Generate new image");
            URL res = getClass().getResource(ImageDisplayController.resource);
            Frame newFrame = MainApp.manager.newFrame(res, "Image " + ImageDisplayController.idGen.getAndIncrement());
            ImageDisplayController contr = F.cast(newFrame.getController());

            Tuple3<NNInfo, Lambda.L5R<NeuralNetwork, Integer, Integer, Integer, Integer, FXMLController.LPixel>, Lambda.L2<PixelWriter, FXMLController.LPixel>> tuple3 = Tuples.create(this.info, this.neuralNetWriterL, this.pixelWriterL);
            FX.submit(() -> {
                newFrame.getStage().show();
                FutureTask setImage = contr.setImage(w, h, tuple3);
                new Thread(setImage).start();
            });
        });

    }

    public void branch() {
        F.unsafeRunWithHandler(FXMLController::alert, () -> {
            int w = Integer.parseInt(tWidth.getText());
            int h = Integer.parseInt(tHeight.getText());
            Log.print("Evolve new image");
            URL resource = getClass().getResource(EvolvingImageDisplayController.resource);
            Frame newFrame = MainApp.manager.newFrame(resource, "Evolving Image " + EvolvingImageDisplayController.idGen.getAndIncrement());
            EvolvingImageDisplayController contr = F.cast(newFrame.getController());

            FX.submit(() -> {
                Log.print("In FX.submit");
                newFrame.getStage().show();
                DefaultHyperNEATMutator mutator = new DefaultHyperNEATMutator(RandomDistribution.uniform(new FastRandom()));
                Log.print("Before prepare image");
                contr.prepareImage(w, h, Tuples.create(this.neuralNetWriterL, this.pixelWriterL));
                Log.print("After prepare image");
                Genome genome = F.cast(this.genome.clone());
                NeuralNetwork network = genome.getNetwork();
                contr.info = network.toNNInfo(Genome.activationMap, F::sigmoid);
                contr.setMutating(genome, mutator);
                Log.print("Set mutating");
            });
        });

    }

    public void mutateAndDisplay() {
        Optional<Throwable> ex = F.checkedRun(() -> {
            mutator.mutate(genome);
            NeuralNetwork network = genome.getNetwork();
            NNInfo toNNInfo = network.toNNInfo(info.activationMap, info.defaultActivation);
            info = toNNInfo;
            WritableImage image = F.cast(view.getImage());
            this.updateImage(dim.g1, dim.g2, image.getPixelWriter()).run();
        });
        if (ex.isPresent()) {
            ex.get().printStackTrace();
        }

    }

    @Override
    public PixelWriter prepareImage(int w, int h, Tuple<Lambda.L5R<NeuralNetwork, Integer, Integer, Integer, Integer, FXMLController.LPixel>, Lambda.L2<PixelWriter, FXMLController.LPixel>> tuple) {
        this.dim = new Tuple<>(w, h);
        tWidth.setText(w + "");
        tHeight.setText(h + "");
        PixelWriter prepareImage = super.prepareImage(w, h, tuple);
        frame.getStage().setHeight(Math.max(h, 200) + 115);
        frame.getStage().setWidth(Math.max(w, 200) + 35);
        return prepareImage;
    }

    @Override
    public void inject(Frame frame, URL url, ResourceBundle rb) {
        this.frame = frame;
    }

}
