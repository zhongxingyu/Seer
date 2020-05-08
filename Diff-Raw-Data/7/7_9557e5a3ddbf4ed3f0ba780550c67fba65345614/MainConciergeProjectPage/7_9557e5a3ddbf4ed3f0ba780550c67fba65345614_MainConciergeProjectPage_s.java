 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.buglabs.osgi.concierge.ui.wizards.newproject;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.JavaConventions;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
 import com.buglabs.osgi.concierge.ui.Activator;
 import com.buglabs.osgi.concierge.ui.info.ProjectInfo;
 
 /**
  * 
  * @author Angel Roman - roman@mdesystems.com
  *
  */
 public class MainConciergeProjectPage extends WizardPage {
 	
 	public static final String PAGE_TITLE = "Concierge Project";
 	private ProjectInfo projInfo;
 	
 	public MainConciergeProjectPage(ProjectInfo pinfo) {
 		//TODO: Add image
 		super(PAGE_TITLE, PAGE_TITLE, Activator.getDefault().getImageDescriptor(Activator.IMAGE_CG_LOGO_WIZARD));
 		projInfo = pinfo;
 		setMessage("Please enter a project name.");
 	}
 	
 	public void createControl(Composite parent) {
 		GridData gdFillH = new GridData(GridData.FILL_HORIZONTAL);
 		
 		Composite top = new Composite(parent, SWT.NONE);
 		top.setLayoutData(new GridData(GridData.FILL_BOTH));
 		top.setLayout(new GridLayout());
 		
 		Composite projectNameComp = new Composite(top, SWT.NONE);
 		GridData gdProjName = new GridData(GridData.FILL_HORIZONTAL);
 		projectNameComp.setLayoutData(gdProjName);
 		projectNameComp.setLayout(new GridLayout(2, false));
 		Label lblProjectName = new Label(projectNameComp, SWT.NONE);
 		lblProjectName.setText("N&ame");
 		
 		Text txtProjectName = new Text(projectNameComp, SWT.BORDER);
 		txtProjectName.setLayoutData(gdFillH);
 		txtProjectName.setFocus();
 		
 		txtProjectName.addModifyListener(new ModifyListener(){
 
 			public void modifyText(ModifyEvent e) {
 				projInfo.setProjectName(((Text) e.widget).getText());
 				setPageComplete(true);
 			}});
 		
 		setControl(top);
 	}
 	
 	public boolean isPageComplete() {
 		
 		IWorkspaceRoot wsroot =ResourcesPlugin.getWorkspace().getRoot();
 		
 		IStatus validate = ResourcesPlugin.getWorkspace().validateName(projInfo.getProjectName(), IResource.PROJECT);
 		
 		if(!validate.isOK() || !isValidProjectName(projInfo.getProjectName())) {
 			setErrorMessage("Invalid project name: " + projInfo.getProjectName());
 			return false;
 		}
 		
		
		
 		IProject proj = wsroot.getProject(projInfo.getProjectName());
 		if(proj.exists()) {
 			setErrorMessage("A project with the name " + projInfo.getProjectName() + " already exists");
 			return false;
 		}
 		
 		setErrorMessage(null);
 		
 		return true;
 	}
 
 	private boolean isValidProjectName(String projectName) {
 		return JavaConventions.validatePackageName(ProjectUtils.formatNameToPackage(projectName)).isOK();
 	}
 
 	private boolean hasCharacter(String str, char c) {
 		return str.indexOf(c) > -1;
 	}
 }
