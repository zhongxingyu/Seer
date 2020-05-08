 package fr.kaddath.apps.fluxx.domain;
 
 import javax.persistence.NamedQuery;
 import javax.persistence.NamedQueries;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 
 
 import static javax.persistence.CascadeType.*;
 
 @Entity
 @NamedQueries(@NamedQuery(name="findItemsByLink",query="SELECT item FROM Item item WHERE item.link = :link"))
 public class Item implements Comparable<Item> {
 
     @Id
     @GeneratedValue(strategy=GenerationType.AUTO)
     private Long id;
 
     @NotNull
     @Column(length=20480)
     private String link;
 
     private String author;
 
     @Lob
     private String description;
 
     private String descriptionType;
 
    @Lob
     private String uri;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date publishedDate;
 
     private String title;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date updatedDate;
 
     @OneToMany(cascade = {ALL}, mappedBy="item")
     private Set<DownloadableItem> downloadableItems = new HashSet<DownloadableItem>();
 
     @ManyToMany(cascade = {DETACH,MERGE,PERSIST,REFRESH})
     private Set<FeedCategory> feedCategories = new HashSet<FeedCategory>();
 
     @ManyToOne
     private Feed feed;
 
     @Override
     public boolean equals(Object o) {
         return link.equals(((Item)o).link);
     }
 
     @Override
     public int hashCode() {
         return link.hashCode();
     }
 
     @Override
     public String toString() {
     	return getTitle();
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getDescriptionType() {
         return descriptionType;
     }
 
     public void setDescriptionType(String descriptionType) {
         this.descriptionType = descriptionType;
     }
 
     public Set<DownloadableItem> getDownloadableItems() {
         return downloadableItems;
     }
 
     public void setDownloadableItems(Set<DownloadableItem> downloadableItems) {
         this.downloadableItems = downloadableItems;
     }
 
     public Feed getFeed() {
         return feed;
     }
 
     public void setFeed(Feed feed) {
         this.feed = feed;
     }
 
     public Set<FeedCategory> getFeedCategories() {
         return feedCategories;
     }
 
     public void setFeedCategories(Set<FeedCategory> feedCategories) {
         this.feedCategories = feedCategories;
     }
 
     public String getLink() {
         return link;
     }
 
     public void setLink(String link) {
         this.link = link;
     }
 
     public Date getPublishedDate() {
         return publishedDate;
     }
 
     public void setPublishedDate(Date publishedDate) {
         this.publishedDate = publishedDate;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Date getUpdatedDate() {
         return updatedDate;
     }
 
     public void setUpdatedDate(Date updatedDate) {
         this.updatedDate = updatedDate;
     }
 
     public String getUri() {
         return uri;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     @Override
     public int compareTo(Item i) {
         return i.getPublishedDate().compareTo(publishedDate);
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 }
