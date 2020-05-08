 package gdp.racetrack;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayDeque;
 import java.util.Deque;
 import javax.imageio.ImageIO;
 import gdp.racetrack.Point;
 
 public class MapGenerator {
 
 	/**
 	 * Generate a new map by the given seed.
 	 * @param seed The seed to be used for generating the map
 	 * @return The new generated map
 	 */
 	public Map generateMap(int seed, int numberPlayers, Difficulty difficulty) {
 		Vec2D size = new Vec2D(numberPlayers*8*Map.GRIDSIZE/difficultyToInt(difficulty), numberPlayers*8*Map.GRIDSIZE*2/3);
 		
 		for(int i=8; i<32; i++){
 			if(size.x <= 1<<i){
 				size = new Vec2D(1<<i, size.y);
 				break;
 			}
 		}
 		for(int i=8; i<32; i++){
 			if(size.x <= 1<<i){
 				size = new Vec2D(size.x, 1<<i);
 				break;
 			}
 		}
 		
 		BufferedImage mapImage = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_RGB);
 		
 		for(int x=0; x<size.x; x++){
 			for(int y=0; y<size.y; y++){
 				mapImage.setRGB(x,y,Map.COLOR_BACKGROUND);
 			}
 		}
 		
 		PerlinNoise noise1D = new PerlinNoise(1, 1, 1, seed);
 		Vec2D relPoints[][] = new Vec2D[4][4];
 		for(int i=0; i<4; i++)
 		{
 			for(int j=0; j<4; j++)
 			{
 				double pos[] = new double[1];
 				pos[0] = i*8+j*2;
 				int x = (int) ((noise1D.getPerlinNoise(pos)+1)/2*size.x/8 + size.x/16);
 				pos[0] = i*8+j*2+1;
 				int y = (int) ((noise1D.getPerlinNoise(pos)+1)/2*size.y/8 + size.y/16);
 				relPoints[i][j] = new Vec2D(x,y);
 			}
 		}
 		
 		Vec2D absPoints[][] = new Vec2D[4][4];
 		for(int i=0; i<4; i++)
 		{
 			for(int j=0; j<4; j++)
 			{
 				absPoints[i][j] = new Vec2D(relPoints[i][j].x + (i%2)*size.x/2 + (j%2)*size.x/4, relPoints[i][j].y + (i/2)*size.y/2 + (j/2)*size.y/4);
 			}
 		}
 		absPoints[0][0] = new Vec2D(absPoints[0][0].x - absPoints[0][0].x%Map.GRIDSIZE, absPoints[0][0].y - absPoints[0][0].y%Map.GRIDSIZE);
 		absPoints[0][2] = new Vec2D(absPoints[0][0].x, absPoints[0][0].y+(numberPlayers+2)*16);
 	
 		
 		Vec2D startPoints[] = new Vec2D[16];
 		Vec2D endPoints[] = new Vec2D[16];
 
 		startPoints[0]  = absPoints[0][0];
 		startPoints[1]  = absPoints[0][1];
 		startPoints[2]  = absPoints[1][0];
 		startPoints[3]  = absPoints[1][1];
 		startPoints[4]  = absPoints[1][3];
 		startPoints[5]  = absPoints[3][1];
 		startPoints[6]  = absPoints[3][3];
 		startPoints[7]  = absPoints[3][2];
 		startPoints[8]  = absPoints[2][3];
 		startPoints[9]  = absPoints[2][2];
 		startPoints[10] = absPoints[2][0];
 		startPoints[11] = absPoints[2][1];
 		startPoints[12] = absPoints[3][0];
 		startPoints[13] = absPoints[1][2];
 		startPoints[14] = absPoints[0][3];
 		startPoints[15] = absPoints[0][2];
 		for(int i=1; i<17; i++)
 		{
 			endPoints[i%16] = startPoints[i-1];
 		}
 		
 		for(int i=0; i<16; i++)
 		{
 			if(i==0)
 				drawLine(startPoints[i], endPoints[i], mapImage, seed+i*2, Map.COLOR_START, 0);
 			else if(i==10)
 				drawLine(startPoints[i], endPoints[i], mapImage, seed+i*2, Map.COLOR_FINISH, 0);
 			else
 				drawLine(startPoints[i], endPoints[i], mapImage, seed+i*2, 0x000000, Math.min(size.x, size.y)/32);
 				
 		}
 		fillImage(mapImage, (absPoints[0][0].x + absPoints[0][3].x)/2, (absPoints[0][0].y + absPoints[0][3].y)/2, Map.COLOR_BACKGROUND, Map.COLOR_TRACK);
 		
 		PerlinNoise noise = new PerlinNoise(1, 1, 1, seed);
 		double pos[] = new double[1];
 		pos[0] = 0;
 		double min = Math.min(size.x, size.y)/2.0;
 		for(int i=0; i<numberPlayers*(1.0+difficultyToInt(difficulty)/2.0)/2; i++){
 			int x, y;
 			do{
 				pos[0] += 1; double tmpX = noise.getPerlinNoise(pos);
 				pos[0] += 1; double tmpY = noise.getPerlinNoise(pos);
 				x = (int)((tmpX + 1)*size.x/2);
 				y = (int)((tmpY + 1)*size.y/2);
 			}while((mapImage.getRGB(x, y)&0xFFFFFF) != Map.COLOR_TRACK || startPoints[0].distTo(new Vec2D(x, y)) < min || startPoints[0].distTo(new Vec2D(x, y)) < min);
 			pos[0] += 1; int radius = (int)((noise.getPerlinNoise(pos) + 2) * Math.min(size.x, size.y)/64);
 			drawCircle(x, y, radius, mapImage, seed+i*2);
 		}
 		
 		
 		AffineTransform at = AffineTransform.getTranslateInstance(0,0);  
         at.rotate(Math.toRadians(90)*(seed%4), size.x/2, size.y/2);  
         AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
         mapImage = op.filter(mapImage, null);
                 
 		Map map = new Map(mapImage);
 		return map;
 	}
 
 	private void drawLine(Vec2D p1, Vec2D p2, BufferedImage image, int seed, int color, int scattering)
 	{
 		int max = Math.max(image.getHeight(), image.getWidth());
 		
 		PerlinNoise noiseX = new PerlinNoise(1, 0.5, 8, seed);		
 		PerlinNoise noiseY = new PerlinNoise(1, 0.5, 8, seed+1);
 		double pos[] = new double[1];
 		pos[0] = 0;
 		int startX = (int) (noiseX.getPerlinNoise(pos)*scattering);
 		int startY = (int) (noiseY.getPerlinNoise(pos)*scattering);
 		pos[0] = 8/Math.PI;
 		int endX = (int) (noiseX.getPerlinNoise(pos)*scattering);
 		int endY = (int) (noiseY.getPerlinNoise(pos)*scattering);
 		
 		for(int i=0; i<=max; i++)
 		{
 			pos[0] = i*8/Math.PI/max;
 			int x = (int) (p1.x + (p2.x - p1.x)*i/max + noiseX.getPerlinNoise(pos)*scattering - startX - (endX - startX)*i/max);
 			int y = (int) (p1.y + (p2.y - p1.y)*i/max + noiseY.getPerlinNoise(pos)*scattering - startY - (endY - startY)*i/max);
 			for(int dx = -1; dx<=1; dx++)
 			{
 				for(int dy = -1; dy<=1; dy++)
 				{
 					int posX = Math.min(Math.max(x+dx, 0), image.getWidth()-1);
 					int posY = Math.min(Math.max(y+dy, 0), image.getHeight()-1);
 					image.setRGB(posX, posY, color);			
 				}
 			}
 		}
 		
 	}
 	
 	private void drawCircle(int centerX, int centerY, int radius, BufferedImage image, int seed) {
 		
 		int max = Math.max(image.getHeight(), image.getWidth());
 		double scattering = radius*2;
 		
 		PerlinNoise noiseX = new PerlinNoise(1, 0.5, 8, seed);		
 		PerlinNoise noiseY = new PerlinNoise(1, 0.5, 8, seed+1);
 		double pos[] = new double[1];
 		pos[0] = 0;
 		int startX = (int) (noiseX.getPerlinNoise(pos)*scattering);
 		int startY = (int) (noiseY.getPerlinNoise(pos)*scattering);
 		pos[0] = 8/Math.PI;
 		int endX = (int) (noiseX.getPerlinNoise(pos)*scattering);
 		int endY = (int) (noiseY.getPerlinNoise(pos)*scattering);
 		
 		for(double r=0; r<=radius; r+=0.5)
 		{
 			for(int i=0; i<=max; i++)
 			{
 				pos[0] = i*8/Math.PI/max;
 				int x = (int) (Math.sin(Math.PI*2*i/max)*r + noiseX.getPerlinNoise(pos)*scattering + centerX - startX - (endX - startX)*i/max);
 				int y = (int) (Math.cos(Math.PI*2*i/max)*r + noiseY.getPerlinNoise(pos)*scattering + centerY - startY - (endY - startY)*i/max);
 				for(int dx = -1; dx<=1; dx++)
 				{
 					for(int dy = -1; dy<=1; dy++)
 					{
 						int posX = Math.min(Math.max(x+dx, 0), image.getWidth()-1);
 						int posY = Math.min(Math.max(y+dy, 0), image.getHeight()-1);
 						image.setRGB(posX, posY, 0x000000);
 					}
 				}
 			}
 		}
 	}
 	
 	private void fillImage(BufferedImage image, int x, int y, int oldColor, int newColor)
 	{
 		Deque<Integer> posX = new ArrayDeque<Integer>();
 		Deque<Integer> posY = new ArrayDeque<Integer>();
 		posX.push(x);
 		posY.push(y);
 		while(!posX.isEmpty())
 		{
 			x = posX.pop();
 			y = posY.pop();
 			if(x <= 0 || x >= image.getWidth()-1 || y <= 0 || y >= image.getHeight()-1) continue;
 			
 			image.setRGB(x, y, newColor);
 			if((image.getRGB(x-1, y)&0xFFFFFF) == oldColor)
 			{
 				posX.push(x-1);
 				posY.push(y);
 			}
 			if((image.getRGB(x+1, y)&0xFFFFFF) == oldColor)
 			{
 				posX.push(x+1);
 				posY.push(y);
 			}
 			if((image.getRGB(x, y-1)&0xFFFFFF) == oldColor)
 			{
 				posX.push(x);
 				posY.push(y-1);
 			}
 			if((image.getRGB(x, y+1)&0xFFFFFF) == oldColor)
 			{
 				posX.push(x);
 				posY.push(y+1);
 			}
 		}
 	}
 	
 	private static int difficultyToInt(Difficulty difficulty)
 	{
 		switch(difficulty){
 		case EASY:
 			return 1;
 		case NORMAL:
 			return 2;
 		case HARD:
 			return 3;
 		default:
 			return 1;
 		}
 	}
 	
 	public static void main(String args[])
 	{
 		MapGenerator mapGen = new MapGenerator();
 		int maps[][] = new int[16*3][2];
 		Difficulty difficulty[] = new Difficulty[maps.length];
 		for(int i=0; i<maps.length; i++)
 		{
 			maps[i][0] = (i/3)%4+1; // numberPlayers
 			maps[i][1] = i/3; // seed
 			switch(i%3){
 			case 0:
 				difficulty[i] = Difficulty.EASY;
 				break;
 			case 1:
 				difficulty[i] = Difficulty.NORMAL;
 				break;
 			case 2:
 				difficulty[i] = Difficulty.HARD;
 				break;
 			}
 			
 		}
 		try {
 			for(int i=0; i<maps.length; i++)
 			{
 				Map map = mapGen.generateMap(maps[i][1], maps[i][0], difficulty[i]);
 				int sizeX = map.getSize().x*Map.GRIDSIZE;
 				int sizeY = map.getSize().y*Map.GRIDSIZE;
 				
 				Graphics turnTest = map.getImage().getGraphics();
 				
 				turnTest.setColor(new Color(Map.COLOR_BACKGROUND*3/2));
 				for(int x=0; x<sizeX; x+=Map.GRIDSIZE)
 				{
 					turnTest.drawLine(x, 0, x, sizeY);
 				}
 				for(int y=0; y<sizeX; y+=Map.GRIDSIZE)
 				{
 					turnTest.drawLine(0, y, sizeX, y);
 				}
 				
 				int posX = sizeX/4;
 				int posY = sizeY/4;
 				Point start = new Point(posX/Map.GRIDSIZE, posY/Map.GRIDSIZE);
 				for(int j=0; j<360; j+=10)
 				{
 					Point end = new Point((int)(posX * Math.sin(Math.toRadians(j))/Map.GRIDSIZE+posX/Map.GRIDSIZE), (int)(posY * Math.cos(Math.toRadians(j))/Map.GRIDSIZE+posY/Map.GRIDSIZE));
 					Turn t = map.getTurnResult(start, end);
 					
 					switch(t.getTurnType()){
 					case COLLISION_ENVIRONMENT:
 						turnTest.setColor(new Color(0xFF0000));
 						turnTest.drawLine(posX, posY, t.getNewPosition().getX()*Map.GRIDSIZE, t.getNewPosition().getY()*Map.GRIDSIZE);
 						break;
 					case FINISH:
 						turnTest.setColor(new Color(Map.COLOR_FINISH));
 						turnTest.drawLine(posX, posY, end.getX()*Map.GRIDSIZE, end.getY()*Map.GRIDSIZE);
 						break;
 					case OK:
 						turnTest.setColor(new Color(0x0000FF));
 						turnTest.drawLine(posX, posY, end.getX()*Map.GRIDSIZE, end.getY()*Map.GRIDSIZE);
 						break;
 					}
 				}
 				
 				ImageIO.write((RenderedImage) map.getImage(), "png", new File("map_" + maps[i][1] + "_" + maps[i][0] + "_" + difficultyToInt(difficulty[i]) + ".png"));
 				
 				BufferedImage metaImage = new BufferedImage(map.getSize().x*Map.GRIDSIZE, map.getSize().y*Map.GRIDSIZE, BufferedImage.TYPE_INT_RGB);
 				for(int x=0; x<sizeX; x++)
 				{
 					for(int y=0; y<sizeY; y++)
 					{
 						if(y%16 == 0 && x%16 == 0)
 						{
 							switch(map.getPointType(new Point(x/16, y/16))){
 							case TRACK:
 								metaImage.setRGB(x, y, Map.COLOR_TRACK);
 								break;
 							case START:
 								metaImage.setRGB(x, y, Map.COLOR_START);
 								break;
 							case FINISH:
 								metaImage.setRGB(x, y, Map.COLOR_FINISH);
 								break;
 							default:
 								metaImage.setRGB(x, y, 0);
 							}
 						}else
 						{
 							metaImage.setRGB(x,y,0);
 						}
 					}
 				}
 				ImageIO.write((RenderedImage) metaImage, "png", new File("map_" + maps[i][1] + "_" + maps[i][0] + "_" + difficultyToInt(difficulty[i]) + "meta.png"));
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
