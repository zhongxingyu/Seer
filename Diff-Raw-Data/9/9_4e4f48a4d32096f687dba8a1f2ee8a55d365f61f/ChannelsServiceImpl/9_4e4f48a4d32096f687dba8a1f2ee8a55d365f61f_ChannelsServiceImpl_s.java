 package com.tsekhan.rssreader.web.services;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.tsekhan.rssreader.dao.ReaderDao;
 import com.tsekhan.rssreader.dao.exceptions.DuplicateSubscribingException;
 import com.tsekhan.rssreader.dao.exceptions.NonexistentAccountException;
 import com.tsekhan.rssreader.dao.exceptions.NonexistentChannelException;
 import com.tsekhan.rssreader.dao.exceptions.NonexistentPackException;
 import com.tsekhan.rssreader.persistence.Channel;
 import com.tsekhan.rssreader.persistence.Pack;
 import com.tsekhan.rssreader.services.RssService;
 import com.tsekhan.rssreader.services.RssServiceImpl;
 import com.tsekhan.rssreader.web.services.views.ChannelView;
 import com.tsekhan.rssreader.web.services.views.PackView;
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
     ReaderDao readerDao;
     @Autowired
     RssService rssService;
     @Autowired
     SearchService searchService;
     private static final Logger logger = Logger.getLogger(ChannelsServiceImpl.class.getName());
 
     /**
      * Listing all packs of specified account.
      *
      * @param login account login.
      * @return Returns list of all packs of specified account.
      * @throws NonexistentAccountException if specified account doesn't exists.
      */
     @Override
     public List<PackView> getPacks(String login)
             throws NonexistentAccountException {
         Set<Pack> packs = readerDao.getPacks(login);
         List<PackView> result = new ArrayList<>();
         for (Pack pack : packs) {
             result.add(new PackView(pack));
         }
         return result;
     }
 
     /**
      * Creates new channel pack for specified account.
      *
      * @param login account login.
      * @param packName pack name.
      * @return Returns new pack view.
      * @throws NonexistentAccountException if specified account doesn't exists.
      */
     @Override
     public PackView createPack(String login, String packName,
             String description, boolean available)
             throws NonexistentAccountException {
         try {
             Long packId = readerDao.createPack(login, packName, description,
                     available);
             // TODO Realize search index deleting.
            // XXX Index work if available==false. Why?
            System.out.println(available);
             if (available == true) {
                 searchService.addIndex(description, String.valueOf(packId),
                         SearchService.ObjectType.PACK);
             }
             Pack pack = readerDao.getPack(packId);
             return new PackView(pack);
         } catch (NonexistentPackException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     // TODO Realize pack info modifying.
 
     /**
      * Removes pack with specified id.
      *
      * @param packId pack id.
      * @throws NonexistentPackException if pack with specified id doesn't
      * exists.
      */
     @Override
     public void removePack(Long packId) throws NonexistentPackException {
         readerDao.removePack(packId);
     }
 
     /**
      * Determines, is specified account is pack owner.
      *
      * @param login account login.
      * @param packId pack id.
      * @return Returns true, if pack with specified id owned by specified
      * account. Otherwise returns false.
      */
     @Override
     public boolean isPackOwner(String login, Long packId) {
         try {
             return readerDao.getPack(packId).getAccount().getLogin()
                     .equals(login);
         } catch (NonexistentPackException ex) {
             logger.log(Level.SEVERE, null, ex);
             return false;
         }
     }
 
     /**
      * Creates subscription of specified pack to specified link.
      *
      * @param packId pack id.
      * @param link link for subscribing.
      * @return Returns channel view if successfully subscripted.
      * @throws NonexistentPackException if specified pack doestn't exists.
      * @throws DuplicateSubscribingException if specified pack already
      * subscribed to specified link.
      */
     // TODO realize detection of inherited rss-link.
     @Override
     public ChannelView subscribe(Long packId, String link)
             throws NonexistentPackException, DuplicateSubscribingException {
         try {
             Long channelId = readerDao.subscribe(packId, link);
             Channel channel = readerDao.getChannel(channelId);
             return new ChannelView(channel);
         } catch (NonexistentChannelException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     /**
      * Removes specified subscription.
      *
      * @param channelId removed channel id.
      * @throws NonexistentChannelException if specified channel doesn't exists.
      */
     @Override
     public void unsubscribe(Long channelId) throws NonexistentChannelException {
         readerDao.removeChannel(channelId);
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
                     readerDao.getChannel(channelId).getPack().getId());
         } catch (NonexistentChannelException ex) {
             logger.log(Level.SEVERE, null, ex);
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
      * @throws NonexistentPackException if pack with specified id doesn't
      * exists.
      */
     @Override
     public List<ExtendedSyndEntry> getEntries(Long packId)
             throws NonexistentPackException {
         List<ExtendedSyndEntry> entries = new ArrayList<>();
         for (Channel channel : readerDao.getChannels(packId)) {
             entries.addAll(getChannelEntries(channel));
         }
         return entries;
     }
 }
