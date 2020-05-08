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
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
import java.io.OutputStream;
 
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
 
   public XWikiAttachment computeImage(XWikiAttachment attachment,
       XWikiContext context, XWikiAttachment attachmentClone, String sheight,
       String swidth, String copyright, String watermark, Color defaultBg,
       String defaultBgString) {
     // crop params
     int cropX = parseIntWithDefault(context.getRequest().get("cropX"), -1);
     int cropY = parseIntWithDefault(context.getRequest().get("cropY"), -1);
     int cropW = parseIntWithDefault(context.getRequest().get("cropW"), -1);
     int cropH = parseIntWithDefault(context.getRequest().get("cropH"), -1);
     boolean needsCropping = needsCropping(cropX, cropY, cropW, cropH);
     LOGGER.debug("Crop needed: " + needsCropping + " -> " + cropX + ":" + cropY + " " + 
         cropW + "x" + cropH);
     // resize params
     if((defaultBgString != null) && defaultBgString.matches("[0-9A-Fa-f]{6}")) {
       int r = Integer.parseInt(defaultBgString.substring(1, 3), 16);
       int g = Integer.parseInt(defaultBgString.substring(3, 5), 16);
       int b = Integer.parseInt(defaultBgString.substring(5), 16);
       defaultBg = new Color(r, g, b);
     }
     int height = parseIntWithDefault(sheight, 0);
     int width = parseIntWithDefault(swidth, 0);
     try {
       attachmentClone = (XWikiAttachment) attachment.clone();
 //      mLogger.debug("dimension: target width=" + width + "; target height=" + height
 //          + "; resized width=" + dimension.getWidth() + "; resized height="
 //          + dimension.getHeight());
       String key = getImageCacheCmd().getCacheKey(attachmentClone, new ImageDimensions(
           width, height), copyright, watermark, cropX, cropY, cropW, cropH);
       LOGGER.debug("attachment key: '" + key + "'");
       InputStream data = getImageCacheCmd().getImageForKey(key);
       if (data != null) {
         LOGGER.info("Found image in Cache.");
         attachmentClone.setContent(data);
       } else {
         LOGGER.info("No cached image.");
         GenerateThumbnail thumbGen = new GenerateThumbnail();
         InputStream in = attachmentClone.getContentInputStream(context);
         BufferedImage img = thumbGen.decodeImage(in);
         in.close();
        ImageDimensions dimension = thumbGen.getThumbnailDimensions(img, width, height);
         if(needsCropping) {
           ByteArrayOutputStream out = new ByteArrayOutputStream();
           ICropImage cropComp = Utils.getComponent(ICropImage.class);
           cropComp.crop(img, cropX, cropY, cropW, cropH, attachmentClone.getMimeType(
               context), out);
           attachmentClone.setContent(new ByteArrayInputStream(((ByteArrayOutputStream)out
               ).toByteArray()));
         }
         if ((height > 0) || (width > 0)) {
           byte[] thumbImageData = getThumbAttachment(img, dimension, thumbGen, 
               attachmentClone.getMimeType(context), watermark, copyright, defaultBg);
           attachmentClone.setContent(new ByteArrayInputStream(thumbImageData));
         }
         getImageCacheCmd().addToCache(key, attachmentClone);
       }
     } catch (Exception exp) {
       LOGGER.error("Error, could not resize / cache image", exp);
       attachmentClone = attachment;
     }
     return attachmentClone;
   }
 
   private boolean needsCropping(int cropX, int cropY, int cropW, int cropH) {
     return (cropX >= 0) && (cropY >= 0) && (cropW > 0) && (cropH > 0);
   }
 
   int parseIntWithDefault(String stringValue, int defValue) {
     int parsedValue = defValue;
     if ((stringValue != null) && (stringValue.length() > 0)) {
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
       Color defaultBg) {
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     thumbGen.createThumbnail(img, out, dim, watermark, copyright, 
         mimeType, defaultBg);
     return out.toByteArray();
   }
 
 }
