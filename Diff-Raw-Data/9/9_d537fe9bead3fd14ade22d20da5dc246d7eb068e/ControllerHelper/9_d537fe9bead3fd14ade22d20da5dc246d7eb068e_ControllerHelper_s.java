 package com.abudko.reseller.huuto.mvc;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Component;
 import org.springframework.ui.Model;
 
 import com.abudko.reseller.huuto.query.enumeration.Addtime;
 import com.abudko.reseller.huuto.query.enumeration.Brand;
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 
 @Component
 public class ControllerHelper {
 
     public static final String SEARCH_PARAMS_ATTRIBUTE = "searchParams";
 
     public static final String SEARCH_RESULTS_ATTRIBUTE = "searchResults";
 
     public static final String ADD_TIMES_ATTRIBUTE = "addtimes";
 
     public static final String CATEGORIES_ATTRIBUTE = "categories";
 
     public static final String BRAND_ATTRIBUTE = "brands";
 
     public static final String SEARCH_FORM_PATH = "search/search";
 
     private Map<Category, List<Brand>> brandMap;
 
     public void setEnumConstantsToRequest(Model model, HttpSession session) {
         model.addAttribute(ADD_TIMES_ATTRIBUTE, Addtime.values());
         model.addAttribute(CATEGORIES_ATTRIBUTE, Category.values());
         SearchParams searchParams = (SearchParams) session.getAttribute(SEARCH_PARAMS_ATTRIBUTE);
         model.addAttribute(BRAND_ATTRIBUTE,
                this.getBrands(searchParams == null || searchParams.getWords() == null ? Category.TALVIHAALARI
                         : Category.valueOf(searchParams.getWords())));
     }
 
     public List<Brand> getBrands(Category category) {
         if (brandMap == null) {
             buildBrandMap();
         }
 
         return brandMap.get(category);
     }
 
     private void buildBrandMap() {
         brandMap = new HashMap<Category, List<Brand>>();
 
         Brand[] brands = Brand.values();
 
         for (Brand brand : brands) {
             Category[] categories = brand.getCategories();
             for (Category category : categories) {
                 if (brandMap.get(category) != null) {
                     List<Brand> brandList = brandMap.get(category);
                     brandList.add(brand);
                 } else {
                     List<Brand> brandList = new ArrayList<Brand>();
                     brandList.add(brand);
                     brandMap.put(category, brandList);
                 }
             }
         }
     }
 }
