 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 
 public class FirstLevel extends Level
 {
 	static int HEIGHT;
 	static int WIDTH;
 	Color color;
 	
 	public FirstLevel(Window w)
 	{
 		HEIGHT = w.getHeight();
 		WIDTH = w.getWidth();
 		
 		setColor(color);
 		
 		ArrayList<Element> walls = new ArrayList<Element>();
 		walls.add(new Element(10, HEIGHT-60, 400, 20, Color.blue, "Floor", false));
 		walls.add(new Element(400, HEIGHT-150, 700, 20, Color.blue, "Floor", false));
 		walls.add(new Element(350, HEIGHT-100, 100, 20, Color.blue, "Floor", false));
 		walls.add(new Element(1550, HEIGHT-100, 400, 20, Color.blue, "Floor", false));
 		walls.add(new Element(1850, HEIGHT-300, 200, 20, Color.blue, "Floor", false));
 		walls.add(new Element(2800, HEIGHT-200, 70, 20, Color.blue, "Floor", false));
 		walls.add(new Element(3000, HEIGHT-300, 70, 20, Color.blue, "Floor", false));
 		walls.add(new Element(3300, HEIGHT-500, 50, 20, Color.blue, "Floor", false));
 		walls.add(new Element(3700, HEIGHT-200, 120, 20, Color.blue, "Floor", false));
 		
 		Image earthFloor = null;
 		
 		try {earthFloor = ImageIO.read(new File("earthFloor.png"));}
 		catch (IOException e){System.out.println("*****ERROR****");}
 		
 		for (Element e : walls)
 			e.setImage(earthFloor);
 		
 		Element floor2 = new Element(2050, HEIGHT-400, 700, 20, Color.blue, "Floor", false);
 		floor2.setImage(earthFloor);
 		walls.add(floor2);
 		
		BearEnemy badguy2 = new BearEnemy(2050, HEIGHT-500, 45, 45, Color.red, "Enemy", true, getWalls(), Window.getWindow());
 		badguy2.setElement(floor2);
 		
 		Element floor = new Element(1100, HEIGHT-200, 400, 20, Color.blue, "Floor", false);
 		floor.setImage(earthFloor);
 		walls.add(floor);
 		
		BearEnemy badguy = new BearEnemy(1350, HEIGHT-400, 45, 45, Color.red, "Enemy", true, getWalls(), Window.getWindow());
 		badguy.setElement(floor);
 		BufferedImage img = null;
 
 		walls.add(badguy);
 		walls.add(badguy2);
 		
 		HealthPack pack = new HealthPack(2150, HEIGHT-600, 45, 45, Color.white, "Health", true, w);
 		BufferedImage img2 = null;
 		
 		try {img2 = ImageIO.read(new File("healthpack.png"));}
 		catch (IOException e){System.out.println("*****ERROR****");}
 
 		walls.add(pack);
 		
 		this.setWalls(walls);
 		
 		ArrayList<NotTangible> background = new ArrayList<NotTangible>();
 		
 		Image treeImg = null;
 		
 		try {treeImg = ImageIO.read(new File("tree.png"));}
 		catch (IOException e){System.out.println("*****ERROR****");}
 		
 		NotTangible tree = new NotTangible(1150, HEIGHT-200-(treeImg.getHeight(null)), treeImg.getWidth(null), treeImg.getHeight(null), Color.blue, "Tree", false);
 		tree.setImage(treeImg);
 		
 		background.add(tree);
 		this.setScenery(background);		
 	}
 	
 	public BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha)
 	{
 		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
 		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
 		Graphics2D g = scaledBI.createGraphics();
 		if (preserveAlpha) 		
 			g.setComposite(AlphaComposite.Src);
 		
 		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
 		g.dispose();
 		return scaledBI;
 	}
 	
 }
