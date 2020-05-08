 package level;
 
 
 
 import java.awt.*;
 import java.awt.image.*;
 import java.util.StringTokenizer;
 import com.golden.gamedev.object.background.abstraction.*;
 import com.golden.gamedev.util.*;
 import com.golden.gamedev.engine.*;
 
 
 @SuppressWarnings("serial")
 public class Map extends AbstractTileBackground {
 
 	public static final int TILE_WIDTH = 32, TILE_HEIGHT = 32;
 
 	Chipset		chipsetE;
 	Chipset		chipsetF;
 	Chipset[] 	chipset;
 
 	int[][] layer1;			// the lower tiles
 	int[][]	layer2;			// the fringe tiles
 
 	public Map(BaseLoader bsLoader, BaseIO bsIO) {
 		super(0, 0, TILE_WIDTH, TILE_HEIGHT);
 
 		layer1 = new int[40][25];
 		layer2 = new int[40][25];
 
		String[] lowerTile = FileUtil.fileRead(bsIO.getStream("rsc/level/lower.lwr"));
		String[] upperTile = FileUtil.fileRead(bsIO.getStream("rsc/level/upper.upr"));
 		for (int j=0;j < layer1[0].length;j++) {
 			StringTokenizer lowerToken = new StringTokenizer(lowerTile[j]);
 			StringTokenizer upperToken = new StringTokenizer(upperTile[j]);
 			for (int i=0;i < layer1.length;i++) {
 				layer1[i][j] = Integer.parseInt(lowerToken.nextToken());
 				layer2[i][j] = Integer.parseInt(upperToken.nextToken());
 			}
 		}
 
 		// set the actual map size based on the read file
 		setSize(layer1.length, layer1[0].length);
 
 		chipsetE = new Chipset(bsLoader.getImages("rsc/level/ChipSet2.png", 6, 24, false));
 		chipsetF = new Chipset(bsLoader.getImages("rsc/level/ChipSet3.png", 6, 24));
 
 		chipset = new Chipset[16];
 		BufferedImage[] image = bsLoader.getImages("rsc/level/ChipSet1.png", 4, 4, false);
 		int[] chipnum = new int[] { 0,1,4,5,8,9,11,12,2,3,6,7,10,11,14,15 };
 		for (int i=0;i < chipset.length;i++) {
 			int num = chipnum[i];
 			BufferedImage[] chips = ImageUtil.splitImages(image[num], 3, 4);
 			chipset[i] = new Chipset(chips);
 		}
 	}
 
 	public void renderTile(Graphics2D g,
 						   int tileX, int tileY,
 						   int x, int y) {
 		// render layer 1
 		int tilenum = layer1[tileX][tileY];
 		if (tilenum < chipsetE.image.length) {
 			g.drawImage(chipsetE.image[tilenum], x, y, null);
 
 		} else if (tilenum >= chipsetE.image.length) {
 			BufferedImage image = chipset[tilenum-chipsetE.image.length].image[2];
 			g.drawImage(image, x, y, null);
 		}
 
 		// render layer 2
 		int tilenum2 = layer2[tileX][tileY];
 		if (tilenum2 != -1) {
 			g.drawImage(chipsetF.image[tilenum2], x, y, null);
 		}
 
 		// layer 3 is rendered by sprite group
 	}
 
 	public boolean isOccupied(int tileX, int tileY) {
 	try {
 	    return (layer2[tileX][tileY] != -1);
 	} catch (Exception e) {
 		// out of bounds
 		return true;
 	} }
 
 
 	// chipset is only a pack of images
 	class Chipset {
 		BufferedImage[] image;
 
 		public Chipset(BufferedImage[] image) {
 			this.image = image;
 		}
 
 	}
 
 }
