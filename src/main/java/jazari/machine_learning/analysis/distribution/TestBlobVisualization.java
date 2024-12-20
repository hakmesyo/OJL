/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.analysis.distribution;

/**
 *
 * @author cezerilab
 */
import java.util.Random;
import jazari.matrix.CMatrix;

public class TestBlobVisualization {

    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                //.setRandomSeed(21)
                .make_blobs(100, 3, 3, 21)
                //.tsne()
                //.scatter()
                ;
        cm.clone().tsne();
        cm.clone().cmd(":","0,1,3").scatter();
        cm.clone().cmd(":","0,2,3").scatter();
        cm.clone().cmd(":","1,2,3").scatter();
//        String[] array = cm.classLabelValues.toArray(new String[0]);
//        ScatterPlotViewer viewer2 = new ScatterPlotViewer(cm.toFloatArray2D(), array);
//        viewer2.show();

    }

}
