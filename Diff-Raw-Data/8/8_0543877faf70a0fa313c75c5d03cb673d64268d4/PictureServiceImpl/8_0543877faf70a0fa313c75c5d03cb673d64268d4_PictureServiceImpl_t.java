 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.service.impl;
 
 import java.awt.Dimension;
 import java.io.IOException;
 import java.util.Date;
import java.util.TimeZone;
 
 import javax.activation.DataSource;
 import javax.annotation.Resource;
 
 import org.shredzone.cilla.core.datasource.ResourceDataSource;
 import org.shredzone.cilla.core.event.Event;
 import org.shredzone.cilla.core.event.EventService;
 import org.shredzone.cilla.core.event.EventType;
 import org.shredzone.cilla.core.model.GallerySection;
 import org.shredzone.cilla.core.model.Picture;
 import org.shredzone.cilla.core.model.Store;
 import org.shredzone.cilla.core.repository.PictureDao;
 import org.shredzone.cilla.core.repository.StoreDao;
 import org.shredzone.cilla.service.PictureService;
 import org.shredzone.cilla.service.resource.ExifAnalyzer;
 import org.shredzone.cilla.service.resource.ImageTools;
 import org.shredzone.cilla.ws.ImageProcessing;
 import org.shredzone.cilla.ws.exception.CillaServiceException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.FileCopyUtils;
 
 /**
  * {@link PictureService} implementation.
  *
  * @author Richard "Shred" Körber
  */
 @Service
 @Transactional
 public class PictureServiceImpl implements PictureService {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private @Resource StoreDao storeDao;
     private @Resource EventService eventService;
     private @Resource PictureDao pictureDao;
     private @Resource ImageTools imageProcessor;
 
     @Override
     public Picture createNew() {
         Picture picture = new Picture();
         return picture;
     }
 
     @Override
     public void addPicture(GallerySection section, Picture picture, DataSource source) throws CillaServiceException {
         if (picture.getId() != 0) {
             throw new IllegalArgumentException("picture is already persisted, id " + picture.getId());
         }
 
         try {
             Store store = picture.getImage();
             store.setContentType(source.getContentType());
             store.setName(source.getName());
             store.setLastModified(new Date());
 
             Dimension dim = imageProcessor.analyzeDimension(source);
             if (dim != null) {
                 picture.setWidth(dim.width);
                 picture.setHeight(dim.height);
             }
 
             ExifAnalyzer exif = imageProcessor.createExifAnalyzer(source);
             if (exif != null) {
                TimeZone tz = picture.getCreateTimeZone();
                if (tz == null) {
                    tz = section.getDefaultTimeZone();
                }
                picture.setCreateDate(exif.getDateTime(tz));
                 picture.setExifData(exif.getExifData());
                 picture.setLocation(exif.getGeolocation());
             } else {
                 picture.setCreateDate(null);
                 picture.setExifData(null);
                 picture.setLocation(null);
             }
 
             picture.setGallery(section);
             section.getPictures().add(picture);
             renumberPictureSequence(section);
             pictureDao.persist(picture);
 
             ResourceDataSource ds = storeDao.access(store);
             FileCopyUtils.copy(source.getInputStream(), ds.getOutputStream());
         } catch (IOException ex) {
             throw new CillaServiceException("Could not read medium", ex);
         }
 
         eventService.fireEvent(new Event(EventType.GALLERY_PICTURE_NEW).value(picture));
     }
 
     @Override
     @CacheEvict(value = "processedImages", allEntries = true)
     public void updatePicture(Picture picture, DataSource source) throws CillaServiceException {
         if (picture.getId() == 0) {
             throw new IllegalArgumentException("picture is not persisted");
         }
 
         if (source != null) {
             try {
                 Store store = picture.getImage();
                 store.setContentType(source.getContentType());
                 store.setName(source.getName());
                 store.setLastModified(new Date());
 
                 Dimension dim = imageProcessor.analyzeDimension(source);
                 if (dim != null) {
                     picture.setWidth(dim.width);
                     picture.setHeight(dim.height);
                 }
 
                 ExifAnalyzer exif = imageProcessor.createExifAnalyzer(source);
                 if (exif != null) {
                     picture.setCreateDate(exif.getDateTime(picture.getCreateTimeZone()));
                     picture.setExifData(exif.getExifData());
                     picture.setLocation(exif.getGeolocation());
                 } else {
                     picture.setCreateDate(null);
                     picture.setExifData(null);
                     picture.setLocation(null);
                 }
 
                 ResourceDataSource ds = storeDao.access(store);
                 FileCopyUtils.copy(source.getInputStream(), ds.getOutputStream());
 
                 renumberPictureSequence(picture.getGallery());
                 eventService.fireEvent(new Event(EventType.GALLERY_PICTURE_UPDATE).value(picture));
             } catch (IOException ex) {
                 throw new CillaServiceException("Could not read medium", ex);
             }
         }
     }
 
     @Override
     @CacheEvict(value = "processedImages", allEntries = true)
     public void removePicture(Picture picture) throws CillaServiceException {
         if (picture.getId() == 0) {
             throw new IllegalArgumentException("picture is not persisted");
         }
 
         GallerySection section = picture.getGallery();
         boolean removed = section.getPictures().remove(picture);
         if (!removed) {
             throw new CillaServiceException("Picture id " + picture.getId() + " does not belong to section id " + section.getId());
         }
 
         try {
             storeDao.access(picture.getImage()).delete();
         } catch (IOException ex) {
             throw new CillaServiceException("Could not delete medium", ex);
         }
 
         try {
             pictureDao.delete(picture);
             eventService.fireEvent(new Event(EventType.GALLERY_PICTURE_DELETE).value(picture));
             renumberPictureSequence(section);
         } catch (RuntimeException ex) {
             log.warn("Rolling back transaction, but medium of picture id {} is already deleted.", picture.getId());
             throw ex;
         }
     }
 
     @Override
     public ResourceDataSource getImage(Picture picture, ImageProcessing process) throws CillaServiceException {
         try {
             ResourceDataSource ds = storeDao.access(picture.getImage());
             if (process != null) {
                 ds = imageProcessor.processImage(ds, process);
             }
             return ds;
         } catch (IOException ex) {
             throw new CillaServiceException(ex);
         }
     }
 
     /**
      * Renumber the picture sequence.
      *
      * @param section
      *            {@link GallerySection} to be renumbered
      */
     private void renumberPictureSequence(GallerySection section) {
         int cnt = 0;
         for (Picture picture : section.getPictures()) {
             picture.setSequence(cnt++);
         }
     }
 
 }
