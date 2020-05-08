 
 package bullshit_paper;
 
 import java.util.Date;
 import java.util.List;
 
public class OnetArticle implements IArticle{
     private final String title;
     private final String content;
     private final Date date;
     private final List<IComment> comments;
     
 
    public OnetArticle(String title, String content, Date date, List<IComment> comments) {
         this.title = title;
         this.content = content;
         this.date = date;
         this.comments = comments;
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public String getContent() {
         return content;
     }
 
     @Override
     public Date getDate() {
         return date;
     }
 
     @Override
     public List<IComment> getComments() {
         return comments;
     }
     
 }
