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
 package org.debox.photo.action;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URLDecoder;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.shiro.SecurityUtils;
 import org.debox.photo.dao.AlbumDao;
 import org.debox.photo.dao.PhotoDao;
 import org.debox.photo.job.ImageProcessor;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.Photo;
 import org.debox.photo.model.ThumbnailSize;
 import org.debox.photo.server.renderer.ZipDownloadRenderer;
 import org.debux.webmotion.server.render.Render;
 import org.im4java.core.IM4JavaException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class AlbumController extends DeboxController {
 
     private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);
     
     protected static AlbumDao albumDao = new AlbumDao();
     protected static PhotoDao photoDao = new PhotoDao();
 
     public Render getAlbums(String token) throws SQLException {
         boolean isAuthenticated = SecurityUtils.getSubject().isAuthenticated();
         List<Album> albums = albumDao.getVisibleAlbums(token, null, isAuthenticated);
         return renderJSON("albums", albums);
     }
 
     public Render getAlbum(String token, String albumName) throws IOException, SQLException {
         boolean isAuthenticated = SecurityUtils.getSubject().isAuthenticated();
         
         Album album;
         albumName = URLDecoder.decode(albumName, "UTF-8");
         if (isAuthenticated) {
             album = albumDao.getAlbumByName(albumName);
         } else {
             album = albumDao.getAlbumByName(token, albumName);
         }
         
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
         }
 
         List<Photo> photos;
         List<Album> albums;
         Album parent;
         
         if (isAuthenticated) {
             photos = photoDao.getPhotos(album.getId());
             parent = albumDao.getAlbum(album.getParentId());
         } else {
             photos = photoDao.getVisiblePhotos(token, album.getId());
             parent = albumDao.getVisibleAlbum(token, album.getParentId());
         }
         albums = albumDao.getVisibleAlbums(token, album.getId(), isAuthenticated);
 
         return renderJSON("album", album, "photos", photos, "albums", albums, "parent", parent);
     }
 
     public Render getAlbumById(String albumId) throws SQLException {
         if (!SecurityUtils.getSubject().isAuthenticated()) {
             return renderStatus(HttpURLConnection.HTTP_FORBIDDEN);
         }
         
         Album album = albumDao.getAlbum(albumId);
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
         }
         return renderJSON(album);
     }
 
     /* Use boolean type with 2.1 WebMotion version */
     public Render editAlbum(String id, String name, String visibility, Boolean downloadable) throws SQLException {
         if (!SecurityUtils.getSubject().isAuthenticated()) {
             return renderStatus(HttpURLConnection.HTTP_FORBIDDEN);
         }
         
         Album album = albumDao.getAlbum(id);
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
         }
 
         album.setName(name);
         album.setVisibility(Album.Visibility.valueOf(visibility.toUpperCase()));
         
         if (downloadable == null) {
             downloadable = false;
         }
         
         album.setDownloadable(downloadable);
 
         albumDao.save(album);
 
         return renderJSON(album);
     }
 
     public Render getAlbumCover(String token, String albumId) throws SQLException, IOException {
         Photo photo;
         if (SecurityUtils.getSubject().isAuthenticated()) {
             photo = photoDao.getAlbumCover(albumId);
         } else {
             photo = photoDao.getVisibleAlbumCover(token, albumId);
         }
 
         if (photo == null) {
             String missingImagePath = getContext().getServletContext().getRealPath("img/folder.png");
             return renderStream(new FileInputStream(missingImagePath), "image/png");
         }
         
         String filename = photo.getTargetPath() + File.separatorChar + ThumbnailSize.SQUARE.getPrefix() + photo.getId() + ".jpg";
         FileInputStream fis = null;
         try {
             fis = new FileInputStream(filename);
         } catch (IOException e) {
             logger.warn(filename + " image doesn't exist, generation in progress.");
             ImageProcessor processor = new ImageProcessor(photo.getSourcePath(), photo.getTargetPath(), photo.getId());
             try {
                 fis = processor.generateThumbnail(ThumbnailSize.SQUARE);
             } catch (IOException | IM4JavaException | InterruptedException ex) {
                 logger.error("Unable to generate thumbnail", ex);
             }
         }
         return renderStream(fis, "image/jpeg");
     }
 
    public Render download(String token, String albumId, boolean resized) throws SQLException {
        Album album = albumDao.getVisibleAlbum(token, albumId);
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
 
         } else if (!album.isDownloadable() && !SecurityUtils.getSubject().isAuthenticated()) {
             return renderStatus(HttpURLConnection.HTTP_FORBIDDEN);
         }
 
         if (resized) {
             List<Photo> photos = photoDao.getPhotos(album.getId());
             Map<String, String> names = new HashMap<>(photos.size());
             for (Photo photo : photos) {
                 names.put(ThumbnailSize.LARGE.getPrefix() + photo.getId() + ".jpg", ThumbnailSize.LARGE.getPrefix() + photo.getName());
             }
             
             return new ZipDownloadRenderer(album.getTargetPath(), album.getName(), names);
         }
         return new ZipDownloadRenderer(album.getSourcePath(), album.getName());
     }
     
 }
