 /**
  *   Copyright (C) 2010  Jonathan Hulka
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * Changelog:
 * 2011 12 31 - Jon
 * Added a focus request to fix keyboard events issue.
  * 
  * 2011 03 18 - Jon
  * Moved ui into PuzzleHandler (common to all PuzzleHandler implementations)
  * 
  * 2011 02 13 - Jon
  * Refactored to work with PuzzleCanvas modifications (see PuzzleCanvas 2011 02 12)
  * 
  * 2010 07 29 - Jon
  * Added logic to check for completed puzzle
  * 
  * 2010 07 27 - Jon
  * Abstracted tilemanager creation code to NewPuzzleDialog
  * This class now communicates with PuzzleCanvas, rather than GUI
  */
 
 import java.util.Random;
 import java.awt.Shape;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 import java.awt.geom.AffineTransform;
 import hulka.tilemanager.*;
 import java.io.PrintStream;
 
 /**
  * To do: make sure tilemask is only acquired once
  * To do: init one AffineTransform and reuse it
  */
 public class SpinnerHandler extends PuzzleHandler implements MouseMotionListener, KeyListener, MouseListener
 {
 	private Random random;
 	static final int MIX_COUNT=5;
 	private int errMargin = 8;
 	
 	private int tileCount;
 	private boolean solved=true;
 
 	Rectangle boardBounds=null, imageBounds=null, clipBounds=null;
 	HexSpinnerManager tileManager;
 	private int tileSize;
 	private int currentTile=-1;
 	private int currentVertex=-1;
 	private int currentSpin = TileManager.SPIN_NONE;
 	private Rectangle cursorBounds = new Rectangle(0,0,PuzzleCanvas.CURSOR_SIZE,PuzzleCanvas.CURSOR_SIZE);
 	
 	//Temporary storage to save heap abuse
 	private Point coords=null;
 	private int [] changedTiles=null;
 
 	public SpinnerHandler(HexSpinnerManager manager)
 	{
 		tileManager=manager;
 		tileCount = tileManager.getTileCount();
 		random = new Random();
 	}
 	
 	public boolean isGameSaved(){return false;}
 	public boolean isGameComplete()
 	{
 		return solved;
 	}
 
 	private boolean checkSolved()
 	{
 		boolean result=true;
 		for(int i=0; i<tileCount&&result; i++)
 		{
 			result=tileManager.getRotationCount(i)==0 && tileManager.getOriginalTileIndex(i)==i;
 		}
 		return result;
 	}
 	
 	public void connect(PuzzleCanvas canvas)
 	{
 		ui=canvas;
 		boardBounds=ui.getBounds(boardBounds);
 
 		imageBounds=new Rectangle();
 		imageBounds.width=tileManager.getBoardWidth();
 		imageBounds.height=tileManager.getBoardHeight();
 		imageBounds.x=(boardBounds.width-imageBounds.width)/2;
 		imageBounds.y=(boardBounds.height-imageBounds.height)/2;
 
 		setupTiles();
 		clipBounds=new Rectangle(0,0,tileSize+errMargin*2,tileSize+errMargin*2);
 		redraw();
 		ui.addMouseListener(this);
 		ui.addMouseMotionListener(this);
 		ui.addKeyListener(this);
 	}
 	
 	public void disconnect()
 	{
 		ui.removeMouseListener(this);
 		ui.removeMouseMotionListener(this);
 		ui.removeKeyListener(this);
 	}
 
 
 	private void setupTiles()
 	{
 		tileSize = tileManager.getTileWidth();
 		int h = tileManager.getTileHeight();
 		if(h > tileSize) tileSize = h;
 		ui.setBuffers(tileCount,tileSize,errMargin);
 		Shape mask = tileManager.getTileMask(0);
 		for(int i=0;i<tileCount;i++)
 			ui.setTileMask(i,mask);
 		mix();
 	}
 	
 	private void mix()
 	{
 		int [] indices = new int[tileManager.getTileCount()];
 		for(int i=0; i < indices.length; i++){indices[i]=i;}
 		for(int i = 0; i < MIX_COUNT; i++)
 		{
 			//Randomize the order
 			for(int j=0; j<indices.length; j++)
 			{
 				int t = indices[j];
 				int index = random.nextInt(indices.length);
 				indices[j] = indices[index];
 				indices[index] = t;
 			}
 			for(int j=0; j<indices.length; j++)
 			{
 				tileManager.spin(indices[j], random.nextInt(2)==0 ? TileManager.SPIN_CCW : TileManager.SPIN_CW);
 			}
 		}
 		solved=false;
 	}
 	
 	public void redraw()
 	{
 		ui.erase();
 		//Draw the whole picture to the background
 		ui.drawTile(imageBounds,null,PuzzleCanvas.DRAW_PUZZLEIMAGE,PuzzleCanvas.DRAW_BACKGROUND);
 		ui.clear();
 		for(int i=0; i < tileCount; i++)
 		{
 			initTileImage(i);
 			coords = tileManager.getTilePosition(i,coords);
 			clipBounds.x=coords.x+imageBounds.x-errMargin;
 			clipBounds.y=coords.y+imageBounds.y-errMargin;
 			ui.drawTile(clipBounds,null,PuzzleCanvas.DRAW_TILEBUFFER,PuzzleCanvas.DRAW_FOREGROUND);
 		}
 		if(currentVertex>-1)markVertex(currentSpin);
 		ui.repaint();
 	}
 
 	private void initTileImage(int tileIndex)
 	{
 		AffineTransform rotation = tileManager.getRotationTransform(tileIndex,new AffineTransform(),errMargin);
 		coords = tileManager.getOriginalTilePosition(tileIndex,coords);
 		ui.setTileIndex(tileIndex);
 		ui.buildTileImage(coords.x,coords.y,rotation);
 	}
 	
 	/**
 	 * Moves clipBounds
 	 **/
 	private void drawTile(int tileIndex)
 	{
 		coords = tileManager.getTilePosition(tileIndex,coords);
 		clipBounds.x=coords.x-errMargin+imageBounds.x;
 		clipBounds.y=coords.y-errMargin+imageBounds.y;
 		ui.drawTile(clipBounds,null,PuzzleCanvas.DRAW_TILEBUFFER,PuzzleCanvas.DRAW_FOREGROUND);
 	}
 	
 	private void markVertex(int spin)
 	{
 		//Backup the image under the rotation icon
 		ui.backupImage(cursorBounds, PuzzleCanvas.DRAW_FOREGROUND);
 
 		//Draw the icon
 		ui.drawTile(cursorBounds,null,spin==TileManager.SPIN_CW ? PuzzleCanvas.DRAW_ROTATECW : PuzzleCanvas.DRAW_ROTATECCW, PuzzleCanvas.DRAW_FOREGROUND);
 	}
 	
 	private void unmarkVertex()
 	{
 		//redraw the saved piece of image
 		ui.drawTile(cursorBounds,null,PuzzleCanvas.DRAW_DRAGBUFFER,PuzzleCanvas.DRAW_FOREGROUND);
 	}
 	
 	public void mouseMoved(MouseEvent e)
 	{
 		int vertex = tileManager.getNearestVertex(e.getX()-imageBounds.x, e.getY()-imageBounds.y);
 		int spin = e.isShiftDown() ? TileManager.SPIN_CCW : TileManager.SPIN_CW;
 		if(vertex>-1)
 		{
 			if(vertex != currentVertex)
 			{
 				if(currentVertex>-1)
 				{
 					unmarkVertex();
 					ui.repaint(cursorBounds);
 				}
 				coords = tileManager.getVertexPosition(vertex,coords);
 				cursorBounds.x = coords.x-PuzzleCanvas.CURSOR_SIZE/2+imageBounds.x;
 				cursorBounds.y = coords.y-PuzzleCanvas.CURSOR_SIZE/2+imageBounds.y;
 				markVertex(spin);
 				ui.repaint(cursorBounds);
 			}
 		}
 		else if(currentVertex>-1)
 		{
 			unmarkVertex();
 			ui.repaint(cursorBounds);
 		}
 		currentSpin = spin;
 		currentVertex = vertex;
 	}
 
 	public void keyPressed(KeyEvent e)
 	{
 		int spin = e.isShiftDown() ? TileManager.SPIN_CCW : TileManager.SPIN_CW;
 		if(spin != currentSpin && currentVertex>-1)
 		{
 			unmarkVertex();
 			markVertex(spin);
 			ui.repaint(cursorBounds);
 		}
 		currentSpin = spin;
 	}
 
 	public void keyReleased(KeyEvent e)
 	{
 		int spin = e.isShiftDown() ? TileManager.SPIN_CCW : TileManager.SPIN_CW;
 		if(spin != currentSpin && currentVertex>-1)
 		{
 			unmarkVertex();
 			markVertex(spin);
 			ui.repaint(cursorBounds);
 		}
 		currentSpin = spin;
 	}
 	
 	public void keyTyped(KeyEvent e){}
 	
 	public void mouseDragged(MouseEvent e){}
 	
 	private Rectangle mcBounds=new Rectangle();
 	public void mouseClicked(MouseEvent e)
 	{
 		if(solved)
 		{
 			mix();
 			redraw();
 		}
 		else if(currentVertex>-1)
 		{
 			tileManager.spin(currentVertex,currentSpin);
 			changedTiles = tileManager.getChangedTiles(changedTiles);
 			for(int i = 0; i < changedTiles.length; i++)
 			{
 				initTileImage(changedTiles[i]);
 				drawTile(changedTiles[i]);
 				if(i==0){mcBounds.x=clipBounds.x;mcBounds.y=clipBounds.y;mcBounds.width=clipBounds.width;mcBounds.height=clipBounds.height;}
 				else mcBounds.add(clipBounds);
 			}
 			markVertex(currentSpin);
 			ui.repaint(mcBounds);
 			solved=checkSolved();
 		}
 	}
 
 	public void onPreview(){}
 	public void mousePressed(MouseEvent e){}
 	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e)
	{
		ui.requestFocusInWindow();
	}
 	public void mouseExited(MouseEvent e){}
 
 	public boolean save(PrintStream out)
 	{
 		return false;
 	}
 }
