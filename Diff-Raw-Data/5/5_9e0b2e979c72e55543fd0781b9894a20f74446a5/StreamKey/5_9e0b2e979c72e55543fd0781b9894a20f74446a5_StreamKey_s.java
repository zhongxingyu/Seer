 package stream;
 
 public class StreamKey
 {
     final boolean isTCP;
     final boolean isUDP;
     final int src_port;
     final int dst_port;
     final String src_ip;
     final String dst_ip;
 
     public StreamKey(boolean isTCP, int src_port, int dst_port,
 		     String src_ip, String dst_ip) {
 	this.isTCP = isTCP;
 	this.src_port = src_port;
 	this.dst_port = dst_port;
 	this.src_ip = src_ip;
 	this.dst_ip = dst_ip;
 	isUDP = !isTCP;
     }
 
     @Override
     public int hashCode() {
 	String s = src_ip + dst_ip + src_port + dst_port +
 	    (isTCP ? "TCP":"") + (isUDP ? "UDP":"");
 
 	return s.hashCode();
     }
 
     @Override
     public boolean equals(Object o) {
 	if (o instanceof StreamKey)
 	    return equals((StreamKey) o);
 	else
 	    return false;
     }
 
     public boolean equals(StreamKey k) {
 	if (src_port != k.src_port)
 	    return false;
 
 	if (dst_port != k.dst_port)
 	    return false;
 
	if (src_ip != k.src_ip)
 	    return false;
 
	if (dst_ip != k.dst_ip)
 	    return false;
 
 	if (isTCP != k.isTCP)
 	    return false;
 
 	if (isUDP != k.isUDP)
 	    return false;
 
 	return true;
     }	
 
 }
