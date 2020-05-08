 package com.diycomputerscience.minesweepercore;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class RandomBoardInitializer implements Initializer {
 
 	private Random mineCountRand = new Random();
 	private Random specificMineRand = new Random();
 	
 	@Override
 	public Point[] mines() {
 		List<Point> points = new ArrayList<Point>();
 		
 		for(int row=0; row<Board.MAX_ROWS; row++) {
 			int minesssCount = mineCountRand.nextInt(Board.MAX_COLS/2);
 			points.addAll(getRandonMinesForRow(row, minesssCount));			
 		}
 		Point pointsArr[] = new Point[points.size()];
 		return points.toArray(pointsArr);
 	}
 	
 	private int getMineCountForThisRow() {
 		return specificMineRand.nextInt(Board.MAX_COLS-1);
 	}
 	
 	private List<Point> getRandonMinesForRow(int row, int mines) {
 		List<Point> points = new ArrayList<Point>();
		for(int j=0; j<mines; j++) {
			int col = getMineCountForThisRow(); 
 			boolean addedMine = false;
 			while(!addedMine) {
 				Point mine = new Point(row, col);					
 				if(!points.contains(mine)) {
 					points.add(mine);
 					addedMine = true;
 				}
 			}
 		}
 		return points;
 	}
 
 }
