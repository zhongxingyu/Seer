 package org.mutoss;
 
 import javax.swing.JOptionPane;
 
 import org.af.commons.widgets.validate.ValidationException;
 import org.af.jhlir.call.RObj;
 import org.mutoss.gui.MuTossGUI;
 import org.mutoss.gui.MuTossMainPanel;
 
 public class MuTossObject {
 	
 	MuTossMainPanel mpanel;
 	
 	protected String objName;
 	
 	protected RObj muObj;
 	protected Model model;
 	public MuTossObject(String objName) {
 		this.objName = objName;
 		MuTossControl.getR().eval(objName+"<-new(\"Mutoss\")");
 		this.muObj = MuTossControl.getR().eval(objName);
 		this.mpanel = MuTossGUI.getGUI().getMpanel();
 	}
 	
 	public void exportAs(String exportName) throws ValidationException {		
 		MuTossControl.getR().eval(exportName+" <- "+objName+"");
 	}
 	
 	public RObj getAlpha() {		
 		return MuTossControl.getR().eval(objName+"@errorControl@alpha");
 	}
 	
 	public RObj getData() {
 		return MuTossControl.getR().eval(objName+"@data");
 	}
 	
 	public RObj getHypotheses() {		
 		return MuTossControl.getR().eval(objName+"@hypotheses");
 	}
 
 	public Model getModel() {
 		return model;
 	}
 	
 	public MuTossMainPanel getMpanel() {
 		return mpanel;
 	}
 
 	public RObj getMuObj() {
 		return muObj;
 	}
 
 	public String getObjName() {
 		return objName;
 	}
 
 	public RObj getPValues() {		
 		return MuTossControl.getR().eval(objName+"@pValues");
 	}
 
 	public boolean hasAdjPValues() {
 		return MuTossControl.getR().eval("length("+objName+"@adjPValues)!=0").asRLogical().getData()[0];
 	}
 
 	public boolean hasData() {
 		return MuTossControl.getR().eval("length("+objName+"@data)!=0").asRLogical().getData()[0];
 	}
 
 	public boolean hasErrorRate() {
 		return MuTossControl.getR().eval("length("+objName+"@errorControl@type)!=0").asRLogical().getData()[0];
 	}
 
 	public boolean hasModel() {
 		return MuTossControl.getR().eval("!is.null("+objName+"@model)").asRLogical().getData()[0];
 	}
 
 	public boolean hasP0() {
 		return MuTossControl.getR().eval("length("+objName+"@pi0)!=0").asRLogical().getData()[0];
 	}
 
 	public boolean hasPValues() {
 		return MuTossControl.getR().eval("length("+objName+"@pValues)!=0").asRLogical().getData()[0];
 	}
 
 	public boolean hasRejected() {
 		return MuTossControl.getR().eval("length("+objName+"@rejected)!=0").asRLogical().getData()[0];
 	}
 
 	public void setAdjPValues(RObj rObj, String label) {
 		MuTossControl.getR().put(".MuTossAdjPValues", rObj);
 		setAdjPValues(".MuTossAdjPValues", label);
 	}
 
 	private void setAdjPValues(String rName, String label) {
 		if (MuTossControl.getR().eval("is.numeric("+rName+")").asRLogical().getData()[0]) {
 			MuTossControl.getR().eval(objName+"@adjPValues <- "+rName+"");
 			mpanel.mlAdjPValues.setText(label);
 		} else {
 			JOptionPane.showMessageDialog(MuTossGUI.getGUI(), "The adjusted p-values have to be of type numeric.", "No numeric", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public void setAlpha(String type, Double alpha) {
 		MuTossControl.getR().eval(objName+"@errorControl <- new(Class=\"ErrorControl\", type=\""+type+"\", alpha="+alpha+")");
 		mpanel.mlErrorRate.setText(type+" (alpha="+alpha+")");		
 	}
 
 	public void setConfIntervals(RObj ci, String label) {
 		MuTossControl.getR().put(".MuTossTmpConfIntervals", ci);
 		MuTossControl.getR().eval(objName+"@confIntervals <- .MuTossTmpConfIntervals");
 		mpanel.mlCI.setText(label);		
 	}
 
 	public void setData(String rName, String label) {
 		//if (MuTossControl.getR().eval("is.data.frame("+rName+")").asRLogical().getData()[0]) {
 			MuTossControl.getR().eval(objName+"@data <- "+rName+"");
 			MuTossControl.getR().eval(objName+"@description <- \""+rName+"\"");	
 			mpanel.mlData.setText(label);
 		/*} else {
 			JOptionPane.showMessageDialog(MuTossGUI.getGUI(), "The data has to be of type data.frame.", "No data.frame", JOptionPane.ERROR_MESSAGE);
 		}*/
 	}
 
 	public void setHypotheses(RObj hypotheses) {
 		MuTossControl.getR().put(".MuTossTmpHypotheses", hypotheses);
 		MuTossControl.getR().eval(objName+"@hypotheses <- .MuTossTmpHypotheses");
 		mpanel.mlHypotheses.setText("User defined matrix");
 	}
 
 	public void setModel(RObj model, String label) {
 		MuTossControl.getR().put(".MuTossTmpModel", model);
 		MuTossControl.getR().eval(objName+"@model <- .MuTossTmpModel");		
 		mpanel.mlModel.setText(label);		
 	}
 
 	public void setModel(String rName) {
 		MuTossControl.getR().eval(objName+"@model <- "+rName+"");
 		this.model = new Model(rName);
 		mpanel.mlModel.setText((new Model(rName)).toString());
 	}
 
 	public void setPValues(RObj pValues, String label) {
 		MuTossControl.getR().put(".MuTossTmpPValues", pValues);
 		MuTossControl.getR().eval(objName+"@pValues <- .MuTossTmpPValues");
 		mpanel.mlPValues.setText(label);		
 	}
 
 	public void setPValues(String rName, String label) {
 		if (MuTossControl.getR().eval("is.numeric("+rName+")").asRLogical().getData()[0]) {
 			MuTossControl.getR().eval(objName+"@pValues <- "+rName+"");
 			mpanel.mlPValues.setText(label);
 		} else {
 			JOptionPane.showMessageDialog(MuTossGUI.getGUI(), "The p-values have to be of type numeric.", "No numeric", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public void setRejected(RObj rObj, String label) {
 		MuTossControl.getR().put(".MuTossRejected", rObj);
 		setRejected(".MuTossRejected", label);		
 	}
 
 	private void setRejected(String rName, String label) {
 		if (MuTossControl.getR().eval("is.logical("+rName+")").asRLogical().getData()[0]) {
 			MuTossControl.getR().eval(objName+"@rejected <- "+rName+"");
 			mpanel.mlRejected.setText(label);
 		} else {
 			JOptionPane.showMessageDialog(MuTossGUI.getGUI(), "The rejected Hypotheses have to be of type logical.", "No logical", JOptionPane.ERROR_MESSAGE);
 		}		
 	}
 
 	public void setStringAsHypotheses(String hypotheses) {
 		MuTossControl.getR().eval(objName+"@hypotheses <- \""+hypotheses+"\"");
 		mpanel.mlHypotheses.setText(hypotheses);
 	}
 
 	public RObj getRModel() {
 		return MuTossControl.getR().eval(objName+"@model");
 	}
 
 	public Boolean hasCI() {
 		return MuTossControl.getR().eval("length("+objName+"@confIntervals)!=0").asRLogical().getData()[0];
 	}
 
 	public Object getErrorControlType() {
 		return MuTossControl.getR().eval(""+objName+"@errorControl@type").asRChar().getData()[0];
 	}
 
 	public void setP0(RObj pi0, String label) {
 		MuTossControl.getR().put(".MuTossTmpPi0", pi0);
 		MuTossControl.getR().eval(objName+"@pi0 <- .MuTossTmpPi0");
 		double estimate = MuTossControl.getR().eval(objName+"@pi0").asRNumeric().getData()[0];
 		mpanel.mlP0.setText(label+", Estimate: "+estimate);		
 	}
 
 	public RObj getAdjPValues() {
 		return MuTossControl.getR().eval(objName+"@adjPValues");
 	}
 
 	public void setErrorControl(RObj eC, String label) {
		//TODO
		//MuTossControl.getR().put(".MuTossTmpeC", eC);
		//MuTossControl.getR().eval(objName+"@errorControl <- .MuTossTmpeC");
 		String type = MuTossControl.getR().eval(objName+"@errorControl@type").asRChar().getData()[0];	
 		mpanel.mlErrorRate.setText(type+" ()");
 	}
 	
 }
