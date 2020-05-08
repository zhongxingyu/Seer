 package es.udc.cartolab.gvsig.fonsagua.forms.factories;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
 
 import es.icarto.gvsig.navtableforms.BasicAbstractForm;
 import es.icarto.gvsig.navtableforms.gui.tables.TableFormFactory;
 import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
 import es.udc.cartolab.gvsig.fonsagua.FonsaguaConstants;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.AbastecimientosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.BombeosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.CaptacionesForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.DepDistribucionForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.DepIntermediosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.SingletonAbastecimientosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento.TuberiasForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.AmenazasForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.AreasPotencialesRiegoForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.CentrosEducativosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.CentrosSaludForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.ComunidadesForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.FuentesContaminacionForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.OtrosServiciosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.PuntosViviendasForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.SingletonComunidadesForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.fuentes.FuentesForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.fuentes.SingletonFuentesForm;
 import es.udc.cartolab.gvsig.users.utils.DBSession;
 
 public class FonsaguaTableFormFactory implements TableFormFactory {
 
     private static FonsaguaTableFormFactory instance = null;
 
     private static final Set<String> mainFormNames = new HashSet<String>();
 
     static {
 	mainFormNames.add(ComunidadesForm.NAME);
 	mainFormNames.add(AmenazasForm.NAME);
 	mainFormNames.add(PuntosViviendasForm.NAME);
 	mainFormNames.add(AreasPotencialesRiegoForm.NAME);
 	mainFormNames.add(CentrosSaludForm.NAME);
 	mainFormNames.add(OtrosServiciosForm.NAME);
 	mainFormNames.add(CentrosEducativosForm.NAME);
 	mainFormNames.add(AbastecimientosForm.NAME);
 	mainFormNames.add(BombeosForm.NAME);
 	mainFormNames.add(CaptacionesForm.NAME);
 	mainFormNames.add(DepIntermediosForm.NAME);
 	mainFormNames.add(DepDistribucionForm.NAME);
 	mainFormNames.add(TuberiasForm.NAME);
 	mainFormNames.add(FuentesForm.NAME);
 	mainFormNames.add(FuentesContaminacionForm.NAME);
     }
 
     public static FonsaguaTableFormFactory getInstance() {
 	if (instance == null) {
 	    instance = new FonsaguaTableFormFactory();
 	}
 	return instance;
     }
 
     private FonsaguaTableFormFactory() {
     }
 
     @Override
     public BasicAbstractForm createForm(FLyrVect layer) {
 	if (layer != null) {
 	    if (layer.getName().equals(ComunidadesForm.NAME)) {
 		return new ComunidadesForm(layer);
 	    } else if (layer.getName().equals(AmenazasForm.NAME)) {
 		return new AmenazasForm(layer);
 	    } else if (layer.getName().equals(PuntosViviendasForm.NAME)) {
 		return new PuntosViviendasForm(layer);
 	    } else if (layer.getName().equals(AreasPotencialesRiegoForm.NAME)) {
 		return new AreasPotencialesRiegoForm(layer);
 	    } else if (layer.getName().equals(CentrosSaludForm.NAME)) {
 		return new CentrosSaludForm(layer);
 	    } else if (layer.getName().equals(OtrosServiciosForm.NAME)) {
 		return new OtrosServiciosForm(layer);
 	    } else if (layer.getName().equals(CentrosEducativosForm.NAME)) {
 		return new CentrosEducativosForm(layer);
 	    } else if (layer.getName().equals(AbastecimientosForm.NAME)) {
 		return new AbastecimientosForm(layer);
 	    } else if (layer.getName().equals(BombeosForm.NAME)) {
 		return new BombeosForm(layer);
 	    } else if (layer.getName().equals(CaptacionesForm.NAME)) {
 		return new CaptacionesForm(layer);
 	    } else if (layer.getName().equals(DepIntermediosForm.NAME)) {
 		return new DepIntermediosForm(layer);
 	    } else if (layer.getName().equals(DepDistribucionForm.NAME)) {
 		return new DepDistribucionForm(layer);
 	    } else if (layer.getName().equals(TuberiasForm.NAME)) {
 		return new TuberiasForm(layer);
 	    } else if (layer.getName().equals(FuentesForm.NAME)) {
 		return new FuentesForm(layer);
 	    } else if (layer.getName().equals(FuentesContaminacionForm.NAME)) {
 		return new FuentesContaminacionForm(layer);
 	    }
 	}
 	return null;
     }
 
     @Override
     public BasicAbstractForm createSingletonForm(FLyrVect layer) {
 	if (layer != null) {
 	    if (layer.getName().equals(ComunidadesForm.NAME)) {
 		return new SingletonComunidadesForm(layer);
 	    } else if (layer.getName().equals(FuentesForm.NAME)) {
 		return new SingletonFuentesForm(layer);
 	    } else if (layer.getName().equals(AbastecimientosForm.NAME)) {
 		return new SingletonAbastecimientosForm(layer);
 	    }
 	}
 	return null;
     }
 
     @Override
     public boolean hasMainForm(String layerName) {
 	return mainFormNames.contains(layerName);
     }
 
     @Override
     public boolean allLayersLoaded() {
 	for (String layer : mainFormNames) {
 	    if (new TOCLayerManager().getLayerByName(layer) == null) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     @Override
     public void checkLayerLoaded(String layerName) {
 	// We check whether the secondary layer is loaded
 	if (new TOCLayerManager().getLayerByName(layerName) == null) {
 	    // If it isn't, we retrieve the current view,
 	    // load the layer and add it
 	    IWindow[] windows = PluginServices.getMDIManager()
 		    .getOrderedWindows();
 	    BaseView view = null;
 	    for (IWindow w : windows) {
 		if (w instanceof BaseView) {
 		    view = (BaseView) w;
 		    break;
 		}
 	    }
 	    try {
 		view.getMapControl()
 			.getMapContext()
 			.getLayers()
 			.addLayer(
 				DBSession.getCurrentSession().getLayer(
					layerName,
					FonsaguaConstants.dataSchema,
 					view.getProjection()));
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }
 	}
     }
 
 }
