 package org.scriptbox.box.remoting.client;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import org.scriptbox.box.remoting.server.BoxInterface;
 import org.scriptbox.util.remoting.endpoint.Connection;
 import org.scriptbox.util.remoting.endpoint.Endpoint;
 import org.scriptbox.util.remoting.endpoint.SshTunnelEndpoint;
 import org.scriptbox.util.remoting.tunnel.Tunnel;
 import org.scriptbox.util.remoting.tunnel.TunnelCredentials;
 import org.springframework.remoting.caucho.HessianProxyFactoryBean;
 
 public class BoxTunnelConnectionFactory extends BoxConnectionFactory {
 	
	private static final Logger LOGGER = LoggerFactory.getLogger( BoxTunnelConnectionFactory.class );
	
 	protected Connection<BoxInterface> build( final Endpoint endpoint ) throws Exception {
 		if( endpoint instanceof SshTunnelEndpoint ) {
 			SshTunnelEndpoint ssh = (SshTunnelEndpoint)endpoint;
 			TunnelCredentials credentials = new TunnelCredentials();
 			credentials.setUser(ssh.getUser());
			credentials.setPassword(ssh.getPassword());
 				
 			final Tunnel tunnel = new Tunnel();
 	        tunnel.setCredentials( credentials );
 	        tunnel.setTunnelHost( ssh.getTunnelHost() );
 	        tunnel.setTunnelPort( ssh.getTunnelPort() );
 	        tunnel.setRemoteHost( ssh.getHost() );
 	        tunnel.setRemotePort( ssh.getPort() );
	       
 	        int localPort = tunnel.connect();
	        
	        if( LOGGER.isDebugEnabled() ) {
	        	LOGGER.debug( "create: established tunnel: localhost:" + localPort + "->" + 
	        		ssh.getTunnelHost() + ":" + ssh.getTunnelPort() + "->" + 
	        		ssh.getHost() + ":" + ssh.getPort() );
	        }
 			HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
 			factory.setServiceUrl(getFormat().replaceAll("%host%", "localhost").replaceAll("%port%",""+localPort));
 			factory.setServiceInterface(BoxInterface.class);
 			factory.afterPropertiesSet();
 			final BoxInterface box = (BoxInterface)factory.getObject();
 			return new Connection<BoxInterface>() {
 				public BoxInterface getProxy() {
 					return box;
 				}
 				public Endpoint getEndpoint() {
 					return endpoint;
 				}
 				public String getIdentifier() {
 					return endpoint.getIdentifier();
 				}
 				public void close() {
 					BoxTunnelConnectionFactory.this.close( this );
 					tunnel.disconnect();
 				}
 			};
 			
 		}
 		return null;
 	}
 }
