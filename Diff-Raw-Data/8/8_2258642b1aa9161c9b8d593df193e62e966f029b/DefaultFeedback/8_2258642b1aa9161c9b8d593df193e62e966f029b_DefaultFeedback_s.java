 /*
  * polycasso - Cubism Artwork generator
  * Copyright 2009-2011 MeBigFatGuy.com
  * Copyright 2009-2011 Dave Brosius
  * Inspired by work by Roger Alsing
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.polycasso;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.WritableRaster;
 
 /**
  * an immutable class for processing a test image against target image for closeness.
  */
 public class DefaultFeedback implements Feedback {
 
     private byte[] targetBuffer;
     private int width, height;
 
     /**
      * creates a feedback object with a given targetImage. Caches the image bytes in
      * member variables.
      */
     public DefaultFeedback() {
     }
 
     /**
      * caches information about the target image
      * 
      * @param targetImage the target image that will be the judge of test images
      */
     @Override
     public void setTargetImage(BufferedImage targetImage) {
         WritableRaster raster = targetImage.getRaster();
         width = targetImage.getWidth();
         height = targetImage.getHeight();
         DataBufferByte dbb = (DataBufferByte)raster.getDataBuffer();
         targetBuffer = dbb.getData();
     }
 
     /**
      * returns a score of how close the test image is to the target
      * which is the square of the error to the target image
      * 
      * @param testImage the image to score
      * @param previousScore the score of the generated image from which this image was created
      * @param changedArea the area of changed between the parent generated image and this one
      * 
      * @return a score that represents its closeness to ideal
      */
     @Override
     public Score calculateScore(BufferedImage testImage, Score previousScore, Rectangle changedArea) {
 
         WritableRaster raster = testImage.getRaster();
         DataBufferByte dbb = (DataBufferByte)raster.getDataBuffer();
         byte[] testBuffer = dbb.getData();
 
         Score score;
         if ((changedArea == null) || (changedArea.width > changedArea.height)) {
             score = calculateYMajorScore(testBuffer, previousScore, changedArea);
         } else {
             score = calculateXMajorScore(testBuffer, previousScore, changedArea);
         }
 
         //        if (Math.random() < 0.05) {
         //            long realScore = calculateGridScore(testBuffer, 0, 0, width, height);
         //            if (realScore != score.getDelta()) {
         //                System.out.println("ERROR: Real: " + realScore + " calc: " + score.getDelta());
         //                Score s = calculateYMajorScore(testBuffer, null, null);
         //                System.out.println("Full Recalc: " + s.getDelta());
         //                calculateScore(testImage, previousScore, changedArea);
         //            }
         //        }
 
         return score;
 
     }
 
     private Score calculateYMajorScore(byte[] testBuffer, Score previousScore, Rectangle changedArea) {
 
         DefaultScore score = (previousScore != null) ? (DefaultScore)previousScore.clone() : new DefaultScore();
 
         boolean needAutoRecalc = (previousScore == null) || (changedArea == null);
         score.overallScore = 0L;
 
         for (int y = 0; y < DefaultScore.NUM_DIVISIONS; y++) {
             int gridHeight = (height / DefaultScore.NUM_DIVISIONS);
             int gridTop = y * gridHeight;
             int gridBottom;
             if (y < (DefaultScore.NUM_DIVISIONS - 1)) {
                 gridBottom = gridTop + gridHeight;
             } else {
                 gridBottom = height;
             }
 
             if (needAutoRecalc || ((changedArea.y <= gridBottom) && ((changedArea.y + changedArea.height) >= gridTop))) {
                 for (int x = 0; x < DefaultScore.NUM_DIVISIONS; x++) {
                     int gridWidth = (width / DefaultScore.NUM_DIVISIONS);
                     int gridLeft = x * gridWidth;
                     int gridRight;
                     if (x < (DefaultScore.NUM_DIVISIONS - 1)) {
                         gridRight = gridLeft + gridWidth;
                     } else {
                         gridRight = width;
                     }
 
                     if (needAutoRecalc || ((changedArea.x <= gridRight) && ((changedArea.x + changedArea.width) >= gridLeft))) {
 
                         long gridError = calculateGridScore(testBuffer, gridLeft, gridTop, gridRight, gridBottom);
 
                         score.gridScores[x][y] = gridError;
                         score.overallScore += gridError;
                     } else {
                         score.overallScore += score.gridScores[x][y];
                     }
                 }
             } else {
                 for (int x = 0; x < DefaultScore.NUM_DIVISIONS; x++) {
                     score.overallScore += score.gridScores[x][y];
                 }
             }
         }
 
         return score;
     }
 
     private Score calculateXMajorScore(byte[] testBuffer, Score previousScore, Rectangle changedArea) {
 
         DefaultScore score = (previousScore != null) ? (DefaultScore)previousScore.clone() : new DefaultScore();
 
         boolean needAutoRecalc = (previousScore == null) || (changedArea == null);
         score.overallScore = 0L;
 
         for (int x = 0; x < DefaultScore.NUM_DIVISIONS; x++) {
             int gridWidth = (width / DefaultScore.NUM_DIVISIONS);
             int gridLeft = x * gridWidth;
             int gridRight;
             if (x < (DefaultScore.NUM_DIVISIONS - 1)) {
                 gridRight = gridLeft + gridWidth;
             } else {
                 gridRight = width;
             }
 
             if (needAutoRecalc || ((changedArea.x <= gridRight) && ((changedArea.x + changedArea.width) >= gridLeft))) {
                 for (int y = 0; y < DefaultScore.NUM_DIVISIONS; y++) {
                     int gridHeight = (height / DefaultScore.NUM_DIVISIONS);
                     int gridTop = y * gridHeight;
                     int gridBottom;
                     if (y < (DefaultScore.NUM_DIVISIONS - 1)) {
                         gridBottom = gridTop + gridHeight;
                     } else {
                         gridBottom = height;
                     }
 
                     if (needAutoRecalc || ((changedArea.y <= gridBottom) && ((changedArea.y + changedArea.height) >= gridTop))) {
 
                         long gridError = calculateGridScore(testBuffer, gridLeft, gridTop, gridRight, gridBottom);
 
                         score.gridScores[x][y] = gridError;
                         score.overallScore += gridError;
                     } else {
                         score.overallScore += score.gridScores[x][y];
                     }
                 }
             } else {
                 for (int y = 0; y < DefaultScore.NUM_DIVISIONS; y++) {
                     score.overallScore += score.gridScores[x][y];
                 }
             }
         }
 
         return score;
     }
 
     private long calculateGridScore(byte[] testBuffer, int gridLeft, int gridTop, int gridRight, int gridBottom) {
         long gridError = 0L;
         for (int gy = gridTop; gy < gridBottom; gy++) {
             int pixelStart = (gy * width * 4) + (gridLeft * 4);
             int pixelEnd = pixelStart + ((gridRight - gridLeft) * 4);
 
             //index 0 is alpha, start at 1 (blue)
             for (int i = pixelStart + 1; i < pixelEnd; i++) {
                 int blue1 = targetBuffer[i] & 0x0FF;
                 int blue2 = testBuffer[i++] & 0x0FF;
                 long blueError = blue1 - blue2;
                 blueError *= blueError;
 
                 int green1 = targetBuffer[i] & 0x0FF;
                 int green2 = testBuffer[i++] & 0x0FF;
                 long greenError = green1 - green2;
                 greenError *= greenError;
 
                 int red1 = targetBuffer[i] & 0x0FF;
                 int red2 = testBuffer[i++] & 0x0FF;
                 long redError = red1 - red2;
                 redError *= redError;
 
                 gridError += redError + greenError  + blueError;
             }
         }
 
         return gridError;
     }
 }
