 package se.bhg.photos.service.impl;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import com.drew.imaging.ImageProcessingException;
 
 import se.bhg.photos.exception.PhotoAlreadyExistsException;
 import se.bhg.photos.model.Photo;
 import se.bhg.photos.service.BhgV4ImporterService;
 import se.bhg.photos.service.FileService;
 import se.bhg.photos.service.PhotoService;
 
 @Service
 public class BhgV4ImporterServiceImpl implements BhgV4ImporterService{
     private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);
     private final static String BASE_PATH_V4 = "/tmp/oldbhg/";
 
     @Autowired
     JdbcTemplate jdbcTemplateImages;
     
     @Autowired
     FileService fileService;
 
     @Autowired
     PhotoService photoService;
     
     @Override
     public void importImagesAndGalleries() throws PhotoAlreadyExistsException {
         //importGalleries();
         importImages();
     }
 
     private void importGalleries() {
         List<Map<String, Object>> result = jdbcTemplateImages.queryForList("select * from gallery");
         for (Map<String, Object> row: result) {
             LOG.info("Found gallery!");
             for (Map.Entry<String, Object> entry : row.entrySet()) {
                 String key = entry.getKey();
                 Object value = entry.getValue();
                 LOG.info(key + " = " + value);
             }
         }
     }
 
     private void importImages() throws PhotoAlreadyExistsException {
         long  start = System.currentTimeMillis();
         List<Map<String, Object>> result = jdbcTemplateImages.queryForList("select * from image");
         int n = 0;
         for (Map<String, Object> row: result) {
             LOG.info("Found images!");
             String file = (String) row.get("filename");
             int lastSlash = file.lastIndexOf("/");
             String fileName = null;
             if (lastSlash < file.length() && lastSlash > 0) {
                 fileName = file.substring(lastSlash + 1);
             } else {
             	LOG.error("Could not find slash in {}", file);
                 continue;
             }
             
             String filePath = BASE_PATH_V4 + file;
             byte[] fileData;
             try {
                 fileData = fileService.readFile(BASE_PATH_V4 + file);
             } catch (IOException e) {
                 LOG.error("Could not read file {}: {}", filePath, e.getLocalizedMessage());
                 e.printStackTrace();
                 continue;
             }
             String uuid = UUID.randomUUID().toString();
             String username = (String) row.get("owner");
             String gallery = Integer.toString((int) row.get("gallery"));
             
             try {
                 Photo photo = photoService.addPhoto(fileName, fileData, username, uuid, gallery);
                photo.setViews(Integer.parseInt((String) row.get("views")));
                photo.setOldId(Integer.parseInt((String) row.get("id")));
                 photo.setImported(true);
                 photoService.save(photo);
             } catch (ImageProcessingException | IOException | PhotoAlreadyExistsException e) {
                 LOG.error("Could not add file {}: {}", filePath, e.getLocalizedMessage());
                 e.printStackTrace();
             }
             n++;
             /*
             for (Map.Entry<String, Object> entry : row.entrySet()) {
                 String key = entry.getKey();
                 Object value = entry.getValue();
                 LOG.info(key + " = " + value);
             }
             */
         }
         long end = System.currentTimeMillis();
         LOG.info("Imported {} images in {}ms", n, end-start);
     }
 }
