 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Dmitry Stadnik (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.examples.taipan.gmf.editor.providers;
 
 import java.util.Map;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
 import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.palette.IPaletteProvider;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.gmf.examples.taipan.gmf.editor.part.TaiPanPaletteFactory;
 
 /**
  * @generated
  */
 public class TaiPanPaletteProvider extends AbstractProvider implements IPaletteProvider {
 
 	/**
 	 * @generated
 	 */
 	public void contributeToPalette(IEditorPart editor, Object content, PaletteRoot root, Map predefinedEntries) {
 		TaiPanPaletteFactory factory = new TaiPanPaletteFactory();
 		factory.fillPalette(root);
 	}
 
 	/**
 	 * @generated
 	 */
 	public void setContributions(IConfigurationElement configElement) {
 		// no configuration
 	}
 
 	/**
 	 * @generated
 	 */
 	public boolean provides(IOperation operation) {
 		return false; // all logic is done in the service
 	}
 }
