 package slug.soc.game;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import slug.soc.game.gameObjects.*;
 
 
 public class TerrianGenerator {
 
 	private TerrainObject[] temperateTerrain = {
 			new TerrainObjectWater(),
 			new TerrainObjectGrassPlain(),
 			new TerrainObjectGrassPlain(),
 			new TerrainObjectForest(),
 			new TerrainObjectGrassPlain(),
 			new TerrainObjectGrassHill()
 	};
 	private TerrainObject[] articTerrain = {
 			new TerrainObjectWater(),
 			new TerrainObjectSnowPlain(),
 			new TerrainObjectSnowHill()
 	};
 
 	private ArrayList<Point> rivers = new ArrayList<Point>();
 
 	public TerrianGenerator(){
 	}
 
 	private TerrainObject[][] constructTerrainMap(int[][] intMap){
 
 		TerrainObject[][] map = new TerrainObject[intMap.length][intMap.length];
 
 		for(int y = 0; y < map.length; y++){
 			for(int x = 0; x < map.length; x++){
 				if(y < map.length/101 || y > map.length - (map.length/101)){
 					try {
 						if(intMap[y][x] < articTerrain.length){
 							map[y][x] = articTerrain[intMap[y][x]].getClass().newInstance();
 						}
 						else{
 							map[y][x] = articTerrain[articTerrain.length - (1 + RandomProvider.getInstance().nextInt(2))].getClass().newInstance();
 						}
 					} catch (InstantiationException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 				}
 				else{
 					try {
 						if(intMap[y][x] < temperateTerrain.length){
 							map[y][x] = temperateTerrain[intMap[y][x]].getClass().newInstance();
 						}
 						else{
 							if(intMap[y][x] > 10){
 								map[y][x] = new TerrainObjectMountain();
 							}
 							else{
 								map[y][x] = temperateTerrain[temperateTerrain.length - (1 + RandomProvider.getInstance().nextInt(3))].getClass().newInstance();
 							}
 						}
 					} catch (InstantiationException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		for(Point p : rivers){
 			map[(int) p.getY()][(int) p.getX()] = new TerrainObjectRiverHorizontal();
 		}
 		generateBiomes(map);
 		return map;
 	}
 
 	private int[][] simulateAnt(int[][] intMap, int cx, int cy, int l){
 		for(int i = 0, c = l;i < c ; i++){
 			if(cy < 0 || cx < 0 || cy > intMap.length -1 || cx > intMap.length -1){
 				cy = RandomProvider.getInstance().nextInt(intMap.length);
 				cx = RandomProvider.getInstance().nextInt(intMap.length);
 			}
 			intMap[cy][cx] += 1;
 			cy += RandomProvider.getInstance().nextInt(3) - 1;
 			cx += RandomProvider.getInstance().nextInt(3) - 1;
 		}
 		return intMap;
 	}
 
 	private int[][] generateIntMap(int w, int h){
 		int[][] intMap = new int[w][h];
 
 		for(int y = 0; y < intMap.length; y++){
 			for(int x = 0; x < intMap.length; x++){
 				intMap[y][x] = 0;
 			}
 		}
 		return intMap;
 	}
 
 	private TerrainObject[][] generateBiomes(TerrainObject[][] map){
 		for(int y = 0; y < map.length; y++){
 			for(int x = 0; x < map.length; x++){
 				if(!map[y][x].isBiome() && (!(map[y][x] instanceof TerrainObjectWater)) ){
 					createBiome(x, y, map,WordGenerator.getInstance().getRandomAdjective());
 				}
 			}
 		}
 		return map;
 	}
 
 	private void smoothTerrain(int[][] intMap){
 		for(int i = 0; i < 2; i++){	
 			for(int y = 0; y < intMap.length; y++){
 				for(int x = 0; x < intMap.length; x++){
 					if(intMap[y][x] == 0){
 						if(x > 0 && y > 0 && x < intMap.length - 1 && y < intMap.length - 1){
 							if((intMap[y + 1][x] > 0 && intMap[y -1][x] > 0) || (intMap[y][x + 1] > 0 && intMap[y][x - 1] > 0)){
 								intMap[y][x] = 1;
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void generateRivers(int[][] intMap){
 		int sY = 39, sX = 60;
 
 		int[][] hMap = new int[intMap.length][intMap.length];
 		for(int y = 0; y < intMap.length; y++){
 			for(int x = 0; x < intMap.length; x++){
 				hMap[y][x] = intMap[y][x];
 				if(intMap[y][x] > temperateTerrain.length){
 					hMap[y][x] = temperateTerrain.length -1;
 				}
 			}
 		}
 		ArrayList<Point> river = new ArrayList<Point>();
 
 		river.add(new Point(sX, sY));
 
 		int i = hMap[sY][sX];
 		int cY = sY, cX = sX;
 
 		boolean finishedBuilding = false;
 
 		while(finishedBuilding == false){
 			ArrayList<Point> possiblePath = new ArrayList<Point>();
			if(hMap[cY + 1][cX] <= hMap[cY][cX] && cY + 1 < hMap.length - 1){
 				possiblePath.add(new Point(cX , cY + 1));
 			}
 			if(hMap[cY - 1][cX] <= hMap[cY][cX] && cY - 1 > -1){
 				possiblePath.add(new Point(cX, cY - 1));
 			}
			if(hMap[cY][cX + 1] <= hMap[cY][cX] && cX + 1 < hMap.length - 1){
 				possiblePath.add(new Point(cX + 1, cY));
 			}
 			if(hMap[cY][cX - 1] <= hMap[cY][cX] && cX - 1 > -1){
 				possiblePath.add(new Point(cX - 1, cY));
 			}
 
 			if(possiblePath.isEmpty()){
 				finishedBuilding = true;
 			}
 
 			ArrayList<Point> updatedPath = new ArrayList<Point>();
 			
 			for(Point p : possiblePath){
 				for(Point rp : river){
 					if(!p.equals(rp)){
 						updatedPath.add(p);
 					}
 				}
 			}
 			possiblePath = updatedPath;
 
 			if(possiblePath.isEmpty()){
 				finishedBuilding = true;
 			}
 
 			if(!finishedBuilding){
 				int r = RandomProvider.getInstance().nextInt(possiblePath.size());
 				river.add(possiblePath.get(r));
 				cY = (int) possiblePath.get(r).getY();
 				cX = (int) possiblePath.get(r).getX();
 			}
 
 		}
 		
 		rivers = river;
 
 	}
 
 	private void createBiome(int x, int y, TerrainObject[][] map, String n){
 		map[y][x].setBiomeString(n + " ");
 		if(y + 1 < map.length -1){
 			if(!map[y + 1][x].isBiome() && map[y + 1][x].getClass() == (map[y][x]).getClass()){
 				createBiome(x, y + 1, map, n);
 			}
 		}
 		if(y - 1 > 0){
 			if(!map[y - 1][x].isBiome() && (map[y - 1][x].getClass() == (map[y][x]).getClass())){
 				createBiome(x, y - 1, map, n);
 			}
 		}
 		if(x + 1 < map.length -1){
 			if(!map[y][x + 1].isBiome() && map[y][x + 1].getClass() == (map[y][x]).getClass()){
 				createBiome(x + 1, y, map, n);
 			}
 		}
 		if(x - 1 > 0){
 			if(!map[y][x - 1].isBiome() && map[y][x - 1].getClass() == (map[y][x]).getClass()){
 				createBiome(x - 1, y, map, n);
 			}
 		}
 	}
 
 	public TerrainObject[][] testGenerateMapAnt(int w, int h){
 
 		int[][] intMap = generateIntMap(w, h);
 
 
 		intMap = simulateAnt(intMap, intMap.length/2, intMap.length/2, 1001);
 
 		return constructTerrainMap(intMap);
 	}
 
 	public TerrainObject[][] testGenerateMapMultiCont(int w, int h){
 
 		int[][] intMap = generateIntMap(w, h);
 
 		for(int n = 0, cy = RandomProvider.getInstance().nextInt(intMap.length), cx = RandomProvider.getInstance().nextInt(intMap.length); n < 4; n++){
 			intMap = simulateAnt(intMap, cx, cy, 2001);
 		}
 
 		smoothTerrain(intMap);
 
 		int avg = 0;
 		int q = 0;
 		int c = 0;
 		for(int y = 0; y < intMap.length; y++){
 			for(int x = 0;x < intMap.length; x++){				//calcualte average tile value.
 				if(intMap[y][x] > 0){
 					avg += intMap[y][x];
 					c++;
 					if(intMap[y][x] > q){
 						q = intMap[y][x];
 					}
 				}
 			}
 		}
 		System.out.println(avg/c);
 		System.out.println(q);
 
 		generateRivers(intMap);
 
 		System.out.println("finish river gen");
 
 		return constructTerrainMap(intMap);
 	}
 
 }
