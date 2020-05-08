 package model.save;
 
import java.io.File;

 /**
  * Holds and returns the different save paths for the game.
  * @author Vidar Eriksson
  *
  */
 public class SavePath {
 	private static String getSavePath(){
		File file2 = new File(System.getenv("APPDATA") + "/Operation5a/");
		file2.mkdir();
		
		return file2.getPath() + "/";
 	}
 	/**
 	 * 
 	 * @return the path settings is to be saved at.
 	 */
 	public static  String settings(){
 		return getSavePath() + "Settings.txt";
 	}
 	/**
 	 * 
 	 * @return the path the high score is to be saved at.
 	 */
 	public static String highScore(){
 		return getSavePath() + "HighScore.txt";
 	}
 	/**
 	 * 
 	 * @return the path a game is to be saved at.
 	 */
 	public static  String savedGame(){
 		return getSavePath() + "SavedGame.txt";
 	}
 }
