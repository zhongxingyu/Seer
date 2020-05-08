 package com.hachisoftware.ism;
 
 import java.awt.image.BufferedImage;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class ISMImageChunk extends Chunk {
 
 	private BufferedImage image;
 	private final int writeFormat;
 	
 	public ISMImageChunk(BufferedImage frame, int imageFormat) {
 		image = frame;
 		writeFormat = imageFormat;	
 		this.chuckID = "MIMG";
 	}
 
 	@Override
 	public void writeChunk(DataOutputStream data) throws IOException {
		ImageIO.write(image, (writeFormat == 1 ? "PNG" : "JPEG"), data);
 	}
 
 	@Override
 	public void readChunk(DataInputStream data) throws IOException {
		image = ImageIO.read(data);
 	}
 
 	public BufferedImage getISMImage() {
 		return image;
 	}
 
 }
