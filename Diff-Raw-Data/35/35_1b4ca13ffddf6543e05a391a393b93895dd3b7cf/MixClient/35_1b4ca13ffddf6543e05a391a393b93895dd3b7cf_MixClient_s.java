 package com.dustyneuron.bitprivacy.exchanger;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 
 
 import org.apache.commons.codec.binary.Hex;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.dustyneuron.bitprivacy.exchanger.messages.SignTransaction;
 import com.dustyneuron.bitprivacy.exchanger.messages.TradeRequest;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 
 import asg.cliche.Command;
 
 public class MixClient {
 	ChannelFactory factory;
 	ClientBootstrap bootstrap;
 	ChannelFuture future;
 	ClientHandler client;
 	
 	WalletMgr walletMgr;
 	
 	List<TradeRequest> requests;
 	
 	public MixClient(WalletMgr w) {
 		walletMgr = w;
 		requests = new ArrayList<TradeRequest>();
 	}
 	
 	
 	@Command
 	public void connect() {
 		factory =
 			new NioClientSocketChannelFactory(
 				Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
  
         bootstrap = new ClientBootstrap(factory);
  
         final MixClient mx = this;
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
              public ChannelPipeline getPipeline() {
             	 client = new ClientHandler(mx);
             	 return Channels.pipeline(client);
              }
         });
  
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         future = bootstrap.connect(new InetSocketAddress("localhost", MixServer.portNumber));
         
         future.awaitUninterruptibly();
         if (!future.isSuccess()) {
         	future.getCause().printStackTrace();
         }
         
         final MixClient mixClient = this;
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override public void run() {
             	mixClient.disconnect();
             }
         });
 	} 
 	
 	void receiveSignTransaction(SignTransaction sign) throws Exception {		
 		System.out.println("Request to sign transaction:");
 		Transaction tx = new Transaction(walletMgr.params, Hex.decodeHex(sign.hexData.toCharArray()));
 		System.out.println(walletMgr.transactionToString(tx));
 		System.out.println("Auto-signing:");
 		tx = walletMgr.newSignedInputsIfPossible(tx);
 		System.out.println(walletMgr.transactionToString(tx));
 		System.out.println("Sending back as json...");
 		sendJson(new SignTransaction(sign.tradeId, sign.signOutput, new String(Hex.encodeHex(tx.bitcoinSerialize()))));
 	}
 	
 	@Command
 	public void send(String message) throws IOException {
 		System.out.println("Sending '" + message + "'");
 		if (client == null) {
 			throw new IOException("Not connected to a server");
 		}
 		
 		client.send(message);
 	}
 	
 	void sendJson (Object o) throws JsonGenerationException, JsonMappingException, IOException {
 		StringWriter writer = new StringWriter();
 		
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.writeValue(writer, o);
 				
 		send(writer.toString());
 	}
 	
 	// Addresses do not serialize to JSON
 	@Command
 	public void testJsonAddress(String a) throws Exception {
 		Address address = new Address(walletMgr.params, a);
 		sendJson(address);
 	}
 		
 	// Sha256Hash do not deserialize from JSON
 	@Command
 	public void testJsonHash(String h) throws Exception {
 		Sha256Hash hash = new Sha256Hash(h);
 		sendJson(hash);
 	}
 
 	@Command
 	public void testJsonBigInteger(String n) throws Exception {
 		BigInteger i = new BigInteger(n);
 		sendJson(i);
 	}
 
 	
 	@Command
 	public void mix(String inputAddress, String outputAddress) throws Exception {
 		
 		Address input = new Address(walletMgr.params, inputAddress);
 		Transaction t = walletMgr.getLargestSpendableOutput(input);
     	if (t == null) {
     		throw new Exception("couldn't find transaction");
     	}
 		int unspentOutputIdx = walletMgr.getLargestSpendableOutput(t, input);
     	if (unspentOutputIdx < 0) {
     		throw new Exception("couldn't find spendable output");
     	}
 		TradeRequest req = new TradeRequest(t, unspentOutputIdx, new Address(walletMgr.params, outputAddress));
 		requests.add(req);
 		sendJson(req);
 	}
 	
 	@Command
 	public void sendTradeRequest(String transactionId, int unspentOutputIdx, String outputAddress) throws Exception {
 		
 		Transaction t = walletMgr.getTransaction(new Sha256Hash(transactionId));
     	if (t == null) {
     		throw new Exception("couldn't find transaction");
     	}
 
 		TradeRequest req = new TradeRequest(t, unspentOutputIdx, new Address(walletMgr.params, outputAddress));
 		requests.add(req);
 		sendJson(req);
 	}
 	
 	@Command
 	public void sendSigned(String tradeKey, String outputAddress, String hexData) throws JsonGenerationException, JsonMappingException, IOException {
 		sendJson(new SignTransaction(tradeKey, outputAddress, hexData));
 	}
 
 	
 	@Command
 	public void disconnect() {
 		if (future != null) {
 			future.getChannel().close().awaitUninterruptibly();
 			future = null;
 			factory.releaseExternalResources();
 			bootstrap.shutdown();
 		}
 	}
 }
