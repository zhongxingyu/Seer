 /*
  * Copyright 2008 Charles Perry
  *
  * This file is part of Harmonium, the TiVo music player.
  *
  * Harmonium is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as 
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * Harmonium is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public
  * License along with Harmonium.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
  
 package org.dazeend.harmonium.screens;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.dazeend.harmonium.HSkin;
 import org.dazeend.harmonium.Harmonium;
 import org.dazeend.harmonium.music.Album;
 import org.dazeend.harmonium.music.CompareDiscs;
 import org.dazeend.harmonium.music.Disc;
 import org.dazeend.harmonium.music.Playable;
 import org.dazeend.harmonium.music.PlaylistEligible;
 
 import com.tivo.hme.bananas.BText;
 import com.tivo.hme.bananas.BView;
 
 
 
 /**
  * 
  * @author Charles Perry (harmonium@DazeEnd.org)
  *
  */
 public class BrowseAlbumScreen extends HAlbumInfoListScreen {
 	
 	private Album album;
 	
 	public BrowseAlbumScreen(Harmonium app, final Album thisAlbum) {
 		super(app, thisAlbum, thisAlbum.toString());
 		
 		this.album = thisAlbum;
 		
 		// If this album is broken into discs, add them to the screen
 		List<Disc> discs = new ArrayList<Disc>();
 		discs.addAll( thisAlbum.getDiscList() );
 		Collections.sort( discs, new CompareDiscs() );
 		for (Disc disc : discs)
 			addToList(disc);
 		
 		// If this album has any tracks that are not identified as members of a disc,
 		// add them to the screen.
 		List<Playable> tracks = new ArrayList<Playable>();
 		tracks.addAll( thisAlbum.getTrackList() );
 		Collections.sort(tracks, this.app.getPreferences().getAlbumTrackComparator());
 		for (Playable track : tracks)
 			addToList(track);
 
 		// Add a note to the bottom of the screen
 		BText enterNote = new BText(	this.getNormal(),
 										this.safeTitleH,
 										getListY() + (5 * this.rowHeight) + (this.screenHeight / 100),
 										this.screenWidth - (2 * this.safeTitleH),
 										this.app.hSkin.paragraphFontSize
 		);
 		enterNote.setFont(app.hSkin.paragraphFont);
 		enterNote.setColor(HSkin.PARAGRAPH_TEXT_COLOR);
 		enterNote.setFlags(RSRC_HALIGN_CENTER + RSRC_VALIGN_BOTTOM);
 		enterNote.setValue("press ENTER to add this album to a playlist");
 		setManagedView(enterNote);
 	}
 	
 	public boolean handleAction(BView view, Object action) {
         if(action.equals("right") || action.equals("select")) {
         	PlaylistEligible musicItem = getListSelection();
        
         	if(musicItem instanceof Disc) {
         		this.app.push(new BrowseDiscScreen(this.app, (Disc)musicItem), TRANSITION_LEFT);
         	}
         	else {
         		if (this.album.getTrackList().size() > 1)
         			this.app.push(new TrackScreen(this.app, (Playable)musicItem, this.album), TRANSITION_LEFT);
         		else
         			this.app.push(new TrackScreen(this.app, (Playable)musicItem), TRANSITION_LEFT);
         	}
             return true;
         } 
 
         return super.handleAction(view, action);
     }
 	
 	/* (non-Javadoc)
 	 * Handles key presses from TiVo remote control.
 	 */
 	@Override
 	public boolean handleKeyPress(int key, long rawcode) {
 		
 		this.app.checkKeyPressToResetInactivityTimer(key);
 		
 		switch(key) {
 		case KEY_PLAY:
 			
 			List<PlaylistEligible> playlist = new ArrayList<PlaylistEligible>();
 			boolean shuffleMode;
 			boolean repeatMode;
 			PlaylistEligible selected = getListSelection();
 			Playable startPlaying = null;
 			
 			// Ian TODO: Something's not right here.  Should we be using album shuffle
 			// 			 mode somewhere?  Why is there even a track shuffle mode?
 			//			 What does it mean to shuffle when you're playing a single
 			//			 track (even though you're not really playing a single track
 			//           *here*, the existence of the setting doesn't make sense to
 			//			 me at the moment.
 			//
 			//			 Also, this logic is also used in TrackScreen.  Maybe this
 			//			 whole block should be pushed down into DiscJockey or something.
 			if(  selected instanceof Disc ) {
 				// Playing an entire disc
				playlist.add( selected );
 				shuffleMode = this.app.getPreferences().getDiscDefaultShuffleMode();
 				repeatMode = this.app.getPreferences().getDiscDefaultRepeatMode();
 			}
 			else {
 				// Playing an individual track
				playlist.add( this.album );
 				shuffleMode = this.app.getPreferences().getTrackDefaultShuffleMode();
 				repeatMode = this.app.getPreferences().getTrackDefaultRepeatMode();
 				startPlaying = (Playable)selected;
 			}
 			this.app.getDiscJockey().play(playlist, shuffleMode, repeatMode, startPlaying);
 			return true;
 		case KEY_ENTER:
 			this.app.play("select.snd");
 			this.app.push(new AddToPlaylistScreen(this.app, this.album), TRANSITION_LEFT);
 			return true;
 		}
 		
 		return super.handleKeyPress(key, rawcode);
 
 	}
 }
