 package pojo;
 
 import java.sql.Timestamp;
 
 /**
  * User: yumingzhe
  * Date: 5/19/12
  * Time: 9:36 PM
  */
 
 public class Blog {
     private Integer id;
     private Integer posterId;
     private String title;
     private String description;
     private String content;
     private Timestamp postDate;
     private String tag;
     private String access;
     private Boolean comment;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
 
     public Integer getPosterId() {
         return posterId;
     }
 
     public void setPosterId(Integer posterId) {
         this.posterId = posterId;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public Timestamp getPostDate() {
         return postDate;
     }
 
     public void setPostDate(Timestamp postDate) {
         this.postDate = postDate;
     }
 
     public String getTag() {
         return tag;
     }
 
     public void setTag(String tag) {
         this.tag = tag;
     }
 
     public String getAccess() {
         return access;
     }
 
     public void setAccess(String access) {
         this.access = access;
     }
 
     public Boolean getComment() {
         return comment;
     }
 
     public void setComment(Boolean comment) {
         this.comment = comment;
     }
 }
