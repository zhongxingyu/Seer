 package p5_remote.utils;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.Raster;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
 import javax.imageio.stream.ImageOutputStream;
 
 import processing.core.PImage;
 
 public class PImageUtils {
 	public static byte [] toJpegByteArray(PImage pimage, int quality) {
 		if (pimage == null) return null;
 		if (quality <= 0 || 100 < quality) {
 			System.err.println("PImageUtils.toJpegByteArray(): quality parameter is between 0 and 100...");
 			return null;
 		}
 		
 		int w = pimage.width;
 		int h = pimage.height;
 		
 		byte [] jpeg_data = null;
 		
 		try {
 			BufferedImage buffered_image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();			
 			ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
 
 			for (int y = 0; y < h; y++) {
 				for (int x = 0; x < w; x++) {
 					buffered_image.setRGB(x, y, pimage.pixels[x + y * w]);
 				}
 			}
 
 			JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
 	        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
 	        param.setCompressionQuality(quality / 100.0f);
 	        ImageWriter iw = ImageIO.getImageWritersByFormatName("jpg").next();
 	        iw.setOutput(ios);
 	        iw.write(null, new IIOImage(buffered_image, null, null), param);		        
			jpeg_data = bos.toByteArray();
			
			iw.dispose(); // cause heap overflow, if forget to call dispose() method in Mac...
			ios.close();
			bos.close();
			buffered_image = null;
 		} catch (IOException e) {
 			System.err.println("PImageUtils.toJpegByteArray(): JPEG encode failed...");
 			e.printStackTrace();
 		}
 		
 		return jpeg_data;
 	}
 	
 	public static PImage toPImage(byte [] jpeg_bytes) {
 		if (jpeg_bytes == null) return null;
 		
 		ByteArrayInputStream bis = new ByteArrayInputStream(jpeg_bytes);
 		BufferedImage bufferd_img = null;
 
 		try {
 			bufferd_img = ImageIO.read(bis);
 			bis.close();
 		} catch (IOException e) {
 			System.err.println("PImageUtils.toPImage(): ImageIO.read() failed...");
 			e.printStackTrace();
 			return null;
 		}
 	
 		int w = bufferd_img.getWidth();
 		int h = bufferd_img.getHeight();
 		
 		Raster raster = bufferd_img.getRaster();
 		DataBufferByte data_buffer = (DataBufferByte) (raster.getDataBuffer());
 		byte[] buf = data_buffer.getData();
 		PImage pimage = new PImage(w, h, PImage.ARGB);
 
 		for (int y = 0; y < h; ++y) {
 			for (int x = 0; x < w; ++x) {
 				int buf_idx = x * 3 + y * w * 3;
 				byte b = buf[buf_idx + 0];
 				byte g = buf[buf_idx + 1];
 				byte r = buf[buf_idx + 2];
 
 				int c = (b << 0) & 0x000000ff | (g << 8) & 0x0000ff00
 						| (r << 16) & 0x00ff0000 | (0xff << 24) & 0xff000000;
 
 				int img_idx = x + y * w;
 				pimage.pixels[img_idx] = c;
 			}
 		}
 		pimage.updatePixels();
 		
 		return pimage;
 	}
 }
