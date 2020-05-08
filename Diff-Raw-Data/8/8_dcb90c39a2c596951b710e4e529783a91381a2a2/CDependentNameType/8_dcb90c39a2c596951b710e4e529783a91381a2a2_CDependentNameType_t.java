 /*
  * Copyright (C) 1990-2001 DMS Decision Management Systems Ges.m.b.H.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
 * $Id: CDependentNameType.java,v 1.8 2005-01-20 14:07:34 klose Exp $
  */
 
 package org.caesarj.compiler.types;
 
 
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.ast.phylum.expression.JExpression;
 import org.caesarj.compiler.ast.phylum.expression.JFieldAccessExpression;
 import org.caesarj.compiler.ast.phylum.expression.JLocalVariableExpression;
 import org.caesarj.compiler.ast.phylum.expression.JNameExpression;
 import org.caesarj.compiler.ast.phylum.expression.JOwnerExpression;
 import org.caesarj.compiler.ast.phylum.expression.JThisExpression;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.context.CBlockContext;
 import org.caesarj.compiler.context.CClassBodyContext;
 import org.caesarj.compiler.context.CClassContext;
 import org.caesarj.compiler.context.CContext;
 import org.caesarj.compiler.context.CExpressionContext;
 import org.caesarj.compiler.context.CTypeContext;
 import org.caesarj.compiler.export.CClass;
 import org.caesarj.compiler.family.Path;
 import org.caesarj.util.MessageDescription;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 
 /**
  * This class represents (generic) class type or type variable in the type structure
  */
 public class CDependentNameType extends CClassNameType 
 {
 
 	// ----------------------------------------------------------------------
 	// CONSTRUCTORS
 	// ----------------------------------------------------------------------
 
 	/**
 	 * Construct a class type
 	 * @param	qualifiedName	the class qualified name of the class
 	 */
 	public CDependentNameType(String qualifiedName)
 	{
 		super(qualifiedName, false);
 	}
 
 
 
 	private JExpression convertToExpression() {
 	    String pathSegs[] = qualifiedName.split("/");
 	    JExpression expr = null;
 	    for (int i = 0; i < pathSegs.length-1; i++) {
 	        if(pathSegs[i].equals("this")) 
 	            expr = new JThisExpression(TokenReference.NO_REF, expr); 
 	        else 
 	            expr = new JNameExpression(TokenReference.NO_REF, expr, pathSegs[i]);
         }
 	    
 	    return expr;
 	}
 	
 	/**
 	 * Resolve and check this dependent type.
 	 */
 	public CType checkType(CTypeContext context) throws UnpositionedError
 	{	 	   
 	    // IVICA: try to lookup the path first	   
 	    JExpression expr = convertToExpression();
 	    if (expr == null){
 	        throw new UnpositionedError(KjcMessages.TYPE_UNKNOWN, qualifiedName);
 	    }
         CContext ctx = (CContext)context;
         CExpressionContext ectx = null;
         KjcEnvironment env;
 
        // create an expression context to analyse the expression
         if (context instanceof CClassContext){
             CClassContext classContext = (CClassContext)context;
             env = classContext.getEnvironment();
             ectx =
                 new CExpressionContext(
                     new CBlockContext(
                         new CClassBodyContext(classContext, env), env, 0 
                     ),
                     env
                 );
         
         } else if (context instanceof CBlockContext){
             env = ((CBlockContext)context).getEnvironment();
             ectx = new CExpressionContext( (CBlockContext)context, env );
         
         } else {
             throw new UnpositionedError(KjcMessages.TYPE_UNKNOWN, qualifiedName);
         }
         
        // try to analyse the fieldaccess
         try{
             expr = expr.analyse(ectx);
         
             if(expr instanceof JFieldAccessExpression || expr instanceof JLocalVariableExpression || expr instanceof JOwnerExpression) {                    
                 TypeFactory factory = context.getTypeFactory();              
                 CClass clazz;
                 
                 String pathSegs[] = qualifiedName.split("/");
 
                 // calculate the plain type of this dependent type
                 clazz = context.getClassReader().loadClass(
                     context.getTypeFactory(),
                     expr.getType(context.getTypeFactory()).getCClass().getQualifiedName()+"$"+pathSegs[pathSegs.length-1]
                 );
 
                 // calculate k for this dependent type
                 int k = Path.calcK( (CContext)context, expr );
                 
                 // create and return new CDependentType
                 CType t = clazz.getAbstractType().checkType(context);
                 CDependentType dt = new CDependentType((CContext)context, k, expr, t);
                 return dt;
             }
 
         } catch (Exception e){
             // If the message of a positioned error is in passThrough, the
             // exception is thrown further. Otherwise, a TYPE_UNKNOWN error
             // is thrown at the end of the function.
             final MessageDescription[] passThrough = new MessageDescription[]{
                     KjcMessages.UNINITIALIZED_FIELD_USED,
                     KjcMessages.UNINITIALIZED_LOCAL_VARIABLE
             };
             
             if (e instanceof PositionedError){
                 PositionedError pe = (PositionedError)e;
                 for (int i=0; i<passThrough.length; i++){
                     if (pe.hasDescription(passThrough[i])){
                         throw new UnpositionedError(passThrough[i], pe.getMessageParameters() );
                     }
                 }
             }
         }
         
         // Throw a default error message
 		throw new UnpositionedError(KjcMessages.TYPE_UNKNOWN, qualifiedName);
 	}  
 		
 }
