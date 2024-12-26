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
 * Sample class to hold individual examples
 */
public class Sample {

    private double[] features;
    private double[] labels;

    public Sample(double[] features, double[] labels) {
        this.features = features;
        this.labels = labels;
    }

    public double[] getFeatures() {
        return features;
    }

    public double[] getLabels() {
        return labels;
    }
}
