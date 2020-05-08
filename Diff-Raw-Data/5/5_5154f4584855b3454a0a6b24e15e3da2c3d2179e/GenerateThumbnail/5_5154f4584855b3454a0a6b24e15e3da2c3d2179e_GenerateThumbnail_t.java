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
 package com.celements.photo.image;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBuffer;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.media.jai.PixelAccessor;
 import javax.media.jai.UnpackedImageData;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.celements.photo.container.ImageDimensions;
 import com.celements.photo.container.ImageLibStrings;
 import com.celements.photo.utilities.Util;
 import com.xpn.xwiki.XWikiException;
 
 /**
  * This class is used to generate a thumbnail. Until now it only works for jpeg
  * files.
  */
 public class GenerateThumbnail {
   
   private static final Log mLogger = LogFactory.getFactory().getInstance(GenerateThumbnail.class);
 
   /**
    * saveTypes : image types which can be preserved resizing the image
    */
   public static final Map<String, String> saveTypes = getSaveTypes();
 
   static Map<String, String> getSaveTypes() {
     HashMap<String, String> map = new HashMap<String, String>();
     map.put("gif", "GIF");
     map.put("image/gif", "GIF");
 //    map.put("jpg", "JPEG");
 //    map.put("jpeg", "JPEG");
 //    map.put("image/jpeg", "JPEG");
     map.put("png", "PNG");
     map.put("image/png", "PNG");
     return map;
   }
   public GenerateThumbnail(){}
     
   /**
    * Calculates an ImageDimensions object, containing the width and height of a
    * thumbnail, respecting the specified maximums (maintaining the aspect 
    * ratio of the original image).
    * 
    * @see com.celements.photo.plugin.container.ImageDimensions
    * @param in InputStream of the image.
    * @param maxWidth Maximum allowed width.
    * @param maxHeight Maximum allowed height.
    * @return ImageDimensions object specifying width and height.
    * @throws XWikiException 
    */
   public ImageDimensions getThumbnailDimensions(InputStream in, int maxWidth, int maxHeight) throws XWikiException{
     BufferedImage img = decodeImage(in);
     return getThumbnailDimensions(img, maxWidth, maxHeight);
   }
 
   /**
    * Calculates an ImageDimensions object, containing the width and height of a
    * thumbnail, respecting the specified maximums (maintaining the aspect 
    * ratio of the original image).
    * 
    * @see com.celements.photo.plugin.container.ImageDimensions
    * @param img BufferedImage representation of the image.
    * @param maxWidth Maximum allowed width.
    * @param maxHeight Maximum allowed height.
    * @return ImageDimensions object specifying width and height.
    */
   public ImageDimensions getThumbnailDimensions(BufferedImage img, int maxWidth, int maxHeight){
     int width = maxWidth;
     int height = maxHeight;
     if(img != null) {
       width = img.getWidth();
       height = img.getHeight();
     }
     mLogger.debug("img width=" + width + "; img height=" + height);
     return getThumbnailDimensions(width, height, maxWidth, maxHeight);
   }
 
   /**
    * Calculates an ImageDimensions object, containing the width and height of a
    * thumbnail, respecting the specified maximums (maintaining the aspect 
    * ratio of the original image).
    * Maximum values <= 0 for a dimension take the original image's value as 
    * maximum for that dimension.
    * 
    * @see com.celements.photo.plugin.container.ImageDimensions
    * @param imgWidth Width of the original image.
    * @param imgHeight Height of the original image.
    * @param maxWidth Maximum allowed width.
    * @param maxHeight Maximum allowed height.
    * @return ImageDimensions object specifying width and height.
    * 
    * TODO implement preserving aspect ratio!!! see commented test in GenerateThumbnailTest
    */
   public ImageDimensions getThumbnailDimensions(int imgWidth, int imgHeight, int maxWidth,
       int maxHeight) {
     int thumbWidth = imgWidth;
     int thumbHeight = imgHeight;
 
     if(maxWidth <= 0){ maxWidth = imgWidth; }
     if(maxHeight <= 0){ maxHeight = imgHeight; }
     
     double widthImgThumbRatio = imgWidth / (double)maxWidth;
     double heightImgThumbRatio = imgHeight / (double)maxHeight;
     
     if((widthImgThumbRatio >= 1.0) || (heightImgThumbRatio >= 1.0)){
       if(widthImgThumbRatio > heightImgThumbRatio){
         thumbWidth = maxWidth;
         thumbHeight = (int)(imgHeight / widthImgThumbRatio);
       } else{
         thumbHeight = maxHeight;
         thumbWidth = (int)(imgWidth / heightImgThumbRatio);
       }
     }
     
     return new ImageDimensions(thumbWidth, thumbHeight);
   }
 
   /**
    * Calculates the dimensions of the specified image.
    * 
    * @see com.celements.photo.plugin.container.ImageDimensions
    * @param in InputStream of the image.
    * @return ImageDimensions object containing width and height of the image.
    * @throws XWikiException 
    */
   public ImageDimensions getImageDimensions(InputStream in) throws XWikiException{
     BufferedImage img = decodeImage(in);
    if (img != null) {
      return new ImageDimensions(img.getWidth(), img.getHeight());
    }
    return null;
   }
 
   /**
    * Calculates the dimensions of the specified image.
    * 
    * @see com.celements.photo.plugin.container.ImageDimensions
    * @param img BufferedImage representation of the image.
    * @return ImageDimensions object containing width and height of the image.
    */
   public ImageDimensions getImageDimensions(BufferedImage img){
     return new ImageDimensions(img.getWidth(), img.getHeight());
   }
   
   /**
    * Decodes an InputStream of a jpg image and returns a BufferedImage.
    *
    * @param in InputStream of the image.
    * @return BufferedImage representation of the image.
    * @throws XWikiException 
    */
   public BufferedImage decodeInputStream(InputStream in) throws XWikiException{
     return decodeImage(in);
   }
   
   /**
    * Creates a thumbnail, reading from an InputStream and writing the result
    * to an OutputStream.
    * 
    * @param in InputStream of the original sized image.
    * @param out OutputStream of the thumbnail.
    * @param width Maximum width of the thumbnail (Aspect ratio maintained).
    * @param height Maximum height of the thumbnail (aspect ratio maintained).
    * @param watermark String to add as a watermark to the image.
    * @param copyright String to add as a copyright to the image.
    * @return An ImageSize object representing the size of the thumbnail.
    * @throws IOException
    * @throws XWikiException 
    */
   public ImageDimensions createThumbnail(InputStream in, OutputStream out, int width, 
       int height, String watermark, String copyright, String type, Color defaultBg) 
       throws IOException, XWikiException {
     return createThumbnail(decodeInputStream(in), out, width, height, watermark, 
         copyright, type, defaultBg);
   }
   
   /**
    * Creates a thumbnail, reading from an InputStream and writing the result
    * to an OutputStream.
    * 
    * @param in InputStream of the original sized image.
    * @param out OutputStream of the thumbnail.
    * @param dimensions Dimensions of the thumbnail (Aspect ratio maintained).
    * @param watermark String to add as a watermark to the image.
    * @param copyright String to add as a copyright to the image.
    * @return An ImageSize object representing the size of the thumbnail.
    * @throws IOException
    * @throws XWikiException 
    */
   public void createThumbnail(InputStream in, OutputStream out, 
       ImageDimensions dimensions, String watermark, String copyright, String type, 
       Color defaultBg) throws IOException, XWikiException {
     createThumbnail(decodeInputStream(in), out, dimensions, watermark, copyright, type,
         defaultBg);
   }
   
   /**
    * Creates a thumbnail from a BufferedImage and writes the result
    * to an OutputStream.
    * 
    * @param img BufferedImage representation of the original image.
    * @param out OutputStream of the thumbnail.
    * @param width Maximum width of the thumbnail (Aspect ratio maintained).
    * @param height Maximum height of the thumbnail (aspect ratio maintained).
    * @param watermark String to add as a watermark to the image.
    * @param copyright String to add as a copyright to the image.
    * @return An ImageSize object representing the size of the thumbnail.
    * @throws IOException
    */
   public ImageDimensions createThumbnail(BufferedImage img, OutputStream out, int width, 
       int height, String watermark, String copyright, String type, Color defaultBg) 
       throws IOException {
     ImageDimensions imgSize = getThumbnailDimensions(img, width, height);
     createThumbnail(img, out, imgSize, watermark, copyright, type, defaultBg);
     return imgSize;
   }
 
   public BufferedImage createThumbnail(BufferedImage img, OutputStream out, 
       ImageDimensions imgSize, String watermark, String copyright, String type, 
       Color defaultBg) {
     Image thumbImg = img; 
     // Only generates a thumbnail if the image is larger than the desired thumbnail.
     mLogger.debug("img: " + img + " - imgSize: " + imgSize);
     if((img.getWidth() > (int)imgSize.getWidth()) || (img.getHeight() > (int)imgSize.getHeight())){
       // The "-1" is used to resize maintaining the aspect ratio.
       thumbImg = img.getScaledInstance((int)imgSize.getWidth(), -1, Image.SCALE_SMOOTH);
     }
     BufferedImage buffThumb = convertImageToBufferedImage(thumbImg, watermark, copyright,
         defaultBg);
     encodeImage(out, buffThumb, img, type);
     return buffThumb;
   }
   
   /**
    * Encodes a BufferedImage to jpeg format and writes it to the specified
    * OutputStream.
    * 
    * @param out OutputStream to write the image to.
    * @param image BufferedImage of the image to encode.
    * @throws IOException
    */
   public void encodeImage(OutputStream out, BufferedImage image, BufferedImage fallback, 
       String type) {
     if(!saveTypes.containsKey(type.toLowerCase())) {
       mLogger.info("encodeImage: convert to png, because [" + type + "] is no saveType.");
       type = "png"; //default for all not jpeg or gif files
     }
     try {
       ImageIO.write(image, saveTypes.get(type.toLowerCase()), out);
     } catch (IOException ioe) {
       mLogger.error("Could not save image as [" + type + "]! " + ioe);
       try {
         ImageIO.write(fallback, saveTypes.get(type.toLowerCase()), out);
       } catch (IOException e) {
         mLogger.error("Could not save fallback image as [" + type + "]! " + e);
       }
     }
   }
   
   /*
    * Converts an Image to a BufferedImage and adds watermark and copyright. 
    * 
    * @param thumbImg The Image to convert.
    * @param watermark String to add as a watermark to the image.
    * @param copyright String to add as a copyright to the image.
    * @return The BufferedImage representation of the Image.
    */
   BufferedImage convertImageToBufferedImage(Image thumbImg, String watermark, 
       String copyright, Color defaultBg) {
     BufferedImage thumb = new BufferedImage(thumbImg.getWidth(null), 
         thumbImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
     Graphics2D g2d = thumb.createGraphics();
     if(defaultBg == null) {
       defaultBg = Color.WHITE;
     }
 //    g2d.setBackground(Color.WHITE); -> has no effect
     g2d.setColor(defaultBg);
     g2d.fillRect(0, 0, thumbImg.getWidth(null), thumbImg.getHeight(null));
     g2d.drawImage(thumbImg, 0, 0, null);
     
     if((watermark != null) && (!watermark.equals(""))){
       drawWatermark(watermark, g2d, thumb.getWidth(), thumb.getHeight());
     }
     
     if((copyright != null) && (!copyright.equals(""))){
       drawCopyright(copyright, g2d, thumb.getWidth(), thumb.getHeight());
     }
     mLogger.info("thumbDimensions: " + thumb.getHeight() + "x" + thumb.getWidth());
     return thumb;
   }
 
   /*
    * Draws the watermark onto the image.
    * 
    * @param watermark String to draw on the image.
    * @param g2d Graphics object of the image.
    */
   private void drawWatermark(String watermark, Graphics2D g2d, int width, int height) {
     //TODO implement 'secure' watermark (i.e. check if this algorithm secure or can it be easily reversed?)
     g2d.setColor(new Color(255, 255, 150));
     AlphaComposite transprency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f);
     g2d.setComposite(transprency);
     
     FontMetrics metrics = calcWatermarkFontSize(watermark, width, g2d);
     g2d.setFont(calcWatermarkFontSize(watermark, width, g2d).getFont());
     
     // rotate around image center
     double angle = (Math.sin(height/(double)width));
       g2d.rotate(-angle, width / 2.0, height / 2.0);
 
     g2d.drawString(
         watermark, 
         (width - metrics.stringWidth(watermark)) / 2, 
         ((height - metrics.getHeight()) / 2) + metrics.getAscent());
     
     // undo rotation for correct copyright positioning
     g2d.rotate(angle, width / 2.0, height / 2.0);
   }
 
   private FontMetrics calcWatermarkFontSize(String watermark, int width,
       Graphics2D g2d) {
     FontMetrics metrics;
     int fontSize = 1;
     do{
       metrics = g2d.getFontMetrics(new Font(g2d.getFont().getFontName(), Font.BOLD, fontSize));
       fontSize++;
     }while(metrics.stringWidth(watermark) < (0.8*width));
     return metrics;
   }
 
   /*
    * Draws the copyright information onto the image.
    * 
    * @param copyright String to draw on the image.
    * @param g2d Graphics object of the image.
    */
   private void drawCopyright(String copyright, Graphics2D g2d, int width, int height) {
     int bottomSpace = 5; //space between copyright and bottom border.
     int rightSpace = 5; //space between copyright and right border.
     int hSpacing = 3; //horizontal space between background and string.
     int vSpacing = 2; //vertical space between background and string.
     int rounding = 5; //rounding of the rect.
     
     FontMetrics metrics = calcCopyrightFontSize(copyright, width, g2d);
     g2d.setFont(metrics.getFont());
     int stringHeight = metrics.getHeight();
     
     drawBackground(copyright, width, height, bottomSpace, rightSpace, vSpacing,
         hSpacing, rounding, stringHeight, metrics, g2d);
     drawString(copyright, width, height, bottomSpace, rightSpace, vSpacing,
         hSpacing, metrics, g2d);
   }
 
   private FontMetrics calcCopyrightFontSize(String copyright, int width, Graphics2D g2d) {
     FontMetrics metrics;
     int fontSize = 16;
     do{
       metrics = g2d.getFontMetrics(new Font(g2d.getFont().getFontName(), Font.BOLD, fontSize));
       fontSize--;
     }while((fontSize > 0) && (metrics.stringWidth(copyright) > (width/3)));
     return metrics;
   }
 
   private void drawBackground(String copyright, int width, int height,
       int bottomSpace, int rightSpace, int vSpacing, int hSpacing,
       int rounding, int stringHeight, FontMetrics metrics, Graphics2D g2d) {
     g2d.setColor(new Color(0, 0, 0));
     AlphaComposite transprency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
     g2d.setComposite(transprency);
     g2d.fillRoundRect(
         width - metrics.stringWidth(copyright) - rightSpace - 2*hSpacing, 
         height - stringHeight - bottomSpace - 2*vSpacing, 
         metrics.stringWidth(copyright) + 2*hSpacing, 
         stringHeight + 2*vSpacing, 
         rounding, rounding);
   }
 
   private void drawString(String copyright, int width, int height,
       int bottomSpace, int rightSpace, int vSpacing, int hSpacing,
       FontMetrics metrics, Graphics2D g2d) {
     AlphaComposite transprency;
     g2d.setColor(new Color(255, 255, 255));
     transprency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.66f);
     g2d.setComposite(transprency);
     //Attention: drawString's [x, y] coordinates are the baseline, not upper or lower corner.
     g2d.drawString(
         copyright, 
         width - metrics.stringWidth(copyright) - hSpacing - rightSpace, 
         height - metrics.getDescent() - vSpacing - bottomSpace);
   }
   
   /**
    * Decodes a jpeg from an InputStream to a BufferedImage.
    * 
    * @param in InputStream representation of a jpeg image.
    * @return Decoded jpeg as BufferedImage.
    * @throws XWikiException 
    */
   public BufferedImage decodeImage(InputStream in) throws XWikiException {
     BufferedImage bufferedImage = null;
     try {
       bufferedImage = ImageIO.read(in);
     } catch (IOException e) {
       mLogger.error("Could not open the file! ", e);
       throw new XWikiException(XWikiException.MODULE_XWIKI_PLUGINS,
           XWikiException.ERROR_XWIKI_UNKNOWN, "Could not decode the image file.", e);
     }
     return bufferedImage;
   }
   
   /**
    * Get a hash code for an image. Only the image data is used in the 
    * calculation, the metainformation is excluded. 
    * The return value is formated as string of the hex codes of the original
    * hash code. Thus, each hash code character is represented by two 
    * characters, representing a hex code. This is done to be able to use the
    * hash in file paths, URLs, ...
    * 
    * The hashing is done using the SHA-256 algorithm. Thus the resulting hash
    * has a length of 256 bit (32 byte) i.e. after the hex conversion 64 byte.
    * 
    * @param in InputStream with the image to hash.
    * @return Hash code of the specified image, excluding metainformation.
    * @throws IOException
    * @throws XWikiException 
    */
   public String hashImage(InputStream in) throws IOException, XWikiException{
     BufferedImage img = decodeImage(in);
     
     String hash = "";
     try {
       hash = Util.getUtil().hashToHex(getHashOfImage(img));
     } catch (NoSuchAlgorithmException e) {
       mLogger.error(e);
     }
     
     return hash;
   }
 
   private String getHashOfImage(BufferedImage img)
       throws NoSuchAlgorithmException {
     MessageDigest digest = MessageDigest.getInstance(ImageLibStrings.HASHING_ALGORITHM);
     
     PixelAccessor pa = new PixelAccessor(img);
     UnpackedImageData uid = pa.getPixels(img.getData(), img.getData().getBounds(),
         DataBuffer.TYPE_BYTE, false);
     byte[][] pixels = uid.getByteData();
     for (int i = 0; i < pixels.length; i++) {
       digest.update(pixels[i]);
     }
     
     return new String(digest.digest());
   }
 }
