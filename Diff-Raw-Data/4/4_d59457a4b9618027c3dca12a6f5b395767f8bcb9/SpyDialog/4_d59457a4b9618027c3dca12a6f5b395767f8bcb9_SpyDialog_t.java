 /*******************************************************************************
  * Copyright (c) 2013 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.e4.tools.event.spy.internal.ui;
 
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.core.services.events.IEventBroker;
 import org.eclipse.e4.tools.event.spy.internal.core.EventMonitor;
 import org.eclipse.e4.tools.event.spy.internal.model.CapturedEvent;
 import org.eclipse.e4.tools.event.spy.internal.model.CapturedEventFilter;
 import org.eclipse.e4.tools.event.spy.internal.model.CapturedEventTreeSelection;
 import org.eclipse.e4.tools.event.spy.internal.model.SpyDialogMemento;
 import org.eclipse.e4.tools.event.spy.internal.util.JDTUtils;
 import org.eclipse.e4.tools.event.spy.internal.util.LoggerWrapper;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 public class SpyDialog extends Dialog implements EventMonitor.NewEventListener {
 	private final static String DIALOG_TITLE = "Event spy dialog";
 
 	private final static String[] SHOW_FILTER_LINK_TEXT = new String[]{"Show filters", "Hide filters"};
 
 	private CapturedEventTree capturedEventTree;
 
 	private CapturedEventFilters capturedEventFilters;
 
 	private Composite outer;
 
 	private EventMonitor eventMonitor;
 
 	private ToggleLink showFiltersLink;
 
 	@Inject
 	private LoggerWrapper logger;
 
 	@Inject
 	private IEventBroker eventBroker;
 
 	@Inject
 	private IEclipseContext context;
 	
 	@Inject
 	public SpyDialog(Shell shell) {
 		super(shell);
 		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
 	}
 
 	/* Layout scheme:
 	 *
 	 *  +-- Outer ----------------------------------------+
 	 *  | +-- actionBar --------------------------------+ |
 	 *  | |                                             | |
 	 *  | |  Start capturing events | ShowFiltersLink   | |
 	 *  | |                                             | |
 	 *  | +---------------------------------------------+ |
 	 *  +-------------------------------------------------+
 	 *  |                                                 |
 	 *  |  CapturedEventFilters                           |
 	 *  |                                                 |
 	 *  +-------------------------------------------------+
 	 *  |                                                 |
 	 *  |  CapturedEventTree                              |
 	 *  |                                                 |
 	 *  +-------------------------------------------------+
 	 *  |                                                 |
 	 *  |                                          Close  |
 	 *  |                                                 |
 	 *  +-------------------------------------------------+
 	 *
 	 * */
 
 	@Override
 	protected Point getInitialSize() {
 		return new Point(608, 450);
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		outer = (Composite) super.createDialogArea(parent);
 		SpyDialogMemento memento = (SpyDialogMemento) context.get(SpyDialogMemento.class.getName()); 
 				
 		createActionBar(outer);
 		createFilters(outer, memento);
 		createCapturedEventTree(outer);
 		return outer;
 	}
 	
 	@Override
 	public boolean close() {
 		saveDialogMemento();
 		return super.close();
 	}
 
 	private void saveDialogMemento() {
 		SpyDialogMemento memento = null;
 		String baseTopic = capturedEventFilters.getBaseTopic();
 		Collection<CapturedEventFilter> filters = capturedEventFilters.getFilters();
 		
 		if (!CapturedEventFilters.BASE_EVENT_TOPIC.equals(baseTopic)) {
 			memento = new SpyDialogMemento();
 			memento.setBaseTopic(baseTopic);
 		}
 		if (!filters.isEmpty()) {
 			if (memento == null) {
 				memento = new SpyDialogMemento();
 			}
 			memento.setFilters(filters);
 		}
 		if (memento != null) {
 			context.set(SpyDialogMemento.class.getName(), memento);
 		}
 	}
 	
 	private void createActionBar(Composite parent) {
 		Composite actionBar = new Composite(parent, SWT.NONE);
 		GridData gridData = createDefaultGridData();
 		gridData.grabExcessVerticalSpace = false;
 		actionBar.setLayoutData(gridData);
 
 		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
 		rowLayout.spacing = 20;
 		actionBar.setLayout(rowLayout);
 
 		ToggleLink link = new ToggleLink(actionBar);
 		link.setText(new String[]{"Start capturing events", "Stop capturing events"});
 		link.setClickListener(new ToggleLink.ClickListener() {
 			public void clicked(boolean toggled) {
 				if (toggled) {
 					captureEvents();
 				} else {
 					stopCaptureEvents();
 				}
 			}
 		});
 
 		showFiltersLink = new ToggleLink(actionBar);
 		showFiltersLink.setText(new String[]{SHOW_FILTER_LINK_TEXT[0], SHOW_FILTER_LINK_TEXT[1]});
 		showFiltersLink.getControl().setLayoutData(new RowData(130, SWT.DEFAULT));
 		showFiltersLink.setClickListener(new ToggleLink.ClickListener() {
 			public void clicked(boolean toggled) {
 				showFilters(toggled);
 			}
 		});
 	}
 
 	private void createFilters(Composite parent, SpyDialogMemento memento) {
 		capturedEventFilters = new CapturedEventFilters(outer);
 		capturedEventFilters.getControl().setVisible(false);
 		GridData gridData = createDefaultGridData();
 		gridData.grabExcessVerticalSpace = false;
 		gridData.exclude = true;
 		capturedEventFilters.getControl().setLayoutData(gridData);
 		
 		if (memento != null) {
 			capturedEventFilters.setBaseTopic(memento.getBaseTopic());
 			capturedEventFilters.setFilters(memento.getFilters());
 		}
 		showFilters(false);
 	}
 
 	private void createCapturedEventTree(Composite parent) {
 		capturedEventTree = new CapturedEventTree(outer);
 		capturedEventTree.getControl().setLayoutData(createDefaultGridData());
 		capturedEventTree.setSelectionListener(new CapturedEventTree.SelectionListener() {
 			public void selectionChanged(CapturedEventTreeSelection selection) {
 				openResource(selection);
 			}
 		});
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText(DIALOG_TITLE);
 	}
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
		//do nothing
 	}
 
 	public void captureEvents() {
 		capturedEventTree.removeAll();
 		if (eventMonitor == null) {
 			eventMonitor = new EventMonitor(eventBroker);
 			eventMonitor.setNewEventListener(this);
 		}
 		eventMonitor.start(capturedEventFilters.getBaseTopic(), capturedEventFilters.getFilters());
 		getShell().setText(DIALOG_TITLE + " - capturing...");
 	}
 
 	public void stopCaptureEvents() {
 		if (eventMonitor != null) {
 			eventMonitor.stop();
 		}
 		getShell().setText(DIALOG_TITLE);
 	}
 
 	public void newEvent(CapturedEvent event) {
 		capturedEventTree.addEvent(event);
 	}
 
 	@SuppressWarnings("restriction")
 	private void openResource(CapturedEventTreeSelection selection) {
 		try {
 			JDTUtils.openClass(selection.getSelection());
 		} catch(ClassNotFoundException exc) {
 			logger.warn(exc.getMessage());
 		}
 	}
 
 	private void showFilters(boolean filtersVisible) {
 		capturedEventFilters.getControl().setVisible(filtersVisible);
 		((GridData) capturedEventFilters.getControl().getLayoutData()).exclude = !filtersVisible;
 
 		//Filters have been set and filters UI is not visible so we have to mark it to user
 		if (!filtersVisible && capturedEventFilters.hasFilters()) {
 			showFiltersLink.setText(new String[] { String.format("%s (%d)", SHOW_FILTER_LINK_TEXT[0],
 				capturedEventFilters.getFiltersCount()), SHOW_FILTER_LINK_TEXT[1]});
 		} else {
 			showFiltersLink.setText(new String[] {SHOW_FILTER_LINK_TEXT[0], SHOW_FILTER_LINK_TEXT[1]});
 		}
 
 		outer.layout(false);
 	}	
 
 	private GridData createDefaultGridData() {
 		GridData gridData = new GridData();
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.verticalSpan = 2;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		return gridData;
 	}
 }
