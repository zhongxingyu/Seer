 package com.forrst.api;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.forrst.api.model.Auth;
 import com.forrst.api.model.Comment;
 import com.forrst.api.model.Photo;
 import com.forrst.api.model.Post;
 import com.forrst.api.model.Snap;
 import com.forrst.api.model.Stat;
 import com.forrst.api.model.User;
 import com.forrst.api.util.ForrstAuthenticationException;
 
 public class ForrstAPIClient implements ForrstAPI {
 
 	private static HttpRequest http;
 	
 	public ForrstAPIClient() {
 		http = new HttpRequest();
 	}
 
 	public Stat stats() {
 	    Stat stat = null;
 	    
 	    try {
 	        JSONObject json = http.get(Endpoint.getInstance().STATS_URI);
 	        
 	        stat = new Stat();
 	        stat.setRateLimit(json.getInt("rate_limit"));
 	        stat.setCallsMade(json.getString("calls_made"));
 	    } catch (JSONException e) {
             throw new RuntimeException("Error fetching stats from Forrst", e);
         }
 
 		return stat;
 	}
 
 	public JSONObject notifications(String accessToken, Map<String,String> options) {
 		Map<String,String> params = new HashMap<String,String>();
 		params.put("access_token", accessToken);
 		
 		if(options != null) {
 			if(options.containsKey("grouped")) {
 				params.put("grouped", options.get("grouped"));
 			}
 		}
 		
 		return http.get(Endpoint.getInstance().NOTIFICATIONS_URI, params);
 	}
 	
 	public Auth usersAuth(String emailOrUsername, String password) throws ForrstAuthenticationException {
 	    Auth auth = null;
 
 		try {
 		    Map<String,String> params = new HashMap<String,String>();
 	        params.put("email_or_username", emailOrUsername);
 	        params.put("password", password);
 	        
 	        JSONObject json = http.post(Endpoint.getInstance().USERS_AUTH_URI, params);
 
 	        auth = new Auth();
 	        auth.setAccessToken(json.getString("token"));
 	        auth.setUserId(json.getInt("user_id"));
         } catch (JSONException e) {
             auth = null;
             throw new RuntimeException("Error authenticating with Forrst", e);
         }
 		
 		return auth;
 	}
 
 	public User usersInfo(Map<String,String> userInfo) {
 		User user = null;
 		Photo photo = null;
 		JSONObject json = null;
 		
 		try {
 		    if(userInfo.containsKey("id") || userInfo.containsKey("username"))
 	            json = http.get(Endpoint.getInstance().USERS_INFO_URI, userInfo);
 		    else
 		        return user;
 
 		    JSONObject photoJson = json.getJSONObject("photos");
 		    
             photo = new Photo();
             user = new User();
             
             photo.setXlUrl(photoJson.getString("xl_url"));
             photo.setLargeUrl(photoJson.getString("large_url"));
             photo.setMediumUrl(photoJson.getString("medium_url"));
             photo.setSmallUrl(photoJson.getString("small_url"));
             photo.setThumbUrl(photoJson.getString("thumb_url"));
             
             user.setId(json.getInt("id"));
             user.setName(json.getString("name"));
             user.setUsername(json.getString("username"));
             user.setUrl(json.getString("url"));
             user.setPosts(json.getInt("posts"));
             user.setLikes(json.getInt("likes"));
             user.setComments(json.getInt("comments"));
             user.setFollowers(json.getInt("followers"));
             user.setFollowing(json.getInt("following"));
             user.setPhoto(photo);
             user.setBio(json.getString("bio"));
             user.setIsA(json.getString("is_a"));
             user.setHomepageUrl(json.getString("homepage_url"));
             user.setTwitter(json.getString("twitter"));
             user.setInDirectory(json.getBoolean("in_directory"));
             user.setTagString(json.getString("tag_string"));
         } catch (JSONException e) {
             user = null;
             throw new RuntimeException("Error fetching user data", e);
         }
 
 		return user;
 	}
 
 	public List<Post> userPosts(Map<String,String> userInfo, Map<String,String> options) {
	    List<Post> posts = null;
 	    
 		Map<String,String> params = new HashMap<String,String>();
 		if(userInfo.containsKey("id"))
 		    params.put("id", userInfo.get("id"));
 		if(userInfo.containsKey("username"))
             params.put("username", userInfo.get("username"));
 		if(options != null) {
 			if(options.containsKey("type"))
 				params.put("type", options.get("type"));
 			if(options.containsKey("limit"))
 				params.put("limit", options.get("limit"));
 			if(options.containsKey("after"))
 				params.put("after", options.get("after"));
 		}
 		
 		try {
 	        JSONObject json = http.get(Endpoint.getInstance().USER_POSTS_URI, params);
             JSONArray postsJsonArray = (JSONArray) json.get("posts");
            posts = new ArrayList<Post>();
             
             for(int i = 0; i < postsJsonArray.length(); i++) {
                 Post post = new Post();
                 Photo photo = new Photo();
                 User user = new User();
                 Snap snap = new Snap();
                 
                 JSONObject postJson = (JSONObject) postsJsonArray.get(i);
                 JSONObject userJson = postJson.getJSONObject("user");
                 JSONObject photoJson = userJson.getJSONObject("photos"); 
                 
                 photo.setXlUrl(photoJson.getString("xl_url"));
                 photo.setLargeUrl(photoJson.getString("large_url"));
                 photo.setMediumUrl(photoJson.getString("medium_url"));
                 photo.setSmallUrl(photoJson.getString("small_url"));
                 photo.setThumbUrl(photoJson.getString("thumb_url"));
                 
                 user.setId(userJson.getInt("id"));
                 user.setName(userJson.getString("name"));
                 user.setUsername(userJson.getString("username"));
                 user.setUrl(userJson.getString("url"));
                 user.setPosts(userJson.getInt("posts"));
                 user.setLikes(userJson.getInt("likes"));
                 user.setComments(userJson.getInt("comments"));
                 user.setFollowers(userJson.getInt("followers"));
                 user.setFollowing(userJson.getInt("following"));
                 user.setPhoto(photo);
                 user.setBio(userJson.getString("bio"));
                 user.setIsA(userJson.getString("is_a"));
                 user.setHomepageUrl(userJson.getString("homepage_url"));
                 user.setTwitter(userJson.getString("twitter"));
                 user.setInDirectory(userJson.getBoolean("in_directory"));
                 user.setTagString(userJson.getString("tag_string"));                
                 if(json.has("snaps")) {
                     JSONObject snapJson = postJson.getJSONObject("snaps");
                     snap.setKeithUrl(snapJson.getString("keith_url"));
                     snap.setLargeUrl(snapJson.getString("large_url"));
                     snap.setMediumUrl(snapJson.getString("medium_url"));
                     snap.setMegaUrl(snapJson.getString("mega_url"));
                     snap.setOriginalUrl(snapJson.getString("original_url"));
                     snap.setSmallUrl(snapJson.getString("small_url"));
                     snap.setThumbUrl(snapJson.getString("thumb_url"));
                     post.setSnap(snap);
                 }
                 post.setId(postJson.getInt("id"));
                 post.setTinyId(postJson.getString("tiny_id"));
                 post.setPostType(postJson.getString("post_type"));
                 post.setPostUrl(postJson.getString("post_url"));
                 post.setCreatedAt(postJson.getString("created_at"));
                 post.setUpdatedAt(postJson.getString("updated_at"));
                 post.setUser(user);
                 post.setPublished(postJson.getBoolean("published"));
                 post.setPublic(postJson.getBoolean("public"));
                 post.setTitle(postJson.getString("title"));
                 post.setUrl(postJson.getString("url"));
                 post.setContent(postJson.getString("content"));
                 post.setDescription(postJson.getString("description"));
                 post.setViewCount(postJson.getInt("view_count"));
                 post.setLikeCount(postJson.getInt("like_count"));
                 post.setCommentCount(postJson.getInt("comment_count"));
                 post.setTagString(postJson.getString("tag_string"));
                 
                 posts.add(post);
             }
         } catch (JSONException e) {
             throw new RuntimeException("Error fetching users posts", e);
         }
 		
 		return posts;
 	}
 
 	public Post postsShow(int id) {
 	    Post post = null;
 	    Photo photo = null;
 	    User user = null;
 	    Snap snap = null;
 	    JSONObject json = null;
 	    
 	    try {
 	        Map<String,String> params = new HashMap<String,String>();
 	        params.put("id", Integer.toString(id));
 	        
 	        json = http.get(Endpoint.getInstance().POSTS_SHOW_URI, params);
 	        
 	        JSONObject userJson = json.getJSONObject("user");
 	        JSONObject photoJson = userJson.getJSONObject("photos");
 
             photo = new Photo();
             user = new User();
             snap = new Snap();
             post = new Post();
             
             photo.setXlUrl(photoJson.getString("xl_url"));
             photo.setLargeUrl(photoJson.getString("large_url"));
             photo.setMediumUrl(photoJson.getString("medium_url"));
             photo.setSmallUrl(photoJson.getString("small_url"));
             photo.setThumbUrl(photoJson.getString("thumb_url"));
             
             user.setId(userJson.getInt("id"));
             user.setName(userJson.getString("name"));
             user.setUsername(userJson.getString("username"));
             user.setUrl(userJson.getString("url"));
             user.setPosts(userJson.getInt("posts"));
             user.setLikes(userJson.getInt("likes"));
             user.setComments(userJson.getInt("comments"));
             user.setFollowers(userJson.getInt("followers"));
             user.setFollowing(userJson.getInt("following"));
             user.setPhoto(photo);
             user.setBio(userJson.getString("bio"));
             user.setIsA(userJson.getString("is_a"));
             user.setHomepageUrl(userJson.getString("homepage_url"));
             user.setTwitter(userJson.getString("twitter"));
             user.setInDirectory(userJson.getBoolean("in_directory"));
             user.setTagString(userJson.getString("tag_string"));
             
             if(json.has("snaps")) {
                 JSONObject snapJson = json.getJSONObject("snaps");
                 snap.setKeithUrl(snapJson.getString("keith_url"));
                 snap.setLargeUrl(snapJson.getString("large_url"));
                 snap.setMediumUrl(snapJson.getString("medium_url"));
                 snap.setMegaUrl(snapJson.getString("mega_url"));
                 snap.setOriginalUrl(snapJson.getString("original_url"));
                 snap.setSmallUrl(snapJson.getString("small_url"));
                 snap.setThumbUrl(snapJson.getString("thumb_url"));
                 post.setSnap(snap);
             }
             
             post.setId(json.getInt("id"));
             post.setTinyId(json.getString("tiny_id"));
             post.setPostType(json.getString("post_type"));
             post.setPostUrl(json.getString("post_url"));
             post.setCreatedAt(json.getString("created_at"));
             post.setUpdatedAt(json.getString("updated_at"));
             post.setUser(user);
             post.setPublished(json.getBoolean("published"));
             post.setPublic(json.getBoolean("public"));
             post.setTitle(json.getString("title"));
             post.setUrl(json.getString("url"));
             post.setContent(json.getString("content"));
             post.setDescription(json.getString("description"));
             post.setViewCount(json.getInt("view_count"));
             post.setLikeCount(json.getInt("like_count"));
             post.setCommentCount(json.getInt("comment_count"));
             post.setTagString(json.getString("tag_string"));
             
 	    } catch (JSONException e) {
             throw new RuntimeException("Error fetching post", e);
         }
 		
 		return post;
 	}
 
 	public JSONObject postsAll(Map<String,String> options) {
 		return http.get(Endpoint.getInstance().POSTS_ALL_URI, options);
 	}
 
 	public JSONObject postsList(String postType, Map<String,String> options) {
 		Map<String,String> params = new HashMap<String,String>();
 		params.put("post_type", postType);
 		
 		if(options != null) {
 			if(options.containsKey("sort")) {
 				params.put("sort", options.get("sort"));
 			}
 			if(options.containsKey("page")) {
 				params.put("page", options.get("page"));
 			}
 		}
 		
 		return http.get(Endpoint.getInstance().POSTS_LIST_URI, params);
 	}
 
 	public List<Comment> postComments(String accessToken, int id) {
 	    List<Comment> comments = null;
 
 		try {
 		    Map<String,String> params = new HashMap<String,String>();
 	        params.put("access_token", accessToken);
 	        params.put("id", Integer.toString(id));
 		    
 		    JSONObject json = http.get(Endpoint.getInstance().POST_COMMENTS_URI, params);
 		    JSONArray commentsJSONArray = (JSONArray) json.get("comments");
 		    
 		    comments = new ArrayList<Comment>();
 		    
 		    for(int commentCount = 0; commentCount < commentsJSONArray.length(); commentCount++) {
                 JSONObject commentJSON = commentsJSONArray.getJSONObject(commentCount);
                 
                 Comment comment = new Comment();
                 comment.setId(commentJSON.getInt("id"));
                 comment.setUserName(commentJSON.getJSONObject("user").getString("name"));
                 comment.setBody(commentJSON.getString("body"));
                 comment.setCreatedAt(Timestamp.valueOf(commentJSON.getString("created_at")));
                 comment.setUserIconUrl(commentJSON.getJSONObject("user").getJSONObject("photos").getString("thumb_url"));
                 comments.add(comment);
                 
                 if (commentJSON.has("replies")) {
                     JSONArray repliesJSONArray = (JSONArray) commentJSON.get("replies");
                     for(int replyCount = 0; replyCount < repliesJSONArray.length(); replyCount++) {
                         JSONObject replyJSON = repliesJSONArray.getJSONObject(replyCount);
 
                         Comment replyComment = new Comment();
                         replyComment.setId(replyJSON.getInt("id"));
                         replyComment.setUserName(replyJSON.getJSONObject("user").getString("name"));
                         replyComment.setBody(replyJSON.getString("body"));
                         replyComment.setCreatedAt(Timestamp.valueOf(replyJSON.getString("created_at")));
                         replyComment.setUserIconUrl(replyJSON.getJSONObject("user").getJSONObject("photos").getString("thumb_url"));
                         comments.add(replyComment);
                     }
                 }
             }
         } catch (JSONException e) {
             comments = null;
             throw new RuntimeException("Error fetching comments from Forrst", e);
         }
 
 		return comments;
 	}
 	
 	public Map<String,String> getEndpointsURIs() {
 		Map<String,String> endpoints = new HashMap<String,String>();
 		
 		endpoints.put("stats", Endpoint.getInstance().STATS_URI);
 		endpoints.put("users/auth", Endpoint.getInstance().USERS_AUTH_URI);
 		endpoints.put("users/info", Endpoint.getInstance().USERS_INFO_URI);
 		endpoints.put("user/posts", Endpoint.getInstance().USER_POSTS_URI);
 		endpoints.put("posts/show", Endpoint.getInstance().POSTS_SHOW_URI);
 		endpoints.put("posts/all", Endpoint.getInstance().POSTS_ALL_URI);
 		endpoints.put("posts/list", Endpoint.getInstance().POSTS_LIST_URI);
 		endpoints.put("post/comments", Endpoint.getInstance().POST_COMMENTS_URI);
 		
 		return endpoints;
 	}
 }
