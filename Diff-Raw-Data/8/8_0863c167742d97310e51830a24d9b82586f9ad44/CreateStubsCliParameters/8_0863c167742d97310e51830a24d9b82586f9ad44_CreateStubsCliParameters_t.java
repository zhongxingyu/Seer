 package nz.org.nesi.appmanage;
 
 import com.beust.jcommander.Parameter;
 
 abstract class CreateStubsCliParameters extends AppManageModule {
 
     @Parameter(names = {"--applications"}, description = "only create documentation for the specified application(s) (comma-separated list)")
    private String applications;
 
     @Parameter(names = {"--ignore"}, description = "Blacklist (file or comma-separated list of strings) for applications to not include")
     private String ignore;
 
     @Parameter(names = {"--doc"}, description = "Create documentation stub for applications with no documentation")
     private boolean createDocumentationStub = false;
 
 
 
    public String getApplications() {
         return applications;
     }
 
     public String getIgnore() { return ignore; }
 
     public boolean isCreateDoc() { return createDocumentationStub; }
 
 
 }
