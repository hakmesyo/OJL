/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.processing.path_following;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author cezerilab
 */
// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com
// Path Following
class Path {

    // A Path is an arraylist of points (PVector objects)
    ArrayList<PVector> points;
    // A path has a radius, i.e how far is it ok for the boid to wander off
    float radius;
    PApplet sim=null;

    Path(PApplet sim) {
        this.sim=sim;
        // Arbitrary radius of 20
        radius = 5;
        points = new ArrayList<>();
    }

    // Add a point to the path
    void addPoint(float x, float y) {
        PVector point = new PVector(x, y);
        points.add(point);
    }

    PVector getStart() {
        return points.get(0);
    }

    PVector getEnd() {
        return points.get(points.size() - 1);
    }

    // Draw the path
    void display() {
        // Draw thick line for radius
        sim.stroke(175);
        sim.strokeWeight(radius * 5);
        sim.noFill();
        sim.beginShape();
        for (PVector v : points) {
            sim.vertex(v.x, v.y);
        }
        sim.endShape();
        // Draw thin line for center of path
        sim.stroke(0);
        sim.strokeWeight(1);
        sim.noFill();
        sim.beginShape();
        for (PVector v : points) {
            sim.vertex(v.x, v.y);
        }
        sim.endShape();
    }
}
