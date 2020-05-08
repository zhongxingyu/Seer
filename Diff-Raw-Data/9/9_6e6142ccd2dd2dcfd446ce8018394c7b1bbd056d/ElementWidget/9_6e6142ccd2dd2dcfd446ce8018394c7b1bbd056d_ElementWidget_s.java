 package net.unikernel.bummel.visual_editor;
 
 import com.kitfox.svg.SVGDiagram;
 import com.kitfox.svg.SVGException;
 import com.kitfox.svg.SVGUniverse;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import org.netbeans.api.visual.layout.LayoutFactory;
 import org.netbeans.api.visual.widget.LabelWidget;
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
 	private LabelWidget labelWidget;
 	private LabelWidget stateWidget;
 	private ElementNode elNode;
 	private SVGDiagram diagram;
 	int width = 20;
 	int height = 20;
 
 	public ElementWidget(Scene scene, ElementNode element) throws MalformedURLException, SVGException, FileNotFoundException
 	{
 		super(scene);
 		this.elNode = element;
 		this.elNode.getLookup().lookup(BasicElement.class)
 				.addPropertyChangeListener(this);
 		
 		body = new Widget(scene);
 		body.setCheckClipping(true);	//to prevent element "pointing" on dragging
 		body.setLayout(LayoutFactory.createVerticalFlowLayout()); //all child widgets will be located at their preffered location
 //                body.setBorder(BorderFactory.createLineBorder(Color.red));//TODO:remove this for the release
                 addChild(body);
                 SVGUniverse svgUniverse = new SVGUniverse();
 				if(this.elNode.getGraphicsURL(0) != null)
 					diagram = svgUniverse.getDiagram(svgUniverse.loadSVG(this.elNode.getGraphicsURL(0)));
 				if(diagram != null)
 				{
 					imageWidget = new SvgWidget(scene, diagram);
 					width = (int) diagram.getWidth();
 					height = (int) diagram.getHeight();
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
 		body.addChild(new LabelWidget(scene, "Label:"+elNode.getDisplayName()));
 		body.addChild(new LabelWidget(scene, "State:"+elNode.getLookup().lookup(BasicElement.class).getState()));
                 body.repaint();//first repaint must be called ater dropping.
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
							(int) (height*0.5*(1+0.5*elNode.getPortOffset(widget.getPort()).doubleValue()))));
 				break;
 			case "left":
 				widget.setPreferredLocation(new Point(0, 
 							//offset from the middle, offset in "percents" of the height half
							(int) (height*0.5*(1+0.5*elNode.getPortOffset(widget.getPort()).doubleValue()))));
 				break;
 			case "up":
 				break;
 			case "down":
 				break;
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt)
 	{
 		if(BasicElement.PROP_STATE.equals(evt.getPropertyName()))
 		{
 			stateWidget.setLabel("State:"+evt.getNewValue());
 		}
 	}
 }
