/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.processing.path_following;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/**
 *
 * @author cezerilab
 */
// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com
// Path Following
// Vehicle class
class Vehicle {

    PathFollowing sim = null;

    // All the usual stuff
    PVector position;
    PVector velocity;
    PVector acceleration;
    float r;
    float maxforce;    // Maximum steering force
    float maxspeed;    // Maximum speed

    // Constructor initialize all values
    public Vehicle(PApplet sim, PVector l, float ms, float mf, PVector initialDirection) {
        this.sim = (PathFollowing) sim;
        position = l.get();
        r = 4.0f;
        maxspeed = ms;
        maxforce = mf;
        acceleration = new PVector(0, 0);

        //velocity = new PVector(maxspeed, 0);
        velocity = initialDirection.copy();
        velocity.setMag(maxspeed);
    }

    // Main "run" function
    public void run() {
        update();
        display();
    }

    // This function implements Craig Reynolds' path following algorithm
    // http://www.red3d.com/cwr/steer/PathFollow.html
    public void follow(Path p) {
        // Predict position 50 (arbitrary choice) frames ahead
        PVector predict = velocity.get();
        predict.normalize();
        predict.mult(velocity.mag()*20);
        PVector predictpos = PVector.add(position, predict);

        // Now we must find the normal to the path from the predicted position
        // We look at the normal for each line segment and pick out the closest one
        PVector normal = null;
        PVector target = null;
        float worldRecord = 1000000;  // Start with a very high record distance that can easily be beaten

        // Loop through all points of the path
        for (int i = 0; i < p.points.size() - 1; i++) {
            // Look at a line segment
            PVector a = p.points.get(i);
            PVector b = p.points.get(i + 1);

            // Get the normal point to that line
            PVector normalPoint = getNormalPoint(predictpos, a, b);

            // Check if the normal point is on the line segment
            PVector dir = PVector.sub(b, a);
            float segmentLength = dir.mag();
            float projection = PVector.sub(normalPoint, a).dot(dir.normalize());

            if (projection < 0 || projection > segmentLength) {
                // If it's not within the line segment, consider the normal to just be the end of the line segment (point b)
                normalPoint = b.get();
            }

            // How far away are we from the path?
            float distance = PVector.dist(predictpos, normalPoint);
            // Did we beat the record and find the closest line segment?
            if (distance < worldRecord) {
                worldRecord = distance;
                // If so the target we want to steer towards is the normal
                normal = normalPoint;

                // Look at the direction of the line segment so we can seek a little bit ahead of the normal
                dir.normalize();
                // This is an oversimplification
                // Should be based on distance to path & velocity
                dir.mult(10);
                target = PVector.add(normalPoint, dir);
            }
        }

        // Only if the distance is greater than the path's radius do we bother to steer
        if (worldRecord > p.radius) {
            seek(target);
        }

        // Debugging
        if (sim.debug) {
            // Draw predicted future position
            sim.stroke(0);
            sim.fill(0);
            sim.line(position.x, position.y, predictpos.x, predictpos.y);
            sim.ellipse(predictpos.x, predictpos.y, 4, 4);

            // Draw normal position
            sim.stroke(0);
            sim.fill(0);
            sim.ellipse(normal.x, normal.y, 4, 4);
            // Draw actual target (red if steering towards it)
            sim.line(predictpos.x, predictpos.y, normal.x, normal.y);
            if (worldRecord > p.radius) {
                sim.fill(255, 0, 0);
            }
            sim.noStroke();
            sim.ellipse(target.x, target.y, 8, 8);
        }
    }

    // A function to get the normal point from a point (p) to a line segment (a-b)
    // This function could be optimized to make fewer new Vector objects
    PVector getNormalPoint(PVector p, PVector a, PVector b) {
        // Vector from a to p
        PVector ap = PVector.sub(p, a);
        // Vector from a to b
        PVector ab = PVector.sub(b, a);
        ab.normalize(); // Normalize the line
        // Project vector "diff" onto line by using the dot product
        ab.mult(ap.dot(ab));
        PVector normalPoint = PVector.add(a, ab);
        return normalPoint;
    }

    // Method to update position
    void update() {
        // Update velocity
        velocity.add(acceleration);
        // Limit speed
        velocity.limit(maxspeed);
        position.add(velocity);
        // Reset accelertion to 0 each cycle
        acceleration.mult(0);
    }

    void applyForce(PVector force) {
        // We could add mass here if we want A = F / M
        acceleration.add(force);
    }

    // A method that calculates and applies a steering force towards a target
    // STEER = DESIRED MINUS VELOCITY
    void seek(PVector target) {
        PVector desired = PVector.sub(target, position);  // A vector pointing from the position to the target

        // If the magnitude of desired equals 0, skip out of here
        // (We could optimize this to check if x and y are 0 to avoid mag() square root
        if (desired.mag() == 0) {
            return;
        }

        // Normalize desired and scale to maximum speed
        desired.normalize();
        desired.mult(maxspeed);
        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(desired, velocity);
        steer.limit(maxforce);  // Limit to maximum steering force

        applyForce(steer);
    }

    void display() {
        // Draw a triangle rotated in the direction of velocity
        //float theta = velocity.heading2D() + sim.radians(90);
        float theta = velocity.heading() + PConstants.PI / 2;
        sim.fill(175);
        sim.stroke(0);
        sim.pushMatrix();
        sim.translate(position.x, position.y);
        sim.rotate(theta);
        sim.beginShape(PConstants.TRIANGLES);
        sim.vertex(0, -r * 2);
        sim.vertex(-r, r * 2);
        sim.vertex(r, r * 2);
        sim.endShape();
        sim.popMatrix();
    }

    // Wraparound
    void borders(Path p) {
        if (position.x > p.getEnd().x + r) {
            position.x = p.getStart().x - r;
            position.y = p.getStart().y + (position.y - p.getEnd().y);
        }
    }
}
