 package edu.tum.lua.types;
 
 import java.util.Arrays;
 import java.util.List;
 
 public abstract class LuaFunctionNative implements LuaFunction {
 
 	@Override
 	public abstract List<Object> apply(List<Object> arguments);
 
 	@Override
 	public List<Object> apply(Object... arguments) {
 		return apply(Arrays.asList(arguments));
 	}
 }
