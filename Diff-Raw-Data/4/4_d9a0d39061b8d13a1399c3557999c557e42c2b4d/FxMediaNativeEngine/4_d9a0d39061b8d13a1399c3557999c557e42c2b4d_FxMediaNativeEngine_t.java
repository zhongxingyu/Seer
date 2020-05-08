 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.media.impl;
 
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.media.FxMediaSelector;
 import com.flexive.shared.media.FxMetadata;
 import com.flexive.shared.stream.BinaryDownloadCallback;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.sanselan.ImageFormat;
 import org.apache.sanselan.ImageInfo;
 import org.apache.sanselan.ImageReadException;
 import org.apache.sanselan.Sanselan;
 import org.apache.sanselan.common.IImageMetadata;
 import org.apache.sanselan.common.ImageMetadata;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Java native Engine
  * This engine relies on java image io and apache sanselan
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FxMediaNativeEngine {
     private static final Log LOG = LogFactory.getLog(FxMediaNativeEngine.class);
 
     /**
      * Do we run in headless mode?
      */
     private static final boolean HEADLESS;
 
     static {
         if ("true".equals(System.getProperty("java.awt.headless"))) {
             HEADLESS = true;
         } else {
             // check if graphics environment is available
             boolean caughtException = false;
             try {
                 GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
             } catch (HeadlessException e) {
                 caughtException = true;
             }
             HEADLESS = caughtException;
         }
     }
 
     /**
      * Scale an image and return the dimensions (width and height) as int array
      *
      * @param original  original file
      * @param scaled    scaled file
      * @param extension extension
      * @param width     desired width
      * @param height    desired height
      * @return actual width ([0]) and height ([1]) of scaled image
      * @throws FxApplicationException on errors
      */
     public static int[] scale(File original, File scaled, String extension, int width, int height) throws FxApplicationException {
         if (HEADLESS && FxMediaImageMagickEngine.IM_AVAILABLE && ".GIF".equals(extension)) {
             //native headless engine can't handle gif transparency ... so if we have IM we use it, else
             //transparent pixels will be black
             return FxMediaImageMagickEngine.scale(original, scaled, extension, width, height);
         }
         BufferedImage bi;
         try {
             bi = ImageIO.read(original);
         } catch (Exception e) {
             LOG.info("Failed to read " + original.getName() + " using ImageIO, trying sanselan");
             try {
                 bi = Sanselan.getBufferedImage(original);
             } catch (Exception e1) {
                 throw new FxApplicationException(LOG, "ex.media.readFallback.error", original.getName(), extension, e.getMessage(), e1.getMessage());
             }
         }
         BufferedImage bi2 = scale(bi, width, height);
 
         String eMsg;
         boolean fallback;
         try {
             fallback = !ImageIO.write(bi2, extension.substring(1), scaled);
             eMsg = "No ImageIO writer found.";
         } catch (Exception e) {
             eMsg = e.getMessage();
             fallback = true;
         }
         if (fallback) {
             try {
                 Sanselan.writeImage(bi2, scaled, getImageFormatByExtension(extension), new HashMap());
             } catch (Exception e1) {
                 throw new FxApplicationException(LOG, "ex.media.write.error", scaled.getName(), extension,
                         eMsg + ", " + e1.getMessage());
             }
         }
         return new int[]{bi2.getWidth(), bi2.getHeight()};
     }
 
     public static BufferedImage scale(BufferedImage bi, int width, int height) {
         BufferedImage bi2;
         int scaleWidth = bi.getWidth(null);
         int scaleHeight = bi.getHeight(null);
         double scaleX = (double) width / scaleWidth;
         double scaleY = (double) height / scaleHeight;
         double scale = Math.min(scaleX, scaleY);
         scaleWidth = (int) ((double) scaleWidth * scale);
         scaleHeight = (int) ((double) scaleHeight * scale);
         Image scaledImage;
         if (HEADLESS) {
             // create a new buffered image, don't rely on a local graphics system (headless mode)
             final int type;
             if (bi.getType() != BufferedImage.TYPE_CUSTOM) {
                 type = bi.getType();
             } else if (bi.getAlphaRaster() != null) {
                 // alpha channel available
                 type = BufferedImage.TYPE_INT_ARGB;
             } else {
                 type = BufferedImage.TYPE_INT_RGB;
             }
             bi2 = new BufferedImage(scaleWidth, scaleHeight, type);
         } else {
             GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
             bi2 = gc.createCompatibleImage(scaleWidth, scaleHeight, bi.getTransparency());
         }
         Graphics2D g = bi2.createGraphics();
         if (scale < 0.3) {
             scaledImage = bi.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
             new ImageIcon(scaledImage).getImage();
             g.drawImage(scaledImage, 0, 0, scaleWidth, scaleHeight, null);
         } else {
             g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
             g.drawImage(bi, 0, 0, scaleWidth, scaleHeight, null);
         }
         g.dispose();
         return bi2;
     }
 
     /**
      * Get the image format based on the extension
      *
      * @param extension image file extension
      * @return ImageFormat (fallback to GIF if unknown)
      */
     public static ImageFormat getImageFormatByExtension(String extension) {
         ImageFormat iFormat;
         if (".BMP".equals(extension))
             iFormat = ImageFormat.IMAGE_FORMAT_BMP;
         else if (".TIF".equals(extension))
             iFormat = ImageFormat.IMAGE_FORMAT_TIFF;
         else if (".PNG".equals(extension))
             iFormat = ImageFormat.IMAGE_FORMAT_PNG;
         else
             iFormat = ImageFormat.IMAGE_FORMAT_GIF;
         return iFormat;
     }
 
     /**
      * Get the image format based on the mime type
      *
      * @param mimeType image mime type
      * @return ImageFormat (fallback to GIF if unknown)
      */
     public static ImageFormat getImageFormatByMimeType(String mimeType) {
         String mime = mimeType == null ? "image/gif" : mimeType.toLowerCase();
         ImageFormat iFormat;
         if ("image/jpeg".equals(mimeType))
             return ImageFormat.IMAGE_FORMAT_JPEG;
         else if ("image/jpg".equals(mimeType))
             return ImageFormat.IMAGE_FORMAT_JPEG;
         else if ("image/gif".equals(mimeType))
             return ImageFormat.IMAGE_FORMAT_GIF;
         else if ("image/png".equals(mimeType))
             return ImageFormat.IMAGE_FORMAT_PNG;
         else if ("image/bmp".equals(mimeType))
             return ImageFormat.IMAGE_FORMAT_PNG;
         else if ("image/tif".equals(mimeType))
             return ImageFormat.IMAGE_FORMAT_TIFF;
         else if ("image/tiff".equals(mimeType))
             return ImageFormat.IMAGE_FORMAT_TIFF;
         else
             return ImageFormat.IMAGE_FORMAT_GIF;
     }
 
     /**
      * Identify a file, returning metadata
      *
      * @param mimeType if not null it will be used to call the correct identify routine
      * @param file     the file to identify
      * @return metadata
      * @throws FxApplicationException on errors
      */
     public static FxMetadata identify(String mimeType, File file) throws FxApplicationException {
         try {
             ImageInfo sii = Sanselan.getImageInfo(file);
             IImageMetadata md = Sanselan.getMetadata(file);
             List<FxMetadata.FxMetadataItem> metaItems;
             if (md == null || md.getItems() == null)
                 metaItems = new ArrayList<FxMetadata.FxMetadataItem>(0);
             else {
                 metaItems = new ArrayList<FxMetadata.FxMetadataItem>(md.getItems().size());
                 for (Object o : md.getItems()) {
                     if (o instanceof ImageMetadata.Item) {
                         ImageMetadata.Item mdi = (ImageMetadata.Item) o;
                         if (!"Unknown".equals(mdi.getKeyword()))
                             metaItems.add(new FxMetadata.FxMetadataItem(mdi.getKeyword(), parseText(mdi.getText())));
                     }
                 }
             }
             return new FxImageMetadataImpl(mimeType, file.getName(), metaItems, sii.getWidth(), sii.getHeight(),
                     sii.getFormat().name, sii.getFormatName(), sii.getCompressionAlgorithm(), sii.getPhysicalWidthDpi(),
                    sii.getPhysicalHeightDpi(), sii.getColorTypeDescription(), sii.usesPalette(), sii.getBitsPerPixel(),
                    sii.isProgressive(), sii.isTransparent(), Sanselan.getICCProfile(file));
         } catch (Exception e) {
             throw new FxApplicationException("ex.media.identify.error", (file == null ? "unknown" : file.getName()),
                     mimeType, e.getMessage());
         }
     }
 
     /**
      * Manipulate image raw data and stream them back
      *
      * @param data     raw image data
      * @param out      stream
      * @param callback optional callback to set mimetype and size
      * @param mimeType mimetype
      * @param selector operations to apply
      * @throws FxApplicationException on errors
      */
     @SuppressWarnings({"UnusedAssignment"})
     public static void streamingManipulate(byte[] data, OutputStream out, BinaryDownloadCallback callback, String mimeType, FxMediaSelector selector) throws FxApplicationException {
         try {
             ImageFormat format = FxMediaNativeEngine.getImageFormatByMimeType(mimeType);
 
             BufferedImage bufferedImage;
 
             if (format == ImageFormat.IMAGE_FORMAT_JPEG) {
                 //need ImageIO for jpegs
                 ByteArrayInputStream bis = new ByteArrayInputStream(data);
                 bufferedImage = ImageIO.read(bis);
                 bis = null;
             } else {
                 try {
                     bufferedImage = Sanselan.getBufferedImage(data);
                 } catch (ImageReadException e) {
                     //might not be supported, try ImageIO
                     ByteArrayInputStream bis = new ByteArrayInputStream(data);
                     bufferedImage = ImageIO.read(bis);
                     bis = null;
                 }
             }
 
             //perform requested operations
             if (selector.isCrop())
                 bufferedImage = bufferedImage.getSubimage(
                         (int) selector.getCrop().getX(),
                         (int) selector.getCrop().getY(),
                         (int) selector.getCrop().getWidth(),
                         (int) selector.getCrop().getHeight());
             if (selector.isFlipHorizontal())
                 bufferedImage = flipHorizontal(bufferedImage);
             if (selector.isFlipVertical())
                 bufferedImage = flipVertical(bufferedImage);
             if (selector.isScaleHeight() || selector.isScaleWidth())
                 bufferedImage = scale(bufferedImage, selector.isScaleWidth() ? selector.getScaleWidth() : bufferedImage.getWidth(),
                         selector.isScaleHeight() ? selector.getScaleHeight() : bufferedImage.getHeight());
             if (selector.getRotationAngle() != 0)
                 bufferedImage = rotate(bufferedImage, selector.getRotationAngle());
 
             if (callback != null) {
                 ByteArrayOutputStream _out = new ByteArrayOutputStream(data.length);
                 boolean fallback = false;
                 try {
                     fallback = !ImageIO.write(bufferedImage, format.extension, _out);
                 } catch (Exception e) {
                     fallback = true;
                 }
                 if (fallback)
                     Sanselan.writeImage(bufferedImage, _out, format, new HashMap(0));
                 byte[] _newData = _out.toByteArray();
                 ImageInfo imageInfo = Sanselan.getImageInfo(_newData);
                 callback.setMimeType(imageInfo.getMimeType());
                 callback.setBinarySize(_newData.length);
                 out.write(_newData);
             } else {
                 Sanselan.writeImage(bufferedImage, out, format, new HashMap(0));
             }
         } catch (Exception e) {
             throw new FxApplicationException(e, "ex.media.manipulate.error", e.getMessage());
         }
     }
 
     /**
      * Rotate an image using the requested angle
      *
      * @param bufferedImage imeg to rotate
      * @param angle         angle to rotate
      * @return BufferedImage containing the rotation
      */
     @SuppressWarnings({"UnusedAssignment"})
     private static BufferedImage rotate(BufferedImage bufferedImage, int angle) {
         angle = angle % 360;
         if (angle == 0)
             return bufferedImage;
         if (angle < 0)
             angle += 360;
         switch (angle) {
             case 90:
                 BufferedImageOp rot90 = new
                         AffineTransformOp(AffineTransform.getRotateInstance(Math.PI / 2.0, bufferedImage.getHeight() / 2.0,
                         bufferedImage.getHeight() / 2.0), AffineTransformOp.TYPE_BILINEAR);
                 BufferedImage img90 = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType());
                 return rot90.filter(bufferedImage, img90);
             case 180:
                 BufferedImageOp rot180 = new
                         AffineTransformOp(AffineTransform.getRotateInstance(Math.PI, bufferedImage.getWidth() / 2.0,
                         bufferedImage.getHeight() / 2.0), AffineTransformOp.TYPE_BILINEAR);
                 BufferedImage img180 = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
                 return rot180.filter(bufferedImage, img180);
             case 270:
                 BufferedImageOp rot270 = new
                         AffineTransformOp(AffineTransform.getRotateInstance(-Math.PI / 2.0, bufferedImage.getWidth() / 2.0,
                         bufferedImage.getWidth() / 2.0), AffineTransformOp.TYPE_BILINEAR);
                 BufferedImage img270 = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType());
                 return rot270.filter(bufferedImage, img270);
             default:
                 //rotate using a non-standard angle (have to draw a box around the image that can fit it)
                 int box = (int) Math.sqrt(bufferedImage.getHeight() * bufferedImage.getHeight() + bufferedImage.getWidth() * bufferedImage.getWidth());
                 BufferedImage imgFree = new BufferedImage(box, box, bufferedImage.getType());
                 BufferedImage imgRet = new BufferedImage(box, box, bufferedImage.getType());
                 Graphics2D g = imgFree.createGraphics();
                 if (bufferedImage.getTransparency() == Transparency.OPAQUE) {
                     //draw a white background on opaque images since they dont support transparency
                     g.setBackground(Color.WHITE);
                     g.clearRect(0, 0, box, box);
                     Graphics2D gr = imgRet.createGraphics();
                     gr.setBackground(Color.WHITE);
                     gr.clearRect(0, 0, box, box);
                     gr = null;
                 }
                 g.drawImage(bufferedImage, box / 2 - bufferedImage.getWidth() / 2, box / 2 - bufferedImage.getHeight() / 2,
                         bufferedImage.getWidth(), bufferedImage.getHeight(), null);
                 g = null;
                 AffineTransform at = new AffineTransform();
                 at.rotate(angle * Math.PI / 180.0, box / 2.0, box / 2.0);
                 BufferedImageOp bio;
                 bio = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                 return bio.filter(imgFree, imgRet);
         }
     }
 
     /**
      * Flip the image horizontal
      *
      * @param bufferedImage original image
      * @return horizontally flipped image
      */
     private static BufferedImage flipHorizontal(BufferedImage bufferedImage) {
         AffineTransform at = AffineTransform.getTranslateInstance(bufferedImage.getWidth(), 0);
         at.scale(-1.0, 1.0);
         BufferedImageOp biOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
         BufferedImage imgRes = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
         return biOp.filter(bufferedImage, imgRes);
     }
 
     /**
      * Flip the image horizontal
      *
      * @param bufferedImage original image
      * @return horizontally flipped image
      */
     private static BufferedImage flipVertical(BufferedImage bufferedImage) {
         AffineTransform at = AffineTransform.getTranslateInstance(0, bufferedImage.getHeight());
         at.scale(1.0, -1.0);
         BufferedImageOp biOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
         BufferedImage imgRes = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
         return biOp.filter(bufferedImage, imgRes);
     }
 
     /**
      * Detect the mimetype of a file based on the first n bytes and the filename
      *
      * @param header   first n bytes of the file to examine
      * @param fileName filename
      * @return detected mimetype
      */
     public static String detectMimeType(byte[] header, String fileName) {
         if (header != null && header.length > 5) {
             try {
                 ImageFormat iformat = Sanselan.guessFormat(header);
                 if (iformat.actual) {
                     return "image/" + iformat.extension.toLowerCase();
                 }
             } catch (Exception e) {
                 LOG.error(e);
             }
         }
         if (!StringUtils.isEmpty(fileName) && fileName.indexOf('.') > 0) {
             //extension based detection
             fileName = fileName.trim().toUpperCase();
             if (fileName.endsWith(".JPG"))
                 return "image/jpeg";
             if (fileName.endsWith(".GIF"))
                 return "image/gif";
             if (fileName.endsWith(".PNG"))
                 return "image/png";
             if (fileName.endsWith(".BMP"))
                 return "image/bmp";
             if (fileName.endsWith(".TIF"))
                 return "image/tiff";
             if (fileName.endsWith(".DOC") || fileName.endsWith(".DOCX"))
                 return "application/msword";
             if (fileName.endsWith(".XLS") || fileName.endsWith(".XLSX"))
                 return "application/msexcel";
             if (fileName.endsWith(".PPT") || fileName.endsWith(".PPTX"))
                 return "application/mspowerpoint";
             if (fileName.endsWith(".PDF"))
                 return "application/pdf";
             if (fileName.endsWith(".HTM"))
                 return "text/html";
             if (fileName.endsWith(".HTML"))
                 return "text/html";
             if (fileName.endsWith(".TXT"))
                 return "text/plain";
             if (fileName.endsWith(".ICO"))
                 return "image/vnd.microsoft.icon";
         }
         //byte signature based detection
         if (header != null && header.length > 5 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) { //PNG
             return "image/png";
         }
         return "application/unknown";
     }
 
     /**
      * Filter out '' from strings and avoid null-Strings
      *
      * @param text text to parse
      * @return filtered text
      */
     private static String parseText(String text) {
         if (StringUtils.isEmpty(text))
             return "";
         if (text.startsWith("'") && text.endsWith("'"))
             return text.substring(1, text.length() - 1);
         return text;
     }
 }
