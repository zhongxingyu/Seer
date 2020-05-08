 package amp.extensions.commons.builder;
 
 import java.util.Arrays;
 
 import amp.commanding.ICommandReceiver;
 import amp.rabbit.topology.Broker;
 import amp.utility.serialization.GsonSerializer;
 import amp.utility.serialization.ISerializer;
 import amp.topology.client.DefaultApplicationExchangeProvider;
 import amp.topology.client.FallbackRoutingInfoProvider;
 import amp.topology.client.GlobalTopologyService;
 import amp.topology.client.HttpRoutingInfoRetriever;
 import amp.utility.http.HttpClientProvider;
 import amp.utility.http.BasicAuthHttpClientProvider;
 import amp.utility.http.SslHttpClientProvider;
 
 /**
  * Build up the Global Topology Client.
  * 
  * @author Richard Clayton (Berico Technologies)
  */
 public class GlobalTopologyBuilder extends FluentExtension {
 
 	HttpClientProvider httpClientProvider;
	ISerializer serializer = new GsonSerializer();
 	DefaultApplicationExchangeProvider primaryFallbackProvider;
 	FallbackRoutingInfoProvider fallbackProvider;
     ICommandReceiver commandReceiver;
 	String host;
 	int port;
 	boolean useSSL = false;
 	String urlExpression;
 	// Ten minute eviction policy.
 	long cacheEntryExpiration = 1000 * 60 * 10;
 
     BusBuilder busBuilder;
 	TransportBuilder transportBuilder;
 	
 	/**
 	 * Initialize the builder with a reference to the parent fluent interface.
 	 * @param busBuilder Parent builder.
 	 * @param transportBuilder Builder in which we will set the topology service.
 	 */
 	public GlobalTopologyBuilder(BusBuilder busBuilder, TransportBuilder transportBuilder) {
 
         super(busBuilder);
 
         this.busBuilder = busBuilder;
 		this.transportBuilder = transportBuilder;
 		this.primaryFallbackProvider = new DefaultApplicationExchangeProvider();
 		this.primaryFallbackProvider.getExchangePrototype().setDurable(true);
 	}
 
 	/**
 	 * Use the HTTP (Basic Auth) GTS Implementation.
 	 * @param host Hostname of the GTS server.
 	 * @param port Port of the GTS server.
 	 * @param user Username.
 	 * @param password Password.
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder usingHttp(
 		String host, int port, String user, String password){
 		
 		this.httpClientProvider = new BasicAuthHttpClientProvider(host, user, password);
 		this.host = host;
 		this.port = port;
 		
 		return this;
 	}
 	
 	/**
 	 * Use the HTTPS (SSL/Mutual Auth) GTS implementation.
 	 * @param host Hostname of the GTS server.
 	 * @param keystore Local path to the key/trust store.
 	 * @param password Password of the key/trust store.
 	 * @param port Port to connect to the remote host.
 	 * @return
 	 */
 	public GlobalTopologyBuilder usingHttps(
 		String host, String keystore, String password, int port){
 		
 		this.httpClientProvider = new SslHttpClientProvider(keystore, password);
 		this.host = host;
 		this.port = port;
 		this.useSSL = true;
 		
 		return this;
 	}
 	
 	/**
 	 * Using an alternate serializer the JsonRoutingInfoSerializer;
 	 * Becareful when you do this.  The default serializer handles a special
 	 * serialization edge case that you may not be aware of.  Also, this serializer
 	 * may be different that the one used on the Event Bus (since it's decoding what's
 	 * coming back from GTS).
 	 * @param serializer Serializer to use for RoutingInfo coming back from GTS.
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder usingSerializer(ISerializer serializer){
 		
 		this.serializer = serializer;
 		
 		return this;
 	}
 	
 	/**
 	 * Overrides the default url expression construction using the url expression
 	 * supplied to this method.  This is an EXPRESSION!  That means you must supply the
 	 * placeholder (%s) that will be used to insert the Event Type.
 	 * @param urlExpression URL Expression with placeholder for Event Type.
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder atUrl(String urlExpression){
 		
 		this.urlExpression = urlExpression;
 		
 		return this;
 	}
 	
 	/**
 	 * Amount of time to keep an entry in the local cache.  When the
 	 * entry expires, a call will be made to the GTS on next request
 	 * (publish or registration).
 	 * @param milliseconds Time to live for each key.
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder refreshingEvery(long milliseconds){
 		
 		this.cacheEntryExpiration = milliseconds;
 		
 		return this;
 	}
 	
 	/**
 	 * Set the fallback provider, overriding the use of a Default App Exchange provider.
 	 * @param fallbackProvider Fallback Provider to use.
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder fallingBackOn(FallbackRoutingInfoProvider fallbackProvider){
 		
 		this.fallbackProvider = fallbackProvider;
 		
 		return this;
 	}
 	
 	/**
 	 * Fallback on the Default App Exchange at the supplied location.
 	 * @param hostname Hostname of the broker.
 	 * @param port Port of the broker.
 	 * @param vhost Virtual Host of the broker.
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder fallingBackOn(String hostname, int port, String vhost){
 		
 		//TODO: JM > CLUSTER_ID
 		//TODO: JM > SSL_ENABLED
 		Broker broker = new Broker("defaultCluster",hostname,port,false);	
 		this.primaryFallbackProvider.setBrokers(Arrays.asList(broker));
 		this.primaryFallbackProvider.getExchangePrototype().setVirtualHost(vhost);
 
 		
 		return this;
 	}
 	
 	/**
 	 * Fallback on the Default App Exchange at the supplied location.
 	 * @param hostname Hostname of the broker.
 	 * @param port Port of the broker.
 	 * @param vhost Virtual Host of the broker.
 	 * @param exchangeName Name of the Application Exchange.
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder fallingBackOn(String hostname, int port, String vhost, String exchangeName){
 		
 		this.primaryFallbackProvider.setExchangeName(exchangeName);
 		
 		return fallingBackOn(hostname, port, vhost);
 	}
 	
 	/**
 	 * Fallback on the Default App Exchange at the supplied location.
 	 * @param hostname Hostname of the broker.
 	 * @param port Port of the broker.
 	 * @param vhost Virtual Host of the broker.
 	 * @param exchangeName Name of the Application Exchange.
 	 * @param exchangeType Type of the Exchange (direct, topic, headers, fanout)
 	 * @return This.
 	 */
 	public GlobalTopologyBuilder fallingBackOn(String hostname, int port, String vhost, String exchangeName, String exchangeType){
 		
 		this.primaryFallbackProvider.setExchangeType(exchangeType);
 		
 		return fallingBackOn(hostname, port, vhost, exchangeName);
 	}
 	
 //	/**
 //	 * Fallback on the Default App Exchange at the supplied location.
 //	 * @param prototype Supply all sample exchange.
 //	 * @return This.
 //	 */
 //	public GlobalTopologyBuilder fallingBackOn(Exchange prototype){
 //		
 //		this.primaryFallbackProvider.setArguments(prototype.getArguments());
 //		this.primaryFallbackProvider.setAutoDelete(prototype.isAutoDelete());
 //		this.primaryFallbackProvider.setDurable(prototype.isDurable());
 //		
 //		return fallingBackOn(
 //				prototype.getHostName(), 
 //				prototype.getPort(), 
 //				prototype.getVirtualHost(), 
 //				prototype.getName(), 
 //				prototype.getExchangeType());
 //	}
 
 	/**
 	 * Build the Topology Service and return to the parent fluent.
 	 * @return Parent Fluent.
 	 */
 	public TransportBuilder andThen(){
 		
 		buildTopologyService();
 		
 		return this.transportBuilder;
 	}
 	
 	/**
 	 * And go back to the primary fluent interface.
 	 * @return original interface.
 	 */
 	@Override
 	public BusBuilder and() {
 
 		buildTopologyService();
 		
 		// Super!
 		return this.transportBuilder.and();
 	}
 	
 	/**
 	 * Build the topology service and set it on the transport fluent.
 	 */
 	void buildTopologyService() {
 		
 		// Fallback Provider.
 		FallbackRoutingInfoProvider fallback = 
 			(this.fallbackProvider != null)? 
 					this.fallbackProvider : this.primaryFallbackProvider;
 		
 		// Build up the URL Expression unless a custom URL was provided.
 		if (this.urlExpression == null) {
 			
 			StringBuilder url = new StringBuilder();
 			
 			if (this.useSSL){
 				url.append("https://");
 			}
 			else {
 				
 				url.append("http://");
 			}
 			
 			url.append(this.host).append(":").append(this.port);
 			
 			url.append("/service/topology/get-routing-info/%s");
 			
 			this.urlExpression = url.toString();
 		}
 		
 		// Build the Routing Info Retriever.
 		HttpRoutingInfoRetriever riRetriever = 
 			new HttpRoutingInfoRetriever(
 				this.httpClientProvider, this.urlExpression, this.serializer);
 		
 		// Initialize GTS.
 		GlobalTopologyService gts = 
 			new GlobalTopologyService(riRetriever, fallback);
 		
 		// Set it on the parent.
 		this.transportBuilder.setTopologyService(gts);
 	}
 }
