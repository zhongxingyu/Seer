 package com.masterofcode.android._10ideas.objects;
 
 import org.json.JSONObject;
 
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: boss1088
  * Date: 5/26/12
  * Time: 12:41 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Idea {
 
     private String id;
     private String created_at;
     private String updated_at;
     private String essential;
     private boolean isPublic;
     private String user_id;
     private Integer votes;
 
     public Idea() {}
 
     public Idea(JSONObject json) {
         setId(json.optString("_id"));
         setCreated_at(json.optString("created_at"));
         setUpdated_at(json.optString("updated_at"));
         setEssential(json.optString("essential"));
        setPublic(json.optBoolean("isPublic"));
         setUser_id(json.optString("user_id"));
         setVotes(json.optInt("votes"));
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getCreated_at() {
         return created_at;
     }
 
     public void setCreated_at(String created_at) {
         this.created_at = created_at;
     }
 
     public String getUpdated_at() {
         return updated_at;
     }
 
     public void setUpdated_at(String updated_at) {
         this.updated_at = updated_at;
     }
 
     public String getEssential() {
         return essential;
     }
 
     public void setEssential(String essential) {
         this.essential = essential;
     }
 
     public String getUser_id() {
         return user_id;
     }
 
     public void setUser_id(String user_id) {
         this.user_id = user_id;
     }
 
     public Integer getVotes() {
         return votes;
     }
 
     public void setVotes(Integer votes) {
         this.votes = votes;
     }
 
     public boolean isPublic() {
         return isPublic;
     }
 
     public void setPublic(boolean aPublic) {
         isPublic = aPublic;
     }
 }
