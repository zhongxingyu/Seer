 /*******************************************************************************
  * Copyright (c) 2004 Eteration Bilisim A.S.
  * All rights reserved.  This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Gorkem Ercan - initial API and implementation
  *     Naci M. Dai
  * 
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Eteration Bilisim A.S.  For more
  * information on eteration, please see
  * <http://www.eteration.com/>.
  ***************************************************************************/
 package org.eclipse.jst.server.generic.internal.ui;
 
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jst.server.generic.internal.core.GenericServerRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.jst.server.generic.ui.GenericServerUIMessages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.wst.server.core.IElement;
 import org.eclipse.wst.server.core.IResourceManager;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.ITaskModel;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 
 /**
  * A serverDefinitionType aware wizard for defining runtimes.
  * 
  * @author Gorkem Ercan
  */
 public class GenericServerRuntimeWizardFragment extends ServerDefinitionTypeAwareWizardFragment {
 	
 
 	private Group selectionBar;
 	private Combo fServerCombo;
 	private ServerTypeDefinitionGroup fServerPanel;
 	private IRuntimeWorkingCopy fRuntimeWC;
     private Map fServerRuntimeProperties;
     private String fSelectedServerType;
     
 	
 	/**
 	 * Constructor
 	 */
 	public GenericServerRuntimeWizardFragment() {
 		super();
 	}
 	/* (non-Javadoc)
 	 * @see com.ibm.wtp.server.ui.wizard.IWizardFragment#isComplete()
 	 */
 	public boolean isComplete() {
 	  	IRuntimeWorkingCopy runtime = getRuntimeWorkingCopy();
 		if (runtime == null)
 			return false;
 		IStatus status = runtime.validate();
 		return (status != null && status.isOK());
 	}
 	
 	public void createContent(Composite parent, IWizardHandle handle) {
 		fRuntimeWC=null;
 		createSelectionBar(parent);
 		createServerDefinitionTypeComposite(parent);
 	}
 
 	
 	private void swapBody() 
 	{
         String selected = fServerCombo == null ? null : fServerCombo
                 .getItem(fServerCombo.getSelectionIndex());
        if (getServerDefinitionId() != null)
            selected = getServerDefinitionId();
         Map properties = null;
        if (getRuntimeWorkingCopy() != null)
            properties = getRuntimeWorkingCopy()
                    .getAttribute(
                            GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,
                            (Map) null);
        ServerRuntime definition = getServerTypeDefinition(selected,
                properties);

        fServerPanel.reset(definition,
                ServerTypeDefinitionGroup.CONTEXT_RUNTIME, properties);
     }
 	/**
      * @param selected
      */
     private void createServerDefinitionTypeComposite(Composite parent) {     
         String selected = fServerCombo==null?null:fServerCombo.getItem(fServerCombo.getSelectionIndex());
         if(getServerDefinitionId()!=null)
 		    selected=getServerDefinitionId();
         Map properties= null;
         if(getRuntimeWorkingCopy()!=null)
             properties = getRuntimeWorkingCopy().getAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,(Map)null);
         ServerRuntime definition = getServerTypeDefinition(selected,properties);
         fServerPanel = new ServerTypeDefinitionGroup(this,definition,ServerTypeDefinitionGroup.CONTEXT_RUNTIME,properties,parent);
     }
     private void createSelectionBar(Composite content) {
 		selectionBar = new Group(content, SWT.SHADOW_ETCHED_IN);
 		selectionBar.setLayout(new GridLayout(2,false));
 		selectionBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		Label label = new Label(selectionBar, SWT.NONE);
 		label.setText(GenericServerUIMessages.getString("runtimeWizard.label.serverType"));
 		label.setLayoutData(new GridData());
 		fServerCombo = new Combo(selectionBar, SWT.BORDER |SWT.READ_ONLY);
 		ServerRuntime[] servers = getAllServerDefinitionTypes();
 		for(int i=0; i<servers.length; i++){
 			fServerCombo.add(servers[i].getName());
 		}
 		fServerCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
 		if(fServerCombo.getItemCount()>0)
			fServerCombo.select(0);
		
 		fServerCombo.addSelectionListener(
 				new SelectionListener() {
 					public void widgetSelected(SelectionEvent e) {
 						swapBody();
 						serverDefinitionTypePropertiesChanged();
 					}
 					public void widgetDefaultSelected(SelectionEvent e) {}
 				}
 			);
 	}
 
 	
 	private String getServerDefinitionId()
 	{
 		String currentDefinition= null;
 		if(getRuntimeWorkingCopy()!=null)
 			currentDefinition =  getRuntimeWorkingCopy().getAttribute(GenericServerRuntime.SERVER_DEFINITION_ID,"");
 		if(currentDefinition!= null && currentDefinition.length()>0)
 		{	
 			return currentDefinition;
 		}
 		return null;
 	}
 	
 	private void selectServerDefinition()
 	{
 		String currentDefinition = getServerDefinitionId();
 		int selectIndex = 0;
 		if(currentDefinition != null)
 		{	
 			selectIndex =  fServerCombo.indexOf(currentDefinition);
 			if(selectIndex<0)
 				selectIndex =0;
 		}
 		fServerCombo.select(selectIndex);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.ui.wizard.IWizardFragment#enter()
 	 */
 	public void enter() {
 		String serverDefinition = getServerDefinitionId();
 		if(serverDefinition!=null && serverDefinition.length()>0)
 		{
 			selectServerDefinition();
			swapBody();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.ui.wizard.IWizardFragment#exit()
 	 */
 	public void exit() {
 
         fRuntimeWC=null;
 	}
 	protected String getSelectedServerType(){
 	   return  fSelectedServerType;
 	}
 	protected Map getServerRuntimeProperties(){
 	    return fServerRuntimeProperties; 
 	}
 
 	private String createName()
 	{
 	    String selected = getSelectedServerType();
 	    IRuntimeWorkingCopy wc = getRuntimeWorkingCopy();
 	    IRuntimeType runtimeType = wc.getRuntimeType();
 	    String name = GenericServerUIMessages.getFormattedString("runtimeName", new String[] {selected,runtimeType.getName()});
 	    
 		IResourceManager rm = ServerCore.getResourceManager();
 		List list = rm.getRuntimes(runtimeType);
 		Iterator iterator = list.iterator();
 		int suffix = 1;
 		String suffixName=name;
 		while(iterator.hasNext())
 	    {
 		   
 	        IElement el = (IElement)iterator.next();
 	        if(el.getName().equals(name)|| el.getName().equals(suffixName))
 	            suffix++;
 	        suffixName= name+" "+suffix;
 	    }
 	    
 		if(suffix>1)
 		    return suffixName;
 	    return name;
 	}
 	
 	private IRuntimeWorkingCopy getRuntimeWorkingCopy()
 	{
 		if(fRuntimeWC == null)
 			fRuntimeWC = (IRuntimeWorkingCopy)getTaskModel().getObject(ITaskModel.TASK_RUNTIME); 
 		return fRuntimeWC;
 	}
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#description()
      */
     public String description() {
         return  GenericServerUIMessages.getString("runtimeWizardDescription");
     }
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#title()
      */
     public String title() {
         return GenericServerUIMessages.getString("runtimeWizardTitle");
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.ui.ServerDefinitionTypeAwareWizardFragment#serverDefinitionTypePropertiesChanged()
      */
     public void serverDefinitionTypePropertiesChanged() {
 		fSelectedServerType = fServerCombo.getItem(fServerCombo.getSelectionIndex());
         fServerRuntimeProperties = fServerPanel.getProperties();
    		String selected = getSelectedServerType();
 		Map properties = getServerRuntimeProperties();
 		IRuntimeWorkingCopy wc = getRuntimeWorkingCopy();       		
 		wc.setAttribute(GenericServerRuntime.SERVER_DEFINITION_ID, selected);
 		wc.setAttribute(GenericServerRuntime.SERVER_INSTANCE_PROPERTIES,properties);
 		wc.setName(createName());
 		validate();
    }
     
     private void validate()
     {
 		if (getRuntimeWorkingCopy() == null) {
 			this.getWizard().setMessage("", IMessageProvider.ERROR);
 			return;
 		}
 		IStatus status = getRuntimeWorkingCopy().validate();
 		if (status == null || status.isOK())
 			getWizard().setMessage(null, IMessageProvider.NONE);
 		else
 			getWizard().setMessage(status.getMessage(), IMessageProvider.ERROR);
 		getWizard().update();
     }
 }
