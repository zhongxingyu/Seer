 package model.save;
 
 /**
  * Holds and returns the different save paths for the game.
  * @author Vidar Eriksson
  *
  */
 public class SavePath {
 	private static String getSavePath(){
		//TODO var sparas?
		return System.getenv("APPDATA") + "/";
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
