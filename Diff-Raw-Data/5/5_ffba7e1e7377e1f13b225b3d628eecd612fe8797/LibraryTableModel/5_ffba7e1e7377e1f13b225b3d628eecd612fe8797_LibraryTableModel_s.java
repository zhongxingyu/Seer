 package iitm.apl.player.ui;
 
 import iitm.apl.bktree.BKTree;
 import iitm.apl.player.Song;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 import javax.swing.table.AbstractTableModel;
 
 /**
  * Table model for a library
  * 
  */
 public class LibraryTableModel extends AbstractTableModel {
 	private static final long serialVersionUID = 8230354699902953693L;
 
 	// TODO: Change to your implementation of Trie/BK-Tree
 	public static int thresholdDistance = 2;
 	private Vector<Song> songListing;
 	private int songIteratorIdx;
 	private Song currentSong;
 	private Iterator<Song> songIterator;
 
 	LibraryTableModel() {
 		songListing = new Vector<Song>();
 		songIterator = songListing.iterator();
 	}
 
 	public void add(Song song) {
 		songListing.add(song);
 		resetIdx();
 		fireTableDataChanged();
 	}
 
 	public void add(Vector<Song> songs) {
 		songListing.addAll(songs);
 		resetIdx();
 		fireTableDataChanged();
 	}
 	public void clearSongListing()
 	{
 		songListing.removeAllElements();
 	}
 	public Song getNextSong()
 	{
 		if(!songListing.contains(currentSong)) {
 			currentSong = songListing.firstElement();
 			return currentSong;
 		}
 		int i = songListing.indexOf(currentSong);
		if(songListing.capacity() > i) {
 			currentSong = songListing.get(i+1);
 			return currentSong;
 		}
 		else {
 			currentSong = songListing.firstElement();
 			return currentSong;
 		}
 	}
 	public Song getPrevSong()
 	{
 		if(!songListing.contains(currentSong)) {
 			currentSong = songListing.firstElement();
 			return currentSong;
 		}
 		int i = songListing.indexOf(currentSong);
		if(songListing.capacity() > 1) {
 			currentSong = songListing.get(i-1);
 			return currentSong;
 		}
 		else {
 			currentSong = songListing.firstElement();
 			return currentSong;
 		}
 	}
 	public void filter(String searchTerm, BKTree<String> songTree, Vector<Song> songVector, Hashtable wordToSong) {
 		// TODO: Connect the searchText keyPressed handler to update the filter
 		// here.
 		
 		searchTerm = searchTerm.toLowerCase();
 		/* Suppose the user has not typed any text, display all the songs and return */
 		if(searchTerm.compareTo("") == 0)
 		{
 			HashMap<String, Integer> filteredSongs = songTree.makeQuery("", 100);
 			Set<String> fsl = filteredSongs.keySet();
 			Vector<String> filteredSongsList = new Vector<String>(fsl);
 			songListing.removeAllElements();
 			for(String string : filteredSongsList)
 			{
 				Vector<Song> vSong = (Vector<Song>) wordToSong.get(string);
 				for( Song song : vSong )
 				{
 					if(!songListing.contains(song)) songListing.add(song);
 				}
 			}
 			resetIdx();
 			fireTableDataChanged();
 			return;
 		}
 		
 		/* Split the searchTerm in to its words */
 		String[] searchTermWords = searchTerm.split(" ");
 		
 		/*Initial checking with only one word - later implement loop and extend to multiple words */
 		songListing.removeAllElements();
 		
 		/* First checking for substrings and starting prefixes - highest preference */
 		
 		Vector<Song> yin = new Vector<Song>();
 		Vector<Song> yang = new Vector<Song>();
 		boolean isYin = true;
 		/* The Yin-Yang funda :
 		 * 	Basically, to find the results for multiple search words, find the ones for each word, and then take their 
 		 * intersection. This is done using only two vector lists. An element is added to one list, only if it is already
 		 * present in the other list (except the starting list, of course)
 		 * */
 		for(int i = 0; i < searchTermWords.length; i++)
 		{
 			if(isYin)
 			{
 				// Must remove all elements from the list before re-adding, else multiple copies of same song in each list 
 				yin.removeAllElements();
 				for(Song song : songVector)
 				{
 					if(song.getTitle().toLowerCase().startsWith(searchTermWords[i]) || song.getAlbum().toLowerCase().startsWith(searchTermWords[i]) || song.getArtist().toLowerCase().startsWith(searchTermWords[i]))
 					{
 						if(!yin.contains(song)) 
 						{
 							if(i == 0) yin.add(song);
 							if(i > 0 && yang.contains(song)) yin.add(song);
 						}
 					}
 					if(song.getTitle().toLowerCase().contains(searchTermWords[i]) || song.getAlbum().toLowerCase().contains(searchTermWords[i]) || song.getArtist().toLowerCase().contains(searchTermWords[i]) )
 					{
 						if(!yin.contains(song))
 						{
 							if(i == 0) yin.add(song);
 							if( i > 0 && yang.contains(song)) yin.add(song);
 						}
 					}
 				}
 				HashMap<String, Integer> filteredSongs = songTree.makeQuery(searchTermWords[i], thresholdDistance);
 				System.out.println("The query of searchTermWords["+i+"] returned :"+filteredSongs);
 				Set<String> fsl = filteredSongs.keySet();
 				Vector<String> filteredSongsList = new Vector<String>(fsl);
 				System.out.println("The song list under queue for yin is " + fsl);
 				for(String string : filteredSongsList)
 				{
 					Vector<Song> vSong = (Vector<Song>) wordToSong.get(string);
 					System.out.println("The song list under queue for the word \""+string+"\" is "+vSong);
 					for( Song song : vSong )
 					{
 						if(!yin.contains(song))
 						{
 							if(i == 0) yin.add(song);
 							if( i > 0 && yang.contains(song)) yin.add(song);
 						}
 					}
 				}
 				System.out.println(i + ": final shortlisted songs : yin : "+ yin);
 				isYin = !isYin;
 			}
 			else
 			{
 				yang.removeAllElements();
 				for(Song song : songVector)
 				{
 					if(song.getTitle().toLowerCase().startsWith(searchTermWords[i]) || song.getAlbum().toLowerCase().startsWith(searchTermWords[i]) || song.getArtist().toLowerCase().startsWith(searchTermWords[i]))
 					{
 						if(!yang.contains(song) && yin.contains(song)) yang.add(song); 
 					}
 					if(song.getTitle().toLowerCase().contains(searchTermWords[i]) || song.getAlbum().toLowerCase().contains(searchTermWords[i]) || song.getArtist().toLowerCase().contains(searchTermWords[i]) )
 					{
 						if(!yang.contains(song) && yin.contains(song)) yang.add(song); 
 					}
 				}
 				HashMap<String, Integer> filteredSongs = songTree.makeQuery(searchTermWords[i], thresholdDistance);
 				System.out.println("The query of searchTermWords["+i+"] returned :"+filteredSongs);
 				Set<String> fsl = filteredSongs.keySet();
 				Vector<String> filteredSongsList = new Vector<String>(fsl);
 				for(String string : filteredSongsList)
 				{
 					Vector<Song> vSong = (Vector<Song>) wordToSong.get(string);
 					System.out.println("The song list under queue for the word \""+string+"\" is "+vSong);
 					for( Song song : vSong )
 					{
 						if(!yang.contains(song) && yin.contains(song)) yang.add(song);
 					}
 				}
 				isYin = !isYin;
 				System.out.println(i + " :final shortlisted songs for yang : "+ yang);
 			}
 		}
 		if(isYin) songListing = new Vector<Song>(yang); 
 		else songListing = new Vector<Song>(yin);
 		resetIdx();
 		fireTableDataChanged();
 	}
 	private class DurationComparator implements Comparator<Song> 
 	{
         @Override
         public int compare(Song arg0, Song arg1) 
         {
                 return arg1.getDuration() - arg0.getDuration();
         }
 	}
 	private class TitleComparator implements Comparator<Song> 
 	{
         @Override
         public int compare(Song arg0, Song arg1) 
         {
                 return arg0.getTitle().compareTo(arg1.getTitle());
         }
 	}
 	private class AlbumComparator implements Comparator<Song> 
 	{
         @Override
         public int compare(Song arg0, Song arg1) 
         {
                 return arg0.getAlbum().compareTo(arg1.getAlbum());
         }
 	}
 	private class ArtistComparator implements Comparator<Song> 
 	{
         @Override
         public int compare(Song arg0, Song arg1) 
         {
                 return arg0.getArtist().compareTo(arg1.getArtist());
         }
 	}
 	/*
 	 * @param - choice:
 	 *	1 - title
 	 *  2 - album
 	 *  3 - artist
 	 *  4 - duration
 	 * */
 	public void sortSongListing (int choice) 
 	{
         Song[] songs = new Song[songListing.size()];
         for ( int i = 0; i < songListing.size(); i++ )
                 songs[i] = songListing.elementAt(i);
 
         if(choice == 1) Arrays.sort( songs, new TitleComparator() );
         if(choice == 2) Arrays.sort( songs, new AlbumComparator() );
         if(choice == 3) Arrays.sort( songs, new ArtistComparator() );
         if(choice == 4) Arrays.sort( songs, new DurationComparator() );
 
         for ( int i = 0; i < songListing.size(); i++ )
                 songListing.setElementAt( songs[i], i );
 
         fireTableDataChanged();
 	}
 	
 	
 	public void resetIdx()
 	{
 		songIteratorIdx = -1;
 		currentSong = null;
 		songIterator = songListing.iterator();
 	}
 	// Gets the song at the currently visible index
 	public Song get(int idx) {
 		if( songIteratorIdx == idx )
 			return currentSong;
 		
 		if(songIteratorIdx > idx)
 		{
 			resetIdx();
 		}
 		while( songIteratorIdx < idx && songIterator.hasNext() )
 		{
 			currentSong = songIterator.next();
 			songIteratorIdx++;
 		}
 		return currentSong;
 	}
 
 	@Override
 	public int getColumnCount() {
 		// Title, Album, Artist, Duration.
 		return 4;
 	}
 
 	@Override
 	public int getRowCount() {
 		// TODO: Changes if you've filtered the list
 		return songListing.size();
 	}
 
 	@Override
 	public Object getValueAt(int row, int col) {
 		// TODO: Get the appropriate row
 		Song song = get(row);
 		if(song == null) return null;
 
 		switch (col) {
 		case 0: // Title
 			return song.getTitle();
 		case 1: // Album
 			return song.getAlbum();
 		case 2: // Artist
 			return song.getArtist();
 		case 3: // Duration
 			int duration = song.getDuration();
 			int mins = duration / 60;
 			int secs = duration % 60;
 			return String.format("%d:%2d", mins, secs);
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public String getColumnName(int column) {
 		switch (column) {
 		case 0: // Title
 			return "Title";
 		case 1: // Album
 			return "Album";
 		case 2: // Artist
 			return "Artist";
 		case 3: // Duration
 			return "Duration";
 		default:
 			return super.getColumnName(column);
 		}
 	}
 
 }
