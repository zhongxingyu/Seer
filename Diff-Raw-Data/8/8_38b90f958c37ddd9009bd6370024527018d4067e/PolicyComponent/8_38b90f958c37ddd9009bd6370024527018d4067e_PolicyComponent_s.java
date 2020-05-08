 /*
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
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 /**
  * This is an interface which any component of the framework must implement.
  */
 public interface PolicyComponent {
     
     /**
      * Serializes the PolicyComponent using an XMLStreamWriter.
      *
      * @param writer the writer that the component should write itself
      * @throws XMLStreamException if an errors in the process of serialization of the
      *                            PolicyComponent.
      */
     public void serialize(XMLStreamWriter writer) throws XMLStreamException;
 
     /**
      * Returns a short value which uniquely identify the type of the
      * PolicyComponent.
      *
     * @return Constants.POLICY for Policy type PolicyComponent
     *         Constants.EXACTLYONE for ExactlyOne type PolicyComponent
     *         Constants.ALL for All type PolicyComponent
     *         Constants.ASSERTION for Assertion type PolicyComponent
      */
     public short getType();
 
     /**
      * Returns true if the argument is equal to self.
      *  
      * @param policyComponent the PolicyComponent to check whether self is 
      *      logically equal or not 
      * @return true if the argument is equal to self.
      */
     public boolean equal(PolicyComponent policyComponent);
 }
