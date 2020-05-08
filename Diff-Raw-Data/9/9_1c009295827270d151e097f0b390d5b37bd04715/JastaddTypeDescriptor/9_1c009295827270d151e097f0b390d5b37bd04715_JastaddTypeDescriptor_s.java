 package javaworld;
 
 import java.util.List;
 import java.util.Vector;
 import java.util.Map;
 
 import AST.Access;
 import AST.ParTypeAccess;
 import AST.TypeAccess;
 import AST.TypeDecl;
 import AST.Wildcard;
 import AST.WildcardExtends;
 import AST.RequiredType;
 import AST.PTDecl;
 import AST.Program;
 import AST.ASTNode;
 import AST.ParTypeDecl;
 
 import com.google.common.base.Joiner;
 
 public class JastaddTypeDescriptor implements TypeDescriptor {
     TypeAccess typeAccess;
     ParTypeAccess parTypeAccess;
 
     boolean byDeclaration;
     TypeDecl typeDeclaration;
 
     boolean isWildcard;
     TypeDecl isWildcardExtending;
 
     List<JastaddTypeDescriptor> typeParameters;
 
     public void initializeFromAccess( Access acc ) {
         /* Note that we keep a reference to the actual access.
            We make the assumption that this is not later mutated
            in ways significant to us.
            (Making a copy extracted from the AST seems like it
             would pose a problem because we later do comparison
             by e.g. calling .decl().)
         */
 
         typeAccess = null;
         parTypeAccess = null;
 
         // System.out.println( "initializing from access " + acc.dumpTree() );
 
         typeParameters = new Vector<JastaddTypeDescriptor>();
 
         if( acc instanceof TypeAccess ) {
             typeAccess = (TypeAccess) acc;
             // System.out.println( "initializing okay with typeA " + typeAccess.dumpTree() );
         } else if( acc instanceof ParTypeAccess ) {
             parTypeAccess = (ParTypeAccess) acc;
             final int n = parTypeAccess.getNumTypeArgument();
             for(int i=0;i<n;i++) {
                 typeParameters.add( new JastaddTypeDescriptor( parTypeAccess.getTypeArgument(i) ) );
             }
         } else if( acc instanceof Wildcard ) {
             isWildcard = true;
         } else if( acc instanceof WildcardExtends ) {
             isWildcardExtending = ((WildcardExtends) acc).type();
         } else {
             throw new RuntimeException( "access of class " + acc.getClass().getName() + " passed to JastaddTypeDescriptor does not describe a type in any known way" );
         }
     }
 
     public JastaddTypeDescriptor( Access acc ) {
         initializeFromAccess( acc );
     }
 
     public JastaddTypeDescriptor( TypeDecl decl ) {
 //        if( decl instanceof ParTypeDecl ) {
         Access myConstructedAccess = (Access) decl.createQualifiedAccess().fullCopy();
         AST.List myParent = new AST.List().add( myConstructedAccess );;
         myParent.setParent( decl );
 
 //        myConstructedAccess.setParent( decl );
         
         initializeFromAccess( myConstructedAccess );
 /*
         } else {
             byDeclaration = true;
             typeDeclaration = decl;
             typeParameters = new Vector<JastaddTypeDescriptor>();
         }
 */
     }
 
     public Access getAccess() {
         if( byDeclaration ) {
             System.out.println( "[warning] constructing access from decl of class " + typeDeclaration.getClass().getName() + "(ID " + typeDeclaration.getID() + ")" );
             TypeAccess rv = new TypeAccess( typeDeclaration.fullName() );
             Access alt1 = typeDeclaration.createBoundAccess();
             Access alt2 = typeDeclaration.createQualifiedAccess();
             String packageName = typeDeclaration.packageName();
             if( typeDeclaration.getParentClass( AST.PTPackage.class ) != null ) {
                 packageName = ((AST.PTPackage)typeDeclaration.getParentClass( AST.PTPackage.class )).getID();
             }
             return new TypeAccess( packageName, typeDeclaration.name() );
         }
         if( isWildcard ) {
             new Wildcard();
         }
         if( isWildcardExtending != null ) {
             new WildcardExtends( isWildcardExtending.createBoundAccess() );
         }
         if( isParametrized() ) {
             return parTypeAccess;
         }
         return typeAccess;
     }
 
     public TypeDecl getBaseTypeDecl() {
         // System.out.println( "getting baseTypeDecl.." );
         if( isWildcardExtending != null ) {
             return isWildcardExtending;
         }
         if( byDeclaration ) {
             return typeDeclaration;
         }
         if( isParametrized() ) {
             return parTypeAccess.genericDecl();
         }
         // System.out.println( "from base access " + typeAccess.dumpTree() );
         // System.out.println( "returning " + typeAccess.decl().getID() );
         return typeAccess.decl();
     }
 
     public TypeDecl getTypeDecl() {
         if( isWildcardExtending != null ) {
             return isWildcardExtending;
         }
         if( byDeclaration ) {
             return typeDeclaration;
         }
         if( parTypeAccess != null ) {
             return parTypeAccess.type();
         }
         return typeAccess.decl();
     }
 
     public ASTNode getContext() {
         // this is a bit of a hack. essentially, we need an ASTNode to provide
         // a context wherein the Access can be evaluated, because we will be
         // _modifying_ the access and then evaluating it again when mapping
         // by the concretification scheme.
         // as this context, we use the original access (or declaration as
         // the case may be), even though it may make zero sense
         // no sense for that node to have an access as a child.
         // that's "okay" because (horrifyingly) this is a _one-way_ link;
         // the context becomes the parent of the artificially constructed
         // (and modified) node, but the modified node does not become a
         // child of the context.
         // (this is possible in JastAdd; the opposite -- a child not having
         // a parent -- is impossible)
         if( byDeclaration ) {
             return typeDeclaration;
         }
         if( parTypeAccess != null ) {
             return parTypeAccess;
         }
         return typeAccess;
     }
 
 
     public boolean isParametrized() {
         return parTypeAccess != null;
     }
 
     public int getArity() {
         return typeParameters.size();
     }
 
 
     public boolean equals( TypeDescriptor that ) {
         if( !(that instanceof JastaddTypeDescriptor) ) return false;
 
         JastaddTypeDescriptor jt = (JastaddTypeDescriptor) that;
         
         if( isWildcard != jt.isWildcard ) return false;
 
         if( isWildcardExtending != jt.isWildcardExtending ) return false;
 
         if( isParametrized() != jt.isParametrized() ) return false;
 
         if( getBaseTypeDecl() != jt.getBaseTypeDecl() ) return false;
 
         if( getArity() != jt.getArity() ) return false;
 
         for(int i=0;i<getArity();i++) {
             if( !typeParameters.get(i).equals( jt.typeParameters.get(i) ) ) return false;
         }
 
         return true;
     }
 
     public boolean isSubtypeOf( TypeDescriptor that ) {
         // note that we can be subtypes of TypeConstraints, not just other JastaddTypeDescriptors
 
         // correction: no, this is no longer possible. (must it be?)
 
         if( !(that instanceof JastaddTypeDescriptor) ) {
             return false;
         }
         JastaddTypeDescriptor jthat = (JastaddTypeDescriptor) that;
         TypeDecl myDecl = getTypeDecl();
         TypeDecl theirDecl = jthat.getTypeDecl();
 
         boolean rv =  myDecl.subtype( theirDecl );
 
         // System.out.println( "is " + myDecl.fullName() + " a subtype of " + theirDecl.fullName() + "? " + rv );
         return rv;
     }
 
     public String toString() {
         if( isWildcard ) {
             return "?";
         }
         if( isWildcardExtending != null ) {
             return "? extends " + new JastaddTypeDescriptor( isWildcardExtending );
         }
 
         StringBuilder sb = new StringBuilder();
 
         sb.append( getBaseTypeDecl().getID() );
         if( isParametrized() ) {
             sb.append( "<" );
             Joiner.on( "," ).appendTo( sb, typeParameters );
             sb.append( ">" );
         }
         return sb.toString();
     }
 
     public TypeDescriptor mapByScheme( ConcretificationScheme scheme ) {
         Access myAccess = (Access) getAccess().fullCopy();
 
         // System.out.println( "going to map " + myAccess.dumpTree() + " by a scheme" );
 
         Map<TypeDecl, Access> dtaMap = scheme.createDeclToAccessMap();
 
         // hack to make sure we can reuse this method to replace roots as well
         AST.List parent = new AST.List();
 //        parent.setParent( getContext() );
         parent.setParent( scheme.getContext() );
         parent.addChild( myAccess );
 
         parent.replaceTypeAccesses( dtaMap );
         myAccess = (Access) parent.getChild(0);
 
         // System.out.println( "this access is now " + myAccess.dumpTree() );
 
         TypeDecl myDecl = Util.declarationFromTypeAccess( myAccess );
 
         TypeDecl myDeclOther = Util.declarationFromTypeAccess( getAccess() );
 
         // System.out.println( "found declaration " + myDecl.getID() + " unknown? " + myDecl.isUnknown() );
         // System.out.println( "see also (unmapped) " + myDeclOther.getID() + " unknown? " + myDeclOther.isUnknown() );
 
         return new JastaddTypeDescriptor( myDecl );
     }
 }
