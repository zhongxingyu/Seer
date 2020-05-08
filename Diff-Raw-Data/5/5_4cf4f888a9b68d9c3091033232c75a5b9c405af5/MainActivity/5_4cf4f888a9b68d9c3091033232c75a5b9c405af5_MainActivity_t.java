 
 package com.uraroji.garage.android.arrraycopybench;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import org.apache.commons.lang3.time.StopWatch;
 
 import java.util.Arrays;
 
 public class MainActivity extends Activity {
 
     private Spinner mArrayTypeSpinner;
     private Spinner mArrayLengthSpinner;
     private Spinner mBenchTimesSpinner;
     private TextView mResultTextView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mArrayTypeSpinner = (Spinner) findViewById(R.id.ArrayTypeSpinner);
         mArrayLengthSpinner = (Spinner) findViewById(R.id.ArrayLengthSpinner);
         mBenchTimesSpinner = (Spinner) findViewById(R.id.BenchTimesSpinner);
         mResultTextView = (TextView) findViewById(R.id.ResultTextView);
 
         Button startButton = (Button) findViewById(R.id.StartButton);
         startButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 startBench();
             }
         });
     }
 
     private void startBench() {
         mResultTextView.setText("");
 
         final String arrayTypeStr = mArrayTypeSpinner.getSelectedItem().toString();
         final int arrayLength = Integer.parseInt(mArrayLengthSpinner.getSelectedItem().toString());
         final int benchTimes = Integer.parseInt(mBenchTimesSpinner.getSelectedItem().toString());
 
         final ProgressDialog dialog = new ProgressDialog(this);
         dialog.setMessage(getString(R.string.benchmarking));
         dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         dialog.setCancelable(false);
         dialog.show();
         
         final Handler handler = new Handler();
         
         if (arrayTypeStr.equals("byte")) {
             final byte[] src = new byte[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = (byte) i;
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     benchResult = copyNative(src, benchTimes);
                     showResult("native", benchResult, handler);
 
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("short")) {
             final short[] src = new short[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = (short) i;
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     benchResult = copyNative(src, benchTimes);
                     showResult("native", benchResult, handler);
 
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("int")) {
             final int[] src = new int[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = (int) i;
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     benchResult = copyNative(src, benchTimes);
                     showResult("native", benchResult, handler);
 
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("long")) {
             final long[] src = new long[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = (long) i;
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     benchResult = copyNative(src, benchTimes);
                     showResult("native", benchResult, handler);
 
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("double")) {
             final double[] src = new double[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = (double) i;
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
 
                     benchResult = copyNative(src, benchTimes);
                     showResult("native", benchResult, handler);
 
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("Object")) {
             final Object[] src = new Object[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = new Object();
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("Byte")) {
             final Byte[] src = new Byte[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = new Byte((byte) i);
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("Integer")) {
             final Integer[] src = new Integer[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = new Integer((int) i);
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("Long")) {
             final Long[] src = new Long[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = new Long((long) i);
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     dialog.dismiss();
                 }
             }).start();
         } else if (arrayTypeStr.equals("Double")) {
             final Double[] src = new Double[arrayLength];
             for (int i = 0; i < src.length; ++i) {
                 src[i] = new Double((double) i);
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     BenchResult benchResult = null;
 
                     benchResult = copyClone(src, benchTimes);
                     showResult("clone", benchResult, handler);
 
                     benchResult = copyArraycopy(src, benchTimes);
                     showResult("System.arraycopy", benchResult, handler);
 
                     benchResult = copyArraysCopyOf(src, benchTimes);
                     showResult("Arrays.copyOf", benchResult, handler);
 
                     benchResult = copyForLoop(src, benchTimes);
                     showResult("for loop", benchResult, handler);
                     
                     dialog.dismiss();
                 }
             }).start();
         } else {
             dialog.dismiss();
         }
     }
 
     private void showResult(final String title, final BenchResult benchResult, Handler handler) {
         handler.post(new Runnable() {
             public void run() {
                 mResultTextView.setText(mResultTextView.getText()
                         + title + " :\n");
                 mResultTextView.setText(mResultTextView.getText()
                         + "\taverage " + String.format("%.2f", benchResult.average() / 1000d)
                        + " microsec\n");
                 mResultTextView.setText(mResultTextView.getText()
                         + "\ttotal "
                        + String.format("%.2f", benchResult.total() / 1000d) + " microsec\n");
             }
         });
     }
 
     private static BenchResult copyClone(byte[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         byte[] copy = null;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = (byte[]) array.clone();
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraycopy(byte[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         byte[] copy = new byte[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             System.arraycopy(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraysCopyOf(byte[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         byte[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = Arrays.copyOf(array, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyForLoop(byte[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         byte[] copy = new byte[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             for (int l = 0; l < array.length; ++l) {
                 copy[l] = array[l];
             }
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyNative(byte[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         byte[] copy = new byte[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             NativeCopy.copyNative(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyClone(short[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         short[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = (short[]) array.clone();
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraycopy(short[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         short[] copy = new short[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             System.arraycopy(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraysCopyOf(short[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         short[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = Arrays.copyOf(array, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyForLoop(short[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         short[] copy = new short[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             for (int l = 0; l < array.length; ++l) {
                 copy[l] = array[l];
             }
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
 
     private static BenchResult copyNative(short[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         short[] copy = new short[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             NativeCopy.copyNative(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyClone(int[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         int[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = (int[]) array.clone();
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraycopy(int[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         int[] copy = new int[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             System.arraycopy(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraysCopyOf(int[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         int[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = Arrays.copyOf(array, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyForLoop(int[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         int[] copy = new int[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             for (int l = 0; l < array.length; ++l) {
                 copy[l] = array[l];
             }
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyNative(int[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         int[] copy = new int[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             NativeCopy.copyNative(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyClone(long[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         long[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = (long[]) array.clone();
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraycopy(long[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         long[] copy = new long[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             System.arraycopy(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraysCopyOf(long[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         long[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = Arrays.copyOf(array, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyForLoop(long[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         long[] copy = new long[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             for (int l = 0; l < array.length; ++l) {
                 copy[l] = array[l];
             }
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyNative(long[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         long[] copy = new long[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             NativeCopy.copyNative(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyClone(double[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         double[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = (double[]) array.clone();
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraycopy(double[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         double[] copy = new double[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             System.arraycopy(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraysCopyOf(double[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         double[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = Arrays.copyOf(array, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyForLoop(double[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         double[] copy = new double[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             for (int l = 0; l < array.length; ++l) {
                 copy[l] = array[l];
             }
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyNative(double[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         double[] copy = new double[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             NativeCopy.copyNative(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyClone(Object[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         Object[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = (Object[]) array.clone();
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraycopy(Object[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         Object[] copy = new Object[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             System.arraycopy(array, 0, copy, 0, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyArraysCopyOf(Object[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         @SuppressWarnings("unused")
         Object[] copy;
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             copy = Arrays.copyOf(array, array.length);
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static BenchResult copyForLoop(Object[] array, int numIterations) {
         BenchResult result = new BenchResult();
         StopWatch stopWatch = new StopWatch();
         Object[] copy = new Object[array.length];
 
         for (int i = 0; i < numIterations; ++i) {
             stopWatch.reset();
             stopWatch.start();
             for (int l = 0; l < array.length; ++l) {
                 copy[l] = array[l];
             }
             stopWatch.stop();
             result.add(stopWatch.getNanoTime());
         }
 
         return result;
     }
 
     private static class BenchResult {
         private int mSize;
         private long mTotal;
 
         public BenchResult() {
         }
 
         public void add(long value) {
             mTotal += value;
             ++mSize;
         }
 
         public long total() {
             return mTotal;
         }
 
         public double average() {
             if (mSize != 0) {
                 return (double) mTotal / mSize;
             } else {
                 return 0;
             }
         }
     }
 }
