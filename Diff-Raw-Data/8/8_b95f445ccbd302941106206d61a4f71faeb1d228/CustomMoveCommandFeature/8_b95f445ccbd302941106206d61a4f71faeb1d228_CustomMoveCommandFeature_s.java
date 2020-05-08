 package org.eclipse.bpmn2.modeler.core.features.command;
 
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 
 public class CustomMoveCommandFeature extends AbstractCustomFeature implements ICustomCommandFeature {
 
 	public CustomMoveCommandFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public void execute(ICustomContext context) {
 		int dx = 0;
 		int dy = 0;
 		int mult = 1;
 		String direction = (String) context.getProperty(COMMAND_HINT);
 		if (direction.endsWith("10")) {
 			mult = 10;
 			direction = direction.replace("10", "");
 		}
 		
 		if ("moveup".equals(direction)) 
 			dy = -1;
 		else if ("movedown".equals(direction)) 
 			dy = 1;
 		else if ("moveleft".equals(direction)) 
 			dx = -1;
 		else if ("moveright".equals(direction)) 
 			dx = 1;
 		
 		dx *= mult;
 		dy *= mult;
 		
 		PictogramElement pes[] = context.getPictogramElements();
 		for (PictogramElement pe : pes) {
 			ContainerShape cs = (ContainerShape) pe;
 			MoveShapeContext moveContext = new MoveShapeContext(cs);
 			ILocation loc = Graphiti.getPeService().getLocationRelativeToDiagram(cs);
 			moveContext.setDeltaX(dx);
 			moveContext.setDeltaY(dy);
 			moveContext.setX(loc.getX() + dx);
 			moveContext.setY(loc.getY() + dy);
 			moveContext.setSourceContainer(cs.getContainer());
 			moveContext.setTargetContainer(cs.getContainer());
 			IMoveShapeFeature f = getFeatureProvider().getMoveShapeFeature(moveContext);
 			f.moveShape(moveContext);
 		}
 	}
 
 	@Override
 	public boolean canExecute(ICustomContext context) {
 		PictogramElement pes[] = context.getPictogramElements();
 		if (pes.length==0)
 			return false;
 		Object v = context.getProperty(COMMAND_HINT);
 		if (!(v instanceof String) || !((String) v).startsWith("move"))
 			return false;
 		
 		for (PictogramElement pe : pes) {
 			if (!(pe instanceof ContainerShape))
 				return false;
 			ContainerShape cs = (ContainerShape) pe;
 			MoveShapeContext moveContext = new MoveShapeContext(cs);
 			moveContext.setSourceContainer(cs.getContainer());
 			moveContext.setTargetContainer(cs.getContainer());
 			IMoveShapeFeature f = getFeatureProvider().getMoveShapeFeature(moveContext);
 			if (!f.canMoveShape(moveContext))
 				return false;
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean isAvailable(String hint) {
 		if (hint!=null && hint.startsWith("move"))
 			return true;
 		return false;
 	}
 
 }
