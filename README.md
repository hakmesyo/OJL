# OJL [Open Jazari Library with Maven Support]
OJL is a matrix, data visualization, image processing, computer vision, machine learning and deep learning framework developed entirely for JVM languages.

Cite as : Ataş, M. (2016). Open Cezeri Library: A novel java based matrix and computer vision framework. Computer Applications in Engineering Education, 24(5), 736-743.

1- Data Visualization Module

-- Plot 2 different Perlin noise signals

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

-- Bar Plot Example with TFigureAttribute object

        TFigureAttribute attr=new TFigureAttribute(
                "Lane Detection Performance Evaluation",
                new String[]{"Accuracy","Groups"},
                new String[]{"Epoch-10","Epoch-20","Epoch-30","Epoch-40","Epoch-50"},
                new String[]{"SCNN","U-Net","ENet","ENet-SAD"}
        );
        CMatrix cm = CMatrix.getInstance()
                .rand(4, 5, -150f, 151f)
                .bar(attr)
                ;


![image](https://github.com/hakmesyo/OJL/assets/3868513/4e8e25c0-5826-4d78-b323-9f9374a7d8a2)

![image](https://github.com/hakmesyo/OJL/assets/3868513/a8314eac-426d-4f14-b9a3-cce509380fb2)



