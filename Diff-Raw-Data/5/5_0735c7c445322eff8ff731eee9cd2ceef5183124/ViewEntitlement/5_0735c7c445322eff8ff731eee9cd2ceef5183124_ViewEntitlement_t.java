 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.Entitlement;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class ViewEntitlement implements Serializable {
 
     private List<Resource> resources = new ArrayList<Resource>();
     private List<Resource> exceptions = new ArrayList<Resource>();
     private List<Action> actions = new ArrayList<Action>();
     private ViewApplication viewApplication;
 
     public ViewEntitlement() {
         // nothing
     }
 
     public ViewEntitlement(Entitlement e, Map<String, ViewApplication> viewApplications) {
         // application
         viewApplication = viewApplications.get(e.getApplicationName());
 
         // resources
         for (String rs : e.getResourceNames()) {
             String resourceClassName = viewApplication.getViewApplicationType().getResourceClassName();
             Resource r;
             try {
                 r = (Resource) Class.forName(resourceClassName).newInstance();
             } catch (ClassNotFoundException cnfe) {
                 throw new RuntimeException(cnfe);
             } catch (InstantiationException ie) {
                 throw new RuntimeException(ie);
             } catch (IllegalAccessException iae) {
                 throw new RuntimeException(iae);
             }
             r.setName(rs);
             resources.add(r);
         }
 
         // exceptions
         // TODO
 
         // actions
         // TODO
 
     }
 
     public List<Resource> getResources() {
         return resources;
     }
 
     public List<Resource> getExceptions() {
         return exceptions;
     }
 
     public void setResources(List<Resource> resources) {
         this.resources = resources;
     }
 
     public Resource[] getResourceArray() {
         return resources.toArray(new Resource[0]);
     }
 
     public void setResourceArray(Resource[] resourceArray) {
         resources = Arrays.asList(resourceArray);
     }
 
     public Entitlement getEntitlement() {
         Entitlement e = new Entitlement();
 
         e.setResourceNames(getResourceSet());
         e.setExcludedResourceNames(getExceptionSet());
         e.setActionValues(getActionMap());
         e.setApplicationName(viewApplication.getName());
 
         return e;
     }
 
     private Set<String> getResourceSet() {
         Set<String> resourceSet = new HashSet<String>();
 
         for (Resource r : resources) {
             resourceSet.add(r.getName());
         }
 
         return resourceSet;
     }
 
     private Set<String> getExceptionSet() {
         Set<String> exceptionSet = new HashSet<String>();
 
         for (Resource r : exceptions) {
             exceptionSet.add(r.getName());
         }
 
         return exceptionSet;
     }
 
     private Map<String, Boolean> getActionMap() {
         Map<String, Boolean> actionMap = new HashMap<String, Boolean>();
 
         for (Action a : actions) {
             actionMap.put(a.getName(), a.getValue());
         }
 
         return actionMap;
     }
 
     public List<Action> getActions() {
         return actions;
     }
 
     public void setActions(List<Action> actions) {
         this.actions = actions;
     }
 
     public ViewApplication getViewApplication() {
         return viewApplication;
     }
 
     public void setViewApplication(ViewApplication viewApplication) {
         this.viewApplication = viewApplication;
     }
 
    public String resourcesToString() {
         StringBuffer b = new StringBuffer();
 
         for (Iterator<Resource> i = resources.iterator(); i.hasNext();) {
             b.append(i.next());
             if (i.hasNext()) {
                 b.append(",");
             }
 
         }
 
         return b.toString();
     }
 
    public String resourcesToFormattedString() {
         StringBuffer b = new StringBuffer();
 
         for (Iterator<Resource> i = resources.iterator(); i.hasNext();) {
             b.append(i.next());
             if (i.hasNext()) {
                 b.append("\n");
             }
 
         }
 
         return b.toString();
     }
 }
