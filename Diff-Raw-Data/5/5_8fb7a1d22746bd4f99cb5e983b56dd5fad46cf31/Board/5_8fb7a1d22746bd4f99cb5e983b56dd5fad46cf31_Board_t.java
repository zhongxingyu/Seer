 package com.drewi.minesweeper;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import javax.swing.JFrame;
 
 import com.drewi.minesweeper.ButtonDrawer.MinesweeperMouseListener;
 
 public class Board extends JFrame implements MinesweeperMouseListener{
 	
 	public interface GameFinishedListener{
 		public void gameOver();
 	}
 	
 	private static final long serialVersionUID = 9148170605818301711L;
 	
 	private List<MineButton> mButtons;
 	private Set<Integer> mBombPositions;
 
 	private int mRows;
 	private int mColumns;
 	private int mMines;
 	
 	private boolean mIsBoardGenerated;
 	private boolean mGameOver;
 	
 	private GameFinishedListener mListener;
 	
 	public Board(int rows, int columns, int mines) {
 		super("Minesweeper");
 		setLayout(null);
 		
 		mRows = rows;
 		mColumns = columns;
 		mMines = mines;
 
 		ButtonDrawer drawer = new ButtonDrawer();
 		mButtons = drawer.createButtons(mRows, mColumns);
 		drawer.setMinesweeperMouseListener(this);
 		add(drawer);
 		
 		setSize(calculateBoardSize(-10));
 		drawer.setSize(calculateBoardSize(0));
 	}
 	
 	public void setGameFinishedListener(GameFinishedListener listener){
 		mListener = listener;
 	}
 	
 	/**
 	 * Generates the board, placing all the mines.
 	 * @param exclude This position or any of its neighbours will not be a bomb.
 	 */
 	private void generateBoard(int exclude){
 		List<Integer> excludes = getNeighbours(mButtons.get(exclude));
 		excludes.add(exclude);
 		Set<Integer> mineButtons = generateBombs(excludes);
 		
 		for(int i=0; i<mButtons.size(); i++){
 			MineButton button = mButtons.get(i);
 			button.setIsMine(mineButtons.contains(i));
 		}
 		countButtonsMineNeighbours();
 		mIsBoardGenerated = true;
 	}
 	
 	private void countButtonsMineNeighbours(){
 		for(MineButton button : mButtons){
 			if(button.isBomb()){
 				continue;
 			}
 			
 			List<Integer> neighbors = getNeighbours(button);
 			
 			int mineNeighbours = 0;
 			for(int neighbourPos : neighbors){
 				if(mButtons.get(neighbourPos).isBomb()){
 					mineNeighbours++;
 				}
 			}
 			button.setMineNeighbours(mineNeighbours);
 		}
 	}
 	
 	public void revealAllBombs(){
 		for(int position : mBombPositions){
 			if(mButtons.get(position).isBomb()){
 				mButtons.get(position).setClicked(true);
 			}
 		}
 	}
 	
 	/**
 	 * Generates mine positions.
 	 * @param excludes These positions will not be a bomb.
 	 */
 	private Set<Integer> generateBombs(List<Integer> excludes){
 		mBombPositions = new HashSet<Integer>();
 		Random rand = new Random();
 		
 		int nbrOfButtons = mRows * mColumns;
 		while (mBombPositions.size() < mMines) {
 			int randPos = rand.nextInt(nbrOfButtons);
 			if(excludes == null || !excludes.contains(randPos)){
 				mBombPositions.add(randPos);
 			}
 		}
 		
 		return mBombPositions;
 	}
 	
 	private void clickedMineButton(MineButton button){
 		int position = button.getRow()*mColumns + button.getColumn();
 		Set<Integer> checkedPositions = new HashSet<Integer>();
 		Set<Integer> clickPositions = new HashSet<Integer>();
 		checkedPositions.add(position);
 		clickPositions.add(position);
 		
 		if(!button.isBomb() && button.getBombNeighbours() == 0){
 			chainMineButtonClick(position, checkedPositions, clickPositions);
 		}
 		
 		for(int clickPosition : clickPositions){
 			mButtons.get(clickPosition).setClicked(true);
 		}
 		
 		if(mListener != null && button.isBomb()){
 			mListener.gameOver();
 			mGameOver = true;
 		}
 	}
 	
 	private void chainMineButtonClick(int position, Set<Integer> checkedPositions, Set<Integer> clickPositions){
 		MineButton button = mButtons.get(position);
 		List<Integer> neighbours = getNeighbours(button);
 		for(int neighbourPos : neighbours){
 			if(checkedPositions.contains(neighbourPos)){
 				continue;
 			}
 			checkedPositions.add(neighbourPos);
 			MineButton neighbourButton = mButtons.get(neighbourPos);
 			
 			if(neighbourButton.isClickable()){
 				clickPositions.add(neighbourPos);
 				if(neighbourButton.getBombNeighbours() == 0){
 					chainMineButtonClick(neighbourPos, checkedPositions, clickPositions);
 				}
 			}
 			
 		}
 	}
 	
 	private List<Integer> getNeighbours(MineButton button){
 		int row = button.getRow();
 		int column = button.getColumn();
 		int pos = row*mColumns + column;
 		boolean atTop = row == 0;
 		boolean atLeft = column == 0;
 		boolean atRight = column == mColumns-1;
 		boolean atBottom = row == mRows-1;
 		
 		ArrayList<Integer> neighbours = new ArrayList<Integer>();
 		
 		if(!atTop && !atLeft)
 			neighbours.add(pos-mColumns-1);
 		if(!atTop)
 			neighbours.add(pos-mColumns);
 		if(!atTop && !atRight)
 			neighbours.add(pos-mColumns+1);
 		if(!atLeft)
 			neighbours.add(pos-1);
 		if(!atRight)
 			neighbours.add(pos+1);
 		if(!atBottom && !atLeft)
 			neighbours.add(pos+mColumns-1);
 		if(!atBottom)
 			neighbours.add(pos+mColumns);
 		if(!atBottom && !atRight)
 			neighbours.add(pos+mColumns+1);
 		
 		return neighbours;
 	}
 	
 	private Dimension calculateBoardSize(int offset) {
 		int width = mColumns*MineButton.SIZE;
 		int height = mRows*MineButton.SIZE;
 		return new Dimension(width + offset, height + offset);
 	}
 	
 	@Override
 	public void setSize(Dimension dimension) {
 		getContentPane().setPreferredSize(dimension);
 		pack();
 	}
 
 	@Override
 	public void onLeftClick(MineButton button) {
 		if(!mIsBoardGenerated){
 			generateBoard(button.getRow()*mColumns + button.getColumn());
 		}
 		if(!mGameOver && button.isClickable()){
 			clickedMineButton(button);
 		}
 	}
 
 	@Override
 	public void onRightClick(MineButton button) {
 		if(!mGameOver && !button.isClicked()){
 			button.toggleFlag();
 		}
 	}
 
 	@Override
 	public void onDualClick(MineButton button) {
 		if(!mIsBoardGenerated || mGameOver || button.isClickable()){
 			return;
 		}
 		Set<Integer> checkedPositions = new HashSet<Integer>();
 		Set<Integer> clickPositions = new HashSet<Integer>();
 		List<Integer> neighbours = getNeighbours(button);
 		int flags = 0;
 		for(int neighbourPos : neighbours){
 			if(mButtons.get(neighbourPos).isFlagged()){
 				flags++;
 				checkedPositions.add(neighbourPos);
 			}
 		}
 		if(button.getBombNeighbours() == flags){
 			checkedPositions.add(button.getRow()*mColumns+button.getColumn());
 			clickPositions.add(button.getRow()*mColumns+button.getColumn());
 			
 			for(int neighbourPos : neighbours){
 				MineButton neighbourButton = mButtons.get(neighbourPos);
 				if(neighbourButton.getBombNeighbours() == 0 && neighbourButton.isClickable()){
 					if(neighbourButton.isBomb()){
						checkedPositions.add(neighbourPos);
 						clickPositions.add(neighbourPos);
 						mGameOver = true;
 					} else {
						clickPositions.add(neighbourPos);
 						chainMineButtonClick(neighbourPos, checkedPositions, clickPositions);
 					}
 				} else if(!neighbourButton.isFlagged()){
					checkedPositions.add(neighbourPos);
 					clickPositions.add(neighbourPos);
 				}
 			}
 			
 			for(int clickPosition : clickPositions){
 				mButtons.get(clickPosition).setClicked(true);
 			}
 			
 			if(mListener != null && mGameOver){
 				mListener.gameOver();
 			}
 		}
 	}
 	
 }
