 package pl.psnc.dl.wf4ever.portal.pages.ro;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.Resource;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.components.form.AnnotationEditAjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.components.form.AuthenticatedAjaxEventButton;
import pl.psnc.dl.wf4ever.portal.events.AddLinkEvent;
 import pl.psnc.dl.wf4ever.portal.events.ResourceSelectedEvent;
import pl.psnc.dl.wf4ever.portal.events.aggregation.DuplicateEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.ResourceDeleteClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.ResourceMoveClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.UpdateClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.annotations.CommentAddClickedEvent;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * Panel with action buttons for a selected resource.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public class ResourceActionsPanel extends EventPanel {
 
     /** id. */
     private static final long serialVersionUID = -3775797988389365540L;
 
     /** Logger. */
     @SuppressWarnings("unused")
     private static final Logger LOG = Logger.getLogger(ResourceActionsPanel.class);
 
     /** Form for the buttons. */
     private Form<Void> form;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param model
      *            selected resource model
      * @param eventBusModel
      *            event bus model for the button clicks
      */
     public ResourceActionsPanel(String id, final IModel<Resource> model, final IModel<EventBus> eventBusModel) {
         super(id, model, eventBusModel);
         setOutputMarkupPlaceholderTag(true);
 
         form = new Form<Void>("form");
         add(form);
         form.add(new ExternalLink("download", new PropertyModel<String>(model, "uri")));
         form.add(new AuthenticatedAjaxEventButton("update", form, eventBusModel, UpdateClickedEvent.class));
         form.add(new AuthenticatedAjaxEventButton("delete", form, eventBusModel, ResourceDeleteClickedEvent.class));
         form.add(new AuthenticatedAjaxEventButton("move", form, eventBusModel, ResourceMoveClickedEvent.class));
        form.add(new AuthenticatedAjaxEventButton("duplicate", form, eventBusModel, DuplicateEvent.class));
        form.add(new AuthenticatedAjaxEventButton("link-add", form, eventBusModel, AddLinkEvent.class));
         form.add(new AnnotationEditAjaxEventButton("add-comment", form, model, eventBusModel,
                 CommentAddClickedEvent.class));
         form.add(new Button("show-all"));
     }
 
 
     @Override
     protected void onConfigure() {
         super.onConfigure();
         setEnabled(getDefaultModelObject() != null);
     }
 
 
     /**
      * Refresh this panel when the selected resource changes.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onResourceSelected(ResourceSelectedEvent event) {
         event.getTarget().add(this);
     }
 
 }
