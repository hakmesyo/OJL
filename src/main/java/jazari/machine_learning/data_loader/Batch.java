/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.data_loader;

/**
 *
 * @author cezerilab
 */
/**
 * Batch class to hold mini-batch data for training
 */
public class Batch {

    public final double[][] features;
    public final double[][] labels;

    public Batch(double[][] features, double[][] labels) {
        this.features = features;
        this.labels = labels;
    }

    public int getSize() {
        return features.length;
    }
}
