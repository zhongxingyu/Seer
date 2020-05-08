 package eionet.meta.exports.codelist;
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 
 import eionet.meta.*;
 import eionet.util.Util;
 
 public class CodelistCSV extends Codelist{
 	
 	/**
 	 * 
 	 * @param conn
 	 * @param writer
 	 * @param delim
 	 */
 	public CodelistCSV(Connection conn, PrintWriter writer){
 		
 		this.writer = writer;
 		if (conn!=null){
 			searchEngine = new DDSearchEngine(conn);
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see eionet.meta.exports.codelist.Codelist#write(java.lang.String, java.lang.String)
 	 */
 	public void write(String objID, String objType) throws Exception{
 		
 		Vector elms = new Vector();
 		if (objType.equalsIgnoreCase(ELM)){
 			DataElement elm = searchEngine.getDataElement(objID);
 			if (elm!=null){
 				elms.add(elm);
 			}
 		}
 		else if (objType.equalsIgnoreCase(TBL)){
 			elms = searchEngine.getDataElements(null, null, null, null, objID, null);
 		}
 		else if (objType.equalsIgnoreCase(DST)){
 			elms = searchEngine.getDataElements(null, null, null, null, null, objID);
 		}
 		else{
 			throw new IllegalArgumentException("Unknown object type: " + objType);
 		}
 		
 		write(elms, objType);
 	}
 
 	/**
 	 * 
 	 * @param elms
 	 * @throws Exception
 	 */
 	private void write(Vector elms, String objType) throws Exception{
 		
 		if (elms==null || elms.isEmpty()){
 			return;
 		}
 		
 		boolean elmObjType = objType.equalsIgnoreCase(ELM);
 		
 		for (int i=0; i<elms.size(); i++){
 			
 			DataElement elm = (DataElement)elms.get(i);
 			String elmIdf = elm.getIdentifier();
 			if (Util.voidStr(elmIdf)){
 				throw new DDRuntimeException("Failed to get the element's identifier");
 			}
 			
			String dstIdf = elm.getDstIdentifier();
			String tblIdf = elm.getTblIdentifier();
 			if (!elmObjType || (elmObjType && !elm.isCommon())){
 				
 				if (Util.voidStr(dstIdf)){
 					throw new DDRuntimeException("Failed to get the dataset's identifier");
 				}
 				if (Util.voidStr(tblIdf)){
 					throw new DDRuntimeException("Failed to get the table's identifier");
 				}
 
 			}
 				
 			Vector fxvs = searchEngine.getFixedValues(elm.getID());
 			for (int j=0; fxvs!=null && j<fxvs.size(); j++){
 
 				FixedValue fxv = (FixedValue)fxvs.get(j);
 				String value = fxv.getValue();
 				if (value!=null && value.trim().length()>0){
 					
 					StringBuffer line = new StringBuffer();
 
 					append(line, dstIdf);
 					line.append(",");
 					append(line, tblIdf);
 					line.append(",");
 					append(line, elm.getShortName());
 					line.append(",");
 
 					// append field indicating if the element has fixed or quantitative values
 					line.append(elm.getType().equals("CH1") ? Boolean.TRUE : Boolean.FALSE);
 					line.append(",");
 					
 					append(line, value);
 					line.append(",");
 					append(line, fxv.getDefinition()==null ? "" : fxv.getDefinition());
 					line.append(",");
 					append(line, fxv.getShortDesc()==null ? "" : fxv.getShortDesc());
 					
 					lines.add(line.toString());
 				}
 			}
 		}
 		
 		if (lines==null || lines.isEmpty()){
 			lines.add("No codelists found!");
 		}
 		else{
 			// header line where the field meanings are explained
 			lines.add(0, "Dataset,Table,Element,Fixed,Value,Definition,ShortDescription");
 		}
 	}
 	
 	/**
 	 * 
 	 * @param buf
 	 * @param s
 	 */
 	private void append(StringBuffer buf, String s){
 		
 		buf.append("\"").append(s.replaceAll("\"", "\"\"")).append("\"");
 	}
 }
