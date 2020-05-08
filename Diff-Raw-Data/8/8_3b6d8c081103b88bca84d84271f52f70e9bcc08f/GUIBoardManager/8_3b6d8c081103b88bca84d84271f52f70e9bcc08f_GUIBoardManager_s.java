 package com.tas.icecave.guiLogic;
 
 import com.tas.icecave.general.sharedPreferences.SharedPreferencesFactory;
 
 import android.graphics.Bitmap;
 import com.tas.icecave.gui.GameActivity;
 import com.tas.icecave.gui.GameTheme;
 import com.tas.icecave.gui.ILoadable;
 import com.tas.icecaveLibrary.general.Consts;
 import com.tas.icecaveLibrary.general.EDifficulty;
 import com.tas.icecaveLibrary.general.EDirection;
 import com.tas.icecaveLibrary.mapLogic.IIceCaveGameStatus;
 import com.tas.icecaveLibrary.mapLogic.IceCaveGame;
 import com.tas.icecaveLibrary.mapLogic.tiles.ITile;
 import com.tas.icecaveLibrary.utils.Point;
 import com.tas.icecaveLibrary.utils.bundle.BaseBundleMetaData;
 import com.tas.icecaveLibrary.utils.bundle.BundleHasher;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.StreamCorruptedException;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * This class manages all GUI logic.
  * 
  * @author Tom
  * 
  */
 @SuppressWarnings("serial")
 public class GUIBoardManager implements Serializable, ILoadable
 {
 	private transient Bitmap[][] mTiles;
 	private Point[][] mRandomTileImages;
 	private IceCaveGame mIceCaveGame;
 	private final long HIDE_SHOW_TIME = 300;
 	private transient GameActivity mContext;
 	private int mBundleIndex;
 	private String[] mBundleFiles;
 	private String mBundleNameKey;
 	private transient LoadingThread mLoad;
 	private final int NO_KEY = -1;
 
 	public GUIBoardManager(GameActivity context)
 	{
 		mContext = context;
 	}
 
 	/**
 	 * Return the minimal moves for the current stage.
 	 * 
 	 * @return Minimal moves for stage.
 	 */
 	public int getMinimalMovesForStage()
 	{
 		return mIceCaveGame.getStageMoves();
 	}
 
 	/**
 	 * Resets the player location on the board.
 	 * 
 	 * @param startLoc
 	 *            - Starting location of the player to restart to.
 	 */
 	private void resetPlayer(Point startLoc)
 	{
 		mIceCaveGame.resetPlayer(startLoc);
 	}
 
 	/**
 	 * Return the overall moves for the current stage.
 	 * 
 	 * @return Moves player made in stage.
 	 */
 	public int getMovesCarriedOutThisStage()
 	{
 		return mIceCaveGame.getCurrentStageTakenMoves();
 	}
 
 	/**
 	 * Return the overall moves for the current game.
 	 * 
 	 * @return Overall moves in game.
 	 */
 	public int getOverAllMovesForGame()
 	{
 		return mIceCaveGame.getOverallMoves();
 	}
 
 	/**
 	 * Start a new game.
 	 * 
 	 * @param boulderNum
 	 *            - Number of boulders to place on map.
 	 * @param boardSizeHeight
 	 *            - Row length of the map.
 	 * @param difficulty
 	 *            - Difficulty level.
 	 * @param Context
 	 *            - activity creating the game
 	 */
 	public void startNewGame(int boardSizeHeight, EDifficulty difficulty)
 	{
 		// Make a square, always
 		int boardSizeWidth = boardSizeHeight;
 
 		mIceCaveGame =
 				new IceCaveGame(boardSizeHeight * boardSizeWidth / Consts.DEFAULT_BOULDER_RELATION,
 								boardSizeHeight,
 								boardSizeWidth,
 								difficulty);
 
 		// Get the tiles
 		mTiles = new Bitmap[boardSizeHeight][boardSizeWidth];
 		mRandomTileImages = new Point[boardSizeHeight][boardSizeWidth];
 
 		// Get the map bundle for the selected configurations
 		BaseBundleMetaData mData =
 				new BaseBundleMetaData(	Consts.DEFAULT_START_POS,
 										difficulty,
 										EDirection.RIGHT,
 										boardSizeHeight,
 										boardSizeWidth,
 										boardSizeHeight * boardSizeWidth / Consts.DEFAULT_BOULDER_RELATION,
 										Consts.MAP_GEN_VERSION,
 										Consts.DEFAULT_WALL_WIDTH);
 
 		try
 		{
 			BundleHasher hasher = new BundleHasher("md5");
 
 			// Get the list of files for the bundle
 			mBundleFiles =
 					mContext.getResources()
 							.getAssets()
 							.list(BundleHasher.hashToString(hasher.createMapBundleHash(mData)));
 
 			// Get the name of the hash and use it as the key for the index
 			mBundleNameKey = BundleHasher.hashToString(hasher.createMapBundleHash(mData));
 
 			// Attempt to reset shared data (of level index) before getting the index incase required
 			resetSharedData();
 
 			// Attempt to get index of user progress in current difficulty
 			mBundleIndex = mContext.getSharedPreferences(Consts.PREFS_FILE_TAG, 0).getInt(mBundleNameKey, 0);
 		} catch (NoSuchAlgorithmException e)
 		{
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Start a new stage.
 	 * 
 	 * @param mapFileStream
 	 *            - Map file stream.
 	 * @param context
 	 * @param gameTheme
 	 * @throws ClassNotFoundException
 	 * @throws IOException
 	 * @throws StreamCorruptedException
 	 * @throws CloneNotSupportedException
 	 */
 	public void newStage(InputStream mapFileStream, GameTheme gameTheme) throws StreamCorruptedException,
 			IOException,
 			ClassNotFoundException,
 			CloneNotSupportedException
 	{
 		mIceCaveGame.newStage(mapFileStream);
 		
 		// Set random tiles for this stage
 		ITile[][] board = mIceCaveGame.getBoard().getBoard();
 		
 		for (int yAxis = 0; yAxis < board.length; yAxis++)
 		{
 			for (int xAxis = 0; xAxis < board[0].length; xAxis++)
 			{
 				// Shuffle tile to select an image before loading them
 				mRandomTileImages[yAxis][xAxis] =
 						GUILogicServiceProvider.getInstance()
 								.getTileFactory()
 								.shuffleTile(board[yAxis][xAxis], gameTheme.getThemeMap());
 			}
 		}
 
 		// Get the tiles
 		mTiles =
 				new Bitmap[mIceCaveGame.getBoard().getBoard().length][mIceCaveGame.getBoard().getBoard()[0].length];
 
 		updateBoard(gameTheme);
 	}
 
 	/**
 	 * Start a new stage.
 	 * 
 	 * @param playerStart
 	 *            - Starting location of the player.
 	 * @param wallWidth
 	 *            - Width of the wall in tiles.
 	 * @param context
 	 *            - Current activity context.
 	 * @throws CloneNotSupportedException
 	 */
 	public void
 			newStage(Point playerStart, int wallWidth, GameTheme gameTheme) throws CloneNotSupportedException
 	{
 		mIceCaveGame.newStage(playerStart, wallWidth);
 		
 		// Set random tiles for this stage
 		ITile[][] board = mIceCaveGame.getBoard().getBoard();
 		
 		for (int yAxis = 0; yAxis < board.length; yAxis++)
 		{
 			for (int xAxis = 0; xAxis < board[0].length; xAxis++)
 			{
 				// Shuffle tile to select an image before loading them
 				mRandomTileImages[yAxis][xAxis] =
 						GUILogicServiceProvider.getInstance()
 								.getTileFactory()
 								.shuffleTile(board[yAxis][xAxis], gameTheme.getThemeMap());
 			}
 		}
 
 		updateBoard(gameTheme);
 	}
 
 	/**
 	 * Update the selected tile on the board
 	 * 
 	 * @param gameTheme
 	 * @param toUpdate
 	 */
 	public void updateBoard(Point toUpdate, GameTheme gameTheme)
 	{
 		ITile[][] board = mIceCaveGame.getBoard().getBoard();
 
 		// Fill a square (by the smaller size of the screen in current orientation)
 		GUIScreenManager screenManager =
 				new GUIScreenManager(board[0].length, board.length, mContext.getWidth(), mContext.getWidth());
 
 		// Warning! Will not work if would allow more than one empty tile to be selected
 		mTiles[toUpdate.y][toUpdate.x] =
 				GUILogicServiceProvider.getInstance()
 						.getTileFactory()
 						.getTiles(board[toUpdate.y][toUpdate.x],
 								screenManager,
 								gameTheme,
 								GUILogicServiceProvider.getInstance()
 								.getTileFactory()
 								.shuffleTile(board[toUpdate.y][toUpdate.x], gameTheme.getThemeMap()));
 	}
 
 	/**
 	 * Update the entire board
 	 * 
 	 * @param gameTheme
 	 */
 	public void updateBoard(GameTheme gameTheme)
 	{
 		ITile[][] board = mIceCaveGame.getBoard().getBoard();
 
 		// Fill a square (by the smaller size of the screen in current orientation)
 		GUIScreenManager screenManager =
 				new GUIScreenManager(board[0].length, board.length, mContext.getWidth(), mContext.getWidth());
 
 		// Go through the game board.
 		for (int yAxis = 0; yAxis < board.length; yAxis++)
 		{
 			for (int xAxis = 0; xAxis < board[0].length; xAxis++)
 			{
 				mTiles[yAxis][xAxis] =
 						GUILogicServiceProvider.getInstance()
 								.getTileFactory()
 								.getTiles(board[yAxis][xAxis],
 										screenManager,
 										gameTheme,
 										mRandomTileImages[yAxis][xAxis]);
 			}
 		}
 	}
 
 	/**
 	 * Get the tiles of the current board.
 	 * 
 	 * @return Map of the current board tiles.
 	 */
 	public Bitmap[][] getTiles()
 	{
 		return mTiles;
 	}
 
 	/**
 	 * Move the player on the board.
 	 * 
 	 * @param direction
 	 *            - Direction to move the player in.
 	 * 
 	 * @return new player point.
 	 */
 	public IIceCaveGameStatus movePlayer(EDirection direction)
 	{
 		return mIceCaveGame.movePlayer(direction);
 	}
 
 	@Override
 	public boolean isInitialLoading()
 	{
 		// If initialized, not initial loading.
 		return !mContext.isInitialized();
 	}
 
 	@Override
 	public long getAnimationDuration()
 	{
 		return HIDE_SHOW_TIME;
 	}
 
 	@Override
 	public GameTheme getGameTheme()
 	{
 		return mContext.getGameTheme();
 	}
 
 	@Override
 	public void onLoad()
 	{
 		// Start creating a new stage, cancel running one if running
 		mLoad =
 				new LoadingThread(	this,
 									Consts.DEFAULT_START_POS,
 									Consts.DEFAULT_WALL_WIDTH,
 									mContext,
 									getGameTheme());
 
 		// Check if file exists for loading
 		if (mBundleFiles != null && mBundleFiles.length > mBundleIndex)
 		{
 			mLoad.loadStageFromFile(mBundleNameKey + "/" + mBundleFiles[mBundleIndex]);
 		}
 
 		mLoad.start();
 	}
 	
 	@Override
 	public void cancelLoad()
 	{
		//mLoad.interrupt(); // FIXME STOP THREAD SOMEHOW!!!
 	}
 
 	/**
 	 * Save the stage index
 	 * 
 	 * @return Index of new stage. NO_KEY if not a loaded stage
 	 */
 	public int saveStageIndex()
 	{
 		int result = NO_KEY;
 
 		// Check if file exists and if index is still within file range
 		if (mBundleFiles != null && mBundleFiles.length > mBundleIndex)
 		{
 			// Increase index
 			mBundleIndex++;
 
 			// Set result
 			result = mBundleIndex;
 
 			// Set index of user progress in current difficulty
 			mContext.getSharedPreferences(Consts.PREFS_FILE_TAG, 0)
 					.edit()
 					.putInt(mBundleNameKey, mBundleIndex)
 					.commit();
 		}
 
 		return result;
 	}
 
 	// Get the bunleIndex for the current difficulty. If there are no files in the bundle return NO KEY
 	public int getBundleIndex()
 	{
 		return (mBundleFiles.length == 0) ? NO_KEY : mBundleIndex;
 	}
 
 	private void resetMoves()
 	{
 		mIceCaveGame.resetMoves();
 	}
 
 	/**
 	 * Reset the stage.
 	 * 
 	 * @throws CloneNotSupportedException
 	 */
 	private void resetStage() throws CloneNotSupportedException
 	{
 		mIceCaveGame.resetStage();
 		updateBoard(getGameTheme());
 	}
 	
 	/**
 	 * Reset the board and player position
 	 */
 	public void reset() {
 		// Reset player position on logic level
 		resetPlayer(Consts.DEFAULT_START_POS);
 		
 		// Reset board
 		try
 		{
 			resetStage();
 			mContext.refreshBoardView();
 		} catch (CloneNotSupportedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void resetSharedData()
 	{
 		// Reset some shared data if reset number value doesn't match current reset number value
 		// Thus every time that value is hardcodedly increased, a reset will occur
 		if (Consts.RESET_NUMBER != (Integer) SharedPreferencesFactory.getInstance()
 				.get(Consts.RESET_SHARED_DATA_TAG))
 		{
 			// Reset level index variables (for now just that)
 			mContext.getSharedPreferences(Consts.PREFS_FILE_TAG, 0).edit().putInt(mBundleNameKey, 0).commit();
 
 			// Finally save new reset number value
 			SharedPreferencesFactory.getInstance().set(Consts.RESET_SHARED_DATA_TAG, Consts.RESET_NUMBER);
 		}
 	}
 }
