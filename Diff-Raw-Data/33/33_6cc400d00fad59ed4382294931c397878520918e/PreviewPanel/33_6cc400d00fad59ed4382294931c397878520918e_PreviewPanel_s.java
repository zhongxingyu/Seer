 package quizsystem.gui.panels.previewpanel;
 
import quizsystem.gui.abs.AbstractController;
import quizsystem.gui.abs.AbstractModel;
 import quizsystem.gui.abs.AbstractPanel;
 import quizsystem.gui.panels.previewpanel.view.PreviewView;
 
 public class PreviewPanel extends AbstractPanel {
 	
	public PreviewPanel(AbstractModel model, AbstractController controller) {
 		this.model = model;
 		this.controller = controller;
		view = new PreviewView(model);
 	}
 	
 }
