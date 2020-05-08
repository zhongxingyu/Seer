 package edu.sunyit.chryslj.barcode;
 
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.util.Log;
 
 /**
  * 
  * @author Justin Chrysler
  * 
  */
 public class BarcodeProcessor
 {
     private static final String TAG = BarcodeProcessor.class.getSimpleName();
 
     // TODO FOR TESTING ONLY.
     public BarcodeProcessor()
     {
     }
 
     /**
      * 
      * @param image
      * @param type
      * @return
      */
     public long decodeImage(Bitmap image, BarcodeDecoder type)
     {
         // TODO
         return 0;
     }
 
     /**
      * 
      * @param image
      * @return
      */
     public BarcodeDecoder determineType(Bitmap image)
     {
         // TODO
         return null;
     }
 
     /**
      * The <code>image</code> data that we are using to generate the binary
      * image is in NV21 format (<a href="http://www.fourcc.org/yuv.php#NV21">YUV
      * pixel formats NV21</a>). This format stores the Y plane in a range from
      * 16 to 255. We need to keep this in mind as we write the algorithms to
      * generate the binary image we need.
      * 
      * @param image
      * @return
      */
     public Bitmap generateBinaryImage(int width, int height, byte[] image)
     {
        int row = (height / 2) * width;
         Bitmap grayBMP = convertToGrayScale(width, height, image);
 
         // TODO great candidate for a new thread
 
         // final int localWidth = width / 4;
         // final int localHeight = height / 4;
         //
         // Bitmap binaryImage = Bitmap.createBitmap(width, height,
         // Bitmap.Config.ARGB_8888);
         //
         // // Lets partition the image off into a few grids. If the grid we
         // // currently want to use would exceed the size of teh bitmap then
         // // adjust to be within the bounds.
         //
         // for (int gridX = 0; gridX < width; gridX += localWidth)
         // {
         // if ((gridX + localWidth) > width)
         // {
         // gridX = width - localWidth;
         // }
         //
         // for (int gridY = 0; gridY < height; gridY += localHeight)
         // {
         // if ((gridY + localHeight) > height)
         // {
         // gridY = height - localHeight;
         // }
         //
         // int[] gridPixelData = new int[localWidth * localHeight];
         // grayBMP.getPixels(gridPixelData, 0, localWidth, gridX, gridY,
         // localWidth, localHeight);
         //
         // int binaryThreshold = otsuBinarythreshold(gridPixelData);
         //
         // for (int column = 0; column < localWidth; column++)
         // {
         // for (int row = 0; row < localHeight; row++)
         // {
         // int newRed;
         // int newGreen;
         // int newBlue;
         // int pixelValue = getPixelValue(grayBMP.getPixel(column +
         // gridX, row + gridY));
         // if (pixelValue >= binaryThreshold)
         // {
         // newRed = 255;
         // newGreen = 255;
         // newBlue = 255;
         // }
         // else
         // {
         // newRed = 0;
         // newGreen = 0;
         // newBlue = 0;
         // }
         //
         // binaryImage.setPixel(column + gridX, row + gridY,
         // Color.rgb(newRed, newGreen, newBlue));
         // }
         // }
         // }
         // }
 
         // Previous attempts using the above commented out code has left
         // undesirable results for the binary image. Lets try to convert only
         // the row we are interested in.
         byte[] rowData = new byte[width];
         int[] binaryRowData = new int[width];
         System.arraycopy(image, row, rowData, 0, width);
 
         int[] rowHistogram = generateImageHistogram(rowData);
 
         int binaryThreshold = getThresholdValue(rowHistogram);
 
         Log.d(TAG, "BinaryThreshold: " + binaryThreshold);
         StringBuilder sb = new StringBuilder();
 
         int left = rowData[0] & 0xff;
         int center = rowData[1] & 0xff;
         for (int column = 1; column < width - 1; column++)
         {
             int right = rowData[column + 1] & 0xff;
             int pixelValue = ((center * 4) - left - right) / 2;
            Log.d(TAG, "PixelValue: " + pixelValue);
             if (pixelValue < binaryThreshold)
             {
                 binaryRowData[column] = Color.BLACK;
                 sb.append("B ");
             }
             else
             {
                 binaryRowData[column] = Color.WHITE;
                 sb.append("W ");
             }
             left = center;
             center = right;
         }
 
         Log.d(TAG, "Binary Row: " + sb.toString());
 
        grayBMP.setPixels(binaryRowData, 0, width, 0, height / 2, width, 1);
 
         // grayBMP.recycle();
 
         return grayBMP;
     }
 
     private int getThresholdValueIterative(byte[] rowData, int[] rowHistogram,
             int threshold)
     {
         int setOneAvg = 0;
         int setTwoAvg = 0;
 
         ArrayList<Integer> setOne = new ArrayList<Integer>();
         ArrayList<Integer> setTwo = new ArrayList<Integer>();
 
         for (int pixel = 0; pixel < rowData.length; pixel++)
         {
             int pixelValue = rowData[pixel] & 0xff;
             if (pixelValue > threshold)
             {
                 setOne.add(pixelValue);
             }
             else
             {
                 setTwo.add(pixelValue);
             }
         }
 
         for (int value : setOne)
         {
             setOneAvg += value;
         }
 
         setOneAvg = setOneAvg / ((setOne.size() != 0) ? setOne.size() : 1);
 
         for (int value : setTwo)
         {
             setTwoAvg += value;
         }
 
         setTwoAvg = setTwoAvg / ((setTwo.size() != 0) ? setTwo.size() : 1);
 
         return (setOneAvg + setTwoAvg) / 2;
     }
 
     /**
      * Using the histogram data we can now find where most of the pixels in the
      * image lie. By finding the two pixel values that are used most often and
      * then finding a suitable place between the two pixel values we can
      * determine a threshold value to be used in the generation of the binary
      * image.
      * 
      * @param histogramData
      * @return
      */
     private int getThresholdValue(int[] histogramData)
     {
         int binaryThreshold = 0;
         int maxHistogramValue = 0;
 
         int firstPeakValue = 0;
         int firstPeakSize = 0;
         int secondPeakValue = 0;
         int secondPeakSize = 0;
 
         for (int x = 0; x < histogramData.length; x++)
         {
             // Check if histogramData[x] is the max size
             if (histogramData[x] > maxHistogramValue)
             {
                 maxHistogramValue = histogramData[x];
             }
 
             // See if we found a new first peak
             if (histogramData[x] > firstPeakSize)
             {
                 firstPeakValue = x;
                 firstPeakSize = histogramData[x];
             }
         }
 
         for (int x = 0; x < histogramData.length; x++)
         {
             int currentDistanceFromLargestPeak = x - firstPeakValue;
 
             int score =
                     histogramData[x] * currentDistanceFromLargestPeak *
                             currentDistanceFromLargestPeak;
             if (score > secondPeakSize)
             {
                 secondPeakValue = x;
                 secondPeakSize = score;
             }
         }
 
         if (firstPeakValue > secondPeakValue)
         {
             int swap = firstPeakValue;
             firstPeakValue = secondPeakValue;
             secondPeakValue = swap;
         }
 
         int bestValleyValue = secondPeakValue - 1;
         int bestValleySize = -1;
 
         for (int x = bestValleyValue; x > firstPeakValue; x--)
         {
             int distanceFromFirst = x - firstPeakValue;
             int score =
                     distanceFromFirst * distanceFromFirst *
                             (secondPeakValue - x) *
                             (maxHistogramValue - histogramData[x]);
             if (score > bestValleySize)
             {
                 bestValleyValue = x;
                 bestValleySize = score;
             }
         }
 
         return bestValleyValue;
     }
 
     /**
      * Using Otsu's algorithm to generate an image with only two values. This
      * helps reduce the work needed to detect edges, or in our case to detect
      * lines of a barcode.
      * 
      * @see <a href="http://en.wikipedia.org/wiki/Otsu's_method">Otsu's
      *      method</a>
      * @param grayBMP
      * @return
      */
     private int otsuBinaryThreshold(byte[] grayPixelData)
     {
         int binaryThreshold = 0;
         float sum = 0;
         float sumB = 0;
         int backgroundWeight = 0;
         int foregroundWegiht = 0;
         float maxVariance = 0;
 
         int[] histogram = generateImageHistogram(grayPixelData);
         int numberOfPixels = grayPixelData.length;
 
         for (int index = 0; index < histogram.length; index++)
         {
             sum += index * histogram[index];
         }
 
         for (int index = 0; index < histogram.length; index++)
         {
             backgroundWeight += histogram[index];
             // Make sure the background weight isn't 0. Do not want to divide
             // by zero later on.
             if (backgroundWeight == 0)
             {
                 continue;
             }
 
             foregroundWegiht = numberOfPixels - backgroundWeight;
             // Check if we have gone through the entire image.
             if (foregroundWegiht == 0)
             {
                 break;
             }
 
             sumB += (float) (index * histogram[index]);
 
             float backgroundMean = sumB / backgroundWeight;
             float foregroundMean = (sum - sumB) / foregroundWegiht;
 
             // Calculate the variance between the classes
             float weightDiff = (backgroundMean - foregroundMean);
             // Otsu: w1(t)*w2(t)*[u1(t) - u2(t)]^2
             float betweenVariance =
                     (float) backgroundWeight * (float) foregroundWegiht *
                             weightDiff * weightDiff;
 
             // See if there is a new max variance
             if (betweenVariance > maxVariance)
             {
                 maxVariance = betweenVariance;
                 binaryThreshold = index;
             }
         }
 
         return binaryThreshold;
     }
 
     /**
      * Get the Y plane of the YCrCB data (NV21 pixel format 4:2:0). The Y plane
      * is a grayscale version of the image and is stored in the first width *
      * height bytes of the colorImageData array.
      * 
      * @param colorImageData
      *            the YCrCB image data we want the grayscale of.
      * @return
      */
     private Bitmap convertToGrayScale(int width, int height,
             byte[] colorImageData)
     {
         int[] pixelData = new int[width * height];
         int yDataOffset = width;
 
         for (int row = 0; row < height; row++)
         {
             int pixelOffset = row * width;
             for (int column = 0; column < width; column++)
             {
                 // Get the grey value from the array and force it to be between
                 // 16 and 255. In the array it could be a negative number.
                 int yPlaneGreyVal = colorImageData[yDataOffset + column] & 0xFF;
 
                 // This next line gives us an ARBG_8888 pixel. It does this by
                 // placing FF (255) in the Alpha byte LOGICAL OR'd with the grey
                 // value from the Y plane multiplied by 0x00010101. This
                 // multiplication propagates the grey value into the RBG bytes.
                 pixelData[pixelOffset + column] =
                         0xFF000000 | (yPlaneGreyVal * 0x00010101);
             }
             yDataOffset += width;
         }
 
         Bitmap grayBMP =
                 Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
         grayBMP.setPixels(pixelData, 0, width, 0, 0, width, height);
         return grayBMP;
     }
 
     /**
      * This method makes the assumption that the input array only has between
      * -127 and 128 values. These values LOGICAL AND'd with 0xff gives us a
      * value between 0 and 255. The image data we are using should only line up
      * between 16 and 255.
      * 
      * @param pixelData
      *            the array of pixel values that we want to generate a histogram
      *            for.
      * @return
      */
     private int[] generateImageHistogram(byte[] pixelData)
     {
         int[] histogram = new int[256];
 
         for (int index = 0; index < histogram.length; index++)
         {
             histogram[index] = 0;
         }
 
         for (byte colorIndex : pixelData)
         {
             // int piexlValue = getPixelValue(colorIndex);
             //
             // histogram[piexlValue]++;
             histogram[colorIndex & 0xff]++;
         }
 
         return histogram;
     }
 
     /**
      * 
      * @param pixel
      * @return
      */
     private int getPixelValue(int pixel)
     {
         int r = Color.red(pixel);
         int g = Color.green(pixel);
         int b = Color.blue(pixel);
 
         return (int) ((r + g + b) / 3);
     }
 }
