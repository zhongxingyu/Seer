 package org.jboss.pressgang.ccms.model.contentspec;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "TranslatedCSNodeString")
 public class TranslatedCSNodeString extends AuditedEntity implements java.io.Serializable {
     private static final long serialVersionUID = -2111116884944950287L;
 
     private Integer translatedCSNodeStringId;
     private TranslatedCSNode translatedCSNode;
     private String locale;
     private String translatedString;
     private Boolean fuzzyTranslation = false;
 
     @Transient
     public Integer getId() {
         return translatedCSNodeStringId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "TranslatedCSNodeStringID", unique = true, nullable = false)
     public Integer getTranslatedCSNodeStringId() {
         return translatedCSNodeStringId;
     }
 
     public void setTranslatedCSNodeStringId(final Integer translatedCSNodeStringId) {
         this.translatedCSNodeStringId = translatedCSNodeStringId;
     }
 
     @Column(name = "Locale", nullable = false)
     @NotNull(message = "{contentspec.translatedstring.locale.notBlank}")
     @NotBlank(message = "{contentspec.translatedstring.locale.notBlank}")
     public String getLocale() {
         return locale;
     }
 
     public void setLocale(String locale) {
         this.locale = locale;
     }
 
    @ManyToOne
     @JoinColumn(name = "TranslatedCSNodeID", nullable = false)
     @NotNull
     public TranslatedCSNode getTranslatedCSNode() {
         return translatedCSNode;
     }
 
     public void setTranslatedCSNode(final TranslatedCSNode translatedCSNode) {
         this.translatedCSNode = translatedCSNode;
     }
 
     @Column(name = "TranslatedString", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getTranslatedString() {
         return translatedString;
     }
 
     public void setTranslatedString(final String translatedString) {
         this.translatedString = translatedString;
     }
 
     @Column(name = "FuzzyTranslation", nullable = false, columnDefinition = "BIT", length = 1)
     @NotNull
     public Boolean getFuzzyTranslation() {
         return fuzzyTranslation;
     }
 
     public void setFuzzyTranslation(final Boolean fuzzyTranslation) {
         this.fuzzyTranslation = fuzzyTranslation;
     }
 }
