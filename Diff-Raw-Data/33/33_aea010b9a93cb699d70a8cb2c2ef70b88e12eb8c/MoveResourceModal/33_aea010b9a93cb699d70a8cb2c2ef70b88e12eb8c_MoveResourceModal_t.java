 package pl.psnc.dl.wf4ever.portal.modals;
 
 import java.util.List;
 
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 import org.purl.wf4ever.rosrs.client.Folder;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.components.form.AjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.CancelClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.OkClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.AggregationChangedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.ResourceMoveClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.ResourceMoveEvent;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * A modal window for moving a resource to a folder.
  * 
  * @author piotrekhol
  * 
  */
 @SuppressWarnings("serial")
 public class MoveResourceModal extends EventPanel {
 
     /** Folder selected by the user. */
     private Folder folder = null;
 
     /** Modal window feedback panel. */
     private MyFeedbackPanel feedbackPanel;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param foldersModel
      *            a list of folders to choose from
      * @param eventBusModel
      *            bus model
      */
     public MoveResourceModal(String id, IModel<List<Folder>> foldersModel, final IModel<EventBus> eventBusModel) {
         super(id, foldersModel, eventBusModel);
         setOutputMarkupPlaceholderTag(true);
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
         form.add(new DropDownChoice<Folder>("folder", new PropertyModel<Folder>(this, "folder"), foldersModel,
                 new ChoiceRenderer<Folder>("path", "uri")));
         form.add(new AjaxEventButton("ok", form, internalEventBusModel, OkClickedEvent.class));
         form.add(new AjaxEventButton("cancel", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
         form.add(new AjaxEventButton("close", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         response.renderJavaScriptReference(new JavaScriptResourceReference(getClass(), "MoveResourceModal.js"));
     }
 
 
     /**
      * Show itself.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onResourceMoveClicked(ResourceMoveClickedEvent event) {
         event.getTarget().appendJavaScript("$('#move-resource-modal').modal('show')");
     }
 
 
     /**
      * Post an event and hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onOk(OkClickedEvent event) {
         if (folder != null) {
             eventBusModel.getObject().post(new ResourceMoveEvent(event.getTarget(), folder));
         }
        event.getTarget().prependJavaScript("$('#move-resource-modal').modal('hide');");
     }
 
 
     /**
      * Hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onCancel(CancelClickedEvent event) {
         event.getTarget().appendJavaScript("$('#move-resource-modal').modal('hide')");
     }
 
 
     /**
      * Called when the list of folders may have changed.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onAggregationChanged(AggregationChangedEvent event) {
         event.getTarget().add(this);
     }
 
 
     public Folder getFolder() {
         return folder;
     }
 
 
     public void setFolder(Folder folder) {
         this.folder = folder;
     }
 
 }
