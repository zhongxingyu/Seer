 package com.euroit.militaryshop.web.controller;
 
import com.euroit.eshop.bean.Trolley;
import com.euroit.eshop.dto.BaseTrolleyItemDto;
 import com.euroit.eshop.exception.VisibleProductNotFoundException;
 import com.euroit.militaryshop.dto.CategoryDto;
 import com.euroit.militaryshop.dto.ProductDto;
 import com.euroit.militaryshop.service.CategoryService;
 import com.euroit.militaryshop.service.ProductService;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.test.web.ModelAndViewAssert;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ProductCardControllerIT {
 	
 	private static final long FAKE_PRODUCT_ID = 777;
 
 	private static final Long CATEGORY_ID = 1l;
 
     private static final String SOME_SEO_NAME = "adidas-wollmantel-klassischer-mantel-ruby";
 	
 	private ProductCardController controller = new ProductCardController();
 	@Mock private ProductService productService;
 	@Mock private CategoryService categoryService;
     @Mock private HttpServletRequest request;
    @Mock private Trolley<BaseTrolleyItemDto> trolley;
     
     private MockMvc mockMvc;
 	
 	@Before public void setUp () {
 		controller.setProductService(productService);
 		controller.setCategoryService(categoryService);
		controller.setTrolley(trolley);
 		
 		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
 	}
 	
 	@Test
 	public void testShowCard() throws Exception {
 		ProductDto productDto = new ProductDto();
 		productDto.setId(FAKE_PRODUCT_ID);
 		
 		List<Long> categoryList = new ArrayList<Long>();
 		categoryList.add(CATEGORY_ID);
 		productDto.setCategoryIds(categoryList);
 		
 		when(productService.getVisibleProductById(FAKE_PRODUCT_ID))
 			.thenReturn(productDto);
 		when(categoryService.findCategoryById(CATEGORY_ID)).thenReturn(new CategoryDto());
 		
 		mockMvc.perform(get("/product/" + FAKE_PRODUCT_ID))
 		    .andExpect(forwardedUrl("product.card"))
 		    .andExpect(model().attributeExists("productDto", "categoryDto"));
 		
 		verify(productService).getVisibleProductById(FAKE_PRODUCT_ID);
 		verify(categoryService).findCategoryById(CATEGORY_ID);		
 	}
 
     @Test
     public void testShowCard2() throws Exception {
         doThrow(new VisibleProductNotFoundException()).when(productService).getVisibleProductById(FAKE_PRODUCT_ID);
         mockMvc.perform(get("/product/" + FAKE_PRODUCT_ID)).andExpect(status().isNotFound());
     }
     
     @Test
     public void shouldShowCardByDefaultSeoNameWithoutCategories() throws Exception {
         ProductDto productDto = new ProductDto();
         productDto.setId(FAKE_PRODUCT_ID);
         productDto.setCategoryIds(new ArrayList<Long>());
         when(productService.getVisibleProductByItemSeoName(eq(SOME_SEO_NAME), anyLong(), anyLong()))
             .thenReturn(productDto);
         mockMvc.perform(get("/" + SOME_SEO_NAME + ".html")).andExpect(status().isOk());
     }
     
     @Test
     public void shouldNotShowCardByDefaultSeoName() throws Exception {
         doThrow(new VisibleProductNotFoundException()).when(productService)
             .getVisibleProductByItemSeoName(eq(SOME_SEO_NAME), anyLong(), anyLong());
         mockMvc.perform(get("/" + SOME_SEO_NAME + ".html")).andExpect(status().isNotFound());
     }
 }
