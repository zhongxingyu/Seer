 package ru.korpse.screenshots.core.controllers;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import ru.korpse.screenshots.core.dao.ShotDao;
 import ru.korpse.screenshots.entities.Shot;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 
 @Controller
 @RequestMapping(value = "/")
 public class FileController {
 	
 	@Autowired
 	private ShotDao dao;
 
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 
 	@RequestMapping(value = "/{key}", method = RequestMethod.GET)
 	public void get(@PathVariable String key, HttpServletResponse res)
 		    throws IOException, EntityNotFoundException {
 		Shot item = dao.get(Shot.keyToId(key));
 		
         BlobKey blobKey = new BlobKey(item.getBlobKey());
         blobstoreService.serve(blobKey, res);
 	}
 
 }
