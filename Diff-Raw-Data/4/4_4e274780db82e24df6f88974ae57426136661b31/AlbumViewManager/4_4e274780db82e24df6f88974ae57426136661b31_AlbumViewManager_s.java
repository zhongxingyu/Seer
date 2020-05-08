 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.controller.managers;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.sammelbox.controller.events.EventObservable;
 import org.sammelbox.controller.events.SammelboxEvent;
 import org.sammelbox.controller.filesystem.XMLStorageWrapper;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AlbumViewManager {
 	private final static Logger LOGGER = LoggerFactory.getLogger(AlbumViewManager.class);
 	private static Map<String, List<AlbumView>> albumNamesToAlbumViews = new HashMap<String, List<AlbumView>>();
 		
 	/** Initializes album views without notifying any attached observers */
 	public static void initialize() {
 		AlbumViewManager.loadViews();
 	}
 	
 	/** Returns the complete list of album view names for all available albums */
 	public static List<String> getAlbumViewNames() {
 		List<String> allViewNames = new LinkedList<String>();
 		
 		for (String albumName : albumNamesToAlbumViews.keySet()) {
 			for (AlbumView albumView : albumNamesToAlbumViews.get(albumName)) {
 				allViewNames.add(albumView.name);
 			}
 		}
 		
 		return allViewNames;
 	}
 	
 	/** Returns the list of album views for a specific album */
 	public static List<AlbumView> getAlbumViews(String albumName) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		if (albumViews != null) {
 			return albumViews;
 		}
 		
 		return new LinkedList<AlbumView>();
 	}
 	
 	public static String[] getAlbumViewNamesArray(String albumName) {
 		List<AlbumView> albumViews = getAlbumViews(albumName);
 		
 		String[] albumViewNames = new String[albumViews.size()];
 		
 		for (int i=0; i<albumViews.size(); i++) {
 			albumViewNames[i] = albumViews.get(i).getName();
 		}
 		
 		return albumViewNames;
 	}
 	
 	public static void addAlbumView(String name, String album, String sqlQuery) {
 		if (albumNamesToAlbumViews.get(album) == null) {
 			List<AlbumView> albumViews = new LinkedList<>();
 			albumViews.add(new AlbumView(name, album, sqlQuery));
 			albumNamesToAlbumViews.put(album, albumViews);
 		} else {
 			List<AlbumView> albumViews = albumNamesToAlbumViews.get(album);
 			albumViews.add(new AlbumView(name, album, sqlQuery));
 			albumNamesToAlbumViews.put(album, albumViews);
 		}
 		
 		storeViewAndAddAlbumViewListUpdatedEvent();
 	}
 	
 	public static void removeAlbumView(String albumName, String viewName) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		for (AlbumView albumView : albumViews) {
 			if (albumView.getName().equals(viewName)) {
 				albumViews.remove(albumView);
 				break;
 			}
 		}
 		
 		storeViewAndAddAlbumViewListUpdatedEvent();
 	}
 	
 	public static boolean hasViewWithName(String albumName, String viewName) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		for (AlbumView albumView : albumViews) {
 			if (albumView.name.equals(viewName)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private static void storeViews() {
 		XMLStorageWrapper.storeViews(albumNamesToAlbumViews);
 	}
 	
 	private static void loadViews() {
 		albumNamesToAlbumViews = XMLStorageWrapper.retrieveViews();
 		
 		for (String albumName : albumNamesToAlbumViews.keySet()) {
 			try {
 				if (!DatabaseOperations.getListOfAllAlbums().contains(albumName)) {
 					albumNamesToAlbumViews.remove(albumName);
 				}
 			} catch (DatabaseWrapperOperationException ex) {
 				LOGGER.error("An error occured while retrieving the list of albums from the database \n Stacktrace: ", ex);
 			}
 		}
 		
 		AlbumViewManager.storeViews();
 	}
 	
 	public static class AlbumView {
 		private String name;
 		private String album;
 		private String sqlQuery;
 				
 		public AlbumView(String name, String album, String sqlQuery) {
 			this.name = name;
 			this.album = album;
 			this.sqlQuery = sqlQuery;
 		}
 				
 		public String getName() {
 			return name;
 		}
 		
 		public void setName(String name) {
 			this.name = name;
 		}
 		
 		public String getAlbum() {
 			return album;
 		}
 
 		public void setAlbum(String album) {
 			this.album = album;
 		}
 
 		public String getSqlQuery() {
 			return sqlQuery;
 		}
 		
 		public void setSqlQuery(String sqlQuery) {
 			this.sqlQuery = sqlQuery;
 		}
 	}
 
 	public static String getSqlQueryByViewName(String albumName, String viewName) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		for (AlbumView albumView : albumViews) {
 			if (albumView.getName().equals(viewName)) {
 				return albumView.getSqlQuery();
 			}
 		}
 		
 		return null;
 	}
 
 	public static boolean hasAlbumViewsAttached(String albumName) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		if (albumViews != null) {
 			for (AlbumView albumView : albumViews) {
 				if (albumView.getAlbum().equals(albumName)) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	public static void moveToFront(String albumName, int selectionIndex) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex);
 		((LinkedList<AlbumView>) albumViews).remove(selectionIndex);
 		((LinkedList<AlbumView>) albumViews).addFirst(tmp);
 		
 		storeViewAndAddAlbumViewListUpdatedEvent();
 	}
 
 	public static void moveOneUp(String albumName, int selectionIndex) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		if (selectionIndex-1 >= 0) {
 			AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex-1);
 			
 			((LinkedList<AlbumView>) albumViews).set(selectionIndex-1, ((LinkedList<AlbumView>) albumViews).get(selectionIndex));
 			((LinkedList<AlbumView>) albumViews).set(selectionIndex, tmp);
 			
 			storeViewAndAddAlbumViewListUpdatedEvent();
 		}
 	}
 
 	public static void moveOneDown(String albumName, int selectionIndex) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		if (selectionIndex+1 <= albumViews.size()-1) {
 			AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex+1);
 			
 			((LinkedList<AlbumView>) albumViews).set(selectionIndex+1, ((LinkedList<AlbumView>) albumViews).get(selectionIndex));
 			((LinkedList<AlbumView>) albumViews).set(selectionIndex, tmp);
 			
 			storeViewAndAddAlbumViewListUpdatedEvent();
 		}
 	}
 
 	public static void moveToBottom(String albumName, int selectionIndex) {
 		List<AlbumView> albumViews = albumNamesToAlbumViews.get(albumName);
 		
 		AlbumView tmp = ((LinkedList<AlbumView>) albumViews).get(selectionIndex);
 		((LinkedList<AlbumView>) albumViews).remove(selectionIndex);
 		((LinkedList<AlbumView>) albumViews).addLast(tmp);
 		
 		storeViewAndAddAlbumViewListUpdatedEvent();
 	}
 
 	public static void removeAlbumViewsFromAlbum(String albumName) {
 		albumNamesToAlbumViews.remove(albumName);
 		
 		storeViewAndAddAlbumViewListUpdatedEvent();
 	}
 	
 	private static void storeViewAndAddAlbumViewListUpdatedEvent() {
 		storeViews();
 		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_VIEW_LIST_UPDATED);
 	}
 }
