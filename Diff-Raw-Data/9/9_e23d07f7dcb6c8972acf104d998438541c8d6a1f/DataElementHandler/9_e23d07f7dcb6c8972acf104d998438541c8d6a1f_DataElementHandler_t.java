 package eionet.meta.savers;
 
 import java.io.PrintStream;
 import java.util.*;
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import eionet.meta.*;
 
 import com.tee.util.*;
 import com.tee.xmlserver.*;
 import com.tee.uit.security.*;
 
 public class DataElementHandler extends BaseHandler {
 
     public static String ATTR_PREFIX = "attr_";
     public static String ATTR_MULT_PREFIX = "attr_mult_";
 
     public static String INHERIT_ATTR_PREFIX = "inherit_";
     public static String INHERIT_COMPLEX_ATTR_PREFIX = "inherit_complex_";
 
     public static String POS_PREFIX = "pos_";
     public static String OLDPOS_PREFIX = "oldpos_";
 
     private String mode = null;
     private String type = null;
     private String delem_id = null;
     private String[] delem_ids = null;
     private String delem_name = null;
 	private String idfier = null;
     private String delem_class = null;
     private String lastInsertID = null;
     
     private String ns_id = null;
     
     private String table_id = null;
     
     private String schemaPhysPath = null;
     private String schemaUrlPath = null;
     
     private HashSet ch1ProhibitedAttrs = new HashSet();
     
     private String mDatatypeID = null; 
     private String datatypeValue = null;
     
     private DDSearchEngine searchEngine = null;
     
     private boolean checkInResult = false;
     
     boolean versioning = true;
     boolean superUser = false;
     
     /** indicates if top namespace needs to be released after an exception*/
     private boolean doCleanup = false;
     
     /**
     for deletion - a HashSet for remembering namespace ids and short_names
     of all working copies, so later we can find originals and deal with them*/
     HashSet originals = new HashSet();
         
     /** for deletion - remember the top namespaces */
     HashSet topns = new HashSet();
     
     /** for storing table ID returned by VersionManager.deleteElm() */
     private String newTblID = null;
 
 	/** for storing restored elm ID returned by Restorer.restoreElm() */
 	private String restoredID = null;
 	
 	private boolean importMode = false;
 
     /**
     *
     */
     public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
         this(conn, new Parameters(req), ctx);
     }
 
     public DataElementHandler(Connection conn, Parameters req, ServletContext ctx){
         this.conn = conn;
         this.req = req;
         this.ctx = ctx;
         this.mode = req.getParameter("mode");
         this.type = req.getParameter("type");
         this.delem_id = req.getParameter("delem_id");
         this.delem_ids = req.getParameterValues("delem_id");
         this.delem_name = req.getParameter("delem_name");
 		this.idfier = req.getParameter("idfier");
         this.delem_class = req.getParameter("delem_class");
         this.ns_id = req.getParameter("ns");
         this.table_id = req.getParameter("table_id");
         
         if (ctx!=null){
 	        String _versioning = ctx.getInitParameter("versioning");
 	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
 	            setVersioning(false);
         }
         
         // set the attributes prohibited to set for CH1 type elements
         try{
             searchEngine = new DDSearchEngine(conn, "", ctx);
             Vector v = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE);
             for (int i=0; v!=null && i<v.size(); i++){
                 DElemAttribute attr = (DElemAttribute)v.get(i);
                 if (attr.getShortName().equalsIgnoreCase("MinSize"))
                     ch1ProhibitedAttrs.add(attr.getID());
                 if (attr.getShortName().equalsIgnoreCase("MaxSize"))
                     ch1ProhibitedAttrs.add(attr.getID());
                 if (attr.getShortName().equalsIgnoreCase("Datatype"))
                     this.mDatatypeID = attr.getID();
             }
         }
         catch (Exception e){}
     }
     
     public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
         this(conn, req, ctx);
         this.mode = mode;
     }
     
     public void setUser(AppUserIF user){
         this.user = user;
     }
     
     public void setImport(boolean importMode){
     	this.importMode = importMode;
     }
     
     public void setVersioning(boolean f){
         this.versioning = f;
     }
     
     public boolean getVersioning(){
         return this.versioning;
     }
     
     public void setSuperUser(boolean su){
         this.superUser = su;
     }
     
     /**
      * 
      * @return
      */
     public String getNewTblID(){
     	return this.newTblID;
     }
     
     /**
     *
     */
     public void cleanup() throws Exception{
         
         if (!doCleanup) return;
         
         Statement stmt = conn.createStatement();
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("DATAELEM");
         
         // if there where any working copies, release originals
         Iterator iter=originals.iterator();
         while (iter.hasNext()){
             String s = (String)iter.next();
             int pos = s.indexOf(",");
             gen.setFieldExpr("WORKING_USER", "NULL");
             String q = gen.updateStatement() + " where WORKING_USER='" +
                         user.getUserName() + "' and PARENT_NS=" +
                         s.substring(0, pos) + " and IDENTIFIER='" +
                         s.substring(pos+1) + "'";
             stmt.executeUpdate(q);
         }
         
         // release the top namespaces
         gen.clear();
         gen.setTable("NAMESPACE");
         gen.setFieldExpr("WORKING_USER", "NULL");
         for (Iterator i=topns.iterator(); i.hasNext(); ){            
             stmt.executeUpdate(gen.updateStatement() +
                     " where NAMESPACE_ID=" + (String)i.next());
         }
         
         stmt.close();
     }
     
     public void execute() throws Exception {
         
         // initialize this.topNsReleaseNeeded (just in case)
         doCleanup = false;
         
         if (mode==null || (!mode.equalsIgnoreCase("add") &&
                           !mode.equalsIgnoreCase("edit") &&
                           !mode.equalsIgnoreCase("delete") &&
                           !mode.equalsIgnoreCase("copy") &&
 						  !mode.equalsIgnoreCase("restore") &&
                           !mode.equalsIgnoreCase("edit_tblelems")))
             throw new Exception("DataElementHandler mode unspecified!");
 
         if (mode.equalsIgnoreCase("add")){
             if (type==null || (!type.equalsIgnoreCase("CH1") &&
                             !type.equalsIgnoreCase("CH2")))
                 throw new Exception("DataElementHandler type unspecified!");
         }
 
         if (mode.equalsIgnoreCase("add") || mode.equalsIgnoreCase("copy")){
             insert();
             delem_id = getLastInsertID();
         }
         else if (mode.equalsIgnoreCase("edit"))
             update();
         else if (mode.equalsIgnoreCase("edit_tblelems"))
             processTableElems();
         else if (mode.equalsIgnoreCase("restore"))
         	restore();
         else{
             delete();
             cleanVisuals();
         }
     }
     
     /**
     *
     */
     private void insert() throws Exception {
 
         // get some stuff of the parent table
         String topNS = null;
         if (!Util.nullString(table_id)){
             if (searchEngine==null)
                 searchEngine = new DDSearchEngine(conn, "", ctx);
             DsTable dsTable = searchEngine.getDatasetTable(table_id);
             if (dsTable != null){
                 ns_id = dsTable.getNamespace();
                 topNS = dsTable.getParentNs();
             }
         }
         
         // make sure we don't try to add under a checked-out namespace
         if (topNS!=null){
 			VersionManager verMan = new VersionManager(conn, user);
 			verMan.setContext(ctx);
 			if (verMan.getWorkingUser(topNS) != null)
 				throw new Exception("Cannot add to a dataset in work!");
         }
         
         // see if making a copy
 		String copy_elem_id = req.getParameter("copy_elem_id");
 		if (copy_elem_id != null && copy_elem_id.length()!=0){
 			copyElem(copy_elem_id);
 			return;
 		}
         
         // make sure you have the necessary params
         if (idfier == null || ns_id == null)
             throw new SQLException("Identifier or namespace not specified!");
 
         // make sure such a data element doe not already exist
         if (exists()) throw new SQLException(
 					"Such a data element already exists!");
 
         SQLGenerator gen = new SQLGenerator();
         Statement stmt = conn.createStatement();
         
         // close the TOP namespace
         if (versioning && !Util.nullString(topNS)){
             gen.setTable("NAMESPACE");
             gen.setField("WORKING_USER", user.getUserName());
             stmt.executeUpdate(gen.updateStatement() +
                     " where NAMESPACE_ID=" + topNS);
         }
         
         // insert the element
         
         if (Util.nullString(delem_name))
 			delem_name = idfier;
         
         gen.clear();
         gen.setTable("DATAELEM");
         gen.setField("IDENTIFIER", idfier);
 		gen.setField("SHORT_NAME", delem_name);
         gen.setField("TYPE", type);
         gen.setField("PARENT_NS", ns_id);
         if (!Util.nullString(topNS))
             gen.setField("TOP_NS", topNS);
             
         String extension = req.getParameter("extends");
         if (extension != null && extension.length()!=0)
             gen.setFieldExpr("EXTENDS", extension);
             
 		String gisType = req.getParameter("gis");
		if (gisType!=null && gisType.length()==0 && importMode)
			gisType = null;
 		if (gisType!=null && !gisType.equals("nogis"))
 			gen.setField("GIS", gisType);
 
         // treat new elements as working copies until checked in
         if (versioning){
             gen.setField("WORKING_COPY", "Y");
             if (user!=null && user.isAuthentic())
                 gen.setField("WORKING_USER", user.getUserName());
         }
 
         // set the status
         String status = req.getParameter("reg_status");
         if (!Util.nullString(status))
             gen.setField("REG_STATUS", status);
         
         stmt.executeUpdate(gen.insertStatement());
 		setLastInsertID();
 		
         stmt.close();
 
         // process other stuff
         setLastInsertID();
         processDataClass();
         processAttributes();
         insertTableElem();
     }
 
     private void update() throws Exception {
         
 		SQLGenerator gen = new SQLGenerator();
 		gen.setTable("DATAELEM");
 		
         String status = req.getParameter("reg_status");
         if (!Util.nullString(status))
             gen.setField("REG_STATUS", status);
         
 		String gisType = req.getParameter("gis");
 		if (gisType==null || gisType.equals("nogis"))
 			gen.setFieldExpr("GIS", "NULL");
 		else
 			gen.setField("GIS", gisType);
 		
 		if (!Util.nullString(gen.getValues()))
 			conn.createStatement().executeUpdate(gen.updateStatement() + 
 										" where DATAELEM_ID=" + delem_id);        
         
 		// short name
 		if (!Util.nullString(delem_name)){
 			gen = new SQLGenerator();
 			gen.setTable("DATAELEM");
 			gen.setField("SHORT_NAME", delem_name);
 			conn.createStatement().executeUpdate(gen.updateStatement() +
 									" where DATAELEM_ID=" + delem_id);
 		}
 
         // if check-in, do the action and exit
         String checkIn = req.getParameter("check_in");
         if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
         	
             VersionManager verMan = new VersionManager(conn, user);
 			verMan.setContext(ctx);
 			
             String verUpw = req.getParameter("ver_upw");
             if (verUpw!=null && verUpw.equalsIgnoreCase("false"))
             	verMan.setUpwardsVersioning(false);
 			
 			String updVer = req.getParameter("upd_version");
 			if (updVer!=null && updVer.equalsIgnoreCase("true"))
 				verMan.updateVersion();
 				
             verMan.checkIn(delem_id, "elm",
                                     req.getParameter("reg_status"));
             return;
         }
         
         lastInsertID = delem_id;
         
         // if request parameter "elm" equals "exs",
         // it means we just need to add "delem_id" to table_id
         String elm = req.getParameter("elm");
         if (elm!=null && elm.equals("exs")){
             insertTableElem();
             return;
         }
         
         deleteAttributes();
         processAttributes();
         processDataClass();
     }
     
     private void delete() throws Exception {
 
         if (delem_ids==null || delem_ids.length==0)
             return;
 
         // do not allow deletion by unauthorized users
         if (user==null || !user.isAuthentic())
             throw new Exception("Unauthorized user!");
         
         // get more data about each element
         StringBuffer buf = new StringBuffer();
         buf.append("select * from DATAELEM where ");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0)
                 buf.append(" or ");
             buf.append("DATAELEM_ID=");
             buf.append(delem_ids[i]);
         }
         
         // loop over all elements
         // if an element is not a working copy, then it's deletion is
         // handled by VersionManager.deleteElm(), otherwise it is simply
         // deleted, but its original's WORKING_USER must be set to NULL
         Vector wrkCopies = new Vector();
         VersionManager verMan = null;
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(buf.toString());
         while (rs.next()){
             
             // see if the element is in work by another user
             String userName = rs.getString("WORKING_USER");
             if (userName!=null &&
                 !userName.equals(user.getUserName()) && !superUser)
                 throw new Exception("Element " + rs.getString("DATAELEM_ID") +
                     " is in work by another user: " + userName);
             
             boolean wrkCopy = rs.getString("WORKING_COPY").equals("Y") ?
                              true : false;
                              
             // if no versioning logic should be applied (for
             // example when overwriting a previous version),
             // then treat the given tables as if they were
             // working copies 
             if (!versioning){
                 wrkCopies.add(rs.getString("DATAELEM.DATAELEM_ID"));
                 if (wrkCopy && !superUser)
                     topns.add(rs.getString("DATAELEM.TOP_NS"));
             }
             // see if it's a working copy
             else if (wrkCopy){
                 originals.add(rs.getString("PARENT_NS") + "," +
                                 rs.getString("IDENTIFIER"));
                 wrkCopies.add(rs.getString("DATAELEM.DATAELEM_ID"));
                 topns.add(rs.getString("DATAELEM.TOP_NS"));
             }
             else{
                 // deletion of non-working copies is handled by verMan
                 if (verMan==null){
                     verMan = new VersionManager(conn, user);
 					verMan.setContext(ctx);
                 }
                 this.newTblID = verMan.deleteElm(rs.getString("DATAELEM_ID"));
             }
         }
         
         if (wrkCopies.size()==0)
             return;
         
         // start working with those legal for deletion
         delem_ids = new String[wrkCopies.size()];
         for (int i=0; i<wrkCopies.size(); i++)
             delem_ids[i] = (String)wrkCopies.get(i);
         
         // delete element dependencies
         deleteAttributes();
         deleteComplexAttributes();
         deleteFixedValues();
         deleteContents();
         deleteCsItems();
 		deleteFkRelations();
         
         // delete elm-tbl relations
         deleteTableElem();
         
         // we've passed the critical point, set cleanup is needed
         // in case an exception happens now
         doCleanup = true;
         
         // delete elements themselves
         buf = new StringBuffer("delete from DATAELEM where ");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0)
                 buf.append(" or ");
             buf.append("DATAELEM_ID=");
             buf.append(delem_ids[i]);
         }
         stmt.executeUpdate(buf.toString());
         stmt.close();
 
         // release originals and top namespaces
 		cleanup();
     }
 
 	/**
 	 * 
 	 */
 	private void restore() throws Exception {
     	
 		if (Util.nullString(delem_id)) return;
 		Restorer restorer = new Restorer(conn);
 		restorer.setUser(user);
 		this.restoredID = restorer.restoreElm(delem_id); 
 	}
 
     private void deleteContentDefinition() throws SQLException {
         
         StringBuffer buf = new StringBuffer("delete from CONTENT_DEFINITION where DATAELEM_ID=");
         buf.append(delem_id);
         
         log(buf.toString());
         
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(buf.toString());
         stmt.close();
     }
     
     private void deleteAttributes() throws SQLException {
     	
     	// find out image attributes, so to skip them later
 		StringBuffer buf = new StringBuffer("select M_ATTRIBUTE_ID ");
 		buf.append("from M_ATTRIBUTE where DISP_TYPE='image'");
 		
 		Vector imgAttrs = new Vector();
 		Statement stmt = conn.createStatement();
 		ResultSet rs = stmt.executeQuery(buf.toString());
 		while (rs.next())
 			imgAttrs.add(rs.getString(1));
         
         buf = new StringBuffer("delete from ATTRIBUTE where (");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0)
                 buf.append(" or ");
             buf.append("DATAELEM_ID=");
             buf.append(delem_ids[i]);
         }
 
         buf.append(") and PARENT_TYPE='E'");
 
 		// skip image attributes        
         for (int i=0; i<imgAttrs.size(); i++)
         	buf.append(" and M_ATTRIBUTE_ID<>").append((String)imgAttrs.get(i));
 
         log(buf.toString());
 
         stmt.executeUpdate(buf.toString());
         stmt.close();
     }
     
     private void deleteComplexAttributes() throws SQLException {
 
         for (int i=0; delem_ids!=null && i<delem_ids.length; i++){
             
             Parameters params = new Parameters();
             params.addParameterValue("mode", "delete");
             params.addParameterValue("legal_delete", "true");
             params.addParameterValue("parent_id", delem_ids[i]);
             params.addParameterValue("parent_type", "E");
             
             AttrFieldsHandler attrFieldsHandler =
                                 new AttrFieldsHandler(conn, params, ctx);
             attrFieldsHandler.setVersioning(versioning);
             try{
                 attrFieldsHandler.execute();
             }
             catch (Exception e){
                 throw new SQLException(e.toString());
             }
         }
     }
     
     private void deleteRelations() throws SQLException {
         
         StringBuffer buf = new StringBuffer("delete from RELATION where ");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0) buf.append(" or ");
             buf.append("PARENT_ID=");
             buf.append(delem_ids[i]);
         }
 
         log(buf.toString());
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(buf.toString());
         stmt.close();
     }
 
     private void deleteContents() throws SQLException {
         
         StringBuffer buf = new StringBuffer();
         buf.append("select CHILD_ID, CHILD_TYPE from CONTENT where PARENT_TYPE='elm' and (");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0) buf.append(" or ");
             buf.append("PARENT_ID=");
             buf.append(delem_ids[i]);
         }
         buf.append(")");
 
         log(buf.toString());
         
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(buf.toString());
         while (rs.next()){
             String contentID = rs.getString("CHILD_ID");
             String contentType = rs.getString("CHILD_TYPE");
             if (contentID != null && contentType != null){
                 buf = null;
                 if (contentType.equals("seq")){
                     buf = new StringBuffer("delete from SEQUENCE where SEQUENCE_ID=");
                     buf.append(contentID);
                 }
                 else if (contentType.equals("chc")){
                     buf = new StringBuffer("delete from CHOICE where CHOICE_ID=");
                     buf.append(contentID);
                 }
                 
                 if (buf == null) continue;
                 
                 stmt.executeUpdate(buf.toString());
             }
         }
         
         buf = new StringBuffer("delete from CONTENT where PARENT_TYPE='elm' and (");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0) buf.append(" or ");
             buf.append("PARENT_ID=");
             buf.append(delem_ids[i]);
         }
         buf.append(")");
         
         log(buf.toString());
         stmt.executeUpdate(buf.toString());
         stmt.close();
     }
     
     private void deleteFixedValues() throws Exception {
         
         StringBuffer buf = new StringBuffer().
         append("select distinct FXV_ID from FXV where ").
         append("OWNER_TYPE='elem' and (");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0)
                 buf.append(" or ");
             buf.append("OWNER_ID=");
             buf.append(delem_ids[i]);
         }
         buf.append(")");
         
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(buf.toString());
         Parameters pars = new Parameters();
         while (rs.next()){
             pars.addParameterValue("del_id", rs.getString("FXV_ID"));
         }
         stmt.close();
         
         pars.addParameterValue("mode", "delete");
         pars.addParameterValue("legal_delete", "true");
         FixedValuesHandler fvHandler = new FixedValuesHandler(conn, pars, ctx);
         fvHandler.setVersioning(versioning);
         fvHandler.execute();
     }
     
     private void deleteFkRelations() throws Exception{
 		StringBuffer buf = new StringBuffer();
 		buf.append("delete from FK_RELATION where ");
 		for (int i=0; i<delem_ids.length; i++){
 			if (i>0)
 				buf.append(" or ");
 			buf.append("A_ID=");
 			buf.append(delem_ids[i]);
 			buf.append(" or B_ID=");
 			buf.append(delem_ids[i]);
 		}
 		
 		Statement stmt = conn.createStatement();
 		stmt.executeUpdate(buf.toString());
 		stmt.close();
     }
     
     private void deleteCsItems() throws Exception {
 
         StringBuffer buf = new StringBuffer();
         buf.append("select distinct CSI_ID from CS_ITEM where CSI_TYPE='elem' and COMPONENT_TYPE='elem' and (");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0)
                 buf.append(" or ");
             buf.append("COMPONENT_ID=");
             buf.append(delem_ids[i]);
         }
         buf.append(")");
 
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(buf.toString());
         Parameters pars = new Parameters();
         while (rs.next()){
             pars.addParameterValue("del_id", rs.getString("CSI_ID"));
         }
         stmt.close();
 
         pars.addParameterValue("mode", "delete");
         CsItemHandler csiHandler = new CsItemHandler(conn, pars, ctx);
         csiHandler.setVersioning(this.versioning);
         csiHandler.execute();
     }
 
     private void insertTableElem() throws SQLException {
 
         if (table_id == null || table_id.length()==0)
             return;
         
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("TBL2ELEM");
         gen.setField("TABLE_ID", table_id);
         gen.setField("DATAELEM_ID", getLastInsertID());
         
         String position = req.getParameter("pos");
         if (Util.nullString(position))
             position = getTableElemPos();
 		    
         gen.setField("POSITION", position);
 
         String sql = gen.insertStatement();
         log(sql);
         
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(sql);
         stmt.close();
     }
     private String getTableElemPos() throws SQLException{
 
         StringBuffer buf = new StringBuffer().
 		append("select max(POSITION) from TBL2ELEM where TABLE_ID=").
         append(table_id);
 
         log(buf.toString());
 
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(buf.toString());
         rs.clearWarnings();
 
         String pos=null;
         if (rs.next())
             pos = rs.getString(1);
         stmt.close();
         if (pos != null){
             try {
               int i = Integer.parseInt(pos) + 1;
               return Integer.toString(i);
             }
             catch(Exception e){
                 return "1";
             }
         }
 
         return "1";
     }
     private void deleteTableElem() throws SQLException {
 
         if (delem_ids==null || delem_ids.length==0)
             return;
 
         StringBuffer buf = new StringBuffer("delete from TBL2ELEM where ");
         for (int i=0; i<delem_ids.length; i++){
             if (i>0)
                 buf.append(" or ");
             buf.append("DATAELEM_ID=");
             buf.append(delem_ids[i]);
         }
 
         log(buf.toString());
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(buf.toString());
         stmt.close();
     }
     private void processTableElems() throws Exception {
 
         String[] posIds = req.getParameterValues("pos_id");
         String old_pos=null;
         String pos=null;
         String parName=null;
         if (posIds==null || posIds.length==0) return;
         if (table_id==null || table_id.length()==0) return;
 
         for (int i=0; i<posIds.length; i++){
             old_pos = req.getParameter(OLDPOS_PREFIX + posIds[i]);
             pos = req.getParameter(POS_PREFIX + posIds[i]);
             if (old_pos.length()==0 || pos.length()==0)
                 continue;
             if (!old_pos.equals(pos))
                 updateTableElems(posIds[i], pos);
         }
     }
     private void updateTableElems(String elemId, String pos) throws Exception {
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("TBL2ELEM");
 
         gen.setField("POSITION", pos);
 
         StringBuffer sqlBuf = new StringBuffer(gen.updateStatement());
         sqlBuf.append(" where TABLE_ID=");
         sqlBuf.append(table_id);
         sqlBuf.append(" and DATAELEM_ID=");
         sqlBuf.append(elemId);
 
         log(sqlBuf.toString());
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(sqlBuf.toString());
         stmt.close();
     }
     
     private void processAttributes() throws Exception {
         String attrID=null;
         Enumeration parNames = req.getParameterNames();
 
         while (parNames.hasMoreElements()){
             String parName = (String)parNames.nextElement();
             if (parName.startsWith(ATTR_PREFIX) &&
                   !parName.startsWith(ATTR_MULT_PREFIX)){
               String attrValue = req.getParameter(parName);
               if (attrValue.length()==0)
                   continue;
               attrID = parName.substring(ATTR_PREFIX.length());
               if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null) continue;  //some attributes will be inherited from table level
               insertAttribute(attrID, attrValue);
             }
             else if(parName.startsWith(ATTR_MULT_PREFIX)){
               String[] attrValues = req.getParameterValues(parName);
               if (attrValues == null || attrValues.length == 0) continue;
 
               attrID = parName.substring(ATTR_MULT_PREFIX.length());
               if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null) continue;  //some attributes will be inherited from table level
 
               for (int i=0; i<attrValues.length; i++){
                   insertAttribute(attrID, attrValues[i]);
               }
             }
             else if (parName.startsWith(INHERIT_ATTR_PREFIX) &&
                   !parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
               attrID = parName.substring(INHERIT_ATTR_PREFIX.length());
               if (table_id==null) continue;
               CopyHandler ch = new CopyHandler(conn, ctx);
               ch.setUser(user);
               ch.copyAttribute(lastInsertID, table_id, "E", "T", attrID);
             }
             else if (parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
               attrID = parName.substring(INHERIT_COMPLEX_ATTR_PREFIX.length());
               if (table_id==null) continue;
               CopyHandler ch = new CopyHandler(conn, ctx);
 			  ch.setUser(user);
               ch.copyComplexAttrs(lastInsertID, table_id, "T", "E", attrID);
             }
         }
 
         // if there is a Datatype attribute and its value wasn't specified,
         // make it a string.
         if (!Util.nullString(mDatatypeID)){
             if (datatypeValue==null){
                 insertAttribute(mDatatypeID, "string");
             }
         }
     }
     
     private void insertAttribute(String attrId, String value) throws Exception {
     	
         // for CH1 certain attributes are not allowed
         if (type!=null && type.equals("CH1") && ch1ProhibitedAttrs.contains(attrId))
             return;
 
         // 'Datatype' attribute needs special handling
         if (mDatatypeID!=null && attrId.equals(mDatatypeID)){
             
             // a CH2 cannot be of 'boolean' datatype
             if (type!=null && type.equals("CH2"))
                 if (value.equalsIgnoreCase("boolean"))
                     throw new Exception("An element of CH2 type cannot be a boolean!");
             
             // make sure that the value matches fixed values for 'Datatype'
             // we can do this in insertAttribute() only, because the problem
             // comes from Import tool only.
             if (searchEngine==null) searchEngine = new DDSearchEngine(conn, "", ctx);
             Vector v = searchEngine.getFixedValues(attrId, "attr");
             boolean hasMatch = false;
             for (int i=0; v!=null && i<v.size(); i++){
                 FixedValue fxv = (FixedValue)v.get(i);
                 if (value.equals(fxv.getValue())){
                     hasMatch = true;
                     break;
                 }
             }
             
             if (!hasMatch)
                 throw new Exception("Unknown datatype for element " + idfier);
                 
             datatypeValue = value;
         }
                     
         
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("ATTRIBUTE");
 
         gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
         gen.setFieldExpr("DATAELEM_ID", lastInsertID);
         gen.setField("VALUE", value);
         gen.setField("PARENT_TYPE", "E");
 
         String sql = gen.insertStatement();
         log(sql);
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(sql);
         stmt.close();
     }
 
     private void updateAttribute(String attrId, String value) throws SQLException {
         
         SQLGenerator gen = new SQLGenerator();
         gen.setTable("ATTRIBUTE");
 
         gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
         gen.setFieldExpr("DATAELEM_ID", delem_id);
         gen.setFieldExpr("PARENT_TYPE", "E");
         gen.setField("VALUE", value);
 
         String sql = gen.updateStatement();
         log(sql);
 
         Statement stmt = conn.createStatement();
         stmt.executeUpdate(sql);
         stmt.close();
     }
     
     private void processDataClass() throws SQLException {
         
         StringBuffer buf = new StringBuffer();
         Statement stmt = conn.createStatement();
         if (!mode.equals("add")){
             
             buf.append("delete from CLASS2ELEM where DATAELEM_ID=");
             buf.append(delem_id);
             
             stmt.executeUpdate(buf.toString());
         }
         
         if (!mode.equals("del")){
             
             if (lastInsertID==null || delem_class == null || delem_class.length()==0) return;
             
             SQLGenerator gen = new SQLGenerator();
             gen.setTable("CLASS2ELEM");
             
             gen.setFieldExpr("DATAELEM_ID", lastInsertID);
             gen.setFieldExpr("DATACLASS_ID", delem_class);
         
             stmt.executeUpdate(gen.insertStatement());
         }
         
         stmt.close();
     }
     
     private void setLastInsertID() throws SQLException {
 
         String qry = "SELECT LAST_INSERT_ID()";
 
         log(qry);
 
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(qry);
         rs.clearWarnings();
         if (rs.next())
             lastInsertID = rs.getString(1);
         stmt.close();
     }
 
     public String getLastInsertID(){
         return lastInsertID;
     }
 
     public boolean exists() throws SQLException {
 
         // data element unique ID consists of SHORT_NAME and PARENT_NS
         StringBuffer buf = new StringBuffer();
         buf.append("select count(*) as COUNT from DATAELEM where IDENTIFIER=");
         buf.append(com.tee.util.Util.strLiteral(idfier));
         buf.append(" and PARENT_NS=");
         buf.append(com.tee.util.Util.strLiteral(ns_id));
         
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(buf.toString());
 
         if (rs.next()){
             if (rs.getInt("COUNT")>0){
                 return true;
             }
         }
 
         stmt.close();
 
         return false;
     }
     private void copyElem(String copyElemID) throws Exception{
 
         if (copyElemID==null) return;
 
         CopyHandler copier = new CopyHandler(conn, ctx);
 		copier.setUser(user);
         lastInsertID = copier.copyElem(copyElemID, false);
 
         if (lastInsertID==null)
             return;
 
         SQLGenerator gen = new SQLGenerator();
 
         gen.setTable("DATAELEM");
         gen.setField("IDENTIFIER", idfier);
         gen.setField("PARENT_NS", ns_id);
         gen.setField("VERSION", "1");
         if (versioning==false){
             if (user!=null && user.isAuthentic())
                 gen.setField("USER", user.getUserName());
         }
         else{
             gen.setField("WORKING_COPY", "Y");
             if (user!=null && user.isAuthentic())
                 gen.setField("WORKING_USER", user.getUserName());
         }
         gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
 
         String q = gen.updateStatement() + " where DATAELEM_ID=" + lastInsertID;
         log(q);
         conn.createStatement().executeUpdate(q);
 
         insertTableElem();
 
     }
 
     public boolean getCheckInResult(){
         return this.checkInResult;
     }
     
 	public String getRestoredID(){
 		return this.restoredID;
 	}
     
 	public static void main(String[] args){
         
 		try{
 			Class.forName("org.gjt.mm.mysql.Driver");
 			Connection conn =
 				DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
 
 			AppUserIF testUser = new TestUser();
 			testUser.authenticate("jaanus", "jaanus");
             
 			Parameters pars = new Parameters();
 			pars.addParameterValue("mode", "delete");
 			pars.addParameterValue("delem_id", "12158");
             
 			DataElementHandler handler =
 								new DataElementHandler(conn, pars, null);
 			handler.setUser(testUser);
 			handler.setVersioning(false);
 			handler.execute();
 	   }
 		catch (Exception e){
 			System.out.println(e.toString());
 			e.printStackTrace(new PrintStream(System.out));
 		}
         
 	}
 }
