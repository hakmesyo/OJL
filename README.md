# OJL
Open Jazari Library with Maven Support

1- Data Visualization Module

1.1 Plot basic random points
CMatrix cm = CMatrix.getInstance()
                .rand(1,100)
                .plot()
                ;
                
![image](https://github.com/hakmesyo/OJL/assets/3868513/93fc4c53-14dd-4062-88f8-988b002688aa)

1.2 Plot 1D signal

float[] f={20.12f,50.13f,35f,62.67f,49.17f,21f,35f,41f,45f,52f};
CMatrix cm = CMatrix.getInstance(f)
                .transpose()
                .plot()
                ;
                
![image](https://github.com/hakmesyo/OJL/assets/3868513/04b77c0e-1e33-4bf5-bc87-bde3b7ab9a46)

