 package com.discount.web;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.discount.domain.Product;
 import com.discount.domain.SearchResults;
 import com.discount.service.SearchService;
 
 @Controller
 public class SearchController extends BaseController {
 
 	private static final int MIN_KEYWORD_LENGTH = 2;
 
 	@Autowired
 	private SearchService searchService;
 
 	@RequestMapping(value = UrlConstants.SEARCH, method = RequestMethod.GET)
 	public String search(
 			@ModelAttribute("searchResults") SearchResults searchResults,
 			Map<String, Object> map) {
 		putRootCategories(map);
 		String searchKeyWord = searchResults.getKeyword();
 		try {
 			searchResults.setKeyword(new String(searchKeyWord
 					.getBytes("iso-8859-1"), "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (searchKeyWord.length() >= MIN_KEYWORD_LENGTH) {
 			Set<Product> searchResultsSet;
 			int serchCategoryId = searchResults.getSerchedCategoryId();
 			if (serchCategoryId > 0) {
 				searchResultsSet = searchService.searchByCategory(
 						searchKeyWord, serchCategoryId);
 			} else {
 				searchResultsSet = searchService.search(searchKeyWord);
 			}
 			searchResults.setMatchedProducts(searchResultsSet);
 		}
 		map.put("searchResults", searchResults);
 		return "search";
 	}
 }
