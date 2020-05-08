 package org.jboss.pressgang.ccms.model;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import java.io.Serializable;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "LanguageFile")
 public class LanguageFile extends AuditedEntity implements Serializable {
     private static final long serialVersionUID = 6439345505628825355L;
 
     private Integer languageFileId = null;
     private String locale;
     private String originalFileName = null;
     private byte[] fileData;
     private File file = null;
 
     @Transient
     @Override
     public Integer getId() {
         return languageFileId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "LanguageFileID", unique = true, nullable = false)
     public Integer getLanguageFileId() {
         return languageFileId;
     }
 
     public void setLanguageFileId(Integer languageFileId) {
         this.languageFileId = languageFileId;
     }
 
     @Column(name = "FileData", columnDefinition = "longblob")
     public byte[] getFileData() {
         return fileData;
     }
 
     public void setFileData(byte[] fileData) {
         this.fileData = fileData;
     }
 
     @Column(name = "OriginalFileName", length = 255)
     @Size(max = 255)
     public String getOriginalFileName() {
         return originalFileName;
     }
 
     public void setOriginalFileName(final String originalFileName) {
         this.originalFileName = originalFileName;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "FileID", nullable = false)
     @NotNull
     public File getFile() {
         return file;
     }
 
     public void setFile(File file) {
         this.file = file;
     }
 
     @Column(name = "Locale", nullable = false, length = 20)
     @NotNull(message = "{languagefile.locale.notBlank}")
     @NotBlank(message = "{languagefile.locale.notBlank}")
     @Size(max = 20)
     public String getLocale() {
         return locale;
     }
 
     public void setLocale(String locale) {
         this.locale = locale;
     }
 }
