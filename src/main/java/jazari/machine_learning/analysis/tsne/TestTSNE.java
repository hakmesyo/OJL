/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.analysis.tsne;

import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestTSNE {

    public static void main(String[] args) {
//        // Veriyi yükle
//        double[][] data = TSNE.loadData("dataset/iris.csv", new int[]{0, 1, 2, 3});
//        String[] labels = TSNE.loadLabels("dataset/iris.csv", 4);
//
//        // TSNE görselleştirmesi oluştur ve göster
//        //TSNE tsne = TSNE.build(data, labels);
//        TSNE tsne = TSNE.build(data, labels, 2, 30, 0.5, 1000);
//        tsne.show();

        CMatrix cm = CMatrix.getInstance()
                .readCSV("dataset/iris.csv","last")
                //.readCSV("dataset/mnist_train_reduced.csv","first")
                //.readCSV("dataset/Kaggle_Digits_1000.csv","first")
                .tsne()
                //.plot()
                //.tsne(30, 0.5, 1000)
                //.make_blobs(1000, 2, 3)
                //.scatter()
                ;
    }

}
