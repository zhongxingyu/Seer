 package pl.psnc.dl.wf4ever.portal.pages.ro;
 
 import java.net.URI;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 
 import pl.psnc.dl.wf4ever.portal.components.form.EditableTextPanel;
 import pl.psnc.dl.wf4ever.portal.model.wicket.AnnotationTripleModel;
 
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 /**
  * Aggregated resource panel.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public class RoSummaryPanel extends Panel {
 
     /** id. */
     private static final long serialVersionUID = -3775797988389365540L;
 
     /** Logger. */
     @SuppressWarnings("unused")
     private static final Logger LOG = Logger.getLogger(RoSummaryPanel.class);
 
     /** A message to show that this RO is aggregated by another one. */
     private WebMarkupContainer nestedRO;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param model
      *            selected resource model
      */
     public RoSummaryPanel(String id, IModel<ResearchObject> model) {
         super(id, model);
         setOutputMarkupId(true);
 
         nestedRO = new WebMarkupContainer("nested-ro");
         nestedRO.setOutputMarkupPlaceholderTag(true);
         nestedRO.add(new ExternalLink("aggregating-ro", new PropertyModel<String>(model, "aggregatingRO.toString"),
                 new PropertyModel<URI>(model, "aggregatingRO")));
         add(nestedRO);
 
         EditableTextPanel titlePanel = new EditableTextPanel("titlePanel", new AnnotationTripleModel(model,
                 URI.create(DCTerms.title.getURI()), true), false);
         titlePanel.setCanDelete(false);
         EditableTextPanel descriptionPanel = new EditableTextPanel("descriptionPanel", new AnnotationTripleModel(model,
                 URI.create(DCTerms.description.getURI()), true), true);
         descriptionPanel.setCanDelete(false);
 
         add(new ExternalLink("uri", new PropertyModel<String>(model, "uri.toString"), new PropertyModel<URI>(model,
                 "uri")));
         add(titlePanel);
         add(new Label("author", new PropertyModel<String>(model, "author.name")));
         add(new Label("createdFormatted", new PropertyModel<String>(model, "createdFormatted")));
         add(new Label("evoType.toString", new PropertyModel<String>(model, "evoType.toString")));
        // I don't know why the property model expression returns null
        //        add(new Label("resources", new PropertyModel<Integer>(model, "resources.size")));
        add(new Label("resources", model.getObject().getResources().size()));
        add(new Label("annotations", new PropertyModel<Integer>(model, "allAnnotations.size")));
         add(descriptionPanel);
     }
 
 
     @Override
     protected void onConfigure() {
         super.onConfigure();
         nestedRO.setVisible(getDefaultModelObject() != null
                 && ((ResearchObject) getDefaultModelObject()).getAggregatingRO() != null);
 
     }
 
 }
