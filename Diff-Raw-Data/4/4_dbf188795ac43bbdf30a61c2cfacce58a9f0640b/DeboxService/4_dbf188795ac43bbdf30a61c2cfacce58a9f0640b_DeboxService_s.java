 /*
  * #%L
  * debox-photos
  * %%
  * Copyright (C) 2012 Debox
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 package org.debox.photo.service;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.nio.file.attribute.FileTime;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.debox.imaging.ImageUtils;
 import org.debox.photo.dao.PhotoDao;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.Media;
 import org.debox.photo.model.Photo;
 import org.debox.photo.model.configuration.ThumbnailSize;
 import org.debox.photo.server.renderer.JacksonRenderJsonImpl;
 import org.debux.webmotion.server.WebMotionController;
 import org.debux.webmotion.server.render.Render;
 import org.debux.webmotion.server.render.RenderStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class DeboxService extends WebMotionController {
 
     private static final Logger logger = LoggerFactory.getLogger(DeboxService.class);
     protected static PhotoDao photoDao = new PhotoDao();
 
     @Override
     public Render renderSuccess() {
         return renderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
     
     public Render renderNotFound() {
         return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
     }
     
     @Override
     public Render renderJSON(Object... model) {
         return new JacksonRenderJsonImpl(toMap(model));
     }
 
     @Override
     protected Map<String, Object> toMap(Object... model) {
         if (model.length == 1) {
             Map<String, Object> map = new LinkedHashMap<>(1);
             map.put(null, model[0]);
             return map;
         }
 
         return super.toMap(model);
     }
 
     protected RenderStatus handleLastModifiedHeader(Media media, ThumbnailSize size) {
         try {
             long lastModified = photoDao.getGenerationTime(media.getId(), size);
             long ifModifiedSince = getContext().getRequest().getDateHeader("If-Modified-Since");
 
             if (lastModified == -1) {
                 String strPath = ImageUtils.getThumbnailPath(media, size);
                 Path path = Paths.get(strPath);
 
                 try {
                     BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                     FileTime lastModifiedTimeAttribute = attributes.lastModifiedTime();
 
                     lastModified = lastModifiedTimeAttribute.toMillis();
                     photoDao.saveThumbnailGenerationTime(media.getId(), size, lastModified);
 
                 } catch (IOException ioe) {
                     logger.error("Unable to access last modified property from file: " + strPath, ioe);
                 }
 
                 logger.warn("Get -1 value for photo " + media.getFilename() + " and size " + size.name());
             }
 
             if (lastModified != -1) {
                 getContext().getResponse().addDateHeader("Last-Modified", lastModified);
                 if (lastModified <= ifModifiedSince) {
                     return new RenderStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
                 }
             }
 
         } catch (SQLException ex) {
             logger.error("Unable to handle Last-Modified header, cause : " + ex.getMessage(), ex);
         }
 
         return new RenderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
 
     protected RenderStatus handleLastModifiedHeader(Album album) {
         try {
             String id = "a." + album.getId();
             long lastModified = photoDao.getGenerationTime(id, ThumbnailSize.SQUARE);
             long ifModifiedSince = getContext().getRequest().getDateHeader("If-Modified-Since");
 
             if (lastModified == -1) {
                 photoDao.saveThumbnailGenerationTime(id, ThumbnailSize.SQUARE, new Date().getTime());
                 logger.warn("Get -1 value for album " + album.getId() + " and size " + ThumbnailSize.SQUARE.name());
             }
 
             if (lastModified != -1) {
                 getContext().getResponse().addDateHeader("Last-Modified", lastModified);
                 if (lastModified <= ifModifiedSince) {
                     return new RenderStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
                 }
             }
 
         } catch (SQLException ex) {
             logger.error("Unable to handle Last-Modified header, cause : " + ex.getMessage(), ex);
         }
 
         return new RenderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
     
 }
