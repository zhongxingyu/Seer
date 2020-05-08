 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Zend Technologies Ltd. - initial API and implementation
  *******************************************************************************/
 package org.zend.usagedata.internal.monitors;
 
import java.io.File;
 import java.util.List;
 
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWindowListener;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.zend.usagedata.monitors.AbstractMonitor;
 import org.zend.usagedata.monitors.MonitorUtils;
 
 /**
  * Instances of the {@link ViewsUsageMonitor} class monitor the use of views in
  * a context of certain perspective. More specifically, it is notified whenever
  * a view closed or activated (given focus) and sends some of these events to
  * the UsageDataService.
  * <p>
  * List of perspectives which should be consider during collecting information
  * about views usage is stored in perspectives file.
  * </p>
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class ViewsUsageMonitor extends AbstractMonitor {
 
 	public static final String MONITOR_ID = "org.zend.viewsUsageMonitor"; //$NON-NLS-1$
 
	private static final String PERSPECTIVES_FILE = "config" + File.separator + "viewsUsageMonitor.perspectives.config"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String VIEWS_FILE = "config" + File.separator + "viewsUsageMonitor.views.config"; //$NON-NLS-1$ //$NON-NLS-2$
 
 	private static final String ACTIVATED = "activated"; //$NON-NLS-1$
 	private static final String VIEW = "view"; //$NON-NLS-1$
 
 	private List<String> perspectives;
 	private List<String> views;
 
 	private IPartListener partListener = new IPartListener() {
 		public void partActivated(IWorkbenchPart part) {
 			recordEvent(ACTIVATED, part);
 		}
 
 		public void partDeactivated(IWorkbenchPart part) {
 		}
 
 		public void partBroughtToTop(IWorkbenchPart part) {
 		}
 
 		public void partClosed(IWorkbenchPart part) {
 		}
 
 		public void partOpened(IWorkbenchPart part) {
 		}
 	};
 
 	private IWindowListener windowListener = new IWindowListener() {
 		public void windowOpened(IWorkbenchWindow window) {
 			hookListener(window);
 		}
 
 		public void windowClosed(IWorkbenchWindow window) {
 			unhookListener(window);
 		}
 
 		public void windowActivated(IWorkbenchWindow window) {
 		}
 
 		public void windowDeactivated(IWorkbenchWindow window) {
 		}
 
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.usagedata.monitors.AbstractMonitor#getId()
 	 */
 	public String getId() {
 		return MONITOR_ID;
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.usagedata.monitors.AbstractMonitor#doStartMonitoring()
 	 */
 	protected void doStartMonitoring() {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		perspectives = MonitorUtils.getValues(PERSPECTIVES_FILE);
 		views = MonitorUtils.getValues(VIEWS_FILE);
 		if (perspectives != null && perspectives.size() > 0) {
 			hookListeners(workbench);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.usagedata.monitors.AbstractMonitor#doStopMonitoring()
 	 */
 	protected void doStopMonitoring() {
 		if (perspectives != null && perspectives.size() > 0) {
 			IWorkbench workbench = PlatformUI.getWorkbench();
 			unhookListeners(workbench);
 		}
 	}
 
 	private void hookListeners(final IWorkbench workbench) {
 		workbench.addWindowListener(windowListener);
 		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
 			hookListener(window);
 		}
 	}
 
 	private void unhookListeners(final IWorkbench workbench) {
 		if (workbench.getDisplay().isDisposed()) {
 			return;
 		}
 		workbench.removeWindowListener(windowListener);
 		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
 			unhookListener(window);
 		}
 	}
 
 	private void hookListener(IWorkbenchWindow window) {
 		if (window == null) {
 			return;
 		}
 		for (IWorkbenchPage page : window.getPages()) {
 			page.addPartListener(partListener);
 		}
 	}
 
 	private void unhookListener(IWorkbenchWindow window) {
 		if (window == null) {
 			return;
 		}
 		for (IWorkbenchPage page : window.getPages()) {
 			page.removePartListener(partListener);
 		}
 	}
 
 	private void recordEvent(String event, IWorkbenchPart part) {
 		IWorkbenchPartSite site = part.getSite();
 		if (site instanceof IViewSite) {
 			if (!views.contains(site.getId())) {
 				IPerspectiveDescriptor perspective = site.getPage()
 						.getPerspective();
 				if (perspectives.contains(perspective.getId())) {
 					recordEvent(MONITOR_ID, event, VIEW, site.getId(),
 							perspective.getId());
 				}
 			}
 		}
 	}
 
 }
