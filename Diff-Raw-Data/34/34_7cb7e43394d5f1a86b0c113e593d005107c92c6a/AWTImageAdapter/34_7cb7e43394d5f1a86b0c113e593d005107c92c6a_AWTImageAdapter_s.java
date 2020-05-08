 package jbookreader.rendering.swing;
 
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 
 import jbookreader.rendering.IDrawable;
 import jbookreader.rendering.Position;
 
 class AWTImageAdapter<T> implements IDrawable<T> {
 
 	private final JGraphicDriver<T> driver;
 	private BufferedImage image;
 	private final int width;
 	private final int height;
 	private final T context;
 
 	AWTImageAdapter(final JGraphicDriver<T> driver, final String contentType, final InputStream dataStream, final T context) throws IOException {
 		this.driver = driver;
 		this.context = context;
 
 		Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(contentType);
 		if (!readers.hasNext()) {
 			throw new UnsupportedOperationException("content type '" + contentType + "' isn't supported");
 		}
 		final ImageReader reader = readers.next();
		ImageInputStream stream = ImageIO.createImageInputStream(dataStream);
		if (stream == null) {
			throw new UnsupportedOperationException("Can't create image input stream");
		}
 		reader.setInput(stream, true);
 
 		width = reader.getWidth(0);
 		height = reader.getHeight(0);
 
 		Thread imageThread =
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					BufferedImage readImage = reader.read(0);
 					synchronized (AWTImageAdapter.this) {
 						image = readImage;
 					}
 					AWTImageAdapter.this.driver.getComponent().repaint();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		imageThread.setPriority(Thread.MIN_PRIORITY);
 		imageThread.start();
 	}
 
 	AWTImageAdapter(JGraphicDriver<T> driver, BufferedImage image, T context) {
 		this.driver = driver;
 		this.image = image;
 		this.context = context;
 
 		width = image.getWidth();
 		height = image.getHeight();
 	}
 
 	public void draw(Position position) {
 		BufferedImage drawImage = null;
 
 		synchronized (this) {
 			drawImage = image;
 		}
 		if (drawImage == null) {
 			driver.getPaperGraphics().draw(new Rectangle2D.Float(
 						driver.getHorizontalPosition(),
 						driver.getVerticalPosition(),
 						width,
 						height));
 		} else {
 			driver.getPaperGraphics().drawImage(image,
 					AffineTransform.getTranslateInstance(
 						driver.getHorizontalPosition(),
 						driver.getVerticalPosition()),
 					driver.getComponent());
 		}
 		driver.addHorizontalSpace(width);
 	}
 
 	public float getDepth() {
 		return 0;
 	}
 
 	public float getHeight() {
 		return height;
 	}
 
 	public float getWidth(Position position) {
 		return width;
 	}
 
 	public T getContext() {
 		return context;
 	}
 
 }
