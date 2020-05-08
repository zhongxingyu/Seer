 package org.hackystat.projectbrowser.page.loadingprocesspanel;
 
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
 import org.apache.wicket.markup.html.basic.MultiLineLabel;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.time.Duration;
 
 /**
  * Panel to show progress of a lengthy operation.
  * This panel will show when process is in progress or process end not successfully.
  * This panel will display the processingMessage.
  * This panel will update every second via Ajax when process is in progress.
  * For more detail, please see Processable interface.
  * 
  * @author Shaoxuan Zhang
  */
 public class LoadingProcessPanel extends Panel {
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
   /** the data model. */
   private Processable dataModel;
   
   /**
    * @param id the wicket component id.
    * @param model the data model associating with this panel.
    */
   // Has to suppress this warning because this constructor calls start() which calls onFinish which
   // is an overridable method. Calling start() is necessary to start the Ajax auto update function
   // if process is undergoing when every time this panel is created. The onFinish() method is not
   // actually called when constructing. It is called within the timers interface method and will be
   // called later.
   @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
   public LoadingProcessPanel(String id, Processable model) {
     super(id);
     this.dataModel = model;
     MultiLineLabel label = 
       new MultiLineLabel("processingMessage", new PropertyModel(dataModel, "processingMessage"));
     label.setOutputMarkupId(true);
     add(label);
     
     //add loading image
     Image loadingImage = new Image("loadingImage", 
         new ResourceReference(LoadingProcessPanel.class, "loading21-1.gif")) {
       /** Support serialization. */
       private static final long serialVersionUID = 1L;
       @Override
       public boolean isVisible() {
         return dataModel.isInProcess();
       }
     };
     add(loadingImage);
 
     if (dataModel.isInProcess()) {
       start();
     }
   }
 
   /**
    * start panel self update.
    */
   public final void start() {
     AjaxSelfUpdatingTimerBehavior selfUpdateBehavior = 
      new AjaxSelfUpdatingTimerBehavior(Duration.milliseconds(200)) {
       /** Support serialization. */
       private static final long serialVersionUID = 1L;
       @Override
       protected void onPostProcessTarget(AjaxRequestTarget target) {
         if (!dataModel.isInProcess()) {
           // do custom action
           onFinished(target);
           // stop the self update
           stop();
         }
       }
     };
     add(selfUpdateBehavior);
   }
   /**
    * visible when data loading is in process or data loading end with error.
    * @return true if this panel is visible.
    */
   @Override
   public boolean isVisible() {
     return dataModel.isInProcess() || !dataModel.isComplete();
   }
   
   /**
    * The method called when loading process is completed.
    * @param target an AjaxRequestTarget.
    */
   protected void onFinished(AjaxRequestTarget target) {
     //do nothing here. add implementation in subclasses.
   }
 }
