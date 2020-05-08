 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.photo.plugin.cmd;
 
 import java.awt.Color;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.ColorConvertOp;
 import java.awt.image.ConvolveOp;
 import java.awt.image.Kernel;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.celements.photo.container.ImageDimensions;
 import com.celements.photo.image.GenerateThumbnail;
 import com.celements.photo.image.ICropImage;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.web.Utils;
 
 public class ComputeImageCommand {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       ComputeImageCommand.class);
 
   private ImageCacheCommand imgCacheCmd;
   
   //TODO -> cache ALL images and convert them to e.g. from CMYK, exept if ?format=raw
   public XWikiAttachment computeImage(XWikiAttachment attachment,
       XWikiContext context, XWikiAttachment attachmentClone, String sheight,
       String swidth, String copyright, String watermark, Color defaultBg,
       String defaultBgStr, String filterStr) {
     // crop params
     int cropX = parseIntWithDefault(context.getRequest().get("cropX"), -1);
     int cropY = parseIntWithDefault(context.getRequest().get("cropY"), -1);
     int cropW = parseIntWithDefault(context.getRequest().get("cropW"), -1);
     int cropH = parseIntWithDefault(context.getRequest().get("cropH"), -1);
     boolean needsCropping = needsCropping(cropX, cropY, cropW, cropH);
     LOGGER.debug("Crop needed: " + needsCropping + " -> " + cropX + ":" + cropY + " " + 
         cropW + "x" + cropH);
     boolean blackAndWhite = parseIntWithDefault(context.getRequest().get("BnW"), 0) == 1;
     LOGGER.debug("Get image as Black & White: " + blackAndWhite);
     defaultBg = getBackgroundColour(defaultBg, defaultBgStr);
     boolean lowerBound = 1 == parseIntWithDefault(context.getRequest().get("lowBound"), 
         0);
     Integer lowerBoundPositioning = parseIntWithDefault(context.getRequest(
         ).get("lowBoundPos"), null);
     boolean raw = 1 == parseIntWithDefault(context.getRequest().get("raw"), 0);
     int height = parseIntWithDefault(sheight, 0);
     int width = parseIntWithDefault(swidth, 0);
     try {
       attachmentClone = (XWikiAttachment) attachment.clone();
 //      mLogger.debug("dimension: target width=" + width + "; target height=" + height
 //          + "; resized width=" + dimension.getWidth() + "; resized height="
 //          + dimension.getHeight());
       String key = getImageCacheCmd().getCacheKey(attachmentClone, new ImageDimensions(
           width, height), copyright, watermark, cropX, cropY, cropW, cropH, blackAndWhite,
           defaultBg, lowerBound, lowerBoundPositioning, filterStr, raw);
       LOGGER.debug("attachment key: '" + key + "'");
       InputStream data = getImageCacheCmd().getImageForKey(key);
       if (data != null) {
         LOGGER.info("Found image in Cache.");
         attachmentClone.setContent(data);
       } else {
         LOGGER.info("No cached image.");
         long timeLast = System.currentTimeMillis();
         if(!raw) {
           GenerateThumbnail thumbGen = new GenerateThumbnail();
           LOGGER.info("start loading image " + timeLast);
           DecodeImageCommand decodeImageCommand = new DecodeImageCommand();
           BufferedImage img = decodeImageCommand.readImage(attachmentClone, context);
           timeLast = logRuntime(timeLast, "image decoded after ");
           if(needsCropping) {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             ICropImage cropComp = Utils.getComponent(ICropImage.class);
             cropComp.crop(img, cropX, cropY, cropW, cropH, attachmentClone.getMimeType(
                 context), out);
             attachmentClone.setContent(new ByteArrayInputStream(
                 ((ByteArrayOutputStream)out).toByteArray()));
             img = decodeImageCommand.readImage(attachmentClone, context);
           }
           timeLast = logRuntime(timeLast, "image cropped after ");
           if ((height > 0) || (width > 0)) {
             ImageDimensions dimension = thumbGen.getThumbnailDimensions(img, width, 
                 height, lowerBound, defaultBg);
             timeLast = logRuntime(timeLast, "got image dimensions after ");
             byte[] thumbImageData = getThumbAttachment(img, dimension, thumbGen, 
                 attachmentClone.getMimeType(context), watermark, copyright, defaultBg,
                 lowerBound, lowerBoundPositioning);
             timeLast = logRuntime(timeLast, "resize done after ");
             attachmentClone.setContent(new ByteArrayInputStream(thumbImageData));
             timeLast = logRuntime(timeLast, "new attachment content set after ");
           }
           if(blackAndWhite) {
             img = decodeImageCommand.readImage(attachmentClone, context);
             ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);  
             ColorConvertOp op = new ColorConvertOp(cs, null);
             BufferedImage bNwImg = op.filter(img, null);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             thumbGen.encodeImage(out, bNwImg, img, attachmentClone.getMimeType(context));
             byte[] bNwImage = out.toByteArray();
             attachmentClone.setContent(new ByteArrayInputStream(bNwImage));
             timeLast = logRuntime(timeLast, "image changed to black & white after ");
           }
           //TODO prevent multiple de- and encoding
           if((filterStr != null) && !"".equals(filterStr)) {
             LOGGER.debug("Filter found [" + filterStr + "]");
             String[] filterParts = filterStr.split("[,;| ]+");
             LOGGER.debug("Filter definition has " + filterParts.length + " parts");
             if(filterParts.length > 2) {
               try {
                 int kerWidth = Integer.parseInt(filterParts[0]);
                 int kerHeight = Integer.parseInt(filterParts[1]);
                 float[] kerMatrix = new float[kerWidth*kerHeight];
                 for(int i = 0; i < kerWidth*kerHeight; i++) {
                   float x;
                   if(filterParts.length <= 4) {
                     x = Float.parseFloat(filterParts[2]);
                     if((filterParts.length == 4) && (i == ((kerWidth*kerHeight)-1)/2)) {
                       x = Float.parseFloat(filterParts[3]);
                     }
                   } else {
                     x = Float.parseFloat(filterParts[i+2]);
                   }
                   kerMatrix[i] = x;
                 }
                 img = decodeImageCommand.readImage(attachmentClone, context);
                 Kernel kernel = new Kernel(kerWidth, kerHeight, kerMatrix);
                 LOGGER.debug("Filtering with kernel configured as " + kerWidth + ", " 
                    + kerHeight + ", " + kerMatrix);
                 BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null); 
                 BufferedImage filteredImg = op.filter(img, null);
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 thumbGen.encodeImage(out, filteredImg, img, attachmentClone.getMimeType(
                     context));
                 attachmentClone.setContent(new ByteArrayInputStream(out.toByteArray()));
                 timeLast = logRuntime(timeLast, "applied kernel filter [" + filterStr 
                     + "] after ");
               } catch(NumberFormatException nfe) {
                 LOGGER.error("Exception parsing filter string [" + filterStr + "]", nfe);
               }
             }
           }
         } else {
           LOGGER.info("Raw image! No alterations done.");
         }
         getImageCacheCmd().addToCache(key, attachmentClone);
         timeLast = logRuntime(timeLast, "image in cache after ");
       }
     } catch (Exception exp) {
       LOGGER.error("Error, could not resize / cache image", exp);
       attachmentClone = attachment;
     }
     return attachmentClone;
   }
 
   Color getBackgroundColour(Color defaultBg, String defaultBgStr) {
     if((defaultBgStr != null) && defaultBgStr.matches(
         "[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?")) {
       int r = Integer.parseInt(defaultBgStr.substring(0, 2), 16);
       int g = Integer.parseInt(defaultBgStr.substring(2, 4), 16);
       int b = Integer.parseInt(defaultBgStr.substring(4, 6), 16);
       if(defaultBgStr.length() == 8) {
         defaultBg = new Color(r, g, b, Integer.parseInt(defaultBgStr.substring(6), 16));
       } else {
         defaultBg = new Color(r, g, b);
       }
     }
     return defaultBg;
   }
 
   long logRuntime(long timeLast, String message) {
     long timeNow = System.currentTimeMillis();
     LOGGER.info(message + (timeNow - timeLast) + " milliseconds " + "(time: " + timeNow 
         + ")");
     return timeNow;
   }
 
   private boolean needsCropping(int cropX, int cropY, int cropW, int cropH) {
     return (cropX >= 0) && (cropY >= 0) && (cropW > 0) && (cropH > 0);
   }
 
   Integer parseIntWithDefault(String stringValue, Integer defValue) {
     Integer parsedValue = defValue;
     if((stringValue != null) && (stringValue.length() > 0)) {
       try {
         parsedValue = Integer.parseInt(stringValue);
       } catch (NumberFormatException numExp) {
         LOGGER.debug("Failed to parse height [" + stringValue + "].", numExp);
       }
     }
     return parsedValue;
   }
 
   void injectImageCacheCmd(ImageCacheCommand mockImgCacheCmd) {
     imgCacheCmd = mockImgCacheCmd;
   }
 
   ImageCacheCommand getImageCacheCmd() {
     if (imgCacheCmd == null) {
       imgCacheCmd = new ImageCacheCommand();
     }
     return imgCacheCmd;
   }
 
   public void flushCache() {
     if (imgCacheCmd != null) {
       imgCacheCmd.flushCache();
       imgCacheCmd = null;
     }
   }
 
   private byte[] getThumbAttachment(BufferedImage img, ImageDimensions dim, 
       GenerateThumbnail thumbGen, String mimeType, String watermark, String copyright,
       Color defaultBg, boolean lowerBound, Integer lowerBoundPositioning) {
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     thumbGen.createThumbnail(img, out, dim, watermark, copyright, mimeType, defaultBg, 
         lowerBound, lowerBoundPositioning);
     return out.toByteArray();
   }
 
 }
