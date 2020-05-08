 /*
  * Created on Dec 3, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.security.upt.actions;
 
 /**
  *
  *<!-- LICENSE_TEXT_START -->
  *
  *The NCICB Common Security Module's User Provisioning Tool (UPT) Software License,
  *Version 3.0 Copyright 2004-2005 Ekagra Software Technologies Limited ('Ekagra')
  *
  *Copyright Notice.  The software subject to this notice and license includes both
  *human readable source code form and machine readable, binary, object code form
  *(the 'UPT Software').  The UPT Software was developed in conjunction with the
  *National Cancer Institute ('NCI') by NCI employees and employees of Ekagra.  To
  *the extent government employees are authors, any rights in such works shall be
  *subject to Title 17 of the United States Code, section 105.    
  *
  *This UPT Software License (the 'License') is between NCI and You.  'You (or
  *'Your') shall mean a person or an entity, and all other entities that control,
  *are controlled by, or are under common control with the entity.  'Control' for
  *purposes of this definition means (i) the direct or indirect power to cause the
  *direction or management of such entity, whether by contract or otherwise, or
  *(ii) ownership of fifty percent (50%) or more of the outstanding shares, or
  *(iii) beneficial ownership of such entity.  
  *
  *This License is granted provided that You agree to the conditions described
  *below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  *no-charge, irrevocable, transferable and royalty-free right and license in its
  *rights in the UPT Software to (i) use, install, access, operate, execute, copy,
  *modify, translate, market, publicly display, publicly perform, and prepare
  *derivative works of the UPT Software; (ii) distribute and have distributed to
  *and by third parties the UPT Software and any modifications and derivative works
  *thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to
  *third parties, including the right to license such rights to further third
  *parties.  For sake of clarity, and not by way of limitation, NCI shall have no
  *right of accounting or right of payment from You or Your sublicensees for the
  *rights granted under this License.  This License is granted at no charge to You.
  *
  *1.	Your redistributions of the source code for the Software must retain the
  *above copyright notice, this list of conditions and the disclaimer and
  *limitation of liability of Article 6 below.  Your redistributions in object code
  *form must reproduce the above copyright notice, this list of conditions and the
  *disclaimer of Article 6 in the documentation and/or other materials provided
  *with the distribution, if any.
  *2.	Your end-user documentation included with the redistribution, if any, must
  *include the following acknowledgment: 'This product includes software developed
  *by Ekagra and the National Cancer Institute.'  If You do not include such
  *end-user documentation, You shall include this acknowledgment in the Software
  *itself, wherever such third-party acknowledgments normally appear.
  *
  *3.	You may not use the names 'The National Cancer Institute', 'NCI' 'Ekagra
  *Software Technologies Limited' and 'Ekagra' to endorse or promote products
  *derived from this Software.  This License does not authorize You to use any
  *trademarks, service marks, trade names, logos or product names of either NCI or
  *Ekagra, except as required to comply with the terms of this License.
  *
  *4.	For sake of clarity, and not by way of limitation, You may incorporate this
  *Software into Your proprietary programs and into any third party proprietary
  *programs.  However, if You incorporate the Software into third party proprietary
  *programs, You agree that You are solely responsible for obtaining any permission
  *from such third parties required to incorporate the Software into such third
  *party proprietary programs and for informing Your sublicensees, including
  *without limitation Your end-users, of their obligation to secure any required
  *permissions from such third parties before incorporating the Software into such
  *third party proprietary software programs.  In the event that You fail to obtain
  *such permissions, You agree to indemnify NCI for any claims against NCI by such
  *third parties, except to the extent prohibited by law, resulting from Your
  *failure to obtain such permissions.
  *
  *5.	For sake of clarity, and not by way of limitation, You may add Your own
  *copyright statement to Your modifications and to the derivative works, and You
  *may provide additional or different license terms and conditions in Your
  *sublicenses of modifications of the Software, or any derivative works of the
  *Software as a whole, provided Your use, reproduction, and distribution of the
  *Work otherwise complies with the conditions stated in this License.
  *
  *6.	THIS SOFTWARE IS PROVIDED 'AS IS,' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  *(INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  *NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO
  *EVENT SHALL THE NATIONAL CANCER INSTITUTE, EKAGRA, OR THEIR AFFILIATES BE LIABLE
  *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  *TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  *THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *<!-- LICENSE_TEXT_END -->
  *
  */
 
 
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.upt.constants.DisplayConstants;
 import gov.nih.nci.security.upt.constants.ForwardConstants;
 import gov.nih.nci.security.upt.forms.BaseDoubleAssociationForm;
 import gov.nih.nci.security.upt.forms.LoginForm;
 
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Category;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 /**
  * @author Kunal Modi (Ekagra Software Technologies Ltd.)
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class CommonDoubleAssociationAction extends CommonAssociationAction
 {
 	private static final Category logDoubleAssociation = Category.getInstance(CommonDoubleAssociationAction.class);	
 	
 	public ActionForward loadDoubleAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDoubleAssociationForm baseDoubleAssociationForm = (BaseDoubleAssociationForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug("||"+baseDoubleAssociationForm.getFormName()+"|loadDoubleAssociation|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		try
 		{
 			baseDoubleAssociationForm.buildDoubleAssociationObject(request);
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|loadDoubleAssociation|Failure|Error Loading Double Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		if (logDoubleAssociation.isDebugEnabled())
 			logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDoubleAssociationForm.getFormName()+"|loadDoubleAssociation|Success|Success in Loading Double Association for "+baseDoubleAssociationForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.LOAD_DOUBLEASSOCIATION_SUCCESS));
 	}
 	
 	public ActionForward setDoubleAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDoubleAssociationForm baseDoubleAssociationForm = (BaseDoubleAssociationForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug("||"+baseDoubleAssociationForm.getFormName()+"|setDoubleAssociation|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());
 		try
 		{
 			baseDoubleAssociationForm.buildDisplayForm(request);
 			baseDoubleAssociationForm.setDoubleAssociationObject(request);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, "Association Update Successful"));
 			saveMessages( request, messages );
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|setDoubleAssociation|Failure|Error Setting Double Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		if (logDoubleAssociation.isDebugEnabled())
 			logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDoubleAssociationForm.getFormName()+"|setDoubleAssociation|Success|Success in Setting Double Association for "+baseDoubleAssociationForm.getFormName()+" object|"
 				+form.toString()+"|");		
 		return (mapping.findForward(ForwardConstants.SET_DOUBLEASSOCIATION_SUCCESS));
 	}
 	
 	public ActionForward loadProtectionGroupAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDoubleAssociationForm baseDoubleAssociationForm = (BaseDoubleAssociationForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug("||"+baseDoubleAssociationForm.getFormName()+"|loadProtectionGroupAssociation|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		try
 		{
 			Collection associatedProtectionGroupRoleContexts = baseDoubleAssociationForm.buildProtectionGroupAssociationObject(request);
 			if (associatedProtectionGroupRoleContexts.size() != 0)
 				session.setAttribute(DisplayConstants.AVAILABLE_PROTECTIONGROUPROLECONTEXT_SET, associatedProtectionGroupRoleContexts);
 			else
 			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "No Associated Protection Group and Roles Found"));			
 				saveErrors( request,errors );
 				if (logDoubleAssociation.isDebugEnabled())
 					logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 						"|"+baseDoubleAssociationForm.getFormName()+"|loadProtectionGroupAssociation|Failure|No Protection Group Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 						+form.toString()+"|");	
 				return (mapping.findForward(ForwardConstants.LOAD_PROTECTIONGROUPASSOCIATION_FAILURE));
 			}
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|loadProtectionGroupAssociation|Failure|Error Loading Protection Group Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		if (logDoubleAssociation.isDebugEnabled())
 			logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDoubleAssociationForm.getFormName()+"|loadProtectionGroupAssociation|Success|Success in Loading Protection Group Association for "+baseDoubleAssociationForm.getFormName()+" object|"
 				+form.toString()+"|");			
 		return (mapping.findForward(ForwardConstants.LOAD_PROTECTIONGROUPASSOCIATION_SUCCESS));		
 	}
 	
 	public ActionForward loadProtectionElementPrivilegesAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDoubleAssociationForm baseDoubleAssociationForm = (BaseDoubleAssociationForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug("||"+baseDoubleAssociationForm.getFormName()+"|loadProtectionElementPrivilegesAssociation|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		try
 		{
 			Collection associatedProtectionElementPrivilegesContexts = baseDoubleAssociationForm.buildProtectionElementPrivilegesObject(request);
 			if (associatedProtectionElementPrivilegesContexts != null && associatedProtectionElementPrivilegesContexts.size() != 0)
 				session.setAttribute(DisplayConstants.AVAILABLE_PROTECTIONELEMENTPRIVILEGESCONTEXT_SET, associatedProtectionElementPrivilegesContexts);
 			else
 			{
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "No Associated Protection Element and Privileges Found"));			
 				saveErrors( request,errors );
 				if (logDoubleAssociation.isDebugEnabled())
 					logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 						"|"+baseDoubleAssociationForm.getFormName()+"|loadProtectionElementPrivilegesAssociation|Failure|No Protection Element Privileges Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 						+form.toString()+"|");	
 				return (mapping.findForward(ForwardConstants.LOAD_PROTECTIONELEMENTPRIVILEGESASSOCIATION_FAILURE));
 			}
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|loadProtectionElementPrivilegesAssociation|Failure|Error Loading Protection Element Privileges Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		if (logDoubleAssociation.isDebugEnabled())
 			logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDoubleAssociationForm.getFormName()+"|loadProtectionElementPrivilegesAssociation|Success|Success in Loading Protection Element Privileges Association for "+baseDoubleAssociationForm.getFormName()+" object|"
 				+form.toString()+"|");			
 		return (mapping.findForward(ForwardConstants.LOAD_PROTECTIONELEMENTPRIVILEGESASSOCIATION_SUCCESS));		
 	}
 
 	public ActionForward removeProtectionGroupAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDoubleAssociationForm baseDoubleAssociationForm = (BaseDoubleAssociationForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug("||"+baseDoubleAssociationForm.getFormName()+"|removeProtectionGroupAssociation|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());
 		if (baseDoubleAssociationForm.getProtectionGroupAssociatedId() == null || baseDoubleAssociationForm.getProtectionGroupAssociatedId().equalsIgnoreCase(""))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "A record needs to be selected first" ));
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|removeProtectionGroupAssociation|Failure|No Protection Group Id selected for "+baseDoubleAssociationForm.getFormName()+" object||");
 			return (mapping.findForward(ForwardConstants.LOAD_PROTECTIONGROUPASSOCIATION_SUCCESS));
 		}
 		try
 		{
 			baseDoubleAssociationForm.buildDisplayForm(request);
 			baseDoubleAssociationForm.removeProtectionGroupAssociation(request);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, "Protection Group Successfully removed"));
 			saveMessages( request, messages );
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|removeProtectionGroupAssociation|Failure|Error Removing Protection Group Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());			
 		}
 		if (logDoubleAssociation.isDebugEnabled())
 			logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDoubleAssociationForm.getFormName()+"|removeGroupAssociation|Success|Success in Removing Protection Group Association for "+baseDoubleAssociationForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.REMOVE_PROTECTIONGROUPASSOCIATION_SUCCESS));		
 	}
 	
 	public ActionForward loadRoleAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDoubleAssociationForm baseDoubleAssociationForm = (BaseDoubleAssociationForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug("||"+baseDoubleAssociationForm.getFormName()+"|loadRoleAssociation|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		if (baseDoubleAssociationForm.getProtectionGroupAssociatedId() == null || baseDoubleAssociationForm.getProtectionGroupAssociatedId().equalsIgnoreCase(""))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "A record needs to be selected first" ));
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|loadRoleAssociation|Failure|No Protection Group Id selected for "+baseDoubleAssociationForm.getFormName()+" object||");
 			return (mapping.findForward(ForwardConstants.LOAD_PROTECTIONGROUPASSOCIATION_SUCCESS));
 		}		
 		try
 		{
 			baseDoubleAssociationForm.buildRoleAssociationObject(request);
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|loadRoleAssociation|Failure|Error Loading Role Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		if (logDoubleAssociation.isDebugEnabled())
 			logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDoubleAssociationForm.getFormName()+"|loadRoleAssociation|Success|Success in Loading Role Association for "+baseDoubleAssociationForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.LOAD_ROLEASSOCIATION_SUCCESS));		
 	}
 	
 	public ActionForward setRoleAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionErrors errors = new ActionErrors();
 		ActionMessages messages = new ActionMessages();
 		
 		HttpSession session = request.getSession();
 		BaseDoubleAssociationForm baseDoubleAssociationForm = (BaseDoubleAssociationForm)form;
 		
 		if (session.isNew() || (session.getAttribute(DisplayConstants.LOGIN_OBJECT) == null)) {
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug("||"+baseDoubleAssociationForm.getFormName()+"|setRoleAssociation|Failure|No Session or User Object Forwarding to the Login Page||");
 			return mapping.findForward(ForwardConstants.LOGIN_PAGE);
 		}
 		UserInfoHelper.setUserInfo(((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId(), session.getId());
 		try
 		{
 			baseDoubleAssociationForm.buildDisplayForm(request);
 			baseDoubleAssociationForm.updateRoleAssociation(request);
 			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(DisplayConstants.MESSAGE_ID, "Association Update Successful"));
 			saveMessages( request, messages );
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (logDoubleAssociation.isDebugEnabled())
 				logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 					"|"+baseDoubleAssociationForm.getFormName()+"|setRoleAssociation|Failure|Error Setting Role Association for the "+baseDoubleAssociationForm.getFormName()+" object|"
 					+form.toString()+"|"+ cse.getMessage());
 		}
 		if (logDoubleAssociation.isDebugEnabled())
 			logDoubleAssociation.debug(session.getId()+"|"+((LoginForm)session.getAttribute(DisplayConstants.LOGIN_OBJECT)).getLoginId()+
 				"|"+baseDoubleAssociationForm.getFormName()+"|setRoleAssociation|Success|Success in Setting Role Association for "+baseDoubleAssociationForm.getFormName()+" object|"
 				+form.toString()+"|");
 		return (mapping.findForward(ForwardConstants.SET_ROLEASSOCIATION_SUCCESS));		
 	}
 
 
 }
