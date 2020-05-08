 package com.abudko.reseller.huuto.mvc.list;
 
 import static com.abudko.reseller.huuto.mvc.ControllerHelper.SEARCH_FORM_PATH;
 import static com.abudko.reseller.huuto.mvc.ControllerHelper.SEARCH_PARAMS_ATTRIBUTE;
 import static com.abudko.reseller.huuto.mvc.ControllerHelper.SEARCH_RESULTS_ATTRIBUTE;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.abudko.reseller.huuto.mvc.ControllerHelper;
 import com.abudko.reseller.huuto.query.QueryConstants;
 import com.abudko.reseller.huuto.query.builder.ParamBuilder;
 import com.abudko.reseller.huuto.query.enumeration.Brand;
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.html.list.ListResponse;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 import com.abudko.reseller.huuto.query.rules.SearchQueryRules;
 import com.abudko.reseller.huuto.query.service.list.QueryListService;
 
 @Controller
 @SessionAttributes({ SEARCH_RESULTS_ATTRIBUTE, SEARCH_PARAMS_ATTRIBUTE })
 public class SearchController {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Resource(name = "searchParamBuilder")
     private ParamBuilder searchParamBuilder;
 
     @Resource
     private QueryListService queryListService;
 
     @Resource
     private SearchQueryRules searchQueryRules;
 
     @Resource
     private ControllerHelper controllerHelper;
 
     @RequestMapping(value = "/search", method = RequestMethod.GET)
     public String searchForm(Model model, HttpSession session) {
 
         log.info(String.format("Handling searchForm GET request"));
 
         if (model.containsAttribute(SEARCH_PARAMS_ATTRIBUTE) == false) {
             model.addAttribute(SEARCH_PARAMS_ATTRIBUTE, new SearchParams());
         }
 
         controllerHelper.setEnumConstantsToRequest(model, session);
 
         return SEARCH_FORM_PATH;
     }
 
     @RequestMapping(value = "/search/**", method = RequestMethod.POST)
     public String searchForm(@ModelAttribute(SEARCH_PARAMS_ATTRIBUTE) @Valid SearchParams searchParams,
             BindingResult result, Model model, HttpSession session) throws UnsupportedEncodingException,
             URISyntaxException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
 
         log.info(String.format("Handling searchForm POST request with parameters %s", searchParams));
 
         if (result.hasErrors()) {
             controllerHelper.setEnumConstantsToRequest(model, session);
             return SEARCH_FORM_PATH;
         }
 
         model.addAttribute(SEARCH_PARAMS_ATTRIBUTE, searchParams);
         searchQueryRules.apply(searchParams);
         String query = getQuery(searchParams);
 
         log.info(String.format("Quering search: %s", query));
 
         Collection<ListResponse> searchResults = queryListService.search(query, searchParams);
         model.addAttribute(SEARCH_RESULTS_ATTRIBUTE, searchResults);
 
         return "redirect:/items/search/results";
     }
 
     private String getQuery(SearchParams searchParams) throws IllegalAccessException, InvocationTargetException,
             NoSuchMethodException {
         String query = searchParamBuilder.buildQuery(searchParams);
         StringBuilder sb = new StringBuilder(QueryConstants.QUERY_URL);
         sb.append(query);
         return sb.toString();
     }
 
     @RequestMapping(value = "/search/results", method = RequestMethod.GET)
     public String searchResults(Model model, HttpSession session) {
         controllerHelper.setEnumConstantsToRequest(model, session);
 
         log.info(String.format("Handling searchResults GET request"));
         return SEARCH_FORM_PATH;
     }
 
     @RequestMapping(value = "/clear", method = RequestMethod.GET)
     public String clearSearchResults(Model model) {
         log.info(String.format("Handling clearSearchResults GET request"));
 
         model.addAttribute(SEARCH_RESULTS_ATTRIBUTE, new ArrayList<ListResponse>());
         model.addAttribute(SEARCH_PARAMS_ATTRIBUTE, new SearchParams());
 
         return "redirect:/items/search";
     }
 
     @RequestMapping(value = "/brands/{category}")
     @ResponseBody
     public List<String> brands(@PathVariable String category) {
         List<Brand> brands = controllerHelper.getBrands(Category.valueOf(category));
         List<String> response = new ArrayList<String>();
         for (Brand brand : brands) {
             response.add(brand.name());
             response.add(brand.getFullName());
         }
 
         return response;
     }
    
    @ExceptionHandler(Throwable.class)
    public String handleException(Throwable t) {
        return "redirect:/error.jsp";
    }
 }
