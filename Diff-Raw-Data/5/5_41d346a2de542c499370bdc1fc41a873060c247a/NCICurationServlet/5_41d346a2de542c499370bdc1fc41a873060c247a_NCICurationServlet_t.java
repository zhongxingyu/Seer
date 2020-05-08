 // Copyright (c) 2005 ScenPro, Inc.
 
// $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/NCICurationServlet.java,v 1.63 2009-01-19 15:32:05 veerlah Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 // import files
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.util.ClockTime;
 
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import gov.nih.nci.cadsr.cdecurate.util.HelpURL;
 
 import org.apache.log4j.Logger;
 
 /**
  * The NCICurationServlet is the main servlet for communicating between the client and the server.
  * <P>
  * 
  * @author Joe Zhou, Sumana Hegde, Tom Phillips, Jesse McKean
  * @version 3.0
  */
 /*
  * The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. (ScenPro) Copyright Notice. The software subject to this notice and license
  * includes both human readable source code form and machine readable, binary, object code form (the CaCORE Software). The CaCORE Software was developed in
  * conjunction with the National Cancer Institute (NCI) by NCI employees and employees of SCENPRO. To the extent government employees are authors, any rights
  * in such works shall be subject to Title 17 of the United States Code, section 105. This CaCORE Software License (the License) is between NCI and You. You
  * (or Your) shall mean a person or an entity, and all other entities that control, are controlled by, or are under common control with the entity. Control
  * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial ownership of such entity. This License is granted provided
  * that You agree to the conditions described below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable
  * and royalty-free right and license in its rights in the CaCORE Software to (i) use, install, access, operate, execute, copy, modify, translate, market,
  * publicly display, publicly perform, and prepare derivative works of the CaCORE Software; (ii) distribute and have distributed to and by third parties the
  * CaCORE Software and any modifications and derivative works thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties,
  * including the right to license such rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
  * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no charge to You. 1. Your
  * redistributions of the source code for the Software must retain the above copyright notice, this list of conditions and the disclaimer and limitation of
  * liability of Article 6, below. Your redistributions in object code form must reproduce the above copyright notice, this list of conditions and the disclaimer
  * of Article 6 in the documentation and/or other materials provided with the distribution, if any. 2. Your end-user documentation included with the
  * redistribution, if any, must include the following acknowledgment: This product includes software developed by SCENPRO and the National Cancer Institute.
  * If You do not include such end-user documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments
  * normally appear. 3. You may not use the names "The National Cancer Institute", "NCI" ScenPro, Inc. and "SCENPRO" to endorse or promote products derived
  * from this Software. This License does not authorize You to use any trademarks, service marks, trade names, logos or product names of either NCI or SCENPRO,
  * except as required to comply with the terms of this License. 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into
  * Your proprietary programs and into any third party proprietary programs. However, if You incorporate the Software into third party proprietary programs, You
  * agree that You are solely responsible for obtaining any permission from such third parties required to incorporate the Software into such third party
  * proprietary programs and for informing Your sublicensees, including without limitation Your end-users, of their obligation to secure any required permissions
  * from such third parties before incorporating the Software into such third party proprietary software programs. In the event that You fail to obtain such
  * permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to the extent prohibited by law, resulting from Your failure
  * to obtain such permissions. 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and to the
  * derivative works, and You may provide additional or different license terms and conditions in Your sublicenses of modifications of the Software, or any
  * derivative works of the Software as a whole, provided Your use, reproduction, and distribution of the Work otherwise complies with the conditions stated in
  * this License. 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SCENPRO, OR THEIR
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  * DAMAGE.
  */
 /**
  * @author shegde
  */
 public class NCICurationServlet extends HttpServlet
 {
     private static final long serialVersionUID = 8538064617183797182L;
 
     public static Properties m_settings;
 
     public static String _dataSourceName = null;
     public static String _authenticateDSName = null;
     public static String _userName = null;
     public static String _password = null;
     
     public static final Logger logger = Logger.getLogger(NCICurationServlet.class.getName());
 
     /**
      * To initialize global variables and load the Oracle driver.
      * 
      * @param config
      *        The ServletConfig object that has the server configuration
      * @throws ServletException
      *         Any exception that occurs during initialization
      */
     public void init(ServletConfig config) throws ServletException
     {
         if (false)
         {
             // For development debug only.
         Enumeration attrs = config.getServletContext().getAttributeNames();
         while (attrs.hasMoreElements())
         {
             String name = (String) attrs.nextElement();
             Object obj = config.getServletContext().getAttribute(name);
             logger.debug(name + " = " + obj.toString());
         }
         }
 
         logger.info(" ");
         logger.info("Starting " + this.getClass().getName());
         logger.info(" ");
 
         super.init(config);
         try
         {
             // Get the properties settings
             // Placeholder data for AC creation coming from CRT
             getProperties();
         }
         catch (Exception ee)
         {
             logger.fatal("Servlet-init : Unable to start curation tool : " + ee.toString(), ee);
         }
 
         // call the method to make oracle connection **database connection class requires lot of changes everywhere;
         // leave it right now */
         this.initOracleConnect();
         
         
     }
     
 
     /**
      * initilize the oracle connect
      */
     private void initOracleConnect()
     {
         try
         {
             logger.info("initOracleConnect - accessing data source pool");
             _dataSourceName = "java:/" + getServletConfig().getInitParameter("jbossDataSource");
             _authenticateDSName="java:/" + getServletConfig().getInitParameter("jbossAuthenticate");
             _userName= getServletConfig().getInitParameter("username");
            _password = getServletConfig().getInitParameter("password");
 
             // Test connnection
             Connection con = null;
             Statement stmt = null;
             ResultSet rset = null;
             boolean connected =false;
             CurationServlet curser = new CurationServlet();
             try
             {
                 con = curser.getConnFromDS();
                 stmt = con.createStatement();
                 rset = stmt.executeQuery("Select sysdate from dual");
                 if (rset.next())
                     {
                 	 rset.getString(1);
                 	 connected =true;
                     }
                 else
                     throw (new Exception("DBPool connection test failed."));
                  if(connected)
                  {
                 	 String helpURL = curser.getHelpURL(con);
                 	 HelpURL.setCurationToolHelpURL(helpURL);
                  }
             }
             catch (Exception e)
             {
                 logger.fatal("Could not open database connection.", e);
             }
             finally{
     			SQLHelper.closeResultSet(rset);
     			SQLHelper.closeStatement(stmt);
     			SQLHelper.closeConnection(con);
             }
         }
         catch (Exception e)
         {
             logger.fatal("initOracleConnect - Some other error", e);
         }
     }
 
     /**
      * The service method services all requests to this servlet.
      * 
      * @param req
      *        The HttpServletRequest object that contains the request
      * @param res
      *        The HttpServletResponse object that contains the response
      */
     public void service(HttpServletRequest req, HttpServletResponse res)
     {
         ClockTime clock = new ClockTime();
         try
         { 
         	String reqType = req.getParameter("reqType"); 
         	HttpSession session = req.getSession();
         	String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
        	if((menuAction == null) && !(reqType.equals("homePage")) && !(reqType.equals("login"))&& !(reqType.equals("view")) && !(reqType.equals("viewVDPVSTab"))&& !(reqType.equals("viewVMAction")) && !(reqType.equals("viewPVAction")) && !(reqType.equals("getAltNames")) && !(reqType.equals("getRefDocument"))){
 				RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/");
 				rd.forward(req, res);
 				return;
 			}
 		   	// add the forwarding to request to session (to go after login)
         	String forwardReq = (String)session.getAttribute("forwardReq");
         	if (forwardReq == null || !forwardReq.equals("login"))
         	   session.setAttribute("forwardReq", reqType);
         	if (forwardReq == null && reqType.equals("login"))
         		session.setAttribute("directLogin", "yes");
         	
         	ACRequestTypes acrt = null;
         	CurationServlet curObj = null;        	
         	try {
         		acrt = ACRequestTypes.valueOf(reqType);
 				if (acrt != null)   
 				{
 				   String className = acrt.getClassName();
 				   curObj = (CurationServlet) Class.forName(className).newInstance();
 				   curObj.init(req, res, this.getServletContext());
 				   curObj.get_m_conn();
 				   curObj.execute(acrt);
 				}
 			} catch (RuntimeException e) {
 				//logger.info(e.toString(), e);
 			}
 	        finally
 	        {
 	        	if (curObj != null)
 					curObj.destroy();	        		
 	        }
 			if (acrt == null)
 			{
 				CurationServlet curser = new CurationServlet(req, res, this.getServletContext());
 				curser.service();
 			}
         }
         catch (Exception e)
         {
             logger.error("Service error : " + e.toString(), e);
         }
         logger.debug("service response time " + clock.toStringLifeTime());
     }
 
     /**
      * The getProperties method sets up some default properties and then looks for the NCICuration.properties file to override the defaults. Called from 'init'
      * method.
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
         catch (Exception e)
         {
             // System.err.println("ERROR - Got exception reading properties " + e);
             logger.error("Servlet-getProperties : " + e.hashCode() + " : " + e.toString(), e);
         }
     }
 
     /**
      * Get Servlet information
      * 
      * @return java.lang.String
      */
     public String getServletInfo()
     {
         return "gov.nih.nci.cadsr.cdecurate.tool.NCICuration Information";
     }
 
     /**
      * The destroy method closes a connection pool to db.
      */
     public void destroy()
     {
         logger.info(" ");
         logger.info("Stopping " + this.getClass().getName());
         logger.info(" ");
         
      }
     
 }
