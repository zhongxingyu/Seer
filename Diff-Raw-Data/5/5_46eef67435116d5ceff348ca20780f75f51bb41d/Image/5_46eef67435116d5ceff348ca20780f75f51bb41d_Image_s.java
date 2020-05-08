 package ch.zhaw.headtracker.image;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 
 public final class Image {
 	private final byte[] pixels;
 	public final int width;
 	public final int height;
 
 	private Image(byte[] pixels, int width, int height) {
 		this.pixels = pixels;
 		this.width = width;
 		this.height = height;
 	}
 
 	public Image(int width, int height) {
 		this(new byte[width * height], width, height);
 	}
 
 	public Image(Image image) {
 		this(image.getData(), image.width, image.height);
 	}
 
 	public int getPixel(int x, int y) {
 		assert 0 <= x && x < width && 0 <= y && y < height;
 
 		return pixels[width * y + x] & 0xff;
 	}
 
 	public void setPixel(int x, int y, int value) {
 		assert 0 <= x && x < width && 0 <= y && y < height;
 		assert 0 <= value && value <= 0xff;
 
 		pixels[width * y + x] = (byte) value;
 	}
 
 	public void setPixel(int x, int y, boolean value) {
 		setPixel(x, y, value ? 0xff : 0);
 	}
 
 	public void invert() {
 		for (int iy = 0; iy < height; iy += 1)
 			for (int ix = 0; ix < width; ix += 1)
 				setPixel(ix, iy, ~getPixel(ix, iy) & 0xff);
 	}
 
 	// Add pixel values of other to this image's values
 	public void add(Image other) {
 		assert other.height == height && other.width == width;
 
 		for (int iy = 0; iy < height; iy += 1) {
 			for (int ix = 0; ix < width; ix += 1) {
 				int pixel = getPixel(ix, iy) + other.getPixel(ix, iy);
 
				setPixel(ix, iy, (byte) (pixel > 0xff ? 0xff : pixel));
 			}
 		}
 	}
 
 	// Subtract pixel values of other from this image's values
 	public void subtract(Image other) {
 		assert other.height == height && other.width == width;
 
 		for (int iy = 0; iy < height; iy += 1) {
 			for (int ix = 0; ix < width; ix += 1) {
 				int pixel = getPixel(ix, iy) - other.getPixel(ix, iy);
 
				setPixel(ix, iy, (byte) (pixel < 0 ? 0 : pixel));
 			}
 		}
 	}
 
 	public void multiply(Image other) {
 		assert other.height == height && other.width == width;
 
 		for(int y = 0; y < height; y += 1)
 			for(int x = 0; x < width; x += 1)
 				setPixel(x, y, getPixel(x, y) * other.getPixel(x, y) / 0xff);
 	}
 
 	@SuppressWarnings({ "InstanceVariableUsedBeforeInitialized" })
 	public byte[] getData() {
 		return Arrays.copyOf(pixels, pixels.length);
 	}
 
 	public static Image readFromStream(InputStream input, int width, int height) throws IOException {
 		int pos = 0;
 		byte[] pixels = new byte[width * height];
 
 		while (pos < width * height) {
 			int res = input.read(pixels, pos, width * height - pos);
 			
 			if (res < 0)
 				throw new IOException();
 			
 			pos += res;
 		}
 
 		return new Image(pixels, width, height);
 	}
 }
