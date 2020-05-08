 package org.hackystat.projectbrowser.page.telemetry;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.model.PropertyModel;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.projectbrowser.page.ProjectBrowserBasePage;
 import org.hackystat.projectbrowser.page.telemetry.datapanel.TelemetryDataPanel;
 import org.hackystat.projectbrowser.page.telemetry.inputpanel.TelemetryInputPanel;
 
 /**
  * Provides a page with Telemetry analyses. 
  * @author Philip Johnson
  */
 public class TelemetryPage extends ProjectBrowserBasePage {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
   /** Telemetry session to hold up the state. */
   private TelemetrySession session = ProjectBrowserSession.get().getTelemetrySession();
   /** the TelemetryInputPanel in this page. */
   private TelemetryInputPanel inputPanel;
   /** the TelemetryDataPanel in this page. */
   private TelemetryDataPanel dataPanel;
   /** the LoadingProcessPanel in this page. */
   private LoadingProcessPanel loadingProcessPanel;
   
   /**
    * Constructs the telemetry page. 
    */
   public TelemetryPage() {
     inputPanel = new TelemetryInputPanel("inputPanel", this);
     inputPanel.setOutputMarkupId(true);
     add(inputPanel);
 
     
     dataPanel = new TelemetryDataPanel("dataPanel");
     dataPanel.setOutputMarkupId(true);
     add(dataPanel);
     loadingProcessPanel = new LoadingProcessPanel("loadingProcessPanel", session.getDataModel()) {
       /** Support serialization. */
       private static final long serialVersionUID = 1L;
       @Override
       protected void onFinished(AjaxRequestTarget target) {
         target.addComponent(inputPanel);
         target.addComponent(dataPanel);
         target.addComponent(this.getPage().get("FooterFeedback"));
       }
     };
     loadingProcessPanel.setOutputMarkupId(true);
    //add(loadingProcessPanel);
     this.get("FooterFeedback").setModel(new PropertyModel(session, "feedback"));
     this.get("FooterFeedback").setOutputMarkupId(true);
   }
   
   /**
    * The action to be performed when the user has set the Project and Date fields. 
    */
   @Override
   public void onProjectDateSubmit() {
     //this.replace(new TelemetryDataPanel("dataPanel"));
   }
 }
