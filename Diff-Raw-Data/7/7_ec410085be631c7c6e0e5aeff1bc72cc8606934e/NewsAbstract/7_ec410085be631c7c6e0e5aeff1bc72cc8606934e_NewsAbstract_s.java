 package com.osastudio.newshub.data;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.osastudio.newshub.data.base.NewsBaseTopicAbstract;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class NewsAbstract extends NewsBaseTopicAbstract implements Parcelable {
 
    public static final int NEWS_TYPE = 0;
    public static final int EXAM_TYPE = 1;
    
    public static final String JSON_KEY_ID = "lesson_id";
    public static final String JSON_KEY_COLOR = "lesson_colour";
    public static final String JSON_KEY_PUBLISHED_TIME = "post_date";
    public static final String JSON_KEY_AUTHOR = "expert_name";
    public static final String JSON_KEY_TITLE = "lesson_title";
    public static final String JSON_KEY_TYPE = "lesson_class";
 
    private int type = 0;
    
    public NewsAbstract() {
       super();
    }
 
    public NewsAbstract(Parcel src) {
       super(src);
    }
 
    public int getType() {
       return this.type;
    }
 
    public void setType(int type) {
       this.type = type;
    }
 
    public String getChannelId() {
       return getTopicId();
    }
 
    public NewsAbstract setChannelId(String channelId) {
       setTopicId(channelId);
       return this;
    }
 
    public static NewsAbstract parseJsonObject(JSONObject jsonObject) {
       NewsAbstract result = new NewsAbstract();
       try {
          if (!jsonObject.isNull(JSON_KEY_ID)) {
             result.setId(jsonObject.getString(JSON_KEY_ID).trim());
          }
          if (!jsonObject.isNull(JSON_KEY_COLOR)) {
             result.setColor(NewsAbstract.parseColorValue(jsonObject.getString(
                   JSON_KEY_COLOR).trim()));
          }
          if (!jsonObject.isNull(JSON_KEY_PUBLISHED_TIME)) {
             result.setPublishedTime(jsonObject.getString(
                   JSON_KEY_PUBLISHED_TIME).trim());
          }
          if (!jsonObject.isNull(JSON_KEY_AUTHOR)) {
             result.setAuthor(jsonObject.getString(JSON_KEY_AUTHOR).trim());
          }
          if (!jsonObject.isNull(JSON_KEY_TITLE)) {
             result.setTitle(jsonObject.getString(JSON_KEY_TITLE).trim());
          }
       } catch (JSONException e) {
 
       }
       return result;
    }
 
    @Override
    public int describeContents() {
       return 0;
    }
 
    @Override
    public void writeToParcel(Parcel dst, int flags) {
       super.writeToParcel(dst, flags);
    }
 
    public static final Parcelable.Creator<NewsAbstract> CREATOR = new Creator<NewsAbstract>() {
       @Override
       public NewsAbstract createFromParcel(Parcel src) {
          return new NewsAbstract(src);
       }
 
       @Override
       public NewsAbstract[] newArray(int size) {
          return new NewsAbstract[size];
       }
    };
 
 }
