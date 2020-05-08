 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  * Other names may be trademarks of their respective owners.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 
 package org.netbeans.modules.javafx.debugger.variablesfiltering;
 
 import org.netbeans.modules.javafx.debugger.models.SequenceObject;
 import org.netbeans.modules.javafx.debugger.models.SequenceField;
 import com.sun.javafx.jdi.FXClassType;
 import com.sun.javafx.jdi.FXObjectReference;
 import com.sun.javafx.jdi.FXPrimitiveValue;
 import com.sun.javafx.jdi.FXReferenceType;
 import com.sun.javafx.jdi.FXSequenceReference;
 import com.sun.jdi.InternalException;
 import com.sun.jdi.ObjectCollectedException;
 import com.sun.jdi.VMDisconnectedException;
 import com.sun.jdi.Value;
 import java.util.ArrayList;
 import java.util.List;
 import org.netbeans.api.debugger.jpda.Field;
 import org.netbeans.api.debugger.jpda.JPDAClassType;
 import org.netbeans.api.debugger.jpda.LocalVariable;
 import org.netbeans.api.debugger.jpda.ObjectVariable;
 import org.netbeans.api.debugger.jpda.This;
 import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
 import org.netbeans.modules.debugger.jpda.models.AbstractVariable;
 //import org.netbeans.modules.debugger.jpda.models.FieldVariable;
 import org.netbeans.modules.debugger.jpda.models.ClassVariableImpl;
 import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
 import org.netbeans.modules.javafx.debugger.models.ScriptClass;
 import org.netbeans.spi.debugger.DebuggerServiceRegistration;
 import org.netbeans.spi.viewmodel.ModelListener;
 import org.netbeans.spi.viewmodel.TreeModel;
 import org.netbeans.spi.viewmodel.TreeModelFilter;
 import org.netbeans.spi.viewmodel.UnknownTypeException;
 
 /**
  *
  * @author Michal Skvor
  */
 @DebuggerServiceRegistration( path="netbeans-JPDASession/FX/LocalsView",types={ org.netbeans.spi.viewmodel.TreeModelFilter.class } )
 public class JavaFXVariablesFilter implements TreeModelFilter {
 
     /** Creates new JavaFXVariablesFilter instance */
     public JavaFXVariablesFilter() {}
 
     /**
      *
      * Returns filtered root of hierarchy.
      *
      * @param   original the original tree model
      * @return  filtered root of hierarchy
      */
     public Object getRoot( TreeModel original ) {
         return original.getRoot();
     }
 
     public Object[] getChildren( TreeModel original, Object parent, int from, int to ) throws UnknownTypeException {
         Object[] visibleChildren = null;
 //        System.out.println(" - " + parent );
         if( parent.equals( original.getRoot())) {
             // Retrieve children
            int parentChildrenCount = original.getChildrenCount( parent );
             Object[] children = original.getChildren( parent, 0, parentChildrenCount );
             parentChildrenCount = children.length;
             List vc = new ArrayList();
             for( int j = 0; j < parentChildrenCount; j++ ) {
                 Object child = children[j];
 //                System.out.println(" - " + child );
                 if( child instanceof ClassVariableImpl ) {
                 } else if (child instanceof JPDAClassType) {
                     Object[] ch = original.getChildren( child, from, to );                
                     for( int i = 0; i < ch.length; i++ ) {
                         Object obj = ch[i];
 //                        System.out.println("    - " + obj );e 
                         if( obj instanceof ClassVariableImpl ) {
                         } else if (obj instanceof This) {
 //                            ClassVariable cv = (ClassVariable)obj;
 //                            vc.add( obj );
                         } else if( obj instanceof Field ) {
                             Field f = (Field)obj;
                             vc.add( obj );
                         } else {
                             vc.add( obj );
                         }
                     }
                 } else if( child instanceof LocalVariable ) {
                     vc.add( child );
                 } else if( child instanceof This ) {
 //                    vc.add( child );
                     This t = (This)child;
                     AbstractVariable av = (AbstractVariable)t;
                     JDIVariable jdiv = (JDIVariable)t;
                     Value v = jdiv.getJDIValue();
                     FXObjectReference fx = (FXObjectReference)v;
                     FXClassType clazz = ((FXReferenceType)fx.referenceType()).scriptClass();
                     if( clazz != null ) {
                         vc.add( new ScriptClass( av.getDebugger(), v, clazz, fx ));
                     }
                 }
             }
 //            List<Object> vvv = vc.subList( from, to > vc.size() ? vc.size() : to );
 //            for( Object o : vvv ) {
 //                System.out.println("    - " + o );
 //            }
             return vc.subList( from, to > vc.size() ? vc.size() : to ).toArray();
         } else {
             // Root static class
             boolean seqType = false;
             List vc = new ArrayList();
             if( parent instanceof ObjectVariable ) {
                 ObjectVariable ov = (ObjectVariable)parent;
 
                 JDIVariable jdiv = (JDIVariable)ov;
                 Value v = jdiv.getJDIValue();
                 if( v instanceof FXSequenceReference ) {
                     FXSequenceReference seq = (FXSequenceReference)v;
 
                     AbstractVariable av = (AbstractVariable)ov;
                     try {                    
                         if( seq != null ) {
                             for( int i = 0; i < seq.length(); i++ ) {
                                 JPDAThreadImpl thread = (JPDAThreadImpl)av.getDebugger().getCurrentThread();  
                                 if( thread == null ) {
                                    return null;
                                 }
                                 thread.accessLock.writeLock().lock();
 
                                 try {            
                                     if( !thread.isSuspended()) {
                                        return null;
                                     }        
                                     Value iv = seq.getValue( i );
 
                                     if( iv instanceof FXPrimitiveValue ) {
                                         vc.add( new SequenceField( av.getDebugger(), 
                                                (FXPrimitiveValue) seq.getValue( i ), ov, i,
                                                v.type().toString()));
                                     } else if( iv instanceof FXObjectReference ) {
                                         String t = iv.type().name();
                                         vc.add( new SequenceObject( av.getDebugger(),
                                                 (FXObjectReference) iv, t,
                                                  ov, i, 1000, v.toString()));
                                     }
                                 } finally {
                                     thread.accessLock.writeLock().unlock();
                                 }          
 
                             }
                         }
                     } catch( VMDisconnectedException ex ) {                                
                     } catch( ObjectCollectedException ex ) {
                     } catch( InternalException ex ) {}
                     seqType = true;
                 } 
             }
             if( !seqType ) {
                 Object[] children = original.getChildren( parent, from, to );
                 for( int i = 0; i < children.length; i++ ) {
                     Object child = children[i];
 //                    System.out.println(" - " + child.toString());
                     if( child instanceof Field ) {
                         Field f = (Field)child;
                         vc.add( child );
                     } else {
                         vc.add( child );
 //                        System.out.println(" - " + child.toString());
                     }
                 }
             }
             visibleChildren = vc.subList( from, to > vc.size() ? vc.size() : to ).toArray();
         }
         return visibleChildren;
     }
 
     public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
         return Integer.MAX_VALUE;
     }
 
     public boolean isLeaf( TreeModel original, Object node ) throws UnknownTypeException {
         boolean il;
 
         if( node instanceof Field ) {
             Field f = (Field)node;
             if( f instanceof ScriptClass || f instanceof ObjectVariable || f instanceof SequenceObject ) {
                 if( "java.lang.String".equals( f.getDeclaredType())) return false;
                 return false;
             } 
             return true;
         }
         il = original.isLeaf( node );
 
         return il;
     }
 
     public void addModelListener( ModelListener l ) {
     }
 
     public void removeModelListener( ModelListener l ) {
     }
 }
