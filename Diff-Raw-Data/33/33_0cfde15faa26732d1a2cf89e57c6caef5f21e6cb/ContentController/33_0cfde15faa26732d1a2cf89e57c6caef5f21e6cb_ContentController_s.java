 package org.safaproject.safa.web.controller.content;
 
 import org.safaproject.safa.exception.ContentNotFoundException;
 import org.safaproject.safa.model.content.Content;
 import org.safaproject.safa.model.content.SearchRequest;
 import org.safaproject.safa.service.ContentService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/content")
 public class ContentController {
 
 	@Autowired
 	private ContentService contentService;
 
 	@RequestMapping(value = "/search", method = RequestMethod.POST, headers = "Accept=application/json")
 	public @ResponseBody
 	Content[] search(@RequestBody SearchRequest searchRequest) {
 
 		return contentService.search(searchRequest.getSelectedTags(),
 				searchRequest.getFirstResult(), searchRequest.getMaxResults(),
 				searchRequest.getOrderBy(), searchRequest.getOrderDirection())
 				.toArray(new Content[0]);
 
 	}
 
	@RequestMapping(value = "/get", method = RequestMethod.GET, headers = "Accept=application/json")
 	public @ResponseBody
	Content get(@RequestBody Long contentId) throws ContentNotFoundException {
 
 		return contentService.get(contentId);
 	}
 }
