 package de.netprojectev.server.networking;
 
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 
 import org.apache.logging.log4j.Logger;
 
 import de.netprojectev.networking.LoginData;
 import de.netprojectev.networking.Message;
 import de.netprojectev.networking.OpCode;
 import de.netprojectev.server.ConstantsServer;
 import de.netprojectev.server.model.PreferencesModelServer;
 import de.netprojectev.utils.LoggerBuilder;
 
 public class AuthHandlerServer extends ChannelInboundHandlerAdapter {
 
 	private static final Logger log = LoggerBuilder.createLogger(AuthHandlerServer.class);
 
 	private final MessageProxyServer proxy;
 
 	private boolean authSuccessful;
 	private boolean chanConnected;
 	
 	public AuthHandlerServer(MessageProxyServer proxy) {
 		super();
 		this.proxy = proxy;
 		authSuccessful = false;
 		chanConnected = false;
 	}
 	
 	@Override
 	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
 		Message received = (Message) msg;
 		if(chanConnected && received.getOpCode().equals(OpCode.CTS_LOGIN_REQUEST)) {
 			
 			LoginData login = (LoginData) received.getData()[0];
 			
 			if(login.getKey().equals(PreferencesModelServer.getPropertyByKey(ConstantsServer.PROP_PW))) {
 				
 				if(proxy.findUserByAlias(login.getAlias()) == null) {
 					proxy.clientConnected(ctx.channel(), login.getAlias());
					authSuccessful = true;
					ctx.writeAndFlush(new Message(OpCode.STC_CONNECTION_ACK));
 					ctx.pipeline().remove(this);
 					log.info("Client connected successfully. Alias: " + login.getAlias());
 				} else {
 					denyAccessToClient("Alias already in use.", ctx);
 				}
 				
 			} else {
 				denyAccessToClient("No valid login.", ctx);
 			}
 			
 		} else if(chanConnected && authSuccessful) {
 			super.channelRead(ctx, msg);
 		} else {
 			denyAccessToClient("Unknown error occured.", ctx);
 		}
 
 	}
 
 	@Override
 	public void channelActive(ChannelHandlerContext ctx) throws Exception {
 		chanConnected = true;
 	}
 
 	
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
 		log.warn("Exception caught in network stack.", cause.getCause());
 	}
 	
 
 	private void denyAccessToClient(String reason, ChannelHandlerContext ctx) throws InterruptedException {
 		log.warn("Login request denied: " + reason); 
 		ctx.writeAndFlush(new Message(OpCode.STC_LOGIN_DENIED, reason)).awaitUninterruptibly();
 		ctx.close().sync();
 	}
 }
