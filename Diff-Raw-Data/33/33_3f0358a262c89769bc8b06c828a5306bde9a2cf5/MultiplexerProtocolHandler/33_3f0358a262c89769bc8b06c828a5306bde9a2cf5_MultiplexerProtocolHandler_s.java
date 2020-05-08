 package multiplexer.jmx;
 
 import multiplexer.Multiplexer.MultiplexerMessage;
 
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipelineCoverage;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 
import sun.jdbc.odbc.ee.ConnectionAttributes;

 @ChannelPipelineCoverage("all")
 class MultiplexerProtocolHandler extends SimpleChannelHandler {
 	
 	private ConnectionsManager connectionsManager;
 
 	public MultiplexerProtocolHandler(ConnectionsManager connectionsManager) {
 		this.connectionsManager = connectionsManager;
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
 		System.err.println("messageReceived");
 		assert e.getMessage() instanceof MultiplexerMessage : e.getMessage()
 				+ " is not a MultiplexerMessage";
 		System.err.println(e.getMessage()); //TODO
 		connectionsManager.messageReceived((MultiplexerMessage) e.getMessage(),ctx.getChannel());
 	}
 
 	@Override
 	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 
 		assert e.getMessage() instanceof MultiplexerMessage : "You should feed the channel with MultiplexerMessages, not "
 				+ e.getMessage();
 		super.writeRequested(ctx, e);
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
 		e.getCause().printStackTrace();
 		e.getChannel().close();
 	}
 }
