 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.core.nonosgi;
 
 import org.eclipse.wazaabi.engine.core.CoreSingletons;
 import org.eclipse.wazaabi.engine.core.annotations.factories.internal.ComposedAnnotationManagerFactoryImpl;
 import org.eclipse.wazaabi.engine.core.celleditors.factories.internal.ComposedCellEditorFactoryImpl;
 import org.eclipse.wazaabi.engine.core.editparts.factories.CoreEditPartFactory;
 import org.eclipse.wazaabi.engine.core.editparts.factories.internal.ComposedEditPartFactoryImpl;
 import org.eclipse.wazaabi.engine.core.events.CoreEventHandlerAdapterFactory;
 import org.eclipse.wazaabi.engine.core.stylerules.factories.CoreStyleRuleManagerFactory;
 import org.eclipse.wazaabi.engine.core.stylerules.factories.internal.ComposedStyleRuleManagerFactoryImpl;
 import org.eclipse.wazaabi.engine.core.views.factories.internal.ComposedWidgetViewFactoryImpl;
 import org.eclipse.wazaabi.engine.edp.EDPSingletons;
 import org.eclipse.wazaabi.engine.edp.nonosgi.EDPHelper;
 
 public class CoreHelper {
 
 	private static boolean neverCalled = true;
 
 	/**
 	 * Initializes the CoreSingletons class when called from a non osgi
 	 * environment. Could be called more than once.
 	 */
 	public static synchronized void init() {
 		if (!neverCalled)
 			return;
 		EDPHelper.init();
 		CoreSingletons
 				.setComposedEditPartFactory(new ComposedEditPartFactoryImpl());
 		CoreSingletons
 				.setComposedWidgetViewFactory(new ComposedWidgetViewFactoryImpl());
 		CoreSingletons
 				.setComposedStyleRuleManagerFactory(new ComposedStyleRuleManagerFactoryImpl());
 		CoreSingletons
 				.setComposedAnnotationManagerFactory(new ComposedAnnotationManagerFactoryImpl());
 		CoreSingletons
 				.setComposedCellEditorFactory(new ComposedCellEditorFactoryImpl());
 
 		CoreSingletons.getComposedEditPartFactory().addEditPartFactory(
 				new CoreEditPartFactory());
 		CoreSingletons.getComposedStyleRuleManagerFactory()
 				.addStyleRuleManagerFactory(new CoreStyleRuleManagerFactory());
 		EDPSingletons.getComposedEventHandlerAdapterFactory()
 				.addEventHandlerAdapterFactory(
 						new CoreEventHandlerAdapterFactory());
 
 		neverCalled = false;
 	}
 
 }
