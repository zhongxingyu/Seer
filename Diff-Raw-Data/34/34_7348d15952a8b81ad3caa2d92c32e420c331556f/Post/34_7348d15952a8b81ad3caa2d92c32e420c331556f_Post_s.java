 package tumblib;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 public class Post {
 	//Total number of Posts created.
 	static int totalCount;
 	
 	private long id;
 	private URL url;
 	private URL urlWithSlug;
 	private PostType type;
 	private Date date;
 	private int bookmarklets;
 	private int mobiles;
 	private String reblogKey = "";
 	private String slug;
	private String[] tags = {};
 	
 	//Very long constructor for desarialization from JSON.
 	public Post(long id, String url, String urlWithSlug, PostType type, long date, 
 			int bookmarklets, int mobiles, String reblogKey, String slug, String[] tags){
 		
 		setId(id);
 		setUrl(url);
 		setUrlWithSlug(urlWithSlug);
 		setType(type);
 		setDate(date);
 		setBookmarklets(bookmarklets);
 		setMobiles(mobiles);
 		setReblogKey(reblogKey);
 		setSlug(slug);
 		setTags(tags);
 		
 		totalCount++;	
 	}	
 	
 	public Post(){
 		
 	}
 	
 	// TODO: Remove main after testing
 	public static void main(String[] args){
 		
 	}
 
 	
 	/*Setter and Getter methods for instance variables. */
 	
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setUrl(String urlString) {
 		try {
 			this.url = new URL(urlString);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.err.println("Please verify that the JSON's URL format is correct.");
 			System.err.println(url);
 		}
 	}
 
 	public URL getUrl() {
 		return url;
 	}
 
 	public void setUrlWithSlug(String urlWithSlug) {
 		try {
 			this.url = new URL(urlWithSlug);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.err.println("Please verify that the JSON's URL format is correct.");
 			System.err.println(urlWithSlug);
 		}
 	}
 
 	public URL getUrlWithSlug() {
 		return urlWithSlug;
 	}
 
 	public void setType(PostType type){
 		this.type = type;
 	}
 
 	public PostType getType() {
 		return type;
 	}
 
 	public void setDate(long date) {
 		this.date = new Date(date);
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setBookmarklets(int bookmarklets) {
 		this.bookmarklets = bookmarklets;
 	}
 
 	public int getBookmarklets() {
 		return bookmarklets;
 	}
 
 	public void setMobiles(int mobiles) {
 		this.mobiles = mobiles;
 	}
 
 	public int getMobiles() {
 		return mobiles;
 	}
 
 	public void setReblogKey(String reblogKey) {
 		this.reblogKey = reblogKey;
 	}
 
 	public String getReblogKey() {
 		return reblogKey;
 	}
 
 	public void setSlug(String slug) {
 		this.slug = slug;
 	}
 
 	public String getSlug() {
 		return slug;
 	}
 
 	public void setTags(String[] tags) {
 		this.tags = tags;
 	}
 
 	public String[] getTags() {
 		return tags;
 	}
 	
 }
