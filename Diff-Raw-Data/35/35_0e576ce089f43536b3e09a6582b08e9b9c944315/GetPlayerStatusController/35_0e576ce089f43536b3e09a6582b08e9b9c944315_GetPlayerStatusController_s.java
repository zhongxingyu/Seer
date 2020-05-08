 package com.mindbadger.spring.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.mindbadger.audioserver.schema.AudioserverDocument;
 import com.mindbadger.jukebox.Jukebox;
 import com.mindbadger.library.Librarian;
 import com.mindbadger.library.Track;
 import com.mindbadger.player.PlayerStatus;
 
 @Controller
 public class GetPlayerStatusController {
   Logger logger = Logger.getLogger(GetPlayerStatusController.class);
   
   @Autowired
   private Jukebox jukebox;
   
   /*
    * http://localhost:1970/AudioServer/svr/getLibrary
    */
   
 	@RequestMapping("/getPlayerStatus")
 	public ModelAndView getPlayerStatus(HttpServletRequest request, HttpServletResponse response) {
 		logger.debug("CONTROLLER: getPlayerStatus");
 
 	    try {
 	       ServletOutputStream outputStream = response.getOutputStream();
 	       
 	       Track track = jukebox.getCurrentTrack();
 	       int trackId = 0;
 	       String artistName = "";
 	       String albumName = "";
 	       String trackName = "NO PLAYLIST";
 	       if (track != null) {
 	      	 trackId = track.getId ();
 	      	 artistName = track.getArtist();
 	      	 albumName = track.getAlbum();
 	      	 trackName = track.getName();
 	       }
 	       
 	       outputStream.println("{");
 	       outputStream.println("\"trackId\" : \"" + trackId + "\",");
 	       outputStream.println("\"artist\" : \"" + artistName + "\",");
 	       outputStream.println("\"album\" : \"" + albumName + "\",");
 	       outputStream.println("\"track\" : \"" + trackName + "\",");
	       outputStream.println("\"isPlaying\" : \"" + (jukebox.getPlayerStatus() == PlayerStatus.PLAYING ? "Y" : "N") + "\",");
 	       outputStream.println("\"isShuffle\" : \"" + (jukebox.isShuffle() ? "Y" : "N") + "\",");
 	       outputStream.println("\"isRepeat\" : \"" + (jukebox.isRepeat() ? "Y" : "N") + "\",");
 	       outputStream.println("\"isStartOfPlaylist\" : \"" + (jukebox.isStartOfPlaylist() ? "Y" : "N") + "\",");
 	       outputStream.println("\"isEndOfPlaylist\" : \"" + (jukebox.isEndOfPlaylist() ? "Y" : "N") + "\"");
 	       outputStream.println("}");
 	     } catch (IOException e) {
 	       e.printStackTrace();
 	     }
 
 		
 		return null;
 	}
 }
