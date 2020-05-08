 package com.tsekhan.rssreader.web;
 
 import com.tsekhan.rssreader.dao.exceptions.DuplicateSubscribingException;
 import com.tsekhan.rssreader.dao.exceptions.NonexistentAccountException;
 import com.tsekhan.rssreader.dao.exceptions.NonexistentChannelException;
 import com.tsekhan.rssreader.dao.exceptions.NonexistentPackException;
 import com.tsekhan.rssreader.web.json.Response;
 import com.tsekhan.rssreader.web.services.ChannelsService;
 import java.security.Principal;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Handles requests directed to the channels page.
  * @author Mikola Tsekhan <tsekhan@gmail.com>
  */
 @Controller
 @RequestMapping ("/channels")
 @Secured("ROLE_USER")
 public class ChannelsController {
     
     @Autowired
     ChannelsService channelsService;
     
     private static final Logger logger = Logger
             .getLogger(ChannelsController.class.getName());
     
     /**
      * Showing {@literal channels.jsp} page.
      */
     @RequestMapping
     public void renderPage() {}
     
     /**
      * Returns list of all packs with all channels inherited.
      * @param principal current principal.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. Parameter {@code message}
      * contains list of packs.
      */
     @RequestMapping(method = RequestMethod.POST, params = {"action=getPacks"})
     public @ResponseBody Response renderPacks(Principal principal) {
         Response response = new Response();
         try {
             response.setMessage(channelsService.getPacks(principal.getName()));
         } catch (NonexistentAccountException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         return response;
     }
     
     /**
      * Creating new channels pack for current user.
      * @param packName new pack`s name.
      * @param principal current principal.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If new pack successfully
      * created, parameter {@code message} contains new pack`s id.
      */
     @RequestMapping(method = RequestMethod.POST, params = {"action=createPack"})
     public @ResponseBody Response createPack(
             @RequestParam("name") String packName,
             @RequestParam("description") String description,
             @RequestParam("available") boolean available,
             Principal principal) {
         Response response = new Response();
         try {
             response.setMessage(channelsService.createPack(principal.getName(),
                    packName, description, available));
         } catch (NonexistentAccountException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         return response;
     }
     
     /**
      * Removing specified pack.
      * @param packId pack id.
      * @param principal current principal.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If successful, parameter
      * {@code message} contains {@code true}. If some errors occurs, parameter
      * {@code errors} contains errors description.
      */
     @RequestMapping(method = RequestMethod.POST, params = {"action=removePack"})
     public @ResponseBody Response removePack(
             @RequestParam("id") Long packId, Principal principal) {
         Response response = new Response();
         if (channelsService.isPackOwner(principal.getName(), packId) == true)
             try {
                 channelsService.removePack(packId);
                 response.setMessage(true);
             } catch (NonexistentPackException ex) {
                 logger.log(Level.WARNING, null, ex);
                 response.addError(ex.getClass().getSimpleName());
             }
         else
             response.addError("Permissions denied");
         return response;
     }
     
     /**
      * Subscribing specified pack to specified link.
      * @param packId pack id.
      * @param link link for subscription. Can be link to page,
      * which contains link to rss-source.
      * @param principal current principal.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If successful subscribed,
      * parameter {@code message} of response contains new channel link.
      * If some errors occurs, parameter {@code errors} will contains
      * errors descriptions.
      */
     @RequestMapping(method = RequestMethod.POST, params = {"action=subscribe"})
     public @ResponseBody Response subscribe(@RequestParam("pack") Long packId,
             @RequestParam("link") String link, Principal principal) {
         Response response = new Response();
         if (channelsService.isPackOwner(principal.getName(), packId) == true) {
             try {
                 response.setMessage(channelsService.subscribe(packId, link));
             } catch (NonexistentPackException
                     | DuplicateSubscribingException ex) {
                 logger.log(Level.SEVERE, null, ex);
                 response.addError(ex.getClass().getSimpleName());
             }
         }
         return response;
     }
     
     /**
      * Unsubscribing pack of current user from specified channel.
      * @param channelId channel id, from which must be unsubscribed.
      * @param principal current principal.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response} with errors
      * description in {@code error}s parameter, if some errors occurs.
      */
     @RequestMapping(method = RequestMethod.POST,
             params = {"action=unsubscribe"})
     public @ResponseBody Response unsubscribe(
             @RequestParam("id") Long channelId, Principal principal) {
         Response response = new Response();
         if (channelsService.isChannelOwner(principal.getName(), channelId)) {
             try {
                 channelsService.unsubscribe(channelId);
             } catch (NonexistentChannelException ex) {
                 logger.log(Level.WARNING, null, ex);
                 response.addError(ex.getClass().getSimpleName());
             }
         }
         else
             response.addError("Permissions denied");
         return response;
     }
     
     /**
      * Returns rss-entries of all channels from specified pack, if user
      * permissions allows this.
      * @param packId pack id.
      * @param principal current principal, for which checking permissions.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If allowed, {@code message}
      * parameter contains list of rss-entries. If not allowed, {@code errors}
      * parameter contains error with description '<i>Permissions denied</i>'.
      */
     @RequestMapping(method = RequestMethod.POST, params = {"action=getEntries"})
     public @ResponseBody Response getEntries(
             @RequestParam("id") Long packId, Principal principal) {
         Response response = new Response();
         if (channelsService.isPackOwner(principal.getName(), packId) == true) {
             try {
                 response.setMessage(channelsService.getEntries(packId));
             } catch (NonexistentPackException ex) {
                 logger.log(Level.WARNING, null, ex);
             }
         }
         else
             response.addError("Permissions denied");
         return response;
     }
     
 }
