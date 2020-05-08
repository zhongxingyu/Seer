 package es.udc.cartolab.gvsig.fonsagua.config;
 
 import java.util.ArrayList;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.values.Value;
 import com.hardcode.gdbms.engine.values.ValueFactory;
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.EditionManager;
 import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.cit.gvsig.listeners.EndGeometryListener;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 
import es.udc.cartolab.gvsig.epanet.config.FonsaguaAlternative;
 import es.udc.cartolab.gvsig.epanet.exceptions.ExternalError;
 import es.udc.cartolab.gvsig.navtable.ToggleEditing;
 
 public class FonsaguaEndGeometryListener implements EndGeometryListener {
 
     @Override
     public void endGeometry(FLayer layer, String cadToolKey) {
 
 	if (cadToolKey.startsWith("_epanet_cadtool_")) {
 	    View vista = (View) PluginServices.getMDIManager()
 		    .getActiveWindow();
 	    EditionManager edMan = CADExtension.getEditionManager();
 	    vista.getMapControl().getCanceldraw().setCanceled(true);
 
 	    FLyrVect lv = (FLyrVect) layer;
 	    VectorialLayerEdited lyrEd = (VectorialLayerEdited) edMan
 		    .getActiveLayerEdited();
 
 	    try {

		// TODO: All this block of code should be changed to allow use
		// gvsig-epanet without fonsagua project
 		ArrayList<IRowEdited> selectedRow = lyrEd.getSelectedRow();
 		IRowEdited iRowEdited = selectedRow.get(0);
 		Value[] attributes = iRowEdited.getAttributes();
 		int idx = lv.getRecordset().getFieldIndexByName(
			FonsaguaAlternative.alternativePK);
 		attributes[idx] = ValueFactory
			.createValue(FonsaguaAlternative.code);
 		iRowEdited.setAttributes(attributes);
 
 		lv.getRecordset().removeSelectionListener(lyrEd);
 	    } catch (ReadDriverException e) {
 		e.printStackTrace();
 	    }
 	    try {
 		new ToggleEditing().stopEditing(layer, false);
 	    } catch (StopWriterVisitorException e) {
 		try {
 		    new ToggleEditing().stopEditing(layer, true);
 		} catch (StopWriterVisitorException e1) {
 
 		}
 		throw new ExternalError(e);
 	    }
 
 	    lv.removeLayerListener(edMan);
 
 	    vista.getMapControl().setTool("zoomIn");
 	    vista.hideConsole();
 	    // vista.repaintMap();
 	    CADExtension.clearView();
 	    PluginServices.getMainFrame().enableControls();
 	}
     }
 }
