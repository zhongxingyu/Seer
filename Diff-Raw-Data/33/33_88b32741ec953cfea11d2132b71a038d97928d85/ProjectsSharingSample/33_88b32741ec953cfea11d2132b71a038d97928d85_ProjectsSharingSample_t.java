 package com.testdroid.api.sample;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.model.APIProject;
 import com.testdroid.api.model.APIProjectSharing;
 import com.testdroid.api.model.APIUser;
 import com.testdroid.api.sample.util.Common;
 
 /**
  *
  * @author Sławomir
  */
 public class ProjectsSharingSample {
 
    public static final APIClient CLIENT = Common.createApiClient();
     
     public static void main(String[] args) {
         try {
             // Get authenticated user
             APIUser me = CLIENT.me();
             
             // Create project
             APIProject project = me.createProject(APIProject.Type.ANDROID);
             
             // Create sharing
             APIProjectSharing projectSharing = project.share("user@localhost");
             System.out.println(String.format("Project %s was shared to user with id %s", project.getName(), projectSharing.getUserId()));
             
             // Now we can login as user with mail that created project was shared to, and check if project was shared correctly
             // fe. searching that project with id
             
             APIListResource<APIProject> projects = me.getProjectsResource(0, 10, project.getId().toString(), null);
             
             for(APIProject p: projects.getEntity().getData()) {
                 System.out.println(p.getName());
                 System.out.println("Project sharings: ");
                 for(APIProjectSharing ps: p.getProjectSharings().getEntity().getData()) {
                     System.out.println(String.format("\t Shared to user with id %s", ps.getUserId()));
                 }
             }
             
         } catch (APIException apie) {
             System.err.println(apie.getMessage());
         }
     }
     
 }
