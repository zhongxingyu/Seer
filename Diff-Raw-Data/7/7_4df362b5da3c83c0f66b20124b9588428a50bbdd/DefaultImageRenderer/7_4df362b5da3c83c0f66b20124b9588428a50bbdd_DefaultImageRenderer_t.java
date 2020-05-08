 package net.coobird.paint.image;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 public final class DefaultImageRenderer implements ImageRenderer
 {
 	/*
 	 * Performance issues:
 	 * Large Canvases will take a long time to render
 	 * think of a way to reduce the time it takes to render
 	 * think of a way to clip the area where the image is to be rendered
 	 */
 	
 	/*
 	 * TODO implement PixelGrid drawing.
 	 * ClippableImageRenderer is the reference implementation.
 	 */
 	
 	/*
 	 * FIXME don't draw background on export to image
 	 * investigate a way to diable background drawing on save to image
 	 */
 	
 	/**
 	 * Instantiates a {@code DefaultImageRenderer}.
 	 */
 	public DefaultImageRenderer()
 	{
 		
 	}
 	
 	/**
 	 * Renders the ImageLayers in the given Canvas to a single BufferedImage.
 	 * @param c					The {@code Canvas} object to render.
 	 */
 	public BufferedImage render(Canvas c)
 	{
 		return render(c, true);
 	}
 	
 	/**
 	 * Renders the ImageLayers in the given Canvas to a single BufferedImage.
 	 * @param c					The {@code Canvas} object to render.
 	 * @param drawBackground	Whether or not to display a checkered
 	 * 							background.
 	 */
 	public BufferedImage render(Canvas c, boolean drawBackground)
 	{
 		if (c == null)
 		{
 			throw new NullPointerException("Canvas not initialized.");
 		}
 		
 		List<ImageLayer> layerList = c.getRenderOrder();
 		
 		BufferedImage img = new BufferedImage(
 				c.getWidth(),
 				c.getHeight(),
 				ImageLayer.getDefaultType()
 		);
 
 		
 		Graphics2D g = img.createGraphics();
 		Composite originalComposite = g.getComposite();
 		
 		if (drawBackground)
 		{
 			drawBackground(img);
 		}
 		
 		for (ImageLayer layer : layerList)
 		{
 			if (!layer.isVisible())
 			{
 				continue;
 			}
 			
			g.setComposite(layer.getAlphaComposite());
 			g.drawImage(layer.getImage(), layer.getX(), layer.getY(), null);
 		}
 		
 		g.setComposite(originalComposite);
 		g.dispose();
 		
 		return img;
 	}
 	
 	/**
 	 * Draws the checkered background.
 	 * @param img			The {@code BufferedImage} objec to draw the
 	 * 						background rendering to.
 	 */
 	private void drawBackground(BufferedImage img)
 	{
 		Graphics g = img.getGraphics();
 
 		int width = img.getWidth();
 		int height = img.getHeight();
 		
 		for (int i = 0; i < width; i += 20)
 		{
 			for (int j = 0; j < height; j += 20)
 			{
 				if ((i + j) % 40 == 0)
 					g.setColor(Color.gray);
 				else
 					g.setColor(Color.white);
 			
 				g.fillRect(i, j, 20, 20);
 			}
 		}
 		
 		g.dispose();
 	}
 }
