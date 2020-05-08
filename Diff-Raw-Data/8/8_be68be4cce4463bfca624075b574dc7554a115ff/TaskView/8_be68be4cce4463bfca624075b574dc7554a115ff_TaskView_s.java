 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package bean.view;
 
 import bean.ApplicationLogger;
 import bean.facade.TaskFacade;
 import bean.view.struct.EntityView;
 import entity.Task;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Named;
 
 /**
  *
  * @author Brian GOHIER
  */
 @Named(value = "taskView")
 @SessionScoped
 public class TaskView extends EntityView<Task, TaskFacade>
 {
     public static final String[] INTERVENTION_TYPES={"LOGICIELLE","MATERIELLE"};
     
     private static final long serialVersionUID = 1L;
     @EJB
     private TaskFacade taskFacade;
     private Task entityPopup;
     
     public TaskView()
     {
         super(Task.class,"task");
     }
     
     public List<String> getInterventionTypes()
     {
         return Arrays.asList(INTERVENTION_TYPES);
     }
 
     @Override
     public String entityView(Task entity)
     {
         System.err.println("VIEW!");
         super.setCreating(false);
         super.setEditing(false);
         this.entityPopup = entity;
         return "view";
     }
     
     @Override
     public String entityCreate()
     {
         super.setCreating(true);
         super.setEditing(false);
         String message="Création d'une entité de la classe \""+Task.class.getName()+"\"";
         ApplicationLogger.writeInfo(message);
         try
         {
             this.entityPopup = Task.class.newInstance();
         }
         catch (InstantiationException ex)
         {
             ApplicationLogger.writeError("Impossible d'instancier un objet de la"
                     + " classe \""+Task.class.getName()+"\"", ex);
         }
         catch (IllegalAccessException ex)
         {
             ApplicationLogger.writeError("Droits refusés pour l'instanciation"
                     + " d'un objet de la classe \""+Task.class.getName()+"\"", ex);
         }
         return "create";
     }
     
     @Override
     public String entityUpdate(Task entity)
     {
         System.err.println("UPDATE!");
         super.setCreating(false);
         super.setEditing(true);
         this.entityPopup = entity;
         return "update";
     }
     
     @Override
     public String create()
     {
         super.setCreating(false);
         super.setEditing(false);
         this.setFacade();
         this.taskFacade.create(this.entityPopup);
         return "list";
     }
     
     @Override
     public String update()
     {
         super.setCreating(false);
         super.setEditing(false);
         this.setFacade();
         this.taskFacade.edit(this.entityPopup);
         return "list";
     }
     
     public Task getEntityPopup()
     {
         return this.entityPopup;
     }
     
     public void setEntityPopup(Task entity)
     {
         this.entityPopup = entity;
     }
 
     @Override
     public void setFacade()
     {
         super.setEntityFacade(this.taskFacade);
     }
 
     @Override
     public List<Task> getEntries()
     {
         return super.findAll();
     }
 
     @Override
     public Task getEntity()
     {
         return super.getInstance();
     }
 
     @Override
     public String getDeleteMessage(Task entity)
     {
         return "Vous êtes sur le point de supprimer définitivement"
                 + " cette tâche ("+entity.toString()
                 + " id="+entity.getId()+"). Cette action est irreversible,"
                 + " êtes-vous certain(e) de vouloir continuer?";
     }
 
     @Override
     public void setEntity(Task entity)
     {
         super.setInstance(entity);
     }
 }
