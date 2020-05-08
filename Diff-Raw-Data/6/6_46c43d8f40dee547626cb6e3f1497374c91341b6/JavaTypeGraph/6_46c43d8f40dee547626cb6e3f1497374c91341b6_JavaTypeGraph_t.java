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
 * $Id: JavaTypeGraph.java,v 1.7 2005-05-12 10:36:01 meffert Exp $
  */
 
 package org.caesarj.compiler.typesys.java;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.caesarj.compiler.Log;
 import org.caesarj.compiler.typesys.graph.CaesarTypeGraph;
 import org.caesarj.compiler.typesys.graph.CaesarTypeNode;
 
 
 /**
  * ...
  * 
  * @author Ivica Aracic
  */
 public class JavaTypeGraph {
     
     private JavaTypeNode root;
     private HashMap caesar2javaMap = new HashMap();
     private HashMap nodes = new HashMap();
     
     public JavaTypeGraph() {
         CaesarTypeNode typeNode = 
         	new CaesarTypeNode(
     			null,
 				CaesarTypeNode.DECLARED,
 				new JavaQualifiedName("org/caesarj/runtime/CaesarObject")
 			);
         
         root = new JavaTypeNode(this, typeNode);
         root.setType(typeNode);
     }
     
     public void generateFrom(CaesarTypeGraph completeGraph) {
     	buildJavaTypeGraph(completeGraph);
                        
         root.genOuterAndQualifiedNames();
         root.genOuterAndQNForGeneratedTypes(new HashSet());
     }  
     
     private void buildJavaTypeGraph(CaesarTypeGraph completeGraph) {    
         Map typeMap = completeGraph.getTypeMap();
         
         // generate graph
         for (Iterator it = typeMap.entrySet().iterator(); it.hasNext();) {
             CaesarTypeNode t = (CaesarTypeNode) ((Map.Entry)it.next()).getValue();
             
             List mixinList = t.getMixinList();
             
             // sort list into compilation graph
             CaesarTypeNode[] mixins = new CaesarTypeNode[mixinList.size()];
             mixins = (CaesarTypeNode[])mixinList.toArray(mixins);
             
             JavaTypeNode current = root;
             
             for (int i=mixins.length-1; i>=0; i--) {
                 CaesarTypeNode mixin = mixins[i];
                 JavaTypeNode next = current.getSubNode(mixin.getQualifiedName().toString());
                 
                 if(next == null) {
                     next = new JavaTypeNode(this, mixin);
                     current.addSubNode(next);
                 }
                                 
                 current = next;
             }
             
             if(t.isImplicitType()) {
                 // append as leaf
                 JavaTypeNode next = new JavaTypeNode(this, t);
                 next.setType(t);
                 current.addSubNode(next);
             }
             else {
                 current.setType(t);
             }
         }
     }
 
     public Collection findMixinCopies(CaesarTypeNode mixin) {        
         Collection res = new LinkedList();
         
         // check first that passed node is not a mixin copy itself        
         Collection typesToGen = getTypesToGenerate();
         
         for (Iterator it = typesToGen.iterator(); it.hasNext();) {
             JavaTypeNode item = (JavaTypeNode) it.next();
            // test against mixin of item.
            if(item.getMixin() == mixin) 
                 res.add(item);
         }        
         
         return res;
     }
     
     public void debug() {
         StringBuffer sb = new StringBuffer();
         root.debug(0, sb);
         
         Log.verbose("===== Java Type Graph =====\n"+sb.toString()+"\n\n");
     }
 
     public void registerJavaType(CaesarTypeNode type, JavaTypeNode node) {
         caesar2javaMap.put(type, node);
     }    
     
     public JavaTypeNode getJavaTypeNode(CaesarTypeNode type) {
         return (JavaTypeNode)caesar2javaMap.get(type);
     }
     
     public JavaTypeNode getNode(JavaQualifiedName qn) {
         return (JavaTypeNode)nodes.get(qn);
     }
     
     protected void registerNode(JavaQualifiedName qn, JavaTypeNode node) {
         nodes.put(qn, node);
     }
 
     /** 
      * @return preorder sorted list of types which has to be generated 
      */
     public Collection getTypesToGenerate() {
         Collection res = new LinkedList();
         root.collectGeneratedTypes(res);
         return res;
     }
 
     /**
      * @return preorder sorted list of all types
      */
     public Collection getAllTypes() {
         Collection res = new LinkedList();
         root.collectAllTypes(res);
         return res;
     }
     
     /**
      * CTODO getAllSourceTypes
      */
     public Collection getAllSourceTypes() {
         return null;
     }
 }
