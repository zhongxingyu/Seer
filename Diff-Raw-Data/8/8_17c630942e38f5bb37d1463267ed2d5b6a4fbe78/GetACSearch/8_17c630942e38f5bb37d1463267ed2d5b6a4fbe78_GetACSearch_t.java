 // Copyright (c) 2000 ScenPro, Inc.
// $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/GetACSearch.java,v 1.95 2009-05-06 15:51:37 veerlah Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import gov.nih.nci.cadsr.cdecurate.database.Alternates;
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsServlet;
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsSession;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 
 import java.io.Serializable;
 import java.sql.CallableStatement;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.SimpleDateFormat;
 import java.util.Hashtable;
 import java.util.Stack;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import oracle.jdbc.driver.OracleTypes;
 
 import org.apache.log4j.Logger;
 
 /**
  * GetACSearch class is for search action of the tool for all components.
  * <P>
  * 
  * @author Sumana Hegde
  * @version 3.0
  * 
  */
 public class GetACSearch implements Serializable
 {
     private static final long serialVersionUID = 1127639772054694933L;
 
     private CurationServlet  m_servlet        = null;
 
     private UtilService         m_util           = new UtilService();
 
     private HttpServletRequest  m_classReq       = null;
 
     private HttpServletResponse m_classRes       = null;
 
     private static final Logger              logger           = Logger.getLogger(GetACSearch.class);
 
     /**
      * Constructs a new instance.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param CurationServlet
      *            NCICuration servlet object.
      */
     public GetACSearch(HttpServletRequest req, HttpServletResponse res, CurationServlet CurationServlet)
     {
         m_classReq = req;
         m_classRes = res;
         m_servlet = CurationServlet;
     }
 
     /**
      * To start a new search called from CurationServlet. selected component, keyword, selected context, seletcted
      * status parameters of request are stored in session. calls search method for selected component to get the result
      * set from the database. search result from the database is stored in session vector "vACSearch". calls method
      * 'setAttributeValues' to get list of selected attributes at search. calls method 'getRowSelected' to get list of
      * checked rows to display. calls get result method for selected component to get vector of selected attribute and
      * checked rows from the ACSearch vector. final result vector is stored in the session vector "results". calls
      * method 'stackSearchComponents' to store the results, ac in the stack to use it for back button on the searchpage
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     public void getACKeywordResult(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             // capture the duration
             // java.util.Date startDate = new java.util.Date();
             // logger.info(m_servlet.getLogMessage(req, "getACKeywordResult", "starting search", startDate, startDate));
             Vector vAC = new Vector();
             // Vector vS = new Vector();
             SetACService setAC = new SetACService(m_servlet);
             DataManager.setAttribute(session, "sortType", "longName");
             DataManager.setAttribute(session, "serProtoID", "");
             // get the selected Administed component for Search
             String sComponent = (String) req.getParameter("listSearchFor");
             String sSearchAC = (String) session.getAttribute("searchAC");
             
             if (sComponent != null)
                 DataManager.setAttribute(session, "searchAC", sComponent);
             else {
             	if (sSearchAC == null)
                	 	DataManager.setAttribute(session, "searchAC", "DataElement");
             	else
                      DataManager.setAttribute(session, "searchAC", sSearchAC);
                  }	
             sSearchAC = (String) session.getAttribute("searchAC");
             // do the keyword search for the selected component
             String sKeyword = (String) req.getParameter("keyword");
             if(sKeyword == null && sSearchAC.equals("PermissibleValue"))
             	sKeyword = (String)session.getAttribute("serKeyword");
             if (sKeyword != null)
             {
                 sKeyword = sKeyword.trim();
                 // call the method to set the attribute checked values
                 setAttributeValues(req, res, sSearchAC, "nothing");
                 DataManager.setAttribute(session, "serKeyword", sKeyword);
                 DataManager.setAttribute(session, "LastAppendWord", sKeyword);
                 UtilService util = new UtilService();
                 // parse the string to handle single quote
                 sKeyword = util.parsedStringSingleQuoteOracle(sKeyword);
                 // filter by context
                 String sContext = "";
                 sContext = getMultiReqValues(sSearchAC, "MainSearch", "Context");
                 // filter by contextUse
                 String sContextUse = (String) req.getParameter("rContextUse");
                 DataManager.setAttribute(session, "serContextUse", sContextUse);
                 if (sContextUse == null)
                     sContextUse = "";
                 // filter by version
                 String sVersion = (String) req.getParameter("rVersion");
                 DataManager.setAttribute(session, "serVersion", sVersion);
                 // get the version number if other
                 String txVersion = "";
                 if (sVersion != null && sVersion.equals("Other"))
                     txVersion = (String) req.getParameter("tVersion");
                 if (txVersion == null)
                     txVersion = "";
                 DataManager.setAttribute(session, "serVersionNum", txVersion);
                 double dVersion = 0;
                 // validate the version and display message if not valid
                 if (!txVersion.equals(""))
                 {
                     // String sValid = setAC.checkVersionDimension(txVersion);
                     String sValid = setAC.checkValueIsNumeric(txVersion, "Version");
                     if (sValid != null && !sValid.equals(""))
                     {
                         logger.error("not a good version " + sValid);
                         DataManager.setAttribute(session, "serVersionNum", "0");
                     }
                     else
                     {
                         Double DVersion = new Double(txVersion);
                         dVersion = DVersion.doubleValue();
                     }
                 }
                 // make sVersion to empty if all or other
                 if (sVersion == null || sVersion.equals("All") || sVersion.equals("Other"))
                     sVersion = "";
                 // filter by value domain type enumerated
                 String sVDType = "";
                 String sVDTypeEnum = (String) req.getParameter("enumBox");
                 DataManager.setAttribute(session, "serVDTypeEnum", sVDTypeEnum);
                 if (sVDTypeEnum != null)
                     sVDType = "E";
                 // filter by value domain type non enumerated
                 String sVDTypeNonEnum = (String) req.getParameter("nonEnumBox");
                 DataManager.setAttribute(session, "serVDTypeNonEnum", sVDTypeNonEnum);
                 if (sVDTypeNonEnum != null && !sVDTypeNonEnum.equals(""))
                 {
                     if (!sVDType.equals(""))
                         sVDType = sVDType + ", ";
                     sVDType = sVDType + "N";
                 }
                 // filter by value domain type enumerated by reference
                 String sVDTypeRef = (String) req.getParameter("refEnumBox");
                 DataManager.setAttribute(session, "serVDTypeRef", sVDTypeRef);
                 if (sVDTypeRef != null && !sVDTypeRef.equals(""))
                 {
                     if (!sVDType.equals(""))
                         sVDType = sVDType + ", ";
                     sVDType = sVDType + "R";
                 }
                 sVDType = sVDType.trim();
                 String sSchemes = "";
                 sSchemes = (String) req.getParameter("listCSName");
                 if (sSchemes != null && sSchemes.equalsIgnoreCase("AllSchemes"))
                     sSchemes = "";
                 DataManager.setAttribute(session, "selCS", sSchemes);
                 String sCDid = "";
                 sCDid = (String) req.getParameter("listCDName");
                 DataManager.setAttribute(session, "serSelectedCD", sCDid);
                 if (sCDid != null && sCDid.equalsIgnoreCase("All Domains"))
                     sCDid = "";
                 // filter by workflow status
                 boolean isBlock = false;
                 if (sSearchAC.equals("ObjectClass") || sSearchAC.equals("Property") || sSearchAC.equals("ConceptClass"))
                     isBlock = true;
                 String sStatus = getStatusValues(req, res, sSearchAC, "MainSearch", isBlock);
                 if ((sStatus == null) || sStatus.equals("AllStatus"))
                     sStatus = "";
                 // filter by registration status
                 String sRegStatus = (String) req.getParameter("listRegStatus");
                 DataManager.setAttribute(session, "serRegStatus", sRegStatus);
                 if (sRegStatus == null || sRegStatus.equals("allReg"))
                     sRegStatus = "";
                 // filter by derivation type
                 String sDerType = (String) req.getParameter("listDeriveType");
                 DataManager.setAttribute(session, "serDerType", sDerType);
                 if (sDerType == null || sDerType.equals("allDer"))
                     sDerType = "";
                 // filter by domain data type
                 String sDataType = (String) req.getParameter("listDataType");
                 DataManager.setAttribute(session, "serDataType", sDataType);
                 if (sDataType == null || sDataType.equals("allData"))
                     sDataType = "";
                 // filter by date created from date
                 String sCreatedFrom = (String) req.getParameter("createdFrom");
                 DataManager.setAttribute(session, "serCreatedFrom", sCreatedFrom);
                 if (sCreatedFrom == null)
                     sCreatedFrom = "";
                 // filter by date created To date
                 String sCreatedTo = (String) req.getParameter("createdTo");
                 DataManager.setAttribute(session, "serCreatedTo", sCreatedTo);
                 if (sCreatedTo == null)
                     sCreatedTo = "";
                 // filter by date Modified from date
                 String sModifiedFrom = (String) req.getParameter("modifiedFrom");
                 DataManager.setAttribute(session, "serModifiedFrom", sModifiedFrom);
                 if (sModifiedFrom == null)
                     sModifiedFrom = "";
                 // filter by date Modified To date
                 String sModifiedTo = (String) req.getParameter("modifiedTo");
                 DataManager.setAttribute(session, "serModifiedTo", sModifiedTo);
                 if (sModifiedTo == null)
                     sModifiedTo = "";
                 // filter by Modifier type
                 String sModifier = (String) req.getParameter("modifier");
                 DataManager.setAttribute(session, "serModifier", sModifier);
                 if ((sModifier == null) || sModifier.equals("allUsers"))
                     sModifier = "";
                 // filter by Creator type
                 String sCreator = (String) req.getParameter("creator");
                 DataManager.setAttribute(session, "serCreator", sCreator);
                 if ((sCreator == null) || sCreator.equals("allUsers"))
                     sCreator = "";
                 // get value of search in
                 String sSearchIn = (String) req.getParameter("listSearchIn");
                 if (sSearchIn == null)
                     sSearchIn = "longName";
                 DataManager.setAttribute(session, "serSearchIn", sSearchIn);
                 // search in by public id
                 if (sSearchIn.equals("minID"))
                 {
                     String sMinID = sKeyword;
                      // if ((sMinID != "") && sSearchAC.equals("DataElement"))
                     // sMinID could be empty when search all Public ID for, example, a creator
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "",
                                         "", sMinID, "", "", "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                     }
                     else if (sSearchAC.equals("DataElementConcept"))
                     {
                         doDECSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, sMinID, "", "", "", dVersion, sCDid, "", "",
                                         "", "", vAC);
                     }
                     else if (sSearchAC.equals("ValueDomain"))
                     {
                         doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, sMinID, "", "", dVersion,
                                         sCDid, "", "", "", "", "", "", sDataType, vAC);
                     }
                     else if (sSearchAC.equals("ConceptualDomain"))
                     {
                         doCDSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, sMinID, "", dVersion, "", "", "", vAC);
                     }
                     else if (sSearchAC.equals("ValueMeaning"))
                     {
                         VMServlet vmSer = new VMServlet(req, res, m_servlet);
                         vmSer.readDataForSearch(this);
                         vAC = (Vector)session.getAttribute("vACSearch");
                     }
                     else if (sSearchAC.equals("ObjectClass"))
                         do_caDSRSearch("", sContext, sStatus, sMinID, vAC, "OC", "", "");
                     else if (sSearchAC.equals("Property"))
                         do_caDSRSearch("", sContext, sStatus, sMinID, vAC, "PROP", "", "");
                     else if (sSearchAC.equals("ConceptClass"))
                         vAC = do_ConceptSearch("", "", sContext, sStatus, sMinID, "", "", vAC);
                 }
                 // call function if seach in is crfName
                 else if (sSearchIn.equals("CRFName"))
                 {
                     // do the keyword search for the selected component
                     String sProtoID = (String) req.getParameter("protoKeyword");
                     DataManager.setAttribute(session, "serProtoID", sProtoID);
                     sProtoID = util.parsedStringSingleQuoteOracle(sProtoID);
                     // crf name is keyword
                     String sCRFName = sKeyword;
                     // crf search if search ac is data element
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "",
                                         "", "", "", "", "", sProtoID, sCRFName, "", "", "", "", "", "", "", "", sDerType,
                                         vAC);
                     }
                     // doDE_CRFSearch(sProtoID, sKeyword, sContext, sVersion, sStatus, sRegStatus,
                     // sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, vAC);
                 }
                 // search in by doc text
                 else if (sSearchIn.equals("docText"))
                 {
                     String sDocs[] = req.getParameterValues("listRDType");
                     String sDocTypes = "ALL";
                     if (sDocs != null && sDocs.length > 0)
                         sDocTypes = getDocTypeValues(sDocs, "MainSearch");
                     String sDocText = sKeyword;
                     // if (sDocText == null || sDocText.equals(""))
                     // sDocText = "*";
                     // if ((sDocText != "") && sSearchAC.equals("DataElement"))
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier,
                                         sDocText, sDocTypes, "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                         sDerType, vAC);
                     }
                 }
                 // search in by names adn doc text long name and historic short cde name
                 else if (sSearchIn.equals("NamesAndDocText"))
                 {
                     String sDocTypes = "Preferred Question Text, HISTORIC SHORT CDE NAME";
                     String sDocText = sKeyword;
                     // if (sDocText == null || sDocText.equals(""))
                     // sDocText = "*";
                     // if ((sDocText != "") && sSearchAC.equals("DataElement"))
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", sKeyword, "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier,
                                         sDocText, sDocTypes, "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                         sDerType, vAC);
                     }
                 }
                 // search in by historical cde id
                 else if (sSearchIn.equals("histID"))
                 {
                     String sHistID = sKeyword;
                     // if ((sHistID != "") && sSearchAC.equals("DataElement"))
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "",
                                         "", "", sHistID, "", "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                     }
                 }
                 // search in by permissible value
                 else if (sSearchIn.equals("permValue"))
                 {
                     String permValue = sKeyword;
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "",
                                         "", "", "", permValue, "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                     }
                     else if (sSearchAC.equals("ValueDomain"))
                     {
                         doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, "", permValue, "", dVersion,
                                         sCDid, "", "", "", "", "", "", sDataType, vAC);
                     }
                 }
                 // search in by origin
                 else if (sSearchIn.equals("origin"))
                 {
                     String sOrigin = sKeyword;
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "",
                                         "", "", "", "", sOrigin, "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                     }
                     else if (sSearchAC.equals("DataElementConcept"))
                     {
                         doDECSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, "", "", "", sOrigin, dVersion, sCDid, "", "",
                                         "", "", vAC);
                     }
                     else if (sSearchAC.equals("ValueDomain"))
                     {
                         doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", sOrigin, dVersion,
                                         sCDid, "", "", "", "", "", "", sDataType, vAC);
                     }
                     else if (sSearchAC.equals("ConceptualDomain"))
                     {
                         doCDSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, "", sOrigin, dVersion, "", "", "", vAC);
                     }
                 }
                 // search in by conName
                 else if (sSearchIn.equals("concept"))
                 {
                     String sCon = sKeyword;
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "",
                                         "", "", "", "", "", "", "", "", "", "", "", "", "", "", sCon, sDerType, vAC);
                     }
                     else if (sSearchAC.equals("DataElementConcept"))
                     {
                         doDECSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, "", "", "", "", dVersion, sCDid, "", "", "",
                                         sCon, vAC);
                     }
                     else if (sSearchAC.equals("ValueDomain"))
                     {
                         doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", "", dVersion, sCDid,
                                         "", "", "", "", "", sCon, sDataType, vAC);
                     }
                     else if (sSearchAC.equals("ConceptualDomain"))
                     {
                         doCDSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, "", "", dVersion, sCon, "", "", vAC);
                     }
                     else if (sSearchAC.equals("PermissibleValue"))
                     {
                         PVServlet pvser = new PVServlet(m_classReq, m_classRes, m_servlet);
                         vAC = pvser.searchPVAttributes("", sCDid, sCon, "");
                     }
                     else if (sSearchAC.equals("ValueMeaning"))
                     {
                         VMServlet vmSer = new VMServlet(req, res, m_servlet);
                         vmSer.readDataForSearch(this);
                         vAC = (Vector)session.getAttribute("vACSearch");
                     }
                     else if (sSearchAC.equals("ObjectClass"))
                         do_caDSRSearch("", sContext, sStatus, "", vAC, "OC", sCon, "");
                     else if (sSearchAC.equals("Property"))
                         do_caDSRSearch("", sContext, sStatus, "", vAC, "PROP", sCon, "");
                     // search pref name if evs identifier
                     else if (sSearchAC.equals("ConceptClass"))
                         vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 }
                 // search in by names and definitions
                 else
                 {
                     // call the method to get the data from the database
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", sKeyword, "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "",
                                         "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                     }
                     else if (sSearchAC.equals("DataElementConcept"))
                     {
                         doDECSearch("", sKeyword, "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", "", "", dVersion,
                                         sCDid, "", "", "", "", vAC);
                     }
                     else if (sSearchAC.equals("ValueDomain"))
                     {
                         doVDSearch("", sKeyword, "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", "", dVersion, sCDid,
                                         "", "", "", "", "", "", sDataType, vAC);
                     }
                     else if (sSearchAC.equals("PermissibleValue"))
                     {
                         PVServlet pvser = new PVServlet(m_classReq, m_classRes, m_servlet);
                         vAC = pvser.searchPVAttributes(sKeyword, sCDid, "", "");
                     }
                     else if (sSearchAC.equals("ObjectClass"))
                     {
                         do_caDSRSearch(sKeyword, sContext, sStatus, "", vAC, "OC", "", "");
                     }
                     else if (sSearchAC.equals("Property"))
                     {
                         do_caDSRSearch(sKeyword, sContext, sStatus, "", vAC, "PROP", "", "");
                     }
                     else if (sSearchAC.equals("ConceptualDomain"))
                     {
                         doCDSearch("", sKeyword, "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", dVersion, "", "", "",
                                         vAC);
                     }
                     else if (sSearchAC.equals("ValueMeaning"))
                     {
                     	initializeStack(session);
                     	VMServlet vmSer = new VMServlet(req, res, m_servlet);
                         vmSer.readDataForSearch(this);
                         vAC = (Vector)session.getAttribute("vACSearch");
                     }
                     else if (sSearchAC.equals("ClassSchemeItems"))
                         doCSISearch(sKeyword, sContext, sSchemes, vAC);
                     else if (sSearchAC.equals("ConceptClass"))
                         vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 }
                 // append the searched data with old data.
                 String AppendAction = (String) session.getAttribute("AppendAction");
                 if (AppendAction != null && AppendAction.equals("Appended"))
                 {
                     DataManager.setAttribute(session, "newSearchRes", vAC);
                     getRowSelected(req, res, true);
                     DataManager.setAttribute(session, "AppendAction", "Was Appended");
                 }
                 else
                 {
                     DataManager.setAttribute(session, "AppendAction", "Not Appended");
                     DataManager.setAttribute(session, "vSelRows", vAC);
                     Vector vCheckList = new Vector();
                     DataManager.setAttribute(session, "CheckList", vCheckList);
                 }
                 // do ID's up here because it will need an existing ID stack first time into getResult
                 if (sSearchAC.equals("DataElement") || sSearchAC.equals("DataElementConcept")
                                 || sSearchAC.equals("ValueDomain") || sSearchAC.equals("ConceptualDomain")
                                 || sSearchAC.equals("ObjectClass") || sSearchAC.equals("Property")
                                 || sSearchAC.equals("ClassSchemeItems") || sSearchAC.equals("PermissibleValue")
                                 || sSearchAC.equals("ConceptClass"))
                 {
                    initializeStack(session);
                     EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
                     // call method to get the final result vector
                     Vector vResult = new Vector();                             	
                     if (sSearchAC.equals("DataElement"))
                         getDEResult(req, res, vResult, "");
                     else if (sSearchAC.equals("DataElementConcept"))
                         getDECResult(req, res, vResult, "");
                     else if (sSearchAC.equals("ValueDomain"))
                         getVDResult(req, res, vResult, "");
                     else if (sSearchAC.equals("PermissibleValue"))
                         getPVVMResult(req, res, vResult, "");
                     else if (sSearchAC.equals("ObjectClass"))
                         evs.get_Result(req, res, vResult, "");
                     else if (sSearchAC.equals("Property"))
                         evs.get_Result(req, res, vResult, "");
                     else if (sSearchAC.equals("Questions"))
                         getQuestionResult(req, res, vResult);
                     else if (sSearchAC.equals("ConceptualDomain"))
                         getCDResult(req, res, vResult, "");
                     else if (sSearchAC.equals("ClassSchemeItems"))
                         getCSIResult(req, res, vResult, "");
                     else if (sSearchAC.equals("ConceptClass"))
                         evs.get_Result(req, res, vResult, "");
                     // store result vector in the attribute
                     DataManager.setAttribute(session, "results", vResult);
                 }
             }
             // capture the duration
             // logger.info(m_servlet.getLogMessage(req, "getKeywordResult", "ending search", startDate, new
             // java.util.Date()));
         }    	
         
         catch (Exception e)
         {
             logger.error("ERROR - GetACSearch-getKeywordResult!! : " + e.toString(), e);
         }
     }
 
     /**
      * Initializes the Stack 
      * @param session
      */
     private void initializeStack(HttpSession session)
     {
     	 Stack vSearchIDStack = new Stack();
          DataManager.setAttribute(session, "vSearchIDStack", vSearchIDStack);
          Stack vSearchNameStack = new Stack();
          DataManager.setAttribute(session, "vSearchNameStack", vSearchNameStack);
          Stack vSearchLongNameStack = new Stack();
          DataManager.setAttribute(session, "vSearchLongNameStack", vSearchLongNameStack);
          Stack vSearchUsedContextStack = new Stack();
          DataManager.setAttribute(session, "vSearchUsedContextStack", vSearchUsedContextStack);
          Stack sSearchACStack = new Stack();
          DataManager.setAttribute(session, "sSearchACStack", sSearchACStack);
          Stack vACSearchStack = new Stack();
          DataManager.setAttribute(session, "vACSearchStack", vACSearchStack);
          Stack vSearchASLStack = new Stack();
          DataManager.setAttribute(session, "vSearchASLStack", vSearchASLStack);
          Stack vSelRowsStack = new Stack();
          DataManager.setAttribute(session, "vSelRowsStack", vSelRowsStack);
          Stack vResultStack = new Stack();
          DataManager.setAttribute(session, "vResultStack", vResultStack);
          Stack vCompAttrStack = new Stack();
          DataManager.setAttribute(session, "vCompAttrStack", vCompAttrStack);
          Stack vAttributeListStack = new Stack();
          DataManager.setAttribute(session, "vAttributeListStack", vAttributeListStack);
     }
     /**
      * Adds the search component, result bean vector and result vector in a stack to use it later.
      * 
      * @param sSearchACStack
      *            string of ACs searched
      * @param vACSearchStack
      *            vector of ac beans searched
      * @param vSelRowsStack
      *            vector of ac beans of selected rows
      * @param vSearchIDStack
      *            vector of ids of the searched ac
      * @param vSearchNameStack
      *            vector of names of the searched ac
      * @param vSearchUsedContextStack
      *            vector of contexts of the search result
      * @param vResultStack
      *            vector of search result used in display
      * @param vCompAttrStack
      *            vector of component attributes used in display
      * @param vAttributeListStack
      *            vector of selected attirbute list
      * @param sSearchAC
      *            the name of the selected component
      * @param vAC
      *            the vector of bean from the result
      * @param vRSel
      *            the vector of selected results
      * @param vSearchID
      *            the vector of id's
      * @param vSearchName
      *            the vector of names
      * @param vResult
      *            result vector used to display.
      * @param vSearchASLStack
      *            vector of work flow status of the result
      * @param vSearchASL
      *            vector of selected asl
      * @param vSearchLongNameStack
      *            vector of long name
      * @param vSearchLongName
      *            vector of long name
      * 
      * @throws Exception
      */
     private void pushAllOntoStack(Stack sSearchACStack, Stack vACSearchStack, Stack vSelRowsStack,
                     Stack vSearchIDStack, Stack vSearchNameStack, Stack vSearchUsedContextStack, Stack vResultStack,
                     Stack vCompAttrStack, Stack vAttributeListStack, String sSearchAC, Vector vAC, Vector vRSel,
                     Vector vSearchID, Vector vSearchName, Vector vResult, Stack vSearchASLStack, Vector vSearchASL,
                     Stack vSearchLongNameStack, Vector vSearchLongName) throws Exception
     {
         try
         {
             HttpSession session = m_classReq.getSession();
             if (sSearchACStack != null)
             {
                 sSearchACStack.push(sSearchAC);
                 DataManager.setAttribute(session, "sSearchACStack", sSearchACStack);
             }
             if (vACSearchStack != null)
             {
                 vACSearchStack.push(vAC);
                 DataManager.setAttribute(session, "vACSearchStack", vACSearchStack);
             }
             if (vSearchASLStack != null)
             {
                 vSearchASLStack.push(vSearchASL);
                 DataManager.setAttribute(session, "vSearchASLStack", vSearchASLStack);
             }
             if (vSelRowsStack != null)
             {
                 vSelRowsStack.push(vRSel);
                 DataManager.setAttribute(session, "vSelRowsStack", vSelRowsStack);
             }
             if (vSearchIDStack != null)
             {
                 vSearchIDStack.push(vSearchID);
                 DataManager.setAttribute(session, "vSearchIDStack", vSearchIDStack);
             }
             if (vSearchNameStack != null)
             {
                 vSearchNameStack.push(vSearchName);
                 DataManager.setAttribute(session, "vSearchNameStack", vSearchNameStack);
             }
             if (vSearchLongNameStack != null)
             {
                 vSearchLongNameStack.push(vSearchLongName);
                 DataManager.setAttribute(session, "vSearchLongNameStack", vSearchLongNameStack);
             }
             if (vResultStack != null)
             {
                 vResultStack.push(vResult);
                 DataManager.setAttribute(session, "vResultStack", vResultStack);
             }
             Vector vCompAttr = new Vector();
             String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (sMenu.equals("searchForCreate"))
                 vCompAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vCompAttr = (Vector) session.getAttribute("selectedAttr");
             if (vCompAttrStack != null && vCompAttr != null)
             {
                 vCompAttrStack.push(vCompAttr);
                 DataManager.setAttribute(session, "vCompAttrStack", vCompAttrStack);
             }
             Vector vACAttr = new Vector();
             if (sMenu.equals("searchForCreate"))
                 vACAttr = (Vector) session.getAttribute("creAttributeList");
             else
                 vACAttr = (Vector) session.getAttribute("serAttributeList");
             if (vAttributeListStack != null && vACAttr != null)
             {
                 vAttributeListStack.push(vACAttr);
                 DataManager.setAttribute(session, "vAttributeListStack", vAttributeListStack);
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-pushAllOntoStack: " + e);
             logger.error("ERROR in GetACSearch-pushAllOntoStack : " + e.toString(), e);
         }
     }
 
     /**
      * Adds the search component, result bean vector and result vector in a stack to use it later.
      * 
      * @param sSearchAC
      *            the name of the selected component
      * @param vAC
      *            the vector of bean from the result
      * @param vRSel
      *            the vector of selected results
      * @param vSearchID
      *            the vector of id's
      * @param vSearchName
      *            the vector of names
      * @param vResult
      *            result vector used to display.
      * @param vSearchASL
      *            result vector used to display.
      * 
      * @throws Exception
      */
     public void stackSearchComponents(String sSearchAC, Vector vAC, Vector vRSel, Vector vSearchID, Vector vSearchName,
                     Vector vResult, Vector vSearchASL, Vector vSearchLongName) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         String sSearchAC2 = "";
         // for Back button, put search results on a stack
         if (sSearchAC.equals("DataElement") || sSearchAC.equals("DataElementConcept")
                         || sSearchAC.equals("ValueDomain") || sSearchAC.equals("ConceptualDomain")
                         || sSearchAC.equals("ObjectClass") || sSearchAC.equals("Property")
                         || sSearchAC.equals("ConceptClass") || sSearchAC.equals("ClassSchemeItems")
                         || sSearchAC.equals("PermissibleValue")|| sSearchAC.equals("ValueMeaning"))
         {
             try
             {
                 // for Back button, put search results on a stack
                 Stack sSearchACStack = (Stack) session.getAttribute("sSearchACStack");
                 Stack vACSearchStack = (Stack) session.getAttribute("vACSearchStack");
                 Stack vSearchASLStack = (Stack) session.getAttribute("vSearchASLStack");
                 Stack vSelRowsStack = (Stack) session.getAttribute("vSelRowsStack");
                 Stack vSearchIDStack = (Stack) session.getAttribute("vSearchIDStack");
                 Stack vSearchNameStack = (Stack) session.getAttribute("vSearchNameStack");
                 Stack vSearchLongNameStack = (Stack) session.getAttribute("vSearchLongNameStack");
                 Stack vSearchUsedContextStack = (Stack) session.getAttribute("vSearchUsedContextStack");
                 Stack vResultStack = (Stack) session.getAttribute("vResultStack");
                 Stack vCompAttrStack = (Stack) session.getAttribute("vCompAttrStack");
                 Stack vAttributeListStack = (Stack) session.getAttribute("vAttributeListStack");
                 if (sSearchACStack != null && sSearchACStack.size() > 0)
                 {
                     sSearchAC2 = (String) sSearchACStack.peek();
                 }
                 if (sSearchAC2 != null && sSearchAC2.equals(sSearchAC))
                 {
                     // pop all the others
                     if (sSearchACStack != null && sSearchACStack.size() > 0)
                         sSearchAC2 = (String) sSearchACStack.pop();
                     if (vACSearchStack != null && vACSearchStack.size() > 0)
                         vACSearchStack.pop();
                     if (vSearchASLStack != null && vSearchASLStack.size() > 0)
                         vSearchASLStack.pop();
                     if (vSelRowsStack != null && vSelRowsStack.size() > 0)
                         vSelRowsStack.pop();
                     if (vSearchIDStack != null && vSearchIDStack.size() > 0)
                         vSearchIDStack.pop();
                     if (vSearchNameStack != null && vSearchNameStack.size() > 0)
                         vSearchNameStack.pop();
                     if (vSearchLongNameStack != null && vSearchLongNameStack.size() > 0)
                         vSearchLongNameStack.pop();
                     if (vSearchUsedContextStack != null && vSearchUsedContextStack.size() > 0)
                         vSearchUsedContextStack.pop();
                     if (vResultStack != null && vResultStack.size() > 0)
                         vResultStack.pop();
                     if (vCompAttrStack != null && vCompAttrStack.size() > 0)
                         vCompAttrStack.pop();
                     if (vAttributeListStack != null && vAttributeListStack.size() > 0)
                         vAttributeListStack.pop();
                     // then push All new ones onto stack
                     pushAllOntoStack(sSearchACStack, vACSearchStack, vSelRowsStack, vSearchIDStack, vSearchNameStack,
                                     vSearchUsedContextStack, vResultStack, vCompAttrStack, vAttributeListStack,
                                     sSearchAC, vAC, vRSel, vSearchID, vSearchName, vResult, vSearchASLStack,
                                     vSearchASL, vSearchLongNameStack, vSearchLongName);
                 }
                 else if (sSearchAC2 != null && sSearchACStack != null)
                 {
                     // push All Others
                     pushAllOntoStack(sSearchACStack, vACSearchStack, vSelRowsStack, vSearchIDStack, vSearchNameStack,
                                     vSearchUsedContextStack, vResultStack, vCompAttrStack, vAttributeListStack,
                                     sSearchAC, vAC, vRSel, vSearchID, vSearchName, vResult, vSearchASLStack,
                                     vSearchASL, vSearchLongNameStack, vSearchLongName);
                 }
             }
             catch (Exception e)
             {
                 // System.err.println("ERROR in GetACSearch-stackSearchComponents: " + e);
                 logger.error("ERROR in GetACSearch-stackSearchComponents : " + e.toString(), e);
             }
         }
     }
 
     /**
      * To get selected attributes or rows to display, called from CurationServlet. calls method 'setAttributeValues'
      * to get list of selected attributes at search. calls method 'getRowSelected' to get list of checked rows to
      * display. calls method 'getDEResult' for selected component to get vector of selected attribute and checked rows
      * from the ACSearch vector. final result vector is stored in the session vector "results".
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param actType
      *            selected action type.
      * 
      */
     public void getACShowResult(HttpServletRequest req, HttpServletResponse res, String actType)
     {
         try
         {
             HttpSession session = req.getSession();
             String sSearchAC = "";
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             // do the keyword search for the selected component
             String sKeyword = (String) req.getParameter("keyword");
             // String sComponent = "";
             if (menuAction.equals("searchForCreate"))
                 sSearchAC = (String) session.getAttribute("creSearchAC");
             else
                 sSearchAC = (String) session.getAttribute("searchAC");
             // selected more attributes from the display attribute list.
             if (actType.equals("Attribute"))
                 setAttributeValues(req, res, sSearchAC, menuAction);
             // store the append action in the session to handle new search
             String sAppendAct = (String) req.getParameter("AppendAction");
             if (sAppendAct != null)
             {
                 DataManager.setAttribute(session, "AppendAction", sAppendAct);
                 // save the last word in the request attribute
                 DataManager.setAttribute(session, "LastAppendWord", (String) session.getAttribute("serKeyword"));
                //DataManager.setAttribute(session, "serKeyword", "");
             }
             // call the method to get the selected rows
             if (menuAction.equals("BEDisplay"))
                 getRowSelectedBEDisplay(req, res);
             else if (actType.equals("Monitor"))
                 getRowSelected(req, res, false);
             else if (actType.equals("Unmonitor"))
                 getRowSelected(req, res, false);
             // get checked rows for data element in all cases and all others only for search for create
             else if (sSearchAC.equals("DataElement")
                             || (!menuAction.equals("searchForCreate") && !actType.equals("Attribute")))
                 getRowSelected(req, res, false);
             EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
             boolean isBlockSearch = false;
             String dtsVocab = req.getParameter("listContextFilterVocab");
             if (dtsVocab != null && !dtsVocab.equals(""))
                 isBlockSearch = true;
             // call method to get the final result vector
             Vector<String> vResult = new Vector<String>();
             // get the final result for selected component
             if (sSearchAC.equals("DataElement"))
                 getDEResult(req, res, vResult, "");
             else if (sSearchAC.equals("DataElementConcept"))
                 getDECResult(req, res, vResult, "");
             else if (sSearchAC.equals("ValueDomain"))
                 getVDResult(req, res, vResult, "");
             else if (sSearchAC.equals("PermissibleValue"))
                 getPVVMResult(req, res, vResult, "");
             else if (sSearchAC.equals("Questions"))
                 getQuestionResult(req, res, vResult);
             else if (sSearchAC.equals("ConceptualDomain"))
                 getCDResult(req, res, vResult, "");
             else if (sSearchAC.equals("ClassSchemeItems"))
                 getCSIResult(req, res, vResult, "");
             else if (sSearchAC.equals("ValueMeaning"))
             {
             	 VMServlet vmSer = new VMServlet(req, res, m_servlet);
             	 VMForm vmform = vmSer.getVmData();
             	 vmform.setVMList((Vector)session.getAttribute("vSelRows"));
             	 vmform.setSearchTerm(sKeyword);
             	 if ((actType.equals("Attribute")&& !menuAction.equals("searchForCreate"))||(actType.equals("")&& !menuAction.equals("searchForCreate")))
             	    	//store the attributes to display in the vmData
             		 vmform.setSelAttrList((Vector)session.getAttribute("selectedAttr"));
             	    else
             	    	//store the attributes to display in the vmData
             	    	vmform.setSelAttrList((Vector)session.getAttribute("creSelectedAttr"));
             	 DataManager.setAttribute(session, "serKeyword", sKeyword);
             	 VMAction vmaction = new VMAction();
             	 vmaction.getVMResult(vmform,req,vResult,this);
             }
             else if (sSearchAC.equals("ObjectClass") || sSearchAC.equals("Property")
                             || sSearchAC.equals("ConceptClass"))
                 evs.get_Result(req, res, vResult, "");
             else if (isBlockSearch == true)
                 evs.get_Result(req, res, vResult, "");
             if (actType.equals("BEDisplayRows"))
                 DataManager.setAttribute(session, "resultsBEDisplay", vResult);
             else
                 DataManager.setAttribute(session, "results", vResult);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR - in GetACSearch-showResult: " + e);
             logger.error("ERROR - GetACSearch-showResult : " + e.toString(), e);
         }
     }
 
     /**
      * Refresh the search results.
      * 
      * @param req
      *            Servlet request.
      * @param res
      *            Servlet response.
      * @param actType
      *            The action string.
      */
     public void getACShowResult2(HttpServletRequest req, HttpServletResponse res, String actType)
     {
         HttpSession session = req.getSession();
         String sSearchAC = "";
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         if (menuAction.equals("searchForCreate"))
             sSearchAC = (String) session.getAttribute("creSearchAC");
         else
             sSearchAC = (String) session.getAttribute("searchAC");
         /*
          * EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet); boolean isBlockSearch = false; String
          * dtsVocab = req.getParameter("listContextFilterVocab"); if (dtsVocab != null && !dtsVocab.equals(""))
          * isBlockSearch = true;
          */
         // call method to get the final result vector
         Vector<String> vResult = new Vector<String>();
         // get the final result for selected component
         if (sSearchAC.equals("DataElement"))
             getDEResult(req, res, vResult, "");
         else if (sSearchAC.equals("DataElementConcept"))
             getDECResult(req, res, vResult, "");
         else if (sSearchAC.equals("ValueDomain"))
             getVDResult(req, res, vResult, "");
         else if (sSearchAC.equals("PermissibleValue"))
             getPVVMResult(req, res, vResult, "");
         else if (sSearchAC.equals("Questions"))
             getQuestionResult(req, res, vResult);
         else if (sSearchAC.equals("ConceptualDomain"))
             getCDResult(req, res, vResult, "");
         else if (sSearchAC.equals("ClassSchemeItems"))
             getCSIResult(req, res, vResult, "");
         // else if (sSearchAC.equals("ObjectClass") || sSearchAC.equals("Property"))
         // evs.get_Result(req, res, vResult, "");
         // else if (isBlockSearch == true)
         // evs.get_Result(req, res, vResult, "");
         if (actType.equals("BEDisplayRows"))
             DataManager.setAttribute(session, "resultsBEDisplay", vResult);
         else
             DataManager.setAttribute(session, "results", vResult);
     }
 
     /**
      * To get the sorted result vector to display for selected sort type, called from CurationServlet. calls method
      * 'getDESortedRows' for the selected component to get sorted bean. calls method 'getDEResult' for the selected
      * component to result vector from the sorted bean. final result vector is stored in the session vector "results".
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     public void getACSortedResult(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
             // get the sort string parameter
             String sSortType = (String) req.getParameter("sortType");
             if (sSortType != null)
                 DataManager.setAttribute(session, "sortType", sSortType);
             String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             String sComponent = "";
             if (sMenuAction.equals("searchForCreate"))
                 sComponent = (String) session.getAttribute("creSearchAC");
             else
                 sComponent = (String) session.getAttribute("searchAC");
             if ((!sComponent.equals("")) && (sComponent != null))
             {
                 Vector<String> vResult = new Vector<String>();
                 if (sComponent.equals("DataElement"))
                 {
                     getDESortedRows(req, res);
                     getDEResult(req, res, vResult, "");
                     // getDEResult(req, res, vResult, "true");
                 }
                 // get final result vector
                 else if (sComponent.equals("DataElementConcept"))
                 {
                     getDECSortedRows(req, res);
                     getDECResult(req, res, vResult, "");
                     // getDECResult(req, res, vResult, "true");
                 }
                 else if (sComponent.equals("ValueDomain"))
                 {
                     getVDSortedRows(req, res);
                     getVDResult(req, res, vResult, "");
                     // getVDResult(req, res, vResult, "true");
                 }
                 else if (sComponent.equals("PermissibleValue"))
                 {
                     getPVVMSortedRows(req, res);
                     getPVVMResult(req, res, vResult, "");
                     // getPVVMResult(req, res, vResult, "true");
                 }
                 else if (sComponent.equals("ValueMeaning"))
                 {
                     VMServlet vmSer = new VMServlet(req, res, m_servlet);
                     vmSer.readDataForSort();
                 }
                 else if (sComponent.equals("Questions"))
                 {
                     getQuestionSortedRows(req, res);
                     getQuestionResult(req, res, vResult);
                 }
                 else if (sComponent.equals("ConceptualDomain"))
                 {
                     getCDSortedRows(req, res);
                     getCDResult(req, res, vResult, "");
                     // getCDResult(req, res, vResult, "true");
                 }
                 else if (sComponent.equals("ClassSchemeItems"))
                 {
                     getCSISortedRows(req, res);
                     getCSIResult(req, res, vResult, "");
                     // getCSIResult(req, res, vResult, "true");
                 }
                 else if (sComponent.equals("ObjectClass"))
                 {
                     getOCSortedRows(req, res);
                     evs.get_Result(req, res, vResult, "");
                     // get_Result(req, res, vResult, "true");
                 }
                 else if (sComponent.equals("Property"))
                 {
                     getPCSortedRows(req, res);
                     evs.get_Result(req, res, vResult, "");
                     // get_Result(req, res, vResult, "true");
                 }
                 else if (sComponent.equals("ConceptClass"))
                 {
                     getEVSSortedRows(req, res);
                     evs.get_Result(req, res, vResult, "");
                 }
                 if (!sComponent.equals("ValueMeaning"))
                     DataManager.setAttribute(session, "results", vResult);
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR - in GetACSearch-sortedResult: " + e);
             logger.error("ERROR - GetACSearch-sortedResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get the Question result vector to display for the user, called from CurationServlet. calls method
      * 'doQuestionSearch' to search the questions. calls method 'setAttributeValues' to get the selected attributes.
      * calls method 'getQuestionSortedRows' for the selected component to get sorted bean. calls method
      * 'getQuestionResult' for the selected component to result vector from the sorted bean. final result vector is
      * stored in the session vector "results".
      * 
      * @throws Exception
      *             e
      */
     public void getACQuestion() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         Vector vResult = new Vector();
         DataManager.setAttribute(session, "searchAC", "Questions");
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "Questions");
         String userName = (String) session.getAttribute("Username");
         // call to search questions
         doQuestionSearch(userName, vResult);
         DataManager.setAttribute(session, "vACSearch", vResult);
         DataManager.setAttribute(session, "vSelRows", vResult);
         // get the selected attributes
         setAttributeValues(m_classReq, m_classRes, "Questions", "nothing");
         // get the final result
         vResult = new Vector();
         getQuestionResult(m_classReq, m_classRes, vResult);
         DataManager.setAttribute(session, "results", vResult);
     }
 
     /**
      * To get resultSet from database for Questions called from getACKeywordResult and Servlet methods.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_DE(DE_IDSEQ, InString, ContID, ContName,
      * ASLName, CDE_ID, OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to Question bean which is added to the vector to return
      * 
      * @param userName
      *            name of the user.
      * @param vList
      *            returns Vector of DEbean.
      * 
      */
     public void doQuestionSearch(String userName, Vector vList)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
             // Create a Callable Statement object.
            if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT_CDE_CURATOR_PKG.SEARCH_QUESTION(?,?,?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
                 cstmt.registerOutParameter(5, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, userName.toUpperCase());
                 cstmt.setString(2, "DRAFT NEW");
                 cstmt.setString(3, null);
                 // Now we are ready to call the stored procedure
                 cstmt.execute();
                 String isValidCRF = (String) cstmt.getString(4);
                 if ((isValidCRF != null) && (isValidCRF.equalsIgnoreCase("ASSIGNED")))
                 {
                     // store the output in the resultset
                     rs = (ResultSet) cstmt.getObject(5);
                     // String s;
                     if (rs != null)
                     {
                         // loop through the resultSet and add them to the bean
                         while (rs.next())
                         {
                             Quest_Bean QuestBean = new Quest_Bean();
                             QuestBean.setPROTO_IDSEQ(rs.getString(1));
                             QuestBean.setPROTOCOL_ID(rs.getString(2));
                             QuestBean.setCRF_IDSEQ(rs.getString(3));
                             QuestBean.setCRF_NAME(rs.getString(4));
                             QuestBean.setQC_IDSEQ(rs.getString(5));
                             QuestBean.setQUEST_NAME(rs.getString(6));
                             QuestBean.setCONTE_IDSEQ(rs.getString(7));
                             QuestBean.setCONTEXT_NAME(rs.getString(8));
                             QuestBean.setASL_NAME(rs.getString(9));
                             QuestBean.setDE_IDSEQ(rs.getString(10));
                             QuestBean.setDE_LONG_NAME(rs.getString(11));
                             QuestBean.setDE_VD_IDSEQ(rs.getString(12));
                             QuestBean.setVD_IDSEQ(rs.getString(13));
                             QuestBean.setVD_LONG_NAME(rs.getString(14));
                             QuestBean.setVD_PREF_NAME(rs.getString(15));
                             QuestBean.setVD_DEFINITION(rs.getString(16));
                             QuestBean.setCDE_ID(rs.getString(17));
                             QuestBean.setHIGH_LIGHT_INDICATOR(rs.getString(18));
                             QuestBean.setQC_ID(rs.getString(19));
                             QuestBean.setQUEST_ORIGIN(rs.getString(20));
                             String sDE = rs.getString(10);
                             String sDE_VD = rs.getString(12);
                             String sCRF_VD = rs.getString(13);
                             if (sDE == null || sDE.equals(""))
                                 QuestBean.setSTATUS_INDICATOR("New");
                             else
                             {
                                 // open the template if value domains are not same for both options.
                                 if ((sCRF_VD != null) && !sDE_VD.equals(sCRF_VD))
                                     QuestBean.setSTATUS_INDICATOR("Template");
                                 else
                                     QuestBean.setSTATUS_INDICATOR("Edit");
                             }
                             vList.addElement(QuestBean);
                         }
                     }
                 }
             }
         }
         catch (Exception e)
         {
             logger.error("ERROR - GetACSearch-QuestionSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
     }
 
     /**
      * To get resultSet from database for DataElement Component called from getACKeywordResult and getCDEIDSearch
      * methods.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_DE(DE_IDSEQ, InString, ContID, ContName,
      * ASLName, CDE_ID, OracleTypes.CURSOR, regStatus, origin, createdFrom, Createdto, ModifiedFrom, modifiedTo,
      * creator, modifier, permvalue, docText, histID, sVersion, doctypes, contextUse)}"
      * 
      * loop through the ResultSet and add them to bean which is added to the vector to return if the record is added to
      * the vector already, updates only the used by context to display all used by contexts in a row.
      * 
      * @param DE_IDSEQ
      *            Data Element's idseq.
      * @param InString
      *            Keyword value, is empty if searchIn is minID.
      * @param ContID
      *            Selected Context IDseq.
      * @param ContName
      *            selected context name.
      * @param ContUse
      *            string is either owned, used, or both.
      * @param sVersion
      *            String yes for latest version filter or null for all version filter.
      * @param dVersion
      *            double typed version number.
      * @param ASLName
      *            selected workflow status name.
      * @param regStatus
      *            String selected Registration Status.
      * @param sCreatedFrom
      *            String created from date filter.
      * @param sCreatedTo
      *            String created to date filter.
      * @param sModifiedFrom
      *            String modified from date filter.
      * @param sModifiedTo
      *            String modified to date filter.
      * @param sCreator
      *            String selected creator.
      * @param sModifier
      *            String selected modifier.
      * @param DocText
      *            String keyword when search in is reference document types
      * @param docTypes
      *            String comma delimited values of selected document types, 'ALL' if everything
      * @param CDE_ID
      *            typed in keyword value for cde_id search, is empty if SearchIN is names and definition.
      * @param histID
      *            String Keyword for historical id search in.
      * @param permValue
      *            String Keyword for permissible value searchin.
      * @param sOrigin
      *            String keyword for origin search in.
      * @param sProtoId
      *            String typed ID of the protocol can be wild card included
      * @param crfName
      *            String keyword value of the crf name.
      * @param pvIDseq
      *            String idseq of the selected permissible value for get associated pvs.
      * @param vdIDseq
      *            String idseq of the selected value domain for get associated vds.
      * @param decIDseq
      *            String idseq of the selected data elmeent concept for get associated decs.
      * @param cdIDseq
      *            String idseq of the selected conceptual domain for get associated cds.
      * @param cscsiIDseq
      *            String idseq of the selected class scheme items for get associated csis.
      * @param conIDseq
      *            string concept idseq to filter the search
      * @param conName
      *            string concept name to filter
      * @param derType
      *            string der type to filter
      * @param vList
      *            returns Vector of DEbean.
      * 
      */
     public void doDESearch(String DE_IDSEQ, String InString, String ContID, String ContName, String ContUse,
                     String sVersion, double dVersion, String ASLName, String regStatus, String sCreatedFrom,
                     String sCreatedTo, String sModifiedFrom, String sModifiedTo, String sCreator, String sModifier,
                     String DocText, String docTypes, String CDE_ID, String histID, String permValue, String sOrigin,
                     String sProtoId, String crfName, String pvIDseq, String vdIDseq, String vmIDSeq, String decIDseq, String cdIDseq,
                     String cscsiIDseq, String conIDseq, String conName, String derType, Vector vList)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         Hashtable desTable = new Hashtable();
         HttpSession session = m_classReq.getSession();
         try
         {
             if ((CDE_ID != null) && !(CDE_ID.equals(""))){
                int id = Integer.parseInt(CDE_ID);	
             }
         	int g = 0;
             // check if search in is protocol/crf name
             boolean isProtoCRF = false;
             if (sProtoId != null && !sProtoId.equals(""))
                 isProtoCRF = true;
             if (isProtoCRF == false && crfName != null && !crfName.equals(""))
                 isProtoCRF = true;
             Vector derAllTypes = (Vector) session.getAttribute("allDerTypes");
             if (derAllTypes == null)
                 derAllTypes = new Vector();
             // get the existing desHash table if appendsearch
             String strAppend = (String) session.getAttribute("AppendAction");
             if (strAppend != null && strAppend.equals("Appended"))
                 desTable = (Hashtable) session.getAttribute("desHashTable");
             // Create a Callable Statement object.
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_DE"
                                 + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(7, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, DE_IDSEQ);
                 cstmt.setString(2, InString);
                 cstmt.setString(3, ContID);
                 cstmt.setString(4, ContName);
                 cstmt.setString(5, ASLName);
                 cstmt.setString(6, CDE_ID);
                 cstmt.setString(8, regStatus);
                 cstmt.setString(9, sOrigin);
                 cstmt.setString(10, sCreatedFrom);
                 cstmt.setString(11, sCreatedTo);
                 cstmt.setString(12, sModifiedFrom);
                 cstmt.setString(13, sModifiedTo);
                 cstmt.setString(14, sCreator);
                 cstmt.setString(15, sModifier);
                 cstmt.setString(16, permValue);
                 cstmt.setString(17, DocText);
                 cstmt.setString(18, histID);
                 cstmt.setString(19, sVersion);
                 cstmt.setString(20, docTypes);
                 cstmt.setString(21, ContUse);
                 cstmt.setString(22, sProtoId);
                 cstmt.setString(23, crfName);
                 cstmt.setDouble(24, dVersion);
                 cstmt.setString(25, pvIDseq);
                 cstmt.setString(26, decIDseq);
                 cstmt.setString(27, vdIDseq);
                 cstmt.setString(28, cscsiIDseq);
                 cstmt.setString(29, cdIDseq);
                 cstmt.setString(30, derType);
                 cstmt.setString(31, conName);
                 cstmt.setString(32, conIDseq);
                 cstmt.setString(33, vmIDSeq);
                 // capture the duration
                  logger.info(cstmt.toString());
                 // new java.util.Date()));
                 // Now we are ready to call the stored procedure
                 cstmt.execute();
                 // capture the duration
                 // logger.info(m_servlet.getLogMessage(m_classReq, "doDESearch", "end executing search", startDate, new
                 // java.util.Date()));
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(7);
                 // logger.info(m_servlet.getLogMessage(m_classReq, "doDESearch", "got resultset object", startDate, new
                 // java.util.Date()));
                 String s;
                 if (rs != null)
                 {
                     DE_Bean DEBean = new DE_Bean();
                     // capture the duration
                     // logger.info(m_servlet.getLogMessage(m_classReq, "doDESearch", "begin search resultset",
                     // startDate, new java.util.Date()));
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         boolean isFound = false;
                         Vector usedBy = new Vector();
                         // check if ID is found in the vector.
                         if (vList != null)
                         {
                             for (int i = 0; i < vList.size(); i++)
                             {
                                 DEBean = (DE_Bean) vList.elementAt(i);
                                 if (DEBean.getDE_DE_IDSEQ().equals(rs.getString("de_idseq")))
                                 {
                                     isFound = true;
                                     break;
                                 }
                             }
                         }
                         // add the primary record to the Vector
                         if (isFound == false)
                         {
                             g = g + 1;
                             DEBean = new DE_Bean();
                             DEBean.setDE_PREFERRED_NAME(rs.getString("preferred_name"));
                             DEBean.setDE_LONG_NAME(rs.getString("long_name"));
                             DEBean.setDE_PREFERRED_DEFINITION(rs.getString("preferred_definition"));
                             DEBean.setDE_ASL_NAME(rs.getString("asl_name"));
                             DEBean.setDE_CONTE_IDSEQ(rs.getString("conte_idseq"));
                             s = rs.getString("begin_date");
                             if (s != null)
                                 s = m_util.getCurationDate(s);
                             DEBean.setDE_BEGIN_DATE(s);
                             s = rs.getString("end_date");
                             if (s != null)
                                 s = m_util.getCurationDate(s);
                             DEBean.setDE_END_DATE(s);
                             DEBean.setDE_CONTEXT_NAME(rs.getString("name"));
                             DEBean.setDE_DEC_IDSEQ(rs.getString("dec_idseq"));
                             DEBean.setDE_DEC_NAME(rs.getString("dec_name"));
                             DEBean.setDE_DEC_Definition(rs.getString("dec_preferred_definition"));
                             DEBean.setDE_VD_IDSEQ(rs.getString("vd_idseq"));
                             DEBean.setDE_VD_NAME(rs.getString("vd_name"));
                             DEBean.setDE_VD_Definition(rs.getString("vd_preferred_definition"));
                             // add the decimal number
                             if (rs.getString("version").indexOf('.') >= 0)
                                 DEBean.setDE_VERSION(rs.getString("version"));
                             else
                                 DEBean.setDE_VERSION(rs.getString("version") + ".0");
                             DEBean.setDE_MIN_CDE_ID(rs.getString("cde_id"));
                             DEBean.setDE_CHANGE_NOTE(rs.getString("change_note"));
                             // DEBean.setDE_LANGUAGE(rs.getString("language"));
                             DEBean.setDE_DE_IDSEQ(rs.getString("de_idseq"));
                             // DEBean.setDE_LANGUAGE_IDSEQ(rs.getString("desig_idseq"));
                             DEBean.setDE_DEC_PREFERRED_NAME(rs.getString("dec_pref_name"));
                             DEBean.setDE_VD_PREFERRED_NAME(rs.getString("vd_pref_name"));
                             DEBean.setDE_ALIAS_NAME(rs.getString("usedby_name"));
                             DEBean.setDE_USEDBY_CONTEXT(rs.getString("used_by_context"));
                             usedBy = new Vector();
                             if (rs.getString("used_by_conte_idseq") != null)
                                 usedBy.addElement(rs.getString("used_by_conte_idseq"));
                             DEBean.setDE_USEDBY_CONTEXT_ID(usedBy);
                             DEBean.setDE_SOURCE(rs.getString("origin"));
                             s = rs.getString("date_created");
                             if (s != null)
                                 s = m_util.getCurationDate(s);
                             DEBean.setDE_DATE_CREATED(s);
                             s = rs.getString("date_modified");
                             if (s != null)
                                 s = m_util.getCurationDate(s);
                             DEBean.setDE_DATE_MODIFIED(s);
                             DEBean.setDE_REG_STATUS(rs.getString("registration_status"));
                             DEBean.setDE_REG_STATUS_IDSEQ(rs.getString("ar_idseq"));
                             DEBean.setDE_CREATED_BY(rs.getString("created_by"));
                             DEBean.setDE_MODIFIED_BY(rs.getString("modified_by"));
                             // get pv and historic cde id with counts
                             DEBean = getMultiRecordsResultOther(rs, DEBean, isProtoCRF);
                             // doc text attributes
                             DEBean.setDOC_TEXT_PREFERRED_QUESTION(rs.getString("long_name_doc_text"));
                             // DEBean.setDOC_TEXT_HISTORIC_NAME(rs.getString("hist_cde_doc_text"));
                             // DEBean = getMultiRecordsResultDocText(rs, DEBean);
                             // check if it is component
                             String derRel = rs.getString("derivation_type");
                             if (derRel == null)
                                 derRel = "";
                             String acID = DEBean.getDE_DE_IDSEQ();
                             if (!derRel.equals("") && !derAllTypes.contains(derRel))
                             {
                                 acID = derRel;
                                 derRel = "Component";
                             }
                             DEBean.setDE_DER_RELATION(derRel);
                             DEBean.setDE_DER_REL_IDSEQ(acID);
                             DEBean.setAC_CONCEPT_NAME(rs.getString("con_name"));
                             DEBean.setALTERNATE_NAME(rs.getString("alt_name"));
                             DEBean.setREFERENCE_DOCUMENT(rs.getString("rd_name"));
                             DEBean.setDE_TYPE_NAME("PRIMARY");
                             // add the combination usedbycontext+acID and desIdseq in the hash table
                             if ((rs.getString("used_by_context") != null)
                                             && !rs.getString("used_by_context").equals(""))
                                 desTable.put(rs.getString("used_by_context") + "," + rs.getString("de_idseq"), rs
                                                 .getString("u_desig_idseq"));
                             vList.addElement(DEBean);
                         }
                         else
                         {
                             // add the used by if not null with the comma.
                             if ((rs.getString("used_by_context") != null)
                                             && (!rs.getString("used_by_context").equals("")))
                             {
                                 usedBy = DEBean.getDE_USEDBY_CONTEXT_ID();
                                 // store it only if does not exist already
                                 if (rs.getString("used_by_conte_idseq") != null
                                                 && !usedBy.contains(rs.getString("used_by_conte_idseq")))
                                 {
                                     usedBy.addElement(rs.getString("used_by_conte_idseq"));
                                     DEBean.setDE_USEDBY_CONTEXT_ID(usedBy);
                                     DEBean.setDE_USEDBY_CONTEXT(DEBean.getDE_USEDBY_CONTEXT() + ", "
                                                     + rs.getString("used_by_context"));
                                     desTable.put(rs.getString("used_by_context") + "," + rs.getString("de_idseq"), rs
                                                     .getString("u_desig_idseq"));
                                 }
                             }
                         }
                     }
                 }
             }
         }catch (NumberFormatException e){}
         catch (Exception e)
         {
           logger.error("ERROR - GetACSearch-DESearch for other : " + e.toString(), e);
         }
         try
         {
            DataManager.setAttribute(session, "desHashTable", desTable);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         
     }
 
     /**
      * to get the records perm value, histID with the counts from the RS into the Bean
      * 
      * @param rst
      *            ResultSet
      * @param deBean
      *            DE_Bean
      * @param isProtoCRF
      *            boolean to show crf and protocol
      * 
      * @return DE_Bean
      * 
      * @throws Exception
      */
     private DE_Bean getMultiRecordsResultOther(ResultSet rst, DE_Bean deBean, boolean isProtoCRF) throws Exception
     {
         String sText = "";
         Integer iCount;
         // doc type min_hist_cde_id
         sText = rst.getString("min_value");
         if (sText != null && !sText.equals(""))
         {
             deBean.setDE_Permissible_Value(sText);
             sText = rst.getString("value_count");
             iCount = new Integer(sText);
             deBean.setDE_Permissible_Value_Count(iCount);
         }
         // protocol id
         if (isProtoCRF)
         {
             sText = rst.getString("protocol_id");
             if (sText != null && !sText.equals(""))
             {
                 deBean.setDE_PROTOCOL_ID(sText);
                 sText = rst.getString("protocol_crf_count");
                 iCount = new Integer(sText);
                 deBean.setDE_PROTO_CRF_Count(iCount);
             }
             // crf name
             sText = rst.getString("crf_name");
             if (sText != null && !sText.equals(""))
             {
                 deBean.setDE_CRF_NAME(sText);
                 sText = rst.getString("protocol_crf_count");
                 iCount = new Integer(sText);
                 deBean.setDE_PROTO_CRF_Count(iCount);
             }
         }
         return deBean;
     }
 
     /**
      * To get resultSet from database for DataElementConcept Component called from getACKeywordResult method.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_DEC(ContID, InString, ContName, ASLName,
      * DEC_IDSEQ, DEC_ID, OracleTypes.CURSOR, origin, , sObject, sProperty, createdFrom, Createdto, ModifiedFrom,
      * modifiedTo, creator, modifier, sVersion)}"
      * 
      * loop through the ResultSet and add them to bean which is added to the vector to return if the record is added to
      * the vector already, updates only the used by context to display all used by contexts in a row.
      * 
      * @param DEC_IDSEQ
      *            Data Element Concept's idseq.
      * @param InString
      *            Keyword value.
      * @param ContID
      *            Selected Context IDseq.
      * @param ContName
      *            selected context name.
      * @param sVersion
      *            String yes for latest version filter or null for all version filter.
      * @param ASLName
      *            selected workflow status name.
      * @param sCreatedFrom
      *            String created from date filter.
      * @param sCreatedTo
      *            String created to date filter.
      * @param sModifiedFrom
      *            String modified from date filter.
      * @param sModifiedTo
      *            String modified to date filter.
      * @param sCreator
      *            String selected creator.
      * @param sModifier
      *            String selected modifier.
      * @param DEC_ID
      *            typed in keyword value for public id search, is empty if SearchIN is names and definition.
      * @param sObject
      *            String Keyword for Object Class search in.
      * @param sProperty
      *            String Keyword for Property searchin.
      * @param sOrigin
      *            String keyword for origin search in.
      * @param dVersion
      *            double typed version number.
      * @param sCDid
      *            String idseq of the selected conceptual domain for get associated cds.
      * @param deIDseq
      *            String idseq of the selected data elmeent for get associated des.
      * @param cscsiIDseq
      *            string cscsi idseq
      * @param conIDseq
      *            string concept idseq
      * @param conName
      *            string concept name
      * @param vList
      *            returns Vector of DEbean.
      */
     public void doDECSearch(String DEC_IDSEQ, String InString, String ContID, String ContName, String sVersion,
                     String ASLName, String sCreatedFrom, String sCreatedTo, String sModifiedFrom, String sModifiedTo,
                     String sCreator, String sModifier, String DEC_ID, String sObject, String sProperty, String sOrigin,
                     double dVersion, String sCDid, String deIDseq, String cscsiIDseq, String conIDseq, String conName,
                     Vector vList)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
         	if ((DEC_ID != null) && !(DEC_ID.equals(""))){
                 int id = Integer.parseInt(DEC_ID);	
              }
         	if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn()
                                 .prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_DEC(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
                 // Now tie the placeholders with actual parameters.
                 cstmt.registerOutParameter(7, OracleTypes.CURSOR);
                 cstmt.setString(1, ContID);
                 cstmt.setString(2, InString);
                 cstmt.setString(3, ContName);
                 cstmt.setString(4, ASLName);
                 cstmt.setString(5, DEC_IDSEQ);
                 cstmt.setString(6, DEC_ID);
                 cstmt.setString(8, sOrigin);
                 cstmt.setString(9, sObject);
                 cstmt.setString(10, sProperty);
                 cstmt.setString(11, sCreatedFrom);
                 cstmt.setString(12, sCreatedTo);
                 cstmt.setString(13, sModifiedFrom);
                 cstmt.setString(14, sModifiedTo);
                 cstmt.setString(15, sCreator);
                 cstmt.setString(16, sModifier);
                 cstmt.setString(17, sVersion);
                 cstmt.setDouble(18, dVersion);
                 cstmt.setString(19, sCDid);
                 cstmt.setString(20, deIDseq);
                 cstmt.setString(21, cscsiIDseq);
                 cstmt.setString(22, conName);
                 cstmt.setString(23, conIDseq);
                 // Now we are ready to call the stored procedure
                 cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(7);
                 String s;
                 String oc_condr_idseq = "";
                 String prop_condr_idseq = "";
                 if (rs != null)
                 {
                     // loop through to printout the outstrings
                     DEC_Bean DECBean = new DEC_Bean();
                     while (rs.next())
                     {
                         DECBean = new DEC_Bean();
                         DECBean.setDEC_PREFERRED_NAME(rs.getString("preferred_name"));
                         DECBean.setDEC_LONG_NAME(rs.getString("long_name"));
                         DECBean.setDEC_PREFERRED_DEFINITION(rs.getString("preferred_definition"));
                         DECBean.setDEC_ASL_NAME(rs.getString("asl_name"));
                         DECBean.setDEC_CONTE_IDSEQ(rs.getString("conte_idseq"));
                         s = rs.getString("begin_date");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         DECBean.setDEC_BEGIN_DATE(s);
                         s = rs.getString("end_date");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         DECBean.setDEC_END_DATE(s);
                         // add the decimal number
                         if (rs.getString("version").indexOf('.') >= 0)
                             DECBean.setDEC_VERSION(rs.getString("version"));
                         else
                             DECBean.setDEC_VERSION(rs.getString("version") + ".0");
                         DECBean.setDEC_DEC_IDSEQ(rs.getString("dec_idseq"));
                         DECBean.setDEC_CHANGE_NOTE(rs.getString("change_note"));
                         DECBean.setDEC_CONTEXT_NAME(rs.getString("context"));
                         DECBean.setDEC_CD_NAME(rs.getString("cd_name"));
                         DECBean.setDEC_OCL_NAME(rs.getString("ocl_name"));
                         DECBean.setDEC_PROPL_NAME(rs.getString("propl_name"));
                         // DECBean.setDEC_OBJ_CLASS_QUALIFIER("");
                         // DECBean.setDEC_PROPERTY_QUALIFIER("");
                         DECBean.setDEC_CD_IDSEQ(rs.getString("CD_IDSEQ"));
                         DECBean.setDEC_OCL_IDSEQ(rs.getString("OC_IDSEQ"));
                         DECBean.setDEC_PROPL_IDSEQ(rs.getString("PROP_IDSEQ"));
                         oc_condr_idseq = rs.getString("oc_condr_idseq");
                         DECBean.setDEC_OC_CONDR_IDSEQ(oc_condr_idseq);
                         prop_condr_idseq = rs.getString("prop_condr_idseq");
                         DECBean.setDEC_PROP_CONDR_IDSEQ(prop_condr_idseq);
                         DECBean.setDEC_DEC_ID(rs.getString("dec_id"));
                         DECBean.setDEC_SOURCE(rs.getString("ORIGIN"));
                         s = rs.getString("date_created");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         DECBean.setDEC_DATE_CREATED(s);
                         s = rs.getString("date_modified");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         DECBean.setDEC_DATE_MODIFIED(s);
                         DECBean.setDEC_CREATED_BY(rs.getString("created_by"));
                         DECBean.setDEC_MODIFIED_BY(rs.getString("modified_by"));
                         DECBean.setDEC_OBJ_ASL_NAME(rs.getString("oc_asl_name"));
                         DECBean.setDEC_PROP_ASL_NAME(rs.getString("prop_asl_name"));
                         DECBean.setAC_CONCEPT_NAME(rs.getString("con_name"));
                         DECBean.setALTERNATE_NAME(rs.getString("alt_name"));
                         DECBean.setREFERENCE_DOCUMENT(rs.getString("rd_name"));
                         vList.addElement(DECBean);
                         // store the ocl name or propl name in the request
                         String reslabel = "Search Results for Data Element Concept associated with - ";
                         if (sObject != null && !sObject.equals(""))
                             m_classReq.setAttribute("resultLabel", reslabel + DECBean.getDEC_OCL_NAME());
                         else if (sProperty != null && !sProperty.equals(""))
                             m_classReq.setAttribute("resultLabel", reslabel + DECBean.getDEC_PROPL_NAME());
                     }
                 }
             }
         }catch (NumberFormatException e){}
         catch (Exception e)
         {
           logger.error("ERROR - GetACSearch-DECSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
     }
 
     /**
      * To get resultSet from database for Value Domian Component called from getACKeywordResult method.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_VD(ContID, InString, ContName, ASLName,
      * VD_IDSEQ, VD_ID, OracleTypes.CURSOR, sOrigin, sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator,
      * sModifier, sPermValue, sVersion, VDType)}"
      * 
      * loop through the ResultSet and add them to bean which is added to the vector to return if the record is added to
      * the vector already, updates only the used by context to display all used by contexts in a row.
      * 
      * @param VD_IDSEQ
      *            Value Domain's idseq.
      * @param InString
      *            Keyword value.
      * @param ContID
      *            Selected Context IDseq.
      * @param ContName
      *            selected context name.
      * @param sVersion
      *            String yes for latest version filter or null for all version filter.
      * @param VDType
      *            String comma delimited 'E,N,R' for each type selected.
      * @param ASLName
      *            selected workflow status name.
      * @param sCreatedFrom
      *            String created from date filter.
      * @param sCreatedTo
      *            String created to date filter.
      * @param sModifiedFrom
      *            String modified from date filter.
      * @param sModifiedTo
      *            String modified to date filter.
      * @param sCreator
      *            String selected creator.
      * @param sModifier
      *            String selected modifier.
      * @param VD_ID
      *            typed in keyword value for public id search, is empty if SearchIN is names and definition.
      * @param sPermValue
      *            String Keyword for Permissible Value search in.
      * @param sOrigin
      *            String keyword for origin search in.
      * @param dVersion
      *            double typed version number.
      * @param sCDid
      *            String idseq of the selected conceptual domain for get associated cds.
      * @param pvIDseq
      *            String idseq of the selected permissible value for get associated pvs.
      * @param deIDseq
      *            String idseq of the selected data elmeent for get associated des.
      * @param cscsiIDseq
      *            string cscsi idseq to filer
      * @param conIDseq
      *            string concept idseq to filter
      * @param conName
      *            string concept name to filter
      * @param sData
      *            string datatype filter value
      * @param vList
      *            returns Vector of DEbean.
      */
     public void doVDSearch(String VD_IDSEQ, String InString, String ContID, String ContName, String sVersion,
                     String VDType, String ASLName, String sCreatedFrom, String sCreatedTo, String sModifiedFrom,
                     String sModifiedTo, String sCreator, String sModifier, String VD_ID, String sPermValue,
                     String sOrigin, double dVersion, String sCDid, String pvIDseq, String deIDseq, String cscsiIDseq,
                     String conIDseq, String vmIDseq, String conName, String sData, Vector vList)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
         	if ((VD_ID != null) && !VD_ID.equals("")){
             	int id = Integer.parseInt(VD_ID);
              }
 
         	if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn()
                                 .prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_VD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
                 // Now tie the placeholders with actual parameters.
                 cstmt.registerOutParameter(7, OracleTypes.CURSOR);
                 cstmt.setString(1, ContID);
                 cstmt.setString(2, InString);
                 cstmt.setString(3, ContName);
                 cstmt.setString(4, ASLName);
                 cstmt.setString(5, VD_IDSEQ);
                 cstmt.setString(6, VD_ID);
                 cstmt.setString(8, sOrigin);
                 cstmt.setString(9, sCreatedFrom);
                 cstmt.setString(10, sCreatedTo);
                 cstmt.setString(11, sModifiedFrom);
                 cstmt.setString(12, sModifiedTo);
                 cstmt.setString(13, sCreator);
                 cstmt.setString(14, sModifier);
                 cstmt.setString(15, sPermValue);
                 cstmt.setString(16, sVersion);
                 cstmt.setString(17, VDType);
                 cstmt.setDouble(18, dVersion);
                 cstmt.setString(19, sCDid);
                 cstmt.setString(20, pvIDseq);
                 cstmt.setString(21, deIDseq);
                 cstmt.setString(22, sData);
                 cstmt.setString(23, cscsiIDseq);
                 cstmt.setString(24, conName);
                 cstmt.setString(25, conIDseq);
                 cstmt.setString(26, vmIDseq);
                 // Now we are ready to call the stored procedure
                 cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(7);
                 String s;
                 if (rs != null)
                 {
                     // loop through to printout the outstrings
                     VD_Bean VDBean = new VD_Bean();
                     while (rs.next())
                     {
                         VDBean = new VD_Bean();
                         VDBean.setVD_PREFERRED_NAME(rs.getString("preferred_name"));
                         VDBean.setVD_LONG_NAME(rs.getString("long_name"));
                         VDBean.setVD_PREFERRED_DEFINITION(rs.getString("preferred_definition"));
                         VDBean.setVD_CONTE_IDSEQ(rs.getString("conte_idseq"));
                         VDBean.setVD_ASL_NAME(rs.getString("asl_name"));
                         VDBean.setVD_VD_IDSEQ(rs.getString("vd_idseq"));
                         // add the decimal number
                         if (rs.getString("version").indexOf('.') >= 0)
                             VDBean.setVD_VERSION(rs.getString("version"));
                         else
                             VDBean.setVD_VERSION(rs.getString("version") + ".0");
                         VDBean.setVD_DATA_TYPE(rs.getString("dtl_name"));
                         s = rs.getString("begin_date");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         VDBean.setVD_BEGIN_DATE(s);
                         s = rs.getString("end_date");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         VDBean.setVD_END_DATE(s);
                         VDBean.setVD_TYPE_FLAG(rs.getString("vd_type_flag"));
                         VDBean.setVD_CHANGE_NOTE(rs.getString("change_note"));
                         VDBean.setVD_UOML_NAME(rs.getString("uoml_name"));
                         VDBean.setVD_FORML_NAME(rs.getString("forml_name"));
                         VDBean.setVD_MAX_LENGTH_NUM(rs.getString("max_length_num"));
                         VDBean.setVD_MIN_LENGTH_NUM(rs.getString("min_length_num"));
                         VDBean.setVD_DECIMAL_PLACE(rs.getString("decimal_place"));
                         VDBean.setVD_CHAR_SET_NAME(rs.getString("char_set_name"));
                         VDBean.setVD_HIGH_VALUE_NUM(rs.getString("high_value_num"));
                         VDBean.setVD_LOW_VALUE_NUM(rs.getString("low_value_num"));
                         VDBean.setVD_REP_TERM(rs.getString("rep_term"));
                         VDBean.setVD_REP_IDSEQ(rs.getString("rep_idseq"));
                         // VDBean.setVD_REP_QUAL(rs.getString("qualifier_name"));
                         VDBean.setVD_REP_CONDR_IDSEQ(rs.getString("rep_condr_idseq"));
                         VDBean.setVD_PAR_CONDR_IDSEQ(rs.getString("vd_condr_idseq"));
                         String rep_condr_idseq = rs.getString("rep_condr_idseq");
                         VDBean.setVD_REP_CONDR_IDSEQ(rep_condr_idseq);
                         // fillRepVectors(rep_condr_idseq, VDBean);
                         VDBean.setVD_CONTEXT_NAME(rs.getString("context"));
                         VDBean.setVD_CD_IDSEQ(rs.getString("cd_idseq"));
                         VDBean.setVD_CD_NAME(rs.getString("cd_name"));
                         // VDBean.setVD_LANGUAGE(rs.getString("language"));
                         // VDBean.setVD_LANGUAGE_IDSEQ(rs.getString("lae_des_idseq"));
                         VDBean.setVD_VD_ID(rs.getString("vd_id"));
                         VDBean.setVD_SOURCE(rs.getString("origin"));
                         s = rs.getString("date_created");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         VDBean.setVD_DATE_CREATED(s);
                         s = rs.getString("date_modified");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         VDBean.setVD_DATE_MODIFIED(s);
                         VDBean.setVD_CREATED_BY(rs.getString("created_by"));
                         VDBean.setVD_MODIFIED_BY(rs.getString("modified_by"));
                         VDBean.setVD_REP_ASL_NAME(rs.getString("rep_asl_name"));
                         VDBean.setAC_CONCEPT_NAME(rs.getString("con_name"));
                         VDBean.setALTERNATE_NAME(rs.getString("alt_name"));
                         VDBean.setREFERENCE_DOCUMENT(rs.getString("rd_name"));
                         // get permissible value
                         s = rs.getString("min_value");
                         if (s != null && !s.equals(""))
                         {
                             VDBean.setVD_Permissible_Value(s);
                             s = rs.getString("value_count");
                             Integer iCount = new Integer(s);
                             VDBean.setVD_Permissible_Value_Count(iCount);
                         }
                         VDBean.setVD_DES_ALIAS_ID("");
                         VDBean.setVD_OBJ_CLASS("");
                         VDBean.setVD_OBJ_QUAL("");
                         VDBean.setVD_PROP_CLASS("");
                         VDBean.setVD_PROP_QUAL("");
                         VDBean.setVD_PROTOCOL_ID("");
                         VDBean.setVD_CRF_NAME("");
                         vList.addElement(VDBean);
                     }
                 }
             }
         }
         catch (NumberFormatException e){}
         catch (Exception e)
         {
           logger.error("ERROR - GetACSearch-doVDSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
     }
 
     /**
      * To get Search results for Conceptual Domain from database called from getACKeywordResult.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_CD(CD_IDSEQ, InString, ContID, ContName,
      * ASLName, CD_ID, OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to bean which is added to the vector to return
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
      * 
      */
     public Vector doCDSearch(String CD_IDSEQ, String InString, String ContID, String ContName, String sVersion,
                     String ASLName, String sCreatedFrom, String sCreatedTo, String sModifiedFrom, String sModifiedTo,
                     String sCreator, String sModifier, String CD_ID, String sOrigin, double dVersion, String conName,
                     String conID, String sVM, Vector vList)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
             if ((CD_ID != null) && !CD_ID.equals("")){
             	int id = Integer.parseInt(CD_ID);
              }
         	if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn()
                                 .prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_CD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
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
                 cstmt.setString(19, m_util.parsedStringSingleQuoteOracle(sVM));
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
                             s = m_util.getCurationDate(s);
                         CDBean.setCD_BEGIN_DATE(s);
                         s = rs.getString("end_date");
                         if (s != null)
                             s = m_util.getCurationDate(s);
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
                             s = m_util.getCurationDate(s);
                         CDBean.setCD_DATE_CREATED(s);
                         s = rs.getString("date_modified");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         CDBean.setCD_DATE_MODIFIED(s);
                         CDBean.setCD_CREATED_BY(rs.getString("created_by"));
                         CDBean.setCD_MODIFIED_BY(rs.getString("modified_by"));
                         vList.addElement(CDBean);
                     }
                 }
             }
         }catch (NumberFormatException e){}
         catch (Exception e)
         {
            logger.error("ERROR - GetACSearch-CDSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return vList;
     }
 
     /**
      * To get Search results for Class Scheme Items from database called from getACKeywordResult.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_CSI(CSI_IDSEQ, InString, ContID, ContName,
      * ASLName, CSI_ID, OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to bean which is added to the vector to return
      * 
      * @param InString
      * 
      * @param ContName
      *            selected context name.
      * @param CSName
      *            string cs name to filter
      * @param vList
      *            returns Vector of DEbean.
      * 
      */
     private void doCSISearch(String InString, String ContName, String CSName, Vector vList)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT_CDE_CURATOR_PKG.SEARCH_CSI(?,?,?,?)}");
                 cstmt.registerOutParameter(4, OracleTypes.CURSOR);
                 cstmt.setString(1, InString);
                 cstmt.setString(2, CSName);
                 cstmt.setString(3, ContName);
                 // Now we are ready to call the stored procedure
                 cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(4);
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         CSI_Bean CSIBean = new CSI_Bean();
                         CSIBean.setCSI_CS_IDSEQ(rs.getString(1));
                         CSIBean.setCSI_CS_NAME(rs.getString(2));
                         CSIBean.setCSI_CS_LONG_NAME(rs.getString(3));
                         CSIBean.setCSI_CS_PUBLICID(rs.getString(5));
                         CSIBean.setCSI_CS_VERSION(rs.getString(4));
                         CSIBean.setCSI_CONTEXT_NAME(rs.getString(6));
                         //CSIBean.setCSI_CSI_IDSEQ(rs.getString(7));
                         CSIBean.setCSI_NAME(rs.getString(8));
                         CSIBean.setCSI_CSITL_NAME(rs.getString(9));
                         CSIBean.setCSI_DEFINITION(rs.getString(10));
                         CSIBean.setCSI_CSCSI_IDSEQ(rs.getString(11));
                        
                         vList.addElement(CSIBean);
                     }
                 }
             }
         }
         catch (Exception e)
         {
            logger.error("ERROR - GetACSearch-CSISearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
     }
 
     /**
      * To get the list of attributes selected to display, called from getACKeywordResult and getACShowResult methods.
      * selected attribute values from the multi select list is stored in session vector 'selectedAttr'. "All Attribute"
      * select will add all the fields of the selected component to the vector
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param sSearchAC
      *            selected Administered component.
      * @param sMenuAct
      *            String menuaction
      * 
      */
     private void setAttributeValues(HttpServletRequest req, HttpServletResponse res, String sSearchAC, String sMenuAct)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vAttrList = new Vector();
             String sAttrList[] = req.getParameterValues("listAttrFilter");
             boolean bAllAttr = false;
             // loop through the string array to extract values.
             if (sAttrList != null)
             {
                 for (int i = 0; i < sAttrList.length; i++)
                 {
                     if (sAttrList[i] != null)
                     {
                         String sAttr = new String(sAttrList[i]);
                         vAttrList.addElement(sAttr);
                         if (sAttr.equals("All Attributes"))
                         {
                             bAllAttr = true;
                             break;
                         }
                     }
                 }
             }
             // add all fields to the vector if all attributes selected.
             if (bAllAttr == true)
             {
                 // get all the attributes from the session to select
                 vAttrList = new Vector();
                 if (sMenuAct.equals("searchForCreate"))
                     vAttrList = (Vector) session.getAttribute("creAttributeList");
                 else
                     vAttrList = (Vector) session.getAttribute("serAttributeList");
             }
             // remove all attributes from the selected list if any
             if (vAttrList.contains("All Attributes"))
                 vAttrList.removeElement("All Attributes");
             // store the vector in session
             if (sMenuAct.equals("searchForCreate"))
             {
                 DataManager.setAttribute(session, "creSelectedAttr", vAttrList);
             }
             else
                 DataManager.setAttribute(session, "selectedAttr", vAttrList);
             
             }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-setAttribute: " + e);
             logger.error("ERROR in GetACSearch-setAttribute : " + e.toString(), e);
         }
     }
 
     /**
      * To get the list of attributes selected to display, called from getACKeywordResult and getACShowResult methods.
      * selected attribute values from the multi select list is stored in session vector 'selectedAttr'. "All Attribute"
      * select will add all the fields of the selected component to the vector
      * 
      * @param sSearchAC
      *            selected Administered component.
      * @param sAction
      *            string menu action
      * @param sAttr
      *            string selected display attribute
      * @return string selected display attributes
      * 
      */
     public String getMultiReqValues(String sSearchAC, String sAction, String sAttr)
     {
         String sValues = "";
         if (sSearchAC == null)
             sSearchAC = "";
         try
         {
             HttpSession session = m_classReq.getSession();
             Vector vSelectList = new Vector();
             Vector vStatusDE = (Vector) session.getAttribute("vStatusDE");
             Vector vStatusDEC = (Vector) session.getAttribute("vStatusDEC");
             Vector vStatusVD = (Vector) session.getAttribute("vStatusVD");
             Vector vStatusCD = (Vector) session.getAttribute("vStatusCD");
             String sSelectList[] = null;
             if (sAttr.equals("WFStatus"))
                 sSelectList = m_classReq.getParameterValues("listStatusFilter");
             else
                 sSelectList = m_classReq.getParameterValues("listMultiContextFilter");
             boolean bAllValues = false;
             if (sSelectList == null)
                 bAllValues = true;
             if (sSelectList != null)
             {
                 // loop through the string array to extract values.
                 for (int i = 0; i < sSelectList.length; i++)
                 {
                     if (sSelectList[i] != null)
                     {
                         // make one string value to submit
                         if (i == 0)
                             sValues = sSelectList[i];
                         else
                             sValues = sValues + "," + sSelectList[i];
                         // set the value and break if all statuses or contexts selected
                         if (sValues.equals("AllContext") || sValues.equals("AllStatus"))
                         {
                             bAllValues = true;
                             break;
                         }
                         // store it in vector to refresh list on the page
                         vSelectList.addElement(sSelectList[i]);
                     }
                 }
             }
             if (bAllValues == true)
             {
                 vSelectList = new Vector();
                 sValues = "";
                 if (sSearchAC.equals("Questions") && sAttr.equals("WFStatus"))
                 {
                     sValues = "DRAFT NEW";
                     vSelectList.addElement("DRAFT NEW");
                 }
             }
             // store it in the session to refresh the list on the page
             if (sAttr.equals("WFStatus"))
             {
                 if (sAction.equals("searchForCreate"))
                 {
                     DataManager.setAttribute(session, "creStatus", vSelectList);
                     m_classReq.setAttribute("creStatusBlocks", vSelectList);
                 }
                 else
                     DataManager.setAttribute(session, "serStatus", vSelectList);
             }
             else
             {
                 if (sAction.equals("searchForCreate"))
                     DataManager.setAttribute(session, "creMultiContext", vSelectList);
                 else
                     DataManager.setAttribute(session, "serMultiContext", vSelectList);
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getMultiReqValues: " + e);
             logger.error("ERROR in GetACSearch-getMultiReqValues : " + e.toString(), e);
         }
         return sValues;
     }
 
     /**
      * To get the list of attributes selected to display, called from getACKeywordResult and getACShowResult methods.
      * selected attribute values from the multi select list is stored in session vector 'selectedAttr'. "All Attribute"
      * select will add all the fields of the selected component to the vector
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param sSearchAC
      *            selected Administered component.
      * @param sAction
      *            String search action
      * @param isBlockSearch
      *            boolean to evs search
      * @return String comma delimited statuses
      * 
      */
     public String getStatusValues(HttpServletRequest req, HttpServletResponse res, String sSearchAC, String sAction,
                     boolean isBlockSearch)
     {
         String sStatus = "";
         if (sSearchAC == null)
             sSearchAC = "";
         try
         {
             HttpSession session = req.getSession();
             Vector vStatusList = new Vector();
             Vector vStatusDE = (Vector) session.getAttribute("vStatusDE");
             Vector vStatusDEC = (Vector) session.getAttribute("vStatusDEC");
             Vector vStatusVD = (Vector) session.getAttribute("vStatusVD");
             Vector vStatusVM = (Vector) session.getAttribute("vStatusVM");
             Vector vStatusCD = (Vector) session.getAttribute("vStatusCD");
             String sStatusList[] = req.getParameterValues("listStatusFilter");
             boolean bAllStatus = false;
             if (sStatusList == null)
                 bAllStatus = true;
             if (sStatusList != null)
             {
                 // loop through the string array to extract values.
                 for (int i = 0; i < sStatusList.length; i++)
                 {
                     if (sStatusList[i] != null)
                     {
                         // make one string value to submit
                         if (i == 0)
                             sStatus = sStatusList[i];
                         else
                             sStatus = sStatus + "," + sStatusList[i];
                         // set the value and break if all statuses checked
                         if (sStatus.equals("AllStatus"))
                         {
                             bAllStatus = true;
                             break;
                         }
                         // store it in vector to refresh list on the page
                         vStatusList.addElement(sStatusList[i]);
                     }
                 }
             }
             if (bAllStatus == true)
             {
                 vStatusList = new Vector();
                 sStatus = "";
                 if (sSearchAC.equals("Questions"))
                 {
                     sStatus = "DRAFT NEW";
                     vStatusList.addElement("DRAFT NEW");
                 }
                 // select version and workflow status to display if not already
                 else if (isBlockSearch)
                     selVerWFStatBlock(sAction);
             }
             // store it in the session to refresh the list on the page
             if (sAction.equals("searchForCreate"))
             {
                 DataManager.setAttribute(session, "creStatus", vStatusList);
                 req.setAttribute("creStatusBlocks", vStatusList);
             }
             else
                 DataManager.setAttribute(session, "serStatus", vStatusList);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getStatusValues: " + e);
             logger.error("ERROR in GetACSearch-setStatusValues : " + e.toString(), e);
         }
         return sStatus;
     }
 
     private void selVerWFStatBlock(String sAction)
     {
         try
         {
             HttpSession session = m_classReq.getSession();
             Vector vSel = new Vector();
             Vector vDisp = new Vector();
             if (sAction.equals("searchForCreate"))
             {
                 vSel = (Vector) session.getAttribute("creSelectedAttr");
                 vDisp = (Vector) session.getAttribute("creAttributeList");
             }
             else
             {
                 vSel = (Vector) session.getAttribute("selectedAttr");
                 vDisp = (Vector) session.getAttribute("serAttributeList");
             }
             // first get the index of version and add it to vsel
             int iInd = -1;
             if (vDisp.contains("Version"))
                 iInd = vDisp.indexOf("Version");
            if (!vSel.contains("Version") && iInd > 0 && iInd<=vSel.size())
                 vSel.insertElementAt("Version", iInd);
             // get index of wf status and add it to vsel
             iInd = -1;
             if (vDisp.contains("Workflow Status"))
                 iInd = vDisp.indexOf("Workflow Status");
            if (!vSel.contains("Workflow Status") && iInd > 0 && iInd<=vSel.size())
                 vSel.insertElementAt("Workflow Status", iInd);
             // store it back in teh session
             if (sAction.equals("searchForCreate"))
                 DataManager.setAttribute(session, "creSelectedAttr", vSel);
             else
                 DataManager.setAttribute(session, "selectedAttr", vSel);
         }
         catch (Exception e)
         {
             logger.error("Error - selVerWFStatBlock " + e.toString(), e);
         }
     }
 
     /**
      * To get the list of doc types selected, called from getACKeywordResult and getACShowResult methods. selected
      * attribute values from the multi select list is stored in session vector 'selectedAttr'. "All Attribute" select
      * will add all the fields of the selected component to the vector
      * 
      * @param sDocTypeList
      *            String[] array of strings.
      * @param sAction
      *            String for search action .
      * 
      * @return String of selectd doc types.
      * @throws Exception
      */
     private String getDocTypeValues(String[] sDocTypeList, String sAction) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         String sDocType = "";
         Vector vDocTypeList = new Vector();
         boolean bAllDocType = false;
         if (sDocTypeList != null)
         {
             // loop through the string array to extract values.
             for (int i = 0; i < sDocTypeList.length; i++)
             {
                 if (sDocTypeList[i] != null)
                 {
                     // make one string value to submit
                     if (i == 0)
                         sDocType = sDocTypeList[i];
                     else
                         sDocType = sDocType + "," + sDocTypeList[i];
                     // set the value and break if all DocTypees checked
                     if (sDocType.equals("AllDocType"))
                     {
                         bAllDocType = true;
                         break;
                     }
                     // store it in vector to refresh list on the page
                     vDocTypeList.addElement(sDocTypeList[i]);
                 }
             }
         }
         // get all types into a vector vector if all Types
         if (bAllDocType == true)
         {
             sDocType = "ALL";
             vDocTypeList = (Vector) session.getAttribute("vRDocType");
         }
         // store them in the session according to the search action
         Vector vSelectedAttr = new Vector();
         if (sAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "creDocTyes", vDocTypeList);
             // add these to default display attributes
             vSelectedAttr = (Vector) session.getAttribute("creSelectedAttr");
         }
         else
         {
             DataManager.setAttribute(session, "serDocTyes", vDocTypeList);
             // add these to default display attributes
             vSelectedAttr = (Vector) session.getAttribute("selectedAttr");
         }
         // add the selected type to the default display attributes
         if (vDocTypeList.contains("Preferred Question Text")
                         && !vSelectedAttr.contains("Preferred Question Text Document Text"))
             vSelectedAttr.addElement("Preferred Question Text Document Text");
         if (vDocTypeList.contains("HISTORIC SHORT CDE NAME")
                         && !vSelectedAttr.contains("Historic Short CDE Name Document Text"))
             vSelectedAttr.addElement("Historic Short CDE Name Document Text");
         if (!vSelectedAttr.contains("Reference Documents"))
             vSelectedAttr.addElement("Reference Documents");
         // get the component attribute list
         Vector vComp = new Vector();
         if (sAction.equals("searchForCreate"))
             vComp = (Vector) session.getAttribute("creAttributeList");
         else
             vComp = (Vector) session.getAttribute("serAttributeList");
         // call method to resort the display attributes
         vSelectedAttr = m_servlet.resortDisplayAttributes(vComp, vSelectedAttr);
         // store the display attributes back in the session
         if (sAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "creSelectedAttr", vSelectedAttr);
             // m_classReq.setAttribute("creSelectedAttrBlocks", vSelectedAttr);
         }
         else
             DataManager.setAttribute(session, "selectedAttr", vSelectedAttr);
         return sDocType;
     }
 
     /**
      * To get compared value to sort. called from getDESortedRows, getDECSortedRows, getVDSortedRows, getPVSortedRows
      * methods. empty strings are considered as strings. according to the fields, converts the string object into
      * integer, double or dates.
      * 
      * @param sField
      *            field name to sort.
      * @param firstName
      *            first name to compare.
      * @param SecondName
      *            second name to compare.
      * 
      * @return int ComparedValue using compareto method of the object.
      * 
      * @throws Exception
      */
     private int ComparedValue(String sField, String firstName, String SecondName) throws Exception
     {
         firstName = firstName.trim();
         SecondName = SecondName.trim();
         // this allows to put empty cells at the bottom by specify the return
         if (firstName.equals(""))
             return 1;
         else if (SecondName.equals(""))
             return -1;
         if (sField.equals("minID") || sField.equals("MaxLength") || sField.equals("MinLength")
                         || sField.equals("Decimal") || sField.equals("decUse"))
         {
             Integer iName1 = new Integer(firstName);
             Integer iName2 = new Integer(SecondName);
             return iName1.compareTo(iName2);
         }
         else if (sField.equals("version"))
         {
             Double dName1 = new Double(firstName);
             Double dName2 = new Double(SecondName);
             return dName1.compareTo(dName2);
         }
         else if ((sField.equals("BeginDate")) || (sField.equals("EndDate")) || (sField.equals("creDate"))
                         || (sField.equals("modDate")))
         {
             SimpleDateFormat dteFormat = new SimpleDateFormat("MM/dd/yyyy");
             java.util.Date dtName1 = dteFormat.parse(firstName);
             java.util.Date dtName2 = dteFormat.parse(SecondName);
             return dtName1.compareTo(dtName2);
         }
         else
         {
             return firstName.compareToIgnoreCase(SecondName);
         }
     }
 
     /**
      * To get final result vector of selected attributes/rows to display for Data Element component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the DEBean vector 'vACSearch' and adds the selected fields to result vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * 
      */
     public void getDEResult(HttpServletRequest req, HttpServletResponse res, Vector vResult, String refresh)
     {
         Vector vDE = new Vector();
         try
         {
             HttpSession session = req.getSession();
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             String actType = (String) req.getParameter("actSelected");
             if (actType == null)
                 actType = "";
             if (menuAction == null)
                 menuAction = "";
             Vector vSelAttr = new Vector();
             Vector vRSel = new Vector();
             if (menuAction.equals("searchForCreate"))
                 vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vSelAttr = (Vector) session.getAttribute("selectedAttr");
             vDE = (Vector) session.getAttribute("vACSearch");
             if (actType.equals("BEDisplayRows") || menuAction.equals("BEDisplay"))
                 vRSel = (Vector) session.getAttribute("vSelRowsBEDisplay");
             else if (menuAction.equals("searchForCreate"))
                 vRSel = (Vector) session.getAttribute("vACSearch");
             else
                 vRSel = (Vector) session.getAttribute("vSelRows");
             // Do this so vRSel is not null when SearchIn is changed to ProtocolID/CRFName
             if (vRSel == null)
                 vRSel = new Vector();
             Integer recs = new Integer(0);
             if (vRSel.size() > 0)
                 recs = new Integer(vRSel.size());
             String recs2 = "";
             if (recs != null)
                 recs2 = recs.toString();
             String sKeyword = "";
             if (menuAction.equals("searchForCreate"))
             {
             	session.setAttribute("creRecsFound", recs2);
                 sKeyword = (String) session.getAttribute("creKeyword");
             }
             else
             {
             	session.setAttribute("recsFound", recs2);
                 sKeyword = (String) session.getAttribute("serKeyword");
             }
             // make keyWordLabel label request session
             if (sKeyword == null)
                 sKeyword = "";
             if (sKeyword.equals("") && !menuAction.equals("searchForCreate"))
                 sKeyword = (String) session.getAttribute("LastAppendWord");
             // add protocol id search term to the keyword search term
             String sProtoID = (String) session.getAttribute("serProtoID");
             if (sProtoID != null && !sProtoID.equals(""))
             {
                 if (sKeyword != null && !sKeyword.equals(""))
                     sKeyword = sProtoID + " & " + sKeyword;
                 else
                     sKeyword = sProtoID;
             }
             session.setAttribute("labelKeyword", "Data Element - " + sKeyword);
             Vector vSearchID = new Vector();
             Vector vSearchName = new Vector();
             Vector vSearchLongName = new Vector();
             Vector vSearchASL = new Vector();
             Vector vSearchDefinition = new Vector();
             Vector vUsedContext = new Vector();
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 DE_Bean DEBean = new DE_Bean();
                 DEBean = (DE_Bean) vRSel.elementAt(i);
                 String sName = DEBean.getDE_LONG_NAME();
                 if (sName == null || sName.equals(""))
                     sName = DEBean.getDE_PREFERRED_NAME();
                 vSearchID.addElement(DEBean.getDE_DE_IDSEQ());
                 vSearchName.addElement(DEBean.getDE_PREFERRED_NAME());
                 vSearchLongName.addElement(DEBean.getDE_LONG_NAME());
                 vSearchASL.addElement(DEBean.getDE_ASL_NAME());
                 vSearchDefinition.addElement(DEBean.getDE_PREFERRED_DEFINITION());
                 vUsedContext.addElement(DEBean.getDE_USEDBY_CONTEXT());
                 if (vSelAttr.contains("Long Name"))
                     vResult.addElement(DEBean.getDE_LONG_NAME());
                 if (vSelAttr.contains("Public ID"))
                     vResult.addElement(DEBean.getDE_MIN_CDE_ID());
                 if (vSelAttr.contains("Version"))
                     vResult.addElement(DEBean.getDE_VERSION());
                 if (vSelAttr.contains("Registration Status"))
                     vResult.addElement(DEBean.getDE_REG_STATUS());
                 if (vSelAttr.contains("Workflow Status"))
                     vResult.addElement(DEBean.getDE_ASL_NAME());
                 if (vSelAttr.contains("Owned By Context"))
                     vResult.addElement(DEBean.getDE_CONTEXT_NAME());
                 if (vSelAttr.contains("Used By Context"))
                     vResult.addElement(DEBean.getDE_USEDBY_CONTEXT());
                 if (vSelAttr.contains("Definition"))
                     vResult.addElement(DEBean.getDE_PREFERRED_DEFINITION());
                 if (vSelAttr.contains("Data Element Concept"))
                     vResult.addElement(DEBean.getDE_DEC_NAME());
                 if (vSelAttr.contains("Value Domain"))
                     vResult.addElement(DEBean.getDE_VD_NAME());
                 if (vSelAttr.contains("Name"))
                     vResult.addElement(DEBean.getDE_PREFERRED_NAME());
                 if (vSelAttr.contains("Origin"))
                     vResult.addElement(DEBean.getDE_SOURCE());
                 if (vSelAttr.contains("Concept Name"))
                     vResult = addMultiRecordConcept(DEBean.getAC_CONCEPT_NAME(), sName, DEBean.getDE_DEC_IDSEQ(),
                                     DEBean.getDE_VD_IDSEQ(), "DataElement", vResult);
                 // if (vSelAttr.contains("Protocol ID")) vResult.addElement(DEBean.getDE_PROTOCOL_ID());
                 // if (vSelAttr.contains("CRF Name")) vResult.addElement(DEBean.getDE_CRF_NAME());
                 // put protocol id and crf with more hyperlink for multirecord attributes
                 vResult = addMultiRecordResultOther(vSelAttr, DEBean, vResult, "proto_crf");
                 if (vSelAttr.contains("Effective Begin Date"))
                     vResult.addElement(DEBean.getDE_BEGIN_DATE());
                 if (vSelAttr.contains("Effective End Date"))
                     vResult.addElement(DEBean.getDE_END_DATE());
                 if (vSelAttr.contains("Creator"))
                     vResult.addElement(DEBean.getDE_CREATED_BY());
                 if (vSelAttr.contains("Date Created"))
                     vResult.addElement(DEBean.getDE_DATE_CREATED());
                 if (vSelAttr.contains("Modifier"))
                     vResult.addElement(DEBean.getDE_MODIFIED_BY());
                 if (vSelAttr.contains("Date Modified"))
                     vResult.addElement(DEBean.getDE_DATE_MODIFIED());
                 if (vSelAttr.contains("Change Note"))
                     vResult.addElement(DEBean.getDE_CHANGE_NOTE());
                 // if (vSelAttr.contains("Language")) vResult.addElement(DEBean.getDE_LANGUAGE());
                 // get doc text and other multirecord attributes
                 vResult = addMultiRecordResultOther(vSelAttr, DEBean, vResult, "Other");
                 // if (vSelAttr.contains("Preferred Question Text Document Text"))
                 // vResult.addElement(DEBean.getDOC_TEXT_PREFERRED_QUESTION());
                 // if (vSelAttr.contains("Historic Short CDE Name Document Text"))
                 // vResult.addElement(DEBean.getDOC_TEXT_HISTORIC_NAME());
                 if (vSelAttr.contains("Alternate Names"))
                     vResult = addMultiRecordAltName(DEBean.getALTERNATE_NAME(), sName, DEBean.getDE_DE_IDSEQ(),
                                     vResult);
                 if (vSelAttr.contains("Reference Documents"))
                     vResult = addMultiRecordRDName(DEBean.getREFERENCE_DOCUMENT(), sName, DEBean.getDE_DE_IDSEQ(),
                                     vResult);
                 // vResult = addMultiRecordResultDocText(vSelAttr, DEBean, vResult);
                 if (vSelAttr.contains("Derivation Relationship"))
                     vResult = addMultiRecordDerDE(DEBean, vResult);
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
             DataManager.setAttribute(session, "SearchLongName", vSearchLongName);
             DataManager.setAttribute(session, "SearchASL", vSearchASL);
             DataManager.setAttribute(session, "SearchDefinitionAC", vSearchDefinition);
             DataManager.setAttribute(session, "SearchUsedContext", vUsedContext);
             // for Back button, put search results on a stack
             // if(!refresh.equals("true") && (!menuAction.equals("searchForCreate")))
             if (!menuAction.equals("searchForCreate"))
                 stackSearchComponents("DataElement", vDE, vRSel, vSearchID, vSearchName, vResult, vSearchASL,
                                 vSearchLongName);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getDEResult: " + e);
             logger.error("ERROR in GetACSearch-getDEResult : " + e.toString(), e);
         }
     }
 
     /**
      * returns the display value with hyperlink
      * 
      * @param de
      *            DE_Bean
      * @param vRes
      *            Vector of the results
      * @return Vector of the results
      */
     private Vector addMultiRecordDerDE(DE_Bean de, Vector<String> vRes)
     {
         String hyperText = "";
         String derRel = de.getDE_DER_RELATION();
         if (derRel != null && !derRel.equals(""))
         {
             String acID = de.getDE_DER_REL_IDSEQ();
             hyperText = derRel + "\n<a href=" + "\"" + "javascript:openDerRelationDetail('" + de.getDE_LONG_NAME()
                             + "', '" + acID + "', '" + derRel + "')" + "\"><b>Details_>></b></a>";
         }
         vRes.addElement(hyperText);
         // return teh vector
         return vRes;
     }
 
     /**
      * adds more hyperlink for concept attributes in the search results, which would open new window to get concepts of
      * AC
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
     public Vector addMultiRecordConDomain(String cdName, String acName, Vector<String> vRes) throws Exception
     {
         String hyperText = "";
         if (cdName != null && !cdName.equals(""))
         {
         	// call the api to return concept attributes according to ac type and ac idseq
             Vector cdList = new Vector();
             cdList = this.doCDSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", acName, cdList); // get
                                                                                                                         // the
                                                                                                                         // list
                                                                                                                         // of
                                                                                                                         // Conceptual
                                                                                                                         // Domains
             m_classReq.setAttribute("ConDomainList", cdList);
             m_classReq.setAttribute("VMName", acName);
             String strdetails = "";
             if(cdList != null && cdList.size()>0)
             {
           	  CD_Bean cd = (CD_Bean)cdList.elementAt(0); 
           	  strdetails = cd.getCD_LONG_NAME();
           	  if(cdList.size() > 1)
           		  strdetails+="...";
             }	  
             UtilService util = new UtilService();
             acName = util.parsedStringSingleQuote(acName);
             acName = util.parsedStringDoubleQuoteJSP(acName);
             hyperText = "<a href=" + "\"" + "javascript:openConDomainWindow('" + acName + "')" + "\""
                             + "><br><b>More_>></b></a>";
             vRes.addElement(strdetails + "  " + hyperText);
         }
         else
             vRes.addElement("");
         return vRes;
     }
 
     /**
      * adds more hyperlink for concept attributes in the search results, which would open new window to get concepts of
      * AC
      * 
      * @param conName
      *            String concept name
      * @param acName
      *            String ac name
      * @param acIDseq
      *            String ac idseq
      * @param ac2IDseq
      *            String antoher ac idseq
      * @param acType
      *            ac type
      * @param vRes
      *            display result vector
      * @return vector of modified display result vector
      * @throws java.lang.Exception
      */
     private Vector addMultiRecordConcept(String conName, String acName, String acIDseq, String ac2IDseq, String acType,
                     Vector<String> vRes) throws Exception
     {
         String hyperText = "";
         if (conName != null && !conName.equals(""))
         {
             UtilService util = new UtilService();
             acName = util.parsedStringSingleQuote(acName);
             acName = util.parsedStringDoubleQuoteJSP(acName);
             hyperText = "<a href=" + "\"" + "javascript:openConceptWindow('" + acName + "','" + acIDseq + "','"
                             + ac2IDseq + "','" + acType + "')" + "\"" + "><br><b>Details_>></b></a>";
             vRes.addElement(conName + "  " + hyperText);
         }
         else
             vRes.addElement("");
         return vRes;
     }
 
     /**
      * Adds details hyperlink for alt names in the search resutls of an ac
      * 
      * @param altName
      *            String altername name
      * @param acName
      *            STring ac name
      * @param acID
      *            String ac_idseq
      * @param vRes
      *            Vector of search results to display
      * @return Vector of search results to display
      * @throws Exception
      */
     private Vector addMultiRecordAltName(String altName, String acName, String acID, Vector<String> vRes)
                     throws Exception
     {
         String hyperText = "";
         if (altName != null && !altName.equals(""))
         {
             UtilService util = new UtilService();
             acName = util.parsedStringSingleQuote(acName);
             acName = util.parsedStringDoubleQuoteJSP(acName);
             hyperText = "<a href=" + "\"" + "javascript:openAltNameWindow('ALL','" + acID + "')" + "\""
                             + "><br><b>Details_>></b></a>";
             vRes.addElement(altName + "  " + hyperText);
         }
         else
             vRes.addElement("");
         return vRes;
     }
 
     /**
      * Adds details hyperlink for ref docs in the search resutls of an ac
      * 
      * @param rdName
      *            String ref docuement
      * @param acName
      *            STring ac name
      * @param acID
      *            String ac_idseq
      * @param vRes
      *            Vector of search results to display
      * @return Vector of search results to display
      * @throws Exception
      */
     private Vector addMultiRecordRDName(String rdName, String acName, String acID, Vector<String> vRes)
                     throws Exception
     {
         String hyperText = "";
         if (rdName != null && !rdName.equals(""))
         {
             UtilService util = new UtilService();
             acName = util.parsedStringSingleQuote(acName);
             acName = util.parsedStringDoubleQuoteJSP(acName);
             hyperText = "<a href=" + "\"" + "javascript:openRefDocWindow('ALL TYPES','" + acID + "')" + "\""
                             + "><br><b>Details_>></b></a>";
             vRes.addElement(rdName + "  " + hyperText);
         }
         else
             vRes.addElement("");
         return vRes;
     }
 
     /**
      * adds more hyperlink for some attributes in the search results, which would open new window to get all attributes
      * of AC
      * 
      * @param vSel
      *            vector of ac display attributes bean
      * @param deBean
      *            current de bean
      * @param vRes
      *            display result vector
      * @param disType
      *            type of attribute to make the more hyperlink
      * @return vector of modified display result vector
      * @throws java.lang.Exception
      */
     private Vector addMultiRecordResultOther(Vector vSel, DE_Bean deBean, Vector vRes, String disType) throws Exception
     {
         String hyperText = "";
         if (disType.equals("proto_crf"))
         {
             // protocol id
             if (vSel.contains("Protocol ID") && deBean.getDE_PROTO_CRF_Count() != null
                             && deBean.getDE_PROTO_CRF_Count().intValue() >= 1)
             {
                 String acName = deBean.getDE_LONG_NAME();
                 if (acName == null || acName.equals(""))
                     acName = deBean.getDE_PREFERRED_NAME();
                 UtilService util = new UtilService();
                 acName = util.parsedStringSingleQuote(acName);
                 acName = util.parsedStringDoubleQuoteJSP(acName);
                 hyperText = "<a href=" + "\"" + "javascript:openProtoCRFWindow('" + acName + "','"
                                 + deBean.getDE_DE_IDSEQ() + "')" + "\"" + "><b>More_>></b></a>";
                 vRes.addElement(deBean.getDE_PROTOCOL_ID() + "  " + hyperText);
             }
             else if (vSel.contains("Protocol ID"))
                 vRes.addElement(deBean.getDE_PROTOCOL_ID());
             // crf Name
             if (vSel.contains("CRF Name") && deBean.getDE_PROTO_CRF_Count() != null
                             && deBean.getDE_PROTO_CRF_Count().intValue() >= 1)
             {
                 String acName = deBean.getDE_LONG_NAME();
                 if (acName == null || acName.equals(""))
                     acName = deBean.getDE_PREFERRED_NAME();
                 UtilService util = new UtilService();
                 acName = util.parsedStringSingleQuote(acName);
                 acName = util.parsedStringDoubleQuoteJSP(acName);
                 hyperText = "<a href=" + "\"" + "javascript:openProtoCRFWindow('" + acName + "','"
                                 + deBean.getDE_DE_IDSEQ() + "')" + "\"" + "><b>More_>></b></a>";
                 vRes.addElement(deBean.getDE_CRF_NAME() + "  " + hyperText);
             }
             else if (vSel.contains("CRF Name"))
                 vRes.addElement(deBean.getDE_CRF_NAME());
         }
         else
         {
             // historical cde id
             /*
              * if (vSel.contains("Historical CDE ID") && deBean.getDE_HIST_CDE_ID_COUNT() != null &&
              * deBean.getDE_HIST_CDE_ID_COUNT().intValue() >= 1) { hyperText = "<a href=" + "\"" +
              * "javascript:openAltNameWindow('HISTORICAL_CDE_ID','" + deBean.getDE_DE_IDSEQ() + "')" + "\"" + "><b>More_>></b></a>";
              * vRes.addElement(deBean.getDE_HIST_CDE_ID() + " " + hyperText); } else if (vSel.contains("Historical CDE
              * ID")) vRes.addElement(deBean.getDE_HIST_CDE_ID());
              */
             // permissible value
             if (vSel.contains("Permissible Value") && deBean.getDE_Permissible_Value_Count() != null
                             && deBean.getDE_Permissible_Value_Count().intValue() >= 1)
             {
                 String acName = deBean.getDE_LONG_NAME();
                 if (acName == null || acName.equals(""))
                     acName = deBean.getDE_PREFERRED_NAME();
                 UtilService util = new UtilService();
                 acName = util.parsedStringSingleQuote(acName);
                 acName = util.parsedStringDoubleQuoteJSP(acName);
                 hyperText = "<a href=" + "\"" + "javascript:openPermValueWindow('" + acName + "','"
                                 + deBean.getDE_VD_IDSEQ() + "')" + "\"" + "><b>More_>></b></a>";
                 vRes.addElement(deBean.getDE_Permissible_Value() + "  " + hyperText);
             }
             else if (vSel.contains("Permissible Value"))
                 vRes.addElement(deBean.getDE_Permissible_Value());
         }
         return vRes;
     }
 
     
     /**
      * @param vmName
      * @param pvCount
      * @param vRes
      * @return
      * @throws Exception
      */
     private Vector addEditVMLink(String vmName, String pvCount, Vector<String> vRes) throws Exception
     {
     	String hyperText = "";
         if (vmName != null && !vmName.equals(""))
         {
             UtilService util = new UtilService();
             hyperText = "<a href=" + "\"" + "javascript:openEditVMWindow('" + pvCount + "')" + "\"" + "><br><b>Edit_VM_>></b></a>";
             vRes.addElement(vmName + "  " + hyperText);
         }
         else
             vRes.addElement("");
         return vRes;
     }
     
     /**
      * get DDE info, including RepType, rule, method, Conca_Char and all DE Components and set them to session
      * 
      * calls oracle stored procedure get_complex_de This method is called by getSelRowToEdit(), by
      * doSearchSelectionAction() or doSearchSelectionBEAction()
      * 
      * @param P_DE_IDSEQ
      *            String in, Primary DE's idseq.
      * 
      */
     public void getDDEInfo(String P_DE_IDSEQ)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         HttpSession session = m_classReq.getSession();
         String sRepType = "";
         String sRule = "";
         String sMethod = "";
         String sConcatChar = "";
         Vector<String> vDEComp = new Vector<String>();
         Vector<String> vDECompID = new Vector<String>();
         Vector<String> vDECompOrder = new Vector<String>();
         Vector<String> vDECompRelID = new Vector<String>();
         Vector<String> vDECompDelete = new Vector<String>();
         String sRulesAction = "newRule";
         try
         {
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn()
                                 .prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.GET_COMPLEX_DE(?,?,?,?,?,?,?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(2, java.sql.Types.VARCHAR);
                 cstmt.registerOutParameter(3, java.sql.Types.VARCHAR);
                 cstmt.registerOutParameter(4, java.sql.Types.VARCHAR);
                 cstmt.registerOutParameter(5, java.sql.Types.VARCHAR);
                 cstmt.registerOutParameter(6, OracleTypes.CURSOR);
                 cstmt.registerOutParameter(7, java.sql.Types.VARCHAR);
                 cstmt.registerOutParameter(8, java.sql.Types.VARCHAR);
                 cstmt.registerOutParameter(9, java.sql.Types.VARCHAR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, P_DE_IDSEQ);
                 // Now we are ready to call the stored procedure
                 cstmt.execute();
                 // assign output DDE info from the result
                 sRepType = (String) cstmt.getString(5);
                 DDE_Bean dde = new DDE_Bean();
                 if (sRepType != null && sRepType.length() > 1)
                 {
                     sMethod = (String) cstmt.getString(2);
                     sRule = (String) cstmt.getString(3);
                     sConcatChar = (String) cstmt.getString(4);
                     if (sMethod == null)
                         sMethod = "";
                     if (sRule == null)
                         sRule = "";
                     if (sConcatChar == null)
                         sConcatChar = "";
                     sRulesAction = "existedRule";
                     // add to dde bean
                     dde.setDE_IDSEQ(P_DE_IDSEQ);
                     dde.setCRTL_NAME(sRepType);
                     dde.setMETHOD(sMethod);
                     dde.setRULE(sRule);
                     dde.setCONCAT_CHAR(sConcatChar);
                     dde.setRULES_ACTION(sRulesAction);
                     dde.setDE_NAME(cstmt.getString(7));
                     dde.setDE_ID(cstmt.getString(8));
                     dde.setDE_VERSION(cstmt.getString(9));
                     dde.setSUBMIT_ACTION("NONE");
                     Vector<DE_COMP_Bean> vComps = new Vector<DE_COMP_Bean>();
                     // set DE Comp vectors from resultset
                     rs = (ResultSet) cstmt.getObject(6);
                     if (rs != null)
                     {
                         // loop through the resultSet and add them to the vector
                         while (rs.next())
                         {
                             vDECompRelID.addElement(rs.getString(1));
                             vDEComp.addElement(rs.getString(2));
                             vDECompID.addElement(rs.getString(3));
                             vDECompOrder.addElement(rs.getString(4));
                             // add to de comp bean
                             DE_COMP_Bean dcomp = new DE_COMP_Bean();
                             dcomp.setCOMP_REL_IDSEQ(rs.getString(1));
                             dcomp.setCOMP_NAME(rs.getString(2));
                             dcomp.setCOMP_IDSEQ(rs.getString(3));
                             dcomp.setDISPLAY_ORDER(rs.getString(4));
                             dcomp.setPUBLIC_ID(rs.getString(5));
                             dcomp.setVERSION(rs.getString(6));
                             dcomp.setPARENT_IDSEQ(P_DE_IDSEQ);
                             dcomp.setSUBMIT_ACTION("NONE");
                             vComps.addElement(dcomp);
                         }
                         dde.setDE_COMP_List(vComps);
                         // sort vDEComp against DECompOrder
                         UtilService util = new UtilService();
                         util.sortDEComps(vDEComp, vDECompID, vDECompRelID, vDECompOrder);
                     }
                 }
                 // save them to session, even empty, refresh it
                 DataManager.setAttribute(session, "DerivedDE", dde);
                 DataManager.setAttribute(session, "vDEComp", vDEComp);
                 DataManager.setAttribute(session, "vDECompID", vDECompID);
                 DataManager.setAttribute(session, "vDECompOrder", vDECompOrder);
                 DataManager.setAttribute(session, "vDECompRelID", vDECompRelID);
                 DataManager.setAttribute(session, "vDECompDelete", vDECompDelete);
                 DataManager.setAttribute(session, "sRepType", sRepType);
                 DataManager.setAttribute(session, "sRule", sRule);
                 DataManager.setAttribute(session, "sMethod", sMethod);
                 DataManager.setAttribute(session, "sConcatChar", sConcatChar);
                 DataManager.setAttribute(session, "sRulesAction", sRulesAction);
                 // store it in the session if bad type
                 String dbType = sRepType;
                 Vector vRepType = (Vector) session.getAttribute("vRepType");
                 if (vRepType != null && sRepType != null && !vRepType.contains(sRepType))
                     DataManager.setAttribute(session, "NotValidDBType", dbType);
                 else
                     DataManager.setAttribute(session, "NotValidDBType", "");
             }
         }
         catch (Exception e)
         {
             logger.error("ERROR - getDDEInfo : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
     }
 
     /**
      * To get final result vector of selected attributes/rows to display for Data Element Concept component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the DECBean vector 'vACSearch' and adds the selected fields to result
      * vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * @param refresh
      *            String refresh action true or false value
      * 
      */
     public void getDECResult(HttpServletRequest req, HttpServletResponse res, Vector<String> vResult, String refresh)
     {
         Vector vDEC = new Vector();
         try
         {
             HttpSession session = req.getSession();
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             // Vector vSearchASL2 = (Vector)session.getAttribute("SearchASL");
             Vector vSelAttr = new Vector();
             Vector vRSel = new Vector();
             String actType = (String) req.getParameter("actSelected");
             if (actType == null)
                 actType = "";
             if (menuAction.equals("searchForCreate"))
                 vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vSelAttr = (Vector) session.getAttribute("selectedAttr");
             // all search results
             vDEC = (Vector) session.getAttribute("vACSearch");
             if (actType.equals("BEDisplayRows") || menuAction.equals("BEDisplay"))
                 vRSel = (Vector) session.getAttribute("vSelRowsBEDisplay");
             else if (menuAction.equals("searchForCreate"))
                 vRSel = (Vector) session.getAttribute("vACSearch");
             else
                 vRSel = (Vector) session.getAttribute("vSelRows");
             // convert int to string to get the rec count
             if (vRSel == null)
                 vRSel = new Vector();
             Integer recs = new Integer(0);
             if (vRSel.size() > 0)
                 recs = new Integer(vRSel.size());
             String recs2 = "";
             if (recs != null)
                 recs2 = recs.toString();
             String sKeyword = "";
             if (menuAction.equals("searchForCreate"))
             {
             	session.setAttribute("creRecsFound", recs2);
                 sKeyword = (String) session.getAttribute("creKeyword");
             }
             else
             {
             	session.setAttribute("recsFound", recs2);
                 sKeyword = (String) session.getAttribute("serKeyword");
             }
             // make keyWordLabel label request session
             if (sKeyword.equals("") && !menuAction.equals("searchForCreate"))
                 sKeyword = (String) session.getAttribute("LastAppendWord");
             if (sKeyword == null)
                 sKeyword = "";
             session.setAttribute("labelKeyword", "Data Element Concept - " + sKeyword);
             Vector<String> vSearchID = new Vector<String>();
             Vector<String> vSearchName = new Vector<String>();
             Vector<String> vSearchLongName = new Vector<String>();
             Vector<String> vSearchASL = new Vector<String>();
             Vector<String> vSearchDefinition = new Vector<String>();
             Vector<String> vUsedContext = new Vector<String>();
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 DEC_Bean DECBean = new DEC_Bean();
                 DECBean = (DEC_Bean) vRSel.elementAt(i);
                 String sName = DECBean.getDEC_LONG_NAME();
                 if (sName == null || sName.equals(""))
                     sName = DECBean.getDEC_PREFERRED_NAME();
                 vSearchID.addElement(DECBean.getDEC_DEC_IDSEQ());
                 vSearchName.addElement(DECBean.getDEC_PREFERRED_NAME());
                 vSearchLongName.addElement(DECBean.getDEC_LONG_NAME());
                 vSearchASL.addElement(DECBean.getDEC_ASL_NAME());
                 vSearchDefinition.addElement(DECBean.getDEC_PREFERRED_DEFINITION());
                 vUsedContext.addElement(DECBean.getDEC_USEDBY_CONTEXT());
                 if (vSelAttr.contains("Long Name"))
                     vResult.addElement(DECBean.getDEC_LONG_NAME());
                 if (vSelAttr.contains("Public ID"))
                     vResult.addElement(DECBean.getDEC_DEC_ID());
                 if (vSelAttr.contains("Version"))
                     vResult.addElement(DECBean.getDEC_VERSION());
                 if (vSelAttr.contains("Workflow Status"))
                     vResult.addElement(DECBean.getDEC_ASL_NAME());
                 if (vSelAttr.contains("Context"))
                     vResult.addElement(DECBean.getDEC_CONTEXT_NAME());
                 if (vSelAttr.contains("Definition"))
                     vResult.addElement(DECBean.getDEC_PREFERRED_DEFINITION());
                 if (vSelAttr.contains("Name"))
                     vResult.addElement(DECBean.getDEC_PREFERRED_NAME());
                 if (vSelAttr.contains("Conceptual Domain"))
                     vResult.addElement(DECBean.getDEC_CD_NAME());
                 if (vSelAttr.contains("Origin"))
                     vResult.addElement(DECBean.getDEC_SOURCE());
                 if (vSelAttr.contains("Concept Name"))
                     vResult = addMultiRecordConcept(DECBean.getAC_CONCEPT_NAME(), sName, DECBean
                                     .getDEC_DEC_IDSEQ(), "", "DataElementConcept", vResult);
                 if (vSelAttr.contains("Effective Begin Date"))
                     vResult.addElement(DECBean.getDEC_BEGIN_DATE());
                 if (vSelAttr.contains("Effective End Date"))
                     vResult.addElement(DECBean.getDEC_END_DATE());
                 if (vSelAttr.contains("Creator"))
                     vResult.addElement(DECBean.getDEC_CREATED_BY());
                 if (vSelAttr.contains("Date Created"))
                     vResult.addElement(DECBean.getDEC_DATE_CREATED());
                 if (vSelAttr.contains("Modifier"))
                     vResult.addElement(DECBean.getDEC_MODIFIED_BY());
                 if (vSelAttr.contains("Date Modified"))
                     vResult.addElement(DECBean.getDEC_DATE_MODIFIED());
                 if (vSelAttr.contains("Change Note"))
                     vResult.addElement(DECBean.getDEC_CHANGE_NOTE());
                 if (vSelAttr.contains("Alternate Names"))
                     vResult = addMultiRecordAltName(DECBean.getALTERNATE_NAME(), sName,
                                     DECBean.getDEC_DEC_IDSEQ(), vResult);
                 if (vSelAttr.contains("Reference Documents"))
                     vResult = addMultiRecordRDName(DECBean.getREFERENCE_DOCUMENT(), sName, DECBean
                                     .getDEC_DEC_IDSEQ(), vResult);
                 // if (vSelAttr.contains("Language")) vResult.addElement(DECBean.getDEC_LANGUAGE());
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
             DataManager.setAttribute(session, "SearchLongName", vSearchLongName);
             DataManager.setAttribute(session, "SearchASL", vSearchASL);
             DataManager.setAttribute(session, "SearchDefinitionAC", vSearchDefinition);
             DataManager.setAttribute(session, "SearchUsedContext", vUsedContext);
             // for Back button, put search results on a stack
             if (!refresh.equals("true") && (!menuAction.equals("searchForCreate")))
                 stackSearchComponents("DataElementConcept", vDEC, vRSel, vSearchID, vSearchName, vResult,
                                 vSearchASL, vSearchLongName);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getDECResult: " + e);
             logger.error("ERROR in GetACSearch-getDECResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get final result vector of selected attributes/rows to display for Value Domain component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the VDBean vector 'vACSearch' and adds the selected fields to result vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * @param refresh
      *            String true or false value of refresh action
      * 
      */
     public void getVDResult(HttpServletRequest req, HttpServletResponse res, Vector<String> vResult, String refresh)
     {
         Vector vVD = new Vector();
         try
         {
             HttpSession session = req.getSession();
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             Vector vSelAttr = new Vector();
             Vector vRSel = new Vector();
             String actType = (String) req.getParameter("actSelected");
             if (actType == null)
                 actType = "";
             if (menuAction.equals("searchForCreate"))
                 vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vSelAttr = (Vector) session.getAttribute("selectedAttr");
             vVD = (Vector) session.getAttribute("vACSearch");
             if (actType.equals("BEDisplayRows") || menuAction.equals("BEDisplay"))
                 vRSel = (Vector) session.getAttribute("vSelRowsBEDisplay");
             else if (menuAction.equals("searchForCreate"))
                 vRSel = (Vector) session.getAttribute("vACSearch");
             else
                 vRSel = (Vector) session.getAttribute("vSelRows");
             // if (menuAction.equals("searchForCreate")) vRSel = null;
             // if (vRSel == null)
             // vRSel = vVD;
             if (vRSel == null)
                 vRSel = new Vector();
             Integer recs = new Integer(0);
             if (vRSel.size() > 0)
                 recs = new Integer(vRSel.size());
             String recs2 = "";
             if (recs != null)
                 recs2 = recs.toString();
             String sKeyword = "";
             if (menuAction.equals("searchForCreate"))
             {
             	session.setAttribute("creRecsFound", recs2);
                 sKeyword = (String) session.getAttribute("creKeyword");
             }
             else
             {
             	session.setAttribute("recsFound", recs2);
                 sKeyword = (String) session.getAttribute("serKeyword");
             }
             // make keyWordLabel label request session
             if (sKeyword.equals("") && !menuAction.equals("searchForCreate"))
                 sKeyword = (String) session.getAttribute("LastAppendWord");
             if (sKeyword == null)
                 sKeyword = "";
             session.setAttribute("labelKeyword", "Value Domain - " + sKeyword);
             Vector<String> vSearchID = new Vector<String>();
             Vector<String> vSearchName = new Vector<String>();
             Vector<String> vSearchLongName = new Vector<String>();
             Vector<String> vSearchASL = new Vector<String>();
             Vector<String> vSearchDefinition = new Vector<String>();
             Vector<String> vUsedContext = new Vector<String>();
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 VD_Bean VDBean = new VD_Bean();
                 VDBean = (VD_Bean) vRSel.elementAt(i);
                 String sName = VDBean.getVD_LONG_NAME();
                 if (sName == null || sName.equals(""))
                     sName = VDBean.getVD_PREFERRED_NAME();
                 vSearchID.addElement(VDBean.getVD_VD_IDSEQ());
                 vSearchName.addElement(VDBean.getVD_PREFERRED_NAME());
                 vSearchLongName.addElement(VDBean.getVD_LONG_NAME());
                 vSearchASL.addElement(VDBean.getVD_ASL_NAME());
                 vSearchDefinition.addElement(VDBean.getVD_PREFERRED_DEFINITION());
                 vUsedContext.addElement(VDBean.getVD_USEDBY_CONTEXT());
                 if (vSelAttr.contains("Long Name"))
                     vResult.addElement(VDBean.getVD_LONG_NAME());
                 if (vSelAttr.contains("Public ID"))
                     vResult.addElement(VDBean.getVD_VD_ID());
                 if (vSelAttr.contains("Version"))
                     vResult.addElement(VDBean.getVD_VERSION());
                 if (vSelAttr.contains("Workflow Status"))
                     vResult.addElement(VDBean.getVD_ASL_NAME());
                 if (vSelAttr.contains("Context"))
                     vResult.addElement(VDBean.getVD_CONTEXT_NAME());
                 if (vSelAttr.contains("Definition"))
                     vResult.addElement(VDBean.getVD_PREFERRED_DEFINITION());
                 if (vSelAttr.contains("Name"))
                     vResult.addElement(VDBean.getVD_PREFERRED_NAME());
                 if (vSelAttr.contains("Conceptual Domain"))
                     vResult.addElement(VDBean.getVD_CD_NAME());
                 if (vSelAttr.contains("Data Type"))
                     vResult.addElement(VDBean.getVD_DATA_TYPE());
                 if (vSelAttr.contains("Origin"))
                     vResult.addElement(VDBean.getVD_SOURCE());
                 if (vSelAttr.contains("Concept Name"))
                     vResult = addMultiRecordConcept(VDBean.getAC_CONCEPT_NAME(), sName, "", VDBean
                                     .getVD_VD_IDSEQ(), "ValueDomain", vResult);
                 if (vSelAttr.contains("Effective Begin Date"))
                     vResult.addElement(VDBean.getVD_BEGIN_DATE());
                 if (vSelAttr.contains("Effective End Date"))
                     vResult.addElement(VDBean.getVD_END_DATE());
                 if (vSelAttr.contains("Creator"))
                     vResult.addElement(VDBean.getVD_CREATED_BY());
                 if (vSelAttr.contains("Date Created"))
                     vResult.addElement(VDBean.getVD_DATE_CREATED());
                 if (vSelAttr.contains("Modifier"))
                     vResult.addElement(VDBean.getVD_MODIFIED_BY());
                 if (vSelAttr.contains("Date Modified"))
                     vResult.addElement(VDBean.getVD_DATE_MODIFIED());
                 if (vSelAttr.contains("Change Note"))
                     vResult.addElement(VDBean.getVD_CHANGE_NOTE());
                 // if (vSelAttr.contains("Language")) vResult.addElement(VDBean.getVD_LANGUAGE());
                 if (vSelAttr.contains("Unit of Measures"))
                     vResult.addElement(VDBean.getVD_UOML_NAME());
                 if (vSelAttr.contains("Display Format"))
                     vResult.addElement(VDBean.getVD_FORML_NAME());
                 if (vSelAttr.contains("Maximum Length"))
                     vResult.addElement(VDBean.getVD_MAX_LENGTH_NUM());
                 if (vSelAttr.contains("Minimum Length"))
                     vResult.addElement(VDBean.getVD_MIN_LENGTH_NUM());
                 if (vSelAttr.contains("High Value Number"))
                     vResult.addElement(VDBean.getVD_HIGH_VALUE_NUM());
                 if (vSelAttr.contains("Low Value Number"))
                     vResult.addElement(VDBean.getVD_LOW_VALUE_NUM());
                 if (vSelAttr.contains("Decimal Place"))
                     vResult.addElement(VDBean.getVD_DECIMAL_PLACE());
                 if (vSelAttr.contains("Type Flag"))
                     vResult.addElement(VDBean.getVD_TYPE_FLAG());
                 // permissible value
                 if (vSelAttr.contains("Permissible Value") && VDBean.getVD_Permissible_Value_Count() != null
                                 && VDBean.getVD_Permissible_Value_Count().intValue() >= 1)
                 {
                     String acName = VDBean.getVD_LONG_NAME();
                     if (acName == null || acName.equals(""))
                         acName = VDBean.getVD_PREFERRED_NAME();
                     UtilService util = new UtilService();
                     acName = util.parsedStringSingleQuote(acName);
                     acName = util.parsedStringDoubleQuoteJSP(acName);
                     String hyperText = "<a href=" + "\"" + "javascript:openPermValueWindow('" + acName + "','"
                                     + VDBean.getVD_VD_IDSEQ() + "')" + "\"" + "><b>More_>></b></a>";
                     vResult.addElement(VDBean.getVD_Permissible_Value() + "  " + hyperText);
                 }
                 else if (vSelAttr.contains("Permissible Value"))
                     vResult.addElement(VDBean.getVD_Permissible_Value());
                 if (vSelAttr.contains("Alternate Names"))
                     vResult = addMultiRecordAltName(VDBean.getALTERNATE_NAME(), sName, VDBean.getVD_VD_IDSEQ(),
                                     vResult);
                 if (vSelAttr.contains("Reference Documents"))
                     vResult = addMultiRecordRDName(VDBean.getREFERENCE_DOCUMENT(), sName, VDBean.getVD_VD_IDSEQ(),
                                     vResult);
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
             DataManager.setAttribute(session, "SearchLongName", vSearchLongName);
             DataManager.setAttribute(session, "SearchASL", vSearchASL);
             DataManager.setAttribute(session, "SearchDefinitionAC", vSearchDefinition);
             DataManager.setAttribute(session, "SearchUsedContext", vUsedContext);
             // for Back button, put search results on a stack
             if (!refresh.equals("true") && (!menuAction.equals("searchForCreate")))
                 stackSearchComponents("ValueDomain", vVD, vRSel, vSearchID, vSearchName, vResult, vSearchASL,
                                 vSearchLongName);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getVDResult: " + e);
             logger.error("ERROR in GetACSearch-getVDResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get final result vector of selected attributes/rows to display for Data Element Concept component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the CDBean vector 'vACSearch' and adds the selected fields to result vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * @param refresh
      *            String true or false value of refresh action
      * 
      */
     private void getCDResult(HttpServletRequest req, HttpServletResponse res, Vector<String> vResult, String refresh)
     {
         Vector vCD = new Vector();
         try
         {
             HttpSession session = req.getSession();
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             Vector vSearchASL2 = (Vector) session.getAttribute("SearchASL");
             Vector vSelAttr = new Vector();
             if (menuAction.equals("searchForCreate"))
                 vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vSelAttr = (Vector) session.getAttribute("selectedAttr");
             vCD = (Vector) session.getAttribute("vACSearch");
             Vector vRSel = new Vector();
             if (menuAction.equals("searchForCreate"))
                 vRSel = (Vector) session.getAttribute("vACSearch");
             else
                 vRSel = (Vector) session.getAttribute("vSelRows");
             // if (menuAction.equals("searchForCreate")) vRSel = null;
             // if (vRSel == null)
             // vRSel = vCD;
             // convert int to string to get the rec count
             if (vRSel == null)
                 vRSel = new Vector();
             Integer recs = new Integer(0);
             if (vRSel.size() > 0)
                 recs = new Integer(vRSel.size());
             String recs2 = "";
             if (recs != null)
                 recs2 = recs.toString();
             String sKeyword = "";
             if (menuAction.equals("searchForCreate"))
             {
             	session.setAttribute("creRecsFound", recs2);
                 sKeyword = (String) session.getAttribute("creKeyword");
             }
             else
             {
             	session.setAttribute("recsFound", recs2);
                 sKeyword = (String) session.getAttribute("serKeyword");
             }
             // make keyWordLabel label request session
             if (sKeyword.equals("") && !menuAction.equals("searchForCreate"))
                 sKeyword = (String) session.getAttribute("LastAppendWord");
             if (sKeyword == null)
                 sKeyword = "";
             session.setAttribute("labelKeyword", "Conceptual Domain - " + sKeyword);
             Vector<String> vSearchID = new Vector<String>();
             Vector<String> vSearchName = new Vector<String>();
             Vector<String> vSearchLongName = new Vector<String>();
             Vector<String> vSearchASL = new Vector<String>();
             Vector<String> vSearchDefinition = new Vector<String>();
             Vector<String> vUsedContext = new Vector<String>();
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 CD_Bean CDBean = new CD_Bean();
                 CDBean = (CD_Bean) vRSel.elementAt(i);
                 String sName = CDBean.getCD_LONG_NAME();
                 if (sName == null || sName.equals(""))
                     sName = CDBean.getCD_PREFERRED_NAME();
                 vSearchID.addElement(CDBean.getCD_CD_IDSEQ());
                 vSearchName.addElement(CDBean.getCD_PREFERRED_NAME());
                 vSearchLongName.addElement(CDBean.getCD_LONG_NAME());
                 vSearchASL.addElement(CDBean.getCD_ASL_NAME());
                 vSearchDefinition.addElement(CDBean.getCD_PREFERRED_DEFINITION());
                 vUsedContext.addElement(CDBean.getCD_CONTEXT_NAME());
                 if (vSelAttr.contains("Long Name"))
                     vResult.addElement(CDBean.getCD_LONG_NAME());
                 if (vSelAttr.contains("Public ID"))
                     vResult.addElement(CDBean.getCD_CD_ID());
                 if (vSelAttr.contains("Version"))
                     vResult.addElement(CDBean.getCD_VERSION());
                 if (vSelAttr.contains("Workflow Status"))
                     vResult.addElement(CDBean.getCD_ASL_NAME());
                 if (vSelAttr.contains("Context"))
                     vResult.addElement(CDBean.getCD_CONTEXT_NAME());
                 if (vSelAttr.contains("Definition"))
                     vResult.addElement(CDBean.getCD_PREFERRED_DEFINITION());
                 if (vSelAttr.contains("Name"))
                     vResult.addElement(CDBean.getCD_PREFERRED_NAME());
                 if (vSelAttr.contains("Origin"))
                     vResult.addElement(CDBean.getCD_SOURCE());
                 // if (vSelAttr.contains("Concept Name"))
                 // vResult = addMultiRecordConcept("", sName, CDBean.getCD_CD_IDSEQ(), "ConceptualDomain",
                 // vResult);
                 // if (vSelAttr.contains("Dimensionality")) vResult.addElement(CDBean.getCD_DIMENSIONALITY());
                 if (vSelAttr.contains("Effective Begin Date"))
                     vResult.addElement(CDBean.getCD_BEGIN_DATE());
                 if (vSelAttr.contains("Effective End Date"))
                     vResult.addElement(CDBean.getCD_END_DATE());
                 if (vSelAttr.contains("Creator"))
                     vResult.addElement(CDBean.getCD_CREATED_BY());
                 if (vSelAttr.contains("Date Created"))
                     vResult.addElement(CDBean.getCD_DATE_CREATED());
                 if (vSelAttr.contains("Modifier"))
                     vResult.addElement(CDBean.getCD_MODIFIED_BY());
                 if (vSelAttr.contains("Date Modified"))
                     vResult.addElement(CDBean.getCD_DATE_MODIFIED());
                 if (vSelAttr.contains("Change Note"))
                     vResult.addElement(CDBean.getCD_CHANGE_NOTE());
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
             DataManager.setAttribute(session, "SearchLongName", vSearchLongName);
             DataManager.setAttribute(session, "SearchASL", vSearchASL);
             DataManager.setAttribute(session, "SearchDefinitionAC", vSearchDefinition);
             DataManager.setAttribute(session, "SearchUsedContext", vUsedContext);
             // for Back button, put search results and attributes on a stack
             if (!refresh.equals("true") && (!menuAction.equals("searchForCreate")))
                 stackSearchComponents("ConceptualDomain", vCD, vRSel, vSearchID, vSearchName, vResult, vSearchASL,
                                 vSearchLongName);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getCDResult: " + e);
             logger.error("ERROR in GetACSearch-getCDResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get resultSet from database for Object Class Component called from getACKeywordResult method.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_OC(InString, ContName, ASLName,
      * OracleTypes.CURSOR)}" loop through the ResultSet and add them to bean which is added to the vector to return
      * 
      * @param InString
      *            Keyword value.
      * @param ContName
      *            selected context name.
      * @param ASLName
      *            selected workflow status name.
      * @param ID
      *            String public id of the ac
      * @param vList
      *            returns Vector of DEC bean.
      * @param type
      *            String ac type
      * @param conName
      *            String concept name to fitler
      * @param conID
      *            String concept id to fitler
      * 
      */
     public void do_caDSRSearch(String InString, String ContName, String ASLName, String ID, Vector<EVS_Bean> vList,
                     String type, String conName, String conID)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         String sCUIString = "";
         String compType = "";
         try
         {
         	if ((ID != null) && !ID.equals("")){
             	int id = Integer.parseInt(ID);
              }
  
         	// Create a Callable Statement object.
            // conn = m_servlet.connectDB(m_classReq, m_classRes);
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             if (type.equals("OC") || type.equals("ObjQ"))
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_OC(?,?,?,?,?,?,?)}");
                 cstmt.registerOutParameter(5, OracleTypes.CURSOR);
                 compType = "Object Class";
             }
             else if (type.equals("PROP") || type.equals("PropQ"))
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_PROP(?,?,?,?,?,?,?)}");
                 cstmt.registerOutParameter(5, OracleTypes.CURSOR);
                 compType = "Property";
             }
             else if (type.equals("REP") || type.equals("RepQ"))
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_REP(?,?,?,?,?,?,?)}");
                 cstmt.registerOutParameter(5, OracleTypes.CURSOR);
                 compType = "Rep Term";
             }
             cstmt.setString(1, InString);
             cstmt.setString(2, ASLName);
             cstmt.setString(3, ContName);
             cstmt.setString(4, ID);
             cstmt.setString(6, conName);
             cstmt.setString(7, conID);
             // Now we are ready to call the stored procedure
             cstmt.execute();
             // store the output in the resultset
             rs = (ResultSet) cstmt.getObject(5);
             if (rs != null)
             {
                 // loop through to printout the outstrings
                 while (rs.next())
                 {
                     EVS_Bean OCBean = new EVS_Bean();
                     String sName = m_util.removeNewLineChar(rs.getString(1));
                     OCBean.setCONCEPT_NAME(sName);
                     String slName = m_util.removeNewLineChar(rs.getString(2));
                     OCBean.setLONG_NAME(slName);
                     String sDef = m_util.removeNewLineChar(rs.getString(3));
                     OCBean.setPREFERRED_DEFINITION(sDef);
                     OCBean.setCONTE_IDSEQ(rs.getString(4));
                     OCBean.setASL_NAME(rs.getString(5));
                     OCBean.setIDSEQ(rs.getString(6));
                     // add the decimal number
                     if (rs.getString("version").indexOf('.') >= 0)
                         OCBean.setVERSION(rs.getString("version"));
                     else
                         OCBean.setVERSION(rs.getString("version") + ".0");
                     OCBean.setEVS_DEF_SOURCE(rs.getString(12));
                     OCBean.setCONDR_IDSEQ(rs.getString("condr_idseq"));
                     OCBean.setEVS_DATABASE("caDSR");
                     OCBean.setcaDSR_COMPONENT(compType);
                     OCBean.setEVS_CONCEPT_SOURCE("origin");
                     OCBean.setID(rs.getString(16));
                     OCBean.setCONTEXT_NAME(rs.getString(13));
                     String decUseURL = "";
                     // get the number of decs using this oc or prop
                     if (type.equals("OC") || type.equals("ObjQ") || type.equals("PROP") || type.equals("PropQ"))
                     {
                         String decCount = rs.getString("dec_count");
                         if (decCount != null && !decCount.equals("") && !decCount.equals("0"))
                         {
                             decUseURL = "<a href=" + "\"" + "javascript:openDECDetail('" + OCBean.getIDSEQ() + "')"
                                             + "\"" + "><b>" + decCount + "</b></a>";
                         }
                     }
                     OCBean.setDEC_USING(decUseURL);
                     // get concatenated string of concept codes for EVS Identifier
                     EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
                     sCUIString = evs.getEVSIdentifierString(rs.getString("condr_idseq"));
                     if (sCUIString == null)
                         sCUIString = "";
                     sCUIString = m_util.removeNewLineChar(sCUIString);
                     OCBean.setCONCEPT_IDENTIFIER(sCUIString);
                     vList.addElement(OCBean);
                 }
             }
         }catch (NumberFormatException e){}
         catch (Exception e)
         {
            logger.error("ERROR - GetACSearch-do_caDSRSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
     }
 
     /**
      * To get resultSet from database for Concept Class called from getACKeywordResult method.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_CONCEPT(InString, ContName, ASLName,
      * OracleTypes.CURSOR)}" loop through the ResultSet and add them to bean which is added to the vector to return
      * 
      * @param InString
      *            Keyword value.
      * @param ContName
      *            selected context name.
      * @param ASLName
      *            selected workflow status name.
      * @param vList
      *            returns Vector of DEC bean.
      * 
      */
     public Vector<EVS_Bean> do_ConceptSearch(String InString, String conIdseq, String ContName, String ASLName,
                     String conID, String decID, String vdID, Vector<EVS_Bean> vList)
     {
        // Connection conn = null;
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
         	if ((conID != null) && !conID.equals("")){
             	int id = Integer.parseInt(conID);
             }
         	HttpSession session = (HttpSession) m_classReq.getSession();
             EVS_UserBean eUser = (EVS_UserBean) m_servlet.sessionData.EvsUsrBean;
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction == null)
                 menuAction = "";
             if (eUser == null)
                 eUser = new EVS_UserBean();
             boolean bVListExist = false;
             // mark it if oc/prop/rep search was done earlier
             if (vList != null && vList.size() > 0)
                 bVListExist = true;
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_CON(?,?,?,?,?,?,?,?)}");
             cstmt.registerOutParameter(6, OracleTypes.CURSOR);
             cstmt.setString(1, InString);
             cstmt.setString(2, ASLName);
             cstmt.setString(3, ContName);
             cstmt.setString(4, conID);
             cstmt.setString(5, conIdseq);
             cstmt.setString(7, decID);
             cstmt.setString(8, vdID);
             // Now we are ready to call the stored procedure
             cstmt.execute();
             // store the output in the resultset
             rs = (ResultSet) cstmt.getObject(6);
             if (rs != null)
             {
                 // loop through to printout the outstrings
                 while (rs.next())
                 {
                     EVS_Bean conBean = new EVS_Bean();
                     String sConName = m_util.removeNewLineChar(rs.getString("long_name"));
                     conBean.setCONCEPT_NAME(sConName);
                     conBean.setLONG_NAME(sConName);
                     String sDef = m_util.removeNewLineChar(rs.getString("preferred_definition"));
                     conBean.setPREFERRED_DEFINITION(sDef);
                     conBean.setCONTE_IDSEQ(rs.getString("conte_idseq"));
                     conBean.setASL_NAME(rs.getString("asl_name"));
                     conBean.setIDSEQ(rs.getString("con_idseq"));
                     conBean.setEVS_DEF_SOURCE(rs.getString("definition_source"));
                     String sVocab = rs.getString("origin");
                     if (menuAction.equals("searchForCreate"))
                         conBean.setEVS_DATABASE("caDSR");
                     else
                         // make the vocab as evs origin from cadsr for main page concept class search
                         conBean.setEVS_DATABASE(sVocab);
                     conBean.setcaDSR_COMPONENT("Concept Class");
                     String selVocab = conBean.getVocabAttr(eUser, sVocab, EVSSearch.VOCAB_DBORIGIN,
                                     EVSSearch.VOCAB_NAME);
                     // store evs vocab name in evs origin and leave meta out
                     if (selVocab.equals(EVSSearch.META_VALUE))
                         conBean.setEVS_ORIGIN(sVocab);
                     else
                         conBean.setEVS_ORIGIN(selVocab);
                     conBean.setID(rs.getString("con_ID"));
                     if (rs.getString("version").indexOf('.') >= 0)
                         conBean.setVERSION(rs.getString("version"));
                     else
                         conBean.setVERSION(rs.getString("version") + ".0");
                     conBean.setCONTEXT_NAME(rs.getString("context"));
                     conBean.setDEC_USING("");
                     String sPref = m_util.removeNewLineChar(rs.getString("preferred_name"));
                     conBean.setCONCEPT_IDENTIFIER(sPref);
                     conBean.setNCI_CC_TYPE(rs.getString("evs_source"));
                     conBean.markNVPConcept(conBean, session);
                     // make sure it is included only once by matching evsId and concept name only if oc/prop/rep search
                     // was done earlier.
                     boolean isExist = false;
                     if (bVListExist == true)
                     {
                         for (int j = 0; j < vList.size(); j++)
                         {
                             EVS_Bean exBean = (EVS_Bean) vList.elementAt(j);
                             String curEvsId = exBean.getCONCEPT_IDENTIFIER();
                             String curLName = exBean.getLONG_NAME();
                             // check if not null or empty
                             if (curEvsId != null && !curEvsId.equals("") && curLName != null && !curLName.equals(""))
                             {
                                 // compare to the current one
                                 String evsId = conBean.getCONCEPT_IDENTIFIER();
                                 String longName = conBean.getLONG_NAME();
                                 if (evsId != null && evsId.equals(curEvsId) && longName != null
                                                 && longName.equals(curLName))
                                     isExist = true;
                             }
                         }
                     }
                     if (isExist == false)
                         vList.addElement(conBean);
                 }
             }
           }
         catch (NumberFormatException e){}
         catch (Exception e)
         {
             logger.error("ERROR - GetACSearch-do_conceptSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return vList;
     }
 
     /**
      * To get vector of checked rows, called from getACKeywordResult and getACShowResult methods. loops through the
      * vector 'vSelRows' and adds the checked rows to a vector. stored resulted vector to in session 'vSelRows'
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getRowSelectedBEDisplay(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vAC = (Vector) session.getAttribute("vACSearch");
             Vector vSelRows = new Vector();
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
             String sSearchAC = (String) session.getAttribute("searchAC");
             Vector vCheckList2 = (Vector) session.getAttribute("CheckList");
             // check if the vector has the data
             if (vSRows != null && vSRows.size() > 0)
             {
                 // loop through the searched vector to get the matched checked rows
                 for (int i = 0; i < (vSRows.size()); i++)
                 {
                     String ckName = ("CK" + i);
                     // if the number of rows selected is more then add only the checked ones to the vector of vSelRows
                     if (vCheckList2 != null && vCheckList2.contains(ckName))
                     {
                         vSelRows.addElement(vSRows.elementAt(i));
                     }
                 }
             }
             DataManager.setAttribute(session, "vSelRowsBEDisplay", vSelRows);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-rowSelectedBEDisplay: " + e);
             logger.error("ERROR in GetACSearch-rowSelectedBEDispay : " + e.toString(), e);
         }
     }
 
     /**
      * To get vector of checked rows, called from getACKeywordResult and getACShowResult methods. loops through the
      * vector 'vSelRows' and adds the checked rows to a vector. stored resulted vector to in session 'vSelRows'
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getRowSelected(HttpServletRequest req, HttpServletResponse res, boolean isNewList)
     {
         try
         {
             HttpSession session = req.getSession();
             // Vector vAC = (Vector)session.getAttribute("vACSearch");
             Vector vSelRows = new Vector();
             Vector vSRows = new Vector();
             String rNumSel = (String) req.getParameter("numSelected");
             String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             String sSearchAC = "";
             // get the searched results according to the menu action
             if (sMenu != null && sMenu.equals("searchForCreate"))
             {
                 sSearchAC = (String) session.getAttribute("creSearchAC");
                 vSRows = (Vector) session.getAttribute("vACSearch");
             }
             else
             {
                 sSearchAC = (String) session.getAttribute("searchAC");
                 vSRows = (Vector) session.getAttribute("vSelRows");
             }
             String actType = (String) req.getParameter("actSelected");
             if (actType == null)
                 actType = "";
             Vector vCheckList = new Vector();
             // check if the vector has the data
             if (vSRows != null && vSRows.size() > 0 && isNewList == false && !actType.equals("NoRowsSelected"))
             {
                 int rowNo = 0;
                 // loop through the searched vector to get the matched checked rows
                 OuterLoop: for (int i = 0; i < (vSRows.size()); i++)
                 {
                     // if the number of rows selected is more then add only the checked ones to the vector of vSelRows
                     if ((rNumSel != null) && (!rNumSel.equals("")))
                     {
                         String ckName = ("CK" + i);
                         String rSel = (String) req.getParameter(ckName);
                         if (rSel == null)
                             continue OuterLoop;
                         else
                         {
                             vCheckList.addElement("CK" + rowNo);
                             rowNo++;
                         }
                     }
                     vSelRows.addElement(vSRows.elementAt(i));
                 }
                 if (!actType.equals("BEDisplayRows"))
                 {
                     if (sMenu.equals("searchForCreate"))
                         DataManager.setAttribute(session, "creCheckList", vCheckList);
                     else
                         DataManager.setAttribute(session, "CheckList", vCheckList);
                 }
             }
             else if (actType.equals("NoRowsSelected"))
             {
                 if (sMenu.equals("searchForCreate"))
                     DataManager.setAttribute(session, "creCheckList", vCheckList);
                 else
                     DataManager.setAttribute(session, "CheckList", null);
                 vSelRows = vSRows;
             }
             else if (!sMenu.equals("searchForCreate"))
             {
                 vSelRows = vSRows;
                 // Code below ensures dupliclate enries are not put in the vectors during an append
                 Vector vSearchID = (Vector) session.getAttribute("SearchID");
                 if (vSearchID == null)
                     vSearchID = new Vector();
                 Vector vAC = (Vector) session.getAttribute("newSearchRes");
                 if (vAC != null)
                 {
                     for (int i = 0; i < (vAC.size()); i++)
                     {
                         if (sSearchAC.equals("DataElement"))
                         {
                             DE_Bean DEBean = new DE_Bean();
                             DEBean = (DE_Bean) vAC.elementAt(i);
                             if (!vSearchID.contains(DEBean.getDE_DE_IDSEQ()))
                             {
                                 vSelRows.addElement(vAC.elementAt(i));
                                 vSearchID.addElement(DEBean.getDE_DE_IDSEQ());
                             }
                         }
                         else if (sSearchAC.equals("DataElementConcept"))
                         {
                             DEC_Bean DECBean = new DEC_Bean();
                             DECBean = (DEC_Bean) vAC.elementAt(i);
                             if (!vSearchID.contains(DECBean.getDEC_DEC_IDSEQ()))
                             {
                                 vSelRows.addElement(vAC.elementAt(i));
                                 vSearchID.addElement(DECBean.getDEC_DEC_IDSEQ());
                             }
                         }
                         else if (sSearchAC.equals("ValueDomain"))
                         {
                             VD_Bean VDBean = new VD_Bean();
                             VDBean = (VD_Bean) vAC.elementAt(i);
                             if (!vSearchID.contains(VDBean.getVD_VD_IDSEQ()))
                             {
                                 vSelRows.addElement(vAC.elementAt(i));
                                 vSearchID.addElement(VDBean.getVD_VD_IDSEQ());
                             }
                         }
                     }
                     DataManager.setAttribute(session, "newSearchRes", new Vector());
                 }
             }
             if (actType != null && actType.equals("BEDisplayRows"))
                 DataManager.setAttribute(session, "vSelRowsBEDisplay", vSelRows);
             else if (!sMenu.equals("searchForCreate"))
                 DataManager.setAttribute(session, "vSelRows", vSelRows);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-rowSelected: " + e);
             logger.error("ERROR in GetACSearch-rowSelected : " + e.toString(), e);
         }
     }
 
     /**
      * To get checked row bean(single or multiple), called from NCICuration Servlet. loops through the vector 'vSelRows'
      * and gets bean of the checked row of a selected component. checks the edit permission if action is not a new from
      * template. gets many to many related components if any. calls 'getACListForEdit' to load the list for the selected
      * context. stores resulted bean in session ('m_DE') for th selected component.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      * @return boolean false if error exists
      */
     public boolean getSelRowToEdit(HttpServletRequest req, HttpServletResponse res, String sAction)
     {
         HttpSession session = req.getSession();
         Vector vBEResult = new Vector();
         boolean isValid = true;
         try
         {
             GetACService getAC = new GetACService(req, res, m_servlet);
             String sUser = (String) session.getAttribute("Username");
             String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
             DataManager.setAttribute(session, "AllAltNameList", new Vector());
             DataManager.setAttribute(session, "AllRefDocList", new Vector());
             String sSearchAC = (String) session.getAttribute("searchAC");
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
             /*if(sSearchAC.equals("ValueMeaning"))
             {	
             Vector vRSel = (Vector) session.getAttribute("vACSearch");
             vSRows=vRSel;
             session.setAttribute("vSelRows", vRSel);
             }*/
             Vector vCheckList = new Vector();
             // handle problem with menu item when get associated.
             if (sSearchAC.equals("DataElement"))
             {
                 // reset menu action is it was get associated
                 if (sMenuAction.equals("NewDECTemplate") || sMenuAction.equals("NewVDTemplate"))
                     sMenuAction = "NewDETemplate";
                 else if (sMenuAction.equals("NewDECVersion") || sMenuAction.equals("NewVDVersion"))
                     sMenuAction = "NewDEVersion";
             }
             else if (sSearchAC.equals("DataElementConcept"))
             {
                 // reset menu action is it was get associated
                 if (sMenuAction.equals("NewDETemplate") || sMenuAction.equals("NewVDTemplate"))
                     sMenuAction = "NewDECTemplate";
                 else if (sMenuAction.equals("NewDEVersion") || sMenuAction.equals("NewVDVersion"))
                     sMenuAction = "NewDECVersion";
             }
             else if (sSearchAC.equals("ValueDomain"))
             {
                 // reset menu action is it was get associated
                 if (sMenuAction.equals("NewDECTemplate") || sMenuAction.equals("NewDETemplate"))
                     sMenuAction = "NewVDTemplate";
                 else if (sMenuAction.equals("NewDECVersion") || sMenuAction.equals("NewDEVersion"))
                     sMenuAction = "NewVDVersion";
             }
             // reset the menu action
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
             // set variable but not the session attribute to avoid the problem with the menuaction other than Edit DE
             if (sAction.equals("EditDesDE"))
                 sMenuAction = sAction;
             // reset cs/csi vectors prior to each block edit
             session.setAttribute("designateMenu", sMenuAction);
             session.setAttribute("designateBE", "");
             Vector selectedRows = new Vector();
             String unCheckedRowId = (String) m_classReq.getParameter("unCheckedRowId");
             if (unCheckedRowId != null && !(unCheckedRowId == "")){
                 int selectedRowID = new Integer(unCheckedRowId);
                 selectedRows.add(vSRows.elementAt(selectedRowID));
             }else if(vSRows.size() > 0){
             	for (int i = 0; i < (vSRows.size()); i++){
             		String ckName = ("CK" + i);
                     String rSel = (String) req.getParameter(ckName);
                     if (rSel != null){
                     	DataManager.setAttribute(session, "ckName", ckName);
                         vCheckList.addElement(ckName);
                         selectedRows.add(vSRows.elementAt(i));
                         session.setAttribute("designateBE", "BlockEditDE");
                     }
             	}
             }
 
             if (selectedRows.size() > 0)
             {
                 String sContext = "";
                 String sStatus = "";
                 String strInValid = "";
                 // capture the duration
                 java.util.Date exDate = new java.util.Date();
                 // logger.info(m_servlet.getLogMessage(m_classReq, "getSelRowToEdit", "begin rowselect", exDate,
                 // exDate));
                 // loop through the searched DE result to get the matched checked rows
                 for (int i = 0; i < (selectedRows.size()); i++)
                 {
                         if (sSearchAC.equals("DataElement"))
                         {
                             DE_Bean DEBean = new DE_Bean();
                             // DEBean = (DE_Bean)selectedRows.elementAt(i);
                             DEBean = DEBean.cloneDE_Bean((DE_Bean) selectedRows.elementAt(i), "Complete");
                             String sContextID = DEBean.getDE_CONTE_IDSEQ();
                             // check the permissiion if not template and not designation
                             if (sMenuAction.equals("NewDETemplate"))
                                 DEBean.markNewAC();
                             else if (!sMenuAction.equals("EditDesDE"))
                                 strInValid = checkWritePermission("de", sUser, sContextID, getAC);
                             // back to search results page if no write permit and is one of the create item
                             if (!strInValid.equals(""))
                             {
                                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                                                                 "User does not have authorization to Create/Edit for the selected context");
                                 Vector vResult = new Vector();
                                 getDEResult(req, res, vResult, "");
                                 DataManager.setAttribute(session, "results", vResult);
                                 session.setAttribute("editID", DEBean.getIDSEQ());
                                 isValid = false;
                                 break;
                             }
                             else
                             {
                                 // get used by attributes and continue with other actions if is valid. No need to do
                                 // this here
                                 DEBean = getDEAttributes(DEBean, sAction, sMenuAction);
                                 // clone the bean to store it
                                 DE_Bean clDEBean = new DE_Bean();
                                 clDEBean = clDEBean.cloneDE_Bean(DEBean, "Complete");
                                 vBEResult.addElement(clDEBean);
                                 // store this bean to get its attributes and to use it to clear the changes
                                 if (DEBean != null)
                                    	DataManager.setAttribute(session, "oldDEBean", clDEBean);
                                 //set the begin and end dates to empty values so the user can enter new values.
                                 if(sMenuAction.equals("NewDETemplate")){
                                 DEBean.setDE_BEGIN_DATE("");
                                 DEBean.setDE_END_DATE("");
                                 }
                                 String desigHeading = DEBean.getDE_LONG_NAME()+ " ["
                                 + DEBean.getDE_MIN_CDE_ID()+"v"+DEBean.getDE_VERSION()+"]";
                                 session.setAttribute("desigHeading", desigHeading);
                                 DataManager.setAttribute(session, "m_DE", DEBean);
                             }
                         }
                         else if (sSearchAC.equals("DataElementConcept"))
                         {
                             DEC_Bean DECBean = new DEC_Bean();
                             DECBean = DECBean.cloneDEC_Bean((DEC_Bean) selectedRows.elementAt(i));
                             String sContextID = "";
                             if (DECBean != null)
                                 sContextID = DECBean.getDEC_CONTE_IDSEQ();
                             if (sMenuAction.equals("NewDECTemplate"))
                                 DECBean.markNewAC();
                             else
                                 strInValid = checkWritePermission("dec", sUser, sContextID, getAC);
                             if (!strInValid.equals(""))
                             {
                                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                                                                 "User does not have authorization to Create/Edit for the selected context");
                                 Vector vResult = new Vector();
                                 getDECResult(req, res, vResult, "");
                                 DataManager.setAttribute(session, "results", vResult);
                                 session.setAttribute("editID", DECBean.getIDSEQ());
                                 isValid = false;
                                 break;
                             }
                             else
                             {
                                 DECBean = getDECAttributes(DECBean, sAction, sMenuAction);
                                 // clone the bean to store it
                                 DEC_Bean clDECBean = new DEC_Bean();
                                 clDECBean = clDECBean.cloneDEC_Bean(DECBean);
                                 vBEResult.addElement(clDECBean);
                                 // store this bean to get its attributes and to use it to clear the changes
                                 if (DECBean != null)
                                     DataManager.setAttribute(session, "oldDECBean", clDECBean);
                                 //set the begin and end dates to empty values so the user can enter new values.
                                 if(sMenuAction.equals("NewDECTemplate")){
                                 DECBean.setDEC_BEGIN_DATE("");
                                 DECBean.setDEC_END_DATE("");
                                 }
                                 DataManager.setAttribute(session, "m_DEC", DECBean);
                                 DataManager.setAttribute(session, "oldOCIdseq", DECBean.getDEC_OCL_IDSEQ());
                                 DataManager.setAttribute(session, "oldPropIdseq", DECBean.getDEC_PROPL_IDSEQ());
                             }
                         }
                         else if (sSearchAC.equals("ValueDomain"))
                         {
                             VD_Bean VDBean = new VD_Bean();
                             VD_Bean VDBean2 = new VD_Bean();
                             VDBean2 = (VD_Bean) selectedRows.elementAt(i);
                             VDBean = VDBean.cloneVD_Bean((VD_Bean) selectedRows.elementAt(i));
                             if (VDBean != null)
                                 sContext = VDBean.getVD_CONTEXT_NAME();
                             if (VDBean != null)
                                 sStatus = VDBean.getVD_ASL_NAME();
                             String sContextID = "";
                             if (VDBean != null)
                                 sContextID = VDBean.getVD_CONTE_IDSEQ();
                             if (sMenuAction.equals("NewVDTemplate"))
                                 VDBean.markNewAC();
                             else
                                 strInValid = checkWritePermission("vd", sUser, sContextID, getAC);
                             if (!strInValid.equals(""))
                             {
                                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                                                                 "User does not have authorization to Create/Edit for the selected context");
                                 Vector vResult = new Vector();
                                 getVDResult(req, res, vResult, "");
                                 DataManager.setAttribute(session, "results", vResult);
                                 session.setAttribute("editID", VDBean.getIDSEQ());
                                 isValid = false;
                                 break;
                             }
                             else
                             {
                                 VDBean = getVDAttributes(VDBean, sAction, sMenuAction);
                                 DataManager.setAttribute(session, "TabFocus", "VD");
                                 // clone the bean to store it
                                 VD_Bean clVDBean = new VD_Bean();
                                 clVDBean = clVDBean.cloneVD_Bean(VDBean);
                                 vBEResult.addElement(clVDBean);
                                 // store this bean to get its attributes and to use it to clear the changes
                                 if (VDBean != null)
                                     DataManager.setAttribute(session, "oldVDBean", clVDBean);
                               //set the begin and end dates to empty values so the user can enter new values.
                                 if(sMenuAction.equals("NewVDTemplate")){
                                 VDBean.setVD_BEGIN_DATE("");
                                 VDBean.setVD_END_DATE("");
                                 }
                                 DataManager.setAttribute(session, "m_VD", VDBean);
                                 DataManager.setAttribute(session, "oldRepIdseq", VDBean.getVD_REP_IDSEQ());
                            }
                         }else if (sSearchAC.equals("ValueMeaning"))
                         {
                         	Session_Data sData = (Session_Data) session.getAttribute(Session_Data.CURATION_SESSION_ATTR);
                         	boolean isSuperUser = sData.UsrBean.isSuperuser();
                         	VM_Bean VMBean = new VM_Bean();
 							VMBean = (VM_Bean) selectedRows.elementAt(i);
 							if (isSuperUser) {
 							DataManager.setAttribute(session, "results", vSRows);
 							DataManager.setAttribute(session, VMForm.SESSION_SELECT_VM, VMBean);
 							DataManager.setAttribute(session, VMForm.SESSION_VM_TAB_FOCUS, VMForm.ELM_ACT_DETAIL_TAB);
 							DataManager.setAttribute(session, VMForm.SESSION_RET_PAGE, VMForm.ACT_BACK_SEARCH);
 							AltNamesDefsServlet altSer = new AltNamesDefsServlet(m_servlet, m_servlet.sessionData.UsrBean);
 							Alternates alt = altSer.getManualDefinition(m_classReq, VMForm.ELM_FORM_SEARCH_EVS);
 							if (alt != null && !alt.getName().equals(""))
 								VMBean.setVM_ALT_DEFINITION(alt.getName());
 
 							/*
 							 * String sVM = VMBean.getVM_LONG_NAME(); // ac name
 							 * for pv // call the api to return concept
 							 * attributes according to ac type and ac idseq
 							 * Vector cdList = new Vector(); cdList =
 							 * this.doCDSearch("", "", "", "", "", "", "", "",
 							 * "", "", "", "", "", "", 0, "", "", sVM, cdList); //
 							 * get // Domains req.setAttribute("ConDomainList",
 							 * cdList); req.setAttribute("VMName", sVM);
 							 */
 							// write the jsp
 							VMServlet vmser = new VMServlet(m_classReq,	m_classRes, m_servlet);
 							VM_Bean vm = (VM_Bean) session.getAttribute(VMForm.SESSION_SELECT_VM);
 							vmser.writeDetailJsp(vm);
 							} else {
 								session.setAttribute("editID", VMBean.getIDSEQ());
 								isValid = false;
 	                            break;
 							}
 	                          
 	                        }
                         
                         else if (sSearchAC.equals("Questions"))
                         {
                             // get the quest bean from the vector
                             Quest_Bean selQuestBean = new Quest_Bean();
                             selQuestBean = (Quest_Bean) selectedRows.elementAt(i);
                             DataManager.setAttribute(session, "m_Quest", selQuestBean);
                             isValid = doSelectDEforQuestion(req, res, selQuestBean, getAC);
                             break;
                         }
                     
                 }
                 // capture the duration
                 // logger.info(m_servlet.getLogMessage(m_classReq, "getSelRowToEdit", "end rowselect", exDate, new
                 // java.util.Date()));
                     vCheckList = new Vector();
                     for (int m = 0; m < (vSRows.size()); m++)
                     {
                         String ckName2 = ("CK" + m);
                         String rSel2 = (String) req.getParameter(ckName2);
                         if (rSel2 != null)
                             vCheckList.addElement(ckName2);
                     }
                 
                 DataManager.setAttribute(session, "CheckList", vCheckList);
             }
             if (sAction.equalsIgnoreCase("BlockEdit") || sAction.equalsIgnoreCase("EditDesDE"))
             {
                 getSelRowForBlockEdit(sSearchAC);
                 DataManager.setAttribute(session, "vBEResult", vBEResult);
             }
             else
             {
                 session.removeAttribute("vBEResult");
             }
             DataManager.setAttribute(session, "vACId", (Vector) m_classReq.getAttribute("vACId"));
             DataManager.setAttribute(session, "vACName", (Vector) m_classReq.getAttribute("vACName"));
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getSelRowToEdit: " + e);
             logger.error("ERROR in GetACSearch-getSelRowToEdit : " + e.toString(), e);
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create/Edit page. Please try again.");
             return false;
         }
         return isValid;
     }
 
     /**
      * The getDECAttributes method gets other attributes of DEC. Called from 'doOpenEditDECPage' method from servlet and
      * 'getSelRowEdit' method Calls doCSCSI_ACSearch to get its cs-csi attributes Calls 'doAltNameSearch' to get
      * alternate names attributes. Calls 'getCDContext' to append context to cd name.
      * 
      * @param DECBean
      *            the selected DEC_Bean
      * @param sAction
      *            string the origin action of the ac
      * @param sMenu
      *            string menu action of the ac
      * 
      * @return DEC_Bean a bean of DEC attributes
      * 
      * @throws Exception
      */
     public DEC_Bean getDECAttributes(DEC_Bean DECBean, String sAction, String sMenu) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
         // get this ac cs-csi attributes
         if (!sMenu.equals("NewDECTemplate"))
         {
             Vector vACCSI = doCSCSI_ACSearch(DECBean.getDEC_DEC_IDSEQ(), DECBean.getDEC_LONG_NAME());
             Vector vCS = (Vector) m_classReq.getAttribute("selCSNames");
             Vector vCSid = (Vector) m_classReq.getAttribute("selCSIDs");
             DECBean.setAC_CS_NAME(vCS);
             DECBean.setAC_CS_ID(vCSid);
             DECBean.setAC_AC_CSI_VECTOR(vACCSI);
             // get ref docs and alt names for this dec
             Vector vRef = doRefDocSearch(DECBean.getDEC_DEC_IDSEQ(), "ALL TYPES", "open");
             DECBean.setAC_REF_DOCS(vRef);
             Vector vAlt = doAltNameSearch(DECBean.getDEC_DEC_IDSEQ(), "", "", "EditDesDE", "open");
             DECBean.setAC_ALT_NAMES(vAlt);
         }
         // make some attributes default/empty for NUE action
         if (sMenu.equals("NewDECTemplate"))
         {
             DECBean.setDEC_VERSION("1.0");
             DECBean.setDEC_ASL_NAME("");
         }
         // make the workflow status to empty if
         if (sMenu.equals("NewDECVersion"))
         {
             DECBean.setDEC_ASL_NAME("");
             // Must preload the Alt Name/Def buffer on a new Version.
             AltNamesDefsSession.loadAsNew(m_servlet, session, DECBean);
         }
         // get all the selected ac id and name
         Vector vACid = (Vector) m_classReq.getAttribute("vACId");
         Vector vACName = (Vector) m_classReq.getAttribute("vACName");
         if (vACid == null)
             vACid = new Vector();
         if (vACName == null)
             vACName = new Vector();
         vACid.addElement(DECBean.getDEC_DEC_IDSEQ());
         String acName = "";
         if (DECBean.getDEC_LONG_NAME() != null && !DECBean.getDEC_LONG_NAME().equals(""))
             acName = DECBean.getDEC_LONG_NAME();
         else
             acName = DECBean.getDEC_PREFERRED_NAME();
         vACName.addElement(acName);
         if (!sAction.equalsIgnoreCase("BlockEdit"))
         {
             String oc_condr_idseq = DECBean.getDEC_OC_CONDR_IDSEQ();
             if (oc_condr_idseq != null)
             {
                 DataManager.setAttribute(session, "vObjectClass", null);
                 evs.fillOCVectors(oc_condr_idseq, DECBean, sMenu);
             }
             String prop_condr_idseq = DECBean.getDEC_PROP_CONDR_IDSEQ();
             if (prop_condr_idseq != null)
             {
                 DataManager.setAttribute(session, "vProperty", null);
                 evs.fillPropVectors(prop_condr_idseq, DECBean, sMenu);
             }
             DECBean.setAC_USER_PREF_NAME(DECBean.getDEC_PREFERRED_NAME());
             DECBean.setAC_PREF_NAME_TYPE("");
             // get the oc and prop information
             EVS_Bean nullEVS = null; 
             DECBean = (DEC_Bean) m_servlet.getACNames(nullEVS, "OpenDEC", DECBean);
             // get dec system name
             InsACService insAC = new InsACService(m_classReq, m_classRes, m_servlet);
             String sysName = insAC.getDECSysName(DECBean);
             if (sysName == null)
                 sysName = "";
             DECBean.setAC_SYS_PREF_NAME(sysName);
             DECBean.setAC_USER_PREF_NAME(DECBean.getDEC_PREFERRED_NAME());
             // make sure that no name type is selected
         }
         // get contexts selected so far
         Vector selContext = (Vector) m_classReq.getAttribute("SelectedContext");
         if (selContext == null)
             selContext = new Vector();
         String sContID = DECBean.getDEC_CONTE_IDSEQ();
         if (!selContext.contains(sContID))
         {
             selContext.addElement(sContID);
             m_classReq.setAttribute("SelectedContext", selContext);
             DECBean.setAC_SELECTED_CONTEXT_ID(selContext);
         }
         // get the cd name with the context
         acName = getCDContext(DECBean.getDEC_CD_IDSEQ());
         if (acName != null && !acName.equals(""))
             DECBean.setDEC_CD_NAME(acName);
         // get contact informatin
         // if (!sAction.equals("BlockEdit"))
         if (!sMenu.equals("NewDECTemplate") && !sAction.equals("Template"))
             DECBean.setAC_CONTACTS(getAC_Contacts(DECBean.getDEC_DEC_IDSEQ(), ""));
         // store ac names and ids in the req attribute
         m_classReq.setAttribute("vACId", vACid);
         m_classReq.setAttribute("vACName", vACName);
         return DECBean;
     }
 
     /**
      * The getDEAttributes method gets other attributes of DE. Called from 'doOpenEditDEPage' method from servlet and
      * 'getSelRowEdit' method Calls doCSCSI_ACSearch to get its cs-csi attributes Calls 'getCDContext' to append context
      * to cd name.
      * 
      * @param DEBean
      *            DE_Bean the selected DE_Bean
      * @param sAction
      *            string the origin action of the ac
      * @param sMenu
      *            string menu action of the ac
      * 
      * @return DE_Bean a bean of DE attributes
      * 
      * @throws Exception
      */
     public DE_Bean getDEAttributes(DE_Bean DEBean, String sAction, String sMenu) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         // get doc text long name idseq for edit
         InsACService insAC = new InsACService(m_classReq, m_classRes, m_servlet);
         // make name changed to false
         DEBean.setDEC_VD_CHANGED(false);
         String desID = insAC.getRD_ID(DEBean.getDE_DE_IDSEQ());
         if (desID != null)
             DEBean.setDOC_TEXT_PREFERRED_QUESTION_IDSEQ(desID);
         if (!sAction.equals("BlockEdit") && !sAction.equals("EditDesDE"))
             getDDEInfo(DEBean.getDE_DE_IDSEQ());
         if (sMenu.equals("NewDETemplate") || sMenu.equals("NewDEVersion"))
             DataManager.setAttribute(session, "sRulesAction", "newRule");
         // make some attributes to default/empty if NUE
         if (sMenu.equals("NewDETemplate") || sAction.equals("Template"))
         {
             DEBean.setDE_VERSION("1.0");
             DEBean.setDE_ASL_NAME("");
             DEBean.setDE_REG_STATUS("");
             DEBean.setDE_REG_STATUS_IDSEQ("");
         }
         // make the workflow status to empty if
         if (sMenu.equals("NewDEVersion"))
         {
             DEBean.setDE_ASL_NAME("");
             // Must preload the Alt Name/Def buffer on a new Version.
             AltNamesDefsSession.loadAsNew(m_servlet, session, DEBean);
         }
         if (!sMenu.equals("NewDETemplate") && !sAction.equals("Template"))
         {
             // get this ac cs-csi attributes not for template
             Vector vACCSI = doCSCSI_ACSearch(DEBean.getDE_DE_IDSEQ(), DEBean.getDE_LONG_NAME());
             Vector vCS = (Vector) m_classReq.getAttribute("selCSNames");
             Vector vCSid = (Vector) m_classReq.getAttribute("selCSIDs");
             DEBean.setAC_CS_NAME(vCS);
             DEBean.setAC_CS_ID(vCSid);
             DEBean.setAC_AC_CSI_VECTOR(vACCSI);
             // get ref docs and alt names for this de
             Vector vRef = doRefDocSearch(DEBean.getDE_DE_IDSEQ(), "ALL TYPES", "open");
             DEBean.setAC_REF_DOCS(vRef);
             Vector vAlt = doAltNameSearch(DEBean.getDE_DE_IDSEQ(), "", "", "EditDesDE", "open");
             DEBean.setAC_ALT_NAMES(vAlt);
         }
         // get all the selected ac id and name
         Vector vACid = (Vector) m_classReq.getAttribute("vACId");
         Vector vACName = (Vector) m_classReq.getAttribute("vACName");
         if (vACid == null)
             vACid = new Vector();
         if (vACName == null)
             vACName = new Vector();
         vACid.addElement(DEBean.getDE_DE_IDSEQ());
         String acName = "";
         if (DEBean.getDE_LONG_NAME() != null && !DEBean.getDE_LONG_NAME().equals(""))
             acName = DEBean.getDE_LONG_NAME();
         else
             acName = DEBean.getDE_PREFERRED_NAME();
         vACName.addElement(acName);
         // get contexts selected so far
         Vector selContext = (Vector) m_classReq.getAttribute("SelectedContext");
         if (selContext == null)
             selContext = new Vector();
         String sContID = DEBean.getDE_CONTE_IDSEQ();
         if (!selContext.contains(sContID))
         {
             selContext.addElement(sContID);
             m_classReq.setAttribute("SelectedContext", selContext);
             DEBean.setAC_SELECTED_CONTEXT_ID(selContext);
         }
         // get associated dec and vd beans into de bean
         // if (!sAction.equals("BlockEdit") && !sAction.equals("EditDesDE"))
         if (!sAction.equals("EditDesDE"))
         {
             DEBean.setAC_USER_PREF_NAME(DEBean.getDE_PREFERRED_NAME());
             DEBean.setAC_PREF_NAME_TYPE("");
             String sDECid = DEBean.getDE_DEC_IDSEQ();
             Vector vDECList = new Vector();
             doDECSearch(sDECid, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "", "",
                             vDECList);
             if (vDECList != null && vDECList.size() > 0)
                 DEBean.setDE_DEC_Bean((DEC_Bean) vDECList.elementAt(0));
             String sVDid = DEBean.getDE_VD_IDSEQ();
             Vector vVDList = new Vector();
             doVDSearch(sVDid, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "", "",
                             "", "", "", vVDList);
             if (vVDList != null && vVDList.size() > 0)
                 DEBean.setDE_VD_Bean((VD_Bean) vVDList.elementAt(0));
             // store teh current one as user preferred
             // get system and abbr name to use it later
 	        DEBean = (DE_Bean) m_servlet.getACNames("noChange", "OpenDE", DEBean);
         }
         // get contact informatin
         // if (!sAction.equals("BlockEdit") && !sAction.equals("EditDesDE"))
         if (!sMenu.equals("NewDETemplate") && !sAction.equals("Template") && !sAction.equals("EditDesDE"))
             DEBean.setAC_CONTACTS(getAC_Contacts(DEBean.getDE_DE_IDSEQ(), ""));
         // store ac names and ids in the req attribute
         m_classReq.setAttribute("vACId", vACid);
         m_classReq.setAttribute("vACName", vACName);
         return DEBean;
     }
 
     /**
      * The getVDAttributes method gets other attributes of VD. Called from 'doOpenEditVDPage' method from servlet and
      * 'getSelRowEdit' method Calls doCSCSI_ACSearch to get its cs-csi attributes Calls 'getCDContext' to append context
      * to cd name.
      * 
      * @param VDBean
      *            the selected VD_Bean
      * @param sAction
      *            string the origin action of the ac
      * @param sMenu
      *            string menu action of the ac
      * 
      * @return VD_Bean a bean of VD attributes
      * 
      * @throws Exception
      */
     public VD_Bean getVDAttributes(VD_Bean VDBean, String sAction, String sMenu) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
         // make name changed to false
         VDBean.setVDNAME_CHANGED(false);
         // get this ac cs-csi attributes
         if (!sMenu.equals("NewVDTemplate"))
         {
             Vector vACCSI = doCSCSI_ACSearch(VDBean.getVD_VD_IDSEQ(), VDBean.getVD_LONG_NAME());
             Vector vCS = (Vector) m_classReq.getAttribute("selCSNames");
             Vector vCSid = (Vector) m_classReq.getAttribute("selCSIDs");
             VDBean.setAC_CS_NAME(vCS);
             VDBean.setAC_CS_ID(vCSid);
             VDBean.setAC_AC_CSI_VECTOR(vACCSI);
             // get ref docs alt names for this vd
             Vector vRef = doRefDocSearch(VDBean.getVD_VD_IDSEQ(), "ALL TYPES", "open");
             VDBean.setAC_REF_DOCS(vRef);
             Vector vAlt = doAltNameSearch(VDBean.getVD_VD_IDSEQ(), "", "", "EditDesDE", "open");
             VDBean.setAC_ALT_NAMES(vAlt);
         }
         // make some attributes default/empty for NUE action
         if (sMenu.equals("NewVDTemplate"))
         {
             VDBean.setVD_VERSION("1.0");
             VDBean.setVD_ASL_NAME("");
         }
         // make the workflow status to empty if
         if (sMenu.equals("NewVDVersion"))
         {
             VDBean.setVD_ASL_NAME("");
             // Must preload the Alt Name/Def buffer on a new Version.
             AltNamesDefsSession.loadAsNew(m_servlet, session, VDBean);
         }
         // get all the selected ac id and name
         Vector vACid = (Vector) m_classReq.getAttribute("vACId");
         Vector vACName = (Vector) m_classReq.getAttribute("vACName");
         if (vACid == null)
             vACid = new Vector();
         if (vACName == null)
             vACName = new Vector();
         vACid.addElement(VDBean.getVD_VD_IDSEQ());
         String acName = "";
         if (VDBean.getVD_LONG_NAME() != null && !VDBean.getVD_LONG_NAME().equals(""))
             acName = VDBean.getVD_LONG_NAME();
         else
             acName = VDBean.getVD_PREFERRED_NAME();
         vACName.addElement(acName);
         boolean isSystemName = false;
         String vdName = VDBean.getVD_PREFERRED_NAME();
         // store preferred name type attributes
         VDBean.setAC_USER_PREF_NAME(vdName);
         VDBean.setAC_PREF_NAME_TYPE("");
         String rep_condr_idseq = VDBean.getVD_REP_CONDR_IDSEQ();
         if (!sAction.equalsIgnoreCase("BlockEdit") && rep_condr_idseq != null)
         {
             DataManager.setAttribute(session, "vRepTerm", null);
             evs.fillRepVectors(rep_condr_idseq, VDBean, sMenu);
             // make the abbreviated name if existing one is system name
             EVS_Bean nullEVS = null; 
             VDBean = (VD_Bean) m_servlet.getACNames(nullEVS, "OpenVD", VDBean);
         }
         // get contexts selected so far
         Vector selContext = (Vector) m_classReq.getAttribute("SelectedContext");
         if (selContext == null)
             selContext = new Vector();
         String sContID = VDBean.getVD_CONTE_IDSEQ();
         if (!selContext.contains(sContID))
         {
             selContext.addElement(sContID);
             m_classReq.setAttribute("SelectedContext", selContext);
             VDBean.setAC_SELECTED_CONTEXT_ID(selContext);
         }
         // get the cd name with the context
         acName = getCDContext(VDBean.getVD_CD_IDSEQ());
         if (acName != null && !acName.equals(""))
             VDBean.setVD_CD_NAME(acName);
         // get pv and parent atttirbutes if not block edit
         PVServlet pvser = new PVServlet(m_classReq, m_classRes, m_servlet);
         if (!sAction.equalsIgnoreCase("BlockEdit"))
             pvser.getPVAttributes(VDBean, sMenu);
         if (!sMenu.equals("NewVDTemplate") && !sAction.equals("Template"))
             VDBean.setAC_CONTACTS(getAC_Contacts(VDBean.getVD_VD_IDSEQ(), ""));
         // store ac names and ids in the req attribute
         m_classReq.setAttribute("vACId", vACid);
         m_classReq.setAttribute("vACName", vACName);
         return VDBean;
     }
 
     /**
      * gets the non evs parent from reference documents table for the vd
      * 
      * @return Vector
      * @throws java.lang.Exception
      */
     public String getMetaParentSource(String sParent, String sCui, VD_Bean PageVD) throws Exception
     {
         // search the existing reference document
         HttpSession session = m_classReq.getSession();
         String sRDMetaCUI = "";
         String sUMLS_CUI = "";
         if (PageVD != null)
         {
             Vector vRef = doRefDocSearch(PageVD.getVD_VD_IDSEQ(), "META_CONCEPT_SOURCE", "open");
             Vector vList = (Vector) m_classReq.getAttribute("RefDocList");
             if (vList != null && vList.size() > 0)
             {
                 for (int i = 0; i < vList.size(); i++)
                 {
                     REF_DOC_Bean RDBean = (REF_DOC_Bean) vList.elementAt(i);
                     // copy rd attributes to evs attribute
                     if (RDBean != null && RDBean.getDOCUMENT_NAME() != null && !RDBean.getDOCUMENT_NAME().equals(""))
                     {
                         sRDMetaCUI = RDBean.getDOCUMENT_TEXT();
                         if (sRDMetaCUI.equals(sCui))
                             return RDBean.getDOCUMENT_NAME();
                         else if (sCui.equals("None"))
                             return RDBean.getDOCUMENT_NAME();
                     }
                 }
             }
         }
         return "";
     }
 
     /**
      * gets the non evs parent from reference documents table for the vd
      * 
      * @param vParent
      *            vector of selected parents
      * @param vd
      *            current VD_Bean
      * @param menuAct
      *            string menu action
      * 
      * @return Vector
      * @throws java.lang.Exception
      */
     public Vector<EVS_Bean> getNonEVSParent(Vector<EVS_Bean> vParent, VD_Bean vd, String menuAct) throws Exception
     {
         // search the existing reference document
         Vector vRef = doRefDocSearch(vd.getVD_VD_IDSEQ(), "VD REFERENCE", "open");
         Vector vList = (Vector) m_classReq.getAttribute("RefDocList");
         if (vList != null && vList.size() > 0)
         {
             for (int i = 0; i < vList.size(); i++)
             {
                 REF_DOC_Bean RDBean = (REF_DOC_Bean) vList.elementAt(i);
                 // copy rd attributes to evs attribute
                 if (RDBean != null && RDBean.getDOCUMENT_NAME() != null && !RDBean.getDOCUMENT_NAME().equals(""))
                 {
                     // since already created by api reset it to do upate action for new version
                     if (menuAct != null && menuAct.equals("versionSubmit"))
                     {
                         for (int j = 0; j < vParent.size(); j++)
                         {
                             EVS_Bean nBean = (EVS_Bean) vParent.elementAt(j);
                             if (nBean.getEVS_DATABASE() != null && nBean.getEVS_DATABASE().equals("Non_EVS"))
                             {
                                 // reset rd id seq
                                 if (RDBean.getDOC_TYPE_NAME().equals(nBean.getCONCEPT_IDENTIFIER())
                                                 && RDBean.getDOCUMENT_NAME().equals(nBean.getLONG_NAME()))
                                 {
                                     nBean.setIDSEQ(RDBean.getREF_DOC_IDSEQ());
                                     vParent.setElementAt(nBean, j);
                                 }
                             }
                         }
                     }
                     else
                     {
                         EVS_Bean eBean = new EVS_Bean();
                         eBean.setIDSEQ(RDBean.getREF_DOC_IDSEQ());
                         eBean.setCONCEPT_IDENTIFIER(RDBean.getDOC_TYPE_NAME());
                         eBean.setLONG_NAME(RDBean.getDOCUMENT_NAME());
                         eBean.setPREFERRED_DEFINITION(RDBean.getDOCUMENT_TEXT());
                         eBean.setEVS_DEF_SOURCE(RDBean.getDOCUMENT_URL());
                         eBean.setCONTE_IDSEQ(RDBean.getCONTE_IDSEQ());
                         eBean.setEVS_DATABASE("Non_EVS");
                         eBean.setCON_AC_SUBMIT_ACTION("UPD");
                         // new one to be created
                         if (menuAct == null || menuAct.equals("NewVDTemplate"))
                         {
                             eBean.setCON_AC_SUBMIT_ACTION("INS");
                             eBean.setIDSEQ("");
                         }
                         vParent.addElement(eBean);
                     }
                 }
             }
         }
         return vParent;
     }
 
     /**
      * resets attributes for block edit
      * 
      * @param sSearchAC
      *            string of the search ac name
      */
     private void getSelRowForBlockEdit(String sSearchAC) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         Vector vCS = (Vector) m_classReq.getAttribute("selCSNames");
         Vector vCSid = (Vector) m_classReq.getAttribute("selCSIDs");
         Vector vACCSI = (Vector) m_classReq.getAttribute("blockAC_CSI");
         // get altname and ref docs from all acs stored in teh session
         Vector vAltNames = (Vector) session.getAttribute("AllAltNameList");
         if (vAltNames == null)
             vAltNames = new Vector();
         Vector vRefDocs = (Vector) session.getAttribute("AllRefDocList");
         if (vRefDocs == null)
             vRefDocs = new Vector();
         // store teh session attributes in the bean according to the ac
         if (sSearchAC.equals("DataElementConcept"))
         {
             DEC_Bean bDECBean = new DEC_Bean();
             bDECBean.setAC_CS_NAME(vCS);
             bDECBean.setAC_CS_ID(vCSid);
             bDECBean.setAC_AC_CSI_VECTOR(vACCSI);
             // add selected contexts to the bean
             Vector selContext = (Vector) m_classReq.getAttribute("SelectedContext");
             bDECBean.setAC_SELECTED_CONTEXT_ID(selContext);
             // add altname and ref docs to this bean
             bDECBean.setAC_ALT_NAMES(vAltNames);
             bDECBean.setAC_REF_DOCS(vRefDocs);
             // store de bean inthe session
             DataManager.setAttribute(session, "m_DEC", bDECBean);
             DEC_Bean clBean = new DEC_Bean();
             DataManager.setAttribute(session, "oldDECBean", clBean.cloneDEC_Bean(bDECBean));
         }
         else if (sSearchAC.equals("ValueDomain"))
         {
             VD_Bean bVDBean = new VD_Bean();
             bVDBean.setAC_CS_NAME(vCS);
             bVDBean.setAC_CS_ID(vCSid);
             bVDBean.setAC_AC_CSI_VECTOR(vACCSI);
             // add selected contexts to the bean
             Vector selContext = (Vector) m_classReq.getAttribute("SelectedContext");
             bVDBean.setAC_SELECTED_CONTEXT_ID(selContext);
             // add altname and ref docs to this bean
             bVDBean.setAC_ALT_NAMES(vAltNames);
             bVDBean.setAC_REF_DOCS(vRefDocs);
             // store de bean inthe session
             DataManager.setAttribute(session, "m_VD", bVDBean);
             VD_Bean clBean = new VD_Bean();
             DataManager.setAttribute(session, "oldVDBean", clBean.cloneVD_Bean(bVDBean));
         }
         else if (sSearchAC.equals("DataElement"))
         {
             DE_Bean bDEBean = new DE_Bean();
             bDEBean.setAC_CS_NAME(vCS);
             bDEBean.setAC_CS_ID(vCSid);
             bDEBean.setAC_AC_CSI_VECTOR(vACCSI);
             // add selected contexts to the bean
             Vector selContext = (Vector) m_classReq.getAttribute("SelectedContext");
             bDEBean.setAC_SELECTED_CONTEXT_ID(selContext);
             // add altname and ref docs to this bean
             bDEBean.setAC_ALT_NAMES(vAltNames);
             bDEBean.setAC_REF_DOCS(vRefDocs);
             // store de bean inthe session
             DataManager.setAttribute(session, "m_DE", bDEBean);
             DE_Bean clBean = new DE_Bean();
             DataManager.setAttribute(session, "oldDEBean", clBean.cloneDE_Bean(bDEBean, "Complete"));
         }
     }
 
     /**
      * To store the old attributes of the selected row in the session. called from selRowToEdit method after gettting
      * the selected row. Extract the attributes from the bean stores it in the session.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param DEBean
      *            Data Element Bean.
      * @param DECBean
      *            Data Element Concept Bean.
      * @param VDBean
      *            Value Domain Bean.
      * 
      * @throws Exception
      */
     private void storeOldAttInSession(HttpServletRequest req, HttpServletResponse res, DE_Bean DEBean,
                     DEC_Bean DECBean, VD_Bean VDBean) throws Exception
     {
         HttpSession session = req.getSession();
         String ACIDseq = "";
         String conName = "";
         String prefName = "";
         String contextID = "";
         String aslName = "";
         if (DEBean != null)
         {
             ACIDseq = DEBean.getDE_DE_IDSEQ();
             conName = DEBean.getDE_CONTEXT_NAME();
             prefName = DEBean.getDE_PREFERRED_NAME();
             contextID = DEBean.getDE_CONTE_IDSEQ();
             aslName = DEBean.getDE_ASL_NAME();
             DataManager.setAttribute(session, "OldDEContextID", contextID);
             DataManager.setAttribute(session, "OldDEContext", conName);
             DataManager.setAttribute(session, "OldDEPreferredName", prefName);
             DataManager.setAttribute(session, "OldDEAslName", aslName);
         }
         else if (DECBean != null)
         {
             ACIDseq = DECBean.getDEC_DEC_IDSEQ();
             conName = DECBean.getDEC_CONTEXT_NAME();
             prefName = DECBean.getDEC_PREFERRED_NAME();
             contextID = DECBean.getDEC_CONTE_IDSEQ();
             aslName = DECBean.getDEC_ASL_NAME();
             DataManager.setAttribute(session, "OldDECContextID", contextID);
             DataManager.setAttribute(session, "OldDECContext", conName);
             DataManager.setAttribute(session, "OldDECPreferredName", prefName);
             DataManager.setAttribute(session, "OldDECAslName", aslName);
         }
         else if (VDBean != null)
         {
             ACIDseq = VDBean.getVD_VD_IDSEQ();
             conName = VDBean.getVD_CONTEXT_NAME();
             prefName = VDBean.getVD_PREFERRED_NAME();
             contextID = VDBean.getVD_CONTE_IDSEQ();
             aslName = VDBean.getVD_ASL_NAME();
             DataManager.setAttribute(session, "OldVDContextID", contextID);
             DataManager.setAttribute(session, "OldVDContext", conName);
             DataManager.setAttribute(session, "OldVDPreferredName", prefName);
             DataManager.setAttribute(session, "OldVDAslName", aslName);
         }
     }
 
     /**
      * To check write permission for the user for the selected context in the selected component, called from
      * getSelRowToEdit. calls 'getAC.hasPrivilege' to get the permission value. returns error Message if no permit.
      * 
      * @param ACType
      *            selected component.
      * @param sUserName
      *            User Name.
      * @param ContID
      *            selected context idseq.
      * @param getAC
      *            reference to class GetACService.
      * 
      * @return String sErrorMessage if permission is No. empty string if Yes.
      */
     private String checkWritePermission(String ACType, String sUserName, String ContID, GetACService getAC)
     {
         String sErrorMessage = "";
         try
         {
             // validation code here
             String sPermit = "";
             sPermit = getAC.hasPrivilege("Create", sUserName, ACType, ContID);
             if (sPermit.equals("Yes"))
                 sErrorMessage = "";
             else if (sPermit.equals("No"))
                 sErrorMessage = sUserName + " does not have authorization";
             else
                 sErrorMessage = "Unable to determine the write permission.";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-checkWritePermission: " + e);
             logger.error("ERROR in GetACSearch-checkWritePermission : " + e.toString(), e);
         }
         return sErrorMessage;
     }
 
     /**
      * To open the Data Element page to create new or edit for the selected question, called from getSelRowToEdit.
      * Stores the DE_Bean for the selected question in the session
      * 
      * @param QuestBean
      *            Quest_Bean.
      * @param getAC
      *            reference to class GetACService.
      * 
      * @return boolean isValid if no write permission
      */
     private boolean doSelectDEforQuestion(HttpServletRequest req, HttpServletResponse res, Quest_Bean QuestBean,
                     GetACService getAC) throws Exception
     {
         HttpSession session = req.getSession();
         String strInValid = "";
         String sDEID = QuestBean.getDE_IDSEQ();
         boolean isValid = true;
         DE_Bean DEBean = new DE_Bean();
         // call search_DE to get all the attributes of DE with this de_id if exists
         if (sDEID != null && !sDEID.equals(""))
         {
             Vector vList = new Vector();
             doDESearch(sDEID, "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                             "", "", "", "", "", "", "", "", vList);
             if (vList != null && vList.size() > 0)
                 DEBean = (DE_Bean) vList.elementAt(0);
             // check permission, get cs and csi, check create or edit
             if (DEBean != null)
             {
                 // check if user is editing or creating new from existing
                 if ((QuestBean.getVD_IDSEQ() != null) && (!QuestBean.getVD_IDSEQ().equals("")))
                 {
                     // check if the de_vd is same as crf_vd
                     if (DEBean.getDE_VD_IDSEQ().equals(QuestBean.getVD_IDSEQ()))
                         QuestBean.setSTATUS_INDICATOR("Edit");
                     else
                         QuestBean.setSTATUS_INDICATOR("Template");
                 }
                 // allow to edit only if the user has the write permission to do so.
                 if (QuestBean.getSTATUS_INDICATOR().equals("Edit"))
                 {
                     String sUser = (String) session.getAttribute("Username");
                     String sContextID = DEBean.getDE_CONTE_IDSEQ();
                     strInValid = checkWritePermission("de", sUser, sContextID, getAC);
                     if (!strInValid.equals(""))
                     {
                         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                                         "User does not have authorization to Create/Edit for the selected context");
                         Vector vResult = new Vector();
                         getQuestionResult(req, res, vResult);
                         DataManager.setAttribute(session, "results", vResult);
                         isValid = false;
                         return isValid;
                     }
                     // get other de attributes for edit
                     DEBean = getDEAttributes(DEBean, "Edit", "Question");
                 }
             }
         }
         // add the quest attributes to the DE bean if not edit
         if (!QuestBean.getSTATUS_INDICATOR().equals("Edit"))
         {
             DEBean.setDE_CONTE_IDSEQ(QuestBean.getCONTE_IDSEQ());
             DEBean.setDE_CONTEXT_NAME(QuestBean.getCONTEXT_NAME());
             DEBean.setDOC_TEXT_PREFERRED_QUESTION(QuestBean.getQUEST_NAME());
             // add vd attribute if VD exists already for the question
             if ((QuestBean.getVD_IDSEQ() != null) && (!QuestBean.getVD_IDSEQ().equals("")))
             {
                 // add the crf_vd attributes to the DE bean
                 DEBean.setDE_VD_IDSEQ(QuestBean.getVD_IDSEQ());
                 DEBean.setDE_VD_NAME(QuestBean.getVD_LONG_NAME());
                 String sName = DEBean.getDE_DEC_NAME();
                 if (sName == null)
                     sName = "";
                 DEBean.setDE_LONG_NAME(sName + " " + QuestBean.getVD_LONG_NAME());
                 DEBean.setDE_VD_PREFERRED_NAME(QuestBean.getVD_PREF_NAME());
                 sName = DEBean.getDE_DEC_PREFERRED_NAME();
                 if (sName == null)
                     sName = "";
                 DEBean.setDE_PREFERRED_NAME(sName + "_" + QuestBean.getVD_PREF_NAME());
                 DEBean.setDE_VD_Definition(QuestBean.getVD_DEFINITION());
                 sName = DEBean.getDE_DEC_Definition();
                 if (sName == null)
                     sName = "";
                 DEBean.setDE_PREFERRED_DEFINITION(sName + "_" + QuestBean.getVD_DEFINITION());
             }
             // get other de attributes this de template
             if (DEBean.getDE_DE_IDSEQ() != null && !DEBean.getDE_DE_IDSEQ().equals(""))
             {
                 DEBean = getDEAttributes(DEBean, "Template", "Question");
                 DEBean.setDE_DE_IDSEQ("");
             }
             // get vd attributes since this question has VD but not DE
             else if (DEBean.getDE_VD_IDSEQ() != null && !DEBean.getDE_VD_IDSEQ().equals(""))
             {
                 String sVDid = DEBean.getDE_VD_IDSEQ();
                 Vector vVDList = new Vector();
                 doVDSearch(sVDid, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "",
                                 "", "", "", "", vVDList);
                 if (vVDList != null && vVDList.size() > 0)
                     DEBean.setDE_VD_Bean((VD_Bean) vVDList.elementAt(0));
                 // get system and abbr name to use it later
                 DEBean = (DE_Bean) m_servlet.getACNames("noChange", "OpenDE", DEBean);
                 DEBean.setAC_USER_PREF_NAME(DEBean.getDE_PREFERRED_NAME());
                 DEBean.setDE_ASL_NAME("DRAFT NEW");
             }
             else
             {
                 String DEDefinition = m_servlet.getPropertyDefinition();
                 DEBean.setDE_PREFERRED_DEFINITION(DEDefinition);
                 DEBean.setDE_ASL_NAME("DRAFT NEW");
                 DEBean.setAC_PREF_NAME_TYPE("SYS");
             }
         }
         // store this in the session bean to use it later
         DataManager.setAttribute(session, "m_Quest", QuestBean);
         DataManager.setAttribute(session, "sDefaultContext", QuestBean.getCONTEXT_NAME());
         DataManager.setAttribute(session, "m_DE", DEBean);
         DE_Bean oldDE = new DE_Bean();
         DataManager.setAttribute(session, "oldDEBean", oldDE.cloneDE_Bean(DEBean, "Complete"));
         return isValid;
     }
 
     /**
      * To get search result from database for Classification Scheme/Items Component called from getACListForEdit method.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_DE_CS_NAME(DE_IDSEQ, OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to CS and CSI vectors. Update the DEBean with CS, CSIDseq, CSI and
      * CSIIDSeq vectors.
      * 
      * 
      */
     public Vector<AC_CSI_Bean> doCSCSI_ACSearch(String AC_IDseq, String AC_Name)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         HttpSession session = m_classReq.getSession();
         Vector vAC_CSI = new Vector();
         try
         {
             // String AC_IDSEQ = DECBean.getDEC_DEC_IDSEQ();
             Vector vCSNames = (Vector) m_classReq.getAttribute("selCSNames");
             Vector vCSids = (Vector) m_classReq.getAttribute("selCSIDs");
             vAC_CSI = (Vector) m_classReq.getAttribute("blockAC_CSI");
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_DE_CS_NAME(?,?)}");
                 // Now tie the placeholders with actual parameters.
                 cstmt.registerOutParameter(2, OracleTypes.CURSOR);
                 cstmt.setString(1, AC_IDseq);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(2);
                 if (rs != null)
                 {
                     // loop through to printout the outstrings
                     while (rs.next())
                     {
                         AC_CSI_Bean acCSIBean = new AC_CSI_Bean();
                         acCSIBean.setAC_CSI_IDSEQ(rs.getString("ac_csi_idseq"));
                         // csi related
                         // store it in the csi bean and store the bean in ac_csi bean
                         CSI_Bean csiBean = new CSI_Bean();
                         csiBean.setCSI_CS_IDSEQ(rs.getString("CS_IDSEQ"));
                         String aName = rs.getString("long_name");
                         aName = m_util.removeNewLineChar(aName);
                         csiBean.setCSI_CS_LONG_NAME(aName);
                         csiBean.setCSI_CSI_IDSEQ(rs.getString("csi_idseq"));
                         aName = rs.getString("CSI_NAME");
                         aName = m_util.removeNewLineChar(aName);
                         csiBean.setCSI_NAME(aName);
                         csiBean.setCSI_CSCSI_IDSEQ(rs.getString("cs_csi_idseq"));
                         csiBean.setP_CSCSI_IDSEQ(rs.getString("p_cs_csi_idseq"));
                         aName = rs.getString("label");
                         aName = m_util.removeNewLineChar(aName);
                         csiBean.setCSI_LABEL(aName);
                         csiBean.setCSI_DISPLAY_ORDER(rs.getString("display_order"));
                         csiBean.setCSI_LEVEL(rs.getString("csi_level"));
                         acCSIBean.setCSI_BEAN(csiBean);
                         // get ac attributes.
                         acCSIBean.setAC_IDSEQ(AC_IDseq);
                         acCSIBean.setAC_LONG_NAME(AC_Name);
                         acCSIBean.setAC_TYPE_NAME("DE_CONCEPT");
                         if (vCSNames == null)
                             vCSNames = new Vector();
                         if (vCSids == null)
                             vCSids = new Vector();
                         if (vCSids == null || vCSids.isEmpty() || (!vCSids.contains(csiBean.getCSI_CS_IDSEQ())))
                         {
                             vCSNames.addElement(csiBean.getCSI_CS_LONG_NAME());
                             vCSids.addElement(csiBean.getCSI_CS_IDSEQ());
                         }
                         if (vAC_CSI == null)
                             vAC_CSI = new Vector();
                         vAC_CSI.addElement(acCSIBean);
                     }
                 }
             }
             m_classReq.setAttribute("selCSNames", vCSNames);
             m_classReq.setAttribute("selCSIDs", vCSids);
             m_classReq.setAttribute("blockAC_CSI", vAC_CSI);
             // store this to keep track of the old ones if removing the existing relationship
             DataManager.setAttribute(session, "vAC_CSI", vAC_CSI);
         }
         catch (Exception e)
         {
            logger.error("ERROR in GetACSearch-doCSCSI_ACSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return vAC_CSI;
     }
 
     /**
      * To get the sorted vector for the selected field in the DE component, called from getACSortedResult. gets the
      * 'sortType' from request and 'vSelRows' vector from session. calls 'getDEFieldValue' to extract the value from the
      * bean for the selected sortType. modified bubble sort method to sort beans in the vector using the strings
      * compareto method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getDESortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null && !sortField.equalsIgnoreCase("referenceDoc"))
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             DE_Bean DEBean = (DE_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 DEBean.setDE_CHECKED(true);
                             else
                                 DEBean.setDE_CHECKED(false);
                         }
                     }
                     // loop through the vector to get the bean row
                     OuterLoop: for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         DE_Bean DESortBean1 = (DE_Bean) vSRows.elementAt(i);
                         String Name1 = getDEFieldValue(DESortBean1, sortField);
                         int tempInd = i;
                         DE_Bean tempBean = DESortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             DE_Bean DESortBean2 = (DE_Bean) vSRows.elementAt(j);
                             String Name2 = getDEFieldValue(DESortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = DESortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = DESortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(DESortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             DE_Bean DEBean = (DE_Bean) vSortedRows.elementAt(i);
                             if (DEBean.getDE_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-DEsortedRows: " + e);
             logger.error("ERROR in GetACSearch-DEsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * can be used to sort properly.
      */
     private void getDESortedRowsBubbleSort(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             DE_Bean DEBean = (DE_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 DEBean.setDE_CHECKED(true);
                             else
                                 DEBean.setDE_CHECKED(false);
                         }
                     }
                     // loop through the vector to get the bean row
                     OuterLoop: for (int i = 0; i < (vSRows.size()); i++)
                     {
                         // sort from the last item in the loop so that the ascending of nlogn can be done.
                         for (int j = vSRows.size() - 1; j >= i; j--)
                         {
                             DE_Bean DESortBean1 = (DE_Bean) vSRows.elementAt(j);
                             String Name1 = getDEFieldValue(DESortBean1, sortField);
                             if (j - 1 >= i)
                             {
                                 DE_Bean DESortBean2 = (DE_Bean) vSRows.elementAt(j - 1);
                                 String Name2 = getDEFieldValue(DESortBean2, sortField);
                                 if (Name1 != "" && !Name1.equals(Name2) && (ComparedValue(sortField, Name2, Name1) > 0))
                                 {
                                     DE_Bean tempBean = DESortBean2;
                                     vSRows.setElementAt(DESortBean1, j - 1);
                                     vSRows.setElementAt(tempBean, j);
                                 }
                             }
                         }
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSRows.size()); i++)
                         {
                             DE_Bean DEBean = (DE_Bean) vSRows.elementAt(i);
                             if (DEBean.getDE_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-DEsortedRows: " + e);
             logger.error("ERROR in GetACSearch-DEsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the bean for the selected field, called from DEsortedRows.
      * 
      * @param curBean
      *            DE bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String DEField Value if selected field is found. otherwise empty string.
      */
     private String getDEFieldValue(DE_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("name"))
                 returnValue = curBean.getDE_PREFERRED_NAME();
             else if (curField.equals("longName"))
                 returnValue = curBean.getDE_LONG_NAME();
             else if (curField.equals("def"))
                 returnValue = curBean.getDE_PREFERRED_DEFINITION();
             else if (curField.equals("context"))
                 returnValue = curBean.getDE_CONTEXT_NAME();
             else if (curField.equals("UsedContext"))
                 returnValue = curBean.getDE_USEDBY_CONTEXT();
             else if (curField.equals("vd"))
                 returnValue = curBean.getDE_VD_NAME();
             else if (curField.equals("minID"))
                 returnValue = curBean.getDE_MIN_CDE_ID();
             else if (curField.equals("version"))
                 returnValue = curBean.getDE_VERSION();
             else if (curField.equals("Comments"))
                 returnValue = curBean.getDE_CHANGE_NOTE();
             else if (curField.equals("Origin"))
                 returnValue = curBean.getDE_SOURCE();
             else if (curField.equals("DEC"))
                 returnValue = curBean.getDE_DEC_NAME();
             else if (curField.equals("Status"))
                 returnValue = curBean.getDE_ASL_NAME();
             else if (curField.equals("BeginDate"))
                 returnValue = curBean.getDE_BEGIN_DATE();
             else if (curField.equals("EndDate"))
                 returnValue = curBean.getDE_END_DATE();
             else if (curField.equals("aliasName"))
                 returnValue = curBean.getDE_ALIAS_NAME();
             else if (curField.equals("ProtoID"))
                 returnValue = curBean.getDE_PROTOCOL_ID();
             else if (curField.equals("CRFName"))
                 returnValue = curBean.getDE_CRF_NAME();
             else if (curField.equals("TypeName"))
                 returnValue = curBean.getDE_TYPE_NAME();
             else if (curField.equals("regStatus"))
                 returnValue = curBean.getDE_REG_STATUS();
             else if (curField.equals("creDate"))
                 returnValue = curBean.getDE_DATE_CREATED();
             else if (curField.equals("creator"))
                 returnValue = curBean.getDE_CREATED_BY();
             else if (curField.equals("modDate"))
                 returnValue = curBean.getDE_DATE_MODIFIED();
             else if (curField.equals("modifier"))
                 returnValue = curBean.getDE_MODIFIED_BY();
             // else if (curField.equals("HistID"))
             // returnValue = curBean.getDE_HIST_CDE_ID();
             else if (curField.equals("permValue"))
                 returnValue = curBean.getDE_Permissible_Value();
             /*
              * else if (curField.equals("DocText")) returnValue = curBean.getDOC_TEXT_PREFERRED_QUESTION(); else if
              * (curField.equals("HistoricName")) returnValue = curBean.getDOC_TEXT_HISTORIC_NAME(); else if
              * (curField.equals("CommentDocText")) returnValue = curBean.getDOC_TEXT_COMMENT(); else if
              * (curField.equals("DESourceDocText")) returnValue = curBean.getDOC_TEXT_DATA_ELEMENT_SOURCE(); else if
              * (curField.equals("DescDocText")) returnValue = curBean.getDOC_TEXT_DESCRIPTION(); else if
              * (curField.equals("DetailDescDocText")) returnValue = curBean.getDOC_TEXT_DETAIL_DESCRIPTION(); else if
              * (curField.equals("ExampleDocText")) returnValue = curBean.getDOC_TEXT_EXAMPLE(); else if
              * (curField.equals("ImageFileDocText")) returnValue = curBean.getDOC_TEXT_IMAGE_FILE(); else if
              * (curField.equals("LabelDocText")) returnValue = curBean.getDOC_TEXT_LABEL(); else if
              * (curField.equals("NoteDocText")) returnValue = curBean.getDOC_TEXT_NOTE(); else if
              * (curField.equals("ReferenceDocText")) returnValue = curBean.getDOC_TEXT_REFERENCE(); else if
              * (curField.equals("TechGuideDocText")) returnValue = curBean.getDOC_TEXT_TECHNICAL_GUIDE(); else if
              * (curField.equals("UMLAttrDocText")) returnValue = curBean.getDOC_TEXT_UML_Attribute(); else if
              * (curField.equals("UMLClassDocText")) returnValue = curBean.getDOC_TEXT_UML_Class(); else if
              * (curField.equals("VVSourceDocText")) returnValue = curBean.getDOC_TEXT_VALID_VALUE_SOURCE();
              */
             else if (curField.equals("conName"))
                 returnValue = curBean.getAC_CONCEPT_NAME();
             else if (curField.equals("altNames"))
                 returnValue = curBean.getALTERNATE_NAME();
             else if (curField.equals("refDocs"))
                 returnValue = curBean.getREFERENCE_DOCUMENT();
             else if (curField.equals("DerRelation"))
                 returnValue = curBean.getDE_DER_RELATION();
             // else if (curField.equals("OtherTypesDocText"))
             // returnValue = curBean.getDOC_TEXT_OTHER_REF_TYPES();
             // else if (curField.equals("referenceDoc"))
             // returnValue = curBean.getDOC_TEXT_OTHER_REF_TYPES();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getDEField: " + e);
             logger.error("ERROR in GetACSearch-getDEField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the DEC component, called from getACSortedResult. gets the
      * 'sortType' from request and 'vSelRows' vector from session. calls 'getDEFieldValue' to extract the value from the
      * bean for the selected sortType. modified bubble sort method to sort beans in the vector using the strings
      * compareto method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getDECSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             DEC_Bean DECBean = (DEC_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 DECBean.setDEC_CHECKED(true);
                             else
                                 DECBean.setDEC_CHECKED(false);
                         }
                     }
                     // loop through the searched DE result to get the matched checked rows
                     OuterLoop: for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         DEC_Bean DECSortBean1 = (DEC_Bean) vSRows.elementAt(i);
                         String Name1 = getDECFieldValue(DECSortBean1, sortField);
                         int tempInd = i;
                         DEC_Bean tempBean = DECSortBean1;
                         String tempName = Name1;
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             DEC_Bean DECSortBean2 = (DEC_Bean) vSRows.elementAt(j);
                             String Name2 = getDECFieldValue(DECSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = DECSortBean2;
                                     tempInd = j;
                                 }
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = DECSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(DECSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             DEC_Bean DECBean = (DEC_Bean) vSortedRows.elementAt(i);
                             if (DECBean.getDEC_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-DECsortedRows: " + e);
             logger.error("ERROR in GetACSearch-DECsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the DECbean for the selected field, called from DECsortedRows.
      * 
      * @param curBean
      *            DEC bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String DECField Value if selected field is found. otherwise empty string.
      */
     private String getDECFieldValue(DEC_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("name"))
                 returnValue = curBean.getDEC_PREFERRED_NAME();
             else if (curField.equals("longName"))
                 returnValue = curBean.getDEC_LONG_NAME();
             else if (curField.equals("def"))
                 returnValue = curBean.getDEC_PREFERRED_DEFINITION();
             else if (curField.equals("context"))
                 returnValue = curBean.getDEC_CONTEXT_NAME();
             else if (curField.equals("UsedContext"))
                 returnValue = curBean.getDEC_USEDBY_CONTEXT();
             else if (curField.equals("version"))
                 returnValue = curBean.getDEC_VERSION();
             else if (curField.equals("Status"))
                 returnValue = curBean.getDEC_ASL_NAME();
             else if (curField.equals("BeginDate"))
                 returnValue = curBean.getDEC_BEGIN_DATE();
             else if (curField.equals("ConDomain"))
                 returnValue = curBean.getDEC_CD_NAME();
             else if (curField.equals("Comments"))
                 returnValue = curBean.getDEC_CHANGE_NOTE();
             else if (curField.equals("EndDate"))
                 returnValue = curBean.getDEC_END_DATE();
             else if (curField.equals("aliasName"))
                 returnValue = curBean.getDEC_ALIAS_NAME();
             else if (curField.equals("ProtoID"))
                 returnValue = curBean.getDEC_PROTOCOL_ID();
             else if (curField.equals("CRFName"))
                 returnValue = curBean.getDEC_CRF_NAME();
             else if (curField.equals("TypeName"))
                 returnValue = curBean.getDEC_TYPE_NAME();
             else if (curField.equals("minID"))
                 returnValue = curBean.getDEC_DEC_ID();
             else if (curField.equals("Origin"))
                 returnValue = curBean.getDEC_SOURCE();
             else if (curField.equals("creDate"))
                 returnValue = curBean.getDEC_DATE_CREATED();
             else if (curField.equals("creator"))
                 returnValue = curBean.getDEC_CREATED_BY();
             else if (curField.equals("modDate"))
                 returnValue = curBean.getDEC_DATE_MODIFIED();
             else if (curField.equals("modifier"))
                 returnValue = curBean.getDEC_MODIFIED_BY();
             else if (curField.equals("conName"))
                 returnValue = curBean.getAC_CONCEPT_NAME();
             else if (curField.equals("altNames"))
                 returnValue = curBean.getALTERNATE_NAME();
             else if (curField.equals("refDocs"))
                 returnValue = curBean.getREFERENCE_DOCUMENT();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getDECField: " + e);
             logger.error("ERROR in GetACSearch-getDECField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the VD component, called from getACSortedResult. gets the
      * 'sortType' from request and 'vSelRows' vector from session. calls 'getDEFieldValue' to extract the value from the
      * bean for the selected sortType. modified bubble sort method to sort beans in the vector using the strings
      * compareToIgnoreCase method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getVDSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             VD_Bean VDBean = (VD_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 VDBean.setVD_CHECKED(true);
                             else
                                 VDBean.setVD_CHECKED(false);
                         }
                     }
                     // loop through the searched DE result to get the matched checked rows
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         VD_Bean VDSortBean1 = (VD_Bean) vSRows.elementAt(i);
                         String Name1 = getVDFieldValue(VDSortBean1, sortField);
                         int tempInd = i;
                         VD_Bean tempBean = VDSortBean1;
                         String tempName = Name1;
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             VD_Bean VDSortBean2 = (VD_Bean) vSRows.elementAt(j);
                             String Name2 = getVDFieldValue(VDSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = VDSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = VDSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(VDSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             VD_Bean VDBean = (VD_Bean) vSortedRows.elementAt(i);
                             if (VDBean.getVD_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-VDSortedRows: " + e);
             logger.error("ERROR in GetACSearch-VDSortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the bean for the selected field, called from VDsortedRows.
      * 
      * @param curBean
      *            VD bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String VDField Value if selected field is found. otherwise empty string.
      */
     private String getVDFieldValue(VD_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("name"))
                 returnValue = curBean.getVD_PREFERRED_NAME();
             else if (curField.equals("longName"))
                 returnValue = curBean.getVD_LONG_NAME();
             else if (curField.equals("def"))
                 returnValue = curBean.getVD_PREFERRED_DEFINITION();
             else if (curField.equals("context"))
                 returnValue = curBean.getVD_CONTEXT_NAME();
             else if (curField.equals("UsedContext"))
                 returnValue = curBean.getVD_USEDBY_CONTEXT();
             else if (curField.equals("version"))
                 returnValue = curBean.getVD_VERSION();
             else if (curField.equals("Status"))
                 returnValue = curBean.getVD_ASL_NAME();
             else if (curField.equals("BeginDate"))
                 returnValue = curBean.getVD_BEGIN_DATE();
             else if (curField.equals("ConDomain"))
                 returnValue = curBean.getVD_CD_NAME();
             else if (curField.equals("EndDate"))
                 returnValue = curBean.getVD_END_DATE();
             else if (curField.equals("HighNum"))
                 returnValue = curBean.getVD_HIGH_VALUE_NUM();
             else if (curField.equals("LowNum"))
                 returnValue = curBean.getVD_LOW_VALUE_NUM();
             else if (curField.equals("Decimal"))
                 returnValue = curBean.getVD_DECIMAL_PLACE();
             else if (curField.equals("MaxLength"))
                 returnValue = curBean.getVD_MAX_LENGTH_NUM();
             else if (curField.equals("MinLength"))
                 returnValue = curBean.getVD_MIN_LENGTH_NUM();
             else if (curField.equals("DataType"))
                 returnValue = curBean.getVD_DATA_TYPE();
             else if (curField.equals("UOML"))
                 returnValue = curBean.getVD_UOML_NAME();
             else if (curField.equals("Format"))
                 returnValue = curBean.getVD_FORML_NAME();
             else if (curField.equals("Comments"))
                 returnValue = curBean.getVD_CHANGE_NOTE();
             else if (curField.equals("TypeFlag"))
                 returnValue = curBean.getVD_TYPE_FLAG();
             else if (curField.equals("aliasName"))
                 returnValue = curBean.getVD_ALIAS_NAME();
             else if (curField.equals("ProtoID"))
                 returnValue = curBean.getVD_PROTOCOL_ID();
             else if (curField.equals("CRFName"))
                 returnValue = curBean.getVD_CRF_NAME();
             else if (curField.equals("TypeName"))
                 returnValue = curBean.getVD_TYPE_NAME();
             else if (curField.equals("minID"))
                 returnValue = curBean.getVD_VD_ID();
             else if (curField.equals("Origin"))
                 returnValue = curBean.getVD_SOURCE();
             else if (curField.equals("creDate"))
                 returnValue = curBean.getVD_DATE_CREATED();
             else if (curField.equals("creator"))
                 returnValue = curBean.getVD_CREATED_BY();
             else if (curField.equals("modDate"))
                 returnValue = curBean.getVD_DATE_MODIFIED();
             else if (curField.equals("modifier"))
                 returnValue = curBean.getVD_MODIFIED_BY();
             else if (curField.equals("permValue"))
                 returnValue = curBean.getVD_Permissible_Value();
             else if (curField.equals("conName"))
                 returnValue = curBean.getAC_CONCEPT_NAME();
             else if (curField.equals("altNames"))
                 returnValue = curBean.getALTERNATE_NAME();
             else if (curField.equals("refDocs"))
                 returnValue = curBean.getREFERENCE_DOCUMENT();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getVDField: " + e);
             logger.error("ERROR in GetACSearch-getVDField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the CD component, called from getACSortedResult. gets the
      * 'sortType' from request and 'vSelRows' vector from session. calls 'getCDFieldValue' to extract the value from the
      * bean for the selected sortType. modified bubble sort method to sort beans in the vector using the strings
      * compareto method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getCDSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             CD_Bean CDBean = (CD_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 CDBean.setCD_CHECKED(true);
                             else
                                 CDBean.setCD_CHECKED(false);
                         }
                     }
                     // loop through the searched DE result to get the matched checked rows
                     OuterLoop: for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         CD_Bean CDSortBean1 = (CD_Bean) vSRows.elementAt(i);
                         String Name1 = getCDFieldValue(CDSortBean1, sortField);
                         int tempInd = i;
                         CD_Bean tempBean = CDSortBean1;
                         String tempName = Name1;
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             CD_Bean CDSortBean2 = (CD_Bean) vSRows.elementAt(j);
                             String Name2 = getCDFieldValue(CDSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = CDSortBean2;
                                     tempInd = j;
                                 }
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = CDSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(CDSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     for (int i = 0; i < (vSortedRows.size()); i++)
                     {
                         CD_Bean CDSortBean1 = (CD_Bean) vSortedRows.elementAt(i);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             CD_Bean CDBean = (CD_Bean) vSortedRows.elementAt(i);
                             if (CDBean.getCD_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-CDsortedRows: " + e);
             logger.error("ERROR in GetACSearch-CDsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the CSIbean for the selected field, called from CSIsortedRows.
      * 
      * @param curBean
      *            CSI bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String CDField Value if selected field is found. otherwise empty string.
      */
     private String getCDFieldValue(CD_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("name"))
                 returnValue = curBean.getCD_PREFERRED_NAME();
             else if (curField.equals("longName"))
                 returnValue = curBean.getCD_LONG_NAME();
             else if (curField.equals("def"))
                 returnValue = curBean.getCD_PREFERRED_DEFINITION();
             else if (curField.equals("context"))
                 returnValue = curBean.getCD_CONTEXT_NAME();
             else if (curField.equals("version"))
                 returnValue = curBean.getCD_VERSION();
             else if (curField.equals("Status"))
                 returnValue = curBean.getCD_ASL_NAME();
             else if (curField.equals("BeginDate"))
                 returnValue = curBean.getCD_BEGIN_DATE();
             else if (curField.equals("Dimension"))
                 returnValue = curBean.getCD_DIMENSIONALITY();
             else if (curField.equals("Comments"))
                 returnValue = curBean.getCD_CHANGE_NOTE();
             else if (curField.equals("EndDate"))
                 returnValue = curBean.getCD_END_DATE();
             else if (curField.equals("minID"))
                 returnValue = curBean.getCD_CD_ID();
             else if (curField.equals("Origin"))
                 returnValue = curBean.getCD_SOURCE();
             else if (curField.equals("creDate"))
                 returnValue = curBean.getCD_DATE_CREATED();
             else if (curField.equals("creator"))
                 returnValue = curBean.getCD_CREATED_BY();
             else if (curField.equals("modDate"))
                 returnValue = curBean.getCD_DATE_MODIFIED();
             else if (curField.equals("modifier"))
                 returnValue = curBean.getCD_MODIFIED_BY();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getCDField: " + e);
             logger.error("ERROR in GetACSearch-getCDField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get final result vector of selected attributes/rows to display for Data Element Concept component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the CSIBean vector 'vACSearch' and adds the selected fields to result
      * vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * 
      */
     private void getCSIResult(HttpServletRequest req, HttpServletResponse res, Vector vResult, String refresh)
     {
         Vector vCSI = new Vector();
         try
         {
             HttpSession session = req.getSession();
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             Vector vSelAttr = new Vector();
             if (menuAction.equals("searchForCreate"))
                 vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vSelAttr = (Vector) session.getAttribute("selectedAttr");
             // all search results
             vCSI = (Vector) session.getAttribute("vACSearch");
             Vector vRSel = (Vector) session.getAttribute("vSelRows");
             if (menuAction.equals("searchForCreate"))
                 vRSel = null;
             if (vRSel == null)
                 vRSel = vCSI;
             // convert int to string to get the rec count
             if (vRSel == null)
                 vRSel = new Vector();
             Integer recs = new Integer(0);
             if (vRSel.size() > 0)
                 recs = new Integer(vRSel.size());
             String recs2 = "";
             if (recs != null)
                 recs2 = recs.toString();
             String sKeyword = "";
             if (menuAction.equals("searchForCreate"))
             {
             	session.setAttribute("creRecsFound", recs2);
                 sKeyword = (String) session.getAttribute("creKeyword");
             }
             else
             {
             	session.setAttribute("recsFound", recs2);
                 sKeyword = (String) session.getAttribute("serKeyword");
             }
             // make keyWordLabel label request session
             if (sKeyword.equals("") && !menuAction.equals("searchForCreate"))
                 sKeyword = (String) session.getAttribute("LastAppendWord");
             if (sKeyword == null)
                 sKeyword = "";
             session.setAttribute("labelKeyword", "Class Scheme Items - " + sKeyword);
             Vector vSearchID = new Vector();
             Vector vSearchName = new Vector();
             Vector vSearchLongName = new Vector();
             Vector vSearchASL = new Vector();
             Vector vSearchDefinition = new Vector();
             Vector vUsedContext = new Vector();
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 CSI_Bean CSIBean = new CSI_Bean();
                 CSIBean = (CSI_Bean) vRSel.elementAt(i);
                 vSearchID.addElement(CSIBean.getCSI_CSCSI_IDSEQ());
                 vSearchName.addElement(CSIBean.getCSI_NAME());
                 vSearchLongName.addElement(CSIBean.getCSI_CS_LONG_NAME());
                 vSearchDefinition.addElement(CSIBean.getCSI_DEFINITION());
                 vUsedContext.addElement(CSIBean.getCSI_CONTEXT_NAME());
                 if (vSelAttr.contains("CSI Name"))
                     vResult.addElement(CSIBean.getCSI_NAME());
                 if (vSelAttr.contains("CSI Type"))
                     vResult.addElement(CSIBean.getCSI_CSITL_NAME());
                 if (vSelAttr.contains("CSI Definition"))
                     vResult.addElement(CSIBean.getCSI_DEFINITION());
                 if (vSelAttr.contains("CS Long Name"))
                     vResult.addElement(CSIBean.getCSI_CS_LONG_NAME());
                 if (vSelAttr.contains("CS Public ID"))
                     vResult.addElement(CSIBean.getCSI_CS_PUBLICID());
                 if (vSelAttr.contains("CS Version"))
                     vResult.addElement(CSIBean.getCSI_CS_VERSION());
                 // if (vSelAttr.contains("Concept Name"))
                 // vResult = addMultiRecordConcept("", CSIBean.getCSI_NAME(), CSIBean.getCSI_NAME(),
                 // "ClassSchemeItem", vResult);
                 if (vSelAttr.contains("Context"))
                     vResult.addElement(CSIBean.getCSI_CONTEXT_NAME());
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
             DataManager.setAttribute(session, "SearchLongName", vSearchLongName);
             DataManager.setAttribute(session, "SearchDefinitionAC", vSearchDefinition);
             DataManager.setAttribute(session, "SearchUsedContext", vUsedContext);
             // for Back button, put search results and attributes on a stack
             if (!refresh.equals("true") && (!menuAction.equals("searchForCreate")))
                 stackSearchComponents("ClassSchemeItems", vCSI, vRSel, vSearchID, vSearchName, vResult, vSearchASL,
                                 vSearchLongName);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getCSIResult: " + e);
             logger.error("ERROR in GetACSearch-getCSIResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the CSIbean for the selected field, called from CSIsortedRows.
      * 
      * @param curBean
      *            CSI bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String CSIField Value if selected field is found. otherwise empty string.
      */
     private String getCSIFieldValue(CSI_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("CSIName"))
                 returnValue = curBean.getCSI_NAME();
             else if (curField.equals("CSName"))
                 returnValue = curBean.getCSI_CS_LONG_NAME();
             else if (curField.equals("def"))
                 returnValue = curBean.getCSI_DEFINITION();
             else if (curField.equals("context"))
                 returnValue = curBean.getCSI_CONTEXT_NAME();
             else if (curField.equals("CSITL"))
                 returnValue = curBean.getCSI_CSITL_NAME();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getCSIField: " + e);
             logger.error("ERROR in GetACSearch-getCSIField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the CSI component, called from getACSortedResult. gets the
      * 'sortType' from request and 'vSelRows' vector from session. calls 'getCSIFieldValue' to extract the value from
      * the bean for the selected sortType. modified bubble sort method to sort beans in the vector using the strings
      * compareto method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getCSISortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             CSI_Bean CSIBean = (CSI_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 CSIBean.setCSI_CHECKED(true);
                             else
                                 CSIBean.setCSI_CHECKED(false);
                         }
                     }
                     // loop through the searched DE result to get the matched checked rows
                     OuterLoop: for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         CSI_Bean CSISortBean1 = (CSI_Bean) vSRows.elementAt(i);
                         String Name1 = getCSIFieldValue(CSISortBean1, sortField);
                         int tempInd = i;
                         CSI_Bean tempBean = CSISortBean1;
                         String tempName = Name1;
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             CSI_Bean CSISortBean2 = (CSI_Bean) vSRows.elementAt(j);
                             String Name2 = getCSIFieldValue(CSISortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = CSISortBean2;
                                     tempInd = j;
                                 }
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = CSISortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(CSISortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     for (int i = 0; i < (vSortedRows.size()); i++)
                     {
                         CSI_Bean CSISortBean1 = (CSI_Bean) vSortedRows.elementAt(i);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             CSI_Bean CSIBean = (CSI_Bean) vSortedRows.elementAt(i);
                             if (CSIBean.getCSI_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
           logger.error("ERROR in GetACSearch-CSIsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get resultSet from database for Permissible Value Component called from getACKeywordResult and getCDEIDSearch
      * methods.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_PVVM(InString, OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to bean which is added to the vector to return
      * 
      * @param InString
      *            Keyword value, is empty if searchIn is minID.
      * @param vList
      *            returns Vector of PVbean.
      * 
      */
 /*    private void doPVVMSearch(String InString, String cd_idseq, String conName, String conID, Vector vList)
     {
         Connection conn = null;
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
             // Create a Callable Statement object.
             conn = m_servlet.connectDB(m_classReq, m_classRes);
             if (conn == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = conn.prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_PVVM(?,?,?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(3, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, InString);
                 cstmt.setString(2, cd_idseq);
                 cstmt.setString(4, conName);
                 cstmt.setString(5, conID);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(3);
                 String s;
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         PV_Bean PVBean = new PV_Bean();
                         PVBean.setPV_PV_IDSEQ(rs.getString("pv_idseq"));
                         PVBean.setPV_VALUE(rs.getString("value"));
                         PVBean.setPV_SHORT_MEANING(rs.getString("short_meaning"));
                         s = rs.getString("begin_date");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         PVBean.setPV_BEGIN_DATE(s);
                         s = rs.getString("end_date");
                         if (s != null)
                             s = m_util.getCurationDate(s);
                         PVBean.setPV_END_DATE(s);
                         PVBean.setPV_MEANING_DESCRIPTION(rs.getString("vm_description"));
                         // add the CD name and version in decimal number together
                         PVBean.setPV_CONCEPTUAL_DOMAIN(rs.getString("cd_name"));
                         // get vm concept attributes
                         String sCondr = rs.getString("condr_idseq");
                         VM_Bean vm = new VM_Bean();
                         vm.setVM_SHORT_MEANING(PVBean.getPV_SHORT_MEANING());
                         vm.setVM_DESCRIPTION(PVBean.getPV_MEANING_DESCRIPTION());
                         vm.setVM_CONDR_IDSEQ(sCondr);
                         EVS_Bean vmConcept = new EVS_Bean();
                         vmConcept.setCONDR_IDSEQ(sCondr);
                         if (sCondr != null && !sCondr.equals(""))
                         {
                             GetACService getAC = new GetACService(m_classReq, m_classRes, m_servlet);
                             Vector vConList = getAC.getAC_Concepts(sCondr, null, true);
                             if (vConList != null && vConList.size() > 0)
                             {
                                 vmConcept = (EVS_Bean) vConList.elementAt(0);
                                 vm.setVM_CONCEPT_LIST(vConList);
                             }
                         }
                         // PVBean.setVM_CONCEPT(vmConcept);
                         PVBean.setPV_VM(vm);
                         // get database attribute
                         PVBean.setPV_EVS_DATABASE("caDSR");
                         vList.addElement(PVBean);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("other problem in GetACSearch-searchPVVM: " + e);
             logger.error("ERROR - GetACSearch-searchPVVM for other : " + e.toString(), e);
         }
         try
         {
             if (rs != null)
                 rs.close();
             if (cstmt != null)
                 cstmt.close();
             if (conn != null)
                 conn.close();
         }
         catch (Exception ee)
         {
             logger.error("ERROR - GetACSearch-searchPVVM for close : " + ee.toString(), ee);
         }
     }
 */
     /**
      * To get final result vector of selected attributes/rows to display for Permissible Values component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the PVBean vector 'vACSearch' and adds the selected fields to result vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * 
      */
     public void getPVVMResult(HttpServletRequest req, HttpServletResponse res, Vector vResult, String refresh)
     {
         Vector vPVVM = new Vector();
         try
         {
             HttpSession session = req.getSession();
             Vector vSearchASL = new Vector();
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             Vector vSelAttr = new Vector();
             if (menuAction.equals("searchForCreate"))
                 vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vSelAttr = (Vector) session.getAttribute("selectedAttr");
             vPVVM = (Vector) session.getAttribute("vACSearch");
             Vector vRSel = (Vector) session.getAttribute("vSelRows");
             if (menuAction.equals("searchForCreate"))
                 vRSel = null;
             if (vRSel == null)
                 vRSel = vPVVM;
             if (vRSel == null)
                 vRSel = new Vector();
             Integer recs = new Integer(0);
             if (vRSel.size() > 0)
                 recs = new Integer(vRSel.size());
             String recs2 = "";
             if (recs != null)
                 recs2 = recs.toString();
             String sKeyword = "";
             if (menuAction.equals("searchForCreate"))
             {
             	session.setAttribute("creRecsFound", recs2);
                 sKeyword = (String) session.getAttribute("creKeyword");
             }
             else
             {
             	session.setAttribute("recsFound", recs2);
                 sKeyword = (String) session.getAttribute("serKeyword");
             }
             // make keyWordLabel label request session
             if (sKeyword.equals("") && !menuAction.equals("searchForCreate"))
                 sKeyword = (String) session.getAttribute("LastAppendWord");
             if (sKeyword == null)
                 sKeyword = "";
             session.setAttribute("labelKeyword", "Permissible Value - " + sKeyword);
             Vector vSearchID = new Vector();
             Vector vSearchName = new Vector();
             Vector vSearchLongName = new Vector();
             Vector vSearchMeanDescription = new Vector();
             String evsDB = "";
             String umlsCUI = "";
             String tempCUI = "";
             String ccode = "";
             int ckCount=0;
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 PV_Bean PVBean = new PV_Bean();
                 PVBean = (PV_Bean) vRSel.elementAt(i);
                 vSearchID.addElement(PVBean.getPV_PV_IDSEQ());
                 vSearchName.addElement(PVBean.getPV_VALUE());
                 vSearchLongName.addElement(PVBean.getPV_SHORT_MEANING());
                 vSearchMeanDescription.addElement(PVBean.getPV_MEANING_DESCRIPTION());
                 evsDB = PVBean.getPV_EVS_DATABASE();
                 umlsCUI = PVBean.getPV_UMLS_CUI_VAL();
                 tempCUI = PVBean.getPV_TEMP_CUI_VAL();
                 ccode = PVBean.getPV_CONCEPT_IDENTIFIER();
                 String pvCount = "pv" + ckCount;
                 String chkCount = "CK" + ckCount;
 	            ckCount += 1;
                 if (vSelAttr.contains("Value"))
                     vResult.addElement(PVBean.getPV_VALUE());
                 if (vSelAttr.contains("Effective Begin Date"))
                     vResult.addElement(PVBean.getPV_BEGIN_DATE());
                 if (vSelAttr.contains("Effective End Date"))
                     vResult.addElement(PVBean.getPV_END_DATE());
                 if (vSelAttr.contains("Value Meaning Long Name"))
                     //vResult.addElement(PVBean.getPV_SHORT_MEANING());
                 	  vResult = addEditVMLink(PVBean
                               .getPV_SHORT_MEANING(),chkCount,vResult);
                 if (vSelAttr.contains("VM Public ID"))
                     vResult.addElement(PVBean.getPV_VM().getVM_ID());
                 if (vSelAttr.contains("VM Version"))
                     vResult.addElement(PVBean.getPV_VM().getVM_VERSION());
                 if (vSelAttr.contains("VM Description"))
                     vResult.addElement(PVBean.getPV_MEANING_DESCRIPTION());
                 if (vSelAttr.contains("Conceptual Domain"))
                     vResult = addMultiRecordConDomain(PVBean.getPV_CONCEPTUAL_DOMAIN(), PVBean
                                     .getPV_SHORT_MEANING(), vResult);
                 
                 /*
                  * EVS_Bean vmCon = PVBean.getVM_CONCEPT(); if (vmCon == null) vmCon = new EVS_Bean(); if
                  * (vSelAttr.contains("EVS Identifier")) vResult.addElement(vmCon.getCONCEPT_IDENTIFIER()); if
                  * (vSelAttr.contains("Description Source")) vResult.addElement(vmCon.getEVS_DEF_SOURCE()); if
                  * (vSelAttr.contains("Vocabulary")) vResult.addElement(PVBean.getPV_EVS_DATABASE());
                  */
                 VM_Bean vm = PVBean.getPV_VM();
                 Vector<EVS_Bean> vcon = vm.getVM_CONCEPT_LIST();
                 String conID = "", defsrc = "", vocab = "";
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
                 if (vSelAttr.contains("EVS Identifier"))
                     vResult.addElement(conID);
                 if (vSelAttr.contains("Description Source"))
                     vResult.addElement(defsrc);
                 if (vSelAttr.contains("Vocabulary"))
                     vResult.addElement(vocab);
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
             DataManager.setAttribute(session, "SearchLongName", vSearchLongName);
             DataManager.setAttribute(session, "SearchMeanDescription", vSearchMeanDescription);
             // for Back button, put search results and attributes on a stack
             if (!refresh.equals("true") && (!menuAction.equals("searchForCreate")))
                 stackSearchComponents("PermissibleValue", vPVVM, vRSel, vSearchID, vSearchName, vResult,
                                 vSearchASL, vSearchLongName);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getPVVMResult: " + e);
             logger.error("ERROR in GetACSearch-getPVVMResult : " + e.toString(), e);
         }
     }
 
     private String getVMResult(Vector<String> vResult, Vector<String> vSel, PV_Bean pv, String curField)
     {
         VM_Bean vm = pv.getPV_VM();
         Vector<EVS_Bean> vmCon = vm.getVM_CONCEPT_LIST();
         String evsId = "", defSrc = "", vocab = "";
         String fldValue = "";
         for (int i = 0; i < vmCon.size(); i++)
         {
             EVS_Bean eB = (EVS_Bean) vmCon.elementAt(i);
             if (!evsId.equals(""))
                 evsId += ", ";
             evsId += eB.getCONCEPT_IDENTIFIER();
             if (!defSrc.equals(""))
                 defSrc += ", ";
             defSrc += eB.getEVS_DEF_SOURCE();
             if (!vocab.equals(""))
                 vocab += ", ";
             vocab += eB.getEVS_DATABASE();
         }
         if (curField.equals(""))
         {
             if (vSel.contains("EVS Identifier"))
                 vResult.addElement(evsId);
             if (vSel.contains("Description Source"))
                 vResult.addElement(defSrc);
             if (vSel.contains("Vocabulary"))
                 vResult.addElement(vocab);
         }
         else
         {
             if (curField.equals("descSource"))
                 return defSrc;
             else if (curField.equals("umls") || curField.equals("Ident") || curField.equals("VMConcept"))
                 return evsId;
             else if (curField.equals("db") || curField.equals("database"))
               return vocab;
         }
         return fldValue;
     }
 
     /**
      * To get the value from the bean for the selected field, called from PVsortedRows.
      * 
      * @param curBean
      *            PV bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String PVField Value if selected field is found. otherwise empty string.
      */
     private String getPVVMFieldValue(PV_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             // EVS_Bean vmCon = curBean.getVM_CONCEPT();
             // if (vmCon == null) vmCon = new EVS_Bean();
             if (curField.equals("value"))
                 returnValue = curBean.getPV_VALUE();
             else if (curField.equals("meaning"))
                 returnValue = curBean.getPV_SHORT_MEANING();
             else if (curField.equals("MeanDesc"))
                 returnValue = curBean.getPV_MEANING_DESCRIPTION();
            // else if (curField.equals("database"))
            //     returnValue = curBean.getPV_EVS_DATABASE();
             else if (curField.equals("ConDomain"))
                 returnValue = curBean.getPV_CONCEPTUAL_DOMAIN();
             else if (curField.equals("BeginDate"))
                 returnValue = curBean.getPV_BEGIN_DATE();
             else if (curField.equals("EndDate"))
                 returnValue = curBean.getPV_END_DATE();
             else if (curField.equals("ValidValue"))
                 returnValue = curBean.getQUESTION_VALUE();
             else if (curField.equals("VMConcept") || curField.equals("descSource") || curField.equals("umls")
                             || curField.equals("Ident") || curField.equals("database")  || curField.equals("db"))
             {
                 return getVMResult(null, null, curBean, curField);
                 // EVS_Bean eBean = curBean.getVM_CONCEPT();
                 // if (eBean != null)
                 // returnValue = eBean.getCONCEPT_IDENTIFIER();
             }
             else if (curField.equals("ParConcept"))
             {
                 EVS_Bean eBean = curBean.getPARENT_CONCEPT();
                 if (eBean != null)
                     returnValue = eBean.getCONCEPT_IDENTIFIER();
             }
             else if (curField.equals("Origin"))
                 returnValue = curBean.getPV_VALUE_ORIGIN();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getPVField: " + e);
             logger.error("ERROR in GetACSearch-getPVField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the PV component, called from getACSortedResult. gets the
      * 'sortType' from request and 'vSelRows' vector from session. calls 'getPVVMFieldValue' to extract the value from
      * the bean for the selected sortType. modified bubble sort method to sort beans in the vector using the strings
      * compareToIgnoreCase method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getPVVMSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             PV_Bean PVBean = (PV_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 PVBean.setPV_CHECKED(true);
                             else
                                 PVBean.setPV_CHECKED(false);
                         }
                     }
                     // loop through the vector to get the bean row
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         PV_Bean PVSortBean1 = (PV_Bean) vSRows.elementAt(i);
                         String Name1 = getPVVMFieldValue(PVSortBean1, sortField);
                         int tempInd = i;
                         PV_Bean tempBean = PVSortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             PV_Bean PVSortBean2 = (PV_Bean) vSRows.elementAt(j);
                             String Name2 = getPVVMFieldValue(PVSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = PVSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = PVSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(PVSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             PV_Bean PVBean = (PV_Bean) vSortedRows.elementAt(i);
                             if (PVBean.getPV_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null && vCheckList.size() > 0)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-PVVMsortedRows: " + e);
             logger.error("ERROR in GetACSearch-PVVMsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * sorts the permissible values from the vd page
      * 
      * @param sortField
      *            String the column to sort
      */
     public void getVDPVSortedRows(VD_Bean vd, String sortField, String action, String id)
     {
         try
         {
             HttpSession session = m_classReq.getSession();
             // Vector<PV_Bean> vSRows = (Vector)session.getAttribute("VDPVList");
             //VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             Vector<PV_Bean> vSRows = vd.getVD_PV_List();
             Vector<PV_Bean> vSortedRows = new Vector();
             boolean isSorted = false;
             if (sortField != null && !sortField.equals(""))
             {
                 // check if the vector has the data
                 if (vSRows != null && vSRows.size() > 0)
                 {
                     // loop through the vector to get the bean row
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         PV_Bean PVSortBean1 = (PV_Bean) vSRows.elementAt(i);
                         String Name1 = getPVVMFieldValue(PVSortBean1, sortField);
                         int tempInd = i;
                         PV_Bean tempBean = PVSortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             PV_Bean PVSortBean2 = (PV_Bean) vSRows.elementAt(j);
                             String Name2 = getPVVMFieldValue(PVSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = PVSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = PVSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(PVSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     // DataManager.setAttribute(session, "VDPVList", vSortedRows);
                     vd.setVD_PV_List(vSortedRows);
                     if (id != null && action != null && action.equals("view")){
                         String viewVD = "viewVD" + id;
                     	DataManager.setAttribute(session, viewVD, vd);
                     }else{
                        DataManager.setAttribute(session, "m_VD", vd);
                     }   
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-VDPVsortedRows: " + e);
             logger.error("ERROR in GetACSearch-VDPVsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the refreshed vector after designating the component to display in SearchPage. called from the servlet
      * gets the bean from the 'vSelRows' vector that has same as desID. updates the used by context attribute of the
      * selected element with the existing one. calls 'getDEResult' to display the result.
      * 
      * @param desContext
      *            designated context name.
      * @param desContextID
      *            designated context IDseq.
      * @param action
      *            to recorgnize created or deleted action.
      * 
      */
     public void refreshDesData(String desCompID, String desContext, String desContextID, String action, String deAlt,
                     String deRD)
     {
         HttpSession session = m_classReq.getSession();
         Vector vSRows = new Vector();
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         if (sMenuAction.equals("searchForCreate"))
             vSRows = (Vector) session.getAttribute("vACSearch");
         else
             vSRows = (Vector) session.getAttribute("vSelRows");
         String selComp = (String) session.getAttribute("searchAC");
         Vector vResult = new Vector();
         if (vSRows.size() > 0)
         {
             for (int i = 0; i < (vSRows.size()); i++)
             {
                 DE_Bean DEBean = new DE_Bean();
                 DEBean = (DE_Bean) vSRows.elementAt(i);
                 // if the deID is same as desID make a copy of this, replace name & context, put back in the vector
                 if (DEBean.getDE_DE_IDSEQ().equals(desCompID))
                 {
                     Vector vUsedBy = DEBean.getDE_USEDBY_CONTEXT_ID();
                     // store alt name ref doc
                     DEBean.setALTERNATE_NAME(deAlt);
                     DEBean.setREFERENCE_DOCUMENT(deRD);
                     // insert the bean if created
                     if (action.equals("INS"))
                     {
                         String sUsed = DEBean.getDE_USEDBY_CONTEXT();
                         if ((sUsed == null) || (sUsed.equals("")))
                             DEBean.setDE_USEDBY_CONTEXT(desContext);
                         else
                             DEBean.setDE_USEDBY_CONTEXT(sUsed + ", " + desContext);
                         // add the context id in usedby vector attribute
                         if (vUsedBy == null)
                             vUsedBy = new Vector();
                         vUsedBy.addElement(desContextID);
                     }
                     // remove the used by context name if deleted
                     else if (action.equals("DEL"))
                     {
                         String sUsed = DEBean.getDE_USEDBY_CONTEXT();
                         DEBean.setDE_USEDBY_CONTEXT(ParseString(sUsed, desContext));
                         // remove context id from used by vector attribute
                         if (vUsedBy != null && vUsedBy.contains(desContextID))
                             vUsedBy.removeElement(desContextID);
                     }
                     // getDESortedRows(req, res); remove the sorting here so that it won't affect the list
                     DEBean.setDE_USEDBY_CONTEXT_ID(vUsedBy);
                     vSRows.setElementAt(DEBean, i);
                     break;
                 }
             }
             // store the bean vector in the session
             if (sMenuAction.equals("searchForCreate"))
                 DataManager.setAttribute(session, "vACSearch", vSRows);
             else
                 DataManager.setAttribute(session, "vSelRows", vSRows);
         }
     }
 
     /**
      * makes the comma delimited context name for used by contexts
      * 
      * @param sUsed
      * @param desContext
      */
     private String ParseString(String sUsed, String desContext)
     {
         String subUsed = "";
         if ((sUsed != null) && (!sUsed.equals("")))
         {
             StringTokenizer desTokens = new StringTokenizer(sUsed, ",");
             while (desTokens.hasMoreTokens())
             {
                 String thisToken = desTokens.nextToken().trim();
                 if (!thisToken.equalsIgnoreCase(desContext))
                 {
                     if (subUsed.equals(""))
                         subUsed = thisToken;
                     else
                         subUsed = subUsed + ", " + thisToken;
                 }
             }
         }
         return subUsed;
     }
 
     /**
      * 
      * @param sSynonym
      */
     private String parseSynonymForHD(String sSynonym)
     {
         String isHeader = "false";
         int index = 0;
         if ((sSynonym != null) && (!sSynonym.equals("")))
         {
             if (sSynonym.indexOf(">HD<") != -1)
                 isHeader = "true";
         }
         return isHeader;
     }
 
     /**
      * To get the refreshed vector after creating or editing the component to display in SearchPage. gets the 'ckName' a
      * row that was selected before the create action and 'vSelRows' vector from session vecotr of selected rows after
      * the search. First insert or replace the checked row data calls 'getDESortedRows' to sort the data, calls
      * 'getDEResult' to add in to the vector for display.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param m_DE
      *            DE_Bean if the selected component is DataElement
      * @param m_DEC
      *            DEC_Bean if the selected component is DataElementConcept
      * @param m_VD
      *            VD_Bean if the selected component is ValueDomain
      * @param sAction
      *            earlier action template, version or Edit.
      * 
      */
     public void refreshData(HttpServletRequest req, HttpServletResponse res, DE_Bean m_DE, DEC_Bean m_DEC,
                     VD_Bean m_VD, Quest_Bean m_Quest, String sAction, String oldIDseq) throws Exception
     {
         HttpSession session = req.getSession();
         String sCKname = (String) session.getAttribute("ckName");
         Vector vSRows = (Vector) session.getAttribute("vSelRows");
         Vector vResult = new Vector();
         String thisIDseq = "";
         if (vSRows.size() > 0)
         {
             if (m_DE != null)
             {
                 // copy the non modifed attributes to redisplay
                 DE_Bean oldDEBean = (DE_Bean) session.getAttribute("oldDEBean");
                 m_DE = m_DE.cloneDE_Bean(oldDEBean, "Partial");
                 // remove used by for version
                 if (sAction.equals("Version") || sAction.equals("Template"))
                 {
                     m_DE.setDE_USEDBY_CONTEXT("");
                     m_DE.setDE_USEDBY_CONTEXT_ID(new Vector());
                 }
                 // refresh the pv counts of this DE
                 m_DE = refreshSearchPVCount(m_DE);
                 // insert a row and sort it
                 if ((sAction.equals("Template")) && (m_DE != null))
                 {
                     vSRows.insertElementAt(m_DE, 0);
                     DataManager.setAttribute(session, "vSelRows", vSRows);
                     // reset the checking to thisRow
                     Vector vCheckList = new Vector();
                     vCheckList.addElement("CK0");
                     DataManager.setAttribute(session, "CheckList", vCheckList);
                     // getDESortedRows(req, res);
                 }
                 else if (!sAction.equals("Refresh"))
                 {
                     // loop through the selected rows vector to get the matched idseq
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         DE_Bean thisDE = (DE_Bean) vSRows.elementAt(i);
                         thisIDseq = thisDE.getDE_DE_IDSEQ();
                         // match idseqs of the changed one to the old one.
                         if (thisIDseq.equals(oldIDseq))
                         {
                             // update row for edit and insert row for version
                             if (sAction.equals("Edit"))
                                 vSRows.setElementAt(m_DE, i);
                             else if (sAction.equals("Version"))
                                 vSRows.insertElementAt(m_DE, i);
                             break;
                         }
                     }
                 }
                 // store it in selrows and get the final result vector
                 DataManager.setAttribute(session, "vSelRows", vSRows);
                 getDEResult(req, res, vResult, "");
             }
             else if (m_DEC != null)
             {
                 // insert a row and sort it
                 if (sAction.equals("Template"))
                 {
                     vSRows.insertElementAt(m_DEC, 0);
                     DataManager.setAttribute(session, "vSelRows", vSRows);
                     // reset the checking to thisRow
                     Vector vCheckList = new Vector();
                     vCheckList.addElement("CK0");
                     DataManager.setAttribute(session, "CheckList", vCheckList);
                     // sort the rows
                     // getDECSortedRows(req, res);
                 }
                 else if (!sAction.equals("Refresh"))
                 {
                     // loop through the selected rows vector to get the matched idseq
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         DEC_Bean thisDEC = (DEC_Bean) vSRows.elementAt(i);
                         /*
                          * String sOCCondrID = m_DEC.getDEC_OC_CONDR_IDSEQ(); if(sOCCondrID != null &&
                          * !sOCCondrID.equals("")) thisDEC.setDEC_OC_CONDR_IDSEQ(sOCCondrID); String sPCCondrID =
                          * m_DEC.getDEC_PROP_CONDR_IDSEQ(); if(sPCCondrID != null && !sPCCondrID.equals(""))
                          * thisDEC.setDEC_PROP_CONDR_IDSEQ(sPCCondrID);
                          */
                         thisIDseq = thisDEC.getDEC_DEC_IDSEQ();
                         // match idseqs of the changed one to the old one.
                         if (thisIDseq.equals(oldIDseq))
                         {
                             // update row for edit and insert row for version
                             if (sAction.equals("Edit"))
                                 vSRows.setElementAt(m_DEC, i);
                             else if (sAction.equals("Version"))
                                 vSRows.insertElementAt(m_DEC, i);
                             break;
                         }
                     }
                 }
                 // store it in selrows and get the final result vector
                 DataManager.setAttribute(session, "vSelRows", vSRows);
                 getDECResult(req, res, vResult, "");
             }
             else if (m_VD != null)
             {
                 // insert a row and sort it
                 if (sAction.equals("Template"))
                 {
                     vSRows.insertElementAt(m_VD, 0);
                     DataManager.setAttribute(session, "vSelRows", vSRows);
                     // reset the checking to thisRow
                     Vector vCheckList = new Vector();
                     vCheckList.addElement("CK0");
                     DataManager.setAttribute(session, "CheckList", vCheckList);
                     // getVDSortedRows(req, res);
                 }
                 else if (!sAction.equals("Refresh"))
                 {
                     // loop through the selected rows vector to get the matched idseq
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         VD_Bean thisVD = (VD_Bean) vSRows.elementAt(i);
                         // String sRepCondrID = m_VD.getVD_REP_CONDR_IDSEQ();
                         // if(sRepCondrID != null && !sRepCondrID.equals(""))
                         // thisVD.setVD_REP_CONDR_IDSEQ(sRepCondrID);
                         thisIDseq = thisVD.getVD_VD_IDSEQ();
                         // match idseqs of the changed one to the old one.
                         if (thisIDseq.equals(oldIDseq))
                         {
                             // update row for edit and insert row for version
                             if (sAction.equals("Edit"))
                                 vSRows.setElementAt(m_VD, i);
                             else if (sAction.equals("Version"))
                                 vSRows.insertElementAt(m_VD, i);
                             break;
                         }
                     }
                 }
                 // store it in selrows and get the final result vector
                 DataManager.setAttribute(session, "vSelRows", vSRows);
                 getVDResult(req, res, vResult, "");
             }
             else if (m_Quest != null)
             {
                 // loop through the selected rows vector to get checked row
                 for (int i = 0; i < (vSRows.size()); i++)
                 {
                     String ckName = ("CK" + i);
                     if (ckName.equals(sCKname))
                     {
                         // update row for edit
                         if (sAction.equals("Edit"))
                             vSRows.setElementAt(m_Quest, i);
                     }
                 }
                 // store it in selrows and get the final result vector
                 DataManager.setAttribute(session, "vSelRows", vSRows);
                 getQuestionResult(req, res, vResult);
             }
             // finally store the display data in the result session
             DataManager.setAttribute(session, "results", vResult);
         }
     }
 
     /**
      * @param sSearchFor
      */
     public Vector refreshSearchPage(String sSearchFor) throws Exception
     {
         EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
         Vector vRes = new Vector();
         String actType = (String) m_classReq.getParameter("actSelect");
         if (sSearchFor != null)
         {
             if (sSearchFor.equals("DataElement"))
                 getDEResult(m_classReq, m_classRes, vRes, "true");
             else if (sSearchFor.equals("DataElementConcept"))
                 getDECResult(m_classReq, m_classRes, vRes, "true");
             else if (sSearchFor.equals("ValueDomain"))
                 getVDResult(m_classReq, m_classRes, vRes, "true");
             else if (sSearchFor.equals("ConceptualDomain"))
                 getCDResult(m_classReq, m_classRes, vRes, "true");
             else if ((sSearchFor.equals("EVSValueMeaning") || sSearchFor.equals("CreateVM_EVSValueMeaning")
                             || sSearchFor.equals("ParentConcept") || sSearchFor.equals("ParentConceptVM")
                             || sSearchFor.equals("ObjectClass") || sSearchFor.equals("Property")
                             || sSearchFor.equals("RepTerm") || sSearchFor.equals("ParentConceptVM")
                             || sSearchFor.equals("ObjectQualifier") || sSearchFor.equals("PropertyQualifier")
                             || sSearchFor.equals("RepQualifier") || sSearchFor.equals("VMConcept")
                             || sSearchFor.equals("EditVMConcept"))
                             && !actType.equals("doVocabChange"))
                 evs.get_Result(m_classReq, m_classRes, vRes, "");
         }
         return vRes;
     }
 
     /**
      * pv count refresh action in de search
      * 
      * @param DEBean
      *            DE_Bean
      * @return DE_Bean
      * @throws Exception
      */
     private DE_Bean refreshSearchPVCount(DE_Bean DEBean) throws Exception
     {
         // update the count of the current bean.
         HttpSession session = m_classReq.getSession();
         boolean isNewVD = false;
         // do the pvac search to get the names and count of the pvs for this vd
         VD_Bean vd = DEBean.getDE_VD_Bean();
         if (vd == null)
             return DEBean;
         Vector<PV_Bean> vdpvs = vd.getVD_PV_List();
         Integer pvCount = 0;
         // get first value
         String pvValue = "";
         if (vdpvs != null && vdpvs.size() > 0)
         {
             pvCount = Integer.valueOf(vdpvs.size());
             PV_Bean pv = (PV_Bean) vdpvs.elementAt(0);
             pvValue = pv.getPV_VALUE();
         }
         // compare the vd id from old to new if they are the same.
         DE_Bean oldDEBean = (DE_Bean) session.getAttribute("oldDEBean");
         String oldVD = oldDEBean.getDE_VD_IDSEQ();
         String newVD = DEBean.getDE_VD_IDSEQ();
         if (oldVD != null && newVD != null && !oldVD.equals("") && !newVD.equals("") && !oldVD.equals(newVD))
             isNewVD = true;
         // add the pv counts of the DE bean
         DEBean.setDE_Permissible_Value(pvValue);
         DEBean.setDE_Permissible_Value_Count(pvCount);
         if (isNewVD == false)
         {
             // update the count of the all beans of the same vd only if it was changed
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
             if (vSRows != null)
             {
                 String thisVDID = DEBean.getDE_VD_IDSEQ();
                 for (int k = 0; k < vSRows.size(); k++)
                 {
                     DE_Bean thisDE = (DE_Bean) vSRows.elementAt(k);
                     // if the same vd id, update pv count
                     if (thisDE.getDE_VD_IDSEQ().equalsIgnoreCase(thisVDID))
                     {
                         // change it only if necessary.
                         thisDE.setDE_Permissible_Value_Count(pvCount);
                         thisDE.setDE_Permissible_Value(pvValue);
                         // reset the element
                         vSRows.setElementAt(thisDE, k);
                     }
                 }
                 DataManager.setAttribute(session, "vSelRows", vSRows);
             }
         }
         return DEBean;
     }
 
     /**
      * To start a new search called from CurationServlet. selected component, keyword, selected context, seletcted
      * status parameters of request are stored in session. calls method 'doDESearch' for selected component to get the
      * result set from the database. search result from the database is stored in session vector "vACSearch". calls
      * method 'setAttributeValues' to get list of selected attributes at search. calls method 'getDEResult' for selected
      * component to get vector of selected attribute and checked rows from the ACSearch vector. final result vector is
      * stored in the session vector "results".
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     public void getACSearchForCreate(HttpServletRequest req, HttpServletResponse res, boolean isIntSearch)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vAC = new Vector();
             DataManager.setAttribute(session, "sortType", "longName");
             SetACService setAC = new SetACService(m_servlet);
             EVSSearch evs = new EVSSearch(req, res, m_servlet);
             String sSearchAC = (String) req.getParameter("listSearchFor");
             if (sSearchAC == null)
             {
                 sSearchAC = (String) session.getAttribute("searchAC");
                 if (sSearchAC == null)
                     logger.error("In GetACSearch-getKeywordResult : no Component is selected");
             }
             DataManager.setAttribute(session, "creSearchAC", sSearchAC);
             // get the search in data
             String sSearchIn = (String) req.getParameter("listSearchIn");
             if (sSearchIn == null)
                 sSearchIn = "longName";
             req.setAttribute("creSearchIn", sSearchIn);
             DataManager.setAttribute(session, "creSearchInBlocks", sSearchIn);
             boolean isBlockSearch = false;
             String dtsVocab = req.getParameter("listContextFilterVocab");
             if (dtsVocab != null && !dtsVocab.equals(""))
                 isBlockSearch = true;
             if (dtsVocab == null)
                 dtsVocab = "";
             // System.out.println(" dtsVocab " + dtsVocab);
             DataManager.setAttribute(session, "dtsVocab", dtsVocab);
             String sSearchInEVS = (String) req.getParameter("listSearchInEVS");
             if (sSearchInEVS == null)
                 sSearchInEVS = "Synonym";
             DataManager.setAttribute(session, "SearchInEVS", sSearchInEVS);
             // System.out.println(sSearchIn + " search in " + sSearchInEVS);
             // filter by Retired Concepts
             String sRetired = (String) req.getParameter("rRetired");
             DataManager.setAttribute(session, "creRetired", sRetired);
             if (sRetired == null)
                 sRetired = "";
             String sMetaSource = (String) req.getParameter("listContextFilterSource");
             if (sMetaSource == null)
                 sMetaSource = "All Sources";
             DataManager.setAttribute(session, "MetaSource", sMetaSource);
             String sMetaLimit = (String) req.getParameter("listMetaLimit");
             int intMetaLimit = 0;
             if (sMetaLimit != null)
             {
                 Integer iMetaLimit = new Integer(sMetaLimit);
                 intMetaLimit = iMetaLimit.intValue();
                 if (intMetaLimit < 100)
                     intMetaLimit = 100;
             }
             else
                 intMetaLimit = 100;
             // do the keyword search for the selected component
             String sKeyword = (String) req.getParameter("keyword");
             if (sKeyword == null)
                 sKeyword = "";
             if (isIntSearch == true)
                 sKeyword = "";
             sKeyword = sKeyword.trim();
             DataManager.setAttribute(session, "creKeyword", sKeyword);
             UtilService util = new UtilService();
             sKeyword = util.parsedStringSingleQuoteOracle(sKeyword);
 
             // call the method to set the attribute checked values if not the initial value
             if (isIntSearch == false)
                 setAttributeValues(req, res, sSearchAC, "searchForCreate");
             // filter by context; looks for ACs that uses multi select context and gets it from requests according to
             // that
             String sContext = "";
             // if (sSearchAC.equals("RepTerm") || sSearchAC.equals("Property") || sSearchAC.equals("ObjectClass"))
             if (isBlockSearch == true)
             {
                 sContext = (String) req.getParameter("listContextFilter");
                 DataManager.setAttribute(session, "creContext", sContext);
                 DataManager.setAttribute(session, "creContextBlocks", sContext);
                 if (sContext == null || sContext.equals("AllContext"))
                     sContext = "";
             }
             else
                 sContext = getMultiReqValues(sSearchAC, "searchForCreate", "Context");
             // filter by contextUse
             String sContextUse = (String) req.getParameter("rContextUse");
             DataManager.setAttribute(session, "creContextUse", sContextUse);
             if (sContextUse == null)
                 sContextUse = "";
             // filter by version
             String sVersion = (String) req.getParameter("rVersion");
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 sVersion = "Yes";
             DataManager.setAttribute(session, "creVersion", sVersion);
             // get the version number if other
             String txVersion = "";
             if (sVersion != null && sVersion.equals("Other"))
                 txVersion = (String) req.getParameter("tVersion");
             if (txVersion == null)
                 txVersion = "";
             DataManager.setAttribute(session, "creVersionNum", txVersion);
             double dVersion = 0;
             // validate the version and display message if not valid
             if (txVersion != null && !txVersion.equals(""))
             {
                 String sValid = setAC.checkVersionDimension(txVersion);
                 if (sValid != null && !sValid.equals(""))
                 {
                     logger.error("not a good version ;" + sValid);
                 }
                 else
                 {
                     Double DVersion = new Double(txVersion);
                     dVersion = DVersion.doubleValue();
                 }
             }
             // make sVersion to empty if all or other
             if (sVersion == null || sVersion.equals("All") || sVersion.equals("Other"))
                 sVersion = "";
             // filter by value domain type enumerated
             String sVDType = "";
             String sVDTypeEnum = (String) req.getParameter("enumBox");
             DataManager.setAttribute(session, "creVDTypeEnum", sVDTypeEnum);
             if (sVDTypeEnum != null)
                 sVDType = "E";
             // filter by value domain type non enumerated
             String sVDTypeNonEnum = (String) req.getParameter("nonEnumBox");
             DataManager.setAttribute(session, "creVDTypeNonEnum", sVDTypeNonEnum);
             if (sVDTypeNonEnum != null && !sVDTypeNonEnum.equals(""))
             {
                 if (!sVDType.equals(""))
                     sVDType = sVDType + ", ";
                 sVDType = sVDType + "N";
             }
             // filter by value domain type enumerated by reference
             String sVDTypeRef = (String) req.getParameter("refEnumBox");
             DataManager.setAttribute(session, "creVDTypeRef", sVDTypeRef);
             if (sVDTypeRef != null && !sVDTypeRef.equals(""))
             {
                 if (!sVDType.equals(""))
                     sVDType = sVDType + ", ";
                 sVDType = sVDType + "R";
             }
             sVDType = sVDType.trim();
             String sCDid = "";
             sCDid = (String) req.getParameter("listCDName");
             if (sCDid == null || sCDid.equals("All Domains"))
                 sCDid = "";
             DataManager.setAttribute(session, "creSelectedCD", sCDid);
             // to get a string from multiselect list
             String sStatus = "";
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 DataManager.setAttribute(session, "creStatus", new Vector());
             else
                 sStatus = getStatusValues(req, res, sSearchAC, "searchForCreate", isBlockSearch);
             if ((sStatus == null) || (sStatus.equals("AllStatus")))
                 sStatus = "";
             // store it in the session for block search
             if (isBlockSearch && sStatus.equals(""))
                 DataManager.setAttribute(session, "creStatusBlocks", "AllStatus");
             else if (isBlockSearch)
                 DataManager.setAttribute(session, "creStatusBlocks", sStatus);
             // filter by registration status
             String sRegStatus = (String) req.getParameter("listRegStatus");
             DataManager.setAttribute(session, "creRegStatus", sRegStatus);
             if (sRegStatus == null || sRegStatus.equals("allReg"))
                 sRegStatus = "";
             // filter by derivation type
             String sDerType = (String) req.getParameter("listDeriveType");
             DataManager.setAttribute(session, "creDerType", sDerType);
             if (sDerType == null || sDerType.equals("allDer"))
                 sDerType = "";
             // filter by data type
             String sDataType = (String) req.getParameter("listDataType");
             DataManager.setAttribute(session, "creDataType", sDataType);
             if (sDataType == null || sDataType.equals("allData"))
                 sDataType = "";
             // fitler by date created from date
             String sCreatedFrom = (String) req.getParameter("createdFrom");
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 sCreatedFrom = "";
             DataManager.setAttribute(session, "creCreatedFrom", sCreatedFrom);
             if (sCreatedFrom == null)
                 sCreatedFrom = "";
             // fitler by date created To date
             String sCreatedTo = (String) req.getParameter("createdTo");
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 sCreatedTo = "";
             DataManager.setAttribute(session, "creCreatedTo", sCreatedTo);
             if (sCreatedTo == null)
                 sCreatedTo = "";
             // fitler by date Modified from date
             String sModifiedFrom = (String) req.getParameter("modifiedFrom");
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 sModifiedFrom = "";
             DataManager.setAttribute(session, "creModifiedFrom", sModifiedFrom);
             if (sModifiedFrom == null)
                 sModifiedFrom = "";
             // fitler by date Modified To date
             String sModifiedTo = (String) req.getParameter("modifiedTo");
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 sModifiedTo = "";
             DataManager.setAttribute(session, "creModifiedTo", sModifiedTo);
             if (sModifiedTo == null)
                 sModifiedTo = "";
             // fitler by Modifier type
             String sModifier = (String) req.getParameter("modifier");
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 sModifier = "";
             DataManager.setAttribute(session, "creModifier", sModifier);
             if ((sModifier == null) || sModifier.equals("allUsers"))
                 sModifier = "";
             // fitler by Creator type
             String sCreator = (String) req.getParameter("creator");
             if (isIntSearch == true && sSearchAC.equals("ConceptualDomain"))
                 sCreator = ""; 
             DataManager.setAttribute(session, "creCreator", sCreator);
             if ((sCreator == null) || sCreator.equals("allUsers"))
                 sCreator = "";
             String sUISearchType = (String) req.getAttribute("UISearchType");
             if (sUISearchType == null || sUISearchType.equals("nothing"))
                 sUISearchType = "term";
             // call the method to get the data from the database and to get the final result vector
             Vector vResult = new Vector();
             String sMinID = sKeyword;
             if (sSearchAC.equals("DataElement"))
             {
                 if (sSearchIn.equals("minID"))
                 {
                     doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                     sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "",
                                     sMinID, "", "", "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                 }
                 else if (sSearchIn.equals("histID"))
                 {
                     String sHistID = sKeyword;
                     doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                     sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "",
                                     "", sHistID, "", "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                 }
                 // search in by doc text
                 else if (sSearchIn.equals("docText"))
                 {
                     String sDocs[] = req.getParameterValues("listRDType");
                     String sDocTypes = "ALL";
                     if (sDocs != null && sDocs.length > 0)
                         sDocTypes = getDocTypeValues(sDocs, "searchForCreate");
                     String sDocText = sKeyword;
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier,
                                         sDocText, sDocTypes, "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                         sDerType, vAC);
                     }
                 }
                 // search in by names adn doc text long name and historic short cde name
                 else if (sSearchIn.equals("NamesAndDocText"))
                 {
                     String sDocTypes = "Preferred Question Text, HISTORIC SHORT CDE NAME";
                     String sDocText = sKeyword;
                     if (sSearchAC.equals("DataElement"))
                     {
                         doDESearch("", sKeyword, "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                         sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier,
                                         sDocText, sDocTypes, "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                         sDerType, vAC);
                     }
                 }
                 // search in for origin
                 else if (sSearchIn.equals("origin"))
                 {
                     String sOrigin = sKeyword;
                     doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                     sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "",
                                     "", "", "", sOrigin, "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                 }
                 // search in by permissible value
                 else if (sSearchIn.equals("permValue"))
                 {
                     String permValue = sKeyword;
                     doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                     sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "",
                                     "", "", permValue, "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                 }
                 // search in by concept name/identifier
                 else if (sSearchIn.equals("concept"))
                 {
                     String sCon = sKeyword;
                     doDESearch("", "", "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                     sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "",
                                     "", "", "", "", "", "", "", "", "", "", "", "", "", sCon, sDerType, vAC);
                 }
                 else
                 {
                     doDESearch("", sKeyword, "", sContext, sContextUse, sVersion, dVersion, sStatus, sRegStatus,
                                     sCreatedFrom, sCreatedTo, sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "",
                                     "", "", "", "", "", "", "", "", "", "", "", "", "", "", sDerType, vAC);
                 }
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 getDEResult(req, res, vResult, "");
             }
             else if (sSearchAC.equals("DataElementConcept"))
             {
                 if (sSearchIn.equals("minID"))
                 {
                     doDECSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                     sModifiedTo, sCreator, sModifier, sMinID, "", "", "", dVersion, sCDid, "", "", "",
                                     "", vAC);
                 }
                 else if (sSearchIn.equals("origin"))
                 {
                     String sOrigin = sKeyword;
                     doDECSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                     sModifiedTo, sCreator, sModifier, "", "", "", sOrigin, dVersion, sCDid, "", "", "",
                                     "", vAC);
                 }
                 else if (sSearchIn.equals("concept"))
                 {
                     String sCon = sKeyword;
                     doDECSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                     sModifiedTo, sCreator, sModifier, "", "", "", "", dVersion, sCDid, "", "", "",
                                     sCon, vAC);
                 }
                 else
                 {
                     doDECSearch("", sKeyword, "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                     sModifiedTo, sCreator, sModifier, "", "", "", "", dVersion, sCDid, "", "", "", "",
                                     vAC);
                 }
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 getDECResult(req, res, vResult, "");
             }
             else if (sSearchAC.equals("ValueDomain"))
             {
                 if (sSearchIn.equals("minID"))
                 {
                     doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                     sModifiedFrom, sModifiedTo, sCreator, sModifier, sMinID, "", "", dVersion, sCDid,
                                     "", "", "", "", "", "", sDataType, vAC);
                 }
                 else if (sSearchIn.equals("permValue"))
                 {
                     String sPermValue = sKeyword;
                     doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                     sModifiedFrom, sModifiedTo, sCreator, sModifier, "", sPermValue, "", dVersion,
                                     sCDid, "", "", "", "", "", "", sDataType, vAC);
                 }
                 else if (sSearchIn.equals("origin"))
                 {
                     String sOrigin = sKeyword;
                     doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                     sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", sOrigin, dVersion, sCDid,
                                     "", "", "", "", "", "", sDataType, vAC);
                 }
                 else if (sSearchIn.equals("concept"))
                 {
                     String sCon = sKeyword;
                     doVDSearch("", "", "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                     sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", "", dVersion, sCDid, "",
                                     "", "", "", "", sCon, sDataType, vAC);
                 }
                 else
                 {
                     doVDSearch("", sKeyword, "", sContext, sVersion, sVDType, sStatus, sCreatedFrom, sCreatedTo,
                                     sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", "", dVersion, sCDid, "",
                                     "", "", "", "", "", sDataType, vAC);
                 }
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 getVDResult(req, res, vResult, "");
             }
             else if (sSearchAC.equals("ConceptualDomain"))
             {
                 // search only if not the first time or if the first time and not all contexts
               //  if (isIntSearch == false || (isIntSearch == true && !sContext.equals("")))
                // {
                     if (sSearchIn.equals("minID"))
                     {
                         doCDSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, sMinID, "", dVersion, "", "", "", vAC);
                     }
                     else if (sSearchIn.equals("origin"))
                     {
                         String sOrigin = sKeyword;
                         doCDSearch("", "", "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo, sModifiedFrom,
                                         sModifiedTo, sCreator, sModifier, "", sOrigin, dVersion, "", "", "", vAC);
                     }
                     else
                     {
                         doCDSearch("", sKeyword, "", sContext, sVersion, sStatus, sCreatedFrom, sCreatedTo,
                                         sModifiedFrom, sModifiedTo, sCreator, sModifier, "", "", dVersion, "", "", "",
                                         vAC);
                     }
                     DataManager.setAttribute(session, "vACSearch", vAC);
                     getCDResult(req, res, vResult, "");
               //  }
                 
             }
             else if (sSearchAC.equals("PermissibleValue"))
             {
                 // store the CRFValue id in the session
                 String crfValueID = req.getParameter("QCValueIDseq");
                 String crfValueName = req.getParameter("QCValueName");
                 if (crfValueID != null && !crfValueID.equals(""))
                 {
                     DataManager.setAttribute(session, "selCRFValueID", crfValueID);
                     DataManager.setAttribute(session, "selQCValueName", crfValueName);
                 }
                 PVServlet pvser = new PVServlet(m_classReq, m_classRes, m_servlet);
                 vAC = pvser.searchPVAttributes(sKeyword, sStatus, "", "");
                 if (isIntSearch == false)
                 {
                     // **************why is this needed******************
                     vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired,
                                     sMetaSource, intMetaLimit, true, -1, "");
                     evs.get_Result(req, res, vResult, "DEF");
                 }
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 DataManager.setAttribute(session, "vPVVMResult", vAC);
                 getPVVMResult(req, res, vResult, "");
             }
             else if (sSearchAC.equals("ValueMeaning"))
             {
                 // doVMSearch(sKeyword, sCDid, vAC);
                 // DataManager.setAttribute(session, "vACSearch", vAC);
                 // getVMResult(req, res, vResult);
                 VMServlet vmSer = new VMServlet(req, res, m_servlet);
                 vmSer.readDataForSearch(this);
             }
             else if (sSearchAC.equals("EVSValueMeaning") || sSearchAC.equals("VMConcept") || sSearchAC.equals("EditVMConcept"))
             {
                 // first do the concept class search
                 if (sSearchIn.equals("publicID"))
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 // now the evs search
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 // make sure that name value pair is not allowed for primary concepts
                 markNVPForPrimaryConcept(vAC);
                 // store it in the session
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("CreateVM_EVSValueMeaning"))
             {
                 // first do the concept class search
                 if (sSearchIn.equals("publicID"))
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 // now the evs search
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 if (vAC != null)
                     DataManager.setAttribute(session, "vCreateVM_EVSValueMeaning", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("ParentConcept"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 if (vAC != null)
                     DataManager.setAttribute(session, "vParentConcept", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("ParentConceptVM"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, false, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 if (vAC != null)
                     DataManager.setAttribute(session, "vParentConceptVM", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("ObjectClass"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 if (sSearchIn.equals("publicID"))
                 {
                     do_caDSRSearch("", sContext, sStatus, sKeyword, vAC, "OC", "", "");
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 }
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                 {
                     do_caDSRSearch(sKeyword, sContext, sStatus, "", vAC, "OC", "", "");
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 }
                 // To search Synonym in EVS, need to filter
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("Property"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 if (sSearchIn.equals("publicID"))
                 {
                     do_caDSRSearch("", sContext, sStatus, sKeyword, vAC, "PROP", "", "");
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 }
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                 {
                     do_caDSRSearch(sKeyword, sContext, sStatus, "", vAC, "PROP", "", "");
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 }
                 // To search synonym you need to filter
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("RepTerm"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 String nonEVSSearchInd = (String)req.getParameter("nonEVSRepTermSearch");
                // System.out.println(nonEVSSearchInd);
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 if(nonEVSSearchInd!= null && nonEVSSearchInd.equals("true"))
                 {
                 	do_nonEVSRepTermSearch(sKeyword, sContext, sStatus, "", vAC, "REP", "", "");
                 	session.setAttribute("nonEVSSearchRepTerm", "false");
                 }
                 else{
                 if (sSearchIn.equals("publicID"))
                 {
                     do_caDSRSearch("", sContext, sStatus, sKeyword, vAC, "REP", "", "");
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 }
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                 {
                     do_caDSRSearch(sKeyword, sContext, sStatus, "", vAC, "REP", "", "");
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 }
                 // To search Synonym in EVS, need to filter
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 }
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("ObjectQualifier"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 if (sSearchIn.equals("publicID"))
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 // To search synonym you need to filter
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("PropertyQualifier"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 if (sSearchIn.equals("publicID"))
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 // To search synonym you need to filter
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             else if (sSearchAC.equals("RepQualifier") || sSearchAC.equals("RepTermQualifier"))
             {
                 String sConteIdseq = (String) req.getParameter("sConteIdseq");
                 if (sConteIdseq == null)
                     sConteIdseq = "";
                 if (sSearchIn.equals("publicID"))
                     vAC = do_ConceptSearch("", "", sContext, sStatus, sKeyword, "", "", vAC);
                 else if (!sSearchIn.equals("Code") || !sSearchInEVS.equals("MetaCode"))
                     vAC = do_ConceptSearch(sKeyword, "", sContext, sStatus, "", "", "", vAC);
                 // To search synonym you need to filter
                 sKeyword = (String) session.getAttribute("creKeyword");
                 vAC = evs.doVocabSearch(vAC, sKeyword, dtsVocab, sSearchInEVS, "", sSearchAC, sRetired, sMetaSource,
                                 intMetaLimit, true, -1, "");
                 DataManager.setAttribute(session, "vACSearch", vAC);
                 evs.get_Result(req, res, vResult, "");
             }
             // store result vector in the attribute
             if (!sSearchAC.equals("ValueMeaning"))
                 DataManager.setAttribute(session, "results", vResult);
         }
         catch (Exception e)
         {
            logger.error("ERROR - GetACSearch-getACSearchForCreate: " + e.toString(), e);
         }
     }
 
     private void markNVPForPrimaryConcept(Vector<EVS_Bean> vAC)
     {
         String totalCon = m_classReq.getParameter("vmConOrder");
         if (totalCon == null || totalCon.equals("0"))
         {
             for (int i = 0; i < vAC.size(); i++)
             {
                 EVS_Bean ebean = (EVS_Bean) vAC.elementAt(i);
                 if (ebean.getNAME_VALUE_PAIR_IND() > 0)
                 {
                     ebean.setNAME_VALUE_PAIR_IND(-1);
                     vAC.setElementAt(ebean, i);
                 }
             }
         }
     }
 
     /**
      * To get the sorted result vector to display for selected sort type, called from CurationServlet. calls method
      * 'getSortedRows' for the selected component to get sorted bean. calls method 'getResult' for the selected
      * component to result vector from the sorted bean. final result vector is stored in the session vector "results".
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     public void getBlockSortedResult(HttpServletRequest req, HttpServletResponse res, String ACType)
     {
         try
         {
             HttpSession session = req.getSession();
             // get the sort string parameter
             String sSortType = (String) req.getParameter("blockSortType");
             if (sSortType != null)
                 DataManager.setAttribute(session, "blockSortType", sSortType);
             String sComponent = "";
             sComponent = (String) session.getAttribute("creSearchAC");
             String sUISearchType = (String) req.getParameter("UISearchType");
             req.setAttribute("UISearchType", sUISearchType);
             EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
             if ((!sComponent.equals("")) && (sComponent != null))
             {
                 Vector vResult = new Vector();
                 getEVSSortedRows(req, res);
                 evs.get_Result(req, res, vResult, "");
                 // String key = (String)req.getAttribute("labelKeyword");
                 // req.setAttribute("labelKeyword", key);
                 DataManager.setAttribute(session, "results", vResult);
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR - in GetACSearch-BlockSortedResult: " + e);
             logger.error("ERROR - GetACSearch-BlockSortedResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get the sorted vector for the selected field in the EVS component, called from getBlockSortedResult. gets the
      * 'blockSortType' from request and 'vACSearch' vector from session. calls 'getEVSFieldValue' to extract the value
      * from the bean for the selected sortType. modified bubble sort method to sort beans in the vector using the
      * strings compareToIgnoreCase method. adds the sorted bean to a vector 'vACSearch'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getEVSSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("blockSortType");
             if (sortField == null || sortField.equals(""))
                 sortField = (String) req.getParameter("sortType");
             else if (sortField == null)
                 sortField = (String) session.getAttribute("blockSortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     vSortedRows = this.sortConcepts(vSRows, sortField);
 /*                    // loop through the vector to get the bean row
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         EVS_Bean EVSSortBean1 = (EVS_Bean) vSRows.elementAt(i);
                         String Name1 = getEVSFieldValue(EVSSortBean1, sortField);
                         int tempInd = i;
                         EVS_Bean tempBean = EVSSortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             EVS_Bean EVSSortBean2 = (EVS_Bean) vSRows.elementAt(j);
                             String Name2 = getEVSFieldValue(EVSSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = EVSSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = EVSSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(EVSSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
 */                    if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-EVSsortedRows: " + e);
             logger.error("ERROR in GetACSearch-EVSsortedRows : " + e.toString(), e);
         }
     }
 
     public Vector<EVS_Bean> sortConcepts(Vector<EVS_Bean> vSRows, String sortField)
     {
         Vector<EVS_Bean> vSortedRows = new Vector<EVS_Bean>();
         try
         {
             // loop through the vector to get the bean row
             for (int i = 0; i < (vSRows.size()); i++)
             {
                 EVS_Bean EVSSortBean1 = (EVS_Bean) vSRows.elementAt(i);
                 String Name1 = getEVSFieldValue(EVSSortBean1, sortField);
                 int tempInd = i;
                 EVS_Bean tempBean = EVSSortBean1;
                 String tempName = Name1;
                 // loop through again to get the next bean in the vector
                 for (int j = i + 1; j < (vSRows.size()); j++)
                 {
                     EVS_Bean EVSSortBean2 = (EVS_Bean) vSRows.elementAt(j);
                     String Name2 = getEVSFieldValue(EVSSortBean2, sortField);
                     if (ComparedValue(sortField, Name1, Name2) > 0)
                     {
                         if (tempInd == i)
                         {
                             tempName = Name2;
                             tempBean = EVSSortBean2;
                             tempInd = j;
                         }
                         // else if (tempName.compareToIgnoreCase(Name2) > 0)
                         else if (ComparedValue(sortField, tempName, Name2) > 0)
                         {
                             tempName = Name2;
                             tempBean = EVSSortBean2;
                             tempInd = j;
                         }
                     }
                 }
                 vSRows.removeElementAt(tempInd);
                 vSRows.insertElementAt(EVSSortBean1, tempInd);
                 vSortedRows.addElement(tempBean);
             }
         }
         catch (Exception e)
         {
             logger.error("ERROR in GetACSearch-sortConcepts : " + e.toString(), e);
         }
         return vSortedRows;
     }
     
     /**
      * 
      * @param vSRows
      * @param menuAction
      * @param sortField
      */
     private void sortEVSStringValue(Vector vSRows, String menuAction, String sortField)
     {
         try
         {
             HttpSession session = m_classReq.getSession();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // check if the vector has the data
             if (vSRows.size() > 0)
             {
                 if (!menuAction.equals("searchForCreate"))
                 {
                     // get the checked rows and store it in the bean
                     for (int i = 0; i < vSRows.size(); i++)
                     {
                         EVS_Bean EVSBean = (EVS_Bean) vSRows.elementAt(i);
                         // check if the row is checked
                         String ckName = ("CK" + i);
                         String rSel = (String) m_classReq.getParameter(ckName);
                         if (rSel != null)
                             EVSBean.setCHECKED(true);
                         else
                             EVSBean.setCHECKED(false);
                     }
                 }
                 // loop through the vector to get the bean row
                 for (int i = 0; i < (vSRows.size()); i++)
                 {
                     isSorted = false;
                     EVS_Bean EVSSortBean1 = (EVS_Bean) vSRows.elementAt(i);
                     String Name1 = getEVSFieldValue(EVSSortBean1, sortField);
                     int tempInd = i;
                     EVS_Bean tempBean = EVSSortBean1;
                     String tempName = Name1;
                     // loop through again to get the next bean in the vector
                     for (int j = i + 1; j < (vSRows.size()); j++)
                     {
                         EVS_Bean EVSSortBean2 = (EVS_Bean) vSRows.elementAt(j);
                         String Name2 = getEVSFieldValue(EVSSortBean2, sortField);
                         if (ComparedValue(sortField, Name1, Name2) > 0)
                         {
                             if (tempInd == i)
                             {
                                 tempName = Name2;
                                 tempBean = EVSSortBean2;
                                 tempInd = j;
                             }
                             // else if (tempName.compareToIgnoreCase(Name2) > 0)
                             else if (ComparedValue(sortField, tempName, Name2) > 0)
                             {
                                 tempName = Name2;
                                 tempBean = EVSSortBean2;
                                 tempInd = j;
                             }
                         }
                     }
                     vSRows.removeElementAt(tempInd);
                     vSRows.insertElementAt(EVSSortBean1, tempInd);
                     vSortedRows.addElement(tempBean);
                 }
                 for (int i = 0; i < (vSortedRows.size()); i++)
                 {
                     EVS_Bean EVSSortBean1 = (EVS_Bean) vSortedRows.elementAt(i);
                 }
                 if (menuAction.equals("searchForCreate"))
                     DataManager.setAttribute(session, "vACSearch", vSortedRows);
                 else
                 {
                     DataManager.setAttribute(session, "vSelRows", vSortedRows);
                     DataManager.setAttribute(session, "sortType", sortField);
                     // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                     Vector vCheckList = new Vector();
                     for (int i = 0; i < (vSortedRows.size()); i++)
                     {
                         EVS_Bean EVSBean = (EVS_Bean) vSortedRows.elementAt(i);
                         if (EVSBean.getCHECKED())
                             vCheckList.addElement("CK" + i);
                     }
                     if (vCheckList != null)
                         DataManager.setAttribute(session, "CheckList", vCheckList);
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-sortEVSStringValue: " + e);
             logger.error("ERROR in GetACSearch-sortEVSStringValue : " + e.toString(), e);
         }
     }
 
     /**
      * 
      * @param vSRows
      * @param menuAction
      * @param sortField
      */
     private void sortEVSIntValue(Vector vSRows, String menuAction, String sortField)
     {
         try
         {
             HttpSession session = m_classReq.getSession();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // check if the vector has the data
             if (vSRows.size() > 0)
             {
                 // loop through the vector to get the bean row
                 for (int i = 0; i < (vSRows.size()); i++)
                 {
                     isSorted = false;
                     EVS_Bean EVSSortBean1 = (EVS_Bean) vSRows.elementAt(i);
                     String Name1 = getEVSFieldValue(EVSSortBean1, sortField);
                     int tempInd = i;
                     EVS_Bean tempBean = EVSSortBean1;
                     String tempName = Name1;
                     // loop through again to get the next bean in the vector
                     for (int j = i + 1; j < (vSRows.size()); j++)
                     {
                         EVS_Bean EVSSortBean2 = (EVS_Bean) vSRows.elementAt(j);
                         String Name2 = getEVSFieldValue(EVSSortBean2, sortField);
                         if (ComparedValue(sortField, Name1, Name2) > 0)
                         {
                             if (tempInd == i)
                             {
                                 tempName = Name2;
                                 tempBean = EVSSortBean2;
                                 tempInd = j;
                             }
                             // else if (tempName.compareToIgnoreCase(Name2) > 0)
                             else if (ComparedValue(sortField, tempName, Name2) > 0)
                             {
                                 tempName = Name2;
                                 tempBean = EVSSortBean2;
                                 tempInd = j;
                             }
                         }
                     }
                     vSRows.removeElementAt(tempInd);
                     vSRows.insertElementAt(EVSSortBean1, tempInd);
                     vSortedRows.addElement(tempBean);
                 }
                 for (int i = 0; i < (vSortedRows.size()); i++)
                 {
                     EVS_Bean EVSSortBean1 = (EVS_Bean) vSortedRows.elementAt(i);
                 }
                 if (menuAction.equals("searchForCreate"))
                     DataManager.setAttribute(session, "vACSearch", vSortedRows);
                 else
                 {
                     DataManager.setAttribute(session, "vSelRows", vSortedRows);
                     DataManager.setAttribute(session, "sortType", sortField);
                     // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                     Vector vCheckList = new Vector();
                     for (int i = 0; i < (vSortedRows.size()); i++)
                     {
                         EVS_Bean EVSBean = (EVS_Bean) vSortedRows.elementAt(i);
                         if (EVSBean.getCHECKED())
                             vCheckList.addElement("CK" + i);
                     }
                     if (vCheckList != null)
                         DataManager.setAttribute(session, "CheckList", vCheckList);
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-sortEVSIntValue: " + e);
             logger.error("ERROR in GetACSearch-sortEVSIntValue : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the bean for the selected field, called from EVSsortedRows.
      * 
      * @param curBean
      *            EVS bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String EVSField Value if selected field is found. otherwise empty string.
      */
     private String getEVSFieldValue(EVS_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("name") || curField.equals("conName"))
                 returnValue = curBean.getLONG_NAME();
             else if (curField.equals("umls") || curField.equals("Ident"))
                 returnValue = curBean.getCONCEPT_IDENTIFIER();
             else if (curField.equals("def"))
                 returnValue = curBean.getPREFERRED_DEFINITION();
             else if (curField.equals("source"))
                 returnValue = curBean.getEVS_DEF_SOURCE();
             else if (curField.equals("db") || curField.equals("database"))
                 returnValue = curBean.getEVS_DATABASE();
             else if (curField.equals("context"))
                 returnValue = curBean.getCONTEXT_NAME();
             else if (curField.equals("Level"))
             {
                 int rValue = curBean.getLEVEL();
                 returnValue = returnValue.valueOf(rValue);
             }
             else if (curField.equals("decUse"))
                 returnValue = getDECCount(curBean.getDEC_USING());
             else if (curField.equals("cadsrComp"))
                 returnValue = curBean.getcaDSR_COMPONENT();
             else if (curField.equals("comment"))
                 returnValue = curBean.getCOMMENTS();
             else if (curField.equals("publicID") || curField.equals("minID"))
                 returnValue = curBean.getID();
             else if (curField.equals("asl") || curField.equals("Status"))
                 returnValue = curBean.getASL_NAME();
             else if (curField.equals("version"))
                 returnValue = curBean.getVERSION();
             else if (curField.equals("semantic"))
                 returnValue = curBean.getEVS_SEMANTIC();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getEVSField: " + e);
             logger.error("ERROR in GetACSearch-getEVSField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the OC component, called from getBlockSortedResult. gets the
      * 'blockSortType' from request and 'vACSearch' vector from session. calls 'getOCFieldValue' to extract the value
      * from the bean for the selected sortType. modified bubble sort method to sort beans in the vector using the
      * strings compareToIgnoreCase method. adds the sorted bean to a vector 'vACSearch'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getOCSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("blockSortType");
             if (sortField == null)
                 sortField = (String) req.getParameter("sortType");
             else if (sortField == null)
                 sortField = (String) session.getAttribute("blockSortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             EVS_Bean OCBean = (EVS_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 OCBean.setCHECKED(true);
                             else
                                 OCBean.setCHECKED(false);
                         }
                     }
                     // loop through the vector to get the bean row
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         EVS_Bean OCSortBean1 = (EVS_Bean) vSRows.elementAt(i);
                         String Name1 = getOCFieldValue(OCSortBean1, sortField);
                         int tempInd = i;
                         EVS_Bean tempBean = OCSortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             EVS_Bean OCSortBean2 = (EVS_Bean) vSRows.elementAt(j);
                             String Name2 = getOCFieldValue(OCSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = OCSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = OCSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(OCSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     for (int i = 0; i < (vSortedRows.size()); i++)
                     {
                         EVS_Bean OCSortBean1 = (EVS_Bean) vSortedRows.elementAt(i);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             EVS_Bean OCBean = (EVS_Bean) vSortedRows.elementAt(i);
                             if (OCBean.getCHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-OCsortedRows: " + e);
             logger.error("ERROR in GetACSearch-OCsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the bean for the selected field, called from OCsortedRows.
      * 
      * @param curBean
      *            OC bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String OCField Value if selected field is found. otherwise empty string.
      */
     private String getOCFieldValue(EVS_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("name") || curField.equals("conName"))
                 returnValue = curBean.getLONG_NAME();
             else if (curField.equals("umls") || curField.equals("Ident"))
                 returnValue = curBean.getCONCEPT_IDENTIFIER();
             else if (curField.equals("def"))
                 returnValue = curBean.getPREFERRED_DEFINITION();
             else if (curField.equals("source"))
                 returnValue = curBean.getEVS_DEF_SOURCE();
             else if (curField.equals("db") || curField.equals("database"))
                 returnValue = curBean.getEVS_DATABASE();
             else if (curField.equals("context"))
                 returnValue = curBean.getCONTEXT_NAME();
             else if (curField.equals("minID") || curField.equals("publicID"))
                 returnValue = curBean.getID();
             else if (curField.equals("decUse"))
                 returnValue = getDECCount(curBean.getDEC_USING());
             else if (curField.equals("asl") || curField.equals("Status"))
                 returnValue = curBean.getASL_NAME();
             else if (curField.equals("version"))
                 returnValue = curBean.getVERSION();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getOCField: " + e);
             logger.error("ERROR in GetACSearch-getOCField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * to return dec count
      * 
      * @param sDECCon
      *            String
      * @return String
      * @throws Exception
      */
     private String getDECCount(String sDECCon) throws Exception
     {
         String sCount = "";
         if (sDECCon != null && !sDECCon.equals(""))
         {
             int begInd = 0;
             int endInd = 0;
             begInd = sDECCon.indexOf("<b>");
             if (begInd > 0)
                 endInd = sDECCon.indexOf("</b>", begInd);
             if (begInd > 0 && endInd > 0)
                 sCount = sDECCon.substring(begInd + 3, endInd);
             // System.out.println(sCount + " dec count " + sDECCon);
         }
         return sCount;
     }
 
     /**
      * To get the sorted vector for the selected field in the PC component, called from getBlockSortedResult. gets the
      * 'blockSortType' from request and 'vACSearch' vector from session. calls 'getPCFieldValue' to extract the value
      * from the bean for the selected sortType. modified bubble sort method to sort beans in the vector using the
      * strings compareToIgnoreCase method. adds the sorted bean to a vector 'vACSearch'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getPCSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("blockSortType");
             if (sortField == null)
                 sortField = (String) req.getParameter("sortType");
             else if (sortField == null)
                 sortField = (String) session.getAttribute("blockSortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             EVS_Bean PCBean = (EVS_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 PCBean.setCHECKED(true);
                             else
                                 PCBean.setCHECKED(false);
                         }
                     }
                     // loop through the vector to get the bean row
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         EVS_Bean PCSortBean1 = (EVS_Bean) vSRows.elementAt(i);
                         String Name1 = getPCFieldValue(PCSortBean1, sortField);
                         int tempInd = i;
                         EVS_Bean tempBean = PCSortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             EVS_Bean PCSortBean2 = (EVS_Bean) vSRows.elementAt(j);
                             String Name2 = getPCFieldValue(PCSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = PCSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = PCSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(PCSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     for (int i = 0; i < (vSortedRows.size()); i++)
                     {
                         EVS_Bean PCSortBean1 = (EVS_Bean) vSortedRows.elementAt(i);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             EVS_Bean PCBean = (EVS_Bean) vSortedRows.elementAt(i);
                             if (PCBean.getCHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-PCsortedRows: " + e);
             logger.error("ERROR in GetACSearch-PCsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the bean for the selected field, called from PCsortedRows.
      * 
      * @param curBean
      *            PC bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String PCField Value if selected field is found. otherwise empty string.
      */
     private String getPCFieldValue(EVS_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("name") || curField.equals("conName"))
                 returnValue = curBean.getLONG_NAME();
             else if (curField.equals("umls") || curField.equals("Ident"))
                 returnValue = curBean.getCONCEPT_IDENTIFIER();
             else if (curField.equals("def"))
                 returnValue = curBean.getPREFERRED_DEFINITION();
             else if (curField.equals("source"))
                 returnValue = curBean.getEVS_DEF_SOURCE();
             else if (curField.equals("db") || curField.equals("database"))
                 returnValue = curBean.getEVS_DATABASE();
             else if (curField.equals("context"))
                 returnValue = curBean.getCONTEXT_NAME();
             else if (curField.equals("minID") || curField.equals("publicID"))
                 returnValue = curBean.getID();
             else if (curField.equals("decUse"))
                 returnValue = getDECCount(curBean.getDEC_USING());
             else if (curField.equals("asl") || curField.equals("Status"))
                 returnValue = curBean.getASL_NAME();
             else if (curField.equals("version"))
                 returnValue = curBean.getVERSION();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getPCField: " + e);
             logger.error("ERROR in GetACSearch-getPCField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get final result vector of selected attributes/rows to display for Permissible Values component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the QuestBean vector 'vACSearch' and adds the selected fields to result
      * vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * 
      */
     public void getQuestionResult(HttpServletRequest req, HttpServletResponse res, Vector vResult)
     {
         Vector vQuestion = new Vector();
         try
         {
             HttpSession session = req.getSession();
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             Vector vSelAttr = new Vector();
             if (menuAction.equals("searchForCreate"))
                 vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
             else
                 vSelAttr = (Vector) session.getAttribute("selectedAttr");
             vQuestion = (Vector) session.getAttribute("vACSearch");
             Vector vRSel = (Vector) session.getAttribute("vSelRows");
             if (menuAction.equals("searchForCreate"))
                 vRSel = null;
             if (vRSel == null)
                 vRSel = vQuestion;
             Integer recs = new Integer(vRSel.size());
             String recs2 = recs.toString();
             String sKeyword = "";
             session.setAttribute("recsFound", recs2);
             Vector vSearchID = new Vector();
             Vector vSearchName = new Vector();
             Vector vSearchLongName = new Vector();
             Vector vSearchASL = new Vector();
             Quest_Bean QuestBean = new Quest_Bean();
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 QuestBean = new Quest_Bean();
                 QuestBean = (Quest_Bean) vRSel.elementAt(i);
                 // store the status in the vector
                 vSearchID.addElement(QuestBean.getSTATUS_INDICATOR());
                 vResult.addElement(QuestBean.getSTATUS_INDICATOR());
                 if (vSelAttr.contains("Question Text"))
                     vResult.addElement(QuestBean.getQUEST_NAME());
                 if (vSelAttr.contains("DE Long Name"))
                     vResult.addElement(QuestBean.getDE_LONG_NAME());
                 if (vSelAttr.contains("DE Public ID"))
                     vResult.addElement(QuestBean.getCDE_ID());
                 if (vSelAttr.contains("Question Public ID"))
                     vResult.addElement(QuestBean.getQC_ID());
                 if (vSelAttr.contains("Origin"))
                     vResult.addElement(QuestBean.getQUEST_ORIGIN());
                 if (vSelAttr.contains("Workflow Status"))
                     vResult.addElement(QuestBean.getASL_NAME());
                 if (vSelAttr.contains("Value Domain"))
                     vResult.addElement(QuestBean.getVD_LONG_NAME());
                 if (vSelAttr.contains("Context"))
                     vResult.addElement(QuestBean.getCONTEXT_NAME());
                 if (vSelAttr.contains("Protocol ID"))
                     vResult.addElement(QuestBean.getPROTOCOL_ID());
                 if (vSelAttr.contains("CRF Name"))
                     vResult.addElement(QuestBean.getCRF_NAME());
                 if (vSelAttr.contains("Highlight Indicator"))
                     vResult.addElement(QuestBean.getHIGH_LIGHT_INDICATOR());
             }
             if (QuestBean != null)
             {
                 // make keyWordLabel label request session
                 DataManager.setAttribute(session, "serKeyword", QuestBean.getCRF_NAME());
                 DataManager.setAttribute(session, "serProtoID", QuestBean.getPROTOCOL_ID());
                 String crfName = QuestBean.getCRF_NAME();
                 if (crfName == null)
                     crfName = "";
                 session.setAttribute("labelKeyword", "Draft New Questions in CRF: - " + crfName);
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
             DataManager.setAttribute(session, "SearchLongName", vSearchLongName);
             DataManager.setAttribute(session, "SearchASL", vSearchASL);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getQuestionResult: " + e);
             logger.error("ERROR in GetACSearch-getQuestionResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the bean for the selected field, called from QuestsortedRows.
      * 
      * @param curBean
      *            Quest bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String QuestField Value if selected field is found. otherwise empty string.
      */
     private String getQuestionFieldValue(Quest_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("QuestText"))
                 returnValue = curBean.getQUEST_NAME();
             else if (curField.equals("context"))
                 returnValue = curBean.getCONTEXT_NAME();
             else if (curField.equals("DEPublicID"))
                 returnValue = curBean.getCDE_ID();
             else if (curField.equals("minID"))
                 returnValue = curBean.getQC_ID();
             else if (curField.equals("Status"))
                 returnValue = curBean.getASL_NAME();
             else if (curField.equals("Origin"))
                 returnValue = curBean.getQUEST_ORIGIN();
             else if (curField.equals("ProtoID"))
                 returnValue = curBean.getPROTOCOL_ID();
             else if (curField.equals("CRFName"))
                 returnValue = curBean.getCRF_NAME();
             else if (curField.equals("DELongName"))
                 returnValue = curBean.getDE_LONG_NAME();
             else if (curField.equals("HighLight"))
                 returnValue = curBean.getHIGH_LIGHT_INDICATOR();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getQuestField: " + e);
             logger.error("ERROR in GetACSearch-getQuestField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the Quest component, called from getACSortedResult. gets the
      * 'sortType' from request and 'vSelRows' vector from session. calls 'getQuestionFieldValue' to extract the value
      * from the bean for the selected sortType. modified bubble sort method to sort beans in the vector using the
      * strings compareToIgnoreCase method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getQuestionSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             if (menuAction.equals("searchForCreate"))
                 vSRows = (Vector) session.getAttribute("vACSearch");
             else
                 vSRows = (Vector) session.getAttribute("vSelRows");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     if (!menuAction.equals("searchForCreate"))
                     {
                         // get the checked rows and store it in the bean
                         for (int i = 0; i < vSRows.size(); i++)
                         {
                             Quest_Bean QuestBean = (Quest_Bean) vSRows.elementAt(i);
                             // check if the row is checked
                             String ckName = ("CK" + i);
                             String rSel = (String) req.getParameter(ckName);
                             if (rSel != null)
                                 QuestBean.setQUEST_CHECKED(true);
                             else
                                 QuestBean.setQUEST_CHECKED(false);
                         }
                     }
                     // loop through the vector to get the bean row
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         Quest_Bean QuestSortBean1 = (Quest_Bean) vSRows.elementAt(i);
                         String Name1 = getQuestionFieldValue(QuestSortBean1, sortField);
                         int tempInd = i;
                         Quest_Bean tempBean = QuestSortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             Quest_Bean QuestSortBean2 = (Quest_Bean) vSRows.elementAt(j);
                             String Name2 = getQuestionFieldValue(QuestSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = QuestSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = QuestSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(QuestSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     if (menuAction.equals("searchForCreate"))
                         DataManager.setAttribute(session, "vACSearch", vSortedRows);
                     else
                     {
                         DataManager.setAttribute(session, "vSelRows", vSortedRows);
                         DataManager.setAttribute(session, "sortType", sortField);
                         // get the checked beans and add the checked names to the vector to keep the checkbox checked.
                         Vector vCheckList = new Vector();
                         for (int i = 0; i < (vSortedRows.size()); i++)
                         {
                             Quest_Bean QuestBean = (Quest_Bean) vSortedRows.elementAt(i);
                             if (QuestBean.getQUEST_CHECKED())
                                 vCheckList.addElement("CK" + i);
                         }
                         if (vCheckList != null && vCheckList.size() > 0)
                             DataManager.setAttribute(session, "CheckList", vCheckList);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-QuestionsortedRows: " + e);
             logger.error("ERROR in GetACSearch-QuestionsortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To get the Question Value result vector to display for the user, called from CurationServlet. calls method
      * 'doQuestValueSearch' to search the questions. calls method 'setAttributeValues' to get the selected attributes.
      * calls method 'getQuestValueSortedRows' for the selected component to get sorted bean. calls method
      * 'getQuestValueResult' for the selected component to result vector from the sorted bean. final result vector is
      * stored in the session vector "results".
      * 
      */
     public void getACQuestionValue(VD_Bean vd) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         Vector vResult = new Vector();
         // get the crf id and question id from the selected quest bean
         Quest_Bean QuestBean = (Quest_Bean) session.getAttribute("m_Quest");
         String sQuestId = QuestBean.getQC_IDSEQ();
         String sCRFId = QuestBean.getCRF_IDSEQ();
         // call to search questions and save the result in the questValue session vector
         doQuestValueSearch(sQuestId, sCRFId, vResult, vd);
     }
 
     /**
      * To get resultSet from database for Question Values called from getACQuestionValue methods.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.SEARCH_DE(DE_IDSEQ, InString, ContID, ContName,
      * ASLName, CDE_ID, OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to Question bean which is added to the vector to return
      * 
      * @param vList
      *            returns Vector of DEbean.
      * 
      */
     private void doQuestValueSearch(String sQuestID, String sCRFID, Vector vList, VD_Bean vd)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         try
         {
             HttpSession session = m_classReq.getSession();
             // VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
             Vector<PV_Bean> pvList = vd.getVD_PV_List();
             // Vector pvList = (Vector)session.getAttribute("VDPVList");
             Vector vvList = new Vector();
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT_CDE_CURATOR_PKG.SEARCH_CRFValue(?,?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(4, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, sQuestID);
                 cstmt.setString(2, "");
                 cstmt.setString(3, "");
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(4);
                 String s;
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         Quest_Value_Bean QuestValueBean = new Quest_Value_Bean();
                         QuestValueBean.setQUESTION_VALUE_IDSEQ(rs.getString(1));
                         QuestValueBean.setQUESTION_VALUE(rs.getString(2));
                         QuestValueBean.setVP_IDSEQ(rs.getString(3));
                         vList.addElement(QuestValueBean);
                         if (pvList == null)
                             pvList = new Vector();
                         // match the vp id with the VD's PV if valid values allready been matched.
                         boolean isMatched = false;
                         for (int i = 0; i < pvList.size(); i++)
                         {
                             PV_Bean pvBean = (PV_Bean) pvList.elementAt(i);
                             if (pvBean != null && pvBean.getPV_VDPVS_IDSEQ() != null)
                             {
                                 if (pvBean.getPV_VDPVS_IDSEQ().equals(QuestValueBean.getVP_IDSEQ()))
                                 {
                                     pvBean.setQUESTION_VALUE_IDSEQ(rs.getString(1));
                                     pvBean.setQUESTION_VALUE(rs.getString(2));
                                     pvList.setElementAt(pvBean, i);
                                     isMatched = true;
                                     break;
                                 }
                             }
                         }
                         // add as a new item to the pv bean if not matched
                         if (!isMatched)
                         {
                             vvList.addElement(rs.getString(2));
                         }
                     }
                 }
                 if (pvList != null)
                 {
                     // DataManager.setAttribute(session, "VDPVList", pvList);
                     vd.setVD_PV_List(pvList);
                     DataManager.setAttribute(session, "m_VD", vd);
                 }
             }
             DataManager.setAttribute(session, "vQuestValue", vList);
             DataManager.setAttribute(session, "NonMatchVV", vvList);
         }
         catch (Exception e)
         {
            logger.error("ERROR - GetACSearch-QuestionValueSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
     }
 
     /**
      * To get final result vector of selected attributes/rows to display for Permissible Values component, called from
      * getACKeywordResult, getACSortedResult and getACShowResult methods. gets the selected attributes from session
      * vector 'selectedAttr'. loops through the QuestBean vector 'vACSearch' and adds the selected fields to result
      * vector.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * @param vResult
      *            output result vector.
      * 
      */
     private void getQuestValueResult(HttpServletRequest req, HttpServletResponse res, Vector vResult)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vQuestValue = (Vector) session.getAttribute("vQuestValue");
             Integer recs = new Integer(vQuestValue.size());
             String recs2 = recs.toString();
             DataManager.setAttribute(session, "creKeyword", "");
             // make keyWordLabel label request session
             Quest_Bean QuestBean = (Quest_Bean) session.getAttribute("m_Quest");
             String sQuestion = QuestBean.getQUEST_NAME();
             session.setAttribute("labelKeyword", "CRF Values - " + sQuestion);
             Vector vSearchID = new Vector();
             Vector vSearchName = new Vector();
             for (int i = 0; i < (vQuestValue.size()); i++)
             {
                 Quest_Value_Bean QuestValueBean = new Quest_Value_Bean();
                 QuestValueBean = (Quest_Value_Bean) vQuestValue.elementAt(i);
                 vSearchID.addElement(QuestValueBean.getQUESTION_VALUE_IDSEQ());
                 vSearchName.addElement(QuestValueBean.getQUESTION_VALUE());
                 vResult.addElement(QuestValueBean.getQUESTION_VALUE());
                 vResult.addElement(QuestValueBean.getPERMISSIBLE_VALUE());
                 vResult.addElement(QuestValueBean.getVALUE_MEANING());
             }
             DataManager.setAttribute(session, "SearchID", vSearchID);
             DataManager.setAttribute(session, "SearchName", vSearchName);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getQuestValueResult: " + e);
             logger.error("ERROR in GetACSearch-getQuestValueResult : " + e.toString(), e);
         }
     }
 
     /**
      * To get the value from the bean for the selected field, called from QuestValuesortedRows.
      * 
      * @param curBean
      *            Quest bean.
      * @param curField
      *            sort Type field name.
      * 
      * @return String QuestValueField Value if selected field is found. otherwise empty string.
      */
     private String getQuestValueFieldValue(Quest_Value_Bean curBean, String curField)
     {
         String returnValue = "";
         try
         {
             if (curField.equals("crfValue"))
                 returnValue = curBean.getQUESTION_VALUE();
             else if (curField.equals("value"))
                 returnValue = curBean.getPERMISSIBLE_VALUE();
             else if (curField.equals("meaning"))
                 returnValue = curBean.getVALUE_MEANING();
             if (returnValue == null)
                 returnValue = "";
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getQuestValueField: " + e);
             logger.error("ERROR in GetACSearch-getQuestValueField : " + e.toString(), e);
         }
         return returnValue;
     }
 
     /**
      * To get the sorted vector for the selected field in the QuestValue component, called from getACSortedResult. gets
      * the 'sortType' from request and 'vSelRows' vector from session. calls 'getQuestValueFieldValue' to extract the
      * value from the bean for the selected sortType. modified bubble sort method to sort beans in the vector using the
      * strings compareToIgnoreCase method. adds the sorted bean to a vector 'vSelRows'.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      */
     private void getQuestValueSortedRows(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vSRows = new Vector();
             Vector vSortedRows = new Vector();
             boolean isSorted = false;
             // get the selected rows
             String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             vSRows = (Vector) session.getAttribute("vQuestValue");
             String sortField = (String) req.getParameter("sortType");
             if (sortField == null)
                 sortField = (String) session.getAttribute("sortType");
             if (sortField != null)
             {
                 // check if the vector has the data
                 if (vSRows.size() > 0)
                 {
                     // loop through the vector to get the bean row
                     for (int i = 0; i < (vSRows.size()); i++)
                     {
                         isSorted = false;
                         Quest_Value_Bean QuestValueSortBean1 = (Quest_Value_Bean) vSRows.elementAt(i);
                         String Name1 = getQuestValueFieldValue(QuestValueSortBean1, sortField);
                         int tempInd = i;
                         Quest_Value_Bean tempBean = QuestValueSortBean1;
                         String tempName = Name1;
                         // loop through again to get the next bean in the vector
                         for (int j = i + 1; j < (vSRows.size()); j++)
                         {
                             Quest_Value_Bean QuestValueSortBean2 = (Quest_Value_Bean) vSRows.elementAt(j);
                             String Name2 = getQuestValueFieldValue(QuestValueSortBean2, sortField);
                             if (ComparedValue(sortField, Name1, Name2) > 0)
                             {
                                 if (tempInd == i)
                                 {
                                     tempName = Name2;
                                     tempBean = QuestValueSortBean2;
                                     tempInd = j;
                                 }
                                 // else if (tempName.compareToIgnoreCase(Name2) > 0)
                                 else if (ComparedValue(sortField, tempName, Name2) > 0)
                                 {
                                     tempName = Name2;
                                     tempBean = QuestValueSortBean2;
                                     tempInd = j;
                                 }
                             }
                         }
                         vSRows.removeElementAt(tempInd);
                         vSRows.insertElementAt(QuestValueSortBean1, tempInd);
                         vSortedRows.addElement(tempBean);
                     }
                     for (int i = 0; i < (vSortedRows.size()); i++)
                     {
                         Quest_Value_Bean QuestValueSortBean1 = (Quest_Value_Bean) vSortedRows.elementAt(i);
                     }
                     DataManager.setAttribute(session, "vQuestValue", vSortedRows);
                 }
             }
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-QuestValuesortedRows: " + e);
             logger.error("ERROR in GetACSearch-QuestValuesortedRows : " + e.toString(), e);
         }
     }
 
     /**
      * To search for definitions. Called from 'service' method where reqType is 'searchEVS' forwards page
      * 'EVSSearchPage.jsp'.
      * 
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * 
      * @throws Exception
      */
     public void doSearchEVS(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         try
         {
             HttpSession session = req.getSession();
             Vector vAC = new Vector();
             Vector vResult = new Vector();
             String termStr = req.getParameter("tfSearchTerm");
             // get the search in data
             String dtsVocab = req.getParameter("listContextFilterVocab");
             if (dtsVocab == null)
                 dtsVocab = "";
             // DataManager.setAttribute(session, "dtsVocab", dtsVocab);
             String sSearchInEVS = (String) req.getParameter("listSearchInEVS");
             if (sSearchInEVS == null)
                 sSearchInEVS = "Synonym";
             // req.setAttribute("SearchInEVS", sSearchInEVS);
             String sMetaSource = (String) req.getParameter("listContextFilterSource");
             if (sMetaSource == null)
                 sMetaSource = "All Sources";
             // req.setAttribute("MetaSource", sMetaSource);
             String sRetired = (String) req.getParameter("rRetired");
             if (sRetired == null)
                 sRetired = "Exclude";
             String sUISearchType = (String) req.getAttribute("UISearchType");
             if (sUISearchType == null || sUISearchType.equals("nothing"))
                 sUISearchType = "term";
             String sMetaLimit = (String) req.getParameter("listMetaLimit");
             int intMetaLimit = 0;
             if (sMetaLimit != null)
             {
                 Integer iMetaLimit = new Integer(sMetaLimit);
                 intMetaLimit = iMetaLimit.intValue();
                 if (intMetaLimit < 100)
                     intMetaLimit = 100;
             }
             else
                 intMetaLimit = 100;
             String sConteIdseq = (String) req.getParameter("sConteIdseq");
             if (sConteIdseq == null)
                 sConteIdseq = "";
             DataManager.setAttribute(session, "creKeyword", termStr);
             // call the method to search
             EVSSearch evs = new EVSSearch(req, res, m_servlet);
             // evs.do_EVSSearch(termStr, vAC, dtsVocab, sSearchInEVS, sMetaSource,
             // intMetaLimit, sUISearchType, sRetired, sConteIdseq, -1);
             vAC = evs.doVocabSearch(vAC, termStr, dtsVocab, sSearchInEVS, "", "", sRetired, sMetaSource, intMetaLimit,
                             true, -1, "");
             DataManager.setAttribute(session, "vACSearch", vAC);
             evs.get_Result(req, res, vResult, "DEF");
             DataManager.setAttribute(session, "EVSresults", vResult);
             session.setAttribute("labelKeyword", termStr);
             Integer recs = new Integer(vAC.size());
             String recs2 = recs.toString();
             String sKeyword = "";
             session.setAttribute("creRecsFound", recs2);
         }
         catch (Exception e)
         {
             logger.error("doSearchEVS :EVS Definition Search : " + e);
         }
     }
 
     /**
      * To get the alternate names for the selected AC from the database.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.GET_ALTERNATE_NAMES(AC_IDSEQ, detl_name,
      * OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to ALT_NAME bean
      * 
      * @param acIdseq
      *            String id of the selected ac.
      * @param detlName
      *            String the detl type of the selected AC.
      * 
      */
     public Vector doAltNameSearch(String acIdseq, String detlName, String CD_ID, String sOrigin, String sFor) 
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         Vector vList = new Vector();
         HttpSession session = m_classReq.getSession();
         try
         {
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT_CDE_CURATOR_PKG.GET_ALTERNATE_NAMES(?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(3, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 if (CD_ID == null || CD_ID.equals(""))
                     cstmt.setString(1, acIdseq);
                 else
                     cstmt.setString(1, CD_ID);
                 cstmt.setString(2, detlName);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(3);
                 String s;
                 // get the current session list of ref doc to append to all DEs
                 Vector vAllAltName = new Vector();
                 if (!sFor.equals("Version"))
                     vAllAltName = (Vector) session.getAttribute("AllAltNameList");
                 if (vAllAltName == null)
                     vAllAltName = new Vector();
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         if (sOrigin.equals("EditDesDE") && rs.getString("detl_name").equals("USED_BY"))
                             continue;
                         ALT_NAME_Bean AltNameBean = new ALT_NAME_Bean();
                         AltNameBean.setALT_NAME_IDSEQ(rs.getString("desig_idseq"));
                         AltNameBean.setCONTE_IDSEQ(rs.getString("conte_idseq"));
                         AltNameBean.setCONTEXT_NAME(rs.getString("context_name"));
                         AltNameBean.setALTERNATE_NAME(rs.getString("name"));
                         AltNameBean.setALT_TYPE_NAME(rs.getString("detl_name"));
                         AltNameBean.setAC_LONG_NAME(rs.getString("ac_long_name"));
                         AltNameBean.setAC_IDSEQ(acIdseq);
                         AltNameBean.setAC_LANGUAGE(rs.getString("lae_name"));
                         AltNameBean.setALT_SUBMIT_ACTION("UPD");
                         vList.addElement(AltNameBean);
                         if (!sFor.equals("Version"))
                             vAllAltName.addElement(AltNameBean);
                     }
                 }
                 if (!sFor.equals("Version"))
                     DataManager.setAttribute(session, "AllAltNameList", vAllAltName);
             }
             m_classReq.setAttribute("AltNameList", vList);
             m_classReq.setAttribute("itemType", detlName);
         }
         catch (Exception e)
         {
           logger.error("ERROR - GetACSearch-doAltNameSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return vList;
     }
 
     /**
      * To get the reference documents for the selected AC and type from the database.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.GET_REFERENCE_DOCUMENTS(AC_IDSEQ, DCTL_NAME,
      * OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to REF_DOC bean
      * 
      * @param acIdseq
      *            String id of the selected ac.
      * @param docType
      *            String the type of reference document.
      * 
      */
     public Vector doRefDocSearch(String acIdseq, String docType, String sFor)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         Vector vList = new Vector();
         HttpSession session = m_classReq.getSession();
         try
         {
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT_CDE_CURATOR_PKG.GET_REFERENCE_DOCUMENTS(?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(3, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, acIdseq);
                 // make it empty if all types
                 if (docType == null || docType.equals("ALL TYPES"))
                     docType = "";
                 cstmt.setString(2, docType);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(3);
                 String s;
                 // get the current session list of ref doc to append to all DEs
                 Vector vAllRefDoc = new Vector();
                 if (!sFor.equals("Version"))
                     vAllRefDoc = (Vector) session.getAttribute("AllRefDocList");
                 if (vAllRefDoc == null)
                     vAllRefDoc = new Vector();
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         REF_DOC_Bean RefDocBean = new REF_DOC_Bean();
                         RefDocBean.setAC_IDSEQ(rs.getString("ac_idseq"));
                         RefDocBean.setAC_LONG_NAME(rs.getString("ac_long_name"));
                         RefDocBean.setREF_DOC_IDSEQ(rs.getString("rd_idseq"));
                         RefDocBean.setDOCUMENT_NAME(rs.getString("rd_name"));
                         RefDocBean.setDOC_TYPE_NAME(rs.getString("rd_dctl_name"));
                         String sValue = rs.getString("rd_doc_text");
                         if (sValue == null)
                             sValue = "";
                         RefDocBean.setDOCUMENT_TEXT(sValue);
                         sValue = rs.getString("url");
                         if (sValue == null)
                             sValue = "";
                         RefDocBean.setDOCUMENT_URL(sValue);
                         RefDocBean.setCONTE_IDSEQ(rs.getString("conte_idseq"));
                         RefDocBean.setCONTEXT_NAME(rs.getString("context_name"));
                         RefDocBean.setAC_LANGUAGE(rs.getString("rd_lae_name"));
                         RefDocBean.setREF_SUBMIT_ACTION("UPD");
                         // RefDocBean.getREF_DOC_IDSEQ());
                         vList.addElement(RefDocBean);
                         if (!sFor.equals("Version"))
                             vAllRefDoc.addElement(RefDocBean);
                     }
                 }
                 if (!sFor.equals("Version"))
                     DataManager.setAttribute(session, "AllRefDocList", vAllRefDoc);
             }
             m_classReq.setAttribute("RefDocList", vList);
             m_classReq.setAttribute("itemType", docType);
         }
         catch (Exception e)
         {
             logger.error("ERROR - GetACSearch-doRefDocSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return vList;
     }
 
     /**
      * ac_version method copies all other attributes from old ac to versioned ac, but some attributes are changed on the
      * page. This method compares the existing pvs and updates its attributes and mark it to update/remove
      * 
      * @param newBean
      *            pv_bean class
      * @param oldVDPV
      *            old vdpv vector from session
      * @return pv_bean updated with changes attributes from the page
      */
     private PV_Bean updatePVBean(PV_Bean newBean, Vector oldVDPV)
     {
         try
         {
             Vector vValue = (Vector) m_classReq.getAttribute("vValue");
             if (vValue == null)
                 vValue = new Vector();
             for (int i = 0; i < oldVDPV.size(); i++)
             {
                 PV_Bean oldPV = (PV_Bean) oldVDPV.elementAt(i);
                 String oldValue = oldPV.getPV_VALUE();
                 String oldMean = oldPV.getPV_SHORT_MEANING();
                 String newValue = newBean.getPV_VALUE();
                 String newMean = newBean.getPV_SHORT_MEANING();
                 if (oldValue.equals(newValue) && oldMean.equals(newMean))
                 {
                     newBean.setPV_VALUE_ORIGIN(oldPV.getPV_VALUE_ORIGIN());
                     newBean.setPV_BEGIN_DATE(oldPV.getPV_BEGIN_DATE());
                     newBean.setPV_END_DATE(oldPV.getPV_END_DATE());
                     if (oldPV.getVP_SUBMIT_ACTION().equalsIgnoreCase("NONE"))
                         newBean.setVP_SUBMIT_ACTION("UPD");
                     else
                         newBean.setVP_SUBMIT_ACTION(oldPV.getVP_SUBMIT_ACTION());
                     // newBean.setVM_CONCEPT(oldPV.getVM_CONCEPT());
                     newBean.setPV_VM(oldPV.getPV_VM());
                     newBean.setPV_VM_CONDR_IDSEQ(oldPV.getPV_VM_CONDR_IDSEQ());
                     newBean.setPARENT_CONCEPT(oldPV.getPARENT_CONCEPT());
                     vValue.addElement(newValue);
                     break;
                 }
             }
             m_classReq.setAttribute("vValue", vValue);
         }
         catch (Exception e)
         {
            logger.error("ERROR - GetACSearch-updatePVBean: " + e.toString(), e);
         }
         return newBean;
     }
 
     /**
      * To get the Protocol CRFs for the selected AC from the database.
      * 
      * calls oracle stored procedure "{call SBREXT_CDE_CURATOR_PKG.GET_PROTO_CRF(AC_IDSEQ, OracleTypes.CURSOR)}"
      * 
      * loop through the ResultSet and add them to Quest bean
      * 
      * @param acIdseq
      *            String id of the selected ac.
      * @param acName
      *            String AC name.
      * 
      * @return Integer of Proto CRF Count
      */
     public Integer doProtoCRFSearch(String acIdseq, String acName)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         Vector vList = new Vector();
         HttpSession session = m_classReq.getSession();
         Integer qCount = new Integer(0);
         try
         {
             Quest_Bean QuestBean = new Quest_Bean();
            if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT_CDE_CURATOR_PKG.GET_PROTOCOL_CRF(?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(2, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, acIdseq);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(2);
                 String s;
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         QuestBean = new Quest_Bean();
                         QuestBean.setPROTOCOL_ID(rs.getString("PROTOCOL_ID"));
                         QuestBean.setPROTOCOL_NAME(rs.getString("PROTOCOL_NAME"));
                         QuestBean.setCRF_NAME(rs.getString("crf"));
                         vList.addElement(QuestBean);
                     }
                 }
             }
             String QuestValue = "";
             if (vList != null && vList.size() > 0)
             {
                 QuestBean = (Quest_Bean) vList.elementAt(0);
                 // QuestValue = QuestBean.getQuest_VALUE();
                 qCount = new Integer(vList.size());
             }
             m_classReq.setAttribute("ProtoCRFList", vList);
             m_classReq.setAttribute("ACName", acName);
         }
         catch (Exception e)
         {
            logger.error("ERROR - GetACSearch-doProtoCRFSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return qCount;
     }
 
     /**
      * To get the Context name for the CD assoicated with VD or DEC. Uses the session attriubtes of CD list and get the
      * context name.
      * 
      * @param sCDid
      *            selected Administered component.
      * 
      */
     private String getCDContext(String sCDid) throws Exception
     {
         String cdName = "";
         HttpSession session = m_classReq.getSession();
         Vector vCDNames = (Vector) session.getAttribute("vCD");
         Vector vCDID = (Vector) session.getAttribute("vCD_ID");
         if (vCDID != null)
         {
             cdName = (String) vCDNames.elementAt(vCDID.indexOf(sCDid));
             if (cdName == null)
                 cdName = "";
         }
         return cdName;
     }
 
     /**
      * calls SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_AC_CONTACT api to create contacts
      * 
      * @param ac_idseq
      * @param acc_idseq
      * @return hashtable of contact beans
      */
     private Hashtable getAC_Contacts(String ac_idseq, String acc_idseq)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         Hashtable vList = new Hashtable();
         try
         {
             HttpSession session = (HttpSession) m_classReq.getSession();
             // get session attributes
             Hashtable hOrg = (Hashtable) session.getAttribute("Organizations");
             if (hOrg == null)
                 hOrg = new Hashtable();
             Hashtable hPer = (Hashtable) session.getAttribute("Persons");
             if (hPer == null)
                 hPer = new Hashtable();
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_AC_CONTACT(?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(3, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, ac_idseq);
                 cstmt.setString(2, acc_idseq);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(3);
                 String s;
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         AC_CONTACT_Bean accBean = new AC_CONTACT_Bean();
                         String accID = rs.getString("acc_idseq");
                         accBean.setAC_CONTACT_IDSEQ(accID);
                         String sOrgID = rs.getString("org_idseq");
                         accBean.setORG_IDSEQ(sOrgID);
                         String sPerID = rs.getString("per_idseq");
                         accBean.setPERSON_IDSEQ(sPerID);
                         accBean.setAC_IDSEQ(rs.getString("ac_idseq"));
                         accBean.setRANK_ORDER(rs.getString("rank_order"));
                         accBean.setCS_CSI_IDSEQ(rs.getString("cs_csi_idseq"));
                         accBean.setCS_IDSEQ(rs.getString("ac_idseq"));//--rs.getString("csi_idseq")removed the column uses ac_idseq
                         accBean.setCONTACT_ROLE(rs.getString("contact_role"));
                         accBean.setACC_SUBMIT_ACTION("NONE");
                         // get org and per name from the list
                         if (sOrgID != null && !sOrgID.equals("") && hOrg.containsKey(sOrgID))
                             accBean.setORG_NAME((String) hOrg.get(sOrgID));
                         if (sPerID != null && !sPerID.equals("") && hPer.containsKey(sPerID))
                             accBean.setPERSON_NAME((String) hPer.get(sPerID));
                         String contName = accBean.getPERSON_NAME();
                         if (contName == null || contName.equals(""))
                             contName = accBean.getORG_NAME();
                         // get comm and addr information
                         Vector vComm = getContactComm(null, sOrgID, sPerID);
                         accBean.setACC_COMM_List(vComm);
                         Vector vAddr = getContactAddr(null, sOrgID, sPerID);
                         accBean.setACC_ADDR_List(vAddr);
                         if (contName != null)
                          vList.put(contName, accBean);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             logger.error("ERROR - GetACSearch-searchACContact for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return vList;
     }
 
     /**
      * calls SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_CONTACT_COMM api to create communication
      * 
      * @param comm_idseq
      *            string comm idseq
      * @param org_idseq
      *            String org idseq
      * @param per_idseq
      *            String per idseq
      * @return Vector of comm bean
      */
     public Vector getContactComm(String comm_idseq, String org_idseq, String per_idseq)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         Vector vList = new Vector();
         try
         {
            HttpSession session = (HttpSession) m_classReq.getSession();
            if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_CONTACT_COMM(?,?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(4, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, org_idseq);
                 cstmt.setString(2, per_idseq);
                 cstmt.setString(3, comm_idseq);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(4);
                 String s;
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         AC_COMM_Bean commBean = new AC_COMM_Bean();
                         commBean.setAC_COMM_IDSEQ(rs.getString("ccomm_idseq"));
                         commBean.setORG_IDSEQ(rs.getString("org_idseq"));
                         commBean.setPERSON_IDSEQ(rs.getString("per_idseq"));
                         commBean.setCTL_NAME(rs.getString("ctl_name"));
                         commBean.setRANK_ORDER(rs.getString("rank_order"));
                         commBean.setCYBER_ADDR(rs.getString("cyber_address"));
                         commBean.setCOMM_SUBMIT_ACTION("NONE");
                         vList.addElement(commBean);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             logger.error("ERROR - GetACSearch-getContactComm for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }  
         return vList;
     }
 
     /**
      * calls SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_CONTACT_ADDR api to create adddress
      * 
      * @param addr_idseq
      *            String addr idseq
      * @param org_idseq
      *            String org idseq
      * @param per_idseq
      *            string per idseq
      * @return vector of addr bean
      */
     public Vector getContactAddr(String addr_idseq, String org_idseq, String per_idseq)
     {
         ResultSet rs = null;
         CallableStatement cstmt = null;
         Vector vList = new Vector();
         try
         {
              HttpSession session = (HttpSession) m_classReq.getSession();
            if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
             else
             {
                 cstmt = m_servlet.getConn().prepareCall("{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_CONTACT_ADDR(?,?,?,?)}");
                 // Now tie the placeholders for out parameters.
                 cstmt.registerOutParameter(4, OracleTypes.CURSOR);
                 // Now tie the placeholders for In parameters.
                 cstmt.setString(1, org_idseq);
                 cstmt.setString(2, per_idseq);
                 cstmt.setString(3, addr_idseq);
                 // Now we are ready to call the stored procedure
                 boolean bExcuteOk = cstmt.execute();
                 // store the output in the resultset
                 rs = (ResultSet) cstmt.getObject(4);
                 String s;
                 if (rs != null)
                 {
                     // loop through the resultSet and add them to the bean
                     while (rs.next())
                     {
                         AC_ADDR_Bean addrBean = new AC_ADDR_Bean();
                         addrBean.setAC_ADDR_IDSEQ(rs.getString("caddr_idseq"));
                         addrBean.setORG_IDSEQ(rs.getString("org_idseq"));
                         addrBean.setPERSON_IDSEQ(rs.getString("per_idseq"));
                         addrBean.setATL_NAME(rs.getString("atl_name"));
                         addrBean.setRANK_ORDER(rs.getString("rank_order"));
                         addrBean.setADDR_LINE1(rs.getString("addr_line1"));
                         addrBean.setADDR_LINE2(rs.getString("addr_line2"));
                         addrBean.setCITY(rs.getString("city"));
                         addrBean.setSTATE_PROV(rs.getString("state_prov"));
                         addrBean.setPOSTAL_CODE(rs.getString("postal_code"));
                         addrBean.setCOUNTRY(rs.getString("country"));
                         addrBean.setADDR_SUBMIT_ACTION("NONE");
                         vList.addElement(addrBean);
                     }
                 }
             }
         }
         catch (Exception e)
         {
             logger.error("ERROR - GetACSearch-getContactAddr for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             cstmt = SQLHelper.closeCallableStatement(cstmt);
         }
         return vList;
     }
 
     /**
      * To get checked row bean(single or multiple), called from NCICuration Servlet. loops through the vector 'vSelRows'
      * and gets bean of the checked row of a selected component. checks the edit permission if action is not a new from
      * template. gets many to many related components if any. calls 'getACListForEdit' to load the list for the selected
      * context. stores resulted bean in session ('m_DE') for th selected component.
      * 
      * @param req
      *            The HttpServletRequest object.
      * @param res
      *            HttpServletResponse object.
      * 
      * @return boolean false if error exists
      */
     public boolean getSelRowToUploadRefDoc(HttpServletRequest req, HttpServletResponse res, String sAction)
     {
         HttpSession session = req.getSession();
         Vector vBEResult = new Vector();
         // doRefDocSearch
         try
         {
             String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
             DataManager.setAttribute(session, "AllAltNameList", new Vector());
             DataManager.setAttribute(session, "AllRefDocList", new Vector());
             String sSearchAC = (String) session.getAttribute("searchAC");
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
             Vector vCheckList = new Vector();
             int selectedRowID = -1;
             String unCheckedRowId = (String) m_classReq.getParameter("unCheckedRowId");
             if (unCheckedRowId != null && !(unCheckedRowId == "")){
                 selectedRowID = new Integer(unCheckedRowId);
            }
             if (selectedRowID>=0)
             {
                 // reset the bean with selected row for the selected component
                 String ckName = ("CK" + selectedRowID);            
                 DataManager.setAttribute(session, "ckName", ckName);
                 //get the checked bean and store it in the session
                 if (sSearchAC.equals("DataElement"))
                 {
                     DE_Bean DEBean = new DE_Bean();
                     DEBean = DEBean.cloneDE_Bean((DE_Bean) vSRows.elementAt(selectedRowID), "Complete");
                     DataManager.setAttribute(session, "m_DE", DEBean);
                 }
                 else if (sSearchAC.equals("DataElementConcept"))
                 {
                     DEC_Bean DECBean = new DEC_Bean();
                     DECBean = DECBean.cloneDEC_Bean((DEC_Bean) vSRows.elementAt(selectedRowID));
                     DataManager.setAttribute(session, "m_DEC", DECBean);
                 }
                 else if (sSearchAC.equals("ValueDomain"))
                 {
                     VD_Bean VDBean = new VD_Bean();
                     VDBean = VDBean.cloneVD_Bean((VD_Bean) vSRows.elementAt(selectedRowID));
                     DataManager.setAttribute(session, "m_VD", VDBean);
                 }
                 else
                 {
                     // No action type....
                 }
             
                 // capture the duration                
                 vCheckList = new Vector();
                 for (int m = 0; m < (vSRows.size()); m++)
                 {
                     String ckName2 = ("CK" + m);
                     String rSel2 = (String) req.getParameter(ckName2);
                     if (rSel2 != null)
                         vCheckList.addElement(ckName2);
                 }
             
                 DataManager.setAttribute(session, "CheckList", vCheckList);
                 DataManager.setAttribute(session, "vBEResult", vBEResult);
                 DataManager.setAttribute(session, "vACId", (Vector) m_classReq.getAttribute("vACId"));
                 DataManager.setAttribute(session, "vACName", (Vector) m_classReq.getAttribute("vACName"));
             }
             //get the ref doc list for the AC
             refreshUploadRefDoc(req, sSearchAC);
         }
         catch (Exception e)
         {
             // System.err.println("ERROR in GetACSearch-getSelRowToEdit: " + e);
             logger.error("ERROR in GetACSearch-getSelRowToEdit : " + e.toString(), e);
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create/Edit page. Please try again.");
             return false;
         }
         return true;
     }
     /**
      * Refreshes the Reference documents list after selecting the DE, deleting the attachment or uploading the attachment.
      * 
      * @param req The HttpServletRequest object.
      * @param String acType.
      * 
       */
     public void refreshUploadRefDoc(HttpServletRequest req, String acType)
     {
     	HttpSession session = req.getSession();
     	String acIdseq = "";
     	
         //get the acidseq from the session as per selected AC
     	if (acType.equals("DataElement"))
         {
         	DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
         	acIdseq = DEBean.getDE_DE_IDSEQ();
         }
         else if (acType.equals("DataElementConcept"))
         {
         	DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
         	acIdseq = DECBean.getDEC_DEC_IDSEQ();
         }
         else if (acType.equals("ValueDomain"))
         {
         	VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
         	acIdseq = VDBean.getVD_VD_IDSEQ();
         }
     	//get the ref docs using the ac idseq
         if (!acIdseq.equals("")) {
         	Vector vRefDoc = doRefDocSearch(acIdseq, "ALL TYPES", "open");
         	req.setAttribute("RefDocList", vRefDoc);
         }
     }
 
     public void do_nonEVSRepTermSearch(String InString, String ContName, String ASLName, String ID, Vector<EVS_Bean> vList,
             String type, String conName, String conID)
     {
     	ResultSet rs = null;
         PreparedStatement pstmt = null;
         String sCUIString = "";
         String compType = "";
         conID = m_classReq.getParameter("conid");
         if (ASLName.equals("")) ASLName=new String("%"); 
         try
         {
             if (m_servlet.getConn() == null)
                 m_servlet.ErrorLogin(m_classReq, m_classRes);
            
              String query = "select rep.preferred_name,rep.long_name,rep.preferred_definition,rep.conte_idseq,rep.asl_name,rep.rep_idseq,rep.version,rep.begin_date,rep.end_date,rep.change_note,rep.origin,rep.definition_source,rep.rep_id,rep.condr_idseq,c.name as CONTEXT from sbrext.representations_view_ext rep, contexts_view c where rep.CONTE_IDSEQ=c.CONTE_IDSEQ and rep.condr_idseq in (select condr_idseq from sbrext.component_concepts_view_ext where con_idseq in (select con_idseq from sbrext.concepts_view_ext where preferred_name = ? and rep.asl_name like ?)and display_order = 0) ORDER BY rep.long_name ASC";
              pstmt = m_servlet.getConn().prepareStatement(query);
              pstmt.setString(1, conID);
              pstmt.setString(2, ASLName);
 			// Now we are ready to call the stored procedure
              rs = pstmt.executeQuery();
 
             if (rs != null)
             {
                 // loop through to printout the outstrings
                 while (rs.next())
                 {
                     EVS_Bean OCBean = new EVS_Bean();
                     String sName = m_util.removeNewLineChar(rs.getString(1));
                     OCBean.setCONCEPT_NAME(sName);
                     String slName = m_util.removeNewLineChar(rs.getString(2));
                     OCBean.setLONG_NAME(slName);
                     String sDef = m_util.removeNewLineChar(rs.getString(3));
                     OCBean.setPREFERRED_DEFINITION(sDef);
                     OCBean.setCONTE_IDSEQ(rs.getString(4));
                     OCBean.setASL_NAME(rs.getString(5));
                     OCBean.setIDSEQ(rs.getString(6));
                     // add the decimal number
                     if (rs.getString("version").indexOf('.') >= 0)
                         OCBean.setVERSION(rs.getString("version"));
                     else
                         OCBean.setVERSION(rs.getString("version") + ".0");
                     OCBean.setEVS_DEF_SOURCE(rs.getString(12));
                     OCBean.setCONDR_IDSEQ(rs.getString("condr_idseq"));
                     OCBean.setEVS_DATABASE("caDSR");
                     OCBean.setcaDSR_COMPONENT(compType);
                     OCBean.setEVS_CONCEPT_SOURCE("origin");
                     OCBean.setID(rs.getString(13));
                     OCBean.setCONTEXT_NAME(rs.getString(15));
                     // get concatenated string of concept codes for EVS Identifier
                     EVSSearch evs = new EVSSearch(m_classReq, m_classRes, m_servlet);
                     sCUIString = evs.getEVSIdentifierString(rs.getString("condr_idseq"));
                     if (sCUIString == null)
                         sCUIString = "";
                     sCUIString = m_util.removeNewLineChar(sCUIString);
                     OCBean.setCONCEPT_IDENTIFIER(sCUIString);
                     vList.addElement(OCBean);
                 }
             }
         }
         catch (Exception e)
         {
           logger.error("ERROR - GetACSearch-do_caDSRSearch for other : " + e.toString(), e);
         }finally{
         	rs = SQLHelper.closeResultSet(rs);
             pstmt = SQLHelper.closePreparedStatement(pstmt);
         }
     }
     
 }
