 /*
 * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.gmfgraph.util;
 
 import org.eclipse.emf.common.util.UniqueEList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.common.codegen.ImportAssistant;
 import org.eclipse.gmf.gmfgraph.BorderLayout;
 import org.eclipse.gmf.gmfgraph.CustomClass;
 import org.eclipse.gmf.gmfgraph.Ellipse;
 import org.eclipse.gmf.gmfgraph.FigureGallery;
 import org.eclipse.gmf.gmfgraph.FlowLayout;
 import org.eclipse.gmf.gmfgraph.GridLayout;
 import org.eclipse.gmf.gmfgraph.GridLayoutData;
 import org.eclipse.gmf.gmfgraph.Label;
 import org.eclipse.gmf.gmfgraph.LabeledContainer;
 import org.eclipse.gmf.gmfgraph.Polygon;
 import org.eclipse.gmf.gmfgraph.PolygonDecoration;
 import org.eclipse.gmf.gmfgraph.Polyline;
 import org.eclipse.gmf.gmfgraph.PolylineConnection;
 import org.eclipse.gmf.gmfgraph.PolylineDecoration;
 import org.eclipse.gmf.gmfgraph.Rectangle;
 import org.eclipse.gmf.gmfgraph.RoundedRectangle;
 import org.eclipse.gmf.gmfgraph.ScalablePolygon;
 import org.eclipse.gmf.gmfgraph.StackLayout;
 import org.eclipse.gmf.gmfgraph.XYLayout;
 import org.eclipse.gmf.gmfgraph.XYLayoutData;
 
 /**
  * @author artem
  */
 class PureGEFFigureQualifiedNameSwitch extends GMFGraphSwitch<String> implements FigureQualifiedNameSwitch  {
 
 	public String get(EObject gmfgraphObject) {
 		return doSwitch(gmfgraphObject);
 	}
 
 	public String get(EObject gmfgraphObject, ImportAssistant importManager) {
 		return importManager.getImportedName(get(gmfgraphObject));
 	}
 
 	public String[] getDependencies(FigureGallery gallery) {
 		UniqueEList<String> rv = new UniqueEList<String>(); // FIXME why UniqueEList, not Set?
 		collectDependencies(gallery, rv);
 		return rv.toArray(new String[rv.size()]);
 	}
 
 	protected void collectDependencies(FigureGallery gallery, UniqueEList<String> result) {
 		final String pluginBasicDraw2d = "org.eclipse.draw2d"; //$NON-NLS-1$
 		if (usesDraw2dFigures(gallery)) {
 			result.add(pluginBasicDraw2d);
 		}
 		if (gallery.getImplementationBundle() != null){
 			result.add(gallery.getImplementationBundle());
 		}
 	}
 
 	private boolean usesDraw2dFigures(FigureGallery gallery) {
 		// assume draw2d always used
 		return !gallery.getFigures().isEmpty();
 	}
 
 	public String caseCustomClass(CustomClass object) {
 		return object.getQualifiedClassName();
 	}
 
 	public String caseFlowLayout(FlowLayout object) {
 		return object.isForceSingleLine() ? "org.eclipse.draw2d.ToolbarLayout" : "org.eclipse.draw2d.FlowLayout";
 	}
 	
 	public String caseXYLayout(XYLayout object) {
 		return "org.eclipse.draw2d.XYLayout";
 	}
 	
 	public String caseXYLayoutData(XYLayoutData object) {
 		return "org.eclipse.draw2d.geometry.Rectangle";
 	}
 	
 	public String caseStackLayout(StackLayout object) {
 		return "org.eclipse.draw2d.StackLayout";
 	}
 
 	public String caseBorderLayout(BorderLayout object) {
 		return "org.eclipse.draw2d.BorderLayout";
 	}
 
 	public String caseLabel(Label object) {
 		return "org.eclipse.draw2d.Label"; //$NON-NLS-1$
 	}
 
 	public String caseLabeledContainer(LabeledContainer object) {
 		return "org.eclipse.draw2d.LabeledContainer"; //$NON-NLS-1$
 	}
 
 	public String caseRectangle(Rectangle object) {
 		return "org.eclipse.draw2d.RectangleFigure"; //$NON-NLS-1$
 	}
 
 	public String caseRoundedRectangle(RoundedRectangle object) {
 		return "org.eclipse.draw2d.RoundedRectangle"; //$NON-NLS-1$
 	}
 
 	public String caseEllipse(Ellipse object) {
 		return "org.eclipse.draw2d.Ellipse"; //$NON-NLS-1$
 	}
 
 	public String casePolygon(Polygon object) {
 		return "org.eclipse.draw2d.Polygon"; //$NON-NLS-1$
 	}
 	
 	public String caseScalablePolygon(ScalablePolygon object) {
 		//custom implementation
 		return object.eContainer() instanceof FigureGallery ? 
 				"org.eclipse.draw2d.Shape" : //$NON-NLS-1$
 				"ScalablePolygon";
 	}
 
 	public String casePolygonDecoration(PolygonDecoration object) {
 		return "org.eclipse.draw2d.PolygonDecoration"; //$NON-NLS-1$
 	}
 
 	public String casePolyline(Polyline object) {
 		return "org.eclipse.draw2d.Polyline"; //$NON-NLS-1$
 	}
 
 	public String casePolylineDecoration(PolylineDecoration object) {
 		return "org.eclipse.draw2d.PolylineDecoration"; //$NON-NLS-1$
 	}
 
 	public String casePolylineConnection(PolylineConnection object) {
 		return "org.eclipse.draw2d.PolylineConnection"; //$NON-NLS-1$
 	}
 	
 	public String caseGridLayout(GridLayout object) {
 		return "org.eclipse.draw2d.GridLayout";
 	}
 
 	public String caseGridLayoutData(GridLayoutData object) {
 		return "org.eclipse.draw2d.GridData";
 	}
 	
 }
