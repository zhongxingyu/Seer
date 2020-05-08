 package complexion.test;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import complexion.resource.Sprite;
 
 public class Test {
 	public static void main(String[] args) throws IOException
 	{
 		Sprite s = new Sprite("mask.dmi");
 		
 	    BufferedImage bi = s.getImage("muzzle", 100, 100);
	    File outputfile = new File("saved.png");
 	    ImageIO.write(bi, "png", outputfile);
 	}
 }
