 package net.praqma.ccanalyzer.clearcase;
 
 import java.io.IOException;
 import net.praqma.ccanalyzer.ClearCaseCounter;
 import net.praqma.ccanalyzer.ClearCaseFunction;
 import net.praqma.util.net.Net;
 
 public class PingDefaultGateway extends ClearCaseFunction{
 	
     @Override
     public String perform( ClearCaseCounter ccc ) throws IOException {
     	String host = Net.getDefaultGateway();
     	logger.debug( "Gateway: " + host );
     	double l = Net.ping( host, 20000 );
     	logger.debug( "Ping: " + l );
         
         return Double.toString( l );
     }
 
 }
