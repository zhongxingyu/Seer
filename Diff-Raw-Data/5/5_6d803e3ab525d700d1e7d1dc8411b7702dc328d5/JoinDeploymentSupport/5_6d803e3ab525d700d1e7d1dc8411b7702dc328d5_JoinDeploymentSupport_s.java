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
 * $Id: JoinDeploymentSupport.java,v 1.5 2006-01-23 18:52:08 gasiunas Exp $
  */
 
 package org.caesarj.compiler.joinpoint;
 
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.ast.phylum.JCompilationUnit;
 import org.caesarj.compiler.ast.phylum.declaration.CjClassDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjInterfaceDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjVirtualClassDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.JTypeDeclaration;
 import org.caesarj.compiler.constants.CaesarConstants;
 import org.caesarj.compiler.context.CContext;
 import org.caesarj.util.PositionedError;
 
 /**
  * @author Ostermann
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class JoinDeploymentSupport implements CaesarConstants {
 	
 	/**
 	 * Generates for every nested crosscutting class the corresponding deployment support classes.
 	 */
 	public static void prepareForDynamicDeployment(JCompilationUnit cu)  throws PositionedError {
 		boolean bNewDelarations = false;
 		CContext ownerCtx = cu.getContext();
 		
 		for (JTypeDeclaration typeDecl : cu.getInners()) {
 			
 			if (typeDecl instanceof CjVirtualClassDeclaration) {
 
 				CjVirtualClassDeclaration caesarClass =
 					(CjVirtualClassDeclaration)typeDecl;
 				
 				if (caesarClass.getRegistryClass() != null) {
 					
 					// add the deployment support classes to the enclosing class
 					CjInterfaceDeclaration aspectIfc = caesarClass.getAspectInterface();
 					CjClassDeclaration registryCls = caesarClass.getRegistryClass();
 					
 					cu.addInners(new JTypeDeclaration[] { aspectIfc, registryCls});
 					bNewDelarations = true;
 					
 					// join the modified and new classes
 					aspectIfc.join(ownerCtx);
 					registryCls.join(ownerCtx);	
 					//caesarClass.getMixinIfcDeclaration().join(ownerCtx);
 				}
 				
 				if (caesarClass.getInners().length > 0) {
 					//consider nested types
 					JoinDeploymentSupport.prepareForDynamicDeployment(caesarClass, cu.getEnvironment());
 				}
 			}
 		}
 		if (bNewDelarations) {
 			rejoinMixinInterfaces(cu.getInners(), ownerCtx);
 		}
 	}
 	
 	private static void prepareForDynamicDeployment(CjClassDeclaration cd, KjcEnvironment environment)  throws PositionedError
 	{
 	    boolean bNewInners = false;
 	    CContext ownerCtx = (CContext)cd.getContext();
 	    
 	    for (JTypeDeclaration inner : cd.getInners())
 		{
 			if (inner instanceof CjVirtualClassDeclaration)
 			{
 				//create support classes for each crosscutting inner class
 				CjVirtualClassDeclaration innerCaesarClass =
 					(CjVirtualClassDeclaration)inner;
 				if (innerCaesarClass.getRegistryClass() != null)
 				{
 					//add the deployment support classes to the enclosing class
 					CjInterfaceDeclaration aspectIfc = innerCaesarClass.getAspectInterface();
 					CjClassDeclaration registryCls = innerCaesarClass.getRegistryClass();
 					
 					cd.addInners(new JTypeDeclaration[] { aspectIfc, registryCls });
 					bNewInners = true;
 										
 					//join the modified and new classes
 					aspectIfc.join(ownerCtx);
 					registryCls.join(ownerCtx);
 					//innerCaesarClass.getMixinIfcDeclaration().join(ownerCtx);					
 				}
 
 				//handle the inners of the inners
 				JTypeDeclaration[] innersInners = innerCaesarClass.getInners();
 				for (int j = 0; j < innersInners.length; j++)
 				{
 					if (innersInners[j] instanceof CjClassDeclaration)
 					{
 						CjClassDeclaration currentInnerInner =
 							(CjClassDeclaration) innersInners[j];
 						JoinDeploymentSupport.prepareForDynamicDeployment(currentInnerInner, environment);
 					}
 				}
 			}
 		}
 
 		if (bNewInners)	{
 			rejoinMixinInterfaces(cd.getInners(), ownerCtx);
 		}
 	}
 	
 	/**
 	 * Rejoin the mixin interfaces of the crosscutting Caesar classes
 	 * 
 	 * @param decl			array of type declarations
 	 * @param ownerCtx		owner context
 	 */
 	private static void rejoinMixinInterfaces(JTypeDeclaration[] decl, CContext ownerCtx) throws PositionedError {
 		for (int i = 0; i < decl.length; i++) {
 			if (decl[i] instanceof CjVirtualClassDeclaration) {
 				CjVirtualClassDeclaration caesarClass =	(CjVirtualClassDeclaration)decl[i];
				if (caesarClass.isCrosscutting()) {
 					caesarClass.getMixinIfcDeclaration().join(ownerCtx);						
 				}
 			}
 		}
 	}
 }
