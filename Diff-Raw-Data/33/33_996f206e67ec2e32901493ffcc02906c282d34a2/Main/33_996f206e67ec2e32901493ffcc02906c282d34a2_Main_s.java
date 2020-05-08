 package com.kokotchy.betaSeriesAPI;
 
 import java.io.File;
import java.util.List;
 
 import com.kokotchy.betaSeriesAPI.api.xmlImpl.BetaSerieApi;
import com.kokotchy.betaSeriesAPI.model.Episode;
import com.kokotchy.betaSeriesAPI.model.Subtitle;
import com.kokotchy.betaSeriesAPI.model.SubtitleLanguage;
 
 /**
  * Start application
  * 
  * @author kokotchy
  */
 public class Main {
 
 	/**
 	 * Start the application with parameters
 	 * 
 	 * @param args
 	 *            Parameters
 	 */
 	public static void main(String[] args) {
 		new Main();
 	}
 
 	/**
 	 * Start the application
 	 */
 	public Main() {
 		UtilsXml.setDebug(true);
 		String[] loadCredentials = Utils.loadCredentials(new File(System
 				.getProperty("user.dir")
 				+ "/src/main/resources/credentials/", "kokotchy"));
 		String token = BetaSerieApi.getMembers().auth(loadCredentials[0],
 				loadCredentials[1]);
		if (token != null) {
			List<Episode> episodes = BetaSerieApi.getMembers().getEpisodes(
					token, SubtitleLanguage.ALL, true);
			for (Episode episode : episodes) {
				if (episode.isDownloaded()) {
					List<Subtitle> subtitles = episode.getSubtitles();
					int nbSub = subtitles.size();
					System.out.println(episode.getShow() + " - "
							+ episode.getTitle() + " with " + nbSub
							+ " subtitles and is downloaded");
					if (episode.hasSubtitle()) {
						System.out.println("There is " + subtitles.size()
								+ " subtitles:");
						for (Subtitle subtitle : subtitles) {
							System.out.println("- [" + subtitle.getQuality()
									+ "] " + subtitle.getFile() + " in "
									+ subtitle.getLanguage());
						}
					}
				}
			}
 		}
 	}
 }
