 package de.zib.gndms.logic.model;
 
 import de.zib.gndms.logic.action.AbstractAction;
 import de.zib.gndms.logic.action.CompositeAction;
 import de.zib.gndms.logic.action.ModelAction;
 import de.zib.gndms.model.common.GridEntity;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 
 
 /**
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  * 
  * User: mjorra, Date: 12.08.2008, Time: 16:36:20
  */
 public abstract class AbstractModelAction<M extends GridEntity, R> extends AbstractAction<R>
 	  implements ModelAction<M, R> {
 
     private M model;
 
 	private EntityManager entityManager;
 
 	private CompositeAction<Void, Object> postponedActions;
 
 	public M getModel() {
 	    return model;
 	}
 
 	public void setModel(M mdl) {
 	    model = mdl;
 	}
 
 
 	@Override
 	public void initialize() {
 		if( entityManager == null )
 		    throw new NoEntityManagerException( );
 	}
 
 
 	@Override
 	public final R execute( ) {
 		return execute(getEntityManager());
 	}
 
     public abstract R execute( @NotNull EntityManager em );
 
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
 
 	public final CompositeAction<Void, Object> getPostponedActions() {
		if (postponedActions == null) {
			final AbstractModelAction modelAction =
				  nextParentOfType(AbstractModelAction.class, getParent());
			postponedActions = modelAction == null ? null : modelAction.getPostponedActions();
		}
 		return postponedActions;
 	}
 
 
 	public final void setPostponedActions(
 		  final CompositeAction<Void, Object> postponedActionsParam) {
 		if (getPostponedActions() != null)
 			throw new IllegalStateException("Cant overwrite postponedActions");
 
 		postponedActions = postponedActionsParam;
 	}
 }
