 package org.eclipse.graphiti.testtool.sketch.features;
 
 import org.eclipse.graphiti.features.DefaultResizeConfiguration;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IResizeConfiguration;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
 
 public class SketchResizeShapeFeature extends DefaultResizeShapeFeature {
 
 	private static final boolean RANDOM_CAN_RESIZE = false;
 
 	public SketchResizeShapeFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public boolean canResizeShape(IResizeShapeContext context) {
 		return RANDOM_CAN_RESIZE ? Math.random() > 0.5 : super.canResizeShape(context);
 	}
 
 	@Override
	public IResizeConfiguration getResizeConfiguration(IResizeShapeContext context) {
 		return new DefaultResizeConfiguration() {
 			@Override
			public boolean isHorizontalResizeAllowed() {
 				return false;
 			}
 		};
 	}
 }
