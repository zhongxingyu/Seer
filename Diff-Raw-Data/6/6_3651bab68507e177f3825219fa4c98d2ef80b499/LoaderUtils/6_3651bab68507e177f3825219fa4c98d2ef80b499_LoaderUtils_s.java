 package org.sankozi.rogueland.data;
 
 import com.google.common.base.CaseFormat;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @author sankozi
  */
 class LoaderUtils {
 
    static <K extends Enum<K>> EnumMap<K, Float> toFloatMap(Map map, Class<K> enumClass){
        EnumMap<K, Float> ret = new EnumMap(enumClass);
        for(Map.Entry entry : (Set<Map.Entry>)map.entrySet()){
             ret.put(Enum.valueOf(enumClass, CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, entry.getKey().toString())), 
                     ((Number)entry.getValue()).floatValue());
         }
         return ret;
     } 
 }
