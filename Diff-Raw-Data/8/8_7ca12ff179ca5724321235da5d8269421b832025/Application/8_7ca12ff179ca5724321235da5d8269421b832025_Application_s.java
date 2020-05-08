 package controllers;
 
 import models.*;
 import play.Play;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 import java.util.List;
 
 public class Application extends Controller {
 
     @Before
     static void addDefaults(){
         renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
     }
 
     public static void index() {
         List<Article> homePagePost = Article.find("approved = true order by submit_date desc").fetch(1);
 
         Company homeCompany = Company.getHomeCompany();
         List<Article> topViewed = getTopViewedArticles();
         List<CubeReview> cubeReviewsList = CubeReview.find("order by created_on desc").fetch();
         Company.findTopRated();
         render(homePagePost, homeCompany, topViewed, cubeReviewsList);
     }
 
     public static List<Article> getTopViewedArticles() {
         return Article.find("approved = true order by article_view desc").fetch();
     }
 
     public static void show(Long id) {
         Article article = Article.findById(id);
         render(article);
     }
 
     public static void about(){
         render();
     }
 
     public static void feedback(){
         render();
     }
     
     public static void sendEmail(){
         CubeMail.feedback(params.get("writingAbout"), params.get("contactSubject"), params.get("userName"), params.get("userEmail"), params.get("userMessage"));
         redirect("/feedback");
     }
 
 }
