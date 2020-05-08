 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import org.restlet.data.Method;
 import org.restlet.Request;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.springframework.beans.factory.annotation.Required;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 
 /**
  * @author Saurabh Agrawal
  */
 public class SiteResource extends AbstractRemovableStorableDomainObjectResource<Site> {
     private SiteService siteService;
 
     @Override
     public void doInit() {
         super.doInit();
 
         Site site = getRequestedObjectDuringInit();
 
         addAuthorizationsFor(Method.PUT, ResourceAuthorization.create(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER, site));
         addAuthorizationsFor(Method.DELETE, ResourceAuthorization.create(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER, site));
         addAuthorizationsFor(Method.GET, site, PscRole.valuesWithSiteScoped());
     }
 
     @Override
     protected Site loadRequestedObject(Request request) {
         String assignedIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);
         return siteService.getByAssignedIdentifier(assignedIdentifier);
     }
 
     @Override
     public void remove(final Site site) {
         try {
             siteService.removeSite(site);
         } catch (Exception e) {
            String message = "Can not delete the site " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
             log.error(message, e);
         }
     }
 
     @Override
     public void store(final Site site) {
         try {
             Site existingSite = getRequestedObject();
             siteService.createOrMergeSites(existingSite, site);
         } catch (Exception e) {
            String message = "Can not update the site " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
             log.error(message, e);
         }
     }
 
     @Override
     public void verifyRemovable(final Site site) throws ResourceException {
         super.verifyRemovable(site);
         if (site == null) {
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Unknown Site Identifier " +UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()));
         }
         if (site.hasAssignments()) {
            String message = "Can not delete the site " + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()) +
                     " because site has some assignments";
             log.error(message);
 
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     message);
 
         }
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setSiteService(final SiteService siteService) {
         this.siteService = siteService;
     }
 }
