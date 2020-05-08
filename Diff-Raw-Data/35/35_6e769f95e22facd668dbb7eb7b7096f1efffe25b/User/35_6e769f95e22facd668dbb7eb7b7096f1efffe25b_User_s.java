 package nl.gridshore.companyhr.app.user;
 
 import nl.gridshore.companyhr.app.api.user.UserCreatedEvent;
 import org.axonframework.domain.AggregateIdentifier;
 import org.axonframework.domain.StringAggregateIdentifier;
 import org.axonframework.eventhandling.annotation.EventHandler;
 import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
 
 /**
  * @author Jettro Coenradie
  */
public class User extends AbstractAnnotatedAggregateRoot {
     public User(AggregateIdentifier identifier) {
         super(identifier);
     }
 
     public User(String userName, String email, String displayName, boolean administrator) {
         super(new StringAggregateIdentifier(userName));
        apply(new UserCreatedEvent(displayName,email,administrator));
     }
 
     @EventHandler
     public void handleUserCreated(UserCreatedEvent event) {
         // nothing for now
     }
 }
