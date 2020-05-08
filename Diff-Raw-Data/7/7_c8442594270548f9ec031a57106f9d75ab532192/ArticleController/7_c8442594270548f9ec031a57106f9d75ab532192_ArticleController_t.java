 package com.dummy.controllers;
 
 import com.dummy.models.Article;
import com.dummy.models.Author;
 import com.dummy.services.MailService;
 import com.expressmvc.annotation.Path;
 import com.expressmvc.annotation.http.GET;
 import com.expressmvc.annotation.http.POST;
 import com.expressmvc.model.ModelContainer;
import com.thoughtworks.Model;
import com.thoughtworks.query.QueryList;
 
 import java.util.List;
 
 @Path("/article")
 public class ArticleController {
     private final MailService mailService;
 
     public ArticleController(MailService mailService) {
         this.mailService = mailService;
     }
 
     @GET
     @Path("/")
     public ModelContainer show() {
         return ModelContainer.initWith();
     }
 
     @GET
     @Path("/display")
     public ModelContainer display() {
        QueryList<Article> articles = Article.find_all().includes(Author.class);
         return ModelContainer.initWith(articles);
     }
 
     @POST
     @Path("/create")
     public ModelContainer create(Article article) {
 
         //fake business logic
         article.setUrl("http://www.example.com/2013/" + article.getAuthorId() + "/" + article.getTitle());
         article.save();
         if (article.getAuthor() != null && article.getAuthor().getEmail() != null) {
             article.getAuthor().save();
         }
 
         //using injected service
         mailService.sendMail("a@b.com", article.toString());
 
         return ModelContainer.initWith(article);
     }
 
 }
