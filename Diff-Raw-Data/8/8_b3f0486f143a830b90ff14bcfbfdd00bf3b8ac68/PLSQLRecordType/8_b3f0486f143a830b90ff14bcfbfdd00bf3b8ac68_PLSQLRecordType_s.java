 /*******************************************************************************
  * Copyright (c) 2011 Oracle. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
  * which accompanies this distribution.
  * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
  * and the Eclipse Distribution License is available at
  * http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors:
  *     Mike Norman - June 10 2011, created DDL parser package
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.metadata;
 
 //javase imports
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 //DDL parser imports
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.DatabaseTypeVisitable;
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.DatabaseTypeVisitor;
 
 public class PLSQLRecordType extends PLSQLType implements DatabaseTypeVisitable {
 
     protected List<FieldType> fields = new ArrayList<FieldType>();
 
 	public PLSQLRecordType(String recordName) {
 		super(recordName);
 	}
 
 	@Override
 	public void addCompositeType(DatabaseType enclosedType) {
 	    if (enclosedType instanceof FieldType) {
 	        fields.add((FieldType)enclosedType);
 	    }
 	}
 
     /**
      * Returns the list of FieldType instances.
      */
     public List<FieldType> getFields() {
         return fields;
     }
 
 	public void accept(DatabaseTypeVisitor visitor) {
 		visitor.visit(this);
 	}
 
     @Override
     public boolean isResolved() {
         // if any of the field types are unresolved, then this record is unresolved
         for (FieldType fType : fields) {
             if (!fType.isResolved()) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(super.toString());
         sb.append(" (");
         for (Iterator<FieldType> iterator = fields.iterator(); iterator.hasNext(); ) {
             FieldType f = iterator.next();
             sb.append("\n\t");
             sb.append(f.toString());
             if (iterator.hasNext()) {
                 sb.append(", ");
             }
         }
         sb.append("\n)");
         return sb.toString();
     }
 }
