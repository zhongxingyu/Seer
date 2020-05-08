 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.neethi;
 
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.Map;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamReader;
 
 import org.w3c.dom.Element;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.neethi.builders.AssertionBuilder;
 import org.apache.neethi.builders.converters.ConverterRegistry;
 
 /**
  * PolicyEngine provides set of methods to create a Policy object from an
  * InputStream, Element, XMLStreamReader, OMElement, etc.. It maintains an instance of
  * AssertionBuilderFactory that can return AssertionBuilders that can create a
  * Domain Assertion out of an element. These AssertionBuilders are used when
  * constructing a Policy object.
  */
 public class PolicyEngine {
 
     private static final Log LOG = LogFactory.getLog(PolicyEngine.class);
 
     private ConverterRegistry converters = new ConverterRegistry();
     private AssertionBuilderFactory factory = new AssertionBuilderFactory(this, converters);
     private PolicyRegistry defaultPolicyRegistry;
     
     /**
      * Registers an AssertionBuilder instances and associates it with a QName.
      * PolicyManager or other AssertionBuilders instances can use this
      * AssertionBuilder instance to process and build an Assertion from a
      * element with the specified QName.
      * 
      * @param qname
      *            the QName of the Assertion that the Builder can build
      * @param builder
      *            the AssertionBuilder that can build assertions that of 'qname'
      *            type
      */
     public void registerBuilder(QName qname, AssertionBuilder builder) {
         factory.registerBuilder(qname, builder);
     }
     
     
     /**
      * The PolicyEngine can have a default PolicyRegistry that the Policy objects
      * that it creates are setup to use when normalize is called without the 
      * PolicyRegistry.   
      * @return the default PolicyRegistry
      */
     public PolicyRegistry getPolicyRegistry() {
         return defaultPolicyRegistry;
     }
 
     public void setPolicyRegistry(PolicyRegistry reg) {
         defaultPolicyRegistry = reg;
     }
 
     /**
      * Creates a Policy object from an InputStream.
      * 
      * @param inputStream
      *            the InputStream of the Policy
      * @return a Policy object of the Policy that is fed as a InputStream
      */
     public Policy getPolicy(InputStream inputStream) {
         try {
             XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
             return getPolicy(reader);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         // TODO throw an IllegalArgumentException
         return null;
     }
 
     public Policy getPolicy(Element el) {
         return getPolicyOperator(el);
     }
     
     
     public Policy getPolicy(XMLStreamReader reader) {
         return getPolicyOperator(reader);
     }
 
     /**
      * Creates a Policy object from an element.
      * 
      * @param element
      *            the Policy element
      * @return a Policy object of the Policy element
      */
     public Policy getPolicy(Object element) {
         return getPolicyOperator(element);
     }
 
     /**
      * Creates a PolicyReference object.
      * 
      * @param inputStream
      *            the InputStream of the PolicyReference
      * @return a PolicyReference object of the PolicyReference
      */
     public PolicyReference getPolicyReference(InputStream inputStream) {
         try {
             XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
             return getPolicyReference(reader);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         // TODO throw an IllegalArgumentException
         return null;
     }
 
     /**
      * Creates a PolicyReference object from an element.
      * 
      * @param element
      *            the PolicyReference element
      * @return a PolicyReference object of the PolicyReference element
      */
     public PolicyReference getPolicyReference(Object element) {
         QName qn = converters.findQName(element);
 
         if (!Constants.isPolicyRef(qn)) {
             throw new RuntimeException(
                     "Specified element is not a <wsp:PolicyReference .. />  element");
         }
 
         PolicyReference reference = new PolicyReference(this);
 
         Map<QName, String> attributes = converters.getAttributes(element);
 
         // setting the URI value
         reference.setURI(attributes.get(new QName("URI")));
         return reference;
     }
 
     private Policy getPolicyOperator(Object element) {
         String ns = converters.findQName(element).getNamespaceURI();
         return (Policy) processOperationElement(element, new Policy(defaultPolicyRegistry, ns));
     }
 
     private ExactlyOne getExactlyOneOperator(Object element) {
         return (ExactlyOne) processOperationElement(element, new ExactlyOne());
     }
 
     private All getAllOperator(Object element) {
         return (All) processOperationElement(element, new All());
     }
 
     private PolicyOperator processOperationElement(Object operationElement,
                                                           PolicyOperator operator) {
 
         if (Constants.TYPE_POLICY == operator.getType()) {
             Policy policyOperator = (Policy) operator;
 
             Map<QName, String> attributes = converters.getAttributes(operationElement);
             
             for (Map.Entry<QName, String> ent : attributes.entrySet()) {
                 policyOperator.addAttribute(ent.getKey(), ent.getValue());
             }
         }
 
 
         for (Iterator iterator = converters.getChildElements(operationElement); 
             iterator.hasNext();) {
             
             Object childElement = iterator.next();
             QName qn = converters.findQName(childElement);
             
             if (childElement == null || qn == null 
                 || qn.getNamespaceURI() == null) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Skipping bad policy element " + childElement);
                 }
             } else if (Constants.isInPolicyNS(qn)) {
                 if (Constants.ELEM_POLICY.equals(qn.getLocalPart())) {
                     operator.addPolicyComponent(getPolicyOperator(childElement));
                 } else if (Constants.ELEM_EXACTLYONE.equals(qn.getLocalPart())) {
                     operator.addPolicyComponent(getExactlyOneOperator(childElement));
                 } else if (Constants.ELEM_ALL.equals(qn.getLocalPart())) {
                     operator.addPolicyComponent(getAllOperator(childElement));
                 } else if (Constants.ELEM_POLICY_REF.equals(qn.getLocalPart())) {
                     operator.addPolicyComponent(getPolicyReference(childElement));
                 } else {
                     operator.addPolicyComponent(factory.build(childElement));
                 }
             } else {
                 operator.addPolicyComponent(factory.build(childElement));
             }
         }
         return operator;
     }
 }
