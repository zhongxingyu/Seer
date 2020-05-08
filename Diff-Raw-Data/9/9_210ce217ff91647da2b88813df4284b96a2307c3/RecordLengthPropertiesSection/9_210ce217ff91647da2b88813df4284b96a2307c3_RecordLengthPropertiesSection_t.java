 /**
 * Copyright (C) 2015  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.property.section;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.lh.dmlj.schema.SchemaPackage;
 import org.lh.dmlj.schema.editor.property.handler.ErrorEditHandler;
 import org.lh.dmlj.schema.editor.property.handler.IEditHandler;
 
 public class RecordLengthPropertiesSection 
 	extends AbstractRecordPropertiesSection {
 
 	public RecordLengthPropertiesSection() {
 		super();
 	}	
 	
 	@Override
 	public List<EAttribute> getAttributes() {		
 		List<EAttribute> attributes = new ArrayList<>();		
 		attributes.add(SchemaPackage.eINSTANCE.getSchemaRecord_PrefixLength());
 		attributes.add(SchemaPackage.eINSTANCE.getSchemaRecord_DataLength());
 		attributes.add(SchemaPackage.eINSTANCE.getSchemaRecord_ControlLength());
 		attributes.add(SchemaPackage.eINSTANCE
 								    .getSchemaRecord_MinimumRootLength());		
 		attributes.add(SchemaPackage.eINSTANCE
 								    .getSchemaRecord_MinimumFragmentLength());				
 		return attributes;
 	}
 	
 	@Override
 	public EObject getEditableObject(EAttribute attribute) {
 		if (attribute == SchemaPackage.eINSTANCE
 			    					  .getSchemaRecord_MinimumRootLength() ||
 			attribute == SchemaPackage.eINSTANCE
 			  						  .getSchemaRecord_MinimumFragmentLength()) {
 			
 			return target;
 		} else {
 			return super.getEditableObject(attribute);
 		}
 	}
 	
 	@Override
 	public IEditHandler getEditHandler(EAttribute attribute, Object newValue) {
 		
 		if (attribute == SchemaPackage.eINSTANCE
 				  					  .getSchemaRecord_MinimumRootLength()) {
 		
 			// get the new minimum root length
 			Short newMinimumRootLength = (Short) newValue;
 			
 			// newMinimumRootLength must include all CALC, index, and sort 
 			// control elements. It must be an unsigned integer; if it is not a 
 			// multiple of 4, we will make it so by rounding up - we treat
 			// null and 0 the same here
 			if (newMinimumRootLength != null && 
 				newMinimumRootLength.shortValue() > 0) {				
 				
 				// round up the new value if not a multiple of 4
 				short i = newMinimumRootLength; 
 				while (i % 4 > 0) {
 					i++;
 				}
 				newMinimumRootLength = Short.valueOf(i);
 				// compute the minimum value; the control length counts an extra
 				// 4 bytes if the record is fragmented, so we have to take that
 				// into account
 				short minimumValue = 
 					!target.isFragmented() ? target.getControlLength() :
 					(short) (target.getControlLength() - 4);				
 				// perform the minimum value check
 				if (newMinimumRootLength < minimumValue) {
 					String message = 
 						"must include all CALC, index, and sort control elements";
 					return new ErrorEditHandler(message);
 				}
 				// don't perform the maximum value check --> the CA IDMS schema compiler does NOT
 				// perform this check				
 				/*if (newMinimumRootLength > target.getDataLength()) {
 					String message = "exceeds 'Data length'";
 					return new ErrorEditHandler(message);
 				}*/ 
 				// the value entered is valid
 				return super.getEditHandler(attribute, newMinimumRootLength);
 			} else if (newMinimumRootLength != null && 
 					   newMinimumRootLength.shortValue() < 0) {
 					
 				String message = "must be an unsigned integer";
 				return new ErrorEditHandler(message);				
 			} else {
 				return super.getEditHandler(attribute, null);
 			}			
 		} else if (attribute == SchemaPackage.eINSTANCE
 				  							 .getSchemaRecord_MinimumFragmentLength()) {
 		
 			// get the new minimum fragment length
 			Short newMinimumFragmentLength = (Short) newValue;
 			
 			// newMinimumFragmentLength must be an unsigned integer; if it is 
 			// not a multiple of 4, we will make it so by rounding up - we treat
 			// null and 0 the same here
 			if (newMinimumFragmentLength != null && 
 				newMinimumFragmentLength.shortValue() > 0) {
 				
 				// round up the new value if not a multiple of 4
 				short i = newMinimumFragmentLength; 
 				while (i % 4 > 0) {
 					i++;
 				}
 				newMinimumFragmentLength = Short.valueOf(i);
 				// the value entered is valid
 				return super.getEditHandler(attribute, newMinimumFragmentLength);
 			} else if (newMinimumFragmentLength != null && 
 				newMinimumFragmentLength.shortValue() < 0) {
 				
 				String message = "must be an unsigned integer";
 				return new ErrorEditHandler(message);
 			} else {
 				return super.getEditHandler(attribute, null);
 			}
 		}
 		return super.getEditHandler(attribute, newValue);
 	}
 
 }
