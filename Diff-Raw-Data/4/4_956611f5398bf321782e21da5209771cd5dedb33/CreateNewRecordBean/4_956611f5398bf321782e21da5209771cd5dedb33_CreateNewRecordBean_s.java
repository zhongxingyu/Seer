 package dk.statsbiblioteket.doms.gui;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import javax.xml.rpc.ServiceException;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.security.Identity;
 
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.impl.methods.ParameterImpl;
 import dk.statsbiblioteket.doms.client.methods.Method;
 import dk.statsbiblioteket.doms.client.methods.Parameter;
 import dk.statsbiblioteket.doms.client.methods.ParameterType;
 import dk.statsbiblioteket.doms.client.objects.CollectionObject;
 import dk.statsbiblioteket.doms.client.objects.ContentModelObject;
 import dk.statsbiblioteket.doms.client.objects.DigitalObject;
 import dk.statsbiblioteket.doms.client.objects.TemplateObject;
 import dk.statsbiblioteket.doms.gui.filebrowser.FileBrowserBean;
 import dk.statsbiblioteket.doms.repository.RepositoryBean;
 import dk.statsbiblioteket.doms.repository.management.Configuration;
 import dk.statsbiblioteket.doms.util.Util;
 
 
 @Name("createNewRecordBean")
 @Scope(ScopeType.PAGE)
 public class CreateNewRecordBean {
     
     @In(create = true)
     private DomsManagerBean domsManager;
     
     @In(create = true)
     private RepositoryBean repository;
     
     @In(create = true)
     private FileBrowserBean fileBrowserBean;
 
     @In
     private Identity identity;
 
     @In(value="domsGuiConfiguration")
     Configuration configuration;
     
     /** **Start Collections********************** */
     @DataModel
     private List<SelectItem> allCollectionsImport;
     private String selectedCollection;
     private Map<String, DigitalObject> collectionMap;
         
     public String getSelectedCollection() {
         return selectedCollection;
     }
 
 
     public void setSelectedCollection(String selectedCollection) throws RemoteException, ServiceException, ServerOperationFailed {
         if(selectedCollection != null) {
             this.selectedCollection = selectedCollection;
             loadImportList();
             loadTemplatesList();
         }
     }
 
     @Factory("allCollectionsImport")
     public void loadAllCollections() throws Exception, ServiceException {
         allCollectionsImport = new ArrayList<SelectItem>();
         allCollectionsImport.add(new SelectItem(null, "Select collection..."));
         collectionMap = new HashMap<String, DigitalObject>();
         List<CollectionObject> collections = repository.getAllCollections();
         for(dk.statsbiblioteket.doms.client.objects.DigitalObject col : collections) {
             allCollectionsImport.add(new SelectItem(col.getPid().toString(), col.getTitle()));
             collectionMap.put(col.getPid().toString(), (DigitalObject) col);
         }
     }
 
     private DigitalObject getSelectedCollectionObject() {
         return collectionMap.get(selectedCollection);
     }
     /** **End Collections********************** */
     
     
     
     
     /** **Start Imports********************* */
     @DataModel
     private List<SelectItem> importList;
     private String selectedImport;
     private Map<String, Method> methodMap;
 
     @Factory("importList")
     public void importListFactory() throws RemoteException, ServiceException, ServerOperationFailed {
         importList = new ArrayList<SelectItem>();
         importList.add(new SelectItem(null, "Select import..."));
         methodMap = new HashMap<String, Method>();
     }
 
     public void loadImportList() throws RemoteException, ServiceException, ServerOperationFailed {
         CollectionObject co = (CollectionObject) getSelectedCollectionObject();
         if(co != null) {
             Set<ContentModelObject> cms = co.getEntryContentModels("GUI");
 
             for(ContentModelObject cm : cms) {
                 Set<Method> methods = cm.getMethods();
                 for(Method method : methods) {
                     methodMap.put(method.getName(), method);
                     importList.add(new SelectItem(method.getName(), method.getName()));
                 }
             }
         }
     }
 
     public String getSelectedImport() {
         return selectedImport;
     }
 
     public void setSelectedImport(String selectedImport) {
         this.selectedImport = selectedImport;
         if(selectedImport != null && !selectedImport.trim().isEmpty()) {
             loadParameters();            
         }
     }
 
   
 
     /** **End Imports*********************** */
     
     
     
     
     /** **Start Templates********************* */
     @DataModel
     private List<SelectItem> templatesList; 
     private String selectedTemplate;        
     private HashMap<String, DigitalObject> objectMap;
     
     @Factory("templatesList")
     public void templatesListFactory() {
         templatesList = new ArrayList<SelectItem>();
         objectMap = new HashMap<String, DigitalObject>();
         templatesList.add(new SelectItem(null, "Select template..."));
 
     }
     
     public void loadTemplatesList() throws RemoteException, ServiceException, ServerOperationFailed {
             Set<TemplateObject> objects = domsManager.getRepository().getTemplates(this.selectedCollection);
             for (DigitalObject obj: objects) {
                 templatesList.add(new SelectItem(obj.getPid().toString(), obj.getTitle()));
                 objectMap.put(obj.getPid().toString(), obj);
             }               
     }
     
     public DigitalObject getSelectedTemplateObject() {
         return objectMap.get(selectedTemplate);
     }   
     
     public String getselectedTemplate() {
         return selectedTemplate;
     }
 
     public void setSelectedTemplate(String selectedTemplate) {
         this.selectedTemplate = selectedTemplate;       
     }
 
     /** **End Templates*********************** */
     
     
     
     /** **Start parameters*********************** */
     @DataModel
     private List<Parameter> importParameters;
 
     @Factory("importParameters")
     public void setupParameters() {
         importParameters = new ArrayList<Parameter>();
     }
 
     public void loadParameters() {
         importParameters = new ArrayList<Parameter>();
 
         Method method = methodMap.get(getSelectedImport());
         Set<Parameter> params = method.getParameters();
 
 
         for(Parameter param : params) {
             importParameters.add(param);
         }
         Collections.sort(importParameters, new Comparator<Parameter>() {
             @Override
             public int compare(Parameter o1, Parameter o2) {
                 return o1.getName().compareTo(o2.getName());
             }
         });
     }
 
     public boolean renderParameter(Parameter p) {
         if(p == null) {
             return false;
         }
         return true;
     }
     /** **End parameters *********************** */
     
     
     
     
     
     
     /** Button action controls start */
     public String createButtonPressed() throws RemoteException,  IOException,  ServerOperationFailed, 
             IOException, InvalidResourceException {
         if(selectedCollection == null) {
             Util.addFacesMessage("Please select a collection.");
             return "";
         }
         
         if(selectedTemplate == null && selectedImport == null) {
             Util.addFacesMessage("Please select a template or import method.");
             return "";            
         }
         
         if(selectedTemplate != null) {
             if(getSelectedTemplateObject() == null) {
                 Util.addFacesMessage("Template not selected.");
                 return "";               
             }
             return handleCreateNew();
         } else if(selectedImport != null) {
             if(getSelectedImport() == null) {
                 Util.addFacesMessage("Import not selected.");
                 return "";               
             }
             return handleImport();
         } else {
             Util.addFacesMessage("Something's not right...");
             return "";
         }
     }
     
     private String handleImport() throws ServerOperationFailed {
         Method method = methodMap.get(getSelectedImport());
         if(method == null) {
             FacesMessages.instance().add(new FacesMessage("Error: Import method is not valid. Please try again."));
             return "";
         }
 
         String message = method.invoke(new HashSet<Parameter>(importParameters));
         /*for (Parameter parameter : importParameters) {
             FacesMessages.instance().add(new FacesMessage(parameter.toString()));
         }*/
         FacesMessages.instance().add(new FacesMessage(message));
 
         return "/search.xhtml";    
     }
     
     private String handleCreateNew() throws RemoteException, ServerOperationFailed {
         String newPid = domsManager.getRepository().createDataObjectFromTemplate(getSelectedTemplateObject().getPid());
         domsManager.setRootDataObject(newPid);
         
         return "/editRecord.xhtml";
     }
     
     
     
     public String cancelButtonPressed() throws ServiceException, Exception {
         FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(),null,"reset");
         return "/newRecord.xhtml";
     }
         
 }
