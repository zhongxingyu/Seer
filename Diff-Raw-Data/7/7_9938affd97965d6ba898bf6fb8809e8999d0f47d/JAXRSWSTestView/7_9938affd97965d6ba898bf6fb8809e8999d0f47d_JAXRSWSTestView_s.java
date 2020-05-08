 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.ui.views;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.wsdl.Definition;
 import javax.xml.soap.SOAPEnvelope;
 import javax.xml.soap.SOAPException;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.IJobChangeListener;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.wst.internet.monitor.core.internal.provisional.IMonitor;
 import org.eclipse.wst.internet.monitor.core.internal.provisional.MonitorCore;
 import org.jboss.tools.ws.ui.JBossWSUIPlugin;
 import org.jboss.tools.ws.ui.messages.JBossWSUIMessages;
 import org.jboss.tools.ws.ui.utils.JAXRSTester;
 import org.jboss.tools.ws.ui.utils.JAXWSTester;
 import org.jboss.tools.ws.ui.utils.ResultsXMLStorage;
 import org.jboss.tools.ws.ui.utils.ResultsXMLStorageInput;
 import org.jboss.tools.ws.ui.utils.TesterWSDLUtils;
 import org.jboss.tools.ws.ui.utils.WSTestUtils;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * View for testing web services (JAX-WS & JAX-RS)
  * @author bfitzpat
  *
  */
 @SuppressWarnings("restriction")
 public class JAXRSWSTestView extends ViewPart {
 
 	private static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
 	private static final String XML_EDITOR_ID = "org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart"; //$NON-NLS-1$
 	private static final String TCPIP_VIEW_ID = "org.eclipse.wst.internet.monitor.view";//$NON-NLS-1$
 	private static final String DELETE = "DELETE";//$NON-NLS-1$
 	private static final String PUT = "PUT";//$NON-NLS-1$
 	private static final String POST = "POST";//$NON-NLS-1$
 	private static final String GET = "GET";//$NON-NLS-1$
 	private static final String JAX_WS = "JAX-WS"; //$NON-NLS-1$
 	private static final String JAX_RS = "JAX-RS"; //$NON-NLS-1$
 	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
 	private static final String RESULT_HEADER_DELIMITER = "%";//$NON-NLS-1$
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "org.jboss.tools.ws.ui.tester.views.TestWSView";//$NON-NLS-1$
 
 	/* UI controls */
 	private Button testButton = null;
 	private Button wsdlButton = null;
 	private Text actionText;
 	private Text resultsText;
 	private Combo urlCombo;
 	private DelimitedStringList dlsList;
 	private Combo methodCombo;
 	private Combo wsTypeCombo;
 	private Text bodyText;
 	private TabFolder tabGroup;
 	private TabItem bodyTab;
 	private TabItem headerTab;
 	private List resultHeadersList;
 	private TabItem resultHeadersTab;
 	private TabItem resultTab;
 	private TabFolder resultTabGroup;
 
 	private TabItem parmsTab;
 
 	private DelimitedStringList parmsList;
 	private Button openTCPIPMonitorButton;
 	private Button addTCPIPMonitorButton;
 
 	private SOAPEnvelope envelope;
 	private MenuItem openInXMLEditorAction;
 	private MenuItem openResponseTagInXMLEditor;
 	private Menu resultsTextMenu;
 	private MenuItem copyMenuAction;
 	
 	private boolean showSampleButton = false;
 
 	/**
 	 * The constructor.
 	 */
 	public JAXRSWSTestView() {
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 
 		Composite innerComposite = new Composite (parent, SWT.NONE);
 		innerComposite.setLayout(new FillLayout());
 
 		SashForm sashForm = new SashForm(innerComposite, SWT.BORDER);
 		sashForm.setOrientation(SWT.HORIZONTAL);
 
 		Composite topHalf = new Composite (sashForm, SWT.NONE);
 		topHalf.setLayout(new GridLayout(2, false));
 
 		Label typeLabel = new Label(topHalf, SWT.NONE);
 		typeLabel.setText(JBossWSUIMessages.JAXRSWSTestView_Web_Service_Type_Label);
 		typeLabel.setLayoutData(new GridData());
 
 		wsTypeCombo = new Combo(topHalf, SWT.DROP_DOWN | SWT.READ_ONLY);
 		wsTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 		wsTypeCombo.add(JAX_WS);
 		wsTypeCombo.add(JAX_RS);
 		wsTypeCombo.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 			public void widgetSelected(SelectionEvent e) {
 				setControlsForWSType(wsTypeCombo.getText());
 				setControlsForMethodType(methodCombo.getText());
 				setControlsForSelectedURL();
 			}
 		});
 
 		Label methodLabel = new Label(topHalf, SWT.NONE);
 		methodLabel.setText(JBossWSUIMessages.JAXRSWSTestView_HTTP_Method_Label);
 		methodLabel.setLayoutData(new GridData());
 
 		methodCombo = new Combo(topHalf, SWT.DROP_DOWN | SWT.READ_ONLY);
 		methodCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 		methodCombo.add(GET);
 		methodCombo.add(POST);
 		methodCombo.add(PUT);
 		methodCombo.add(DELETE);
 		methodCombo.setText(GET);
 		methodCombo.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 			public void widgetSelected(SelectionEvent e) {
 				setControlsForMethodType(methodCombo.getText());
 			}
 		});
 
 		Label urlLabel = new Label(topHalf, SWT.NONE);
 		urlLabel.setText(JBossWSUIMessages.JAXRSWSTestView_Service_URL_Label);
 		urlLabel.setLayoutData(new GridData());
 
 		urlCombo = new Combo(topHalf, SWT.BORDER | SWT.DROP_DOWN);
 		urlCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 		urlCombo.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 			public void widgetSelected(SelectionEvent e) {
 				setControlsForSelectedURL();
 			}
 		});
 		urlCombo.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent e) {
 			}
 
 			public void keyReleased(KeyEvent e) {
 				setControlsForSelectedURL();
 				if (e.keyCode == SWT.CR) {
 					handleTest(wsTypeCombo.getText());
 				}
 			}
 		});
 
 		Label actionLabel = new Label(topHalf, SWT.NONE);
 		actionLabel.setText(JBossWSUIMessages.JAXRSWSTestView_Action_URL_Label);
 		actionLabel.setLayoutData(new GridData());
 
 		actionText = new Text(topHalf, SWT.BORDER);
 		actionText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
 
 		tabGroup = new TabFolder(topHalf, SWT.BORDER);
 
 		bodyTab = new TabItem(tabGroup, SWT.NONE, 0);
 		bodyTab.setText(JBossWSUIMessages.JAXRSWSTestView_Request_Body_Label);
 
 		parmsTab = new TabItem(tabGroup, SWT.NONE, 1);
 		parmsTab.setText(JBossWSUIMessages.JAXRSWSTestView_Request_Parameters_Label);
 
 		parmsList = new DelimitedStringList(tabGroup, SWT.None);
 		parmsTab.setControl(parmsList);
 		GridData parmsListGD = new GridData(SWT.FILL, SWT.FILL, true, true);
 		parmsListGD.horizontalSpan = 2;
 		parmsList.setLayoutData(parmsListGD);
 
 		headerTab = new TabItem(tabGroup, SWT.NONE, 2);
 		bodyText = new Text(tabGroup, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
 		GridData btGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		bodyText.setLayoutData(btGridData);
 		bodyTab.setControl(bodyText);
 
 		headerTab.setText(JBossWSUIMessages.JAXRSWSTestView_Request_Header_Label);
 		GridData hgGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		hgGridData.horizontalSpan = 2;
 		tabGroup.setLayoutData(hgGridData);
 
 		dlsList = new DelimitedStringList(tabGroup, SWT.None);
 		headerTab.setControl(dlsList);
 		GridData dlsListGD = new GridData(SWT.FILL, SWT.FILL, true, true);
 		dlsListGD.horizontalSpan = 2;
 		dlsList.setLayoutData(dlsListGD);
 
 		Composite buttonBar = new Composite ( topHalf, SWT.NONE);
 		GridData buttonBarGD = new GridData(SWT.FILL, SWT.NONE, true, false);
 		buttonBarGD.horizontalSpan = 2;
 		buttonBar.setLayoutData(buttonBarGD);
 		buttonBar.setLayout(new RowLayout());
 
 		testButton = new Button (buttonBar, SWT.PUSH);
 		testButton.setText(JBossWSUIMessages.JAXRSWSTestView_Invoke_Label);
 
 		testButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				handleTest(wsTypeCombo.getText());
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		wsdlButton = new Button (buttonBar, SWT.PUSH);
 		wsdlButton.setText(JBossWSUIMessages.JAXRSWSTestView_Button_Get_From_WSDL);
 
 		wsdlButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				WSDLBrowseDialog wbDialog =  new WSDLBrowseDialog(getSite().getShell());
 				int rtnCode = wbDialog.open();
 				if (rtnCode == Window.OK){
 					Definition wsdlDef = wbDialog.getWSDLDefinition();
 					String output = TesterWSDLUtils.getSampleSOAPInputMessage(wsdlDef, 
 							wbDialog.getServiceTextValue(), 
 							wbDialog.getPortTextValue(), 
 							wbDialog.getBindingValue(), 
 							wbDialog.getOperationTextValue());
 					String endpointURL = TesterWSDLUtils.getEndpointURL(wsdlDef, 
 							wbDialog.getServiceTextValue(), 
 							wbDialog.getPortTextValue(), 
 							wbDialog.getBindingValue(), 
 							wbDialog.getOperationTextValue());
 					String actionURL = TesterWSDLUtils.getActionURL(wsdlDef, 
 							wbDialog.getServiceTextValue(), 
 							wbDialog.getPortTextValue(), 
 							wbDialog.getBindingValue(), 
 							wbDialog.getOperationTextValue());
 					String soapIn = generateSampleSOAP(output);
 					bodyText.setText(soapIn);
 					urlCombo.setText(endpointURL);
 					actionText.setText(actionURL);
 					setControlsForWSType(wsTypeCombo.getText());
 					setControlsForMethodType(methodCombo.getText());
 					setControlsForSelectedURL();
 				}
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		addTCPIPMonitorButton = new Button(buttonBar, SWT.PUSH);
 		addTCPIPMonitorButton.setText(JBossWSUIMessages.JAXRSWSTestView_Configure_Monitor_Button);
 
 		addTCPIPMonitorButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				configureMonitor();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		openTCPIPMonitorButton = new Button(buttonBar, SWT.PUSH);
 		openTCPIPMonitorButton.setText(JBossWSUIMessages.JAXRSWSTestView_Open_Monitor_Button);
 
 		openTCPIPMonitorButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				openMonitor();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		Button sampleButton = new Button(buttonBar, SWT.PUSH);
 		sampleButton.setText(JBossWSUIMessages.JAXRSWSTestView_Set_Sample_Data_Label);
 		sampleButton.setVisible(showSampleButton);
 
 		sampleButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				setupSample();
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		Composite bottomHalf = new Composite (sashForm, SWT.NONE);
 		bottomHalf.setLayout(new GridLayout(2, false));
 
 		resultTabGroup = new TabFolder(bottomHalf, SWT.BORDER);
 		GridData rtGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		rtGridData.horizontalSpan = 2;
 		resultTabGroup.setLayoutData(rtGridData);
 
 		resultTab = new TabItem(resultTabGroup, SWT.NONE, 0);
 		resultTab.setText(JBossWSUIMessages.JAXRSWSTestView_Results_Body_Label);
 		resultsText = new Text(resultTabGroup, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY );
 		resultsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		resultsTextMenu = new Menu(resultsText.getShell(), SWT.POP_UP);
 		
 		copyMenuAction = new MenuItem(resultsTextMenu, SWT.PUSH);
 		copyMenuAction.setText(JBossWSUIMessages.JAXRSWSTestView_CopyResultsMenu);
 		copyMenuAction.setAccelerator(SWT.CTRL + 'C');
 		copyMenuAction.addSelectionListener(new SelectionListener(){
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				if (resultsText.getSelectionCount() == 0)
 					resultsText.selectAll();
 				resultsText.copy();
 			}
 
 			public void widgetSelected(SelectionEvent arg0) {
 				widgetDefaultSelected(arg0);
 			}
 		});
 		new MenuItem(resultsTextMenu, SWT.SEPARATOR);
 		
 		openInXMLEditorAction = new MenuItem(resultsTextMenu, SWT.PUSH);
 		openInXMLEditorAction.setText(JBossWSUIMessages.JAXRSWSTestView_Open_Result_in_XML_Editor);
 		openInXMLEditorAction.setAccelerator(SWT.CTRL + 'O');
 		openInXMLEditorAction.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent arg0) {
 				String string = resultsText.getText();
 				openXMLEditor(string);
 			}
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				widgetSelected(arg0);
 			}
 		});
 
 		openResponseTagInXMLEditor = new MenuItem(resultsTextMenu, SWT.PUSH);
 		openResponseTagInXMLEditor.setText(JBossWSUIMessages.JAXRSWSTestView_Open_Response_Tag_Contents_in_XML_Editor);
 		openResponseTagInXMLEditor.setAccelerator(SWT.CTRL + 'R');
 		openResponseTagInXMLEditor.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent arg0) {
 				String string = null;
 				if (envelope != null){
 					try {
 						NodeList list = envelope.getBody().getChildNodes();
 						for (int i = 0; i< list.getLength(); i++){
 							Node node = list.item(i);
 							if (node.getNodeName().contains("Response")){ //$NON-NLS-1$
 								NodeList list2 = node.getChildNodes();
 								for (int j = 0; j<list2.getLength(); j++){
 									Node node2 = list2.item(j);
 									if (node2.getNodeName().contains("Result")){ //$NON-NLS-1$
 										Node node3 = node2.getChildNodes().item(0);
 										if (node3.getNodeType() == Node.TEXT_NODE) {
 											string = node3.getNodeValue();
 											break;
 										} else if (node3.getNodeType() == Node.ELEMENT_NODE) {
 											string = node2.toString();
 											break;
 										}
 									}
 								}
 								if (string != null) break;
 							}
 						}
 						if (string != null){
 							openXMLEditor(string);
 						}
 					} catch (SOAPException e) {
 						JBossWSUIPlugin.log(e);
 					}
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				widgetSelected(arg0);
 			}
 		});
 		
 		resultsText.setMenu(resultsTextMenu);		
 
 		resultTab.setControl(resultsText);
 		
 		resultsText.addFocusListener(new FocusListener() {
 			
 			public void focusLost(FocusEvent arg0) {
 			}
 			
 			public void focusGained(FocusEvent arg0) {
 				setMenusForCurrentState();
 			}
 		});
 
 		resultHeadersTab = new TabItem(resultTabGroup, SWT.NONE, 1);
 		resultHeadersTab.setText(JBossWSUIMessages.JAXRSWSTestView_Results_Header_Label);
 		resultHeadersList = new List(resultTabGroup, SWT.V_SCROLL);
 		resultHeadersTab.setControl(resultHeadersList);
 		GridData rdlsListGD = new GridData(SWT.FILL, SWT.FILL, true, true);
 		rdlsListGD.horizontalSpan = 2;
 		resultHeadersList.setLayoutData(dlsListGD);
 
 		wsTypeCombo.setText(JAX_WS);
 		setControlsForWSType(wsTypeCombo.getText());
 		setControlsForMethodType(methodCombo.getText());
 		setControlsForSelectedURL();
 		setMenusForCurrentState();
 	}
 	
 	private String generateSampleSOAP ( String innerText ) {
 		String soapIn = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>\n" + //$NON-NLS-1$
 			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " + //$NON-NLS-1$
 			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +  //$NON-NLS-1$
 			"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +  //$NON-NLS-1$
 			">\n" + //$NON-NLS-1$
 			"<soap:Body>\n";//$NON-NLS-1$
 		if (innerText != null)
 			soapIn = soapIn + innerText;
 		soapIn = soapIn +
 			"</soap:Body>\n" + //$NON-NLS-1$
 			"</soap:Envelope>";	 //$NON-NLS-1$
 		return soapIn;
 	}
 	
 	private void setMenusForCurrentState() {
 		if (resultsText!= null && !resultsText.isDisposed()){
 			boolean enabled = resultsText.getText().trim().length() > 0; 
 			copyMenuAction.setEnabled(enabled);
 			openInXMLEditorAction.setEnabled(enabled);
 			if (wsTypeCombo.getText().equalsIgnoreCase(JAX_WS)) {
 				openResponseTagInXMLEditor.setEnabled(enabled);
 			} else if (wsTypeCombo.getText().equalsIgnoreCase(JAX_RS) ){
 				openResponseTagInXMLEditor.setEnabled(false);
 			}
 		}
 	}
 	
 	private void openXMLEditor (String text){
 		IWorkbenchWindow window = getSite().getWorkbenchWindow();
 		IStorage storage = new ResultsXMLStorage(text);
 		IStorageEditorInput input = new ResultsXMLStorageInput(storage);
 		IWorkbenchPage page = window.getActivePage();
 		if (page != null) {
 			try {
 				if (window.getWorkbench().getEditorRegistry().findEditor(XML_EDITOR_ID) != null) {
 					page.openEditor(input, XML_EDITOR_ID);
 				} else {
 					page.openEditor(input, DEFAULT_TEXT_EDITOR_ID);
 				}
 			} catch (PartInitException e) {
 				JBossWSUIPlugin.log(e);
 			}			
 		}
 	}
 
 	private void setControlsForSelectedURL() {
 		if (urlCombo.getText().trim().length() > 0) {
 			testButton.setEnabled(true);
 			addTCPIPMonitorButton.setEnabled(true);
 		} else {
 			testButton.setEnabled(false);
 			addTCPIPMonitorButton.setEnabled(false);
 		}
 	}
 
 	/*
 	 * Open the TCP/IP Monitor View 
 	 */
 	private void openMonitor() {
 		try {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().
 			getActivePage().showView(TCPIP_VIEW_ID);
 		} catch (PartInitException e) {
 			JBossWSUIPlugin.log(e);
 		}
 	}
 
 	private IMonitor findMonitor(String urlToCheck) {
 		IMonitor monitor = null;
 
 		IMonitor[] monitors = MonitorCore.getMonitors();
 		if (monitors != null && monitors.length > 0) {
 			for (int i= 0; i < monitors.length; i++) {
 				if (urlToCheck.contains(monitors[i].getRemoteHost())) {
 					monitor = monitors[i];
 					break;
 				}
 			}
 		}
 		return monitor;
 	}
 
 	/*
 	 * Configure a TCP/IP Monitor entry so we can monitor it 
 	 */
 	private void configureMonitor() {
 		if (urlCombo.getText().trim().length() > 0) {
 			String oldUrl = urlCombo.getText();
 			IMonitor monitor = findMonitor(oldUrl);
 			if (monitor == null) {
 
 				URL tempURL = null;
 				try {
 					tempURL = new URL(oldUrl);
 				} catch (MalformedURLException e) {
 					// ignore
 				}
 				AddMonitorDialog dialog = new AddMonitorDialog(getSite().getShell());
 				if (tempURL != null) {
 					dialog.getMonitor().setRemoteHost(tempURL.getHost());
 					if (tempURL.getPort() > 0) 
 						dialog.getMonitor().setRemotePort(tempURL.getPort());
 				}
 				if (dialog.open() == Window.CANCEL)
 					return;
 				monitor = dialog.getMonitor();
 			}
 
 			if (monitor != null) {
 				monitor = findMonitor(oldUrl);
 				if (monitor != null) {
 					if (!monitor.isRunning()) {
 						try {
 							monitor.start();
 							int port = monitor.getLocalPort();
 							int remotePort = monitor.getRemotePort();
 							String host = monitor.getRemoteHost();
 							String newUrl = null;
							if (oldUrl.contains(host + ":" + remotePort)) { //$NON-NLS-1$
								newUrl = oldUrl.replace(host + ":" + remotePort, "localhost:" + port); //$NON-NLS-1$ //$NON-NLS-2$
 							} else {
 								newUrl = oldUrl.replace(host, "localhost:" + port); //$NON-NLS-1$
 							}
 							urlCombo.setText(newUrl);
 						} catch (CoreException e) {
 							// if we hit an error, open a dialog
 							ErrorDialog dialog = new ErrorDialog(this.getSite().getShell(), 
 									JBossWSUIMessages.JAXRSWSTestView_Error_Title_Starting_Monitor,
 									JBossWSUIMessages.JAXRSWSTestView_Error_Msg_Starting_Monitor,
 									new Status(IStatus.ERROR, JBossWSUIPlugin.PLUGIN_ID, 
 									e.getLocalizedMessage(), e), IStatus.ERROR);
 							dialog.open();
 						}
 					}
 				}
 			}
 		}		
 	}
 
 	/*
 	 * Enable/disable controls based on the WS technology type
 	 * and the method.
 	 * 
 	 * @param methodType
 	 */
 	private void setControlsForMethodType ( String methodType ) {
 		if (wsTypeCombo.getText().equalsIgnoreCase(JAX_RS) &&
 				methodType.equalsIgnoreCase(GET)) {
 			bodyText.setEnabled(false);
 		} else {
 			bodyText.setEnabled(true);
 		}
 	}
 
 	/*
 	 * Enable/disable controls based on the WS technology type
 	 * @param wsType
 	 */
 	private void setControlsForWSType ( String wsType ) {
 		if (wsType.equalsIgnoreCase(JAX_WS)) {
 			actionText.setEnabled(true);
 			bodyText.setEnabled(true);
 			methodCombo.setEnabled(false);
 			parmsList.setEnabled(false);
 			dlsList.setEnabled(false);
 			parmsTab.getControl().setEnabled(false);
 			headerTab.getControl().setEnabled(true);
 			methodCombo.setText(POST);
 
 			String emptySOAP = "<?xml version=\"1.0\" standalone=\"yes\" ?>" + //$NON-NLS-1$
 			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " + //$NON-NLS-1$
 			"xmlns:ns=\"INSERT_URL_HERE\">" + //$NON-NLS-1$
 			"<soap:Body>" + //$NON-NLS-1$
 			"</soap:Body>" + //$NON-NLS-1$
 			"</soap:Envelope>";	 //$NON-NLS-1$
 			emptySOAP = WSTestUtils.addNLsToXML(emptySOAP);
 
 			if (bodyText.getText().trim().length() == 0) {
 				bodyText.setText(emptySOAP);
 			}
 			wsdlButton.setEnabled(true);
 		}
 		else if (wsType.equalsIgnoreCase(JAX_RS)) {
 			actionText.setEnabled(false);
 			bodyText.setEnabled(true);
 			methodCombo.setEnabled(true);
 			parmsList.setEnabled(true);
 			dlsList.setEnabled(true);
 			parmsTab.getControl().setEnabled(true);
 			headerTab.getControl().setEnabled(true);
 			methodCombo.setText(GET);
 			wsdlButton.setEnabled(false);
 		}
 		setMenusForCurrentState();
 	}
 
 	/*
 	 * Sets up the controls to call a public sample RESTful WS that does
 	 * a postal code lookup or a JAX-WS service that does a 
 	 * Shakespeare lookup. 
 	 */
 	private void setupSample() {
 		// go to http://www.geonames.org/export/web-services.html for example
 		//http://ws.geonames.org/postalCodeSearch?postalcode=9011&maxRows=10
 		if (wsTypeCombo.getText().equalsIgnoreCase(JAX_RS)) {
 			urlCombo.setText("http://ws.geonames.org/postalCodeSearch?"); //$NON-NLS-1$
 			parmsList.setSelection("postalcode=80920,maxRows=10"); //$NON-NLS-1$
 			dlsList.setSelection("content-type=application/xml"); //$NON-NLS-1$
 			methodCombo.setText(GET);
 			tabGroup.setSelection(parmsTab);
 			bodyText.setText(EMPTY_STRING);
 		}
 		else if (wsTypeCombo.getText().equalsIgnoreCase(JAX_WS)) {
 			String soapIn = "<?xml version=\"1.0\" standalone=\"yes\" ?>" + //$NON-NLS-1$
 			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " + //$NON-NLS-1$
 			"xmlns:ns=\"http://xmlme.com/WebServices\">" + //$NON-NLS-1$
 			"<soap:Body>" + //$NON-NLS-1$
 			"<ns:GetSpeech>" + //$NON-NLS-1$
 			"<ns:Request>slings and arrows</ns:Request>"+ //$NON-NLS-1$
 			"</ns:GetSpeech>"+ //$NON-NLS-1$
 			"</soap:Body>" + //$NON-NLS-1$
 			"</soap:Envelope>";	 //$NON-NLS-1$
 			soapIn = WSTestUtils.addNLsToXML(soapIn);
 
 			urlCombo.setText("http://www.xmlme.com/WSShakespeare.asmx"); //$NON-NLS-1$
 			actionText.setText("http://xmlme.com/WebServices/GetSpeech"); //$NON-NLS-1$
 			bodyText.setText(soapIn);
 			parmsList.setSelection(EMPTY_STRING);
 			dlsList.setSelection(EMPTY_STRING);
 			tabGroup.setSelection(bodyTab);
 		}
 		setControlsForWSType(wsTypeCombo.getText());
 		setControlsForMethodType(methodCombo.getText());
 		setControlsForSelectedURL();
 	}
 
 	/*
 	 * Actually perform the test based on which type of activity it is 
 	 */
 	private void handleTest(final String wsTech) {
 
 		if (urlCombo.getItemCount() > 0) {
 			java.util.List<String> aList = Arrays.asList(urlCombo.getItems());
 			if (!aList.contains(urlCombo.getText())) {
 				urlCombo.add(urlCombo.getText());
 			}
 		} else {
 			urlCombo.add(urlCombo.getText());
 		}
 
 		final String url = urlCombo.getText();
 		final String action = actionText.getText();
 		final String body = bodyText.getText();
 		final String method = methodCombo.getText();
 		final String headers = dlsList.getSelection();
 		final String parms = parmsList.getSelection();
 
 		Job aJob = new Job(JBossWSUIMessages.JAXRSWSTestView_Invoking_WS_Status) {
 			protected IStatus run(IProgressMonitor monitor) {
 				IStatus status = Status.OK_STATUS;
 				// execute the task ...
 				if (wsTech.equalsIgnoreCase(JAX_RS)) {
 					status = handleRSTest(monitor, url, method, body, parms, headers);
 				}
 				else if (wsTech.equalsIgnoreCase(JAX_WS)) {
 					status = handleWSTest(monitor, url, action, body);
 				}
 				monitor.done();
 				return status;  
 			}
 		};
 		// true to indicate that this job was initiated by a UI end user
 		aJob.setUser(true);		
 		aJob.addJobChangeListener(new IJobChangeListener() {
 
 			public void sleeping(IJobChangeEvent event) {};
 			public void scheduled(IJobChangeEvent event) {};
 			public void running(IJobChangeEvent event) {};
 			public void done(IJobChangeEvent event) {
 				if (event.getResult() instanceof WSTestStatus) {
 					final WSTestStatus status = (WSTestStatus) event.getResult();
 					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 						public void run() {
 							resultsText.setText(status.getResultsText());
 							resultHeadersList.removeAll();
 							String[] headers =
 								DelimitedStringList.parseString(status.getHeadersList(), RESULT_HEADER_DELIMITER);
 							if (headers != null && headers.length > 0) {
 								for (int i = 0; i < headers.length; i++) { 
 									resultHeadersList.add(headers[i]);
 								}
 							}
 						}
 					});
 				}
 			}
 
 			public void awake(IJobChangeEvent event) {};
 			public void aboutToRun(IJobChangeEvent event) {};
 		});
 		aJob.schedule();
 
 		setMenusForCurrentState();
 	}
 
 	/*
 	 * Actually call the WS and displays the result 
 	 */
 	private IStatus handleWSTest(final IProgressMonitor monitor, String url, String action, String body) {
 		try {
 
 			envelope = null;
 			monitor.worked(10);
 			JAXWSTester tester = new JAXWSTester();
 			tester.doTest(url, action, body);
 			monitor.worked(70);
 			String result = tester.getResultBody();
 			envelope = tester.getResultSOAP();
 			String cleanedUp = WSTestUtils.addNLsToXML(result);
 
 			WSTestStatus status = new WSTestStatus(IStatus.OK, 
 					JBossWSUIPlugin.PLUGIN_ID, 
 					JBossWSUIMessages.JAXRSWSTestView_JAXWS_Success_Status);
 			status.setResultsText(cleanedUp);
 			monitor.worked(10);
 
 			String listText = EMPTY_STRING;
 			if (tester.getResultHeaders() != null) {
 				Iterator<?> iter = tester.getResultHeaders().entrySet().iterator();
 				while (iter.hasNext()) {
 					String text = EMPTY_STRING;
 					Entry<?, ?> entry = (Entry<?, ?>) iter.next();
 					if (entry.getKey() == null) 
 						text = entry.getValue().toString();
 					else
 						text = text + entry.toString();
 					listText = listText + text;
 					if (iter.hasNext()) {
 						listText = listText + RESULT_HEADER_DELIMITER;
 					}
 				}
 			}
 			status.setHeadersList(listText);
 			monitor.worked(10);
 			return status;
 		} catch (Exception e) {
 			WSTestStatus status = new WSTestStatus(IStatus.ERROR, 
 					JBossWSUIPlugin.PLUGIN_ID, 
 					JBossWSUIMessages.JAXRSWSTestView_Exception_Status + e.getLocalizedMessage());
 			status.setResultsText(e.toString());
 			JBossWSUIPlugin.log(e);
 			return status;
 		}
 	}
 
 	/*
 	 * Actually call the RESTful WS to test it
 	 */
 	private IStatus handleRSTest(final IProgressMonitor monitor, String address, String method, String body, String parms, String headersStr) {
 
 		if (method.equalsIgnoreCase(GET))
 			body = EMPTY_STRING;
 
 		// if no actual text in the request body, set to null
 		if (body.trim().length() == 0) body = null;
 
 		monitor.worked(10);
 
 		// Process parameters for web service call
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		if (parms != null && parms.length() > 0) {
 			String[] parsedList = DelimitedStringList.parseString(parms , ","); //$NON-NLS-1$
 			if (parsedList != null && parsedList.length > 0) {
 				for (int i = 0; i < parsedList.length; i++) {
 					String nameValuePair = parsedList[i];
 					String[] nameAndValue = DelimitedStringList.parseString(nameValuePair, "="); //$NON-NLS-1$
 					if (nameAndValue != null && nameAndValue.length == 2) {
 						parameters.put(nameAndValue[0], nameAndValue[1]);
 					}
 				}
 			}
 		}
 
 		monitor.worked(10);
 		// Process headers for web service call
 		HashMap<String, String> headers = new HashMap<String, String>();
 		if (headersStr != null && headersStr.length() > 0) {
 			String[] parsedList = DelimitedStringList.parseString(headersStr , ","); //$NON-NLS-1$
 			if (parsedList != null && parsedList.length > 0) {
 				for (int i = 0; i < parsedList.length; i++) {
 					String nameValuePair = parsedList[i];
 					String[] nameAndValue = DelimitedStringList.parseString(nameValuePair, "="); //$NON-NLS-1$
 					if (nameAndValue != null && nameAndValue.length == 2) {
 						headers.put(nameAndValue[0], nameAndValue[1]);
 					}
 				}
 			}
 		}
 
 		JAXRSTester tester = new JAXRSTester();
 
 		// now actually call it
 		try {
 
 			// call the service
 			tester.doTest(address, parameters, headers, method, body);
 
 			String result = tester.getResultBody();
 
 			// put the results in the result text field
 			String cleanedUp = WSTestUtils.addNLsToXML(result);
 
 			WSTestStatus status = new WSTestStatus(IStatus.OK, 
 					JBossWSUIPlugin.PLUGIN_ID, 
 					JBossWSUIMessages.JAXRSWSTestView_JAXRS_Success_Status);
 			status.setResultsText(cleanedUp);
 
 			String listText = EMPTY_STRING;
 			if (tester.getResultHeaders() != null) {
 				Iterator<?> iter = tester.getResultHeaders().entrySet().iterator();
 				while (iter.hasNext()) {
 					String text = EMPTY_STRING;
 					Entry<?, ?> entry = (Entry<?, ?>) iter.next();
 					if (entry.getKey() == null) 
 						text = entry.getValue().toString();
 					else
 						text = text + entry.toString();
 					listText = listText + text;
 					if (iter.hasNext()) {
 						listText = listText + RESULT_HEADER_DELIMITER;
 					}
 				}
 			}
 
 			status.setHeadersList(listText);
 			monitor.worked(10);
 			return status;
 
 		} catch (Exception e) {
 			String result = tester.getResultBody();
 
 			// put the results in the result text field
 			String cleanedUp = WSTestUtils.addNLsToXML(result);
 
 			WSTestStatus status = new WSTestStatus(IStatus.OK, 
 					JBossWSUIPlugin.PLUGIN_ID, 
 					JBossWSUIMessages.JAXRSWSTestView_JAXRS_Success_Status);
 			status.setResultsText(cleanedUp);
 
 			String listText = EMPTY_STRING;
 			if (tester.getResultHeaders() != null) {
 				Iterator<?> iter = tester.getResultHeaders().entrySet().iterator();
 				while (iter.hasNext()) {
 					String text = EMPTY_STRING;
 					Entry<?, ?> entry = (Entry<?, ?>) iter.next();
 					if (entry.getKey() == null) 
 						text = entry.getValue().toString();
 					else
 						text = text + entry.toString();
 					listText = listText + text;
 					if (iter.hasNext()) {
 						listText = listText + RESULT_HEADER_DELIMITER;
 					}
 				}
 			}
 
 			status.setHeadersList(listText);
 			monitor.worked(10);
 			return status;
 		}
 	}
 
 	/**
 	 * Passing the focus request to the control.
 	 */
 	public void setFocus() {
 		// set initial focus to the URL text combo
 		urlCombo.setFocus();
 	}
 
 }
