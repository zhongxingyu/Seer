 package jobengine.application;
 
 import jobengine.domain.Advert;
 import jobengine.domain.Adverts;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.List;
 
 @Controller
 @RequestMapping(value = "/adverts",
         consumes = {"*/*", "application/json","application/vnd.jobrapido.v1+json"},
         produces = {"application/vnd.jobrapido.v1+json"})
 class ApiAdverts {
 
     private Logger logger = LoggerFactory.getLogger(ApiAdverts.class);
     @Autowired
     private Adverts adverts;
 
     @RequestMapping(method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public List<Advert> index_v1(String what, String where) {
         logger.debug("api|v1|GET /adverts");
         return adverts.search(what, where);
     }
 
     @RequestMapping(method = RequestMethod.GET,
            consumes = "application/vnd.jobrapido.alpha+json",
             produces = "application/vnd.jobrapido.alpha+json")
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public List<Advert> index_alpha(String what, String where) {
         logger.debug("api|alpha|GET /adverts");
         return adverts.search(what, where);
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public Advert show(@PathVariable Long id) {
         logger.debug("api|GET /adverts/{}", id);
         return adverts.viewAdvert(id);
     }
 }
