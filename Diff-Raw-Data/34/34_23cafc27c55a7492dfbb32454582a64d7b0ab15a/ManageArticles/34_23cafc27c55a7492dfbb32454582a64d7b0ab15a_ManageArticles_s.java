 package org.amm.ams.web.controllers;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.amm.ams.service.interfaces.ArticleService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("article")
 public class ManageArticles {
 
 	@Autowired
 	private ArticleService articleService;
 	
 	@RequestMapping("list")
 	public String listArticles(Map<String, Object> params, HttpSession sess){
 		
 		String langPrefix = LocaleContextHolder.getLocale().getLanguage();
 		params.put("articles", articleService.findAll());
 		
 		return "articles/list";
 	}
 	
 	@RequestMapping(value = "create", method = RequestMethod.GET)
 	public String createAddArticleForm(Map<String, Object> params, HttpSession sess){
 		
		params.put("articles", articleService.findAll());
 		
		return "articles/list";
 	}
 	
 	@RequestMapping(value = "create", method = RequestMethod.POST)
 	public String submitAddArticleForm(Map<String, Object> params, HttpSession sess){
 		
 		params.put("articles", articleService.findAll());
 		
 		return "articles/list";
 	}
 	
	@RequestMapping(value = "edit", method = RequestMethod.GET)
	public String createEditArticleForm(Map<String, Object> params, HttpSession sess){
 		
 		params.put("articles", articleService.findAll());
 		
 		return "articles/list";
 	}
 	
 	@RequestMapping(value = "edit", method = RequestMethod.POST)
 	public String submitEditArticleForm(Map<String, Object> params, HttpSession sess){
 		
 		params.put("articles", articleService.findAll());
 		
 		return "articles/list";
 	}
 }
