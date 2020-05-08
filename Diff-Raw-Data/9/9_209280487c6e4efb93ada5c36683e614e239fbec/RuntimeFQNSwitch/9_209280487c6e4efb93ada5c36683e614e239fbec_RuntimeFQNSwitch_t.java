 /*
 * Copyright (c) 2005, 2007 Borland Software Corporation
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
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.util.UniqueEList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.gmfgraph.FigureGallery;
 import org.eclipse.gmf.gmfgraph.GMFGraphPackage;
 import org.eclipse.gmf.gmfgraph.Label;
 import org.eclipse.gmf.gmfgraph.PolylineConnection;
 
 /**
  * FQNSwitch to use with figures utilizing power of GMF Runtime
  * @author artem
  */
 public class RuntimeFQNSwitch extends PureGEFFigureQualifiedNameSwitch {
 
 	protected void collectDependencies(FigureGallery gallery, UniqueEList<String> result) {
 		super.collectDependencies(gallery, result);
 		final String pluginRuntimeDraw2d = "org.eclipse.gmf.runtime.draw2d.ui"; //$NON-NLS-1$
 		if (usesEClassWithID(gallery, new int[] {GMFGraphPackage.POLYLINE_CONNECTION, GMFGraphPackage.LABEL})) {
 			result.add(pluginRuntimeDraw2d);
 		}
 	}
 
 	private boolean usesEClassWithID(FigureGallery gallery, int[] ids) {
 		// Perhaps, EcoreUtil.getAllContents(gallery, false) would be better - 
 		// - e.g. if eClass().getClassifierID() works for proxies?
 		Arrays.sort(ids);
 		for (Iterator<EObject> it = gallery.eAllContents(); it.hasNext(); ) {
 			EObject next = it.next();
 			if (Arrays.binarySearch(ids, next.eClass().getClassifierID()) >= 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String casePolylineConnection(PolylineConnection object) {
 		return "org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx"; //$NON-NLS-1$
 	}
 
 	public String caseLabel(Label object) {
 		return "org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel"; //$NON-NLS-1$
 	}
 }
