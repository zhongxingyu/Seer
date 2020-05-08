 package org.jahia.application;
 
 import org.apache.commons.io.FilenameUtils;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.FileImageOutputStream;
 import javax.imageio.stream.ImageOutputStream;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.IndexColorModel;
 import java.io.File;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 /**
  * Abstract Java2D common image operation implementations
  */
 public abstract class AbstractJava2DImageOperation extends AbstractImageOperation {
 
     public boolean isAvailable() {
         return true;
     }
 
     protected abstract Graphics2D getGraphics2D(BufferedImage bufferedImage);
 
     protected boolean canRead(File sourceFile) {
         String sourceFileExtension = FilenameUtils.getExtension(sourceFile.getPath());
         Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersBySuffix(sourceFileExtension.toLowerCase());
         if (imageReaderIterator.hasNext()) {
             return true;
         } else {
             return false;
         }
     }
 
     protected void saveImageToFile(BufferedImage dest, File destFile) throws IOException {
         String fileExtension = FilenameUtils.getExtension(destFile.getPath()).toLowerCase();
         Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersBySuffix(fileExtension);
         if (suffixWriters.hasNext()) {
             ImageWriter imageWriter = suffixWriters.next();
             ImageOutputStream imageOutputStream = new FileImageOutputStream(destFile);
             imageWriter.setOutput(imageOutputStream);
             imageWriter.write(dest);
             imageOutputStream.close();
         } else {
             System.err.println("Couldn't find a writer for extension : " + fileExtension + "(" + this.getClass().getName() + ")");
         }
     }
 
     public ResizeType[] getSupportedResizeTypes() {
         return new ResizeType[]{
                 ResizeType.ADJUST_SIZE,
                 ResizeType.SCALE_TO_FILL,
                 ResizeType.ASPECT_FILL,
                 ResizeType.ASPECT_FIT
         };
     }
 
     public Image getImage(File sourceFile) throws IOException {
         if (!canRead(sourceFile)) {
             System.err.println("Image reading for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
             return null;
         }
         // Read image to scale
         BufferedImage originalImage = ImageIO.read(sourceFile);
 
         return new ImageJImage(sourceFile.getPath(), null, 0, originalImage, null, true);
     }
 
     public boolean resize(Image image, File outputFile, int newWidth, int newHeight, AbstractImageOperation.ResizeType resizeType) throws IOException {
 
         BufferedImage originalImage = ((ImageJImage) image).getOriginalImage();
 
         ResizeCoords resizeCoords = getResizeCoords(resizeType, originalImage.getWidth(), originalImage.getHeight(), newWidth, newHeight);
         if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
             newWidth = resizeCoords.getTargetWidth();
             newHeight = resizeCoords.getTargetHeight();
         }
 
         BufferedImage dest = getDestImage(newWidth, newHeight, originalImage);
 
         // Paint source image into the destination, scaling as needed
         Graphics2D graphics2D = getGraphics2D(dest);
 
         graphics2D.drawImage(originalImage,
                 resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                 resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                 resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                 resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                 null);
         graphics2D.dispose();
 
         // Save destination image
         saveImageToFile(dest, outputFile);
 
         return true;
     }
 
     protected BufferedImage getDestImage(int newWidth, int newHeight, BufferedImage originalImage) {
         BufferedImage dest;
         if (originalImage.getColorModel() instanceof IndexColorModel) {
             // dest = new BufferedImage(newWidth, newHeight, originalImage.getType(), (IndexColorModel) originalImage.getColorModel());
             dest = new BufferedImage(originalImage.getColorModel(), originalImage.getColorModel().createCompatibleWritableRaster(newWidth, newHeight), false, new Hashtable<Object, Object>());
         } else {
             dest = new BufferedImage(newWidth, newHeight, originalImage.getType());
         }
         return dest;
     }
 
     public boolean crop(Image image, File outputFile, int left, int top, int width, int height) throws IOException {
 
         BufferedImage originalImage = ((ImageJImage) image).getOriginalImage();
 
         int clippingWidth = width;
         if (left + clippingWidth > originalImage.getWidth()) {
             clippingWidth = originalImage.getWidth() - left;
         }
         int clippingHeight = height;
         if (top + clippingHeight > originalImage.getHeight()) {
             clippingHeight = originalImage.getHeight() - top;
         }
         BufferedImage clipping = getDestImage(clippingWidth, clippingHeight, originalImage);
         Graphics2D area = getGraphics2D(clipping);
         area.drawImage(originalImage, 0, 0, clippingWidth, clippingHeight, left, top, left + clippingWidth,
                 top + clippingHeight, null);
         area.dispose();
 
         // Save destination image
         saveImageToFile(clipping, outputFile);
 
         return true;
     }
 
     public boolean rotate(Image image, File outputFile, boolean clockwise) throws IOException {
 
         BufferedImage originalImage = ((ImageJImage) image).getOriginalImage();
 
         BufferedImage dest = getDestImage(originalImage.getHeight(), originalImage.getWidth(), originalImage);
         // Paint source image into the destination, scaling as needed
         Graphics2D graphics2D = getGraphics2D(dest);
 
         double angle = Math.toRadians(clockwise ? 90 : -90);
         double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
         int w = originalImage.getWidth(), h = originalImage.getHeight();
         int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
         graphics2D.translate((neww - w) / 2, (newh - h) / 2);
         graphics2D.rotate(angle, w / 2, h / 2);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
         if (originalImage.getColorModel() instanceof IndexColorModel) {
             graphics2D.drawImage(originalImage, 0, 0, graphics2D.getBackground(), null);
         } else {
             graphics2D.drawImage(originalImage, 0, 0, null);
         }
 
         // Save destination image
         saveImageToFile(dest, outputFile);
         return true;
     }
 
 }
