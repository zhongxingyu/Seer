 /*******************************************************************************
  * Copyright (c) 2009, 2011 Florian Pirchner and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Florian Pirchner - initial implementation
  * compeople AG     - created new example based on SnipetLinkRidget001
  *******************************************************************************/
 package org.eclipse.riena.sample.snippets;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.ui.ridgets.IBrowserRidget;
 import org.eclipse.riena.ui.ridgets.listener.ILocationListener;
 import org.eclipse.riena.ui.ridgets.listener.LocationEvent;
 import org.eclipse.riena.ui.ridgets.swt.SwtRidgetFactory;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * An {@link IBrowserRidget} showing how to use {@link ILocationListener} to
  * track and ackowledge URL changes.
  */
 public final class SnippetBrowserRidget003 {
 
 	public static void main(final String[] args) {
 		final Display display = Display.getDefault();
 		try {
 			final Shell shell = UIControlsFactory.createShell(display);
 			shell.setText(SnippetBrowserRidget003.class.getSimpleName());
 			shell.setLayout(new FillLayout());
 
 			final Browser browser = new Browser(shell, SWT.BORDER);
 
 			// ridgets
 			final IBrowserRidget browserRidget = (IBrowserRidget) SwtRidgetFactory.createRidget(browser);
 			browserRidget.setUrl("http://www.eclipse.org"); //$NON-NLS-1$
 
 			browserRidget.addLocationListener(new ILocationListener() {
 				public boolean locationChanging(final LocationEvent event) {
 					System.out.println(event);
 					final String msg = String.format("Go to:\n\n'%s' ?", event.getLocation()); //$NON-NLS-1$
 					return MessageDialog.openQuestion(shell, "Confirm Change", msg); //$NON-NLS-1$
 				}
 			});
 
 			shell.setSize(500, 500);
 			shell.open();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 		} finally {
 			display.dispose();
 		}
 	}
 }
