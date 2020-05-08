 package org.linkstorage.controller;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import org.linkstorage.bean.LinkBean;
 import org.linkstorage.bean.LinkList;
 import org.linkstorage.model.Link;
 import org.linkstorage.repository.LinkBase;
 
 @Controller
 @RequestMapping("/integration/*")
 public class LinkController {
 
 	/**
 	 *
 	 */
 	private LinkBase linksRepository;
 
 	/**
 	 *
 	 */
 	private static final String XML_VIEW_NAME = "links";
 
 	/**
 	 *
 	 * @param linksRepository
 	 */
 	@Autowired
 	public LinkController(LinkBase linksRepository) {
 		this.linksRepository = linksRepository;
 	}
 
 	/**
 	 *
 	 * @param id
 	 * @return
 	 */
 	@RequestMapping(method=RequestMethod.GET, value="link/{id}")
 	public ModelAndView getLink(@PathVariable String id) {
 		Link link = linksRepository.getLink(Integer.parseInt(id));
 		LinkBean linkbean = new LinkBean(link);
		
		ModelAndView modelAndView = new ModelAndView(XML_VIEW_NAME);
	    modelAndView.addObject("link", linkbean);
	    
	    return modelAndView;
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	@RequestMapping(method=RequestMethod.GET, value="links")
 	public ModelAndView getLinks() {
 		List<Link> links = linksRepository.getLinks();
 		LinkList linksList = new LinkList(links);
		
 		return new ModelAndView(XML_VIEW_NAME, "links", linksList);
 	}
 }
