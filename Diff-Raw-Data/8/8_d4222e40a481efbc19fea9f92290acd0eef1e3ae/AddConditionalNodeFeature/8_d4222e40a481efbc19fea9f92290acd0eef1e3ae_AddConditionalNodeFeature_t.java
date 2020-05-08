 package de.dubmas.modulob.state.diagram.graphiti.add;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 
 import de.dubmas.modulob.state.ConditionalNode;
 
 public class AddConditionalNodeFeature extends AbstractAddShapeFeature{
 
 	private static final IColorConstant CLASS_TEXT_FOREGROUND =
 	        new ColorConstant(51, 51, 153);
 	 
 	    private static final IColorConstant CLASS_FOREGROUND =
 	        new ColorConstant(0, 0, 0);
 	 
 	    private static final IColorConstant CLASS_BACKGROUND =
 	        new ColorConstant(255, 255, 255);
 	
 	public AddConditionalNodeFeature(IFeatureProvider fp) {
 		super(fp);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public boolean canAdd(IAddContext context) {
 		// check if user wants to add a EClass
         if (context.getNewObject() instanceof ConditionalNode) {
             // check if user wants to add to a diagram
             if (context.getTargetContainer() instanceof Diagram) {
                 return true;
             }
         }
         return false;
 	}
 
 	@Override
 	public PictogramElement add(IAddContext context) {
 		ConditionalNode addedConditionalNode = (ConditionalNode) context.getNewObject();
 		Diagram targetDiagram = (Diagram) context.getTargetContainer();
 
 		// CONTAINER SHAPE
 		IPeCreateService peCreateService = Graphiti.getPeCreateService();
 		ContainerShape containerShape    = peCreateService.createContainerShape(
 																targetDiagram, true);
 
 		// define a default size for the shape
 		IGaService gaService = Graphiti.getGaService();
 
 		// create and set graphics algorithm
         Polygon polygon = 
             this.createPolygon(containerShape);
         polygon.setForeground(manageColor(CLASS_FOREGROUND));
         polygon.setBackground(manageColor(CLASS_BACKGROUND));
         polygon.setLineWidth(2);
         gaService.setLocation(polygon, context.getX(), context.getY());
 
         // create link and wire it
         link(containerShape, addedConditionalNode);
 		
         // add a chopbox anchor to the shape
         peCreateService.createChopboxAnchor(containerShape);
         
         // create an additional box relative anchor at middle-right
         final BoxRelativeAnchor boxAnchor =
              peCreateService.createBoxRelativeAnchor(containerShape);
       
         boxAnchor.setRelativeWidth(1.0);
         boxAnchor.setRelativeHeight(0.5);
   
         // anchor references visible rectangle instead of invisible rectangle
         boxAnchor.setReferencedGraphicsAlgorithm(polygon);
   
         // assign a graphics algorithm for the box relative anchor
         Rectangle rectangle = gaService.createRectangle(boxAnchor);
         rectangle.setFilled(true);
   
         // anchor is located on the right border of the visible rectangle
         // and touches the border of the invisible rectangle
   
         int w = 6;
         gaService.setLocationAndSize(rectangle, -2 * w, -w, 2 * w, 2 * w);
         rectangle.setForeground(manageColor(CLASS_FOREGROUND));
         rectangle.setBackground(manageColor(CLASS_BACKGROUND));
         
         // call the layout feature
         layoutPictogramElement(containerShape);
 
 		return containerShape;
 	}
 	
 	private Polygon createPolygon(ContainerShape containerShape){
 		List<Point> points = new ArrayList<Point>(4);
		Point p1 = Graphiti.getGaService().createPoint(20, 0);
		Point p2 = Graphiti.getGaService().createPoint(40, 20);
		Point p3 = Graphiti.getGaService().createPoint(20, 40);
		Point p4 = Graphiti.getGaService().createPoint(0, 20);
 		points.add(p1);
 		points.add(p2);
 		points.add(p3);
 		points.add(p4);
 		Polygon p = Graphiti.getGaService().createPolygon(containerShape, points);
 		
 		return p; 
 	}
 }
