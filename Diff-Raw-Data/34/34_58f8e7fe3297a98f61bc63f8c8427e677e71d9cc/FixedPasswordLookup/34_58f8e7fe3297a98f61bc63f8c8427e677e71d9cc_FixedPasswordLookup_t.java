 package deus.core.transport.observers.impl;
 
 import deus.core.transport.id.TransportId;
import deus.core.transport.observers.TransportProtocolPasswordLookup;
 
public class FixedPasswordLookup implements TransportProtocolPasswordLookup {
 
 	private final String password;
 
 
 	public FixedPasswordLookup(String password) {
 
 		this.password = password;
 	}
 
 
 	@Override
 	public String getPassword(TransportId transportId) {
 		return password;
 	}
 
 }
