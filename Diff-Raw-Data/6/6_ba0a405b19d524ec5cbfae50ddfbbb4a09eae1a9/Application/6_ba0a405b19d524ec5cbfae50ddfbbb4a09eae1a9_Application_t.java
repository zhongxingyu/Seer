 package controllers;
 
 import play.*;
 import play.mvc.*;
 import java.util.*;
 import models.*;
 
 public class Application extends Controller {
     public static void index() {
         String userName = "";
        int authority = 0;
         if(session.get("userId") != null) {
             String userId = session.get("userId");
             User user = User.find("byUserid",userId).first();
             userName = user.name;
            authority = user.authority;
         }
     	List<Upload> olderUploads = Upload.find("order by postedAt desc").from(0).fetch(3);
     	List<News> latestNews = News.findAll();
     	Report report = Report.find("order by id desc").first();
    	render(olderUploads,latestNews,report,userName, authority);
     }
 
     public static void about() {
     	render();
     }
 }
