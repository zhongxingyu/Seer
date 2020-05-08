 package com.teamboid.twitterapi.search;
 
 import com.teamboid.twitterapi.status.Place;
 import com.teamboid.twitterapi.status.PlaceJSON;
 import com.teamboid.twitterapi.status.entity.hashtag.HashtagEntity;
 import com.teamboid.twitterapi.status.entity.hashtag.HashtagEntityJSON;
 import com.teamboid.twitterapi.status.entity.media.MediaEntity;
 import com.teamboid.twitterapi.status.entity.media.MediaEntityJSON;
 import com.teamboid.twitterapi.status.entity.mention.MentionEntity;
 import com.teamboid.twitterapi.status.entity.mention.MentionEntityJSON;
 import com.teamboid.twitterapi.status.entity.url.UrlEntity;
 import com.teamboid.twitterapi.status.entity.url.UrlEntityJSON;
 import com.teamboid.twitterapi.status.GeoLocation;
 import com.teamboid.twitterapi.utilities.Time;
 import com.teamboid.twitterapi.json.JSONArray;
 import com.teamboid.twitterapi.json.JSONObject;
 import com.teamboid.twitterapi.utilities.Utils;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 /**
  * Handles parsing JSON and associated values into a {@link Tweet} instance.
  * @author Aidan Follestad
  */
 class TweetJSON implements Tweet, Serializable {
 
 	private static final long serialVersionUID = -925081287752007939L;
 
 	public TweetJSON(JSONObject json) throws Exception {
         _createdAt = Time.getTwitterSearchDate(json.getString("created_at"));
         if (!json.isNull("entities")) {
             JSONObject entities = json.getJSONObject("entities");
            JSONArray urls = entities.optJSONArray("urls");
            if (urls != null) {
                 ArrayList<UrlEntity> toSet = new ArrayList<UrlEntity>();
                 for (int i = 0; i < urls.length(); i++) {
                     toSet.add(new UrlEntityJSON(urls.getJSONObject(i)));
                 }
                 _urlEntities = toSet.toArray(new UrlEntity[0]);
             }
            JSONArray hashtags = entities.optJSONArray("hashtags");
            if (hashtags != null) {
                 ArrayList<HashtagEntity> toSet = new ArrayList<HashtagEntity>();
                for (int i = 0; i < urls.length(); i++) {
                    toSet.add(new HashtagEntityJSON(urls.getJSONObject(i)));
                 }
                 _hashtagEntities = toSet.toArray(new HashtagEntity[0]);
             }
            JSONArray mentions = entities.optJSONArray("user_mentions");
            if (mentions != null) {
                 ArrayList<MentionEntity> toSet = new ArrayList<MentionEntity>();
                for (int i = 0; i < urls.length(); i++) {
                    toSet.add(new MentionEntityJSON(urls.getJSONObject(i)));
                 }
                 _mentionEntities = toSet.toArray(new MentionEntity[0]);
             }
            JSONArray media = entities.optJSONArray("media");
            if (media != null) {
                 ArrayList<MediaEntity> toSet = new ArrayList<MediaEntity>();
                for (int i = 0; i < urls.length(); i++) {
                    toSet.add(new MediaEntityJSON(urls.getJSONObject(i)));
                 }
                 _mediaEntities = toSet.toArray(new MediaEntity[0]);
             }
         }
         _fromUser = json.getString("from_user");
         _fromUserId = json.getLong("from_user_id");
         if(!json.isNull("geo")) {
             _geo = new GeoLocation(json.getJSONObject("geo"));
         }
         if(!json.isNull("place")) {
             _place = new PlaceJSON(json.getJSONObject("place"));
         }
         _id = json.getLong("id");
         _langCode = json.getString("iso_language_code");
         if(!json.isNull("metadata")) {
             JSONObject metadata = json.getJSONObject("metadata");
             if(!metadata.isNull("recent_retweets")) {
                 _recentRetweets = metadata.getInt("recent_retweets");
             }
             _resultType = SearchQuery.ResultType.valueOf(metadata.getString("result_type").toUpperCase());
         }
         _profileImageUrl = Utils.unescape(json.getString("profile_image_url"));
         _source = Utils.unescape(json.getString("source"));
         _sourcePlain = Utils.stripAnchor(_source);
         if(!json.isNull("text")) {
             _text = Utils.unescape(json.getString("text"));
         }
         if(!json.isNull("to_user_id")) {
             _toUserId = json.getLong("to_user_id");
         }
     }
 
     private Calendar _createdAt;
     private String _profileImageUrl;
     private long _fromUserId;
     private String _fromUser;
     private String _text;
     private long _toUserId;
     private long _id;
     private GeoLocation _geo;
     private Place _place;
     private String _langCode;
     private String _source;
     private String _sourcePlain;
     private int _recentRetweets;
     private SearchQuery.ResultType _resultType;
     private UrlEntity[] _urlEntities;
     private MediaEntity[] _mediaEntities;
     private HashtagEntity[] _hashtagEntities;
     private MentionEntity[] _mentionEntities;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Calendar getCreatedAt() { return _createdAt; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getProfileImageUrl() { return _profileImageUrl; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long getFromUserId() { return _fromUserId; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getFromUser() { return _fromUser; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getText() { return _text; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long getToUserId() { return _toUserId; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long getId() { return _id; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public GeoLocation getGeo() { return _geo; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getIsoLanguageCode() { return _langCode; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSource() { return _source; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSourcePlain() { return _sourcePlain; }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getRecentRetweets() { return _recentRetweets; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SearchQuery.ResultType getResultType() { return _resultType; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UrlEntity[] getUrlEntities() { return _urlEntities; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public MediaEntity[] getMediaEntities() { return _mediaEntities; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public HashtagEntity[] getHashtagEntities() { return  _hashtagEntities; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public MentionEntity[] getMentionEntities() { return _mentionEntities; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Place getPlace() { return _place; }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public GeoLocation getGeoLocation() { return _geo; }
 
 
     public static Tweet[] createStatusList(JSONArray array) throws Exception {
         ArrayList<Tweet> toReturn = new ArrayList<Tweet>();
         for(int i = 0; i < array.length(); i++) {
             toReturn.add(new TweetJSON(array.getJSONObject(i)));
         }
         return toReturn.toArray(new Tweet[0]);
     }
 }
