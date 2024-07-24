/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.processing;

import processing.core.PApplet;
import static processing.core.PConstants.P3D;
import processing.core.PVector;

/**
 *
 * @author cezerilab
 */
public class Template extends PApplet{
    
    
    
    
    PVector v1;

    public static void main(String[] args) {
        Template sketch = new Template();
        PApplet.runSketch(new String[]{"--location=0,30", "Simulation"}, sketch);
    }

    @Override
    public void settings() {
        size(800, 600, P3D);
        smooth(8);
    }

    @Override
    public void setup() {
        v1 = new PVector(20, 20);
    }

    @Override
    public void draw() {
        //println(degrees(v1.heading()));
    }
    
}
