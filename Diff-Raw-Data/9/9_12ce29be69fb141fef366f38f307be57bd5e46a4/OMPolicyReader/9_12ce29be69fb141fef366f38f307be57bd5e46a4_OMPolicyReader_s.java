 /*
  * Copyright 2001-2004 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.ws.policy.util;
 
 import org.apache.axiom.om.OMAbstractFactory;
 import org.apache.axiom.om.OMAttribute;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMXMLParserWrapper;
 import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
 import org.apache.ws.policy.All;
 import org.apache.ws.policy.Assertion;
 import org.apache.ws.policy.Policy;
 import org.apache.ws.policy.PolicyConstants;
 import org.apache.ws.policy.PolicyReference;
 import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.ExactlyOne;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 /**
  * OMPolicyReader implements PolicyReader interface and provides different
  * methods to create a policy object It ueses AxisOM as its underlying mechanism
  * to process XML.
  */
 public class OMPolicyReader implements PolicyReader {
 	OMPolicyReader() {
 	}
 
 	public Policy readPolicy(InputStream in) {
 		try {
             
 			XMLStreamReader reader = XMLInputFactory.newInstance()
 					.createXMLStreamReader(in);
 			OMXMLParserWrapper builder = OMXMLBuilderFactory
 					.createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
 							reader);
 
 			OMElement element = builder.getDocumentElement();
 			return readPolicy(element);
 
 		} catch (XMLStreamException ex) {
 			throw new RuntimeException("error : " + ex.getMessage());
 		}
 	}
 
 	private Assertion readAssertion(OMElement element) {
         
		String namespace = element.getNamespace().getName();
 		String localName = element.getLocalName();
 
 		if (!(namespace.equals(PolicyConstants.POLICY_NAMESPACE_URI))) {
 			return readPrimitiveAssertion(element);
 		}
 
 		if (localName.equals(PolicyConstants.POLICY)) {
 			return readPolicy(element);
 
 		} else if (localName.equals(PolicyConstants.ALL)) {
 			return readAndComposite(element);
 
 		} else if (localName.equals(PolicyConstants.EXACTLY_ONE)) {
 			return readXorComposite(element);
 
 		} else if (localName.equals(PolicyConstants.POLICY_REFERENCE)) {
 			return readPolicyReference(element);
 
 		} else {
 			throw new RuntimeException("unknown element ..");
 		}
 	}
 
 	public Policy readPolicy(OMElement element) {
     Policy policy = new Policy();
 
     // We treat all attributes equally ...
     OMAttribute attri;
     Iterator it = element.getAllAttributes();
     while (it.hasNext()) {
       attri = (OMAttribute) it.next();
       policy.addAttribute(attri.getQName(), attri.getAttributeValue());
     }
     policy.addTerms(readTerms(element));
     return policy;
 	}
 
 	private All readAndComposite(OMElement element) {
 		All andCompositeAssertion = new All();
 		andCompositeAssertion.addTerms(readTerms(element));
 		return andCompositeAssertion;
 	}
 
 	private ExactlyOne readXorComposite(OMElement element) {
 		ExactlyOne xorCompositeAssertion = new ExactlyOne();
 		xorCompositeAssertion.addTerms(readTerms(element));
 		return xorCompositeAssertion;
 	}
 
 	public PolicyReference readPolicyReference(OMElement element) {
 		OMAttribute attribute = element.getAttribute(new QName("URI"));
 		return new PolicyReference(attribute.getAttributeValue());
 	}
 
 	private PrimitiveAssertion readPrimitiveAssertion(OMElement element) {
 		QName qname = element.getQName();
 		PrimitiveAssertion result = new PrimitiveAssertion(qname);
 
 		result.setAttributes(getAttributes(element));
 
 		String isOptional = result.getAttribute(new QName(
 				PolicyConstants.POLICY_NAMESPACE_URI, "Optional"));
 		result.setOptional(new Boolean(isOptional).booleanValue());
 
 		// setting the text value ..
 		String strValue = element.getText();
 
 		if (strValue != null && strValue.length() != 0) {
 			result.setStrValue(strValue.trim());
 		}
 
 		//CHECK ME
 		Iterator childElements = element.getChildElements();
 
 		while (childElements.hasNext()) {
 			OMElement childElement = (OMElement) childElements.next();
 
			if (childElement.getNamespace().getName().equals(
 					PolicyConstants.POLICY_NAMESPACE_URI)
 					&& childElement.getLocalName().equals(
 							PolicyConstants.POLICY)) {
 				Policy policy = readPolicy(childElement);
 				result.addTerm(policy);
 
 			} else {
 				PrimitiveAssertion pa = readPrimitiveAssertion(childElement);
 				result.addTerm(pa);
 			}
 		}
 		return result;
 	}
 
 	private ArrayList readTerms(OMElement element) {
 		ArrayList terms = new ArrayList();
 		Iterator childElements = element.getChildren();
 
 		while (childElements.hasNext()) {
 			Object obj = childElements.next();
 
 			if (obj instanceof OMElement) {
 				OMElement e = (OMElement) obj;
 				terms.add(readAssertion(e));
 			}
 		}
 		return terms;
 	}
 
 	private Hashtable getAttributes(OMElement element) {
 		Hashtable attributes = new Hashtable();
 		Iterator iterator = element.getAllAttributes();
 
 		while (iterator.hasNext()) {
 			OMAttribute attribute = (OMAttribute) iterator.next();
 			attributes.put(attribute.getQName(), attribute.getAttributeValue());
 		}
 
 		return attributes;
 	}
 }
