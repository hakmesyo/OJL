/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.processing.path_following;

import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author cezerilab
 */
public class PathFollowing extends PApplet {

    public static void main(String[] args) {
        PathFollowing sketch = new PathFollowing();
        PApplet.runSketch(new String[]{"--location=0,30", "Simulation"}, sketch);
    }
// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com

// Path Following
// Via Reynolds: // http://www.red3d.com/cwr/steer/PathFollow.html
// Using this variable to decide whether to draw all the stuff
    public boolean debug = true;

// A path object (series of connected points)
    Path path;

// Two vehicles
    Vehicle car1;
    Vehicle car2;

    @Override
    public void settings() {
        size(1000, 1000, P3D);
        smooth(8);
    }

    @Override
    public void setup() {
        // Call a function to generate new Path object

        // Each vehicle has different maxspeed and maxforce for demo purposes
        PVector initialDirection = new PVector(0, -1);
        car1 = new Vehicle(this, new PVector(width - 100, height-100), 3, 0.05f, initialDirection);
        car2 = new Vehicle(this, new PVector(width - 900, height-50), 1f, 0.01f, new PVector(1, -1));
        //car2 = new Vehicle(this, new PVector(0, height / 2), 3, 0.1f);
        newPath();
    }

    @Override
    public void draw() {
        background(255);
        path.display();
        car1.follow(path);
        car2.follow(path);
        car1.run();
        car2.run();
        fill(0);
        text("Hit space bar to toggle debugging lines.\nClick the mouse to generate a new path.", 10, height - 30);
    }

    public void newPath() {
        path = new Path(this);
        leftTurnPath();
        //rightTurnPath();
    }

    private void leftTurnPath() {        
        path.addPoint(width - 100, height - 400);
        path.addPoint(width - 100, height - 400);
        path = addLeftViraj(path, width - 300, height - 600, 20);
        path.addPoint(width - 600, height - 600);
        path = addLeftViraj(path, width - 800, height - 400, 20);
        path.addPoint(width - 800, height - 300);
        path = addLeftViraj(path, width - 600, height - 100, 20);
        path.addPoint(width - 300, height - 100);
        path = addLeftViraj(path, width - 100, height - 300, 20);
        path.addPoint(width - 100, height - 400);
    }

    private void rightTurnPath() {
        path.addPoint(width - 800, height - 600);
        path.addPoint(width - 800, height - 600);
        path = addRightViraj(path, width - 600, height - 800, 20);
        path = addLeftViraj(path, width - 550, height - 850, 20);
        path = addRightViraj(path, width - 500, height - 900, 20);
        path.addPoint(width - 400, height - 900);
        path = addRightViraj(path, width - 350, height - 850, 20);
        path = addLeftViraj(path, width - 300, height - 800, 20);
        path = addRightViraj(path, width - 100, height - 600, 20);
        path.addPoint(width - 100, height - 400);
        path = addRightViraj(path, width - 300, height - 200, 20);
        path.addPoint(width - 600, height - 200);
        path = addRightViraj(path, width - 800, height - 400, 20);
        path.addPoint(width - 800, height - 600);
    }

    Path addLeftViraj(Path path, float p2x, float p2y, int numNodes) {
        float p1x=0;
        float p1y=0;
        if (path.points.size()>1) {
            p1x=path.getEnd().x;
            p1y=path.getEnd().y;
        }
        float radiusX = abs(p2x - p1x);
        float radiusY = abs(p2y - p1y);

        float centerX = 0;
        float centerY = 0;

        float startAngle = 0;
        float endAngle = 0;
        if (p1x > p2x && p1y > p2y) {
            centerX = p2x;
            centerY = p1y;
            startAngle = 0;
            endAngle = HALF_PI;
        } else if (p1x > p2x && p1y < p2y) {
            centerX = p1x;
            centerY = p2y;
            startAngle = HALF_PI;
            endAngle = PI;
        } else if (p1x < p2x && p1y < p2y) {
            centerX = p2x;
            centerY = p1y;
            startAngle = PI;
            endAngle = PI + HALF_PI;
        } else if (p1x < p2x && p1y > p2y) {
            centerX = p1x;
            centerY = p2y;
            startAngle = PI + HALF_PI;
            endAngle = TWO_PI;
        }

        float angleStep = (endAngle - startAngle) / numNodes;

        for (int i = 0; i <= numNodes; i++) {
            float angle = startAngle + i * angleStep;
            float x = centerX + radiusX * cos(angle);
            float y = centerY - radiusY * sin(angle);
            path.addPoint(x, y);
        }

        return path;
    }

    Path addRightViraj(Path path, float p2x, float p2y, int numNodes) {
        float p1x=0;
        float p1y=0;
        if (path.points.size()>1) {
            p1x=path.getEnd().x;
            p1y=path.getEnd().y;
        }
        float radiusX = abs(p2x - p1x);
        float radiusY = abs(p2y - p1y);
        float centerX = 0;
        float centerY = 0;
        float startAngle = 0;
        float endAngle = 0;

        if (p1x > p2x && p1y > p2y) {
            centerX = p1x;
            centerY = p2y;
            startAngle = PI + HALF_PI;
            endAngle = PI;
        } else if (p1x > p2x && p1y < p2y) {
            centerX = p2x;
            centerY = p1y;
            startAngle = TWO_PI;
            endAngle = PI + HALF_PI;
        } else if (p1x < p2x && p1y < p2y) {
            centerX = p1x;
            centerY = p2y;
            startAngle = HALF_PI;
            endAngle = 0;
        } else if (p1x < p2x && p1y > p2y) {
            centerX = p2x;
            centerY = p1y;
            startAngle = PI;
            endAngle = HALF_PI;
        }

        float angleStep = (endAngle - startAngle) / numNodes;
        for (int i = 0; i <= numNodes; i++) {
            float angle = startAngle + i * angleStep;
            float x = centerX + radiusX * cos(angle);
            float y = centerY - radiusY * sin(angle);
            path.addPoint(x, y);
        }
        return path;
    }

    @Override
    public void keyPressed() {
        if (key == ' ') {
            debug = !debug;
        }
    }

    @Override
    public void mousePressed() {
        newPath();
    }

}
