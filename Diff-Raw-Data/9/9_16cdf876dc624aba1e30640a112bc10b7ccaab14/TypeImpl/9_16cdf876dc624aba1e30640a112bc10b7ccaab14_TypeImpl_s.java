 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
  *
  *  The contents of this file are subject to the terms of either the GNU
  *  General Public License Version 2 only ("GPL") or the Common Development
  *  and Distribution License("CDDL") (collectively, the "License").  You
  *  may not use this file except in compliance with the License. You can obtain
  *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
  *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
  *  language governing permissions and limitations under the License.
  *
  *  When distributing the software, include this License Header Notice in each
  *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
  *  Sun designates this particular file as subject to the "Classpath" exception
  *  as provided by Sun in the GPL Version 2 section of the License file that
  *  accompanied this code.  If applicable, add the following below the License
  *  Header, with the fields enclosed by brackets [] replaced by your own
  *  identifying information: "Portions Copyrighted [year]
  *  [name of copyright owner]"
  *
  *  Contributor(s):
  *
  *  If you wish your version of this file to be governed by only the CDDL or
  *  only the GPL Version 2, indicate your decision by adding "[Contributor]
  *  elects to include this software in this distribution under the [CDDL or GPL
  *  Version 2] license."  If you don't indicate a single choice of license, a
  *  recipient has the option to distribute your version of this file under
  *  either the CDDL, the GPL Version 2 or to extend the choice of license to
  *  its licensees as provided above.  However, if you add GPL Version 2 code
  *  and therefore, elected the GPL Version 2 license, then the option applies
  *  only if the new code is made subject to such option by the copyright
  *  holder.
  */
 package org.glassfish.hk2.classmodel.reflect.impl;
 
 import org.glassfish.hk2.classmodel.reflect.*;
 
 import java.util.*;
 import java.net.URI;
 
 /**
  * Implementation of the Type abstraction.
  *
  * @author Jerome Dochez
  */
 public class TypeImpl extends AnnotatedElementImpl implements Type {
 
     final TypeProxy<Type> sink;
     final List<MethodModel> methods = new ArrayList<MethodModel>();
     final Set<URI> definingURIs= Collections.synchronizedSet(new HashSet<URI>());
 
 
     public TypeImpl(String name, TypeProxy<Type> sink) {
         super(name);
         this.sink = sink;
     }
 
     @Override
     public Collection<URI> getDefiningURIs() {
         return Collections.unmodifiableSet(definingURIs);
     }
 
     void addDefiningURI(URI uri) {
         definingURIs.add(uri);
     }
 
     @Override
     public boolean wasDefinedIn(Collection<URI> uris) {
         for (URI uri : uris) {
             if (definingURIs.contains(uri)) {
                 return true;
             }
         }
         return false;
     }
 
     void addMethod(MethodModelImpl m) {
         methods.add(m);
     }
 
     @Override
     public Collection<MethodModel> getMethods() {
         return Collections.unmodifiableList(methods);
     }
 
     TypeProxy getProxy() {
         return sink;
     }
 
     @Override
     public Collection<Member> getReferences() {
         return Collections.unmodifiableSet(sink.getRefs());
     }
 
     @Override
     protected void print(StringBuffer sb) {
         super.print(sb);    //To change body of overridden methods use File | Settings | File Templates.
         sb.append(", subclasses=[");
         for (AnnotatedElement cm : sink.getSubTypeRefs()) {
             sb.append(" ").append(cm.getName());
         }
         sb.append("]");
     }
 }
