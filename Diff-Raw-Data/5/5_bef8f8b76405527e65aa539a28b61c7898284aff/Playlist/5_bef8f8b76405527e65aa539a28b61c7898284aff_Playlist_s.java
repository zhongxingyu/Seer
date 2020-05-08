 package com.player.bll;
 
 // Version 2
 
 import java.util.*;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 
 public class Playlist implements VampMediaCollection {
   
   LinkedList<Song> playlist;
   private String name;
  private SessionFactory sf;
   private Session session;
   private UserAccount user;
   private long id;
   private List<PlaylistSong> pSongs;
 
   public Playlist() {
   }
     
   public Playlist( UserAccount user, String nameToUse ) {
     this.user = user;
     playlist = new LinkedList();
     name = nameToUse;
     Transaction tx = DataAccess.getSession().beginTransaction();
     DataAccess.getSession().save( this );
     tx.commit();
   }
   
   public String getName() {
     return name;
   }
   
   public void setName( String name ) {
     this.name = name;
   }
   
   public void setUser( UserAccount user ) {
     this.user = user;
   }
   
   public UserAccount getUser() {
     return user;
   }
   
   public void setId( long id ) {
     this.id = id;
     getSongs();
   }
 
   public long getId() {
     return id;
   }
   
   public void getSongs() {
    Query query = DataAccess.getSession().createQuery( "FROM PlaylistSong WHERE playlist_id = :playlistId" );
     query.setParameter( "playlistId", this.getId() );
     pSongs = query.list();
   }
   
   public Song get( int i ) {
     return pSongs.get( i ).getSong();
   }
   
   public ArrayList<Song> getAll() {
     ArrayList<Song> allSongs = new ArrayList();
     for ( PlaylistSong pS : pSongs ){
       allSongs.add( pS.getSong() );
     }
     return allSongs;
   }
   
   public int size() {
     return playlist.size();
   }
   
   public void add( Song songToAdd ) {
     PlaylistSong newPSong = new PlaylistSong( this, songToAdd );
     Transaction tx = DataAccess.getSession().beginTransaction();
     DataAccess.getSession().save( newPSong );
     tx.commit();
     pSongs.add( newPSong );
   }
 
   public void add( int i, Song songToAdd ) {
     playlist.add( i, songToAdd );
   }
   
   public void addAll( List<Song> listToAdd ) {
     for( Song s : listToAdd ) {
       add( s );
     }
   }
   
   public boolean contains( Song songToCheck ) {
     return playlist.contains( songToCheck );
   }
   
   public void remove( PlaylistSong s ) {
     pSongs.remove( s );
     DataAccess.getSession().delete( s );
     Transaction tx = DataAccess.getSession().beginTransaction();
     tx.commit();
   }
 
   public void remove( int i ) {
     remove( pSongs.get( i ) );
   }
 
     public Object[][] getDataVector() {
     int arraySize = pSongs.size();
     Object[][] objectToReturn = new Object[arraySize][5];
     for ( int i = 0; i < arraySize; i++ ) {
       Song song = pSongs.get( i ).getSong();
       objectToReturn[i] = new Object[]{
         song.getTitle(),
         song.getArtist(),
         String.valueOf( song.getFormatedLength() ),
         song.getAlbum(),
         String.valueOf( song.getTrackNumber() )
       };
     }
     return objectToReturn;
   }
 }
