/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.onnx;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dell_lab
 */
public class ONNXModelRunner {
    public static void main(String[] args) {
        String modelPath="D:\\DATASETS\\SAM/sam_onnx_example.onnx";
        try {
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            OrtSession session = env.createSession(modelPath, options);
            
            Map<String,NodeInfo> inputInfo=session.getInputInfo();
            Set<String> inputNames=session.getInputNames();
            
            //Map<String, OnnxTensor> inputs=new HashMap();
            OnnxTensor t1=null,t2=null;
            Map<String, OnnxTensor> inputs=Map.of("name1",t1,"name2",t2);
                    

            // Modelin giriş i
            
            env.close();
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }
}
