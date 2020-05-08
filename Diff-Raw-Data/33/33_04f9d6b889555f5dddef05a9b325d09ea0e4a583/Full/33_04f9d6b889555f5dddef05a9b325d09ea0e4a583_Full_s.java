 package gov.usgs.cida.coastalhazards.model.summary;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @Entity
 @Table(name="full_summary")
 public class Full implements Serializable {
 
 
     private transient long id;
     private String title;
     private String text;
     private List<Publication> publications;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     @Column(name="text", length = 64000)
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     @Column(name="title", length = 1024)
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     @ElementCollection
     @Embedded
     public List<Publication> getPublications() {
         return publications;
     }
 
     public void setPublications(List<Publication> publications) {
         this.publications = publications;
     }
     
 }
