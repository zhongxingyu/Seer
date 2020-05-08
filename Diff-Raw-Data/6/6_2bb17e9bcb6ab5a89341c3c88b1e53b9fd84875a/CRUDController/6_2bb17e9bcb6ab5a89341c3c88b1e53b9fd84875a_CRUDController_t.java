 package com.rameses.osiris2.common;
 import com.rameses.osiris2.client.InvokerProxy;
 import com.rameses.rcp.annotations.Binding;
 import com.rameses.rcp.annotations.ChangeLog;
 import com.rameses.rcp.annotations.Invoker;
 import com.rameses.rcp.common.Action;
 import com.rameses.rcp.common.MsgBox;
 import com.rameses.rcp.common.StyleRule;
 import com.rameses.rcp.framework.ChangeLogHandler;
 import com.rameses.util.MapEditor;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class CRUDController 
 {
     public final static String MODE_CREATE = "create";
     public final static String MODE_EDIT = "edit";
     public final static String MODE_READ = "read";
                 
     @Invoker
     protected com.rameses.osiris2.Invoker invoker;
     
     @Binding
     protected com.rameses.rcp.framework.Binding binding;
 
     @ChangeLog
     private com.rameses.rcp.framework.ChangeLog changeLog; 
     
 
     private ListModelHandler listModelHandler;
     private List formActions;    
     private List navActions;    
     
     private Map entity = new HashMap();    
     private String mode = MODE_READ;  
     private CRUDService service; 
     private MapEditor mapEditor;
     
     protected String styleSource;
     
     
     public abstract String getServiceName(); 
     public abstract String getEntityName();
     
             
     // <editor-fold defaultstate="collapsed" desc=" Getter/Setter "> 
         
     public String getTitle() { 
         String text = (invoker == null? "Title": invoker.getCaption()); 
         if (MODE_CREATE.equals(getMode())) 
             return text + " (New)";
         else if (MODE_EDIT.equals(getMode())) 
             return text + " (Edit)";
         else
             return text; 
     }
     
     public final String getMode() { 
         return mode; 
     }
             
     public Map getEntity() { 
         return entity; 
     }
     
     public void setEntity(Map entity) 
     {
         if (entity != null) 
         {
             if (entity.get("state") == null) entity.put("state", "DRAFT"); 
         }
         
         this.entity = entity;
     }
     
     public CRUDService getService() 
     {
         String name = getServiceName();
         if (name == null || name.trim().length() == 0)
             throw new RuntimeException("No service name specified"); 
             
         if (service == null) {
             service = (CRUDService) InvokerProxy.getInstance().create(name, CRUDService.class);
         }
         return service;
     }    
     
     public final boolean isDirty() { 
         return changeLog.hasChanges(); 
     }
     
     public ListModelHandler getListModelHandler() { return this.listModelHandler; } 
     public void setListModelHandler(ListModelHandler listModelHandler) {
         this.listModelHandler = listModelHandler;
     }
     
     protected Map getChanges() 
     {
         final Map map = new HashMap();
         if (changeLog.hasChanges()) 
         {
             ChangeLogHandler handler = new ChangeLogHandler() 
             {
                 private String fldname;
 
                 public void clear(com.rameses.rcp.framework.ChangeLog log) {}
                 public void end() {}
                 public void endEntity() {}
                 public void endField() {}
                 public void showChange(Object oldValue, Object newValue) {
                     Map diff = new HashMap();
                     diff.put("oldvalue", oldValue);
                     diff.put("newvalue", newValue);
                     map.put(fldname, diff); 
                 }
                 public void showHistory(Date timeChanged, Object oldValue, Object newValue) {}
                 public void start() {}
                 public void startEntity(Object entity) {}
                 public void startField(String fieldName) {
                     this.fldname = fieldName; 
                 }
             };
             changeLog.scan(handler); 
             //System.out.println("Difference -> " + changeLog.getDifference());
         } 
         return map; 
     }     
     
     // </editor-fold>    
         
     // <editor-fold defaultstate="collapsed" desc=" Options "> 
           
     public boolean isAllowCreate() { return true; } 
     public boolean isAllowOpen() { return true; } 
     public boolean isAllowEdit() { return true; } 
     public boolean isShowFormActions() { return true; } 
     
     protected boolean isShowConfirmOnSave() { return false; }  
     protected boolean isShowNavigationBar() { return true; }
     
     public String getCreateFocusComponent() {  return "entity.code"; }    
     public String getEditFocusComponent() { return "entity.description"; }        
     
     
     /*
      *  use as a prefix value in generating the UID key 
      */
     protected String getPrefixId() { return "MF"; }
     
     public List getStyleRules() {
         List list = new ArrayList();
         list.add(new StyleRule("entity.*", "#{mode=='read'}").add("enabled", false)); 
         list.add(new StyleRule("entity.*", "#{mode!='read'}").add("enabled", true));         
         return list; 
     }    
     
     // </editor-fold>
         
     // <editor-fold defaultstate="collapsed" desc=" Form / Navigation Actions "> 
     
     public List getFormActions() {
         if (formActions == null) 
         {
             if (!isShowFormActions()) return null;
             
             formActions = new ArrayList();
             formActions.add(createAction("close", "Close", "images/toolbars/cancel.png", "ctrl C", 'c', "#{mode=='read'}", true));  
             if (isAllowCreate())
                 formActions.add(createAction("init", "New", "images/toolbars/create.png", "ctrl N", 'n', "#{mode=='read'}", true));        
             
             if (isAllowEdit())
             {
                formActions.add(createAction("edit", "Edit", "images/toolbars/edit.png", "ctrl E", 'e',  "#{mode=='read' && entity.state=='DRAFT'}", true)); 
                formActions.add(createAction("delete", "Delete", "images/toolbars/trash.png", null, 'd', "#{mode=='read' && entity.state=='DRAFT'}", true)); 
                formActions.add(createAction("approve", "Approve", "images/toolbars/approve.png", null, 'v', "#{mode=='read' && entity.state=='DRAFT'}", true)); 
             }
             
             if (isAllowCreate() || isAllowEdit())
             {
                 formActions.add(createAction("cancel", "Cancel", "images/toolbars/cancel.png", "ctrl C", 'c', "#{mode!='read'}", true)); 
                 formActions.add(createAction("save", "Save", "images/toolbars/save.png", "ctrl S", 's', "#{mode!='read'}", false)); 
                 formActions.add(createAction("undo", "Undo", "images/toolbars/undo.png", "ctrl Z", 'u', "#{mode=='edit'}", true)); 
             } 
         } 
         return formActions;         
     }
     
     public List getNavActions() 
     {
         if (navActions == null) 
         {
             navActions = new ArrayList();
             if (isShowNavigationBar())
             {
                 navActions.add(createAction("moveBackRecord", "Move to previous record", "images/toolbars/arrow_up.png", null, '\u0000', "#{mode=='read'}", true));  
                 navActions.add(createAction("moveNextRecord", "Move to next record", "images/toolbars/arrow_down.png", null, '\u0000', "#{mode=='read'}", true));  
             }
         }
         return navActions;
     }
     
     private Action createAction(String name, String caption, String icon, String shortcut, char mnemonic, String visibleWhen, boolean immediate) 
     {
         Action a = new Action(name, caption, icon, mnemonic);
         if( shortcut != null ) a.getProperties().put("shortcut", shortcut);    
         if (visibleWhen != null) a.setVisibleWhen(visibleWhen); 
         
         a.setImmediate( immediate );
         return a;
     }      
     
     // </editor-fold> 
 
         
     protected Map createEntity() {
         return new LinkedHashMap();
     }
     
     public final void init()
     {
         if (!isAllowCreate())
             throw new IllegalStateException("PERMISSION DENIED! Invoking this method is not authorized."); 
                         
         Map data = createEntity();  
         if (data.get("objid") == null) 
             data.put("objid", getPrefixId() + new java.rmi.server.UID());
         if (data.get("state") == null) 
             data.put("state", "DRAFT"); 
         
         setEntity(data);
         mode = MODE_CREATE; 
         focusComponent(getCreateFocusComponent()); 
     }
     
     public Object cancel() 
     {
         if (MODE_EDIT.equals(this.mode)) 
         {
             if (changeLog.hasChanges()) 
             {
                 if (!MsgBox.confirm("Changes will be discarded. Continue?")) 
                     return null; 
 
                 changeLog.undoAll(); 
                 changeLog.clear();
             }
             
             this.mode = MODE_READ; 
             return null; 
         }
         else {
             return close(); 
         }
     }
     
     public Object close() 
     {
         this.mode = MODE_READ; 
         this.changeLog.clear(); 
         return "_close"; 
     }
     
     protected void onbeforeSave(){}
     protected void onafterSave(){}
     
     public void save()
     {
         try 
         {
             if (isShowConfirmOnSave()) {
                 if (!MsgBox.confirm("You are about to save this record. Continue?")) return;
             }
             
             onbeforeSave();
 
             if (MODE_CREATE.equals(this.mode))
                 setEntity(getService().create(getEntity())); 
             
             else if (MODE_EDIT.equals(this.mode)) 
                 setEntity(getService().update(getEntity())); 
 
             String oldmode = this.mode;
             this.mode = MODE_READ; 
             this.changeLog.clear();
             
             onafterSave(); 
             
             if (listModelHandler != null) 
             {
                 if (MODE_EDIT.equals(oldmode)) 
                     listModelHandler.handleUpdate(getEntity()); 
                 else if (MODE_CREATE.equals(oldmode)) 
                     listModelHandler.handleInsert(getEntity());                 
             }
         }
         catch(Exception ex) {
             MsgBox.err(ex); 
         } 
     }
     
     public void open() 
     {
         if (!isAllowOpen())
             throw new IllegalStateException("PERMISSION DENIED! Invoking this method is not authorized."); 
         
         Map data = getService().open(getEntity());
         if (data == null) throw new NullPointerException("Record does not exist");
         
         if (data.get("state") == null) data.put("state", "DRAFT"); 
         
         setEntity(data); 
         mode = MODE_READ;                 
     }
     
     public void edit() 
     {
         if (!isAllowEdit())
             throw new IllegalStateException("PERMISSION DENIED! Invoking this method is not authorized."); 
         
         this.mode = MODE_EDIT; 
         focusComponent(getEditFocusComponent()); 
     }
     
     public void undo() {
         changeLog.undo(); 
     }    
     
     public void approve()
     {
         if (MsgBox.confirm("You are about to approve this document. Continue?"))
         {
             Map data = getService().approve(getEntity()); 
             setEntity(data);
 
             if (data.get("state") == null) data.put("state", "DRAFT"); 
         } 
     }
     
     public Object delete()
     {
         if (MsgBox.confirm("You are about to delete this document. Continue?"))
         {
             Map data = getEntity();
             getService().delete(data); 
             if (listModelHandler != null)
                 listModelHandler.handleRemove(data); 
                 
             return close();
         }
         else {
             return null; 
         }
     }    
     
     public void moveBackRecord() 
     {
         if (listModelHandler != null)
         {
             listModelHandler.moveBackRecord();
             Object data = listModelHandler.getSelectedEntity(); 
             if (data == null) return;
             
             setEntity((Map) data); 
             open(); 
             
             if (binding != null) binding.refresh(); 
         }
     }
     
     public void moveNextRecord() 
     {
         if (listModelHandler != null)
         {
             listModelHandler.moveNextRecord();
             Object data = listModelHandler.getSelectedEntity(); 
             if (data == null) return;
             
             setEntity((Map) data); 
             open();          
             
             if (binding != null) binding.refresh(); 
         }
     }    
       
     private void focusComponent(String ctrlname)
     {
         if ( binding != null ) binding.focus(ctrlname); 
     }    
     
 }
