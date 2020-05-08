 package com.wedlum.styleprofile.controller;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.util.Map;
 
import com.fasterxml.jackson.databind.JsonNode;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.wedlum.styleprofile.domain.photo.Photo;
 import com.wedlum.styleprofile.domain.photo.PhotoGallery;
 import com.wedlum.styleprofile.util.web.ParseUtils;
 
 @Controller
 @RequestMapping(value = "photo/detail")
 public class PhotoDetailController {
 
     @Autowired
     private PhotoGallery gallery;
 
     @RequestMapping(value = "{id:.+}", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public String get(@PathVariable String id) throws FileNotFoundException, IOException {
         return gallery.loadDetail(id);
     }
 
     @RequestMapping(value = "{id:.+}", method = RequestMethod.PUT, consumes = "application/json")
     @ResponseBody
     public void put(@RequestBody String body) throws FileNotFoundException, IOException {
        Map<String, Object> map = ParseUtils.fromJson(body);
        gallery.storeDetail(map.get("id").toString(), map.get("metadata").toString());
     }
 
     @RequestMapping(value = "{id:.+}", method = RequestMethod.DELETE)
     @ResponseBody
     public void delete(@PathVariable String id) throws FileNotFoundException, IOException {
         gallery.delete(id);
     }
 }
