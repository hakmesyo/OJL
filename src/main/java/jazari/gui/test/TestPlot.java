/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui.test;

import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.types.TFigureAttribute;
import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author cezerilab
 */
public class TestPlot {

    public static void main(String[] args) {
        testSimplePlot();
//        testPerlin();
//        testKalman();
//        testTrigonometry();
//        testAnimatedSinPlot();
//        testAnimatedRandomPlot();
//        testAnimatedPerlinPlot();
    }

    private static void testKalman() {
        // discrete time interval
        double dt = 0.1d;
        // position measurement noise (meter)
        double measurementNoise = 10d;
        // acceleration noise (meter/sec^2)
        double accelNoise = 0.2d;

        // A = [ 1 dt ]
        //     [ 0  1 ]
        RealMatrix A = new Array2DRowRealMatrix(new double[][]{{1, dt}, {0, 1}});
        // B = [ dt^2/2 ]
        //     [ dt     ]
        RealMatrix B = new Array2DRowRealMatrix(new double[][]{{Math.pow(dt, 2d) / 2d}, {dt}});
        // H = [ 1 0 ]
        RealMatrix H = new Array2DRowRealMatrix(new double[][]{{1d, 0d}});
        // x = [ 0 0 ]
        RealVector x = new ArrayRealVector(new double[]{0, 0});

        RealMatrix tmp = new Array2DRowRealMatrix(new double[][]{
            {Math.pow(dt, 4d) / 4d, Math.pow(dt, 3d) / 2d},
            {Math.pow(dt, 3d) / 2d, Math.pow(dt, 2d)}});
        // Q = [ dt^4/4 dt^3/2 ]
        //     [ dt^3/2 dt^2   ]
        RealMatrix Q = tmp.scalarMultiply(Math.pow(accelNoise, 2));
        // P0 = [ 1 1 ]
        //      [ 1 1 ]
        RealMatrix P0 = new Array2DRowRealMatrix(new double[][]{{1, 1}, {1, 1}});
        // R = [ measurementNoise^2 ]
        RealMatrix R = new Array2DRowRealMatrix(new double[]{Math.pow(measurementNoise, 2)});

        // constant control input, increase velocity by 0.1 m/s per cycle
        RealVector u = new ArrayRealVector(new double[]{0.1d});

        ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        KalmanFilter filter = new KalmanFilter(pm, mm);

        RandomGenerator rand = new JDKRandomGenerator();

        RealVector tmpPNoise = new ArrayRealVector(new double[]{Math.pow(dt, 2d) / 2d, dt});
        RealVector mNoise = new ArrayRealVector(1);

        // iterate 60 steps
        double[][] data = new double[60][2];
        for (int i = 0; i < 60; i++) {
            filter.predict(u);

            // simulate the process
            RealVector pNoise = tmpPNoise.mapMultiply(accelNoise * rand.nextGaussian());

            // x = A * x + B * u + pNoise
            x = A.operate(x).add(B.operate(u)).add(pNoise);

            // simulate the measurement
            mNoise.setEntry(0, measurementNoise * rand.nextGaussian());

            // z = H * x + m_noise
            RealVector z = H.operate(x).add(mNoise);

            filter.correct(z);

            double position = filter.getStateEstimation()[0];
            double velocity = filter.getStateEstimation()[1];
            data[i] = new double[]{position, velocity};
            System.out.println("position = " + position + " velocity = " + velocity);
        }
        CMatrix cm = CMatrix.getInstance()
                .setArray(data)
                .plot(new TFigureAttribute("Kalman Example", "Kalman behaviour along time", "Iterations,Value", "position,velocity"));

    }

    private static void testPerlin() {
        int min = -200;
        int max = 200;
        CMatrix cm1 = CMatrix.getInstance()
                .range(min, max)
                .perlinNoise(0.01f);
        CMatrix cm2 = CMatrix.getInstance()
                .range(min, max)
                .perlinNoise(0.022f);
        CMatrix cm = cm1.cat(1, cm2);
        cm.plot(CMatrix.getInstance().range(min, max).toFloatArray1D());
    }

    private static void testTrigonometry() {
        CMatrix cm = CMatrix.getInstance()
                .range(360)
                .toRadians();
        CMatrix cm1 = cm.clone().sin();
        CMatrix cm2 = cm.clone().timesScalar(3).cos();
        CMatrix cm3 = cm1.cat(1, cm2)
                //.println()
                .plot();
    }

