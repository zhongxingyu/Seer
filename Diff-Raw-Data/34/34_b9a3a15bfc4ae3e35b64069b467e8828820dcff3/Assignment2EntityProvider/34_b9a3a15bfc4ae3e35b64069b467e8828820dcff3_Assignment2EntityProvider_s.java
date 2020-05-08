 package org.sakaiproject.assignment2.tool.entity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.azeckoski.reflectutils.DeepUtils;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.tool.DisplayUtil;
 import org.sakaiproject.entitybroker.EntityReference;
 import org.sakaiproject.entitybroker.EntityView;
 import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
 import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
 import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
 import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
 import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
 import org.sakaiproject.entitybroker.entityprovider.search.Search;
 import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
 import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
 
 
 /**
  * Entity Provider for Assn2 assignments.
  * 
  * @author sgithens
  *
  */
 public class Assignment2EntityProvider extends AbstractEntityProvider implements
 CoreEntityProvider, RESTful, RequestStorable {
 
     // Dependency
     private AssignmentLogic assignmentLogic;
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
     
     // Dependency
     private AssignmentPermissionLogic permissionLogic;
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
     
     // Dependency
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
     
     // Dependency
     private DisplayUtil displayUtil;
     public void setDisplayUtil(DisplayUtil displayUtil) {
         this.displayUtil = displayUtil;
     }
     
     public static String PREFIX = "assignment2";
     public String getEntityPrefix() {
         return PREFIX;
     }
     
     @EntityCustomAction(action="sitelist", viewKey=EntityView.VIEW_LIST)
    public List getAssignmentListForSite(EntityView view) {
        String context = "48ff42c3-9da3-4340-a8b8-5c5ad183b1d4";
         
        List<Assignment2> viewable = assignmentLogic.getViewableAssignments("48ff42c3-9da3-4340-a8b8-5c5ad183b1d4");
         
         List togo = new ArrayList();
         
         Map<Assignment2, List<String>> assignmentViewableStudentsMap = 
             permissionLogic.getViewableStudentsForUserForAssignments(externalLogic.getCurrentUserId(), context, viewable);
         
         
         
         for (Assignment2 asnn: viewable) {
             Map asnnmap = new HashMap();
             asnnmap.put("id", asnn.getId());
             asnnmap.put("title", asnn.getTitle());
             asnnmap.put("openDate", asnn.getOpenDate());
             asnnmap.put("dueDate", asnn.getDueDate());
             asnnmap.put("graded", asnn.isGraded());
             
             List<String> viewableStudents = assignmentViewableStudentsMap.get(asnn);
             
             asnnmap.put("inAndNew", displayUtil.getSubmissionStatusForAssignment(asnn, viewableStudents));
             
             togo.add(asnnmap);
         }
         
         return togo;
     }
 
     public boolean entityExists(String id) {
         boolean exists;
         try {
             assignmentLogic.getAssignmentById(new Long(id));
             exists = true;
         }
         catch (AssignmentNotFoundException anfe) {
             exists = false;
         }
         return exists;
     }
 
     public String createEntity(EntityReference ref, Object entity,
             Map<String, Object> params) {
         Assignment2 assignment = (Assignment2) entity;
         assignmentLogic.saveAssignment(assignment);
         return assignment.getId().toString();
     }
 
     public Object getSampleEntity() {
         return new Assignment2();
     }
 
     public void updateEntity(EntityReference ref, Object entity,
             Map<String, Object> params) {
         Assignment2 assignment = (Assignment2) entity;
         assignmentLogic.saveAssignment(assignment);
     }
 
     public Object getEntity(EntityReference ref) {
         Assignment2 asnn = assignmentLogic.getAssignmentByIdWithAssociatedData(new Long(ref.getId()));
         
         DeepUtils deep = DeepUtils.getInstance();
         
         return deep.deepClone(asnn, 3, new String[] {"submissionsSet",
                 "ListOfAssociatedGroupReferences","assignmentGroupSet",
                 "attachmentSet","assignmentAttachmentRefs"});
     }
 
     public void deleteEntity(EntityReference ref, Map<String, Object> params) {
         // TODO Auto-generated method stub
         
     }
 
     public List<?> getEntities(EntityReference ref, Search search) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public String[] getHandledOutputFormats() {
         return new String[] {Formats.XML, Formats.JSON, Formats.HTML };
     }
 
     public String[] getHandledInputFormats() {
         return new String[] {Formats.XML, Formats.JSON, Formats.HTML };
     }
 
    
    RequestStorage requestStorage = null;
    public void setRequestStorage(RequestStorage requestStorage) {
        this.requestStorage = requestStorage;
    }

 }
