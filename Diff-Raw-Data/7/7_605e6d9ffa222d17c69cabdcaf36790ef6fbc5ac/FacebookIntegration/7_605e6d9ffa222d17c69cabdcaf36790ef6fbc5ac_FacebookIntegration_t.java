 package ch.kanti_wohlen.asteroidminer.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.JsArray;
 
 public class FacebookIntegration {
 
 	private static Runnable highscoreCallback;
 	private static int lastScore;
 
 	public static native void init() /*-{
 		$doc.FB.init({
 			appId : '425756014191466',
 			xfbml : true,
 			status : true,
 			cookie : true,
 		});
 	}-*/;
 
 	public static native void logIn() /*-{
 		// Only log in if user is not logged in yet.
 		$doc.FB.getLoginStatus(function logInIfNecessary(response) {
 			if (response && response.status == 'connected') {
 				$doc.connected = true;
 				$doc.userID = response.authResponse.userID;
 			} else {
 				$doc.FB.login(function checkLoginStatus(response) {
 					if (response && response.status == 'connected') {
 						$doc.connected = true;
 						$doc.userID = response.authResponse.userID;
 					}
 				}, {
 					scope : 'publish_stream,publish_actions'
 				});
 			}
 		});
 	}-*/;
 
 	public static native boolean isLoggedIn() /*-{
 		return $doc.connected;
 	}-*/;
 
 	public static native void updateHighscore(int newScore) /*-{
 		@ch.kanti_wohlen.asteroidminer.client.FacebookIntegration::lastScore = newScore;
 
 		$doc.FB.api("/425756014191466/scores", "GET", function(currentScore) {
 			var getUserScore = function(scores) {
 				if (!(scores && scores.data)) {
 					$doc.highscores = [];
 					return 2147483647;
 				} else {
 					$doc.highscores = scores.data;
 					for (var i = 0; i < scores.data.length; ++i) {
 						var score = scores.data[i];
 						if ((score.application.id == '425756014191466')
 								&& (score.user.id == $doc.userID)) {
 							score.user.name = 'Your previous highscore';
 							return score.score;
 						}
 					}
 				}
 				return 0;
 			}
 
 			var score = getUserScore(currentScore);
 			$doc.highscores.push({
 				user : {
 					name : "This game",
 					id : $doc.userID
 				},
 				score : newScore,
 				application : {
 					id : '425756014191466'
 				}
 			});
 			if (newScore > score) {
 				$doc.FB.api("/me/scores", "POST", {
 					score : newScore
 				}, function(response) {
 				});
 			}
 		});
 	}-*/;
 
 	public static void refreshHighscores(Runnable callback) {
 		if (highscoreCallback != null) return;
 		if (callback != null) {
 			highscoreCallback = callback;
 			refreshHighscores(true);
 		} else {
 			refreshHighscores(false);
 		}
 	}
 
 	private static native void refreshHighscores(boolean callback) /*-{
 		$doc.FB.api("/425756014191466/scores", "GET", function(scores) {
 			if (!(scores && scores.data)) {
 				$doc.highscores = [];
 			} else {
 				$doc.highscores = scores.data;
 			}
 
 			if (callback) {
 				@ch.kanti_wohlen.asteroidminer.client.FacebookIntegration::executeCallback()();
 			}
 		});
 	}-*/;
 
 	private static void executeCallback() {
 		if (highscoreCallback == null) return;
 		highscoreCallback.run();
 		highscoreCallback = null;
 	}
 
 	public static List<JavaScriptHighscore> getHighscores() {
 		final JsArray<JavaScriptHighscore> nativeHighscores = getFriendHighscores();
 		final List<JavaScriptHighscore> highscores = new ArrayList<JavaScriptHighscore>(nativeHighscores.length());
 		for (int i = 0; i < nativeHighscores.length(); ++i) {
 			JavaScriptHighscore highscore = nativeHighscores.get(i);
 			if (highscore.getApplicationID().equals("425756014191466")) {
 				highscores.add(nativeHighscores.get(i));
 			}
 		}
 		return highscores;
 	}
 
 	private static native JsArray<JavaScriptHighscore> getFriendHighscores() /*-{
 		return $doc.highscores;
 	}-*/;
 
 	public static native void postScoreMessage() /*-{
 		var lastScore = @ch.kanti_wohlen.asteroidminer.client.FacebookIntegration::lastScore;
 		$doc.FB.ui({
 			method: 'feed',
			name: 'AsteroidMiner: ' + lastScore + ' points',
 			link: 'https://apps.facebook.com/asteroid_miner/',
			picture: 'https://raw.github.com/Firedroide/AsteroidMiner/master/Logo.png',
			description: 'I have just gotten a score of ' + lastScore + ' points in AsteroidMiner. How many can you get?'
 		}, function(response) {
 		});
 	}-*/;
 
 	public static native void postFriendScoreBeaten() /*-{
 		// TODO: make not todo anymore.
 	}-*/;
 }
