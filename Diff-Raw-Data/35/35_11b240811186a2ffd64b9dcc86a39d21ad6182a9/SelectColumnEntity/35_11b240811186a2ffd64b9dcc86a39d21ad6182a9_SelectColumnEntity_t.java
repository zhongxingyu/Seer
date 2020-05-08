 package org.motechproject.carereporting.domain;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "select_column")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "select_column_id"))
 })
 public class SelectColumnEntity extends AbstractEntity {
 
     @Column(name = "name")
     private String name;
 
     @Column(name = "function")
     private String functionName;
 
    @Column(name = "table_name")
     private String tableName;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getFunctionName() {
         return functionName;
     }
 
     public void setFunctionName(String functionName) {
         this.functionName = functionName;
     }
 
     public String getTableName() {
         return tableName;
     }
 
     public void setTableName(String tableName) {
         this.tableName = tableName;
     }
 }
