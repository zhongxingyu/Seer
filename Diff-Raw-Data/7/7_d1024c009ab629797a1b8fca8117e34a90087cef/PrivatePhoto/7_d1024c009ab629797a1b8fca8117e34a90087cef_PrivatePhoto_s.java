 package ai.ilikeplaces.entities;
 
import ai.ilikeplaces.entities.etc.Refresh;
import ai.ilikeplaces.entities.etc.RefreshException;
import ai.ilikeplaces.entities.etc.RefreshSpec;
import ai.ilikeplaces.entities.etc.Refreshable;
 import ai.scribble.License;
 import ai.scribble.WARNING;
 import ai.scribble._bidirectional;
 import ai.scribble._unidirectional;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Ravindranath Akila
  */
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Entity
 public class PrivatePhoto implements Serializable, Comparable<PrivatePhoto>, Refreshable<PrivatePhoto> {
 
     private static final long serialVersionUID = 1L;
     public Long privatePhotoId;
 
     public String privatePhotoFilePath;
 
     public String privatePhotoURLPath;
 
     public String privatePhotoName;
 
     public String privatePhotoDescription;
 
     public Date privatePhotoUploadDate;
 
     public Date privatePhotoTakenDate;
 
     public HumansPrivatePhoto humansPrivatePhoto;
 
     public List<Album> albums;
     final static public String albumsCol = "albums";
 
     public Wall privatePhotoWall;
 
     private static final Refresh<PrivatePhoto> REFRESH = new Refresh<PrivatePhoto>();
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getPrivatePhotoId() {
         return privatePhotoId;
     }
 
     public void setPrivatePhotoId(Long privatePhotoId) {
         this.privatePhotoId = privatePhotoId;
     }
 
     @Transient
     public PrivatePhoto setPrivatePhotoIdR(Long privatePhotoId) {
         setPrivatePhotoId(privatePhotoId);
         return this;
     }
 
     public String getPrivatePhotoName() {
         return privatePhotoName;
     }
 
     public void setPrivatePhotoName(String privatePhotoName) {
         this.privatePhotoName = privatePhotoName;
     }
 
     @Transient
     public PrivatePhoto setPrivatePhotoNameR(String privatePhotoName) {
         setPrivatePhotoName(privatePhotoName);
         return this;
     }
 
     public String getPrivatePhotoDescription() {
         return privatePhotoDescription;
     }
 
     public void setPrivatePhotoDescription(final String privatePhotoDescription) {
         this.privatePhotoDescription = privatePhotoDescription;
     }
 
     @Transient
     public PrivatePhoto setPrivatePhotoDescriptionR(final String privatePhotoDescription) {
         setPrivatePhotoDescription(privatePhotoDescription);
         return this;
     }
 
     @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
     public HumansPrivatePhoto getHumansPrivatePhoto() {
         return humansPrivatePhoto;
     }
 
     public void setHumansPrivatePhoto(HumansPrivatePhoto humansPrivatePhoto) {
         this.humansPrivatePhoto = humansPrivatePhoto;
     }
 
     @Transient
     public PrivatePhoto setHumansPrivatePhotoR(HumansPrivatePhoto humansPrivatePhoto) {
         setHumansPrivatePhoto(humansPrivatePhoto);
         return this;
     }
 
     public String getPrivatePhotoFilePath() {
         return privatePhotoFilePath;
     }
 
     public void setPrivatePhotoFilePath(String privatePhotoFilePath) {
         this.privatePhotoFilePath = privatePhotoFilePath;
     }
 
     @Transient
     public PrivatePhoto setPrivatePhotoFilePathR(String privatePhotoFilePath) {
         setPrivatePhotoFilePath(privatePhotoFilePath);
         return this;
     }
 
 
     @Temporal(javax.persistence.TemporalType.DATE)
     public Date getPrivatePhotoTakenDate() {
         return privatePhotoTakenDate;
     }
 
     public void setPrivatePhotoTakenDate(Date privatePhotoTakenDate) {
         this.privatePhotoTakenDate = privatePhotoTakenDate;
     }
 
     @Transient
     public PrivatePhoto setPrivatePhotoTakenDateR(Date privatePhotoTakenDate) {
         setPrivatePhotoTakenDate(privatePhotoTakenDate);
         return this;
     }
 
     public String getPrivatePhotoURLPath() {
         return privatePhotoURLPath;
     }
 
     public void setPrivatePhotoURLPath(String privatePhotoURLPath) {
         this.privatePhotoURLPath = privatePhotoURLPath;
     }
 
     @Transient
     public PrivatePhoto setPrivatePhotoURLPathR(String privatePhotoURLPath) {
         setPrivatePhotoURLPath(privatePhotoURLPath);
         return this;
     }
 
     @Temporal(javax.persistence.TemporalType.DATE)
     public Date getPrivatePhotoUploadDate() {
         return privatePhotoUploadDate;
     }
 
     public void setPrivatePhotoUploadDate(Date privatePhotoUploadDate) {
         this.privatePhotoUploadDate = privatePhotoUploadDate;
     }
 
     @Transient
     public PrivatePhoto setPrivatePhotoUploadDateR(Date privatePhotoUploadDate) {
         setPrivatePhotoUploadDate(privatePhotoUploadDate);
         return this;
     }
 
     @_bidirectional(ownerside = _bidirectional.OWNING.IS)
     @WARNING(warning = "Owning as deleting a photo should automatically reflect in albums, not vice versa.")
     @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
     public List<Album> getAlbums() {
         return albums;
     }
 
     public void setAlbums(List<Album> albums) {
         this.albums = albums;
     }
 
     @_unidirectional
     @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     public Wall getPrivatePhotoWall() {
         return privatePhotoWall;
     }
 
     public void setPrivatePhotoWall(Wall privatePhotoWall) {
         this.privatePhotoWall = privatePhotoWall;
     }
 
     public PrivatePhoto setPrivatePhotoWallR(Wall privatePhotoWall) {
         this.privatePhotoWall = privatePhotoWall;
         return this;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 37 * hash + (this.privatePhotoId != null ? this.privatePhotoId.hashCode() : 0);
         return hash;
     }
 
 
     /**
      * @param showChangeLog__
      * @return changeLog
      */
     @Override
     public String toString() {
         return "PrivatePhoto{" +
                 "privatePhotoId=" + privatePhotoId +
                 ", privatePhotoFilePath='" + privatePhotoFilePath + '\'' +
                 ", privatePhotoURLPath='" + privatePhotoURLPath + '\'' +
                 ", privatePhotoName='" + privatePhotoName + '\'' +
                 ", privatePhotoDescription='" + privatePhotoDescription + '\'' +
                 ", privatePhotoUploadDate=" + privatePhotoUploadDate +
                 ", privatePhotoTakenDate=" + privatePhotoTakenDate +
                 ", humansPrivatePhoto=" + humansPrivatePhoto +
                 ", albums=" + albums +
                 ", privatePhotoWall=" + privatePhotoWall +
                 '}';
     }
 
 
     /**
      * @param toBeComparedWith Object To Be ComparedWith
      * @return curent - toBeComparedWith
      */
     @Override
     public int compareTo(final PrivatePhoto toBeComparedWith) {
         return (int) (this.getPrivatePhotoId() - toBeComparedWith.getPrivatePhotoId());
     }
 
     @Override
     public PrivatePhoto refresh(final RefreshSpec refreshSpec) throws RefreshException {
         REFRESH.refresh(this, refreshSpec);
         return this;
     }
 }
