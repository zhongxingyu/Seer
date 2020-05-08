 /*
  * Created on Nov 20, 2005
  */
 package uk.org.ponder.rsf.processor;
 
 import uk.org.ponder.errorutil.TargettedMessageList;
 import uk.org.ponder.rsf.componentprocessor.ViewProcessor;
 import uk.org.ponder.rsf.preservation.StatePreservationManager;
 import uk.org.ponder.rsf.renderer.ViewRender;
 import uk.org.ponder.rsf.state.ErrorStateManager;
 import uk.org.ponder.rsf.view.View;
 import uk.org.ponder.rsf.view.ViewGenerator;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.util.RunnableWrapper;
 
 /**
  * Controls the operation of a "render cycle" of RSF. Locates component
  * producers, generates the view, performs fixups, and invokes the renderer. Any
  * errors through the construction of this bean are passed back through GetHandler.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class RSFRenderHandler implements RenderHandler {
   // all request-scope dependencies
   private ViewGenerator viewgenerator;
   private ErrorStateManager errorstatemanager;
   private RunnableWrapper getwrapper;
   private ViewProcessor viewprocessor;
   private ViewParameters viewparams;
 
   private StatePreservationManager presmanager;
   private ViewRender viewrender;
   private TargettedMessageList targettedMessageList;
 
   public void setTargettedMessageList(TargettedMessageList targettedMessageList) {
     this.targettedMessageList = targettedMessageList;
   }
 
   public void setViewGenerator(ViewGenerator viewgenerator) {
     this.viewgenerator = viewgenerator;
   }
 
   public void setErrorStateManager(ErrorStateManager errorstatemanager) {
     this.errorstatemanager = errorstatemanager;
   }
 
   public void setAlterationWrapper(RunnableWrapper getwrapper) {
     this.getwrapper = getwrapper;
   }
 
   public void setViewProcessor(ViewProcessor viewprocessor) {
     this.viewprocessor = viewprocessor;
   }
 
   public void setViewParameters(ViewParameters viewparams) {
     this.viewparams = viewparams;
   }
 
   public void setStatePreservationManager(StatePreservationManager presmanager) {
     this.presmanager = presmanager;
   }
 
   public void setViewRender(ViewRender viewrender) {
     this.viewrender = viewrender;
   }
 
   // Since this is a request-scope bean, there is no problem letting the
   // returned view from the getwrapper escape into this member.
   private View view;
 
   /**
    * The beanlocator is passed in to allow the late location of the ViewRender
    * bean which needs to occur in a controlled exception context.
    */
   public void handle(PrintOutputStream pos) {
    // *outside* alteration wrapper so that AW may be BeanFetchBracketed.
    presmanager.scopeRestore();
     getwrapper.wrapRunnable(new Runnable() {
       public void run() {
         if (viewparams.flowtoken != null) {
           presmanager.restore(viewparams.flowtoken, viewparams.endflow != null);
         }
         // this must now be AFTER restoration since the templateexpander may
         // access the model. Shucks!!
         view = viewgenerator.getView();
         // even a "read" from the model may want to cause a scope to be allocated.
         // it will have to be the user's responsibility not to violate idempotency.
         presmanager.scopePreserve();
         viewprocessor.setView(view);
         view = viewprocessor.getProcessedView();
       }
     }).run();
     viewrender.setMessages(targettedMessageList);
     // TODO: globaltargetid detection has not been investigated for a while
     viewrender
           .setGlobalMessageTarget(errorstatemanager.errorstate.globaltargetid);
     viewrender.setView(view);
     viewrender.render(pos);
   }
 
 }
