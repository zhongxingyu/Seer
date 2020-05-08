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
 package org.lh.dmlj.schema.editor.importtool.syntax;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.lh.dmlj.schema.AreaProcedureCallFunction;
 import org.lh.dmlj.schema.ProcedureCallTime;
 import org.lh.dmlj.schema.editor.importtool.IAreaDataCollector;
 
 public class AreaDataCollector 
 	implements IAreaDataCollector<SchemaSyntaxWrapper> {
 
 	private static List<String> getProcedureLines(SchemaSyntaxWrapper context) {
 		
 		List<String> list = new ArrayList<>();
 		String scanItem = "         CALL ";				
 		for (String line : context.getLines()) {
 			if (line.startsWith(scanItem)) {				
 				list.add(line);
 			}
 		}		
 		return list;
 	}
 	
 	public AreaDataCollector() {
 		super();
 	}
 
 	@Override
 	public String getName(SchemaSyntaxWrapper context) {
 		return context.getLines().get(1).substring(18).trim();
 	}
 
 	@Override
 	public Collection<ProcedureCallTime> getProcedureCallTimes(SchemaSyntaxWrapper context) {
 		
 		List<String> procedureNames = new ArrayList<>(getProceduresCalled(context));
 		List<String> procedureLines = getProcedureLines(context);
 		
 		List<ProcedureCallTime> list = new ArrayList<>();
 		
 		for (int k = 0; k < procedureLines.size(); k++) {
 			
 			String procedureName = procedureNames.get(k);
 			String line = procedureLines.get(k);
 			
 			int i = line.indexOf(" " + procedureName + " ") + procedureName.length() + 2;
 			
 			if (line.substring(i).startsWith("ON ERROR DURING")) {
 				list.add(ProcedureCallTime.ON_ERROR_DURING);
 			} else {
 				int j = line.indexOf(" ", i);
 				String p;
 				if (j > -1) {
 					p = line.substring(i, j);
 				} else {
 					p = line.substring(i);
 				}
 				ProcedureCallTime procedureCallTime = ProcedureCallTime.valueOf(p);
 				list.add(procedureCallTime);
 			}
 		
 		}
 		
 		return list;
 		
 	}
 
 	@Override
 	public Collection<AreaProcedureCallFunction> getProcedureCallFunctions(SchemaSyntaxWrapper context) {
 
 		List<String> procedureNames = new ArrayList<>(getProceduresCalled(context));
 		List<String> procedureLines = getProcedureLines(context);		
 		
 		List<AreaProcedureCallFunction> list = new ArrayList<>();
 		
 		for (int k = 0; k < procedureLines.size(); k++) {
 			
 			String procedureName = procedureNames.get(k);
 			String line = procedureLines.get(k);
 		
 			int i = line.indexOf(" " + procedureName + " ") + procedureName.length() + 2;		
 	
 			int j;
 			if (line.substring(i).startsWith("ON ERROR DURING")) {
 				j = i + 15;
 			} else {
 				j = line.indexOf(" ", i);
 			}			
 			if (j > -1) {
 				String p = line.substring(j).trim();
 				if (p.equals("")) {
 					list.add(AreaProcedureCallFunction.EVERY_DML_FUNCTION);									
 				} else {					
 					if (!p.startsWith("READY")) {
 						AreaProcedureCallFunction areaProcedureCallFunction =
 							AreaProcedureCallFunction.valueOf(p);
 						list.add(areaProcedureCallFunction);
 					} else {
 						String q = p.replaceAll(" ", "_").replaceFirst("_FOR", "");
 						AreaProcedureCallFunction areaProcedureCallFunction =
 							AreaProcedureCallFunction.valueOf(q);
 						list.add(areaProcedureCallFunction);
 					}
 				}
 			} else {
 				list.add(AreaProcedureCallFunction.EVERY_DML_FUNCTION);
 			}			
 		}
 		
 		return list;
 		
 	}	
 
 	@Override
 	public Collection<String> getProceduresCalled(SchemaSyntaxWrapper context) {
 		List<String> list = new ArrayList<>();
 		for (String line : context.getLines()) {
 			if (line.startsWith("         CALL ")) {
 				int i = line.indexOf(" ", 14);
 				String procedureName = line.substring(14, i).trim();
				list.add(procedureName);
 			}
 		}
 		return list;
 	}
 
 }
