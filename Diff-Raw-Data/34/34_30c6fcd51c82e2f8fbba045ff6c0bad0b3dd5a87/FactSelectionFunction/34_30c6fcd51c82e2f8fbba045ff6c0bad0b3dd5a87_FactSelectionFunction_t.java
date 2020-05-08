 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.data.warehouse.query;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 public class FactSelectionFunction {
 	
 	public static final FactSelectionFunction AVG = new FactSelectionFunction("AVG");
 	
 	public static final FactSelectionFunction COUNT = new FactSelectionFunction("COUNT");
 	
 	public static final FactSelectionFunction MAX = new FactSelectionFunction("MAX");
 	
 	public static final FactSelectionFunction MIN = new FactSelectionFunction("MIN");
 	
 	public static final FactSelectionFunction STD = new FactSelectionFunction("STD");
 	
 	public static final FactSelectionFunction SUM = new FactSelectionFunction("SUM");
 
 	public static final FactSelectionFunction[] ALL_FUNCTIONS = 
 			new FactSelectionFunction[] { AVG, COUNT, MAX, MIN, STD, SUM };
 	
 	private static final Map<String, FactSelectionFunction> functionsMappedByCode;
 	
 	public static FactSelectionFunction byCode(String code) {
 		return functionsMappedByCode == null ? null : functionsMappedByCode.get(code);
 	}
 	
 	static {
 		Map<String, FactSelectionFunction> data = new HashMap<String, FactSelectionFunction>();
		for(FactSelectionFunction function : ALL_FUNCTIONS) {
			if(data.containsKey(function.getCode()))
				throw new RuntimeException(String.format("Duplicate function: %s", function.getCode()));
			
			data.put(function.getCode(), function);
 		}
 		functionsMappedByCode = Collections.unmodifiableMap(data);
 	}
 	
 	private String syntax;
 	
 	private FactSelectionFunction(String syntax) {
 		this.syntax = syntax;
 	}
 	
 	public String getSyntax() {
 		return syntax;
 	}
 	
 	public String getCode() {
 		return syntax;
 	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((syntax == null) ? 0 : syntax.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactSelectionFunction other = (FactSelectionFunction) obj;
		if (syntax == null) {
			if (other.syntax != null)
				return false;
		} else if (!syntax.equals(other.syntax))
			return false;
		return true;
	}
 }
