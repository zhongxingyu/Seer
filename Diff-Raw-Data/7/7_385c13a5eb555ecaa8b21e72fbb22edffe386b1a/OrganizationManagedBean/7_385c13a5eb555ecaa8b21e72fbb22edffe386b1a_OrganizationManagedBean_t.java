 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package oss.process.scrum.managed.bean;
 
 import java.util.ArrayList;
 import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
 
 /**
  *
  * @author gpuliyar
  */
@ManagedBean
@RequestScoped
 public class OrganizationManagedBean {
 
     public List<Organization> organizations;
 
     public List<Organization> getAllOrganizations() {
         organizations = new ArrayList<Organization>();
         Organization organization = new Organization();
         organization.setCode("GRC");
         organization.setDescription("GRC Description");
         organization.setId("1");
         organization.setName("Governance Risk And Compliance");
         organizations.add(organization);
         organization = new Organization();
         organization.setCode("ITG");
         organization.setDescription("ITGRC Description");
         organization.setId("2");
         organization.setName("IT Governance Risk And Compliance");
         organizations.add(organization);
         organization = new Organization();
         organization.setCode("RSK");
         organization.setDescription("RSK Description");
         organization.setId("3");
         organization.setName("Risk Management");
         organizations.add(organization);
         return organizations;
     }
 }
