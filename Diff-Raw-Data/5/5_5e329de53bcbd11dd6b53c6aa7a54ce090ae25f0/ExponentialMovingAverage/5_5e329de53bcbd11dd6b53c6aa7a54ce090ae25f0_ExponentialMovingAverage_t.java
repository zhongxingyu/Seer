 
 /**
  * The Exponential Moving Average is similar to the Linear Weighted Moving Average except it applies exponentially decreasing weighting factors to the data points
  * @author Team Gredona
  */
 public class ExponentialMovingAverage extends Strategy {
 
     public ExponentialMovingAverage() {
         type = "ExponentialMovingAverage";
         typeInt = 2;
     }
 
     @Override
     protected float computeSlowMovingAverage() {
         //int t = slowDataBuffer.size();
        float alpha = 2.0f / (SLOW_PERIOD + 1);
         
         //Because N and t are not dependent on each other, we can let EMA_1 = price_1
         if (slowDataBuffer.size() == 1) {
             return slowDataBuffer.peekLast();
         }
         
         //Then we use the recursive formula for all t > 1
         return currentSlowMovingAverage + alpha * (slowDataBuffer.peekLast() - currentSlowMovingAverage);
     }
 
     @Override
     protected float computeFastMovingAverage() {
         //int t = slowDataBuffer.size();
        float alpha = 2.0f / (FAST_PERIOD + 1);
         
         //Because N and t are not dependent on each other, we can let EMA_1 = price_1
         if (fastDataBuffer.size() == 1) {
             return fastDataBuffer.peekLast();
         }
         
         //Then we use the recursive formula for all t > 1
         return currentFastMovingAverage + alpha * (fastDataBuffer.peekLast() - currentFastMovingAverage);
     }
 }
