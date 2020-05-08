 package org.eclipse.ecf.client.jgroups;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.ecf.core.ContainerFactory;
 import org.eclipse.ecf.core.ContainerTypeDescription;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDCreateException;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.identity.Namespace;
 import org.eclipse.ecf.internal.provider.jgroups.JGroupsClientContainerInstantiator;
 import org.eclipse.ecf.provider.jgroups.identity.JGroupsNamespace;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 
 public class Application implements IApplication {
 
 	protected static String CONTAINER_CLIENT = "ecf.jgroups.client";
 	protected static String CONTAINER_MANAGER = "ecf.jgroups.manager";
 	protected static String CONTAINER_DESCRIPTION="Trivial JGroups client";
	protected static String CONTAINER_FACTORY="ecf.jgroups.client.containerFactory";
 	
 	private IApplicationContext context;
 
 	protected IContainer client;
 
 	protected ID managerID;
 
 	private IApplicationContext appContext = null;
 	private static String jgURL;
 
 	protected IContainer createClient() throws Exception {
		return ContainerFactory.getDefault().createContainer( 
 				new ContainerTypeDescription(CONTAINER_CLIENT,
 						JGroupsClientContainerInstantiator.class.getName(),
						CONTAINER_DESCRIPTION), getServerIdentity() );
 	}
 
     private ID getServerIdentity() throws IDCreateException, URISyntaxException {
     	return IDFactory.getDefault().createID("ecf.namespace.jgroupsid",jgURL);
     }
 
 	@Override
 	public Object start(IApplicationContext context) throws Exception {
 		final String[] args = mungeArguments((String[]) context.getArguments()
 				.get("application.args")); //$NON-NLS-1$
 		if (args.length < 1) {
 			usage();
 			return IApplication.EXIT_OK;
 		} else {
 			this.appContext = context;
 			jgURL = args[0];
 			System.out.println(jgURL);
 			ID managerID= getServerIdentity();
 			System.out.println(managerID.toExternalForm());
 			synchronized (this) {
 				client = createClient();
 				client.connect(managerID, null);
 				System.out.println("JGroups client started with manager id="
 						+ managerID);
 				this.wait();
 			}
 		}
 
 		return IApplication.EXIT_OK;
 	}
 
 	@Override
 	public void stop() {
 
 		synchronized (this) {
 			if (client != null) {
 				client.dispose();
 				client = null;
 				this.notifyAll();
 			}
 		}
 
 	}
 
 	private String[] mungeArguments(String originalArgs[]) {
 		if (originalArgs == null)
 			return new String[0];
 		final List<String> l = new ArrayList<String>();
 		for (int i = 0; i < originalArgs.length; i++)
 			if (!originalArgs[i].equals("-pdelaunch")) //$NON-NLS-1$
 				l.add(originalArgs[i]);
 		return l.toArray(new String[] {});
 	}
 
 	private void usage() {
 		System.out.println("Usage: eclipse.exe -application " //$NON-NLS-1$
 				+ this.getClass().getName()
 				+ " jgroups:///<jgroupsChannelName>"); //$NON-NLS-1$
 		System.out
 				.println("   Examples: eclipse -application org.eclipse.ecf.provider.jgroups.JGroupsManager jgroups:///jgroupsChannel"); //$NON-NLS-1$
 	}
 
 }
