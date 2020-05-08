 package pl.psnc.dl.wf4ever.portal.modals;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 import org.apache.wicket.util.lang.Bytes;
 import org.purl.wf4ever.rosrs.client.Annotable;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.components.form.AjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.CancelClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.OkClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.annotations.ImportAnnotationClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.annotations.ImportAnnotationReadyEvent;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * Modal window for importing an annotation body.
  * 
  * @author piotrekhol
  * 
  */
 public class ImportAnnotationModal extends EventPanel {
 
     /** id. */
     private static final long serialVersionUID = 8709783939660653237L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(ImportAnnotationModal.class);
 
     /** Modal window feedback panel. */
     private MyFeedbackPanel feedbackPanel;
 
     /** The resource that is being annotated. */
     private IModel<? extends Annotable> annotableModel;
 
     /** Component for the uploaded file. */
     private FileUploadField fileUpload;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param eventBusModel
      *            bus model
      */
     public ImportAnnotationModal(String id, final IModel<EventBus> eventBusModel) {
         super(id, null, eventBusModel);
 
         Form<?> form = new Form<Void>("form");
         add(form);
 
         feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         form.add(feedbackPanel);
 
         // Enable multipart mode (need for uploads file)
         form.setMultiPart(true);
 
         // max upload size, 10k
         form.setMaxSize(Bytes.megabytes(10));
         fileUpload = new FileUploadField("fileUpload");
         form.add(fileUpload);
 
         LoadableDetachableModel<EventBus> internalEventBusModel = new LoadableDetachableModel<EventBus>() {
 
             /** id. */
             private static final long serialVersionUID = 5225667860067218852L;
 
 
             @Override
             protected EventBus load() {
                 return new EventBus();
             }
         };
         internalEventBusModel.getObject().register(this);
 
         form.add(new AjaxEventButton("ok", form, internalEventBusModel, OkClickedEvent.class));
         form.add(new AjaxEventButton("cancel", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
         form.add(new AjaxEventButton("close", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         response.renderJavaScriptReference(new JavaScriptResourceReference(getClass(), "ImportAnnotationModal.js"));
     }
 
 
     /**
      * Display this modal.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onImportAnnotationsClicked(ImportAnnotationClickedEvent event) {
         this.annotableModel = event.getAnnotableModel();
         event.getTarget().appendJavaScript("$('#import-annotation-modal').modal('show')");
     }
 
 
     /**
      * Post an event and hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onOk(OkClickedEvent event) {
         final FileUpload uploadedFile = fileUpload.getFileUpload();
         if (uploadedFile != null) {
             try {
                 eventBusModel.getObject().post(
                     new ImportAnnotationReadyEvent(event.getTarget(), annotableModel, uploadedFile));
                event.getTarget().prependJavaScript("$('#import-annotation-modal').modal('hide')");
             } catch (Exception e) {
                 LOG.error("Error when importing annotation", e);
                 error(e);
             }
         }
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
         event.getTarget().add(feedbackPanel);
         event.getTarget().appendJavaScript("$('#import-annotation-modal').modal('hide')");
     }
 
 }
