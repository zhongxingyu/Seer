 package com.ngdb.web.pages;
 
 import com.ngdb.entities.Registry;
 import com.ngdb.entities.Top100Item;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.ReferenceService;
 import com.ngdb.web.model.Top100List;
 import org.apache.commons.lang.StringUtils;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.SelectModel;
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.beaneditor.BeanModel;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.BeanModelSource;
 import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class Top100 {
 
     @Inject
     private Registry registry;
 
     @Property
     private Top100Item topItem;
 
     @Persist
     @Property
     private String top100;
 
     @Persist
     private String currentTop100;
 
     @Inject
     private AjaxResponseRenderer ajaxResponseRenderer;
 
     @InjectComponent
     private Zone top100Zone;
 
     @Inject
     private Messages messages;
 
     @Inject
     private BeanModelSource beanModelSource;
 
     @Persist
     @Property
     private BeanModel<Top100Item> model;
 
     private final Collection<String> tops = new ArrayList<String>();
 
     @Persist
     private long oldCount;
 
     @Inject
     private ReferenceService referenceService;
 
    @SetupRender
    void init() {
         oldCount = Long.MIN_VALUE;
         List<Platform> platforms = referenceService.getPlatforms();
         for (Platform platform : platforms) {
             tops.add("Top 100 - Collection - "+platform.getShortName());
         }
         for (Platform platform : platforms) {
             tops.add("Top 100 - Wishlist - "+platform.getShortName());
         }
         tops.add("Top 100 - Rating");
         tops.add("Top 100 - Recently sold");
         tops.add("Top 100 - Recently in shop");
         currentTop100 = tops.iterator().next();
 
         model = beanModelSource.createDisplayModel(Top100Item.class, messages);
         model.get("title").label(messages.get("common.Title")).sortable(true);
         model.get("originTitle").label(messages.get("common.Origin")).sortable(true);
         model.get("rank").label(messages.get("common.Rank")).sortable(true);
         model.get("count").label(messages.get("common.Count")).sortable(true);
         model.include("rank", "title", "count", "originTitle");
         model.reorder("rank", "count", "title", "originTitle");
     }
 
     public Collection<Top100Item> getTopItems() {
         Collection<Top100Item> top100ItemList = new ArrayList<Top100Item>();
 
         Platform platform = null;
         if(StringUtils.countMatches(currentTop100, "-") == 2) {
             String platformName = currentTop100.split("-")[2].trim();
             platform = referenceService.findPlatformByName(platformName);
         }
         top100ItemList = listGames(top100ItemList, platform);
         return top100ItemList;
     }
 
     private Collection<Top100Item> listGames(Collection<Top100Item> top100ItemList, Platform platform) {
         if(currentTop100.contains("Collection")) {
             top100ItemList = registry.findTop100OfGamesInCollection(platform);
         } else if(currentTop100.contains("Wishlist")) {
             top100ItemList = registry.findTop100OfGamesInWishlist(platform);
         } else if(currentTop100.contains("Rating")) {
             top100ItemList = registry.findTop100OfGamesWithRating();
         } else if(currentTop100.contains("Recently sold")) {
             top100ItemList = registry.findTop100OfGamesRecentlySold();
         } else if(currentTop100.contains("Recently in shop")) {
             top100ItemList = registry.findTop100OfGamesRecentlyInShop();
         }
         return top100ItemList;
     }
 
     public SelectModel getTop100List() {
         return new Top100List(tops);
     }
 
     @OnEvent(component = "top100", value = EventConstants.VALUE_CHANGED)
     public void onSelectFromTop100(String currentTop100) {
         this.currentTop100 = currentTop100;
         ajaxResponseRenderer.addRender(top100Zone);
     }
 
 }
