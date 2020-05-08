 package org.cotrix.engine.impl;
 
 import static org.cotrix.common.Utils.*;
 import static org.cotrix.engine.impl.Task.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.Callable;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 
 import org.cotrix.action.Action;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.engine.Engine;
 import org.cotrix.engine.TaskOutcome;
 import org.cotrix.lifecycle.Lifecycle;
 import org.cotrix.lifecycle.LifecycleService;
 import org.cotrix.user.User;
 
 /**
  * Default {@link Engine} implementation
  * 
  * @author Fabio Simeoni
  *
  */
 @ApplicationScoped
 public class DefaultEngine implements Engine {
 
 	private final User user;
 	private final LifecycleService lcService;
 	
 	@Inject
 	public DefaultEngine(@Current User user,LifecycleService lcService) {
 		this.user=user;
 		this.lcService=lcService;
 	}
 	
 	@Override
 	public TaskClause perform(final Action a) {
 		return new TaskClause() {
 			
 			@Override
 			public <T> TaskOutcome<T> with(Callable<T> task) {
 				
 				return perform(a, task);
 				
 			}
 			
 			@Override
 			public TaskOutcome<Void> with(Runnable task) {
 				
 				return with(asCallable(task));
 			}
 		};
 	}
 	
 	//helpers
 	
 	private <T> TaskOutcome<T> perform(Action action, Callable<T> callable) {
 		
 		notNull("action",action);
 		notNull("task",callable);
 		
 		return action.isOnInstance()?
 					performOnInstance(action, callable):
 					performForUser(action, callable);
 					
 	}
 	
 	private <T> TaskOutcome<T> performOnInstance(Action action, Callable<T> callable) {
 		
 		Lifecycle lifecycle = lcService.lifecycleOf(action.instance());
 			
 		if (!action.isIn(lifecycle.allowed()))
			throw new IllegalStateException(user.name()+" cannot perform "+action);
 		
 		Collection<Action> permissions = user.permissions();
 		
 		T output =  perform(action, callable,permissions);
 		
 		lifecycle.notify(action);
 		
 		
 		//build next actions filtering by current user's permissions
 		Collection<Action> next = new ArrayList<Action>();
 		
 		for (Action a : lifecycle.allowed())
 			if (a.isIn(permissions))
 				next.add(a);
 		
 		return new TaskOutcome<T>(next, output);
 		
 	}
 	
 	private <T> TaskOutcome<T> performForUser(Action action, Callable<T> callable) {
 		
 		Collection<Action> permissions = user.permissions();
 		
 		T output =  perform(action,callable,permissions);
 		
 		return new TaskOutcome<T>(permissions, output);
 		
 	}
 	
 	private <T> T perform(Action action, Callable<T> callable, Collection<Action> permissions) {
 		
 		if (!action.isIn(permissions))
				throw new IllegalAccessError(user.name()+" cannot perform "+action);
 		
 		
 		Task<T> task = taskFor(callable); 
 		
 		return  task.execute(action,user);
 		
 	}
 	
 	private Callable<Void> asCallable(final Runnable task) {
 		return new Callable<Void>() {
 			@Override
 			public Void call() throws Exception {
 				task.run();
 				return null;
 			}
 		};
 	}
 }
