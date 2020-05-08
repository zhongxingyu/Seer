 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.preppa.web.pages.contribution.article;
 
 import com.preppa.web.data.ArticleDAO;
 import com.preppa.web.entities.Article;
 import com.preppa.web.entities.Tag;
 import com.preppa.web.entities.User;
 import java.util.List;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 /**
  *
  * @author newtonik
  */
 public class ShowArticle {
 @Property
 private Article article;
 @Inject
 private ArticleDAO articleDAO;
 @Property
 private User author;
 @Property
 private String authorname;
 @Property
 private List<Tag> tags;
 
 void onActivate(int id) {
             this.article = articleDAO.findById(id);
             this.author = article.getUser();
             this.tags = article.getTaglist();
             
             if(author != null) {
                 authorname = author.getUsername();
             }
             if(authorname == null)
             {
                 authorname = "unknown dude";
             }
             
  }
 
  public void set(String title) {
         this.article = articleDAO.findByTitle(title);
         this.author = article.getUser();
         if(author != null) {
             authorname = author.getUsername();
         }
         if(authorname == null)
         {
             authorname = "unknown dude";
         }
 
  }
 

  public void setarticle(Article article) {
         this.article = articleDAO.findById(article.getId());
 
         this.article = article;
         this.author = article.getUser();
             if(author != null) {
             authorname = author.getUsername();
         }
     
          if(authorname == null)
          {
             authorname = "unknown dude";
         }
 
     }
 
 }
