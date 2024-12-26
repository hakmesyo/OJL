/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package jazari.machine_learning.data_loader;

/**
 *
 * @author cezerilab
 */
/**
 * Dataset interface for different data sources
 */
public interface Dataset {

    int size();

    Sample getSample(int index);

    void shuffle();

    int getFeatureSize();

    int getLabelSize();
}

