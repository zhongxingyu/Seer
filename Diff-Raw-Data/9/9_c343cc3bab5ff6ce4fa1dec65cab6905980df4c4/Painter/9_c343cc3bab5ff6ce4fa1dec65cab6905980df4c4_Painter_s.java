 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.TreeMap;
 
 import javax.swing.ImageIcon;
 
 
 public class Painter 
 {
 	private static TreeMap<ImageEnum, ImageIcon> allImages;
 
 	// Image constants
 	public static enum ImageEnum {
 		RAISIN, NUT, PUFF_CHOCOLATE, PUFF_CORN, BANANA, CHEERIO, CINNATOAST, FLAKE_BRAN, CORNFLAKE, GOLDGRAHAM, STRAWBERRY, 
 		PART_ROBOT_HAND, KIT_ROBOT_HAND, ROBOT_ARM_1, ROBOT_BASE, ROBOT_RAIL,
 		KIT, KIT_TABLE, KITPORT, KITPORT_HOOD_IN, KITPORT_HOOD_OUT, PALLET,
 		FEEDER, LANE, NEST, DIVERTER, DIVERTER_ARM, PARTS_BOX,
 		CAMERA_FLASH, SHADOW1, SHADOW2,
 		GANTRY_BASE, GANTRY_CRANE, GANTRY_TRUSS_H, GANTRY_TRUSS_V, GANTRY_WHEEL
 	}
 
 	/**
 	 * Basic draw method. Images rotate about their center point.
 	 * 
 	 * @param g - graphics object to draw to
 	 * @param partType - enum to determine which image to draw
 	 * @param currentTime
 	 * @param movement - location of the image
 	 * @param useCenterPoint - if true, uses the movement object to determine where the center of the image will be, if false, where the upper left corner of the image will be
 	 */
 	static void draw(Graphics2D g, ImageEnum partType, long currentTime, Movement movement, boolean useCenterPoint)
 	{
 		ImageIcon image = allImages.get(partType);
 		
 		if (image == null)
 		{
 			System.err.println("The " + partType.toString() + " image has not been loaded yet!");
 			return;
 		}
 		
 		// Convert the ImageIcon to BufferedImage to rotate and scale
 		int w = image.getIconWidth();
 		int h = image.getIconHeight();
 		BufferedImage buffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2 = (Graphics2D)buffImg.getGraphics();
 		image.paintIcon(null, g2, 0, 0);
 		g2.dispose();
 		
 		AffineTransform tx = new AffineTransform();
 
 		int imgWidth = buffImg.getWidth();
 		int imgHeight = buffImg.getHeight();
 
 		// Rotate
 		double rot = movement.calcRot(currentTime);
 		if (rot != 0) {
 			tx.translate(imgWidth, imgHeight);
 			tx.rotate(rot);
 			tx.translate(-imgWidth/2, -imgHeight/2);
 		}
 
 		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
 		
 		try {
 			if (rot != 0) buffImg = op.filter(buffImg, null);
 			
 			int drawOffsetX = 0;
 			int drawOffsetY = 0;
 			if (useCenterPoint)
 			{
 				drawOffsetX = -1*imgWidth/2;
 				drawOffsetY = -1*imgHeight/2;
 			}
 			if (rot == 0)
 			{
 				drawOffsetX += imgWidth/2;
 				drawOffsetY += imgHeight/2;
 			}
 			
 			g.drawImage(buffImg, (int)(movement.calcPos(currentTime).x - imgWidth/2) + drawOffsetX, (int)(movement.calcPos(currentTime).y - imgHeight/2) + drawOffsetY, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	/**
 	 * Draws and scales. Images rotate about their center point.
 	 * 
 	 * @param g - graphics object to draw to
 	 * @param partType - enum to determine which image to draw
 	 * @param desiredWidth - scaled width - if -1, width is automatically scaled to maintain aspect ratio
 	 * @param desiredHeight - scaled height - if -1, height is automatically scaled to maintain aspect ratio
 	 * @param currentTime
 	 * @param movement - location of the image
 	 * @param useCenterPoint - if true, uses the movement object to determine where the center of the image will be, if false, where the upper left corner of the image will be
 	 */
 	static void draw(Graphics2D g, ImageEnum partType, int desiredWidth, int desiredHeight, long currentTime, Movement movement, boolean useCenterPoint)
 	{
 		ImageIcon image = allImages.get(partType);
 		
 		if (image == null)
 		{
 			System.err.println("The " + partType.toString() + " image has not been loaded yet!");
 			return;
 		}
 		
 		// Convert the ImageIcon to BufferedImage to rotate and scale
 		int w = image.getIconWidth();
 		int h = image.getIconHeight();
 		BufferedImage buffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2 = (Graphics2D)buffImg.getGraphics();
 		image.paintIcon(null, g2, 0, 0);
 		g2.dispose();
 		
 		BufferedImage scaledImg = scaleImage(buffImg, desiredWidth, desiredHeight);
 
 		int imgWidth = scaledImg.getWidth();
 		int imgHeight = scaledImg.getHeight();
 		
 		
 		AffineTransform tx = new AffineTransform();
 
 		// Rotate
 		double rot = movement.calcRot(currentTime);
 		if (rot != 0) {
 			tx.translate(imgWidth, imgHeight);
 			tx.rotate(rot);
 			tx.translate(-imgWidth/2, -imgHeight/2);
 		}
 		
 		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
 		
 		try {
 			if (rot != 0) scaledImg = op.filter(scaledImg, null);
 			
 			int drawOffsetX = 0;
 			int drawOffsetY = 0;
 			if (useCenterPoint)
 			{
 				drawOffsetX = -1*imgWidth/2;
 				drawOffsetY = -1*imgHeight/2;
 			}
 			if (rot == 0)
 			{
 				drawOffsetX += imgWidth/2;
 				drawOffsetY += imgHeight/2;
 			}
 			
 			g.drawImage(scaledImg, (int)movement.calcPos(currentTime).x - imgWidth/2 + drawOffsetX,
 								   (int)movement.calcPos(currentTime).y  - imgHeight/2 + drawOffsetY, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns the width of an image that was scaled using automatic width scaling to maintain aspect ratio
 	 * 
 	 * @param partType - enum to identify which image was used
 	 * @param desiredHeight - the height the image was scaled to
 	 * @return
 	 */
 	public static int getScaledWidth(ImageEnum partType, double desiredHeight)
 	{
 		ImageIcon image = allImages.get(partType);
 
 		if (image == null)
 		{
 			System.err.println("The " + partType.toString() + " image has not been loaded yet!");
 			return -1;
 		}
 
 		// Convert the ImageIcon to BufferedImage to rotate and scale
 		int w = image.getIconWidth();
 		int h = image.getIconHeight();
 		
 		double scaleFactor = ((double)desiredHeight)/((double)h);
 		return (int) (scaleFactor*w);
 	}
 	
 	/**
 	 * Returns the height of an image that was scaled using automatic height scaling to maintain aspect ratio
 	 * 
 	 * @param partType - enum to identify which image was used
 	 * @param desiredWidth - the width the image was scaled to
 	 * @return
 	 */
 	public static int getScaledHeight(ImageEnum partType, double desiredWidth)
 	{
 		ImageIcon image = allImages.get(partType);
 
 		if (image == null)
 		{
 			System.err.println("The " + partType.toString() + " image has not been loaded yet!");
 			return -1;
 		}
 
 		// Convert the ImageIcon to BufferedImage to rotate and scale
 		double w = (double)image.getIconWidth();
 		double h = (double)image.getIconHeight();
 		
 		double scaleFactor = ((double)desiredWidth)/((double)w);
 		return (int) (Math.round(scaleFactor*h));
 	}
 	
 	/**
 	 * Scales an image
 	 * @param img - the image to be scaled
 	 * @param desiredWidth - if -1, width is scaled automatically to maintain aspect ratio
 	 * @param desiredHeight - if -1, height is scaled automatically to maintain aspect ratio
 	 * @return the resulting scaled image
 	 */
 	public static BufferedImage scaleImage(BufferedImage img, int desiredWidth, int desiredHeight)
 	{
 		double xScaleFactor = (desiredWidth)*1.0/img.getWidth();
 		double yScaleFactor = (desiredHeight)*1.0/img.getHeight();
 
 		if (desiredWidth == -1)
 			xScaleFactor = yScaleFactor;
 		if (desiredHeight == -1)
 			yScaleFactor = xScaleFactor;
 
 		BufferedImage scaledImg = new BufferedImage((int)(img.getWidth()*xScaleFactor), (int)(img.getHeight()*yScaleFactor), img.getType());
 		Graphics2D gfx = scaledImg.createGraphics();
 		gfx.drawImage(img, 0, 0, (int)(img.getWidth()*xScaleFactor), (int)(img.getHeight()*yScaleFactor),
 				0, 0, img.getWidth(), img.getHeight(), null);
 		gfx.dispose();
 		
 		return scaledImg;
 	}
 	
 	/**
 	 * Crops an image
 	 * @param img - the image to be cropped
 	 * @param x - coordinate of the upper left corner of the crop
 	 * @param y - coordinate of the upper left corner of the crop
 	 * @param width - width of the crop (width of the desired end image)
 	 * @param height - height of the crop (height of the desired end image)
 	 * @return the resulting cropped image
 	 */
 	public static BufferedImage cropImage(BufferedImage img, int x, int y, int width, int height)
 	{
 		return img.getSubimage(x, y, width, height);
 	}
 	
 	public static ImageIcon getImageIcon(ImageEnum en)
 	{
 		return allImages.get(en);
 	}
 
 	public static void loadImages()
 	{
 		// Images need to be loaded
 		System.out.print("Loading images... ");
 		
 		allImages = new TreeMap<ImageEnum, ImageIcon>();
 		
 		try {
 		addImage(ImageEnum.RAISIN, "images/parts/raisin.png");
 		addImage(ImageEnum.NUT, "images/parts/nut.png");
 		addImage(ImageEnum.PUFF_CHOCOLATE, "images/parts/puff_chocolate.png");
 		addImage(ImageEnum.PUFF_CORN, "images/parts/puff_corn.png");
 		addImage(ImageEnum.BANANA, "images/parts/banana.png");
 		addImage(ImageEnum.CHEERIO, "images/parts/cheerio.png");
 		addImage(ImageEnum.CINNATOAST, "images/parts/cinnatoast.png");
		addImage(ImageEnum.CORNFLAKE, "images/parts/cornflake.png");
 		addImage(ImageEnum.FLAKE_BRAN, "images/parts/flake_bran.png");
 		addImage(ImageEnum.GOLDGRAHAM, "images/parts/goldgraham.png");
 		addImage(ImageEnum.STRAWBERRY, "images/parts/strawberry.png");
 		
 		addImage(ImageEnum.PART_ROBOT_HAND, "images/robots/part_robot_hand.png");
 		addImage(ImageEnum.KIT_ROBOT_HAND, "images/robots/kit_robot_hand.png");
 		addImage(ImageEnum.ROBOT_ARM_1, "images/robots/robot_arm_1.png");
 		addImage(ImageEnum.ROBOT_BASE, "images/robots/robot_base.png");
 		addImage(ImageEnum.ROBOT_RAIL, "images/robots/robot_rail.png");
 		
 		addImage(ImageEnum.KIT, "images/kit/empty_kit.png");
 		addImage(ImageEnum.KIT_TABLE, "images/kit/kit_table.png");
 		addImage(ImageEnum.KITPORT, "images/kit/kitport.png");
 		addImage(ImageEnum.KITPORT_HOOD_IN, "images/kit/kitport_hood_in.png");
 		addImage(ImageEnum.KITPORT_HOOD_OUT, "images/kit/kitport_hood_out.png");
 		addImage(ImageEnum.PALLET, "images/kit/pallet.png");
 		
 		addImage(ImageEnum.FEEDER, "images/lane/feeder.png");
 		addImage(ImageEnum.LANE, "images/lane/lane.png");
 		addImage(ImageEnum.NEST, "images/lane/nest.png");
 		addImage(ImageEnum.DIVERTER, "images/lane/diverter.png");
 		addImage(ImageEnum.DIVERTER_ARM, "images/lane/diverter_arm.png");
 		addImage(ImageEnum.PARTS_BOX, "images/lane/partsbox.png");
 		
 		addImage(ImageEnum.CAMERA_FLASH, "images/misc/camera_flash.png");
 		addImage(ImageEnum.SHADOW1, "images/misc/shadow1.png");
 		addImage(ImageEnum.SHADOW2, "images/misc/shadow2.png");
 		
 		addImage(ImageEnum.GANTRY_BASE, "images/gantry/gantry_base.png");
 		addImage(ImageEnum.GANTRY_CRANE, "images/gantry/gantry_crane.png");
 		addImage(ImageEnum.GANTRY_TRUSS_H, "images/gantry/gantry_truss_h.png");
 		addImage(ImageEnum.GANTRY_TRUSS_V, "images/gantry/gantry_truss_v.png");
 		addImage(ImageEnum.GANTRY_WHEEL, "images/gantry/gantry_wheel.png");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		System.out.println("Done");
 	}
 
 	private static void addImage(ImageEnum imageEnum, String path) {
 		// exit program if file not found
 		if (!new File(path).exists()) {
 			System.out.println("Could not find file \"" + path + "\".\n"
 			                   + "The program will exit now.");
 			System.exit(1);
 		}
 		// add new ImageIcon to image list
 		allImages.put(imageEnum, new ImageIcon(path));
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
