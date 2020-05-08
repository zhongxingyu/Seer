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
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMNamespace;
 import org.apache.neethi.builders.AssertionBuilder;
 
 import sun.misc.Service;
 
 /**
  * AssertionFactory is used to create an Assertion from an OMElement. It uses an
  * appropriate AssertionBuilder instace to create an Assertion based on the
  * QName of the given OMElement. Domain Policy authors could right custom
  * AssertionBuilders to build Assertions for domain specific assertions and
  * register them.
  * 
  */
 public class AssertionBuilderFactory {
     
     public static final String POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";
     
     public static final String POLICY = "Policy";
     
     public static final String EXACTLY_ONE = "ExactlyOne";
     
     public static final String ALL = "All";
     
     private final QName XML_ASSERTION_BUILDER = new QName(
             "http://test.org/test", "test");
 
     private static HashMap registeredBuilders = new HashMap();
     
     static {
         AssertionBuilder builder;
         
         for (Iterator providers = Service.providers(AssertionBuilder.class); providers.hasNext();) {
             builder = (AssertionBuilder) providers.next();
            //registerBuilder(builder.getKnownElement(), builder);
         }
         
     }
 
     public static void registerBuilder(QName key, AssertionBuilder builder) {
         registeredBuilders.put(key, builder);
     }
     
     public AssertionBuilderFactory() {
     }
     
     /**
      * Returns an assertion
      * @param element
      * @return
      */
     public Assertion build(OMElement element) {
         OMNamespace namespace = element.getNamespace();
 
         AssertionBuilder builder;
 
         if (namespace != null) {
             QName qname = new QName(namespace.getNamespaceURI(), element.getLocalName());
             builder = (AssertionBuilder) registeredBuilders.get(qname);
 
             if (builder != null) {
                 return builder.build(element, this);
             }
         }
 
         builder = (AssertionBuilder) registeredBuilders
                 .get(XML_ASSERTION_BUILDER);
         return builder.build(element, this);
     }
 
     public AssertionBuilder getBuilder(QName qname) {
         return (AssertionBuilder) registeredBuilders.get(qname);
     }
     
 
 }
