 /*
 * Copyright (c) 2006, 2009 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Yury Chernikov (Borland) - initial API and implementation
  */
 package org.eclipse.uml2.diagram.common.draw2d;
 
 import org.eclipse.draw2d.geometry.Insets;
 import org.eclipse.gmf.runtime.diagram.ui.figures.NoteFigure;
 
 public class ConstraintFigureBase extends NoteFigure {
 	public ConstraintFigureBase() {
		this(100, 60, new Insets(MARGIN_DP, MARGIN_DP, MARGIN_DP, CLIP_MARGIN_DP));
 	}
 	
 	public ConstraintFigureBase(int width, int height, Insets insets){
 		super(width, height, insets);
 	}
	
 }
