 package de.zib.gndms.logic.model;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import com.google.inject.Inject;
 import de.zib.gndms.kit.access.RequiresCredentialProvider;
 import de.zib.gndms.kit.access.CredentialProvider;
 import de.zib.gndms.kit.configlet.ConfigletProvider;
 import de.zib.gndms.kit.util.WidAux;
 import de.zib.gndms.logic.action.LogAction;
 import de.zib.gndms.logic.model.gorfx.permissions.PermissionConfiglet;
 import de.zib.gndms.model.common.types.FilePermissions;
 import de.zib.gndms.model.gorfx.AbstractTask;
 import de.zib.gndms.model.gorfx.types.AbstractORQ;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.model.util.EntityManagerAux;
 import de.zib.gndms.model.util.TxFrame;
 import de.zib.gndms.stuff.copy.Copier;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityExistsException;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import java.io.Serializable;
 import java.util.GregorianCalendar;
 
 
 /**
  * A TaskAction is used to execute a model of type {@link AbstractTask}s.
  *
  * The method {@link #execute(EntityManager)} invokes {@code transit()} in a loop, wich changes the TaskState of the model and
  * invokes a method corresponding to the TaskSate. A concrete subclass must define the model's action
  * (on {@code IN_PROGRESS}) by implementing {@link #onInProgress(AbstractTask)}. 
  *
  *
  * @author  try ste fan pla nti kow zib
  * @version $Id$
  *
  *          User: stepn Date: 15.09.2008 Time: 11:26:48
  */
 @SuppressWarnings({ "AbstractMethodCallInConstructor" })
 public abstract class TaskAction extends AbstractModelAction<AbstractTask, AbstractTask> implements LogAction, RequiresCredentialProvider
 {
     /**
      * the ExecutionService on which this TaskAction runs
      */
     private TaskExecutionService service;
     protected Log log = LogFactory.getLog( this.getClass() );
     private String wid;
     private Class<? extends AbstractTask> taskClass;
     /**
      * A backup of the model
      */
     private AbstractTask backup;
     /**
      * an EntityManagerFactory in the case that the used EntityManager is broken
      */
 	private EntityManagerFactory emf;
     private ConfigletProvider configletProvider;
     private CredentialProvider credentialProvider;
     private boolean detached = false; ///< A detached taskAction only wraps a task which is executed within another taskAction.
                                       /// It doesn't update the task itself only represents the latest state of the task.
     private volatile boolean cancelled = false; ///< set to true if the action should be destroyed from the outside.
                                       /// If the action enters transit is stops.
 
 
     public boolean isDetached() {
         return detached;
     }
 
 
     public void setDetached( boolean detached ) {
         this.detached = detached;
     }
 
 
    public synchronized boolean getCancelled() {
         return cancelled;
     }
 
    public synchronized void setCancelled( boolean cancel ) {
        cancelled = cancel;
     }
 
 
     /**
      *
      */
     private static final class ShutdownTaskActionException extends RuntimeException {
         private static final long serialVersionUID = 2772466358157719820L;
 
         ShutdownTaskActionException(final Exception eParam) {
             super(eParam);
         }
     }
 
     /**
      * A TransitException is used to store a new desired {@code TaskState}, the model (an AbstractTask) shall use.
      * Its state can be changed using {@link AbstractTask#transit(TaskState)}.
      * Note that there is a predefined order in which state a TaskState can be changed.
      *
      * There a some special subclasses used to stop an AbstractTask in special situations.
      *
      * To stop an AbstractTask use the subclass {@link StopException}.
      * If the model has finished its computation, throw a {@link FinishedException}
      * If something went wrong, throw a {@link FailedException}
      *
      * @see TaskState
      * @see AbstractTask
      */
     protected static class TransitException extends RuntimeException {
         private static final long serialVersionUID = 1101501745642141770L;
 
         /**
          * the new, desired TaskState for the model.
          * Its state is changed using {@link AbstractTask#transit(TaskState)} 
          */
         protected final TaskState newState;
 
         /**
          * Creates a new TransitException and stores the new state, a corresponding AbstractTask shall use.
          *
          * @param newStateParam the new TaskState for an AbstractTask
          */
         protected TransitException(final @NotNull TaskState newStateParam) {
             super();
             newState = newStateParam;
         }
 
         /**
          * Creates a new TransitException and stores the new state, a corresponding AbstractTask shall use.
          * A RuntimeException must be denoted, which will be included in this RuntimeException.
          *
          * @param newStateParam the new TaskState for an AbstractTask
          * @param cause the RuntimeException, which will be included in this RuntimeException
          */
         protected TransitException(final @NotNull TaskState newStateParam,
                                    final @NotNull RuntimeException cause) {
             super(cause);
             newState = newStateParam;
         }
 
         public boolean isDemandingAbort() { return false; }
     }
 
     /**
      * A TransitException used to signalize that a failure occured during the model's computation.
      *
      * It sets {@link TransitException#newState} to {@code FAILED} and stores a RuntimeException.
      *
      */
     protected static class FailedException extends TransitException {
         private static final long serialVersionUID = -4220356706557491625L;
 
         protected FailedException(final @NotNull RuntimeException cause) {
             super(TaskState.FAILED, cause);
         }
 
         @Override
         public boolean isDemandingAbort() { return true; }
     }
 
    /**
      * A TransitException used to signalize that model's computation is finished.
      *
      * It stores the result of the computation and sets {@link TransitException#newState} to {@code FINISHED}
      */
     protected static class FinishedException extends TransitException {
         private static final long serialVersionUID = 196914329532915066L;
 
         protected final Serializable result;
 
         protected FinishedException(final Serializable resultParam) {
             super(TaskState.FINISHED);
             result = resultParam;
         }
     }
 
    /**
      * A TransitException used to signalize that the model's computation has to stop now.
     *
      */
     private static class StopException extends TransitException {
         private static final long serialVersionUID = 7783981039310846994L;
 
        /**
         * Signalizes that the TaskAction has to stop now.
         * 
         * @param newStateParam the current TaskState of the model.
         */
         protected StopException(final @NotNull TaskState newStateParam) {
             super(newStateParam);
         }
 
         @Override
         public boolean isDemandingAbort() { return true; }
     }
 
     public TaskAction() {
         super();
     }
 
     
     public TaskAction(final @NotNull EntityManager em, final @NotNull AbstractTask model) {
         super();
         initFromModel(em, model);
     }
 
     /**
      * Initializes a TaskAction by denoting an EntityManager and a model.
      *
      * The model is made persistent by the EntityManager.
      * The EntityManager and the model are stored, using {@code setOwnEntityManager()} and {@code setModelAndBackup()}.
      * A Backup of the model is done.
      *
      *
      * @param em an EntityManager, storing AbstractTasks
      * @param model an AbstractTask to be stored as model of {@code this} and to be stored in the database
      */
     public void initFromModel(final EntityManager em, AbstractTask model) {
 
         boolean wasActive = em.getTransaction().isActive();
         if (!wasActive)
             em.getTransaction().begin();
         try {
             final boolean contained = em.contains(model);
             if (! contained) {
                 try {
                     em.persist(model);
                 }
                 catch (EntityExistsException e) {
                     model = em.merge(model);
                 }
             }
             if (!wasActive)
                 em.getTransaction().commit();
             setOwnEntityManager(em);
             setModelAndBackup(model);
         }
         finally {
             if (em.getTransaction().isActive())
                 em.getTransaction().rollback();
         }
     }
 
 
     public TaskAction(final @NotNull EntityManager em, final @NotNull String pk, Class<? extends AbstractTask> cls ) {
         super();
         setTaskClass( cls );
         initFromPk(em, pk);
     }
 
     /**
      * Initializes a TaskAction by denoting an EntityManager and primary key
      *
      * Retrieves the model (an AbstractTask), managed by the EntityManager, sets it as the model of {@code this} and
      * makes a backup of it.
      *
      * The AbstractTask is select by the primary key {@code pk}. The EntityManager {@code em}
      * will be set as EntityManager for {@code this}, using {@link #setOwnEntityManager(javax.persistence.EntityManager)}.
      *
      * @param em an EntityManager, storing AbstractTasks
      * @param pk the primary key a of specific AbstractTask, which is managed by the EntityManager
      */
     public void initFromPk(final EntityManager em, final String pk) {
         boolean wasActive = em.getTransaction().isActive();
         if (!wasActive) em.getTransaction().begin();
         try {
             final AbstractTask model = em.find(getTaskClass(), pk);
             if (model == null)
                 throw new IllegalArgumentException("Model not found for pk: " + pk);
             if (!wasActive) em.getTransaction().commit();
             setOwnEntityManager(em);
             setModelAndBackup(model);
         }
         finally {
             if (em.getTransaction().isActive()) {
                 em.getTransaction().rollback();
             }
         }
     }
 
 
     @Override
     public void setModel(final @NotNull AbstractTask mdl) {
         super.setModel(mdl);    // Overridden method
         wid = mdl.getWid();
         taskClass = mdl.getClass();
     }
 
 
     public void cleanUpOnFail( final @NotNull AbstractTask model ) {
         // some actions don't need this
     }
 
 
     /**
      * Stores the model {@code mdl} and makes a backup of it using a deep copy, not a reference copy.
      *
      * @see #setModel(de.zib.gndms.model.gorfx.AbstractTask) 
      * @see #setBackup(de.zib.gndms.model.gorfx.AbstractTask) 
      * @param mdl a model to be set
      */
     protected void setModelAndBackup( final @NotNull AbstractTask mdl ) {
         setModel( mdl );
         setBackup( Copier.copy( false, mdl ) );
     }
 
 
 
     protected @NotNull Class<? extends AbstractTask> getTaskClass() {
         return taskClass;
     }
 
 
     protected void setTaskClass( Class<? extends AbstractTask> cls ) {
         taskClass = cls;
     }
 
 
     /**
      * Returns the TaskExecutionService corresponding to this TaskAction or a parent TaskAction.
      *
      * @return the TaskExecutionService corresponding to this TaskAction or a parent TaskAction.
      */
     public TaskExecutionService getService() {
         if (service == null) {
             final TaskAction taskAction = nextParentOfType(TaskAction.class);
             return taskAction == null ? null : taskAction.getService();
         }
         return service;
     }
 
 
     public void setService(final @NotNull TaskExecutionService serviceParam) {
         if (service == null)
             service = serviceParam;
         else
            throw new IllegalStateException("Can't overwrite service");
     }
 
 
     @Override
     protected final boolean isExecutingInsideTransaction() {
         return false;
     }
 
 
     @Override
     public AbstractTask call() throws RuntimeException {
         try {
             final AbstractTask task = getModel();
             if (task != null)  {
                 WidAux.initWid(getModel().getWid());
                 if( task.getOrq() != null ) {
                     WidAux.initGORFXid( ( (AbstractORQ) task.getOrq()).getActId() );
                 }
             }
             return super.call();    // Overridden method
         }
         finally {
             WidAux.removeGORFXid();
             WidAux.removeWid();
         }
     }
 
     /**
      * Invokes {@link #transit(TaskState)} in a loop, which may change the model's TaskState and invokes then a method,
      * corresponding to current {@code TaskState}.
      *
      * It stops as soon as {@code transit} throws a {@code StopException},
      * which is thrown if the model's TaskState is set to {@code FAILED} or {@code FINISHED}.
      * The model is then returned.
      *
      * An implementing class must at least define, what the model is supposed to do, when its TaskState is set to
      * {@code IN_PROGRESS}, using the method {@link #onInProgress(AbstractTask)}.
      * The methods for the other TaskState-values are already implemented (see {@link #onInitialized(AbstractTask)} for example),
      * but may be overwritten by a subclass.
      *
      * Note: Flow control in the loop is done using Exceptions
      *
      * @param em the EntityManager being executed on its persistence context.
      * @return the model after it has been executed
      */
     @SuppressWarnings({ "ThrowableInstanceNeverThrown", "ObjectAllocationInLoop" })
     @Override
     public AbstractTask execute(final @NotNull EntityManager em) {
 
         boolean first = true;
         TransitException curEx = null;
         do {
             // for debugging
             final String id = getModel().getId();
             final TaskState curState = getModel().getState();
             try {
                 try {
                     if (first) {
                         try {
 	                        final AbstractORQ orq = AbstractORQ.class.cast(getModel().getOrq());
 	                        trace( "execute() with " + orq.getLoggableDescription(), null );
                             trace("transit(null)", null);
                             transit(null);
                         }
                         finally {
                             first = false;
                         }
                     }
                     else {
                         trace("transit(" + curEx.newState + ')', curEx.getCause());
                         transit(curEx.newState);
                     }
                     // if we come here, transit has failed
                     throw new IllegalStateException("No proper TransitException thrown");
                 }
                 /* On stop: finish loop and return model to caller */
                 catch (final StopException newEx) {
                     trace("markAsDone()", null);
                     markAsDone();
                     curEx = null;
                 }
                 /* On transit: switch to next state according to newEx */
                 catch (final TransitException newEx) {
                     curEx = newEx;
                 }
                 /* On shutdown: Do not set the task to failed, simply throw e */
                 catch (final ShutdownTaskActionException e) {
                     throw e;
                 }
                 /* On runtime ex: Set task to failed */
                 catch (RuntimeException e_in) {
 	                // no joke
 	                final @NotNull RuntimeException e_out = e_in == null ? new NullPointerException() : e_in;
                     trace("catch(RuntimeException)", e_out);
                     /* Cant go to FAILED after FINISHED, i.e. onFinish must never fail */
                     if (TaskState.FINISHED.equals(getModel().getState()))
                         throw e_out;                             // todo log this one
                     else
                         fail(e_out);
                 }
             }
             /* If fail was called due to a RuntimeException in above try block, use resulting
                exception for the next state transition.  Ensures that the failed state of the
                task will be persisted.
              */
             catch (TransitException newEx) {
                 curEx = newEx;
             }
         } while (curEx != null);
         trace("return getModel()", null);
         return getModel();
     }
 
 
     @SuppressWarnings({ "HardcodedFileSeparator" })
     protected void trace(final @NotNull String userMsg, final Throwable cause) {
         final Log log1 = getLog();
         final AbstractTask model = getModel();
         final String msg;
         if (model == null)
             msg = userMsg;
         else {
             final TaskState state = model.getState();
             final String descr = model.getDescription();
             msg = "TA of AbstractTask " + model.getId()
                     + (state == null ? "" : '/' + state.toString()) + ':'
                     + (userMsg.length() > 0 ? ' ' : "") + userMsg
                     + (descr == null ? "" : " DESCR: '" + descr + '\'');
         }
        if (cause == null)
            log1.trace(msg);
         else
            log1.trace(msg, cause);
 
     }
 
     /**
      * Inform all listeners about the current model.
      */
     protected void refreshTaskResource() {
 
         try {
             getPostponedActions().getListener().onModelChange(getModel());
         }
         catch (RuntimeException e) {
             // intentionally ignored
         }
     }
 
     /**
      *
      * Retrieves the AbstractTask from the database, which corresponds to {@code getModel()).
      * It is marked as done, by invoking {@code setDone(true)} on it and restored to the database.
      *
      * It will be set as {@code this}' the new model by calling {@code setModelAndBackup()}, which also makes a backup
      * of the new model.
      *
      */
     private void markAsDone() {
         final @NotNull AbstractTask model = getModel();
        if ( ! ( getCancelled() || model.isDone() ) ) {
             final EntityManager em = getEntityManager();
             try {
                 em.getTransaction().begin();
                 AbstractTask newModel = em.find(AbstractTask.class, getModel().getId());
                 newModel.setDone(true);
                 em.getTransaction().commit();
                 setModelAndBackup(newModel);
             }  catch (RuntimeException e) {
                 trace( "Non throwable exception: ", e );
             }
             finally {
                 try{
                     if (em.getTransaction().isActive())
                         em.getTransaction().rollback();
                 } catch ( RuntimeException e ) {
                     trace( "Non throwable exception: ", e );
                 }
             }
         }
     }
 
 
     /**
      * Retrieves the current model and checks if its lifetime is already exceeded.
      * In this case the model's TaskState will be set to {@code failed} and a {@link FailedException} is thrown,
      * which will stop the the main loop in {@code execute()}.
      *
      * Otherwise, the model's TaskState is set to {@code newState} and {@link #transit(TaskState, AbstractTask)}
      * will be invoked,
      * which calls a method corresponding to the TaskState {@code newState} and throws a {@code TransitException} after its
      * execution specifying the next TaskSate value for the model.
      *
      * The model is made persistent by the EntityManager and the changed model will be 
      * commited to the database.
      * If something goes wrong while commiting the new model, a stable rollback is assured.
      *
      * @param newState the new TaskState for the model
      */
     @SuppressWarnings( { "CaughtExceptionImmediatelyRethrown", "ThrowableInstanceNeverThrown" } )
     private void transit(final TaskState newState) {
 
         if ( getCancelled() ) {
             log.debug( "cancelled" );
             throw new StopException( TaskState.IN_PROGRESS_UNKNOWN );
         }
 
         EntityManager em = getEntityManager();
         @NotNull AbstractTask model = getModel();
 
         // this throws a stop exception on timeout
         if(! ( TaskState.FINISHED.equals( newState ) || TaskState.FAILED.equals( newState ) ) )
             checkTimeout( model, em );
 
         try {
             em.getTransaction().begin();
             if (newState != null) {
                 if (! em.contains(model)) {
                     log.debug( "model not in EntityManager" );
                     try {
                         try {
                             em.persist(model);
                         }
                         catch (EntityExistsException e) {
                             log.debug( "persisting failed merging", e );
                             rewindTransaction(em.getTransaction());
                             em.merge(model);
                         }
                     }
                     catch (RuntimeException e2) {
                         log.debug( "probably persisting and merging failed", e2 );
                         rewindTransaction(em.getTransaction());
                         final @NotNull AbstractTask newModel = em.find(AbstractTask.class, model.getId());
                         model.mold(newModel);
                         newModel.refresh(em);
                         setModel( newModel );
                         model = getModel();
                         log.debug( "completed renewing of model" );
                     }
                 }
             }
 
             model.transit(newState);
 
             boolean committed = false;
             try {
                 em.getTransaction().commit();
 //                em.flush( );
                 committed = true;
             } catch ( Exception e ) {
                 log.debug( "commit of transit (" + newState.name() +") model failed ", e );
                 try {
                     rewindTransaction(em.getTransaction());
                     // if this point is reached s.th. is terribly foobared
                     // restore backup and fail
                     model = Copier.copy( false, backup );
                     em.merge( model );
                     // backup should be clean so commit mustn't fail.
                     model.fail( e );
                     em.getTransaction().commit();
                 } catch ( Exception e2 ) {
                     log.debug( "restoring previous version failed", e2 );
                     try {
                         // refresh em for final commit
                         EntityManagerAux.rollbackAndClose( em );
                     } catch( RuntimeException e3 ) {
                         log.debug( "rollback old em failed", e3 );
                     }
 
                     EntityManager nem = emf.createEntityManager();
                     TxFrame tx = new TxFrame( nem );
                     try {
                         log.debug( "loading fresh model" );
                         model = nem.find( model.getClass( ), backup.getId() );
                         boolean unkown = ( model == null );
                         model = Copier.copy( false, backup );
                         model.fail( e2 );
                         if( unkown )
                             nem.persist( model );
                    //     else
                    //         nem.merge( model );
                         tx.commit();
                         setModel( model );
                     } catch ( RuntimeException e3 ) {
                         log.debug( "exception during refresh: ", e3 );
                         throw e3;
                     } finally {
                         tx.finish();
                         setOwnEntityManager( nem );
                         em = nem;
                     }
                 }
             }
 
             // if model could be commited it has a clean state
             // refresh backup
             if( committed )
                 setBackup( Copier.copy( false, model ) );
 
             final TaskState modelState = model.getState();
             refreshTaskResource();
             //noinspection HardcodedFileSeparator
             trace("on" + modelState + "()", null);
             transit(modelState, model);
         }
         // for debugging
         catch (RuntimeException e) {
             throw e;
         }
         finally {
             try {
                 // for debuggin'
                 boolean ta = em.getTransaction().isActive();
                 if ( ta ) {
                     log.debug( "final rollback" );
                     em.getTransaction().rollback();
                 }
             } catch ( Exception e ) {
                 log.debug( "final rollback failed", e );
             }
         }
     }
 
     /**
      * Invokes a rollback on an entity transaction and a following {@code begin()},
      * only if it has been marked (using {@code setRollbackOnly()}).
      *
      * @param txParam a transaction to be rewinded
      */
     private void rewindTransaction(final EntityTransaction txParam) {
         if( txParam.isActive() ) {
             if ( txParam.getRollbackOnly()) {
                 txParam.rollback();
                 txParam.begin();
             }
         } else
             txParam.begin();
     }
 
 
     /**
      * This method is used to control and change the Taskstate of an AbstractTask.
      *
      * It checks if the {@code model}'s TaskState can be set to {@code newState}.
      * If it is allowed (according to the order given in {@link TaskState}),
      * a method corresponding to the value of {@code newState} will be invoked.
      * 
      * All these methods must throw a {@code TransitException} to specify the new desired state.
      *
      *
      * @see TaskState
      * @see #onCreated(AbstractTask)
      * @see #onInitialized(AbstractTask)
      * @see #onUnknown(AbstractTask)
      * @see #onInProgress(AbstractTask)
      * @see #onFailed(AbstractTask) 
      * @see #onFinished(AbstractTask)
      *
      * @param newState the state, the model's state should be changed to
      * @param model the model whose state should be changed.
      */
     @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
     private void transit(final @NotNull TaskState newState, final @NotNull AbstractTask model) {
         switch (model.getState().transit(newState)) {
             case CREATED: onCreated(model); break;
             case CREATED_UNKNOWN: onUnknown(model); break;
             case INITIALIZED: onInitialized(model); break;
             case INITIALIZED_UNKNOWN: onUnknown(model); break;
             case IN_PROGRESS: onInProgress(model); break;
             case IN_PROGRESS_UNKNOWN: onUnknown(model); break;
             case FAILED:
                     if (! model.isDone())
                         onFailed(model);
                     /* safety catch-all */
                     throw new StopException(TaskState.FAILED);
             case FINISHED:
                     if (! model.isDone())
                         onFinished(model);
                     /* safety catch-all */
                     throw new StopException(TaskState.FINISHED);
             default:
                 throw new IllegalStateException("Invalid or unsupported task state");
         }
     }
 
     /**
      * This method will be called by {@link #transit(TaskState)},
      * if the model's state is unknown (so {@code CREATED_UNKNOWN}, {@code INITIALIZED_UNKNOWN} or {@code IN_PROGRESS_UNKNOWN}).
      *
      * It throws an UnsupportedOperationException.
      * 
      * @param model the model, whose TaskState is set to unknown.
      *
      * @return it throws an UnsupportedOperationException
      */
     protected TaskState onUnknown(final AbstractTask model) {
         throw new UnsupportedOperationException();
     }
 
 
     /**
      * This method will be called by {@link #transit(TaskState)},
      * if the model's TaskState is set to {@code CREATED}.
      *
      * It throws a {@code TransitException} with {@code INITIALIZED} as its TaskState.
      * This signalizes to change the model's TaskState to {@code INITIALIZED}.
      *
      *
      * @param model the model, whose TaskState is set to {@code CREATED}
      */
     protected void onCreated(final AbstractTask model) {
         transitToState(TaskState.INITIALIZED);
 
     }
 
 
     /**
      * This method will be called by {@link #transit(TaskState)},
      * if the model's TaskState is set to {@code INITIALIZED}.
      *
      * It throws a {@code TransitException} with {@code IN_PROGRESS} as its TaskState.
      * This signalizes to change the model's TaskState to {@code IN_PROGRESS}.
      *
      * @param model the model, whose TaskState is set to {@code INITIALIZED}
      */
     protected void onInitialized(final AbstractTask model) {
         transitToState(TaskState.IN_PROGRESS);
     }
 
     /**
      * This method will be called by {@link #transit(TaskState)},
      * if the model's TaskState is set to {@code IN_PROGRESS}.
      *
      * Define here what the action on the model, when its {@code TaskState} is set to {@code IN_PROGRESS}.
      *
      * An implementation of this method must throw a {@code TransitException} to define to which state the model
      * shall be set to.
      *
      * @param model the model, whose TaskState is set to {@code IN_PROGRESS}
      */
     protected abstract void onInProgress(final @NotNull AbstractTask model);
 
 
    /**
      * This method will be called by {@link #transit(TaskState)},
      * if the model's TaskState is set to {@code FAILED}.
      *
      * It makes a cleanup by calling {@code tryCleanup()} and calls {@link #stop(AbstractTask)} afterwards.
      *
      * @param model the model, whose TaskState is set to {@code FAILED}
      */
     protected final void onFailed(final @NotNull AbstractTask model) {
         tryCleanup( model );
         stop(model);
     }
 
     /**
      * This method will be called by {@link #transit(TaskState)},
      * if the model's TaskState is set to {@code FINISHED}
      *
      * It calls {@link #stop(AbstractTask)} 
      *
      * @param model the model, whose TaskState is set to {@code FINISHED}
      */
     protected void onFinished(final @NotNull AbstractTask model) {
         stop(model);
     }
 
     /**
      * Throws a new {@code TransitException} with {@code newState] as its TaskState.
      *
      * Note: It does not check if the state change is allow (according to the given order by {@link TaskState}.
      *
      * @param newState the new desired TaskState for the model belonging to this
      */
     protected static void transitToState(final @NotNull TaskState newState) {
         throw new TransitException(newState);
     }
 
 
     /**
      * Use this method to signalize, that an error occured during the model's computation.
      *
      * It throws a {@link FailedException} containing the RuntimeException {@code e}.
      *
      * Sets the progress of the AbstractTask to {@code zero} and stores the Exception in
      * the model's failure String {@link AbstractTask#getFaultString()}.
      * The data of the AbstractTask will be set to {@code e} and the TaskState is set to {@code FAILED}.
      *
      * @param e a RuntimeException which occured during the model's computation
      */
     protected void fail(final @NotNull RuntimeException e) {
         getModel().fail(e);
 		//e.fillInStackTrace();
 	    // getLog().info("About to transit(FAIL) due to:", e);
         throw new FailedException(e);
     }
 
 
     /**
      * Use this method to signalize, that an model's computation is finished.
      *
      * It throws a {@link FinishedException} containing the result of the computation.
      *
      * It sets the progress of the AbstractTask to {@link AbstractTask#maxProgress} and stores the result in
      * {@link de.zib.gndms.model.gorfx.AbstractTask#getData()}.
      *
      * The model's TaskState is set to {@code FINISHED}.
      *
      * @param result the result of the the model's computation
      */
     protected void finish(final Serializable result) {
         getModel().finish(result);
         throw new FinishedException(result);
     }
 
     /**
      * Throws a StopException with and stores the model's current state in the Exception
      *
      * @param model an AbstractTask, which has to be stopped.
      */
     @SuppressWarnings({ "MethodMayBeStatic" })
     protected void stop(final @NotNull AbstractTask model) {
         throw new StopException(model.getState());
     }
 
     /**
      * If the {@code TaskExecutionService} this TaskAction runs on, is terminating or already terminated,
      * a {@link ShutdownTaskActionException} is thrown.
      *
      * @param e an InterruptedException, which will be passed to the {@code ShutdownTaskActionException}
      */
     protected final void shutdownIfTerminating(InterruptedException e) {
         if (getService().isTerminating())
             throw new ShutdownTaskActionException(e);
     }
 
     @SuppressWarnings({ "MethodMayBeStatic" })
     protected final void honorOngoingTransit(RuntimeException e) {
         if (isTransitException(e))
             throw e;
     }
 
     public static boolean isTransitException( Exception e ) {
         return e instanceof TransitException;
     }
 
 
     public static boolean isFinishedTransition( Exception e ) {
         return e instanceof FinishedException;
     }
 
 
     public static boolean isFailedTransition( Exception e ) {
         return e instanceof FailedException;
     }
 
 
     public Log getLog() {
         return log;
     }
 
 
     public void setLog(final Log logParam) {
         //log = logParam;
     }
 
     
     protected AbstractTask getBackup() {
         return backup;
     }
 
 
     protected void setBackup( AbstractTask backup ) {
         this.backup = backup;
     }
 
     /**
      * Stopps the computation of the model if the task lifetime is already exceeded.
      *
      * If the lifetime is exceeded, it sets the TaskState of the given model as well as the corresponding AbstractTask
      * in the database (if still present) to {@code FAILURE} by invoking {@code fail()}.
      *
      * @see #fail(RuntimeException)
      * @see AbstractTask#fail(Exception)
      *  
      * @param model a task with a termination time
      * @param em the entityManager storing the model
      */
     private void checkTimeout( @NotNull AbstractTask model, @NotNull EntityManager em ) {
 
         // check the task lifetime
         if( model.getTerminationTime().compareTo( new GregorianCalendar( ) ) < 1 ) {
             getLog().debug(  "Task lifetime exceeded" );
             TxFrame tx = new TxFrame( em );
             boolean containt = false;
             try {
                 // check if model is still there
                 containt = em.contains( model );
                 if( containt ) {
                     model.fail( new RuntimeException( "Task lifetime exceeded" ) );
                     getLog().debug(  "Try to persist task" );
                 }
                 tx.commit( );
             } catch ( Exception e ) {
                 // exception here  doesn't really matter
                 // task is doomed anyway
                 getLog().warn( e );
             } finally {
                 tx.finish();
             }
             // interrupt this thread
             getLog().debug(  "Stopping task thread" );
             //Thread.currentThread().interrupt();
             if( containt ) refreshTaskResource();
             fail( new RuntimeException( "Task lifetime exceeded" ) );
         }
     }
 
 
 	@Override
 	public EntityManager getEntityManager() {
 		return getOwnEntityManager();
 	}
 
 
 	public EntityManagerFactory getEmf() {
 		return emf;
 	}
 
 
 	@Inject
 	public void setEmf(final @NotNull EntityManagerFactory emfParam) {
 		emf = emfParam;
 	}
 
 
     public ConfigletProvider getConfigletProvider() {
         return configletProvider;
     }
 
 
     @Inject
     public void setConfigletProvider( ConfigletProvider configletProvider ) {
         this.configletProvider = configletProvider;
     }
 
 
     /**
      * Tries to call the {@link #cleanUpOnFail(de.zib.gndms.model.gorfx.AbstractTask)} } method for the model,
      * catches and logs possible exceptions.
      *
      * @param model The model, on which a cleanup must be done.
      */
     public void tryCleanup( @NotNull AbstractTask model )  {
 
         try{
             cleanUpOnFail( model );
         } catch ( Exception e ) {
             // don' throw them again
             getLog().debug( "Exception on task cleanup: " + e.toString() );
         }
     }
 
 
     /**
      * Extracts the actual permissions from a tasks permission info object
      *
      * @return
      */
     public FilePermissions actualPermissions( ) {
 
         if( getModel().getPermissions() != null ) {
             PermissionConfiglet pc = configletProvider.getConfiglet( PermissionConfiglet.class, getModel().getPermissions().getPermissionConfigletName() );
             if( pc != null )
                 return pc.permissionsFor( getModel().getPermissions().getUserName() );
         }
 
         return null;
     }
 
 
     public void setCredentialProvider( CredentialProvider cp ) {
         this.credentialProvider = cp;
     }
 
 
     public CredentialProvider getCredentialProvider() {
 
         return this.credentialProvider;
     }
 }
