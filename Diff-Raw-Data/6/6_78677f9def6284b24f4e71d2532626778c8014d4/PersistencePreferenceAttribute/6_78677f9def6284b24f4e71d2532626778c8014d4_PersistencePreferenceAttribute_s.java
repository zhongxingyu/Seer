 package org.gridlab.gridsphere.portlet.jsrimpl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 public class PersistencePreferenceAttribute {
 
     private String oid = null;
     protected boolean readonly = false;
     protected String name = "";
     protected List values = new ArrayList();
 
     public String getOid() {
         return oid;
     }
 
     public void setOid(String oid) {
         this.oid = oid;
     }
 
     public void setReadOnly(boolean readonly) {
         this.readonly = readonly;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setValues(List values) {
         this.values = values;
     }
 
     public List getValues() {
         return values;
     }
 
     public boolean isReadOnly() {
         return readonly;
     }
 
     public void setAValues(String[] values) {
         this.values = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            this.values.add(values[i]);
         }
     }
 
     public String[] getAValues() {
         String[] array = new String[values.size()];
         return (String[]) this.values.toArray(array);
     }
 
     public void setValue(String value) {
         setAValues(new String[]{value});
     }
 
     public String getValue() {
         return (String) values.get(0);
     }
 
 
 }
