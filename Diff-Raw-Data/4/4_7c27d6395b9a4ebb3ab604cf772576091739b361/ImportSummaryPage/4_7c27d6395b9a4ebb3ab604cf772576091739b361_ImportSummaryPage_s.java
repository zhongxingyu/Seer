 package org.geoserver.web.importer;
 
 import static org.geoserver.web.importer.ImportSummaryProvider.*;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.IModel;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.importer.ImportStatus;
 import org.geoserver.importer.ImportSummary;
 import org.geoserver.importer.LayerSummary;
 import org.geoserver.web.CatalogIconFactory;
 import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.data.layer.LayerPage;
 import org.geoserver.web.data.resource.ResourceConfigurationPage;
 import org.geoserver.web.demo.PreviewLayer;
 import org.geoserver.web.wicket.GeoServerTablePanel;
 import org.geoserver.web.wicket.ParamResourceModel;
 import org.geoserver.web.wicket.GeoServerDataProvider.Property;
 
 @SuppressWarnings("serial")
 public class ImportSummaryPage extends GeoServerSecuredPage {
 
     public ImportSummaryPage(ImportSummary summary) {
         // the synthetic results
         if(summary.getFailures() > 0) {
             add(new Label("summary", new ParamResourceModel("summaryFailures", this, summary.getTotalLayers(), summary.getFailures())));
         } else {
             add(new Label("summary", new ParamResourceModel("summarySuccess", this, summary.getTotalLayers())));
         }
 
         GeoServerTablePanel<LayerSummary> table = new GeoServerTablePanel<LayerSummary>("importSummary", new ImportSummaryProvider(
                 summary.getLayers())) {
 
             @Override
             protected Component getComponentForProperty(String id, IModel itemModel,
                     Property<LayerSummary> property) {
                 final LayerSummary layerSummary = (LayerSummary) itemModel.getObject();
                 if(property == SUCCESS) {
                     final CatalogIconFactory icons = CatalogIconFactory.get();
                     ResourceReference icon = layerSummary.getStatus().successful() ? 
                             icons.getEnabledIcon() : icons.getDisabledIcon();
                     Fragment f = new Fragment(id, "iconFragment", ImportSummaryPage.this);
                     f.add(new Image("icon", icon));
                     return f;
                 } else if(property == COMMANDS) {
                     Fragment f = new Fragment(id, "commands", ImportSummaryPage.this);
 
                     if(layerSummary.getStatus().successful()) {
                         // TODO: move the preview link generation ability to some utility object
                         PreviewLayer preview = new PreviewLayer(layerSummary.getLayer());
                         String link = preview.getWmsLink() + "&format=application/openlayers";
                         f.add(new ExternalLink("preview", link));
                     } else {
                         ExternalLink link = new ExternalLink("preview", "#");
                         link.setEnabled(false);
                         f.add(link);
                     }
                     
                     Link editLink = new Link("edit") {
 
                         @Override
                         public void onClick() {
                             Page p = new ResourceConfigurationPage(layerSummary.getLayer(), true);
                             setResponsePage(p);
                         }
                         
                     };
                     editLink.setEnabled(layerSummary.getLayer() != null);
                     f.add(editLink);
                     return f;
                 }
                 return null;
             }
 
         };
         table.setOutputMarkupId(true);
         table.setFilterable(false);
         add(table);
     }
 }
