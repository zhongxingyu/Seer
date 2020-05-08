 package org.eclipse.dltk.core.internal.rse.ssh;
 
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.dltk.core.internal.rse.DLTKRSEPlugin;
 import org.eclipse.dltk.ssh.core.ISshConnection;
 import org.eclipse.dltk.ssh.core.SshConnectionManager;
 import org.eclipse.rse.core.IRSESystemType;
 import org.eclipse.rse.core.PasswordPersistenceManager;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.model.SystemSignonInformation;
 import org.eclipse.rse.core.subsystems.IConnectorService;
 
 import com.jcraft.jsch.Session;
 
 public class RSESshManager {
 
 	private static final String RSE_SSH_CONNECTOR_SERVICE_CLASSNAME = "org.eclipse.rse.internal.connectorservice.ssh.SshConnectorService"; //$NON-NLS-1$
 	private static final String SSH_CONNECTOR_SERVICE_GETSESSION_METHODNAME = "getSession"; //$NON-NLS-1$
 
 	/**
 	 * Create or return Ssh connection for specified remote host.
 	 * 
 	 * Right now support only stored ssh passwords retrieval.
 	 * 
 	 */
 	private static Set<IHost> hostsInInitialization = new HashSet<IHost>();
 
 	public static ISshConnection getConnection(final IHost host) {
 		synchronized (hostsInInitialization) {
 			if (hostsInInitialization.contains(host)) {
 				return null;
 			}
 		}
 		IConnectorService[] connectorServices = host.getConnectorServices();
 		IRSESystemType systemType = host.getSystemType();
 
 		// Only ssh connections are supported
 		if (!systemType.getId().equals(IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID)) {
 			return null;//
 		}
 		for (final IConnectorService connector : connectorServices) {
 			// String hostName = host.getHostName();
 			// Retrieve user name
 			final String userId = connector.getUserId();
 			String location = userId + "@" + host.getHostName();
 			final ISshConnection connection = SshConnectionManager
 					.getConnection(location);
 			if (connection.isDisabled()) {
 				return null;
 			}
 			if (connection.isConnected()) {
 				return connection;
 			}
 			// Make connect in separate thread
 			Thread connectionInitializationThread = new Thread() {
 				@Override
 				public void run() {
					initializeConnection(host, connector, userId, connection);
 				}
 			};
 			connectionInitializationThread.start();
 		}
 		return null;
 	}
 
 	private static void initializeConnection(IHost host,
 			IConnectorService connector, String userId,
 			ISshConnection connection) {
 		// Try to resolve not persisted password from SSh connector
 		synchronized (hostsInInitialization) {
 			if (hostsInInitialization.contains(host)) {
 				return;
 			}
 			hostsInInitialization.add(host);
 		}
 		try {
 			try {
 				if (!connector.isConnected()) {
 					connector.connect(new NullProgressMonitor());
 				}
 			} catch (Exception e) {
 				DLTKRSEPlugin.log(e);
 			}
 			if (!connector.isConnected()) {
 				// Set connection as disabled for ten minutes
 				connection.setDisabled(1000 * 60 * 10);
 				return;
 			}
 
 			final Class<? extends IConnectorService> connectorClass = connector
 					.getClass();
 			if (RSE_SSH_CONNECTOR_SERVICE_CLASSNAME.equals(connectorClass
 					.getName())) {
 				// Ssh is available
 				try {
 					Method method = connectorClass.getMethod(
 							SSH_CONNECTOR_SERVICE_GETSESSION_METHODNAME,
 							new Class[0]);
 					if (method != null) {
 						Object invoke = method.invoke(connector,
 								(Object[]) null);
 						if (invoke instanceof Session) {
 							Session session = (Session) invoke;
 							connection.setPassword(session.getUserInfo()
 									.getPassword());
 							connection.connect();
 							if (connection.isConnected()) {
 								return;
 							}
 						}
 					}
 				} catch (Exception e) {
 					DLTKRSEPlugin.log(e);
 				}
 			}
 		} finally {
 			synchronized (hostsInInitialization) {
 				hostsInInitialization.remove(host);
 			}
 		}
 
 		// Try to find password and specify it for Ssh Connection.
 		SystemSignonInformation information = PasswordPersistenceManager
 				.getInstance().find(host.getSystemType(), host.getHostName(),
 						userId);
 		if (information != null && information.getPassword() != null) {
 			connection.setPassword(information.getPassword());
 			connection.connect();
 			if (connection.isConnected()) {
 				return;
 			}
 		}
 		// Try to connect any way
 		connection.connect();
 		if (connection.isConnected()) {
 			return;
 		}
 		// Set connection to disabled state for 10 seconds
 		connection.setDisabled(10000);
 	}
 }
