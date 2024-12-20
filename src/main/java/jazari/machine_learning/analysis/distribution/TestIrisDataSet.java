/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.analysis.distribution;

import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestIrisDataSet {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                //.make_blobs(100, 4, 3)
                .readCSV("dataset/iris.csv", "last")
                //.readCSV("dataset/mnist_train_reduced.csv", "first")
                //.println()
                //.scatter()
                //.tsne()
                ;
        cm.clone().tsne();
//        cm.clone().scatter(0,1);
//        cm.clone().scatter(0,2);
//        cm.clone().scatter(1,2);
    }
}
