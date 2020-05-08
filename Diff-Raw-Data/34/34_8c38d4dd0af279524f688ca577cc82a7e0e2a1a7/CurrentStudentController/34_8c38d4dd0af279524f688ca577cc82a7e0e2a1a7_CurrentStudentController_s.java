 package controllers;
 
 import entities.CurrentStudent;
 import controllers.util.JsfUtil;
 import controllers.util.PaginationHelper;
 import beans.CurrentStudentFacade;
 import entities.Attendance;
 import entities.FacultySubject;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.SelectItem;
 
 @ManagedBean(name = "currentStudentController")
 @SessionScoped
 public class CurrentStudentController implements Serializable {
 
     private CurrentStudent current;
     private DataModel items = null;
     private DataModel attendanceByDiv = null;
     @EJB
     private beans.CurrentStudentFacade ejbFacade;
     private PaginationHelper pagination;
     private int selectedItemIndex;
 
     public CurrentStudentController() {
     }
     
     public DataModel getAttendanceByDiv (FacultySubject f) {
         String div = f.getDivision();
         short batch = f.getBatch();
         short semester = f.getIdSubject().getSemester();
         if(batch==0){
            attendanceByDiv = new ListDataModel(getFacade().getCurrentStudentByDivTheory(semester, div));
             return attendanceByDiv;
         }
         else {
         
            attendanceByDiv = new ListDataModel(getFacade().getCurrentStudentByDiv(semester, div, batch));
             return attendanceByDiv;
         }
     }
     
     public List<CurrentStudent> getCurrentStudentList ()
     {
         return (List<CurrentStudent>) attendanceByDiv.getWrappedData();
     }
     public void setCurrentStudentList(List<CurrentStudent> s) {
         
         attendanceByDiv.setWrappedData(s);
     }
 
     public CurrentStudent getSelected() {
         if (current == null) {
             current = new CurrentStudent();
             selectedItemIndex = -1;
         }
         return current;
     }
 
     private CurrentStudentFacade getFacade() {
         return ejbFacade;
     }
 
     public PaginationHelper getPagination() {
         if (pagination == null) {
             pagination = new PaginationHelper(10) {
                 @Override
                 public int getItemsCount() {
                     return getFacade().count();
                 }
 
                 @Override
                 public DataModel createPageDataModel() {
                     return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                 }
             };
         }
         return pagination;
     }
 
     public String prepareList() {
         recreateModel();
         return "List";
     }
     
     public String prepareListA() {
         recreateModel();
         return "ListA";
     }
 
     public String prepareView() {
         current = (CurrentStudent) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "View";
     }
 
     public String prepareCreate() {
         current = new CurrentStudent();
         Date d = new Date();
         d.setMonth(6);
         d.setDate(1);
         d.setYear(113);
         current.setAcademicYear(d);
         selectedItemIndex = -1;
         return "Create";
     }
 
     public String create() {
         try {
             getFacade().create(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CurrentStudentCreated"));
             return prepareCreate();
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String prepareEdit() {
         current = (CurrentStudent) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "Edit";
     }
 
     public String update() {
         try {
             Date d = new Date();
             d = current.getAcademicYear();
             d.setMonth(6);
             current.setAcademicYear(d);
             getFacade().edit(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CurrentStudentUpdated"));
             return "View";
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String destroy() {
         current = (CurrentStudent) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         performDestroy();
         recreatePagination();
         recreateModel();
         return "List";
     }
 
     public String destroyAndView() {
         performDestroy();
         recreateModel();
         updateCurrentItem();
         if (selectedItemIndex >= 0) {
             return "View";
         } else {
             // all items were removed - go back to list
             recreateModel();
             return "List";
         }
     }
 
     private void performDestroy() {
         try {
             getFacade().remove(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CurrentStudentDeleted"));
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
         }
     }
 
     private void updateCurrentItem() {
         int count = getFacade().count();
         if (selectedItemIndex >= count) {
             // selected index cannot be bigger than number of items:
             selectedItemIndex = count - 1;
             // go to previous page if last page disappeared:
             if (pagination.getPageFirstItem() >= count) {
                 pagination.previousPage();
             }
         }
         if (selectedItemIndex >= 0) {
             current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
         }
     }
 
     public DataModel getItems() {
         if (items == null) {
             items = getPagination().createPageDataModel();
         }
         return items;
     }
 
     private void recreateModel() {
         items = null;
         attendanceByDiv =null;
     }
 
     private void recreatePagination() {
         pagination = null;
     }
 
     public String next() {
         getPagination().nextPage();
         recreateModel();
         return "List";
     }
 
     public String previous() {
         getPagination().previousPage();
         recreateModel();
         return "List";
     }
 
     public SelectItem[] getItemsAvailableSelectMany() {
         return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
     }
 
     public SelectItem[] getItemsAvailableSelectOne() {
         return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
     }
 
     @FacesConverter(forClass = CurrentStudent.class)
     public static class CurrentStudentControllerConverter implements Converter {
 
         public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
             if (value == null || value.length() == 0) {
                 return null;
             }
             CurrentStudentController controller = (CurrentStudentController) facesContext.getApplication().getELResolver().
                     getValue(facesContext.getELContext(), null, "currentStudentController");
             return controller.ejbFacade.find(getKey(value));
         }
 
         java.lang.Integer getKey(String value) {
             java.lang.Integer key;
             key = Integer.valueOf(value);
             return key;
         }
 
         String getStringKey(java.lang.Integer value) {
             StringBuffer sb = new StringBuffer();
             sb.append(value);
             return sb.toString();
         }
 
         public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
             if (object == null) {
                 return null;
             }
             if (object instanceof CurrentStudent) {
                 CurrentStudent o = (CurrentStudent) object;
                 return getStringKey(o.getIdCurrentStudent());
             } else {
                 throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CurrentStudent.class.getName());
             }
         }
     }
 }
