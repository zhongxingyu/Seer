 package com.araeosia.space;
 
 import com.araeosia.space.util.ChunkPair;
 import com.araeosia.space.util.Planet;
 import org.bukkit.World;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.Vector;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 public class SpaceGenerator extends ChunkGenerator {
 	private HashMap<ChunkPair, ArrayList<Planet>> planets = new HashMap<>();
 	private Space plugin;
 
 	public SpaceGenerator(Space plugin){
 		this.plugin = plugin;
 	}
 
 	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid){
 		ChunkPair cp = new ChunkPair(chunkX, chunkZ);
 		if(!planets.containsKey(cp)){
 			planets.put(cp, new ArrayList<Planet>());
 		}
 		byte[][] result = new byte[world.getMaxHeight() / 16][];
 		ArrayList<Vector> planetsToGenerate = new ArrayList<>();
 		for(int i=0; i<16; i++){
			for(int j=0; j<16; j++){
				for(int k=0; k<world.getMaxHeight(); k++){
 					if(random.nextInt(60000)==1337){
 						planetsToGenerate.add(new Vector(i, j, k));
 					}
 				}
 			}
 		}
 		for(Vector v : planetsToGenerate){
 			int coreRadius = random.nextInt(20)+4;
 			int totalRadius = random.nextInt(20)+coreRadius;
 			int coreMaterial = getMaterial(random);
 			int shellMaterial = getMaterial(random);
 			int coreX = v.getBlockX()+(chunkX*16);
 			int coreZ = v.getBlockZ()+(chunkZ*16);
 			Planet e = new Planet(coreMaterial, coreRadius, shellMaterial, totalRadius, coreX, v.getBlockY(), coreZ);
 			planets.get(cp).add(e);
 		}
 		ArrayList<Planet> planetsToProcess = new ArrayList<Planet>();
 		planetsToProcess.addAll(planets.get(cp));
 		for(int i=-2; i<3; i++){
 			for(int j=-2; j<3; j++){
 				if(!(i==0 && j==0)){
 					ChunkPair toCheck = new ChunkPair(chunkX+i, chunkZ+j);
 					if(planets.get(toCheck)!=null && planets.get(toCheck).size()>0){
 						planetsToProcess.addAll(planets.get(toCheck));
 					}
 				}
 			}
 		}
 		for(Planet p : planetsToProcess){
 			for(int i=0; i<16; i++){
 				int realX = i+(chunkX*16);
 				for(int j=0; j<world.getMaxHeight(); j++){
 					for(int k=0; k<16; k++){
 						int realZ = k+(chunkZ*16);
 						if(p.getVector().distance(new Vector(realX, j, realZ))<=p.getCoreRadius()){
 							setBlock(result, i, j, k, p.getCoreMaterial().byteValue());
 						}else if(p.getVector().distance(new Vector(realX, j, realZ))<=p.getTotalRadius()){
 							setBlock(result, i, j, k, p.getShellMaterial().byteValue());
 						}
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	private int getMaterial(Random r) {
 		int number = r.nextInt(1000);
 		if(number<5){
 			return 56;
 		}else if(number>=5 && number<15){
 			return 14;
 		}else if(number>=15 && number<30){
 			return 15;
 		}else if(number>=30 && number<100){
 			return 1;
 		}else if(number>=100 && number<175){
 			return 10;
 		}else if(number>=175 && number<250){
 			return 8;
 		}else if(number>=250 && number<275){
 			return 16;
 		}else if(number>=275 && number<325){
 			return 25;
 		}else if(number>=325 && number<375){
 			return 7;
 		}else if(number>=375 && number<450){
 			return 89;
 		}else if(number>=450 && number<525){
 			return 45;
 		}else if(number>=525 && number<575){
 			return 73;
 		}else if(number>=575 && number<650){
 			return 0;
 		}else if(number>=650 && number<700){
 			return 85;
 		}else if(number>=700 && number<800){
 			return 3;
 		}else if(number>=800 && number<850){
 			return 98;
 		}else if(number>=850 && number<900){
 			return 48;
 		}else if(number>=900 && number<933){
 			return 49;
 		}else if(number>=933 && number<966){
 			return 47;
 		}else{
 			return 121;
 		}
 	}
 
 	public void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
 		if (result[y >> 4] == null) //is this chunkpart already initialised?
 		{
 			result[y >> 4] = new byte[4096]; //initialise the chunk part
 		}
 		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid; //set the block (look above, how this is done)
 	}
 
 	public HashMap<ChunkPair, ArrayList<Planet>> getPlanets() {
 		return planets;
 	}
 }
