 package net.cyklotron.cms.organizations;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 
 import net.cyklotron.cms.documents.DocumentNodeResource;
 
 public interface OrganizationRegistryService
 {
     /**
      * Retrieve organizations with matching names.
      * 
      * @param substring substring to be searched within organization names.
      * @return list of organizations.
      */
     public List<Organization> getOrganizations(String substring);
 
     /**
      * Retrieve organization data.
      * 
      * @return organization with specified id.
      */
    public Organization getOrganization(long id);
 
     /**
      * Returns the contents of RSS/Atom news feed for an organization.
      * <P>
      * Content-Type header for the response should be text/xml and encoding should be UTF-8.
      * </p>
      * 
      * @param parameters request parameters, containing organization id
      * @return contents of the feed.
      */
     public String getOrganizationNewsFeed(Parameters parameters)
         throws ProcessingException;
 
     Collection<DocumentNodeResource> getOrganizationNewestDocuments(Parameters parameters,
         int limit, int offset)
         throws ProcessingException;
 }
