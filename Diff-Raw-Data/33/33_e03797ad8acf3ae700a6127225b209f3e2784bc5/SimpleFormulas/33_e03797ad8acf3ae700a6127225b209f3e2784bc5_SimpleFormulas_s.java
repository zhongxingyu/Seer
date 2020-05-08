 /*
  * Copyright 2013 Charles University in Prague
  * Copyright 2013 Vojtech Horky
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
 package cz.cuni.mff.d3s.spl.core.impl;
 
 import java.util.NoSuchElementException;
 
 import cz.cuni.mff.d3s.spl.core.Data;
 import cz.cuni.mff.d3s.spl.core.Formula;
 import cz.cuni.mff.d3s.spl.core.MathematicalInterpretation;
 import cz.cuni.mff.d3s.spl.core.Result;
 
 public class SimpleFormulas {
 	public static Formula createAsmallerThanB(String a, String b) {
 		return new LeftSmallerThanRight(a, b);
 	}
 	
 	public static Formula createSmallerThanConst(String var, double constant) {
 		return new SmallerThanConstant(var, constant);
 	}
 	
 	private static class LeftSmallerThanRight implements Formula {
 		private MathematicalInterpretation apparatus;
 		private String leftName;
 		private Data leftData;
 		private String rightName;
 		private Data rightData;
 		
 		public LeftSmallerThanRight(String left, String right) {
 			apparatus = KindergartenMath.INSTANCE;
 			leftName = left;
 			rightName = right;
 		}
 
 		@Override
 		public void setInterpreation(MathematicalInterpretation apparatus) {
 			this.apparatus = apparatus;
 		}
 
 		@Override
 		public void bind(String variable, Data data)
 				throws NoSuchElementException {
 			if (leftName.equals(variable)) {
 				leftData = data;
 			} else if (rightName.equals(variable)) {
 				rightData = data;
 			} else {
 				throw new NoSuchElementException("No such variable in the formula.");
 			}
 		}
 
 		@Override
 		public Result evaluate() {
 			return apparatus.isGreaterThan(rightData.getStatisticSnapshot(),
 				leftData.getStatisticSnapshot());
 		}
 	}
 	
 	private static class SmallerThanConstant implements Formula {
 		private MathematicalInterpretation interpretation;
 		private String sourceName;
 		private Data data;
 		private double constant;
 		
 		public SmallerThanConstant(String name, double c) {
 			interpretation = KindergartenMath.INSTANCE;
 			sourceName = name;
 			constant = c;
 		}
 
 		@Override
 		public void setInterpreation(MathematicalInterpretation interpretation) {
 			this.interpretation = interpretation;
 		}
 
 		@Override
		public void bind(String variable, Data data)
 				throws NoSuchElementException {
 			if (sourceName.equals(variable)) {
				this.data = data;
 			} else {
 				throw new NoSuchElementException("No such variable in the formula.");
 			}
 		}
 
 		@Override
 		public Result evaluate() {
 			return interpretation.isSmallerThan(data.getStatisticSnapshot(), constant);
 		}
 	}
 }
