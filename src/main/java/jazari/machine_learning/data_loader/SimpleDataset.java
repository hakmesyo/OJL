/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.data_loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author cezerilab
 */
/**
 * Basic Dataset Implementation
 */
public class SimpleDataset implements Dataset {

    protected List<Sample> samples;
    protected int featureSize;
    protected int labelSize;
    protected Random random;

    public SimpleDataset(int featureSize, int labelSize) {
        this.samples = new ArrayList<>();
        this.featureSize = featureSize;
        this.labelSize = labelSize;
        this.random = new Random();
    }

    public void addSample(Sample sample) {
        samples.add(sample);
    }

    @Override
    public int size() {
        return samples.size();
    }

    @Override
    public Sample getSample(int index) {
        return samples.get(index);
    }

    @Override
    public void shuffle() {
        Collections.shuffle(samples, random);
    }

    @Override
    public int getFeatureSize() {
        return featureSize;
    }

    @Override
    public int getLabelSize() {
        return labelSize;
    }
}
