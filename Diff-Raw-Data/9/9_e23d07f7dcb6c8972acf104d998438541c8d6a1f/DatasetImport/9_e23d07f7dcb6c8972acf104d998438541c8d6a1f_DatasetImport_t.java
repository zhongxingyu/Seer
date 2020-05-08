 
 
 // Copyright (c) 2000 TietoEnator
 package eionet.meta.imp;
 
 
 import java.util.*;
 import java.sql.*;
 
 import eionet.meta.*;
 import eionet.meta.savers.*;
 
 import javax.servlet.ServletContext;
 
 import com.tee.xmlserver.AppUserIF;
 import com.tee.util.*;
 
 /**
  * A Class class.
  * <P>
  * @author Enriko Ksper
  */
 public class DatasetImport{
 
     public static String SEP = "_";
     public static String TYPE_FXV = "fixedValues";
     public static String TYPE_ALL = "datasets";
 
     private DatasetImportHandler handler;
     private DDSearchEngine searchEngine;
     private Connection conn = null;
     private ServletContext ctx = null;
 
     private StringBuffer responseText = new StringBuffer();
     private String lastInsertID = null;
     private Hashtable tables;
     private Vector dbSimpleAttrs;
     private Vector dbComplexAttrs;
 
 //Parameter vectors
 /*    private Vector ds_params;
 //    private Vector tbl_params;
     private Vector delem_params;
     private Vector fxv_params;
     private Vector tbl2elem_params;
 */
     private Hashtable all_params=null;
     private Hashtable tblMap=null;
     private Vector complex_attrs=null;
     private Hashtable unknown_tbl=null;
 //id mapping
     private Hashtable dsID=null;
     private Hashtable tblID=null;
     private Hashtable delemID=null;
     private Hashtable fxvID=null;
     private Hashtable fxvElemID=null;
 
     private int ds_count=0;
     private int delem_count=0;
     private int tbl_count=0;
     private int fxv_count=0;
 
     private int ds_count_all=0;
     private int delem_count_all=0;
     private int tbl_count_all=0;
     private int fxv_count_all=0;
 
     private String import_type=null;
     private String import_parent_id=null; // this can be delem_id, table_id or dataset_id depends on the import_type
     
     private AppUserIF user = null;
     
   /**
    * Constructor
    */
     public DatasetImport(DatasetImportHandler handler, Connection conn, ServletContext ctx, String type){
         this.handler = handler;
         this.conn = conn;
         this.searchEngine = new DDSearchEngine(conn, null, ctx);
         this.ctx=ctx;
         tblMap = new Hashtable();
         all_params = new Hashtable();
         setMapping();
     }
 
     public void execute() throws Exception {
 
       // get info from xml handler
       tables = handler.getTables();
 
       // check, if import type from interface is the same as import type in xml file
       String xml_import_type = handler.getImportName();
       if (import_type==null) import_type=TYPE_ALL;
       if (xml_import_type==null) xml_import_type=TYPE_ALL;
 
       if (!import_type.equalsIgnoreCase(xml_import_type)){
             responseText.append("<br>Import failed!");
             responseText.append("<br>Imported xml file does not have the same type.");
             responseText.append("<br>Import type:" + import_type + "; Xml file import type:" + xml_import_type);
             return;
       }
       // get attributes' names and ids from database
       setDBAttrs();
 
       // import only fixed values to one element
       if (import_type.equalsIgnoreCase(TYPE_FXV))
       {
         if (import_parent_id == null)
         {
             responseText.append("<br>Import failed!");
             responseText.append("<br>Data element id is not specified");
             return;         
         }
         setParams("FIXED_VALUE", true, "FXV", "new_value", null, null);
         delemID = new Hashtable();
         saveFixedValues();
         responseText.append("<br>Fixed values found:" + fxv_count_all + "; successfully imported:" + fxv_count);
       }
       // import datasets and its components
       else
       {
         setParams("DATASET", true, "DST", "ds_name", "DS", "ds_id");
         setParams("DS_TABLE", true, "TBL", "short_name", null, null);
         setParams("DATAELEM", true, null, "delem_name", "E", "delem_id");
         setParams("TBL2ELEM", false, null, null, null, null);
         setParams("FIXED_VALUE", true, "FXV", "new_value", null, null);
                           /*
                         //For testing
                         for (int i=0; i<complex_attrs.size(); i++){
                           Parameters params = (Parameters)complex_attrs.get(i);
                           Enumeration pars = params.getParameterNames();
                           String parname;
                           while (pars.hasMoreElements()){
                               parname=(String)pars.nextElement();
                               responseText.append(parname + "=" + params.getParameter(parname) + "|");
                           }
                           responseText.append("<br>");
                         }
                         */
           saveDataset();
           saveTables();
           saveDElem();
           saveFixedValues();
           saveComplexAttrs();       
       
   
           responseText.append("<br>");
           responseText.append("<br>Datasets found:" + ds_count_all + "; successfully imported:" + ds_count);
           responseText.append("<br>Dataset tables found:" + tbl_count_all + "; successfully imported:" + tbl_count);
           responseText.append("<br>Data elements found:" + delem_count_all + "; successfully imported:" + delem_count);
           responseText.append("<br>Fixed values found:" + fxv_count_all + "; successfully imported:" + fxv_count);
           responseText.append("<br>");
           responseText.append("<br>Unknown fields found from the following tables:");
           Enumeration keys = unknown_tbl.keys();
           while (keys.hasMoreElements()){
             String key = (String)keys.nextElement();
             responseText.append("<br>" + key + ": " + unknown_tbl.get(key).toString());
           }
       }
 
 
     }
     private void saveDataset() throws Exception{
         DatasetHandler dsHandler;
         Parameters par;
         dsID = new Hashtable();
 
         Vector ds_params = (Vector)all_params.get("DATASET");
         if(ds_params == null) return;
         if(ds_params.size()==0) return;
 
         for (int i=0; i< ds_params.size(); i++){
             par =(Parameters)ds_params.get(i);
                         /*For testing
                           Enumeration pars = par.getParameterNames();
                           String parname;
                           while (pars.hasMoreElements()){
                               parname=(String)pars.nextElement();
                               responseText.append(parname + "=" + par.getParameter(parname) + "|");
                           }
                           responseText.append("<br>");
                         */
             try{
                 dsHandler = new DatasetHandler(conn, par, ctx);
 				dsHandler.setUser(user);
                 dsHandler.setVersioning(false);
 				dsHandler.setImport(true);
                 dsHandler.execute();
                 ds_count++;
                 dsID.put((String)par.getParameter("ds_id"), (String)dsHandler.getLastInsertID());
              }
              catch(Exception e){
                  responseText.append("Dataset import failed! Could not store dataset into database - " +
                 par.getParameter("ds_name") + "<br>");
                  responseText.append(e.toString() + "<br>");
              }
         }
       //  responseText.append(dsID.toString());
     }
     private void saveTables() throws Exception{
         DsTableHandler tblHandler;
         Parameters par;
         String ds_id;
         tblID = new Hashtable();
 
         Vector tbl_params = (Vector)all_params.get("DS_TABLE");
 
         if(tbl_params == null) return;
         if(tbl_params.size()==0) return;
 
         for (int i=0; i< tbl_params.size(); i++){
             par =(Parameters)tbl_params.get(i);
             ds_id = par.getParameter("ds_id");
             if (dsID.containsKey(ds_id)){
                 par.removeParameter("ds_id");
                 par.addParameterValue("ds_id", (String)dsID.get(ds_id));
             }
             else{
                 responseText.append("Dataset id was not found for table: " +
                 par.getParameter("short_name") + "<br>");
                 continue;
             }
             try{
                 tblHandler = new DsTableHandler(conn, par, ctx);
 				tblHandler.setUser(user);
                 tblHandler.setVersioning(false);
 				tblHandler.setImport(true);
                 tblHandler.execute();
                 tbl_count++;
                 tblID.put((String)par.getParameter("tbl_id"), (String)tblHandler.getLastInsertID());
             }
             catch(Exception e){
                 responseText.append("Dataset import failed! Could not store dataset table into database - " +
                 par.getParameter("short_name") + "<br>");
                 responseText.append(e.toString() + "<br>");
             }
         }
   //      responseText.append(tblID.toString());
     }
     private void saveDElem() throws Exception{
         DataElementHandler delemHandler;
         Parameters par;
         delemID = new Hashtable();
         String delem_id;
 
         Vector delem_params = (Vector)all_params.get("DATAELEM");
 
         if(delem_params == null) return;
         if(delem_params.size()==0) return;
 
         for (int i=0; i< delem_params.size(); i++){
             par =(Parameters)delem_params.get(i);
             delem_id=(String)par.getParameter("delem_id");
             getTbl2elem(delem_id, par);
             par.removeParameter("delem_id");
             try{
                 delemHandler = new DataElementHandler(conn, par, ctx);
 				delemHandler.setUser(user);
                 delemHandler.setVersioning(false);
 				delemHandler.setImport(true);
                 delemHandler.execute();
                 delem_count++;
                 delemID.put(delem_id, (String)delemHandler.getLastInsertID());
             }
             catch(Exception e){
                 responseText.append("Dataset import failed! Could not store data element into database - " +
                 par.getParameter("delem_name") + "<br>");
                 responseText.append(e.toString() + "<br>");
             }
         }
        // responseText.append(delemID.toString());
     }
     private void getTbl2elem(String elem_id, Parameters par){
         Parameters tbl2elem_par;
         String tbl_id;
         String parelem_id;
 
         Vector tbl2elem_params = (Vector)all_params.get("TBL2ELEM");
 
         for(int i=0; i<tbl2elem_params.size(); i++){
             tbl2elem_par = (Parameters)tbl2elem_params.get(i);
             parelem_id = (String)tbl2elem_par.getParameter("delem_id");
             if (parelem_id.equals(elem_id)){
                 tbl_id = (String)tbl2elem_par.getParameter("table_id");
                 par.addParameterValue("table_id", (String)tblID.get(tbl_id));
                 //par.addParameterValue("pos", (String)tbl2elem_par.getParameter("pos"));
                 break;
             }
         }
     }
     
 	private void saveFixedValues() throws Exception{
 		FixedValuesHandler fxvHandler = null;
 		Parameters par;
 		String delem_id=null;
 		String parent_id=null;
 		String parentType="elem";
 		String fxv_val=null;
 		fxvID = new Hashtable();  // stores keys - fxv id in xml; values - dataelem id in db
 		fxvElemID = new Hashtable();  // stores keys - fxv id in xml; values - dataelem id in db
 		//boolean bHasParent = false;
 
 		Vector fxv_params = (Vector)all_params.get("FIXED_VALUE");
 
 		if(fxv_params == null) return;
 		if(fxv_params.size()==0) return;
 
 		fxv_count_all = fxv_params.size();
 
 		for (int i=0; i< fxv_params.size(); i++){
 			par =(Parameters)fxv_params.get(i);
 			//bHasParent=false;
 
 			fxv_val = par.getParameter("new_value");
 			if (fxv_val == null) par.addParameterValue("new_value", "");
 
 			delem_id = par.getParameter("delem_id");
 			parent_id = par.getParameter("parent_id");
 
 			if (delem_id==null) delem_id="0";
 			
 			/*
 			if (delem_id.equals("0")) bHasParent=true;
 			if (delem_id.equals("")) bHasParent=true;
 
 			if (bHasParent==true){  // value is child element for another value
 				if (fxvID.containsKey(parent_id)){
 					par.removeParameter("parent_id");
 					par.addParameterValue("parent_csi", (String)fxvID.get(parent_id));
 				}
 				else{
 					responseText.append("Fixed value parent id was not found for fixed value " +
 					fxv_val + "<br>");
 					continue;
 				}
 				if (fxvElemID.containsKey(parent_id)){
 					delem_id=(String)fxvElemID.get(parent_id);
 					par.removeParameter("delem_id");
 					par.addParameterValue("delem_id", delem_id);
 				}
 				else{
 					responseText.append("Fixed value parent id was not found for fixed value " +
 					fxv_val + "<br>");
 					continue;
 				}
 			}
 			else{   // value is on the top level
 			*/
 			
 			if (import_type.equalsIgnoreCase(TYPE_FXV))
 			{
 			  delemID.put(delem_id, import_parent_id);
 			}
             
 			if (delemID.containsKey(delem_id)){
 				delem_id = (String)delemID.get(delem_id);
 				par.removeParameter("delem_id");
 				par.addParameterValue("delem_id", delem_id);
 			}
 			else{
 				responseText.append("Data element id was not found for fixed value " +
 				fxv_val + "<br>");
 				continue;
 			}
 			//}
 
 			try{
 				par.addParameterValue("parent_type", parentType);
 				
 				if (fxvHandler==null ||
 						!delem_id.equals(fxvHandler.getOwnerID())){
 					fxvHandler = new FixedValuesHandler(conn, par, ctx);
 					fxvHandler.setVersioning(false);
 				}
 				
 				fxvHandler.execute(par);
 				fxv_count++;
 				fxvID.put(par.getParameter("id"), (String)fxvHandler.getLastInsertID());
 				fxvElemID.put(par.getParameter("id"), delem_id);
 			 }
 			catch(Exception e){
 				responseText.append("Dataset import failed! Could not store fixed value into database - " +
 				fxv_val + "<br>");
 				responseText.append(e.toString() + "<br>");
 			}
 		}
 	}
     
     private void saveComplexAttrs() throws Exception{
         AttrFieldsHandler saveHandler;
         Parameters par;
         String parent_id;
         String parent_type;
 
         if(complex_attrs == null) return;
         if(complex_attrs.size()==0) return;
 
         for (int i=0; i< complex_attrs.size(); i++){
             par =(Parameters)complex_attrs.get(i);
             parent_id = par.getParameter("parent_id");
             parent_type = par.getParameter("parent_type");
 //responseText.append("d:" + parent_id);
             if (parent_type.equals("E")){  //element
                 if (delemID.containsKey(parent_id)){
                     par.removeParameter("parent_id");
                     par.addParameterValue("parent_id", (String)delemID.get(parent_id));
                 }
                 else{
                     responseText.append("Data element id was not found for complex attribute.<br>");
                     continue;
                 }
             }
             else if (parent_type.equals("DS")){  //dataset
                 if (dsID.containsKey(parent_id)){
                     par.removeParameter("parent_id");
                     par.addParameterValue("parent_id", (String)dsID.get(parent_id));
                 }
                 else{
                     responseText.append("Data element id was not found for complex attribute.<br>");
                     continue;
                 }
             }
             else
                 continue;
             try{
                 saveHandler = new AttrFieldsHandler(conn, par, ctx);
                 saveHandler.setVersioning(false);
                 saveHandler.execute();
              }
             catch(Exception e){
                 responseText.append("Could not store complex attributes into database");
                 responseText.append(e.toString() + "<br>");
             }
         }
     }
     private void replaceAttributes(Hashtable impAttrs, Parameters params){
        String attrName;
        Enumeration attrKeys = impAttrs.keys();
        while (attrKeys.hasMoreElements()){
           attrName = (String)attrKeys.nextElement();
           String attrValue = (String)impAttrs.get(attrName);
       }
     }
     private void setParams(String table, boolean hasAttrs, String type, String context, String parent_type, String id_field){
       Parameters params;
       Vector tbl_params;
       Hashtable row;
       Vector rowMap;
       Hashtable fieldMap;
       String importValue;
       String allowNull;
 
       tbl_params=new Vector();
 
       Vector tbl = (Vector)tables.get(table);
       if (table.equals("DATASET"))
           ds_count_all=tbl.size();
       else if (table.equals("DS_TABLE"))
           tbl_count_all=tbl.size();
       else if (table.equals("DATAELEM"))
           delem_count_all=tbl.size();
 
       for (int i=0; i<tbl.size(); i++){
           row= (Hashtable)tbl.get(i);
 
           params = new Parameters();
           params.addParameterValue("mode", "add");
 
           rowMap=(Vector)tblMap.get(table);
           for (int c=0; c<rowMap.size(); c++){
               fieldMap=(Hashtable)rowMap.get(c);
               importValue = (String)row.get((String)fieldMap.get("imp"));
               allowNull=(String)fieldMap.get("allowNull");
               if (allowNull.equals("false")){
                   if (importValue==null || importValue.length() == 0){
                     responseText.append((String)fieldMap.get("text") + " is empty!<br>");
                     break;
                   }
               }
               row.remove((String)fieldMap.get("imp"));
               params.addParameterValue((String)fieldMap.get("param"), importValue);
           }
           if (hasAttrs){
               if (type==null)
                   type=params.getParameter("type");
               getSimpleAttrs(row, params, type, (String)params.getParameter(context));
               if (parent_type!=null){
                 try{
                   getComplexAttrs(row, (String)params.getParameter(id_field), parent_type);
                 }
                 catch(Exception e){
                   responseText.append("Reading complex attributes failed - " +
                   (String)params.getParameter("context") + "<br>");
                   responseText.append(e.toString() + "<br>");
                 }
               }
           }
                         /*For testing
                           Enumeration pars = params.getParameterNames();
                           String parname;
                           while (pars.hasMoreElements()){
                               parname=(String)pars.nextElement();
                               responseText.append(parname + "=" + params.getParameter(parname) + "|");
                           }
                           responseText.append("<br>");
                         */
           addUnknown(table, row);
           tbl_params.add(params);
       }
       all_params.put(table, tbl_params);
     }
     private void getSimpleAttrs(Hashtable row, Parameters params, String type, String context_name){
         String attrName=null;
         String attrValue=null;
         String impAttrName=null;
         String impAttrValue=null;
         boolean dispMult=false;
         for (int i=0; i< dbSimpleAttrs.size(); i++){
             DElemAttribute delemAttr = (DElemAttribute)dbSimpleAttrs.get(i);
             if (delemAttr.displayFor(type)){
               attrName = delemAttr.getShortName();
               dispMult = delemAttr.getDisplayMultiple().equals("1") ? true:false;
 
 //find attributes with multiple values
               if (dispMult){
                 for(int c=1; c<=9; c++){
                   impAttrName = attrName.toLowerCase() + SEP + Integer.toString(c);
                   if(row.containsKey(impAttrName)){
                     impAttrValue = (String)row.get(impAttrName);
                     if (impAttrValue != null && impAttrValue.length() > 0){
                       params.addParameterValue(DataElementHandler.ATTR_MULT_PREFIX + delemAttr.getID(), impAttrValue);
                     }
                     row.remove(impAttrName);
                  }
                 }
               }
               else{
 //find mandatory attributes
                 if (delemAttr.getObligation().equals("M")){
                     if (row.containsKey(attrName.toLowerCase())){
                         attrValue = (String)row.get(attrName.toLowerCase());
                         if (attrValue == null || attrValue.length()==0){
                             responseText.append("Could not find mandatory attribute (" + attrName + ") value from specified xml for " + getContextName(type) + " - " + context_name + "!<br>");
                         }
                         else{
                             params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                         }
                         row.remove(attrName.toLowerCase());
                     }
                     else{
                         responseText.append("Could not find mandatory attribute (" + attrName + ") value from specified xml for " + getContextName(type) + " - " + context_name + "!<br>");
                     }
                 }
 //find other attributes
                 else{
                     if (row.containsKey(attrName.toLowerCase())){
                         attrValue = (String)row.get(attrName.toLowerCase());
                         if (attrValue != null && attrValue.length() != 0){
                             params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                         }
                         row.remove(attrName.toLowerCase());
                     }
                 }
               }
             }
         }
     }
     private void  getComplexAttrs(Hashtable row, String parent_id, String parent_type) throws Exception{
         String attrName;
         String attrValue;
         String attr_id;
         Enumeration impAttrKeys;
         String impFieldName=null;
         Parameters par;
         String fieldName;
         Vector attrFields=null;
         Hashtable field;
         String impAttrValue;
         boolean bHasAttr=false;
         boolean bHasField=true;
 
         if (complex_attrs == null) complex_attrs = new Vector();
         for (int i=0; i< dbComplexAttrs.size(); i++){
             bHasAttr = false;
             DElemAttribute delemAttr = (DElemAttribute)dbComplexAttrs.get(i);
             attrName = delemAttr.getShortName();
             attr_id = delemAttr.getID();
             impAttrKeys = row.keys();
             while (impAttrKeys.hasMoreElements()){
               impFieldName = (String)impAttrKeys.nextElement();
               bHasAttr = (impFieldName.startsWith(attrName.toLowerCase() + SEP)) ? true : false;
               if (bHasAttr) break;
             }
             if (bHasAttr){
                 attrFields = searchEngine.getAttrFields(attr_id);
                 if (attrFields==null) continue;
                 for(int c=1; bHasField; c++){
                   bHasField = false;
                   par = new Parameters();
                   for (int j=0; j< attrFields.size(); j++){
                     field =  (Hashtable)attrFields.get(j);
                     fieldName = (String)field.get("name");
                     impFieldName = attrName.toLowerCase() + SEP + fieldName.toLowerCase() + SEP + Integer.toString(c);
                     if(row.containsKey(impFieldName)){
                         impAttrValue = (String)row.get(impFieldName);
                         if (impAttrValue == null || impAttrValue.length() == 0){
                           row.remove(impFieldName);
                           continue;
                         }
 
                         par.addParameterValue(AttrFieldsHandler.FLD_PREFIX + (String)field.get("id"), impAttrValue);
                         bHasField = true;
                     }
                   }
 
                   if (bHasField){
                     par.addParameterValue("mode", "add");
                     par.addParameterValue("parent_type", parent_type);
                     par.addParameterValue("parent_id",  parent_id);
                     par.addParameterValue("position", Integer.toString(c));
                    // par.addParameterValue("name", impFieldName);
                     par.addParameterValue("attr_id", attr_id);
 
                     complex_attrs.add(par);
                   }
                 }
                 bHasField=true;
             }
             //remove empty attributes rows
             impAttrKeys = row.keys();
             while (impAttrKeys.hasMoreElements()){
               impFieldName = (String)impAttrKeys.nextElement();
               for (int j=0; j< attrFields.size(); j++){
                 field =  (Hashtable)attrFields.get(j);
                 fieldName = (String)field.get("name");
                 if (impFieldName.startsWith(attrName.toLowerCase() + SEP + fieldName.toLowerCase()))
                     row.remove(impFieldName);
               }
             }
         }
     }
 
     private void setDBAttrs() throws Exception{
         dbSimpleAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_SIMPLE);
         dbComplexAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
     }
     private void setMapping(){
         Vector rowMap=new Vector();
 
         //DATASET
         rowMap.add(getFieldMap("dataset_id", "ds_id", false, "dataset id in DATASET table"));
         rowMap.add(getFieldMap("short_name", "ds_name", false, "dataset short name in DATASET table"));
 		rowMap.add(getFieldMap("identifier", "idfier", false, "dataset identifier in DATASET table"));
         rowMap.add(getFieldMap("version", "version", false, "Dataset version in DATASET table"));
 		rowMap.add(getFieldMap("regstatus", "reg_status", true, "REG_STATUS in DATASET table"));
         tblMap.put("DATASET", rowMap);
         rowMap = new Vector();
 
         //DS_TABLE
         rowMap.add(getFieldMap("table_id", "tbl_id", false, "dataset table id in DS_TABLE table"));
         rowMap.add(getFieldMap("dataset_id", "ds_id", false, "dataset id in DS_TABLE table"));
         rowMap.add(getFieldMap("short_name", "short_name", false, "dataset table short name in DS_TABLE table"));
 		rowMap.add(getFieldMap("identifier", "idfier", false, "dataset table identifier in DS_TABLE table"));
 		rowMap.add(getFieldMap("regstatus", "reg_status", true, "REG_STATUS in DS_TABLE table"));
         //rowMap.add(getFieldMap("name", "full_name", true, "dataset table full name in DS_TABLE table"));
         //rowMap.add(getFieldMap("definition", "definition", true, "Dataset table definition short name in DS_TABLE table"));
         tblMap.put("DS_TABLE", rowMap);
         rowMap = new Vector();
 
         //DATA ELEMENT
         rowMap.add(getFieldMap("dataelem_id", "delem_id", false, "data element id in DATAELEM table"));
         rowMap.add(getFieldMap("type", "type", false, "data element type in DATAELEM table"));
         rowMap.add(getFieldMap("short_name", "delem_name", false, "data element short name in DATAELEM table"));
 		rowMap.add(getFieldMap("identifier", "idfier", false, "data element identifier in DATAELEM table"));
		rowMap.add(getFieldMap("gis", "gis", false, "GIS in DATAELEM table"));
 		rowMap.add(getFieldMap("regstatus", "reg_status", true, "REG_STATUS in DATAELEM table"));
  //       rowMap.add(getFieldMap("namespace_id", "ns", true, "data element namespace_id in DATAELEM table"));
         tblMap.put("DATAELEM", rowMap);
         rowMap = new Vector();
 
         //TBL2ELEM
         rowMap.add(getFieldMap("dataelem_id", "delem_id", false, "data element id in TBL2ELEM table"));
         rowMap.add(getFieldMap("table_id", "table_id", false, "dataset table id in TBL2ELEM"));		
         //rowMap.add(getFieldMap("position", "pos", true, "position in TBL2ELEM table"));
         tblMap.put("TBL2ELEM", rowMap);
         rowMap = new Vector();
 
         //FIXED VALUE
         rowMap.add(getFieldMap("dataelem_id", "delem_id", true, "data element id in FIXED_VALUE table"));
         //rowMap.add(getFieldMap("parent_id", "parent_id", true, "parent fixed value id in FIXED_VALUE table"));
         rowMap.add(getFieldMap("fixed_value_id", "id", true, "id"));
         rowMap.add(getFieldMap("value", "new_value", true, "fixed value in FIXED_VALUE table"));
 		rowMap.add(getFieldMap("definition", "definition", true, "definition in FIXED_VALUE table"));
 		rowMap.add(getFieldMap("shortdescription", "short_desc", true, "shortdescription in FIXED_VALUE table"));
         tblMap.put("FIXED_VALUE", rowMap);
     }
     private void addUnknown(String table, Hashtable row){
         Vector unknown=null;
         String field;
 
         if (unknown_tbl == null) unknown_tbl = new Hashtable();
         if (unknown_tbl.containsKey(table)){
             unknown=(Vector)unknown_tbl.get(table);
         }
         if (unknown==null) unknown=new Vector();
         Enumeration keys = row.keys();
         while (keys.hasMoreElements()){
             field = (String)keys.nextElement();
             if (unknown.indexOf(field)==-1){
                 unknown.add(field);
             }
         }
         if(unknown.size()>0) unknown_tbl.put(table, unknown);
 
     }
     private Hashtable getFieldMap(String impField, String param, boolean allowNull, String text){
         Hashtable fieldMap = new Hashtable();
         fieldMap.put("imp", impField);
         fieldMap.put("param", param);
         fieldMap.put("allowNull", String.valueOf(allowNull));
         fieldMap.put("text", text);
 
         return fieldMap;
 
     }
     private String getContextName(String code){
         String ret="";
         if (code.equals("DST"))
             ret = "dataset";
         if (code.equals("FXV"))
             ret = "allowable value";
         if (code.equals("CH1"))
             ret = "data element with fixed values";
         if (code.equals("CH2"))
             ret = "data element with quantitative values";
         if (code.equals("TBL"))
             ret = "dataset table";
         return ret;
     }
     public String getResponseText(){
         return responseText.toString();
     }
     public void setImportType(String type)
     {
       if (type.equals("FXV"))
           import_type= TYPE_FXV;
       else
           import_type= TYPE_ALL;     
     }
     public void setParentID(String parent_id)
     {
         this.import_parent_id = parent_id;
     }
     
     public void setUser(AppUserIF user){
     	this.user = user;
     }
 }
 
