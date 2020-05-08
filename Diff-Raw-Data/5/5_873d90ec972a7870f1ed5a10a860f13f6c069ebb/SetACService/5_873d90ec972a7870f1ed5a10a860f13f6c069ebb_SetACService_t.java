 // Copyright (c) 2000 ScenPro, Inc.
 
 // $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/SetACService.java,v 1.65 2009-04-28 15:22:30 veerlah Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import gov.nih.nci.cadsr.cdecurate.database.Alternates;
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsServlet;
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsSession;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 import gov.nih.nci.cadsr.common.Constants;
 import gov.nih.nci.cadsr.persist.common.DBConstants;
 
 import java.io.Serializable;
 import java.sql.CallableStatement;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 /**
  * This utility class is used during the validation of the create/edit action of the tool.
  * <P>
  * @author Sumana Hegde
  * @version 3.0
  */
 
 /*
 The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. ("ScenPro")
 Copyright Notice.  The software subject to this notice and license includes both
 human readable source code form and machine readable, binary, object code form
 ("the CaCORE Software").  The CaCORE Software was developed in conjunction with
 the National Cancer Institute ("NCI") by NCI employees and employees of SCENPRO.
 To the extent government employees are authors, any rights in such works shall
 be subject to Title 17 of the United States Code, section 105.
 This CaCORE Software License (the "License") is between NCI and You.  "You (or "Your")
 shall mean a person or an entity, and all other entities that control, are
 controlled by, or are under common control with the entity.  "Control" for purposes
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
 include the following acknowledgment: "This product includes software developed
 by SCENPRO and the National Cancer Institute."  If You do not include such end-user
 documentation, You shall include this acknowledgment in the Software itself,
 wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" "ScenPro, Inc."
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
 
 public class SetACService implements Serializable
 {
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 	UtilService m_util = new UtilService();
 	CurationServlet m_servlet;
 	Logger logger = Logger.getLogger(SetACService.class.getName());
 	Vector<String> m_vRetWFS = new Vector<String>();
 	Vector<String> m_ReleaseWFS = new Vector<String>();
 	Vector<String> m_vRegStatus = new Vector<String>();
 
 	/**
 	 * Instantiate the class
 	 * @param CurationServlet CurationServlet
 	 */
 	public SetACService(CurationServlet CurationServlet)
 	{
 		m_servlet = CurationServlet;
 		//default retired work flow statuses
 		m_vRetWFS.addElement("RETIRED ARCHIVED");
 		m_vRetWFS.addElement("RETIRED DELETED");
 		m_vRetWFS.addElement("RETIRED PHASED OUT");
 		m_vRetWFS.addElement("RETIRED WITHDRAWN");
 		//released workflow statuses list
 		m_ReleaseWFS.addElement("RELEASED");
 		m_ReleaseWFS.addElement("RELEASED-NON-CMPLNT");
 		m_ReleaseWFS.addElement("CMTE APPROVED");
 		m_ReleaseWFS.addElement("CMTE SUBMTD");
 		m_ReleaseWFS.addElement("CMTE SUBMTD USED");
 		m_ReleaseWFS.addElement("APPRVD FOR TRIAL USE");
 		m_ReleaseWFS.addElement("DRAFT MOD");
 		//valid registration status for relaased ac
 		m_vRegStatus.addElement("Candidate");
 		m_vRegStatus.addElement("Standard");
 		m_vRegStatus.addElement("Proposed");
 
 	}
 
 	/**
 	 * To check validity of the data for Data Element component before submission, called from CurationServlet.
 	 * Validation is done against Database restriction and ISO1179 standards.
 	 * Some validations are seperated according to Edit or Create actions.
 	 * calls various methods to get validity messages and store it into the vector.
 	 * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_DE DataElement Bean.
 	 * @param getAC reference to GetACService class.
 	 *
 	 */
 	public void setValidatePageValuesDE(HttpServletRequest req,
 			HttpServletResponse res, DE_Bean m_DE, GetACService getAC)// throws ServletException,IOException
 	{
 		HttpSession session = req.getSession();
 		Vector<ValidateBean> vValidate = new Vector<ValidateBean>();
 		try
 		{
 			String s;
 			//String s2;
 			boolean bMandatory = true;
 			boolean bNotMandatory = false;
 			String strInValid = "";
 			String strUnique = "";
 			//int iLengthLimit = 30;
 			int iNoLengthLimit = -1;
 			String sDEAction = (String)session.getAttribute("DEAction");
 			String sOriginAction = (String)session.getAttribute("originAction");
 			String sMenu = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 			String sDDEAction = (String)session.getAttribute("DDEAction");
 			boolean isUserEnter = false;
 			//System.out.println(sOriginAction + " validate  " + sMenu + " de " + sDEAction);
 
 			//check edit or create
 			if (sDEAction.equals("EditDE"))
 				sDEAction = "Edit";
 			else
 				sDEAction = "Create";
 			//get the right label for pref type
 			if (sDEAction.equals("Create") && (sMenu.equalsIgnoreCase("nothing")
 					|| sMenu.equalsIgnoreCase("NewDEFromMenu") || sDDEAction.equals("CreateNewDEFComp")))
 				isUserEnter = true;
 
 			String sUser = (String)session.getAttribute("Username");
 			// mandatory
 			s = m_DE.getDE_CONTEXT_NAME();
 			if (s == null) s = "";
 			String sID = m_DE.getDE_CONTE_IDSEQ();
 
 			if ((sUser != null) && (sID != null))
 				strInValid = checkWritePermission("de", sUser, sID, getAC);
 			if (strInValid.equals("")) DataManager.setAttribute(session, "sDefaultContext", s);
 			UtilService.setValPageVector(vValidate, "Context", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			s = m_DE.getDE_DEC_NAME();
 			if (s == null) s = "";
 			//strUnique = checkUniqueInContext("Version", "DE", m_DE, null, null, getAC, sDEAction);
 			strUnique = this.checkUniqueDECVDPair(m_DE, getAC, sDEAction, sMenu);
 			UtilService.setValPageVector(vValidate, "Data Element Concept", s, bMandatory, iNoLengthLimit, strUnique, sOriginAction);
 
 			//strUnique is same as the above.
 			s = m_DE.getDE_VD_NAME();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Value Domain", s, bMandatory, iNoLengthLimit, strUnique, sOriginAction);
 
 			s = m_DE.getDE_LONG_NAME();
 			if (s == null) s = "";
 			strInValid = "";
 			UtilService.setValPageVector(vValidate, "Long Name", s, bMandatory, 255, strInValid, sOriginAction);
 
 			s = m_DE.getDE_PREFERRED_NAME();
 			if (s == null) s = "";
 			strInValid = "";
 			//checks uniuqe in the context and name differred for Released
 			if (s.equals("")== false)
 			{
 				if (!s.equalsIgnoreCase("(Generated by the System)"))
 					strInValid = checkUniqueInContext("Name", "DE", m_DE, null, null, getAC, sDEAction);
 				DE_Bean oldDE = (DE_Bean)session.getAttribute("oldDEBean");
 				if (oldDE != null && sDEAction.equals("Edit"))
 				{
 					String oName = oldDE.getDE_PREFERRED_NAME();
 					String oStatus = oldDE.getDE_ASL_NAME();
 					strInValid = strInValid + checkNameDiffForReleased(oName, s, oStatus);
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Short Name", s, bMandatory, 30, strInValid, sOriginAction);
 
 			//pref name type
 			s = m_DE.getAC_PREF_NAME_TYPE();
 			this.setValidatePrefNameType(s, isUserEnter, vValidate, sOriginAction);
 
 			s = m_DE.getDE_PREFERRED_DEFINITION();
 			if (s == null) s = "";
 			strInValid = "";
 			UtilService.setValPageVector(vValidate, "Definition", s, bMandatory, 2000, strInValid, sOriginAction);
 
 			//workflow status
 			s = m_DE.getDE_ASL_NAME();
 			if (s == null) s = "";
 			strInValid = "";
 			//check associated DEV and VD WFS if DE is released
 			if (!s.equals(""))
 				strInValid = this.checkReleasedWFS(m_DE, s);
 			UtilService.setValPageVector(vValidate, "Workflow Status", s, bMandatory, 20, strInValid, sOriginAction);
 
 			s = m_DE.getDE_VERSION();
 			if (s == null) s = "";
 			strInValid = "";
 			if (s.equals("")==false)
 			{
 				//strInValid = strUnique + this.checkVersionDimension(s);   //checkValueIsNumeric(s);
 				strInValid = this.checkVersionDimension(s);   //checkValueIsNumeric(s);
 				DE_Bean oldDE = (DE_Bean)session.getAttribute("oldDEBean");
 				String menuAction = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 				if (oldDE != null && menuAction.equals("NewDEVersion"))
 				{
 					String oVer = oldDE.getDE_VERSION();
 					if (s.equals(oVer))
 						strInValid = strInValid + "Must change the version number to create a new version.\n";
 					//check if new verison is unique within the public id
 					strInValid = strInValid + checkUniqueInContext("Version", "DE", m_DE, null, null, getAC, sDEAction);
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Version", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
 			//make it empty
 			strInValid = "";
 
 			//registration status
 			s = m_DE.getDE_REG_STATUS();
 			if (s == null) s = "";
 			strInValid = "";
 
 			// if (s.equalsIgnoreCase("Standard") || s.equalsIgnoreCase("Candidate") || s.equalsIgnoreCase("Proposed"))
 			if (m_vRegStatus.contains(s))
 				strInValid = this.checkDECOCExist(m_DE.getDE_DEC_IDSEQ(), req, res);
 			UtilService.setValPageVector(vValidate, "Registration Status", s, bNotMandatory, 50, strInValid, sOriginAction);
 
 			//add begin and end dates to the validate vector
 			String sB = m_DE.getDE_BEGIN_DATE();
 			if (sB == null) sB = "";
 			String sE = m_DE.getDE_END_DATE();
 			if (sE == null) sE = "";
 			String wfs = m_DE.getDE_ASL_NAME();
 			this.addDatesToValidatePage(sB, sE, wfs, sDEAction, vValidate, sOriginAction);
 			//add question text
 			s = m_DE.getDOC_TEXT_PREFERRED_QUESTION();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Preferred Question Text", s, bNotMandatory, 4000, "", sOriginAction);
 
 			//add cs-csi to validate page
 			String sCS = "";
 			String sCSI = "";
 			String sAC = "";
 			if(m_DE.getAC_CS_NAME() != null)
 			{
 				Vector vACCSI = (Vector)m_DE.getAC_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
 				if (vACCSI != null && !vACCSI.isEmpty())
 				{
 					for (int i=0; i<vACCSI.size(); i++)
 					{
 						AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
 						CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
 						if (i>0)
 						{
 							sCS += ", ";
 							sCSI += ", ";
 							sAC += ", ";
 						}
 						sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
 						sCSI += csiBean.getCSI_NAME(); //accsiBean.getCSI_NAME();
 						sAC += accsiBean.getAC_LONG_NAME();
 					}
 				}
 			}
 
 			UtilService.setValPageVector(vValidate, "Classification Scheme", sCS, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 			strInValid = "";
 			if (sCS.equals("")==false && (sCSI.equals("") || sCSI == null))   //it is a pair
 				strInValid = "Items must be selected for the selected Classification Scheme.";
 			UtilService.setValPageVector(vValidate, "Classification Scheme Items", sCSI, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			//contacts
 			Hashtable hConts = m_DE.getAC_CONTACTS();
 			strInValid = "";
 			s = "";
 			if (hConts != null && hConts.size()>0)
 			{
 				Enumeration enum1 = hConts.keys();
 				//loop through the contacts
 				while (enum1.hasMoreElements())
 				{
 					String sKey = (String)enum1.nextElement();
 					//get the contact bean
 					AC_CONTACT_Bean acc = (AC_CONTACT_Bean)hConts.get(sKey);
 					String sSubmit = "";
 					if (acc != null) sSubmit = acc.getACC_SUBMIT_ACTION();
 					//add to the string if not deleted
 					if (sSubmit != null && !sSubmit.equals("DEL"))
 					{
 						if (!s.equals("")) s += ", ";
 						s += sKey;
 					}
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Contacts", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			//origin
 			s = m_DE.getDE_SOURCE();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Data Element Origin", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			s = m_DE.getDE_CHANGE_NOTE();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Change Note", s, bNotMandatory, 2000, "", sOriginAction);
 
 			// add DDE info to DE validate page
 			// if (!sOriginAction.equals("BlockEditDE") && !sOriginAction.equals("CreateNewDEFComp"))
 			if (!sDDEAction.equals("CreateNewDEFComp"))
 				addDDEToDEValidatePage(req, res, vValidate, sOriginAction);
 		}
 		catch(Exception e)
 		{
 			logger.error("Error - Validate DE Values ", e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error Validate DE");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 		//store it in the request
 		Vector<String> vValString = this.makeStringVector(vValidate);
 		req.setAttribute("vValidate", vValString);
 		if(m_DE.getDE_IN_FORM())
 			req.setAttribute("deIDSEQ", m_DE.getDE_DE_IDSEQ());
 		
 	} // end of setValidatePageValues
 
 	/**
 	 * @param sBegin
 	 * @param sEnd
 	 * @param sWFS
 	 * @param editAction
 	 * @param vValidate
 	 * @param sOriginAction
 	 */
 	public void addDatesToValidatePage(String sBegin, String sEnd, String sWFS, String editAction,
 			Vector<ValidateBean> vValidate, String sOriginAction)
 	{
 		try
 		{
 			String begValid = "";
 			if (!sBegin.equals(""))
 			{
 				begValid = this.validateDateFormat(sBegin);
 				//if validated (ret date and input dates are same), no error message
 				if (!begValid.equals("")) begValid = "Begin " + begValid;
 			}
 			//need to make sure the begin date is valid date
 			if (editAction.equalsIgnoreCase("Edit") || editAction.equalsIgnoreCase("N/A"))
 				UtilService.setValPageVector(vValidate, "Effective Begin Date", sBegin, false, -1, begValid, sOriginAction);
 			else
 				UtilService.setValPageVector(vValidate, "Effective Begin Date", sBegin, false, -1, begValid, sOriginAction);
 
 			String endValid = "";
 			//there should be begin date if end date is not null  (8/28 removed)
 			/*if (!sEnd.equals("") && sBegin.equals(""))
         endValid = "If you select an End Date, you must also select a Begin Date.";
       else*/ if (!sEnd.equals(""))
       {
     	  endValid = this.validateDateFormat(sEnd);
     	  //if validated (ret date and input dates are same), no error message
     	  if (!endValid.equals("")) endValid = "End " + endValid;
       }
       //compare teh dates only if both begin date and end dates are valid
       if (!sEnd.equals("") && !sBegin.equals("") && endValid.equals("") && begValid.equals(""))
       {
     	  String notValid = this.compareDates(sBegin, sEnd);
     	  if (notValid.equalsIgnoreCase("true"))
     		  endValid = "Begin Date must be before the End Date";
     	  else if (!notValid.equalsIgnoreCase("false"))
     		  endValid = notValid;  // "Error Occurred in validating Begin and End Dates";
       }
       sWFS = sWFS.toUpperCase();
       // if (wfs.equals("RETIRED ARCHIVED") || wfs.equals("RETIRED DELETED") || wfs.equals("RETIRED PHASED OUT"))
       if (m_vRetWFS.contains(sWFS))
       {
     	  UtilService.setValPageVector(vValidate, "Effective End Date", sEnd, true, -1, endValid, sOriginAction);
       }
       else
       {
     	  UtilService.setValPageVector(vValidate, "Effective End Date", sEnd, false, -1, endValid, sOriginAction);
       }
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - addDatesToValidatePage " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error addDatesToValidatePage");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 	}
 
 	/**
 	 * To add DDE info to DE validate vector, called from setValidatePageValues.
 	 * All data are valid because they are either pick from list or no validate rule
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param vValidate Vector of validate values.
 	 * @param sOriginAction String of origin action.
 	 */
 	public void addDDEToDEValidatePage(HttpServletRequest req,
 			HttpServletResponse res, Vector<ValidateBean> vValidate, String sOriginAction) //throws ServletException,IOException
 	{
 		try
 		{
 			//String strInValid = "valid";
 			boolean bNotMandatory = false;
 			boolean bMandatory = true;
 			int iNoLengthLimit = -1;
 
 			//HttpSession session = req.getSession();
 			String sDDERepTypes[] = req.getParameterValues("selRepType");
 			String sRepType = sDDERepTypes[0];
 			String sRule = (String)req.getParameter("DDERule");
 			String sMethod = (String)req.getParameter("DDEMethod");
 			String sConcatChar = (String)req.getParameter("DDEConcatChar");
 
 			if(sRepType == null || sRepType.length() < 1)
 				return;
 			else
 				UtilService.setValPageVector(vValidate, "Derivation Type", sRepType, bMandatory, iNoLengthLimit, "", sOriginAction);
 
 			if(sRule == null)
 				sRule = "";
 			UtilService.setValPageVector(vValidate, "Rule", sRule, bMandatory, iNoLengthLimit, "", sOriginAction);
 
 			if(sMethod == null)
 				sMethod = "";
 			UtilService.setValPageVector(vValidate, "Method", sMethod, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			if(sConcatChar == null)
 				sConcatChar = "";
 			UtilService.setValPageVector(vValidate, "Concatenate Character", sConcatChar, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			String sDEComps[] = req.getParameterValues("selDECompHidden");
 			String sDECompOrders[] = req.getParameterValues("selDECompOrderHidden");
 			// Vector vDEComp = new Vector();
 			// Vector vDECompOrder = new Vector();
 			String sName = "";
 			String sOrder = "";
 			if(sDEComps != null)
 			{
 				for (int i = 0; i<sDEComps.length; i++)
 				{
 					String sDEComp = sDEComps[i];
 					String sDECompOrder = sDECompOrders[i];
 					if(i < sDEComps.length - 1)
 					{
 						sName = sName + sDEComp + ", ";
 						sOrder = sOrder + sDECompOrder + ", ";
 					}
 					else
 					{
 						sName = sName + sDEComp;
 						sOrder = sOrder + sDECompOrder;
 					}
 				}
 				UtilService.setValPageVector(vValidate, "Data Element Component", sName, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 				UtilService.setValPageVector(vValidate, "Data Element Component Order", sOrder, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - addDDEToDEValidatePage " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error addDDEToDEValidatePage");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 	}  // end of addDDEToDEValidatePage
 
 	/**
 	 * To check validity of the data for Data Element Concept component before submission, called from CurationServlet.
 	 * Validation is done against Database restriction and ISO1179 standards.
 	 * Some validations are seperated according to Edit or Create actions.
 	 * calls various methods to get validity messages and store it into the vector.
 	 * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_DEC DataElementConcept Bean.
 	 * @param m_OC
 	 * @param m_PC
 	 * @param getAC reference to GetACService class.
 	 * @param m_OCQ
 	 * @param m_PCQ
 	 *
 	 */
 	public void setValidatePageValuesDEC(HttpServletRequest req,
 			HttpServletResponse res, DEC_Bean m_DEC, EVS_Bean m_OC, EVS_Bean m_PC,
 			GetACService getAC, EVS_Bean m_OCQ, EVS_Bean m_PCQ)
 	//throws ServletException,IOException, Exception
 	{
 		Vector<ValidateBean> vValidate = new Vector<ValidateBean>();
 		try
 		{
 			////System.out.println("setValidatePageValuesDEC");
 			HttpSession session = req.getSession();
 			// GetACSearch getACSer = new GetACSearch(req, res, m_servlet);
 			String sDECAction = (String)session.getAttribute("DECAction");
 			String sUser = (String)session.getAttribute("Username");
 			String sOriginAction = (String)session.getAttribute("originAction");
 			String sMenu = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 			//System.out.println(sOriginAction + " validate  " + sMenu + " dec " + sDECAction);
 			boolean isUserEnter = false;
 			//check edit or create
 			if (sDECAction.equals("EditDEC"))
 				sDECAction = "Edit";
 			else
 				sDECAction = "Create";
 
 			//get the right label for pref type
 			if (sDECAction.equals("Create") && (sMenu.equalsIgnoreCase("nothing")
 					|| sMenu.equalsIgnoreCase("NewDECFromMenu") || sOriginAction.indexOf("CreateNewDEC") > -1))
 				isUserEnter = true;
 
 			String s;
 			boolean bMandatory = true;
 			boolean bNotMandatory = false;
 			String strInValid = "";
 			//int iLengthLimit = 30;
 			int iNoLengthLimit = -1;
 
 			// mandatory for both Edit and New
 			s = m_DEC.getDEC_CONTEXT_NAME();
 			String sID = m_DEC.getDEC_CONTE_IDSEQ();
 			// System.out.println("setValidatePageValuesDEC0");
 			if ((sUser != null) && (sID != null))
 				strInValid = checkWritePermission("dec", sUser, sID, getAC);
 			if (strInValid.equals("")) DataManager.setAttribute(session, "sDefaultContext", s);
 			UtilService.setValPageVector(vValidate, "Context", s, bMandatory, 36, strInValid, sOriginAction);
 			//validate naming components
 			this.setValidateNameComp(vValidate, "DataElementConcept", req, res, m_DEC, m_OC, m_PC, null, null);
 			// System.out.println("setValidatePageValuesDEC2");
 
 
 			s = m_DEC.getDEC_PREFERRED_NAME();
 			if (s == null) s = "";
 			strInValid = "";
 			//check if valid system generated name
 			String nameType = m_DEC.getAC_PREF_NAME_TYPE();
 			if (nameType != null && nameType.equals("SYS"))
 			{
 				Vector vObjectClass = (Vector) session.getAttribute("vObjectClass");
 				Vector vProperty = (Vector) session.getAttribute("vProperty");
 				//no object and no property
 				//if ( (vObjectClass == null || vObjectClass.size()<1) && (vProperty == null || vProperty.size()<1) )
 				if ( (vObjectClass != null && vObjectClass.size()>0) && (vProperty == null || vProperty.size()<1) )	//GF31953
 				{
 					String oStatus = m_DEC.getDEC_ASL_NAME();
 					if(oStatus != null && oStatus.equals(DBConstants.ASL_NAME_RELEASED)) {
 						strInValid = "Requires Object Class and Property to create System Generated Short Name.";
 						s = "";
 						UtilService.setValPageVector(vValidate, "Property", s, bMandatory, 255, strInValid, sOriginAction);	//GF31953
 					} else if(oStatus == null) {
 						throw new Exception("Not able to validate Object Class's Property (workflow status is NULL or empty).");
 					}
 				}
 			}			
 			
 			
 			s = m_DEC.getDEC_LONG_NAME();
 			if (s == null) s = "";
 			strInValid = "";
 			UtilService.setValPageVector(vValidate, "Long Name", s, bMandatory, 255, strInValid, sOriginAction);
 
 			//checks uniuqe in the context and name differred for Released
 			if (!s.equals(""))
 			{
 				if (!s.equalsIgnoreCase("(Generated by the System)"))
 					strInValid = checkUniqueInContext("Name", "DEC", null, m_DEC, null, getAC, sDECAction);
 				DEC_Bean oldDEC = (DEC_Bean)session.getAttribute("oldDECBean");
 				if (oldDEC != null && sDECAction.equals("Edit"))
 				{
 					String oName = oldDEC.getDEC_PREFERRED_NAME();
 					String oStatus = oldDEC.getDEC_ASL_NAME();
 					strInValid = strInValid + checkNameDiffForReleased(oName, s, oStatus);
 				}
 			}
 //			if(m_DEC != null) {
 //				s = m_DEC.getDEC_PREFERRED_NAME();	//GF32004 added this line for short name because previously long name value is set to short name (as per line 631)
 //			}
 			UtilService.setValPageVector(vValidate, "Short Name", s, bMandatory, 30, strInValid, sOriginAction);  
 
 			//pref name type
 			s = m_DEC.getAC_PREF_NAME_TYPE();
 			this.setValidatePrefNameType(s, isUserEnter, vValidate, sOriginAction);
 
 			s = m_DEC.getDEC_PREFERRED_DEFINITION();
 			if (s == null) s = "";
 			strInValid = "";
 			UtilService.setValPageVector(vValidate, "Definition", s, bMandatory, 2000, strInValid, sOriginAction);
 			
 			String oldDef = null;
 			if (!s.equals(""))
 			{	
 				DEC_Bean oldDEC = (DEC_Bean)session.getAttribute("oldDECBean");
 				if (oldDEC != null && sDECAction.equals("Edit"))
 				{
 					oldDef = oldDEC.getDEC_PREFERRED_DEFINITION();
 				}
 			}
 			
 			//Check Definition against (constructed) chosen definition, add to Alt Def if not same, add Warning in vValidate vector.
 			System.out.println("req [" + req + "] req.getSession() [" + req.getSession() + "] noldDef [" + oldDef + "] ");
 			String chosenDef = constructChosenDefinition(req.getSession(), "DEC", oldDef);
			if (chosenDef != null && !chosenDef.startsWith(s))  {//Using startsWith if PrefDef is truncated.
 				//add Warning
 				String warningMessage = "Valid \n Note: Your chosen definition is being replaced by standard definition.  Your chosen definition is being added as an alternate definition if it does not exist already.";
 				UtilService.setValPageVector(vValidate, "Alternate Definition",chosenDef, false, 0, warningMessage, sOriginAction);
 			
 				//add Alt Def
 			
 				AltNamesDefsSession altSession = AltNamesDefsSession.getAlternates(req, AltNamesDefsSession._searchDEC);
 				
 				altSession.addAlternateDefinition(chosenDef, m_DEC, m_servlet.getConn());
 				
 				m_DEC.setAlternates(altSession);
 				
 			}
 			//validation for both edit and DEc
 			s = m_DEC.getDEC_CD_NAME();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Conceptual Domain", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
 			s = m_DEC.getDEC_ASL_NAME();
 			if (s == null) s = "";
 			if(s.equals("Released") || s.equals("RELEASED"))
 			{
 				if (sDECAction.equals("Edit") || sDECAction.equals("Create"))
 					strInValid = checkOCPropWorkFlowStatuses(req, res, m_DEC.getDEC_OCL_IDSEQ(), m_DEC.getDEC_PROPL_IDSEQ(), strInValid);
 				/*        if (sDECAction.equals("Edit"))
           strInValid = checkOCPropWorkFlowStatuses(req, res, m_DEC.getDEC_OCL_IDSEQ(), m_DEC.getDEC_PROPL_IDSEQ(), strInValid);
         else if(sDECAction.equals("Create"))
         {
           //EVS_Bean OCBean = (EVS_Bean)session.getAttribute("m_OC");
           //EVS_Bean PCBean = (EVS_Bean)session.getAttribute("m_PC");
           String oc_id = "";
           String prop_id = "";
           if(m_OC != null)
             oc_id = m_OC.getIDSEQ();
           if(m_PC != null)
             prop_id = m_PC.getIDSEQ();
           strInValid = checkOCPropWorkFlowStatuses(req, res, oc_id, prop_id, strInValid);
         }
 				 */
 			}
 			UtilService.setValPageVector(vValidate, "Workflow Status", s, bMandatory, 20, strInValid, sOriginAction);
 
 			s = m_DEC.getDEC_VERSION();
 			strInValid = "";
 			if (s != null && s.equals("")==false)	//[#32158] Validation of DEC missing a Property thows unexpect exception.
 			{
 				strInValid = this.checkVersionDimension(s);   //checkValueIsNumeric(s);
 				DEC_Bean oldDEC = (DEC_Bean)session.getAttribute("oldDECBean");
 				String menuAction = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 				if (oldDEC != null && menuAction.equals("NewDECVersion"))
 				{
 					String oVer = oldDEC.getDEC_VERSION();
 					if (s.equals(oVer))
 						strInValid = strInValid + "Must change the version number to create a new version.\n";
 					//check if new verison is unique within the public id
 					strInValid = strInValid + checkUniqueInContext("Version", "DEC", null, m_DEC, null, getAC, sDECAction);
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Version", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			//add begin and end dates to the validate vector
 			String sB = m_DEC.getDEC_BEGIN_DATE();
 			if (sB == null) sB = "";
 			String sE = m_DEC.getDEC_END_DATE();
 			if (sE == null) sE = "";
 			String wfs = m_DEC.getDEC_ASL_NAME();
 			this.addDatesToValidatePage(sB, sE, wfs, sDECAction, vValidate, sOriginAction);
 
 			/*  s = m_DEC.getDEC_BEGIN_DATE();
       if (s == null) s = "";
       if (sDECAction.equals("Edit"))  // || sOriginAction.equals("BlockEditDEC"))
          UtilService.setValPageVector(vValidate, "Effective Begin Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
       else
          UtilService.setValPageVector(vValidate, "Effective Begin Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_DEC.getDEC_END_DATE();
       if (s == null) s = "";
       String wfs = m_DEC.getDEC_ASL_NAME();
       wfs = wfs.toUpperCase();
      // if (wfs.equals("RETIRED ARCHIVED") || wfs.equals("RETIRED DELETED") || wfs.equals("RETIRED PHASED OUT"))
       if (m_vRetWFS.contains(wfs))
         UtilService.setValPageVector(vValidate, "Effective End Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
       else
         UtilService.setValPageVector(vValidate, "Effective End Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);  */
 
 			//add cs-csi to validate page
 			String sCS = "";
 			String sCSI = "";
 			String sAC = "";
 			if(m_DEC != null && m_DEC.getAC_CS_NAME() != null)	//[#32158] Validation of DEC missing a Property thows unexpect exception.
 			{
 				Vector vACCSI = (Vector)m_DEC.getAC_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
 				if (vACCSI != null && !vACCSI.isEmpty())
 				{
 					for (int i=0; i<vACCSI.size(); i++)
 					{
 						AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
 						CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
 						if (i>0)
 						{
 							sCS += ", ";
 							sCSI += ", ";
 							sAC +=", ";
 						}
 						sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
 						sCSI += csiBean.getCSI_NAME(); // accsiBean.getCSI_NAME();
 						sAC += accsiBean.getAC_LONG_NAME();
 					}
 				}
 			}
 
 			UtilService.setValPageVector(vValidate, "Classification Scheme", sCS, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 			strInValid = "";
 			if (sCS.equals("")==false && (sCSI.equals("") || sCSI == null))   //it is a pair
 				strInValid = "Items must be selected for the selected Classification Scheme.";
 			UtilService.setValPageVector(vValidate, "Classification Scheme Items", sCSI, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			//contacts
 			Hashtable hConts = m_DEC.getAC_CONTACTS();
 			strInValid = "";
 			s = "";
 			if (hConts != null && hConts.size()>0)
 			{
 				Enumeration enum1 = hConts.keys();
 				//loop through the contacts
 				while (enum1.hasMoreElements())
 				{
 					String sKey = (String)enum1.nextElement();
 					//get the contact bean
 					AC_CONTACT_Bean acc = (AC_CONTACT_Bean)hConts.get(sKey);
 					String sSubmit = "";
 					if (acc != null) sSubmit = acc.getACC_SUBMIT_ACTION();
 					//add to the string if not deleted
 					if (sSubmit != null && !sSubmit.equals("DEL"))
 					{
 						if (!s.equals("")) s += ", ";
 						s += sKey;
 					}
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Contacts", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			s = m_DEC.getDEC_SOURCE();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Data Element Concept Origin", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			s = m_DEC.getDEC_CHANGE_NOTE();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Change Note", s, bNotMandatory, 2000, "", sOriginAction);
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - setValidatePageValuesDEC " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error setValidatePageValuesDEC");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 
 		// finally, send vector to JSP
 		//store it in teh request
 		Vector<String> vValString = this.makeStringVector(vValidate);
 		req.setAttribute("vValidate", vValString);
 	}
 	private String constructChosenDefinition(HttpSession session, String type, String oldDef) {
 		String def = "";
  
 		if (type.equals("DEC")) {
 			Vector<String> ocDefs = (Vector<String>) session.getAttribute("chosenOCDefs");
 			Vector<String> propDefs = (Vector<String>) session.getAttribute("chosenPropDefs");
 			
 			//If they're null, that means we didn't change objectClass/property
 			if (ocDefs == null)
 				ocDefs = getDefs((Vector<EVS_Bean>)session.getAttribute("vObjectClass"));
 			if (propDefs == null)
 				propDefs = getDefs((Vector<EVS_Bean>)session.getAttribute("vProperty"));
 			
 			
 			def = getDECDef(oldDef, ocDefs, propDefs);
 			
 		} else //VD
 		{
 			Vector<String> repDefs = (Vector<String>) session.getAttribute("chosenRepDefs");
 			
 			if (repDefs == null)
 				repDefs = getDefs((Vector<EVS_Bean>)session.getAttribute("vRepTerm"));
 			
 			VD_Bean oldVD = (VD_Bean)session.getAttribute("oldVDBean");
 		
 			def = getVDDef(oldDef, repDefs);
 		}
 		
 		return def;
 	}
 
 	public Vector<String> getDefs(Vector<EVS_Bean> vConcepts) {
 		Vector<String> defs = new Vector<String>();
 		
 		if(vConcepts != null) {	//32158
 			for (EVS_Bean eb: vConcepts) {
 				String definition = eb.getPREFERRED_DEFINITION();
 				defs.add(definition);
 			}
 		}
 		
 		return defs;
 	}
 	
 	public String getDECDef(String oldDef, Vector<String> vObjectClass, Vector<String> vProperty)
 	{	
 		String sDef = "";
 		if (oldDef != null && oldDef.length() > 0)
 			sDef = oldDef;
 		// get the existing one if not restructuring the name but appending it
 		
 		// add the Object Class qualifiers first
 		for (int i = 1; vObjectClass.size() > i; i++)
 		{
 			String ocDef = vObjectClass.elementAt(i);
 							
 			if (ocDef.startsWith("*")) { //Add only if new 
 				ocDef = ocDef.substring(1); //peel off the *
 				if (!sDef.equals(""))
 					sDef += "_"; // add definition
 				sDef += ocDef;
 			}
 			
 		}
 		// add the Object Class primary
 		if (vObjectClass != null && vObjectClass.size() > 0)
 		{
 			String ocDef = vObjectClass.elementAt(0);
 			if (ocDef != null && ocDef.startsWith("*")) { //Add only if new //32158
 				ocDef = ocDef.substring(1); //peel off the *
 				if (!sDef.equals(""))
 					sDef += "_"; // add definition
 				sDef += ocDef;		
 			}
 		}
 		// get the Property into the long name and abbr name
 		// add the property qualifiers first
 		for (int i = 1; vProperty.size() > i; i++)
 		{
 			String propDef = vProperty.elementAt(i);
 			if (propDef.startsWith("*")) { //Add only if new 
 				propDef = propDef.substring(1); //peel off the *
 				if (!sDef.equals(""))
 					sDef += "_"; // add definition
 				sDef += propDef;
 			}
 				
 			
 		}
 		// add the property primary
 		if (vProperty != null && vProperty.size() > 0)
 		{
 			String propDef = vProperty.elementAt(0);
			if (propDef != null && propDef.startsWith("*")) { //Add only if new 
 				propDef = propDef.substring(1); //peel off the *
 				if (!sDef.equals(""))
 					sDef += "_"; // add definition
 				sDef += propDef;
 			}
 		}
 		
 		return sDef;
 	}
 	
 	public String getVDDef(String oldDef, Vector<String> vRepTerm)
 	{	
 		String sDef = "";
 		if (oldDef != null && oldDef.length() > 0)
 			sDef = oldDef;
 		// get the existing one if not restructuring the name but appending it
 		
 		// add the Object Class qualifiers first
 		for (int i = 1; vRepTerm.size() > i; i++)
 		{
 			String ocDef = vRepTerm.elementAt(i);
 			if (ocDef.startsWith("*")) { //Add only if new 
 				ocDef = ocDef.substring(1); //peel off the *
 				if (!sDef.equals(""))
 					sDef += "_"; // add definition
 				sDef += ocDef;
 			}
 			
 		}
 		// add the Object Class primary
 		if (vRepTerm != null && vRepTerm.size() > 0)
 		{
 			String ocDef = vRepTerm.elementAt(0);
 			if (ocDef != null && ocDef.startsWith("*")) { //Add only if new 
 				ocDef = ocDef.substring(1); //peel off the *		
 				if (!sDef.equals(""))
 					sDef += "_"; // add definition
 				sDef += ocDef;			
 			}
 		}
 		
 		return sDef;
 	}
 	/**
 	 * To check validity of the data for Value Domain component before submission, called from CurationServlet.
 	 * Validation is done against Database restriction and ISO1179 standards.
 	 * Some validations are seperated according to Edit or Create actions.
 	 * calls various methods to get validity messages and store it into the vector.
 	 * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_VD ValueDomain Bean.
 	 * @param m_OC
 	 * @param m_PC
 	 * @param m_REP
 	 * @param m_OCQ
 	 * @param m_PCQ
 	 * @param m_REPQ
 	 * @param getAC reference to GetACService class.
 	 *
 	 */
 	public void setValidatePageValuesVD(HttpServletRequest req, HttpServletResponse res,
 			VD_Bean m_VD, EVS_Bean m_OC, EVS_Bean m_PC, EVS_Bean m_REP, EVS_Bean m_OCQ,
 			EVS_Bean m_PCQ, EVS_Bean m_REPQ, GetACService getAC) //throws ServletException,IOException, Exception
 	{
 		Vector<ValidateBean> vValidate = new Vector<ValidateBean>();
 		//Vector<String> vs = new Vector<String>();
 		try
 		{
 			//System.out.println("setValidatePageValuesVD");
 			HttpSession session = req.getSession();
 			//GetACSearch getACSer = new GetACSearch(req, res, m_servlet);
 			String s;
 			boolean bMandatory = true;
 			boolean bNotMandatory = false;
 			String strInValid = "";
 			//int iLengthLimit = 30;
 			int iNoLengthLimit = -1;
 			String sVDAction = (String)session.getAttribute("VDAction");
 			String sOriginAction = (String)session.getAttribute("originAction");
 			String sUser = (String)session.getAttribute("Username");
 			String sMenu = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 			boolean isUserEnter = false;
 
 			//check edit or create
 			if (sVDAction.equals("EditVD"))
 				sVDAction = "Edit";
 			else
 				sVDAction = "Create";
 
 			//get the right label for pref type
 			if (sVDAction.equals("Create") && (sMenu.equalsIgnoreCase("nothing")
 					|| sMenu.equalsIgnoreCase("NewVDFromMenu") || sOriginAction.indexOf("CreateNewVD") > -1))
 				isUserEnter = true;
 
 			// mandatory for both EDit + editSQL); and Create
 			//      s = m_VD.getVD_REP_TERM();
 			//      if (s == null) s = "";
 			//      strInValid = "";
 			//      UtilService.setValPageVector(vValidate, "Rep Term", s, bMandatory, 225, strInValid, sOriginAction);
 
 			s = m_VD.getVD_CONTEXT_NAME();
 			String sID = m_VD.getVD_CONTE_IDSEQ();
 			if ((sUser != null) && (sID != null))
 				strInValid = checkWritePermission("vd", sUser, sID, getAC);
 			if (strInValid.equals("")) DataManager.setAttribute(session, "sDefaultContext", s);
 			UtilService.setValPageVector(vValidate, "Context", s, bMandatory, 36, strInValid, sOriginAction);
 
 			s = m_VD.getVD_TYPE_FLAG();
 			//String q = "";
 			if (s == null) s = "";
 			if(s.equals("N"))
 				UtilService.setValPageVector(vValidate, "Type", "Non-Enumerated", bMandatory, iNoLengthLimit, "", sOriginAction);
 			else if(s.equals("E"))
 				UtilService.setValPageVector(vValidate, "Type", "Enumerated", bMandatory, iNoLengthLimit, "", sOriginAction);
 			else
 				UtilService.setValPageVector(vValidate, "Type", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
 			String checkValidityRep = (String)session.getAttribute("checkValidityRep");
 			if (checkValidityRep != null && checkValidityRep.equals("Yes")){
 				//validate the naming component
 				this.setValidateNameComp(vValidate, "ValueDomain", req, res, null, null, null, m_VD, m_REP);
 			}else{
 				UtilService.setValPageVector(vValidate, "Rep Term", m_VD.getVD_REP_TERM(), bMandatory, iNoLengthLimit, "", sOriginAction);
 			}
 			s = m_VD.getVD_LONG_NAME();
 			if (s == null) s = "";
 			strInValid = "";
 			UtilService.setValPageVector(vValidate, "Long Name", s, bMandatory, 255, strInValid, sOriginAction);
 
 			s = m_VD.getVD_PREFERRED_NAME();
 			if (s == null) s = "";
 			strInValid = "";
 			//checks unique in the context and name differed for Released
 			if (!s.equals(""))
 			{
 				if (!s.equalsIgnoreCase("(Generated by the System)"))
 					strInValid = checkUniqueInContext("Name", "VD", null, null, m_VD, getAC, sVDAction);
 				VD_Bean oldVD = (VD_Bean)session.getAttribute("oldVDBean");
 				if (oldVD != null && sVDAction.equals("Edit"))
 				{
 					String oName = oldVD.getVD_PREFERRED_NAME();
 					String oStatus = oldVD.getVD_ASL_NAME();
 					strInValid = strInValid + checkNameDiffForReleased(oName, s, oStatus);
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Short Name", s, bMandatory, 30, strInValid, sOriginAction);
 
 			//pref name type
 			s = m_VD.getAC_PREF_NAME_TYPE();
 			this.setValidatePrefNameType(s, isUserEnter, vValidate, sOriginAction);
 
 			s = m_VD.getVD_PREFERRED_DEFINITION();
 			if (s == null) s = "";
 			strInValid = "";
 			UtilService.setValPageVector(vValidate, "Definition", s, bMandatory, 2000, strInValid, sOriginAction);
 			String oldDef = null;
 			if (!s.equals(""))
 			{	
 				VD_Bean oldVD = (VD_Bean)session.getAttribute("oldVDBean");
 				if (oldVD != null && sVDAction.equals("Edit"))
 				{
 					oldDef = oldVD.getVD_PREFERRED_DEFINITION();
 				}
 			}
 			//Check Definition against (constructed) chosen definition, add to Alt Def if not same, add Warning in vValidate vector.
 			String chosenDef = constructChosenDefinition(req.getSession(), "VD", oldDef);
 			
 			if (!chosenDef.startsWith(s))  {//Using startsWith if PrefDef is truncated.
 				//add Warning
 				String warningMessage = "Warning: Your chosen definitions are being replaced by standard definitions.  Your chosen definition is being added as an alternate definition if it does not exist already.";
 				UtilService.setValPageVector(vValidate, "Alternate Definition", chosenDef, false, 0, warningMessage, sOriginAction);
 				
 				//add Alt Def
 				
 				AltNamesDefsSession	altSession = AltNamesDefsSession.getAlternates(req, AltNamesDefsSession._searchVD);
 			
 				altSession.addAlternateDefinition(chosenDef,m_VD, m_servlet.getConn());
 				m_VD.setAlternates(altSession);
 			}
 			
 			//same for both edit and new
 			s = m_VD.getVD_CD_NAME();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Conceptual Domain", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
 			s = m_VD.getVD_ASL_NAME();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Workflow Status", s, bMandatory, 20, "", sOriginAction);
 
 			s = m_VD.getVD_VERSION();
 			if (s == null) s = "";
 			strInValid = "";
 			if (s.equals("")==false)
 			{
 				strInValid = this.checkVersionDimension(s);   //checkValueIsNumeric(s);
 				VD_Bean oldVD = (VD_Bean)session.getAttribute("oldVDBean");
 				String menuAction = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 				if (oldVD != null && menuAction.equals("NewVDVersion"))
 				{
 					String oVer = oldVD.getVD_VERSION();
 					if (s.equals(oVer))
 						strInValid = strInValid + "Must change the version number to create a new version.\n";
 					//check if new verison is unique within the public id
 					strInValid = strInValid + checkUniqueInContext("Version", "VD", null, null, m_VD, getAC, sVDAction);
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Version", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			s = m_VD.getVD_DATA_TYPE();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Data Type", s, bMandatory, 20, "", sOriginAction);
 
 			//add parent and permissible values if exist
 			m_VD.setValidateList(vValidate);
 			PVServlet pvser = new PVServlet(req, res, m_servlet);
 			pvser.addPVValidates(m_VD);  //     this.validateVDPVS(req, res, m_VD, vValidate, sOriginAction);
 			vValidate = m_VD.getValidateList();
 
 			//add begin and end dates to the validate vector
 			String sB = m_VD.getVD_BEGIN_DATE();
 			if (sB == null) sB = "";
 			String sE = m_VD.getVD_END_DATE();
 			if (sE == null) sE = "";
 			String wfs = m_VD.getVD_ASL_NAME();
 			this.addDatesToValidatePage(sB, sE, wfs, sVDAction, vValidate, sOriginAction);
 
 			//get value domain other attributes
 			this.setValidateVDOtherAttr(vValidate, m_VD, sOriginAction);
 
 			//add cs-csi to validate page
 			String sCS = "";
 			String sCSI = "";
 			String sAC = "";
 			if(m_VD.getAC_CS_NAME() != null)
 			{
 				Vector vACCSI = (Vector)m_VD.getAC_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
 				if (vACCSI != null && !vACCSI.isEmpty())
 				{
 					for (int i=0; i<vACCSI.size(); i++)
 					{
 						AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
 						CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
 						if (i>0)
 						{
 							sCS += ", ";
 							sCSI += ", ";
 							sAC += ", ";
 						}
 						sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
 						sCSI += csiBean.getCSI_NAME();  // accsiBean.getCSI_NAME();
 						sAC += accsiBean.getAC_LONG_NAME();
 					}
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Classification Scheme", sCS, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 			strInValid = "";
 			if (sCS.equals("")==false && (sCSI.equals("") || sCSI == null))   //it is a pair
 				strInValid = "Items must be selected for the selected Classification Scheme.";
 			UtilService.setValPageVector(vValidate, "Classification Scheme Items", sCSI, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			//contacts
 			Hashtable hConts = m_VD.getAC_CONTACTS();
 			strInValid = "";
 			s = "";
 			if (hConts != null && hConts.size()>0)
 			{
 				Enumeration enum1 = hConts.keys();
 				//loop through the contacts
 				while (enum1.hasMoreElements())
 				{
 					String sKey = (String)enum1.nextElement();
 					//get the contact bean
 					AC_CONTACT_Bean acc = (AC_CONTACT_Bean)hConts.get(sKey);
 					String sSubmit = "";
 					if (acc != null) sSubmit = acc.getACC_SUBMIT_ACTION();
 					//add to the string if not deleted
 					if (sSubmit != null && !sSubmit.equals("DEL"))
 					{
 						if (!s.equals("")) s += ", ";
 						s += sKey;
 					}
 				}
 			}
 			UtilService.setValPageVector(vValidate, "Contacts", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			s = m_VD.getVD_SOURCE();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Value Domain Origin", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			s = m_VD.getVD_CHANGE_NOTE();
 			if (s == null) s = "";
 			UtilService.setValPageVector(vValidate, "Change Note", s, bNotMandatory, 2000, "", sOriginAction);
 
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - setValidatePageValuesVD " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error setValidatePageValuesVD");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 
 		// finally, send vector to JSP
 		Vector<String> vValString = this.makeStringVector(vValidate);
 		req.setAttribute("vValidate", vValString);
 		if(m_VD.getVD_IN_FORM())
 			req.setAttribute("vdIDSEQ", m_VD.getVD_VD_IDSEQ());
 		
 		m_VD.setValidateList(vValidate);
 		
 	}  // end of setValidatePageValuesVD
 
 
 	/**
 	 * To check validity of the data for Permissible Values component before submission, called from CurationServlet.
 	 * Validation is done against Database restriction and ISO1179 standards.
 	 * calls various methods to get validity messages and store it into the vector.
 	 * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_PV Permissible value Bean.
 	 * @param getAC reference to GetACService class.
 	 *
 	 * @throws IOException  If an input or output exception occurred
 	 * @throws ServletException  If servlet exception Occurred
 	 * @throws Exception
 	 */
 	/*  public void setValidatePageValuesPV(HttpServletRequest req, HttpServletResponse res,
         PV_Bean m_PV, GetACService getAC) //throws ServletException,IOException, Exception
   {
     Vector<ValidateBean> vValidate = new Vector<ValidateBean>();
     try
     {
       HttpSession session = req.getSession();
       String s;
       boolean bMandatory = true;
       boolean bNotMandatory = false;
       String strInValid = "";
       //int iLengthLimit = 30;
       int iNoLengthLimit = -1;
       String pvAction = (String)session.getAttribute("PVAction");
 
       s = m_PV.getPV_PV_IDSEQ();
       if (s == null) s = "";
       //validate these only if not block edit
       if (pvAction == null || !(pvAction.equals("editPV") && s.equals("")))
       {
         // mandatory
         s = m_PV.getQUESTION_VALUE();
         if (s != null && !s.equals(""))
           UtilService.setValPageVector(vValidate, "Valid Value", s, bNotMandatory, iNoLengthLimit, strInValid, "");
 
         s = m_PV.getPV_VALUE();
         if (s == null) s = "";
         UtilService.setValPageVector(vValidate, "Value", s, bMandatory, iNoLengthLimit, strInValid, "");
 
         s = m_PV.getPV_SHORT_MEANING();
         if (s == null) s = "";
         strInValid = "";
         UtilService.setValPageVector(vValidate, "Value Meaning", s, bMandatory, 2000, strInValid, "");
 
         s = m_PV.getPV_MEANING_DESCRIPTION();
         if (s == null) s = "";
         UtilService.setValPageVector(vValidate, "Value Meaning Description", s, bNotMandatory, 2000, "", "");
 
         EVS_Bean evs = (EVS_Bean)m_PV.getVM_CONCEPT();
         if (evs == null) evs = new EVS_Bean();
         s = evs.getCONCEPT_IDENTIFIER();
         if (s != null && !s.equals("") && evs.getEVS_DATABASE() != null)
           s = s + " : " + evs.getEVS_DATABASE();
         if (s == null) s = "";
         UtilService.setValPageVector(vValidate, "EVS Concept Code", s, bNotMandatory, iNoLengthLimit, "", "");
       }
 
       s = m_PV.getPV_VALUE_ORIGIN();
       if (s == null) s = "";
       UtilService.setValPageVector(vValidate, "Value Origin", s, bNotMandatory, iNoLengthLimit, "", "");
 
       //add begin and end dates to the validate vector
       String sB = m_PV.getPV_BEGIN_DATE();
       if (sB == null) sB = "";
       String sE = m_PV.getPV_END_DATE();
       if (sE == null) sE = "";
       //validate these only if not block edit
       if (pvAction == null || !(pvAction.equals("editPV") && s.equals("")))
         this.addDatesToValidatePage(sB, sE, "N/A", "N/A", vValidate, "");
       else
         this.addEditPVDatesToValidatePage(req, sB, sE, vValidate);
 
       s = m_PV.getPV_BEGIN_DATE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Effective Begin Date", s, bNotMandatory, iNoLengthLimit, "", "");
 
       s = m_PV.getPV_END_DATE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Effective End Date", s, bNotMandatory, iNoLengthLimit, "", "");
 
     }
     catch (Exception e)
     {
       logger.fatal("Error - setValidatePageValuesPV " + e.toString(), e);
       ValidateBean vbean = new ValidateBean();
       vbean.setACAttribute("Error setValidatePageValuesPV");
       vbean.setAttributeContent("Error message " + e.toString());
       vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
       vValidate.addElement(vbean);
     }
 
       // finaly, send vector to JSP
     //store it in teh request
     Vector<String> vValString = this.makeStringVector(vValidate);
     req.setAttribute("vValidate", vValString);
    }  // end of setValidatePageValuesPV
 	 */
 	/**
 	 * add the validate message the validation vector
 	 * @param sType String selected pref name type
 	 * @param isUser boolean is name type selected is user
 	 * @param vValidate Vector validate message vector
 	 * @param sOrigin String originAction
 	 */
 	private void setValidatePrefNameType(String sType, boolean isUser, Vector<ValidateBean> vValidate,
 			String sOrigin)
 	{
 		try
 		{
 			if (sType == null || sType.equals("")) sType = "USER";
 			//use the exanded description of the type
 			if(sType.equalsIgnoreCase("ABBR")) sType = "Abbreviated";
 			else if(sType.equalsIgnoreCase("SYS")) sType = "System Generated";
 			else if(sType.equalsIgnoreCase("USER"))
 			{
 				if (isUser) sType = "User Entered";  //edit/version/nue pages
 				else sType = "Existing Name";      //create new page
 			}
 			UtilService.setValPageVector(vValidate, "Short Name Type", sType, true, -1, "", sOrigin);
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setValidatePrefNameType " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error setValidatePrefNameType");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 	}
 
 	/**
 	 * makes validation for block edit
 	 * @param req
 	 * @param res
 	 * @param sACType
 	 */
 	public void setValidateBlockEdit(HttpServletRequest req, HttpServletResponse res,
 			String sACType) //throws Exception
 	{
 		Vector<ValidateBean> vValidate = new Vector<ValidateBean>();
 		try
 		{
 			//System.out.println("in setValidateBlockEdit");
 			HttpSession session = req.getSession();
 			//boolean bMandatory = true;
 			boolean bNotMandatory = false;
 			String strInValid = "";
 			//int iLengthLimit = 30;
 			int iNoLengthLimit = -1;
 			//String sDEAction = (String)session.getAttribute("DEAction");
 			String sOriginAction = (String)session.getAttribute("originAction");
 			//String sMenu = (String)session.getAttribute("MenuAction");
 			String s = "", sVer = "", sWF = "", sOrigin = "", sBD = "", sED = "", sCN = "";  //sLan = "",
 			Vector vACCSI = new Vector(), vAC_CS = new Vector(), vACNames = new Vector();
 			//call the method to check validation rules
 			this.checkBlockEditRules(req, res, sACType);
 			//get request and session attributes
 			//String decvdValid = (String)req.getAttribute("DECVDValid");
 			//String regValid = (String)req.getAttribute("REGValid");
 			String begValid = (String)req.getAttribute("BEGValid");
 			String endValid = (String)req.getAttribute("ENDValid");
 			String wfValid = (String)req.getAttribute("WFValid");
 			//get the common attributes for all three
 			DE_Bean de = (DE_Bean)session.getAttribute("m_DE");
 			DEC_Bean dec = (DEC_Bean)session.getAttribute("m_DEC");
 			VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
 			EVS_Bean oc = (EVS_Bean)session.getAttribute("m_OC");
 			EVS_Bean pc = (EVS_Bean)session.getAttribute("m_PC");
 			EVS_Bean rep = (EVS_Bean)session.getAttribute("m_REP");
 			// EVS_Bean ocq = (EVS_Bean)session.getAttribute("m_OCQ");
 			// EVS_Bean pcq = (EVS_Bean)session.getAttribute("m_PCQ");
 			// EVS_Bean repq = (EVS_Bean)session.getAttribute("m_REPQ");
 			if (sACType.equals("DataElement"))
 			{
 				sVer = de.getDE_VERSION();
 				sWF = de.getDE_ASL_NAME();
 				sOrigin = de.getDE_SOURCE();
 				sCN = de.getDE_CHANGE_NOTE();
 				sBD = de.getDE_BEGIN_DATE();
 				sED = de.getDE_END_DATE();
 				vACNames = de.getAC_CS_NAME();
 				vACCSI = (Vector)de.getAC_AC_CSI_VECTOR();
 				vAC_CS = de.getAC_AC_CSI_VECTOR();
 			}
 			else if (sACType.equals("DataElementConcept"))
 			{
 				sVer = dec.getDEC_VERSION();
 				sWF = dec.getDEC_ASL_NAME();
 				sOrigin = dec.getDEC_SOURCE();
 				sCN = dec.getDEC_CHANGE_NOTE();
 				sBD = dec.getDEC_BEGIN_DATE();
 				sED = dec.getDEC_END_DATE();
 				vACNames = dec.getAC_CS_NAME();
 				vACCSI = (Vector)dec.getAC_AC_CSI_VECTOR();
 				vAC_CS = dec.getAC_AC_CSI_VECTOR();
 			}
 			else if (sACType.equals("ValueDomain"))
 			{
 				sVer = vd.getVD_VERSION();
 				sWF = vd.getVD_ASL_NAME();
 				sOrigin = vd.getVD_SOURCE();
 				sCN = vd.getVD_CHANGE_NOTE();
 				sBD = vd.getVD_BEGIN_DATE();
 				sED = vd.getVD_END_DATE();
 				vACNames = vd.getAC_CS_NAME();
 				vACCSI = (Vector)vd.getAC_AC_CSI_VECTOR();
 				vAC_CS = vd.getAC_AC_CSI_VECTOR();
 			}
 
 			//add them to validate pages
 			if (sACType.equals("DataElement"))
 			{
 				//dec attribute
 				s = de.getDE_DEC_NAME();
 				if (s == null) s = "";
 				UtilService.setValPageVector(vValidate, "Data Element Concept", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 				//vd attribute
 				s = de.getDE_VD_NAME();
 				if (s == null) s = "";
 				UtilService.setValPageVector(vValidate, "Value Domain", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 				//pref name type
 				s = de.getAC_PREF_NAME_TYPE();
 				if (s != null && !s.equals("")) //add the value only if was selected
 					this.setValidatePrefNameType(s, false, vValidate, sOriginAction);
 				else
 					UtilService.setValPageVector(vValidate, "Short Name Type", "", false, -1, "", sOriginAction);
 			}
 			else if (sACType.equals("DataElementConcept"))
 			{
 				session.setAttribute("checkValidityOC", "Yes");
 				session.setAttribute("checkValidityProp", "Yes");
 				//validate naming components
 				this.setValidateNameComp(vValidate, sACType, req, res, dec, oc, pc, null, null);
 				//pref name type only if was selected
 				s = dec.getAC_PREF_NAME_TYPE();
 				if (s != null && !s.equals("")) //add the value only if was selected
 					this.setValidatePrefNameType(s, false, vValidate, sOriginAction);
 				else
 					UtilService.setValPageVector(vValidate, "Short Name Type", "", false, -1, "", sOriginAction);
 				//cd attribute
 				s = dec.getDEC_CD_NAME();
 				if (s == null) s = "";
 				UtilService.setValPageVector(vValidate, "Conceptual Domain", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 			}
 			else if (sACType.equals("ValueDomain"))
 			{
 				session.setAttribute("checkValidityRep", "Yes");
 				//validate naming components
 				this.setValidateNameComp(vValidate, sACType, req, res, null, null, null, vd, rep);
 				//cd attribute
 				s = vd.getVD_CD_NAME();
 				if (s == null) s = "";
 				UtilService.setValPageVector(vValidate, "Conceptual Domain", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 			}
 			//workflow status
 			if (sWF == null) sWF = "";
 			UtilService.setValPageVector(vValidate, "Workflow Status", sWF, bNotMandatory, 20, "", sOriginAction);
 
 			//version
 			if (sVer == null) sVer = "";
 			strInValid = "";
 			UtilService.setValPageVector(vValidate, "Version", sVer, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			//registration status
 			if (sACType.equals("DataElement"))
 			{
 				s = de.getDE_REG_STATUS();
 				if (s == null) s = "";
 				strInValid = "";
 				UtilService.setValPageVector(vValidate, "Registration Status", s, bNotMandatory, 50, strInValid, sOriginAction);
 			}
 			//data type attributes
 			if (sACType.equals("ValueDomain"))
 			{
 				s = vd.getVD_DATA_TYPE();
 				if (s == null) s = "";
 				UtilService.setValPageVector(vValidate, "Data Type", s, bNotMandatory, 20, "", sOriginAction);
 			}
 			//begin date
 			if (sBD == null) sBD = "";
 			strInValid = "";
 			if (begValid != null && !begValid.equals(""))
 				strInValid = begValid;  // "Begin Date is null for " + begValid;
 			UtilService.setValPageVector(vValidate, "Effective Begin Date", sBD, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 			//edn date
 			if (sED == null) sED = "";
 			strInValid = "";
 			if (!sWF.equals("") && wfValid != null && !wfValid.equals(""))
 				strInValid = "End Date does not exist in " + wfValid;
 			if (endValid != null && !endValid.equals(""))
 			{
 				//if (!strInValid.equals("")) strInValid = "\n";
 				strInValid = endValid;
 			}
 			UtilService.setValPageVector(vValidate, "Effective End Date", sED, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 			//document text for DE
 			if (sACType.equals("DataElement"))
 			{
 				s = de.getDOC_TEXT_PREFERRED_QUESTION();
 				if (s == null) s = "";
 				UtilService.setValPageVector(vValidate, "Preferred Question Text", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 			}
 			//other value domain attributes
 			if (sACType.equals("ValueDomain"))
 			{
 				this.setValidateVDOtherAttr(vValidate, vd, sOriginAction);
 			}
 			//add cs-csi to validate page
 			String sCS = "";
 			String sCSI = "";
 			String sAC = "";
 			if(vACNames != null)
 			{
 				//Vector vACCSI = (Vector)de.getAC_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
 				if (vACCSI != null && !vACCSI.isEmpty())
 				{
 					for (int i=0; i<vACCSI.size(); i++)
 					{
 						AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
 						CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
 						if (i>0)
 						{
 							sCS += ", ";
 							sCSI += ", ";
 							sAC += ", ";
 						}
 						sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
 						sCSI += csiBean.getCSI_NAME(); //accsiBean.getCSI_NAME();
 						sAC += accsiBean.getAC_LONG_NAME();
 					}
 				}
 			}
 			//Vector vAC_CS = de.getAC_AC_CSI_VECTOR();
 			Vector vOriginal_ACCS = (Vector)session.getAttribute("vAC_CSI");
 			if (vAC_CS == null) vAC_CS = new Vector();
 			if (vOriginal_ACCS == null) vOriginal_ACCS = new Vector();
 			strInValid = "";
 			//no change Occurred
 			if (vAC_CS.size() == vOriginal_ACCS.size())
 				strInValid = "No Change";
 			//some added or removed cs-csis to/from the existing list.
 			else if (vAC_CS.size() != vOriginal_ACCS.size())
 				strInValid = "Valid";
 			UtilService.setValPageVector(vValidate, "Classification Scheme", "", bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 			UtilService.setValPageVector(vValidate, "Classification Scheme Items", "", bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
 			//origin or source
 			if (sOrigin == null) sOrigin = "";
 			UtilService.setValPageVector(vValidate, "Data Element Origin", sOrigin, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
 			//comment or change note
 			if (sCN == null) sCN = "";
 			UtilService.setValPageVector(vValidate, "Change Note", sCN, bNotMandatory, 2000, "", sOriginAction);
 
 			//  vd.setValidateList(vValidate);
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setValidateBlockEdit " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error setValidateBlockEdit");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 		// finally, send vector to JSP
 		//store it in the request
 		Vector<String> vValString = this.makeStringVector(vValidate);
 		req.setAttribute("vValidate", vValString);
 
 		// }
 	}
 
 	/**
 	 * makes validation for naming components
 	 * @param vValidate validate vector
 	 * @param acType type of ac
 	 * @param req HttpServletRequest
 	 * @param res HttpServletResponse
 	 * @param m_DEC DEC_Bean
 	 * @param m_OC OC_Bean
 	 * @param m_PC PC_Bean
 	 * @param m_VD VD_Bean
 	 * @param m_REP REP_Bean
 	 */
 	private void setValidateNameComp(Vector<ValidateBean> vValidate, String acType, HttpServletRequest req,
 			HttpServletResponse res, DEC_Bean m_DEC, EVS_Bean m_OC, EVS_Bean m_PC,
 			VD_Bean m_VD, EVS_Bean m_REP)
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			GetACSearch getACSer = new GetACSearch(req, res, m_servlet);
 			InsACService insAC = new InsACService(req, res, m_servlet);
 			String sOriginAction = (String)session.getAttribute("originAction");
 			Vector vOC = new Vector();
 			Vector vPROP = new Vector();
 			Vector vREP = new Vector();
 			vOC = (Vector)session.getAttribute("vObjectClass");
 			vPROP = (Vector)session.getAttribute("vProperty");
 			vREP = (Vector)session.getAttribute("vRepTerm");
 			if (vOC == null) vOC = new Vector();
 			if (vPROP == null) vPROP = new Vector();
 			if (vREP == null) vREP = new Vector();
 			String s = "";
 			boolean bMandatory = true;
 			boolean bNotMandatory = false;
 			String strInValid = "";
 			String strOCValid = "";
 			String strPropValid = "";
 			if (acType.equals("DataElementConcept")){
 				String sDECAction = (String)session.getAttribute("DECAction");
 				//check edit or create
 				if (sDECAction.equals("EditDEC"))
 					sDECAction = "Edit";
 				else
 					sDECAction = "Create";
 				String checkValidityOC = (String)session.getAttribute("checkValidityOC");
 				String checkValidityProp = (String)session.getAttribute("checkValidityProp");
 
 
 
 				String sP = m_DEC.getDEC_OCL_NAME_PRIMARY();
 				if (sP == null) sP = "";
 				String sQ = "";
 				Vector vQ = m_DEC.getDEC_OC_QUALIFIER_NAMES();
 				if (vQ != null && vQ.size() > 0)
 					sQ = (String)vQ.elementAt(0);
 				if (sQ == null) sQ = "";
 				String sOCL = m_DEC.getDEC_OCL_NAME();
 				s = m_DEC.getDEC_PROPL_NAME();
 				String strOCInvalid = "";
 				String strWarning = "";
 				String strOCWarning = "";
 				ValidationStatusBean ocStatusBean = new ValidationStatusBean();
 				HashMap<String, String> defaultContext = (HashMap)session.getAttribute("defaultContext");
 
 				//check validity of object class
 				if (checkValidityOC.equals("Yes")){
 					//rule - must have primary if qualifier exists
 					if(!sQ.equals("") && sP.equals(""))
 						strOCInvalid = "Cannot have Qualifier Concepts without a Primary Concept. <br>";
 
 					//checks if concept code used in oc exists in other vocabulary (** need to review this rule **)
 					if (!sQ.equals("") || !sP.equals("") && m_OC != null)
 						strOCInvalid = strOCInvalid + checkConceptCodeExistsInOtherDB(vOC, insAC, null);
 
 					//rule - OCL must be present if associated DE with some Reg Status exist
 					if ((sOCL == null || sOCL.equals("")) && sDECAction.equals("Edit"))
 						strOCInvalid = strOCInvalid + checkDEUsingDEC("ObjectClass", m_DEC, getACSer);
 					else if (sOCL == null || sOCL.equals(""))
 						m_DEC.setDEC_OCL_IDSEQ("");
 
 					//check for warning of oc existance
 					if((sOCL == null || sOCL.equals("")) && !sOriginAction.equals("BlockEditDEC"))
 						strOCWarning = "Warning: a Data Element Concept should have an Object Class.  <br>";
 
 					//checks if new or using existing of oc
 					//check the validity of oc if there is no warning or invalid
 					if ((strOCInvalid == null || strOCInvalid.equals("")) && strOCWarning.equals("")) {
 						if ((vOC != null && vOC.size()>0) && (defaultContext != null && defaultContext.size()>0)){
 							ocStatusBean = insAC.evsBeanCheck(vOC, defaultContext, sOCL, "Object Class");
 						}
 					}
 
 					//set the condr idseq of OC to DEC
 					if (ocStatusBean.isAllConceptsExists()){
 						if (!ocStatusBean.isNewVersion()) {
 							if (ocStatusBean.isCondrExists() && ocStatusBean.getCondrIDSEQ() != null && !ocStatusBean.getCondrIDSEQ().equals("")){
 								m_DEC.setDEC_OC_CONDR_IDSEQ(ocStatusBean.getCondrIDSEQ());
 							}
 							if (ocStatusBean.isEvsBeanExists()&& ocStatusBean.getEvsBeanIDSEQ() != null && !ocStatusBean.getEvsBeanIDSEQ().equals("")){
 								m_DEC.setDEC_OCL_IDSEQ(ocStatusBean.getEvsBeanIDSEQ());
 							}
 							DataManager.setAttribute(session, "ocStatusBean", ocStatusBean);
 						}
 					}
 
 				}
 
 
 				String strPropInvalid = "";
 				String sProp = m_DEC.getDEC_PROPL_NAME_PRIMARY();
 				if (sProp == null) sProp = "";
 				String sQual = "";
 				Vector vQual = m_DEC.getDEC_PROP_QUALIFIER_NAMES();
 				if (vQual != null && vQual.size() > 0)
 					sQual = (String)vQual.elementAt(0);
 				String sPropL = m_DEC.getDEC_PROPL_NAME();
 				if (sPropL == null || sPropL.equals(""))
 					m_DEC.setDEC_PROPL_IDSEQ("");
 				ValidationStatusBean propStatusBean = new ValidationStatusBean();
 
 				//check validity of property
 				if (checkValidityProp.equals("Yes")){
 					//rule - must have primary if qualifier exists
 					if(!sQual.equals("") && sProp.equals(""))
 						strPropInvalid = "Cannot have Qualifier Concepts without a Primary Concept. <br>";
 
 					//checks if concept code used in prop exists in other vocabulary (** need to review this rule **)
 					if(!sQual.equals("") || !sProp.equals("") && m_PC != null)
 						strPropInvalid = strPropInvalid + checkConceptCodeExistsInOtherDB(vPROP, insAC, null);
 
 					//checks if new or using existing of prop
 
 					//check the validity of prop if no error
 					if (strPropInvalid == null || strPropInvalid.equals("")) {
 						if ((vPROP != null && vPROP.size()>0) && (defaultContext != null && defaultContext.size()>0)){
 							propStatusBean = insAC.evsBeanCheck(vPROP, defaultContext, s, "Property");
 						}
 					}
 					//set the condr idseq of prop to DEC
 					if (propStatusBean.isAllConceptsExists()){
 						if (!propStatusBean.isNewVersion()) {
 							if (propStatusBean.isCondrExists() && propStatusBean.getCondrIDSEQ() != null && !propStatusBean.getCondrIDSEQ().equals("")){
 								m_DEC.setDEC_PROP_CONDR_IDSEQ(propStatusBean.getCondrIDSEQ());
 							}
 							if (propStatusBean.isEvsBeanExists() && propStatusBean.getEvsBeanIDSEQ() != null && !propStatusBean.getEvsBeanIDSEQ().equals("")){
 								m_DEC.setDEC_PROPL_IDSEQ(propStatusBean.getEvsBeanIDSEQ());
 							}
 							DataManager.setAttribute(session, "propStatusBean", propStatusBean);
 						}
 					}
 				}
 				String retCode = "";
 				if (checkValidityOC.equals("Yes") || checkValidityProp.equals("Yes")){
 					//display error if not verified properly.
 					retCode = (String)req.getAttribute("retcode");
 				}
 
 				if (checkValidityOC.equals("Yes") || checkValidityProp.equals("Yes")){
 					//check oc prop combination already exists in the database
 					String objID = "";
 					if (checkValidityOC.equals("Yes")){
 						objID = ocStatusBean.getEvsBeanIDSEQ();
 					}else{
 						objID = m_DEC.getDEC_OCL_IDSEQ();
 					}
 					String propID = "";
 					if (checkValidityProp.equals("Yes")){
 						propID = propStatusBean.getEvsBeanIDSEQ();
 					}else{
 						propID = m_DEC.getDEC_PROPL_IDSEQ();
 					}
 
 					if (retCode != null && !retCode.equals(""))
 					{
 						//display error if not verified properly.
 						String sMsg = (String)session.getAttribute(Session_Data.SESSION_STATUS_MESSAGE);
 						if (sMsg == null) sMsg = "";
 						if (sOCL != null && !sOCL.equals("") && (objID == null || objID.equals("")))
 							strOCInvalid = strOCInvalid + sMsg;
 						if (sPropL != null && !sPropL.equals("") && (propID == null || propID.equals("")))
 							strPropInvalid = strPropInvalid + sMsg;
 					}
 					//check oc prop combination already exists in the database //begin of GF30681
 					else if (!sOriginAction.equals("BlockEditDEC"))
 					{
 						if ((objID != null && !objID.equals("")) && (propID != null && !propID.equals(""))){
 							strInValid = insAC.checkUniqueOCPropPair(m_DEC, "UniqueAndVersion", sOriginAction);
 						}else if ((objID != null && !objID.equals("")) &&  (vPROP == null || vPROP.size()<1)){
 							strInValid = insAC.checkUniqueOCPropPair(m_DEC, "UniqueAndVersion", sOriginAction);
 						}else if ((vOC == null || vOC.size()<1) && (propID != null && !propID.equals(""))){
 							strInValid = insAC.checkUniqueOCPropPair(m_DEC, "UniqueAndVersion", sOriginAction);
 						}
 						if (strInValid.startsWith("Warning")) {
 							strWarning += strInValid;
 						} else {
 							strOCInvalid = strOCInvalid + strInValid;
 							strPropInvalid  = strPropInvalid + strInValid;
 						}
 					} //end of GF30681
 				}
 				if (checkValidityOC.equals("Yes")){
 					//add appropriate message to attributes of oc and prop only if there is no invalid message
 					if (strOCInvalid.equals("")) {
 						//add the warning message to valid
 						strOCValid = strOCWarning + strWarning;
 						//add valid word if no warning
 						if (strOCValid.equals(""))
 							strOCValid = "Valid"+ "<br>";
 
 						//add the valid message
 						if (ocStatusBean.getStatusMessage() != null && !ocStatusBean.getStatusMessage().equals("")){
 							strOCValid += ocStatusBean.getStatusMessage();
 						}
 					} else {  //add the warning to invalid message
 						strOCInvalid = strOCInvalid + strOCWarning + strWarning;
 					}
 
 
 				}
 
 				if (checkValidityProp.equals("Yes")){
 					//add appropriate message to attributes of oc and prop only if there is no invalid message
 					if (strPropInvalid.equals("")) {
 						//add the warning message to valid
 						strPropValid = strWarning;
 						//add valid word if no warning
 						if (strPropValid.equals(""))
 							strPropValid = "Valid"+ "<br>";
 						//add the valid message
 						if (propStatusBean.getStatusMessage() != null && !propStatusBean.getStatusMessage().equals("")){
 							strPropValid += propStatusBean.getStatusMessage();
 						}
 					} else {  //add the warning to invalid message
 						strOCInvalid = strOCInvalid + strOCWarning + strWarning;
 						strPropInvalid  = strPropInvalid + strWarning;
 					}
 
 				}
 				UtilService.setValPageVectorForOC_Prop_Rep(vValidate, "Object Class", sOCL, bNotMandatory, 255, strOCInvalid, sOriginAction, strOCValid);	//GF30681
 				//UtilService.setValPageVectorForOC_Prop_Rep(vValidate, "Property", s, bNotMandatory, 255, strPropInvalid, sOriginAction, strPropValid);	//GF31953
 			}
 			else
 			{
 				String sVDAction = (String)session.getAttribute("VDAction");
 				//check edit or create
 				if (sVDAction.equals("EditVD"))
 					sVDAction = "Edit";
 				else
 					sVDAction = "Create";
 
 				String ss = "";
 				String strRepValid ="";
 				ss = m_VD.getVD_REP_TERM();
 				//String repID = m_VD.getVD_REP_IDSEQ();
 				String sP = m_VD.getVD_REP_NAME_PRIMARY();
 				if (sP == null || sP.equals(" ")) sP = "";
 				String sQ = "";
 				Vector vQual = m_VD.getVD_REP_QUALIFIER_NAMES();
 				if (vQual != null && vQual.size() > 0)
 					sQ = (String)vQual.elementAt(0);
 				if(!sQ.equals("") && sP.equals(""))
 					strInValid = "Cannot have Qualifier Concepts without a Primary Concept.<br>";
 				if(!sQ.equals("") || !sP.equals("") && m_REP != null)
 					strInValid = strInValid + checkConceptCodeExistsInOtherDB(vREP, insAC, null);
 				//check the validity of Rep Term if there is no error
 				ValidationStatusBean repStatusBean = new ValidationStatusBean();
 				if ((strInValid == null || strInValid.equals(""))){
 					HashMap<String, String> defaultContext = (HashMap)session.getAttribute("defaultContext");
 					if ((vREP != null && vREP.size()>0) && (defaultContext != null && defaultContext.size()>0)){
 						repStatusBean = insAC.evsBeanCheck(vREP, defaultContext, ss, "Representation Term");
 					}
 					if (repStatusBean.getStatusMessage() != null && !repStatusBean.getStatusMessage().equals("")){
 						strRepValid = "Valid" + "<br> " + repStatusBean.getStatusMessage();
 					}
 				}
 				DataManager.setAttribute(session, "vdStatusBean", repStatusBean);
 
 				if (sOriginAction != null && sOriginAction.equals("BlockEditVD")){
 					UtilService.setValPageVectorForOC_Prop_Rep(vValidate, "Rep Term", ss, bNotMandatory, 255, strInValid, sOriginAction, strRepValid);
 				}else{
 					UtilService.setValPageVectorForOC_Prop_Rep(vValidate, "Rep Term", ss, bMandatory, 255, strInValid, sOriginAction, strRepValid);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setValidateNameComp " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error setValidateNameComp");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 	}
 
 	/**
 	 * to get all other value domain attributes
 	 * @param vValidate
 	 * @param m_VD
 	 * @param sOriginAction
 	 */
 	private void setValidateVDOtherAttr(Vector<ValidateBean> vValidate, VD_Bean m_VD, String sOriginAction) //throws Exception
 	{
 		try
 		{
 			String s = "";
 			String strInValid = "";
 			boolean bNotMandatory = false;
 
 			s = m_VD.getVD_UOML_NAME();
 			if (s == null) s = "";
 			strInValid = checkValueDomainIsTypeMeasurement();
 			UtilService.setValPageVector(vValidate, "Unit Of Measure", s, bNotMandatory, 20, strInValid, sOriginAction);
 
 			s = m_VD.getVD_FORML_NAME();
 			if (s == null) s = "";
 			strInValid = checkValueDomainIsTypeMeasurement();
 			UtilService.setValPageVector(vValidate, "Display Format", s, bNotMandatory, 20, strInValid, sOriginAction);
 
 			s = m_VD.getVD_MIN_LENGTH_NUM();
 			if (s == null) s = "";
 			strInValid = checkValueIsNumeric(s, "Minimum Length");
 			UtilService.setValPageVector(vValidate, "Minimum Length", s, bNotMandatory, 8, strInValid, sOriginAction);
 
 			s = m_VD.getVD_MAX_LENGTH_NUM();
 			if (s == null) s = "";
 			strInValid = checkValueIsNumeric(s, "Maximum Length");  // + checkLessThan8Chars(s);
 			UtilService.setValPageVector(vValidate, "Maximum Length", s, bNotMandatory, 8, strInValid, sOriginAction);
 			s = m_VD.getVD_LOW_VALUE_NUM();
 
 			strInValid = "";
 			strInValid = checkValueIsNumeric(s, "Low Value");   //+ checkValueDomainIsNumeric(s, "Low Value Number Attribute");
 			UtilService.setValPageVector(vValidate, "Low Value", s, bNotMandatory, 255, strInValid, sOriginAction);
 
 			s = m_VD.getVD_HIGH_VALUE_NUM();
 			if (s == null) s = "";
 			strInValid = "";
 			strInValid = checkValueIsNumeric(s, "High Value");  //+ checkValueDomainIsNumeric(s, "High Value Number Attribute");
 			UtilService.setValPageVector(vValidate, "High Value", s, bNotMandatory, 255, strInValid, sOriginAction);
 
 			s = m_VD.getVD_DECIMAL_PLACE();
 			if (s == null) s = "";
 			strInValid = "";
 			strInValid = checkValueIsNumeric(s, "Decimal Place");    //checkValueDomainIsNumeric(s, "Decimal Place Attribute");
 			UtilService.setValPageVector(vValidate, "Decimal Place", s, bNotMandatory, 2, strInValid, sOriginAction);
 
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setValidateVDOtherAttr " + e.toString(), e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error setValidateVDOtherAttr");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 	}
 
 
 	/**
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object
 	 * @param oc_idseq String
 	 * @param prop_idseq  String
 	 * @param strInvalid  String
 	 * @return String value of the invalid message
 	 *
 	 */
 	public String checkOCPropWorkFlowStatuses(HttpServletRequest req,
 			HttpServletResponse res, String oc_idseq, String prop_idseq, String strInvalid)
 	{
 		ResultSet rs = null;
 		Statement cstmt = null;
 		try
 		{
 			String sOC_WFS = "";
 			String sProp_WFS = "";
 			String sOCSQL = "Select asl_name from object_classes_view_ext where oc_idseq = '" + oc_idseq + "'";
 			String sPropSQL = "Select asl_name from PROPERTIES_EXT where prop_idseq = '" + prop_idseq + "'";
 
 			if (oc_idseq == null) oc_idseq = "";
 			if (prop_idseq == null) prop_idseq = "";
 			if(!(oc_idseq.equals("") && prop_idseq.equals(""))) // at least one is in database
 			{
 				if (m_servlet.getConn() == null)  // still null to login page
 					m_servlet.ErrorLogin(req, res);
 				else
 				{
 					if(!oc_idseq.equals(""))
 					{
 						cstmt = m_servlet.getConn().createStatement();
 						rs = cstmt.executeQuery(sOCSQL);
 						//loop through to printout the outstrings
 						while(rs.next())
 						{
 							sOC_WFS = rs.getString(1);
 						}
 						if (sOC_WFS == null) sOC_WFS = "";
 						sOC_WFS = sOC_WFS.toUpperCase();
 						if (!sOC_WFS.equals("RELEASED"))
 							strInvalid = "For DEC Work Flow Status to be 'Released', " +
 							"the Object Class and Property Work Flow Statuses must be 'Released'.";
 					}
 					else if(!prop_idseq.equals(""))
 					{
 						// Now check Property WFStatus
 						cstmt = m_servlet.getConn().createStatement();
 						rs = cstmt.executeQuery(sPropSQL);
 						while(rs.next())
 						{
 							sProp_WFS = rs.getString(1);
 						}
 						if (sProp_WFS == null) sProp_WFS = "";
 						sProp_WFS = sProp_WFS.toUpperCase();
 						if (!sProp_WFS.equals("RELEASED"))
 							strInvalid = "For DEC Work Flow Status to be 'Released', " +
 							"the Object Class and Property Work Flow Statuses must be 'Released'.";
 					}
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("ERROR in checkOCPropWorkFlowStatuses " + e.toString(), e);
 		} finally {
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeStatement(cstmt);
 		}
 		return strInvalid;
 	} //end checkOCPropWorkFlowStatuses
 
 	/**
 	 * To check for existence of value-meaning pair, return idseq if exist, else create new and return idseq
 	 * called from CurationServlet.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param sValue
 	 * @param sMeaning
 	 * @param sCD conceptual domain
 	 * @return String pv idseq
 	 *
 	 */
 	public String createNewPVVM(HttpServletRequest req, HttpServletResponse res,
 			String sValue, String sMeaning, String sCD)
 	{
 		ResultSet rs = null;
 		Statement cstmt = null;
 		String pvIdseq = "";
 		String sSQL = "Select pv_idseq from permissible_values where value = '" + sValue + "'" +
 		" and short_meaning = '" + sMeaning + "'";
 		try
 		{
 			if (m_servlet.getConn() == null)  // still null to login page
 				m_servlet.ErrorLogin(req, res);
 			else
 			{
 				cstmt = m_servlet.getConn().createStatement();
 				rs = cstmt.executeQuery(sSQL);
 				//loop through to printout the outstrings
 				while(rs.next())
 				{
 					pvIdseq = rs.getString(1);
 				}// end of while
 				if (pvIdseq == null)
 					createNewPVVM(req, res, sValue, sMeaning, sCD);
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("ERROR in checkPVVM " + e.toString(), e);
 		}finally {
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeStatement(cstmt);
 		}
 		return pvIdseq;
 	} //end checkPVVM
 
 	/**
 	 * To check write permission for the user, in selected context and selected component,
 	 * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * Calls 'getAC.hasPrivilege' method to get yes or no string value.
 	 *
 	 * @param ACType Selected component type.
 	 * @param sUserName User login Name.
 	 * @param ContID Conte_idseq for the selected context.
 	 * @param getAC reference to GetACService class.
 	 *
 	 * @return String strValid message if no permit or other problem. empty string if has permission.
 	 */
 	public String checkWritePermission(String ACType, String sUserName, String ContID, GetACService getAC)
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
 				sErrorMessage = sUserName + " does not have authorization to create/edit in this context";
 			else
 				sErrorMessage = "Problem with write privileges.";
 		}
 		catch(Exception e)
 		{
 			logger.error("ERROR in checkWritePermission " + e.toString(), e);
 			sErrorMessage = "Error Occurred in checkWritePermission";
 		}
 		return sErrorMessage;
 	}
 
 	/**
 	 * For Block Edit, checks whether Begin Date is after any End dates being edited.
 	 * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * @param req request object
 	 * @param sBeg string begin date
 	 * @param sACType string ac type
 	 *
 	 * @return String strValid message if begin date is after end dates.
 	 */
 	public String validateBegDateVsEndDates(HttpServletRequest req, String sBeg, String sACType)
 	{
 		String strInvalid = "";
 		try
 		{
 			String sPrefName = "";
 			String sEndDate = "";
 			String bDateOK = "";
 			HttpSession session = req.getSession();
 			Vector vBERows = (Vector)session.getAttribute("vBEResult");
 			if (vBERows.size()>0)
 			{
 				for(int i=0; i<(vBERows.size()); i++)
 				{
 					if (sACType.equals("DE"))
 					{
 						DE_Bean de = new DE_Bean();
 						de = (DE_Bean)vBERows.elementAt(i);
 						sPrefName = de.getDE_PREFERRED_NAME();
 						sEndDate = de.getDE_END_DATE();
 						if (sEndDate == null) sEndDate = "";
 						if (!sEndDate.equals(""))
 							bDateOK = compareDates(sBeg, sEndDate);
 						if (bDateOK.equals("true"))
 							strInvalid = strInvalid + "Beg Date exceeds End Date on DE " + sPrefName + ". ";
 					}
 					else if (sACType.equals("DEC"))
 					{
 						DEC_Bean dec = new DEC_Bean();
 						dec = (DEC_Bean)vBERows.elementAt(i);
 						sPrefName = dec.getDEC_PREFERRED_NAME();
 						sEndDate = dec.getDEC_END_DATE();
 						if (sEndDate == null) sEndDate = "";
 						if (!sEndDate.equals(""))
 							bDateOK = compareDates(sBeg, sEndDate);
 						if (bDateOK.equals("true"))
 							strInvalid = strInvalid + "Beg Date exceeds End Date on DEC " + sPrefName + ". ";
 					}
 					else if (sACType.equals("VD"))
 					{
 						VD_Bean vd = new VD_Bean();
 						vd = (VD_Bean)vBERows.elementAt(i);
 						sPrefName = vd.getVD_PREFERRED_NAME();
 						sEndDate = vd.getVD_END_DATE();
 						if (sEndDate == null) sEndDate = "";
 						if (!sEndDate.equals(""))
 							bDateOK = compareDates(sBeg, sEndDate);
 						if (bDateOK.equals("true"))
 							strInvalid = strInvalid + "Beg Date exceeds End Date on VD " + sPrefName + ". ";
 					}
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("ERROR in validateBegDateVsEndDates " + e.toString(), e);
 			strInvalid += "Error Occurred in validateBegDateVsEndDates";
 		}
 		return strInvalid;
 	}
 
 	/**
 	 * For Block Edit, checks whether End Date is before any Begin dates being edited.
 	 * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * @param req HttpServletRequest
 	 * @param sEnd String end date
 	 * @param sACType String ac type
 	 *
 	 * @return String strValid message if end date before begin dates.
 	 */
 	public String validateEndDateVsBeginDates(HttpServletRequest req, String sEnd, String sACType)
 	{
 		String strInvalid = "";
 		String sPrefName = "";
 		String sBegDate = "";
 		String bDateOK = "";
 		HttpSession session = req.getSession();
 		Vector vBERows = (Vector)session.getAttribute("vBEResult");
 		if (vBERows.size()>0)
 		{
 			for(int i=0; i<(vBERows.size()); i++)
 			{
 				if (sACType.equals("DE"))
 				{
 					DE_Bean de = new DE_Bean();
 					de = (DE_Bean)vBERows.elementAt(i);
 					sPrefName = de.getDE_PREFERRED_NAME();
 					sBegDate = de.getDE_BEGIN_DATE();
 					if (sBegDate == null) sBegDate = "";
 					if (!sBegDate.equals(""))
 						bDateOK = compareDates(sBegDate, sEnd);
 					if (bDateOK.equals("true"))
 						strInvalid = strInvalid + "End Date is before Begin Date on DE " + sPrefName + ". ";
 				}
 				else if (sACType.equals("DEC"))
 				{
 					DEC_Bean dec = new DEC_Bean();
 					dec = (DEC_Bean)vBERows.elementAt(i);
 					sPrefName = dec.getDEC_PREFERRED_NAME();
 					sBegDate = dec.getDEC_BEGIN_DATE();
 					if (sBegDate == null) sBegDate = "";
 					if (!sBegDate.equals(""))
 						bDateOK = compareDates(sBegDate, sEnd);
 					if (bDateOK.equals("true"))
 						strInvalid = strInvalid + "End Date is before Begin Date on DEC " + sPrefName + ". ";
 				}
 				else if (sACType.equals("VD"))
 				{
 					VD_Bean vd = new VD_Bean();
 					vd = (VD_Bean)vBERows.elementAt(i);
 					sPrefName = vd.getVD_PREFERRED_NAME();
 					sBegDate = vd.getVD_BEGIN_DATE();
 					if (sBegDate == null) sBegDate = "";
 					if (!sBegDate.equals(""))
 						bDateOK = compareDates(sBegDate, sEnd);
 					if (bDateOK.equals("true"))
 						strInvalid = strInvalid + "End Date is before Begin Date on VD " + sPrefName + ". ";
 				}
 			}
 		}
 		return strInvalid;
 	}
 
 	/**
 	 * For Block Edit, checks whether end Date is before begin Date.
 	 * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * @param sBegDate String begin date to compare
 	 * @param sEndDate String end date to compare
 	 *
 	 * @return String strFail message if date2 before date1.
 	 */
 	public String compareDates(String sBegDate, String sEndDate)
 	{
 		String strFail = "";
 		try
 		{
 			java.util.Date begDate;
 			java.util.Date endDate;
 			java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
 			dateFormat.setLenient(false);
 			begDate = dateFormat.parse(sBegDate);
 			endDate = dateFormat.parse(sEndDate);
 			if (endDate.before(begDate))
 				strFail = "true";
 			else
 				strFail = "false";
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setacservice_compareDates  : " + e.toString(), e);
 			return "Error Occurred in validating Begin and End Dates";
 		}
 		return strFail;
 	}
 
 	private String validateDateFormat(String sDate)
 	{
 		String validDate = "";
 		if (sDate != null && !sDate.equals(""))
 		{
 			try
 			{
 				java.util.Date dDate;
 				java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
 				dateFormat.setLenient(false);
 				Calendar dCal = Calendar.getInstance();
 				dDate = dateFormat.parse(sDate);
 				dCal.setTime(dDate);
 				dCal.setLenient(false);
 				if (String.valueOf(dCal.get(Calendar.YEAR)).length() < 4)
 					validDate = "Date must be of format MM/DD/YYYY.";
 			}
 			catch (Exception e)
 			{
 				logger.error("ERROR in validateDateFormat  : " + e.toString(), e);
 				validDate = "Date must be of format MM/DD/YYYY.";
 			}
 		}
 		return validDate;
 	}
 	/**
 	 * For Block Edit, check for various business rules.
 	 *
 	 * @param req  HttpServletRequest
 	 * @param res  HttpServletResponse
 	 * @param sACType type ac for the validation
 	 */
 	public void checkBlockEditRules(HttpServletRequest req, HttpServletResponse res,
 			String sACType)
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			String dvValid = "";  //sDECVal = "", sVDVal = "",
 			String regValid = "";  //, sReg = "";
 			String pgBDate = "", pgEDate = "", bdValid = "", edValid = "";
 			String pgWFStatus = "", wfValid = "";
 			Vector vBERows = (Vector)session.getAttribute("vBEResult");
 			//GetACService getAC = new GetACService(req, res, m_servlet);
 			//get the page attributes
 			if (sACType.equals("DataElement"))
 			{
 				DE_Bean pageDE = (DE_Bean)session.getAttribute("m_DE");
 				//check the validation for dec-vd combination
 				//sDECVal = pageDE.getDE_DEC_IDSEQ();
 				//sVDVal = pageDE.getDE_VD_IDSEQ();
 				//sReg = pageDE.getDE_REG_STATUS();
 				pgBDate = pageDE.getDE_BEGIN_DATE();
 				pgEDate = pageDE.getDE_END_DATE();
 				pgWFStatus = pageDE.getDE_ASL_NAME();
 			}
 			else if (sACType.equals("DataElementConcept"))
 			{
 				DEC_Bean pageDEC = (DEC_Bean)session.getAttribute("m_DEC");
 				pgBDate = pageDEC.getDEC_BEGIN_DATE();
 				pgEDate = pageDEC.getDEC_END_DATE();
 				pgWFStatus = pageDEC.getDEC_ASL_NAME();
 			}
 			else if (sACType.equals("ValueDomain"))
 			{
 				VD_Bean pageVD = (VD_Bean)session.getAttribute("m_VD");
 				pgBDate = pageVD.getVD_BEGIN_DATE();
 				pgEDate = pageVD.getVD_END_DATE();
 				pgWFStatus = pageVD.getVD_ASL_NAME();
 			}
 			//check for validity of the dates
 			String pgDateValid = "";
 			if (pgBDate != null && !pgBDate.equals(""))
 			{
 				bdValid = this.validateDateFormat(pgBDate);
 				if (!bdValid.equals("")) bdValid = "Begin " + bdValid;
 			}
 			if (pgEDate != null && !pgEDate.equals(""))
 			{
 				edValid = this.validateDateFormat(pgEDate);
 				if (!edValid.equals("")) edValid = "End " + edValid;
 			}
 			//mark the validity of the new begin or end date
 			if (!bdValid.equals("") || !edValid.equals(""))
 				pgDateValid = "error";
 
 			//loop through the selected acs and check the changed data against the existing one
 			if (vBERows.size()>0)
 			{
 				for(int i=0; i<(vBERows.size()); i++)
 				{
 					String sWF = "", sBD = "", sED = "", acName = "";
 					if (sACType.equals("DataElement"))
 					{
 						DE_Bean de = (DE_Bean)vBERows.elementAt(i);
 						if (de == null) de = new DE_Bean();
 						acName = de.getDE_LONG_NAME();
 						//dec and vd attributes
 						sWF = de.getDE_ASL_NAME();    //workflow status attributes
 						sBD = de.getDE_BEGIN_DATE();    //begin date attribute
 						sED = de.getDE_END_DATE();      //end date attributes
 					}
 					else if (sACType.equals("DataElementConcept"))
 					{
 						DEC_Bean dec = (DEC_Bean)vBERows.elementAt(i);
 						if (dec == null) dec = new DEC_Bean();
 						acName = dec.getDEC_LONG_NAME();   //long name
 						sWF = dec.getDEC_ASL_NAME();    //workflow status attributes
 						sBD = dec.getDEC_BEGIN_DATE();    //begin date attribute
 						sED = dec.getDEC_END_DATE();      //end date attributes
 						// String sCont = dec.getDEC_CONTE_IDSEQ();
 						// req.setAttribute("blockContext", sCont);
 					}
 					else if (sACType.equals("ValueDomain"))
 					{
 						VD_Bean vd = (VD_Bean)vBERows.elementAt(i);
 						if (vd == null) vd = new VD_Bean();
 						acName = vd.getVD_LONG_NAME();     //long name
 						sWF = vd.getVD_ASL_NAME();    //workflow status attributes
 						sBD = vd.getVD_BEGIN_DATE();    //begin date attribute
 						sED = vd.getVD_END_DATE();      //end date attributes
 					}
 					//below validation is same for all acs
 					//store page attribute in the bean attr variable according to its value on page
 					if (pgWFStatus != null && !pgWFStatus.equals(""))
 						sWF = pgWFStatus;
 					if (pgBDate != null && !pgBDate.equals("") && pgDateValid.equals(""))
 						sBD = pgBDate;
 					if (pgEDate != null && !pgEDate.equals("") && pgDateValid.equals(""))
 						sED = pgEDate;
 					//check begin date end date relationship
 					if (sED != null && !sED.equals(""))
 					{
 						if (sBD != null && !sBD.equals(""))
 						{
 							String dValid = compareDates(sBD, sED);
 							if (dValid == null) dValid = "";
 							if (dValid.equals("true"))
 							{
 								if (!edValid.equals("")) edValid = edValid + ", ";    //add the comma for next selected ac
 								edValid = edValid + acName;
 							}
 							else if (!dValid.equals("false"))  //dValid.equals("error"))
 								edValid = "End date is not a valid date.";
 						}
 					}
 					else  //end date cannot be null for some workflow status
 					{
 						sWF = sWF.toUpperCase();
 						if (m_vRetWFS.contains(sWF))
 						{
 							if (!wfValid.equals("")) wfValid = wfValid + ", ";    //add the comma for next selected ac
 							wfValid = wfValid + acName;
 						}
 					}
 				}   //end loop
 				//append text to acnames that have begin date greater than end date
 				if (pgDateValid.equals("") && !edValid.equals(""))
 					edValid = "Begin Date is not before the End Date for " + edValid;
 				//store teh error messages in teh request
 				req.setAttribute("DECVDValid", dvValid);
 				req.setAttribute("REGValid", regValid);
 				req.setAttribute("BEGValid", bdValid);
 				req.setAttribute("ENDValid", edValid);
 				req.setAttribute("WFValid", wfValid);
 			}  //end if not null
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in checkBlockEditRules  : " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * To check whether data contains only alpha numeric values, called from setValidatePageValuesDE,
 	 * setValidatePageValuesDEC, setValidatePageValuesVD, setValidatePageValuesPV, setValidatePageValuesVM methods.
 	 * First character must be only alphabet or empty space.
 	 * Name field must contain only alphanumeric and '-' character.
 	 *
 	 * @param sValue input data.
 	 * @param sField selected field.
 	 *
 	 * @return String strValid message if invlid. otherwise empty string.
 	 */
 	public String checkValidAlphanumeric(String sValue, String sField)
 	{
 		String strString = "";
 		try
 		{
 			boolean bValidFlag = true;
 			sValue = sValue.trim();
 			// the first character must be alphabets
 			if(sValue.length() < 1)  return "Must be a character. \n";
 			char firstLetter = sValue.charAt(0);
 			if ((Character.isUpperCase(firstLetter)) || (Character.isLowerCase(firstLetter))|| (Character.isWhitespace(firstLetter)))
 				bValidFlag = true;
 			else
 			{
 				bValidFlag = false;
 				strString = "First letter must be alphabetic. ";
 			}
 			//only alphabets and numbers only for Name
 			if (sField.equals("Name"))
 			{
 				for (int i=1; i < sValue.length(); i++)
 				{
 					firstLetter = sValue.charAt(i);
 					if ((Character.isUpperCase(firstLetter)) || (Character.isLowerCase(firstLetter)))
 						bValidFlag = true;
 					else if ((Character.isDigit(firstLetter)) || (Character.isWhitespace(firstLetter)) || (firstLetter == '_'))
 						bValidFlag = true;
 					else
 					{
 						bValidFlag = false;
 						strString = strString + "Name can only be alphanumeric characters. ";
 						break;
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in checkValidAlphanumeric  : " + e.toString(), e);
 			strString = "ERROR in checkValidAlphanumeric ";
 		}
 		// return the string value
 		return strString;
 	}
 
 	/**
 	 * To check whether data is unique value in the database for the selected component,
 	 * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * Creates the sql queries for the selected field, to check if the value exists in the database.
 	 * Calls 'getAC.doComponentExist' to execute the query.
 	 *
 	 * @param mDEC Data Element Concept Bean.
 	 * @param req HttpServletRequest object
 	 * @param res HttpServletResponse object
 	 * @param setAction string set action
 	 *
 	 * @return String retValue message if exists already. Otherwise empty string.
 	 */
 	public String checkUniqueOCPropPair(DEC_Bean mDEC, HttpServletRequest req,
 			HttpServletResponse res, String setAction)
 	{
 		try
 		{
 			//boolean bValidFlag = false;
 			HttpSession session = req.getSession();
 			GetACService getAC = new GetACService(req, res, m_servlet);
 			String sSQL="", editSQL="", propSQL = "", ocSQL = "";  //versSQL="",
 			String menuAction = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 			//check unique if id is not the same for update
 			String sContID = mDEC.getDEC_CONTE_IDSEQ();
 			// String sPublicID = mDEC.getDEC_DEC_ID();
 
 			if (setAction.equalsIgnoreCase("EditDEC") || setAction.equalsIgnoreCase("editDECfromDE")
 					|| menuAction.equals("NewDECVersion"))
 				editSQL = " AND DEC.DEC_ID <> '" +  mDEC.getDEC_DEC_ID() + "'";
 
 			String sOCID = mDEC.getDEC_OCL_IDSEQ();
 			String sPropID = mDEC.getDEC_PROPL_IDSEQ();
 			//get oc sql
 			if (sOCID != null && !sOCID.equals(""))
 				ocSQL = " AND DEC.OC_IDSEQ = '" + sOCID + "'";
 			else
 				ocSQL = " AND DEC.OC_IDSEQ IS NULL";
 			//get prop sql
 			if (sPropID != null && !sPropID.equals(""))
 				propSQL = " AND DEC.PROP_IDSEQ = '" + sPropID + "'";
 			else
 				propSQL = " AND DEC.PROP_IDSEQ IS NULL";
 			//make the query
 			sSQL = "SELECT distinct DEC_ID FROM DATA_ELEMENT_CONCEPTS_VIEW DEC WHERE DEC.CONTE_IDSEQ = '" + sContID + "'"
 			+ ocSQL + propSQL + editSQL;      //versSQL + editSQL;
 			//logger.debug("oc prop pair " + sSQL);
 
 			String sDECID = getAC.isUniqueInContext(sSQL);
 			if (sDECID == null || sDECID.equals(""))
 			{
 				sSQL = "SELECT distinct DEC_ID FROM DATA_ELEMENT_CONCEPTS_VIEW DEC WHERE DEC.CONTE_IDSEQ <> '" + sContID + "'"
 				+ ocSQL + propSQL + editSQL;      //versSQL + editSQL;
 				String sContexts = getAC.isUniqueInContext(sSQL);
 				if(sContexts == null || sContexts.equals(""))
 					return "";
 				else
 				{
 					return "Warning: DEC's with combination of Object Class and Property already exists in other contexts with Public ID(s): " + sContexts;
 				}
 			}
 			else
 				return "Combination of Object Class, Property and Context already exists in DEC with Public ID(s): " + sDECID;
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in checkUniqueOCPropPair  : " + e.toString(), e);
 			return "ERROR in checkUniqueOCPropPair ";
 		}
 	}
 
 	/**
 	 * To check whether data is unique value in the database for the selected component,
 	 * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * Creates the sql queries for the selected field, to check if the value exists in the database.
 	 * Calls 'getAC.doComponentExist' to execute the query.
 	 *
 	 * @param mDE Data Element Bean.
 	 * @param getAC reference to GetACService class.
 	 * @param setAction string action to set
 	 * @param sMenu string menu action
 	 *
 	 * @return String retValue message if exists already. Otherwise empty string.
 	 */
 	public String checkUniqueDECVDPair(DE_Bean mDE, GetACService getAC, String setAction, String sMenu)
 	{
 		try
 		{
 			String sSQL="", editSQL="";  //, versSQL="";
 
 			//check unique if id is not the same for update
 			String sPublicID = mDE.getDE_MIN_CDE_ID();
 
 			if (setAction.equals("Edit") || sMenu.equals("NewDEVersion"))
 				editSQL = " AND DE.CDE_ID <> '" +  sPublicID + "'";
 			/*    if (setAction.equals("Edit"))
          editSQL = " AND DE.DE_IDSEQ <> '" +  mDE.getDE_DE_IDSEQ() + "'";
       else if(setAction.equals("NewDEVersion"))
          versSQL =  "' AND DE.CDE_ID <> '" + sPublicID;  */
 			String sVDID = mDE.getDE_VD_IDSEQ();
 			String sDECID = mDE.getDE_DEC_IDSEQ();
 			String sContID = mDE.getDE_CONTE_IDSEQ();
 			if (sPublicID == null) sPublicID = "";
 			sSQL = "SELECT LONG_NAME FROM DATA_ELEMENTS_VIEW DE WHERE DE.VD_IDSEQ = '" + sVDID +
 			"' AND DE.DEC_IDSEQ = '" + sDECID +   //versSQL +
 			"' AND DE.CONTE_IDSEQ = '" + sContID +
 			"'" + editSQL ;
 
 			String sRegStat = mDE.getDE_REG_STATUS();
 			if (sRegStat == null) sRegStat = "";
 			String sName = getAC.isUniqueInContext(sSQL);
 			if(sName == null || sName.equals(""))
 				return "";
 			else
 			{
 				//if (sRegStat.equalsIgnoreCase("Standard") || sRegStat.equalsIgnoreCase("Candidate")
 				//|| sRegStat.equalsIgnoreCase("Proposed"))
 				if (m_vRegStatus.contains(sRegStat))
 					return "Combination of DEC, VD and Context already exists in DE: " + sName;
 				else
 					return "Warning: Combination of DEC, VD and Context already exists in DE: " + sName;
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in checkUniqueDECVDPair  : " + e.toString(), e);
 			return "ERROR in checkUniqueDECVDPair ";
 		}
 	}
 
 	/**
 	 * To check whether data is unique value in the database for the selected component,
 	 * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * Creates the sql queries for the selected field, to check if the value exists in the database.
 	 * Calls 'getAC.doComponentExist' to execute the query.
 	 *
 	 * @param sField selected field.
 	 * @param ACType input data.
 	 * @param mDE Data Element Bean.
 	 * @param mDEC Data Element Concept Bean.
 	 * @param mVD Value Domain Bean.
 	 * @param getAC reference to GetACService class.
 	 * @param setAction string set action
 	 *
 	 * @return String retValue message if exists already. Otherwise empty string.
 	 */
 	public String checkUniqueInContext(String sField, String ACType, DE_Bean mDE,
 			DEC_Bean mDEC, VD_Bean mVD, GetACService getAC, String setAction)
 	{
 		try
 		{
 			boolean bValidFlag = false;
 			String sSQL=""; String sContext=""; String sValue=""; String sVersion=""; String editSQL="";
 			String retValue = "Not unique within Context and Version. ";
 			if (ACType.equals("DE"))
 			{
 				sContext = mDE.getDE_CONTEXT_NAME();
 				if(sContext == null) sContext = "";
 				sVersion = mDE.getDE_VERSION();
 				if(sVersion == null) sVersion = "";
 				//check unique if id is not the same for update
 				if (setAction.equals("Edit"))
 					editSQL = " AND DE.DE_IDSEQ <> '" +  mDE.getDE_DE_IDSEQ() + "'";
 				/* if (sField.equals("LongName"))
         {
           sValue = mDE.getDE_LONG_NAME();
           sValue = m_util.parsedStringSingleQuoteOracle(sValue);
           sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE, CONTEXTS_VIEW CV" +
                       " WHERE DE.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DE.LONG_NAME = '" + sValue + "'" +
                       " AND DE.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
         }
         else*/ if (sField.equals("Name"))
         {
         	sValue = mDE.getDE_PREFERRED_NAME();
         	sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         	sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE, CONTEXTS_VIEW CV" +
         	" WHERE DE.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DE.PREFERRED_NAME = '" + sValue + "'" +
         	" AND DE.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
         }
         /*    else if (sField.equals("Definition"))
         {
           sValue = mDE.getDE_PREFERRED_DEFINITION();
           sValue = m_util.parsedStringSingleQuoteOracle(sValue);
           sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE, CONTEXTS_VIEW CV" +
                       " WHERE DE.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DE.PREFERRED_DEFINITION = '" + sValue + "'" +
                       " AND CV.NAME = '" + sContext + "'"  + editSQL;
           retValue = "Not unique within the Context. ";
         } */
         else if (sField.equals("Version"))
         {
         	String deID = mDE.getDE_MIN_CDE_ID();
         	sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE WHERE DE.CDE_ID = '" + deID +
         	"' AND DE.VERSION = '" + sVersion + "'";
         	retValue = "Not unique within the Public ID.";
 
         	/*  String sVDID = mDE.getDE_VD_IDSEQ();
           String sDECID = mDE.getDE_DEC_IDSEQ();
           String sContID = mDE.getDE_CONTE_IDSEQ();
           if(sVersion == null) sVersion = "";
           sSQL = "SELECT LONG_NAME FROM DATA_ELEMENTS_VIEW DE WHERE DE.VD_IDSEQ = '" + sVDID + "' AND DE.DEC_IDSEQ = '" +
                   sDECID + "' AND DE.VERSION = '" + sVersion + "' AND DE.CONTE_IDSEQ = '" + sContID + "'" + editSQL; */
         }
 			}
 			else if (ACType.equals("DEC"))
 			{
 				sContext = mDEC.getDEC_CONTEXT_NAME();
 				sVersion = mDEC.getDEC_VERSION();
 				//check unique if id is not the same for update
 				if (setAction.equals("Edit"))
 					editSQL = " AND DEC.DEC_IDSEQ <> '" +  mDEC.getDEC_DEC_IDSEQ() + "'";
 
 				/* if (sField.equals("LongName"))
         {
           sValue = mDEC.getDEC_LONG_NAME();
           sValue = m_util.parsedStringSingleQuoteOracle(sValue);
           sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC, CONTEXTS_VIEW CV" +
                       " WHERE DEC.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DEC.LONG_NAME = '" + sValue + "'" +
                       " AND DEC.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
         }
         else*/ if (sField.equals("Name"))
         {
         	sValue = mDEC.getDEC_PREFERRED_NAME();
         	sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         	sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC, CONTEXTS_VIEW CV" +
         	" WHERE DEC.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DEC.PREFERRED_NAME = '" + sValue + "'" +
         	" AND DEC.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
         }
         else if (sField.equals("Version"))
         {
         	String decID = mDEC.getDEC_DEC_ID();
         	sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC WHERE DEC.DEC_ID = '" + decID +
         	"' AND DEC.VERSION = '" + sVersion + "'";
         	retValue = "Not unique within the Public ID.";
         }
         /*   else if (sField.equals("Definition"))
         {
           sValue = mDEC.getDEC_PREFERRED_DEFINITION();
           sValue = m_util.parsedStringSingleQuoteOracle(sValue);
           sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC, CONTEXTS_VIEW CV" +
                       " WHERE DEC.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DEC.PREFERRED_DEFINITION = '" + sValue + "'" +
                       " AND CV.NAME = '" + sContext + "'" + editSQL;
           retValue = "Not unique within the Context. ";
         } */
 			}
 			else if (ACType.equals("VD"))
 			{
 				sContext = mVD.getVD_CONTEXT_NAME();
 				sVersion = mVD.getVD_VERSION();
 				//check unique if id is not the same for update
 				if (setAction.equals("Edit"))
 					editSQL = " AND VD.VD_IDSEQ <> '" +  mVD.getVD_VD_IDSEQ() + "'";
 
 				/*  if (sField.equals("LongName"))
         {
           sValue = mVD.getVD_LONG_NAME();
           sValue = m_util.parsedStringSingleQuoteOracle(sValue);
           sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD, CONTEXTS_VIEW CV" +
                       " WHERE VD.CONTE_IDSEQ = CV.CONTE_IDSEQ AND VD.LONG_NAME = '" + sValue + "'" +
                       " AND VD.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
         }
         else*/ if (sField.equals("Name"))
         {
         	sValue = mVD.getVD_PREFERRED_NAME();
         	sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         	sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD, CONTEXTS_VIEW CV" +
         	" WHERE VD.CONTE_IDSEQ = CV.CONTE_IDSEQ AND VD.PREFERRED_NAME = '" + sValue + "'" +
         	" AND VD.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
         }
         else if (sField.equals("Version"))
         {
         	String vdID = mVD.getVD_VD_ID();
         	sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD WHERE VD.VD_ID = '" + vdID +
         	"' AND VD.VERSION = '" + sVersion + "'";
         	retValue = "Not unique within the Public ID.";
         }
         /*  else if (sField.equals("Definition"))
         {
           sValue = mVD.getVD_PREFERRED_DEFINITION();
           sValue = m_util.parsedStringSingleQuoteOracle(sValue);
           sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD, CONTEXTS_VIEW CV" +
                       " WHERE VD.CONTE_IDSEQ = CV.CONTE_IDSEQ AND VD.PREFERRED_DEFINITION = '" + sValue + "'" +
                       " AND CV.NAME = '" + sContext + "'" + editSQL;
           retValue = "Not unique within the Context. ";
         } */
 			}
 			/*   if(sField.equals("Version"))
       {
         String sName = getAC.isUniqueInContext(sSQL);
         if(sName == null || sName.equals(""))
           return "";
         else
           return "Combination of DEC, VD, and Version already exists in this context as DE:  " + sName;
       }
       else
       { */
 			bValidFlag = getAC.doComponentExist(sSQL);
 			if (bValidFlag == true)
 				return retValue;
 			else
 				return "";
 			// }
 	}
 		catch (Exception e)
 		{
 			logger.error("ERROR in checkUniqueInContext  : " + e.toString(), e);
 			return "ERROR in checkUniqueInContext ";
 		}
 	}
 
 	/**
 	 * @param sDECid string dec idseq
 	 * @param req HttpServletRequest
 	 * @param res HttpServletResponse
 	 * @return String oc_condr idseq
 	 */
 	public String checkDECOCExist(String sDECid, HttpServletRequest req, HttpServletResponse res)
 	{
 		String strInvalid = "Associated Data Element Concept must have an Object Class.";
 		try
 		{
 			Vector vList = new Vector();
 			//get the DEC attributes from the ID
 			GetACSearch serAC = new GetACSearch(req, res, m_servlet);
 			serAC.doDECSearch(sDECid, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "", "", vList, "0");
 			if (vList != null)
 			{
 				//loop through hte list and find if oc exists
 				for (int i=0; i<vList.size(); i++)
 				{
 					DEC_Bean dec = (DEC_Bean)vList.elementAt(i);
 					String sOC = dec.getDEC_OC_CONDR_IDSEQ();
 					if (sOC != null && !sOC.equals(""))
 						return "";  //found one
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("SetACService_checkDECOCExist- Unable to check if OC exists " + e.toString(), e);
 			return "ERROR Occurred in checkDECOCExist ";
 		}
 		return strInvalid;
 	}
 
 	/**
 	 * To check whether data is unique value in the database for the building blocks,
 	 * called from setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * Creates the sql queries for the selected type, to check if the value exists in the database.
 	 * Calls 'getAC.doBlockExist' to execute the query.
 	 * @param vAC vector of concepts in the search results
 	 * @param insAC class object
 	 * @param m_VM vm bean
 	 *
 	 * @return String retValue message if exists already. Otherwise empty string.
 	 */
 	public String checkConceptCodeExistsInOtherDB(Vector vAC, InsACService insAC, VM_Bean m_VM) //throws Exception
 	{
 		String strInValid = "";
 		try
 		{
 			String sRet = "";
 			//boolean blnBadData = false;
 			if(vAC != null)
 			{
 				for (int m=0; m<vAC.size(); m++)
 				{
 					EVS_Bean evsBean = (EVS_Bean)vAC.elementAt(m);
 					if(evsBean != null && evsBean.getCON_AC_SUBMIT_ACTION() != null && !evsBean.getCON_AC_SUBMIT_ACTION().equals("DEL"))
 					{
 						strInValid = insAC.getConcept(sRet, evsBean, true);
 						if(strInValid == null)
 							strInValid = "";
 						if(strInValid.length()>2)
 						{
 							if(!strInValid.substring(0,2).equals("An"))
 								strInValid = "";
 							else
 								break;
 						}
 					}
 				}
 			}
 			else if(m_VM != null)
 			{
 				EVS_Bean evsBean = m_VM.getVM_CONCEPT();
 				if(evsBean != null)
 				{
 					strInValid = insAC.getConcept(sRet, evsBean, true);
 					if(strInValid == null)
 						strInValid = "";
 					if(strInValid.length()>2)
 					{
 						if(!strInValid.substring(0,2).equals("An"))
 							strInValid = "";
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("SetACService_checkConceptCodeExistsInOtherDB- Unable to check if CC exists " + e.toString(), e);
 			return "ERROR Occurred in checkConceptCodeExistsInOtherDB ";
 		}
 		return strInValid;
 
 	}
 
 	/**
 	 * To check whether data is unique value in the database for the building blocks,
 	 * called from setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * Creates the sql queries for the selected type, to check if the value exists in the database.
 	 * Calls 'getAC.doBlockExist' to execute the query.
 	 *
 	 * @param ACType String selected block type.
 	 * @param m_DEC
 	 * @param getAC reference to GetACService class.
 	 *
 	 * @return String retValue message if exists already. Otherwise empty string.
 	 */
 	public String checkDEUsingDEC(String ACType, DEC_Bean m_DEC, GetACSearch getAC) //throws Exception
 	{
 		String strInValid = "";
 		try
 		{
 			if (ACType != null && ACType.equals("ObjectClass"))
 			{
 				Vector vRes = new Vector();
 				String sID = m_DEC.getDEC_DEC_IDSEQ();
 				if (sID != null && !sID.equals(""))
 					getAC.doDESearch("", "", "","","","", 0, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", sID, "", "", "", "", "", "", vRes,"0", 0);
 				if (vRes != null && vRes.size()>0)
 				{
 					String sRegStatus = "", sDEName = "";
 					for (int i=0; i<vRes.size(); i++)
 					{
 						DE_Bean de = (DE_Bean)vRes.elementAt(i);
 						String sReg = de.getDE_REG_STATUS();
 						if (sReg == null) sReg = "";
 						String sDE = de.getDE_LONG_NAME();
 						//if(sReg.equalsIgnoreCase("Standard") || sReg.equalsIgnoreCase("Candidate")
 						//|| sReg.equalsIgnoreCase("Proposed"))
 						if (m_vRegStatus.contains(sReg))
 						{
 							if (!sRegStatus.equals("")) sRegStatus = sRegStatus + ", ";
 							sRegStatus = sRegStatus + sReg;
 							if (!sDEName.equals("")) sDEName = sDEName + ", ";
 							sDEName = sDEName + sDE;
 						}
 					}
 					if (!sDEName.equals(""))
 						strInValid = "A Data Element of name " + sDEName + " uses this DEC " +
 						"and the DE has a Registration Status of " + sRegStatus + " so the DEC must have an Object Class. \n";
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error in SetACService_checkDEUsingDEC " + e.toString(), e);
 			return "ERROR Occurred in checkDEUsingDEC ";
 		}
 		return strInValid;
 	}
 
 	/**
 	 * check if the workflow status of DEC and VD are released for released DE.
 	 * @param de DE_Bean object
 	 * @param sWFS selected workflow status
 	 * @return String message to send back
 	 */
 	public String checkReleasedWFS(DE_Bean de, String sWFS)
 	{
 		String sValid = "";
 		try
 		{
 			if (!m_ReleaseWFS.contains(sWFS)) return "";  //not released wfs
 
 			//validity check if de's wfs only if one of the released wfs
 			String sDEC_valid = "", sVD_valid = "";
 			//check if dec has valid workflow status
 			DEC_Bean dec = de.getDE_DEC_Bean();
 			if (dec == null) dec = new DEC_Bean();
 			String sDECWF = dec.getDEC_ASL_NAME();
 			if (sDECWF == null || !m_ReleaseWFS.contains(sDECWF))
 				sDEC_valid = "DEC";
 			//check if vd has valid workflow status
 			VD_Bean vd = de.getDE_VD_Bean();
 			if (vd == null) vd = new VD_Bean();
 			String sVDWF = vd.getVD_ASL_NAME();
 			if (sVDWF == null || !m_ReleaseWFS.contains(sVDWF))
 				sVD_valid = "VD";
 			//if not valid_dec or valid_vd, check if de reg status if one of the three
 			if (!sDEC_valid.equals("") || !sVD_valid.equals(""))
 			{
 				String sReg = de.getDE_REG_STATUS(); //get the selected reg status
 				if (sReg == null) sReg = "";
 				sValid = sDEC_valid; //add dec
 				if (!sValid.equals("") && !sVD_valid.equals("")) sValid += " and ";  //add and if vd
 				sValid += sVD_valid; //add the vd
 				//display message if one of the three
 				String strWFS = "";
 				for (int i=1; i<m_ReleaseWFS.size(); i++)
 				{
 					if (!strWFS.equals("")) strWFS += ", ";
 					strWFS += m_ReleaseWFS.elementAt(i);
 				}
 				if (m_vRegStatus.contains(sReg))
 					sValid = "Associated " + sValid + " must be RELEASED or (" + strWFS + ").";
 				else  //give warning otherwise
 					sValid = "Warning: Associated " + sValid + " should be RELEASED or (" + strWFS + ").";
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error- checkReleasedWFS " + e.toString(), e);
 			return "Error Occurred in checkReleasedWFS";
 		}
 		return sValid;
 	}
 
 	/**
 	 * To check whether data is unique value in the database for the building blocks,
 	 * called from setValidatePageValuesDEC, setValidatePageValuesVD methods.
 	 * Creates the sql queries for the selected type, to check if the value exists in the database.
 	 * Calls 'getAC.doBlockExist' to execute the query.
 	 *
 	 * @param ACType String selected block type.
 	 * @param sContext String selected context.
 	 * @param sValue String field value.
 	 * @param getAC reference to GetACService class.
 	 * @param strInValid String message
 	 * @param sASLName Workflow status of the selected ac
 	 *
 	 * @return String retValue message if exists already. Otherwise empty string.
 	 */
 	public String checkUniqueBlock(String ACType, String sContext, String sValue, GetACSearch getAC, String strInValid, String sASLName)
 	{
 		try
 		{
 			//String sSQL = "";
 			Vector<EVS_Bean> vList = new Vector<EVS_Bean>();
 			if (ACType != null && ACType.equals("ObjectClass"))
 			{
 				getAC.do_caDSRSearch(sValue, sContext, sASLName, "", vList, "OC", "", "", "0");
 				if (vList.size() > 0)
 				{
 					EVS_Bean OCBean = new EVS_Bean();
 					OCBean = (EVS_Bean)vList.elementAt(0);
 					String name = OCBean.getLONG_NAME();
 					strInValid = "Object Class already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
 					return strInValid;
 				}
 				else
 					return strInValid;
 			}
 			else if (ACType.equals("Property"))
 			{
 				getAC.do_caDSRSearch(sValue, sContext, sASLName, "", vList, "PROP", "", "", "0");
 				if (vList.size() > 0)
 				{
 					EVS_Bean PCBean = new EVS_Bean();
 					PCBean = (EVS_Bean)vList.elementAt(0);
 					String name = PCBean.getLONG_NAME();
 					strInValid = "Property already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
 					return strInValid;
 				}
 				else
 					return strInValid;
 			}
 			else if (ACType.equals("RepTerm"))
 			{
 				getAC.do_caDSRSearch(sValue, sContext, sASLName, "", vList, "REP", "", "", "0");
 				if (vList.size() > 0)
 				{
 					EVS_Bean PCBean = new EVS_Bean();
 					PCBean = (EVS_Bean)vList.elementAt(0);
 					String name = PCBean.getLONG_NAME();
 					strInValid = "Rep Term already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
 					return strInValid;
 				}
 				else
 					return strInValid;
 			}
 			else if (ACType.equals("ObjectQualifier") || ACType.equals("PropertyQualifier") || ACType.equals("RepQualifier"))
 			{
 				getAC.do_caDSRSearch(sValue, sContext, "", "", vList, "Q", "", "", "0");
 				if (vList.size() > 0)
 				{
 					EVS_Bean PCBean = new EVS_Bean();
 					PCBean = (EVS_Bean)vList.elementAt(0);
 					String name = PCBean.getLONG_NAME();
 					strInValid = "Qualifier already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
 					return strInValid;
 				}
 				else
 					return strInValid;
 			}
 			return strInValid;
 		}
 		catch (Exception e)
 		{
 			logger.error("Error- checkUniqueBlock " + e.toString(), e);
 			return "Error Occurred in checkUniqueBlock";
 		}
 	}
 
 	/**
 	 * This method is no longer in use.
 	 * To check whether DE name contains Object Class, Property and Rep Term, called from nowhere.
 	 *
 	 * @param req HttpServletRequest object.
 	 *
 	 * @return String strValid message if invalid. otherwise empty string.
 	 */
 	public String checkObjectPropertyAndRepTerms(HttpServletRequest req)
 	{
 		//String returnString;
 		String returnString1;
 		String returnString2;
 		String returnString3;
 		String sTmp1, sTmp2, sTmp3;
 
 		sTmp1 = (String)req.getParameter("selObjectClass");
 		sTmp2 = (String)req.getParameter("selPropertyClass");
 		sTmp3 = (String)req.getParameter("RepTerm");
 
 		if (sTmp1 != null && !(sTmp1.equals("")))
 			returnString1 = "";
 		else
 			returnString1 = "Object term must be found in DE Long Name. \n";
 
 		if (sTmp2 != null && !(sTmp2.equals("")))
 			returnString2 = "";
 		else
 			returnString2 = "Property term must be present in DE Long Name. \n";
 
 		if (sTmp3 != null && !(sTmp3.equals("")))
 			returnString3 = "";
 		else
 			returnString3 = "Representation term must be present in DE Long Name. \n";
 
 		return returnString1 + returnString2 + returnString3;
 	}
 
 	/**
 	 * This method is no longer in use.
 	 * To check whether DEC name contains Object Class, Property, called from nowhere.
 	 *
 	 * @param s DEC long name.
 	 * @param sDEObj Object class name.
 	 * @param sDEProp Property Name.
 	 *
 	 * @return String strValid message if not found in the name. otherwise empty string.
 	 */
 	public String checkForLongNameObjectAndPropertyInDECName(String s, String sDEObj, String sDEProp)
 	{
 		String returnString;
 		String returnString2;
 		//check whether sDECObj and DECProp substrings are in Long Name string s
 		boolean bContainsDEObjSubstring = (s.indexOf(sDEObj) >= 0);
 		boolean bContainsDEPropSubstring = (s.indexOf(sDEProp) >= 0);
 
 		if (bContainsDEObjSubstring == true)
 			returnString = "";
 		else
 			returnString = "DE Long Name Object term must be found in DEC Name. \n";
 
 		if (bContainsDEPropSubstring == true)
 			returnString2 = "";
 		else
 			returnString2 = "DE Long Name Property term must be found in DEC Name. \n";
 
 		return returnString + returnString2;
 	}
 
 	/**
 	 * This method is no longer in use.
 	 * To check whether VD name contains Rep Term, called from nowhere.
 	 *
 	 * @param s VD long name.
 	 * @param sDERep Represention term name.
 	 *
 	 * @return String strValid message if not found in the name. otherwise empty string.
 	 */
 	public String checkForVDRepresentationInDELongName(String s, String sDERep)
 	{
 		// Will check whether DE Long Name representation term is in the VD Name;
 		// if not, the VD Name Representation term is not equal to the DE Representation term
 		boolean bVDNameContainsDERepSubstring = (s.indexOf(sDERep) >= 0);
 		if (bVDNameContainsDERepSubstring == true)
 			return "";
 		else
 			return "Value Domain Representation term not found in DE Long Name. \n";
 	}
 
 	/**
 	 * To check Unit of Measurement and Format attributes of Value Domain components are valid, called from setValidatePageValuesVD method.
 	 *
 	 * @return String strValid message if not valid. otherwise empty string.
 	 */
 	public String checkValueDomainIsTypeMeasurement()
 	{
 		boolean bValidFlag = true;
 		// validation code here
 		if (bValidFlag == true)
 			return "";
 		else
 			return "Value Domain is not of type Measurement. \n";
 	}
 
 	/**
 	 * To check data is less than 8 characters, called from setValidatePageValuesVD method.
 	 * @param s String data to check.
 	 *
 	 * @return String strValid message if character is greater than 8 characters. otherwise empty string.
 	 */
 	public String checkLessThan8Chars(String s)
 	{
 		int s_length = s.length();
 		if(s_length < 9)
 			return "";
 		else
 			return " Maximum Length must be less than 100,000,000. \n";
 	}
 
 	/**
 	 * To version data dimension is 4,2, called from setValidatePageValuesVD method.
 	 * So, Version should be less that eqaal or less than 99.99 with two decimal character length and two whole number legnth.
 	 *
 	 * @param sValue data to check.
 	 *
 	 * @return String strValid message if character is greater than 99.99. otherwise empty string.
 	 */
 	public String checkVersionDimension(String sValue)
 	{
 		try
 		{
 			String strValid = this.checkValueIsNumeric(sValue, "Version");
 			//return if not a numeric data
 			if (strValid != null && !strValid.equals(""))
 				return strValid;
 			else
 			{
 				strValid = "Version number must be less than 100 with only two digit decimal values. \n";
 
 				//invalid if length is greater than five
 				if (sValue.length()>5)
 					return strValid; //isValid = false;
 				//invalid if point is not found and whole number is greater than 2.
 				else if (sValue.indexOf(".") == -1 && sValue.length()>2)
 					return strValid;
 				//invalid if first half from the point is greater than 2
 				else if (sValue.indexOf(".") != -1 && sValue.substring(0, sValue.indexOf(".")).length()>2)
 					return strValid;
 				//invalid if the second half from the point is greater than 2
 				else if (sValue.indexOf(".") != -1 && sValue.substring(sValue.indexOf(".") + 1).length()>2)
 					return strValid;
 				//invalid if another point is found in the data
 				else if (sValue.indexOf(".") != -1 && sValue.substring(sValue.indexOf(".") + 1).indexOf(".") >= 0)
 					return strValid;
 			}
 			return "";
 		}
 		catch (Exception e)
 		{
 			logger.error("Error- checkVersionDimension " + e.toString(), e);
 			return "Error Occurred in checkVersionDimension";
 		}
 	}
 
 	/**
 	 * To check data is numeric, called from setValidatePageValuesVD method.
 	 *
 	 * @param sValue data to check.
 	 * @param sField ac attributes
 	 *
 	 * @return String strValid message if character is not numeric. otherwise empty string.
 	 */
 	public String checkValueIsNumeric(String sValue, String sField)
 	{
 		try
 		{
 			String sValid = "";
 			char aLetter;
 			if(sValue == null) sValue = "";
 			for (int i=0; i < sValue.length(); i++)
 			{
 				aLetter = sValue.charAt(i);
 				if ((Character.isDigit(aLetter)) || (Character.isWhitespace(aLetter)) || (aLetter == '.'))
 				{
 					if (aLetter == '.' && sField.equals("Decimal Place"))
 					{
 						sValid = "Must contain only positive numbers. \n";
 						break;
 					}
 					else
 						sValid = "";
 				}
 				else
 				{
 					sValid = "Must contain only positive numbers. \n";
 					break;
 				}
 			}
 			return sValid;
 		}
 		catch (Exception e)
 		{
 			logger.error("Error- checkValueIsNumeric " + e.toString(), e);
 			return "Error Occurred in checkValueIsNumeric";
 		}
 	}
 
 	/**
 	 * To truncate term from EVS to 30 characters
 	 *
 	 * @param sValue data to check.
 	 *
 	 * @return String strValid message if character is not numeric. otherwise empty string.
 	 */
 	public String truncateTerm(String sValue)
 	{
 		if(sValue.length() > 0)
 			sValue = sValue.substring(0,30);
 		return sValue;
 	}
 
 	/**
 	 * To check data is Integer, called from setValidatePageValuesVD method.
 	 *
 	 * @param sValue data to check.
 	 * @param sField attribute name.
 	 *
 	 * @return String strValid message if Value Domain is not a numeric Data Type.
 	 * Otherwise empty string.
 	 */
 	public String checkValueDomainIsNumeric(String sValue, String sField)
 	{
 		boolean bValidFlag = true;
 		// validation code here
 		if (bValidFlag == true)
 			return "";
 		else
 			return sField + " must contain only positive numbers.";
 	}
 
 	/**
 	 * To set the valid page vector with attribute, data and message, called from setValidatePageValuesDE,
 	 * setValidatePageValuesDEC, setValidatePageValuesVD, setValidatePageValuesPV, setValidatePageValuesVM methods.
 	 * Attribute Name and Data added to the vector.
 	 * Checks if satisfies mandatory, length limit valid and adds the appropriate messages along with earlier message to the vecotr.
 	 *
 	 * @param v vector to display data on the page.
 	 * @param sItem Name of the attribute.
 	 * @param sContent input value of the attribute.
 	 * @param bMandatory true if attribute is a mandatory for submit.
 	 * @param iLengLimit integer value for length limit if any for the attribute.
 	 * @param strInValid invalid messages from other validation checks.
 	 * @param sOriginAction String origin action
 	 *
 	 */
 	public void setValPageVector(Vector<String> v, String sItem, String sContent, boolean bMandatory, int iLengLimit, String strInValid, String sOriginAction)
 	{
 		String sValid = "Valid";
 		String sNoChange = "No Change";
 		String sMandatory = Constants.DEFAULT_MANDATORY_ATTRIBUTE_TEXT;
 		if(sItem.equals("Effective End Date"))
 			sMandatory = "Effective End Date field is Mandatory for this workflow status. \n";
 
 		v.addElement(sItem);
 		if(sContent == null || sContent.equals("") || sContent.length() < 1)   // content emplty
 		{
 			v.addElement("");   // content
 			if(bMandatory)
 			{
 				v.addElement(sMandatory + strInValid);   //status, required field
 			}
 			else if(strInValid.equals(""))
 			{
 				if (sOriginAction.equals("BlockEditDE") || sOriginAction.equals("BlockEditDEC") || sOriginAction.equals("BlockEditVD"))
 					v.addElement(sNoChange);   //status, OK, even empty, not require
 				else
 					v.addElement(sValid);
 			}
 			else
 				v.addElement(strInValid);
 		}
 		else                      // have something in content
 		{
 			v.addElement(sContent);   // content
 			if(iLengLimit > 0)    // has length limit
 			{
 				if(sContent.length() > iLengLimit)  // not valid
 				{
 					v.addElement(sItem + " is too long. \n" + strInValid);
 				}
 				else
 				{
 					if (strInValid.equals(""))
 						v.addElement(sValid);   //status, OK, not exceed limit
 					else
 						v.addElement(strInValid);
 				}
 			}
 			else
 			{
 				if(strInValid.equals(""))
 					v.addElement(sValid);   //status, OK, not exceed limit
 				else
 					v.addElement(strInValid);
 			}
 		}
 	}
 
 	/**
 	 * To set the values from request to Data Element Bean, called from CurationServlet.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_DE Data Element Bean.
 	 */
 	public void setDEValueFromPage(HttpServletRequest req,
 			HttpServletResponse res, DE_Bean m_DE)
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			String sOriginAction = (String)session.getAttribute("originAction");
 			if (sOriginAction == null) sOriginAction.equals("");
 
 			//get the selected contexts from the DE bean
 			DE_Bean selDE = (DE_Bean)session.getAttribute("m_DE");
 			//keep the seach de attributes if menu action is editdesde
 			//make sure the selected context is saved
 			m_DE.setAC_SELECTED_CONTEXT_ID(selDE.getAC_SELECTED_CONTEXT_ID());
 			String sID;  //sIdx,
 			String sName = "";
 
 			if(sOriginAction.equals("BlockEditDE"))
 				sID = "";
 			else
 				sID = (String)req.getParameter("selContext");
 
 			if ((sID != null) || (!sID.equals("")))
 			{
 				sName = m_util.getNameByID((Vector)session.getAttribute("vContext"),
 						(Vector)session.getAttribute("vContext_ID"), sID);
 				m_DE.setDE_CONTE_IDSEQ(sID);
 				if(sName != null) m_DE.setDE_CONTEXT_NAME(sName);
 			}
 
 			//   if (!sMenu.equals("EditDesDE"))
 			//   {
 			sID = (String)req.getParameter("deIDSEQ");
 			if(sID != null)
 			{
 				m_DE.setDE_DE_IDSEQ(sID);
 			}
 
 			sID = (String)req.getParameter("CDE_IDTxt");
 			if(sID != null)
 			{
 				m_DE.setDE_MIN_CDE_ID(sID);
 			}
 			String sDECid[] = req.getParameterValues("selDEC");
 			if(sDECid != null)
 			{
 				sID = sDECid[0];
 				m_DE.setDE_DEC_IDSEQ(sID);
 				sName = (String)req.getParameter("selDECText");
 				if(sName != null) m_DE.setDE_DEC_NAME(sName);
 			}
 
 			String sVDid[] = req.getParameterValues("selVD");
 			if(sVDid != null)
 			{
 				sID = sVDid[0];
 				m_DE.setDE_VD_IDSEQ(sID);
 				sName = (String)req.getParameter("selVDText");
 				if (sName != null) m_DE.setDE_VD_NAME(sName);
 			}
 
 			//set LONG_NAME
 			// String sTmp;
 			// String sName2;
 
 			if(sOriginAction.equals("BlockEditDE"))
 				sName = "";
 			else
 				sName = (String)req.getParameter("txtLongName");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DE.setDE_LONG_NAME(sName);
 			}
 
 			if(sOriginAction.equals("BlockEditDE"))
 				sName = "";
 			else
 				sName = (String)req.getParameter("txtPreferredName");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DE.setDE_PREFERRED_NAME(sName);
 				/*  String sNameType = (String)req.getParameter("rNameConv");
       m_DE.setAC_PREF_NAME_TYPE(sNameType);
       String sSysName = m_DE.getAC_SYS_PREF_NAME();
       String sAbbName = m_DE.getAC_ABBR_PREF_NAME();
       //make sure to capture the user typed name at any page refresh.
       if (sName != null && !sName.equals("") && !sName.equals("(Generated by the System)")
         && !sName.equals(sSysName) && !sName.equals(sAbbName) && sNameType != null && sNameType.equals("USER"))
         m_DE.setAC_USER_PREF_NAME(sName);  */
 			}
 
 			//set DE_PREFERRED_DEFINITION
 			sName = (String)req.getParameter("CreateDefinition");
 			if (sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DE.setDE_PREFERRED_DEFINITION(sName);
 			}
 
 			//set DOC_TEXT_PREFERRED_QUESTION
 			sName = (String)req.getParameter("CreateDocumentText");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DE.setDOC_TEXT_PREFERRED_QUESTION(sName);
 			}
 			//set DOC_TEXT_PREFERRED_QUESTION
 			sName = (String)req.getParameter("doctextIDSEQ");
 			if(sName != null)
 			{
 				m_DE.setDOC_TEXT_PREFERRED_QUESTION_IDSEQ(sName);
 			}
 
 			//set DE_SOURCE
 			sName = (String)req.getParameter("selSource");
 			if(sName != null)
 			{
 				m_DE.setDE_SOURCE(sName);
 			}
 			//set DE_SOURCE
 			sName = (String)req.getParameter("sourceIDSEQ");
 			if(sName != null)
 			{
 				m_DE.setDE_SOURCE_IDSEQ(sName);
 			}
 
 			//set DE_BEGIN_DATE
 			sName = (String)req.getParameter("BeginDate");
 			if(sName != null)
 			{
 				m_DE.setDE_BEGIN_DATE(sName);
 			}
 
 			//set DE_END_DATE
 			sName = (String)req.getParameter("EndDate");
 			if(sName != null)
 			{
 				m_DE.setDE_END_DATE(sName);
 			}
 
 			//set DE_CHANGE_NOTE
 			sName = (String)req.getParameter("CreateChangeNote");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DE.setDE_CHANGE_NOTE(sName);
 			}
 
 			//set DE_VERSION
 			if(sOriginAction.equals("BlockEditDE"))
 			{
 				sName = (String)req.getParameter("VersionCheck");
 				if(sName == null)
 					sName = "";
 				else
 				{
 					sName = (String)req.getParameter("WholeCheck");
 					if(sName == null)
 					{
 						sName = (String)req.getParameter("PointCheck");
 						if(sName != null)
 							m_DE.setDE_VERSION("Point");
 					}
 					else
 						m_DE.setDE_VERSION("Whole");
 				}
 			}
 			else
 			{
 				sName = (String)req.getParameter("Version");
 				if(sName != null)
 				{
 					sName = sName.trim();
 					String isNum = this.checkValueIsNumeric(sName, "Version");
 					//if numeric and no . and less than 2 length add .0 in the end.
 					if ((isNum == null || isNum.equals("")) && (sName.indexOf(".") == -1 && sName.length() < 3))
 						sName = sName + ".0";
 					m_DE.setDE_VERSION(sName);
 				}
 			}
 
 			//set DE_ASL_NAME
 			sName = (String)req.getParameter("selStatus");
 			if (sName != null)
 				m_DE.setDE_ASL_NAME(sName);
 
 			//set DE_REG_STATUS
 			sName = (String)req.getParameter("selRegStatus");
 			if (sName != null)
 				m_DE.setDE_REG_STATUS(sName);
 
 			//set DE_REG_STATUS_ID
 			sName = (String)req.getParameter("regStatusIDSEQ");
 			if (sName != null)
 				m_DE.setDE_REG_STATUS_IDSEQ(sName);
 
 			sName = (String)req.getParameter("DECDefinition");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DE.setDE_DEC_Definition(sName);
 			}
 
 			sName = (String)req.getParameter("VDDefinition");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DE.setDE_VD_Definition(sName);
 			}
 
 			//cs-csi relationship
 			m_DE = this.setDECSCSIfromPage(req, m_DE);
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - setDEValueFromPage " + e.toString(), e);
 		}
 		//System.out.println("end de page values " );
 	} // end of setDEValueFromPage
 
 
 	/**
 	 * To set the values from request to Data ElementConcept Bean, called from CurationServlet.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_DEC Data Element Concept Bean.
 	 */
 	public void setDECValueFromPage(HttpServletRequest req, HttpServletResponse res, DEC_Bean m_DEC)
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			String sOriginAction = (String)session.getAttribute("originAction");
 			if (sOriginAction == null) sOriginAction.equals("");
 
 			//get the selected contexts from the DE bean
 			DEC_Bean selDEC = (DEC_Bean)session.getAttribute("m_DEC");
 			m_DEC.setAC_SELECTED_CONTEXT_ID(selDEC.getAC_SELECTED_CONTEXT_ID());
 
 			String sID;  //sIdx,
 			String sName = "";
 
 			sID = (String)req.getParameter("decIDSEQ");
 			if(sID != null)
 				m_DEC.setDEC_DEC_IDSEQ(sID);
 
 			sID = (String)req.getParameter("CDE_IDTxt");
 			if(sID != null)
 				m_DEC.setDEC_DEC_ID(sID);
 
 			if(sOriginAction.equals("BlockEditDEC"))
 				sID = "";
 			else
 				sID = (String)req.getParameter("selContext");
 			if ((sID != null) || (!sID.equals("")))
 			{
 				sName = m_util.getNameByID((Vector)session.getAttribute("vContext"), (Vector)session.getAttribute("vContext_ID"), sID);
 				m_DEC.setDEC_CONTE_IDSEQ(sID);
 				m_DEC.setDEC_CONTEXT_NAME(sName);
 			}
 
 			String s = (String)req.getParameter("txtObjClass");
 			if(s != null)
 				m_DEC.setDEC_OCL_NAME(s);
 
 			s = (String)req.getParameter("txtPropClass");
 			if(s != null)
 				m_DEC.setDEC_PROPL_NAME(s);
 
 			sName = "";
 			if(sOriginAction.equals("BlockEditDEC"))
 				sName = "";
 			else
 				sName = (String)req.getParameter("txtLongName");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);
 				m_DEC.setDEC_LONG_NAME(sName);
 			}
 
 			//set PREFERRED_NAME
 			if(sOriginAction.equals("BlockEditDEC"))
 				sName = "";
 			else
 				sName = (String)req.getParameter("txtPreferredName");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);
 				m_DEC.setDEC_PREFERRED_NAME(sName);
 			}
 
 			//set DEC_PREFERRED_DEFINITION
 			sName = (String)req.getParameter("CreateDefinition");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);
 				m_DEC.setDEC_PREFERRED_DEFINITION(sName);
 			}
 
 			sID = (String)req.getParameter("selConceptualDomain");
 			if(sID != null)
 			{
 				m_DEC.setDEC_CD_IDSEQ(sID);
 				sName = (String)req.getParameter("selConceptualDomainText");
 				if ((sName == null) || (sName.equals("")))
 				{
 					if ((Vector)session.getAttribute("vCD") != null)
 						sName = m_util.getNameByID((Vector)session.getAttribute("vCD"),(Vector)session.getAttribute("vCD_ID"), sID);
 				}
 				if(sName != null) m_DEC.setDEC_CD_NAME(sName);
 			}
 
 			//set DEC_SOURCE
 			sName = (String)req.getParameter("selSource");
 			if(sName != null)
 				m_DEC.setDEC_SOURCE(sName);
 
 			//set DEC_BEGIN_DATE
 			sName = (String)req.getParameter("BeginDate");
 			if(sName != null);
 			m_DEC.setDEC_BEGIN_DATE(sName);
 
 			//set DEC_END_DATE
 			sName = (String)req.getParameter("EndDate");
 			if(sName != null)
 				m_DEC.setDEC_END_DATE(sName);
 
 			//set DE_CHANGE_NOTE
 			sName = (String)req.getParameter("CreateChangeNote");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DEC.setDEC_CHANGE_NOTE(sName);
 			}
 
 			//set DEC_VERSION
 			if(sOriginAction.equals("BlockEditDEC"))
 			{
 				sName = (String)req.getParameter("VersionCheck");
 				if(sName == null)
 					sName = "";
 				else
 				{
 					sName = (String)req.getParameter("WholeCheck");
 					if(sName == null)
 					{
 						sName = (String)req.getParameter("PointCheck");
 						if(sName != null)
 							m_DEC.setDEC_VERSION("Point");
 					}
 					else
 						m_DEC.setDEC_VERSION("Whole");
 				}
 			}
 			else
 			{
 				sName = (String)req.getParameter("Version");
 				if(sName != null)
 				{
 					sName = sName.trim();
 					String isNum = this.checkValueIsNumeric(sName, "Version");
 					//if numeric and no . and less than 2 length add .0 in the end.
 					if ((isNum == null || isNum.equals("")) && (sName.indexOf(".") == -1 && sName.length() < 3))
 						sName = sName + ".0";
 					m_DEC.setDEC_VERSION(sName);
 				}
 			}
 
 			//set DEC_ASL_NAME
 			sName = (String)req.getParameter("selStatus");
 			if(sName != null)
 				m_DEC.setDEC_ASL_NAME(sName);
 
 			//set DE_CHANGE_NOTE
 			sName = (String)req.getParameter("CreateChangeNote");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_DEC.setDEC_CHANGE_NOTE(sName);
 			}
 
 			//cs-csi relationship
 			String[] sNAMEs = req.getParameterValues("selCSNAMEHidden");
 			m_DEC.setAC_CS_NAME(this.getSelectionFromPage(sNAMEs));
 
 			//get associated ac-csi
 			Vector<String> vCSCSIs = new Vector<String>(), vACCSIs = new Vector<String>();
 			Vector<String> vACs = new Vector<String>(), vACNames = new Vector<String>();
 			String[] sIDs, sACCSIs, sACs;
 			String sACCSI, sAC;
 			//get selected cs-csi
 			sIDs = req.getParameterValues("selCSCSIHidden");
 			sACCSIs = req.getParameterValues("selACCSIHidden");
 			sACs = req.getParameterValues("selACHidden");
 			Vector vNames = (Vector)session.getAttribute("vACName");
 			Vector vIDs = (Vector)session.getAttribute("vACId");
 
 			if(sIDs != null)
 			{
 				for (int i=0;i<sIDs.length;i++)
 				{
 					sID = sIDs[i];
 					sACCSI = sACCSIs[i];
 					sAC = sACs[i];
 					if (sACCSI == null)  sACCSI = "";
 					if (sAC == null)  sAC = m_DEC.getDEC_DEC_IDSEQ();
 					if ((sID != null) && (!sID.equals("")))
 					{
 						vCSCSIs.addElement(sID);
 						vACCSIs.addElement(sACCSI);
 						vACs.addElement(sAC);
 						//get the ac name
 						String acName = m_DEC.getDEC_LONG_NAME();
 						if (sAC != null && vNames != null && vIDs != null)
 						{
 							if (vIDs.indexOf(sAC) >= 0)
 								acName = (String)vNames.elementAt(vIDs.indexOf(sAC));
 						}
 						vACNames.addElement(acName);
 					}
 				}
 			}
 			m_DEC.setAC_CS_CSI_ID(vCSCSIs);
 
 			//store accsi bean list list in the session
 			Vector vCSList = (Vector)session.getAttribute("CSCSIList");
 			Vector<AC_CSI_Bean> vList = getACCSIFromPage(vCSCSIs, vACCSIs, vCSList, vACs, vACNames);
 			m_DEC.setAC_AC_CSI_VECTOR(vList);
 
 			//get associated ac-csi
 			sIDs = req.getParameterValues("selACCSIHidden");
 			m_DEC.setAC_AC_CSI_ID(this.getSelectionFromPage(sIDs));
 
 			//get associated cs-id
 			sIDs = req.getParameterValues("selectedCS");
 			m_DEC.setAC_CS_ID(this.getSelectionFromPage(sIDs));
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - setDECValueFromPage " + e.toString(), e);
 		}
 	} // end of setDECValueFromPage
 
 	/**
 	 * stores the values from string array into a vector
 	 * returns back the vector.
 	 *
 	 * @param sSelectionList array of string.
 	 * @return Vector of elements from the array.
 	 */
 	private Vector getSelectionFromPage(String[] sSelectionList) //throws ServletException,IOException
 	{
 		Vector<String> vSelections = new Vector<String>();
 		try
 		{
 			if(sSelectionList != null)
 			{
 				for (int i=0;i<sSelectionList.length;i++)
 				{
 					String sID = sSelectionList[i];
 					if ((sID != null) && (!sID.equals("")))
 						vSelections.addElement(sID);
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - setDECValueFromPage " + e.toString(), e);
 		}
 		return vSelections;
 	}
 
 	/**
 	 *
 	 * @param vCSCSIs
 	 * @param vACCSIs
 	 * @param vCSCSIList
 	 * @param vACs
 	 * @param vAC_Name
 	 * @return vector of accsi object
 	 */
 	private Vector<AC_CSI_Bean> getACCSIFromPage(Vector vCSCSIs, Vector vACCSIs, Vector vCSCSIList,
 			Vector vACs, Vector vAC_Name) //throws ServletException,IOException
 			{
 		Vector<AC_CSI_Bean> vACCSIList = new Vector<AC_CSI_Bean>();   //get selected CSCSI atributes of this AC
 		try
 		{
 			//loop through the cscsilist to get csi attributes
 			if (vCSCSIs != null && vCSCSIs.size()>0)
 			{
 				Vector<String> vCSINames = new Vector<String>();
 				Vector<String> vCSNames = new Vector<String>();
 				//get all cs-csi attributes from the list
 				for (int i=0; i<vCSCSIList.size(); i++)
 				{
 					CSI_Bean csiBean = (CSI_Bean)vCSCSIList.elementAt(i);
 					String sCSCSIid = csiBean.getCSI_CSCSI_IDSEQ();
 
 					//match the cscsiid from the bean with the selected cscsi id vector
 					for (int j=0; j<vCSCSIs.size(); j++)
 					{
 						String sCSIID = (String)vCSCSIs.elementAt(j);
 						if (sCSIID.equalsIgnoreCase(sCSCSIid))
 						{
 							//store the attributes in ac-csi bean to retain the selected ones.
 							AC_CSI_Bean accsiBean = new AC_CSI_Bean();
 							accsiBean.setCSCSI_IDSEQ(sCSCSIid);
 							accsiBean.setCSI_BEAN(csiBean);
 							vCSNames.addElement(csiBean.getCSI_CS_LONG_NAME());
 							vCSINames.addElement(csiBean.getCSI_NAME());
 							String ACCSI = "";
 							if (vACCSIs != null)
 							{
 								ACCSI = (String)vACCSIs.elementAt(j);
 								if (ACCSI == null) ACCSI = "";
 							}
 							accsiBean.setAC_CSI_IDSEQ(ACCSI);              //get its ac-csi id
 							//ac id
 							String sAC = "";
 							if (vACs != null)
 							{
 								sAC = (String)vACs.elementAt(j);      //add ac id
 								if (sAC == null) sAC = "";
 							}
 							accsiBean.setAC_IDSEQ(sAC);
 							//ac name
 							if (vAC_Name != null)
 							{
 								sAC = (String)vAC_Name.elementAt(j);      //add ac name
 								if (sAC == null) sAC = "";
 							}
 							accsiBean.setAC_LONG_NAME(sAC);
 							//add bean to the vector
 							vACCSIList.addElement(accsiBean);
 						}
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error Occurred in getACCSIFromPage " + e.toString(), e);
 		}
 		return vACCSIList;
 			}
 
 	/**
 	 *
 	 * @param req
 	 * @param deBean
 	 * @return de bean
 	 */
 	public DE_Bean setDECSCSIfromPage(HttpServletRequest req, DE_Bean deBean)
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			//cs-csi relationship
 			String[] sNAMEs = req.getParameterValues("selCSNAMEHidden");
 			deBean.setAC_CS_NAME(this.getSelectionFromPage(sNAMEs));
 
 			//get associated ac-csi
 			Vector<String> vCSCSIs = new Vector<String>(), vACCSIs = new Vector<String>();
 			Vector<String> vACs = new Vector<String>(), vACNames = new Vector<String>();
 			String[] sIDs, sACCSIs, sACs;
 			String sACCSI, sAC, sID;
 			//get selected cs-csi
 			sIDs = req.getParameterValues("selCSCSIHidden");
 			sACCSIs = req.getParameterValues("selACCSIHidden");
 			sACs = req.getParameterValues("selACHidden");
 			Vector vNames = (Vector)session.getAttribute("vACName");
 			Vector vIDs = (Vector)session.getAttribute("vACId");
 
 			if(sIDs != null)
 			{
 				for (int i=0;i<sIDs.length;i++)
 				{
 					sID = sIDs[i];
 					sACCSI = sACCSIs[i];
 					sAC = sACs[i];
 					if (sACCSI == null)  sACCSI = "";
 					if (sAC == null)  sAC = deBean.getDE_DE_IDSEQ();
 					if ((sID != null) && (!sID.equals("")))
 					{
 						vCSCSIs.addElement(sID);
 						vACCSIs.addElement(sACCSI);
 						vACs.addElement(sAC);
 						//get the ac name
 						String acName = deBean.getDE_LONG_NAME();
 						//  System.out.println(sID + " : " + sACCSI + " : " + sAC + " : " + vIDs.indexOf(sAC) + " : " + vNames.size());
 						if (sAC != null && vNames != null && vIDs != null)
 						{
 							if (vIDs.indexOf(sAC) >= 0)
 								acName = (String)vNames.elementAt(vIDs.indexOf(sAC));
 						}
 						vACNames.addElement(acName);
 					}
 				}
 			}
 			deBean.setAC_CS_CSI_ID(vCSCSIs);
 
 			//store accsi bean list list in the session
 			Vector vCSList = (Vector)session.getAttribute("CSCSIList");
 			Vector<AC_CSI_Bean> vList = getACCSIFromPage(vCSCSIs, vACCSIs, vCSList, vACs, vACNames);
 			deBean.setAC_AC_CSI_VECTOR(vList);//req.setAttribute("vACCSIList", vACCSIList);
 
 			//get associated ac-csi
 			sIDs = req.getParameterValues("selACCSIHidden");
 			deBean.setAC_AC_CSI_ID(this.getSelectionFromPage(sIDs));
 
 			//get associated cs-id
 			sIDs = req.getParameterValues("selectedCS");
 			deBean.setAC_CS_ID(this.getSelectionFromPage(sIDs));
 			// System.out.println(" leaving setacservice_setdecscsivaluefrompage ");
 		}
 		catch (Exception e)
 		{
 			logger.error("ERROR in setacservice_setdecscsivaluefrompage  : " + e.toString(), e);
 		}
 		//return the bean
 		return deBean;
 	}
 
 	/**
 	 * To set the values from request to Value Domain Bean, called from CurationServlet.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_VD Value Domain Bean.
 	 *
 	 */
 	public void setVDValueFromPage(HttpServletRequest req,
 			HttpServletResponse res, VD_Bean m_VD)
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			String sOriginAction = (String)session.getAttribute("originAction");
 			if (sOriginAction == null) sOriginAction.equals("");
 
 			//get the selected contexts from the DE bean
 			VD_Bean selVD = (VD_Bean)session.getAttribute("m_VD");
 			m_VD.setAC_SELECTED_CONTEXT_ID(selVD.getAC_SELECTED_CONTEXT_ID());
 			String sName = "";
 			String sID;  //sIdx,
 			sID = (String)req.getParameter("vdIDSEQ");
 			if(sID != null)
 				m_VD.setVD_VD_IDSEQ(sID);
 			sID = (String)req.getParameter("CDE_IDTxt");
 			if(sID != null)
 				m_VD.setVD_VD_ID(sID);
 
 			if(sOriginAction.equals("BlockEditVD"))
 				sID = "";
 			else
 				sID = (String)req.getParameter("selContext");
 
 			if ((sID != null) || (!sID.equals("")))
 			{
 				sName = m_util.getNameByID((Vector)session.getAttribute("vContext"), (Vector)session.getAttribute("vContext_ID"), sID);
 				m_VD.setVD_CONTE_IDSEQ(sID);
 				m_VD.setVD_CONTEXT_NAME(sName);
 			}
 
 			String s = (String)req.getParameter("txtRepTerm");
 			if(s != null)
 				m_VD.setVD_REP_TERM(s);
 
 			sName = "";
 			if(sOriginAction.equals("BlockEditVD"))
 				sName = "";
 			else
 				sName = (String)req.getParameter("txtLongName");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_VD.setVD_LONG_NAME(sName);
 			}
 			//add the preferred type name
 			String selNameType = (String)req.getParameter("rNameConv");
 			if (selNameType != null)
 				m_VD.setVD_TYPE_NAME(selNameType);
 
 			//set PREFERRED_NAME
 			if(sOriginAction.equals("BlockEditVD"))
 				sName = "";
 			else
 				sName = (String)req.getParameter("txtPreferredName");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_VD.setVD_PREFERRED_NAME(sName);
 			}
 
 			//set VD_PREFERRED_DEFINITION
 			sName = (String)req.getParameter("CreateDefinition");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_VD.setVD_PREFERRED_DEFINITION(sName);
 			}
 
 			sID = (String)req.getParameter("selConceptualDomain");
 			if(sID != null)
 			{
 				m_VD.setVD_CD_IDSEQ(sID);
 				sName = (String)req.getParameter("selConceptualDomainText");
 				if ((sName == null) || (sName.equals("")))
 				{
 					if ((Vector)session.getAttribute("vCD") != null)
 						sName = m_util.getNameByID((Vector)session.getAttribute("vCD"), (Vector)session.getAttribute("vCD_ID"), sID);
 				}
 				if(sName != null) m_VD.setVD_CD_NAME(sName);
 			}
 
 			//set VD_VERSION
 			if(sOriginAction.equals("BlockEditVD"))
 			{
 				sName = (String)req.getParameter("VersionCheck");
 				if(sName == null)
 					sName = "";
 				else
 				{
 					sName = (String)req.getParameter("WholeCheck");
 					if(sName == null)
 					{
 						sName = (String)req.getParameter("PointCheck");
 						if(sName != null)
 							m_VD.setVD_VERSION("Point");
 
 					}
 					else
 						m_VD.setVD_VERSION("Whole");
 				}
 			}
 			else
 			{
 				sName = (String)req.getParameter("Version");
 				if(sName != null)
 				{
 					sName = sName.trim();
 					String isNum = this.checkValueIsNumeric(sName, "Version");
 					//if numeric and no . and less than 2 length add .0 in the end.
 					if ((isNum == null || isNum.equals("")) && (sName.indexOf(".") == -1 && sName.length() < 3))
 						sName = sName + ".0";
 					m_VD.setVD_VERSION(sName);
 				}
 			}
 
 			//set VD_ASL_NAME
 			sName = (String)req.getParameter("selStatus");
 			if(sName != null)
 				m_VD.setVD_ASL_NAME(sName);
 
 			//set VD_DATA_TYPE
 			sName = (String)req.getParameter("selDataType");
 			if(sName != null)
 				m_VD.setVD_DATA_TYPE(sName);
 
 			//set VD_SOURCE
 			sName = (String)req.getParameter("selSource");
 			if(sName != null)
 				m_VD.setVD_SOURCE(sName);
 
 			//set VD_TYPE_FLAG
 			String sVDType = "";
 			if(sOriginAction.equals("BlockEditVD"))
 				sVDType = "";
 			else
 				sVDType = (String)req.getParameter("listVDType");
 			if(sVDType != null)
 			{
 				m_VD.setVD_TYPE_FLAG(sVDType);
 			}
 
 			//set VD_BEGIN_DATE
 			sName = (String)req.getParameter("BeginDate");
 			if(sName != null)
 				m_VD.setVD_BEGIN_DATE(sName);
 
 			//set VD_UOML_NAME
 			sName = (String)req.getParameter("selUOM");
 			if(sName != null)
 				m_VD.setVD_UOML_NAME(sName);
 
 			//set VD_FORML_NAME
 			sName = (String)req.getParameter("selUOMFormat");
 			if(sName != null)
 				m_VD.setVD_FORML_NAME(sName);
 
 			//set VD_MIN_LENGTH_NUM
 			sName = (String)req.getParameter("tfMinLength");
 			if(sName != null)
 				m_VD.setVD_MIN_LENGTH_NUM(sName);
 
 			//set VD_MAX_LENGTH_NUM
 			sName = (String)req.getParameter("tfMaxLength");
 			if(sName != null)
 				m_VD.setVD_MAX_LENGTH_NUM(sName);
 
 			//set VD_LOW_VALUE_NUM
 			sName = (String)req.getParameter("tfLowValue");
 			if(sName != null)
 				m_VD.setVD_LOW_VALUE_NUM(sName);
 
 			//set VD_HIGH_VALUE_NUM
 			sName = (String)req.getParameter("tfHighValue");
 			if(sName != null)
 				m_VD.setVD_HIGH_VALUE_NUM(sName);
 
 			//set VD_DECIMAL_PLACE
 			sName = (String)req.getParameter("tfDecimal");
 			if(sName != null)
 				m_VD.setVD_DECIMAL_PLACE(sName);
 
 			//set VD_END_DATE
 			sName = (String)req.getParameter("EndDate");
 			if(sName != null)
 				m_VD.setVD_END_DATE(sName);
 
 			//set VD_CHANGE_NOTE
 			sName = (String)req.getParameter("CreateChangeNote");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_VD.setVD_CHANGE_NOTE(sName);
 			}
 
 			sName = (String)req.getParameter("ObjDefinition");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_VD.setVD_Obj_Definition(sName);
 			}
 
 			sName = (String)req.getParameter("PropDefinition");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_VD.setVD_Prop_Definition(sName);
 			}
 
 			sName = (String)req.getParameter("RepDefinition");
 			if(sName != null)
 			{
 				sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 				m_VD.setVD_Rep_Definition(sName);
 			}
 
 			//cs-csi relationship
 			String[] sNAMEs = req.getParameterValues("selCSNAMEHidden");
 			m_VD.setAC_CS_NAME(this.getSelectionFromPage(sNAMEs));
 
 			//get associated ac-csi
 			Vector<String> vCSCSIs = new Vector<String>(), vACCSIs = new Vector<String>();
 			Vector<String> vACs = new Vector<String>(), vACNames = new Vector<String>();
 			String[] sIDs, sACCSIs, sACs;
 			String sACCSI, sAC;
 			//get selected cs-csi
 			sIDs = req.getParameterValues("selCSCSIHidden");
 			sACCSIs = req.getParameterValues("selACCSIHidden");
 			sACs = req.getParameterValues("selACHidden");
 			Vector vNames = (Vector)session.getAttribute("vACName");
 			Vector vIDs = (Vector)session.getAttribute("vACId");
 
 			if(sIDs != null)
 			{
 				for (int i=0;i<sIDs.length;i++)
 				{
 					sID = sIDs[i];
 					sACCSI = sACCSIs[i];
 					sAC = sACs[i];
 					if (sACCSI == null)  sACCSI = "";
 					if (sAC == null)  sAC = m_VD.getVD_VD_IDSEQ();
 					if ((sID != null) && (!sID.equals("")))
 					{
 						vCSCSIs.addElement(sID);
 						vACCSIs.addElement(sACCSI);
 						vACs.addElement(sAC);
 						//get the ac name
 						String acName = m_VD.getVD_LONG_NAME();
 						if (sAC != null && vNames != null && vIDs != null)
 						{
 							if (vIDs.indexOf(sAC) >= 0)
 								acName = (String)vNames.elementAt(vIDs.indexOf(sAC));
 						}
 						vACNames.addElement(acName);
 					}
 				}
 			}
 			m_VD.setAC_CS_CSI_ID(vCSCSIs);
 
 			//store accsi bean list list in the session
 			Vector vCSList = (Vector)session.getAttribute("CSCSIList");
 			Vector vList = getACCSIFromPage(vCSCSIs, vACCSIs, vCSList, vACs, vACNames);
 			m_VD.setAC_AC_CSI_VECTOR(vList);
 
 
 			Vector vParentCodes = (Vector)session.getAttribute("vParentCodes");
 			Vector vParentNames = (Vector)session.getAttribute("vParentNames");
 			Vector vParentDB = (Vector)session.getAttribute("vParentDB");
 			Vector vParentMetaSource = (Vector)session.getAttribute("vParentMetaSource");
 			Vector vParentList = (Vector)session.getAttribute("vParentList");
 			m_VD.setVD_PARENT_CODES(vParentCodes);
 			m_VD.setVD_PARENT_NAMES(vParentNames);
 			m_VD.setVD_PARENT_DB(vParentDB);
 			m_VD.setVD_PARENT_META_SOURCE(vParentMetaSource);
 			m_VD.setVD_PARENT_LIST(vParentList);
 
 
 			//get associated ac-csi
 			sIDs = req.getParameterValues("selACCSIHidden");
 			m_VD.setAC_AC_CSI_ID(this.getSelectionFromPage(sIDs));
 
 			//get associated cs-id
 			sIDs = req.getParameterValues("selectedCS");
 			m_VD.setAC_CS_ID(this.getSelectionFromPage(sIDs));
 		}
 		catch (Exception e)
 		{
 			logger.error("Error - setVDValueFromPage " + e.toString(), e);
 		}
 	} // end of setVDValueFromPage
 
 	/**
 	 * To set the values from request to Permissible Value Bean, called from CurationServlet.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param m_PV PV Bean.
 	 *
 	 */
 	public void setPVValueFromPage(HttpServletRequest req,
 			HttpServletResponse res, PV_Bean m_PV) //throws ServletException,IOException
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			//String sIdx, sID;
 			String sName = "";
 			//set PV_ID
 			PV_Bean oldPV = (PV_Bean)session.getAttribute("pageOpenBean");
 			if (oldPV == null) oldPV = oldPV.copyBean(m_PV);
 
 			sName = (String)req.getParameter("selValidValue");
 			if(sName == null) sName = "";
 			m_PV.setQUESTION_VALUE_IDSEQ(sName);
 
 			sName = (String)req.getParameter("txValidValue");
 			if(sName == null) sName = "";
 			m_PV.setQUESTION_VALUE(sName);
 
 			//set PV_VALUE
 			sName = (String)req.getParameter("txtPermValue");
 			if(sName == null) sName = "";
 			sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 			m_PV.setPV_VALUE(sName);
 			m_PV = this.getModifiedPV(m_PV, oldPV, req);   //handle the changed pv
 
 			//set PV_VERSION
 			sName = (String)req.getParameter("selShortMeanings");
 			if(sName == null) sName = "";
 			sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 			m_PV.setPV_SHORT_MEANING(sName);
 
 			//set PV_ASL_NAME
 			sName = (String)req.getParameter("CreateDescription");
 			if(sName == null) sName = "";
 			sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
 			if (sName.length() > 2000) sName = sName.substring(0, 2000);
 			m_PV.setPV_MEANING_DESCRIPTION(sName);
 
 			//set PV_Origin
 			sName = (String)req.getParameter("selPVSource");
 			if(sName == null) sName = "";
 			m_PV.setPV_VALUE_ORIGIN(sName);
 
 			//set PV_BEGIN_DATE
 			sName = (String)req.getParameter("BeginDate");
 			if(sName == null) sName = "";
 			m_PV.setPV_BEGIN_DATE(sName);
 
 			//set PV_END_DATE
 			sName = (String)req.getParameter("EndDate");
 			if(sName == null) sName = "";
 			m_PV.setPV_END_DATE(sName);
 		}
 		catch (Exception e)
 		{
 			logger.error("Error Occurred in setPVValueFromPage " + e.toString(), e);
 		}
 	} // end of setPVValueFromPage
 
 	/**
 	 * need to allow editing of the existing pv. mark the pv as new and
 	 * update vdpvs list with the old one marking as deleted to remove its relationship with the vd.
 	 * @param pv current pv bean
 	 * @param oldPV old PV bean
 	 * @param req request variable.
 	 * @return PV_Bean modified current pv bean
 	 */
 	private PV_Bean getModifiedPV(PV_Bean pv, PV_Bean oldPV, HttpServletRequest req)
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			if (oldPV == null) oldPV = new PV_Bean();
 			String sOldName = oldPV.getPV_VALUE();
 			if (sOldName == null) sOldName = "";
 			String sOldID = oldPV.getPV_PV_IDSEQ();
 			if (sOldID == null) sOldID = "";
 			String sName = pv.getPV_VALUE();
 			if (sName == null) sName = "";
 			//check if name was changed
 			if (!sName.equals(sOldName) && !sOldID.equals(""))
 			{
 				//make current pv as new
 				// sName = m_util.parsedStringJSPDoubleQuote(sName);
 				//System.out.println(sName + " modify " + sOldName);
 				pv.setPV_PV_IDSEQ("EVS_" + sName);
 				pv.setVP_SUBMIT_ACTION("INS");
 				pv.setPV_VDPVS_IDSEQ("");
 				//mark the old pv as deleted and add it the vector in the end.
 				oldPV.setVP_SUBMIT_ACTION("DEL");
 				oldPV.setPV_CHECKED(false);
 				VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
 				Vector<PV_Bean> vVDPVList = vd.getVD_PV_List();  // (Vector)session.getAttribute("VDPVList");
 				if (vVDPVList == null) vVDPVList = new Vector<PV_Bean>();
 				vVDPVList.addElement(oldPV);
 				// DataManager.setAttribute(session, "VDPVList", vVDPVList);
 				vd.setVD_PV_List(vVDPVList);
 				DataManager.setAttribute(session, "m_VD", vd);
 			}
 		}
 		catch (Exception e)
 		{
 			logger.error("Error Occurred in getModifiedPV " + e.toString(), e);
 		}
 		return pv;
 	}
 
 	/**
 	 * Method to check if name has changed for released element.
 	 *
 	 * @param oldName  String old name
 	 * @param newName String new name
 	 * @param oldStatus  String old status
 	 *
 	 * @return String strValid message if not valid. otherwise empty string.
 	 */
 	public String checkNameDiffForReleased(String oldName, String newName, String oldStatus)
 	{
 		if (oldStatus == null || oldStatus.equals(""))
 			return "";
 
 		if (oldStatus.equals("RELEASED"))
 		{
 			oldName = oldName.trim();
 			newName = newName.trim();
 			if (oldName.equals(newName))
 				return "";
 			else
 				return "Cannot update the Short Name if the workflow status is RELEASED. \n";
 		}
 		else
 			return "";
 	}  //end of checkNameDiffForReleased
 
 	/**
 	 * To get the existing Permissible value idseq from the database for the selected value meaning.
 	 * Called from setVDvalue from page.
 	 * Gets all the attribute values from the bean, sets in parameters, and registers output parameter.
 	 * Calls oracle stored procedure
 	 *   "{call SBREXT_Get_Row.GET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}" to submit
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param pv PV Bean.
 	 *
 	 * @return String return code from the stored procedure call. null if no error occurred.
 	 */
 	public String getPV(HttpServletRequest req, HttpServletResponse res, PV_Bean pv)
 	{
 		ResultSet rs = null;
 		CallableStatement cstmt = null;
 		String sReturnCode = "";  //out
 
 		try
 		{
 			String sValue = pv.getPV_VALUE();
 			String sShortMeaning = pv.getPV_SHORT_MEANING();
 
 			if (m_servlet.getConn() == null)
 				m_servlet.ErrorLogin(req, res);
 			else
 			{
 				cstmt = m_servlet.getConn().prepareCall("{call SBREXT_get_Row.GET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				// register the Out parameters
 				cstmt.registerOutParameter(1,java.sql.Types.VARCHAR);       //return code
 				cstmt.registerOutParameter(2,java.sql.Types.VARCHAR);       //pv id
 				cstmt.registerOutParameter(3,java.sql.Types.VARCHAR);       //value
 				cstmt.registerOutParameter(4,java.sql.Types.VARCHAR);       //short meaning
 				cstmt.registerOutParameter(5,java.sql.Types.VARCHAR);       //meaning description
 				cstmt.registerOutParameter(6,java.sql.Types.VARCHAR);       // high value num
 				cstmt.registerOutParameter(7,java.sql.Types.VARCHAR);       //low value num
 				cstmt.registerOutParameter(8,java.sql.Types.VARCHAR);       //  begin date
 				cstmt.registerOutParameter(9,java.sql.Types.VARCHAR);       //end date
 				cstmt.registerOutParameter(10,java.sql.Types.VARCHAR);       //created by
 				cstmt.registerOutParameter(11,java.sql.Types.VARCHAR);       //date created
 				cstmt.registerOutParameter(12,java.sql.Types.VARCHAR);       //modified by
 				cstmt.registerOutParameter(13,java.sql.Types.VARCHAR);       //date modified
 
 				// Set the In parameters (which are inherited from the PreparedStatement class)
 				cstmt.setString(3,sValue);
 				cstmt.setString(4,sShortMeaning);
 
 				// Now we are ready to call the stored procedure
 				//boolean bExcuteOk =
 				cstmt.execute();
 				sReturnCode = cstmt.getString(1);
 				pv.setPV_PV_IDSEQ(cstmt.getString(2));
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("ERROR in setACService-getPV for other : " + e.toString(), e);
 		}finally {
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return sReturnCode;
 	}
 
 	/**
 	 * Check if the permissible values associated with the form for the selected VD
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param vdIDseq string unique id for value domain.
 	 * @param vpIDseq string unique id for vd pvs table.
 	 *
 	 * @return boolean true if pv is associated with the form false otherwise
 	 */
 	public boolean checkPVQCExists(HttpServletRequest req, HttpServletResponse res,
 			String vdIDseq, String vpIDseq) //throws Exception
 	{
 		ResultSet rs = null;
 		PreparedStatement pstmt = null;
 		boolean isValid = false;
 		try
 		{
 			if (m_servlet.getConn() == null)
 				m_servlet.ErrorLogin(req, res);
 			else
 			{
 				pstmt = m_servlet.getConn().prepareStatement("select SBREXT_COMMON_ROUTINES.VD_PVS_QC_EXISTS(?,?) from DUAL");
 				// register the Out parameters
 				pstmt.setString(1, vpIDseq);
 				pstmt.setString(2, vdIDseq);
 				// Now we are ready to call the function
 				rs = pstmt.executeQuery();
 				while (rs.next())
 				{
 					if (rs.getString(1).equalsIgnoreCase("TRUE"))
 						isValid = true;
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("ERROR in setACService-checkPVQCExists for other : " + e.toString(), e);
 		}finally {
 			rs = SQLHelper.closeResultSet(rs);
 			pstmt = SQLHelper.closePreparedStatement(pstmt);
 		}
 		return isValid;
 	}   //end checkPVQCExists
 
 	/**
 	 * gets pv ids from the page and checks if there are any new ones or any removed ones
 	 * updates the pv list vector both for id and bean and stores them in the session.
 	 *
 	 * @param req servlet request
 	 * @param res servlet response
 	 */
 	@SuppressWarnings("unchecked")
 	public void addRemovePageVDPVs(HttpServletRequest req, HttpServletResponse res) //throws Exception
 	{
 		try
 		{
 			HttpSession session = req.getSession();
 			//get the current pvs on the page
 			String pvAction = (String)session.getAttribute("PVAction");
 			if (pvAction == null) pvAction = "";
 
 			VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
 			if (vd == null) vd = new VD_Bean();
 			String strInvalid = "";
 			Vector<PV_Bean> vVDPVList = vd.getVD_PV_List();  // (Vector)session.getAttribute("VDPVList");
 			if (vVDPVList == null) vVDPVList = new Vector<PV_Bean>();
 			if (!pvAction.equalsIgnoreCase("") && !pvAction.equalsIgnoreCase("createPV"))
 			{
 				if (vVDPVList != null && vVDPVList.size() > 0)
 				{
 					//do the following only if edit or remove
 					int iPVChecked = 0;
 					int iPVLoop = 0;
 					//for (int j=0; j<sPV_IDs.length; j++)  //loop through pvid lists
 					for (int j=0; j<vVDPVList.size(); j++)  //loop through pvid lists
 					{
 						PV_Bean pvBean = (PV_Bean)vVDPVList.elementAt(j);
 						if (pvBean == null) pvBean = new PV_Bean();
 						//do the pv count only if not deleted
 						String pvAct = pvBean.getVP_SUBMIT_ACTION();
 						if (pvAct != null && pvAct.equals("DEL"))
 							continue;
 						//check if this id is selected
 						String rSel = (String)req.getParameter("ck"+iPVLoop);
 						//increase the pvloop count
 						iPVLoop += 1;
 						if (rSel != null)
 						{
 							//since double quote from remove the double quote to match id from the jsp.
 							String pvID = pvBean.getPV_PV_IDSEQ();
 							if (pvID == null) pvID = "";
 							pvID = pvID.replace('"', ' ');
 							//store match pv id in the vector
 							if (pvAction.equals("removePV"))
 							{
 								//check if it is associated with the valid value in the form
 								String vdID = vd.getVD_VD_IDSEQ();
 								String vpID = pvBean.getPV_VDPVS_IDSEQ();
 								boolean isExists = false;
 								if (vdID != null && !vdID.equals("") && vpID != null && !vpID.equals(""))
 								{
 									isExists = this.checkPVQCExists(req, res, vdID, vpID);
 									if (isExists)
 									{
 										strInvalid = strInvalid + "Unable to remove the Permissible Value " +
 										pvBean.getPV_VALUE() + " because it is used in a CRF.\\n";
 										pvBean.setVP_SUBMIT_ACTION("NONE");
 									}
 								}
 								if (!isExists)
 								{
 									pvBean.setVP_SUBMIT_ACTION("DEL");  //mark as del if removed from the page
 									//put crf value back to non matched when deleted pv
 									String sVV = pvBean.getQUESTION_VALUE();
 									if (sVV != null && !sVV.equals(""))
 									{
 										Vector<String> vVV = (Vector)session.getAttribute("NonMatchVV");
 										if (vVV == null) vVV = new Vector<String>();
 										if (!vVV.contains(sVV))
 											vVV.addElement(sVV);
 										DataManager.setAttribute(session, "NonMatchVV", vVV);
 									}
 								}
 							}
 							else
 							{  //set edit/create attributes
 								pvBean.setVP_SUBMIT_ACTION("UPD");
 								pvBean.setPV_CHECKED(true);
 								// vCheckRow.addElement(pvID);
 								iPVChecked += 1;
 								if (iPVChecked == 1)
 									DataManager.setAttribute(session, "m_PV", pvBean);
 							}
 							vVDPVList.setElementAt(pvBean, j);  //reset the attribute with the bean attributes
 						}  //end rsel check
 					} //end vdpv loop
 					if (iPVChecked >1)
 						DataManager.setAttribute(session, "m_PV", new PV_Bean());
 				}  //end vdpv has value
 			} //end if not create pv
 			//store it in session
 			// DataManager.setAttribute(session, "VDPVList", vVDPVList);
 			vd.setVD_PV_List(vVDPVList);
 			DataManager.setAttribute(session, "m_VD", vd);
 			Vector<PV_Bean> oldVDPVList = new Vector<PV_Bean>();
 			for (int k =0; k<vVDPVList.size(); k++)
 			{
 				PV_Bean cBean = new PV_Bean();
 				cBean = cBean.copyBean((PV_Bean)vVDPVList.elementAt(k));
 				oldVDPVList.addElement(cBean);
 				//System.out.println(cBean.getPV_BEGIN_DATE() + " what is at set " + cBean.getPV_END_DATE());
 			}
 			DataManager.setAttribute(session, "oldVDPVList", oldVDPVList);  //stor eit in the session
 			if (!strInvalid.equals(""))
 			{
 				m_servlet.storeStatusMsg(strInvalid);
 			}
 		}
 		catch(Exception ee)
 		{
 			logger.error("ERROR in setACService-addRemovePageVDPVs : " + ee.toString(), ee);
 		}
 	}  //end addremovepagevdpvs
 
 	/**
 	 * @param vValidate
 	 * @return Vector of string
 	 */
 	public static Vector<String> makeStringVector(Vector<ValidateBean> vValidate)
 	{
 		Vector<String> vs = new Vector<String>();
 		for (int i =0; i<vValidate.size(); i++)
 		{
 			ValidateBean vbean = (ValidateBean)vValidate.elementAt(i);
 			vs.addElement(vbean.getACAttribute());
 			vs.addElement(vbean.getAttributeContent());
 			vs.addElement(vbean.getAttributeStatus());
 		}
 		return vs;
 	}
 }   //close the class
