/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.tensorflowcnn;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author laim0nas100
 */
public class DL4JCNN {

    public int channels = 1;
    public int height = 100;
    public int width = 100;
    public int batchSize = 20;
    public String rootPath = "E:\\University\\MIF_Master_Informatics_Semester1\\Neural Networks\\NN data\\";
    public String localPath = rootPath + "Cepstr_full\\no_of_classes_10\\";

    public FileSplit getData(String from) throws Exception {
        File dataFile = new File(from);
        return new FileSplit(dataFile, NativeImageLoader.ALLOWED_FORMATS);
    }

    public RecordReaderDataSetIterator getDataset(String from) throws Exception {
        FileSplit data = this.getData(from);
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);

        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
        int numLabels = data.getRootDir().listFiles(File::isDirectory).length;
        RecordReaderDataSetIterator dataSetIterator = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
        recordReader.initialize(data);
        scaler.fit(dataSetIterator);

        dataSetIterator.setPreProcessor(scaler);
        return dataSetIterator;
    }

    public void run() throws Exception {
        Log.print("Input data");
//        
        localPath = "D:\\no_of_classes_10\\";

        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();

        FileSplit trainingSplit = this.getData(localPath + "train");
        long numExamples = trainingSplit.length();

        int numLabels = trainingSplit.getRootDir().listFiles(File::isDirectory).length;
        Log.print("Different categories:", numLabels, "Examples:", numExamples);

        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);

        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);

        RecordReaderDataSetIterator dataSetIterator = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);

        Log.print("Initialize record Reader");
        recordReader.initialize(trainingSplit);

        Log.print("Initialize network");
        ModelInfo info = new ModelInfo();
        info.channels = 1;
        info.width = width;
        info.height = height;
        info.numLabels = numLabels;
        info.seed = 1337;
        MultiLayerNetwork lenetModel = this.lenetModel(info);
        lenetModel.init();

        Log.print("Fit scaler");

        scaler.fit(dataSetIterator);

        dataSetIterator.setPreProcessor(scaler);

        EarlyStoppingConfiguration earlyStoppingConfig = new EarlyStoppingConfiguration.Builder<>()
                .epochTerminationConditions(new MaxEpochsTerminationCondition(5))
                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(10, TimeUnit.MINUTES))
                .saveLastModel(true)
                .build();

        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(earlyStoppingConfig, lenetModel, dataSetIterator);
        Log.print("Start fitting");
        EarlyStoppingResult<MultiLayerNetwork> fit = trainer.fit();

        //we only have 1 model now, so don't use validation set
        RecordReaderDataSetIterator testSet = this.getDataset(localPath + "test");

        MultiLayerNetwork bestModel = fit.getBestModel();
        Log.print("Evaluate");
        Log.print("Best model null?", bestModel == null);//if 0 epoch passed, this will be null

        Evaluation evaluate;
        if (bestModel == null) {
            evaluate = lenetModel.evaluate(testSet);
        } else {
            evaluate = bestModel.evaluate(testSet);
        }
        Log.print("Current accuracy", evaluate.accuracy());
    }

    public static void main(String... args) throws Exception {
        if(true){
            return;
        }
        Log.main().keepBufferForFile = false;
        Log.main().threadName = false;
//        Log.main().surroundString = false;
//        Log.main().timeStamp = false;
        new DL4JCNN().run();
        Log.print("End");

        System.exit(0);

    }

    public static class ModelInfo {

        public int seed;
        public int channels;
        public int width;
        public int height;
        public int numLabels;
    }

    public MultiLayerNetwork lenetModel(ModelInfo info) {
        /**
         * Revisde Lenet Model approach developed by ramgo2 achieves slightly
         * above random Reference:
         * https://gist.github.com/ramgo2/833f12e92359a2da9e5c2fb6333351c5
         *
         */
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(info.seed)
                .l2(0.005)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.0001, 0.9))
                .list()
                .layer(0, convInit("cnn1", info.channels, 50, new int[]{5, 5}, new int[]{1, 1}, new int[]{0, 0}, 0))
                .layer(1, maxPool("maxpool1", new int[]{2, 2}))
                .layer(2, conv5x5("cnn2", 100, new int[]{5, 5}, new int[]{1, 1}, 0))
                .layer(3, maxPool("maxool2", new int[]{2, 2}))
                .layer(4, new DenseLayer.Builder().nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(info.numLabels)
                        .activation(Activation.SOFTMAX)
                        .build())
                .backprop(true).pretrain(false)
                .setInputType(InputType.convolutional(info.height, info.width, info.channels))
                .build();

        return new MultiLayerNetwork(conf);

    }

    private ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv3x3(String name, int out, double bias) {
        return new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1}).name(name).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(new int[]{5, 5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
    }

    private SubsamplingLayer maxPool(String name, int[] kernel) {
        return new SubsamplingLayer.Builder(kernel, new int[]{2, 2}).name(name).build();
    }

    private DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
        return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
    }
}
