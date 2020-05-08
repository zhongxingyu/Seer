 /*******************************************************************************
  * Copyright (c) 2013 Remain Software and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wim Jongman and Markus Kuppe 
  *******************************************************************************/
 package org.eclipse.ecf.example.chat.ui.parts;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.e4.ui.di.UISynchronize;
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.ecf.example.chat.model.IChatMessage;
 import org.eclipse.ecf.example.chat.tracker.CentralisticChatTracker;
 import org.eclipse.ecf.example.chat.tracker.ChatTracker;
 import org.eclipse.ecf.example.chat.tracker.IPointToPointChatListener;
 import org.eclipse.ecf.example.chat.tracker.P2PChatTracker;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.wb.swt.ResourceManager;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 public class ChatPart implements IPointToPointChatListener {
 	private Text fMessage;
 	private String fLastMessage;
 	private Composite fStackComposite;
 	private final FormToolkit fFormToolkit = new FormToolkit(
 			Display.getDefault());
 	private Text fServer;
 	private Text fHandle;
 	private Text fParticipants;
 	private SashForm sashForm;
 	private MessageComposite messageComposite;
 	private Button btnServerMode;
 	private ChatTracker fTracker;
 
 	@Inject
 	private UISynchronize sync;
 	private Composite fMessageComposite;
 
 	@PostConstruct
 	public void createComposite(final Composite parent,
 			@Optional final ConfigurationAdmin cm, MPart part, final Shell shell)
 			throws UnknownHostException {
 		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		fStackComposite = new Composite(parent, SWT.NONE);
 		final StackLayout stackLayout = new StackLayout();
 		fStackComposite.setLayout(stackLayout);
 
 		ScrolledForm loginForm = fFormToolkit
 				.createScrolledForm(fStackComposite);
 		loginForm.setImage(ResourceManager.getPluginImage(
 				"org.eclipse.ecf.example.chat.product", "icons/login.gif"));
 		loginForm.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.BOLD));
 		fFormToolkit.paintBordersFor(loginForm);
 		loginForm.setText("Login");
 		loginForm.getBody().setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		Composite loginBody = fFormToolkit.createComposite(loginForm.getBody(),
 				SWT.NONE);
 		fFormToolkit.paintBordersFor(loginBody);
 		loginBody.setLayout(new GridLayout(3, false));
 
 		// Handle
 		fFormToolkit.createLabel(loginBody, "Handle", SWT.NONE);
 
 		fHandle = fFormToolkit.createText(loginBody, "", SWT.NONE);
 		if (part.getElementId()
 				.equals("org.eclipse.ecf.example.chat.ui.part.0")) {
 			String canonicalHostName = "";
 			try {
 				canonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 				canonicalHostName = "unknown";
 			}
 			fHandle.setText(System.getProperty("user.name", "nobody") + "@"
 					+ canonicalHostName);
 		} else {
 			fHandle.setText(part.getLabel());
 		}
 		fHandle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2,
 				1));
 
 		// Server
 		fFormToolkit.createLabel(loginBody, "DiscoServer", SWT.NONE);
 
 		fServer = fFormToolkit.createText(loginBody, "", SWT.NONE);
 		fServer.setText("zk://disco.ecf-project.org");
 		fServer.selectAll();
 		fServer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2,
 				1));
 		if (cm == null) {
 			fServer.setEnabled(false);
 			fServer.setText("No discovery and/or ConfigurationAdmin available");
 		}
 
 		// centralistic mode
 		fFormToolkit.createLabel(loginBody, "Centralistic Mode", SWT.NONE);
 
 		btnServerMode = fFormToolkit.createButton(loginBody, "", SWT.CHECK);
 
 		final Button btnLogin = fFormToolkit.createButton(loginBody, "Go online",
 				SWT.NONE);
 		btnLogin.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				final boolean serverMode = btnServerMode.getSelection();
 				final String handle = fHandle.getText();
 				final URI uri;
 				if (fServer.isEnabled()) {
 					uri = URI.create(fServer.getText());
 				} else {
 					uri = URI.create("no://host");
 				}
 				
 				final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
 						shell);
 				try {
 					dialog.run(true, false, new IRunnableWithProgress() {
 
 						@Override
 						public void run(IProgressMonitor monitor)
 								throws InvocationTargetException,
 								InterruptedException {
							doLogin(serverMode, handle, uri.getHost(), cm);
 							sync.syncExec(new Runnable() {
 								@Override
 								public void run() {
 									fHandle.getShell().setText(
 											fHandle.getShell().getText() + ": "
 													+ fHandle.getText());
 									stackLayout.topControl = fMessageComposite;
 									fStackComposite.layout();
 								}
 							});
 						}
 					});
 				} catch (InvocationTargetException doesNotHappen) {
 					doesNotHappen.printStackTrace();
 				} catch (InterruptedException doesNotHappen) {
 					doesNotHappen.printStackTrace();
 				}
 			}
 		});
 		btnLogin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		
 		// Only allow to go only on a correc URI
 		fServer.addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				if (fServer.isEnabled()) {
 					String text = fServer.getText();
 					try {
 						URI uri = new URI(text);
 						if (uri.getScheme() != null && uri.getHost() != null) {
 							btnLogin.setEnabled(true);
 							return;
 						}
 					} catch (URISyntaxException e1) {
 					}
 					btnLogin.setEnabled(false);
 				}
 			}
 		});
 
 
 		stackLayout.topControl = loginForm;
 
 		fMessageComposite = fFormToolkit.createComposite(fStackComposite, SWT.NONE);
 		fFormToolkit.paintBordersFor(fMessageComposite);
 		GridLayout gl_fMessageComposite = new GridLayout(2, false);
 		fMessageComposite.setLayout(gl_fMessageComposite);
 
 		Composite composite = fFormToolkit.createComposite(fMessageComposite, SWT.NONE);
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
 				2, 1));
 		GridLayout gl_fMessageForm = new GridLayout(1, false);
 		gl_fMessageForm.marginHeight = 0;
 		gl_fMessageForm.marginWidth = 0;
 		composite.setLayout(gl_fMessageForm);
 		fFormToolkit.paintBordersFor(composite);
 
 		sashForm = new SashForm(composite, SWT.SMOOTH);
 		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
 				1));
 		fFormToolkit.adapt(sashForm);
 		fFormToolkit.paintBordersFor(sashForm);
 
 		messageComposite = new MessageComposite(sashForm, SWT.NONE);
 		fFormToolkit.adapt(messageComposite);
 		fFormToolkit.paintBordersFor(messageComposite);
 
 		fParticipants = fFormToolkit.createText(sashForm, "", SWT.READ_ONLY
 				| SWT.MULTI);
 		fParticipants.setForeground(SWTResourceManager
 				.getColor(SWT.COLOR_DARK_BLUE));
 		fParticipants.setFont(SWTResourceManager
 				.getFont("Courier", 9, SWT.BOLD));
 		sashForm.setWeights(new int[] { 4, 1 });
 
 		fMessage = new Text(fMessageComposite, SWT.BORDER);
 		fMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
 				1, 1));
 
 		Button btnSend = new Button(fMessageComposite, SWT.NONE);
 		btnSend.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,
 				1));
 		btnSend.setText("Send");
 		btnSend.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				final String text = fMessage.getText();
 				final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
 						shell);
 				try {
 					dialog.run(true, false, new IRunnableWithProgress() {
 						@Override
 						public void run(IProgressMonitor monitor)
 								throws InvocationTargetException,
 								InterruptedException {
 							fTracker.publish(text);
 							sync.syncExec(new Runnable() {
 								@Override
 								public void run() {
 									fMessage.setText("");
 									fMessage.setFocus();
 								}
 							});
 						}
 					});
 				} catch (InvocationTargetException doesNotHappen) {
 					doesNotHappen.printStackTrace();
 				} catch (InterruptedException doesNotHappen) {
 					doesNotHappen.printStackTrace();
 				}
 			}
 		});
 		btnSend.getShell().setDefaultButton(btnSend);
 	}
 
	private void doLogin(boolean serverMode, String handle, String discoServer,
 			ConfigurationAdmin cm) {
 		setupTracker(serverMode, handle, discoServer, cm);
 	}
 
 	private void setupTracker(boolean serverMode, String handle,
			String discoServer, ConfigurationAdmin cm) {
 		if (serverMode == true) {
 			fTracker = new CentralisticChatTracker(this, handle);
 		} else {
 			fTracker = new P2PChatTracker(this, handle);
 		}
 
 		if (cm != null) {
 			try {
 				Configuration configuration = cm.getConfiguration(
 						"org.eclipse.ecf.example.chat.config", "?");
 				Dictionary<String, Object> properties = configuration
 						.getProperties();
 				if (properties == null) {
 					properties = new Hashtable<String, Object>();
 				}
				properties.put("SERVER", discoServer);
 				configuration.update(properties);
 			} catch (IOException doesNotHappen) {
 				doesNotHappen.printStackTrace();
 			}
 		}
 
 		fTracker.setup();
 	}
 	
 	// If CM comes up or goes down dynamically
 	@Inject
 	private void setConfigAdmin(@Optional final ConfigurationAdmin cm, UISynchronize sync) {
 		sync.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				if (cm == null) {
 					fServer.setEnabled(false);
 				} else {
 					fServer.setEnabled(true);
 					fServer.setText("zk://disco.ecf-project.org");
 				}
 			}
 		});
 	}
 
 	private synchronized void processParticipantsList() {
 		String[] participants = fTracker.getSortedParticipants();
 		StringBuilder result = new StringBuilder();
 		for (String user : participants) {
 			result.append(user).append("\r\n");
 		}
 		if (!fParticipants.isDisposed())
 			fParticipants.setText(result.toString());
 	}
 
 	@PreDestroy
 	private void dispose() {
 		if (fFormToolkit != null) {
 			fFormToolkit.dispose();
 		}
 		// If user never connects but ends the application right away, fTracker
 		// is null
 		if (fTracker != null) {
 			fTracker.dispose();
 		}
 	}
 
 	@Focus
 	public void setFocus() {
 		fMessage.setFocus();
 	}
 
 	@Override
 	public synchronized void messageRecevied(final IChatMessage message) {
 		if (!message.getMessage().equals(fLastMessage)) {
 			fLastMessage = message.getMessage();
 			sync.asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					boolean isLocal = fHandle.getText().equals(
 							message.getHandle());
 					messageComposite.addItem(new ChatElement(fLastMessage,
 							message.getHandle(), new Date(), isLocal));
 				}
 			});
 		}
 
 	}
 
 	@Override
 	public synchronized void joined(final String handle) {
 		sync.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				processParticipantsList();
 				messageComposite.addItem(new ChatElement(handle, new Date(),
 						ChatElement.State.JOINED));
 			}
 		});
 	}
 
 	@Override
 	public synchronized void left(final String handle) {
 		sync.asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				processParticipantsList();
 				if (!messageComposite.isDisposed()) {
 					messageComposite.addItem(new ChatElement(handle,
 							new Date(), ChatElement.State.LEFT));
 				}
 			}
 		});
 	}
 }
