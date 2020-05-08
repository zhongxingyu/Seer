 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.pavo;
 
 import maximusvladimir.dagen.Perlin;
 import maximusvladimir.dagen.Rand;
 
 //import com.jpii.navalbattle.data.Constants;
 
 /**
  * Procedural-layer map generator for Pavo
  * @author maximusvladimir
  *
  */
 public class ProceduralLayeredMapGenerator {
 	private static $JSNAO9JW10SKJF194OI[] json;
 	private static $kdOWj20Janro2[] barracades;
 	private static Rand rand = Game.Settings.rand;
 	public static final int RIVERSIZE = 512;
 	static {
 		doInit();
 	}
 	private static void doInit() {
 		berlin = new Perlin(Game.Settings.seed,0,0);
		json = new $JSNAO9JW10SKJF194OI[60];
 		for (int c = 0; c < json.length; c++) {
 			json[c] = new $JSNAO9JW10SKJF194OI(PavoHelper.getGameWidth(WorldSize.WORLD_LARGE)*32,
 					PavoHelper.getGameHeight(WorldSize.WORLD_LARGE)*32);
 		}
 		/*barracades = new $kdOWj20Janro2[20];
 		for (int c = 0; c < barracades.length; c++) {
 			$kdOWj20Janro2 h = new $kdOWj20Janro2();
 			int x2 = 0;
 			int z2 = 0;
 			int s = rand.nextInt(5,10);
 			boolean flag = false;
 			while (!flag) {
 				int[] mem = pSnwonUJa();
 				x2 = mem[0];
 				z2 = mem[1];
 				//System.out.println("x2="+x2+"z2="+z2);
 				//if (getPoint(x2-s,z2-s) > 0.2 && getPoint(x2+s,z2-s) > 0.2
 					//	&& getPoint(x2+s,z2+s) > 0.2 && getPoint(x2-s,z2+s) > 0.2) {
 					flag = true;
 				//}
 			}
 			h.x = x2;
 			h.z = z2;
 			h.size = (byte)s;
 			barracades[c] = h;
 		}*/
 	}
 	private static int[] pSnwonUJa() {
 		int x2 = 0, z2 = 0;
 		while (x2 <= 12 || z2 <= 12 || getPoint(x2,z2) < 0.5) {
 			x2 = rand.nextInt(0, (PavoHelper.getGameWidth(WorldSize.WORLD_LARGE)*32)-12);
 			z2 = rand.nextInt(0, (PavoHelper.getGameHeight(WorldSize.WORLD_LARGE)*32)-12);
 		}
 		int[] c = new int[2];
 		c[0] = x2;
 		c[1] = z2;
 		return c;
 	}
 	private static Perlin berlin;
 	public static byte getValidHouse(int x, int z) {
 		/*for (int bjwI = 0; bjwI < barracades.length; bjwI++) {
 			$kdOWj20Janro2 p = barracades[bjwI];
 			if (x == p.x && z == p.z)
 				return p.size;
 		}*/
 		return 0;
 	}
 	public static float getPoint(float x, float z) {
 		float lvl0 = getLevel0(x,z);
 		//float lvl1 = getLevel1(x,z);
 		float lvl2 = getLevel2(x,z);
 		float lvl3 = getLevel3(x,z);
 		float lvl4 = getLevel4(x,z);
 		//float lvl5 = getLevel5(x,z);
 		//float lvl6 = getLevel6(x,z);
 		//float lvl7 = getLevel7(x,z);
 		float mixer = ((lvl0*25.0f)+(lvl4*5)+(lvl2*2.5f)+(lvl3*2.5f)) / 38.0f;//(lvl1*20.0f)+(lvl2*5.0f)+(lvl3*3.0f)+
 				//(lvl4*5.0f)+(lvl5*5.0f)+(lvl6*1.5f)-(lvl7*12.0f))/98.5f;
 		float mixed = ((mixer+1)/2.0f)-0.1f;
 		
 		if (mixed > 0.57)
 			mixed += 0.28;
 		
 		/*if (blitRiver(x,z)){
 			mixed = getLevel2(mixer,mixed+z)+0.2f;
 			//if (mixed < 0.0f)
 				mixed = 0;
 			//if (mixed > 0.4f)
 				//mixed = 0.4f;
 		}*/
 		
 		float res = (float) ((mixed - 0.3)/0.21);
 		if (res > 1)
 			res = 1;
 		if (res < 0)
 			res = 0;
 		
 		if (blitRiver(x,z) && res > 0.4){
 			//mixed = getLevel2(mixer,mixed+z)+0.2f;
 			res = res - 0.4f;
 			if (res > 1)
 				res = 1;
 			if (res < 0)
 				res = 0;
 		}
 		
 		return res;
 	}
 	private static float ld0 = 1024;
 	private static float ld1 = 128;
 	private static float ld2 = 32;
 	private static float ld3 = 64;
 	private static float ld4 = 512;
 	private static float ld5 = 1024;
 	private static float ld7 = 8196;
 	private static boolean blitRiver(float x, float z) {
 		for (int v = 0; v < json.length; v++) {
 			int cx = (int) (x - json[v].TInaOAJNqi0930142);
 			int cy = (int) (z - json[v].TIXXXXX93jOfna91);
 			if (cx < RIVERSIZE && cy < RIVERSIZE && cx >= 0 && cy >= 0) {
 				return json[v].c(cx,cy);
 			}
 		}
 		return false;
 	}
 	private static float getLevel7(float x, float z) {
 		return berlin.noise(x/ld7,z/ld7);
 	}
 	private static float getLevel0(float x, float z) {
 		return berlin.noise(x/ld0, z/ld0);
 	}
 	private static float getLevel6(float x, float z) {
 		return berlin.noise(x,z);
 	}
 	private static float getLevel1(float x, float z) {
 		return berlin.noise(x/ld1,z/ld1);
 	}
 	private static float getLevel2(float x, float z) {
 		return berlin.noise(x/ld2,z/ld2);
 	}
 	private static float getLevel3(float x, float z) {
 		return berlin.noise(x/ld3,z/ld3);
 	}
 	private static float getLevel4(float x, float z) {
 		return berlin.noise(x/ld4,z/ld4);
 	}
 	private static float getLevel5(float x, float z) {
 		return berlin.noise(x/ld5,z/ld5);
 	}
 }
 class $kdOWj20Janro2 {
 	public int x, z;
 	public byte size;
 	public $kdOWj20Janro2() {
 		
 	}
 }
 class $JSNAO9JW10SKJF194OI {
 	private Rand r;
 	public boolean[][] ASOGLICAL_9201;
 	public int TInaOAJNqi0930142, TIXXXXX93jOfna91;
 	public $JSNAO9JW10SKJF194OI(int LEEsiILIE, int PLwmajwifKW) {
 		____b(PLwmajwifKW,LEEsiILIE);
 		try {
 			Thread.sleep(1);
 		} catch (Throwable t) {
 		}
 		a();
 	}
 	public void ____b(int UJ4DNw92IF34JAOfn29jnr0n, int JFNaoiwu2OAnq29nf) {
 		r = Game.Settings.rand;
 		TInaOAJNqi0930142 = r.nextInt(0,JFNaoiwu2OAnq29nf);
 		TIXXXXX93jOfna91 = r.nextInt(0,UJ4DNw92IF34JAOfn29jnr0n);
 		ASOGLICAL_9201 = new boolean[ProceduralLayeredMapGenerator.RIVERSIZE][ProceduralLayeredMapGenerator.RIVERSIZE];
 	}
 	private void a() {
 		int lastx = ProceduralLayeredMapGenerator.RIVERSIZE/2;
 		int lasty = ProceduralLayeredMapGenerator.RIVERSIZE/2;
 		for (int y = 0; y < (ProceduralLayeredMapGenerator.RIVERSIZE/3)*2; y++) {
 			int dx = -1;
 			while (dx < 0 || dx >= ProceduralLayeredMapGenerator.RIVERSIZE)
 				dx = lastx+r.nextInt(-1,3);
 			int dy = -1;
 			while (dy < 0 || dy >= ProceduralLayeredMapGenerator.RIVERSIZE)
 				dy = lasty+r.nextInt(-1,3);
 			f(dx,dy);
 			lastx = dx;
 			lasty = dy;
 		}
 	}
 	private void f(int cx, int cy) {
 		ASOGLICAL_9201[cx][cy] = true;
 		if (cx >= 2 && cx < ProceduralLayeredMapGenerator.RIVERSIZE - 2 && cy >= 2 && cy < ProceduralLayeredMapGenerator.RIVERSIZE - 2) {
 			ASOGLICAL_9201[cx-1][cy-1] = true;
 			ASOGLICAL_9201[cx+1][cy-1] = true;
 			ASOGLICAL_9201[cx-1][cy+1] = true;
 			ASOGLICAL_9201[cx+1][cy+1] = true;
 			ASOGLICAL_9201[cx-1][cy] = true;
 			ASOGLICAL_9201[cx+1][cy] = true;
 			ASOGLICAL_9201[cx][cy+1] = true;
 			ASOGLICAL_9201[cx][cy-1] = true;
 		}
 	}
 	public boolean c(int CKasnaOwn, int USJaimw) {
 		return ASOGLICAL_9201[CKasnaOwn][USJaimw];
 	}
 }
