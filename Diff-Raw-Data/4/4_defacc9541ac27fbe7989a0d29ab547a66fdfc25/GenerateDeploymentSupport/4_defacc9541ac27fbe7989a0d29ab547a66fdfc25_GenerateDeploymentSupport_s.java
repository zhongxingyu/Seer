 package org.caesarj.compiler.joinpoint;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.caesarj.compiler.CompilerBase;
 import org.caesarj.compiler.KjcEnvironment;
 import org.caesarj.compiler.aspectj.CaesarAdviceKind;
 import org.caesarj.compiler.aspectj.CaesarDeclare;
 import org.caesarj.compiler.aspectj.CaesarNameMangler;
 import org.caesarj.compiler.ast.phylum.declaration.CjAdviceDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjPointcutDeclaration;
 import org.caesarj.compiler.ast.phylum.declaration.CjVirtualClassDeclaration;
 import org.caesarj.compiler.typesys.CaesarTypeSystem;
 import org.caesarj.compiler.typesys.graph.CaesarTypeNode;
 import org.caesarj.compiler.typesys.java.JavaTypeNode;
 
 /**
  * @author vaidas
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class GenerateDeploymentSupport {
 	CompilerBase compiler;
 	KjcEnvironment environment;
 	CaesarTypeSystem caesarTypeSystem;
 		
 	public GenerateDeploymentSupport(CompilerBase compiler, KjcEnvironment environment) {
 		this.compiler = compiler;
 		this.environment = environment;
 		this.caesarTypeSystem = environment.getCaesarTypeSystem(); 
 	}
 	
 	public void generateSupportClasses() {
 		determineUniqueCrosscutting();
 		determineToBeGenerated();
 		generateAdviceMethods();
 		generateAspectRegistries();
 		cleanCrosscuttingInfo();		
 	}
 	
 	private void determineUniqueCrosscutting() {
         Collection allTypes = caesarTypeSystem.getJavaTypeGraph().getAllTypes();        
 		 
 		for (Iterator it = allTypes.iterator(); it.hasNext();) {
 		 	JavaTypeNode item = (JavaTypeNode)it.next();
 		 	CaesarTypeNode node = item.getType();
 		 	if (node == null) {
 		 		continue;
 		 	}		 	
 		 	List ccLst = getCrosscuttingMixinList(node);
 		 	/* skip non-crosscutting classes */
 		 	if (!ccLst.isEmpty()) {
 		 		List ccSuperLst = getSuperCrosscuttingMixinList(node);
 		 	
 			 	/* check if the crosscutting info is different from super */
 			 	if (!ccSuperLst.containsAll(ccLst)) {
 			 		node.setUniqueCrosscutting();
 			 	}
 		 	}
 		}
 	}
 	
 	private void determineToBeGenerated() {
 		Collection allTypes = caesarTypeSystem.getJavaTypeGraph().getAllTypes();        
 		 
 		for (Iterator it = allTypes.iterator(); it.hasNext();) {
 		 	JavaTypeNode item = (JavaTypeNode)it.next();
 		 	CaesarTypeNode node = item.getType();
 		 	if (node == null) {
 		 		continue;
 		 	}
 		 	if (node.canBeInstantiated()) {
		 		List ccLst = getCrosscuttingMixinList(node);
		 		for (Iterator it2 = ccLst.iterator(); it2.hasNext();) {
 		 			CaesarTypeNode mixin = (CaesarTypeNode)it2.next();
 		 			if (mixin.isUniqueCrosscutting()) {
 		 				mixin.setNeedsAspectRegistry();
 		 				break;
 		 			}		 			
 		 		}
 		 	}
 		}
 	}
 	
 	private void generateAdviceMethods() {
 		Collection allTypes = caesarTypeSystem.getJavaTypeGraph().getAllTypes();        
 		 
 		for (Iterator it = allTypes.iterator(); it.hasNext();) {
 		 	JavaTypeNode item = (JavaTypeNode)it.next();
 		 	CaesarTypeNode node = item.getType();
 		 	if (node == null) {
 		 		continue;
 		 	}		 	
 		 	if (node.declaredCrosscutting()) {
 		 		CjVirtualClassDeclaration caesarClass = node.getTypeDecl().getCorrespondingClassDeclaration();
 		 		caesarClass.sortAdvicesByOrderNr();
 				
 		 		DeploymentClassFactory utils =
 					new DeploymentClassFactory(
 						caesarClass,
 						environment);
 				
 				utils.generateAdviceMethods();
 		 	}
 		}
 	}
 	
 	private void generateAspectRegistries() {
 		Collection allTypes = caesarTypeSystem.getJavaTypeGraph().getAllTypes();        
 		 
 		for (Iterator it = allTypes.iterator(); it.hasNext();) {
 		 	JavaTypeNode item = (JavaTypeNode)it.next();
 		 	CaesarTypeNode node = item.getType();
 		 	if (node == null) {
 		 		continue;
 		 	}		 	
 		 	if (node.needsAspectRegistry()) {
 		 		CjVirtualClassDeclaration caesarClass = node.getTypeDecl().getCorrespondingClassDeclaration();
 				
 		 		DeploymentClassFactory utils =
 					new DeploymentClassFactory(
 						caesarClass,
 						environment);
 		 		
 		 		List pointcuts = new LinkedList();
 		 		List advices = new LinkedList();
 		 		List declares = new LinkedList();
 		 		collectAllCrosscuts(node, pointcuts, advices, declares);
 		 		CjAdviceDeclaration[] adviceArr = (CjAdviceDeclaration[])advices.toArray(new CjAdviceDeclaration[0]);
 		 		CjPointcutDeclaration[] pointcutArr = (CjPointcutDeclaration[])pointcuts.toArray(new CjPointcutDeclaration[0]);
 		 		CaesarDeclare[] declareArr = (CaesarDeclare[])declares.toArray(new CaesarDeclare[0]);
 		 		
 		 		caesarClass.setAspectInterface(
 		 				utils.createAspectInterface(adviceArr));
 		 		caesarClass.setRegistryClass(
 		 				utils.createSingletonAspect(pointcutArr, adviceArr, declareArr));
 		 		utils.modifyAspectClass();				
 		 	}
 		}
 	}
 	
 	private void cleanCrosscuttingInfo() {
 		Collection allTypes = caesarTypeSystem.getJavaTypeGraph().getAllTypes();        
 		 
 		for (Iterator it = allTypes.iterator(); it.hasNext();) {
 		 	JavaTypeNode item = (JavaTypeNode)it.next();
 		 	CaesarTypeNode node = item.getType();
 		 	if (node == null) {
 		 		continue;
 		 	}		 	
 		 	if (node.declaredCrosscutting()) {
 		 		CjVirtualClassDeclaration caesarClass = node.getTypeDecl().getCorrespondingClassDeclaration();
 				
 		 		DeploymentClassFactory utils =
 					new DeploymentClassFactory(
 						caesarClass,
 						environment);
 				
 				utils.cleanCrosscuttingInfo();
 		 	}
 		}
 	}
 		
 	private List getSuperCrosscuttingMixinList(CaesarTypeNode node) {
 		Iterator it = node.getMixinList().iterator();
 		it.next(); /* skip the node itself */
 		if (it.hasNext()) {
 			return getCrosscuttingMixinList((CaesarTypeNode)it.next());
 		}
 		else {
 			return new LinkedList(); /* return empty list */
 		}
 	}
 	
 	private List getCrosscuttingMixinList(CaesarTypeNode node) {
 		List lst = new LinkedList();
 		for (Iterator it = node.getMixinList().iterator(); it.hasNext();) {
             CaesarTypeNode item = (CaesarTypeNode) it.next();
             if (item.declaredCrosscutting()) {
             	lst.add(item);
             }
         }
 		return lst;
 	}
 	
 	private void collectAllCrosscuts(CaesarTypeNode node, List pointcuts, List advices, List declares) {
 		List ccLst = getCrosscuttingMixinList(node);
 		HashSet pctSet = new HashSet();
 		
 		/* counter for advice name generation */
 		int counter = 0;
 		int mixinCount = ccLst.size();
 			
  		for (Iterator it = ccLst.iterator(); it.hasNext();) {
  			counter++;
  			CaesarTypeNode mixin = (CaesarTypeNode)it.next();
  			CjVirtualClassDeclaration classDecl = mixin.getTypeDecl().getCorrespondingClassDeclaration();
  			
  			/* add all before and around advices to the list */
  			CjAdviceDeclaration declAdv[] = classDecl.getAdvices();
  			for (int i1 = 0; i1 < declAdv.length; i1++) {
  				CjAdviceDeclaration advCopy = new CjAdviceDeclaration(declAdv[i1]);
  				
  				/* put advice at the order of correct precedence */
  				if (declAdv[i1].getKind() != CaesarAdviceKind.After &&
  	 				declAdv[i1].getKind() != CaesarAdviceKind.AfterThrowing &&
  					declAdv[i1].getKind() != CaesarAdviceKind.AfterReturning) 
  				{
  					String ident = CaesarNameMangler.adviceName(
  	 						"" + counter,
  	 						advCopy.getKind(),
  	 						advCopy.getTokenReference().getLine());
  	 				advCopy.setIdent(ident);
  					advices.add(advCopy);
  	 			} 								
  			}
  			
  			/* add unique pointcuts to the list */
  			CjPointcutDeclaration declPct[] = classDecl.getPointcuts();
  			for (int i1 = 0; i1 < declPct.length; i1++) {
  				String ident = declPct[i1].getIdent();
  				if (!pctSet.contains(ident)) {
  					pctSet.add(ident);
  					pointcuts.add(declPct[i1]);
  				} 				
  			}
  			
  			/* colect all static crosscutting declarations */
  			CaesarDeclare declArr[] = classDecl.getDeclares();
  			for (int i1 = 0; i1 < declArr.length; i1++) {
  				declares.add(declArr[i1]);
  			}
  		}
  		
  		/* add all after advices to the list in reverse order */
 		counter = 0;
 		for (int mixinNr = ccLst.size()-1; mixinNr >= 0; mixinNr--) {
  			counter++;
  			CaesarTypeNode mixin = (CaesarTypeNode)ccLst.get(mixinNr);
  			CjVirtualClassDeclaration classDecl = mixin.getTypeDecl().getCorrespondingClassDeclaration();
  			
  			/* add all after advices to the list */
  			CjAdviceDeclaration declAdv[] = classDecl.getAdvices();
  			for (int i1 = 0; i1 < declAdv.length; i1++) {
  				CjAdviceDeclaration advCopy = new CjAdviceDeclaration(declAdv[i1]);
  				
  				/* put advice at the order of correct precedence */
  				if (declAdv[i1].getKind() == CaesarAdviceKind.After ||
  	 				declAdv[i1].getKind() == CaesarAdviceKind.AfterThrowing ||
  					declAdv[i1].getKind() == CaesarAdviceKind.AfterReturning) 
  				{
  					String ident = CaesarNameMangler.adviceName(
  	 						"" + (mixinCount + counter),
  	 						advCopy.getKind(),
  	 						advCopy.getTokenReference().getLine());
  	 				advCopy.setIdent(ident); 	 				
  					advices.add(advCopy);
  	 			} 				 				
  			} 	
  		}
  		
  		counter = 0;
  		/* go through all advices once again and set order number */
  		for (Iterator it = advices.iterator(); it.hasNext();) {
  			((CjAdviceDeclaration)it.next()).setOrderNr(counter++);
  		}
 	}
 }
