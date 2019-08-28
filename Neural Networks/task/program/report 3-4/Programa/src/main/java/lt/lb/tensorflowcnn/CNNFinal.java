/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.tensorflowcnn;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lt.lb.commons.Log;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laim0nas100
 */
public class CNNFinal {

    private static final Logger log = LoggerFactory.getLogger(CNNFinal.class);
//    private static final String basePath = System.getProperty("user.dir") + "/img";
    private static final String basePath = "D:\\NN data";

    public static void main(String[] args) throws Exception {
        Log.main().stackTrace = false;
        int height = 60;
        int width = 30;
        int channels = 1; // single channel for grayscale images
        int outputNum = 70; // classification
        int batchSize = 256;
        int nEpochs = 50;
        int depth = 1;

//        org.nd4j.jita.conf.CudaEnvironment.getInstance().getConfiguration()
//                .allowMultiGPU(true)
//                .allowCrossDeviceAccess(true);
        int seed = 1234;
        Random randNumGen = new Random(seed);

        log.info("Data load and vectorization...");

        // vectorization of train data
//        File trainData = new File(basePath + "/mnist_png/training");
        File trainData = new File(basePath + "/no_of_classes_"+outputNum+"/trainCNV");
        FileSplit trainSplit = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator(); // parent path as the image label
        ImageRecordReader trainRR = new ImageRecordReader(height, width, channels, labelMaker);
        trainRR.initialize(trainSplit);
        DataSetIterator trainIter = new RecordReaderDataSetIterator(trainRR, batchSize, 1, outputNum);

        // pixel values from 0-255 to 0-1 (min-max scaling)
        DataNormalization scaler = new ImagePreProcessingScaler(0, depth);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);

        // vectorization of test data
//        File testData = new File(basePath + "/mnist_png/testing");
        File testData = new File(basePath + "/no_of_classes_"+outputNum+"/testCNV");
        FileSplit testSplit = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        ImageRecordReader testRR = new ImageRecordReader(height, width, channels, labelMaker);
        testRR.initialize(testSplit);
        DataSetIterator testIter = new RecordReaderDataSetIterator(testRR, batchSize, 1, outputNum);
        testIter.setPreProcessor(scaler); // same normalization for better results

        Log.print("Network configuration and training...");


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0005)
                .updater(new Nesterovs(0.01, 0.9))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(channels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1) // nIn need not specified in later layers
                        .nOut(50)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutional(height, width, 1)) // InputType.convolutional for normal image
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(10) {
            @Override
            public void iterationDone(Model model, int iteration, int epoch) {
                Log.print(iteration, epoch);
                super.iterationDone(model, iteration, epoch); //To change body of generated methods, choose Tools | Templates.
            }
        });
        Log.print("Total num of params:", net.numParams());

        for (int i = 0; i < 10; i++) {
//            Log.print(trainRR.next());
        }

//        if (true) return;
        // evaluation while training (the score should go down)
        for (int i = 0; i < nEpochs; i++) {
            trainIter.reset();
            testIter.reset();
            net.fit(trainIter);
            Log.print("Completed epoch", i);
            Evaluation eval = net.evaluate(testIter);
            Log.print(eval.stats());

        }

        ModelSerializer.writeModel(net, new File(basePath + "/"+Log.getZonedDateTime("HH-mm-ss ")+"model.zip"), true);
    }
}
