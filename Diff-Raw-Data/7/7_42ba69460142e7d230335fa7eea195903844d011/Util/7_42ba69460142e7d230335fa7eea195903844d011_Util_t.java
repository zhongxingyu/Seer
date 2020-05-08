 package org.csgames.ai.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Util {
 	
 	public static class Point2D{
 		public final int x;
 		public final int y;
 		
 		public Point2D(int x, int y){ 
 			this.x = x; 
 			this.y = y; 
 		}
 		
 		public boolean equals(Object other){
 			if(!(other instanceof Point2D)) return false;
 			if(other == null) return false;
 			Point2D p = (Point2D) other;
 			return p.x == x && p.y == y;
 		}
 		
 		public String toString(){
 			return String.format("[%d, %d]", x, y);
 		}
 	}
 	
 	public final static String PLAYER_1 = "1";
 	public final static String PLAYER_2 = "2";
 	public final static String PLAYER_3 = "3";
 	public final static String PLAYER_4 = "4";
 	public final static String MYSELF = "Y";
 	public final static String BRICK_WALL = "W";
 	public final static String HARD_WALL = "H";
 	public final static String EMPTY = " ";
 	public final static String BOMB = "B";
 	public final static String EXPLOSION = "E";
 	public final static String SUDDEN_DEATH_ALERT = "A";
 	public final static String POW_UP_BOMB = "b";
 	public final static String POW_UP_RANGE = "r";
 	public final static String POW_UP_DET = "d";
 		
 	private String[][] map;
 	
 	public void updateMap(String[][] map){
 		this.map = map;
 	}
 	
 	public List<Point2D> search(int x, int y, int max, String type){
 		ArrayList<Point2D> list = new ArrayList<Point2D>();
 		
 		int lowBoundX = Math.max(0, x - max);
 		int lowBoundY = Math.max(0, y - max);
 		int hiBoundX = Math.min(map.length, x + max);
 		int hiBoundY = Math.min(map[0].length, y + max);
 		
 		for(int col = lowBoundX; col < hiBoundX; col++){
 			for(int row = lowBoundY; row < hiBoundY; row++){
 				
 				if( map[col][row].equals(type) ){
 					list.add(new Point2D(col, row));
 				}
 			}
 		}
 		
 		return list;
 	}
 	
	public double distance(Point2D first, Point2D second){
		return distance(first.x, first.y, second.x, second.y);
	}
	
 	public double distance(int x1, int y1, int x2, int y2){
 		int dX = x2 - x1;
 		int dY = y2 - y1;
 		return Math.sqrt(dX*dX + dY*dY);
 	}
 	
	public double checkSafety(Point2D p) { return checkSafety(p.x, p.y); }
 	
 	public double checkSafety(int x, int y){
 		return 0.0;
 	}
 	
 	public Point2D getMyLocation(){
 		for(int i = 0; i < map.length; i++)
 			for(int j = 0; j < map[0].length; j++)
 				if(map[i][j].equals("Y")) return new Point2D(i, j);
 		
 		return new Point2D(-1, -1);
 	}
 	
 	public String at(Point2D p) { return at(p.x, p.y); }
 	
 	public String at(int x, int y){
 		return map[x][y];
 	}
 }
