 package org.eclipse.persistence.tools.oracleddl.metadata;
 
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.DatabaseTypeVisitor;
 
 public class ObjectTableType extends CompositeDatabaseTypeBase implements CompositeDatabaseType {
 
     protected String schema;
     protected DatabaseType enclosedType;
 
     public ObjectTableType(String typeName) {
         super(typeName);
     }
 
     public String getSchema() {
         return schema;
     }
     public void setSchema(String schema) {
        this.schema = schema;
     }
 
     public DatabaseType getEnclosedType() {
         return enclosedType;
     }
     public void addCompositeType(DatabaseType enclosedType) {
         this.enclosedType = enclosedType;
     }
 
     public void accept(DatabaseTypeVisitor visitor) {
         visitor.visit(this);
     }
 }
