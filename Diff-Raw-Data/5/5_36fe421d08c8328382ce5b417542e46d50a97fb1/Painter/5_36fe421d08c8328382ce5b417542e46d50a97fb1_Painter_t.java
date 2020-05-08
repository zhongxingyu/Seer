 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.util.TreeMap;
 
 import javax.swing.ImageIcon;
 
 
 public class Painter 
 {
 	private static TreeMap<ImageEnum, ImageIcon> allImages;
 
 	// Image constants
 	public static enum ImageEnum {
 		RAISIN, NUT, CORNFLAKE, PUFF_CHOCOLATE, 
 		PART_ROBOT_HAND, KIT_ROBOT_HAND, ROBOT_ARM_1, ROBOT_BASE, ROBOT_RAIL,
 		KIT, KIT_TABLE, KITPORT, KITPORT_HOOD_IN, KITPORT_HOOD_OUT, PALLET,
 		FEEDER, LANE, NEST, DIVERTER, DIVERTER_ARM, PARTS_BOX,
 		CAMERA_FLASH, SHADOW1, SHADOW2,
 		GANTRY_BASE, GANTRY_CRANE, GANTRY_TRUSS_MID, GANTRY_WHEEL
 	}
 
 
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
 		tx.translate(imgWidth, imgHeight);
 		tx.rotate(movement.calcRot(currentTime));
 		tx.translate(-imgWidth/2, -imgHeight/2);
 
 		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
 		
 		try {
 			buffImg = op.filter(buffImg, null);
 			
 			int drawOffsetX = 0;
 			int drawOffsetY = 0;
 			if (useCenterPoint)
 			{
 				drawOffsetX = -1*imgWidth/2;
 				drawOffsetY = -1*imgHeight/2;
 			}
 			
 			g.drawImage(buffImg, (int)(movement.calcPos(currentTime).x - imgWidth/2) + drawOffsetX, (int)(movement.calcPos(currentTime).y - imgHeight/2) + drawOffsetY, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/** Draw and scale. Set either desiredWidth or desiredHeight to -1 to set automatic scaling for that dimension */
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
 		
 		// Scale
 		double xScaleFactor = (desiredWidth)*1.0/buffImg.getWidth();
 		double yScaleFactor = (desiredHeight)*1.0/buffImg.getHeight();
 		
 		if (desiredWidth == -1)
 			xScaleFactor = yScaleFactor;
 		if (desiredHeight == -1)
 			yScaleFactor = xScaleFactor;
 						
 		BufferedImage scaledImg = new BufferedImage((int)(buffImg.getWidth()*xScaleFactor), (int)(buffImg.getHeight()*yScaleFactor), buffImg.getType());
 		Graphics2D gfx = scaledImg.createGraphics();
 		gfx.drawImage(buffImg, 0, 0, (int)(buffImg.getWidth()*xScaleFactor), (int)(buffImg.getHeight()*yScaleFactor),
 							   0, 0, buffImg.getWidth(), buffImg.getHeight(), null);
 		gfx.dispose();
 		
 
 		int imgWidth = scaledImg.getWidth();
 		int imgHeight = scaledImg.getHeight();
 		
		
 		AffineTransform tx = new AffineTransform();
 
 		// Rotate
 		tx.translate(imgWidth, imgHeight);
 		tx.rotate(movement.calcRot(currentTime));
 		tx.translate(-imgWidth/2, -imgHeight/2);
 		
 		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
 		
 		try {
 			scaledImg = op.filter(scaledImg, null);
 			
 			int drawOffsetX = 0;
 			int drawOffsetY = 0;
 			if (useCenterPoint)
 			{
 				drawOffsetX = -1*imgWidth/2;
 				drawOffsetY = -1*imgHeight/2;
 			}
 			
			g.drawImage(scaledImg, (int)movement.calcPos(currentTime).x - imgWidth/2 + drawOffsetX,
								   (int)movement.calcPos(currentTime).y  - imgHeight/2 + drawOffsetY, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static int getScaledWidth(ImageEnum partType, int desiredHeight)
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
 	
 	public static int getScaledHeight(ImageEnum partType, int desiredWidth)
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
 		
 		double scaleFactor = ((double)desiredWidth)/((double)w);
 		return (int) (scaleFactor*h);
 	}
 
 	public static void loadImages()
 	{
 		// Images need to be loaded
 		System.out.println("Loading images...");
 		
 		allImages = new TreeMap<ImageEnum, ImageIcon>();
 		
 		allImages.put(ImageEnum.RAISIN, new ImageIcon("images/parts/raisin.png"));
 		allImages.put(ImageEnum.NUT, new ImageIcon("images/parts/nut.png"));
 		allImages.put(ImageEnum.CORNFLAKE, new ImageIcon("images/parts/cornflake.png"));
 		allImages.put(ImageEnum.PUFF_CHOCOLATE, new ImageIcon("images/parts/puff_chocolate.png"));
 		
 		allImages.put(ImageEnum.PART_ROBOT_HAND, new ImageIcon("images/robots/part_robot_hand.png"));
 		allImages.put(ImageEnum.KIT_ROBOT_HAND, new ImageIcon("images/robots/kit_robot_hand.png"));
 		allImages.put(ImageEnum.ROBOT_ARM_1, new ImageIcon("images/robots/robot_arm_1.png"));
 		allImages.put(ImageEnum.ROBOT_BASE, new ImageIcon("images/robots/robot_base.png"));
 		allImages.put(ImageEnum.ROBOT_RAIL, new ImageIcon("images/robots/robot_rail.png"));
 		
 		allImages.put(ImageEnum.KIT, new ImageIcon("images/kit/empty_kit.png"));
 		allImages.put(ImageEnum.KIT_TABLE, new ImageIcon("images/kit/kit_table.png"));
 		allImages.put(ImageEnum.KITPORT, new ImageIcon("images/kit/kitport.png"));
 		allImages.put(ImageEnum.KITPORT_HOOD_IN, new ImageIcon("images/kit/kitport_hood_in.png"));
 		allImages.put(ImageEnum.KITPORT_HOOD_OUT, new ImageIcon("images/kit/kitport_hood_out.png"));
 		allImages.put(ImageEnum.PALLET, new ImageIcon("images/kit/pallet.png"));
 		
 		allImages.put(ImageEnum.FEEDER, new ImageIcon("images/lane/feeder.png"));
 		allImages.put(ImageEnum.LANE, new ImageIcon("images/lane/lane.png"));
 		allImages.put(ImageEnum.NEST, new ImageIcon("images/lane/nest.png"));
 		allImages.put(ImageEnum.DIVERTER, new ImageIcon("images/lane/diverter.png"));
 		allImages.put(ImageEnum.DIVERTER_ARM, new ImageIcon("images/lane/diverter_arm.png"));
 		allImages.put(ImageEnum.PARTS_BOX, new ImageIcon("images/lane/partsbox.png"));
 		
 		allImages.put(ImageEnum.CAMERA_FLASH, new ImageIcon("images/misc/camera_flash.png"));
 		allImages.put(ImageEnum.SHADOW1, new ImageIcon("images/misc/shadow1.png"));
 		allImages.put(ImageEnum.SHADOW2, new ImageIcon("images/misc/shadow2.png"));
 		
 		allImages.put(ImageEnum.GANTRY_BASE, new ImageIcon("images/gantry/gantry_base.png"));
 		allImages.put(ImageEnum.GANTRY_CRANE, new ImageIcon("images/gantry/gantry_crane.png"));
 		allImages.put(ImageEnum.GANTRY_TRUSS_MID, new ImageIcon("images/gantry/gantry_truss_mid.png"));
 		allImages.put(ImageEnum.GANTRY_WHEEL, new ImageIcon("images/gantry/gantry_wheel.png"));
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
