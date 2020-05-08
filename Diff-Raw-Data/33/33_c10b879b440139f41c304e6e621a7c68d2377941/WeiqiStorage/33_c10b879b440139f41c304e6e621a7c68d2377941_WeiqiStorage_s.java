 package gatherer;
 
import java.util.List;

 import models.BlackOrWhite;
 
 public interface WeiqiStorage {
 
 	void addGame(String server, String id, String white, String wRank, String black, String bRank,
 			int turn, int size, int handicap, float komi, int byo, int observer);
 
 	void addMove(String server, String id, int number, BlackOrWhite player, String coordinate,
			int seconds, int byo, List<String> prisoners);
 
 	void addResult(String server, String game, String result);
 }
