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
 * $Id: JFieldAccessExpression.java,v 1.31 2005-11-10 14:35:22 gasiunas Exp $
  */
 
 package org.caesarj.compiler.ast.phylum.expression;
 
 import org.caesarj.compiler.ClassReader;
 import org.caesarj.compiler.ast.phylum.expression.literal.JLiteral;
 import org.caesarj.compiler.ast.visitor.IVisitor;
 import org.caesarj.compiler.cclass.CastUtils;
 import org.caesarj.compiler.codegen.CodeLabel;
 import org.caesarj.compiler.codegen.CodeSequence;
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.context.CConstructorContext;
 import org.caesarj.compiler.context.CExpressionContext;
 import org.caesarj.compiler.context.CInitializerContext;
 import org.caesarj.compiler.context.CVariableInfo;
 import org.caesarj.compiler.context.GenerationContext;
 import org.caesarj.compiler.export.CCjPrivilegedField;
 import org.caesarj.compiler.export.CClass;
 import org.caesarj.compiler.export.CField;
 import org.caesarj.compiler.export.CMethod;
 import org.caesarj.compiler.export.CSourceClass;
 import org.caesarj.compiler.export.CSourceField;
 import org.caesarj.compiler.family.ContextExpression;
 import org.caesarj.compiler.family.FieldAccess;
 import org.caesarj.compiler.family.Path;
 import org.caesarj.compiler.types.CClassNameType;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.CWarning;
 import org.caesarj.util.InconsistencyException;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 
 /**
  * JLS 15.11 Field Access Expression.
  *
  * A field access expression may access a field of an object or array.
  */
 public class JFieldAccessExpression extends JExpression {
 
   // ----------------------------------------------------------------------
   // CONSTRUCTORS
   // ----------------------------------------------------------------------
 
   /**
    * Construct a node in the parsing tree
    *
    * @param	where		the line of this node in the source code
    * @param	prefix		the prefix denoting the object to search
    * @param	ident		the simple name of the field
    */
   public JFieldAccessExpression(TokenReference where,
                                 JExpression prefix,
                                 String ident)
   {
     super(where);
 
     this.prefix = prefix;
     this.ident = ident;
     this.constantPrefix = false;
     this.accessor = null;
     analysed = false;
   }
 
 
   /**
    * Construct a node in the parsing tree
    * @param	where		the line of this node in the source code
    * @param	ident		the simple name of the field
    */
   public JFieldAccessExpression(TokenReference where, String ident) {
     this(where, null, ident);
   }
 
   /**
    * Construct a node which not need to be analysed. The prefix should 
    * not require to be analysed.
    *
    * @param	where		the line of this node in the source code
    * @param	prefix		the prefix denoting the object to search
    * @param	field		field to access
    */
   public JFieldAccessExpression(TokenReference where,
 			       JExpression prefix,
 			       CField field)
   {
     super(where);
 
     this.prefix = prefix;
     this.ident = field.getIdent();
     this.constantPrefix = false;
     this.accessor = null;
     this.field = field;
     this.type = field.getType();
     analysed = true;
   }
   // ----------------------------------------------------------------------
   // ACCESSORS
   // ----------------------------------------------------------------------
 
   /**
    * Returns the simple name of the field.
    */
   public String getIdent() {
     return ident;
   }
   
   public JExpression getPrefix() {
       return prefix;
   }
 
   /**
    * Returns the type of the expression.
    */
   public CType getType(TypeFactory factory) {
     return type;
   }
 
   /**
    * Tests whether this expression denotes a compile-time constant (JLS 15.28).
    *
    * @return	true iff this expression is constant
    */
   public boolean isConstant() {
     // A compile-time constant expression is an expression [...]
     // that is composed using only the following :
     // - Simple names that refer to final variables whose initializers
     //   are constant expressions
     // - Qualified names of the form TypeName . Identifier that refer to
     //   final variables whose initializers are constant expressions
 
     return constantPrefix
       && field.isFinal()
       && field.getValue() != null
       && field.getValue().isConstant();
   }
 
   /**
    * Returns true if this field accepts assignments.
    */
   public boolean isLValue(CExpressionContext context) {
     if (!field.isFinal()|| field.isSynthetic()) {
       return true;
     } else if (context.getClassContext().getCClass() == field.getOwner()
 	       && !((CSourceField)field).isFullyDeclared()) {
       return !CVariableInfo.mayBeInitialized(context.getBodyContext().getFieldInfo(((CSourceField)field).getPosition()));
     } else {
       return false; 
     }
   }
 
   /**
    * Returns true if there must be exactly one initialization of the field. 
    *
    * @return true if the field is final.
    */
   public boolean isFinal() {
     return field.isFinal();
   }
 
   /**
    * Returns true iff this field is already initialized.
    */
   public boolean isInitialized(CExpressionContext context) {
     if (!(field instanceof CSourceField) || field.isStatic()) {
       return true;
     } else if (context.getClassContext().getCClass() == field.getOwner() &&
 	       !((CSourceField)field).isFullyDeclared()) {
       return CVariableInfo.isInitialized(context.getBodyContext().getFieldInfo(((CSourceField)field).getPosition()));
     } else {
       return true;
     }
   }
 
   /**
    * Declares this variable to be initialized.
    *
    * @exception	UnpositionedError an error if this object can't actually
    *		be assignated this may happen with final variables.
    */
   public void setInitialized(CExpressionContext context) {
     if ((field instanceof CSourceField) 
         && (context.getClassContext().getCClass() == field.getOwner()) 
         && (!((CSourceField)field).isFullyDeclared()) || field.isFinal()) {
       context.setFieldInfo(((CSourceField)field).getPosition(), CVariableInfo.INITIALIZED);
     }
   }
 
   /**
    * Returns the exported field.
    */
   public CField getField() {
     return field;
   }
 
   /**
    * Returns the literal value of this field.
    */
   public JLiteral getLiteral() {
     return (JLiteral)field.getValue();
   }
 
   /**
    * Returns a string representation of this expression.
    */
   public String toString() {
     StringBuffer	buffer = new StringBuffer();
 
     buffer.append("JFieldAccessExpression[");
     buffer.append(prefix);
     buffer.append(", ");
     buffer.append(ident);
     if (isConstant()) {
       buffer.append(" = ");
       buffer.append(field);
     }
     buffer.append("]");
     return buffer.toString();
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
   public JExpression analyse(CExpressionContext context) throws PositionedError {
     TypeFactory         factory = context.getTypeFactory();
 
     if (analysed) {
       return this;
     }
 
     JExpression origPrefix = prefix;
 
     CClass	local = context.getClassContext().getCClass();
 
     //findPrefix(local, new CExpressionContext(context, context.getEnvironment(), false, false));
     // the method body inserted here
     {
 	    if (prefix != null) {
 	        prefix = prefix.analyse(context);
 	        check(
 	            context,
 	            prefix.getType(factory).isClassType(),
 	            KjcMessages.FIELD_BADACCESS,
 	            prefix.getType(factory));
 	        constantPrefix = prefix instanceof JTypeNameExpression;
 	    }
 	    else {
 	        constantPrefix = true;
 	
 	        try {
 	            field = context.lookupField(
 	                local,
 	                null /* primary == null */,
 	                ident);
 	            if (field == null) {
 	                field = context.getClassContext().lookupOuterField(
 	                    local,
 	                    null,
 	                    ident); // $$$ Why searching again
 	            }
 	        }
 	        catch (UnpositionedError e) {
 	            throw e.addPosition(getTokenReference());
 	        }
 	        
 	        check(context, field != null, KjcMessages.FIELD_UNKNOWN, ident);
 	
 	        if( local.isMixin() ) {
 	        	if (field.isStatic()) {
 	        		prefix = 
                         new JTypeNameExpression(
                             getTokenReference(),
                             new CClassNameType(field.getOwnerType().getQualifiedName())
                         );
 	        	}
 	        	else if(local.getDepth() == field.getOwner().getDepth()) {
 	              	//IVICA: use "this" as target for the field calls within a cclass
 	                prefix = new JOwnerExpression(getTokenReference(), local);	                
 	            }
 	            else {
 	                // return accessor method for this field access
 	                // check it is not an write access
 	                check(context, !context.isLeftSide(), CaesarMessages.READ_ONLY_ACCESS_TO_CCLASS_FIELDS);
 	                
 	                return new CjAccessorCallExpression(
 	                			getTokenReference(),
 								prefix,
 								field
 	        	       		).analyse(context);
 	            }
 	        }
 	        else {
 	            if (!field.isStatic()) {
 		            check(
 		                context,
 		                !local.isStatic() || local.descendsFrom(field.getOwner()),
 		                KjcMessages.FIELD_STATICERR,
 		                ident
 	                );		            
 		            
		            if (context.getClassContext().getCClass().isMixin()) {
 	                	prefix = new JOwnerExpression(getTokenReference(), context.getClassContext().getCClass());
 	                }
 	                else {
 	                	prefix = new JOwnerExpression(getTokenReference(), field.getOwner());
 	                }
 		        }
 		        else {
 		            prefix = 
 		                new JTypeNameExpression(
 		                    getTokenReference(), 
 		                    new CClassNameType(field.getOwnerType().getQualifiedName())
 	                    );
 		        }
 	        }
 	        
 	        
 	        prefix = prefix.analyse(context);
 	    }
     }              
     
     // IVICA: if we have a cclass, and want to acces the public field, map to accessor method
     CType prefixType = prefix.getType(factory); 
     if(prefixType.getCClass().isMixinInterface() && prefixType.getCClass()!=context.getClassContext().getCClass()) {
         
         // check it is not an write access
         check(context, !context.isLeftSide(), CaesarMessages.READ_ONLY_ACCESS_TO_CCLASS_FIELDS);        
         
         checkAccess(local, context, true);
         
         return
         	new CjAccessorCallExpression(
         	    getTokenReference(),
         	    prefix,
         	    field
         	).analyse(context);        
     }
     // --- end ---      
 
     checkAccess(local, context, false);
     
 	if (field instanceof CCjPrivilegedField) {
 		CCjPrivilegedField privField = (CCjPrivilegedField) field;
 
 		JExpression[] args = { prefix };
 		JMethodCallExpression methodCallExpr =
 			new JMethodCallExpression(
 				getTokenReference(),
 				prefix,
 				privField.getAccessMethod(!context.isLeftSide()),
 				args);
 
 		return methodCallExpr;
 
 	}
     
 
     if (context.getMethodContext() instanceof CInitializerContext
         || context.getMethodContext() instanceof CConstructorContext) {
       // !!! 011109 graf : all cases caught ? see jacks4kopi tests Kjc65,66,67
      check(context,
            context.isLeftSide() 
            || !field.isFinal()
            || field.isSynthetic()
            || field.getOwner() != local
            || !(prefix instanceof JThisExpression)
            || (context.getMethodContext() instanceof CConstructorContext && field.isStatic())
            || !(field instanceof CSourceField)
            || CVariableInfo.isInitialized(context.getBodyContext().getFieldInfo(((CSourceField)field).getPosition())),
            KjcMessages.UNINITIALIZED_FIELD_USED, 
            field.getIdent());
 
      check(context,
            context.isLeftSide()
            || context.getMethodContext() instanceof CConstructorContext 
            || field.isSynthetic()
            || field.getOwner() != local
            || ((origPrefix != null) && !(origPrefix instanceof JThisExpression))
            || !(field instanceof CSourceField)
            || field.isAnalysed(),
            KjcMessages.USE_BEFORE_DEF, field.getIdent());
     }
 
     check(context,
 	  field.isStatic() || !(prefix instanceof JTypeNameExpression),
 	  KjcMessages.FIELD_NOSTATIC, ident);
     if (field.isStatic() && !(prefix instanceof JTypeNameExpression)) {
       context.reportTrouble(new CWarning(getTokenReference(),
 					 KjcMessages.INSTANCE_PREFIXES_STATIC_FIELD,
 					 ident,
 					 prefix.getType(factory)));
     }
 
     if (field instanceof CSourceField && !context.discardValue()) {
       ((CSourceField)field).setUsed();
     }
 
     type = field.getType();
     calcFamilyType();
     
 
     if (isConstant()) {
       // FIX Type!!
       return field.getValue();
     } else if (field.requiresAccessor(local, prefix instanceof JSuperExpression)) {
       // FIX Type!!
 
       CSourceClass      target = field.getAccessorOwner((CSourceClass)local);
 
       if (context.isLeftSide()) {
         accessor = null;
         accessorData = new SetAccessorData();
         accessorData.target = target;
         accessorData.isSuper = prefix instanceof JSuperExpression;
         accessorData.typeFactory = context.getTypeFactory();
         return this;
       } else {
         CMethod         method = field.getAccessor(context.getTypeFactory(), 
                                                    target, 
                                                    false,
                                                    prefix instanceof JSuperExpression, 
                                                    -1);
         if (prefix instanceof JSuperExpression) {
           prefix = new JFieldAccessExpression(getTokenReference(), 
                                               new JThisExpression(getTokenReference()), 
                                               local.getField(JAV_OUTER_THIS));
         }
         JMethodCallExpression     accessor = 
           new JMethodCallExpression(getTokenReference(),
                                     null,
                                     method,
                                     (field.isStatic()) ? 
                                       JExpression.EMPTY : 
                                       new JExpression[] {prefix});
         accessor.analyse(context);
         return accessor;
       }
     } 
     else { 
       // IVICA: insert cast
         if(!context.isLeftSide()) {
 	      CType castType = 
 	          CastUtils.instance().castFrom(
 	              context, field.getType(), context.getClassContext().getCClass());
 	      
 	      if(castType != null) {
 	          return new CjCastExpression(
 	              getTokenReference(),
 	              this,
 	              castType
 	          );
 	      }
         }
       
       return this;
     }
   }
   
   protected void calcFamilyType() throws PositionedError {
   	 //IVICA: calc family type
     try {
         // store family here
         if(getIdent().equals(JAV_OUTER_THIS)) {
             thisAsFamily = prefix.getThisAsFamily().clonePath(); 
             ((ContextExpression)thisAsFamily.getHead()).adaptK(+1);
             
             family = thisAsFamily.clonePath();
 		    ((ContextExpression)family.getHead()).adaptK(+1);
         }
         else {
 	        Path prefixFam = prefix.getThisAsFamily();	        	       
 	        
 	        if (type.isReference() && !type.isArrayType()) {
 	            Path p = new FieldAccess(field.isFinal(), prefixFam.clonePath(), field.getIdent(), (CReferenceType)type);
 	            family = p.normalize();
 	            
 	            thisAsFamily = p;	            
 	        }
         }
     }
     catch (UnpositionedError e) {
         throw e.addPosition(getTokenReference());
     }
   }
 
   /**
    * Returns true iff this expression require an accessor. (e.g. Setting in an inner 
    * class the value of a private field of the enclosing class)
    */
   public boolean requiresAccessor() {
     return accessor != null || accessorData != null;
   }
 
   /**
    * Returns an accessor which can be used to set this member. 
    * @param expr the value used to set the member
    */
   public JExpression getAccessor(JExpression[] expr, int oper) {
     JExpression[]           args;
 
     if (getField().isStatic()) {
       args = expr;
     } else {
       args = new JExpression[expr.length+1];
       System.arraycopy(expr, 0, args, 1, expr.length);
       args[0] = prefix;
     }
 
     accessor = field.getAccessor(accessorData.typeFactory, 
                                  accessorData.target, 
                                  true,
                                  accessorData.isSuper, 
                                  oper);
 
     JMethodCallExpression   setMethod = 
       new JMethodCallExpression(getTokenReference(),
                                 new JTypeNameExpression(getTokenReference(),
                                                         field.getOwner().getAbstractType()),
                                 accessor.getIdent(),
                                 args);
     return setMethod;
   }
   /**
    * Finds the type of the prefix.
    * 
    * @exception PositionedError
    *                Error catched as soon as possible
    */
 //    protected void findPrefix(CClass local, CExpressionContext context)
 //        throws PositionedError {
 //        TypeFactory factory = context.getTypeFactory();
 //
 //        if (prefix != null) {
 //            prefix = prefix.analyse(context);
 //            check(
 //                context,
 //                prefix.getType(factory).isClassType(),
 //                KjcMessages.FIELD_BADACCESS,
 //                prefix.getType(factory));
 //            constantPrefix = prefix instanceof JTypeNameExpression;
 //        }
 //        else {
 //            constantPrefix = true;
 //
 //            try {
 //                field = context.lookupField(
 //                    local,
 //                    null /* primary == null */,
 //                    ident);
 //                if (field == null) {
 //                    field = context.getClassContext().lookupOuterField(
 //                        local,
 //                        null,
 //                        ident); // $$$ Why searching again
 //                }
 //            }
 //            catch (UnpositionedError e) {
 //                throw e.addPosition(getTokenReference());
 //            }
 //            
 //            check(context, field != null, KjcMessages.FIELD_UNKNOWN, ident);
 //
 //            if (!field.isStatic()) {
 //                check(
 //                    context,
 //                    !local.isStatic() || local.descendsFrom(field.getOwner()),
 //                    KjcMessages.FIELD_STATICERR,
 //                    ident);
 //                
 //                //prefix = new JOwnerExpression(getTokenReference(), field.getOwner());
 //                //IVICA: use "this" as target for the field calls within a cclass                
 //                prefix = context.getClassContext().getCClass().isMixin() ?
 //                    new JOwnerExpression(getTokenReference(), context.getClassContext().getCClass())
 //                    : new JOwnerExpression(getTokenReference(), field.getOwner());
 //                
 //            }
 //            else {
 //                prefix = new JTypeNameExpression(getTokenReference(), field.getOwnerType());
 //            }
 //            
 //            prefix = prefix.analyse(context);
 //        }
 //    }
 
   /**
    * Checks is access to prefix is okay
    *
    * @exception	PositionedError	Error catched as soon as possible
    */
   protected void checkAccess(CClass local, CExpressionContext context, boolean checkInMixin) throws PositionedError {
     TypeFactory         factory = context.getTypeFactory();
 
     CClass	access = prefix.getType(factory).getCClass();
     if(checkInMixin) {
         ClassReader classReader = context.getClassReader();
         access = classReader.loadClass(factory, access.getImplQualifiedName());
     }
 
     try {
       // FIX lackner 25.11.01  used for xkjc (Main.java) prefix instanceof JSuperExpression
       field = access.lookupField(local, (prefix instanceof JSuperExpression || prefix instanceof JThisExpression) ? null : access, ident);
     } catch (UnpositionedError e) {
       throw e.addPosition(getTokenReference());
     }
     check(context, field != null, KjcMessages.FIELD_UNKNOWN, ident);
 
     //!!!DEBUG 000213 graf (see CReferenceType.getCClass())
     try {
       field.setType(field.getType().checkType(context));
     } catch (UnpositionedError e) {
       throw e.addPosition(getTokenReference());
     }
     //!!!DEBUG 000213 graf
  
 
     if ((context.getMethodContext() instanceof CConstructorContext)
         && (prefix instanceof JThisExpression)
         && !field.isStatic()) {
       check(context,
             ((CConstructorContext)context.getMethodContext()).isSuperConstructorCalled(),
             KjcMessages.CONSTRUCTOR_EXPLICIT_CALL, field.getIdent());
     }
 
     if ((context.getMethodContext() instanceof CInitializerContext)
         && (local == field.getOwner())) {
       check(context,
             field.isAnalysed()
             || context.isLeftSide() 
             || field.isSynthetic() 
             || ((field instanceof CSourceField) 
                 && CVariableInfo.isInitialized(context.getBodyContext().getFieldInfo(((CSourceField)field).getPosition()))),
             KjcMessages.USE_BEFORE_DEF, field.getIdent());
 
     }
   }
 
   public boolean equals(Object o) {
     return (o instanceof JFieldAccessExpression) &&
       field.equals(((JFieldAccessExpression)o).field) &&
       prefix.equals(((JFieldAccessExpression)o).prefix);
   }
 
   // ----------------------------------------------------------------------
   // CONSTANT FIELD HANDLING
   // ----------------------------------------------------------------------
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public boolean booleanValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a boolean literal");
     } else {
       return field.getValue().booleanValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public byte byteValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a byte literal");
     } else {
       return field.getValue().byteValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public char charValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a char literal");
     } else {
       return field.getValue().charValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public double doubleValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a double literal");
     } else {
       return field.getValue().doubleValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public float floatValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a float literal");
     } else {
       return field.getValue().floatValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public int intValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not an int literal");
     } else {
       return field.getValue().intValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public long longValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a long literal");
     } else {
       return field.getValue().longValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public short shortValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a short literal");
     } else {
       return field.getValue().shortValue();
     }
   }
 
   /**
    * Returns the constant value of the expression.
    * The expression must be a literal.
    */
   public String stringValue() {
     if (!isConstant()) {
       throw new InconsistencyException(this + " is not a string literal");
     } else {
       return field.getValue().stringValue();
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
   public void genCode(GenerationContext context, boolean discardValue) {
     CodeSequence code = context.getCodeSequence();
 
     setLineNumber(code);
 
     String target = null;
     
     if (! field.isStatic()) {
       
         prefix.genCode(context, false);
         
         // IVICA: if we are in a cclass, use prefix type as target
         if(prefix.getType(context.getTypeFactory()).getCClass().isMixin())
             target = prefix.getType(context.getTypeFactory()).getCClass().getQualifiedName();
         
       if (discardValue) {
         /*
          * JLS 15.11.1 Field Access Using a Primary :
          * If the field is not static, and
          * If the value of the Primary is null, then
          * a NullPointerException is thrown.
          */
         CodeLabel     ok = new CodeLabel();
 
         code.plantJumpInstruction(opc_ifnonnull, ok);
         code.plantNoArgInstruction(opc_aconst_null);
         code.plantNoArgInstruction(opc_athrow);
         code.plantLabel(ok);
       }
     } else if (prefix != null) {
       prefix.genCode(context, true);
     }
     if (!discardValue) {
       field.genLoad(context, target);
     }
   }
 
   /**
    * Generates JVM bytecode to store a value into the storage location
    * denoted by this expression.
    *
    * Storing is done in 3 steps :
    * - prefix code for the storage location (may be empty),
    * - code to determine the value to store,
    * - suffix code for the storage location.
    *
    * @param	code		the code list
    */
   public void genStartStoreCode(GenerationContext context) {
     if (! field.isStatic()) {
       prefix.genCode(context, false);
     } else if (prefix != null) {
       prefix.genCode(context, true);
     }
   }
 
   /**
    * Generates JVM bytecode to for compound assignment, pre- and 
    * postfix expressions.
    *
    * @param	code		the code list
    */
   public void genStartAndLoadStoreCode(GenerationContext context, boolean discardValue) {
     CodeSequence code = context.getCodeSequence();
 
     genStartStoreCode(context);
 
     // prefix is witten with method genStartCode(...)
     if(! field.isStatic()) {
       code.plantNoArgInstruction(opc_dup);
     }
 
     if (!discardValue) {
       field.genLoad(context, null);
     }
   }
 
   /**
    * Generates JVM bytecode to store a value into the storage location
    * denoted by this expression.
    *
    * Storing is done in 3 steps :
    * - prefix code for the storage location (may be empty),
    * - code to determine the value to store,
    * - suffix code for the storage location.
    *
    * @param	code		the code list
    * @param	discardValue	discard the result of the evaluation ?
    */
   public void genEndStoreCode(GenerationContext context, boolean discardValue) {
     CodeSequence        code = context.getCodeSequence();
     TypeFactory         factory = context.getTypeFactory();
 
     if (!discardValue) {
       int	opcode;
 
       if (field.isStatic()) {
 	if (getType(factory).getSize() == 2) {
 	  opcode = opc_dup2;
 	} else {
 	  opcode = opc_dup;
 	}
       } else {
 	if (getType(factory).getSize() == 2) {
 	  opcode = opc_dup2_x1;
 	} else {
 	  opcode = opc_dup_x1;
 	}
       }
 
       code.plantNoArgInstruction(opcode);
     }
 
     field.genStore(context);
   }
  
   static class SetAccessorData{
     CSourceClass        target;
     TypeFactory         typeFactory;
     boolean             isSuper;
   }
 
   public void recurse(IVisitor s) {
     prefix.accept(s);
   }
   
   // ----------------------------------------------------------------------
   // DATA MEMBERS
   // ----------------------------------------------------------------------
 
   /*
    * Is the prefix null or a type name ? Needed for isConstant().
    */
   private boolean		constantPrefix;
   private boolean		analysed;
   private CType                 type;
   private boolean               startCode;
   
   protected JExpression		prefix;		// !!! graf 991205 make private
   protected String		ident;
   protected CField		field;
   protected CMethod             accessor;
   protected SetAccessorData     accessorData;
 }
