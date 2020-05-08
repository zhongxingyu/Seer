 package bots;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 public class Map {
 	private static final int TILE_SIZE = 10;
 	private final int width;
 	private final int height;
 	private final List<List<Ground>> tiles;
 	private final BufferedImage offscreen;
 	private final BufferedImage map_img;
 
 	public Map(String name) throws IOException {
 		URL url = this.getClass().getResource("/resources/maps/"+name);
 		assert (url != null);
 		map_img = (BufferedImage) new ImageIcon(ImageIO.read(url)).getImage();
 
 		width = map_img.getWidth() * TILE_SIZE;
 		height = map_img.getHeight() * TILE_SIZE;
 		tiles = new ArrayList<List<Ground>>(height);
 		offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 	}
 
 	public Ground get(final int x, final int y) {
 		if (x < 0 || y < 0 || x > width || y > height) {
 			return Ground.Void;
 		}
 
 		final int tileX = x / TILE_SIZE;
 		final int tileY = y / TILE_SIZE;
 
 		return tiles.get(tileX).get(tileY);
 	}
 
 	public void paint(Graphics g, Component observer) {
 		g.drawImage(offscreen, 0, 0, observer);
 	}
 
 	public Image cloneImage() {
 		BufferedImage img = new BufferedImage(offscreen.getWidth(), offscreen.getHeight(), offscreen.getType());
 		Graphics g = img.getGraphics();
 		g.drawImage(offscreen, 0, 0, null);
 		return img;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void init(Base red, Base blue) {
 		// TODO paint a nicer map!
 		for (int img_x = 0; img_x < map_img.getWidth(); img_x++) {
 			List<Ground> row = new ArrayList<Ground>(width);
 			for (int img_y = 0; img_y < map_img.getWidth(); img_y++) {
 				int pixel = map_img.getRGB(img_y, img_x);
 
 				Ground ground = Ground.Void;
 				switch (pixel) {
 				case 0xff3a9d3a:
 					ground = Ground.Grass;
 					break;
 				case 0xff375954:
 					ground = Ground.Swamp;
 					break;
 				case 0xffe8d35e:
 					ground = Ground.Sand;
 					break;
 				case 0xff323f05:
 					ground = Ground.Forest;
 					break;
 				case 0xff0000ff: /* BLUE */
 					ground = Ground.Grass;
					blue.setPosition(img_x*TILE_SIZE, img_y*TILE_SIZE);
 					break;
 				case 0xff787878:
 					ground = Ground.Rocks;
 					break;
 				case 0xffffffff: /* WHITE */
 					ground = Ground.Grass;
 					break;
 				case 0xffff0000: /* RED */
 					ground = Ground.Grass;
					red.setPosition(img_x*TILE_SIZE, img_y*TILE_SIZE);
 					break;
 				case 0xff3674db:
 					ground =  Ground.Water;
 					break;
 				default:
 					throw new RuntimeException("Map broken. Unknown color: "+Integer.toHexString(pixel));
 				}
 				row.add(img_y, ground);
 
 				/* paint offscreen image */
 				for (int i = 0; i < TILE_SIZE; i++) {
 					for (int j = 0; j < TILE_SIZE; j++) {
 						offscreen.setRGB(img_x*TILE_SIZE+i, img_y*TILE_SIZE+j, pixel);
 					}
 				}
 			}
 			tiles.add(row);
 		}
 	}
 }
