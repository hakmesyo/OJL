/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class DataSet {

    public List<Data> data = new ArrayList();
    public float[] classLabelIndex;
    public int nChannel = 1;
    public int nClasses;

    public float[][][][] getDataX() {
        int nr = data.size();
        float[][][][] ret = new float[nr][nChannel][][];
        if (nChannel == 1) {
            for (int i = 0; i < nr; i++) {
                ret[i][0] = data.get(i).gray;
            }
        }else if(nChannel==3){
            for (int i = 0; i < nr; i++) {
                ret[i][0] = data.get(i).red;
                ret[i][1] = data.get(i).green;
                ret[i][2] = data.get(i).blue;
            }
        }
        return ret;
    }
    
    public float[][][][] getSubsetX(int fromIndex, int toIndex) {
        int nr = toIndex-fromIndex;
        float[][][][] ret = new float[nr][nChannel][][];
        if (nChannel == 1) {
            for (int i = fromIndex; i < toIndex; i++) {
                ret[i-fromIndex][0] = data.get(i).gray;
            }
        }else if(nChannel==3){
            for (int i = fromIndex; i < toIndex; i++) {
                ret[i-fromIndex][0] = data.get(i).red;
                ret[i-fromIndex][1] = data.get(i).green;
                ret[i-fromIndex][2] = data.get(i).blue;
            }
        }
        return ret;
    }
    
    public float[][] getDataY() {
        int nr = data.size();
        float[][] ret = new float[nr][nClasses];
        for (int i = 0; i < nr; i++) {
            int cl_index=data.get(i).classLabelIndex;
            ret[i]=FactoryUtils.getOneHotEncoding(nClasses, (int)this.classLabelIndex[cl_index]);
        }
        return ret;
    }

    public float[][] getSubsetY(int fromIndex, int toIndex) {
        int nr = toIndex-fromIndex;
        float[][] ret = new float[nr][nClasses];
        for (int i = fromIndex; i < toIndex; i++) {
            int cl_index=data.get(i).classLabelIndex;
            ret[i-fromIndex]=FactoryUtils.getOneHotEncoding(nClasses, (int)this.classLabelIndex[cl_index]);
        }
        return ret;
    }

    public float[][] getSubsetYEnsemble(int fromIndex, int toIndex) {
        int nr = toIndex-fromIndex;
        float[][] ret = new float[nr][nClasses];
        for (int i = fromIndex; i < toIndex; i++) {
            int cl_index=data.get(i).classLabelIndex;
            ret[i-fromIndex]=FactoryUtils.getOneHotEncoding(nClasses, cl_index);
        }
        return ret;
    }

    public void shuffle(Random rnd) {
        Collections.shuffle(data,rnd);
    }

}
