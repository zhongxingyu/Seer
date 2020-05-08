 package pl.psnc.dl.wf4ever.portal.modals;
 
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.components.form.AjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.CancelClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.OkClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.FolderAddClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.FolderAddReadyEvent;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * A modal for adding resources to the RO.
  * 
  * @author piotrekhol
  * 
  */
 @SuppressWarnings("serial")
 public class AddFolderModal extends EventPanel {
 
     /** Feedback panel. */
     private MyFeedbackPanel feedbackPanel;
 
     /** Folder name. */
     private String name;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param eventBusModel
      *            event bus
      */
     public AddFolderModal(String id, final IModel<EventBus> eventBusModel) {
         super(id, null, eventBusModel);
         Form<?> form = new Form<Void>("form");
         add(form);
 
         feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         form.add(feedbackPanel);
 
         LoadableDetachableModel<EventBus> internalEventBusModel = new LoadableDetachableModel<EventBus>() {
 
             /** id. */
             private static final long serialVersionUID = 5225667860067218852L;
 
 
             @Override
             protected EventBus load() {
                 return new EventBus();
             }
         };
         internalEventBusModel.getObject().register(this);
 
         TextField<String> nameField = new RequiredTextField<>("folder-name", new PropertyModel<String>(this, "name"));
         form.add(nameField);
 
         form.add(new AjaxEventButton("ok", form, internalEventBusModel, OkClickedEvent.class));
         form.add(new AjaxEventButton("cancel", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
         form.add(new AjaxEventButton("close", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         response.renderJavaScriptReference(new JavaScriptResourceReference(getClass(), "AddFolderModal.js"));
     }
 
 
     /**
      * Show itself.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onAddFolderClicked(FolderAddClickedEvent event) {
         event.getTarget().appendJavaScript("$('#add-folder-modal').modal('show')");
     }
 
 
     /**
      * Post an event and hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onOk(OkClickedEvent event) {
         eventBusModel.getObject().post(new FolderAddReadyEvent(event.getTarget(), name));
        event.getTarget().prependJavaScript("$('#add-folder-modal').modal('hide')");
         event.getTarget().add(feedbackPanel);
     }
 
 
     /**
      * Hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onCancel(CancelClickedEvent event) {
         event.getTarget().appendJavaScript("$('#add-folder-modal').modal('hide')");
         event.getTarget().add(feedbackPanel);
     }
 
 
     public String getName() {
         return name;
     }
 
 
     public void setName(String name) {
         this.name = name;
     }
 }
