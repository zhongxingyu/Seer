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
 
 import com.evinceframework.data.warehouse.FactTable;
 
 public class AbstractQuery implements Query {
 	
 	private FactTable factTable;
 	
 	private FactSelection[] factSelections = new FactSelection[]{};
 	
 	private DimensionCriterion<?>[] dimensionCriteria = new DimensionCriterion[]{};
 	
	private FactRangeCriterion[] factCriteria = new FactRangeCriterion[]{};
 	
 	protected AbstractQuery(FactTable factTable, FactSelection[] factSelections) {
 		this.factTable = factTable;
 		this.factSelections = factSelections;
 	}
 
 	protected AbstractQuery(FactTable factTable, FactSelection[] factSelections, 
 			DimensionCriterion<?>[] dimensionCriteria) {
 		this.factTable = factTable;
 		this.factSelections = factSelections;
 		this.dimensionCriteria = dimensionCriteria;
 	}
 	
 	protected AbstractQuery(FactTable factTable, FactSelection[] factSelections,
			DimensionCriterion<?>[] dimensionCriteria, FactRangeCriterion[] factCriteria) {
 		this.factTable = factTable;
 		this.factSelections = factSelections;
 		this.dimensionCriteria = dimensionCriteria;
 		this.factCriteria = factCriteria;
 	}
 
 	@Override
 	public FactTable getFactTable() {
 		return factTable;
 	}
 
 	@Override
 	public FactSelection[] getFactSelections() {
 		return factSelections;
 	}
 
 	@Override
 	public DimensionCriterion<?>[] getDimensionCriterion() {
 		return dimensionCriteria;
 	}
 
 	@Override
	public FactRangeCriterion[] getFactCriterion() {
 		return factCriteria;
 	}
 
 }
