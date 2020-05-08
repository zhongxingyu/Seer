 package fr.frozen.iron.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 
 import fr.frozen.iron.common.IronWorld;
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.common.equipment.Weapon;
 import fr.frozen.iron.protocol.Protocol;
 
 public class IronUtil {
 
 	public static final String ORC = "orc";
 	public static final String ELF = "elf";
 	public static final String HUMAN = "human";
 	public static final String DWARF = "dwarf";
 	public static final String UNDEAD = "undead";
 	
 	public static String getIronDirPath() {
 		return System.getProperty("user.home") 
 		+ System.getProperty("file.separator")
 		+ IronConst.ironDir;
 	}
 	
 	public static String getSaveFilePath() {
 		return System.getProperty("user.home") 
 		+ System.getProperty("file.separator")
 		+ IronConst.ironDir
 		+ System.getProperty("file.separator")
 		+ IronConst.saveFile;
 	}
 	
 	public static String findName() {
 		String name = findOptionValue("username");
 		if (name == null) return IronConst.defaultName;
 		return name;
 	}
 	
 	
 
 	public static int getDamage(IronUnit src, IronUnit dst, boolean meleeAttack) {
 		Weapon weapon = meleeAttack ? src.getMeleeWeapon() : src.getRangedWeapon();
 		if (weapon == null) return 0;
 		float damage = weapon.getDamage();
 		float armorValue = 0;
 		
 		
 		if (weapon.isMagical()) {
 			damage += damage * (float)(src.getStats().getIntelligence() / 10.0);
 		} else { //physical
 			if (meleeAttack) {
 				damage += damage * (float)(src.getStats().getStrength() / 10.0);
 			} else {
 				damage += damage * (float)(src.getStats().getAgility() / 10.0);
 			}
 		}
 		
 		if (dst.getShield() != null) {
 			armorValue += weapon.isMagical() ? dst.getShield().getMagicalArmor() :
 											   dst.getShield().getPhysicalArmor();
 		}
 		if (dst.getArmor() != null) {
 			armorValue += weapon.isMagical() ? dst.getArmor().getMagicalArmor() :
 											   dst.getArmor().getPhysicalArmor();
 		}
 		
 		damage -= damage * (float)(armorValue / 100.0);
 		float movePenalty = (float)src.getMovement() / src.getMaxMovement();
 		
 		damage *= movePenalty;
 		
 		return (int)damage;
 	}
 	
 	public static String findOptionValue(String optionName) {
 		String option = null;
 		String filePath = getSaveFilePath();
 		File saveFile = new File(filePath);
 		if (saveFile == null || !saveFile.exists() || saveFile.isDirectory()) return null;
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(saveFile));
 			String line = null;
 			optionName += "=";
 			while ((line = br.readLine()) != null) {
 				if (line.length() > optionName.length() && line.startsWith(optionName)) {
 					option = line.substring(optionName.length(), line.length());
 					break;
 				}
 			}
 			br.close();
 		} catch (Exception e) {
 			System.out.println("error when trying to find "+option+"  : "+e.getLocalizedMessage());
 		}
 		
 		return option;
 	}
 	
 	public static void saveName(String name) {
 		saveOptionValue("username", name);
 	}
 	
 	public static void saveOptionValue(String option, String value) {
 		String filePath = getSaveFilePath();
 		String dirPath = getIronDirPath();
 		
 		option += "=";
 		
 		try {
 			File dir = new File(dirPath);
 			if (!dir.exists()) {
 				dir.mkdir();
 			}
 
 			File saveFile = new File(filePath);
 			if (!saveFile.exists()) {
 				saveFile.createNewFile();
 			}
 
 			BufferedReader br;
 			br = new BufferedReader(new FileReader(saveFile));
 			String text ="";
 			String line = null;
 			while ((line = br.readLine()) != null) {
 				text += line + "\r\n";
 			}
 			if (text.contains(option)) {
 				text = text.replaceAll(option+".*", option+value);
 			} else {
 				text += option+value;
 			}
 
 			BufferedWriter bw;
 			bw = new BufferedWriter(new FileWriter(saveFile));
 			bw.write(text);
 			bw.flush();
 			bw.close();
 		}catch (Exception e) {
 			System.out.println("error in saveOption when saving "+option+value+ " :"+e.getLocalizedMessage());
 		}
 	}
 	
 	public static String getRaceStr(Protocol race) {
 		switch(race) {
 		case ORC_RACE :
 			return ORC;
 		case ELF_RACE :
 			return ELF;
 		case HUMAN_RACE :
 			return HUMAN;
 		case DWARF_RACE :
 			return DWARF;
 		case UNDEAD_RACE :
 			return UNDEAD;
 		}
 		return null;
 	}
 	
 	public static List<Vector2f> getHorizontalIntersections(IronWorld world, int x1, int y1, int x2, int y2, double angle) {
 		List<Vector2f> points = new ArrayList<Vector2f>();
 		
 		int Py = (int)(y1 / IronConst.TILE_HEIGHT);
 		if (y2 - y1 >= 0) Py ++;
 		Py *= IronConst.TILE_HEIGHT;
 
 		int nbIntersections = (int)(y2/ IronConst.TILE_HEIGHT) - (int)(y1 / IronConst.TILE_HEIGHT);
 		nbIntersections = Math.abs(nbIntersections);
 		
 		int side = 1;
 		if (y2 - y1 < 0) {
 			side = - side;
 		}
 		
 		points.add(new Vector2f(x1, y1));
 		
 		for (int i = 0; i <  nbIntersections; i++) {
 			Vector2f ref1 = new Vector2f(0, Py);
 			Vector2f ref2 =  new Vector2f(IronConst.MAP_WIDTH * IronConst.TILE_WIDTH - 1, Py);
 			
 			Vector2f P = IronUtil.getIntersectionPoint(new Vector2f(x1, y1), new Vector2f(x2, y2), ref1,ref2);
 			//P.setY(Py);//theres a bug where i get 255.99998 and it screws up the checkGrid part
 			Py += IronConst.TILE_HEIGHT * side;
 			
 			if (P == null) break;
 			points.add(P);
 		}
 		
 		points.add(new Vector2f(x2, y2));
 		
 		return points;
 	}
 	
 	
 	public static boolean checkGrid(IronWorld world, int x1, int y1, int x2, int y2) {
 		Vector2f A,B;
 		int Px, nbIntersections, squareX, squareY;
 		float tmpX, tmpY;
 		Vector2f vec = new Vector2f(x2 - x1, y2 - y1);
 		double angle = IronUtil.getAngle(vec, new Vector2f(1,0));
 		List<Vector2f> points = getHorizontalIntersections(world, x1, y1, x2, y2, angle);
 			
 		main : for (int i = 1; i < points.size(); i++) {
 			A = points.get(i - 1);
 			B = points.get(i);
 			
 			Px = (int)(A.getX() / IronConst.TILE_WIDTH);
 			if (B.getX() - A.getX() >= 0) Px ++;
 			Px *= IronConst.TILE_HEIGHT;
 			
 			nbIntersections = (int)(B.getX() / IronConst.TILE_WIDTH) - (int)(A.getX() / IronConst.TILE_WIDTH);
 			nbIntersections = Math.abs(nbIntersections);
 			
 			int side = 1;
 			if (B.getX() - A.getX() < 0) {
 				side = - side;
 			}
 			
 			tmpX = A.getX();
 			tmpY = A.getY();
 			
 			for (int j = 0; j < nbIntersections; j++) {
 				Vector2f P = null, ref1, ref2;
 				
 				while (P == null &&  Px >= 0 && Px < IronConst.MAP_WIDTH * IronConst.TILE_WIDTH) {
 					ref1 = new Vector2f(Px, 0);
 					ref2 =  new Vector2f(Px, IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT - 1);
 					P = IronUtil.getIntersectionPoint(A, B, ref1,ref2);
 					
 					//if (P != null)
 						//P.setX(Px);//theres a bug where i get 255.99998 and it screws up the checkGrid part
 					Px += IronConst.TILE_WIDTH * side;
 				}
 				
 				if (P == null) break;
 				
 				squareX =(int) Math.min(P.getX() / IronConst.TILE_WIDTH, tmpX / IronConst.TILE_WIDTH);
 				squareY =(int) Math.min(P.getY() / IronConst.TILE_HEIGHT, tmpY / IronConst.TILE_HEIGHT);
 				
 
 				if (P.getX() == tmpX && P.getY() == tmpY) {
 					tmpX = P.getX();
 					tmpY = P.getY();
 					continue;
 				}
 
 				tmpX = P.getX();
 				tmpY = P.getY();
 				
 				
 				if (squareX < 0 || squareX >= IronConst.MAP_WIDTH || squareY < 0 || squareY >= IronConst.MAP_HEIGHT)
 					break main;
 				
 				if (squareX == x1 / IronConst.TILE_WIDTH && squareY == y1 / IronConst.TILE_HEIGHT) continue;
 				if (squareX == x2 / IronConst.TILE_WIDTH && squareY == y2 / IronConst.TILE_HEIGHT) break main;
 				
 				if (!world.getMap().getTile(squareX, squareY).canShootOver()) {
 					return false;
 				}
 				
 			}
 			
 			squareX =(int) Math.min(B.getX() / IronConst.TILE_WIDTH, tmpX / IronConst.TILE_WIDTH);
 			squareY =(int) Math.min(B.getY() / IronConst.TILE_HEIGHT, tmpY / IronConst.TILE_HEIGHT);
 			
 			if (squareX < 0 || squareX >= IronConst.MAP_WIDTH || squareY < 0 || squareY >= IronConst.MAP_HEIGHT)
 				break main;
 			
 			if (squareX == x1 / IronConst.TILE_WIDTH && squareY == y1 / IronConst.TILE_HEIGHT) continue;
 			if (squareX == x2 / IronConst.TILE_WIDTH && squareY == y2 / IronConst.TILE_HEIGHT) break main;
 			
 			if (!world.getMap().getTile(squareX, squareY).canShootOver()) {
 				return false;
 			}
 		}//end for main
 		
 		return true;
 	}
 	
 	
 	
 	
 	public static double getAngle(Vector2f vec) { //returns angle between vec and (0,-1)
 		double angle = Math.atan2(vec.getY(), vec.getX()) * 57.2957795f;
 		angle += 90;
 		return angle;
 	}
 	
 	
 	public static double getAngle(Vector2f vec1, Vector2f vec2) {
 		/*
 		 * oldversion :
 		 * 
 		 * double distanceV1 = IronUtil.distance(0, 0, (int)vec1.getX(), (int)vec1.getY());
 		double distanceV2 = IronUtil.distance(0, 0, (int)vec2.getX(), (int)vec2.getY());
 		
 		double angle = Math.acos((vec1.getX() * vec2.getX() + vec1.getY() * vec2.getY()) / (distanceV1 * distanceV2));//angle between vec and (0,-1)
 		*/
 		double distanceV1 = vec1.length();
 		double distanceV2 = vec2.length();
 		if (distanceV1 == 0 || distanceV2 == 0) return 0;
 		double angle = Math.acos(Vector2f.dot(vec1, vec2) / (distanceV1 * distanceV2));
 		return angle;
 	}
 	
 	public static double distance(float x1, float y1, float x2, float y2) {
 		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) *(y2 - y1));
 	}
 	
 	public static double getLength(Vector2f vec1, Vector2f vec2) {
 		return distance(vec1.getX(), vec1.getY(), vec2.getX(), vec2.getY());
 	}
 	
 	public static Vector2f getIntersectionPoint(Vector2f A, Vector2f B, Vector2f C, Vector2f D) {
 		
 		Vector2f I = new Vector2f(B.getX() - A.getX(), B.getY() - A.getY()); 
 		Vector2f J = new Vector2f(D.getX() - C.getX(), D.getY() - C.getY());
 		
 		double denom = I.getX() * J.getY() - I.getY() * J.getX();
 		if (denom == 0) {
 			return null; //parallel
 		}
 		
 		double m = - (-I.getX() * A.getY() + I.getX() * C.getY() + I.getY() * A.getX() - I.getY() * C.getX()) / denom;
 		double k = - (A.getX() * J.getY() - C.getX() * J.getY() - J.getX() * A.getY() + J.getX() * C.getY()) / denom; 
 
 		//m = -(-Ix*Ay+Ix*Cy+Iy*Ax-Iy*Cx)/(Ix*Jy-Iy*Jx)
 		//k = -(Ax*Jy-Cx*Jy-Jx*Ay+Jx*Cy)/(Ix*Jy-Iy*Jx)
 		
 		
 		if (m <= 0 || m >= 1 || k <= 0 || k >= 1) {
 			return null;//not intersection on segment portion
 		}
 		
 		Vector2f P = new Vector2f((float)(A.getX() + k * I.getX()),
 								  (float)(A.getY() + k * I.getY()));
 		
 		return P;
 	}
 	
 	public static byte[] intToByteArray(int val) {
 		byte [] data = new byte[4];
 		
 		for (int i = 3; i >= 0; i--) {
 			data[i] = (byte) (val & 0x000000ff);
 			val >>= 8;
 		}
 		return data;
 	}
 	
 	public static int byteArrayToInt(byte []array) {
 		if (array.length != 4) {
 			System.err.println("wrong array length cannot parse it to int");
 			return 0;
 		}
 		int val = 0;
 		for (int i = 0; i < 4; i++) {
 			val |= (array [i] & 0x000000ff);
 			if (i < 3)
 				val <<= 8; 
 		}
 		return val;
 	}
 	
 	public static byte[] readToArray(DataInputStream is, int length) throws IOException {
 		byte [] data = new byte[length];
 		int pos = 0;
 		int read;
 		int totalRead = 0;
 		
 		while ((read = is.read(data, pos, length - totalRead)) > 0) {
 			totalRead += read;
 			pos += read;
 			
 			if (totalRead == length) break;
 		}
 		if (totalRead != length) throw new IOException("problem not as many bytes as expected");
 		
 		return data;
 	}
 	
 	public static void main(String []args) {
 		int val = 29856162;
 		byte [] barray  = intToByteArray(val);
 		if (val == byteArrayToInt(barray)) {
 			System.out.println("ok " + val);
 		}
 		else {
 			System.out.println("error"+ byteArrayToInt(barray));
 		}
 		
 		/*saveName("pauljohnson");
 		System.out.println(findName());*/
 		
 	}
 }
