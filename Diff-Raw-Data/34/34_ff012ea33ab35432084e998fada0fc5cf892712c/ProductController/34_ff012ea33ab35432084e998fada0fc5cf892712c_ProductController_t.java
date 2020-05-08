 package com.discount.web;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.discount.domain.Producer;
 import com.discount.domain.ProductCategory;
 import com.discount.domain.Review;
 import com.discount.domain.Product;
 import com.discount.domain.Range;
 import com.discount.service.ProducerService;
 import com.discount.service.ProductCategoryService;
 import com.discount.service.ProductService;
 import com.discount.service.RangeService;
 
 @Controller
 @RequestMapping(UrlConstants.PRODUCTS)
 public class ProductController extends BaseController {
 
 	@Autowired
 	private ProductService productService;
 	@Autowired
 	private ProductCategoryService categoryService;
 	@Autowired
 	private ProducerService producerService;
 	@Autowired
 	private RangeService rangeService;
 
 	@RequestMapping(UrlConstants.PRODUCTS)
 	public String getProducts(Map<String, Object> map) {
 		putRootCategories(map);
 		map.put("products", productService.findAll());
 
 		return "products";
 	}
 
 	@RequestMapping(UrlConstants.GET_PRODUCT_BY_CATEGORY)
 	public String getProductsByCategory(
 			@PathVariable("categoryId") Integer categoryId,
 			Map<String, Object> map) {
 		putRootCategories(map);
 
 		// List<Producer> producersByCategory = producerService
 		// .findByCategoryId(categoryId);
 		ProductCategory category = categoryService.findById(categoryId);
 		List<Producer> producers = category.getProducers();
 		Map<Integer, List<Product>> productsByCategoryAndProducer = new HashMap<Integer, List<Product>>();
 
 		for (Producer producer : producers) {
 			productsByCategoryAndProducer.put(producer.getId(), productService
 					.findByCategoryAndProducer(categoryId, producer.getId()));
 		}
 
 		map.put("category", category);
 		map.put("producers", producers);
 		map.put("productsByCategory",
 				productService.findByCategoryId(categoryId));
 		map.put("productsByCategoryAndProducer", productsByCategoryAndProducer);
 
 		return "products-by-category";
 	}
 
 	@RequestMapping(UrlConstants.GET_PRODUCT_BY_RANGE)
 	public String getProductsByRange(@PathVariable("rangeId") Integer rangeId,
 			Map<String, Object> map) {
 		putRootCategories(map);
 
 		Range range = rangeService.findById(rangeId);
 		List<Product> productsByRange = productService.findByRange(rangeId);
 		map.put("range", range);
 		map.put("productsByRange", productsByRange);
 
 		return "products-by-range";
 	}
 
 	@RequestMapping(UrlConstants.GET_PRODUCT_BY_PRODUCER)
 	public String getProductsByProducerId(
 			@PathVariable("producerId") Integer producerId,
 			Map<String, Object> map) {
 		putRootCategories(map);
 
 		map.put("productsByProducer",
 				productService.findByProducerId(producerId));
 		map.put("producer", producerService.findById(producerId));
 		return "products-by-producer";
 	}
 
 	@RequestMapping(UrlConstants.GET_PRODUCT)
 	public String getProductById(@PathVariable("productId") Integer productId,
 			Map<String, Object> map) {
 		putRootCategories(map);
 		map.put("comment", new Review());
 
 		Product product = productService.findById(productId);
 		map.put("product", product);
		if (product != null) {
			Range range = product.getRange();
			List<Product> productByRange = new ArrayList<Product>();
			if (range != null) {
				productByRange = productService.findByRange(range.getId());
				map.put("productsByRange", productByRange);
			}
 		}
 		return "product";
 	}
 
 	@RequestMapping(UrlConstants.GET_PRODUCT_POPUP_INFO)
 	public String getProductPopupInfo(
 			@PathVariable("productId") Integer productId,
 			Map<String, Object> map) {
 		Product product = productService.findById(productId);
 		map.put("product", product);
 		Range range = product.getRange();
 		if (range != null) {
 			map.put("productsByRange",
 					productService.findByRange(range.getId()));
 		}
 		return "small-product-popup";
 	}
 
 }
