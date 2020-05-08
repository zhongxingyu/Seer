 package amp.rabbit.topology;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * Represents the common behavior between Producing and Consuming Routes.
  * 
  * @author Richard Clayton (Berico Technologies)
  */
 public abstract class BaseRoute {
 
 	ArrayList<Broker> brokers = new ArrayList<Broker>();
 	
 	Exchange exchange;
 	
 	ArrayList<String> routingKeys = new ArrayList<String>();
 	
 	public BaseRoute(){}
 	
 	public BaseRoute(Collection<Broker> brokers, Exchange exchange,
 			Collection<String> routingKeys) {
 		
 		this.brokers.addAll(brokers);
 		this.exchange = exchange;
 		this.routingKeys.addAll(routingKeys);
 	}
 	
 	public void setBrokers(Collection<Broker> brokers){
		
 		this.brokers.addAll(brokers);
 	}
 	
 	public Collection<Broker> getBrokers(){
 		
 		return this.brokers;
 	}
 	
 	public void setExchange(Exchange exchange){
 		
 		this.exchange = exchange;
 	}
 	
 	public Exchange getExchange(){
 		
 		return this.exchange;
 	}
 	
 	public void setRoutingKeys(Collection<String> routingKeys){
 		
 		this.routingKeys.addAll(routingKeys);
 	}
 	
 	public Collection<String> getRoutingKeys(){
 		
 		return this.routingKeys;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((brokers == null) ? 0 : brokers.hashCode());
 		result = prime * result
 				+ ((exchange == null) ? 0 : exchange.hashCode());
 		result = prime * result
 				+ ((routingKeys == null) ? 0 : routingKeys.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		BaseRoute other = (BaseRoute) obj;
 		if (brokers == null) {
 			if (other.brokers != null)
 				return false;
 		} else if (!brokers.equals(other.brokers))
 			return false;
 		if (exchange == null) {
 			if (other.exchange != null)
 				return false;
 		} else if (!exchange.equals(other.exchange))
 			return false;
 		if (routingKeys == null) {
 			if (other.routingKeys != null)
 				return false;
 		} else if (!routingKeys.equals(other.routingKeys))
 			return false;
 		return true;
 	}
 }
