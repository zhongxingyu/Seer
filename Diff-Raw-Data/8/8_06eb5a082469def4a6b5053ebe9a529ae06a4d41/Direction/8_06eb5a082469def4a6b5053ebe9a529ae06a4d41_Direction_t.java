 package com.golddigger.model;
 
 public enum Direction{
 	NORTH,SOUTH,EAST,WEST,NORTH_EAST,SOUTH_EAST,NORTH_WEST,SOUTH_WEST;
 	
 	public static Direction parse(String url){
 		if (url.equalsIgnoreCase(NORTH.toString())) return NORTH;
 		if (url.equalsIgnoreCase(SOUTH.toString())) return SOUTH;
 		if (url.equalsIgnoreCase(EAST.toString())) return EAST;
 		if (url.equalsIgnoreCase(WEST.toString())) return WEST;
 		if (url.equalsIgnoreCase(NORTH_EAST.toString())) return NORTH_EAST;
 		if (url.equalsIgnoreCase(SOUTH_EAST.toString())) return SOUTH_EAST;
 		if (url.equalsIgnoreCase(NORTH_WEST.toString())) return NORTH_WEST;
 		if (url.equalsIgnoreCase(SOUTH_WEST.toString())) return SOUTH_WEST;
 		return null;
 	}
 
 	public boolean isHex() {
 		return this != EAST && this != WEST;
 	}
 	
 
 	public Point2D getOffset(Point2D position){
 		int i = position.y % 2;
 		
 		switch(this){
 		case NORTH: return position.add(-1,0);
 		case SOUTH: return position.add(1,0);
 		case EAST: return position.add(0,1);
 		case WEST: return position.add(0,-1);
		case NORTH_EAST: return position.add(i-1, 1);
		case SOUTH_EAST :return position.add(i, 1);
		case NORTH_WEST: return position.add(i-1, -1);
		case SOUTH_WEST: return position.add(i,-1);
 		default: return position;
 		}
 	}
 }
