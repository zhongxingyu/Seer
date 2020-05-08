 package uk.ac.ox.oucs.search2;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.user.api.UserDirectoryService;
 import uk.ac.ox.oucs.search2.filter.SearchFilter;
 import uk.ac.ox.oucs.search2.result.SearchResultList;
 
import java.util.*;
 
 /**
  * @author Colin Hebert
  */
 public abstract class AbstractSearchService implements SearchService {
     private final static Log logger = LogFactory.getLog(AbstractSearchService.class);
     private int defaultLength = 10;
     private Iterable<SearchFilter> searchFilters;
     private UserDirectoryService userDirectoryService;
     private SiteService siteService;
 
     @Override
     public SearchResultList search(String searchQuery) {
         return search(searchQuery, getAllViewableSites(), 0, defaultLength, searchFilters);
     }
 
     @Override
     public SearchResultList search(String searchQuery, Collection<String> contexts) {
         return search(searchQuery, contexts, 0, defaultLength, searchFilters);
     }
 
     @Override
     public SearchResultList search(String searchQuery, int start, int length) {
         return search(searchQuery, getAllViewableSites(), start, length, searchFilters);
     }
 
     @Override
     public SearchResultList search(String searchQuery, Collection<String> contexts, int start, int length) {
         return search(searchQuery, contexts, start, length, searchFilters);
     }
 
     protected abstract SearchResultList search(String searchQuery, Collection<String> contexts, int start, int length, Iterable<SearchFilter> filterChain);
 
     @Override
     public void setSearchFilters(Iterable<SearchFilter> searchFilters) {
         this.searchFilters = searchFilters;
     }
 
     private Collection<String> getAllViewableSites() {
         try {
             logger.info("Finding every site to in which the current user is a member.");
             String userId = userDirectoryService.getCurrentUser().getId();
 
             //TODO: Check that PUBVIEW and ACCESS aren't redundant
             List<Site> userSites = siteService.getSites(SiteService.SelectionType.ACCESS, null, null, null, null, null);
             List<Site> publicSites = siteService.getSites(SiteService.SelectionType.PUBVIEW, null, null, null, null, null);
             Collection<String> siteIds = new HashSet<String>(userSites.size() + publicSites.size() + 1);
             for (Site site : userSites) {
                 siteIds.add(site.getId());
             }
             for (Site site : publicSites) {
                 siteIds.add(site.getId());
             }
             siteIds.add(siteService.getUserSiteId(userId));
             logger.debug("Found " + siteIds.size() + " userSites: " + siteIds);
             return siteIds;
         } catch (Exception e) {
             logger.warn("Couldn't get every site for the current user.", e);
             return Collections.emptyList();
         }
     }
 
     public void setDefaultLength(int defaultLength) {
         this.defaultLength = defaultLength;
     }
 
     public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
         this.userDirectoryService = userDirectoryService;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 }
