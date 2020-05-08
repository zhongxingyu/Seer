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
 package org.eclipse.gmf.internal.bridge;
 
 import org.eclipse.gmf.gmfgraph.DiagramLabel;
 import org.eclipse.gmf.gmfgraph.Figure;
 import org.eclipse.gmf.gmfgraph.FigureAccessor;
 import org.eclipse.gmf.gmfgraph.FigureHandle;
 
 /**
  * Handcoded decisions
  * @author artem
  */
 public class Knowledge {
 
 	public static boolean isExternal(DiagramLabel diagramLabel) {
 		FigureHandle figure = diagramLabel.getFigure();
 		if (figure instanceof Figure) {
 			return ((Figure) figure).getParent() == null;
 		} else if (figure instanceof FigureAccessor) {
 			return false;
 		}
		throw new IllegalStateException("No more known subclasses of FigureHandle");
 	}
 
 }
