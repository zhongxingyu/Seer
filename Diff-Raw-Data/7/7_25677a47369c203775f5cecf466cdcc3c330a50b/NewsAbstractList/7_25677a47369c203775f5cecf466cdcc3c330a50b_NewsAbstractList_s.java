 package com.osastudio.newshub.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.text.TextUtils;
 
 public class NewsAbstractList extends NewsBaseObject {
 
    public static final String JSON_KEY_ABSTRACT_LIST = "list";
 
    private List<NewsAbstract> abstractList;
    private HashMap<String, Integer> abstractMap;
 
    public NewsAbstractList() {
      this.abstractList = new ArrayList<NewsAbstract>();
      this.abstractMap = new LinkedHashMap<String, Integer>();
    }
 
    public List<NewsAbstract> getAbstractList() {
       return this.abstractList;
    }
 
    public NewsAbstractList setAbstractList(List<NewsAbstract> list) {
       this.abstractList = list;

       NewsAbstract abs = null;
       for (int i = 0; i < this.abstractList.size(); i++) {
          abs = this.abstractList.get(i);
          if (abs != null) {
             this.abstractMap.put(abs.getArticleId(), i);
          }
       }
       return this;
    }
    
    public int getNewsAbstractIndex(NewsAbstract abs) {
       if (abs == null || TextUtils.isEmpty(abs.getArticleId())
             || !this.abstractMap.containsKey(abs.getArticleId())) {
          return -1;
       }
       return this.abstractMap.get(abs.getArticleId());
    }
    
    public static NewsAbstractList parseJsonObject(JSONObject jsonObject) {
       NewsAbstractList result = null;
       try {
          if (jsonObject.isNull(JSON_KEY_RESULT_CODE)
                || NewsAbstractList.isResultFailure(jsonObject
                      .getString(JSON_KEY_RESULT_CODE))) {
             return null;
          }
 
          result = new NewsAbstractList();
          if (!jsonObject.isNull(JSON_KEY_ABSTRACT_LIST)) {
             JSONArray jsonArray = jsonObject
                   .getJSONArray(JSON_KEY_ABSTRACT_LIST);
             List<NewsAbstract> list = new ArrayList<NewsAbstract>();
             NewsAbstract abs = null;
             for (int i = 0; i < jsonArray.length(); i++) {
                try {
                   if (!jsonArray.isNull(i)) {
                      abs = NewsAbstract.parseJsonObject(jsonArray
                            .getJSONObject(i));
                      if (abs != null) {
                         list.add(abs);
                      }
                   }
                } catch (JSONException e) {
                   continue;
                }
             }
             result.setAbstractList(list);
          }
       } catch (JSONException e) {
 
       }
 
       return result;
    }
 
 }
