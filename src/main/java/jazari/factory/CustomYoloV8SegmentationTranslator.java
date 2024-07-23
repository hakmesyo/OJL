/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.factory;

/**
 *
 * @author cezerilab
 */
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloV8Translator;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.TranslatorContext;
import java.util.List;

//public class CustomYoloV8SegmentationTranslator extends YoloV8Translator {
public class CustomYoloV8SegmentationTranslator{

//    public CustomYoloV8SegmentationTranslator(Builder builder) {
//        super(builder);
//    }
//
//    @Override
//    public DetectedObjects processOutput(TranslatorContext ctx, NDList list) {
//        NDArray predictions = list.singletonOrThrow();
//        int numClasses = 5;  // Modelinizin gerçek sınıf sayısı
//        int numMasks = predictions.getShape().get(1) - numClasses - 4;
//        
//        // predictions'ı yeniden şekillendirin
//        predictions = predictions.reshape(-1, numClasses + 4 + numMasks);
//        
//        return super.processOutput(ctx, new NDList(predictions));
//    }
//
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    public static final class Builder extends YoloV8Translator.Builder<Builder> {
//        @Override
//        protected Builder self() {
//            return this;
//        }
//
//        public CustomYoloV8SegmentationTranslator build() {
//            return new CustomYoloV8SegmentationTranslator(this);
//        }
//    }
}
