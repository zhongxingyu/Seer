 package org.amanzi.awe.catalog.neo;
 
 import java.io.IOException;
 
 import net.refractions.udig.catalog.IGeoResourceInfo;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.icons.IconManager;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.ImageData;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 
 /**
  * This class produces information about the CSV data stream, including the
  * bounds of the data. Currently this is done by reading the stream and
  * expanding an envelope to include all points. A better approach would be to
  * provide meta-data with the stream.
  */
 public class NeoGeoResourceInfo extends IGeoResourceInfo {
 	NeoGeoResource handle;
 	Node gisNode;
 
     public NeoGeoResourceInfo(NeoGeoResource resource, Node gisNode, IProgressMonitor monitor) throws IOException {
         NeoService service = NeoServiceProvider.getProvider().getService();
         Transaction tx = service.beginTx();
         try {
             this.handle = resource;
             this.gisNode = gisNode;
 
             GeoNeo geoNeo = handle.getGeoNeo(monitor);
             try {
                 this.name = gisNode.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                 this.title = this.name;
                 this.description = gisNode.hasProperty(INeoConstants.PROPERTY_DESCRIPTION_NAME) ? gisNode.getProperty(
                         INeoConstants.PROPERTY_DESCRIPTION_NAME).toString() : "GeoNeo Data";
                 this.bounds = geoNeo.getBounds();
             } catch (Exception e) {
                 System.err.println("Failed to determine GeoResourceInfo: " + e.getMessage());
                 e.printStackTrace(System.err);
             }
            final ImageData imageData = IconManager.getIconManager().getImage(IconManager.NETWORK_ICON).getImageData();
             this.icon = new ImageDescriptor() {
                 @Override
                 public ImageData getImageData() {
                     return imageData;
                 }
             };
         } finally {
             tx.finish();
         }
 	}
 
     @Override
     public String getTitle() {
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             return gisNode.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
         } finally {
             tx.finish();
         }
     }
 }
