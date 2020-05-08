 /**
  * Copyright (C) 2013  Luc Hermans
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
 package org.lh.dmlj.schema.editor.common;
 
 import org.lh.dmlj.schema.DuplicatesOption;
 import org.lh.dmlj.schema.Key;
 import org.lh.dmlj.schema.KeyElement;
 import org.lh.dmlj.schema.MemberRole;
 import org.lh.dmlj.schema.OwnerRole;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.SetMembershipOption;
 import org.lh.dmlj.schema.SetMode;
 import org.lh.dmlj.schema.SetOrder;
 import org.lh.dmlj.schema.SortSequence;
 import org.lh.dmlj.schema.StorageMode;
 
 public abstract class Tools {
 	
 	public static String getCalcKey(Key calcKey) {
 		if (calcKey == null) {
 			return "";
 		}
 		StringBuilder p = new StringBuilder();		
 		for (KeyElement keyElement :calcKey.getElements()) {
 			if (p.length() > 0) {
 				p.append(", ");
 			}
 			p.append(keyElement.getElement().getName());
 		}
 		return p.toString();
 	}
 
 	public static String getDuplicatesOption(Key calcKey) {
 		if (calcKey == null) {
 			return "";
 		}
 		DuplicatesOption duplicatesOption = calcKey.getDuplicatesOption();
 		if (duplicatesOption == DuplicatesOption.NOT_ALLOWED) {
 			return "DN";
 		} else if (duplicatesOption == DuplicatesOption.FIRST) {
 			return "DF";
 		} else if (duplicatesOption == DuplicatesOption.LAST) {
 			return "DL";
 		} else {
 			return "DD";
 		}
 	}
 
 	public static String getMembershipOption(MemberRole memberRole) {
 		SetMembershipOption membershipOption = memberRole.getMembershipOption();
 		if (membershipOption == SetMembershipOption.MANDATORY_AUTOMATIC) {
 			return "MA";
 		} else if (membershipOption == SetMembershipOption.MANDATORY_MANUAL) {
 			return "MM";
 		} else if (membershipOption == SetMembershipOption.OPTIONAL_AUTOMATIC) {
 			return "OA";
 		} else {
 			return "OM";
 		}
 	}
 	
 	public static short getFirstAvailablePointerPosition(SchemaRecord record) {
 		
 		short highestPointerPosition = 0;
 		
 		for (OwnerRole ownerRole : record.getOwnerRoles()) {
 			if (ownerRole.getNextDbkeyPosition() > highestPointerPosition) {
 				highestPointerPosition = ownerRole.getNextDbkeyPosition();
 			}
 			if (ownerRole.getPriorDbkeyPosition() != null &&
 				ownerRole.getPriorDbkeyPosition()
 						 .shortValue() > highestPointerPosition) {
 				
 				highestPointerPosition = 
 					ownerRole.getPriorDbkeyPosition().shortValue();
 			}			
 		}
 		
 		for (MemberRole memberRole : record.getMemberRoles()) {
 			if (memberRole.getNextDbkeyPosition() != null &&
 				memberRole.getNextDbkeyPosition()
 						  .shortValue() > highestPointerPosition) {
 				
 				highestPointerPosition = 
 					memberRole.getNextDbkeyPosition().shortValue();
 			}
 			if (memberRole.getPriorDbkeyPosition() != null &&
 				memberRole.getPriorDbkeyPosition()
 						  .shortValue() > highestPointerPosition) {
 				
 				highestPointerPosition = 
 					memberRole.getPriorDbkeyPosition().shortValue();
 			}
 			if (memberRole.getOwnerDbkeyPosition() != null &&
 				memberRole.getOwnerDbkeyPosition()
 						  .shortValue() > highestPointerPosition) {
 				
 				highestPointerPosition = 
 					memberRole.getOwnerDbkeyPosition().shortValue();
 			}
 			if (memberRole.getIndexDbkeyPosition() != null &&
 				memberRole.getIndexDbkeyPosition()
 						  .shortValue() > highestPointerPosition) {
 				
 				highestPointerPosition = 
 					memberRole.getIndexDbkeyPosition().shortValue();
 			}
 		}
 		
 		return (short) (highestPointerPosition + 1);
 		
 	}
 	
 	public static String getPointers(MemberRole memberRole) {
 		StringBuilder p = new StringBuilder();
 		
 		if (memberRole.getSet().getMode() == SetMode.CHAINED) {
 			p.append("N");
 			if (memberRole.getPriorDbkeyPosition() != null) {
 				p.append("P");
 			}
 			if (memberRole.getOwnerDbkeyPosition() != null) {
 				p.append("O");
 			}
 		} else {
 			if (memberRole.getIndexDbkeyPosition() != null ||
 				memberRole.getOwnerDbkeyPosition() != null) {
 				
 				if (memberRole.getIndexDbkeyPosition() != null) {
 					p.append("I");
 				}
 				if (memberRole.getOwnerDbkeyPosition() != null) {
 					p.append("O");
 				}
 			} else {
 				p.append("-");
 			}
 		}
 		
 		return p.toString();
 	}
 
 	public static String getSortKeys(MemberRole memberRole) {
 		
 		if (memberRole.getSet().getOrder() != SetOrder.SORTED) {
 			return null;
 		}
 		
 		StringBuilder p = new StringBuilder();
 		for (KeyElement keyElement : memberRole.getSortKey().getElements()) {
 			SortSequence sortSequence = keyElement.getSortSequence();
 			boolean ascending = 
 				keyElement.getSortSequence() == SortSequence.ASCENDING;
 			if (p.length() == 0) {
 				// very first line
 				if (ascending) {
 					p.append("ASC (");
 				} else {
 					p.append("DESC (");
 				}				
 			} else if ((sortSequence == SortSequence.ASCENDING) != ascending) {				
 				// switch of sort sequence
 				p.append("),\n");
 				if (ascending) {
 					p.append("ASC (");
 				} else {
 					p.append("DESC (");
 				}				
 			} else {
 				// same sort sequence
 				p.append(",\n");
 				// by using a tab character, things will currently not line up 
 				// as we would like them to...
 				p.append("\t");
 			}			
 			p.append(keyElement.getElement().getName());
 		}
 		p.append(") ");		
 		p.append(getDuplicatesOption(memberRole.getSortKey()));
 		return p.toString();
 	}
 
 	public static String getStorageMode(StorageMode storageMode) {
 		if (storageMode == StorageMode.FIXED) {
 			return "F";
 		} else if (storageMode == StorageMode.FIXED_COMPRESSED) {
 			return "FC";
 		} else if (storageMode == StorageMode.VARIABLE) {
 			return "V";
 		} else {
 			return "VC";
 		}
 	}
 
 	public static String getSystemOwnerArea(MemberRole memberRole) {
		try {
 			return memberRole.getSet()
 							 .getSystemOwner()
 							 .getAreaSpecification()
 							 .getArea()
 							 .getName();
		} catch (Throwable t) {
			// if, for some reason, detecting the system owner's area fails (e.g. somewhere during
			// an index removal process or when undoing the creation of a new index), return null
 			return null;
 		}
 	}
 	
 	/**
 	 * Removes the trailing underscore from the given name (DDLCATLOD related
 	 * records and sets).  The given name does not necessarily have a trailing
 	 * underscore.
 	 * @param name
 	 * @return
 	 */
 	public static String removeTrailingUnderscore(String name) {
 		// remove the trailing underscore from the given name (DDLCATLOD related
 		// records and sets)
 		StringBuilder p = new StringBuilder(name);
 		if (p.charAt(p.length() - 1) == '_') {
 			p.setLength(p.length() - 1);
 		}
 		return p.toString();
 	}
 	
 }
