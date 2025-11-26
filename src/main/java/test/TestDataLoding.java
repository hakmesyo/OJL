package test;

import jazari.matrix.CMatrix;

public class TestDataLoding {

	public static void main(String[] args) {
		//CMatrix cm = CMatrix.getInstance().rand(10,10, 0).range(100).perlinNoise().tr().plot();
		// Your code here
		//System.out.println("Hello from TestDataLoding");
		CMatrix cm = CMatrix.getInstance().readARFF("dataset\\iris.arff").println();
				//;
		//CMatrix cm1=cm.clone().cmd(":","0").println();		
		
	}
}
