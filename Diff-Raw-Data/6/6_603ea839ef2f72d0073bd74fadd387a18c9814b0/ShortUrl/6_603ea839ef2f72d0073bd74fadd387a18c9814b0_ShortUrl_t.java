 package com.github.urlchopper.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Version;
 
 import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang3.builder.ToStringStyle;
 
 /** Domain class, represents a short url.
  * @author Marton_Sereg
  *
  */
 @Entity
 public class ShortUrl {
 
    private static final int ORIGINAL_URL_MAX_LENGTH = 1000;

     private String shortUrl;
 
    @Column(length = ORIGINAL_URL_MAX_LENGTH)
     private String originalUrl;
 
     private Long activeUntil;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Column(name = "id")
     private Long id;
 
     @Version
     @Column(name = "version")
     private Integer version;
 
     public String getShortUrl() {
         return this.shortUrl;
     }
 
     public void setShortUrl(String shortUrl) {
         this.shortUrl = shortUrl;
     }
 
     public String getOriginalUrl() {
         return this.originalUrl;
     }
 
     public void setOriginalUrl(String originalUrl) {
         this.originalUrl = originalUrl;
     }
 
     public Long getActiveUntil() {
         return this.activeUntil;
     }
 
     public void setActiveUntil(Long activeUntil) {
         this.activeUntil = activeUntil;
     }
 
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Integer getVersion() {
         return this.version;
     }
 
     public void setVersion(Integer version) {
         this.version = version;
     }
 
     /**
      * ReflectionToString based toString method.
      * @return String representation of ShortUrl
      */
     public String toString() {
         return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
     }
 }
