 package net.unikernel.bummel.visual_editor;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.datatransfer.Transferable;
 import javax.swing.JComponent;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import org.netbeans.api.visual.action.AcceptProvider;
 import org.netbeans.api.visual.action.ActionFactory;
 import org.netbeans.api.visual.action.ConnectorState;
 import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.vmd.VMDPinWidget;
 import org.netbeans.api.visual.widget.LayerWidget;
 import org.netbeans.api.visual.widget.Widget;
 import org.openide.nodes.Node;
 import org.openide.nodes.NodeTransfer;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author mcangel
  */
 public class CircuitGraphPinScene extends GraphPinScene<ElementNode, String, String>
 {
 	private LayerWidget mainLayer = new LayerWidget(this);
 
 	public CircuitGraphPinScene()
 	{
 		this.addChild(mainLayer);
 		
 		
 		getActions().addAction(ActionFactory.createAcceptAction(new AcceptProvider()
 		{
 			@Override
 			public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
 			{
 				Node node = NodeTransfer.node(transferable,	NodeTransfer.DND_COPY);
 				if (node != null && (node.getLookup().lookup(BasicElement.class)) != null)
 				{
 					JComponent view = getView();
 					Graphics2D g2 = (Graphics2D) view.getGraphics();
 					Rectangle visRect = view.getVisibleRect();
 					view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
 					g2.drawString(node.getDisplayName(),
 							(float) point.getLocation().getX(),
 							(float) point.getLocation().getY());
 					return ConnectorState.ACCEPT;
 				} else
 				{
 					return ConnectorState.REJECT_AND_STOP;
 				}
 			}
 
 			@Override
 			public void accept(Widget widget, Point point, Transferable transferable)
 			{
 				Node node = NodeTransfer.node(transferable, NodeTransfer.DND_COPY);
 				
 				JComponent view = getView();
 				Rectangle visRect = view.getVisibleRect();
 				view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
 
 				BasicElement el = node.getLookup().lookup(BasicElement.class);
 				ElementNode elNode;
 				try
 				{
 					elNode = new ElementNode(el.getClass().newInstance());
 					CircuitGraphPinScene.this.addNode(elNode)
 						.setPreferredLocation(widget.convertLocalToScene(point));
				for (Integer i : el.getPorts())
 				{
 					addPin(elNode, elNode.getDisplayName() + i);
 				}
 				} catch (InstantiationException ex)
 				{
 					Exceptions.printStackTrace(ex);
 				} catch (IllegalAccessException ex)
 				{
 					Exceptions.printStackTrace(ex);
 				}
 			}
 		}));
 	}
 	
 	@Override
 	protected Widget attachNodeWidget(ElementNode node)
 	{
 		ElementWidget widget = new ElementWidget(this);
 		mainLayer.addChild(widget);
 
 		widget.getActions().addAction(createSelectAction());
 		widget.getActions().addAction(ActionFactory.createMoveAction());
 
 		return widget;
 	}
 
 	@Override
 	protected Widget attachEdgeWidget(String edge)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	protected Widget attachPinWidget(ElementNode node, String pin)
 	{
 //		VMDPinWidget widget = new VMDPinWidget(this);
 //		widget.setPinName(pin);
 		ElementPortWidget widget = new ElementPortWidget(this);
 		widget.setPortName(pin);
 		((ElementWidget) findWidget(node)).attachPortWidget(widget);
 
 //		IconNodeWidget widget = new IconNodeWidget(this);
 //		widget.setLabel ("Pin: " + pin);
 //		mainLayer.addChild (widget);
 
 		widget.getActions().addAction(createObjectHoverAction());
 		widget.getActions().addAction(createSelectAction());
 
 		return widget;
 	}
 
 	@Override
 	protected void attachEdgeSourceAnchor(String edge, String oldSourcePin, String sourcePin)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	protected void attachEdgeTargetAnchor(String edge, String oldTargetPin, String targetPin)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 	
 }
