 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.hash;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.fasterxml.jackson.annotation.JsonValue;
 
 public class HashKey implements Comparable<HashKey>{
 	final public HashKeyResource type;
 	final public Hash hash;
 	
 	public HashKey(HashKeyResource type, byte[] digest) {
 		this.hash = new Hash(digest);
 		this.type = type;
 	}
 	public HashKey(HashKeyResource type, Hash hash) {
 		this.hash = hash;
 		this.type = type;
 	}
 
 	public HashKey(HashKeyResource valueOf, String substring) {
 		this(valueOf,new Hash(substring));
 	}
 
 	
 	public static HashKey valueOf(String s) {
 		String g = s.substring(0, 1);
 		return new HashKey(HashKeyResource.valueOf(g),s.substring(1));
 	}
 
 	@Override @JsonValue
 	public String toString() {
 		return type.name()+hash.toString();
 	}
 
 	@Override
 	public int hashCode() {
 		return type.ordinal() + hash.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof HashKey) {
 			return compareTo((HashKey) obj) == 0;
 		}
 		return false;
 	}
 
 	@Override
 	public int compareTo(HashKey that) {
 		if(that == null) return 1;
 		int rc = this.type.ordinal() - that.type.ordinal();
		if( rc != 0 ){
 			rc = this.hash.compareTo(that.hash);
 		}
 		return rc;
 	}
 	
 	public static HashKey buildHashKey(HashKeyResource resource, InputStream in) throws IOException {
 		return new HashKey(resource,Hash.buildHash(in));
 	}
 
 	public static HashKey buildHashKey(HashKeyResource resource, byte[] buffer) {
 		return new HashKey(resource,Hash.buildHash(buffer));
 	}
 	
 	public static HashKey buildHashKey(HashKeyResource resource, String s) {
 		return new HashKey(resource,Hash.buildHash(s));
 	}
 	
 
 }
