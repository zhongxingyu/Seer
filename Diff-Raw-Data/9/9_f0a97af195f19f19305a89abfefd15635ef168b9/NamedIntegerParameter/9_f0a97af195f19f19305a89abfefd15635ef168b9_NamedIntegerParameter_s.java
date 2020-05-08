 package net.cheney.motown.server.api;
 
 public class NamedIntegerParameter extends NamedParameter<Integer> {
 
	protected NamedIntegerParameter(String name) {
 		super(name, Integer.class);
 	}
 
 	@Override
 	public Integer decode(String string) {
 		return Integer.valueOf(string);
 	}
 
 	@Override
 	public String encode(Integer value) {
 		return value.toString();
 	}
 
 }
