 /*
  * Copyright (c) 2008 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.eandb.jmist.framework.display;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Serializable;
 
 import ca.eandb.jdcp.JdcpUtil;
 import ca.eandb.jdcp.job.HostService;
 import ca.eandb.jmist.framework.Display;
 import ca.eandb.jmist.framework.Raster;
 import ca.eandb.jmist.framework.color.CIEXYZ;
 import ca.eandb.jmist.framework.color.Color;
 import ca.eandb.jmist.framework.color.ColorModel;
 import ca.eandb.jmist.util.matlab.MatlabWriter;
 import ca.eandb.util.UnexpectedException;
 
 /**
  * @author brad
  *
  */
 public final class MatlabXYZFileDisplay implements Display, Serializable {
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 4433336367833663738L;
 
 	private static final String DEFAULT_FILENAME = "output.mat";
 
 	private static final String DEFAULT_IMAGE_NAME = "image";
 
 	private final String fileName;
 
 	private final String imageName;
 
 	private double[] array;
 
 	private int width;
 
 	private int height;
 
 	public MatlabXYZFileDisplay(String fileName, String imageName) {
 		this.fileName = fileName;
 		this.imageName = imageName;
 	}
 
 	public MatlabXYZFileDisplay(String filename) {
 		this(filename, DEFAULT_IMAGE_NAME);
 	}
 
 	public MatlabXYZFileDisplay() {
 		this(DEFAULT_FILENAME, DEFAULT_IMAGE_NAME);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#fill(int, int, int, int, ca.eandb.jmist.framework.color.Color)
 	 */
 	public void fill(int x, int y, int w, int h, Color color) {
 		for (int dy = 0; dy < h; dy++, y++) {
 			int index = (y * width + x) * 3;
 			for (int dx = 0; dx < w; dx++) {
 				CIEXYZ c = color.toXYZ();
 				array[index++] = c.X();
 				array[index++] = c.Y();
				array[index++] = c.Z();
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#finish()
 	 */
 	public void finish() {
 		try {
 			OutputStream stream = getOutputStream();
 			MatlabWriter matlab = new MatlabWriter(stream);
 			matlab.write(imageName, array, null,
 					new int[] { height, width, 3 },
 					new int[] { 3 * width, 3, 1 });
 			matlab.flush();
 			matlab.close();
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 	/**
 	 * Gets the <code>OutputStream</code> to write to.
 	 * @return The <code>OutputStream</code> to write to.
 	 * @throws FileNotFoundException If the file cannot be created.
 	 */
 	private FileOutputStream getOutputStream() throws FileNotFoundException {
 		HostService service = JdcpUtil.getHostService();
 		if (service != null) {
 			return service.createFileOutputStream(fileName);
 		} else {
 			return new FileOutputStream(fileName);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#initialize(int, int, ca.eandb.jmist.framework.color.ColorModel)
 	 */
 	public void initialize(int w, int h, ColorModel colorModel) {
 		this.width = w;
 		this.height = h;
 		this.array = new double[width * height * 3];
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#setPixel(int, int, ca.eandb.jmist.framework.color.Color)
 	 */
 	public void setPixel(int x, int y, Color pixel) {
 		int index = (y * width + x) * 3;
 		CIEXYZ c = pixel.toXYZ();
 		array[index++] = c.X();
 		array[index++] = c.Y();
 		array[index] = c.Z();
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#setPixels(int, int, ca.eandb.jmist.framework.Raster)
 	 */
 	public void setPixels(int x, int y, Raster pixels) {
 		int w = pixels.getWidth();
 		int h = pixels.getHeight();
 		for (int ry = 0; ry < h; ry++, y++) {
 			int index = (y * width + x) * 3;
 			for (int rx = 0; rx < w; rx++) {
 				Color pixel = pixels.getPixel(rx, ry);
 				CIEXYZ c = pixel.toXYZ();
 				array[index++] = c.X();
 				array[index++] = c.Y();
				array[index++] = c.Z();
 			}
 		}
 	}
 
 }
