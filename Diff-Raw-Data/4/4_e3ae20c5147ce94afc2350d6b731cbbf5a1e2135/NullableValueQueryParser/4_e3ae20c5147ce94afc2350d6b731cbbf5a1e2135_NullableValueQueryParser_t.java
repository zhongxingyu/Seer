 package com.brightcove.johnny.http;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import com.brightcove.johnny.coll.MapEntry;
 
 /**
  * A query parser that allows keys that are missing values (e.g. the second
  * <code>a</code> in <code>?a=5&amp;a&amp;b=7</code>) and uses a null value
  * to represent them.
  */
 public class NullableValueQueryParser implements QueryParser {
 
     private static final List<Map.Entry<String,String>> EMPTY_LIST =
             Collections.unmodifiableList(new ArrayList<Map.Entry<String,String>>(0));
 
     /** Pattern that finds ampersands. */
     public static final Pattern AMPERSAND = Pattern.compile("&");
     /**
      * Pattern that finds ampersands or semicolons. The W3C recommends
      * allowing both, although this is very rare in the wild.
      */
     public static final Pattern AMPERSAND_OR_SEMICOLON = Pattern.compile("[&;]");
 
     private final Pattern pairSep;
     private final Pattern kSep = Pattern.compile("=");
 
     /**
      * Default parser, using ampersand (<code>&amp;</code>) to delimit
      * key-value pairs from each other.
      */
     public NullableValueQueryParser() {
         this(AMPERSAND);
     }
 
     /**
      * Split key-value pairs from each other based on arbitrary pattern.
      */
     public NullableValueQueryParser(Pattern pairSep) {
         if (pairSep == null) {
             throw new NullPointerException("pair separator pattern must not be null");
         }
         this.pairSep = pairSep;
     }
 
     public Iterable<Map.Entry<String, String>> parseAs(String queryRaw) {
         if (queryRaw == null) {
             return EMPTY_LIST;
         }
         LinkedList<Map.Entry<String, String>> ret = new LinkedList<Map.Entry<String, String>>();
         String[] pairs = pairSep.split(queryRaw);
         for (String pair : pairs) {
             if (pair.isEmpty()) {
                 continue;
             }
             String[] kv = kSep.split(pair, 2);
            String k = Codecs.percentDecode(kv[0]);
            String v = kv.length == 1 ? null : Codecs.percentDecode(kv[1]);
            ret.addLast(new MapEntry<String, String>(k, v));
         }
         return Collections.unmodifiableList(ret);
     }
 }
