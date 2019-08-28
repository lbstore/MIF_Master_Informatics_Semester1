package lt.lb.cppndemo;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.Lambda.L2;
import lt.lb.commons.Lambda.L5R;
import lt.lb.commons.Log;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuple3;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.InjectableController;
import lt.lb.commons.misc.Interval;
import lt.lb.commons.misc.Pos;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.parsing.CommentParser;
import lt.lb.neurevol.evolution.NEAT.Genome;
import lt.lb.neurevol.evolution.NEAT.HyperNEAT.HGenome;
import lt.lb.neurevol.evolution.NEAT.imp.DefaultHyperNEATMutator;
import lt.lb.neurevol.neural.NNInfo;
import lt.lb.neurevol.neural.NeuralNetwork;
import lt.lb.neurevol.neural.NeuronInfo;
import lt.lb.neurevol.neural.Synapse;

public class FXMLController implements InjectableController {

    @FXML
    CheckBox monochrome;

    @FXML
    TextArea area;
    @FXML
    TextField tWidth;
    @FXML
    TextField tHeight;

    @FXML
    private void handleButtonAction(ActionEvent event) throws Exception {

        F.unsafeRunWithHandler(FXMLController::alert, () -> {
            int w = Integer.parseInt(tWidth.getText());
            int h = Integer.parseInt(tHeight.getText());
            Log.print("Generate new image");
            URL resource = getClass().getResource(ImageDisplayController.resource);
            Frame newFrame = MainApp.manager.newFrame(resource, "Image " + ImageDisplayController.idGen.getAndIncrement());
            ImageDisplayController contr = F.cast(newFrame.getController());
            Tuple<L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel>, L2<PixelWriter, LPixel>> makeWriter = this.makeWriter();

            Tuple3<NNInfo, L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel>, L2<PixelWriter, LPixel>> tuple3 = new Tuple3<>(this.parseNNInfoText(), makeWriter.g1, makeWriter.g2);

            FX.submit(() -> {
                newFrame.getStage().show();
                FutureTask setImage = contr.setImage(w, h, tuple3);
                new Thread(setImage).start();
            });
        });

    }

    @FXML
    private void evolveBois() {

        F.unsafeRunWithHandler(FXMLController::alert, () -> {
            int w = Integer.parseInt(tWidth.getText());
            int h = Integer.parseInt(tHeight.getText());
            Log.print("Evolve new image");
            URL resource = getClass().getResource(EvolvingImageDisplayController.resource);
            Frame newFrame = MainApp.manager.newFrame(resource, "Evolving Image " + EvolvingImageDisplayController.idGen.getAndIncrement());
            EvolvingImageDisplayController contr = F.cast(newFrame.getController());
            Tuple<L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel>, L2<PixelWriter, LPixel>> makeWriter = this.makeWriter();

            FX.submit(() -> {
                Log.print("In FX.submit");
                newFrame.getStage().show();
                DefaultHyperNEATMutator mutator = new DefaultHyperNEATMutator(RandomDistribution.uniform(new FastRandom()));
                Log.print("Before prepare image");
                contr.prepareImage(w, h, makeWriter);
                Log.print("After prepare image");
                int output = monochrome.isSelected() ? 1 : 3;
                Genome genome = new Genome(3, output);
                NeuralNetwork network = genome.getNetwork();
                contr.info = network.toNNInfo(Genome.activationMap, F::sigmoid);
                contr.setMutating(genome, mutator);
                Log.print("Set mutating");
            });
        });

    }

    public static void alert(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.showAndWait();

    }

    @Override
    public void initialize() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public void update() {
    }

    @Override
    public void inject(Frame frame, URL url, ResourceBundle rb) {
    }

    public static class LPixel {

        public Pos pos;
        public Integer r;
        public Integer g;
        public Integer b;

        public String toString() {
            return pos.toString() + " " + r + " " + g + " " + b;
        }
    }

    Lambda.L1R<Double, Integer> toRbg = Lambda.of(x -> (int) (Math.round(Interval.ZERO_ONE.clamp((x+1d)/2d) * 255)));

    public L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel> generateImage2() {

        
        return Lambda.of((net, x1, y1, w, h) -> {
            double xn = (double)x1/w;
            double yn = (double)y1 /h;
            double x = (xn * 2) - 1;
            double y = (yn * 2) - 1;
            double xa = Math.abs(x);
            double ya = Math.abs(y);
            double d = Math.sqrt(x*x + y*y);
            Double[] evaluate = net.evaluate(ArrayOp.asArray(x, y, d));
            LPixel pixel = new LPixel();
            pixel.pos = new Pos(x1, y1);
            pixel.r = toRbg.apply(evaluate[0]);
            if (evaluate.length > 1) {
                pixel.g = toRbg.apply(evaluate[1]);
                pixel.b = toRbg.apply(evaluate[2]);
            } else {
                pixel.g = pixel.r;
                pixel.b = pixel.r;
            }
            return pixel;
        });
    }

