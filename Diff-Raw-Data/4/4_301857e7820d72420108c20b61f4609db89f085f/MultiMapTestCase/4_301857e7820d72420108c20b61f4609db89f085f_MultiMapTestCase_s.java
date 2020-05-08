 package org.openblend.guava101.test;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimaps;
 import com.google.common.collect.SetMultimap;
 import junit.framework.Assert;
 import org.junit.Test;
 import org.openblend.guava101.support.Person;
 
 import static org.openblend.guava101.support.Constants.ALES;
 import static org.openblend.guava101.support.Constants.ANDY;
 import static org.openblend.guava101.support.Constants.ELA;
 import static org.openblend.guava101.support.Constants.JANEZ;
 import static org.openblend.guava101.support.Constants.LJUBLJANA;
 import static org.openblend.guava101.support.Constants.VRHNIKA;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public class MultiMapTestCase {
     @Test
     public void testBefore() throws Exception {
        Map<String, Collection<Person>> map = new HashMap<String, Collection<Person>>();
         put(map, VRHNIKA, ALES);
         put(map, LJUBLJANA, ANDY);
         put(map, VRHNIKA, ELA);
         put(map, LJUBLJANA, JANEZ);
         put(map, VRHNIKA, ELA);
 
         doAssert(map);
     }
 
     protected synchronized void put(Map<String, Collection<Person>> map, String key, Person person) {
         Collection<Person> persons = map.get(key);
         if (persons == null) {
             persons = new HashSet<Person>();
             map.put(key, persons);
         }
         persons.add(person);
     }
 
     @Test
     public void testAfter() throws Exception {
         SetMultimap<String, Person> map = Multimaps.synchronizedSetMultimap(HashMultimap.<String, Person>create());
         map.put(VRHNIKA, ALES);
         map.put(LJUBLJANA, ANDY);
         map.put(VRHNIKA, ELA);
         map.put(LJUBLJANA, JANEZ);
         map.put(VRHNIKA, ELA);
 
         doAssert(map.asMap());
     }
 
     private void doAssert(Map<String, Collection<Person>> map) {
         System.out.println(map);
         Assert.assertEquals(2, map.get(VRHNIKA).size());
         Assert.assertEquals(2, map.get(LJUBLJANA).size());
     }
 }
