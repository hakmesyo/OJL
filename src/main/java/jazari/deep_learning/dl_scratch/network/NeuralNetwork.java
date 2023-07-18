package jazari.deep_learning.dl_scratch.network;

import java.util.ArrayList;
import java.util.List;
import static jazari.deep_learning.dl_scratch.data.MatrixUtility.multiply;
import static jazari.deep_learning.dl_scratch.data.MatrixUtility.add;
import jazari.deep_learning.dl_scratch.layers.Layer;
import jazari.deep_learning.dl_scratch.data.Image;
import net.ericaro.neoitertools.generators.combinatorics.SubListNumber;

public class NeuralNetwork {

    List<Layer> _layers;
    double scaleFactor;

    public NeuralNetwork(List<Layer> _layers, double scaleFactor) {
        this._layers = _layers;
        this.scaleFactor = scaleFactor;
        linkLayers();
    }

    private void linkLayers() {

        if (_layers.size() <= 1) {
            return;
        }

        for (int i = 0; i < _layers.size(); i++) {
            if (i == 0) {
                _layers.get(i).set_nextLayer(_layers.get(i + 1));
            } else if (i == _layers.size() - 1) {
                _layers.get(i).set_previousLayer(_layers.get(i - 1));
            } else {
                _layers.get(i).set_previousLayer(_layers.get(i - 1));
                _layers.get(i).set_nextLayer(_layers.get(i + 1));
            }
        }
    }

    public double[] getErrors(double[] networkOutput, int correctAnswer) {
        int numClasses = networkOutput.length;

        double[] expected = new double[numClasses];

        expected[correctAnswer] = 1;

        return add(networkOutput, multiply(expected, -1));
    }

    private int getMaxIndex(double[] in) {

        double max = 0;
        int index = 0;

        for (int i = 0; i < in.length; i++) {
            if (in[i] >= max) {
                max = in[i];
                index = i;
            }

        }

        return index;
    }

    public int guess(Image image) {
        List<double[][]> inList = new ArrayList<>();
        inList.add(multiply(image.getData(), (1.0 / scaleFactor)));

        double[] out = _layers.get(0).getOutput(inList);
        int guess = getMaxIndex(out);

        return guess;
    }

    public float test(List<Image> images) {
        int correct = 0;

        for (Image img : images) {
            int guess = guess(img);

            if (guess == img.getLabel()) {
                correct++;
            }
        }

        return ((float) correct / images.size());
    }
    
    int nn=0;

    public void train(List<Image> images, int from, int to) {

        //int n_threads = images.size() / batch_size-1;
//        int n_threads = 1;
//        nn=0;
//        for (int i = 0; i < n_threads; i++) {            
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    List<Image> lst = images.subList(nn * batch_size, (nn + 1) * batch_size);
//                    nn++;
//                    for (int k = 0; k < lst.size(); k++) {
//                        Image img = lst.get(k);
//                        List<double[][]> inList = new ArrayList<>();
//                        inList.add(multiply(img.getData(), (1.0 / scaleFactor)));
//
//                        double[] out = _layers.get(0).getOutput(inList);
//                        double[] dldO = getErrors(out, img.getLabel());
//
//                        _layers.get((_layers.size() - 1)).backPropagation(dldO);
//                        System.out.println(k + ".imge");
//                    }
//                }
//            }).start();
//        }

        //List<Image> lst = images.subList(nn * batch_size, (nn + 1) * batch_size);
        int k=0;
        for (Image img : images) {
            List<double[][]> inList = new ArrayList<>();
            inList.add(multiply(img.getData(), (1.0 / scaleFactor)));

            double[] out = _layers.get(0).getOutput(inList);
            double[] dldO = getErrors(out, img.getLabel());

            _layers.get((_layers.size() - 1)).backPropagation(dldO);
            System.out.println((from++) + ".imge");
        }

    }

}
