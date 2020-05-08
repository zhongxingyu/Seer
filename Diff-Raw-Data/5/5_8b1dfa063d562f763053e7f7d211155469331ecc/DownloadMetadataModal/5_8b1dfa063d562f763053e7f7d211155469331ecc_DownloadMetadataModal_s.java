 package pl.psnc.dl.wf4ever.portal.modals;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.event.Broadcast;
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.model.PropertyModel;
 
 import pl.psnc.dl.wf4ever.portal.events.MetadataDownloadClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.MetadataDownloadEvent;
 import pl.psnc.dl.wf4ever.portal.utils.RDFFormat;
 
 /**
  * A modal window for downloading RO metadata.
  * 
  * @author piotrekhol
  * 
  */
 public class DownloadMetadataModal extends AbstractModal {
 
     /** id. */
     private static final long serialVersionUID = 69939222284940124L;
 
     /** RDF format selected by the user. */
     private RDFFormat format = RDFFormat.RDFXML;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      */
     public DownloadMetadataModal(String id) {
         super(id, "download-metadata-modal", "Download metadata");
 
         List<RDFFormat> formats = Arrays.asList(RDFFormat.RDFXML, RDFFormat.TURTLE, RDFFormat.TRIG, RDFFormat.TRIX,
             RDFFormat.N3);
         modal.add(withFocus(new DropDownChoice<RDFFormat>("rdfFormat", new PropertyModel<RDFFormat>(this, "format"),
                 formats, new IChoiceRenderer<RDFFormat>() {
 
                     /**
                      * 
                      */
                     private static final long serialVersionUID = 1736168325971226618L;
 
 
                     @Override
                     public Object getDisplayValue(RDFFormat format) {
                         return format.getName();
                     }
 
 
                     @Override
                     public String getIdValue(RDFFormat format, int index) {
                         return "" + index;
                     }
                 })));
     }
 
 
     @Override
     public void onEvent(IEvent<?> event) {
         super.onEvent(event);
        if (event instanceof MetadataDownloadClickedEvent) {
             onMetadataDownloadClicked((MetadataDownloadClickedEvent) event.getPayload());
         }
     }
 
 
     /**
      * Show itself.
      * 
      * @param event
      *            AJAX event
      */
    public void onMetadataDownloadClicked(MetadataDownloadClickedEvent event) {
         show(event.getTarget());
     }
 
 
     @Override
     public void onOk(AjaxRequestTarget target) {
         send(getPage(), Broadcast.BREADTH, new MetadataDownloadEvent(target, format));
         hide(target);
     }
 
 
     public RDFFormat getFormat() {
         return format;
     }
 
 
     public void setFormat(RDFFormat format) {
         this.format = format;
     }
 
 }
