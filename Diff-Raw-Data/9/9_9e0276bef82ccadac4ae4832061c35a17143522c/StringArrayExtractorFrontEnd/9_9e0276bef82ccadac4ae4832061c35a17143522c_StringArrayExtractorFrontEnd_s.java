 /**
  * Licensed under MPL / LGPL dual-license.
  *
  * Copyright (c) 2010 Tamas Eppel <Tamas.Eppel@gmail.com>
  *
  * You should have received a copy of the licenses
  * along with this program.
  * If not, see:
  *
  *    <http://www.gnu.org/licenses/>
  *    <http://www.mozilla.org/MPL/MPL-1.1.html>
  */
 package org.pcosta.vax.impl;
 
 import static com.google.common.base.Predicates.equalTo;
 import static com.google.common.base.Predicates.isNull;
 import static com.google.common.base.Predicates.not;
 import static com.google.common.base.Predicates.or;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.pcosta.vax.ExceptionHandler;
 import org.pcosta.vax.ExtractorFrontEnd;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 /**
  *
  * @author Tamas.Eppel@gmail.com
  *
  */
 public class StringArrayExtractorFrontEnd implements ExtractorFrontEnd<Map<String, String[]>> {
 
     private Map<String[], String[]> values = Collections.emptyMap();
 
     private String keySeparator = ".";
 
     private boolean skipBlanks = true;
 
     private boolean qualified = false;
 
     private static final Predicate<String> IS_NOT_BLANK = not(or(isNull(), equalTo("")));
 
     private static final Function<Object, String> TO_STRING = new Function<Object, String>() {
         @Override
         public String apply(final Object from) {
             return from == null ? null : from.toString();
         }
     };
 
     @Override
     public void init() {
         values = new HashMap<String[], String[]>();
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     public void addValue(final String[] key, final Object value) {
         final Collection<Object> valueList;
         if (value != null && value.getClass().isArray()) {
             valueList = newArrayList((Object[]) value);
        } else if (value != null && value instanceof Collection) {
             valueList = (Collection) value;
         } else {
             valueList = newArrayList(value);
         }
         List<String> resultList = newArrayList(Collections2.transform(valueList, TO_STRING));
         if (skipBlanks) {
             resultList = newArrayList(filter(resultList, IS_NOT_BLANK));
         }
         if ((skipBlanks && !resultList.isEmpty()) || !skipBlanks) {
             values.put(key, resultList.toArray(new String[resultList.size()]));
         }
     }
 
     @Override
     public boolean contains(final String[] key) {
         return values.containsKey(key);
     }
 
     @Override
     public void setExceptionHandler(final ExceptionHandler exceptionHandler) {
     }
 
     @Override
     public Map<String, String[]> getExtracted() {
         final Map<String, String[]> result = new HashMap<String, String[]>(values.size());
         for (final Entry<String[], String[]> entry : values.entrySet()) {
             final String[] keys = entry.getKey();
             String key;
             if (qualified) {
                 key = Joiner.on(keySeparator).join(keys);
             } else {
                 key = keys[keys.length - 1];
             }
             result.put(key, entry.getValue());
         }
         return result;
     }
 
     public String getKeySeparator() {
         return keySeparator;
     }
 
     public void setKeySeparator(final String keySeparator) {
         this.keySeparator = keySeparator;
     }
 
     public boolean isSkipBlanks() {
         return skipBlanks;
     }
 
     public void setSkipBlanks(final boolean skipBlanks) {
         this.skipBlanks = skipBlanks;
     }
 
     public boolean isQualified() {
         return qualified;
     }
 
     public void setQualified(final boolean qualified) {
         this.qualified = qualified;
     }
 }
