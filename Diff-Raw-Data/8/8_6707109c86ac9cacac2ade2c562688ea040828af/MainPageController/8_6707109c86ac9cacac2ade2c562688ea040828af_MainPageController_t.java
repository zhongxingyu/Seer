 package com.euroit.militaryshop.web.controller;
 
 import com.euroit.militaryshop.dto.CategoryDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.service.CategoryService;
 import com.euroit.militaryshop.service.ProductService;
 import com.euroit.militaryshop.web.controller.base.BaseTrolleyAwareController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 @Controller
 @RequestMapping("/")
 public class MainPageController extends BaseTrolleyAwareController {
	private static final Logger log = LoggerFactory.getLogger(MainPageController.class);
     org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(MainPageController.class);
 
 	static final String PRODUCT_LIST = "productList";
 
 	static final String CATEGORY_LIST = "categoryList";
 	
 	@Autowired
 	ProductService productService;	
 
 	@Autowired
 	CategoryService categoryService;
 
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView show() {
 		ModelAndView mav = new ModelAndView("mainPage");
 		List<ProductDto> listProductDto = productService.getVisibleProducts();
 		mav.addObject(PRODUCT_LIST, listProductDto);
 		List<CategoryDto> listCategoryDto = categoryService.getAllCategories();
 		mav.addObject(CATEGORY_LIST, listCategoryDto);
 
 		return mav;
 	}
 }
