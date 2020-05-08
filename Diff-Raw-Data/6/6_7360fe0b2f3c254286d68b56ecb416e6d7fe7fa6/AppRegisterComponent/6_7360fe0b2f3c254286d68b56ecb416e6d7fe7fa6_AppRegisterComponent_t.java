 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.wizards.appregister;
 
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import org.eclipse.swt.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.TaskModel;
 import org.eclipse.wst.server.ui.internal.wizard.*;
 import org.eclipse.wst.server.ui.internal.wizard.fragment.NewRuntimeWizardFragment;
 import org.eclipse.wst.server.ui.wizard.WizardFragment;
 import org.jboss.tools.common.model.ui.attribute.XAttributeSupport;
 import org.jboss.tools.common.model.ui.attribute.adapter.IModelPropertyEditorAdapter;
 import org.jboss.tools.common.model.ui.attribute.editor.ExtendedFieldEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.IMutableFieldEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.IPropertyEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.MutableComboBoxFieldEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.MutableMultipleChoiceFieldEditor;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 
 
 import org.jboss.tools.common.meta.XAttribute;
 import org.jboss.tools.common.meta.XModelEntity;
 import org.jboss.tools.common.meta.action.SpecialWizard;
 import org.jboss.tools.common.meta.action.XEntityData;
 import org.jboss.tools.common.meta.action.impl.XEntityDataImpl;
 import org.jboss.tools.common.meta.action.impl.handlers.HUtil;
 import org.jboss.tools.common.meta.constraint.impl.XAttributeEditorImpl;
