 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
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
 * $Id: JMethodCallExpression.java,v 1.28 2005-03-03 15:49:36 aracic Exp $
  */
 
 package org.caesarj.compiler.ast.phylum.expression;
 
 
 import org.caesarj.compiler.Log;
 import org.caesarj.compiler.ast.CMethodNotFoundError;
 import org.caesarj.compiler.ast.visitor.IVisitor;
 import org.caesarj.compiler.codegen.CodeSequence;
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.constants.Constants;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.context.AdditionalGenerationContext;
 import org.caesarj.compiler.context.CClassContext;
 import org.caesarj.compiler.context.CConstructorContext;
 import org.caesarj.compiler.context.CContext;
 import org.caesarj.compiler.context.CExpressionContext;
 import org.caesarj.compiler.context.GenerationContext;
 import org.caesarj.compiler.export.CClass;
 import org.caesarj.compiler.export.CMethod;
 import org.caesarj.compiler.export.CSourceClass;
 import org.caesarj.compiler.export.CSourceMethod;
 import org.caesarj.compiler.family.ArgumentAccess;
 import org.caesarj.compiler.family.ContextExpression;
 import org.caesarj.compiler.family.Dummy;
 import org.caesarj.compiler.family.MethodAccess;
 import org.caesarj.compiler.family.Path;
 import org.caesarj.compiler.types.CArrayType;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CThrowableInfo;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.CWarning;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 
 /**
  * JLS 15.12 Method Invocation Expressions
  */
 public class JMethodCallExpression extends JExpression
 {
 
 	/**
 	 * Construct a node in the parsing tree
 	 * This method is directly called by the parser
 	 * @param	where		the line of this node in the source code
 	 * @param	prefix		an expression that is a field of a class representing a method
 	 * @param	ident		the method identifier
 	 * @param	args		the argument of the call
 	 */
 	public JMethodCallExpression(
 		TokenReference where,
 		JExpression prefix,
 		String ident,
 		JExpression[] args)
 	{
 		super(where);
 
 		this.prefix = prefix;
 		this.ident = ident.intern(); // $$$ graf 010530 : why intern ?
 		this.args = args;
 		this.analysed = false;
 	}
 
 	/**
 	 * Constructs a node. This node need NOT be analysed
 	 * For example these objects are used in sythetic accessors used for inner classes.
 	 * The prefix and the arguments must be analysed before the creation of these object, 
 	 * or they must be expressions which not need to be analysed.
 	 *
 	 * @param	where		the line of this node in the source code
 	 * @param	prefix		an expression that is a field of a class representing a method
 	 * @param	ident		the method identifier
 	 * @param	args		the argument of the call
 	 */
 	public JMethodCallExpression(
 		TokenReference where,
 		JExpression prefix,
 		CMethod method,
 		JExpression[] args)
 	{
 		super(where);
 
 		this.prefix = prefix;
 		this.method = method;
 		this.ident = method.getIdent();
 		this.args = args;
 		this.type = method.getReturnType();
 		this.analysed = false; // IVICA: reanalyse, this is relevant for recalculating the family
 	}
 
 	// ----------------------------------------------------------------------
 	// ACCESSORS
 	// ----------------------------------------------------------------------
 
 	/**
 	 * @return the type of this expression
 	 */
 	public CType getType(TypeFactory factory)
 	{
 		return type == null ? method.getReturnType() : type;
 	}
 
 	/**
 	 * Returns true iff this expression can be used as a statement (JLS 14.8)
 	 */
 	public boolean isStatementExpression()
 	{
 		return true;
 	}
 
 	// ----------------------------------------------------------------------
 	// SEMANTIC ANALYSIS
 	// ----------------------------------------------------------------------
 	/**
 	 * Analyses the expression (semantically).
 	 * @param	context		the analysis context
 	 * @return	an equivalent, analysed expression
 	 * @exception	PositionedError	the analysis detected an error
 	 */
 	public JExpression analyse(CExpressionContext context)
 		throws PositionedError
 	{
 		TypeFactory factory = context.getTypeFactory();
 
 		if (analysed) { return this; }
 		
 		//Walter: this method is called now 
 		//rather than analise the args directly
 		CType[] argTypes = getArgumentTypes(context, args, factory);
 		CClass local = context.getClassContext().getCClass();
 
 		if(local.isMixin() && prefix == null) {
 		    // IVICA: this part is quite tricky :(
 		    // the problem is, when accessing a cclass method with prefix == null
 		    // but the searched method is in one of the outer classes, we have to search in
 		    // interfaces, but our context is mixin implementation itself
 		    // so we do the following:
 		    try {
 		        // start searching the method in this
 		        prefix = new JThisExpression(getTokenReference());
 		        findMethod(context, local, argTypes);
 		    }
 		    catch(PositionedError e) {
 		        // if there is no method in this, go to the mixin interface owner of 
 		        // the mixin implementation
 		        CClass clazz = local.getMixinInterface().getOwner();
 		        
 		        // successivelly append outer() calls
 		        // outer().m() ?
 		        // outer().outer().m() ?
 		        // etc.
 		        boolean found = false;
 		        while(!found && clazz != null) {
 		            prefix = new CjOuterExpression(getTokenReference(), clazz.getAbstractType());
 		            try {
 		                findMethod(context, local, argTypes);
 		                found = true;
 		            }
 		            catch (PositionedError e2) {
 		                clazz = clazz.getOwner();		                
                     }
 		        }
 		        
                 if(!found)
                     throw e;
             }
 		}
 		else {
 		    findMethod(context, local, argTypes);
 		}
 		
 
  		CReferenceType[] exceptions = method.getThrowables();
 
 		for (int i = 0; i < exceptions.length; i++)
 		{
 			if (exceptions[i].isCheckedException(context))
 			{
 				if (prefix == null
 					|| // special case of clone
 				!prefix
 						.getType(factory)
 						.isArrayType()
 					|| ident != Constants.JAV_CLONE
 					|| !exceptions[i].getCClass().getQualifiedName().equals(
 						"java/lang/CloneNotSupportedException"))
 				{
 					context.getBodyContext().addThrowable(
 						new CThrowableInfo(exceptions[i], this));
 				}
 			}
 		}
 
 		CClass access = method.getOwner();
 
 		if (prefix == null && !method.isStatic())
 		{
 			if (access == local)
 			{
 				prefix = new JThisExpression(getTokenReference());
 			}
 			else
 			{
 			    // IVICA    
 			    if(local.isMixin()) {
 			        prefix = new CjOuterExpression(getTokenReference(), access.getAbstractType());
 			    }			    
 			    else {
 			        prefix = new JOwnerExpression(getTokenReference(), access);
 			    }
 			}
 			prefix = prefix.analyse(context);
 		}
 
 		if ((prefixType == null) && (prefix != null))
 		{
 			prefixType = prefix.getType(factory);
 		}
 
 		// JLS 8.8.5.1
 		// An explicit constructor invocation statement in a constructor body may 
 		// not refer to any instance variables or instance methods declared in 
 		// this class or any superclass, or use this or super in any expression; 
 		// otherwise, a compile-time error occurs.     
 		if ((context.getMethodContext() instanceof CConstructorContext)
 			&& (prefix instanceof JThisExpression)
 			&& !method.isStatic())
 		{
 			check(
 				context,
 				((CConstructorContext) context.getMethodContext())
 					.isSuperConstructorCalled(),
 				KjcMessages.INSTANCE_METHOD_IN_EXP_CONSTRUCTOR_CALL,
 				method);
 		}
 
 		check(
 			context,
 			method.isStatic() || !(prefix instanceof JTypeNameExpression),
 			KjcMessages.INSTANCE_METHOD_CALL_IN_STATIC_CONTEXT,
 			method);
 
 		if (method.isStatic()
 			&& prefix != null
 			&& !(prefix instanceof JTypeNameExpression))
 		{
 			context.reportTrouble(
 				new CWarning(
 					getTokenReference(),
 					KjcMessages.INSTANCE_PREFIXES_STATIC_METHOD,
 					method.getIdent(),
 					prefix.getType(factory)));
 		}
 		
 		
 		argTypes = method.getParameters();
 				
 		Log.verbose("METHOD-CALL to "+getIdent()+" at line "+getTokenReference().getLine());
 		argPaths = new Path[argTypes.length];
 		argTypePaths = new Path[argTypes.length];
 		for (int i = 0; i < argTypes.length; i++)
 		{
 			if (args[i] instanceof JTypeNameExpression)
 			{
 				check(
 					context,
 					false,
 					KjcMessages.VAR_UNKNOWN,
 					((JTypeNameExpression) args[i]).getQualifiedName());
 			}
 			
 			//
 			// IVICA: family type-checks on method calls
 			//			
 			doFamilyCheck(context, i, argTypes);
 			
 			
 			args[i] = args[i].convertType(context, argTypes[i]);
 		}				
 		
 		/////////////////////////////////////////////////
 
 		// Mark method as used if it is a source method
 		if (method instanceof CSourceMethod)
 		{
 			((CSourceMethod) method).setUsed();
 		}
 
 		if (method.getReturnType().getTypeID() != TID_VOID
 			&& context.discardValue())
 		{
 			context.reportTrouble(
 				new CWarning(
 					getTokenReference(),
 					KjcMessages.UNUSED_RETURN_VALUE_FROM_FUNCTION_CALL,
 					method.getIdent()));
 
 		}
 		boolean isSuper = prefix instanceof JSuperExpression;
 
 		if (method.requiresAccessor(local, isSuper))
 		{
 			if (!method.isStatic())
 			{
 				JExpression[] argsTmp = new JExpression[args.length + 1];
 
 				if (isSuper)
 				{
 					prefix =
 						new JFieldAccessExpression(
 							getTokenReference(),
 							new JThisExpression(getTokenReference()),
 							local.getField(JAV_OUTER_THIS));
 				}
 				argsTmp[0] = prefix;
 				System.arraycopy(args, 0, argsTmp, 1, args.length);
 				prefix = null;
 				args = argsTmp;
 			}
 			CSourceClass target = method.getAccessorOwner((CSourceClass) local);
 
 			method =
 				method.getAccessor(context.getTypeFactory(), target, isSuper);
 		}
 
 		// if the returntype is generic (and the owner of the method not 'this class')
 		// or it is a typevariable and need a cast
 		type = method.getReturnType();
 
 		
 		// IVICA: calculate the family of this epxression
 		if( type.isCaesarReference() ) {
 		    calcExpressionFamily();
 		}
 		else if(		    
 		    type.isClassType() 
 		    //&& type.getCClass() == context.getClassContext().getCClass() 
 		    && getIdent().startsWith(JAV_ACCESSOR)
 		    && method.isSynthetic()
 	    ) {
 		    // CRITICAL: this is not nice at all -> tide it up
 		    // we have an outer accessor method (and it has never a prefix?)  
 	        int k = 0;		        
             CClass clazz = type.getCClass();
             
             CContext ctx = context.getBlockContext();
             
             // ... find first class context ... 
             while (!(ctx instanceof CClassContext)){
                 ctx = ctx.getParentContext();
                 k++;
             }
             // ... and search for the correct outer class.
             while ( ((CClassContext)ctx).getCClass() != clazz ){
                 ctx = ctx.getParentContext();
                 k++;
                 
                 check(
                     context,
                     !(context == null || !(ctx instanceof CClassContext) ),                        
                     CaesarMessages.ILLEGAL_PATH_ELEMENT, 
                     "accessor method not returning a outer reference"
                 );
             }                		       
 	        
 	        family = new ContextExpression(null, k+1, null);
 		    thisAsFamily = new ContextExpression(null, k, null);
 		}
 		
 		
 		// fixed lackner 18.03.2002 commment out because sometimes it is necessary to evaluate it twice.
 		//    analysed = true;
 		return this;
 	}
 	
 	/**
 	 * calculate the family of the returned object
 	 */
 	protected void calcExpressionFamily() throws PositionedError {
 		try {
 	        // CTODO: ugly these constants
 	        if(method.isCaesarFactoryMethod()) {
 	            // factory method... make special treatement
 	            // CTODO: this is ugly, why not having own data type for factory methods
 	            family = prefix.getThisAsFamily();
 	            thisAsFamily = new Dummy(prefix.getThisAsFamily().clonePath());
 	        }
 	        else {
 		        Path returnTypePath = ((CReferenceType)type).getPath().clonePath();
 		        Path returnTypePathHead = returnTypePath.getHead();
 		        if( ((ContextExpression)returnTypePathHead).getK() == 0) {
 		            // here we have a return type depending directly on a method argument
 		            
 		            ArgumentAccess fa = (ArgumentAccess)returnTypePath.getHeadPred();		           
 		            family = returnTypePath.substituteFirstAccess( argPaths[fa.getArgPos()] );
 		        }
 		        else {
 		            // here we have a dependent type not depending on a method argument
 	                thisAsFamily = prefix.getThisAsFamily().clonePath();
 		            thisAsFamily = new MethodAccess(thisAsFamily, method.getIdent(), null);		            
 		            thisAsFamily = thisAsFamily.append( returnTypePath );
 		            thisAsFamily = new Dummy(thisAsFamily);
 	
 		            family = thisAsFamily.clonePath().normalize2();
 		        }
 	        }
 	    }
 	    catch (UnpositionedError e) {
 	        throw e.addPosition(getTokenReference());
 	    }
 	}
 	
 	protected void doFamilyCheck(CExpressionContext context, int i, CType[] argTypes) throws PositionedError {
 	    try {	
 
 //		    argPaths[i] = Path.createFrom(context.getBlockContext(), args[i]);			    
 //		    argTypePaths[i] = argPaths[i].normalize();
 
 	        argPaths[i] = args[i].getThisAsFamily();
 	        argTypePaths[i] = args[i].getFamily();
 
 		    if(argTypes[i].isDependentType()) {		       
 		        CReferenceType refType = (CReferenceType)argTypes[i];
 		        
 		        Log.verbose("handling parameter "+i);
 		        		        			    			    
 			    Log.verbose("expr:"+argPaths[i]+" -> fam:"+argTypePaths[i]);
 			    
 			    //if(argTypes[i].isDependentType())
 			    if(argTypes[i].getCClass().isNested()) {
 			        CReferenceType depType = (CReferenceType)refType;
 			        Path depTypePath = depType.getPath();
 			        
 			        // the family of this dependent type starts with a parameter
 			        if(((ContextExpression)depTypePath.getHead()).getK() == 0) {
 			            // use the path of the dependent parameter type
 			            // and substitute the parameter access ( ctx(0).param ) with the 
 			            // passed path of the argument expression				            
 			            ArgumentAccess fa = (ArgumentAccess)depTypePath.getHeadPred();
 			            
 			            check(
 			                context,
 			                fa.getArgPos() < i,
 			                CaesarMessages.DECLARATION_DEPENDENCY_ORDER
 			            );
 			            
 			            depTypePath = depTypePath.substituteFirstAccess( argPaths[fa.getArgPos()] );
 			            
 			            Log.verbose("\tfam:"+depTypePath);
 			            				            
 			            check(
 							context,
 							argTypePaths[i].isAssignableTo( depTypePath ),
 							KjcMessages.ASSIGNMENT_BADTYPE,
							argTypePaths[i].toString()+"."+refType.getIdent(),
							depTypePath.toString()+"."+depType.getIdent());
 			        }
 			        else {
 			            // this dependent type does not depend on a family defined
 			            // in the signature of the method
 			            
 			            Path p = prefix.getThisAsFamily();
 			            				            
 			            p = new MethodAccess(p, method.getIdent(), null);				            
 			            p = p.append( depTypePath ); 
 			            
 			            Path pNorm = p.normalize2();
 			            
 			            Log.verbose("\tfam:"+pNorm);
 			            
 			            check(
 							context,
 							argTypePaths[i].isAssignableTo( pNorm ),
 							KjcMessages.ASSIGNMENT_BADTYPE,
 							"P:"+argTypePaths[i].toString(),
 							"P:"+pNorm.toString());
 			        }
 			    }
 		    }
 		}
 		catch (UnpositionedError e) {
             e.addPosition(getTokenReference());
         }
 	}
 	
 	/**
 	 * This method has been created to allow subclasses to override it.
 	 * @param context
 	 * @param args
 	 * @param factory
 	 * @return
 	 * @throws PositionedError
 	 * @author Walter Augusto Werner
 	 */
 	protected CType[] getArgumentTypes(
 		CExpressionContext context,
 		JExpression[] args,
 		TypeFactory factory)
 		throws PositionedError
 	{
 		CType[] argTypes = new CType[args.length];
 
 		for (int i = 0; i < argTypes.length; i++)
 		{
 		// evaluate the arguments in rhs mode, result will be used
 			args[i] =
 				args[i].analyse(
 					new CExpressionContext(context, context.getEnvironment()));
 			argTypes[i] = args[i].getType(factory);
 			try
 			{
 				argTypes[i] = argTypes[i].checkType(context);
 			}
 			catch (UnpositionedError e)
 			{
 				throw e.addPosition(getTokenReference());
 			}
 		}
 		return argTypes;
 	}
 
 	protected void findMethod(
 		CExpressionContext context,
 		CClass local,
 		CType[] argTypes)
 		throws PositionedError
 	{
 		TypeFactory factory = context.getTypeFactory();
 
 		if (prefix != null)
 		{
 			// evaluate the prefix in rhs mode, result will be used
 			prefix =
 				prefix.analyse(
 					new CExpressionContext(context, context.getEnvironment()));
 			if (prefix instanceof JNameExpression)
 			{
 				// condition as if-statement because of arguments to method check
 				check(
 					context,
 					false,
 					KjcMessages.BAD_METHOD_NAME,
 					((JNameExpression) prefix).getName());
 			}
 			check(
 				context,
 				prefix.getType(factory).isReference(),
 				KjcMessages.METHOD_BADPREFIX,
 				ident,
 				prefix.getType(factory));
 
 			if (prefix.getType(factory).isArrayType())
 			{
 				// JLS 6.6.1
 				// An array type is accessible if and only if its element type is accessible. 
 				check(
 					context,
 					((CArrayType) prefix.getType(factory))
 						.getBaseType()
 						.isPrimitive()
 						|| ((CArrayType) prefix.getType(factory))
 							.getBaseType()
 							.getCClass()
 							.isAccessible(
 							local),
 					KjcMessages.CLASS_NOACCESS,
 					((CArrayType) prefix.getType(factory)).getBaseType());
 			}
 			check(
 				context,
 				prefix.getType(factory).getCClass().isAccessible(local),
 				KjcMessages.CLASS_NOACCESS,
 				prefix.getType(factory).getCClass());
 
 			// if method is defined in more than one bound??
 			if (method != null)
 			{
 				return;
 			}
 
 			try
 			{
 				// FIX it lackner 19.11.01      used for internalInitLoadDefinition of PPage : prefix instanceof JSuperExpression
 				method =
 					prefix.getType(factory).getCClass().lookupMethod(
 						context,
 						local,
 						(prefix instanceof JThisExpression
 							|| prefix instanceof JSuperExpression)
 							? null
 							: prefix.getType(factory),
 						ident,
 						argTypes);
 				prefixType = prefix.getType(factory);
 			}
 			catch (UnpositionedError e)
 			{
 				throw e.addPosition(getTokenReference());
 			}
 		}
 		else
 		{
 			// FIX lackner 04.04.2002 workaround if double analysed with accessor method
 			if (method != null)
 			{
 				return;
 			}
 			try
 			{
 				method =
 					context.lookupMethod(context, local, null
 				/*prefix == null*/
 				, ident, argTypes);
 			}
 			catch (UnpositionedError e)
 			{
 				throw e.addPosition(getTokenReference());
 			}
 		}
 		if (method == null)
 		{
 			String prefixName;
 
 			if (prefix instanceof JNameExpression)
 			{
 				prefixName =
 					((JNameExpression) prefix).getQualifiedName() + ".";
 			}
 			else if (prefix instanceof JTypeNameExpression)
 			{
 				prefixName =
 					((JTypeNameExpression) prefix).getQualifiedName() + ".";
 			}
 			else
 			{
 				prefixName =
 					(prefix == null)
 						? ""
 						: prefix.getType(factory).toString() + ".";
 			}
 			throw new CMethodNotFoundError(
 				getTokenReference(),
 				this,
 				prefixName + ident,
 				argTypes);
 		}
 	}
 	
 	// ----------------------------------------------------------------------
 	// CODE GENERATION
 	// ----------------------------------------------------------------------
 	/**
 	 * Generates JVM bytecode to evaluate this expression.
 	 *
 	 * @param	code		the bytecode sequence
 	 * @param	discardValue	discard the result of the evaluation ?
 	 */
 	public void genCode(GenerationContext context, boolean discardValue)
 	{
 		CodeSequence code = context.getCodeSequence();
 		TypeFactory factory = context.getTypeFactory();
 
 		setLineNumber(code);
 
 		if (!method.isStatic())
 		{
 			prefix.genCode(context, false);
 		}
 		else if (prefix != null)
 		{
 			prefix.genCode(context, true);
 		}
 
 		for (int i = 0; i < args.length; i++)
 		{
 			args[i].genCode(context, false);
 		}
 		
 		
 		CClass callerClass = AdditionalGenerationContext.instance().getCurrentClass();
 		
 		// IVICA: when setting target for method calls owned by 
 		// a class != Object do following:
 		// - use direct super as target for super calls 
 		// - use caller class as target for virtual calls
 		// (this is the way javac would do it to)
 
 		if(!method.getOwner().isObjectClass()) {
 			if(prefix instanceof JSuperExpression) {
 			    method.genCode(
 			        context, 
 			        true, 
 			        callerClass.getSuperClass());		    		   
 			}	
 			else {
 			    CClass prefixTarget = null;
 			    
 			    // callerClass.descendsFrom(method.getOwner()
 				if(prefix instanceof JOwnerExpression) {
 				    prefixTarget = callerClass;
 				}
 				else if(prefix == null && callerClass.descendsFrom(method.getOwner())) {
 				    // this check is necessary, since we could have an inner class with prefix = null
 				    // accessing methods defined in the enclosing class
 				    prefixTarget = callerClass;
 				}
 				else if(prefix != null) {
 				    prefixTarget = prefix.getType(context.getTypeFactory()).getCClass();
 				}
 				else {
 				    prefixTarget = method.getOwner();
 				}
 				
 		        method.genCode(
 		            context, 
 		            false, 
 		            prefixTarget);		    		   		    
 			}
 		}
 		else {
 		    method.genCode(
 	            context, 
 	            prefix instanceof JSuperExpression, 
 	            method.getOwner());	
 		}
 		
 
 		if (discardValue) {
 			code.plantPopInstruction(getType(factory));
 		}
 	}
 
 	public void recurse(IVisitor s) {
 	    if(prefix != null)
 	        prefix.accept(s);
         for (int i = 0; i < args.length; i++) {
             args[i].accept(s);
         }
     }
 
 	public CType getPrefixType() {
         return prefixType;
     }
 	
 	public JExpression getPrefix(){
 	    return prefix;
 	}
 	
 	/*
 	 * TODO Check if this is called outside of Path
 	 */
 	public String getIdent(){
 	    return ident;
 	}
 
 	public String toString() {
 	    return "JMethodCallExpression["+ident+"] "+super.toString();
 	}
 	
 	// ----------------------------------------------------------------------
 	// DATA MEMBERS
 	// ----------------------------------------------------------------------
 
 	protected Path argPaths[];
 	protected Path argTypePaths[];
 	protected JExpression prefix;
 	protected String ident;
 	protected JExpression[] args;
 	private boolean analysed;
 	protected CMethod method;
 	protected CType type;
 	protected CType prefixType;
     
 }
