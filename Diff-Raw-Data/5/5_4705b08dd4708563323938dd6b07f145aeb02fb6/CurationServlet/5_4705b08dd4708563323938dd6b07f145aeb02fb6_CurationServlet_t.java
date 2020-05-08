 // Copyright (c) 2005 ScenPro, Inc.
 // $Header: /CVSNT/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/CurationServlet.java,v 1.81 2007/04/30 14:29:11
 // lhebel Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 // import files
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsServlet;
 import gov.nih.nci.cadsr.cdecurate.ui.DesDEServlet;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 import gov.nih.nci.cadsr.persist.ac.Admin_Components_Mgr;
 import gov.nih.nci.cadsr.persist.exception.DBException;
 import gov.nih.nci.cadsr.persist.user.User_Accounts_Mgr;
 import gov.nih.nci.cadsr.sentinel.util.DSRAlert;
 import gov.nih.nci.cadsr.sentinel.util.DSRAlertImpl;
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
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
     protected SetACService       m_setAC          = new SetACService(this);
     public HttpServletRequest  m_classReq       = null;
     public HttpServletResponse m_classRes       = null;
     protected ServletContext m_servletContext;
     public Connection m_conn=null;
 
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
     }
 
     /**
      *
      */
     public CurationServlet()
     {
 
     }
 
     /**
      * @param req HttpServletRequest object
      * @param res HttpServletResponse object
      * @param sc ServletContext object
      *
      */
     public void init(HttpServletRequest req, HttpServletResponse res, ServletContext sc)
     {
         m_classReq = req;
         m_classRes = res;
         m_servletContext = sc;
     }
 
     
     /**
      * makes the login message with all the information
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
 
 
     /**
      * Start in the /conf/template.cdecurate-oracle-ds.xml file. Notice the <jndi-name>.
      * This name is used by JBoss to create and identify the connection pool.
      * We copied this name to the /conf/template.web.xml file in the <param-value> element.
      * The <param-name> for this initialization value appears in the code
      * NCICurationServlet.initOracleConnect() method.
      * The data source pool name is then saved in a class variable _dataSourceName.     *
      * The variable is used by the CurationServlet.getConnFromDS() method which
      * is used by the CurationServlet.connectDB() method.
      * @return
      */
     public Connection getConnFromDS()
     {
         // Use tool database pool.
         Context envContext = null;
         DataSource ds = null;
         String user_;
         String pswd_;
         try
         {
             envContext = new InitialContext();
             ds = (DataSource) envContext.lookup(NCICurationServlet._dataSourceName);
             user_ = NCICurationServlet._userName;
             pswd_ = NCICurationServlet._password;
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
      * @param ub_
      * @return Connection
      */
     public Connection connectDB()
     {
         Connection SBRDb_conn = null;
         try
         {
             try
             {
               	 SBRDb_conn = this.getConnFromDS();
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
      * @param req
      * @param res
      * @param session
      */
     private void login(HttpServletRequest req, HttpServletResponse res,HttpSession session)throws Exception
     {    	
     	String username = req.getParameter("Username").toUpperCase();
         String password = req.getParameter("Password");
     	CaDsrUserCredentials uc = new CaDsrUserCredentials();
 	    UserBean userbean = new UserBean();
 	    User_Accounts_Mgr userAccountMgr = new User_Accounts_Mgr();
 	    try
     	 {
     	    userbean.setUsername(username);
     	    userbean.setPassword(password);
      		uc.validateCredentials(NCICurationServlet._userName, NCICurationServlet._password, username, password);
      		userbean.setUserFullName(userAccountMgr.getUserFullName(username, m_conn));
     	    userbean.setPassword("");
             userbean.setDBAppContext("/cdecurate");
           	DataManager.setAttribute(session, "Userbean", userbean);
           	DataManager.setAttribute(session, "Username", userbean.getUsername());
           	sessionData.UsrBean = userbean;
             GetACService getAC = new GetACService(m_classReq, m_classRes, this);
             getAC.getWriteContextList();
     	  }
 	      catch(DBException e){
 	    	 logger.error("Unable to get User FullName" + e);
 	      }
     	  catch (Exception ex)
     	  {
     		  userbean=null;
     		  logger.error("Failed credential validation, code is " + uc.getCheckCode());
     	      logger.error("Redirecting the user to Login Page");
 			  ForwardErrorJSP(req, res, "Incorrect Username or Password. Please re-enter.");				
     	  }      	  
      }
 
     /**
      * Authenticates the user login credentials with the jboss authentication
      * data source.
      * @param user
      * @return
      */
 /*    private boolean authenticate(UserBean user)
     {
     	 // Use tool database pool.
         Context envContext = null;
         DataSource ds = null;
         boolean validUser =false;
         try
         {
             envContext = new InitialContext();
             ds = (DataSource) envContext.lookup(NCICurationServlet._authenticateDSName);
         }
         catch (Exception e)
         {
             String stErr = "Error creating database pool[" + e.getMessage() + "].";
             logger.fatal(stErr, e);
             return false;
         }
         // Test connnection
         Connection con = null;
         try
         {
         	con = ds.getConnection(user.getUsername(), user.getPassword());
             if(con!=null)
             	{
             	validUser = true;
             	}
         }
         catch (SQLException e)
         {
            logger.fatal(e.toString(), e);
             return false;
         }finally{
         	SQLHelper.closeConnection(con);
         }
         return validUser;
     }
 */
     public void get_m_conn()
     {
         HttpSession session = m_classReq.getSession(true);
     	// get the session data object from the session
         sessionData = (Session_Data) session.getAttribute(Session_Data.CURATION_SESSION_ATTR);
         if (sessionData == null)
             sessionData = new Session_Data();
         //get the connection
         if (m_conn == null)
         {
         	m_conn = connectDB();
         	setConn(m_conn);
         }
     }
     
     protected UserBean checkLoggedIn() throws Exception
     {
     	UserBean ub = checkUserBean(m_classReq, m_classRes);
         if (ub == null)
         {
         	HttpSession session = m_classReq.getSession(true);
             String errMsg = getDBConnectMessage("Session Terminated");
 			DataManager.setAttribute(session, "ErrorMessage", errMsg);
 			// get the menu action from request
 			String mnReq = (String) m_classReq.getParameter("serMenuAct");
 			if (mnReq == null)
 			mnReq = "";
 			DataManager.setAttribute(session, "serMenuAct", mnReq);
 			// forward the error page
 			ForwardErrorJSP(m_classReq, m_classRes, errMsg);
         }
         return ub;
     }
     /**
      * The service method services all requests to this servlet.
      *
      */
     public void service()
     {
         UserBean ub = null;
         HttpSession session;
         session = m_classReq.getSession(true);
         try
         {
         	// get the session data object from the session
             sessionData = (Session_Data) session.getAttribute(Session_Data.CURATION_SESSION_ATTR);
             if (sessionData == null)
                      	sessionData = new Session_Data();
             else
         	m_conn = connectDB();
             String reqType = m_classReq.getParameter("reqType");
             m_classReq.setAttribute("LatestReqType", reqType);
             if (reqType != null)
             {
                 while (true)
                 {
                    // check the validity of the user login
                     if (reqType.equals("login"))
                     {
                         //DataManager.clearSessionAttributes(session);
                        // sessionData = new Session_Data();
                    	 String cancelLogin = (String)m_classReq.getParameter("cancelLogin");
                    	 if (cancelLogin.equals("No")){
                           login(m_classReq,m_classRes,session);
                    	 }  
                          String prevReq = m_classReq.getParameter("previousReqType");
                          if (prevReq == null) prevReq = "/SearchResultsPage.jsp";
                          ForwardJSP(m_classReq, m_classRes, prevReq);
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
                     	 if (reqType.equals("newCCFromMenu")) // fromForm
                          {
                              doCreateCCActions(m_classReq, m_classRes);
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
 //                        else if (reqType.equals("searchQualifiers"))
 //                        {
 //                            doQualifierSearchActions(m_classReq, m_classRes);
 //                        }
 /*                        // get more records of Doc Text
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
 */                        else if (reqType.equals("treeSearch"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeRefresh"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeExpand"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeCollapse"))
                         {
                            this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("getSubConcepts"))
                         {
                            this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("getSuperConcepts"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
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
             SQLHelper.closeConnection(m_conn);
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
 
         }finally{
             SQLHelper.closeConnection(m_conn);
         }
     } // end of service
 
     
     /**
      * The service method services all requests to this servlet.
      *
      */
     public void serviceOLD()
     {
         UserBean ub = null;
         HttpSession session;
         session = m_classReq.getSession(true);
         try
         {
         	// get the session data object from the session
             sessionData = (Session_Data) session.getAttribute(Session_Data.CURATION_SESSION_ATTR);
             if (sessionData == null)
                      	sessionData = new Session_Data();
             else
         	m_conn = connectDB();
             String reqType = m_classReq.getParameter("reqType");
             m_classReq.setAttribute("LatestReqType", reqType);
             if (reqType != null)
             {
                 while (true)
                 {
                    // check the validity of the user login
                     if (reqType.equals("login"))
                     {
                         DataManager.clearSessionAttributes(session);
                         sessionData = new Session_Data();
                          login(m_classReq,m_classRes,session);
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
 /*                        if (reqType.equals("homePage"))
                         {
                             doHomePage(m_classReq, m_classRes);
                         }
                         else if (DERequestTypes.valueOf(reqType) != null)  
                         {
                            DE_Servlet deServ = new DE_Servlet(m_classReq, m_classRes); // doOpenCreateNewPages(m_classReq, m_classRes, "de");
                            deServ.AC_Service(DERequestTypes.valueOf(reqType));
                         }
                         else if (reqType.equals("newDECFromMenu"))
                         {
                            // doOpenCreateNewPages(m_classReq, m_classRes, "dec");
                         }
                         else if (reqType.equals("newVDFromMenu"))
                         {
                            // doOpenCreateNewPages(m_classReq, m_classRes, "vd");
                         }
                         else if (reqType.equals("newDECfromForm")) // when DEC form is submitted
                         {
                             doCreateDECActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("newVDfromForm")) // when Edit VD form is submitted
                         {
                             doCreateVDActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("editDEC")) // when Edit DEC form is submitted
                         {
                             doEditDECActions(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("editVD"))
                         {
                             doEditVDActions(m_classReq, m_classRes);
                         }
   else*/                if (reqType.equals("newPV")) // fromForm
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
                    /*     else if (reqType.equals("createNewDEC"))
                         {
                             doOpenCreateDECPage(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("createNewVD"))
                         {
                             doOpenCreateVDPage(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("validateDECFromForm"))
                         {
                             doInsertDEC(m_classReq, m_classRes);
                         }
                         else if (reqType.equals("validateVDFromForm"))
                         {
                             doInsertVD(m_classReq, m_classRes);
                         } */
 /*                        else if (reqType.equals("searchACs"))
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
 */                        else if (reqType.equals("errorPageForward"))
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
 /*                        else if (reqType.equals("searchBlocks"))
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
  */                        
                         else if (reqType.equals("treeSearch"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeRefresh"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeExpand"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("treeCollapse"))
                         {
                            this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("getSubConcepts"))
                         {
                            this.doEVSSearchActions(reqType, m_classReq, m_classRes);
                         }
                         else if (reqType.equals("getSuperConcepts"))
                         {
                             this.doEVSSearchActions(reqType, m_classReq, m_classRes);
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
             SQLHelper.closeConnection(m_conn);
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
 
         }finally{
             SQLHelper.closeConnection(m_conn);
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
 /*    private void doLogin(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         ForwardJSP(req, res, "/CDEHomePage.jsp");
     }
 */
     /**
      * Gets the Help URL from Tool options table. 
      * @param req
      * @param res
      */
     public String getHelpURL(Connection con)
     {
     	 GetACService getAC = new GetACService(); 
     	Vector vList = new Vector();
         vList = getAC.getToolOptionData(con,"CURATION", "HELP.HOME", "");
         String aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
        return aURL;
     }
     
     /**
      * @param req
      * @param res
      * @throws Exception
      */
     private void doCreateCCActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
     	 ForwardJSP(req, res, "/CreateConceptClass.jsp");
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
     private void doCreatePVActions(HttpServletRequest req, HttpServletResponse res) throws Exception
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
                 /*if (vmBean.getVM_SHORT_MEANING().equals(selVM))*/
                 if (vmBean.getVM_LONG_NAME().equals(selVM))
                 {
                    // pvBean.setPV_MEANING_DESCRIPTION(vmBean.getVM_DESCRIPTION());
                 	pvBean.setPV_MEANING_DESCRIPTION(vmBean.getVM_PREFERRED_DEFINITION());
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
      * @param req
      * @param res
      * @throws Exception
      */
     protected void doOpenEditVM(HttpServletRequest req, HttpServletResponse res, VM_Bean vm, PV_Bean pv) throws Exception
     {
     	ValueDomainServlet vdServ = (ValueDomainServlet) this.getACServlet("ValueDomain");
         PVServlet pvSer = new PVServlet(req, res, vdServ);
         String pageFor = pvSer.openVMPageEdit(vm,pv);
         if (pageFor != null && !pageFor.equals(""))
         {
             if (pageFor.charAt(0) != '/')
                 pageFor = "/" + pageFor;
             ForwardJSP(req, res, pageFor);
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
     private void doEditPVActions(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
     	ValueDomainServlet vdServ = (ValueDomainServlet) this.getACServlet("ValueDomain");
         PVServlet pvSer = new PVServlet(req, res, vdServ);
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
     private void doEditVMActions(HttpServletRequest req, HttpServletResponse res) throws Exception
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
     private void doMarkAddRemoveDesignation(HttpServletRequest req, String pageAct) throws Exception
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
 /*    public void doChangeContext(HttpServletRequest req, HttpServletResponse res, String sACType) throws Exception
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
 */
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
     private Vector<EVS_Bean> getEVSSelRowVector(HttpServletRequest req, Vector<EVS_Bean> vList) throws Exception
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
                             vdMean = vdVM.getVM_LONG_NAME();//vdVM.getVM_SHORT_MEANING(); // pvBean.getPV_SHORT_MEANING();
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
          //   vm.setVM_SHORT_MEANING(pBean.getPV_SHORT_MEANING());
             vm.setVM_LONG_NAME(pBean.getPV_SHORT_MEANING());
            // vm.setVM_DESCRIPTION(pBean.getPV_MEANING_DESCRIPTION());
             vm.setVM_PREFERRED_DEFINITION(pBean.getPV_MEANING_DESCRIPTION());
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
      * called after setDEC or setVD to reset EVS session attributes
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
 /*    public void resetEVSBeans(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
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
 */
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
     protected String getNewVersionNumber(String version, String lastVersion) throws Exception
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
      * The doSuggestionDE method forwards to EVSSearch jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     protected void doSuggestionDE(HttpServletRequest req, HttpServletResponse res) throws Exception
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
     protected void doOpenCreatePVPage(HttpServletRequest req, HttpServletResponse res, String sPVAction, String vdPage)
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
     //    doOpenSearchPage(req, res);  Later***
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
 /*    public void doSearchPV(HttpServletRequest req, HttpServletResponse res) throws Exception
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
 */
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
     protected void doOpenCreateVMPage(HttpServletRequest req, HttpServletResponse res, @SuppressWarnings("unused") String origin) throws Exception
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
      * method to call all evs actions
      *
      * @param reqType
      *            String evs search action
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      */
     protected void doEVSSearchActions(String reqType, HttpServletRequest req, HttpServletResponse res)
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
     private void doSearchEVS(HttpServletRequest req, HttpServletResponse res) throws Exception
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
 /*    public void doQualifierSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
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
 */
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
 /*    public void doNewSearchBB(HttpServletRequest req, HttpServletResponse res) throws Exception
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
 */
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
     protected Hashtable<String, AC_CONTACT_Bean> doContactACUpdates(HttpServletRequest req, String sAct)
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
      * Monitor the user selected items with a Sentinel Alert.
      *
      * @param req
      *            The session request.
      * @param res
      *            The session response.
      */
     protected void doMonitor(HttpServletRequest req, HttpServletResponse res)
     {
         // Init main variables.
         HttpSession session = req.getSession();
         String msg = null;
         Vector<String> vCheckList = new Vector<String>();
         while (true)
         {
             // Be sure something was selected by the user.
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
             /*if (vSRows == null || vSRows.size() == 0){
                 msg = "No items were selected from the Search Results.";
                 break;
             }*/
             // Get session information.
             UserBean Userbean = new UserBean();
             Userbean = (UserBean) session.getAttribute("Userbean");
             if (Userbean == null)
             {
                 msg = "User session information is missing.";
                 break;
             }
             CallableStatement stmt = null;
             try
             {
                 // Get the selected items and associate each with the appropriate CSI
                 String user = Userbean.getUsername();
                 user = user.toUpperCase();
                // Add the selected items to the CSI
                 String csi_idseq = null;
                 int ndx = 0;
                 stmt = m_conn.prepareCall("begin SBREXT_CDE_CURATOR_PKG.ADD_TO_SENTINEL_CS(?,?,?); end;");
                 stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
                 stmt.setString(2, user);
                 try
                 {
                     vCheckList = new Vector<String>();
                     String unCheckedRowId = (String) m_classReq.getParameter("unCheckedRowId");
                     if (unCheckedRowId != null && !(unCheckedRowId == "")){
                         int selectedRowID = new Integer(unCheckedRowId);
                         AC_Bean bean = (AC_Bean) vSRows.elementAt(selectedRowID);
                         stmt.setString(1, bean.getIDSEQ());
                         stmt.execute();
                         if (stmt.getString(3) != null)
                             csi_idseq = stmt.getString(3);
                         for (int m = 0; m < (vSRows.size()); m++){
                             String ckName2 = ("CK" + m);
                             String rSel2 = (String) req.getParameter(ckName2);
                             if (rSel2 != null)
                                 vCheckList.addElement(ckName2);
                         }
                      }else if(vSRows.size() > 0){ 
                       for (ndx = 0; ndx < vSRows.size(); ++ndx){
                          String temp;
                          String ckName = ("CK" + ndx);
                          temp = req.getParameter(ckName);
                          if (temp != null){
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
                     } 
                    
                     DataManager.setAttribute(session, "CheckList", vCheckList); // add the check list in the session.
                 }
                 catch (ClassCastException e)
                 {
                     // This happens when the selected element does not extend the AC_Bean abstract class.
                     csi_idseq = "";
                 }
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
            }
             catch (Exception e)
             {
                 msg = "An unexpected exception occurred, please notify the Help Desk. Details have been written to the log.";
                 logger.fatal("cdecurate: doMonitor(): " + e.toString(), e);
             }
             finally{
             	SQLHelper.closeCallableStatement(stmt);
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
     protected void doUnmonitor(HttpServletRequest req, HttpServletResponse res)
     {
         // Init main variables.
         HttpSession session = req.getSession();
         String msg = null;
         Vector<String> vCheckList = new Vector<String>();
         while (true)
         {
             // Be sure something was selected by the user.
             Vector vSRows = (Vector) session.getAttribute("vSelRows");
            /* if (vSRows == null || vSRows.size() == 0){
                 msg = "No items were selected from the Search Results.";
                 break;
             }*/
             // Get session information.
             UserBean Userbean = new UserBean();
             Userbean = (UserBean) session.getAttribute("Userbean");
             if (Userbean == null){
                 msg = "User session information is missing.";
                 break;
             }
             // Get list of selected AC's.
             Vector<String> list = new Vector<String>();
             String unCheckedRowId = (String) m_classReq.getParameter("unCheckedRowId");
             if (unCheckedRowId != null && !(unCheckedRowId == "")){
                 int selectedRowID = new Integer(unCheckedRowId);
                 AC_Bean bean = (AC_Bean) vSRows.elementAt(selectedRowID);
                 list.add(bean.getIDSEQ());
                 for (int m = 0; m < (vSRows.size()); m++)
                 {
                     String ckName2 = ("CK" + m);
                     String rSel2 = (String) req.getParameter(ckName2);
                     if (rSel2 != null)
                         vCheckList.addElement(ckName2);
                 }
             }else if(vSRows.size() > 0){ 
                for (int ndx = 0; ndx < vSRows.size(); ++ndx){
                  try{
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
                 }catch (ClassCastException e){
                 }
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
             CallableStatement stmt=null;
             for (int ndx = 0; ndx < list.size(); ++ndx)
             {
                 try
                 {
                     String temp = list.elementAt(ndx);
                     stmt = m_conn.prepareCall("begin SBREXT_CDE_CURATOR_PKG.REMOVE_FROM_SENTINEL_CS('"
                                     + temp + "','" + user + "'); END;");
                     stmt.execute();
                     SQLHelper.closeCallableStatement(stmt);
                     msg = "The selected item is no longer monitored by the Alert Definition";
                 }
                 catch (Exception e)
                 {
                     msg = "An unexpected exception occurred, please notify the Help Desk. Details have been written to the log.";
                     logger.fatal("cdecurate: doUnmonitor(): " + e.toString(), e);
                 }finally{
                 	SQLHelper.closeCallableStatement(stmt);
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
     protected void doSearchSelectionAction(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sSearchAC = (String) session.getAttribute("searchAC"); // get the selected component
         CurationServlet acServlet = getACServlet(sSearchAC);
         // gets the bean for the row selected
         GetACSearch getACSearch = new GetACSearch(req, res, acServlet);
         if (getACSearch.getSelRowToEdit(req, res, "") == false)
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         else
         {
            // String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected
                                                                                                     // menu action
           //  String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
           //  String sOriginAction = (String) session.getAttribute("sOriginAction");
             // call method to handle DE actions.
             if (sSearchAC.equals("DataElement"))
                 this.doSerSelectActDE(req, res);
             else if (sSearchAC.equals("DataElementConcept"))
                 this.doSerSelectActDEC(req, res);
             else if (sSearchAC.equals("ValueDomain"))
                 this.doSerSelectActVD(req, res);
             else if (sSearchAC.equals("ValueMeaning"))
                 this.doSerSelectActVM(req, res);
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
     protected void doSearchSelectionBEAction() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         // gets the bean for the row selected
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         if (getACSearch.getSelRowToEdit(m_classReq, m_classRes, "BlockEdit") == false)
             ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
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
                 ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp");
             }
             else if (sSearchAC.equals("DataElementConcept"))
             {
                 DataManager.setAttribute(session, "originAction", "BlockEditDEC");
                 this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
                 ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
             }
             else if (sSearchAC.equals("ValueDomain"))
             {
                 DataManager.setAttribute(session, "vRepTerm", null);
                 DataManager.setAttribute(session, "newRepTerm", "");
                 DataManager.setAttribute(session, "originAction", "BlockEditVD");
                 ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
             }
             else
             {
                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create or Edit page.\\n"
                                 + "Please try again.");
                 ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
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
      * does the search selection action for the Value Meaning search forward the page according to the action
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doSerSelectActVM(HttpServletRequest req, HttpServletResponse res) throws Exception
     {
         HttpSession session = req.getSession();
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected menu
                                                                                                 // action
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         // make sure the menu action is for DE, set it otherwise
         if (sMenuAction.equalsIgnoreCase("NewDETemplate") || (sMenuAction.equalsIgnoreCase("NewDECTemplate")))
             sMenuAction = "NewVMTemplate";
         else if (sMenuAction.equalsIgnoreCase("NewDEVersion") || (sMenuAction.equalsIgnoreCase("NewDECVersion")))
             sMenuAction = "NewVMVersion";
         else if (sMenuAction.equalsIgnoreCase("editDE") || (sMenuAction.equalsIgnoreCase("editDEC")))
             sMenuAction = "editVM";
         // set the menuaction session attribute
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         // forward to create VM page if template or version
         if ((sMenuAction.equals("NewVMTemplate")) || (sMenuAction.equals("NewVMVersion")))
         {
             DataManager.setAttribute(session, "VMPageAction", "validate");
             ForwardJSP(req, res, "/CreateVMPage.jsp");
             DataManager.setAttribute(session, "originAction", "NewVM");
         }
         // forward to edit VM page if editing
         else if (sMenuAction.equals("editVM") || sMenuAction.equals("nothing"))
         {
         	DataManager.setAttribute(session, "originAction", "EditVM");
             ForwardJSP(req, res, "/ValueMeaningDetail.jsp");
         }
         else if (sButtonPressed.equals("Search"))
         {
             DataManager.setAttribute(session, "originAction", "EditVM");
             ForwardJSP(req, res, "/ValueMeaningDetail.jsp");
         }
         else
         {
             DataManager.setAttribute(session, "originAction", "EditVM");
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to open the Create or Edit page.\\n"
                             + "Please try again.");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
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
          logger.info("Log Status Message " + sMsg);
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
     protected void clearSessionAttributes(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
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
         DataManager.setAttribute(session, "VMForm.SESSION_RET_PAGE", null);
         // DataManager.setAttribute(session, "OpenTreeToConcept", "");
         
 
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
     protected void clearCreateSessionAttributes(HttpServletRequest req, @SuppressWarnings("unused") HttpServletResponse res) throws Exception
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
     	SQLHelper.closeConnection(m_conn);
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
     private void doLogout(HttpServletRequest req, HttpServletResponse res) throws Exception
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
             RequestDispatcher rd = this.m_servletContext.getRequestDispatcher(fullPage);
             rd.forward(req, res);
             return;
         }
         catch (Exception e)
         {
            // e.printStackTrace();
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
             String fullPage = "/";
             String reqMsg = (String) req.getAttribute("ReqErrorMessage");
             if (reqMsg != null && !reqMsg.equals(""))
                 errMsg = reqMsg;
             DataManager.setAttribute(session, "ErrorMessage", errMsg);
             if (!(errMsg).equals("Logged out."))
                fullPage = "/jsp/ErrorPage.jsp";
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
     protected void doRefDocumentUpload(HttpServletRequest req, HttpServletResponse res, String sOrigin)
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
 
     public void doOpenViewPage() throws Exception
     {
     	System.out.println("I am here open view page");
     	//read the parameters idseq, public id and version from the request
 		String acIDSEQ = m_classReq.getParameter("idseq");
 		String sValue = m_classReq.getParameter("id");
 		long publicID =  0; 
 		if (sValue != null)
 			publicID = Long.parseLong(sValue); 
 		sValue = m_classReq.getParameter("version");
 		double version = 0;
 		if (sValue != null)
 			version = Double.parseDouble(sValue);
 		
     	//query the ac table to get the actl name
 		String errMsg = "";
 		Admin_Components_Mgr acMgr = new Admin_Components_Mgr();
 		ArrayList<String> ac = null;
 		try {
 			ac = acMgr.getActlName(acIDSEQ, publicID, version, m_conn);
 		} catch (DBException e) {
 			logger.error("ac query", e);
 			errMsg = e.getMessage();
 		}
 		
 		//get the details for the selected AC
 		if (ac != null) {
 			String actlReq = "view"+ac.get(0);  //"DATAELEMENT";  //DE_CONCEPT ;  VALUEDOMAIN  ;  VALUEMEANING
 			acIDSEQ = ac.get(1);
 			if (acIDSEQ != null || !acIDSEQ.equals("")) {
 				m_classReq.setAttribute("acIdseq", acIDSEQ);
 				CurationServlet servObj = getACServlet(actlReq);
 				servObj.execute(getACType(actlReq));
 			} else {
 				errMsg = "Unable to determine the administered components used to view the data";
 				logger.error(errMsg);
 			}			
 		}
 		m_classReq.setAttribute("errMsg", errMsg);
      	ForwardJSP(m_classReq, m_classRes, "/ViewPage.jsp");
     }
 	
 	
 	public void execute(ACRequestTypes reqType) throws Exception {		
 		System.out.println("curation servlet");	
 		switch (reqType){
 			case view:
 				doOpenViewPage(); 
 				break;
 		}
 	}
 	
 	public AC_Bean getACNames(EVS_Bean newBean, String nameAct, AC_Bean pageAC) {
 		logger.debug("get name " + nameAct);
 		return pageAC;
 	}
 	public AC_Bean getACNames(String nameAct, String origin, AC_Bean bean) {
 		logger.debug("get name " + nameAct);
 		return bean;
 	}
 	
 	public AC_Bean getSystemName(AC_Bean ac, Vector<EVS_Bean> vParent) {
 		logger.debug("get name ");
 		return ac;
 	}
 
 	public ACRequestTypes getACType(String ac) {
 		ACRequestTypes acrt = ACRequestTypes.valueOf(ac);
 		return acrt;
 	}
 	
 	public CurationServlet getACServlet(String ac)
 	{
 		CurationServlet servObj = this;
     	try {
     		ACRequestTypes acrt = getACType(ac);
 			if (acrt != null)   
 			{
 			   String className = acrt.getClassName();
 			   servObj = (CurationServlet) Class.forName(className).newInstance();
 			   servObj.init(m_classReq, m_classRes, this.m_servletContext);
 			   servObj.setConn(this.m_conn);
 			   servObj.sessionData = this.sessionData;
 			}
 		} catch (Exception e) {
 			logger.error("Which AC " + e.toString());
 		}		
 		return servObj;
 	}
 
 } // end of class
