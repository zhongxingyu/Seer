 package nl.gridshore.companyhr.app.project;
 
 import nl.gridshore.companyhr.app.api.project.ProjectCreatedEvent;
 import nl.gridshore.companyhr.app.api.project.ProjectNameChangedEvent;
 import org.axonframework.domain.AggregateIdentifier;
 import org.axonframework.eventhandling.annotation.EventHandler;
 import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
 
 /**
  * @author Jettro Coenradie
  */
public class Project extends AbstractAnnotatedAggregateRoot {
 
     public Project(AggregateIdentifier identifier) {
         super(identifier);
     }
 
     public Project(String name) {
         super();
         apply(new ProjectCreatedEvent(name));
     }
 
     public void changeName(String projectNewName) {
         apply(new ProjectNameChangedEvent(projectNewName));
     }
 
     @EventHandler
     public void onCreate(ProjectCreatedEvent event) {
         // nothing for now
     }
 
     @EventHandler
     public void onNameChange(ProjectNameChangedEvent event) {
         // nothing for now
     }
 }
