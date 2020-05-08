 package org.jboss.tools.ws.creation.ui.wsrt;
 
 import java.util.Vector;
 
 import org.eclipse.wst.command.internal.env.core.ICommandFactory;
 import org.eclipse.wst.command.internal.env.core.SimpleCommandFactory;
 import org.eclipse.wst.common.environment.IEnvironment;
 import org.eclipse.wst.ws.internal.wsrt.AbstractWebService;
 import org.eclipse.wst.ws.internal.wsrt.IContext;
 import org.eclipse.wst.ws.internal.wsrt.ISelection;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceInfo;
 import org.eclipse.wst.ws.internal.wsrt.WebServiceScenario;
 import org.jboss.tools.ws.creation.core.commands.BindingFilesValidationCommand;
 import org.jboss.tools.ws.creation.core.commands.InitialCommand;
import org.jboss.tools.ws.creation.core.commands.WSDL2JavaCommand;
 import org.jboss.tools.ws.creation.core.commands.WSProviderInvokeCommand;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 
 public class JBossWebService extends AbstractWebService {
 
 	public JBossWebService(WebServiceInfo info){
 		super(info);
 	}
 	
 	@Override
 	public ICommandFactory assemble(IEnvironment env, IContext ctx,
 			ISelection sel, String project, String earProject) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ICommandFactory deploy(IEnvironment env, IContext ctx,
 			ISelection sel, String project, String earProject) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@SuppressWarnings({ "restriction", "unchecked" })
 	@Override
 	public ICommandFactory develop(IEnvironment env, IContext ctx,
 			ISelection sel, String project, String earProject) {
 		
 		Vector commands = new Vector();
 		ServiceModel model = new ServiceModel();
 		model.setWebProjectName(project);
 		if (ctx.getScenario().getValue() == WebServiceScenario.TOPDOWN)	{ 
 			commands.add(new InitialCommand(model, this, WebServiceScenario.TOPDOWN));
 			commands.add(new BindingFilesValidationCommand(model));
			commands.add(new WSDL2JavaCommand(model));
 			//commands.add(new JbossWSRuntimeCommand(ResourcesPlugin.getWorkspace().getRoot().getProject(project)));
 		}
 		else if (ctx.getScenario().getValue() == WebServiceScenario.BOTTOMUP){
 			commands.add(new InitialCommand(model, this, WebServiceScenario.BOTTOMUP));
 			commands.add(new WSProviderInvokeCommand(model));
 			//commands.add(new JbossWSRuntimeCommand(ResourcesPlugin.getWorkspace().getRoot().getProject(project)));
 		}
 		
 		return new SimpleCommandFactory(commands);
 	}
 
 	@Override
 	public ICommandFactory install(IEnvironment env, IContext ctx,
 			ISelection sel, String project, String earProject) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ICommandFactory run(IEnvironment env, IContext ctx, ISelection sel,
 			String project, String earProject) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
