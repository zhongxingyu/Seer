 /*
  * Copyright 2014 Studentmediene i Trondheim AS
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package no.dusken.momus.controller;
 
 import no.dusken.momus.model.Source;
 import no.dusken.momus.model.SourceTag;
 import no.dusken.momus.service.SourceService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.List;
 
 @Controller
 @RequestMapping("/source")
 public class SourceController {
 
     @Autowired
     private SourceService sourceService;
 
     Logger logger = LoggerFactory.getLogger(getClass());
 
     @RequestMapping(method = RequestMethod.GET)
     public @ResponseBody List<Source> getAllSources() {
         return sourceService.getSourceRepository().findAll();
     }
 
     @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
     public @ResponseBody Source saveSource(@RequestBody Source source) {
         return sourceService.save(source);
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public @ResponseBody Source getSourceById(@PathVariable("id") Long id) {
        return sourceService.getSourceRepository().findOne(id);
     }
 
 
 
     // =============== TAGS below =======================
 
     @RequestMapping(value = "/tag", method = RequestMethod.GET)
     public @ResponseBody List<SourceTag> getAllTags() {
         return sourceService.getTagRepository().findAll();
     }
 
     @RequestMapping(value = "/tag", method = RequestMethod.POST)
     public @ResponseBody SourceTag createTag(@RequestBody SourceTag newTag) {
         return sourceService.getTagRepository().save(newTag);
     }
 
     @RequestMapping(value = "/tag/{tagId}", method = RequestMethod.PUT)
     public @ResponseBody SourceTag updateTag(@RequestBody SourceTag newTag, @PathVariable("tagId") String tagId) {
         return sourceService.updateTag(new SourceTag(tagId), newTag);
     }
 
     /**
      * This method is of type PUT instead of DELETE, because a bug
      * in Spring makes the decoding of /tag/{tagId} fail when the tag
      * contains æøå
      */
     @RequestMapping(value = "/tag/delete", method = RequestMethod.PUT)
     public @ResponseBody void deleteTag(@RequestBody SourceTag tag) {
         sourceService.deleteTag(tag);
     }
 
 
     @RequestMapping(value = "/tag/unused", method = RequestMethod.GET)
     public @ResponseBody List<SourceTag> getUnusedTags() {
         return sourceService.getTagRepository().getUnusedTags();
     }
 
 
 }
