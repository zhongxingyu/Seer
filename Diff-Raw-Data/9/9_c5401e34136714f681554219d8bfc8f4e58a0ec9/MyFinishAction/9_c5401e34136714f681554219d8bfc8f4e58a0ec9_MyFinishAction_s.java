 package com.iver.cit.gvsig.gui.cad;
 
 import jwizardcomponent.FinishAction;
 import jwizardcomponent.JWizardComponents;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.StartEditing;
 import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
 import com.iver.cit.gvsig.fmap.MapControl;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.fmap.edition.rules.IRule;
 import com.iver.cit.gvsig.fmap.edition.rules.RulePolygon;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.gui.cad.createLayer.NewLayerWizard;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 
 public class MyFinishAction extends FinishAction {
     JWizardComponents myWizardComponents;
     FinishAction oldAction;
     ITableDefinition lyrDef = null;
     View view;
     private NewLayerWizard wizard;
 
     public MyFinishAction(JWizardComponents wizardComponents, View view,
 	    NewLayerWizard wizard) {
 	super(wizardComponents);
 	oldAction = wizardComponents.getFinishAction();
 	myWizardComponents = wizardComponents;
 	this.view = view;
 	this.wizard = wizard;
 	// TODO Auto-generated constructor stub
     }
 
     @Override
     public void performAction() {
 	FLyrVect lyr = null;
 	MapControl mapCtrl = view.getMapControl();
 	try {
 	    mapCtrl.getMapContext().beginAtomicEvent();
 	    lyr = wizard.createLayer(mapCtrl.getProjection());
 	} catch (Exception e) {
	    NotificationManager.showMessageError(e.getLocalizedMessage(), e);
 	    return;
 	}
 
 	if (lyr == null) {
 	    return;
 	}
 
 	lyr.setVisible(true);
 
 	mapCtrl.getMapContext().getLayers().addLayer(lyr);
 
 	mapCtrl.getMapContext().endAtomicEvent();
 	lyr.addLayerListener(CADExtension.getEditionManager());
 	lyr.setActive(true);
 
 	try {
 	    lyr.setEditing(true);
 	    VectorialEditableAdapter vea = (VectorialEditableAdapter) lyr
 		    .getSource();
 	    vea.getRules().clear();
 	    // TODO: ESTO ES PROVISIONAL, DESCOMENTAR LUEGO
 	    if (vea.getShapeType() == FShape.POLYGON) {
 		IRule rulePol = new RulePolygon();
 		vea.getRules().add(rulePol);
 	    }
 	    StartEditing.startCommandsApplicable(view, lyr);
 	    vea.getCommandRecord().addCommandListener(mapCtrl);
 	    view.showConsole();
 
 	    // Para cerrar el cuadro de dilogo.
 	    oldAction.performAction();
 	} catch (ReadDriverException e) {
 	    NotificationManager.addError(e);
 	} catch (StartEditionLayerException e) {
 	    NotificationManager.addError(e);
 	}
 
     }
 }
