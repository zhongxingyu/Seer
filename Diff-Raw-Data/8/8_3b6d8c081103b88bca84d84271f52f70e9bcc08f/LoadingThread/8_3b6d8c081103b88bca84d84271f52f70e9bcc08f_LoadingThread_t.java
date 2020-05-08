 package com.tas.icecave.guiLogic;
 
 import android.os.SystemClock;
 
 import android.content.res.AssetManager;
 import com.tas.icecave.gui.GameActivity;
 import com.tas.icecave.gui.GameTheme;
 import com.tas.icecaveLibrary.general.Consts;
 import com.tas.icecaveLibrary.utils.AutoObservable;
 import com.tas.icecaveLibrary.utils.Point;
 import com.tas.icecaveLibrary.utils.UpdateDataBundle;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StreamCorruptedException;
 
 public class LoadingThread extends Thread
 {
 	private GUIBoardManager mBoardManager;
 	private Point mPlayerPosition;
 	private int mWallWidth;
 	private GameActivity mActivity;
 	private GameTheme mTheme;
 	private AutoObservable mObservable;
 	private InputStream mInputStream;
 
 	public LoadingThread(GUIBoardManager boardManager,
 			Point playerPosition,
 			int wallWidth,
 			GameActivity activity,
 			GameTheme theme)
 	{
 		mBoardManager = boardManager;
 		mPlayerPosition = playerPosition;
 		mWallWidth = wallWidth;
 		mActivity = activity;
 		mTheme = theme;
 		mObservable = new AutoObservable();
 		mObservable.addObserver(mActivity);
 		mInputStream = null;
 	}
 
 	public void loadStageFromFile(String stageName)
 	{
 		try
 		{	
 			AssetManager assetManager = mActivity.getResources().getAssets();
 	        mInputStream = assetManager.open(stageName);
 		}
 		catch (StreamCorruptedException e1)
 		{
 			e1.printStackTrace();
 
 			// TODO HANDLE ERROR
 			return;
 		}
 		catch (IOException e1)
 		{
 			// TODO HANDLE ERROR
 			e1.printStackTrace();
 			return;
 		}
 	}
 
 	@Override
 	public void run()
 	{
 		final long MINIMUM_LOADING_TIME = 2500; // Should be enough time to read the texts on the screen
 
 		// Shuffle the theme.
 		mTheme.getThemeMap().shuffle();
 
 		if (mInputStream != null)
 		{
 			try
 			{
 				mBoardManager.newStage(mInputStream, mTheme);
 				mInputStream.close();
 				mInputStream = null;
 			} catch (StreamCorruptedException e)
 			{
 				e.printStackTrace();
 			} catch (IOException e)
 			{
 				e.printStackTrace();
 			} catch (ClassNotFoundException e)
 			{
 				e.printStackTrace();
 			}
 			catch (CloneNotSupportedException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else
 		{
 			// Create new stage
 			try
 			{
 				mBoardManager.newStage(mPlayerPosition, mWallWidth, mTheme);
 			}
 			catch (CloneNotSupportedException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// Get loading time
 		long loadingTime = SystemClock.currentThreadTimeMillis();
 
 		// Sleep
 		try
 		{
 			// If loading time took less than minimum, add a delay to the animation
 			if (MINIMUM_LOADING_TIME - loadingTime > 0)
 			{
 				Thread.sleep(MINIMUM_LOADING_TIME - loadingTime);
 			}
 		} catch (InterruptedException e)
 		{
			// This is pretty normal. Nothing special to do in this case
 			e.printStackTrace();
 		}
 
 		// Notify on completion
 		mObservable.notifyObservers(new UpdateDataBundle(Consts.LOADING_LEVEL_FINISHED_UPDATE, null));
 	}
 }
