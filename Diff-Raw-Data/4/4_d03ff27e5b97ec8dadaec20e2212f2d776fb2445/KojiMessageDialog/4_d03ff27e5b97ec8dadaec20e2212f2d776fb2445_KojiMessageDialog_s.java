 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.koji;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.browser.IWebBrowser;
 import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
 import org.eclipse.ui.forms.widgets.FormText;
 
 /**
  * Message dialog showing the link to the Koji Web page showing build info.
  *
  */
 public class KojiMessageDialog extends MessageDialog {
 	private int taskId;
 	private URL kojiWebUrl;
 	private String messageText;
 	private Image dialogContentImage;
 
 	/**
 	 * Creates the message dialog with the given index.
 	 * 
 	 * @param shell
 	 *            A valid shell (make sure shell is on heap not stack)
 	 * @param dialogTitle
 	 *            The dialog title.
 	 * @param dialogTitleImage
 	 *            The image for the dialog title bar
 	 * @param dialogImageType
 	 *            The image type
 	 * @param dialogButtonLabels
 	 *            Labels for the dialog buttons.
 	 * @param defaultIndex
 	 *            The default index (usually 1)
 	 * @param kojiWebURL
 	 *            The base URL to Koji Web.
 	 * @param taskId
 	 *            The id of the pushed task
 	 * @param messageText
 	 *            The textual content of the message dialog.
 	 * @param dialogContentImage
 	 *            The image for the message dialog content.
 	 * @param buildToolName
 	 *            The name of the build tool.
 	 */
 	public KojiMessageDialog(Shell shell, String dialogTitle,
 			Image dialogTitleImage, int dialogImageType,
 			String[] dialogButtonLabels, int defaultIndex, URL kojiWebURL,
 			int taskId, String messageText, Image dialogContentImage, String buildToolName) {
 		super(shell, dialogTitle, dialogTitleImage, NLS.bind(
 				KojiText.KojiMessageDialog_buildNumberMsg,
 				buildToolName, taskId),
 				dialogImageType, dialogButtonLabels, defaultIndex);
 		this.kojiWebUrl = kojiWebURL;
 		this.taskId = taskId;
 		this.messageText = messageText;
 	}
 
 	@Override
 	public Image getImage() {
 		return this.dialogContentImage;
 	}
 
 	@Override
 	protected Control createCustomArea(Composite parent) {
 		FormText taskLink = new FormText(parent, SWT.NONE);
 		final String url = KojiUrlUtils.constructTaskUrl(taskId, kojiWebUrl);
 		taskLink.setText("<form><p>" +  //$NON-NLS-1$
 				this.messageText + "</p><p>"+ url //$NON-NLS-1$
 						+ "</p></form>", true, true); //$NON-NLS-1$
 		taskLink.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				try {
 					IWebBrowser browser = PlatformUI
 							.getWorkbench()
 							.getBrowserSupport()
 							.createBrowser(
 									IWorkbenchBrowserSupport.NAVIGATION_BAR
 											| IWorkbenchBrowserSupport.LOCATION_BAR
 											| IWorkbenchBrowserSupport.STATUS,
 									"koji_task", null, null); //$NON-NLS-1$
 					browser.openURL(new URL(url));
 				} catch (PartInitException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		return taskLink;
 	}
 }
