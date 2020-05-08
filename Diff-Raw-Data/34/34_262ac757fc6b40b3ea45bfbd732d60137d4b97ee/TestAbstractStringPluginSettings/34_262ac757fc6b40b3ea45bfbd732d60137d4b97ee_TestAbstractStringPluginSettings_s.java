 package com.atlassian.sal.core.pluginsettings;
 
 import java.util.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class TestAbstractStringPluginSettings
 {
     private PluginSettingsAcceptor acceptor;
     private static final String KEY = "some key";
 
     @Before
     public void setUp()
     {
         acceptor = new PluginSettingsAcceptor();
     }
 
     @Test
     public void testString()
     {
         String value = "this is the value";
         
         acceptor.put(KEY, value);
         assertEquals("Values should be equal.", value, acceptor.get(KEY));
     }
 
     @Test
     public void testList()
     {
         final List<String> value = new ArrayList<String>();
         final String first = "Beanz Meanz Heinz";
         final String second = "Sic 'em Rex!";
         
         value.add(first);
         value.add(second);
         
         acceptor.put(KEY, value);
         
         final Object actual = acceptor.get(KEY);
         
         assertTrue(actual instanceof List);
         
         final List<String> real = (List) actual;
         
         assertEquals("List size should be the same", 2, real.size());
         assertEquals("List should still be in order", first, real.get(0));
         assertEquals("List should still be in order", second, real.get(1));
     }
 
     @Test
     public void testMap()
     {
         final Map<String, String> value = new HashMap<String, String>();
         final String[] first = {"antzpantz", "Sic 'em Rex!"}; 
         final String[] second = {"homestar", "Lookin' at a thing in a bag"}; 
         final String[] third = {"he-man", "By the power of Greyskull!!!"};
         
         mapPut(value, first);
         mapPut(value, second);
         mapPut(value, third);
         
         acceptor.put(KEY, value);
 
         final Object actual = acceptor.get(KEY);
         
         assertTrue(actual instanceof Map);
         
         final Map real = (Map) actual;
         
         assertEquals("The size should be the same as what was entered.", 3, real.entrySet().size());
         assertMapEntryEquals("Map should retrieve the correct value for each key", real, first);
         assertMapEntryEquals("Map should retrieve the correct value for each key", real, second);
         assertMapEntryEquals("Map should retrieve the correct value for each key", real, third);
     }
 
     @Test
     public void testProperties()
     {
         final Properties value = new Properties();
         final String[] first = {"antzpantz", "Sic 'em Rex!"}; 
         final String[] second = {"homestar", "Lookin' at a thing in a bag"}; 
         final String[] third = {"he-man", "By the power of Greyskull!!!"};
      
         propertiesPut(value, first);        
         propertiesPut(value, second);        
         propertiesPut(value, third);
         
         acceptor.put(KEY, value);
         
         final Object actual = acceptor.get(KEY);
         
         assertTrue(actual instanceof Properties);
         
         final Properties real = (Properties) actual;
         
         assertPropertiesEntryEquals("Propertis should contain the same value for each key", real, first);
         assertPropertiesEntryEquals("Propertis should contain the same value for each key", real, second);
         assertPropertiesEntryEquals("Propertis should contain the same value for each key", real, third);
     }
 
     @Test
     public void testPutReturnValueNull()
     {
         List<String> value = Arrays.asList("one", "two", "three");
         assertNull(acceptor.put(KEY, value));
     }
 
     @Test
     public void testPutReturnValueOldValue()
     {
         List<String> oldValue = Arrays.asList("two", "three", "four");
         acceptor.put(KEY, oldValue);
         List<String> value = Arrays.asList("one", "two", "three");
         assertEquals(oldValue, acceptor.put(KEY, value));
     }
 
     @Test
     public void testRemoveReturnValueNull()
     {
         assertNull(acceptor.remove(KEY));
     }
 
     @Test
     public void testRemoveReturnValueOldValue()
     {
         List<String> oldValue = Arrays.asList("two", "three", "four");
         acceptor.put(KEY, oldValue);
         assertEquals(oldValue, acceptor.remove(KEY));
     }
 
     private void assertPropertiesEntryEquals(String errMsg, Properties real, String[] kvPair)
     {
         assertEquals(errMsg, kvPair[1], real.getProperty(kvPair[0]));
     }
 
     private static void propertiesPut(Properties properties, String[] values)
     {
         properties.setProperty(values[0], values[1]);
     }
 
     private static void assertMapEntryEquals(String errMsg, Map real, String[] kvPair)
     {
         assertEquals(errMsg, kvPair[1], real.get(kvPair[0]));
     }
 
     private static void mapPut(Map<String, String> map, String[] values)
     {
         map.put(values[0], values[1]);
     }
 
 
     private static final class PluginSettingsAcceptor extends AbstractStringPluginSettings
     {
         private final Map<String,String> backingStore = new HashMap();
         
         protected void putActual(String key, String val)
         {
             backingStore.put(key, val);
         }
 
         protected String getActual(String key)
         {
             return backingStore.get(key);                        
         }
 
         protected void removeActual(String key)
         {
             backingStore.remove(key);
         }
     }
 }
