 package myorg.classifier;
 
 import java.lang.Math;
 import java.util.List;
 import java.util.Random;
 
 import myorg.io.FeatureVector;
 import myorg.io.WeightVector;
 
 public class SVMPegasosLearner {
 
     public static void learnWithStochasticLoop(
        List<FeatureVector> fVecList,
        float lambda, int numIters, WeightVector w
     ) {
         if (w == null) {
             return;
         }
 
         int dataSize = fVecList.size();
 
         Random rnd = new Random(1000);
 
         for (int i = 1; i <= numIters; i++) {
             float eta = 1.0f / (lambda * i); // Pegasos learning rate
 
             int idx = rnd.nextInt(dataSize);
             FeatureVector fVec = fVecList.get(idx);
 
             learnWithStochasticOneStep(fVec, eta, lambda, w);
         }
     }
 
     public static void learnWithStochasticOneStep(
        FeatureVector fVec, float eta, float lambda, WeightVector w
     ) {
        float y = (fVec.getLabel() > 0) ? 1.0f : -1.0f;
         float ip = w.innerProduct(fVec);
 
        float loss = 1.0f - y * ip;
 
         if (loss > 0.0f) {
             w.addVector(fVec, y * eta);
         }
 
         double b = 1.0 / Math.sqrt(lambda * w.getSquaredNorm());
         if (b < 1.0) {
             w.scale((float)b);
         }
     }
 }
 
