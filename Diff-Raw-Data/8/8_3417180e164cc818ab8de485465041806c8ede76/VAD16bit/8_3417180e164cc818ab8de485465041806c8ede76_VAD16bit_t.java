 package talkingnet.core.vad;
 
 import talkingnet.core.Element;
 import talkingnet.core.io.Pushable;
 import talkingnet.core.io.Pushing;
 
 /**
  *
  * @author Alexander Oblovatniy <oblovatniy@gmail.com>
  */
 public class VAD16bit extends Element implements Pushable, Pushing {
 
     private float threshold = 0;
     private int maxLevel = Short.MAX_VALUE;
    private float sum;
     private short sample;
     private float currentLevel;
     
     private Pushable sink;
 
     public VAD16bit(Pushable sink, String title) {
         super(title);
         this.sink = sink;
     }
 
     public void push_in(byte[] data, int size) {
         if (isLevelReached(data)) {
             push_out(data, size);
         }
     }
 
     private boolean isLevelReached(byte[] data) {
         sum = 0;
         
         for (int i = 0; i < data.length; i+=2) {
             sample = (short) ((data[i+1] << Byte.SIZE) + data[i]);
            sum += sample / maxLevel;
         }
         
        currentLevel = (float) (sum / data.length);
         
         return currentLevel >= threshold;
     }
 
     public void push_out(byte[] data, int size) {
         sink.push_in(data, size);
     }
 
     public float getThreshold() {
         return threshold;
     }
 
     public void setThreshold(float threshold) {
         this.threshold = threshold;
     }
 
     public int getMaxLevel() {
         return maxLevel;
     }
 
     public void setMaxLevel(int maxLevel) {
         this.maxLevel = maxLevel;
     }
 }
