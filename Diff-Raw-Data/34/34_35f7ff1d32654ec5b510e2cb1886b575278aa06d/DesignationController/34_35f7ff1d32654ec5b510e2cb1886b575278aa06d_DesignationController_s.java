 /*
  * MSc(Biomedical Informatics) Project
  * 
  * Development and Implementation of a Web-based Combined Data Repository of 
  Genealogical, Clinical, Laboratory and Genetic Data 
  * and
  * a Set of Related Tools
  */
 package gov.health.bean;
 
 import gov.health.facade.DesignationFacade;
 import gov.health.entity.Designation;
 import gov.health.entity.PersonInstitution;
 import gov.health.facade.InstitutionTypeFacade;
 import gov.health.facade.PersonInstitutionFacade;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 
 /**
  *
  * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
  * Informatics)
  */
 @ManagedBean
 @SessionScoped
 public final class DesignationController implements Serializable {
 
     @EJB
     private DesignationFacade facade;
     @EJB
     PersonInstitutionFacade piFacade;
     @ManagedProperty(value = "#{sessionController}")
     SessionController sessionController;
     List<Designation> officialDesignations;
     List<Designation> unOfficialDesignations;
     List<Designation> allDesignations;
     List<Designation> mappedDesignationsToOfficial;
     List<Designation> unmappedDesignations;
     Designation officialDesignation;
     Designation mappedDesignation;
     Designation unmappedDesignation;
     Designation oldDesignation;
     private Designation current;
     private List<Designation> items = null;
    private int selectedItemIndex;
     String selectText = "";
     String sql;
     Integer offSel = 0;
 
     public Designation getOldDesignation() {
         return oldDesignation;
     }
 
     public void setOldDesignation(Designation oldDesignation) {
         this.oldDesignation = oldDesignation;
     }
 
     public PersonInstitutionFacade getPiFacade() {
         return piFacade;
     }
 
     public void setPiFacade(PersonInstitutionFacade piFacade) {
         this.piFacade = piFacade;
     }
 
     public int replaceDesignations(Designation going, Designation comming) {
         sql = "select pi from PersonInstitution pi where pi.designation.id = " + going.getId() ;
         List<PersonInstitution> pis= getPiFacade().findBySQL(sql);
         for (PersonInstitution pi:pis){
             pi.setDesignation(comming);
             getPiFacade().edit(pi);
         }
         return pis.size();
     }
 
     public Integer getOffSel() {
         return offSel;
     }
 
     public void setOffSel(Integer offSel) {
         recreateModel();
         this.offSel = offSel;
     }
 
     public Designation getMappedDesignation() {
         return mappedDesignation;
     }
 
     public void setMappedDesignation(Designation mappedDesignation) {
         this.mappedDesignation = mappedDesignation;
     }
 
     public Designation getUnmappedDesignation() {
         return unmappedDesignation;
     }
 
     public void setUnmappedDesignation(Designation unmappedDesignation) {
         this.unmappedDesignation = unmappedDesignation;
     }
 
     public List<Designation> getUnmappedDesignations() {
         sql = "select d from Designation d where d.retired = false and d.official = false and d.mappedToDesignation is null order by d.name";
         unmappedDesignations = getFacade().findBySQL(sql);
         return unmappedDesignations;
     }
 
     public void setUnmappedDesignations(List<Designation> unmappedDesignations) {
         this.unmappedDesignations = unmappedDesignations;
     }
 
     public void removeMapped() {
         if (mappedDesignation == null) {
             JsfUtil.addErrorMessage("Please select a designation to mapped to");
             return;
         }
         mappedDesignation.setMappedToDesignation(null);
         JsfUtil.addSuccessMessage("Mapping Removed");
     }
 
     public void addMappedDesignation() {
         if (unmappedDesignation == null) {
             JsfUtil.addErrorMessage("Please select a designation to mapped to");
             return;
         }
         unmappedDesignation.setMappedToDesignation(mappedDesignation);
     }
 
     public List<Designation> getMappedDesignationsToOfficial() {
         if (officialDesignation == null) {
             return null;
         } else {
             sql = "select d from Designation d where d.mappedDesignation.id = " + getOfficialDesignation().getId();
             return getFacade().findBySQL(sql);
         }
     }
 
     public void setMappedDesignationsToOfficial(List<Designation> mappedDesignationsToOfficial) {
         this.mappedDesignationsToOfficial = mappedDesignationsToOfficial;
     }
 
     public Designation getOfficialDesignation() {
         return officialDesignation;
     }
 
     public void setOfficialDesignation(Designation officialDesignation) {
         this.officialDesignation = officialDesignation;
     }
 
     public String getSql() {
         return sql;
     }
 
     public void setSql(String sql) {
         this.sql = sql;
     }
 
     public List<Designation> getAllDesignations() {
         sql = "select d from Designation d where d.retired = false order by d.name";
         if (allDesignations == null) {
             allDesignations = getFacade().findBySQL(sql);
         }
         return allDesignations;
     }
 
     public void setAllDesignations(List<Designation> allDesignations) {
         this.allDesignations = allDesignations;
     }
 
     public List<Designation> getOfficialDesignations() {
         sql = "select d from Designation d where d.retired = false and d.official = true order by d.name";
         if (officialDesignations == null) {
             officialDesignations = getFacade().findBySQL(sql);
         }
         return officialDesignations;
     }
 
     public void setOfficialDesignations(List<Designation> officialDesignations) {
         this.officialDesignations = officialDesignations;
     }
 
     public List<Designation> getUnOfficialDesignations() {
         sql = "select d from Designation d where d.retired = false and d.official = false order by d.name";
         if (unOfficialDesignations == null) {
             unOfficialDesignations = getFacade().findBySQL(sql);
         }
         return unOfficialDesignations;
     }
 
     public void setUnOfficialDesignations(List<Designation> unOfficialDesignations) {
         this.unOfficialDesignations = unOfficialDesignations;
     }
 
     public Designation findDesignationByName(String name) {
         Designation temDes;
         sql = "select d from Designation d where d.retired = false and d.name = '" + name + "'";
         temDes = getFacade().findFirstBySQL(sql);
         if (temDes == null) {
             sql = "select d from Designation d where d.name = '" + name + "'";
             temDes = getFacade().findFirstBySQL(sql);
             if (temDes == null) {
                 temDes = new Designation();
                 temDes.setName(name);
                 getFacade().create(temDes);
             } else {
                 temDes.setRetired(false);
                 getFacade().edit(temDes);
             }
         }
         return temDes;
     }
 
     public DesignationController() {
     }
 
     public DesignationFacade getFacade() {
         return facade;
     }
 
     public SessionController getSessionController() {
         return sessionController;
     }
 
     public void setSessionController(SessionController sessionController) {
         this.sessionController = sessionController;
     }
 
     public List<Designation> getLstItems() {
         return getFacade().findBySQL("Select d From Designation d");
     }
 
     public void setLstItems(List<Designation> lstItems) {
         this.officialDesignations = lstItems;
     }
 
    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        this.selectedItemIndex = selectedItemIndex;
    }

    public Designation getCurrent() {
         if (current == null) {
             current = new Designation();
         }
         return current;
     }
 
     public void setCurrent(Designation current) {
         oldDesignation = current.getMappedToDesignation();
         this.current = current;
     }
 
     public List<Designation> getItems() {
 //        items = getFacade().findAll("name", true);
         if (items == null) {
             if (getOffSel() == 0) {
                 sql = "SELECT i FROM Designation i where i.retired=false order by i.name";
                 items = getAllDesignations();
             } else if (getOffSel() == 1) {
                 items = getOfficialDesignations();
                 sql = "SELECT i FROM Designation i where i.retired=false and i.official = true order by i.name";
             } else if (getOffSel() == 2) {
                 items = getUnOfficialDesignations();
                 sql = "SELECT i FROM Designation i where i.retired=false and i.official = false order by i.name";
             } else if (getOffSel() == 3) {
                 items = getUnmappedDesignations();
                 sql = "SELECT i FROM Designation i where i.retired=false and i.official = false and i.mappedToDesignation is null order by i.name";
             } else {
                 sql = "SELECT i FROM Designation i where i.retired=false order by i.name";
             }
         }
         return items;
     }
 
     public static int intValue(long value) {
         int valueInt = (int) value;
         if (valueInt != value) {
             throw new IllegalArgumentException(
                     "The long value " + value + " is not within range of the int type");
         }
         return valueInt;
     }
 
     public List<Designation> searchItems() {
         recreateModel();
         if (selectText.equals("")) {
             items = getItems();
         } else {
             if (getOffSel() == 0) {
                 sql = "SELECT i FROM Designation i where i.retired=false and lower(i.name) like '%" + selectText.toLowerCase() + "%' order by i.name";
                 items = getAllDesignations();
             } else if (getOffSel() == 1) {
                 items = getOfficialDesignations();
                 sql = "SELECT i FROM Designation i where i.retired=false and i.official = true and lower(i.name) like '%" + selectText.toLowerCase() + "%' order by i.name";
             } else if (getOffSel() == 2) {
                 items = getUnOfficialDesignations();
                 sql = "SELECT i FROM Designation i where i.retired=false and i.official = false and lower(i.name) like '%" + selectText.toLowerCase() + "%' order by i.name";
             } else if (getOffSel() == 3) {
                 items = getUnmappedDesignations();
                 sql = "SELECT i FROM Designation i where i.retired=false and i.official = false and i.mappedToDesignation is null and lower(i.name) like '%" + selectText.toLowerCase() + "%' order by i.name";
             } else {
                 sql = "SELECT i FROM Designation i where i.retired=false and lower(i.name) like '%" + selectText.toLowerCase() + "%' order by i.name";
             }
             items = getFacade().findBySQL(sql);
             if (items.size() > 0) {
                 current = items.get(0);
                 Long temLong = current.getId();
                selectedItemIndex = intValue(temLong);
             } else {
                 current = null;
                selectedItemIndex = -1;
             }
 
         }
         return items;
     }
 
     public Designation searchItem(String itemName, boolean createNewIfNotPresent) {
         Designation searchedItem = getFacade().findFirstBySQL("select d from Designation d where d.retired=false and lower(d.name)= '" + itemName.toLowerCase() + "'");
         if (createNewIfNotPresent) {
             searchedItem = new Designation();
             searchedItem.setName(itemName);
             searchedItem.setCreatedAt(Calendar.getInstance().getTime());
             searchedItem.setCreater(sessionController.loggedUser);
             getFacade().create(searchedItem);
         }
         return searchedItem;
     }
 
     private void recreateModel() {
         items = null;
         setOfficialDesignations(null);
         setUnOfficialDesignations(null);
         setMappedDesignation(null);
         setUnmappedDesignations(null);
     }
 
     public void prepareAdd() {
        selectedItemIndex = -1;
         current = new Designation();
     }
 
     public void saveSelected() {
         String msg;
         if (sessionController.getPrivilege().isInstUser() == false) {

             JsfUtil.addErrorMessage("You are not autherized to make changes to any content");
             return;
         }
        if (selectedItemIndex > 0) {
             getFacade().edit(current);
             msg = new MessageProvider().getValue("savedOldSuccessfully");
         } else {
             current.setCreatedAt(Calendar.getInstance().getTime());
             current.setCreater(sessionController.loggedUser);
            current.setOfficial(true);
             getFacade().create(current);
             msg = new MessageProvider().getValue("savedNewSuccessfully");
         }
         if (oldDesignation != current.getMappedToDesignation()) {
             msg = msg + " " + replaceDesignations(oldDesignation, current.getMappedToDesignation()) + " records updated.";
         }
         JsfUtil.addSuccessMessage(msg);
         recreateModel();
         getItems();
         selectText = "";
        selectedItemIndex = intValue(current.getId());
     }
 
     public void addDirectly() {
         JsfUtil.addSuccessMessage("1");
         try {
 
             current.setCreatedAt(Calendar.getInstance().getTime());
             current.setCreater(sessionController.loggedUser);
 
             getFacade().create(current);
             JsfUtil.addSuccessMessage(new MessageProvider().getValue("savedNewSuccessfully"));
             current = new Designation();
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, "Error");
         }
 
     }
 
     public void delete() {
         if (current != null) {
             current.setRetired(true);
             current.setRetiredAt(Calendar.getInstance().getTime());
             current.setRetirer(sessionController.loggedUser);
             getFacade().edit(current);
             JsfUtil.addSuccessMessage(new MessageProvider().getValue("deleteSuccessful"));
         } else {
             JsfUtil.addErrorMessage(new MessageProvider().getValue("nothingToDelete"));
         }
         recreateModel();
         getItems();
         selectText = "";
        selectedItemIndex = -1;
         current = null;
     }
 
     public String getSelectText() {
         return selectText;
     }
 
     public void setSelectText(String selectText) {
         this.selectText = selectText;
         searchItems();
     }
 
     @FacesConverter(forClass = Designation.class)
     public static class DesignationControllerConverter implements Converter {
 
         public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
             if (value == null || value.length() == 0) {
                 return null;
             }
             DesignationController controller = (DesignationController) facesContext.getApplication().getELResolver().
                     getValue(facesContext.getELContext(), null, "designationController");
             return controller.facade.find(getKey(value));
         }
 
         java.lang.Long getKey(String value) {
             java.lang.Long key;
             key = Long.valueOf(value);
             return key;
         }
 
         String getStringKey(java.lang.Long value) {
             StringBuffer sb = new StringBuffer();
             sb.append(value);
             return sb.toString();
         }
 
         public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
             if (object == null) {
                 return null;
             }
             if (object instanceof Designation) {
                 Designation o = (Designation) object;
                 return getStringKey(o.getId());
             } else {
                 throw new IllegalArgumentException("object " + object + " is of type "
                         + object.getClass().getName() + "; expected type: " + DesignationController.class.getName());
             }
         }
     }
 }
