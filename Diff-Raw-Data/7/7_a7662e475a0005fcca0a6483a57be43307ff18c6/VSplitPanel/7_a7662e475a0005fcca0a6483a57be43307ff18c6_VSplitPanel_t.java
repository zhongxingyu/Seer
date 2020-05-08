 /*******************************************************************************
  * Copyright (c) 2013 takoyaki.ch.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     takoyaki.ch - Initial version
  ******************************************************************************/
 package ch.takoyaki.email.html.client.ui.generic;
 
 import ch.takoyaki.email.html.client.logging.Log;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.ProvidesResize;
 import com.google.gwt.user.client.ui.RequiresResize;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class VSplitPanel extends Composite implements RequiresResize, ProvidesResize {
 
 	private static VSplitPanelBinder uiBinder = GWT
 			.create(VSplitPanelBinder.class);
 
 	interface VSplitPanelBinder extends UiBinder<Widget, VSplitPanel> {
 	}
 
 	public VSplitPanel() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	public void addNorth(Widget w, double size) {
 		((SplitLayoutPanel) getWidget()).addNorth(w, size);
 	}
 
 	public void addSouth(Widget w) {
 		((SplitLayoutPanel) getWidget()).add(w);
 	}
 
 	@Override
 	public void onResize() {
 		((SplitLayoutPanel) getWidget()).onResize();		
 	}
 }
