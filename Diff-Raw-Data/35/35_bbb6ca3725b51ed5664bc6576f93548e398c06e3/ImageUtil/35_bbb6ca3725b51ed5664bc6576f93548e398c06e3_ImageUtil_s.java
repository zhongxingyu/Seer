 package com.abbajoa.thumbnailmirror;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
 import javax.imageio.stream.FileImageOutputStream;
 
 import com.mortennobel.imagescaling.DimensionConstrain;
 import com.mortennobel.imagescaling.ResampleOp;
 
 public class ImageUtil {
 
 	public static void writeJpeg(RenderedImage image, IIOMetadata metadata, float quality, File dest) throws FileNotFoundException, IOException {
 		ImageWriter writer = null;
 		FileImageOutputStream output = null;
 		try {
 			writer = ImageIO.getImageWritersByFormatName("jpeg").next();
 			ImageWriteParam param = writer.getDefaultWriteParam();
 			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
 			param.setCompressionQuality(quality);
 			output = new FileImageOutputStream(dest);
 			writer.setOutput(output);
 			IIOImage iioImage = new IIOImage(image, null, metadata);
 			writer.write(null, iioImage, param);
 		} finally {
 			if (writer != null)
 				writer.dispose();
 			if (output != null)
 				output.close();
 		}
 	}
 
 	public static BufferedImage getRescaled(BufferedImage img, int maxBoxSize) {
 		ResampleOp  resampleOp = new ResampleOp (DimensionConstrain.createMaxDimension(maxBoxSize, maxBoxSize));
 		return resampleOp.filter(img, null);
 	}
 
	public static ImageReader createJpegReader(File src) throws FileNotFoundException, IOException {
		ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
		reader.setInput(new FileImageInputStream(src));
		return reader;
 	}
 
 	public static BufferedImage readImage(ImageReader reader) throws IOException {
 		return reader.read(0);
 	}
 
 	public static IIOMetadata readJpegExifTags(ImageReader reader) throws IOException {
 		return reader.getImageMetadata(0);
 	}
 
 }
