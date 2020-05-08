 package net.chrissearle.flickrvote.web.vote;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.Preparable;
 import net.chrissearle.flickrvote.service.ChallengeService;
 import net.chrissearle.flickrvote.service.model.ChallengeInfo;
 import net.chrissearle.flickrvote.service.model.ImageInfo;
 import net.chrissearle.flickrvote.service.model.PhotographerInfo;
 import net.chrissearle.flickrvote.web.FlickrVoteWebConstants;
 import org.apache.log4j.Logger;
 import org.apache.struts2.interceptor.SessionAware;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 import java.util.Map;
 
 public class VoteAction extends ActionSupport implements SessionAware, Preparable {
     Logger logger = Logger.getLogger(VoteAction.class);
 
     private Map<String, Object> session;
 
     List<String> votes;
 
     private ChallengeInfo challenge;
     private List<ImageInfo> images;
 
     @Autowired
     private ChallengeService challengeService;
 
     public void setVotes(List<String> votes) {
         if (logger.isDebugEnabled()) {
             logger.debug("setVotes: " + votes);
         }
 
         this.votes = votes;
     }
 
     public void setSession(Map<String, Object> stringObjectMap) {
         if (logger.isDebugEnabled()) {
             logger.debug("setSession");
         }
 
         this.session = stringObjectMap;
     }
 
     @Override
     public String execute() throws Exception {
         if (logger.isDebugEnabled()) {
             logger.debug("execute");
         }
 
         PhotographerInfo photographer = (PhotographerInfo) session.get(FlickrVoteWebConstants.FLICKR_USER_SESSION_KEY);
 
         if (logger.isDebugEnabled()) {
             logger.debug("Votes by: " + photographer.getName() + " : " + votes);
         }
 
         for (String imageId : votes) {
             challengeService.vote(photographer.getId(), imageId);
         }
 
         addActionMessage("Thankyou for your vote");
         
         return SUCCESS;
     }
 
     @Override
     public void validate() {
         if (logger.isDebugEnabled()) {
             logger.debug("validate");
         }
 
         ChallengeInfo challenge = challengeService.getVotingChallenge();
 
         int voteCount = 5;
 
         if (challenge != null) {
             List<ImageInfo> images = challengeService.getImagesForChallenge(challenge.getTag());
 
             if (images != null) {
                 if (images.size() <= voteCount) {
                     voteCount = images.size();
 
                     boolean seenPhotographer = false;
 
                     PhotographerInfo photographer = (PhotographerInfo) session.get(FlickrVoteWebConstants.FLICKR_USER_SESSION_KEY);
 
                     for (ImageInfo image : images) {
                         if (image.getPhotographerName().equals(photographer.getName())) {
                             seenPhotographer = true;
 
                             if (votes != null && votes.contains(image.getId())) {
                                 addActionError("You may not vote for yourself");
                             }
                         }
                     }
 
                     if (seenPhotographer) {
                         voteCount--;
                     }
 
                     if (votes == null || votes.size() != voteCount) {
                         addActionError("Incorrect number of votes - you must vote for " + voteCount + " photos");
                     }
                 }
             }
         }
     }
 
     @Override
     public String input() {
         if (logger.isDebugEnabled()) {
             logger.debug("input");
         }
 
         if (session.containsKey(FlickrVoteWebConstants.FLICKR_USER_SESSION_KEY)) {
             PhotographerInfo photographer = (PhotographerInfo) session.get(FlickrVoteWebConstants.FLICKR_USER_SESSION_KEY);
 
             if (challengeService.hasVoted(photographer.getId())) {
                 return "alreadyVoted";
             }
         }
 
         return INPUT;
     }
 
     public void prepare() throws Exception {
         if (logger.isDebugEnabled()) {
             logger.debug("prepare");
         }
 
         challenge = challengeService.getVotingChallenge();
 
         if (challenge != null) {
             images = challengeService.getImagesForChallenge(challenge.getTag());
         }
     }
 
     public ChallengeInfo getChallenge() {
         if (logger.isDebugEnabled()) {
             logger.debug("getChallenge: " + challenge);
         }
 
         return challenge;
     }
 
     public List<ImageInfo> getImages() {
         if (logger.isDebugEnabled()) {
             logger.debug("getImages: "+ images);
         }
 
         return images;
     }
 
    public String list() throws Exception {
        return "list";
     }
 }
