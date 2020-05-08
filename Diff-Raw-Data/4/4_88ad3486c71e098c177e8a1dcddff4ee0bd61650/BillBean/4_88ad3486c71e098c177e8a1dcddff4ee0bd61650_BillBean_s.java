 package com.marbl.rekeningrijders.website.bean;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.domain.*;
 import com.marbl.rekeningrijders.website.service.RekeningRijdersService;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Map;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 //</editor-fold>
 
 @Named
 @SessionScoped
 public class BillBean implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     @Inject
     private RekeningRijdersService service;
     private ArrayList<Bill> all;
     private Bill current;
     @Inject
     private DriverBean driverBean;
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
     public ArrayList<Bill> getAll() {
         return all;
     }
 
     public Bill getCurrent() {
         return current;
     }
 
    public ArrayList<Object> getMovements() {
         if (current != null) {
             return current.getMovements();
         } else {
             return null;
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Methods">
     public void findAll() {
         all = service.findBillsByBSN(driverBean.getLoggedInDriver().getBSN());
     }
 
     public void findCurrent() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
 
         if (requestParameterMap.containsKey("id")) {
             Long id = Long.parseLong(requestParameterMap.get("id"));
             current = service.findBill(id);
         }
 
         if (current == null) {
             showOverview();
         }
     }
     
     public Driver findLoggedInDriver() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
 
         if (request.getUserPrincipal() != null) {
             int bsn = Integer.parseInt(request.getUserPrincipal().getName());
             return service.findDriver(bsn);
         } else {
             return null;
         }
     }
 
     public void saveChanges() {
         service.editBill(current);
         showOverview();
     }
 
     public void showOverview() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         
         try {
             externalContext.redirect("bill-overview.xhtml");
             current = null;
         } catch (IOException ex) {
         }
     }
     //</editor-fold>
 }
