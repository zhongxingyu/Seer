 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.scheme.libraries;
 
 import java.awt.FileDialog;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 
 import java.io.*;
 import javax.imageio.*;
 
 import wombat.scheme.libraries.gui.ImageFrame;
 import wombat.scheme.libraries.types.ImageData;
 
 /**
  * Helper class to load, save, and display images using Java. 
  */
 public class ImageAPI {
 	private ImageAPI() {}
 	
 	/**
 	 * Show a dialog to allow the user to choose an image, then read it into a byte stream.
 	 * @param dir The directory to read from.
 	 * @return A stream of encoded data. The first two values are rows then columns, then the sequence is [r,g,b,a] across rows then down.
 	 * @throws IOException If we cannot read the file.
 	 */
 	public static ImageData readImage() throws IOException {
 		FileDialog fc = new FileDialog((java.awt.Frame) null, "read-image", FileDialog.LOAD);
         fc.setVisible(true);
         if (fc.getFile() == null)
         	throw new IllegalArgumentException("Error in read-image: no image chosen.");
 
         File file = new File(fc.getDirectory(), fc.getFile());
 		return readImage(null, file.getCanonicalPath());
 	}
 	
 	/**
 	 * Read an image into a bytestream.
 	 * @param dir The directory to read from.
 	 * @param filename The file to read.
 	 * @return A stream of encoded data. The first two values are rows then columns, then the sequence is [r,g,b,a] across rows then down.
 	 * @throws IOException If we cannot read the image.
 	 */
 	public static ImageData readImage(String dir, String filename) throws IOException {
 		if (!new File(filename).isAbsolute())
 			filename = new File(dir, filename).getCanonicalPath();
 		
 		if (!new File(filename).exists())
 			throw new IllegalArgumentException("Error in read-image: '" + filename + "' does not exist");
 		
 		BufferedImage bi_orig = ImageIO.read(new File(filename));
 		
		if (bi_orig == null) {
			String filetypes = "";
			for (String s : ImageIO.getReaderFileSuffixes())
				filetypes += s + " ";
			filetypes = filetypes.trim();
			
			throw new IllegalArgumentException("Error in read-image: Unable to read '" + filename + "'. Valid file types are: " + filetypes);
		}
			
 		// This is a fix to deal with getRGB screwing up on previously grayscale images
 		BufferedImage bi = new BufferedImage(bi_orig.getWidth(), bi_orig.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		bi.getGraphics().drawImage(bi_orig, 0, 0, null);
 		
 		int[] data = new int[bi.getWidth() * bi.getHeight()];
 		bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), data, 0, bi.getWidth());
 		
 		return new ImageData(bi.getWidth(), bi.getHeight(), data);
 	}
 	
 	/**
 	 * Write the image. Display a dialog for the filename.
 	 * @param dir The directory to read from.
 	 * @param data The image to write.
 	 * @throws IOException If we cannot write the image.
 	 */
 	public static void writeImage(Image img) throws IOException {
 		FileDialog fc = new FileDialog((java.awt.Frame) null, "write-image", FileDialog.SAVE);
         fc.setVisible(true);
 
         if (fc.getFile() == null)
         	throw new IllegalArgumentException("Error in read-image: no image chosen");
 
         File file = new File(fc.getDirectory(), fc.getFile());
 		
 		String[] parts = file.getName().split("\\.");
 		ImageIO.write((RenderedImage) img, parts[parts.length - 1], file);
 	}
 	
 	/**
 	 * Write the given [r,g,b,a] buffer to an image. Display a dialog for the filename.
 	 * @param dir The directory to read from.
 	 * @param data The image to write. The first two values are the width and height.
 	 * @throws IOException If we cannot write the image.
 	 */
 	public static void writeImage(ImageData img) throws IOException {
 		FileDialog fc = new FileDialog((java.awt.Frame) null, "write-image", FileDialog.SAVE);
         fc.setVisible(true);
 
         if (fc.getFile() == null)
         	throw new IllegalArgumentException("Error in read-image: no image chosen");
 
         File file = new File(fc.getDirectory(), fc.getFile());
         writeImage(null, img, file.getCanonicalPath());
 	}
 	
 	/**
 	 * Write the given [r,g,b,a] buffer to an image.
 	 * @param dir The directory to read from.
 	 * @param data The image to write. The first two values are the width and height.
 	 * @param filename The file to write to.
 	 * @throws IOException If we cannot write the image.
 	 */
 	public static void writeImage(String dir, ImageData img, String filename) throws IOException {
 		RenderedImage ri = new BufferedImage(img.Width, img.Height, BufferedImage.TYPE_INT_RGB);
 		((BufferedImage) ri).setRGB(0, 0, img.Width, img.Height, img.Data, 0, img.Width);
 
 		if (!new File(filename).isAbsolute())
 			filename = new File(dir, filename).getCanonicalPath();
 		
 		File file = new File(filename);
 		
 		String[] parts = file.getName().split("\\.");
 		ImageIO.write(ri, parts[parts.length - 1], file);
 	}
 	
 	/**
 	 * Display the given [r,g,b,a] buffer as an image.
 	 * @param data The image to write. The first two values are the width and height.
 	 * @throws IOException If we cannot write the image.
 	 */
 	public static void displayImage(ImageData img) {
 		final RenderedImage ri = new BufferedImage(img.Width, img.Height, BufferedImage.TYPE_INT_RGB);
 		((BufferedImage) ri).setRGB(0, 0, img.Width, img.Height, img.Data, 0, img.Width);
 		
 		// Create the basic frame.
 		new ImageFrame((Image) ri).setVisible(true);
 	}
 }
 
 
