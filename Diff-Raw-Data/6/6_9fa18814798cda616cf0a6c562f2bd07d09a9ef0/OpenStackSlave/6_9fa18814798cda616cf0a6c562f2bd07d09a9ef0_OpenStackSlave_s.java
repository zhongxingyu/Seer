 package jenkins.plugins.openstack;
 
 import hudson.Extension;
 import hudson.model.TaskListener;
 import hudson.model.Descriptor.FormException;
 import hudson.os.windows.ManagedWindowsServiceLauncher;
 import hudson.plugins.sshslaves.SSHLauncher;
 import hudson.slaves.AbstractCloudComputer;
 import hudson.slaves.AbstractCloudSlave;
 import hudson.slaves.NodeProperty;
 import hudson.slaves.ComputerLauncher;
 import hudson.slaves.SlaveComputer;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.openstack.nova.NovaClient;
 import org.openstack.nova.api.ServersCore;
 import org.openstack.nova.model.Server;
 import org.openstack.nova.model.Server.Addresses;
 import org.openstack.nova.model.Server.Addresses.Address;
 
 public final class OpenStackSlave extends AbstractCloudSlave {
 
 	public final String cloudId;
 	public final String templateId;
 	public final String serverId;
 	public final boolean stopOnTerminate;
 	
 	private transient SlaveTemplate template;
     private transient OpenStackCloud parent;
     private transient Server server;
 
     @DataBoundConstructor
     public OpenStackSlave(String cloudId,
     					  String templateId,
     					  String serverId,
     					  boolean stopOnTerminate) throws IOException, FormException {
     	this(OpenStackCloud.get(cloudId).getTemplate(templateId),
     		 OpenStackCloud.get(cloudId),
     		 OpenStackCloud.get(cloudId).connect().execute(ServersCore.showServer(serverId)));
     }
 	
     public OpenStackSlave(SlaveTemplate template,
     					  OpenStackCloud parent,
     					  Server server) throws IOException, FormException {
     	this(template, parent, server, template.stopOnTerminate);
     }
 
 	public OpenStackSlave(SlaveTemplate template,
 						  OpenStackCloud parent,
 						  Server server,
 						  boolean stopOnTerminate) throws IOException, FormException {
 		super(template.id + " (" + server.getId() + ")",
 			  template.description,
 			  template.remoteFS,
 			  template.getNumExecutors(),
 			  template.stopOnTerminate ? Mode.EXCLUSIVE : Mode.NORMAL,
 			  template.labels,
 			  new OpenStackComputerLauncher(),
 			  new OpenStackRetentionStrategy(),
 			  Collections.<NodeProperty<?>>emptyList());
 		
 		this.cloudId = parent.id;
 		this.templateId = template.id;
 		this.serverId = server.getId();
 		this.stopOnTerminate = stopOnTerminate;
 		readResolve();
 	}
     
 	protected Object readResolve() {
     	super.readResolve();
 		this.setLauncher(new OpenStackComputerLauncher());
 		this.setRetentionStrategy(new OpenStackRetentionStrategy());
 		update();
     	return this;
 	}
 	
 	@Override
 	public ComputerLauncher getLauncher() {
 		return new OpenStackComputerLauncher();
 	}
 	
     @Override
     public AbstractCloudComputer<OpenStackSlave> createComputer() {
         return new OpenStackComputer(this);
     }
 
     protected void update() {
     	/* Reload the cloud pointer. */
     	parent = OpenStackCloud.get(cloudId);
     	
     	/* Reload the template if required. */
     	template = parent.getTemplate(templateId);
     	
     	/* Update the server. */
     	NovaClient client = parent.connect();
     	server = client.execute(ServersCore.showServer(serverId));
     }
 
     protected synchronized void waitForActive() throws InterruptedException {
     	do {
     		update();
     		wait(1000);
     	} while( server != null &&
     			 server.getStatus() != null &&
     			 !server.getStatus().equals("ACTIVE") &&
     			 !server.getStatus().equals("ERROR") );
     }
     
 	@Override
 	protected synchronized void _terminate(TaskListener listener)
 			throws IOException, InterruptedException {
 		update();
 		if( parent != null && server != null ) {
 			NovaClient client = parent.connect();
 			client.execute(ServersCore.deleteServer(server.getId()));
 			server = null;
 		}
 		notifyAll();
 	}
 
 	public boolean isUnix() {
 		update();
 		return template.isUnix;
 	}
 	
 	public synchronized void launch(SlaveComputer computer, TaskListener listener)
 			throws InterruptedException {
 		update();
 		
 		/*
 		 * TODO:
 		 * Support various SSH options (possibly bootstrapped directly
 		 * from the ssh-slaves plugin configuration). For now, we use
 		 * only key-based authentication from the standard spots -- and
 		 * we assume that Java is installed on the images when they come
 		 * up.
 		 */
 		Addresses addresses = server.getAddresses();
 		for( List<Address> network : addresses.getAddresses().values() ) {
 			for( Address addr : network ) {
 				ComputerLauncher launcher;
 				
 				if( computer.isUnix().booleanValue() ) {
 					launcher = new SSHLauncher(
 								addr.getAddr(), 22,
 								template.remoteUser,
 								template.remotePassword, /* password */
 								template.privateKey, /* private key */
 								null, null, null, /* JDK installation */
								template.remoteUser.equals("root") ? null : "sudo", /* prefix */
 								null); /* suffix */
 				} else {
 					launcher =
 						new ManagedWindowsServiceLauncher(
 								template.remoteUser,
 								template.remotePassword,
 								addr.getAddr());
 				}
 
 				try {
 					launcher.launch(computer, listener);
 				} catch (IOException e) {
 					continue;
 				}
 				if( computer.getChannel() != null )
 					return;
 			}
 		}
 	}
 
     @Extension
     public static final class DescriptorImpl extends SlaveDescriptor {
         @Override
 		public String getDisplayName() {
             return "OpenStack";
         }
     }
}
