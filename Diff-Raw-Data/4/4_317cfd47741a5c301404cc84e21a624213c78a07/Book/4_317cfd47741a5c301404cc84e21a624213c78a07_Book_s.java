 package edu.upc.dsbw.spring.business.model;
 
 
 //import java.util.HashMap;
 //import java.util.HashSet;
 //import java.util.Iterator;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 //import java.util.Map;
 //import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.CollectionId;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.hibernate.annotations.IndexColumn;
 import org.hibernate.annotations.LazyCollection;
 import org.hibernate.annotations.LazyCollectionOption;
 import org.hibernate.annotations.Type;
 import org.springframework.web.multipart.MultipartFile;
 
 import edu.upc.dsbw.spring.business.dao.FileUpload;
 
 @Entity
 @Table(name = "BOOK")
 public class Book {
 	
 	private static final int DEF_WIDTH = 210;
 	private static final int DEF_HEIGHT = 265;
 	private static final String DEF_INTERNAL_FOLDER = "target" + File.separator + "mybookshelf" + File.separator + "files" + File.separator + "books";
 	private static final String DEF_EXTERNAL_FOLDER = "/" + "files" + "/" + "books";
 	private static final String DEF_FORMAT = "png";
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "BOOK_ID", unique = true, nullable = false)
 	private Integer id;	
 
 	
 
 	@ManyToMany	(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
 	@JoinTable(name = "BOOK_AUTHOR",  joinColumns = { 
 			@JoinColumn(name = "BOOKID", nullable = false, updatable = false) }, 
 			inverseJoinColumns = { @JoinColumn(name = "AUTHORID", 
 					nullable = false, updatable = false) })
 	@Fetch(value = FetchMode.SUBSELECT)
 	@OrderBy("firstName, lastName")
 //	@CollectionId(
 //			columns = @Column(name = "author_id"),
 //			type = @Type(type = "integer"),
 //			generator = "identity")
 //	@IndexColumn(name="BOOKID")
 //	@LazyCollection(value = LazyCollectionOption.FALSE)
 	private List<Author> authors =  new LinkedList<Author>();
 //	private Set<Author> authors =  new HashSet<Author>();
 	 
 	@Column(name = "TITLE", nullable = false)
 	private String title;
 
 	@Column(name = "PUBLICATION_YEAR", nullable = false)
 	private int publicationYear;
 
 	@Column(name = "INSERTION_DATE")
 	private java.util.Date insertionDate;
 
 	@Column(name = "AVRATING")
 	private Float avRating;
 
 	@Column(name = "DESCRIPTION")
 	private String description;
 	
 	@Column(name = "UPLOADER")
 	private int uploader;
 	
 	@Column(name = "EDITORIAL")
 	private String editorial;
 	
 	@Column(name = "POPULARITY")
 	private int popularity;
 
 	@OneToMany(mappedBy = "book" , fetch=FetchType.EAGER)
 //	private Set<UserBook> userBooks = new HashSet<UserBook>();
 	@Fetch(value = FetchMode.SUBSELECT)
 	private List<UserBook> userBooks = new LinkedList<UserBook>();	
 	
 	public Book() {
 	}
 //	public Book() {
 //
 //		this.ratings = new LinkedList<Integer>();
 //		this.avRating = 0;
 //		this.description = description;
 //		
 //		this.votes = new HashMap<String, Integer>();
 //
 //	}
 
 	
 	public Book(String title, int publicationYear) {
 		this.setTitle(title);
 		this.setPublicationYear(publicationYear);
 //		this.setInsertionDate(new java.util.Date());
 	}
 	
 	public Book(String title, int publicationYear, String description) {
 		this.setTitle(title);
 		this.setPublicationYear(publicationYear);
 //		this.setInsertionDate(new java.util.Date());
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public int getPublicationYear() {
 		return publicationYear;
 	}
 
 	public void setPublicationYear(int publicationYear) {
 		this.publicationYear = publicationYear;
 	}
 
 	public java.util.Date getInsertionDate() {
 		return insertionDate;
 	}
 
 	public void setInsertionDate(java.util.Date insertionDate) {
 		this.insertionDate = insertionDate;
 	}
 	
 //	public HashMap<String,Integer> getVotes(){
 //	  return votes;
 //	}
 
 	public Float getAvRating() {
 		return avRating;
 	}
 
 	public void setAvRating(Float avRating) {
 		this.avRating = avRating;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 //	public void addVote(String username, int vote){
 //	  this.votes.put(username,vote);
 //	  this.avRating = this.calculateAverageRating();
 //	}
 
 //	public List<UserBook> getUserBooks() {
 //		return new LinkedList<UserBook>(this.userBooks);
 //	}
 
 
 //	public void setUserBooks(List<UserBook> userBooks) {
 //		this.userBooks = new HashSet<UserBook>(userBooks);
 //	}
 	
 
 
 //	public List<Author> getAuthors() {
 //		return new LinkedList<Author>(this.authors);
 //	}
 
 
 //	public void setAuthors(List<Author> authors) {
 //		this.authors = new HashSet<Author>(authors);
 //	}
 
 
 	public void addAuthor(Author author) {
 		this.authors.add(author);
 		
 	}
 
 	public void removeAuthor(Author author) {
 		this.authors.remove(author);
		
  
   	public void emptyAuthors() {
 		this.authors.clear();
 	}
 
 	public List<UserBook> getUserBooks() {
 		return userBooks;
 	}
 
 
 	public void setAuthors(List<Author> authors) {
 		this.authors = authors;
 	}
 
 	public List<Author> getAuthors() {
 		return authors;
 	}
 
 	public void setUserBooks(List<UserBook> userBooks) {
 		this.userBooks = userBooks;
 	}
 	
 	public String setImage(MultipartFile file) throws IOException {
 		FileUpload.doUpload(file, DEF_INTERNAL_FOLDER, getInternalImagePath(), DEF_WIDTH, DEF_HEIGHT, DEF_FORMAT);
         
         return getImagePath();
     }
 	
 	private String getFileName() {
 		return this.getId().toString() + "." + DEF_FORMAT;
 	}
 	
 	private String getInternalImagePath() {
 		return DEF_INTERNAL_FOLDER + File.separator + getFileName();
 	}
 	
 	public String getImagePath() {
 		String theoreticalExternalPath = DEF_EXTERNAL_FOLDER + "/" + getFileName();
 		
 		Random rand = new Random(System.nanoTime());
 		
 		Integer numb = new Integer(rand.nextInt());
 		
 		if (new File(getInternalImagePath()).exists()) return theoreticalExternalPath + "?" + String.valueOf(numb*Integer.signum(numb));
 		return null;
 	}
 	
 //
 //	public List<User> getUsers() {
 //		return new LinkedList<User>(this.users);
 //	}
 //
 //
 //	public void setUsers(List<User> users) {
 //		this.users = new HashSet<User>(users);
 //	}
 
 
 	public int getUploader() {
 		return uploader;
 	}
 
 
 	public void setUploader(int uploader) {
 		this.uploader = uploader;
 	}
 
 
 	public String getEditorial() {
 		return editorial;
 	}
 
 
 	public void setEditorial(String editorial) {
 		this.editorial = editorial;
 	}
 
 
 	public int getPopularity() {
 		return popularity;
 	}
 	
 	public void incrPopularity() {
 		++this.popularity;
 	}
 
 
 	public void setPopularity(int popularity) {
 		this.popularity = popularity;
 	}
 
 	// Book(Author author, String title, int publicationDate, String
 	// description) {
 	// this.author = author;
 	// this.title = title;
 	// this.publicationYear = publicationDate;
 	// this.insertionDate = new java.util.Date();
 	//
 	// // this.ratings = new LinkedList<Integer>();
 	// this.avRating = 0;
 	// this.description = description;
 	// }
 	//
 	// public void remove() {
 	// author.removeBook(this);
 	// }
 	//
 	// public void update(Author author, String title, int publicationDate,
 	// String description) {
 	// if (!this.author.equals(author)) {
 	// this.author.removeBook(this);
 	// this.author = author;
 	// author.addBook(this);
 	// }
 	// this.title = title;
 	// this.publicationYear = publicationDate;
 	// this.description = description;
 	// // S'hauria de mirar que posar aqui..
 	// this.avRating = 0;
 	// }
 
 	// public Integer getId() {
 	// return id;
 	// }
 	//
 	// public void setId(Integer id) {
 	// if (this.id != null && this.id != id) {
 	// throw new IllegalStateException();
 	// }
 	// this.id = id;
 	//
 	// }
 	//
 	// public String getTitle() {
 	// return title;
 	// }
 
 	// public Author getAuthor() {
 	// return author;
 	// }
 
 	// public int getPublicationYear() {
 	// return publicationYear;
 	// }
 	//
 	// public java.util.Date getInsertionDate() {
 	// return insertionDate;
 	// }
 
 	// public LinkedList<Integer> getRatings() {
 	// return ratings;
 	// }
 
 	// public float getAvRating() {
 	// return avRating;
 	// }
 
 	// public String getDescription() {
 	// return description;
 	// }
 
 	// public void addRating(int rating) {
 	// this.ratings.add(rating);
 	// this.avRating = this.calculateAverageRating();
 	// }
 	//
 	// private float calculateAverageRating() {
 	// int sum = 0;
 	// float avrating;
 	// for (int i = 0; i < this.ratings.size(); i++) {
 	// sum += this.ratings.get(i);
 	// }
 	// avrating = (float) sum / (float) this.ratings.size();
 	// return avrating;
 	// }
 
 
 //  private float calculateAverageRating() {
 //		int sum = 0;
 //		float avrating;
 //		Iterator it = this.votes.entrySet().iterator();
 //    for (Map.Entry<String, Integer> entry : this.votes.entrySet())
 //    {
 //        // System.out.println(entry.getKey() + "/" + entry.getValue());
 //        sum += entry.getValue();
 //    }
 //    
 //    avrating = (float) sum / (float) this.votes.size();
 //    return avrating;
 //	}
 	
 //	public HashMap<Integer,Integer> votesByValue(){
 //	  HashMap<Integer, Integer> votesByValue = new HashMap<Integer, Integer>();
 //	  
 //	  votesByValue.put(1,0);
 //    votesByValue.put(2,0);
 //    votesByValue.put(3,0);
 //    votesByValue.put(4,0);
 //    votesByValue.put(5,0);
 //	  
 //	  Iterator it = this.votes.entrySet().iterator();
 //    for (Map.Entry<String, Integer> entry : this.votes.entrySet())
 //    {
 //        votesByValue.put(entry.getValue(), votesByValue.get(entry.getValue())+1 );
 //    }
 //    
 //    return votesByValue;
 //	}
 	
 //	public Integer getNumVotes(){
 //	  int sum = 0;
 //	  Iterator it = this.votes.entrySet().iterator();
 //    for (Map.Entry<String, Integer> entry : this.votes.entrySet())
 //    {
 //        // System.out.println(entry.getKey()+": "+entry.getValue());
 //        sum += 1;
 //    }
 //    
 //    return sum;
 //	}
 //	
 //	public Integer getVoteBy(String username){
 //	  return this.votes.get(username);
 //	}
 
 }
