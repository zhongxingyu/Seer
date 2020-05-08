 package com.idragon.adastra.context;
 
 import java.awt.Dimension;
 import java.awt.Image;
 
 
 /**
 * <p>Image sources can load images from various resources.</p>
  *
  * @author  iDragon
  */
 public interface ImageSource {
 
     /**
      * Get an image with its default size.
      *
      * @param   imageCode  Image code.
      *
      * @return  the image, or {@code null}, if the image doesn't exist.
      */
     Image getImage(String imageCode);
 
     /**
      * Get an image with the given dimensions. If the default image size is different, the image
      * will be resized.
      *
      * @param   imageCode  Image code.
      * @param   dimension  Dimension, or {@code null} to use the image's default size.
      *
      * @return  the image, or {@code null}, if the image doesn't exist.
      */
     Image getImage(String imageCode, Dimension dimension);
 
     /**
      * Get an image with the given size. If the default image size is different, the image will be
      * resized.
      *
      * @param   imageCode  Image code.
      * @param   width      Required image width.
      * @param   height     Required image height.
      *
      * @return  the image, or {@code null}, if the image doesn't exist.
      */
     Image getImage(String imageCode, int width, int height);
 }
