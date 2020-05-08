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
 package org.lh.dmlj.schema.editor.importtool.syntax;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.lh.dmlj.schema.DuplicatesOption;
 import org.lh.dmlj.schema.SetMembershipOption;
 import org.lh.dmlj.schema.SetMode;
 import org.lh.dmlj.schema.SetOrder;
 import org.lh.dmlj.schema.SortSequence;
 import org.lh.dmlj.schema.editor.importtool.ISetDataCollector;
 
 public class SetDataCollector 
 	implements ISetDataCollector<SchemaSyntaxWrapper> {
 
 	public SetDataCollector() {
 		super();
 	}
 
 	@Override
 	public Short getDisplacementPageCount(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.startsWith("         MODE IS INDEX ") &&
 				line.indexOf(" BLOCK CONTAINS ") > -1 &&
 				line.indexOf(" KEYS DISPLACEMENT IS ") > -1 &&
 				line.indexOf(" PAGES") > -1) {				
 				
 				int i = line.indexOf(" KEYS DISPLACEMENT IS ");
 				int j = line.indexOf(" PAGES");
 				return Short.valueOf(line.substring(i + 22, j).trim());
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public DuplicatesOption getDuplicatesOption(SchemaSyntaxWrapper context,
 												String memberRecordName) {
 		
 		int i = indexOf("                 DUPLICATES ARE ", context, 
 					    memberRecordName);
 		
 		String line = context.getLines().get(i);
 		if (line.substring(32).startsWith("BY DBKEY")) {
 			return DuplicatesOption.BY_DBKEY;
 		} else if (line.substring(32).startsWith("FIRST")) {
 			return DuplicatesOption.FIRST;
 		} else if (line.substring(32).startsWith("LAST")) {
 			return DuplicatesOption.LAST;
 		} else if (line.substring(32).startsWith("NOT ALLOWED")) {
 			return DuplicatesOption.NOT_ALLOWED;
 		} else {
 			return null;
 		}
 		
 	}
 
 	@Override
 	public Short getKeyCount(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.startsWith("         MODE IS INDEX ") &&
 				line.indexOf(" BLOCK CONTAINS ") > -1 &&
 				line.indexOf(" KEYS") > -1) {				
 				
 				int i = line.indexOf(" BLOCK CONTAINS ");
 				int j = line.indexOf(" KEYS");
 				return Short.valueOf(line.substring(i + 16, j).trim());
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Short getMemberIndexDbkeyPosition(SchemaSyntaxWrapper context) {
 		// for indexed sets only; indexed sets always have exactly 1 member		
 		String memberRecordName =
 			getMemberRecordNames(context).toArray(new String[] {})[0];				
 		int i = indexOf("             INDEX DBKEY POSITION IS ", context, 
 					    memberRecordName);
 		String p = context.getLines().get(i).substring(37).trim();
 		if (p.equals("OMITTED")) {
 			return null;
 		} else {
 			return Short.valueOf(p);
 		}		
 	}
 
 	@Override
 	public Short getMemberNextDbkeyPosition(SchemaSyntaxWrapper context,
 											String memberRecordName) {
 		
 		int i = indexOf("             NEXT DBKEY POSITION IS ", context, 
 			    		memberRecordName);
 		String p = context.getLines().get(i).substring(36).trim();
 		return Short.valueOf(p);
 	}
 
 	@Override
 	public Short getMemberOwnerDbkeyPosition(SchemaSyntaxWrapper context,
 											 String memberRecordName) {
 		
 		int i = indexOf("             OWNER DBKEY POSITION IS ", context, 
 			    		memberRecordName);
 		if (i > -1) {
 			String p = context.getLines().get(i).substring(37).trim();
 			return Short.valueOf(p);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public Short getMemberPriorDbkeyPosition(SchemaSyntaxWrapper context,
 											 String memberRecordName) {
 		
 		int i = indexOf("             PRIOR DBKEY POSITION IS ", context, 
 	    				memberRecordName);
 		if (i > -1) {
 			String p = context.getLines().get(i).substring(37).trim();
 			return Short.valueOf(p);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public Collection<String> getMemberRecordNames(SchemaSyntaxWrapper context) {
 		List<String> list = new ArrayList<>();
 		for (String line : context.getLines()) {
 			if (line.startsWith("         MEMBER IS ")) {
 				
 				String memberRecordName = line.substring(19).trim();; 
 				list.add(memberRecordName);
 			}
 		}
 		return list;
 	}
 
 	@Override
 	public String getName(SchemaSyntaxWrapper context) {
 		return context.getLines().get(1).substring(17).trim();
 	}
 
 	@Override
 	public short getOwnerNextDbkeyPosition(SchemaSyntaxWrapper context) {
 		
 		int i = 0;
 		while (!context.getLines().get(i).startsWith("         OWNER IS ")) {
 			i += 1;
 		}
 		i += 1;
 		
 		while (!context.getLines().get(i).startsWith("             NEXT DBKEY POSITION IS ") &&
 			   !context.getLines().get(i).startsWith("         MEMBER IS ") ) {
 			
 			i += 1;
 		}
 		if (context.getLines().get(i).startsWith("             NEXT DBKEY POSITION IS ")) {
 			String p = context.getLines().get(i).substring(36).trim();
 			return Short.valueOf(p).shortValue();
 		} else {		
 			return -1;
 		}
 		
 	}
 
 	@Override
 	public Short getOwnerPriorDbkeyPosition(SchemaSyntaxWrapper context) {
 		
 		int i = 0;
 		while (!context.getLines().get(i).startsWith("         OWNER IS ")) {
 			i += 1;
 		}
 		i += 1;
 		
 		while (!context.getLines().get(i).startsWith("             PRIOR DBKEY POSITION IS ") &&
 			   !context.getLines().get(i).startsWith("         MEMBER IS ") ) {
 			
 			i += 1;
 		}
 		if (context.getLines().get(i).startsWith("             PRIOR DBKEY POSITION IS ")) {
 			String p = context.getLines().get(i).substring(37).trim();
 			return Short.valueOf(p);
 		} else {		
 			return null;
 		}		
 		
 	}
 
 	@Override
 	public String getOwnerRecordName(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.startsWith("         OWNER IS ") &&
 				!line.startsWith("         OWNER IS SYSTEM")) {
 				
 				return line.substring(18).trim();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public SetMembershipOption getSetMembershipOption(SchemaSyntaxWrapper context, 
 													  String memberRecordName) {
 		
 		int i = indexOf(context, memberRecordName) + 1;		                
 		
 		while (i < context.getLines().size() &&
 			   !context.getLines().get(i).startsWith("         MEMBER IS ")) {
 			
 			String line = context.getLines().get(i);
 			if (line.startsWith("             MANDATORY AUTOMATIC")) {
 				return SetMembershipOption.MANDATORY_AUTOMATIC;
 			} else if (line.startsWith("             MANDATORY MANUAL")) {
 				return SetMembershipOption.MANDATORY_MANUAL;
 			} else if (line.startsWith("             OPTIONAL AUTOMATIC")) {
 				return SetMembershipOption.OPTIONAL_AUTOMATIC;
 			} else if (line.startsWith("             OPTIONAL MANUAL")) {				
 				return SetMembershipOption.OPTIONAL_MANUAL;
 			}
 			i += 1;
 		}
 		
 		return null;
 	}
 
 	@Override
 	public SetMode getSetMode(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.startsWith("         MODE IS ")) {
 				if (line.substring(17).startsWith("CHAIN")) {
 					return SetMode.CHAINED;
 				} else if (line.substring(17).startsWith("INDEX")) {
 					return SetMode.INDEXED;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public SetOrder getSetOrder(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.startsWith("         ORDER IS ")) {
 				if (line.substring(18).startsWith("FIRST")) {
 					return SetOrder.FIRST;
 				} else if (line.substring(18).startsWith("LAST")) {
 					return SetOrder.LAST;
 				} else if (line.substring(18).startsWith("NEXT")) {
 					return SetOrder.NEXT;
 				} else if (line.substring(18).startsWith("PRIOR")) {
 					return SetOrder.PRIOR;
 				} else if (line.substring(18).startsWith("SORTED")) {
 					return SetOrder.SORTED;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Collection<String> getSortKeyElements(SchemaSyntaxWrapper context,
 												 String memberRecordName) {
 						
 		List<String> list = new ArrayList<>();		
 		int i = indexOf("             KEY IS (", context, memberRecordName);		
 		do {
 			i += 1;
 			int j = context.getLines().get(i).indexOf(" ", 17);
 			String elementName = context.getLines().get(i).substring(17, j); 
 			list.add(elementName);			
 		} while (!context.getLines().get(i).trim().endsWith(" )"));
 		
 		return list;
 	}
 
 	@Override
 	public SortSequence getSortSequence(SchemaSyntaxWrapper context,
 										String memberRecordName, 
 										String keyElementName) {
 		
 		if (keyElementName != null) {
 			// the set member is NOT sorted by dbkey
 			String scanItem = "                 " + keyElementName + " ";
 			int i = indexOf(scanItem, context, memberRecordName);
 			int j = context.getLines().get(i).indexOf(" ", 17);
 			String p = context.getLines().get(i).substring(j + 1).trim();
 			if (p.startsWith("ASCENDING")) {
 				return SortSequence.ASCENDING;
 			} else if (p.startsWith("DESCENDING")) {
 				return SortSequence.DESCENDING;
 			} else {
 				return null;
 			}
 		} else {
 			// the set member IS sorted by dbkey
 			String scanItem = "                 DBKEY DESCENDING";
 			int i = indexOf(scanItem, context, memberRecordName);
 			if (i > -1) {				
 				return SortSequence.DESCENDING;
 			} else {
 				return SortSequence.ASCENDING;
 			}
 		}
 	}
 
 	@Override
 	public String getSymbolicIndexName(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.startsWith("         MODE IS INDEX USING ")) {				
 				return line.substring(29).trim();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getSystemOwnerAreaName(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
			// check for a WITHIN AREA clause, taking in mind that it can be commented out (meaning
			// the system owner is in the same area as the member record (but we return the area
			// name found in the WITHIN AREA clause)
			if (line.length() > 2 && line.substring(2).startsWith("           WITHIN AREA ")) {				
 				String p = line.substring(25).trim();
 				int i = p.indexOf(" ");
 				if (i > -1) {
 					return p.substring(0, i);
 				} else {
 					return p;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Integer getSystemOwnerOffsetOffsetPageCount(SchemaSyntaxWrapper context) {
 		// [...] WITHIN AREA EMP-DEMO-REGION OFFSET 1 PAGES FOR 4 PAGES
 		for (String line : context.getLines()) {
 			if (line.indexOf("WITHIN AREA ") > -1 &&
 				line.indexOf(" OFFSET ") > -1 &&
 				line.indexOf(" PAGES FOR ") > -1) {
 				
 				int i = line.indexOf(" OFFSET ");
 				int j = line.indexOf(" PAGES FOR ", i);
 				return Integer.valueOf(line.substring(i + 8, j).trim());
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Short getSystemOwnerOffsetOffsetPercent(SchemaSyntaxWrapper context) {
 		// [...] WITHIN AREA EMP-DEMO-REGION OFFSET 5 PERCENT FOR 4 PAGES
 		for (String line : context.getLines()) {
 			if (line.indexOf("WITHIN AREA ") > -1 &&
 				line.indexOf(" OFFSET ") > -1 &&
 				line.indexOf(" PERCENT FOR ") > -1) {
 				
 				int i = line.indexOf(" OFFSET ");
 				int j = line.indexOf(" PERCENT FOR ", i);
 				return Short.valueOf(line.substring(i + 8, j).trim());
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Integer getSystemOwnerOffsetPageCount(SchemaSyntaxWrapper context) {
 		// [...] WITHIN AREA EMP-DEMO-REGION OFFSET 5 PERCENT FOR 4 PAGES
 		for (String line : context.getLines()) {
 			if (line.indexOf("WITHIN AREA ") > -1 &&
 				line.indexOf(" OFFSET ") > -1 &&
 				line.indexOf(" FOR ") > -1 &&
 				line.trim().endsWith(" PAGES")) {
 				
 				int i = line.indexOf(" FOR ");
 				int j = line.indexOf(" PAGES", i);
 				return Integer.valueOf(line.substring(i + 5, j).trim());				
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Short getSystemOwnerOffsetPercent(SchemaSyntaxWrapper context) {
 		// [...] WITHIN AREA EMP-DEMO-REGION OFFSET 5 PERCENT FOR 5 PERCENT
 		for (String line : context.getLines()) {
 			if (line.indexOf("WITHIN AREA ") > -1 &&
 				line.indexOf(" OFFSET ") > -1 &&
 				line.indexOf(" FOR ") > -1 &&
 				line.trim().endsWith(" PERCENT")) {
 				
 				int i = line.indexOf(" FOR ");
 				int j = line.indexOf(" PERCENT", i);
 				return Short.valueOf(line.substring(i + 5, j).trim());				
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getSystemOwnerSymbolicSubareaName(SchemaSyntaxWrapper context) {
 		// [..] WITHIN AREA ALMAI102 SUBAREA AREA1
 		for (String line : context.getLines()) {
 			if (line.indexOf("WITHIN AREA ") > -1 &&
 			    line.indexOf(" SUBAREA ") > -1) {
 			    	
 			    int i = line.indexOf(" SUBAREA ");
 				return line.substring(i + 9).trim();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean getSortKeyIsNaturalSequence(SchemaSyntaxWrapper context,
 											   String memberRecordName) {
 		String scanItem = "                 NATURAL SEQUENCE";
 		return indexOf(scanItem, context, memberRecordName) > -1;
 	}
 
 	private int indexOf(SchemaSyntaxWrapper context, String memberRecordName) {
 	
 		String scanItem = "         MEMBER IS " + memberRecordName;
 		int i = 0;
 		while (i < context.getLines().size() &&
 			   !context.getLines().get(i).startsWith(scanItem)) {
 			
 			i += 1;
 		}
 		
 		if (i < context.getLines().size()) {
 			return i;
 		}
 		
 		throw new RuntimeException("logic error: no line found starting with '" +
 					   			   "         MEMBER IS ' for member record " + 
 					   			   memberRecordName + " (set=" +
 					   			   getName(context) + ")");
 
 }	
 	
 	private int indexOf(String scanItem, SchemaSyntaxWrapper context, 
 						String memberRecordName) {
 				
 		int i = indexOf(context, memberRecordName) + 1;
 		
 		while (i < context.getLines().size() &&
 			   !context.getLines().get(i).startsWith("         MEMBER IS ")) {
 			
 			String line = context.getLines().get(i);
 			if (line.startsWith(scanItem)) {
 				return i;
 			}
 			i += 1;
 		}
 		
 		return -1;
 		
 	}	
 	
 	@Override
 	public boolean isKeyCompressed(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.trim().equals("COMPRESSED") ||
 				line.trim().equals("UNCOMPRESSED")) {				
 				
 				return line.trim().equals("COMPRESSED");
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isSortedByDbkey(SchemaSyntaxWrapper context) {
 		// for indexed sets only; indexed sets always have exactly 1 member
 		for (String line : context.getLines()) {
 			if (line.trim().equals("DBKEY ASCENDING") ||
 				line.trim().equals("DBKEY DESCENDING")) {				
 				
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isSystemOwned(SchemaSyntaxWrapper context) {
 		for (String line : context.getLines()) {
 			if (line.startsWith("         OWNER IS SYSTEM")) {				
 				return true;
 			}
 		}
 		return false;
 	}	
 	
 }
