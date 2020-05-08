 /**
  *
  */
 package org.jrecruiter.web.actions;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Map;
 
import org.jrecruiter.common.CollectionUtils;
 import org.jrecruiter.model.Job;
import org.jrecruiter.model.ServerSettings;
import org.jrecruiter.model.util.FilteringUtils.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.CollectionFactory;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 
 /**
  * @author Gunnar Hillert
  * @version $Id:UserService.java 128 2007-07-27 03:55:54Z ghillert $
  */
 public class JobsRssAction extends BaseAction {
 
     /** serialVersionUID */
     private static final long serialVersionUID = -4901833648423551648L;
 
     private SyndFeed rssFeed = new SyndFeedImpl();
 
    private ServerSettings serverSettings;

     /**
      * @return the rssFeed
      */
     public SyndFeed getRssFeed() {
         return rssFeed;
     }
 
     /**
      * @param rssFeed the rssFeed to set
      */
     public void setRssFeed(SyndFeed rssFeed) {
         this.rssFeed = rssFeed;
     }
 
     public String execute() {
         rssFeed.setFeedType("rss_1.0");
         rssFeed.setTitle("AJUG Job Postings");
         rssFeed.setLink("http://www.ajug.org/jobs");
         rssFeed.setDescription("RSS feed of the Atlanta Java User Group's job posting service.");
 
         List <SyndEntry>entries = new ArrayList<SyndEntry>();
         SyndEntry entry;
         SyndContent description;
 
        final Map<String, String>sortOrders = CollectionUtils.getHashMap();
        sortOrders.put("updateDate", "DESC"); //FIXME
        List <Job> jobs = jobService.getJobs(20, 1, sortOrders, null);
 
         for (Job job : jobs) {
             entry = new SyndEntryImpl();
             entry.setTitle(job.getJobTitle());
             entry.setPublishedDate(job.getUpdateDate());
             description = new SyndContentImpl();
             description.setType("text/plain");
             description.setValue(job.getDescription());
             entry.setDescription(description);
             entries.add(entry);

            final String jobUrl = this.serverSettings.getServerAddress() + ServerSettings.ServerActions.JOB_DETAIL.getPath() + "?jobId=" + job.getId();

            entry.setLink(jobUrl);
         }
 
         rssFeed.setEntries(entries);
 
         return SUCCESS;
     }

    /**
     * @param serverSettings the serverSettings to set
     */
    public void setServerSettings(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }


 }
