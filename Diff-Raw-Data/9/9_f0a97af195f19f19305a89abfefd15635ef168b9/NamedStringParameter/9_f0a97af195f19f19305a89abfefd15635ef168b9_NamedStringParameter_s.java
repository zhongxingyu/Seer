 package net.cheney.motown.server.api;
 
 public class NamedStringParameter extends NamedParameter<String> {
 
	protected NamedStringParameter(String name) {
 		super(name, String.class);
 	}
 
 	@Override
 	public String decode(String string) {
 		return string.toString();
 	}
 
 	@Override
 	public String encode(String value) {
 		return value.toString();
 	}
 
 }
