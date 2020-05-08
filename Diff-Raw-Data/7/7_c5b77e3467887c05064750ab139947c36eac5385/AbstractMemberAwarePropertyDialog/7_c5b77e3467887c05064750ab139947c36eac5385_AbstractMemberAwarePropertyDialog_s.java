 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.behavior.dialogs;
 
 import java.util.HashSet;
 
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.Attribute;
 import org.eclipse.etrice.core.room.Operation;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 import org.eclipse.etrice.ui.behavior.dialogs.PortMessageSelectionDialog.MsgItemPair;
 import org.eclipse.etrice.ui.behavior.dialogs.PortMessageSelectionDialog.OperationItemPair;
 import org.eclipse.etrice.ui.common.dialogs.AbstractPropertyDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * @author Henrik Rentz-Reichert
  *
  */
 public abstract class AbstractMemberAwarePropertyDialog extends AbstractPropertyDialog {
 	
 	private class LastTextListener implements FocusListener {
 
 		@Override
 		public void focusGained(FocusEvent e) {
 			if (e.getSource() instanceof Text) {
 				boolean enableMemberButton = memberAware.contains(e.getSource());
 				boolean enableMessageButton = messageAware.contains(e.getSource());
 				if (enableMemberButton || enableMessageButton)
 					lastTextField = (Text) e.getSource();
 				else
 					lastTextField = null;
 				members.setEnabled(enableMemberButton);
 				messages.setEnabled(enableMessageButton);
 			}
 			else {
 				lastTextField = null;
 				members.setEnabled(false);
 				messages.setEnabled(false);
 			}
 		}
 
 		@Override
 		public void focusLost(FocusEvent e) {
 		}
 	}
 
 	private Text lastTextField = null;
 	private Button members;
 	private Button messages;
 	private ActorClass ac;
 	private LastTextListener listener = new LastTextListener();
 	private HashSet<Control> memberAware = new HashSet<Control>();
 	private HashSet<Control> messageAware = new HashSet<Control>();
 	private HashSet<Control> recvOnly = new HashSet<Control>();
 	
 	/**
 	 * @param shell
 	 * @param title
 	 * @param ac
 	 */
 	public AbstractMemberAwarePropertyDialog(Shell shell, String title, ActorClass ac) {
 		super(shell, title);
 		this.ac = ac;
 	}
 
 	/**
 	 * @return the ac
 	 */
 	public ActorClass getActorClass() {
 		return ac;
 	}
 
 	/**
 	 * @param body
 	 */
 	protected void createMembersAndMessagesButtons(Composite body) {
 		Composite buttonsArea = getToolkit().createComposite(body);
 		buttonsArea.setLayout(new GridLayout(2, true));
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		gd.horizontalSpan = 2;
 		buttonsArea.setLayoutData(gd);
 		
 		members = new Button(buttonsArea, SWT.PUSH);
 		members.setText("Mem&bers");
 		gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		members.setLayoutData(gd);
 		members.setEnabled(false);
 		members.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleMembersPressed();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				handleMembersPressed();
 			}
 		});
 		
 		messages = new Button(buttonsArea, SWT.PUSH);
 		messages.setText("Me&ssages");
 		gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		messages.setLayoutData(gd);
 		messages.setEnabled(false);
 		messages.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleMessagesPressed();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				handleMessagesPressed();
 			}
 		});
 	}
 
 	protected void handleMembersPressed() {
 		MemberSelectionDialog dlg = new MemberSelectionDialog(getShell(), ac);
 		if (dlg.open()==Window.OK) {
 			Object selected = dlg.getSelected();
 			if (selected instanceof Attribute)
 				insertText(((Attribute)selected).getName());
 			else if (selected instanceof Operation)
 				insertText(((Operation)selected).getName()+RoomHelpers.getSignature((Operation) selected));
 		}
 	}
 
 	protected void handleMessagesPressed() {
 		boolean receiveOnly = recvOnly.contains(lastTextField);
 		PortMessageSelectionDialog dlg = new PortMessageSelectionDialog(getShell(), ac, receiveOnly);
 		if (dlg.open()==Window.OK) {
 			if (dlg.getMethodItemPair()!=null) {
 				if (dlg.getMethodItemPair() instanceof MsgItemPair) {
 					MsgItemPair pair = (MsgItemPair) dlg.getMethodItemPair();
 					if (pair.out) {
 						String data = pair.msg.getData()!=null? pair.msg.getData().getName() : "";
						insertText(pair.item.getName()+"."+pair.msg.getName()+"("+data+")");
 					}
 					else
 						insertText(pair.item.getName()+"."+pair.msg.getName());
 				}
 				if (dlg.getMethodItemPair() instanceof OperationItemPair) {
 					OperationItemPair pair = (OperationItemPair) dlg.getMethodItemPair();
 					String arglist = RoomHelpers.getArguments(pair.op);
 					insertText(pair.item.getName()+"."+pair.op.getName()+arglist);
 				}
 			}
 		}
 	}
 
 	private void insertText(String txt) {
 		if (lastTextField!=null) {
 			int begin = txt.indexOf('(');
 			int end = txt.indexOf(')');
 			int offset = lastTextField.getSelection().x;
 			lastTextField.insert(txt);
 			if (begin>=0 && end>=0 && end>begin+1)
 				lastTextField.setSelection(offset+begin+1, offset+end);
 			lastTextField.setFocus();
 		}
 	}
 
 	public void configureMemberAware(Control ctrl) {
 		configureMemberAware(ctrl, false, false);
 	}
 	
 	public void configureMemberAware(Control ctrl, boolean useMembers, boolean useMessages) {
 		configureMemberAware(ctrl, useMembers, useMembers, false);
 	}
 	
 	public void configureMemberAware(Control ctrl, boolean useMembers, boolean useMessages, boolean useRecvMessagesOnly) {
 		if (useMembers)
 			memberAware.add(ctrl);
 		if (useMessages)
 			messageAware.add(ctrl);
 		if (useRecvMessagesOnly)
 			recvOnly.add(ctrl);
 		ctrl.addFocusListener(listener);
 	}
 }
