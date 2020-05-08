 package pl.kurylek.junktion.controllers;
 
 import static org.slf4j.LoggerFactory.getLogger;
 import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
 import static org.springframework.http.HttpStatus.NOT_FOUND;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 import java.util.List;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import pl.kurylek.junktion.loaders.exceptions.DocumentNotFoundException;
 import pl.kurylek.junktion.repositories.exceptions.SolrRepositoryException;
 import pl.kurylek.junktion.services.DocumentSearchService;
 import pl.kurylek.junktion.snapshots.DocumentSnaphot;
 
 @Controller
 public class SearchController {
 
     final Logger logger = getLogger(getClass());
     @Autowired
     private DocumentSearchService documentSearchService;
 
     @RequestMapping(value = { "/" }, method = GET)
     public ModelAndView handleSearchPage() {
 	ModelAndView model = new ModelAndView("search");
 	return model;
     }
 
     @RequestMapping(value = { "/search/{query}" }, method = GET, produces = "application/json")
     @ResponseBody
     public List<DocumentSnaphot> getDocumentsByContentOrFilenameOrAuthor(@PathVariable String query) {
 	logger.info("Requested documents matching \"" + query + "\"");
 	return documentSearchService.findByContentOrAuthorOrTitleOrPath(query);
     }
 
     @RequestMapping(value = { "/search/{query}/{skip}" }, method = GET, produces = "application/json")
     @ResponseBody
     public List<DocumentSnaphot> getDocumentsByContentOrFilenameOrAuthor(@PathVariable String query,
 	    @PathVariable int skip) {
 	logger.info("Requested documents matching \"" + query + "\", skipping first " + skip);
 	return documentSearchService.findByContentOrAuthorOrTitleOrPath(query, skip);
     }
 
     @ExceptionHandler(DocumentNotFoundException.class)
     @ResponseStatus(value = NOT_FOUND)
     @ResponseBody
     public String handleDocumentNotFoundException(DocumentNotFoundException dnfe, Locale locale) {
 	logger.error("Document not found!", dnfe);
	return dnfe.getMessage();
     }
 
     @ExceptionHandler(SolrRepositoryException.class)
     @ResponseStatus(value = INTERNAL_SERVER_ERROR)
     @ResponseBody
     public String handleDocumentRepositoryException(SolrRepositoryException sre, Locale locale) {
 	logger.error("Solr server failed!", sre);
	return sre.getMessage();
     }
 }
