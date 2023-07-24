package jazari.deep_learning.dl_scratch;

import java.util.List;

import static java.util.Collections.shuffle;
import jazari.deep_learning.dl_scratch.data.DataReader;
import jazari.deep_learning.dl_scratch.data.Image;
import jazari.deep_learning.dl_scratch.network.NetworkBuilder;
import jazari.deep_learning.dl_scratch.network.NeuralNetwork;
import jazari.factory.FactoryUtils;

public class Main {

    public static void main(String[] args) {

        long SEED = 123;

        System.out.println("Starting data loading...");

        List<Image> imagesTest = new DataReader().readData("D:\\ai\\djl\\mnist\\csv\\mnist_test.csv");
        List<Image> imagesTrain = new DataReader().readData("D:\\ai\\djl\\mnist\\csv\\mnist_train.csv");

        System.out.println("Images Train size: " + imagesTrain.size());
        System.out.println("Images Test size: " + imagesTest.size());

        NetworkBuilder builder = new NetworkBuilder(28, 28, 256 * 100);
        builder.addConvolutionLayer(8, 5, 1, 0.1, SEED);
        builder.addMaxPoolLayer(3, 2);
        builder.addFullyConnectedLayer(10, 0.1, SEED);

        NeuralNetwork net = builder.build();

        //long t1=FactoryUtils.tic();
        
        //t1=FactoryUtils.toc(t1);
        float rate = net.test(imagesTest);
        System.out.println("Pre training success rate: " + rate);
        float concurrent_rate = net.test_concurrent(imagesTest);
        System.out.println("Pre training success concurrent_rate: " + concurrent_rate);

        int epochs = 1;
        int batch_size = 5096;
        for (int i = 0; i < epochs; i++) {
            shuffle(imagesTrain);
            net.train(imagesTrain);
            rate = net.test(imagesTest);
            System.out.println("Success rate after round " + i + ": " + rate);
        }

//        List<Image> lst_1=imagesTrain.subList(0, 5096);
//        List<Image> lst_2=imagesTrain.subList(5096, 2*5096);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < epochs; i++) {
//                    //shuffle(imagesTrain);
//                    net.train(lst_1, 0, 5096);
//                    float rate = net.test(imagesTest);
//                    System.out.println("Success rate after round " + i + ": " + rate);
//                }
//            }
//        }).start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < epochs; i++) {
//                    net.train(lst_2, 5096,2*5096);
//                    float rate = net.test(imagesTest);
//                    System.out.println("Success rate after round " + i + ": " + rate);
////                    shuffle(imagesTrain);
////                    net.train(imagesTrain, batch_size);
////                    float rate = net.test(imagesTest);
////                    System.out.println("Success rate after round " + i + ": " + rate);
//                }
//            }
//        }).start();
    }
}
