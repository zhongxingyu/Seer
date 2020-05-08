 package org.lh.dmlj.schema.editor.template;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import org.lh.dmlj.schema.*;
 
 public class SetTemplate
 {
   protected static String nl;
   public static synchronized SetTemplate create(String lineSeparator)
   {
     nl = lineSeparator;
     SetTemplate result = new SetTemplate();
     nl = null;
     return result;
   }
 
   public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "     ADD" + NL + "     SET NAME IS ";
   protected final String TEXT_2 = NL + "         ORDER IS ";
   protected final String TEXT_3 = NL + "         MODE IS ";
   protected final String TEXT_4 = NL + "         OWNER IS ";
   protected final String TEXT_5 = NL + "*+           WITHIN AREA ";
   protected final String TEXT_6 = NL + "             NEXT DBKEY POSITION IS ";
   protected final String TEXT_7 = "             " + NL + "             PRIOR DBKEY POSITION IS ";
   protected final String TEXT_8 = NL + "         OWNER IS SYSTEM";
   protected final String TEXT_9 = NL + "             WITHIN AREA ";
   protected final String TEXT_10 = " SUBAREA ";
   protected final String TEXT_11 = NL + "             WITHIN AREA ";
   protected final String TEXT_12 = " OFFSET ";
   protected final String TEXT_13 = " FOR ";
   protected final String TEXT_14 = " ";
   protected final String TEXT_15 = NL + "             WITHIN AREA ";
   protected final String TEXT_16 = NL + "         MEMBER IS ";
   protected final String TEXT_17 = NL + "*+           WITHIN AREA ";
   protected final String TEXT_18 = NL + "             NEXT DBKEY POSITION IS ";
   protected final String TEXT_19 = "             " + NL + "             PRIOR DBKEY POSITION IS ";
   protected final String TEXT_20 = NL + "             INDEX DBKEY POSITION IS ";
   protected final String TEXT_21 = NL + "             INDEX DBKEY POSITION IS OMITTED";
   protected final String TEXT_22 = NL + "             LINKED TO OWNER" + NL + "             OWNER DBKEY POSITION IS ";
   protected final String TEXT_23 = NL + "             ";
   protected final String TEXT_24 = NL + "             KEY IS (";
   protected final String TEXT_25 = NL + "                 ";
   protected final String TEXT_26 = " ";
   protected final String TEXT_27 = " ";
   protected final String TEXT_28 = NL + "                 DUPLICATES ARE ";
   protected final String TEXT_29 = NL + "                 NATURAL SEQUENCE";
   protected final String TEXT_30 = NL + "                 COMPRESSED";
   protected final String TEXT_31 = NL + "                 UNCOMPRESSED";
   protected final String TEXT_32 = NL + "         .";
 
   public String generate(Object argument)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
 /**
  * Copyright (C) 2014  Luc Hermans
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
 
     
 
 /*
 This template will generate a set's DDL syntax.
   
 See SetTemplateTest for a JUnit testcase.
 */
 
 Object[] args = ((List<?>) argument).toArray();
 Set set = (Set) args[0];
 boolean sortSchemaEntities = ((Boolean) args[1]).booleanValue();
 
 String setName;
 if (set.getName().endsWith("_")) {
 	setName = set.getName().substring(0, set.getName().length() - 1);
 } else {
 	setName = set.getName();
 }
 String mode;
 if (set.getMode() == SetMode.CHAINED) {
     if (set.getOwner().getPriorDbkeyPosition() != null) {
         mode = "CHAIN LINKED TO PRIOR";
     } else {
         mode = "CHAIN";
     }
 } else {
     String id;
     if (set.getSchema().getName().equals("IDMSNTWK") &&
         set.getSchema().getVersion() == 1) {
         
         if (setName.equals("IX-AREA")) {
             id = "ID IS 1 ";
         } else if (setName.equals("IX-BUFFER")) {
             id = "ID IS 2 ";
         } else if (setName.equals("IX-DBNAME")) {
             id = "ID IS 4 ";
         } else if (setName.equals("IX-DMCL")) {
             id = "ID IS 5 ";
         } else if (setName.equals("IX-DMCLSEGMENT")) {
             id = "ID IS 6 ";
         } else if (setName.equals("IX-FILE")) {
             id = "ID IS 7 ";
         } else if (setName.equals("IX-SEGMENT")) {
             id = "ID IS 10 ";
         } else {
             id = "";
         }
     } else {
         id = "";
     }
     IndexedSetModeSpecification indexedSetModeSpecification =
         set.getIndexedSetModeSpecification();
     if (indexedSetModeSpecification.getSymbolicIndexName() != null) {
         mode = "INDEX " + id + "USING " + 
                indexedSetModeSpecification.getSymbolicIndexName();
     } else if (indexedSetModeSpecification.getDisplacementPageCount() != null &&
                indexedSetModeSpecification.getDisplacementPageCount() != 0) {
         mode = "INDEX " + id + "BLOCK CONTAINS " + 
                indexedSetModeSpecification.getKeyCount() + 
                " KEYS DISPLACEMENT IS " + 
                indexedSetModeSpecification.getDisplacementPageCount();
     } else {
          mode = "INDEX " + id + "BLOCK CONTAINS " + 
                indexedSetModeSpecification.getKeyCount() + " KEYS";
     }
 }
 
     stringBuffer.append(TEXT_1);
     stringBuffer.append( setName );
     stringBuffer.append(TEXT_2);
     stringBuffer.append( set.getOrder().toString() );
     stringBuffer.append(TEXT_3);
     stringBuffer.append( mode );
     
 if (set.getOwner() != null) {
     String recordName;
     if (set.getOwner().getRecord().getName().endsWith("_")) {
         StringBuilder p = 
        	    new StringBuilder(set.getOwner().getRecord().getName()); 
         p.setLength(p.length() - 1);
         recordName = p.toString();
     } else {
         recordName = set.getOwner().getRecord().getName();
     }
 
     stringBuffer.append(TEXT_4);
     stringBuffer.append( recordName );
     stringBuffer.append(TEXT_5);
     stringBuffer.append( set.getOwner().getRecord().getAreaSpecification().getArea().getName() );
     stringBuffer.append(TEXT_6);
     stringBuffer.append( set.getOwner().getNextDbkeyPosition() );
      
     if (set.getOwner().getPriorDbkeyPosition() != null) {
 
     stringBuffer.append(TEXT_7);
     stringBuffer.append( set.getOwner().getPriorDbkeyPosition() );
     
     }
 } else {
     AreaSpecification areaSpecification = 
         set.getSystemOwner().getAreaSpecification();
     String areaName = areaSpecification.getArea().getName();
     String symbolicSubareaName = areaSpecification.getSymbolicSubareaName();
     OffsetExpression offsetExpression = areaSpecification.getOffsetExpression();    
 
     stringBuffer.append(TEXT_8);
     
     if (symbolicSubareaName != null) {
 
     stringBuffer.append(TEXT_9);
     stringBuffer.append( areaName );
     stringBuffer.append(TEXT_10);
     stringBuffer.append( symbolicSubareaName );
     
 	} else if (offsetExpression != null) { 
 	    String p;
 	    if (offsetExpression.getOffsetPageCount() != null) {
 	        p = offsetExpression.getOffsetPageCount() + " PAGES";
 	    } else if (offsetExpression.getOffsetPercent() != null) {
 	    	p = offsetExpression.getOffsetPercent() + " PERCENT";
 	    } else {
 	        p = "0";
 	    }
 	    String q;
 	    if (offsetExpression.getPercent() != null) {
 	        q = offsetExpression.getPercent() + " PERCENT";
 	    } else if (offsetExpression.getPageCount() != null) {
 	        q = offsetExpression.getPageCount() + " PAGES";
 	    } else {
 	        q = "100 PERCENT";
 	    }
 
     stringBuffer.append(TEXT_11);
     stringBuffer.append( areaName );
     stringBuffer.append(TEXT_12);
     stringBuffer.append( p );
     stringBuffer.append(TEXT_13);
     stringBuffer.append( q );
     stringBuffer.append(TEXT_14);
     
     } else {
 
     stringBuffer.append(TEXT_15);
     stringBuffer.append( areaName );
     
     }
 }
 List<MemberRole> memberRoles = new ArrayList<>(set.getMembers());
 if (sortSchemaEntities) {
 	Collections.sort(memberRoles, new Comparator<MemberRole>() {
 		public int compare(MemberRole m1, MemberRole m2) {
 			return m1.getRecord().getName().compareTo(m2.getRecord().getName());
 		}
 	});
 }
 for (MemberRole memberRole : memberRoles) {
     String recordName;
     if (memberRole.getRecord().getName().endsWith("_")) {
         StringBuilder p = new StringBuilder(memberRole.getRecord().getName()); 
         p.setLength(p.length() - 1);
         recordName = p.toString();
     } else {
         recordName = memberRole.getRecord().getName();
     }
 
     stringBuffer.append(TEXT_16);
     stringBuffer.append( recordName );
     stringBuffer.append(TEXT_17);
     stringBuffer.append( memberRole.getRecord().getAreaSpecification().getArea().getName() );
     
     if (memberRole.getNextDbkeyPosition() != null) {
 
     stringBuffer.append(TEXT_18);
     stringBuffer.append( memberRole.getNextDbkeyPosition() );
     
     }
     if (memberRole.getPriorDbkeyPosition() != null) {
 
     stringBuffer.append(TEXT_19);
     stringBuffer.append( memberRole.getPriorDbkeyPosition() );
     
     }
     if (memberRole.getIndexDbkeyPosition() != null) {
 
     stringBuffer.append(TEXT_20);
     stringBuffer.append( memberRole.getIndexDbkeyPosition() );
     
     } else if (set.getSystemOwner() != null) {
 
     stringBuffer.append(TEXT_21);
     
     }
     if (memberRole.getOwnerDbkeyPosition() != null) {
 
     stringBuffer.append(TEXT_22);
     stringBuffer.append( memberRole.getOwnerDbkeyPosition() );
     
     }    
 
     stringBuffer.append(TEXT_23);
     stringBuffer.append( memberRole.getMembershipOption().toString().replaceAll("_", " ") );
     
     if (set.getOrder() == SetOrder.SORTED) {
         Key key = memberRole.getSortKey();
 
     stringBuffer.append(TEXT_24);
     
         for (KeyElement keyElement : key.getElements()) {
             String bracket;
             if (keyElement == key.getElements().get(key.getElements().size() - 1)) {
                 bracket = ")";
             } else {
                 bracket = "";
             }
            String elementOrDbkey;
            if (!keyElement.isDbkey()) {
            	elementOrDbkey = keyElement.getElement().getName();
            } else {
            	elementOrDbkey = "DBKEY";
            }
 
     stringBuffer.append(TEXT_25);
    stringBuffer.append( elementOrDbkey );
     stringBuffer.append(TEXT_26);
     stringBuffer.append( keyElement.getSortSequence() );
     stringBuffer.append(TEXT_27);
     stringBuffer.append( bracket );
     
         }
 
     stringBuffer.append(TEXT_28);
     stringBuffer.append( key.getDuplicatesOption().toString().replaceAll("_", " ") );
     
         if (key.isNaturalSequence()) {
 
     stringBuffer.append(TEXT_29);
     
         }
         if (set.getMode() == SetMode.INDEXED && key.isCompressed()) {
 
     stringBuffer.append(TEXT_30);
     
         } else if (set.getMode() == SetMode.INDEXED) {
 
     stringBuffer.append(TEXT_31);
     
         }
     }
 }
 
     stringBuffer.append(TEXT_32);
     return stringBuffer.toString();
   }
 }
