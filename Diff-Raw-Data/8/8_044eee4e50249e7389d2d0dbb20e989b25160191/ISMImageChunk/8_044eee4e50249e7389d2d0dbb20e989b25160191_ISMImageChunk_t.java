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
		byte[] imageBytes = ((DataBufferByte) image.getData().getDataBuffer()).getData();
		data.write(imageBytes, 0, imageBytes.length);
 	}
 
 	@Override
 	public void readChunk(DataInputStream data) throws IOException {
		byte[] buffer = new byte[8192 * 1024];
		data.read(buffer);
		image = ImageIO.read(new ByteArrayInputStream(buffer));
 	}
 
 	public BufferedImage getISMImage() {
 		return image;
 	}
 
 }
