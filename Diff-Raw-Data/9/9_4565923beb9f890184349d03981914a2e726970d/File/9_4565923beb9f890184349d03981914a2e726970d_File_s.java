 package org.jboss.pressgang.ccms.model;
 
 import javax.persistence.Cacheable;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "File")
 public class File extends AuditedEntity implements Serializable {
     private static final long serialVersionUID = -5557699207265609562L;
 
     private Integer fileId = null;
     private String fileName = null;
     private String filePath = null;
     private String description = null;
     private Boolean explodeArchive = false;
     private Set<LanguageFile> languageFiles = new HashSet<LanguageFile>();
 
     @Override
     public Integer getId() {
         return fileId;
     }
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "FileID", unique = true, nullable = false)
     public Integer getFileId() {
         return fileId;
     }
 
     public void setFileId(Integer fileId) {
         this.fileId = fileId;
     }
 
     @Column(name = "FileName", nullable = false, length = 255)
     @Size(max = 255)
     @NotNull(message = "file.filename.notBlank")
     @NotBlank(message = "file.filename.notBlank")
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(String fileName) {
         this.fileName = fileName;
     }
 
     @Column(name = "FilePath", length = 512)
     @Size(max = 512)
     public String getFilePath() {
         return filePath;
     }
 
     public void setFilePath(String filePath) {
         if (filePath == null) {
             this.filePath = null;
         } else if (filePath.trim().isEmpty() || filePath.endsWith("/") || filePath.endsWith("\\")) {
             this.filePath = filePath.trim();
         } else {
             this.filePath = filePath.trim() + "/";
         }
     }
 
     @Column(name = "Description", length =  512)
     @Size(max = 512)
     public String getDescription() {
         return description;
     }
 
     public void setDescription(final String description) {
         this.description = description;
     }
 
     @Column(name = "ExplodeArchive", columnDefinition = "BIT", length = 1)
     public Boolean getExplodeArchive() {
         return explodeArchive;
     }
 
     public void setExplodeArchive(Boolean explodeArchive) {
         this.explodeArchive = explodeArchive;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<LanguageFile> getLanguageFiles() {
         return languageFiles;
     }
 
     public void setLanguageFiles(Set<LanguageFile> languageFiles) {
         this.languageFiles = languageFiles;
     }
 
     @Transient
     public List<LanguageFile> getLanguageFilesArray() {
         final List<LanguageFile> retValue = new ArrayList<LanguageFile>(languageFiles);
         Collections.sort(retValue, new Comparator<LanguageFile>() {
             @Override
             public int compare(final LanguageFile o1, final LanguageFile o2) {
                 if (o1.getLocale() == null && o2.getLocale() == null) return 0;
                 if (o1.getLocale() == null) return -1;
                 if (o2.getLocale() == null) return 1;
                 return o1.getLocale().compareTo(o2.getLocale());
             }
         });
 
         return retValue;
     }
 
     public void addLanguageFile(final LanguageFile languageFile) {
         languageFiles.add(languageFile);
         languageFile.setFile(this);
     }
 
     public void removeLanguageFile(final LanguageFile languageFile) {
         languageFiles.remove(languageFile);
         languageFile.setFile(null);
     }
 }
