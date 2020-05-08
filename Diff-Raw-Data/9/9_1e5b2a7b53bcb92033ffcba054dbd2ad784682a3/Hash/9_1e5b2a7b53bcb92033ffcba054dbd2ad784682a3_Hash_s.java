 package org.beequeue.hash;
 
 import java.io.InputStream;
 import java.security.MessageDigest;
 
 import org.beequeue.util.Bytes;
 
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonValue;
 
 public class Hash implements Comparable<Hash>{
 	final public byte digest[];
 	
 	public Hash(byte[] digest) {
 		this.digest = digest;
 	}
 	public Hash(String s) {
 		this.digest = MessageDigestUtils.fromHexString(s);
 	}
 
 	@JsonCreator
 	public static Hash valueOf(String s) {
 		return new Hash(s);
 	}
 	
 	@Override @JsonValue
 	public String toString() {
 		return MessageDigestUtils.toHexString(digest);
 	}
 
 	@Override
 	public int hashCode() {
 		return digest[0]+digest[1]+digest[3];
 	}
 
 	@Override
 	public int compareTo(Hash that) {
 		if(that == null) return 1;
 		int rc = this.digest.length - that.digest.length ;
		if( rc != 0){
 			for (int i = 0; i < this.digest.length; i++) {
 				if( (rc = this.digest[i] - that.digest[i]) != 0 ) {
 					return rc;
 				}
 			}
 		}
 		return rc;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Hash) {
 			return compareTo((Hash)obj) == 0 ;
 		}
 		return false;
 	}
 	
 	
 	private static ThreadLocal<byte[]> BUFFER = new ThreadLocal<byte[]>();
 	private static byte[] ensureBuffer() {
 		byte[] buffer = BUFFER.get();
 		if(buffer==null){
 			BUFFER.set(new byte[32*1024]);
 			buffer = BUFFER.get();
 		}
 		return buffer;
 	}
 	
 	public static Hash buildHash(byte[] buffer) {
 		return buildHash(new Bytes(buffer));
 	}
 	public static Hash buildHash(String s)  {
 		return buildHash(new Bytes(s));
 	}
 	public static Hash buildHash(InputStream in)  {
 		return buildHash(new Bytes(ensureBuffer(),in));
 	}
 	
 	public static Hash buildHash(Iterable<Bytes> it) {
 		MessageDigest md = MessageDigestUtils.md();
 		for (Bytes buffer : it) {
 			md.update(buffer.bytes(), 0, buffer.size());
 		}
 		return new Hash(md.digest());
 	}
 
 
 	
 }
