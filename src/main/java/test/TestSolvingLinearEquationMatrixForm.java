/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestSolvingLinearEquationMatrixForm {

    /**
     * x	+	y	+	z	=	6
     * 2y	+	5z	=	−4 2x	+	5y	−	z	=	27
     *
     * @param args
     */
    public static void main(String[] args) {
        float[][] A = {
            {1, 1, 1},
            {0, 2, 5},
            {2, 5, -1}
        };

//        //singular matrix
//        float[][] A = {
//            {-1, 2, -3},
//            {4, -5, 6},
//            {-7, 8, -9}
//        };
        float[][] B = {
            {6},
            {-4},
            {27}
        };

        CMatrix cmA = CMatrix.getInstance(A).println();
        CMatrix cmB = CMatrix.getInstance(B).println();
        /**
         * AX = B X = A(-1)B
         */
        CMatrix cmX = cmA.clone().inv().println().dot(cmB).println();

        /**
         * first take transposes of all given matrixes XA = B XAA(-1)=BA(-1)
         */
        CMatrix cmX2 = cmB.clone().T().println().dot(cmA.clone().tr().inv()).println();
    }
}
