 // Copyright (c) 2005 ScenPro, Inc.
 // $Header: /CVSNT/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/CurationServlet.java,v 1.81 2007/04/30 14:29:11
 // lhebel Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 // import files
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsServlet;
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsSession;
 import gov.nih.nci.cadsr.cdecurate.ui.DesDEServlet;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 import gov.nih.nci.cadsr.cdecurate.util.ToolURL;
 import gov.nih.nci.cadsr.sentinel.util.DSRAlert;
 import gov.nih.nci.cadsr.sentinel.util.DSRAlertImpl;
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 
 /**
  * The CurationServlet is the main servlet for communicating between the client and the server.
  * <P>
  *
  * @author Joe Zhou, Sumana Hegde, Tom Phillips, Jesse McKean
  * @version 3.0
  *
  */
 /*
  * The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. (ScenPro) Copyright Notice. The software
  * subject to this notice and license includes both human readable source code form and machine readable, binary, object
  * code form (the CaCORE Software). The CaCORE Software was developed in conjunction with the National Cancer
  * Institute (NCI) by NCI employees and employees of SCENPRO. To the extent government employees are authors, any
  * rights in such works shall be subject to Title 17 of the United States Code, section 105. This CaCORE Software
  * License (the License) is between NCI and You. You (or Your) shall mean a person or an entity, and all other
  * entities that control, are controlled by, or are under common control with the entity. Control for purposes of this
  * definition means (i) the direct or indirect power to cause the direction or management of such entity, whether by
  * contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii)
  * beneficial ownership of such entity. This License is granted provided that You agree to the conditions described
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and
  * royalty-free right and license in its rights in the CaCORE Software to (i) use, install, access, operate, execute,
  * copy, modify, translate, market, publicly display, publicly perform, and prepare derivative works of the CaCORE
  * Software; (ii) distribute and have distributed to and by third parties the CaCORE Software and any modifications and
  * derivative works thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties,
  * including the right to license such rights to further third parties. For sake of clarity, and not by way of
  * limitation, NCI shall have no right of accounting or right of payment from You or Your sublicensees for the rights
  * granted under this License. This License is granted at no charge to You. 1. Your redistributions of the source code
  * for the Software must retain the above copyright notice, this list of conditions and the disclaimer and limitation of
  * liability of Article 6, below. Your redistributions in object code form must reproduce the above copyright notice,
  * this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials provided with the
  * distribution, if any. 2. Your end-user documentation included with the redistribution, if any, must include the
  * following acknowledgment: This product includes software developed by SCENPRO and the National Cancer Institute. If
  * You do not include such end-user documentation, You shall include this acknowledgment in the Software itself,
  * wherever such third-party acknowledgments normally appear. 3. You may not use the names "The National Cancer
  * Institute", "NCI" ScenPro, Inc. and "SCENPRO" to endorse or promote products derived from this Software. This
  * License does not authorize You to use any trademarks, service marks, trade names, logos or product names of either
  * NCI or SCENPRO, except as required to comply with the terms of this License. 4. For sake of clarity, and not by way
  * of limitation, You may incorporate this Software into Your proprietary programs and into any third party proprietary
  * programs. However, if You incorporate the Software into third party proprietary programs, You agree that You are
  * solely responsible for obtaining any permission from such third parties required to incorporate the Software into
  * such third party proprietary programs and for informing Your sublicensees, including without limitation Your
  * end-users, of their obligation to secure any required permissions from such third parties before incorporating the
  * Software into such third party proprietary software programs. In the event that You fail to obtain such permissions,
  * You agree to indemnify NCI for any claims against NCI by such third parties, except to the extent prohibited by law,
  * resulting from Your failure to obtain such permissions. 5. For sake of clarity, and not by way of limitation, You may
  * add Your own copyright statement to Your modifications and to the derivative works, and You may provide additional or
  * different license terms and conditions in Your sublicenses of modifications of the Software, or any derivative works
  * of the Software as a whole, provided Your use, reproduction, and distribution of the Work otherwise complies with the
  * conditions stated in this License. 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A
  * PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SCENPRO, OR THEIR AFFILIATES BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 /**
  * @author shegde
  *
  */
 public class CurationServlet
 {
     private SetACService       m_setAC          = new SetACService(this);
     public HttpServletRequest  m_classReq       = null;
     public HttpServletResponse m_classRes       = null;
     private ServletContext m_servletContext;
     private Connection m_conn=null;
 
     public static final Logger  logger  = Logger.getLogger(CurationServlet.class.getName());
     /** declare the global variable sessionData */
     public Session_Data        sessionData;
     
     /**
      * @param req HttpServletRequest object
      * @param res HttpServletResponse object
      * @param sc ServletContext object
      *
      */
     public CurationServlet(HttpServletRequest req, HttpServletResponse res, ServletContext sc)
     {
         m_classReq = req;
         m_classRes = res;
         m_servletContext = sc;
         //m_conn=conn;
     }
 
     /**
      *
      */
     public CurationServlet()
     {
 
     }
 
     /**
      * makes the loggin message with all the information
      *
      * @param req
      * @param sMethod
      *            string a function name this is called from
      * @param endMsg
      *            string a message to append to
      * @param bDate
      *            Date begin date to calculate teh duration
      * @param eDate
      *            date end date to calculate teh duration
      * @return String message
      */
     public String getLogMessage(HttpServletRequest req, String sMethod, String endMsg, java.util.Date bDate,
                     java.util.Date eDate)
     {
         String sMsg = "";
         try
         {
             HttpSession session = req.getSession();
             // call this in utility service class
             UtilService util = new UtilService();
             sMsg = util.makeLogMessage(session, sMethod, endMsg, bDate, eDate);
         }
         catch (Exception e)
         {
             logger.warn("Unable to get the log message - " + sMsg);
         }
         return sMsg;
     }
 
 
     /**
      * display user friendly error message
      *
      * @param eMsg
      *            String message
      * @return String user friendly message
      */
     private String getDBConnectMessage(String eMsg)
     {
         String retMsg = "";
         if (eMsg.contains("invalid username/password"))
             retMsg = "caDSR connection error, please verify the user name and password.";
         else if (eMsg.contains("maximum number of processes"))
             retMsg = "Reached connection limits, please try again later. If this persists contact the NIH Help Desk.";
         else if (eMsg.contains("Network Adapter") || eMsg.contains("Got minus one"))
             retMsg = "Network connection error, please contact the NIH Help Desk.";
         else if (eMsg.contains("Session Terminated"))
             retMsg = "Session has been terminated. Possible reasons could be a session timeout or an internal processing error. Please try again or contact the NIH Help Desk.";
         else
             retMsg = "Encountered an unexpected and unknown connection error, please contact the NIH Help Desk.";
         return retMsg;
     }
 
     public Connection getConnFromDS(String user_, String pswd_)
     {
         // Use tool database pool.
         Context envContext = null;
         DataSource ds = null;
         try
         {
             envContext = new InitialContext();
             ds = (DataSource) envContext.lookup(NCICurationServlet._dataSourceName);
         }
         catch (Exception e)
         {
             String stErr = "Error creating database pool[" + e.getMessage() + "].";
             e.printStackTrace();
             System.out.println(stErr);
             logger.fatal(stErr, e);
             return null;
         }
         // Test connnection
         Connection con = null;
         try
         {
             con = ds.getConnection(user_, pswd_);
         }
         catch (Exception e)
         {
             System.err.println("Could not open database connection.");
             e.printStackTrace();
             logger.fatal(e.toString(), e);
             return null;
         }
         return con;
     }
 
     /**
      * free up the connection
      *
      * @param con
      */
     public void freeConnection(Connection con)
     {
         try
         {
             if (con != null && !con.isClosed())
                 con.close();
         }
         catch (Exception e)
         {
             logger.fatal("Error close connection ", e);
         }
     }
 
     /**
      * since connection info is stored in the session, I should be able to use connect DB with out passing information
      *
      * @return Connnection object
      */
     public Connection connectDB()
     {
         return connectDB(sessionData.UsrBean);
     }
 
     /**
      * This method tries to connect to the db, returns the Connection object if successful, if unsuccessful tries to
      * reconnect, returns null if unsuccessful. Called from various classes needing a connection. forwards page
      * 'CDELoginPageError2.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @return Connection SBRDb_conn 04/15/03 JZ Implement Realm Authen connction
      */
     public Connection connectDB(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res)
     {
         HttpSession session = req.getSession();
         return connectDB(session);
     }
 
     /**
      * @param session_
      * @return Connection
      */
     public Connection connectDB(HttpSession session_)
     {
         UserBean ub = (UserBean) session_.getAttribute("Userbean");
         return connectDB(ub);
     }
 
     /**
      * @param ub_
      * @return Connection
      */
     public Connection connectDB(UserBean ub_)
     {
         Connection SBRDb_conn = null;
         try
         {
             String username = "";
             String password = "";
             // make sure user name from session is active
             if (ub_ == null || ub_.getUsername().equals(""))
                 throw new Exception("User information is null");
             // get user info
             username = ub_.getUsername();
             password = ub_.getPassword();
             try
             {
                 SBRDb_conn = this.getConnFromDS(username, password);
             }
             catch (Exception e)
             {
                 logger.fatal("Servlet error: no pool connection.", e);
             }
         }
         catch (Exception e)
         {
             logger.fatal("Servlet connectDB : " + e.toString(), e);
         }
         return SBRDb_conn;
     }
     
     /**
      * Performs the login 
      */
     private void login(HttpServletRequest req, HttpServletResponse res,HttpSession session)
     {
     	String username = req.getParameter("Username");
         String password = req.getParameter("Password");
         UserBean userbean = new UserBean();
         userbean.setUsername(username);
         userbean.setPassword(password);
         m_conn= this.connectDB(userbean);
         DataManager.setAttribute(session, "Userbean", userbean);
     }   
 
     /**
      * The service method services all requests to this servlet.
      *
      */
     public void service()
     {
         UserBean ub = null;
         HttpSession session;
        // m_classReq = req;
         session = m_classReq.getSession(true);
         try
         {    
         	// get the session data object from the session
             sessionData = (Session_Data) session.getAttribute(Session_Data.CURATION_SESSION_ATTR);
             if (sessionData == null)
                      	sessionData = new Session_Data();
             else
             	m_conn = connectDB((UserBean)session.getAttribute("Userbean"));
             String reqType = m_classReq.getParameter("reqType");
             // logger.info("servlet reqType!: "+ reqType); //log the request
             m_classReq.setAttribute("LatestReqType", reqType);
             if (reqType != null)
             {
                 while (true)
                 {
                     // System.out.println("reqType " + reqType);
                     // check the validity of the user login
                     if (reqType.equals("login"))
                     {
                         DataManager.clearSessionAttributes(session);
                         sessionData = new Session_Data();
                         login(m_classReq,m_classRes,session);
                         doHomePage(m_classReq, m_classRes);
                         break;
                     }
                     if ("heartbeat".equals(reqType))
                     {
                         doLogout(m_classReq, m_classRes);
                         break;
                     }
                     // do the requests
                     ub = checkUserBean(m_classReq, m_classRes);
                     if (ub != null)
                     {
                         // java.util.Date startDate = new java.util.Date();
                         // logger.info(this.getLogMessage(m_classReq, "service", "started Request " + reqType, startDate,
                         // startDate));
                         if (reqType.equals("homePage"))
                         {
                             doHomePage(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("newDEFromMenu"))
                         {
                             doOpenCreateNewPages(m_classReq, m_classRes, "de");
                         }
                         else if (reqType.equals("newDECFromMenu"))
                         {
                             doOpenCreateNewPages(m_classReq, m_classRes, "dec");
                         }
                         else if (reqType.equals("newVDFromMenu"))
                         {
                             doOpenCreateNewPages(m_classReq, m_classRes, "vd");
                         }
                         else if (reqType.equals("newDEfromForm"))
                         {
                             doCreateDEActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("newDECfromForm")) // when DEC form is submitted
                         {
                             doCreateDECActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("newVDfromForm")) // when Edit VD form is submitted
                         {
                             doCreateVDActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("editDE")) // when Edit DE form is submitted
                         {
                             doEditDEActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("editDEC")) // when Edit DEC form is submitted
                         {
                             doEditDECActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("editVD"))
                         {
                             doEditVDActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("newPV")) // fromForm
                         {
                             doCreatePVActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("pvEdits")) // fromForm
                         {
                             doEditPVActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals(VMForm.ELM_FORM_REQ_DETAIL) || reqType.equals(VMForm.ELM_FORM_REQ_USED)
                                         || reqType.equals(VMForm.ELM_FORM_REQ_VAL)) // fromForm
                         {
                             doEditVMActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("createPV") || reqType.equals("editPV"))
                         {
                             doOpenCreatePVPage(m_classReq, m_classRes, reqType, "");
                         }
                         else if (reqType.equals("createNewDEC"))
                         {
                             doOpenCreateDECPage(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("createNewVD"))
                         {
                             doOpenCreateVDPage(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("validateDEFromForm"))
                         {
                             doInsertDE(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("validateDECFromForm"))
                         {
                             doInsertDEC(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("validateVDFromForm"))
                         {
                             doInsertVD(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("searchACs"))
                         {
                             doGetACSearchActions(m_classReq, m_classRes); // m_classReq for search parameters page
                         }
                         else if (reqType.equals("showResult"))
                         {
                             doSearchResultsAction(m_classReq, m_classRes, ub); // m_classReq from search results page
                         }
                         else if (reqType.equals("showBEDisplayResult"))
                         {
                             doDisplayWindowBEAction(m_classReq, m_classRes); // m_classReq from search results page showBEDisplayResult
                         }
                         else if (reqType.equals("showDECDetail"))
                         {
                             doDECDetailDisplay(m_classReq, m_classRes); // m_classReq from DECDetailsWindow page
                         }
                         else if (reqType.equals("doSortCDE"))
                         {
                             doSortACActions(m_classReq, m_classRes); // on sort by heading for search
                         }
                         else if (reqType.equals("doSortBlocks"))
                         {
                             doSortBlockActions(m_classReq, m_classRes, "Blocks"); // on sort by heading for search
                         }
                         else if (reqType.equals("doSortQualifiers"))
                         {
                             doSortBlockActions(m_classReq, m_classRes, "Qualifiers"); // on sort by heading for search
                         }
                         else if (reqType.equals("getSearchFilter"))
                         {
                             doOpenSearchPage(m_classReq, m_classRes); // on click on the search from menu
                         }
                         else if (reqType.equals("actionFromMenu"))
                         {
                             doMenuAction(m_classReq, m_classRes); // on click on the edit/create from menu
                         }
                         else if (reqType.equals("errorPageForward"))
                         {
                             doJspErrorAction(m_classReq, m_classRes); // on click on the edit/create from menu
                         }
                         else if (reqType.equals("logout"))
                         {
                             doLogout(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("searchEVS"))
                         {
                             doSearchEVS(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("searchBlocks"))
                         {
                             doBlockSearchActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("searchQualifiers"))
                         {
                             doQualifierSearchActions(m_classReq, m_classRes);
                         }
                         // get more records of Doc Text
                         else if (reqType.equals("getRefDocument"))
                         {
                             this.doRefDocSearchActions(m_classReq, m_classRes);
                         }
                         // get more records of alternate names
                         else if (reqType.equals("getAltNames"))
                         {
                             this.doAltNameSearchActions(m_classReq, m_classRes);
                         }
                         // get more records of permissible values
                         else if (reqType.equals("getPermValue"))
                         {
                             this.doPermValueSearchActions(m_classReq, m_classRes);
                         }
                         // get DDE details
                         else if (reqType.equals("getDDEDetails"))
                         {
                             this.doDDEDetailsActions(m_classReq, m_classRes);
                         }
                         // get more records of protocol crf
                         else if (reqType.equals("getProtoCRF"))
                         {
                             this.doProtoCRFSearchActions(m_classReq, m_classRes);
                         }
                         // get detailed records of concept class
                         else if (reqType.equals("getConClassForAC"))
                         {
                             this.doConClassSearchActions(m_classReq, m_classRes);
                         }
                         // get cd details for vm
                         else if (reqType.equals("showCDDetail"))
                         {
                             this.doConDomainSearchActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeSearch"))
                         {
                             // doTreeSearchRequest(m_classReq, m_classRes, "", "", "", "");
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeRefresh"))
                         {
                             // doTreeRefreshRequest(m_classReq, m_classRes);
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeExpand"))
                         {
                             // doTreeExpandRequest(m_classReq, m_classRes);
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeCollapse"))
                         {
                             // doTreeCollapseRequest(m_classReq, m_classRes);
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("getSubConcepts"))
                         {
                             // doGetSubConcepts(m_classReq, m_classRes);
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("getSuperConcepts"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                             // doGetSuperConcepts(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("designateDE"))
                         {
                             new DesDEServlet(this, ub).doAction(m_classReq, m_classRes, "Edit");
                         }
                         else if (reqType.equals(AltNamesDefsServlet._reqType))
                         {
                             new AltNamesDefsServlet(this, ub).doAction(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("RefDocumentUpload"))
                         {
                             this.doRefDocumentUpload(m_classReq, m_classRes, "Request");
                         }
                         else if (reqType.equals("nonEVSSearch"))
                         {
                             this.doNonEVSPageAction(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("ACcontact"))
                         {
                             this.doContactEditActions(m_classReq, m_classRes);
                         }
                         // log message
                         // java.util.Date endDate = new java.util.Date();
                         // logger.info(this.getLogMessage(m_classReq, "service", "ended Request " + reqType, startDate, endDate));
                         break;
                     }
                     if (!reqType.equals("login"))
                     {
                         String errMsg = getDBConnectMessage("Session Terminated"); // "Please login again. Your session has
                                                                                     // been terminated. Possible reasons
                                                                                     // could be a session timeout or an
                                                                                     // internal processing error.";
                         DataManager.setAttribute(session, "ErrorMessage", errMsg);
                         // get the menu action from request
                         String mnReq = (String) m_classReq.getParameter("serMenuAct");
                         if (mnReq == null)
                             mnReq = "";
                         DataManager.setAttribute(session, "serMenuAct", mnReq);
                         // forward the error page
                         ForwardErrorJSP(m_classReq, m_classRes, errMsg);
                         break;
                     }
                     break;
                 }
             }
             else
             {
                 this.logger.fatal("Service: no DB Connection");
                 ErrorLogin(m_classReq, m_classRes);
             }
             freeConnection(m_conn);
         }
         catch (Exception e)
         {
             logger.fatal("Service error : " + e.toString(), e);
             session = m_classReq.getSession();
             String msg = e.toString();
             try
             {
                 if (msg != null)
                     ForwardErrorJSP(m_classReq, m_classRes, msg);
                 else
                     ForwardErrorJSP(m_classReq, m_classRes, "A page error has occurred. Please login again.");
             }
             catch (Exception ee)
             {
                 logger.fatal("Service forward error : " + ee.toString(), ee);
             }
             finally{
             	try {
 					m_conn.close();
 				} catch (SQLException e1) {
 					// TODO Auto-generated catch block
 					 logger.fatal("Error in finaaly block while closing connection : " + e1.toString(), e1);
 				}
             }
         }
     } // end of service
 
     /**
      * The checkUserBean method gets the session then checks whether a Userbean exits. Called from 'service' method.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @return userBeanExists
      *
      * @throws Exception
      */
     private UserBean checkUserBean(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session;
         session = req.getSession(true);
         UserBean userbean = (UserBean) session.getAttribute("Userbean");
         if (userbean == null)
         {
             logger.fatal("User bean is null");
             // ForwardErrorJSP(req, res, "Please login again. Your session has been terminated. Possible reasons could
             // be a session timeout or an internal processing error.");
         }
         else
         {
             EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; // (EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);
                                                                                 // //("EvsUserBean");
             if (eUser == null)
                 eUser = new EVS_UserBean();
         }
         return userbean;
     }
 
     /**
      * The doOpenCreateNewPages method will set some session attributes then forward the request to a Create page.
      * Called from 'service' method where reqType is 'newDEFromMenu', 'newDECFromMenu', 'newVDFromMenu' Sets some
      * intitial session attributes. Calls 'getAC.getACList' to get the Data list from the database for the selected
      * context. Sets session Bean and forwards the create page for the selected component.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sACType
      *            The type of page being called, i.e. de, dec, or vd
      *
      * @throws Exception
      */
     private void doOpenCreateNewPages(HttpServletRequest req, HttpServletResponse res, String sACType) throws Exception
     {
         //GetACService getAC = new GetACService(req, res, this); //
         HttpSession session = req.getSession();
         clearSessionAttributes(req, res);
         this.clearBuildingBlockSessionAttributes(req, res);
         String context = (String) session.getAttribute("sDefaultContext"); // from Login.jsp
         //String ContextInList = (String) session.getAttribute("ContextInList");
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
         DataManager.setAttribute(session, "DDEAction", "nothing"); // reset from "CreateNewDEFComp"
         //String sOriginAction = (String) session.getAttribute("originAction");
         DataManager.setAttribute(session, "sCDEAction", "nothing");
         DataManager.setAttribute(session, "VDPageAction", "nothing");
         DataManager.setAttribute(session, "DECPageAction", "nothing");
         DataManager.setAttribute(session, "sDefaultContext", context);
         this.clearCreateSessionAttributes(req, res); // clear some session attributes
         if (sACType.equals("de"))
         {
             DE_Bean m_DE = new DE_Bean();
             m_DE.setDE_ASL_NAME("DRAFT NEW");
             m_DE.setAC_PREF_NAME_TYPE("SYS");
             DataManager.setAttribute(session, "m_DE", m_DE);
             DE_Bean oldDE = new DE_Bean();
             oldDE.setDE_ASL_NAME("DRAFT NEW");
             oldDE.setAC_PREF_NAME_TYPE("SYS");
             DataManager.setAttribute(session, "oldDEBean", oldDE);
             DataManager.setAttribute(session, "originAction", "NewDEFromMenu");
             DataManager.setAttribute(session, "LastMenuButtonPressed", "CreateDE");
             doInitDDEInfo(req, res);
             ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else if (sACType.equals("dec"))
         {
             DataManager.setAttribute(session, "originAction", "NewDECFromMenu");
             DataManager.setAttribute(session, "LastMenuButtonPressed", "CreateDEC");
             DEC_Bean m_DEC = new DEC_Bean();
             m_DEC.setDEC_ASL_NAME("DRAFT NEW");
             m_DEC.setAC_PREF_NAME_TYPE("SYS");
             DataManager.setAttribute(session, "m_DEC", m_DEC);
             DEC_Bean oldDEC = new DEC_Bean();
             oldDEC.setDEC_ASL_NAME("DRAFT NEW");
             oldDEC.setAC_PREF_NAME_TYPE("SYS");
             DataManager.setAttribute(session, "oldDECBean", oldDEC);
             EVS_Bean m_OC = new EVS_Bean();
             DataManager.setAttribute(session, "m_OC", m_OC);
             EVS_Bean m_PC = new EVS_Bean();
             DataManager.setAttribute(session, "m_PC", m_PC);
             EVS_Bean m_OCQ = new EVS_Bean();
             DataManager.setAttribute(session, "m_OCQ", m_OCQ);
             EVS_Bean m_PCQ = new EVS_Bean();
             DataManager.setAttribute(session, "m_PCQ", m_PCQ);
             DataManager.setAttribute(session, "selPropRow", "");
             DataManager.setAttribute(session, "selPropQRow", "");
             DataManager.setAttribute(session, "selObjQRow", "");
             DataManager.setAttribute(session, "selObjRow", "");
             ForwardJSP(req, res, "/CreateDECPage.jsp");
         }
         else if (sACType.equals("vd"))
         {
             DataManager.setAttribute(session, "originAction", "NewVDFromMenu");
             DataManager.setAttribute(session, "LastMenuButtonPressed", "CreateVD");
             VD_Bean m_VD = new VD_Bean();
             m_VD.setVD_ASL_NAME("DRAFT NEW");
             m_VD.setAC_PREF_NAME_TYPE("SYS");
             DataManager.setAttribute(session, "m_VD", m_VD);
             VD_Bean oldVD = new VD_Bean();
             oldVD.setVD_ASL_NAME("DRAFT NEW");
             oldVD.setAC_PREF_NAME_TYPE("SYS");
             DataManager.setAttribute(session, "oldVDBean", oldVD);
             EVS_Bean m_OC = new EVS_Bean();
             DataManager.setAttribute(session, "m_OC", m_OC);
             EVS_Bean m_PC = new EVS_Bean();
             DataManager.setAttribute(session, "m_PC", m_PC);
             EVS_Bean m_REP = new EVS_Bean();
             DataManager.setAttribute(session, "m_REP", m_REP);
             EVS_Bean m_OCQ = new EVS_Bean();
             DataManager.setAttribute(session, "m_OCQ", m_OCQ);
             EVS_Bean m_PCQ = new EVS_Bean();
             DataManager.setAttribute(session, "m_PCQ", m_PCQ);
             EVS_Bean m_REPQ = new EVS_Bean();
             DataManager.setAttribute(session, "m_REPQ", m_REPQ);
             // empty the session attributes
             // DataManager.setAttribute(session, "VDPVList", new Vector<PV_Bean>());
             // DataManager.setAttribute(session, "oldVDPVList", new Vector<PV_Bean>());
             // DataManager.setAttribute(session, "PVIDList", new Vector());
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         else if (sACType.equals("pv"))
         {
             ForwardJSP(req, res, "/CreatePVPage.jsp");
         }
     } // end of doOpenCreateNewPages
 
     // ////////////////////////////////////////////////////////////////////////////////////
     /**
      * The doLogin method forwards to CDEHomePage.jsp.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doLogin(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         ForwardJSP(req, res, "/CDEHomePage.jsp");
     }
 
     /**
      * The doHomePage method gets the session, set some session attributes, then connects to the database. Called from
      * 'service' method where reqType is 'login', 'homePage' calls 'getAC.getACList' to get the Data list from the
      * database for the selected Context at login page. calls 'doOpenSearchPage' to open the home page.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client 04/15/03 Implement Realm Authentication connection
      *
      */
     public void doHomePage(HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             HttpSession session = req.getSession();
             m_classReq = req;
             m_classRes = res;
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
             DataManager.setAttribute(session, "originAction", "nothing");
             DataManager.setAttribute(session, "DDEAction", "nothing"); // to separate from DDE with simple de
             DataManager.setAttribute(session, "VMMeaning", "nothing");
             DataManager.setAttribute(session, "ConnectedToDB", "nothing");
             req.setAttribute("UISearchType", "nothing");
             DataManager.setAttribute(session, "OpenTreeToConcept", "false");
             DataManager.setAttribute(session, "strHTML", "");
             DataManager.setAttribute(session, "creSearchAC", "");
             DataManager.setAttribute(session, "ParentConcept", "");
             DataManager.setAttribute(session, "SelectedParentName", "");
             DataManager.setAttribute(session, "SelectedParentCC", "");
             DataManager.setAttribute(session, "SelectedParentDB", "");
             DataManager.setAttribute(session, "SelectedParentMetaSource", "");
             DataManager.setAttribute(session, "ConceptLevel", "0");
             DataManager.setAttribute(session, "sDefaultStatus", "DRAFT NEW");
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
             //String Username = req.getParameter("Username");
             //String Password = req.getParameter("Password");
             UserBean userbean= (UserBean) session.getAttribute("Userbean");
             //Userbean.setUsername(Username);
             //Userbean.setPassword(Password);
             userbean.setDBAppContext("/cdecurate");
             DataManager.setAttribute(session, "Userbean", userbean);
             DataManager.setAttribute(session, "Username", userbean.getUsername());
             sessionData.UsrBean = userbean;
             GetACService getAC = new GetACService(req, res, this);
             getAC.verifyConnection(req, res);
             // dbcon.verifyConnection(req);
             String ConnectedToDB = (String) session.getAttribute("ConnectedToDB");
             if (ConnectedToDB != null && !ConnectedToDB.equals("No"))
             {
             	userbean.setSuperuser(getAC.getSuperUserFlag(userbean.getUsername()));
 
                 // get initial list from cadsr
                 Vector vList = null;
                 String aURL = null;
                 vList = new Vector();
                 vList = getAC.getToolOptionData("EVS", "NEWTERM.URL", "");
                 aURL = null;
                 if (vList != null && vList.size() > 0)
                 {
                     TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
                     if (tob != null)
                         aURL = tob.getVALUE();
                 }
                 ToolURL.setEVSNewTermURL(session, aURL);
                 vList = new Vector();
                 vList = getAC.getToolOptionData("CDEBrowser", "URL", "");
                 aURL = null;
                 if (vList != null && vList.size() > 0)
                 {
                     TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
                     if (tob != null)
                         aURL = tob.getVALUE();
                 }
                 ToolURL.setBrowserUrl(session, aURL);
                 vList = new Vector();
                 vList = getAC.getToolOptionData("SENTINEL", "URL", "");
                 aURL = null;
                 if (vList != null && vList.size() > 0)
                 {
                     TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
                     if (tob != null)
                         aURL = tob.getVALUE();
                 }
                 ToolURL.setSentinelUrl(session, aURL);
                 vList = new Vector();
                 vList = getAC.getToolOptionData("UMLBrowser", "URL", "");
                 aURL = null;
                 if (vList != null && vList.size() > 0)
                 {
                     TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
                     if (tob != null)
                         aURL = tob.getVALUE();
                 }
                 ToolURL.setUmlBrowserUrl(session, aURL);
                 getAC.getACList(req, res, "", true, "ALL");
                 doOpenSearchPage(req, res);
                 getCompAttrList(req, res, "DataElement", "searchForCreate");
                 // get EVS info
                 try
                 {
                     EVS_UserBean eUser = new EVS_UserBean();
                     eUser.getEVSInfoFromDSR(req, res, this);
                     EVSSearch evs = new EVSSearch(req, res, this);
                     evs.getMetaSources();
                     // m_EVS_CONNECT = euBean.getEVSConURL();
                     // getVocabHandles(req, res);
                     // DoHomepageThread thread = new DoHomepageThread(req, res, this);
                     // thread.start();
                 }
                 catch (Exception ee)
                 {
                     logger.fatal("Servlet-doHomePage-evsthread : " + ee.toString(), ee);
                 }
             }
             else
             {
                 DataManager.setAttribute(session, "ConnectedToDB", "nothing"); // was No, so reset value
                 ForwardErrorJSP(req, res,
                                 "Problem with login. User name/password may be incorrect, or database connection can not be established.");
                 // ForwardErrorJSP(req, res, "Unable to connect to the database. Please log in again.");
             }
         }
         catch (Exception e)
         {
             try
             {
                 // if error, forward to login page to re-enter username and password
                 logger.fatal("Servlet-doHomePage : " + e.toString(), e);
                 String msg = e.getMessage().substring(0, 12);
                 if (msg.equals("Io exception"))
                     ForwardErrorJSP(req, res, "Io Exception. Session Terminated. Please log in again.");
                 else
                     ForwardErrorJSP(req, res, "Incorrect Username or Password. Please re-enter.");
             }
             catch (Exception ee)
             {
                 logger.fatal("Servlet-doHomePage, display error page : " + ee.toString(), ee);
             }
         }
     }
 
     /**
      * The doCreateDEActions method handles CreateDE or EditDE actions of the request. Called from 'service' method
      * where reqType is 'newDEfromForm' Calls 'ValidateDE' if the action is Validate or submit. Calls 'doSuggestionDE'
      * if the action is open EVS Window. Calls 'doOpenCreateDECPage' if the action is create new DEC from DE Page. Calls
      * 'doOpenCreateVDPage' if the action is create new VD from VD Page.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doCreateDEActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sPageAction = (String) req.getParameter("pageAction");
         if (sPageAction != null)
             DataManager.setAttribute(session, "sCDEAction", sPageAction);
         String sMenuAction = (String) req.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sSubAction = (String) req.getParameter("DEAction");
         DataManager.setAttribute(session, "DEAction", sSubAction);
         String sOriginAction = (String) session.getAttribute("originAction");
         // save DDE info every case except back from DEComp
         String ddeType = (String) req.getParameter("selRepType");
         if (ddeType != null && !ddeType.equals(""))
             doUpdateDDEInfo(req, res);
         // handle all page actions
         if (sPageAction.equals("changeContext"))
             doChangeContext(req, res, "de");
         else if (sPageAction.equals("submit"))
             doSubmitDE(req, res);
         else if (sPageAction.equals("validate"))
             doValidateDE(req, res);
         else if (sPageAction.equals("suggestion"))
             doSuggestionDE(req, res);
         else if (sPageAction.equals("updateNames"))
         {
             this.doGetDENames(req, res, "new", "Search", null);
             ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else if (sPageAction.equals("changeNameType"))
         {
             this.doChangeDENameType(req, res, "changeType");
             ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else if (sPageAction.equals("createNewDEC"))
         {
             DataManager.setAttribute(session, "originAction", "CreateNewDECfromCreateDE");
             doOpenCreateDECPage(req, res);
         }
         else if (sPageAction.equals("createNewVD"))
         {
             DataManager.setAttribute(session, "originAction", "CreateNewVDfromCreateDE");
             doOpenCreateVDPage(req, res);
         }
         else if (sPageAction.equals("CreateNewDEFComp"))
             doOpenCreateDECompPage(req, res);
         else if (sPageAction.equals("DECompBackToNewDE") || sPageAction.equals("DECompBackToEditDE"))
             doOpenDEPageFromDEComp(req, res);
         else if (sPageAction.equals("Store Alternate Names") || sPageAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(req, res, "DataElement", sPageAction, "createAC");
         else if (sPageAction.equals("doContactUpd") || sPageAction.equals("removeContact"))
         {
             DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
             // capture all page attributes
             m_setAC.setDEValueFromPage(req, res, DEBean);
             DEBean.setAC_CONTACTS(this.doContactACUpdates(req, sPageAction));
             ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else if (sPageAction.equals("clearBoxes"))
         {
             DE_Bean DEBean = (DE_Bean) session.getAttribute("oldDEBean");
             String sDEID = DEBean.getDE_DE_IDSEQ();
             String sVDid = DEBean.getDE_VD_IDSEQ();
             Vector vList = new Vector();
             // get VD's attributes from the database again
             GetACSearch serAC = new GetACSearch(req, res, this);
             if (sDEID != null && !sDEID.equals(""))
             {
                 serAC.doDESearch(sDEID, "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                 "", "", "", "", "", "", "", "", "", "", vList);
             }
             if (vList.size() > 0) // get all attributes
             {
                 DEBean = (DE_Bean) vList.elementAt(0);
                 DEBean = serAC.getDEAttributes(DEBean, sOriginAction, sMenuAction);
                 serAC.getDDEInfo(DEBean.getDE_DE_IDSEQ()); // clear dde
             }
             else if (sVDid == null || sVDid.equals(""))
             {
                 DEBean = new DE_Bean();
                 DEBean.setDE_ASL_NAME("DRAFT NEW");
                 DEBean.setAC_PREF_NAME_TYPE("SYS");
                 this.doInitDDEInfo(req, res);
             }
             // get the clone copy of hte updated bean to open the page
             DE_Bean pgBean = new DE_Bean();
             DataManager.setAttribute(session, "m_DE", pgBean.cloneDE_Bean(DEBean, "Complete"));
             ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else if (sPageAction.equals("backToSearch"))
         {
             this.clearCreateSessionAttributes(req, res);
             GetACSearch serAC = new GetACSearch(req, res, this);
             // forword to search page with de search results
             if (sMenuAction.equals("NewDETemplate") || sMenuAction.equals("NewDEVersion"))
             {
                 DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
                 serAC.refreshData(req, res, DEBean, null, null, null, "Refresh", "");
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             // forward to search page with questions search results
             else if (sMenuAction.equals("Questions"))
             {
                 Quest_Bean QuestBean = (Quest_Bean) session.getAttribute("m_Quest");
                 serAC.refreshData(req, res, null, null, null, QuestBean, "Refresh", "");
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             else
                 ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
     }
 
     /**
      * The doEditDEActions method handles EditDE actions of the request. Called from 'service' method where reqType is
      * 'EditDEfromForm' Calls 'ValidateDE' if the action is Validate or submit. Calls 'doSuggestionDE' if the action is
      * open EVS Window. Calls 'doOpenDECForEdit' if the action is Edit DEC from EditDE Page. Calls 'doOpenVDForEdit' if
      * the action is Edit VD from EditDE Page.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doEditDEActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sPageAction = (String) req.getParameter("pageAction");
         if (sPageAction != null)
             DataManager.setAttribute(session, "sCDEAction", sPageAction);
         String sMenuAction = (String) req.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sSubAction = (String) req.getParameter("DEAction");
         DataManager.setAttribute(session, "DEAction", sSubAction);
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sOriginAction == null)
             sOriginAction = "";
         // save DDE info every case except back from DEComp
         String ddeType = (String) req.getParameter("selRepType");
         String oldDDEType = (String) session.getAttribute("sRepType");
         // update the dde info if new one or if old one if not block edit
         if (!sOriginAction.equals("BlockEditDE")
                         && ((ddeType != null && !ddeType.equals("")) || (oldDDEType != null && !oldDDEType.equals(""))))
             doUpdateDDEInfo(req, res);
         if (sPageAction.equals("submit"))
             doSubmitDE(req, res);
         else if (sPageAction.equals("validate") && sOriginAction.equals("BlockEditDE"))
             doValidateDEBlockEdit(req, res);
         else if (sPageAction.equals("validate"))
             doValidateDE(req, res);
         else if (sPageAction.equals("suggestion"))
             doSuggestionDE(req, res);
         else if (sPageAction.equals("EditDECfromDE"))
         {
             DataManager.setAttribute(session, "originAction", "editDECfromDE");
             doOpenEditDECPage(req, res);
         }
         else if (sPageAction.equals("createNewDEC"))
         {
             DataManager.setAttribute(session, "originAction", "CreateNewDECfromEditDE");
             this.doOpenCreateDECPage(req, res);
         }
         else if (sPageAction.equals("createNewVD"))
         {
             DataManager.setAttribute(session, "originAction", "CreateNewVDfromEditDE");
             this.doOpenCreateVDPage(req, res);
         }
         else if (sPageAction.equals("EditVDfromDE"))
         {
             DataManager.setAttribute(session, "originAction", "editVDfromDE");
             doOpenEditVDPage(req, res);
         }
         else if (sPageAction.equals("updateNames"))
         {
             // DE_Bean de = this.doGetDENames(req, res, "append", "Search", null);
             this.doGetDENames(req, res, "new", "Search", null);
             ForwardJSP(req, res, "/EditDEPage.jsp");
         }
         else if (sPageAction.equals("changeNameType"))
         {
             this.doChangeDENameType(req, res, "changeType");
             ForwardJSP(req, res, "/EditDEPage.jsp");
         }
         else if (sPageAction.equals("clearBoxes"))
         {
             DE_Bean DEBean = (DE_Bean) session.getAttribute("oldDEBean");
             String sDEID = DEBean.getDE_DE_IDSEQ();
             Vector vList = new Vector();
             // get VD's attributes from the database again
             GetACSearch serAC = new GetACSearch(req, res, this);
             if (sDEID != null && !sDEID.equals(""))
                 serAC.doDESearch(sDEID, "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                                 "", "", "", "", "", "", "", "", "", "", vList);
             if (vList.size() > 0) // get all attributes
             {
                 DEBean = (DE_Bean) vList.elementAt(0);
                 DEBean = serAC.getDEAttributes(DEBean, sOriginAction, sMenuAction);
                 serAC.getDDEInfo(DEBean.getDE_DE_IDSEQ()); // clear dde
             }
             else
             {
                 this.doInitDDEInfo(req, res);
             }
             DE_Bean pgBean = new DE_Bean();
             DataManager.setAttribute(session, "m_DE", pgBean.cloneDE_Bean(DEBean, "Complete"));
             ForwardJSP(req, res, "/EditDEPage.jsp");
         }
         else if (sPageAction.equals("backToSearch"))
         {
             GetACSearch serAC = new GetACSearch(req, res, this);
             // forward to search page with questions search results
             if (sMenuAction.equals("Questions"))
             {
                 Quest_Bean QuestBean = (Quest_Bean) session.getAttribute("m_Quest");
                 serAC.refreshData(req, res, null, null, null, QuestBean, "Refresh", "");
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             else if (sMenuAction.equalsIgnoreCase("editDE") || sOriginAction.equalsIgnoreCase("BlockEditDE")
                             || sButtonPressed.equals("Search") || sOriginAction.equalsIgnoreCase("EditDE")) // ||
                                                                                                             // sMenuAction.equals("EditDesDE"))
             {
                 // DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
                 Vector<String> vResult = new Vector<String>();
                 serAC.getDEResult(req, res, vResult, "");
                 DataManager.setAttribute(session, "results", vResult);
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             else
                 ForwardJSP(req, res, "/EditDEPage.jsp");
         }
         else if (sPageAction.equals("CreateNewDEFComp"))
         {
             doOpenCreateDECompPage(req, res);
         }
         else if (sPageAction.equals("Store Alternate Names") || sPageAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(req, res, "DataElement", sPageAction, "editAC");
         else if (sPageAction.equals("doContactUpd") || sPageAction.equals("removeContact"))
         {
             DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
             // capture all page attributes
             m_setAC.setDEValueFromPage(req, res, DEBean);
             DEBean.setAC_CONTACTS(this.doContactACUpdates(req, sPageAction));
             ForwardJSP(req, res, "/EditDEPage.jsp");
         }
     } // end of doEditDEActions
 
     /**
      * this method is used to create preferred name for DE it gets the selected DEC or VD bean from the search result
      * and stores it in DE if from teh search. if from create or edit dec or vd, it refreshes DE bean with new dec or
      * vd. names of all three types will be stored in the bean for later use if type is changed, it populates name
      * according to type selected.
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param nameAct
      *            string new name or apeend name
      * @param sOrigin
      *            String what changed to make this name creation
      * @param pageDE
      *            current de bean
      * @return DE_Bean
      * @throws java.lang.Exception
      */
     public DE_Bean doGetDENames(HttpServletRequest req, HttpServletResponse res, String nameAct, String sOrigin,
                     DE_Bean pageDE) throws Exception
     {
         HttpSession session = req.getSession();
         if (pageDE == null)
             pageDE = (DE_Bean) session.getAttribute("m_DE");
         // get other de attributes from page
         if (sOrigin.equals("Search"))
             m_setAC.setDEValueFromPage(req, res, pageDE);
         String sSysName = pageDE.getAC_SYS_PREF_NAME();
         String sAbbName = pageDE.getAC_ABBR_PREF_NAME();
         // store teh page one if typed ealier
         String selNameType = pageDE.getAC_PREF_NAME_TYPE();
         if (selNameType == null)
             selNameType = "";
         if (sOrigin.equals("Search") || sOrigin.equals("Remove"))
         {
             selNameType = (String) req.getParameter("rNameConv");
             if (selNameType == null)
                 selNameType = "";
             String sPrefName = (String) req.getParameter("txtPreferredName");
             if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
                 pageDE.setAC_USER_PREF_NAME(sPrefName);
         }
         String sUsrName = pageDE.getAC_USER_PREF_NAME();
         // get other attrs
         sSysName = "";
         sAbbName = "";
         DEC_Bean deDEC = pageDE.getDE_DEC_Bean();
         VD_Bean deVD = pageDE.getDE_VD_Bean();
         String sDef = pageDE.getDE_PREFERRED_DEFINITION();
         // get selected row from the search results
         if (sOrigin.equals("Search"))
         {
             String acSearch = (String) req.getParameter("acSearch");
             if (acSearch == null)
                 acSearch = "ALL";
             // get the result vector from the session
             Vector vRSel = (Vector) session.getAttribute("vACSearch");
             if (vRSel == null)
                 vRSel = new Vector();
             // get the array from teh hidden list
             String selRows[] = req.getParameterValues("hiddenSelRow");
             if (selRows == null)
                 DataManager.setAttribute(session, "StatusMessage", "Unable to select Row, please try again");
             else
             {
                 // loop through the array of strings
                 for (int i = 0; i < selRows.length; i++)
                 {
                     String thisRow = selRows[i];
                     Integer IRow = new Integer(thisRow);
                     int iRow = IRow.intValue();
                     if (iRow < 0 || iRow > vRSel.size())
                     {
                         DataManager.setAttribute(session, "StatusMessage", "Row size is either too big or too small.");
                     }
                     else
                     {
                         if (acSearch.equals("DataElementConcept"))
                         {
                             deDEC = (DEC_Bean) vRSel.elementAt(iRow);
                             pageDE.setDE_DEC_Bean(deDEC);
                             // if (nameAct.equals("append"))
                             // pageDE.setDE_PREFERRED_DEFINITION(pageDE.getDE_PREFERRED_DEFINITION() + "_" +
                             // deDEC.getDEC_PREFERRED_DEFINITION());
                         }
                         else if (acSearch.equals("ValueDomain"))
                         {
                             deVD = (VD_Bean) vRSel.elementAt(iRow);
                             pageDE.setDE_VD_Bean(deVD);
                             // if (nameAct.equals("append"))
                             // pageDE.setDE_PREFERRED_DEFINITION(pageDE.getDE_PREFERRED_DEFINITION() + "_" +
                             // deVD.getVD_PREFERRED_DEFINITION());
                         }
                     }
                 }
             }
         }
         String sLongName = "";
         if (nameAct.equals("new"))
             sDef = "";
         // get sys name and abbr names from dec and vd beans
         if (deDEC != null)
         {
             String sver = deDEC.getDEC_VERSION();
             if (sver != null && sver.indexOf(".") < 0)
                 sver += ".0";
             sSysName = deDEC.getDEC_DEC_ID() + "v" + sver + ":";
             sAbbName = deDEC.getDEC_PREFERRED_NAME() + "_";
             sLongName = deDEC.getDEC_LONG_NAME() + " ";
             if (nameAct.equals("new"))
                 sDef = deDEC.getDEC_PREFERRED_DEFINITION() + "_";
         }
         if (deVD != null)
         {
             String sver = deVD.getVD_VERSION();
             if (sver != null && sver.indexOf(".") < 0)
                 sver += ".0";
             sSysName = sSysName + deVD.getVD_VD_ID() + "v" + sver;
             sAbbName = sAbbName + deVD.getVD_PREFERRED_NAME();
             sLongName = sLongName + deVD.getVD_LONG_NAME();
             if (nameAct.equals("new"))
                 sDef = sDef + deVD.getVD_PREFERRED_DEFINITION();
         }
         // truncate to 30 characters
         if (sAbbName != null && sAbbName.length() > 30)
             sAbbName = sAbbName.substring(0, 30);
         // truncate to 30 characters
         if (sSysName != null && sSysName.length() > 30)
             sSysName = sSysName.substring(0, 30);
         pageDE.setAC_ABBR_PREF_NAME(sAbbName);
         pageDE.setAC_SYS_PREF_NAME(sSysName);
         // set pref name accoding to teh type
         if (selNameType.equals("SYS"))
             pageDE.setDE_PREFERRED_NAME(sSysName);
         else if (selNameType.equals("ABBR"))
             pageDE.setDE_PREFERRED_NAME(sAbbName);
         else if (selNameType.equals("USER")) // store the user typeed name
             pageDE.setDE_PREFERRED_NAME(sUsrName);
         // update long name and defintion if dec or vd is searched
         if (!sOrigin.equalsIgnoreCase("openDE")) // (sOrigin.equals("Search"))
         {
             pageDE.setDE_LONG_NAME(sLongName);
             pageDE.setDE_PREFERRED_DEFINITION(sDef);
             pageDE.setDEC_VD_CHANGED(true); // mark as changed
         }
         // update session attributes
         DataManager.setAttribute(session, "m_DE", pageDE);
         return pageDE;
     }
 
     /**
      * changes the dec name type as selected
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param sOrigin
      *            string of origin action of the ac
      * @throws java.lang.Exception
      */
     public void doChangeDENameType(HttpServletRequest req, HttpServletResponse res, @SuppressWarnings("unused") String sOrigin) throws Exception
     {
         HttpSession session = req.getSession();
         // get teh selected type from teh page
         DE_Bean pageDE = (DE_Bean) session.getAttribute("m_DE");
         // capture all other attributes
         m_setAC.setDEValueFromPage(req, res, pageDE);
         String sSysName = pageDE.getAC_SYS_PREF_NAME();
         String sAbbName = pageDE.getAC_ABBR_PREF_NAME();
        // String sUsrName = pageDE.getAC_USER_PREF_NAME();
         String sNameType = (String) req.getParameter("rNameConv");
         if (sNameType == null)
             sNameType = "";
         // logger.debug(sSysName + " name abb " + sAbbName + " name usr " + sUsrName);
         // get the existing preferred name to make sure earlier typed one is saved in the user
         String sPrefName = (String) req.getParameter("txtPreferredName");
         if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)")
                         && !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
             pageDE.setAC_USER_PREF_NAME(sPrefName); // store typed one in de bean
         // reset system generated or abbr accoring
         if (sNameType.equals("SYS"))
             pageDE.setDE_PREFERRED_NAME(sSysName);
         else if (sNameType.equals("ABBR"))
             pageDE.setDE_PREFERRED_NAME(sAbbName);
         else if (sNameType.equals("USER"))
             pageDE.setDE_PREFERRED_NAME(pageDE.getAC_USER_PREF_NAME());
         // store the type in the bean
         pageDE.setAC_PREF_NAME_TYPE(sNameType);
         // logger.debug(pageDE.getAC_PREF_NAME_TYPE() + " pref " + pageDE.getDE_PREFERRED_NAME());
         DataManager.setAttribute(session, "m_DE", pageDE);
     }
 
     /**
      * changes the dec name type as selected
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param sOrigin
      *            string of origin action of the ac
      * @throws java.lang.Exception
      */
     public void doChangeDECNameType(HttpServletRequest req, HttpServletResponse res, @SuppressWarnings("unused") String sOrigin) throws Exception
     {
         HttpSession session = req.getSession();
         // get teh selected type from teh page
         DEC_Bean pageDEC = (DEC_Bean) session.getAttribute("m_DEC");
         m_setAC.setDECValueFromPage(req, res, pageDEC); // capture all other attributes
         String sSysName = pageDEC.getAC_SYS_PREF_NAME();
         String sAbbName = pageDEC.getAC_ABBR_PREF_NAME();
         String sUsrName = pageDEC.getAC_USER_PREF_NAME();
         String sNameType = (String) req.getParameter("rNameConv");
         if (sNameType == null || sNameType.equals(""))
             sNameType = "SYS"; // default
             // logger.debug(sSysName + " name type " + sNameType);
         // get the existing preferred name to make sure earlier typed one is saved in the user
         String sPrefName = (String) req.getParameter("txtPreferredName");
         if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)")
                         && !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
             pageDEC.setAC_USER_PREF_NAME(sPrefName); // store typed one in de bean
         // reset system generated or abbr accoring
         if (sNameType.equals("SYS"))
             pageDEC.setDEC_PREFERRED_NAME(sSysName);
         else if (sNameType.equals("ABBR"))
             pageDEC.setDEC_PREFERRED_NAME(sAbbName);
         else if (sNameType.equals("USER"))
             pageDEC.setDEC_PREFERRED_NAME(sUsrName);
         // store the type in the bean
         pageDEC.setAC_PREF_NAME_TYPE(sNameType);
         // logger.debug(pageDEC.getAC_PREF_NAME_TYPE() + " pref " + pageDEC.getDEC_PREFERRED_NAME());
         DataManager.setAttribute(session, "m_DEC", pageDEC);
     }
 
     /**
      * changes the dec name type as selected
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param sOrigin
      *            string of origin action of the ac
      * @throws java.lang.Exception
      */
     public void doChangeVDNameType(HttpServletRequest req, HttpServletResponse res, @SuppressWarnings("unused") String sOrigin) throws Exception
     {
         HttpSession session = req.getSession();
         // get teh selected type from teh page
         VD_Bean pageVD = (VD_Bean) session.getAttribute("m_VD");
         m_setAC.setVDValueFromPage(req, res, pageVD); // capture all other attributes
         String sSysName = pageVD.getAC_SYS_PREF_NAME();
         String sAbbName = pageVD.getAC_ABBR_PREF_NAME();
         String sUsrName = pageVD.getAC_USER_PREF_NAME();
         String sNameType = (String) req.getParameter("rNameConv");
         if (sNameType == null || sNameType.equals(""))
             sNameType = "SYS"; // default
         // get the existing preferred name to make sure earlier typed one is saved in the user
         String sPrefName = (String) req.getParameter("txtPreferredName");
         if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)")
                         && !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
             pageVD.setAC_USER_PREF_NAME(sPrefName); // store typed one in de bean
         // reset system generated or abbr accoring
         if (sNameType.equals("SYS"))
         {
             if (sSysName == null)
                 sSysName = "";
             // limit to 30 characters
             if (sSysName.length() > 30)
                 sSysName = sSysName.substring(sSysName.length() - 30);
             pageVD.setVD_PREFERRED_NAME(sSysName);
         }
         else if (sNameType.equals("ABBR"))
             pageVD.setVD_PREFERRED_NAME(sAbbName);
         else if (sNameType.equals("USER"))
             pageVD.setVD_PREFERRED_NAME(sUsrName);
         pageVD.setAC_PREF_NAME_TYPE(sNameType); // store the type in the bean
         // logger.debug(pageVD.getAC_PREF_NAME_TYPE() + " pref " + pageVD.getVD_PREFERRED_NAME());
         DataManager.setAttribute(session, "m_VD", pageVD);
     }
 
     /**
      * Does open editDEC page action from DE page called from 'doEditDEActions' method. Calls
      * 'm_setAC.setDEValueFromPage' to store the DE bean for later use Using the DEC idseq, calls 'SerAC.search_DEC'
      * method to gets dec attributes to populate. stores DEC bean in session and opens editDEC page. goes back to editDE
      * page if any error.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doOpenEditDECPage(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         // store the de values in the session
         m_setAC.setDEValueFromPage(req, res, m_DE);
         DataManager.setAttribute(session, "m_DE", m_DE);
         this.clearBuildingBlockSessionAttributes(req, res);
         String sDEC_ID = null;
         String sDECid[] = req.getParameterValues("selDEC");
         if (sDECid != null)
             sDEC_ID = sDECid[0];
         // get the dec bean for this id
         if (sDEC_ID != null)
         {
             Vector vList = new Vector();
             GetACSearch serAC = new GetACSearch(req, res, this);
             serAC.doDECSearch(sDEC_ID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "",
                             "", vList);
             // forward editDEC page with this bean
             if (vList.size() > 0)
             {
                 for (int i = 0; i < vList.size(); i++)
                 {
                     DEC_Bean DECBean = new DEC_Bean();
                     DECBean = (DEC_Bean) vList.elementAt(i);
                     // check if the user has write permission
                     String contID = DECBean.getDEC_CONTE_IDSEQ();
                     String sUser = (String) session.getAttribute("Username");
                     GetACService getAC = new GetACService(req, res, this);
                     String hasPermit = getAC.hasPrivilege("Create", sUser, "dec", contID);
                     // forward to editDEC if has write permission
                     if (hasPermit.equals("Yes"))
                     {
                         DECBean = serAC.getDECAttributes(DECBean, "Edit", "Edit"); // get DEC other Attributes
                         // store the bean in the session attribute
                         DataManager.setAttribute(session, "m_DEC", DECBean);
                         DEC_Bean oldDEC = new DEC_Bean();
                         oldDEC = oldDEC.cloneDEC_Bean(DECBean);
                         DataManager.setAttribute(session, "oldDECBean", oldDEC);
                         ForwardJSP(req, res, "/EditDECPage.jsp"); // forward to editDEC page
                     }
                     // go back to editDE with message if no permission
                     else
                     {
                         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "No edit permission in "
                                         + DECBean.getDEC_CONTEXT_NAME() + " context");
                         ForwardJSP(req, res, "/EditDEPage.jsp"); // forward to editDE page
                     }
                     break;
                 }
             }
             // display error message and back to edit DE page
             else
                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                                 "Unable to get Existing DEConcept attributes from the database");
         }
         // display error message and back to editDE page
         else
         {
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to get the DEConcept id from the page");
             // forward the depage when error occurs
             ForwardJSP(req, res, "/EditDEPage.jsp"); // forward to editDE page
         }
     }
 
     /**
      * Does open editVD page action from DE page called from 'doEditDEActions' method. Calls
      * 'm_setAC.setDEValueFromPage' to store the DE bean for later use Using the VD idseq, calls 'SerAC.search_VD'
      * method to gets dec attributes to populate. stores VD bean in session and opens editVD page. goes back to editDE
      * page if any error.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doOpenEditVDPage(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         // store the de values in the session
         m_setAC.setDEValueFromPage(req, res, m_DE);
         DataManager.setAttribute(session, "m_DE", m_DE);
         this.clearBuildingBlockSessionAttributes(req, res);
         String sVDID = null;
         String sVDid[] = req.getParameterValues("selVD");
         if (sVDid != null)
             sVDID = sVDid[0];
         // get the dec bean for this id
         if (sVDID != null)
         {
             Vector vList = new Vector();
             GetACSearch serAC = new GetACSearch(req, res, this);
             serAC.doVDSearch(sVDID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "", "",
                             "", "", vList);
             // forward editVD page with this bean
             if (vList.size() > 0)
             {
                 for (int i = 0; i < vList.size(); i++)
                 {
                     VD_Bean VDBean = new VD_Bean();
                     VDBean = (VD_Bean) vList.elementAt(i);
                     // check if the user has write permission
                     String contID = VDBean.getVD_CONTE_IDSEQ();
                     String sUser = (String) session.getAttribute("Username");
                     GetACService getAC = new GetACService(req, res, this);
                     String hasPermit = getAC.hasPrivilege("Create", sUser, "vd", contID);
                     // forward to editVD if has write permission
                     if (hasPermit.equals("Yes"))
                     {
                         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
                         VDBean = serAC.getVDAttributes(VDBean, "Edit", sMenuAction); // get VD other Attributes
                         DataManager.setAttribute(session, "m_VD", VDBean);
                         VD_Bean oldVD = new VD_Bean();
                         oldVD = oldVD.cloneVD_Bean(VDBean);
                         DataManager.setAttribute(session, "oldVDBean", oldVD);
                         // DataManager.setAttribute(session, "oldVDBean", VDBean);
                         ForwardJSP(req, res, "/EditVDPage.jsp"); // forward to editVD page
                     }
                     // go back to editDE with message if no permission
                     else
                     {
                         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "No edit permission in "
                                         + VDBean.getVD_CONTEXT_NAME() + " context");
                         ForwardJSP(req, res, "/EditDEPage.jsp"); // forward to editDE page
                     }
                     break;
                 }
             }
             // display error message and back to edit DE page
             else
             {
                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                                 "Unable to get Existing VD attributes from the database");
                 ForwardJSP(req, res, "/EditDEPage.jsp"); // forward to editDE page
             }
         }
         // display error message and back to editDE page
         else
         {
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to get the VDid from the page");
             ForwardJSP(req, res, "/EditDEPage.jsp"); // forward to editDE page
         }
     }// end doEditDECAction
 
     /**
      * The doCreateDECActions method handles CreateDEC or EditDEC actions of the request. Called from 'service' method
      * where reqType is 'newDECfromForm' Calls 'doValidateDEC' if the action is Validate or submit. Calls
      * 'doSuggestionDE' if the action is open EVS Window.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doCreateDECActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) req.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sAction = (String) req.getParameter("pageAction");
         DataManager.setAttribute(session, "DECPageAction", sAction); // store the page action in attribute
         String sSubAction = (String) req.getParameter("DECAction");
         DataManager.setAttribute(session, "DECAction", sSubAction);
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sAction.equals("changeContext"))
             doChangeContext(req, res, "dec");
         else if (sAction.equals("submit"))
             doSubmitDEC(req, res);
         else if (sAction.equals("validate"))
             doValidateDEC(req, res);
         else if (sAction.equals("suggestion"))
             doSuggestionDE(req, res);
         else if (sAction.equals("UseSelection"))
         {
             String nameAction = "newName";
             if (sMenuAction.equals("NewDECTemplate") || sMenuAction.equals("NewDECVersion"))
                 nameAction = "appendName";
             doDECUseSelection(req, res, nameAction);
             ForwardJSP(req, res, "/CreateDECPage.jsp");
             return;
         }
         else if (sAction.equals("RemoveSelection"))
         {
             doRemoveBuildingBlocks(req, res);
             // re work on the naming if new one
             DEC_Bean dec = (DEC_Bean) session.getAttribute("m_DEC");
             if (!sMenuAction.equals("NewDECTemplate") && !sMenuAction.equals("NewDECVersion"))
                 dec = this.doGetDECNames(req, res, null, "Search", dec); // change only abbr pref name
             else
                 dec = this.doGetDECNames(req, res, null, "Remove", dec); // need to change the long name & def also
             DataManager.setAttribute(session, "m_DEC", dec);
             ForwardJSP(req, res, "/CreateDECPage.jsp");
             return;
         }
         else if (sAction.equals("changeNameType"))
         {
             this.doChangeDECNameType(req, res, "changeType");
             ForwardJSP(req, res, "/CreateDECPage.jsp");
         }
         else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(req, res, "DataElementConcept", sAction, "createAC");
         // add, edit and remove contacts
         else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
         {
             DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
             // capture all page attributes
             m_setAC.setDECValueFromPage(req, res, DECBean);
             DECBean.setAC_CONTACTS(this.doContactACUpdates(req, sAction));
             ForwardJSP(req, res, "/CreateDECPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
             DEC_Bean DECBean = (DEC_Bean) session.getAttribute("oldDECBean");
             this.clearBuildingBlockSessionAttributes(req, res);
             String sDECID = DECBean.getDEC_DEC_IDSEQ();
             Vector vList = new Vector();
             // get VD's attributes from the database again
             GetACSearch serAC = new GetACSearch(req, res, this);
             if (sDECID != null && !sDECID.equals(""))
                 serAC.doDECSearch(sDECID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "",
                                 "", "", vList);
             // forward editVD page with this bean
             if (vList.size() > 0)
             {
                 DECBean = (DEC_Bean) vList.elementAt(0);
                 DECBean = serAC.getDECAttributes(DECBean, sOriginAction, sMenuAction);
             }
             else
             // new one
             {
                 DECBean = new DEC_Bean();
                 DECBean.setDEC_ASL_NAME("DRAFT NEW");
                 DECBean.setAC_PREF_NAME_TYPE("SYS");
             }
             DEC_Bean pgBean = new DEC_Bean();
             DataManager.setAttribute(session, "m_DEC", pgBean.cloneDEC_Bean(DECBean));
             ForwardJSP(req, res, "/CreateDECPage.jsp");
         }
         // open the create DE page or search result page
         else if (sAction.equals("backToDE"))
         {
             this.clearCreateSessionAttributes(req, res);
             this.clearBuildingBlockSessionAttributes(req, res);
             if (sMenuAction.equals("NewDECTemplate") || sMenuAction.equals("NewDECVersion"))
             {
                 DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
                 GetACSearch serAC = new GetACSearch(req, res, this);
                 serAC.refreshData(req, res, null, DECBean, null, null, "Refresh", "");
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             else if (sOriginAction.equalsIgnoreCase("CreateNewDECfromEditDE"))
                 ForwardJSP(req, res, "/EditDEPage.jsp");
             else
                 ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
     }
 
     /**
      * The doEditDECActions method handles EditDEC actions of the request. Called from 'service' method where reqType is
      * 'EditDEC' Calls 'ValidateDEC' if the action is Validate or submit. Calls 'doSuggestionDEC' if the action is open
      * EVS Window.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doEditDECActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) req.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sAction = (String) req.getParameter("pageAction");
         DataManager.setAttribute(session, "DECPageAction", sAction); // store the page action in attribute
         String sSubAction = (String) req.getParameter("DECAction");
         DataManager.setAttribute(session, "DECAction", sSubAction);
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sAction.equals("submit"))
             doSubmitDEC(req, res);
         else if (sAction.equals("validate") && sOriginAction.equals("BlockEditDEC"))
             doValidateDECBlockEdit(req, res);
         else if (sAction.equals("validate"))
             doValidateDEC(req, res);
         else if (sAction.equals("suggestion"))
             doSuggestionDE(req, res);
         else if (sAction.equals("UseSelection"))
         {
             String nameAction = "appendName";
             if (sOriginAction.equals("BlockEditDEC"))
                 nameAction = "blockName";
             doDECUseSelection(req, res, nameAction);
             ForwardJSP(req, res, "/EditDECPage.jsp");
             return;
         }
         else if (sAction.equals("RemoveSelection"))
         {
             doRemoveBuildingBlocks(req, res);
             // re work on the naming if new one
             DEC_Bean dec = (DEC_Bean) session.getAttribute("m_DEC");
             dec = this.doGetDECNames(req, res, null, "Remove", dec); // need to change the long name & def also
             DataManager.setAttribute(session, "m_DEC", dec);
             ForwardJSP(req, res, "/EditDECPage.jsp");
             return;
         }
         else if (sAction.equals("changeNameType"))
         {
             this.doChangeDECNameType(req, res, "changeType");
             ForwardJSP(req, res, "/EditDECPage.jsp");
         }
         else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(req, res, "DataElementConcept", sAction, "editAC");
         // add, edit and remove contacts
         else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
         {
             DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
             // capture all page attributes
             m_setAC.setDECValueFromPage(req, res, DECBean);
             DECBean.setAC_CONTACTS(this.doContactACUpdates(req, sAction));
             ForwardJSP(req, res, "/EditDECPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
             DEC_Bean DECBean = (DEC_Bean) session.getAttribute("oldDECBean");
             this.clearBuildingBlockSessionAttributes(req, res);
             String sDECID = DECBean.getDEC_DEC_IDSEQ();
             Vector vList = new Vector();
             // get VD's attributes from the database again
             GetACSearch serAC = new GetACSearch(req, res, this);
             if (sDECID != null && !sDECID.equals(""))
                 serAC.doDECSearch(sDECID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "",
                                 "", "", vList);
             if (vList.size() > 0)
             {
                 DECBean = (DEC_Bean) vList.elementAt(0);
                 // logger.debug("cleared name " + DECBean.getDEC_PREFERRED_NAME());
                 DECBean = serAC.getDECAttributes(DECBean, sOriginAction, sMenuAction);
             }
             DEC_Bean pgBean = new DEC_Bean();
             DataManager.setAttribute(session, "m_DEC", pgBean.cloneDEC_Bean(DECBean));
             ForwardJSP(req, res, "/EditDECPage.jsp");
         }
         // open the create DE page or search result page
         else if (sAction.equals("backToDE"))
         {
             this.clearBuildingBlockSessionAttributes(req, res); // clear session attributes
             if (sOriginAction.equalsIgnoreCase("editDECfromDE"))
                 ForwardJSP(req, res, "/EditDEPage.jsp");
             else if (sMenuAction.equalsIgnoreCase("editDEC") || sOriginAction.equalsIgnoreCase("BlockEditDEC")
                             || sButtonPressed.equals("Search") || sOriginAction.equalsIgnoreCase("EditDEC"))
             {
                 DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
                 GetACSearch serAC = new GetACSearch(req, res, this);
                 serAC.refreshData(req, res, null, DECBean, null, null, "Refresh", "");
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             else
                 ForwardJSP(req, res, "/EditDECPage.jsp");
         }
     }
 
     /**
      * The doCreateVDActions method handles CreateVD or EditVD actions of the request. Called from 'service' method
      * where reqType is 'newVDfromForm' Calls 'doValidateVD' if the action is Validate or submit. Calls 'doSuggestionDE'
      * if the action is open EVS Window.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doCreateVDActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) req.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sAction = (String) req.getParameter("pageAction");
         DataManager.setAttribute(session, "VDPageAction", sAction); // store the page action in attribute
         String sSubAction = (String) req.getParameter("VDAction");
         DataManager.setAttribute(session, "VDAction", sSubAction);
         String sOriginAction = (String) session.getAttribute("originAction");
         System.out.println("create vd " + sAction);
         if (sAction.equals("changeContext"))
             doChangeContext(req, res, "vd");
         else if (sAction.equals("validate"))
         {
             doValidateVD(req, res);
             ForwardJSP(req, res, "/ValidateVDPage.jsp");
         }
         else if (sAction.equals("submit"))
             doSubmitVD(req, res);
         else if (sAction.equals("createPV") || sAction.equals("editPV") || sAction.equals("removePV"))
             doOpenCreatePVPage(req, res, sAction, "createVD");
         else if (sAction.equals("removePVandParent") || sAction.equals("removeParent"))
             doRemoveParent(req, res, sAction, "createVD");
         else if (sAction.equals("searchPV"))
             doSearchPV(req, res);
         else if (sAction.equals("createVM"))
             doOpenCreateVMPage(req, res, "vd");
         else if (sAction.equals("Enum") || sAction.equals("NonEnum"))
         {
             doSetVDPage(req, res, "Create");
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
             /*
              * VD_Bean VDBean = (VD_Bean)session.getAttribute("oldVDBean"); //clear related the session attributes
              * DataManager.setAttribute(session, "VDParentConcept", new Vector()); DataManager.setAttribute(session, "VDPVList", new Vector());
              * this.clearBuildingBlockSessionAttributes(req, res); String sVDID = VDBean.getVD_VD_IDSEQ(); Vector vList =
              * new Vector(); //get VD's attributes from the database again GetACSearch serAC = new GetACSearch(req, res,
              * this); if (sVDID != null && !sVDID.equals("")) serAC.doVDSearch(sVDID, "", "", "", "", "", "", "", "",
              * "", "", "", "", "", "", "", 0, "", "", "", "", "", "", "", vList); //forward editVD page with this bean
              * if (vList.size() > 0) { VDBean = (VD_Bean)vList.elementAt(0); VDBean = serAC.getVDAttributes(VDBean,
              * sOriginAction, sMenuAction); } else { VDBean = new VD_Bean(); VDBean.setVD_ASL_NAME("DRAFT NEW");
              * VDBean.setAC_PREF_NAME_TYPE("SYS"); } VD_Bean pgBean = new VD_Bean(); DataManager.setAttribute(session, "m_VD",
              * pgBean.cloneVD_Bean(VDBean)); ForwardJSP(req, res, "/CreateVDPage.jsp");
              */
             VDServlet vdser = new VDServlet(req, res, this);
             @SuppressWarnings("unused") String ret = vdser.clearEditsOnPage(sOriginAction, sMenuAction); // , "vdEdits");
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         /*
          * else if (sAction.equals("refreshCreateVD")) { doSelectParentVD(req, res); ForwardJSP(req, res,
          * "/CreateVDPage.jsp"); return; }
          */else if (sAction.equals("UseSelection"))
         {
             String nameAction = "newName";
             if (sMenuAction.equals("NewVDTemplate") || sMenuAction.equals("NewVDVersion"))
                 nameAction = "appendName";
             doVDUseSelection(req, res, nameAction);
             ForwardJSP(req, res, "/CreateVDPage.jsp");
             return;
         }
         else if (sAction.equals("RemoveSelection"))
         {
             doRemoveBuildingBlocksVD(req, res);
             // re work on the naming if new one
             VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             if (!sMenuAction.equals("NewVDTemplate") && !sMenuAction.equals("NewVDVersion"))
                 vd = this.doGetVDNames(req, res, null, "Search", vd); // change only abbr pref name
             else
                 vd = this.doGetVDNames(req, res, null, "Remove", vd); // need to change the long name & def also
             DataManager.setAttribute(session, "m_VD", vd);
             ForwardJSP(req, res, "/CreateVDPage.jsp");
             return;
         }
         else if (sAction.equals("changeNameType"))
         {
             this.doChangeVDNameType(req, res, "changeType");
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         /*
          * else if (sAction.equals("CreateNonEVSRef")) { doNonEVSReference(req, res); ForwardJSP(req, res,
          * "/CreateVDPage.jsp"); }
          */else if (sAction.equals("addSelectedCon"))
         {
             doSelectVMConcept(req, res, sAction);
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         else if (sAction.equals("sortPV"))
         {
             GetACSearch serAC = new GetACSearch(req, res, this);
             String sField = (String) req.getParameter("pvSortColumn");
             serAC.getVDPVSortedRows(sField); // call the method to sort pv attribute
             ForwardJSP(req, res, "/CreateVDPage.jsp");
             return;
         }
         else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(req, res, "ValueDomain", sAction, "createAC");
         // add/edit or remove contacts
         else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
         {
             VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
             // capture all page attributes
             m_setAC.setVDValueFromPage(req, res, VDBean);
             VDBean.setAC_CONTACTS(this.doContactACUpdates(req, sAction));
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         // open the DE page or search page with
         else if (sAction.equals("goBack"))
         {
             VDServlet vdser = new VDServlet(req, res, this);
             String sFor = vdser.goBackfromVD(sOriginAction, sMenuAction, "", "", "create");
             ForwardJSP(req, res, sFor);
         }
         else if (sAction.equals("vdpvstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "PV");
             doValidateVD(req, res);
             ForwardJSP(req, res, "/PermissibleValue.jsp");
         }
         else if (sAction.equals("vddetailstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "VD");
             doValidateVD(req, res);
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
     }
 
     /**
      * The doEditDEActions method handles EditDE actions of the request. Called from 'service' method where reqType is
      * 'EditVD' Calls 'ValidateDE' if the action is Validate or submit. Calls 'doSuggestionDE' if the action is open EVS
      * Window.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doEditVDActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) req.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sAction = (String) req.getParameter("pageAction");
         DataManager.setAttribute(session, "VDPageAction", sAction); // store the page action in attribute
         String sSubAction = (String) req.getParameter("VDAction");
         DataManager.setAttribute(session, "VDAction", sSubAction);
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         String sSearchAC = (String) session.getAttribute("SearchAC");
         if (sSearchAC == null)
             sSearchAC = "";
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sAction.equals("submit"))
             doSubmitVD(req, res);
         else if (sAction.equals("validate") && sOriginAction.equals("BlockEditVD"))
             doValidateVDBlockEdit(req, res);
         else if (sAction.equals("validate"))
         {
             doValidateVD(req, res);
             ForwardJSP(req, res, "/ValidateVDPage.jsp");
         }
         else if (sAction.equals("suggestion"))
             doSuggestionDE(req, res);
         else if (sAction.equals("UseSelection"))
         {
             String nameAction = "appendName";
             if (sOriginAction.equals("BlockEditVD"))
                 nameAction = "blockName";
             doVDUseSelection(req, res, nameAction);
             ForwardJSP(req, res, "/EditVDPage.jsp");
             return;
         }
         else if (sAction.equals("RemoveSelection"))
         {
             doRemoveBuildingBlocksVD(req, res);
             // re work on the naming if new one
             VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             vd = this.doGetVDNames(req, res, null, "Remove", vd); // change only abbr pref name
             DataManager.setAttribute(session, "m_VD", vd);
             ForwardJSP(req, res, "/EditVDPage.jsp");
             return;
         }
         else if (sAction.equals("changeNameType"))
         {
             this.doChangeVDNameType(req, res, "changeType");
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         else if (sAction.equals("sortPV"))
         {
             GetACSearch serAC = new GetACSearch(req, res, this);
             String sField = (String) req.getParameter("pvSortColumn");
             serAC.getVDPVSortedRows(sField); // call the method to sort pv attribute
             ForwardJSP(req, res, "/EditVDPage.jsp");
             return;
         }
         else if (sAction.equals("createPV") || sAction.equals("editPV") || sAction.equals("removePV"))
             doOpenCreatePVPage(req, res, sAction, "editVD");
         else if (sAction.equals("removePVandParent") || sAction.equals("removeParent"))
             doRemoveParent(req, res, sAction, "editVD");
         else if (sAction.equals("addSelectedCon"))
         {
             doSelectVMConcept(req, res, sAction);
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         else if (sAction.equals("Enum") || sAction.equals("NonEnum"))
         {
             doSetVDPage(req, res, "Edit");
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(req, res, "ValueDomain", sAction, "editAC");
         // add/edit or remove contacts
         else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
         {
             VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
             // capture all page attributes
             m_setAC.setVDValueFromPage(req, res, VDBean);
             VDBean.setAC_CONTACTS(this.doContactACUpdates(req, sAction));
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
             VDServlet vdser = new VDServlet(req, res, this);
             @SuppressWarnings("unused") String ret = vdser.clearEditsOnPage(sOriginAction, sMenuAction); // , "vdEdits");
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         // open the Edit DE page or search page with
         else if (sAction.equals("goBack"))
         {
             VDServlet vdser = new VDServlet(req, res, this);
             String sFor = vdser.goBackfromVD(sOriginAction, sMenuAction, sSearchAC, sButtonPressed, "edit");
             ForwardJSP(req, res, sFor);
         }
         else if (sAction.equals("vdpvstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "PV");
             doValidateVD(req, res);
             ForwardJSP(req, res, "/PermissibleValue.jsp");
         }
         else if (sAction.equals("vddetailstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "VD");
             doValidateVD(req, res);
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
     }
 
     /**
      * The doCreatePVActions method handles the submission of a CreatePV form Called from DON'T KNOW Calls
      * 'doValidatePV' if the action is Validate or submit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     public void doCreatePVActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sAction = (String) req.getParameter("pageAction");
         String sMenuAction = (String) req.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
        // String sOriginAction = (String) session.getAttribute("originAction");
         // clear searched data from teh session attributes
         /*
          * if (sAction.equals("submit")) doSubmitPV(req, res); else if (sAction.equals("validate")) doValidatePV(req,
          * res); else
          */if (sAction.equals("createNewVM"))
             this.doOpenCreateVMPage(req, res, "vm");
         // store vm attributes in pv bean
         else if (sAction.equals("appendSearchVM"))
         {
             PV_Bean pvBean = (PV_Bean) session.getAttribute("m_PV");
             if (pvBean == null)
                 pvBean = new PV_Bean();
             SetACService setAC = new SetACService(this);
             setAC.setPVValueFromPage(req, res, pvBean);
             String selVM = pvBean.getPV_SHORT_MEANING();
             Vector vRSel = (Vector) session.getAttribute("vACSearch");
             if (vRSel == null)
                 vRSel = new Vector();
             for (int i = 0; i < (vRSel.size()); i++)
             {
                 VM_Bean vmBean = (VM_Bean) vRSel.elementAt(i);
                 // store the vm attributes in pv attribute
                 if (vmBean.getVM_SHORT_MEANING().equals(selVM))
                 {
                     pvBean.setPV_MEANING_DESCRIPTION(vmBean.getVM_DESCRIPTION());
                     // pvBean.setVM_CONCEPT(vmBean.getVM_CONCEPT());
                     pvBean.setPV_VM(vmBean);
                     break;
                 }
             }
             DataManager.setAttribute(session, "m_PV", pvBean);
             ForwardJSP(req, res, "/CreatePVPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
             PV_Bean pvOpen = (PV_Bean) session.getAttribute("pageOpenBean");
             PV_Bean pvBean = (PV_Bean) session.getAttribute("m_PV");
             if (pvOpen != null)
                 pvBean = pvBean.copyBean(pvOpen);
             DataManager.setAttribute(session, "m_PV", pvBean);
             ForwardJSP(req, res, "/CreatePVPage.jsp");
         }
         else if (sAction.equals("backToVD"))
         {
             // set the checked property to false
             Vector<PV_Bean> vOldVDPVList = (Vector) session.getAttribute("oldVDPVList");
             if (vOldVDPVList != null)
             {
                 for (int i = 0; i < vOldVDPVList.size(); i++)
                 {
                     PV_Bean oldPV = (PV_Bean) vOldVDPVList.elementAt(i);
                     // System.out.println(oldPV.getPV_BEGIN_DATE() + " back vd old " + oldPV.getPV_END_DATE());
                     oldPV.setPV_CHECKED(false);
                     vOldVDPVList.setElementAt(oldPV, i);
                 }
                 DataManager.setAttribute(session, "VDPVList", vOldVDPVList);
             }
             if (sMenuAction.equals("editVD") || sMenuAction.equals("editDE") || sButtonPressed.equals("Search"))
                 ForwardJSP(req, res, "/EditVDPage.jsp"); // back to Edit VD Screen
             else
                 ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
     }
 
     /**
      * The doEditPVActions method handles the submission of a CreatePV form Calls 'doValidatePV' if the action is
      * Validate or submit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doEditPVActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         PVServlet pvSer = new PVServlet(req, res, this);
         String pageFor = pvSer.doEditPVActions();
         if (pageFor != null && !pageFor.equals(""))
         {
             if (pageFor.charAt(0) != '/')
                 pageFor = "/" + pageFor;
             ForwardJSP(req, res, pageFor);
         }
     }
 
     /**
      * The doEditVMActions method handles the submission of a CreatePV form Calls 'doValidateVM' if the action is
      * Validate or submit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doEditVMActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         VMServlet vmSer = new VMServlet(req, res, this);
         String pageFor = vmSer.run(); // doEditVMActions();
         if (pageFor != null && !pageFor.equals(""))
         {
             if (pageFor.charAt(0) != '/')
                 pageFor = "/" + pageFor;
             ForwardJSP(req, res, pageFor);
         }
     }
 
     /**
      * adds and removes alternate names and reference documents from the vectors
      *
      * @param req
      * @param pageAct
      * @throws java.lang.Exception
      */
     public void doMarkAddRemoveDesignation(HttpServletRequest req, String pageAct) throws Exception
     {
         // call methods for different actions
         if (pageAct.equals("addAlt")) // do add alt name action
             this.doMarkAddAltNames(req);
         else if (pageAct.equals("addRefDoc")) // do add ref doc action
             this.doMarkAddRefDocs(req);
         else if (pageAct.equals("removeAlt")) // remove alt names
             this.doMarkRemoveAltNames(req);
         else if (pageAct.equals("removeRefDoc")) // remove refernece documents
             this.doMarkRemoveRefDocs(req);
     }
 
     /**
      * adds alternate names to the vectors
      *
      * @param req
      * @throws java.lang.Exception
      */
     @SuppressWarnings("unchecked")
     private void doMarkAddAltNames(HttpServletRequest req) throws Exception
     {
         HttpSession session = req.getSession();
        // InsACService insAC = new InsACService(m_classReq, m_classRes, this);
        // String stgContMsg = "";
         // get the sessin vectors
         Vector<ALT_NAME_Bean> vAltNames = (Vector) session.getAttribute("AllAltNameList");
         Vector vContext = (Vector) session.getAttribute("vWriteContextDE");
         if (vContext == null)
             vContext = new Vector();
         // add alternate names
         String selName = (String) req.getParameter("txtAltName");
         if (selName == null)
             selName = "";
         selName = selName.trim();
         if (selName.equals(""))
         {
             storeStatusMsg("Please enter a text for the alternate name");
             return;
         }
         // get the request vectors
         Vector<String> vACId = (Vector) session.getAttribute("vACId");
         if (vACId == null)
             vACId = new Vector<String>();
         Vector<String> vACName = (Vector) session.getAttribute("vACName");
         if (vACName == null)
             vACName = new Vector<String>();
         String sContID = (String) req.getParameter("selContext");
         String sContext = (String) req.getParameter("contextName");
         if (sContID != null)
             req.setAttribute("desContext", sContID);
         String sLang = (String) req.getParameter("dispLanguage");
         if (sLang != null)
             req.setAttribute("desLang", sLang);
         String selType = (String) req.getParameter("selAltType");
         // handle the context and ac name for new AC (DE, DEC and VD)
         if (vACId.size() < 1)
             vACId.addElement("new");
         if (vACName.size() < 1)
             vACName.addElement("new");
         if (sContID == null || sContID.equals(""))
             sContID = "new";
         // continue with acitons
         for (int i = 0; i < vACId.size(); i++)
         {
             // get ac names
             String acID = (String) vACId.elementAt(i);
             if (acID == null)
                 acID = "";
             String acName = "";
             if (vACName.size() > i)
                 acName = (String) vACName.elementAt(i);
             // get page attributes
             // check if another record with same type, name, ac and context exists already
             boolean isExist = false;
             for (int k = 0; k < vAltNames.size(); k++)
             {
                 ALT_NAME_Bean altBean = (ALT_NAME_Bean) vAltNames.elementAt(k);
                 // check if it was existed in the list already
                 if (altBean.getALT_TYPE_NAME().equals(selType) && altBean.getALTERNATE_NAME().equals(selName)
                                 && altBean.getCONTE_IDSEQ().equals(sContID) && altBean.getAC_IDSEQ().equals(acID))
                 {
                     // change the submit action if deleted
                     if (altBean.getALT_SUBMIT_ACTION().equals("DEL"))
                     {
                         // mark it as ins if new one or upd if old one
                         String altID = altBean.getALT_NAME_IDSEQ();
                         if (altID == null || altID.equals("") || altID.equals("new"))
                             altBean.setALT_SUBMIT_ACTION("INS");
                         else
                             altBean.setALT_SUBMIT_ACTION("UPD");
                         vAltNames.setElementAt(altBean, k);
                     }
                     isExist = true;
                 }
             }
             // add new one if not existed in teh bean already
             if (isExist == false)
             {
                 // fill in the bean and vector
                 ALT_NAME_Bean AltNameBean = new ALT_NAME_Bean();
                 AltNameBean.setALT_NAME_IDSEQ("new");
                 AltNameBean.setCONTE_IDSEQ(sContID);
                 AltNameBean.setCONTEXT_NAME(sContext);
                 AltNameBean.setALTERNATE_NAME(selName);
                 AltNameBean.setALT_TYPE_NAME(selType);
                 AltNameBean.setAC_LONG_NAME(acName);
                 AltNameBean.setAC_IDSEQ(acID);
                 AltNameBean.setAC_LANGUAGE(sLang);
                 AltNameBean.setALT_SUBMIT_ACTION("INS");
                 vAltNames.addElement(AltNameBean); // add the bean to a vector
             }
         }
         DataManager.setAttribute(session, "AllAltNameList", vAltNames);
     }
 
     /**
      * removes alternate names from the vectors
      *
      * @param req
      * @throws java.lang.Exception
      */
     @SuppressWarnings("unchecked")
     private void doMarkRemoveAltNames(HttpServletRequest req) throws Exception
     {
         HttpSession session = req.getSession();
        // InsACService insAC = new InsACService(m_classReq, m_classRes, this);
         String stgContMsg = "";
         // get the sessin vectors
         Vector<ALT_NAME_Bean> vAltNames = (Vector) session.getAttribute("AllAltNameList");
         Vector<String> vContext = (Vector) session.getAttribute("vWriteContextDE");
         if (vContext == null)
             vContext = new Vector<String>();
         String sContID = (String) req.getParameter("selContext");
         if (sContID != null)
             req.setAttribute("desContext", sContID);
         int j = -1; // to keep track of number of items on the page
         Vector<String> vAltAttrs = new Vector<String>();
         for (int i = 0; i < vAltNames.size(); i++)
         {
             ALT_NAME_Bean aBean = (ALT_NAME_Bean) vAltNames.elementAt(i);
             if (!aBean.getALT_SUBMIT_ACTION().equals("DEL"))
             {
                 String altName = aBean.getALTERNATE_NAME();
                 String altType = aBean.getALT_TYPE_NAME();
                 String altCont = aBean.getCONTEXT_NAME();
                 // go to next record if same type, name and context does exist
                 String curAltAttr = altType + " " + altName + " " + altCont;
                 // increase teh count only if it didn't exist in the disp vecot list
                 if (!vAltAttrs.contains(curAltAttr))
                 {
                     vAltAttrs.addElement(curAltAttr);
                     j += 1;
                 }
                 String ckItem = (String) req.getParameter("ACK" + j);
                 // get the right selected item to mark as deleted
                 if (ckItem != null)
                 {
                     if (vContext.contains(altCont) || altCont.equals("") || altCont.equalsIgnoreCase("new"))
                     {
                         aBean.setALT_SUBMIT_ACTION("DEL");
                         vAltNames.setElementAt(aBean, i);
                         // check if another record with same type, name and context but diff ac exists to remove
                         for (int k = 0; k < vAltNames.size(); k++)
                         {
                             ALT_NAME_Bean altBean = (ALT_NAME_Bean) vAltNames.elementAt(k);
                             if (!altBean.getALT_SUBMIT_ACTION().equals("DEL")
                                             && altBean.getALTERNATE_NAME().equals(altName))
                             {
                                 if (altBean.getALT_TYPE_NAME().equals(altType)
                                                 && altBean.getCONTEXT_NAME().equals(altCont))
                                 {
                                     altBean.setALT_SUBMIT_ACTION("DEL"); // mark them also deleted
                                     vAltNames.setElementAt(altBean, k);
                                 }
                             }
                         }
                     }
                     else
                         stgContMsg += "\\n\\t" + altName + " in " + altCont + " Context ";
                     break;
                 }
             }
         }
         if (stgContMsg != null && !stgContMsg.equals(""))
             storeStatusMsg("Unable to remove the following Alternate Names, because the user does not have write permission to remove "
                                             + stgContMsg);
         DataManager.setAttribute(session, "AllAltNameList", vAltNames);
     } // end remove alt names
 
     /**
      * adds reference documents to the vectors
      *
      * @param req
      * @throws java.lang.Exception
      */
     @SuppressWarnings("unchecked")
     private void doMarkAddRefDocs(HttpServletRequest req) throws Exception
     {
         HttpSession session = req.getSession();
        // InsACService insAC = new InsACService(m_classReq, m_classRes, this);
         String selName = (String) req.getParameter("txtRefName");
         if (selName == null)
             selName = "";
         selName = selName.trim();
         if (selName.equals(""))
         {
             storeStatusMsg("Please enter a text for the alternate name");
             return;
         }
         // continue with adding
        // String stgContMsg = "";
         Vector<REF_DOC_Bean> vRefDocs = (Vector) session.getAttribute("AllRefDocList");
         Vector<String> vACId = (Vector) session.getAttribute("vACId");
         if (vACId == null)
             vACId = new Vector<String>();
         Vector<String> vACName = (Vector) session.getAttribute("vACName");
         if (vACName == null)
             vACName = new Vector<String>();
         Vector vContext = (Vector) session.getAttribute("vWriteContextDE");
         if (vContext == null)
             vContext = new Vector();
         // get request attributes
         String sContID = (String) req.getParameter("selContext");
         String sContext = (String) req.getParameter("contextName");
         if (sContID != null)
             req.setAttribute("desContext", sContID);
         String sLang = (String) req.getParameter("dispLanguage");
         if (sLang != null)
             req.setAttribute("desLang", sLang);
         String selType = (String) req.getParameter("selRefType");
         String selText = (String) req.getParameter("txtRefText");
         String selUrl = (String) req.getParameter("txtRefURL");
         // handle the context and ac name for new AC (DE, DEC and VD)
         if (vACId.size() < 1)
             vACId.addElement("new");
         if (vACName.size() < 1)
             vACName.addElement("new");
         if (sContID == null || sContID.equals(""))
             sContID = "new";
         // do add ref doc action
         for (int i = 0; i < vACId.size(); i++)
         {
             // get ac names
             String acID = (String) vACId.elementAt(i);
             if (acID == null)
                 acID = "";
             String acName = "";
             if (vACName.size() > i)
                 acName = (String) vACName.elementAt(i);
             // check if another record with same type, name, ac and context exists already
             boolean isExist = false;
             for (int k = 0; k < vRefDocs.size(); k++)
             {
                 REF_DOC_Bean refBean = (REF_DOC_Bean) vRefDocs.elementAt(k);
                 // check if it was existed in the list already
                 if (refBean.getDOC_TYPE_NAME().equals(selType) && refBean.getDOCUMENT_NAME().equals(selName)
                                 && refBean.getCONTE_IDSEQ().equals(sContID) && refBean.getAC_IDSEQ().equals(acID))
                 {
                     // change the submit action if deleted
                     if (refBean.getREF_SUBMIT_ACTION().equals("DEL"))
                     {
                         // mark it as ins if new one or upd if old one
                         String refID = refBean.getREF_DOC_IDSEQ();
                         if (refID == null || refID.equals("") || refID.equals("new"))
                             refBean.setREF_SUBMIT_ACTION("INS");
                         else
                             refBean.setREF_SUBMIT_ACTION("UPD");
                         vRefDocs.setElementAt(refBean, k);
                     }
                     isExist = true;
                 }
             }
             // add new one if not existed in teh bean already
             if (isExist == false)
             {
                 // fill in the bean and vector
                 REF_DOC_Bean RefDocBean = new REF_DOC_Bean();
                 RefDocBean.setAC_IDSEQ(acID);
                 RefDocBean.setAC_LONG_NAME(acName);
                 RefDocBean.setREF_DOC_IDSEQ("new");
                 RefDocBean.setDOCUMENT_NAME(selName);
                 RefDocBean.setDOC_TYPE_NAME(selType);
                 RefDocBean.setDOCUMENT_TEXT(selText);
                 RefDocBean.setDOCUMENT_URL(selUrl);
                 RefDocBean.setCONTE_IDSEQ(sContID);
                 RefDocBean.setCONTEXT_NAME(sContext);
                 RefDocBean.setAC_LANGUAGE(sLang);
                 RefDocBean.setREF_SUBMIT_ACTION("INS");
                 vRefDocs.addElement(RefDocBean); // add the bean to a vector
             }
         }
         DataManager.setAttribute(session, "AllRefDocList", vRefDocs);
     } // end add ref docs
 
     /**
      * removes reference documents from the vectors
      *
      * @param req
      * @throws java.lang.Exception
      */
     @SuppressWarnings("unchecked")
     private void doMarkRemoveRefDocs(HttpServletRequest req) throws Exception
     {
         HttpSession session = req.getSession();
         //InsACService insAC = new InsACService(m_classReq, m_classRes, this);
         String stgContMsg = "";
         Vector<REF_DOC_Bean> vRefDocs = (Vector) session.getAttribute("AllRefDocList");
         Vector vContext = (Vector) session.getAttribute("vWriteContextDE");
         if (vContext == null)
             vContext = new Vector();
         String sContID = (String) req.getParameter("selContext");
         if (sContID != null)
             req.setAttribute("desContext", sContID);
         int j = -1; // to keep track of number of items on the page
         Vector<String> vRefAttrs = new Vector<String>();
         for (int i = 0; i < vRefDocs.size(); i++)
         {
             REF_DOC_Bean rBean = (REF_DOC_Bean) vRefDocs.elementAt(i);
           //  String refID = rBean.getREF_DOC_IDSEQ();
             if (!rBean.getREF_SUBMIT_ACTION().equals("DEL"))
             {
                 String refName = rBean.getDOCUMENT_NAME();
                 String refType = rBean.getDOC_TYPE_NAME();
                 String refCont = rBean.getCONTEXT_NAME();
                 // go to next record if same type, name and context does exist
                 String curRefAttr = refType + " " + refName + " " + refCont;
                 // increase teh count only if it didn't exist in the disp vecot list
                 if (!vRefAttrs.contains(curRefAttr))
                 {
                     vRefAttrs.addElement(curRefAttr);
                     j += 1; // increase the count
                 }
                 String ckItem = (String) req.getParameter("RCK" + j);
                 // get the right selected item to mark as deleted
                 if (ckItem != null)
                 {
                     if (vContext.contains(refCont) || refCont.equals("") || refCont.equalsIgnoreCase("new"))
                     {
                         rBean.setREF_SUBMIT_ACTION("DEL");
                         vRefDocs.setElementAt(rBean, i);
                         // check if another record with same type, name and context but diff ac exists to remove
                         for (int k = 0; k < vRefDocs.size(); k++)
                         {
                             REF_DOC_Bean refBean = (REF_DOC_Bean) vRefDocs.elementAt(k);
                             if (!refBean.getREF_SUBMIT_ACTION().equals("DEL")
                                             && refBean.getDOCUMENT_NAME().equals(refName))
                             {
                                 if (refBean.getDOC_TYPE_NAME().equals(refType)
                                                 && refBean.getCONTEXT_NAME().equals(refCont))
                                 {
                                     refBean.setREF_SUBMIT_ACTION("DEL"); // mark them also deleted
                                     vRefDocs.setElementAt(refBean, k);
                                 }
                             }
                         }
                     }
                     else
                         stgContMsg += "\\n\\t" + refName + " in " + refCont + " Context ";
                     break;
                 }
             }
         }
         if (stgContMsg != null && !stgContMsg.equals(""))
             storeStatusMsg("Unable to remove the following Reference Documents, because the user does not have write permission to remove "
                                             + stgContMsg);
         DataManager.setAttribute(session, "AllRefDocList", vRefDocs);
     } // end remove ref doc
 
     /**
      * stores altname and reference documetns created while maintaining ac in the ac bean
      *
      * @param req
      *            HttpServletRequest request object
      * @param res
      *            HttpServletResponse response object
      * @param sAC
      *            maintained ac
      * @param sType
      *            type whether alt name or ref doc
      * @param sACAct
      *            is ac edit or create
      */
     public void doMarkACBeanForAltRef(HttpServletRequest req, HttpServletResponse res, String sAC, String sType,
                     String sACAct)
     {
         HttpSession session = (HttpSession) req.getSession();
         if (sACAct.equals("openAR") || sACAct.equals("submitAR"))
         {
             Vector vRefDoc = new Vector(), vAltName = new Vector();
             // get the alt names and ref docs from teh bean
             if (sAC.equals("DataElement"))
             {
                 DE_Bean de = (DE_Bean) session.getAttribute("m_DE");
                 vAltName = (Vector) de.getAC_ALT_NAMES();
                 vRefDoc = (Vector) de.getAC_REF_DOCS();
             }
             if (sAC.equals("DataElementConcept"))
             {
                 DEC_Bean dec = (DEC_Bean) session.getAttribute("m_DEC");
                 vAltName = dec.getAC_ALT_NAMES();
                 vRefDoc = dec.getAC_REF_DOCS();
             }
             if (sAC.equals("ValueDomain"))
             {
                 VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
                 vAltName = vd.getAC_ALT_NAMES();
                 vRefDoc = vd.getAC_REF_DOCS();
             }
             // store the alt name and ref doc in the session
             if (vAltName == null)
                 vAltName = new Vector();
             Vector<ALT_NAME_Bean> vAllAltName = new Vector<ALT_NAME_Bean>();
             for (int i = 0; i < vAltName.size(); i++)
             {
                 ALT_NAME_Bean thisAlt = new ALT_NAME_Bean();
                 thisAlt = thisAlt.copyAltNames((ALT_NAME_Bean) vAltName.elementAt(i));
                 vAllAltName.addElement(thisAlt);
             }
             DataManager.setAttribute(session, "AllAltNameList", vAllAltName);
             if (vRefDoc == null)
                 vRefDoc = new Vector();
             Vector<REF_DOC_Bean> vAllRefDoc = new Vector<REF_DOC_Bean>();
             for (int i = 0; i < vRefDoc.size(); i++)
             {
                 REF_DOC_Bean thisDoc = new REF_DOC_Bean();
                 thisDoc = thisDoc.copyRefDocs((REF_DOC_Bean) vRefDoc.elementAt(i));
                 vAllRefDoc.addElement(thisDoc);
             }
             DataManager.setAttribute(session, "AllRefDocList", vAllRefDoc);
         }
         else
         {
             sType = sType.replace("Store ", ""); // remove word store from the string to use later
             Vector vAllRefDoc = (Vector) session.getAttribute("AllRefDocList");
             Vector vAllAltName = (Vector) session.getAttribute("AllAltNameList");
             if (sAC.equals("DataElement"))
             {
                 // System.out.println("checking de bean");
                 // stroe it in the bean
                 DE_Bean de = (DE_Bean) session.getAttribute("m_DE");
                 m_setAC.setDEValueFromPage(req, res, de); // capture all other page or request attributes
                 if (sType.equals("Alternate Names"))
                     de.setAC_ALT_NAMES((Vector) vAllAltName);
                 else if (sType.equals("Reference Documents"))
                     de.setAC_REF_DOCS((Vector) vAllRefDoc);
                 // update session and forward
                 DataManager.setAttribute(session, "m_DE", de);
                 if (sACAct.equals("createAC"))
                     ForwardJSP(req, res, "/CreateDEPage.jsp");
                 else if (sACAct.equals("editAC"))
                     ForwardJSP(req, res, "/EditDEPage.jsp");
             }
             else if (sAC.equals("DataElementConcept"))
             {
                 // stroe it in the bean
                 DEC_Bean dec = (DEC_Bean) session.getAttribute("m_DEC");
                 m_setAC.setDECValueFromPage(req, res, dec); // capture all other page or request attributes
                 if (sType.equals("Alternate Names"))
                     dec.setAC_ALT_NAMES(vAllAltName);
                 else if (sType.equals("Reference Documents"))
                     dec.setAC_REF_DOCS(vAllRefDoc);
                 // update session and forward
                 DataManager.setAttribute(session, "m_DEC", dec);
                 if (sACAct.equals("createAC"))
                     ForwardJSP(req, res, "/CreateDECPage.jsp");
                 else if (sACAct.equals("editAC"))
                     ForwardJSP(req, res, "/EditDECPage.jsp");
             }
             else if (sAC.equals("ValueDomain"))
             {
                 // stroe it in the bean
                 VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
                 m_setAC.setVDValueFromPage(req, res, vd); // capture all other page or request attributes
                 if (sType.equals("Alternate Names"))
                     vd.setAC_ALT_NAMES(vAllAltName);
                 else if (sType.equals("Reference Documents"))
                     vd.setAC_REF_DOCS(vAllRefDoc);
                 // update session and forward
                 DataManager.setAttribute(session, "m_VD", vd);
                 if (sACAct.equals("createAC"))
                     ForwardJSP(req, res, "/CreateVDPage.jsp");
                 else if (sACAct.equals("editAC"))
                     ForwardJSP(req, res, "/EditVDPage.jsp");
             }
         }
     }
 
     /**
      * The doChangeContext method resets the bean then forwards to Create page Called from 'doCreateDEActions',
      * 'doCreateDECActions', 'doCreateVDActions' methods. Calls 'getAC.getACList' if the action is Validate or submit.
      * Stores empty bean and forwards the create page of the selected component. Stores selected context as default
      * session. If the create DEC and VD origin is DE, only store the new context in the bean.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sACType
      *            String ac type
      *
      * @throws Exception
      */
     public void doChangeContext(HttpServletRequest req, HttpServletResponse res, String sACType) throws Exception
     {
         UtilService util = new UtilService();
         HttpSession session = req.getSession();
         String sContextID = (String) req.getParameter("selContext");
         String sNewContext = util.getNameByID((Vector) session.getAttribute("vContext"), (Vector) session
                         .getAttribute("vContext_ID"), sContextID);
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sOriginAction == null)
             sOriginAction = "";
         boolean bNewContext = true;
       //  boolean bDE = sACType.equals("de");
         if (sNewContext != null)
         {
             GetACService getAC = new GetACService(req, res, this);
             getAC.getACList(req, res, sNewContext, bNewContext, sACType);
         }
         if (sACType.equals("de"))
         {
             DataManager.setAttribute(session, "sDefaultContext", sNewContext);
             DataManager.setAttribute(session, "m_DE", new DE_Bean());
             ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else if (sACType.equals("dec"))
         {
             DEC_Bean m_DEC = new DEC_Bean();
             if (sOriginAction.equals("CreateNewDEC"))
                 m_DEC.setDEC_CONTEXT_NAME(sNewContext);
             else
                 DataManager.setAttribute(session, "sDefaultContext", sNewContext);
             DataManager.setAttribute(session, "m_DEC", m_DEC);
             ForwardJSP(req, res, "/CreateDECPage.jsp");
         }
         else if (sACType.equals("vd"))
         {
             VD_Bean m_VD = new VD_Bean();
             if (sOriginAction.equals("CreateNewVD"))
                 m_VD.setVD_CONTEXT_NAME(sNewContext);
             else
                 DataManager.setAttribute(session, "sDefaultContext", sNewContext);
             DataManager.setAttribute(session, "m_VD", m_VD);
             ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
     }
 
     /**
      * Called from doCreateDEActions. Calls 'setAC.setDEValueFromPage' to set the DEC data from the page. Calls
      * 'setAC.setValidatePageValuesDE' to validate the data. Loops through the vector vValidate to check if everything
      * is valid and Calls 'doInsertDE' to insert the data. If vector contains invalid fields, forwards to validation
      * page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doSubmitDE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "sCDEAction", "validate");
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         GetACService getAC = new GetACService(req, res, this);
         m_setAC.setDEValueFromPage(req, res, m_DE);
         m_setAC.setValidatePageValuesDE(req, res, m_DE, getAC);
         DataManager.setAttribute(session, "m_DE", m_DE);
         boolean isValid = true;
         boolean isValidFlag = true;
         Vector vValidate = new Vector();
         vValidate = (Vector) req.getAttribute("vValidate");
         if (vValidate == null)
             isValid = false;
         else
         {
             for (int i = 0; vValidate.size() > i; i = i + 3)
             {
                 String sStat = (String) vValidate.elementAt(i + 2);
                 if ((sStat.equals("Valid") || sStat
                                 .equals("Warning: A Data Element with a duplicate DEC and VD pair already exists within the selected context. ")))
                     isValid = true; // this just keeps the status quo
                 else
                     isValidFlag = false; // we have true failure here
             }
             isValid = isValidFlag;
         }
         if (isValid == false)
         {
             ForwardJSP(req, res, "/ValidateDEPage.jsp");
         }
         else
         {
             doInsertDE(req, res);
         }
     } // end of doSumitDE
 
     /**
      * Called from doCreateDECActions. Calls 'setAC.setDECValueFromPage' to set the DEC data from the page. Calls
      * 'setAC.setValidatePageValuesDEC' to validate the data. Loops through the vector vValidate to check if everything
      * is valid and Calls 'doInsertDEC' to insert the data. If vector contains invalid fields, forwards to validation
      * page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doSubmitDEC(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "sDECAction", "validate");
         DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
         if (m_DEC == null)
             m_DEC = new DEC_Bean();
         EVS_Bean m_OC = new EVS_Bean();
         EVS_Bean m_PC = new EVS_Bean();
         EVS_Bean m_OCQ = new EVS_Bean();
         EVS_Bean m_PCQ = new EVS_Bean();
         GetACService getAC = new GetACService(req, res, this);
         m_setAC.setDECValueFromPage(req, res, m_DEC);
         m_OC = (EVS_Bean) session.getAttribute("m_OC");
         m_PC = (EVS_Bean) session.getAttribute("m_PC");
         m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
         m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
         m_setAC.setValidatePageValuesDEC(req, res, m_DEC, m_OC, m_PC, getAC, m_OCQ, m_PCQ);
         DataManager.setAttribute(session, "m_DEC", m_DEC);
         boolean isValid = true;
         Vector vValidate = new Vector();
         vValidate = (Vector) req.getAttribute("vValidate");
         if (vValidate == null)
             isValid = false;
         else
         {
             for (int i = 0; vValidate.size() > i; i = i + 3)
             {
                 String sStat = (String) vValidate.elementAt(i + 2);
                 if (sStat.equals("Valid") == false)
                 {
                     isValid = false;
                 }
             }
         }
         if (isValid == false)
         {
             ForwardJSP(req, res, "/ValidateDECPage.jsp");
         }
         else
         {
             doInsertDEC(req, res);
         }
     } // end of doSumitDE
 
     /**
      * Called from doCreateVDActions. Calls 'setAC.setVDValueFromPage' to set the VD data from the page. Calls
      * 'setAC.setValidatePageValuesVD' to validate the data. Loops through the vector vValidate to check if everything
      * is valid and Calls 'doInsertVD' to insert the data. If vector contains invalid fields, forwards to validation
      * page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doSubmitVD(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "sVDAction", "validate");
         VD_Bean m_VD = new VD_Bean();
         EVS_Bean m_OC = new EVS_Bean();
         EVS_Bean m_PC = new EVS_Bean();
         EVS_Bean m_REP = new EVS_Bean();
         EVS_Bean m_OCQ = new EVS_Bean();
         EVS_Bean m_PCQ = new EVS_Bean();
         EVS_Bean m_REPQ = new EVS_Bean();
         GetACService getAC = new GetACService(req, res, this);
         m_setAC.setVDValueFromPage(req, res, m_VD);
         m_OC = (EVS_Bean) session.getAttribute("m_OC");
         m_PC = (EVS_Bean) session.getAttribute("m_PC");
         m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
         m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
         m_REP = (EVS_Bean) session.getAttribute("m_REP");
         m_REPQ = (EVS_Bean) session.getAttribute("m_REPQ");
         m_setAC.setValidatePageValuesVD(req, res, m_VD, m_OC, m_PC, m_REP, m_OCQ, m_PCQ, m_REPQ, getAC);
         DataManager.setAttribute(session, "m_VD", m_VD);
         boolean isValid = true;
         Vector vValidate = new Vector();
         vValidate = (Vector) req.getAttribute("vValidate");
         if (vValidate == null)
             isValid = false;
         else
         {
             for (int i = 0; vValidate.size() > i; i = i + 3)
             {
                 String sStat = (String) vValidate.elementAt(i + 2);
                 if (sStat.equals("Valid") == false)
                 {
                     isValid = false;
                 }
             }
         }
         if (isValid == false)
         {
             ForwardJSP(req, res, "/ValidateVDPage.jsp");
         }
         else
         {
             doInsertVD(req, res);
         }
     } // end of doSumitVD
 
     /**
      * The doValidateDE method gets the values from page the user filled out, validates the input, then forwards results
      * to the Validate page Called from 'doCreateDEActions' method. Calls 'setAC.setDEValueFromPage' to set the data
      * from the page to the bean. Calls 'setAC.setValidatePageValuesDE' to validate the data. Stores 'm_DE' bean in
      * session. Forwards the page 'ValidateDEPage.jsp' with validation vector to display.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doValidateDE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // do below for versioning to check whether these two have changed
         DataManager.setAttribute(session, "DEEditAction", "DEEdit");
         DataManager.setAttribute(session, "sCDEAction", "validate");
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         GetACService getAC = new GetACService(req, res, this);
         m_setAC.setDEValueFromPage(req, res, m_DE);
         m_setAC.setValidatePageValuesDE(req, res, m_DE, getAC);
         DataManager.setAttribute(session, "m_DE", m_DE);
         ForwardJSP(req, res, "/ValidateDEPage.jsp");
     } // end of doValidateDE
 
     /**
      * The doValidateDE method gets the values from page the user filled out, validates the input, then forwards results
      * to the Validate page Called from 'doCreateDEActions' method. Calls 'setAC.setDEValueFromPage' to set the data
      * from the page to the bean. Calls 'setAC.setValidatePageValuesDE' to validate the data. Stores 'm_DE' bean in
      * session. Forwards the page 'ValidateDEPage.jsp' with validation vector to display.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doValidateDEBlockEdit(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "sCDEAction", "validate");
         DataManager.setAttribute(session, "DEAction", "EditDE");
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         // GetACService getAC = new GetACService(req, res, this);
         m_setAC.setDEValueFromPage(req, res, m_DE);
         DataManager.setAttribute(session, "m_DE", m_DE);
         m_setAC.setValidateBlockEdit(req, res, "DataElement");
         DataManager.setAttribute(session, "DEEditAction", "DEBlockEdit");
         ForwardJSP(req, res, "/ValidateDEPage.jsp");
     } // end of doValidateDE
 
     /**
      * @param sDefSource
      *            string def source selected
      * @return String defintion source
      * @throws Exception
      */
     public String getSourceToken(String sDefSource) throws Exception
     {
         int index = -1;
         int length = 0;
         if (sDefSource != null)
             length = sDefSource.length();
         String pointStr = ": Concept Source ";
         index = sDefSource.indexOf(pointStr);
         if (index == -1)
             index = 0;
         if (index > 0 && length > 18)
             sDefSource = sDefSource.substring((index + 17), sDefSource.length());
         return sDefSource;
     }
 
     /**
      * The doValidateDEC method gets the values from page the user filled out, validates the input, then forwards
      * results to the Validate page Called from 'doCreateDECActions' 'doSubmitDEC' method. Calls
      * 'setAC.setDECValueFromPage' to set the data from the page to the bean. Calls 'setAC.setValidatePageValuesDEC' to
      * validate the data. Stores 'm_DEC' bean in session. Forwards the page 'ValidateDECPage.jsp' with validation vector
      * to display.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doValidateDEC(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         // System.err.println("in doValidateDEC");
         HttpSession session = req.getSession();
         // do below for versioning to check whether these two have changed
         DataManager.setAttribute(session, "DECPageAction", "validate"); // store the page action in attribute
         DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
         if (m_DEC == null)
             m_DEC = new DEC_Bean();
         EVS_Bean m_OC = new EVS_Bean();
         EVS_Bean m_PC = new EVS_Bean();
         EVS_Bean m_OCQ = new EVS_Bean();
         EVS_Bean m_PCQ = new EVS_Bean();
         GetACService getAC = new GetACService(req, res, this);
         m_setAC.setDECValueFromPage(req, res, m_DEC);
         m_OC = (EVS_Bean) session.getAttribute("m_OC");
         m_PC = (EVS_Bean) session.getAttribute("m_PC");
         m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
         m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
         // System.err.println("in doValidateDEC call setValidate");
         m_setAC.setValidatePageValuesDEC(req, res, m_DEC, m_OC, m_PC, getAC, m_OCQ, m_PCQ);
         DataManager.setAttribute(session, "m_DEC", m_DEC);
         ForwardJSP(req, res, "/ValidateDECPage.jsp");
     } // end of doValidateDEC
 
     /**
      * The doValidateDECBlockEdit method gets the values from page the user filled out, validates the input, then
      * forwards results to the Validate page Called from 'doCreateDECActions' 'doSubmitDEC' method. Calls
      * 'setAC.setDECValueFromPage' to set the data from the page to the bean. Calls 'setAC.setValidatePageValuesDEC' to
      * validate the data. Stores 'm_DEC' bean in session. Forwards the page 'ValidateDECPage.jsp' with validation vector
      * to display.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doValidateDECBlockEdit(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "DECPageAction", "validate"); // store the page action in attribute
         DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
         if (m_DEC == null)
             m_DEC = new DEC_Bean();
         m_setAC.setDECValueFromPage(req, res, m_DEC);
         DataManager.setAttribute(session, "m_DEC", m_DEC);
         m_setAC.setValidateBlockEdit(req, res, "DataElementConcept");
         DataManager.setAttribute(session, "DECEditAction", "DECBlockEdit");
         ForwardJSP(req, res, "/ValidateDECPage.jsp");
     } // end of doValidateDEC
 
     /**
      * The doValidateVD method gets the values from page the user filled out, validates the input, then forwards results
      * to the Validate page Called from 'doCreateVDActions', 'doSubmitVD' method. Calls 'setAC.setVDValueFromPage' to
      * set the data from the page to the bean. Calls 'setAC.setValidatePageValuesVD' to validate the data. Stores 'm_VD'
      * bean in session. Forwards the page 'ValidateVDPage.jsp' with validation vector to display.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doValidateVD(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sAction = (String) req.getParameter("pageAction");
         if (sAction == null)
             sAction = "";
         // do below for versioning to check whether these two have changed
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
         EVS_Bean m_OC = new EVS_Bean();
         EVS_Bean m_PC = new EVS_Bean();
         EVS_Bean m_REP = new EVS_Bean();
         EVS_Bean m_OCQ = new EVS_Bean();
         EVS_Bean m_PCQ = new EVS_Bean();
         EVS_Bean m_REPQ = new EVS_Bean();
         GetACService getAC = new GetACService(req, res, this);
         DataManager.setAttribute(session, "VDPageAction", "validate"); // store the page action in attribute
         m_setAC.setVDValueFromPage(req, res, m_VD);
         m_OC = (EVS_Bean) session.getAttribute("m_OC");
         m_PC = (EVS_Bean) session.getAttribute("m_PC");
         m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
         m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
         m_REP = (EVS_Bean) session.getAttribute("m_REP");
         m_REPQ = (EVS_Bean) session.getAttribute("m_REPQ");
         m_setAC.setValidatePageValuesVD(req, res, m_VD, m_OC, m_PC, m_REP, m_OCQ, m_PCQ, m_REPQ, getAC);
         DataManager.setAttribute(session, "m_VD", m_VD);
         /*
          * if(sAction.equals("Enum") || sAction.equals("NonEnum") || sAction.equals("EnumByRef")) ForwardJSP(req, res,
          * "/CreateVDPage.jsp"); else if (!sAction.equals("vdpvstab") && !sAction.equals("vddetailstab"))
          * ForwardJSP(req, res, "/ValidateVDPage.jsp");
          */} // end of doValidateVD
 
     /**
      * The doSetVDPage method gets the values from page the user filled out, Calls 'setAC.setVDValueFromPage' to set the
      * data from the page to the bean. Stores 'm_VD' bean in session. Forwards the page 'CreateVDPage.jsp' with
      * validation vector to display.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sOrigin
      *            origin where it is called from
      *
      * @throws Exception
      */
     public void doSetVDPage(HttpServletRequest req, HttpServletResponse res, String sOrigin) throws Exception
     {
         try
         {
             HttpSession session = req.getSession();
             String sAction = (String) req.getParameter("pageAction");
             if (sAction == null)
                 sAction = "";
             // do below for versioning to check whether these two have changed
             VD_Bean vdBean = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
             m_setAC.setVDValueFromPage(req, res, vdBean);
             // check if pvs are used in the form when type is changed to non enumerated.
             if (!sAction.equals("Enum"))
             {
                 // get vdid from the bean
                 // VD_Bean vdBean = (VD_Bean)session.getAttribute("m_VD");
                 String sVDid = vdBean.getVD_VD_IDSEQ();
                 boolean isExist = false;
                 if (sOrigin.equals("Edit"))
                 {
                     // call function to check if relationship exists
                     SetACService setAC = new SetACService(this);
                     isExist = setAC.checkPVQCExists(req, res, sVDid, "");
                     if (isExist)
                     {
                         String sMsg = "Unable to change Value Domain type to Non-Enumerated "
                                         + "because one or more Permissible Values are being used in a Case Report Form. \\n"
                                         + "Please create a new version of this Value Domain to change the type to Non-Enumerated.";
                         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, sMsg);
                         vdBean.setVD_TYPE_FLAG("E");
                         DataManager.setAttribute(session, "m_VD", vdBean);
                     }
                 }
                 // mark all the pvs as deleted to remove them while submitting.
                 if (!isExist)
                 {
                     Vector<PV_Bean> vVDPVs = vdBean.getVD_PV_List(); // (Vector)session.getAttribute("VDPVList");
                     if (vVDPVs != null)
                     {
                         // set each bean as deleted to handle later
                         Vector<PV_Bean> vRemVDPV = vdBean.getRemoved_VDPVList();
                         if (vRemVDPV == null)
                             vRemVDPV = new Vector<PV_Bean>();
                         for (int i = 0; i < vVDPVs.size(); i++)
                         {
                             PV_Bean pvBean = (PV_Bean) vVDPVs.elementAt(i);
                             vRemVDPV.addElement(pvBean);
                         }
                         vdBean.setRemoved_VDPVList(vRemVDPV);
                         vdBean.setVD_PV_List(new Vector<PV_Bean>());
                     }
                 }
             }
             else
             {
                 // remove meta parents since it is not needed for enum types
                 Vector<EVS_Bean> vParentCon = vdBean.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
                 if (vParentCon == null)
                     vParentCon = new Vector<EVS_Bean>();
                 for (int i = 0; i < vParentCon.size(); i++)
                 {
                     EVS_Bean ePar = (EVS_Bean) vParentCon.elementAt(i);
                     if (ePar == null)
                         ePar = new EVS_Bean();
                     String parDB = ePar.getEVS_DATABASE();
                     // System.out.println(i + " setvdpage " + parDB);
                     if (parDB != null && parDB.equals("NCI Metathesaurus"))
                     {
                         ePar.setCON_AC_SUBMIT_ACTION("DEL");
                         vParentCon.setElementAt(ePar, i);
                     }
                 }
                 vdBean.setReferenceConceptList(vParentCon);
                 DataManager.setAttribute(session, "m_VD", vdBean);
                 // get back pvs associated with this vd
                 VD_Bean oldVD = (VD_Bean) session.getAttribute("oldVDBean");
                 if (oldVD == null)
                     oldVD = new VD_Bean();
                 if (oldVD.getVD_TYPE_FLAG() != null && oldVD.getVD_TYPE_FLAG().equals("E"))
                 {
                     if (oldVD.getVD_VD_IDSEQ() != null && !oldVD.getVD_VD_IDSEQ().equals(""))
                     {
                        // String pvAct = "Search";
                         String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
                        // if (sMenu.equals("NewVDTemplate"))
                           //  pvAct = "NewUsing";
                       //  Integer pvCount = new Integer(0);
                         vdBean.setVD_PV_List(oldVD.cloneVDPVVector(oldVD.getVD_PV_List()));
                         vdBean.setRemoved_VDPVList(new Vector<PV_Bean>());
                         GetACSearch serAC = new GetACSearch(req, res, this);
                         if (sMenu.equals("Questions"))
                             serAC.getACQuestionValue(vdBean);
                     }
                 }
             }
             DataManager.setAttribute(session, "m_VD", vdBean);
         }
         catch (Exception e)
         {
             logger.fatal("Error - doSetVDPage " + e.toString(), e);
         }
     } // end of doValidateVD
 
     /**
      * gets the row number from the hiddenSelRow Loops through the selected row and gets the evs bean for that row from
      * the vector of evs search results. adds it to vList vector and return the vector back
      *
      * @param req
      *            HttpServletRequest
      * @return Vector of EVS Beans
      * @throws java.lang.Exception
      */
     public EVS_Bean getEVSSelRow(HttpServletRequest req) throws Exception
     {
         HttpSession session = req.getSession();
      //   InsACService insAC = new InsACService(req, m_classRes, this);
         // get the result vector from the session
         EVS_Bean eBean = new EVS_Bean();
         Vector vRSel = (Vector) session.getAttribute("vACSearch");
         if (vRSel == null)
             vRSel = new Vector();
         // get the array from teh hidden list
         String selRows[] = req.getParameterValues("hiddenSelRow");
         if (selRows == null)
             storeStatusMsg("Unable to select Concept, please try again");
         else
         {
             // loop through the array of strings
             for (int i = 0; i < selRows.length; i++)
             {
                 String thisRow = selRows[i];
                 Integer IRow = new Integer(thisRow);
                 int iRow = IRow.intValue();
                 if (iRow < 0 || iRow > vRSel.size())
                     storeStatusMsg("Row size is either too big or too small.");
                 else
                 {
                     eBean = (EVS_Bean) vRSel.elementAt(iRow);
                     if (eBean == null || eBean.getLONG_NAME() == null)
                     {
                         storeStatusMsg("Unable to obtain concept from the " + iRow
                                         + " row of the search results.\\n" + "Please try again.");
                         continue;
                     }
                 }
             }
         }
         return eBean;
     }
 
     /**
      * gets the row number from the hiddenSelRow Loops through the selected row and gets the evs bean for that row from
      * the vector of evs search results. adds it to vList vector and return the vector back
      *
      * @param req
      *            HttpServletRequest
      * @param vList
      *            Existing Vector of EVS Beans
      * @return Vector of EVS Beans
      * @throws java.lang.Exception
      */
     public Vector<EVS_Bean> getEVSSelRowVector(HttpServletRequest req, Vector<EVS_Bean> vList) throws Exception
     {
         HttpSession session = req.getSession();
         // get the result vector from the session
         Vector vRSel = (Vector) session.getAttribute("vACSearch");
         if (vRSel == null)
             vRSel = new Vector();
         // get the array from teh hidden list
         String selRows[] = req.getParameterValues("hiddenSelRow");
         if (selRows == null)
             storeStatusMsg("Unable to select Concept, please try again");
         else
         {
             // loop through the array of strings
             for (int i = 0; i < selRows.length; i++)
             {
                 String thisRow = selRows[i];
                 int iRow = Integer.parseInt(thisRow);
                // Integer IRow = new Integer(thisRow);
                // int iRow = IRow.intValue();
                 if (iRow < 0 || iRow > vRSel.size())
                 {
                     storeStatusMsg(iRow + " Row size is either too big or too small.");
                     logger.fatal(iRow + " CurationServelt - getEVSSelRowVector:  Row size is either too big or too small.");
                 }
                 else
                 {
                     EVS_Bean eBean = (EVS_Bean) vRSel.elementAt(iRow);
                     // send it back if unable to obtion the concept
                     if (eBean == null || eBean.getLONG_NAME() == null)
                     {
                         storeStatusMsg("Unable to obtain concept from the " + thisRow
                                         + " row of the search results.\\n" + "Please try again.");
                         logger.fatal(thisRow + " CurationServelt - getEVSSelRowVector:  Unable to obtain concept from the results.");
                         continue;
                     }
                     vList.addElement(eBean);
                 }
             }
             //add duplicate checks logic here
             EVSSearch serEVS = new EVSSearch(m_classReq, m_classRes, this);
             if (vList != null && vList.size() > 0)
                 vList = serEVS.getNCIPrefConcepts(vList);
         }
         return vList;
     }
 
     /**
      * makes the vd's system generated name
      *
      * @param req
      *            HttpServletRequest object
      * @param vd
      *            current vd bean
      * @param vParent
      *            vector of seelected parents
      * @return modified vd bean
      * @throws java.lang.Exception
      */
     public VD_Bean doGetVDSystemName(HttpServletRequest req, VD_Bean vd, Vector vParent) throws Exception
     {
         try
         {
             // make the system generated name
             String sysName = "";
             for (int i = vParent.size() - 1; i > -1; i--)
             {
                 EVS_Bean par = (EVS_Bean) vParent.elementAt(i);
                 String evsDB = par.getEVS_DATABASE();
                 String subAct = par.getCON_AC_SUBMIT_ACTION();
                 if (subAct != null && !subAct.equals("DEL") && evsDB != null && !evsDB.equals("Non_EVS"))
                 {
                     // add the concept id to sysname if less than 20 characters
                     if (sysName.equals("") || sysName.length() < 20)
                         sysName += par.getCONCEPT_IDENTIFIER() + ":";
                     else
                         break;
                 }
             }
             // append vd public id and version in the end
             if (vd.getVD_VD_ID() != null)
                 sysName += vd.getVD_VD_ID();
             String sver = vd.getVD_VERSION();
             if (sver != null && sver.indexOf(".") < 0)
                 sver += ".0";
             if (vd.getVD_VERSION() != null)
                 sysName += "v" + sver;
             // limit to 30 characters
             if (sysName.length() > 30)
                 sysName = sysName.substring(sysName.length() - 30);
             vd.setAC_SYS_PREF_NAME(sysName); // store it in vd bean
             // make system name preferrd name if sys was selected
             String selNameType = (String) req.getParameter("rNameConv");
             // get it from the vd bean if null
             if (selNameType == null)
             {
                 selNameType = vd.getVD_TYPE_NAME();
             }
             else
             {
                 // store the keyed in text in the user field for later use.
                 String sPrefName = (String) req.getParameter("txPreferredName");
                 if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
                     vd.setAC_USER_PREF_NAME(sPrefName);
             }
             if (selNameType != null && selNameType.equals("SYS"))
                 vd.setVD_PREFERRED_NAME(sysName);
         }
         catch (Exception e)
         {
             this.logger.fatal("ERROR - doGetVDSystemName : " + e.toString(), e);
         }
         return vd;
     }
 
     /**
      * fills in the non evs parent attributes and sends back to create non evs parent page to view details
      *
      * @param req
      *            HttpServletRequest object
      * @param res
      *            HttpServletResponse object
      * @throws java.lang.Exception
      */
     private void doNonEVSPageAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sPageAct = (String) req.getParameter("actSelect");
         if (sPageAct.equals("viewParent"))
         {
             VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             Vector<EVS_Bean> vParentCon = vd.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
             if (vParentCon != null)
             {
                 String selName = (String) req.getParameter("txtRefName");
                 for (int i = 0; i < vParentCon.size(); i++)
                 {
                     EVS_Bean eBean = (EVS_Bean) vParentCon.elementAt(i);
                     String thisName = eBean.getLONG_NAME();
                     String sDB = eBean.getEVS_DATABASE();
                     // get the selected name from the vector
                     if (selName != null && thisName != null && sDB != null && selName.equals(thisName)
                                     && sDB.equals("Non_EVS"))
                     {
                         req.setAttribute("SelectedVDParent", eBean);
                         break;
                     }
                 }
             }
             this.ForwardJSP(req, res, "/NonEVSSearchPage.jsp");
         }
         else
         { // send back to block search for parent
             ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
         }
     }
 
     /**
      * marks the parent and/or its pvs as deleted from the session.
      *
      * @param req
      * @param res
      * @param sPVAction
      * @param vdPage
      * @throws java.lang.Exception
      */
     private void doRemoveParent(HttpServletRequest req, HttpServletResponse res, String sPVAction, String vdPage)
                     throws Exception
     {
         HttpSession session = req.getSession();
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
         Vector<EVS_Bean> vParentCon = m_VD.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
         if (vParentCon == null)
             vParentCon = new Vector<EVS_Bean>();
         // get the selected parent info from teh request
         String sParentCC = (String) req.getParameter("selectedParentConceptCode");
         String sParentName = (String) req.getParameter("selectedParentConceptName");
         String sParentDB = (String) req.getParameter("selectedParentConceptDB");
         // for non evs parent compare the long names instead
         if (sParentName != null && !sParentName.equals("") && sParentDB != null && sParentDB.equals("Non_EVS"))
             sParentCC = sParentName;
         if (sParentCC != null)
         {
             for (int i = 0; i < vParentCon.size(); i++)
             {
                 EVS_Bean eBean = (EVS_Bean) vParentCon.elementAt(i);
                 if (eBean == null)
                     eBean = new EVS_Bean();
                 String thisParent = eBean.getCONCEPT_IDENTIFIER();
                 if (thisParent == null)
                     thisParent = "";
                 String thisParentName = eBean.getLONG_NAME();
                 if (thisParentName == null)
                     thisParentName = "";
                 String thisParentDB = eBean.getEVS_DATABASE();
                 if (thisParentDB == null)
                     thisParentDB = "";
                 // for non evs parent compare the long names instead
                 if (sParentDB != null && sParentDB.equals("Non_EVS"))
                     thisParent = thisParentName;
                 // look for the matched parent from the vector to remove
                 if (sParentCC.equals(thisParent))
                 {
                     @SuppressWarnings("unused") String strHTML = "";
                     EVSMasterTree tree = new EVSMasterTree(req, thisParentDB, this);
                     strHTML = tree.refreshTree(thisParentName, "false");
                     strHTML = tree.refreshTree("parentTree" + thisParentName, "false");
                     if (sPVAction.equals("removePVandParent"))
                     {
                         Vector<PV_Bean> vVDPVList = m_VD.getVD_PV_List(); // (Vector)session.getAttribute("VDPVList");
                         if (vVDPVList == null)
                             vVDPVList = new Vector<PV_Bean>();
                         // loop through the vector of pvs to get matched parent
                         for (int j = 0; j < vVDPVList.size(); j++)
                         {
                             PV_Bean pvBean = (PV_Bean) vVDPVList.elementAt(j);
                             if (pvBean == null)
                                 pvBean = new PV_Bean();
                             EVS_Bean pvParent = (EVS_Bean) pvBean.getPARENT_CONCEPT();
                             if (pvParent == null)
                                 pvParent = new EVS_Bean();
                             String pvParCon = pvParent.getCONCEPT_IDENTIFIER();
                             // match the parent concept with the pv's parent concept
                             if (thisParent.equals(pvParCon))
                             {
                                 pvBean.setVP_SUBMIT_ACTION("DEL"); // mark the vp as deleted
                                // String pvID = pvBean.getPV_PV_IDSEQ();
                                 vVDPVList.setElementAt(pvBean, j);
                             }
                         }
                         m_VD.setVD_PV_List(vVDPVList);
                         // DataManager.setAttribute(session, "VDPVList", vVDPVList);
                     }
                     // mark the parent as delected and leave
                     eBean.setCON_AC_SUBMIT_ACTION("DEL");
                     vParentCon.setElementAt(eBean, i);
                     break;
                 }
             }
         }
         // DataManager.setAttribute(session, "VDParentConcept", vParentCon);
         m_VD.setReferenceConceptList(vParentCon);
         // make sure all other changes are stored back in vd
         m_setAC.setVDValueFromPage(req, res, m_VD);
         // make vd's system preferred name
         m_VD = this.doGetVDSystemName(req, m_VD, vParentCon);
         DataManager.setAttribute(session, "m_VD", m_VD);
         // make the selected parent in hte session empty
         DataManager.setAttribute(session, "SelectedParentName", "");
         DataManager.setAttribute(session, "SelectedParentCC", "");
         DataManager.setAttribute(session, "SelectedParentDB", "");
         DataManager.setAttribute(session, "SelectedParentMetaSource", "");
         // forward teh page according to vdPage
         if (vdPage.equals("editVD"))
             ForwardJSP(req, res, "/EditVDPage.jsp");
         else
             ForwardJSP(req, res, "/CreateVDPage.jsp");
     }
 
     /**
      * splits the vd rep term from cadsr into individual concepts
      *
      * @param sComp
      *            name of the searched component
      * @param m_Bean
      *            selected EVS bean
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param nameAction
      *            string naming action
      *
      */
     public void splitIntoConceptsVD(String sComp, EVS_Bean m_Bean, HttpServletRequest req, HttpServletResponse res,
                     String nameAction)
     {
         try
         {
             HttpSession session = req.getSession();
           //  String sSelRow = "";
             VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD");
             if (m_VD == null)
                 m_VD = new VD_Bean();
             m_setAC.setVDValueFromPage(req, res, m_VD);
             Vector vRepTerm = (Vector) session.getAttribute("vRepTerm");
             if (vRepTerm == null)
                 vRepTerm = new Vector();
             String sCondr = m_Bean.getCONDR_IDSEQ();
             String sLongName = m_Bean.getLONG_NAME();
             String sIDSEQ = m_Bean.getIDSEQ();
             if (sComp.equals("RepTerm") || sComp.equals("RepQualifier"))
             {
                 m_VD.setVD_REP_TERM(sLongName);
                 m_VD.setVD_REP_IDSEQ(sIDSEQ);
             }
            // String sRepTerm = m_VD.getVD_REP_TERM();
             if (sCondr != null && !sCondr.equals(""))
             {
                 GetACService getAC = new GetACService(req, res, this);
                 Vector vCon = getAC.getAC_Concepts(sCondr, null, true);
                 if (vCon != null && vCon.size() > 0)
                 {
                     for (int j = 0; j < vCon.size(); j++)
                     {
                         EVS_Bean bean = new EVS_Bean();
                         bean = (EVS_Bean) vCon.elementAt(j);
                         if (bean != null)
                         {
                             if (j == 0) // Primary Concept
                                 m_VD = this.addRepConcepts(req, res, nameAction, m_VD, bean, "Primary");
                             else
                                 // Secondary Concepts
                                 m_VD = this.addRepConcepts(req, res, nameAction, m_VD, bean, "Qualifier");
                         }
                     }
                 }
             }
         }
         catch (Exception e)
         {
             this.logger.fatal("ERROR - splitintoConceptVD : " + e.toString(), e);
         }
     }
 
     /**
      * splits the dec object class or property from cadsr into individual concepts
      *
      * @param sComp
      *            name of the searched component
      * @param m_Bean
      *            selected EVS bean
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param nameAction
      *            string naming action
      *
      */
     public void splitIntoConcepts(String sComp, EVS_Bean m_Bean, HttpServletRequest req, HttpServletResponse res,
                     String nameAction)
     {
         try
         {
             HttpSession session = req.getSession();
           //  String sSelRow = "";
             DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
             if (m_DEC == null)
                 m_DEC = new DEC_Bean();
             m_setAC.setDECValueFromPage(req, res, m_DEC);
             Vector vObjectClass = (Vector) session.getAttribute("vObjectClass");
             if (vObjectClass == null)
                 vObjectClass = new Vector();
             Vector vProperty = (Vector) session.getAttribute("vProperty");
             if (vProperty == null)
                 vProperty = new Vector();
             String sCondr = m_Bean.getCONDR_IDSEQ();
             String sLongName = m_Bean.getLONG_NAME();
             String sIDSEQ = m_Bean.getIDSEQ();
             if (sIDSEQ == null)
                 sIDSEQ = "";
             if (sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier"))
             {
                 m_DEC.setDEC_OCL_NAME(sLongName);
                 m_DEC.setDEC_OCL_IDSEQ(sIDSEQ);
             }
             else if (sComp.equals("Property") || sComp.equals("PropertyClass") || sComp.equals("PropertyQualifier"))
             {
                 m_DEC.setDEC_PROPL_NAME(sLongName);
                 m_DEC.setDEC_PROPL_IDSEQ(sIDSEQ);
             }
           //  String sObjClass = m_DEC.getDEC_OCL_NAME();
             if (sCondr != null && !sCondr.equals(""))
             {
                 GetACService getAC = new GetACService(req, res, this);
                 Vector vCon = getAC.getAC_Concepts(sCondr, null, true);
                 if (vCon != null && vCon.size() > 0)
                 {
                     for (int j = 0; j < vCon.size(); j++)
                     {
                         EVS_Bean bean = new EVS_Bean();
                         bean = (EVS_Bean) vCon.elementAt(j);
                         if (bean != null)
                         {
                             if (sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier"))
                             {
                                 if (j == 0) // Primary Concept
                                     m_DEC = this.addOCConcepts(req, res, nameAction, m_DEC, bean, "Primary");
                                 else
                                     // Secondary Concepts
                                     m_DEC = this.addOCConcepts(req, res, nameAction, m_DEC, bean, "Qualifier");
                             }
                             else if (sComp.equals("Property") || sComp.equals("PropertyClass")
                                             || sComp.equals("PropertyQualifier"))
                             {
                                 if (j == 0) // Primary Concept
                                     m_DEC = this.addPropConcepts(req, res, nameAction, m_DEC, bean, "Primary");
                                 else
                                     // Secondary Concepts
                                     m_DEC = this.addPropConcepts(req, res, nameAction, m_DEC, bean, "Qualifier");
                             }
                         }
                     }
                 }
             }// sCondr != null
         }
         catch (Exception e)
         {
             this.logger.fatal("ERROR - splitintoConcept : " + e.toString(), e);
         }
     }
 
     /**
      * makes three types of preferred names and stores it in the bean
      *
      * @param req
      *            HttpServletRequest object
      * @param res
      *            HttpServletResponse object
      * @param newBean
      *            new evs bean
      * @param nameAct
      *            string new name or apeend name
      * @param pageDEC
      *            current dec bean
      * @return dec bean
      */
     public DEC_Bean doGetDECNames(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res, EVS_Bean newBean, String nameAct,
                     DEC_Bean pageDEC)
     {
         HttpSession session = req.getSession();
         if (pageDEC == null)
             pageDEC = (DEC_Bean) session.getAttribute("m_DEC");
         // get DEC object class and property names
         String sLongName = "";
         String sPrefName = "";
         String sAbbName = "";
         String sOCName = "";
         String sPropName = "";
         String sDef = "";
         // get the existing one if not restructuring the name but appending it
         if (newBean != null)
         {
             sLongName = pageDEC.getDEC_LONG_NAME();
             if (sLongName == null)
                 sLongName = "";
             sDef = pageDEC.getDEC_PREFERRED_DEFINITION();
             if (sDef == null)
                 sDef = "";
         }
         // get the typed text on to user name
         String selNameType = "";
         if (nameAct.equals("Search") || nameAct.equals("Remove"))
         {
             selNameType = (String) req.getParameter("rNameConv");
             sPrefName = (String) req.getParameter("txPreferredName");
             if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
                 pageDEC.setAC_USER_PREF_NAME(sPrefName);
         }
         // get the object class into the long name and abbr name
         Vector vObjectClass = (Vector) session.getAttribute("vObjectClass");
         if (vObjectClass == null)
             vObjectClass = new Vector();
         // add the Object Class qualifiers first
         for (int i = 1; vObjectClass.size() > i; i++)
         {
             EVS_Bean eCon = (EVS_Bean) vObjectClass.elementAt(i);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String conName = eCon.getLONG_NAME();
             if (conName == null)
                 conName = "";
             if (!conName.equals(""))
             {
                 // rearrange it long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += conName;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (conName.length() > 3)
                     sAbbName += conName.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += conName;
                 // add object qualifiers to object class name
                 if (!sOCName.equals(""))
                     sOCName += " ";
                 sOCName += conName;
             }
         }
         // add the Object Class primary
         if (vObjectClass != null && vObjectClass.size() > 0)
         {
             EVS_Bean eCon = (EVS_Bean) vObjectClass.elementAt(0);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String sPrimary = eCon.getLONG_NAME();
             if (sPrimary == null)
                 sPrimary = "";
             if (!sPrimary.equals(""))
             {
                 // rearrange it only long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += sPrimary;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (sPrimary.length() > 3)
                     sAbbName += sPrimary.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += sPrimary;
                 // add primary object to object name
                 if (!sOCName.equals(""))
                     sOCName += " ";
                 sOCName += sPrimary;
             }
         }
         // get the Property into the long name and abbr name
         Vector vProperty = (Vector) session.getAttribute("vProperty");
         if (vProperty == null)
             vProperty = new Vector();
         // add the property qualifiers first
         for (int i = 1; vProperty.size() > i; i++)
         {
             EVS_Bean eCon = (EVS_Bean) vProperty.elementAt(i);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String conName = eCon.getLONG_NAME();
             if (conName == null)
                 conName = "";
             if (!conName.equals(""))
             {
                 // rearrange it long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += conName;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (conName.length() > 3)
                     sAbbName += conName.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += conName;
                 // add property qualifiers to property name
                 if (!sPropName.equals(""))
                     sPropName += " ";
                 sPropName += conName;
             }
         }
         // add the property primary
         if (vProperty != null && vProperty.size() > 0)
         {
             EVS_Bean eCon = (EVS_Bean) vProperty.elementAt(0);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String sPrimary = eCon.getLONG_NAME();
             if (sPrimary == null)
                 sPrimary = "";
             if (!sPrimary.equals(""))
             {
                 // rearrange it only long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += sPrimary;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (sPrimary.length() > 3)
                     sAbbName += sPrimary.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += sPrimary;
                 // add primary property to property name
                 if (!sPropName.equals(""))
                     sPropName += " ";
                 sPropName += sPrimary;
             }
         }
         // truncate to 30 characters
         if (sAbbName != null && sAbbName.length() > 30)
             sAbbName = sAbbName.substring(0, 30);
         // add the abbr name to vd bean and page is selected
         pageDEC.setAC_ABBR_PREF_NAME(sAbbName);
         // make abbr name name preferrd name if sys was selected
         if (selNameType != null && selNameType.equals("ABBR"))
             pageDEC.setDEC_PREFERRED_NAME(sAbbName);
         // appending to the existing;
         if (newBean != null)
         {
             String sSelectName = newBean.getLONG_NAME();
             if (!sLongName.equals(""))
                 sLongName += " ";
             sLongName += sSelectName;
             if (!sDef.equals(""))
                 sDef += "_"; // add definition
             sDef += newBean.getPREFERRED_DEFINITION();
         }
         // store the long names, definition, and usr name in vd bean if searched
         if (nameAct.equals("Search"))
         {
             pageDEC.setDEC_LONG_NAME(sLongName);
             pageDEC.setDEC_PREFERRED_DEFINITION(sDef);
         }
         pageDEC.setDEC_OCL_NAME(sOCName);
         pageDEC.setDEC_PROPL_NAME(sPropName);
         if (nameAct.equals("Search") || nameAct.equals("Remove"))
         {
             pageDEC.setAC_SYS_PREF_NAME("(Generated by the System)"); // only for dec
             if (selNameType != null && selNameType.equals("SYS"))
                 pageDEC.setDEC_PREFERRED_NAME(pageDEC.getAC_SYS_PREF_NAME());
         }
         return pageDEC;
     }
 
     /**
      * this method is used to create preferred name for VD names of all three types will be stored in the bean for later
      * use if type is changed, it populates name according to type selected.
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param newBean
      *            new EVS bean to append the name to
      * @param nameAct
      *            string new name or apeend name
      * @param pageVD
      *            current vd bean
      * @return VD bean
      */
     public VD_Bean doGetVDNames(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res, EVS_Bean newBean, String nameAct,
                     VD_Bean pageVD)
     {
         HttpSession session = req.getSession();
         if (pageVD == null)
             pageVD = (VD_Bean) session.getAttribute("m_VD");
         // get vd object class and property names
         String sLongName = "";
         String sPrefName = "";
         String sAbbName = "";
         String sDef = "";
         // get the existing one if not restructuring the name but appending it
         if (newBean != null)
         {
             sLongName = pageVD.getVD_LONG_NAME();
             if (sLongName == null)
                 sLongName = "";
             sDef = pageVD.getVD_PREFERRED_DEFINITION();
             if (sDef == null)
                 sDef = "";
         }
         // get the typed text on to user name
         String selNameType = "";
         if (nameAct.equals("Search") || nameAct.equals("Remove"))
         {
             selNameType = (String) req.getParameter("rNameConv");
             sPrefName = (String) req.getParameter("txPreferredName");
             if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
                 pageVD.setAC_USER_PREF_NAME(sPrefName);
         }
         // get the object class into the long name and abbr name
         String sObjClass = pageVD.getVD_OBJ_CLASS();
         if (sObjClass == null)
             sObjClass = "";
         if (!sObjClass.equals(""))
         {
             // rearrange it long name
             if (newBean == null)
             {
                 if (!sLongName.equals(""))
                     sLongName += " "; // add extra space if not empty
                 sLongName += sObjClass;
                 EVS_Bean mOC = (EVS_Bean) session.getAttribute("m_OC");
                 if (mOC != null)
                 {
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += mOC.getPREFERRED_DEFINITION();
                 }
             }
             if (!sAbbName.equals(""))
                 sAbbName += "_"; // add underscore if not empty
             if (sObjClass.length() > 3)
                 sAbbName += sObjClass.substring(0, 4); // truncate to 4 letters
             else
                 sAbbName = sObjClass;
         }
         // get the property into the long name and abbr name
         String sPropClass = pageVD.getVD_PROP_CLASS();
         if (sPropClass == null)
             sPropClass = "";
         if (!sPropClass.equals(""))
         {
             // rearrange it long name
             if (newBean == null)
             {
                 if (!sLongName.equals(""))
                     sLongName += " "; // add extra space if not empty
                 sLongName += sPropClass;
                 EVS_Bean mPC = (EVS_Bean) session.getAttribute("m_PC");
                 if (mPC != null)
                 {
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += mPC.getPREFERRED_DEFINITION();
                 }
             }
             if (!sAbbName.equals(""))
                 sAbbName += "_"; // add underscore if not empty
             if (sPropClass.length() > 3)
                 sAbbName += sPropClass.substring(0, 4); // truncate to 4 letters
             else
                 sAbbName += sPropClass;
         }
         Vector vRep = (Vector) session.getAttribute("vRepTerm");
         if (vRep == null)
             vRep = new Vector();
         // add the qualifiers first
         for (int i = 1; vRep.size() > i; i++)
         {
             EVS_Bean eCon = (EVS_Bean) vRep.elementAt(i);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String conName = eCon.getLONG_NAME();
             if (conName == null)
                 conName = "";
             if (!conName.equals(""))
             {
                 // rearrange it long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += conName;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (conName.length() > 3)
                     sAbbName += conName.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += conName;
             }
         }
         // add the primary
         if (vRep != null && vRep.size() > 0)
         {
             EVS_Bean eCon = (EVS_Bean) vRep.elementAt(0);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String sPrimary = eCon.getLONG_NAME();
             if (sPrimary == null)
                 sPrimary = "";
             if (!sPrimary.equals(""))
             {
                 // rearrange it only long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += sPrimary;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (sPrimary.length() > 3)
                     sAbbName += sPrimary.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += sPrimary;
             }
         }
         // truncate to 30 characters
         if (sAbbName != null && sAbbName.length() > 30)
             sAbbName = sAbbName.substring(0, 30);
         // add the abbr name to vd bean and page is selected
         pageVD.setAC_ABBR_PREF_NAME(sAbbName);
         // make abbr name name preferrd name if sys was selected
         if (selNameType != null && selNameType.equals("ABBR"))
             pageVD.setVD_PREFERRED_NAME(sAbbName);
         if (newBean != null) // appending to the existing;
         {
             String sSelectName = newBean.getLONG_NAME();
             if (!sLongName.equals(""))
                 sLongName += " ";
             sLongName += sSelectName;
             if (!sDef.equals(""))
                 sDef += "_"; // add definition
             sDef += newBean.getPREFERRED_DEFINITION();
         }
         // store the long names, definition, and usr name in vd bean if searched
         if (nameAct.equals("Search"))
         {
             pageVD.setVD_LONG_NAME(sLongName);
             pageVD.setVD_PREFERRED_DEFINITION(sDef);
             pageVD.setVDNAME_CHANGED(true);
         }
         return pageVD;
     }
 
     /**
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param nameAction
      *            string naming action
      *
      */
     @SuppressWarnings("unchecked")
     public void doDECUseSelection(HttpServletRequest req, HttpServletResponse res, String nameAction)
     {
         try
         {
             HttpSession session = req.getSession();
             String sSelRow = "";
          //   InsACService insAC = new InsACService(req, res, this);
             DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
             if (m_DEC == null)
                 m_DEC = new DEC_Bean();
             m_setAC.setDECValueFromPage(req, res, m_DEC);
             Vector<EVS_Bean> vObjectClass = (Vector) session.getAttribute("vObjectClass");
             if (vObjectClass == null)
                 vObjectClass = new Vector<EVS_Bean>();
             Vector<EVS_Bean> vProperty = (Vector) session.getAttribute("vProperty");
             if (vProperty == null)
                 vProperty = new Vector<EVS_Bean>();
             Vector vAC = null;
             EVS_Bean blockBean = new EVS_Bean();
             String sComp = (String) req.getParameter("sCompBlocks");
             if (sComp == null)
                 sComp = "";
             // get the search bean from teh selected row
             sSelRow = (String) req.getParameter("selCompBlockRow");
             vAC = (Vector) session.getAttribute("vACSearch");
             if (vAC == null)
                 vAC = new Vector();
             if (sSelRow != null && !sSelRow.equals(""))
             {
                 String sObjRow = sSelRow.substring(2);
                 Integer intObjRow = new Integer(sObjRow);
                 int intObjRow2 = intObjRow.intValue();
                 if (vAC.size() > intObjRow2 - 1)
                     blockBean = (EVS_Bean) vAC.elementAt(intObjRow2);
                 String sNVP = (String) req.getParameter("nvpConcept");
                 if (sNVP != null && !sNVP.equals(""))
                 {
                     blockBean.setNVP_CONCEPT_VALUE(sNVP);
                     String sName = blockBean.getLONG_NAME();
                     blockBean.setLONG_NAME(sName + "::" + sNVP);
                     blockBean.setPREFERRED_DEFINITION(blockBean.getPREFERRED_DEFINITION() + "::" + sNVP);
                 }
                 System.out.println(sNVP + sComp + blockBean.getLONG_NAME() + blockBean.getPREFERRED_DEFINITION());
             }
             else
             {
                 storeStatusMsg("Unable to get the selected row from the " + sComp + " search results.\\n"
                                 + "Please try again.");
                 return;
             }
             // send it back if unable to obtion the concept
             if (blockBean == null || blockBean.getLONG_NAME() == null)
             {
                 storeStatusMsg("Unable to obtain concept from the selected row of the " + sComp
                                 + " search results.\\n" + "Please try again.");
                 return;
             }
             // do the primary search selection action
             if (sComp.equals("ObjectClass") || sComp.equals("Property") || sComp.equals("PropertyClass"))
             {
                 if (blockBean.getEVS_DATABASE().equals("caDSR"))
                 {
                     // split it if rep term, add concept class to the list if evs id exists
                     if (blockBean.getCONDR_IDSEQ() == null || blockBean.getCONDR_IDSEQ().equals(""))
                     {
                         if (blockBean.getCONCEPT_IDENTIFIER() == null || blockBean.getCONCEPT_IDENTIFIER().equals(""))
                         {
                             storeStatusMsg("This " + sComp
                                             + " is not associated to a concept, so the data is suspect. \\n"
                                             + "Please choose another " + sComp + " .");
                         }
                         else
                         // concept class search results
                         {
                             if (sComp.equals("ObjectClass"))
                                 m_DEC = this.addOCConcepts(req, res, nameAction, m_DEC, blockBean, "Primary");
                             else
                                 m_DEC = this.addPropConcepts(req, res, nameAction, m_DEC, blockBean, "Primary");
                         }
                     }
                     else
                         // split it into concepts for object class or property search results
                         splitIntoConcepts(sComp, blockBean, req, res, nameAction);
                 }
                 else
                 // evs search results
                 {
                     if (sComp.equals("ObjectClass"))
                         m_DEC = this.addOCConcepts(req, res, nameAction, m_DEC, blockBean, "Primary");
                     else
                         m_DEC = this.addPropConcepts(req, res, nameAction, m_DEC, blockBean, "Primary");
                 }
             }
             else if (sComp.equals("ObjectQualifier"))
             {
                 // Do this to reserve zero position in vector for primary concept
                 if (vObjectClass.size() < 1)
                 {
                     EVS_Bean OCBean = new EVS_Bean();
                     vObjectClass.addElement(OCBean);
                     DataManager.setAttribute(session, "vObjectClass", vObjectClass);
                 }
                 m_DEC = this.addOCConcepts(req, res, nameAction, m_DEC, blockBean, "Qualifier");
             }
             else if (sComp.equals("PropertyQualifier"))
             {
                 // Do this to reserve zero position in vector for primary concept
                 if (vProperty.size() < 1)
                 {
                     EVS_Bean PCBean = new EVS_Bean();
                     vProperty.addElement(PCBean);
                     DataManager.setAttribute(session, "vProperty", vProperty);
                 }
                 m_DEC = this.addPropConcepts(req, res, nameAction, m_DEC, blockBean, "Qualifier");
             }
             // rebuild new name if not appending
             if (nameAction.equals("newName"))
                 m_DEC = this.doGetDECNames(req, res, null, "Search", m_DEC);
             else if (nameAction.equals("blockName"))
                 m_DEC = this.doGetDECNames(req, res, null, "blockName", m_DEC);
             DataManager.setAttribute(session, "m_DEC", m_DEC);
         }
         catch (Exception e)
         {
             this.logger.fatal("ERROR - doDECUseSelection : " + e.toString(), e);
         }
     } // end of doDECUseSelection
 
     /**
      * adds the selected concept to the vector of concepts for property
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param nameAction
      *            String naming action
      * @param decBean
      *            selected DEC_Bean
      * @param eBean
      *            selected EVS_Bean
      * @param ocType
      *            String property type (primary or qualifier)
      * @return DEC_Bean
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private DEC_Bean addOCConcepts(HttpServletRequest req, HttpServletResponse res, String nameAction,
                     DEC_Bean decBean, EVS_Bean eBean, String ocType) throws Exception
     {
         HttpSession session = req.getSession();
         // add the concept bean to the OC vector and store it in the vector
         Vector<EVS_Bean> vObjectClass = (Vector) session.getAttribute("vObjectClass");
         if (vObjectClass == null)
             vObjectClass = new Vector<EVS_Bean>();
         // get the evs user bean
         EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; // (EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);
                                                                             // //("EvsUserBean");
         if (eUser == null)
             eUser = new EVS_UserBean();
         eBean.setCON_AC_SUBMIT_ACTION("INS");
         eBean.setCONTE_IDSEQ(decBean.getDEC_CONTE_IDSEQ());
         String eDB = eBean.getEVS_DATABASE();
         if (eDB != null && eBean.getEVS_ORIGIN() != null && eDB.equalsIgnoreCase("caDSR"))
         {
             eDB = eBean.getVocabAttr(eUser, eBean.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME, EVSSearch.VOCAB_DBORIGIN); // "vocabName",
                                                                                                                     // "vocabDBOrigin");
             if (eDB.equals(EVSSearch.META_VALUE)) // "MetaValue"))
                 eDB = eBean.getEVS_ORIGIN();
             eBean.setEVS_DATABASE(eDB); // eBean.getEVS_ORIGIN());
         }
         // get its matching thesaurus concept
         // System.out.println(eBean.getEVS_ORIGIN() + " before thes concept for OC " + eDB);
         EVSSearch evs = new EVSSearch(req, res, this);
         eBean = evs.getThesaurusConcept(eBean);
         // add to the vector and store it in the session, reset if primary and alredy existed, add otehrwise
         if (ocType.equals("Primary") && vObjectClass.size() > 0)
             vObjectClass.setElementAt(eBean, 0);
         else
             vObjectClass.addElement(eBean);
         DataManager.setAttribute(session, "vObjectClass", vObjectClass);
         DataManager.setAttribute(session, "newObjectClass", "true");
         // add rep primary attributes to the vd bean
         if (ocType.equals("Primary"))
         {
             decBean.setDEC_OCL_NAME_PRIMARY(eBean.getLONG_NAME());
             decBean.setDEC_OC_CONCEPT_CODE(eBean.getCONCEPT_IDENTIFIER());
             decBean.setDEC_OC_EVS_CUI_ORIGEN(eBean.getEVS_DATABASE());
             decBean.setDEC_OCL_IDSEQ(eBean.getIDSEQ());
             DataManager.setAttribute(session, "m_OC", eBean);
         }
         // update qualifier vectors
         else
         {
             // add it othe qualifiers attributes of the selected DEC
             Vector<String> vOCQualifierNames = decBean.getDEC_OC_QUALIFIER_NAMES();
             if (vOCQualifierNames == null)
                 vOCQualifierNames = new Vector<String>();
             vOCQualifierNames.addElement(eBean.getLONG_NAME());
             Vector<String> vOCQualifierCodes = decBean.getDEC_OC_QUALIFIER_CODES();
             if (vOCQualifierCodes == null)
                 vOCQualifierCodes = new Vector<String>();
             vOCQualifierCodes.addElement(eBean.getCONCEPT_IDENTIFIER());
             Vector<String> vOCQualifierDB = decBean.getDEC_OC_QUALIFIER_DB();
             if (vOCQualifierDB == null)
                 vOCQualifierDB = new Vector<String>();
             vOCQualifierDB.addElement(eBean.getEVS_DATABASE());
             decBean.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
             decBean.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
             decBean.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
             // if (vOCQualifierNames.size()>0)
             // decBean.setDEC_OBJ_CLASS_QUALIFIER((String)vOCQualifierNames.elementAt(0));
             // store it in the session
             DataManager.setAttribute(session, "m_OCQ", eBean);
         }
         // DataManager.setAttribute(session, "selObjQRow", sSelRow);
         // add to name if appending
         if (nameAction.equals("appendName"))
             decBean = this.doGetDECNames(req, res, eBean, "Search", decBean);
         return decBean;
     } // end addOCConcepts
 
     /**
      * adds the selected concept to the vector of concepts for property
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param nameAction
      *            String naming action
      * @param decBean
      *            selected DEC_Bean
      * @param eBean
      *            selected EVS_Bean
      * @param propType
      *            String property type (primary or qualifier)
      * @return DEC_Bean
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private DEC_Bean addPropConcepts(HttpServletRequest req, HttpServletResponse res, String nameAction,
                     DEC_Bean decBean, EVS_Bean eBean, String propType) throws Exception
     {
         HttpSession session = req.getSession();
         // add the concept bean to the OC vector and store it in the vector
         Vector<EVS_Bean> vProperty = (Vector) session.getAttribute("vProperty");
         if (vProperty == null)
             vProperty = new Vector<EVS_Bean>();
         // get the evs user bean
         EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; // (EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);
                                                                             // //("EvsUserBean");
         if (eUser == null)
             eUser = new EVS_UserBean();
         eBean.setCON_AC_SUBMIT_ACTION("INS");
         eBean.setCONTE_IDSEQ(decBean.getDEC_CONTE_IDSEQ());
         String eDB = eBean.getEVS_DATABASE();
         if (eDB != null && eBean.getEVS_ORIGIN() != null && eDB.equalsIgnoreCase("caDSR"))
         {
             eDB = eBean.getVocabAttr(eUser, eBean.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME, EVSSearch.VOCAB_DBORIGIN); // "vocabName",
                                                                                                                     // "vocabDBOrigin");
             if (eDB.equals(EVSSearch.META_VALUE)) // "MetaValue"))
                 eDB = eBean.getEVS_ORIGIN();
             eBean.setEVS_DATABASE(eDB); // eBean.getEVS_ORIGIN());
         }
         // System.out.println(eBean.getEVS_ORIGIN() + " before thes concept for PROP " + eDB);
         EVSSearch evs = new EVSSearch(req, res, this);
         eBean = evs.getThesaurusConcept(eBean);
         // add to the vector and store it in the session, reset if primary and alredy existed, add otehrwise
         if (propType.equals("Primary") && vProperty.size() > 0)
             vProperty.setElementAt(eBean, 0);
         else
             vProperty.addElement(eBean);
         DataManager.setAttribute(session, "vProperty", vProperty);
         DataManager.setAttribute(session, "newProperty", "true");
         // add rep primary attributes to the vd bean
         if (propType.equals("Primary"))
         {
             decBean.setDEC_PROPL_NAME_PRIMARY(eBean.getLONG_NAME());
             decBean.setDEC_PROP_CONCEPT_CODE(eBean.getCONCEPT_IDENTIFIER());
             decBean.setDEC_PROP_EVS_CUI_ORIGEN(eBean.getEVS_DATABASE());
             decBean.setDEC_PROPL_IDSEQ(eBean.getIDSEQ());
             DataManager.setAttribute(session, "m_PC", eBean);
         }
         // update qualifier vectors
         else
         {
             Vector<String> vPropQualifierNames = decBean.getDEC_PROP_QUALIFIER_NAMES();
             if (vPropQualifierNames == null)
                 vPropQualifierNames = new Vector<String>();
             vPropQualifierNames.addElement(eBean.getLONG_NAME());
             Vector<String> vPropQualifierCodes = decBean.getDEC_PROP_QUALIFIER_CODES();
             if (vPropQualifierCodes == null)
                 vPropQualifierCodes = new Vector<String>();
             vPropQualifierCodes.addElement(eBean.getCONCEPT_IDENTIFIER());
             Vector<String> vPropQualifierDB = decBean.getDEC_PROP_QUALIFIER_DB();
             if (vPropQualifierDB == null)
                 vPropQualifierDB = new Vector<String>();
             vPropQualifierDB.addElement(eBean.getEVS_DATABASE());
             decBean.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
             decBean.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
             decBean.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
             // if(vPropQualifierNames.size()>0)
             // decBean.setDEC_PROPERTY_QUALIFIER((String)vPropQualifierNames.elementAt(0));
             DataManager.setAttribute(session, "m_PCQ", eBean);
         }
         // DataManager.setAttribute(session, "selObjQRow", sSelRow);
         // add to name if appending
         if (nameAction.equals("appendName"))
             decBean = this.doGetDECNames(req, res, eBean, "Search", decBean);
         return decBean;
     } // end addPropConcepts
 
     /**
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param nameAction
      *            stirng name action
      *
      */
     @SuppressWarnings("unchecked")
     public void doVDUseSelection(HttpServletRequest req, HttpServletResponse res, String nameAction)
     {
         try
         {
             HttpSession session = req.getSession();
             String sSelRow = "";
           //  InsACService insAC = new InsACService(req, res, this);
             VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD");
             if (m_VD == null)
                 m_VD = new VD_Bean();
             m_setAC.setVDValueFromPage(req, res, m_VD);
             Vector<EVS_Bean> vRepTerm = (Vector) session.getAttribute("vRepTerm");
             if (vRepTerm == null)
                 vRepTerm = new Vector<EVS_Bean>();
             Vector vAC = new Vector();
             ;
             EVS_Bean m_REP = new EVS_Bean();
             String sComp = (String) req.getParameter("sCompBlocks");
             // get rep term components
             if (sComp.equals("RepTerm") || sComp.equals("RepQualifier"))
             {
                 sSelRow = (String) req.getParameter("selRepRow");
                 // vAC = (Vector)session.getAttribute("vRepResult");
                 vAC = (Vector) session.getAttribute("vACSearch");
                 if (vAC == null)
                     vAC = new Vector();
                 if (sSelRow != null && !sSelRow.equals(""))
                 {
                     String sObjRow = sSelRow.substring(2);
                     Integer intObjRow = new Integer(sObjRow);
                     int intObjRow2 = intObjRow.intValue();
                     if (vAC.size() > intObjRow2 - 1)
                         m_REP = (EVS_Bean) vAC.elementAt(intObjRow2);
                     // get name value pari
                     String sNVP = (String) req.getParameter("nvpConcept");
                     if (sNVP != null && !sNVP.equals(""))
                     {
                         m_REP.setNVP_CONCEPT_VALUE(sNVP);
                         String sName = m_REP.getLONG_NAME();
                         m_REP.setLONG_NAME(sName + "::" + sNVP);
                         m_REP.setPREFERRED_DEFINITION(m_REP.getPREFERRED_DEFINITION() + "::" + sNVP);
                     }
                     System.out.println(sNVP + sComp + m_REP.getLONG_NAME());
                 }
                 else
                 {
                     storeStatusMsg("Unable to get the selected row from the Rep Term search results.");
                     return;
                 }
                 // send it back if unable to obtion the concept
                 if (m_REP == null || m_REP.getLONG_NAME() == null)
                 {
                     storeStatusMsg("Unable to obtain concept from the selected row of the " + sComp
                                     + " search results.\\n" + "Please try again.");
                     return;
                 }
                 // handle the primary search
                 if (sComp.equals("RepTerm"))
                 {
                     if (m_REP.getEVS_DATABASE().equals("caDSR"))
                     {
                         // split it if rep term, add concept class to the list if evs id exists
                         if (m_REP.getCONDR_IDSEQ() == null || m_REP.getCONDR_IDSEQ().equals(""))
                         {
                             if (m_REP.getCONCEPT_IDENTIFIER() == null || m_REP.getCONCEPT_IDENTIFIER().equals(""))
                             {
                                 storeStatusMsg("This Rep Term is not associated to a concept, so the data is suspect. \\n"
                                                                 + "Please choose another Rep Term.");
                             }
                             else
                                 m_VD = this.addRepConcepts(req, res, nameAction, m_VD, m_REP, "Primary");
                         }
                         else
                             splitIntoConceptsVD(sComp, m_REP, req, res, nameAction);
                     }
                     else
                         m_VD = this.addRepConcepts(req, res, nameAction, m_VD, m_REP, "Primary");
                 }
                 else if (sComp.equals("RepQualifier"))
                 {
                     // Do this to reserve zero position in vector for primary concept
                     if (vRepTerm.size() < 1)
                     {
                         EVS_Bean OCBean = new EVS_Bean();
                         vRepTerm.addElement(OCBean);
                         DataManager.setAttribute(session, "vRepTerm", vRepTerm);
                     }
                     m_VD = this.addRepConcepts(req, res, nameAction, m_VD, m_REP, "Qualifier");
                 }
             }
             else
             {
                 EVS_Bean eBean = this.getEVSSelRow(req);
                 if (eBean != null && eBean.getLONG_NAME() != null)
                 {
                     if (sComp.equals("VDObjectClass"))
                     {
                         m_VD.setVD_OBJ_CLASS(eBean.getLONG_NAME());
                         DataManager.setAttribute(session, "m_OC", eBean);
                     }
                     else if (sComp.equals("VDPropertyClass"))
                     {
                         m_VD.setVD_PROP_CLASS(eBean.getLONG_NAME());
                         DataManager.setAttribute(session, "m_PC", eBean);
                     }
                     if (nameAction.equals("appendName"))
                         m_VD = this.doGetVDNames(req, res, eBean, "Search", m_VD);
                 }
             }
             // rebuild new name if not appending
             if (nameAction.equals("newName"))
                 m_VD = this.doGetVDNames(req, res, null, "Search", m_VD);
             else if (nameAction.equals("blockName"))
                 m_VD = this.doGetVDNames(req, res, null, "blockName", m_VD);
             DataManager.setAttribute(session, "m_VD", m_VD);
         }
         catch (Exception e)
         {
             this.logger.fatal("ERROR - doVDUseSelection : " + e.toString(), e);
         }
     } // end of doVDUseSelection
 
     /**
      * adds the selected concept to the vector of concepts for property
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param nameAction
      *            String naming action
      * @param vdBean
      *            selected DEC_Bean
      * @param eBean
      *            selected EVS_Bean
      * @param repType
      *            String property type (primary or qualifier)
      * @return DEC_Bean
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private VD_Bean addRepConcepts(HttpServletRequest req, HttpServletResponse res, String nameAction, VD_Bean vdBean,
                     EVS_Bean eBean, String repType) throws Exception
     {
         HttpSession session = req.getSession();
         // add the concept bean to the OC vector and store it in the vector
         Vector<EVS_Bean> vRepTerm = (Vector) session.getAttribute("vRepTerm");
         if (vRepTerm == null)
             vRepTerm = new Vector<EVS_Bean>();
         // get the evs user bean
         EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; // (EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);
                                                                             // //("EvsUserBean");
         if (eUser == null)
             eUser = new EVS_UserBean();
         eBean.setCON_AC_SUBMIT_ACTION("INS");
         eBean.setCONTE_IDSEQ(vdBean.getVD_CONTE_IDSEQ());
         String eDB = eBean.getEVS_DATABASE();
         if (eDB != null && eBean.getEVS_ORIGIN() != null && eDB.equalsIgnoreCase("caDSR"))
         {
             eDB = eBean.getVocabAttr(eUser, eBean.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME, EVSSearch.VOCAB_DBORIGIN); // "vocabName",
                                                                                                                     // "vocabDBOrigin");
             if (eDB.equals(EVSSearch.META_VALUE)) // "MetaValue"))
                 eDB = eBean.getEVS_ORIGIN();
             eBean.setEVS_DATABASE(eDB); // eBean.getEVS_ORIGIN());
         }
         // System.out.println(eBean.getEVS_ORIGIN() + " before thes concept for REP " + eDB);
         EVSSearch evs = new EVSSearch(req, res, this);
         eBean = evs.getThesaurusConcept(eBean);
         // add to the vector and store it in the session, reset if primary and alredy existed, add otehrwise
         if (repType.equals("Primary") && vRepTerm.size() > 0)
             vRepTerm.setElementAt(eBean, 0);
         else
             vRepTerm.addElement(eBean);
         DataManager.setAttribute(session, "vRepTerm", vRepTerm);
         DataManager.setAttribute(session, "newRepTerm", "true");
         // add rep primary attributes to the vd bean
         if (repType.equals("Primary"))
         {
             vdBean.setVD_REP_NAME_PRIMARY(eBean.getLONG_NAME());
             vdBean.setVD_REP_CONCEPT_CODE(eBean.getCONCEPT_IDENTIFIER());
             vdBean.setVD_REP_EVS_CUI_ORIGEN(eBean.getEVS_DATABASE());
             vdBean.setVD_REP_IDSEQ(eBean.getIDSEQ());
             DataManager.setAttribute(session, "m_REP", eBean);
         }
         else
         {
             // add rep qualifiers to the vector
             Vector<String> vRepQualifierNames = vdBean.getVD_REP_QUALIFIER_NAMES();
             if (vRepQualifierNames == null)
                 vRepQualifierNames = new Vector<String>();
             vRepQualifierNames.addElement(eBean.getLONG_NAME());
             Vector<String> vRepQualifierCodes = vdBean.getVD_REP_QUALIFIER_CODES();
             if (vRepQualifierCodes == null)
                 vRepQualifierCodes = new Vector<String>();
             vRepQualifierCodes.addElement(eBean.getCONCEPT_IDENTIFIER());
             Vector<String> vRepQualifierDB = vdBean.getVD_REP_QUALIFIER_DB();
             if (vRepQualifierDB == null)
                 vRepQualifierDB = new Vector<String>();
             vRepQualifierDB.addElement(eBean.getEVS_DATABASE());
             vdBean.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
             vdBean.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
             vdBean.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
             // if(vRepQualifierNames.size()>0)
             // vdBean.setVD_REP_QUAL((String)vRepQualifierNames.elementAt(0));
             DataManager.setAttribute(session, "vRepQResult", null);
             DataManager.setAttribute(session, "m_REPQ", eBean);
         }
         // DataManager.setAttribute(session, "selRepQRow", sSelRow);
         // add to name if appending
         if (nameAction.equals("appendName"))
             vdBean = this.doGetVDNames(req, res, eBean, "Search", vdBean);
         return vdBean;
     } // end addRepConcepts
 
     /**
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      *
      */
     @SuppressWarnings("unchecked")
     public void doRemoveBuildingBlocks(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sSelRow = "";
         DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
         if (m_DEC == null)
             m_DEC = new DEC_Bean();
         Vector<EVS_Bean> vObjectClass = (Vector) session.getAttribute("vObjectClass");
         if (vObjectClass == null)
             vObjectClass = new Vector<EVS_Bean>();
     //    int iOCSize = vObjectClass.size();
         Vector<EVS_Bean> vProperty = (Vector) session.getAttribute("vProperty");
         if (vProperty == null)
             vProperty = new Vector<EVS_Bean>();
      //   int iPropSize = vProperty.size();
         String sComp = (String) req.getParameter("sCompBlocks");
         if (sComp == null)
             sComp = "";
         if (sComp.equals("ObjectClass"))
         {
             EVS_Bean m_OC = new EVS_Bean();
             vObjectClass.setElementAt(m_OC, 0);
             DataManager.setAttribute(session, "vObjectClass", vObjectClass);
             m_DEC.setDEC_OCL_NAME_PRIMARY("");
             m_DEC.setDEC_OC_CONCEPT_CODE("");
             m_DEC.setDEC_OC_EVS_CUI_ORIGEN("");
             m_DEC.setDEC_OCL_IDSEQ("");
             DataManager.setAttribute(session, "RemoveOCBlock", "true");
             DataManager.setAttribute(session, "newObjectClass", "true");
         }
         else if (sComp.equals("Property") || sComp.equals("PropertyClass"))
         {
             EVS_Bean m_PC = new EVS_Bean();
             vProperty.setElementAt(m_PC, 0);
             DataManager.setAttribute(session, "vProperty", vProperty);
             m_DEC.setDEC_PROPL_NAME_PRIMARY("");
             m_DEC.setDEC_PROP_CONCEPT_CODE("");
             m_DEC.setDEC_PROP_EVS_CUI_ORIGEN("");
             m_DEC.setDEC_PROPL_IDSEQ("");
             DataManager.setAttribute(session, "RemovePropBlock", "true");
             DataManager.setAttribute(session, "newProperty", "true");
         }
         else if (sComp.equals("ObjectQualifier"))
         {
             sSelRow = (String) req.getParameter("selObjQRow");
             if (sSelRow != null && !(sSelRow.equals("")))
             {
                 Integer intObjRow = new Integer(sSelRow);
                 int intObjRow2 = intObjRow.intValue();
                 // add 1 because 0 element is OC, not a qualifier
                 int int1 = intObjRow2 + 1;
                 if (vObjectClass.size() > (int1))
                 {
                     vObjectClass.removeElementAt(int1);
                     DataManager.setAttribute(session, "vObjectClass", vObjectClass);
                 }
                 // m_DEC.setDEC_OBJ_CLASS_QUALIFIER("");
                 Vector vOCQualifierNames = m_DEC.getDEC_OC_QUALIFIER_NAMES();
                 if (vOCQualifierNames == null)
                     vOCQualifierNames = new Vector();
                 if (vOCQualifierNames.size() > intObjRow2)
                     vOCQualifierNames.removeElementAt(intObjRow2);
                 Vector vOCQualifierCodes = m_DEC.getDEC_OC_QUALIFIER_CODES();
                 if (vOCQualifierCodes == null)
                     vOCQualifierCodes = new Vector();
                 if (vOCQualifierCodes.size() > intObjRow2)
                     vOCQualifierCodes.removeElementAt(intObjRow2);
                 Vector vOCQualifierDB = m_DEC.getDEC_OC_QUALIFIER_DB();
                 if (vOCQualifierDB == null)
                     vOCQualifierDB = new Vector();
                 if (vOCQualifierDB.size() > intObjRow2)
                     vOCQualifierDB.removeElementAt(intObjRow2);
                 m_DEC.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
                 m_DEC.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
                 m_DEC.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
                 DataManager.setAttribute(session, "RemoveOCBlock", "true");
                 DataManager.setAttribute(session, "newObjectClass", "true");
                 DataManager.setAttribute(session, "m_OCQ", null);
             }
         }
         else if (sComp.equals("PropertyQualifier"))
         {
             sSelRow = (String) req.getParameter("selPropQRow");
             if (sSelRow != null && !(sSelRow.equals("")))
             {
                 Integer intPropRow = new Integer(sSelRow);
                 int intPropRow2 = intPropRow.intValue();
                 // add 1 because 0 element is OC, not a qualifier
                 int int1 = intPropRow2 + 1;
                 // invert because the list on ui is i9nverse to vector
                 if (vProperty.size() > (int1))
                 {
                     vProperty.removeElementAt(int1);
                     DataManager.setAttribute(session, "vProperty", vProperty);
                 }
                 // m_DEC.setDEC_PROPERTY_QUALIFIER("");
                 Vector vPropQualifierNames = m_DEC.getDEC_PROP_QUALIFIER_NAMES();
                 if (vPropQualifierNames == null)
                     vPropQualifierNames = new Vector();
                 if (vPropQualifierNames.size() > intPropRow2)
                     vPropQualifierNames.removeElementAt(intPropRow2);
                 Vector vPropQualifierCodes = m_DEC.getDEC_PROP_QUALIFIER_CODES();
                 if (vPropQualifierCodes == null)
                     vPropQualifierCodes = new Vector();
                 if (vPropQualifierCodes.size() > intPropRow2)
                     vPropQualifierCodes.removeElementAt(intPropRow2);
                 Vector vPropQualifierDB = m_DEC.getDEC_PROP_QUALIFIER_DB();
                 if (vPropQualifierDB == null)
                     vPropQualifierDB = new Vector();
                 if (vPropQualifierDB.size() > intPropRow2)
                     vPropQualifierDB.removeElementAt(intPropRow2);
                 m_DEC.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
                 m_DEC.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
                 m_DEC.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
                 DataManager.setAttribute(session, "RemovePropBlock", "true");
                 DataManager.setAttribute(session, "newObjectClass", "true");
                 DataManager.setAttribute(session, "m_PCQ", null);
             }
         }
         m_setAC.setDECValueFromPage(req, res, m_DEC);
         DataManager.setAttribute(session, "m_DEC", m_DEC);
     } // end of doRemoveQualifier
 
     /**
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      *
      */
     @SuppressWarnings("unchecked")
     public void doRemoveBuildingBlocksVD(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sSelRow = "";
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD");
         if (m_VD == null)
             m_VD = new VD_Bean();
         Vector<EVS_Bean> vRepTerm = (Vector) session.getAttribute("vRepTerm");
         if (vRepTerm == null)
             vRepTerm = new Vector<EVS_Bean>();
         String sComp = (String) req.getParameter("sCompBlocks");
         if (sComp == null)
             sComp = "";
         if (sComp.equals("RepTerm"))
         {
             EVS_Bean m_REP = new EVS_Bean();
             vRepTerm.setElementAt(m_REP, 0);
             DataManager.setAttribute(session, "vRepTerm", vRepTerm);
             m_VD.setVD_REP_NAME_PRIMARY("");
             m_VD.setVD_REP_CONCEPT_CODE("");
             m_VD.setVD_REP_EVS_CUI_ORIGEN("");
             m_VD.setVD_REP_IDSEQ("");
             DataManager.setAttribute(session, "RemoveRepBlock", "true");
             DataManager.setAttribute(session, "newRepTerm", "true");
         }
         else if (sComp.equals("RepQualifier"))
         {
             sSelRow = (String) req.getParameter("selRepQRow");
             if (sSelRow != null && !(sSelRow.equals("")))
             {
                 Integer intObjRow = new Integer(sSelRow);
                 int intObjRow2 = intObjRow.intValue();
                 if (vRepTerm.size() > (intObjRow2 + 1))
                 {
                     vRepTerm.removeElementAt(intObjRow2 + 1); // add 1 so zero element not removed
                     DataManager.setAttribute(session, "vRepTerm", vRepTerm);
                 }
                 // m_VD.setVD_REP_QUAL("");
                 Vector vRepQualifierNames = m_VD.getVD_REP_QUALIFIER_NAMES();
                 if (vRepQualifierNames == null)
                     vRepQualifierNames = new Vector();
                 if (vRepQualifierNames.size() > intObjRow2)
                     vRepQualifierNames.removeElementAt(intObjRow2);
                 Vector vRepQualifierCodes = m_VD.getVD_REP_QUALIFIER_CODES();
                 if (vRepQualifierCodes == null)
                     vRepQualifierCodes = new Vector();
                 if (vRepQualifierCodes.size() > intObjRow2)
                     vRepQualifierCodes.removeElementAt(intObjRow2);
                 Vector vRepQualifierDB = m_VD.getVD_REP_QUALIFIER_DB();
                 if (vRepQualifierDB == null)
                     vRepQualifierDB = new Vector();
                 if (vRepQualifierDB.size() > intObjRow2)
                     vRepQualifierDB.removeElementAt(intObjRow2);
                 m_VD.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
                 m_VD.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
                 m_VD.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
                 DataManager.setAttribute(session, "RemoveRepBlock", "true");
                 DataManager.setAttribute(session, "newRepTerm", "true");
             }
         }
         else if (sComp.equals("VDObjectClass"))
         {
             m_VD.setVD_OBJ_CLASS("");
             DataManager.setAttribute(session, "m_OC", new EVS_Bean());
         }
         else if (sComp.equals("VDPropertyClass"))
         {
             m_VD.setVD_PROP_CLASS("");
             DataManager.setAttribute(session, "m_PC", new EVS_Bean());
         }
         m_setAC.setVDValueFromPage(req, res, m_VD);
         DataManager.setAttribute(session, "m_VD", m_VD);
     } // end of doRemoveQualifier
 
     /**
      * to store the selected value meanings from EVS into pv bean to submit later.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sPVAction
      *            string pv action
      *
      */
     public void doSelectVMConcept(HttpServletRequest req, HttpServletResponse res, String sPVAction)
     {
         try
         {
             //InsACService insAC = new InsACService(req, res, this);
             HttpSession session = req.getSession();
             DataManager.setAttribute(session, "PVAction", sPVAction);
             VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
             // get the existing pvs from the session
             Vector<PV_Bean> vVDPVList = m_VD.getVD_PV_List(); // (Vector)session.getAttribute("VDPVList");
             if (vVDPVList == null)
                 vVDPVList = new Vector<PV_Bean>();
             // get the VMs selected from EVS from the page.
             Vector<EVS_Bean> vEVSList = this.getEVSSelRowVector(req, new Vector<EVS_Bean>());
             if (vEVSList != null && vEVSList.size() > 0)
             {
                 // get the parent concept which is same for all the selected values
                 String sSelectedParentName = (String) session.getAttribute("SelectedParentName");
                 String sSelectedParentCC = (String) session.getAttribute("SelectedParentCC");
                 String sSelectedParentDB = (String) session.getAttribute("SelectedParentDB");
                 String sSelectedParentMetaSource = (String) session.getAttribute("SelectedParentMetaSource");
                 // get the parent concept
                 EVS_Bean parConcept = new EVS_Bean();
                 if (sSelectedParentName != null && !sSelectedParentName.equals(""))
                 {
                     parConcept.setLONG_NAME(sSelectedParentName);
                     parConcept.setCONCEPT_IDENTIFIER(sSelectedParentCC);
                     parConcept.setEVS_DATABASE(sSelectedParentDB);
                     parConcept.setEVS_CONCEPT_SOURCE(sSelectedParentMetaSource);
                 }
                 String notUpdateVDPVs = "";
                 // String updatedVDPVs = "";
                 for (int i = 0; i < vEVSList.size(); i++)
                 {
                     EVS_Bean eBean = (EVS_Bean) vEVSList.elementAt(i);
                     EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; //
 
                     if (eUser == null)
                         eUser = new EVS_UserBean();
                     // get the nci vocab if it meta or other vocab only if not referenced
                     if (sSelectedParentName == null || sSelectedParentName.equals(""))
                     {
                         // get teh right vocab name
                         String eDB = eBean.getVocabAttr(eUser, eBean.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME,
                                         EVSSearch.VOCAB_DBORIGIN); // "vocabName", "vocabDBOrigin");
                         if (eDB.equals(EVSSearch.META_VALUE)) // "MetaValue"))
                             eDB = eBean.getEVS_ORIGIN();
                         eBean.setEVS_DATABASE(eDB); // eBean.getEVS_ORIGIN());
                         // get the thesaurus term
                         EVSSearch evs = new EVSSearch(req, res, this);
                         eBean = evs.getThesaurusConcept(eBean);
                     }
                     // get cadsr data
                     ConceptAction conact = new ConceptAction();
                     ConceptForm data = new ConceptForm();
                     //data.setDBConnection(this.connectDB(req, res));
                     data.setDBConnection(m_conn);
                     data.setCurationServlet(this);
                     eBean = conact.getCaDSRConcept(eBean, eUser, data);
                     //this.freeConnection(data.getDBConnection());
                     String  errMsg = data.getStatusMsg();
                     if (!errMsg.equals(""))
                         storeStatusMsg(errMsg + "\\n");
                     String sValue = eBean.getLONG_NAME();
                     String sMean = eBean.getLONG_NAME();
                     System.out.println(sValue + " selectVMConcept " + sMean);
                     // add the level to the value if parent exists to update the value
                     if (sSelectedParentName != null && !sSelectedParentName.equals(""))
                     {
                         Integer iLevel = new Integer(eBean.getLEVEL());
                         sValue = sValue + " [" + iLevel.toString() + "]";
                     }
                     boolean isExist = false;
                     boolean isUpdated = false;
                     // int updRow = -1;
                     for (int j = 0; j < vVDPVList.size(); j++) // check if the concept already exists.
                     {
                         PV_Bean pvBean = new PV_Bean();
                         pvBean = (PV_Bean) vVDPVList.elementAt(j);
                         VM_Bean vdVM = pvBean.getPV_VM();
                         String vdValue = pvBean.getPV_VALUE();
                         String vdMean = "";
                         if (vdVM != null)
                             vdMean = vdVM.getVM_SHORT_MEANING(); // pvBean.getPV_SHORT_MEANING();
                         // check if value meaning was already existed
                         if (vdMean != null && vdMean.equalsIgnoreCase(sMean))
                         {
                             String pvSubmit = pvBean.getVP_SUBMIT_ACTION();
                             // put back the deleted pv if it has same value-vm pair
                             if (pvSubmit != null && pvSubmit.equals("DEL") && vdValue.equalsIgnoreCase(sValue))
                             {
                                 // set to update if idseq is non evs and is from cadsr
                                 if (pvBean.getPV_PV_IDSEQ() != null && !pvBean.getPV_PV_IDSEQ().equals("EVS_" + sValue))
                                     pvBean.setVP_SUBMIT_ACTION("UPD");
                                 else
                                     pvBean.setVP_SUBMIT_ACTION("INS"); // evs term
                                 // mark as deleted
                                 isUpdated = true;
                                 // updRow = j; //need this to update the vector
                                 this.storePVinVDPVList(vVDPVList, pvBean, eBean, parConcept, sValue, sMean, j, true);
                             }
                             else if (pvSubmit != null && !pvSubmit.equals("DEL") && vdValue.equalsIgnoreCase(sValue)) // was not deleted
                             {
                                 String sValMean = "\\tValue: " + sValue + " and Meaning: "
                                                 + sMean + "\\n";
                                 notUpdateVDPVs += sValMean;
                                 isExist = true;
                             }
                         }
                     }
                     // add to the bean if not exists
                     if (isExist == false && !isUpdated)
                     {
                         this.storePVinVDPVList(vVDPVList, new PV_Bean(), eBean, parConcept, sValue, sMean, -1, false);
                     }
                 }
                 // System.out.println(updatedVDPVs + " selMinVD " + vVDPVList.size());
                 // DataManager.setAttribute(session, "VDPVList", vVDPVList);
                 m_VD.setVD_PV_List(vVDPVList);
                 DataManager.setAttribute(session, "m_VD", m_VD);
                 // alert if value meaning alredy exists but updated with concept info
                 /*
                  * if (updatedVDPVs != null && !updatedVDPVs.equals("")) { String stMsg = "The following Value and
                  * Meaning is updated with the Concept Relationship. \\n"; InsACService insAC = new InsACService(req,
                  * res, this); insAC.storeStatusMsg(stMsg + updatedVDPVs); }
                  */// alert if value meaning alredy exists for pv on the page
                 if (notUpdateVDPVs != null && !notUpdateVDPVs.equals(""))
                 {
                     String stMsg = "The following Value and Meaning already exists in the Value Domain.\\n";
                     storeStatusMsg(stMsg + notUpdateVDPVs);
                 }
             }
         }
         catch (Exception e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             logger.fatal("ERROR - ", e);
         }
     }
 
     /**
      * stores the selected pvs from concepts search in vdpv list
      * @param vVPList Vector<PV_Bean> object
      * @param pBean PV_Bean permissible value object
      * @param eBean EVS_Bean concept object
      * @param parBean EVS_Bean parent concept
      * @param sValue String value
      * @param sMean String value meaning
      * @param updRow int single row
      * @param isUpdated boolean value of updated or not
      * @return Vector<PV_Bean> object
      */
     private Vector<PV_Bean> storePVinVDPVList(Vector<PV_Bean> vVPList, PV_Bean pBean, EVS_Bean eBean, EVS_Bean parBean,
                     String sValue, String sMean, int updRow, boolean isUpdated)
     {
         try
         {
             if (!isUpdated) // do not update these if
             {
                 // store concept name as value and vm in the pv bean
                 // pBean = new PV_Bean();
                 pBean.setPV_PV_IDSEQ("EVS_" + sValue); // store id as EVS
                 pBean.setPV_VALUE(sValue);
                 pBean.setPV_SHORT_MEANING(sMean);
                 pBean.setVP_SUBMIT_ACTION("INS");
             }
             // allow to update the definition if different description for evs selected items
             pBean.setPV_MEANING_DESCRIPTION(eBean.getPREFERRED_DEFINITION());
             // these are for vd-pvs
             SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
             if (pBean.getVP_SUBMIT_ACTION().equals("INS"))
                 pBean.setPV_BEGIN_DATE(formatter.format(new java.util.Date()));
             // add evs bean to pv bean
             VM_Bean vm = new VM_Bean();
             vm.setVM_SHORT_MEANING(pBean.getPV_SHORT_MEANING());
             vm.setVM_DESCRIPTION(pBean.getPV_MEANING_DESCRIPTION());
             vm.setVM_CONDR_IDSEQ(eBean.getCONDR_IDSEQ());
             Vector<EVS_Bean> vmCon = new Vector<EVS_Bean>();
             vmCon.addElement(eBean);
             vm.setVM_CONCEPT_LIST(vmCon);
             vm.setVM_SUBMIT_ACTION(VMForm.CADSR_ACTION_INS);
             pBean.setPV_VM(vm);
             System.out.println(eBean.getCONCEPT_IDENTIFIER() + " vm concepts " + vmCon.size());
             // pBean.setVM_CONCEPT(eBean);
             pBean.setPARENT_CONCEPT(parBean);
             if (isUpdated)
                 vVPList.setElementAt(pBean, updRow); // udpate the vector
             else
                 vVPList.addElement(pBean); // store bean in vector
         }
         catch (Exception e)
         {
             logger.fatal("Error - store value and meaning in vdpv list: Value - " + sValue + " and Meaning - " + sMean, e);
         }
         return vVPList;
     }
 
 
     /**
      * The doValidateVD method gets the values from page the user filled out, validates the input, then forwards results
      * to the Validate page Called from 'doCreateVDActions', 'doSubmitVD' method. Calls 'setAC.setVDValueFromPage' to
      * set the data from the page to the bean. Calls 'setAC.setValidatePageValuesVD' to validate the data. Stores 'm_VD'
      * bean in session. Forwards the page 'ValidateVDPage.jsp' with validation vector to display.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doValidateVDBlockEdit(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
         DataManager.setAttribute(session, "VDPageAction", "validate"); // store the page action in attribute
         m_setAC.setVDValueFromPage(req, res, m_VD);
         DataManager.setAttribute(session, "m_VD", m_VD);
         m_setAC.setValidateBlockEdit(req, res, "ValueDomain");
         DataManager.setAttribute(session, "VDEditAction", "VDBlockEdit");
         ForwardJSP(req, res, "/ValidateVDPage.jsp");
     } // end of doValidateVD
 
     /**
      * The doInsertDE method to insert or update record in the database. Called from 'service' method where reqType is
      * 'validateDEFromForm'. Retrieves the session bean m_DE. if the action is reEditDE forwards the page back to Edit
      * or create pages. Otherwise, calls 'insAC.setDE' to submit the record to the database null value return would
      * store the statusMessage as successful in session and forwards the page 'SearchResultsPage.jsp' for create new
      * from template, new version, or Edit actions, and 'CreateDEPage.jsp' for create new DE action. If ret is not null
      * stores the statusMessage as error message in session and forwards the page 'CreateDEPage.jsp' for create and
      * 'EditDEPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doInsertDE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // make sure that status message is empty
         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
         Vector vStat = new Vector();
         DataManager.setAttribute(session, "vStatMsg", vStat);
         String sAction = (String) req.getParameter("ValidateDEPageAction");
         String sDEEditAction = (String) session.getAttribute("DEEditAction");
         if (sDEEditAction == null)
             sDEEditAction = "";
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sOriginAction == null)
             sOriginAction = "";
         String sDEAction = (String) session.getAttribute("DEAction");
         if (sDEAction == null)
             sDEAction = "";
         //String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         if (sAction == null)
             sAction = "submitting"; // covers direct submits without going to Validate page
         if (sAction != null)
         {
             // go back from validation page according editing or creating action
             if (sAction.equals("reEditDE"))
             {
                 if (sDEAction.equals("EditDE"))
                     ForwardJSP(req, res, "/EditDEPage.jsp");
                 else
                     ForwardJSP(req, res, "/CreateDEPage.jsp");
             }
             else
             {
                 if (sDEAction.equals("EditDE") && !sOriginAction.equals("BlockEditDE"))
                     doUpdateDEAction(req, res);
                 // update the data for block editing
                 else if (sDEEditAction.equals("DEBlockEdit"))
                     doUpdateDEActionBE(req, res);
                 // insert a new one if create new, template or version
                 else
                     doInsertDEfromMenuAction(req, res);
             }
         }
     } // end of doInsertDE
 
     /**
      * update record in the database and display the result. Called from 'doInsertDE' method when the aciton is editing.
      * Retrieves the session bean m_DE. calls 'insAC.setDE' to update the database. calls 'serAC.refreshData' to get the
      * refreshed search result forwards the page back to search page with refreshed list after updating.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'EditDEPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doUpdateDEAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         InsACService insAC = new InsACService(req, res, this); //
         DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
         DE_Bean oldDEBean = (DE_Bean) session.getAttribute("oldDEBean");
         // udpate the status message with DE name and ID
         storeStatusMsg("Data Element Name : " + DEBean.getDE_LONG_NAME());
         storeStatusMsg("Public ID : " + DEBean.getDE_MIN_CDE_ID());
         // call stored procedure to update attributes
         String ret = insAC.setDE("UPD", DEBean, "Edit", oldDEBean);
         // forwards to search page if successful
         if ((ret == null) || ret.equals(""))
         {
             ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), ""); // set DEComp rules and relations
             GetACSearch serAC = new GetACSearch(req, res, this);
             String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
             // forward to search page with the refreshed question list
             if (sMenuAction.equals("Questions"))
             {
                 DataManager.setAttribute(session, "searchAC", "Questions");
                 Quest_Bean questBean = (Quest_Bean) session.getAttribute("m_Quest");
                 if (questBean != null)
                 {
                     questBean.setQUEST_DEFINITION(DEBean.getDE_PREFERRED_DEFINITION());
                     questBean.setSUBMITTED_LONG_NAME(DEBean.getDOC_TEXT_PREFERRED_QUESTION());
                     questBean.setDE_IDSEQ(DEBean.getDE_DE_IDSEQ());
                     questBean.setDE_LONG_NAME(DEBean.getDE_LONG_NAME());
                     questBean.setVD_IDSEQ(DEBean.getDE_VD_IDSEQ());
                     questBean.setVD_LONG_NAME(DEBean.getDE_VD_NAME());
                     questBean.setSTATUS_INDICATOR("Edit");
                     serAC.refreshData(req, res, null, null, null, questBean, "Edit", "");
                     // reset the appened attributes to remove all the checking of the row
                     Vector vCheck = new Vector();
                     DataManager.setAttribute(session, "CheckList", vCheck);
                     DataManager.setAttribute(session, "AppendAction", "Not Appended");
                     // call the method to update Quest contents table
                     ret = insAC.setQuestContent(questBean, "", "");
                     DataManager.setAttribute(session, "m_Quest", questBean);
                 }
             }
             else
             {
                 DEBean.setDE_ALIAS_NAME(DEBean.getDE_PREFERRED_NAME());
                 DEBean.setDE_TYPE_NAME("PRIMARY");
                 String oldID = oldDEBean.getDE_DE_IDSEQ();
                 serAC.refreshData(req, res, DEBean, null, null, null, "Edit", oldID);
             }
             ForwardJSP(req, res, "/SearchResultsPage.jsp"); // forward to search page
         }
         // back to edit page if not successful
         else
         {
             // DataManager.setAttribute(session, "statusMessage", ret + " - Unable to update Data Element successfully.");
             DataManager.setAttribute(session, "sCDEAction", "nothing");
             ForwardJSP(req, res, "/EditDEPage.jsp"); // send it back to edit page
         }
     }
 
     /**
      * creates new record in the database and display the result. Called from 'doInsertDE' method when the aciton is
      * create new DEC from Menu. Retrieves the session bean m_DE. calls 'insAC.setDE' to insert in the database. calls
      * 'serAC.refreshData' to get the refreshed search result for template/version forwards the page back to create DEC
      * page if new DE or back to search page if template or version after successful insert.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'createDEPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doInsertDEfromMenuAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         InsACService insAC = new InsACService(req, res, this);
         GetACSearch serAC = new GetACSearch(req, res, this);
         String ret = "";
         boolean isUpdateSuccess = true;
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
        // String sOriginAction = (String) session.getAttribute("originAction");
         String sDDEAction = (String) session.getAttribute("DDEAction");
         // insert the new DE for DDE
         if (sDDEAction.equals("CreateNewDEFComp"))
         {
             doInsertDEComp(req, res);
             return;
         }
         DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
         DE_Bean oldDEBean = (DE_Bean) session.getAttribute("oldDEBean");
         // DE version
         if (sMenuAction.equals("NewDEVersion"))
         {
             // udpate the status message with DE name and ID only for version and updates
             storeStatusMsg("Data Element Name : " + DEBean.getDE_LONG_NAME());
             storeStatusMsg("Public ID : " + DEBean.getDE_MIN_CDE_ID());
             // call stored procedure to version and update attributes
             ret = insAC.setAC_VERSION(DEBean, null, null, "DataElement");
             if (ret == null || ret.equals(""))
             {
                 // update other attributes
                 ret = insAC.setDE("UPD", DEBean, "Version", oldDEBean);
                 if (ret != null && !ret.equals(""))
                 {
                     // DataManager.setAttribute(session, "statusMessage", ret + " - Created new version but unable to update
                     // attributes successfully.");
                     // add newly created row to searchresults and send it to edit page for update
                     isUpdateSuccess = false;
                     String oldID = oldDEBean.getDE_DE_IDSEQ();
                     DEBean = DEBean.cloneDE_Bean(oldDEBean, "Versioned");
                     serAC.refreshData(req, res, DEBean, null, null, null, "Version", oldID);
                 }
             }
             else
                 storeStatusMsg("\\t " + ret + " - Unable to create new version successfully.");
         }
         else
         { // insert a new one
             ret = insAC.setDE("INS", DEBean, "New", oldDEBean);
         }
         if ((ret == null) || ret.equals(""))
         {
             DataManager.setAttribute(session, "sCDEAction", "nothing");
             DataManager.setAttribute(session, "DECLongID", "nothing");
             DataManager.setAttribute(session, "VDLongID", "nothing");
             // forwards to search page with the refreshed list after success if template or version
             if ((sMenuAction.equals("NewDETemplate")) || (sMenuAction.equals("NewDEVersion")))
             {
                 ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), "INS"); // set DEComp rules and relationships
                 DataManager.setAttribute(session, "searchAC", "DataElement");
                 String oldID = oldDEBean.getDE_DE_IDSEQ();
                 if (sMenuAction.equals("NewDETemplate"))
                     serAC.refreshData(req, res, DEBean, null, null, null, "Template", oldID);
                 else if (sMenuAction.equals("NewDEVersion"))
                     serAC.refreshData(req, res, DEBean, null, null, null, "Version", oldID);
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             // forward to search page with the refreshed question list
             else if (sMenuAction.equals("Questions"))
             {
                 ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), "INS"); // set DEComp rules and relationships
                 doInitDDEInfo(req, res);
                 DataManager.setAttribute(session, "searchAC", "Questions");
                 Quest_Bean questBean = (Quest_Bean) session.getAttribute("m_Quest");
                 if (questBean != null)
                 {
                     questBean.setQUEST_DEFINITION(DEBean.getDE_PREFERRED_DEFINITION());
                     questBean.setSUBMITTED_LONG_NAME(DEBean.getDOC_TEXT_PREFERRED_QUESTION());
                     questBean.setDE_IDSEQ(DEBean.getDE_DE_IDSEQ());
                     questBean.setDE_LONG_NAME(DEBean.getDE_LONG_NAME());
                     questBean.setCDE_ID(DEBean.getDE_MIN_CDE_ID());
                     questBean.setVD_IDSEQ(DEBean.getDE_VD_IDSEQ());
                     questBean.setVD_LONG_NAME(DEBean.getDE_VD_NAME());
                     questBean.setSTATUS_INDICATOR("Edit");
                     serAC.refreshData(req, res, null, null, null, questBean, "Edit", "");
                     // reset the appened attributes to remove all the checking of the row
                     Vector vCheck = new Vector();
                     DataManager.setAttribute(session, "CheckList", vCheck);
                     DataManager.setAttribute(session, "AppendAction", "Not Appended");
                     // call the method to update Quest contents table
                     ret = insAC.setQuestContent(questBean, "", "");
                     // //// to get new DE's CDE_ID, reload all questions
                     String userName = (String) session.getAttribute("Username");
                     // call to search questions
                     Vector vResult = new Vector();
                     serAC.doQuestionSearch(userName, vResult);
                     DataManager.setAttribute(session, "vACSearch", vResult);
                     DataManager.setAttribute(session, "vSelRows", vResult);
                     vResult = new Vector<String>();
                     serAC.getQuestionResult(req, res, vResult);
                     DataManager.setAttribute(session, "results", vResult);
                     // ///////////////////////////
                     if (ret != null && !ret.equals(""))
                         storeStatusMsg("\\t " + ret + " : Unable to update CRF Questions.");
                     else
                         storeStatusMsg("\\t Successfully updated CRF Questions.");
                     DataManager.setAttribute(session, "m_Quest", questBean);
                 }
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             // forwards back to create page with empty data if create new
             else
             {
                 ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), ""); // after Primery DE created, set DEComp rules and
                                                                     // relationship
                 doInitDDEInfo(req, res);
                 DEBean = new DE_Bean();
                 DEBean.setDE_ASL_NAME("DRAFT NEW");
                 DEBean.setAC_PREF_NAME_TYPE("SYS");
                 DataManager.setAttribute(session, "m_DE", DEBean);
                 ForwardJSP(req, res, "/CreateDEPage.jsp");
             }
         }
         // sends back to create page with the error message.
         else
         {
             DataManager.setAttribute(session, "sCDEAction", "validate");
             // forward to create or edit pages
             if (isUpdateSuccess == false)
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             else
                 ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
     } // end of doInsertDEfromMenuAction
 
     /**
      * creates new DE Component and back to CreateDE/EditDE. Called from 'doInsertDEfromMenuAction' method when
      * 'originAction' is 'CreateNewDEFComp'. Retrieves the session bean m_DE. calls 'insAC.setDE' to insert in the
      * database.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'createDEPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     public void doInsertDEComp(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         InsACService insAC = new InsACService(req, res, this);
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         String ret = "";
         DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
         DE_Bean oldDEBean = (DE_Bean) session.getAttribute("oldDEBean");
         DE_Bean pDEBean = (DE_Bean) session.getAttribute("p_DEBean");
         DE_Bean pOldBean = (DE_Bean) session.getAttribute("p_oldBean");
         // insert a new one
         ret = insAC.setDE("INS", DEBean, "New", oldDEBean);
         if ((ret == null) || ret.equals("")) // set session vectors
         {
             // One DEComp for DDE is created, add it to vDEComp and back to CreateDE page or EditDE page
             // get exist vDEComp vectors from session
             Vector<String> vDEComp = new Vector<String>();
             Vector<String> vDECompID = new Vector<String>();
             Vector<String> vDECompOrder = new Vector<String>();
             Vector<String> vDECompRelID = new Vector<String>();
             vDEComp = (Vector) session.getAttribute("vDEComp");
             vDECompID = (Vector) session.getAttribute("vDECompID");
             vDECompOrder = (Vector) session.getAttribute("vDECompOrder");
             vDECompRelID = (Vector) session.getAttribute("vDECompRelID");
             int iCount = vDEComp.size() + 1;
             String sCount = Integer.toString(iCount);
             // add new one to v
             String sName = DEBean.getDE_LONG_NAME();
             if (sName == null || sName.equals(""))
                 sName = DEBean.getDE_PREFERRED_NAME();
             vDEComp.addElement(sName);
             vDECompID.addElement(DEBean.getDE_DE_IDSEQ());
             vDECompOrder.addElement(sCount);
             vDECompRelID.addElement("newDEComp");
             // set v back
             DataManager.setAttribute(session, "vDEComp", vDEComp);
             DataManager.setAttribute(session, "vDECompID", vDECompID);
             DataManager.setAttribute(session, "vDECompOrder", vDECompOrder);
             DataManager.setAttribute(session, "vDECompRelID", vDECompRelID);
             // change flag
             DataManager.setAttribute(session, "originAction", "DECompCreated");
             DataManager.setAttribute(session, "DDEAction", "nothing"); // reset from "CreateNewDEFComp"
             DataManager.setAttribute(session, "m_DE", pDEBean); // back old DE (replace DDE) to session for edit
             DataManager.setAttribute(session, "oldDEBean", pOldBean); // store old de bean back
             DataManager.setAttribute(session, "sCDEAction", "nothing");
             DataManager.setAttribute(session, "DECLongID", "nothing");
             DataManager.setAttribute(session, "VDLongID", "nothing");
         }
         else
         {
             DataManager.setAttribute(session, "sCDEAction", "validate");
         }
         // sends back to create page with the error message.
         if (sMenuAction.equalsIgnoreCase("EditDE"))
             ForwardJSP(req, res, "/EditDEPage.jsp");
         else
             ForwardJSP(req, res, "/CreateDEPage.jsp");
     } // end of doInsertDEComp
 
     /**
      * The doInsertDEC method to insert or update record in the database. Called from 'service' method where reqType is
      * 'validateDECFromForm'. Retrieves the session bean m_DEC. if the action is reEditDEC forwards the page back to
      * Edit or create pages.
      *
      * Otherwise, calls 'doUpdateDECAction' for editing the vd. calls 'doInsertDECfromDEAction' for creating the vd from
      * DE page. calls 'doInsertDECfromMenuAction' for creating the vd from menu .
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doInsertDEC(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // make sure that status message is empty
         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
         Vector vStat = new Vector();
         DataManager.setAttribute(session, "vStatMsg", vStat);
         String sOriginAction = (String) session.getAttribute("originAction");
         String sDECAction = (String) session.getAttribute("DECAction");
         if (sDECAction == null)
             sDECAction = "";
         String sDECEditAction = (String) session.getAttribute("DECEditAction");
         if (sDECEditAction == null)
             sDECEditAction = "";
         String sAction = (String) req.getParameter("ValidateDECPageAction");
         if (sAction == null)
             sAction = "submitting"; // for direct submit without validating
         if (sAction != null)
         {// going back to create or edit pages from validation page
             if (sAction.equals("reEditDEC"))
             {
                 if (sDECAction.equals("EditDEC") || sDECAction.equals("BlockEdit"))
                     ForwardJSP(req, res, "/EditDECPage.jsp");
                 else
                     ForwardJSP(req, res, "/CreateDECPage.jsp");
             }
             else
             {
                 // update the database for edit action
                 if (sDECAction.equals("EditDEC") && !sOriginAction.equals("BlockEditDEC"))
                     doUpdateDECAction(req, res);
                 else if (sDECEditAction.equals("DECBlockEdit"))
                     doUpdateDECActionBE(req, res);
                 // if create new dec from create DE page.
                 else if (sOriginAction.equals("CreateNewDECfromCreateDE")
                                 || sOriginAction.equals("CreateNewDECfromEditDE"))
                     doInsertDECfromDEAction(req, res, sOriginAction);
                 // FROM MENU, TEMPLATE, VERSION
                 else
                     doInsertDECfromMenuAction(req, res);
             }
         }
     } // end of doInsertDEC
 
     /**
      * update record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
      * editing. Retrieves the session bean m_DEC. calls 'insAC.setDEC' to update the database. updates the DEbean and
      * sends back to EditDE page if origin is form DEpage otherwise calls 'serAC.refreshData' to get the refreshed
      * search result forwards the page back to search page with refreshed list after updating. If ret is not null stores
      * the statusMessage as error message in session and forwards the page back to 'EditDECPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doUpdateDECAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
         DEC_Bean oldDECBean = (DEC_Bean) session.getAttribute("oldDECBean");
         //String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         InsACService insAC = new InsACService(req, res, this);
         // doInsertDECBlocks(req, res, null); //insert any building blocks from Thesaurus first
         // udpate the status message with DEC name and ID
         storeStatusMsg("Data Element Concept Name : " + DECBean.getDEC_LONG_NAME());
         storeStatusMsg("Public ID : " + DECBean.getDEC_DEC_ID());
         // call stored procedure to update attributes
         String ret = insAC.setDEC("UPD", DECBean, "Edit", oldDECBean);
         // after succcessful update
         if ((ret == null) || ret.equals(""))
         {
             String sOriginAction = (String) session.getAttribute("originAction");
             // forward page back to EditDE
             if (sOriginAction.equals("editDECfromDE"))
             {
                 DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
                 DEBean.setDE_DEC_IDSEQ(DECBean.getDEC_DEC_IDSEQ());
                 DEBean.setDE_DEC_NAME(DECBean.getDEC_LONG_NAME());
                 // reset the attributes
                 DataManager.setAttribute(session, "originAction", "");
                 // add DEC Bean into DE BEan
                 DEBean.setDE_DEC_Bean(DECBean);
                 // DEBean = this.doGetDENames(req, res, "noChange", "editDEC", DEBean);
                 DEBean = this.doGetDENames(req, res, "new", "editDEC", DEBean);
                 DataManager.setAttribute(session, "m_DE", DEBean);
                 ForwardJSP(req, res, "/EditDEPage.jsp");
             }
             // go to search page with refreshed list
             else
             {
                 DECBean.setDEC_ALIAS_NAME(DECBean.getDEC_PREFERRED_NAME());
                 DECBean.setDEC_TYPE_NAME("PRIMARY");
                 DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "editDEC");
                 GetACSearch serAC = new GetACSearch(req, res, this);
                 String oldID = DECBean.getDEC_DEC_IDSEQ();
                 serAC.refreshData(req, res, null, DECBean, null, null, "Edit", oldID);
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
         }
         // go back to edit page if not successful in update
         else
             ForwardJSP(req, res, "/EditDECPage.jsp"); // back to DEC Page
     }
 
     /**
      * creates new record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
      * create new DEC from DEPage. Retrieves the session bean m_DEC. calls 'insAC.setDEC' to update the database.
      * forwards the page back to create DE page after successful insert.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'createDECPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sOrigin
      *            String value to check where this action originated.
      *
      * @throws Exception
      */
     public void doInsertDECfromDEAction(HttpServletRequest req, HttpServletResponse res, String sOrigin)
                     throws Exception
     {
         HttpSession session = req.getSession();
         InsACService insAC = new InsACService(req, res, this);
         DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
         DEC_Bean oldDECBean = (DEC_Bean) session.getAttribute("oldDECBean");
        // String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // doInsertDECBlocks(req, res, null); //insert any building blocks from Thesaurus first
         String ret = insAC.setDEC("INS", DECBean, "New", oldDECBean);
         // add new dec attributes to de bean and forward to create de page if success.
         if ((ret == null) || ret.equals(""))
         {
             DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
             DEBean.setDE_DEC_NAME(DECBean.getDEC_LONG_NAME());
             DEBean.setDE_DEC_IDSEQ(DECBean.getDEC_DEC_IDSEQ());
             // add DEC Bean into DE BEan
             DEBean.setDE_DEC_Bean(DECBean);
             DEBean = this.doGetDENames(req, res, "new", "newDEC", DEBean);
             DataManager.setAttribute(session, "m_DE", DEBean);
            // String sContext = (String) session.getAttribute("sDefaultContext");
            // boolean bNewContext = true;
             DataManager.setAttribute(session, "originAction", ""); // reset this session attribute
             if (sOrigin != null && sOrigin.equals("CreateNewDECfromEditDE"))
                 ForwardJSP(req, res, "/EditDEPage.jsp");
             else
                 ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         // go back to create dec page if error
         else
         {
             DataManager.setAttribute(session, "DECPageAction", "validate");
             ForwardJSP(req, res, "/CreateDECPage.jsp"); // send it back to dec page
         }
     }
 
     /**
      * to created object class, property and qualifier value from EVS into cadsr. Retrieves the session bean m_DEC.
      * calls 'insAC.setDECQualifier' to insert the database.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param DECBeanSR
      *            dec attribute bean.
      *
      * @return DEC_Bean return the bean with the changed attributes
      * @throws Exception
      */
     public DEC_Bean doInsertDECBlocks(HttpServletRequest req, HttpServletResponse res, DEC_Bean DECBeanSR)
                     throws Exception
     {
         // logger.debug("doInsertDECBlocks");
         HttpSession session = req.getSession();
         InsACService insAC = new InsACService(req, res, this);
         String sNewOC = (String) session.getAttribute("newObjectClass");
         String sNewProp = (String) session.getAttribute("newProperty");
         if (sNewOC == null)
             sNewOC = "";
         if (sNewProp == null)
             sNewProp = "";
         if (DECBeanSR == null)
             DECBeanSR = (DEC_Bean) session.getAttribute("m_DEC");
         String sRemoveOCBlock = (String) session.getAttribute("RemoveOCBlock");
         String sRemovePropBlock = (String) session.getAttribute("RemovePropBlock");
         if (sRemoveOCBlock == null)
             sRemoveOCBlock = "";
         if (sRemovePropBlock == null)
             sRemovePropBlock = "";
         /*
          * if (sNewOC.equals("true")) DECBeanSR = insAC.setObjectClassDEC("INS", DECBeanSR, req); else
          * if(sRemoveOCBlock.equals("true"))
          */
         String sOC = DECBeanSR.getDEC_OCL_NAME();
         if (sOC != null && !sOC.equals(""))
             DECBeanSR = insAC.setObjectClassDEC("INS", DECBeanSR, req);
         /*
          * if (sNewProp.equals("true")) DECBeanSR = insAC.setPropertyDEC("INS", DECBeanSR, req); else
          * if(sRemovePropBlock.equals("true"))
          */
         String sProp = DECBeanSR.getDEC_PROPL_NAME();
         if (sProp != null && !sProp.equals(""))
             DECBeanSR = insAC.setPropertyDEC("INS", DECBeanSR, req);
         return DECBeanSR;
     }
 
     /**
      * creates new record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
      * create new DEC from Menu. Retrieves the session bean m_DEC. calls 'insAC.setVD' to update the database. calls
      * 'serAC.refreshData' to get the refreshed search result for template/version forwards the page back to create DEC
      * page if new DEC or back to search page if template or version after successful insert.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'createDECPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doInsertDECfromMenuAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         InsACService insAC = new InsACService(req, res, this);
         GetACSearch serAC = new GetACSearch(req, res, this);
         String ret = "";
       //  String ret2 = "";
         boolean isUpdateSuccess = true;
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
         DEC_Bean oldDECBean = (DEC_Bean) session.getAttribute("oldDECBean");
         // doInsertDECBlocks(req, res, null); //insert any building blocks from Thesaurus first
         if (sMenuAction.equals("NewDECVersion"))
         {
             // udpate the status message with DEC name and ID
             storeStatusMsg("Data Element Concept Name : " + DECBean.getDEC_LONG_NAME());
             storeStatusMsg("Public ID : " + DECBean.getDEC_DEC_ID());
             // creates new version first and updates all other attributes
             ret = insAC.setAC_VERSION(null, DECBean, null, "DataElementConcept");
             if (ret == null || ret.equals(""))
             {
                 // update other attributes
                 ret = insAC.setDEC("UPD", DECBean, "Version", oldDECBean);
                 // resetEVSBeans(req, res);
                 if (ret != null && !ret.equals(""))
                 {
                     // DataManager.setAttribute(session, "statusMessage", ret + " - Created new version but unable to update
                     // attributes successfully.");
                     // add newly created row to searchresults and send it to edit page for update
                     isUpdateSuccess = false;
                     // put back old attributes except version, idseq and workflow status
                     String oldID = oldDECBean.getDEC_DEC_IDSEQ();
                     String newID = DECBean.getDEC_DEC_IDSEQ();
                     String newVersion = DECBean.getDEC_VERSION();
                     DECBean = DECBean.cloneDEC_Bean(oldDECBean);
                     DECBean.setDEC_DEC_IDSEQ(newID);
                     DECBean.setDEC_VERSION(newVersion);
                     DECBean.setDEC_ASL_NAME("DRAFT MOD");
                     // add newly created dec into the resultset.
                     serAC.refreshData(req, res, null, DECBean, null, null, "Version", oldID);
                 }
             }
             else
                 storeStatusMsg("\\t " + ret + " - Unable to create new version successfully.");
         }
         else
         {
             ret = insAC.setDEC("INS", DECBean, "New", oldDECBean);
         }
         if ((ret == null) || ret.equals(""))
         {
             // DataManager.setAttribute(session, "statusMessage", "New Data Element Concept is created successfully.");
             DataManager.setAttribute(session, "DECPageAction", "nothing");
            // String sContext = (String) session.getAttribute("sDefaultContext");
            // boolean bNewContext = true;
             DataManager.setAttribute(session, "originAction", ""); // reset this session attribute
             // forwards to search page with the refreshed list after success if template or version
             if ((sMenuAction.equals("NewDECTemplate")) || (sMenuAction.equals("NewDECVersion")))
             {
                 DataManager.setAttribute(session, "searchAC", "DataElementConcept");
                 DECBean.setDEC_ALIAS_NAME(DECBean.getDEC_PREFERRED_NAME());
                 DECBean.setDEC_TYPE_NAME("PRIMARY");
                 String oldID = oldDECBean.getDEC_DEC_IDSEQ();
                 if (sMenuAction.equals("NewDECTemplate"))
                     serAC.refreshData(req, res, null, DECBean, null, null, "Template", oldID);
                 else if (sMenuAction.equals("NewDECVersion"))
                     serAC.refreshData(req, res, null, DECBean, null, null, "Version", oldID);
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             // go back to create dec page to create another one
             else
             {
                 DECBean = new DEC_Bean();
                 DECBean.setDEC_ASL_NAME("DRAFT NEW");
                 DECBean.setAC_PREF_NAME_TYPE("SYS");
                 DataManager.setAttribute(session, "m_DEC", DECBean);
                 EVS_Bean m_OC = new EVS_Bean();
                 DataManager.setAttribute(session, "m_OC", m_OC);
                 EVS_Bean m_PC = new EVS_Bean();
                 DataManager.setAttribute(session, "m_PC", m_PC);
                 EVS_Bean m_OCQ = new EVS_Bean();
                 DataManager.setAttribute(session, "m_OCQ", m_OCQ);
                 EVS_Bean m_PCQ = new EVS_Bean();
                 DataManager.setAttribute(session, "m_PCQ", m_PCQ);
                 DataManager.setAttribute(session, "selObjQRow", "");
                 DataManager.setAttribute(session, "selObjRow", "");
                 DataManager.setAttribute(session, "selPropQRow", "");
                 DataManager.setAttribute(session, "selPropRow", "");
                 ForwardJSP(req, res, "/CreateDECPage.jsp");
             }
         }
         // go back to create dec page if error occurs
         else
         {
             DataManager.setAttribute(session, "DECPageAction", "validate");
             // forward to create or edit pages
             if (isUpdateSuccess == false)
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             else
                 ForwardJSP(req, res, "/CreateDECPage.jsp");
         }
     }
 
     /**
      * The doInsertVD method to insert or update record in the database. Called from 'service' method where reqType is
      * 'validateVDFromForm'. Retrieves the session bean m_VD. if the action is reEditVD forwards the page back to Edit
      * or create pages.
      *
      * Otherwise, calls 'doUpdateVDAction' for editing the vd. calls 'doInsertVDfromDEAction' for creating the vd from
      * DE page. calls 'doInsertVDfromMenuAction' for creating the vd from menu .
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doInsertVD(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // make sure that status message is empty
         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
         Vector vStat = new Vector();
         DataManager.setAttribute(session, "vStatMsg", vStat);
         String sVDAction = (String) session.getAttribute("VDAction");
         if (sVDAction == null)
             sVDAction = "";
         String sVDEditAction = (String) session.getAttribute("VDEditAction");
         if (sVDEditAction == null)
             sVDEditAction = "";
         String sAction = (String) req.getParameter("ValidateVDPageAction");
        // String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
        // String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sAction == null)
             sAction = "submitting"; // for direct submit without validating
        // String spageAction = (String) req.getParameter("pageAction");
         if (sAction != null)
         { // goes back to create/edit pages from validation page
             if (sAction.equals("reEditVD"))
             {
                 String vdfocus = (String) session.getAttribute("TabFocus");
                 if (vdfocus != null && vdfocus.equals("PV"))
                     ForwardJSP(req, res, "/PermissibleValue.jsp");
                 else
                 {
                     if (sVDAction.equals("EditVD") || sVDAction.equals("BlockEdit"))
                         ForwardJSP(req, res, "/EditVDPage.jsp");
                     else
                         ForwardJSP(req, res, "/CreateVDPage.jsp");
                 }
             }
             else
             {
                 // edit the existing vd
                 if (sVDAction.equals("NewVD") && sOriginAction.equals("NewVDFromMenu"))
                     doInsertVDfromMenuAction(req, res);
                 else if (sVDAction.equals("EditVD") && !sOriginAction.equals("BlockEditVD"))
                     doUpdateVDAction(req, res);
                 else if (sVDEditAction.equals("VDBlockEdit"))
                     doUpdateVDActionBE(req, res);
                 // if create new vd from create/edit DE page.
                 else if (sOriginAction.equals("CreateNewVDfromCreateDE")
                                 || sOriginAction.equals("CreateNewVDfromEditDE"))
                     doInsertVDfromDEAction(req, res, sOriginAction);
                 // from the menu AND template/ version
                 else
                 {
                     doInsertVDfromMenuAction(req, res);
                 }
             }
         }
     } // end of doInsertVD
 
     /**
      * update record in the database and display the result. Called from 'doInsertVD' method when the aciton is editing.
      * Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. updates the DEbean and sends back to
      * EditDE page if origin is form DEpage otherwise calls 'serAC.refreshData' to get the refreshed search result
      * forwards the page back to search page with refreshed list after updating.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'EditVDPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doUpdateVDAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
         VD_Bean oldVDBean = (VD_Bean) session.getAttribute("oldVDBean");
       //  String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         InsACService insAC = new InsACService(req, res, this);
         doInsertVDBlocks(req, res, null);
         // udpate the status message with DE name and ID
         storeStatusMsg("Value Domain Name : " + VDBean.getVD_LONG_NAME());
         storeStatusMsg("Public ID : " + VDBean.getVD_VD_ID());
         // call stored procedure to update attributes
         String ret = insAC.setVD("UPD", VDBean, "Edit", oldVDBean);
         // forward to search page with refreshed list after successful update
         if ((ret == null) || ret.equals(""))
         {
             this.clearCreateSessionAttributes(req, res); // clear some session attributes
             String sOriginAction = (String) session.getAttribute("originAction");
             GetACSearch serAC = new GetACSearch(req, res, this);
             // forward page back to EditDE
             if (sOriginAction.equals("editVDfromDE") || sOriginAction.equals("EditDE"))
             {
                 DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
                 if (DEBean != null)
                 {
                     DEBean.setDE_VD_IDSEQ(VDBean.getVD_VD_IDSEQ());
                     DEBean.setDE_VD_PREFERRED_NAME(VDBean.getVD_PREFERRED_NAME());
                     DEBean.setDE_VD_NAME(VDBean.getVD_LONG_NAME());
                     // reset the attributes
                     DataManager.setAttribute(session, "originAction", "");
                     // add DEC Bean into DE BEan
                     DEBean.setDE_VD_Bean(VDBean);
                     DataManager.setAttribute(session, "m_DE", DEBean);
                     // DEBean = this.doGetDENames(req, res, "noChange", "editVD", DEBean);
                     DEBean = this.doGetDENames(req, res, "new", "editVD", DEBean);
                 }
                 ForwardJSP(req, res, "/EditDEPage.jsp");
             }
             // go to search page with refreshed list
             else
             {
                 VDBean.setVD_ALIAS_NAME(VDBean.getVD_PREFERRED_NAME());
                 // VDBean.setVD_TYPE_NAME("PRIMARY");
                 DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "editVD");
                 String oldID = VDBean.getVD_VD_IDSEQ();
                 serAC.refreshData(req, res, null, null, VDBean, null, "Edit", oldID);
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
         }
         // goes back to edit page if error occurs
         else
         {
             DataManager.setAttribute(session, "VDPageAction", "nothing");
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
     }
 
     /**
      * update record in the database and display the result. Called from 'doInsertVD' method when the aciton is editing.
      * Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. updates the DEbean and sends back to
      * EditDE page if origin is form DEpage otherwise calls 'serAC.refreshData' to get the refreshed search result
      * forwards the page back to search page with refreshed list after updating.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'EditVDPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doUpdateVDActionBE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD"); // validated edited m_VD
         boolean isRefreshed = false;
         String ret = ":";
         InsACService insAC = new InsACService(req, res, this);
         GetACSearch serAC = new GetACSearch(req, res, this);
         GetACService getAC = new GetACService(req, res, this);
        // Vector vStatMsg = new Vector();
         String sNewRep = (String) session.getAttribute("newRepTerm");
         if (sNewRep == null)
             sNewRep = "";
         System.out.println(" new rep " + sNewRep);
         Vector vBERows = (Vector) session.getAttribute("vBEResult");
         int vBESize = vBERows.size();
         Integer vBESize2 = new Integer(vBESize);
         req.setAttribute("vBESize", vBESize2);
         String sRep_IDSEQ = "";
         if (vBERows.size() > 0)
         {
             // Be sure the buffer is loaded when doing versioning.
             String newVersion = VDBean.getVD_VERSION();
             if (newVersion == null)
                 newVersion = "";
             boolean newVers = (newVersion.equals("Point") || newVersion.equals("Whole"));
             if (newVers)
             {
                 @SuppressWarnings("unchecked")
                 Vector<AC_Bean> tvec = vBERows;
                 AltNamesDefsSession.loadAsNew(this, session, tvec);
             }
             for (int i = 0; i < (vBERows.size()); i++)
             {
                 // String sVD_ID = ""; //out
                 VD_Bean VDBeanSR = new VD_Bean();
                 VDBeanSR = (VD_Bean) vBERows.elementAt(i);
                 VD_Bean oldVDBean = new VD_Bean();
                 oldVDBean = oldVDBean.cloneVD_Bean(VDBeanSR);
             //    String oldName = (String) VDBeanSR.getVD_PREFERRED_NAME();
                 // updates the data from the page into the sr bean
                 InsertEditsIntoVDBeanSR(VDBeanSR, VDBean, req);
                 // create newly selected rep term
                 if (i == 0 && sNewRep.equals("true"))
                 {
                     doInsertVDBlocks(req, res, VDBeanSR); // create it
                     sRep_IDSEQ = VDBeanSR.getVD_REP_IDSEQ(); // get rep idseq
                     if (sRep_IDSEQ == null)
                         sRep_IDSEQ = "";
                     VDBean.setVD_REP_IDSEQ(sRep_IDSEQ); // add page vd bean
                     String sRep_Condr = VDBeanSR.getVD_REP_CONDR_IDSEQ(); // get rep condr
                     if (sRep_Condr == null)
                         sRep_Condr = "";
                     VDBean.setVD_REP_CONDR_IDSEQ(sRep_Condr); // add to page vd bean
                     // VDBean.setVD_REP_QUAL("");
                 }
                 // DataManager.setAttribute(session, "m_VD", VDBeanSR);
                 String oldID = oldVDBean.getVD_VD_IDSEQ();
                 // udpate the status message with DE name and ID
                 storeStatusMsg("Value Domain Name : " + VDBeanSR.getVD_LONG_NAME());
                 storeStatusMsg("Public ID : " + VDBeanSR.getVD_VD_ID());
                 // insert the version
                 if (newVers) // block version
                 {
                     // creates new version first and updates all other attributes
                     String strValid = m_setAC.checkUniqueInContext("Version", "VD", null, null, VDBeanSR, getAC,
                                     "version");
                     if (strValid != null && !strValid.equals(""))
                         ret = "unique constraint";
                     else
                         ret = insAC.setAC_VERSION(null, null, VDBeanSR, "ValueDomain");
                     if (ret == null || ret.equals(""))
                     {
                         // PVServlet pvser = new PVServlet(req, res, this);
                         // pvser.searchVersionPV(VDBean, 0, "", "");
                         // get the right system name for new version
                         String prefName = VDBeanSR.getVD_PREFERRED_NAME();
                         String vdID = VDBeanSR.getVD_VD_ID();
                         String newVer = "v" + VDBeanSR.getVD_VERSION();
                         String oldVer = "v" + oldVDBean.getVD_VERSION();
                         // replace teh version number if system generated name
                         if (prefName.indexOf(vdID) > 0)
                         {
                             prefName = prefName.replaceFirst(oldVer, newVer);
                             VDBean.setVD_PREFERRED_NAME(prefName);
                         }
                         // keep the value and value count stored
                         String pvValue = VDBeanSR.getVD_Permissible_Value();
                         Integer pvCount = VDBeanSR.getVD_Permissible_Value_Count();
                         ret = insAC.setVD("UPD", VDBeanSR, "Version", oldVDBean);
                         if (ret == null || ret.equals(""))
                         {
                             VDBeanSR.setVD_Permissible_Value(pvValue);
                             VDBeanSR.setVD_Permissible_Value_Count(pvCount);
                             serAC.refreshData(req, res, null, null, VDBeanSR, null, "Version", oldID);
                             isRefreshed = true;
                             // reset the appened attributes to remove all the checking of the row
                             Vector vCheck = new Vector();
                             DataManager.setAttribute(session, "CheckList", vCheck);
                             DataManager.setAttribute(session, "AppendAction", "Not Appended");
                             // resetEVSBeans(req, res);
                         }
                     }
                     // alerady exists
                     else if (ret.indexOf("unique constraint") >= 0)
                         storeStatusMsg("\\t New version " + VDBeanSR.getVD_VERSION()
                                         + " already exists in the data base.\\n");
                     // some other problem
                     else
                         storeStatusMsg("\\t " + ret + " : Unable to create new version "
                                         + VDBeanSR.getVD_VERSION() + ".\\n");
                 }
                 else
                 // block edit
                 {
                     ret = insAC.setVD("UPD", VDBeanSR, "Edit", oldVDBean);
                     // forward to search page with refreshed list after successful update
                     if ((ret == null) || ret.equals(""))
                     {
                         serAC.refreshData(req, res, null, null, VDBeanSR, null, "Edit", oldID);
                         isRefreshed = true;
                     }
                 }
             }
             AltNamesDefsSession.blockSave(this, session);
         }
         // to get the final result vector if not refreshed at all
         if (!(isRefreshed))
         {
             Vector<String> vResult = new Vector<String>();
             serAC.getVDResult(req, res, vResult, "");
             DataManager.setAttribute(session, "results", vResult); // store the final result in the session
             DataManager.setAttribute(session, "VDPageAction", "nothing");
         }
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * called after setDEC or setVD to reset EVS session attributes
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void resetEVSBeans(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         EVS_Bean m_OC = new EVS_Bean();
         DataManager.setAttribute(session, "m_OC", m_OC);
         EVS_Bean m_PC = new EVS_Bean();
         DataManager.setAttribute(session, "m_PC", m_PC);
         EVS_Bean m_Rep = new EVS_Bean();
         DataManager.setAttribute(session, "m_Rep", m_Rep);
         EVS_Bean m_OCQ = new EVS_Bean();
         DataManager.setAttribute(session, "m_OCQ", m_OCQ);
         EVS_Bean m_PCQ = new EVS_Bean();
         DataManager.setAttribute(session, "m_PCQ", m_PCQ);
         EVS_Bean m_REPQ = new EVS_Bean();
         DataManager.setAttribute(session, "m_REPQ", m_REPQ);
         DataManager.setAttribute(session, "selPropRow", "");
         DataManager.setAttribute(session, "selPropQRow", "");
         DataManager.setAttribute(session, "selObjQRow", "");
         DataManager.setAttribute(session, "selObjRow", "");
         DataManager.setAttribute(session, "selRepQRow", "");
         DataManager.setAttribute(session, "selRepRow", "");
     }
 
     /**
      * updates bean the selected VD from the changed values of block edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param VDBeanSR
      *            selected vd bean from search result
      * @param vd
      *            VD_Bean of the changed values.
      *
      * @throws Exception
      */
     public void InsertEditsIntoVDBeanSR(VD_Bean VDBeanSR, VD_Bean vd, @SuppressWarnings("unused") HttpServletRequest req) throws Exception
     {
         // get all attributes of VDBean, if attribute != "" then set that attribute of VDBeanSR
         String sDefinition = vd.getVD_PREFERRED_DEFINITION();
         if (sDefinition == null)
             sDefinition = "";
         if (!sDefinition.equals(""))
             VDBeanSR.setVD_PREFERRED_DEFINITION(sDefinition);
         String sCD_ID = vd.getVD_CD_IDSEQ();
         if (sCD_ID == null)
             sCD_ID = "";
         if (!sCD_ID.equals("") && !sCD_ID.equals(null))
             VDBeanSR.setVD_CD_IDSEQ(sCD_ID);
         String sCDName = vd.getVD_CD_NAME();
         if (sCDName == null)
             sCDName = "";
         if (!sCDName.equals("") && !sCDName.equals(null))
             VDBeanSR.setVD_CD_NAME(sCDName);
         String sAslName = vd.getVD_ASL_NAME();
         if (sAslName == null)
             sAslName = "";
         if (!sAslName.equals(""))
             VDBeanSR.setVD_ASL_NAME(sAslName);
         String sDtlName = vd.getVD_DATA_TYPE();
         if (sDtlName == null)
             sDtlName = "";
         if (!sDtlName.equals(""))
             VDBeanSR.setVD_DATA_TYPE(sDtlName);
         String sMaxLength = vd.getVD_MAX_LENGTH_NUM();
         if (sMaxLength == null)
             sMaxLength = "";
         if (!sMaxLength.equals(""))
             VDBeanSR.setVD_MAX_LENGTH_NUM(sMaxLength);
         String sFormlName = vd.getVD_FORML_NAME(); // UOM Format
         if (sFormlName == null)
             sFormlName = "";
         if (!sFormlName.equals(""))
             VDBeanSR.setVD_FORML_NAME(sFormlName);
         String sUomlName = vd.getVD_UOML_NAME();
         if (sUomlName == null)
             sUomlName = "";
         if (!sUomlName.equals(""))
             VDBeanSR.setVD_UOML_NAME(sUomlName);
         String sLowValue = vd.getVD_LOW_VALUE_NUM();
         if (sLowValue == null)
             sLowValue = "";
         if (!sLowValue.equals(""))
             VDBeanSR.setVD_LOW_VALUE_NUM(sLowValue);
         String sHighValue = vd.getVD_HIGH_VALUE_NUM();
         if (sHighValue == null)
             sHighValue = "";
         if (!sHighValue.equals(""))
             VDBeanSR.setVD_HIGH_VALUE_NUM(sHighValue);
         String sMinLength = vd.getVD_MIN_LENGTH_NUM();
         if (sMinLength == null)
             sMinLength = "";
         if (!sMinLength.equals(""))
             VDBeanSR.setVD_MIN_LENGTH_NUM(sMinLength);
         String sDecimalPlace = vd.getVD_DECIMAL_PLACE();
         if (sDecimalPlace == null)
             sDecimalPlace = "";
         if (!sDecimalPlace.equals(""))
             VDBeanSR.setVD_DECIMAL_PLACE(sDecimalPlace);
         String sBeginDate = vd.getVD_BEGIN_DATE();
         if (sBeginDate == null)
             sBeginDate = "";
         if (!sBeginDate.equals(""))
             VDBeanSR.setVD_BEGIN_DATE(sBeginDate);
         String sEndDate = vd.getVD_END_DATE();
         if (sEndDate == null)
             sEndDate = "";
         if (!sEndDate.equals(""))
             VDBeanSR.setVD_END_DATE(sEndDate);
         String sSource = vd.getVD_SOURCE();
         if (sSource == null)
             sSource = "";
         if (!sSource.equals(""))
             VDBeanSR.setVD_SOURCE(sSource);
         String changeNote = vd.getVD_CHANGE_NOTE();
         if (changeNote == null)
             changeNote = "";
         if (!changeNote.equals(""))
             VDBeanSR.setVD_CHANGE_NOTE(changeNote);
         // get cs-csi from the page into the DECBean for block edit
         Vector vAC_CS = vd.getAC_AC_CSI_VECTOR();
         if (vAC_CS != null)
             VDBeanSR.setAC_AC_CSI_VECTOR(vAC_CS);
         String sRepTerm = vd.getVD_REP_TERM();
         if (sRepTerm == null)
             sRepTerm = "";
         if (!sRepTerm.equals(""))
             VDBeanSR.setVD_REP_TERM(sRepTerm);
         String sRepCondr = vd.getVD_REP_CONDR_IDSEQ();
         if (sRepCondr == null)
             sRepCondr = "";
         if (!sRepCondr.equals(""))
             VDBeanSR.setVD_REP_CONDR_IDSEQ(sRepCondr);
         String sREP_IDSEQ = vd.getVD_REP_IDSEQ();
         if (sREP_IDSEQ != null && !sREP_IDSEQ.equals(""))
             VDBeanSR.setVD_REP_IDSEQ(sREP_IDSEQ);
         /*
          * String sRepQual = vd.getVD_REP_QUAL(); if (sRepQual == null) sRepQual = ""; if (!sRepQual.equals(""))
          * VDBeanSR.setVD_REP_QUAL(sRepQual);
          */
         String version = vd.getVD_VERSION();
         String lastVersion = (String) VDBeanSR.getVD_VERSION();
         int index = -1;
         String pointStr = ".";
         String strWhBegNumber = "";
         int iWhBegNumber = 0;
         index = lastVersion.indexOf(pointStr);
         String strPtBegNumber = lastVersion.substring(0, index);
         String afterDecimalNumber = lastVersion.substring((index + 1), (index + 2));
         if (index == 1)
             strWhBegNumber = "";
         else if (index == 2)
         {
             strWhBegNumber = lastVersion.substring(0, index - 1);
             Integer WhBegNumber = new Integer(strWhBegNumber);
             iWhBegNumber = WhBegNumber.intValue();
         }
         String strWhEndNumber = ".0";
         String beforeDecimalNumber = lastVersion.substring((index - 1), (index));
         String sNewVersion = "";
         Integer IadNumber = new Integer(0);
         Integer IbdNumber = new Integer(0);
         String strIncADNumber = "";
         String strIncBDNumber = "";
         if (version == null)
             version = "";
         else if (version.equals("Point"))
         {
             // Point new version
             int incrementADNumber = 0;
             int incrementBDNumber = 0;
             Integer adNumber = new Integer(afterDecimalNumber);
             Integer bdNumber = new Integer(strPtBegNumber);
             int iADNumber = adNumber.intValue(); // after decimal
             int iBDNumber = bdNumber.intValue(); // before decimal
             if (iADNumber != 9)
             {
                 incrementADNumber = iADNumber + 1;
                 IadNumber = new Integer(incrementADNumber);
                 strIncADNumber = IadNumber.toString();
                 sNewVersion = strPtBegNumber + "." + strIncADNumber; // + strPtEndNumber;
             }
             else
             // adNumber == 9
             {
                 incrementADNumber = 0;
                 incrementBDNumber = iBDNumber + 1;
                 IbdNumber = new Integer(incrementBDNumber);
                 strIncBDNumber = IbdNumber.toString();
                 IadNumber = new Integer(incrementADNumber);
                 strIncADNumber = IadNumber.toString();
                 sNewVersion = strIncBDNumber + "." + strIncADNumber; // + strPtEndNumber;
             }
             VDBeanSR.setVD_VERSION(sNewVersion);
         }
         else if (version.equals("Whole"))
         {
             // Whole new version
             Integer bdNumber = new Integer(beforeDecimalNumber);
             int iBDNumber = bdNumber.intValue();
             int incrementBDNumber = iBDNumber + 1;
             if (iBDNumber != 9)
             {
                 IbdNumber = new Integer(incrementBDNumber);
                 strIncBDNumber = IbdNumber.toString();
                 sNewVersion = strWhBegNumber + strIncBDNumber + strWhEndNumber;
             }
             else
             // before decimal number == 9
             {
                 int incrementWhBegNumber = iWhBegNumber + 1;
                 Integer IWhBegNumber = new Integer(incrementWhBegNumber);
                 String strIncWhBegNumber = IWhBegNumber.toString();
                 IbdNumber = new Integer(0);
                 strIncBDNumber = IbdNumber.toString();
                 sNewVersion = strIncWhBegNumber + strIncBDNumber + strWhEndNumber;
             }
             VDBeanSR.setVD_VERSION(sNewVersion);
         }
     }
 
     /**
      * updates bean the selected DEC from the changed values of block edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param DECBeanSR
      *            selected DEC bean from search result
      * @param dec
      *            DEC_Bean of the changed values.
      *
      * @throws Exception
      */
     public void InsertEditsIntoDECBeanSR(DEC_Bean DECBeanSR, DEC_Bean dec, @SuppressWarnings("unused") HttpServletRequest req) throws Exception
     {
         // get all attributes of DECBean, if attribute != "" then set that attribute of DECBeanSR
         String sDefinition = dec.getDEC_PREFERRED_DEFINITION();
         if (sDefinition == null)
             sDefinition = "";
         if (!sDefinition.equals(""))
             DECBeanSR.setDEC_PREFERRED_DEFINITION(sDefinition);
         String sCD_ID = dec.getDEC_CD_IDSEQ();
         if (sCD_ID == null)
             sCD_ID = "";
         if (!sCD_ID.equals(""))
             DECBeanSR.setDEC_CD_IDSEQ(sCD_ID);
         String sCDName = dec.getDEC_CD_NAME();
         if (sCDName == null)
             sCDName = "";
         if (!sCDName.equals("") && !sCDName.equals(null))
             DECBeanSR.setDEC_CD_NAME(sCDName);
         String sBeginDate = dec.getDEC_BEGIN_DATE();
         if (sBeginDate == null)
             sBeginDate = "";
         if (!sBeginDate.equals(""))
             DECBeanSR.setDEC_BEGIN_DATE(sBeginDate);
         String sEndDate = dec.getDEC_END_DATE();
         if (sEndDate == null)
             sEndDate = "";
         if (!sEndDate.equals(""))
             DECBeanSR.setDEC_END_DATE(sEndDate);
         String sSource = dec.getDEC_SOURCE();
         if (sSource == null)
             sSource = "";
         if (!sSource.equals(""))
             DECBeanSR.setDEC_SOURCE(sSource);
         String changeNote = dec.getDEC_CHANGE_NOTE();
         if (changeNote == null)
             changeNote = "";
         if (!changeNote.equals(""))
             DECBeanSR.setDEC_CHANGE_NOTE(changeNote);
         // get cs-csi from the page into the DECBean for block edit
         Vector<AC_CSI_Bean> vAC_CS = dec.getAC_AC_CSI_VECTOR();
         if (vAC_CS != null)
             DECBeanSR.setAC_AC_CSI_VECTOR(vAC_CS);
         String sOCL = dec.getDEC_OCL_NAME();
         if (sOCL == null)
             sOCL = "";
         if (!sOCL.equals(""))
         {
             DECBeanSR.setDEC_OCL_NAME(sOCL);
             String sOCCondr = dec.getDEC_OC_CONDR_IDSEQ();
             if (sOCCondr == null)
                 sOCCondr = "";
             if (!sOCCondr.equals(""))
                 DECBeanSR.setDEC_OC_CONDR_IDSEQ(sOCCondr);
             String sOCL_IDSEQ = dec.getDEC_OCL_IDSEQ();
             if (sOCL_IDSEQ != null && !sOCL_IDSEQ.equals(""))
                 DECBeanSR.setDEC_OCL_IDSEQ(sOCL_IDSEQ);
         }
         String sPropL = dec.getDEC_PROPL_NAME();
         if (sPropL == null)
             sPropL = "";
         if (!sPropL.equals(""))
         {
             DECBeanSR.setDEC_PROPL_NAME(sPropL);
             String sPCCondr = dec.getDEC_PROP_CONDR_IDSEQ();
             if (sPCCondr == null)
                 sPCCondr = "";
             if (!sPCCondr.equals(""))
                 DECBeanSR.setDEC_PROP_CONDR_IDSEQ(sPCCondr);
             String sPROPL_IDSEQ = dec.getDEC_PROPL_IDSEQ();
             if (sPROPL_IDSEQ != null && !sPROPL_IDSEQ.equals(""))
                 DECBeanSR.setDEC_PROPL_IDSEQ(sPROPL_IDSEQ);
         }
         // update dec pref type and abbr name
         DECBeanSR.setAC_PREF_NAME_TYPE(dec.getAC_PREF_NAME_TYPE());
         String status = dec.getDEC_ASL_NAME();
         if (status == null)
             status = "";
         if (!status.equals(""))
             DECBeanSR.setDEC_ASL_NAME(status);
         String version = dec.getDEC_VERSION();
         String lastVersion = (String) DECBeanSR.getDEC_VERSION();
         int index = -1;
         String pointStr = ".";
         String strWhBegNumber = "";
         int iWhBegNumber = 0;
         index = lastVersion.indexOf(pointStr);
         String strPtBegNumber = lastVersion.substring(0, index);
         String afterDecimalNumber = lastVersion.substring((index + 1), (index + 2));
         if (index == 1)
             strWhBegNumber = "";
         else if (index == 2)
         {
             strWhBegNumber = lastVersion.substring(0, index - 1);
             Integer WhBegNumber = new Integer(strWhBegNumber);
             iWhBegNumber = WhBegNumber.intValue();
         }
         String strWhEndNumber = ".0";
         String beforeDecimalNumber = lastVersion.substring((index - 1), (index));
         String sNewVersion = "";
         Integer IadNumber = new Integer(0);
         Integer IbdNumber = new Integer(0);
         String strIncADNumber = "";
         String strIncBDNumber = "";
         if (version == null)
             version = "";
         else if (version.equals("Point"))
         {
             // Point new version
             int incrementADNumber = 0;
             int incrementBDNumber = 0;
             Integer adNumber = new Integer(afterDecimalNumber);
             Integer bdNumber = new Integer(strPtBegNumber);
             int iADNumber = adNumber.intValue(); // after decimal
             int iBDNumber = bdNumber.intValue(); // before decimal
             if (iADNumber != 9)
             {
                 incrementADNumber = iADNumber + 1;
                 IadNumber = new Integer(incrementADNumber);
                 strIncADNumber = IadNumber.toString();
                 sNewVersion = strPtBegNumber + "." + strIncADNumber; // + strPtEndNumber;
             }
             else
             // adNumber == 9
             {
                 incrementADNumber = 0;
                 incrementBDNumber = iBDNumber + 1;
                 IbdNumber = new Integer(incrementBDNumber);
                 strIncBDNumber = IbdNumber.toString();
                 IadNumber = new Integer(incrementADNumber);
                 strIncADNumber = IadNumber.toString();
                 sNewVersion = strIncBDNumber + "." + strIncADNumber; // + strPtEndNumber;
             }
             DECBeanSR.setDEC_VERSION(sNewVersion);
         }
         else if (version.equals("Whole"))
         {
             // Whole new version
             Integer bdNumber = new Integer(beforeDecimalNumber);
             int iBDNumber = bdNumber.intValue();
             int incrementBDNumber = iBDNumber + 1;
             if (iBDNumber != 9)
             {
                 IbdNumber = new Integer(incrementBDNumber);
                 strIncBDNumber = IbdNumber.toString();
                 sNewVersion = strWhBegNumber + strIncBDNumber + strWhEndNumber;
             }
             else
             // before decimal number == 9
             {
                 int incrementWhBegNumber = iWhBegNumber + 1;
                 Integer IWhBegNumber = new Integer(incrementWhBegNumber);
                 String strIncWhBegNumber = IWhBegNumber.toString();
                 IbdNumber = new Integer(0);
                 strIncBDNumber = IbdNumber.toString();
                 sNewVersion = strIncWhBegNumber + strIncBDNumber + strWhEndNumber;
             }
             DECBeanSR.setDEC_VERSION(sNewVersion);
         }
     }
 
     /**
      * updates bean the selected DE from the changed values of block edit.
      *
      * @param DEBeanSR
      *            selected DE bean from search result
      * @param de
      *            DE_Bean of the changed values.
      * @param req
      *            request object
      * @param res
      *            response object
      */
     public void InsertEditsIntoDEBeanSR(DE_Bean DEBeanSR, DE_Bean de, HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             // get all attributes of DEBean, if attribute != "" then set that attribute of DEBeanSR
             String sDefinition = de.getDE_PREFERRED_DEFINITION();
             if (sDefinition == null)
                 sDefinition = "";
             if (!sDefinition.equals(""))
                 DEBeanSR.setDE_PREFERRED_DEFINITION(sDefinition);
             // do dec/vd/registration status uniqueness check before making changes.
             DEC_Bean Pdec = de.getDE_DEC_Bean();
             if (Pdec == null)
                 Pdec = new DEC_Bean();
             VD_Bean Pvd = de.getDE_VD_Bean();
             if (Pvd == null)
                 Pvd = new VD_Bean();
             // get the dec vd ids frm teh page beans
             String sDEC_ID = Pdec.getDEC_DEC_IDSEQ(); // de.getDE_DEC_IDSEQ();
             if (sDEC_ID == null)
                 sDEC_ID = "";
             String sVD_ID = Pvd.getVD_VD_IDSEQ(); // de.getDE_VD_IDSEQ();
             if (sVD_ID == null)
                 sVD_ID = "";
             String RegStatus = de.getDE_REG_STATUS();
             if (RegStatus == null)
                 RegStatus = "";
             // store the old reg status; get the dec vd idseqs from the sr bean
             DEC_Bean SRdec = DEBeanSR.getDE_DEC_Bean();
             if (SRdec == null)
                 SRdec = new DEC_Bean();
             VD_Bean SRvd = DEBeanSR.getDE_VD_Bean();
             if (SRvd == null)
                 SRvd = new VD_Bean();
             String oldDEC = SRdec.getDEC_DEC_IDSEQ(); // DEBeanSR.getDE_DEC_IDSEQ();
             String oldVD = SRvd.getVD_VD_IDSEQ(); // DEBeanSR.getDE_VD_IDSEQ();
             String oldReg = DEBeanSR.getDE_REG_STATUS();
             if (oldReg == null)
                 oldReg = "";
             // update the bean with only the changed data
             if (!sDEC_ID.equals(""))
                 DEBeanSR.setDE_DEC_IDSEQ(sDEC_ID);
             if (!sVD_ID.equals(""))
                 DEBeanSR.setDE_VD_IDSEQ(sVD_ID);
             if (!RegStatus.equals(""))
                 DEBeanSR.setDE_REG_STATUS(RegStatus);
             // do the validation for dec-vd pair
             SetACService setAC = new SetACService(this);
             GetACService getAC = new GetACService(req, res, this);
             //InsACService insAC = new InsACService(req, res, this);
             String sValid = setAC.checkUniqueDECVDPair(DEBeanSR, getAC, "Edit", "Edit");
             // put back to old if not valid even if it is just warning
             boolean newDECVD = true;
             if (sValid != null && !sValid.equals(""))
             {
                 String changeAC = "\\t Please note that "; // message if old one has the problem
                 if (!sDEC_ID.equals(""))
                 {
                     DEBeanSR.setDE_DEC_IDSEQ(oldDEC);
                     changeAC = "\\t Unable to update the Data Element Concept because \\n\\t";
                     newDECVD = false;
                 }
                 else if (!sVD_ID.equals(""))
                 {
                     DEBeanSR.setDE_VD_IDSEQ(oldVD);
                     changeAC = "\\t Unable to update the Value Domain because \\n\\t";
                     newDECVD = false;
                 }
                 storeStatusMsg(changeAC
                                 + "the combination of DEC, VD and Context already exists in other Data Elements.\\n");
             }
             // do the validation for reg status
             String sReg = RegStatus;
             if (sReg.equals(""))
                 sReg = oldReg;
             if (sReg == null)
                 sReg = "";
             if (sReg.equalsIgnoreCase("Standard") || sReg.equalsIgnoreCase("Candidate")
                             || sReg.equalsIgnoreCase("Proposed"))
             {
                 String sDEC = DEBeanSR.getDE_DEC_IDSEQ();
                 String sRegValid = setAC.checkDECOCExist(sDEC, req, res);
                 if (sRegValid != null && !sRegValid.equals(""))
                 {
                     // go back to old one
                     String changeAC = "\\t Please note that "; // message if old one has the problem
                     boolean isRegChange = false;
                     if (!RegStatus.equals(""))
                     {
                         // check if old one is also standard
                         if (!oldReg.equalsIgnoreCase("Standard") && !oldReg.equalsIgnoreCase("Candidate")
                                         && !oldReg.equalsIgnoreCase("Proposed"))
                         {
                             DEBeanSR.setDE_REG_STATUS(oldReg);
                             changeAC = "\\t Unable to update the Registration Status because \\n\\t";
                             isRegChange = true;
                         }
                     }
                     // need to put back old dec, since can't do anything with Reg status
                     if (!sDEC_ID.equals("") && isRegChange == false)
                     {
                         DEBeanSR.setDE_DEC_IDSEQ(oldDEC);
                         changeAC = "\\t Unable to update the Data Element Concept because \\n\\t";
                         newDECVD = false;
                     }
                     storeStatusMsg(changeAC
                                     + "the Data Element Concept is not associated with an Object Class.\\n");
                 }
             }
             // update the names and the bean if dec and vd are valid
             String sDEC = de.getDE_DEC_NAME();
             if (sDEC == null)
                 sDEC = "";
             if (!sDEC.equals("") && newDECVD)
             {
                 DEBeanSR.setDE_DEC_NAME(sDEC);
                 DEBeanSR.setDE_DEC_Bean(de.getDE_DEC_Bean());
             }
             String sVD = de.getDE_VD_NAME();
             if (sVD == null)
                 sVD = "";
             if (!sVD.equals("") && newDECVD)
             {
                 DEBeanSR.setDE_VD_NAME(sVD);
                 DEBeanSR.setDE_VD_Bean(de.getDE_VD_Bean());
             }
             // get preferred name if not user and not released
             String prefType = de.getAC_PREF_NAME_TYPE();
           //  String oldName = DEBeanSR.getDE_PREFERRED_NAME();
             String oldASL = DEBeanSR.getDE_ASL_NAME();
             if (oldASL == null)
                 oldASL = "";
             String pageVer = de.getDE_VERSION();
             if (pageVer == null)
                 pageVer = "";
             if (prefType != null && !prefType.equals("USER"))
             {
                 DEBeanSR.setAC_PREF_NAME_TYPE(prefType);
                 if (oldASL.equals("RELEASED") && !(pageVer.equals("Point") || pageVer.equals("Whole")))
                     storeStatusMsg("\\t Unable to update the Short Name because \\n"
                                     + "\\t the Workflow Status of the Data Element is RELEASED.\\n");
                 else
                     DEBeanSR = this.doGetDENames(req, res, "noChange", "openDE", DEBeanSR);
             }
             // check the workflow status
             String sASL = de.getDE_ASL_NAME();
             if (sASL != null && !sASL.equals(""))
             {
                 String wfsValid = m_setAC.checkReleasedWFS(DEBeanSR, sASL);
                 if (wfsValid.equals(""))
                     DEBeanSR.setDE_ASL_NAME(sASL);
                 else
                     // do not update
                     storeStatusMsg("\\t Unable to update the Workflow Status because " + wfsValid + "\\n");
             }
             // other attributes
             String sDocText = de.getDOC_TEXT_PREFERRED_QUESTION();
             if (sDocText == null)
                 sDocText = "";
             if (!sDocText.equals(""))
                 DEBeanSR.setDOC_TEXT_PREFERRED_QUESTION(sDocText);
             String sBeginDate = de.getDE_BEGIN_DATE();
             if (sBeginDate == null)
                 sBeginDate = "";
             if (!sBeginDate.equals(""))
                 DEBeanSR.setDE_BEGIN_DATE(sBeginDate);
             String sEndDate = de.getDE_END_DATE();
             if (sEndDate == null)
                 sEndDate = "";
             if (!sEndDate.equals(""))
                 DEBeanSR.setDE_END_DATE(sEndDate);
             String sSource = de.getDE_SOURCE();
             if (sSource == null)
                 sSource = "";
             if (!sSource.equals(""))
                 DEBeanSR.setDE_SOURCE(sSource);
             String changeNote = de.getDE_CHANGE_NOTE();
             if (changeNote == null)
                 changeNote = "";
             if (!changeNote.equals(""))
                 DEBeanSR.setDE_CHANGE_NOTE(changeNote);
             // get cs-csi from the page into the DECBean for block edit
             Vector<AC_CSI_Bean> vAC_CS = de.getAC_AC_CSI_VECTOR();
             if (vAC_CS != null)
                 DEBeanSR.setAC_AC_CSI_VECTOR(vAC_CS);
         }
         catch (Exception e)
         {
             logger.fatal("Error - InsertEditsIntoDEBeanSR ", e);
         }
     }
 
     /**
      * updates bean the selected DE from the changed values of block edit.
      *
      * @param DEBeanSR
      *            selected DE bean from search result
      * @param de
      *            DE_Bean of the changed values.
      * @param req
      * @param res
      *
      * @return String valid if no version error
      * @throws Exception
      */
     public String InsertVersionDEBeanSR(DE_Bean DEBeanSR, DE_Bean de, HttpServletRequest req, HttpServletResponse res)
                     throws Exception
     {
         // get the version number if versioned
         String version = de.getDE_VERSION();
         String lastVersion = (String) DEBeanSR.getDE_VERSION();
         if (lastVersion == null)
             lastVersion = "";
         String verError = "valid";
        // InsACService insAC = new InsACService(req, res, this);
         GetACService getAC = new GetACService(req, res, this);
         String sValid = m_setAC.checkUniqueDECVDPair(DEBeanSR, getAC, "Edit", "Edit");
         if (sValid != null && !sValid.equals("")) // version only if valid dec-vd pair
         {
             storeStatusMsg("\\t Unable to create new version because \\n"
                             + "\\t the combination of DEC, VD and Context already exists in other Data Elements.\\n");
             verError = "decvdError";
         }
         else
         // get the right version number
         {
             String newVersion = this.getNewVersionNumber(version, lastVersion);
             if (newVersion != null && !newVersion.equals(""))
                 DEBeanSR.setDE_VERSION(newVersion);
             else
             {
                 storeStatusMsg("\\t Unable to create new version because \\n"
                                 + "\\t new version of the Data Element is not available.\\n");
                 verError = "verNumError";
             }
         }
         return verError;
     }
 
     /**
      * gets the point or whole version number from old version for block versioning.
      *
      * @param version
      *            Version of the selected from the page either point or whole
      * @param lastVersion
      *            old Version number of the selected bean.
      *
      * @return newVersion version number that need to updated to.
      * @throws Exception
      */
     private String getNewVersionNumber(String version, String lastVersion) throws Exception
     {
         int index = -1;
         String pointStr = ".";
         String strWhBegNumber = "";
         int iWhBegNumber = 0;
         index = lastVersion.indexOf(pointStr);
         String strPtBegNumber = lastVersion.substring(0, index);
         String afterDecimalNumber = lastVersion.substring((index + 1), (index + 2));
         if (index == 1)
             strWhBegNumber = "";
         else if (index == 2)
         {
             strWhBegNumber = lastVersion.substring(0, index - 1);
             Integer WhBegNumber = new Integer(strWhBegNumber);
             iWhBegNumber = WhBegNumber.intValue();
         }
         String strWhEndNumber = ".0";
         String beforeDecimalNumber = lastVersion.substring((index - 1), (index));
         String sNewVersion = "";
         Integer IadNumber = new Integer(0);
         Integer IbdNumber = new Integer(0);
         String strIncADNumber = "";
         String strIncBDNumber = "";
         if (version == null)
             version = "";
         else if (version.equals("Point"))
         {
             // Point new version
             int incrementADNumber = 0;
             int incrementBDNumber = 0;
             Integer adNumber = new Integer(afterDecimalNumber);
             Integer bdNumber = new Integer(strPtBegNumber);
             int iADNumber = adNumber.intValue(); // after decimal
             int iBDNumber = bdNumber.intValue(); // before decimal
             if (iADNumber != 9)
             {
                 incrementADNumber = iADNumber + 1;
                 IadNumber = new Integer(incrementADNumber);
                 strIncADNumber = IadNumber.toString();
                 sNewVersion = strPtBegNumber + "." + strIncADNumber; // + strPtEndNumber;
             }
             else
             // adNumber == 9
             {
                 incrementADNumber = 0;
                 incrementBDNumber = iBDNumber + 1;
                 IbdNumber = new Integer(incrementBDNumber);
                 strIncBDNumber = IbdNumber.toString();
                 IadNumber = new Integer(incrementADNumber);
                 strIncADNumber = IadNumber.toString();
                 sNewVersion = strIncBDNumber + "." + strIncADNumber; // + strPtEndNumber;
             }
             // DEBeanSR.setDE_VERSION(sNewVersion);
         }
         else if (version.equals("Whole"))
         {
             // Whole new version
             Integer bdNumber = new Integer(beforeDecimalNumber);
             int iBDNumber = bdNumber.intValue();
             int incrementBDNumber = iBDNumber + 1;
             if (iBDNumber != 9)
             {
                 IbdNumber = new Integer(incrementBDNumber);
                 strIncBDNumber = IbdNumber.toString();
                 sNewVersion = strWhBegNumber + strIncBDNumber + strWhEndNumber;
             }
             else
             // before decimal number == 9
             {
                 int incrementWhBegNumber = iWhBegNumber + 1;
                 Integer IWhBegNumber = new Integer(incrementWhBegNumber);
                 String strIncWhBegNumber = IWhBegNumber.toString();
                 IbdNumber = new Integer(0);
                 strIncBDNumber = IbdNumber.toString();
                 sNewVersion = strIncWhBegNumber + strIncBDNumber + strWhEndNumber;
             }
             // DEBeanSR.setDE_VERSION(sNewVersion);
         }
         return sNewVersion;
     }
 
     /**
      * update record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
      * editing. Retrieves the session bean m_DEC. calls 'insAC.setDEC' to update the database. otherwise calls
      * 'serAC.refreshData' to get the refreshed search result forwards the page back to search page with refreshed list
      * after updating.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'EditDECPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doUpdateDECActionBE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
         DataManager.setAttribute(session, "DECEditAction", ""); // reset this
         boolean isRefreshed = false;
         String ret = ":";
         InsACService insAC = new InsACService(req, res, this);
         GetACSearch serAC = new GetACSearch(req, res, this);
         GetACService getAC = new GetACService(req, res, this);
         // String sNewOC = (String)session.getAttribute("newObjectClass");
         // String sNewProp = (String)session.getAttribute("newProperty");
         Vector vBERows = (Vector) session.getAttribute("vBEResult");
         int vBESize = vBERows.size();
         Integer vBESize2 = new Integer(vBESize);
         req.setAttribute("vBESize", vBESize2);
        // String sOC_IDSEQ = "";
        // String sProp_IDSEQ = "";
         if (vBERows.size() > 0)
         {
             // Be sure the buffer is loaded when doing versioning.
             String newVersion = DECBean.getDEC_VERSION();
             if (newVersion == null)
                 newVersion = "";
             boolean newVers = (newVersion.equals("Point") || newVersion.equals("Whole"));
             if (newVers)
             {
                 @SuppressWarnings("unchecked")
                 Vector<AC_Bean> tvec = vBERows;
                 AltNamesDefsSession.loadAsNew(this, session, tvec);
             }
             for (int i = 0; i < (vBERows.size()); i++)
             {
                 DEC_Bean DECBeanSR = new DEC_Bean();
                 DECBeanSR = (DEC_Bean) vBERows.elementAt(i);
                 DEC_Bean oldDECBean = new DEC_Bean();
                 oldDECBean = oldDECBean.cloneDEC_Bean(DECBeanSR);
               //  String oldName = (String) DECBeanSR.getDEC_PREFERRED_NAME();
                 // gets all the changed attrributes from the page
                 InsertEditsIntoDECBeanSR(DECBeanSR, DECBean, req);
                 // DataManager.setAttribute(session, "m_DEC", DECBeanSR);
                 String oldID = oldDECBean.getDEC_DEC_IDSEQ();
                 // udpate the status message with DEC name and ID
                 storeStatusMsg("Data Element Concept Name : " + DECBeanSR.getDEC_LONG_NAME());
                 storeStatusMsg("Public ID : " + DECBeanSR.getDEC_DEC_ID());
                 // creates a new version
                 if (newVers) // block version
                 {
                     // creates new version first and updates all other attributes
                     String strValid = m_setAC.checkUniqueInContext("Version", "DEC", null, DECBeanSR, null, getAC,
                                     "version");
                     if (strValid != null && !strValid.equals(""))
                         ret = "unique constraint";
                     else
                         ret = insAC.setAC_VERSION(null, DECBeanSR, null, "DataElementConcept");
                     if (ret == null || ret.equals(""))
                     {
                         ret = insAC.setDEC("UPD", DECBeanSR, "BlockVersion", oldDECBean);
                         // resetEVSBeans(req, res);
                         // add this bean into the session vector
                         if (ret == null || ret.equals(""))
                         {
                             serAC.refreshData(req, res, null, DECBeanSR, null, null, "Version", oldID);
                             isRefreshed = true;
                             // reset the appened attributes to remove all the checking of the row
                             Vector vCheck = new Vector();
                             DataManager.setAttribute(session, "CheckList", vCheck);
                             DataManager.setAttribute(session, "AppendAction", "Not Appended");
                         }
                     }
                     // alerady exists
                     else if (ret.indexOf("unique constraint") >= 0)
                         storeStatusMsg("\\t The version " + DECBeanSR.getDEC_VERSION()
                                         + " already exists in the data base.\\n");
                     // some other problem
                     else
                         storeStatusMsg("\\t " + ret + " : Unable to create new version "
                                         + DECBeanSR.getDEC_VERSION() + ".\\n");
                 }
                 else
                 // block edit
                 {
                     ret = insAC.setDEC("UPD", DECBeanSR, "BlockEdit", oldDECBean);
                     // forward to search page with refreshed list after successful update
                     if ((ret == null) || ret.equals(""))
                     {
                         serAC.refreshData(req, res, null, DECBeanSR, null, null, "Edit", oldID);
                         isRefreshed = true;
                     }
                 }
             }
             AltNamesDefsSession.blockSave(this, session);
         }
         // to get the final result vector if not refreshed at all
         if (!(isRefreshed))
         {
             Vector<String> vResult = new Vector<String>();
             serAC.getDECResult(req, res, vResult, "");
             DataManager.setAttribute(session, "results", vResult); // store the final result in the session
             DataManager.setAttribute(session, "DECPageAction", "nothing");
         }
         // forward to search page.
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * update record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
      * editing. Retrieves the session bean m_DEC. calls 'insAC.setDE' to update the database. otherwise calls
      * 'serAC.refreshData' to get the refreshed search result forwards the page back to search page with refreshed list
      * after updating.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'EditDEPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doUpdateDEActionBE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
         if (DEBean == null)
             DEBean = new DE_Bean();
         String ret = ":";
         DataManager.setAttribute(session, "DEEditAction", ""); // reset this
         boolean isRefreshed = false;
         InsACService insAC = new InsACService(req, res, this);
         GetACSearch serAC = new GetACSearch(req, res, this);
         GetACService getAC = new GetACService(req, res, this);
         Vector vBERows = (Vector) session.getAttribute("vBEResult");
         int vBESize = vBERows.size();
         Integer vBESize2 = new Integer(vBESize);
         req.setAttribute("vBESize", vBESize2);
         if (vBERows.size() > 0)
         {
             // Be sure the buffer is loaded when doing versioning.
             String newVersion = DEBean.getDE_VERSION();
             if (newVersion == null)
                 newVersion = "";
             boolean newVers = (newVersion.equals("Point") || newVersion.equals("Whole"));
             if (newVers)
             {
                 @SuppressWarnings("unchecked")
                 Vector<AC_Bean> tvec = vBERows;
                 AltNamesDefsSession.loadAsNew(this, session, tvec);
             }
             for (int i = 0; i < (vBERows.size()); i++)
             {
                 DE_Bean DEBeanSR = new DE_Bean();
                 DEBeanSR = (DE_Bean) vBERows.elementAt(i);
                 // udpate the status message with DE name and ID
                 storeStatusMsg("Data Element Name : " + DEBeanSR.getDE_LONG_NAME());
                 storeStatusMsg("Public ID : " + DEBeanSR.getDE_MIN_CDE_ID());
                 DE_Bean oldDEBean = new DE_Bean();
                 oldDEBean = oldDEBean.cloneDE_Bean(DEBeanSR, "Complete");
               //  String oldName = (String) DEBeanSR.getDE_PREFERRED_NAME();
                 // gets all the data from the page
                 InsertEditsIntoDEBeanSR(DEBeanSR, DEBean, req, res);
                 // DataManager.setAttribute(session, "m_DE", DEBeanSR);
                 String oldID = oldDEBean.getDE_DE_IDSEQ();
                 // creates a new version
                 if (newVers) // block version
                 {
                     String validVer = this.InsertVersionDEBeanSR(DEBeanSR, DEBean, req, res);
                     if (validVer != null && validVer.equals("valid"))
                     {
                         // insert a new row with new version
                         String strValid = m_setAC.checkUniqueInContext("Version", "DE", DEBeanSR, null, null, getAC,
                                         "version");
                         if (strValid != null && !strValid.equals(""))
                             ret = "unique constraint";
                         else
                             ret = insAC.setAC_VERSION(DEBeanSR, null, null, "DataElement");
                         if ((ret == null) || ret.equals(""))
                         {
                             // update this new row with changed attributes
                             ret = insAC.setDE("UPD", DEBeanSR, "Version", oldDEBean);
                             if ((ret == null) || ret.equals(""))
                             {
                                 // do dde updates for new version
                                 serAC.getDDEInfo(oldDEBean.getDE_DE_IDSEQ()); // get info, set session attributes
                                 DataManager.setAttribute(session, "sRulesAction", "newRule"); // reset the rules action attribute
                                 ret = insAC.setDDE(DEBeanSR.getDE_DE_IDSEQ(), ""); // set DEComp rules and relations
                                 // save the status message and retain the this row in the vector
                                 serAC.refreshData(req, res, DEBeanSR, null, null, null, "Version", oldID);
                                 isRefreshed = true;
                                 // reset the appened attributes to remove all the checking of the row
                                 Vector vCheck = new Vector();
                                 DataManager.setAttribute(session, "CheckList", vCheck);
                                 DataManager.setAttribute(session, "AppendAction", "Not Appended");
                             }
                         }
                         // alerady exists
                         else if (ret.indexOf("unique constraint") >= 0)
                             storeStatusMsg("\\t The new version " + DEBeanSR.getDE_VERSION()
                                             + " already exists in the data base.\\n");
                         // some other problem
                         else
                             storeStatusMsg("\\t " + ret + " : Unable to create new version "
                                             + DEBeanSR.getDE_VERSION() + "\\n");
                     }
                 }
                 else
                 // block edit
                 {
                     ret = insAC.setDE("UPD", DEBeanSR, "Edit", oldDEBean);
                     if ((ret == null) || ret.equals(""))
                     {
                         serAC.refreshData(req, res, DEBeanSR, null, null, null, "Edit", oldID);
                         isRefreshed = true;
                     }
                 }
             }
             AltNamesDefsSession.blockSave(this, session);
         }
         // to get the final result vector if not refreshed at all
         if (!(isRefreshed))
         {
             Vector<String> vResult = new Vector<String>();
             serAC.getDEResult(req, res, vResult, "");
             DataManager.setAttribute(session, "results", vResult); // store the final result in the session
             DataManager.setAttribute(session, "DEPageAction", "nothing");
         }
         // forward to search page.
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * creates new record in the database and display the result. Called from 'doInsertVD' method when the aciton is
      * create new VD from DEPage. Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. forwards
      * the page back to create DE page after successful insert.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'createVDPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sOrigin
      *            string value from where vd creation action was originated.
      *
      * @throws Exception
      */
     public void doInsertVDfromDEAction(HttpServletRequest req, HttpServletResponse res, String sOrigin)
                     throws Exception
     {
         HttpSession session = req.getSession();
         VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
         InsACService insAC = new InsACService(req, res, this);
      //   GetACSearch serAC = new GetACSearch(req, res, this);
      //   String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // insert the building blocks attriubtes before inserting vd
         doInsertVDBlocks(req, res, null);
         String ret = insAC.setVD("INS", VDBean, "New", null);
         // updates the de bean with new vd data after successful insert and forwards to create page
         if ((ret == null) || ret.equals(""))
         {
             DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
             DEBean.setDE_VD_NAME(VDBean.getVD_LONG_NAME());
             DEBean.setDE_VD_IDSEQ(VDBean.getVD_VD_IDSEQ());
             // add DEC Bean into DE BEan
             DEBean.setDE_VD_Bean(VDBean);
             DataManager.setAttribute(session, "m_DE", DEBean);
             DEBean = this.doGetDENames(req, res, "new", "newVD", DEBean);
             this.clearCreateSessionAttributes(req, res); // clear some session attributes
             if (sOrigin != null && sOrigin.equals("CreateNewVDfromEditDE"))
                 ForwardJSP(req, res, "/EditDEPage.jsp");
             else
                 ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         // goes back to create vd page if error
         else
         {
             DataManager.setAttribute(session, "VDPageAction", "validate");
             ForwardJSP(req, res, "/CreateVDPage.jsp"); // send it back to vd page
         }
     }
 
     /**
      * to create object class, property, rep term and qualifier value from EVS into cadsr. Retrieves the session bean
      * m_VD. calls 'insAC.setDECQualifier' to insert the database.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param VDBeanSR
      *            dec attribute bean.
      *
      * @throws Exception
      */
     public void doInsertVDBlocks(HttpServletRequest req, HttpServletResponse res, VD_Bean VDBeanSR) throws Exception
     {
         HttpSession session = req.getSession();
         if (VDBeanSR == null)
             VDBeanSR = (VD_Bean) session.getAttribute("m_VD");
         String sRemoveRepBlock = (String) session.getAttribute("RemoveRepBlock");
         if (sRemoveRepBlock == null)
             sRemoveRepBlock = "";
         EVS_Bean REPBean = (EVS_Bean) session.getAttribute("m_REP");
         if (REPBean == null)
             REPBean = new EVS_Bean();
         EVS_Bean REPQBean = (EVS_Bean) session.getAttribute("m_REPQ");
         if (REPQBean == null)
             REPQBean = new EVS_Bean();
         String sNewRep = (String) session.getAttribute("newRepTerm");
         if (sNewRep == null)
             sNewRep = "";
         String sREP_IDSEQ = "";
         @SuppressWarnings("unused") String retObj = "";
      //   String retProp = "";
      //   String retRep = "";
      //  String retObjQual = "";
      //   String retPropQual = "";
      //   String retRepQual = "";
         InsACService insAC = new InsACService(req, res, this);
         /*
          * if (sNewRep.equals("true")) retRepQual = insAC.setRepresentation("INS", sREP_IDSEQ, VDBeanSR, REPQBean, req);
          * else if(sRemoveRepBlock.equals("true"))
          */
         String sRep = VDBeanSR.getVD_REP_TERM();
         if (sRep != null && !sRep.equals(""))
             retObj = insAC.setRepresentation("INS", sREP_IDSEQ, VDBeanSR, REPBean, req);
         // create new version if not released
         sREP_IDSEQ = VDBeanSR.getVD_REP_IDSEQ();
         if (sREP_IDSEQ != null && !sREP_IDSEQ.equals(""))
         {
             // CALL to create new version if not released
             if (VDBeanSR.getVD_REP_ASL_NAME() != null && !VDBeanSR.getVD_REP_ASL_NAME().equals("RELEASED"))
             {
                 sREP_IDSEQ = insAC.setOC_PROP_REP_VERSION(sREP_IDSEQ, "RepTerm");
                 if (sREP_IDSEQ != null && !sREP_IDSEQ.equals(""))
                     VDBeanSR.setVD_REP_IDSEQ(sREP_IDSEQ);
             }
         }
         else
         {
             if (VDBeanSR.getVD_REP_CONDR_IDSEQ() != null && !VDBeanSR.getVD_REP_CONDR_IDSEQ().equals(""))
                 VDBeanSR.setVD_REP_CONDR_IDSEQ("");
         }
         DataManager.setAttribute(session, "newRepTerm", "");
     }
 
     /**
      * creates new record in the database and display the result. Called from 'doInsertVD' method when the aciton is
      * create new VD from Menu. Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. calls
      * 'serAC.refreshData' to get the refreshed search result for template/version forwards the page back to create VD
      * page if new VD or back to search page if template or version after successful insert.
      *
      * If ret is not null stores the statusMessage as error message in session and forwards the page back to
      * 'createVDPage.jsp' for Edit.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doInsertVDfromMenuAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
         InsACService insAC = new InsACService(req, res, this);
         GetACSearch serAC = new GetACSearch(req, res, this);
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         VD_Bean oldVDBean = (VD_Bean) session.getAttribute("oldVDBean");
         if (oldVDBean == null)
             oldVDBean = new VD_Bean();
         String ret = "";
         boolean isUpdateSuccess = true;
         doInsertVDBlocks(req, res, null);
         if (sMenuAction.equals("NewVDVersion"))
         {
             // udpate the status message with DE name and ID
             storeStatusMsg("Value Domain Name : " + VDBean.getVD_LONG_NAME());
             storeStatusMsg("Public ID : " + VDBean.getVD_VD_ID());
             // creates new version first
             ret = insAC.setAC_VERSION(null, null, VDBean, "ValueDomain");
             if (ret == null || ret.equals(""))
             {
                 // get pvs related to this new VD, it was created in VD_Version
                 // TODO serAC.doPVACSearch(VDBean.getVD_VD_IDSEQ(), VDBean.getVD_LONG_NAME(), "Version");
                 PVServlet pvser = new PVServlet(req, res, this);
                 pvser.searchVersionPV(VDBean, 1, "", "");
                 // update non evs changes
                 Vector<EVS_Bean> vParent = VDBean.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
                 if (vParent != null && vParent.size() > 0)
                     vParent = serAC.getNonEVSParent(vParent, VDBean, "versionSubmit");
                 // get the right system name for new version; cannot use teh api because parent concept is not updated
                 // yet
                 String prefName = VDBean.getVD_PREFERRED_NAME();
                 if (prefName == null || prefName.equalsIgnoreCase("(Generated by the System)"))
                 {
                     VDBean = this.doGetVDSystemName(req, VDBean, vParent);
                     VDBean.setVD_PREFERRED_NAME(VDBean.getAC_SYS_PREF_NAME());
                 }
                 // and updates all other attributes
                 ret = insAC.setVD("UPD", VDBean, "Version", oldVDBean);
                 // resetEVSBeans(req, res);
                 if (ret != null && !ret.equals(""))
                 {
                     // add newly created row to searchresults and send it to edit page for update
                     isUpdateSuccess = false;
                     String oldID = oldVDBean.getVD_VD_IDSEQ();
                     String newID = VDBean.getVD_VD_IDSEQ();
                     String newVersion = VDBean.getVD_VERSION();
                     VDBean = VDBean.cloneVD_Bean(oldVDBean);
                     VDBean.setVD_VD_IDSEQ(newID);
                     VDBean.setVD_VERSION(newVersion);
                     VDBean.setVD_ASL_NAME("DRAFT MOD");
                     // refresh the result list by inserting newly created VD
                     serAC.refreshData(req, res, null, null, VDBean, null, "Version", oldID);
                 }
             }
             else
                 storeStatusMsg("\\t " + ret + " - Unable to create new version successfully.");
         }
         else
         {
             // creates new one
             ret = insAC.setVD("INS", VDBean, "New", oldVDBean); // create new one
         }
         if ((ret == null) || ret.equals(""))
         {
             this.clearCreateSessionAttributes(req, res); // clear some session attributes
             DataManager.setAttribute(session, "VDPageAction", "nothing");
             DataManager.setAttribute(session, "originAction", "");
             // forwards to search page with refreshed list if template or version
             if ((sMenuAction.equals("NewVDTemplate")) || (sMenuAction.equals("NewVDVersion")))
             {
                 DataManager.setAttribute(session, "searchAC", "ValueDomain");
                 DataManager.setAttribute(session, "originAction", "NewVDTemplate");
                 VDBean.setVD_ALIAS_NAME(VDBean.getVD_PREFERRED_NAME());
                 // VDBean.setVD_TYPE_NAME("PRIMARY");
                 String oldID = oldVDBean.getVD_VD_IDSEQ();
                 if (sMenuAction.equals("NewVDTemplate"))
                     serAC.refreshData(req, res, null, null, VDBean, null, "Template", oldID);
                 else if (sMenuAction.equals("NewVDVersion"))
                     serAC.refreshData(req, res, null, null, VDBean, null, "Version", oldID);
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             // forward to create vd page with empty data if new one
             else
             {
                 this.doOpenCreateNewPages(req, res, "vd");
                 /*
                  * VDBean = new VD_Bean(); VDBean.setVD_ASL_NAME("DRAFT NEW"); VDBean.setAC_PREF_NAME_TYPE("SYS");
                  * DataManager.setAttribute(session, "m_VD", VDBean); EVS_Bean m_OC = new EVS_Bean(); DataManager.setAttribute(session, "m_OC",
                  * m_OC); EVS_Bean m_PC = new EVS_Bean(); DataManager.setAttribute(session, "m_PC", m_PC); EVS_Bean m_Rep = new
                  * EVS_Bean(); DataManager.setAttribute(session, "m_Rep", m_Rep); EVS_Bean m_OCQ = new EVS_Bean();
                  * DataManager.setAttribute(session, "m_OCQ", m_OCQ); EVS_Bean m_PCQ = new EVS_Bean(); DataManager.setAttribute(session, "m_PCQ",
                  * m_PCQ); EVS_Bean m_REPQ = new EVS_Bean(); DataManager.setAttribute(session, "m_REPQ", m_REPQ);
                  * DataManager.setAttribute(session, "m_PCQ", m_PCQ); DataManager.setAttribute(session, "selPropRow", "");
                  * DataManager.setAttribute(session, "selPropQRow", ""); DataManager.setAttribute(session, "selObjQRow", "");
                  * DataManager.setAttribute(session, "selObjRow", ""); DataManager.setAttribute(session, "selRepQRow", "");
                  * DataManager.setAttribute(session, "selRepRow", ""); ForwardJSP(req, res, "/CreateVDPage.jsp");
                  */}
         }
         // goes back to create/edit vd page if error
         else
         {
             DataManager.setAttribute(session, "VDPageAction", "validate");
             // forward to create or edit pages
             if (isUpdateSuccess == false)
             {
                 // insert the created NUE in the results.
                 String oldID = oldVDBean.getVD_VD_IDSEQ();
                 if (sMenuAction.equals("NewVDTemplate"))
                     serAC.refreshData(req, res, null, null, VDBean, null, "Template", oldID);
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             else
                 ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
     }
 
     /**
      * The doSuggestionDE method forwards to EVSSearch jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doSuggestionDE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         ForwardJSP(req, res, "/EVSSearch.jsp");
     }
 
     /**
      * The doOpenCreatePVPage method gets the session, gets some values from the createVD page and stores in bean m_VD,
      * sets some session attributes, then forwards to CreatePV page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sPVAction
      *            string pv action
      * @param vdPage
      *            string opened vd page
      *
      * @throws Exception
      */
     public void doOpenCreatePVPage(HttpServletRequest req, HttpServletResponse res, String sPVAction, String vdPage)
                     throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "PVAction", sPVAction);
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
         m_setAC.setVDValueFromPage(req, res, m_VD); // store VD bean
         DataManager.setAttribute(session, "VDPageAction", "validate");
         DataManager.setAttribute(session, "m_VD", m_VD);
         // call the method to add/remove pvs.
         m_setAC.addRemovePageVDPVs(req, res);
         if (sPVAction.equals("removePV"))
         {
             if (vdPage.equals("editVD"))
                 ForwardJSP(req, res, "/EditVDPage.jsp");
             else
                 ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         else
         {
             // store the old pv in another session
             PV_Bean pvBean = (PV_Bean) session.getAttribute("m_PV");
             if (pvBean == null)
                 pvBean = new PV_Bean();
             // copy the pv session attributes to store that can be used for clear button
             PV_Bean pageBean = new PV_Bean();
             pageBean = pageBean.copyBean(pvBean);
             DataManager.setAttribute(session, "pageOpenBean", pageBean);
             ForwardJSP(req, res, "/CreatePVPage.jsp");
         }
     }
 
     /**
      * The doJspErrorAction method is called when there is an error on a jsp page. User is forwarded to
      * SearchResultsPage
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doJspErrorAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         doOpenSearchPage(req, res);
     }
 
     /**
      * The doSearchPV method gets the session, gets some values from the createVD page and stores in bean m_VD, sets
      * some session attributes, then forwards to CreatePV page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doSearchPV(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
         EVS_Bean m_OC = new EVS_Bean();
         EVS_Bean m_PC = new EVS_Bean();
         EVS_Bean m_REP = new EVS_Bean();
         EVS_Bean m_OCQ = new EVS_Bean();
         EVS_Bean m_PCQ = new EVS_Bean();
         EVS_Bean m_REPQ = new EVS_Bean();
         m_setAC.setVDValueFromPage(req, res, m_VD); // store VD bean
         DataManager.setAttribute(session, "VDPageAction", "searchValues");
         DataManager.setAttribute(session, "m_VD", m_VD);
         DataManager.setAttribute(session, "m_OC", m_OC);
         DataManager.setAttribute(session, "m_PC", m_PC);
         DataManager.setAttribute(session, "m_REP", m_REP);
         DataManager.setAttribute(session, "m_OCQ", m_OCQ);
         DataManager.setAttribute(session, "m_PCQ", m_PCQ);
         DataManager.setAttribute(session, "m_REPQ", m_REPQ);
         DataManager.setAttribute(session, "PValue", "");
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * The doOpenCreateVMPage method gets the session, gets some values from the createVD page and stores in bean m_VD,
      * sets some session attributes, then forwards to CreateVM page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param origin
      *
      * @throws Exception
      */
     public void doOpenCreateVMPage(HttpServletRequest req, HttpServletResponse res, @SuppressWarnings("unused") String origin) throws Exception
     {
         HttpSession session = req.getSession();
         String sOrigin = (String) session.getAttribute("originAction");
         PV_Bean m_PV = new PV_Bean();
         SetACService setAC = new SetACService(this);
         setAC.setPVValueFromPage(req, res, m_PV);
         DataManager.setAttribute(session, "m_PV", m_PV);
         // reset vm bean
         VM_Bean VMBean = new VM_Bean();
         // get cd value from the VD bean and make it default for VM
         VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
         VMBean.setVM_CD_NAME(VDBean.getVD_CD_NAME());
         VMBean.setVM_CD_IDSEQ(VDBean.getVD_CD_IDSEQ());
         // store the bean in the session
         DataManager.setAttribute(session, "m_VM", VMBean);
         DataManager.setAttribute(session, "creSearchAC", "EVSValueMeaning");
         if (sOrigin.equals("CreateInSearch"))
             ForwardJSP(req, res, "/CreateVMSearchPage.jsp");
         else
             ForwardJSP(req, res, "/CreateVMPage.jsp");
     }
 
     /**
      * The doOpenCreateDECPage method gets the session, gets some values from the createDE page and stores in bean m_DE,
      * sets some session attributes, then forwards to CreateDEC page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doOpenCreateDECPage(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // DataManager.setAttribute(session, "originAction", fromWhere); //"CreateNewDECfromCreateDE");
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         m_setAC.setDEValueFromPage(req, res, m_DE); // store DEC bean
         DataManager.setAttribute(session, "m_DE", m_DE);
         DEC_Bean m_DEC = new DEC_Bean();
         m_DEC.setDEC_ASL_NAME("DRAFT NEW");
         m_DEC.setAC_PREF_NAME_TYPE("SYS");
         DataManager.setAttribute(session, "m_DEC", m_DEC);
         DEC_Bean oldDEC = new DEC_Bean();
         oldDEC = oldDEC.cloneDEC_Bean(m_DEC);
         DataManager.setAttribute(session, "oldDECBean", oldDEC);
         this.clearCreateSessionAttributes(req, res); // clear some session attributes
         // DataManager.setAttribute(session, "oldDECBean", m_DEC);
         ForwardJSP(req, res, "/CreateDECPage.jsp");
     }
 
     /**
      * The doOpenCreateDECompPage method get current primary DE bean, seve it to session as old DE Bean, then forward to
      * CreateDE page for DE Comp. It is called from doCreateDEActions and doEditDEActions
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doOpenCreateDECompPage(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // DataManager.setAttribute(session, "originAction", "CreateNewDEFComp");
         DataManager.setAttribute(session, "DDEAction", "CreateNewDEFComp");
         // store the old bean into primary old bean
         DE_Bean oldBean = (DE_Bean) session.getAttribute("oldDEBean");
         if (oldBean == null)
             oldBean = new DE_Bean();
         DE_Bean primBean = oldBean.cloneDE_Bean(oldBean, "Complete");
         DataManager.setAttribute(session, "p_oldBean", primBean);
         // store the page bean into primary bean
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         m_setAC.setDEValueFromPage(req, res, m_DE); // store DE bean
         DataManager.setAttribute(session, "p_DEBean", m_DE); // save primary DE
         // clear DEBean because new DE Comp
         DE_Bean de = new DE_Bean();
         de.setDE_ASL_NAME("DRAFT NEW");
         de.setAC_PREF_NAME_TYPE("SYS");
         DataManager.setAttribute(session, "m_DE", de);
         DataManager.setAttribute(session, "oldDEBean", new DE_Bean());
         this.clearCreateSessionAttributes(req, res); // clear some session attributes
         ForwardJSP(req, res, "/CreateDEPage.jsp");
     }
 
     /**
      * The doOpenDEPageFromDEComp method set primary DE from old DE Bean back to current DE Bean, then forward to
      * CreateDE page or EditDE page. It is called from doCreateDEActions
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doOpenDEPageFromDEComp(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sPageAction = (String) req.getParameter("pageAction");
         DataManager.setAttribute(session, "DDEAction", "nothing"); // reset from "CreateNewDEFComp"
         // set primary DE back
         DE_Bean pDEBean = new DE_Bean();
         pDEBean = (DE_Bean) session.getAttribute("p_DEBean");
         DataManager.setAttribute(session, "m_DE", pDEBean);
         // set primary oldDE back
         DE_Bean pOldBean = new DE_Bean();
         pOldBean = (DE_Bean) session.getAttribute("p_oldBean");
         DataManager.setAttribute(session, "oldDEBean", pOldBean);
         if (sPageAction.equals("DECompBackToNewDE"))
         {
             DataManager.setAttribute(session, "originAction", "NewDEFromMenu");
             ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else if (sPageAction.equals("DECompBackToEditDE"))
         {
             DataManager.setAttribute(session, "originAction", "EditDE");
             ForwardJSP(req, res, "/EditDEPage.jsp");
         }
     }
 
     /**
      * The doUpdateDDEInfo get DDE info from jsp page hidden fields and save them to session It is called from
      * doCreateDEActions and doEditDEActions
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     public void doUpdateDDEInfo(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // Create DDE from CreateDE page, save existed DEComp to session first, then goto CreateDE page for DDE
         // get exist vDEComp vectors from jsp
         String sDEComps[] = req.getParameterValues("selDECompHidden");
         String sDECompIDs[] = req.getParameterValues("selDECompIDHidden");
         String sDECompOrders[] = req.getParameterValues("selDECompOrderHidden");
         String sDECompRelIDs[] = req.getParameterValues("selDECompRelIDHidden");
         Vector<String> vDEComp = new Vector();
         Vector<String> vDECompID = new Vector();
         Vector<String> vDECompOrder = new Vector();
         Vector<String> vDECompRelID = new Vector();
         if (sDEComps != null && sDECompIDs != null)
         {
             for (int i = 0; i < sDEComps.length; i++)
             {
                 String sDEComp = sDEComps[i];
                 String sDECompID = sDECompIDs[i];
                 String sDECompOrder = sDECompOrders[i];
                 String sDECompRelID = sDECompRelIDs[i];
                 vDEComp.addElement(sDEComp);
                 vDECompID.addElement(sDECompID);
                 vDECompOrder.addElement(sDECompOrder);
                 vDECompRelID.addElement(sDECompRelID);
             }
             // sort vDEComp against DECompOrder
             UtilService util = new UtilService();
             util.sortDEComps(vDEComp, vDECompID, vDECompRelID, vDECompOrder);
         }
         // save it, even empty, refresh
         DataManager.setAttribute(session, "vDEComp", vDEComp);
         DataManager.setAttribute(session, "vDECompID", vDECompID);
         DataManager.setAttribute(session, "vDECompOrder", vDECompOrder);
         DataManager.setAttribute(session, "vDECompRelID", vDECompRelID);
         // DEComp removed list
         String sDECompDeletes[] = req.getParameterValues("selDECompDeleteHidden");
         String sDECompDelNames[] = req.getParameterValues("selDECompDelNameHidden");
         Vector<String> vDECompDelete = new Vector<String>();
         Vector<String> vDECompDelName = new Vector<String>();
         if (sDECompDeletes != null)
         {
             for (int i = 0; i < sDECompDeletes.length; i++)
             {
                 String sDECompDelete = sDECompDeletes[i];
                 String sDECompDelName = sDECompDelNames[i];
                 vDECompDelete.addElement(sDECompDelete);
                 vDECompDelName.addElement(sDECompDelName);
                 // System.out.println(sDECompDelName + " updte dde info " + sDECompDelete);
             }
         }
         // save it to session
         DataManager.setAttribute(session, "vDECompDelete", vDECompDelete);
         DataManager.setAttribute(session, "vDECompDelName", vDECompDelName);
         // DDE rules
         String sDDERepTypes[] = req.getParameterValues("selRepType");
         String sRepType = sDDERepTypes[0];
         String sRule = (String) req.getParameter("DDERule");
         String sMethod = (String) req.getParameter("DDEMethod");
         String sConcatChar = (String) req.getParameter("DDEConcatChar");
         if (sRepType != null)
             DataManager.setAttribute(session, "sRepType", sRepType);
         else
             DataManager.setAttribute(session, "sRepType", "");
         if (sConcatChar != null)
             DataManager.setAttribute(session, "sConcatChar", sConcatChar);
         else
             DataManager.setAttribute(session, "sConcatChar", "");
         if (sRule != null)
             DataManager.setAttribute(session, "sRule", sRule);
         else
             DataManager.setAttribute(session, "sRule", "");
         if (sMethod != null)
             DataManager.setAttribute(session, "sMethod", sMethod);
         else
             DataManager.setAttribute(session, "sMethod", "");
     }
 
     /**
      * The doInitDDEInfo set DDE data to session It is called from doOpenCreateNewPages and doOpenSearchPage
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doInitDDEInfo(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         Vector vDEComp = new Vector();
         Vector vDECompID = new Vector();
         Vector vDECompOrder = new Vector();
         DataManager.setAttribute(session, "vDEComp", vDEComp);
         DataManager.setAttribute(session, "vDECompID", vDECompID);
         DataManager.setAttribute(session, "vDECompOrder", vDECompOrder);
         DataManager.setAttribute(session, "sRepType", "");
         DataManager.setAttribute(session, "NotValidDBType", "");
         DataManager.setAttribute(session, "sConcatChar", "");
         DataManager.setAttribute(session, "sRule", "");
         DataManager.setAttribute(session, "sMethod", "");
         DataManager.setAttribute(session, "sRulesAction", "newRule");
         // init rep type drop list
         // GetACSearch serAC = new GetACSearch(req, res, this);
         // serAC.getComplexRepType();
     } // end of doInitDDEInfo
 
     /**
      * The doOpenCreateVDPage method gets the session, gets some values from the createDE page and stores in bean m_DE,
      * sets some session attributes, then forwards to CreateVD page
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doOpenCreateVDPage(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         m_setAC.setDEValueFromPage(req, res, m_DE); // store VD bean
         DataManager.setAttribute(session, "m_DE", m_DE);
         // clear some session attributes
         this.clearCreateSessionAttributes(req, res);
         // reset the vd attributes
         VD_Bean m_VD = new VD_Bean();
         m_VD.setVD_ASL_NAME("DRAFT NEW");
         m_VD.setAC_PREF_NAME_TYPE("SYS");
         // call the method to get the QuestValues if exists
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         if (sMenuAction.equals("Questions"))
         {
             GetACSearch serAC = new GetACSearch(req, res, this);
             serAC.getACQuestionValue(m_VD);
             // check if enumerated or not
             Vector vCRFval = (Vector) session.getAttribute("vQuestValue");
             if (vCRFval != null && vCRFval.size() > 0)
                 m_VD.setVD_TYPE_FLAG("E");
             else
                 m_VD.setVD_TYPE_FLAG("N");
             // read property file and set the VD bean for Placeholder data
             String VDDefinition = NCICurationServlet.m_settings.getProperty("VDDefinition");
             m_VD.setVD_PREFERRED_DEFINITION(VDDefinition);
             String DataType = NCICurationServlet.m_settings.getProperty("DataType");
             m_VD.setVD_DATA_TYPE(DataType);
             String MaxLength = NCICurationServlet.m_settings.getProperty("MaxLength");
             m_VD.setVD_MAX_LENGTH_NUM(MaxLength);
         }
         DataManager.setAttribute(session, "m_VD", m_VD);
         VD_Bean oldVD = new VD_Bean();
         oldVD = oldVD.cloneVD_Bean(m_VD);
         DataManager.setAttribute(session, "oldVDBean", oldVD);
         // DataManager.setAttribute(session, "oldVDBean", m_VD);
         ForwardJSP(req, res, "/CreateVDPage.jsp");
     }
 
     /**
      * To search a component or to display more attributes after the serach. Called from 'service' method where reqType
      * is 'searchACs' calls 'getACSearch.getACKeywordResult' method when the action is a new search. calls
      * 'getACSearch.getACShowResult' method when the action is a display attributes. calls 'doRefreshPageForSearchIn'
      * method when the action is searchInSelect. forwards JSP 'SearchResultsPage.jsp' if the action is not
      * searchForCreate. if action is searchForCreate forwards OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doGetACSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String actType = (String) req.getParameter("actSelect");
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         String sUISearchType = (String) req.getAttribute("UISearchType");
         if (sUISearchType == null || sUISearchType.equals("nothing"))
             sUISearchType = "";
        // String sSearchInEVS = "";
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         if ((menuAction != null) && (actType != null))
         {
             // start a new search from search parameter
             if (actType.equals("Search"))
             {
                 // search is from create page
                 if (menuAction.equals("searchForCreate"))
                 {
                     getACSearch.getACSearchForCreate(req, res, false);
                     ForwardJSP(req, res, "/OpenSearchWindow.jsp");
                 }
                 // search is from regular search page
                 else
                 {
                     String sComponent = (String) req.getParameter("listSearchFor");
                     if (sComponent != null && sComponent.equals("Questions"))
                     {
                         DataManager.setAttribute(session, "originAction", "QuestionSearch");
                         getACSearch.getACQuestion();
                     }
                     else
                         getACSearch.getACKeywordResult(req, res);
                     // forward to search result page of main search
                     ForwardJSP(req, res, "/SearchResultsPage.jsp");
                 }
             }
             // set the attribute send the page back to refresh.
             else if (actType.equals("SearchDef"))
             {
                 getACSearch.doSearchEVS(req, res);
                 ForwardJSP(req, res, "/EVSSearchPage.jsp");
             }
             else if (actType.equals("SearchDefVM"))
             {
                 getACSearch.doSearchEVS(req, res);
                 ForwardJSP(req, res, "/EVSSearchPageVM.jsp");
             }
             // show the selected attributes (update button)
             else if (actType.equals("Attribute"))
             {
                 getACSearch.getACShowResult(req, res, "Attribute");
                 if (menuAction.equals("searchForCreate"))
                     ForwardJSP(req, res, "/OpenSearchWindow.jsp");
                 else
                     ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
             else if (actType.equals("AttributeRef"))
             {
                 getACSearch.getACShowResult(req, res, "Attribute");
                 ForwardJSP(req, res, "/OpenSearchWindowReference.jsp");
             }
             // set the attribute send the page back to refresh.
             else if (actType.equals("searchInSelect"))
                 doRefreshPageForSearchIn(req, res);
             // set the attribute send the page back to refresh.
             else if (actType.equals("searchForSelectOther"))
                 doRefreshPageOnSearchFor(req, res, "Other");
             // set the attribute send the page back to refresh.
             else if (actType.equals("searchForSelectCRF"))
                 doRefreshPageOnSearchFor(req, res, "CRFValue");
             // call method to UI filter change when hyperlink if pressed.
             else if (actType.equals("advanceFilter") || actType.equals("simpleFilter"))
                 this.doUIFilterChange(req, res, menuAction, actType);
             // call method when hyperlink if pressed.
             else if (actType.equals("term") || actType.equals("tree"))
             {
                 EVSSearch evs = new EVSSearch(req, res, this);
                 evs.doTreeSearch(actType, "EVSValueMeaning");
             }
             // something is wrong, send error page
             else
                 ForwardJSP(req, res, "/ErrorPage.jsp");
         }
         else
             ForwardJSP(req, res, "/ErrorPage.jsp");
     }
 
     /**
      * To refresh the page when filter hyperlink is pressed. Called from 'doGetACSearchActions' method gets request
      * parameters to store the selected values in the session according to what the menu action is forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param menuAction
      *            to distinguish between main search and search for create windows
      * @param actType
      *            type of filter a simple or advanced
      *
      * @throws Exception
      */
     private void doUIFilterChange(HttpServletRequest req, HttpServletResponse res, String menuAction, String actType)
                     throws Exception
     {
         HttpSession session = req.getSession();
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         String sSearchAC = req.getParameter("listSearchFor");
         // store the all the selected attributes in search parameter jsp
         this.getSelectedAttr(req, res, menuAction, "ChangeUIFilter");
         // get list of search previous search results
         Vector vResult = getACSearch.refreshSearchPage(sSearchAC);
         DataManager.setAttribute(session, "results", vResult);
         // set the session attributes send the page back to refresh for simple filter.
         if (menuAction.equals("searchForCreate"))
         {
             if (actType.equals("advanceFilter"))
                 DataManager.setAttribute(session, "creUIFilter", "advanced");
             else if (actType.equals("simpleFilter"))
                 DataManager.setAttribute(session, "creUIFilter", "simple");
             ForwardJSP(req, res, "/OpenSearchWindow.jsp");
         }
         // set session the attribute send the page back to refresh for advanced filter.
         else
         {
             if (actType.equals("advanceFilter"))
                 DataManager.setAttribute(session, "serUIFilter", "advanced");
             else if (actType.equals("simpleFilter"))
                 DataManager.setAttribute(session, "serUIFilter", "simple");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
     }
 
     /**
      * method to call all evs actions
      *
      * @param reqType
      *            String evs search action
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      */
     private void doEVSSearchActions(String reqType, HttpServletRequest req, HttpServletResponse res)
     {
         try
         {
             // System.out.println("evs search " + reqType);
             EVSSearch evs = new EVSSearch(req, res, this);
             if (reqType.equals("getSuperConcepts"))
                 evs.doGetSuperConcepts();
             else if (reqType.equals("getSubConcepts"))
                 evs.doGetSubConcepts();
             else if (reqType.equals("treeSearch"))
                 evs.doTreeSearchRequest("", "", "", "");
             else if (reqType.equals("treeRefresh"))
                 evs.doTreeRefreshRequest();
             else if (reqType.equals("treeExpand"))
                 evs.doTreeExpandRequest();
             else if (reqType.equals("treeCollapse"))
                 evs.doTreeCollapseRequest();
             else if (reqType.equals("OpenTreeToConcept"))
                 evs.openTreeToConcept(reqType);
             else if (reqType.equals("OpenTreeToParentConcept"))
                 evs.openTreeToConcept(reqType);
             // else if (reqType.equals("OpenTreeToParentConcept"))
             // evs.openTreeToParentConcept(reqType);
             else if (reqType.equals("term") || reqType.equals("tree"))
             {
                 String dtsVocab = req.getParameter("listContextFilterVocab");
                 evs.doCollapseAllNodes(dtsVocab);
                 evs.doTreeSearch(reqType, "Blocks");
             }
             else if (reqType.equals("defaultBlock"))
             {
                 String dtsVocab = req.getParameter("listContextFilterVocab");
                 evs.doCollapseAllNodes(dtsVocab);
             }
             else if (reqType.equals("showConceptInTree"))
                 evs.showConceptInTree(reqType);
         }
         catch (Exception ex)
         {
             // System.out.println("doEVSSearchActions : " + ex.toString());
             logger.fatal("doEVSSearchActions : " + ex.toString(), ex);
             // this.ForwardErrorJSP(req, res, ex.getMessage());
         }
     }
 
     /**
      * To refresh the page when the search in changed from drop down list. Called from 'doGetACSearchActions' method
      * modifies the session attribute 'selectedAttr' or 'creSelectedAttr' according to what is selected. forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     public void doRefreshPageForSearchIn(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // same for both searchfor create and regular search
         String sSearchIn = (String) req.getParameter("listSearchIn");
         if (sSearchIn == null)
             sSearchIn = "longName";
         // same for both searchfor create and regular search
         String sSearchAC = (String) req.getParameter("listSearchFor");
         // set the selected display attributes so they persist through refreshes
         String selAttrs[] = req.getParameterValues("listAttrFilter");
        // int selLength = selAttrs.length;
         Vector<String> vSelAttrs = new Vector<String>();
         String sID = "";
         if (selAttrs != null)
         {
             for (int i = 0; i < selAttrs.length; i++)
             {
                 sID = selAttrs[i];
                 if ((sID != null) && (!sID.equals("")))
                     vSelAttrs.addElement(sID);
             }
         }
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // add/remove protocol and crf from the displayable attriubtes according to the search in.
         Vector<String> vCompAttr = new Vector<String>();
         if (menuAction.equals("searchForCreate"))
             vCompAttr = (Vector) session.getAttribute("creAttributeList");
         else
             vCompAttr = (Vector) session.getAttribute("serAttributeList");
         if (vCompAttr != null && sSearchIn.equals("CRFName"))
         {
             if (!vCompAttr.contains("Protocol ID"))
                 vCompAttr.insertElementAt("Protocol ID", 13);
             if (!vCompAttr.contains("CRF Name"))
                 vCompAttr.insertElementAt("CRF Name", 14);
         }
         else
         {
             if (vCompAttr.contains("Protocol ID"))
                 vCompAttr.removeElement("Protocol ID");
             if (vCompAttr.contains("CRF Name"))
                 vCompAttr.removeElement("CRF Name");
         }
         // put it back in the session
         if (menuAction.equals("searchForCreate"))
             DataManager.setAttribute(session, "creAttributeList", vCompAttr);
         else
             DataManager.setAttribute(session, "serAttributeList", vCompAttr);
         // store the all the selected attributes in search parameter jsp
         this.getSelectedAttr(req, res, menuAction, "ChangeSearchIn");
         // gets selected attributes and sets session attributes.
         if (!menuAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "serSearchIn", sSearchIn); // set the search in attribute
             // call method to add or remove selected display attributes as search in changes
             Vector vSelectedAttr = getDefaultSearchInAttr(sSearchAC, sSearchIn, vSelAttrs, vCompAttr);
             // Store the session attributes
             DataManager.setAttribute(session, "selectedAttr", vSelectedAttr);
             GetACSearch serAC = new GetACSearch(req, res, this);
             Vector vResult = serAC.refreshSearchPage(sSearchAC);
             DataManager.setAttribute(session, "results", vResult);
             DataManager.setAttribute(session, "serKeyword", "");
             DataManager.setAttribute(session, "serProtoID", "");
             // send page
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
         else
         // menu action searchForCreate
         {
             req.setAttribute("creSearchIn", sSearchIn); // set the search in attribute
             // call method to add or remove selected display attributes as search in changes
             Vector vSelectedAttr = getDefaultSearchInAttr(sSearchAC, sSearchIn, vSelAttrs, vCompAttr);
             // Store the session attributes
             DataManager.setAttribute(session, "creSelectedAttr", vSelectedAttr);
             // req.setAttribute("creSelectedAttrBlocks", vSelectedAttr);
             GetACSearch serAC = new GetACSearch(req, res, this);
             Vector vResult = serAC.refreshSearchPage(sSearchAC);
             DataManager.setAttribute(session, "results", vResult);
             DataManager.setAttribute(session, "creKeyword", "");
             // set the session attribute for searchAC
             ForwardJSP(req, res, "/OpenSearchWindow.jsp");
         }
     }
 
     /**
      * To add or remove search in attributes as seach in changed.
      *
      * @param sSearchAC
      *            String searching component
      * @param sSearchIn
      *            String searching in attribute
      * @param vSelectedAttr
      *            Vector selected attribute
      * @param vComp
      *            Vector of all attributes of the selected component.
      *
      * @return Vector selected attribute vector
      * @throws Exception
      */
     private Vector getDefaultSearchInAttr(String sSearchAC, String sSearchIn, Vector<String> vSelectedAttr,
                     Vector<String> vComp) throws Exception
     {
         // first remove all the searchIn from the selected attribute list
         if (vSelectedAttr.contains("Protocol ID"))
             vSelectedAttr.remove("Protocol ID");
         if (vSelectedAttr.contains("CRF Name"))
             vSelectedAttr.remove("CRF Name");
         // add public id to selected attribute seperately for each type
         if (sSearchIn.equals("minID"))
         {
             if (!vSelectedAttr.contains("Public ID"))
                 vSelectedAttr.add("Public ID");
         }
         // select the hist cde id if not selected and remove crf/protocol for hist cdeid searchin
         else if (sSearchIn.equals("histID"))
         {
             // if (!vSelectedAttr.contains("Historical CDE ID"))
             // vSelectedAttr.add("Historical CDE ID");
             if (!vSelectedAttr.contains("Alternate Names"))
                 vSelectedAttr.add("Alternate Names");
         }
         else if (sSearchIn.equals("origin"))
         {
             if (!vSelectedAttr.contains("Origin"))
                 vSelectedAttr.add("Origin");
         }
         else if (sSearchIn.equals("concept"))
         {
             if (sSearchAC.equals("DataElement") || sSearchAC.equals("DataElementConcept")
                             || sSearchAC.equals("ValueDomain") || sSearchAC.equals("ConceptualDomain")
                             || sSearchAC.equals("ClassSchemeItems"))
             {
                 if (!vSelectedAttr.contains("Concept Name"))
                     vSelectedAttr.add("Concept Name");
             }
         }
         /*
          * else if (sSearchIn.equals("NamesAndDocText")) { if (!vSelectedAttr.contains("Preferred Question Text Document
          * Text")) vSelectedAttr.add("Preferred Question Text Document Text"); if (!vSelectedAttr.contains("Historic
          * Short CDE Name Document Text")) vSelectedAttr.add("Historic Short CDE Name Document Text"); } else if
          * (sSearchIn.equals("docText")) { if (!vSelectedAttr.contains("Preferred Question Text Document Text"))
          * vSelectedAttr.add("Preferred Question Text Document Text"); if (!vSelectedAttr.contains("Historic Short CDE
          * Name Document Text")) vSelectedAttr.add("Historic Short CDE Name Document Text"); if
          * (!vSelectedAttr.contains("Reference Documents")) vSelectedAttr.add("Reference Documents"); }
          */
         // add ref docs in the displayable list if doc text is selected
         else if (sSearchIn.equals("docText") || sSearchIn.equals("NamesAndDocText"))
         {
             if (!vSelectedAttr.contains("Reference Documents"))
                 vSelectedAttr.add("Reference Documents");
         }
         else if (sSearchIn.equals("permValue"))
         {
             if (!vSelectedAttr.contains("Permissible Value"))
                 vSelectedAttr.add("Permissible Value");
         }
         // add proto and crf and remove cde id if crf name is search in
         else if (sSearchIn.equals("CRFName"))
         {
             if (sSearchAC.equals("DataElement"))
             {
                 if (!vSelectedAttr.contains("Protocol ID"))
                     vSelectedAttr.add("Protocol ID");
                 if (!vSelectedAttr.contains("CRF Name"))
                     vSelectedAttr.add("CRF Name");
             }
             else if (sSearchAC.equals("Questions"))
             {
                 if (!vSelectedAttr.contains("Question Text"))
                     vSelectedAttr.add("Question Text");
                 if (!vSelectedAttr.contains("DE Long Name"))
                     vSelectedAttr.add("DE Long Name");
                 if (!vSelectedAttr.contains("DE Public ID"))
                     vSelectedAttr.add("DE Public ID");
                 if (!vSelectedAttr.contains("Workflow Status"))
                     vSelectedAttr.add("Workflow Status");
                 if (!vSelectedAttr.contains("Protocol ID"))
                     vSelectedAttr.add("Protocol ID");
             }
         }
         // call method to resort the display attributes
         vSelectedAttr = this.resortDisplayAttributes(vComp, vSelectedAttr);
         return vSelectedAttr;
     } // end of getDefaultSearchInAttr
 
     /**
      * resorts the display attributes from the component attributes after add/remove attributes of selected attribute
      * vector.
      *
      * @param vCompAttr
      *            list of attributes of the selected component
      * @param vSelectAttr
      *            list of selected attributes of according to the action
      *
      * @return return the sorted selected attributes list
      *
      * @throws Exception
      */
     public Vector<String> resortDisplayAttributes(Vector<String> vCompAttr, Vector<String> vSelectAttr)
                     throws Exception
     {
         // resort the display attributes
         Vector<String> vReSort = new Vector<String>();
         if (vCompAttr != null)
         {
             for (int j = 0; j < vCompAttr.size(); j++)
             {
                 String thisAttr = (String) vCompAttr.elementAt(j);
                 // add this attr to a vector if it is a selected attr
                 if (vSelectAttr.contains(thisAttr))
                     vReSort.addElement(thisAttr);
             }
         }
         return vReSort;
     }
 
     /**
      * To refresh the page when filter hyperlink is pressed. Called from 'doGetACSearchActions' method gets request
      * parameters to store the selected values in the session according to what the menu action is forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param menuAction
      *            to distinguish between main search and search for create windows
      * @param actType
      *            type of filter a simple or advanced
      *
      * @throws Exception
      */
     private void getSelectedAttr(HttpServletRequest req, HttpServletResponse res, String menuAction, String actType)
                     throws Exception
     {
         HttpSession session = req.getSession();
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         String sSearchAC = "";
         Vector vDocType = new Vector();
         // store the all the attributes in search parameter jsp
         String sProtoID = (String) req.getParameter("protoKeyword");
         String sKeyword = (String) req.getParameter("keyword"); // the keyword
         String sContext = (String) req.getParameter("listContextFilter"); // filter by context
         String sContextUse = (String) req.getParameter("rContextUse"); // filter by contextUse
         String sVersion = (String) req.getParameter("rVersion"); // filter by version
         String sVDTypeEnum = (String) req.getParameter("typeEnum"); // filter by value domain type enumerated
         String sVDTypeNonEnum = (String) req.getParameter("typeNonEnum"); // filter by value domain type non
                                                                             // enumerated
         String sVDTypeRef = (String) req.getParameter("typeEnumRef"); // filter by value domain type enumerated by
                                                                         // reference
         String sRegStatus = (String) req.getParameter("listRegStatus"); // filter by registration status
         @SuppressWarnings("unused") String sStatus = "";
         String sCreFrom = "", sCreTo = "", sModFrom = "", sModTo = "", sCre = "", sMod = "";
         if (actType.equals("ChangeSearchIn"))
         {
             sCreFrom = (String) req.getParameter("createdFrom"); // filter by createdFrom
             sCreTo = (String) req.getParameter("createdTo"); // filter by createdTo
             sModFrom = (String) req.getParameter("modifiedFrom"); // filter by modifiedFrom
             sModTo = (String) req.getParameter("modifiedTo"); // filter by modifiedTo
             sCre = (String) req.getParameter("creator"); // filter by creator
             sMod = (String) req.getParameter("modifier"); // filter by modifier
         }
         // set the session attributes send the page back to refresh for simple filter.
         if (menuAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "creKeyword", sKeyword); // keep the old context criteria
             DataManager.setAttribute(session, "creProtoID", sProtoID); // keep the old protocol id criteria
             DataManager.setAttribute(session, "creContext", sContext); // keep the old context criteria
             req.setAttribute("creContextBlocks", sContext);
             DataManager.setAttribute(session, "creContextUse", sContextUse); // store contextUse in the session
             DataManager.setAttribute(session, "creVersion", sVersion); // store version in the session
             DataManager.setAttribute(session, "creVDTypeEnum", sVDTypeEnum); // store VDType Enum in the session
             DataManager.setAttribute(session, "creVDTypeNonEnum", sVDTypeNonEnum); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "creVDTypeRef", sVDTypeRef); // store VDType Ref in the session
             DataManager.setAttribute(session, "creRegStatus", sRegStatus); // store regstatus in the session
             DataManager.setAttribute(session, "creCreatedFrom", sCreFrom); // empty the date attributes
             DataManager.setAttribute(session, "creCreatedTo", sCreTo); // empty the date attributes
             DataManager.setAttribute(session, "creModifiedFrom", sModFrom); // empty the date attributes
             DataManager.setAttribute(session, "creModifiedTo", sModTo); // empty the date attributes
             DataManager.setAttribute(session, "creCreator", sCre); // empty the creator attributes
             DataManager.setAttribute(session, "creModifier", sMod); // empty the modifier attributes
             DataManager.setAttribute(session, "creDocTyes", vDocType);
             sSearchAC = (String) session.getAttribute("creSearchAC");
             sStatus = getACSearch.getMultiReqValues(sSearchAC, "searchForCreate", "Context");
             sStatus = getACSearch.getStatusValues(req, res, sSearchAC, "searchForCreate", false); // to get a string
                                                                                                     // from multiselect
                                                                                                     // list
         }
         // set session the attribute send the page back to refresh for advanced filter.
         else
         {
             DataManager.setAttribute(session, "serKeyword", sKeyword); // keep the old criteria
             DataManager.setAttribute(session, "serProtoID", sProtoID); // keep the old protocol id criteria
             DataManager.setAttribute(session, "LastAppendWord", sKeyword);
             DataManager.setAttribute(session, "serContext", sContext); // keep the old context criteria
             DataManager.setAttribute(session, "serContextUse", sContextUse); // store contextUse in the session
             DataManager.setAttribute(session, "serVersion", sVersion); // store version in the session
             DataManager.setAttribute(session, "serVDTypeEnum", sVDTypeEnum); // store VDType Enum in the session
             DataManager.setAttribute(session, "serVDTypeNonEnum", sVDTypeNonEnum); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "serVDTypeRef", sVDTypeRef); // store VDType Ref in the session
             DataManager.setAttribute(session, "serRegStatus", sRegStatus); // store regstatus in the session
             DataManager.setAttribute(session, "serCreatedFrom", sCreFrom); // empty the date attributes
             DataManager.setAttribute(session, "serCreatedTo", sCreTo); // empty the date attributes
             DataManager.setAttribute(session, "serModifiedFrom", sModFrom); // empty the date attributes
             DataManager.setAttribute(session, "serModifiedTo", sModTo); // empty the date attributes
             DataManager.setAttribute(session, "serCreator", sCre); // empty the creator attributes
             DataManager.setAttribute(session, "serModifier", sMod); // empty the modifier attributes
             DataManager.setAttribute(session, "serDocTyes", vDocType); // empty doctype list
             sSearchAC = (String) session.getAttribute("searchAC");
             sStatus = getACSearch.getMultiReqValues(sSearchAC, "MainSearch", "Context");
             sStatus = getACSearch.getStatusValues(req, res, sSearchAC, "MainSearch", false); // to get a string from
                                                                                                 // multiselect list
         }
     }
 
     /**
      * To refresh the page when the search For changed from drop down list. Called from 'doGetACSearchActions' method
      * modifies the session attribute 'selectedAttr' or 'creSelectedAttr' according to what is selected. forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sOrigin
      *
      * @throws Exception
      */
     public void doRefreshPageOnSearchFor(HttpServletRequest req, HttpServletResponse res, String sOrigin)
                     throws Exception
     {
         HttpSession session = req.getSession();
         // clearSessionAttributes(req, res);
         // get the search for parameter from the request
         String sSearchAC = (String) req.getParameter("listSearchFor");
         String sSearchIn = "longName";
         // call the method to get attribute list for the selected AC
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         getCompAttrList(req, res, sSearchAC, menuAction);
         // change the selected attributes according to what is selected
         Vector vSelectedAttr = new Vector();
         vSelectedAttr = getDefaultAttr(sSearchAC, sSearchIn);
         this.getDefaultFilterAtt(req, res); // get the default filter by attributes
         if (!menuAction.equals("searchForCreate"))
         {
             // Store the session attributes
             DataManager.setAttribute(session, "selectedAttr", vSelectedAttr);
             DataManager.setAttribute(session, "searchAC", sSearchAC);
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
             // new searchFor, reset the stacks
             clearSessionAttributes(req, res);
             if (sSearchAC.equals("ConceptClass"))
             {
                 Vector<String> vStatus = new Vector<String>();
                 vStatus.addElement("RELEASED");
                 DataManager.setAttribute(session, "serStatus", vStatus);
             }
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
         else
         {
             // Store the session attributes
             DataManager.setAttribute(session, "creSelectedAttr", vSelectedAttr);
             DataManager.setAttribute(session, "creSearchAC", sSearchAC);
             DataManager.setAttribute(session, "vACSearch", new Vector());
             // do the basic search for conceptual domain
             if (sSearchAC.equals("ConceptualDomain")) // || sSearchAC.equals("ValueMeaning"))
             {
                 GetACSearch getACSearch = new GetACSearch(req, res, this);
                 getACSearch.getACSearchForCreate(req, res, true);
             }
             if (sSearchAC.equals("ValueMeaning"))
             {
                 VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
                 String sCDid = "";
                 if (vd != null)
                     sCDid = vd.getVD_CD_IDSEQ();
                 DataManager.setAttribute(session, "creSelectedCD", sCDid);
             }
             // forward the page with crfresults if it is crf value search, otherwise searchResults
             if (sOrigin.equals("CRFValue"))
                 ForwardJSP(req, res, "/CRFValueSearchWindow.jsp");
             else
                 ForwardJSP(req, res, "/OpenSearchWindow.jsp");
         }
     }
 
     /**
      * To search results by clicking on the column heading. Called from 'service' method where reqType is 'searchEVS'
      * forwards page 'EVSSearchPage.jsp'.
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
         //HttpSession session = req.getSession();
         //Vector vResults = new Vector();
         try
         {
             GetACSearch getACSearch = new GetACSearch(req, res, this);
             getACSearch.doSearchEVS(req, res);
         }
         catch (Exception e)
         {
             // System.err.println("EVS Search : " + e);
             this.logger.fatal("ERROR - EVS Search : " + e.toString(), e);
         }
         ForwardJSP(req, res, "/EVSSearchPage.jsp");
         // ForwardJSP(req, res, "/OpenSearchWindow.jsp");
     }
 
     /**
      * To search a component or to display more attributes after the serach. Called from 'service' method where reqType
      * is 'searchACs' calls 'getACSearch.getACKeywordResult' method when the action is a new search. calls
      * 'getACSearch.getACShowResult' method when the action is a display attributes. forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doBlockSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         Boolean approvedRep = new Boolean(false);
         String actType = (String) req.getParameter("actSelect");
         if (actType == null)
             actType = "";
         String sSearchFor = (String) req.getParameter("listSearchFor");
         String sKeyword = (String) req.getParameter("keyword");
         String dtsVocab = req.getParameter("listContextFilterVocab");
         // String sSearchInEVS = "";
        // String sUISearchType = "";
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         String sMetaSource = req.getParameter("listContextFilterSource");
         if (sMetaSource == null)
             sMetaSource = "";
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         DataManager.setAttribute(session, "creSearchAC", sSearchFor);
         DataManager.setAttribute(session, "dtsVocab", dtsVocab);
         DataManager.setAttribute(session, "creKeyword", sKeyword);
         getCompAttrList(req, res, sSearchFor, "searchForCreate");
         // System.out.println(sSearchFor + " block actions " + actType);
         if ((menuAction != null) && (actType != null))
         {
             if (actType.equals("Search"))
             {
             	               
             	session.setAttribute("ApprovedRepTerm", approvedRep);
             	getACSearch.getACSearchForCreate(req, res, false);
                 ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("Attribute"))
             {
             	getACSearch.getACShowResult(req, res, "Attribute");
                 ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("FirstSearch"))
             {
                 this.getDefaultBlockAttr(req, res, "NCI_Thesaurus"); // "Thesaurus/Metathesaurus");
                 
                 //to display the pre-populated table with the list of approved Rep Terms.
                 if(sSearchFor.equals("RepTerm"))
                 {
                 System.out.println(req.getParameter("nonEVSRepTermSearch"));	
                  this.getRepTermDefaultContext();	
                  approvedRep=true;	
                  session.setAttribute("ApprovedRepTerm", approvedRep);
               // get default attributes
                  Vector vSel = (Vector) session.getAttribute("creSelectedAttr");
                  Vector vSelClone = (Vector) vSel.clone();
                  vSelClone.remove("Public ID");
                  vSelClone.remove("EVS Identifier");
                  vSelClone.remove("Workflow Status");
                  vSelClone.remove("Semantic Type");
                  vSelClone.remove("Context");
                  vSelClone.remove("Vocabulary");
                  vSelClone.remove("caDSR Component");
                  DataManager.setAttribute(session, "creSelectedAttr", vSelClone);
                  getApprovedRepTerm();
                  
                 }
                 ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("OpenTreeToConcept") || actType.equals("OpenTreeToParentConcept")
                             || actType.equals("term") || actType.equals("tree"))
             {
                this.doEVSSearchActions(actType, req, res);
             }
             else if (actType.equals("doVocabChange"))
             {
                this.getDefaultBlockAttr(req, res, dtsVocab);
                 req.setAttribute("UISearchType", "term");
                 ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("nonEVS"))
                 ForwardJSP(req, res, "/NonEVSSearchPage.jsp");            
         }
         else
             ForwardJSP(req, res, "/ErrorPage.jsp");
     }
 
     /**
      * To search a component or to display more attributes after the serach. Called from 'service' method where reqType
      * is 'searchACs' calls 'getACSearch.getACKeywordResult' method when the action is a new search. calls
      * 'getACSearch.getACShowResult' method when the action is a display attributes. forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doQualifierSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String actType = (String) req.getParameter("actSelect");
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         if ((menuAction != null) && (actType != null))
         {
             if (actType.equals("Search"))
             {
                 if (menuAction.equals("searchForCreate"))
                 {
                     getACSearch.getACSearchForCreate(req, res, false);
                     ForwardJSP(req, res, "/OpenSearchWindowQualifiers.jsp");
                 }
                 else
                 {
                     getACSearch.getACKeywordResult(req, res);
                     ForwardJSP(req, res, "/SearchResultsPage2.jsp");
                 }
             }
             else if (actType.equals("Attribute"))
             {
                 getACSearch.getACShowResult(req, res, "Attribute");
                 if (menuAction.equals("searchForCreate"))
                     ForwardJSP(req, res, "/OpenSearchWindowQualifiers.jsp");
                 else
                     ForwardJSP(req, res, "/SearchResultsPage2.jsp");
             }
         }
         else
             ForwardJSP(req, res, "/ErrorPage.jsp");
     }
 
     /**
      * Sets a session attribute for a Building Block search. Called from 'service' method where reqType is 'newSearchBB'
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doNewSearchBB(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
         if (m_DEC == null)
             m_DEC = new DEC_Bean();
     //    EVS_Bean m_OC = new EVS_Bean();
     //    EVS_Bean m_PC = new EVS_Bean();
     //    EVS_Bean m_OCQ = new EVS_Bean();
     //    EVS_Bean m_PCQ = new EVS_Bean();
       //  GetACService getAC = new GetACService(req, res, this);
         m_setAC.setDECValueFromPage(req, res, m_DEC);
         DataManager.setAttribute(session, "m_DEC", m_DEC);
         String searchComp = (String) req.getParameter("searchComp");
         if (searchComp.equals("ObjectClass"))
             DataManager.setAttribute(session, "creSearchAC", "ObjectClass");
         else if (searchComp.equals("Property"))
             DataManager.setAttribute(session, "creSearchAC", "Property");
         else if (searchComp.equals("ObjectQualifier"))
             DataManager.setAttribute(session, "creSearchAC", "ObjectQualifier");
         else if (searchComp.equals("PropertyQualifier"))
             DataManager.setAttribute(session, "creSearchAC", "PropertyQualifier");
         ForwardJSP(req, res, "/CreateDECPage.jsp");
     }
 
     /**
      * to get reference documents for the selected ac and doc type called when the reference docuemnts window opened
      * first time and calls 'getAC.getReferenceDocuments' forwards page back to reference documents
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doRefDocSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         GetACSearch getAC = new GetACSearch(req, res, this);
         String acID = req.getParameter("acID");
         String itemType = req.getParameter("itemType");
         @SuppressWarnings("unused") Vector vRef = getAC.doRefDocSearch(acID, itemType, "open");
         ForwardJSP(req, res, "/ReferenceDocumentWindow.jsp");
     }
 
     /**
      * to get alternate names for the selected ac and doc type called when the alternate names window opened first time
      * and calls 'getAC.getAlternateNames' forwards page back to alternate name window jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doAltNameSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         GetACSearch getAC = new GetACSearch(req, res, this);
         String acID = req.getParameter("acID");
         String CD_ID = req.getParameter("CD_ID");
         if (CD_ID == null)
             CD_ID = "";
         String itemType = req.getParameter("itemType");
         if (itemType != null && itemType.equals("ALL"))
             itemType = "";
         @SuppressWarnings("unused") Vector vAlt = getAC.doAltNameSearch(acID, itemType, CD_ID, "other", "open");
         ForwardJSP(req, res, "/AlternateNameWindow.jsp");
     }
 
     /**
      * to get Permissible Values for the selected ac called when the permissible value window opened first time and
      * calls 'getAC.doPVACSearch' forwards page back to Permissible Value window jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doPermValueSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
       //  GetACSearch getAC = new GetACSearch(req, res, this);
         String acID = req.getParameter("acID");
         String acName = req.getParameter("itemType"); // ac name for pv
         if (acName != null && acName.equals("ALL"))
             acName = "-";
         String sConteIdseq = (String) req.getParameter("sConteIdseq");
         if (sConteIdseq == null)
             sConteIdseq = "";
         PVServlet pvser = new PVServlet(req, res, this);
         pvser.searchVersionPV(null, 0, acID, acName);
         ForwardJSP(req, res, "/PermissibleValueWindow.jsp");
     }
 
     /**
      * to get Derived DE info and components called when the DerivedDEWindow opened first time and calls
      * 'getAC.getDDEInfo' forwards page back to DerivedDEWindow jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private void doDDEDetailsActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = (HttpSession) req.getSession();
         GetACSearch getAC = new GetACSearch(req, res, this);
         String acID = req.getParameter("acID");
         String acName = req.getParameter("acName"); // de name for DDE
         String searchType = req.getParameter("itemType"); // dde type
         // split acID into list if more then one existed
         if (searchType != null && searchType.equals("Component")) // (acID.indexOf(',') > 0)
         {
             String[] ddes = acID.split(",");
             Vector vDDEs = new Vector();
             for (int i = 0; i < ddes.length; i++)
             {
                 String sAC = ddes[i];
                 sAC = sAC.trim();
                 getAC.getDDEInfo(sAC); // call api
                 DDE_Bean dde = (DDE_Bean) session.getAttribute("DerivedDE"); // get it from teh session
                 if (dde != null)
                     vDDEs.addElement(dde); // store it in the vector
             }
             // store it in the session
             req.setAttribute("AllDerivedDE", vDDEs);
         }
         else
             // only one
             getAC.getDDEInfo(acID);
         req.setAttribute("ACName", acName);
         ForwardJSP(req, res, "/DerivedDEWindow.jsp");
     }
 
     /**
      * display all the concepts for the selected ac from search results page
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @throws Exception
      */
     private void doConClassSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         GetACSearch getAC = new GetACSearch(req, res, this);
         String acID = req.getParameter("acID"); // dec id
         String ac2ID = req.getParameter("ac2ID"); // vd id
         //String acType = req.getParameter("acType"); // actype to search
         String acName = req.getParameter("acName"); // ac name for pv
         // call the api to return concept attributes according to ac type and ac idseq
         Vector<EVS_Bean> conList = new Vector<EVS_Bean>();
         conList = getAC.do_ConceptSearch("", "", "", "", "", acID, ac2ID, conList);
         req.setAttribute("ConceptClassList", conList);
         req.setAttribute("ACName", acName);
         // store them in request parameter to display and forward the page
         ForwardJSP(req, res, "/ConceptClassDetailWindow.jsp");
     }
 
     /**
      * display conceptual for the selected vm from the search results.
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @throws Exception
      */
     private void doConDomainSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         GetACSearch getAC = new GetACSearch(req, res, this);
         String sVM = req.getParameter("acName"); // ac name for pv
         // call the api to return concept attributes according to ac type and ac idseq
         Vector cdList = new Vector();
         cdList = getAC.doCDSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", sVM, cdList); // get
                                                                                                                     // the
                                                                                                                     // list
                                                                                                                     // of
                                                                                                                     // Conceptual
                                                                                                                     // Domains
         req.setAttribute("ConDomainList", cdList);
         req.setAttribute("VMName", sVM);
         // store them in request parameter to display and forward the page
         ForwardJSP(req, res, "/ConDomainDetailWindow.jsp");
     }
 
     /**
      * contact action from create and edit ac pages to either remove the selected contact or store the modified contact
      * back in the ac bean
      *
      * @param req
      *            HttpServletRequest
      * @param sAct
      *            String AC contact update action from create and edit pages
      * @return Hashtable of contact name and contact bean object
      */
     @SuppressWarnings("unchecked")
     private Hashtable<String, AC_CONTACT_Bean> doContactACUpdates(HttpServletRequest req, String sAct)
     {
         HttpSession session = req.getSession();
         Hashtable<String, AC_CONTACT_Bean> hConts = (Hashtable) session.getAttribute("AllContacts");
         if (hConts == null)
             hConts = new Hashtable<String, AC_CONTACT_Bean>();
         try
         {
             String sCont = "";
             AC_CONTACT_Bean accBean = new AC_CONTACT_Bean();
             if (sAct.equals("removeContact"))
             {
                 sCont = (String) req.getParameter("selContact");
                 if (sCont != null && !sCont.equals("") && hConts.containsKey(sCont))
                     accBean = accBean.copyContacts((AC_CONTACT_Bean) hConts.get(sCont));
                 accBean.setACC_SUBMIT_ACTION("DEL");
             }
             else
             {
                 sCont = (String) session.getAttribute("selContactKey");
                 accBean = accBean.copyContacts((AC_CONTACT_Bean) session.getAttribute("selACContact"));
                 if (accBean == null)
                     accBean = new AC_CONTACT_Bean();
                 // new contact
                 if (sCont == null || sCont.equals(""))
                 {
                     Hashtable hOrg = (Hashtable) session.getAttribute("Organizations");
                     Hashtable hPer = (Hashtable) session.getAttribute("Persons");
                     sCont = accBean.getPERSON_IDSEQ();
                     if (sCont != null && !sCont.equals("") && hPer.containsKey(sCont))
                         sCont = (String) hPer.get(sCont);
                     else
                     {
                         sCont = accBean.getORG_IDSEQ();
                         if (sCont != null && !sCont.equals("") && hOrg.containsKey(sCont))
                             sCont = (String) hOrg.get(sCont);
                     }
                     accBean.setACC_SUBMIT_ACTION("INS");
                 }
                 else
                     accBean.setACC_SUBMIT_ACTION("UPD");
             }
             // put it back in teh hash table
             if (sCont != null && !sCont.equals(""))
                 hConts.put(sCont, accBean);
         }
         catch (Exception e)
         {
             logger.fatal("Error - doContactACUpdates : " + e.toString(), e);
         }
         DataManager.setAttribute(session, "selContactKey", ""); // remove the attributes
         DataManager.setAttribute(session, "selACContact", null);
         // session.removeAttribute("selContactKey"); //remove the attributes
         // session.removeAttribute("selACContact");
         return hConts;
     }
 
     /**
      * Contact edit action including open, view, edit comm, edit addr and change organization etc
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private void doContactEditActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // get all the contacts from the session attribute set in create/edit page of the ac
         Hashtable hConts = (Hashtable) session.getAttribute("AllContacts");
         if (hConts == null)
             hConts = new Hashtable();
         // get the page action
         String sPgAct = (String) req.getParameter("pageAction");
         if (sPgAct != null && !sPgAct.equals(""))
         {
             try
             {
                 // get request and session attributes
                 String sContAct = (String) req.getParameter("contAction");
                 if (sContAct == null || sContAct.equals(""))
                     sContAct = "new";
                 AC_CONTACT_Bean accBean = (AC_CONTACT_Bean) session.getAttribute("selACContact");
                 if (accBean == null)
                     accBean = new AC_CONTACT_Bean();
                 // if page action contact action is edit pull out contact bean from the all contacts for selected contxt
                 if (sPgAct.equals("openPage")) // && sContAct.equals("view"))
                 {
                     if (sContAct.equals("view")) // edit contact
                     {
                         String selCont = (String) req.getParameter("selContact");
                         if (selCont != null && hConts.containsKey(selCont))
                         {
                             accBean = accBean.copyContacts((AC_CONTACT_Bean) hConts.get(selCont));
                             DataManager.setAttribute(session, "selContactKey", selCont);
                         }
                         // System.out.println(sContAct + " contat sele " + selCont + " contains " +
                         // hConts.containsKey(selCont));
                     }
                     else
                     // new contact
                     {
                         accBean = new AC_CONTACT_Bean();
                         DataManager.setAttribute(session, "selContactKey", "");
                     }
                 }
                 else
                 // if (!sPgAct.equals("openPage")) //if not opening the page store the changed data in teh bean
                 {
                     String conOrder = (String) req.getParameter("rank");
                     if (conOrder != null && !conOrder.equals(""))
                         accBean.setRANK_ORDER(conOrder);
                     String conPer = (String) req.getParameter("selPer");
                     if (conPer == null)
                         conPer = "";
                     accBean.setPERSON_IDSEQ(conPer);
                     String conOrg = (String) req.getParameter("selOrg");
                     if (conOrg == null)
                         conOrg = "";
                     accBean.setORG_IDSEQ(conOrg);
                     String conRole = (String) req.getParameter("selRole");
                     if (conRole != null && !conRole.equals(""))
                         accBean.setCONTACT_ROLE(conRole);
                 }
                 // change the radio button action
                 if (sPgAct.equals("changeType"))
                 {
                     String sType = (String) req.getParameter("rType");
                     if (sType != null && !sType.equals(""))
                         req.setAttribute("TypeSelected", sType);
                 }
                 // get the comm and addr info for the selected contact
                 else if (sPgAct.equals("changeContact"))
                 {
                     String perID = accBean.getPERSON_IDSEQ();
                     String orgID = accBean.getORG_IDSEQ();
                     if ((perID != null && !perID.equals("")) || (orgID != null && !orgID.equals("")))
                     {
                         GetACSearch getAC = new GetACSearch(req, res, this);
                         Vector<AC_COMM_Bean> vComm = getAC.getContactComm("", orgID, perID);
                         if (vComm == null)
                             vComm = new Vector<AC_COMM_Bean>();
                         accBean.setACC_COMM_List(vComm);
                         Vector<AC_ADDR_Bean> vAddr = getAC.getContactAddr("", orgID, perID);
                         if (vAddr == null)
                             vAddr = new Vector<AC_ADDR_Bean>();
                         accBean.setACC_ADDR_List(vAddr);
                     }
                 }
                 // adding comm attributes to com bean
                 else if (sPgAct.indexOf("Comm") > -1)
                     accBean = this.doContCommAction(req, accBean, sPgAct);
                 // adding comm attributes to com bean
                 else if (sPgAct.indexOf("Addr") > -1)
                     accBean = this.doContAddrAction(req, accBean, sPgAct);
                 // store contact changes with all contacts as new or update
                 else if (sPgAct.equals("updContact"))
                 {
                     sContAct = "doContactUpd";
                     String sMsg = "Contact Information updated successfully, \\n"
                                     + "but will not be associated to the Administered Component (AC) \\n"
                                     + "or written to the database until the AC has been successfully submitted.";
                     DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, sMsg);
                 }
                 // store the acc bean in teh request (sends back empty if new action)
                 DataManager.setAttribute(session, "selACContact", accBean);
                 req.setAttribute("ContAction", sContAct);
             }
             catch (Exception e)
             {
                 logger.fatal("Error - doContactEditActions : " + e.toString(), e);
             }
         }
         ForwardJSP(req, res, "/EditACContact.jsp");
     }
 
     /**
      * Contact communication change actions
      *
      * @param req
      *            HttpServletRequest
      * @param ACBean
      *            AC_CONTACT_Bean
      * @param sAct
      *            String communication action
      * @return AC_CONTACT_Bean
      */
     private AC_CONTACT_Bean doContCommAction(HttpServletRequest req, AC_CONTACT_Bean ACBean, String sAct)
     {
         try
         {
             Vector<AC_COMM_Bean> vComm = ACBean.getACC_COMM_List();
             if (vComm == null)
                 vComm = new Vector<AC_COMM_Bean>();
             AC_COMM_Bean commB = new AC_COMM_Bean();
             int selInd = -1;
             for (int j = 0; j < vComm.size(); j++) // loop through existing lists
             {
                 String rSel = (String) req.getParameter("com" + j);
                 // check if this id is selected
                 if (rSel != null)
                 {
                     commB = commB.copyComms((AC_COMM_Bean) vComm.elementAt(j));
                     selInd = j;
                     break;
                 }
             }
             // System.out.println(selInd + " commAct " + sAct);
             // if editComm action set comm bean in teh request and return back
             if (sAct.equals("editComm") && selInd > -1)
             {
                 req.setAttribute("CommForEdit", commB);
                 req.setAttribute("CommCheckForEdit", "com" + selInd);
                 return ACBean;
             }
             // handle remove or add selection actions
             if (commB == null)
                 commB = new AC_COMM_Bean();
             if (sAct.equals("removeComm")) // remove the item and exit
                 commB.setCOMM_SUBMIT_ACTION("DEL");
             else if (sAct.equals("addComm")) // udpate or adding new
             {
                 // get the attributes from the page
                 String cType = (String) req.getParameter("selCommType");
                 if (cType == null)
                     cType = "";
                 String cOrd = (String) req.getParameter("comOrder");
                 if (cOrd == null)
                     cOrd = "";
                 String cCyber = (String) req.getParameter("comCyber");
                 if (cCyber == null)
                     cCyber = "";
                 String sCommName = cType + "_" + cOrd + "_" + cCyber;
                 // check these attributes already exist in the list
                 boolean wasDeleted = false;
                 for (int k = 0; k < vComm.size(); k++)
                 {
                     AC_COMM_Bean exComm = (AC_COMM_Bean) vComm.elementAt(k);
                     String ct = exComm.getCTL_NAME();
                     if (ct == null)
                         ct = "";
                     String cc = exComm.getCYBER_ADDR();
                     if (cc == null)
                         cc = "";
                     String co = exComm.getRANK_ORDER(); // leave this for now till confirmed
                     if (co == null)
                         co = "";
                     String exCommName = cType + "_" + cOrd + "_" + cCyber;
                     if (sCommName.equals(exCommName))
                     {
                         // allow to create duplicates but undelete if it was deleted
                         String exSubmit = commB.getCOMM_SUBMIT_ACTION();
                         if (exSubmit != null && exSubmit.equals("DEL"))
                         {
                             wasDeleted = true;
                             commB = commB.copyComms((AC_COMM_Bean) vComm.elementAt(k));
                             if (commB.getAC_COMM_IDSEQ() == null || commB.getAC_COMM_IDSEQ().equals(exCommName))
                                 commB.setCOMM_SUBMIT_ACTION("INS");
                             else
                                 commB.setCOMM_SUBMIT_ACTION("UPD");
                             selInd = k; // reset the index
                         }
                         break;
                     }
                 }
                 // update or add new attributes if was not deleted
                 if (!wasDeleted)
                 {
                     commB.setCTL_NAME(cType);
                     commB.setRANK_ORDER(cOrd);
                     commB.setCYBER_ADDR(cCyber);
                     if (selInd > -1)
                         commB.setCOMM_SUBMIT_ACTION("UPD");
                     else
                         commB.setCOMM_SUBMIT_ACTION("INS");
                 }
             }
             // set the vector
             if (selInd > -1)
                 vComm.setElementAt(commB, selInd);
             else
                 vComm.addElement(commB);
             ACBean.setACC_COMM_List(vComm); // set the bean
         }
         catch (Exception e)
         {
             logger.fatal("Error - doContCommAction : " + e.toString(), e);
         }
         return ACBean;
     }
 
     /**
      * Contact address change action
      *
      * @param req
      *            HttpServletRequest
      * @param ACBean
      *            AC_CONTACT_Bean
      * @param sAct
      *            String address edit action
      * @return AC_CONTACT_Bean
      */
     private AC_CONTACT_Bean doContAddrAction(HttpServletRequest req, AC_CONTACT_Bean ACBean, String sAct)
     {
         try
         {
             Vector<AC_ADDR_Bean> vAddr = ACBean.getACC_ADDR_List();
             if (vAddr == null)
                 vAddr = new Vector<AC_ADDR_Bean>();
             AC_ADDR_Bean addrB = new AC_ADDR_Bean();
             int selInd = -1;
             for (int j = 0; j < vAddr.size(); j++) // loop through existing lists
             {
                 String rSel = (String) req.getParameter("addr" + j);
                 // check if this id is selected
                 if (rSel != null)
                 {
                     addrB = addrB.copyAddress((AC_ADDR_Bean) vAddr.elementAt(j));
                     selInd = j;
                     break;
                 }
             }
             // System.out.println(selInd + " addrAct " + sAct);
             // if editAddr action set addr bean in teh request and return back
             if (sAct.equals("editAddr") && selInd > -1)
             {
                 req.setAttribute("AddrForEdit", addrB);
                 req.setAttribute("AddrCheckForEdit", "addr" + selInd);
                 return ACBean;
             }
             // handle remove or add selection actions
             if (addrB == null)
                 addrB = new AC_ADDR_Bean();
             if (sAct.equals("removeAddr")) // remove the item and exit
                 addrB.setADDR_SUBMIT_ACTION("DEL");
             else if (sAct.equals("addAddr")) // udpate or adding new
             {
                 // get the attributes from the page
                 String aType = (String) req.getParameter("selAddrType");
                 if (aType == null)
                     aType = "";
                 String aOrd = (String) req.getParameter("txtPrimOrder");
                 if (aOrd == null)
                     aOrd = "";
                 String aAddr1 = (String) req.getParameter("txtAddr1");
                 if (aAddr1 == null)
                     aAddr1 = "";
                 String aAddr2 = (String) req.getParameter("txtAddr2");
                 if (aAddr2 == null)
                     aAddr2 = "";
                 String aCity = (String) req.getParameter("txtCity");
                 if (aCity == null)
                     aCity = "";
                 String aState = (String) req.getParameter("txtState");
                 if (aState == null)
                     aState = "";
                 String aCntry = (String) req.getParameter("txtCntry");
                 if (aCntry == null)
                     aCntry = "";
                 String aPost = (String) req.getParameter("txtPost");
                 if (aPost == null)
                     aPost = "";
                 String selAddrName = aType + "_" + aOrd + "_" + aAddr1 + "_" + aAddr2 + "_" + aCity + "_" + aState
                                 + "_" + aCntry + "_" + aPost;
                 // check these attributes already exist in the list
                 boolean wasDeleted = false;
                 for (int k = 0; k < vAddr.size(); k++)
                 {
                     AC_ADDR_Bean exAddr = (AC_ADDR_Bean) vAddr.elementAt(k);
                     String at = exAddr.getATL_NAME();
                     if (at == null)
                         at = "";
                     String ao = exAddr.getRANK_ORDER();
                     if (ao == null)
                         ao = "";
                     String aA1 = exAddr.getADDR_LINE1();
                     if (aA1 == null)
                         aA1 = "";
                     String aA2 = exAddr.getADDR_LINE2();
                     if (aA2 == null)
                         aA2 = "";
                     String aCy = exAddr.getCITY();
                     if (aCy == null)
                         aCy = "";
                     String aS = exAddr.getSTATE_PROV();
                     if (aS == null)
                         aS = "";
                     String aCny = exAddr.getCOUNTRY();
                     if (aCny == null)
                         aCny = "";
                     String aP = exAddr.getPOSTAL_CODE();
                     if (aP == null)
                         aP = "";
                     String exAddrName = at + "_" + ao + "_" + aA1 + "_" + aA2 + "_" + aCy + "_" + aS + "_" + aCny + "_"
                                     + aP;
                     // compare the two to check if exists
                     if (selAddrName.equals(exAddrName))
                     {
                         // allow to create duplicates but undelete if it was deleted
                         String exSubmit = addrB.getADDR_SUBMIT_ACTION();
                         if (exSubmit != null && exSubmit.equals("DEL"))
                         {
                             wasDeleted = true;
                             addrB = addrB.copyAddress((AC_ADDR_Bean) vAddr.elementAt(k));
                             if (addrB.getAC_ADDR_IDSEQ() == null || addrB.getAC_ADDR_IDSEQ().equals(exAddrName))
                                 addrB.setADDR_SUBMIT_ACTION("INS");
                             else
                                 addrB.setADDR_SUBMIT_ACTION("UPD");
                             selInd = k; // reset the index
                         }
                         break;
                     }
                 }
                 // update or add new attributes if was not deleted
                 if (!wasDeleted)
                 {
                     addrB.setATL_NAME(aType);
                     addrB.setRANK_ORDER(aOrd);
                     addrB.setADDR_LINE1(aAddr1);
                     addrB.setADDR_LINE2(aAddr2);
                     addrB.setCITY(aCity);
                     addrB.setSTATE_PROV(aState);
                     addrB.setCOUNTRY(aCntry);
                     addrB.setPOSTAL_CODE(aPost);
                     if (selInd > -1)
                         addrB.setADDR_SUBMIT_ACTION("UPD");
                     else
                         addrB.setADDR_SUBMIT_ACTION("INS");
                 }
             }
             // set the vector
             if (selInd > -1)
                 vAddr.setElementAt(addrB, selInd);
             else
                 vAddr.addElement(addrB);
             ACBean.setACC_ADDR_List(vAddr); // set the bean
         }
         catch (Exception e)
         {
             logger.fatal("Error - doContAddrAction : " + e.toString(), e);
         }
         return ACBean;
     }
 
     /**
      * to get Protocol CRF for the selected ac called when the ProtoCRF window opened first time and calls
      * 'getAC.doProtoCRFSearch' forwards page back to ProtoCRFwindow jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doProtoCRFSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         GetACSearch getAC = new GetACSearch(req, res, this);
         String acID = req.getParameter("acID");
         String acName = req.getParameter("itemType"); // ac name for proto crf
         if (acName != null && acName.equals("ALL"))
             acName = "-";
         @SuppressWarnings("unused") Integer pvCount = getAC.doProtoCRFSearch(acID, acName);
         ForwardJSP(req, res, "/ProtoCRFWindow.jsp");
     }
 
     /**
      * To do edit/create from template/new version of a component, clear all records or to display only selected rows
      * after the serach. Called from 'service' method where reqType is 'showResult'. calls 'getACSearch.getSelRowToEdit'
      * method when the action is a edit/create from template/new version. if user doesn't have write permission to
      * edit/create new version forwards the page back to SearchResultsPage.jsp with an error message. For edit, forwards
      * the edit page for the selected component. For new Version/create new from template forwards the create page for
      * the selected component. calls 'getACSearch.getACShowResult' method when the action is show only selected rows and
      * forwards JSP 'SearchResultsPage.jsp'. forwards the page 'SearchResultsPage.jsp' with empty result vector if
      * action is clear records.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param ub_
      *
      * @throws Exception
      */
     public void doSearchResultsAction(HttpServletRequest req, HttpServletResponse res, UserBean ub_) throws Exception
     {
         HttpSession session = req.getSession();
         String actType = (String) req.getParameter("actSelected");
         String sSearchAC = (String) session.getAttribute("searchAC"); // get the selected component
         String sAction = (String) req.getParameter("pageAction");
         DataManager.setAttribute(session, "originAction", "");
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // get the sort string parameter
         @SuppressWarnings("unused") String sSortType = "";
         if (actType == null)
             sSortType = (String) req.getParameter("sortType");
         // sort the header click
         if (actType.equals("sort"))
             doSortACActions(req, res);
         // edit selection button from search results page
         else if (actType.equals("Edit"))
             doSearchSelectionAction(req, res);
         // edit selection button from search results page
         else if (actType.equals("BlockEdit"))
             doSearchSelectionBEAction(req, res);
         // open the designate de page
         else if (actType.equals("EditDesignateDE"))
             new DesDEServlet(this, ub_).doAction(req, res, "Open");
         // open Ref Document Upload page
         else if (actType.equals("RefDocumentUpload"))
             this.doRefDocumentUpload(req, res, "Open");
         // store empty result vector in the attribute
         else if (actType.equals("clearRecords"))
         {
             Vector vResult = new Vector();
             DataManager.setAttribute(session, "results", vResult);
             DataManager.setAttribute(session, "vSelRows", vResult);
             DataManager.setAttribute(session, "CheckList", vResult);
             DataManager.setAttribute(session, "AppendAction", "Not Appended");
             req.setAttribute("recsFound", "No ");
             DataManager.setAttribute(session, "serKeyword", "");
             DataManager.setAttribute(session, "serProtoID", "");
             DataManager.setAttribute(session, "LastAppendWord", "");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
         // use permissible value for selected crf value
         else if (actType.equals("usePVforCRFValue"))
         {
             PV_Bean m_PV = new PV_Bean();
             doRefreshPVSearchPage(req, res, m_PV, "Search");
         }
             // get Associate AC
         else if (actType.equals("AssocDEs") || actType.equals("AssocDECs") || actType.equals("AssocVDs"))
             doGetAssociatedAC(req, res, actType, sSearchAC);
         else if (sAction.equals("backFromGetAssociated"))
         {
             DataManager.setAttribute(session, "backFromGetAssociated", "backFromGetAssociated");
             DataManager.setAttribute(session, "CheckList", null);
             DataManager.setAttribute(session, "LastAppendWord", "");
             DataManager.setAttribute(session, "serProtoID", "");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
         else if (!menuAction.equals("searchForCreate") && actType.equals("Monitor"))
             doMonitor(req, res);
         else if (!menuAction.equals("searchForCreate") && actType.equals("UnMonitor"))
             doUnmonitor(req, res);
         else
         { // show selected rows only.
             GetACSearch getACSearch = new GetACSearch(req, res, this);
             getACSearch.getACShowResult(req, res, actType);
             if (menuAction.equals("searchForCreate"))
                 ForwardJSP(req, res, "/OpenSearchWindow.jsp");
             else
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
     }
 
     /**
      * Monitor the user selected items with a Sentinel Alert.
      *
      * @param req
      *            The session request.
      * @param res
      *            The session response.
      */
     private void doMonitor(HttpServletRequest req, HttpServletResponse res)
     {
         // Init main variables.
         HttpSession session = req.getSession();
         String msg = null;
         Vector<String> vCheckList = new Vector<String>();
         while (true)
         {
             // Be sure something was selected by the user.
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
             if (vSRows == null || vSRows.size() == 0)
             {
                 msg = "No items were selected from the Search Results.";
                 break;
             }
             // Get session information.
             UserBean Userbean = new UserBean();
             Userbean = (UserBean) session.getAttribute("Userbean");
             if (Userbean == null)
             {
                 msg = "User session information is missing.";
                 break;
             }
             CallableStatement stmt = null;
             //Connection con = null;
             try
             {
                 // Get the selected items and associate each with the appropriate CSI
                 String user = Userbean.getUsername();
                 user = user.toUpperCase();
                 //con = connectDB(req, res);
                 // Add the selected items to the CSI
                 String csi_idseq = null;
                 int ndx = 0;
                 stmt = m_conn.prepareCall("begin SBREXT_CDE_CURATOR_PKG.ADD_TO_SENTINEL_CS(?,?,?); end;");
                 stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
                 stmt.setString(2, user);
                 try
                 {
                     vCheckList = new Vector<String>();
                     for (ndx = 0; ndx < vSRows.size(); ++ndx)
                     {
                         String temp;
                         String ckName = ("CK" + ndx);
                         temp = req.getParameter(ckName);
                         if (temp != null)
                         {
                             AC_Bean bean = (AC_Bean) vSRows.elementAt(ndx);
                             temp = bean.getIDSEQ();
                             stmt.setString(1, temp);
                             stmt.execute();
                             temp = stmt.getString(3);
                             vCheckList.addElement(ckName);
                             if (temp != null)
                                 csi_idseq = temp;
                         }
                     }
                     DataManager.setAttribute(session, "CheckList", vCheckList); // add the check list in the session.
                 }
                 catch (ClassCastException e)
                 {
                     // This happens when the selected element does not extend the AC_Bean abstract class.
                     csi_idseq = "";
                 }
                 stmt.close();
                 if (csi_idseq == null)
                 {
                     msg = "None of the selected items can be added to your Reserved CSI.";
                 }
                 else if (csi_idseq.length() == 0)
                 {
                     msg = "The selected items are not supported for the Monitor feature.";
                 }
                 else
                 {
                     // Have the Sentinel watch the CSI.
                     DSRAlert sentinel = DSRAlertImpl.factory(m_conn);
                     ndx = sentinel.createAlert(user, csi_idseq);
                     switch (ndx)
                     {
                     case DSRAlert.RC_FAILED:
                         msg = "An error occurred attempting to create the Alert Definition.";
                         break;
                     case DSRAlert.RC_INCOMPATIBLE:
                         msg = "The Sentinel API server does not support this request.";
                         break;
                     case DSRAlert.RC_UNAUTHORIZED:
                         msg = "You are not authorized to create a Sentinel Alert.";
                         break;
                     default:
                         String itemTxt = (vSRows.size() == 1) ? "item is" : "items are";
                         msg = "The selected " + itemTxt + " now monitored by the Alert Definition \""
                                         + sentinel.getAlertName() + "\"";
                         msg = msg.replaceAll("[\"]", "\\\\\"");
                         break;
                     }
                 }
                 //con.close();
             }
             catch (Exception e)
             {
                 msg = "An unexpected exception occurred, please notify the Help Desk. Details have been written to the log.";
                 logger.fatal("cdecurate: doMonitor(): " + e.toString(), e);
                 try
                 {
                     if (stmt != null)
                         stmt.close();
                    // if (con != null)
                     //    con.close();
                 }
                 catch (SQLException ex)
                 {
                 }
             }
             break;
         }
         // Send the message back to the user.
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         getACSearch.getACShowResult2(req, res, "Monitor");
         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, msg);
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * Unmonitor the user selected items with a Sentinel Alert.
      *
      * @param req
      *            The session request.
      * @param res
      *            The session response.
      */
     private void doUnmonitor(HttpServletRequest req, HttpServletResponse res)
     {
         // Init main variables.
         HttpSession session = req.getSession();
         String msg = null;
         Vector<String> vCheckList = new Vector<String>();
         while (true)
         {
             // Be sure something was selected by the user.
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
             if (vSRows == null || vSRows.size() == 0)
             {
                 msg = "No items were selected from the Search Results.";
                 break;
             }
             // Get session information.
             UserBean Userbean = new UserBean();
             Userbean = (UserBean) session.getAttribute("Userbean");
             if (Userbean == null)
             {
                 msg = "User session information is missing.";
                 break;
             }
             // Get list of selected AC's.
             Vector<String> list = new Vector<String>();
             for (int ndx = 0; ndx < vSRows.size(); ++ndx)
             {
                 try
                 {
                     String temp;
                     String ckName = ("CK" + ndx);
                     temp = req.getParameter(ckName);
                     if (temp != null)
                     {
                         AC_Bean bean = (AC_Bean) vSRows.elementAt(ndx);
                         temp = bean.getIDSEQ();
                         list.add(temp);
                         vCheckList.addElement(ckName);
                     }
                 }
                 catch (ClassCastException e)
                 {
                 }
             }
             DataManager.setAttribute(session, "CheckList", vCheckList); // add the check list in the session.
             if (list.size() == 0)
             {
                 msg = "None of the selected AC's were previously Monitored.";
                 break;
             }
             // Update the database - remove the CSI association to the AC's.
             String user = Userbean.getUsername();
             user = user.toUpperCase();
            // Connection con = null;
             for (int ndx = 0; ndx < list.size(); ++ndx)
             {
                 try
                 {
                     //con = connectDB(req, res);
                     String temp = list.elementAt(ndx);
                     CallableStatement stmt = m_conn.prepareCall("begin SBREXT_CDE_CURATOR_PKG.REMOVE_FROM_SENTINEL_CS('"
                                     + temp + "','" + user + "'); END;");
                     stmt.execute();
                     stmt.close();
                    // con.close();
                     msg = "The selected item is no longer monitored by the Alert Definition";
                 }
                 catch (Exception e)
                 {
                     msg = "An unexpected exception occurred, please notify the Help Desk. Details have been written to the log.";
                     logger.fatal("cdecurate: doUnmonitor(): " + e.toString(), e);
                     /*try
                     {
                         //if (con != null && !con.isClosed())
                         //{
                         //    con.close();
                         //}
                     }
                     catch (SQLException ex)
                     {
                         logger.fatal("cdecurate: doUnmonitor() for close : " + ex.toString(), ex);
                     }*/
                 }
             }
             break;
         }
         // Send the message back to the user.
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         getACSearch.getACShowResult2(req, res, "Monitor");
         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, msg);
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * The doRefreshPVSearchPage method forwards crfValue search page with refreshed list updates the quest value bean
      * with searched/created pv data,
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param m_PV
      *            PV_Bean searched/created pv data
      * @param Origin
      *            String origin of the action
      *
      * @throws Exception
      */
     public void doRefreshPVSearchPage(HttpServletRequest req, HttpServletResponse res, PV_Bean m_PV, String Origin)
                     throws Exception
     {
         HttpSession session = req.getSession();
         if (Origin.equals("CreateNew"))
         {
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Value created and inserted successfully.");
             Vector<String> vNewPV = new Vector<String>();
             vNewPV.addElement(m_PV.getPV_PV_IDSEQ());
             vNewPV.addElement(m_PV.getPV_VALUE());
             vNewPV.addElement(m_PV.getPV_SHORT_MEANING());
             req.setAttribute("newPVData", vNewPV);
         }
         // get the selected pv data from the request
         else
         {
             // using designation hidden fields to get the selected value & meanings
             String sPVID = (String) req.getParameter("desName");
             if (sPVID != null)
                 m_PV.setPV_PV_IDSEQ(sPVID);
             String sPValue = (String) req.getParameter("desContext");
             if (sPValue != null)
                 m_PV.setPV_VALUE(sPValue);
             String sPVMean = (String) req.getParameter("desContextID");
             if (sPVMean != null)
                 m_PV.setPV_SHORT_MEANING(sPVMean);
         }
         // forwards the page to regular pv search if not questions
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         if (Origin.equals("CreateNew") && !sMenuAction.equals("Questions"))
             ForwardJSP(req, res, "/OpenSearchWindow.jsp");
         else
         {
             // get the selected crf value and update its attribute with the selected/created pvvalue
             String selCRFValue = (String) session.getAttribute("selCRFValueID");
             if (selCRFValue != null)
             {
                 // get the crf value vector to update
                 Vector vQuestValue = (Vector) session.getAttribute("vQuestValue");
                 if (vQuestValue != null)
                 {
                     for (int i = 0; i < (vQuestValue.size()); i++)
                     {
                         Quest_Value_Bean QuestValueBean = new Quest_Value_Bean();
                         QuestValueBean = (Quest_Value_Bean) vQuestValue.elementAt(i);
                         // update the quest bean with the new value meaning
                         if (QuestValueBean.getQUESTION_VALUE_IDSEQ().equalsIgnoreCase(selCRFValue))
                         {
                             QuestValueBean.setPERM_VALUE_IDSEQ(m_PV.getPV_PV_IDSEQ());
                             QuestValueBean.setPERMISSIBLE_VALUE(m_PV.getPV_VALUE());
                             QuestValueBean.setVALUE_MEANING(m_PV.getPV_SHORT_MEANING());
                             break;
                         }
                     }
                     DataManager.setAttribute(session, "vQuestValue", vQuestValue);
                 }
             }
             ForwardJSP(req, res, "/CRFValueSearchWindow.jsp");
         }
     }
 
     /**
      * gets the selected row from the search result to forward the data. Called from 'doSearchResultsAction' method
      * where actType is 'edit' calls 'getACSearch.getSelRowToEdit' method to get the row bean. if user doesn't have
      * permission to write to the selected context goes back to search page. otherwise forwards to create/edit pages for
      * the selected component.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doSearchSelectionAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // gets the bean for the row selected
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         if (getACSearch.getSelRowToEdit(req, res, "") == false)
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         else
         {
            // String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected
                                                                                                     // menu action
             String sSearchAC = (String) session.getAttribute("searchAC"); // get the selected component
           //  String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
           //  String sOriginAction = (String) session.getAttribute("sOriginAction");
             // call method to handle DE actions.
             if (sSearchAC.equals("DataElement"))
                 this.doSerSelectActDE(req, res);
             else if (sSearchAC.equals("DataElementConcept"))
                 this.doSerSelectActDEC(req, res);
             else if (sSearchAC.equals("ValueDomain"))
                 this.doSerSelectActVD(req, res);
             else if (sSearchAC.equals("Questions"))
             {
                 // DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
                 // get status indicatior from the quest bean
                 Quest_Bean QuestBean = (Quest_Bean) session.getAttribute("m_Quest");
                 String sStatus = QuestBean.getSTATUS_INDICATOR();
                 // forward the page to createDE if new or existing or to edit page if edit
                 if (sStatus.equals("Edit"))
                     ForwardJSP(req, res, "/EditDEPage.jsp");
                 else
                     ForwardJSP(req, res, "/CreateDEPage.jsp");
             }
             else
             {
                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create or Edit page.\n"
                                 + "Please try again.");
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
         }
     }
 
     /**
      * get the definition property from the setting
      *
      * @return the DEDefinition value
      */
     public String getPropertyDefinition()
     {
         return NCICurationServlet.m_settings.getProperty("DEDefinition");
     }
 
     /**
      * does the search selection action for the Data element search forward the page according to the action
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doSerSelectActDE(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected menu
                                                                                                 // action
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         // make sure the menu action is for DE, set it otherwise
         if (sMenuAction.equalsIgnoreCase("NewDECTemplate") || (sMenuAction.equalsIgnoreCase("NewVDTemplate")))
             sMenuAction = "NewDETemplate";
         else if (sMenuAction.equalsIgnoreCase("NewDECVersion") || (sMenuAction.equalsIgnoreCase("NewVDVersion")))
             sMenuAction = "NewDEVersion";
         else if (sMenuAction.equalsIgnoreCase("editDEC") || (sMenuAction.equalsIgnoreCase("editVD")))
             sMenuAction = "editDE";
         // set the menuaction session attribute
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         // forward to create DE page if template or version
         if ((sMenuAction.equals("NewDETemplate")) || (sMenuAction.equals("NewDEVersion")))
         {
             // DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
             DataManager.setAttribute(session, "sCDEAction", "validate");
             ForwardJSP(req, res, "/CreateDEPage.jsp");
             DataManager.setAttribute(session, "originAction", "NewDE");
         }
         // forward to edit DE page if editing
         else if (sMenuAction.equals("editDE") || sMenuAction.equals("nothing"))
         {
             DataManager.setAttribute(session, "originAction", "EditDE");
             ForwardJSP(req, res, "/EditDEPage.jsp");
         }
         else if (sButtonPressed.equals("Search"))
         {
             DataManager.setAttribute(session, "originAction", "EditDE");
             ForwardJSP(req, res, "/EditDEPage.jsp");
         }
         else
         {
             DataManager.setAttribute(session, "originAction", "EditDE");
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create or Edit page.\\n"
                             + "Please try again.");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
     }
 
     /**
      * does the search selection action for the Data element concept search forward the page according to the action
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doSerSelectActDEC(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected menu
                                                                                                 // action
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         // make sure the menu action is for DEC, set it otherwise
         if (sMenuAction.equalsIgnoreCase("NewDETemplate") || (sMenuAction.equalsIgnoreCase("NewVDTemplate")))
             sMenuAction = "NewDECTemplate";
         else if (sMenuAction.equalsIgnoreCase("NewDEVersion") || (sMenuAction.equalsIgnoreCase("NewVDVersion")))
             sMenuAction = "NewDECVersion";
         else if (sMenuAction.equalsIgnoreCase("editDE") || (sMenuAction.equalsIgnoreCase("editVD")))
             sMenuAction = "editDEC";
         // set the menuaction session attribute
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         // forward to create DEC page if template or version
         if ((sMenuAction.equals("NewDECTemplate")) || (sMenuAction.equals("NewDECVersion")))
         {
             DataManager.setAttribute(session, "DECPageAction", "validate");
             ForwardJSP(req, res, "/CreateDECPage.jsp");
             // DataManager.setAttribute(session, "originAction", "NewDEC");
         }
         // forward to edit DEC page if editing
         else if (sMenuAction.equals("editDEC") || sMenuAction.equals("nothing"))
         {
             DataManager.setAttribute(session, "originAction", "EditDEC");
             ForwardJSP(req, res, "/EditDECPage.jsp");
         }
         else if (sButtonPressed.equals("Search"))
         {
             DataManager.setAttribute(session, "originAction", "EditDEC");
             ForwardJSP(req, res, "/EditDECPage.jsp");
         }
         else
         {
             DataManager.setAttribute(session, "originAction", "EditDEC");
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create or Edit page.\\n"
                             + "Please try again.");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
     }
 
     /**
      * does the search selection action for the Value Domain search forward the page according to the action
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doSerSelectActVD(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected menu
                                                                                                 // action
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         // make sure the menu action is for DE, set it otherwise
         if (sMenuAction.equalsIgnoreCase("NewDETemplate") || (sMenuAction.equalsIgnoreCase("NewDECTemplate")))
             sMenuAction = "NewVDTemplate";
         else if (sMenuAction.equalsIgnoreCase("NewDEVersion") || (sMenuAction.equalsIgnoreCase("NewDECVersion")))
             sMenuAction = "NewVDVersion";
         else if (sMenuAction.equalsIgnoreCase("editDE") || (sMenuAction.equalsIgnoreCase("editDEC")))
             sMenuAction = "editVD";
         // set the menuaction session attribute
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         // forward to create VD page if template or version
         if ((sMenuAction.equals("NewVDTemplate")) || (sMenuAction.equals("NewVDVersion")))
         {
             DataManager.setAttribute(session, "VDPageAction", "validate");
             ForwardJSP(req, res, "/CreateVDPage.jsp");
             DataManager.setAttribute(session, "originAction", "NewVD");
         }
         // forward to edit VD page if editing
         else if (sMenuAction.equals("editVD") || sMenuAction.equals("nothing"))
         {
             DataManager.setAttribute(session, "originAction", "EditVD");
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         else if (sButtonPressed.equals("Search"))
         {
             DataManager.setAttribute(session, "originAction", "EditVD");
             ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         else
         {
             DataManager.setAttribute(session, "originAction", "EditVD");
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create or Edit page.\\n"
                             + "Please try again.");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
     }
 
     /**
      * gets the selected row from the search result to forward the data. Called from 'doSearchResultsAction' method
      * where actType is 'edit' calls 'getACSearch.getSelRowToEdit' method to get the row bean. if user doesn't have
      * permission to write to the selected context goes back to search page. otherwise forwards to create/edit pages for
      * the selected component.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doSearchSelectionBEAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // gets the bean for the row selected
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         if (getACSearch.getSelRowToEdit(req, res, "BlockEdit") == false)
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         else
         {
            // String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected
                                                                                                     // menu action
             String sSearchAC = (String) session.getAttribute("searchAC"); // get the selected component
           //  String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
             if (sSearchAC.equals("DataElement"))
             { // open the edit page
                 DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
                 DEBean.setDE_DEC_IDSEQ("");
                 DEBean.setDE_DEC_NAME("");
                 DEBean.setDE_VD_IDSEQ("");
                 DEBean.setDE_VD_NAME("");
                 DataManager.setAttribute(session, "m_DE", DEBean); // users need cs-csi to view
                 DataManager.setAttribute(session, "originAction", "BlockEditDE");
                 DataManager.setAttribute(session, "DEEditAction", "");
                 ForwardJSP(req, res, "/EditDEPage.jsp");
             }
             else if (sSearchAC.equals("DataElementConcept"))
             {
                 DataManager.setAttribute(session, "originAction", "BlockEditDEC");
                 this.clearBuildingBlockSessionAttributes(req, res);
                 ForwardJSP(req, res, "/EditDECPage.jsp");
             }
             else if (sSearchAC.equals("ValueDomain"))
             {
                 DataManager.setAttribute(session, "vRepTerm", null);
                 DataManager.setAttribute(session, "newRepTerm", "");
                 DataManager.setAttribute(session, "originAction", "BlockEditVD");
                 ForwardJSP(req, res, "/EditVDPage.jsp");
             }
             else
             {
                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create or Edit page.\\n"
                                 + "Please try again.");
                 ForwardJSP(req, res, "/SearchResultsPage.jsp");
             }
         }
     }
 
     /**
      * to display the selected elements for block edit, opened from create/edit pages.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doDisplayWindowBEAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "BEDisplay"); // set the menu to BEDisplay to get the
                                                                                 // results properly
         getACSearch.getACShowResult(req, res, "BEDisplayRows");
         DataManager.setAttribute(session, "BEDisplaySubmitted", "true");
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenu); // set the menu back to way it was
         ForwardJSP(req, res, "/OpenBlockEditWindow.jsp");
     }
 
     /**
      * to display the associated DEC for the selected oc or prop.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doDECDetailDisplay(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
        // HttpSession session = req.getSession();
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         String acID = (String) req.getParameter("acID");
         String acName = (String) req.getParameter("acName");
         if (acName == null || acName.equals(""))
             acName = "doneSearch";
         Vector vList = new Vector();
         if (acID != null && !acID.equals(""))
         {
             if (acName != null && acName.equals("ObjectClass"))
                 getACSearch.doDECSearch("", "", "", "", "", "", "", "", "", "", "", "", "", acID, "", "", 0, "", "",
                                 "", "", "", vList);
             if (acName != null && acName.equals("Property"))
                 getACSearch.doDECSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", acID, "", 0, "", "",
                                 "", "", "", vList);
         }
         req.setAttribute("pageAct", acName);
         req.setAttribute("lstDECResult", vList);
         ForwardJSP(req, res, "/DECDetailWindow.jsp");
     }
 
     /**
      * gets the selected row from the search result to forward the data. Called from 'doSearchResultsAction' method
      * where actType is 'AssocDEs', AssocDECs or AssocVDs gets the index and ID/Names from the session attributes to get
      * the row bean. calls 'getACSearch.getAssociatedDESearch', 'getACSearch.getAssociatedDECSearch', or
      * 'getACSearch.getAssociatedVDSearch' method to get search associated results depending actType. calls
      * 'getACSearch.getDEResult', 'getACSearch.getDECResult', or 'getACSearch.getVDResult' method to get final result
      * vector which is stored in the session. resets default attributes and other session attributes forwards to
      * SearchResultsPage to display the search results.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param assocAC
      *            String actType of the search result page.
      * @param sSearchAC
      *            String search type from the drop down list.
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private void doGetAssociatedAC(HttpServletRequest req, HttpServletResponse res, String assocAC, String sSearchAC)
                     throws Exception
     {
         HttpSession session = req.getSession();
         int thisInd = 0;
         // get the searched ID and Name vectors
         Vector vIDs = (Vector) session.getAttribute("SearchID");
         // get the long / names of the selected ac
         Vector vNames = new Vector();
         if (sSearchAC.equals("DataElementConcept") || sSearchAC.equals("ValueDomain")
                         || sSearchAC.equals("ConceptualDomain") || sSearchAC.equals("DataElement"))
         {
             vNames = (Vector) session.getAttribute("SearchLongName");
         }
         // PermissibleValue, ClassSchemeItems, ObjectClass, Property
         else
         {
             vNames = (Vector) session.getAttribute("SearchName");
         }
       //  Vector oldVResult = (Vector) session.getAttribute("results");
         // get the selected row index from the hidden field.
         String sID = "";
         String sName = "";
         // convert the string to integer and to int.
         Integer curInd = new Integer((String) req.getParameter("hiddenSelectedRow"));
         if (curInd != null)
             thisInd = curInd.intValue();
         if (vIDs != null && !vIDs.equals("") && vIDs.size() > 0 && (thisInd < vIDs.size()))
         {
             sID = (String) vIDs.elementAt(thisInd);
             if (vNames != null && vNames.size() > thisInd)
                 sName = (String) vNames.elementAt(thisInd);
         }
         if (sID != null && !sID.equals(""))
         {
             // reset the default attributes
             Vector vSelVector = new Vector();
             String sSearchIn = (String) session.getAttribute("serSearchIn");
             GetACSearch getACSearch = new GetACSearch(req, res, this);
             Vector vResult = new Vector();
             String oldSearch = "";
             String newSearch = "";
            // String retCode = "";
             String pvID = "", cdID = "", deID = "", decID = "", vdID = "", cscsiID = "", ocID = "", propID = "", conID = "";
             // get the search results from the database.
             if (assocAC.equals("AssocDEs"))
             {
                 req.setAttribute("GetAssocSearchAC", "true");
                 // retCode = getACSearch.doAssociatedDESearch(sID, sSearchAC);
                 if (sSearchAC.equals("PermissibleValue"))
                     pvID = sID;
                 else if (sSearchAC.equals("DataElementConcept"))
                     decID = sID;
                 else if (sSearchAC.equals("ValueDomain"))
                     vdID = sID;
                 else if (sSearchAC.equals("ConceptualDomain"))
                     cdID = sID;
                 else if (sSearchAC.equals("ClassSchemeItems"))
                     cscsiID = sID;
                 else if (sSearchAC.equals("ConceptClass"))
                     conID = sID;
                 // do the search only if id is not null
                 Vector vRes = new Vector();
                 if (sID != null && !sID.equals(""))
                     getACSearch.doDESearch("", "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "", "",
                                     "", "", "", "", pvID, vdID, decID, cdID, cscsiID, conID, "", "", vRes);
                 DataManager.setAttribute(session, "vSelRows", vRes);
                 // do attributes after the search so no "two simultaneous request" errors
                 vSelVector = this.getDefaultAttr("DataElement", sSearchIn);
                 DataManager.setAttribute(session, "selectedAttr", vSelVector);
                 getCompAttrList(req, res, "DataElement", "nothing");
                 // if (retCode.equals("0"))
                 getACSearch.getDEResult(req, res, vResult, "");
                 DataManager.setAttribute(session, "searchAC", "DataElement");
                 newSearch = "Data Element";
             }
             else if (assocAC.equals("AssocDECs"))
             {
                 req.setAttribute("GetAssocSearchAC", "true");
                 // retCode = getACSearch.doAssociatedDECSearch(sID, sSearchAC);
                 if (sSearchAC.equals("ObjectClass"))
                     ocID = sID;
                 else if (sSearchAC.equals("Property"))
                     propID = sID;
                 else if (sSearchAC.equals("DataElement"))
                     deID = sID;
                 else if (sSearchAC.equals("ConceptualDomain"))
                     cdID = sID;
                 else if (sSearchAC.equals("ClassSchemeItems"))
                     cscsiID = sID;
                 else if (sSearchAC.equals("ConceptClass"))
                     conID = sID;
                 Vector vRes = new Vector();
                 getACSearch.doDECSearch("", "", "", "", "", "", "", "", "", "", "", "", "", ocID, propID, "", 0, cdID,
                                 deID, cscsiID, conID, "", vRes);
                 DataManager.setAttribute(session, "vSelRows", vRes);
                 // do attributes after the search so no "two simultaneous request" errors
                 vSelVector = this.getDefaultAttr("DataElementConcept", sSearchIn);
                 DataManager.setAttribute(session, "selectedAttr", vSelVector);
                 getCompAttrList(req, res, "DataElementConcept", "nothing");
                 // if (retCode.equals("0"))
                 getACSearch.getDECResult(req, res, vResult, "");
                 DataManager.setAttribute(session, "searchAC", "DataElementConcept");
                 newSearch = "Data Element Concept";
             }
             else if (assocAC.equals("AssocVDs"))
             {
                 req.setAttribute("GetAssocSearchAC", "true");
                 if (sSearchAC.equals("PermissibleValue"))
                     pvID = sID;
                 else if (sSearchAC.equals("DataElement"))
                     deID = sID;
                 else if (sSearchAC.equals("ConceptualDomain"))
                     cdID = sID;
                 else if (sSearchAC.equals("ClassSchemeItems"))
                     cscsiID = sID;
                 else if (sSearchAC.equals("ConceptClass"))
                     conID = sID;
                 Vector vRes = new Vector();
                 getACSearch.doVDSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, cdID, pvID,
                                 deID, cscsiID, conID, "", "", vRes);
                 DataManager.setAttribute(session, "vSelRows", vRes);
                 // do attributes after the search so no "two simultaneous request" errors
                 vSelVector = this.getDefaultAttr("ValueDomain", sSearchIn);
                 DataManager.setAttribute(session, "selectedAttr", vSelVector);
                 getCompAttrList(req, res, "ValueDomain", "nothing");
                 getACSearch.getVDResult(req, res, vResult, "");
                 DataManager.setAttribute(session, "searchAC", "ValueDomain");
                 newSearch = "Value Domain";
             }
             // get the old search for the label
             if (sSearchAC.equals("ConceptualDomain"))
                 oldSearch = "Conceptual Domain";
             else if (sSearchAC.equals("DataElementConcept"))
                 oldSearch = "Data Element Concept";
             else if (sSearchAC.equals("ValueDomain"))
                 oldSearch = "Value Domain";
             else if (sSearchAC.equals("PermissibleValue"))
                 oldSearch = "Permissible Value";
             else if (sSearchAC.equals("DataElement"))
                 oldSearch = "Data Element";
             else if (sSearchAC.equals("ClassSchemeItems"))
                 oldSearch = "Class Scheme Items";
             // make keyword empty and label for search result page.
             DataManager.setAttribute(session, "serKeyword", "");
             String labelWord = "";
             labelWord = " associated with " + oldSearch + " - " + sName; // make the label
             req.setAttribute("labelKeyword", newSearch + labelWord); // make the label
             // save the last word in the request attribute
             DataManager.setAttribute(session, "LastAppendWord", labelWord);
             DataManager.setAttribute(session, "results", vResult); // store result vector in the attribute
             Vector vCheckList = new Vector();
             DataManager.setAttribute(session, "CheckList", vCheckList); // empty the check list in the new search when not appended.
             DataManager.setAttribute(session, "backFromGetAssociated", "");
         }
         // couldnot find a id, go back to search results
         else
         {
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                             "Unable to determine the ID of the selected item. ");
         }
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * To sort the search results by clicking on the column heading. Called from 'service' method where reqType is
      * 'doSortCDE' calls 'getACSearch.getACSortedResult' method and forwards page 'SearchResultsPage.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doSortACActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         getACSearch.getACSortedResult(req, res);
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected menu
                                                                                                 // action
         if (sMenuAction.equals("searchForCreate"))
             ForwardJSP(req, res, "/OpenSearchWindow.jsp");
         else
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * To sort the search results of the blocks by clicking on the column heading. Called from 'service' method where
      * reqType is 'doSortBlocks' calls 'getACSearch.getBlocksSortedResult' method and forwards page
      * 'OpenSearchWindowBlocks.jsp' or 'OpenSearchWindowQualifiers.jsp'
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param ACType
      *
      * @throws Exception
      */
     public void doSortBlockActions(HttpServletRequest req, HttpServletResponse res, @SuppressWarnings("unused") String ACType) throws Exception
     {
         HttpSession session = req.getSession();
        // GetACSearch serAC = new GetACSearch(req, res, this);
         // EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
         String actType = (String) req.getParameter("actSelected");
         String sComp = (String) req.getParameter("searchComp");
         String sSelectedParentCC = (String) req.getParameter("selectedParentConceptCode");
         if (sSelectedParentCC == null)
             sSelectedParentCC = "";
         String sSelectedParentName = (String) req.getParameter("selectedParentConceptName");
         if (sSelectedParentName == null)
             sSelectedParentName = "";
         String sSelectedParentDB = (String) req.getParameter("selectedParentConceptDB");
         if (sSelectedParentDB == null)
             sSelectedParentDB = "";
         String sSelectedParentMetaSource = (String) req.getParameter("selectedParentConceptMetaSource");
         if (sSelectedParentMetaSource == null)
             sSelectedParentMetaSource = "";
         DataManager.setAttribute(session, "ParentMetaSource", sSelectedParentMetaSource);
         if (actType.equals("FirstSearch"))
         {
             if (sComp.equals("ParentConceptVM"))
             {
                 DataManager.setAttribute(session, "SelectedParentCC", sSelectedParentCC);
                 DataManager.setAttribute(session, "SelectedParentDB", sSelectedParentDB);
                 DataManager.setAttribute(session, "SelectedParentMetaSource", sSelectedParentMetaSource);
                 DataManager.setAttribute(session, "SelectedParentName", sSelectedParentName);
             }
             getCompAttrList(req, res, sComp, "searchForCreate");
             DataManager.setAttribute(session, "creContext", "");
             DataManager.setAttribute(session, "creSearchAC", sComp);
             ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
         }
         else if (actType.equals("showConceptInTree"))
             this.doEVSSearchActions(actType, req, res);
         // evs.showConceptInTree(sComp, actType);
         else if (actType.equals("appendConcept"))
         {
             PVServlet pvSer = new PVServlet(req, res, this);
             @SuppressWarnings("unused") String sPage = pvSer.storeConceptAttributes();
             ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
         }
         else
         {
             GetACSearch getACSearch = new GetACSearch(req, res, this);
             getACSearch.getBlockSortedResult(req, res, "Blocks");
             ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
         }
     }
 
     /**
      * To open search page after login or click search on the menu. Called from 'service' method where reqType is
      * 'getSearchFilter' Adds default attributes to 'selectedAttr' session vector. Makes empty 'results' session vector.
      * stores 'No ' to 'recsFound' session attribute. forwards page 'CDEHomePage.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doOpenSearchPage(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "vStatMsg", new Vector());
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
         DataManager.setAttribute(session, "LastMenuButtonPressed", "Search");
         Vector vDefaultAttr = new Vector();
         String searchAC = (String) session.getAttribute("searchAC");
         if (searchAC == null)
             searchAC = "DataElement";
         // make the default to longName if not Questions
         String sSearchIn = (String) session.getAttribute("serSearchIn");
         if ((sSearchIn == null) || (!searchAC.equals("Questions")))
             sSearchIn = "longName";
         DataManager.setAttribute(session, "serSearchIn", sSearchIn);
         vDefaultAttr = getDefaultAttr(searchAC, sSearchIn); // default display attributes
         DataManager.setAttribute(session, "selectedAttr", vDefaultAttr);
         this.getDefaultFilterAtt(req, res); // default filter by attributes
         doInitDDEInfo(req, res);
         clearSessionAttributes(req, res);
         // call the method to get attribute list for the selected AC
         getCompAttrList(req, res, searchAC, "nothing");
         ForwardJSP(req, res, "/CDEHomePage.jsp");
     }
 
     /**
      * stores status message in the session
      * @param sMsg string message to append to.
      */
      @SuppressWarnings("unchecked")
      public void storeStatusMsg(String sMsg)
      {
        try
        {
          HttpSession session = m_classReq.getSession();
          UtilService util = new UtilService();
          Vector<String> vStatMsg = (Vector)session.getAttribute("vStatMsg");
          String statusMsg = (String)session.getAttribute(Session_Data.SESSION_STATUS_MESSAGE);
          if (statusMsg == null) statusMsg = "";
          //parse single  double  quotes and new line char if any
          String alrtMsg = sMsg;
          if (!alrtMsg.equalsIgnoreCase("\\n"))
              alrtMsg = util.parsedStringAlertNewLine(alrtMsg);
          alrtMsg = util.parsedStringDoubleQuote(alrtMsg);
          alrtMsg = util.parsedStringSingleQuote(alrtMsg);
          if (vStatMsg == null) vStatMsg = new Vector<String>();
          //add message to both to string status message and vector stats message if not too big
          if (vStatMsg.size() < 35)
          {
            if (sMsg.equalsIgnoreCase("\\n"))
              statusMsg = statusMsg + alrtMsg;
            else
              statusMsg = statusMsg + alrtMsg + "\\n";
            DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, statusMsg);
          }
          //remove tab and newline from the msg for vector
          if (!sMsg.equalsIgnoreCase("\\n") && !sMsg.equalsIgnoreCase("\n"))
            sMsg = util.parsedStringMsgVectorTabs(sMsg, vStatMsg);
          if (!sMsg.equals(""))
              vStatMsg.addElement(sMsg);
 
          DataManager.setAttribute(session, "vStatMsg", vStatMsg);
          //add this message to the logger
          logger.fatal("Log Status Message " + sMsg);
        }
        catch(Exception e)
        {
          logger.fatal("ERROR in CurationServlet-storeStatusMsg for exception : " + e.toString(), e);
         // m_classReq.setAttribute("retcode", "Message Exception");
        }
      }
 
     /**
      * To clear session attributes when a main Menu button/item is selected.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @throws Exception
      */
     public void clearSessionAttributes(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "vSearchIDStack", null);
         DataManager.setAttribute(session, "SearchID", null);
         DataManager.setAttribute(session, "vSearchNameStack", null);
         DataManager.setAttribute(session, "SearchName", null);
         DataManager.setAttribute(session, "sSearchACStack", null);
         DataManager.setAttribute(session, "vACSearchStack", null);
         DataManager.setAttribute(session, "vSearchASLStack", null);
         DataManager.setAttribute(session, "vACSearch", null);
         DataManager.setAttribute(session, "vSelRowsStack", null);
         DataManager.setAttribute(session, "vResultStack", null);
         DataManager.setAttribute(session, "vCompAttrStack", null);
         DataManager.setAttribute(session, "backFromGetAssociated", "");
         DataManager.setAttribute(session, "GetAssocSearchAC", "");
         DataManager.setAttribute(session, "results", null);
         DataManager.setAttribute(session, "vSelRows", null);
         DataManager.setAttribute(session, "selCS", "");
         DataManager.setAttribute(session, "serSelectedCD", "");
         // parent concept for the VD
         // DataManager.setAttribute(session, "VDParentConcept", new Vector());
         DataManager.setAttribute(session, "vParentList", null);
         DataManager.setAttribute(session, "vParentNames", null);
         DataManager.setAttribute(session, "vParentCodes", null);
         DataManager.setAttribute(session, "vParentDB", null);
         DataManager.setAttribute(session, "vParentMetaSource", null);
         DataManager.setAttribute(session, "SelectedParentName", "");
         DataManager.setAttribute(session, "SelectedParentCC", "");
         DataManager.setAttribute(session, "SelectedParentDB", "");
         DataManager.setAttribute(session, "ParentMetaSource", null);
         // pv list for the vd
         // DataManager.setAttribute(session, "VDPVList", new Vector());
         // DataManager.setAttribute(session, "PVIDList", new Vector());
         DataManager.setAttribute(session, "m_OC", null);
         DataManager.setAttribute(session, "selObjRow", null);
         DataManager.setAttribute(session, "m_PC", null);
         DataManager.setAttribute(session, "selPropRow", null);
         DataManager.setAttribute(session, "vObjectClass", null);
         DataManager.setAttribute(session, "vProperty", null);
         DataManager.setAttribute(session, "m_DEC", null);
         DataManager.setAttribute(session, "m_REP", null);
         DataManager.setAttribute(session, "selRepRow", null);
         DataManager.setAttribute(session, "m_OCQ", null);
         DataManager.setAttribute(session, "selObjQRow", null);
         DataManager.setAttribute(session, "m_PCQ", null);
         DataManager.setAttribute(session, "selPropQRow", null);
         DataManager.setAttribute(session, "m_REPQ", null);
         DataManager.setAttribute(session, "selRepQRow", null);
         DataManager.setAttribute(session, "creKeyword", "");
         DataManager.setAttribute(session, "serKeyword", "");
         DataManager.setAttribute(session, "EVSresults", null);
         DataManager.setAttribute(session, "VDEditAction", null);
         DataManager.setAttribute(session, "DEEditAction", null);
         DataManager.setAttribute(session, "DECEditAction", null);
         DataManager.setAttribute(session, "ParentConceptCode", null);
         // DataManager.setAttribute(session, "OpenTreeToConcept", "");
         DataManager.setAttribute(session, "TabFocus", "VD");
         
     }
 
     /**
      * To clear session attributes when a main Menu button/item is selected.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @throws Exception
      */
     public void clearBuildingBlockSessionAttributes(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, "m_OC", null);
         DataManager.setAttribute(session, "selObjRow", null);
         DataManager.setAttribute(session, "m_PC", null);
         DataManager.setAttribute(session, "selPropRow", null);
         DataManager.setAttribute(session, "vObjResults", null);
         DataManager.setAttribute(session, "vPropResults", null);
         DataManager.setAttribute(session, "vRepResults", null);
         DataManager.setAttribute(session, "m_REP", null);
         DataManager.setAttribute(session, "selRepRow", null);
         DataManager.setAttribute(session, "vObjQResults", null);
         DataManager.setAttribute(session, "m_OCQ", null);
         DataManager.setAttribute(session, "selObjQRow", null);
         DataManager.setAttribute(session, "vPropQResults", null);
         DataManager.setAttribute(session, "m_PCQ", null);
         DataManager.setAttribute(session, "selPropQRow", null);
         DataManager.setAttribute(session, "vRepQResults", null);
         DataManager.setAttribute(session, "m_REPQ", null);
         DataManager.setAttribute(session, "selRepQRow", null);
         DataManager.setAttribute(session, "vObjectClass", null);
         DataManager.setAttribute(session, "newObjectClass", "");
         DataManager.setAttribute(session, "RemoveOCBlock", "");
         DataManager.setAttribute(session, "vProperty", null);
         DataManager.setAttribute(session, "newProperty", "");
         DataManager.setAttribute(session, "RemovePropBlock", "");
         DataManager.setAttribute(session, "vRepTerm", null);
         DataManager.setAttribute(session, "newRepTerm", "");
         DataManager.setAttribute(session, "ConceptLevel", "0");
         DataManager.setAttribute(session, "creMetaCodeSearch", null);
         DataManager.setAttribute(session, "creKeyword", "");
         DataManager.setAttribute(session, "serKeyword", "");
         DataManager.setAttribute(session, "EVSresults", null);
         DataManager.setAttribute(session, "ParentMetaSource", null);
         DataManager.setAttribute(session, "ParentConceptCode", null);
        }
 
     /**
      * To clear session attributes when a main Menu button/item is selected.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @throws Exception
      */
     public void clearCreateSessionAttributes(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         // parent concept for the VD
         // DataManager.setAttribute(session, "VDParentConcept", new Vector());
         DataManager.setAttribute(session, "SelectedParentName", "");
         DataManager.setAttribute(session, "SelectedParentCC", "");
         DataManager.setAttribute(session, "SelectedParentDB", "");
         // pv list for the vd
         // DataManager.setAttribute(session, "VDPVList", new Vector());
         DataManager.setAttribute(session, "PVIDList", new Vector());
         DataManager.setAttribute(session, "creKeyword", "");
         DataManager.setAttribute(session, "serKeyword", "");
         DataManager.setAttribute(session, "EVSresults", null);
         DataManager.setAttribute(session, "OpenTreeToConcept", "");
         DataManager.setAttribute(session, "labelKeyword", "");
         // clear altname refdoc attributes after creating, editing, or back
         DataManager.setAttribute(session, "AllAltNameList", new Vector());
         DataManager.setAttribute(session, "AllRefDocList", new Vector());
         DataManager.setAttribute(session, "vACId", new Vector());
         DataManager.setAttribute(session, "vACName", new Vector());
         DataManager.setAttribute(session, "TabFocus", "VD");
         // clear vm attribute
         DataManager.setAttribute(session, VMForm.SESSION_SELECT_VM, new VM_Bean());
     }
 
     /**
      * To get the default attributes for the selected Component.
      *
      * @param searchAC
      *            String The selected Administered component
      * @param sSearchIn
      *            String The selected search in filter.
      *
      * @return Vector selected attribute Vector
      *
      * @throws Exception
      */
     public Vector<String> getDefaultAttr(String searchAC, String sSearchIn) throws Exception
     {
         Vector<String> vDefaultAttr = new Vector<String>();
         if (searchAC == null)
             searchAC = "DataElement";
         if (sSearchIn == null)
             sSearchIn = "longName";
         // store the default attributes to select and set some default attributes
         if (searchAC.equals("PermissibleValue"))
         {
             vDefaultAttr.addElement("Value");
             vDefaultAttr.addElement("Value Meaning");
             vDefaultAttr.addElement("Value Meaning Description");
             vDefaultAttr.addElement("Conceptual Domain");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Definition Source");
             vDefaultAttr.addElement("Vocabulary");
         }
         else if (searchAC.equals("ValueMeaning"))
         {
             vDefaultAttr.addElement("Value Meaning");
             vDefaultAttr.addElement("Meaning Description");
             vDefaultAttr.addElement("Conceptual Domain");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Definition Source");
             vDefaultAttr.addElement("Vocabulary");
         }
         else if (searchAC.equals("Questions"))
         {
             vDefaultAttr.addElement("Question Text");
             vDefaultAttr.addElement("DE Long Name");
             vDefaultAttr.addElement("DE Public ID");
             vDefaultAttr.addElement("Workflow Status");
             vDefaultAttr.addElement("Value Domain");
         }
         else if (searchAC.equals("ObjectClass") || searchAC.equals("Property"))
         {
             vDefaultAttr.addElement("Concept Name");
             vDefaultAttr.addElement("Public ID");
             vDefaultAttr.addElement("Version");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Definition");
             vDefaultAttr.addElement("Definition Source");
             vDefaultAttr.addElement("Context");
             vDefaultAttr.addElement("Vocabulary");
             vDefaultAttr.addElement("DEC's Using");
         }
         else if (searchAC.equals("ConceptClass"))
         {
             vDefaultAttr.addElement("Concept Name");
             vDefaultAttr.addElement("Public ID");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Vocabulary");
             vDefaultAttr.addElement("Definition");
             vDefaultAttr.addElement("Definition Source");
             vDefaultAttr.addElement("Context");
         }
         else if (searchAC.equals("ClassSchemeItems"))
         {
             vDefaultAttr.addElement("CSI Name");
             vDefaultAttr.addElement("CSI Type");
             vDefaultAttr.addElement("CSI Definition");
             vDefaultAttr.addElement("CS Long Name");
             vDefaultAttr.addElement("CS Public ID");
             vDefaultAttr.addElement("CS Version");
             vDefaultAttr.addElement("Context");
         }
        /* else if (searchAC.equals("RepTerm"))
         {
             vDefaultAttr.addElement("Concept Name");
             vDefaultAttr.addElement("Definition");
             vDefaultAttr.addElement("Definition Source");
         }*/
         else
         {
             vDefaultAttr.addElement("Long Name");
             vDefaultAttr.addElement("Public ID");
             vDefaultAttr.addElement("Version");
             if (searchAC.equals("DataElement"))
                 vDefaultAttr.addElement("Registration Status");
             vDefaultAttr.addElement("Workflow Status");
             // only if search is Data element
             if (searchAC.equals("DataElement"))
             {
                 vDefaultAttr.addElement("Owned By Context");
                 vDefaultAttr.addElement("Used By Context");
             }
             else
                 vDefaultAttr.addElement("Context");
             vDefaultAttr.addElement("Definition");
             // only if search is Data element
             if (searchAC.equals("DataElement"))
             {
                 vDefaultAttr.addElement("Data Element Concept");
                 vDefaultAttr.addElement("Value Domain");
             }
         }
         return vDefaultAttr;
     }
 
     /**
      * default attributes for evs searches
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param dtsVocab
      *            String vocab name
      * @throws Exception
      */
     public void getDefaultBlockAttr(HttpServletRequest req, HttpServletResponse res, String dtsVocab) throws Exception
     {
         HttpSession session = req.getSession();
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "searchForCreate");
         Vector vResult = new Vector();
         DataManager.setAttribute(session, "results", vResult);
         DataManager.setAttribute(session, "creRecsFound", "No ");
         // defuault filter attributes
         String sSearchInEVS = "Name";
         DataManager.setAttribute(session, "dtsVocab", dtsVocab);
         DataManager.setAttribute(session, "SearchInEVS", sSearchInEVS);
         DataManager.setAttribute(session, "creSearchInBlocks", "longName");
         DataManager.setAttribute(session, "creContextBlocks", "All Contexts");
         DataManager.setAttribute(session, "creStatusBlocks", "RELEASED");
         DataManager.setAttribute(session, "creRetired", "Exclude");
         DataManager.setAttribute(session, "MetaSource", "All Sources");
         // get default attributes
         Vector vSel = (Vector) session.getAttribute("creAttributeList");
         Vector vSelClone = (Vector) vSel.clone();
         vSelClone.remove("Version");
         DataManager.setAttribute(session, "creSelectedAttr", vSelClone);
         // make default tree
         this.doEVSSearchActions("defaultBlock", req, res);
         // this.doCollapseAllNodes(req, dtsVocab);
     }
 
     /**
      * To get the default filter by attributes for the selected Component.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void getDefaultFilterAtt(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // reset to default filter by criteria
         if (!menuAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "serStatus", new Vector());
             DataManager.setAttribute(session, "serMultiContext", new Vector());
             DataManager.setAttribute(session, "serContext", ""); // keep the old context criteria
             DataManager.setAttribute(session, "serContextUse", ""); // store contextUse in the session
             DataManager.setAttribute(session, "serVersion", ""); // store version in the session
             DataManager.setAttribute(session, "serVDTypeEnum", ""); // store VDType Enum in the session
             DataManager.setAttribute(session, "serVDTypeNonEnum", ""); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "serVDTypeRef", ""); // store VDType Ref in the session
             DataManager.setAttribute(session, "serRegStatus", ""); // store regstatus in the session
             DataManager.setAttribute(session, "serDerType", ""); // store derivation Type in the session
             DataManager.setAttribute(session, "serCreatedFrom", "");
             DataManager.setAttribute(session, "serCreatedTo", "");
             DataManager.setAttribute(session, "serModifiedFrom", "");
             DataManager.setAttribute(session, "serModifiedTo", "");
             DataManager.setAttribute(session, "serCreator", "");
             DataManager.setAttribute(session, "serModifier", "");
             DataManager.setAttribute(session, "serKeyword", "");
             DataManager.setAttribute(session, "serProtoID", "");
             DataManager.setAttribute(session, "serSearchIn", "longName"); // make default to longName
             DataManager.setAttribute(session, "selCS", "");
             DataManager.setAttribute(session, "serSelectedCD", "");
             // reset the appened attributes
             req.setAttribute("recsFound", "No ");
             DataManager.setAttribute(session, "CheckList", new Vector());
             DataManager.setAttribute(session, "AppendAction", "Not Appended");
             DataManager.setAttribute(session, "vSelRows", new Vector());
             DataManager.setAttribute(session, "results", new Vector());
         }
         else
         {
             DataManager.setAttribute(session, "creStatus", new Vector());
             DataManager.setAttribute(session, "creMultiContext", new Vector()); // keep the old context criteria
             DataManager.setAttribute(session, "creContext", ""); // keep the old context criteria
             DataManager.setAttribute(session, "creContextUse", ""); // store contextUse in the session
             DataManager.setAttribute(session, "creVersion", ""); // store version in the session
             DataManager.setAttribute(session, "creVDTypeEnum", ""); // store VDType Enum in the session
             DataManager.setAttribute(session, "creVDTypeNonEnum", ""); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "creVDTypeRef", ""); // store VDType Ref in the session
             DataManager.setAttribute(session, "creRegStatus", ""); // store regstatus in the session
             DataManager.setAttribute(session, "creDerType", ""); // store derivation Type in the session
             DataManager.setAttribute(session, "creCreatedFrom", "");
             DataManager.setAttribute(session, "creCreatedTo", "");
             DataManager.setAttribute(session, "creModifiedFrom", "");
             DataManager.setAttribute(session, "creModifiedTo", "");
             DataManager.setAttribute(session, "creCreator", "");
             DataManager.setAttribute(session, "creModifier", "");
             DataManager.setAttribute(session, "creKeyword", "");
             DataManager.setAttribute(session, "creProtoID", "");
             req.setAttribute("creSearchIn", "longName"); // make default to longName
             DataManager.setAttribute(session, "creSelectedCD", "");
         }
     }
 
     /**
      * To open search page when clicked on edit, create new from template, new version on the menu. Called from
      * 'service' method where reqType is 'actionFromMenu' Sets the attribte 'searchAC' to the selected component. Sets
      * the attribte 'MenuAction' to the selected menu action. Makes empty 'results' session vector. stores 'No ' to
      * 'recsFound' session attribute. forwards page 'SearchResultsPage.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doMenuAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         this.clearSessionAttributes(req, res);
         this.clearBuildingBlockSessionAttributes(req, res);
         this.clearCreateSessionAttributes(req, res);
         String sMAction = (String) req.getParameter("hidMenuAction");
         if (sMAction == null)
             sMAction = "nothing";
         DataManager.setAttribute(session, "DDEAction", "nothing"); // reset from "CreateNewDEFComp"
         String searchAC = "DataElement";
         // sets the session attributes of the selection menu action and selected component
         if (sMAction.equals("editDE") || sMAction.equals("editDEC") || sMAction.equals("editVD"))
             DataManager.setAttribute(session, "LastMenuButtonPressed", "Edit");
         else if (sMAction.equals("NewDETemplate") || sMAction.equals("NewDEVersion")
                         || sMAction.equals("NewDECTemplate") || sMAction.equals("NewDECVersion")
                         || sMAction.equals("NewVDTemplate") || sMAction.equals("NewVDVersion"))
             DataManager.setAttribute(session, "LastMenuButtonPressed", "CreateTemplateVersion");
         if ((sMAction == null) || (sMAction.equals("nothing")) || (sMAction.equals("Questions")))
             sMAction = "nothing";
         else
         {
             if ((sMAction.equals("NewDETemplate")) || (sMAction.equals("NewDEVersion")) || (sMAction.equals("editDE")))
                 searchAC = "DataElement";
             else if ((sMAction.equals("NewDECTemplate")) || (sMAction.equals("NewDECVersion"))
                             || (sMAction.equals("editDEC")))
                 searchAC = "DataElementConcept";
             else if ((sMAction.equals("NewVDTemplate")) || (sMAction.equals("NewVDVersion"))
                             || (sMAction.equals("editVD")))
             {
                 searchAC = "ValueDomain";
                 DataManager.setAttribute(session, "originAction", "NewVDTemplate");
                 DataManager.setAttribute(session, "VDEditAction", "editVD");
                 this.clearBuildingBlockSessionAttributes(req, res);
             }
         }
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMAction);
         DataManager.setAttribute(session, "searchAC", searchAC);
         // sets the default attributes and resets to empty result vector
         Vector vResult = new Vector();
         DataManager.setAttribute(session, "results", vResult);
         req.setAttribute("recsFound", "No ");
         DataManager.setAttribute(session, "serKeyword", "");
         DataManager.setAttribute(session, "serProtoID", "");
         DataManager.setAttribute(session, "LastAppendWord", "");
         // remove the status message if any
         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
         DataManager.setAttribute(session, "vStatMsg", new Vector());
         // set it to longname be default
         String sSearchIn = "longName";
         Vector vSelVector = new Vector();
         // call the method to get default attributes
         vSelVector = getDefaultAttr(searchAC, sSearchIn);
         DataManager.setAttribute(session, "selectedAttr", vSelVector);
         this.getDefaultFilterAtt(req, res); // default filter by attributes
         this.getCompAttrList(req, res, searchAC, sMAction); // call the method to get attribute list for the selected AC
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     /**
      * To get the list of attributes for the selected search component. Called from 'doRefreshPageOnSearchFor',
      * 'doMenuAction', 'doOpenSearchPage' methods stores the vector in the session attribute.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param selSearch
      *            the component to search for.
      * @param sMenu
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     public void getCompAttrList(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res, String selSearch, String sMenu)
                     throws Exception
     {
         HttpSession session = req.getSession();
         Vector vCompAtt = new Vector();
         if (selSearch.equals("DataElement"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Registration Status");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Owned By Context");
             vCompAtt.addElement("Used By Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Data Element Concept");
             vCompAtt.addElement("Value Domain");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Concept Name");
             // protocol id and crf name is not default and positioned at 13th & 14th place
             // vCompAtt.addElement("Protocol ID");
             // vCompAtt.addElement("CRF Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             // vCompAtt.addElement("Historical CDE ID");
             vCompAtt.addElement("Permissible Value");
             // vCompAtt.addElement("Preferred Question Text Document Text");
             // vCompAtt.addElement("Historic Short CDE Name Document Text");
             vCompAtt.addElement("Alternate Names");
             vCompAtt.addElement("Reference Documents");
             vCompAtt.addElement("Derivation Relationship");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("DataElementConcept"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Conceptual Domain");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             vCompAtt.addElement("Alternate Names");
             vCompAtt.addElement("Reference Documents");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ValueDomain"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Conceptual Domain");
             vCompAtt.addElement("Data Type");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             vCompAtt.addElement("Unit of Measures");
             vCompAtt.addElement("Display Format");
             vCompAtt.addElement("Maximum Length");
             vCompAtt.addElement("Minimum Length");
             vCompAtt.addElement("High Value Number");
             vCompAtt.addElement("Low Value Number");
             vCompAtt.addElement("Decimal Place");
             vCompAtt.addElement("Type Flag");
             vCompAtt.addElement("Permissible Value");
             vCompAtt.addElement("Alternate Names");
             vCompAtt.addElement("Reference Documents");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("PermissibleValue"))
         {
             vCompAtt.addElement("Value");
             vCompAtt.addElement("Value Meaning");
             vCompAtt.addElement("Value Meaning Description");
             vCompAtt.addElement("Conceptual Domain");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("EVS Identifier");
             vCompAtt.addElement("Description Source");
             vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ValueMeaning"))
         {
             vCompAtt.addElement("Value Meaning");
             vCompAtt.addElement("Meaning Description");
             vCompAtt.addElement("Conceptual Domain");
             vCompAtt.addElement("EVS Identifier");
             vCompAtt.addElement("Definition Source");
             vCompAtt.addElement("Vocabulary");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         else if (selSearch.equals("ParentConcept") || selSearch.equals("PV_ValueMeaning"))
         {
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("EVS Identifier");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Definition Source");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Semantic Type");
             vCompAtt.addElement("Vocabulary");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         else if (selSearch.equals("ParentConceptVM"))
         {
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("EVS Identifier");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Definition Source");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Semantic Type");
             vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("Level");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         else if (selSearch.equals("Questions"))
         {
             vCompAtt.addElement("Question Text");
             vCompAtt.addElement("DE Long Name");
             vCompAtt.addElement("DE Public ID");
             vCompAtt.addElement("Question Public ID");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Value Domain");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Protocol ID");
             vCompAtt.addElement("CRF Name");
             vCompAtt.addElement("Highlight Indicator");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ConceptualDomain"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Origin");
             // vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ClassSchemeItems"))
         {
             vCompAtt.addElement("CSI Name");
             vCompAtt.addElement("CSI Type");
             vCompAtt.addElement("CSI Definition");
             vCompAtt.addElement("CS Long Name");
             vCompAtt.addElement("CS Public ID");
             vCompAtt.addElement("CS Version");
             // vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ObjectClass") || selSearch.equals("Property") || selSearch.equals("PropertyClass")
                         || selSearch.equals("RepTerm") || selSearch.equals("VDObjectClass")
                         || selSearch.equals("VDProperty") || selSearch.equals("VDPropertyClass")
                         || selSearch.equals("VDRepTerm") || selSearch.equals("RepQualifier")
                         || selSearch.equals("ObjectQualifier") || selSearch.equals("PropertyQualifier")
                         || selSearch.equals("VDRepQualifier") || selSearch.equals("VDObjectQualifier")
                         || selSearch.equals("VDPropertyQualifier") || selSearch.equals("EVSValueMeaning")
                         || selSearch.equals("CreateVM_EVSValueMeaning") || selSearch.equals("ConceptClass")
                         || selSearch.equals("VMConcept") || selSearch.equals("EditVMConcept"))
         {
             boolean isMainConcept = false;
             if (!sMenu.equals("searchForCreate") && selSearch.equals("ConceptClass"))
                 isMainConcept = true;
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("EVS Identifier");
             if (isMainConcept) // add here if main concept search
                 vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Definition Source");
             vCompAtt.addElement("Workflow Status");
             if (sMenu.equals("searchForCreate")) // add this only if search for create which as evs search
                 vCompAtt.addElement("Semantic Type");
             vCompAtt.addElement("Context");
             if (!isMainConcept) // add here if not main concept search
                 vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("caDSR Component");
             if (selSearch.equals("ObjectClass") || selSearch.equals("Property") || selSearch.equals("PropertyClass")
                             || selSearch.equals("VDObjectClass") || selSearch.equals("VDProperty")
                             || selSearch.equals("VDPropertyClass"))
                 vCompAtt.addElement("DEC's Using");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         // store it in the session
         if (sMenu.equals("searchForCreate"))
             DataManager.setAttribute(session, "creAttributeList", vCompAtt);
         else
             DataManager.setAttribute(session, "serAttributeList", vCompAtt);
         Vector vSelectedAttr = (Vector) session.getAttribute("creSelectedAttr");
         if (vSelectedAttr == null || selSearch.equals("ReferenceValue"))
         {
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
             DataManager.setAttribute(session, "creSearchAC", selSearch);
         }
     } // end compattlist
 
     /**
      * This method forwards to an Error page. Called from 'service' method where reqType is 'actionFromMenu' forwards
      * page 'ErrorLoginPage.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      */
     public void ErrorLogin(HttpServletRequest req, HttpServletResponse res)
     {
         // capture the stack in logger
         try
         {
             throw new Exception("Error Login used in various methods");
         }
         catch (Exception e)
         {
             logger.fatal("Error Caught : ", e);
         }
         // forward to error jsp
         try
         {
             ForwardErrorJSP(req, res, "Session Terminated. Please log in again.");
         }
         catch (Exception e)
         {
             // System.err.println("ERROR - ErrorLogin: " + e);
             this.logger.fatal("ERROR - ErrorLogin: " + e.toString(), e);
         }
     }
 
     /**
      * The destroy method closes a connection pool to db.
      */
     public void destroy()
     {
     }
 
     /**
      * doLogout method closes the connection and forwards to Login page Called from Logout button on Titlebar.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     public void doLogout(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         try
         {
             HttpSession session = req.getSession();
             session.invalidate();
             ForwardErrorJSP(req, res, "Logged out.");
         }
         catch (Exception e)
         {
             logger.fatal("ERROR - ErrorLogin: " + e.toString(), e);
         }
     }
 
     /**
      * The ForwardJSP method forwards to a jsp page.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sJSPPage
      *            The JSP page to which to forward.
      */
     public void ForwardJSP(HttpServletRequest req, HttpServletResponse res, String sJSPPage)
     {
         try
         {
             // forward to the jsp (or htm)
             HttpSession session = req.getSession();
             String sMsg = (String) session.getAttribute(Session_Data.SESSION_STATUS_MESSAGE);
             if (sMsg != null && !sMsg.equals(""))
             {
                 sMsg += "\\n\\nPlease use Ctrl+C to copy the message to a text file";
                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, sMsg);
             }
             // store the session data object in the session at the end of the request
             DataManager.setAttribute(session, Session_Data.CURATION_SESSION_ATTR, this.sessionData);
             String fullPage = "/jsp" + sJSPPage;
           
          // ServletContext sc = this.getServletContext();
             RequestDispatcher rd = m_servletContext.getRequestDispatcher(fullPage);
             rd.forward(req, res);
             return;
         }
         catch (Exception e)
         {
             e.printStackTrace();
             this.logger.fatal("Servlet-ForwardJSP : " + e.toString(), e);
         }
     }
 
     /**
      * The ForwardErrorJSP method forwards to a jsp page.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param errMsg
      *            String error message
      * @throws Exception
      */
     public void ForwardErrorJSP(HttpServletRequest req, HttpServletResponse res, String errMsg) throws Exception
     {
         try
         {
             HttpSession session;
             session = req.getSession(true);
             String reqMsg = (String) req.getAttribute("ReqErrorMessage");
             if (reqMsg != null && !reqMsg.equals(""))
                 errMsg = reqMsg;
             DataManager.setAttribute(session, "ErrorMessage", errMsg);
             String fullPage = "/";
             //ServletContext sc = this.getServletContext();
             RequestDispatcher rd = m_servletContext.getRequestDispatcher(fullPage);
             rd.forward(req, res);
         }
         catch (Exception e)
         {
             logger.fatal("Servlet-ForwardErrorJSP : " + e.toString(), e);
         }
     }
 
     /**
      * This is the primary method for handling requests from the Reference Document Attachments page.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sOrigin
      *            The String controling weather this is a open action or other request type action
      */
     public void doRefDocumentUpload(HttpServletRequest req, HttpServletResponse res, String sOrigin)
     {
         String sAction;
         String msg = null;
       //  HttpSession session = req.getSession();
         RefDocAttachment refDocAt = new RefDocAttachment(req, res, this);
         msg = req.getContentType();
         // upload file into the database as blob
         if (msg.startsWith("multipart/form-data"))
         {
             // file upload
             refDocAt.doFileUpload();
         }
         else
         {
             // get action type
             if ((String) req.getParameter("newRefDocPageAction") != null)
             {
                 sAction = (String) req.getParameter("newRefDocPageAction");
             }
             else
             {
                 sAction = "nothing";
             }
             // Open the upload ref doc page
             if (sOrigin.equals("Open"))
             {
                 refDocAt.doOpen();
             }
             // Request from page to preform actions
             else if (sOrigin.equals("Request"))
             {
                 // return to search results from upload page
                 if (sAction.equals("backToSearch"))
                 {
                     refDocAt.doBack();
                 }
                 // Delete ref doc attachment
                 else if (sAction.equals("DeleteAttachment"))
                 {
                     refDocAt.doDeleteAttachment();
                 }
                 // Catch any undefined action from page
                 else
                 {
                     try
                     {
                         logger.fatal("Reference Document Attachments Upload: Unknown Request Type.");
                         ForwardErrorJSP(req, res, "Unexpected Request. Session Terminated. Please login again.");
                     }
                     catch (Exception e)
                     {
                         logger.fatal("ERROR - ErrorLogin: " + e.toString(), e);
                     }
                 }
             }
             // catch unknown Ref type
             else
             {
                 try
                 {
                     ForwardErrorJSP(req, res, "Unexpected Request. Session Terminated. Please login again.");
                     logger.fatal("Reference Document Attachments Upload: Unknown Origin Type.");
                 }
                 catch (Exception e)
                 {
                     logger.fatal("ERROR - ErrorLogin: " + e.toString(), e);
                 }
             }
         }
     }
 
 	/**
 	 * @return the m_conn
 	 */
 	public Connection getConn() {
 		return this.m_conn;
 	}
 
 	/**
 	 * @param m_conn the m_conn to set
 	 */
 	public void setConn(Connection conn) {
 		this.m_conn = conn;
 	}
 	
 	private void getApprovedRepTerm()
 	{
 		 Vector vResult = new Vector();
 		 String valueString=new String();
 		 HttpSession session = m_classReq.getSession(true);
 		 ConceptServlet conSer = new ConceptServlet(m_classReq, m_classRes, this);
 		 ConceptAction conact = new ConceptAction();
 		 try {
 			Statement stm = m_conn.createStatement();
 			ResultSet rs = stm.executeQuery("SELECT value from TOOL_OPTIONS_VIEW_EXT where PROPERTY like '%REPTERM.PRICON%'");
 			while(rs.next())
 			{
 				valueString+="'"+rs.getString(1)+"',";
 			}
 			String valu = valueString.substring(0,valueString.length()-1);
 			rs.close();
 			stm.close();
 			String sql = "SELECT con.*,cont.name as Context FROM CONCEPTS_VIEW_EXT con,CONTEXTS_VIEW cont WHERE con.CONTE_IDSEQ=cont.CONTE_IDSEQ and PREFERRED_NAME IN ("+valu+") ORDER BY con.long_name ASC";
 			//System.out.println(sql);
 			Statement stm1 = m_conn.createStatement();
 			ResultSet rs1 = stm1.executeQuery(sql);
 			if(rs1!=null)
 			{
 				conact.getApprovedRepTermConcepts(rs1,conSer.getData());
 				DataManager.setAttribute(session , "vACSearch", conSer.getData().getConceptList());
 			}
 			rs1.close();
 			stm1.close();			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		 EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
 		 evs.get_Result(m_classReq, m_classRes, vResult, "");
 		 DataManager.setAttribute(m_classReq.getSession(), "results", vResult);
 		 	
 	}
 	/**
 	 * Gets the default Rep term Context
 	 */
 	private void getRepTermDefaultContext()
     {
 		String defaultContext=null;
 		HttpSession session = m_classReq.getSession(true);
         try
         {
             String sQuery = "select value from sbrext.tool_options_view_ext where property like 'REPTERM.DEFAULT.CONTEXT'";
             Statement stm = m_conn.createStatement();
 			ResultSet rs = stm.executeQuery(sQuery);
 			while(rs.next())
 			{
 				defaultContext= rs.getString(1);
 			}
             session.setAttribute("defaultRepTermContext", defaultContext);
         }
         catch (Exception e)
         {
             logger.fatal("Error - getRepTermDefaultContext : " + e.toString(), e);
         }
     }
 } // end of class