import org.jboss.tools.common.meta.impl.XModelMetaDataImpl;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.options.PreferenceModelUtilities;
 import org.jboss.tools.common.model.util.XModelObjectUtil;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.context.RegisterServerContext;
 import org.jboss.tools.jst.web.server.*;
 import org.jboss.tools.jst.web.ui.Messages;
 
 public class AppRegisterComponent {
 	static String ENTITY = "WebPrjRegisterApplication"; //$NON-NLS-1$
 	static String ATTR_REGISTER = "register"; //$NON-NLS-1$
 	static String ATTR_LOCATION = "location"; //$NON-NLS-1$
 	static String ATTR_APP_NAME = "application name"; //$NON-NLS-1$
 	static String ATTR_RUNTIME = "runtime"; //$NON-NLS-1$
 	static String ATTR_TARGET_SERVER = "target server"; //$NON-NLS-1$
 	static String ATTR_SEPARATOR = "separator"; //$NON-NLS-1$
 	
 	RegisterServerContext context;
 	boolean isEnabling = true;
 	XAttributeSupport enableSupport;
 	private IModelPropertyEditorAdapter enableAdapter;
 	XAttributeSupport support;
 	Layout supportLayout;
 	Composite supportControl;
 	AddServer addServer = new AddServer();
 	AddRuntime addRuntime = new AddRuntime();
 
 	boolean disableContextRoot = false;
 	
 	public void setLight(boolean b) {
 	}
 
 	public void setDisableContextRoot(boolean disableContextRoot) {
 		this.disableContextRoot = disableContextRoot;
 	}
 	
 	public void setLayoutForSupport(Layout layout) {
 		supportLayout = layout;
 	}
 	
 	public void dispose() {
 		if (enableSupport!=null) enableSupport.dispose();
 		enableSupport = null;
 		if (support!=null) support.dispose();
 		support = null;
 		if (enableAdapter!=null) enableAdapter.dispose();
 		enableAdapter = null;
 		context = null;
 	}
 	
 	public void setEnabling(boolean b) {
 		isEnabling = b;
 	}
 	
 	public void setContext(RegisterServerContext context) {
 		this.context = context;
 	}
 	
 	public void init() {
 		XModelObject dummy = PreferenceModelUtilities.getPreferenceModel().getRoot();
 		String[][] attInfo = null;
 		attInfo = new String[][] {
 			{ENTITY, ""}, {ATTR_APP_NAME, "yes"}, {ATTR_RUNTIME, "yes"}, {ATTR_SEPARATOR, "no"}, {ATTR_TARGET_SERVER, "no"} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		};
 
 		initDisablingContextRoot();
 		
 		XEntityData entityData = XEntityDataImpl.create(attInfo);
 		support = new XAttributeSupport() {
 			protected boolean keepGreedy(String name, int index, int greedyCount) {
 				return true;
 			}
 		};
 		support.init(dummy, entityData);
 		entityData = XEntityDataImpl.create(new String[][] {{ENTITY, ""}, {ATTR_REGISTER, "no"}}); //$NON-NLS-1$ //$NON-NLS-2$
 		if(isEnabling()) {
 			enableSupport = new XAttributeSupport();
 			enableSupport.init(dummy, entityData);
 			enableAdapter = (IModelPropertyEditorAdapter)enableSupport.getPropertyEditorAdapterByName(ATTR_REGISTER);
 			enableAdapter.addValueChangeListener(new EnableAdapterListener());		
 		}
 		initValues();
 		IModelPropertyEditorAdapter a = support.getPropertyEditorAdapterByName(ATTR_TARGET_SERVER);
 		a.addValueChangeListener(new ServerInputChangeListener());
 		a = support.getPropertyEditorAdapterByName(ATTR_RUNTIME);
 		a.addValueChangeListener(new RuntimeInputChangeListener());
 	}
 
 	void initDisablingContextRoot() {
		XModelEntity entity = XModelMetaDataImpl.getInstance().getEntity(ENTITY);
 		XAttribute attr = entity.getAttribute(ATTR_APP_NAME);
 		XAttributeEditorImpl ed = (XAttributeEditorImpl)attr.getEditor();
 		if(disableContextRoot) {
 			ed.setName("Uneditable"); //$NON-NLS-1$
 		} else {
 			ed.setName("Text"); //$NON-NLS-1$
 		}
 	}
 	
 	public boolean isEnabling() {
 		return isEnabling;
 	}
 	
 	public void loadApplicationName() {
 		String n = context.getApplicationName() == null ? "" : context.getApplicationName(); //$NON-NLS-1$
 		support.getPropertyEditorAdapterByName(ATTR_APP_NAME).setValue(n);
 	}
 	
 	public String getApplicationName() {
 		return support.getPropertyEditorAdapterByName(ATTR_APP_NAME).getStringValue(true);
 	}
 	
 	public Control createControl(Composite parent) {
 		return (isEnabling()) ? createEnablingControl(parent) : createEnabledControl(parent);
 	}
 	
 	public Control createEnablingControl(Composite parent) {
 		Composite c = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.marginWidth = 0;
 		c.setLayout(layout);
 		enableSupport.setLayout(getEnablingLayout());
 		enableSupport.createControl(c);
 		setLableText(enableSupport, "register"); //$NON-NLS-1$
 		Group group = new Group(c, SWT.SHADOW_ETCHED_IN);
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		group.setLayout(new GridLayout());
 		createSupportControl(group);
 		updateEnablement();
 		return c;
 	}
 	
 	private void setLableText(XAttributeSupport s, String field) {
 		ExtendedFieldEditor f = (ExtendedFieldEditor)s.getFieldEditorByName(field);
 		if(f == null) return;
 		String t = f.getLabelText();
 		int i = (t.indexOf("%1")); //$NON-NLS-1$
 		if(i < 0) return;
 		String insert = ""; //$NON-NLS-1$
 		IServer server = ServerManager.getInstance().getSelectedServer();
 		if(server != null) {
 			insert = server.getServerType().getName();
 		} else {
 			insert = "server"; //$NON-NLS-1$
 		}
 		t = t.substring(0, i) + insert + t.substring(i + 2);
 		f.setLabelText(t);
 	}
 
 	public Control createEnabledControl(Composite parent) {
 		Control sc = createSupportControl(parent);
 		updateEnablement();
 		return sc;
 	}
 	
 	private Control createSupportControl(Composite parent) {
 		if(supportLayout != null) support.setLayout(supportLayout);
 		Control sc = support.createControl(parent);
 		setLableText(support, "location"); //$NON-NLS-1$
 		supportControl = (Composite)sc;
 		GridData g2 = new GridData(GridData.FILL_HORIZONTAL);
 		g2.horizontalIndent = 0;
 		sc.setLayoutData(g2);
 		IMutableFieldEditor editor = (IMutableFieldEditor)support.getFieldEditorByName(ATTR_TARGET_SERVER);
 		editor.setChange(addServer);
 		editor = (IMutableFieldEditor)support.getFieldEditorByName(ATTR_RUNTIME);
 		editor.setChange(addRuntime);
 		return sc;
 	}
 	
 	Layout getEnablingLayout() {
 		GridLayout gridLayout = new GridLayout(2, false);
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.horizontalSpacing = 0;
 		gridLayout.verticalSpacing = 0;
 		return gridLayout;
 	}
 	
 	private void initValues() {
 		if(isEnabling()) {
 			enableSupport.getEntityData().setValue(ATTR_REGISTER, context.isEnabled() ? "yes" : "no"); //$NON-NLS-1$ //$NON-NLS-2$
 			enableSupport.load();
 		}
 		String n = context.getApplicationName() == null ? "" : context.getApplicationName(); //$NON-NLS-1$
 		support.getEntityData().setValue(ATTR_APP_NAME, n);
 		
 		initRuntimeValue();
 		initTargetServerValue();
 		support.load();
 	}
 	
 	void initRuntimeValue() {
 		IRuntime[] rs = ServerCore.getRuntimes();
 		IServer selectedServer = ServerManager.getInstance().getSelectedServer();
 		IRuntime selectedRuntime = (selectedServer != null) ? selectedServer.getRuntime() : null;
 		IRuntime selectedRuntime1 = null;
 		String[] ns = new String[rs.length];
 		for (int i = 0; i < rs.length; i++) {
 			ns[i] = rs[i].getName();
 			if(rs[i] == selectedRuntime || selectedRuntime1 == null) selectedRuntime1 = rs[i];
 		}
 		IProject p = context.getProjectHandle();
 		if(p != null && p.exists() && p.isAccessible()) {
 			try {
 				IRuntime sr = J2EEProjectUtilities.getServerRuntime(p);
 				if(sr != null) selectedRuntime1 = sr;
 			} catch (CoreException exc) {
 				WebModelPlugin.getPluginLog().logError(exc);
 			}
 		}
 		HUtil.hackAttributeConstraintList(new XEntityData[]{support.getEntityData()}, 0, ATTR_RUNTIME,  ns);
 		String runtimeName = (selectedRuntime1 != null) ? selectedRuntime1.getName() : ""; //$NON-NLS-1$
 		context.setRuntimeName(runtimeName);
 		support.getEntityData().setValue(ATTR_RUNTIME, runtimeName);
 	}
 	
 	void initTargetServerValue() {
 		String runtimeName = context.getRuntimeName();
 		IServer[] is = ServerCore.getServers();
 		IServer selected = null;
 		IServer selected1 = null;
 		ArrayList<String> l = new ArrayList<String>();
 		IServer sel = ServerManager.getInstance().getSelectedServer();
 		if(sel != null) selected = sel;
 		for (int i = 0; i < is.length; i++) {
 			if(is[i].getRuntime() != null &&
 					runtimeName.equals(is[i].getRuntime().getName())) {
 				if(selected == is[i] || selected1 == null) {
 					selected1 = is[i];
 				}
 				l.add(is[i].getName());
 			}
 		}
 		String[] vs = (String[])l.toArray(new String[0]);
 		HUtil.hackAttributeConstraintList(new XEntityData[]{support.getEntityData()}, 0, ATTR_TARGET_SERVER,  vs);
 		selected = selected1;
 		if(selected != null && context.isInitiallyEnabled()) {
 			context.setTargetServers(new IServer[]{selected});
 			support.getEntityData().setValue(ATTR_TARGET_SERVER, selected.getName());
 		} else {
 			context.setTargetServers(new IServer[]{});
 			support.getEntityData().setValue(ATTR_TARGET_SERVER, ""); //$NON-NLS-1$
 		}
 	}
 	
 	class EnableAdapterListener implements java.beans.PropertyChangeListener {
 		public void propertyChange(java.beans.PropertyChangeEvent evt) {
 			updateEnablement();
 		}		
 	}
 	
 	void updateEnablement() {
 		if(supportControl == null || supportControl.isDisposed()) return;
 		boolean enabled = isRegisterEnabled();
 		support.getPropertyEditorByName(ATTR_APP_NAME).getFieldEditor(supportControl).setEnabled(enabled, supportControl);
 		support.getPropertyEditorByName(ATTR_TARGET_SERVER).getFieldEditor(supportControl).setEnabled(enabled, supportControl);
 	}
 	
 	boolean isRegisterEnabled() {
 		return !isEnabling() || ("yes".equals(enableAdapter.getValue())); //$NON-NLS-1$
 	}
 	
 	public String getErrorMessage() {
 		commit();
 		return context.getErrorMessage();
 	}
 	
 	public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
 		support.addPropertyChangeListener(listener);
 		if(isEnabling()) enableSupport.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
 		support.removePropertyChangeListener(listener);
 		if(isEnabling()) enableSupport.removePropertyChangeListener(listener);
 	}
 	
 	public void commit() {
 		Properties p = support.getValues();
 		context.setApplicationName(p.getProperty(ATTR_APP_NAME));
 		context.setEnabled(isRegisterEnabled());
 	}
 	
 	class RuntimeInputChangeListener implements java.beans.PropertyChangeListener {
 		public void propertyChange(java.beans.PropertyChangeEvent evt) {
 			String value = (String)evt.getNewValue();
 			onRuntimeChange(value);
 		}
 	}
 
 	void onRuntimeChange(String value) {
 		value = (value == null) ? "" : value.trim(); //$NON-NLS-1$
 		if(value.equals(context.getRuntimeName())) return;
 		context.setRuntimeName(value);
 		initTargetServerValue();
 		MutableMultipleChoiceFieldEditor ed = (MutableMultipleChoiceFieldEditor)support.getFieldEditorByName(ATTR_TARGET_SERVER);
 		ed.propertyChange(new PropertyChangeEvent(this, IPropertyEditor.LIST_CONTENT, "old", "new"));			 //$NON-NLS-1$ //$NON-NLS-2$
 	}
 	
 	class ServerInputChangeListener implements java.beans.PropertyChangeListener {
 		public void propertyChange(java.beans.PropertyChangeEvent evt) {
 			String value = (String)evt.getNewValue();
 			onTargetServerChange(value);
 		}
 	}
 	
 	void onTargetServerChange(String value) {
 		ArrayList<IServer> list = new ArrayList<IServer>();
 		String[] s = XModelObjectUtil.asStringArray(value);
 		IServer[] is = ServerCore.getServers();
 		for (int i = 0; i < is.length; i++) {
 			for (int j = 0; j < s.length; j++) {
 				if(is[i].getName().equals(s[j])) {
 					if(!list.contains(is[i])) list.add(is[i]);
 					break;
 				}
 			}
 		}
 		context.setTargetServers(list.toArray(new IServer[0]));
 	}
 	
 	class AddServer implements SpecialWizard {
 		Properties p;
 
 		public void setObject(Object object) {
 			p = (Properties)object;
 		}
 
 		public int execute() {
 			Shell shell = (Shell)p.get("shell"); //$NON-NLS-1$
 			IServer server = newServer(shell);
 			if(server == null) return -1;
 
 			IModelPropertyEditorAdapter a = support.getPropertyEditorAdapterByName(ATTR_RUNTIME);
 			if(server.getRuntime() != null && !server.getRuntime().getName().equals(a.getValue())) {
 				MutableComboBoxFieldEditor ed1 = (MutableComboBoxFieldEditor)support.getFieldEditorByName(ATTR_RUNTIME);
 				ed1.setNewValue(server.getRuntime().getName());
 			}			
 			
 			p.setProperty("value", server.getName()); //$NON-NLS-1$
 			initTargetServerValue();
 			MutableMultipleChoiceFieldEditor ed = (MutableMultipleChoiceFieldEditor)support.getFieldEditorByName(ATTR_TARGET_SERVER);
 			ed.propertyChange(new PropertyChangeEvent(this, IPropertyEditor.LIST_CONTENT, "old", "new"));			 //$NON-NLS-1$ //$NON-NLS-2$
 			return 0;
 		}
 		
 	}
 	
 	private IServer newServer(Shell shell) {
 		NewServerWizard wizard = new NewServerWizard();
 		WizardDialog dialog = new WizardDialog(shell, wizard);
 		
 		if (dialog.open() == Window.CANCEL) {
 			return null;
 		}
 		IServer server = (IServer)wizard.getRootFragment().getTaskModel().getObject(TaskModel.TASK_SERVER);
 		if(server == null) return null;
 		ServerManager.getInstance().setSelectedServer(server.getId());
 		return server;
 	}
 
 	class AddRuntime implements SpecialWizard {
 		Properties p;
 
 		public void setObject(Object object) {
 			p = (Properties)object;
 		}
 
 		public int execute() {
 			Shell shell = (Shell)p.get("shell"); //$NON-NLS-1$
 			IRuntime runtime = newRuntime(shell);
 			if(runtime == null) return -1;
 			p.setProperty("value", runtime.getName()); //$NON-NLS-1$
 			initRuntimeValue();
 			context.setRuntimeName("");  //prepare for change //$NON-NLS-1$
 			return 0;
 		}
 		
 	}
 	
 	private IRuntime newRuntime(Shell shell) {
 		WizardFragment fragment = null;
 		String title = Messages.AppRegisterComponent_NewServerRuntime; // ServerUIPlugin.getResource2("wizNewRuntimeWizardTitle");
 		fragment = new WizardFragment() {
 			protected void createChildFragments(List list) {
 				list.add(new NewRuntimeWizardFragment());
 				list.add(WizardTaskUtil.SaveRuntimeFragment);
 			}
 		};
 		TaskWizard wizard = new TaskWizard(title, fragment);
 		wizard.setForcePreviousAndNextButtons(true);
 		WizardDialog dialog = new WizardDialog(shell, wizard);
 		int result = dialog.open();
 		if(Window.OK != result) return null;
 		return (IRuntime)wizard.getRootFragment().getTaskModel().getObject(TaskModel.TASK_RUNTIME);
 	}
 
 }
