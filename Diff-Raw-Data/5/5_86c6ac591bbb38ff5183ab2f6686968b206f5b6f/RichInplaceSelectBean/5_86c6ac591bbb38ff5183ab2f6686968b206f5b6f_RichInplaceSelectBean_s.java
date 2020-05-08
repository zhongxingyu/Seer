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
 import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import org.richfaces.component.UIInplaceSelect;
 
 import org.richfaces.tests.metamer.Attributes;
 import org.richfaces.tests.metamer.model.Capital;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for rich:inplaceSelect.
  *
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean(name = "richInplaceSelectBean")
@SessionScoped
 public class RichInplaceSelectBean implements Serializable {
 
     private static final long serialVersionUID = -1L;
     private static Logger logger;
     private Attributes attributes;
     @ManagedProperty(value = "#{model.capitals}")
     private List<Capital> capitals;
     private List<SelectItem> capitalsOptions = null;
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
 
         capitalsOptions = new ArrayList<SelectItem>();
         for (Capital capital : capitals) {
             capitalsOptions.add(new SelectItem(capital.getState(), capital.getState()));
         }
 
         attributes = Attributes.getUIComponentAttributes(UIInplaceSelect.class, getClass(), false);
 
         attributes.setAttribute("defaultLabel", "Click here to edit");
         attributes.setAttribute("editEvent", "click");
         attributes.setAttribute("listHeight", "200px");
         attributes.setAttribute("listWidth", "200px");
         attributes.setAttribute("openOnEdit", true);
         attributes.setAttribute("rendered", true);
         attributes.setAttribute("saveOnSelect", true);
 
         // TODO has to be tested in another way
         attributes.remove("converter");
         attributes.remove("validator");
     }
 
     public Attributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     public void setCapitals(List<Capital> capitals) {
         this.capitals = capitals;
     }
 
     public List<SelectItem> getCapitalsOptions() {
         return capitalsOptions;
     }
 
     public void setCapitalsOptions(List<SelectItem> capitalsOptions) {
         this.capitalsOptions = capitalsOptions;
     }
 
     public void listener(ValueChangeEvent event) {
         RichBean.logToPage("* value changed: " + event.getOldValue() + " -> " + event.getNewValue());
     }
 }