    public NNInfo parseNNInfoText() {
        NNInfo info = new NNInfo();
        info.inputs = 3;

        info.outputs = 1;
        info.defaultActivation = F::sigmoid;
        info.activationMap = HGenome.getDefaultActivationMap();

        List<String> asList = Arrays.asList(this.area.getText().split("\n"));
        LinkedList<String> params = new LinkedList<>(CommentParser.parseAllComments(asList, "#", "/*", "*/"));
        Predicate<String> empty = (s) -> {
            return s == null || s.isEmpty() || s.trim().isEmpty();
        };
        F.filterInPlace(params, empty.negate());
        Log.main().async = false;
        Log.printLines(params);
        int hidden = Integer.parseInt(params.pollFirst().trim());
        ArrayList<NeuronInfo> infos = new ArrayList<>();

        Function<String[], NeuronInfo> parseMe = (val) -> {
            NeuronInfo ni = new NeuronInfo();
            ni.afType = Integer.parseInt(val[0]);
            ni.bias = Double.parseDouble(val[1]);
            return ni;

        };
        for (int i = 0; i < info.inputs; i++) {
            String[] val = params.pollFirst().trim().split(" ");
            infos.add(parseMe.apply(val));
        }

        for (int i = 0; i < info.outputs; i++) {
            String[] val = params.pollFirst().trim().split(" ");
            infos.add(parseMe.apply(val));
        }

        for (int i = 0; i < hidden; i++) {
            String[] val = params.pollFirst().trim().split(" ");
            infos.add(parseMe.apply(val));
        }
        ArrayList<Synapse> synapses = new ArrayList<>();
        F.iterate(params, (i, s) -> {
            String[] val = s.trim().split(" ");
            Log.print("Parsing synapse:");
            Log.print(Arrays.asList(val));
            synapses.add(new Synapse(
                    Integer.parseInt(val[0]),
                    Integer.parseInt(val[1]),
                    Double.parseDouble(val[2])
            ));
        });

        info.biases = infos;
        info.links = synapses;
        Log.main().async = true;
        return info;
    }

    public NNInfo parseNNInfo() {
        NNInfo info = new NNInfo();
        info.inputs = 3;
        info.outputs = 1;
        info.defaultActivation = F::sigmoid;
        info.activationMap = HGenome.getDefaultActivationMap();

        ArrayList<NeuronInfo> infos = new ArrayList<>();

        // 2 input 1 output 1 hidden
        for (int i = 0; i < info.inputs; i++) {
            F.unsafeRun(() -> {
                NeuronInfo ni = new NeuronInfo();
                ni.afType = 0;
                ni.bias = 0.001d;
                infos.add(ni);
            });
        }

        for (int i = 0; i < info.outputs; i++) {
            F.unsafeRun(() -> {
                NeuronInfo ni = new NeuronInfo();
                ni.afType = 2;
                ni.bias = 0.001d;
                infos.add(ni);
            });
        }
        F.unsafeRun(() -> {
            NeuronInfo ni = new NeuronInfo();
            ni.afType = 2;
            ni.bias = 0.0d;
            infos.add(ni);
        });
        F.unsafeRun(() -> {
            NeuronInfo ni = new NeuronInfo();
            ni.afType = 4;
            ni.bias = 0.0d;
            infos.add(ni);
        });

        // 0 1, 2, 3
        // 0,1,2 - input
        // 3 - output
        // 4,5 - hidden
        //links 
        ArrayList<Synapse> synapses = new ArrayList<>();
        synapses.add(new Synapse(0, 4, 1.666));
        synapses.add(new Synapse(1, 4, -0.777));
        synapses.add(new Synapse(2, 4, -0.888));
        synapses.add(new Synapse(4, 5, 1.111));
        synapses.add(new Synapse(5, 3, 1));
        info.biases = infos;
        info.links = synapses;
        return info;
    }

    public L2<PixelWriter, LPixel> writer() {
        L2<PixelWriter, LPixel> writer = (wr, pixel) -> {
            int w1 = pixel.pos.get()[0].intValue();
            int h1 = pixel.pos.get()[1].intValue();
            wr.setColor(w1, h1, javafx.scene.paint.Color.rgb(pixel.r, pixel.g, pixel.b));

        };
        return writer;
    }

    public Tuple<L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel>, L2<PixelWriter, LPixel>> makeWriter() {
        L5R<NeuralNetwork, Integer, Integer, Integer, Integer, LPixel> generateImage = this.generateImage2();

        return new Tuple<>(generateImage, writer());
    }
}
