 package org.jboss.errai.cdiwb.client.local;
 
 import java.util.List;
 
import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.api.BusErrorCallback;
 import org.jboss.errai.bus.client.api.messaging.Message;
 import org.jboss.errai.cdiwb.shared.JobDescriptor;
 import org.jboss.errai.cdiwb.shared.ServerJobService;
 import org.jboss.errai.common.client.api.Caller;
 import org.jboss.errai.common.client.api.RemoteCallback;
 import org.jboss.errai.ui.client.widget.ListWidget;
 import org.jboss.errai.ui.nav.client.local.DefaultPage;
 import org.jboss.errai.ui.nav.client.local.Page;
 import org.jboss.errai.ui.shared.api.annotations.DataField;
 import org.jboss.errai.ui.shared.api.annotations.EventHandler;
 import org.jboss.errai.ui.shared.api.annotations.Templated;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 
 @Templated("#main")
 @Page(path="home", role = DefaultPage.class)
 public class HomePage extends Composite {
 
   @Inject private Caller<ServerJobService> jobService;
 
   @Inject @DataField BusStatusWidget busStatusWidget;
 
   @Inject @DataField Button jobListRefreshButton;
   @Inject @DataField ListWidget<JobDescriptor, ServerEventWidget> runningJobsPanel;
   @Inject @DataField Label jobServiceErrorMessage;
 
   @Inject @DataField TextBox fireEventClass;
   @Inject @DataField Button startFiringButton;
 
   @Inject @DataField TextBox observerEventClass;
   @Inject @DataField Button startListeningButton;
   @Inject @DataField ListWidget<CdiObserver, CdiObserverWidget> observersPanel;
  @Inject @DataField Label observerErrorMessage;

  @PostConstruct
  private void setup() {
    jobServiceErrorMessage.setText("");
    observerErrorMessage.setText("");
  }
 
   @EventHandler("jobListRefreshButton")
   private void refreshJobList(ClickEvent e) {
     jobService.call(new RemoteCallback<List<JobDescriptor>>() {
       @Override
       public void callback(List<JobDescriptor> response) {
         List<JobDescriptor> jobs = runningJobsPanel.getValue();
         jobs.clear();
         for (JobDescriptor jd : response) {
           jobs.add(jd);
         }
       }
     },
     new BusErrorCallback() {
       @Override
       public boolean error(Message message, Throwable throwable) {
         jobServiceErrorMessage.setText(throwable.toString());
         return false;
       }
     }).getRunningJobs();
   }
 
   @EventHandler("startFiringButton")
   private void startEvents(ClickEvent e) {
     jobServiceErrorMessage.setText("");
     jobService.call(new RemoteCallback<JobDescriptor>() {
       @Override
       public void callback(JobDescriptor jd) {
         runningJobsPanel.getValue().add(jd);
         startFiringButton.setEnabled(true);
       }
     }, new BusErrorCallback() {
       @Override
       public boolean error(Message message, Throwable throwable) {
         jobServiceErrorMessage.setText(throwable.toString());
         startFiringButton.setEnabled(true);
         return false;
       }
     }).startFiringEvents(1000, fireEventClass.getText());
     startFiringButton.setEnabled(false);
   }
 
   @EventHandler("startListeningButton")
   private void createObserver(ClickEvent e) {
     CdiObserver observer = new CdiObserver(observerEventClass.getText());
     observersPanel.getValue().add(observer);
   }
 }
