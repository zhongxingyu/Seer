 package ch.kanti_wohlen.asteroidminer.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import ch.kanti_wohlen.asteroidminer.AsteroidMiner;
 import ch.kanti_wohlen.asteroidminer.GameLauncher;
 import ch.kanti_wohlen.asteroidminer.Pair;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.backends.gwt.GwtApplication;
 import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
 
 public class GwtLauncher extends GwtApplication implements GameLauncher {
 
 	@Override
 	public GwtApplicationConfiguration getConfig() {
 		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(960, 640);
 		cfg.fps = 45;
 		cfg.antialiasing = false;
 		return cfg;
 	}
 
 	@Override
 	public ApplicationListener getApplicationListener() {
 		return new AsteroidMiner(this, 45);
 	}
 
 	@Override
 	public void log(String tag, String message, Throwable exception) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public int getLogLevel() {
 		return 0;
 	}
 
 	@Override
 	public void onModuleLoad() {
 		super.onModuleLoad();
 		FacebookIntegration.init();
 		FacebookIntegration.logIn();
 	}
 
 	@Override
 	public void setHighscore(int newScore) {
 		FacebookIntegration.updateHighscore(newScore);
 	}
 
 	@Override
 	public void refreshHighscores(Runnable callback) {
 		FacebookIntegration.refreshHighscores(callback);
 	}
 
 	@Override
 	public List<Pair<String, Integer>> getHighscores() {
 		List<JavaScriptHighscore> nativeScores = FacebookIntegration.getHighscores();
 		List<Pair<String, Integer>> highscores = new ArrayList<Pair<String,Integer>>(nativeScores.size());
 		for (JavaScriptHighscore highscore : nativeScores) {
 			highscores.add(new Pair<String, Integer>(highscore.getUserName(), highscore.getScore()));
 		}
 
 		Collections.sort(highscores, new Comparator<Pair<String, Integer>>() {
 
 			@Override
 			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
 				return o2.getValue().compareTo(o1.getValue());
 			}
 		});
 		return highscores;
 	}
 
 	@Override
 	public void postFeedHighscore() {
 		FacebookIntegration.postScoreMessage(1000); // TODO: temp
 	}
 
 	@Override
 	public void postFeedFriendScoreBeaten() {
 		FacebookIntegration.postFriendScoreBeaten();
 	}
 }
