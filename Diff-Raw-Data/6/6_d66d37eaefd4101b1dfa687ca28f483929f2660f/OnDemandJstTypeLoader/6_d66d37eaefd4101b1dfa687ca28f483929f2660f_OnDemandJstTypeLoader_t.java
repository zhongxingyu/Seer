 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.loader;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import org.eclipse.vjet.dsf.jst.FileBinding;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.ts.IJstTypeLoader;
 import org.eclipse.vjet.dsf.jst.ts.util.JstRefTypeDependencyCollector;
 import org.eclipse.vjet.dsf.jstojava.parser.VjoParser;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateConfig;
 import org.eclipse.vjet.dsf.ts.event.group.AddGroupEvent;
 import org.eclipse.vjet.dsf.util.JavaSourceLocator;
 
 public class OnDemandJstTypeLoader implements IJstTypeLoader{
 	private static final String ONDEMAND = "ONDEMAND";
 	private static final String JS = ".js";
 	private static final String VJO = ".vjo";
 	private IJstType m_type;
 	private String m_group = ONDEMAND;
 	private List<SourceType> m_sources;
 	private Map<String,String> m_lookedup = new HashMap<String, String>();
 
 	
 	public OnDemandJstTypeLoader(IJstType type) {
 		m_type = type;
 	}
 	public OnDemandJstTypeLoader(String group, IJstType type) {
 		m_type = type;
 		m_group = group;
 	}
 	public List<SourceType> loadJstTypes(List<AddGroupEvent> groupList) {
 		m_sources = new ArrayList<SourceType>(5);
 		JavaSourceLocator locator = JavaSourceLocator.getInstance();
 		addSource(locator, m_type);
 		findSources(m_type);
 		ArrayList<SourceType> rev = new ArrayList<SourceType>();
 		for (int i = m_sources.size()-1; i>=0; i--) {
 			rev.add(m_sources.get(i));
 		}
 		return rev;
 	}
 	
 	protected void findSources(String typeName) {
 		if(alreadyLookedup(typeName)){
 			return;
 		}
 		
 		JavaSourceLocator locator = JavaSourceLocator.getInstance();
 		URL url = locator.getSourceUrl(typeName, ".js");
 		if (url!=null) {
 			TranslateConfig cfg = new TranslateConfig();
 			cfg.setSkiptImplementation(true);
 			VjoParser p = new VjoParser(cfg);
 			System.out.println("url: "+url);
 			IJstType type = p.parse(m_group, url);
 			if (type!=null) {
 				findSources(type);
 			}
 		}
 	}
 	
 	protected boolean alreadyLookedup(String typeName) {
 		if(m_lookedup.get(typeName)!=null){
 			return true;
 		}
 		
 		m_lookedup.put(typeName,"");
 		return false;
 	}
 	protected void findSources(IJstType type) {
 		JavaSourceLocator locator = JavaSourceLocator.getInstance();
 		//Currently, only inactive needs, and mixins are required dependencies
 		//for generation
 		   for (IJstTypeReference ineed : JstRefTypeDependencyCollector
 	                .getDependency(type).values()) {
 	            addSource(locator, ineed.getReferencedType());
 	            findSources(ineed.getReferencedType().getName()); // RECURSIVE
 	        }
 
 	        for (IJstType ineed : type.getImports()) {
 	            if (ineed.getAlias() != null) {
 	                addSource(locator, ineed);
 	                findSources(ineed.getName());
 	            }
 	        }
 		
 		
 //		for (IJstType ineed : type.getInactiveImports()) {
 //			addSource(locator, ineed);
 //			findSources(ineed.getName());
 //		}
 //		for (IJstTypeReference mixin : type.getMixinsRef()) {
 //			addSource(locator, mixin.getReferencedType());
 //			findSources(mixin.getReferencedType().getName());
 //		}
 //		
 		
 		
 	}
 	
 	protected void addSource(JavaSourceLocator locator, IJstType ineed) {
		if(ineed.getSource()!=null && ineed.getSource().getBinding() !=null && ineed.getSource().getBinding() instanceof FileBinding){
			m_sources.add(new SourceType(m_group, ineed.getName(),null, ((FileBinding)ineed.getSource().getBinding()).getFile()));
		}
		
 		URL url = locator.getSourceUrl(ineed.getName(), JS);
 		if(url==null){
 			url = locator.getSourceUrl(ineed.getName(), VJO);
 		}
 		
 		if (url!=null) {
 			String src = locator.getSource(url);
 			m_sources.add(new SourceType(m_group,ineed.getName(),src,new File(url.getFile())));
 		}
 	}
 	
 }
