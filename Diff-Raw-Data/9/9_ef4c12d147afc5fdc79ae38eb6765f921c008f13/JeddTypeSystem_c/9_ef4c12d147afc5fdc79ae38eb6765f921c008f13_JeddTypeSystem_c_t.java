 /* Jedd - A language for implementing relations using BDDs
  * Copyright (C) 2003 Ondrej Lhotak
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package polyglot.ext.jedd.types;
 
 import polyglot.ast.*;
 import polyglot.types.*;
 import polyglot.ext.jedd.ast.*;
 import polyglot.ext.jl.ast.*;
 import polyglot.ext.jl.types.*;
 import polyglot.util.*;
 import java.util.*;
 
 public class JeddTypeSystem_c extends TypeSystem_c implements JeddTypeSystem {
     // TODO: implement new methods in JeddTypeSystem.
     // TODO: override methods as needed from TypeSystem_c.
     public BDDType BDDType( List domainPairs ) {
         return new BDDType_c( this, domainPairs );
     }
     public void checkOverride(MethodInstance mi, MethodInstance mj) throws SemanticException {
         super.checkOverride(mi, mj);
     }
     public void makeBDDFormalsConform(MethodInstance mi, MethodInstance mj) throws SemanticException {
 found_bdd:
         {
             for( Iterator tIt = mi.formalTypes().iterator(); tIt.hasNext(); ) {
                 final Type t = (Type) tIt.next();
                 if( t instanceof BDDType ) break found_bdd;
             }
             if( mi.returnType() instanceof BDDType ) break found_bdd;
             return;
         }
         MethodDecl di = (MethodDecl) instance2Decl.get(mi);
         MethodDecl dj = (MethodDecl) instance2Decl.get(mj);
         if( dj == null ) return;
         Iterator itI = di.formals().iterator();
         Iterator itJ = dj.formals().iterator();
         while(itI.hasNext()) {
             Formal fi = (Formal) itI.next();
             Formal fj = (Formal) itJ.next();
             if( !(fi.type() instanceof BDDType) ) continue;
             BDDType ti = (BDDType) fi.type();
             BDDType tj = (BDDType) fj.type();
             for( Iterator domainIt = ti.map().keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 addMustEqualEdge( DNode.v( fi.localInstance(), domain ),
                         DNode.v( fj.localInstance(), domain ) );
             }
         }
        if( mi.returnType() instanceof BDDType ) {
            BDDType ti = (BDDType) mi.returnType();
            BDDType tj = (BDDType) mj.returnType();
             for( Iterator domainIt = ti.map().keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 addMustEqualEdge( DNode.v( mi, domain ), DNode.v( mj, domain ) );
             }
         }
     }
     public boolean equals( TypeObject t1, TypeObject t2 ) {
         if( t1 instanceof BDDType 
         && t2 instanceof BDDType ) {
             BDDType bt1 = (BDDType) t1;
             BDDType bt2 = (BDDType) t2;
            return bt1.map().keySet().equals( bt2.map().keySet() );
         }
         return super.equals(t1, t2);
     }
 
     private Type getType( Object n ) {
         if( n instanceof Expr ) return ((Expr) n).type();
         if( n instanceof VarInstance ) return ((VarInstance) n).type();
         throw new InternalCompilerError( n.toString() );
     }
 
     public void physicalDomains( JeddNodeFactory nf, Collection jobs ) throws SemanticException {
         PhysDom.v().findAssignment( nf, this, jobs );
         //printDomainsDot();
         //findPhys();
     }
     /*
     public void addPhysConstraints() {
         for( Iterator exprIt = exprs.iterator(); exprIt.hasNext(); ) {
             final Object expr = (Object) exprIt.next();
             BDDType t = (BDDType) getType( expr );
             if( t == null ) {
                 throw new RuntimeException( "type of "+expr+" is null" );
             }
             for( Iterator domainIt = t.map().keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 for( Iterator domain2It = t.map().keySet().iterator(); domain2It.hasNext(); ) {
                     final Type domain2 = (Type) domain2It.next();
                     if( domain.equals( domain2 ) ) continue;
                     addConflictEdge( dnode( expr, domain ), dnode( expr, domain2 ) );
                 }
             }
         }
     }
     */
     /*
     public void addPhys() throws SemanticException {
         for( Iterator exprIt = exprs.iterator(); exprIt.hasNext(); ) {
             final Object expr = (Object) exprIt.next();
             BDDType t = (BDDType) getType( expr );
             Map map = t.map();
             for( Iterator domainIt = map.keySet().iterator(); domainIt.hasNext(); ) {
                 final Type domain = (Type) domainIt.next();
                 Type newPhys = (Type) map.get( domain );
                 if( newPhys == null ) continue;
                 Type oldPhys = dnode( expr, domain ).rep().phys;
                 if( oldPhys != null && !oldPhys.equals( newPhys ) ) {
                     if( expr instanceof Node ) {
                         throw new SemanticException( "Clash of physical domains "+oldPhys+" and "+newPhys+".", ((Node) expr).position() );
                     } else if( expr instanceof TypeObject ) {
                         throw new SemanticException( "Clash of physical domains "+oldPhys+" and "+newPhys+".", ((TypeObject) expr).position() );
                     } else {
                         throw new SemanticException( "Clash of physical domains "+oldPhys+" and "+newPhys+" in "+expr+"." );
                     }
                 }
                 dnode( expr, domain ).rep().phys = newPhys;
             }
         }
     }
     */
     /*
     public void findPhys() throws SemanticException {
         // First merge all nodes with the same physical domain
         for( Iterator n1It = dnodes.keySet().iterator(); n1It.hasNext(); ) {
             final DNode n1 = (DNode) n1It.next();
             if( n1.rep() != n1 ) continue;
             if( n1.phys == null ) continue;
             for( Iterator n2It = dnodes.keySet().iterator(); n2It.hasNext(); ) {
                 final DNode n2 = (DNode) n2It.next();
                 if( n2.rep() != n2 ) continue;
                 if( n1 == n2 ) continue;
                 if( !n1.phys.equals( n2.phys ) ) continue;
                 if( !tryMergeDNodes( n1, n2 ) ) {
                     throw new InternalCompilerError( "Couldn't merge nodes "+n1+" and "+n2+" with reps "+n1.rep()+" and "+n2.rep() );
                 }
             }
         }
 
         // Now merge any nodes that can be merged
         boolean changes;
         do {
             changes = false;
             for( Iterator n1It = dnodes.keySet().iterator(); n1It.hasNext(); ) {
                 final DNode n1 = (DNode) n1It.next();
                 if( n1.rep() != n1 ) continue;
                 if( n1.phys == null ) continue;
                 for( Iterator eIt = new ArrayList(assignEdges).iterator(); eIt.hasNext(); ) {
                     final DNode[] e = (DNode[]) eIt.next();
                     if( e[0].rep().equals( n1 ) )
                         changes = tryMergeDNodes( e[1], n1 ) | changes;
                     if( e[1].rep().equals( n1 ) )
                         changes = tryMergeDNodes( e[0], n1 ) | changes;
                 }
             }
         } while(changes);
 
         // Now print everything out
         printDomainsDot();
 
         for( Iterator exIt = new LinkedList(exprs).iterator(); exIt.hasNext(); ) {
 
             final Object ex = (Object) exIt.next();
             BDDType t;
             t = (BDDType) getType( ex );
             for( Iterator pairIt = t.domainPairs().iterator(); pairIt.hasNext(); ) {
                 final Type[] pair = (Type[]) pairIt.next();
                 DNode dnode = findDnode( ex, pair[0] );
                 if( pair[1] != null && !pair[1].equals( dnode.rep().phys ) ) {
                     throw new InternalCompilerError( "Original phys was "+pair[1]+
                             " but "+dnode.rep().phys+" was assigned for domain "+
                             pair[0]+" of expression "+ex+" whose class is "+ex.getClass().getName()+"; dnode is "+dnode+" and rep is "+dnode.rep()+"." );
                 }
                 /*
                 if( pair[1] == null ) {
                     System.out.println( "assigning "+dnode.rep().phys+" from "+dnode.rep()+", rep of "+dnode+" to domain "+pair[0]+" of "+ex+" whose current phys is "+pair[1] );
                 }
                 pair[1] = dnode.rep().phys;
                 if( pair[1] == null ) {
                     if( ex instanceof Node )
                         throw new SemanticException( "Couldn't make up a physical domain for the domain "+pair[0]+".", ((Node) ex).position() );
                     else
                         throw new SemanticException( "Couldn't make up a physical domain for the domain "+pair[0]+" in "+ex+"." );
                 }
             }
         }
     }
     */
 
     //private Set assignEdges = new HashSet();
     //private Set conflictEdges = new HashSet();
     //private Set mustEqualEdges = new HashSet();
     public void addAssignEdge( DNode n1, DNode n2 ) {
         if( n1 == null ) return;
         if( n2 == null ) return;
         PhysDom.v().assignEdges.add( new DNode[] { n1, n2 } );
     }
     /*
     public void addConflictEdge( DNode n1, DNode n2 ) {
         if( n1 == null ) return;
         if( n2 == null ) return;
         conflictEdges.add( new DNode[] { n1, n2 } );
     }
     */
     public void addMustEqualEdge( DNode n1, DNode n2 ) {
         if( n1 == null ) return;
         if( n2 == null ) return;
         PhysDom.v().mustEqualEdges.add( new DNode[] { n1, n2 } );
     }
     private ClassType jeddType( String name ) {
         try {
             ClassType ret = typeForName(name);
             if( ret == null ) 
                 throw new InternalCompilerError( "Couldn't find type "+name );
             return ret;
         } catch( SemanticException e ) {
             throw new InternalCompilerError( "Couldn't find type "+name );
         }
     }
     public ClassType jedd() {
         return jeddType( "jedd.internal.Jedd" );
     }
     public ClassType attribute() {
         return jeddType( "jedd.Attribute" );
     }
     public ClassType physicalDomain() {
         return jeddType( "jedd.PhysicalDomain" );
     }
     public ClassType domain() {
         return jeddType( "jedd.Domain" );
     }
     public ClassType relation() {
         return jeddType( "jedd.internal.RelationContainer" );
     }
     public BDDType sameDomains( BDDType t ) {
         List domains = new LinkedList();
         for( Iterator domainIt = t.map().keySet().iterator(); domainIt.hasNext(); ) {
             final Type domain = (Type) domainIt.next();
             domains.add( new Type[] { domain, null } );
         }
         return BDDType( domains );
     }
     public BDDType cloneDomains( BDDType t ) {
         List domains = new LinkedList();
         Map map = t.map();
         for( Iterator domainIt = map.keySet().iterator(); domainIt.hasNext(); ) {
             final Type domain = (Type) domainIt.next();
             domains.add( new Type[] { domain, (Type) map.get(domain) } );
         }
         return BDDType( domains );
     }
     private Map instance2Decl = new HashMap();
     public Map instance2Decl() {
         return instance2Decl;
     }
 
     public FieldDecl getField( ClassType type, String name ) throws SemanticException {
         ClassDecl cd = (ClassDecl) instance2Decl.get(type);
         if( cd == null ) throw new SemanticException(
                 "Need field "+name+" of class "+type+" but it has no declaration." );
         ClassBody body = cd.body();
         if( body == null ) throw new SemanticException(
                 "Need field "+name+" of class "+type+" but it has no body." );
         for( Iterator memberIt = body.members().iterator(); memberIt.hasNext(); ) {
             final ClassMember member = (ClassMember) memberIt.next();
             if( !( member instanceof FieldDecl ) ) continue;
             FieldDecl fd = (FieldDecl) member;
             if( name.equals( fd.name() ) ) return fd;
         }
         throw new SemanticException( "Need field "+name+
                 " of class "+type+" which has no field with that name." );
     }
 
     protected NullType createNull() {
         return new NullType_c( this ) {
             public boolean isImplicitCastValidImpl(Type toType) {
                 if( toType instanceof BDDType ) return false;
                 return super.isImplicitCastValidImpl(toType);
             }
             public boolean isCastValidImpl(Type toType) {
                 if( toType instanceof BDDType ) return false;
                 return super.isImplicitCastValidImpl(toType);
             }
         };
     }
 }
