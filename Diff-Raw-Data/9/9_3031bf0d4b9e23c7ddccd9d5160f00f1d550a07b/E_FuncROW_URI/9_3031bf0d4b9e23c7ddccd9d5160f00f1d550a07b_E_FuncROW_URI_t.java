 /**
  * Copyright 2009 Andreas Langegger, andreas@langegger.at, Austria
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
 package uk.co.brenn.xlwrap.expr.func.spreadsheet;
 
 import at.jku.xlwrap.common.Utils;
 import at.jku.xlwrap.common.XLWrapException;
 import at.jku.xlwrap.exec.ExecutionContext;
 import at.jku.xlwrap.map.expr.E_RangeRef;
 import at.jku.xlwrap.map.expr.TypeCast;
 import at.jku.xlwrap.map.expr.XLExpr;
 import at.jku.xlwrap.map.expr.func.FunctionRegistry;
 import at.jku.xlwrap.map.expr.func.XLExprFunction;
 import at.jku.xlwrap.map.expr.val.E_Boolean;
 import at.jku.xlwrap.map.expr.val.E_Long;
 import at.jku.xlwrap.map.expr.val.E_String;
 import at.jku.xlwrap.map.expr.val.XLExprValue;
 import at.jku.xlwrap.map.range.CellRange;
 import at.jku.xlwrap.map.range.Range;
 import at.jku.xlwrap.map.range.Range.CellIterator;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 
 /**
  * @author dorgon
  *
  */
 public class E_FuncROW_URI extends XLExprFunction {
 
 	/**
 	 * default constructor
 	 */
 	public E_FuncROW_URI() {
 	}
 
 	/**
 	 * single argument (a range)
 	 */
 	public E_FuncROW_URI(XLExpr arg) {
 		args.add(arg);
 	}
 
 	@Override
 	public XLExprValue<String> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
 		// ignores actual cell value, just use the range reference to determine row
 
 		if (!(args.get(1) instanceof E_RangeRef))
 			throw new XLWrapException("Argument " + args.get(1) + " of " + FunctionRegistry.getFunctionName(this.getClass()) + " must be a cell range reference.");
 		Range absolute = ((E_RangeRef) args.get(1)).getRange().getAbsoluteRange(context);
 		if (!(absolute instanceof CellRange))
 			throw new XLWrapException("Argument " + args.get(1) + " of " + FunctionRegistry.getFunctionName(this.getClass()) + " must be a cell range reference.");
                 String prefix = getArg(0).eval(context).getValue().toString();
                 XLExprValue<?> value1 = getArg(1).eval(context);
                 if (value1 == null){
                     return null;
                 }
		int row = ((CellRange) absolute).getRow() + 1;
 		return new E_String(prefix + row);
 	}
 	
 }
