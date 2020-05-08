 package org.guiceae.main.repositories;
 
 import com.google.appengine.api.users.UserServiceFactory;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 import org.guiceae.main.model.Article;
 import org.guiceae.main.model.ArticleState;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: Igor Petruk
  * Date: 29.06.12
  * Time: 21:49
  */
 public class ArticleRepository {
     static {
         ObjectifyService.register(Article.class);
     }
 
     @Inject
     Provider<Objectify> ofy;
 
     @Inject
     SearchRepository searchRepository;
 
     public void publish(Long id) {
         Article article = getArticle(id);
         article.setState(ArticleState.PUBLISHED);
         article.setAuthor(UserServiceFactory.getUserService().getCurrentUser().getNickname());
         article.setCreated(new Date());
         article.setLastUpdated(new Date());
         searchRepository.submitToSearch(article);
         ofy.get().put(article);
     }
 
     public Article getArticle(Long id) {
         return ofy.get().get(Article.class, id);
     }
 
     public boolean permalinkExists(String permalink) {
         List<Article> strings = ofy.get().query(Article.class).filter("permalink", permalink).list();
         if (strings.isEmpty()) {
             return false;
         } else if (strings.size() == 1) {
             return true;
         } else {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     public Article getArticleByPermalink(String permalink) {
         List<Article> strings = ofy.get().query(Article.class).filter("permalink", permalink).list();
         if (strings.isEmpty()) {
             return null;
         } else if (strings.size() == 1) {
             return strings.get(0);
         } else {
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     public void delete(Long id) {
         searchRepository.deleteFromSearch(getArticle(id));
         ofy.get().delete(Article.class, id);
     }
 
     public int count(String feed, boolean onlyPublished) {
         Query<Article> query = ofy.get().
                 query(Article.class).
                 filter("feed", feed);
         if (onlyPublished) {
             query = query.filter("state", ArticleState.PUBLISHED);
         }
         return query.count();
     }
 
     public List<Article> getPendingFeed(String feed) {
         return ofy.get().query(Article.class)
                 .filter("feed", feed)
                 .filter("state", ArticleState.PENDING)
                 .order("-lastUpdated")
                 .list();
     }
 
     public List<Article> getFeed(String feed, boolean onlyPublished, Integer offset) {
        return getFeed(feed, onlyPublished, offset, 5);
    }

    public List<Article> getFeed(String feed, boolean onlyPublished, Integer offset, Integer limit) {
         List<Article> articles = new ArrayList<Article>();
         if (!onlyPublished && (offset == 0)) {
             articles.addAll(getPendingFeed(feed));
         }
         Query<Article> query = ofy.get()
                 .query(Article.class)
                 .filter("feed", feed)
                 .filter("state", ArticleState.PUBLISHED)
                 .order("-created")
                 .offset(offset)
                .limit(limit);
         articles.addAll(query.list());
         return articles;
     }
 
     public void mergeArticle(Article article) {
         if (article.getId() == null || article.getId() == 0) {
             article.setId(null);
             article.setLastUpdated(new Date());
             ofy.get().put(article);
         } else {
             Objectify ofy = this.ofy.get();
             Article oldArticle = ofy.get(Article.class, article.getId());
             oldArticle.setLastUpdated(new Date());
             oldArticle.setToView(article.getToView());
             oldArticle.setEditableContent(article.getEditableContent());
             oldArticle.setContent(article.getContent());
             oldArticle.setShortContent(article.getShortContent());
             oldArticle.setMainPhotoUrl(article.getMainPhotoUrl());
             oldArticle.setFeed(article.getFeed());
             oldArticle.setTitle(article.getTitle());
             if (!oldArticle.getPermalink().equals(article.getPermalink())) {
                 searchRepository.deleteFromSearch(oldArticle);
             }
             oldArticle.setPermalink(article.getPermalink());
             if (ArticleState.PUBLISHED.equals(oldArticle.getState())) {
                 searchRepository.submitToSearch(oldArticle);
             }
             ofy.put(oldArticle);
         }
     }
 }
 
 
 
