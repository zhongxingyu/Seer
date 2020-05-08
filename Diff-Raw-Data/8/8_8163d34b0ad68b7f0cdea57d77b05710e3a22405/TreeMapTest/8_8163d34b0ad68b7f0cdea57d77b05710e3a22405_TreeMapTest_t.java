 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.*;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.assertThat;
 
 public class TreeMapTest {
 
     private TreeMap treeMap;
 
     @Before
     public void setUp() throws Exception {
         treeMap = new TreeMap(){{
             put("0",0);
             put("1",8);
             put("2",16);
             put("3",48);
             put("7",68);
             put("9",65);
         }};
 
     }
 
     @Test
     public void should_get_the_least_ceiling_entry(){
         Map.Entry entry = treeMap.ceilingEntry("0");
 
         assertThat((String) entry.getKey(), is("0"));
     }
 
     @Test
     public void should_get_floor_entry() throws Exception {
         Map.Entry entry = treeMap.floorEntry("5");
         assertThat((String) entry.getKey(),is("3"));
     }
 
 
     @Test
     public void should_put_object_in_natural_order(){
         String key = (String) treeMap.ceilingKey("5");
 
         assertThat(key, is("7"));
     }
     @Test
     public void should_return_null_for_comparator(){
         Comparator comparator = treeMap.comparator();
         assertThat(comparator,nullValue());
     }
     @Test
     public void should_get_descending_set(){
         NavigableSet navigableSet = treeMap.descendingKeySet();
         assertThat((String) navigableSet.iterator().next(),is("9"));
     }
     @Test
     public void should_get_descending_map(){
         Set set = treeMap.descendingMap().keySet();
         assertThat((String) set.iterator().next(),is("9"));
     }
     @Test
     public void should_get_first_entry(){
         Map.Entry entry = treeMap.firstEntry();
         assertThat((String) entry.getKey(),is("0"));
     }
 
     @Test
     public void should_get_last_entry() throws Exception {
         Map.Entry entry = treeMap.lastEntry();
 
         assertThat((String) entry.getKey(),is("9"));
     }
 
     @Test
     public void should_get_higher_entry() throws Exception {
         Map.Entry entry = treeMap.higherEntry("2");
 
         assertThat((String) entry.getKey(),is("3"));
     }
 
     @Test
     public void should_get_lower_key() throws Exception {
         Map.Entry entry = treeMap.lowerEntry("2");
 
         assertThat((String) entry.getKey(),is("1"));
     }
 
     @Test
     public void should_get_head_map() throws Exception {
         SortedMap sortedMap = treeMap.headMap("2");
 
         assertThat(sortedMap.size(),is(2));
     }
 
     @Test
     public void should_get_head_map_with_equal_key() throws Exception {
         NavigableMap navigableMap = treeMap.headMap("2", true);
         assertThat(navigableMap.size(),is(3));
     }
 
     @Test
     public void should_get_tail_map() throws Exception {
         SortedMap sortedMap = treeMap.tailMap("6");
 
         assertThat(sortedMap.size(),is(2));
     }
 
     @Test
     public void should_get_tail_map_with_equal_key() throws Exception {
         NavigableMap navigableMap = treeMap.tailMap("7", true);
 
         assertThat(navigableMap.size(),is(2));
     }
 
     @Test
     public void should_poll_first_entry() throws Exception {
         Map.Entry entry = treeMap.pollFirstEntry();
 
        assertThat((String) entry.getKey(),is("0"));
     }
 
     @Test
     public void should_poll_last_entry() throws Exception {
         Map.Entry entry = treeMap.pollLastEntry();
         assertThat((String) entry.getKey(),is("9"));
     }
 
     @Test
     public void should_get_submap() throws Exception {
         SortedMap sortedMap = treeMap.subMap("2", "7");
         assertThat(sortedMap.size(),is(2));
     }
 
     @Test
     public void should_get_submap_bigger_than_keyfrom() throws Exception {
         NavigableMap navigableMap = treeMap.subMap("2", false, "7", false);
         assertThat(navigableMap.size(),is(1));
     }
 
     @Test
     public void should_get_submap_equal_to_keyto() throws Exception {
         NavigableMap navigableMap = treeMap.subMap("2", false, "7", true);
 
         assertThat(navigableMap.size(),is(2));
     }
 
     @Test
     public void should_return_old_value_when_key_exists() throws Exception {
         Integer put = (Integer) treeMap.put("0", 1000);
 
         assertThat(put,is(0));
     }
     @Test
     public void should_return_null_when_key_not_exists() throws Exception {
         Integer put = (Integer) treeMap.put("100", 1000);
 
         assertThat(put,nullValue());
     }
 
 }