    private static void testAnimatedSinPlot() {
        //plotAnimated kendi içinde loop mekanizması barındırıyor, functional interface ve lambda kullanımı var
        CMatrix cm = CMatrix.getInstance()
                .range(100)
                .println()
                .range2D(0,90,2)
                .transpose()
                .println()
                .toRadians()
                .timesScalar(5)
//                .sin()
//                .cat(1, CMatrix.getInstance()
//                .range(90)
//                .toRadians()
//                .timesScalar(5)
//                .cos()
//                )
                .plotAnimated(-1, 100,
                index -> {
                    return new float[]{(float) Math.sin(Math.PI/180*5*index),3*(float) Math.cos(Math.PI/180*5*index)};
                }
        );
//        //klasik for döngüsü, pop ve ardından push ile
//        CMatrix cm = CMatrix.getInstance()
//                .range(90)
//                .toRadians()
//                //.timesScalar(3)
//                .sin();
//        long t1 = System.currentTimeMillis();
//        for (int i = 0; i < 760; i++) {
//            cm.tic().pop().push((float) Math.sin(Math.PI / 180 * i)).plotRefresh().toc();
//            try {
//                Thread.sleep(30);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(TestPlot.class.getName()).log(Level.SEVERE, null, ex);
//            }
////            System.out.println("elapsed time="+(System.currentTimeMillis()-t1));
////            t1=System.currentTimeMillis();
//        }

    }

    private static void testAnimatedRandomPlot() {
        //plotAnimated kendi içinde loop mekanizması barındırıyor, functional interface ve lambda kullanımı var
        CMatrix cm = CMatrix.getInstance()
                .rand(100).plotAnimated(-1, 100,
                index -> {
                    return new float[]{(float) Math.random()};
                }
        );
//        //klasik for döngüsü, pop ve ardından push ile
//        CMatrix cm = CMatrix.getInstance()
//                .rand(90);
//        long t1 = System.currentTimeMillis();
//        for (int i = 90; i < 360; i++) {
//            cm.tic().pop().push((float) Math.random()).plotRefresh().toc();
//            try {
//                Thread.sleep(30);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(TestPlot.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    // Runnable türünde bir parametre alan bir metot
    public static void runFunction(Runnable function) {
        // Runnable arayüzünün run metodu çağrılır
        function.run();
    }

    private static void testAnimatedPerlinPlot() {
        CMatrix cm = CMatrix.getInstance()
                .range(100);
        CMatrix cm1 = cm.clone().perlinNoise(0.05f);
        CMatrix cm2 = cm.clone().perlinNoise(0.03f);
        CMatrix cm3 = cm.clone().perlinNoise(0.02f);
        CMatrix cm4 = cm.clone().perlinNoise(0.01f);
        CMatrix cmx = cm1.cat(1, cm2).cat(1, cm3).cat(1, cm4);

        //plot animated kullanılarak (kodun ne kadar temiz olduğuna dikkat edin)
        cmx = cmx
                .tic()
                .plotAnimated(-1, 100,
                        index -> {
                            float f1 = FactoryUtils.perlinNoise(index, 0.07f);
                            float f2 = FactoryUtils.perlinNoise(index, 0.05f);
                            float f3 = FactoryUtils.perlinNoise(index, 0.03f);
                            float f4 = FactoryUtils.perlinNoise(index, 0.01f);
                            return new float[]{f1, f2, f3, f4};
                        })
                .toc();

//        klasik for döngüsü, append ve plotRefresh kullanılarak
//        long t1 = System.currentTimeMillis();
//        for (int i = 0; i < 3600; i++) {
//            //cm=cm.tic().pop().toc("pop cost:").push(FactoryUtils.perlinNoise(i, 0.05f)).toc("push cost:").plotRefresh().toc("plot cost");
//
//            //append ve plotRefresh kullanılarak
//            System.out.println("");
//            cmx = cmx
//                    .tic()
//                    .append(
//                            FactoryUtils.perlinNoise(i, 0.05f),
//                            FactoryUtils.perlinNoise(i, 0.03f),
//                            FactoryUtils.perlinNoise(i, 0.01f))
//                    .toc()
//                    .plotRefresh(10)
//                    .toc();
//
////            try {
////                Thread.sleep(10);
////            } catch (InterruptedException ex) {
////                Logger.getLogger(TestPlot.class.getName()).log(Level.SEVERE, null, ex);
////            }
////            System.out.println("elapsed time="+(System.currentTimeMillis()-t1));
////            t1=System.currentTimeMillis();
//        }
    }

    private static void testSimplePlot() {
        float[] f={20.12f,50.13f,35f,62.67f,49.17f,21f,35f,41f,45f,52f};
        f=CMatrix.getInstance().linspace(-30, 150, 200).toFloatArray1D();
        CMatrix cm = CMatrix.getInstance(f)
                //.range(-21, 21)
                .perlinNoise(0.1f)
                //.rand(330,1)
                //.println()
                //.plot(CMatrix.getInstance().range(-21,21).toFloatArray1D())
                .plot(f)
                ;
    }
}
