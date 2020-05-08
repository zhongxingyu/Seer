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
 import java.util.List;
 
 import javax.activation.DataSource;
 import javax.annotation.Resource;
 
 import org.shredzone.cilla.core.datasource.ResourceDataSource;
 import org.shredzone.cilla.core.event.Event;
 import org.shredzone.cilla.core.event.EventService;
 import org.shredzone.cilla.core.event.EventType;
 import org.shredzone.cilla.core.model.Header;
 import org.shredzone.cilla.core.model.Store;
 import org.shredzone.cilla.core.repository.HeaderDao;
 import org.shredzone.cilla.core.repository.StoreDao;
 import org.shredzone.cilla.core.repository.UserDao;
 import org.shredzone.cilla.service.HeaderService;
 import org.shredzone.cilla.service.SecurityService;
 import org.shredzone.cilla.service.resource.ExifAnalyzer;
 import org.shredzone.cilla.service.resource.ImageTools;
 import org.shredzone.cilla.ws.ImageProcessing;
 import org.shredzone.cilla.ws.exception.CillaServiceException;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.FileCopyUtils;
 
 /**
  * Default implementation of {@link HeaderService}.
  *
  * @author Richard "Shred" Körber
  */
 @Service
 @Transactional
 public class HeaderServiceImpl implements HeaderService {
 
     private @Resource UserDao userDao;
     private @Resource HeaderDao headerDao;
     private @Resource StoreDao storeDao;
     private @Resource ImageTools imageProcessor;
     private @Resource EventService eventService;
     private @Resource SecurityService securityService;
 
     @Override
     public Header createNew() {
         Date now = new Date();
 
         Header header = new Header();
         header.setCreator(userDao.fetch(securityService.getAuthenticatedUser().getUserId()));
         header.setCreation(now);
         return header;
     }
 
     @Override
     public void create(Header header, DataSource headerImg, DataSource fullImg) throws CillaServiceException {
         if (headerImg == null || fullImg == null) {
             throw new IllegalArgumentException("no header and/or full-view provided");
         }
 
         try {
             Date now = new Date();
 
             Store headerStore = header.getHeaderImage();
             headerStore.setContentType(headerImg.getContentType());
             headerStore.setName(headerImg.getName());
             headerStore.setLastModified(now);
 
             Store fullStore = header.getFullImage();
             fullStore.setContentType(fullImg.getContentType());
             fullStore.setName(fullImg.getName());
             fullStore.setLastModified(now);
 
             Dimension dim = imageProcessor.analyzeDimension(headerImg);
             if (dim != null) {
                 header.setWidth(dim.width);
                 header.setHeight(dim.height);
             }
 
             ExifAnalyzer fullExif = imageProcessor.createExifAnalyzer(fullImg);
             header.setLocation(fullExif != null ? fullExif.getGeolocation() : null);
 
             headerDao.persist(header);
 
             ResourceDataSource headerDs = storeDao.access(header.getHeaderImage());
             FileCopyUtils.copy(headerImg.getInputStream(), headerDs.getOutputStream());
 
             ResourceDataSource fullDs = storeDao.access(header.getFullImage());
             FileCopyUtils.copy(fullImg.getInputStream(), fullDs.getOutputStream());
         } catch (IOException ex) {
             throw new CillaServiceException("Could not read medium", ex);
         }
 
         eventService.fireEvent(new Event(EventType.HEADER_NEW).value(header));
     }
 
     @Override
     public void update(Header header) throws CillaServiceException {
         eventService.fireEvent(new Event(EventType.HEADER_UPDATE).value(header));
     }
 
     @Override
    @CacheEvict(value = "processedImages", allEntries = true)
     public void updateImage(Header header, DataSource headerImg, DataSource fullImg) throws CillaServiceException {
         if (header.getId() == 0) {
             throw new IllegalArgumentException("header is not persisted");
         }
 
         try {
             Date now = new Date();
 
             if (headerImg != null) {
                 Store headerStore = header.getHeaderImage();
                 headerStore.setContentType(headerImg.getContentType());
                 headerStore.setName(headerImg.getName());
                 headerStore.setLastModified(now);
 
                 Dimension dim = imageProcessor.analyzeDimension(headerImg);
                 if (dim != null) {
                     header.setWidth(dim.width);
                     header.setHeight(dim.height);
                 } else {
                     header.setWidth(0);
                     header.setHeight(0);
                 }
 
                 ResourceDataSource headerDs = storeDao.access(header.getHeaderImage());
                 FileCopyUtils.copy(headerImg.getInputStream(), headerDs.getOutputStream());
             }
 
             if (fullImg != null) {
                 Store fullStore = header.getFullImage();
                 fullStore.setContentType(fullImg.getContentType());
                 fullStore.setName(fullImg.getName());
                 fullStore.setLastModified(now);
 
                 ExifAnalyzer fullExif = imageProcessor.createExifAnalyzer(fullImg);
                 header.setLocation(fullExif != null ? fullExif.getGeolocation() : null);
 
                 ResourceDataSource fullDs = storeDao.access(header.getFullImage());
                 FileCopyUtils.copy(fullImg.getInputStream(), fullDs.getOutputStream());
             }
 
             eventService.fireEvent(new Event(EventType.HEADER_UPDATE).value(header));
         } catch (IOException ex) {
             throw new CillaServiceException("Could not read medium", ex);
         }
     }
 
     @Override
    @CacheEvict(value = "processedImages", allEntries = true)
     public void remove(Header header) throws CillaServiceException {
         try {
             storeDao.access(header.getFullImage()).delete();
             storeDao.access(header.getHeaderImage()).delete();
             headerDao.delete(header);
             eventService.fireEvent(new Event(EventType.HEADER_DELETE).value(header));
         } catch (IOException ex) {
             throw new CillaServiceException("Could not delete header media", ex);
         }
     }
 
     @Override
     public boolean isVisible(Header header) {
         return header.isEnabled();
     }
 
     @Override
     public List<Header> getVisibleHeaders() {
         return headerDao.fetchEnabled();
     }
 
     @Override
     public ResourceDataSource getHeaderImage(Header header, ImageProcessing process) throws CillaServiceException {
         try {
             ResourceDataSource ds = storeDao.access(header.getHeaderImage());
             if (process != null) {
                 ds = imageProcessor.processImage(ds, process);
             }
             return ds;
         } catch (IOException ex) {
             throw new CillaServiceException(ex);
         }
     }
 
     @Override
     public ResourceDataSource getFullImage(Header header, ImageProcessing process) throws CillaServiceException {
         try {
             ResourceDataSource ds = storeDao.access(header.getFullImage());
             if (process != null) {
                 ds = imageProcessor.processImage(ds, process);
             }
             return ds;
         } catch (IOException ex) {
             throw new CillaServiceException(ex);
         }
     }
 
 }
