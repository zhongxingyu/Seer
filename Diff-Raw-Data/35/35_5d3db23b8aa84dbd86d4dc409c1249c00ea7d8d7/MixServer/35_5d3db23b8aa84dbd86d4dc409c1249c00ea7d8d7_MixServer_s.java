 package com.dustyneuron.bitprivacy.exchanger;
 
 import org.apache.commons.codec.binary.Hex;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.dustyneuron.bitprivacy.exchanger.messages.SignTransaction;
 import com.dustyneuron.bitprivacy.exchanger.messages.TradeRequest;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.AddressFormatException;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.Utils;
 import com.google.bitcoin.core.WrongNetworkException;
 
 import asg.cliche.Command;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 
 public class MixServer {
 	
 	public static final int portNumber = 9000;
 	
 	private ServerBootstrap bootstrap;
 	private ChannelFactory factory;
 	private Channel channel;
 	final private List<ServerHandler> serverHandlers;
 	
 	final ChannelGroup allChannels;
 	
 	final private WalletMgr walletMgr;
 	final private ObjectMapper mapper;
 	final private Map<BigInteger, List<ServerTradeRequest>> requestMap;
 	final private Map<Sha256Hash, ServerTrade> tradeMap;
 
 	
 	public MixServer(WalletMgr w) {
 		walletMgr = w;
 		mapper = new ObjectMapper();
 		requestMap = new HashMap<BigInteger, List<ServerTradeRequest>>();
 		tradeMap = new HashMap<Sha256Hash, ServerTrade>();
 		allChannels = new DefaultChannelGroup("mix-server");
 		serverHandlers = new ArrayList<ServerHandler>();
 	}
 	
 	@Command
 	public synchronized void start() {
 		factory =
 			new NioServerSocketChannelFactory(
 				Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
  
         bootstrap = new ServerBootstrap(factory);
  
         final MixServer mixServer = this;
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
              public ChannelPipeline getPipeline() {
             	 System.out.println("Starting new handler");
             	 ServerHandler serverHandler = new ServerHandler(mixServer);
             	 serverHandlers.add(serverHandler);
             	 return Channels.pipeline(serverHandler);
              }
         });
  
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         channel = bootstrap.bind(new InetSocketAddress(portNumber));
         allChannels.add(channel);
         
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override public void run() {
             	mixServer.stop();
             }
         });
 	}
 	
 	void send(String msg, Channel c) {
		System.out.println("Sending " + msg);
 		System.out.flush();
 		ChannelBuffer msgData = ChannelBuffers.buffer(1024);
 		msgData.writeBytes(msg.getBytes());
 		
 		c.write(msgData);
 	}
 	
 	void sendJson (Object o, Channel c) throws JsonGenerationException, JsonMappingException, IOException {
 		StringWriter writer = new StringWriter();
 		mapper.writeValue(writer, o);
 		send(writer.toString(), c);
 	}
 
 	
 	void makeTrade(BigInteger btc) throws Exception {
 		System.out.println("Making trade...");
 		System.out.flush();
 		
 		List<ServerTradeRequest> list = requestMap.get(btc);
 		if (list.size() < 2) {
 			throw new Exception("not enough requests for trade");
 		}
 		requestMap.remove(btc);
 		
 		Transaction tx = new Transaction(walletMgr.params);
 		for (ServerTradeRequest r : list) {
 			tx.addInput(r.req.source.getTransactionOutput(walletMgr));
 			tx.addOutput(r.req.btc, new Address(walletMgr.params, r.req.output));
 		}
 		
 		Sha256Hash key = tx.getHash();
 		String unsignedHexData = new String(Hex.encodeHex(tx.bitcoinSerialize()));
 		ServerTrade trade = new ServerTrade(tx, list);
 		tradeMap.put(key, trade);
 		
 		System.out.println("Trade key is " + key);
 		
 		ServerTradeRequest next = trade.getNextToSign();
 		Address a = new Address(walletMgr.params, next.req.output);
 		SignTransaction sign = new SignTransaction(key, a, unsignedHexData);
		System.out.println("sendJson obj " + sign);
		System.out.println("sendJson channel " + next.contact);
		System.out.println("would call sendJson with SignTransaction(" + key + ", " + a + ", " + unsignedHexData + ")");
		//sendJson(sign, next.contact);
 	}
 		
 	@Command
 	synchronized void testJsonSignTransaction(String key, String a, String unsignedHexData) throws WrongNetworkException, AddressFormatException, JsonGenerationException, JsonMappingException, IOException {
 		
 		SignTransaction sign = new SignTransaction(new Sha256Hash(key), new Address(walletMgr.params, a), unsignedHexData);
 		StringWriter writer = new StringWriter();
 		mapper.writeValue(writer, sign);
 		System.out.println(writer.toString());
 	}
 	
 
 	synchronized void handleMessage(String data, Channel channel) {
 		 try {
 			@SuppressWarnings("unchecked")
 			Map<String, Object> jsonData = mapper.readValue(data, Map.class);
 			//for (String key : jsonData.keySet()) {
 			//	System.out.println("json: " + key + ": " + jsonData.get(key).toString());
 			//}
 			
 			switch (jsonData.get("type").toString()) {
 				case "trade":
 					System.out.println("Received trade request");
 					System.out.flush();
 					TradeRequest req = mapper.readValue(data, TradeRequest.class);
 					System.out.println("Verifying...");
 					System.out.flush();
 					req.verify(walletMgr);
 					System.out.println("Adding valid trade request to list");
 					System.out.flush();
 					
 					ServerTradeRequest serverReq = new ServerTradeRequest(req, channel); 
 					
 					if (requestMap.containsKey(req.btc)) {
 						List<ServerTradeRequest> list = requestMap.get(req.btc);
 						list.add(serverReq);
 						if (list.size() > 1) {
 							makeTrade(req.btc);
 						}
 						
 					} else {
 						List<ServerTradeRequest> list = new ArrayList<ServerTradeRequest>();
 						list.add(serverReq);
 						requestMap.put(req.btc, list);
 					}
 					break;
 					
 				case "sign":
 					System.out.println("Received SignTransaction");
 					System.out.flush();
 					SignTransaction sign = mapper.readValue(data, SignTransaction.class);
 					ServerTrade trade = tradeMap.get(sign.tradeId);
 					trade.addSignature(walletMgr, sign);
 					ServerTradeRequest next = trade.getNextToSign();
 					if (next == null) {
 						// broadcast tx!
 						System.out.println("Ready to broadcast tx:");
 						System.out.println(trade.inProgress.toString(null));
 						tradeMap.remove(trade);
 					}
 					else {
						SignTransaction nextSign = new SignTransaction(sign.tradeId, new Address(walletMgr.params, next.req.output), sign.hexData);
 						sendJson(nextSign, next.contact);
 					}
 					break;
 					
 				default:
 					throw new Exception("unknown data type");
 			}
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
 	}
 	
 	@Command
 	public synchronized void requests() {
 		for (BigInteger btc : requestMap.keySet()) {
 			System.out.println(Utils.bitcoinValueToFriendlyString(btc) + ":");
 			for (ServerTradeRequest r : requestMap.get(btc)) {
 				System.out.println("  " + r.contact.toString());
 			}
 			System.out.println();
 		}
 	}
 	
 	@Command
 	public synchronized void trades() {
 		for (Sha256Hash hash : tradeMap.keySet()) {
 			System.out.println(hash.toString() + ":");
 			for (ServerTradeRequest r : tradeMap.get(hash).haveSigned.keySet()) {
 				System.out.println("  haveSigned: " + r.contact.toString() + " " + tradeMap.get(hash).haveSigned.get(r));
 			}
 			System.out.println();
 		}
 	}
 	
 	@Command
 	public synchronized void stop() {
 		if (allChannels.size() > 0) {
 			System.out.println("Starting shutdown...");
 			for (ServerHandler s : serverHandlers) {
 				System.out.println("ServerHandler " + s.toString());
 			}
 			System.out.println("Closing channels...");
 			allChannels.close().awaitUninterruptibly();
 			allChannels.clear();
 			System.out.println("Releasing external resources...");
 			factory.releaseExternalResources();
 			System.out.println("calling bootstrap.shutdown()...");
 			bootstrap.shutdown();
 		}
 	}
 }
