 import ij.ImagePlus;
 import ij.process.Blitter;
 import ij.process.ByteProcessor;
 import ij.process.ImageProcessor;
 
 import java.awt.Color;
 import java.util.List;
 
 public class TextureTools {
     private TextureTools() {
     }
 
     final static int BOX_SIZE = 32;
 
     final static private int NUM_LAP_PYR_LEVELS = 4;
 
     final static private float[] GAUSSIAN_5X5 = { 2, 4, 5, 4, 2, 4, 9, 12, 9,
             4, 5, 12, 15, 12, 5, 4, 9, 12, 9, 4, 2, 4, 5, 4, 2 };
 
     static IntegralImage[] generateLaplacianIntegralPyramid(ImageProcessor img) {
         ImageProcessor[] gaussianIm = new ImageProcessor[NUM_LAP_PYR_LEVELS + 1];
         ImageProcessor[] laplacianIm = new ImageProcessor[NUM_LAP_PYR_LEVELS + 1];
         IntegralImage[] result = new IntegralImage[NUM_LAP_PYR_LEVELS * 3];
 
         // make gaussian pyramid
         gaussianIm[NUM_LAP_PYR_LEVELS] = img;
 
         for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
             int gindex = NUM_LAP_PYR_LEVELS - i;
             gaussianIm[gindex] = createOnePyramidStepDown(gaussianIm[gindex + 1]);
         }
 
         // for (ImageProcessor imageProcessor : gaussianIm) {
         // System.out.println("gauss: " + imageProcessor);
         // }
 
         // make laplacian pyramid
         laplacianIm[0] = gaussianIm[0];
         for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
             laplacianIm[i - 1]
                     .setInterpolationMethod(ImageProcessor.NEAREST_NEIGHBOR);
            laplacianIm[i] = laplacianIm[i - 1]
                    .resize(gaussianIm[i].getWidth());
             laplacianIm[i].copyBits(gaussianIm[i], 0, 0, Blitter.DIFFERENCE);
         }
 
         // for (ImageProcessor imageProcessor : laplacianIm) {
         // System.out.println("laplace: " + imageProcessor);
         // }
 
         // make integral images
         for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
             int ii = (i - 1) * 3;
 
             ByteProcessor rr = (ByteProcessor) laplacianIm[i].toFloat(0, null)
                     .convertToByte(true);
             ByteProcessor gg = (ByteProcessor) laplacianIm[i].toFloat(1, null)
                     .convertToByte(true);
             ByteProcessor bb = (ByteProcessor) laplacianIm[i].toFloat(2, null)
                     .convertToByte(true);
 
             result[ii] = new IntegralImage(rr);
             result[ii + 1] = new IntegralImage(gg);
             result[ii + 2] = new IntegralImage(bb);
         }
 
         // display ?
         if (false) {
             for (int i = 0; i < gaussianIm.length; i++) {
                 new ImagePlus("gaussian " + i, gaussianIm[i]).show();
             }
             for (int i = 0; i < laplacianIm.length; i++) {
                 new ImagePlus("laplacian " + i, laplacianIm[i]).show();
             }
         }
         if (false) {
             for (int i = 0; i < result.length; i++) {
                 new ImagePlus("integral " + i, result[i].toImageProcessor())
                         .show();
             }
         }
 
         return result;
     }
 
     static double[] generateFeatures(IntegralImage imgs[]) {
         IntegralImage ii = imgs[imgs.length - 1];
         return generateFeatures(imgs, 0, 0, ii.getWidth(), ii.getHeight());
     }
 
     static double[] generateFeatures(IntegralImage imgs[], int x, int y, int w,
             int h) {
         double result[] = new double[imgs.length];
 
         for (int i = 0; i < result.length; i++) {
             int scale = ((result.length - 1) - i) / 3;
             int xx = x >> scale;
             int yy = y >> scale;
             int ww = w >> scale;
             int hh = h >> scale;
 
             // System.out.println("(" + x + "," + y + "), (" + w + "x" + h +
             // ")");
             // System.out.println(" scale: " + scale + ", (" + xx + "," + yy
             // + "), (" + ww + "x" + hh + ")");
             result[i] = imgs[i].getAverage(xx, yy, ww, hh);
         }
 
         return result;
     }
 
     private static ImageProcessor createOnePyramidStepDown(ImageProcessor img) {
         ImageProcessor blurred = img.duplicate();
         blurred.convolve(GAUSSIAN_5X5, 5, 5);
         blurred.setInterpolationMethod(ImageProcessor.NEAREST_NEIGHBOR);
         return blurred.resize(blurred.getWidth() / 2);
     }
 
     static void findPairwiseMatches(IntegralImage[] imgs,
             List<double[]> features, ImageProcessor output, int boxSize,
             double distanceThreshold) {
         int w = output.getWidth();
         int h = output.getHeight();
 
         for (int y = 0; y < h - boxSize; y++) {
             for (int x = 0; x < w - boxSize; x++) {
                 double minDistance = Double.MAX_VALUE;
                 double ff[] = generateFeatures(imgs, x, y, boxSize, boxSize);
                 for (double[] feature : features) {
                     double distance = 0.0;
                     for (int i = 0; i < feature.length; i++) {
                         if (ff[i] > feature[i]) {
                             distance += Math.abs(ff[i] - feature[i]) / ff[i];
                         } else {
                             distance += Math.abs(ff[i] - feature[i])
                                     / feature[i];
                         }
                     }
                     distance /= feature.length;
                     // System.out.println(Arrays.toString(ff));
                     // System.out.println(" " + Arrays.toString(feature));
                     // System.out.println(" " + distance);
                     minDistance = Math.min(distance, minDistance);
                 }
 
                 // System.out.println("minDistance: " + minDistance);
                 if (false) {
                     output.setRoi(x, y, boxSize, boxSize);
                     float v = (float) minDistance;
                     output.setColor(new Color(v, v, v));
                     output.fill();
                 } else if (minDistance <= distanceThreshold) {
                     // System.out.println("filling " + x + "," + y);
                     output.setRoi(x, y, boxSize, boxSize);
                     output.fill();
                 }
             }
         }
     }
 }
