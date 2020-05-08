 package org.jboss.jbossesb.eclipse.plugin.view.part;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.PolylineConnection;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.jboss.jbossesb.eclipse.plugin.controller.XMLElementManipulator;
 import org.jboss.jbossesb.eclipse.plugin.model.XMLAttribute;
 import org.jboss.jbossesb.eclipse.plugin.model.XMLElement;
 import org.jboss.jbossesb.eclipse.plugin.view.figure.ProviderBusFigure;
 import org.jboss.jbossesb.eclipse.plugin.view.figure.ProviderCoverFigure;
 import org.jboss.jbossesb.eclipse.plugin.view.figure.ProviderFigure;
 import org.jboss.jbossesb.eclipse.plugin.view.figure.ServiceFigure;
 import org.jboss.jbossesb.eclipse.plugin.view.policy.CommonObjectComponentEditPolicy;
 import org.jboss.jbossesb.eclipse.plugin.view.policy.CommonObjectGraphicalNodeEditPolicy;
 
 /**
  * TODO JavaDoc
  * 
  * @author Tomas Sedmik, tomas.sedmik@gmail.com
  * @since 2013-03-25
  */
 public class ServicePart extends CommonObjectPart {
 
 	private Random rand = new Random();
 
 	@Override
 	protected IFigure createFigure() {
 		return new ServiceFigure();
 	}
 
 	@Override
 	protected void createEditPolicies() {
 		installEditPolicy(EditPolicy.COMPONENT_ROLE, new CommonObjectComponentEditPolicy());
 		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new CommonObjectGraphicalNodeEditPolicy());
 	}
 
 	@Override
 	protected void refreshVisuals() {
 		
 		ServiceFigure figure = (ServiceFigure) getFigure();
 		XMLElement model = (XMLElement) getModel();
 		EditorPart parent = (EditorPart) getParent();
 
 		// set label - service name
 		figure.getLabel().setText(XMLElementManipulator.getAttrValue(model, "name"));
 		
 		setActions(figure, model);
 		setSizeAndPosition(parent, model);
 		setConnections(parent, figure, model);
 	}
 	
 	private void setSizeAndPosition(EditorPart parent, XMLElement model) {
 		
 		Rectangle layout = model.getRectangle();
 		
 		if (layout == null) {
 			layout = new Rectangle(rand.nextInt(300), rand.nextInt(300), 200, 50);
 			model.setRectangle(layout);
 		}
 		
 		parent.setLayoutConstraint(this, figure, layout);
 	}
 	
 	private void setActions(ServiceFigure figure, XMLElement model) {
 		
 		// TODO do it better. It is not necessary to remove all actions during every repaint.
 		figure.getActions().removeAll();
 		List<XMLElement> actions = XMLElementManipulator.getActions(model);
 		
 		if (actions.isEmpty()) {
 			
 			Label label = new Label();
 			label.setText("--- No Actions ---");
 			label.setFont(new Font(null, "Arial", 12, SWT.ITALIC));
 			figure.getActions().add(label);
 			
 		} else {
 			
 			int counter = 1;
 			for (XMLElement action : actions) {
 				
 				// extract name
 				String name = XMLElementManipulator
 						.getAttrValue(action, "name");
 				figure.getActions().add(new Label(counter + ". " + name));
 				counter++;
 				
 			}
 		}
 	}
 	
 	private void setConnections(EditorPart parent, ServiceFigure figure, XMLElement model) {
 	
 		List<XMLElement> children = model.getChildren();
 		
 		// get listeners
 		for (XMLElement child : children) {
 			if (child.getAddress().startsWith("/jbossesb/services/service/listeners")) {
 				children = child.getChildren();
 				break;
 			}
 		}
 		
 		// no listeners => end
 		if (children == null) {
 			return;
 		}
 		
 		for (XMLElement child : children) {
 			XMLAttribute attr = child.getAttribute("busidref"); 
 			if (attr != null) {
 				String busid = attr.getValue();
 				ProviderBusFigure target = getConnectionTarget(parent, busid);
 				if (target != null) {
 					
 					//FIXME fix adding multiple connections during refresh
 					
 					// create a connection
 					PolylineConnection c = new PolylineConnection();
 					c.setSourceAnchor(figure.getConnectionAnchor());
 					c.setTargetAnchor(target.getConnectionAnchor());
 					figure.getParent().add(c);
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private ProviderBusFigure getConnectionTarget(EditorPart parent, String busid) {
 		
 		List<IFigure> figures = parent.getFigure().getChildren();
 		
 		for (IFigure figure : figures) {
 			if (figure instanceof ProviderCoverFigure) {
 				
 				ProviderFigure provider = (ProviderFigure) figure.getChildren().get(0);
 				List<IFigure> buses = provider.getChildren();
 				
 				for (IFigure bus : buses) {
 					if (bus instanceof ProviderBusFigure) {
 						String text = ((ProviderBusFigure) bus).getLabel().getText();
 						if (text.equals(busid)) {
 							return (ProviderBusFigure) bus;
 						}
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 }
