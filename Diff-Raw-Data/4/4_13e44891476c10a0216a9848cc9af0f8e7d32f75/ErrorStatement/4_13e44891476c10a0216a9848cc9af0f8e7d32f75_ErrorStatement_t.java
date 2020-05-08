 /*
  * <copyright>
  *
  * Copyright (c) 2005-2006 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.gmf.internal.xpand.ast;
 
 import java.util.Set;
 
 import org.eclipse.gmf.internal.xpand.expression.AnalysationIssue;
 import org.eclipse.gmf.internal.xpand.expression.ast.Expression;
 import org.eclipse.gmf.internal.xpand.model.XpandExecutionContext;
 
 /**
  * @author Sven Efftinge
  */
 public class ErrorStatement extends Statement {
 
     private final Expression message;
 
     public ErrorStatement(final int start, final int end, final int line, final Expression msg) {
         super(start, end, line);
         message = msg;
     }
 
     public Expression getMessage() {
         return message;
     }
 
     public void analyze(final XpandExecutionContext ctx, final Set<AnalysationIssue> issues) {
         message.analyze(ctx, issues);
     }
 
     @Override
     public void evaluateInternal(final XpandExecutionContext ctx) {
    	String result = String.valueOf(message.evaluate(ctx));
        System.err.println("ERROR:" + result); // FIXME syserr is not an error reporting!!!
        throw new RuntimeException(result);
     }
 
 }
