 package org.lisak.pguide.model;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import org.lisak.pguide.exceptions.NoSuchEntity;
 
 import javax.security.auth.Subject;
 
 import java.util.Comparator;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lisak
  * Date: 06.08.13
  * Time: 17:12
  */
 @EntitySubclass(index = true)
 public class Article extends Content {
     @Index String title;
     String text;
 
     public Article() {
         super();
     }
 
     public Article(String id, String title, String text) {
         super(id);
         this.title = title;
         this.text = text;
     }
 
     public String getText() {
         return text;
     }
 
     public String getFormattedText() {
         //returns formatted text, ie. paragraphs enclosed in <div>
         String adjusted = text.replaceAll("(?m)^[ \t]*\r?\n", "</div>\n<div>");
 
         return "<div>" + adjusted + "</div>";
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public static Comparator<Article> TitleComparator = new Comparator<Article>() {
 
         @Override
         public int compare(Article article, Article article2) {
             return article.getTitle().compareTo(article2.getTitle());
         }
 
 
     };
 }
