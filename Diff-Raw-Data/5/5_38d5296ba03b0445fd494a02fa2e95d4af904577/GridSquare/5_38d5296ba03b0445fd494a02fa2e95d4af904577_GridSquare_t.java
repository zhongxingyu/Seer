 package com.noobathon.minesweeper.ui;
 
 import java.awt.Color;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 public class GridSquare extends JPanel implements MouseListener
 {
 	private static final long serialVersionUID = 1523739159832757191L;
 	
 	private static final int SQUARE_SIDE_LENGTH = 75;
 	public static final Color NON_ACTIVE_COLOR = Color.LIGHT_GRAY;
     public static final Color CLEARED = Color.WHITE;
     public static final Color FLAGGED_COLOR = Color.BLACK;
     public static final Color BORDER_COLOR = Color.BLACK;
 	
 	private int xCoordinate, yCoordinate;
 	protected MinesweeperGridFrame parentFrame;
 	
 	public static final int BOMB = 1;
 	public static final int EMPTY = 0;
 
     public boolean covered = false;
     public boolean isFlagged = false;
     public boolean inProcessing = false;
 
 	public static GridSquare newGridSquare(int yCoordinate, int xCoordinate, MinesweeperGridFrame parentFrame)
 	{
 		if (BombSquare.shouldBeABomb())
 			return new BombSquare(xCoordinate, yCoordinate, parentFrame);
 		else
 			return new GridSquare(xCoordinate, yCoordinate, parentFrame);
 	}
 
 	
 	public GridSquare(int xCoordinate, int yCoordinate, MinesweeperGridFrame parentFrame)
 	{
 		super();
 
         covered = true;
 
 		this.xCoordinate = xCoordinate;
 		this.yCoordinate = yCoordinate;
 		
 		this.addMouseListener(this);
 		
 		this.parentFrame = parentFrame;
 		setSize(SQUARE_SIDE_LENGTH, SQUARE_SIDE_LENGTH);
 		setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
 		setBackground(NON_ACTIVE_COLOR);
 	}
 
     public int getXCoordinate()
     {
         return xCoordinate;
     }
 
     public int getYCoordinate()
     {
         return yCoordinate;
     }
 
 	public int getSquareType()
 	{
 		return EMPTY;
 	}
 
     public boolean isCovered()
     {
         return covered;
     }

    public boolean isFlagged()
    {
        return isFlagged;
    }
 	
 	public void leftClick() 
 	{
         if (!isFlagged)
 		    uncover();
 	}
 	
 	public void rightClick() 
 	{
         if (!isFlagged && this.getSquareType() == BOMB)
         {
             parentFrame.decrementFlaggedBombs();
         }
         else if (isFlagged && this.getSquareType() == BOMB)
         {
             parentFrame.incrementFlaggedBombs();
         }
         else if (!isFlagged && this.getSquareType() != BOMB)
         {
             parentFrame.decrementBadFlags();
         }
         else if (isFlagged && this.getSquareType() != BOMB)
         {
             parentFrame.incrementBadFlags();
         }
 
         isFlagged = !isFlagged;
 		swapColor();
 
         if (parentFrame.isGameWon())
             System.out.println("YOU WIN!");
 	}
 	
 	private void swapColor()
 	{
         if (isCovered())
         {
             if (this.isFlagged)
                 this.setBackground(FLAGGED_COLOR);
             else
                 this.setBackground(NON_ACTIVE_COLOR);
         }
 	}
 	
 	public void uncover()
 	{
 		parentFrame.uncover(this);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) 
 	{
 		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
 			leftClick();
 		else
 			rightClick();
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// fTODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
