 package org.mcguppy.eventplaner.jsf;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.model.SelectItem;
 import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
 import org.mcguppy.eventplaner.jpa.controllers.exceptions.NonexistentEntityException;
 import org.mcguppy.eventplaner.jpa.entities.Shift;
 import org.mcguppy.eventplaner.jpa.entities.StaffMember;
 import org.mcguppy.eventplaner.jpa.entities.StaffMember.Title;
 import org.mcguppy.eventplaner.jsf.util.JsfUtil;
 
 /**
  *
  * @author stefan meichtry
  */
 public class StaffMemberController {
 
     public StaffMemberController() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         jpaController = (StaffMemberJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "staffMemberJpa");
         converter = new StaffMemberConverter();
     }
     private StaffMember staffMember = null;
     private List<StaffMember> staffMemberItems = null;
     private StaffMemberJpaController jpaController = null;
     private StaffMemberConverter converter = null;
 
     public SelectItem[] getStaffMemberItemsAvailableSelectMany() {
         List<StaffMember> itemsList = jpaController.findStaffMemberEntities();
         Collections.sort(itemsList);
         return JsfUtil.getSelectItems(itemsList, false);
     }
 
     public SelectItem[] getStaffMemberItemsAvailableSelectOne() {
         return JsfUtil.getSelectItems(jpaController.findStaffMemberEntities(), true);
     }
 
     public SelectItem[] getShiftStaffMemberItemsAvailableSelectOne() {
         ShiftController shiftController = new ShiftController();
         Shift shift = (Shift) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentShift", shiftController.getConverter(), null);
         List<StaffMember> staffMemberList = new ArrayList<StaffMember>();
         if (shift.getStaffMembers() != null) {
             for (StaffMember staffMemberIter : shift.getStaffMembers()) {
                 staffMemberList.add(jpaController.findStaffMember(staffMemberIter.getId()));
             }
         }
         return JsfUtil.getSelectItems(staffMemberList, true);
     }
 
     public StaffMember getStaffMember() {
         if (staffMember == null) {
             staffMember = (StaffMember) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentStaffMember", converter, null);
         }
         if (staffMember == null) {
             staffMember = new StaffMember();
         }
         if (null != staffMember.getShifts() && staffMember.getShifts().size() > 0) {
             Collections.sort((List<Shift>) staffMember.getShifts());
         }
         return staffMember;
     }
 
     public int getItemCount() {
         return jpaController.getStaffMemberCount();
     }
 
     public String listSetup() {
         reset();
         return "staffMemberList";
     }
 
     public String createSetup() {
         reset();
         staffMember = new StaffMember();
         return "staffMemberCreate";
     }
 
     public String create() {
         try {
             jpaController.create(staffMember);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("StaffMemberCreated"));
         } catch (Exception e) {
             JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
         return listSetup();
     }
 
     public String detailSetup() {
         return scalarSetup("staffMemberDetail");
     }
 
     public String editSetup() {
         return scalarSetup("staffMemberEdit");
     }
 
     public String destroySetup() {
         JsfUtil.addWarnMessage(ResourceBundle.getBundle("/Bundle").getString("DestroyStaffMemberWarning"));
         return scalarSetup("staffMemberDestroy");
     }
 
     private String scalarSetup(String destination) {
         reset();
         staffMember = (StaffMember) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentStaffMember", converter, null);
         if (staffMember == null) {
             JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("StaffMemberMissing"));
             return relatedOrListOutcome();
         }
         return destination;
     }
 
     public String edit() {
         String staffMemberString = converter.getAsString(FacesContext.getCurrentInstance(), null, staffMember);
         String currentStaffMemberString = JsfUtil.getRequestParameter("jsfcrud.currentStaffMember");
         if (staffMemberString == null || staffMemberString.length() == 0 || !staffMemberString.equals(currentStaffMemberString)) {
             String outcome = editSetup();
             if ("staffMemberEdit".equals(outcome)) {
                 JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("StaffMemberEditError"));
             }
             return outcome;
         }
         try {
             jpaController.edit(staffMember);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("StaffMemberUpdated"));
         } catch (NonexistentEntityException ne) {
             JsfUtil.addErrorMessage(ne.getLocalizedMessage());
             return listSetup();
         } catch (Exception e) {
             JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
         return detailSetup();
     }
 
     public String destroy() {
         String staffMemberString = converter.getAsString(FacesContext.getCurrentInstance(), null, staffMember);
         String currentStaffMemberString = JsfUtil.getRequestParameter("jsfcrud.currentStaffMember");
         if (staffMemberString == null || staffMemberString.length() == 0 || !staffMemberString.equals(currentStaffMemberString)) {
             String outcome = destroySetup();
             if ("staffMemberDestroy".equals(outcome)) {
                 JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("StaffMemberDestoryError"));
             }
             return outcome;
         }
         try {
             jpaController.destroy(staffMember);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("StaffMemberDeleted"));
         } catch (NonexistentEntityException ne) {
             JsfUtil.addErrorMessage(ne.getLocalizedMessage());
             return relatedOrListOutcome();
         } catch (Exception e) {
             JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
         return relatedOrListOutcome();
     }
 
     private String relatedOrListOutcome() {
         String relatedControllerOutcome = relatedControllerOutcome();
         if (relatedControllerOutcome != null) {
             return relatedControllerOutcome;
         }
         return listSetup();
     }
 
     public List<StaffMember> getStaffMemberItems() {
         if (staffMemberItems == null) {
             staffMemberItems = jpaController.findStaffMemberEntities();
             Collections.sort(staffMemberItems);
         }
         return staffMemberItems;
     }
 
    public List<StaffMember> getRefreshedStaffMemberItems() {
        this.reset();
        return this.getStaffMemberItems();
    }

     private String relatedControllerOutcome() {
         String relatedControllerString = JsfUtil.getRequestParameter("jsfcrud.relatedController");
         String relatedControllerTypeString = JsfUtil.getRequestParameter("jsfcrud.relatedControllerType");
         if (relatedControllerString != null && relatedControllerTypeString != null) {
             FacesContext context = FacesContext.getCurrentInstance();
             Object relatedController = context.getApplication().getELResolver().getValue(context.getELContext(), null, relatedControllerString);
             try {
                 Class<?> relatedControllerType = Class.forName(relatedControllerTypeString);
                 Method detailSetupMethod = relatedControllerType.getMethod("detailSetup");
                 return (String) detailSetupMethod.invoke(relatedController);
             } catch (ClassNotFoundException e) {
                 throw new FacesException(e);
             } catch (NoSuchMethodException e) {
                 throw new FacesException(e);
             } catch (IllegalAccessException e) {
                 throw new FacesException(e);
             } catch (InvocationTargetException e) {
                 throw new FacesException(e);
             }
         }
         return null;
     }
 
     private void reset() {
         staffMember = null;
         staffMemberItems = null;
     }
 
     public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
         StaffMember newStaffMember = new StaffMember();
         String newCustomerString = converter.getAsString(FacesContext.getCurrentInstance(), null, newStaffMember);
         String customerString = converter.getAsString(FacesContext.getCurrentInstance(), null, staffMember);
         if (!newCustomerString.equals(customerString)) {
             createSetup();
         }
     }
 
     public Converter getConverter() {
         return converter;
     }
 
     public SelectItem[] getTitlesAvailableSelectOne() {
         List<Title> titleList = Arrays.asList(Title.values());
         return JsfUtil.getSelectItems(titleList, true);
     }
 }
