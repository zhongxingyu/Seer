 package org.vfny.geoserver.issues;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * The issue service interface for GeoServer.
 * This service allows you to add, modify and retrieve issues relating to a specific
 * 'target'. A target is any object, like a feature or layer.
 * @note This service makes use of handlers to actually do the work
 * via an extension point design. Therefore if Issues handlers are used incorrectly 
 * you may receive duplicate issues.
  * @author quintona
  * @since 1.1.0
  */
 public interface IIssueService {
     /**
      * Adds issues to the back end
      * All the issues within this list must relate to the same target type
      * @param issues issues to added.
      */
     void addIssues(List<IIssue> issues) throws IOException;
     /**
      * Saves the issue to the storage
      * @param issue issue to save
      */
     void modifyIssue(IIssue issue) throws IOException;
     /**
      * Removes the issues from storage
      * All of these issues must be related to the same target type
      * @param issues issues to remove.
      */
     void removeIssues(Collection<IIssue> issues ) throws IOException;
     /**
      * Returns a list of issues, the issues should be ordered in the correct order.
      * @return a list of issues
      * @throws IOException
      */
     Collection< ? extends IIssue> getIssues() throws IOException;
     /**
      * Returns a list of issues, the issues should be ordered in the correct order.
      * @param target The target object
      * @return a list of issues
      * @throws IOException
      */
     Collection< ? extends IIssue> getIssues(ITarget target) throws IOException;
 }
 
