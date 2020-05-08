 package cz.muni.fi.xtovarn.heimdall.netty;
 
 import com.google.inject.Inject;
 import cz.muni.fi.xtovarn.heimdall.guice.Startable;
 import cz.muni.fi.xtovarn.heimdall.netty.group.SecureChannelGroup;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class NettyServer implements Startable {
 
 	private final SecureChannelGroup secureChannelGroup;
 	public final static int SERVER_PORT = 6000;
 
 	private ServerBootstrap bootstrap;
 
 	@Inject
 	public NettyServer(SecureChannelGroup secureChannelGroup) {
 		this.secureChannelGroup = secureChannelGroup;
 	}
 
 
 	@Override
 	public void start() {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(2));
 
 		bootstrap = new ServerBootstrap(factory);
 
 		bootstrap.setPipelineFactory(new ServerPipelineFactory(secureChannelGroup));
 
 		bootstrap.setOption("child.tcpNoDelay", true);
 		bootstrap.setOption("child.keepAlive", true);
 
 		bootstrap.bind(new InetSocketAddress(SERVER_PORT));
 	}
 
 	@Override
 	public void stop() {
 		System.out.println("Closing " + this.getClass() + "...");
 
 		try {
 			secureChannelGroup.close().await(5, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		bootstrap.releaseExternalResources();
 
 		System.out.println(this.getClass() + " closed!");
 	}
 }
