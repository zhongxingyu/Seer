 /*
  * ClassCompositionComponent.java (JQualityVis)
  * Copyright 2011 Luke Plaster. All rights reserved.
  */
 package org.lukep.javavis.ui.swing;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JComponent;
 
 import org.lukep.javavis.metrics.MetricMeasurement;
 import org.lukep.javavis.metrics.MetricRegistry;
 import org.lukep.javavis.program.generic.models.ClassModel;
 import org.lukep.javavis.program.generic.models.IGenericModelNode;
 import org.lukep.javavis.program.generic.models.MethodModel;
 import org.lukep.javavis.ui.swing.WorkspaceContext.ChangeEvent;
 import org.lukep.javavis.util.JavaVisConstants;
 
 /**
  * The Class ClassCompositionComponent.
  */
 @SuppressWarnings("serial")
 public class ClassCompositionComponent extends JComponent implements Observer {
 
 	private static final Color BACK_COLOR	= Color.gray;
 	private static final Color BORDER_COLOR = Color.black;
 	private static final Color LABEL_COLOR	= Color.white;
 	
 	protected WorkspaceContext wspContext;
 	protected IGenericModelNode currentModel;
 
 	/**
 	 * Instantiates a new class composition component.
 	 *
 	 * @param wspContext the wsp context
 	 */
 	public ClassCompositionComponent(WorkspaceContext wspContext) {
 		super();
 		this.wspContext = wspContext;
 		wspContext.addObserver(this);
 	}
 	
 	/**
 	 * Instantiates a new class composition component.
 	 *
 	 * @param modelTarget the model target
 	 */
 	public ClassCompositionComponent(IGenericModelNode modelTarget) {
 		super();
 		this.currentModel = modelTarget;
 	}
 	
 	/**
 	 * Sets the current model.
 	 *
 	 * @param model the new current model
 	 */
 	public void setCurrentModel(IGenericModelNode model) {
 		currentModel = model;
 		repaint();
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
 	 */
 	@Override
 	public void paint(Graphics g) {
 		if (currentModel != null
 				&& currentModel instanceof ClassModel) {
 			ClassModel currentClass = (ClassModel) currentModel;
 			
 			// start out with a blank canvas
 			g.setColor(BACK_COLOR);
 			g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
 			
 			// add the blocks representing methods
 			MetricMeasurement currentClassNumStatements = 
 				currentClass.getMetricMeasurement(
 					MetricRegistry.getInstance().getMetricAttribute(
 							JavaVisConstants.METRIC_NUM_OF_STATEMENTS));
 			assert(currentClassNumStatements != null);
 			int pixelsPerStatement = (int) Math.ceil(currentClassNumStatements.getResult());
 			pixelsPerStatement = pixelsPerStatement > 0 ? 
 					(int) Math.ceil(getHeight() / (double)pixelsPerStatement) : getHeight();
 			
 			int rectY, rectHeight, lastY = 0;
 			double complexity;
 			MethodModel method;
 			for (int i = 0; i < currentClass.getMethodCount(); i++) {
 				method = (MethodModel) currentClass.getMethods().get(i);
 				rectHeight = (int)(pixelsPerStatement * 
 						method.getMetricMeasurement(
 								MetricRegistry.getInstance().getMetricAttribute(
 									JavaVisConstants.METRIC_NUM_OF_STATEMENTS)).getResult());
 				rectY = lastY;
 				lastY += rectHeight;
 				g.setColor(BORDER_COLOR);
 				g.drawRect(0, rectY, getWidth() - 1, rectHeight);
 				
 				// TODO revise
 				complexity = method.getMetricMeasurement(
 						MetricRegistry.getInstance().getMetricAttribute(
 							JavaVisConstants.METRIC_CYCLO_COMPLEX)).getResult();
 				g.setColor( new Color(Math.min((int)((complexity / 30.0) * 255), 255), 170, 109) );
 				g.fillRect(0 + 1 , rectY + 1 , getWidth() - 2, rectHeight - 1);
 			}
 			g.setColor(BORDER_COLOR);
 			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
 			
 			// draw container's simple name
 			g.setColor(LABEL_COLOR);
			g.setFont(new Font("Tahoma", Font.PLAIN, 9));
			g.drawString(currentClass.getParent().getSimpleName(), 5, 10);
 			
 			// draw class name
 			int fontH = g.getFontMetrics().getHeight();
 			g.setFont(new Font("Tahoma", Font.BOLD, 10));
 			g.drawString(currentClass.getSimpleName(), 5, fontH + 10);
 		}
 		super.paint(g);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		if (ChangeEvent.SELECTED_CHANGE == (ChangeEvent) arg
 				&& wspContext.getSelectedItem() != currentModel)
 			setCurrentModel(wspContext.getSelectedItem());
 	}
 	
 }
