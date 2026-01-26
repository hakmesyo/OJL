# Open Jazari Library (OJL)

![OJL Version](https://img.shields.io/badge/OJL-1.0.0-orange.svg)
![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)
[![Processing](https://img.shields.io/badge/Processing-4.3-blue.svg)](https://processing.org/)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow.svg)](https://opensource.org/licenses/Apache-2.0)

## 📘 Overview

OJL (Open Jazari Library) is a comprehensive framework designed for JVM languages, specializing in:
- Matrix Operations
- Data Visualization
- Image Processing
- Computer Vision
- Machine Learning
- Deep Learning
- JAZO Labeling Tool
-- Classic Bounding Box
-- Classic Polygon
-- Spline Based Lane Labeling
-- [Future Version] Automatic Labeling (SAM Based)

### 📖 Academic Citation

Ataş, M. (2016). Open Cezeri Library: A novel java based matrix and computer vision framework. Computer Applications in Engineering Education, 24(5), 736-743.

## 🚀 Key Features

- Full JVM language compatibility
- High-performance matrix computations
- Advanced visualization techniques
- Integrated machine learning tools
- Comprehensive image processing capabilities

## 📦 Installation

### Maven Dependency

```xml
<dependency>
    <groupId>com.github.hakmesyo</groupId>
    <artifactId>ojl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle Dependency

```groovy
implementation 'com.github.hakmesyo:ojl:1.0.0'
```

## 🛠 Usage Examples

### 1. Data Visualization: Perlin Noise Plotting

```java
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
```

### 2. Bar Plot with Custom Attributes

```java
TFigureAttribute attr = new TFigureAttribute(
        "Lane Detection Performance Evaluation",
        new String[]{"Accuracy","Groups"},
        new String[]{"Epoch-10","Epoch-20","Epoch-30","Epoch-40","Epoch-50"},
        new String[]{"SCNN","U-Net","ENet","ENet-SAD"}
);
CMatrix cm = CMatrix.getInstance()
        .rand(4, 5, -150f, 151f)
        .bar(attr);
```

## 📚 Documentation

Comprehensive documentation available at [OJL Documentation](https://github.com/hakmesyo/OJL/wiki)

## 🤝 Contributing

We welcome contributions! Please read our [Contribution Guidelines](CONTRIBUTING.md)

## 📞 Contact

- **Project Owner:** Musa Ataş
- **Email:** hakmesyo@gmail.com, musa.atas@siirt.edu.tr
- **GitHub:** https://github.com/hakmesyo
- **Project Link:** [https://github.com/hakmesyo/OJL](https://github.com/hakmesyo/OJL)

## 👥 Contributors

- **Contributor:** Berkkan Kaya
- **Contribution:** macOS (Apple Silicon) & Linux platform compatibility
- **Email:** kayaberkkan@gmail.com
- **GitHub:** https://github.com/kayaberkkan

## 📜 License

Distributed under Apache 2.0 License. See `LICENSE` for more information.

---

**Note:** OJL is continuously evolving. Your feedback and contributions are valuable! 🌈
