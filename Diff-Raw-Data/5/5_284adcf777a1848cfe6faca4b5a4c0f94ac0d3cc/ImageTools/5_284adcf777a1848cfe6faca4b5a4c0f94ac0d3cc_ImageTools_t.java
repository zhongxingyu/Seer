 package cm.generic;
 
 /**
  * @author madhur
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 
 import java.io.*;
 import java.awt.image.*;
 import java.awt.*;
 import javax.imageio.*;
 import javax.imageio.stream.*;
 import java.util.*;
 import javax.imageio.plugins.jpeg.*;
 import com.sun.image.codec.jpeg.*;
 
 
 public class ImageTools 
 {
     // Reads the jpeg image in infile, compresses the image,
     // and writes it back out to outfile.
     // compressionQuality ranges between 0 and 1,
     // 0-lowest, 1-highest.
     public static void writeJpegFile(RenderedImage rendImage, File outfile, float compressionQuality) 
     {
         try 
         {
     
             // Find a jpeg writer
             ImageWriter writer = null;
             Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
             if (iter.hasNext()) {
                 writer = (ImageWriter)iter.next();
             }
     
             // Prepare output file
             ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
             writer.setOutput(ios);
     		
     		ImageTools imageTools	=	new ImageTools();
             // Set the compression quality
             ImageWriteParam iwparam = imageTools.new MyImageWriteParam();
             iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
             iwparam.setCompressionQuality(compressionQuality);
     
             // Write the image
             writer.write(null, new IIOImage(rendImage, null, null), iwparam);
     
             // Cleanup
             ios.flush();
             writer.dispose();
             ios.close();
         } 
         catch (Exception e) 
         {
         	e.printStackTrace();
         }
     }
     
     /**
     * given an image
     * it creates a thumbnail out of it
     */
 	public static void createThumbnail(BufferedImage image, String fileName)
 	{
 		final int THUMBNAIL_WIDTH 		= 245;
 		final int THUMBNAIL_HEIGHT		= 350;
 		final int THUMBNAIL_QUALITY	= 75;
 		/*
 	    // load image from INFILE
 	    Image image = Toolkit.getDefaultToolkit().getImage(args[0]);
 	    MediaTracker mediaTracker = new MediaTracker(new Frame());
 	    mediaTracker.addImage(image, 0);
 	    mediaTracker.waitForID(0);
 	    // determine thumbnail size from WIDTH and HEIGHT
 	    int thumbWidth = Integer.parseInt(args[2]);
 	    int thumbHeight = Integer.parseInt(args[3]);*/
 	    
 	    int thumbWidth 	= THUMBNAIL_WIDTH;
 	    int thumbHeight	= THUMBNAIL_HEIGHT;
 	    
 	    double thumbRatio = (double)thumbWidth / (double)thumbHeight;
 	    int imageWidth = image.getWidth(null);
 	    int imageHeight = image.getHeight(null);
 	    double imageRatio = (double)imageWidth / (double)imageHeight;
 	    
 	    if (thumbRatio < imageRatio) 
 	    {
 	      thumbHeight = (int)(thumbWidth / imageRatio);
 	    } 
 	    else 
 	    {
 	      thumbWidth = (int)(thumbHeight * imageRatio);
 	    }
 	    // draw original image to thumbnail image object and
 	    // scale it to the new size on-the-fly
 	    BufferedImage thumbImage = new BufferedImage(thumbWidth, 
 	      thumbHeight, BufferedImage.TYPE_INT_RGB);
 	      
 	    Graphics2D graphics2D = thumbImage.createGraphics();
 	    
 	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 	      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 	      
 	    graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
 	    
 	    // save thumbnail image to OUTFILE
 	    try
 	    {
		    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
//		    FileOutputStream(fileName + "_thumbnail" + ".jpg"));
		    
 		    
 		    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
 		      
 		    int quality =  THUMBNAIL_QUALITY;
 		    
 		    quality = Math.max(0, Math.min(quality, 100));
 		    param.setQuality((float)quality / 100.0f, false);
 		    encoder.setJPEGEncodeParam(param);
 		    
 	    	encoder.encode(thumbImage);
 	    }
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	    System.out.println("Done.");
 
   	}
 
     
     
     // This class overrides the setCompressionQuality() method to workaround
     // a problem in compressing JPEG images using the javax.imageio package.
     class MyImageWriteParam extends JPEGImageWriteParam 
     {
         public MyImageWriteParam() {
             super(Locale.getDefault());
         }
     
         // This method accepts quality levels between 0 (lowest) and 1 (highest) and simply converts
         // it to a range between 0 and 256; this is not a correct conversion algorithm.
         // However, a proper alternative is a lot more complicated.
         // This should do until the bug is fixed.
         public void setCompressionQuality(float quality) 
         {
             if (quality < 0.0F || quality > 1.0F) {
                 throw new IllegalArgumentException("Quality out-of-bounds!");
             }
             this.compressionQuality = 256 - (quality * 256);
         }
     }
 
 
 }
