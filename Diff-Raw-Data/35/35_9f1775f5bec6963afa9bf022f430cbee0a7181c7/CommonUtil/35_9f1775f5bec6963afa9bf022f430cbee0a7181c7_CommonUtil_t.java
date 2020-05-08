 /**
  * Copyright Notice. Copyright 2008 ScenPro, Inc (caBIG
  * Participant).caXchange was created with NCI funding and is part of the
  * caBIG initiative. The software subject to this notice and license includes
  * both human readable source code form and machine readable, binary, object
  * code form (the caBIG Software). This caBIG Software License (the
  * License) is between caBIG Participant and You. You (or Your) shall mean
  * a person or an entity, and all other entities that control, are controlled
  * by, or are under common control with the entity. Control for purposes of
  * this definition means (i) the direct or indirect power to cause the direction
  * or management of such entity, whether by contract or otherwise, or (ii)
  * ownership of fifty percent (50%) or more of the outstanding shares, or (iii)
  * beneficial ownership of such entity. License. Provided that You agree to the
  * conditions described below, caBIG Participant grants You a non-exclusive,
  * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and
  * royalty-free right and license in its rights in the caBIG Software,
  * including any copyright or patent rights therein, to (i) use, install,
  * disclose, access, operate, execute, reproduce, copy, modify, translate,
  * market, publicly display, publicly perform, and prepare derivative works of
  * the caBIG Software in any manner and for any purpose, and to have or permit
  * others to do so; (ii) make, have made, use, practice, sell, and offer for
  * sale, import, and/or otherwise dispose of caBIG Software (or portions
  * thereof); (iii) distribute and have distributed to and by third parties the
  * caBIG Software and any modifications and derivative works thereof; and (iv)
  * sublicense the foregoing rights set out in (i), (ii) and (iii) to third
  * parties, including the right to license such rights to further third parties.
  * For sake of clarity, and not by way of limitation, caBIG Participant shall
  * have no right of accounting or right of payment from You or Your sublicensees
  * for the rights granted under this License. This License is granted at no
  * charge to You. Your downloading, copying, modifying, displaying, distributing
  * or use of caBIG Software constitutes acceptance of all of the terms and
  * conditions of this Agreement. If you do not agree to such terms and
  * conditions, you have no right to download, copy, modify, display, distribute
  * or use the caBIG Software. 1. Your redistributions of the source code for
  * the caBIG Software must retain the above copyright notice, this list of
  * conditions and the disclaimer and limitation of liability of Article 6 below.
  * Your redistributions in object code form must reproduce the above copyright
  * notice, this list of conditions and the disclaimer of Article 6 in the
  * documentation and/or other materials provided with the distribution, if any.
  * 2. Your end-user documentation included with the redistribution, if any, must
  * include the following acknowledgment: This product includes software
  * developed by ScenPro, Inc. If You do not include such end-user
  * documentation, You shall include this acknowledgment in the caBIG Software
  * itself, wherever such third-party acknowledgments normally appear. 3. You may
  * not use the names ScenPro, Inc, The National Cancer Institute, NCI,
  * Cancer Bioinformatics Grid or caBIG to endorse or promote products
  * derived from this caBIG Software. This License does not authorize You to use
  * any trademarks, service marks, trade names, logos or product names of either
  * caBIG Participant, NCI or caBIG, except as required to comply with the
  * terms of this License. 4. For sake of clarity, and not by way of limitation,
  * You may incorporate this caBIG Software into Your proprietary programs and
  * into any third party proprietary programs. However, if You incorporate the
  * caBIG Software into third party proprietary programs, You agree that You are
  * solely responsible for obtaining any permission from such third parties
  * required to incorporate the caBIG Software into such third party proprietary
  * programs and for informing Your sublicensees, including without limitation
  * Your end-users, of their obligation to secure any required permissions from
  * such third parties before incorporating the caBIG Software into such third
  * party proprietary software programs. In the event that You fail to obtain
  * such permissions, You agree to indemnify caBIG Participant for any claims
  * against caBIG Participant by such third parties, except to the extent
  * prohibited by law, resulting from Your failure to obtain such permissions. 5.
  * For sake of clarity, and not by way of limitation, You may add Your own
  * copyright statement to Your modifications and to the derivative works, and
  * You may provide additional or different license terms and conditions in Your
  * sublicenses of modifications of the caBIG Software, or any derivative works
  * of the caBIG Software as a whole, provided Your use, reproduction, and
  * distribution of the Work otherwise complies with the conditions stated in
  * this License. 6. THIS caBIG SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED
  * OR IMPLIED WARRANTIES (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE)
  * ARE DISCLAIMED. IN NO EVENT SHALL THE ScenPro, Inc OR ITS AFFILIATES BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS caBIG SOFTWARE, EVEN IF ADVISED OF
  * THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caxchange.ctom.viewer.util;
 
 import gov.nih.nci.caxchange.ctom.viewer.constants.DisplayConstants;
 import gov.nih.nci.caxchange.ctom.viewer.forms.LoginForm;
 
import java.io.*;
 import java.util.Properties;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class CommonUtil.
  * 
  * @author asharma
  */
 public class CommonUtil
 {
 
 	/** The Constant CONFIG_FILE_1. */
 	private static final String CONFIG_FILE_1 = "/baseURL.properties";
 
 	/** The Constant CONFIG_FILE_2. */
 	private static final String CONFIG_FILE_2 = "/labviewer.properties";
 
 	/** The Constant log. */
 	private static final Logger log = Logger.getLogger(CommonUtil.class);
    public static Properties props1 = new Properties();
    static {
        try {
            props1.load(new FileInputStream(CONFIG_FILE_1));
        } catch(IOException ioe) {
            log.error("Error reading the config file: " + CONFIG_FILE_1);
        }
    }
 
    /**
 	 * Get the properties from properties file.
 	 * 
 	 * @param session the session
 	 */
 	public void getProperties(HttpSession session)
 	{
 
		//try
		//{
 
            //File file = new File(session.getServletContext().getRealPath(CONFIG_FILE_1));
            //InputStream stream = getClass().getResourceAsStream(CONFIG_FILE_1);
			//props1.load(stream);
            //props1.load(new FileInputStream(CONFIG_FILE_1));
 			String caAERSurl = (String) props1.getProperty("BaseURLcaAERS");
 			String c3prurl = (String) props1.getProperty("BaseURLC3PR");
 			String C3Durl = (String) props1.getProperty("BaseURLC3D");
 			String url = (String) props1.getProperty("url");
 			String tissueurl = (String) props1.getProperty("tissueURL");
 
 			session.setAttribute("BaseURLcaAERS", caAERSurl);
 			session.setAttribute("BaseURLC3PR", c3prurl);
 			session.setAttribute("caXchangeURL", url);
 			session.setAttribute("BaseURLC3D", C3Durl);
 			session.setAttribute("tissueURL", tissueurl);
		/*}
 		catch (FileNotFoundException e1)
 		{
 			log.error("The config file not found: " + CONFIG_FILE_1);
 		}
 		catch (IOException e1)
 		{
 			log.error("Error reading the config file: " + CONFIG_FILE_1);
 		}
        */
 		try
 		{
 			Properties props = new Properties();
 			InputStream stream = getClass().getResourceAsStream(CONFIG_FILE_2);
 			props.load(stream);
 			String testEnabled = (String) props.getProperty("testEnabled");
 			session.setAttribute("testEnabled", testEnabled);
 			String webssoEnabled = (String) props.getProperty("websso.enabled");
 			session.setAttribute("webssoEnabled", webssoEnabled);
 			String webssoCasServer = (String) props.getProperty("websso.cas.server");
 			session.setAttribute("webssoCasServer", webssoCasServer);
 			String version = (String) props.getProperty("version");
 			session.setAttribute("version", version);
 			// hotlink type can be _blank,_self,${hotLink_NAME}:value set in
 			// properties file
 			String hotLinkType = (String) props.getProperty("hotLink_Type");
 			session.setAttribute("hotLinkType", hotLinkType);
 			String propertyFilePath = (String) props.getProperty("propertyFilePath");
 			session.setAttribute("propertyFilePath", propertyFilePath);
 
 		}
 		catch (FileNotFoundException e1)
 		{
 			log.error("The config file not found: " + CONFIG_FILE_2);
 		}
 		catch (IOException e1)
 		{
 			log.error("Error reading the config file: " + CONFIG_FILE_2);
 		}
 	}
 
 	/**
 	 * Check user login.
 	 * 
 	 * @param session the session
 	 * 
 	 * @return the string
 	 */
 	public String checkUserLogin(HttpSession session)
 	{
 
 		String userEmail = null;
 		// If logged in via WEBSSO
 		String gridIDentity = (String) session.getAttribute("CAGRID_SSO_GRID_IDENTITY");
 		if (gridIDentity != null)
 		{
 			int beginIndex = gridIDentity.lastIndexOf("=");
 			int endIndex = gridIDentity.length();
 			userEmail = gridIDentity.substring(beginIndex + 1, endIndex);
 		}
 		// if logged in via CSM
 		if (userEmail == null)
 		{
 			LoginForm loginForm = (LoginForm) session.getAttribute(DisplayConstants.LOGIN_OBJECT);
 			if (loginForm != null)
 			{
 				userEmail = loginForm.getLoginId();
 			}
 
 		}
 		return userEmail;
 	}
 
 	/**
 	 * Clear menu session data.
 	 * 
 	 * @param session the session
 	 */
 	public void clearMenuSessionData(HttpSession session)
 	{
 
 		session.removeAttribute(DisplayConstants.CURRENT_ACTION);
 		session.removeAttribute(DisplayConstants.CURRENT_FORM);
 		session.removeAttribute("StudySearchForm");
 		session.removeAttribute("ParticipantSearchForm");
 		session.removeAttribute("LabActivitiesSearchForm");
 		session.removeAttribute("HealthCareSiteForm");
 		session.removeAttribute("PIForm");
 		session.removeAttribute(DisplayConstants.SEARCH_RESULT);
 		session.removeAttribute(DisplayConstants.SEARCH_RESULT_COUNT);
 		session.removeAttribute("pageTitle");
 		session.removeAttribute("StudyTitle");
 		session.removeAttribute("studyId");
 		session.removeAttribute("ID");
 		session.removeAttribute("participantId");
 		session.removeAttribute("patientId");
 	}
 
 	/**
 	 * Clear session data.
 	 * 
 	 * @param session the session
 	 */
 	public void clearSessionData(HttpSession session)
 	{
 
 		clearMenuSessionData(session);
 	}
 
 	/**
 	 * Clear study session data.
 	 * 
 	 * @param session the session
 	 */
 	public void clearStudySessionData(HttpSession session)
 	{
 
 		clearMenuSessionData(session);
 	}
 
 	/**
 	 * Clear participant session data.
 	 * 
 	 * @param session the session
 	 */
 	public void clearParticipantSessionData(HttpSession session)
 	{
 
 		clearMenuSessionData(session);
 	}
 
 	/**
 	 * Clear lab session data.
 	 * 
 	 * @param session the session
 	 */
 	public void clearLabSessionData(HttpSession session)
 	{
 
 		session.removeAttribute(DisplayConstants.CURRENT_FORM);
 		session.removeAttribute(DisplayConstants.SEARCH_RESULT);
 		session.removeAttribute(DisplayConstants.SEARCH_RESULT_COUNT);
 		session.removeAttribute("ALL_SEARCH_RESULT");
 
 	}
 
 }
