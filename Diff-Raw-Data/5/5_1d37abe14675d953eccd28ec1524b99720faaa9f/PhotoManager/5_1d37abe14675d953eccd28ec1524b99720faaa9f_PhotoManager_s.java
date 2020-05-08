 package com.easytag.core.managers;
 
 import com.easytag.core.entity.jpa.Album;
 import com.easytag.core.entity.jpa.EasyTagFile;
 import com.easytag.core.entity.jpa.Photo;
 import com.easytag.core.enums.PhotoStatus;
 import com.easytag.exceptions.TagException;
 import com.easytag.utils.PreviewUtils;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.ejb.Asynchronous;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author rogvold
  */
 @Stateless
 public class PhotoManager implements PhotoManagerLocal {
 
     @PersistenceContext(unitName = "EasyTagCorePU")
     EntityManager em;
     
     @EJB
     AlbumManagerLocal aMan;
     
     @EJB
     FileManagerLocal  fMan;
 
     @Override
     public Photo getPhotoById(Long photoId) {
         if (photoId == null) {
             return null;
         }
         return em.find(Photo.class, photoId);
     }
 
     @Override
     public List<Photo> getPhotosInAlbum(Long albumId) {
         Query q = em.createQuery("select p from Photo p where p.albumId = :albumId").setParameter("albumId", albumId);
         List<Photo> list = q.getResultList();
         if (list == null || list.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
         return list;
     }
 
     @Override
     public Integer getPhotosAmount(Long albumId) throws TagException {
         Query q = em.createQuery("select count(p) from Photo p where p.albumId = :albumId").setParameter("albumId", albumId);
         return ((Long) q.getSingleResult()).intValue();
     }
 
     @Override
     public Photo addPhoto(Long userId, Long albumId, String name, String description, String tags, Long fileId, Long height, Long width) throws TagException {
         
         if (albumId == null) {
             throw new TagException("Album Id is not specified"); 
         }
         if (userId == null) {
             throw new TagException("User Id is not specified"); 
         }
         Album album = em.find(Album.class, albumId);
         
         if (!userId.equals(album.getCreatorId())){
             throw new TagException("User is not creator");
         }
         
         if (fileId == null) {
             throw new TagException("File Id is not specified");
         }
         //TODO check file existance
         
         Photo photo =  new Photo(name, description, tags,
                 PhotoStatus.NEW, getPhotosAmount(albumId) + 1, albumId, fileId, height, width);
         // for now set preview the same as original file
         photo.setPreviewId(fileId);
         
         return em.merge(photo);
     }
 
     @Override
     public List<Photo> addPhotos(Long userId, Long albumId, List<Long> fileIdList) throws TagException {
         
         if (albumId == null) {
             throw new TagException("Album Id is not specified"); 
         }
         if (userId == null) {
             throw new TagException("User Id is not specified"); 
         }
         Album album = em.find(Album.class, albumId);
         
         if (!userId.equals(album.getCreatorId())){
             throw new TagException("User is not creator");
         }
         
         for(Long id:fileIdList){
             if (id == null) {
                 throw new TagException("addPhotos: File Id is not specified");
             }
             //TODO check file existance
         }
         
         List<Photo> lp = new ArrayList<Photo>();
         Integer number = getPhotosAmount(albumId);
         for(Long id:fileIdList){
             number++;
             Photo photo =  new Photo(null, null, null, PhotoStatus.NEW,  number, albumId, id, null, null);
             em.merge(photo);
             lp.add(photo);
         }
         return lp;        
     }
 
     @Override
     public Album getAlbumByPhotoId(Long photoId) throws TagException {     
         Photo p = getPhotoById(photoId);
         if (p == null){
             throw new TagException("Photo is not specified");
         }
         return aMan.getAlbumById(p.getAlbumId());
     }   
 
     @Override
     @Asynchronous
     public void generatePreview(Photo p) throws TagException {
         
         EasyTagFile etf = fMan.findFileById(p.getFileId());
         
         String path = etf.getCurrentPath();        
         String prevPath = path.substring(0, path.lastIndexOf(".")) 
                 + PreviewUtils.POSTFIX + path.substring(path.lastIndexOf("."));
         
         String name = etf.getOriginalName();
         String prevName = name.substring(0, name.lastIndexOf(".")) 
                 + PreviewUtils.POSTFIX + name.substring(name.lastIndexOf("."));
         
         try {
             PreviewUtils.makePreview(path, prevPath);
         } catch (Exception e) {
             throw new TagException("can't make a preview");
         }
         
         EasyTagFile prevFile = fMan.addFile(etf.getUserId(), prevName, prevPath, etf.getContentType());
        p.setPreviewId(prevFile.getId());   }
 
     @Override
     public List<Photo> findPhotosByTagName(String query) {
                 
         String[] words = query.split("\\s+");
         
         Query q = em.createQuery("select distinct p from "
                     + "Photo p, EasyTag e where e.name like :query "
                     + "and p.id = e.photoId").setParameter("query", "%" + words[0] + "%");            
         List<Photo> list = q.getResultList();
         
         List<Photo> curr;
         for(int i = 1; i < words.length; i++){        
             q = em.createQuery("select distinct p from "
                     + "Photo p, EasyTag e where e.name like :query "
                     + "and p.id = e.photoId").setParameter("query", "%" + words[i] + "%");
             curr = q.getResultList();
             list.retainAll(curr);
         }
         
        
         if (list == null || list.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
         return list;
     }
 }
