 package org.emsionline.emsiweb.web.controller;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.emsionline.emsiweb.domain.ChurchContent;
 import org.emsionline.emsiweb.domain.ChurchContentKey;
 import org.emsionline.emsiweb.domain.LocalizedChurch;
 import org.emsionline.emsiweb.domain.LocalizedChurchKey;
 import org.emsionline.emsiweb.domain.LocalizedChurchOrg;
 import org.emsionline.emsiweb.domain.LocalizedChurchOrgKey;
 import org.emsionline.emsiweb.service.ChurchContentService;
 import org.emsionline.emsiweb.service.LocalizedChurchOrgService;
 import org.emsionline.emsiweb.service.LocalizedChurchService;
 import org.emsionline.emsiweb.web.controller.domain.ChurchPage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.http.HttpRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 @RequestMapping("/cemi")
 @Controller
 public class ChurchController {
 
 	// TODO: Do a lookup instead of hardcoding to id 1
 	public static final int CEMI_CHURCH_ORG_ID = 1;
 	
 	final Logger logger = LoggerFactory.getLogger(ChurchController.class);
 
 	@Autowired
 	private LocalizedChurchService churchService;
 	
 
 	@Autowired
 	private ChurchContentService churchContentService;
 
 	
 	@Autowired
 	private LocalizedChurchOrgService churchOrgService;
 
 	@Autowired
 	private MessageSource messageSource;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String list(HttpServletRequest req, Model uiModel) {
 		
 		Locale locale = RequestContextUtils.getLocale(req);
 		
 		String lang = resolveLanguage(req);
 
 		String userAgent = req.getHeader("User-Agent");
 		
 		boolean css3TreeSupport = true;
 		if (userAgent.contains("MSIE 7.0") || userAgent.contains("MSIE 8.0")) {
 			css3TreeSupport = false;
 		}
 		uiModel.addAttribute("css3TreeSupport", css3TreeSupport);
 
 		LocalizedChurchOrg church_org = churchOrgService.findById(new LocalizedChurchOrgKey(new Long(CEMI_CHURCH_ORG_ID), lang));
 		uiModel.addAttribute("church_org", church_org);
 		
 		List<LocalizedChurch> all_churches = churchService.findAllByLocale(lang);
 		uiModel.addAttribute("allChurches", all_churches);
 		
 		uiModel.addAttribute("page_title", messageSource.getMessage("label_cemi_title", new Object[]{}, locale));
 		uiModel.addAttribute("meta_description", messageSource.getMessage("label_cemi_title", new Object[]{}, locale));
 		
 		return "cemi/list";
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public String showDefault(HttpServletRequest req, @PathVariable("id") String id, Model uiModel) throws NoSuchRequestHandlingMethodException {
 		return show(req, id, "intro", uiModel);
 	
 	}
 	
 	@RequestMapping(value = "/{id}/{page_id}",  method = RequestMethod.GET)
 	public String show(HttpServletRequest req, @PathVariable("id") String id, @PathVariable("page_id") String page_id, Model uiModel) throws NoSuchRequestHandlingMethodException {
 		
 		Locale locale = RequestContextUtils.getLocale(req);
 
 		String lang = resolveLanguage(req);
 		
 		LocalizedChurchOrg church_org = churchOrgService.findById(new LocalizedChurchOrgKey(new Long(CEMI_CHURCH_ORG_ID), lang));
 		uiModel.addAttribute("church_org", church_org);
 
 		
 		LocalizedChurch church = findChurch(id, lang);
 		if (church == null) {
 			throw new NoSuchRequestHandlingMethodException("show", ChurchController.class);
 		}
 		uiModel.addAttribute("church", church);
 		
 		Map<String, String> churchHierarchy = retrieveChurchHierarchy(church);
 		uiModel.addAttribute("churchHierarchy", churchHierarchy);
 		
 		ChurchContent content = churchContentService.findById(new ChurchContentKey(church.getId().getChurchId(), lang, page_id));
 		if (content == null) {
 			//throw new NoSuchRequestHandlingMethodException("show", ChurchController.class);
 			return "redirect:/cemi/" + id;
 		}
 		uiModel.addAttribute("content", content);
 		
 
 		
 		List<ChurchContent> contentList = churchContentService.findById_ChurchIdAndId_Locale(church.getId().getChurchId(), lang);
 		//uiModel.addAttribute("contentList", contentList);
 		Map<String, ChurchContent> contentMap = new HashMap<String, ChurchContent>();
 
 		for (ChurchContent c : contentList) {
 			contentMap.put(c.getId().getPageId(), c);
 			//logger.info("|" + c.getId().getPageId() + "| " + c.getTitle());
 		}
 		uiModel.addAttribute("contentMap", contentMap);
 
 		
 		uiModel.addAttribute("page_title", content.getTitle());
 		uiModel.addAttribute("meta_description", content.getTitle() + " Chinese Christian Evengelical Church Seminary International");
 		
 
 		String userAgent = req.getHeader("User-Agent");
 
 		boolean css3TreeSupport = true;
 		if (userAgent.contains("MSIE 7.0") || userAgent.contains("MSIE 8.0")) {
 			css3TreeSupport = false;
 		}
 		uiModel.addAttribute("css3TreeSupport", css3TreeSupport);
 		
 		return "cemi/show";
 	}
 
 	
 	@RequestMapping(value = "/{id}", params="edit", method = RequestMethod.GET)
 	public String editDefault(HttpServletRequest req, @RequestParam("edit") String edit, @PathVariable("id") String id, Model uiModel) throws NoSuchRequestHandlingMethodException {
 		return edit(req, edit, id, "intro", uiModel);
 	
 	}
 	
 	@RequestMapping(value = "/{id}/{page_id}", params="edit", method = RequestMethod.GET)
 	public String edit(HttpServletRequest req, @RequestParam("edit") String edit, @PathVariable("id") String id, @PathVariable("page_id") String page_id, Model uiModel) throws NoSuchRequestHandlingMethodException {
 		String userAgent = req.getHeader("User-Agent");
 
 		boolean css3TreeSupport = true;
 		if (userAgent.contains("MSIE 7.0") || userAgent.contains("MSIE 8.0")) {
 			css3TreeSupport = false;
 		}
 		uiModel.addAttribute("css3TreeSupport", css3TreeSupport);
 		
 		
 		String lang = resolveLanguage(req);
 		
 		LocalizedChurchOrg church_org = churchOrgService.findById(new LocalizedChurchOrgKey(new Long(CEMI_CHURCH_ORG_ID), lang));
 		uiModel.addAttribute("church_org", church_org);		
 
 		LocalizedChurch church = findChurch(id, lang);		
 		if (church == null) {
 			throw new NoSuchRequestHandlingMethodException("edit", ChurchController.class);
 		}		
 
 		uiModel.addAttribute("church", church);
 		
 		Map<String, String> churchHierarchy = retrieveChurchHierarchy(church);
 		logger.info(churchHierarchy.toString());
 		uiModel.addAttribute("churchHierarchy", churchHierarchy);
 		
 		ChurchContent content = churchContentService.findById(new ChurchContentKey(church.getId().getChurchId(), lang, page_id));
 		uiModel.addAttribute("content", content);
		logger.info(content.getBody());
 
 		
 		List<ChurchContent> contentList = churchContentService.findById_ChurchIdAndId_Locale(church.getId().getChurchId(), lang);
 		//uiModel.addAttribute("contentList", contentList);
 		Map<String, ChurchContent> contentMap = new HashMap<String, ChurchContent>();
 
 		
 		
 		for (ChurchContent c : contentList) {
 			contentMap.put(c.getId().getPageId(), c);
 			//logger.info("|" + c.getId().getPageId() + "| " + c.getTitle());
 		}
 		uiModel.addAttribute("contentMap", contentMap);
 
 		
 		
 		if (edit.equals("inline")) {
 			return "cemi/edit-inline";
 		}
 		else {
 			return "cemi/edit";
 		}
 		
 	}
 
 	@RequestMapping(value = "/{id}/save", headers = {"Accept=application/json"}, method = RequestMethod.POST)
 	public @ResponseBody ChurchPage save(HttpServletRequest req, @PathVariable("id") String id, @RequestBody ChurchPage page) throws NoSuchRequestHandlingMethodException {
 		return save(req, id,  "intro", page);
 	}
 
 	
 	@RequestMapping(value = "/{id}/{page_id}/save", headers = {"Accept=application/json"}, method = RequestMethod.POST)
 	public @ResponseBody ChurchPage save(HttpServletRequest req, @PathVariable("id") String id, @PathVariable("page_id") String page_id, @RequestBody ChurchPage page) throws NoSuchRequestHandlingMethodException {
 		
 		logger.info("remote ip=" + req.getRemoteAddr());
 		logger.info("context_path=" + req.getContextPath());
 		logger.info("id=" + id);
 		logger.info("page_id=" + page_id);
 		
 		// Reverse path conversion
 		String body = page.getBody();
 		
 		/*
 		${fn:replace(fn:replace(fn:replace(content.body, '/emsi/images/', imgBase), '/emsi/files/', imgBase2), '/emsiweb/images/', imgBase3)}
 		<spring:url value="/images/emsi/" var="imgBase" />
 		<spring:url value="/images/files/" var="imgBase2" />
 		<spring:url value="/images/" var="imgBase3"/>
 		*/
 		
 		logger.info("page.body.before=|" + body + "|");
 
		body = body.replaceAll(req.getContextPath() + "/images/emsi/", "/emsi/images/");
		body = body.replaceAll(req.getContextPath() + "/images/files/", "/emsi/files/");
		body = body.replaceAll(req.getContextPath() + "/images/", "/emsiweb/images/");
 		
 		
 		logger.info("page.body.after=|" + body + "|");
 		
 		String lang = resolveLanguage(req);
 
 		LocalizedChurch church = findChurch(id, lang);
 		if (church == null) {
 			throw new NoSuchRequestHandlingMethodException("save", ChurchController.class);
 		}		
 		
 		ChurchContent content = churchContentService.findById(new ChurchContentKey(church.getId().getChurchId(), lang, page_id));
 		content.setBody(body);
 		
 		
 		churchContentService.save(content);
 		
 		page.setBody("Save successful");
 		return page;
 	}
 	
 	private Map<String, String> retrieveChurchHierarchy(LocalizedChurch church) {
 		
 		Map<String, String> hier = new HashMap<String, String>();
 
 		if (church != null) {
 			hier.put(church.getChurchPath(), church.getChurchPath());
 		
 			for (LocalizedChurchOrg org = church.getParentOrg(); org != null; org = org.getParentOrg()) {
 				hier.put(org.getChurchOrgPath(), org.getChurchOrgPath());
 			}
 		}
 		
 		return hier;
 	}
 
 	
 	public String resolveLanguage(HttpServletRequest req) {
 		Locale locale = RequestContextUtils.getLocale(req);
 		// TODO: Need to make the language selection more generic
 		String lang = locale.getLanguage();
 		if (!(lang.equals("en") || lang.equals("zh"))) {
 			lang = "en";
 		}
 		
 		return lang;
 	}
 	
 	public LocalizedChurch findChurch(String id, String lang) {
 		LocalizedChurch church = null;
 		try {
 			church = churchService.findById(new LocalizedChurchKey(new Long(id), lang));
 		}
 		catch (NumberFormatException e) {
 			// Ignore
 		}
 		
 		if (church == null) {
 			church = churchService.findById_LocaleAndChurchPath(lang, id);
 		}
 		
 		return church;
 
 	}
 }
