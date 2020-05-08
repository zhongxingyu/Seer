 package nl.astraeus.persistence;
 
 import nl.astraeus.persistence.reflect.ReflectHelper;
 
 import javax.annotation.CheckForNull;
 import java.util.*;
 
 /**
  * User: rnentjes
  * Date: 6/26/12
  * Time: 8:05 PM
  */
 public class PersistentQuery<K, M extends Persistent<K>> {
 
     private static enum SelectorType {
         EQUALS,
         NULL,
         NOT_NULL,
         GREATER,
         SMALLER,
         GREATER_EQUAL,
         SMALLER_EQUAL
     }
 
     private static class Selector {
         public SelectorType type;
         public Object value;
 
         private Selector(SelectorType type, Object value) {
             this.type = type;
             this.value = value;
         }
     }
 
     private PersistentDao<K, M> dao;
     private Map<String, Set<Selector>> selections = new HashMap<String, Set<Selector>>();
     private List<String> order = new LinkedList<String>();
     private int from, max;
 
     protected static class OrderComparator<K, M extends Persistent<K>> implements Comparator<M> {
 
         private String [] order;
 
         public OrderComparator(List<String> order) {
             this.order = order.toArray(new String[order.size()]);
         }
 
         @Override
         public int compare(M o1, M o2) {
             int index = 0;
             int result = 0;
 
             boolean desc;
             String field;
 
             while(index < order.length && result == 0) {
                 field = order[index];
                 desc = false;
 
                 if (field.charAt(0) == '-') {
                     desc = true;
                     field = field.substring(1);
                 } else if (field.charAt(0) == '+') {
                     field = field.substring(1);
                 }
 
                Object v1 = ReflectHelper.get().getFieldValue(o1, field);
                Object v2 = ReflectHelper.get().getFieldValue(o2, field);
 
                 if (v1 instanceof Comparable) {
                     result = ((Comparable)v1).compareTo(v2);
 
                     if (desc) {
                         result = -result;
                     }
                 }
 
                 index++;
             }
 
             if (result == 0) {
                if (o1.getId() instanceof Comparable && o1.getClass().equals(o2.getClass())) {
                     Comparable c1 = (Comparable)o1;
                     Comparable c2 = (Comparable)o2;
 
                     result = c1.compareTo(c2);
                 }
             }
 
             return result;
         }
     }
 
     public PersistentQuery(PersistentDao<K, M> dao) {
         this.dao = dao;
         this.from = 0;
         this.max = dao.size();
     }
 
     public PersistentQuery<K, M> from(int from) {
         this.from = from;
 
         return this;
     }
 
     public PersistentQuery<K, M> max(int max) {
         this.max = max;
 
         return this;
     }
 
     private void addSelection(String property, SelectorType type, Object value) {
         Set<Selector> values = selections.get(property);
 
         if (values == null) {
             values = new HashSet<Selector>();
 
             selections.put(property, values);
         }
 
         values.add(new Selector(type, value));
     }
 
     public PersistentQuery<K, M> where(String property, Object value) {
         addSelection(property, SelectorType.EQUALS, value);
 
         return this;
     }
 
     public PersistentQuery<K, M> equals(String property, Object value) {
         addSelection(property, SelectorType.EQUALS, value);
 
         return this;
     }
 
     public PersistentQuery<K, M> isNull(String property) {
         addSelection(property, SelectorType.NULL, null);
 
         return this;
     }
 
     public PersistentQuery<K, M> isNotNull(String property) {
         addSelection(property, SelectorType.NOT_NULL, null);
 
         return this;
     }
 
     public PersistentQuery<K, M> greater(String property, Object value) {
         addSelection(property, SelectorType.GREATER, null);
 
         return this;
     }
 
     public PersistentQuery<K, M> smaller(String property, Object value) {
         addSelection(property, SelectorType.SMALLER, null);
 
         return this;
     }
 
     public PersistentQuery<K, M> greaterEquals(String property, Object value) {
         addSelection(property, SelectorType.GREATER_EQUAL, null);
 
         return this;
     }
 
     public PersistentQuery<K, M> smallerEqual(String property, Object value) {
         addSelection(property, SelectorType.SMALLER_EQUAL, null);
 
         return this;
     }
 
     public PersistentQuery<K, M> order(String ... property) {
         order.addAll(Arrays.asList(property));
 
         return this;
     }
 
 
 
     public SortedSet<M> getResultSet() {
         // result as set with order comparator
         int fromCounter = 0;
         int nrResults = 0;
 
         SortedSet<M> result = new TreeSet<M>(new OrderComparator(order));
 
         Set<K> subSelection = null;
 
         Set<String> properties = selections.keySet();
         for (String property : properties) {
             PersistentIndex index = PersistentManager.get().getIndex(dao.getModelClass(), property);
 
             if (index != null) {
                 Set<Selector> selectors = selections.get(property);
 
                 for(Selector selector : selectors) {
                     if (selector.type == SelectorType.EQUALS) {
                         Set<K> keys = index.find(selector.value);
 
                         if (subSelection == null) {
                             subSelection = keys;
                         } else {
                             subSelection.retainAll(keys);
                         }
                     }
 
                 }
             }
         }
 
         if (subSelection != null) {
             for (K id : subSelection) {
                 M m = dao.find(id);
 
                 boolean match = isMatch(m);
 
                 if (match) {
                     if (fromCounter >= from) {
                         result.add(m);
                         nrResults++;
                     }
                     fromCounter++;
                 }
 
                 if (nrResults == max) {
                     break;
                 }
             }
         } else {
             for (M m : dao.findAll()) {
                 boolean match = isMatch(m);
 
                 if (match) {
                     if (fromCounter >= from) {
                         result.add(m);
                         nrResults++;
                     }
                     fromCounter++;
                 }
 
                 if (nrResults == max) {
                     break;
                 }
             }
         }
 
         return result;
     }
 
     @CheckForNull
     public M getSingleResult() {
         M result = null;
         Set<M> resultList = getResultSet();
 
         if (resultList.size() == 1) {
             result = resultList.iterator().next();
         } else {
             // warn or error???
         }
 
         return result;
     }
 
     public boolean isMatch(M m) {
         boolean result = true;
         Set<String> properties = selections.keySet();
 
         for (String property : properties) {
             Object om = ReflectHelper.get().getFieldValue(m, property);
 
             for (Selector selector : selections.get(property)) {
                 result &= compare(selector.type, om, selector.value);
             }
         }
 
         return result;
     }
 
     private boolean compare(SelectorType type, Object valueLeft, Object valueRight) {
         boolean result = false;
 
         switch(type) {
             case EQUALS:
                 if (valueLeft != null && valueLeft.equals(valueRight)) {
                     result = true;
                 }
                 break;
             case NULL:
                 result = valueLeft == null;
 
                 break;
             case NOT_NULL:
                 result = valueLeft != null;
 
                 break;
 //            case GREATER:
                 //result = !compare(GREATER, om, selector.value);
   //              break;
             default:
                 throw new IllegalStateException(type+" not implemented yet!");
         }
 
         return result;
     }
 }
