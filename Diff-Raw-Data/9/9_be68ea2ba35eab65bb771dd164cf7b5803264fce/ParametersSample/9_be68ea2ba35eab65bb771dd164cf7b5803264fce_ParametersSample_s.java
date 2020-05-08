 package com.testdroid.api.sample;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.model.APIProject;
 import com.testdroid.api.model.APITestRunConfig;
 import com.testdroid.api.model.APITestRunParameter;
 import com.testdroid.api.model.APIUser;
 import com.testdroid.api.sample.util.Common;
 
 /**
  *
 * @author Sławomir
  */
 public class ParametersSample {
 
     private static final APIClient client = Common.createApiClient();
     
     public static void main(String[] args) {
         try {
             APIUser me = client.me();
             APIProject project = me.createProject(APIProject.Type.ANDROID);
             
             project.createParameter("project_parameter", "value");
             
             APIListResource<APITestRunParameter> parameters = project.getParameters();
             
             System.out.println("Parameters of project:");            
             for(APITestRunParameter parameter: parameters.getEntity().getData()) {
                 System.out.println(String.format("%s = %s", parameter.getKey(), parameter.getValue()));
             }            
             
             APITestRunConfig testRunConfig = project.getTestRunConfig();
             
             testRunConfig.createParameter("test_run_parameter", "value");
             
             parameters = testRunConfig.getParameters();
             
             // Notice that project parameter is also listed there
             System.out.println("Test run parameters");
             for(APITestRunParameter parameter: parameters.getEntity().getData()) {
                 System.out.println(String.format("%s = %s", parameter.getKey(), parameter.getValue()));
             }
             
         } catch(APIException apie) {
             System.out.println(apie.getMessage());
         }
     }
     
 }
