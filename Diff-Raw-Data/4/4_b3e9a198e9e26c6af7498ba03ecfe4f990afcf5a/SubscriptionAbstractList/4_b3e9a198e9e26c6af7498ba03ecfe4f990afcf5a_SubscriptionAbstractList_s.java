 package com.osastudio.newshub.data;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.osastudio.newshub.data.base.NewsBaseAbstractList;
 
 public class SubscriptionAbstractList extends
       NewsBaseAbstractList<SubscriptionAbstract> {
    
   public static final String JSON_KEY_CHANNEL_NAME = "lssue_title";
   public static final String JSON_KEY_CHANNEL_DESCRIPTION = "lssue_note";
 
    private String channelName;
    private String channelDesc;
 
    public SubscriptionAbstractList() {
       super();
    }
 
    public SubscriptionAbstractList(JSONObject jsonObject) {
       super(jsonObject);
 
       if (isSuccess()) {
          try {
             if (!jsonObject.isNull(JSON_KEY_CHANNEL_NAME)) {
                setChannelName(jsonObject.getString(JSON_KEY_CHANNEL_NAME)
                      .trim());
             }
             if (!jsonObject.isNull(JSON_KEY_CHANNEL_DESCRIPTION)) {
                setChannelDesciption(jsonObject.getString(
                      JSON_KEY_CHANNEL_DESCRIPTION).trim());
             }
             if (!jsonObject.isNull(JSON_KEY_LIST)) {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_LIST);
                SubscriptionAbstract abs = null;
                ArrayList<SubscriptionAbstract> abstracts = new ArrayList<SubscriptionAbstract>();
                for (int i = 0; i < jsonArray.length(); i++) {
                   try {
                      if (!jsonArray.isNull(i)) {
                         abs = SubscriptionAbstract.parseJsonObject(jsonArray
                               .getJSONObject(i));
                         if (abs != null) {
                            abstracts.add(abs);
                         }
                      }
                   } catch (JSONException e) {
                      continue;
                   }
                }
                setList(abstracts);
             }
          } catch (JSONException e) {
 
          }
       }
    }
 
    public String getChannelName() {
       return this.channelName;
    }
 
    public SubscriptionAbstractList setChannelName(String name) {
       this.channelName = name;
       return this;
    }
 
    public String getChannelDescription() {
       return this.channelDesc;
    }
 
    public SubscriptionAbstractList setChannelDesciption(String desc) {
       this.channelDesc = desc;
       return this;
    }
 
 }
