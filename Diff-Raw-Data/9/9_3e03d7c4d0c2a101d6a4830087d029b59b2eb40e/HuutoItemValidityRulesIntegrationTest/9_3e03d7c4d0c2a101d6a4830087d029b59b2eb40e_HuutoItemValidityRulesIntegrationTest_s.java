 package com.abudko.scheduled.rules.huuto;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.util.Collection;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.abudko.reseller.huuto.query.builder.ParamBuilder;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 import com.abudko.reseller.huuto.query.service.list.QueryListService;
 import com.abudko.scheduled.rules.ItemValidityRules;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/spring/test-app-config.xml" })
 public class HuutoItemValidityRulesIntegrationTest {
     
     @Autowired
     @Qualifier("huutoItemValidityRules")
     private ItemValidityRules rules;
     
     @Autowired
     @Qualifier("atomParamBuilder")
     private ParamBuilder paramBuilder;
 
     @Autowired
     @Qualifier("atomQueryListServiceImpl")
     private QueryListService queryService;
 
     @Test
     public void testIdInvalid() {
        assertFalse(rules.isValid("764577676"));;
     }
     
     @Test
     public void testIdValid() throws UnsupportedEncodingException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, URISyntaxException {
         SearchParams params = getSearchParams();
         Collection<ListResponse> result = queryService.search(getQuery(params), params);
         ListResponse response = result.iterator().next();
         
         String itemUrl = response.getItemUrl();
         int index = itemUrl.lastIndexOf("/");
         
         assertTrue(rules.isValid(itemUrl.substring(index + 1)));
     }
     
     @Test
     public void testIdNull() {
         assertTrue(rules.isValid(null));;
     }
     
     @Test
     public void testIdNotHuuto() {
         assertTrue(rules.isValid("LEishchi"));;
     }
     
     private SearchParams getSearchParams() {
         SearchParams searchParams = new SearchParams();
         searchParams.setWords("TALVIHAALARI");
         searchParams.setLocation("LAPUA");
         return searchParams;
     }
 
     private String getQuery(SearchParams searchParams) throws IllegalAccessException, InvocationTargetException,
             NoSuchMethodException {
         return paramBuilder.buildQuery(searchParams);
     }
 }
