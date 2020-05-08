 package com.agroknow.linkchecker.service;
 
 import com.agroknow.linkchecker.domain.FileMetadata;
 import com.agroknow.linkchecker.domain.URLMetadata;
 
 import java.io.IOException;
 
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.Response.Status.Family;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class LinkCheckingService {
 
     private static final Logger LOG = LoggerFactory.getLogger(LinkCheckingService.class);
     private static final Logger LOG_URL = LoggerFactory.getLogger("url.check.log");
     private static final Logger LOG_REDIRECTION = LoggerFactory.getLogger("url.redirection.log");
 
     private static final int HTTP_TIMEOUT = 30000;
     private static final int DOMAIN_ERROR_THRESHOLD = 100;
 
     private RedirectionRulesService redirectionRulesService;
 
     public LinkCheckingService() {
     }
 
     public LinkCheckingService(RedirectionRulesService redirectionRulesService) {
         this.redirectionRulesService = redirectionRulesService;
     }
 
     public FileMetadata checkFileLocations(FileMetadata fileMeta) {
         if (CollectionUtils.isEmpty(fileMeta.getLocations())) {
             LOG.debug("File {} contains no locations. Skipping...", fileMeta);
             MetricsRegistryHolder.getCounter("FILE[NO-URL]").inc();
             return fileMeta;
         }
 
         for (URLMetadata location : fileMeta.getLocations()) {
             try {
                 checkLocation(location);
             } catch (IOException e) {
                 LOG.debug("Error checking location {}", location, e);
                 location.setStatusFamily(Family.OTHER);
                 location.setResponseStatusCode(-1);
             } catch (Exception e) {
                 LOG.error("Error checking location {}", location, e);
                 location.setStatusFamily(Family.OTHER);
                 location.setResponseStatusCode(-1);
             }
             fileMeta.setFailed(fileMeta.isFailed() || location.getStatusFamily() != Family.SUCCESSFUL);
 
             MetricsRegistryHolder.getCounter("URL.STATUS[" + location.getStatusFamily() + "]").inc();
             MetricsRegistryHolder.getCounter("DOMAIN[" + location.getDomain() + "][" + (location.getStatusFamily() != Family.SUCCESSFUL ? (location.getStatusFamily() == Family.REDIRECTION ? "REDIRECTION" : "ERROR") : "SUCCESS") + "]").inc();
             if (location.getStatusFamily() == Family.REDIRECTION) {
                LOG_REDIRECTION.debug("{}||{}||{}||{}||{}||{}", new Object[] { location.getDomain(), fileMeta.getIdentifier(), location.getUrl(), location.getStatusFamily(), location.getResponseStatusCode(), location.getRedirectsToUrl() });
             } else {
                LOG_URL.debug("{}||{}||{}||{}||{}", new Object[] { location.getDomain(), fileMeta.getIdentifier(), location.getUrl(), location.getStatusFamily(), location.getResponseStatusCode() });
             }
         }
 
         return fileMeta;
     }
 
     private void checkLocation(URLMetadata location) throws IOException {
         if (location.getStatusFamily() != null) {
             LOG.debug("Location {} already checked in previous step and found with status {}. Skipping...", location, location.getStatusFamily());
             return;
         }
 
         LOG.trace("About to check location {}", location);
 
         shouldSkipLocationCheck(location);
 
         int responseCode = Jsoup.connect(location.getUrl().toString()).followRedirects(false).ignoreHttpErrors(true).ignoreContentType(true).timeout(HTTP_TIMEOUT).maxBodySize(1).userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2").execute().statusCode();
         Status status = Status.fromStatusCode(responseCode);
 
         if (status != null && status.getFamily() == Family.REDIRECTION) {
             Connection.Response resp = Jsoup.connect(location.getUrl().toString()).followRedirects(true).ignoreHttpErrors(true).ignoreContentType(true).timeout(HTTP_TIMEOUT).maxBodySize(1).userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2").execute();
             location.setRedirectsToUrl(resp.url().toString());
             Integer redirectionRuleStatus = redirectionRulesService.isRedirectionValid(location);
             if(resp.url().toString().equalsIgnoreCase(location.getUrl().toString())) {
                 responseCode = resp.statusCode();
             }
             else if(redirectionRuleStatus != null) {
                 responseCode = redirectionRuleStatus;
             }
             status = Status.fromStatusCode(responseCode);
         }
 
         location.setStatusFamily(status == null ? Family.OTHER : status.getFamily());
         location.setResponseStatusCode(responseCode);
         LOG.trace("Location checked. Current status {}", location);
     }
 
     private boolean shouldSkipLocationCheck(URLMetadata location) {
         if(MetricsRegistryHolder.getCounter("DOMAIN[" + location.getDomain() + "][ERROR]").getCount() > DOMAIN_ERROR_THRESHOLD) {
             LOG_URL.debug("{}, {}, {}", new Object[] { location.getDomain(), location.getUrl(), "SKIPPED" });
             MetricsRegistryHolder.getCounter("URL.STATUS[SKIPPED]").inc();
             MetricsRegistryHolder.getCounter("DOMAIN[" + location.getDomain() + "][SKIPPED]").inc();
             location.setStatusFamily(Family.OTHER);
             location.setResponseStatusCode(-1);
             return true;
         }
         return false;
     }
 
     public void setRedirectionRulesService(RedirectionRulesService redirectionRulesService) {
         this.redirectionRulesService = redirectionRulesService;
     }
 }
