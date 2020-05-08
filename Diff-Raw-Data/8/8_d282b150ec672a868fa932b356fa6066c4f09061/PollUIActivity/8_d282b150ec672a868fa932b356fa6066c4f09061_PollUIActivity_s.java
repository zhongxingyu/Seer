 /*
  * Copyright (C) 2003-2013 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.forum.ext.impl;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.forum.common.CommonUtils;
 import org.exoplatform.forum.common.webui.WebUIUtils;
 import org.exoplatform.forum.service.Utils;
 import org.exoplatform.poll.service.Poll;
 import org.exoplatform.poll.service.PollService;
 import org.exoplatform.portal.mop.SiteType;
 import org.exoplatform.social.core.space.SpaceUtils;
 import org.exoplatform.social.core.space.model.Space;
 import org.exoplatform.social.core.space.spi.SpaceService;
 import org.exoplatform.social.webui.activity.BaseUIActivity;
 import org.exoplatform.web.application.RequestContext;
 import org.exoplatform.web.url.navigation.NavigationResource;
 import org.exoplatform.web.url.navigation.NodeURL;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 
 @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/forum/social-integration/plugin/space/PollUIActivity.gtmpl", events = {
   @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
   @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
   @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
   @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
   @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
   @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
   @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class) })
 public class PollUIActivity extends BaseKSActivity {
   
   private String totalOfUsersVote;
 
   public Map<String, List<String>> getVotes() throws Exception{
     Map<String, List<String>> info = new LinkedHashMap<String, List<String>>();
     String pollId = getActivityParamValue(PollSpaceActivityPublisher.POLL_ID).replace(Utils.TOPIC, Utils.POLL);
     PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
     Poll poll = pollService.getPoll(pollId);
     String[] options = poll.getOption();
     String[] userVotes = poll.getUserVote();
     String[] votes = poll.getVote();
     int[] votesValues = getVotesOfOption(userVotes, options.length);
     
     for (int i = 0; i < options.length; i++) {
       List<String> values = new LinkedList<String>();
       values.add(buildPercentVote(votes[i]));
       values.add(getStringFromNumberOfVotes(votesValues[i]));
       info.put(CommonUtils.decodeSpecialCharToHTMLnumber(options[i]), values);
     }
    setTotalUsersVotes(getStringFromNumberOfVotes(userVotes.length));
     return info;
   }
   
   
   /**
    * Gets only 2 numbers after the point
    * 
    * @param percent
    * @return
    */
   private String buildPercentVote(String percent) {
     int index = percent.lastIndexOf(".");
     if (index > 0 && (index + 2) < percent.length()) {
       percent = percent.substring(0, index + 3);
     }
     return percent;
   }
   
   /**
    * @return the nbTotalVotes
    */
   public String getTotalUsersVotes() {
     return totalOfUsersVote;
   }
 
   /**
    * @param nbTotalVotes the nbTotalVotes to set
    */
   public void setTotalUsersVotes(String totalOfUsersVote) {
     this.totalOfUsersVote = totalOfUsersVote;
   }
   
   private String getStringFromNumberOfVotes(int nbVotes) {
     if (nbVotes <= 1) {
       return WebUIUtils.getLabel(null, "PollUIActivity.label.vote").replace("{0}", String.valueOf(nbVotes));
     } else {
       return WebUIUtils.getLabel(null, "PollUIActivity.label.votes").replace("{0}", String.valueOf(nbVotes));
     }
   }
 
   private int[] getVotesOfOption(String[] userVotes, int length) {
     int[] tab = new int[length];
     for (String userVote : userVotes) {
       String[] votes = userVote.split(":");
       for (int i = 1; i < votes.length; i++) {
         int index = Integer.parseInt(votes[i]);
        tab[index] =+ 1;
       }
     }
     return tab;
   }
   
   @SuppressWarnings("unused")
   private String getLink() {
     String spaceLink = getSpaceHomeURL(getSpaceGroupId());
     if (spaceLink == null)
       return getActivityParamValue(PollSpaceActivityPublisher.POLL_LINK_KEY);
     String topicId = getActivityParamValue(PollSpaceActivityPublisher.POLL_ID);
     String topicLink = String.format("%s/forum/topic/%s", spaceLink, topicId);
     return topicLink;
   }
   
   private String getSpaceGroupId() {
     return getActivityParamValue(PollSpaceActivityPublisher.SPACE_GROUP_ID);
   }
   
   public String getSpaceHomeURL(String spaceGroupId) {
     if ("".equals(spaceGroupId))
       return null;
     String permanentSpaceName = spaceGroupId.split("/")[2];
     SpaceService spaceService  = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
     Space space = spaceService.getSpaceByGroupId(spaceGroupId);
     
     NodeURL nodeURL =  RequestContext.getCurrentInstance().createURL(NodeURL.TYPE);
     NavigationResource resource = new NavigationResource(SiteType.GROUP, SpaceUtils.SPACE_GROUP + "/"
                                         + permanentSpaceName, space.getPrettyName());
    
     return nodeURL.setResource(resource).toString(); 
   }
   
 }
