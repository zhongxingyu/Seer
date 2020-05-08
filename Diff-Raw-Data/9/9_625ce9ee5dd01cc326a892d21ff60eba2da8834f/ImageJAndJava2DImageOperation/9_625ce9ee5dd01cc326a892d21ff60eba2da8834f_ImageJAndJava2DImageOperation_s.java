 package org.jahia.application;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * A combination of ImageJ and Java2D implementations, to expand the supported formats of Java2D.
  */
public class ImageJAndJava2DImageOperation extends Java2DBicubicImageOperation {
 
     private ImageJImageOperation imageJImageOperation = new ImageJImageOperation();
 
     public String getImplementationName() {
         return "ImageJAndJava2D";
     }
 
     @Override
     public Image getImage(File sourceFile) throws IOException {
         if (super.canRead(sourceFile)) {
             return super.getImage(sourceFile);
         } else {
             return imageJImageOperation.getImage(sourceFile);
         }
     }
 
     @Override
     public boolean resize(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
         ImageJImage imageJImage = (ImageJImage) image;
         if (imageJImage.isJava2DUsed()) {
             return super.resize(image, outputFile, newWidth, newHeight, resizeType);    //To change body of overridden methods use File | Settings | File Templates.
         } else {
             System.out.println("Using ImageJ code for file " + imageJImage.getPath() + "...");
             return imageJImageOperation.resize(image, outputFile, newWidth, newHeight, resizeType);
         }
     }
 
     @Override
     public boolean crop(Image image, File outputFile, int left, int top, int width, int height) throws IOException {
         ImageJImage imageJImage = (ImageJImage) image;
         if (imageJImage.isJava2DUsed()) {
             return super.crop(image, outputFile, left, top, width, height);    //To change body of overridden methods use File | Settings | File Templates.
         } else {
             System.out.println("Using ImageJ code for file " + image.getPath() + "...");
             return imageJImageOperation.crop(image, outputFile, left, top, width, height);
         }
     }
 
     @Override
     public boolean rotate(Image image, File outputFile, boolean clockwise) throws IOException {
         ImageJImage imageJImage = (ImageJImage) image;
         if (imageJImage.isJava2DUsed()) {
             return super.rotate(image, outputFile, clockwise);    //To change body of overridden methods use File | Settings | File Templates.
         } else {
             System.out.println("Using ImageJ code for file " + image.getPath() + "...");
             return imageJImageOperation.rotate(image, outputFile, clockwise);
         }
     }
 
 }
