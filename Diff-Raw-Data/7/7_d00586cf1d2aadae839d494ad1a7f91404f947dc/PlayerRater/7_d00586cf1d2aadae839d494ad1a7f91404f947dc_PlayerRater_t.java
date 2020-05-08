 package com.seventhirtyfive;
 
 import java.awt.Color;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 
 /**
  * 
  * @author Mike Schassberger
  *
  */
 public class PlayerRater {
 	// TODO: Make this a singleton
 	private static PlayerRater mSingleton = null;
 	
 	/**
 	 * This will add a modifier to Catchers and MI eligible players
 	 */
 	public static final boolean USE_SCARCITY_MODIFIER = true;
 	public static final double SCARCITY_MODIFIER = 0.10;		// 20 percent
 	
	public static final String BASEDIR = "/home/mschassberger/Builds/AU/src/AUPlayerRater/AU/data/";
 	
	public static final String CURRENTYEAR = "2014";
	public static final String LASTYEAR = "2013";
 	public static final String BATTER_FILE_PROJECTIONS = BASEDIR+CURRENTYEAR+"/"+CURRENTYEAR+"_BatterProjections.csv";
 	public static final String PITCHER_FILE_PROJECTIONS = BASEDIR+CURRENTYEAR+"/"+CURRENTYEAR+"_PitcherProjections.csv";
 	public static final String TEAM_FILE = BASEDIR+CURRENTYEAR+"/"+CURRENTYEAR+"_TeamTotals.csv";
 	
 	// used to determine rookies
 	public static final String BATTER_FILE_PREVIOUSYEAR = BASEDIR+LASTYEAR+"/"+LASTYEAR+"_BatterProjections.csv";
 	public static final String PITCHER_FILE_PREVIOUSYEAR = BASEDIR+LASTYEAR+"/"+LASTYEAR+"_PitcherProjections.csv";
 	public static final String BATTER_FILE_3YEAR_AVG = BASEDIR+CURRENTYEAR+"/"+CURRENTYEAR+"_Batters_3yearAvg.csv";
 	public static final String PITCHER_FILE_3YEAR_AVG = BASEDIR+CURRENTYEAR+"/"+CURRENTYEAR+"_Pitchers_3yearAvg.csv";
 	
 	private static Debug735 mBatterWindow;
 	private static Debug735 mPitcherWindow;
 	private static Debug735 mTeamWindow;
 	private static UIBase mUiBase = new UIBase("Temp");
 	
 	private FileOutputStream mBatterFile = null;
 	private String mBatterOutputFile = BASEDIR+CURRENTYEAR+"BatterPrices.csv";
 	private BufferedOutputStream mBatterStream = null;
 	private String mPitcherOutputFile = BASEDIR+CURRENTYEAR+"PitcherPrices.csv";
 	private BufferedOutputStream mPitcherStream = null;
 	
 	// eligibility files
 	public static final String mCatcherFile = BASEDIR+CURRENTYEAR+"/"+"Catchers.csv";
 	public static final String mFirstBaseFile = BASEDIR+CURRENTYEAR+"/"+"/FirstBase.csv";
 	public static final String mSecondBaseFile = BASEDIR+CURRENTYEAR+"/"+"/SecondBase.csv";
 	public static final String mThirdBaseFile = BASEDIR+CURRENTYEAR+"/"+"ThirdBase.csv";
 	public static final String mShortStopFile = BASEDIR+CURRENTYEAR+"/"+"ShortStop.csv";
 	public static final String mOutfieldFile = BASEDIR+CURRENTYEAR+"/"+"Outfielders.csv";
 	public static final String mStartingPitcherFile = BASEDIR+CURRENTYEAR+"/"+"StartingPitchers.csv";
 	public static final String mReliefPitcherFile = BASEDIR+CURRENTYEAR+"/"+"ReliefPitchers.csv";
 
 	public static PlayerRater getInstance() {
 		if (mSingleton==null) {
 			mSingleton = new PlayerRater();
 		}
 		return mSingleton;
 	}
 	
 	public BufferedOutputStream getPitcherStream() {
 		if (mPitcherStream == null) {
 			try {
 				File f = new File(mPitcherOutputFile);
 				mPitcherStream = new BufferedOutputStream(new FileOutputStream(f));
 			} catch(Exception e) {
 				System.out.println(mPitcherOutputFile+" file stream creation error: "+e);
 			}
 		}
 		return mPitcherStream;
 	}
 	
 	public void closeStream( BufferedOutputStream os ) {
 		try {
 			os.close();
 			mBatterFile.close();
 		} catch( Exception e ) {
 			System.out.println("Stream"+os+" could not be closed: "+e);
 		}
 	}
 
 	public BufferedOutputStream getBatterStream() {
 		if (mBatterStream == null) {
 			try {
 				File f = new File(mBatterOutputFile);
 				mBatterFile = new FileOutputStream(f);
 				mBatterStream = new BufferedOutputStream(mBatterFile);
 			} catch(Exception e) {
 				System.out.println(mBatterOutputFile+" file stream creation error: "+e);
 			}
 		}
 		return mBatterStream;
 	}
 
 	public PlayerRater() {
 		mTeamWindow = new Debug735(new Color(128, 0, 128), "AU Team List");
 		mTeamWindow.init();
 		mBatterWindow = new Debug735(new Color(128, 128, 128), "Batters");
 		mBatterWindow.init();
 		mPitcherWindow = new Debug735(new Color(0, 128, 0), "Pitchers");
 		mPitcherWindow.init();
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		PlayerRaterRunner runner = new PlayerRaterRunner();
 		runner.start();
 	}
 	
 	/**
 	 * Main entry point for PlayerRater application
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		PlayerRater m = PlayerRater.getInstance();
 	}
 	
 	public static Debug735 getDebugger() {
 		return mBatterWindow;
 	}
 	
 	public static Debug735 getPitcherWindow() {
 		return mPitcherWindow;
 	}
 	public static Debug735 getTeamWindow() {
 		return mTeamWindow;
 	}
 	
 	private class PlayerRaterRunner extends Thread {
 
 		@Override
 		public void run() {
 			String test = "Hitter Rankings";
 			mBatterWindow.addString(test);
 
 			mTeamWindow.addString("AU Team List");
 			String fileToLoad = TEAM_FILE;
 			AUFileLoader fl = new AUFileLoader(fileToLoad, AUFileLoader.TEAM_LOAD, AUPlayer.YEAR_2009);
 			if (fl.load()) {
 				mTeamWindow.addString(fileToLoad+" LOADED");
 			} else {
 				mTeamWindow.addString(fileToLoad+" FAILED");
 			}
 
 			fileToLoad = BATTER_FILE_PROJECTIONS;
 			fl = new AUFileLoader(fileToLoad, AUFileLoader.BATTER_LOAD, AUPlayer.YEAR_2009);
 			if (fl.load()) {
 				mBatterWindow.addString(fileToLoad+" LOADED");
 			} else {
 				mBatterWindow.addString(fileToLoad+" FAILED");
 			}
 
 			mPitcherWindow.addString("Pitcher Rankings");
 			fileToLoad = PITCHER_FILE_PROJECTIONS;
 			fl = new AUFileLoader(fileToLoad, AUFileLoader.PITCHER_LOAD, AUPlayer.YEAR_2009);
 			if (fl.load()) {
 				mPitcherWindow.addString(fileToLoad+" LOADED");
 			} else {
 				mPitcherWindow.addString(fileToLoad+" FAILED");
 			}
 		 }
 	}
 }
 
