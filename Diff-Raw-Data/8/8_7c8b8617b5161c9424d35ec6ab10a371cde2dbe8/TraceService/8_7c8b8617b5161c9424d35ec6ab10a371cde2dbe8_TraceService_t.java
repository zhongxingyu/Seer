 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    cbrand - Bug 377783 - Dump for figures in connection layer needed
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.services.impl;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.draw2d.FreeformLayeredPane;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.internal.figures.GFMultilineText;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.internal.services.ITraceService;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class TraceService implements ITraceService {
 
 	private final boolean FULL_QUALIFIED = false;
 	private final boolean ADD_OBJECT_INFO = false;
 	private final boolean ADD_STYLE_INFO = false;
 
 	public String getStacktrace(Throwable t) {
 		if (t == null)
 			return null;
 		StringWriter writer = new StringWriter();
 		t.printStackTrace(new PrintWriter(writer));
 		return writer.toString();
 	}
 
 	public void dumpFigureTree(IFigure figure) {
 		System.out.println("\nFigure Tree"); //$NON-NLS-1$
 		dumpFigureTree(figure, 0);
 	}
 
 	public void dumpFigureTree(IFigure figure, int indent) {
 		String indentString = createIndentString(indent);
 
 		String additional = ""; //$NON-NLS-1$
 		if (figure instanceof Label) {
 			Label label = (Label) figure;
 			additional = " (text: " + label.getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (figure instanceof GFMultilineText) {
 			GFMultilineText mlt = (GFMultilineText) figure;
 			additional = additional + " (text: " + mlt.getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (!figure.isVisible()) {
 			additional = additional + " (NOT visible)"; //$NON-NLS-1$
 		}
 		additional = additional + " (" + figure.getBounds() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 		if (ADD_OBJECT_INFO) {
 			additional = additional + " (" + getObjectInfo(figure) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		System.out.println(indentString + getClassName(figure, FULL_QUALIFIED) + additional);
 
 		@SuppressWarnings("unchecked")
 		List<IFigure> children = figure.getChildren();
 		for (IFigure childFigure : children) {
 			dumpFigureTree(childFigure, indent + 2);
 		}
 	}
 
 	public void dumpFigureTreeWithConnectionLayer(IFigure figure) {
 		IFigure dumpRoot = figure;
		FreeformLayeredPane root = findFreeformLayeredPane(dumpRoot);
 		if (root != null) {
 			dumpRoot = root;
 		}
 		dumpFigureTree(dumpRoot);
 	}
 
	private FreeformLayeredPane findFreeformLayeredPane(IFigure figure) {
 		IFigure parentFigure = figure.getParent();
 		if (parentFigure instanceof FreeformLayeredPane) {
 			return (FreeformLayeredPane) parentFigure;
 		}
 		if (parentFigure != null) {
			return findFreeformLayeredPane(parentFigure);
 		}
 		return null;
 	}
 
 	public void dumpEditPartTree(EditPart editPart) {
 		System.out.println("\nEdit Part Tree()"); //$NON-NLS-1$
 		dumpEditPartTree(editPart, 0);
 	}
 
 	public void dumpEditPartTree(EditPart editPart, int indent) {
 		String indentString = createIndentString(indent);
 		Object m = editPart.getModel();
 		String additional = ""; //$NON-NLS-1$
 		if (m instanceof PictogramElement) {
 			PictogramElement pe = (PictogramElement) m;
 			GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 			if (ga != null) {
 				additional = additional + " (" + getClassName(ga, FULL_QUALIFIED) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 				if (ADD_OBJECT_INFO) {
 					additional = additional + " (" + getObjectInfo(ga) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 		System.out.println(indentString + getClassName(editPart, FULL_QUALIFIED) + additional);
 
 		List<EditPart> ch = GraphitiUiInternal.getGefService().getEditPartChildren(editPart);
 		for (EditPart epChild : ch) {
 			dumpEditPartTree(epChild, indent + 2);
 		}
 	}
 
 	public void dumpPictogramModelTree(PictogramElement pe) {
 		System.out.println("\nPictogram Model Tree()"); //$NON-NLS-1$
 		dumpPictogramModelTree(pe, 0);
 	}
 
 	public void dumpPictogramModelTree(PictogramElement pe, int indent) {
 		String indentString = createIndentString(indent);
 
 		String additional = ""; //$NON-NLS-1$
 		additional = additional + " (" + (pe.isActive() ? "active" : "inactive") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 		additional = additional + " (" + (pe.isVisible() ? "visible" : "invisible") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 
 		System.out.println(indentString + GefService.PE + getClassName(pe, FULL_QUALIFIED) + additional);
 
 		dumpGATree(pe.getGraphicsAlgorithm(), indent + 2);
 
 		Collection<PictogramElement> peChildren = Graphiti.getPeService().getPictogramElementChildren(pe);
 		for (PictogramElement peChild : peChildren) {
 			dumpPictogramModelTree(peChild, indent + 2);
 		}
 	}
 
 	public void dumpGATree(GraphicsAlgorithm ga) {
 		dumpGATree(ga, 0);
 	}
 
 	public void dumpGATree(GraphicsAlgorithm ga, int indent) {
 		String indentString = createIndentString(indent);
 		if (ga == null) {
 			return;
 		}
 
 		String additional = ""; //$NON-NLS-1$
 		IDimension size = Graphiti.getGaService().calculateSize(ga);
 		additional = additional
 				+ " (" + ga.getX() + ", " + ga.getY() + ", " + size.getWidth() + ", " + size.getHeight() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		if (ADD_OBJECT_INFO) {
 			additional = additional + " (" + getObjectInfo(ga) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		System.out.println(indentString + getClassName(ga, FULL_QUALIFIED) + additional);
 
 		if (ADD_STYLE_INFO) {
 			if (ga.getStyle() != null) {
 				Style style = ga.getStyle();
 				dumpStyleTree(style, indent + 2);
 			}
 		}
 
 		List<GraphicsAlgorithm> gaChildren = ga.getGraphicsAlgorithmChildren();
 		for (GraphicsAlgorithm gaChild : gaChildren) {
 			dumpGATree(gaChild, indent + 2);
 		}
 	}
 
 	public void dumpStyleTree(Style style) {
 		dumpStyleTree(style, 0);
 	}
 
 	public void dumpStyleTree(Style style, int indent) {
 		String indentString = createIndentString(indent);
 		if (style == null) {
 			return;
 		}
 		String additional = ""; //$NON-NLS-1$
 		String styleId = style.getId();
 		if (styleId != null) {
 			additional = additional + " (" + styleId + ")";
 		}
 		if (ADD_OBJECT_INFO) {
 			additional = additional + " (" + getObjectInfo(style) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		System.out.println(indentString + getClassName(style, FULL_QUALIFIED) + additional);
 
 		EObject eC = style.eContainer();
 		if (eC instanceof Style) {
 			Style parentStyle = (Style) eC;
 			dumpStyleTree(parentStyle, indent + 2);
 		}
 	}
 
 	private String getClassName(Object o, boolean fullQualified) {
 		if (fullQualified) {
 			return o.getClass().getName();
 		} else {
 			return o.getClass().getSimpleName();
 		}
 	}
 
 	private String createIndentString(int indent) {
 		int s = 0;
 		String indentString = ""; //$NON-NLS-1$
 		while (s < indent) {
 			indentString = indentString + " "; //$NON-NLS-1$
 			s++;
 		}
 		return indentString;
 	}
 
 	private String getObjectInfo(Object o) {
 		return o.toString();
 	}
 }
