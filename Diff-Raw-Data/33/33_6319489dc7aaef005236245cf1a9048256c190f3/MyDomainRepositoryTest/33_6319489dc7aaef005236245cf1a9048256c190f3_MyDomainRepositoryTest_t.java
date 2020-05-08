 package com.masternaut.repository.customer;
 
 import com.masternaut.PaddingtonException;
 import com.masternaut.repository.BaseCustomerRepositoryTest;
 import com.masternaut.repository.domain.MyDomain;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.util.StopWatch;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import static com.mongodb.util.ThreadUtil.sleep;
 import static junit.framework.Assert.assertNotSame;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 import static org.junit.Assert.*;
 
 public class MyDomainRepositoryTest extends BaseCustomerRepositoryTest {
     @Autowired
     private MyDomainRepository myDomainRepository;
 
     @Before
     public void before() {
         myDomainRepository.deleteAllForCustomer(customer1.getId());
         myDomainRepository.deleteAllForCustomer(customer2.getId());
         myDomainRepository.deleteAllForCustomer(customer3.getId());
     }
 
     @Test
    public void save_single_with_no_customerId() {
         MyDomain myDomain = new MyDomain();
         try {
             myDomainRepository.save(myDomain);
             fail("Exception not thrown");
         } catch (PaddingtonException pe) {
             assertEquals("CustomerId not set on MyDomain", pe.getMessage());
         }
     }
 
     @Test
    public void save_list_with_no_customerId() {
        try {
            myDomainRepository.save(new MyDomain(), new MyDomain());
            fail("Exception not thrown");
        } catch (PaddingtonException pe) {
            assertEquals("CustomerId not set on MyDomain", pe.getMessage());
        }
    }

    @Test
     public void saveList() {
         MyDomain myDomain1 = new MyDomain();
         myDomain1.setCustomerId(customer1.getId());
         myDomain1.setName("A1");
 
         MyDomain myDomain2 = new MyDomain();
         myDomain2.setCustomerId(customer1.getId());
         myDomain2.setName("A2");
 
         myDomainRepository.save(Arrays.asList(myDomain1, myDomain2));
 
         myDomainRepository.findById(myDomain1.getId(), customer1.getId());
         myDomainRepository.findById(myDomain1.getId(), customer1.getId());
     }
 
     @Test
     public void deleteAllForCustomerId() {
         MyDomain asset1 = new MyDomain();
         asset1.setCustomerId(customer1.getId());
         myDomainRepository.save(asset1);
 
         MyDomain asset2 = new MyDomain();
         asset2.setCustomerId(customer2.getId());
         myDomainRepository.save(asset2);
 
         myDomainRepository.deleteAllForCustomer(customer1.getId());
 
         List<MyDomain> assetsForCustomer1 = myDomainRepository.findAllForCustomer(customer1.getId());
         List<MyDomain> assetsForCustomer2 = myDomainRepository.findAllForCustomer(customer2.getId());
 
         assertEquals(0, assetsForCustomer1.size());
         assertEquals(1, assetsForCustomer2.size());
     }
 
     @Test
     public void findAllForCustomerId() {
         MyDomain customerAsset = new MyDomain();
         customerAsset.setCustomerId(customer1.getId());
         customerAsset.setName("MyCustomerAssetName");
         myDomainRepository.save(customerAsset);
 
         MyDomain otherCustomerAsset = new MyDomain();
         otherCustomerAsset.setCustomerId(customer1.getId());
         otherCustomerAsset.setName("MyOtherCustomerAsset");
         otherCustomerAsset.setCustomerId(customer2.getId());
         myDomainRepository.save(otherCustomerAsset);
 
         List<MyDomain> foundAssets = myDomainRepository.findAllForCustomer(customer1.getId());
 
         assertEquals(1, foundAssets.size());
         assertEquals("MyCustomerAssetName", foundAssets.get(0).getName());
         assertEquals(customer1.getId(), foundAssets.get(0).getCustomerId());
     }
 
     @Test
     public void count() {
         MyDomain a1 = new MyDomain();
         a1.setCustomerId(customer1.getId());
 
         MyDomain a2 = new MyDomain();
         a2.setCustomerId(customer2.getId());
 
         myDomainRepository.save(a1);
         myDomainRepository.save(a2);
 
         assertEquals(2, myDomainRepository.countForAllCustomers());
     }
 
     @Test
     public void findByInvalidId() {
         try {
             myDomainRepository.findById("InvalidId", customer1.getId());
             fail("exception not thrown");
         } catch (PaddingtonException pe) {
             assertEquals("MyDomain with id of 'InvalidId' not found.", pe.getMessage());
         }
     }
 
     @Test
     public void tryFindById_with_invalidId() {
         MyDomain myDomain = myDomainRepository.tryFindById("InvalidId", customer1.getId());
         assertNull(myDomain);
     }
 
     @Test
     public void tryFindById() {
         MyDomain myDomain = new MyDomain("A", customer1.getId());
         myDomainRepository.save(myDomain);
 
         MyDomain loadedMyDomain = myDomainRepository.tryFindById(myDomain.getId(), customer1.getId());
         assertEquals(myDomain.getName(), loadedMyDomain.getName());
     }
 
     @Test
     public void countForCustomer() {
         MyDomain asset1 = new MyDomain();
         asset1.setCustomerId(customer1.getId());
 
         MyDomain asset2 = new MyDomain();
         asset2.setCustomerId(customer1.getId());
 
         MyDomain asset3 = new MyDomain();
         asset3.setCustomerId(customer2.getId());
 
         myDomainRepository.save(Arrays.asList(asset1, asset2, asset3));
     }
 
     @Test
     public void bulkInsert() {
         MyDomain asset1 = new MyDomain();
         asset1.setCustomerId(customer1.getId());
 
         MyDomain asset2 = new MyDomain();
         asset2.setCustomerId(customer1.getId());
 
         myDomainRepository.bulkInsert(Arrays.asList(asset1, asset2));
 
         List<MyDomain> allForCustomer = myDomainRepository.findAllForCustomer(customer1.getId());
         assertEquals(2, allForCustomer.size());
     }
 
     @Test
     public void customer_MongoTemplate_are_cached_and_quick() {
         StopWatch stopWatch = new StopWatch();
         stopWatch.start();
 
         Set<MongoOperations> lotsOfCustomerConnections = myDomainRepository
                 .createLotsOfCustomerConnections(customer1.getId(), 10000);
 
         stopWatch.stop();
 
         System.out.println(String.format("Took %fs", stopWatch.getTotalTimeSeconds()));
 
         assertTrue(stopWatch.getTotalTimeSeconds() < 1);
 
         assertEquals(1, lotsOfCustomerConnections.size());
     }
 
     @Test
     @Ignore("slow")
     public void default_cache_is_for_10_seconds() {
         MongoTemplate mongoTemplate1 = (MongoTemplate) myDomainRepository.createLotsOfCustomerConnections(customer1.getId(), 1).toArray()[0];
 
         int elevenSeconds = 11000;
 
         sleep(elevenSeconds);
 
         MongoTemplate mongoTemplate2 = (MongoTemplate) myDomainRepository.createLotsOfCustomerConnections(customer1.getId(), 1).toArray()[0];
 
         assertNotSame(mongoTemplate1, mongoTemplate2);
     }
 
     @Test
     public void deleteById() {
         MyDomain domain1 = new MyDomain("a", customer1.getId());
         MyDomain domain2 = new MyDomain("b", customer1.getId());
 
         myDomainRepository.save(domain1, domain2);
 
         myDomainRepository.deleteById(domain1.getId(), customer1.getId());
 
         List<MyDomain> allForCustomer = myDomainRepository.findAllForCustomer(customer1.getId());
         assertEquals(1, allForCustomer.size());
         assertEquals("b", allForCustomer.get(0).getName());
     }
 
     @Test
     public void findByIds() {
         MyDomain domain1 = new MyDomain("a", customer1.getId());
         MyDomain domain2 = new MyDomain("b", customer1.getId());
         MyDomain domain3 = new MyDomain("c", customer2.getId());
 
         myDomainRepository.save(domain1, domain2, domain3);
 
         List<MyDomain> foundResults = myDomainRepository.findByIds(Arrays.asList(domain1.getId(), domain2.getId()), customer1.getId());
 
         assertEquals(2, foundResults.size());
         assertEquals("a", foundResults.get(0).getName());
         assertEquals("b", foundResults.get(1).getName());
     }
 
     @Test
     public void findAllForCustomer_with_paging(){
         createLotsOfDomainEntities(10, customer1.getId());
 
         PageRequest pageRequest = new PageRequest(2, 3);
 
         Page<MyDomain> pagedResults = myDomainRepository.findAllForCustomer(customer1.getId(), pageRequest);
 
         assertEquals(2, pagedResults.getNumber());
         assertEquals(3, pagedResults.getContent().size());
         assertEquals("6", pagedResults.getContent().get(0).getName());
         assertEquals("7",pagedResults.getContent().get(1).getName());
         assertEquals("8",pagedResults.getContent().get(2).getName());
     }
 
     @Test
     public void existsForCustomer_when_not_exists(){
         assertFalse(myDomainRepository.exists("NotAValidId", customer1.getId()));
     }
 
     @Test
     public void existsForCustomer_when_exists(){
         MyDomain myDomain = new MyDomain("A", customer1.getId());
         myDomainRepository.save(myDomain);
 
         assertTrue(myDomainRepository.exists(myDomain.getId(), customer1.getId()));
     }
 
     private void createLotsOfDomainEntities(int numberToCreate, String customerId) {
         for(int i = 0; i < numberToCreate ; i++){
             MyDomain myDomain = new MyDomain(String.format("%d", i), customerId);
             myDomainRepository.save(myDomain);
         }
     }
 }
