 package openstack_examples;
 
 import java.util.Calendar;
 
 import org.openstack.keystone.KeystoneClient;
 import org.openstack.keystone.api.Authenticate;
 import org.openstack.keystone.api.ListTenants;
 import org.openstack.keystone.model.Access;
 import org.openstack.keystone.model.Authentication;
 import org.openstack.keystone.model.Authentication.PasswordCredentials;
 import org.openstack.keystone.model.Authentication.Token;
 import org.openstack.keystone.model.Tenants;
 import org.openstack.keystone.utils.KeystoneUtils;
 import org.openstack.nova.NovaClient;
 import org.openstack.nova.api.FlavorsCore;
 import org.openstack.nova.api.ImagesCore;
 import org.openstack.nova.api.ServersCore;
 import org.openstack.nova.model.Flavor;
 import org.openstack.nova.model.Flavors;
 import org.openstack.nova.model.Image;
 import org.openstack.nova.model.Images;
 import org.openstack.nova.model.Server;
 import org.openstack.nova.model.ServerForCreate;
 import org.openstack.nova.model.Servers;
 
 public class LaunchMonitor {
 
 	private static final String KEYSTONE_AUTH_URL = "http://openstack.infosys.tuwien.ac.at:5000/v2.0";
 
	private static final String KEYSTONE_USERNAME = "login";
 
	private static final String KEYSTONE_PASSWORD = "pass";
 
 	private static final String KEY_NAME = "aic12";
 
 	private static final String SECURITY_GROUP_NAME = "default";
 
 	private Access serverAccess = null;
 
 	public Server createServer(String serverName, String flavorRef,
 			String imgRef) {
 		// define server instance
 		ServerForCreate serverForCreate = new ServerForCreate();
 		serverForCreate.setName(serverName);
 		serverForCreate.setFlavorRef(flavorRef);
 		serverForCreate.setImageRef(imgRef);
 		serverForCreate.setKeyName(KEY_NAME);
 		serverForCreate.getSecurityGroups().add(
 				new ServerForCreate.SecurityGroup(SECURITY_GROUP_NAME));
 
 		// create server
 		Server server = this.getNovaClient().execute(
 				ServersCore.createServer(serverForCreate));
 
 		System.out.println(server);
 
 		return server;
 	}
 
 	public void terminateServer(String id) {
 		this.getNovaClient().execute(ServersCore.deleteServer(id));
 	}
 
 	public Flavors getFlavors() {
 		return this.getNovaClient().execute(FlavorsCore.listFlavors());
 	}
 
 	public Servers getServers() {
 		return this.getNovaClient().execute(ServersCore.listServers(true));
 	}
 
 	public Images getImages() {
 		return this.getNovaClient().execute(ImagesCore.listImages());
 	}
 
 	/**
 	 * @param id
 	 *            Server id
 	 * @return Returns Server with @id, if it exists. Otherwise null.
 	 */
 	public Server getServer(String id) {
 		for (Server server : this.getServers()) {
 			if (server.getId().equals(id)) {
 				System.out.println(server);
 				return server;
 			}
 		}
 
 		return null;
 	}
 
 	private Boolean isTokenExpired() {
 		return Calendar.getInstance().getTime()
 				.compareTo(serverAccess.getToken().getExpires().getTime()) == 1;
 	}
 
 	private NovaClient getNovaClient() {
 		if (serverAccess == null || isTokenExpired()) {
 			KeystoneClient keystone = new KeystoneClient(KEYSTONE_AUTH_URL);
 			Authentication authentication = new Authentication();
 			PasswordCredentials passwordCredentials = new PasswordCredentials();
 			passwordCredentials.setUsername(KEYSTONE_USERNAME);
 			passwordCredentials.setPassword(KEYSTONE_PASSWORD);
 			authentication.setPasswordCredentials(passwordCredentials);
 
 			// access with unscoped token
 			serverAccess = keystone.execute(new Authenticate(authentication));
 
 			// use the token in the following requests
 			keystone.setToken(serverAccess.getToken().getId());
 
 			Tenants tenants = keystone.execute(new ListTenants());
 
 			// try to exchange token using the first tenant
 			if (tenants.getList().size() > 0) {
 
 				authentication = new Authentication();
 				Token token = new Token();
 				token.setId(serverAccess.getToken().getId());
 				authentication.setToken(token);
 				authentication.setTenantId(tenants.getList().get(0).getId());
 
 				serverAccess = keystone
 						.execute(new Authenticate(authentication));
 
 			} else {
 				System.out.println("No tenants found!");
 				return null;
 			}
 		}
 
 		return new NovaClient(KeystoneUtils.findEndpointURL(
 				serverAccess.getServiceCatalog(), "compute", null, "public"),
 				serverAccess.getToken().getId());
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		LaunchMonitor monitor = new LaunchMonitor();
 
 		String flavorRef = null;
 		String imgRef = null;
 
 		// print all flavors
 		for (Flavor flavor : monitor.getFlavors()) {
 			if (flavor.getName().equals("m1.tiny"))
 				flavorRef = flavor.getLinks().get(0).getHref();
 			System.out.println(flavor);
 		}
 
 		// print all images
 		for (Image image : monitor.getImages()) {
 			if (image.getName().equals("mongo-fresh")) {
 				imgRef = image.getLinks().get(0).getHref();
 			}
 			System.out.println(image);
 		}
 
 		// print all instances
 		for (Server server : monitor.getServers()) {
 			System.out.println(server);
 		}
 
 		if (!(imgRef == null && flavorRef == null)) {
 			// start instance
 			Server server = monitor.createServer("new-server-from-java",
 					flavorRef, imgRef);
 			// waiting for ACTIVE state
 			Boolean isActive = false;
 			while (!isActive) {
 				try {
 					Thread.sleep(10000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				isActive = monitor.getServer(server.getId()).getStatus()
 						.equals("ACTIVE");
 			}
 			// terminate instance
 			monitor.terminateServer(server.getId());
 		}
 
 		// stop started instance
 
 	}
 }
