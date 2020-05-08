 package de.lexi.model;
 
 import java.awt.Dimension;
 import java.util.Random;
 
 
 public class DefaultFieldModel extends AbstractFieldModel {
 
 	private BorderedField tiles;
 	private final int width;
 	private final int height;
 	private final int numberOfBombs;
 
 
 	public DefaultFieldModel(int width, int height, int numberOfBombs) {
 		super();
 		this.width = width;
 		this.height = height;
 		this.numberOfBombs = numberOfBombs;
 
 		initialiseTiles();
 		distributeBombs();
 		computeAndSetNumbers();
 	}
 
 	private void initialiseTiles(){
 		tiles = new BorderedField(width,height);
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				tiles.set(i, j, new Tile(i,j,false,false,false,-1));
 			}
 		}
 	}
 
 	private void distributeBombs() {
 		Random r = new Random();
 		int i = 0;
 		while (i < numberOfBombs) {
 			int x = r.nextInt(width);
 			int y = r.nextInt(height);
 			if (!tiles.get(x,y).hasBomb()) {
 				tiles.get(x,y).setBomb(true);
 				i++;
 			}
 		}
 	}
 
 	private void computeAndSetNumbers(){
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Tile[] neighbours = getNeighbours(i, j);
 				int numBombs = 0;
 				for(Tile n : neighbours){
 					if(n != null && n.hasBomb()){
 						numBombs++;
 					}
 				}
 				tiles.get(i,j).setNumber(numBombs);
 			}
 		}
 	}
 
 	private Tile[] getNeighbours(int x, int y){
 		Tile[] neighbours = new Tile[8];
 		neighbours[0] = tiles.get(x, y-1);
 		neighbours[1] = tiles.get(x+1,y-1);
 		neighbours[2] = tiles.get(x+1, y);
 		neighbours[3] = tiles.get(x+1,y+1);
 		neighbours[4] = tiles.get(x,y+1);
 		neighbours[5] = tiles.get(x-1,y+1);
 		neighbours[6] = tiles.get(x-1,y);
 		neighbours[7] = tiles.get(x-1, y-1);
 		return neighbours;
 	}
 
 	@Override
 	public void flipTile(int x, int y) {
 		if(!isInField(x, y)){
 			throw new IllegalArgumentException("["+x+","+y+"] is out of range.");
 		}else if(!tiles.get(x,y).isFlipped()){
 			tiles.get(x,y).setFlipped(true);
 			fireTileChanged(x, y);
 			if(tiles.get(x,y).hasBomb()){
 				fireBombActivated(x, y);
 			}
 			flipTileRec(x, y);
 		}
 		else {
 			int number = tiles.get(x,y).getNumber(); 
 			if(number != 0 ) {
 				Tile[] neighbours = getNeighbours(x, y);
 				int cntMarks = 0;
 				for(Tile n : neighbours) {
					if(n != null) {
						cntMarks += n.isMarked() ? 1 : 0; 
					}
 				}
 				if(cntMarks == number) {
 					for(Tile n : neighbours) {
						if(n!=null && !n.isMarked()) {
 							n.setFlipped(true);
 							fireTileChanged(n.getX(), n.getY());
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void flipTileRec(int x, int y) {
 		if(tiles.get(x, y).getNumber() == 0) {
 			Tile[] neighbours = getNeighbours(x, y);
 			for(Tile n : neighbours) {
 				if(n!=null && !n.hasBomb() && !n.isFlipped()) {
 					n.setFlipped(true);
 					fireTileChanged(n.getX(), n.getY());
 					flipTileRec(n.getX(), n.getY());
 				}
 			}
 		}
 	}
 
 	@Override
 	public int getNumberOfBombs() {
 		return numberOfBombs;
 	}
 
 	@Override
 	public Dimension getSize() {
 		return new Dimension(width,height);
 	}
 
 	@Override
 	public boolean hasBomb(int x, int y) {
 		if(!isInField(x, y)){
 			throw new IllegalArgumentException("["+x+","+y+"] is out of range.");
 		}
 		return tiles.get(x,y).hasBomb();
 	}
 
 	@Override
 	public boolean isFlipped(int x, int y) {
 		if(!isInField(x, y)){
 			throw new IllegalArgumentException("["+x+","+y+"] is out of range.");
 		}
 		return tiles.get(x,y).isFlipped();
 	}
 
 	@Override
 	public boolean isMarked(int x, int y) {
 		if(!isInField(x, y)){
 			throw new IllegalArgumentException("["+x+","+y+"] is out of range.");
 		}
 		return tiles.get(x,y).isMarked();
 	}
 
 	@Override
 	public void markTile(int x, int y) {
 		if(!isInField(x, y)){
 			throw new IllegalArgumentException("["+x+","+y+"] is out of range.");
 		}else if(!tiles.get(x,y).isFlipped()){
 			tiles.get(x,y).setMarked(true);
 			fireTileChanged(x, y);
 		}
 	}
 
 	@Override
 	public void unmarkTile(int x, int y) {
 		if(!isInField(x, y)){
 			throw new IllegalArgumentException("["+x+","+y+"] is out of range.");
 		}else if(!tiles.get(x,y).isFlipped()){
 			tiles.get(x,y).setMarked(false);
 			fireTileChanged(x, y);
 		}
 	}
 
 	@Override
 	public int getNumber(int x, int y) {
 		if(!isInField(x, y)){
 			throw new IllegalArgumentException("["+x+","+y+"] is out of range.");
 		}else{
 			return tiles.get(x, y).getNumber();
 		}
 	}
 
 	private boolean isInField(int x, int y){
 		if(x < 0 || x > width || y < 0 || y > height){
 			return false;
 		}
 		return true;
 	}
 
 	private class BorderedField{
 		private Tile[][] tiles;
 
 		public BorderedField(int width, int height){
 			tiles = new Tile[width+2][height+2];
 		}
 
 		public void set(int x, int y, Tile t){
 			tiles[x+1][y+1] = t;
 		}
 
 		public Tile get(int x, int y){
 			return tiles[x+1][y+1];
 		}
 	}
 
 
 
 }
