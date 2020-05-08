 package org.vxp7755_nxz3937.freeforall;
 
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import java.util.Random;
 import java.util.ArrayList;
 //import org.vxp7755_nxz3937.freeforall.R;
 
 public class ControllerThread extends Thread {
 	
 	public final int MSGTYPE_MOVER   = 1;
 	public final int MSGTYPE_SPAWNER = 2;
 	public final int MSGTYPE_QUIT    = 3;
 	
 	public final int SPAWNTYPE_SYS   = 1;
 	public final int SPAWNTYPE_USER  = 2;
 	
 	public final int DEFAULT_SPAWN_TIME = 6000;
 	public final int DEFAULT_MOVE_TIME = 1000;
 
 	private Random random = new Random();
 	
 	public Handler boardHandler;
 	private Board board;
 	private boolean paused;
 	private double spawnSpeedMultiplier;
 	private double moveSpeedMultiplier;
 	private MainView ui;
 	
 	ControllerThread( MainView gui )
 	{
 		paused          = false;
 		spawnSpeedMultiplier = 1.0;
 		moveSpeedMultiplier = 1.0;
 		ui              = gui;
 		board           = new Board( ui.GRID_WIDTH, ui.GRID_HEIGHT );
 	}
 	
 	@Override
 	public void run()
 	{
 		Looper.prepare();
 		
 		boardHandler = new Handler(){
 			@Override
 			public void handleMessage( Message msg )
 			{
 				Log.i("BoardHandler", "Message Received");
 				if( !paused )
 				{
 					Log.i("BoardHandler", String.format("What: %d :: Arg1: %d", msg.what, msg.arg1));
 					if( msg.what == MSGTYPE_MOVER )
 					{
 						handleMover( (MoveObject)msg.obj );
 					} 
 					else if( msg.what == MSGTYPE_SPAWNER )
 					{
 						handleSpawner( msg );
 					}
 					else
 					{
 						Log.i("BoardHandler", "Quit request received");
 						this.getLooper().quit();
 					}
 				}
 			}
 			
 			
 			private void handleMover( MoveObject mover )
 			{
 				PieceThread cell = board.getCell( mover.x, mover.y );
 				
 				Log.i("handleMover", "Moving...");
 				
 				if( cell != null ) // If cell occupied
 				{
 					Log.i("handleMover", "Eating...");
 					cell.eaten();
 					board.givePoint( cell.getTeam() );
 				}
 				
 				// Update board cells
 				board.setCell( mover.me.getX(), mover.me.getY(), null);
 				board.setCell( mover.x, mover.y, mover.me );
 				
 				// Send messages to UI to update the cell moved from and moved to
 				Handler uiHandler = ui._redrawHandler;
 				Message updateMsg = uiHandler.obtainMessage();
 				
 				UpdateData updateOldLoc = new UpdateData( mover.me.getX(), mover.me.getY(), 0, board.getScores() );
 				UpdateData updateNewLoc = new UpdateData( mover.x, mover.y, 0, board.getScores() );
 				
 				Log.i("moveHandler", "Sending UI a message");
 				
 				updateMsg.obj = updateOldLoc;
 				uiHandler.sendMessage(updateMsg);
 				updateMsg.obj = updateNewLoc;
 				uiHandler.sendMessage(updateMsg);
 			}
 			
 			private void handleSpawner( Message msg )
 			{
 				int[] dimensions = board.getSize();
 				int boardWidth = dimensions[0];
 				int boardHeight = dimensions[1];
 				
 				if( msg.arg1 == SPAWNTYPE_SYS )
 				{
 					Log.i("BoardHandler", "System spawn request received");
 					// generate 4 different coordinate sets
 					int cords[][] = getUniqueCoordinates(4);
 					
 					for(int i = 0; i < 4; i++ ) {
 						spawnPiece(cords[i][0], cords[i][1], (i+1));
 					}
 				}
 				else // SPAWNTYPE_USER
 				{
 					if (! paused) {
 						Log.i("BoardHandler", "User spawn request received");
 						// process top-left corner (team 1/red)
 						spawnPiece(0,0,1);
 						// process top-right corner (team 2/green)
 						spawnPiece((boardWidth-1),0,2);
 						// process bottom-right corner (team 3/ blue)
 						spawnPiece(0,(boardHeight-1),3);
 						// process bottom-left corner (team 4/yellow)
 						spawnPiece((boardWidth-1),(boardHeight-1),4);
 					}
 				}
 			}
 		};
 		
 		Message spawnMsg = boardHandler.obtainMessage();
 		spawnMsg.what    = MSGTYPE_SPAWNER;
 		spawnMsg.arg1    = SPAWNTYPE_SYS;
 		
 		boardHandler.sendMessage( spawnMsg );
 			
 		Log.i("Controller", "Looper starting");
 		Looper.loop();
 	}
 	
 	/**
 	 * 
 	 * @param team	team number of new piece to spawn (1-4)
 	 * @param x		row to place new PiecesThread
 	 * @param y		column to place new PieceThread
 	 */
 	private void spawnPiece(int x, int y, int team)
 	{
 		// consume PieceThread if cell is occupied
 		if (board.getCell(x, y) != null) {
 			board.getCell(x, y).eaten();
 			board.givePoint(team);
 		}
 		
 		// generate new PieceThread
 		int pieceType = this.random.nextInt(4);
 		PieceThread newPiece;
 		switch(pieceType) {
 			case 0:		newPiece = new LeftUp_PieceThread(x, y, team, this );
 						Log.i( "Controller", "Spawned LeftUp");
 						break;
 			case 1:		newPiece = new LeftDown_PieceThread(x, y, team, this );
 						Log.i( "Controller", "Spawned LeftDown");
 						break;
 			case 2:		newPiece = new RightUp_PieceThread(x, y, team, this );
 						Log.i( "Controller", "Spawned RightUp");
 						break;
 			default:	newPiece = new RightDown_PieceThread(x, y, team, this);
 						Log.i( "Controller", "Spawned RightDown");
 						break;
 		}
 		
 		// set new piece
 		board.setCell(x, y, newPiece);
 		
 		// Send messages to UI to update the cell
 		Handler uiHandler = ui._redrawHandler;
 		Message updateMsg = uiHandler.obtainMessage();
 		
 		UpdateData updateNewLoc = new UpdateData( x, y, 0, board.getScores() );
 		
 		Log.i("moveHandler", "Sending UI a message");
 		
 		updateMsg.obj = updateNewLoc;
 		uiHandler.sendMessage(updateMsg);
 		
 		// tell PieceThread to run
 		newPiece.start();
 		
 	}
 	
 	private int[][]getUniqueCoordinates( int numCoordinates )
 	{
 		int boardSize[] = board.getSize();
 		int boardHeight = boardSize[0];
 		int boardWidth = boardSize[1];
 		
 		ArrayList<int[]> coordinates = new ArrayList<int[]>();
 		
 		// generate coordinates until you have a unique set
 		while (coordinates.size() < numCoordinates) {
 			
 			// generate new coordinates
 			int x = this.random.nextInt(boardWidth);
 			int y = this.random.nextInt(boardHeight);
 			
 			// check that coordinates are unique
 			boolean goodCoordinates = true;
 			for(int[] testCoords : coordinates) {
 				if(testCoords[0] == x && testCoords[0] == y) {
 					goodCoordinates = false;
 				}
 			}
 			
 			// if the new coordinates are good, add them to the ArrayList
 			if (goodCoordinates) {
 				int[] newCoords = {x,y};
 				coordinates.add(newCoords);
 			}
 			
 		}
 		
		// convert coordinates to primative array
		int[][] result = new int[coordinates.size()][2];
		for(int i = 0; i < coordinates.size(); i++) {
			result[i][0] = coordinates.get(i)[0];
			result[i][0] = coordinates.get(i)[1];
		}
		
		return result;
 	}
 	
 	/** Call the UI to redraw board */
 	public void drawCell()
 	{
 		ui._redrawHandler.sendEmptyMessage( 0 );
 	}
 	
 	/** Get the board */
 	public Board getBoard() { return board; }
 	
 	
 	/**
 	 * Decrease move delay multiplier
 	 */
 	public void moveSpeedUp()
 	{
 		if (this.moveSpeedMultiplier > 0.25) {
 			this.moveSpeedMultiplier -= 0.25;
 		}
 		Log.i("moveSpeedUp", String.format("moveSpeedMultiplier = %f",
 											this.moveSpeedMultiplier));
 	}
 	
 	
 	/**
 	 * Increase move delay multiplier
 	 */
 	public void moveSlowDown()
 	{
 		if (this.moveSpeedMultiplier < 2.0 ) {
 			this.moveSpeedMultiplier += 0.25;
 		}
 		Log.i("moveSlowDown", String.format("moveSpeedMultiplier = %f",
 				this.moveSpeedMultiplier));
 	}
 	
 	
 	/**
 	 * Decrease spawn delay multiplier
 	 */
 	public void spawnSpeedUp()
 	{
 		if (this.spawnSpeedMultiplier > 0.25) {
 			this.spawnSpeedMultiplier -= 0.25;
 		}
 		Log.i("spawnSpeedUp", String.format("spawnSpeedMultiplier = %f",
 											this.spawnSpeedMultiplier));
 	}
 	
 	
 	/**
 	 * Increase spawn delay multiplier
 	 */
 	public void spawnSlowDown()
 	{
 		if (this.spawnSpeedMultiplier < 2.0 ) {
 			this.spawnSpeedMultiplier += 0.25;
 		}
 		Log.i("spawnSlowDown", String.format("spawnSpeedMultiplier = %f",
 				this.spawnSpeedMultiplier));
 	}
 	
 	/**
 	 * Modify whether or not the simulation is pause
 	 * 
 	 * @param value true to pause the sim, otherwise false
 	 */
 	public void switchPause( boolean value )
 	{
 		this.paused = value;
 		if (this.paused) {
 			Log.i("switchPause", "paused = true");
 		} else {
 			Log.i("switchPause", "paused = false");
 		}
 	}
 	
 	/**
 	 * Retrieves the move delay used by PieceThreads
 	 * 
 	 * @return delay in ms, which PieceThe reads should wait between moving
 	 */
 	public int getMoveDelay()
 	{
 		return (int) (this.moveSpeedMultiplier * this.DEFAULT_MOVE_TIME);
 	}
 	
 	
 	/**
 	 * Retrieves the spawn delay used by the SpawnerThread
 	 * 
 	 * @return delay in ms, in between system-driven PieceThread spawns
 	 */
 	public int getSpawnDelay() {
 		return (int) (this.spawnSpeedMultiplier * this.DEFAULT_SPAWN_TIME);
 	}
 	
 	
 	/**
 	 * Checks if the sim is currently paused
 	 * 
 	 * @return true if sim paused, otherwise false
 	 */
 	public boolean isPaused() {
 		return this.paused;
 	}
 }
