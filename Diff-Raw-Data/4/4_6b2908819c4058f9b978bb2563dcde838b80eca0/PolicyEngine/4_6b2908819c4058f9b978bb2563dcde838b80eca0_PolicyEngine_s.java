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
 package org.apache.neethi;
 
 import org.apache.axiom.om.OMAbstractFactory;
 import org.apache.axiom.om.OMAttribute;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMNamespace;
 import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
 import org.apache.neethi.builders.AssertionBuilder;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLInputFactory;
 import java.io.InputStream;
 import java.util.Iterator;
 
 /**
  * PolicyEngine provides set of methods to create a Policy object from an
  * InputStream, OMElement, .. etc. It maintains an instance of
  * AssertionBuilderFactory that can return AssertionBuilders that can create a
  * Domain Assertion out of an OMElement. These AssertionBuilders are used when
  * constructing a Policy object.
  */
 public class PolicyEngine {
 
     public static final String POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";
 
     public static final String POLICY = "Policy";
 
     public static final String EXACTLY_ONE = "ExactlyOne";
 
     public static final String ALL = "All";
 
     public static final String POLICY_REF = "PolicyReference";
 
     private static AssertionBuilderFactory factory = new AssertionBuilderFactory();
 
     /**
      * Registers an AssertionBuilder instances and associates it with a QName.
      * PolicyManager or other AssertionBuilders instances can use this
      * AssertionBuilder instance to process and build an Assertion from a
      * OMElement with the specified QName.
      * 
      * @param qname
      * @param builder
      */
     public static void registerBuilder(QName qname, AssertionBuilder builder) {
         AssertionBuilderFactory.registerBuilder(qname, builder);
     }
 
     /**
      * Creates a Policy object from an InputStream.
      * 
      * @param inputStream
      * @return
      */
     public static Policy getPolicy(InputStream inputStream) {
         try {
             OMElement element = OMXMLBuilderFactory.createStAXOMBuilder(
                     OMAbstractFactory.getOMFactory(),
                     XMLInputFactory.newInstance().createXMLStreamReader(
                             inputStream)).getDocumentElement();
             return getPolicy(element);
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         // TODO throw an IllegalArgumentException
         return null;
     }
 
     public static PolicyReference getPolicyReferene(InputStream inputStream) {
 
         try {
             OMElement element = OMXMLBuilderFactory.createStAXOMBuilder(
                     OMAbstractFactory.getOMFactory(),
                     XMLInputFactory.newInstance().createXMLStreamReader(
                             inputStream)).getDocumentElement();
             return getPolicyReference(element);
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         // TODO throw an IllegalArgumentException
         return null;
 
     }
 
     /**
      * Creates a Policy object from an OMElement.
      *
      * @param element
      * @return
      */
     public static Policy getPolicy(OMElement element) {
        
        if (! element.getQName().equals(Constants.Q_ELEM_POLICY)) {
            throw new IllegalArgumentException("Input is not a wsp:Policy type");
        }
         return getPolicyOperator(element);
     }
 
     public static PolicyReference getPolicyReference(OMElement element) {
 
         if (!(Constants.URI_POLICY_NS.equals(element.getNamespace()
                 .getNamespaceURI()) && Constants.ELEM_POLICYREF.equals(element
                 .getLocalName()))) {
 
             throw new RuntimeException(
                     "Specified element is not a <wsp:PolicyReference .. />  element");
         }
 
         PolicyReference reference = new PolicyReference();
 
         // setting the URI value
         reference.setURI(element.getAttributeValue(new QName("URI")));
         return reference;
     }
 
     private static Policy getPolicyOperator(OMElement element) {
         return (Policy) processOperationElement(element, new Policy());
     }
 
     private static ExactlyOne getExactlyOneOperator(OMElement element) {
         return (ExactlyOne) processOperationElement(element, new ExactlyOne());
     }
 
     private static All getAllOperator(OMElement element) {
         return (All) processOperationElement(element, new All());
     }
 
     private static PolicyOperator processOperationElement(
             OMElement operationElement, PolicyOperator operator) {
 
         if (Constants.TYPE_POLICY == operator.getType()) {
             Policy policyOperator = (Policy) operator;
             
             OMAttribute attribute;
             OMNamespace namespace;
             QName key;
             
             for (Iterator iterator = operationElement.getAllAttributes(); iterator.hasNext(); ) {
                 attribute = (OMAttribute) iterator.next();
                 namespace = attribute.getNamespace();
                 
                 if (namespace == null) {
                     key = new QName(attribute.getLocalName());
                     
                 } else if (namespace.getPrefix() == null) {
                     key = new QName(namespace.getNamespaceURI(), attribute.getLocalName());
                     
                 } else {
                     key = new QName(namespace.getNamespaceURI(), attribute.getLocalName(), namespace.getPrefix());
                 }
                 
                 policyOperator.addAttribute(key, attribute.getAttributeValue());
             }
         }
         
         OMElement childElement;
         
 
         for (Iterator iterator = operationElement.getChildElements(); iterator
                 .hasNext();) {
             childElement = (OMElement) iterator.next();
 
             if (Constants.URI_POLICY_NS.equals(childElement.getNamespace()
                     .getNamespaceURI())) {
 
                 if (Constants.ELEM_POLICY.equals(childElement.getLocalName())) {
                     operator
                             .addPolicyComponent(getPolicyOperator(childElement));
 
                 } else if (Constants.ELEM_EXACTLYONE.equals(childElement
                         .getLocalName())) {
                     operator
                             .addPolicyComponent(getExactlyOneOperator(childElement));
 
                 } else if (Constants.ELEM_ALL.equals(childElement
                         .getLocalName())) {
                     operator.addPolicyComponent(getAllOperator(childElement));
 
                 } else if (Constants.ELEM_POLICYREF.equals(childElement
                         .getLocalName())) {
                     operator
                             .addPolicyComponent(getPolicyReference(childElement));
                 }
 
             } else {
                     operator.addPolicyComponent(factory.build(childElement));
             }
         }
         return operator;
     }
 }
