 package com.tsekhan.rssreader.web.services;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.tsekhan.rssreader.dao.ChannelDao;
 import com.tsekhan.rssreader.dao.PackDao;
 import com.tsekhan.rssreader.dao.TagDao;
 import com.tsekhan.rssreader.dao.exceptions.account.AccountNonexistentException;
 import com.tsekhan.rssreader.dao.exceptions.channel.ChannelDuplicationException;
 import com.tsekhan.rssreader.dao.exceptions.channel.ChannelNonexistentException;
 import com.tsekhan.rssreader.dao.exceptions.pack.PackDescriptionException;
 import com.tsekhan.rssreader.dao.exceptions.pack.PackNameException;
 import com.tsekhan.rssreader.dao.exceptions.pack.PackNonexistentException;
 import com.tsekhan.rssreader.dao.exceptions.tag.TagDuplicationException;
 import com.tsekhan.rssreader.dao.exceptions.tag.TagNameException;
 import com.tsekhan.rssreader.dao.exceptions.tag.TagNonexistentException;
 import com.tsekhan.rssreader.persistence.Channel;
 import com.tsekhan.rssreader.persistence.Pack;
 import com.tsekhan.rssreader.persistence.Tag;
 import com.tsekhan.rssreader.services.RssService;
 import com.tsekhan.rssreader.web.services.views.ChannelView;
 import com.tsekhan.rssreader.web.services.views.PackView;
 import com.tsekhan.rssreader.web.services.views.TagView;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Repository;
 
 /**
  * Service, that provides channels controller functionality.
  *
  * @author Mikola Tsekhan <tsekhan@gmail.com>
  */
 @Repository
 @Scope("singleton")
 public class ChannelsServiceImpl implements ChannelsService {
 
     @Autowired
     private PackDao packDao;
     @Autowired
     private TagDao tagDao;
     @Autowired
     private ChannelDao channelDao;
     @Autowired
     private RssService rssService;
     @Autowired
     private SearchService searchService;
     private static final Logger logger =
             Logger.getLogger(ChannelsServiceImpl.class.getName());
 
     /**
      * Listing all packs of specified account.
      *
      * @param login account login.
      * @return Returns list of all packs of specified account.
      * @throws AccountNonexistentException if specified account doesn't exists.
      */
     @Override
     public List<PackView> getPacks(String login)
             throws AccountNonexistentException {
         Set<Pack> packs = packDao.getPacks(login);
         List<PackView> result = new ArrayList<>();
         for (Pack pack : packs) {
             result.add(new PackView(pack));
         }
         return result;
     }
 
     /**
      * Returns specified pack as PackView.
      *
      * @param packId - pack id.
      * @return Returns specified pack as PackView.
      * @throws PackNonexistentException if specified pack doesn't exists.
      */
     @Override
     public PackView getPack(Long packId) throws PackNonexistentException {
         return new PackView(packDao.getPack(packId));
     }
 
     /**
      * Creates new channel pack for specified account.
      *
      * @param login account login.
      * @param packName pack name.
      * @return Returns new pack`s id.
      * @throws AccountNonexistentException if specified account doesn't exists.
      */
     @Override
     public Long createPack(String login, String packName,
             String description, boolean available)
             throws AccountNonexistentException, PackNameException,
             PackDescriptionException {
         Long packId = packDao.createPack(login, packName, description,
                 available);
         if (available == true) {
             // TODO Search!
                 /*
              * searchService.addIndex(description, String.valueOf(packId),
              * SearchService.ObjectType.PACK);
              */
         }
         return packId;
     }
 
     /**
      * Removes pack with specified id.
      *
      * @param packId pack id.
      * @throws PackNonexistentException if pack with specified id doesn't
      * exists.
      */
     @Override
     public void removePack(Long packId) throws PackNonexistentException {
         // TODO Implement search index removing.
         /*
          * searchService.removeIndex(packId.toString(),
          * SearchService.ObjectType.PACK);
          */
         for (Channel channel : packDao.getPack(packId).getChannels()) {
             if (channelDao.countSubscriptions(channel.getSourceLink()) <= 1) {
                 // TODO Implement search index removing.
                 /*
                  * searchService.removeIndex(channel.getSourceLink(),
                  * SearchService.ObjectType.CHANNEL);
                  */
             }
         }
         packDao.removePack(packId);
     }
 
     /**
      * Modifies pack parameters.
      *
      * @param packId pack id.
      * @param name new pack name.
      * @param description new pack description.
      * @param available is pack available for viewing by other users.
      * @throws PackNonexistentException if pack with specified id doesn't
      * exists.
      * @throws PackNameException if new pack name is invalid.
      * @throws PackDescriptionException if new pack description is invalid.
      */
     @Override
     public void updatePack(Long packId, String name, String description,
             boolean available) throws PackNonexistentException,
             PackNameException, PackDescriptionException {
         packDao.updatePack(packId, name, description, available);
     }
 
     /**
      * Determines, is specified account is pack owner.
      *
      * @param login account login.
      * @param id pack id.
      * @return Returns true, if pack with specified id owned by specified
      * account. Otherwise returns false.
      */
     @Override
     public boolean isPackOwner(String login, Long id) {
         try {
             return packDao.getPack(id).getAccount().getLogin().equals(login);
         } catch (PackNonexistentException ex) {
             logger.log(Level.INFO, ex.getLocalizedMessage(), ex);
             return false;
         }
     }
 
     @Override
     public TagView addTag(Long packId, String name)
             throws TagDuplicationException, TagNameException,
             PackNonexistentException {
         Long id = tagDao.addTag(packId, name);
         Tag tag;
         try {
             tag = tagDao.getTag(id);
         } catch (TagNonexistentException ex) {
             logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
             throw new RuntimeException(ex);
         }
         return new TagView(tag);
     }
 
     @Override
     public void removeTag(Long tagId) throws TagNonexistentException {
         tagDao.removeTag(tagId);
     }
 
     @Override
     public boolean isTagOwner(String login, Long tagId) {
         try {
             return isPackOwner(login,
                     tagDao.getTag(tagId).getTagPack().getId());
         } catch (TagNonexistentException ex) {
             logger.log(Level.INFO, ex.getLocalizedMessage(), ex);
             return false;
         }
     }
 
     /**
      * Creates subscription of specified pack to specified link.
      *
      * @param packId pack id.
      * @param link link for subscribing.
      * @return Returns new channel view.
      * @throws PackNonexistentException if specified pack doesn't exists.
      * @throws ChannelDuplicationException if specified pack already subscribed
      * to specified link.
      */
     // TODO realize detection of inherited rss-link.
     @Override
     public ChannelView subscribe(Long packId, String link)
             throws PackNonexistentException, ChannelDuplicationException {
         Long id = channelDao.subscribe(packId, link);
         Channel channel;
         try {
             channel = channelDao.getChannel(id);
         } catch (ChannelNonexistentException ex) {
             logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
             throw new RuntimeException(ex);
         }
         return new ChannelView(channel);
     }
 
     /**
      * Removes specified subscription.
      *
      * @param channelId removed channel id.
      * @throws ChannelNonexistentException if specified channel doesn't exists.
      */
     @Override
     public void unsubscribe(Long channelId) throws ChannelNonexistentException {
         Channel channel = channelDao.getChannel(channelId);
         // TODO Return search index deleting.
         /*
          * if (packDao.countSubscriptions(channel.getSourceLink()) <= 1)
          * searchService.removeIndex(channel.getSourceLink(),
          * SearchService.ObjectType.CHANNEL);
          */
         channelDao.removeChannel(channelId);
     }
 
     /**
      * Determines, is specified account is channel owner,
      *
      * @param login account login.
      * @param channelId channel id.
      * @return Returns true, if channel with specified id owned by specified
      * account. Otherwise returns false.
      */
     @Override
     public boolean isChannelOwner(String login, Long channelId) {
         try {
             return isPackOwner(login,
                     channelDao.getChannel(channelId).getChannelPack().getId());
         } catch (ChannelNonexistentException ex) {
             logger.log(Level.INFO, ex.getLocalizedMessage(), ex);
             return false;
         }
     }
 
     private List<ExtendedSyndEntry> getChannelEntries(Channel channel) {
         SyndFeed feed = rssService.getFeed(channel.getSourceLink());
         List<ExtendedSyndEntry> result = new ArrayList<>();
         if (feed != null) {
             for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
                 ExtendedSyndEntry newEntry = new ExtendedSyndEntry();
                 newEntry.copyFrom(entry);
                 newEntry.setChannelId(channel.getId());
                 result.add(newEntry);
             }
         }
         return result;
     }
 
     /**
      * Listing all entries of specified pack.
      *
      * @param packId pack id.
      * @return Returns list of items of
      * {@link RssServiceImpl.ExtendedSyndEntry} class.
      * @throws PackNonexistentException if pack with specified id doesn't
      * exists.
      */
     @Override
     public List<ExtendedSyndEntry> getEntries(Long packId)
             throws PackNonexistentException {
         List<ExtendedSyndEntry> entries = new ArrayList<>();
         for (Channel channel : channelDao.getChannels(packId)) {
             entries.addAll(getChannelEntries(channel));
         }
         return entries;
     }
 }
