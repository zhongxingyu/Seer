 /*
  * ActiveCrudService.java
  *
  * Created on August 30, 2013, 4:42 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.services.extended;
 
 import com.rameses.annotations.ProxyMethod;
 import groovy.lang.GroovyObject;
 import java.util.Map;
 
 /**
  *
  * @author Elmo
  */
 public abstract class ActiveCrudService {
     
     protected abstract Object getEm();
     
     public String getSubSchemaName() {
         return null;
     }
     
     public boolean isValidate() {
         return true;
     }
     
     protected GroovyObject getObj() {
         return (GroovyObject)getEm();
     }
     
     public void beforeCreate(Object data) {;}
     public void afterCreate(Object data) {;}
     public void beforeUpdate(Object data) {;}
     public void afterUpdate(Object data) {;}
     public void beforeOpen(Object data) {;}
     public void afterOpen(Object data) {;}
     public void beforeRemoveEntity(Object data) {;}
     public void afterRemoveEntity(Object data) {;}
     
     public void beforeApprove(Object data) {;}
     public void afterApprove(Object data) {;}
     
     @ProxyMethod
     public Object create(Object data) {
         beforeCreate(data);
         if(getSubSchemaName()!=null) {
             data = getObj().invokeMethod( "create", new Object[] {data, getSubSchemaName()} );
         }
         else {
             data = getObj().invokeMethod( "create", new Object[] {data} );
         }
         afterCreate(data);
         return data;
     }
     
     @ProxyMethod
     public Object update(Object data) {
         beforeUpdate(data);
         if(getSubSchemaName()!=null) {
             data = getObj().invokeMethod( "update", new Object[]{data, getSubSchemaName()} );    
         }
         else {
             data = getObj().invokeMethod( "update", new Object[]{data} );    
         }
         afterUpdate(data);
         return data;
     }
     
     @ProxyMethod
     public Object open(Object data) {
         beforeOpen(data);
         if(getSubSchemaName()!=null) {
            data = getObj().invokeMethod("open",new Object[]{data, getSubSchemaName()});    
         }
         else {
            data = getObj().invokeMethod("open",new Object[]{data});    
         }
         afterOpen(data);
         if(data==null || ((Map)data).isEmpty() ) {
             throw new RuntimeException( "record does not exist" );
         }
         return data;
     }
     
     @ProxyMethod
     public void removeEntity(Object data) {
         beforeRemoveEntity(data);
         if(getSubSchemaName()!=null) {
             getObj().invokeMethod("delete",new Object[]{data, getSubSchemaName()});
         }
         else {
            getObj().invokeMethod("delete",new Object[]{data}); 
         }
         afterRemoveEntity(data);
     }
     
     @ProxyMethod
     public void approve(Object data) {
         beforeApprove(data);
         getObj().invokeMethod("approve", new Object[]{data});
         afterApprove(data);
     }
     
     
 }
