 package net.unikernel.bummel.visual_editor;
 
 import com.kitfox.svg.SVGException;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import org.netbeans.api.visual.layout.LayoutFactory;
 import org.netbeans.api.visual.widget.Scene;
 import org.netbeans.api.visual.widget.Widget;
 /**
  *
  * @author mcangel
  */
 public class ElementWidget extends Widget implements PropertyChangeListener
 {
 	private Widget body;
 	private Widget imageWidget;
 	private ElementNode elNode;
 	private Map<Integer, SvgWidget> stateImages;
 	int width = 20;
 	int height = 20;
 
 	public ElementWidget(Scene scene, ElementNode element) throws MalformedURLException, SVGException
 	{
 		super(scene);
 		this.elNode = element;
 		this.elNode.getLookup().lookup(BasicElement.class)
 				.addPropertyChangeListener(this);
 		stateImages = new HashMap<>();
 		body = new Widget(scene);
 		body.setCheckClipping(true);	//to prevent element "pointing" on dragging
 		body.setLayout(LayoutFactory.createVerticalFlowLayout()); //all child widgets will be located at their preffered location
                 addChild(body);
				imageWidget = new SvgWidget(scene, this.elNode.getGraphicsURL(this.elNode.getLookup().lookup(BasicElement.class).getState()));
 				if(((SvgWidget)imageWidget).getDiagram() != null)
 				{
					stateImages.put(this.elNode.getLookup().lookup(BasicElement.class).getState(),
							(SvgWidget)imageWidget);
 					width = imageWidget.getPreferredBounds().width;
 					height = imageWidget.getPreferredBounds().height;
 				}
 				else
 				{
 					imageWidget = new Widget(scene){
 						@Override
 						protected Rectangle calculateClientArea()
 						{
 							return new Rectangle(-width/2, -height/2, width+1, height+1);
 						}
 
 						@Override
 						protected void paintWidget()
 						{
 							Graphics2D g = getGraphics();
 							g.setColor(getForeground());
 							g.drawOval(-width/2, -height/2, width, height);
 						}
 					};
 				}
 		body.addChild(imageWidget);
 	}
 	
 	/**
 	 * Attaches a pin widget to the node widget.
 	 *
 	 * @param widget the pin widget
 	 */
 	public void attachPortWidget(ElementPortWidget widget)
 	{
 		widget.setCheckClipping(true);
 		addChild(widget);
 		switch (elNode.getPortDirection(widget.getPort()))
 		{
 			case "right":
 				widget.setPreferredLocation(new Point(width, 
 							//offset from the middle, offset in "percents" of the height half
 							(int) (height*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2)));
 				break;
 			case "left":
 				widget.setPreferredLocation(new Point(0, 
 							//offset from the middle, offset in "percents" of the height half
 							(int) (height*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2)));
 				break;
 			case "up":
 				widget.setPreferredLocation(new Point((int) (width*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2),height));
 				break;
 			case "down":
 				widget.setPreferredLocation(new Point((int) (width*(1+elNode.getPortOffset(widget.getPort()).doubleValue())/2),0));
 				break;
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt)
 	{
 		if(BasicElement.PROP_STATE.equals(evt.getPropertyName()))
 		{
 			if(!stateImages.containsKey((Integer)evt.getNewValue()))
 			{
 				SvgWidget newImage = new SvgWidget(this.getScene(), this.elNode.getGraphicsURL((Integer)evt.getNewValue()));
 				if(((SvgWidget)imageWidget).getDiagram() != null)
 				{
 					stateImages.put((Integer)evt.getNewValue(), newImage);
 					width = imageWidget.getPreferredBounds().width;
 					height = imageWidget.getPreferredBounds().height;
 				}
 			}
 			if(stateImages.containsKey((Integer)evt.getNewValue()))
 			{
 				imageWidget.removeFromParent();
 				imageWidget = stateImages.get((Integer)evt.getNewValue());
 				body.addChild(imageWidget);
 				this.revalidate();
 				this.repaint();
 			}
 		}
 	}
 }
