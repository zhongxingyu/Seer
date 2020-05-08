 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright � 2003-2005 
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
 * $Id: CjVirtualClassDeclaration.java,v 1.37 2006-01-23 11:33:29 gasiunas Exp $
  */
 
 package org.caesarj.compiler.ast.phylum.declaration;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import org.caesarj.compiler.aspectj.CaesarDeclare;
 import org.caesarj.compiler.ast.JavaStyleComment;
 import org.caesarj.compiler.ast.JavadocComment;
 import org.caesarj.compiler.ast.phylum.JPhylum;
 import org.caesarj.compiler.ast.phylum.variable.JFormalParameter;
 import org.caesarj.compiler.ast.visitor.IVisitor;
 import org.caesarj.compiler.constants.CaesarConstants;
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.context.CCompilationUnitContext;
 import org.caesarj.compiler.context.CContext;
 import org.caesarj.compiler.export.CCjSourceClass;
 import org.caesarj.compiler.export.CClass;
 import org.caesarj.compiler.export.CCompilationUnit;
 import org.caesarj.compiler.export.CMethod;
 import org.caesarj.compiler.export.CModifier;
 import org.caesarj.compiler.export.CSourceClass;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.typesys.CaesarTypeSystem;
 import org.caesarj.compiler.typesys.graph.CaesarTypeNode;
 import org.caesarj.compiler.typesys.java.JavaQualifiedName;
 import org.caesarj.compiler.typesys.java.JavaTypeNode;
 import org.caesarj.util.InconsistencyException;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 
 /**
  * This class represents a cclass in the syntax tree.  
  */
 public class CjVirtualClassDeclaration extends CjClassDeclaration {       
 
     public CjVirtualClassDeclaration(
         TokenReference where,
         int modifiers,
         String ident,
         CReferenceType[] superClasses,
         CReferenceType wrappee,
         CReferenceType[] interfaces,
         JFieldDeclaration[] fields,
         JMethodDeclaration[] methods,
         JTypeDeclaration[] inners,
         JPhylum[] initializers,
         JavadocComment javadoc,
         JavaStyleComment[] comment,
         CjPointcutDeclaration[] pointcuts,
         CjAdviceDeclaration[] advices,
         CaesarDeclare[] declares,
 		boolean implClass) {
         super(
             where,
             modifiers | ACC_MIXIN,
             implClass ? ident+"_Impl" : ident,
             null,
             wrappee,
             interfaces,
             fields,
             methods,
             inners,
             initializers,
             javadoc,
             comment,
             pointcuts,
             advices,
             declares);
               
         
         this.superClasses = superClasses;
         
         if(implClass) {                    
             originalIdent = ident;
             
             //rename constructors
             for(int i=0; i<this.methods.length; i++) {
                 JMethodDeclaration method = this.methods[i];
                 if(method instanceof JConstructorDeclaration) {
                     method.ident = (method.ident + "_Impl").intern();
                 }
             }
         }        
     }
 
   
     protected String originalIdent;
 
     public String getOriginalIdent() {
         return originalIdent;
     }
 
     protected CSourceClass createSourceClass(CClass owner, CCompilationUnit cunit, String prefix) {
         return new CCjSourceClass(
             owner,
             getTokenReference(),
             modifiers,
             ident,
             prefix + ident,
             isDeprecated(),
             false,
 			true,
 			cunit,
             this,
             perClause);
     }
     
     /**
      * Create new empty inner type with given name and join it.
      * 
      * @param context		- context for type resolving
      * @param ident			- name of the class
      * @param bAbstract		- is class abstract
      * @return
      * @throws PositionedError
      */
     public CjVirtualClassDeclaration createInnerCaesarType(
     		CContext context, String ident, boolean bAbstract) 
     	throws PositionedError {
     	// generate here
     	CjMixinInterfaceDeclaration ifcDecl = 
             new CjMixinInterfaceDeclaration(
                 getTokenReference(),
                 ACC_PUBLIC,
                 ident,
                 CReferenceType.EMPTY,
                 CReferenceType.EMPTY,
                 new JFieldDeclaration[0],
                 new JMethodDeclaration[0],
                 new JTypeDeclaration[0],
                 new JPhylum[0]
             ); 
         
         getMixinIfcDeclaration().addInners(new JTypeDeclaration[] {ifcDecl });
         
         /* determine if the class is abstract */
         int abstractModifier = bAbstract ? ACC_ABSTRACT : 0;
         
         CjVirtualClassDeclaration implDecl =
             new CjVirtualClassDeclaration(
                 getTokenReference(),
                 ACC_PUBLIC | abstractModifier,
                 ident + "_Impl",
                 CReferenceType.EMPTY,
                 null, // wrappee
                 new CReferenceType[]{ifcDecl.getSourceClass().getAbstractType()}, // CTODO ifcs
                 new JFieldDeclaration[0],
 				new JMethodDeclaration[0],
                 new JTypeDeclaration[0],
                 new JPhylum[0], null, null, 
                 CjPointcutDeclaration.EMPTY,
                 CjAdviceDeclaration.EMPTY,
                 new CaesarDeclare[0],
                 false
             );
         
         // add inners
         addInners(new JTypeDeclaration[] {implDecl });
         
         implDecl.getSourceClass().close(
             implDecl.getInterfaces(),
             new Hashtable(),
             CMethod.EMPTY
         );
         
         implDecl.setMixinIfcDeclaration(ifcDecl);
         ifcDecl.setCorrespondingClassDeclaration(implDecl);
         
         implDecl.getSourceClass().setImplicit(true);
         ifcDecl.getSourceClass().setImplicit(true);
         
         return implDecl;
     }
     
     /**
      * this one generates recursively implicit inner types 
      */
     public void createImplicitCaesarTypes(CContext context) throws PositionedError {
         JavaQualifiedName qualifiedName =
             new JavaQualifiedName(
                 getMixinIfcDeclaration().getSourceClass().getQualifiedName()
             );
         
         CaesarTypeSystem typeSystem = context.getEnvironment().getCaesarTypeSystem();
 
         CaesarTypeNode typeNode = typeSystem.getCaesarTypeGraph().getType(qualifiedName);
         JavaTypeNode javaTypeNode = typeSystem.getJavaTypeGraph().getJavaTypeNode(typeNode);
         
         javaTypeNode.setCClass(getSourceClass());
         javaTypeNode.setDeclaration(this);
         
         // recurse in original inners
         for(int i=0; i<inners.length; i++) {
             inners[i].createImplicitCaesarTypes(context);
         }
 
         /*
          * add implicit subtypes
          */ 
         for(CaesarTypeNode subNode : typeNode.implicitInners()) {
             CjVirtualClassDeclaration implDecl = 
             	createInnerCaesarType(context, subNode.getQualifiedName().getIdent(), subNode.isAbstract());
             
             // and recurse into
             implDecl.createImplicitCaesarTypes(context);
         }        
     }   
     
     /**
      * ajdusts super types of the class
      * remark: in cclass preparation step we've set 
      * interface list to EMPTY and super type to null
      */
     public void adjustSuperType(CContext context) throws PositionedError {
         try {
             JavaQualifiedName qualifiedName =
                 new JavaQualifiedName(
                     getMixinIfcDeclaration().getSourceClass().getQualifiedName()
                 );
             
             CaesarTypeSystem typeSystem = context.getEnvironment().getCaesarTypeSystem();
 
             CaesarTypeNode typeNode = typeSystem.getCaesarTypeGraph().getType(qualifiedName);
             JavaTypeNode javaTypeNode = typeSystem.getJavaTypeGraph().getJavaTypeNode(typeNode);
             
             
             /*
              * adjust supertype 
              */
             
             JavaTypeNode superType = javaTypeNode.getParent();
             
             CReferenceType superTypeRef = 
                 context.getTypeFactory().createType(superType.getQualifiedName().toString(), true);
             
             superTypeRef = (CReferenceType)superTypeRef.checkType(context);
             
             CReferenceType ifcs[] =
                 new CReferenceType[]{
                     getMixinIfcDeclaration().getSourceClass().getAbstractType()
                 };
             
             setInterfaces(ifcs);            
             setSuperClass(superTypeRef);
             
             getSourceClass().setSuperClass(superTypeRef);            
             getSourceClass().setInterfaces(ifcs);
             
             for(int i=0; i<inners.length; i++) {
                 inners[i].adjustSuperType(context);
             }
         }
         catch (Throwable e) {
             // MSG
             e.printStackTrace();
             throw new PositionedError(getTokenReference(), CaesarMessages.CANNOT_CREATE);
         }
     }   
     
         
     /**
      * - check that cclass modifier is always set to public
      */
     public void join(CContext context) throws PositionedError {
 
         if(!CModifier.contains(ACC_PUBLIC, modifiers)) {
 	        context.reportTrouble(
 	            new PositionedError(
 	                getTokenReference(),
 	                CaesarMessages.ONLY_PUBLIC_CCLASS
                 )
             );
         }
              
         super.join(context);        
     }
     
     /**
      * Resolves the binding and providing references. Of course it calls the
      * super implementation of the method also.
      */
     public void checkInterface(CContext context) throws PositionedError {
     	// if an inner class, set to static
     	// we are manually managing the outer references
         if(getSourceClass().isNested()) {
             this.modifiers |= ACC_STATIC;
             getSourceClass().setModifiers(this.modifiers);
         }
         
         checkAbstractInners(context);    	
         
     	super.checkInterface(context);
     	
 		// CTODO: check inheritance of full throwable list on method redefinition
     }
     
     /**
      * Checks if concrete virtual classes do not override abstract ones
      */
     protected void checkAbstractInners(CContext context) throws PositionedError {
     	JavaQualifiedName qualifiedName =
             new JavaQualifiedName(
                 getMixinIfcDeclaration().getSourceClass().getQualifiedName()
             );
     	
     	CaesarTypeSystem typeSystem = context.getEnvironment().getCaesarTypeSystem();
         CaesarTypeNode typeNode = typeSystem.getCaesarTypeGraph().getType(qualifiedName);
     	
     	for(CaesarTypeNode subNode : typeNode.declaredInners()) {
             
             if (subNode.isAbstract()) {
             	for (CaesarTypeNode fb : subNode.directFurtherbounds()) {
             		if (!fb.isAbstract()) {
             			throw new PositionedError(
             				subNode.getTypeDecl().getTokenReference(),
         					CaesarMessages.ABSTRACT_CANNOT_OVERRIDE_CONCRETE
         				);                		            		
                 	}
             	}
             }                        
         }
     }
         
     // check method overriding in the context of virtual classes
     public void checkVirtualClassMethodSignatures(CCompilationUnitContext context) throws PositionedError {
     	for (int i = 0; i < methods.length; i++) {    	        	    
             JMethodDeclaration methodDecl = methods[i];
             CMethod method = methodDecl.getMethod();
             
             if(method.isConstructor())
                 continue;
             
             // find initial declaration of the method
             CMethod initialMethodDecl = findInitialDeclaration(method, context);
             
             // set parameter list equal to the inital method declaration 
             if(!initialMethodDecl.equals(method)) {
 	            // check return type and parameters
 	            if(
 	                method.getReturnType().isClassType() 
 	                && method.getReturnType().getCClass().isMixinInterface()
 	            ) {
 	                
                 	if(!context.getEnvironment().getCaesarTypeSystem().
 	                	isIncrementOf(
 	                	    method.getReturnType().getCClass().getQualifiedName(),
 	                	    initialMethodDecl.getReturnType().getCClass().getQualifiedName()
 	                    )
 	                ) {
                 	    // different return types and the lower is not increment of the upper
                 	    // no modification, just continue
                 	    // checkAllBodies will throw an exception
                 	    continue;
                 	}
                 	
 	            }
 	            
 	            
 	            // update formal parameter list
 	            JFormalParameter origFormalParams[] = methodDecl.getParameters();
 	            JFormalParameter[] newFormalParams = new JFormalParameter[origFormalParams.length];
 	            
 	            for(int p = 0; p < newFormalParams.length; p++) {
 	                newFormalParams[p] = new JFormalParameter(
 	                    origFormalParams[p].getTokenReference(),
 	                    origFormalParams[p].getDescription(),
 	                    initialMethodDecl.getParameters()[p],
 	                    origFormalParams[p].getIdent(),
 	                    origFormalParams[p].isFinal()
                     );
                 }
 	            
 	            // update declaration
 	            methodDecl.setReturnType(initialMethodDecl.getReturnType());
 	            methodDecl.setParameters(newFormalParams);
 	            
 	            // update type information
 	            method.setReturnType(initialMethodDecl.getReturnType());
 	            method.setParameters(initialMethodDecl.getParameters());
             }
         }    	    
     	
     	// recurse into inners
     	for (int i = 0; i < inners.length; i++) {
             inners[i].checkVirtualClassMethodSignatures(context);
         }
     }    
 
     
     private CMethod findInitialDeclaration(CMethod orig, CContext context) {
         CClass ownerSuperClass = orig.getOwner().getSuperClass();
         CMethod lastFound = orig;
         
         while(ownerSuperClass != null) {
             
             CMethod[] methods = ownerSuperClass.getMethods();
             
             for (int i = 0; i < methods.length; i++) {
                 
                 CMethod other = methods[i];
                 
                 if(other.isConstructor())
                     continue;
                 
                 boolean equals = true;
                 
                 if(
                     !orig.getIdent().equals(other.getIdent())
                     || orig.getParameters().length != other.getParameters().length
                 ) {
                     equals = false;
                 }
 
                 // check params
                 for (int j = 0; j < orig.getParameters().length && equals; j++) {
                     if (!orig.getParameters()[j].equals(other.getParameters()[j])) {
                         // check if the params are furtherbindings
                         
                        if (orig.getParameters()[j].isClassType() &&
                        		other.getParameters()[j].isClassType()) {                        
 	                        if(
 	                            !context.getEnvironment().getCaesarTypeSystem().
 	                            	isIncrementOf(
 	                            	    orig.getParameters()[j].getCClass().getQualifiedName(),
 	                            	    other.getParameters()[j].getCClass().getQualifiedName()
 	                                )
 	                        ) {
 	                            equals = false;	                            
 	                        }
                         }
                         else {
                             equals = false;
                         }
                     }
                 }
                                     
                 if(equals) {   
                     lastFound = methods[i];
                 }
             }
             
             ownerSuperClass = ownerSuperClass.getSuperClass();
         }
         
         return lastFound;
     }
     
     
     protected int getAllowedModifiers() {
         return super.getAllowedModifiers() | ACC_MIXIN;
     }
         
     /**
      * reference to corresponding mixin interface
      */     
     public void setMixinIfcDeclaration(CjMixinInterfaceDeclaration caesarInterfaceDeclaration)  {
         this.mixinIfcDecl = caesarInterfaceDeclaration;
     }
     
     public CjMixinInterfaceDeclaration getMixinIfcDeclaration() {
     	if(mixinIfcDecl == null)
     		throw new InconsistencyException("mixin interface has not been assigned to this virtual class");
         return mixinIfcDecl;
     }
     
     public CReferenceType[] getSuperClasses() {
         return superClasses;
     }
     
     public void setAspectInterface(CjInterfaceDeclaration aspectIfc)  {
         this.aspectIfc = aspectIfc;
     }
     
     public void setRegistryClass(CjClassDeclaration registryCls)  {
         this.registryCls = registryCls;
     }
     
     public CjInterfaceDeclaration getAspectInterface()  {
         return aspectIfc;
     }
     
     public CjClassDeclaration getRegistryClass()  {
         return registryCls;
     }
     
     /** Return children for visitor traversal */
     public void recurse(IVisitor p) {
         super.recurse(p);
         
         // iterate over original advices
         for (int i1 = 0; i1 < origAdvices.length; i1++) {
 			origAdvices[i1].accept(p);
         }
 		
         // iterate over original pointcuts
 		for (int i1 = 0; i1 < origPointcuts.length; i1++) {
 			origPointcuts[i1].accept(p);
         }
     }
     
     /**
      * Get the declared pointcuts in the registry and declare them in the
      * CClass. When resolving the pointcut, the weaver will look in the CClass
      * and not directly here.
      *
      */
     public void declarePointcuts() {
         // Add the declared pointcuts to the source class
         CjClassDeclaration registry = this.getRegistryClass();
         if (registry != null) {
 
             CCjSourceClass registryCrosscuttingClass = (CCjSourceClass) registry.getSourceClass();
             CjPointcutDeclaration[] pointcuts = registry.getPointcuts();
 	        for (int j = 0; j < pointcuts.length; j++) {
 	            registryCrosscuttingClass.addDeclaredPointcut(this, pointcuts[j]);
 	        }
         }
     }
     
     /**
      * Delete pointcuts from the class, but make their backup,
      * because it it is needed to build crosscutting view
      */
     public void deactivatePointcuts() {
     	origPointcuts = pointcuts;
     	pointcuts = new CjPointcutDeclaration[0];    	    	
     }
     
     /**
      * Delete advices from the class, but make their backup,
      * because it it is needed to build crosscutting view
      */
     public void deactivateAdvices() {
     	origAdvices = advices;
     	for (int i1 = 0; i1 < origAdvices.length; i1++) {
     		origAdvices[i1].deactivate();
     	}
     	advices = new CjAdviceDeclaration[0];    	    	
     }
 
     /**
      * Get original advices as they were declared
      * in the source code
      */
     public CjAdviceDeclaration[] getOriginalAdvices() {
     	return origAdvices;
     }
     
     /**
      * Get original pointcuts as they were declared
      * in the source code
      */
     public CjPointcutDeclaration[] getOriginalPointcuts() {
     	return origPointcuts;
     }
     
     /**
      * Check if the class implements PerThisDeployableInterface
      */
     public boolean isPerThisDeployable() {
     	CReferenceType type = getContext().getTypeFactory().createType(
                 CaesarConstants.PER_THIS_DEPLOYABLE_IFC, true);
     	return this.getMixinIfcDeclaration().getSourceClass().descendsFrom(type.getCClass()); 
     }
         
     private CjMixinInterfaceDeclaration mixinIfcDecl = null;
     
     private CReferenceType[] superClasses;
     
     /* generated deployment support classes (can be null) */
     private CjInterfaceDeclaration aspectIfc = null;
 	
 	private CjClassDeclaration registryCls = null;
 	
 	/** The originally declared advices */
     protected CjAdviceDeclaration[] origAdvices = new CjAdviceDeclaration[0];
     
     /** The originally declared pointcuts */
     protected CjPointcutDeclaration[] origPointcuts = new CjPointcutDeclaration[0];
 }
