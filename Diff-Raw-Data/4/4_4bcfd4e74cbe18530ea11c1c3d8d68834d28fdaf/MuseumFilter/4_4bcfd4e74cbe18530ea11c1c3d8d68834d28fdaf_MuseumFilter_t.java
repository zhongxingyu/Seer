 package com.ngdb.entities;
 
 import com.ngdb.entities.article.Accessory;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.article.Hardware;
 import com.ngdb.entities.article.element.Tag;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.Publisher;
 import com.ngdb.entities.user.CollectionObject;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 
 import java.math.BigInteger;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import static org.hibernate.criterion.Order.asc;
 import static org.hibernate.criterion.Projections.*;
 import static org.hibernate.criterion.Restrictions.eq;
 import static org.hibernate.criterion.Restrictions.in;
 
 public class MuseumFilter extends AbstractFilter {
 
     private ArticleFactory articleFactory;
 
     private Tag filteredTag;
     private String filteredNgh;
     private Date filteredReleaseDate;
 
     private Session session;
 
     public MuseumFilter(ArticleFactory articleFactory, Session session) {
         this.articleFactory = articleFactory;
         this.session = session;
         clear();
     }
 
     public void clear() {
         super.clear();
         this.filteredTag = null;
         this.filteredNgh = null;
         this.filteredReleaseDate = null;
     }
 
     public String getQueryLabel() {
         String queryLabel = super.getQueryLabel();
         if (filteredPublisher != null) {
             queryLabel += " published by " + orange(filteredPublisher.getName());
         }
         if (filteredNgh != null) {
             queryLabel += " with ngh " + orange(filteredNgh);
         }
         if (filteredReleaseDate != null) {
             queryLabel += " released at " + orange(new SimpleDateFormat("MM/dd/yyyy").format(filteredReleaseDate));
         }
         if (filteredTag != null) {
             queryLabel += " with tag " + orange(filteredTag.getName());
         }
         return queryLabel;
     }
 
     @Override
     public long getNumHardwares() {
         if (filteredUser != null) {
             return ((BigInteger)session.createSQLQuery("SELECT COUNT(article_id) FROM CollectionObject WHERE user_id = "+filteredUser.getId()+" AND article_id IN (SELECT id FROM Hardware)").uniqueResult()).longValue();
         }
         return articleFactory.getNumHardwares();
     }
 
     @Override
     public long getNumGames() {
         if (filteredUser != null) {
             return ((BigInteger)session.createSQLQuery("SELECT COUNT(article_id) FROM CollectionObject WHERE user_id = "+filteredUser.getId()+" AND article_id IN (SELECT id FROM Game)").uniqueResult()).longValue();
         }
         return articleFactory.getNumGames();
     }
 
     @Override
     public long getNumAccessories() {
         if (filteredUser != null) {
             return ((BigInteger)session.createSQLQuery("SELECT COUNT(article_id) FROM CollectionObject WHERE user_id = "+filteredUser.getId()+" AND article_id IN (SELECT id FROM Accessory)").uniqueResult()).longValue();
         }
         return articleFactory.getNumAccessories();
     }
 
     public List<Article> getArticles() {
         Criteria criteria = createCriteria();
        if(isFilteredByGames()) {
            criteria = addNghFilter(criteria);
        }
         criteria = addPlatformFilter(criteria);
         criteria = addOriginFilter(criteria);
         criteria = addPublisherFilter(criteria);
         criteria = addTagFilter(criteria);
         criteria = addUserFilter(criteria);
         return criteria.addOrder(asc("title")).list();
     }
 
     public int getNumArticlesInThisPlatform(Platform platform) {
         Criteria criteria = createCriteria().setProjection(countDistinct("id"));
         criteria = addTagFilter(criteria);
         criteria = addUserFilter(criteria);
         criteria = criteria.add(eq("platformShortName", platform.getShortName()));
         return ((Long)criteria.uniqueResult()).intValue();
     }
 
     public int getNumArticlesInThisOrigin(Origin origin) {
         Criteria criteria = createCriteria().setProjection(countDistinct("id"));
         criteria = addPlatformFilter(criteria);
         criteria = addTagFilter(criteria);
         criteria = addUserFilter(criteria);
         criteria = criteria.add(eq("originTitle", origin.getTitle()));
         return ((Long)criteria.uniqueResult()).intValue();
     }
 
     public int getNumArticlesInThisPublisher(Publisher publisher) {
         Criteria criteria = createCriteria().setProjection(countDistinct("id"));
         criteria = addPlatformFilter(criteria);
         criteria = addOriginFilter(criteria);
         criteria = addTagFilter(criteria);
         criteria = addUserFilter(criteria);
         criteria = criteria.add(eq("publisher", publisher));
         return ((Long)criteria.uniqueResult()).intValue();
     }
 
     private Criteria addPublisherFilter(Criteria criteria) {
         if (filteredPublisher != null) {
             criteria = criteria.add(eq("publisher",filteredPublisher));
         }
         return criteria;
     }
 
     private Criteria addNghFilter(Criteria criteria) {
         if (filteredNgh != null) {
             criteria = criteria.add(eq("ngh", filteredNgh));
         }
         return criteria;
     }
 
     private Criteria addOriginFilter(Criteria criteria) {
         if (filteredOrigin != null) {
             criteria = criteria.add(eq("originTitle", filteredOrigin.getTitle()));
         }
         return criteria;
     }
 
     private Criteria addTagFilter(Criteria criteria) {
         if (filteredTag != null) {
             criteria = criteria.createAlias("tags.tags", "tag");
             criteria = criteria.add(eq("tag.name", filteredTag.getName()));
         }
         return criteria;
     }
 
     private Criteria addPlatformFilter(Criteria criteria) {
         if (filteredPlatform != null) {
             criteria = criteria.add(eq("platformShortName", filteredPlatform.getShortName()));
         }
         return criteria;
     }
 
     private Criteria addUserFilter(Criteria criteria) {
         if(filteredUser != null) {
             List<CollectionObject> articles = session.createCriteria(CollectionObject.class).setProjection(distinct(property("id.articleId"))).add(eq("owner", filteredUser)).list();
             criteria = criteria.add(in("id", articles));
         }
         return criteria;
     }
 
     public void filterByTag(Tag tag) {
         this.filteredTag = tag;
     }
 
     public void filterByNgh(String ngh) {
         this.filteredNgh = ngh;
     }
 
     public void filterByReleaseDate(Date releaseDate) {
         this.filteredReleaseDate = releaseDate;
     }
 
     public Tag getFilteredTag() {
         return filteredTag;
     }
 
     public Date getFilteredReleaseDate() {
         return filteredReleaseDate;
     }
 
     private Criteria createCriteria() {
         Class<?> clazz = Accessory.class;
         if(filteredByGames) {
             clazz = Game.class;
         } else if(filteredByHardwares) {
             clazz = Hardware.class;
         }
         return session.createCriteria(clazz);
     }
 
 }
