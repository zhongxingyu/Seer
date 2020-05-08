 /**
  * 
  * Copyright 2002 - 2007 NCHELP
  * 
  * Author:	Tim Bornholtz, The Bornholtz Group
  * 			Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by The Bornholtz Group 
  * and Priority Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package org.nchelp.meteor.provider.access;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.soap.encoding.soapenc.Base64;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.MeteorConstants;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.Ssn;
 import org.nchelp.meteor.util.exception.AuthenticationLevelException;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.ParsingException;
 
 
 
 /**
  * This is the servlet object that processes an HTML request. The request will
  * send a parameter indicating a list request or a detail request. The servlet
  * will apply an XSL Template to the XML returned in order to display detail to
  * the user.
  * 
  * @version $Revision$ $Date$
  * @since Meteor1.0
  * @author timb
  *  
  */
 public class MeteorServlet extends HttpServlet
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1719553886513257062L;
 	private static transient final Log log = LogFactory.getLog(MeteorServlet.class);
 
 	/**
 	 * This is the main workhorse of this object. This is the method responsible
 	 * for handling the HTTP request and responding.
 	 * 
 	 * @param req
 	 *          HTTP request object
 	 * @param res
 	 *          HTTP response object
 	 */
 	public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		// The only object that should get stored in the session object
 		// is the SecurityToken
 
 		try
 		{
 			String xml = null;
 
 			HttpSession sess = req.getSession();
 
 			MeteorParameters parms = new MeteorParameters();
 			parms.setSsn((String)sess.getAttribute("CurrentSSN"));
 
 			String level = req.getParameter("Level");
 			String originalLevel = level;
 			if (level == null)
 			{
 				level = req.getParameter("level");
 			}
 
 			//Map httpParameterMap = req.getParameterMap();
 			Map xsltParameterMap = new HashMap();
 
 			// Make sure that the SSN is always in the HashMap.
 			// If it is still passed in as a parameter then that will
 			// overwrite this one.
 			String ssn = parms.getSsn();
 			if (ssn == null)
 				ssn = "";
 			xsltParameterMap.put("SSN", ssn);
 
 			// If a security Token is available in the session,
 			// then set that immediately. Any other security
 			// token that is passed in will override this one.
 			SecurityToken token = (SecurityToken)sess.getAttribute("SecurityToken");
 			if (token != null)
 			{
 				log.debug("Reading security token from the session object");
 				parms.setSecurityToken(token);
 				parms.setRole(token.getRole());
 			}
 
 			try
 			{
 				buildParameters(req, parms, xsltParameterMap);
 			}
 			catch (ParameterException e)
 			{
 				level = "query";
 				sess.removeAttribute("CurrentSSN");
 				sess.removeAttribute("inquiryType");
 				xml = validationError(e.getMessage());
 				log.debug("ParameterException: " + xml);
 			}
 
 			log.debug("Inquiry Level is: " + level);
 			
 			sess.setAttribute("CurrentSSN", parms.getSsn());
 
 			String inquiryType = parms.getInquiryType();
 			if (inquiryType == null)
 			{
 				inquiryType = "";
 			}
 			sess.setAttribute("inquiryType", inquiryType);
 
 			// Read the security Token out of the session
 			// If it isn't in there then that's OK, since
 			// it will have a null value which is what the
 			// rest of the program expects
 			if (parms.getSecurityToken() != null)
 			{
 				// One was passed in as a parameter
 				// So save it off
 				log.debug("Saving the Security Token to the session object");
 				sess.setAttribute("SecurityToken", parms.getSecurityToken());
 			}
 
 			
 			IndexQueryService indexQueryService = this.getIndexQueryService(parms, sess);
 			
 			indexQueryService.setMeteorParameters(parms);
 			indexQueryService.setHttpSession(sess);
 
 			if (xml == null && !"query".equals(level))
 			{
 				//
 				// get the xml containing the list of loans
 				//
 				if(this.processInBackground()){
 					ThreadGroup grp = new ThreadGroup("Meteor Query");
 					
 					IndexQueryServiceThread thread = new IndexQueryServiceThread(grp, indexQueryService);
 					log.debug("Starting the IndexQueryServiceThread from the MeteorServlet");
 					thread.start();
 				} else {
 					indexQueryService.query();
 				}
 				//indexQueryService.query();
 					
 
 			}
 
 			log.debug("XML: " + xml + " Level: " + level);
 			if (xml == null && "query".equals(level))
 			{
 				xml = "<MeteorDataErrors></MeteorDataErrors>";
 			} else if(! "query".equals(level)){
 				try
 				{
 					log.debug("getting the response from the IndexQueryService");
 					xml = indexQueryService.getResponse();
 					//log.debug("XML is now: " + xml);
 				}
 				catch (AuthenticationLevelException e)
 				{
 					log.warn("User required an Authentication Level of " + e.getMinimumAuthenticationLevel());
 					AuthenticationQueryService aqs = new AuthenticationQueryService();
 					xml = aqs.getAuthenticationProviders(e.getMinimumAuthenticationLevel(), token.getRole());
 					level = "authentication";
 				}
 
 				if(xml == null && this.processInBackground()){
 					log.debug("XML is still null so forcing the action to the status page");
 					xml = "<MeteorDataResponse/>";
 					originalLevel = level;
 					level = "status";
 				}
 			}
 			
 
 			//
 			// Convert the xml and send response to user
 			//
 			String output;
 			if (this.getApplyXsl())
 			{
 				res.setContentType("text/html");
 
 				// Put the Servlet name in the xslt Parameters too
 				xsltParameterMap.put("SERVLET", req.getContextPath() + req.getServletPath());
 				xsltParameterMap.put("STATUSSERVLET", req.getContextPath() + this.getStatusServlet());
 				xsltParameterMap.put("CONTEXTPATH", req.getContextPath());
 				if(originalLevel == null){
 					originalLevel = "default";
 				}
 				xsltParameterMap.put("ORIGINALLEVEL", originalLevel);
 				xsltParameterMap.put("ROLE", parms.getRole());
 				if(level == null){
 					level = "default";
 				}
 				xsltParameterMap.put("LEVEL", level);
 
 				TransformXML trans = new TransformXML();
 				
 				if(! "query".equalsIgnoreCase(level)){
 					// Don't apply the business rules when we're going to the query page. There is no point.
 					// Apply the business rules first, then do the transform to html
 					String tmpXML = trans.transform(parms, "businessrules", xsltParameterMap, getServletContext(), xml);
 					
 				
 					if(tmpXML != null){
 						xml = tmpXML;
 					} else {
 						log.error("The XML output from the business rules is null.  Please look in the log file for previous errors and correct those.");
 					}
 					//log.debug("XML After Business Rules: \n" + xml);
 				}
 					
 				output = trans.transform(parms, level, xsltParameterMap, getServletContext(), xml);
 
 				// Put the Servlet name in the xslt Parameters too
 				xsltParameterMap.put("SERVLET", req.getContextPath() + req.getServletPath());
 			}
 			else
 			{
 				res.setContentType("text/xml");
 				output = xml;
 			}
 
 			PrintWriter out = res.getWriter();
 
 			out.write(output);
 
 			out.close();
 		}
 		catch (Exception e)
 		{
 			criticalError(res, "Unspecified error occurred: " + e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Gets the address.
 	 * 
 	 * @return Returns a URL
 	 */
 	public String getServletInfo ()
 	{
 		return "This is the Meteor servlet.";
 	}
 
 	/**
 	 * Handles any errors occurring during processing of the request
 	 * 
 	 * @param msg
 	 *          Error message
 	 */
 	private void criticalError (HttpServletResponse res, String msg, Exception e)
 			throws java.io.IOException
 	{
 		log.error("Critical Error occured in MeteorServlet: " + msg, e);
 
 		// Send the response
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 
 		out.println("<html>");
 		out.println("<head><title>Error Page</title></head>");
 		out.println("<body>");
 		out.println("<h1>The follow error has occurred while processing " + "the Meteor request:  " + msg + "</h1>");
 		out.println("</body></html>");
 
 		if (e != null)
 			e.printStackTrace(out);
 
 		out.close();
 	}
 
 	private String validationError (String message)
 	{
 		return "<MeteorDataErrors> " + (message == null ? "" : "<ErrorMessage>" + message +
 		       "</ErrorMessage>") + "</MeteorDataErrors>";
 	}
 	
 	private String getStatusServlet(){
 		Resource resource = ResourceFactory.createResource("accessprovider.properties");
 		return resource.getProperty("meteor.status.servletpath", "/status");
 	}
 	
 	private boolean processInBackground(){
 		Resource resource = ResourceFactory.createResource("accessprovider.properties");
 		String prop = resource.getProperty("meteor.backgroundprocessing", "Yes");
 		
 		return ("Yes".equalsIgnoreCase(prop) ? true : false);
 	}
 	
 	private IndexQueryService getIndexQueryService(MeteorParameters parms, HttpSession sess){
 		IndexQueryService indexQueryService;
 		
 		
 		indexQueryService = (IndexQueryService)sess.getAttribute(MeteorConstants.SESSION_QUERY_SERVICE);
 		
 		boolean createNew = false;
 		
 		if(indexQueryService == null){
 			log.debug("Session IndexQueryService is null, creating a new IndexQueryService");
 			createNew = true;
 		} else {
 			// Look at the current one and make sure the SSN matches.  If not, destroy it and create a new one
 			MeteorParameters tmpParms = indexQueryService.getMeteorParameters();
 			if(parms == null){
 				//Huh??
 				log.debug("No MeteorParamters passed into the getIndexQueryService method.");
 				createNew = true;
 			}
 			if(tmpParms == null){
 				log.debug("Existing IndexQueryService does not have a MeteorParameters object");
 				createNew = true;
 			}
 			if(! parms.getSsn().equals(tmpParms.getSsn())) {
 				log.debug("Existing IndexQueryService MeteorParameters SSN does not match new SSN, creating new IndexQueryService");
 				createNew = true;
 			}
 			else {
 				log.debug("Existing session IndexQueryService SSN matches the current query SSN.  Reusing the IndexQueryService object");
 			}
 			
 		}
 
 		if(createNew){
 			if(indexQueryService != null){
 				indexQueryService = null;
 			}
 			indexQueryService = new IndexQueryService();
 			sess.setAttribute(MeteorConstants.SESSION_QUERY_SERVICE, indexQueryService);
 		}
 		
 		return indexQueryService;
 	}
 
 	/**
 	 * Method buildParameters.
 	 * 
 	 * @param req
 	 * @param parms
 	 * @param xsltParameterMap
 	 * @throws ParameterException
 	 */
 	private void buildParameters (HttpServletRequest req, MeteorParameters parms, Map xsltParameterMap) throws ParameterException
 	{
 		HttpSession session = req.getSession();
 
 		StringBuffer fatalErrors = new StringBuffer();
 
 		Resource authResource = ResourceFactory.createResource("authentication.properties");
 		parms.setInstitutionID(authResource.getProperty("authentication.identifier"));
 
 		Resource resource = ResourceFactory.createResource("accessprovider.properties");
 		String strRequireAssertion = resource.getProperty("meteor.authentication.requirepassedassertion", "No");
 		boolean requirePassedAssertion = ("Yes".equalsIgnoreCase(strRequireAssertion) ? true : false);
 
 		String strAcceptAssertion = resource.getProperty("meteor.authentication.acceptassertions", "Yes");
 		boolean acceptPassedAssertion = ("Yes".equalsIgnoreCase(strAcceptAssertion) ? true : false);
 
 		Enumeration enumer = req.getParameterNames();
 
 		while (enumer.hasMoreElements())
 		{
 			String key = (String)enumer.nextElement();
 			String value = req.getParameter(key);
 
 			if (key == null)
 				continue;
 
 			if (key.equalsIgnoreCase("SSN"))
 			{
 				if (value == null)
 					value = "";
 
 				// Remove all of the '-'
 				String ssn = Ssn.trimdashes(value);
 
 				// Set the value back to the complete SSN
 				// since all of the values are stored
 				// in a hash to send to xslt.
 				value = ssn;
 				parms.setSsn(ssn);
 
 				if (!Ssn.validate(ssn))
 				{
 					fatalErrors.append("Invalid SSN.");
 				}
 			}
 			else if (value == null || value.equals(""))
 			{
 				continue;
 			}
 			else if (key.equalsIgnoreCase("forcerefresh"))
 			{
 				boolean refresh = false;
 
 				if ("yes".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value))
 				{
 					refresh = true;
 				}
 
 				parms.setForceRefresh(refresh);
 			}
 			else if (key.equalsIgnoreCase("overrideminimumlevel"))
 			{
 				boolean override = false;
 
 				if ("yes".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value))
 				{
 					log.info("Authentication Level Override is true");
 					override = true;
 					parms.setForceRefresh(true);
 				} else {
 					log.info("Authentication Level Override was passed to servlet, but it was an unknown value: '" + value + "'");
 				}
 				parms.setOverrideMinimumAuthenticationLevel(override);
 			}
 			else if (key.equalsIgnoreCase("assertion"))
 			{
 				if (!acceptPassedAssertion)
 				{
 					log.error("An assertion was passed into the Access Provider " + "but the accessprovider.properties has the key " +
 					          "meteor.authentication.acceptassertions set to No.  " + "The received assertion will be ignored");
 					continue;
 				}
 				try
 				{
 					// Assertion must be passed in as Base64 encoded
 					value = new String(Base64.decode(value));
 					log.info("Received the Authentication Assertion: " + value);
 
 					SecurityToken token = new SecurityToken(value);
 					parms.setSecurityToken(token);
 
 					parms.setCurrentUser(token.getUserid());
 					parms.setRole(token.getRole());
 				}
 				catch (ParsingException e)
 				{
 					// Don't worry about it, just treat it as an invalid token
 					log.info("Invalid security token received: " + value);
 				}
 
 			}
 			else if (key.equalsIgnoreCase("action"))
 			{
 				parms.setAction(value);
 			}
 			else if (key.equalsIgnoreCase("id"))
 			{
 				parms.addAward(value);
 				
 			}
 			else if (key.equalsIgnoreCase("inquiryType"))
 			{
 				// This key will only be valid when the role is APCSR
 				if(value.toUpperCase().startsWith(SecurityToken.roleFAA.toUpperCase())){
 					value = SecurityToken.roleFAA;
 				} else if(value.toUpperCase().startsWith(SecurityToken.roleBORROWER.toUpperCase())){
 					value = SecurityToken.roleBORROWER;
 				}
 				
 				parms.setInquiryType(value);
 			}
 
 			// Pass all parameters on to XSLT. They
 			// might be needed to construct URLs to
 			// other pages within Meteor
 			// be sure to always store them in upper case just
 			// to make the xslt more consistent
 			//
 			// TODO: Passing any and all URL parameters into the app may be a security problem
 			//
 			xsltParameterMap.put(key.toUpperCase(), value);
 		}
 
 		// Check a few other things just to make sure that everything is filled out
 		// properly
 		if (requirePassedAssertion && parms.getSecurityToken() == null)
 		{
 			fatalErrors.append("A security token is required to access this site but one was not provided." +
 			                   "  You will not be able to continue.  Please see your administrator. ");
 		}
 
 		if (parms.getInstitutionID() == null)
 		{
 			fatalErrors.append("Invalid Institution ID (null). ");
 		}
 
 		// If they did <b>not</b> pass in a security token
 		// then get the current user's userid and role.
 		// If a token was passed, then we should use that
 		// userid and role
 		if (parms.getSecurityToken() == null)
 		{
 			// Get the opaque userid of the person using this Meteor servlet
 			String userClass = resource.getProperty("meteor.user.authentication.class",
 			                                        "org.nchelp.meteor.provider.access.ServletRemoteUser");
 			log.debug("Using User Authentication class: " + userClass);
 
 			IUserAuthentication ua;
 			try
 			{
 				ua = (IUserAuthentication)Class.forName(userClass).newInstance();
 				parms.setCurrentUser(ua.getUserOpaqueID(req));
 			}
 			catch (Exception e)
 			{
 				log.fatal("Error getting the Opaque User ID from the class: " + userClass + " Root Exception: " + e);
 				fatalErrors.append(e);
 			}
 
 			// Get the role of the person using this Meteor servlet
 			String userRoleClass = resource.getProperty("meteor.user.role.class", "org.nchelp.meteor.provider.access.ServletRole");
 			log.debug("Using User Role class: " + userRoleClass);
 
 			IUserRole ur;
 			try
 			{
 				ur = (IUserRole)Class.forName(userRoleClass).newInstance();
 				parms.setRole(ur.getUserRole(req, parms.getCurrentUser()));
 			}
 			catch (Exception e)
 			{
 				log.fatal("Error getting the User Role from the class: " + userRoleClass + " Root Exception: " + e);
 				fatalErrors.append(e);
 			}
 
 			if (SecurityToken.roleFAA.equals(parms.getRole()))
 			{
 				// Get the Org type of the person using this Meteor servlet
 				String clazz = resource.getProperty("meteor.user.organizationtype.class", "org.nchelp.meteor.provider.access.SchoolOrganizationType");
 
 				log.debug("Using Organization Type class: " + clazz);
 				IOrganizationType iot;
 				try
 				{
 					iot = (IOrganizationType)Class.forName(clazz).newInstance();
 					parms.setOrganizationType(iot.getUserOrganizationType(req, parms.getCurrentUser()));
 				}
 				catch (Exception e)
 				{
 					log.fatal("Error getting the User Role from the class: " + clazz + " Root Exception: " + e);
 					fatalErrors.append(e);
 				}
 
 				// Get the Org ID of the person using this Meteor servlet
 				clazz = resource.getProperty("meteor.user.organizationid.class", "org.nchelp.meteor.provider.access.UnknownOrganizationID");
 
 				log.debug("Using Organization Type class: " + clazz);
 				IOrganizationID ioo;
 				try
 				{
 					ioo = (IOrganizationID)Class.forName(clazz).newInstance();
 					parms.setOrganizationID(ioo.getOrganizationID(req, parms.getCurrentUser()));
 				}
 				catch (Exception e)
 				{
 					log.fatal("Error getting the User Role from the class: " + clazz + " Root Exception: " + e);
 					fatalErrors.append(e);
 				}
 			
 				// Get the Org ID Type of the person using this Meteor servlet
 				clazz = resource.getProperty("meteor.user.organizationidtype.class", "org.nchelp.meteor.provider.access.OPEIDType");
 
 				log.debug("Using Organization ID Type class: " + clazz);
 				IOrganizationIDType ioit;
 				try
 				{
 					ioit = (IOrganizationIDType)Class.forName(clazz).newInstance();
 					parms.setOrganizationID(ioit.getUserOrganizationIDType(req, parms.getCurrentUser()));
 				}
 				catch (Exception e)
 				{
 					log.fatal("Error getting the User Role from the class: " + clazz + " Root Exception: " + e);
 					fatalErrors.append(e);
 				}
 			}
 
 			if (SecurityToken.roleBORROWER.equals(parms.getRole()))
 			{
 				// Get the SSN of the person using this Meteor servlet
 				String ssnClass = resource.getProperty("meteor.user.ssn.class", "org.nchelp.meteor.provider.access.NoSSN");
 
 				log.debug("Using Borrower SSN class: " + ssnClass);
 				IBorrowerSSN ibs;
 				try
 				{
 					ibs = (IBorrowerSSN)Class.forName(ssnClass).newInstance();
 					parms.setSsn(ibs.getBorrowerSSN(req, parms.getCurrentUser()));
 				}
 				catch (Exception e)
 				{
 					log.fatal("Error getting the User Role from the class: " + ssnClass + " Root Exception: " + e);
 					fatalErrors.append(e);
 				}
 			}
 
 			if (SecurityToken.roleLENDER.equals(parms.getRole()))
 			{
 				// Get the Lender ID of the person using this Meteor servlet
 				String lenderClass = resource.getProperty("meteor.user.lenderid.class", "org.nchelp.meteor.provider.access.NoLenderID");
 
 				log.debug("Using Lender ID class: " + lenderClass);
 				ILender il;
 				try
 				{
 					il = (ILender)Class.forName(lenderClass).newInstance();
 					parms.setLenderID(il.getLenderID(req, parms.getCurrentUser()));
 				}
 				catch (Exception e)
 				{
 					log.fatal("Error getting the User Role from the class: " + lenderClass + " Root Exception: " + e);
 					fatalErrors.append(e.getMessage());
 				}
 			}
 		}
 
 		if ((SecurityToken.roleAPCSR.equals(parms.getRole()) || SecurityToken.roleLENDER.equals(parms.getRole())) &&
 		    (parms.getInquiryType() == null || "".equals(parms.getInquiryType())) && parms.getSsn() != null)
 		{
 			String type = (String)session.getAttribute("inquiryType");
 			if (type == null || "".equals(type))
 			{
 				log.info("Role is " + parms.getRole() + " but inquiryType is '" + type + "'");
 				fatalErrors.append("An Inquiry Type must be selected. ");
 			}
 			else
 			{
 				parms.setInquiryType(type);
 			}
 		}
 
 		if (parms.getRole() == null)
 			fatalErrors.append("Invalid Role (null). ");
 
 		if (parms.getSsn() == null || "".equals(parms.getSsn()))
 		{
 			parms.setSsn("");
 			throw new ParameterException((String)null);
 		}
 
 		if (fatalErrors.length() > 0)
 		{
 			throw new ParameterException(fatalErrors.toString());
 		}
 
 		return;
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
 	 */
 	public void doPost (HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
 	{
 		this.doGet(arg0, arg1);
 	}
 
 	/**
 	 * Choose to apply XSL or return raw XML. Sallie Mae requested that this
 	 * method be added. Before modifying it, please get in touch with them.
 	 * 
 	 * @return boolean If true, apply XSL transform if false, return pure XML.
 	 */
 	protected boolean getApplyXsl ()
 	{
 		return true;
 	}
 
 }
