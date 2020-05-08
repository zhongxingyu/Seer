 /*
  * Copyright (c) 2010 Mysema Ltd.
  * All rights reserved.
  * 
  */
 package com.mysema.webmin.conf;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 import javax.servlet.ServletContext;
 
 import com.mysema.commons.lang.Assert;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 
 /**
  * Bundle provides
  * 
  * @author tiwe
  * @version $Id$
  */
 public class Bundle {
     
     @Nullable
     @XStreamAsAttribute
     @XStreamAlias("extends")
     private String _extends;
 
     private long maxage;
 
     @XStreamAsAttribute
     private String name;
 
     private transient String localName;
 
     @Nullable
     @XStreamAsAttribute
     private String path;
 
     @Nullable
     private List<Resource> resources = new ArrayList<Resource>();
     
     private transient Map<String,Resource> resourceByPath;
 
     @Nullable
     @XStreamAsAttribute
     private String type; // default is "javascript"
     
     public String getLocalName() {
         return localName;
     }
 
     public long getMaxage() {
         return maxage;
     }
 
     public String getName() {
         return name;
     }
 
     public String getPath() {
         return path;
     }
 
     @Nullable
     public Resource getResourceForPath(String path) {
         return resourceByPath.get(path);
     }
 
     public List<Resource> getResources() {
         return resources;
     }
 
     public String getType() {
         return type;
     }
 
     @SuppressWarnings("unchecked")
     void initialize(Configuration c, ServletContext context) {
         if (resources == null){
             resources = new ArrayList<Resource>();
         }        
         if (type == null){
             type = "javascript";
         }        
         if (path != null){
             localName = path.substring(path.lastIndexOf('/') + 1);
         }
         // handle wildcards
         List<Resource> wildcards = new ArrayList<Resource>(resources.size());
         List<Resource> additions = new ArrayList<Resource>();
         for (Resource resource : resources){
             // add base path
             if (c.getBasePath() != null && !resource.getPath().startsWith("/")) {
                 resource.addPathPrefix(c.getBasePath());
             }
             if (resource.getPath().contains("*")){
                 int index = resource.getPath().indexOf('*');                
                 String prefix = resource.getPath().substring(0, index);
                 String suffix = null;
                 if (index != resource.getPath().length() -1){
                     suffix = resource.getPath().substring(index +1);
                 }
                 Set<String> paths = context.getResourcePaths(prefix);
                 if (paths != null){
                     for (String p : paths){
                         if (p.contains(".svn")){
                             continue;                        
                         }else if (suffix == null || p.endsWith(suffix)){
                             additions.add(new Resource(p,false,false));    
                         }                        
                     }    
                 }                
                 wildcards.add(resource);
             }
         }
         resources.removeAll(wildcards);
         resources.addAll(additions);
     
         if (_extends != null) {            
             // process extends
             Set<Resource> res = new LinkedHashSet<Resource>();
            for (String name : _extends.split(",")) {
                Bundle parent = c.getBundleByName(name);
                 parent.initialize(c, context);
                 res.addAll(parent.getResources());
             }
             resources.addAll(0, res);
             _extends = null;
         }
         
         resourceByPath = new HashMap<String,Resource>();
         for (Resource resource : resources){            
             resourceByPath.put(resource.getPath(), resource);
         }
         
     }
 
     public void setExtends(String e) {
         this._extends = e;
     }
 
     public void setMaxage(long maxage) {
         this.maxage = maxage;
     }
 
     public void setName(String name) {
         this.name = Assert.notNull(name,"name");
     }
 
     public void setPath(String path) {
         this.path = Assert.notNull(path,"path");        
     }
 
     public void setType(String type) {
         this.type = Assert.notNull(type,"type");
     }
     
     @Override
     public String toString(){
         return resources.toString();
     }
 }
