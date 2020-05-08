 package matcher;
 
 import java.util.List;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 
 public class Lists {
     /**
      * スタティックファクトリ.
      * @param items expected list.
      * @return matcher for list
      */
    public static <T> Matcher<List<?>> list(@SuppressWarnings("unchecked") T... items) {
         return new ListMatcher(items);
     }
     
     static class ListMatcher extends BaseMatcher<List<?>> {
         final Object[] items;  // expected
         List<?> actual;
         boolean matches = false;
         int index = 0;  // 失敗したときのインデックス
         
         ListMatcher(Object[] items) {
             this.items = items;
         }
 
         @Override
         public boolean matches(Object actual) {
             if(!(actual instanceof List)) {
                 return false;
             }
             
             List<?> list = (List<?>) actual;
             this.actual = list;
             
             if (list.size() != items.length) {
                 return false;
             }
             
             for (Object object : list) {
                 Object other = items[index];
                 if (object != null && !object.equals(other)) {
                     return false;
                 } else if (object == null && other != null) {
                     return false;
                 }
                 index += 1;
             }
             
             return true;
         }
         
         @Override
         public void describeTo(Description description) {
             if (actual == null) {
                 description.appendValue(items);
             } else {
                 description.appendValue(items[index])
                            .appendText(", but")
                            .appendValue(actual.get(index))
                            .appendText(" at index of " + index);
                 
             }
         }
         
     }
     
     
 }
