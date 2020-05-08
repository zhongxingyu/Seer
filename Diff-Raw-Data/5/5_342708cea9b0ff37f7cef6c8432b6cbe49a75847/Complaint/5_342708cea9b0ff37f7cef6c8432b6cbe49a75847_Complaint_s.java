 package com.tobbetu.en4s.backend;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.util.Log;
 import android.widget.ImageView;
 
 import com.tobbetu.en4s.R;
 import com.tobbetu.en4s.Utils;
 import com.tobbetu.en4s.cache.Cache;
 import com.tobbetu.en4s.helpers.CommentRejectedException;
 import com.tobbetu.en4s.helpers.VoteRejectedException;
 
 @SuppressLint("SimpleDateFormat")
 public class Complaint implements Serializable {
 
     private static final long serialVersionUID = -4700299102770387240L;
 
     private String id;
     private String title;
     private Date date;
     private User reporter;
     private String category;
     private int upVote;
     private int downVote;
     private Set<String> upvoters;
     private Set<String> downvoters;
     private double latitude;
     private double longitude;
     private String address;
     private String city;
     private List<String> imageURLs = null;
     // private final List<Image> images = new ArrayList<Image>();
     private List<Comment> comments = null;
     private int comments_count;
     private String public_URL;
     private String slug_URL;
 
     public Complaint() {
     }
 
     public Complaint(String title, Date date, String address) {
         this.title = title;
         this.date = date;
         this.address = address;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public int getUpVote() {
         return upVote;
     }
 
     public void setUpVote(int upVote) {
         this.upVote = upVote;
     }
 
     public int getDownVote() {
         return downVote;
     }
 
     public void setDownVote(int downVote) {
         this.downVote = downVote;
     }
 
     public boolean alreadyUpVoted(User me) {
         // HashSet has O(1) lookup, I hope device has enough memory
         return upvoters.contains(me.getId());
     }
 
     public double getLatitude() {
         return latitude;
     }
 
     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 
     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public User getReporter() {
         return reporter;
     }
 
     public void setReporter(User reporter) {
         this.reporter = reporter;
     }
 
     public String getCategory() {
         return category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public String getPublicURL() {
         return public_URL;
     }
 
     public void setPublicURL(String url) {
         this.public_URL = "http://enforceapp.com" + url;
     }
 
     public String getSlug_URL() {
         return slug_URL;
     }
 
     public void setSlug_URL(String url) {
         this.slug_URL = "http://enforceapp.com" + url;
     }
 
     public List<Comment> getComments() throws IOException, JSONException {
         if (comments_count == 0) {
             return new LinkedList<Comment>();
         } else if (comments != null) {
             return comments;
         } else {
             comments = Comment.getComments(this);
             return comments;
         }
     }
 
     public int getCommentsCount() {
         return comments_count;
     }
 
     public void setImages(List<String> images) {
         this.imageURLs = images;
     }
 
     public void addJustUploadedImage(String url) {
         if (this.imageURLs == null)
             this.imageURLs = new ArrayList<String>();
         imageURLs.add(url);
     }
 
     public int imageCount() {
         return imageURLs.size();
     }
 
     public String getDateAsString(Context ctx) {
         // WARNING this is going to be fucking ugly
         long now = System.currentTimeMillis();
         long unixtime = this.date.getTime();
 
         if (now - unixtime < 0) {
             return "Just Now";
         } else if (now - 60 * 1000 < unixtime) { // in fucking min
             return String.format(ctx.getString(R.string.comp_sec),
                     ((now - unixtime) / 1000));
         } else if (now - 60 * 60 * 1000 < unixtime) { // fucking hour
             return String.format(ctx.getString(R.string.comp_min),
                     ((now - unixtime) / 60 / 1000));
         } else if (now - 24 * 60 * 60 * 1000 < unixtime) { // fucking day
             return String.format(ctx.getString(R.string.comp_hour),
                     ((now - unixtime) / 60 / 60 / 1000));
         } else if (now - 7 * 24 * 60 * 60 * 1000 < unixtime) { // fucking week
             return String.format(ctx.getString(R.string.comp_day),
                     ((now - unixtime) / 24 / 60 / 60 / 1000));
         } else if (now - 30 * 7 * 24 * 60 * 60 * 1000 < unixtime) {
             // fucking month
             return String.format(ctx.getString(R.string.comp_week),
                     ((now - unixtime) / 7 / 24 / 60 / 60 / 1000));
         } else if (now - 12 * 30 * 7 * 24 * 60 * 60 * 1000 < unixtime) {
             // fucking month
             return String.format(ctx.getString(R.string.comp_month),
                     ((now - unixtime) / 30 / 7 / 24 / 60 / 60 / 1000));
         } else {
             SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
             return df.format(date);
         }
     }
 
     public String getDistance(Context ctx, double lat, double lon) {
         float distance = Utils.calculateDistance(lat, lon, this.latitude,
                 this.longitude);
         if (distance < 0.0001) {
             return "Just Here!";
        } else if (distance < 10000) {
             return String.format(ctx.getString(R.string.comp_meter), distance);
         } else {
             return String.format(ctx.getString(R.string.comp_km),
                    distance / 1000);
         }
     }
 
     public void getImage(int index, String size, ImageView iv)
             throws IndexOutOfBoundsException {
         if (index > imageURLs.size())
             throw new IndexOutOfBoundsException();
 
         String url = imageURLs.get(index);
         Cache.getInstance().getImage(Image.getImageURL(url, size), iv);
     }
 
     public Complaint save() throws IOException {
         Log.d("[JSON]", this.toJSON());
         HttpResponse post = Requests.post("/complaint", this.toJSON());
         if (!Requests.checkStatusCode(post, HttpStatus.SC_CREATED)) {
             // TODO throw exception
             Log.d(getClass().getName(), "Status Code in not 201");
         }
         try {
             return fromJSON(new JSONObject(Requests.readResponse(post)));
         } catch (JSONException e) {
             Log.e(getClass().getName(), "Impossible JSONException throwed", e);
             Log.e("[WARNING]",
                     "FYI, you are going to fucked up becuse newly created complaint object is null");
             return null;
         }
     }
 
     public void upvote(User me, String location) throws IOException,
             VoteRejectedException {
         HttpResponse put = Requests.put(
                 String.format("/complaint/%s/upvote", this.id), location);
         if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_ACCEPTABLE)) {
             Log.e(getClass().getName(), "Upvote Rejected");
             throw new VoteRejectedException("Upvote Rejected");
         } else if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_FOUND)) {
             Log.e(getClass().getName(),
                     "Upvote Rejected because complaint id is wrong");
             throw new VoteRejectedException("There is no such complaint");
         }
 
         // upvote successful
         this.upVote++;
         this.upvoters.add(me.getId());
     }
 
     public void downvote(User me, String location) throws IOException,
             VoteRejectedException {
         HttpResponse put = Requests.put(
                 String.format("/complaint/%s/downvote", this.id), location);
         if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_ACCEPTABLE)) {
             Log.e(getClass().getName(), "Upvote Rejected");
             throw new VoteRejectedException("Upvote Rejected");
         } else if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_FOUND)) {
             Log.e(getClass().getName(),
                     "Upvote Rejected because complaint id is wrong");
             throw new VoteRejectedException("There is no such complaint");
         }
 
         // downvote successful
         this.downVote++;
         this.downvoters.add(me.getId());
     }
 
     public void comment(String text) throws IOException,
             CommentRejectedException, JSONException {
         JSONObject comment = new JSONObject();
         comment.put("text", text);
 
         HttpResponse put = Requests.put(String.format("/comments/%s", this.id),
                 comment.toString());
 
         if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_ACCEPTABLE)) {
             Log.e(getClass().getName(), "Comment Rejected");
             throw new CommentRejectedException("Comment Rejected");
         } else if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_FOUND)) {
             Log.e(getClass().getName(),
                     "Comment Rejected because complaint id is wrong");
             throw new CommentRejectedException("There is no such complaint");
         }
 
         // comment successful
         if (comments_count == 0 || comment == null) {
             // If it is first comment ensure everything is normal
             this.comments_count = 0;
             this.comments = new LinkedList<Comment>();
         }
         this.comments.add(Comment.fromJSON(Requests.readResponse(put)));
         this.comments_count++;
     }
 
     public String toJSON() {
         JSONObject newObj = new JSONObject();
         try {
             newObj.put("title", this.title);
             newObj.put("category", this.category);
             newObj.put("city", this.city);
             newObj.put("address", this.address);
 
             JSONArray geo = new JSONArray();
             geo.put(this.latitude);
             geo.put(this.longitude);
             newObj.put("location", geo);
         } catch (JSONException e) {
             Log.e("Complaint.toJSON", "Unexpected JSONException", e);
         }
         return newObj.toString();
     }
 
     private static Complaint fromJSON(JSONObject elem) throws JSONException {
         Complaint obj = new Complaint();
 
         obj.setId(elem.getString("_id"));
         obj.setTitle(elem.getString("title"));
         obj.setReporter(User.fromJSON(elem.getJSONObject("user")));
         obj.setCategory(elem.getString("category"));
         obj.setUpVote(elem.getInt("upvote_count"));
         obj.setDownVote(elem.getInt("downvote_count"));
         obj.setAddress(elem.getString("address"));
         obj.setCity(elem.getString("city"));
         obj.comments_count = elem.getInt("comments_count");
         obj.setPublicURL(elem.getString("public_url"));
         obj.setSlug_URL(elem.getString("slug_url"));
 
         JSONArray upvoters = elem.getJSONArray("upvoters");
         Set<String> tmp = new HashSet<String>();
         for (int i = 0; i < upvoters.length(); i++) {
             String user = upvoters.getString(i);
             tmp.add(user);
         }
         obj.upvoters = tmp;
 
         JSONArray downvoters = elem.getJSONArray("downvoters");
         tmp = new HashSet<String>();
         for (int i = 0; i < downvoters.length(); i++) {
             String user = downvoters.getString(i);
             tmp.add(user);
         }
         obj.downvoters = tmp;
 
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
         try {
             obj.setDate(df.parse(elem.getString("date")));
         } catch (ParseException e) {
             Log.e("Complaint.fromJSON",
                     "Date Parse Error: " + elem.getString("date"), e);
             obj.setDate(new Date());
         }
 
         JSONArray geo = elem.getJSONArray("location");
         obj.setLatitude(geo.getDouble(0));
         obj.setLongitude(geo.getDouble(1));
 
         if (elem.has("pics")) {
             JSONArray pics = elem.getJSONArray("pics");
             ArrayList<String> picsList = new ArrayList<String>();
             for (int i = 0; i < pics.length(); i++) {
                 picsList.add(pics.optString(i));
             }
             obj.setImages(picsList);
         }
 
         return obj;
     }
 
     private static List<Complaint> parseList(String jsonResponse)
             throws JSONException {
         List<Complaint> list = new LinkedList<Complaint>();
 
         JSONArray results = new JSONArray(jsonResponse);
         for (int i = 0; i < results.length(); i++) {
             JSONObject item = results.getJSONObject(i);
             list.add(Complaint.fromJSON(item));
         }
         return list;
     }
 
     public static List<Complaint> getList(String url) throws IOException,
             JSONException {
         // TODO not forget to change that
         HttpResponse get = Requests.get(url);
 
         if (!Requests.checkStatusCode(get, HttpStatus.SC_OK))
             Log.e("Complaint.getList", "[ERROR] Status Code: "
                     + get.getStatusLine().getStatusCode());
         String response = Requests.readResponse(get);
         return Complaint.parseList(response);
     }
 
     public static List<Complaint> getHotList() throws IOException,
             JSONException {
         return Complaint.getList("/complaint/hot");
 
     }
 
     public static List<Complaint> getNewList() throws IOException,
             JSONException {
         return Complaint.getList("/complaint/recent");
     }
 
     public static List<Complaint> getTopList() throws IOException,
             JSONException {
         return Complaint.getList("/complaint/top");
     }
 
     public static List<Complaint> getNearList(double lat, double lon)
             throws IOException, JSONException {
         return Complaint.getList(String.format(
                 "/complaint/near?latitude=%s&longitude=%s",
                 Double.toString(lat), Double.toString(lon)));
     }
 }
