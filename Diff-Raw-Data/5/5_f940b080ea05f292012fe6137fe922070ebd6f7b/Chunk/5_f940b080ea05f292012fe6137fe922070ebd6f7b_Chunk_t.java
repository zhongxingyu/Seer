 package com.jpii.navalbattle.pavo;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
import java.util.Random;
 
 import maximusvladimir.dagen.*;
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.RenderConstants;
 
 public class Chunk extends Renderable {
 	int x,z;
 	boolean generated = false;
 	public EntityReference Tile00, Tile10, Tile01,Tile11;
 	static Perlin p = new Perlin(Constants.MAIN_RAND.nextLong(),0,0);
 	Rand rand = new Rand();
 	World w;
 	BufferedImage terrain;
 	public Chunk(World w) {
 		this.w = w;
 		Tile00 = Tile10 = Tile01 = Tile11 = w.getEntityManager().getTypeById(0);
 	}
 	public void setX(int x) {
 		this.x = x;
 	}
 	public void setZ(int z) {
 		this.z = z;
 	}
 	public int getX() {
 		return x;
 	}
 	public int getZ() {
 		return z;
 	}
 	public void setLoc(int x, int z) {
 		this.x = x;
 		this.z = z;
 	}
 	public void render() {
 		//if (!ready)
 			//return;
 		//ready = false;
		Random rp = new Random(Constants.MAIN_SEED+(x&z)+x-z+(z|x));
		rand = new Rand(rp.nextLong());
 		terrain = new BufferedImage(34,34,BufferedImage.TYPE_INT_RGB);
 		Graphics g = terrain.getGraphics();
 		int water00 = 0,water01 = 0,water10 = 0,water11 = 0;
 		for (int lsx = 0; lsx < 100/3; lsx++) {
 			for (int lsz = 0; lsz < 100/3; lsz++) {
 				float frsh = McRegion.getPoint(lsx+(100.0f/3.0f*x), lsz+(100.0f/3.0f*z));
 				int opcode = (int)(frsh*255.0f);
 				//opcode = (opcode+(int)(McRegion.getPoint((this.x*100), (this.z*100))*255.0f))/2;
 				if (opcode > 255)
 					opcode = 255;
 				if (opcode < 0)
 					opcode = 0;
 				g.setColor(new Color(opcode,opcode,opcode));
 				int nawo = rand.nextInt(-5, 8);
 				if (opcode < 130) {
 					g.setColor(Helper.adjust(Helper.randomise(new Color(83+nawo,83+nawo,132+nawo),
 	                        5, rand, false), 1 - ((frsh)/2 / RenderConstants.GEN_WATER_HEIGHT), 30));
 				}
 				else if (opcode < 135) {
 					g.setColor(Helper.adjust(Helper.randomise(RenderConstants.GEN_SAND_COLOR,
 	                        RenderConstants.GEN_COLOR_DIFF, rand, false), (1.0-frsh)/2, 50));
 					if (lsx < 16.6666666666666666 && lsz < 16.666666666666666)
 						water00 = 1;
 					else if (lsx >= 16.666666666666 && lsz < 16.666666666666666)
 						water10 = 1;
 					else if (lsx < 16.666666666666 && lsz >= 16.666666666666666)
 						water01 = 1;
 					else if (lsx >= 16.666666666666 && lsz >= 16.666666666666666)
 						water11 = 1;
 				}
 				else{
 					g.setColor(Helper.adjust(Helper.randomise(RenderConstants.GEN_GRASS_COLOR,
 	                        RenderConstants.GEN_COLOR_DIFF, rand, false), (1.0-frsh)/2, 60));
 					if (lsx < 16.6666666666666666 && lsz < 16.666666666666666)
 						water00 = 1;
 					else if (lsx >= 16.666666666666 && lsz < 16.666666666666666)
 						water10 = 1;
 					else if (lsx < 16.666666666666 && lsz >= 16.666666666666666)
 						water01 = 1;
 					else if (lsx >= 16.666666666666 && lsz >= 16.666666666666666)
 						water11 = 1;
 				}
 				if (opcode > 216) {
 					Color base = Helper.adjust(Helper.randomise(new Color(121+nawo,131+nawo,112+nawo),
                         5, rand, false), 1 - ((frsh)/2 / RenderConstants.GEN_WATER_HEIGHT), 30);
 					base = PavoHelper.Lerp(base, RenderConstants.GEN_GRASS_COLOR, ((opcode)/39.0));
 					g.setColor(base);
 				}
 				g.drawLine(lsx,lsz,lsx,lsz);
 				//g.fillRect(lsx*3,lsz*3,4,4);
 			}
 		}
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2), (getX()*2), water00);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2)+1, (getX()*2), water10);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2), (getX()*2)+1, water01);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2)+1, (getX()*2)+1, water11);
 		writeBuffer();
 		//ready = true;
 		generated = true;
 	}
 	public void writeBuffer() {
 		buffer = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		g.drawImage(terrain, 0, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile00), 0, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile10), 50, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile01), 0, 50, null);
 		g.drawImage(w.getEntityManager().getImage(Tile11), 50, 50, null);
 	}
 	public boolean isGenerated() {
 		return generated;
 	}
 }
