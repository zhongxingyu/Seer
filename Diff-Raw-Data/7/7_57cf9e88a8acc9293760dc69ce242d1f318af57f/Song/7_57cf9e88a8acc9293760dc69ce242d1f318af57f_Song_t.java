 package pian.model;
 
 
 public class Song {
 	private int id;
 	private String title;
 	private Artist artist;
 	private String link;
 	private Album album;
 	
 	public Song(){
 		
 	}
 	
 	public Song(String title, String link) {
 		this.title = title;
 		this.artist = null;
 		this.link = link;
 		this.album = null;
 	}
 	
 	public Song(String title, Artist artist, String link,
 			Album album) {
 		this.title = title;
 		this.artist = artist;
 		this.link = link;
 		this.album = album;
 	}
 	
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public Artist getArtist() {
		if (artist == null){
			artist = new Artist("Chưa rõ");
		}
 		return artist;
 	}
 
 	public void setArtist(Artist artist) {
 		this.artist = artist;
 	}
 
 	public String getLink() {
 		return link;
 	}
 
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 	public Album getAlbum() {
		if (album == null){
			album = new Album("Chưa rõ", getArtist());
		}
 		return album;
 	}
 
 	public void setAlbum(Album album) {
 		this.album = album;
 	}
 }
