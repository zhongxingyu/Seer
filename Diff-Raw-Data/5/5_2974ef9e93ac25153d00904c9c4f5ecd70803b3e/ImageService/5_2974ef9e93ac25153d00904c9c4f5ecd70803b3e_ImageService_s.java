 package com.cqlybest.common.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.cqlybest.common.bean.Image;
 import com.cqlybest.common.dao.MongoDao;
 
 @Service
 public class ImageService {
 
   @Autowired
   private MongoDao mongoDao;
 
   public void addImage(Image image) {
     mongoDao.createObject("Image", image);
   }
 
   public void updateImage(Image image) {
    mongoDao.updateObject("Image", image.getId(), image);;
   }
 
   public Image getImage(String imageId) {
    return mongoDao.createQuery("Image").eq("_id", imageId).findObject(Image.class);
   }
 
 }
