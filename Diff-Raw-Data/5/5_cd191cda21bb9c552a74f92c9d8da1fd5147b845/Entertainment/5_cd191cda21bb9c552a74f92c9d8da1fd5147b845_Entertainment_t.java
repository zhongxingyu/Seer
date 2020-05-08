 package fr.cg95.cvq.business.request.ticket;
 
 import java.io.Serializable;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="ticket_entertainment")
 public class Entertainment implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     public static final String SEARCH_BY_NAME = "name";
     public static final String SEARCH_BY_CATEGORY = "category";
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
 
     @Column(name="external_id")
     private String externalId;
 
     private String information;
 
     private String name;
 
     private String link;
 
     private String category;
 
     private byte[] logo;
 
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="entertainment")
     private Set<Event> events = new LinkedHashSet<Event>();
 
     public Entertainment() {
     }
 
     public Entertainment(String externalId, String name, String link, String category) {
         this.externalId = externalId;
         this.name = name;
         this.link = link;
         this.category = category;
     }
 
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getExternalId() {
         return externalId;
     }
 
     public void setExternalId(String externalId) {
         this.externalId = externalId;
     }
 
     public String getInformation() {
         return this.information;
     }
 
     public void setInformation(String information) {
         this.information = information;
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getLink() {
         return this.link;
     }
 
     public void setLink(String link) {
         this.link = link;
     }
 
     public String getCategory() {
         return this.category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public byte[] getLogo() {
         return logo;
     }
 
     public void setLogo(byte[] logo) {
         this.logo = logo;
     }
 
     public Set<Event> getEvents() {
         return events;
     }
 
     public void setEvents(Set<Event> events) {
         this.events = events;
     }
 }
 
