 package org.intellij.alt.runconfiguration;
 
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.dynjs.runtime.DynObject;
 
 public class DynObjectConfiguration implements Configuration {
     private DynObject object;
 
     public DynObjectConfiguration(DynObject object) {
         this.object = object;
     }
 
     public String main() {
         return (String) object.get("main");
     }
 
     public String arguments() {
         return (String) object.get("arguments");
     }
 
     public String name() {
         return (String) object.get("name");
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         DynObjectConfiguration that = (DynObjectConfiguration) o;
 
         if (object != null ? !object.equals(that.object) : that.object != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return object != null ? object.hashCode() : 0;
     }
 }
