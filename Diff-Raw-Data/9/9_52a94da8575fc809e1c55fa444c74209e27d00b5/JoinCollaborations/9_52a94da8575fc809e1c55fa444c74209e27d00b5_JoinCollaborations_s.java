 /*
  * Created on 11.02.2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
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
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
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
         		
         		if (collabDecl.isExported()) {
 	                nestedDecl.generateInterface(
 	        			environment.getClassReader(),   
 	        			collabDecl.getCClass(), 
 	        			collabDecl.getCClass().getQualifiedName() + "$"
 	                );
         		}
         		
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
         
        cu.setInners(new JTypeDeclaration[0]);
 	}
 }
