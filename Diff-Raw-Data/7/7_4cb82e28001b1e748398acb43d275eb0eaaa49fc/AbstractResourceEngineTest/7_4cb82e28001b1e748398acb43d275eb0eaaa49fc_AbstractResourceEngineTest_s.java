 /* AbstractResourceEngineTest.java - created on May 9, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.store;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import eu.europeana.uim.api.ResourceEngine;
 import eu.europeana.uim.store.bean.CollectionBean;
 import eu.europeana.uim.store.bean.ProviderBean;
 
 /**
  * Abstract base class to test the contract of the resource engine
  * 
  * @author Rene Wiermer (rene.wiermer@kb.nl)
  * @param <I> 
  * @date May 9, 2011
  */
 public abstract class AbstractResourceEngineTest<I> {
         ResourceEngine<I> engine = null;
         final String EXAMPLE_KEY_1="example1.property.file";
         final String EXAMPLE_KEY_2="example2.property";
         final String EXAMPLE_KEY_3="3";
         final String EXAMPLE_KEY_4="4";
         final String EXAMPLE_VALUE_1="exampleValue1";
         final String EXAMPLE_VALUE_2="exampleValue2";
               
 
         /**
          * Setups storage engine.
          */
         @Before
         public void setUp() {
             engine = getResourceEngine();
             performSetUp();
         }
 
         /**
          * Override this for additional setup
          */
         protected void performSetUp() {
             // nothing todo
         }
 
         /**
          * @return configured storage engine
          */
         protected abstract ResourceEngine<I> getResourceEngine();
         
         
         /**
          * 
          */
         protected abstract I getExampleID();
             
         abstract class AbstractCreateAndGetResourceTestCase<J> {
             
             public abstract LinkedHashMap<String,List<String>>  getEntityResources(J entity,List<String> keys);
             public abstract void setEntityResources(J entity,LinkedHashMap<String, List<String>> resources);
             public abstract J getExampleEntity();
             public void run() {
                 //simple put and get
                 List<String> testList1=new LinkedList<String>();
                 testList1.add(EXAMPLE_VALUE_1);
                 testList1.add(EXAMPLE_VALUE_2); 
                 LinkedHashMap<String, List<String>> testSet1=new LinkedHashMap<String, List<String>>();
                 testSet1.put(EXAMPLE_KEY_1,testList1);
                 J testEntity=getExampleEntity();
                 setEntityResources(testEntity,testSet1);
                 
                 @SuppressWarnings({ "unchecked", "rawtypes" })
                 LinkedHashMap<String,List<String>> result=getEntityResources(testEntity, new LinkedList(testSet1.keySet()));
                 assertNotNull(result);
                 assertNotNull(result.get(EXAMPLE_KEY_1));
                 assertEquals(2,result.get(EXAMPLE_KEY_1).size());
                 assertEquals(EXAMPLE_VALUE_1,result.get(EXAMPLE_KEY_1).get(0));
                 assertEquals(EXAMPLE_VALUE_2,result.get(EXAMPLE_KEY_1).get(1));
                 
                 
                 //overwrite with fewer list entrys
                 List<String> testList2=new LinkedList<String>();
                 testList2.add(EXAMPLE_VALUE_1);
                
                 LinkedHashMap<String, List<String>> testSet2=new LinkedHashMap<String, List<String>>();
                 testSet2.put(EXAMPLE_KEY_1,testList2);
                 
                 setEntityResources(testEntity,testSet2);
                 
                 @SuppressWarnings({ "rawtypes", "unchecked" })
                 LinkedHashMap<String,List<String>> result2=getEntityResources(testEntity,new LinkedList(testSet2.keySet()));
                 assertNotNull(result2);
                 assertEquals(1,result2.get(EXAMPLE_KEY_1).size());
                 assertEquals(EXAMPLE_VALUE_1,result2.get(EXAMPLE_KEY_1).get(0)); 
                 
                 
                 //check if you ask for more keys than there are actually stored
                 //return only those which are in there
                 
                 List<String> testList=new LinkedList<String>();
                 testList.add(EXAMPLE_VALUE_1);
                 testList.add(EXAMPLE_VALUE_2); 
                 
                 LinkedHashMap<String, List<String>> testSet3=new LinkedHashMap<String, List<String>>();
                testSet1.put(EXAMPLE_KEY_3,testList);
                engine.setGlobalResources(testSet3);
                 
                 LinkedList<String> keys=new LinkedList<String>();
                 keys.add(EXAMPLE_KEY_3);
                 keys.add(EXAMPLE_KEY_4);
                 
                LinkedHashMap<String,List<String>> result3=engine.getGlobalResources(keys);
                 assertNotNull(result3);
                 assertEquals(1,result3.size());
                 assertNotNull(result3.get(EXAMPLE_KEY_3));
                 assertEquals(2,result3.get(EXAMPLE_KEY_3).size());     
                 assertNull(result3.get(EXAMPLE_KEY_4));
             }
         }
         
         @Test
         public void testCreateAndGetGlobalResources() {
     
    
           new AbstractCreateAndGetResourceTestCase<String>(){
 
             @Override
             public LinkedHashMap<String, List<String>> getEntityResources(String entity,
                     List<String> keys) {
                 return engine.getGlobalResources(keys);
             }
 
             @Override
             public void setEntityResources(String entity,
                     LinkedHashMap<String, List<String>> resources) {
                 engine.setGlobalResources(resources);
             }
 
             @Override
             public String getExampleEntity() {
                 return null;
             } 
           }.run();
           
            
 
                               
         }
         
         @Test 
         public void testMoreGlobalPropertiesRequestedThenExists() {
             
             //ask for more keys than there are actually stored.
             //The keys should be included in the result but have empty entry at the global level
 
             
         }
         
         @Test(expected=IllegalArgumentException.class)
         public void testNullGlobalResource() {
             engine.setGlobalResources(null);
         }
         
         
         
         @Test
         public void testCreateAndGetProviderResources() {    
    
           new AbstractCreateAndGetResourceTestCase<Provider<I>>(){
             @Override
             public LinkedHashMap<String, List<String>> getEntityResources(Provider<I> entity,
                     List<String> keys) {
                 return engine.getProviderResources(entity, keys);
             }
 
             @Override
             public void setEntityResources(Provider<I> entity,
                     LinkedHashMap<String, List<String>> resources) {
               engine.setProviderResources(entity, resources);
             } 
             
             @Override
             public Provider<I> getExampleEntity() {
                 return new ProviderBean<I>(getExampleID()); 
                     
               
             }
           }.run();
                                         
         }
         
         
         @Test
         public void testCreateAndGetCollectionResources() {    
    
           new AbstractCreateAndGetResourceTestCase<Collection<I>>(){
             @Override
             public LinkedHashMap<String, List<String>> getEntityResources(Collection<I> entity,
                     List<String> keys) {
                 return engine.getCollectionResources(entity, keys);
             }
 
             @Override
             public void setEntityResources(Collection<I> entity,
                     LinkedHashMap<String, List<String>> resources) {
               engine.setCollectionResources(entity, resources);
             } 
             
             @Override
             public Collection<I> getExampleEntity() {
                 return new CollectionBean<I>(getExampleID(),new ProviderBean<I>(getExampleID()));      
                 
             }
           }.run();
                                         
         }
        
         
 }
