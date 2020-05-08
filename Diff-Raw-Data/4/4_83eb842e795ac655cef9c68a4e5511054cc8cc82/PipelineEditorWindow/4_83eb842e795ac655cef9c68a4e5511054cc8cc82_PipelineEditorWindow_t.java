 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.core.pipelineBuilder.client.json.autobeans.Pipeline;
 import org.iplantc.core.pipelines.client.presenter.PipelineViewPresenter;
 import org.iplantc.core.pipelines.client.views.PipelineView;
 import org.iplantc.core.pipelines.client.views.PipelineViewImpl;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 import org.iplantc.de.client.views.windows.configs.PipelineEditorWindowConfig;
 import org.iplantc.de.client.views.windows.configs.WindowConfig;
 
 import com.google.gwt.user.client.Command;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.sencha.gxt.widget.core.client.Dialog;
 import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
 import com.sencha.gxt.widget.core.client.box.MessageBox;
 import com.sencha.gxt.widget.core.client.event.HideEvent;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
 
 public class PipelineEditorWindow extends IplantWindowBase {
     private final PipelineView.Presenter presenter;
     private String initPipelineJson;
     private boolean close_after_save;
 
     public PipelineEditorWindow(WindowConfig config) {
         super(null, null);
 
         setHeadingText(I18N.DISPLAY.pipeline());
         setSize("900", "500"); //$NON-NLS-1$ //$NON-NLS-2$
         setMinWidth(640);
         setMinHeight(440);
 
         PipelineView view = new PipelineViewImpl();
         presenter = new PipelineViewPresenter(view, new PublishCallbackCommand());
 
         if (config instanceof PipelineEditorWindowConfig) {
             PipelineEditorWindowConfig pipelineConfig = (PipelineEditorWindowConfig)config;
             Pipeline pipeline = pipelineConfig.getPipeline();
 
             if (pipeline != null) {
                 presenter.setPipeline(pipeline);
                 initPipelineJson = presenter.getPublishJson(pipeline);
             } else {
                 Splittable serviceWorkflowJson = pipelineConfig.getServiceWorkflowJson();
                if (serviceWorkflowJson != null) {
                    initPipelineJson = serviceWorkflowJson.getPayload();
                }
                 presenter.setPipeline(serviceWorkflowJson);
             }
 
         }
 
         presenter.go(this);
         close_after_save = false;
     }
 
     class PublishCallbackCommand implements Command {
         @Override
         public void execute() {
             IplantAnnouncer.getInstance().schedule(I18N.DISPLAY.publishWorkflowSuccess());
             if (close_after_save) {
                 close_after_save = false;
                 PipelineEditorWindow.super.hide();
             }
         }
 
     }
 
     @Override
     public void hide() {
         if (initPipelineJson != null
                 && !initPipelineJson.equals(presenter.getPublishJson(presenter.getPipeline()))) {
             MessageBox box = new MessageBox(I18N.DISPLAY.save(), "");
             box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CANCEL);
             box.setIcon(MessageBox.ICONS.question());
             box.setMessage(I18N.DISPLAY.unsavedChanges());
             box.addHideHandler(new HideHandler() {
 
                 @Override
                 public void onHide(HideEvent event) {
                     Dialog btn = (Dialog)event.getSource();
                     if (btn.getHideButton().getText().equalsIgnoreCase(PredefinedButton.NO.toString())) {
                         PipelineEditorWindow.super.hide();
                     }
                     if (btn.getHideButton().getText().equalsIgnoreCase(PredefinedButton.YES.toString())) {
                         presenter.saveOnClose();
                         close_after_save = true;
                     }
 
                 }
             });
             box.show();
         } else {
             PipelineEditorWindow.super.hide();
         }
     }
 
     @Override
     public WindowState getWindowState() {
         PipelineEditorWindowConfig configData = ConfigFactory.workflowIntegrationWindowConfig();
         configData.setPipeline(presenter.getPipeline());
         return createWindowState(configData);
     }
 }
