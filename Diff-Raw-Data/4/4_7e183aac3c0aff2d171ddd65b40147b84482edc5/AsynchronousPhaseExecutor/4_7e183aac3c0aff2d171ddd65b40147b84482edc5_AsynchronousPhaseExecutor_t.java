 package com.dottydingo.service.pipeline;
 
 import java.util.concurrent.Executor;
 
 /**
  */
 public class AsynchronousPhaseExecutor<C> extends SynchronousPhaseExecutor<C> implements ContextRunnable<C>
 {
    protected PhaseRunnableFactory<C> phaseRunnableFactory = new DefaultPhaseRunnableFactory<C>();
    protected Executor executor;
 
     public void setPhaseRunnableFactory(PhaseRunnableFactory<C> phaseRunnableFactory)
     {
         this.phaseRunnableFactory = phaseRunnableFactory;
     }
 
     public void setExecutor(Executor executor)
     {
         this.executor = executor;
     }
 
     @Override
     public void execute(C phaseContext)
     {
         executor.execute(phaseRunnableFactory.createRunnable(phaseContext,this));
     }
 
     public void run(C phaseContext)
     {
         try
         {
             phase.execute(phaseContext);
 
             PhaseExecutor<C> nextPhase = nextPhaseSelector.getNextPhase(phaseContext);
             if(nextPhase != null)
                 nextPhase.execute(phaseContext);
         }
         catch (Throwable throwable)
         {
             errorHandler.handleError(phaseContext,throwable);
         }
     }
 }
