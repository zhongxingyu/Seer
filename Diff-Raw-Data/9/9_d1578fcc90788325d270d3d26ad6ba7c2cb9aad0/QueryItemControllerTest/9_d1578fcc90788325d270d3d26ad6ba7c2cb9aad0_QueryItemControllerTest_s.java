 package com.abudko.reseller.huuto.mvc.item;
 
 import static org.hamcrest.Matchers.hasProperty;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.when;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @ContextConfiguration(locations = { "classpath:/spring/test-webapp-config.xml" })
 public class QueryItemControllerTest {
 
     private static final String URL = "url";
 
     private static final String ITEM_PATH = "/item";
 
    private static final String FORWARDED_URL_PATH = "/WEB-INF/views/item.jsp";
 
     @Autowired
     private WebApplicationContext wac;
 
     @Resource
     private QueryItemService queryItemService;
 
     private MockMvc mockMvc;
 
     private ItemResponse itemResponse;
 
     @Before
     public void setup() {
         this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
 
         itemResponse = new ItemResponse();
         itemResponse.setCondition("condition");
         itemResponse.setHv(true);
         itemResponse.setImgBaseSrc("imgSrc");
         itemResponse.setLocation("location");
         itemResponse.setItemUrl("itemUrl");
         itemResponse.setPrice("43");
     }
 
     @Test
     public void testCreateItemOrderWithItemResponse() throws Exception {
         when(queryItemService.extractItem(URL)).thenReturn(itemResponse);
 
         this.mockMvc.perform(get(ITEM_PATH).param("url", URL)).andExpect(status().isOk())
                 .andExpect(model().attribute("itemOrder", hasProperty("itemResponse", is(itemResponse))))
                 .andExpect(forwardedUrl(FORWARDED_URL_PATH));
     }
 
     @Test
     public void testCreateItemOrderWithNewPrice() throws Exception {
         when(queryItemService.extractItem(URL)).thenReturn(itemResponse);
 
         this.mockMvc.perform(get(ITEM_PATH).param("url", URL)).andExpect(status().isOk())
                 .andExpect(model().attribute("itemOrder", hasProperty("newPrice", is(itemResponse.getPrice()))))
                 .andExpect(forwardedUrl(FORWARDED_URL_PATH));
     }
 }
