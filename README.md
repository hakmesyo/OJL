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

1.3 Plot 2 different Perlin noise signals

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

![image](https://github.com/hakmesyo/OJL/assets/3868513/37d3b7d2-8658-4565-a62e-0b327261b924)


