 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import org.joda.time.DateTime;
 
 import play.db.ebean.*;
 import play.db.ebean.Model.Finder;
 import play.mvc.Result;
 import models.*;
 
 @Entity
 @Table(name="Public_Memento")
 public class PublicMemento extends Model {
 
 	@Id
 	@Column (name = "public_memento_id")
 	private Long publicMementoId;
 
 	@Column
 	private char headline;
 
 	@Column
 	private String text;
 	
 	@Column
 	private char type;
 	
 	@Column (name = "resource_type")
 	private char resourceType;
 	
 	@Column
 	private char category;
 	
 	@Column (name = "resource_url")
 	private String resourceUrl;
 	
 	@Column
 	private String author;
 	
 	@Column
 	private char locale;
 	
 	@Column (name = "creation_date")
 	private DateTime creationDate;
 	
 	@Column (name = "resource_thumbnail_url")
 	private String resourceThumbnailUrl;
 	
 	@Column
 	private char source;
 	
 	@Column (name = "source_url")
 	private String sourceUrl;
 	
 	@ManyToOne
 	@JoinColumn (name = "fuzzy_startdate", updatable=true, insertable=true)
 	private FuzzyDate startDate;
 	
 	@ManyToOne
 	@JoinColumn (name = "location_start_id", updatable=true, insertable=true)
 	private Location startId;
 	
 	@ManyToOne
 	@JoinColumn (name = "fuzzy_enddate", updatable=true, insertable=true)
 	private FuzzyDate endDate;
 	
 	@ManyToOne
 	@JoinColumn (name = "location_end_id", updatable=true, insertable=true)
 	private Location endId;
 	
 	
 //metodi
 
 	public static Finder<Long,PublicMemento> find = new Finder(
 		    Long.class, PublicMemento.class
 		  );
   
   public static List<PublicMemento> all() {
     return find.all();
   }
   
   public static void create(PublicMemento context) {
 	  context.save();
   }
   
   public static void delete(Long id) {
 	  find.ref(id).delete();
   }
   
   public static void update(Long id) {
 	  find.ref(id).update();
   }
 	
   public static PublicMemento getPublicMementoByLocation (Long locationId) {
	  List <PublicMemento> publicMementoLocation = find.where().eq("location_id", locationId)
 			  .eq("updatable", true)
 			  .findList();
 	  if (publicMementoLocation != null && !publicMementoLocation.isEmpty()) {
 		  return publicMementoLocation.get(publicMementoLocation.size()-1);
 	  }
 	  else return null;
   }
 	
   public static PublicMemento getPublicMementoByDate (Long fuzzyDateId) {
	  List <PublicMemento> publicMementoDate = find.where().eq("fuzzy_date_id", fuzzyDateId)
 			  .eq("updatable", true)
 			  .findList();
 	  if (publicMementoDate != null && !publicMementoDate.isEmpty()) {
 		  return publicMementoDate.get(publicMementoDate.size()-1);
 	  }
 	  else return null;
   }
   
   public static PublicMemento getPublicMementoById (Long publicMementoId) {
	  PublicMemento publicMemento = find.where().eq("public_memento_id", publicMementoId)
 			  .findUnique();
 	  return publicMemento;
   }
 
 
 //getter e setter	
   
   public Long getPublicMementoId() {
 		return publicMementoId;
 	}
 
 	public void setPublicMementoId(Long publicMementoId) {
 		this.publicMementoId = publicMementoId;
 	}
 
 	public char getHeadline() {
 		return headline;
 	}
 
 	public void setHeadline(char headline) {
 		this.headline = headline;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public char getType() {
 		return type;
 	}
 
 	public void setType(char type) {
 		this.type = type;
 	}
 
 	public char getResourceType() {
 		return resourceType;
 	}
 
 	public void setResourceType(char resourceType) {
 		this.resourceType = resourceType;
 	}
 
 	public char getCategory() {
 		return category;
 	}
 
 	public void setCategory(char category) {
 		this.category = category;
 	}
 
 	public String getResourceUrl() {
 		return resourceUrl;
 	}
 
 	public void setResourceUrl(String resourceUrl) {
 		this.resourceUrl = resourceUrl;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	public char getLocale() {
 		return locale;
 	}
 
 	public void setLocale(char locale) {
 		this.locale = locale;
 	}
 
 	public DateTime getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(DateTime creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public String getResourceThumbnailUrl() {
 		return resourceThumbnailUrl;
 	}
 
 	public void setResourceThumbnailUrl(String resourceThumbnailUrl) {
 		this.resourceThumbnailUrl = resourceThumbnailUrl;
 	}
 
 	public char getSource() {
 		return source;
 	}
 
 	public void setSource(char source) {
 		this.source = source;
 	}
 
 	public String getSourceUrl() {
 		return sourceUrl;
 	}
 
 	public void setSourceUrl(String sourceUrl) {
 		this.sourceUrl = sourceUrl;
 	}
 
 	public FuzzyDate getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(FuzzyDate startDate) {
 		this.startDate = startDate;
 	}
 
 	public Location getStartId() {
 		return startId;
 	}
 
 	public void setStartId(Location startId) {
 		this.startId = startId;
 	}
 
 	public FuzzyDate getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(FuzzyDate endDate) {
 		this.endDate = endDate;
 	}
 
 	public Location getEndId() {
 		return endId;
 	}
 
 	public void setEndId(Location endId) {
 		this.endId = endId;
 	}
 }
