 package cells;
 
 import java.awt.Graphics;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import pxlJam.Crayon;
 
 public class Map {
 	private Cell[][] tiles;
 	private Crayon character;
 
 	private int width;
 	private int height;
 
 	private File file;
 
 	public Map(String filename) {
 		file = new File(filename);
 		readMap();
 	}
 
 	public void readMap() {
 		try {
 			Scanner scan = new Scanner(file);
 
 			width = scan.nextInt();
 			height = scan.nextInt();
 
 			tiles = new Cell[width][height];
 			int tempInt;
 
 			for (int i = 0; i < width; i++) {
 				for (int j = 0; j < height; j++) {
 					if (scan.hasNext()) {
 						tempInt = scan.nextInt();
						if (tempInt == 2)
 							character = new Crayon(i * Cell.CELL_WIDTH, j * Cell.CELL_HEIGHT);
 						else {
							tiles[i][j] = getWalltype(tempInt, i * Cell.CELL_WIDTH, j * Cell.CELL_HEIGHT);
 						}
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Cell getWalltype(int fileNum, int x, int y) {
 		Cell tempCell = null;
 		if (fileNum == 1)
 			tempCell = new Wall(x, y);
 		else if (fileNum == 2) {
 			// something else
 		} else if (fileNum == 0) {
 			tempCell = null;
 		}
 
 		return tempCell;
 	}
 
 	public void draw(Graphics g) {
 		for (int i = 0; i < tiles.length; i++)
 			for (int j = 0; j < tiles[i].length; j++) {
 				if (tiles[i][j] != null) {
 					tiles[i][j].draw(g);
 				}
 			}
 
 		if (character != null) {
 			character.draw(g);
 		}else{
 			System.out.println("Character is null");
 		}
 	}
 
 	public Crayon getCharacter() {
 		return character;
 	}
 
 	public void setCharacter(Crayon character) {
 		this.character = character;
 	}
 	public void setCell(int x, int y, Cell c){
 		tiles[x][y] = c;
 	}
 	
 }
