 package org.eclipse.jst.server.generic.ui.internal;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jst.server.generic.core.internal.GenericServerRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 
 public class ServerTypeDefinitionRuntimeDecorator extends ServerTypeDefinitionDecorator {
 
 	private GenericServerRuntime fRuntime;
 	public ServerTypeDefinitionRuntimeDecorator(ServerRuntime definition, Map initialProperties, IWizardHandle wizard, GenericServerRuntime runtime) {
 		super(definition, initialProperties,CONTEXT_RUNTIME,wizard);
 		fRuntime=runtime;
 	}
 
 	public boolean validate(){
 
 		if(fRuntime==null)
 			return false;
 		fRuntime.setServerDefinitionId(fRuntime.getRuntime().getRuntimeType().getId());
         fRuntime.setServerInstanceProperties(getValues());
       
 		IStatus status = fRuntime.validate();
 		
 		if (status == null || status.isOK()){

 			fWizard.setMessage(null, IMessageProvider.NONE);
 			fWizard.update();
	        String wDir = fRuntime.getServerTypeDefinition().getResolver().resolveProperties(fRuntime.getServerTypeDefinition().getStart().getWorkingDirectory()); 
	        fRuntime.getRuntimeWorkingCopy().setLocation(new Path(wDir));

 			return false;
 		}else
 		{
 			fWizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
 			fWizard.update();
 			return true;
 		} 
 	}
 }
