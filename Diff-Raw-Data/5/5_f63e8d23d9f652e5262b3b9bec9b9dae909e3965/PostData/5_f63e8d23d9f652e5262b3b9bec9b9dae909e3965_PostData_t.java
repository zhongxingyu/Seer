 package org.notlocalhost.cpanel;
 
import java.util.HashMap;
 import java.util.Map;
 
 public class PostData {
     private String _module;
     private String _function;
     private Map<String, String> _data;
     
     public PostData() {
        _data = new HashMap<String, String>();
     }
     public PostData(String module, String function) {
         _module = module;
         _function = function;
        _data = new HashMap<String, String>();
     }
     public PostData putString(String key, String value) {
         _data.put(key, value);
         return this;
     }
     public PostData putInt(String key, Integer value) {
         _data.put(key, value.toString());
         return this;
     }
     public PostData putFloat(String key, Float value) {
         _data.put(key, value.toString());
         return this;
     }
     public PostData setModule(String module) {
         _module = module;
         return this;
     }
     public PostData setFunction(String function) {
         _function = function;
         return this;
     }
     
     public String getString(String key) {
         return _data.get(key);
     }
 }
