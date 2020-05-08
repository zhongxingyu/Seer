 package org.genedb.crawl.elasticsearch;
 
 import javax.annotation.PostConstruct;
 
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.common.settings.ImmutableSettings;
 import org.elasticsearch.common.settings.Settings;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 
 public class TransportConnection extends BaseConnection implements Connection {
 	
 	TransportClient client;
 	private String host;
 	private int port = 9300;
 	private String clusterName;
 	
 	public void setHost(String host) {
 		this.host = host;
 	}
 	
 	public void setPort(int port) {
 		this.port = port;
 	}
 	
 	public void setClusterName(String clusterName) {
 		this.clusterName = clusterName;
 	}
 	
 	
 	@PostConstruct
 	public void configure () {
 		
 		Settings settings = ImmutableSettings.settingsBuilder()
        	.put("cluser.name",clusterName)
         	.build();
 		
 		client = new TransportClient(settings);
 		client.addTransportAddress(new InetSocketTransportAddress(host, port));
 		
 	}
 	
 	public Client getClient() {
 		return (Client) client;
 	}
 
 	public void close() {
 		if (client != null) {
 			client.close();
 		}
 	}
 	
 }
