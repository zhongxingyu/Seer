 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jst.ts.util;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.traversal.IJstVisitor;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.ts.graph.DependencyNode;
 import org.eclipse.vjet.dsf.ts.group.Group;
 import org.eclipse.vjet.dsf.ts.method.MethodName;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 
 /**
  * Sample implementation to collect nodes that depend on JstMethod nodes.
  * 
  * Please note: the code is INCOMPLETE. 
  *
  */
 public class MethodDependencyVisitor extends ADependencyVisitor<MethodName> implements IJstVisitor {
 		
 	//
 	// Satisfy IJstVisitor
 	//
 	public void preVisit(IJstNode node){
 	}
 	
 	public boolean visit(IJstNode node){
 		if (node instanceof JstIdentifier){
 			JstIdentifier identifier = (JstIdentifier)node;
 			addDependency(identifier.getJstBinding(), identifier);
 		}
 //		if(node instanceof IJstGlobalFunc){
 //			System.out.println("test");
 //			IJstGlobalFunc func = (IJstGlobalFunc)node;
 ////			addDependency(func., dependent)
 //		}
 		return true;
 	}
 	
 	public void endVisit(IJstNode node){
 	}
 	
 	public void postVisit(IJstNode node){
 		
 	}
 	
 	//
 	// API
 	//
 	public Map<MethodName,List<IJstNode>> getMethodDependencies(){
 		return m_dependencies;
 	}
 	
 	
 	private void addDependency(final IJstNode binding, final IJstNode dependent){
 		
 		if (binding == null || !(binding instanceof IJstMethod)){				
 			return;
 		}
 		
 		IJstMethod jstMethod = (IJstMethod)binding;
		if(jstMethod.getName()==null){
			return;
		}
		
 		String mtdName = jstMethod.getName().getName();
 		IJstType mtdOwnerType = binding.getOwnerType();
 
 		if (mtdOwnerType == null) {
 			return;
 		}
 	
 	
 		
 		if (mtdOwnerType.getMethod(mtdName) == null && mtdOwnerType.getGlobalVar(mtdName)==null){
 			// method with same mtdName is missing, check constructor
 			
 			IJstMethod constructor = mtdOwnerType.getConstructor();
 			
 			if (binding != constructor) {
 				return; // binding is neither a constructor nor a member method
 			}
 		}
 		
 		String groupName=null;
 		if (mtdOwnerType.getPackage() != null) {
 			groupName = mtdOwnerType.getPackage().getGroupName();
 		}
 		
 		// handle empty group name (e.g. native types)		
 		if (groupName == null && m_ts != null) { // could be in native group
 			Group group = m_ts.getGroup(mtdOwnerType);
 			
 			if (group != null) {
 				groupName = group.getName();
 			}
 		}
 		
 		MethodName name = new MethodName(new TypeName(
 				groupName, 
 				mtdOwnerType.getName()), 
 				mtdName);
 		
 		if (m_ts != null) {
 			IJstType type = (IJstType)m_ts.getType(name.typeName());
 			if (type == null) {
 				DependencyNode<IJstType> node = (DependencyNode<IJstType>)m_ts.getUnresolvedNodes().get(mtdOwnerType.getName());
 				if (node == null) {
 					node = new DependencyNode<IJstType>(mtdOwnerType.getName(), mtdOwnerType, null);
 					m_ts.addUnresolvedNode(node);
 				}
 			}	
 			else if (JstTypeSpaceMgr.isDefaultLibName(groupName)) {
 				addImplicitDependency(dependent, groupName, mtdOwnerType);
 			}
 		}
 			
 		add_if_absent(name, dependent);
 	}
 	
 }
