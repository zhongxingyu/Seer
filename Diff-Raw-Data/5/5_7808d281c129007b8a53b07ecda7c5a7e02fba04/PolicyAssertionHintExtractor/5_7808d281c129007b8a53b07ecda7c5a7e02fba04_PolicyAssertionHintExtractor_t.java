 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.plugins.planner.policy;
 
 
 import static java.util.Collections.emptyList;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.neethi.Assertion;
 import org.apache.neethi.Policy;
 import org.apache.servicemix.common.util.DOMUtil;
 import org.apache.servicemix.nmr.api.Exchange;
 import org.apache.servicemix.nmr.api.Role;
 import org.eclipse.swordfish.core.planner.strategy.Hint;
 import org.eclipse.swordfish.core.planner.strategy.HintExtractor;
 import org.eclipse.swordfish.core.resolver.policy.PolicyExtractor;
 import org.eclipse.swordfish.internal.core.util.smx.ServiceMixSupport;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 
 
 public class PolicyAssertionHintExtractor implements HintExtractor {
 
 	public static final String ENDPOINT_METADAT_PROPERTY = "org.eclipse.swordfish.core.resolver.EndpointMetadata";
 
 	private static final Log LOG = LogFactory.getLog(PolicyAssertionHintExtractor.class);
 
 	private static final List<Hint<?>> EMPTY_LIST = emptyList();
 	private PolicyExtractor policyExtractor;
 	public PolicyExtractor getPolicyExtractor() {
 		return policyExtractor;
 	}
 
 	public void setPolicyExtractor(PolicyExtractor policyExtractor) {
 		this.policyExtractor = policyExtractor;
 	}
 
 	public PolicyAssertionHintExtractor() {
 
 	}
 
 	public List<Hint<?>> extractHints(MessageExchange messageExchange) {
 
 		Policy policy = getPolicy(messageExchange);
 		return policy != null ? extractAssertions(policy) : EMPTY_LIST;
 	}
 
 	private Policy getPolicy(MessageExchange messageExchange) {
 		Policy policy = null;
 		try {
 		Exchange exchange = ServiceMixSupport.toNMRExchange(messageExchange);
 
 		if(exchange.getRole() == Role.Consumer){
 	        Map headers = (Map)exchange.getIn().getHeader("org.apache.servicemix.soap.headers");
		    if (headers == null) {
		    	return null;
		    }
	        DocumentFragment policyFragment =
 					(DocumentFragment)headers.get(new QName("http://eclipse.org/swordfish/headers", "Policy"));
 		    if (policyFragment == null) {
 		    	for (Object key : headers.keySet()) {
 		    		if (key instanceof QName && ((QName)key).getLocalPart().equals("Policy")) {
 		    			policyFragment = (DocumentFragment) headers.get(key);
 		    		}
 		    	}
 		    }
 		    if (policyFragment == null) {
 		    	return null;
 		    }
 		    Element policyElement = (Element)policyFragment.getFirstChild();
 				String source = DOMUtil.asXML(policyElement);
 				return policyExtractor.extractPolicy(new ByteArrayInputStream(source.getBytes("UTF8")));
 		}
 		} catch (Exception ex) {
 			LOG.warn(ex.getMessage(), ex);
 		}
 		return policy;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List <Hint<?>> extractAssertions(Policy policy) {
 		List <Hint<?>> assertions = new ArrayList<Hint<?>>();
 
 		Iterator<Iterable<Assertion>> alternativesIter = policy.getAlternatives();
 		if (alternativesIter.hasNext()) {
 			Iterable<Assertion> alternative = alternativesIter.next();
 
 			for (Assertion assertion : alternative) {
 				assertions.add(new AssertionHint<Assertion>(assertion));
 			}
 		}
 		return assertions;
 	}
 }
