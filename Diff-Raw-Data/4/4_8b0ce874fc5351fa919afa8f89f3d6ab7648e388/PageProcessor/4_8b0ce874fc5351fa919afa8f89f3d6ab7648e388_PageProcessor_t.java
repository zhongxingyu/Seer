 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.text.TextProposal;
import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.CustomTagLibAttribute;
 import org.jboss.tools.jst.web.kb.taglib.CustomTagLibManager;
 import org.jboss.tools.jst.web.kb.taglib.IAttribute;
 import org.jboss.tools.jst.web.kb.taglib.IComponent;
 import org.jboss.tools.jst.web.kb.taglib.ICustomTagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.IFacesConfigTagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 
 /**
  * @author Alexey Kazakov
  */
 public class PageProcessor implements IProposalProcessor {
 
 	private static final PageProcessor INSTANCE = new PageProcessor();
 	private ICustomTagLibrary[] customTagLibs;
 	private CustomTagLibAttribute[] componentExtensions;
 
 	/**
 	 * @return instance of PageProcessor
 	 */
 	public static PageProcessor getInstance() {
 		return INSTANCE;
 	}
 
 	private PageProcessor() {
 		customTagLibs = CustomTagLibManager.getInstance().getLibraries();
 		componentExtensions = CustomTagLibManager.getInstance().getComponentExtensions();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.ProposalProcessor#getProposals(org.jboss.tools.jst.web.kb.KbQuery, org.jboss.tools.jst.web.kb.PageContext)
 	 */
 	public TextProposal[] getProposals(KbQuery query, IPageContext context) {
 		ArrayList<TextProposal> proposals = new ArrayList<TextProposal>();
 
 		if (!isQueryForELProposals(query, context)) {
 			ITagLibrary[] libs =  context.getLibraries();
 			for (int i = 0; libs != null && i < libs.length; i++) {
 				if(libs[i] instanceof IFacesConfigTagLibrary) {
 					continue;
 				}
 				TextProposal[] libProposals = libs[i].getProposals(query, context);
 				for (int j = 0; libProposals != null && j < libProposals.length; j++) {
 					proposals.add(libProposals[j]);
 				}
 			}
 			if (query.getType() == KbQuery.Type.ATTRIBUTE_VALUE) {
 				Map<String, IAttribute> attrbMap = new HashMap<String, IAttribute>();
 				for (TextProposal proposal : proposals) {
 					if(proposal.getSource()!=null && proposal.getSource() instanceof IAttribute) {
 						IAttribute att = (IAttribute)proposal.getSource();
 						attrbMap.put(att.getName(), att);
 					}
 				}
 				IAttribute[] attrs = getAttributes(query, context, false);
 				for (int i = 0; i < attrs.length; i++) {
 					attrbMap.put(attrs[i].getName(), attrs[i]);
 				}
 				for (int i = 0; i < componentExtensions.length; i++) {
 					if(attrbMap.containsKey(componentExtensions[i].getName())) {
 						TextProposal[] attProposals = componentExtensions[i].getProposals(query, context);
 						for (int j = 0; j < attProposals.length; j++) {
 							proposals.add(attProposals[j]);
 						}
 					}
 				}
 			}
 			for (int i = 0; customTagLibs != null && i < customTagLibs.length; i++) {
 				TextProposal[] libProposals = customTagLibs[i].getProposals(query, context);
 				for (int j = 0; libProposals != null && j < libProposals.length; j++) {
 					proposals.add(libProposals[j]);
 				}
 			}
 		} else {
 			String value = query.getValue();
 			String elString = value;
 			ELResolver[] resolvers =  context.getElResolvers();
 			for (int i = 0; resolvers != null && i < resolvers.length; i++) {
 				proposals.addAll(resolvers[i].getProposals(context, elString));
 			}
 		}
 		return proposals.toArray(new TextProposal[proposals.size()]);
 	}
 
 	private boolean isQueryForELProposals(KbQuery query, IPageContext context) {
 		if (query.getType() != KbQuery.Type.ATTRIBUTE_VALUE &&
				((query.getType() != KbQuery.Type.TEXT ) || !(context instanceof IFaceletPageContext || context instanceof XmlContextImpl))) { 
 			return false;
 		}
 
 		return (query.getValue() != null && 
 				(query.getValue().startsWith("#{") || //$NON-NLS-1$
 					query.getValue().startsWith("${"))); //$NON-NLS-1$
 	}
 	
  	/**
 	 * Returns components
 	 * @param query
 	 * @param context
 	 * @return components
 	 */
 	public IComponent[] getComponents(KbQuery query, IPageContext context) {
 		return getComponents(query, context, false);
 	}
 
 	public IComponent[] getComponents(KbQuery query, IPageContext context, boolean includeComponentExtensions) {
 		ArrayList<IComponent> components = new ArrayList<IComponent>();
 		ITagLibrary[] libs =  context.getLibraries();
 		for (int i = 0; i < libs.length; i++) {
 			if(libs[i] instanceof IFacesConfigTagLibrary) {
 				continue;
 			}
 			IComponent[] libComponents = libs[i].getComponents(query, context);
 			for (int j = 0; j < libComponents.length; j++) {
 				if(includeComponentExtensions || !libComponents[j].isExtended()) {
 					components.add(libComponents[j]);
 				}
 			}
 		}
 		for (int i = 0; customTagLibs != null && i < customTagLibs.length; i++) {
 			IComponent[] libComponents = customTagLibs[i].getComponents(query, context);
 			for (int j = 0; j < libComponents.length; j++) {
 				if(includeComponentExtensions || !libComponents[j].isExtended()) {
 					components.add(libComponents[j]);
 				}
 			}
 		}
 		return components.toArray(new IComponent[components.size()]);
 	}
 
 	private final static IAttribute[] EMPTY_ATTRIBUTE_ARRAY = new IAttribute[0];
 
 	/**
 	 * Returns attributes
 	 * @param query
 	 * @param context
 	 * @return attributes
 	 */
 	public IAttribute[] getAttributes(KbQuery query, IPageContext context) {
 		return getAttributes(query, context, true);
 	}
 
 	private IAttribute[] getAttributes(KbQuery query, IPageContext context, boolean includeComponentExtensions) {
 		if(query.getType() == KbQuery.Type.ATTRIBUTE_NAME || query.getType() == KbQuery.Type.ATTRIBUTE_VALUE) {
 			ArrayList<IAttribute> attributes = new ArrayList<IAttribute>();
 			Map<String, IAttribute> attrbMap = new HashMap<String, IAttribute>();
 			IComponent[] components  = getComponents(query, context, includeComponentExtensions);
 			for (int i = 0; i < components.length; i++) {
 				IAttribute[] libAttributess = components[i].getAttributes(query, context);
 				for (int j = 0; j < libAttributess.length; j++) {
 					attributes.add(libAttributess[j]);
 					attrbMap.put(libAttributess[j].getName(), libAttributess[j]);
 				}
 			}
 			if(includeComponentExtensions) {
 				for (int i = 0; i < componentExtensions.length; i++) {
 					if(attrbMap.containsKey(componentExtensions[i].getName())) {
 						attributes.add(componentExtensions[i]);
 					}
 				}
 			}
 			return attributes.toArray(new IAttribute[attributes.size()]);
 		}
 		return EMPTY_ATTRIBUTE_ARRAY;
 	}
 }
