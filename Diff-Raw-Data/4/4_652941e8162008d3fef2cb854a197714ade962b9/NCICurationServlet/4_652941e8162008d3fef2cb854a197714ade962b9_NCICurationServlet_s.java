 package com.scenpro.NCICuration;
 
 
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 import java.awt.*;
 import javax.swing.*;
 import java.text.*;
 import java.net.URL;
 
 import gov.nih.nci.EVS.search.*;
 import gov.nih.nci.EVS.exception.*;
 import gov.nih.nci.common.util.*;
 import gov.nih.nci.common.exception.*;
 import gov.nih.nci.EVS.domain.*;
 
 import oracle.jdbc.driver.*;
 import oracle.jdbc.pool.OracleOCIConnectionPool;
 import oracle.jdbc.oci.OracleOCIConnection;
 
 import javax.servlet.http.HttpServletRequest;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.Connection;
 import java.util.Properties;
 import java.util.Hashtable;
 
 import org.apache.log4j.*;
 
 /**
  * The NCICurationServlet is the main servlet for communicating between the
  * client and the server.
  * <P>
  * @author Joe Zhou, Sumana Hegde, Tom Phillips
  * @version 3.0
  *
  */
 /*
 The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. (ScenPro)  
 Copyright Notice.  The software subject to this notice and license includes both
 human readable source code form and machine readable, binary, object code form
 (the CaCORE Software).  The CaCORE Software was developed in conjunction with
 the National Cancer Institute (NCI) by NCI employees and employees of SCENPRO.
 To the extent government employees are authors, any rights in such works shall
 be subject to Title 17 of the United States Code, section 105.    
 This CaCORE Software License (the License) is between NCI and You.  You (or Your)
 shall mean a person or an entity, and all other entities that control, are 
 controlled by, or are under common control with the entity.  Control for purposes
 of this definition means (i) the direct or indirect power to cause the direction
 or management of such entity, whether by contract or otherwise, or (ii) ownership
 of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial 
 ownership of such entity.  
 This License is granted provided that You agree to the conditions described below.
 NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge,
 irrevocable, transferable and royalty-free right and license in its rights in the
 CaCORE Software to (i) use, install, access, operate, execute, copy, modify, 
 translate, market, publicly display, publicly perform, and prepare derivative 
 works of the CaCORE Software; (ii) distribute and have distributed to and by 
 third parties the CaCORE Software and any modifications and derivative works 
 thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to 
 third parties, including the right to license such rights to further third parties.
 For sake of clarity, and not by way of limitation, NCI shall have no right of 
 accounting or right of payment from You or Your sublicensees for the rights 
 granted under this License.  This License is granted at no charge to You.
 1.	Your redistributions of the source code for the Software must retain the above
 copyright notice, this list of conditions and the disclaimer and limitation of
 liability of Article 6, below.  Your redistributions in object code form must
 reproduce the above copyright notice, this list of conditions and the disclaimer
 of Article 6 in the documentation and/or other materials provided with the 
 distribution, if any.
 2.	Your end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment: This product includes software developed 
 by SCENPRO and the National Cancer Institute.  If You do not include such end-user
 documentation, You shall include this acknowledgment in the Software itself, 
 wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" ScenPro, Inc.
 and "SCENPRO" to endorse or promote products derived from this Software.  
 This License does not authorize You to use any trademarks, service marks, trade names,
 logos or product names of either NCI or SCENPRO, except as required to comply with
 the terms of this License.
 4.	For sake of clarity, and not by way of limitation, You may incorporate this
 Software into Your proprietary programs and into any third party proprietary 
 programs.  However, if You incorporate the Software into third party proprietary
 programs, You agree that You are solely responsible for obtaining any permission
 from such third parties required to incorporate the Software into such third party
 proprietary programs and for informing Your sublicensees, including without 
 limitation Your end-users, of their obligation to secure any required permissions
 from such third parties before incorporating the Software into such third party
 proprietary software programs.  In the event that You fail to obtain such permissions,
 You agree to indemnify NCI for any claims against NCI by such third parties, 
 except to the extent prohibited by law, resulting from Your failure to obtain
 such permissions.
 5.	For sake of clarity, and not by way of limitation, You may add Your own 
 copyright statement to Your modifications and to the derivative works, and You 
 may provide additional or different license terms and conditions in Your sublicenses
 of modifications of the Software, or any derivative works of the Software as a 
 whole, provided Your use, reproduction, and distribution of the Work otherwise 
 complies with the conditions stated in this License.
 6.	THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
 NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  
 IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SCENPRO, OR THEIR AFFILIATES 
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 public class NCICurationServlet extends HttpServlet
 {
   //Attributes
   private Properties m_settings;
   private SetACService m_setAC = new SetACService(this);
  // private Connection m_conn = null;
   private  HttpServletRequest m_classReq = null;
   private HttpServletResponse m_classRes = null;
   static private Hashtable hashOracleOCIConnectionPool = new Hashtable();
   public String m_VOCAB_NCI = "NCI_Thesaurus";
   public String m_VOCAB_MED = "MedDRA";
   public String m_VOCAB_MGE = "MGED_Ontology";
   public String m_VOCAB_LOI = "LOINC";
   public String m_VOCAB_HL7 = "HL7_V3";
   public String m_VOCAB_UWD = "UWD_Visual_Anatomist";
   public String m_VOCAB_VA = "VA_NDFRT";
   public String m_VOCAB_GO = "GO";
   
   public Logger logger = Logger.getLogger(NCICurationServlet.class.getName());
 
 
   /**
   * To initialize global variables and load the Oracle
   * driver.
   *
   * @param config The ServletConfig object that has the server configuration
   * @throws ServletException Any exception that occurs during initialization
   */
   public void init(ServletConfig config) throws ServletException
   {
         super.init(config); 
         // Load the Oracle driver
         try
         {
             DriverManager.registerDriver(new OracleDriver());
         }
         catch (Exception e)
         {
             logger.fatal("Servlet-init : Unable to load Oracle driver : " + e.toString());
         }
         try
         {
           // Get the properties settings
 	 	      getProperties();  //Placeholder data for AC creation coming from CRT (JZ Realm Authen don't need property)
         }
         catch (Exception ee)
         {
             logger.fatal("Servlet-init : Unable to start curation tool : " + ee.toString());           
         }  
         //call the mehtod to make oracle connection
         this.initOracleConnect();
   }
 
   /**
    * makes the loggin message with all the information
    * @param req
    * @param sMethod string a function name this is called from
    * @param endMsg string a message to append to
    * @param bDate Date begin date to calculate teh duration
    * @param eDate date end date to calculate teh duration
    * @return 
    */
   public String getLogMessage(HttpServletRequest req, String sMethod, String endMsg,
      java.util.Date bDate, java.util.Date eDate)          
   {
     String sMsg = "";
     try
     {
       HttpSession session = req.getSession();
       //call this in utility service class
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
    * iniitilize the oracle connection
    */
   private void initOracleConnect()
   {
     try
     {
       logger.fatal("initOracleConnect - opening connection");
         String  stAppContext = "/cdecurate";
         String  stDBServerUrl = getServletConfig().getInitParameter("server");
         String  stDBLogin = getServletConfig().getInitParameter("username");
         String  stDBPassword = getServletConfig().getInitParameter("password");
         String  sMinConns = getServletConfig().getInitParameter("iMinConns");
         if(sMinConns == null) sMinConns = "1";
         Integer IiMinConns = new Integer(sMinConns);
         int iMinConns = IiMinConns.intValue();
         String  sMaxConns = getServletConfig().getInitParameter("iMaxConns");
          if(sMaxConns == null) sMaxConns = "5";
         Integer IiMaxConns = new Integer(sMaxConns);
         int iMaxConns = IiMaxConns.intValue();
 
         OracleOCIConnectionPool cpool = (OracleOCIConnectionPool)hashOracleOCIConnectionPool.get(stAppContext);
         if (cpool != null) return;
         // Create database pool
         try 
         {
             cpool = connect(stDBServerUrl,stDBLogin,stDBPassword,iMinConns,iMaxConns);
             hashOracleOCIConnectionPool.put(stAppContext, cpool);
         }
         catch (Exception e) 
         {
             String stErr = "Error creating database pool[" + e.getMessage() + "].";
             e.printStackTrace();
             //System.out.println(stErr);
             logger.fatal(stErr);
         }
 
         // Test connnection
         Connection con = null;
         Statement stmt = null;
         ResultSet rset = null;
         try	
         {
             con = getConnection(stAppContext, stDBLogin, stDBPassword);
             stmt = con.createStatement();
             rset = stmt.executeQuery("Select sysdate from dual");
             if (rset.next())
                 rset.getString(1);
             else
                 throw (new Exception("DBPool connection test failed."));
         }
         catch(Exception e) 
         {
             //System.err.println("Could not open database connection.");
             e.printStackTrace();
             logger.fatal(e.toString());
         }
         finally 
         {
             try {rset.close();} catch (Exception e) {};
             try {stmt.close();} catch (Exception e) {};
             freeConnection(con);
         }
     }
     catch (Exception e)
     {
       logger.fatal("initOracleConnect - Some other error");
     }
   }
   /**
    * makes connection pool
    * @param stDBServerUrl string server url
    * @param stDBLogin string database to login to
    * @param stDBPassword string password to connect to
    * @param iMinConns int number for min connection
    * @param iMaxConns int number for max connection
    * @return OracleOCIConnectionPool object
    * @throws java.lang.Exception
    */
   static private OracleOCIConnectionPool connect(String  stDBServerUrl, String stDBLogin,
         String  stDBPassword, int iMinConns, int iMaxConns) throws Exception 
   {
      //   System.out.println("Creating new OracleOCIConnectionPool.");
         Properties p = new Properties();
         p.put(OracleOCIConnectionPool.CONNPOOL_MIN_LIMIT, Integer.toString(iMinConns));
         p.put(OracleOCIConnectionPool.CONNPOOL_MAX_LIMIT, Integer.toString(iMaxConns));
         p.put(OracleOCIConnectionPool.CONNPOOL_INCREMENT, "1");
 
         return(new OracleOCIConnectionPool(stDBLogin, stDBPassword, stDBServerUrl, p));
   }
 
  /**
    * gets the connection object from pool 
    * @param stDBAppContext
    * @param stUser
    * @param stPassword
    * @return 
    * @throws java.lang.Exception
    */
   static public Connection getConnection(String stDBAppContext, String stUser, String stPassword) throws Exception 
   {
         OracleOCIConnectionPool cpool = (OracleOCIConnectionPool)hashOracleOCIConnectionPool.get(stDBAppContext);
         if (cpool == null)
         {
           return(null);
         }
         return(cpool.getConnection(stUser, stPassword));
   }    
 
   /**
    * free up the connection
    * @param con
    */
     static public void freeConnection(Connection con) {
         try { con.close(); } catch (Exception e) {}
     }
     
  //   static public void destroy() {
         // Do not destroy the connection pool since it may be in use by
         // many other servlets.  To close the global pool, you must
         // restart the server.
  //   }
  
   
    /**
   * This method tries to connect to the db, returns the Connection object if
   * successful, if unsuccessful tries to reconnect, returns null if unsuccessful.
   * Called from various classes needing a connection.
   * forwards page 'CDELoginPageError2.jsp'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @return Connection SBRDb_conn
   * 04/15/03 JZ Implement Realm Authen connction
   */
   public Connection connectDB(HttpServletRequest req, HttpServletResponse res)
   {
       Connection SBRDb_conn = null;
       HttpSession session = req.getSession();
       try
       {
         String username  = "";
         String password  = "";
         String sDBAppContext = "/cdecurate";
         UserBean Userbean  = new UserBean();
         Userbean  = (UserBean)session.getAttribute("Userbean");
         if(Userbean != null)
         {
           username  = Userbean.getUsername();
           password  = Userbean.getPassword();
           sDBAppContext = Userbean.getDBAppContext();
         }
         try
         {
           SBRDb_conn = this.getConnection(sDBAppContext, username, password);
         }
         catch(Exception e)
         {
           logger.fatal("Servlet error: no pool connection.");
         }
       }
       catch (Exception e)
       {
           logger.fatal("Servlet connectDB : "  + e.toString());
       }
       return SBRDb_conn;
   } 
 
     // Now store the cache in the application context for all to use
   //  this.getServletContext().setAttribute("cdecuration.connectionPool", occi); 
  // }
 
   /**
    * The service method services all requests to this servlet.
    *
    * @param request The HttpServletRequest object that contains the request
    * @param response The HttpServletResponse object that contains the response
    *
    */
   public void service(HttpServletRequest req, HttpServletResponse res)
   {
     Connection conn = null;
     boolean userBeanExists = true;
     HttpSession session;
     m_classReq = req;
     session = req.getSession(true);
     try
     {
       String reqType = req.getParameter("reqType");
 //logger.debug("servlet reqType!: "+ reqType);
 //System.out.println("servlet reqType!: "+ reqType);
       session.setAttribute("LatestReqType", reqType); 
       if (reqType != null)
       {
         //check the validity of the user login
         if(reqType.equals("login"))
         {
             doHomePage(req,res);
         }
         else if (!reqType.equals("login"))
         {
           userBeanExists = checkUserBean(req, res, userBeanExists);
         }
         //do the requests
         if(userBeanExists == true)
         {
           java.util.Date startDate = new java.util.Date();          
           logger.info(this.getLogMessage(req, "service", "started Request " + reqType, startDate, startDate));
           if(reqType.equals("homePage"))
           {
             doHomePage(req,res);
           }
           else if(reqType.equals("newDEFromMenu"))
           {
             doOpenCreateNewPages(req,res,"de");
           }
           else if(reqType.equals("newDECFromMenu"))
           {
             doOpenCreateNewPages(req,res,"dec");
           }
            else if(reqType.equals("newVDFromMenu"))
           {
             doOpenCreateNewPages(req,res,"vd");
           }
           else if(reqType.equals("newDEfromForm"))
           {
             doCreateDEActions(req,res);
           }
            else if(reqType.equals("newDECfromForm"))  //when DEC form is submitted
           {
             doCreateDECActions(req,res);
           }
            else if(reqType.equals("newVDfromForm")) //when Edit VD form is submitted
           {
             doCreateVDActions(req,res);
           }
           else if(reqType.equals("editDE")) //when Edit DE form is submitted
           {
             doEditDEActions(req,res);
           }
            else if(reqType.equals("editDEC"))  //when Edit DEC form is submitted
           {
             doEditDECActions(req,res);
           }
            else if(reqType.equals("editVD"))
           {
             doEditVDActions(req,res);
           }
           else if(reqType.equals("newPV")) // fromForm
           {
             doCreatePVActions(req,res);
           }
            else if(reqType.equals("newVM")) // fromForm
           {
             doCreateVMActions(req,res);
           }
           else if(reqType.equals("createPV") || reqType.equals("editPV"))
           {
             doOpenCreatePVPage(req, res, reqType, "");
           }
           else if(reqType.equals("createNewDEC"))
           {
             doOpenCreateDECPage(req, res);
           }
           else if(reqType.equals("createNewVD"))
           {
             doOpenCreateVDPage(req, res);
           }
           else if(reqType.equals("validateDEFromForm"))
           {
             doInsertDE(req,res);
           }
           else if(reqType.equals("validateDECFromForm"))
           {
             doInsertDEC(req,res);
           }
           else if(reqType.equals("validateVDFromForm"))
           {
             doInsertVD(req,res);
           }
            else if(reqType.equals("validatePVFromForm"))
           {
             doInsertPV(req,res);
           }
            else if(reqType.equals("validateVMFromForm"))
           {
             doInsertVM(req,res);
           }
           else if(reqType.equals("searchACs"))
           {
             doGetACSearchActions(req,res);      //req for search parameters page
           }
           else if(reqType.equals("showResult"))
           {
             doSearchResultsAction(req,res);  //req from search results page
           }
            else if(reqType.equals("showBEDisplayResult"))
           {
             doDisplayWindowBEAction(req,res);  //req from search results page  showBEDisplayResult
           }
           else if(reqType.equals("doSortCDE"))
           {
             doSortACActions(req,res);  //on sort by heading for search
           }
           else if(reqType.equals("doSortBlocks"))
           {
             doSortBlockActions(req,res, "Blocks");  //on sort by heading for search
           }
           else if(reqType.equals("doSortQualifiers"))
           {
             doSortBlockActions(req,res, "Qualifiers");  //on sort by heading for search
           }
           else if(reqType.equals("getSearchFilter"))
           {
             doOpenSearchPage(req,res);  //on click on the search from menu
           }
           else if(reqType.equals("actionFromMenu"))
           {
             doMenuAction(req, res);      //on click on the edit/create from menu
           }
           else if(reqType.equals("errorPageForward"))
           {
             doJspErrorAction(req, res);      //on click on the edit/create from menu
           }
           else if(reqType.equals("logout"))
           {
             doLogout(req, res);
           }
           else if(reqType.equals("searchEVS"))
           {
             doSearchEVS(req, res);
           }
           else if(reqType.equals("searchBlocks"))
           {
             doBlockSearchActions(req, res);
           }
           else if(reqType.equals("searchQualifiers"))
           {
             doQualifierSearchActions(req, res);
           }
           //get more records of Doc Text
           else if(reqType.equals("getRefDocument"))
           {
             this.doRefDocSearchActions(req, res);
           }
           //get more records of alternate names
           else if(reqType.equals("getAltNames"))
           {
             this.doAltNameSearchActions(req, res);
           }
           //get more records of permissible values
           else if(reqType.equals("getPermValue"))
           {
             this.doPermValueSearchActions(req, res);
           }
           //get more records of protocol crf
           else if(reqType.equals("getProtoCRF"))
           {
             this.doProtoCRFSearchActions(req, res);
           }
           else if(reqType.equals("treeSearch"))
           {
             doTreeSearchRequest(req, res, "", "", "", "");
           }
           else if(reqType.equals("treeRefresh"))
           {
             doTreeRefreshRequest(req, res);
           }
           else if(reqType.equals("treeExpand"))
           {
             doTreeExpandRequest(req, res);
           }
           else if(reqType.equals("treeCollapse")) 
           {
             doTreeCollapseRequest(req, res);
           }
           else if(reqType.equals("getSubConcepts")) 
           {
             doGetSubConcepts(req, res);
           }
           else if(reqType.equals("getSuperConcepts")) 
           {
             doGetSuperConcepts(req, res);
           }
           else if (reqType.equals("designateDE"))
           {
             this.doEditDesDEActions(req, res, "Edit");
           }
           else if (reqType.equals("nonEVSSearch"))
           {
             this.doNonEVSPageAction(req, res);
           }
           //log message
           java.util.Date endDate = new java.util.Date();
           logger.info(this.getLogMessage(req, "service", "ended Request", startDate, endDate));
         }
         else if(!reqType.equals("login"))
         {
           String errMsg = "Please login again. Your session has been terminated. Possible reasons could be a session timeout or an internal processing error.";
           session.setAttribute("ErrorMessage", errMsg);
           ForwardErrorJSP(req, res, errMsg); 
         }
       }
       else
       {
         this.logger.fatal("Service: no DB Connection");
         ErrorLogin(req, res);
       }
     }
     catch(Exception e)
     {
       logger.fatal("Service error : " + e.toString());
       session = req.getSession();
       String msg = e.toString();
       try
       {
         if (msg != null)
           ForwardErrorJSP(req, res, msg);
         else
           ForwardErrorJSP(req, res, "A page error has occurred. Please login again.");
       }
       catch(Exception ee)
       {
           logger.fatal("Service forward error : " + ee.toString());
       }
     }
   }  // end of service
 
   /**
   * The checkUserBean method gets the session then checks whether a Userbean exits.
   * Called from 'service' method.
   *
   * @param request The HttpServletRequest from the client
   * @param response The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private boolean checkUserBean(HttpServletRequest req,
     HttpServletResponse res, boolean userBeanExists) throws Exception
   {
     HttpSession session;
     session = req.getSession(true);
     UserBean userbean  = (UserBean)session.getAttribute("Userbean");
     String reqType = req.getParameter("reqType");
     if(userbean == null)
     {
       userBeanExists = false;
       logger.fatal("User bean is null");
   //    ForwardErrorJSP(req, res, "Please login again. Your session has been terminated. Possible reasons could be a session timeout or an internal processing error."); 
     }
     else
       userBeanExists = true;
     return userBeanExists;
   }
 
   /**
   * The doOpenCreateNewPages method will set some session attributes then forward the request to a Create page.
   * Called from 'service' method where reqType is 'newDEFromMenu', 'newDECFromMenu', 'newVDFromMenu'
   * Sets some intitial session attributes.
   * Calls 'getAC.getACList' to get the Data list from the database for the selected context.
   * Sets session Bean and forwards the create page for the selected component.
   *
   * @param request The HttpServletRequest from the client
   * @param response The HttpServletResponse back to the client
   * @param sACType The type of page being called, i.e. de, dec, or vd
   *
   * @throws Exception
   */
   private void doOpenCreateNewPages(HttpServletRequest req,
     HttpServletResponse res, String sACType) throws Exception
   {
       GetACService getAC = new GetACService(req, res, this);  //
       HttpSession session = req.getSession();
       clearSessionAttributes(req, res);
       String context = (String)session.getAttribute("sDefaultContext");  // from Login.jsp
       String ContextInList = (String)session.getAttribute("ContextInList");
       session.setAttribute("MenuAction", "nothing");
 
       String sOriginAction = (String)session.getAttribute("originAction");
       session.setAttribute("sCDEAction", "nothing");
       session.setAttribute("VDPageAction", "nothing");
       session.setAttribute("DECPageAction", "nothing");
       session.setAttribute("sDefaultContext", context);
       if(sACType.equals("de"))
       {
         DE_Bean m_DE = new DE_Bean();
         m_DE.setDE_ASL_NAME("DRAFT NEW");
         m_DE.setAC_PREF_NAME_TYPE("SYS");
         session.setAttribute("m_DE", m_DE);
         DE_Bean oldDE = new DE_Bean();
         oldDE.setDE_ASL_NAME("DRAFT NEW");
         oldDE.setAC_PREF_NAME_TYPE("SYS");
         session.setAttribute("oldDEBean", oldDE);
         session.setAttribute("originAction", "NewDEFromMenu");
         session.setAttribute("LastMenuButtonPressed", "CreateDE");
         doInitDDEInfo(req, res);
 
         ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       else if(sACType.equals("dec"))
       {
         session.setAttribute("originAction", "NewDECFromMenu");
         session.setAttribute("LastMenuButtonPressed", "CreateDEC");
         DEC_Bean m_DEC = new DEC_Bean();
         m_DEC.setDEC_ASL_NAME("DRAFT NEW");
         m_DEC.setAC_PREF_NAME_TYPE("SYS");
         session.setAttribute("m_DEC", m_DEC);
         DEC_Bean oldDEC = new DEC_Bean();
         oldDEC.setDEC_ASL_NAME("DRAFT NEW");
         oldDEC.setAC_PREF_NAME_TYPE("SYS");
         session.setAttribute("oldDECBean", oldDEC);
         EVS_Bean m_OC = new EVS_Bean();
         session.setAttribute("m_OC", m_OC);
         EVS_Bean m_PC = new EVS_Bean();
         session.setAttribute("m_PC", m_PC);
         EVS_Bean m_OCQ = new EVS_Bean();
         session.setAttribute("m_OCQ", m_OCQ);
         EVS_Bean m_PCQ = new EVS_Bean();
         session.setAttribute("m_PCQ", m_PCQ);
         session.setAttribute("selPropRow", "");
         session.setAttribute("selPropQRow", "");
         session.setAttribute("selObjQRow", "");
         session.setAttribute("selObjRow", "");
         ForwardJSP(req, res, "/CreateDECPage.jsp");
       }
       else if(sACType.equals("vd"))
       {
         session.setAttribute("originAction", "NewVDFromMenu");
         session.setAttribute("LastMenuButtonPressed", "CreateVD");
         VD_Bean m_VD = new VD_Bean();
         m_VD.setVD_ASL_NAME("DRAFT NEW");
         m_VD.setAC_PREF_NAME_TYPE("SYS");
         session.setAttribute("m_VD", m_VD);
         VD_Bean oldVD = new VD_Bean();
         oldVD.setVD_ASL_NAME("DRAFT NEW");
         oldVD.setAC_PREF_NAME_TYPE("SYS");
         session.setAttribute("oldVDBean", oldVD);
         EVS_Bean m_OC = new EVS_Bean();
         session.setAttribute("m_OC", m_OC);
         EVS_Bean m_PC = new EVS_Bean();
         session.setAttribute("m_PC", m_PC);
         EVS_Bean m_REP = new EVS_Bean();
         session.setAttribute("m_REP", m_REP);
         EVS_Bean m_OCQ = new EVS_Bean();
         session.setAttribute("m_OCQ", m_OCQ);
         EVS_Bean m_PCQ = new EVS_Bean();
         session.setAttribute("m_PCQ", m_PCQ);
         EVS_Bean m_REPQ = new EVS_Bean();
         session.setAttribute("m_REPQ", m_REPQ);
         //empty the session attributes
         session.setAttribute("VDPVList", new Vector());
         session.setAttribute("PVIDList", new Vector());     
         ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
       else if(sACType.equals("pv"))
       {
         ForwardJSP(req, res, "/CreatePVPage.jsp");
       }
   }  //end of doOpenCreateNewPages
 
 
   /**
   * The getProperties method sets up some default properties and then looks
   * for the NCICuration.properties file to override the defaults.
   * Called from 'init' method.
   */
   private void getProperties()
   {
       Properties defaultProperties;
       InputStream input;
 
       // Set the defaults first
       defaultProperties = new Properties();
       defaultProperties.put("DEDefinition", "Please provide definition.");
       defaultProperties.put("VDDefinition", "Please provide definition.");
       defaultProperties.put("DataType", "CHARACTER");
       defaultProperties.put("MaxLength", "200");
       
   		// Now read the properties file for any changes
 	  	m_settings = new Properties(defaultProperties);
       try
   		{
 	  	 	input = NCICurationServlet.class.getResourceAsStream("NCICuration.properties");
 		  	m_settings.load(input);
 	  	}
 	  	catch(Exception e)
 		{
             //System.err.println("ERROR - Got exception reading properties " + e);
             logger.fatal("Servlet-getProperties : " + e.hashCode() + " : " + e.toString());
   	   }
   }  // end getProperties
 
 
   /**
    * Get Servlet information
    * @return java.lang.String
    */
   public String getServletInfo()
   {
     return "com.scenpro.NCICuration.NCICuration Information";
   }
 
 //////////////////////////////////////////////////////////////////////////////////////
   /**
   * The doLogin method forwards to CDEHomePage.jsp.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doLogin(HttpServletRequest req,
     HttpServletResponse res) throws Exception
   {
       ForwardJSP(req, res, "/CDEHomePage.jsp");
   }
   
 
   /**
   * The doHomePage method gets the session, set some session attributes, then
   * connects to the database.
   * Called from 'service' method where reqType is 'login', 'homePage'
   * calls 'getAC.getACList' to get the Data list from the database for the selected Context at login page.
   * calls 'doOpenSearchPage' to open the home page.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * 04/15/03 Implement Realm Authentication connection
   *
   */
    public void doHomePage(HttpServletRequest req, HttpServletResponse res)
    {
     try
     {
     
         DescLogicConcept dlc = new DescLogicConcept();
 	
         HttpSession session = req.getSession();
         session.setAttribute("MenuAction", "nothing");
         session.setAttribute("originAction", "nothing");
         session.setAttribute("VMMeaning", "nothing");
         session.setAttribute("ConnectedToDB", "nothing");
         req.setAttribute("UISearchType", "nothing"); 
         req.setAttribute("OpenTreeToConcept", "false");
         session.setAttribute("strHTML", ""); 
         session.setAttribute("creSearchAC", "");
         session.setAttribute("ParentConcept", "");
         session.setAttribute("SelectedParentName", "");
         session.setAttribute("SelectedParentCC", "");
         session.setAttribute("SelectedParentDB", "");
         session.setAttribute("SelectedParentMetaSource", "");
         session.setAttribute("ConceptLevel", "0");
       
         String Username = req.getParameter("Username");
         String Password = req.getParameter("Password");
         UserBean Userbean = new UserBean();
         Userbean.setUsername(Username);
         Userbean.setPassword(Password);
         Userbean.setDBAppContext("/cdecurate");
         session.setAttribute("Userbean", Userbean);
         session.setAttribute("Username", Username);
         session.setAttribute("sDefaultStatus", "DRAFT NEW");
         session.setAttribute("statusMessage", "");   
         GetACService getAC = new GetACService(req, res, this);
         getAC.getACList(req, res, "", true, "ALL");
         String ConnectedToDB = (String)session.getAttribute("ConnectedToDB");
       
         if (!ConnectedToDB.equals("No"))
         {
           getVocabHandles(req, res);
           DoHomepageThread thread = new DoHomepageThread(req, res, this);
           thread.start();
           doOpenSearchPage(req, res);
           getCompAttrList(req, res, "DataElement", "searchForCreate");
         }
         else
         {  
            session.setAttribute("ConnectedToDB", "nothing"); // was No, so reset value
            ForwardErrorJSP(req, res, "Unable to connect to the database. Please log in again.");
         }
     }
     catch (Exception e)
     {
       try
       {
        // if error, forward to login page to re-enter username and password
         logger.fatal("Servlet-doHomePage : " + e.toString());
         String msg = e.getMessage().substring(0, 12);
         if(msg.equals("Io exception"))
           ForwardErrorJSP(req, res, "Session terminated. Please log in again.");
         else
            ForwardErrorJSP(req, res, "Incorrect Username or Password. Please re-enter.");
        }
       catch (Exception ee)
       {
         logger.fatal("Servlet-doHomePage, display error page : " + ee.toString());
       }
     }
   }
 
    /**
    * To get final result vector of selected attributes/rows to display for Rep Term component,
    * gets the selected attributes from session vector 'selectedAttr'.
    * loops through the RepBean vector 'vACSearch' and adds the selected fields to result vector.
    *
    * @param req The HttpServletRequest object.
    * @param res HttpServletResponse object.
    *
    */
   public void getVocabHandles(HttpServletRequest req, HttpServletResponse res)
   {
     try
     {   
       gov.nih.nci.EVS.domain.DescLogicConcept dlc = null;
       dlc = new gov.nih.nci.EVS.domain.DescLogicConcept(); 
       Source[] srcCollection = dlc.getSources();
       String vocab = "";
       for(int i=0; i<srcCollection.length; i++)
       {
         vocab = srcCollection[i].getAbbreviation();
         if(vocab.substring(0,2).equalsIgnoreCase("NC"))
           m_VOCAB_NCI = vocab;
         else if(vocab.substring(0,2).equalsIgnoreCase("Me"))
           m_VOCAB_MED = vocab;
         else if(vocab.substring(0,2).equalsIgnoreCase("MG"))
           m_VOCAB_MGE = vocab;
         else if(vocab.substring(0,2).equalsIgnoreCase("LO"))
           m_VOCAB_LOI = vocab;
         else if(vocab.substring(0,2).equalsIgnoreCase("HL"))
           m_VOCAB_HL7 = vocab;
         else if(vocab.substring(0,2).equalsIgnoreCase("UW"))
           m_VOCAB_UWD = vocab;
         else if(vocab.substring(0,2).equalsIgnoreCase("VA"))
           m_VOCAB_VA = vocab;
         else if(vocab.substring(0,2).equalsIgnoreCase("GO"))
           m_VOCAB_GO = vocab;
       }  
     }
     catch(Exception e)
     {
       this.logger.fatal("ERROR in Servlet-getVocabHandles : " + e.toString());
     }
   }
 
 
   /**
   * The doCreateDEActions method handles CreateDE or EditDE actions of the request.
   * Called from 'service' method where reqType is 'newDEfromForm'
   * Calls 'ValidateDE' if the action is Validate or submit.
   * Calls 'doSuggestionDE' if the action is open EVS Window.
   * Calls 'doOpenCreateDECPage' if the action is create new DEC from DE Page.
   * Calls 'doOpenCreateVDPage' if the action is create new VD from VD Page.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doCreateDEActions(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       String sPageAction = (String)req.getParameter("newCDEPageAction");
 
       if (sPageAction != null)
         session.setAttribute("sCDEAction", sPageAction);
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
 
       String sSubAction = (String)req.getParameter("DEAction");
       session.setAttribute("DEAction", sSubAction);
 
       String sOriginAction = (String)session.getAttribute("originAction");
       // save DDE info every case except back from DEComp
       String ddeType = (String)req.getParameter("selRepType");
       if (ddeType != null && !ddeType.equals(""))
         doUpdateDDEInfo(req, res);     
       if(sPageAction.equals("changeContext"))
         doChangeContext(req, res, "de");
       else if(sPageAction.equals("submit"))
         doSubmitDE(req, res);
       else if(sPageAction.equals("validate"))
         doValidateDE(req, res);
       else if(sPageAction.equals("suggestion"))
         doSuggestionDE(req, res);
       else if (sPageAction.equals("updateNames"))
       {
         DE_Bean de = this.doGetDENames(req, res, "new", "Search", null);
         ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       else if (sPageAction.equals("changeNameType"))
       {
         this.doChangeDENameType(req, res, "changeType");
         ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       else if(sPageAction.equals("createNewDEC"))
       {
         session.setAttribute("originAction", "CreateNewDECfromCreateDE");
         doOpenCreateDECPage(req, res);
       }
       else if(sPageAction.equals("createNewVD"))
       {
         session.setAttribute("originAction", "CreateNewVDfromCreateDE");
         doOpenCreateVDPage(req, res);
       }
       else if(sPageAction.equals("CreateNewDEFComp"))
         doOpenCreateDECompPage(req, res);
       else if(sPageAction.equals("DECompBackToNewDE") ||sPageAction.equals("DECompBackToEditDE"))
         doOpenDEPageFromDEComp(req, res);
       else if (sPageAction.equals("clearBoxes"))
       {
          DE_Bean DEBean = (DE_Bean)session.getAttribute("oldDEBean");
          String sDEID = DEBean.getDE_DE_IDSEQ();
          String sVDid = DEBean.getDE_VD_IDSEQ();
          Vector vList = new Vector();           
          //get VD's attributes from the database again
          GetACSearch serAC = new GetACSearch(req, res, this);
          if (sDEID != null && !sDEID.equals(""))
          {
             serAC.doDESearch(sDEID, "", "","","","", 0, "", "", "", "", "", "", "", "", "", 
                       "", "", "", "", "", "", "", "", "", "", "", "", vList);
          }
          if (vList.size() > 0)   //get all attributes
          {
            DEBean = (DE_Bean)vList.elementAt(0);
            DEBean = serAC.getDEAttributes(DEBean, sOriginAction, sMenuAction);
            serAC.getDDEInfo(DEBean.getDE_DE_IDSEQ());  //clear dde 
          }                  
          else if (sVDid == null || sVDid.equals(""))
          {
            DEBean = new DE_Bean();
            DEBean.setDE_ASL_NAME("DRAFT NEW");
            DEBean.setAC_PREF_NAME_TYPE("SYS");
            this.doInitDDEInfo(req, res);
          }
          DE_Bean pgBean = new DE_Bean();
          session.setAttribute("m_DE", pgBean.cloneDE_Bean(DEBean, "Complete"));
          ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       else if (sPageAction.equals("backToSearch"))
       {
          GetACSearch serAC = new GetACSearch(req, res, this);
            //forword to search page with de search results
          if (sMenuAction.equals("NewDETemplate") || sMenuAction.equals("NewDEVersion"))
          {
             DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
             serAC.refreshData(req, res, DEBean, null, null, null, "Refresh", "");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
           //forward to search page with questions search results
          else if (sMenuAction.equals("Questions"))
          {
             Quest_Bean QuestBean = (Quest_Bean)session.getAttribute("m_Quest");
             serAC.refreshData(req, res, null, null, null, QuestBean, "Refresh", "");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
          else
             ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
   }
 
   /**
   * The doEditDEActions method handles EditDE actions of the request.
   * Called from 'service' method where reqType is 'EditDEfromForm'
   * Calls 'ValidateDE' if the action is Validate or submit.
   * Calls 'doSuggestionDE' if the action is open EVS Window.
   * Calls 'doOpenDECForEdit' if the action is Edit DEC from EditDE Page.
   * Calls 'doOpenVDForEdit' if the action is Edit VD from EditDE Page.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doEditDEActions(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       String sPageAction = (String)req.getParameter("newCDEPageAction");
       if (sPageAction != null)
         session.setAttribute("sCDEAction", sPageAction);
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
       String sSubAction = (String)req.getParameter("DEAction");
       session.setAttribute("DEAction", sSubAction);
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
       String sOriginAction = (String)session.getAttribute("originAction");
 
       // save DDE info every case except back from DEComp
       String ddeType = (String)req.getParameter("selRepType");
       if (ddeType != null && !ddeType.equals(""))
         doUpdateDDEInfo(req, res);
       if(sPageAction.equals("submit"))
           doSubmitDE(req, res);
       else if(sPageAction.equals("validate") && sOriginAction.equals("BlockEditDE"))
           doValidateDEBlockEdit(req, res);
       else if (sPageAction.equals("validate"))
           doValidateDE(req, res);
       else if(sPageAction.equals("suggestion"))
           doSuggestionDE(req, res);
       else if(sPageAction.equals("EditDECfromDE"))
       {
           session.setAttribute("originAction", "editDECfromDE");
           doOpenEditDECPage(req, res);
       }
       else if(sPageAction.equals("createNewDEC"))
       {
           session.setAttribute("originAction", "CreateNewDECfromEditDE");
           this.doOpenCreateDECPage(req, res);
       }
       else if(sPageAction.equals("createNewVD"))
       {
           session.setAttribute("originAction", "CreateNewVDfromEditDE");
           this.doOpenCreateVDPage(req, res);
       }
       else if(sPageAction.equals("EditVDfromDE"))
       {
           session.setAttribute("originAction", "editVDfromDE");
           doOpenEditVDPage(req, res);
       }
       else if (sPageAction.equals("updateNames"))
       {
         DE_Bean de = this.doGetDENames(req, res, "append", "Search", null);
         ForwardJSP(req, res, "/EditDEPage.jsp");
       }
       else if (sPageAction.equals("changeNameType"))
       {
         this.doChangeDENameType(req, res, "changeType");
         ForwardJSP(req, res, "/EditDEPage.jsp");
       }
       else if (sPageAction.equals("clearBoxes"))
       {
           DE_Bean DEBean = (DE_Bean)session.getAttribute("oldDEBean");
           String sDEID = DEBean.getDE_DE_IDSEQ();
           Vector vList = new Vector();           
           //get VD's attributes from the database again
           GetACSearch serAC = new GetACSearch(req, res, this);
           if (sDEID != null && !sDEID.equals(""))
             serAC.doDESearch(sDEID, "", "","","","", 0, "", "", "", "", "", "", "", "", "", 
                         "", "", "", "", "", "", "", "", "", "", "", "", vList);
           if (vList.size() > 0)   //get all attributes
           {
              DEBean = (DE_Bean)vList.elementAt(0);
              DEBean = serAC.getDEAttributes(DEBean, sOriginAction, sMenuAction);
              serAC.getDDEInfo(DEBean.getDE_DE_IDSEQ());  //clear dde 
           }
           else
           {
             this.doInitDDEInfo(req, res);
           }
           DE_Bean pgBean = new DE_Bean();
           session.setAttribute("m_DE", pgBean.cloneDE_Bean(DEBean, "Complete"));
           ForwardJSP(req, res, "/EditDEPage.jsp");
       }
       else if (sPageAction.equals("backToSearch"))
       {
            GetACSearch serAC = new GetACSearch(req, res, this);
             //forward to search page with questions search results
            if (sMenuAction.equals("Questions"))
            {
               Quest_Bean QuestBean = (Quest_Bean)session.getAttribute("m_Quest");
               serAC.refreshData(req, res, null, null, null, QuestBean, "Refresh", "");
               ForwardJSP(req, res, "/SearchResultsPage.jsp");
            }
            else if (sMenuAction.equalsIgnoreCase("editDE") || sOriginAction.equalsIgnoreCase("BlockEditDE") || sButtonPressed.equals("Search")
                     || sOriginAction.equalsIgnoreCase("EditDE"))  // || sMenuAction.equals("EditDesDE"))
            {
               DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
               Vector vResult = new Vector();
               serAC.getDEResult(req, res, vResult, "");
               session.setAttribute("results", vResult);
               ForwardJSP(req, res, "/SearchResultsPage.jsp");
            }
            else
               ForwardJSP(req, res, "/EditDEPage.jsp");
       }
       else if(sPageAction.equals("CreateNewDEFComp"))
       {
           doOpenCreateDECompPage(req, res);
       }
   }  // end of doEditDEActions
 
   /**
    * this method is used to create preferred name for DE 
    * it gets the selected DEC or VD bean from the search result and stores it in DE if from teh search.
    * if from create or edit dec or vd, it refreshes DE bean with new dec or vd.
    * names of all three types will be stored in the bean for later use
    * if type is changed, it populates name according to type selected. 
    * 
    * @param req HttpServletRequest
    * @param res HttpServletResponse
    * @param nameAct string new name or apeend name 
    * @param sOrigin String what changed to make this name creation
    * @param pageDE current de bean
    * @throws java.lang.Exception
    */
   public DE_Bean doGetDENames(HttpServletRequest req, HttpServletResponse res, 
       String nameAct, String sOrigin, DE_Bean pageDE) throws Exception
   {
     HttpSession session = req.getSession();
     if (pageDE == null) pageDE = (DE_Bean)session.getAttribute("m_DE");
     //get other de attributes from page
     if (sOrigin.equals("Search"))
       m_setAC.setDEValueFromPage(req, res, pageDE);
 
     String sSysName = pageDE.getAC_SYS_PREF_NAME();
     String sAbbName = pageDE.getAC_ABBR_PREF_NAME();
     //store teh page one if typed ealier
     String selNameType = pageDE.getAC_PREF_NAME_TYPE();
     if (selNameType == null) selNameType = "";
     if (sOrigin.equals("Search") || sOrigin.equals("Remove"))
     {
       selNameType = (String)req.getParameter("rNameConv");
       if (selNameType == null) selNameType = "";
       String sPrefName = (String)req.getParameter("txPreferredName");
       if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
         pageDE.setAC_USER_PREF_NAME(sPrefName);
     }
     String sUsrName = pageDE.getAC_USER_PREF_NAME();
     //get other attrs
     sSysName = "";
     sAbbName = "";
     DEC_Bean deDEC = pageDE.getDE_DEC_Bean();
     VD_Bean deVD = pageDE.getDE_VD_Bean();
     String sDef = pageDE.getDE_PREFERRED_DEFINITION();
     //get selected row from the search results
     if (sOrigin.equals("Search"))
     {
       String acSearch = (String)req.getParameter("acSearch");
       if (acSearch == null) acSearch = "ALL";
   logger.debug(sOrigin + "searched " + acSearch);
       //get the result vector from the session
       Vector vRSel = (Vector)session.getAttribute("vACSearch");
       if (vRSel == null) vRSel = new Vector();
       //get the array from teh hidden list
       String selRows[] = req.getParameterValues("hiddenSelRow");
       if (selRows == null)
         session.setAttribute("StatusMessage", "Unable to select Row, please try again");    
       else
       {
         //loop through the array of strings
         for (int i=0; i<selRows.length; i++)
         {
           String thisRow = selRows[i];
           Integer IRow = new Integer(thisRow);
           int iRow = IRow.intValue();
           if (iRow < 0 || iRow > vRSel.size())
           {
             session.setAttribute("StatusMessage", "Row size is either too big or too small.");
           }
           else
           {
             if (acSearch.equals("DataElementConcept"))
             {
               deDEC = (DEC_Bean)vRSel.elementAt(iRow);
               pageDE.setDE_DEC_Bean(deDEC);
               if (nameAct.equals("append"))
                 pageDE.setDE_PREFERRED_DEFINITION(pageDE.getDE_PREFERRED_DEFINITION() + "_" + deDEC.getDEC_PREFERRED_DEFINITION());
             }
             else if (acSearch.equals("ValueDomain"))
             {
               deVD = (VD_Bean)vRSel.elementAt(iRow);
               pageDE.setDE_VD_Bean(deVD);
               if (nameAct.equals("append"))
                 pageDE.setDE_PREFERRED_DEFINITION(pageDE.getDE_PREFERRED_DEFINITION() + "_" + deVD.getVD_PREFERRED_DEFINITION());
             }
           }
         }
       }
     }
         
     String sLongName = "";
     if (nameAct.equals("new")) sDef = "";
     //get sys name and abbr names from dec and vd beans
     if (deDEC != null)
     {
       sSysName = deDEC.getDEC_DEC_ID() + "v" + deDEC.getDEC_VERSION() + ":";
       sAbbName = deDEC.getDEC_PREFERRED_NAME() + "_";
       sLongName = deDEC.getDEC_LONG_NAME() + " ";
       if (nameAct.equals("new"))
         sDef = deDEC.getDEC_PREFERRED_DEFINITION() + "_";
     }
     if (deVD != null)
     {
       sSysName = sSysName + deVD.getVD_VD_ID() + "v" + deVD.getVD_VERSION();
       sAbbName = sAbbName + deVD.getVD_PREFERRED_NAME();      
       sLongName = sLongName + deVD.getVD_LONG_NAME();
       if (nameAct.equals("new"))
         sDef = sDef + deVD.getVD_PREFERRED_DEFINITION();      
     }
     //truncate to 30 characters 
     if (sAbbName != null && sAbbName.length()>30)
       sAbbName = sAbbName.substring(0,30);
     //truncate to 30 characters 
     if (sSysName != null && sSysName.length()>30)
       sSysName = sSysName.substring(0,30);
     pageDE.setAC_ABBR_PREF_NAME(sAbbName);
     pageDE.setAC_SYS_PREF_NAME(sSysName);
 logger.debug(sUsrName + " name type " + selNameType);
     //set pref name accoding to teh type
     if (selNameType.equals("SYS"))
       pageDE.setDE_PREFERRED_NAME(sSysName);
     else if (selNameType.equals("ABBR"))
       pageDE.setDE_PREFERRED_NAME(sAbbName);
     else if (selNameType.equals("USER"))  //store the user typeed name
       pageDE.setDE_PREFERRED_NAME(sUsrName);
 
     //update long name and defintion if dec or vd is searched
     if (!sOrigin.equalsIgnoreCase("openDE"))      //(sOrigin.equals("Search"))
     {
       pageDE.setDE_LONG_NAME(sLongName);
       pageDE.setDE_PREFERRED_DEFINITION(sDef);
     }
 logger.debug(pageDE.getAC_PREF_NAME_TYPE() + " pref " + pageDE.getDE_PREFERRED_NAME());
     session.setAttribute("m_DE", pageDE); 
     return pageDE;
   }
 
   /**
    * changes the dec name type as selected
    * @param req HttpServletRequest
    * @param res HttpServletResponse
    * @param sOrigin string of origin action of the ac
    * @throws java.lang.Exception
    */
   public void doChangeDENameType(HttpServletRequest req, HttpServletResponse res, 
       String sOrigin) throws Exception
   {
     HttpSession session = req.getSession();
     //get teh selected type from teh page
     DE_Bean pageDE = (DE_Bean)session.getAttribute("m_DE");
     //capture all other attributes
     m_setAC.setDEValueFromPage(req, res, pageDE);  
     
     String sSysName = pageDE.getAC_SYS_PREF_NAME();
     String sAbbName = pageDE.getAC_ABBR_PREF_NAME();
     String sUsrName = pageDE.getAC_USER_PREF_NAME();
     String sNameType = (String)req.getParameter("rNameConv");
     if (sNameType == null) sNameType = "";
 logger.debug(sSysName + " name abb " + sAbbName + " name usr " + sUsrName);
 
     //get the existing preferred name to make sure earlier typed one is saved in the user
     String sPrefName = (String)req.getParameter("txtPreferredName");
     if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)") 
         && !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
        pageDE.setAC_USER_PREF_NAME(sPrefName); //store typed one in de bean 
     //reset system generated or abbr accoring 
     if (sNameType.equals("SYS"))
       pageDE.setDE_PREFERRED_NAME(sSysName);
     else if (sNameType.equals("ABBR"))
       pageDE.setDE_PREFERRED_NAME(sAbbName);
     else if (sNameType.equals("USER"))
       pageDE.setDE_PREFERRED_NAME(pageDE.getAC_USER_PREF_NAME());
     //store the type in the bean
     pageDE.setAC_PREF_NAME_TYPE(sNameType);   
 logger.debug(pageDE.getAC_PREF_NAME_TYPE() + " pref " + pageDE.getDE_PREFERRED_NAME());
     session.setAttribute("m_DE", pageDE);    
   }
   
   /**
    * changes the dec name type as selected
    * @param req HttpServletRequest
    * @param res HttpServletResponse
    * @param sOrigin string of origin action of the ac
    * @throws java.lang.Exception
    */
   public void doChangeDECNameType(HttpServletRequest req, HttpServletResponse res, 
       String sOrigin) throws Exception
   {
     HttpSession session = req.getSession();
     //get teh selected type from teh page
     DEC_Bean pageDEC = (DEC_Bean)session.getAttribute("m_DEC");
     m_setAC.setDECValueFromPage(req, res, pageDEC);  //capture all other attributes
     
     String sSysName = pageDEC.getAC_SYS_PREF_NAME();
     String sAbbName = pageDEC.getAC_ABBR_PREF_NAME();
     String sUsrName = pageDEC.getAC_USER_PREF_NAME();
     String sNameType = (String)req.getParameter("rNameConv");
     if (sNameType == null || sNameType.equals("")) sNameType = "SYS";  //default
 logger.debug(sSysName + " name type " + sNameType);
 
     //get the existing preferred name to make sure earlier typed one is saved in the user
     String sPrefName = (String)req.getParameter("txtPreferredName");
     if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)") 
         && !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
        pageDEC.setAC_USER_PREF_NAME(sPrefName); //store typed one in de bean 
     //reset system generated or abbr accoring 
     if (sNameType.equals("SYS"))
       pageDEC.setDEC_PREFERRED_NAME(sSysName);
     else if (sNameType.equals("ABBR"))
       pageDEC.setDEC_PREFERRED_NAME(sAbbName);
     else if (sNameType.equals("USER"))
       pageDEC.setDEC_PREFERRED_NAME(sUsrName);
     //store the type in the bean
     pageDEC.setAC_PREF_NAME_TYPE(sNameType);   
 
 logger.debug(pageDEC.getAC_PREF_NAME_TYPE() + " pref " + pageDEC.getDEC_PREFERRED_NAME());
     session.setAttribute("m_DEC", pageDEC);    
   }
   
   /**
    * changes the dec name type as selected
    * @param req HttpServletRequest
    * @param res HttpServletResponse
    * @param sOrigin string of origin action of the ac
    * @throws java.lang.Exception
    */
   public void doChangeVDNameType(HttpServletRequest req, HttpServletResponse res, 
       String sOrigin) throws Exception
   {
     HttpSession session = req.getSession();
     //get teh selected type from teh page
     VD_Bean pageVD = (VD_Bean)session.getAttribute("m_VD");
     m_setAC.setVDValueFromPage(req, res, pageVD);  //capture all other attributes
     
     String sSysName = pageVD.getAC_SYS_PREF_NAME();
     String sAbbName = pageVD.getAC_ABBR_PREF_NAME();
     String sUsrName = pageVD.getAC_USER_PREF_NAME();
     String sNameType = (String)req.getParameter("rNameConv");
     if (sNameType == null || sNameType.equals("")) sNameType = "SYS";  //default
 
     //get the existing preferred name to make sure earlier typed one is saved in the user
     String sPrefName = (String)req.getParameter("txtPreferredName");
     if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)") 
         && !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
        pageVD.setAC_USER_PREF_NAME(sPrefName); //store typed one in de bean 
     //reset system generated or abbr accoring 
     if (sNameType.equals("SYS"))
       pageVD.setVD_PREFERRED_NAME(sSysName);
     else if (sNameType.equals("ABBR"))
       pageVD.setVD_PREFERRED_NAME(sAbbName);
     else if (sNameType.equals("USER"))
       pageVD.setVD_PREFERRED_NAME(sUsrName);
     
     pageVD.setAC_PREF_NAME_TYPE(sNameType);   //store the type in the bean
 
 logger.debug(pageVD.getAC_PREF_NAME_TYPE() + " pref " + pageVD.getVD_PREFERRED_NAME());
     session.setAttribute("m_VD", pageVD);
     
   }
   /**
   * Does open editDEC page action from DE page called from 'doEditDEActions' method.
   * Calls 'm_setAC.setDEValueFromPage' to store the DE bean for later use
   * Using the DEC idseq, calls 'SerAC.search_DEC' method to gets dec attributes to populate.
   * stores DEC bean in session and opens editDEC page.
   * goes back to editDE page if any error.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doOpenEditDECPage(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
       if (m_DE == null) m_DE = new DE_Bean();
       //store the de values in the session
       m_setAC.setDEValueFromPage(req, res, m_DE);
       session.setAttribute("m_DE", m_DE);
       this.clearBuildingBlockSessionAttributes(req, res);
       String sDEC_ID = null;
       String sDECid[] = req.getParameterValues("selDEC");
       if (sDECid != null)
          sDEC_ID = sDECid[0];
 
       //get the dec bean for this id
       if (sDEC_ID != null)
       {
          Vector vList = new Vector();
          GetACSearch serAC = new GetACSearch(req, res, this);
          serAC.doDECSearch(sDEC_ID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", vList);
          //forward editDEC page with this bean
          if (vList.size() > 0)
          {
             for (int i =0; i < vList.size(); i++)
             {
                DEC_Bean DECBean = new DEC_Bean();
                DECBean = (DEC_Bean)vList.elementAt(i);
                //check if the user has write permission
                String contID = DECBean.getDEC_CONTE_IDSEQ();
                String sUser = (String)session.getAttribute("Username");
                GetACService getAC = new GetACService(req, res, this);
                String hasPermit = getAC.hasPrivilege("Create", sUser, "dec", contID);
                //forward to editDEC if has write permission
                if (hasPermit.equals("Yes"))
                {
                   DECBean = serAC.getDECAttributes(DECBean, "Edit", "Edit");   //get DEC other Attributes
                   //store the bean in the session attribute
                   session.setAttribute("m_DEC", DECBean); 
                   DEC_Bean oldDEC = new DEC_Bean();
                   oldDEC = oldDEC.cloneDEC_Bean(DECBean);
                   session.setAttribute("oldDECBean", oldDEC);
                   ForwardJSP(req, res, "/EditDECPage.jsp");   //forward to editDEC page
                }
                //go back to editDE with message if no permission
                else
                {
                   session.setAttribute("statusMessage", "No edit permission in " +
                             DECBean.getDEC_CONTEXT_NAME() + " context");
                   ForwardJSP(req, res, "/EditDEPage.jsp");   //forward to editDE page
                }
                break;
             }
          }
          //display error message and back to edit DE page
          else
              session.setAttribute("statusMessage", "Unable to get Existing DEConcept attributes from the database");
       }
       //display error message and back to editDE page
       else
       {
           session.setAttribute("statusMessage", "Unable to get the DEConcept id from the page");
           //forward the depage when error occurs
           ForwardJSP(req, res, "/EditDEPage.jsp");   //forward to editDE page
       }
   }
 
   /**
   * Does open editVD page action from DE page called from 'doEditDEActions' method.
   * Calls 'm_setAC.setDEValueFromPage' to store the DE bean for later use
   * Using the VD idseq, calls 'SerAC.search_VD' method to gets dec attributes to populate.
   * stores VD bean in session and opens editVD page.
   * goes back to editDE page if any error.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doOpenEditVDPage(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
       if (m_DE == null) m_DE = new DE_Bean();
       //store the de values in the session
       m_setAC.setDEValueFromPage(req, res, m_DE);
       session.setAttribute("m_DE", m_DE);
       this.clearBuildingBlockSessionAttributes(req, res);
       String sVDID = null;
       String sVDid[] = req.getParameterValues("selVD");
       if (sVDid != null)
          sVDID = sVDid[0];
 
       //get the dec bean for this id
       if (sVDID != null)
       {
          Vector vList = new Vector();
          GetACSearch serAC = new GetACSearch(req, res, this);
          serAC.doVDSearch(sVDID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", vList);
          //forward editVD page with this bean
          if (vList.size() > 0)
          {
             for (int i =0; i < vList.size(); i++)
             {
                VD_Bean VDBean = new VD_Bean();
                VDBean = (VD_Bean)vList.elementAt(i);
                //check if the user has write permission
                String contID = VDBean.getVD_CONTE_IDSEQ();
                String sUser = (String)session.getAttribute("Username");
                GetACService getAC = new GetACService(req, res, this);
                String hasPermit = getAC.hasPrivilege("Create", sUser, "vd", contID);
                //forward to editVD if has write permission
                if (hasPermit.equals("Yes"))
                {
                   String sMenuAction = (String)session.getAttribute("MenuAction");
                   VDBean = serAC.getVDAttributes(VDBean, "Edit", sMenuAction);   //get VD other Attributes
 
                   session.setAttribute("m_VD", VDBean);
                   VD_Bean oldVD = new VD_Bean();
                   oldVD = oldVD.cloneVD_Bean(VDBean);
                   session.setAttribute("oldVDBean", oldVD);
                  // session.setAttribute("oldVDBean", VDBean);
                   ForwardJSP(req, res, "/EditVDPage.jsp");   //forward to editVD page
                }
                //go back to editDE with message if no permission
                else
                {
                   session.setAttribute("statusMessage", "No edit permission in " +
                             VDBean.getVD_CONTEXT_NAME() + " context");
                   ForwardJSP(req, res, "/EditDEPage.jsp");   //forward to editDE page
                }
                break;
             }
          }
          //display error message and back to edit DE page
          else
          {
              session.setAttribute("statusMessage", "Unable to get Existing VD attributes from the database");
              ForwardJSP(req, res, "/EditDEPage.jsp");   //forward to editDE page
          }
       }
       //display error message and back to editDE page
       else
       {
           session.setAttribute("statusMessage", "Unable to get the VDid from the page");
           ForwardJSP(req, res, "/EditDEPage.jsp");   //forward to editDE page
       }
   }//end doEditDECAction
 
    /**
   * The doCreateDECActions method handles CreateDEC or EditDEC actions of the request.
   * Called from 'service' method where reqType is 'newDECfromForm'
   * Calls 'doValidateDEC' if the action is Validate or submit.
   * Calls 'doSuggestionDE' if the action is open EVS Window.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doCreateDECActions(HttpServletRequest req,  HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
 
       String sAction = (String)req.getParameter("newCDEPageAction");
       session.setAttribute("DECPageAction", sAction);      //store the page action in attribute
       String sSubAction = (String)req.getParameter("DECAction");
       session.setAttribute("DECAction", sSubAction);
       String sOriginAction = (String)session.getAttribute("originAction");
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
         //re work on the naming if new one
         DEC_Bean dec = (DEC_Bean)session.getAttribute("m_DEC");
         if (!sMenuAction.equals("NewDECTemplate") && !sMenuAction.equals("NewDECVersion"))
           dec = this.doGetDECNames(req, res, null, "Search", dec);  //change only abbr pref name
         else
           dec = this.doGetDECNames(req, res, null, "Remove", dec);  //need to change the long name & def also
         session.setAttribute("m_DEC", dec);
         ForwardJSP(req, res, "/CreateDECPage.jsp");
         return;
       } 
       else if (sAction.equals("changeNameType"))
       {
         this.doChangeDECNameType(req, res, "changeType");
         ForwardJSP(req, res, "/CreateDECPage.jsp");
       }
       else if (sAction.equals("clearBoxes"))
       {
          DEC_Bean DECBean = (DEC_Bean)session.getAttribute("oldDECBean");
          this.clearBuildingBlockSessionAttributes(req, res);
          String sDECID = DECBean.getDEC_DEC_IDSEQ();
          Vector vList = new Vector();           
          //get VD's attributes from the database again
          GetACSearch serAC = new GetACSearch(req, res, this);
          if (sDECID != null && !sDECID.equals(""))
             serAC.doDECSearch(sDECID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", vList);
          //forward editVD page with this bean
          if (vList.size() > 0)
          {
            DECBean = (DEC_Bean)vList.elementAt(0);
            DECBean = serAC.getDECAttributes(DECBean, sOriginAction, sMenuAction);
          }
          else  //new one
          {
             DECBean = new DEC_Bean();
             DECBean.setDEC_ASL_NAME("DRAFT NEW");
             DECBean.setAC_PREF_NAME_TYPE("SYS");
          }
          DEC_Bean pgBean = new DEC_Bean();
           session.setAttribute("m_DEC", pgBean.cloneDEC_Bean(DECBean));
          ForwardJSP(req, res, "/CreateDECPage.jsp");
       }
       //open the create DE page or search result page
       else if (sAction.equals("backToDE"))
       {
          if (sMenuAction.equals("NewDECTemplate") || sMenuAction.equals("NewDECVersion"))
          {
             DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
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
   * The doEditDECActions method handles EditDEC actions of the request.
   * Called from 'service' method where reqType is 'EditDEC'
   * Calls 'ValidateDEC' if the action is Validate or submit.
   * Calls 'doSuggestionDEC' if the action is open EVS Window.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doEditDECActions(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
 
       String sAction = (String)req.getParameter("newCDEPageAction");
       session.setAttribute("DECPageAction", sAction);      //store the page action in attribute
       String sSubAction = (String)req.getParameter("DECAction");
       session.setAttribute("DECAction", sSubAction);
 
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
       String sOriginAction = (String)session.getAttribute("originAction");
       if (sAction.equals("submit"))
           doSubmitDEC(req, res);
       else if(sAction.equals("validate") && sOriginAction.equals("BlockEditDEC"))
           doValidateDECBlockEdit(req, res);
       else if (sAction.equals("validate"))
           doValidateDEC(req, res);
       else if(sAction.equals("suggestion"))
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
           //re work on the naming if new one
           DEC_Bean dec = (DEC_Bean)session.getAttribute("m_DEC");
           dec = this.doGetDECNames(req, res, null, "Remove", dec);  //need to change the long name & def also
           session.setAttribute("m_DEC", dec);
           ForwardJSP(req, res, "/EditDECPage.jsp");
           return;
       } 
       else if (sAction.equals("changeNameType"))
       {
         this.doChangeDECNameType(req, res, "changeType");
         ForwardJSP(req, res, "/EditDECPage.jsp");
       }
       else if (sAction.equals("clearBoxes"))
       {
           DEC_Bean DECBean = (DEC_Bean)session.getAttribute("oldDECBean");
           this.clearBuildingBlockSessionAttributes(req, res);
           String sDECID = DECBean.getDEC_DEC_IDSEQ();
           Vector vList = new Vector();           
           //get VD's attributes from the database again
           GetACSearch serAC = new GetACSearch(req, res, this);
           if (sDECID != null && !sDECID.equals(""))
             serAC.doDECSearch(sDECID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", vList);
           if (vList.size() > 0)
           {
              DECBean = (DEC_Bean)vList.elementAt(0);
      logger.debug("cleared name " + DECBean.getDEC_PREFERRED_NAME());
              DECBean = serAC.getDECAttributes(DECBean, sOriginAction, sMenuAction);
           }
           DEC_Bean pgBean = new DEC_Bean();
           session.setAttribute("m_DEC", pgBean.cloneDEC_Bean(DECBean));
           ForwardJSP(req, res, "/EditDECPage.jsp");
       }
       //open the create DE page or search result page
       else if (sAction.equals("backToDE"))
       {
          this.clearBuildingBlockSessionAttributes(req, res);  //clear session attributes
          if (sOriginAction.equalsIgnoreCase("editDECfromDE"))
             ForwardJSP(req, res, "/EditDEPage.jsp");
          else if (sMenuAction.equalsIgnoreCase("editDEC") || sOriginAction.equalsIgnoreCase("BlockEditDEC") || sButtonPressed.equals("Search")
                   || sOriginAction.equalsIgnoreCase("EditDEC"))
          {
             DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
             GetACSearch serAC = new GetACSearch(req, res, this);
             serAC.refreshData(req, res, null, DECBean, null, null, "Refresh", "");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
          else
             ForwardJSP(req, res, "/EditDECPage.jsp");
       }
    }
 
   /**
   * The doCreateVDActions method handles CreateVD or EditVD actions of the request.
   * Called from 'service' method where reqType is 'newVDfromForm'
   * Calls 'doValidateVD' if the action is Validate or submit.
   * Calls 'doSuggestionDE' if the action is open EVS Window.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doCreateVDActions(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
       String sAction = (String)req.getParameter("newCDEPageAction");
       session.setAttribute("VDPageAction", sAction);      //store the page action in attribute
       String sSubAction = (String)req.getParameter("VDAction");
       session.setAttribute("VDAction", sSubAction);
       String sOriginAction = (String)session.getAttribute("originAction");
       if(sAction.equals("changeContext"))
         doChangeContext(req, res, "vd");
       else if(sAction.equals("validate"))
         doValidateVD(req, res);
       else if(sAction.equals("submit"))
         doSubmitVD(req, res);
       else if(sAction.equals("createPV") || sAction.equals("editPV") || sAction.equals("removePV"))
         doOpenCreatePVPage(req, res, sAction, "createVD");
       else if(sAction.equals("removePVandParent") || sAction.equals("removeParent"))
         doRemoveParent(req, res, sAction, "createVD");
       else if(sAction.equals("searchPV"))
         doSearchPV(req, res);
       else if(sAction.equals("createVM"))
         doOpenCreateVMPage(req, res, "vd"); 
       else if(sAction.equals("Enum") || sAction.equals("NonEnum"))
       {
           doSetVDPage(req, res, "Create");
           ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
       else if (sAction.equals("clearBoxes"))
       {
          VD_Bean VDBean = (VD_Bean)session.getAttribute("oldVDBean");
          //clear related the session attributes 
          session.setAttribute("VDParentConcept", new Vector());
          session.setAttribute("VDPVList", new Vector());
          this.clearBuildingBlockSessionAttributes(req, res);
          String sVDID = VDBean.getVD_VD_IDSEQ();
          Vector vList = new Vector();           
          //get VD's attributes from the database again
          GetACSearch serAC = new GetACSearch(req, res, this);
          if (sVDID != null && !sVDID.equals(""))
             serAC.doVDSearch(sVDID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", vList);
          //forward editVD page with this bean
          if (vList.size() > 0)
          {
            VDBean = (VD_Bean)vList.elementAt(0);
            VDBean = serAC.getVDAttributes(VDBean, sOriginAction, sMenuAction);
          }
          else
          {
            VDBean = new VD_Bean();
            VDBean.setVD_ASL_NAME("DRAFT NEW");
            VDBean.setAC_PREF_NAME_TYPE("SYS");
          }
          VD_Bean pgBean = new VD_Bean();
          session.setAttribute("m_VD", pgBean.cloneVD_Bean(VDBean));
          ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
       else if (sAction.equals("refreshCreateVD"))
       {
         doSelectParentVD(req, res);
         ForwardJSP(req, res, "/CreateVDPage.jsp");
         return;
       }
       else if (sAction.equals("UseSelection"))
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
         //re work on the naming if new one
         VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
         if (!sMenuAction.equals("NewVDTemplate") && !sMenuAction.equals("NewVDVersion"))
           vd = this.doGetVDNames(req, res, null, "Search", vd);  //change only abbr pref name
         else
           vd = this.doGetVDNames(req, res, null, "Remove", vd);  //need to change the long name & def also
         session.setAttribute("m_VD", vd);
         ForwardJSP(req, res, "/CreateVDPage.jsp");
         return;
       } 
       else if (sAction.equals("changeNameType"))
       {
         this.doChangeVDNameType(req, res, "changeType");
         ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
       else if (sAction.equals("CreateNonEVSRef"))
       {
          doNonEVSReference(req, res);
          ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
       else if (sAction.equals("addSelectedVM"))
       {
          doSelectVMinVD(req, res, sAction);
          ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
       else if (sAction.equals("sortPV"))
       {
         GetACSearch serAC = new GetACSearch(req, res, this);
         String sField = (String)req.getParameter("pvSortColumn");
         serAC.getVDPVSortedRows(sField);          //call the method to sort pv attribute
         ForwardJSP(req, res, "/CreateVDPage.jsp");
         return;
       }
       //open the DE page or search page with
       else if (sAction.equals("backToDE"))
       {
          this.clearCreateSessionAttributes(req, res);
          if (sMenuAction.equals("NewVDTemplate") || sMenuAction.equals("NewVDVersion"))
          {
             VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
             GetACSearch serAC = new GetACSearch(req, res, this);
             serAC.refreshData(req, res, null, null, VDBean, null, "Refresh", "");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
          else if (sOriginAction.equalsIgnoreCase("CreateNewVDfromEditDE"))
             ForwardJSP(req, res, "/EditDEPage.jsp");
          else
             ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
   }
 
   /**
   * The doEditDEActions method handles EditDE actions of the request.
   * Called from 'service' method where reqType is 'EditVD'
   * Calls 'ValidateDE' if the action is Validate or submit.
   * Calls 'doSuggestionDE' if the action is open EVS Window.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doEditVDActions(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
 
       HttpSession session = req.getSession();
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
       String sAction = (String)req.getParameter("newCDEPageAction");
       session.setAttribute("VDPageAction", sAction);      //store the page action in attribute
       String sSubAction = (String)req.getParameter("VDAction");
       session.setAttribute("VDAction", sSubAction);
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
       String sSearchAC = (String)session.getAttribute("SearchAC");
       if (sSearchAC == null) sSearchAC = "";
       String sOriginAction = (String)session.getAttribute("originAction");
 
       if (sAction.equals("submit"))
         doSubmitVD(req, res);
       else if(sAction.equals("validate") && sOriginAction.equals("BlockEditVD"))
         doValidateVDBlockEdit(req, res);
       else if(sAction.equals("validate"))
         doValidateVD(req, res);
       else if(sAction.equals("suggestion"))
         doSuggestionDE(req, res);
       else if (sAction.equals("refreshCreateVD"))
       {
         doSelectParentVD(req, res);
         ForwardJSP(req, res, "/EditVDPage.jsp");
         return;
       }
       else if (sAction.equals("CreateNonEVSRef"))
       {
          doNonEVSReference(req, res);
          ForwardJSP(req, res, "/EditVDPage.jsp");
       }
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
         //re work on the naming if new one
         VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
         vd = this.doGetVDNames(req, res, null, "Remove", vd); //change only abbr pref name
         session.setAttribute("m_VD", vd);
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
         String sField = (String)req.getParameter("pvSortColumn");
         serAC.getVDPVSortedRows(sField);          //call the method to sort pv attribute
         ForwardJSP(req, res, "/EditVDPage.jsp");
         return;
       }
       else if(sAction.equals("createPV") || sAction.equals("editPV") || sAction.equals("removePV"))
         doOpenCreatePVPage(req, res, sAction, "editVD");
       else if(sAction.equals("removePVandParent") || sAction.equals("removeParent"))
         doRemoveParent(req, res, sAction, "editVD");
       else if (sAction.equals("addSelectedVM"))
       {
          doSelectVMinVD(req, res, sAction);
          ForwardJSP(req, res, "/EditVDPage.jsp");
       }
       else if(sAction.equals("Enum") || sAction.equals("NonEnum"))
       {
         doSetVDPage(req, res, "Edit");
         ForwardJSP(req, res, "/EditVDPage.jsp");
       }
       else if (sAction.equals("clearBoxes"))
       {
          VD_Bean VDBean = (VD_Bean)session.getAttribute("oldVDBean");
          //clear related the session attributes 
          session.setAttribute("VDParentConcept", new Vector());
          session.setAttribute("VDPVList", new Vector());
          this.clearBuildingBlockSessionAttributes(req, res);
          String sVDID = VDBean.getVD_VD_IDSEQ();
          Vector vList = new Vector();           
          //get VD's attributes from the database again
          GetACSearch serAC = new GetACSearch(req, res, this);
          if (sVDID != null && !sVDID.equals(""))
             serAC.doVDSearch(sVDID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", vList);
          //forward editVD page with this bean
          if (vList.size() > 0)
          {
            VDBean = (VD_Bean)vList.elementAt(0);
            VDBean = serAC.getVDAttributes(VDBean, sOriginAction, sMenuAction);
          }
          VD_Bean pgBean = new VD_Bean();
          session.setAttribute("m_VD", pgBean.cloneVD_Bean(VDBean));
          ForwardJSP(req, res, "/EditVDPage.jsp");
       }
       //open the Edit DE page or search page with
       else if (sAction.equals("backToDE"))
       {
       //System.out.println(sOriginAction + "backtode" + sMenuAction);
           //forward the page to editDE if originated from DE
          if (sOriginAction.equalsIgnoreCase("editVDfromDE"))
             ForwardJSP(req, res, "/EditDEPage.jsp");
          //forward the page to search if originated from Search
          else if (sMenuAction.equalsIgnoreCase("editVD") || sOriginAction.equalsIgnoreCase("EditVD") || sOriginAction.equalsIgnoreCase("BlockEditVD")  
          || ((sButtonPressed.equals("Search") && !sSearchAC.equals("DataElement"))))
          {
             VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
             GetACSearch serAC = new GetACSearch(req, res, this);
             serAC.refreshData(req, res, null, null, VDBean, null, "Refresh", "");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
          else
             ForwardJSP(req, res, "/EditVDPage.jsp");
       }
    }
 
   /**
   * The doCreatePVActions method handles the submission of a CreatePV form
   * Called from DON'T KNOW
   * Calls 'doValidatePV' if the action is Validate or submit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doCreatePVActions(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
       HttpSession session = req.getSession();
       String sAction = (String)req.getParameter("newCDEPageAction");
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
       String sOriginAction = (String)session.getAttribute("originAction");
 
       if (sAction.equals("submit"))
          doSubmitPV(req, res);
       else if (sAction.equals("validate"))
          doValidatePV(req, res);
       else if (sAction.equals("createNewVM"))
          this.doOpenCreateVMPage(req, res, "vm");
       //store vm attributes in pv bean
       else if (sAction.equals("appendSearchVM"))
       {
         PV_Bean pvBean = (PV_Bean)session.getAttribute("m_PV");
         if (pvBean == null) pvBean = new PV_Bean();
         SetACService setAC = new SetACService(this);
         setAC.setPVValueFromPage(req, res, pvBean);
         String selVM = pvBean.getPV_SHORT_MEANING();
         Vector vRSel = (Vector)session.getAttribute("vACSearch");
         if (vRSel == null) vRSel = new Vector();
         for (int i=0; i<(vRSel.size()); i++)
         {
           VM_Bean vmBean = (VM_Bean)vRSel.elementAt(i);
           //store the vm attributes in pv attribute
           if (vmBean.getVM_SHORT_MEANING().equals(selVM))
           {
             pvBean.setPV_MEANING_DESCRIPTION(vmBean.getVM_DESCRIPTION());
             pvBean.setVM_CONCEPT(vmBean.getVM_CONCEPT());
             break;
           }          
         }
         session.setAttribute("m_PV", pvBean);
         ForwardJSP(req, res, "/CreatePVPage.jsp");
       }
       else if (sAction.equals("clearBoxes"))
       {
           PV_Bean pvOpen = (PV_Bean)session.getAttribute("pageOpenBean");
           PV_Bean pvBean = (PV_Bean)session.getAttribute("m_PV");
           if (pvOpen != null) 
             pvBean = pvBean.copyBean(pvOpen);
           
           session.setAttribute("m_PV", pvBean);
           ForwardJSP(req, res, "/CreatePVPage.jsp");
       }
       else if (sAction.equals("backToVD"))
       {
         //set the checked property to false
         Vector vVDPVList = (Vector)session.getAttribute("VDPVList");
         if (vVDPVList != null)
         {
           for (int i=0; i<vVDPVList.size(); i++)
           {
             PV_Bean pv = (PV_Bean)vVDPVList.elementAt(i);
             pv.setPV_CHECKED(false);
             vVDPVList.setElementAt(pv, i);
           }
           session.setAttribute("VDPVList", vVDPVList);
         }
         if (sMenuAction.equals("editVD") || sMenuAction.equals("editDE") || sButtonPressed.equals("Search"))
            ForwardJSP(req, res, "/EditVDPage.jsp");  //back to Edit VD Screen
         else
            ForwardJSP(req, res, "/CreateVDPage.jsp");
       } 
   }
 
    /**
   * The doCreateVMActions method handles the submission of a CreatePV form
   * Called from 
   * Calls 'doValidateVM' if the action is Validate or submit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doCreateVMActions(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
       HttpSession session = req.getSession();
       String sMenuAction = (String)req.getParameter("MenuAction");
       if (sMenuAction != null)
         session.setAttribute("MenuAction", sMenuAction);
       String sAction = (String)req.getParameter("newCDEPageAction");
       if(sAction.equals("submit"))
           doSubmitVM(req, res);
       else if(sAction.equals("validate"))
           doValidateVM(req, res);
       //store concept attributes in vm bean
       else if (sAction.equals("appendSearchVM"))
       {
         VM_Bean vmBean = (VM_Bean)session.getAttribute("m_VM");
         if (vmBean == null) vmBean = new VM_Bean();
         SetACService setAC = new SetACService(this);
         setAC.setVMValueFromPage(req, res, vmBean);
         String selVM = vmBean.getVM_SHORT_MEANING();
         Vector vRSel = (Vector)session.getAttribute("vACSearch");
         if (vRSel == null) vRSel = new Vector();
         String selRow = (String)req.getParameter("hiddenSelRow");
         Integer IRow = new Integer(selRow);
         int iRow = IRow.intValue();
         if (iRow < 0 || iRow > vRSel.size())
           session.setAttribute("StatusMessage", "Unable to select Concept, please try again");
         else
         {
           for (int i=0; i<(vRSel.size()); i++)
           {
             if (iRow == i)
             {
               EVS_Bean eBean = (EVS_Bean)vRSel.elementAt(i);
               //get the right concept id
               String sCon = eBean.getNCI_CC_VAL();
              // String sConType = eBean.getNCI_CC_TYPE();
               if (sCon == null || sCon.equalsIgnoreCase("No value returned."))
                 sCon = eBean.getUMLS_CUI_VAL();
               if (sCon == null || sCon.equalsIgnoreCase("No value returned."))
                 sCon = eBean.getTEMP_CUI_VAL();
               eBean.setNCI_CC_VAL(sCon);
               //store the concept attributes in vm bean
               vmBean.setVM_CONCEPT(eBean);
               session.setAttribute("EVSSearched", "searched");  //store evs or not
               break;
             }
           }
         }
         session.setAttribute("m_VM", vmBean);
         ForwardJSP(req, res, "/CreateVMPage.jsp");
       }
       else if (sAction.equals("backToPV"))
       {
          String sOriginAction = (String)session.getAttribute("originAction");
          if (sOriginAction.equals("CreateInSearch"))
             ForwardJSP(req, res, "/CreatePVSearchPage.jsp");
          else
             ForwardJSP(req, res, "/CreatePVPage.jsp");
       }
   }
 
   /**
   * The doDesignateDEActions method handles DesignatedDE actions of the request.
   * Called from 'service' method where reqType is 'DesignatedDE'
   * Calls 'ValidateDEC' if the action is Validate or submit.
   * Calls 'doSuggestionDEC' if the action is open EVS Window.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doEditDesDEActions(HttpServletRequest req, HttpServletResponse res, String sOrigin)
    throws Exception
    {
       HttpSession session = req.getSession();
       //do the open designate page action
       if (sOrigin.equals("Open"))
       {
          //get the list of checked DEs from the page
          GetACSearch getACSearch = new GetACSearch(req, res, this);
          getACSearch.getSelRowToEdit(req, res, "EditDesDE");
          ForwardJSP(req, res, "/EditDesignateDEPage.jsp");              
       }      
       //do edit designate page action
       else if (sOrigin.equals("Edit"))
       {
         String sAction = (String)req.getParameter("newCDEPageAction");
         if (sAction == null) sAction = "";
         if (sAction.equals("") || sAction.equals("clearBoxes") || sAction.equals("nothing"))
         {
           logger.fatal("clearing designate de " + sAction);
           //loop thorough the list of selected ACs and gets designated ac attributes
           ForwardJSP(req, res, "/EditDesignateDEPage.jsp");      
         }         
         else if (sAction.equals("create") || sAction.equals("remove"))
         {
           logger.debug("inserting data");
           InsACService insAC = new InsACService(req, res, this);
           insAC.doSubmitDesDE(sAction);
           session.setAttribute("CheckList", new Vector()); //empty the check list.          
         }
         //forward to search results page if created or going back
         if (sAction.equals("backToSearch") || sAction.equals("create") || sAction.equals("remove"))
         {
           Vector vResult = new Vector();
           GetACSearch serAC = new GetACSearch(req, res, this);
           serAC.getDEResult(req, res, vResult, "");
           session.setAttribute("results", vResult);
           //reset the menu action to edit de if it was editdesde
           session.setAttribute("AllAltNameList", new Vector()); 
           session.setAttribute("AllRefDocList", new Vector()); 
           ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }   
       }
    }
 
   /**
   * The doChangeContext method resets the bean then forwards to Create page
   * Called from 'doCreateDEActions', 'doCreateDECActions', 'doCreateVDActions' methods.
   * Calls 'getAC.getACList' if the action is Validate or submit.
   * Stores empty bean and forwards the create page of the selected component.
   * Stores selected context as default session.
   * If the create DEC and VD origin is DE, only store the new context in the bean.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doChangeContext(HttpServletRequest req,
    HttpServletResponse res, String sACType) throws Exception
    {
       UtilService util = new UtilService();
       HttpSession session = req.getSession();
       String sContextID = (String)req.getParameter("selContext");
       String sNewContext = util.getNameByID((Vector)session.getAttribute("vContext"),
              (Vector)session.getAttribute("vContext_ID"), sContextID);
 
       String sOriginAction = (String)session.getAttribute("originAction");
       if (sOriginAction == null) sOriginAction = "";
       boolean bNewContext = true;
       boolean bDE = sACType.equals("de");
       if(sNewContext != null)
       {
           GetACService getAC = new GetACService(req, res, this);
           getAC.getACList(req, res, sNewContext, bNewContext, sACType);
       }
       if(sACType.equals("de"))
       {
         session.setAttribute("sDefaultContext", sNewContext);
         session.setAttribute("m_DE", new DE_Bean());
         ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       else if(sACType.equals("dec"))
       {
         DEC_Bean m_DEC = new DEC_Bean();
         if (sOriginAction.equals("CreateNewDEC"))
            m_DEC.setDEC_CONTEXT_NAME(sNewContext);
         else
            session.setAttribute("sDefaultContext", sNewContext);
 
         session.setAttribute("m_DEC", m_DEC);
         ForwardJSP(req, res, "/CreateDECPage.jsp");
       }
        else if(sACType.equals("vd"))
       {
         VD_Bean m_VD = new VD_Bean();
         if (sOriginAction.equals("CreateNewVD"))
            m_VD.setVD_CONTEXT_NAME(sNewContext);
         else
            session.setAttribute("sDefaultContext", sNewContext);
 
         session.setAttribute("m_VD", m_VD);
         ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
   }
 
   /**
   * Called from doCreateDEActions.
   * Calls 'setAC.setDEValueFromPage' to set the DEC data from the page.
   * Calls 'setAC.setValidatePageValuesDE' to validate the data.
   * Loops through the vector vValidate to check if everything is valid and
   * Calls 'doInsertDE' to insert the data.
   * If vector contains invalid fields, forwards to validation page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doSubmitDE(HttpServletRequest req, HttpServletResponse res) 
    throws Exception
    {
       HttpSession session = req.getSession();
       session.setAttribute("sCDEAction", "validate");
       DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
       if (m_DE == null) m_DE = new DE_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setDEValueFromPage(req, res, m_DE);
       m_setAC.setValidatePageValuesDE(req, res, m_DE, getAC);
       session.setAttribute("m_DE", m_DE);
 
       boolean isValid = true;
       boolean isValidFlag = true;
       Vector vValidate = new Vector();
       vValidate = (Vector)req.getAttribute("vValidate");
       if (vValidate == null)
        isValid = false;
       else
       {
         for (int i = 0; vValidate.size()>i; i = i+3)
         {
           String sStat = (String)vValidate.elementAt(i+2);
           if((sStat.equals("Valid") || sStat.equals("Warning: A Data Element with a duplicate DEC and VD pair already exists within the selected context. ")))
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
         doInsertDE(req,res);
       }
    } // end of doSumitDE
 
   /**
   * Called from doCreateDECActions.
   * Calls 'setAC.setDECValueFromPage' to set the DEC data from the page.
   * Calls 'setAC.setValidatePageValuesDEC' to validate the data.
   * Loops through the vector vValidate to check if everything is valid and
   * Calls 'doInsertDEC' to insert the data.
   * If vector contains invalid fields, forwards to validation page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doSubmitDEC(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       HttpSession session = req.getSession();
       session.setAttribute("sDECAction", "validate");
       DEC_Bean m_DEC = (DEC_Bean)session.getAttribute("m_DEC");
       if (m_DEC == null) m_DEC = new DEC_Bean();
       EVS_Bean m_OC = new EVS_Bean();
       EVS_Bean m_PC = new EVS_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setDECValueFromPage(req, res, m_DEC);
       m_OC = (EVS_Bean)session.getAttribute("m_OC");
       m_PC = (EVS_Bean)session.getAttribute("m_PC");
 
       m_setAC.setValidatePageValuesDEC(req, res, m_DEC, m_OC, m_PC, getAC);
       session.setAttribute("m_DEC", m_DEC);
       boolean isValid = true;
       Vector vValidate = new Vector();
       vValidate = (Vector)req.getAttribute("vValidate");
       if (vValidate == null)
        isValid = false;
       else
       {
         for (int i = 0; vValidate.size()>i; i = i+3)
         {
           String sStat = (String)vValidate.elementAt(i+2);
           if(sStat.equals("Valid")==false)
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
         doInsertDEC(req,res);
       }
    } // end of doSumitDE
 
    /**
   * Called from doCreateVDActions.
   * Calls 'setAC.setVDValueFromPage' to set the VD data from the page.
   * Calls 'setAC.setValidatePageValuesVD' to validate the data.
   * Loops through the vector vValidate to check if everything is valid and
   * Calls 'doInsertVD' to insert the data.
   * If vector contains invalid fields, forwards to validation page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doSubmitVD(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       HttpSession session = req.getSession();
       session.setAttribute("sVDAction", "validate");
       VD_Bean m_VD = new VD_Bean();
       EVS_Bean m_OC = new EVS_Bean();
       EVS_Bean m_PC = new EVS_Bean();
       EVS_Bean m_REP = new EVS_Bean();
       EVS_Bean m_OCQ = new EVS_Bean();
       EVS_Bean m_PCQ = new EVS_Bean();
       EVS_Bean m_REPQ = new EVS_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setVDValueFromPage(req, res, m_VD);
       m_OC = (EVS_Bean)session.getAttribute("m_OC");
       m_PC = (EVS_Bean)session.getAttribute("m_PC");
       m_OCQ = (EVS_Bean)session.getAttribute("m_OCQ");
       m_PCQ = (EVS_Bean)session.getAttribute("m_PCQ");
       m_REP = (EVS_Bean)session.getAttribute("m_REP");
       m_REPQ = (EVS_Bean)session.getAttribute("m_REPQ");
       m_setAC.setValidatePageValuesVD(req, res, m_VD, m_OC, m_PC, m_REP, m_OCQ, m_PCQ, m_REPQ, getAC);
       session.setAttribute("m_VD", m_VD);
       boolean isValid = true;
       Vector vValidate = new Vector();
       vValidate = (Vector)req.getAttribute("vValidate");
       if (vValidate == null)
        isValid = false;
       else
       {
         for (int i = 0; vValidate.size()>i; i = i+3)
         {
           String sStat = (String)vValidate.elementAt(i+2);
           if(sStat.equals("Valid")==false)
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
         doInsertVD(req,res);
       }
    } // end of doSumitVD
 
   /**
   * Called from doCreatePVActions.
   * Calls 'setAC.setPVValueFromPage' to set the VD data from the page.
   * Calls 'setAC.setValidatePageValuesPV' to validate the data.
   * Loops through the vector vValidate to check if everything is valid and
   * Calls 'doInsertPV' to insert the data.
   * If vector contains invalid fields, forwards to validation page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doSubmitPV(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       HttpSession session = req.getSession();
       
       PV_Bean m_PV = new PV_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setPVValueFromPage(req, res, m_PV);
       m_setAC.setValidatePageValuesPV(req, res, m_PV, getAC);
 
       session.setAttribute("m_PV", m_PV);
       boolean isValid = true;
       Vector vValidate = new Vector();
       vValidate = (Vector)req.getAttribute("vValidate");
       if (vValidate == null)
        isValid = false;
       else
       {
         for (int i = 0; vValidate.size()>i; i = i+3)
         {
           String sStat = (String)vValidate.elementAt(i+2);
           if(sStat.equals("Valid")==false)
           {
            isValid = false;
           }
         }
       }
       if (isValid == false)
       {
         ForwardJSP(req, res, "/ValidatePVPage.jsp");
       }
       else
       {
         doInsertPV(req,res);
       }
    } // end of doSumitPV
 
     /**
   * Called from doCreateVMActions.
   * Calls 'setAC.setVMValueFromPage' to set the VM data from the page.
   * Calls 'setAC.setValidatePageValuesVM' to validate the data.
   * Loops through the vector vValidate to check if everything is valid and
   * Calls 'doInsertVM' to insert the data.
   * If vector contains invalid fields, forwards to validation page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doSubmitVM(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       HttpSession session = req.getSession();
       session.setAttribute("sVMAction", "validate");
       VM_Bean m_VM = new VM_Bean();
       PV_Bean m_PV = new PV_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setVMValueFromPage(req, res, m_VM);
       m_setAC.setValidatePageValuesVM(req, res, m_VM, getAC);
       session.setAttribute("m_VM", m_VM);
 
       boolean isValid = true;
       Vector vValidate = new Vector();
       vValidate = (Vector)req.getAttribute("vValidate");
       if (vValidate == null)
        isValid = false;
       else
       {
         for (int i = 0; vValidate.size()>i; i = i+3)
         {
           String sStat = (String)vValidate.elementAt(i+2);
           if(sStat.equals("Valid")==false)
           {
            isValid = false;
           }
         }
       }
       if (isValid == false)
       {
         ForwardJSP(req, res, "/ValidateVMPage.jsp");
       }
       else
       {
         doInsertVM(req,res);
       }
    } // end of doSumitVM
 
   /**
   * The doValidateDE method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'doCreateDEActions' method.
   * Calls 'setAC.setDEValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesDE' to validate the data.
   * Stores 'm_DE' bean in session.
   * Forwards the page 'ValidateDEPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
  public void doValidateDE(HttpServletRequest req, HttpServletResponse res)
  throws Exception
  {
       HttpSession session = req.getSession();
 
        // do below for versioning to check whether these two have changed
       session.setAttribute("DEEditAction", "DEEdit");
       session.setAttribute("sCDEAction", "validate");
       DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
       if (m_DE == null) m_DE = new DE_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setDEValueFromPage(req, res, m_DE);
       m_setAC.setValidatePageValuesDE(req, res, m_DE, getAC);
       session.setAttribute("m_DE", m_DE);
       ForwardJSP(req, res, "/ValidateDEPage.jsp");
   } // end of doValidateDE
 
   /**
   * The doValidateDE method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'doCreateDEActions' method.
   * Calls 'setAC.setDEValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesDE' to validate the data.
   * Stores 'm_DE' bean in session.
   * Forwards the page 'ValidateDEPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
  public void doValidateDEBlockEdit(HttpServletRequest req, HttpServletResponse res)
  throws Exception
  {
       HttpSession session = req.getSession();
       session.setAttribute("sCDEAction", "validate");
       session.setAttribute("DEAction", "EditDE");
       DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
       if (m_DE == null) m_DE = new DE_Bean();
      // GetACService getAC = new GetACService(req, res, this);
       m_setAC.setDEValueFromPage(req, res, m_DE);      
       session.setAttribute("m_DE", m_DE);
       m_setAC.setValidateBlockEdit(req, res, "DataElement");
       session.setAttribute("DEEditAction", "DEBlockEdit");
       ForwardJSP(req, res, "/ValidateDEPage.jsp");
   } // end of doValidateDE
 
  /**
   * @param sDefSource string def source selected
   * @throws Exception
   */
  public String getSourceToken(String sDefSource)
    throws Exception
 {
     int index = -1;
     int length = 0;
     if(sDefSource != null)
       length = sDefSource.length();
     String pointStr = ": Concept Source ";
     index = sDefSource.indexOf(pointStr);
     if(index == -1) index = 0;
     if(index > 0 && length >18)
       sDefSource = sDefSource.substring((index + 17 ), sDefSource.length());
 
     return sDefSource;
 }      
       
    /**
   * The doValidateDEC method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'doCreateDECActions' 'doSubmitDEC' method.
   * Calls 'setAC.setDECValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesDEC' to validate the data.
   * Stores 'm_DEC' bean in session.
   * Forwards the page 'ValidateDECPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doValidateDEC(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
        // do below for versioning to check whether these two have changed
       session.setAttribute("DECPageAction", "validate");      //store the page action in attribute
       DEC_Bean m_DEC = (DEC_Bean)session.getAttribute("m_DEC");
       if (m_DEC == null) m_DEC = new DEC_Bean();
       EVS_Bean m_OC = new EVS_Bean();
       EVS_Bean m_PC = new EVS_Bean();
       EVS_Bean m_OCQ = new EVS_Bean();
       EVS_Bean m_PCQ = new EVS_Bean();
 
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setDECValueFromPage(req, res, m_DEC);
       m_OC = (EVS_Bean)session.getAttribute("m_OC");
       m_PC = (EVS_Bean)session.getAttribute("m_PC");
 
       m_setAC.setValidatePageValuesDEC(req, res, m_DEC, m_OC, m_PC, getAC);
       session.setAttribute("m_DEC", m_DEC);
 
       ForwardJSP(req, res, "/ValidateDECPage.jsp");
   } // end of doValidateDEC
 
    /**
   * The doValidateDECBlockEdit method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'doCreateDECActions' 'doSubmitDEC' method.
   * Calls 'setAC.setDECValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesDEC' to validate the data.
   * Stores 'm_DEC' bean in session.
   * Forwards the page 'ValidateDECPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doValidateDECBlockEdit(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       session.setAttribute("DECPageAction", "validate");      //store the page action in attribute
       DEC_Bean m_DEC = (DEC_Bean)session.getAttribute("m_DEC");
       if (m_DEC == null) m_DEC = new DEC_Bean();
  
       m_setAC.setDECValueFromPage(req, res, m_DEC);
       session.setAttribute("m_DEC", m_DEC);
    
       m_setAC.setValidateBlockEdit(req, res, "DataElementConcept");
       session.setAttribute("DECEditAction", "DECBlockEdit");
       ForwardJSP(req, res, "/ValidateDECPage.jsp");
   } // end of doValidateDEC
 
   /**
   * The doValidateVD method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'doCreateVDActions', 'doSubmitVD' method.
   * Calls 'setAC.setVDValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesVD' to validate the data.
   * Stores 'm_VD' bean in session.
   * Forwards the page 'ValidateVDPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doValidateVD(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
       HttpSession session = req.getSession();
       String sAction = (String)req.getParameter("newCDEPageAction");
       if(sAction == null) sAction = "";
       // do below for versioning to check whether these two have changed
       VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();  
       EVS_Bean m_OC = new EVS_Bean();
       EVS_Bean m_PC = new EVS_Bean();
       EVS_Bean m_REP = new EVS_Bean();
       EVS_Bean m_OCQ = new EVS_Bean();
       EVS_Bean m_PCQ = new EVS_Bean();
       EVS_Bean m_REPQ = new EVS_Bean();
       GetACService getAC = new GetACService(req, res, this);
       session.setAttribute("VDPageAction", "validate");      //store the page action in attribute
       m_setAC.setVDValueFromPage(req, res, m_VD);
       m_OC = (EVS_Bean)session.getAttribute("m_OC");
       m_PC = (EVS_Bean)session.getAttribute("m_PC");
       m_OCQ = (EVS_Bean)session.getAttribute("m_OCQ");
       m_PCQ = (EVS_Bean)session.getAttribute("m_PCQ");
       m_REP = (EVS_Bean)session.getAttribute("m_REP");
       m_REPQ = (EVS_Bean)session.getAttribute("m_REPQ");
       m_setAC.setValidatePageValuesVD(req, res, m_VD, m_OC, m_PC, m_REP, m_OCQ, m_PCQ, m_REPQ, getAC);
       session.setAttribute("m_VD", m_VD);
       if(sAction.equals("Enum") || sAction.equals("NonEnum") || sAction.equals("EnumByRef"))
         ForwardJSP(req, res, "/CreateVDPage.jsp");
       else
        ForwardJSP(req, res, "/ValidateVDPage.jsp");
   } // end of doValidateVD
 
     /**
   * The doSetVDPage method gets the values from page the user filled out,
   * Calls 'setAC.setVDValueFromPage' to set the data from the page to the bean.
   * Stores 'm_VD' bean in session.
   * Forwards the page 'CreateVDPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   
   public void doSetVDPage(HttpServletRequest req, HttpServletResponse res, String sOrigin)
   throws Exception
   {
       HttpSession session = req.getSession();
       String sAction = (String)req.getParameter("newCDEPageAction");
       if(sAction == null) sAction = "";
     // do below for versioning to check whether these two have changed
       VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
       EVS_Bean m_OC = new EVS_Bean();
       EVS_Bean m_PC = new EVS_Bean();
       EVS_Bean m_REP = new EVS_Bean();
       EVS_Bean m_OCQ = new EVS_Bean();
       EVS_Bean m_PCQ = new EVS_Bean();
       EVS_Bean m_REPQ = new EVS_Bean();
       m_setAC.setVDValueFromPage(req, res, m_VD);
       session.setAttribute("m_VD", m_VD);        
       m_OC = (EVS_Bean)session.getAttribute("m_OC");
       m_PC = (EVS_Bean)session.getAttribute("m_PC");
       m_OCQ = (EVS_Bean)session.getAttribute("m_OCQ");
       m_PCQ = (EVS_Bean)session.getAttribute("m_PCQ");
       m_REP = (EVS_Bean)session.getAttribute("m_REP");
       m_REPQ = (EVS_Bean)session.getAttribute("m_REPQ");
       //check if pvs are used in the form when type is changed to non enumerated.
       if (!sAction.equals("Enum"))
       {
         //get vdid from the bean
         VD_Bean vdBean = (VD_Bean)session.getAttribute("m_VD");
         String sVDid = vdBean.getVD_VD_IDSEQ();
         boolean isExist = false;
         if (sOrigin.equals("Edit"))
         {
           //call function to check if relationship exists
           SetACService setAC = new SetACService(this);
           isExist = setAC.checkPVQCExists(req, res, sVDid, "");
           if (isExist)
           {
             String sMsg = "Unable to change Value Domain type to Non-Enumerated " + 
                  "because one or more Permissible Values are being used in a Case Report Form. \\n" +
                  "Please create a new version of this Value Domain to change the type to Non-Enumerated.";
             session.setAttribute("statusMessage", sMsg);
             vdBean.setVD_TYPE_FLAG("E");
             session.setAttribute("m_VD", vdBean);              
           }
         }
         //mark all the pvs as deleted to remove them while submitting.
         if (!isExist)
         {
           Vector vVDPVs = (Vector)session.getAttribute("VDPVList");
           if (vVDPVs != null)
           {
             //set each bean as deleted to handle later
             for (int i=0; i<vVDPVs.size(); i++)
             {
               PV_Bean pvBean = (PV_Bean)vVDPVs.elementAt(i);
               pvBean.setVP_SUBMIT_ACTION("DEL");
               vVDPVs.setElementAt(pvBean, i);
             }
             //reset the attribute
             session.setAttribute("VDPVList", vVDPVs);
           }
         }
       }
       else
       {
         //remove meta parents since it is not needed for enum types
         Vector vParentCon = (Vector)session.getAttribute("VDParentConcept");
         if (vParentCon == null) vParentCon = new Vector();    
         for (int i=0; i<vParentCon.size(); i++)
         {
           EVS_Bean ePar = (EVS_Bean)vParentCon.elementAt(i);
           if (ePar == null) ePar = new EVS_Bean();
           String parDB = ePar.getEVS_DATABASE();
           if (parDB != null && parDB.equals("NCI Metathesaurus"))
           {
             ePar.setCON_AC_SUBMIT_ACTION("DEL");
             vParentCon.setElementAt(ePar, i);
           }
         }
         session.setAttribute("VDParentConcept", vParentCon);
       }
   } // end of doValidateVD
   
   /**
    * gets the row number from the hiddenSelRow
    * Loops through the selected row and gets the evs bean for that row from the vector of evs search results.
    * adds it to vList vector and return the vector back
    * @param req HttpServletRequest
    * @param vList Existing Vector of EVS Beans
    * @return Vector of EVS Beans
    * @throws java.lang.Exception
    */
   public EVS_Bean getEVSSelRow(HttpServletRequest req) throws Exception
   {
     HttpSession session = req.getSession();   
     //get the result vector from the session
     EVS_Bean eBean = new EVS_Bean();
     Vector vRSel = (Vector)session.getAttribute("vACSearch");
     if (vRSel == null) vRSel = new Vector();
     //get the array from teh hidden list
     String selRows[] = req.getParameterValues("hiddenSelRow");
     if (selRows == null)
       session.setAttribute("StatusMessage", "Unable to select Concept, please try again");    
     else
     {
       //loop through the array of strings
       for (int i=0; i<selRows.length; i++)
       {
         String thisRow = selRows[i];
         Integer IRow = new Integer(thisRow);
         int iRow = IRow.intValue();
         if (iRow < 0 || iRow > vRSel.size())
           session.setAttribute("StatusMessage", "Row size is either too big or too small.");
         else
           eBean = (EVS_Bean)vRSel.elementAt(iRow);
       }      
     }   
     return eBean;
   }
   /**
    * gets the row number from the hiddenSelRow
    * Loops through the selected row and gets the evs bean for that row from the vector of evs search results.
    * adds it to vList vector and return the vector back
    * @param req HttpServletRequest
    * @param vList Existing Vector of EVS Beans
    * @return Vector of EVS Beans
    * @throws java.lang.Exception
    */
   public Vector getEVSSelRowVector(HttpServletRequest req, Vector vList) throws Exception
   {
     HttpSession session = req.getSession();   
     //get the result vector from the session
     Vector vRSel = (Vector)session.getAttribute("vACSearch");
     if (vRSel == null) vRSel = new Vector();
     //get the array from teh hidden list
     String selRows[] = req.getParameterValues("hiddenSelRow");
 
     if (selRows == null)
       session.setAttribute("StatusMessage", "Unable to select Concept, please try again");    
     else
     {
       //loop through the array of strings
       for (int i=0; i<selRows.length; i++)
       {
         String thisRow = selRows[i];
         Integer IRow = new Integer(thisRow);
         int iRow = IRow.intValue();
         if (iRow < 0 || iRow > vRSel.size())
           session.setAttribute("StatusMessage", "Row size is either too big or too small.");
         else
         {
           EVS_Bean eBean = (EVS_Bean)vRSel.elementAt(iRow);
           //make sure it doesn't exist in the list
           boolean isExist = false;
           if (vList != null && vList.size() > 0)
           {
             for (int k=0; k<vList.size(); k++)
             {
               EVS_Bean thisBean = (EVS_Bean)vList.elementAt(k);
               if (thisBean.getNCI_CC_VAL().equals(eBean.getNCI_CC_VAL()))
               {
                 String acAct = thisBean.getCON_AC_SUBMIT_ACTION();
                 //put it back if was deleted
                 if (acAct != null && acAct.equals("DEL"))
                 {
                   thisBean.setCON_AC_SUBMIT_ACTION("INS");
                   vList.setElementAt(thisBean, k);
                 }
                 isExist = true;
               }
             }
           }
           if (isExist == false)
           {
             eBean.setCON_AC_SUBMIT_ACTION("INS");
             vList.addElement(eBean);  
           }          
         }
       }      
     }   
     return vList;
   }
   /**
    * makes the vd's system generated name
    * @param req HttpServletRequest object
    * @param vd current vd bean
    * @param vParent vector of seelected parents
    * @return modified vd bean
    * @throws java.lang.Exception
    */
   public VD_Bean doGetVDSystemName(HttpServletRequest req, VD_Bean vd, Vector vParent) throws Exception
   {
     //make the system generated name
     String sysName = "";
     for (int i = vParent.size()-1; i > -1; i--)
     {
       EVS_Bean par = (EVS_Bean)vParent.elementAt(i);
       String evsDB = par.getEVS_DATABASE();
       String subAct = par.getCON_AC_SUBMIT_ACTION();
       if (subAct != null && !subAct.equals("DEL") && evsDB != null && !evsDB.equals("Non_EVS"))
       {
         //add the concept id to sysname if less than 20 characters
         if (sysName.equals("") || sysName.length() < 20)
           sysName += par.getNCI_CC_VAL() + ":";
         else
           break;
       }
     }
     //append vd public id and version in the end
     if (vd.getVD_VD_ID() != null) sysName += vd.getVD_VD_ID();
     if (vd.getVD_VERSION() != null) sysName += "v" + vd.getVD_VERSION();
     vd.setAC_SYS_PREF_NAME(sysName);  //store it in vd bean
     //limit to 30 characters
     if (sysName.length() > 30)
       sysName = sysName.substring(sysName.length()-30);
 
     //make system name preferrd name if sys was selected
     String selNameType = (String)req.getParameter("rNameConv");
     if (selNameType != null && selNameType.equals("SYS"))
       vd.setVD_PREFERRED_NAME(sysName);
     //store the keyed in text in the user field for later use.
     String sPrefName = (String)req.getParameter("txPreferredName");
     if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
       vd.setAC_USER_PREF_NAME(sPrefName);
       
     return vd;
   }
   /**
    * called when parent is added to the page
    * @param req
    * @param res
    * @throws java.lang.Exception
    */
   public void doSelectParentVD(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
     HttpSession session = req.getSession();        
     //store the evs bean for the parent concepts in vector and in session.    
     Vector vParentCon = (Vector)session.getAttribute("VDParentConcept");
     if (vParentCon == null) vParentCon = new Vector();
     vParentCon = this.getEVSSelRowVector(req, vParentCon);
     session.setAttribute("VDParentConcept", vParentCon);
     
     VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
     m_setAC.setVDValueFromPage(req, res, m_VD);
     //make vd's system preferred name
     m_VD = this.doGetVDSystemName(req, m_VD, vParentCon);
     
     session.setAttribute("m_VD", m_VD);
     //store the last page action in request
     req.setAttribute("LastAction", "parSelected");
   } // end
   
   /**
    * stores the non evs parent reference information in evs bean and to parent list.
    * reference document is matched like this with the evs bean adn stored in parents vector as a evs bean
    * setNCI_CC_VAL as document type (VD REFERENCE)
    * setLONG_NAME as document name
    * setEVS_DATABASE as Non_EVS text 
    * setPREFERRED_DEFINITION as document text
    * setEVS_DEF_SOURCE as document url
    * 
    * @param req HttpServletRequest object
    * @param res HttpServletResponse object
    * @throws java.lang.Exception
    */
   public void doNonEVSReference(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
     HttpSession session = req.getSession();   
     //document type  (concept code)
     String sParCode = "VD REFERENCE";
     //document name  (concept long name)
     String sParName = (String)req.getParameter("hiddenParentName");
     if(sParName == null) sParName = "";
     //document text  (concept definition)
     String sParDef = (String)req.getParameter("hiddenParentCode");
     if(sParDef == null) sParDef = "";
     //document url  (concept defintion source)
     String sParDefSource = (String)req.getParameter("hiddenParentDB");
     if(sParDefSource == null) sParDefSource = "";
     //parent type (concept database)
     String sParDB = "Non_EVS";
     
     //make a string for view    
     String sParListString = sParName + "        " + sParDB; 
     if(sParListString == null) sParListString = "";
    
     //store the evs bean for the parent concepts in vector and in session.    
     Vector vParentCon = (Vector)session.getAttribute("VDParentConcept");
     if (vParentCon == null) vParentCon = new Vector();
     EVS_Bean parBean = new EVS_Bean();
     parBean.setNCI_CC_VAL(sParCode);  //doc type
     parBean.setLONG_NAME(sParName);   //doc name
     parBean.setEVS_DATABASE(sParDB);  //ref type (non evs)
     parBean.setPREFERRED_DEFINITION(sParDef);  //doc text
     parBean.setEVS_DEF_SOURCE(sParDefSource);  //doc url
     parBean.setCON_AC_SUBMIT_ACTION("INS");
     vParentCon.addElement(parBean);
     session.setAttribute("VDParentConcept", vParentCon);
     
     VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
     m_setAC.setVDValueFromPage(req, res, m_VD);
     session.setAttribute("m_VD", m_VD); 
     //store the last page action in request
     req.setAttribute("LastAction", "parSelected");
   } // end
   
   /**
    * fills in the non evs parent attributes and sends back to create non evs parent page to view details
    * @param req HttpServletRequest object
    * @param res HttpServletResponse object
    * @throws java.lang.Exception
    */
   private void doNonEVSPageAction(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
     HttpSession session = req.getSession();
     String sPageAct = (String)req.getParameter("actSelect");
     if (sPageAct.equals("viewParent"))
     {
       Vector vParentCon = (Vector)session.getAttribute("VDParentConcept");
       if (vParentCon != null)
       {
         String selName = (String)req.getParameter("txtRefName");
         for (int i=0; i<vParentCon.size(); i++)
         {
           EVS_Bean eBean = (EVS_Bean)vParentCon.elementAt(i);
           String thisName = eBean.getLONG_NAME();
           String sDB = eBean.getEVS_DATABASE();
           //get the selected name from the vector
           if (selName != null && thisName != null && sDB != null && selName.equals(thisName) && sDB.equals("Non_EVS"))
           {
             req.setAttribute("SelectedVDParent", eBean);
             break;
           }
         }
       }
       this.ForwardJSP(req, res, "/NonEVSSearchPage.jsp");
     }
     else
     { //send back to block search for parent
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
   private void doRemoveParent(HttpServletRequest req, HttpServletResponse res, 
       String sPVAction, String vdPage)  throws Exception
   {
     HttpSession session = req.getSession();   
     Vector vParentCon = (Vector)session.getAttribute("VDParentConcept");
     if (vParentCon == null) vParentCon = new Vector();    
     //get the selected parent info from teh request
     String sParentCC = (String)req.getParameter("selectedParentConceptCode");
     
     if (sParentCC != null)
     {
       for (int i=0; i<vParentCon.size(); i++)
       {
         EVS_Bean eBean = (EVS_Bean)vParentCon.elementAt(i);
         if (eBean == null) eBean = new EVS_Bean();
         String thisParent = eBean.getNCI_CC_VAL();
         if (thisParent == null) thisParent = "";
         String thisParentName = eBean.getLONG_NAME();
         if (thisParentName == null) thisParentName = "";
         String thisParentDB = eBean.getEVS_DATABASE();
         if(thisParentDB.equals("NCI Thesaurus")) thisParentDB = "NCI_Thesaurus";
         if (thisParentDB == null) thisParentDB = "";
   //System.out.println("doRemoveParent: thisParentName: "  + thisParentName + " thisParentDB: " + thisParentDB);
         if (sParentCC.equals(thisParent))
         {
           String strHTML = "";
           EVSMasterTree tree = new EVSMasterTree(req, thisParentDB, this);
           strHTML = tree.refreshTree(thisParentName, "false");
           strHTML = tree.refreshTree("parentTree"+thisParentName, "false");
           
           if (sPVAction.equals("removePVandParent"))
           {
             Vector vVDPVList = (Vector)session.getAttribute("VDPVList");
             if (vVDPVList == null) vVDPVList = new Vector();
             //loop through the vector of pvs to get matched parent
             for (int j=0; j<vVDPVList.size(); j++)
             {
               PV_Bean pvBean = (PV_Bean)vVDPVList.elementAt(j);
               if (pvBean == null) pvBean = new PV_Bean();
               EVS_Bean pvParent = (EVS_Bean)pvBean.getPARENT_CONCEPT();
               if (pvParent == null) pvParent = new EVS_Bean();
               String pvParCon = pvParent.getNCI_CC_VAL();
               //match the parent concept with the pv's parent concept
               if (thisParent.equals(pvParCon))
               {
                 pvBean.setVP_SUBMIT_ACTION("DEL");   //mark the vp as deleted
                 String pvID = pvBean.getPV_PV_IDSEQ();
                 vVDPVList.setElementAt(pvBean, j);
               }
             }
             session.setAttribute("VDPVList", vVDPVList);            
           }
           //mark the parent as delected and leave
           eBean.setCON_AC_SUBMIT_ACTION("DEL");
           vParentCon.setElementAt(eBean,i);
           break;
         }
       }
     }
     session.setAttribute("VDParentConcept", vParentCon);
     //make sure all other changes are stored back in vd
     VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
     m_setAC.setVDValueFromPage(req, res, m_VD);
     //make vd's system preferred name
     m_VD = this.doGetVDSystemName(req, m_VD, vParentCon);
     session.setAttribute("m_VD", m_VD); 
     //make the selected parent in hte session empty
     session.setAttribute("SelectedParentName", "");
     session.setAttribute("SelectedParentCC", "");
     session.setAttribute("SelectedParentDB", "");
     session.setAttribute("SelectedParentMetaSource", "");
     //forward teh page according to vdPage 
     if (vdPage.equals("editVD"))
        ForwardJSP(req, res, "/EditVDPage.jsp");
     else
        ForwardJSP(req, res, "/CreateVDPage.jsp");                 
   }
   
 /**
    * splits the vd rep term from cadsr into individual concepts
    * 
    * @param sComp name of the searched component
    * @param m_Bean selected EVS bean
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    * @param pref name action either to make new name or apppend to hte old
    *
    * @throws Exception
    * 
    */
   public void splitIntoConceptsVD(String sComp, EVS_Bean m_Bean, HttpServletRequest req,
                                 HttpServletResponse res, String nameAction)
   {   
   try
   {
     HttpSession session = req.getSession(); 
     String sSelRow = "";
     VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");
     if (m_VD == null) m_VD = new VD_Bean();
     m_setAC.setVDValueFromPage(req, res, m_VD);
     Vector vRepTerm = (Vector)session.getAttribute("vRepTerm");
     if (vRepTerm == null) vRepTerm = new Vector();
     String sCondr = m_Bean.getCONDR_IDSEQ();
     String sLongName = m_Bean.getLONG_NAME();
     String sIDSEQ = m_Bean.getIDSEQ();
     if(sComp.equals("RepTerm") || sComp.equals("RepQualifier"))
     {
         m_VD.setVD_REP_TERM(sLongName);
         m_VD.setVD_REP_IDSEQ(sIDSEQ);
     }
     String sRepTerm = m_VD.getVD_REP_TERM();
       if (sCondr != null && !sCondr.equals(""))
       {
         GetACService getAC = new GetACService(req, res, this);
         Vector vCon = getAC.getAC_Concepts(sCondr, null, true);
         if (vCon != null && vCon.size() > 0)
         {
           for (int j=0; j<vCon.size(); j++)
           {
             EVS_Bean bean = new EVS_Bean();
             bean = (EVS_Bean)vCon.elementAt(j);
             if(bean != null)
             {
               if(j == 0) // Primary Concept
               {
                 bean.setCON_AC_SUBMIT_ACTION("INS");
                 bean.setCONTE_IDSEQ(m_VD.getVD_CONTE_IDSEQ());
                 if(sComp.equals("RepTerm") || sComp.equals("RepQualifier"))
                 {
                   m_VD.setVD_REP_TERM(sLongName);
                   if(vRepTerm.size()<1)
                     vRepTerm.addElement(bean);
                   else
                     vRepTerm.setElementAt(bean, 0);
                   session.setAttribute("vRepTerm", vRepTerm);
                   session.setAttribute("newRepTerm", "true");              
                   m_VD.setVD_REP_NAME_PRIMARY(bean.getLONG_NAME());
                   m_VD.setVD_REP_CONCEPT_CODE(bean.getNCI_CC_VAL());
                   m_VD.setVD_REP_EVS_CUI_ORIGEN(bean.getEVS_DATABASE());   
                   session.setAttribute("m_REP", bean);
                   session.setAttribute("selRepRow", sSelRow);
                   //add to name if appending
                   if (nameAction.equals("appendName"))
                     m_VD = this.doGetVDNames(req, res, bean, "Search", m_VD);
                 } 
               }
               else //Secondary Concepts
               {
                 bean.setCON_AC_SUBMIT_ACTION("INS");
                 bean.setCONTE_IDSEQ(m_VD.getVD_CONTE_IDSEQ());
                 if(sComp.equals("RepTerm") || sComp.equals("RepQualifier"))
                 {
                     bean = (EVS_Bean)vCon.elementAt(j);
                     if(bean != null)
                     {
                       vRepTerm.addElement(bean);
                       session.setAttribute("vRepTerm", vRepTerm);
                       session.setAttribute("newRepTerm", "true");
                       Vector vRepQualifierNames = m_VD.getVD_REP_QUALIFIER_NAMES();
                       if (vRepQualifierNames == null) vRepQualifierNames = new Vector();
                       vRepQualifierNames.addElement(bean.getLONG_NAME());
                       Vector vRepQualifierCodes = m_VD.getVD_REP_QUALIFIER_CODES();
                       if (vRepQualifierCodes == null) vRepQualifierCodes = new Vector();
                       vRepQualifierCodes.addElement(bean.getNCI_CC_VAL());
                       Vector vRepQualifierDB = m_VD.getVD_REP_QUALIFIER_DB();
                       if (vRepQualifierDB == null) vRepQualifierDB = new Vector();
                       vRepQualifierDB.addElement(bean.getEVS_DATABASE());
                       m_VD.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
                       m_VD.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
                       m_VD.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
                       session.setAttribute("vRepQResult", null);
                       session.setAttribute("m_REPQ", bean);
                       session.setAttribute("selRepQRow", sSelRow);
                       //add to name if appending
                       if (nameAction.equals("appendName"))
                         m_VD = this.doGetVDNames(req, res, bean, "Search", m_VD);
                     }
                 }
               }
             }
           }
         }
       }
     }
    catch (Exception e)
    {
       this.logger.fatal("ERROR - splitintoConceptVD : " + e.toString());
   }
 }
 
   
 /**
    * splits the dec object class or property from cadsr into individual concepts
    * 
    * @param sComp name of the searched component
    * @param m_Bean selected EVS bean
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    * @param pref name action either to make new name or apppend to hte old
    *
    * @throws Exception
    * 
    */
   public void splitIntoConcepts(String sComp, EVS_Bean m_Bean, HttpServletRequest req,
                                 HttpServletResponse res, String nameAction)
   {      
   try
   {
     HttpSession session = req.getSession(); 
     String sSelRow = "";
     DEC_Bean m_DEC = (DEC_Bean)session.getAttribute("m_DEC");
     if (m_DEC == null) m_DEC = new DEC_Bean();
     m_setAC.setDECValueFromPage(req, res, m_DEC);
     Vector vObjectClass = (Vector)session.getAttribute("vObjectClass");
     if (vObjectClass == null) vObjectClass = new Vector();
     Vector vProperty = (Vector)session.getAttribute("vProperty");
     if (vProperty == null) vProperty = new Vector();
     String sCondr = m_Bean.getCONDR_IDSEQ();
     String sLongName = m_Bean.getLONG_NAME();
     String sIDSEQ = m_Bean.getIDSEQ();
     if(sIDSEQ == null) sIDSEQ = "";
     if(sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier"))
     {
       m_DEC.setDEC_OCL_NAME(sLongName);
       m_DEC.setDEC_OCL_IDSEQ(sIDSEQ);
     }
     else if(sComp.equals("Property") || sComp.equals("PropertyClass") || sComp.equals("PropertyQualifier"))
     { 
       m_DEC.setDEC_PROPL_NAME(sLongName);
       m_DEC.setDEC_PROPL_IDSEQ(sIDSEQ);
     }
     String sObjClass = m_DEC.getDEC_OCL_NAME();
     if (sCondr != null && !sCondr.equals(""))
     {
         GetACService getAC = new GetACService(req, res, this);
         Vector vCon = getAC.getAC_Concepts(sCondr, null, true);
         if (vCon != null && vCon.size() > 0)
         {
             // Primary concept, last element in the vector
             EVS_Bean bean = new EVS_Bean();
             bean = (EVS_Bean)vCon.elementAt(0);
             if(bean != null)
             {
                 bean.setCON_AC_SUBMIT_ACTION("INS");
                 bean.setCONTE_IDSEQ(m_DEC.getDEC_CONTE_IDSEQ());
                 if(sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier"))
                 {
                   m_DEC.setDEC_OCL_NAME(sLongName);
                   if(vObjectClass.size()<1)
                     vObjectClass.addElement(bean);
                   else
                     vObjectClass.setElementAt(bean, 0);
                   session.setAttribute("vObjectClass", vObjectClass);
                   session.setAttribute("newObjectClass", "true");
                   m_DEC.setDEC_OCL_NAME_PRIMARY(bean.getLONG_NAME());
                   m_DEC.setDEC_OC_CONCEPT_CODE(bean.getNCI_CC_VAL());
                   m_DEC.setDEC_OC_EVS_CUI_ORIGEN(bean.getEVS_DATABASE());   
                   session.setAttribute("m_OC", bean);
                   session.setAttribute("selObjRow", sSelRow);
                   //add to name if appending
                   if (nameAction.equals("appendName"))
                     m_DEC = this.doGetDECNames(req, res, bean, "Search", m_DEC);
                 }
                 else if(sComp.equals("Property") || sComp.equals("PropertyClass")
                         || sComp.equals("PropertyQualifier"))
                 {
                   m_DEC.setDEC_PROPL_NAME(sLongName);
                   if(vProperty.size()<1)
                     vProperty.addElement(bean);
                   else
                     vProperty.setElementAt(bean, 0);
                   session.setAttribute("vProperty", vProperty);
                   session.setAttribute("newProperty", "true");
                   m_DEC.setDEC_PROPL_NAME_PRIMARY(bean.getLONG_NAME());
                   m_DEC.setDEC_PROP_CONCEPT_CODE(bean.getNCI_CC_VAL());
                   m_DEC.setDEC_PROP_EVS_CUI_ORIGEN(bean.getEVS_DATABASE());
                   session.setAttribute("m_PC", bean);
                   session.setAttribute("selPropRow", sSelRow);
                   //add to name if appending
                   if (nameAction.equals("appendName"))
                     m_DEC = this.doGetDECNames(req, res, bean, "Search", m_DEC);
                 }
           }
           //Secondary Concepts
           for (int j=1; j<vCon.size(); j++)
           {
            // int g = vCon.size()-1; 
             bean = new EVS_Bean();
             bean = (EVS_Bean)vCon.elementAt(j);
             if(bean != null)
             {
                 bean.setCON_AC_SUBMIT_ACTION("INS");
                 bean.setCONTE_IDSEQ(m_DEC.getDEC_CONTE_IDSEQ());
                 if(sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier"))
                 {
                     bean = (EVS_Bean)vCon.elementAt(j);
                     if(bean != null)
                     {
                       vObjectClass.addElement(bean);
                       session.setAttribute("vObjectClass", vObjectClass);
                       session.setAttribute("newObjectClass", "true");
                       Vector vOCQualifierNames = m_DEC.getDEC_OC_QUALIFIER_NAMES();
                       if (vOCQualifierNames == null) vOCQualifierNames = new Vector();
                       vOCQualifierNames.addElement(bean.getLONG_NAME());
                       Vector vOCQualifierCodes = m_DEC.getDEC_OC_QUALIFIER_CODES();
                       if (vOCQualifierCodes == null) vOCQualifierCodes = new Vector();
                       vOCQualifierCodes.addElement(bean.getNCI_CC_VAL());
                       Vector vOCQualifierDB = m_DEC.getDEC_OC_QUALIFIER_DB();
                       if (vOCQualifierDB == null) vOCQualifierDB = new Vector();
                       vOCQualifierDB.addElement(bean.getEVS_DATABASE());
                       m_DEC.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
                       m_DEC.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
                       m_DEC.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
                       session.setAttribute("m_OCQ", bean);
                       session.setAttribute("selObjQRow", sSelRow);
                       //add to name if appending
                       if (nameAction.equals("appendName"))
                         m_DEC = this.doGetDECNames(req, res, bean, "Search", m_DEC);
                     }
                 }
                  else if(sComp.equals("Property") || sComp.equals("PropertyClass")
                         || sComp.equals("PropertyQualifier"))
                 {
                    // EVS_Bean bean = new EVS_Bean();
                     bean = (EVS_Bean)vCon.elementAt(j);
                     if(bean != null)
                     {
                       vProperty.addElement(bean);
                       session.setAttribute("vProperty", vProperty);
                       session.setAttribute("newProperty", "true");
                       Vector vPropQualifierNames = m_DEC.getDEC_PROP_QUALIFIER_NAMES();
                       if (vPropQualifierNames == null) vPropQualifierNames = new Vector();
                       vPropQualifierNames.addElement(bean.getLONG_NAME());
                       Vector vPropQualifierCodes = m_DEC.getDEC_PROP_QUALIFIER_CODES();
                       if (vPropQualifierCodes == null) vPropQualifierCodes = new Vector();
                       vPropQualifierCodes.addElement(bean.getNCI_CC_VAL());
                       Vector vPropQualifierDB = m_DEC.getDEC_PROP_QUALIFIER_DB();
                       if (vPropQualifierDB == null) vPropQualifierDB = new Vector(); 
                       vPropQualifierDB.addElement(bean.getEVS_DATABASE());
                       m_DEC.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
                       m_DEC.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
                       m_DEC.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
                       session.setAttribute("m_PCQ", bean);
                       session.setAttribute("selObjQRow", sSelRow);
                       //add to name if appending
                       if (nameAction.equals("appendName"))
                         m_DEC = this.doGetDECNames(req, res, bean, "Search", m_DEC);
                     }
                 }
               }
             }
           }//vCon != null
         }//sCondr != null
     }
     catch (Exception e)
     {
       this.logger.fatal("ERROR - splitintoConcept : " + e.toString());
     }
   }
 
   /**
    * makes three types of preferred names and stores it in the bean
    * @param req HttpServletRequest object
    * @param res HttpServletResponse object
    * @param newBean new evs bean
    * @param nameAct string new name or apeend name 
    * @param pageDEC current dec bean
    * @return dec bean
    */
   public DEC_Bean doGetDECNames(HttpServletRequest req, HttpServletResponse res, 
       EVS_Bean newBean, String nameAct, DEC_Bean pageDEC)
   {
     HttpSession session = req.getSession();
     if (pageDEC == null) pageDEC = (DEC_Bean)session.getAttribute("m_DEC");
     //get DEC object class and property names
     String sLongName = "";
     String sPrefName = "";
     String sAbbName = "";
     String sOCName = "";
     String sPropName = "";
     String sDef = "";
     //get the existing one if not restructuring the name but appending it
     if (newBean != null)
     {
       sLongName = pageDEC.getDEC_LONG_NAME();
       if (sLongName == null) sLongName = "";
       sDef = pageDEC.getDEC_PREFERRED_DEFINITION();
       if (sDef == null) sDef = "";
     }
     //get the typed text on to user name
     String selNameType = "";
     if (nameAct.equals("Search") || nameAct.equals("Remove"))
     {
       selNameType = (String)req.getParameter("rNameConv");
       sPrefName = (String)req.getParameter("txPreferredName");
       if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
         pageDEC.setAC_USER_PREF_NAME(sPrefName);
     }
     //get the object class into the long name and abbr name
     Vector vObjectClass = (Vector)session.getAttribute("vObjectClass");
     if (vObjectClass == null) vObjectClass = new Vector();
     //add the Object Class qualifiers first 
     for (int i = 1; vObjectClass.size()>i; i++)
     {
        EVS_Bean eCon = (EVS_Bean)vObjectClass.elementAt(i);
        if (eCon == null) eCon = new EVS_Bean();
        String conName = eCon.getLONG_NAME();
        if (conName == null) conName = "";
        if (!conName.equals(""))
        {
          //rearrange it  long name and definition
          if (newBean == null)
          {
              if (!sLongName.equals("")) sLongName += " ";
              sLongName += conName;
              if (!sDef.equals("")) sDef += "_";  //add definition
              sDef += eCon.getPREFERRED_DEFINITION();
          }
          if (!sAbbName.equals("")) sAbbName += "_";
          if (conName.length() > 3) sAbbName += conName.substring(0, 4);  //truncate to four letters
          else sAbbName += conName;
          //add object qualifiers to object class name
          if (!sOCName.equals("")) sOCName += " ";
          sOCName += conName;
        }
     }
     //add the Object Class primary
     if (vObjectClass != null && vObjectClass.size() > 0)
     {
        EVS_Bean eCon = (EVS_Bean)vObjectClass.elementAt(0);
        if (eCon == null) eCon = new EVS_Bean();
        String sPrimary = eCon.getLONG_NAME();
        if (sPrimary == null) sPrimary = "";
        if (!sPrimary.equals(""))
        {
          //rearrange it only long name and definition
          if (newBean == null)
          {
            if (!sLongName.equals("")) sLongName += " ";
            sLongName += sPrimary;
            if (!sDef.equals("")) sDef += "_";  //add definition
            sDef += eCon.getPREFERRED_DEFINITION();
          }
          if (!sAbbName.equals("")) sAbbName += "_";
          if (sPrimary.length() > 3) sAbbName += sPrimary.substring(0, 4);  //truncate to four letters
          else sAbbName += sPrimary;         
          //add primary object to object name
          if (!sOCName.equals("")) sOCName += " ";
          sOCName += sPrimary;
        }
     }
 
     //get the Property into the long name and abbr name
     Vector vProperty = (Vector)session.getAttribute("vProperty");
     if (vProperty == null) vProperty = new Vector();
     //add the property qualifiers first 
     for (int i = 1; vProperty.size()>i; i++)
     {
        EVS_Bean eCon = (EVS_Bean)vProperty.elementAt(i);
        if (eCon == null) eCon = new EVS_Bean();
        String conName = eCon.getLONG_NAME();
        if (conName == null) conName = "";
        if (!conName.equals(""))
        {
          //rearrange it  long name and definition
          if (newBean == null)
          {
              if (!sLongName.equals("")) sLongName += " ";
              sLongName += conName;
              if (!sDef.equals("")) sDef += "_";  //add definition
              sDef += eCon.getPREFERRED_DEFINITION();
          }
          if (!sAbbName.equals("")) sAbbName += "_";
          if (conName.length() > 3) sAbbName += conName.substring(0, 4);  //truncate to four letters
          else sAbbName += conName; 
          //add property qualifiers to property name
          if (!sPropName.equals("")) sPropName += " ";
          sPropName += conName;
        }
     }
     //add the property primary
     if (vProperty != null && vProperty.size() > 0)
     {
        EVS_Bean eCon = (EVS_Bean)vProperty.elementAt(0);
        if (eCon == null) eCon = new EVS_Bean();
        String sPrimary = eCon.getLONG_NAME();
        if (sPrimary == null) sPrimary = "";
        if (!sPrimary.equals(""))
        {
          //rearrange it only long name and definition
          if (newBean == null)
          {
            if (!sLongName.equals("")) sLongName += " ";
            sLongName += sPrimary;
            if (!sDef.equals("")) sDef += "_";  //add definition
            sDef += eCon.getPREFERRED_DEFINITION();
          }
          if (!sAbbName.equals("")) sAbbName += "_";
          if (sPrimary.length() > 3) sAbbName += sPrimary.substring(0, 4);  //truncate to four letters
          else sAbbName += sPrimary;  
          //add primary property to property name
          if (!sPropName.equals("")) sPropName += " ";
          sPropName += sPrimary;
        }
     }
 
     //truncate to 30 characters 
     if (sAbbName != null && sAbbName.length()>30)
       sAbbName = sAbbName.substring(0,30);
    //add the abbr name to vd bean and page is selected
     pageDEC.setAC_ABBR_PREF_NAME(sAbbName);
     //make abbr name name preferrd name if sys was selected
     if (selNameType != null && selNameType.equals("ABBR"))
       pageDEC.setDEC_PREFERRED_NAME(sAbbName); 
     //appending to the existing;
     if (newBean != null) 
     {
        String sSelectName = newBean.getLONG_NAME();
        if (!sLongName.equals("")) sLongName += " ";
        sLongName += sSelectName;
        if (!sDef.equals("")) sDef += "_";  //add definition
        sDef += newBean.getPREFERRED_DEFINITION();
     }
     //store the long names, definition, and usr name in vd bean if searched 
     if (nameAct.equals("Search"))
     {
       pageDEC.setDEC_LONG_NAME(sLongName);
       pageDEC.setDEC_PREFERRED_DEFINITION(sDef);
     }
     pageDEC.setDEC_OCL_NAME(sOCName);
     pageDEC.setDEC_PROPL_NAME(sPropName);
     if (nameAct.equals("Search") || nameAct.equals("Remove"))
     {
       pageDEC.setAC_SYS_PREF_NAME("(Generated by the System)");  //only for dec
       if (selNameType != null && selNameType.equals("SYS"))
         pageDEC.setDEC_PREFERRED_NAME(pageDEC.getAC_SYS_PREF_NAME()); 
     }
     return pageDEC;
   }
 
   /**
    * this method is used to create preferred name for VD 
    * names of all three types will be stored in the bean for later use
    * if type is changed, it populates name according to type selected. 
    * 
    * @param req HttpServletRequest
    * @param res HttpServletResponse
    * @param newBean new EVS bean to append the name to
    * @param nameAct string new name or apeend name 
    * @param pageVD current vd bean
    * @throws java.lang.Exception
    */
   public VD_Bean doGetVDNames(HttpServletRequest req, HttpServletResponse res, 
       EVS_Bean newBean, String nameAct, VD_Bean pageVD)
   {
     HttpSession session = req.getSession();
     if (pageVD == null) pageVD = (VD_Bean)session.getAttribute("m_VD");
     //get vd object class and property names
     String sLongName = "";
     String sPrefName = "";
     String sAbbName = "";
     String sDef = "";
     //get the existing one if not restructuring the name but appending it
     if (newBean != null)
     {
       sLongName = pageVD.getVD_LONG_NAME();
       if (sLongName == null) sLongName = "";
       sDef = pageVD.getVD_PREFERRED_DEFINITION();
       if (sDef == null) sDef = "";
     }
     //get the typed text on to user name
     String selNameType = "";
     if (nameAct.equals("Search") || nameAct.equals("Remove"))
     {
       selNameType = (String)req.getParameter("rNameConv");
       sPrefName = (String)req.getParameter("txPreferredName");
       if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
         pageVD.setAC_USER_PREF_NAME(sPrefName);
     }
     //get the object class into the long name and abbr name
     String sObjClass = pageVD.getVD_OBJ_CLASS();
     if (sObjClass == null) sObjClass = "";
     if (!sObjClass.equals(""))
     {
       //rearrange it long name
       if (newBean == null)
       {
         if (!sLongName.equals("")) sLongName += " ";  //add extra space if not empty
         sLongName += sObjClass;
         EVS_Bean mOC = (EVS_Bean)session.getAttribute("m_OC");
         if (mOC != null)
         {
           if (!sDef.equals("")) sDef += "_";  //add definition
           sDef += mOC.getPREFERRED_DEFINITION();
         }
       }
       if (!sAbbName.equals("")) sAbbName += "_";  //add underscore if not empty
       if (sObjClass.length()>3) sAbbName += sObjClass.substring(0,4);  //truncate to 4 letters
       else sAbbName = sObjClass;
     }
     //get the property into the long name and abbr name
     String sPropClass = pageVD.getVD_PROP_CLASS();
     if (sPropClass == null) sPropClass = "";
     if (!sPropClass.equals(""))
     {
       //rearrange it long name
       if (newBean == null)
       {
         if (!sLongName.equals("")) sLongName += " ";  //add extra space if not empty
         sLongName += sPropClass;
         EVS_Bean mPC = (EVS_Bean)session.getAttribute("m_PC");
         if (mPC != null)
         {
           if (!sDef.equals("")) sDef += "_";  //add definition
           sDef += mPC.getPREFERRED_DEFINITION();
         }
       }
       if (!sAbbName.equals("")) sAbbName += "_";  //add underscore if not empty
       if (sPropClass.length()>3) sAbbName += sPropClass.substring(0,4);  //truncate to 4 letters
       else sAbbName += sPropClass;
     }
     Vector vRep = (Vector)session.getAttribute("vRepTerm");
      if (vRep == null) vRep = new Vector();
     //add the qualifiers first 
     for (int i = 1; vRep.size()>i; i++)
     {
        EVS_Bean eCon = (EVS_Bean)vRep.elementAt(i);
        if (eCon == null) eCon = new EVS_Bean();
        String conName = eCon.getLONG_NAME();
        if (conName == null) conName = "";
        if (!conName.equals(""))
        {
          //rearrange it  long name and definition
          if (newBean == null)
          {
              if (!sLongName.equals("")) sLongName += " ";
              sLongName += conName;
              if (!sDef.equals("")) sDef += "_";  //add definition
              sDef += eCon.getPREFERRED_DEFINITION();
          }
          if (!sAbbName.equals("")) sAbbName += "_";
          if (conName.length() > 3) sAbbName += conName.substring(0, 4);  //truncate to four letters
          else sAbbName += conName; 
        }
     }
     //add the primary
     if (vRep != null && vRep.size() > 0)
     {
        EVS_Bean eCon = (EVS_Bean)vRep.elementAt(0);
        if (eCon == null) eCon = new EVS_Bean();
        String sPrimary = eCon.getLONG_NAME();
        if (sPrimary == null) sPrimary = "";
        if (!sPrimary.equals(""))
        {
          //rearrange it only long name and definition
          if (newBean == null)
          {
            if (!sLongName.equals("")) sLongName += " ";
            sLongName += sPrimary;
            if (!sDef.equals("")) sDef += "_";  //add definition
            sDef += eCon.getPREFERRED_DEFINITION();
          }
          if (!sAbbName.equals("")) sAbbName += "_";
          if (sPrimary.length() > 3) sAbbName += sPrimary.substring(0, 4);  //truncate to four letters
          else sAbbName += sPrimary;         
        }
     }
     //truncate to 30 characters 
     if (sAbbName != null && sAbbName.length()>30)
       sAbbName = sAbbName.substring(0,30);
    //add the abbr name to vd bean and page is selected
     pageVD.setAC_ABBR_PREF_NAME(sAbbName);
     //make abbr name name preferrd name if sys was selected
     if (selNameType != null && selNameType.equals("ABBR"))
       pageVD.setVD_PREFERRED_NAME(sAbbName); 
     if (newBean != null) //appending to the existing;
     {
        String sSelectName = newBean.getLONG_NAME();
        if (!sLongName.equals("")) sLongName += " ";
        sLongName += sSelectName;
        if (!sDef.equals("")) sDef += "_";  //add definition
        sDef += newBean.getPREFERRED_DEFINITION();
     }
     //store the long names, definition, and usr name in vd bean if searched 
     if (nameAct.equals("Search"))
     {
       pageVD.setVD_LONG_NAME(sLongName);
       pageVD.setVD_PREFERRED_DEFINITION(sDef);
     }
     return pageVD;
   }
  /**
    *
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    * 
    */
   public void doDECUseSelection(HttpServletRequest req, HttpServletResponse res, String nameAction)
   {    
    try
    {
     HttpSession session = req.getSession(); 
     String sSelRow = "";
     InsACService insAC = new InsACService(req, res, this);
     DEC_Bean m_DEC = (DEC_Bean)session.getAttribute("m_DEC");
     if (m_DEC == null) m_DEC = new DEC_Bean();
     m_setAC.setDECValueFromPage(req, res, m_DEC);
     Vector vObjectClass = (Vector)session.getAttribute("vObjectClass");
     if (vObjectClass == null) vObjectClass = new Vector();
     Vector vProperty = (Vector)session.getAttribute("vProperty");
     if (vProperty == null) vProperty = new Vector();
     
     Vector vAC = null;
     EVS_Bean m_OC = new EVS_Bean();
     EVS_Bean m_PC = new EVS_Bean();
     EVS_Bean m_OCQ = new EVS_Bean();
     EVS_Bean m_PCQ = new EVS_Bean();
     String sComp = (String)req.getParameter("sCompBlocks");
     if(sComp == null) sComp = "";
     if(sComp.equals("ObjectClass"))
     {
       sSelRow = (String)req.getParameter("selObjRow");
       vAC = (Vector)session.getAttribute("vACSearch");
       if (vAC == null) vAC = new Vector();
       if (sSelRow != null && !(sSelRow.equals("")))
       {
         String sObjRow = sSelRow.substring(2);
         Integer intObjRow = new Integer(sObjRow);
         int intObjRow2 = intObjRow.intValue();
         if(vAC.size()>intObjRow2-1)
           m_OC = (EVS_Bean)vAC.elementAt(intObjRow2);
         if(m_OC.getEVS_DATABASE().equals("caDSR"))
         {
           if(m_OC.getCONDR_IDSEQ() == null || m_OC.getCONDR_IDSEQ().equals(""))
             insAC.storeStatusMsg("This Object Class is not associated to a concept, so the data is suspect. \\n" +
             "Please choose another Object Class.");
           else
             splitIntoConcepts(sComp, m_OC, req, res, nameAction);
         }
         else
         {
           m_OC.setCON_AC_SUBMIT_ACTION("INS");
           m_OC.setCONTE_IDSEQ(m_DEC.getDEC_CONTE_IDSEQ());
           if(vObjectClass.size()<1)
             vObjectClass.addElement(m_OC);
           else
             vObjectClass.setElementAt(m_OC, 0);
           session.setAttribute("vObjectClass", vObjectClass);      
           session.setAttribute("newObjectClass", "true");
           m_DEC.setDEC_OCL_NAME_PRIMARY(m_OC.getLONG_NAME());
           m_DEC.setDEC_OC_CONCEPT_CODE(m_OC.getNCI_CC_VAL());
           m_DEC.setDEC_OC_EVS_CUI_ORIGEN(m_OC.getEVS_DATABASE());   
           m_DEC.setDEC_OCL_IDSEQ(m_OC.getIDSEQ());
           session.setAttribute("m_OC", m_OC);
           session.setAttribute("selObjRow", sSelRow);
           //add to name if appending
           if (nameAction.equals("appendName"))
             m_DEC = this.doGetDECNames(req, res, m_OC, "Search", m_DEC);
         }
       }
     }
     else if(sComp.equals("Property") || sComp.equals("PropertyClass"))
     {
       sSelRow = (String)req.getParameter("selPropRow");
     //  vAC = (Vector)session.getAttribute("vPropResult");
       vAC = (Vector)session.getAttribute("vACSearch");
       if (vAC == null) vAC = new Vector();
       if (sSelRow != null && !(sSelRow.equals("")))
       {
         String sPropRow = sSelRow.substring(2);
         Integer intPropRow = new Integer(sPropRow);
         int intPropRow2 = intPropRow.intValue();
         if(vAC.size()>intPropRow2-1)
           m_PC = (EVS_Bean)vAC.elementAt(intPropRow2);
         if(m_PC.getEVS_DATABASE().equals("caDSR"))
         {
           if(m_PC.getCONDR_IDSEQ() == null || m_PC.getCONDR_IDSEQ().equals(""))
             insAC.storeStatusMsg("This Property is not associated to a concept, so the data is suspect. " +
             "Please choose another Property.");
           else
           splitIntoConcepts(sComp, m_PC, req, res, nameAction);
         }
         else
         {
           m_PC.setCON_AC_SUBMIT_ACTION("INS");
           m_PC.setCONTE_IDSEQ(m_DEC.getDEC_CONTE_IDSEQ());
           if(vProperty.size()<1)
             vProperty.addElement(m_PC);
           else
             vProperty.setElementAt(m_PC, 0);
           session.setAttribute("vProperty", vProperty);
           session.setAttribute("newProperty", "true");
           m_DEC.setDEC_PROPL_NAME_PRIMARY(m_PC.getLONG_NAME());
           m_DEC.setDEC_PROP_CONCEPT_CODE(m_PC.getNCI_CC_VAL());
           m_DEC.setDEC_PROP_EVS_CUI_ORIGEN(m_PC.getEVS_DATABASE());
           m_DEC.setDEC_PROPL_IDSEQ(m_OC.getIDSEQ());
       //    session.setAttribute("vPropResult", null);
           session.setAttribute("m_PC", m_PC);
           session.setAttribute("selPropRow", sSelRow);
           //add to name if appending
           if (nameAction.equals("appendName"))
             m_DEC = this.doGetDECNames(req, res, m_PC, "Search", m_DEC);
         }
       }
     }
     else if(sComp.equals("ObjectQualifier"))
     {
       // Do this to reserve zero position in vector for primary concept
       if(vObjectClass.size()<1)
       {
         EVS_Bean OCBean = new EVS_Bean();
         vObjectClass.addElement(OCBean);
       }
       sSelRow = (String)req.getParameter("selObjQRow");
       vAC = (Vector)session.getAttribute("vACSearch");
       if (vAC == null) vAC = new Vector();
       if (sSelRow != null && !(sSelRow.equals("")))
       {
         String sObjRow = sSelRow.substring(2);
         Integer intObjRow = new Integer(sObjRow);
         int intObjRow2 = intObjRow.intValue();
         m_OCQ = (EVS_Bean)vAC.elementAt(intObjRow2);
         if(m_OCQ.getEVS_DATABASE().equals("caDSR"))
           splitIntoConcepts(sComp, m_OCQ, req, res, nameAction);
         else
         {
           m_OCQ.setCON_AC_SUBMIT_ACTION("INS");
           m_OCQ.setCONTE_IDSEQ(m_DEC.getDEC_CONTE_IDSEQ());
           vObjectClass.addElement(m_OCQ);
           session.setAttribute("vObjectClass", vObjectClass);
           session.setAttribute("newObjectClass", "true");
           Vector vOCQualifierNames = m_DEC.getDEC_OC_QUALIFIER_NAMES();
           if (vOCQualifierNames == null) vOCQualifierNames = new Vector();
           vOCQualifierNames.addElement(m_OCQ.getLONG_NAME());
           Vector vOCQualifierCodes = m_DEC.getDEC_OC_QUALIFIER_CODES();
           if (vOCQualifierCodes == null) vOCQualifierCodes = new Vector();
           vOCQualifierCodes.addElement(m_OCQ.getNCI_CC_VAL());
           Vector vOCQualifierDB = m_DEC.getDEC_OC_QUALIFIER_DB();
           if (vOCQualifierDB == null) vOCQualifierDB = new Vector();   
           vOCQualifierDB.addElement(m_OCQ.getEVS_DATABASE());
           m_DEC.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
           m_DEC.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
           m_DEC.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
            if(vOCQualifierNames.size()>0)
               m_DEC.setDEC_OBJ_CLASS_QUALIFIER((String)vOCQualifierNames.elementAt(0));
           session.setAttribute("m_OCQ", m_OCQ);
           session.setAttribute("selObjQRow", sSelRow);
           //add to name if appending
           if (nameAction.equals("appendName"))
             m_DEC = this.doGetDECNames(req, res, m_OCQ, "Search", m_DEC);
         }
       }
     }
     else if(sComp.equals("PropertyQualifier"))
     {
       // Do this to reserve zero position in vector for primary concept
       if(vProperty.size()<1)
       {
         EVS_Bean PCBean = new EVS_Bean();
         vProperty.addElement(PCBean);
       }
       sSelRow = (String)req.getParameter("selPropQRow");
       vAC = (Vector)session.getAttribute("vACSearch");
       if (vAC == null) vAC = new Vector();
       if (sSelRow != null && !(sSelRow.equals("")))
       {
         String sPropRow = sSelRow.substring(2);
         Integer intPropRow = new Integer(sPropRow);
         int intPropRow2 = intPropRow.intValue();
         m_PCQ = (EVS_Bean)vAC.elementAt(intPropRow2);
         if(m_PCQ.getEVS_DATABASE().equals("caDSR"))
           splitIntoConcepts(sComp, m_PCQ, req, res, nameAction);
         else
         {
           m_PCQ.setCON_AC_SUBMIT_ACTION("INS");
           m_PCQ.setCONTE_IDSEQ(m_DEC.getDEC_CONTE_IDSEQ());
           vProperty.addElement(m_PCQ);
           session.setAttribute("vProperty", vProperty); 
           session.setAttribute("newProperty", "true");
           Vector vPropQualifierNames = m_DEC.getDEC_PROP_QUALIFIER_NAMES();
           if (vPropQualifierNames == null) vPropQualifierNames = new Vector();
           vPropQualifierNames.addElement(m_PCQ.getLONG_NAME());
           Vector vPropQualifierCodes = m_DEC.getDEC_PROP_QUALIFIER_CODES();
           if (vPropQualifierCodes == null) vPropQualifierCodes = new Vector();
           vPropQualifierCodes.addElement(m_PCQ.getNCI_CC_VAL());
           Vector vPropQualifierDB = m_DEC.getDEC_PROP_QUALIFIER_DB();
           if (vPropQualifierDB == null) vPropQualifierDB = new Vector(); 
           vPropQualifierDB.addElement(m_PCQ.getEVS_DATABASE());
           m_DEC.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
           m_DEC.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
           m_DEC.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
           if(vPropQualifierNames.size()>0)
               m_DEC.setDEC_PROPERTY_QUALIFIER((String)vPropQualifierNames.elementAt(0));
           session.setAttribute("m_PCQ", m_PCQ);
           session.setAttribute("selObjQRow", sSelRow);
           //add to name if appending
           if (nameAction.equals("appendName"))
             m_DEC = this.doGetDECNames(req, res, m_PCQ, "Search", m_DEC);
         }
       }
     }
     //rebuild new name if not appending
     if (nameAction.equals("newName"))
       m_DEC = this.doGetDECNames(req, res, null, "Search", m_DEC);
     else if (nameAction.equals("blockName"))
       m_DEC = this.doGetDECNames(req, res, null, "blockName", m_DEC);
     session.setAttribute("m_DEC", m_DEC); 
     }
     catch (Exception e)
     {
       this.logger.fatal("ERROR - doDECUseSelection : " + e.toString());
     }
   } // end of doDECUseSelection
 
 /**
    *
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    * 
    */
   public void doVDUseSelection(HttpServletRequest req, HttpServletResponse res, String nameAction)
   {  
     try
     {
       HttpSession session = req.getSession(); 
       String sSelRow = "";
       InsACService insAC = new InsACService(req, res, this);
       VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");
       if (m_VD == null) m_VD = new VD_Bean();
       m_setAC.setVDValueFromPage(req, res, m_VD);
 
       Vector vRepTerm = (Vector)session.getAttribute("vRepTerm");
       if (vRepTerm == null)  vRepTerm = new Vector();
 
       Vector vAC = new Vector();;
       EVS_Bean m_REP = new EVS_Bean();
       EVS_Bean m_REPQ = new EVS_Bean();
       String sComp = (String)req.getParameter("sCompBlocks"); 
 
       if(sComp.equals("RepTerm"))
       {
         sSelRow = (String)req.getParameter("selRepRow");
       //  vAC = (Vector)session.getAttribute("vRepResult");
         vAC = (Vector)session.getAttribute("vACSearch");
         if (vAC == null) vAC = new Vector();
         if (sSelRow != null && !(sSelRow.equals("")))
         {
           String sObjRow = sSelRow.substring(2);
           Integer intObjRow = new Integer(sObjRow);
           int intObjRow2 = intObjRow.intValue();
           if(vAC.size()>intObjRow2-1)
             m_REP = (EVS_Bean)vAC.elementAt(intObjRow2);
 
           if(m_REP.getEVS_DATABASE().equals("caDSR"))
           {
             if(m_REP.getCONDR_IDSEQ() == null || m_REP.getCONDR_IDSEQ().equals(""))
             {
               insAC.storeStatusMsg("This Rep Term is not associated to a concept, so the data is suspect. \\n" +
               "Please choose another Rep Term.");
             }
             else
               splitIntoConceptsVD(sComp, m_REP, req, res, nameAction);
           }
           else
           {
             m_REP.setCON_AC_SUBMIT_ACTION("INS");
             m_REP.setCONTE_IDSEQ(m_VD.getVD_CONTE_IDSEQ());
             if(vRepTerm.size()<1)
               vRepTerm.addElement(m_REP);
             else
               vRepTerm.setElementAt(m_REP, 0);
             session.setAttribute("vRepTerm", vRepTerm);
             session.setAttribute("newRepTerm", "true");
 
             m_VD.setVD_REP_NAME_PRIMARY(m_REP.getLONG_NAME());
             m_VD.setVD_REP_CONCEPT_CODE(m_REP.getNCI_CC_VAL());
             m_VD.setVD_REP_EVS_CUI_ORIGEN(m_REP.getEVS_DATABASE());   
             m_VD.setVD_REP_IDSEQ(m_REP.getIDSEQ());
 
             session.setAttribute("m_REP", m_REP);
             session.setAttribute("selRepRow", sSelRow);
             //add to name if appending
             if (nameAction.equals("appendName"))
               m_VD = this.doGetVDNames(req, res, m_REP, "Search", m_VD);
           }
         }
       }
       else if(sComp.equals("RepQualifier"))
       { 
            // Do this to reserve zero position in vector for primary concept
         if(vRepTerm.size()<1)
         {
           EVS_Bean OCBean = new EVS_Bean();
           vRepTerm.addElement(OCBean);
         }
         sSelRow = (String)req.getParameter("selRepQRow");
         vAC = (Vector)session.getAttribute("vACSearch");
         if (vAC == null) vAC = new Vector();
         if (sSelRow != null && !(sSelRow.equals("")))
         {
           String sObjRow = sSelRow.substring(2);
           Integer intObjRow = new Integer(sObjRow);
           int intObjRow2 = intObjRow.intValue();
           m_REPQ = (EVS_Bean)vAC.elementAt(intObjRow2);
           if(m_REPQ.getEVS_DATABASE().equals("caDSR"))
             splitIntoConceptsVD(sComp, m_REPQ, req, res, nameAction);
           else
           {
             m_REPQ.setCON_AC_SUBMIT_ACTION("INS");
             m_REPQ.setCONTE_IDSEQ(m_VD.getVD_CONTE_IDSEQ());
 
             vRepTerm.addElement(m_REPQ);
             session.setAttribute("vRepTerm", vRepTerm);
             session.setAttribute("newRepTerm", "true");
 
             Vector vRepQualifierNames = m_VD.getVD_REP_QUALIFIER_NAMES();
             if (vRepQualifierNames == null) vRepQualifierNames = new Vector();
             vRepQualifierNames.addElement(m_REPQ.getLONG_NAME());
             Vector vRepQualifierCodes = m_VD.getVD_REP_QUALIFIER_CODES();
             if (vRepQualifierCodes == null) vRepQualifierCodes = new Vector();
             vRepQualifierCodes.addElement(m_REPQ.getNCI_CC_VAL());
             Vector vRepQualifierDB = m_VD.getVD_REP_QUALIFIER_DB();
             if (vRepQualifierDB == null) vRepQualifierDB = new Vector();
             vRepQualifierDB.addElement(m_REPQ.getEVS_DATABASE());  
             m_VD.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
             m_VD.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
             m_VD.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
             if(vRepQualifierNames.size()>0)
             {
                 m_VD.setVD_REP_QUAL((String)vRepQualifierNames.elementAt(0));
             }
             session.setAttribute("vRepQResult", null);
             session.setAttribute("m_REPQ", m_REPQ);
             session.setAttribute("selRepQRow", sSelRow);
             //add to name if appending
             if (nameAction.equals("appendName"))
               m_VD = this.doGetVDNames(req, res, m_REPQ, "Search", m_VD);
           }
         }
       }
       else
       {
         EVS_Bean eBean = this.getEVSSelRow(req);
         if (eBean != null)
         {
           if (sComp.equals("VDObjectClass"))
           {
             m_VD.setVD_OBJ_CLASS(eBean.getLONG_NAME());
             session.setAttribute("m_OC", eBean);
           }
           else if (sComp.equals("VDPropertyClass"))
           {
             m_VD.setVD_PROP_CLASS(eBean.getLONG_NAME());
             session.setAttribute("m_PC", eBean);
           }
           if (nameAction.equals("appendName"))
             m_VD = this.doGetVDNames(req, res, eBean, "Search", m_VD);
         }
       }
       //rebuild new name if not appending
       if (nameAction.equals("newName"))
         m_VD = this.doGetVDNames(req, res, null, "Search", m_VD);
       else if (nameAction.equals("blockName"))
         m_VD = this.doGetVDNames(req, res, null, "blockName", m_VD);
       session.setAttribute("m_VD", m_VD);
     }
     catch (Exception e)
     {
       this.logger.fatal("ERROR - doVDUseSelection : " + e.toString());
     }
   } // end of doVDUseSelection
 
 
 /**
    *
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    * 
    */
   public void doRemoveBuildingBlocks(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {            
     HttpSession session = req.getSession(); 
     String sSelRow = "";
     DEC_Bean m_DEC = (DEC_Bean)session.getAttribute("m_DEC");
     if (m_DEC == null) m_DEC = new DEC_Bean();
     Vector vObjectClass = (Vector)session.getAttribute("vObjectClass");
     if (vObjectClass == null) vObjectClass = new Vector();
     int iOCSize = vObjectClass.size();
     Vector vProperty = (Vector)session.getAttribute("vProperty");
     if (vProperty == null) vProperty = new Vector();
     int iPropSize = vProperty.size();
     String sComp = (String)req.getParameter("sCompBlocks");
     if(sComp == null) sComp = "";
 
     if(sComp.equals("ObjectClass"))
     {
         EVS_Bean m_OC = new EVS_Bean();
         vObjectClass.setElementAt(m_OC, 0);
         session.setAttribute("vObjectClass", vObjectClass);
         m_DEC.setDEC_OCL_NAME_PRIMARY("");
         m_DEC.setDEC_OC_CONCEPT_CODE("");
         m_DEC.setDEC_OC_EVS_CUI_ORIGEN("");
         m_DEC.setDEC_OCL_IDSEQ("");
         session.setAttribute("RemoveOCBlock", "true");
         session.setAttribute("newObjectClass", "true");
     }
     else if(sComp.equals("Property") || sComp.equals("PropertyClass"))
     {
         EVS_Bean m_PC = new EVS_Bean();
         vProperty.setElementAt(m_PC, 0);
         session.setAttribute("vProperty", vProperty);
         m_DEC.setDEC_PROPL_NAME_PRIMARY("");
         m_DEC.setDEC_PROP_CONCEPT_CODE("");
         m_DEC.setDEC_PROP_EVS_CUI_ORIGEN("");
         m_DEC.setDEC_OCL_IDSEQ("");
         session.setAttribute("RemovePropBlock", "true");
         session.setAttribute("newProperty", "true");
     }
     else if(sComp.equals("ObjectQualifier"))
     {
       sSelRow = (String)req.getParameter("selObjQRow");
       if (sSelRow != null && !(sSelRow.equals("")))
       {
         Integer intObjRow = new Integer(sSelRow);
         int intObjRow2 = intObjRow.intValue();
         // add 1 because 0 element is OC, not a qualifier
         int int1 = intObjRow2 + 1;
         if(vObjectClass.size() > (int1))
         {
           vObjectClass.removeElementAt(int1); 
           session.setAttribute("vObjectClass", vObjectClass);
         }   
         m_DEC.setDEC_OBJ_CLASS_QUALIFIER("");
         Vector vOCQualifierNames = m_DEC.getDEC_OC_QUALIFIER_NAMES();
         if (vOCQualifierNames == null) vOCQualifierNames = new Vector();
         if(vOCQualifierNames.size() > intObjRow2)
           vOCQualifierNames.removeElementAt(intObjRow2);
         Vector vOCQualifierCodes = m_DEC.getDEC_OC_QUALIFIER_CODES();
         if (vOCQualifierCodes == null) vOCQualifierCodes = new Vector();
         if(vOCQualifierCodes.size() > intObjRow2)
           vOCQualifierCodes.removeElementAt(intObjRow2);
         Vector vOCQualifierDB = m_DEC.getDEC_OC_QUALIFIER_DB();
         if (vOCQualifierDB == null) vOCQualifierDB = new Vector();
         if(vOCQualifierDB.size() > intObjRow2)
           vOCQualifierDB.removeElementAt(intObjRow2);
         m_DEC.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
         m_DEC.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
         m_DEC.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
         session.setAttribute("RemoveOCBlock", "true");
         session.setAttribute("newObjectClass", "true");
       }
     }
     else if(sComp.equals("PropertyQualifier"))
     {
       sSelRow = (String)req.getParameter("selPropQRow");
       if (sSelRow != null && !(sSelRow.equals("")))
       {
         Integer intPropRow = new Integer(sSelRow);
         int intPropRow2 = intPropRow.intValue();
         // add 1 because 0 element is OC, not a qualifier
         int int1 = intPropRow2 + 1;
         //invert because the list on ui is i9nverse to vector
         if(vProperty.size() > (int1))
         {
           vProperty.removeElementAt(int1);
           session.setAttribute("vProperty", vProperty);
         }
         m_DEC.setDEC_PROPERTY_QUALIFIER("");
         Vector vPropQualifierNames = m_DEC.getDEC_PROP_QUALIFIER_NAMES();
         if (vPropQualifierNames == null) vPropQualifierNames = new Vector();
         if(vPropQualifierNames.size() > intPropRow2)
           vPropQualifierNames.removeElementAt(intPropRow2);
         Vector vPropQualifierCodes = m_DEC.getDEC_PROP_QUALIFIER_CODES();
         if (vPropQualifierCodes == null) vPropQualifierCodes = new Vector();
         if(vPropQualifierCodes.size() > intPropRow2)
           vPropQualifierCodes.removeElementAt(intPropRow2);
         Vector vPropQualifierDB = m_DEC.getDEC_PROP_QUALIFIER_DB();
         if (vPropQualifierDB == null) vPropQualifierDB = new Vector();
         if(vPropQualifierDB.size() > intPropRow2)
           vPropQualifierDB.removeElementAt(intPropRow2);
         m_DEC.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
         m_DEC.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
         m_DEC.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
         session.setAttribute("RemovePropBlock", "true");
         session.setAttribute("newObjectClass", "true");
         }
       }
     m_setAC.setDECValueFromPage(req, res, m_DEC);
     session.setAttribute("m_DEC", m_DEC);  
   } // end of doRemoveQualifier
 
 /**
    *
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    * 
    */
   public void doRemoveBuildingBlocksVD(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {            
     HttpSession session = req.getSession(); 
     String sSelRow = "";
     VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");
     if (m_VD == null) m_VD = new VD_Bean();
     Vector vRepTerm = (Vector)session.getAttribute("vRepTerm");
     if (vRepTerm == null) vRepTerm = new Vector();
     String sComp = (String)req.getParameter("sCompBlocks");
     if(sComp == null) sComp = "";
 
     if(sComp.equals("RepTerm"))
     {
         EVS_Bean m_REP = new EVS_Bean();
         vRepTerm.setElementAt(m_REP, 0);
         session.setAttribute("vRepTerm", vRepTerm);
         m_VD.setVD_REP_NAME_PRIMARY("");
         m_VD.setVD_REP_CONCEPT_CODE("");
         m_VD.setVD_REP_EVS_CUI_ORIGEN("");
         m_VD.setVD_REP_IDSEQ("");
         session.setAttribute("RemoveRepBlock", "true");
         session.setAttribute("newRepTerm", "true");
     }
     else if(sComp.equals("RepQualifier"))
     {
       sSelRow = (String)req.getParameter("selRepQRow");
       if (sSelRow != null && !(sSelRow.equals("")))
       {
         Integer intObjRow = new Integer(sSelRow);
         int intObjRow2 = intObjRow.intValue(); 
         if(vRepTerm.size() > (intObjRow2 + 1))
         {
           vRepTerm.removeElementAt(intObjRow2 + 1); //add 1 so zero element not removed
           session.setAttribute("vRepTerm", vRepTerm);
         }  
         m_VD.setVD_REP_QUAL("");
         Vector vRepQualifierNames = m_VD.getVD_REP_QUALIFIER_NAMES();
         if (vRepQualifierNames == null) vRepQualifierNames = new Vector();
         if(vRepQualifierNames.size() > intObjRow2)
           vRepQualifierNames.removeElementAt(intObjRow2);
         Vector vRepQualifierCodes = m_VD.getVD_REP_QUALIFIER_CODES();
         if (vRepQualifierCodes == null) vRepQualifierCodes = new Vector();
         if(vRepQualifierCodes.size() > intObjRow2)
           vRepQualifierCodes.removeElementAt(intObjRow2);
         Vector vRepQualifierDB = m_VD.getVD_REP_QUALIFIER_DB();
         if (vRepQualifierDB == null) vRepQualifierDB = new Vector();
         if(vRepQualifierDB.size() > intObjRow2)
           vRepQualifierDB.removeElementAt(intObjRow2);
         m_VD.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
         m_VD.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
         m_VD.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
         session.setAttribute("RemoveRepBlock", "true");
         session.setAttribute("newRepTerm", "true");
       }
     }
     else if (sComp.equals("VDObjectClass"))
     {
         m_VD.setVD_OBJ_CLASS("");
         session.setAttribute("m_OC", new EVS_Bean());      
     }
     else if (sComp.equals("VDPropertyClass"))
     {
         m_VD.setVD_PROP_CLASS("");
         session.setAttribute("m_PC", new EVS_Bean());      
     }
 
     m_setAC.setVDValueFromPage(req, res, m_VD);
     session.setAttribute("m_VD", m_VD);  
 
   } // end of doRemoveQualifier
 
   /**
    * to store the selected value meanings from EVS into pv bean to submit later.
    *
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    * 
    */
    private void doSelectVMinVD(HttpServletRequest req, HttpServletResponse res, String sPVAction) 
    throws Exception
    {
       HttpSession session = req.getSession();
       session.setAttribute("PVAction", sPVAction);
       VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setVDValueFromPage(req, res, m_VD);
       session.setAttribute("m_VD", m_VD);
       //get the existing pvs from the session
       Vector vVDPVList = (Vector)session.getAttribute("VDPVList");
       if (vVDPVList == null) vVDPVList = new Vector();
       //get the VMs selected from EVS from the page.
       Vector vEVSList = this.getEVSSelRowVector(req, new Vector());
       if (vEVSList != null)
       {
         //get the parent concept which is same for all the selected values
         String sSelectedParentName = (String)session.getAttribute("SelectedParentName");
         String sSelectedParentCC = (String)session.getAttribute("SelectedParentCC");
         String sSelectedParentDB = (String)session.getAttribute("SelectedParentDB");
         String sSelectedParentMetaSource = (String)session.getAttribute("SelectedParentMetaSource");
         //get the parent concept
         EVS_Bean parConcept = new EVS_Bean();
         if (sSelectedParentName != null && !sSelectedParentName.equals(""))
         {
           parConcept.setLONG_NAME(sSelectedParentName);
           parConcept.setNCI_CC_VAL(sSelectedParentCC);
           parConcept.setEVS_DATABASE(sSelectedParentDB);
           parConcept.setEVS_CONCEPT_SOURCE(sSelectedParentMetaSource);
         }
         String selVDPVs = "";
         for (int i=0; i<vEVSList.size(); i++)
         {
           EVS_Bean eBean = (EVS_Bean)vEVSList.elementAt(i);
           String sValue = eBean.getLONG_NAME();
           String sMean = eBean.getLONG_NAME();
           //add the level to the value if parent exists to update the value
           if (sSelectedParentName != null && !sSelectedParentName.equals(""))
           {
             Integer iLevel = new Integer(eBean.getLEVEL());
             sValue = sValue + " [" + iLevel.toString() + "]"; 
           }
 
           boolean isExist = false;
           boolean wasDeleted = false;
           int updRow = -1;
           PV_Bean pvBean = new PV_Bean();
           for (int j=0; j<vVDPVList.size(); j++)  //check if the concept already exists.
           {
             pvBean = (PV_Bean)vVDPVList.elementAt(j);
             //exist if already exists 
             if (pvBean.getPV_VALUE().equals(sValue) && pvBean.getPV_SHORT_MEANING().equals(sMean))
             {
               //re-adding the deleted item if exists in the vector but marked as deleted
               if (pvBean.getVP_SUBMIT_ACTION() == null) pvBean.setVP_SUBMIT_ACTION("INS");
               if (pvBean.getVP_SUBMIT_ACTION().equals("DEL"))
               {
                 //set to update if idseq is non evs and is from cadsr 
                 if (pvBean.getPV_PV_IDSEQ() != null && !pvBean.getPV_PV_IDSEQ().equals("EVS_" + sValue))
                   pvBean.setVP_SUBMIT_ACTION("UPD");
                 else
                   pvBean.setVP_SUBMIT_ACTION("INS");   //evs term
                 //mark as deleted
                 wasDeleted = true;
                 updRow = j;  //need this to update the vector                
               }
               else  //keep track of the duplicate items and do not add to the bean
               {
                 selVDPVs = selVDPVs + "Value: " + sValue + " and Meaning: " + sMean + "\\n";
                 isExist = true;
               }
               break;
             }
           }
           //add to the bean if not exists
           if (isExist == false)
           {
             if (!wasDeleted)  //do not update these if  
             {
               //store concept name as value and vm in the pv bean
               pvBean = new PV_Bean();
               pvBean.setPV_PV_IDSEQ("EVS_" + sValue);   //store id as EVS
               pvBean.setPV_VALUE(sValue);
               pvBean.setPV_SHORT_MEANING(sMean);
               pvBean.setVP_SUBMIT_ACTION("INS");
             }
             //allow to update the definition if different description for evs selected items
             if (pvBean.getPV_PV_IDSEQ() == null || pvBean.getPV_PV_IDSEQ().equals("EVS_" + sValue))
               pvBean.setPV_MEANING_DESCRIPTION(eBean.getPREFERRED_DEFINITION());
             //these are for vd-pvs             
             SimpleDateFormat formatter = new SimpleDateFormat ("MM/dd/yyyy");
             if (pvBean.getVP_SUBMIT_ACTION().equals("INS"))
               pvBean.setPV_BEGIN_DATE(formatter.format(new java.util.Date()));
             
             pvBean.setVM_CONCEPT(eBean);   //add evs bean to pv bean 
             pvBean.setPARENT_CONCEPT(parConcept);
             if (wasDeleted)
               vVDPVList.setElementAt(pvBean, updRow);   //udpate the vector
             else
               vVDPVList.addElement(pvBean);    //store bean in vector
           }
         }
         session.setAttribute("VDPVList", vVDPVList);        
         //alert if value meaning alredy exists for pv on the page
         if (selVDPVs != null && !selVDPVs.equals(""))
         {
           String stMsg = "The following Value and Meaning already exists in the Value Domain. \\n";
           session.setAttribute("statusMessage", stMsg + selVDPVs);
         }
       }
    }
    
    /**
   * The doValidateVD method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'doCreateVDActions', 'doSubmitVD' method.
   * Calls 'setAC.setVDValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesVD' to validate the data.
   * Stores 'm_VD' bean in session.
   * Forwards the page 'ValidateVDPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doValidateVDBlockEdit(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
       HttpSession session = req.getSession();
       VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
       session.setAttribute("VDPageAction", "validate");      //store the page action in attribute
       m_setAC.setVDValueFromPage(req, res, m_VD);
       session.setAttribute("m_VD", m_VD);
       m_setAC.setValidateBlockEdit(req, res, "ValueDomain");
       session.setAttribute("VDEditAction", "VDBlockEdit");
       ForwardJSP(req, res, "/ValidateVDPage.jsp");
   } // end of doValidateVD
 
   /**
   * The doValidatePV method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'service' method where reqType = 'newPV' and 'doCreatePVActions','doSubmitPV' methods.
   * Calls 'setAC.setPVValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesPV' to validate the data.
   * Stores 'm_PV' bean in session.
   * Forwards the page 'ValidatePVPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doValidatePV(HttpServletRequest req,  HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       PV_Bean m_PV = (PV_Bean)session.getAttribute("m_PV");
       if (m_PV == null) m_PV = new PV_Bean();
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setPVValueFromPage(req, res, m_PV);
       m_setAC.setValidatePageValuesPV(req, res, m_PV, getAC);
       session.setAttribute("m_PV", m_PV);
 
       String sOrigin = (String)session.getAttribute("originAction");
       if (sOrigin.equals("CreateInSearch"))
          ForwardJSP(req, res, "/ValidatePVSearchPage.jsp");
       else
          ForwardJSP(req, res, "/ValidatePVPage.jsp");
   } // end of doValidatePV
 
   /**
   * The doValidateVM method gets the values from page the user filled out,
   * validates the input, then forwards results to the Validate page
   * Called from 'service' method where reqType = 'newVM'.
   * Calls 'setAC.setVMValueFromPage' to set the data from the page to the bean.
   * Calls 'setAC.setValidatePageValuesVM' to validate the data.
   * Stores 'm_VM' bean in session.
   * Stores 'm_PV' bean in session.
   * Forwards the page 'ValidateVMPage.jsp' with validation vector to display.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doValidateVM(HttpServletRequest req,  HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
 
       VM_Bean m_VM = (VM_Bean)session.getAttribute("m_VM");
       GetACService getAC = new GetACService(req, res, this);
       m_setAC.setVMValueFromPage(req, res, m_VM);
       m_setAC.setValidatePageValuesVM(req, res, m_VM, getAC);
       session.setAttribute("m_VM", m_VM);
 
       String sOrigin = (String)session.getAttribute("originAction");
       if (sOrigin.equals("CreateInSearch"))
          ForwardJSP(req, res, "/ValidateVMSearchPage.jsp");
       else
          ForwardJSP(req, res, "/ValidateVMPage.jsp");
   } // end of doValidateVM
 
   /**
   * The doInsertDE method to insert or update record in the database.
   * Called from 'service' method where reqType is 'validateDEFromForm'.
   * Retrieves the session bean m_DE.
   * if the action is reEditDE forwards the page back to Edit or create pages.
   * Otherwise, calls 'insAC.setDE' to submit the record to the database
   * null value return would store the statusMessage as successful in session and
   * forwards the page 'SearchResultsPage.jsp' for create new from template, new version, or Edit actions,
   * and 'CreateDEPage.jsp' for create new DE action.
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page 'CreateDEPage.jsp' for create and 'EditDEPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertDE(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       //make sure that status message is empty
       session.setAttribute("statusMessage", "");
       Vector vStat = new Vector();
       session.setAttribute("vStatMsg", vStat);
       String sAction = (String)req.getParameter("ValidateDEPageAction");
       String sDEEditAction = (String)session.getAttribute("DEEditAction");
       if (sDEEditAction == null) sDEEditAction = "";
       String sOriginAction = (String)session.getAttribute("originAction");
       if (sOriginAction == null) sOriginAction = "";
       String sDEAction = (String)session.getAttribute("DEAction");
       if (sDEAction == null) sDEAction = "";
       String sMenu = (String)session.getAttribute("MenuAction");
       if (sAction == null)
         sAction = "submitting"; //covers direct submits without going to Validate page
 
       if (sAction != null)
       {
          //go back from validation page according editing or creating action
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
            //update the data for block editing
            else if (sDEEditAction.equals("DEBlockEdit"))
              doUpdateDEActionBE(req, res);
           //insert a new one if create new, template or version
            else
              doInsertDEfromMenuAction(req, res);
         }
       }
   } // end of doInsertDE
 
   /**
   * update record in the database and display the result.
   * Called from 'doInsertDE' method when the aciton is editing.
   * Retrieves the session bean m_DE.
   * calls 'insAC.setDE' to update the database.
   * calls 'serAC.refreshData' to get the refreshed search result
   * forwards the page back to search page with refreshed list after updating.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'EditDEPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doUpdateDEAction(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       InsACService insAC = new InsACService(req, res, this);  //
       DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
       DE_Bean oldDEBean = (DE_Bean)session.getAttribute("oldDEBean");
 
       //udpate the status message with DE name and ID
       insAC.storeStatusMsg("Data Element Name : " + DEBean.getDE_LONG_NAME());
       insAC.storeStatusMsg("Public ID : " + DEBean.getDE_MIN_CDE_ID());
       
       //call stored procedure to update attributes
       String ret = insAC.setDE("UPD", DEBean, "Edit", oldDEBean);
       //forwards to search page if successful
       if ((ret == null) || ret.equals(""))
       {
          ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), "");   // set DEComp rules and relations
          GetACSearch serAC = new GetACSearch(req, res, this);
          String sMenuAction = (String)session.getAttribute("MenuAction");
          //forward to search page with the refreshed question list
          if (sMenuAction.equals("Questions"))
          {
             session.setAttribute("searchAC", "Questions");
             Quest_Bean questBean = (Quest_Bean)session.getAttribute("m_Quest");
             if (questBean != null)
             {
                questBean.setQUEST_DEFINITION(DEBean.getDE_PREFERRED_DEFINITION());
                questBean.setSUBMITTED_LONG_NAME(DEBean.getDOC_TEXT_LONG_NAME());
                questBean.setDE_IDSEQ(DEBean.getDE_DE_IDSEQ());
                questBean.setDE_LONG_NAME(DEBean.getDE_LONG_NAME());
                questBean.setVD_IDSEQ(DEBean.getDE_VD_IDSEQ());
                questBean.setVD_LONG_NAME(DEBean.getDE_VD_NAME());
                questBean.setSTATUS_INDICATOR("Edit");
                serAC.refreshData(req, res, null, null, null, questBean, "Edit", "");
                //reset the appened attributes to remove all the checking of the row
                Vector vCheck = new Vector();
                session.setAttribute("CheckList", vCheck);
                session.setAttribute("AppendAction", "Not Appended");
 
                //call the method to update Quest contents table
                ret = insAC.setQuestContent(questBean, "", "");
                session.setAttribute("m_Quest", questBean);
             }
          }
          else
          {
             DEBean.setDE_ALIAS_NAME(DEBean.getDE_PREFERRED_NAME());
             DEBean.setDE_TYPE_NAME("PRIMARY");
             String oldID = oldDEBean.getDE_DE_IDSEQ();
             serAC.refreshData(req, res, DEBean, null, null, null, "Edit", oldID);
          }
          ForwardJSP(req, res, "/SearchResultsPage.jsp");   //forward to search page
       }
       //back to edit page if not successful
       else
       {
           //session.setAttribute("statusMessage", ret + " - Unable to update Data Element successfully.");
           session.setAttribute("sCDEAction", "nothing");
           ForwardJSP(req, res, "/EditDEPage.jsp");   //send it back to edit page
       }
    }
 
   /**
   * creates new record in the database and display the result.
   * Called from 'doInsertDE' method when the aciton is create new DEC from Menu.
   * Retrieves the session bean m_DE.
   * calls 'insAC.setDE' to insert in the database.
   * calls 'serAC.refreshData' to get the refreshed search result for template/version
   * forwards the page back to create DEC page if new DE or back to search page
   * if template or version after successful insert.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'createDEPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertDEfromMenuAction(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       InsACService insAC = new InsACService(req, res, this);
       GetACSearch serAC = new GetACSearch(req, res, this);
       String ret = "";
       boolean isUpdateSuccess = true;
       String sMenuAction = (String)session.getAttribute("MenuAction");
       String sOriginAction = (String)session.getAttribute("originAction");
 
       if(sOriginAction.equals("CreateNewDEFComp"))
       {
           doInsertDEComp(req, res);
           return;
       }   
 
       DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
       DE_Bean oldDEBean = (DE_Bean)session.getAttribute("oldDEBean");
       //DE version
       if (sMenuAction.equals("NewDEVersion"))
       {
         //udpate the status message with DE name and ID only for version and updates
         insAC.storeStatusMsg("Data Element Name : " + DEBean.getDE_LONG_NAME());
         insAC.storeStatusMsg("Public ID : " + DEBean.getDE_MIN_CDE_ID());        
         //call stored procedure to version and update attributes
          ret = insAC.setAC_VERSION(DEBean, null, null, "DataElement");
          if (ret == null || ret.equals(""))
          {
             //update other attributes
             ret = insAC.setDE("UPD", DEBean, "Version", oldDEBean);
             if (ret != null && !ret.equals(""))
             {
                //session.setAttribute("statusMessage", ret + " - Created new version but unable to update attributes successfully.");
                //add newly created row to searchresults and send it to edit page for update
                isUpdateSuccess = false;
                String oldID = oldDEBean.getDE_DE_IDSEQ();
                DEBean = DEBean.cloneDE_Bean(oldDEBean, "Versioned");
                serAC.refreshData(req, res, DEBean, null, null, null, "Version", oldID);
             }
          }
          else
             insAC.storeStatusMsg("\t " + ret + " - Unable to create new version successfully.");
       }
       else
       {    //insert a new one
          ret = insAC.setDE("INS", DEBean, "New", oldDEBean);
       }
 
       if ((ret == null) || ret.equals(""))
       {
          session.setAttribute("sCDEAction", "nothing");
          session.setAttribute("DECLongID", "nothing");
          session.setAttribute("VDLongID", "nothing");
 
          //forwards to search page with the refreshed list after success if template or version
          if ((sMenuAction.equals("NewDETemplate")) || (sMenuAction.equals("NewDEVersion")))
          {
             ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), "INS");   // set DEComp rules and relationships
             session.setAttribute("searchAC", "DataElement");
 
             String oldID = oldDEBean.getDE_DE_IDSEQ();
             if (sMenuAction.equals("NewDETemplate"))
                serAC.refreshData(req, res, DEBean, null, null, null, "Template", oldID);
             else if (sMenuAction.equals("NewDEVersion"))
                serAC.refreshData(req, res, DEBean, null, null, null, "Version", oldID);
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
          //forward to search page with the refreshed question list
          else if (sMenuAction.equals("Questions"))
          {
             ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), "INS");   // set DEComp rules and relationships
             doInitDDEInfo(req, res);
             session.setAttribute("searchAC", "Questions");
             Quest_Bean questBean = (Quest_Bean)session.getAttribute("m_Quest");
             if (questBean != null)
             {
                questBean.setQUEST_DEFINITION(DEBean.getDE_PREFERRED_DEFINITION());
                questBean.setSUBMITTED_LONG_NAME(DEBean.getDOC_TEXT_LONG_NAME());
                questBean.setDE_IDSEQ(DEBean.getDE_DE_IDSEQ());
                questBean.setDE_LONG_NAME(DEBean.getDE_LONG_NAME());
                questBean.setCDE_ID(DEBean.getDE_MIN_CDE_ID());
                questBean.setVD_IDSEQ(DEBean.getDE_VD_IDSEQ());
                questBean.setVD_LONG_NAME(DEBean.getDE_VD_NAME());
                questBean.setSTATUS_INDICATOR("Edit");
                serAC.refreshData(req, res, null, null, null, questBean, "Edit", "");
                //reset the appened attributes to remove all the checking of the row
                Vector vCheck = new Vector();
                session.setAttribute("CheckList", vCheck);
                session.setAttribute("AppendAction", "Not Appended");
 
                //call the method to update Quest contents table
                ret = insAC.setQuestContent(questBean, "", "");
 
               //////  to get new DE's CDE_ID, reload all questions
               String userName = (String)session.getAttribute("Username");
               //call to search questions
               Vector vResult = new Vector();
               serAC.doQuestionSearch(userName, vResult);
               session.setAttribute("vACSearch", vResult);
               session.setAttribute("vSelRows", vResult);
               vResult = new Vector();
               serAC.getQuestionResult(req, res, vResult);
               session.setAttribute("results", vResult);
               /////////////////////////////
                if (ret != null && !ret.equals(""))
                   insAC.storeStatusMsg("\t " + ret + " : Unable to update CRF Questions.");
                else
                   insAC.storeStatusMsg("\t Successfully updated CRF Questions.");
                session.setAttribute("m_Quest", questBean);
             }
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
          //forwards back to create page with empty data if create new
          else
          {
              ret = insAC.setDDE(DEBean.getDE_DE_IDSEQ(), "");   // after Primery DE created, set DEComp rules and relationship
              doInitDDEInfo(req, res);
              DEBean = new DE_Bean();
              DEBean.setDE_ASL_NAME("DRAFT NEW");
              DEBean.setAC_PREF_NAME_TYPE("SYS");
              session.setAttribute("m_DE", DEBean);
              ForwardJSP(req, res, "/CreateDEPage.jsp");
          }
       }
       //sends back to create page with the error message.
       else
       {
           session.setAttribute("sCDEAction", "validate");
           //forward to create or edit pages
           if (isUpdateSuccess == false)
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
           else
             ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
    }  // end of doInsertDEfromMenuAction
 
   /**
   * creates new DE Component and back to CreateDE/EditDE.
   * Called from 'doInsertDEfromMenuAction' method when 'originAction' is 'CreateNewDEFComp'.
   * Retrieves the session bean m_DE.
   * calls 'insAC.setDE' to insert in the database.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'createDEPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertDEComp(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       InsACService insAC = new InsACService(req, res, this);
       String sMenuAction = (String)session.getAttribute("MenuAction");
       String ret = "";
 
       DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
       DE_Bean oldDEBean = (DE_Bean)session.getAttribute("oldDEBean");
       //insert a new one
       ret = insAC.setDE("INS", DEBean, "New", oldDEBean);
       
       if ((ret == null) || ret.equals(""))  //set session vectors
       {
          //One DEComp for DDE is created, add it to vDEComp and back to CreateDE page or EditDE page
           //get exist vDEComp vectors from session
           Vector vDEComp = new Vector();
           Vector vDECompID = new Vector();
           Vector vDECompOrder = new Vector();
           Vector vDECompRelID = new Vector();
           vDEComp = (Vector)session.getAttribute("vDEComp");
           vDECompID = (Vector)session.getAttribute("vDECompID");
           vDECompOrder = (Vector)session.getAttribute("vDECompOrder");
           vDECompRelID = (Vector)session.getAttribute("vDECompRelID");
           int iCount = vDEComp.size() + 1;
           String sCount = Integer.toString(iCount);          
           //add new one to v
           String sName = DEBean.getDE_LONG_NAME();
           if(sName == null || sName.equals(""))
             sName = DEBean.getDE_PREFERRED_NAME();
           vDEComp.addElement(sName);
           vDECompID.addElement(DEBean.getDE_DE_IDSEQ());
           vDECompOrder.addElement(sCount);
           vDECompRelID.addElement("newDEComp");           
           //set v back
           session.setAttribute("vDEComp", vDEComp);
           session.setAttribute("vDECompID", vDECompID);
           session.setAttribute("vDECompOrder", vDECompOrder);
           session.setAttribute("vDECompRelID", vDECompRelID);
           //change flag
           session.setAttribute("originAction", "DECompCreated");
           session.setAttribute("m_DE", oldDEBean);  // back old DE (replace DDE) to session for edit
           session.setAttribute("sCDEAction", "nothing");
           session.setAttribute("DECLongID", "nothing");
           session.setAttribute("VDLongID", "nothing");
       }
       else
       {
           session.setAttribute("sCDEAction", "validate");
       }
 
       //sends back to create page with the error message.
       if (sMenuAction.equalsIgnoreCase("EditDE"))
           ForwardJSP(req, res, "/EditDEPage.jsp");
       else
           ForwardJSP(req, res, "/CreateDEPage.jsp");
    }  // end of doInsertDEComp
 
   /**
   * The doInsertDEC method to insert or update record in the database.
   * Called from 'service' method where reqType is 'validateDECFromForm'.
   * Retrieves the session bean m_DEC.
   * if the action is reEditDEC forwards the page back to Edit or create pages.
   *
   * Otherwise, calls 'doUpdateDECAction' for editing the vd.
   * calls 'doInsertDECfromDEAction' for creating the vd from DE page.
   * calls 'doInsertDECfromMenuAction' for creating the vd from menu .
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertDEC(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
 
       HttpSession session = req.getSession();
       //make sure that status message is empty
       session.setAttribute("statusMessage", "");
       Vector vStat = new Vector();
       session.setAttribute("vStatMsg", vStat);
       String sOriginAction = (String)session.getAttribute("originAction");
       String sDECAction = (String)session.getAttribute("DECAction");
       if (sDECAction == null) sDECAction = "";
       String sDECEditAction = (String)session.getAttribute("DECEditAction");
       if (sDECEditAction == null) sDECEditAction = "";
       String sAction = (String)req.getParameter("ValidateDECPageAction");
       if (sAction == null)
         sAction = "submitting"; //for direct submit without validating
       if (sAction != null)
       {//going back to create or edit pages from validation page
        if (sAction.equals("reEditDEC"))
        {
          if (sDECAction.equals("EditDEC") || sDECAction.equals("BlockEdit"))
             ForwardJSP(req, res, "/EditDECPage.jsp");
          else
              ForwardJSP(req, res, "/CreateDECPage.jsp");
        }
        else
        {
           //update the database for edit action
           if (sDECAction.equals("EditDEC") && !sOriginAction.equals("BlockEditDEC"))
              doUpdateDECAction(req, res);
            else if (sDECEditAction.equals("DECBlockEdit"))
              doUpdateDECActionBE(req, res);
           //if create new dec from create DE page.
           else if (sOriginAction.equals("CreateNewDECfromCreateDE") || sOriginAction.equals("CreateNewDECfromEditDE"))
              doInsertDECfromDEAction(req, res, sOriginAction);
           //FROM MENU, TEMPLATE, VERSION
           else
              doInsertDECfromMenuAction(req, res);
         }
       }
   } // end of doInsertDEC
 
   /**
   * update record in the database and display the result.
   * Called from 'doInsertDEC' method when the aciton is editing.
   * Retrieves the session bean m_DEC.
   * calls 'insAC.setDEC' to update the database.
   * updates the DEbean and sends back to EditDE page if origin is form DEpage
   * otherwise calls 'serAC.refreshData' to get the refreshed search result
   * forwards the page back to search page with refreshed list after updating.
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'EditDECPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doUpdateDECAction(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
       DEC_Bean oldDECBean = (DEC_Bean)session.getAttribute("oldDECBean");
       String sMenu = (String)session.getAttribute("MenuAction");
       InsACService insAC = new InsACService(req, res, this);
      // doInsertDECBlocks(req, res, null);  //insert any building blocks from Thesaurus first
 
       //udpate the status message with DEC name and ID
       insAC.storeStatusMsg("Data Element Concept Name : " + DECBean.getDEC_LONG_NAME());
       insAC.storeStatusMsg("Public ID : " + DECBean.getDEC_DEC_ID());
       
       //call stored procedure to update attributes
       String ret = insAC.setDEC("UPD", DECBean, "Edit", oldDECBean);
       //after succcessful update
       if ((ret == null) || ret.equals(""))
       {
          String sOriginAction = (String)session.getAttribute("originAction");
 
          //forward page back to EditDE
          if (sOriginAction.equals("editDECfromDE"))
          {
             DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
             DEBean.setDE_DEC_IDSEQ(DECBean.getDEC_DEC_IDSEQ());
             DEBean.setDE_DEC_NAME(DECBean.getDEC_LONG_NAME());
             //reset the attributes
             session.setAttribute("originAction", "");
             //add DEC Bean into DE BEan
             DEBean.setDE_DEC_Bean(DECBean);
             DEBean = this.doGetDENames(req, res, "noChange", "editDEC", DEBean);
             session.setAttribute("m_DE", DEBean);
             ForwardJSP(req, res, "/EditDEPage.jsp");
          }
          //go to search page with refreshed list
          else
          {
              DECBean.setDEC_ALIAS_NAME(DECBean.getDEC_PREFERRED_NAME());
              DECBean.setDEC_TYPE_NAME("PRIMARY");
              session.setAttribute("MenuAction", "editDEC");
              GetACSearch serAC = new GetACSearch(req, res, this);
              String oldID = DECBean.getDEC_DEC_IDSEQ();
              serAC.refreshData(req, res, null, DECBean, null, null, "Edit", oldID);
              ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
       }
       //go back to edit page if not successful in update
       else
           ForwardJSP(req, res, "/EditDECPage.jsp");   //back to DEC Page
    }
 
   /**
   * creates new record in the database and display the result.
   * Called from 'doInsertDEC' method when the aciton is create new DEC from DEPage.
   * Retrieves the session bean m_DEC.
   * calls 'insAC.setDEC' to update the database.
   * forwards the page back to create DE page after successful insert.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'createDECPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param sOrigin String value to check where this action originated.
   *
   * @throws Exception
   */
    public void doInsertDECfromDEAction(HttpServletRequest req, HttpServletResponse res, String sOrigin)
    throws Exception
    {
       HttpSession session = req.getSession();
       InsACService insAC = new InsACService(req, res, this);
       DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
       DEC_Bean oldDECBean = (DEC_Bean)session.getAttribute("oldDECBean");
       String sMenu = (String)session.getAttribute("MenuAction");
   //    doInsertDECBlocks(req, res, null);  //insert any building blocks from Thesaurus first
       String ret = insAC.setDEC("INS", DECBean, "New", oldDECBean);
       //add new dec attributes to de bean and forward to create de page if success.
       if ((ret == null) || ret.equals(""))
       {
          DE_Bean DEBean  = (DE_Bean)session.getAttribute("m_DE");
          DEBean.setDE_DEC_NAME(DECBean.getDEC_LONG_NAME());
          DEBean.setDE_DEC_IDSEQ(DECBean.getDEC_DEC_IDSEQ());
          //add DEC Bean into DE BEan
          DEBean.setDE_DEC_Bean(DECBean);
          DEBean = this.doGetDENames(req, res, "new", "newDEC", DEBean);
          session.setAttribute("m_DE", DEBean);
 
          String sContext = (String)session.getAttribute("sDefaultContext");
          boolean bNewContext = true;
 
          session.setAttribute("originAction", "");  //reset this session attribute
          if (sOrigin != null && sOrigin.equals("CreateNewDECfromEditDE"))
             ForwardJSP(req, res, "/EditDEPage.jsp");
          else
             ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       //go back to create dec page if error
       else
       {
           session.setAttribute("DECPageAction", "validate");
           ForwardJSP(req, res, "/CreateDECPage.jsp");   //send it back to dec page
       }
    }
 
   /**
   * to created object class, property and qualifier value from EVS into cadsr.
   * Retrieves the session bean m_DEC.
   * calls 'insAC.setDECQualifier' to insert the database.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param  DEC_Bean dec attribute bean.
   *
   * @throws Exception
   */
    public void doInsertDECBlocks(HttpServletRequest req, HttpServletResponse res, DEC_Bean DECBeanSR)
    throws Exception
    {
 logger.debug("doInsertDECBlocks");
       HttpSession session = req.getSession();
       InsACService insAC = new InsACService(req, res, this);
       String sDEC_ID = "";
       String sOCL_IDSEQ = "";
       String sPROPL_IDSEQ = "";
       String retObj = "";
       String retProp = "";
       String retObjQual = "";
       String retPropQual = "";  
       String sNewOC = (String)session.getAttribute("newObjectClass");
       String sNewProp = (String)session.getAttribute("newProperty");
       if(sNewOC == null) sNewOC = "";
       if(sNewProp == null) sNewProp = "";
       if (DECBeanSR == null)
         DECBeanSR = (DEC_Bean)session.getAttribute("m_DEC");
       String sRemoveOCBlock = (String)session.getAttribute("RemoveOCBlock");
       String sRemovePropBlock = (String)session.getAttribute("RemovePropBlock");
       if(sRemoveOCBlock == null) sRemoveOCBlock = "";
       if(sRemovePropBlock == null) sRemovePropBlock = "";
   
       if (sNewOC.equals("true"))
         retObj = insAC.setObjectClassDEC("INS", DECBeanSR, req);
       else if(sRemoveOCBlock.equals("true"))
         retObj = insAC.setObjectClassDEC("INS", DECBeanSR, req);
       
       if (sNewProp.equals("true"))
         retProp = insAC.setPropertyDEC("INS", DECBeanSR, req);
       else if(sRemovePropBlock.equals("true"))
         retProp = insAC.setPropertyDEC("INS", DECBeanSR, req);
 
      session.setAttribute("RemoveOCBlock", "");
      session.setAttribute("RemovePropBlock", "");
      session.setAttribute("newObjectClass", "");
      session.setAttribute("newProperty", "");
    }
 
 
   /**
   * creates new record in the database and display the result.
   * Called from 'doInsertDEC' method when the aciton is create new DEC from Menu.
   * Retrieves the session bean m_DEC.
   * calls 'insAC.setVD' to update the database.
   * calls 'serAC.refreshData' to get the refreshed search result for template/version
   * forwards the page back to create DEC page if new DEC or back to search page
   * if template or version after successful insert.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'createDECPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertDECfromMenuAction(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       InsACService insAC = new InsACService(req, res, this);
       GetACSearch serAC = new GetACSearch(req, res, this);
       String ret = "";
       String ret2 = "";
       boolean isUpdateSuccess = true;
       String sMenuAction = (String)session.getAttribute("MenuAction");
       DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
       DEC_Bean oldDECBean = (DEC_Bean)session.getAttribute("oldDECBean");
 
   //    doInsertDECBlocks(req, res, null);  //insert any building blocks from Thesaurus first
 
       if (sMenuAction.equals("NewDECVersion"))
       {
          //udpate the status message with DEC name and ID
          insAC.storeStatusMsg("Data Element Concept Name : " + DECBean.getDEC_LONG_NAME());
          insAC.storeStatusMsg("Public ID : " + DECBean.getDEC_DEC_ID());
          //creates new version first and updates all other attributes
          ret = insAC.setAC_VERSION(null, DECBean, null, "DataElementConcept");
          if (ret == null || ret.equals(""))
          {
             //update other attributes
             ret = insAC.setDEC("UPD", DECBean, "Version", oldDECBean);
           //  resetEVSBeans(req, res);
             if (ret != null && !ret.equals(""))
             {
                //session.setAttribute("statusMessage", ret + " - Created new version but unable to update attributes successfully.");
                //add newly created row to searchresults and send it to edit page for update
                isUpdateSuccess = false;
                //put back old attributes except version, idseq and workflow status
                String oldID = oldDECBean.getDEC_DEC_IDSEQ();
                String newID = DECBean.getDEC_DEC_IDSEQ();
                String newVersion = DECBean.getDEC_VERSION();
                DECBean = DECBean.cloneDEC_Bean(oldDECBean);
                DECBean.setDEC_DEC_IDSEQ(newID);
                DECBean.setDEC_VERSION(newVersion);
                DECBean.setDEC_ASL_NAME("DRAFT MOD");
                //add newly created dec into the resultset.
                serAC.refreshData(req, res, null, DECBean, null, null, "Version", oldID);
             }
          }
          else
             insAC.storeStatusMsg("\t " + ret + " - Unable to create new version successfully.");
       }
       else
       {
          ret = insAC.setDEC("INS", DECBean, "New", oldDECBean);
       }
 
       if ((ret == null) || ret.equals(""))
       {
           //session.setAttribute("statusMessage", "New Data Element Concept is created successfully.");
           session.setAttribute("DECPageAction", "nothing");
           String sContext = (String)session.getAttribute("sDefaultContext");
           boolean bNewContext = true;
 
           session.setAttribute("originAction", "");  //reset this session attribute
           //forwards to search page with the refreshed list after success if template or version
           if ((sMenuAction.equals("NewDECTemplate")) || (sMenuAction.equals("NewDECVersion")))
           {
             session.setAttribute("searchAC", "DataElementConcept");
             DECBean.setDEC_ALIAS_NAME(DECBean.getDEC_PREFERRED_NAME());
             DECBean.setDEC_TYPE_NAME("PRIMARY");
             String oldID = oldDECBean.getDEC_DEC_IDSEQ();
             if (sMenuAction.equals("NewDECTemplate"))
                serAC.refreshData(req, res, null, DECBean, null, null, "Template", oldID);
             else if (sMenuAction.equals("NewDECVersion"))
                serAC.refreshData(req, res, null, DECBean, null, null, "Version", oldID);
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
           }
           //go back to create dec page to create another one
           else
           {
              DECBean = new DEC_Bean();
              DECBean.setDEC_ASL_NAME("DRAFT NEW");
              DECBean.setAC_PREF_NAME_TYPE("SYS");
              session.setAttribute("m_DEC", DECBean);
              EVS_Bean m_OC = new EVS_Bean();
              session.setAttribute("m_OC", m_OC);
              EVS_Bean m_PC = new EVS_Bean();
              session.setAttribute("m_PC", m_PC);
              EVS_Bean m_OCQ = new EVS_Bean();
              session.setAttribute("m_OCQ", m_OCQ);
              EVS_Bean m_PCQ = new EVS_Bean();
              session.setAttribute("m_PCQ", m_PCQ);
              session.setAttribute("selObjQRow", "");
              session.setAttribute("selObjRow", "");
              session.setAttribute("selPropQRow", "");
              session.setAttribute("selPropRow", "");
              ForwardJSP(req, res, "/CreateDECPage.jsp");
           }
       }
       //go back to create dec page if error occurs
       else
       {
           session.setAttribute("DECPageAction", "validate");
           //forward to create or edit pages
           if (isUpdateSuccess == false)
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
           else
             ForwardJSP(req, res, "/CreateDECPage.jsp");
       }
    }
 
   /**
   * The doInsertPV method to insert or update record in the database.
   * Called from 'service' method where reqType is 'validatePVFromForm'.
   * Retrieves the session bean m_PV.
   * Calls 'insAC.setPV' to submit the record to the database.
   * Calls 'getAC.getPermissableValues' method to reload the PV data from database to get the fresh list and
   * forwards the page 'CreateVDPage.jsp' to go back to creating new VD.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertPV(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       InsACService insAC = new InsACService(req, res, this);  //
       String ret = "";
       String sPV_ID = "";
       NCICurationServlet servlet;
       //get the value of the origin
       String sOrigin = (String)session.getAttribute("originAction");
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
       //String sAction = (String)req.getParameter("newCDEPageAction");
       String sAction = (String)req.getParameter("ValidatePVPageAction");
       String sPVSubmit = (String)session.getAttribute("PVAction");
 
       String sMenuAction = (String)session.getAttribute("MenuAction");
       PV_Bean m_PV = (PV_Bean)session.getAttribute("m_PV");
 
       if (sAction == null)
         sAction = "submitting"; //for direct submit without validating
       if (sAction.equals("reEditPV"))
       {
          if (sOrigin.equals("CreateInSearch"))
             ForwardJSP(req, res, "/CreatePVSearchPage.jsp");
          else
             ForwardJSP(req, res, "/CreatePVPage.jsp");
       }
       else 
       {
         //create the pv in the database
         if (sPVSubmit.equals("createPV"))
         {
           ret = insAC.setPV("INS", sPV_ID, m_PV);
           //add new pv attribute to vd bean after successful create
           if (ret != null && !ret.equals("") && !ret.equals("API_PV_300"))
           {
              session.setAttribute("statusMessage", ret + " : Unable to create a Permissible Value successfully.");
              session.setAttribute("m_PV", m_PV);
              ForwardJSP(req, res, "/CreatePVPage.jsp");  //back to PV Screen
           }
         }
         //remove matched valid value from the non matched list
         String sVV = m_PV.getQUESTION_VALUE();
         if (sVV != null && !sVV.equals(""))
         {
           Vector vQVList = (Vector)session.getAttribute("NonMatchVV");
           if (vQVList != null && vQVList.contains(sVV))
             vQVList.remove(sVV);
           session.setAttribute("NonMatchVV", vQVList);
         }
         //loop through the pvlist to update edit pv or new pv attributes
         Vector vVDPV = (Vector)session.getAttribute("VDPVList");
         if (vVDPV == null) vVDPV = new Vector();
         //add the bean to list if creating new one and nothing exists
         boolean isCreated = false;
         for (int i=0; i<vVDPV.size(); i++)
         {
           PV_Bean thisPV = (PV_Bean)vVDPV.elementAt(i);
           String sValue = m_PV.getPV_VALUE();
           String sMeaning = m_PV.getPV_SHORT_MEANING();
           //check if newly created exists in the vd list already
           if (sPVSubmit.equals("createPV"))
           {
             if(thisPV.getPV_VALUE().equals(sValue) && thisPV.getPV_SHORT_MEANING().equals(sMeaning))
             {
               isCreated = true;
               session.setAttribute("statusMessage", "Permissible Value already exists for the Value Domain");
               break;
             }
           }
           //updating the single one
           else if (sPVSubmit.equals("editPV"))
           {
             if (thisPV.getPV_CHECKED())
             {
               String s = m_PV.getPV_VALUE_ORIGIN();
               if (s != null && !s.equals("")) thisPV.setPV_VALUE_ORIGIN(s);
               s = m_PV.getPV_BEGIN_DATE();
               if (s != null && !s.equals("")) thisPV.setPV_BEGIN_DATE(s);
               s = m_PV.getPV_END_DATE();
               if (s != null && !s.equals("")) thisPV.setPV_END_DATE(s);
               if (sValue != null && !sValue.equals("")) thisPV.setPV_VALUE(sValue);
               //update the submit action according to pv idseq
               s = thisPV.getPV_PV_IDSEQ();
               if (s == null || s.equals("") || (s.length()>3 && s.indexOf("EVS")>-1))
                 thisPV.setVP_SUBMIT_ACTION("INS");
               else
                 thisPV.setVP_SUBMIT_ACTION("UPD");
               thisPV.setPV_CHECKED(false);  //set it to false as it is done
               //update the vector
               vVDPV.setElementAt(thisPV, i);
             }
           }
         }
         //add the new one on the list.
         if (!isCreated)
         {
          session.setAttribute("statusMessage", "Permissible Value created/updated successfully.");
           if (sPVSubmit.equals("createPV"))
           {
             m_PV.setVP_SUBMIT_ACTION("INS");
             vVDPV.addElement(m_PV);
           }
         }
         //update teh session attribute
         session.setAttribute("VDPVList", vVDPV);
         
         //forward the page to appropriate jsps
         if (sOrigin.equalsIgnoreCase("editVD") || sOrigin.equals("editVDfromDE")) // || sOrigin.equals("CreateNewVDfromEditDE"))
         {
             session.setAttribute("VDEditAction", "EditAfterPV");
             ForwardJSP(req, res, "/EditVDPage.jsp");  //back to Edit VD Screen
         }
         else
             ForwardJSP(req, res, "/CreateVDPage.jsp");  //back to Create VD Screen
       
       }  
   } // end of doInsertPV
 
    /**
   * The doInsertVM method retrieves the session beans m_VM and m_PV, calls the stored
   * procedures setVM and setPV to submit the new values and meanings to the database,
   * then forwards back to the Create page.
   * Called from 'service' method where reqType is 'validateVMFromForm'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertVM(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       String sOrigin = (String)session.getAttribute("originAction");
       InsACService insAC = new InsACService(req, res, this);  //
       String ret = "";
       VM_Bean m_VM = (VM_Bean)session.getAttribute("m_VM");
       String sAction = (String)req.getParameter("ValidateVMPageAction");
       String sEVSSearch = (String)session.getAttribute("EVSSearched");
       session.setAttribute("statusMessage", "");
       boolean isSuccessSubmit = false;
       if (sAction != null && sAction.equals("reEditPV"))
       {
         session.setAttribute("VMBack", "true");
         ForwardJSP(req, res, "/CreateVMPage.jsp");
       }
       else
       {
         insAC.storeStatusMsg("Value Meaning : " + m_VM.getVM_SHORT_MEANING());
         m_VM = insAC.getVM(m_VM);
         //create new one if not exists
         ret = m_VM.getRETURN_CODE();
         if (ret != null && !ret.equals(""))
           ret = insAC.setVM("INS", m_VM);
         //empty the vmconcept if not continues as evs search
         if (sEVSSearch == null || sEVSSearch.equals(""))
         {
           m_VM.setVM_CONCEPT(new EVS_Bean());
           ret = (String)req.getAttribute("retcode");
           if (ret == null || ret.equals(""))  //update cd vm relationship
             insAC.setCDVMS("INS", m_VM);
         }
         else      //call to update vm with evs attributes        
           ret = insAC.setVM_EVS("UPD", m_VM);
           
         //display success message if no error exists and update pv bean
         String sReturn = (String)req.getAttribute("retcode");
         if (sReturn == null || sReturn.equals(""))
         {
             insAC.storeStatusMsg("\\t Successfully updated Value Meaning Attributes");
             PV_Bean m_PV = (PV_Bean)session.getAttribute("m_PV");
             String sName = m_VM.getVM_SHORT_MEANING();
             m_PV.setPV_SHORT_MEANING(sName);
             m_PV.setPV_MEANING_DESCRIPTION(m_VM.getVM_DESCRIPTION());
             m_PV.setVM_CONCEPT(m_VM.getVM_CONCEPT());
             session.setAttribute("m_PV", m_PV);
             ForwardJSP(req, res, "/CreatePVPage.jsp");
         }
         //go back to vm page if error occured.
         else
         {
           session.setAttribute("m_VM", m_VM);
           ForwardJSP(req, res, "/CreateVMPage.jsp");
         }
       }
 
   } // end of doInsertVM
 
    /**
   * The doInsertVM method retrieves the session beans m_VM and m_PV, calls the stored
   * procedures setVM and setPV to submit the new values and meanings to the database,
   * then forwards back to the Create page.
   * Called from 'service' method where reqType is 'validateVMFromForm'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertVMOld(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       String sOrigin = (String)session.getAttribute("originAction");
       InsACService insAC = new InsACService(req, res, this);  //
       String ret = "";
       String ret2 = "";
       String sVM_ID = "";
       String sPV_ID = "";
       VM_Bean m_VM = (VM_Bean)session.getAttribute("m_VM");
       String sAction = (String)req.getParameter("ValidateVMPageAction");
       String sEVSSearch = (String)session.getAttribute("EVSSearched");
       session.setAttribute("statusMessage", "");
       if (sAction == null)
         sAction = "submitting"; //for direct submit without validating
       boolean isSuccessSubmit = false;
       if (sAction.equals("reEditPV"))
       {
          if (sOrigin.equals("CreateInSearch"))
             ForwardJSP(req, res, "/CreateVMSearchPage.jsp");
          else
          {
            session.setAttribute("VMBack", "true");
            ForwardJSP(req, res, "/CreateVMPage.jsp");
          }
       }
       else
       {
         insAC.storeStatusMsg("Value Meaning : " + m_VM.getVM_SHORT_MEANING());    
         //empty the vmconcept if not continues as evs search
         if (sEVSSearch == null || sEVSSearch.equals(""))
         {
         logger.debug("am I here? ");
             m_VM.setVM_CONCEPT(new EVS_Bean());
         }
         //updates the pv bean's vm attribute after creating a new one.
         if ((ret == null) || (ret.equals("")) || (ret.equals("API_VM_300")))
         {
           isSuccessSubmit = true;
         }  
         else
         {
             //session.setAttribute("statusMessage", ret + " : Unable to create Value Meaning successfully.");
             session.setAttribute("m_VM", m_VM);
             if (sOrigin.equals("CreateInSearch"))
               ForwardJSP(req, res, "/CreateVMSearchPage.jsp");
             else
               ForwardJSP(req, res, "/CreateVMPage.jsp");
         }
       }
       //display success message if no error exists for each DE
       String sReturn = (String)req.getAttribute("retcode");
       if (sReturn == null || sReturn.equals(""))
           insAC.storeStatusMsg("\\t Successfully updated Value Meaning Attributes");
       
       //forword the page to create pv after successful creation of VM or using the existing one
       if (isSuccessSubmit == true)
       {
         PV_Bean m_PV = (PV_Bean)session.getAttribute("m_PV");
         String sName = m_VM.getVM_SHORT_MEANING();
         m_PV.setPV_SHORT_MEANING(sName);
         m_PV.setPV_MEANING_DESCRIPTION(m_VM.getVM_DESCRIPTION());
         m_PV.setVM_CONCEPT(m_VM.getVM_CONCEPT());
         session.setAttribute("m_PV", m_PV);
         if (sOrigin.equals("CreateInSearch"))
             ForwardJSP(req, res, "/CreatePVSearchPage.jsp");
         else
             ForwardJSP(req, res, "/CreatePVPage.jsp");
       }
 
   } // end of doInsertVM
 
   /**
   * The doInsertVD method to insert or update record in the database.
   * Called from 'service' method where reqType is 'validateVDFromForm'.
   * Retrieves the session bean m_VD.
   * if the action is reEditVD forwards the page back to Edit or create pages.
   *
   * Otherwise, calls 'doUpdateVDAction' for editing the vd.
   * calls 'doInsertVDfromDEAction' for creating the vd from DE page.
   * calls 'doInsertVDfromMenuAction' for creating the vd from menu .
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertVD(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       //make sure that status message is empty
       session.setAttribute("statusMessage", "");
       Vector vStat = new Vector();
       session.setAttribute("vStatMsg", vStat);
       String sVDAction = (String)session.getAttribute("VDAction");
       if (sVDAction == null) sVDAction = "";
       String sVDEditAction = (String)session.getAttribute("VDEditAction");
       if (sVDEditAction == null) sVDEditAction = "";
       String sAction = (String)req.getParameter("ValidateVDPageAction");
       String sMenuAction = (String)session.getAttribute("MenuAction");
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
       String sOriginAction = (String)session.getAttribute("originAction");
       if (sAction == null)
         sAction = "submitting"; //for direct submit without validating
 
       String sNewCDEPageAction = (String)req.getParameter("newCDEPageAction");
       if (sAction != null)
       {  //goes back to create/edit pages from validation page
         if (sAction.equals("reEditVD"))
         {
           if (sVDAction.equals("EditVD") || sVDAction.equals("BlockEdit"))
               ForwardJSP(req, res, "/EditVDPage.jsp");
           else
               ForwardJSP(req, res, "/CreateVDPage.jsp");
         }
         else
         {
           //edit the existing vd
           if (sVDAction.equals("EditVD") && !sOriginAction.equals("BlockEditVD"))
              doUpdateVDAction(req, res);
           else if (sVDEditAction.equals("VDBlockEdit"))
              doUpdateVDActionBE(req, res);
           //if create new vd from create/edit DE page.
           else if (sOriginAction.equals("CreateNewVDfromCreateDE") || sOriginAction.equals("CreateNewVDfromEditDE"))
              doInsertVDfromDEAction(req, res, sOriginAction);
           //from the menu  AND template/ version
           else
               doInsertVDfromMenuAction(req, res);
         }
       }
   } // end of doInsertVD
 
   /**
   * update record in the database and display the result.
   * Called from 'doInsertVD' method when the aciton is editing.
   * Retrieves the session bean m_VD.
   * calls 'insAC.setVD' to update the database.
   * updates the DEbean and sends back to EditDE page if origin is form DEpage
   * otherwise calls 'serAC.refreshData' to get the refreshed search result
   * forwards the page back to search page with refreshed list after updating.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'EditVDPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doUpdateVDAction(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
       VD_Bean oldVDBean = (VD_Bean)session.getAttribute("oldVDBean");
       String sMenu = (String)session.getAttribute("MenuAction");
       InsACService insAC = new InsACService(req, res, this);
       doInsertVDBlocks(req, res, null);
       //udpate the status message with DE name and ID
       insAC.storeStatusMsg("Value Domain Name : " + VDBean.getVD_LONG_NAME());
       insAC.storeStatusMsg("Public ID : " + VDBean.getVD_VD_ID());
       
       //call stored procedure to update attributes
       String ret = insAC.setVD("UPD", VDBean, "Edit", oldVDBean);
       //forward to search page with refreshed list after successful update
       if ((ret == null) || ret.equals(""))
       {
          this.clearCreateSessionAttributes(req, res);  //clear some session attributes
          String sOriginAction = (String)session.getAttribute("originAction");
          GetACSearch serAC = new GetACSearch(req, res, this);
          //forward page back to EditDE
          if (sOriginAction.equals("editVDfromDE") || sOriginAction.equals("EditDE"))
          {
             DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
             DEBean.setDE_VD_IDSEQ(VDBean.getVD_VD_IDSEQ());
             DEBean.setDE_VD_PREFERRED_NAME(VDBean.getVD_PREFERRED_NAME());
             DEBean.setDE_VD_NAME(VDBean.getVD_LONG_NAME());
             //reset the attributes
             session.setAttribute("originAction", "");
             //add DEC Bean into DE BEan
             DEBean.setDE_VD_Bean(VDBean);
             session.setAttribute("m_DE", DEBean);
             DEBean = this.doGetDENames(req, res, "noChange", "editVD", DEBean);
             ForwardJSP(req, res, "/EditDEPage.jsp");
          }
          //go to search page with refreshed list
          else
          {
              VDBean.setVD_ALIAS_NAME(VDBean.getVD_PREFERRED_NAME());
              VDBean.setVD_TYPE_NAME("PRIMARY");
              session.setAttribute("MenuAction", "editVD");
              String oldID = VDBean.getVD_VD_IDSEQ();
              serAC.refreshData(req, res, null, null, VDBean, null, "Edit", oldID);
              ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
       }
       //goes back to edit page if error occurs
       else
       {
           session.setAttribute("VDPageAction", "nothing");
           ForwardJSP(req, res, "/EditVDPage.jsp");
       }
    }
 
  /**
   * update record in the database and display the result.
   * Called from 'doInsertVD' method when the aciton is editing.
   * Retrieves the session bean m_VD.
   * calls 'insAC.setVD' to update the database.
   * updates the DEbean and sends back to EditDE page if origin is form DEpage
   * otherwise calls 'serAC.refreshData' to get the refreshed search result
   * forwards the page back to search page with refreshed list after updating.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'EditVDPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doUpdateVDActionBE(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD"); // validated edited m_VD
       boolean isRefreshed = false;
       String ret = ":";
       InsACService insAC = new InsACService(req, res, this);
       GetACSearch serAC = new GetACSearch(req, res, this);
       GetACService getAC = new GetACService(req, res, this);
       Vector vStatMsg = new Vector();
       String sNewRep = (String)session.getAttribute("newRepTerm");
 
       Vector vBERows = (Vector)session.getAttribute("vBEResult");
       int vBESize = vBERows.size();
       Integer vBESize2 = new Integer(vBESize);
       req.setAttribute("vBESize", vBESize2);
       String sRep_IDSEQ = "";
       if (vBERows.size()>0)
       {
         for(int i=0; i<(vBERows.size()); i++)
         {
           //String sVD_ID = ""; //out
           VD_Bean VDBeanSR = new VD_Bean();
           VDBeanSR = (VD_Bean)vBERows.elementAt(i);
           VD_Bean oldVDBean = new VD_Bean();
           oldVDBean = oldVDBean.cloneVD_Bean(VDBeanSR);
           String oldName = (String)VDBeanSR.getVD_PREFERRED_NAME();
 
             //gets the point or whole from the VD Bean's version attribute
           String newVersion = (String)VDBean.getVD_VERSION();
           if (newVersion == null) newVersion = "";
           //create newly selected rep term
           if (i==0 && sNewRep.equals("true"))
           {
             doInsertVDBlocks(req, res, VDBeanSR);  //create it
             sRep_IDSEQ = VDBeanSR.getVD_REP_IDSEQ();  //get rep idseq
             if(sRep_IDSEQ == null) sRep_IDSEQ = "";
             VDBean.setVD_REP_IDSEQ(sRep_IDSEQ);  //add page vd bean
             
             String sRep_Condr = VDBeanSR.getVD_REP_CONDR_IDSEQ();  //get rep condr
             if(sRep_Condr == null) sRep_Condr = "";   
             VDBean.setVD_REP_CONDR_IDSEQ(sRep_Condr);  //add to page vd bean
             
             VDBean.setVD_REP_QUAL("");
           }
           //updates the data from the page into the sr bean
           InsertEditsIntoVDBeanSR(VDBeanSR, VDBean, req);
          // session.setAttribute("m_VD", VDBeanSR);         
           String oldID = oldVDBean.getVD_VD_IDSEQ();
           //udpate the status message with DE name and ID
           insAC.storeStatusMsg("Value Domain Name : " + VDBeanSR.getVD_LONG_NAME());
           insAC.storeStatusMsg("Public ID : " + VDBeanSR.getVD_VD_ID());
           //insert the version
           if (newVersion.equals("Point") || newVersion.equals("Whole"))  // block version
           {
              //creates new version first and updates all other attributes
              String strValid = m_setAC.checkUniqueInContext("Version", "VD", null, null, VDBeanSR, getAC, "version");
              if (strValid != null && !strValid.equals(""))
                 ret = "unique constraint";
              else
                 ret = insAC.setAC_VERSION(null, null, VDBeanSR, "ValueDomain");
              if (ret == null || ret.equals(""))
              {
                 session.setAttribute("VDPVList", new Vector());  //empty the vector to get fresh
                 serAC.doPVACSearch(VDBeanSR.getVD_VD_IDSEQ(), VDBeanSR.getVD_LONG_NAME(), "Edit");
                 //get the right system name for new version
                 String prefName = VDBeanSR.getVD_PREFERRED_NAME();
                 String vdID = VDBeanSR.getVD_VD_ID();
                 String newVer = "v" + VDBeanSR.getVD_VERSION();
                 String oldVer = "v" + oldVDBean.getVD_VERSION();
                 //replace teh version number if system generated name
                 if (prefName.indexOf(vdID) > 0)
                 {
                   prefName = prefName.replaceFirst(oldVer, newVer);
                   VDBean.setVD_PREFERRED_NAME(prefName);
                 }
                 ret = insAC.setVD("UPD", VDBeanSR, "Version", oldVDBean);
                 if (ret == null || ret.equals(""))
                 {
                    serAC.refreshData(req, res, null, null, VDBeanSR, null, "Version", oldID);
                    isRefreshed = true;
                    //reset the appened attributes to remove all the checking of the row
                    Vector vCheck = new Vector();
                    session.setAttribute("CheckList", vCheck);
                    session.setAttribute("AppendAction", "Not Appended");
                    //resetEVSBeans(req, res);
                 }
              }
              //alerady exists
              else if (ret.indexOf("unique constraint") >= 0)
                 insAC.storeStatusMsg("\\t New version " + VDBeanSR.getVD_VERSION() + " already exists in the data base.\\n");
              //some other problem
              else
                 insAC.storeStatusMsg("\\t " + ret + " : Unable to create new version " + VDBeanSR.getVD_VERSION() +  ".\\n");
           }
           else  // block edit
           {
               ret = insAC.setVD("UPD", VDBeanSR, "Edit", oldVDBean);
               //forward to search page with refreshed list after successful update
               if ((ret == null) || ret.equals(""))
               {
                 serAC.refreshData(req, res, null, null, VDBeanSR, null, "Edit", oldID);
                 isRefreshed = true;
               }
            }
         }
       }
 
       //to get the final result vector if not refreshed at all
       if (!(isRefreshed))
       {
          Vector vResult = new Vector();
          serAC.getVDResult(req, res, vResult, "");
          session.setAttribute("results", vResult);    //store the final result in the session
          session.setAttribute("VDPageAction", "nothing");
       }
 
       ForwardJSP(req, res, "/SearchResultsPage.jsp");
    }
 
   /** called after setDEC or setVD to reset EVS session attributes
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void resetEVSBeans(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
         HttpSession session = req.getSession();
         EVS_Bean m_OC = new EVS_Bean();
         session.setAttribute("m_OC", m_OC);
         EVS_Bean m_PC = new EVS_Bean();
         session.setAttribute("m_PC", m_PC);
         EVS_Bean m_Rep = new EVS_Bean();
         session.setAttribute("m_Rep", m_Rep);
         EVS_Bean m_OCQ = new EVS_Bean();
         session.setAttribute("m_OCQ", m_OCQ);
         EVS_Bean m_PCQ = new EVS_Bean();
         session.setAttribute("m_PCQ", m_PCQ);
         EVS_Bean m_REPQ = new EVS_Bean();
         session.setAttribute("m_REPQ", m_REPQ);
 
         session.setAttribute("selPropRow", "");
         session.setAttribute("selPropQRow", "");
         session.setAttribute("selObjQRow", "");
         session.setAttribute("selObjRow", "");
         session.setAttribute("selRepQRow", "");
         session.setAttribute("selRepRow", "");
    }
 
   /**
   * updates bean the selected VD from the changed values of block edit.
   *
   * @param req The HttpServletRequest from the client
   * @param VDBeanSR selected vd bean from search result
   * @param vd VD_Bean of the changed values.
   *
   * @throws Exception
   */
    public void InsertEditsIntoVDBeanSR(VD_Bean VDBeanSR, VD_Bean vd, HttpServletRequest req)
    throws Exception
    {
     // get all attributes of VDBean, if attribute != "" then set that attribute of VDBeanSR
       String sDefinition = vd.getVD_PREFERRED_DEFINITION();
       if (sDefinition == null) sDefinition = "";
       if (!sDefinition.equals("")) VDBeanSR.setVD_PREFERRED_DEFINITION(sDefinition);
       String sCD_ID = vd.getVD_CD_IDSEQ();
       if (sCD_ID == null) sCD_ID = "";
       if (!sCD_ID.equals("") && !sCD_ID.equals(null)) VDBeanSR.setVD_CD_IDSEQ(sCD_ID);
       String sCDName = vd.getVD_CD_NAME();
       if (sCDName == null) sCDName = "";
       if (!sCDName.equals("") && !sCDName.equals(null)) VDBeanSR.setVD_CD_NAME(sCDName);
       String sAslName = vd.getVD_ASL_NAME();
       if (sAslName == null) sAslName = "";
       if (!sAslName.equals("")) VDBeanSR.setVD_ASL_NAME(sAslName);
       String sDtlName = vd.getVD_DATA_TYPE();
       if (sDtlName == null) sDtlName = "";
       if (!sDtlName.equals("")) VDBeanSR.setVD_DATA_TYPE(sDtlName);
       String sMaxLength = vd.getVD_MAX_LENGTH_NUM();
       if (sMaxLength == null) sMaxLength = "";
       if (!sMaxLength.equals("")) VDBeanSR.setVD_MAX_LENGTH_NUM(sMaxLength);
       String sFormlName = vd.getVD_FORML_NAME(); //UOM Format
       if (sFormlName == null) sFormlName = "";
       if (!sFormlName.equals("")) VDBeanSR.setVD_FORML_NAME(sFormlName);
       String sUomlName = vd.getVD_UOML_NAME();
       if (sUomlName == null) sUomlName = "";
       if (!sUomlName.equals("")) VDBeanSR.setVD_UOML_NAME(sUomlName);
       String sLowValue = vd.getVD_LOW_VALUE_NUM();
       if (sLowValue == null) sLowValue = "";
       if (!sLowValue.equals("")) VDBeanSR.setVD_LOW_VALUE_NUM(sLowValue);
       String sHighValue = vd.getVD_HIGH_VALUE_NUM();
       if (sHighValue == null) sHighValue = "";
       if (!sHighValue.equals("")) VDBeanSR.setVD_HIGH_VALUE_NUM(sHighValue);
       String sMinLength = vd.getVD_MIN_LENGTH_NUM();
       if (sMinLength == null) sMinLength = "";
       if (!sMinLength.equals("")) VDBeanSR.setVD_MIN_LENGTH_NUM(sMinLength);
       String sDecimalPlace = vd.getVD_DECIMAL_PLACE();
       if (sDecimalPlace == null) sDecimalPlace = "";
       if (!sDecimalPlace.equals("")) VDBeanSR.setVD_DECIMAL_PLACE(sDecimalPlace);
       String sBeginDate = vd.getVD_BEGIN_DATE();
       if (sBeginDate == null) sBeginDate = "";
       if (!sBeginDate.equals("")) VDBeanSR.setVD_BEGIN_DATE(sBeginDate);
       String sEndDate = vd.getVD_END_DATE();
       if (sEndDate == null) sEndDate = "";
       if (!sEndDate.equals("")) VDBeanSR.setVD_END_DATE(sEndDate);
       String sSource = vd.getVD_SOURCE();
       if (sSource == null) sSource = "";
       if (!sSource.equals("")) VDBeanSR.setVD_SOURCE(sSource);
       String changeNote = vd.getVD_CHANGE_NOTE();
       if (changeNote == null) changeNote = "";
       if (!changeNote.equals("")) VDBeanSR.setVD_CHANGE_NOTE(changeNote);
 
       //get cs-csi from the page into the DECBean for block edit
       Vector vAC_CS = vd.getVD_AC_CSI_VECTOR();
       if (vAC_CS != null) VDBeanSR.setVD_AC_CSI_VECTOR(vAC_CS);
 
       String sRepTerm = vd.getVD_REP_TERM();
       if (sRepTerm == null) sRepTerm = "";
       if (!sRepTerm.equals("")) VDBeanSR.setVD_REP_TERM(sRepTerm);
       
       String sRepCondr = vd.getVD_REP_CONDR_IDSEQ();
       if (sRepCondr == null) sRepCondr = "";
       if (!sRepCondr.equals("")) VDBeanSR.setVD_REP_CONDR_IDSEQ(sRepCondr);
 
       String sREP_IDSEQ = vd.getVD_REP_IDSEQ();
       if (sREP_IDSEQ != null && !sREP_IDSEQ.equals(""))
         VDBeanSR.setVD_REP_IDSEQ(sREP_IDSEQ);
    /*   String sREP_IDSEQ = (String)req.getAttribute("REP_IDSEQ");
       if(sREP_IDSEQ == null || sREP_IDSEQ.equals(""))
       {
         String sRepTermID = vd.getVD_REP_IDSEQ();
         if (sRepTermID == null) sRepTermID = "";
         if (!sRepTermID.equals("")) VDBeanSR.setVD_REP_IDSEQ(sRepTermID);
       }
       else
          VDBeanSR.setVD_REP_IDSEQ(sREP_IDSEQ);  */
 
       String sRepQual = vd.getVD_REP_QUAL();
       if (sRepQual == null) sRepQual = "";
       if (!sRepQual.equals("")) VDBeanSR.setVD_REP_QUAL(sRepQual);
 
       String version = vd.getVD_VERSION();
       String lastVersion = (String)VDBeanSR.getVD_VERSION();
       int index = -1;
       String pointStr = ".";
       String strWhBegNumber = "";
       int iWhBegNumber = 0;
       index = lastVersion.indexOf(pointStr);
       String strPtBegNumber = lastVersion.substring(0, index);
       String afterDecimalNumber = lastVersion.substring((index + 1 ), (index + 2));
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
         int iBDNumber = bdNumber.intValue();  //before decimal
         if (iADNumber != 9)
         {
           incrementADNumber = iADNumber + 1;
           IadNumber = new Integer(incrementADNumber);
           strIncADNumber = IadNumber.toString();
           sNewVersion = strPtBegNumber + "." + strIncADNumber;  //+ strPtEndNumber;
         }
         else //adNumber == 9
         {
           incrementADNumber = 0;
           incrementBDNumber = iBDNumber + 1;
           IbdNumber = new Integer(incrementBDNumber);
           strIncBDNumber = IbdNumber.toString();
           IadNumber = new Integer(incrementADNumber);
           strIncADNumber = IadNumber.toString();
           sNewVersion = strIncBDNumber + "." + strIncADNumber;  //+ strPtEndNumber;
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
         else // before decimal number == 9
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
   * @param req The HttpServletRequest from the client
   * @param DECBeanSR selected DEC bean from search result
   * @param dec DEC_Bean of the changed values.
   *
   * @throws Exception
   */
    public void InsertEditsIntoDECBeanSR(DEC_Bean DECBeanSR, DEC_Bean dec, HttpServletRequest req)
    throws Exception
    {
     // get all attributes of DECBean, if attribute != "" then set that attribute of DECBeanSR
       String sDefinition = dec.getDEC_PREFERRED_DEFINITION();
       if (sDefinition == null) sDefinition = "";
       if (!sDefinition.equals("")) DECBeanSR.setDEC_PREFERRED_DEFINITION(sDefinition);
       String sCD_ID = dec.getDEC_CD_IDSEQ();
       if (sCD_ID == null) sCD_ID = "";
       if (!sCD_ID.equals("")) DECBeanSR.setDEC_CD_IDSEQ(sCD_ID);
       String sCDName = dec.getDEC_CD_NAME();
       if (sCDName == null) sCDName = "";
       if (!sCDName.equals("") && !sCDName.equals(null)) DECBeanSR.setDEC_CD_NAME(sCDName);
       String sBeginDate = dec.getDEC_BEGIN_DATE();
       if (sBeginDate == null) sBeginDate = "";
       if (!sBeginDate.equals("")) DECBeanSR.setDEC_BEGIN_DATE(sBeginDate);
       String sEndDate = dec.getDEC_END_DATE();
       if (sEndDate == null) sEndDate = "";
       if (!sEndDate.equals("")) DECBeanSR.setDEC_END_DATE(sEndDate);
       String sSource = dec.getDEC_SOURCE();
       if (sSource == null) sSource = "";
       if (!sSource.equals("")) DECBeanSR.setDEC_SOURCE(sSource);
       String changeNote = dec.getDEC_CHANGE_NOTE();
       if (changeNote == null) changeNote = "";
       if (!changeNote.equals("")) DECBeanSR.setDEC_CHANGE_NOTE(changeNote);
       //get cs-csi from the page into the DECBean for block edit
       Vector vAC_CS = dec.getDEC_AC_CSI_VECTOR();
       if (vAC_CS != null) DECBeanSR.setDEC_AC_CSI_VECTOR(vAC_CS);
       
    /*   String sOCL = dec.getDEC_OCL_NAME();
       if (sOCL == null) sOCL = "";
       if (!sOCL.equals("")) DECBeanSR.setDEC_OCL_NAME(sOCL);
       
       String sOCCondr = dec.getDEC_OC_CONDR_IDSEQ();
       if (sOCCondr == null) sOCCondr = "";
       if (!sOCCondr.equals("")) DECBeanSR.setDEC_OC_CONDR_IDSEQ(sOCCondr);
 
       String sOCL_IDSEQ = (String)req.getAttribute("OCL_IDSEQ");
       if(sOCL_IDSEQ == null || sOCL_IDSEQ.equals(""))
       {
         String sOCLID = dec.getDEC_OCL_IDSEQ();
         if (sOCLID == null) sOCLID = "";
         if (!sOCLID.equals("")) DECBeanSR.setDEC_OCL_IDSEQ(sOCLID);
       }
       else
         DECBeanSR.setDEC_OCL_IDSEQ(sOCL_IDSEQ);
 
       String sPropL = dec.getDEC_PROPL_NAME();
       if (sPropL == null) sPropL = "";
       if (!sPropL.equals("")) DECBeanSR.setDEC_PROPL_NAME(sPropL);
       
       String sPCCondr = dec.getDEC_PROP_CONDR_IDSEQ();
       if (sPCCondr == null) sPCCondr = "";
       if (!sPCCondr.equals("")) DECBeanSR.setDEC_PROP_CONDR_IDSEQ(sPCCondr);
 
       String sPROPL_IDSEQ = (String)req.getAttribute("PROPL_IDSEQ");
       if(sPROPL_IDSEQ == null || sPROPL_IDSEQ.equals(""))
       {
         String sPropLID = dec.getDEC_PROPL_IDSEQ();
         if (sPropLID == null) sPropLID = "";
         if (!sPropLID.equals("")) DECBeanSR.setDEC_PROPL_IDSEQ(sPropLID);
       }
       else
         DECBeanSR.setDEC_PROPL_IDSEQ(sPROPL_IDSEQ);
 
       String sOCQ = dec.getDEC_OBJ_CLASS_QUALIFIER();
       if (sOCQ == null) sOCQ = "";
       if (!sOCQ.equals("")) DECBeanSR.setDEC_OBJ_CLASS_QUALIFIER(sOCQ);
       String sPropQ = dec.getDEC_PROPERTY_QUALIFIER();
       if (sPropQ == null) sPropQ = "";
       if (!sPropQ.equals("")) DECBeanSR.setDEC_PROPERTY_QUALIFIER(sPropQ);  */
 
       String status = dec.getDEC_ASL_NAME();
       if (status == null) status = "";
       if (!status.equals("")) DECBeanSR.setDEC_ASL_NAME(status);
 
       String version = dec.getDEC_VERSION();
       String lastVersion = (String)DECBeanSR.getDEC_VERSION();
       int index = -1;
       String pointStr = ".";
       String strWhBegNumber = "";
       int iWhBegNumber = 0;
       index = lastVersion.indexOf(pointStr);
       String strPtBegNumber = lastVersion.substring(0, index);
       String afterDecimalNumber = lastVersion.substring((index + 1 ), (index + 2));
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
         int iBDNumber = bdNumber.intValue();  //before decimal
         if (iADNumber != 9)
         {
           incrementADNumber = iADNumber + 1;
           IadNumber = new Integer(incrementADNumber);
           strIncADNumber = IadNumber.toString();
           sNewVersion = strPtBegNumber + "." + strIncADNumber;  //+ strPtEndNumber;
         }
         else //adNumber == 9
         {
           incrementADNumber = 0;
           incrementBDNumber = iBDNumber + 1;
           IbdNumber = new Integer(incrementBDNumber);
           strIncBDNumber = IbdNumber.toString();
           IadNumber = new Integer(incrementADNumber);
           strIncADNumber = IadNumber.toString();
           sNewVersion = strIncBDNumber + "." + strIncADNumber;  //+ strPtEndNumber;
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
         else // before decimal number == 9
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
   * @param DEBeanSR selected DE bean from search result
   * @param de DE_Bean of the changed values.
   *
   * @throws Exception
   */
    public void InsertEditsIntoDEBeanSR(DE_Bean DEBeanSR, DE_Bean de, 
       HttpServletRequest req, HttpServletResponse res)  throws Exception
    {
     // get all attributes of DEBean, if attribute != "" then set that attribute of DEBeanSR
       String sDefinition = de.getDE_PREFERRED_DEFINITION();
       if (sDefinition == null) sDefinition = "";
       if (!sDefinition.equals("")) DEBeanSR.setDE_PREFERRED_DEFINITION(sDefinition);
       String sDEC_ID = de.getDE_DEC_IDSEQ();
       if (sDEC_ID == null) sDEC_ID = "";
       if (!sDEC_ID.equals("")) DEBeanSR.setDE_DEC_IDSEQ(sDEC_ID);
       String sVD_ID = de.getDE_VD_IDSEQ();
       if (sVD_ID == null) sVD_ID = "";
       if (!sVD_ID.equals("")) DEBeanSR.setDE_VD_IDSEQ(sVD_ID);
       String sDEC = de.getDE_DEC_NAME();
       if (sDEC == null) sDEC = "";
       if (!sDEC.equals("")) DEBeanSR.setDE_DEC_NAME(sDEC);
       String sVD = de.getDE_VD_NAME();
       if (sVD == null) sVD = "";
       if (!sVD.equals("")) DEBeanSR.setDE_VD_NAME(sVD);
       String sDocText = de.getDOC_TEXT_LONG_NAME();
       if (sDocText == null) sDocText = "";
       if (!sDocText.equals("")) DEBeanSR.setDOC_TEXT_LONG_NAME(sDocText);
       String sBeginDate = de.getDE_BEGIN_DATE();
       if (sBeginDate == null) sBeginDate = "";
       if (!sBeginDate.equals("")) DEBeanSR.setDE_BEGIN_DATE(sBeginDate);
       String sEndDate = de.getDE_END_DATE();
       if (sEndDate == null) sEndDate = "";
       if (!sEndDate.equals("")) DEBeanSR.setDE_END_DATE(sEndDate);
       String sSource = de.getDE_SOURCE();
       if (sSource == null) sSource = "";
       if (!sSource.equals("")) DEBeanSR.setDE_SOURCE(sSource);
       String status = de.getDE_ASL_NAME();
       if (status == null) status = "";
       if (!status.equals("")) DEBeanSR.setDE_ASL_NAME(status);
       String RegStatus = de.getDE_REG_STATUS();
       if (RegStatus == null) RegStatus = "";
       if (!RegStatus.equals("")) DEBeanSR.setDE_REG_STATUS(RegStatus);  
       String changeNote = de.getDE_CHANGE_NOTE();
       if (changeNote == null) changeNote = "";
       if (!changeNote.equals("")) DEBeanSR.setDE_CHANGE_NOTE(changeNote);
 
       //get cs-csi from the page into the DECBean for block edit
       Vector vAC_CS = de.getDE_AC_CSI_VECTOR();
       if (vAC_CS != null) DEBeanSR.setDE_AC_CSI_VECTOR(vAC_CS);
       
       String version = de.getDE_VERSION();
       String lastVersion = (String)DEBeanSR.getDE_VERSION();
       int index = -1;
       String pointStr = ".";
       String strWhBegNumber = "";
       int iWhBegNumber = 0;
       index = lastVersion.indexOf(pointStr);
       String strPtBegNumber = lastVersion.substring(0, index);
       String afterDecimalNumber = lastVersion.substring((index + 1 ), (index + 2));
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
         int iBDNumber = bdNumber.intValue();  //before decimal
         if (iADNumber != 9)
         {
           incrementADNumber = iADNumber + 1;
           IadNumber = new Integer(incrementADNumber);
           strIncADNumber = IadNumber.toString();
           sNewVersion = strPtBegNumber + "." + strIncADNumber;  //+ strPtEndNumber;
         }
         else //adNumber == 9
         {
           incrementADNumber = 0;
           incrementBDNumber = iBDNumber + 1;
           IbdNumber = new Integer(incrementBDNumber);
           strIncBDNumber = IbdNumber.toString();
           IadNumber = new Integer(incrementADNumber);
           strIncADNumber = IadNumber.toString();
           sNewVersion = strIncBDNumber + "." + strIncADNumber;  //+ strPtEndNumber;
         }
         DEBeanSR.setDE_VERSION(sNewVersion);
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
         else // before decimal number == 9
         {
           int incrementWhBegNumber = iWhBegNumber + 1;
           Integer IWhBegNumber = new Integer(incrementWhBegNumber);
           String strIncWhBegNumber = IWhBegNumber.toString();
           IbdNumber = new Integer(0);
           strIncBDNumber = IbdNumber.toString();
           sNewVersion = strIncWhBegNumber + strIncBDNumber + strWhEndNumber;
         }
         DEBeanSR.setDE_VERSION(sNewVersion);
       }
 }
 
   /**
   * update record in the database and display the result.
   * Called from 'doInsertDEC' method when the aciton is editing.
   * Retrieves the session bean m_DEC.
   * calls 'insAC.setDEC' to update the database.
   * otherwise calls 'serAC.refreshData' to get the refreshed search result
   * forwards the page back to search page with refreshed list after updating.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'EditDECPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doUpdateDECActionBE(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
       session.setAttribute("DECEditAction", ""); //reset this
       boolean isRefreshed = false;
       String ret = ":";
       InsACService insAC = new InsACService(req, res, this);
       GetACSearch serAC = new GetACSearch(req, res, this);
       GetACService getAC = new GetACService(req, res, this);
       //String sNewOC = (String)session.getAttribute("newObjectClass");
       //String sNewProp = (String)session.getAttribute("newProperty");
       Vector vBERows = (Vector)session.getAttribute("vBEResult");
       int vBESize = vBERows.size();
       Integer vBESize2 = new Integer(vBESize);
       req.setAttribute("vBESize", vBESize2);
       String sOC_IDSEQ = "";
       String sProp_IDSEQ = "";
       if (vBERows.size()>0)
       {
         for (int i=0; i<(vBERows.size()); i++)
         {
           DEC_Bean DECBeanSR = new DEC_Bean();
           DECBeanSR = (DEC_Bean)vBERows.elementAt(i);
           DEC_Bean oldDECBean = new DEC_Bean();
           oldDECBean = oldDECBean.cloneDEC_Bean(DECBeanSR);
           String oldName = (String)DECBeanSR.getDEC_PREFERRED_NAME();
           //gets version from page
           String newVersion = (String)DECBean.getDEC_VERSION();
           if (newVersion == null) newVersion = "";
           //gets all the changed attrributes from the page
          /* if(i ==0)
           {
             if(sNewOC.equals("true") || sNewProp.equals("true"))
               doInsertDECBlocks(req, res, DECBeanSR);
             sOC_IDSEQ = DECBeanSR.getDEC_OCL_IDSEQ();
             if(sOC_IDSEQ == null) sOC_IDSEQ = "";
             sProp_IDSEQ = DECBeanSR.getDEC_PROPL_IDSEQ();
             if(sProp_IDSEQ == null) sProp_IDSEQ = "";
             DECBean.setDEC_OCL_IDSEQ(sOC_IDSEQ);
             DECBean.setDEC_OCL_IDSEQ(sProp_IDSEQ);
             DECBean.setDEC_OBJ_CLASS_QUALIFIER("");
             DECBean.setDEC_PROPERTY_QUALIFIER("");
             
             String sOC_Condr = DECBeanSR.getDEC_OC_CONDR_IDSEQ();
             if(sOC_Condr == null) sOC_Condr = "";
             DECBean.setDEC_OC_CONDR_IDSEQ(sOC_Condr);
             
             String sPC_Condr = DECBeanSR.getDEC_PROP_CONDR_IDSEQ();
             if(sPC_Condr == null) sPC_Condr = "";
             DECBean.setDEC_PROP_CONDR_IDSEQ(sPC_Condr);
           } */
           InsertEditsIntoDECBeanSR(DECBeanSR, DECBean, req);
          // session.setAttribute("m_DEC", DECBeanSR);
           String oldID = oldDECBean.getDEC_DEC_IDSEQ();
           //udpate the status message with DEC name and ID
           insAC.storeStatusMsg("Data Element Concept Name : " + DECBeanSR.getDEC_LONG_NAME());
           insAC.storeStatusMsg("Public ID : " + DECBeanSR.getDEC_DEC_ID());
           //creates a new version
           if (newVersion.equals("Point") || newVersion.equals("Whole"))  // block version
           {
              //creates new version first and updates all other attributes
              String strValid = m_setAC.checkUniqueInContext("Version", "DEC", null, DECBeanSR, null, getAC, "version");
              if (strValid != null && !strValid.equals(""))
                 ret = "unique constraint";
              else
                 ret = insAC.setAC_VERSION(null, DECBeanSR, null, "DataElementConcept");
              if (ret == null || ret.equals(""))
              {
                 ret = insAC.setDEC("UPD", DECBeanSR, "Version", oldDECBean);
                // resetEVSBeans(req, res);
                 //add this bean into the session vector
                 if (ret == null || ret.equals(""))
                 {
                    serAC.refreshData(req, res, null, DECBeanSR, null, null, "Version", oldID);
                    isRefreshed = true;
                    //reset the appened attributes to remove all the checking of the row
                    Vector vCheck = new Vector();
                    session.setAttribute("CheckList", vCheck);
                    session.setAttribute("AppendAction", "Not Appended");
                 }
              }
              //alerady exists
              else if (ret.indexOf("unique constraint") >= 0)
                 insAC.storeStatusMsg("\\t The version " + DECBeanSR.getDEC_VERSION() + " already exists in the data base.\\n");
              //some other problem
              else
                 insAC.storeStatusMsg("\\t " + ret + " : Unable to create new version " + DECBeanSR.getDEC_VERSION() + ".\\n");
           }
           else  // block edit
           {
             ret = insAC.setDEC("UPD", DECBeanSR, "BlockEdit", oldDECBean);
             //forward to search page with refreshed list after successful update
             if ((ret == null) || ret.equals(""))
             {
                serAC.refreshData(req, res, null, DECBeanSR, null,  null, "Edit", oldID);
                isRefreshed = true;
             }
           }
         }
       }
       //to get the final result vector if not refreshed at all
       if (!(isRefreshed))
       {
          Vector vResult = new Vector();
          serAC.getDECResult(req, res, vResult, "");
          session.setAttribute("results", vResult);    //store the final result in the session
          session.setAttribute("DECPageAction", "nothing");
       }
 
       //forward to search page.
       ForwardJSP(req, res, "/SearchResultsPage.jsp");
    }
 
    /**
   * update record in the database and display the result.
   * Called from 'doInsertDEC' method when the aciton is editing.
   * Retrieves the session bean m_DEC.
   * calls 'insAC.setDE' to update the database.
   * otherwise calls 'serAC.refreshData' to get the refreshed search result
   * forwards the page back to search page with refreshed list after updating.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'EditDEPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doUpdateDEActionBE(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
       String ret = ":";
       session.setAttribute("DEEditAction", ""); //reset this
       boolean isRefreshed = false;
 
       InsACService insAC = new InsACService(req, res, this);
       GetACSearch serAC = new GetACSearch(req, res, this);
       GetACService getAC = new GetACService(req, res, this);
 
       Vector vBERows = (Vector)session.getAttribute("vBEResult");
       int vBESize = vBERows.size();
       Integer vBESize2 = new Integer(vBESize);
       req.setAttribute("vBESize", vBESize2);
       if (vBERows.size()>0)
       {
         for(int i=0; i<(vBERows.size()); i++)
         {
           DE_Bean DEBeanSR = new DE_Bean();
           DEBeanSR = (DE_Bean)vBERows.elementAt(i);
           //udpate the status message with DE name and ID
           insAC.storeStatusMsg("Data Element Name : " + DEBeanSR.getDE_LONG_NAME());
           insAC.storeStatusMsg("Public ID : " + DEBeanSR.getDE_MIN_CDE_ID());
           DE_Bean oldDEBean = new DE_Bean();
           oldDEBean = oldDEBean.cloneDE_Bean(DEBeanSR, "Complete");
           String oldName = (String)DEBeanSR.getDE_PREFERRED_NAME();
             //gets the version from the page
           String newVersion = (String)DEBean.getDE_VERSION();
           if (newVersion == null) newVersion = "";
           //gets all the data from the page
           InsertEditsIntoDEBeanSR(DEBeanSR, DEBean, req, res);
           session.setAttribute("m_DE", DEBeanSR);
           String oldID = oldDEBean.getDE_DE_IDSEQ();
           //creates a new version
           if (newVersion.equals("Point") || newVersion.equals("Whole"))  // block version
           {
              //insert a new row with new version
              String strValid = m_setAC.checkUniqueInContext("Version", "DE", DEBeanSR, null, null, getAC, "version");
              if (strValid != null && !strValid.equals(""))
                 ret = "unique constraint";
              else
                 ret = insAC.setAC_VERSION(DEBeanSR, null, null, "DataElement");
              if ((ret == null) || ret.equals(""))
              {
                 //update this new row with changed attributes
                 ret = insAC.setDE("UPD", DEBeanSR, "Version", oldDEBean);
                 if ((ret == null) || ret.equals(""))
                 {
                    //save the status message and retain the this row in the vector
                    serAC.refreshData(req, res, DEBeanSR, null, null,  null, "Version", oldID);
                    isRefreshed = true;
                    //reset the appened attributes to remove all the checking of the row
                    Vector vCheck = new Vector();
                    session.setAttribute("CheckList", vCheck);
                    session.setAttribute("AppendAction", "Not Appended");
                 }
              }
              //alerady exists
              else if (ret.indexOf("unique constraint") >= 0)
                 insAC.storeStatusMsg("\\t The new version " + DEBeanSR.getDE_VERSION() + " already exists in the data base.\\n");
              //some other problem
              else
                 insAC.storeStatusMsg("\\t " + ret + " : Unable to create new version " + DEBeanSR.getDE_VERSION() + "\\n");
           }
           else  // block edit
           {    
               ret = insAC.setDE("UPD", DEBeanSR, "Edit", oldDEBean);
               if ((ret == null) || ret.equals(""))
               {
                 serAC.refreshData(req, res, DEBeanSR, null, null,  null, "Edit", oldID);
                 isRefreshed = true;
               }
           }
         }
       }
       //to get the final result vector if not refreshed at all
       if (!(isRefreshed))
       {
          Vector vResult = new Vector();
          serAC.getDEResult(req, res, vResult, "");
          session.setAttribute("results", vResult);    //store the final result in the session
          session.setAttribute("DEPageAction", "nothing");
       }
       //forward to search page.
       ForwardJSP(req, res, "/SearchResultsPage.jsp");
    }
 
   /**
   * creates new record in the database and display the result.
   * Called from 'doInsertVD' method when the aciton is create new VD from DEPage.
   * Retrieves the session bean m_VD.
   * calls 'insAC.setVD' to update the database.
   * forwards the page back to create DE page after successful insert.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'createVDPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param sOrigin string value from where vd creation action was originated.
   *
   * @throws Exception
   */
    public void doInsertVDfromDEAction(HttpServletRequest req, HttpServletResponse res, String sOrigin)
    throws Exception
    {
       HttpSession session = req.getSession();
       VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
 
       InsACService insAC = new InsACService(req, res, this);
       GetACSearch serAC = new GetACSearch(req, res, this);
       String sMenu = (String)session.getAttribute("MenuAction");
       //insert the building blocks attriubtes before inserting vd
       doInsertVDBlocks(req, res, null);
       String ret = insAC.setVD("INS", VDBean, "New", null);
       //updates the de bean with new vd data after successful insert and forwards to create page
       if ((ret == null) || ret.equals(""))
       {
          DE_Bean DEBean  = (DE_Bean)session.getAttribute("m_DE");
          DEBean.setDE_VD_NAME(VDBean.getVD_LONG_NAME());
          DEBean.setDE_VD_IDSEQ(VDBean.getVD_VD_IDSEQ());
          //add DEC Bean into DE BEan
          DEBean.setDE_VD_Bean(VDBean);
          session.setAttribute("m_DE", DEBean);
          DEBean = this.doGetDENames(req, res, "new", "newVD", DEBean);
 
          this.clearCreateSessionAttributes(req, res);  //clear some session attributes
          if (sOrigin != null && sOrigin.equals("CreateNewVDfromEditDE"))
             ForwardJSP(req, res, "/EditDEPage.jsp");
          else
             ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       //goes back to create vd page if error
       else
       {
           session.setAttribute("VDPageAction", "validate");
           ForwardJSP(req, res, "/CreateVDPage.jsp");   //send it back to vd page
       }
    }
 
 
   /**
   * to create object class, property, rep term and qualifier value from EVS into cadsr.
   * Retrieves the session bean m_VD.
   * calls 'insAC.setDECQualifier' to insert the database.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param  VD_Bean dec attribute bean.
   *
   * @throws Exception
    */
    public void doInsertVDBlocks(HttpServletRequest req, HttpServletResponse res, VD_Bean VDBeanSR)
    throws Exception
    {
       HttpSession session = req.getSession();
       if (VDBeanSR == null) VDBeanSR = (VD_Bean)session.getAttribute("m_VD");
       String sRemoveRepBlock = (String)session.getAttribute("RemoveRepBlock");
       if(sRemoveRepBlock == null) sRemoveRepBlock = "";
       EVS_Bean REPBean = (EVS_Bean)session.getAttribute("m_REP");
       EVS_Bean REPQBean = (EVS_Bean)session.getAttribute("m_REPQ");
       String sNewRep = (String)session.getAttribute("newRepTerm");
       if(sNewRep == null) sNewRep = "";
 
       String sREP_IDSEQ = "";
       String retObj = "";
       String retProp = "";
       String retRep = "";
       String retObjQual = "";
       String retPropQual = "";
       String retRepQual = "";
       InsACService insAC = new InsACService(req, res, this);
     
       if (sNewRep.equals("true"))
           retRepQual = insAC.setRepresentation("INS", sREP_IDSEQ, VDBeanSR, REPQBean, req);
       else if(sRemoveRepBlock.equals("true"))
         retObj = insAC.setRepresentation("INS" , sREP_IDSEQ, VDBeanSR, REPBean, req);
       
     session.setAttribute("newRepTerm", "");
   }
 
   /**
   * creates new record in the database and display the result.
   * Called from 'doInsertVD' method when the aciton is create new VD from Menu.
   * Retrieves the session bean m_VD.
   * calls 'insAC.setVD' to update the database.
   * calls 'serAC.refreshData' to get the refreshed search result for template/version
   * forwards the page back to create VD page if new VD or back to search page
   * if template or version after successful insert.
   *
   * If ret is not null stores the statusMessage as error message in session and
   * forwards the page back to 'createVDPage.jsp' for Edit.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doInsertVDfromMenuAction(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
       HttpSession session = req.getSession();
       VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
 
       InsACService insAC = new InsACService(req, res, this);
       GetACSearch serAC = new GetACSearch(req, res, this);
       String sMenuAction = (String)session.getAttribute("MenuAction");
       VD_Bean oldVDBean = (VD_Bean)session.getAttribute("oldVDBean");
       String ret = "";
       boolean isUpdateSuccess = true; 
       doInsertVDBlocks(req, res, null);
       if (sMenuAction.equals("NewVDVersion"))
       {
          //udpate the status message with DE name and ID
          insAC.storeStatusMsg("Value Domain Name : " + VDBean.getVD_LONG_NAME());
          insAC.storeStatusMsg("Public ID : " + VDBean.getVD_VD_ID());
          //creates new version first
          ret = insAC.setAC_VERSION(null, null, VDBean, "ValueDomain");
          if (ret == null || ret.equals(""))
          {
             //get pvs related to this new VD, it was created in VD_Version
             serAC.doPVACSearch(VDBean.getVD_VD_IDSEQ(), VDBean.getVD_LONG_NAME(), "Version");
             //update non evs changes
             Vector vParent = (Vector)session.getAttribute("VDParentConcept");
             if (vParent != null && vParent.size() > 0)
               vParent = serAC.getNonEVSParent(vParent, VDBean, "versionSubmit");
             //get the right system name for new version
             String prefName = VDBean.getVD_PREFERRED_NAME();
             if (prefName == null || prefName.equalsIgnoreCase("(Generated by the System)"))
             {
               VDBean = this.doGetVDSystemName(req, VDBean, vParent);
               VDBean.setVD_PREFERRED_NAME(VDBean.getAC_SYS_PREF_NAME());
             }
             //  and updates all other attributes
             ret = insAC.setVD("UPD", VDBean, "Version", oldVDBean);
             //resetEVSBeans(req, res);
             if (ret != null && !ret.equals(""))
             {
                //add newly created row to searchresults and send it to edit page for update
                isUpdateSuccess = false;
                String oldID = oldVDBean.getVD_VD_IDSEQ();
                String newID = VDBean.getVD_VD_IDSEQ();
                String newVersion = VDBean.getVD_VERSION();
                VDBean = VDBean.cloneVD_Bean(oldVDBean);
                VDBean.setVD_VD_IDSEQ(newID);
                VDBean.setVD_VERSION(newVersion);
                VDBean.setVD_ASL_NAME("DRAFT MOD");
                //refresh the result list by inserting newly created VD
                serAC.refreshData(req, res, null, null, VDBean, null, "Version", oldID);
             }
          }
          else
             insAC.storeStatusMsg("\t " + ret + " - Unable to create new version successfully.");
       }
       else
       {    
          //parent appended system name as default for new vd
          String prefName = VDBean.getVD_PREFERRED_NAME();
          if ((prefName == null || prefName.equalsIgnoreCase("(Generated by the System)")) && VDBean.getAC_SYS_PREF_NAME() != null)
             VDBean.setVD_PREFERRED_NAME(VDBean.getAC_SYS_PREF_NAME());
          //creates new one      
          ret = insAC.setVD("INS", VDBean, "New", oldVDBean);  //create new one
          //get the system generated with public id.
          if (prefName == null || prefName.equalsIgnoreCase("(Generated by the System)"))
          {
             Vector vParent = (Vector)session.getAttribute("VDParentConcept");
             VDBean = this.doGetVDSystemName(req, VDBean, vParent);
             VDBean.setVD_PREFERRED_NAME(VDBean.getAC_SYS_PREF_NAME());
             ret = insAC.setVD("UPD", VDBean, "Update", oldVDBean); //update the preferred name
          }            
       }
 
       if ((ret == null) || ret.equals(""))
       {
          this.clearCreateSessionAttributes(req, res);  //clear some session attributes
          
          session.setAttribute("VDPageAction", "nothing");
          session.setAttribute("originAction", "");
          //forwards to search page with refreshed list if template or version
          if ((sMenuAction.equals("NewVDTemplate")) || (sMenuAction.equals("NewVDVersion")))
          {
             session.setAttribute("searchAC", "ValueDomain");
             session.setAttribute("originAction", "NewVDTemplate");
             VDBean.setVD_ALIAS_NAME(VDBean.getVD_PREFERRED_NAME());
             VDBean.setVD_TYPE_NAME("PRIMARY");
             String oldID = oldVDBean.getVD_VD_IDSEQ();
 
             if (sMenuAction.equals("NewVDTemplate"))
                serAC.refreshData(req, res, null, null, VDBean, null, "Template", oldID);
             else if (sMenuAction.equals("NewVDVersion"))
                serAC.refreshData(req, res, null, null, VDBean, null, "Version", oldID);
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
          }
          //forward to create vd page with empty data if new one
          else
          {
              VDBean = new VD_Bean();
              VDBean.setVD_ASL_NAME("DRAFT NEW");
              VDBean.setAC_PREF_NAME_TYPE("SYS");
              session.setAttribute("m_VD", VDBean);
              EVS_Bean m_OC = new EVS_Bean();
              session.setAttribute("m_OC", m_OC);
              EVS_Bean m_PC = new EVS_Bean();
              session.setAttribute("m_PC", m_PC);
              EVS_Bean m_Rep = new EVS_Bean();
              session.setAttribute("m_Rep", m_Rep);
              EVS_Bean m_OCQ = new EVS_Bean();
              session.setAttribute("m_OCQ", m_OCQ);
              EVS_Bean m_PCQ = new EVS_Bean();
              session.setAttribute("m_PCQ", m_PCQ);
              EVS_Bean m_REPQ = new EVS_Bean();
              session.setAttribute("m_REPQ", m_REPQ);
              session.setAttribute("m_PCQ", m_PCQ);
              session.setAttribute("selPropRow", "");
              session.setAttribute("selPropQRow", "");
              session.setAttribute("selObjQRow", "");
              session.setAttribute("selObjRow", "");
              session.setAttribute("selRepQRow", "");
              session.setAttribute("selRepRow", "");
              ForwardJSP(req, res, "/CreateVDPage.jsp");
          }
       }
       //goes back to create/edit vd page if error
       else
       {
           session.setAttribute("VDPageAction", "validate");
           //forward to create or edit pages
           if (isUpdateSuccess == false)
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
           else
             ForwardJSP(req, res, "/CreateVDPage.jsp");
       }
    }
 
   /**
   * The doSuggestionDE method forwards to EVSSearch jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doSuggestionDE(HttpServletRequest req,
           HttpServletResponse res) throws Exception
    {
       ForwardJSP(req, res, "/EVSSearch.jsp");
    }
 
   /**
   * The doOpenCreatePVPage method gets the session, gets some values from the createVD
   * page and stores in bean m_VD, sets some session attributes, then forwards
   * to CreatePV page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doOpenCreatePVPage(HttpServletRequest req, HttpServletResponse res, 
       String sPVAction, String vdPage)  throws Exception
    {
        HttpSession session = req.getSession();
        session.setAttribute("PVAction", sPVAction);
        VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
        m_setAC.setVDValueFromPage(req, res, m_VD);   //store VD bean
        session.setAttribute("VDPageAction", "validate");
        session.setAttribute("m_VD", m_VD);
        //call the method to add/remove pvs.
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
           //store the old pv in another session
           PV_Bean pvBean = (PV_Bean)session.getAttribute("m_PV");
           if (pvBean == null) pvBean = new PV_Bean();
           //copy the pv session attributes to store that can be used for clear button
           PV_Bean pageBean = new PV_Bean();
           pageBean = pageBean.copyBean(pvBean);
           session.setAttribute("pageOpenBean", pageBean);
           ForwardJSP(req, res, "/CreatePVPage.jsp");
        }
   }
 
   /**
   * The doJspErrorAction method is called when there is an error on a jsp page.
   * User is forwarded to SearchResultsPage
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doJspErrorAction(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
        doOpenSearchPage(req, res);
    }
 
    /**
   * The doSearchPV method gets the session, gets some values from the createVD
   * page and stores in bean m_VD, sets some session attributes, then forwards
   * to CreatePV page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doSearchPV(HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
        HttpSession session = req.getSession();
        VD_Bean m_VD = (VD_Bean)session.getAttribute("m_VD");  //new VD_Bean();
        EVS_Bean m_OC = new EVS_Bean();
        EVS_Bean m_PC = new EVS_Bean();
        EVS_Bean m_REP = new EVS_Bean();
        EVS_Bean m_OCQ = new EVS_Bean();
        EVS_Bean m_PCQ = new EVS_Bean();
        EVS_Bean m_REPQ = new EVS_Bean();
        m_setAC.setVDValueFromPage(req, res, m_VD);   //store VD bean
        session.setAttribute("VDPageAction", "searchValues");
        session.setAttribute("m_VD", m_VD);
        session.setAttribute("m_OC", m_OC);
        session.setAttribute("m_PC", m_PC);
        session.setAttribute("m_REP", m_REP);
        session.setAttribute("m_OCQ", m_OCQ);
        session.setAttribute("m_PCQ", m_PCQ);
        session.setAttribute("m_REPQ", m_REPQ);
        session.setAttribute("PValue", "");
        ForwardJSP(req, res, "/SearchResultsPage.jsp");
   }
 
    /**
   * The doOpenCreateVMPage method gets the session, gets some values from the createVD
   * page and stores in bean m_VD, sets some session attributes, then forwards
   * to CreateVM page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doOpenCreateVMPage(HttpServletRequest req, HttpServletResponse res, String origin)
    throws Exception 
    {
       HttpSession session = req.getSession();
       String sOrigin = (String)session.getAttribute("originAction");
 
       PV_Bean m_PV = new PV_Bean();
       SetACService setAC = new SetACService(this);
       setAC.setPVValueFromPage(req, res, m_PV);
       session.setAttribute("m_PV", m_PV);
       //reset vm bean
       VM_Bean VMBean = new VM_Bean();
       //get cd value from the VD bean and make it default for VM
       VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
       VMBean.setVM_CD_NAME(VDBean.getVD_CD_NAME());
       VMBean.setVM_CD_IDSEQ(VDBean.getVD_CD_IDSEQ());
       //store the bean in the session
       session.setAttribute("m_VM", VMBean);
       session.setAttribute("creSearchAC", "EVSValueMeaning");      
 
       if (sOrigin.equals("CreateInSearch"))
          ForwardJSP(req, res, "/CreateVMSearchPage.jsp");
       else
          ForwardJSP(req, res, "/CreateVMPage.jsp");
   }
 
   /**
   * The doOpenCreateDECPage method gets the session, gets some values from the createDE
   * page and stores in bean m_DE, sets some session attributes, then forwards
   * to CreateDEC page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doOpenCreateDECPage(HttpServletRequest req, HttpServletResponse res)
    throws Exception
   {
        HttpSession session = req.getSession();
       // session.setAttribute("originAction", fromWhere);  //"CreateNewDECfromCreateDE");
       DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
       if (m_DE == null) m_DE = new DE_Bean();
        m_setAC.setDEValueFromPage(req, res, m_DE);   //store DEC bean
        session.setAttribute("m_DE", m_DE);
 
        DEC_Bean m_DEC = new DEC_Bean();
        m_DEC.setDEC_ASL_NAME("DRAFT NEW");
        m_DEC.setAC_PREF_NAME_TYPE("SYS");
        session.setAttribute("m_DEC", m_DEC);
        DEC_Bean oldDEC = new DEC_Bean();
        oldDEC = oldDEC.cloneDEC_Bean(m_DEC);
        session.setAttribute("oldDECBean", oldDEC);
       // session.setAttribute("oldDECBean", m_DEC);
        ForwardJSP(req, res, "/CreateDECPage.jsp");
   }
 
   /**
   * The doOpenCreateDECompPage method get current primary DE bean, seve it to session as old DE Bean,
   * then forward to CreateDE page for DE Comp. 
   * It is called from doCreateDEActions and doEditDEActions
   * 
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doOpenCreateDECompPage(HttpServletRequest req, HttpServletResponse res)
    throws Exception
   {
        HttpSession session = req.getSession();
        session.setAttribute("originAction", "CreateNewDEFComp");
        DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
        if (m_DE == null) m_DE = new DE_Bean();
        m_setAC.setDEValueFromPage(req, res, m_DE);   //store DE bean
        session.setAttribute("oldDEBean", m_DE);    // save primary DE to oldDEBean
        // clear DEBean because new DE Comp
        DE_Bean de = new DE_Bean();
        de.setDE_ASL_NAME("DRAFT NEW");
        de.setAC_PREF_NAME_TYPE("SYS");
        session.setAttribute("m_DE", de);
        ForwardJSP(req, res, "/CreateDEPage.jsp");
   }
 
   /**
   * The doOpenDEPageFromDEComp method set primary DE from old DE Bean back to current DE Bean,
   * then forward to CreateDE page or EditDE page. 
   * It is called from doCreateDEActions
   * 
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doOpenDEPageFromDEComp(HttpServletRequest req, HttpServletResponse res)
    throws Exception
   {
       HttpSession session = req.getSession();
       String sPageAction = (String)req.getParameter("newCDEPageAction");
       //set primary DE back         
       DE_Bean oldDEBean = new DE_Bean();
       oldDEBean = (DE_Bean)session.getAttribute("oldDEBean");
       session.setAttribute("m_DE", oldDEBean);
 
       if(sPageAction.equals("DECompBackToNewDE"))
       {
          session.setAttribute("originAction", "NewDEFromMenu");
          ForwardJSP(req, res, "/CreateDEPage.jsp");
       }
       else if(sPageAction.equals("DECompBackToEditDE"))
       {
          session.setAttribute("originAction", "EditDE");
          ForwardJSP(req, res, "/EditDEPage.jsp");
       }
   }
 
   /**
   * The doUpdateDDEInfo get DDE info from jsp page hidden fields and save them to session
   * It is called from doCreateDEActions and doEditDEActions
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doUpdateDDEInfo(HttpServletRequest req, HttpServletResponse res)
    throws Exception
   {
         HttpSession session = req.getSession();
         // Create DDE from CreateDE page, save existed DEComp to session first, then goto CreateDE page for DDE
         //get exist vDEComp vectors from jsp
         String sDEComps[] = req.getParameterValues("selDECompHidden");
         String sDECompIDs[] = req.getParameterValues("selDECompIDHidden");
         String sDECompOrders[] = req.getParameterValues("selDECompOrderHidden");
         String sDECompRelIDs[] = req.getParameterValues("selDECompRelIDHidden");
         Vector vDEComp = new Vector();
         Vector vDECompID = new Vector();
         Vector vDECompOrder = new Vector();
         Vector vDECompRelID = new Vector();
         if(sDEComps != null && sDECompIDs != null)
         {
           for (int i = 0; i<sDEComps.length; i++) 
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
           //sort vDEComp against DECompOrder
           UtilService util = new UtilService();
           util.sortDEComps(vDEComp, vDECompID, vDECompRelID, vDECompOrder);
         }
         // save it, even empty, refresh
         session.setAttribute("vDEComp", vDEComp);
         session.setAttribute("vDECompID", vDECompID);
         session.setAttribute("vDECompOrder", vDECompOrder);
         session.setAttribute("vDECompRelID", vDECompRelID);
         // DEComp removed list
         String sDECompDeletes[] = req.getParameterValues("selDECompDeleteHidden");
         String sDECompDelNames[] = req.getParameterValues("selDECompDelNameHidden");
         Vector vDECompDelete = new Vector();
         Vector vDECompDelName = new Vector();
         if(sDECompDeletes != null)
         {
           for (int i = 0; i<sDECompDeletes.length; i++) 
           {
               String sDECompDelete = sDECompDeletes[i];
               String sDECompDelName = sDECompDelNames[i];
               vDECompDelete.addElement(sDECompDelete);
               vDECompDelName.addElement(sDECompDelName);
           }
         }
         // save it to session
         session.setAttribute("vDECompDelete", vDECompDelete);
         session.setAttribute("vDECompDelName", vDECompDelName);
         // DDE rules
         String sDDERepTypes[] = req.getParameterValues("selRepType");
         String sRepType = sDDERepTypes[0];
         String sRule = (String)req.getParameter("DDERule");
         String sMethod = (String)req.getParameter("DDEMethod");
         String sConcatChar = (String)req.getParameter("DDEConcatChar");
 
         if(sRepType != null)
             session.setAttribute("sRepType", sRepType);
         else
             session.setAttribute("sRepType", "");
         if(sConcatChar != null)
             session.setAttribute("sConcatChar", sConcatChar);
         else
             session.setAttribute("sConcatChar", "");
         if(sRule != null)
             session.setAttribute("sRule", sRule);
         else
             session.setAttribute("sRule", "");
         if(sMethod != null)
             session.setAttribute("sMethod", sMethod);
         else
             session.setAttribute("sMethod", "");
   }
 
   /**
   * The doInitDDEInfo set DDE data to session
   * It is called from doOpenCreateNewPages and doOpenSearchPage
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doInitDDEInfo(HttpServletRequest req, HttpServletResponse res)
    throws Exception
   {
       HttpSession session = req.getSession();
       Vector vDEComp = new Vector();
       Vector vDECompID = new Vector();
       Vector vDECompOrder = new Vector();
       session.setAttribute("vDEComp", vDEComp);
       session.setAttribute("vDECompID", vDECompID);
       session.setAttribute("vDECompOrder", vDECompOrder);
       session.setAttribute("sRepType", "");
       session.setAttribute("sConcatChar", "");
       session.setAttribute("sRule", "");
       session.setAttribute("sMethod", "");
       session.setAttribute("sRulesAction", "newRule");
       //init rep type drop list
       GetACSearch serAC = new GetACSearch(req, res, this);
       serAC.getComplexRepType();
   }  // end of doInitDDEInfo          
 
    /**
   * The doOpenCreateVDPage method gets the session, gets some values from the createDE
   * page and stores in bean m_DE, sets some session attributes, then forwards
   * to CreateVD page
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
    public void doOpenCreateVDPage(HttpServletRequest req, HttpServletResponse res)
    throws Exception
   {
        HttpSession session = req.getSession();
        DE_Bean m_DE = (DE_Bean)session.getAttribute("m_DE");
        if (m_DE == null) m_DE = new DE_Bean();
        m_setAC.setDEValueFromPage(req, res, m_DE);   //store VD bean
        session.setAttribute("m_DE", m_DE);
 
        VD_Bean m_VD = new VD_Bean();
        m_VD.setVD_ASL_NAME("DRAFT NEW");
        m_VD.setAC_PREF_NAME_TYPE("SYS");
       //call the method to get the QuestValues if exists
        String sMenuAction = (String)session.getAttribute("MenuAction");
        if (sMenuAction.equals("Questions"))
        {
           GetACSearch serAC = new GetACSearch(req, res, this);
           serAC.getACQuestionValue();
           //check if enumerated or not
           Vector vCRFval = (Vector)session.getAttribute("vQuestValue");
           if (vCRFval != null && vCRFval.size() > 0)
              m_VD.setVD_TYPE_FLAG("E");
           else
              m_VD.setVD_TYPE_FLAG("N");
           //read property file and set the VD bean for Placeholder data
           String VDDefinition = m_settings.getProperty("VDDefinition");
           m_VD.setVD_PREFERRED_DEFINITION(VDDefinition);
           String DataType = m_settings.getProperty("DataType");
           m_VD.setVD_DATA_TYPE(DataType);
           String MaxLength = m_settings.getProperty("MaxLength");
           m_VD.setVD_MAX_LENGTH_NUM(MaxLength);
        }
 
        session.setAttribute("m_VD", m_VD);
        VD_Bean oldVD = new VD_Bean();
        oldVD = oldVD.cloneVD_Bean(m_VD);
        session.setAttribute("oldVDBean", oldVD);
        //session.setAttribute("oldVDBean", m_VD);
        ForwardJSP(req, res, "/CreateVDPage.jsp");
   }
 
   /**
   * To search a component or to display more attributes after the serach.
   * Called from 'service' method where reqType is 'searchACs'
   * calls 'getACSearch.getACKeywordResult' method when the action is a new search.
   * calls 'getACSearch.getACShowResult' method when the action is a display attributes.
   * calls 'doRefreshPageForSearchIn' method when the action is searchInSelect.
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doGetACSearchActions(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
 
        HttpSession session = req.getSession();
        String actType = (String)req.getParameter("actSelect");
        String menuAction = (String)session.getAttribute("MenuAction");
        String sUISearchType = (String)req.getAttribute("UISearchType");
        if(sUISearchType == null || sUISearchType.equals("nothing")) sUISearchType = "";
        String sSearchInEVS = "";
        GetACSearch getACSearch = new GetACSearch(req, res, this);
        if ((menuAction != null) && (actType != null))
        {
           //start a new search from search parameter
           if (actType.equals("Search"))
           {
              //search is from create page
              if (menuAction.equals("searchForCreate"))
              {
                  getACSearch.getACSearchForCreate(req, res, false);
                  ForwardJSP(req, res, "/OpenSearchWindow.jsp");
              }
              //search is from regular search page
              else
              {
                  String sComponent = (String)req.getParameter("listSearchFor");
                  if (sComponent != null && sComponent.equals("Questions"))
                  {
                     session.setAttribute("originAction", "QuestionSearch");
                     getACSearch.getACQuestion();
                  }
                  else
                     getACSearch.getACKeywordResult(req, res);
                   //forward to search result page of main search
                  ForwardJSP(req, res, "/SearchResultsPage.jsp");
              }
           }
            //set the attribute send the page back to refresh.
           else if(actType.equals("SearchDef"))
           {
             getACSearch.doSearchEVS(req, res);
             ForwardJSP(req, res, "/EVSSearchPage.jsp");
           }
            else if(actType.equals("SearchDefVM"))
           {
             getACSearch.doSearchEVS(req, res);
             ForwardJSP(req, res, "/EVSSearchPageVM.jsp");
           }
           //show the selected attributes (update button)
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
            else if (actType.equals("doVocabChange"))
           {
              String sVocab = (String)req.getParameter("listContextFilterVocab");
              String sSearchFor = (String)req.getParameter("listSearchFor");
              session.setAttribute("dtsVocab", sVocab);
              req.setAttribute("creSearchIn", "longName");
              if(sVocab.equals("Thesaurus/Metathesaurus") || sVocab.equals("UWD VISUAL ANATOMIST"))
               sSearchInEVS = "Synonym";
              else if(sVocab.equals("VA NDFRT"))
               sSearchInEVS = "Search_Name";
              else if(sVocab.equals("MGED") || sVocab.equals("GO") || sVocab.equals("LOINC")) 
               sSearchInEVS = "Name";
              else
               sSearchInEVS = "Name";
              req.setAttribute("SearchInEVS", sSearchInEVS);
              getCompAttrList(req, res, sSearchFor, "searchForCreate");
              if (!sUISearchType.equals("tree"))
                 ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
              else if (sUISearchType.equals("tree"))
                 this.doTreeSearch(req, res, sUISearchType, sSearchFor); 
           } 
           //set the attribute send the page back to refresh.
           else if(actType.equals("searchInSelect"))
                doRefreshPageForSearchIn(req, res);
 
           //set the attribute send the page back to refresh.
           else if(actType.equals("searchForSelectOther"))
                doRefreshPageOnSearchFor(req, res, "Other");
 
           //set the attribute send the page back to refresh.
           else if(actType.equals("searchForSelectCRF"))
                doRefreshPageOnSearchFor(req, res, "CRFValue");
 
           //call method to UI filter change when hyperlink if pressed.
           else if (actType.equals("advanceFilter") || actType.equals("simpleFilter"))
              this.doUIFilterChange(req, res, menuAction, actType);
 
           //call method  when hyperlink if pressed.
           else if (actType.equals("term") || actType.equals("tree"))
              this.doTreeSearch(req, res, actType, "EVSValueMeaning");
 
           //something is wrong, send error page
           else
               ForwardJSP(req, res, "/ErrorPage.jsp");
        }
        else
            ForwardJSP(req, res, "/ErrorPage.jsp");
   }
 
    /**
   * To refresh the page when filter hyperlink is pressed.
   * Called from 'doGetACSearchActions' method
   * gets request parameters to store the selected values in the session according to what the menu action is
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param String  menuAction to distinguish between main search and search for create windows
   * @param String  actType type of filter a simple or advanced
   *
   * @throws Exception
   */
   private void doUIFilterChange(HttpServletRequest req, HttpServletResponse res, 
       String menuAction, String actType)  throws Exception
   {
       HttpSession session = req.getSession();
       GetACSearch getACSearch = new GetACSearch(req, res, this);
       String sSearchAC = req.getParameter("listSearchFor");
       
       //store the all the selected attributes in search parameter jsp
       this.getSelectedAttr(req, res, menuAction, "ChangeUIFilter");
 
       //get list of search previous search results
       Vector vResult = getACSearch.refreshSearchPage(sSearchAC);
       session.setAttribute("results", vResult); 
       //set the session attributes send the page back to refresh for simple filter.
       if (menuAction.equals("searchForCreate"))
       {
           if (actType.equals("advanceFilter"))
             session.setAttribute("creUIFilter", "advanced");
           else if (actType.equals("simpleFilter"))
             session.setAttribute("creUIFilter", "simple");
           ForwardJSP(req, res, "/OpenSearchWindow.jsp");
       }
       //set session the attribute send the page back to refresh for advanced filter.
       else
       {
           if (actType.equals("advanceFilter"))
              session.setAttribute("serUIFilter", "advanced");
           else if (actType.equals("simpleFilter"))
              session.setAttribute("serUIFilter", "simple");
           ForwardJSP(req, res, "/SearchResultsPage.jsp");
       }
   }
   
  /**
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param String  actType type of filter a simple or advanced
   * @param String  asearchype type of filter a simple or advanced
   *
   * @throws Exception
   */
   private void doCollapseAllNodes(HttpServletRequest req, String dtsVocab)  throws Exception
   {
     HttpSession session = req.getSession();
     session.setAttribute("results", null);
     session.setAttribute("vACSearch", null);
     session.setAttribute("ConceptLevel", "0");
     req.setAttribute("UISearchType", "term");
     EVSMasterTree tree = new EVSMasterTree(req, dtsVocab, this);
     tree.collapseAllNodes();
   }
   
   /**
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param String  actType type of filter a simple or advanced
   * @param String  asearchype type of filter a simple or advanced
   *
   * @throws Exception
   */
   private void doTreeSearch(HttpServletRequest req, HttpServletResponse res, 
    String actType, String searchType)  throws Exception
   {
 //System.out.println("doBlockSearch doTreeSearch");
       HttpSession session = req.getSession();
       GetACSearch getACSearch = new GetACSearch(req, res, this);
       String dtsVocab = req.getParameter("listContextFilterVocab");
       if (actType.equals("term")|| actType.equals(""))
           req.setAttribute("UISearchType", "term");
       else if (actType.equals("tree"))
       {
           req.setAttribute("UISearchType", "tree");
           EVSMasterTree tree = new EVSMasterTree(req, dtsVocab, this);
           String strHTML = tree.populateTreeRoots(dtsVocab);
           session.setAttribute("strHTML", strHTML);
       }
       ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
   }
   
 /**
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param String  actType type of filter a simple or advanced
   * @param String  asearchype type of filter a simple or advanced
   *
   * @throws Exception
 */
   private void doTreeOpenToConcept(HttpServletRequest req, HttpServletResponse res, 
    String actType, String searchType, String sCCode, String sCCodeDB, String sCCodeName)  throws Exception
   {
       HttpSession session = req.getSession();
       String strHTML = "";
       String sOpenToTree = (String)req.getParameter("openToTree");
      // get the concept name from the ccode
       GetACSearch serAC = new GetACSearch(req, res, this);
       if(sCCode.equals("") && !sCCodeName.equals(""))
         sCCode = serAC.do_getEVSCode(sCCodeName, sCCodeDB);   
       if(sOpenToTree == null) sOpenToTree = "";
       if (actType.equals("term")|| actType.equals(""))
           req.setAttribute("UISearchType", "term");
       else if (actType.equals("tree"))
       {
           this.doTreeSearchRequest(req, res, "", sCCode, "false", sCCodeDB);
           req.setAttribute("UISearchType", "tree");
           EVSMasterTree tree = new EVSMasterTree(req, sCCodeDB, this);
           strHTML = tree.populateTreeRoots(sCCodeDB);
           strHTML = tree.openTreeToConcept(sCCode, sCCodeDB, "");
           req.setAttribute("OpenTreeToConcept", "true");
           session.setAttribute("strHTML", strHTML);
       }
       else if (actType.equals("parentTree"))
       {
            if(sCCodeDB.equals("NCI Thesaurus") || sCCodeDB.equals("Thesaurus/Metathesaurus")
           || sCCodeDB.equals("NCI_Thesaurus")) 
             sCCodeName = filterName(sCCodeName, "js");
           session.setAttribute("ParentConcept", sCCodeName);
           this.doTreeSearchRequest(req, res, sCCodeName, sCCode, "false", sCCodeDB);
           req.setAttribute("UISearchType", "tree");
           EVSMasterTree tree = new EVSMasterTree(req, sCCodeDB, this);
           if(!sCCodeDB.equals("NCI Metathesaurus"))
             strHTML = tree.populateTreeRoots(sCCodeDB);
           strHTML = tree.showParentConceptTree(sCCode, sCCodeDB, sCCodeName);
           req.setAttribute("OpenTreeToConcept", "true");
           session.setAttribute("strHTML", strHTML);
       }
        else if (actType.equals("OpenParentTreeToConcept"))
       {
 //System.out.println("OpenParentTreeToConcep");
           this.doTreeSearchRequest(req, res, sCCodeName, sCCode, "false", sCCodeDB);
           req.setAttribute("UISearchType", "tree");
           EVSMasterTree tree = new EVSMasterTree(req, sCCodeDB, this);
           strHTML = tree.populateTreeRoots(sCCodeDB);
           strHTML = tree.openParentTreeToConcept(sCCode, sCCodeDB, sCCodeName);
           req.setAttribute("OpenTreeToConcept", "true");
           session.setAttribute("strHTML", strHTML);
  // System.out.println("done OpenParentTreeToConcep");
       }
       ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
   }
   
   /**
 	 * Puts in and takes out "_"
    *  @param String nodeName.
    *  @param String type.
 	 */
 	private final String filterName(String nodeName, String type)
   {
     if(type.equals("display"))
       nodeName = nodeName.replaceAll("_"," ");
     else if(type.equals("js"))
       nodeName = nodeName.replaceAll(" ","_");
       return nodeName;
   }
   
     /**
   * gets request parameters to store the selected values in the session according to what the menu action is
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doTreeRefreshRequest(HttpServletRequest req, HttpServletResponse res)  throws Exception
   {
       HttpSession session = req.getSession();
       String vocab = req.getParameter("vocab");
       if(vocab == null || vocab.equals("Thesaurus/Metathesaurus")) 
         vocab = "NCI_Thesaurus";
       else if(vocab.equals("MGED")) 
         vocab = "MGED_Ontology";
       session.setAttribute("dtsVocab", vocab);   
       this.doCollapseAllNodes(req, vocab);
       EVSMasterTree tree = new EVSMasterTree(req, vocab, this);
       String strHTML = tree.refreshTree(vocab, "");
       session.setAttribute("strHTML", strHTML);
       req.setAttribute("UISearchType", "tree");
       ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
   }
 
   
   /**
   * gets request parameters to store the selected values in the session according to what the menu action is
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doTreeSearchRequest(HttpServletRequest req, HttpServletResponse res, 
   String sConceptName, String sConceptID, String strForward, String dtsVocab)  throws Exception
   {
       HttpSession session = req.getSession();
       session.setAttribute("ConceptLevel", "0");
       String sUISearchType = (String)req.getAttribute("UISearchType");
       if (sUISearchType == null || sUISearchType.equals("nothing")) sUISearchType = "Concept Code";
       String sKeywordID = (String)req.getParameter("keywordID");
       String sKeywordName = (String)req.getParameter("keywordName");
       if (sKeywordID == null || sKeywordID.equals("")) sKeywordID = sConceptID;
       if (sKeywordName == null || sKeywordName.equals("")) sKeywordName = sConceptName;
 
       String sVocab = (String)req.getParameter("vocab");
       if(sVocab == null) sVocab = dtsVocab;
       if(sVocab.equals("LOINC") || sVocab.equals("VA_NDFRT"))
         sKeywordName = filterName(sKeywordName, "display");
       GetACSearch serAC = new GetACSearch(req, res, this);
       if(sKeywordID.equals("") && !sKeywordName.equals(""))
         sKeywordID = serAC.do_getEVSCode(sKeywordName, sVocab);   
 
       String sOpenToTree = (String)req.getParameter("openToTree");
       if(sOpenToTree == null) sOpenToTree = "";
 
       String sRetired = (String)req.getParameter("rRetired");
       if (sRetired == null) sRetired = "Exclude";  
       String sConteIdseq = (String)req.getParameter("sConteIdseq");
       if (sConteIdseq == null) sConteIdseq = "";
       session.setAttribute("creRetired", sRetired);
       session.setAttribute("dtsVocab", sVocab);
       String sSearchType = "";
       String sSearchAC = (String)session.getAttribute("creSearchAC");
 
       if(sSearchAC == null) 
         sSearchAC = "";
       else if(sSearchAC.equals("ObjectClass"))
         sSearchType = "OC";
       else if(sSearchAC.equals("Property"))
         sSearchType = "PROP";
       else if(sSearchAC.equals("RepTerm"))
         sSearchType = "REP";
       else if(sSearchAC.equals("ObjectQualifier"))
         sSearchType = "ObjQ";
        else if(sSearchAC.equals("PropertyQualifier"))
         sSearchType = "PropQ";
        else if(sSearchAC.equals("RepQualifier"))
         sSearchType = "RepQ";
       Vector vAC = new Vector();
       Vector vResult = new Vector();
       if (sKeywordID != null)
       {
         if(!sSearchAC.equals("ParentConcept") && !sSearchAC.equals("ParentConceptVM")
         && !sSearchAC.equals("EVSValueMeaning") && !sOpenToTree.equals("true"))
         {
           if(sVocab.equals("Thesaurus/Metathesaurus") || sVocab.equals("NCI_Thesaurus")
           || sVocab.equals("NCI Thesaurus") || sVocab.equals("MedDRA"))
             sKeywordName = filterName(sKeywordName, "display");
             // to avoid two "*" if user typed on e there
             if (sKeywordName.substring(sKeywordName.length()-1, sKeywordName.length()).equals("*"))
                 sKeywordName = sKeywordName.substring(0, sKeywordName.length()-1);
             sKeywordName = sKeywordName + "*";
             
           if(sSearchAC.equals("ObjectClass") || sSearchAC.equals("Property") 
             || sSearchAC.equals("RepTerm"))
             serAC.do_caDSRSearch(sKeywordName, "", "", "", vAC, sSearchType);
         }
         if (sKeywordID != null && !sKeywordID.equals("")) 
           serAC.do_EVSSearch(sKeywordID, vAC, sVocab, "Concept Code",
           "All Sources", 100, sUISearchType, sRetired, sConteIdseq);
         else if (sKeywordName != null && !sKeywordName.equals("")) 
           serAC.do_EVSSearch(sKeywordName, vAC, sVocab, "term",
           "All Sources", 100, sUISearchType, sRetired, sConteIdseq);
           
         session.setAttribute("vACSearch", vAC);
         if(sSearchAC.equals("ParentConcept"))
           session.setAttribute("vParResult", vAC);
         else if(sSearchAC.equals("ParentConceptVM"))
           session.setAttribute("vParResultVM", vAC);
 
         if (sSearchAC.equals("EVSValueMeaning"))
         {
           serAC.get_Result(req, res, vResult, "DEF");
           session.setAttribute("EVSresults", vResult);
           session.setAttribute("results", vResult);
         }
         else
         {
           serAC.get_Result(req, res, vResult, "");
           session.setAttribute("results", vResult);
         }       
         session.setAttribute("creKeyword", sKeywordID);
         req.setAttribute("labelKeyword", sKeywordName);
         Integer recs = new Integer(vAC.size());
         String recs2 = recs.toString();
         sKeywordID = "";
         sKeywordName = "";
         req.setAttribute("creRecsFound", recs2);  
       }
  
       req.setAttribute("UISearchType", "tree");
       if(!strForward.equals("false"))
       {
         ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
       }
   }
 
   /**
   * gets request parameters to store the selected values in the session according to what the menu action is
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doGetSubConcepts(HttpServletRequest req, HttpServletResponse res)  throws Exception
   {
       HttpSession session = req.getSession();
       String sConceptName = (String)req.getParameter("nodeName"); 
       String sConceptCode = (String)req.getParameter("nodeCode");
       String dtsVocab = (String)req.getParameter("vocab");
       String sDefSource = (String)req.getParameter("defSource");
 
       String sSearchType = (String)req.getParameter("searchType"); 
       String sRetired = "";  //(String)req.getParameter("rRetired");
       String sConteIdseq = (String)req.getParameter("sConteIdseq");
       if (sConteIdseq == null) sConteIdseq = "";
       
     if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("")
      || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       dtsVocab = this.m_VOCAB_NCI; //"NCI_Thesaurus";
     else if(dtsVocab.equals("VA NDFRT"))
       dtsVocab = this.m_VOCAB_VA;  //"VA_NDFRT";
     else if(dtsVocab.equals("UWD VISUAL ANATOMIST") || dtsVocab.equals("UWD_VISUAL_ANATOMIST"))
       dtsVocab = this.m_VOCAB_UWD;  //"UWD_Visual_Anatomist";
     else if(dtsVocab.equals("MGED")) 
       dtsVocab = this.m_VOCAB_MGE;  //"MGED_Ontology";
     else if(dtsVocab.equals("GO"))
       dtsVocab = this.m_VOCAB_GO;
     else if(dtsVocab.equals("LOINC"))
       dtsVocab = this.m_VOCAB_LOI;
     else if(dtsVocab.equals("MedDRA"))
       dtsVocab = this.m_VOCAB_MED;
     else if(dtsVocab.equals("HL7_V3")) 
       dtsVocab = this.m_VOCAB_HL7;
       
      if (dtsVocab == null) dtsVocab = "";
      if (sConceptName == null || sConceptCode == null || dtsVocab == null) 
         return;
       Vector vAC = new Vector();
       Vector vResult = new Vector();
       String sSearchAC = (String)session.getAttribute("creSearchAC");
       if(sSearchAC == null) sSearchAC = "";
       GetACSearch serAC = new GetACSearch(req, res, this);
       Vector vSubConceptNames = null;
       
       String sParent = (String)session.getAttribute("ParentConcept");
       if(sParent == null) sParent = "";
       String sParentSource = "";
       if(dtsVocab.equals("NCI Metathesaurus") && sSearchAC.equals("ParentConceptVM"))
         sParentSource = serAC.getMetaParentSource(sParent, "None", null);
       if(!sParentSource.equals(""))
         sDefSource = sParentSource;
       sParent = filterName(sParent, "display");
       int ilevelStartingConcept = 0;
       if(sConceptName.equals(sParent))
         ilevelStartingConcept = 0;
       else
       {
         if(dtsVocab.equals("NCI Metathesaurus"))
           ilevelStartingConcept = serAC.getLevelDownFromParentMeta(sConceptName, dtsVocab, sDefSource);
         else 
           ilevelStartingConcept = serAC.getLevelDownFromParent(sConceptName, dtsVocab);
       }
       String sLevelStartingConcept = "";
       Integer ILevelStartingConcept = new Integer(ilevelStartingConcept);
       sLevelStartingConcept = ILevelStartingConcept.toString();
       session.setAttribute("ConceptLevel", sLevelStartingConcept);
       if(dtsVocab.equals("NCI Metathesaurus"))
       {
         sDefSource = (String)session.getAttribute("ParentMetaSource"); 
         sDefSource = getSourceToken(sDefSource);
         if(sDefSource == null) sDefSource = "";
       }
       if(sSearchType.equals("All"))
         vSubConceptNames = serAC.getSubConceptNames(dtsVocab, sConceptName, "All", sConceptCode, sDefSource);
       else if(sSearchType.equals("Immediate"))
         vSubConceptNames = serAC.getSubConceptNames(dtsVocab, sConceptName, "Immediate", sConceptCode, sDefSource);
       for(int j=0; j < vSubConceptNames.size(); j++) 
       { 
           String sName = (String)vSubConceptNames.elementAt(j);
           String sCode = serAC.do_getEVSCode(sName, dtsVocab);   
           serAC.do_EVSSearch(sCode, vAC, dtsVocab, "Concept Code",
           sDefSource, 100, "term", sRetired, sConteIdseq);         
       }
       
       session.setAttribute("vACSearch", vAC);
       if(sSearchAC.equals("ParentConcept"))
         session.setAttribute("vParResult", vAC);
       else if(sSearchAC.equals("ParentConceptVM"))
         session.setAttribute("vParResultVM", vAC);
       else  //for all other
         session.setAttribute("vACSearch", vAC);
         
       session.setAttribute("creRetired", sRetired);
       session.setAttribute("dtsVocab", dtsVocab);
     
       if (sSearchAC.equals("EVSValueMeaning"))
       {
         serAC.get_Result(req, res, vResult, "DEF");
         session.setAttribute("EVSresults", vResult);
         session.setAttribute("results", vResult);
       }
        else if (sSearchAC.equals("ObjectClass"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("Property") || sSearchAC.equals("PropertyClass"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("RepTerm"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("ObjectQualifier"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("PropertyQualifier"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("RepQualifier"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       } 
       else
       { 
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       } 
       session.setAttribute("creKeyword", sConceptCode);
       req.setAttribute("labelKeyword", sConceptName);
       Integer recs = new Integer(vAC.size());
       String recs2 = recs.toString();
       req.setAttribute("creRecsFound", recs2);  
       session.setAttribute("vACSearch", vAC);
       req.setAttribute("UISearchType", "tree");
       ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
   }
 
   /**
   * gets request parameters to store the selected values in the session according to what the menu action is
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doGetSuperConcepts(HttpServletRequest req, HttpServletResponse res)  throws Exception
   {
       HttpSession session = req.getSession();
       String sConceptName = (String)req.getParameter("nodeName"); 
       String sConceptCode = (String)req.getParameter("nodeCode");
       String dtsVocab = (String)req.getParameter("vocab");
       String sDefSource = (String)req.getParameter("defSource");
       String sSearchType = (String)req.getParameter("searchType");
       String sRetired = "";  //(String)req.getParameter("rRetired");
     //  if (sRetired == null) sRetired = "Exclude";
       String sConteIdseq = (String)req.getParameter("sConteIdseq");
       if (sConteIdseq == null) sConteIdseq = "";
       
     if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("")
     || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       dtsVocab = this.m_VOCAB_NCI; //"NCI_Thesaurus";
     else if(dtsVocab.equals("VA NDFRT"))
       dtsVocab = this.m_VOCAB_VA;  //"VA_NDFRT";
     else if(dtsVocab.equals("UWD VISUAL ANATOMIST") || dtsVocab.equals("UWD_VISUAL_ANATOMIST"))
       dtsVocab = this.m_VOCAB_UWD;  //"UWD_Visual_Anatomist";
     else if(dtsVocab.equals("MGED")) 
       dtsVocab = this.m_VOCAB_MGE;  //"MGED_Ontology";
     else if(dtsVocab.equals("GO"))
       dtsVocab = this.m_VOCAB_GO;
     else if(dtsVocab.equals("LOINC"))
       dtsVocab = this.m_VOCAB_LOI;
     else if(dtsVocab.equals("MedDRA"))
       dtsVocab = this.m_VOCAB_MED;
     else if(dtsVocab.equals("HL7_V3")) 
       dtsVocab = this.m_VOCAB_HL7;
       
       if (sConceptName == null || sConceptCode == null || dtsVocab == null) 
         return;
       Vector vAC = new Vector();
       Vector vResult = new Vector();
       String sSearchAC = (String)session.getAttribute("creSearchAC");
       if(sSearchAC == null) sSearchAC = "";
       GetACSearch serAC = new GetACSearch(req, res, this);
       Vector vSuperConceptNames = null;
       String sName = "";
       String sCode = "";
       String sParentName = "";
       if(sSearchAC.equals("ParentConceptVM"))
         sParentName= (String)session.getAttribute("ParentConcept");   
       vSuperConceptNames = serAC.getSuperConceptNames(dtsVocab, sConceptName, sConceptCode, sDefSource);
       for(int j=0; j < vSuperConceptNames.size(); j++) 
       { 
           sName = (String)vSuperConceptNames.elementAt(j);
           if(sName != null && !sName.equals(""))
             sCode = serAC.do_getEVSCode(sName, dtsVocab);
           if(sCode != null && !sCode.equals(""))
             serAC.do_EVSSearch(sCode, vAC, dtsVocab, "Concept Code",
             "All Sources", 100, "term", sRetired, sConteIdseq);
           if(sName.equals(sParentName))
             break;
       }
       session.setAttribute("vACSearch", vAC);
       if(sSearchAC.equals("ParentConcept"))
         session.setAttribute("vParResult", vAC);
       else if(sSearchAC.equals("ParentConceptVM"))
         session.setAttribute("vParResultVM", vAC);
       session.setAttribute("creRetired", sRetired);
       session.setAttribute("dtsVocab", dtsVocab);
     
       if (sSearchAC.equals("EVSValueMeaning"))
       {
         serAC.get_Result(req, res, vResult, "DEF");
         session.setAttribute("EVSresults", vResult);       
       }
       else if (sSearchAC.equals("ObjectClass"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("Property") || sSearchAC.equals("PropertyClass"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("RepTerm"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("ObjectQualifier"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("PropertyQualifier"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       }
       else if (sSearchAC.equals("RepQualifier"))
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       } 
       else
       {
         serAC.get_Result(req, res, vResult, "");
         session.setAttribute("results", vResult);
       } 
       session.setAttribute("creKeyword", sConceptCode);
       req.setAttribute("labelKeyword", sConceptName);
       Integer recs = new Integer(vAC.size());
       String recs2 = recs.toString();
       req.setAttribute("creRecsFound", recs2);  
       session.setAttribute("vACSearch", vAC);
       req.setAttribute("UISearchType", "tree");
       ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
   }
 
 
     /**
   * gets request parameters to store the selected values in the session according to what the menu action is
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doTreeExpandRequest(HttpServletRequest req, HttpServletResponse res)  throws Exception
   {
       HttpSession session = req.getSession();
       String sSearchAC = (String)session.getAttribute("creSearchAC");
       session.setAttribute("creKeyword", "");
       if(sSearchAC == null)
         sSearchAC = "";
       else
       {
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         Vector vResult = getACSearch.refreshSearchPage(sSearchAC);
         session.setAttribute("results", vResult); 
       }
       String nodeName = req.getParameter("nodeName");
       String nodeCode = req.getParameter("nodeCode");
       String vocab = req.getParameter("vocab");
       if(vocab == null) vocab = "NCI_Thesaurus";
       session.setAttribute("dtsVocab", vocab);     
       GetACSearch serAC = new GetACSearch(req, res, this);
       if(nodeCode.equals("") && !nodeName.equals(""))
         nodeCode = serAC.do_getEVSCode(nodeName, vocab);   
       EVSMasterTree tree = new EVSMasterTree(req, vocab, this);
       String strHTML = tree.expandNode(nodeName, vocab, "", nodeCode, "");
       session.setAttribute("strHTML", strHTML);
       req.setAttribute("UISearchType", "tree");
       ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
   }
   
     /**
   * gets request parameters to store the selected values in the session according to what the menu action is
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doTreeCollapseRequest(HttpServletRequest req, HttpServletResponse res)  throws Exception
   {
       HttpSession session = req.getSession();
       String sSearchAC = (String)session.getAttribute("creSearchAC");
       if(sSearchAC == null)
         sSearchAC = "";
       else
       {
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         Vector vResult = getACSearch.refreshSearchPage(sSearchAC);
         session.setAttribute("results", vResult); 
       }
       String nodeName = req.getParameter("nodeName");
       String vocab = req.getParameter("vocab");
       if(vocab == null) vocab = "NCI_Thesaurus";
       session.setAttribute("dtsVocab", vocab);  
       EVSMasterTree tree = new EVSMasterTree(req, vocab, this);
       String strHTML = tree.collapseNode(nodeName, vocab, "");
       session.setAttribute("strHTML", strHTML);
       req.setAttribute("UISearchType", "tree");
       ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
   }
   
   /**
   * To refresh the page when the search in changed from drop down list.
   * Called from 'doGetACSearchActions' method
   * modifies the session attribute 'selectedAttr' or 'creSelectedAttr' according to what is selected.
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doRefreshPageForSearchIn(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
        HttpSession session = req.getSession();
        //same for both searchfor create and regular search
        String sSearchIn = (String)req.getParameter("listSearchIn");
        if (sSearchIn == null)
           sSearchIn = "longName";
 
        //same for both searchfor create and regular search
        String sSearchAC = (String)req.getParameter("listSearchFor");
 
        // set the selected display attributes so they persist through refreshes
        String selAttrs[] = req.getParameterValues("listAttrFilter");
        int selLength = selAttrs.length;
        Vector vSelAttrs = new Vector();
        String sID = "";
        if(selAttrs != null)
        {
           for (int i=0;i<selAttrs.length;i++)
           {
             sID = selAttrs[i];
             if ((sID != null) && (!sID.equals("")))
               vSelAttrs.addElement(sID);
           }
        }
 
        String menuAction = (String)session.getAttribute("MenuAction");
        //add/remove protocol and crf from the displayable attriubtes according to the search in.
        Vector vCompAttr = new Vector();
        if (menuAction.equals("searchForCreate"))
           vCompAttr = (Vector)session.getAttribute("creAttributeList");
        else
           vCompAttr = (Vector)session.getAttribute("serAttributeList");
           
        if (vCompAttr != null && sSearchIn.equals("CRFName"))
        {
           if (!vCompAttr.contains("Protocol ID")) vCompAttr.insertElementAt("Protocol ID", 12);
           if (!vCompAttr.contains("CRF Name")) vCompAttr.insertElementAt("CRF Name", 13);
        }
        else
        {
           if (vCompAttr.contains("Protocol ID")) vCompAttr.removeElement("Protocol ID");
           if (vCompAttr.contains("CRF Name")) vCompAttr.removeElement("CRF Name");            
        }
        //put it back in the session
        if (menuAction.equals("searchForCreate"))
           session.setAttribute("creAttributeList", vCompAttr);
        else
           session.setAttribute("serAttributeList", vCompAttr);
 
        //store the all the selected attributes in search parameter jsp
        this.getSelectedAttr(req, res, menuAction, "ChangeSearchIn");
 
        //gets selected attributes and sets session attributes.
        if (!menuAction.equals("searchForCreate"))
        {
           session.setAttribute("serSearchIn", sSearchIn);  //set the search in attribute
           //call method to add or remove selected display attributes as search in changes
           Vector vSelectedAttr = getDefaultSearchInAttr(sSearchAC, sSearchIn, vSelAttrs, vCompAttr);
           
           //Store the session attributes
           session.setAttribute("selectedAttr", vSelectedAttr);
            GetACSearch serAC = new GetACSearch(req, res, this);
            Vector vResult = serAC.refreshSearchPage(sSearchAC);
            session.setAttribute("results", vResult);
           session.setAttribute("serKeyword", "");
           session.setAttribute("serProtoID", "");
           //send page
           ForwardJSP(req, res, "/SearchResultsPage.jsp");
        }
        else    //menu action searchForCreate
        {
            req.setAttribute("creSearchIn", sSearchIn);  //set the search in attribute
            //call method to add or remove selected display attributes as search in changes
            Vector vSelectedAttr = getDefaultSearchInAttr(sSearchAC, sSearchIn, vSelAttrs, vCompAttr);           
            //Store the session attributes
            session.setAttribute("creSelectedAttr", vSelectedAttr);
            //req.setAttribute("creSelectedAttrBlocks", vSelectedAttr);
 
            GetACSearch serAC = new GetACSearch(req, res, this);
            Vector vResult = serAC.refreshSearchPage(sSearchAC);
            session.setAttribute("result", vResult);
            session.setAttribute("creKeyword", "");
            //set the session attribute for searchAC
            ForwardJSP(req, res, "/OpenSearchWindow.jsp");
        }
   }
 
   /**
   * To add or remove search in attributes as seach in changed.
   *
   * @param sSearchAC String searching component
   * @param sSearchIn String searching in attribute
   * @param vSelectedAttr Vector selected attribute
   * @param vCom Vector of all attributes of the selected component.
   *
   * @return Vector selected attribute vector
   * @throws Exception
   */
   private Vector getDefaultSearchInAttr(String sSearchAC, String sSearchIn, 
     Vector vSelectedAttr, Vector vComp) throws Exception
   {
       //first remove all the searchIn from the selected attribute list 
       if (vSelectedAttr.contains("Protocol ID"))
           vSelectedAttr.remove("Protocol ID");
       if (vSelectedAttr.contains("CRF Name"))
           vSelectedAttr.remove("CRF Name");
       
       //add public id to selected attribute seperately for each type
       if (sSearchIn.equals("minID"))
       {
          if (!vSelectedAttr.contains("Public ID"))
             vSelectedAttr.add("Public ID");
       }
       //select the hist cde id if not selected and remove crf/protocol for hist cdeid searchin
       else if (sSearchIn.equals("histID"))
       {
          if (!vSelectedAttr.contains("Historical CDE ID"))
             vSelectedAttr.add("Historical CDE ID");
       }
       else if (sSearchIn.equals("origin"))
       {
          if (!vSelectedAttr.contains("Origin"))
             vSelectedAttr.add("Origin");
       }
       else if (sSearchIn.equals("NamesAndDocText"))
       {
          if (!vSelectedAttr.contains("Long Name Document Text"))
             vSelectedAttr.add("Long Name Document Text");
          if (!vSelectedAttr.contains("Historic Short CDE Name Document Text"))
             vSelectedAttr.add("Historic Short CDE Name Document Text");
       }
       else if (sSearchIn.equals("docText"))
       {
          if (!vSelectedAttr.contains("Long Name Document Text"))
             vSelectedAttr.add("Long Name Document Text");
          if (!vSelectedAttr.contains("Historic Short CDE Name Document Text"))
             vSelectedAttr.add("Historic Short CDE Name Document Text");
          if (!vSelectedAttr.contains("Reference Documents"))
             vSelectedAttr.add("Reference Documents");
       }
       else if (sSearchIn.equals("permValue"))
       {
          if (!vSelectedAttr.contains("Permissible Value"))
             vSelectedAttr.add("Permissible Value");
       }
       //add proto and crf and remove cde id if crf name is search in
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
 
       //call method to resort the display attributes
       vSelectedAttr = this.resortDisplayAttributes(vComp, vSelectedAttr);
       return vSelectedAttr;
     
   }  // end of getDefaultSearchInAttr
 
   /**
    * resorts the display attributes from the component attributes 
    * after add/remove attributes of selected attribute vector.
    * 
    * @param vCompAttr list of attributes of the selected component
    * @param vSelectAttr list of selected attributes of according to the action
    * 
    * @return return the sorted selected attributes list
    * 
    * @throws Exception
    */
   public Vector resortDisplayAttributes(Vector vCompAttr, Vector vSelectAttr) throws Exception
   {
       //resort the display attributes
       Vector vReSort = new Vector();
       if (vCompAttr != null)
       {
         for (int j=0; j<vCompAttr.size(); j++)
         {
           String thisAttr = (String)vCompAttr.elementAt(j);
           //add this attr to a vector if it is a selected attr
           if (vSelectAttr.contains(thisAttr))
             vReSort.addElement(thisAttr);
         }
       }
       return vReSort;
   }
    /**
   * To refresh the page when filter hyperlink is pressed.
   * Called from 'doGetACSearchActions' method
   * gets request parameters to store the selected values in the session according to what the menu action is
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param String  menuAction to distinguish between main search and search for create windows
   * @param String  actType type of filter a simple or advanced
   *
   * @throws Exception
   */
   private void getSelectedAttr(HttpServletRequest req, HttpServletResponse res, 
       String menuAction, String actType)  throws Exception
   {
       HttpSession session = req.getSession();
       GetACSearch getACSearch = new GetACSearch(req, res, this);
       String sSearchAC = "";
       Vector vDocType = new Vector();
       
       //store the all the attributes in search parameter jsp
       String sProtoID = (String)req.getParameter("protoKeyword");
       String sKeyword = (String)req.getParameter("keyword");  //the keyword      
       String sContext = (String)req.getParameter("listContextFilter");  //filter by context 
       String sContextUse = (String)req.getParameter("rContextUse");   //filter by contextUse
       String sVersion = (String)req.getParameter("rVersion");   //filter by version
       String sVDTypeEnum = (String)req.getParameter("typeEnum");    //filter by value domain type enumerated
       String sVDTypeNonEnum = (String)req.getParameter("typeNonEnum");    //filter by value domain type non enumerated
       String sVDTypeRef = (String)req.getParameter("typeEnumRef");      //filter by value domain type enumerated by reference
       String sRegStatus = (String)req.getParameter("listRegStatus");   //filter by registration status
       String sStatus = "";
       String sCreFrom = "", sCreTo = "", sModFrom = "", sModTo = "", sCre = "", sMod = "";
       if (actType.equals("ChangeSearchIn"))
       {
         sCreFrom = (String)req.getParameter("createdFrom");   //filter by createdFrom
         sCreTo = (String)req.getParameter("createdTo");   //filter by createdTo
         sModFrom = (String)req.getParameter("modifiedFrom");   //filter by modifiedFrom
         sModTo = (String)req.getParameter("modifiedTo");   //filter by modifiedTo
         sCre = (String)req.getParameter("creator");   //filter by creator
         sMod = (String)req.getParameter("modifier");   //filter by modifier
       }
 
       //set the session attributes send the page back to refresh for simple filter.
       if (menuAction.equals("searchForCreate"))
       {
           session.setAttribute("creKeyword", sKeyword);   //keep the old context criteria
           session.setAttribute("creProtoID", sProtoID);  //keep the old protocol id criteria
           session.setAttribute("creContext", sContext);   //keep the old context criteria
           req.setAttribute("creContextBlocks", sContext);
           session.setAttribute("creContextUse", sContextUse);   //store contextUse in the session
           session.setAttribute("creVersion", sVersion);   //store version in the session
           session.setAttribute("creVDTypeEnum", sVDTypeEnum);   //store VDType Enum in the session
           session.setAttribute("creVDTypeNonEnum", sVDTypeNonEnum);   //store VDType Non Enum in the session
           session.setAttribute("creVDTypeRef", sVDTypeRef);   //store VDType Ref in the session
           session.setAttribute("creRegStatus", sRegStatus);   //store regstatus in the session
           session.setAttribute("creCreatedFrom", sCreFrom);   //empty the date attributes 
           session.setAttribute("creCreatedTo", sCreTo);     //empty the date attributes 
           session.setAttribute("creModifiedFrom", sModFrom);   //empty the date attributes 
           session.setAttribute("creModifiedTo", sModTo);   //empty the date attributes 
           session.setAttribute("creCreator", sCre);      //empty the creator attributes 
           session.setAttribute("creModifier", sMod);  //empty the modifier attributes 
           session.setAttribute("creDocTyes", vDocType);
 
           sSearchAC = (String)session.getAttribute("creSearchAC");
           sStatus = getACSearch.getMultiReqValues(sSearchAC, "searchForCreate", "Context");
           sStatus = getACSearch.getStatusValues(req, res, sSearchAC, "searchForCreate");   //to get a string from multiselect list
       }
       //set session the attribute send the page back to refresh for advanced filter.
       else
       {
           session.setAttribute("serKeyword", sKeyword);   //keep the old criteria
           session.setAttribute("serProtoID", sProtoID);  //keep the old protocol id criteria
           session.setAttribute("LastAppendWord", sKeyword);
           session.setAttribute("serContext", sContext);   //keep the old context criteria
           session.setAttribute("serContextUse", sContextUse);   //store contextUse in the session
           session.setAttribute("serVersion", sVersion);   //store version in the session
           session.setAttribute("serVDTypeEnum", sVDTypeEnum);   //store VDType Enum in the session
           session.setAttribute("serVDTypeNonEnum", sVDTypeNonEnum);   //store VDType Non Enum in the session
           session.setAttribute("serVDTypeRef", sVDTypeRef);   //store VDType Ref in the session
           session.setAttribute("serRegStatus", sRegStatus);   //store regstatus in the session
           session.setAttribute("serCreatedFrom", sCreFrom);   //empty the date attributes 
           session.setAttribute("serCreatedTo", sCreTo);     //empty the date attributes 
           session.setAttribute("serModifiedFrom", sModFrom);   //empty the date attributes 
           session.setAttribute("serModifiedTo", sModTo);   //empty the date attributes 
           session.setAttribute("serCreator", sCre);      //empty the creator attributes 
           session.setAttribute("serModifier", sMod);  //empty the modifier attributes 
           session.setAttribute("serDocTyes", vDocType);  //empty doctype list
 
           sSearchAC = (String)session.getAttribute("searchAC");
           sStatus = getACSearch.getMultiReqValues(sSearchAC, "MainSearch", "Context");
           sStatus = getACSearch.getStatusValues(req, res, sSearchAC, "MainSearch");   //to get a string from multiselect list
       }
   }
   
    /**
   * To refresh the page when the search For changed from drop down list.
   * Called from 'doGetACSearchActions' method
   * modifies the session attribute 'selectedAttr' or 'creSelectedAttr' according to what is selected.
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doRefreshPageOnSearchFor(HttpServletRequest req, HttpServletResponse res, String sOrigin)
   throws Exception
   {
        HttpSession session = req.getSession();
     //   clearSessionAttributes(req, res);
        //get the search for parameter from the request
        String sSearchAC = (String)req.getParameter("listSearchFor");
        String sSearchIn = "longName";
        //call the method to get attribute list for the selected AC
        String menuAction = (String)session.getAttribute("MenuAction");
        getCompAttrList(req, res, sSearchAC, menuAction);
        //change the selected attributes according to what is selected
        Vector vSelectedAttr = new Vector();
        vSelectedAttr = getDefaultAttr(sSearchAC, sSearchIn);
        this.getDefaultFilterAtt(req, res);    //get the default filter by attributes
        if (!menuAction.equals("searchForCreate"))
        {
           //Store the session attributes
           session.setAttribute("selectedAttr", vSelectedAttr);
           session.setAttribute("searchAC", sSearchAC);
           session.setAttribute("MenuAction", "nothing");
           // new searchFor, reset the stacks
           clearSessionAttributes(req,res);
           ForwardJSP(req, res, "/SearchResultsPage.jsp");
        }
        else
        {
           //Store the session attributes
           session.setAttribute("creSelectedAttr", vSelectedAttr);
           session.setAttribute("creSearchAC", sSearchAC);
           session.setAttribute("vACSearch", new Vector());
           //do the basic search for conceptual domain
           if (sSearchAC.equals("ConceptualDomain"))   // || sSearchAC.equals("ValueMeaning"))
           {
             GetACSearch getACSearch = new GetACSearch(req, res, this);
             getACSearch.getACSearchForCreate(req, res, true);
           }
           if (sSearchAC.equals("ValueMeaning"))
           {
             VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
             String sCDid = "";
             if (vd != null) sCDid = vd.getVD_CD_IDSEQ();          
             session.setAttribute("creSelectedCD", sCDid);            
           }
           //forward the page with crfresults if it is crf value search, otherwise searchResults
           if (sOrigin.equals("CRFValue"))
               ForwardJSP(req, res, "/CRFValueSearchWindow.jsp");
           else
               ForwardJSP(req, res, "/OpenSearchWindow.jsp");
        }
   }
 
      /**
   * To search results by clicking on the column heading.
   * Called from 'service' method where reqType is 'searchEVS'
   * forwards page 'EVSSearchPage.jsp'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doSearchEVS(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
     HttpSession session = req.getSession();
     Vector vResults = new Vector();
     try
     {
        GetACSearch getACSearch = new GetACSearch(req, res, this);
        getACSearch.doSearchEVS(req, res);
     }
     catch (Exception e)
     {
       //System.err.println("EVS Search : " + e);
       this.logger.fatal("ERROR - EVS Search : " + e.toString());
     }
     ForwardJSP(req, res, "/EVSSearchPage.jsp");
    // ForwardJSP(req, res, "/OpenSearchWindow.jsp");
 }
 
   /**
   * To search a component or to display more attributes after the serach.
   * Called from 'service' method where reqType is 'searchACs'
   * calls 'getACSearch.getACKeywordResult' method when the action is a new search.
   * calls 'getACSearch.getACShowResult' method when the action is a display attributes.
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doBlockSearchActions(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
        HttpSession session = req.getSession();
        String actType = (String)req.getParameter("actSelect");
        if(actType == null) actType = "";
        String sSearchFor = (String)req.getParameter("listSearchFor");
        String dtsVocab = req.getParameter("listContextFilterVocab");
        String sSearchInEVS = "";
        String sUISearchType = ""; 
        String menuAction = (String)session.getAttribute("MenuAction");
        String sMetaSource = req.getParameter("listContextFilterSource");
        if (sMetaSource == null) sMetaSource = ""; 
        GetACSearch getACSearch = new GetACSearch(req, res, this);
        session.setAttribute("creSearchAC", sSearchFor);
        session.setAttribute("dtsVocab", dtsVocab); 
        getCompAttrList(req, res, sSearchFor, "searchForCreate");      
        if ((menuAction != null) && (actType != null))
        {
           if (actType.equals("Search"))
           {
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
             session.setAttribute("creContext", "");
             req.setAttribute("creSearchIn", "longName");
             if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("UWD VISUAL ANATOMIST")
             || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
               sSearchInEVS = "Synonym";
             else if(dtsVocab.equals("VA NDFRT"))
               sSearchInEVS = "Search_Name";
             else if(dtsVocab.equals("MGED") || dtsVocab.equals("GO") || dtsVocab.equals("LOINC")) 
               sSearchInEVS = "Name";
             else
               sSearchInEVS = "Name";
             req.setAttribute("SearchInEVS", sSearchInEVS);
             this.doCollapseAllNodes(req, dtsVocab);
             req.setAttribute("UISearchType", "term");
             ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
           }
           else if (actType.equals("OpenTreeToConcept"))
           {           
             String sCCodeDB = (String)req.getParameter("sCCodeDB"); 
             String sCCode = (String)req.getParameter("sCCode");
             String sCCodeName = (String)req.getParameter("sCCodeName");   
             if(!sCCode.equals(""))
             {
               if(sCCodeDB.equals("NCI Thesaurus"))  sCCodeDB = "Thesaurus/Metathesaurus";
               this.doCollapseAllNodes(req, dtsVocab);
               this.doTreeOpenToConcept(req, res, "tree", "Blocks", sCCode, sCCodeDB, sCCodeName);
             }
             else 
             {
               this.doCollapseAllNodes(req, dtsVocab);
               this.doTreeSearch(req, res, actType, "Blocks");
             }
           }
           else if (actType.equals("OpenTreeToParentConcept"))
           { 
             String sCCodeDB = (String)req.getParameter("sCCodeDB");
             String sCCode = (String)req.getParameter("sCCode");
             String sCCodeName = (String)req.getParameter("sCCodeName");  
             session.setAttribute("SelectedParentName", sCCodeName);
             session.setAttribute("SelectedParentCC", sCCode);
             session.setAttribute("SelectedParentDB", sCCodeDB);
             if(!sCCode.equals(""))
             {
               if(sCCodeDB.equals("NCI Thesaurus"))  sCCodeDB = "Thesaurus/Metathesaurus";
               this.doCollapseAllNodes(req, dtsVocab);
               this.doTreeOpenToConcept(req, res, "parentTree", "Blocks", sCCode, sCCodeDB, sCCodeName);
             }
             else 
             {
               this.doCollapseAllNodes(req, dtsVocab);
               this.doTreeSearch(req, res, actType, "Blocks");
             }
           }
           else if (actType.equals("doVocabChange"))
           {
             session.setAttribute("dtsVocab", dtsVocab);
             req.setAttribute("creSearchIn", "longName");
             if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("UWD VISUAL ANATOMIST")
             || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
               sSearchInEVS = "Synonym";
             else if(dtsVocab.equals("VA NDFRT"))
               sSearchInEVS = "Search_Name";
             else if(dtsVocab.equals("MGED") || dtsVocab.equals("GO") || dtsVocab.equals("LOINC")) 
               sSearchInEVS = "Name";
             else
               sSearchInEVS = "Name";
             req.setAttribute("SearchInEVS", sSearchInEVS);
             getCompAttrList(req, res, sSearchFor, "searchForCreate");
             this.doCollapseAllNodes(req, dtsVocab);
             req.setAttribute("UISearchType", "term");
             ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
           } 
           else if (actType.equals("term") || actType.equals("tree")) 
           {
             this.doCollapseAllNodes(req, dtsVocab);
             this.doTreeSearch(req, res, actType, "Blocks");
           }
           else if (actType.equals("nonEVS"))
             ForwardJSP(req, res, "/NonEVSSearchPage.jsp");
        }
        else
            ForwardJSP(req, res, "/ErrorPage.jsp");
   }
 
    /**
   * To search a component or to display more attributes after the serach.
   * Called from 'service' method where reqType is 'searchACs'
   * calls 'getACSearch.getACKeywordResult' method when the action is a new search.
   * calls 'getACSearch.getACShowResult' method when the action is a display attributes.
   * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
   * if action is searchForCreate forwards OpenSearchWindow.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doQualifierSearchActions(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
        HttpSession session = req.getSession();
        String actType = (String)req.getParameter("actSelect");
        String menuAction = (String)session.getAttribute("MenuAction");
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
   * Sets a session attribute for a Building Block search.
   * Called from 'service' method where reqType is 'newSearchBB'
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doNewSearchBB(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
        HttpSession session = req.getSession();
        DEC_Bean m_DEC = (DEC_Bean)session.getAttribute("m_DEC");
        if (m_DEC == null) m_DEC = new DEC_Bean();
        EVS_Bean m_OC = new EVS_Bean();
        EVS_Bean m_PC = new EVS_Bean();
        EVS_Bean m_OCQ = new EVS_Bean();
        EVS_Bean m_PCQ = new EVS_Bean();
        GetACService getAC = new GetACService(req, res, this);
        m_setAC.setDECValueFromPage(req, res, m_DEC);
        session.setAttribute("m_DEC", m_DEC);
 
        String searchComp = (String)req.getParameter("searchComp");
        if(searchComp.equals("ObjectClass"))
           session.setAttribute("creSearchAC", "ObjectClass");
        else if(searchComp.equals("Property"))
           session.setAttribute("creSearchAC", "Property");
        else if(searchComp.equals("ObjectQualifier"))
           session.setAttribute("creSearchAC", "ObjectQualifier");
        else if(searchComp.equals("PropertyQualifier"))
           session.setAttribute("creSearchAC", "PropertyQualifier");
        ForwardJSP(req, res, "/CreateDECPage.jsp");
   }
 
   /**
    * to get reference documents for the selected ac and doc type
    * called when the reference docuemnts window opened first time and calls 'getAC.getReferenceDocuments'
    * forwards page back to reference documents
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    */
    private void doRefDocSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       GetACSearch getAC = new GetACSearch(req, res, this);
       String acID = req.getParameter("acID");
       String itemType = req.getParameter("itemType");
       getAC.doRefDocSearch(acID, itemType);
       ForwardJSP(req, res, "/ReferenceDocumentWindow.jsp");      
    }
    
   /**
    * to get alternate names for the selected ac and doc type
    * called when the alternate names window opened first time and calls 'getAC.getAlternateNames'
    * forwards page back to alternate name window jsp
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    */
    private void doAltNameSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       GetACSearch getAC = new GetACSearch(req, res, this);
       String acID = req.getParameter("acID");
       String CD_ID = req.getParameter("CD_ID");
       if(CD_ID == null) CD_ID = "";
       String itemType = req.getParameter("itemType");
       if (itemType != null && itemType.equals("ALL")) itemType = "";
       getAC.doAltNameSearch(acID, itemType, CD_ID, "other");
       ForwardJSP(req, res, "/AlternateNameWindow.jsp");      
    }
    
   /**
    * to get Permissible Values for the selected ac 
    * called when the permissible value window opened first time and calls 'getAC.doPVACSearch'
    * forwards page back to Permissible Value window jsp
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    */
    private void doPermValueSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       GetACSearch getAC = new GetACSearch(req, res, this);
       String acID = req.getParameter("acID");
       String acName = req.getParameter("itemType");  //ac name for pv
       if (acName != null && acName.equals("ALL")) acName = "-";
       String sConteIdseq = (String)req.getParameter("sConteIdseq");
       if (sConteIdseq == null) sConteIdseq = "";
       Integer pvCount = getAC.doPVACSearch(acID, acName, "Detail");
       ForwardJSP(req, res, "/PermissibleValueWindow.jsp");      
    }
    
   /**
    * to get Protocol CRF for the selected ac 
    * called when the ProtoCRF window opened first time and calls 'getAC.doProtoCRFSearch'
    * forwards page back to ProtoCRFwindow jsp
    * @param req The HttpServletRequest from the client
    * @param res The HttpServletResponse back to the client
    *
    * @throws Exception
    */
    private void doProtoCRFSearchActions(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
       GetACSearch getAC = new GetACSearch(req, res, this);
       String acID = req.getParameter("acID");
       String acName = req.getParameter("itemType");  //ac name for proto crf
       if (acName != null && acName.equals("ALL")) acName = "-";
       Integer pvCount = getAC.doProtoCRFSearch(acID, acName);
       ForwardJSP(req, res, "/ProtoCRFWindow.jsp");      
    }
    
   /**
   * To do edit/create from template/new version of a component, clear all records or to display only selected rows after the serach.
   * Called from 'service' method where reqType is 'showResult'.
   * calls 'getACSearch.getSelRowToEdit' method when the action is a edit/create from template/new version.
   * if user doesn't have write permission to edit/create new version forwards the page back to SearchResultsPage.jsp with an error message.
   * For edit, forwards the edit page for the selected component.
   * For new Version/create new from template forwards the create page for the selected component.
   * calls 'getACSearch.getACShowResult' method when the action is show only selected rows and forwards JSP 'SearchResultsPage.jsp'.
   * forwards the page 'SearchResultsPage.jsp' with empty result vector if action is clear records.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doSearchResultsAction(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
     HttpSession session = req.getSession();
     String actType = (String)req.getParameter("actSelected");
     String sSearchAC = (String)session.getAttribute("searchAC");   //get the selected component
     String sAction = (String)req.getParameter("newCDEPageAction");
     session.setAttribute("originAction","");
 
     //get the sort string parameter
     String sSortType = "";
     if (actType == null)
        sSortType = (String)req.getParameter("sortType");
     //sort the header click
     if (actType.equals("sort"))
        doSortACActions(req, res);
 
     //edit selection button from search results page
     else if (actType.equals("Edit") )
        doSearchSelectionAction(req, res);
 
     //edit selection button from search results page
     else if (actType.equals("BlockEdit") )
        doSearchSelectionBEAction(req, res);
 
     //open the designate de page
     else if (actType.equals("EditDesignateDE"))
       this.doEditDesDEActions(req, res, "Open");
 
     //store empty result vector in the attribute
     else if (actType.equals("clearRecords"))
     {
       Vector vResult = new Vector();
       session.setAttribute("results", vResult);
       session.setAttribute("vSelRows", vResult);
       session.setAttribute("CheckList", vResult);
       session.setAttribute("AppendAction", "Not Appended");
       req.setAttribute("recsFound", "No ");
       session.setAttribute("serKeyword", "");
       session.setAttribute("serProtoID", "");
       session.setAttribute("LastAppendWord", "");
       ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
 
     //use permissible value for selected crf value
     else if (actType.equals("usePVforCRFValue"))
     {
        PV_Bean m_PV = new PV_Bean();
        doRefreshPVSearchPage(req, res, m_PV, "Search");
     }
     //forward to create pv page for a new value
     else if (actType.equals("createNewValue"))
     {
        session.setAttribute("originAction", "CreateInSearch");
        //store the selected crf value in pv value bean to create new one.
        String selCRFValue = (String)session.getAttribute("selQCValueName");
        if (selCRFValue != null)
        {
           PV_Bean PVBean = new PV_Bean();
           PVBean.setPV_VALUE(selCRFValue);
           session.setAttribute("m_PV", PVBean);
        }
 
        GetACService getAC = new GetACService(req, res, this);  //
        getAC.getACValueMeaningList(req, res);
        ForwardJSP(req, res, "/CreatePVSearchPage.jsp");
     }
     //get Associate AC
     else if (actType.equals("AssocDEs") || actType.equals("AssocDECs") || actType.equals("AssocVDs"))
        doGetAssociatedAC(req, res, actType, sSearchAC);
     else if (sAction.equals("backFromGetAssociated"))
     {
       session.setAttribute("backFromGetAssociated", "backFromGetAssociated");
       session.setAttribute("CheckList", null);
       session.setAttribute("LastAppendWord", "");
       session.setAttribute("serProtoID", "");
       ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
     else
     {  //show selected rows only.
       GetACSearch getACSearch = new GetACSearch(req, res, this);
       getACSearch.getACShowResult(req, res, actType);
       String menuAction = (String)session.getAttribute("MenuAction");
       if (menuAction.equals("searchForCreate"))
          ForwardJSP(req, res, "/OpenSearchWindow.jsp");
       else
          ForwardJSP(req, res, "/SearchResultsPage.jsp");
     }
  }
 
    /**
   * The doRefreshPVSearchPage method forwards crfValue search page with refreshed list
   * updates the quest value bean with searched/created pv data,
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param m_PV PV_Bean searched/created pv data
   * @param origin String origin of the action
   *
   * @throws Exception
   */
    public void doRefreshPVSearchPage(HttpServletRequest req, HttpServletResponse res,
            PV_Bean m_PV, String Origin)  throws Exception
    {
       HttpSession session = req.getSession();
 
       if (Origin.equals("CreateNew"))
       {
          session.setAttribute("statusMessage", "Value created and inserted successfully.");
          Vector vNewPV = new Vector();
          vNewPV.addElement(m_PV.getPV_PV_IDSEQ());
          vNewPV.addElement(m_PV.getPV_VALUE());
          vNewPV.addElement(m_PV.getPV_SHORT_MEANING());
          req.setAttribute("newPVData", vNewPV);
       }
       //get the selected pv data from the request
       else
       {
           //using designation hidden fields to get the selected value & meanings
          String sPVID = (String)req.getParameter("desName");
          if (sPVID != null) m_PV.setPV_PV_IDSEQ(sPVID);
          String sPValue= (String)req.getParameter("desContext");
          if (sPValue != null) m_PV.setPV_VALUE(sPValue);
          String sPVMean = (String)req.getParameter("desContextID");
          if (sPVMean != null) m_PV.setPV_SHORT_MEANING(sPVMean);
       }
       //forwards the page to regular pv search if not questions
       String sMenuAction = (String)session.getAttribute("MenuAction");
       if (Origin.equals("CreateNew") && !sMenuAction.equals("Questions"))
          ForwardJSP(req, res, "/OpenSearchWindow.jsp");
       else
       {
          //get the selected crf value and update its attribute with the selected/created pvvalue
          String selCRFValue = (String)session.getAttribute("selCRFValueID");
          if (selCRFValue != null)
          {
             //get the crf value vector to update
             Vector vQuestValue = (Vector)session.getAttribute("vQuestValue");
             if (vQuestValue != null)
             {
                for (int i=0; i<(vQuestValue.size()); i++)
                {
                    Quest_Value_Bean QuestValueBean = new Quest_Value_Bean();
                    QuestValueBean = (Quest_Value_Bean)vQuestValue.elementAt(i);
                   //update the quest bean with the new value meaning
                   if (QuestValueBean.getQUESTION_VALUE_IDSEQ().equalsIgnoreCase(selCRFValue))
                   {
                      QuestValueBean.setPERM_VALUE_IDSEQ(m_PV.getPV_PV_IDSEQ());
                      QuestValueBean.setPERMISSIBLE_VALUE(m_PV.getPV_VALUE());
                      QuestValueBean.setVALUE_MEANING(m_PV.getPV_SHORT_MEANING());
                      break;
                   }
                }
                session.setAttribute("vQuestValue", vQuestValue);
             }
          }
          ForwardJSP(req, res, "/CRFValueSearchWindow.jsp");
       }
   }
 
    /**
   * gets the selected row from the search result to forward the data.
   * Called from 'doSearchResultsAction' method where actType is 'edit'
   * calls 'getACSearch.getSelRowToEdit' method to get the row bean.
   * if user doesn't have permission to write to the selected context goes back to search page.
   * otherwise forwards to create/edit pages for the selected component.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doSearchSelectionAction(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
      HttpSession session = req.getSession();
      //gets the bean for the row selected
      GetACSearch getACSearch = new GetACSearch(req, res, this);
      
      if (getACSearch.getSelRowToEdit(req, res, "") == false)
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
      else
      {
         String sMenuAction = (String)session.getAttribute("MenuAction");   //get the selected menu action
         String sSearchAC = (String)session.getAttribute("searchAC");  //get the selected component
         String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
         String sOriginAction = (String)session.getAttribute("sOriginAction");
           //call method to handle DE actions.
         if (sSearchAC.equals("DataElement"))
           this.doSerSelectActDE(req, res);
         else if (sSearchAC.equals("DataElementConcept"))
           this.doSerSelectActDEC(req, res);
         else if (sSearchAC.equals("ValueDomain"))
           this.doSerSelectActVD(req, res);
         else if (sSearchAC.equals("Questions"))
         {
           //session.setAttribute("statusMessage", "");
           //get status indicatior from the quest bean
           Quest_Bean QuestBean = (Quest_Bean)session.getAttribute("m_Quest");
           String sStatus = QuestBean.getSTATUS_INDICATOR();
 
           //forward the page to createDE  if new or existing or to edit page if edit
           if (sStatus.equals("Edit"))
              ForwardJSP(req, res, "/EditDEPage.jsp");
           else
              ForwardJSP(req, res, "/CreateDEPage.jsp");
         }
         else
         {
             session.setAttribute("statusMessage", "Unable to open the Create or Edit page.\n" +
                  "Please try again.");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
      }
   }
 
   /**
    * get the defintion property from the setting
    * @return 
    */
   public String getPropertyDefinition()
   {
     return m_settings.getProperty("DEDefinition");
   }
   /**
   * does the search selection action for the Data element search
   * forward the page according to the action
   * 
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doSerSelectActDE(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
       HttpSession session = req.getSession();
       String sMenuAction = (String)session.getAttribute("MenuAction");   //get the selected menu action
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
       //make sure the menu action is for DE, set it otherwise
       if (sMenuAction.equalsIgnoreCase("NewDECTemplate") || (sMenuAction.equalsIgnoreCase("NewVDTemplate")))
           sMenuAction = "NewDETemplate";
       else if (sMenuAction.equalsIgnoreCase("NewDECVersion") || (sMenuAction.equalsIgnoreCase("NewVDVersion")))
           sMenuAction = "NewDEVersion";
       else if (sMenuAction.equalsIgnoreCase("editDEC") || (sMenuAction.equalsIgnoreCase("editVD")))
           sMenuAction = "editDE";
       //set the menuaction session attribute
       session.setAttribute("MenuAction", sMenuAction);
 
        //forward to create DE page if template or version
       if ((sMenuAction.equals("NewDETemplate")) || (sMenuAction.equals("NewDEVersion")))
       {
         //session.setAttribute("statusMessage", "");
         session.setAttribute("sCDEAction", "validate");
         ForwardJSP(req, res, "/CreateDEPage.jsp");
         session.setAttribute("originAction", "NewDE");
       }
       //forward to edit DE page if editing
       else if (sMenuAction.equals("editDE") || sMenuAction.equals("nothing"))
       {
         session.setAttribute("originAction", "EditDE");
         ForwardJSP(req, res, "/EditDEPage.jsp");
       }
       else if (sButtonPressed.equals("Search"))
       {
         session.setAttribute("originAction", "EditDE");
         ForwardJSP(req, res, "/EditDEPage.jsp");
       }
       else
       {
         session.setAttribute("originAction", "EditDE");
         session.setAttribute("statusMessage", "Unable to open the Create or Edit page.\\n" +
              "Please try again.");
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
       }    
   }
 
   /**
   * does the search selection action for the Data element concept search
   * forward the page according to the action
   * 
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doSerSelectActDEC(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
       HttpSession session = req.getSession();
       String sMenuAction = (String)session.getAttribute("MenuAction");   //get the selected menu action
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
        //make sure the menu action is for DEC, set it otherwise
        if (sMenuAction.equalsIgnoreCase("NewDETemplate") || (sMenuAction.equalsIgnoreCase("NewVDTemplate")))
           sMenuAction = "NewDECTemplate";
        else if (sMenuAction.equalsIgnoreCase("NewDEVersion") || (sMenuAction.equalsIgnoreCase("NewVDVersion")))
           sMenuAction = "NewDECVersion";
        else if (sMenuAction.equalsIgnoreCase("editDE") || (sMenuAction.equalsIgnoreCase("editVD")))
           sMenuAction = "editDEC";
 
        //set the menuaction session attribute
        session.setAttribute("MenuAction", sMenuAction);
 
       //forward to create DEC page if template or version
       if ((sMenuAction.equals("NewDECTemplate")) || (sMenuAction.equals("NewDECVersion")))
       {
         session.setAttribute("DECPageAction", "validate");
         ForwardJSP(req, res, "/CreateDECPage.jsp");
         //session.setAttribute("originAction", "NewDEC");
       }
       //forward to edit DEC page if editing
       else if (sMenuAction.equals("editDEC") || sMenuAction.equals("nothing"))
       {
         session.setAttribute("originAction", "EditDEC");
         ForwardJSP(req, res, "/EditDECPage.jsp");
       }
       else if (sButtonPressed.equals("Search"))
       {
         session.setAttribute("originAction", "EditDEC");
         ForwardJSP(req, res, "/EditDECPage.jsp");
       }
        else
       {
         session.setAttribute("originAction", "EditDEC");
         session.setAttribute("statusMessage", "Unable to open the Create or Edit page.\\n" +
              "Please try again.");
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
       }
   }
 
   /**
   * does the search selection action for the Value Domain search
   * forward the page according to the action
   * 
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doSerSelectActVD(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
       HttpSession session = req.getSession();
       String sMenuAction = (String)session.getAttribute("MenuAction");   //get the selected menu action
       String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
        //make sure the menu action is for DE, set it otherwise
        if (sMenuAction.equalsIgnoreCase("NewDETemplate") || (sMenuAction.equalsIgnoreCase("NewDECTemplate")))
           sMenuAction = "NewVDTemplate";
        else if (sMenuAction.equalsIgnoreCase("NewDEVersion") || (sMenuAction.equalsIgnoreCase("NewDECVersion")))
           sMenuAction = "NewVDVersion";
        else if (sMenuAction.equalsIgnoreCase("editDE") || (sMenuAction.equalsIgnoreCase("editDEC")))
           sMenuAction = "editVD";
 
        //set the menuaction session attribute
        session.setAttribute("MenuAction", sMenuAction);
 
        //forward to create VD page if template or version
       if ((sMenuAction.equals("NewVDTemplate")) || (sMenuAction.equals("NewVDVersion")))
       {
         session.setAttribute("VDPageAction", "validate");
         ForwardJSP(req, res, "/CreateVDPage.jsp");
         session.setAttribute("originAction", "NewVD");
       }
       //forward to edit VD page if editing
       else if (sMenuAction.equals("editVD") || sMenuAction.equals("nothing"))
       {
         session.setAttribute("originAction", "EditVD");
         ForwardJSP(req, res, "/EditVDPage.jsp");
       }
       else if (sButtonPressed.equals("Search"))
       {
         session.setAttribute("originAction", "EditVD");
         ForwardJSP(req, res, "/EditVDPage.jsp");
       }
       else
       {
         session.setAttribute("originAction", "EditVD");
         session.setAttribute("statusMessage", "Unable to open the Create or Edit page.\\n" +
              "Please try again.");
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
       }
   }
    /**
   * gets the selected row from the search result to forward the data.
   * Called from 'doSearchResultsAction' method where actType is 'edit'
   * calls 'getACSearch.getSelRowToEdit' method to get the row bean.
   * if user doesn't have permission to write to the selected context goes back to search page.
   * otherwise forwards to create/edit pages for the selected component.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doSearchSelectionBEAction(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
      HttpSession session = req.getSession();
      //gets the bean for the row selected
      GetACSearch getACSearch = new GetACSearch(req, res, this);
      if (getACSearch.getSelRowToEdit(req, res, "BlockEdit") == false)
         ForwardJSP(req, res, "/SearchResultsPage.jsp");
      else
      {
         String sMenuAction = (String)session.getAttribute("MenuAction");   //get the selected menu action
         String sSearchAC = (String)session.getAttribute("searchAC");  //get the selected component
         String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
         if (sSearchAC.equals("DataElement"))
         { //open the edit page
           DE_Bean DEBean = (DE_Bean)session.getAttribute("m_DE");
           DEBean.setDE_DEC_IDSEQ("");
           DEBean.setDE_DEC_NAME("");
           DEBean.setDE_VD_IDSEQ("");
           DEBean.setDE_VD_NAME("");
           session.setAttribute("m_DE", DEBean);  //users need cs-csi to view
 
           session.setAttribute("originAction", "BlockEditDE");
           session.setAttribute("DEEditAction", "");
           ForwardJSP(req, res, "/EditDEPage.jsp");
         }
         else if (sSearchAC.equals("DataElementConcept"))
         { 
         //  DEC_Bean DECBean = (DEC_Bean)session.getAttribute("m_DEC");
           session.setAttribute("originAction", "BlockEditDEC");
           session.setAttribute("vObjectClass", null);
           session.setAttribute("newObjectClass", "");
           session.setAttribute("vProperty", null);
           session.setAttribute("newProperty", "");
           
           ForwardJSP(req, res, "/EditDECPage.jsp");
         }
         else if (sSearchAC.equals("ValueDomain"))
         { 
         //  VD_Bean VDBean = (VD_Bean)session.getAttribute("m_VD");
           session.setAttribute("vRepTerm", null);
           session.setAttribute("newRepTerm", "");
           session.setAttribute("originAction", "BlockEditVD");
           ForwardJSP(req, res, "/EditVDPage.jsp");
         }
         else
         {
             session.setAttribute("statusMessage", "Unable to open the Create or Edit page.\\n" +
                  "Please try again.");
             ForwardJSP(req, res, "/SearchResultsPage.jsp");
         }
      }
   }
 
   /**
   * to display the selected elements for block edit, opened from create/edit pages.
   * 
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   private void doDisplayWindowBEAction(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
     HttpSession session = req.getSession();
     GetACSearch getACSearch = new GetACSearch(req, res, this);
     String sMenu = (String)session.getAttribute("MenuAction");
     session.setAttribute("MenuAction", "BEDisplay");   //set the menu to BEDisplay to get the results properly
     getACSearch.getACShowResult(req, res, "BEDisplayRows");
     session.setAttribute("BEDisplaySubmitted", "true");
     session.setAttribute("MenuAction", sMenu);   //set the menu back to way it was
     ForwardJSP(req, res, "/OpenBlockEditWindow.jsp");
   }
 
   /**
   * gets the selected row from the search result to forward the data.
   * Called from 'doSearchResultsAction' method where actType is 'AssocDEs', AssocDECs or AssocVDs
   * gets the index and ID/Names from the session attributes to get the row bean.
   * calls 'getACSearch.getAssociatedDESearch', 'getACSearch.getAssociatedDECSearch', or 'getACSearch.getAssociatedVDSearch' method
   * to get search associated results depending actType.
   * calls 'getACSearch.getDEResult', 'getACSearch.getDECResult', or 'getACSearch.getVDResult' method
   * to get final result vector which is stored in the session.
   * resets default attributes and other session attributes
   * forwards to SearchResultsPage to display the search results.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param assocAC String actType of the search result page.
   * @param sSearchAC String search type from the drop down list.
   *
   * @throws Exception
   */
   private void doGetAssociatedAC(HttpServletRequest req, HttpServletResponse res, String assocAC, String sSearchAC)
   throws Exception
   {
      HttpSession session = req.getSession();
      int thisInd = 0;
      //get the searched ID and Name vectors
      Vector vIDs = (Vector)session.getAttribute("SearchID");
      Vector vNames = (Vector)session.getAttribute("SearchName");
      Vector oldVResult = (Vector)session.getAttribute("results");
      //get the selected row index from the hidden field.
      String sID = "";
      String sName = "";
      //convert the string to integer and to int.
      Integer curInd = new Integer((String)req.getParameter("hiddenSelectedRow"));
      if (curInd != null)
       thisInd = curInd.intValue();
      if (vIDs != null && !vIDs.equals("") && vIDs.size()>0 && (thisInd < vIDs.size()))
      {
         sID = (String)vIDs.elementAt(thisInd);
         sName = (String)vNames.elementAt(thisInd);
      }
      if (sID != null && !sID.equals(""))
      {
         //reset the default attributes
         Vector vSelVector = new Vector();
         String sSearchIn = (String)session.getAttribute("serSearchIn");
 
         GetACSearch getACSearch = new GetACSearch(req, res, this);
         Vector vResult = new Vector();
         String oldSearch = "";
         String newSearch = "";
         String retCode = "";
         String pvID = "", cdID = "", deID = "", decID = "", vdID = "", cscsiID = "", ocID = "", propID = ""; 
         //get the search results from the database.
         if (assocAC.equals("AssocDEs"))
         {
            req.setAttribute("GetAssocSearchAC", "true");
           // retCode = getACSearch.doAssociatedDESearch(sID, sSearchAC);
            if (sSearchAC.equals("PermissibleValue")) pvID = sID;
            else if (sSearchAC.equals("DataElementConcept")) decID = sID;
            else if (sSearchAC.equals("ValueDomain")) vdID = sID;
            else if (sSearchAC.equals("ConceptualDomain")) cdID = sID;
            else if (sSearchAC.equals("ClassSchemeItems")) cscsiID = sID;
            //do the search only if id is not null
            Vector vRes = new Vector();
            if (sID != null && !sID.equals(""))
               getACSearch.doDESearch("", "", "","","","", 0, "", "", "", "", "", "", "", "", "", 
                       "", "", "", "", "", "", "", pvID, vdID, decID, cdID, cscsiID, vRes);
            session.setAttribute("vSelRows", vRes);
            // do attributes after the search so no "two simultaneous request" errors
            vSelVector = this.getDefaultAttr("DataElement", sSearchIn);
            session.setAttribute("selectedAttr", vSelVector);        
            getCompAttrList(req, res, "DataElement", "nothing");          
           // if (retCode.equals("0"))
            getACSearch.getDEResult(req, res, vResult, "");   
            session.setAttribute("searchAC", "DataElement");
            newSearch = "Data Element";
         }
         else if (assocAC.equals("AssocDECs"))
         {
            req.setAttribute("GetAssocSearchAC", "true");
           // retCode = getACSearch.doAssociatedDECSearch(sID, sSearchAC);
            if (sSearchAC.equals("ObjectClass")) ocID = sID;
            else if (sSearchAC.equals("Property")) propID = sID;
            else if (sSearchAC.equals("DataElement")) deID = sID;
            else if (sSearchAC.equals("ConceptualDomain")) cdID = sID;
            Vector vRes = new Vector();
            getACSearch.doDECSearch("", "", "", "", "", "", "", "", "", "", "", "", "", ocID, propID, "", 0, cdID, deID, vRes);
            session.setAttribute("vSelRows", vRes);
            // do attributes after the search so no "two simultaneous request" errors
            vSelVector = this.getDefaultAttr("DataElementConcept", sSearchIn);
            session.setAttribute("selectedAttr", vSelVector);
            getCompAttrList(req, res, "DataElementConcept", "nothing");
            //if (retCode.equals("0"))
               getACSearch.getDECResult(req, res, vResult, "");
            session.setAttribute("searchAC", "DataElementConcept");
            newSearch = "Data Element Concept";
         }
         else if (assocAC.equals("AssocVDs"))
         {
            req.setAttribute("GetAssocSearchAC", "true");
            if (sSearchAC.equals("PermissibleValue")) pvID = sID;
            else if (sSearchAC.equals("DataElement")) deID = sID;
            else if (sSearchAC.equals("ConceptualDomain")) cdID = sID;
            Vector vRes = new Vector();
            getACSearch.doVDSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, cdID, pvID, deID, vRes);
            session.setAttribute("vSelRows", vRes);
            // do attributes after the search so no "two simultaneous request" errors
            vSelVector = this.getDefaultAttr("ValueDomain", sSearchIn);
            session.setAttribute("selectedAttr", vSelVector);
            getCompAttrList(req, res, "ValueDomain", "nothing");
            getACSearch.getVDResult(req, res, vResult, "");
            session.setAttribute("searchAC", "ValueDomain");
            newSearch = "Value Domain";
         }
 
         //get the old search for the label
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
         //make keyword empty and label for search result page.
         session.setAttribute("serKeyword", "");
         String labelWord = "";
         labelWord = " associated with " + oldSearch + " - " + sName;   //make the label
         req.setAttribute("labelKeyword", newSearch + labelWord);   //make the label
         //save the last word in the request attribute
         session.setAttribute("LastAppendWord", labelWord);
         session.setAttribute("results", vResult);      //store result vector in the attribute
         Vector vCheckList = new Vector();
         session.setAttribute("CheckList", vCheckList); //empty the check list in the new search when not appended.
         session.setAttribute("backFromGetAssociated", ""); 
      }
      //couldnot find a id, go back to search results
      else
      {
         session.setAttribute("statusMessage", "Unable to determine the ID of the selected item. ");
      }
      ForwardJSP(req, res, "/SearchResultsPage.jsp");
   }
 
 
   /**
   * To sort the search results by clicking on the column heading.
   * Called from 'service' method where reqType is 'doSortCDE'
   * calls 'getACSearch.getACSortedResult' method and forwards page 'SearchResultsPage.jsp'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doSortACActions(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
        HttpSession session = req.getSession();
        GetACSearch getACSearch = new GetACSearch(req, res, this);
        getACSearch.getACSortedResult(req, res);
        String sMenuAction = (String)session.getAttribute("MenuAction");   //get the selected menu action
 
        if (sMenuAction.equals("searchForCreate"))
           ForwardJSP(req, res, "/OpenSearchWindow.jsp");
        else
           ForwardJSP(req, res, "/SearchResultsPage.jsp");
   }
 
   /**
   * To sort the search results of the blocks by clicking on the column heading.
   * Called from 'service' method where reqType is 'doSortBlocks'
   * calls 'getACSearch.getBlocksSortedResult' method and
   * forwards page 'OpenSearchWindowBlocks.jsp' or 'OpenSearchWindowQualifiers.jsp'
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doSortBlockActions(HttpServletRequest req, HttpServletResponse res, String ACType)
   throws Exception
   {
        HttpSession session = req.getSession();
        String actType = (String)req.getParameter("actSelected");
        String sComp = (String)req.getParameter("searchComp");
 //System.out.println("doSortBlocks sComp: " + sComp + " actType: " + actType);
        String sSelectedParentCC = (String)req.getParameter("selectedParentConceptCode");
        if(sSelectedParentCC == null) sSelectedParentCC = "";
        String sSelectedParentName = (String)req.getParameter("selectedParentConceptName"); 
        if(sSelectedParentName == null) sSelectedParentName = "";
        String sSelectedParentDB = (String)req.getParameter("selectedParentConceptDB");
        if(sSelectedParentDB == null) sSelectedParentDB = "";
        String sSelectedParentMetaSource = (String)req.getParameter("selectedParentConceptMetaSource");
        if(sSelectedParentMetaSource == null) sSelectedParentMetaSource = "";
 
        session.setAttribute("ParentMetaSource", sSelectedParentMetaSource);
        if (actType.equals("FirstSearch"))
        { 
           if(sComp.equals("ParentConceptVM"))
           {
             session.setAttribute("SelectedParentCC", sSelectedParentCC);
             session.setAttribute("SelectedParentDB", sSelectedParentDB);
             session.setAttribute("SelectedParentMetaSource", sSelectedParentMetaSource);
             session.setAttribute("SelectedParentName", sSelectedParentName);
           }
           getCompAttrList(req, res, sComp, "searchForCreate");
           session.setAttribute("creContext", "");
           session.setAttribute("creSearchAC", sComp);
           ForwardJSP(req, res, "/OpenSearchWindowBlocks.jsp");
        }
        else if(actType.equals("showConceptInTree"))
        {
           session.setAttribute("creSearchAC", sComp);
          
           String sCCodeDB = (String)req.getParameter("OCCCodeDB");
           String sCCode = (String)req.getParameter("OCCCode");
           String sCCodeName = (String)req.getParameter("OCCCodeName");
           if(!sCCode.equals("") && !sComp.equals("ParentConcept"))
           {
             if(sCCodeDB.equals("NCI Thesaurus"))  sCCodeDB = "Thesaurus/Metathesaurus";
             this.doCollapseAllNodes(req, sCCodeDB);
             if(sComp.equals("ParentConceptVM"))
             {
               this.doTreeOpenToConcept(req, res, "OpenParentTreeToConcept", "Blocks", sCCode, sCCodeDB, sCCodeName);      
             }
             else
               this.doTreeOpenToConcept(req, res, "tree", "Blocks", sCCode, sCCodeDB, sCCodeName);
             return;
           }
           else if(!sComp.equals("ParentConcept"))
           {
             this.doCollapseAllNodes(req, sCCodeDB);
             this.doTreeSearch(req, res, actType, "Blocks");
           }
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
   * To open search page after login or click search on the menu.
   * Called from 'service' method where reqType is 'getSearchFilter'
   * Adds default attributes to 'selectedAttr' session vector.
   * Makes empty 'results' session vector.
   * stores 'No ' to 'recsFound' session attribute.
   * forwards page 'CDEHomePage.jsp'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doOpenSearchPage(HttpServletRequest req,
     HttpServletResponse res) throws Exception
   {
     HttpSession session = req.getSession();
     Vector vMsg = new Vector();
     vMsg.addElement("first Object");
     vMsg.addElement("second Object");
     vMsg.addElement("third Object");
     session.setAttribute("vStatMsg", vMsg);
     session.setAttribute("MenuAction", "nothing");
     session.setAttribute("LastMenuButtonPressed", "Search");
     Vector vDefaultAttr = new Vector();
     String searchAC = (String)session.getAttribute("searchAC");
     if (searchAC == null) searchAC = "DataElement";
     //make the default to longName if not Questions
     String sSearchIn = (String)session.getAttribute("serSearchIn");
     if ((sSearchIn == null) || (!searchAC.equals("Questions")))
         sSearchIn = "longName";
     session.setAttribute("serSearchIn", sSearchIn);
 
     vDefaultAttr = getDefaultAttr(searchAC, sSearchIn);  //default display attributes  
     session.setAttribute("selectedAttr", vDefaultAttr);
 
     this.getDefaultFilterAtt(req, res);   //default filter by attributes
 
     doInitDDEInfo(req, res);
     clearSessionAttributes(req, res);
     //call the method to get attribute list for the selected AC
     getCompAttrList(req, res, searchAC, "nothing");
     ForwardJSP(req, res, "/CDEHomePage.jsp");
  }
 
   /**
   * To clear session attributes when a main Menu button/item is selected.
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @throws Exception
   */
   public void clearSessionAttributes(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
         HttpSession session = req.getSession();
         session.setAttribute("vSearchIDStack", null);
         session.setAttribute("SearchID", null);
         session.setAttribute("vSearchNameStack", null);
         session.setAttribute("SearchName", null);
         session.setAttribute("sSearchACStack", null);
         session.setAttribute("vACSearchStack", null);
         session.setAttribute("vSearchASLStack", null);
         session.setAttribute("vACSearch", null);
         session.setAttribute("vSelRowsStack", null);
         session.setAttribute("vResultStack", null);
         session.setAttribute("vCompAttrStack", null);
         session.setAttribute("backFromGetAssociated", "");
         session.setAttribute("GetAssocSearchAC", ""); 
         session.setAttribute("results", null);
         session.setAttribute("vSelRows", null);
         session.setAttribute("selCS", "");
         session.setAttribute("serSelectedCD", "");
         //parent concept for the VD
         session.setAttribute("VDParentConcept", new Vector());
         session.setAttribute("vParentList", null);
         session.setAttribute("vParentNames", null);
         session.setAttribute("vParentCodes", null);
         session.setAttribute("vParentDB", null);
         session.setAttribute("vParentMetaSource", null);
         session.setAttribute("SelectedParentName", "");
         session.setAttribute("SelectedParentCC", "");
         session.setAttribute("SelectedParentDB", "");
         session.setAttribute("ParentMetaSource", null);
         //pv list for the vd
         session.setAttribute("VDPVList", new Vector());
         session.setAttribute("PVIDList", new Vector());
         session.setAttribute("m_OC", null);
         session.setAttribute("selObjRow", null);
         session.setAttribute("m_PC", null);
         session.setAttribute("selPropRow", null);
         session.setAttribute("vObjectClass", null);
         session.setAttribute("vProperty", null);
         session.setAttribute("m_DEC", null);
         session.setAttribute("m_REP", null);
         session.setAttribute("selRepRow", null);
         
         session.setAttribute("m_OCQ", null);
         session.setAttribute("selObjQRow", null);
         session.setAttribute("m_PCQ", null);
         session.setAttribute("selPropQRow", null);
         session.setAttribute("m_REPQ", null);
         session.setAttribute("selRepQRow", null); 
         session.setAttribute("creKeyword", "");
         session.setAttribute("serKeyword", "");
         session.setAttribute("EVSresults", null);
   }
   
   /**
   * To clear session attributes when a main Menu button/item is selected.
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @throws Exception
   */
   public void clearBuildingBlockSessionAttributes(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
         HttpSession session = req.getSession();
         session.setAttribute("m_OC", null);
         session.setAttribute("selObjRow", null);
         session.setAttribute("m_PC", null);
         session.setAttribute("selPropRow", null);
         session.setAttribute("vObjResults", null);
         session.setAttribute("vPropResults", null);
         session.setAttribute("vRepResults", null);
         session.setAttribute("m_REP", null);
         session.setAttribute("selRepRow", null); 
         session.setAttribute("vObjQResults", null);
         session.setAttribute("m_OCQ", null);
         session.setAttribute("selObjQRow", null);
         session.setAttribute("vPropQResults", null);
         session.setAttribute("m_PCQ", null);
         session.setAttribute("selPropQRow", null);
         session.setAttribute("vRepQResults", null);
         session.setAttribute("m_REPQ", null);
         session.setAttribute("selRepQRow", null); 
         
         session.setAttribute("vObjectClass", null);
         session.setAttribute("newObjectClass", "");
         session.setAttribute("vProperty", null);
         session.setAttribute("newProperty", "");
         session.setAttribute("vRepTerm", null);
         session.setAttribute("newRepTerm", "");       
         session.setAttribute("ConceptLevel", "0");
         session.setAttribute("creMetaCodeSearch", null);
         session.setAttribute("creKeyword", "");
         session.setAttribute("serKeyword", "");
         session.setAttribute("EVSresults", null);
         session.setAttribute("ParentMetaSource", null);
   }
   
   /**
   * To clear session attributes when a main Menu button/item is selected.
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @throws Exception
   */
   public void clearCreateSessionAttributes(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
         HttpSession session = req.getSession();
         //parent concept for the VD
         session.setAttribute("VDParentConcept", new Vector());
         session.setAttribute("SelectedParentName", "");
         session.setAttribute("SelectedParentCC", "");
         session.setAttribute("SelectedParentDB", "");
         //pv list for the vd
         session.setAttribute("VDPVList", new Vector());
         session.setAttribute("PVIDList", new Vector());
         session.setAttribute("creKeyword", "");
         session.setAttribute("serKeyword", "");
         session.setAttribute("EVSresults", null);
   }
 
   /**
   * To get the default attributes for the selected Component.
   *
   * @param searchAC String The selected Administered component
   * @param sSearchIn String The selected search in filter.
   *
   * @return Vector selected attribute Vector 
   * 
   * @throws Exception
   */
   public Vector getDefaultAttr(String searchAC, String sSearchIn) throws Exception
   {
     Vector vDefaultAttr = new Vector();
     if (searchAC == null) searchAC = "DataElement";
     if (sSearchIn == null)  sSearchIn = "longName";
     //store the default attributes to select and set some default attributes
     if (searchAC.equals("PermissibleValue"))
     {
       vDefaultAttr.addElement("Value");
       vDefaultAttr.addElement("Value Meaning");
       vDefaultAttr.addElement("PV Meaning Description");
       vDefaultAttr.addElement("Conceptual Domain");
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
      else if (searchAC.equals("EVSValueMeaning") || searchAC.equals("ParentConcept")
               || searchAC.equals("PV_ValueMeaning"))
     {
       vDefaultAttr.addElement("Concept Name");
       vDefaultAttr.addElement("EVS Identifier");
       vDefaultAttr.addElement("Definition");
       vDefaultAttr.addElement("Definition Source");
       vDefaultAttr.addElement("Vocabulary");
     }
     else if (searchAC.equals("ParentConceptVM"))
     {
        vDefaultAttr.addElement("Concept Name");
        vDefaultAttr.addElement("EVS Identifier");
        vDefaultAttr.addElement("Definition");
        vDefaultAttr.addElement("Definition Source");
        vDefaultAttr.addElement("Vocabulary");
        vDefaultAttr.addElement("Level");
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
        vDefaultAttr.addElement("EVS Identifier");
        vDefaultAttr.addElement("Definition");
        vDefaultAttr.addElement("Definition Source");
        vDefaultAttr.addElement("Context");
        vDefaultAttr.addElement("Vocabulary");
        vDefaultAttr.addElement("DEC's Using");
     }
     else if (searchAC.equals("ClassSchemeItems"))
     {
        vDefaultAttr.addElement("CSI Name");
        vDefaultAttr.addElement("CSI Type");
        vDefaultAttr.addElement("CSI Definition");
        vDefaultAttr.addElement("CS Long Name");
        vDefaultAttr.addElement("Context");
     }
     else
     {
       vDefaultAttr.addElement("Long Name");
       vDefaultAttr.addElement("Public ID");
       vDefaultAttr.addElement("Version");
       if (searchAC.equals("DataElement"))
         vDefaultAttr.addElement("Registration Status");
       vDefaultAttr.addElement("Workflow Status");
       //only if search is Data element
       if (searchAC.equals("DataElement"))
       {
         vDefaultAttr.addElement("Owned By Context");
         vDefaultAttr.addElement("Used By Context");
       }
       else
         vDefaultAttr.addElement("Context");
       
       vDefaultAttr.addElement("Definition");
       //only if search is Data element
       if (searchAC.equals("DataElement"))
       {
         vDefaultAttr.addElement("Data Element Concept");
         vDefaultAttr.addElement("Value Domain");
       }
     }
     return vDefaultAttr;
  }
  
   /**
   * To get the default filter by attributes for the selected Component.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
  private void getDefaultFilterAtt(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
     HttpSession session = req.getSession();
     String menuAction = (String)session.getAttribute("MenuAction");
 
     //reset to default filter by criteria
     if (!menuAction.equals("searchForCreate"))
     {
         session.setAttribute("serStatus", new Vector());
         session.setAttribute("serMultiContext", new Vector());
         session.setAttribute("serContext", "");   //keep the old context criteria
         session.setAttribute("serContextUse", "");   //store contextUse in the session
         session.setAttribute("serVersion", "");   //store version in the session
         session.setAttribute("serVDTypeEnum", "");   //store VDType Enum in the session
         session.setAttribute("serVDTypeNonEnum", "");   //store VDType Non Enum in the session
         session.setAttribute("serVDTypeRef", "");   //store VDType Ref in the session
         session.setAttribute("serRegStatus", "");   //store regstatus in the session
         session.setAttribute("serCreatedFrom", "");
         session.setAttribute("serCreatedTo", "");
         session.setAttribute("serModifiedFrom", "");
         session.setAttribute("serModifiedTo", "");
         session.setAttribute("serCreator", "");     
         session.setAttribute("serModifier", "");
     
         session.setAttribute("serKeyword", "");
         session.setAttribute("serProtoID", "");
         session.setAttribute("serSearchIn", "longName"); //make default to longName
         session.setAttribute("selCS", "");
         session.setAttribute("serSelectedCD", "");
 
         //reset the appened attributes    
         req.setAttribute("recsFound", "No ");
         session.setAttribute("CheckList", new Vector());
         session.setAttribute("AppendAction", "Not Appended");
         session.setAttribute("vSelRows", new Vector());
         session.setAttribute("results", new Vector());
     }
     else
     {
         session.setAttribute("creStatus", new Vector());
         session.setAttribute("creMultiContext", new Vector());   //keep the old context criteria
         session.setAttribute("creContext", "");   //keep the old context criteria
         session.setAttribute("creContextUse", "");   //store contextUse in the session
         session.setAttribute("creVersion", "");   //store version in the session
         session.setAttribute("creVDTypeEnum", "");   //store VDType Enum in the session
         session.setAttribute("creVDTypeNonEnum", "");   //store VDType Non Enum in the session
         session.setAttribute("creVDTypeRef", "");   //store VDType Ref in the session
         session.setAttribute("creRegStatus", "");   //store regstatus in the session
         session.setAttribute("creCreatedFrom", "");
         session.setAttribute("creCreatedTo", "");
         session.setAttribute("creModifiedFrom", "");
         session.setAttribute("creModifiedTo", "");
         session.setAttribute("creCreator", "");     
         session.setAttribute("creModifier", "");
     
         session.setAttribute("creKeyword", "");
         session.setAttribute("creProtoID", "");
         req.setAttribute("creSearchIn", "longName"); //make default to longName
         session.setAttribute("creSelectedCD", "");
     }
   }
 
   /**
   * To open search page when clicked on edit, create new from template, new version on the menu.
   * Called from 'service' method where reqType is 'actionFromMenu'
   * Sets the attribte 'searchAC' to the selected component.
   * Sets the attribte 'MenuAction' to the selected menu action.
   * Makes empty 'results' session vector.
   * stores 'No ' to 'recsFound' session attribute.
   * forwards page 'SearchResultsPage.jsp'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doMenuAction(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
     HttpSession session = req.getSession();
     clearSessionAttributes(req, res);
     String sMAction = (String)req.getParameter("hidMenuAction");
     if (sMAction == null) sMAction = "nothing";
     String searchAC = "DataElement";
     //sets the session attributes of the selection menu action and selected component
     if (sMAction.equals("editDE") || sMAction.equals("editDEC") || sMAction.equals("editVD"))
       session.setAttribute("LastMenuButtonPressed", "Edit");
     else if (sMAction.equals("NewDETemplate") || sMAction.equals("NewDEVersion") || sMAction.equals("NewDECTemplate") || sMAction.equals("NewDECVersion") || sMAction.equals("NewVDTemplate") || sMAction.equals("NewVDVersion"))
       session.setAttribute("LastMenuButtonPressed", "CreateTemplateVersion");
 
     if ((sMAction == null) || (sMAction.equals("nothing")) || (sMAction.equals("Questions")))
       sMAction = "nothing";
     else
     {
       if ((sMAction.equals("NewDETemplate")) || (sMAction.equals("NewDEVersion")) || (sMAction.equals("editDE")))
         searchAC = "DataElement";
       else if ((sMAction.equals("NewDECTemplate")) || (sMAction.equals("NewDECVersion")) || (sMAction.equals("editDEC")))
         searchAC = "DataElementConcept";
       else if ((sMAction.equals("NewVDTemplate")) || (sMAction.equals("NewVDVersion")) || (sMAction.equals("editVD")))
       {
         searchAC = "ValueDomain";
         session.setAttribute("originAction", "NewVDTemplate");
         session.setAttribute("VDEditAction", "editVD");
         this.clearBuildingBlockSessionAttributes(req, res);
       }
     }
     session.setAttribute("MenuAction", sMAction);
     session.setAttribute("searchAC", searchAC);
     //sets the default attributes and resets to empty result vector
     Vector vResult = new Vector();
     session.setAttribute("results", vResult);
     req.setAttribute("recsFound", "No ");
     session.setAttribute("serKeyword", "");
     session.setAttribute("serProtoID", "");
     session.setAttribute("LastAppendWord", "");
     //set it to longname be default
     String sSearchIn = "longName";
     Vector vSelVector = new Vector();
     //call the method to get default attributes
     vSelVector = getDefaultAttr(searchAC, sSearchIn);    
     session.setAttribute("selectedAttr", vSelVector);
     this.getDefaultFilterAtt(req, res);   //default filter by attributes
     this.getCompAttrList(req, res, searchAC, sMAction);    //call the method to get attribute list for the selected AC
     ForwardJSP(req, res, "/SearchResultsPage.jsp");
  }
 
   /**
   * To get the list of attributes for the selected search component.
   * Called from 'doRefreshPageOnSearchFor', 'doMenuAction', 'doOpenSearchPage' methods
   * stores the vector in the session attribute.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param String selSearch the componenet to search for.
   *
   * @throws Exception
   */
 
  public void getCompAttrList(HttpServletRequest req, HttpServletResponse res,
          String selSearch, String sMenu)  throws Exception
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
        vCompAtt.addElement("Effective Begin Date");
        vCompAtt.addElement("Effective End Date");
        vCompAtt.addElement("Creator");
        vCompAtt.addElement("Date Created");
        vCompAtt.addElement("Modifier");
        vCompAtt.addElement("Date Modified");
        vCompAtt.addElement("Comments/Change Note");
        vCompAtt.addElement("Historical CDE ID");
        vCompAtt.addElement("Permissible Value");
        vCompAtt.addElement("Long Name Document Text");
        vCompAtt.addElement("Historic Short CDE Name Document Text");
        vCompAtt.addElement("Reference Documents");
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
        vCompAtt.addElement("Effective Begin Date");
        vCompAtt.addElement("Effective End Date");
        vCompAtt.addElement("Creator");
        vCompAtt.addElement("Date Created");
        vCompAtt.addElement("Modifier");
        vCompAtt.addElement("Date Modified");
        vCompAtt.addElement("Comments/Change Note");
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
        vCompAtt.addElement("Effective Begin Date");
        vCompAtt.addElement("Effective End Date");
        vCompAtt.addElement("Creator");
        vCompAtt.addElement("Date Created");
        vCompAtt.addElement("Modifier");
        vCompAtt.addElement("Date Modified");
        vCompAtt.addElement("Comments/Change Note");
        vCompAtt.addElement("Unit of Measures");
        vCompAtt.addElement("Display Format");
        vCompAtt.addElement("Maximum Length");
        vCompAtt.addElement("Minimum Length");
        vCompAtt.addElement("High Value Number");
        vCompAtt.addElement("Low Value Number");
        vCompAtt.addElement("Decimal Place");
        vCompAtt.addElement("Type Flag");
        vCompAtt.addElement("Permissible Value");
        vCompAtt.addElement("All Attributes");
     }
     else if (selSearch.equals("PermissibleValue"))
     {
        vCompAtt.addElement("Value");
        vCompAtt.addElement("Value Meaning");
        vCompAtt.addElement("PV Meaning Description");
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
        session.setAttribute("creSelectedAttr", vCompAtt);
     }
      else if (selSearch.equals("EVSValueMeaning") || selSearch.equals("ParentConcept")
      || selSearch.equals("CreateVM_EVSValueMeaning") || selSearch.equals("PV_ValueMeaning"))
     {
        vCompAtt.addElement("Concept Name");
        vCompAtt.addElement("EVS Identifier");
        vCompAtt.addElement("Definition");
        vCompAtt.addElement("Definition Source");
        vCompAtt.addElement("Vocabulary");
        session.setAttribute("creSelectedAttr", vCompAtt);
     }
     else if (selSearch.equals("ParentConceptVM"))
     {
        vCompAtt.addElement("Concept Name");
        vCompAtt.addElement("EVS Identifier");
        vCompAtt.addElement("Definition");
        vCompAtt.addElement("Definition Source");
        vCompAtt.addElement("Vocabulary");
        vCompAtt.addElement("Level");
        session.setAttribute("creSelectedAttr", vCompAtt);
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
        vCompAtt.addElement("Effective Begin Date");
        vCompAtt.addElement("Effective End Date");
        vCompAtt.addElement("Creator");
        vCompAtt.addElement("Date Created");
        vCompAtt.addElement("Modifier");
        vCompAtt.addElement("Date Modified");
        vCompAtt.addElement("Comments/Change Note");
        vCompAtt.addElement("All Attributes");
     }
     else if (selSearch.equals("ClassSchemeItems"))
     {
        vCompAtt.addElement("CSI Name");
        vCompAtt.addElement("CSI Type");
        vCompAtt.addElement("CSI Definition");
        vCompAtt.addElement("CS Long Name");
        vCompAtt.addElement("Context");
        vCompAtt.addElement("All Attributes");
     }
      else if (selSearch.equals("ObjectClass")
      || selSearch.equals("Property") || selSearch.equals("PropertyClass")
      || selSearch.equals("RepTerm") || selSearch.equals("VDObjectClass")
      || selSearch.equals("VDProperty") || selSearch.equals("VDPropertyClass")
      || selSearch.equals("VDRepTerm"))
     {
        vCompAtt.addElement("Concept Name");
        vCompAtt.addElement("Public ID");
        vCompAtt.addElement("EVS Identifier");
        vCompAtt.addElement("Definition");
        vCompAtt.addElement("Definition Source");
        vCompAtt.addElement("Context");
        vCompAtt.addElement("Vocabulary");
        vCompAtt.addElement("DEC's Using");
        session.setAttribute("creSelectedAttr", vCompAtt);
     }
      else if (selSearch.equals("RepQualifier") || selSearch.equals("ObjectQualifier") 
      || selSearch.equals("PropertyQualifier") || selSearch.equals("VDRepQualifier")
      || selSearch.equals("VDObjectQualifier") || selSearch.equals("VDPropertyQualifier"))
     {     
        vCompAtt.addElement("Concept Name");
        vCompAtt.addElement("EVS Identifier");
        vCompAtt.addElement("Definition");
        vCompAtt.addElement("Definition Source");
        vCompAtt.addElement("Comments");
        vCompAtt.addElement("Vocabulary");
        session.setAttribute("creSelectedAttr", vCompAtt);
     }
     if (sMenu.equals("searchForCreate"))
        session.setAttribute("creAttributeList", vCompAtt);
     else
        session.setAttribute("serAttributeList", vCompAtt);
 
     Vector vSelectedAttr = (Vector)session.getAttribute("creSelectedAttr");
     if (vSelectedAttr == null || selSearch.equals("ReferenceValue"))
     {
        session.setAttribute("creSelectedAttr", vCompAtt);
        session.setAttribute("creSearchAC", selSearch);
     }
  }  //end compattlist
 
 
   /**
   * This method forwards to an Error page.
   * Called from 'service' method where reqType is 'actionFromMenu'
   * forwards page 'ErrorLoginPage.jsp'.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   */
   public void ErrorLogin(HttpServletRequest req,
     HttpServletResponse res)
   {
       try
       {
          ForwardErrorJSP(req, res, "Session terminated. Please log in again.");
       }
       catch (Exception e)
       {
         //System.err.println("ERROR - ErrorLogin: " + e);
         this.logger.fatal("ERROR - ErrorLogin: " + e.toString());
       }
   }
 
  
  
 
   /**
   * The destroy method closes a connection pool to db.
   */
   public void destroy()
   {
     OracleOCIConnectionPool cpool = (OracleOCIConnectionPool)hashOracleOCIConnectionPool.get("/cdecurate");
     try
     {
       cpool.close();
     }
     catch (Exception e)
     {
       logger.fatal("ERROR - destroy: " + e.toString());
     }
     
      cpool = null;
      hashOracleOCIConnectionPool = null;
   }
 
 
   /**
   * doLogout method closes the connection and forwards to Login page
   * Called from Logout button on Titlebar.jsp
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   *
   * @throws Exception
   */
   public void doLogout(HttpServletRequest req, HttpServletResponse res)
   throws Exception
   {
       try
       {
           HttpSession session = req.getSession();
           session.invalidate();
           ForwardErrorJSP(req, res, "Logged out.");
       }
       catch (Exception e)
       {
         logger.fatal("ERROR - ErrorLogin: " + e.toString());        
       }
   }
 
   /**
   * The ForwardJSP method forwards to a jsp page.
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   */
   private void ForwardJSP(HttpServletRequest req,
     HttpServletResponse res, String sJSPPage)
   {
     try
     {
       // forward to the jsp (or htm)
       String fullPage = "/jsp" + sJSPPage;
       ServletContext sc = this.getServletContext();
       RequestDispatcher rd = sc.getRequestDispatcher(fullPage);
       rd.forward(req, res);
     }
     catch(Exception e)
     {
       e.printStackTrace();
       this.logger.fatal("Servlet-ForwardJSP : " + e.toString());
     }
   }
 
    /**
   * The ForwardErrorJSP method forwards to a jsp page.
   *
   * @param req The HttpServletRequest from the client
   * @param res The HttpServletResponse back to the client
   * @param errMsg  String error message
   */
   public void ForwardErrorJSP(HttpServletRequest req,
     HttpServletResponse res, String errMsg) throws Exception
   {
     try
     {
       HttpSession session;
       session = req.getSession(true);
       session.setAttribute("ErrorMessage", errMsg);
       String fullPage = "/";
       ServletContext sc = this.getServletContext();
       RequestDispatcher rd = sc.getRequestDispatcher(fullPage);
       rd.forward(req, res);
     }
     catch(Exception e)
     {
       logger.fatal("Servlet-ForwardErrorJSP : " + e.toString() );
     }
   }
 
 }  //end of class 
