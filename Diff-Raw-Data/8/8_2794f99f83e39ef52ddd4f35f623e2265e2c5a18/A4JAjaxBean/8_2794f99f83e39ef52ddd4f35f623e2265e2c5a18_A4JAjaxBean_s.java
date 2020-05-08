 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 
 package org.richfaces.tests.metamer.bean;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.ajax4jsf.component.behavior.AjaxBehavior;
 
 import org.richfaces.tests.metamer.Attributes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for a4j:ajax.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean(name = "a4jAjaxBean")
@SessionScoped
 public class A4JAjaxBean implements Serializable {
 
     private static final long serialVersionUID = -546567867L;
     private static Logger logger;
     private Attributes attributes;
     private String input;
     private List<String> cars;
     private String car;
     private boolean boolVal;
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
 
         attributes = Attributes.getBehaviorAttributes(AjaxBehavior.class, getClass());
         // the 'event' attribute for behavior tag must be a literal
         attributes.remove("event");
         
         attributes.setAttribute("render", "output1, output2");
         attributes.setAttribute("execute", "@form");
                 
         cars = new ArrayList<String>();
         cars.add("Ferrari");
         cars.add("Lexus");
         car = "Ferrari";
     }
 
     /**
      * Getter for attributes.
      * 
      * @return A map containing all attributes of tested component. Name of the component is key in the map.
      */
     public Attributes getAttributes() {
         return attributes;
     }
 
     /**
      * Setter for attributes.
      * 
      * @param attributes
      *            map containing all attributes of tested component. Name of the component is key in the map.
      */
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     /**
      * Getter for input.
      * @return content of the input field on the page
      */
     public String getInput() {
         return input;
     }
 
     /**
      * Setter for input.
      * @param input new content of the input field on the page
      */
     public void setInput(String input) {
         this.input = input;
     }
 
     public String getCar() {
         return car;
     }
 
     public void setCar(String car) {
         this.car = car;
     }
 
     public List<String> getCars() {
         return cars;
     }
 
     public void setCars(List<String> cars) {
         this.cars = cars;
     }
 
     public boolean isBoolVal() {
         return boolVal;
     }
 
     public void setBoolVal(boolean boolVal) {
         this.boolVal = boolVal;
     }
     
 }
