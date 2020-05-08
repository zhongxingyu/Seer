 package pl.psnc.dl.wf4ever.portal.modals;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.components.form.AjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.CancelClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.MetadataDownloadClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.MetadataDownloadEvent;
 import pl.psnc.dl.wf4ever.portal.events.OkClickedEvent;
 import pl.psnc.dl.wf4ever.portal.utils.RDFFormat;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * A modal window for downloading RO metadata.
  * 
  * @author piotrekhol
  * 
  */
 @SuppressWarnings("serial")
 public class DownloadMetadataModal extends EventPanel {
 
     /** RDF format selected by the user. */
     private RDFFormat format = RDFFormat.RDFXML;
 
     /** Modal window feedback panel. */
     private MyFeedbackPanel feedbackPanel;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param eventBusModel
      *            bus model
      */
     public DownloadMetadataModal(String id, final IModel<EventBus> eventBusModel) {
         super(id, null, eventBusModel);
 
         Form<?> form = new Form<Void>("downloadMetadataForm");
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
 
         List<RDFFormat> formats = Arrays.asList(RDFFormat.RDFXML, RDFFormat.TURTLE, RDFFormat.TRIG, RDFFormat.TRIX,
             RDFFormat.N3);
         DropDownChoice<RDFFormat> formatDropDown = new DropDownChoice<RDFFormat>("rdfFormat",
                 new PropertyModel<RDFFormat>(this, "format"), formats, new IChoiceRenderer<RDFFormat>() {
 
                     @Override
                     public Object getDisplayValue(RDFFormat format) {
                         return format.getName();
                     }
 
 
                     @Override
                     public String getIdValue(RDFFormat format, int index) {
                         return "" + index;
                     }
                 });
         form.add(formatDropDown);
         form.add(new AjaxEventButton("ok", form, internalEventBusModel, OkClickedEvent.class));
         form.add(new AjaxEventButton("cancel", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
         form.add(new AjaxEventButton("close", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         response.renderJavaScriptReference(new JavaScriptResourceReference(getClass(), "DownloadMetadataModal.js"));
     }
 
 
     /**
      * Show itself.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onMetadataDownloadClicked(MetadataDownloadClickedEvent event) {
         event.getTarget().appendJavaScript("$('#download-metadata-modal').modal('show')");
     }
 
 
     /**
      * Post an event and hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onOk(OkClickedEvent event) {
         eventBusModel.getObject().post(new MetadataDownloadEvent(event.getTarget(), format));
        event.getTarget().prependJavaScript("$('#download-metadata-modal').modal('hide');");
     }
 
 
     /**
      * Hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onCancel(CancelClickedEvent event) {
         event.getTarget().appendJavaScript("$('#download-metadata-modal').modal('hide')");
     }
 
 
     public RDFFormat getFormat() {
         return format;
     }
 
 
     public void setFormat(RDFFormat format) {
         this.format = format;
     }
 
 }
