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
 * $Id: JoinCollaborations.java,v 1.5 2005-10-11 14:59:55 gasiunas Exp $
  */
 package org.caesarj.compiler.joincollab;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.caesarj.compiler.CompilerBase;
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.ast.phylum.JCompilationUnit;
 import org.caesarj.compiler.ast.phylum.declaration.CjClassDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjVirtualClassDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.JTypeDeclaration;
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.util.PositionedError;
 
 /**
  * @author vaidas
  *
  * TODO [documentation]
  */
 public class JoinCollaborations {
 	
 	protected KjcEnvironment environment = null;
 	protected CompilerBase compiler = null;
 	protected Map declMap = null;
     
 	public JoinCollaborations(KjcEnvironment environment, CompilerBase compiler) {
 		this.environment = environment;
 		this.compiler = compiler;
 	}
 	
 	public void joinAll(JCompilationUnit[] tree) {
 		buildIndex(tree);
 		
 		for (int i = 0; i < tree.length; i++) {
             JCompilationUnit cu = tree[i];
             
             if (!cu.getPackageName().isCollaboration()) {
             	continue;
             }
             
             String collabName = cu.getPackageName().getName();
             CjVirtualClassDeclaration collabDecl = (CjVirtualClassDeclaration)declMap.get(collabName);
             
             if (collabDecl == null) {
         		compiler.reportTrouble(
         	            new PositionedError(
         	            		cu.getPackageName().getTokenReference(),
     							CaesarMessages.CANNOT_FIND_ENCLOSING_CCLASS /* !!! change */
                         )
                     );
         		continue;
         	} else {
         	    // Set the collaboration declaration in the compilation unit
         	    cu.setCollaboration(collabDecl);
         	}
             
            	moveInners(cu, collabDecl);            
         }
 	}
 	
 	protected void buildIndex(JCompilationUnit[] tree) {
 		declMap = new Hashtable(1000);
 		
 		for (int i = 0; i < tree.length; i++) {
             JCompilationUnit cu = tree[i];
             JTypeDeclaration inners[] = cu.getInners();
             for (int i1 = 0; i1 < inners.length; i1++) {
             	if (inners[i1] instanceof CjVirtualClassDeclaration) {
             		buildIndex((CjVirtualClassDeclaration)inners[i1], cu.getPackageName().getName());
             	}
             }
 		}	
 	}
 	
 	protected void buildIndex(CjVirtualClassDeclaration typeDecl, String prefix) {
 		String className = prefix + "/" + typeDecl.getOriginalIdent();
 		declMap.put(className, typeDecl);
 		
 		JTypeDeclaration inners[] = typeDecl.getInners();
         for (int i1 = 0; i1 < inners.length; i1++) {
         	if (inners[i1] instanceof CjVirtualClassDeclaration) {
         		buildIndex((CjVirtualClassDeclaration)inners[i1], className);
         	}
         }
 	}
 	
 	protected void moveInners(JCompilationUnit cu, CjVirtualClassDeclaration collabDecl) {
 		
         JTypeDeclaration inners[] = cu.getInners();
         
         for (int i1 = 0; i1 < inners.length; i1++) {
         	if (inners[i1] instanceof CjVirtualClassDeclaration) {
         		CjVirtualClassDeclaration nestedDecl = (CjVirtualClassDeclaration)inners[i1];
         		
        		collabDecl.setOriginalCompUnit(cu);
         		
         		JTypeDeclaration newIfc[] = { nestedDecl };
         		collabDecl.addInners(newIfc);
         	}
         	else if (!(inners[i1] instanceof CjClassDeclaration)) {
         		compiler.reportTrouble(
     	            new PositionedError(
     	            	inners[i1].getTokenReference(),
 						CaesarMessages.CCLASS_CANNOT_NEST_CLASS
                     )
                 );
         	}            		
         }
         
         // Warn the compilation unit, that the inners were already copied.
         // It will cause the inners to be moved to origTypeDeclarations
         cu.fireInnersCopied();
         //cu.setInners(new JTypeDeclaration[0]);
 	}
 }
