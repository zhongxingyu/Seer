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
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
 import javax.swing.tree.TreeNode;
 
 import org.richfaces.component.UITree;
 import org.richfaces.tests.metamer.Attributes;
 import org.richfaces.tests.metamer.model.tree.CompactDisc;
 import org.richfaces.tests.metamer.model.tree.CompactDiscXmlDescriptor;
 import org.richfaces.tests.metamer.model.tree.Company;
 import org.richfaces.tests.metamer.model.tree.Country;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for rich:list.
  * 
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 @ManagedBean(name = "richTreeBean")
// cannot be view-scoped (see https://jira.jboss.org/browse/RF-9287)
@SessionScoped
 public class RichTreeBean implements Serializable {
 
     private static final long serialVersionUID = 4008175400649809L;
     private static Logger logger;
     private Attributes attributes;
     private List<TreeNode> root = new ArrayList<TreeNode>();
 
     @ManagedProperty(value = "#{model}")
     private Model model;
 
     private Map<String, Country> countriesCache = new HashMap<String, Country>();
     private Map<String, Company> companiesCache = new HashMap<String, Company>();
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
 
         attributes = Attributes.getUIComponentAttributes(UITree.class, getClass(), false);
         attributes.get("rendered").setValue(true);
         attributes.get("toggleType").setValue("ajax");
         attributes.get("selectionType").setValue("ajax");
 
         for (CompactDiscXmlDescriptor descriptor : model.getCompactDiscs()) {
             createCompactDisc(descriptor);
         }
     }
 
     public Attributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     private CompactDisc createCompactDisc(CompactDiscXmlDescriptor descriptor) {
         final Company company = findOrCreateCompany(descriptor);
 
         CompactDisc cd = new CompactDisc(descriptor.getTitle(), descriptor.getArtist(), company, descriptor.getPrice(),
             descriptor.getYear());
         company.getCds().add(cd);
         return cd;
     }
 
     private Company findOrCreateCompany(CompactDiscXmlDescriptor descriptor) {
         final Country country = findOrCreateCountry(descriptor);
 
         String companyName = descriptor.getCompany();
         Company company = companiesCache.get(companyName);
         if (company == null) {
             company = new Company();
             company.setName(companyName);
             company.setParent(country);
             country.getCompanies().add(company);
             companiesCache.put(companyName, company);
         }
         return company;
     }
 
     private Country findOrCreateCountry(CompactDiscXmlDescriptor descriptor) {
         String countryName = descriptor.getCountry();
         Country country = countriesCache.get(countryName);
         if (country == null) {
             country = new Country();
             country.setName(countryName);
             countriesCache.put(countryName, country);
             root.add(country);
         }
         return country;
     }
     
     public void setModel(Model model) {
         this.model = model;
     }
     
     public List<TreeNode> getRoot() {
         return root;
     }
 }
