/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui.test;

import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.types.TFigureAttribute;
import jazari.utils.PidController;
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
//        testPidController();
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
        data=FactoryMatrix.transpose(data);
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
                .transpose()
                .plot();
    }

    private static void testAnimatedSinPlot() {
        //plotAnimated kendi içinde loop mekanizması barındırıyor, functional interface ve lambda kullanımı var
        CMatrix cm = CMatrix.getInstance()
                .range(100)
                //.println()
                .range2D(0,90,2)
                .transpose()
                //.println()
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
        float[] f={0,20.12f,50.13f,35f,62.67f,49.17f,21f,35f,41f,45f,52f};
        //f=CMatrix.getInstance().linspace(-30, 150, 180).toFloatArray1D();
        CMatrix cm = CMatrix.getInstance(f)
                .transpose()
                //.plot()
                //.perlinNoise(0.03f)
                //.shape()
                //.plot(f)
                //.println()
                //.plot(CMatrix.getInstance().linspace(-3, 3, 10).toFloatArray1D())
                //.plot(CMatrix.getInstance().linspace(-3, 3, 10).toFloatArray1D())
                //.plot(f)
                //.rand(1, 100)
                .plot()
                ;
    }

    private static void testPidController() {
        PidController pid = new PidController(0.25, 0.01, 0.4);
        pid.setOutputLimits(10);
        //pid.setMaxIOutput(2);
        //pid.setOutputRampRate(3);
        //pid.setOutputFilter(.3);
        pid.setSetpointRange(40);

        double target = 100;

        double actual = 0;
        double output = 0;

        pid.setSetpoint(0);
        pid.setSetpoint(target);
        

        System.err.printf("Target\tActual\tOutput\tError\n");
        //System.err.printf("Output\tP\tI\tD\n");

        // Position based test code
        int n=200;
        double[][] data=new double[4][n];
        for (int i = 0; i < n; i++) {

            //if(i==50)miniPID.setI(.05);
            if (i == n/2) {
                target = target/2;
            }

            //if(i==75)target=(100);
            //if(i>50 && i%4==0)target=target+(Math.random()-.5)*50;
            output = pid.getOutput(actual, target);
            actual = actual + output;

            //System.out.println("=========================="); 
            //System.out.printf("Current: %3.2f , Actual: %3.2f, Error: %3.2f\n",actual, output, (target-actual));
            System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f\n", target, actual, output, (target - actual));
            data[0][i]=target;
            data[1][i]=actual;
            data[2][i]=output;
            data[3][i]=(target - actual);

            //if(i>80 && i%5==0)actual+=(Math.random()-.5)*20;
        }
        
        CMatrix cm = CMatrix.getInstance(data)
                .plot(new String[]{"Pid Controller"},new String[]{"target","actual","output","(target-actual)"})
                ;

    }
}
