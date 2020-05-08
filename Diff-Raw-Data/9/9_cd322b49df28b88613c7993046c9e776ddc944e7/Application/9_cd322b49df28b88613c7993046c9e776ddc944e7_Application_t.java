 package controllers;
 
 import java.util.List;
 
 import models.Project;
 import play.mvc.Controller;
 
 public class Application extends Controller {
     
     public static void index() {
         final int maxToReturn = 100;
         List<Project> projects = Project.find(
             "order by projectTitle asc"
        ).fetch(maxToReturn);
         
         render(projects);
     }
     
     
     public static void addProject(final Project newProject) {
         if (validation.valid(newProject).ok) {
             // load the Set<Token> sets from the input Strings
             newProject.initializeSets();
             newProject.save();
             flash.success("Thanks for posting");
             index();
         } else {
             if (newProject != null) {
                 deleteProject(newProject);
             }
             
             final int maxToReturn = 100;
             List<Project> projects = Project.find(
                 "order by projectTitle asc"
            ).fetch(maxToReturn);
             flash.clear();
             flash.put("projectTitle", newProject.projectTitle);
             flash.put("artist", newProject.artist);
             flash.put("myImage", newProject.myImage);
             flash.put("description", newProject.description);
             flash.put("tags", newProject.tags);
             flash.put("livingInspirations", newProject.livingInspirations);
             flash.put("pastInspirations", newProject.pastInspirations);
             flash.put(
                 "nonArtistInspirations", 
                 newProject.nonArtistInspirations
             );
             flash.put("emails", newProject.emails);
             flash.put("message", newProject.message);
             render("Application/index.html", projects);
         }
     }
     
     public static void data() {
         final int maxToReturn = 100;
         List<Project> projects = Project.find(
             "order by projectTitle asc"
        ).fetch(maxToReturn);
        System.out.println("list length found: " + projects.size());
         render(projects);
     }
     
     public static void getImage(final long id) {
         Project project = Project.findById(id);
         if (project == null) {
             return;
         }
 
         if (project.myImage == null) {
             return;
         }
         
         response.setContentTypeIfNotSet(project.myImage.type());
         renderBinary(project.myImage.get());
     }
     
     private static void deleteProject(final Project project) {
         if (project == null) {
             return;
         }
 
         if (project.myImage != null) {
             project.myImage.getFile().delete();
         }
         
         project.delete();
      }
 }
