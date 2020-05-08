 // Copyright ScenPro, Inc 2007
 
 // $Header:
 // /share/content/gforge/cdecurate/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/VMAction.java,v
 // 1.47 2008/12/26 19:14:35 chickerura Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import gov.nih.nci.cadsr.cdecurate.database.Alternates;
 import gov.nih.nci.cadsr.cdecurate.database.DBAccess;
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 import gov.nih.nci.cadsr.cdecurate.util.ToolException;
 
 import java.io.Serializable;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 
 import oracle.jdbc.driver.OracleTypes;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author shegde
  */
 public class VMAction implements Serializable
 {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger logger = Logger.getLogger(VMAction.class.getName());
 
 	private UtilService util = new UtilService();
 
 	/** constructor */
 	public VMAction()
 	{
 
 	}
 
 	// other public methods
 	/**
 	 * searching for Value Meaning in caDSR calls oracle stored procedure "{call
 	 * SBREXT_CDE_CURATOR_PKG.SEARCH_VM(InString, OracleTypes.CURSOR)}" loop
 	 * through the ResultSet and add them to bean which is added to the vector
 	 * to return
 	 * 
 	 * @param data
 	 *            VMForm object
 	 */
 	public void searchVMValues(VMForm data, String sRecordsDisplayed)
 	{
 
 		ResultSet rs = null;
 		CallableStatement cstmt = null;
 		try
 		{
 			if ((data.getSearchFilterID() != null) && !data.getSearchFilterID().equals("")){
             	int id = Integer.parseInt(data.getSearchFilterID());
              }
 			// do not continue search if no search filter
 			/*
 			 * if (data.getSearchFilterConName().equals("") &&
 			 * data.getSearchFilterID().equals("") &&
 			 * data.getSearchFilterCD().equals("") &&
 			 * data.getSearchFilterCondr().equals("") &&
 			 * data.getSearchFilterDef().equals("")) return;
 			 */
 			
 		
 			Vector<VM_Bean> vmList = data.getVMList();
 			if (vmList == null)
 				vmList = new Vector<VM_Bean>();
 			if (data.getCurationServlet().getConn() != null)
 			{
 				// parse the string.
 				String sDef = util.parsedStringSingleQuoteOracle(data.getSearchFilterDef());
 				String sTerm = util.parsedStringSingleQuoteOracle(data.getSearchTerm());
 				String sCon = util.parsedStringSingleQuoteOracle(data.getSearchFilterConName());
 
 				// cstmt =
 				// data.getCurationServlet().getConn().prepareCall("{call
 				// SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_VM(?,?,?,?,?,?,?)}");
 				cstmt =
 						data
 								.getCurationServlet()
 								.getConn()
 								.prepareCall(
 										"{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_VM(?,?,?,?,?,?,?,?,?,?)}");
 				// Now tie the placeholders for out parameters.
 				cstmt.registerOutParameter(5, OracleTypes.CURSOR);
 				// Now tie the placeholders for In parameters.
 				cstmt.setString(1, sTerm); // name
 				cstmt.setString(2, data.getSearchFilterCD());
 				cstmt.setString(3, sDef);
 				cstmt.setString(4, data.getSearchFilterCondr());
 				cstmt.setString(6, sCon);
 				cstmt.setString(7, data.getSearchFilterCondr());
 				cstmt.setString(8, data.getSearchFilterID());
 				cstmt.setString(9, data.getVersionInd());
 				cstmt.setDouble(10, data.getVersionNumber());
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				// store the output in the resultset
 				rs = (ResultSet) cstmt.getObject(5);
 
 				if (rs != null)
 				{
 					int g = 0;
 					int recordsDisplayed = GetACSearch.getInt(sRecordsDisplayed);
 					// loop through the resultSet and add them to the bean
 					while (rs.next() && g < recordsDisplayed)
 					{
 						g = g + 1;
 						VM_Bean vmBean = doSetVMAttributes(rs, data.getCurationServlet().getConn());
 						vmBean.setVM_BEGIN_DATE(rs.getString("begin_date"));
 						vmBean.setVM_END_DATE(rs.getString("end_date"));
 						vmBean.setVM_CD_NAME(rs.getString("cd_name"));
 						vmList.addElement(vmBean); // add the bean to a vector
 					} // END WHILE
 					if (g == recordsDisplayed){
                     	int totalRecords = getResultSetSize(rs);
                     	DataManager.setAttribute(data.getRequest().getSession(), "totalRecords", Integer.toString(totalRecords));
                     } else {
                     	//TBD - NPE
//                    	if(data.getRequest() != null && data.getRequest().getSession() != null) {
                    		DataManager.setAttribute(data.getRequest().getSession(), "totalRecords", Integer.toString(g));
//                    	}
                     }
 				} // END IF
 			}
 			data.setVMList(vmList);
 		}
 		catch (NumberFormatException e){}
 		catch (Exception e)
 		{
 			logger.error("ERROR - VMAction-searchVM for other : " + e.toString(), e);
 			data.setStatusMsg(data.getStatusMsg() + "\\tError : Unable to search VM."
 					+ e.toString());
 			data.setActionStatus(VMForm.ACTION_STATUS_FAIL);
 		}
 		finally
 		{
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 	} // endVM search
 
 	/**
 	 * To get final result vector of selected attributes/rows to display for
 	 * Permissible Values component, called from getACKeywordResult,
 	 * getACSortedResult and getACShowResult methods. gets the selected
 	 * attributes from session vector 'selectedAttr'. loops through the VMBean
 	 * vector 'vACSearch' and adds the selected fields to result vector.
 	 * 
 	 * @param data
 	 *            VMForm object
 	 */
 	public void getVMResult(VMForm data, HttpServletRequest httpReq, Vector<String> vResult,
 			GetACSearch getac)
 	{
 
 		try
 		{
 			String menuAction =
 					(String) httpReq.getSession().getAttribute(Session_Data.SESSION_MENU_ACTION);
 			Vector vVM = (Vector) httpReq.getSession().getAttribute("vACSearch");
 			// get number of records
 			Vector vRSel = data.getVMList(); // (Vector)session.getAttribute("vACSearch");
 			if (vRSel == null)
 				vRSel = new Vector();
 			Integer iRecs = new Integer(0);
 			if (vRSel.size() > 0)
 				iRecs = new Integer(vRSel.size());
 			String sRecs = "";
 			if (iRecs != null)
 				sRecs = iRecs.toString();
 			data.setNumRecFound(sRecs);
 			httpReq.getSession().setAttribute("recsFound", data.getNumRecFound());
 			
 
 			// make keyWordLabel label request session
 			String sKeyword = "";
 			sKeyword = data.getSearchTerm(); // (String)session.getAttribute("creKeyword");
 			if (sKeyword == null)
 				sKeyword = "";
 			data.setResultLabel("Value Meaning : " + sKeyword); // req.setAttribute("labelKeyword",
 																// "Value
 																// Meaning : " +
 																// sKeyword);
 																// //make the
 																// label
 
 			// loop through the bean collection to add it to the result vector
 			Vector<String> vSelAttr = data.getSelAttrList(); // (Vector)session.getAttribute("creSelectedAttr");
 			// Vector<String> vResult = new Vector<String>();
 
 			Vector<String> vSearchID = new Vector<String>();
 			Vector<String> vSearchName = new Vector<String>();
 			Vector<String> vSearchLongName = new Vector<String>();
 			Vector<String> vSearchASL = new Vector<String>();
 			Vector<String> vSearchDefinition = new Vector<String>();
 			Vector<String> vUsedContext = new Vector<String>();
 			for (int i = 0; i < (vRSel.size()); i++)
 			{
 				VM_Bean VMBean = new VM_Bean();
 				VMBean = (VM_Bean) vRSel.elementAt(i);
 				Vector<EVS_Bean> vcon = VMBean.getVM_CONCEPT_LIST();
 				String conID = "", defsrc = "", vocab = "";
 				vSearchID.addElement(VMBean.getVM_IDSEQ());
 				vSearchName.addElement(VMBean.getVM_LONG_NAME());
 				vSearchLongName.addElement(VMBean.getVM_LONG_NAME());
 				vSearchDefinition.addElement(VMBean.getVM_PREFERRED_DEFINITION());
 				vUsedContext.addElement(VMBean.getContextName());
 				for (int j = 0; j < vcon.size(); j++)
 				{
 					EVS_Bean con = (EVS_Bean) vcon.elementAt(j);
 					if (!conID.equals(""))
 						conID += ": ";
 					conID += con.getCONCEPT_IDENTIFIER();
 					if (!defsrc.equals(""))
 						defsrc += ": ";
 					defsrc += con.getEVS_DEF_SOURCE();
 					if (!vocab.equals(""))
 						vocab += ": ";
 					vocab += con.getEVS_DATABASE();
 				}
 				// they have to be in the order of attribute multi select list
 				EVS_Bean vmConcept = VMBean.getVM_CONCEPT();
 				if (vmConcept == null)
 					vmConcept = new EVS_Bean();
 				// if (vSelAttr.contains("Value Meaning"))
 				// vResult.addElement(VMBean.getVM_SHORT_MEANING());
 				// if (vSelAttr.contains("Meaning Description"))
 				// vResult.addElement(VMBean.getVM_DESCRIPTION());
 				if (vSelAttr.contains("Long Name"))
 					vResult.addElement(VMBean.getVM_LONG_NAME());
 				if (vSelAttr.contains("Public ID"))
 					vResult.addElement(VMBean.getVM_ID());
 				if (vSelAttr.contains("Version"))
 					vResult.addElement(VMBean.getVM_VERSION());
 				if (vSelAttr.contains("Workflow Status"))
 					vResult.addElement(VMBean.getASL_NAME());
 				if (vSelAttr.contains("EVS Identifier"))
 					vResult.addElement(conID); // vmConcept.getCONCEPT_IDENTIFIER());
 				// if (vSelAttr.contains("Conceptual Domain"))
 				// vResult.addElement(VMBean.getVM_CD_NAME());
 				if (vSelAttr.contains("Conceptual Domain"))
 					vResult =
 							addMultiRecordConDomain(VMBean.getVM_CD_NAME(), VMBean
 									.getVM_LONG_NAME(), vResult, httpReq, data);
 				if (vSelAttr.contains("Definition"))
 					vResult.addElement(VMBean.getVM_PREFERRED_DEFINITION());
 				if (vSelAttr.contains("Definition Source"))
 					vResult.addElement(defsrc); // vmConcept.getEVS_DEF_SOURCE());
 				if (vSelAttr.contains("Vocabulary"))
 					vResult.addElement(vocab); // vmConcept.getEVS_DATABASE());
 				if (vSelAttr.contains("Comments"))
 					vResult.addElement(VMBean.getVM_CHANGE_NOTE());
 				if (vSelAttr.contains("Effective Begin Date"))
 					vResult.addElement(VMBean.getVM_BEGIN_DATE());
 				if (vSelAttr.contains("Effective End Date"))
 					vResult.addElement(VMBean.getVM_END_DATE());
 			}
 			DataManager.setAttribute(httpReq.getSession(), "SearchID", vSearchID);
 			DataManager.setAttribute(httpReq.getSession(), "SearchName", vSearchName);
 			DataManager.setAttribute(httpReq.getSession(), "SearchLongName", vSearchLongName);
 			DataManager.setAttribute(httpReq.getSession(), "SearchASL", vSearchASL);
 			DataManager.setAttribute(httpReq.getSession(), "SearchDefinitionAC", vSearchDefinition);
 			DataManager.setAttribute(httpReq.getSession(), "SearchUsedContext", vUsedContext);
 			data.setResultList(vResult);
 			if (!menuAction.equals("searchForCreate"))
 				getac.stackSearchComponents("ValueMeaning", vVM, vRSel, vSearchID, vSearchName,
 						vResult, vSearchASL, vSearchLongName);
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in VMAction-getVMResult : " + e.toString(), e);
 			data.setStatusMsg(data.getStatusMsg() + "\\tError : Unable to search VM."
 					+ e.toString());
 			data.setActionStatus(VMForm.ACTION_STATUS_FAIL);
 		}
 	}
 
 	/**
 	 * To get final result vector of selected attributes/rows to display for
 	 * Permissible Values component, called from getACKeywordResult,
 	 * getACSortedResult and getACShowResult methods. gets the selected
 	 * attributes from session vector 'selectedAttr'. loops through the VMBean
 	 * vector 'vACSearch' and adds the selected fields to result vector.
 	 * 
 	 * @param data
 	 *            VMForm object
 	 */
 	public void getVMResult(VMForm data, HttpServletRequest httpReq, GetACSearch getac)
 	{
 
 		try
 		{
 			String menuAction =
 					(String) httpReq.getSession().getAttribute(Session_Data.SESSION_MENU_ACTION);
 			Vector vVM = (Vector) httpReq.getSession().getAttribute("vACSearch");
 			// get number of records
 			Vector vRSel = data.getVMList(); // (Vector)session.getAttribute("vACSearch");
 			if (vRSel == null)
 				vRSel = new Vector();
 			Integer iRecs = new Integer(0);
 			if (vRSel.size() > 0)
 				iRecs = new Integer(vRSel.size());
 			String sRecs = "";
 			if (iRecs != null)
 				sRecs = iRecs.toString();
 			data.setNumRecFound(sRecs);
 
 			// make keyWordLabel label request session
 			String sKeyword = "";
 			sKeyword = data.getSearchTerm(); // (String)session.getAttribute("creKeyword");
 			if (sKeyword == null)
 				sKeyword = "";
 			data.setResultLabel("Value Meaning : " + sKeyword); // req.setAttribute("labelKeyword",
 																// "Value
 																// Meaning : " +
 																// sKeyword);
 																// //make the
 																// label
 
 			// loop through the bean collection to add it to the result vector
 			Vector<String> vSelAttr = data.getSelAttrList(); // (Vector)session.getAttribute("creSelectedAttr");
 			Vector<String> vResult = new Vector<String>();
 
 			Vector<String> vSearchID = new Vector<String>();
 			Vector<String> vSearchName = new Vector<String>();
 			Vector<String> vSearchLongName = new Vector<String>();
 			Vector<String> vSearchASL = new Vector<String>();
 			Vector<String> vSearchDefinition = new Vector<String>();
 			Vector<String> vUsedContext = new Vector<String>();
 			for (int i = 0; i < (vRSel.size()); i++)
 			{
 				VM_Bean VMBean = new VM_Bean();
 				VMBean = (VM_Bean) vRSel.elementAt(i);
 				Vector<EVS_Bean> vcon = VMBean.getVM_CONCEPT_LIST();
 				String conID = "", defsrc = "", vocab = "";
 				vSearchID.addElement(VMBean.getVM_IDSEQ());
 				vSearchName.addElement(VMBean.getVM_LONG_NAME());
 				vSearchLongName.addElement(VMBean.getVM_LONG_NAME());
 				vSearchDefinition.addElement(VMBean.getVM_PREFERRED_DEFINITION());
 				vUsedContext.addElement(VMBean.getContextName());
 				for (int j = 0; j < vcon.size(); j++)
 				{
 					EVS_Bean con = (EVS_Bean) vcon.elementAt(j);
 					if (!conID.equals(""))
 						conID += ": ";
 					conID += con.getCONCEPT_IDENTIFIER();
 					if (!defsrc.equals(""))
 						defsrc += ": ";
 					defsrc += con.getEVS_DEF_SOURCE();
 					if (!vocab.equals(""))
 						vocab += ": ";
 					vocab += con.getEVS_DATABASE();
 				}
 				// they have to be in the order of attribute multi select list
 				EVS_Bean vmConcept = VMBean.getVM_CONCEPT();
 				if (vmConcept == null)
 					vmConcept = new EVS_Bean();
 				// if (vSelAttr.contains("Value Meaning"))
 				// vResult.addElement(VMBean.getVM_SHORT_MEANING());
 				// if (vSelAttr.contains("Meaning Description"))
 				// vResult.addElement(VMBean.getVM_DESCRIPTION());
 				if (vSelAttr.contains("Long Name"))
 					vResult.addElement(VMBean.getVM_LONG_NAME());
 				if (vSelAttr.contains("Public ID"))
 					vResult.addElement(VMBean.getVM_ID());
 				if (vSelAttr.contains("Version"))
 					vResult.addElement(VMBean.getVM_VERSION());
 				if (vSelAttr.contains("Workflow Status"))
 					vResult.addElement(VMBean.getASL_NAME());
 				if (vSelAttr.contains("EVS Identifier"))
 					vResult.addElement(conID); // vmConcept.getCONCEPT_IDENTIFIER());
 				// if (vSelAttr.contains("Conceptual Domain"))
 				// vResult.addElement(VMBean.getVM_CD_NAME());
 				if (vSelAttr.contains("Conceptual Domain"))
 					vResult =
 							addMultiRecordConDomain(VMBean.getVM_CD_NAME(), VMBean
 									.getVM_LONG_NAME(), vResult, httpReq, data);
 				if (vSelAttr.contains("Definition"))
 					vResult.addElement(VMBean.getVM_PREFERRED_DEFINITION());
 				if (vSelAttr.contains("Definition Source"))
 					vResult.addElement(defsrc); // vmConcept.getEVS_DEF_SOURCE());
 				if (vSelAttr.contains("Vocabulary"))
 					vResult.addElement(vocab); // vmConcept.getEVS_DATABASE());
 				if (vSelAttr.contains("Comments"))
 					vResult.addElement(VMBean.getVM_CHANGE_NOTE());
 				if (vSelAttr.contains("Effective Begin Date"))
 					vResult.addElement(VMBean.getVM_BEGIN_DATE());
 				if (vSelAttr.contains("Effective End Date"))
 					vResult.addElement(VMBean.getVM_END_DATE());
 			}
 			DataManager.setAttribute(httpReq.getSession(), "SearchID", vSearchID);
 			DataManager.setAttribute(httpReq.getSession(), "SearchName", vSearchName);
 			DataManager.setAttribute(httpReq.getSession(), "SearchLongName", vSearchLongName);
 			DataManager.setAttribute(httpReq.getSession(), "SearchASL", vSearchASL);
 			DataManager.setAttribute(httpReq.getSession(), "SearchDefinitionAC", vSearchDefinition);
 			DataManager.setAttribute(httpReq.getSession(), "SearchUsedContext", vUsedContext);
 			data.setResultList(vResult);
 			if (!menuAction.equals("searchForCreate"))
 				getac.stackSearchComponents("ValueMeaning", vVM, vRSel, vSearchID, vSearchName,
 						vResult, vSearchASL, vSearchLongName);
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in VMAction-getVMResult : " + e.toString(), e);
 			data.setStatusMsg(data.getStatusMsg() + "\\tError : Unable to search VM."
 					+ e.toString());
 			data.setActionStatus(VMForm.ACTION_STATUS_FAIL);
 		}
 	}
 
 	/**
 	 * To get final result vector of selected attributes/rows to display for
 	 * Permissible Values component, called from getACKeywordResult,
 	 * getACSortedResult and getACShowResult methods. gets the selected
 	 * attributes from session vector 'selectedAttr'. loops through the VMBean
 	 * vector 'vACSearch' and adds the selected fields to result vector.
 	 * 
 	 * @param data
 	 *            VMForm object
 	 */
 	public void getVMResult(VMForm data, HttpServletRequest httpReq)
 	{
 
 		try
 		{
 			// get number of records
 			Vector vRSel = data.getVMList(); // (Vector)session.getAttribute("vACSearch");
 			if (vRSel == null)
 				vRSel = new Vector();
 			Integer iRecs = new Integer(0);
 			if (vRSel.size() > 0)
 				iRecs = new Integer(vRSel.size());
 			String sRecs = "";
 			if (iRecs != null)
 				sRecs = iRecs.toString();
 			data.setNumRecFound(sRecs);
 
 			// make keyWordLabel label request session
 			String sKeyword = "";
 			sKeyword = data.getSearchTerm(); // (String)session.getAttribute("creKeyword");
 			if (sKeyword == null)
 				sKeyword = "";
 			data.setResultLabel("Value Meaning : " + sKeyword); // req.setAttribute("labelKeyword",
 																// "Value
 																// Meaning : " +
 																// sKeyword);
 																// //make the
 																// label
 
 			// loop through the bean collection to add it to the result vector
 			Vector<String> vSelAttr = data.getSelAttrList(); // (Vector)session.getAttribute("creSelectedAttr");
 			Vector<String> vResult = new Vector<String>();
 
 			Vector<String> vSearchID = new Vector<String>();
 			Vector<String> vSearchName = new Vector<String>();
 			Vector<String> vSearchLongName = new Vector<String>();
 			Vector<String> vSearchASL = new Vector<String>();
 			Vector<String> vSearchDefinition = new Vector<String>();
 			Vector<String> vUsedContext = new Vector<String>();
 			for (int i = 0; i < (vRSel.size()); i++)
 			{
 				VM_Bean VMBean = new VM_Bean();
 				VMBean = (VM_Bean) vRSel.elementAt(i);
 				Vector<EVS_Bean> vcon = VMBean.getVM_CONCEPT_LIST();
 				String conID = "", defsrc = "", vocab = "";
 				vSearchID.addElement(VMBean.getVM_IDSEQ());
 				vSearchName.addElement(VMBean.getVM_LONG_NAME());
 				vSearchLongName.addElement(VMBean.getVM_LONG_NAME());
 				vSearchDefinition.addElement(VMBean.getVM_PREFERRED_DEFINITION());
 				vUsedContext.addElement(VMBean.getContextName());
 				for (int j = 0; j < vcon.size(); j++)
 				{
 					EVS_Bean con = (EVS_Bean) vcon.elementAt(j);
 					if (!conID.equals(""))
 						conID += ": ";
 					conID += con.getCONCEPT_IDENTIFIER();
 					if (!defsrc.equals(""))
 						defsrc += ": ";
 					defsrc += con.getEVS_DEF_SOURCE();
 					if (!vocab.equals(""))
 						vocab += ": ";
 					vocab += con.getEVS_DATABASE();
 				}
 				// they have to be in the order of attribute multi select list
 				EVS_Bean vmConcept = VMBean.getVM_CONCEPT();
 				if (vmConcept == null)
 					vmConcept = new EVS_Bean();
 				// if (vSelAttr.contains("Value Meaning"))
 				// vResult.addElement(VMBean.getVM_SHORT_MEANING());
 				// if (vSelAttr.contains("Meaning Description"))
 				// vResult.addElement(VMBean.getVM_DESCRIPTION());
 				if (vSelAttr.contains("Long Name"))
 					vResult.addElement(VMBean.getVM_LONG_NAME());
 				if (vSelAttr.contains("Public ID"))
 					vResult.addElement(VMBean.getVM_ID());
 				if (vSelAttr.contains("Version"))
 					vResult.addElement(VMBean.getVM_VERSION());
 				if (vSelAttr.contains("Workflow Status"))
 					vResult.addElement(VMBean.getASL_NAME());
 				if (vSelAttr.contains("EVS Identifier"))
 					vResult.addElement(conID); // vmConcept.getCONCEPT_IDENTIFIER());
 				// if (vSelAttr.contains("Conceptual Domain"))
 				// vResult.addElement(VMBean.getVM_CD_NAME());
 				if (vSelAttr.contains("Conceptual Domain"))
 					vResult =
 							addMultiRecordConDomain(VMBean.getVM_CD_NAME(), VMBean
 									.getVM_LONG_NAME(), vResult, httpReq, data);
 				if (vSelAttr.contains("Definition"))
 					vResult.addElement(VMBean.getVM_PREFERRED_DEFINITION());
 				if (vSelAttr.contains("Definition Source"))
 					vResult.addElement(defsrc); // vmConcept.getEVS_DEF_SOURCE());
 				if (vSelAttr.contains("Vocabulary"))
 					vResult.addElement(vocab); // vmConcept.getEVS_DATABASE());
 				if (vSelAttr.contains("Comments"))
 					vResult.addElement(VMBean.getVM_CHANGE_NOTE());
 				if (vSelAttr.contains("Effective Begin Date"))
 					vResult.addElement(VMBean.getVM_BEGIN_DATE());
 				if (vSelAttr.contains("Effective End Date"))
 					vResult.addElement(VMBean.getVM_END_DATE());
 			}
 			DataManager.setAttribute(httpReq.getSession(), "SearchID", vSearchID);
 			DataManager.setAttribute(httpReq.getSession(), "SearchName", vSearchName);
 			DataManager.setAttribute(httpReq.getSession(), "SearchLongName", vSearchLongName);
 			DataManager.setAttribute(httpReq.getSession(), "SearchASL", vSearchASL);
 			DataManager.setAttribute(httpReq.getSession(), "SearchDefinitionAC", vSearchDefinition);
 			DataManager.setAttribute(httpReq.getSession(), "SearchUsedContext", vUsedContext);
 			data.setResultList(vResult);
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in VMAction-getVMResult : " + e.toString(), e);
 			data.setStatusMsg(data.getStatusMsg() + "\\tError : Unable to search VM."
 					+ e.toString());
 			data.setActionStatus(VMForm.ACTION_STATUS_FAIL);
 		}
 	}
 
 	/**
 	 * To get the sorted vector for the selected field in the VM component,
 	 * called from getACSortedResult. gets the 'sortType' from request and
 	 * 'vSelRows' vector from session. calls 'getVMFieldValue' to extract the
 	 * value from the bean for the selected sortType. modified bubble sort
 	 * method to sort beans in the vector using the strings compareToIgnoreCase
 	 * method. adds the sorted bean to a vector 'vSelRows'.
 	 * 
 	 * @param data
 	 *            VM_Form object
 	 */
 	public void getVMSortedRows(VMForm data)
 	{
 
 		try
 		{
 			Vector<VM_Bean> vSRows = new Vector<VM_Bean>();
 			Vector<VM_Bean> vSortedRows = new Vector<VM_Bean>();
 			// get the selected rows
 			vSRows = data.getVMList();
 			String sortField = data.getSortField();
 			if (sortField != null)
 			{
 				// check if the vector has the data
 				if (vSRows.size() > 0)
 				{
 					// loop through the vector to get the bean row
 					for (int i = 0; i < (vSRows.size()); i++)
 					{
 						VM_Bean VMSortBean1 = (VM_Bean) vSRows.elementAt(i);
 						String Name1 = getVMFieldValue(VMSortBean1, sortField);
 						int tempInd = i;
 						VM_Bean tempBean = VMSortBean1;
 						String tempName = Name1;
 						// loop through again to get the next bean in the vector
 						for (int j = i + 1; j < (vSRows.size()); j++)
 						{
 							VM_Bean VMSortBean2 = (VM_Bean) vSRows.elementAt(j);
 							String Name2 = getVMFieldValue(VMSortBean2, sortField);
 							try
 							{
 								// UtilService util = data.getUtil();
 								if (util == null)
 									util = new UtilService();
 								if (util.ComparedValue("String", Name1, Name2) > 0)
 								{
 									if (tempInd == i)
 									{
 										tempName = Name2;
 										tempBean = VMSortBean2;
 										tempInd = j;
 									}
 									// else if
 									// (tempName.compareToIgnoreCase(Name2) > 0)
 									else if (util.ComparedValue("String", tempName, Name2) > 0)
 									{
 										tempName = Name2;
 										tempBean = VMSortBean2;
 										tempInd = j;
 									}
 								}
 							}
 							catch (RuntimeException e)
 							{
 								logger.error("Error - Compare Value in Value Meaning sort", e);
 							}
 						}
 						vSRows.removeElementAt(tempInd);
 						vSRows.insertElementAt(VMSortBean1, tempInd);
 						vSortedRows.addElement(tempBean); // add the temp bean
 															// to a vector
 					}
 					data.setVMList(vSortedRows);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in VMAction-VMsortedRows : ", e);
 			data.setStatusMsg(data.getStatusMsg() + "\\tError : Unable to search VM."
 					+ e.toString());
 			data.setActionStatus(VMForm.ACTION_STATUS_FAIL);
 		}
 	}
 
 	/**
 	 * appends the Value meaning selected from the VM search result
 	 * 
 	 * @param selRows
 	 *            String array of the selected rows
 	 * @param vRSel
 	 *            vector VM Bean of the search results
 	 * @param pv
 	 *            PVBean object of the selected row
 	 */
 	public void doAppendSelectVM(String[] selRows, Vector<VM_Bean> vRSel, PV_Bean pv)
 	{
 
 		String errMsg = "";
 		// loop through the array of strings
 		for (int i = 0; i < selRows.length; i++)
 		{
 			String thisRow = selRows[i];
 			Integer IRow = new Integer(thisRow);
 			int iRow = IRow.intValue();
 			if (iRow < 0 || iRow > vRSel.size())
 				errMsg += "Row size is either too big or too small.";
 			else
 			{
 				VM_Bean vm = (VM_Bean) vRSel.elementAt(iRow);
 				// if (vm != null && vm.getVM_SHORT_MEANING() != null)
 				if (vm != null && vm.getVM_LONG_NAME() != null)
 					pv.setPV_VM(vm);
 			}
 		}
 		// log the errr message
 		if (!errMsg.equals(""))
 			logger.error("Error msg : doAppendSelectVM " + errMsg);
 	}
 
 	/**
 	 * saves and marks the VM changes to store it in the database
 	 * 
 	 * @param pv
 	 *            PVBean object
 	 * @param vd
 	 *            VDBean object
 	 * @param data
 	 *            VMForm object
 	 */
 	public void setDataForCreate(PV_Bean pv, VD_Bean vd, VMForm data)
 	{
 
 		VM_Bean vm = data.getVMBean();
 		// VM_Bean selvm = data.getSelectVM();
 		// boolean handTypedVM = true;
 		// Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
 		vm.setVM_CD_IDSEQ(vd.getVD_CD_IDSEQ());
 		vm.setVM_CD_NAME(vd.getVD_CD_NAME());
 		vm.setVM_BEGIN_DATE(pv.getPV_BEGIN_DATE()); // vm begin date
 		vm.setVM_END_DATE(pv.getPV_END_DATE()); // vm end date
 		// call the action change VM to validate the vm
 		data.setVMBean(vm);
 		// if (vm.getVM_IDSEQ() == null || vm.getVM_IDSEQ().equals(""))
 		// this.doChangeVM(data);
 		VM_Bean exVM = validateVMData(data);
 		if (exVM == null)
 		{
 			vm.setVM_IDSEQ("");
 			vm.setVM_SUBMIT_ACTION(data.CADSR_ACTION_INS);
 		}
 	}
 
 	/**
 	 * to submit the VM changes to the database. checks if already exists in the
 	 * database. gets concept id to associates, creates concept or non concept
 	 * VM creates cd relationship
 	 * 
 	 * @param data
 	 *            VMForm object
 	 * @return String error message
 	 */
 	public String doSubmitVM(VMForm data)
 	{
 
 		String erMsg = "";
 		String returnCode = null;
 		VM_Bean vm = data.getVMBean();
 		String sAct = vm.getVM_SUBMIT_ACTION();
 		if (!sAct.equals("") && !sAct.equals(VMForm.CADSR_ACTION_NONE))
 		{
 
 			if (vm.isNewAC())
 			{
 				returnCode = this.setNewVersionVM(data);
 				if (returnCode == null)
 				{
 					vm.setVM_SUBMIT_ACTION(VMForm.CADSR_ACTION_UPD);
 				}
 			}
 
 			// check if vm exists for INS action (not sure if needed or what to
 			// do later)
 			data.setStatusMsg("");
 			String ret = "";
 			// check the condition whether changing vm with concept or not
 			Vector<EVS_Bean> vCon = vm.getVM_CONCEPT_LIST(); // get the con
 																// list first
 			if (vCon != null && vCon.size() > 0)
 			{
 				// set concept (get concept will be done at the time of
 				// selection itself
 				ConceptAction conAct = new ConceptAction();
 				ConceptForm condata = new ConceptForm();
 				condata.setCurationServlet(data.getCurationServlet());
 				if (data.getCurationServlet().getConn() != null)
 					condata.setDBConnection(data.getCurationServlet().getConn()); // get
 																					// the
 																					// connection
 				else
 					condata.setDBConnection(data.getCurationServlet().getConn()); // VMServlet.makeDBConnection());
 																					// //make
 																					// the
 																					// connection
 
 				String conArray = conAct.getConArray(vCon, true, condata);
 				// append the concept message
 				if (!condata.getStatusMsg().equals(""))
 					erMsg += condata.getStatusMsg();
 				// set setvm_evs method to set th edata
 				ret = this.setVM(data, conArray);
 			}
 			else {
 				//Remove old condr if it exists first, then continue on with the upd/insert function
 				ret = this.removeVMCondr(data);
 				if (ret != null && !ret.equals(""))
 					ret += "\\n" + ret;
 				
 				ret = this.setVM(data, null); // update or insert non evs vm
 			}
 			
 			if (ret != null && !ret.equals(""))
 				erMsg += "\\n" + ret;
 
 			// exit if error occurred
 			if (erMsg == null || erMsg.equals(""))
 			{
 				// create cdvms relationship
 				if (!vm.getVM_CD_IDSEQ().equals(""))
 					this.setCDVMS(data, vm);
 				// reset back to none after successful submission
 				vm.setVM_SUBMIT_ACTION(VMForm.CADSR_ACTION_NONE);
 			}
 		}
 		return erMsg;
 	}
 
 	/**
 	 * Makes the name for VM from VM concepts
 	 * 
 	 * @param vm
 	 *            VMBEan object
 	 * @param iFrom
 	 *            int value of which page teh action going to be
 	 */
 	public void makeVMNameFromConcept(VM_Bean vm, int iFrom)
 	{
 
 		Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
 		String vmName = "";
 		String vmDef = "";
 		for (int i = 0; i < vmCon.size(); i++)
 		{
 			EVS_Bean con = vmCon.elementAt(i);
 			if (!con.getNVP_CONCEPT_VALUE().equals(""))
 			{
 				String conName = con.getLONG_NAME();
 				String conDef = con.getPREFERRED_DEFINITION();
 				// String conExp = "::" + con.getNVP_CONCEPT_VALUE();
 				int nvpInd = conName.indexOf("::");
 				if (nvpInd > 0 && i == vmCon.size() - 1) // last one in the
 															// list, remove con
 															// value from the
 															// name
 				{
 					conName = conName.substring(0, nvpInd);
 					// con.setNVP_CONCEPT_VALUE(""); //NOte - do not remove
 					int nvpDefInd = conDef.indexOf("::");
 					if (nvpDefInd > 0)
 						conDef = conDef.substring(0, nvpDefInd);
 				}
 				else if (nvpInd < 1 && i < vmCon.size() - 1) // put back the
 																// concept value
 																// in the name
 																// and def
 				{
 					String sNVP = con.getNVP_CONCEPT_VALUE();
 					conName = conName + "::" + sNVP;
 					conDef = conDef + "::" + sNVP;
 				}
 				con.setLONG_NAME(conName);
 				con.setPREFERRED_DEFINITION(conDef);
 				vmCon.setElementAt(con, i);
 			}
 			if (!vmName.equals(""))
 				vmName += " ";
 			vmName += con.getLONG_NAME();
 			if (!vmDef.equals(""))
 				vmDef += ": ";
 			vmDef += con.getPREFERRED_DEFINITION();
 		}
 		// change the name only from new vm
 		switch (iFrom)
 		{
 		case ConceptForm.FOR_PV_PAGE_CONCEPT:
 			// vm.setVM_DESCRIPTION(vmDef);
 			vm.setVM_PREFERRED_DEFINITION(vmDef);
 			// String curName = vm.getVM_SHORT_MEANING();
 			String curName = vm.getVM_LONG_NAME();
 			if (!curName.equalsIgnoreCase(vmName))
 			{
 				vm.setVM_LONG_NAME(vmName);
 				// vm.setVM_SHORT_MEANING(vmName);
 				vm.setVM_LONG_NAME(vmName);
 				vm.setVM_SUBMIT_ACTION(VMForm.CADSR_ACTION_INS);
 			}
 			break;
 		case ConceptForm.FOR_VM_PAGE_CONCEPT:
 			// add the vm definition to manual defintion if it is empty
 			if (vm.getVM_ALT_DEFINITION().equals(""))
 				// vm.setVM_ALT_DEFINITION(vm.getVM_DESCRIPTION());
 				vm.setVM_ALT_DEFINITION(vm.getVM_PREFERRED_DEFINITION());
 			// add name and description
 			vm.setVM_ALT_NAME(vmName);
 			// vm.setVM_DESCRIPTION(vmDef);
 			vm.setVM_PREFERRED_DEFINITION(vmDef);
 			break;
 		case ConceptForm.FOR_VM_PAGE_OPEN:
 			vm.setVM_ALT_NAME(vmName);
 			break;
 		}
 	}
 
 	/**
 	 * resets the vm concepts from page
 	 * 
 	 * @param sCons
 	 *            list of concept names from teh page
 	 * @param vm
 	 *            VMBean selected value meaning
 	 * @param pv
 	 *            PVBean selected permissible value
 	 */
 	public void resetVMConcepts(String[] sCons, VM_Bean vm, PV_Bean pv)
 	{
 
 		Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
 		if (sCons.length != vmCon.size())
 		{
 			Vector<EVS_Bean> newList = new Vector<EVS_Bean>();
 			// remove the deleted concepts if not the same size
 			for (int i = 0; i < sCons.length; i++)
 			{
 				String sID = realTrim(sCons[i]);  //String.trim() does not work on all whitespace characters
 				for (int j = 0; j < vmCon.size(); j++)
 				{
 					EVS_Bean eBean = (EVS_Bean) vmCon.elementAt(j);
 					String conId = eBean.getCONCEPT_IDENTIFIER().trim();
 					if (sID.equalsIgnoreCase(conId))
 					{
 						newList.addElement(eBean);
 						break;
 					}
 				}
 			}
 			vm.setVM_CONCEPT_LIST(newList);
 			pv.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_UPD); // PVForm.CADSR_ACTION_INS);
 																// //pv changed
 		}
 		// get the name
 		this.makeVMNameFromConcept(vm, ConceptForm.FOR_PV_PAGE_CONCEPT);
 	}
 
 	private String realTrim(String toTrim) {
 		
 		int front = 0;
 		int back = toTrim.length();
 		if (toTrim.length() > 0) {
 			for (front = 0; front < back; front++) {
 				if (!Character.isSpaceChar(toTrim.charAt(front)))
 					break;
 			}
 			for (back = toTrim.length()-1; back > front; back--) {
 				if (!Character.isSpaceChar(toTrim.charAt(back))){
 					back++;
 					break;
 				}
 			} 
 		}
 		
 		return toTrim.substring(front, back);
 	}
 	
 	/**
 	 * inserts or adds new depeneding on teh existing concepts and sets vm
 	 * attributes
 	 * 
 	 * @param vm
 	 *            VM bean
 	 * @param eBean
 	 *            evs bean object
 	 * @param iFrom
 	 *            from pv or vm edit pages
 	 */
 	public void doAppendConcept(VM_Bean vm, EVS_Bean eBean, int iFrom)
 	{
 
 		// update vm with the concept
 		Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
 		if (vmCon.size() > 0)
 		{
 			eBean.setPRIMARY_FLAG(ConceptForm.CONCEPT_QUALIFIER);
 			vmCon.insertElementAt(eBean, vmCon.size() - 1); // TODO - check if
 															// this is right????
 		}
 		else
 		{
 			eBean.setPRIMARY_FLAG(ConceptForm.CONCEPT_PRIMARY);
 			vmCon.addElement(eBean);
 		}
 		vm.setVM_CONCEPT_LIST(vmCon);
 		// reset it only if pv edit
 		// vm.setVM_IDSEQ("");
 		// vm.setVM_SUBMIT_ACTION(VMForm.CADSR_ACTION_UPD);
 		// //VMForm.CADSR_ACTION_INS);
 		// vm.setVM_SHORT_MEANING(eBean.getLONG_NAME()); //have same name for
 		// now
 		switch (iFrom)
 		{
 		case ConceptForm.FOR_PV_PAGE_CONCEPT:
 			vm.setVM_LONG_NAME(eBean.getLONG_NAME()); // have same name for
 														// now
 			break;
 		case ConceptForm.FOR_VM_PAGE_CONCEPT:
 			makeVMNameFromConcept(vm, ConceptForm.FOR_VM_PAGE_CONCEPT);
 			break;
 		}
 	}
 
 	/**
 	 * method to delete the concept
 	 * 
 	 * @param vm
 	 *            VM_Bean object
 	 * @param sRow
 	 *            selected pv row
 	 * @return String error message
 	 */
 	public String doDeleteConcept(VM_Bean vm, String sRow)
 	{
 
 		String errMsg = "";
 		try
 		{
 			Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
 			// to remove all the concepts or the last one in the list
 			if (sRow.equals("-99") || vmCon.size() == 1)
 			{
 				vm.setVM_CONCEPT_LIST(new Vector<EVS_Bean>());
 				// put alt name back to vm definition
 				if (!vm.getVM_ALT_DEFINITION().trim().equals(""))
 					// vm.setVM_DESCRIPTION(vm.getVM_ALT_DEFINITION());
 					vm.setVM_PREFERRED_DEFINITION(vm.getVM_ALT_DEFINITION());
 				vm.setVM_ALT_NAME("");
 				vm.setVM_CONDR_IDSEQ(" ");
 			}
 			else
 			// individual concepts
 			{
 				int iRow = Integer.parseInt(sRow);
 				if (iRow < 0 || iRow > vmCon.size())
 					errMsg += "Unable to determine the selected concept from the list.";
 				else
 				{
 					vmCon.removeElementAt(iRow);
 					this.resetPrimaryFlag(vmCon);
 					vm.setVM_CONCEPT_LIST(vmCon);
 					// reset the concept name summary and description
 					makeVMNameFromConcept(vm, ConceptForm.FOR_VM_PAGE_CONCEPT);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR - doDeleteConcept : ", e);
 		}
 		return errMsg;
 	}
 
 	/**
 	 * method to move the concept up and down
 	 * 
 	 * @param vm
 	 *            VM_Bean object
 	 * @param sRow
 	 *            selected concept row
 	 * @param moveAct
 	 *            String up or down move action
 	 * @return String error message
 	 */
 	public String doMoveConcept(VM_Bean vm, String sRow, String moveAct)
 	{
 
 		String errMsg = "";
 		try
 		{
 			Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
 			Integer IRow = new Integer(sRow);
 			int iRow = IRow.intValue();
 			if (iRow < 0 || iRow > vmCon.size())
 				errMsg += "Unable to determine the selected concept from the list.";
 			else if (iRow == 0 && moveAct.equals("moveUpConcept"))
 				errMsg += "Unable to move the concept up in the list.";
 			else if (iRow == vmCon.size() - 1 && moveAct.equals("moveDownConcept"))
 				errMsg += "Unable to move the concept down in the list.";
 			else
 			{
 				// first store current concept and remove it
 				EVS_Bean curCon = (EVS_Bean) vmCon.remove(iRow);
 				// insert teh current one at the next or previous position
 				if (moveAct.equals(VMForm.ACT_CON_MOVEDOWN))
 					vmCon.insertElementAt(curCon, iRow + 1);
 				else if (moveAct.equals(VMForm.ACT_CON_MOVEUP))
 					vmCon.insertElementAt(curCon, iRow - 1);
 				// mark the primary flag
 				this.resetPrimaryFlag(vmCon);
 				vm.setVM_CONCEPT_LIST(vmCon);
 			}
 			// reset the concept name summary and description
 			if (errMsg.equals(""))
 				makeVMNameFromConcept(vm, ConceptForm.FOR_VM_PAGE_CONCEPT);
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR - doMoveConcept : ", e);
 		}
 		return errMsg;
 	}
 
 	/**
 	 * get the vm from the pv bean gets the vm name and definition from the
 	 * concept list depending on the where the method is called from
 	 * 
 	 * @param pv
 	 *            selected pv bean
 	 * @param toAct
 	 *            constant to open the page
 	 * @return VM_Bean object
 	 */
 	public VM_Bean getVM(PV_Bean pv, int toAct)
 	{
 
 		VM_Bean selVM = new VM_Bean().copyVMBean(pv.getPV_VM());
 		// make sure vm's long exists;
 		if (selVM.getVM_LONG_NAME() == null || selVM.getVM_LONG_NAME().equals(""))
 			// selVM.setVM_LONG_NAME(selVM.getVM_SHORT_MEANING());
 			selVM.setVM_LONG_NAME(selVM.getVM_LONG_NAME());
 		Vector<EVS_Bean> vmCon = selVM.getVM_CONCEPT_LIST();
 		if (vmCon != null && vmCon.size() > 0)
 			makeVMNameFromConcept(selVM, ConceptForm.FOR_VM_PAGE_OPEN);
 		// get the vd, de and crf associate only when opening the page ; act = 0
 		if (toAct == 0)
 			logger.debug("get other acs");
 
 		return selVM;
 	}
 
 	/**
 	 * @param selVM
 	 * @return
 	 */
 	public String checkVMNameExists(VM_Bean selVM, VMForm data)
 	{
 
 		String message = "";
 		VMForm vmForm = new VMForm();
 		vmForm.setCurationServlet(data.getCurationServlet());
 		vmForm.setVMBean(selVM);
 		vmForm.setSearchTerm(selVM.getVM_LONG_NAME());
 		this.searchVMValues(vmForm, "0");
 		if (vmForm.getVMList().size() > 0)
 		{
 			for (int i = 0; i < vmForm.getVMList().size(); i++)
 			{
 				VM_Bean vm = (VM_Bean) vmForm.getVMList().get(i);
 				if (vm.getIDSEQ() != selVM.getIDSEQ())
 				{
 					message = "Warning: There exists a VM with the same name";
 				}
 			}
 		}
 		return message;
 
 	}
 
 	/**
 	 * To check validity of the data for Value Meanings component before
 	 * submission. Validation is done against Database restriction and ISO1179
 	 * standards. calls various methods to get validity messages and store it
 	 * into the vector. Valid/Invalid Messages are stored in request Vector
 	 * 'vValidate' along with the field, data.
 	 * 
 	 * @param vm
 	 *            Value Meanings Bean.
 	 * @return ValidateBean
 	 */
 	public Vector<ValidateBean> doValidateVM(VM_Bean vm, VMForm data)
 	{
 
 		Vector<ValidateBean> vValidate = new Vector<ValidateBean>();
 
 		try
 		{
 			String message = checkVMNameExists(vm, data);
 			String s;
 			String strInValid = "";
 			s = vm.getVM_LONG_NAME();
 			if (s == null)
 				s = "";
 			UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_NAME, s, true, 255, strInValid,
 					"");
 			if (s != null && !message.equals(""))
 				UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_NAME, s, false, 255,
 						strInValid + message, "");
 			// s = vm.getVM_DESCRIPTION();
 			s = vm.getVM_PREFERRED_DEFINITION();
 			if (s == null)
 				s = "";
 			Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
 			if (vmCon.size() > 0)
 			{
 				String sAlt = vm.getVM_ALT_DEFINITION();
 				if (!sAlt.equals(""))
 					UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_MAN_DESC, sAlt, false,
 							2000, strInValid, "");
 				// system generated
 				UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_SYS_DESC, s, true, 2000,
 						strInValid, "");
 			}
 			else
 				UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_DESC, s, true, 2000,
 						strInValid, "");
 
 			s = vm.getVM_CHANGE_NOTE();
 			if (s == null)
 				s = "";
 			UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_CH_NOTE, s, false, 2000, "", "");
 
 			s = vm.getVM_ALT_NAME();
 			if (s == null)
 				s = "";
 			UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_CON_SUM, s, false, 2000, "", "");
 
 			s = "";
 			for (int i = 0; i < vmCon.size(); i++)
 			{
 				EVS_Bean eBean = (EVS_Bean) vmCon.elementAt(i);
 				String sN = eBean.getLONG_NAME();
 				if (!sN.equals(""))
 				{
 					String sID = eBean.getCONCEPT_IDENTIFIER();
 					if (!sID.equals(""))
 						sN += " : " + sID;
 					s += sN + "<br>";
 				}
 			}
 			UtilService.setValPageVector(vValidate, VMForm.ELM_LBL_CON_NAME + " : "
 					+ VMForm.ELM_LBL_CON_ID, s, false, -1, "", "");
 
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setValidatePageValuesVM " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error setValidatePageValuesVM");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 		return vValidate;
 	} // end of do Validate VM
 
 	/**
 	 * resets the concept type of each concept
 	 * 
 	 * @param vmCon
 	 *            Vector of EVS_Bean object
 	 */
 	public void resetPrimaryFlag(Vector<EVS_Bean> vmCon)
 	{
 
 		int size = vmCon.size();
 		for (int i = 0; i < size; i++)
 		{
 			EVS_Bean eBean = vmCon.elementAt(i);
 			if (i == size - 1)
 				eBean.setPRIMARY_FLAG(ConceptForm.CONCEPT_PRIMARY);
 			else
 				eBean.setPRIMARY_FLAG(ConceptForm.CONCEPT_QUALIFIER);
 			vmCon.setElementAt(eBean, i);
 		}
 	}
 
 	/**
 	 * Sting gets the vm display name
 	 * 
 	 * @param vm
 	 *            VM_Bean object
 	 * @return concatenated vm name
 	 */
 	public String getVMDisplayName(VM_Bean vm)
 	{
 
 		String vmNameDisplay = "";
 		String sLongName = vm.getVM_LONG_NAME();
 		// append the name, id and version to display
 		if (!sLongName.equals(""))
 		{
 			String sVersion = vm.getVM_VERSION();
 			String sVMID = vm.getVM_ID();
 			vmNameDisplay = sLongName + "  [" + sVMID + "v" + sVersion + "]";
 		}
 		return vmNameDisplay;
 	}
 
 	/**
 	 * store vm attributes from the database in pv bean
 	 * 
 	 * @param rs
 	 *            ResultSet from the query
 	 * @param conn
 	 *            Connection object
 	 * @return VM_Bean
 	 */
 	public VM_Bean doSetVMAttributes(ResultSet rs, Connection conn)
 	{
 
 		VM_Bean vm = new VM_Bean();
 		try
 		{
 			// vm.setVM_SHORT_MEANING(rs.getString("short_meaning"));
 			// String vmD = rs.getString("vm_description");
 			/*
 			 * String vmD = rs.getString("vm_definition_source"); if (vmD ==
 			 * null || vmD.equals(""))
 			 */
 			String vmD = rs.getString("PREFERRED_DEFINITION");
 			// vm.setVM_DESCRIPTION(vmD);
 			vm.setVM_PREFERRED_DEFINITION(vmD);
 			vm.setVM_SUBMIT_ACTION(VMForm.CADSR_ACTION_NONE);
 			vm.setVM_LONG_NAME(rs.getString("LONG_NAME"));
 			vm.setVM_IDSEQ(rs.getString("VM_IDSEQ"));
 			vm.setVM_ID(rs.getString("VM_ID"));
 			vm.setVM_CONTE_IDSEQ(rs.getString("conte_idseq"));
 			// String sChg = rs.getString("comments");
 			// if (sChg == null || sChg.equals(""))
 			String sChg = rs.getString("change_note");
 			vm.setVM_CHANGE_NOTE(sChg);
 			vm.setASL_NAME(rs.getString("asl_name"));
 			// vm.setVM_DEFINITION_SOURCE(rs.getString("vm_definition_source"));
 			this.getVMVersion(rs, vm);
 			String sCondr = rs.getString("condr_idseq");
 			vm.setVM_CONDR_IDSEQ(sCondr);
 			// get vm concepts
 			if (sCondr != null && !sCondr.equals(""))
 			{
 				ConceptForm cdata = new ConceptForm();
 				cdata.setDBConnection(conn);
 				ConceptAction cact = new ConceptAction();
 				Vector<EVS_Bean> conList = cact.getAC_Concepts(sCondr, cdata);
 				vm.setVM_CONCEPT_LIST(conList);
 				DBAccess db = new DBAccess(conn);
 				String idSeq = rs.getString("VM_IDSEQ");
 				Alternates[] altList = db.getAlternates(new String[]
 				{ idSeq }, true, true);
 				vm.setVM_ALT_LIST(altList);
 			}
 		}
 		catch (SQLException e)
 		{
 			logger.error("ERROR - -doSetVMAttributes for close : " + e.toString(), e);
 		}
 		catch (ToolException e1)
 		{
 			logger.error("ERROR - -doSetVMAttributes for close : " + e1.toString(), e1);
 		}
 		return vm;
 	}
 
 	/**
 	 * to validate the vm changes on pv page records if multiple value meanings
 	 * match against name, defintion or concept exist in cadsr already sends
 	 * back the exact match vm (against name, def and concept) if exists
 	 * 
 	 * @param data
 	 *            VMForm object
 	 * @return VM_Bean object if exact match otherwise null
 	 */
 	public VM_Bean validateVMData(VMForm data)
 	{
 
 		VM_Bean vmBean = data.getVMBean();
 		// String VMName = vmBean.getVM_SHORT_MEANING();
 		String VMName = vmBean.getVM_LONG_NAME();
 		// check for vm name match
 		getExistingVM(VMName, "", "", data); // check if vm exists
 		Vector<VM_Bean> nameList = data.getExistVMList();
 		// if the returned one has the same idseq as as the one in hand; ignore
 		// it
 		// boolean editexisting = false; if (nameList.size() == 1)
 		if (nameList.size() > 0)
 		{
 			for (int k = 0; k < nameList.size(); k++)
 			{
 				VM_Bean existVM = checkExactMatch(nameList.elementAt(k), vmBean);
 				if (existVM != null)
 				{
 					data.setVMBean(existVM);
 					return existVM; // return the exact match name- definition-
 									// concept
 				}
 			}
 		}
 
 		// add the name matched one to the vector of nameMatchVMs; mark this one
 		// as (name) match
 		if (nameList.size() > 0)
 			getFlaggedMessageVM(data, 'E');
 		else
 			data.setExistVMList(new Vector<VM_Bean>()); // make it empty because
 														// found the existing
 
 		// String VMDef = vmBean.getVM_DESCRIPTION();
 		String VMDef = vmBean.getVM_PREFERRED_DEFINITION();
 		Vector<EVS_Bean> vCon = vmBean.getVM_CONCEPT_LIST();
 		// check if default definition; added when no definition exist; ignore
 		// it if found
 		if (!checkDefaultDefinition(VMDef, vCon))
 		{
 			// check for vm defn match
 			this.getExistingVM("", "", VMDef, data);
 			Vector<VM_Bean> defList = data.getDefnVMList();
 			// add the list of definitionMatchVMs to existing list if not
 			// existed already
 			if (defList.size() > 0)
 				getFlaggedMessageVM(data, 'D');
 		}
 		// check if selected vm has concepts
 		if (vCon != null && vCon.size() > 0)
 		{
 			// get the condr idseq from the database
 			String sCondr = this.getConceptCondr(vCon, data);
 			if (!sCondr.equals(""))
 			{
 				getExistingVM("", sCondr, "", data);
 				Vector<VM_Bean> conList = data.getConceptVMList();
 				// add the list of conceptMatchVMs to existing list if not
 				// existed already
 				if (conList.size() > 0)
 					getFlaggedMessageVM(data, 'C');
 			}
 		}
 		return null; // no exact match found
 	}
 
 	/**
 	 * gets the exact match vm
 	 * 
 	 * @param existVM
 	 *            existing vm
 	 * @param newVM
 	 *            new vm
 	 * @return VM_Bean object if matched, null otherwise
 	 */
 	public VM_Bean checkExactMatch(VM_Bean existVM, VM_Bean newVM)
 	{
 
 		boolean match = true;
 
 		/*
 		 * String VMDef = newVM.getVM_DESCRIPTION(); String nameDef =
 		 * existVM.getVM_DESCRIPTION();
 		 */
 		String VMDef = newVM.getVM_PREFERRED_DEFINITION();
 		String nameDef = existVM.getVM_PREFERRED_DEFINITION();
 		// match the name
 		if (!newVM.getVM_LONG_NAME().equals(existVM.getVM_LONG_NAME()))
 			match = false;
 		// check for exact match by defintion
 		else if (VMDef.equals(nameDef))
 		{
 			// check for exact match for the concepts
 			Vector<EVS_Bean> vCon = newVM.getVM_CONCEPT_LIST();
 			Vector<EVS_Bean> nameCon = existVM.getVM_CONCEPT_LIST();
 			if (nameCon.size() == vCon.size())
 			{
 				for (int i = 0; i < nameCon.size(); i++)
 				{
 					EVS_Bean nBean = nameCon.elementAt(i);
 					EVS_Bean cBean = vCon.elementAt(i);
 					// if concepts don't match break the loop
 					if (!nBean.getCONCEPT_IDENTIFIER().equals(cBean.getCONCEPT_IDENTIFIER()))
 					{
 						match = false; // concept data don't match
 						break;
 					}
 				}
 			}
 			else
 				// concept size don't match
 				match = false;
 		}
 		else
 			// defintion don't match
 			match = false;
 		// return the exact match vm
 		if (match)
 			return existVM;
 		// if reached here send back null
 		return null;
 	}
 
 	/**
 	 * To create a new version of the VM call the stored procedure
 	 * sbr.meta_Config_mgmt.VM_VERSION (p_Idseq
 	 * Admin_components_view.ac_idseq%TYPE ,p_version IN
 	 * Admin_components_view.version%TYPE ,p_new_vm_idseq OUT
 	 * admin_components_View.ac_idseq%TYPE , P_RETURN_CODE OUT VARCHAR2)
 	 * 
 	 * @param data
 	 *            VM Data object.
 	 * @return sReturnCode String return code from the stored procedure call.
 	 *         null if no error occurred.
 	 */
 	private String setNewVersionVM(VMForm data)
 	{
 
 		String sReturnCode = "None";
 		CallableStatement cstmt = null;
 		try
 		{
 			VM_Bean vm = data.getVMBean();
 			if (data.getCurationServlet().getConn() != null)
 			{
 				cstmt =
 						data.getCurationServlet().getConn().prepareCall(
 								"{call sbr.meta_Config_mgmt.VM_VERSION(?,?,?,?,?)}");
 
 				// Set the out parameters (which are inherited from the
 				// PreparedStatement class)
 				cstmt.registerOutParameter(3, java.sql.Types.VARCHAR); // NEW
 				// ID
 				cstmt.registerOutParameter(4, java.sql.Types.VARCHAR); // RETURN
 				// CODE
 
 				cstmt.setString(1, vm.getVM_IDSEQ()); // AC idseq
 				Double DVersion = new Double(vm.getVM_VERSION()); // convert
 																	// the
 																	// version
 				// to double type
 				double dVersion = DVersion.doubleValue();
 				cstmt.setDouble(2, dVersion);
 
 				// Get the username from the session.
 				String userName = data.getCurationServlet().sessionData.UsrBean.getUsername();
 				cstmt.setString(5, userName); // username
 
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				sReturnCode = cstmt.getString(4);
 				String newACID = cstmt.getString(3);
 				// trim off the extra spaces in it
 				if (newACID != null && !newACID.equals(""))
 					newACID = newACID.trim();
 				// update the bean if return code is null and new de id is not
 				// null
 				if (sReturnCode == null && newACID != null)
 				{
 					// update the bean
 					vm.setVM_IDSEQ(newACID);
 
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in creating new VM Version: " + e.toString(), e);
 		}
 		finally
 		{
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return sReturnCode;
 	}
 
 	// private methods
 
 	/**
 	 * To insert a new Value Meaing in the database when selected a term from
 	 * EVS. Called from CurationServlet. Gets all the attribute values from the
 	 * bean, sets in parameters, and registers output parameter. Calls oracle
 	 * stored procedure "{call
 	 * SBREXT_Set_Row.SET_VM_CONDR(?,?,?,?,?,?,?,?,?,?,?)}" to submit
 	 * 
 	 * @param data
 	 *            VM Data object.
 	 * @param conArray
 	 * @return String return code
 	 */
 	
 	private String removeVMCondr(VMForm data) {
 		
 		ResultSet rs = null;
 		PreparedStatement ps = null;
 		String stMsg = ""; // out
 		try
 		{
 			VM_Bean vm = data.getVMBean();
 			String idseq = vm.getIDSEQ();
 			if (data.getCurationServlet().getConn() != null)
 			{
 				ps = data
 				.getCurationServlet()
 				.getConn()
 				.prepareStatement("update VALUE_MEANINGS_VIEW set condr_idseq = '' where vm_idseq = ?");
 				
 				ps.setString(1,idseq);
 				
 				boolean ret = ps.execute();
 				
 			}
 			
 		} catch (Exception e)
 		{
 			logger.error("ERROR in setVM for other : " + e.toString(), e);
 			data.setRetErrorCode("Exception");
 			stMsg += "\\tException : Unable to remove all VM concepts.";
 		}
 		finally
 		{
 			rs = SQLHelper.closeResultSet(rs);
 			ps = SQLHelper.closePreparedStatement(ps);
 		}
 		
 		return stMsg;
 	}
 	
 	private String setVM(VMForm data, String conArray)
 	{
 
 		ResultSet rs = null;
 		CallableStatement cstmt = null;
 		String stMsg = ""; // out
 		try
 		{
 			VM_Bean vm = data.getVMBean();
 			String sAction = vm.getVM_SUBMIT_ACTION();
 			if (sAction == null)
 				sAction = VMForm.CADSR_ACTION_INS;
 			if (data.getCurationServlet().getConn() != null)
 			{
 				// cstmt = conn.prepareCall("{call
 				// SBREXT.SBREXT_SET_ROW.SET_VM(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				cstmt =
 						data
 								.getCurationServlet()
 								.getConn()
 								.prepareCall(
 										"{call SBREXT_SET_ROW.SET_VM(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				// register the Out parameters
 				cstmt.registerOutParameter(2, java.sql.Types.VARCHAR); // return
 																		// code
 				cstmt.registerOutParameter(3, java.sql.Types.VARCHAR); // action
 				cstmt.registerOutParameter(5, java.sql.Types.VARCHAR); // vm_idseq
 				cstmt.registerOutParameter(6, java.sql.Types.VARCHAR); // preferred
 																		// name
 				cstmt.registerOutParameter(7, java.sql.Types.VARCHAR); // long
 																		// name
 				cstmt.registerOutParameter(8, java.sql.Types.VARCHAR); // preferred
 																		// definition
 				cstmt.registerOutParameter(9, java.sql.Types.VARCHAR); // context
 																		// idseq
 				cstmt.registerOutParameter(10, java.sql.Types.VARCHAR); // asl
 																		// name
 				cstmt.registerOutParameter(11, java.sql.Types.VARCHAR); // version
 				cstmt.registerOutParameter(12, java.sql.Types.VARCHAR); // vm_id
 				cstmt.registerOutParameter(13, java.sql.Types.VARCHAR); // latest
 																		// version
 																		// ind
 				cstmt.registerOutParameter(14, java.sql.Types.VARCHAR); // condr
 																		// idseq
 				cstmt.registerOutParameter(15, java.sql.Types.VARCHAR); // definition
 																		// source
 				cstmt.registerOutParameter(16, java.sql.Types.VARCHAR); // origin
 				cstmt.registerOutParameter(17, java.sql.Types.VARCHAR); // change
 																		// note
 				cstmt.registerOutParameter(18, java.sql.Types.VARCHAR); // begin
 																		// date
 				cstmt.registerOutParameter(19, java.sql.Types.VARCHAR); // end
 																		// date
 				cstmt.registerOutParameter(20, java.sql.Types.VARCHAR); // created
 																		// by
 				cstmt.registerOutParameter(21, java.sql.Types.VARCHAR); // date
 																		// created
 				cstmt.registerOutParameter(22, java.sql.Types.VARCHAR); // modified
 																		// by
 				cstmt.registerOutParameter(23, java.sql.Types.VARCHAR); // date
 																		// modified
 
 				// Get the username from the session.
 				String userName = data.getCurationServlet().sessionData.UsrBean.getUsername();
 				// Set the In parameters (which are inherited from the
 				// PreparedStatement class)
 				cstmt.setString(1, userName);
 				cstmt.setString(3, sAction);
 				cstmt.setString(4, conArray);
 				cstmt.setString(5, vm.getVM_IDSEQ());
 				// set value meaning if action is to update
 				// if (sAction.equals(VMForm.CADSR_ACTION_UPD) || conArray ==
 				// null)
 				// cstmt.setString(7, vm.getVM_SHORT_MEANING());
 				cstmt.setString(7, vm.getVM_LONG_NAME());
 				// definition and change note
 				// cstmt.setString(8, vm.getVM_DESCRIPTION());
 				cstmt.setString(8, vm.getVM_PREFERRED_DEFINITION());
 				cstmt.setString(10, vm.getASL_NAME());
 				cstmt.setString(11, vm.getVM_VERSION());
 				cstmt.setString(17, vm.getVM_CHANGE_NOTE());
 				// remove the concepts
 				if (vm.getVM_CONDR_IDSEQ().equals(" "))
 					cstmt.setString(14, null);
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				String sRet = cstmt.getString(2);
 				if (sRet != null && !sRet.equals("") && !sRet.equals("API_VM_300"))
 				{
 					stMsg =
 							"\\t" + sRet + " : Unable to update the Value Meaning - "
 									+ vm.getVM_LONG_NAME() + ".";
 					// + vm.getVM_SHORT_MEANING() + ".";
 					// creation
 					data.setRetErrorCode(sRet);
 					data.setPvvmErrorCode(sRet);
 				}
 				else
 				{
 					// store the vm attributes created by stored procedure in
 					// the bean
 					// vm.setVM_SHORT_MEANING(cstmt.getString(7));
 					vm.setVM_LONG_NAME(cstmt.getString(7));
 					// vm.setVM_DESCRIPTION(cstmt.getString(8));
 					vm.setVM_PREFERRED_DEFINITION(cstmt.getString(8));
 					vm.setVM_CHANGE_NOTE(cstmt.getString(17));
 					vm.setVM_BEGIN_DATE(cstmt.getString(18));
 					vm.setVM_END_DATE(cstmt.getString(19));
 					vm.setVM_CONDR_IDSEQ(cstmt.getString(14));
 					vm.setVM_IDSEQ(cstmt.getString(5));
 					vm.setVM_CONTE_IDSEQ(cstmt.getString(9));
 					vm.setASL_NAME(cstmt.getString(10));
 					vm.setVM_VERSION(cstmt.getString(11));
 					vm.setVM_ID(cstmt.getString(12));
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setVM for other : " + e.toString(), e);
 			data.setRetErrorCode("Exception");
 			stMsg += "\\tException : Unable to update VM attributes.";
 		}
 		finally
 		{
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return stMsg;
 	}
 
 	/**
 	 * To insert a new CD VMS relationship in the database after creating VM or
 	 * its relationship with VD. Called from CurationServlet. Gets all the
 	 * attribute values from the bean, sets in parameters, and registers output
 	 * parameter. Calls oracle stored procedure "{call
 	 * SBREXT_Set_Row.SET_CD_VMS(?,?,?,?,?,?,?,?,?,?,?)}" to submit
 	 * 
 	 * @param data
 	 *            VMForm object
 	 * @param vm
 	 *            VM Bean.
 	 * @return String return code from the stored procedure call. null if no
 	 *         error occurred.
 	 */
 	private String setCDVMS(VMForm data, VM_Bean vm)
 	{
 
 		CallableStatement cstmt = null;
 		String sReturnCode = ""; // out
 		try
 		{
 			if (data.getCurationServlet().getConn() != null)
 			{
 				// cstmt = conn.prepareCall("{call
 				// SBREXT_Set_Row.SET_CD_VMS(?,?,?,?,?,?,?,?,?,?,?)}");
 				cstmt =
 						data.getCurationServlet().getConn().prepareCall(
 								"{call SBREXT_SET_ROW.SET_CD_VMS(?,?,?,?,?,?,?,?,?,?,?,?)}");
 				// register the Out parameters
 				cstmt.registerOutParameter(2, java.sql.Types.VARCHAR); // return
 																		// code
 				cstmt.registerOutParameter(4, java.sql.Types.VARCHAR); // out
 																		// cv_idseq
 				cstmt.registerOutParameter(5, java.sql.Types.VARCHAR); // out
 																		// cd_idseq
 				cstmt.registerOutParameter(6, java.sql.Types.VARCHAR); // out
 																		// value
 																		// meaning
 				cstmt.registerOutParameter(7, java.sql.Types.VARCHAR); // out
 																		// description
 				cstmt.registerOutParameter(8, java.sql.Types.VARCHAR); // out
 																		// VM_IDSEQ
 				cstmt.registerOutParameter(9, java.sql.Types.VARCHAR); // date
 																		// created
 				cstmt.registerOutParameter(10, java.sql.Types.VARCHAR); // created
 																		// by
 				cstmt.registerOutParameter(11, java.sql.Types.VARCHAR); // modified
 																		// by
 				cstmt.registerOutParameter(12, java.sql.Types.VARCHAR); // date
 																		// modified
 
 				// Set the In parameters (which are inherited from the
 				// PreparedStatement class)
 				// Get the username from the session.
 				String userName = data.getCurationServlet().sessionData.UsrBean.getUsername();
 				// Set the In parameters (which are inherited from the
 				// PreparedStatement class)
 				cstmt.setString(1, userName);
 				cstmt.setString(3, VMForm.CADSR_ACTION_INS);
 				cstmt.setString(5, vm.getVM_CD_IDSEQ());
 				// cstmt.setString(6, vm.getVM_SHORT_MEANING());
 				cstmt.setString(6, vm.getVM_LONG_NAME());
 				// cstmt.setString(7, vm.getVM_DESCRIPTION());
 				cstmt.setString(7, vm.getVM_PREFERRED_DEFINITION());
 				cstmt.setString(8, vm.getVM_IDSEQ());
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				sReturnCode = cstmt.getString(2);
 				// String sCV_ID = CStmt.getString(3);
 				if (sReturnCode != null && !sReturnCode.equals("")
 						&& !sReturnCode.equals("API_CDVMS_203"))
 				{
 					data
 							.setStatusMsg(data.getStatusMsg()
 									+ "\\t "
 									+ sReturnCode
 									+ " : Unable to update Conceptual Domain and Value Meaning relationship - "
 									// + vm.getVM_CD_NAME() + " and " +
 									// vm.getVM_SHORT_MEANING() + ".");
 									+ vm.getVM_CD_NAME() + " and " + vm.getVM_LONG_NAME() + ".");
 					logger.error(data.getStatusMsg());
 					// data.setRetErrorCode(sReturnCode);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setCDVMS for other : " + e.toString(), e);
 			data.setRetErrorCode("Exception");
 			data.setStatusMsg(data.getStatusMsg()
 					+ "\\tException : Unable to update CD and VM relationship.");
 		}
 		finally
 		{
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return sReturnCode;
 	}
 
 	/**
 	 * To get the value from the bean for the selected field, called from
 	 * VMsortedRows.
 	 * 
 	 * @param curBean
 	 *            VM bean.
 	 * @param curField
 	 *            sort Type field name.
 	 * @return String VMField Value if selected field is found. otherwise empty
 	 *         string.
 	 */
 	private String getVMFieldValue(VM_Bean curBean, String curField)
 	{
 
 		String returnValue = "";
 		try
 		{
 			EVS_Bean curEVS = curBean.getVM_CONCEPT();
 			if (curEVS == null)
 				curEVS = new EVS_Bean();
 			if (curField.equals("meaning"))
 				// returnValue = curBean.getVM_SHORT_MEANING();
 				returnValue = curBean.getVM_LONG_NAME();
 			else if (curField.equals("MeanDesc"))
 				// returnValue = curBean.getVM_DESCRIPTION();
 				returnValue = curBean.getVM_PREFERRED_DEFINITION();
 			else if (curField.equals("comment"))
 				returnValue = curBean.getVM_CHANGE_NOTE();
 			else if (curField.equals("ConDomain"))
 				returnValue = curBean.getVM_CD_NAME();
 			else if (curField.equals("umls"))
 				returnValue = curEVS.getCONCEPT_IDENTIFIER();
 			else if (curField.equals("source"))
 				returnValue = curEVS.getEVS_DEF_SOURCE();
 			else if (curField.equals("database"))
 				returnValue = curEVS.getEVS_DATABASE();
 
 			if (returnValue == null)
 				returnValue = "";
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in VMAction-getVMField : " + e.toString(), e);
 		}
 		return returnValue;
 	}
 
 	/**
 	 * adds all existing vms to vector to display it on the page
 	 * 
 	 * @param data
 	 *            VMForm object
 	 * @param vmFlag
 	 *            char to display all or vm by name filter, vm by concept
 	 *            filter, vm by defin filter.
 	 */
 	private void getFlaggedMessageVM(VMForm data, char vmFlag)
 	{
 
 		Vector<VM_Bean> vErrMsg = data.getErrorMsgList();
 		data.setStatusMsg("Value Meaning");
 		switch (vmFlag)
 		{
 		case 'A': // print all cases
 		case 'E': // existing vm only
 			Vector<VM_Bean> vmexist = data.getExistVMList();
 			if (vmexist != null && vmexist.size() > 0)
 			{
 				for (int i = 0; i < vmexist.size(); i++)
 				{
 					VM_Bean vm = vmexist.elementAt(i);
 					// if (vm == null || vm.getVM_SHORT_MEANING() == null ||
 					// vm.getVM_SHORT_MEANING().equals(""))
 					if (vm == null || vm.getVM_LONG_NAME() == null
 							|| vm.getVM_LONG_NAME().equals(""))
 						break;
 					// mark the message
 					vm.setVM_COMMENT_FLAG("Name matches.");
 					vErrMsg.addElement(vm);
 				}
 			}
 			if (vmFlag != 'A')
 				break;
 		case 'C': // concept vm only
 			Vector<VM_Bean> vmcon = data.getConceptVMList();
 			if (vmcon != null && vmcon.size() > 0)
 			{
 				for (int i = 0; i < vmcon.size(); i++)
 				{
 					VM_Bean vm = vmcon.elementAt(i);
 					// if (vm == null || vm.getVM_SHORT_MEANING() == null ||
 					// vm.getVM_SHORT_MEANING().equals(""))
 					if (vm == null || vm.getVM_LONG_NAME() == null
 							|| vm.getVM_LONG_NAME().equals(""))
 						break;
 					// mark the message
 					if (!existsInList(vm, vErrMsg))
 					{
 						vm.setVM_COMMENT_FLAG("Concept matches.");
 						vErrMsg.addElement(vm);
 					}
 				}
 			}
 			if (vmFlag != 'A')
 				break;
 		case 'D':
 			Vector<VM_Bean> vmdef = data.getDefnVMList();
 			if (vmdef != null && vmdef.size() > 0)
 			{
 				for (int i = 0; i < vmdef.size(); i++)
 				{
 					VM_Bean vm = vmdef.elementAt(i);
 					// if (vm == null || vm.getVM_SHORT_MEANING() == null ||
 					// vm.getVM_SHORT_MEANING().equals(""))
 					if (vm == null || vm.getVM_LONG_NAME() == null
 							|| vm.getVM_LONG_NAME().equals(""))
 						break;
 					// mark the message
 					if (!existsInList(vm, vErrMsg))
 					{
 						vm.setVM_COMMENT_FLAG("Definition matches.");
 						vErrMsg.addElement(vm);
 					}
 				}
 			}
 			break;
 		default:
 		}
 		data.setErrorMsgList(vErrMsg);
 	}
 
 	/**
 	 * checks if aleady exists in the list
 	 * 
 	 * @param vm
 	 *            VM_Bean obejct to check against
 	 * @param existList
 	 *            Vector<VM_Bean> existing list
 	 * @return boolean value
 	 */
 	private boolean existsInList(VM_Bean vm, Vector<VM_Bean> existList)
 	{
 
 		boolean exists = false;
 		for (int i = 0; i < existList.size(); i++)
 		{
 			VM_Bean eBean = existList.elementAt(i);
 			if (eBean.getVM_IDSEQ().equals(vm.getVM_IDSEQ()))
 			{
 				exists = true;
 				break;
 			}
 		}
 		return exists;
 	}
 
 	/**
 	 * gets the existing condr idseq from the database
 	 * 
 	 * @param conList
 	 *            EVS Bean vector of concepts of the vm
 	 * @param data
 	 *            VMForm object
 	 * @return String condridseq
 	 */
 	private String getConceptCondr(Vector<EVS_Bean> conList, VMForm data)
 	{
 
 		String condr = "";
 		ConceptForm conData = new ConceptForm();
 		conData.setConceptList(conList);
 		conData.setDBConnection(data.getCurationServlet().getConn());
 		conData.setCurationServlet(data.getCurationServlet());
 		// call the method n concept action
 		ConceptAction conAct = new ConceptAction();
 		condr = conAct.getConDerivation(conData);
 		return condr;
 	}
 
 	/**
 	 * to get the vms filtered by vmname, condridseq, or definition
 	 * 
 	 * @param vmName
 	 *            String vm name to filter
 	 * @param sCondr
 	 *            String condr idseq to filter
 	 * @param sDef
 	 *            String defintion to filter
 	 * @param data
 	 *            VMForm object to filter
 	 */
 	private void getExistingVM(String vmName, String sCondr, String sDef, VMForm data)
 	{
 
 		// set data filters
 		data.setSearchTerm(vmName); // search by name
 		data.setSearchFilterCondr(sCondr); // search by condr
 		data.setSearchFilterDef(sDef); // search by defintion
 		// call method
 		data.setVMList(new Vector<VM_Bean>());
 		searchVMValues(data, "0");
 		// set teh flag
 		Vector<VM_Bean> vmList = data.getVMList();
 		if (vmList != null && vmList.size() > 0)
 		{
 			if (!vmName.equals(""))
 				data.setExistVMList(vmList);
 			else if (!sCondr.equals(""))
 				data.setConceptVMList(vmList);
 			else if (!sDef.equals(""))
 				data.setDefnVMList(vmList);
 		}
 	}
 
 	/**
 	 * puts the .0 to the version number if to make it decimal
 	 * 
 	 * @param rs
 	 *            ResultSet object
 	 * @param vm
 	 *            VM_Bean object
 	 */
 	private void getVMVersion(ResultSet rs, VM_Bean vm)
 	{
 
 		try
 		{
 			String rsV = rs.getString("version");
 			if (rsV == null)
 				rsV = "";
 			if (!rsV.equals("") && rsV.indexOf('.') < 0)
 				rsV += ".0";
 			vm.setVM_VERSION(rsV);
 		}
 		catch (SQLException e)
 		{
 			logger.error("ERROR - getVMVersion ", e);
 		}
 	}
 
 	/**
 	 * no need to match the default definition in cadsr. this method to get
 	 * check editing vm has the default definition
 	 * 
 	 * @param sDef
 	 *            String current defintion
 	 * @param vmCon
 	 *            Vector<EVS_Bean> list of concepts
 	 * @return boolean true if it default value
 	 */
 	private boolean checkDefaultDefinition(String sDef, Vector<EVS_Bean> vmCon)
 	{
 
 		String dDef = VMForm.DEFINITION_DEFAULT_VALUE;
 		boolean bDefault = false;
 		// check for empty or single one
 		if (sDef.equals("") || sDef.equalsIgnoreCase(dDef) || sDef.equalsIgnoreCase(dDef + "."))
 			bDefault = true;
 		// make the default defintion from the concepts to match the selected
 		// definition
 		if (vmCon.size() > 1)
 		{
 			bDefault = true; // assume default defintion when concept exists
 			for (int i = 0; i < vmCon.size(); i++)
 			{
 				EVS_Bean eBean = vmCon.elementAt(i);
 				String conDef = eBean.getPREFERRED_DEFINITION();
 				if (!conDef.equalsIgnoreCase(dDef) && !conDef.equalsIgnoreCase(dDef + "."))
 				{
 					bDefault = false;
 					break;
 				}
 			}
 		}
 		return bDefault;
 	}
 
 	/**
 	 * to get the sql query string
 	 * 
 	 * @param sInput
 	 *            String vm value
 	 * @return sql string
 	 */
 	private String makeSelectQuery(String sInput)
 	{
 
 		StringTokenizer st = new StringTokenizer(sInput, ",");
 		StringBuffer inClause = new StringBuffer();
 		boolean firstValue = true;
 		while (st.hasMoreTokens())
 		{
 			inClause.append('?');
 			if (st.hasMoreTokens())
 				inClause.append(',');
 			st.nextToken();
 		}
 		String clause = inClause.substring(0,inClause.lastIndexOf(","));
 		String sSQL =
 				"SELECT vm.VM_IDSEQ, vm.LONG_NAME, vm.PREFERRED_DEFINITION"
 						+ ", vm.begin_date, vm.end_date, vm.condr_idseq, vm.VERSION, vm.vm_id, vm.conte_idseq"
 						+ ", vm.asl_name, vm.change_note, vm.comments, vm.latest_version_ind"
 						+ " FROM sbr.value_meanings_view vm WHERE vm.long_name IN ("+ clause + ") ORDER BY long_name";
 		return sSQL;
 	}
 
 	
 	/*
 	 * private String makeSelectQuery(String sInput) { String sSQL = "SELECT
 	 * vm.VM_IDSEQ, vm.LONG_NAME, vm.PREFERRED_DEFINITION, vm.description
 	 * vm_description" + ", vm.begin_date, vm.end_date, vm.condr_idseq,
 	 * vm.short_meaning, vm.VERSION,vm.vm_id, vm.conte_idseq" + ", vm.asl_name,
 	 * vm.change_note, vm.comments, vm.latest_version_ind" + " FROM
 	 * sbr.value_meanings_view vm" + " WHERE vm.short_meaning IN ('" + sInput +
 	 * "')" + " ORDER BY short_meaning"; String sSQL = "SELECT vm.VM_IDSEQ,
 	 * vm.LONG_NAME, vm.PREFERRED_DEFINITION" + ", vm.begin_date, vm.end_date,
 	 * vm.condr_idseq, vm.VERSION, vm.vm_id, vm.conte_idseq" + ", vm.asl_name,
 	 * vm.change_note, vm.comments, vm.latest_version_ind" + " FROM
 	 * sbr.value_meanings_view vm" + " WHERE vm.long_name IN (?) ORDER BY
 	 * long_name"; return sSQL; }
 	 */
 	/**
 	 * to search multiple vm names at time
 	 * 
 	 * @param conn
 	 *            connection object
 	 * @param searchString
 	 *            String search string
 	 * @return vector of existing vm bean object
 	 */
 	public Vector<VM_Bean> searchMultipleVM(Connection conn, String searchString)
 	{
 
 		ResultSet rs = null;
 		PreparedStatement pstmt = null;
 		Vector<VM_Bean> vVMs = new Vector<VM_Bean>();
 		try
 		{
 			// make sql
 			String sSQL = makeSelectQuery(searchString);
 			// prepare statement
 			pstmt = conn.prepareStatement(sSQL);
 			
 			StringTokenizer st = new StringTokenizer(searchString, ",");
 			int ii = 0;
 			while (st.hasMoreTokens())
 			{
 				ii++;
 				pstmt.setString(ii, st.nextToken());
 			}
 			// Now we are ready to call the function
 			rs = pstmt.executeQuery();
 			// get attributes from the recorset
 			while (rs.next())
 			{
 				VM_Bean vm = doSetVMAttributes(rs, conn);
 				// add the element
 				vVMs.addElement(vm);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR - : " + e.toString(), e);
 		}
 		finally
 		{
 			rs = SQLHelper.closeResultSet(rs);
             pstmt = SQLHelper.closePreparedStatement(pstmt);
 		}
 		return vVMs;
 	}
 
 	/**
 	 * adds more hyperlink for concept attributes in the search results, which
 	 * would open new window to get concepts of AC
 	 * 
 	 * @param cdName
 	 *            String conceptual domain name
 	 * @param acName
 	 *            String ac name
 	 * @param vRes
 	 *            display result vector
 	 * @return vector of modified display result vector
 	 * @throws java.lang.Exception
 	 */
 	public Vector addMultiRecordConDomain(String cdName, String acName, Vector<String> vRes,
 			HttpServletRequest httpRequest, VMForm data) throws Exception
 	{
 
 		String hyperText = "";
 		if (cdName != null && !cdName.equals(""))
 		{
 			// call the api to return concept attributes according to ac type
 			// and ac idseq
 			Vector cdList = new Vector();
 			cdList =
 					this.doCDSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "",
 							"", acName, cdList, data); // get
 			// the
 			// list
 			// of
 			// Conceptual
 			// Domains
 			httpRequest.setAttribute("ConDomainList", cdList);
 			httpRequest.setAttribute("VMName", acName);
 			UtilService util = new UtilService();
 			acName = util.parsedStringSingleQuote(acName);
 			acName = util.parsedStringDoubleQuoteJSP(acName);
 			String strdetails = "";
 			if (cdList != null && cdList.size() > 0)
 			{
 				CD_Bean cd = (CD_Bean) cdList.elementAt(0);
 				strdetails = cd.getCD_LONG_NAME();
 				if (cdList.size() > 1)
 					strdetails += "...";
 			}
 			hyperText =
 					"<a href=" + "\"" + "javascript:openConDomainWindow('" + acName + "')" + "\""
 							+ "><br><b>More_>></b></a>";
 			vRes.addElement(strdetails + "  " + hyperText);
 		}
 		else
 			vRes.addElement("");
 		return vRes;
 	}
 
 	/**
 	 * To get Search results for Conceptual Domain from database called from
 	 * getACKeywordResult. calls oracle stored procedure "{call
 	 * SBREXT_CDE_CURATOR_PKG.SEARCH_CD(CD_IDSEQ, InString, ContID, ContName,
 	 * ASLName, CD_ID, OracleTypes.CURSOR)}" loop through the ResultSet and add
 	 * them to bean which is added to the vector to return
 	 * 
 	 * @param CD_IDSEQ
 	 *            String cd_idseq to filter
 	 * @param InString
 	 *            String search term
 	 * @param ContID
 	 *            String context_idseq
 	 * @param ContName
 	 *            selected context name.
 	 * @param sVersion
 	 *            String version indicator to filter
 	 * @param ASLName
 	 *            selected workflow status name.
 	 * @param sCreatedFrom
 	 *            String from created date
 	 * @param sCreatedTo
 	 *            String to create date
 	 * @param sModifiedFrom
 	 *            String from modified date
 	 * @param sModifiedTo
 	 *            String to modified date
 	 * @param sCreator
 	 *            String creater to filter
 	 * @param sModifier
 	 *            String modifier to filter
 	 * @param CD_ID
 	 *            String public id of cd
 	 * @param sOrigin
 	 *            String origin value to filter
 	 * @param dVersion
 	 *            double value of version number to filter
 	 * @param conName
 	 *            STring concept name or identifier to filter
 	 * @param conID
 	 *            String con idseq to fitler
 	 * @param sVM
 	 *            String value meaning
 	 * @param vList
 	 *            returns Vector of DEbean.
 	 * @return Vector of CD Bean
 	 */
 	public Vector doCDSearch(String CD_IDSEQ, String InString, String ContID, String ContName,
 			String sVersion, String ASLName, String sCreatedFrom, String sCreatedTo,
 			String sModifiedFrom, String sModifiedTo, String sCreator, String sModifier,
 			String CD_ID, String sOrigin, double dVersion, String conName, String conID,
 			String sVM, Vector vList, VMForm data)
 	{
 
 		ResultSet rs = null;
 		CallableStatement cstmt = null;
 		try
 		{
 			if (data.getCurationServlet().getConn() != null)
 			{
 				cstmt =
 						data
 								.getCurationServlet()
 								.getConn()
 								.prepareCall(
 										"{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_CD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				cstmt.registerOutParameter(7, OracleTypes.CURSOR);
 				cstmt.setString(1, InString);
 				cstmt.setString(2, ContID);
 				cstmt.setString(3, ContName);
 				cstmt.setString(4, ASLName);
 				cstmt.setString(5, CD_IDSEQ);
 				cstmt.setString(6, CD_ID);
 				cstmt.setString(8, sCreatedFrom);
 				cstmt.setString(9, sCreatedTo);
 				cstmt.setString(10, sModifiedFrom);
 				cstmt.setString(11, sModifiedTo);
 				cstmt.setString(12, sOrigin);
 				cstmt.setString(13, sCreator);
 				cstmt.setString(14, sModifier);
 				cstmt.setString(15, sVersion);
 				cstmt.setDouble(16, dVersion);
 				cstmt.setString(17, conName);
 				cstmt.setString(18, conID);
 				cstmt.setString(19, util.parsedStringSingleQuoteOracle(sVM));
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				// store the output in the resultset
 				rs = (ResultSet) cstmt.getObject(7);
 				String s;
 				if (rs != null)
 				{
 					// loop through the resultSet and add them to the bean
 					while (rs.next())
 					{
 						CD_Bean CDBean = new CD_Bean();
 						CDBean.setCD_PREFERRED_NAME(rs.getString("preferred_name"));
 						CDBean.setCD_LONG_NAME(rs.getString("long_name"));
 						CDBean.setCD_PREFERRED_DEFINITION(rs.getString("preferred_definition"));
 						CDBean.setCD_ASL_NAME(rs.getString("asl_name"));
 						CDBean.setCD_CONTE_IDSEQ(rs.getString("conte_idseq"));
 						s = rs.getString("begin_date");
 						if (s != null)
 							s = util.getCurationDate(s);
 						CDBean.setCD_BEGIN_DATE(s);
 						s = rs.getString("end_date");
 						if (s != null)
 							s = util.getCurationDate(s);
 						CDBean.setCD_END_DATE(s);
 						// add the decimal number
 						if (rs.getString("version").indexOf('.') >= 0)
 							CDBean.setCD_VERSION(rs.getString("version"));
 						else
 							CDBean.setCD_VERSION(rs.getString("version") + ".0");
 						CDBean.setCD_CD_IDSEQ(rs.getString("cd_idseq"));
 						CDBean.setCD_CHANGE_NOTE(rs.getString("change_note"));
 						CDBean.setCD_CONTEXT_NAME(rs.getString("context"));
 						CDBean.setCD_CD_ID(rs.getString("cd_id"));
 						CDBean.setCD_SOURCE(rs.getString("ORIGIN"));
 						CDBean.setCD_DIMENSIONALITY(rs.getString("dimensionality"));
 						s = rs.getString("date_created");
 						if (s != null)
 							s = util.getCurationDate(s);
 						CDBean.setCD_DATE_CREATED(s);
 						s = rs.getString("date_modified");
 						if (s != null)
 							s = util.getCurationDate(s);
 						CDBean.setCD_DATE_MODIFIED(s);
 						CDBean.setCD_CREATED_BY(rs.getString("created_by"));
 						CDBean.setCD_MODIFIED_BY(rs.getString("modified_by"));
 						vList.addElement(CDBean);
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR - VMAction-CDSearch for other : " + e.toString(), e);
 		}
 		finally
 		{
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return vList;
 	}
 	
     private int getResultSetSize(ResultSet rs) throws SQLException {
     	int size = 0;
     	size = rs.getRow();
     	while(rs.next())
     		size++;
     	
     	return size;
     }
 
 }// end of the class
