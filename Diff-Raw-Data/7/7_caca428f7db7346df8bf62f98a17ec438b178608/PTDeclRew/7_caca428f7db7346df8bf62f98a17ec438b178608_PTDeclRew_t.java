 package javaworld;
 
 import testutils.utils.CriticalPTException;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Iterator;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 
 import com.google.common.base.Joiner;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 
 // it _might_ be argued that AST.* could be appropriate..
 import AST.PTInterfaceAddsDecl;
 import AST.PTGenericInterfaceAddsDecl;
 import AST.SimpleSet;
 import AST.InterfaceDecl;
 import AST.Access;
 import AST.BodyDecl;
 import AST.ClassDecl;
 import AST.ClassAccess;
 import AST.CompilationUnit;
 import AST.ImportDecl;
 import AST.GenericClassDecl;
 import AST.List;
 import AST.Modifier;
 import AST.Modifiers;
 import AST.Opt;
 import AST.PTClassAddsDecl;
 import AST.PTClassDecl;
 import AST.PTDecl;
 import AST.PTInstDecl;
 import AST.PTInstTuple;
 import AST.PTTemplate;
 import AST.PTPackage;
 import AST.SimpleClass;
 import AST.TypeDecl;
 import AST.EnumDecl;
 import AST.PTEnumDecl;
 import AST.TypeVariable;
 import AST.TypeAccess;
 import AST.ParTypeAccess;
 import AST.InterfaceDecl;
 import AST.GenericInterfaceDecl;
 import AST.PTDummyRename;
 import AST.PTMethodRename;
 import AST.PTMethodRenameAll;
 import AST.PTFieldRename;
 import AST.MethodDecl;
 
 import AST.PTConstructorDecl;
 import AST.PTTSuperConstructorCall;
 import AST.VarAccess;
 import AST.TemplateClassIdentifier;
 import AST.Expr;
 import AST.Stmt;
 import AST.Block;
 import AST.ParameterDeclaration;
 import AST.ASTNode;
 import AST.ConstructorDecl;
 import AST.RequiredType;
 import AST.RequiredClass;
 import AST.RequiredInterface;
 import AST.RequiredTypeInstantiation;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.google.common.collect.Iterables;
 
 /** TODO after my work on interfaces:
   *   - check for various illegal situations such as
   *     merging interfaces with classes
   *   - normalize the names of things that are now
   *     referred to as "classes" in method names but
   *     could really also be interfaces
   *     (e.g. destinationClassIDsWithInstTuples
   *     and friends).
   * -svk
   */
 
 
 public class PTDeclRew {
 
     private ParameterRewriter paramRewriter;
 
 	private final PTDecl ptDeclToBeRewritten;
 
 	private ImmutableList<SimpleClassRew> simpleClasses;
 
 	public PTDeclRew(PTDecl ptDeclToBeRewritten) {
 		this.ptDeclToBeRewritten = ptDeclToBeRewritten;
         addImpliedRenames();
 	}
 
 	protected void flushCaches() {
 		ptDeclToBeRewritten.flushCaches();
 	}
 
 	protected void copyImportDecls() {
 		CompilationUnit ownCU = ptDeclToBeRewritten.getCompilationUnit();
 		for (PTInstDecl instDecl : ptDeclToBeRewritten.getPTInstDecls()) {
 			PTTemplate originator = instDecl.getTemplate();
 			if (originator == null)
 				continue; // error caught elsewhere
 			CompilationUnit cunit = originator.getCompilationUnit();
 			if (cunit == ownCU)
 				continue;
 			for (ImportDecl id : cunit.getImportDeclList()) {
 				if (!ptDeclToBeRewritten.hasImportDecl(id)) {
 					ImportDecl copy = (ImportDecl) id.copy();
 					ownCU.addImportDecl(copy);
 				}
 			}
 		}
 	}
 
     /** Create enums 'inherited' from templates, with their new names.
      */
 
     protected void createRenamedEnums() {
         for( String name : getDestinationIDsForEnums() ) {
             ptDeclToBeRewritten.getPTEnumDeclList().add( getRenamedEnumByName( name ) );
         }
     }
 
     protected void concretifyRequiredTypes() {
         concretifyRequiredTypes( false );
     }
 
     protected void concretifyRequiredTypes(boolean alreadyAutoconcretified) {
         Multimap<String,Access> concretifications = HashMultimap.create();
 
         RequiredTypeRewriter rewriter = new RequiredTypeRewriter();
         java.util.Set<RequiredType> toBeDeleted = new java.util.LinkedHashSet<RequiredType>();
 
         /* see email, per now assume: <= on target name (after renames) */
 
         Map<RequiredType, TypeDecl> concretificationPlan = new LinkedHashMap<RequiredType, TypeDecl>();
 
         java.util.Set<RequiredType> lacksExplicitConcretification = new java.util.LinkedHashSet<RequiredType>();
 
         if( ptDeclToBeRewritten instanceof PTPackage ) {
             for( RequiredType rt : ptDeclToBeRewritten.getRequiredTypeList() ) {
                 lacksExplicitConcretification.add( rt );
             }
         }
 
 		for (PTInstDecl instDecl : ptDeclToBeRewritten.getPTInstDecls()) {
             for( RequiredTypeInstantiation rti : instDecl.getRequiredTypeInstantiationList() ) {
                 // System.out.println( "detected concretification " + rti.getRequiredTypeName() );
                 concretifications.put( rti.getRequiredTypeName(), (Access) rti.getConcreteTypeAccess() );
             }
         }
 
         int processed = 0;
 
         for( String key : concretifications.keySet() ) {
             boolean stopError = false;
             TypeDecl tdecl = null;
         
             SimpleSet matches = ptDeclToBeRewritten.lookupTypeInPTDecl( key );
             if( matches.size() < 1 ) {
                 ptDeclToBeRewritten.error( "concretification of unknown type: " + key );
                 stopError = true;
             } else if( matches.size() > 1 ) {
                 // detected elsewhere
                 stopError = true;
             } else {
                 tdecl = (TypeDecl) matches.iterator().next();
                 if( !(tdecl instanceof RequiredType) ) {
                     stopError = true;
                     ptDeclToBeRewritten.error( "concretification of non-required type/class/interface: " + key );
                 }
             }
             if( concretifications.get( key ).size() > 1 ) {
                 ptDeclToBeRewritten.error( "multiple concretifications of " + key );
                 stopError = true;
             }
 
             TypeDecl replacementType  = null;
             String replacement = "";
 
             if( !stopError ) {
                 Access replacementAccess = concretifications.get( key ).iterator().next();
                 replacementType = Util.declarationFromTypeAccess( replacementAccess );
 
                 concretificationPlan.put( (RequiredType) tdecl, replacementType );
 
                 processed ++;
             }
 
         }
 
         for( RequiredType rt : concretificationPlan.keySet() ) {
             lacksExplicitConcretification.remove( rt );
         }
 
         if( processed != concretifications.keySet().size() ) {
             // failure
             return;
         }
 
         ConcretificationScheme scheme = new ConcretificationScheme( concretificationPlan, ptDeclToBeRewritten.getPTDeclContext() );
 
         // this needs to be done in specific order, TODO topological sort by internal extends-relations
         // (?)
         // doesn't seem to require toposort by extends relations
         // we can't toposort by access as this can be circular, so we can exclude that as a solution
         for( RequiredType rt : lacksExplicitConcretification ) {
             TypeConstraint tc = rt.getTypeConstraint( scheme );
             // System.out.println( "should auto-concretify " + rt.getID() + " to constraints " + tc );
 
             java.util.List<TypeDecl> supertypes = new java.util.ArrayList<TypeDecl> ();
 
             TypeDecl dominatingSupertype = ptDeclToBeRewritten.getProgram().typeObject(); // java.lang.Object
             supertypes.add( dominatingSupertype );
 
             boolean mustBeClass = tc.mustBeClass();
             boolean hasContents = tc.getMethodsIterator().hasNext()
                                   || tc.getConstructorsIterator().hasNext();
 
             for( Iterator<TypeDescriptor> itd = tc.getExtendedTypesIterator(); itd.hasNext(); ) {
                 TypeDescriptor td = itd.next();
                 TypeDecl tdecl = Util.declarationFromTypeAccess( td.mapByScheme( scheme ).getAccess() );
                 supertypes.add( tdecl );
             }
             for( Iterator<TypeDescriptor> itd = tc.getImplementedTypesIterator(); itd.hasNext(); ) {
                 TypeDescriptor td = itd.next();
                 TypeDecl tdecl = Util.declarationFromTypeAccess( td.mapByScheme( scheme ).getAccess() );
                 supertypes.add( tdecl );
             }
 
             for( TypeDecl tdecl : supertypes ) {
                 // System.out.println( "is " + dominatingSupertype.fullName() + " a dominating supertype? then " + tdecl.fullName() + " must be a subtype.." );
                 if( !dominatingSupertype.subtype( tdecl ) ) {
                     // System.out.println( "but it is not. how about the other way around?" );
                     if( tdecl.subtype( dominatingSupertype ) ) {
                         // System.out.println( "good!" );
                         dominatingSupertype = tdecl;
                     } else {
                         // System.out.println( "failed" );
                         dominatingSupertype = null;
                         break;
                     }
                 }
             }
 
             // System.out.println( "has contents? " + hasContents );
             // System.out.println( "has domSuper? " + (dominatingSupertype != null) );
             // System.out.println( "must be interface? " + tc.mustBeInterface() );
 
             if( !hasContents
                 && dominatingSupertype != null
                 && !( ( dominatingSupertype.isClassDecl() && tc.mustBeInterface() )
                       || ( dominatingSupertype.isInterfaceDecl() && tc.mustBeClass() ) )
               ) {
                 // System.out.println( "auto-concretify " + rt.getID() + " by replacing with dominating explicit supertype " + dominatingSupertype.fullName() );
 
                 concretificationPlan.put( rt, dominatingSupertype );
                 
             } else {
                 // System.out.println( "cannot auto-concretify " + rt.getID() + " by replacement with existing type" );
                 try {
                     TypeDecl td = JastaddTypeConstraints.convertToTypeDecl( rt.getID(), tc, ptDeclToBeRewritten );
 
                     if( td instanceof InterfaceDecl ) {
                         ptDeclToBeRewritten.addInterfaceDecl( (InterfaceDecl) td );
 
                         concretificationPlan.put( rt, td );
                     } else if( td instanceof ClassDecl ) {
                         ptDeclToBeRewritten.addSimpleClass( new PTClassDecl( (ClassDecl) td ) );
 
                         concretificationPlan.put( rt, td );
                     } else {
                         throw new OperationImpossible( "conversion resulted in unexpected type " + td.getClass().getName() + " (internal error)" );
                     }
                 }
                 catch( OperationImpossible e ) {
                     // System.out.println( "failed to auto-concretify " + rt.getID() );
                 }
             }
         }
 
         {
             boolean didChange;
             do {
                 // TODO ensure no cycles possible (with erroneous code)
 
                 didChange = false;
 
                 for( RequiredType tdecl : concretificationPlan.keySet() ) {
                     TypeDecl tdval = concretificationPlan.get( tdecl );
                     if( tdval instanceof RequiredType ) {
                         TypeDecl tdvalval = concretificationPlan.get( tdval );
                         // System.out.println( "performing redirect: "  + tdecl.getID() + " --> " + tdval.getID() + " --> " + tdvalval.getID() );
                         concretificationPlan.put( tdecl, tdvalval );
                         didChange = true;
                     }
                 }
             } while( didChange );
         }
 
         for( RequiredType tdecl : concretificationPlan.keySet() ) {
             boolean stopError = false;
             String key = tdecl.getID();
             TypeDecl replacementType = concretificationPlan.get( tdecl );
 
             // check conformance
             if( !stopError ) {
                 RequiredType reqType = (RequiredType) tdecl;
                 if( ! lacksExplicitConcretification.contains( tdecl ) ) {
                     TypeConstraint cand = JastaddTypeConstraints.fromReferenceTypeDecl( replacementType, scheme );
                     TypeConstraint constraint = reqType.getTypeConstraint( scheme );
                     if( cand == null ) {
                         if( replacementType == null ) {
                             ptDeclToBeRewritten.error( "concretification candidate not found" ); // TODO friendlier
                         } else {
                             ptDeclToBeRewritten.error( "concretification candidate " + replacementType.getID() + " is unsuitable (not a known reference type)" );
                         }
                         stopError = true;
                     } else {
                         try {
                             cand.satisfies( constraint, scheme );
                         }
                         catch( TypeConstraintFailed e ) {
                             ptDeclToBeRewritten.error( e.getMessage() );
                             stopError = true;
                         }
                     }
                 }
 
                 if( !stopError ) {
                     /*
                     String replacementName = replacementType.getID();
                     // TODO this should be a fully qualified access to avoid problems
                     // however, that's not just TypeAccess("java.lang.foo")..
 
                     TypeAccess typeAccess = new TypeAccess( replacementName );
 
                     */
 
 //                    Access access = replacementType.createQualifiedAccess();
                     Collection<Access> originalAccesses = concretifications.get( tdecl.getID() );
                     Access originalAccess;
                     if( originalAccesses.iterator().hasNext() ) {
                         originalAccess = originalAccesses.iterator().next();
                     } else {
                         // System.out.println( "adding rewrite from required type " + reqType.getID() + " --> " + replacementType.getID() );
                         originalAccess = replacementType.createQualifiedAccess();
                     }
 
                     rewriter.addRewrite( reqType, originalAccess );
                     toBeDeleted.add( reqType );
                 }
             }
         }
 
         // System.out.println( "performing all rewrites" );
         rewriter.mutate( ptDeclToBeRewritten );
 
         {
             AST.List<RequiredType> remainingTypes = new AST.List<RequiredType>();
             for( RequiredType rt : ptDeclToBeRewritten.getRequiredTypeList() ) {
                 if( ! toBeDeleted.contains( rt ) ) {
                     remainingTypes = remainingTypes.add( rt );
                 }
             }
             ptDeclToBeRewritten.setRequiredTypeList( remainingTypes );
         }
 
 
         if( ptDeclToBeRewritten instanceof PTPackage ) {
             java.util.Set<String> unconcrete = new java.util.HashSet<String>();
             for( RequiredType rt : ptDeclToBeRewritten.getRequiredTypeList() ) {
                 unconcrete.add( rt.getID() );
             }
 
             if( unconcrete.size() > 0 ) {
                 ptDeclToBeRewritten.error( "remaining unconcretified required types in package: " + Joiner.on( ", " ).join( unconcrete ) );
             }
         }
     }
 
     protected ConcretificationScheme createRequiredTypeTargets() {
         Map<RequiredType, TypeDecl> concMapToTemporaries = new java.util.LinkedHashMap<RequiredType, TypeDecl> ();
 
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         for( String key : destinationClassIDsWithInstTuples.keySet() ) {
             java.util.List<RequiredType> originatorReqTypes = new java.util.ArrayList<RequiredType>();
             TypeDecl nonRTOriginator = null;
 
             for( PTInstTuple tuple : destinationClassIDsWithInstTuples.get( key ) ) {
                 TypeDecl decl = tuple.getOriginator();
                 if( decl instanceof RequiredType ) {
                     RequiredType renamedDecl = new InstTupleRew( tuple ).getRenamedSourceRequiredType();
                     originatorReqTypes.add( renamedDecl );
                 } else {
                     nonRTOriginator = decl;
                 }
             }
 
             if( originatorReqTypes.size() == 0 ) continue;
 
             PTInstDecl ptid = (PTInstDecl) destinationClassIDsWithInstTuples.get( key ).iterator().next().getParentClass( PTInstDecl.class );
 
             if( nonRTOriginator != null ) {
                 ptid.error( "cannot merge required type(s) with concrete type " + nonRTOriginator.getID() + " into " + key );
                 continue;
             }
 
             boolean mustBeType = false, mustBeClass = false, mustBeInterface = false;
             java.util.Set<String> typesMerged = new java.util.HashSet<String>();
             
             for( RequiredType rt : originatorReqTypes ) {
                 if( rt instanceof RequiredClass ) {
                     mustBeClass = true;
                     typesMerged.add( "required class " + rt.getID() );
                 } else if( rt instanceof RequiredInterface ) {
                     mustBeInterface = true;
                     typesMerged.add( "required interface " + rt.getID() );
                 } else {
                     // note, here we're removing potential freedom per article
                     mustBeType = true;
                     typesMerged.add( "required type " + rt.getID() );
                 }
             }
             int totalReqs = (mustBeType ? 1 : 0) + (mustBeClass ? 1 : 0) + (mustBeInterface ? 1 : 0);
 
             if( totalReqs != 1 ) {
                 ptid.error( "will not perform heterogeneous merge of required types: " + Joiner.on( ", " ).join( typesMerged ) );
                 continue;
             }
 
             RequiredType myTemporaryRequiredType = JastaddTypeConstraints.convertToRequiredType( key, new TypeConstraint(), ptDeclToBeRewritten.getPTDeclContext() );
 
             ptDeclToBeRewritten.addRequiredType( myTemporaryRequiredType );
 
             for( RequiredType originatorRT : originatorReqTypes ) {
                 concMapToTemporaries.put( originatorRT, myTemporaryRequiredType );
             }
         }
 
         return new ConcretificationScheme( concMapToTemporaries, ptDeclToBeRewritten.getPTDeclContext() );
     }
 
 	protected void createMergedRequiredTypes() {
         ConcretificationScheme temporaryScheme = createRequiredTypeTargets();
 
         Multimap<String, RequiredType> localRtAdds = HashMultimap.create();
         for( RequiredType rta : ptDeclToBeRewritten.getRequiredTypeAdditions() ) {
             localRtAdds.put( rta.getID(), rta );
         }
 
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         for( String key : destinationClassIDsWithInstTuples.keySet() ) {
             java.util.List<RequiredType> originatorReqTypes = new java.util.ArrayList<RequiredType>();
             TypeDecl nonRTOriginator = null;
 
             for( PTInstTuple tuple : destinationClassIDsWithInstTuples.get( key ) ) {
                 TypeDecl decl = tuple.getOriginator();
                 if( decl instanceof RequiredType ) {
                     RequiredType renamedDecl = new InstTupleRew( tuple ).getRenamedSourceRequiredType();
                     originatorReqTypes.add( renamedDecl );
                 } else {
                     nonRTOriginator = decl;
                 }
             }
 
             if( originatorReqTypes.size() == 0 ) continue;
 
 
 
             /*
 
             PTInstDecl ptid = (PTInstDecl) destinationClassIDsWithInstTuples.get( key ).iterator().next().getParentClass( PTInstDecl.class );
 
             if( nonRTOriginator != null ) {
                 ptid.error( "cannot merge required type(s) with concrete type " + nonRTOriginator.getID() + " into " + key );
                 continue;
             }
 
             boolean mustBeType = false, mustBeClass = false, mustBeInterface = false;
             java.util.Set<String> typesMerged = new java.util.HashSet<String>();
             
             for( RequiredType rt : originatorReqTypes ) {
                 if( rt.getTypeConstraint().mustBeClass() ) {
                     mustBeClass = true;
                     typesMerged.add( "required class " + rt.getID() );
                 } else if( rt.getTypeConstraint().mustBeInterface() ) {
                     mustBeInterface = true;
                     typesMerged.add( "required interface " + rt.getID() );
                 } else {
                     // note, here we're removing potential freedom per article
                     mustBeType = true;
                     typesMerged.add( "required type " + rt.getID() );
                 }
             }
             int totalReqs = (mustBeType ? 1 : 0) + (mustBeClass ? 1 : 0) + (mustBeInterface ? 1 : 0);
 
             if( totalReqs != 1 ) {
                 ptid.error( "will not perform heterogeneous merge of required types: " + Joiner.on( ", " ).join( typesMerged ) );
                 continue;
             }
             */
 
             TypeConstraint tc = new TypeConstraint();
 
             for( RequiredType rt : originatorReqTypes ) {
                 tc.absorb( rt.getTypeConstraint( temporaryScheme ) );
             }
 
             for( RequiredType rt : localRtAdds.get( key ) ) {
                 tc.absorb( rt.getTypeConstraint( temporaryScheme ) );
             }
             localRtAdds.removeAll( key );
 
             // System.out.println( "creating new required type in: " + ptDeclToBeRewritten.dumpTree() );
 
             SimpleSet temporaryReqTypes = ptDeclToBeRewritten.lookupTypeInPTDecl( key );
             if( temporaryReqTypes.size() != 1 ) {
                 ptDeclToBeRewritten.error( "internal compiler error -- failed to properly create temporary RT (expected 1 found " + temporaryReqTypes.size() + ")" );
                 continue;
             }
             ASTNode temporaryReqType = (ASTNode) temporaryReqTypes.iterator().next();
             if( !(temporaryReqType instanceof RequiredType) ) {
                 temporaryReqType.error( "[implementation restriction, issue 7] [TODO] classes cannot currently share names with incoming required types -- give the class another name" );
                 continue;
             }
             // System.out.println( "replacing temporary reqtype of class " + temporaryReqType.getClass().getName() );
 
             RequiredType myRequiredType = JastaddTypeConstraints.convertToRequiredType( key, tc, ptDeclToBeRewritten.getPTDeclContext() );
 
             temporaryReqType.replaceSelfWith( myRequiredType );
         }
 
         for( String key : localRtAdds.keySet() ) {
             for( RequiredType rt : localRtAdds.get( key ) ) {
                 rt.error( "required type-adds to nonexistent required type " + key );
             }
         }
     }
 /*
 
     protected void createMergedRequiredTypes() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Set<String> localRequiredTypes = new HashSet<String>();
 
         for( RequiredType rt : ptDeclToBeRewritten.getRequiredTypeList() ) {
             localRequiredTypes.add( rt.getID() );
         }
 
         Set<String> missingRTNames = Sets.difference( getDestinationIDsForRequiredTypes(), localRequiredTypes );
 
         for( String name : missingRTNames ) {
         }
     }
 */
 
 	protected void createMergedInterfaces() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Set<String> addInterfaces = ptDeclToBeRewritten.getAdditionInterfaceNamesSet();
         Set<String> missingAddsInterfaceNames = Sets.difference( getDestinationIDsForInterfaces(), addInterfaces );
 
         Map<String,String> internalInterfacesBeingRenamed = new java.util.HashMap<String,String>();
 
         for( String dest : destinationClassIDsWithInstTuples.keySet() ) {
             for( PTInstTuple ptit : destinationClassIDsWithInstTuples.get( dest ) ) {
                 internalInterfacesBeingRenamed.put( ptit.getOrgID(), dest );
             }
         }
 
         Set<String> genericNames = new HashSet<String>();
 
         for(String name : missingAddsInterfaceNames ) {
             InterfaceDecl ptiad = createInterfaceAddsDeclForName( name, getDestinationClassIDsWithInstTuples().get( name ) );
             ptDeclToBeRewritten.addInterfaceDecl( ptiad );
             if( ptiad.isGenericType() ) {
                 genericNames.add( name );
             }
         }
 
         Map<TypeDecl, Access> renamingMap = new java.util.LinkedHashMap<TypeDecl, Access> ();
 
         for( String name : getDestinationIDsForInterfaces() ) {
             Collection<PTInstTuple> ituples = getDestinationClassIDsWithInstTuples().get( name );
             InterfaceDecl target = null;
             if( genericNames.contains( name ) ) {
                 target = ptDeclToBeRewritten.lookupAddsGenericInterface( name );
             } else {
                 target = ptDeclToBeRewritten.lookupAddsInterface( name );
             }
 
             TypeConstraint otc = JastaddTypeConstraints.fromInterfaceDecl( target, new ConcretificationScheme( ptDeclToBeRewritten.getPTDeclContext() ) );
             TypeConstraint tc = new TypeConstraint();
             boolean hadAddsInterface = !missingAddsInterfaceNames.contains( name );
             boolean printDebugStuff = false;
 
             for(PTInstTuple ituple : ituples) {
                 InterfaceDecl idecl = new InstTupleRew( ituple ).getRenamedSourceInterface();
                 InterfaceDecl unrenamedOriginal = (InterfaceDecl) ituple.getOriginator();
 
                 TypeConstraint additional = new TypeConstraint();
 
                 // the unrenamed still has access to supertypes (unrenamed, obviously!)
                 // the renamed has the correct internals
                 // we do not need any recursion as we're not going to use constraint-checking here.
                 // so this is fromInterfaceDeclInto light.
 
                 for( Object superio : unrenamedOriginal.getSuperInterfaceIdList() ) {
                     AST.Access myUnrenamedAccess = (AST.Access) ((AST.ASTNode)superio).fullCopy();
                     myUnrenamedAccess.setParent(((AST.ASTNode)superio).getParent());
                     InterfaceDecl originalDecl = (InterfaceDecl) Util.declarationFromTypeAccess( myUnrenamedAccess );
                     String originalName = originalDecl.getID();
                     String newName = internalInterfacesBeingRenamed.get( originalName );
                     if( newName != null && !originalName.equals( newName ) ) {
                         SimpleSet newInterfaces = ptDeclToBeRewritten.lookupTypeInPTDecl( newName );
                         InterfaceDecl newDecl = (InterfaceDecl) newInterfaces.iterator().next();
                         Access newAccess = newDecl.createBoundAccess();
                         renamingMap.put( originalDecl, newAccess );
                     }
 
                     additional.addImplementedType( new JastaddTypeDescriptor( (AST.Access) superio ) );
                 }
 
                 java.util.HashMap lmsm = idecl.localMethodsSignatureMap();
                 for( Object methodKey : lmsm.keySet() ) {
                     Object methodValue = lmsm.get( methodKey );
                     MethodDecl method = (MethodDecl) methodValue;
                     MethodDescriptor methodDesc = JastaddTypeConstraints.describeMethodDecl( method, new ConcretificationScheme( ptDeclToBeRewritten.getPTDeclContext() ) );
                     additional.addMethod( methodDesc );
                 }
 
                 tc.absorb( additional );
             }
 
             /*
             Set<String> addedSignatures = new HashSet<String> ();
 
             for( Object mdsig : target.localMethodsSignatureMap().keySet() ) {
                 addedSignatures.add( (String) mdsig );
             }
             */
 
             // System.out.println( tc );
 
             target.setSuperInterfaceIdList( new AST.List() );
 
             for(Iterator<TypeDescriptor> it = otc.getImplementedTypesIterator(); it.hasNext(); ) {
                 JastaddTypeDescriptor jtd = (JastaddTypeDescriptor) it.next();
                 target.addSuperInterfaceId( (AST.Access) jtd.getAccess().fullCopy() );
             }
 
             for(Iterator<TypeDescriptor> it = tc.getImplementedTypesIterator(); it.hasNext(); ) {
                 JastaddTypeDescriptor jtd = (JastaddTypeDescriptor) it.next();
                 target.addSuperInterfaceId( (AST.Access) jtd.getAccess().fullCopy() );
             }
 
             for(Iterator<MethodDescriptor> it = tc.getMethodsIterator(); it.hasNext(); ) {
                 MethodDescriptor method = it.next();
 
                 if( otc.hasMethod( method ) ) continue;
 
                 MethodDecl methDecl = JastaddTypeConstraints.simpleToMethodDecl( method );
 //                String methSig = methDecl.signature();
 //                if( !addedSignatures.contains( methSig ) ) {
 //                    addedSignatures.add( methSig );
 //                    System.out.println( "Adding new method: " + methDecl );
 
                     target.addMemberMethod( JastaddTypeConstraints.simpleToMethodDecl( method ) );
 //                } else {
 //                    System.out.println( "Ignoring duplicate method: " + methDecl );
 //                }
             }
 
             if( printDebugStuff ) {
                 int sources = ituples.size();
                 System.out.print( "From " + sources + " sources ");
                 if( hadAddsInterface ) {
                     System.out.print( "plus adds class " );
                 }
                 // System.out.println( "created interface " + name + ": " + target );
             }
         }
 
         ptDeclToBeRewritten.replaceTypeAccesses( renamingMap );
     }
 
     protected void updateAccessesToInternalRenames() {
         Map<BodyDecl, BodyDecl> virtualsToReals = new java.util.LinkedHashMap<BodyDecl, BodyDecl> ();
         for( PTInstDecl ptid : ptDeclToBeRewritten.getPTInstDecls() ) {
             for( PTInstTuple ptit : ptid.getPTInstTupleList() ) {
                 new InstTupleRew( ptit ).createVirtualRenamingDeclarations(virtualsToReals);
             }
         }
         /*
         for( SimpleClassRew simplec : simpleClasses ) {
             simplec.createVirtualRenamingDeclarations( virtualsToReals );
         }
 
         for( String name : getDestinationIDsForInterfaces() ) {
             Collection<PTInstTuple> ituples = getDestinationClassIDsWithInstTuples().get( name );
         }
         for( 
             getDestinationIDsForRequiredTypes()
 */
 
 
         ptDeclToBeRewritten.replaceInternallyRenamedAccesses( virtualsToReals );
         ptDeclToBeRewritten.removeDummyDecls( virtualsToReals.keySet() );
     }
 
 
 	/**
 	 * Needs extended classes in correct order. Minit dependencies are inherited
 	 * and therefore a superclass must be extended before its child.
 	 */
 	protected void extendAddClassesWithInstantiatons() {
 		Set<String> visited = Sets.newHashSet();
         boolean didMakeProgress;
 
 		while (visited.size() < simpleClasses.size()) {
             // This loop is a bit of a wart as it's apt to go infinite-loop
             // during testing if something is wrong elsewhere so its
             // assumptions fail, which is confusing.
             // added the didMakeProgress to fail instead of looping infinitely.
             // Better ideas welcome.
             // [example of fail: ExtendExternal test]
 
             didMakeProgress = false;
 
 			for (SimpleClassRew decl : simpleClasses) {
 				String superName = decl.getSuperClassname();
 				if (!visited.contains(decl.getName())) {
                     try {
                         if ( superName == null
                             || visited.contains(superName)
                             || !decl.hasSuperclassInternalTo( ptDeclToBeRewritten ) ) {
                             visited.add(decl.getName());
                             didMakeProgress = true;
 
                             // System.out.println( "about to extendClass " + decl.getClassDecl().getID() );
                             // System.out.println( "name has generic origins? " + (null != nameHasGenericOrigin( decl.getClassDecl().getID() ) ) );
 
                             decl.extendClass(getDestinationClassIDsWithInstTuples(),
                                              getParameterRewriter(),
                                              nameHasGenericOrigin( decl.getClassDecl().getID() ) );
                         }
                     }
                     catch( CriticalPTException e ) {
                         ptDeclToBeRewritten.error( "internal compiler error (extendAddClassesWithInstantiatons() is confused and would fail with exception)" );
                         return;
                     }
 				}
 			}
 
             if( !didMakeProgress ) {
                 /* Confusingly, throwing a CriticalPTException is not in itself an indication
                    of an error. This is perhaps for the best since it means we must call
                    .error() with something more descriptive to be output to the screen.
                 */
                 ptDeclToBeRewritten.error( "internal compiler error (extendAddClassesWithInstantiatons() is confused and would loop infinitely)" );
                 return; // TODO: this will probably lead to some confusing error messages
 //                throw new CriticalPTException( "extendAddClassesWithInstantiatons() is confused -- would loop infinitely" );
             }
 		}
 
         // TODO: here (?) is the appropriate place to fix --
         //       the different updated classes may have stale references to other's originals.
         //       see -Dname=test/compiler_semantic_tests/single_file/interface/InterfaceInTemplateRenameExplicit3.java
 	}
 
     /** Get IDs for the destination _required types_.
       */
     protected Set<String> getDestinationIDsForRequiredTypes() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Set<String> rv = new TreeSet<String>();
         for( String x : destinationClassIDsWithInstTuples.keySet() ) {
             TypeDecl typeDecl = destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator();
             if( typeDecl instanceof RequiredType ) {
                 rv.add( x );
             }
         }
         return rv;
     }
 
     // XXX note that in the next few methods we only test the first element.
     //     it is assumed that all the types are equal -- trying to merge an
     //     interface with a class is obviously nonsensical and an error.
 
     /** Get IDs for the destination _enums_.
       */
     protected Set<String> getDestinationIDsForEnums() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Set<String> rv = new TreeSet<String>();
         for( String x : destinationClassIDsWithInstTuples.keySet() ) {
             TypeDecl typeDecl = destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator();
             if( typeDecl instanceof EnumDecl ) {
                 rv.add( x );
             }
         }
         return rv;
     }
 
     protected Multimap<String,ClassDecl> getDestinationForExtendingExternals() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Multimap<String,ClassDecl> rv = HashMultimap.create();
         for( String x : destinationClassIDsWithInstTuples.keySet() ) {
             for( PTInstTuple ptit : destinationClassIDsWithInstTuples.get(x) ) {
                 TypeDecl typeDecl = ptit.getOriginator();
                 if( typeDecl instanceof EnumDecl ) continue;
                 if( typeDecl instanceof ClassDecl ) {
                     ClassDecl cd = (ClassDecl) typeDecl;
                     if( cd.getModifiers().isExtendsExternal() ) {
                         rv.put( x, cd );
                     }
                 }
             }
         }
         return rv;
     }
 
 
     /** Get IDs for the destination _classes_ -- that is, stripping away
       * the IDs of any interfaces (and enums, although they are technically
       * classdecls) among the renamed elements.
       */
 
     protected Set<String> getDestinationIDsForNonEnumClasses() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Set<String> rv = new TreeSet<String>();
         for( String x : destinationClassIDsWithInstTuples.keySet() ) {
             TypeDecl typeDecl = destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator();
             if( typeDecl instanceof EnumDecl ) continue;
             if( typeDecl instanceof ClassDecl ) {
                 rv.add( x );
             }
         }
         return rv;
     }
 
     /** Get IDs for the destination _interfaces_.
       */
 
     protected Set<String> getDestinationIDsForInterfaces() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Set<String> rv = new TreeSet<String>();
         for( String x : destinationClassIDsWithInstTuples.keySet() ) {
             if( destinationClassIDsWithInstTuples.get(x).iterator().next().getOriginator() instanceof InterfaceDecl ) {
                 rv.add( x );
             }
         }
         return rv;
     }
 
     /** Get a renamed enum by (new) name.
         
         Note: this assumes that enums cannot be merged. That's reasonable, I think?
      */
 
     protected PTEnumDecl getRenamedEnumByName(String name) {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         PTInstTuple tup = Iterables.getOnlyElement( destinationClassIDsWithInstTuples.get(name));
         return new InstTupleRew(tup).getRenamedSourceEnum();
     }
 
     /** Get a renamed interface by (new) name.
 
         Note: this assumes that interfaces cannot be merged. This is an invalid assumption, do not use. (Method to be removed.)
      */
 
     protected InterfaceDecl getRenamedInterfaceByName(String name) {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
         Set<String> rv = new TreeSet<String>();
         PTInstTuple tup = Iterables.getOnlyElement( destinationClassIDsWithInstTuples.get(name));
         return new InstTupleRew(tup).getRenamedSourceInterface();
     }
 
     public AST.List<AST.TypeVariable> nameHasGenericOrigin( String name ) {
         java.util.Collection<PTInstTuple> instTuples = getDestinationClassIDsWithInstTuples().get( name );
 
         TypeDecl gtype = null;
         int genericOrigins = 0, nonGenericOrigins = 0;
         for( PTInstTuple instTuple : instTuples ) {
             if( instTuple.getOriginator().isGenericType() ) {
                 genericOrigins++;
                 gtype = instTuple.getOriginator();
             } else {
                 nonGenericOrigins++;
             }
         }
 
         if( genericOrigins == 0 ) {
             return null;
         } else if( genericOrigins == 1 && nonGenericOrigins == 0 ) {
             if( gtype instanceof GenericClassDecl ) {
                 return ((GenericClassDecl)gtype).getTypeParameterList().fullCopy();
             }
             if( gtype instanceof GenericInterfaceDecl ) {
                 return ((GenericInterfaceDecl)gtype).getTypeParameterList().fullCopy();
             }
             // more? see index
             throw new CriticalPTException( "single-generic origin type unhandled: " + gtype.getClass().getName() );
         } else if( genericOrigins == 1 && nonGenericOrigins != 0 ) {
             // C<A> + D --> CD<A>
             // hard to handle -- we now need to change all type accesses (in D, because they may be "A")
 
             gtype.error( "merge target " + name + " has generic and non-generic origins [not implemented yet]" );
             throw new CriticalPTException( "mixed-generic origins, giving up" );
         } else if( genericOrigins > 1 && nonGenericOrigins == 0 ) {
             AST.List<TypeVariable> referenceTypePars = null;
             if( gtype instanceof GenericClassDecl ) {
                 referenceTypePars = ((GenericClassDecl)gtype).getTypeParameterList().fullCopy();
                referenceTypePars.setParent(((GenericClassDecl)gtype).getTypeParameterList().getParent());
             }
             if( gtype instanceof GenericInterfaceDecl ) {
                 referenceTypePars = ((GenericInterfaceDecl)gtype).getTypeParameterList().fullCopy();
                referenceTypePars.setParent(((GenericInterfaceDecl)gtype).getTypeParameterList().getParent());
             }
             // System.out.println( "checking " + referenceTypePars.dumpTree() );
 
             for( PTInstTuple instTuple : instTuples ) {
                 TypeDecl otherGType = instTuple.getOriginator();
                 AST.List<TypeVariable> candidateTypePars = null;
                 if( otherGType instanceof GenericClassDecl ) {
                     candidateTypePars = ((GenericClassDecl)otherGType).getTypeParameterList().fullCopy();
                    candidateTypePars.setParent(((GenericClassDecl)otherGType).getTypeParameterList().getParent());
                 }
                 if( otherGType instanceof GenericInterfaceDecl ) {
                     candidateTypePars = ((GenericInterfaceDecl)otherGType).getTypeParameterList().fullCopy();
                    candidateTypePars.setParent(((GenericInterfaceDecl)otherGType).getTypeParameterList().getParent());
                 }
 
                 // System.out.println( "against " + candidateTypePars.dumpTree() );
 
                 boolean listsEqual = false;
                 if( referenceTypePars.getNumChild() == candidateTypePars.getNumChild() ) {
                     int i;
                     for(i=0;i< referenceTypePars.getNumChild(); i++ ) {
                         TypeVariable x = (TypeVariable) referenceTypePars.getChild(i);
                         TypeVariable y = (TypeVariable) candidateTypePars.getChild(i);
                         if( !( x.instanceOf( y ) && y.instanceOf( x ) ) ) {
                             // System.out.println( "mismatch on parameter " + i + ": " + x.dumpTree() + " versus " + y.dumpTree() );
                             break;
                         }
                     }
                     if( i == referenceTypePars.getNumChild() ) {
                         listsEqual = true;
                     }
                 }
 
                 if( !listsEqual ) {
                     throw new CriticalPTException( "generic origin consistency check failed, giving up" );
                 }
             }
 
             return referenceTypePars;
         } else {
             gtype.error( "merge target " + name + " has multiple generic origins" );
             throw new CriticalPTException( "multiple generic origins, giving up (" + genericOrigins + " generic, " + nonGenericOrigins + " non-generic)" );
         }
 
         // TODO: these exceptions should be caught and translated to an error message, weren't they in the past?
     }
 
     protected InterfaceDecl createInterfaceAddsDeclForName( String name, java.util.Collection<PTInstTuple> instTuples) {
         Modifiers mods = new Modifiers();
 
         AST.List<AST.TypeVariable> typePars = nameHasGenericOrigin( name );
         if( typePars == null ) {
             return new PTInterfaceAddsDecl( mods, name, new List<Access>(), new List<BodyDecl>(), new List() );
         } else {
             return new PTGenericInterfaceAddsDecl( mods, name, new List<Access>(), new List<BodyDecl>(), typePars, new List() );
         }
     }
 
 
     protected ClassDecl createClassDeclForName( String name, java.util.Collection<PTInstTuple> instTuples, boolean addExtendsExternal, Access superClassAccess ) {
         Modifiers mods = new Modifiers();
 
         if( addExtendsExternal ) {
             mods.addModifier( new Modifier( "extendsexternal" ) );
         }
 
         Opt<Access> optAccess;
 
         if( superClassAccess != null ) {
             optAccess = new Opt<Access>( superClassAccess );
         } else {
             optAccess = new Opt<Access>();
         }
 
 
         GenericClassDecl gtype = null;
         int genericOrigins = 0, nonGenericOrigins = 0;
         for( PTInstTuple instTuple : instTuples ) {
             if( instTuple.getOriginator().isGenericType() ) {
                 genericOrigins++;
                 gtype = (GenericClassDecl) instTuple.getOriginator();
             } else {
                 nonGenericOrigins++;
             }
         }
 
         ClassDecl cls;
         AST.List<AST.TypeVariable> typePars = nameHasGenericOrigin( name );
 
         if( typePars == null ) {
             cls = new ClassDecl(mods,
                                 name,
                                 optAccess,
                                 new List<Access>(),
                                 new List<BodyDecl>());
         } else {
             cls = new GenericClassDecl( mods,
                                         name,
                                         optAccess,
                                         new List<Access>(),
                                         new List<BodyDecl>(),
                                         typePars.fullCopy() );
         }
 
         // TODO eliminate code duplication w/ nameHasGenericOrigin
 /*
         
         if( genericOrigins == 0 ) {
             cls = new ClassDecl(mods,
                                 name,
                                 optAccess,
                                 new List<Access>(),
                                 new List<BodyDecl>());
         } else if( genericOrigins == 1 && nonGenericOrigins == 0 ) {
             // this is simple to handle -- copy the arguments
             cls = new GenericClassDecl( mods,
                                         name,
                                         optAccess,
                                         new List<Access>(),
                                         new List<BodyDecl>(),
                                         gtype.getTypeParameterList().fullCopy() );
         } else if( genericOrigins == 1 && nonGenericOrigins != 0 ) {
             // C<A> + D --> CD<A>
             // hard to handle -- we now need to change all type accesses 
 
             gtype.error( "merge target " + name + " has generic and non-generic origins [not implemented yet]" );
             throw new CriticalPTException( "mixed-generic origins, giving up" );
         } else {
             // problems with multiple generics: C<A> + D<B> --> CD<?,?> (order?)
             // this seems fundamental
 
             gtype.error( "merge target " + name + " has multiple generic origins" );
             throw new CriticalPTException( "multiple generic origins, giving up" );
         }
 */
 
         /* Here's an exception to these classes being empty: if we have just one originator, and
            it has at least one constructor with positive arity, we will create redirecting constructors.
         */
         if( (genericOrigins+nonGenericOrigins) == 1 ) {
             PTInstTuple instTuple = instTuples.iterator().next();
             PTInstDecl instDecl = (PTInstDecl) instTuple.getParentClass( PTInstDecl.class );
             ClassDecl origin = (ClassDecl) instTuple.getOriginator();
 
             boolean doCreateRedirectingConstructors = false;
 
             for( ASTNode node : origin.getConstructorDeclList() ) {
                 ConstructorDecl constructor = (ConstructorDecl) node;
                 if( constructor.arity() != 0 ) {
                     doCreateRedirectingConstructors = true;
                 }
             }
 
             if( doCreateRedirectingConstructors ) {
                 for( ASTNode node : origin.getConstructorDeclList() ) {
                     ConstructorDecl constructor = (ConstructorDecl) node;
 
                     Modifiers newMods = constructor.getModifiers().fullCopy();
                     String constructorName = name; // the renamed class name, not the original constructor name, doh
                     List<ParameterDeclaration> constructorParameters = constructor.getParameterList().fullCopy();
                     List<Access> constructorThrows  = constructor.getExceptions().fullCopy();
                     Opt<Stmt> constructorInvocation = constructor.getConstructorInvocationOpt().fullCopy();
                     Block emptyBody = new Block();
 
                     TemplateClassIdentifier tci = TemplateClassIdentifier.extractFrom( instDecl, origin.getID() );
                     List<Expr> actualParameters = new AST.List<Expr>();
 
                     for( ParameterDeclaration pdecl : constructorParameters ) {
                         actualParameters.add( new VarAccess( pdecl.getID() ) );
                     }
 
                     PTTSuperConstructorCall explicitInvocation = new PTTSuperConstructorCall( tci, actualParameters );
 
                     List<PTTSuperConstructorCall> explicitInvocations = new AST.List<PTTSuperConstructorCall>();
                     explicitInvocations.add( explicitInvocation );
 
                     // System.out.println( "creating PTConstructorDecl named " + constructorName );
                     PTConstructorDecl myConstructor = new PTConstructorDecl( newMods,
                                                                              constructorName,
                                                                              constructorParameters,
                                                                              constructorThrows,
                                                                              constructorInvocation,
                                                                              emptyBody,
                                                                              explicitInvocations );
                     cls.addBodyDecl( myConstructor );
                 }
             }
         }
 
 
         return cls;
     }
 
 	protected void createEmptyMissingAddClasses() {
 
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = getDestinationClassIDsWithInstTuples();
 		Builder<SimpleClassRew> lb = ImmutableList.builder();
 
 		/*
 		 * Add the explicitly defined classes residing in this PTDecl to our
 		 * rewrite list.
 		 */
 		for (SimpleClass decl : ptDeclToBeRewritten.getSimpleClassList()) {
 			lb.add(new SimpleClassRew(decl));
         }
 		/*
 		 * Does SimpleClassRew really need the whole
 		 * destinationClassIDsWithInstTuples? Why?
 		 */
 
 		/*
 		 * For those classes which are implicitly defined by inst clauses and
 		 * where no concrete destination class is found, add an empty adds
 		 * class. For example: package P { inst T with A => X; // no class X
 		 * adds .. given later }
 		 */
 		Set<String> addClasses = ptDeclToBeRewritten.getAdditionClassNamesSet();
         Multimap<String, ClassDecl> extendingExternalsClasses = getDestinationForExtendingExternals();
 		Set<String> missingAddsClass =
                 Sets.difference(
                 Sets.difference( getDestinationIDsForNonEnumClasses(),
                                  addClasses ),
                                  extendingExternalsClasses.keySet() );
         for(String name : extendingExternalsClasses.keySet() ) {
             /* TODO cleanup, why are these treated separatedly? led to a bug, see continue workaround below */
 
             /* I believe checking for clashes is done elsewhere, so here we just take the FIRST
                extends-external class.
                TODO make this deterministic. (Note that it is only nondeterministic if there IS
                another error, though.)
             */
 
             if( ! Sets.difference( getDestinationIDsForNonEnumClasses(), addClasses ).contains( name ) ) {
                 continue;
             }
 
             Access superClassAccess = (Access) extendingExternalsClasses.get(name).iterator().next().getSuperClassAccess().fullCopy();
 
             ClassDecl cls = createClassDeclForName( name, destinationClassIDsWithInstTuples.get( name ), true, superClassAccess );
 
             AST.Opt<Access> redundantSuperclass = new AST.Opt<Access>();
             AST.List<Access> redundantInterfaces = new AST.List<Access>();
 
 			PTClassAddsDecl addClass = new PTClassAddsDecl(cls, redundantSuperclass, redundantInterfaces);
 			ptDeclToBeRewritten.addSimpleClass(addClass);
 			lb.add(new SimpleClassRew(addClass));
         }
 
 		for (String name : missingAddsClass) {
             ClassDecl cls = createClassDeclForName( name, destinationClassIDsWithInstTuples.get( name ), false, null );
             AST.Opt<Access> redundantSuperclass = new AST.Opt<Access>();
             AST.List<Access> redundantInterfaces = new AST.List<Access>();
 
 			PTClassAddsDecl addClass = new PTClassAddsDecl(cls, redundantSuperclass, redundantInterfaces);
 			addClass.setWasAddsClass(false);
 
 			ptDeclToBeRewritten.addSimpleClass(addClass); /* Code understanding note:
                                                              this is the line where the actual magic happens;
                                                              something is added to the AST. "simpleClasses"
                                                              just keeps track of rewriters internally which
                                                              have references to this addClass.
                                                              addSimpleClass() is a method generated by the
                                                              parser. */
 
 			lb.add(new SimpleClassRew(addClass));
 		}
 		simpleClasses = lb.build();
 	}
 
 	/**
 	 * returns a multimap where the key is DestinationClassID (String) and the
 	 * value is a list of InstTuples (for example A => X). Per key there may be
 	 * more than one InstTuple if we are merging several source classes.
 	 */
 	private Multimap<String, PTInstTuple> getDestinationClassIDsWithInstTuples() {
 		Multimap<String, PTInstTuple> destinationClassIDsWithInstTuples = HashMultimap.create();
 		for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
 			for (PTInstTuple instTuple : templateInst.getPTInstTupleList()) {
 				destinationClassIDsWithInstTuples.put(instTuple.getID(),
 						instTuple);
 			}
 		}
 		return destinationClassIDsWithInstTuples;
 	}
 
     public void markReadyForNameResolution() {
         ptDeclToBeRewritten.markReadyForNameResolution();
     }
 
     public boolean isPackage() {
         return ptDeclToBeRewritten instanceof PTPackage;
     }
 
     public boolean isTemplate() {
         return ptDeclToBeRewritten instanceof PTTemplate;
     }
 
 	public void createInitIfPackage() {
         if( !isPackage() ) return;
 /* // obsolete?
         String dummyName = addDummyClass();
 */
         for (SimpleClassRew x : simpleClasses) {
 /*
             if( !x.inheritsFromExtendsExternal() ) {
                 x.createInitConstructor(dummyName);
                 x.createDummyConstructor(dummyName);
             }
 */
         /* With backwards-E, things become more explicit.
            Package classes call super, then tsupers.
            Template classes call tsupers.
 
            The two things to note are:
              - Template class MAY NOT call super, even though
                the normal Java rule is that they HAVE to call
                super.
              - We must recognize the syntax
                tsuper[TemplateName](args).
                This must be converted to a minit call when
                recognized in a constructor position.
              - We need to _require_ tsuper in some places,
                so we can generate the appropriate constructors.
         */
             x.rewriteConstructorsInPackage();
         }
 	}
 
 	private String addDummyClass() {
 		String dummyName = "DUMMY$"; // TODO something better?
 		ClassDecl dummy = new ClassDecl(new Modifiers(), dummyName, new Opt(),
 				new List(), new List());
 		ptDeclToBeRewritten.getSimpleClassList().add(new PTClassDecl(dummy));
 		return dummyName;
 	}
 
     /* Find, by destination name, a class being instantiated within a
        template being instantiated (this is not the same as the class this
        class will end up being copied into). This class is within the template
        part of the AST and should not be mutated. It is useful for
        resolving self-references in type parameters.
     */
 
     public ClassDecl getPrecopyClass( String className ) {
         for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
             for(PTInstTuple instTup : templateInst.getPTInstTuples()) {
                 if( !instTup.getID().equals( className ) ) continue;
 
                 SimpleSet typeDecs = templateInst.getTemplate().ptLookupTypeIn( instTup.getOrgID() );
 
                 Iterator<TypeDecl> i = typeDecs.iterator();
                 // ambiguity is really an error, but should be dealt with elsewhere
                 while( i.hasNext() ) {
                     TypeDecl td = i.next();
                     if( td instanceof ClassDecl ) {
                         return (ClassDecl) td;
                     }
                 }
             }
         }
         return null;
     }
 
     /* Find, by destination name, an interface being instantiated within a
        template being instantiated. See equivalent for classes above.
     */
 
     public InterfaceDecl getPrecopyInterface( String className ) {
         for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
             for(PTInstTuple instTup : templateInst.getPTInstTuples()) {
                 if( !instTup.getID().equals( className ) ) continue;
 
                 SimpleSet typeDecs = templateInst.getTemplate().ptLookupTypeIn( instTup.getOrgID() );
 
                 Iterator<TypeDecl> i = typeDecs.iterator();
                 // ambiguity is really an error, but should be dealt with elsewhere
                 while( i.hasNext() ) {
                     TypeDecl td = i.next();
                     if( td instanceof InterfaceDecl ) {
                         return (InterfaceDecl) td;
                     }
                 }
             }
         }
         return null;
     }
 
 
     /* Add the extended classes of a class (we consider a class as extending itself)
        to a set. This is nontrivial because the classes extended might not be
        local yet -- classes referenced may be inside templates, in which case the
        class in the template should be returned. (As such, this may not give
        the expected results post-copy.) This is useful for self-referencing
        type parameters.
     */
 
     public void precopyAddExtendedClassesOf( ClassDecl classDecl, Set<ClassDecl> extendedClasses ) {
         boolean didTryTemplateLookup = false;
         extendedClasses.add( classDecl );
         ClassDecl x = classDecl;
         while( x.hasSuperclass() ) {
             if( !x.superclass().isUnknown() ) {
                 x = x.superclass();
                 extendedClasses.add( x );
             } else if( !didTryTemplateLookup ) {
                 // lookup failed. try by name.. but only once (if this works, any further references won't be to something instantiated in this ptdecl)
                 ClassDecl cd = getPrecopyClass( x.getSuperClassName() );
                 if( cd == null ) {
                     // certainly an error, but should probably be caught/reported somewhere else.
                     // 1l 123456789
                     break;
                 }
                 x = cd; // continue exploring superclasses
                 extendedClasses.add( cd );
                 didTryTemplateLookup = true;
             } else {
                 classDecl.warning( "compiler confused while computing pre-copy superclasses" );
                 break;
             }
         }
     }
 
     public void removeInstStatements() {
         int i = 0;
         ptDeclToBeRewritten.setPTInstDeclList( new AST.List<PTInstDecl> () );
         /*
         while( i < ptDeclToBeRewritten.getNumChild() ) {
             if( ptDeclToBeRewritten.getChild(i) instanceof PTInstDecl ) {
                 System.out.println( "removing child "  + ptDeclToBeRewritten.getChild(i) );
                 ptDeclToBeRewritten.removeChild( i );
             } else {
                 i++;
             }
         }
         */
     }
 
     public ParameterRewriter getParameterRewriter() {
         if( paramRewriter == null ) {
             ParameterRewriter pr = new ParameterRewriter();
 
             for (PTInstDecl templateInst : ptDeclToBeRewritten.getPTInstDecls()) {
                 PTTemplate ptt = templateInst.getTemplate();
 
                 List formalp = ptt.getTypeParameterList();
                 List actualp = templateInst.getTypeArgumentList();
 
 
                 if( formalp.getNumChild() != actualp.getNumChild() ) {
                     templateInst.error( "arity mismatch when instantiating " + templateInst.getID() + ": takes " + formalp.getNumChild() + " type argument(s), not " + actualp.getNumChild() );
                 }
 
                 Iterator fpi = formalp.iterator();
                 Iterator api = actualp.iterator();
                 
                 int argcount = 0;
 
                 // this loop is basically zipWith, more elegant way to write in Java?
                 while( fpi.hasNext() && api.hasNext() ) {
                     TypeVariable fparam = (TypeVariable) fpi.next();
                     TypeAccess aparam = (TypeAccess) api.next();
                     argcount++;
 
                     TypeDecl tdecl = aparam.decl();
 
                     // check constraints:
                     //   - if fparam claims to inherit from a class C,
                     //     tdecl must represent a class V and V must
                     //     inherit from C.
                     //   - for any interface I fparam claims to implement,
                     //     tdecl must represent a class C and C must
                     //     implement I
 
                     // this may be complicated by the fact that tdecl
                     // might be either a pt-class or a non-pt class. (?)
                     // (never seems to be a SimpleClass, always (rewritten
                     //  to a?) plain ClassDecl. figure out why, make
                     //  sure this is not dangerously ordering-dependent)
                     if( !(tdecl instanceof ClassDecl) ) {
                         templateInst.error( "when instantiating " + templateInst.getID() + ", argument " + argcount + " does not satisfy constraints: does not refer to a class" );
                         continue;
                     }
                     ClassDecl classDecl = (ClassDecl) tdecl;
 
                     HashSet<ClassDecl> extendedClasses = new LinkedHashSet<ClassDecl>();
                     precopyAddExtendedClassesOf( classDecl, extendedClasses );
 
                     HashSet<InterfaceDecl> implementedInterfaces = new LinkedHashSet<InterfaceDecl>();
                     for( Access a : classDecl.getImplementsList() ) {
                         if( a instanceof TypeAccess ) {
                             TypeAccess ta = (TypeAccess) a;
                             String name = ta.name();
                             TypeDecl td = ta.decl();
                             if( td.isUnknown() ) {
                                 InterfaceDecl id = getPrecopyInterface( name );
                                 if( id != null ) {
                                     implementedInterfaces.add( id );
                                 }
                             } else if( td instanceof InterfaceDecl ) {
                                 implementedInterfaces.add( (InterfaceDecl) td );
                             } else {
                                 // implementing non-interface. error elsewhere.
                             }
                         } else if( a instanceof ParTypeAccess ) {
                             ParTypeAccess pta = (ParTypeAccess) a;
                             TypeDecl td = pta.genericDecl();
                             if( !td.isUnknown() && td instanceof GenericInterfaceDecl ) {
                                 // TODO: what are we actually to do with this GenericInterfaceDecl?
                                 //       what we probably want is what is usually in .implementedInterfaces(),
                                 //       which is a ParInterfaceDecl (parametrized interface decl?).
                                 //       requires further study of interfaces (and I think this
                                 //       is more or less the same problem as issue 13)
                                 implementedInterfaces.add( (InterfaceDecl) td );
                             }
                         } else {
                             classDecl.warning( "compiler confused -- something unexpected in implements-list: " + a.getClass().getName() );
                         }
                     }
 
                     boolean okay = true;
 
                     for( Access constraintAcc : fparam.getTypeBoundList() ) {
                         TypeDecl constraint = constraintAcc.type();
                         if( constraint.isClassDecl() ) {
                             if( !extendedClasses.contains( constraint ) ) {
                                 templateInst.error( "when instantiating " + templateInst.getID() + ", argument " + argcount + " does not satisfy constraints: does not extend class " + constraint.fullName() );
                                 okay = false;
                             }
                         }
                         if( constraint.isInterfaceDecl() ) {
                             if( !implementedInterfaces.contains( constraint ) ) {
                                 templateInst.error( "when instantiating " + templateInst.getID() + ", argument " + argcount + " does not satisfy constraints: does not implement interface " + constraint.fullName() );
                                 okay = false;
                             }
                         }
                     }
 
                     // we try to add as many rewrites as possible, even if
                     // we've encountered errors along the way.
                     // this seems to give more informative error messages
                     // (and eliminates in some but not all cases confusing
                     //  ones about there being no visible type with the
                     //  type variable name (in the generated Java AST))
 
                     pr.addRewrite( fparam, aparam );
                 }
             }
 
             paramRewriter = pr;
         }
         assert( paramRewriter != null );
         return paramRewriter;
     }
 
     /** Get the before-rename IDs (the names in the template context) all
       * the interfaces and classes extended by the class or interface being
       * renamed that are inside the same template.
       *
       * This is useful because an explicit rename (e.g. method rename) in one
       * of these roots should mean an implicit rename here.
       **/
     private static Set<String> getRootIDsOf( TypeDecl t ) {
         PTDecl myEnclosingDecl = (PTDecl) t.getParentClass( PTDecl.class );
         assert( myEnclosingDecl instanceof PTTemplate );
 
         Set<String> rv = new HashSet<String>();
 
         if( t instanceof RequiredType ) {
             RequiredType rt = (RequiredType) t;
             if( rt.hasSuperTypeAccess() ) {
                 TypeDecl td = Util.declarationFromTypeAccess( rt.getSuperTypeAccess() );
                 if( td.getParentClass( PTDecl.class ) == myEnclosingDecl ) {
                     rv.add( td.getID() );
                 }
             }
 
             for( Access a : rt.getImplementsList() ) {
                 TypeDecl td = Util.declarationFromTypeAccess( a );
                 if( td.getParentClass( PTDecl.class ) == myEnclosingDecl ) {
                     rv.add( td.getID() );
                 }
             }
         }
 
         for( Object o : t.implementedInterfaces() ) {
             InterfaceDecl idecl = (InterfaceDecl) o;
             PTDecl enclosingDecl = (PTDecl) idecl.getParentClass( PTDecl.class );
             // be aware that enclosingDecl might well be null!
 
             if( enclosingDecl == myEnclosingDecl ) {
                 rv.add( idecl.getID() );
             }
         }
 
         if( t instanceof ClassDecl ) {
             ClassDecl cd = ((ClassDecl) t).superclass();
 
             while( cd != null ) {
                 PTDecl enclosingDecl = (PTDecl) cd.getParentClass( PTDecl.class );
                 if( enclosingDecl != myEnclosingDecl ) {
                     break;
                 }
 
                 rv.add( cd.getID() );
 
                 cd = cd.superclass();
             }
         }
 
         return rv;
     }
 
     private static PTInstTuple getPTInstTupleByOriginatorName( PTInstDecl ptid, String name ) {
         for( PTInstTuple ptit : ptid.getPTInstTupleList() ) {
             if( ptit.getOrgID().equals( name ) ) {
                 return ptit;
             }
         }
         return null;
     }
 
     private void addImpliedRenamesToPTInstDecl( PTInstDecl instDecl ) {
         for( PTInstTuple ptit : instDecl.getPTInstTupleList() ) {
             TypeDecl base = ptit.getOriginator();
 
             if( base == null ) { // something is very wrong! unable to proceed, this might be a duplicate error (?)
                 ptit.error( "unable to find instantiated class for inst: " + instDecl );
                 continue;
             }
 
             String baseID = base.getID();
             Set<String> rootIDs = getRootIDsOf( base );
 
             for( String rootID : rootIDs ) {
                 PTInstTuple ptitRoot = getPTInstTupleByOriginatorName( instDecl, rootID );
                 if( ptitRoot != null ) {
                     for( PTDummyRename ptdr : ptitRoot.getPTDummyRenameList() ) {
                         String orgId = ptdr.getOrgID();
                         boolean overspecified = false;
 
                         /* TODO CHECK should field renames be inherited like this at all? */
                         // Conclusion: no, I don't think so
                         if( ptdr instanceof PTFieldRename ) {
                             continue;
                         }
 
                         /* First check whether this is a field/method we actually have in the class/interface
                            (locally, as in "physically" code-wise there, not just inherited) -- otherwise
                            it doesn't need to be renamed here.
                         */
                         if( ptdr instanceof PTMethodRenameAll ) {
                             boolean found = false;
                             for( Object o : base.localMethodsSignatureMap().values() ) {
                                 MethodDecl md = (MethodDecl) o;
                                 if( md.getID().equals( ptdr.getOrgID() ) ) {
                                     found = true;
                                     break;
                                 }
                             }
                             if( !found ) {
                                 continue;
                             }
                         } else if( ptdr instanceof PTMethodRename ) {
                             PTMethodRename ptmr = (PTMethodRename) ptdr;
                             boolean found = false;
                             for( Object o : base.localMethodsSignatureMap().values() ) {
                                 MethodDecl md = (MethodDecl) o;
                                 if( md.getPTEarlySignature().equals( ptmr.getOldSignature() ) ) {
                                     found = true;
                                     break;
                                 }
                             }
                             if( !found ) {
                                 continue;
                             }
                         } else {
                             assert( ptdr instanceof PTFieldRename );
                             boolean found = !base.localFields( ptdr.getOrgID() ).isEmpty();
                             if( ! found ) {
                                 continue;
                             }
                         }
 
                         for( PTDummyRename ptdrExisting : ptit.getPTDummyRenameList() ) {
                             if( orgId.equals( ptdrExisting.getOrgID() ) ) {
                                 /* We need to check that these are consistent, otherwise this
                                    is an error. */
 
                                 if( (((ptdrExisting instanceof PTMethodRename) || (ptdrExisting instanceof PTMethodRenameAll) )
                                       && (ptdr instanceof PTFieldRename) )
                                     ||
                                     (((ptdr instanceof PTMethodRename) || (ptdr instanceof PTMethodRenameAll)) && (ptdrExisting instanceof PTFieldRename))
                                     ) {
                                     continue;
                                 } else if( (ptdr instanceof PTFieldRename) && (ptdr instanceof PTFieldRename ) ) {
                                     if( ptdr.getID().equals( ptdrExisting.getID() ) ) {
                                         overspecified = true;
                                         continue;
                                     }
                                 } else if( (ptdrExisting instanceof PTMethodRenameAll) || (ptdr instanceof PTMethodRenameAll) ) {
                                     // logic feels a bit too clever, CHECK covers everything?
                                     // this is supposed to cover three cases: all/all, one/all, all/one.
                                     if( ptdrExisting.getID().equals( ptdr.getID() ) ) {
                                         overspecified = true;
                                         continue;
                                     }
                                 } else {
                                     assert( ptdr instanceof PTMethodRename );
                                     assert( ptdrExisting instanceof PTMethodRename );
 
                                     PTMethodRename ptmr = (PTMethodRename) ptdr;
                                     PTMethodRename ptmrExisting = (PTMethodRename) ptdrExisting;
 
                                     if( !ptmr.getOldSignature().equals( ptmrExisting.getOldSignature() ) ) {
                                         // these are renames for different methods, so there's no consistency issue.
                                         continue;
                                     }
 
                                     if( ptmr.getID().equals( ptmrExisting.getID() ) ) {
                                         overspecified = true;
                                         continue;
                                     }
                                 }
                             } else {
                                 continue;
                             }
 
                             // System.out.println( "NOTING ERROR" );
                             ptdrExisting.error( "" + ptdrExisting + " conflicts with implied rename from root " + rootID + ": " + ptdr );
                         }
 
                         if( overspecified ) continue;
 
                         PTDummyRename newRename = (PTDummyRename) ptdr.fullCopy();
 
                         // System.out.println( "adding implied rename " + newRename + " to baseid " + baseID );
 
                         ptit.addPTDummyRename( newRename );
                     }
                 }
             }
         }
     }
 
     private void addImpliedRenames() {
         for( PTInstDecl ptid : ptDeclToBeRewritten.getPTInstDecls() ) {
             addImpliedRenamesToPTInstDecl( ptid );
         }
     }
 }
