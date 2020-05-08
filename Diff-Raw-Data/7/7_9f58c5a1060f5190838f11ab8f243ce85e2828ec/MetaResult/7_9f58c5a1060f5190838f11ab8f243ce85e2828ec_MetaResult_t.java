 package org.dada.core;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 import clojure.lang.Indexed;
 
 public class MetaResult  implements Serializable, Indexed {
 
 	private final Metadata<?, ?> metadata;
 	private final String prefix;
 	private final Collection<Collection<Object>> pairs;
 	private final Collection<Object> operation;
 	
 	public MetaResult(Metadata<?, ?> metadata, String prefix, Collection<Collection<Object>> pairs, Collection<Object> operation) {
 		super();
 		this.metadata = metadata;
 		this.prefix = prefix;
 		this.pairs = pairs;
 		this.operation = operation;
		System.out.println("METARESULT: "+prefix+", "+pairs+", "+operation);
 	}
 
 	public Metadata<?, ?> getMetedata() {
 		return metadata;
 	}
 
 	public String getPrefix() {
 		return prefix;
 	}
 
 	public Collection<Collection<Object>> getPairs() {
 		return pairs;
 	}
 
 	public Collection<Object> getOperation() {
 		return operation;
 	}
 
 	// Indexed
 	
 	@Override
 	public int count() {
 		return 4;
 	}
 
 	@Override
 	public Object nth(int i) {
 		return nth(i, null);
 	}
 
 	@Override
 	public Object nth(int i, Object notFound) {
 		switch (i) {
 		case 0: return metadata;
 		case 1: return prefix;
 		case 2: return pairs;
 		case 3: return operation;
 		default: return notFound;
 		}
 	}
 
 }
