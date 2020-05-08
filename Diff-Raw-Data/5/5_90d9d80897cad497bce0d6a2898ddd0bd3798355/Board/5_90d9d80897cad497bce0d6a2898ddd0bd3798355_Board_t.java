 package com.duckcult.conway.gol;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes.Usage;
 
 public class Board {
 	public static final double PERCENT_ALIVE = 0.5;
 	ArrayList<ArrayList<Cell>> grid;
 	
 	private int height;
 	private int width;
 	/**
 	 * File based constructor.
 	 * assumes the file is a space delimited grid representation where alive is 1, dead is 0.
 	 * @param f
 	 * @throws FileNotFoundException 
 	 */
 	public Board(File f) throws FileNotFoundException {
 		Scanner fileScan = new Scanner(f);
 		grid = new ArrayList<ArrayList<Cell>>();
 		String [] line;
 		ArrayList<Cell>temp;
 		do {
 			line = fileScan.nextLine().split(" ");
 			temp = new ArrayList<Cell>();
 			temp.ensureCapacity(line.length);
 			for (String s: line) {
 				temp.add(new BasicCell(s.equals("1")));
 			}
 			grid.add(temp);
 		}
 		while(fileScan.hasNext());
 		height = grid.size();
 		width = grid.get(0).size();
 	}
 	
 	public Board(int height, int width) {
 		this(height, width, 0);	
 	}
 	
 	private Board(int height, int width, int flag) {
 		initBoard(height,width,flag);
 	}
 	
 	public static Board emptyBoard(int height, int width) {
 		return new Board(height,width,1);
 	}
 	
 	public static Board allLive(int height, int width) {
 		return new Board(height,width,2);
 	}
 
 //simple mutator methods	
 	//private void setWrap(int type){wrap = type;}
 //simple accessor methods
 	public int getWidth(){return width;}
 	public int getHeight(){return height;}
 	public Cell getCell(int x, int y){return grid.get(y).get(x);}
 	
 	private void initBoard(int height, int width, int flag) {
 		grid = new ArrayList<ArrayList<Cell>>(height);
 		for (int i=0; i<height;i++){
 			switch(flag) {
 			case 1:
 				grid.add(emptyRow(width));
 			case 2:
 				grid.add(fullRow(width));
 			default:
 				grid.add(randomRow(width));	
 			}
 		}
 		this.height = height;
 		this.width = width;
 	}
 		
 	private ArrayList<Cell> fullRow (int length) {
 		ArrayList<Cell> temp = new ArrayList<Cell>(length);
 		for (int i=0;i<length;i++)
 			temp.add(new BasicCell(true));
 		return temp;
 	}
 	
 	private ArrayList<Cell> randomRow(int length) {
 		ArrayList<Cell> temp = new ArrayList<Cell>(length);
 		for (int i=0; i<length;i++){
 			temp.add(new BasicCell(Math.random()>PERCENT_ALIVE));
 		}
 		return temp;
 	}
 	
 	private ArrayList<Cell> emptyRow(int length) {
 		ArrayList<Cell> temp = new ArrayList<Cell>(length);
 		for (int i=0;i<length;i++)
 			temp.add(new BasicCell());
 		return temp;
 	}
  	
 	public void advanceBoard() {
 		for (int i = 0; i < grid.size()-1; i++) {
 			grid.set(i, grid.get(i+1));
 		}
 		grid.set(grid.size()-1, randomRow(width));
 	}
 	
 	public void update() {
 		for(int i = 0; i < height; i++) {
 			ArrayList<Cell> temp = grid.get(i);
 			for (int j = 0; j < width; j++) {
 				temp.get(j).check(getNeighbors(j,i));
 			}
 		}
 	}
 	
 	/**
 	 * Returns the neighbors of a cell collapsed into a single array.
 	 * Their positions map to:
 	 * [[0 1 2]
 	 *  [3 _ 4]
 	 *  [5 6 7]]
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	private ArrayList<Cell> getNeighbors(int x, int y) {
 		ArrayList<Cell> temp = new ArrayList<Cell>();
 		if(x>0 && y>0) temp.add(getCell(x-1,y-1));
 		if(x<width-1 && y<height-1)temp.add(getCell(x+1,y+1));
 		if(x>0)	temp.add(getCell(x-1,y));
 		if(y>0) temp.add(getCell(x,y-1));
 		if(x<width-1 && y>0) temp.add(getCell(x+1,y-1)); 
 		if(x<width-1 && y<height-1) temp.add(getCell(x+1,y+1));
 		if(x<width-1) temp.add(getCell(x+1,y));
 		if(y<height-1) temp.add(getCell(x,y+1));
 		return temp;
 	}
 	
 	/**
 	 * Returns a 1D arrayList of meshes of the board for rendering
 	 * the arraylist is ordered left then up.
 	 * @param depth
 	 * @return
 	 */
 	public ArrayList<Mesh> toMeshes(float depth) {
 		ArrayList<Mesh> ret = new ArrayList<Mesh>(height*width);
 		float squareSize = 2f/(float)width;
 		float l = -1;
 		float r = squareSize-1;
 		float t = 2f/(float)height-1;
 		float b = -1;
 		for (int i = 0; i < height; i++) {
 			l = -1;
			r=squareSize-1;
 			for (int j = 0; j < width; j++) {
 				if(getCell(j,i).isAlive()) {
 					Mesh m = new Mesh(true,4,4,
 						new VertexAttribute(Usage.Position,3,"a_position"),
 						new VertexAttribute(Usage.ColorPacked, 4, "a_color"));
				System.out.println("l="+l+" r="+r+" t="+t+" b="+b);
 					m.setVertices(new float[] {l, b, depth, getCell(j,i).getColor().toFloatBits(),
 											   r, b, depth, getCell(j,i).getColor().toFloatBits(),
 											   l, t, depth, getCell(j,i).getColor().toFloatBits(),
 											   r, t, depth, getCell(j,i).getColor().toFloatBits() });
 					m.setIndices(new short[] {0,1,2,3});
 					ret.add(m);
 				}
 				l = r;
 				r += squareSize;
 			}
 			b = t;
 			t+=squareSize;
 			//t += ((float)2)/(float)height;
 		}
 		return ret;
 	}
 	
 	public ArrayList<Mesh> updateMeshes(ArrayList<Mesh> current) {
 		int i = 0;
 		int j = 0;
 		float [] verts = new float[16];
 		for (Mesh m : current) {
 			m.getVertices(verts);
 			verts[3] = getCell(j,i).getColor().toFloatBits();
 			verts[7] = getCell(j,i).getColor().toFloatBits();
 			verts[11] = getCell(j,i).getColor().toFloatBits();
 			verts[15] = getCell(j,i).getColor().toFloatBits();
 			m.setVertices(verts);
 			if(j<width-1)
 				j++;
 			else {
 				j=0;
 				if(i<height-1)
 					i++;
 			}
 		}
 		return current;
 	}
 	
 	
 	//rotates a coordinate on the center of its axis, used in klien bottle
 		private int rotateOnAxis (int coord, int max) {
 			return (max-1)-coord;
 		}
 
 	//basic toString method, doesn't actually work since objectification the toStrings for each cell would be wrong
 		public String toString(){
 			String ret = "";
 			for (int i = 0; i < grid.size(); i++){
 				ArrayList<Cell> temp = grid.get(i);
 				for (int j = 0; j < temp.size(); j++){
 					ret = ret + temp.get(j);
 				}
 				ret = ret + "\n";
 			}
 			return ret;
 		}
 		
 	//a method that checks to see if everything is dead and returns true if so
 		public boolean extinct(){
 			for (int i = 0; i < grid.size(); i++){
 				ArrayList<Cell> temp = grid.get(i);
 				for (int j =0; j < temp.size(); j++){
 					if (temp.get(j).isAlive())
 						return false;
 				}
 			}
 			return true;
 		}
 		
 		public void setCells(int x, int y, Cell[][] cells) {
 			for (int i = 0; i < cells.length; i++) {
 				for (int j = 0; j < cells[0].length; j++) {
 					if(cells[i][j]!=null){
 						grid.get(y).set(x, cells[i][j]);
 					}
 					x++;
 				}
 				y++;
 			}
 		}
 		
 		public boolean checkCells(int x, int y, Cell[][]cells) {
 			for (int i = 0; i < cells.length; i++) {
 				for (int j = 0; j < cells[0].length; j++) {
 					if(cells[i][j]!=null && grid.get(x).get(y).isAlive())
 						return true;
 					x++;
 				}
 				y++;
 			}
 			return false;
 		}
 		
 		public ArrayList<ArrayList<Cell>> getSubgrid(int x, int y, int width, int height) {
 			ArrayList<ArrayList<Cell>> ret = new ArrayList<ArrayList<Cell>> (height);
 			for (int i = y; i < height; i++) {
 				ret.add((ArrayList<Cell>) grid.get(i).subList(x, x+width));
 			}
 			return ret;
 		}
 	
 	
 //sets up the board for the closed box setting.
 	/*private void initBoardBox(int pRow, int pCol, int ratio) {
 		int temp;
 		this.row = pRow+2;
 		this.col = pCol+2;
 		grid = new Cell [row][col];
 		for (int i =0; i < row; i++){
 			for (int j = 0; j < col; j++){
 				if (j==0 || j == col-1 || i==0 || i==row-1)
 					grid[i][j] = new WallCell();
 				else{
 					temp = Math.abs(fate.nextInt()%100);
 					if (temp < ratio)	
 						grid[i][j] = new BasicCell (true, CellProfile.getRandomProfile());
 					else
 						grid[i][j] = new BasicCell (false, CellProfile.getRandomProfile());
 				}
 			}
 		}
 	}
 
 	private void initBoardGlobe(int pRow, int pCol, int ratio) {
 		int temp;
 		this.row = pRow;
 		this.col = pCol+2;
 		grid = new Cell [row][col];
 		for (int i =0; i < row; i++){
 			for (int j = 0; j < col; j++){
 				if (i==0 || i==row-1)
 					grid[i][j] = new WallCell();
 				else{
 					temp = Math.abs(fate.nextInt()%100);
 					if (temp < ratio)	
 						grid[i][j] = new BasicCell (true, CellProfile.getRandomProfile());
 					else
 						grid[i][j] = new BasicCell (false, CellProfile.getRandomProfile());
 				}
 			}
 		}
 	}
 
 	private void initBoardNoWalls(int pRow, int pCol, int ratio) {
 		int temp;
 		this.row = pRow;
 		this.col = pCol;
 		grid = new Cell [row][col];
 		for (int i =0; i < row; i++){
 			for (int j = 0; j < col; j++){
 				temp = Math.abs(fate.nextInt()%100);
 				if (temp < ratio)	
 					grid[i][j] = new BasicCell (true, CellProfile.getRandomProfile());
 				else
 					grid[i][j] = new BasicCell (false, CellProfile.getRandomProfile());
 			}
 		}
 	}*/
 //
 /*controls a single turn iteration	
 	public void turn(){
 		switch (wrap){
 			case 0:
 				for (int i = 1; i < row-1; i++){
 					for (int j = 1; j < col-1;j++){
 						grid[i][j].check(getNeighbors(i,j));
 					}
 				}
 				break;
 			case 1:
 				for (int i = 1; i < row-1; i++){
 					for (int j = 0; j < col;j++){
 						grid[i][j].check(getNeighbors(i,j));
 					}
 				}
 				break;
 			case 2:
 			case 3:
 				for (int i = 1; i < row; i++) {
 					for(int j = 0; j < col; j++) {
 						grid[i][j].check(getNeighbors(i,j));
 					}
 				}
 			default:
 				for (int i = 1; i < row-1; i++){
 					for (int j = 1; j < col-1;j++){
 						grid[i][j].check(getNeighbors(i,j));
 					}
 				}
 				break;
 		}
 		return;
 	}*/
 
 /*collects a 2D array of neighbor Cells to pass to the check method
 	private Cell[][] getNeighbors(int cRow,int cCol) {
 		Cell[][] neighbors = new Cell[3][3];
 		switch (wrap){
 			case 0:
 				neighbors = getNeighborsBox(cRow, cCol,neighbors);
 				break;
 			case 1:
 				neighbors = getNeighborsGlobe(cRow, cCol, neighbors);
 				break;
 			case 2:
 				neighbors = getNeighborsTorus(cRow, cCol, neighbors);
 				break;
 			case 3:
 				neighbors = getNeighborsKlein(cRow, cCol, neighbors);
 				break;
 			default:
 				neighbors = getNeighborsBox(cRow,cCol,neighbors);
 		}
 		return neighbors;
 	}*/
 /*
 //collects the neighbors of a cell in the closed box wrap setting, assumes a dead cell buffer
 	private Cell[][] getNeighborsBox(int cRow,int cCol, Cell[][] neighbors) {
 		neighbors[0][0] = grid[cRow-1][cCol-1];
 		neighbors[0][1] = grid[cRow-1][cCol];
 		neighbors[0][2] = grid[cRow-1][cCol+1];
 		neighbors[1][0] = grid[cRow][cCol-1];
 		neighbors[1][2] = grid[cRow][cCol+1];
 		neighbors[2][0] = grid[cRow+1][cCol-1];
 		neighbors[2][1] = grid[cRow+1][cCol];
 		neighbors[2][2] = grid[cRow+1][cCol+1];
 		return neighbors;
 	}
 
 //collects the neighbors of a cell for the globe wrap setting
 	private Cell[][] getNeighborsGlobe(int cRow,int cCol, Cell[][] neighbors) {
 		int preRow = cRow-1;
 		int nexRow = cRow+1;
 		int preCol = (cCol==0 ? col-1 : cCol-1);
 		int nexCol = (cCol==col-1 ? 0 : cCol+1);
 		neighbors[0][0] = grid[preRow][preCol];
 		neighbors[0][1] = grid[preRow][cCol];
 		neighbors[0][2] = grid[preRow][nexCol];   
 		neighbors[1][0] = grid[cRow][preCol];   
 		neighbors[1][2] = grid[cRow][nexCol];  
 		neighbors[2][0] = grid[nexRow][preCol];  
 		neighbors[2][1] = grid[nexRow][cCol]; 
 		neighbors[2][2] = grid[nexRow][nexCol]; 
 		return neighbors;
 	}
 
 //collects the neighbors of a cell for the torus wrap setting
 	private Cell[][] getNeighborsTorus(int cRow, int cCol, Cell[][] neighbors) {
 		int preRow = (cRow==0 ? row-1 : cRow-1);
 		int preCol = (cCol==0 ? col-1 : cCol-1);
 		int nexRow = (cRow==row-1 ? 0 : cRow+1);
 		int nexCol = (cCol==col-1 ? 0 : cCol+1);
 		neighbors[0][0] = grid[preRow][preCol];
 		neighbors[0][1] = grid[preRow][cCol];
 		neighbors[0][2] = grid[preRow][nexCol];   
 		neighbors[1][0] = grid[cRow][preCol];   
 		neighbors[1][2] = grid[cRow][nexCol];  
 		neighbors[2][0] = grid[nexRow][preCol];  
 		neighbors[2][1] = grid[nexRow][cCol]; 
 		neighbors[2][2] = grid[nexRow][nexCol]; 
 		return neighbors;
 	}                         
 
 //collects the neighbors of a cell for the klein bottle wrap setting
 	private Cell[][] getNeighborsKlein(int cRow, int cCol, Cell[][] neighbors) {
 		int nexRow = (cRow==row-1 ? 0 : cRow+1);
 		int preRow = (cRow==0 ? row-1 : cRow-1);
 		if (nexRow==0 || preRow==row-1)
 			cCol = rotateOnAxis(cCol, col);
 		int preCol = (cCol==0 ? col-1 : cCol-1);
 		int nexCol = (cCol==col-1 ? 0 : cCol+1);
 		neighbors[0][0] = grid[preRow][preCol];
 		neighbors[0][1] = grid[preRow][cCol];
 		neighbors[0][2] = grid[preRow][nexCol];   
 		neighbors[1][0] = grid[cRow][preCol];   
 		neighbors[1][2] = grid[cRow][nexCol];  
 		neighbors[2][0] = grid[nexRow][preCol];  
 		neighbors[2][1] = grid[nexRow][cCol]; 
 		neighbors[2][2] = grid[nexRow][nexCol]; 
 		return neighbors;
 	}
 */
 
 
 }
