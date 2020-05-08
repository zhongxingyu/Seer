 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.fitting.functions;
 
 /**
  * A binary operator that uses two functions
  */
 abstract public class ABinaryOperator extends AOperator implements IOperator {
 	IFunction fa;
 	IFunction fb;
 
 	public ABinaryOperator() {
 		super();
 	}
 
 	@Override
 	public boolean isExtendible() {
 		return false;
 	}
 
 	@Override
 	public int getRequiredFunctions() {
 		return 2;
 	}
 
 	@Override
 	public void addFunction(IFunction function) {
 		if (fa == null) {
 			setFunction(0, function);
 		} else if (fb == null) {
 			setFunction(1, function);
 		} else {
 			throw new IndexOutOfBoundsException("Can only add two functions to a binary operator");
 		}
 	}
 
 	@Override
 	public void setFunction(int index, IFunction function) {
 		switch (index) {
 		case 0:
 			fa = function;
 			break;
 		case 1:
 			fb = function;
 			break;
 		default:
 			throw new IndexOutOfBoundsException("Can not set this index as it is not 0 or 1");
 		}
		if (function != null)
			function.setParentOperator(this);
 		updateParameters();
 	}
 
 	@Override
 	public void updateParameters() {
 		params.clear();
 		for (int i = 0; i < 2; i++) {
 			IFunction f = getFunction(i);
 			if (f == null)
 				continue;
 			for (int j = 0, jmax = f.getNoOfParameters(); j < jmax; j++) {
 				IParameter p = f.getParameter(j);
 				boolean add = true;
 				for (IParameter param : params) {
 					if (p == param) {
 						add = false;
 						break;
 					}
 				}
 				if (add) {
 					params.add(p);
 				}
 			}
 		}
 		setDirty(true);
 
 		if (parent != null) {
 			parent.updateParameters();
 		}
 	}
 
 	@Override
 	public int getNoOfFunctions() {
 		return (fa == null ? 0 : 1) + (fb == null ? 0 : 1);
 	}
 
 	@Override
 	public IFunction getFunction(int index) {
 		switch (index) {
 		case 0:
 			return fa;
 		case 1:
 			return fb;
 		default:
 			throw new IndexOutOfBoundsException("Can not get this index as it is not 0 or 1");
 		}
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer out = new StringBuffer();
 		if (fa != null) {
 			out.append(String.format("Function 0 - \n"));
 			out.append(fa.toString());
 			out.append('\n');
 		}
 		if (fb != null) {
 			out.append(String.format("Function 1 - \n"));
 			out.append(fb.toString());
 			out.append('\n');
 		}
 
 		return out.length() == 0 ? OPERATOR_NO_FUNCTIONS : out.substring(0, out.length() - 1);
 	}
 
 	@Override
 	public IFunction[] getFunctions() {
 		return new IFunction[] {fa, fb};
 	}
 
 	@Override
 	public void removeFunction(int index) {
 		switch (index) {
 		case 0:
 			fa.setParentOperator(null);
 			fa = fb;
 			fb = null;
 			break;
 		case 1:
 			fb.setParentOperator(null);
 			fb = null;
 			break;
 		default:
 			throw new IndexOutOfBoundsException("Can not remove this index as it is not 0 or 1");
 		}
 		updateParameters();
 	}
 
 	@Override
 	public boolean isValid() {
 		return super.isValid() && fa != null && fb != null;
 	}
 }
