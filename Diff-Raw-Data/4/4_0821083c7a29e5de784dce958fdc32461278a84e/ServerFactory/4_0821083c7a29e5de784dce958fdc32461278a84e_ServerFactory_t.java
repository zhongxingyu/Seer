 package org.cluenet.clueservices.ircObjects;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class ServerFactory {
 	private static ServerFactory factory = new ServerFactory();
 	private static Map< String, Server > map = Collections.synchronizedMap( new HashMap< String, Server >() );
 	
 	public class Server extends IrcObject implements IrcSource {
 		private String name;
 		private Server parent;
 		private String description;
 		private Boolean isSynchronized;
 		private Server( String name, Server parent, String description ) {
 			this.name = name;
 			this.parent = parent;
 			this.description = description;
 			this.isSynchronized = false;
 		}
 		
 		public void setSynchronized() {
 			isSynchronized = true;
 		}
 
 		public Boolean isSynchronized() {
 			return isSynchronized;
 		}
 		
 		public String toString() {
 			return name;
 		}
 		
 		public Server getParent() {
 			return parent;
 		}
		
		public String getDescription() {
			return description;
		}
 	}
 	
 	private ServerFactory() {
 		
 	}
 
 	public static Server find( String name ) {
 		return map.get( name );
 	}
 
 	public static Server create( String name, Server parent, String description ) {
 		Server srv = factory.new Server( name, parent, description );
 		map.put( name, srv );
 		return srv;
 	}
 	
 }
