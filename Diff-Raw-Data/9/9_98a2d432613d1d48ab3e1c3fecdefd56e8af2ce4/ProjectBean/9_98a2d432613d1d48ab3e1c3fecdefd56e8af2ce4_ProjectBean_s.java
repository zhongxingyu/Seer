 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package roman.cwk.managedbeans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import roman.cwk.bus.BusinessException;
 import roman.cwk.bus.ProjectService;
 import roman.cwk.entity.Project;
 
 /**
  *
  * @author Roman Macor
  */
 @ManagedBean
 //@RequestScoped
 @SessionScoped
 public class ProjectBean extends BaseBean{
 
     private Project project;
     private String searchString ="";
     @EJB
     private ProjectService ejbProjectService;
 //    @ManagedProperty(value="#{registrationBean}")
 //    private RegistrationBean registration;
 
     /**
      * Creates a new instance of ProjectBean
      */
     public ProjectBean() {
     }
 
     public Project getProject() {
         if (project == null) {
             project = new Project();
         }
         return project;
     }
 
     public void setProject(Project project) {
         this.project = project;
     }
 
     public String getSearchString() {
         return searchString;
     }
 
     public void setSearchString(String searchString) {
         this.searchString = searchString;
     }
     
     
 
     public String create() {
         String organizationName = this.getLogedUsername();
         try {
             this.project = ejbProjectService.create(project, organizationName);
         } catch (BusinessException ex) {
             Logger.getLogger(ProjectBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "/project/list";
     }
 
     
 
     public List getProjectsForLogedUser() {
         String organizationName = this.getLogedUsername();
         List projects = new ArrayList();
         try {
             projects = ejbProjectService.getProjectsForUser(organizationName);
         } catch (BusinessException ex) {
             Logger.getLogger(ProjectBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
         }
         return projects;
     }
 
     public String prepareDetail(Long id) {
         try {
             project = ejbProjectService.prepareDetail(id);
         } catch (BusinessException ex) {
             Logger.getLogger(ProjectBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "detail";
     }
 
     public String prepareCreate() {
         project = new Project();
         return "/project/create";
     }
 
     public String detele(Long projectId) {
         try {
             ejbProjectService.detele(projectId);
         } catch (BusinessException ex) {
             Logger.getLogger(ProjectBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
         }
         return "/project/list";
     }
 
     public String prepareEdit(Long projectId) {
         try {
             this.project = ejbProjectService.prepareEdit(projectId);
         } catch (BusinessException ex) {
             Logger.getLogger(ProjectBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "/project/edit";
     }
     public String edit(){
         try {
             project = ejbProjectService.edit(project);
         } catch (BusinessException ex) {
             Logger.getLogger(ProjectBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
        return "/project/list";
     }
     public String prepareList(){
         searchString = "";
         return "/index";
     }
     public List<Project> getProjectsByName() {
         return ejbProjectService.getProjectsByName(searchString);
     }
     public String searchByName() {
         return "/list_projects";
     }
     
 }
