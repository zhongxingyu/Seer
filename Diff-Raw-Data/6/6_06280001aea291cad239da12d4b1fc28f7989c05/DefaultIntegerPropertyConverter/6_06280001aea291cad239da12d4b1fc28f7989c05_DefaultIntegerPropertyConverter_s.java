 package com.adaptiweb.utils.ci;
 
 import com.adaptiweb.utils.commons.VariableResolver;
 
 
 class DefaultIntegerPropertyConverter implements PropertyConverter<Integer> {
 
 	@Override
 	public Integer convert(VariableResolver variables, String name, String defaultValue) {
		return Integer.parseInt(variables.resolveValue(name));
 	}
 
 	@Override
 	public Class<Integer> getType() {
		return int.class;
 	}
 }
