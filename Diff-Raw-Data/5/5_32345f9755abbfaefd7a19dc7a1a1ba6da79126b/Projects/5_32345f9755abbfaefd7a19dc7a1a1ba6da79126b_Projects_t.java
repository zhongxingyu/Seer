 package controllers;
 
 import java.util.List;
 import models.Project;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.i18n.Messages;
 import play.data.validation.Validation;
 import play.data.validation.Valid;
 
 
 public class Projects extends SearchableController {
     @Before
     public static void beforeAction() {
         List<Project> projects =
             Project.find("byIsActive", new Boolean(true)).fetch();
         renderArgs.put("projects", projects);
     }
 
     public static void index() {
         List<Project> entities = models.Project.all().fetch();
         render(entities);
     }
 
     public static void create(Project entity) {
         render(entity);
     }
 
     public static void show(java.lang.Long id) {
         Project entity = Project.findById(id);
         render(entity);
     }
 
     public static void edit(java.lang.Long id) {
         Project entity = Project.findById(id);
         render(entity);
     }
 
     public static void delete(java.lang.Long id) {
         Project entity = Project.findById(id);
         entity.delete();
         index();
     }
 
     public static void save(@Valid Project entity) {
         if (validation.hasErrors()) {
             flash.error(Messages.get("scaffold.validation"));
             render("@create", entity);
         }
         entity.save();
         flash.success(Messages.get("scaffold.created", "Project"));
         index();
     }
 
     public static void update(@Valid Project entity) {
         if (validation.hasErrors()) {
             flash.error(Messages.get("scaffold.validation"));
             render("@edit", entity);
         }
 
         entity = entity.merge();
 
         entity.save();
         flash.success(Messages.get("scaffold.updated", "Project"));
         index();
     }
 
     @Override
     protected Class getOwnedModel() {
         return Project.class;
     }
 
    @Override
    protected Class getOwnedModel() {
        return Project.class;
    }

 }
