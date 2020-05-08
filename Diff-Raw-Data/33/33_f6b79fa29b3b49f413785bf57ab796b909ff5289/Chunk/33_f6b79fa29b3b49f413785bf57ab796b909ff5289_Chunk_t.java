 package com.jpii.navalbattle.pavo;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 import maximusvladimir.dagen.*;
 
 import com.jpii.navalbattle.data.Constants;
 
 public class Chunk extends Renderable {
 	int x,z;
 	boolean generated = false;
 	static Perlin p = new Perlin(Constants.MAIN_RAND.nextLong(),0,0);
 	public Chunk() {
 		
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
 	public float getPoint(int x, int z) {
 		return (p.noise(x+(this.x*100), z+(this.z*100)));
 	}
 	public void render() {
 		//if (!ready)
 			//return;
 		//ready = false;
 		buffer = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
 		Graphics g = buffer.getGraphics();
 		for (int lsx = 0; lsx < 100; lsx++) {
 			for (int lsz = 0; lsz < 100; lsz++) {
				int opcode = (int)(Math.abs(getPoint(lsx/10,lsz/10))*127);
				//double h = 0;
				//double j = 0;
				//double s = 5;
				//double hn = 1.0/s;
				//double cx = hn * x;
				//double cz = hn * z;
				//h = p.lerp(lsx/100.0, cx,cx+hn);
				//j = p.lerp(lsx/100.0, cz,cz+hn);
				opcode = (int)(((opcode*4.0) + (getPoint(lsx,lsz)*6.0*127)) / 10.0);
				opcode = (int)(((opcode*2.75)+(Math.abs(getPoint(lsx/4,lsz/4)*127.0*2.25)))/10.0);
				opcode = (int)(((opcode*6.0) + (Math.abs(getPoint(lsx/50,lsz/50)*4.0)))/10.0);
				
				opcode += 168;
				//opcode = 30+(int)(Math.abs(p.noise(h, j)*127)+opcode)/2;//110+(int)((opcode * 5) + p.noise(h, j)*95)/100;
				//g.setColor(new Color(opcode,opcode,opcode));
				if (opcode > 255)
					opcode = 255;
				if (opcode < 0)
					opcode = 0;
				if (opcode < 200)
					g.setColor(new Color(0,10,opcode));
				else
					g.setColor(new Color(10,opcode,15));
				g.fillRect(lsx*3,lsz*3,4,4);
 			}
 		}
 		//ready = true;
 		generated = true;
 	}
 	public boolean isGenerated() {
 		return generated;
 	}
 }
