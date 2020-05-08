 package com.mobiarch.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
 
 import com.mobiarch.entity.Media;
 
 @Stateless
 public class MediaManager {
 	@EJB
 	ParamManager pm;
 	
 	@PersistenceContext(name="QVPU")
 	EntityManager em;
 	Logger logger = Logger.getLogger(getClass().getName());
 
 	private String getDerivedFileName(Media m) {
 		return "Media-" + m.getGalleryId() + "-" + m.getId() + "-" + m.getFileName();
 	}
 
 	public void addMedia(Media m, InputStream is) {
 		em.persist(m);
 		em.flush();
 		
 		m.setFileName(getDerivedFileName(m));
 		File file = getOSFile(m);
 		logger.info("Writing to: " + file.getAbsoluteFile());
 		logger.info("Gallery: " + m.getGalleryId());
 		
 		FileOutputStream os = null;
 		try {
 			os = new FileOutputStream(file);
 			byte buff[] = new byte[256];
 			int len;
 			
 			while ((len = is.read(buff)) > 0) {
 				os.write(buff, 0, len);
 			}
 			os.close();
 		} catch (Exception e) {
 			if (os != null) {
 				try {
 					os.close();
 					file.delete();
 				} catch (IOException e1) {
 				}
 			}
 			throw new RuntimeException(e);
 		}
 	}
 
 	private File getOSFile(Media m) {
 		File file = new File(pm.getStringValue("media_dir"), m.getFileName());
 		return file;
 	}
 	
 	public List<Media> getAllForGallery(int galleryId) {
		TypedQuery<Media> q = em.createQuery("select m from Media m where galleryId=:galleryId", Media.class);
		
		q.setParameter("galleryId", galleryId);
		
		List<Media> list = q.getResultList();
 		String mediaURL = pm.getStringValue("media_url_prefix");
 		
 		for (Media m : list) {
 			m.setUrl(mediaURL + "/" + m.getFileName());
 		}
 		
 		return list;
 	}
 
 	public Media getMedia(int id) {
 		return em.find(Media.class, id);
 	}
 	
 	public void deleteMedia(int mediaId) {
 		Media m = getMedia(mediaId);
 		File f = getOSFile(m);
 		
 		em.remove(em.find(Media.class, mediaId));
 		f.delete();
 	}
 }
