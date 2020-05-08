 package com.ngdb.entities;
 
 import com.ngdb.entities.article.Accessory;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.article.Hardware;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.shop.ShopItem;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 
 import java.util.*;
 
 import static org.hibernate.criterion.Order.asc;
 import static org.hibernate.criterion.Projections.countDistinct;
 import static org.hibernate.criterion.Projections.id;
 import static org.hibernate.criterion.Restrictions.eq;
 import static org.hibernate.criterion.Restrictions.in;
 
 public class MarketFilter extends AbstractFilter {
 
     private Market market;
     private Session session;
 
     public MarketFilter(com.ngdb.entities.Market market, Session session) {
         this.market = market;
         this.session = session;
         clear();
     }
 
     public Collection<ShopItem> getShopItems() {
         if(isFilteredByAnUserWithoutShopItems()) {
             return new ArrayList<ShopItem>();
         }
         Criteria articleCriteria = createArticleCriteria();
         articleCriteria = addPlatformCriteria(articleCriteria, filteredPlatform);
         articleCriteria = addOriginCriteria(articleCriteria, filteredOrigin);
         List<Long> articles = articleCriteria.list();
         if(articles.isEmpty()) {
             return new ArrayList<ShopItem>();
         }
         Criteria criteria = session.createCriteria(ShopItem.class).add(eq("sold", false));
        criteria = addArticleCriteria(criteria, filteredArticle);
         criteria = addUserFilter(criteria);
         return criteria.add(in("article.id", articles)).addOrder(asc("priceInCustomCurrency")).list();
     }
 
     private Criteria addPlatformCriteria(Criteria criteria, Platform platform) {
         if(platform == null) {
             return criteria;
         }
         return criteria.add(eq("platformShortName", platform.getShortName()));
     }
 
     private Criteria addOriginCriteria(Criteria criteria, Origin origin) {
         if(origin == null) {
             return criteria;
         }
         return criteria.add(eq("originTitle", origin.getTitle()));
     }
 
    private Criteria addArticleCriteria(Criteria criteria, Article article) {
        if(article == null) {
            return criteria;
        }
        return criteria.add(eq("article", article));
    }

     private Criteria createArticleCriteria() {
         Class<? extends Article> clazz = Accessory.class;
         if(isFilteredByGames()) {
             clazz = Game.class;
         }
         if(isFilteredByHardwares()) {
             clazz = Hardware.class;
         }
         return session.createCriteria(clazz).setProjection(id());
     }
 
     public int getNumShopItemsInThisOrigin(Origin origin) {
         if(isFilteredByAnUserWithoutShopItems()) {
             return 0;
         }
         Criteria articleCriteria = createArticleCriteria();
         if (filteredPlatform != null) {
             articleCriteria = addPlatformCriteria(articleCriteria, filteredPlatform);
         }
         articleCriteria = addOriginCriteria(articleCriteria, origin);
         List articles = articleCriteria.list();
         if(articles.isEmpty()) {
             return 0;
         }
         Criteria criteria = createShopItemCriteria().setProjection(countDistinct("id"));
         criteria = addUserFilter(criteria);
         return ((Long)criteria.add(in("article.id", articles)).uniqueResult()).intValue();
     }
 
     public int getNumShopItemsInThisPlatform(Platform platform) {
         if(isFilteredByAnUserWithoutShopItems()) {
             return 0;
         }
         Criteria articleCriteria = createArticleCriteria();
         articleCriteria = addPlatformCriteria(articleCriteria, platform);
         List articlesInThisPlatform = articleCriteria.list();
         if(articlesInThisPlatform.isEmpty()) {
             return 0;
         }
         Criteria criteria = createShopItemCriteria().setProjection(countDistinct("id"));
         criteria = addUserFilter(criteria);
         criteria = criteria.add(in("article.id", articlesInThisPlatform));
         return ((Long)criteria.uniqueResult()).intValue();
     }
 
     private Criteria addUserFilter(Criteria criteria) {
         if(filteredUser != null) {
             criteria = criteria.add(eq("seller", filteredUser));
         }
         return criteria;
     }
 
     private Criteria createShopItemCriteria() {
         return session.createCriteria(ShopItem.class);
     }
 
     private boolean isFilteredByAnUserWithoutShopItems() {
         return filteredUser != null && filteredUser.getShop().getNumShopItems() == 0;
     }
 
     public long getNumHardwares() {
         if (filteredUser != null) {
             return market.getNumHardwaresForSaleBy(filteredUser);
         }
         return market.getNumHardwaresForSale();
     }
 
     public long getNumAccessories() {
         if (filteredUser != null) {
             return market.getNumAccessoriesForSaleBy(filteredUser);
         }
         return market.getNumAccessoriesForSale();
     }
 
     public long getNumGames() {
         if (filteredUser != null) {
             return market.getNumGamesForSaleBy(filteredUser);
         }
         return market.getNumGamesForSale();
     }
 
 }
