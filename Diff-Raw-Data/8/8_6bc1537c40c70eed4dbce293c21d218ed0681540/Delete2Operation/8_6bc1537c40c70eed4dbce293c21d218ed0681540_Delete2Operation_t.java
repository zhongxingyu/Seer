 /*
  *   Delete2Operation.java
  * 	 @Author Oleg Gorobets
  *   Created: 26.07.2007
  *   CVS-ID: $Id: 
  *************************************************************************/
 
 package org.swfparser.operation;
 
 import java.util.Stack;
 
 import org.swfparser.CodeUtil;
 import org.swfparser.Operation;
 
 public class Delete2Operation extends UnaryOperation {
 
    protected Operation property;

 	public Delete2Operation(Stack<Operation> stack) {
 		super(stack);
        property = stack.pop();
 	}
 
 	public String getStringValue(int level) {
 		return 
 		new StringBuffer()
 		.append(CodeUtil.getIndent(level))
 		.append("delete ")
        .append(CodeUtil.getSimpleValue(property, level))
//		.append(op.getStringValue(level))
 		
 		.toString();
 	}
 
 }
