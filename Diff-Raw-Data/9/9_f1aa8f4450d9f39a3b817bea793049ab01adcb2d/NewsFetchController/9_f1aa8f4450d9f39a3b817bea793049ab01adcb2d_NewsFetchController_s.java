 package com.gemdigger.server.controller.task;
 
 import com.gemdigger.server.dao.NewsDao;
 import com.gemdigger.server.model.Action;
 import com.gemdigger.server.model.NewsAction;
 import com.gemdigger.server.model.NewsBody;
 import com.gravity.goose.Article;
 import com.gravity.goose.Goose;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @Controller
 public class NewsFetchController {
 
     public static final Logger log = Logger.getLogger(NewsFetchController.class.getName());
 
     @Autowired
     Goose goose;
 
     @Autowired
     NewsDao newsDao;
 
 	@RequestMapping("/task/fetch")
 	public @ResponseBody FetchStatus fetchArticle(@RequestParam Long actionId) {
         FetchStatus status = FetchStatus.FAIL;
 
         try {
             NewsAction newsAction = newsDao.getActionById(actionId);
 
             log.info("Fetching an article from "  + newsAction.getUrl() + "...");
 
             Article article = goose.extractContent(newsAction.getUrl());
 
             NewsBody body = newsDao.getBodyByUrl(article.canonicalLink());
 
             if (body == null) {
                 body = new NewsBody();
                 body.setBody(article.cleanedArticleText());
                 body.setTags(article.getTagsSet());
                 body.setTitle(article.title());
                 body.setRawHtml(article.rawHtml());
                body.setUrl(article.canonicalLink());
 
                 newsDao.saveNewsBody(body);
                 log.info("Created news body record for "  + newsAction.getUrl() + " with canonical url: " + article.canonicalLink() );
             } else {
                 log.info("News body with this url already exists");
                 //TODO: running the checksum and updating the body
             }
 
             newsAction.setCanonicalUrl(body.getUrl());
             newsDao.saveAction(newsAction);
             log.info("Updated news action");
 
             status = FetchStatus.SUCCESS;
         } catch (Throwable ex) {
             log.log(Level.SEVERE, ex.getMessage(), ex);
         } finally {
             return status;
         }
     }
 
 }
