 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.css.properties;
 
 import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
 import org.jboss.tools.jst.jsp.outline.cssdialog.tabs.BaseTabControl;
 import org.jboss.tools.jst.jsp.outline.cssdialog.tabs.TabPropertySheetControl;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  */
 public class PropertiesSection extends AbstractCSSSection {
 
 	@Override
 	public BaseTabControl createSectionControl(Composite parent) {
 
		//quick fix to solve appearing 2 scrollbars 
		((TabbedPropertyComposite)getTabComposite()).getScrolledComposite().setExpandVertical(false);
 		return new TabPropertySheetControl(parent, getStyleAttributes(),
 				getBindingContext());
		
 	}
 }
