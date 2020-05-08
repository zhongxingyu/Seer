 package edu.uib.info323.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import edu.uib.info323.dao.ImageDao;
 import edu.uib.info323.image.ImageProcessor;
 import edu.uib.info323.model.Image;
 
 @Controller
 public class ReindexSize {
 	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexSize.class);
 
 	@Autowired
 	ImageDao imageDao;
 	
 	@Autowired
 	ImageProcessor processor;
 	
 	@RequestMapping("/reindex")
 	public void reindex() {
 		while(true) {
			List<Image> images = imageDao.getNotReIndexed();
 			imageDao.updateAnalysedDate(images);
 			Map<String, Integer> exceptions = new HashMap<String, Integer>();
 			List<Image> failures = new ArrayList<Image>(100);
 			for(Image image : images) {
				LOGGER.debug("(Re)starting reindex loop");
 				try {
 				processor.setImage(image);
 				image.setHeight(processor.getImageHeight());
 				image.setWidth(processor.getImageWidth());
 				}
 				catch(Exception e) {
 					int count = exceptions.containsKey(e.getClass().getName()) ? exceptions.get(e.getClass().getName()) : 0;
 					exceptions.put(e.getClass().getName(), ++count);
 					failures.add(image);
 				}
 			}
 			if(failures.size() > 0) {
 				LOGGER.warn(failures.size()  + " images could not be processed and were deleted, because of exceptions: " + exceptions.toString());
 				imageDao.delete(failures);
 			}
 			images.removeAll(failures);
			imageDao.updateReIndexed(images);
 		}
 	}
 }
